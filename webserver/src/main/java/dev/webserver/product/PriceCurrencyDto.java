package dev.webserver.product;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

public record PriceCurrencyDto(
        @NotNull(message = "Please enter product price")
        @NotEmpty(message = "cannot be empty")
        BigDecimal price,
        @NotNull(message = "Please enter or choose a product currency")
        @NotEmpty(message = "Please enter or choose a product currency")
        String currency
) implements Serializable { }