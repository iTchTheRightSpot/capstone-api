package com.example.sarabrandserver.product.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonInclude(NON_EMPTY)
    @Size(min = 5, max = 25, message = "Min of 5 and max of 25")
    private String productName;

    @JsonProperty(required = true, value = "product_visible")
    @JsonInclude(NON_EMPTY)
    private Boolean isVisible;

    @JsonProperty(required = true, value = "product_detail")
    @JsonInclude(NON_EMPTY)
    private ProductDetailDTO detailDTO;

}
