package com.sarabrandserver.product.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ProductDetailDTO {

    @JsonProperty(value = "product_id")
    @NotNull(message = "UUID cannot be empty")
    @NotEmpty(message = "UUID cannot be empty")
    private String uuid;

    @JsonProperty(required = true, value = "visible")
    @NotNull(message = "Please choose if product should be visible")
    private Boolean visible;

    @JsonProperty(required = true, value = "colour")
    @NotNull(message = "Please enter or choose product colour")
    @NotEmpty(message = "Please enter or choose product colour")
    private String colour;

    @JsonProperty(required = true, value = "sizeInventory")
    @NotNull(message = "Size or Inventory cannot be empty")
    private SizeInventoryDTO[] sizeInventory;

}
