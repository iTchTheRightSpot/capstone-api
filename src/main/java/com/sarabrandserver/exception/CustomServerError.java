package com.sarabrandserver.exception;

public class CustomServerError extends RuntimeException {
    public CustomServerError(String message) {
        super(message);
    }
}
