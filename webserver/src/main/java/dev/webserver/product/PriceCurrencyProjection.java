package dev.webserver.product;

import dev.webserver.enumeration.SarreCurrency;

import java.math.BigDecimal;

public interface PriceCurrencyProjection {

    String getName();
    String getDescription();
    SarreCurrency getCurrency();
    BigDecimal getPrice();

}
