package dev.webserver.security;

import dev.webserver.AbstractEnvironment;
import jakarta.servlet.http.Cookie;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

import static dev.webserver.security.JwtEnum.*;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.SECONDS;

@Service
class JwtService extends AbstractEnvironment {

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;

    protected JwtService(final Environment environment, final JwtEncoder encoder, final JwtDecoder decoder) {
        super(environment);
        this.encoder = encoder;
        this.decoder = decoder;
    }

    public String generateJwt(@NotNull final Authentication authentication) {
        final UserDetailz details = (UserDetailz) authentication.getPrincipal();
        final String[] roles = details.getAuthorities().stream().map(authority -> JwtUtil.substringAfter(authority.getAuthority(), "ROLE_")).toArray(String[]::new);
        final Instant now = Instant.now();
        final JwtClaimsSet set = JwtClaimsSet.builder()
                .issuer(application)
                .issuedAt(now)
                .expiresAt(now.plus(maxage, SECONDS))
                .subject(String.valueOf(details.user().userId()))
                .claims((map) -> map.putAll(Map.of(CLAIMS.property(), roles, FIRSTNAME.property(), details.user().firstname(), USER_ID.property(), details.user().userId())))
                .build();

        return encoder.encode(JwtEncoderParameters.from(set)).getTokenValue();
    }

    public boolean jwtNoneExpired(final String jwt) {
        try {
            decoder.decode(jwt);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }

    public boolean refreshTokenNeeded(final String jwt) {
        try {
            final Jwt decoded = decoder.decode(jwt);
            final Instant expiresAt = decoded.getExpiresAt();
            final Instant now = Instant.now();
            assert expiresAt != null;
            return expiresAt.isAfter(now) && expiresAt.isBefore(now.plus(5, HOURS));
        } catch (JwtException | NullPointerException e) {
            return false;
        }
    }

    public String extractSubject(final Cookie cookie) {
        return decoder.decode(cookie.getValue()).getSubject();
    }

}