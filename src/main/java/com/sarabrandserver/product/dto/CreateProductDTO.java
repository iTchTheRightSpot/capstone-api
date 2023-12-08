package com.sarabrandserver.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public record CreateProductDTO(
        @NotNull(message = "Please select category as product has to below to a category")
        @NotEmpty(message = "Please select category as product has to below to a category")
        String category,

        @NotNull(message = "Product collection cannot be null")
        String collection,

        @NotNull(message = "Name cannot be null")
        @NotEmpty(message = "Please enter product name")
        @Size(max = 80, message = "Max of 80")
        String name,

        @Size(max = 1000, message = "max of 1000 letters")
        @NotNull(message = "Please enter product description")
        @NotEmpty(message = "Please enter product description")
        String desc,

        @NotNull(message = "cannot be empty")
        PriceCurrencyDTO[] priceCurrency,

        @NotNull(message = "Please choose if product should be visible")
        Boolean visible,

        @JsonProperty(value = "sizeInventory")
        @NotNull(message = "Size or Inventory cannot be empty")
        SizeInventoryDTO[] sizeInventory,

        @NotNull(message = "Please enter or choose product colour")
        @NotEmpty(message = "Please enter or choose product colour")
        String colour
) implements Serializable { }