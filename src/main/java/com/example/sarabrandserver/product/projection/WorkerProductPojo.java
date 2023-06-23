package com.example.sarabrandserver.product.projection;

import java.math.BigDecimal;

// Class Acts Spring Data Project For fetching All Products
public interface WorkerProductPojo {
    String getName();
    String getDesc();
    BigDecimal getPrice();
    String getCurrency();
    String getSku();
    boolean getStatus();
    String getSizes();
    int getQuantity();
    String getImage();
    String getColour();
}
