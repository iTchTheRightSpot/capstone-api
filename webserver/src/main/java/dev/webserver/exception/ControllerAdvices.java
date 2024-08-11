package dev.webserver.exception;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.format.DateTimeParseException;
import java.util.function.BiFunction;
import java.util.regex.PatternSyntaxException;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
@Order(HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
class ControllerAdvices {

    private static final Logger log = LoggerFactory.getLogger(ControllerAdvices.class);

    private static final BiFunction<Class<?>, String, String> formatErrorMessage =
            (clazz, message) -> message.replace(clazz.getName() + ": ", "");

    @ExceptionHandler(value = {DuplicateException.class, ResourceAttachedException.class, OutOfStockException.class})
    public ResponseEntity<ExceptionResponse> duplicateException(final RuntimeException ex) {
        final String message = ex.getCause() != null ? formatErrorMessage.apply(ex.getCause().getClass(), ex.getMessage()) : ex.getMessage();
        final var res = new ExceptionResponse(message, "", CONFLICT);
        return new ResponseEntity<>(res, CONFLICT);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ExceptionResponse> handleMaxFileSizeExceeded() {
        final var res = new ExceptionResponse("One of more files are too large. Each file has to be %s", "", PAYLOAD_TOO_LARGE);
        return new ResponseEntity<>(res, PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler({S3Exception.class, CustomServerError.class})
    public ResponseEntity<ExceptionResponse> awsException(final RuntimeException ex) {
        final String message = ex.getCause() != null ? formatErrorMessage.apply(ex.getCause().getClass(), ex.getMessage()) : ex.getMessage();
        final var res = new ExceptionResponse(message, "", INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(res, INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> methodArgumentException(final MethodArgumentNotValidException ex) {
        final String message = ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        return new ResponseEntity<>(new ExceptionResponse(message, "", BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(CustomBadRequestException.class)
    public ResponseEntity<ExceptionResponse> badRequestException(final CustomBadRequestException ex) {
        final String message = ex.getCause() != null ? formatErrorMessage.apply(ex.getCause().getClass(), ex.getMessage()) : ex.getMessage();
        return new ResponseEntity<>(new ExceptionResponse(message, "", BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ExceptionResponse> missingRequestParameterException(final MissingServletRequestParameterException ex) {
        return new ResponseEntity<>(new ExceptionResponse(ex.getMessage(), "", BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ExceptionResponse> handlerMethodException(final HandlerMethodValidationException ex) {
        final String message = ex.getAllValidationResults().getFirst().getResolvableErrors().getFirst().getDefaultMessage();
        return new ResponseEntity<>(new ExceptionResponse(message, "", BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler({CustomInvalidFormatException.class})
    public ResponseEntity<ExceptionResponse> formatException(final CustomInvalidFormatException ex) {
        final var res = new ExceptionResponse(ex.getMessage(), "", BAD_REQUEST);
        return new ResponseEntity<>(res, BAD_REQUEST);
    }

    @ExceptionHandler({SQLIntegrityConstraintViolationException.class})
    public ResponseEntity<ExceptionResponse> sqlIntegrityException(final SQLIntegrityConstraintViolationException e) {
        log.error("SqlIntegrityException Exception {}", e.getMessage());
        final var res = new ExceptionResponse("sql conflict", "", CONFLICT);
        return new ResponseEntity<>(res, CONFLICT);
    }

    @ExceptionHandler({PatternSyntaxException.class})
    public ResponseEntity<ExceptionResponse> formatException() {
       final var res = new ExceptionResponse("invalid cookie", "", BAD_REQUEST);
        return new ResponseEntity<>(res, BAD_REQUEST);
    }

    @ExceptionHandler({DateTimeParseException.class, NumberFormatException.class})
    public ResponseEntity<ExceptionResponse> timeFormatException(final RuntimeException e) {
        final String message = switch (e) {
            case DateTimeParseException ignored1 -> "invalid datetime format";
            case NumberFormatException ignored -> "invalid number";
            default -> "please verify your request";
        };

        final var res = new ExceptionResponse(message, "", BAD_REQUEST);
        return new ResponseEntity<>(res, BAD_REQUEST);
    }

    @ExceptionHandler(value = {NullPointerException.class})
    public ResponseEntity<ExceptionResponse> nullException(final NullPointerException e) {
        log.error("NullPointerException ExceptionHandler {}", e.getMessage());
        final var res = new ExceptionResponse("an error occurred", "", INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(res, INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionResponse> illegalStateException(final IllegalArgumentException ex) {
        final String message = ex.getCause() != null ? formatErrorMessage.apply(ex.getCause().getClass(), ex.getMessage()) : ex.getMessage();
        return new ResponseEntity<>(new ExceptionResponse(message, "", BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(CustomNotFoundException.class)
    public ResponseEntity<ExceptionResponse> notFoundException(final CustomNotFoundException ex) {
        final String message = ex.getCause() != null ? formatErrorMessage.apply(ex.getCause().getClass(), ex.getMessage()) : ex.getMessage();
        return new ResponseEntity<>(new ExceptionResponse(message, "", NOT_FOUND), NOT_FOUND);
    }

}
