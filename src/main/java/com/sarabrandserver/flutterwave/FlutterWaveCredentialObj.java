package com.sarabrandserver.flutterwave;

import java.io.Serializable;

public record FlutterWaveCredentialObj(
        String pubKey,
        String secretKey,
        String encryptionKey
) implements Serializable { }