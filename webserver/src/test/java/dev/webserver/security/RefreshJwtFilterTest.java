package dev.webserver.security;

import dev.webserver.AbstractUnitTest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

final class RefreshJwtFilterTest extends AbstractUnitTest {

    private RefreshJwtFilter refreshJwtFilter;

    @Mock
    private JwtService jwtService;
    @Mock
    private UserDetailsService detailsService;

    @BeforeEach
    void setUp() {
        refreshJwtFilter = new RefreshJwtFilter("JSESSIONID", "/", 3600, jwtService, detailsService);
    }

    @Test
    void jwtShouldBeRefreshedIfWithinExpiration() throws Exception {
        // given
        final String oldJwt = "old.jwt.token";
        final String newJwt = "new.jwt.token";

        final Cookie cookie = new Cookie("JSESSIONID", oldJwt);
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final FilterChain filterChain = mock(FilterChain.class);

        // when
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(request.getRequestURI()).thenReturn("/path");

        when(jwtService.refreshTokenNeeded(anyString())).thenReturn(true);
        when(jwtService.extractSubject(any(Cookie.class))).thenReturn("-1");
        when(jwtService.generateJwt(any(Authentication.class))).thenReturn(newJwt);

        // method to test
        refreshJwtFilter.doFilterInternal(request, response, filterChain);

        // then
        assertEquals(newJwt, cookie.getValue());
        assertEquals(3600, cookie.getMaxAge());
        assertEquals("/", cookie.getPath());
        verify(response).addCookie(cookie);
        verify(filterChain).doFilter(request, response);
    }
}