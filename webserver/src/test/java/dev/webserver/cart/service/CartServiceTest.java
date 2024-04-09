package dev.webserver.cart.service;

import dev.webserver.AbstractUnitTest;
import dev.webserver.aws.S3Service;
import dev.webserver.cart.repository.CartItemRepo;
import dev.webserver.cart.repository.ShoppingSessionRepo;
import dev.webserver.product.service.ProductSkuService;
import dev.webserver.util.CustomUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static java.time.temporal.ChronoUnit.HOURS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CartServiceTest extends AbstractUnitTest {

    @Value("${cart.cookie.name}")
    private String CARTCOOKIE;

    private CartService cartService;

    @Mock
    private ShoppingSessionRepo shoppingSessionRepo;
    @Mock
    private CartItemRepo cartItemRepo;
    @Mock
    private ProductSkuService productSKUService;
    @Mock
    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        this.cartService = new CartService(
                this.shoppingSessionRepo,
                this.cartItemRepo,
                this.productSKUService,
                this.s3Service
        );

        this.cartService.setSplit("%");
        long bound = 5;
        this.cartService.setBound(bound);
    }

    @Test
    void shouldUpdateCookieMaxAge() {
        // when
        Instant expiration = Instant.now().plus(1, HOURS);
        long maxAgeInSeconds = Instant.now().until(expiration, ChronoUnit.SECONDS);

        String value = "cookie%" + CustomUtil.toUTC(Date.from(expiration))
                .toInstant().getEpochSecond();
        Cookie cookie = new Cookie(CARTCOOKIE, value);
        cookie.setMaxAge((int) maxAgeInSeconds);

        HttpServletResponse res = mock(HttpServletResponse.class);

        // method to test
        cartService.validateCookieExpiration(res, cookie);

        // then
        verify(this.shoppingSessionRepo, times(1))
                .updateShoppingSessionExpiry(anyString(), any(Date.class));
    }

    /**
     * Simulates shopping session cookie isn't about to expire
     * so no update should be made
     * */
    @Test
    void shouldNotUpdateCookieMaxAge() {
        // when
        Instant expiration = Instant.now().plus(10, HOURS);
        long maxAgeInSeconds = Instant.now().until(expiration, ChronoUnit.SECONDS);

        String value = "cookie%" + CustomUtil.toUTC(Date.from(expiration))
                .toInstant().getEpochSecond();
        Cookie cookie = new Cookie(CARTCOOKIE, value);
        cookie.setMaxAge((int) maxAgeInSeconds);

        HttpServletResponse res = mock(HttpServletResponse.class);

        // method to test
        cartService.validateCookieExpiration(res, cookie);

        // then
        verify(shoppingSessionRepo, times(0))
                .updateShoppingSessionExpiry(anyString(), any(Date.class));
    }

}