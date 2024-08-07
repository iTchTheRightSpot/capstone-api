package dev.webserver.cart;

import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.exception.CustomInvalidFormatException;
import dev.webserver.exception.CustomNotFoundException;
import dev.webserver.exception.OutOfStockException;
import dev.webserver.external.aws.IS3Service;
import dev.webserver.product.Product;
import dev.webserver.product.ProductSku;
import dev.webserver.product.ProductSkuService;
import dev.webserver.util.CustomUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Supplier;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    // cart expiration
    private static final Instant expiration = Instant.now().plus(2, DAYS);

    @Setter @Getter
    @Value(value = "${cart.split}")
    private String split;
    @Setter @Getter
    @Value(value = "${aws.bucket}")
    private String BUCKET;
    @Setter @Getter
    @Value("${cart.cookie.name}")
    private String CARTCOOKIE;
    @Setter @Getter
    @Value(value = "${server.servlet.session.cookie.secure}")
    private boolean COOKIESECURE;
    @Setter @Getter
    @Value("${shopping.session.expiration.bound}")
    private long bound;

    private final ShoppingSessionRepository shoppingSessionRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductSkuService productSKUService;
    private final IS3Service s3Service;

    /**
     * Updates the expiration of a cookie if it is within the expiration period.
     * If the cookie is valid and is within {@link #bound}, cookie is updated and
     * sent back in the response.
     *
     * @param res    the HttpServletResponse object to add the updated cookie to
     * @param cookie the Cookie object to validate and update
     * @throws CustomInvalidFormatException if the cookie value is invalid or cannot
     * be parsed.
     */
    public void validateCookieExpiration(HttpServletResponse res, Cookie cookie) {
        try {
            String[] arr = cookie.getValue().split(split);

            Date now = CustomUtil.toUTC(new Date());
            long parsed = Long.parseLong(arr[1]);

            Date cookieDate = CustomUtil
                    .toUTC(Date.from(Instant.ofEpochSecond(parsed)));

            Duration between = Duration.between(now.toInstant(), cookieDate.toInstant());

            long hours = between.toHours();

            if (hours <= bound) {
                // update cookie expiry
                long maxAgeInSeconds = Instant.now().until(expiration, ChronoUnit.SECONDS);

                String value = arr[0] + CustomUtil
                        .toUTC(Date.from(expiration)).toInstant().getEpochSecond();

                this.shoppingSessionRepository
                        .updateShoppingSessionExpiry(arr[0], CustomUtil.toUTC(Date.from(expiration)));

                // cookie
                cookie.setValue(value);
                cookie.setPath("/");
                cookie.setMaxAge((int) maxAgeInSeconds);

                res.addCookie(cookie);
            }
        } catch (RuntimeException ex) {
            log.error("validateCookieExpiration method, {}", ex.getMessage());
            throw new CustomInvalidFormatException("invalid cookie");
        }
    }

    /**
     * Retrieves all {@link CartItem} objects asynchronously. These objects are all
     * of the {@link ProductSku} that contain in a users shopping cart.
     * <p>
     * If a cart cookie exists in the request, the method retrieves the cart items associated
     * with the cookie.If custom cookie exists, a new cookie is created and added to the
     * response.
     *
     * @param currency the currency for which cart items should be retrieved.
     * @param req the HttpServletRequest object to retrieve the cart cookie.
     * @param res the HttpServletResponse object to add the new cart cookie if needed.
     * @return a list of {@link CartResponse} objects representing the {@link CartItem}.
     * @throws CustomInvalidFormatException if the cart cookie value is invalid or cannot be parsed.
     */
    public List<CartResponse> cartItems(
            SarreCurrency currency,
            HttpServletRequest req,
            HttpServletResponse res
    ) {
        Cookie cookie = CustomUtil.cookie(req, CARTCOOKIE);

        if (cookie == null) {
            // cookie value
            long maxAgeInSeconds = Instant.now().until(expiration, ChronoUnit.SECONDS);
            String value = UUID.randomUUID() + split + expiration.getEpochSecond();

            // cookie
            Cookie c = new Cookie(CARTCOOKIE, value);
            c.setMaxAge((int) maxAgeInSeconds);
            c.setHttpOnly(true);
            c.setPath("/");
            c.setSecure(COOKIESECURE);

            res.addCookie(c);

            return List.of();
        }

        validateCookieExpiration(res, cookie);

        String[] arr = cookie.getValue().split(split);

        var futures = shoppingSessionRepository
                .cartItemsByCookieValue(currency, arr[0])
                .stream()
                .map(db -> (Supplier<CartResponse>) () -> new CartResponse(
                        db.getUuid(),
                        s3Service.preSignedUrl(BUCKET, db.getKey()),
                        db.getName(),
                        db.getPrice(),
                        db.getCurrency(),
                        db.getColour(),
                        db.getSize(),
                        db.getSku(),
                        db.getQty(),
                        db.getWeight(),
                        db.getWeightType()
                ))
                .toList();

        return CustomUtil.asynchronousTasks(futures).join();
    }

    /**
     * Adds a {@link ProductSku} to a user's shopping cart by creating or updating a
     * {@link ShoppingSession}.
     * <p>
     * Retrieves a unique cookie associated to a user's device from the {@link HttpServletRequest}.
     * This cookie is used to identify the user's {@link ShoppingSession}.
     * <p>
     * If the specified {@link ProductSku} does not exist, a {@link CustomNotFoundException}
     * is thrown. If the {@link Product} is out of stock
     * or the requested quantity exceeds available inventory, an {@link OutOfStockException}
     * is thrown.
     *
     * @param dto the {@link CartDto} containing information about the {@link ProductSku}
     *            and quantity.
     * @param req the {@link HttpServletRequest} containing the unique cookie associated with
     *            the user's device.
     * @throws CustomNotFoundException      if the specified {@link ProductSku} does not exist.
     * @throws OutOfStockException          if the {@link ProductSku} is out of stock or the
     * requested quantity exceeds available inventory.
     * @throws CustomInvalidFormatException if the cookie is invalid.
     */
    @Transactional(rollbackFor = Exception.class)
    public void create(CartDto dto, HttpServletRequest req) {
        Cookie cookie = CustomUtil.cookie(req, CARTCOOKIE);

        if (cookie == null) {
            throw new CustomNotFoundException("no cookie found. Kindly refresh window");
        }

        var productSku = this.productSKUService.productSkuBySku(dto.sku());

        int qty = productSku.getInventory();

        if (qty <= 0 || dto.qty() > qty) {
            throw new OutOfStockException("product or selected quantity is out of stock.");
        }

        String[] arr = cookie.getValue().split(split);

        Optional<ShoppingSession> session = shoppingSessionRepository.shoppingSessionByCookie(arr[0]);

        if (session.isEmpty()) {
            try {
                long parsed = Long.parseLong(arr[1]);
                createNewShoppingSession(
                        arr[0],
                        Date.from(Instant.ofEpochSecond(parsed)),
                        dto.qty(),
                        productSku
                );
            } catch (RuntimeException ex) {
                log.error("create method , {}", ex.getMessage());
                throw new CustomInvalidFormatException("invalid cookie");
            }
        } else {
            addToExistingShoppingSession(session.get(), dto.qty(), productSku);
        }
    }

    /**
     * Creates a new shopping session
     */
    private void createNewShoppingSession(String cookie, Date expiration, int qty, ProductSku sku) {
        var session = this.shoppingSessionRepository.save(
                new ShoppingSession(
                        cookie,
                        CustomUtil.toUTC(new Date()),
                        CustomUtil.toUTC(expiration),
                        new HashSet<>(),
                        new HashSet<>()
                )
        );

        this.cartItemRepository.save(new CartItem(qty, session, sku));
    }

    /**
     * Creates or updates a CartItem
     */
    private void addToExistingShoppingSession(ShoppingSession session, int qty, ProductSku sku) {
        var optional = cartItemRepository
                .cartItemByShoppingSessionIdAndProductSkuSku(
                        session.shoppingSessionId(),
                        sku.getSku()
                );

        if (optional.isEmpty()) {
            this.cartItemRepository.save(new CartItem(qty, session, sku));
        } else {
            this.cartItemRepository.updateCartItemQtyByCartId(optional.get().getCartId(), qty);
        }
    }

    /**
     * Deletes a {@link CartItem} from associated to a {@link ShoppingSession}.
     *
     * @param req the HttpServletRequest object containing a unique cookie for
     *            every device that visit out application.
     * @param sku unique {@link ProductSku}.
     * */
    @Transactional(rollbackFor = Exception.class)
    public void deleteFromCart(HttpServletRequest req, String sku) {
        Cookie cookie = CustomUtil.cookie(req, CARTCOOKIE);

        if (cookie == null) {
            return;
        }

        String[] arr = cookie.getValue().split(split);

        this.cartItemRepository.deleteCartItemByCookieAndSku(arr[0], sku);
    }

}