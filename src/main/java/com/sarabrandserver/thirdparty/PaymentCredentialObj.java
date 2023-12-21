package com.sarabrandserver.thirdparty;

import java.io.Serializable;

/**
 * Main constructor for PayStack
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

    /**
     * Nested constructor for returning only pub key
     * */
    public PaymentCredentialObj(String pubKey) {
        this(pubKey, "", "");
    }

}