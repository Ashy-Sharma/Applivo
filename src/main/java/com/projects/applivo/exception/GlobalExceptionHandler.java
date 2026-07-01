package com.projects.applivo.exception;

import com.projects.applivo.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

import org.slf4j.Logger;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException exception,
                                                                HttpServletRequest request){

        return buildErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request);

    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException exception,
                                                                HttpServletRequest request){

        return buildErrorResponse(HttpStatus.CONFLICT, exception.getMessage(), request);

    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException exception,
                                                                HttpServletRequest request){

        return buildErrorResponse(HttpStatus.UNAUTHORIZED, exception.getMessage(), request);

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException exception,
                                                                HttpServletRequest request){

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + " : " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request);


    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationErrors(BadCredentialsException exception,
                                                            HttpServletRequest request){

        return buildErrorResponse(HttpStatus.UNAUTHORIZED, exception.getMessage(), request);

    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception exception,
                                                       HttpServletRequest request){

        log.error("Unhandled exception", exception);

        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", request);

    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request ){

        ErrorResponse error = ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(status).body(error);
    }



}
