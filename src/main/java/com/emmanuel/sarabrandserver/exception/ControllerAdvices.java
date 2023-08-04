package com.emmanuel.sarabrandserver.exception;

import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
@Order(HIGHEST_PRECEDENCE)
public class ControllerAdvices {
    private final Environment environment;

    public ControllerAdvices(Environment environment) {
        this.environment = environment;
    }

    private record ExceptionDetails(String message, HttpStatus httpStatus, ZonedDateTime timestamp) { }


    @ExceptionHandler(value = {DuplicateException.class})
    public ResponseEntity<?> duplicateException(Exception ex) {
        var exceptionDetails = new ExceptionDetails(
                ex.getMessage(),
                CONFLICT,
                ZonedDateTime.now(ZoneId.of("UTC"))
        );
        return new ResponseEntity<>(exceptionDetails, CONFLICT);
    }

    @ExceptionHandler(value = {CustomNotFoundException.class})
    public ResponseEntity<?> customNotFoundException(Exception ex) {
        var exceptionDetails = new ExceptionDetails(
                ex.getMessage(),
                NOT_FOUND,
                ZonedDateTime.now(ZoneId.of("UTC"))
        );
        return new ResponseEntity<>(exceptionDetails, NOT_FOUND);
    }

    @ExceptionHandler(value = {AuthenticationException.class, UsernameNotFoundException.class})
    public ResponseEntity<?> authenticationException(Exception e) {
        var exceptionDetails = new ExceptionDetails(
                e.getMessage(),
                UNAUTHORIZED,
                ZonedDateTime.now(ZoneId.of("UTC"))
        );
        return new ResponseEntity<>(exceptionDetails, UNAUTHORIZED);
    }

    @ExceptionHandler(value = {AccessDeniedException.class})
    public ResponseEntity<?> accessException(Exception e) {
        var exceptionDetails = new ExceptionDetails(
                e.getMessage(),
                FORBIDDEN,
                ZonedDateTime.now(ZoneId.of("UTC"))
        );
        return new ResponseEntity<>(exceptionDetails, FORBIDDEN);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxFileSizeExceeded(Exception ex) {
        var maxSize = this.environment.getProperty("spring.servlet.multipart.max-file-size", "");
        var exceptionDetails = new ExceptionDetails(
                "File size exceeds the limit of %s bytes".formatted(maxSize),
                PAYLOAD_TOO_LARGE,
                ZonedDateTime.now(ZoneId.of("UTC"))
        );
        return new ResponseEntity<>(exceptionDetails, PAYLOAD_TOO_LARGE);
    }

}
