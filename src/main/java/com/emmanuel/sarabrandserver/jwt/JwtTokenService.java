package com.emmanuel.sarabrandserver.jwt;

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
    private int tokenExpiry = 30; // Set to max state of lambda function in minutes
    private int boundToSendRefreshToken = 15; // minutes

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public JwtTokenService(
            JwtEncoder jwtEncoder,
            JwtDecoder jwtDecoder
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    /**
     * Generates a jwt token
     * @param authentication of type UsernamePasswordAuthenticationToken
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
     * Validates if token is valid, and it is within bound to refresh jwt token.
     * @param token of jwt
     * @return JwtUserStatus custom record class
     * */
    public JwtUserStatus _validateTokenExpiryDate(@NotNull final String token) {
        try {
            Jwt jwt = this.jwtDecoder.decode(token);
            Instant expiresAt = jwt.getExpiresAt();
            if (expiresAt != null) {
                Instant now = Instant.now();
                Instant boundFromNow = now.plus(boundToSendRefreshToken, ChronoUnit.MINUTES);
                return new JwtUserStatus(
                        jwt.getSubject(),
                        !expiresAt.isBefore(now) && expiresAt.isBefore(boundFromNow)
                );
            }
        } catch (JwtException e) {
            log.error("JWT exception. Token is expired: {}", e.getMessage());
        }

        return new JwtUserStatus(token, false);
    }

    // Convert tokenExpiry to seconds
    public int maxAge() {
        return this.getTokenExpiry() * 60;
    }

}
