<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/auth/jwt/JwtConfig.java
package dev.webserver.auth.jwt;
========
package dev.capstone.auth.jwt;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/auth/jwt/JwtConfig.java

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

import java.util.Arrays;

/**
 * For Jwt config details
 * <a href="https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html">...</a>
 */
@Configuration
public class JwtConfig {

    private static final RSAKey rsaKey = RSAConfig.GENERATERSAKEY();

    @Value(value = "${server.servlet.session.cookie.name}")
    private String JSESSIONID;
    @Value(value = "${jwt.claim}")
    private String CLAIM;

    @Bean
    public JwtEncoder jwtEncoder() {
        JWKSource<SecurityContext> source = new ImmutableJWKSet<>(new JWKSet(rsaKey));
        return new NimbusJwtEncoder(source);
    }

    @Bean
    public JwtDecoder jwtDecoder() throws JOSEException {
        return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        var authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName(CLAIM);
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
    public BearerTokenResolver bearerTokenResolver(JwtDecoder decoder, JwtTokenService service) {
        return new BearerResolver(JSESSIONID, decoder, service);
    }

    private record BearerResolver(
            String JSESSIONID,
            JwtDecoder decoder,
            JwtTokenService service
    ) implements BearerTokenResolver {
        @Override
        public String resolve(HttpServletRequest request) {
            Cookie[] cookies = request.getCookies();
            // ternary operator
            return cookies == null ? null : Arrays
                    .stream(cookies)
                    .filter(cookie -> cookie.getName().equals(JSESSIONID))
                    .filter(this.service::_isTokenNoneExpired)
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
    }

}