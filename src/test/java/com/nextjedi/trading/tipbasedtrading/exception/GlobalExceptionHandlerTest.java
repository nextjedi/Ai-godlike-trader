package com.nextjedi.trading.tipbasedtrading.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GlobalExceptionHandler
 */
@DisplayName("Global Exception Handler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should handle ResourceNotFoundException")
    void shouldHandleResourceNotFoundException() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("Portfolio", "id", 123L);

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleResourceNotFound(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).contains("Portfolio not found");
        assertThat(response.getBody().getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
    }

    @Test
    @DisplayName("Should handle InsufficientFundsException")
    void shouldHandleInsufficientFundsException() {
        // Given
        InsufficientFundsException exception = new InsufficientFundsException(
                "strategyId123", 10000.0, 5000.0);

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                exceptionHandler.handleInsufficientFunds(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).contains("Insufficient funds");
        assertThat(response.getBody().getErrorCode()).isEqualTo("INSUFFICIENT_FUNDS");
    }

    @Test
    @DisplayName("Should handle BrokerException")
    void shouldHandleBrokerException() {
        // Given
        BrokerException exception = new BrokerException("Order placement failed", 
                new RuntimeException("Connection timeout"));

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                exceptionHandler.handleBrokerException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(503);
        assertThat(response.getBody().getMessage()).contains("Order placement failed");
        assertThat(response.getBody().getErrorCode()).isEqualTo("BROKER_ERROR");
    }

    @Test
    @DisplayName("Should handle ValidationException")
    void shouldHandleValidationException() {
        // Given
        ValidationException exception = new ValidationException("Price must be positive");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                exceptionHandler.handleValidation(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).contains("Price must be positive");
        assertThat(response.getBody().getErrorCode()).isEqualTo("VALIDATION_ERROR");
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException")
    void shouldHandleMethodArgumentNotValidException() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("portfolio", "totalCapital", "must be positive");
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(fieldError));
        
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                exceptionHandler.handleValidationErrors(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).contains("Validation failed");
        assertThat(response.getBody().getErrorCode()).isEqualTo("VALIDATION_ERROR");
    }

    @Test
    @DisplayName("Should handle BadCredentialsException")
    void shouldHandleBadCredentialsException() {
        // Given
        BadCredentialsException exception = new BadCredentialsException("Invalid username or password");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                exceptionHandler.handleBadCredentials(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getMessage()).contains("Invalid username or password");
        assertThat(response.getBody().getErrorCode()).isEqualTo("AUTHENTICATION_FAILED");
    }

    @Test
    @DisplayName("Should handle AccessDeniedException")
    void shouldHandleAccessDeniedException() {
        // Given
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                exceptionHandler.handleAccessDenied(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(403);
        assertThat(response.getBody().getMessage()).contains("Access denied");
        assertThat(response.getBody().getErrorCode()).isEqualTo("ACCESS_DENIED");
    }

    @Test
    @DisplayName("Should handle generic Exception")
    void shouldHandleGenericException() {
        // Given
        Exception exception = new RuntimeException("Unexpected error occurred");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                exceptionHandler.handleGenericException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).contains("An unexpected error occurred");
        assertThat(response.getBody().getErrorCode()).isEqualTo("INTERNAL_ERROR");
    }

    @Test
    @DisplayName("Should include metadata in exception response")
    void shouldIncludeMetadataInExceptionResponse() {
        // Given
        TradingException exception = new TradingException("Test error", "TEST_ERROR");
        exception.addMetadata("key1", "value1");
        exception.addMetadata("key2", 123);

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                exceptionHandler.handleGenericException(exception);

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMetadata()).isNotNull();
        assertThat(response.getBody().getMetadata()).hasSize(2);
        assertThat(response.getBody().getMetadata()).containsEntry("key1", "value1");
        assertThat(response.getBody().getMetadata()).containsEntry("key2", 123);
    }
}
