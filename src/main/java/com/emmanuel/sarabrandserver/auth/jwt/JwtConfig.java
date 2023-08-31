package com.emmanuel.sarabrandserver.auth.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.UUID;

/**
 * For Jwt config details
 * <a href="https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html">...</a>
 */
@Configuration
public class JwtConfig {
    private RSAKey rsaKey;

    private record Jwks() {
        public static RSAKey generateRsa() {
            KeyPair keyPair = KeyGeneratorUtils.generateRsaKey();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(UUID.randomUUID().toString())
                    .build();
        }
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        rsaKey = Jwks.generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwks) {
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    public JwtDecoder jwtDecoder() throws JOSEException {
        return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        var authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("role");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    /**
     * The reason for BearerResolver is since by default, Resource Server looks for a bearer token in the
     * Authorization header, and I am sending my jwt token as a cookie instead of Authorization
     * header, I need to inform Resource Server/BearerTokenAuthenticationFilter where to look for my jwt token.
     * Look at the link below for reference.
     * <a href="https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/bearer-tokens.html">...</a>
     */
    @Bean
    public BearerTokenResolver bearerTokenResolver(
            Environment environment,
            JwtDecoder jwtDecoder,
            JwtTokenService jwtTokenService
    ) {
        return new BearerResolver(environment, jwtDecoder, jwtTokenService);
    }

    private record BearerResolver(
            Environment env,
            JwtDecoder decoder,
            JwtTokenService service
    ) implements BearerTokenResolver {
        @Override
        public String resolve(HttpServletRequest request) {
            Cookie[] cookies = request.getCookies();
            if (cookies == null) {
                return null;
            }

            String JSESSIONID = this.env.getProperty("server.servlet.session.cookie.name");
            return Arrays.stream(cookies)
                    .filter(cookie -> cookie.getName().equals(JSESSIONID))
                    .filter(this.service::_isTokenNoneExpired)
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
    }

}
