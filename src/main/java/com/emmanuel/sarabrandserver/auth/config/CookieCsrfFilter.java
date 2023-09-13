package com.emmanuel.sarabrandserver.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Spring Security 6 doesn't set a XSRF-TOKEN cookie by default.
 * This solution is
 * <a href="https://github.com/spring-projects/spring-security/issues/12141#issuecomment-1321345077">
 * recommended by Spring Security.</a>
 */
public class CookieCsrfFilter extends OncePerRequestFilter {

    @Value(value = "${server.servlet.session.cookie.secure}")
    private boolean COOKIESECURE;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain
    ) throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        response.setHeader(csrfToken.getHeaderName(), csrfToken.getToken());
        filterChain.doFilter(request, response);
    }

}