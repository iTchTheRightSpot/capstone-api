<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/product/projection/PriceCurrencyPojo.java
package dev.webserver.product.projection;

import dev.webserver.enumeration.SarreCurrency;
========
package dev.capstone.product.projection;

import dev.capstone.enumeration.SarreCurrency;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/product/projection/PriceCurrencyPojo.java

import java.math.BigDecimal;

public interface PriceCurrencyPojo {

    String getName();
    String getDescription();
    SarreCurrency getCurrency();
    BigDecimal getPrice();

}
