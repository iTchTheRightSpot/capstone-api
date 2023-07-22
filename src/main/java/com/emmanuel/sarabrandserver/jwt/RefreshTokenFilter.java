package com.emmanuel.sarabrandserver.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Class implements refresh token logic*/
@Component @Slf4j
public class RefreshTokenFilter extends OncePerRequestFilter {
    private final JwtTokenService tokenService;
    private final UserDetailsService userDetailsService;
    private final Environment environment;

    public RefreshTokenFilter(
            JwtTokenService tokenService,
            @Qualifier(value = "clientDetailsService") UserDetailsService userDetailsService,
            Environment environment
    ) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
        this.environment = environment;
    }

    /**
     * The objective of this filter is to replace jwt and cookie's max age if jwt is within expiration bound(look in
     * JwtTokenService to find out the bound).
     * */
    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain
    ) throws ServletException, IOException {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && !request.getRequestURI().equals("/api/v1/auth/logout")) {
            String JSESSIONID = this.environment.getProperty("server.servlet.session.cookie.name");
            String LOGGEDSESSION = this.environment.getProperty("custom.cookie.frontend");

            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(JSESSIONID)) {
                    var obj = this.tokenService._validateTokenIsWithinExpirationBound(cookie);

                    if (obj._refreshTokenNeeded()) {
                        var userDetails = this.userDetailsService.loadUserByUsername(obj.principal());
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
                    }
                }

                if (cookie.getName().equals(LOGGEDSESSION)) {
                    cookie.setMaxAge(this.tokenService.maxAge());
                    response.addCookie(cookie);
                } // End of If
            } // End of for
        }

        filterChain.doFilter(request, response);
    }

}