<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/cart/dto/CartDTO.java
package dev.webserver.cart.dto;
========
package dev.capstone.cart.dto;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/cart/dto/CartDTO.java

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record CartDTO (
        @NotNull(message = "cannot be not null")
        @NotEmpty(message = "cannot be empty")
        String sku,

        @NotNull(message = "cannot be not null")
        Integer qty
) implements Serializable { }