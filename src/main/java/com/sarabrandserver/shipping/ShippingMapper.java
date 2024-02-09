package com.sarabrandserver.shipping;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Acts as an updateDto and Shipping response mapper.
 * */
public record ShippingMapper(
        @NotNull
        @JsonProperty("shipping_id")
        Long id,
        @NotNull
        @NotEmpty
        String country,
        @NotNull
        @JsonProperty("ngn_price")
        BigDecimal ngn,
        @NotNull
        @JsonProperty("usd_price")
        BigDecimal usd
) implements Serializable { }