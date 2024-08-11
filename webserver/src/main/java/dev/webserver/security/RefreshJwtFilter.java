package dev.webserver.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated;

@RequiredArgsConstructor
final class RefreshJwtFilter extends OncePerRequestFilter {

    private final String jsessionid;
    private final String path;
    private final int maxage;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
        final Cookie[] cookies = request.getCookies();

        // base case
        if (cookies == null || request.getRequestURI().endsWith("logout")) {
            filterChain.doFilter(request, response);
            return;
        }

        // validate refresh token is needed
        Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(jsessionid) && jwtService.refreshTokenNeeded(cookie.getValue()))
                .findFirst()
                .ifPresent(cookie -> {
                    final String userid = jwtService.extractSubject(cookie);
                    final var userDetails = userDetailsService.loadUserByUsername(userid);

                    final UsernamePasswordAuthenticationToken authenticated =
                            authenticated(userDetails, null, userDetails.getAuthorities());

                    final String jwt = jwtService.generateJwt(authenticated);

                    // update cookie
                    cookie.setValue(jwt);
                    cookie.setMaxAge(maxage);
                    cookie.setPath(path);

                    // add cookie to response
                    response.addCookie(cookie);
                });

        filterChain.doFilter(request, response);
    }
}