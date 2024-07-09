package dev.webserver.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
class RefreshTokenFilter extends OncePerRequestFilter {

    @Value(value = "${server.servlet.session.cookie.name}")
    private String jsessionid;
    @Value(value = "${server.servlet.session.cookie.path}")
    private String path;
    @Value(value = "${server.servlet.session.cookie.max-age}")
    private int maxage;

    private final JwtService tokenService;
    private final UserDetailsService userDetailsService;

    /**
     * The objective of this filter is to replace JSESSIONID if jwt is
     * within expiration time.
     * Note: For each request, there can only be one valid jwt as
     * logic to validate this is done in AuthService class.
     * */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Cookie[] cookies = request.getCookies();

        // Base case
        if (cookies == null || request.getRequestURI().endsWith("logout")) {
            filterChain.doFilter(request, response);
            return;
        }

        // validate refresh token is needed
        Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(jsessionid) && tokenService.refreshTokenNeeded(cookie))
                .findFirst()
                .ifPresent(cookie -> {
                    String principal = tokenService.extractSubject(cookie);
                    var userDetails = userDetailsService.loadUserByUsername(principal);

                    String jwt = tokenService.generateToken(UsernamePasswordAuthenticationToken.authenticated(principal, null, userDetails.getAuthorities()));

                    // update cookie
                    cookie.setValue(jwt);
                    cookie.setMaxAge(maxage);
                    cookie.setHttpOnly(true);
                    cookie.setPath(path);

                    // add cookie to response
                    response.addCookie(cookie);
                });

        filterChain.doFilter(request, response);
    }

}