package dev.webserver.cart;

import dev.webserver.enumeration.SarreCurrency;

import java.math.BigDecimal;

// Spring Data Projection
public record CartDbMapper (
        // product
        String uuid,
        String name,
        String imageKey,
        Double weight,
        String weightType,
        // product sku
        String size,
        String sku,
        // price currency
        BigDecimal price,
        SarreCurrency currency,
        // product detail
        String colour,
        // cart
        Integer qty,
        // shopping session
        String sessionId
) { }