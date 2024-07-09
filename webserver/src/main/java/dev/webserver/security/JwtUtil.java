package dev.webserver.security;

import com.nimbusds.jose.jwk.RSAKey;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

public record JwtUtil() {

    /**
     * generates a java.security.KeyPair pub & priv key at runtime
     */
    private static KeyPair RSAKEYPAIR() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Generate com.nimbusds.jose.jwk.RSAKey at runtime
     * */
    public static RSAKey GENERATERSAKEY() {
        KeyPair keyPair = RSAKEYPAIR();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
    }

    public static String substringAfter(final String str, final String separator) {
        if (str == null || str.isEmpty()) {
            return str;
        } else if (separator == null) {
            return "";
        }

        int pos = str.indexOf(separator);
        return pos == -1 ? "" : str.substring(pos + separator.length());
    }

}
