package dev.webserver.cart;

import dev.webserver.AbstractUnitTest;
import dev.webserver.cache.CacheImpl;
import dev.webserver.external.aws.IS3Service;
import dev.webserver.product.ProductSkuService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.core.env.Environment;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

final class CartServiceTest extends AbstractUnitTest {

    private CartService cartService;

    @Mock
    private Environment environment;
    @Mock
    private IShoppingSessionRepository sessionRepository;
    @Mock
    private ICartRepository cartRepository;
    @Mock
    private ProductSkuService productSKUService;
    @Mock
    private IS3Service s3Service;
    @Mock
    private CacheImpl<String, List<CartResponse>> listOfCartResponseCache;

    @BeforeEach
    void setUp() {
        cartService = new CartService(
                environment,
                sessionRepository,
                cartRepository,
                productSKUService,
                s3Service,
                listOfCartResponseCache
        );
        super.setUpEnvironmentVariables(cartService);
    }

    @Test
    void shouldUpdateShoppingSessionCookieAsItIsAboutToExpire() {
        // when
        final long maxAgeInSeconds = Instant.now().plusSeconds(Duration.ofMinutes(30).getSeconds()).getEpochSecond();

        final String value = "cookie%" + maxAgeInSeconds;
        final Cookie cookie = new Cookie("CARTCOOKIE", value);
        cookie.setMaxAge((int) maxAgeInSeconds);

        // when
        final HttpServletResponse res = mock(HttpServletResponse.class);
        cartService.setShoppingSessionExpirationBoundInSeconds(1800L); // 30 mins

        // method to test
        cartService.validateCookieExpiration(res, cookie);

        // then
        verify(sessionRepository, times(1)).updateShoppingSessionExpiry(anyString(), any(LocalDateTime.class));
    }

    /**
     * Simulates shopping session cookie isn't about to expire
     * so no update should be made
     * */
    @Test
    void shouldNotUpdateShoppingSessionCookieAsItIsNotWithinExpirationBound() {
        // when
        final long maxAgeInSeconds = Instant.now()
                .plusSeconds(Duration.ofHours(10).getSeconds())
                .getEpochSecond();

        final String value = "cookie%" + maxAgeInSeconds;
        final Cookie cookie = new Cookie("CARTCOOKIE", value);
        cookie.setMaxAge((int) maxAgeInSeconds);

        // when
        final HttpServletResponse res = mock(HttpServletResponse.class);

        // method to test
        cartService.validateCookieExpiration(res, cookie);

        // then
        verify(sessionRepository, times(0)).updateShoppingSessionExpiry(anyString(), any(LocalDateTime.class));
    }

}