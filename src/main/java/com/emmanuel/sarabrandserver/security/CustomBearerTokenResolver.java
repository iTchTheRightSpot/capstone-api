package com.emmanuel.sarabrandserver.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

import java.util.Arrays;
import java.util.Optional;

/**
 * The reason for a custom BearerTokenResolver is since by default, Resource Server looks for a bearer token in the
 * Authorization header, and I am sending my jwt token as a cookie instead of Authorization
 * header, I need to inform Resource Server/BearerTokenAuthenticationFilter where to look for my jwt token.
 * Look at the link below for reference.
 * <a href="https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/bearer-tokens.html">...</a>
 * */
@Slf4j
public class CustomBearerTokenResolver implements BearerTokenResolver {

    @Value(value = "${server.servlet.session.cookie.name}")
    private String COOKIENAME;
    private final JwtDecoder jwtDecoder;

    public CustomBearerTokenResolver(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public String resolve(final HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            Optional<Cookie> cookie = Arrays.stream(cookies)
                    .filter(name -> name.getName().equals(COOKIENAME))
                    .findFirst();

            if (cookie.isPresent()) {
                try { // Note this is an expensive compute
                    String token = cookie.get().getValue();
                    this.jwtDecoder.decode(token);
                    return token;
                } catch (JwtException e) {
                    log.error("Jwt Exception {}", e.getMessage());
                    return null;
                }
            }
        }

        return null;
    }

}