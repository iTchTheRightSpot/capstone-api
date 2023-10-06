package com.sarabrandserver.product.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProductResponse {
    @JsonProperty(value = "product_id")
    private String id;
    private String name;
    private String desc;
    private BigDecimal price;
    private String currency;
    @JsonProperty(value = "image")
    private String imageUrl;
    private String category;
    private String collection;
}
