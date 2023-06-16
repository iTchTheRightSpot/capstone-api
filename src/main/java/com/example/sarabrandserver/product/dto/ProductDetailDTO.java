package com.example.sarabrandserver.product.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailDTO implements Serializable {

    @JsonProperty(required = true, value = "product_visible")
    @JsonInclude(NON_EMPTY)
    private Boolean isVisible;

    @JsonProperty(required = true, value = "qty")
    @NotNull
    private Integer qty;

    @JsonProperty(required = true, value = "size")
    @NotNull @NotEmpty
    private String size;

    @JsonProperty(required = true, value = "colour")
    @NotNull @NotEmpty
    private String colour;

}
