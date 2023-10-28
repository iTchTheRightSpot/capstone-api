package com.sarabrandserver.cart.projection;

import com.sarabrandserver.enumeration.SarreCurrency;

import java.math.BigDecimal;

// Spring Data Projection
public interface CartPojo {

    String getSession(); // sessionId
    String getKey(); // s3 key
    String getName(); // product name
    BigDecimal getPrice();
    SarreCurrency getCurrency();
    String getColour();
    String getSize();
    String getSku();
    Integer getQty();

}