package com.emmanuel.sarabrandserver.exception;

public class CustomAwsException extends RuntimeException {
    public CustomAwsException(String message) {
        super(message);
    }
}
