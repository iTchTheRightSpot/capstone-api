package com.sarabrandserver.product.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

public record PriceCurrencyDTO (
        @NotNull(message = "Please enter product price")
        BigDecimal price,
        @NotNull(message = "Please enter or choose a product currency")
        @NotEmpty(message = "Please enter or choose a product currency")
        String currency
) implements Serializable { }