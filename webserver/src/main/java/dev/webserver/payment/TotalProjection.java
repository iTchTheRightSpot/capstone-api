package dev.webserver.payment;

import java.math.BigDecimal;

public interface TotalProjection {

    Integer getQty();
    BigDecimal getPrice();
    Double getWeight();

}
