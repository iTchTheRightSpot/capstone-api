package dev.webserver.product.projection;

import dev.webserver.product.entity.Product;

import java.math.BigDecimal;

/**
 * Using Spring Data Projection, {@link ProductPojo}
 * maps desired {@link Product}
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
