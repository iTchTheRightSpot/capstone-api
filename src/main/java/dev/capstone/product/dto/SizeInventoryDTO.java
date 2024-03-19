<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/product/dto/SizeInventoryDTO.java
package dev.webserver.product.dto;
========
package dev.capstone.product.dto;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/product/dto/SizeInventoryDTO.java

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record SizeInventoryDTO (
        @NotNull(message = "Please enter or choose product quantity")
        Integer qty,
        @NotNull(message = "Please enter or choose product size")
        @NotEmpty(message = "Please enter or choose product size")
        String size
) implements Serializable { }
