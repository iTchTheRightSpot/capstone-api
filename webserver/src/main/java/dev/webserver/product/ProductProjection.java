package dev.webserver.product;

import java.math.BigDecimal;

/**
 * Using Spring Data Projection, {@link ProductProjection}
 * maps desired {@link Product}
 * from the database.
 * */
public interface ProductProjection {
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
