package com.example.sarabrandserver.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Builder
@Data
@Getter @Setter
public class CreateProductDTO {

    @JsonProperty(required = true, value = "category_name")
    @NotNull @NotEmpty
    private String categoryName;

    @JsonProperty(required = true, value = "product_name")
    @NotNull @NotEmpty
    @Size(min = 5, max = 25, message = "Min of 5 and max of 25")
    private String productName;

    @JsonProperty(value = "description")
    @Size(max = 255, message = "Max of 255")
    @NotNull
    private String desc;

    @JsonProperty(required = true, value = "qty")
    @NotNull
    private Integer qty;

    @JsonProperty(required = true, value = "size")
    @NotNull
    private String size;

    @JsonProperty(required = true, value = "colour")
    @NotNull
    private String colour;

    @JsonProperty(required = true, value = "price")
    @NotNull
    private BigDecimal price;

    @JsonProperty(required = true, value = "currency")
    @NotNull
    private String currency; // Default will be USD

}
