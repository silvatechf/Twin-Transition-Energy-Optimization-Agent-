package com.twintransition.energy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Global handler for REST exceptions, ensuring validation errors are returned 
 * with a standardized JSON body, which satisfies the MockMvc tests.
 */
@ControllerAdvice
public class RestExceptionHandler {

    /**
     * Handles validation exceptions (@Valid) and returns a structured list of errors.
     * This is necessary to ensure the MockMvc test can assert on the JSON body.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        
        if (!fieldErrors.isEmpty()) {
            response.put("errors", fieldErrors.values().toArray());
        }

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
