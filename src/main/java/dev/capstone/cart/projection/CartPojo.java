<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/cart/projection/CartPojo.java
package dev.webserver.cart.projection;

import dev.webserver.enumeration.SarreCurrency;
========
package dev.capstone.cart.projection;

import dev.capstone.enumeration.SarreCurrency;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/cart/projection/CartPojo.java

import java.math.BigDecimal;

// Spring Data Projection
public interface CartPojo {

    String getUuid(); // product uuid
    String getSession(); // sessionId
    String getKey(); // s3 key
    String getName(); // product name
    BigDecimal getPrice();
    SarreCurrency getCurrency();
    String getColour();
    String getSize();
    String getSku();
    Integer getQty();
    Double getWeight();
    String getWeightType();

}