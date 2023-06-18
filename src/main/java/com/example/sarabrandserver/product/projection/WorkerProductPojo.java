package com.example.sarabrandserver.product.projection;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

// Class Acts Spring Data Project For fetching All Products
public interface WorkerProductPojo {
    @JsonProperty(value = "product_name")
    String getName();
    @JsonProperty(value = "description")
    String getDesc();
    @JsonProperty(value = "price")
    BigDecimal getPrice();
    @JsonProperty(value = "currency")
    String getCurrency();
    @JsonProperty(value = "sku")
    String getSku();
    @JsonProperty(value = "status")
    boolean getStatus();
    @JsonProperty(value = "sizes")
    String getSizes();
    @JsonProperty(value = "quantity")
    int getQuantity();
    @JsonProperty(value = "image_url")
    String getImage();
    @JsonProperty(value = "colour")
    String getColour();
}
