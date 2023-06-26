package com.emmanuel.sarabrandserver.product.projection;

import java.math.BigDecimal;

public interface ProductPojo {
    long getId();
    String getName();
    String getDesc();
    BigDecimal getPrice();
    String getCurrency();
    String getKey();
}
