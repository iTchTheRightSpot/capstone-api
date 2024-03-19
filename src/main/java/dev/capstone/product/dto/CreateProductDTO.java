<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/product/dto/CreateProductDTO.java
package dev.webserver.product.dto;
========
package dev.capstone.product.dto;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/product/dto/CreateProductDTO.java

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public record CreateProductDTO(
        @JsonProperty(value = "category_id")
        @NotNull(message = "Please select categoryId as product has to below to a categoryId")
        Long categoryId,

        @NotNull(message = "Name cannot be null")
        @NotEmpty(message = "Please enter product name")
        @Size(max = 80, message = "Max of 80")
        String name,

        @Size(max = 1000, message = "max of 1000 letters")
        @NotNull(message = "Please enter product description")
        @NotEmpty(message = "Please enter product description")
        String desc,

        @NotNull
        Double weight,

        @NotNull(message = "cannot be empty")
        PriceCurrencyDto[] priceCurrency,

        @NotNull(message = "Please choose if product should be visible")
        Boolean visible,

        @JsonProperty(value = "sizeInventory")
        @NotNull(message = "Size or Inventory cannot be empty")
        SizeInventoryDTO[] sizeInventory,

        @NotNull(message = "Please enter or choose product colour")
        @NotEmpty(message = "Please enter or choose product colour")
        String colour
) implements Serializable { }