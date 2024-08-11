package dev.webserver.exception;

import org.springframework.http.HttpStatus;

public record ExceptionResponse(String message, String redirect_url, HttpStatus status) { }