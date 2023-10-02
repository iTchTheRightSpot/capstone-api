package com.sarabrandserver.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class RefreshTokenFilter extends OncePerRequestFilter {

    @Value(value = "${server.servlet.session.cookie.name}")
    private String JSESSIONID;

    @Value(value = "${server.servlet.session.cookie.max-age}")
    private int maxAge;

    private final JwtTokenService tokenService;
    private final UserDetailsService userDetailsService;

    public RefreshTokenFilter(
            JwtTokenService tokenService,
            @Qualifier(value = "userDetailService") UserDetailsService userDetailsService
    ) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * The objective of this filter is to replace JSESSIONID if jwt is
     * within expiration time.
     * Note: For each request, there can only be one valid jwt as
     * logic to validate this is done in AuthService class. Also,
     * pub and priv keys are generated at runtime
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
                .filter(cookie -> cookie.getName().equals(JSESSIONID))
                .filter(this.tokenService::_refreshTokenNeeded)
                .findFirst()
                .ifPresent(cookie -> {
                    var principal = this.tokenService.extractSubject(cookie);
                    var userDetails = this.userDetailsService.loadUserByUsername(principal);
                    var authenticated = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities() // roles
                    );
                    String token = this.tokenService.generateToken(authenticated);
                    cookie.setValue(token);
                    cookie.setMaxAge(maxAge);
                    response.addCookie(cookie);
                });
        filterChain.doFilter(request, response);
    }

}
