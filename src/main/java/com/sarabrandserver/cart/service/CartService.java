package com.sarabrandserver.cart.service;

import com.sarabrandserver.aws.S3Service;
import com.sarabrandserver.cart.dto.CartDTO;
import com.sarabrandserver.cart.entity.CartItem;
import com.sarabrandserver.cart.entity.ShoppingSession;
import com.sarabrandserver.cart.repository.CartItemRepo;
import com.sarabrandserver.cart.repository.ShoppingSessionRepo;
import com.sarabrandserver.cart.response.CartResponse;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.exception.CustomInvalidFormatException;
import com.sarabrandserver.exception.CustomNotFoundException;
import com.sarabrandserver.exception.OutOfStockException;
import com.sarabrandserver.product.entity.ProductSku;
import com.sarabrandserver.product.service.ProductSkuService;
import com.sarabrandserver.util.CustomUtil;
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

import java.time.Instant;
import java.util.*;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private final int expire = 2; // cart expiration

    @Setter @Getter
    @Value(value = "${cart.split}")
    private String split;
    @Setter @Getter
    @Value(value = "${aws.bucket}")
    private String BUCKET;
    @Setter @Getter
    @Value("${cart.cookie.name}")
    private String CART_COOKIE;
    @Setter @Getter
    @Value(value = "${server.servlet.session.cookie.secure}")
    private boolean COOKIESECURE;
    @Setter @Getter
    @Value("${shopping.session.expiration.bound}")
    private long bound;

    private final ShoppingSessionRepo shoppingSessionRepo;
    private final CartItemRepo cartItemRepo;
    private final ProductSkuService productSKUService;
    private final S3Service s3Service;

    /**
     * Updates cookie if it is within expiration
     *
     * @throws CustomInvalidFormatException is array is out of bound or parsing of string to a Long
     */
    public void validateCookieExpiration(HttpServletResponse res, Cookie cookie) {
        try {
            String[] arr = cookie.getValue().split(this.split);
            long d = Long.parseLong(arr[1]);
            long calc = d + (this.bound * 3600);

            Date five = CustomUtil.toUTC(new Date(calc));
            Date now = CustomUtil.toUTC(new Date());

            // within expiration bound
            if (five.before(now)) {
                Instant instant = Instant.now().plus(this.expire, DAYS);
                String value = arr[0] + this.split + instant.toEpochMilli();

                // update expiration in db
                Date expiry = CustomUtil.toUTC(new Date(instant.toEpochMilli()));

                this.shoppingSessionRepo.updateShoppingSessionExpiry(arr[0], expiry);

                // change cookie value
                cookie.setValue(value);
                cookie.setPath("/");

                // response
                res.addCookie(cookie);
            }
        } catch (RuntimeException ex) {
            log.error("validateCookieExpiration method, {}", ex.getMessage());
            throw new CustomInvalidFormatException("invalid cookie");
        }
    }

    /**
     * Returns a list of CartResponse
     */
    public List<CartResponse> cartItems(
            SarreCurrency currency,
            HttpServletRequest req,
            HttpServletResponse res
    ) {
        Cookie cookie = CustomUtil.cookie(req, CART_COOKIE);

        if (cookie == null) {
            // cookie value
            Instant instant = Instant.now().plus(this.expire, DAYS);
            String value = UUID.randomUUID() + this.split + instant.toEpochMilli();

            // cookie
            Cookie c = new Cookie(CART_COOKIE, value);
            c.setMaxAge((int) instant.toEpochMilli());
            c.setHttpOnly(true);
            c.setPath("/");
            c.setSecure(COOKIESECURE);

            // add c to res
            res.addCookie(c);

            return new ArrayList<>();
        }

        validateCookieExpiration(res, cookie);

        String[] arr = cookie.getValue().split(this.split);

        return this.shoppingSessionRepo
                .cartItemsByCookieValue(currency, arr[0]) //
                .stream() //
                .map(pojo -> {
                    String url = this.s3Service.preSignedUrl(BUCKET, pojo.getKey());
                    return new CartResponse(
                            pojo.getUuid(),
                            url,
                            pojo.getName(),
                            pojo.getPrice(),
                            pojo.getCurrency(),
                            pojo.getColour(),
                            pojo.getSize(),
                            pojo.getSku(),
                            pojo.getQty()
                    );
                }) //
                .toList();
    }

    /**
     * Creates a new shopping session or persists details into an existing shopping session.
     *
     * @throws CustomNotFoundException if dto property sku does not exist
     * @throws OutOfStockException if dto property qty is greater than inventory
     * @throws CustomInvalidFormatException if cookie is in invalid format
     */
    @Transactional
    public void create(CartDTO dto, HttpServletRequest req) {
        Cookie cookie = CustomUtil.cookie(req, CART_COOKIE);

        if (cookie == null) {
            throw new CustomNotFoundException("No cookie found. Kindly refresh window");
        }

        var productSku = this.productSKUService.productSkuBySKU(dto.sku());

        int qty = productSku.getInventory();

        if (qty <= 0 || dto.qty() > qty) {
            throw new OutOfStockException("Product or selected quantity is out of stock.");
        }

        String[] arr = cookie.getValue().split(this.split);

        Optional<ShoppingSession> session = this.shoppingSessionRepo
                .shoppingSessionByCookie(arr[0]);

        if (session.isEmpty()) {
            Date date;

            try {
                long d = Long.parseLong(arr[1]);
                date = new Date(d);
            } catch (RuntimeException ex) {
                log.error("create method , {}", ex.getMessage());
                throw new CustomInvalidFormatException("invalid cookie");
            }

            create_new_shopping_session(arr[0], date, dto.qty(), productSku);
        } else {
            addToExistingShoppingSession(session.get(), dto.qty(), productSku);
        }
    }

    /**
     * Creates a new shopping session
     */
    private void create_new_shopping_session(String cookie, Date expiration, int qty, ProductSku sku) {
        var session = this.shoppingSessionRepo
                .save(
                        new ShoppingSession(
                                cookie,
                                CustomUtil.toUTC(new Date()),
                                CustomUtil.toUTC(expiration),
                                new HashSet<>(),
                                new HashSet<>()
                        )
                );

        this.cartItemRepo.save(new CartItem(qty, session, sku));
    }

    /**
     * Creates or updates a CartItem
     */
    private void addToExistingShoppingSession(ShoppingSession session, int qty, ProductSku sku) {
        var optional = cartItemRepo
                .cartItemByShoppingSessionIdAndProductSkuSku(
                        session.shoppingSessionId(),
                        sku.getSku()
                );

        if (optional.isEmpty()) {
            this.cartItemRepo.save(new CartItem(qty, session, sku));
        } else {
            // update quantity if cart is present
            this.cartItemRepo.updateCartQtyByCartId(optional.get().getCartId(), qty);
        }
    }

    /**
     * Deletes a CartItem from ShoppingSession based on
     * sku and user ip address
     *
     * @param sku unique ProductSku
     * */
    @Transactional
    public void deleteFromCart(HttpServletRequest req, String sku) {
        Cookie cookie = CustomUtil.cookie(req, CART_COOKIE);

        if (cookie == null) {
            return;
        }

        String[] arr = cookie.getValue().split(this.split);

        this.cartItemRepo.deleteCartItemByCookieAndSku(arr[0], sku);
    }

}