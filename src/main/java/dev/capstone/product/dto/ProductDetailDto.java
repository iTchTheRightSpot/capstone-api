<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/product/dto/ProductDetailDto.java
package dev.webserver.product.dto;
========
package dev.capstone.product.dto;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/product/dto/ProductDetailDto.java

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record ProductDetailDto(
        @JsonProperty(value = "product_id")
        @NotNull(message = "UUID cannot be empty")
        @NotEmpty(message = "UUID cannot be empty")
        String uuid,

        @NotNull(message = "Please choose if product should be visible")
        Boolean visible,

        @NotNull(message = "Please enter or choose product colour")
        @NotEmpty(message = "Please enter or choose product colour")
        String colour,

        @JsonProperty(value = "sizeInventory")
        @NotNull(message = "Size or Inventory cannot be empty")
        SizeInventoryDTO[] sizeInventory
) implements Serializable { }