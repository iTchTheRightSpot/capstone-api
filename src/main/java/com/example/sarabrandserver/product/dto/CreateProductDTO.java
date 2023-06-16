package com.example.sarabrandserver.product.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@Builder
@Data
@Getter @Setter
public class CreateProductDTO {

    @JsonProperty(required = true, value = "category_name")
    @JsonInclude(NON_EMPTY)
    private String categoryName;

    @JsonProperty(required = true, value = "product_name")
    @NotNull @NotEmpty
    @Size(min = 5, max = 80, message = "Min of 5 and max of 80")
    private String productName;

    @JsonProperty(value = "description")
    @Size(max = 255, message = "Max of 255")
    @NotNull @NotEmpty
    private String desc;

    @JsonProperty(value = "price")
    @NotNull
    private Double price;

    @JsonProperty(value = "currency")
    @NotNull @NotEmpty
    private String currency;

    @JsonProperty(required = true, value = "product_detail")
    @NotNull @NotEmpty
    private ProductDetailDTO detailDTO;

}
