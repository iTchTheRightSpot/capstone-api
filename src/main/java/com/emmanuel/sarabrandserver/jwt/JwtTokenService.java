package com.emmanuel.sarabrandserver.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service @Getter @Setter @Slf4j
public class JwtTokenService {
    private int tokenExpiry = 30; // minutes.
    private int boundToSendRefreshToken = 15; // minutes

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public JwtTokenService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    /**
     * Generates a jwt token
     * @param authentication of type org.springframework.security.core
     * @return String
     * */
    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();

         String[] role = authentication.getAuthorities() //
                 .stream() //
                 .map(grantedAuthority -> StringUtils
                         .substringAfter(grantedAuthority.getAuthority(), "ROLE_")
                 ) //
                 .toArray(String[]::new);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(tokenExpiry, ChronoUnit.MINUTES))
                .subject(authentication.getName())
                .claim("role", role)
                .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    /**
     * Validates if token is with expiration bound. Needed in RefreshTokenFilter
     * @param cookie of type jakarta.servlet.http.Cookie
     * @return UserJwtStatus
     * */
    public UserJwtStatus _validateTokenIsWithinExpirationBound(@NotNull final Cookie cookie) {
        try {
            Jwt jwt = this.jwtDecoder.decode(cookie.getValue()); // throws an error if jwt is not valid
            Instant expiresAt = jwt.getExpiresAt();
            if (expiresAt != null) {
                Instant now = Instant.now();
                Instant boundFromNow = now.plus(boundToSendRefreshToken, ChronoUnit.MINUTES);
                return new UserJwtStatus(
                        jwt.getSubject(),
                        !expiresAt.isBefore(now) && expiresAt.isBefore(boundFromNow)
                );
            }
        } catch (JwtException e) {
            cookie.setMaxAge(0); // Remove cookie
            log.error("JWT exception in JwtTokenService class. {}", e.getMessage());
        }

        return new UserJwtStatus(cookie.getValue(), false);
    }

    /** Simply validates if token is expired or not */
    public boolean _validateTokenExpiration(final String jwt) {
        try {
            // Based on docs, this will throw an error is token is expired or tampered with.
            this.jwtDecoder.decode(jwt);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }

    // Convert tokenExpiry to seconds
    public int maxAge() {
        return this.getTokenExpiry() * 60;
    }

}
