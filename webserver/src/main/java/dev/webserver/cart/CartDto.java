package dev.webserver.cart;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record CartDto(
        @NotNull(message = "cannot be not null")
        @NotEmpty(message = "cannot be empty")
        String sku,

        @NotNull(message = "cannot be not null")
        Integer qty
) implements Serializable { }