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

/** Objective of this class is to replace jwt cookie if it is not expired, and it is within time bound */
@Component
public class CustomFilter extends OncePerRequestFilter {
    @Value(value = "${server.servlet.session.cookie.name}")
    private String JSESSIONID;

    @Value(value = "${custom.cookie.frontend}")
    private String LOGGEDSESSION;

    private final JwtTokenService tokenService;
    private final UserDetailsService userDetailsService;

    public CustomFilter(
            JwtTokenService tokenService,
            @Qualifier(value = "clientDetailService") UserDetailsService userDetailsService
    ) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
    }

    /** Updates jwt cookie value and max age */
    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain
    ) throws ServletException, IOException {
        Cookie[] cookies = request.getCookies();

        if (cookies != null && !request.getRequestURI().equals("/api/v1/auth/logout")) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(JSESSIONID)) {
                    var obj = this.tokenService._validateTokenExpiryDate(cookie.getValue());

                    if (obj.isTokenValid()) {
                        var userDetails = this.userDetailsService.loadUserByUsername(obj.principal());
                        var authentication = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        String token = this.tokenService.generateToken(authentication);
                        cookie.setValue(token);
                        cookie.setMaxAge(this.tokenService.maxAge());
                        response.addCookie(cookie);
                    }
                }

                if (cookie.getName().equals(LOGGEDSESSION)) {
                    cookie.setMaxAge(this.tokenService.maxAge());
                    response.addCookie(cookie);
                }
            }
        }

        filterChain.doFilter(request, response);
    }


}
