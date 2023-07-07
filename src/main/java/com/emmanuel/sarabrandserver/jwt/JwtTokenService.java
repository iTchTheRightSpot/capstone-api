package com.emmanuel.sarabrandserver.jwt;

import jakarta.validation.constraints.NotNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service @Setter @Slf4j
public class JwtTokenService {
    private int expiryForToken = 30;
    private int boundToSendRefreshToken = 10;

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
                .expiresAt(now.plus(expiryForToken, ChronoUnit.MINUTES))
                .subject(authentication.getName())
                .claim("role", role)
                .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    /**
     * Validates if token validity is within 10 mains of expiration.
     * @param token of jwt
     * @throws JwtException is token is invalid
     * @return JwtUserStatus custom record class
     * */
    public JwtUserStatus _validateTokenExpiryDate(@NotNull final String token) {
        try {
            Jwt jwt = this.jwtDecoder.decode(token);
            Instant expiresAt = jwt.getExpiresAt();
            if (expiresAt != null) {
                Instant now = Instant.now();
                Instant tenMinutesFromNow = now.plus(boundToSendRefreshToken, ChronoUnit.MINUTES);
                return new JwtUserStatus(
                        jwt.getSubject(),
                        !expiresAt.isBefore(now) && expiresAt.isBefore(tenMinutesFromNow)
                );
            }
        } catch (JwtException e) {
            log.error("JWT exception. Token is expired: {}", e.getMessage());
        }

        return new JwtUserStatus(token, false);
    }


}
