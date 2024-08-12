package dev.webserver.enumeration;

import lombok.Getter;

@Getter
public enum CapstoneCurrency {

    NGN("NGN"), USD("USD");

    private final String currency;

    CapstoneCurrency(final String currency) {
        this.currency = currency;
    }

}
