<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/product/response/CustomMultiPart.java
package dev.webserver.product.response;
========
package dev.capstone.product.response;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/product/response/CustomMultiPart.java

import java.io.File;
import java.util.Map;

// Needed to upload files to s3
public record CustomMultiPart(File file, Map<String, String> metadata, String key) { }