package com.example.api.exception;

import com.example.api.domain.model.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class RestControllerAdviceExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(RestControllerAdviceExceptionHandler.class);

    /**
     * Handles validation errors from MethodArgumentNotValidException and returns an ApiError.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        String errorMessage = "Validation error(s): " +
                fieldErrors.stream()
                        .map(error -> String.format("%s %s", error.getField(), error.getDefaultMessage()))
                        .collect(Collectors.joining(", "));

        log.error(errorMessage, ex);

        return new ApiError(errorMessage);
    }

    /**
     * Handles generic exceptions and returns an ApiError or a custom error for email validation.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex, BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors("email")) {
            String emailErrorMessage = Objects.requireNonNull(bindingResult.getFieldError("email")).getDefaultMessage();
            return ResponseEntity.badRequest().body(new ApiError("Email validation error: " + emailErrorMessage));
        }
        String errorMessage = "An internal server error occurred: " + ex.getLocalizedMessage();
        log.error(errorMessage, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiError(errorMessage));
    }

    /**
     * Handles CustomerNotFound and NoResultException and returns an ApiError with a warning log.
     */
    @ExceptionHandler(value = {CustomerNotFound.class, NoResultException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleCustomerNotFoundException(Exception ex, HttpServletRequest request) {
        String idFromRequest = extractIdFromRequest(request);
        String errorMessage = "Client " + idFromRequest + " not found with parameters sent ";
        log.warn(errorMessage);
        return new ApiError(errorMessage);
    }

    /**
     * Handles BusinessException and returns an ApiError with a warning log.
     */
    @ExceptionHandler(value = BusinessException.class)
    public ResponseEntity<ApiError> handleCustomerNotFoundException(BusinessException ex) {
        String errorMessage = ex.getLocalizedMessage();
        log.warn(errorMessage);
        return new ResponseEntity<>(new ApiError(errorMessage), ex.getHttpStatus());
    }

    /**
     * Extracts the ID from the request URI.
     */
    private String extractIdFromRequest(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String[] pathSegments = requestUri.split("/");
        if (pathSegments.length > 0) {
            return pathSegments[pathSegments.length - 1];
        }
        return "ID not found in the request";
    }
}
