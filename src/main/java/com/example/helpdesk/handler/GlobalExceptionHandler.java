package com.example.helpdesk.handler;

import com.example.helpdesk.domain.DuplicateTicket;
import com.example.helpdesk.domain.TicketNotFound;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;

import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private String correlationId(HttpServletRequest request) {

        String id = request.getHeader("X-Correlation-Id");

        if (id == null || id.isBlank()) {
            return UUID.randomUUID().toString();
        }

        return id;
    }

    private ResponseEntity<ApiError> error(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request
    ) {

        return ResponseEntity.status(status)
                .body(new ApiError(
                        code,
                        message,
                        correlationId(request)
                ));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> authenticationError(
            AuthenticationException ex,
            HttpServletRequest request
    ) {

        return error(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHENTICATED",
                "bad username or password",
                request
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> accessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {

        return error(
                HttpStatus.FORBIDDEN,
                "FORBIDDEN",
                "not allowed for your role",
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validationError(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        var field = ex.getBindingResult().getFieldError();

        String message = field == null
                ? "validation failed"
                : field.getField() + " " + field.getDefaultMessage();

        return error(
                HttpStatus.BAD_REQUEST,
                "VALIDATION",
                message,
                request
        );
    }

    @ExceptionHandler(TicketNotFound.class)
    public ResponseEntity<ApiError> ticketNotFound(
            TicketNotFound ex,
            HttpServletRequest request
    ) {

        return error(
                HttpStatus.NOT_FOUND,
                "NOT_FOUND",
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(DuplicateTicket.class)
    public ResponseEntity<ApiError> duplicateTicket(
            DuplicateTicket ex,
            HttpServletRequest request
    ) {

        return error(
                HttpStatus.CONFLICT,
                "DUPLICATE",
                "Ticket with this business key already exists",
                request
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> invalidUuid(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {

        return error(
                HttpStatus.BAD_REQUEST,
                "VALIDATION",
                "id must be a UUID",
                request
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> invalidJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {

        return error(
                HttpStatus.BAD_REQUEST,
                "VALIDATION",
                "body must be JSON",
                request
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> unsupportedMediaType(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request
    ) {

        return error(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "UNSUPPORTED",
                "Unsupported content type",
                request
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> resourceNotFound(
            NoResourceFoundException ex,
            HttpServletRequest request
    ) {

        return error(
                HttpStatus.NOT_FOUND,
                "NOT_FOUND",
                "Resource not found",
                request
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> invalidTransition(
            IllegalStateException ex,
            HttpServletRequest request
    ) {

        return error(
                HttpStatus.CONFLICT,
                "INVALID_TRANSITION",
                "Ticket status transition is not allowed",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpectedError(
            Exception ex,
            HttpServletRequest request
    ) {

        String id = correlationId(request);

        log.error("Unexpected error. Correlation ID: {}", id, ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(
                        "INTERNAL",
                        "unexpected error",
                        id
                ));
    }
}