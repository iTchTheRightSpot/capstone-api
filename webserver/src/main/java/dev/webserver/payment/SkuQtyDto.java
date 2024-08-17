package dev.webserver.payment;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record SkuQtyDto(
        @NotNull
        @NotEmpty
        String sku,
        @NotNull
        Integer qty
) implements Serializable {}