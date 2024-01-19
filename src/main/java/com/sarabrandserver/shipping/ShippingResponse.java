package com.sarabrandserver.shipping;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sarabrandserver.enumeration.ShippingType;

import java.io.Serializable;
import java.math.BigDecimal;

public record ShippingResponse(
        @JsonProperty("shipping_id")
        long uuid,
        @JsonProperty("ngn_price")
        BigDecimal ngn,
        @JsonProperty("usd_price")
        BigDecimal usd,
        ShippingType type
) implements Serializable { }