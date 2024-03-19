<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/enumeration/SarreCurrency.java
package dev.webserver.enumeration;
========
package dev.capstone.enumeration;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/enumeration/SarreCurrency.java

import lombok.Getter;

@Getter
public enum SarreCurrency {

    NGN("NGN"), USD("USD");

    private final String currency;

    SarreCurrency(String currency) {
        this.currency = currency;
    }

}
