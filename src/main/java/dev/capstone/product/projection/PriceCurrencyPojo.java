package dev.capstone.product.projection;

import dev.capstone.enumeration.SarreCurrency;

import java.math.BigDecimal;

public interface PriceCurrencyPojo {

    String getName();
    String getDescription();
    SarreCurrency getCurrency();
    BigDecimal getPrice();

}
