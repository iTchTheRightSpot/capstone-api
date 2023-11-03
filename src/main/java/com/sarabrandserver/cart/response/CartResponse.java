package com.sarabrandserver.cart.response;

import com.sarabrandserver.enumeration.SarreCurrency;

import java.math.BigDecimal;

public record CartResponse(
        String url,
        String product_name,
        BigDecimal price,
        SarreCurrency currency,
        String colour,
        String size,
        String sku,
        int qty
) { }