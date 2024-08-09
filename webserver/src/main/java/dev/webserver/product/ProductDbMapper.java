package dev.webserver.product;

import dev.webserver.enumeration.SarreCurrency;

import java.math.BigDecimal;

public record ProductDbMapper(
        // product
        String uuid,
        String name,
        String imageKey,
        Double weight,
        String weightType,
        String description,
        // price currency
        BigDecimal price,
        SarreCurrency currency,
        // category
        String categoryName
) {}
