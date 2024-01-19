package com.sarabrandserver.payment.projection;

import java.math.BigDecimal;

public interface TotalPojo {

    Integer getQty();
    BigDecimal getPrice();
    Double getWeight();

}
