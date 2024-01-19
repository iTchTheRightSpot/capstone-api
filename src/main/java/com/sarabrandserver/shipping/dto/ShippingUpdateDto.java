package com.sarabrandserver.shipping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

public record ShippingUpdateDto(
        @NotNull
        @JsonProperty("shipping_id")
        Long id,
        @NotNull
        @JsonProperty("ngn_price")
        BigDecimal ngn,
        @NotNull
        @JsonProperty("usd_price")
        BigDecimal usd
) implements Serializable { }