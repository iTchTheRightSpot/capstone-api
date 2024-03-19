package dev.webserver.thirdparty;

import java.io.Serializable;

/**
 * Main constructor for Flutterwave
 * */
public record PaymentCredentialObj(
        String pubKey,
        String secretKey,
        String encryptionKey
) implements Serializable {

    /**
     * Nested constructor for PayStack
     * */
    public PaymentCredentialObj(String pubKey, String secretKey) {
        this(pubKey, secretKey, "");
    }

}