<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/product/dto/PriceCurrencyDto.java
package dev.webserver.product.dto;
========
package dev.capstone.product.dto;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/product/dto/PriceCurrencyDto.java

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

public record PriceCurrencyDto(
        @NotNull(message = "Please enter product price")
        @NotEmpty(message = "cannot be empty")
        BigDecimal price,
        @NotNull(message = "Please enter or choose a product currency")
        @NotEmpty(message = "Please enter or choose a product currency")
        String currency
) implements Serializable { }