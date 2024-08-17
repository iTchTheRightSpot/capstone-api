package dev.webserver.payment;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RaceConditionCartDbMapper(
        // product sku
        @JsonProperty("sku_id")
        Long skuId,
        String sku,
        Integer inventory,
        String size,
        // cart
        @JsonProperty("cart_id")
        Long cartId,
        Integer qty,
        // shopping session
        @JsonProperty("session_id")
        Long sessionId
) {}
