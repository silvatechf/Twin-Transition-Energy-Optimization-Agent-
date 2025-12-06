// src/main/java/com/twintransition/energy/dto/ApiResponse.java
package com.twintransition.energy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;

/**
 * A generic record used to standardize API responses, including a status message
 * and the main data payload.
 * @param message The localized success or informational message (e.g., from i18n).
 * @param data The main data payload (e.g., OptimizationRecommendation).
 * @param status The HTTP status code (optional, default is OK).
 */
public record ApiResponse<T>(
        String message,
        T data,
        @JsonProperty("status")
        int status
) {
    // Convenience constructor for successful responses (HTTP 200 OK)
    public ApiResponse(String message, T data) {
        this(message, data, HttpStatus.OK.value());
    }
}