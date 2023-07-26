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
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/** Class implements refresh token logic */
@Component @Slf4j
public class RefreshTokenFilter extends OncePerRequestFilter {
    private final JwtTokenService tokenService;
    private final UserDetailsService userDetailsService;
    private final Environment environment;
    private final JwtDecoder jwtDecoder;

    public RefreshTokenFilter(
            JwtTokenService tokenService,
            @Qualifier(value = "clientDetailsService") UserDetailsService userDetailsService,
            Environment environment,
            JwtDecoder jwtDecoder
    ) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
        this.environment = environment;
        this.jwtDecoder = jwtDecoder;
    }

    /**
     * The objective of this filter is implement refresh token logic.
     * 1. Replace the first jwt token that needs to be refreshed.
     * 2. Update LOGGEDSESSION max age if refresh token is implemented.
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
        String uri = request.getRequestURI();
        boolean isLogout = uri.length() > 6 && uri.endsWith("logout");

        // Base case
        if (cookies == null || isLogout) {
            filterChain.doFilter(request, response);
            return;
        }

        String JSESSIONID = this.environment.getProperty("server.servlet.session.cookie.name");
        AtomicBoolean updateLOGGEDSESSION = new AtomicBoolean(false);

        // validate refresh token is needed
        Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(JSESSIONID))
                .filter(this.tokenService::_refreshTokenNeeded)
                .findFirst()
                .ifPresent(cookie -> {
                    var principal = extractSubject(cookie);
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
                    updateLOGGEDSESSION.set(true);
                    response.addCookie(cookie);
                });

        String LOGGEDSESSION = this.environment.getProperty("custom.cookie.frontend");
        if (updateLOGGEDSESSION.get()) {
            Arrays.stream(cookies)
                    .filter(cookie -> cookie.getName().equals(LOGGEDSESSION))
                    .findFirst()
                    .ifPresent(cookie -> {
                        cookie.setMaxAge(this.tokenService.maxAge());
                        response.addCookie(cookie);
                    });
        }

        filterChain.doFilter(request, response);
    }

    private String extractSubject(final Cookie cookie) {
        return this.jwtDecoder.decode(cookie.getValue()).getSubject();
    }

}
