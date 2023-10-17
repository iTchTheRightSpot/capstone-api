package com.sarabrandserver.exception;

public class CustomStripeException extends RuntimeException {
    public CustomStripeException(String message) {
        super(message);
    }
}
