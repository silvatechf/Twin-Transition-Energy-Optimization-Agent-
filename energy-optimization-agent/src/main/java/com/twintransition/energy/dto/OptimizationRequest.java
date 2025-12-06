package com.twintransition.energy.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) para a requisição de otimização de energia.
 * Contém os dados de entrada necessários para o Agente Python (ML e Limites).
 */
public record OptimizationRequest(
        
        @NotNull(message = "Historical data cannot be null.")
        @Size(min = 1, message = "Historical consumption must contain at least one data point.")
        List<Double> historicalConsumptionKwH,

        @NotNull(message = "Weather forecast cannot be null.")
        @Size(min = 1, message = "Weather forecast must contain at least one data point.")
        List<Double> weatherForecastDegreesC,

        @NotNull(message = "Optimization limits must be specified.")
        OptimizationLimitsDto limits,
        
        // CORREÇÃO FINAL: Campo de idioma necessário para a lógica Python
        @NotNull(message = "Selected language must be provided.")
        String selectedLanguage
) {}