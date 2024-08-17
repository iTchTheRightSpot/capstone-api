package dev.webserver.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;

public record CreateProductDto(
        @JsonProperty(value = "category_id")
        @NotNull(message = "Please select categoryId as product has to below to a categoryId")
        Long categoryId,

        @NotNull(message = "product name cannot be null")
        @NotEmpty(message = "product name cannot be empty")
        @Size(max = 50, message = "Max of 50")
        String name,

        @Size(max = 2000, message = "max of 2000 letters")
        @NotNull(message = "Please enter product description")
        @NotEmpty(message = "Please enter product description")
        String desc,

        @NotNull
        BigDecimal weight,

        @NotNull(message = "cannot be empty")
        PriceCurrencyDto[] priceCurrency,

        @NotNull(message = "Please choose if product should be visible")
        Boolean visible,

        @JsonProperty(value = "sizeInventory")
        @NotNull(message = "Size or Inventory cannot be empty")
        SizeInventoryDto[] sizeInventory,

        @NotNull(message = "Please enter or choose product colour")
        @NotEmpty(message = "Please enter or choose product colour")
        @Size(max = 50, message = "colour has to have a max of 50 letters")
        String colour
) implements Serializable { }