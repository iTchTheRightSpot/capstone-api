package com.emmanuel.sarabrandserver.product.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProductResponse {
    @JsonIgnore
    @Value(value = "${s3.pre-assigned.url}")
    private String ASSIGNED_URL;

    @JsonProperty(value = "id")
    private long id;
    private String name;
    private String desc;
    private double price;
    private String currency;
    @JsonProperty(value = "image")
    private String imageUrl;

    // Constructor needed for responding on the admin UI
    public ProductResponse(long id, String name, String desc, double price, String currency, String imageUrl) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.price = price;
        this.currency = currency;
        this.imageUrl = ASSIGNED_URL + imageUrl;
    }

    // Constructor needed for responding on the client UI
    public ProductResponse(String name, String desc, double price, String currency, String imageUrl) {
        this.name = name;
        this.desc = desc;
        this.price = price;
        this.currency = currency;
        this.imageUrl = ASSIGNED_URL + imageUrl;
    }
}
