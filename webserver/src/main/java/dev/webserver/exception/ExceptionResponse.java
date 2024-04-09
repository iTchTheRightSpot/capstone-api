package dev.webserver.exception;

import org.springframework.http.HttpStatus;

public record ExceptionResponse(String message, HttpStatus status) { }