package dev.webserver.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import dev.webserver.AbstractEnvironment;
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

import java.util.Arrays;

import static dev.webserver.security.JwtEnum.CLAIMS;

/**
 * For Jwt config details
 * <a href="https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html">...</a>
 */
@Configuration
class JwtConfiguration extends AbstractEnvironment {

    private static final RSAKey RSA_KEY = JwtUtil.GENERATERSAKEY();

    protected JwtConfiguration(final Environment environment) {
        super(environment);
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        final JWKSource<SecurityContext> source = new ImmutableJWKSet<>(new JWKSet(RSA_KEY));
        return new NimbusJwtEncoder(source);
    }

    @Bean
    public JwtDecoder jwtDecoder() throws JOSEException {
        return NimbusJwtDecoder.withPublicKey(RSA_KEY.toRSAPublicKey()).build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        final var authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName(CLAIMS.property());
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        final var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    /**
     * The reason for BearerResolver is since by default, Resource Server looks for a bearer token in the
     * Authorization header, and I am sending my jwt token as a cookie instead of Authorization
     * header, I need to inform Resource Server/BearerTokenAuthenticationFilter where to look for my jwt token.
     * @see <a href="https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/bearer-tokens.html">documentation</a>
     */
    @Bean
    public BearerTokenResolver bearerTokenResolver(final JwtDecoder decoder, final JwtService service) {
        return new BearerResolver(jsessionid, decoder, service);
    }

    private record BearerResolver(String jsessionid, JwtDecoder decoder, JwtService service) implements BearerTokenResolver {
        @Override
        public String resolve(HttpServletRequest request) {
            final Cookie[] cookies = request.getCookies();
            return cookies == null ? null : Arrays.stream(cookies)
                    .filter(cookie -> cookie.getName().equals(jsessionid) && service.jwtNoneExpired(cookie.getValue()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
    }

}