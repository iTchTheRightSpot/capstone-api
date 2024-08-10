package dev.webserver.enumeration;

import lombok.Getter;

@Getter
public enum SarreCurrency {

    NGN("NGN"), USD("USD");

    private final String currency;

    SarreCurrency(final String currency) {
        this.currency = currency;
    }

}
