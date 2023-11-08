package com.sarabrandserver.product.projection;

import com.sarabrandserver.enumeration.SarreCurrency;

import java.io.Serializable;
import java.math.BigDecimal;

public interface PriceCurrencyPojo extends Serializable {

    String getName();
    String getDescription();
    SarreCurrency getCurrency();
    BigDecimal getPrice();

}
