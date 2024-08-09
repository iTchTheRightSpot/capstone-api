package dev.webserver.product;

import dev.webserver.enumeration.SarreCurrency;

import java.math.BigDecimal;

public record PriceCurrencyDbMapper(
        // product
        String name,
        String description,
        // price currency
        SarreCurrency currency,
        BigDecimal price
) {}
