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
import com.sarabrandserver.product.service.ProductSKUService;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
@RequiredArgsConstructor
@Setter @Getter
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private final int expire = 2; // cart expiration
    private final int expirationBound = 5;
    private final String split = "%";

    @Value(value = "${aws.bucket}")
    private String BUCKET;
    @Value("${cart.cookie.name}")
    private String CART_COOKIE;
    @Value(value = "${server.servlet.session.cookie.secure}")
    private boolean COOKIESECURE;

    private final ShoppingSessionRepo shoppingSessionRepo;
    private final CartItemRepo cartItemRepo;
    private final ProductSKUService productSKUService;
    private final CustomUtil customUtil;
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
            long calc = d + (this.expirationBound * 3600);

            Date five = this.customUtil.toUTC(new Date(calc));
            Date now = this.customUtil.toUTC(new Date());

            // within expiration bound
            if (five.before(now)) {
                Instant instant = Instant.now().plus(this.expire, DAYS);
                String value = arr[0] + this.split + instant.toEpochMilli();

                // update expiration in db
                Date expiry = this.customUtil.toUTC(new Date(instant.toEpochMilli()));

                this.shoppingSessionRepo.updateShoppingSessionExpiry(arr[0], expiry);

                // change cookie value
                cookie.setValue(value);
                cookie.setPath("/");

                // response
                res.addCookie(cookie);
            }
        } catch (RuntimeException ex) {
            log.error("validateCookieExpiration method, {}", ex.getMessage());
            throw new CustomInvalidFormatException("Invalid cookie");
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
        Cookie cookie = createCookieValue(req);

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
        Cookie cookie = createCookieValue(req);

        if (cookie == null) {
            return;
        }

        var qty = this.productSKUService
                .productSkuBySKU(dto.sku())
                .getInventory();

        if (qty <= 0 || dto.qty() > qty) {
            throw new OutOfStockException("Product or selected quantity is out of stock.");
        }

        String[] arr = cookie.getValue().split(this.split);
        String param = arr[0];

        Optional<ShoppingSession> optional = this.shoppingSessionRepo
                .shoppingSessionByCookie(param);

        if (optional.isEmpty()) {
            Date date;

            try {
                long d = Long.parseLong(arr[1]);
                date = new Date(d);
            } catch (RuntimeException ex) {
                log.error("create method , {}", ex.getMessage());
                throw new CustomInvalidFormatException("invalid cookie");
            }

            create_new_shopping_session(param, date, dto);
        } else {
            add_to_existing_shopping_session(optional.get(), dto);
        }
    }

    /**
     * Creates a new shopping session
     */
    public void create_new_shopping_session(String uuid, Date expiration, CartDTO dto) {
        var shoppingSession = new ShoppingSession(
                uuid,
                this.customUtil.toUTC(new Date()),
                this.customUtil.toUTC(expiration),
                new HashSet<>()
        );

        ShoppingSession session = this.shoppingSessionRepo.save(shoppingSession);

        this.cartItemRepo.save(new CartItem(dto.qty(), dto.sku(), session));
    }

    /**
     * Creates or updates a CartItem
     */
    public void add_to_existing_shopping_session(ShoppingSession session, CartDTO dto) {
        CartItem cart = this.cartItemRepo
                .cart_item_by_shopping_session_id_and_sku(session.getShoppingSessionId(), dto.sku())
                .orElse(null);

        if (cart == null) {
            // create new cart
            this.cartItemRepo.save(new CartItem(dto.qty(), dto.sku(), session));
        } else {
            // update quantity if cart is present
            this.cartItemRepo.updateCartQtyByCartId(cart.getCartId(), dto.qty());
        }
    }

    /**
     * Deletes a CartItem from ShoppingSession based on
     * sku and user ip address
     *
     * @param sku unique ProductSku
     * */
    @Transactional
    public void remove_from_cart(HttpServletRequest req, String sku) {
        Cookie cookie = createCookieValue(req);

        if (cookie == null) {
            return;
        }

        String[] arr = cookie.getValue().split(this.split);

        this.cartItemRepo.delete_cartItem_by_cookie_and_sku(arr[0], sku);
    }

    /**
     * Schedule deletion for expired ShoppingSession every 10 mins
     * <a href="https://docs.spring.io/spring-framework/reference/integration/scheduling.html">...</a>
     * */
    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.MINUTES, zone = "UTC")
    public void schedule() {
        delete();
    }

    public void delete() {
        Date date = this.customUtil.toUTC(new Date());
        var list = this.shoppingSessionRepo.allByExpiry(date);

        // delete children
        for (ShoppingSession s : list) {
            this.cartItemRepo.deleteByParentID(s.getShoppingSessionId());
        }

        for (ShoppingSession s : list) {
            this.shoppingSessionRepo.deleteById(s.getShoppingSessionId());
        }
    }

    /**
     * Retrieves cookie value from request
     */
    private Cookie createCookieValue(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        return cookies == null
                ? null
                : Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(CART_COOKIE))
                .findFirst()
                .orElse(null);
    }

}