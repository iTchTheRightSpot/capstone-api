package dev.webserver.payment;

public record RaceConditionCartDbMapper(
        // productSku
        Long skuId,
        String sku,
        Integer inventory,
        String size,
        // cart
        Long cartId,
        Integer qty,
        // shopping session
        Long sessionId
) {}
