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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.PatternSyntaxException;

import static dev.webserver.util.CustomUtil.TO_GREENWICH;
import static java.time.ZoneOffset.UTC;

@Service
@RequiredArgsConstructor
class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);
    private static final long MAX_CART_EXPIRATION_IN_SECONDS = Duration.ofDays(2).getSeconds();

    @Setter @Getter
    @Value(value = "${cart.split}")
    private String split;
    @Setter @Getter
    @Value(value = "${aws.bucket}")
    private String bucket;
    @Setter @Getter
    @Value("${cart.cookie.name}")
    private String cartcookie;
    @Setter @Getter
    @Value(value = "${server.servlet.session.cookie.secure}")
    private boolean cookiesecure;
    @Setter @Getter
    @Value("${shopping.session.expiration.bound}")
    private long bound;

    private final IShoppingSessionRepository sessionRepository;
    private final ICartRepository cartRepository;
    private final ProductSkuService productSkuService;
    private final IS3Service s3Service;

    /**
     * Updates the expiration of a cookie if it is within the expiration period.
     * If the cookie is valid and is within {@link #bound}, cookie is updated and
     * sent back in the response.
     *
     * @param response    the HttpServletResponse object to add the updated cookie to
     * @param cookie the Cookie object to validate and update
     * @throws CustomInvalidFormatException if the cookie value is invalid or cannot
     * be parsed.
     */
    void validateCookieExpiration(final HttpServletResponse response, final Cookie cookie) {
        try {
            final String[] arr = cookie.getValue().split(split);

            final var now = TO_GREENWICH.apply(null);
            final long parsed = Long.parseLong(arr[1]);

            final var cookieDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(parsed), UTC);

            final Duration between = Duration.between(now, cookieDate);

            final long hours = between.toHours();

            if (hours <= bound) {
                // update cookie expiry
                final LocalDateTime expirationLdt = now.plusSeconds(MAX_CART_EXPIRATION_IN_SECONDS);
                final int maxAgeInSeconds = expirationLdt.getSecond();

                final String value = arr[0] + maxAgeInSeconds;

                sessionRepository.updateShoppingSessionExpiry(arr[0], expirationLdt);

                // cookie
                cookie.setValue(value);
                cookie.setPath("/");
                cookie.setMaxAge(maxAgeInSeconds);

                response.addCookie(cookie);
            }
        } catch (PatternSyntaxException | NumberFormatException ex) {
            log.error("CartService validateCookieExpiration method, {}", ex.getMessage());
            throw new CustomInvalidFormatException("invalid cookie");
        }
    }

    /**
     * Retrieves all {@link Cart} objects asynchronously. These objects are all
     * of the {@link ProductSku} that contain in a users shopping cart.
     * <p>
     * If a cart cookie exists in the request, the method retrieves the cart items associated
     * with the cookie.If custom cookie exists, a new cookie is created and added to the
     * response.
     *
     * @param currency the currency for which cart items should be retrieved.
     * @param req the HttpServletRequest object to retrieve the cart cookie.
     * @param res the HttpServletResponse object to add the new cart cookie if needed.
     * @return a list of {@link CartResponse} objects representing the {@link Cart}.
     * @throws CustomInvalidFormatException if the cart cookie value is invalid or cannot be parsed.
     */
    public List<CartResponse> cartItems(
            final SarreCurrency currency,
            final HttpServletRequest req,
            final HttpServletResponse res
    ) {
        final Cookie cookie = CustomUtil.cookie(req, cartcookie);

        if (cookie == null) {
            // cookie value
            final int maxAgeInSeconds = TO_GREENWICH.apply(null).plusSeconds(MAX_CART_EXPIRATION_IN_SECONDS).getSecond();
            final String value = UUID.randomUUID() + split + maxAgeInSeconds;

            // cookie
            final Cookie c = new Cookie(cartcookie, value);
            c.setMaxAge(maxAgeInSeconds);
            c.setHttpOnly(true);
            c.setPath("/");
            c.setSecure(cookiesecure);

            res.addCookie(c);

            return List.of();
        }

        validateCookieExpiration(res, cookie);

        final String[] arr = cookie.getValue().split(split);

        final var futures = sessionRepository
                .cartItemsByCookieValue(currency, arr[0])
                .stream()
                .map(db -> (Supplier<CartResponse>) () -> new CartResponse(
                        db.uuid(),
                        s3Service.preSignedUrl(bucket, db.imageKey()),
                        db.name(),
                        db.price(),
                        db.currency(),
                        db.colour(),
                        db.size(),
                        db.sku(),
                        db.qty(),
                        db.weight(),
                        db.weightType()
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
    public void create(final CartDto dto, final HttpServletRequest req) {
        final Cookie cookie = CustomUtil.cookie(req, cartcookie);

        if (cookie == null) {
            throw new CustomNotFoundException("please refresh your tab");
        }

        final var sku = productSkuService.productSkuBySku(dto.sku());

        final int qty = sku.inventory();

        if (qty <= 0 || dto.qty() > qty) {
            throw new OutOfStockException("product or selected quantity is out of stock.");
        }

        try {
            final String[] arr = cookie.getValue().split(split);
            final var optional = sessionRepository.shoppingSessionByCookie(arr[0]);

            if (optional.isEmpty()) {
                createNewShoppingSession(arr[0], Long.parseLong(arr[1]), dto.qty(), sku.skuId());
            } else {
                addToExistingShoppingSession(optional.get().sessionId(), dto.qty(), sku);
            }
        } catch (PatternSyntaxException | NumberFormatException ex) {
            log.error("CartService create method , {}", ex.getMessage());
            throw new CustomInvalidFormatException("invalid cookie");
        }
    }

    /**
     * Creates a new shopping session
     */
    private void createNewShoppingSession(final String cookie, final long expirationEpochSeconds, final int qty, final Long skuId) {
        final ShoppingSession session = sessionRepository.save(ShoppingSession.builder()
                .sessionId(null)
                .cookie(cookie)
                .createAt(TO_GREENWICH.apply(null))
                .expireAt(LocalDateTime.ofInstant(Instant.ofEpochSecond(expirationEpochSeconds), UTC))
                .build());

        cartRepository.save(new Cart(null, qty, session.sessionId(), skuId));
    }

    /**
     * Creates or updates a CartItem
     */
    private void addToExistingShoppingSession(final long sessionId, final int qty, final ProductSku sku) {
        final var optional = cartRepository.cartByShoppingSessionIdAndProductSkuSku(sessionId, sku.sku());

        if (optional.isEmpty()) {
            cartRepository.save(new Cart(null, qty, sessionId, sku.skuId()));
        } else {
            cartRepository.updateCartQtyByCartId(optional.get().cartId(), qty);
        }
    }

    /**
     * Deletes a {@link Cart} from associated to a {@link ShoppingSession}.
     *
     * @param req the HttpServletRequest object containing a unique cookie for
     *            every device that visit out application.
     * @param sku unique {@link ProductSku}.
     * */
    @Transactional(rollbackFor = Exception.class)
    public void deleteFromCart(final HttpServletRequest req, final String sku) {
        final Cookie cookie = CustomUtil.cookie(req, cartcookie);

        if (cookie == null) {
            return;
        }

        final String[] arr = cookie.getValue().split(split);

        cartRepository.deleteCartByCookieAndProductSku(arr[0], sku);
    }

}