package com.emmanuel.sarabrandserver.product.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProductResponse {
    @JsonProperty(value = "product_id")
    private long id;
    private String name;
    private String desc;
    private double price;
    private String currency;
    @JsonProperty(value = "url")
    private String imageUrl;

    // Constructor needed for responding on the client UI
    public ProductResponse(String name, String desc, double price, String currency, String imageUrl) {
        this.name = name;
        this.desc = desc;
        this.price = price;
        this.currency = currency;
        this.imageUrl = imageUrl;
    }
}
