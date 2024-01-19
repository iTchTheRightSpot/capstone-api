package com.sarabrandserver.shipping;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

public record ShippingDto(
        @NotNull
        @JsonProperty("ngn_price")
        BigDecimal ngn,
        @NotNull
        @JsonProperty("usd_price")
        BigDecimal usd,
        @NotNull
        @NotEmpty
        String type
) implements Serializable { }