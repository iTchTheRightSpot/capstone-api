package com.sarabrandserver.product.util;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SizeInventoryDTO {

    @NotNull(message = "Please enter or choose product quantity")
    private Integer qty;

    @NotNull(message = "Please enter or choose product size")
    @NotEmpty(message = "Please enter or choose product size")
    private String size;

}
