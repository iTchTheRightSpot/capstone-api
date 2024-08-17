package dev.webserver.exception;

public class CustomServerException extends RuntimeException {
    public CustomServerException(final String message) {
        super(message);
    }
}
