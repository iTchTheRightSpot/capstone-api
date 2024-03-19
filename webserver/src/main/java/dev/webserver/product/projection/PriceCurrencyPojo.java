package dev.webserver.product.projection;

import dev.webserver.enumeration.SarreCurrency;

import java.math.BigDecimal;

public interface PriceCurrencyPojo {

    String getName();
    String getDescription();
    SarreCurrency getCurrency();
    BigDecimal getPrice();

}
