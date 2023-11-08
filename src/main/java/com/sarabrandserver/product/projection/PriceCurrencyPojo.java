package com.sarabrandserver.product.projection;

import com.sarabrandserver.enumeration.SarreCurrency;

import java.math.BigDecimal;

public interface PriceCurrencyPojo {

    String getName();
    String getDescription();
    SarreCurrency getCurrency();
    BigDecimal getPrice();

}
