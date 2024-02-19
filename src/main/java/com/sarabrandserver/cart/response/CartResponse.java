package com.sarabrandserver.cart.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sarabrandserver.enumeration.SarreCurrency;

import java.math.BigDecimal;

public record CartResponse(
        String product_id,
        String url,
        String product_name,
        BigDecimal price,
        SarreCurrency currency,
        String colour,
        String size,
        String sku,
        int qty,
        double weight,
        @JsonProperty("weight_type")
        String weightType
) { }