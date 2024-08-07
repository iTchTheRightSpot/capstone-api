package dev.webserver.cart;

import dev.webserver.AbstractUnitTest;
import dev.webserver.external.aws.IS3Service;
import dev.webserver.product.ProductSkuService;
import dev.webserver.util.CustomUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CartServiceTest extends AbstractUnitTest {

    private String cartcookie;

    private CartService cartService;

    @Mock
    private IShoppingSessionRepository sessionRepository;
    @Mock
    private ICartRepository cartRepository;
    @Mock
    private ProductSkuService productSKUService;
    @Mock
    private IS3Service s3Service;

    @BeforeEach
    void setUp() {
        cartService = new CartService(
                sessionRepository,
                cartRepository,
                productSKUService,
                s3Service
        );

        cartService.setSplit("%");
        cartService.setBound(5);
        cartService.setCartcookie("CARTCOOKIE");
        cartcookie = cartService.getCartcookie();
    }

    @Test
    void shouldUpdateCookieMaxAge() {
        // when
        final long expiration = Duration.ofHours(1).getSeconds();
        final int maxAgeInSeconds = CustomUtil.TO_GREENWICH.apply(null).plusSeconds(expiration).getSecond();

        final String value = "cookie%" + maxAgeInSeconds;
        final Cookie cookie = new Cookie(cartcookie, value);
        cookie.setMaxAge(maxAgeInSeconds);

        final HttpServletResponse res = mock(HttpServletResponse.class);

        // method to test
        cartService.validateCookieExpiration(res, cookie);

        // then
        verify(sessionRepository, times(1))
                .updateShoppingSessionExpiry(anyString(), any(LocalDateTime.class));
    }

    /**
     * Simulates shopping session cookie isn't about to expire
     * so no update should be made
     * */
    @Test
    void shouldNotUpdateCookieMaxAge() {
        // when
        final long expiration = Duration.ofHours(10).getSeconds();
        final int maxAgeInSeconds = CustomUtil.TO_GREENWICH.apply(null).plusSeconds(expiration).getSecond();

        final String value = "cookie%" + maxAgeInSeconds;
        final Cookie cookie = new Cookie(cartcookie, value);
        cookie.setMaxAge(maxAgeInSeconds);

        final HttpServletResponse res = mock(HttpServletResponse.class);

        // method to test
        cartService.validateCookieExpiration(res, cookie);

        // then
        verify(sessionRepository, times(0))
                .updateShoppingSessionExpiry(anyString(), any(LocalDateTime.class));
    }

}