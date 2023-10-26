package com.sarabrandserver.cart.response;

import java.math.BigDecimal;

public record CartResponse(
        String sessionId,
        String url,
        String product_name,
        BigDecimal price,
        String currency,
        String sku,
        int qty
) { }