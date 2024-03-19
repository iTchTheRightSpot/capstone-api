package dev.webserver.exception;

public class CustomInvalidFormatException extends RuntimeException {
    public CustomInvalidFormatException(String message) {
        super(message);
    }
}
