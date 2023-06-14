package com.example.sarabrandserver.product.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailDTO implements Serializable {

    @JsonProperty(value = "description")
    @Size(max = 255, message = "Max of 255")
    @JsonInclude(NON_EMPTY)
    private String desc;

    @JsonProperty(required = true, value = "qty")
    @JsonInclude(NON_EMPTY)
    private Integer qty;

    @JsonProperty(required = true, value = "size")
    @JsonInclude(NON_EMPTY)
    private String size;

    @JsonProperty(required = true, value = "colour")
    @JsonInclude(NON_EMPTY)
    private String colour;

    @JsonProperty(required = true, value = "price")
    @JsonInclude(NON_EMPTY)
    private BigDecimal price;

    @JsonProperty(required = true, value = "currency")
    @JsonInclude(NON_EMPTY)
    private String currency; // Default will be USD

    @JsonProperty(required = true, value = "product_detail_visible")
    @JsonInclude(NON_EMPTY)
    private Boolean isVisible;

}
