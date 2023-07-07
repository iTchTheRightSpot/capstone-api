package com.emmanuel.sarabrandserver.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

/** Objective of this class is to replace jwt cookie if it is not expired, and it is within time bound */
@Component
public class CustomFilter extends OncePerRequestFilter {
    @Value(value = "${custom.cookie.name}")
    private String COOKIENAME;

    private final JwtTokenService tokenService;
    private final UserDetailsService userDetailsService;

    public CustomFilter(
            JwtTokenService tokenService,
            @Qualifier(value = "clientDetailService") UserDetailsService userDetailsService
    ) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain
    ) throws ServletException, IOException {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            Optional<Cookie> cookie = Arrays.stream(cookies)
                    .filter(name -> name.getName().equals(COOKIENAME))
                    .findFirst();

            if (cookie.isPresent()) {
                // obj is a custom record JwtUserStatus.
                var obj = this.tokenService._validateTokenExpiryDate(cookie.get().getValue());

                if (obj.isTokenValid()) {
                    var userDetails = this.userDetailsService.loadUserByUsername(obj.principal());

                    var authentication = UsernamePasswordAuthenticationToken.authenticated(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    String token = this.tokenService.generateToken(authentication);
                    cookie.get().setValue(token);
                    response.addCookie(cookie.get());
                }
            }
        }

        filterChain.doFilter(request, response);
    }


}
