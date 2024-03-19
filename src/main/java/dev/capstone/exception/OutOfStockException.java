<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/exception/OutOfStockException.java
package dev.webserver.exception;
========
package dev.capstone.exception;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/exception/OutOfStockException.java

public class OutOfStockException extends RuntimeException {

    public OutOfStockException(String message) {
        super(message);
    }

    public OutOfStockException() {
        super("resource out of stock");
    }
}
