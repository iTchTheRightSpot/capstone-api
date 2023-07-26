package com.emmanuel.sarabrandserver.security;

import com.emmanuel.sarabrandserver.jwt.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Objective of this class is to delete all invalid jwt cookies */
@Component
public class MiddlewareFilter extends OncePerRequestFilter {
    private final Environment environment;
    private final JwtTokenService jwtTokenService;

    public MiddlewareFilter(Environment environment, JwtTokenService jwtTokenService) {
        this.environment = environment;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain
    ) throws ServletException, IOException {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String JSESSIONID = this.environment.getProperty("server.servlet.session.cookie.name");
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(JSESSIONID) && !this.jwtTokenService._isTokenNoneExpired(cookie)) {
                cookie.setValue("");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }

        filterChain.doFilter(request, response);
    }

}
