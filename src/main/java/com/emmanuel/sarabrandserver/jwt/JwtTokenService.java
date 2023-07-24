package com.emmanuel.sarabrandserver.jwt;

import com.emmanuel.sarabrandserver.util.CustomUtil;
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

import static java.time.temporal.ChronoUnit.MINUTES;

@Service @Getter @Setter @Slf4j
public class JwtTokenService {
    private int tokenExpiry = 30; // minutes.
    private int boundToSendRefreshToken = 15; // minutes

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final CustomUtil customUtil;

    public JwtTokenService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, CustomUtil customUtil) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.customUtil = customUtil;
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
                .expiresAt(now.plus(tokenExpiry, MINUTES))
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
                Instant bound = now.plus(boundToSendRefreshToken, MINUTES);
                return new UserJwtStatus(
                        jwt.getSubject(),
                        expiresAt.isAfter(now) && expiresAt.isBefore(bound)
                );
            }
        } catch (JwtException e) {
            this.customUtil.expireCookie(cookie);
            log.error("JWT exception in _validateTokenIsWithinExpirationBound. {}", e.getMessage());
        }
        return new UserJwtStatus(cookie.getValue(), false);
    }

    /** Simply validates if token is expired or not */
    public boolean _isTokenNoneExpired(@NotNull final Cookie cookie) {
        try {
            // Based on docs, this will throw an error is token is expired or tampered with.
            this.jwtDecoder.decode(cookie.getValue());
            return true;
        } catch (JwtException ex) {
            this.customUtil.expireCookie(cookie);
            return false;
        }
    }

    // Convert tokenExpiry to seconds
    public int maxAge() {
        return this.getTokenExpiry() * 60;
    }

}
