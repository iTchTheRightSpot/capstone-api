package com.sarabrandserver.cart.projection;

import com.sarabrandserver.enumeration.SarreCurrency;

import java.io.Serializable;
import java.math.BigDecimal;

// Spring Data Projection
public interface CartPojo extends Serializable {

    String getUuid(); // product uuid
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