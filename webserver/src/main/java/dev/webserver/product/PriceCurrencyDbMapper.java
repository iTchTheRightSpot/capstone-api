package dev.webserver.product;

import dev.webserver.enumeration.CapstoneCurrency;

import java.math.BigDecimal;

public record PriceCurrencyDbMapper(
        // product
        String name,
        String description,
        // price currency
        CapstoneCurrency currency,
        BigDecimal price
) {}
