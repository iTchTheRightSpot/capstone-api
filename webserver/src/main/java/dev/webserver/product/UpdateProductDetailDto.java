package dev.webserver.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record UpdateProductDetailDto(
        @NotNull(message = "cannot be empty")
        @NotEmpty(message = "cannot be empty")
        String sku,
        @NotNull(message = "cannot be empty")
        @NotEmpty(message = "cannot be empty")
        String colour,
        @JsonProperty(value = "is_visible")
        @NotNull(message = "cannot be empty")
        Boolean isVisible,
        @NotNull(message = "cannot be empty")
        Integer qty,
        @NotNull(message = "cannot be empty")
        @NotEmpty(message = "cannot be empty")
        String size
) implements Serializable { }