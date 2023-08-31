package com.emmanuel.sarabrandserver.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

/** Class implements refresh token logic */
@Component
public class RefreshTokenFilter extends OncePerRequestFilter {
    private final JwtTokenService tokenService;
    private final UserDetailsService userDetailsService;
    private final Environment environment;

    public RefreshTokenFilter(
            JwtTokenService tokenService,
            @Qualifier(value = "userDetailService") UserDetailsService userDetailsService,
            Environment environment
    ) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
        this.environment = environment;
    }

    /**
     * The objective of this filter is implement refresh token logic.
     * 1. Replace JSESSIONID cookie value with new token.
     * <br/>
     * Note: For each request, there can only be one valid jwt as logic to validate this is done in AuthService class.
     * */
    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain
    ) throws ServletException, IOException {
        Cookie[] cookies = request.getCookies();

        // Base case
        if (cookies == null || request.getRequestURI().endsWith("logout")) {
            filterChain.doFilter(request, response);
            return;
        }

        String JSESSIONID = this.environment.getProperty("server.servlet.session.cookie.name");

        // validate refresh token is needed
        Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(JSESSIONID))
                .filter(this.tokenService::_refreshTokenNeeded)
                .findFirst()
                .ifPresent(cookie -> {
                    var principal = this.tokenService.extractSubject(cookie);
                    var userDetails = this.userDetailsService.loadUserByUsername(principal);
                    String token = this.tokenService.generateToken(
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities() // roles
                            )
                    );
                    cookie.setValue(token);
                    cookie.setMaxAge(this.tokenService.maxAge());
                    response.addCookie(cookie);
                });

        filterChain.doFilter(request, response);
    }

}
