package dev.webserver.cart;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.webserver.enumeration.SarreCurrency;

import java.math.BigDecimal;

public record CartDbMapper (
        // product
        String uuid,
        String name,
        @JsonProperty("image_key")
        String imageKey,
        Double weight,
        @JsonProperty("weight_type")
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
        @JsonProperty("session_id")
        String sessionId
) { }