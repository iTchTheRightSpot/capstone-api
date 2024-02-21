package com.sarabrandserver.product.projection;

import java.math.BigDecimal;

/**
 * Using Spring Data Projection, {@link ProductPojo}
 * maps desired {@link com.sarabrandserver.product.entity.Product}
 * from the database.
 * */
public interface ProductPojo {
    String getUuid();
    String getName();
    String getDescription();
    BigDecimal getPrice();
    String getCurrency();
    String getImage();
    Double getWeight();
    String getWeightType();
    String getCategory();
}
