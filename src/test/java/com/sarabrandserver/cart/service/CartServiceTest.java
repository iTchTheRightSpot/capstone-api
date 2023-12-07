package com.sarabrandserver.cart.service;

import com.sarabrandserver.AbstractUnitTest;
import com.sarabrandserver.aws.S3Service;
import com.sarabrandserver.cart.repository.CartItemRepo;
import com.sarabrandserver.cart.repository.ShoppingSessionRepo;
import com.sarabrandserver.product.service.ProductSKUService;
import com.sarabrandserver.util.CustomUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.HOURS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CartServiceTest extends AbstractUnitTest {

    private CartService cartService;

    @Value("${cart.cookie.name}")
    private String CART_COOKIE;

    @Mock
    private ShoppingSessionRepo shoppingSessionRepo;
    @Mock
    private CartItemRepo cartItemRepo;
    @Mock
    private ProductSKUService productSKUService;
    @Mock
    private CustomUtil customUtil;
    @Mock
    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        this.cartService = new CartService(
                this.shoppingSessionRepo,
                this.cartItemRepo,
                this.productSKUService,
                this.customUtil,
                this.s3Service
        );
    }

    /**
     * Simulates ShoppingSession expiration is updated as cookie
     * is within expiration.
     * */
    @Test
    void validate_cookie_about_to_expire() {
        // given
        int expirationBound = this.cartService.getExpirationBound();
        String split = this.cartService.getSplit();

        Instant now = Instant.now();
        Instant yesterday = now.minus(expirationBound, HOURS);
        String value = UUID.randomUUID() + split + yesterday.toEpochMilli();
        Cookie cookie = new Cookie(CART_COOKIE, value);
        HttpServletResponse res = mock(HttpServletResponse.class);

        // when
        when(this.customUtil.toUTC(any(Date.class)))
                .thenReturn(new Date(yesterday.toEpochMilli()))
                .thenReturn(new Date(now.toEpochMilli()));

        // then
        this.cartService.validateCookieExpiration(res, cookie);
        verify(this.shoppingSessionRepo, times(1))
                .updateShoppingSessionExpiry(anyString(), any(Date.class));
    }

    /**
     * Simulates shopping session cookie isn't about to expire
     * so no update should be made
     * */
    @Test
    void validate_cookie() {
        // given
        int expirationBound = this.cartService.getExpirationBound();
        String split = this.cartService.getSplit();

        Instant now = Instant.now();
        Instant yesterday = now.plus(expirationBound, HOURS);
        String value = UUID.randomUUID() + split + yesterday.toEpochMilli();
        Cookie cookie = new Cookie(CART_COOKIE, value);
        HttpServletResponse res = mock(HttpServletResponse.class);

        // when
        when(this.customUtil.toUTC(any(Date.class)))
                .thenReturn(new Date(yesterday.toEpochMilli()))
                .thenReturn(new Date(now.toEpochMilli()));

        // then
        this.cartService.validateCookieExpiration(res, cookie);
        verify(this.shoppingSessionRepo, times(0))
                .updateShoppingSessionExpiry(anyString(), any(Date.class));
    }

}