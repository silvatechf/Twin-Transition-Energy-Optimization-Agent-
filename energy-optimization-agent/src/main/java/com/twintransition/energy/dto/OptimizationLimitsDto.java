// src/main/java/com/twintransition/energy/dto/OptimizationLimitsDto.java
package com.twintransition.energy.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record OptimizationLimitsDto(
        @NotNull(message = "Max comfort temperature is required.")
        @DecimalMax(value = "30.0", message = "Max temperature cannot exceed 30.0°C.")
        @DecimalMin(value = "15.0", message = "Max temperature cannot be below 15.0°C.")
        Double maxTemp,
        
        @NotNull(message = "Min comfort temperature is required.")
        @DecimalMax(value = "25.0", message = "Min temperature cannot exceed 25.0°C.")
        @DecimalMin(value = "10.0", message = "Min temperature cannot be below 10.0°C.")
        Double minComfortTemp
) {}