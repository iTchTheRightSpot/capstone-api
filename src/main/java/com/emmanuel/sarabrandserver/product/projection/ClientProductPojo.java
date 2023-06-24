package com.emmanuel.sarabrandserver.product.projection;

import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.util.List;

// Spring Data Projection
public interface ClientProductPojo {
    @Value(value = "name")
    String getName();
    @Value(value = "desc")
    String getDesc();
    @Value(value = "price")
    BigDecimal getPrice();
    @Value(value = "currency")
    String getCurrency();
    @Value(value = "sku")
    List<String> getSku();
    @Value(value = "size")
    List<String> getSize();
    @Value(value = "qty")
    List<Integer> getQuantity();
    @Value(value = "image_url")
    List<String> getImage();
    @Value(value = "colour")
    List<String> getColour();
}
