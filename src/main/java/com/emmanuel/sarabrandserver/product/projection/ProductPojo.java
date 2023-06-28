package com.emmanuel.sarabrandserver.product.projection;

import java.math.BigDecimal;

// Spring data projection
public interface ProductPojo {
    Long getId();
    String getName();
    String getDesc();
    BigDecimal getPrice();
    String getCurrency();
    String getKey();
}
