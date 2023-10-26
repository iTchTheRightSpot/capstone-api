package com.sarabrandserver.enumeration;

import lombok.Getter;

@Getter
public enum SarreCurrency {
    NGN("NGN"), USD("USD");

    private final String currency;

    SarreCurrency(String currency) {
        this.currency = currency;
    }

}
