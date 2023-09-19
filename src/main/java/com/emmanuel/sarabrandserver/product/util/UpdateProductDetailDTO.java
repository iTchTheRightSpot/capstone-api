package com.emmanuel.sarabrandserver.product.util;

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
public class UpdateProductDetailDTO {

    @JsonProperty(required = true)
    @NotNull(message = "cannot be empty")
    @NotEmpty(message = "cannot be empty")
    private String sku;

    @JsonProperty(value = "is_visible", required = true)
    @NotNull(message = "cannot be empty")
    private Boolean isVisible;

    @JsonProperty(required = true)
    @NotNull(message = "cannot be empty")
    private Integer qty;

    @NotNull(message = "cannot be empty")
    @NotEmpty(message = "cannot be empty")
    private String size;

}
