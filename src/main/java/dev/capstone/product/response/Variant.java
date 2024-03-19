<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/product/response/Variant.java
package dev.webserver.product.response;
========
package dev.capstone.product.response;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/product/response/Variant.java

import java.io.Serializable;

public record Variant (String sku, String inventory, String size) implements Serializable { }
