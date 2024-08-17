package dev.webserver.product;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public record SizeInventoryDto(
        @NotNull(message = "Please enter or choose product quantity")
        Integer qty,
        @NotNull(message = "Please enter or choose product size")
        @NotEmpty(message = "Please enter or choose product size")
        @Size(max = 50, message = "size has to have a max length of 50 letters")
        String size
) implements Serializable { }
