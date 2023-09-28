package com.emmanuel.sarabrandserver.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
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
    public ExceptionResponse duplicateException(Exception ex) {
        return new ExceptionResponse(ex.getMessage(), CONFLICT);
    }

    @ExceptionHandler(value = {CustomNotFoundException.class})
    public ExceptionResponse customNotFoundException(Exception ex) {
        return new ExceptionResponse(ex.getMessage(), NOT_FOUND);
    }

    @ExceptionHandler(value = {AuthenticationException.class, UsernameNotFoundException.class})
    public ExceptionResponse authenticationException(Exception e) {
        return new ExceptionResponse(e.getMessage(), UNAUTHORIZED);
    }

    @ExceptionHandler(value = {AccessDeniedException.class})
    public ExceptionResponse accessException(Exception e) {
        return new ExceptionResponse(e.getMessage(), FORBIDDEN);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ExceptionResponse handleMaxFileSizeExceeded() {
        var maxSize = this.environment.getProperty("spring.servlet.multipart.max-file-size", "");
        return new ExceptionResponse(
                "One of more files are too large. Each file has to be %s".formatted(maxSize),
                PAYLOAD_TOO_LARGE
        );
    }

    @ExceptionHandler({S3Exception.class, CustomAwsException.class, SseException.class})
    public ExceptionResponse awsException(Exception ex) {
        return  new ExceptionResponse(ex.getMessage(), INTERNAL_SERVER_ERROR);
    }

    /** Displays custom exception */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ExceptionResponse validationException(MethodArgumentNotValidException ex) {
        StringBuilder sb = new StringBuilder();
        String lineSeparator = System.getProperty("line.separator");

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errMessage = error.getDefaultMessage();
            sb.append(fieldName).append(": ").append(errMessage).append(lineSeparator);
        });

        return new ExceptionResponse(sb.toString(), BAD_REQUEST);
    }

    @ExceptionHandler({InvalidFormat.class})
    public ExceptionResponse formatException(Exception ex) {
        return  new ExceptionResponse(ex.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler({SQLIntegrityConstraintViolationException.class})
    public ExceptionResponse sqlDuplicateException() {
        return new ExceptionResponse("Duplicate entry(s)", CONFLICT);
    }

}
