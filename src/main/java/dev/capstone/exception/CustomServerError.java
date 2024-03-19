package dev.capstone.exception;

public class CustomServerError extends RuntimeException {
    public CustomServerError(String message) {
        super(message);
    }
}
