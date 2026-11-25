package com.dsantillan.tfm.patients.exception;

import com.dsantillan.tfm.common.exception.CommonExceptionResponse;
import com.dsantillan.tfm.common.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RestControllerAdvice
public class PatientsExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonExceptionResponse> handleValidationException(
            MethodArgumentNotValidException ex) {
        log.warn("Request validation failed", ex);

        String validationErrors = Stream.concat(
                        ex.getBindingResult().getFieldErrors().stream()
                                .map(fieldError ->
                                        fieldError.getField() + ": " +
                                                fieldError.getDefaultMessage()),

                        ex.getBindingResult().getGlobalErrors().stream()
                                .map(ObjectError::getDefaultMessage)
                )
                .distinct()
                .collect(Collectors.joining(", "));

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed: " + validationErrors);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<CommonExceptionResponse> handleNotFound(
            ResourceNotFoundException ex) {
        log.warn(ex.getMessage(), ex);
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CommonExceptionResponse> handleMalformedRequest(
            HttpMessageNotReadableException ex) {
        log.warn("Malformed request body", ex);
        return buildResponse(HttpStatus.BAD_REQUEST, "The request body is invalid");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<CommonExceptionResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex) {
        log.error("Database integrity violation", ex);
        return buildResponse(HttpStatus.CONFLICT, "The user could not be created because it already exists");
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<CommonExceptionResponse> handleDatabaseUnavailable(
            DataAccessResourceFailureException ex) {
        log.error("Database is unavailable", ex);
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "The database is temporarily unavailable");
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<CommonExceptionResponse> handleExternalServiceUnavailable(
            ResourceAccessException ex) {
        log.error("External service is unavailable", ex);
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "A required service is temporarily unavailable");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<CommonExceptionResponse> handleRuntimeException(RuntimeException ex) {
        log.error("Unexpected error while processing the user request", ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred while processing the request");
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<CommonExceptionResponse> handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
        log.error("Authorization failed", ex);
        return buildResponse(
                HttpStatus.FORBIDDEN,
                "You are not authorized to perform this action");
    }

    private ResponseEntity<CommonExceptionResponse> buildResponse(
            HttpStatus status,
            String message) {
        CommonExceptionResponse response = CommonExceptionResponse.builder()
                .message(message)
                .code(status.value())
                .status(status.getReasonPhrase())
                .build();
        return ResponseEntity.status(status).body(response);
    }
}
