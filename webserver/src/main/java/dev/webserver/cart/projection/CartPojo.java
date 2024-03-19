package dev.webserver.cart.projection;

import dev.webserver.enumeration.SarreCurrency;

import java.math.BigDecimal;

// Spring Data Projection
public interface CartPojo {

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
    Double getWeight();
    String getWeightType();

}