package com.sarabrandserver.product.projection;

import java.math.BigDecimal;

// Spring data projection
public interface ProductPojo {
    String getUuid();
    String getName();
    String getDescription();
    BigDecimal getPrice();
    String getCurrency();
    String getKey();
    String getCategory();
    String getCollection();
}
