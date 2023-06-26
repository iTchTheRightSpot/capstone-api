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
    String getSku();
    @Value(value = "size")
    String getSize();
    @Value(value = "qty")
    String getQuantity();
    @Value(value = "colour")
    String getColour();
    @Value(value = "url")
    List<String> getImage();
}
