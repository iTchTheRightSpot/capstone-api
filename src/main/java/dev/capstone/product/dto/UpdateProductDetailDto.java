<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/product/dto/UpdateProductDetailDto.java
package dev.webserver.product.dto;
========
package dev.capstone.product.dto;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/product/dto/UpdateProductDetailDto.java

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