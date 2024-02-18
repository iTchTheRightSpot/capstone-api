package com.sarabrandserver.shipping;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;

public record ShippingDto (
        @NotNull
        @NotEmpty
        @Size(max = 57, message = "Max of 57")
        String country,
        @NotNull
        @JsonProperty("ngn_price")
        BigDecimal ngn,
        @NotNull
        @JsonProperty("usd_price")
        BigDecimal usd
) implements Serializable { }