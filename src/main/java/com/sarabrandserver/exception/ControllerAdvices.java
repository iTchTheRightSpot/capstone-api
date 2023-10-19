package com.sarabrandserver.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.sql.SQLIntegrityConstraintViolationException;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
@Order(HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class ControllerAdvices {

    private final Environment environment;

    @ExceptionHandler(value = {DuplicateException.class, ResourceAttachedException.class})
    public ResponseEntity<ExceptionResponse> duplicateException(Exception ex) {
        var res = new ExceptionResponse(ex.getMessage(), CONFLICT);
        return new ResponseEntity<>(res, CONFLICT);
    }

    @ExceptionHandler(value = {CustomNotFoundException.class})
    public ResponseEntity<ExceptionResponse> customNotFoundException(Exception ex) {
        var res = new ExceptionResponse(ex.getMessage(), NOT_FOUND);
        return new ResponseEntity<>(res, NOT_FOUND);
    }

    @ExceptionHandler(value = {AuthenticationException.class, UsernameNotFoundException.class})
    public ResponseEntity<ExceptionResponse> authenticationException(Exception e) {
        var r = new ExceptionResponse(e.getMessage(), UNAUTHORIZED);
        return new ResponseEntity<>(r, UNAUTHORIZED);
    }

    @ExceptionHandler(value = {AccessDeniedException.class})
    public ResponseEntity<ExceptionResponse> accessException(Exception e) {
        var res = new ExceptionResponse(e.getMessage(), FORBIDDEN);
        return new ResponseEntity<>(res, FORBIDDEN);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ExceptionResponse> handleMaxFileSizeExceeded() {
        var maxSize = this.environment
                .getProperty("spring.servlet.multipart.max-file-size", "");

        var res = new ExceptionResponse(
                "One of more files are too large. Each file has to be %s".formatted(maxSize),
                PAYLOAD_TOO_LARGE
        );

        return new ResponseEntity<>(res, PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler({S3Exception.class, CustomAwsException.class, SseException.class})
    public ResponseEntity<ExceptionResponse> awsException(Exception ex) {
        var res = new ExceptionResponse(ex.getMessage(), INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(res, INTERNAL_SERVER_ERROR);
    }

    /** Displays custom exception */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ExceptionResponse> validationException(MethodArgumentNotValidException ex) {
        StringBuilder sb = new StringBuilder();
        String lineSeparator = System.getProperty("line.separator");

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errMessage = error.getDefaultMessage();
            sb.append(fieldName).append(": ").append(errMessage).append(lineSeparator);
        });

        var res = new ExceptionResponse(sb.toString(), BAD_REQUEST);

        return new ResponseEntity<>(res, BAD_REQUEST);
    }

    @ExceptionHandler({CustomInvalidFormatException.class, CustomStripeException.class})
    public ResponseEntity<ExceptionResponse> formatException(Exception ex) {
        var res = new ExceptionResponse(ex.getMessage(), BAD_REQUEST);
        return new ResponseEntity<>(res, BAD_REQUEST);
    }

    @ExceptionHandler({SQLIntegrityConstraintViolationException.class})
    public ResponseEntity<ExceptionResponse> sqlDuplicateException() {
        var res = new ExceptionResponse("Duplicate entry(s)", CONFLICT);
        return new ResponseEntity<>(res, CONFLICT);
    }

}