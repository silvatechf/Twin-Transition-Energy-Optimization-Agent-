// src/main/java/com/twintransition/energy/controller/OptimizationController.java
package com.twintransition.energy.controller;

import com.twintransition.energy.dto.ApiResponse; // <-- CORREÇÃO 1: Adicionar a importação
import com.twintransition.energy.dto.OptimizationRequest;
import com.twintransition.energy.dto.OptimizationRecommendation;
import com.twintransition.energy.service.OptimizationService;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/optimization")
public class OptimizationController {

    private final OptimizationService optimizationService;
    private final MessageSource messageSource;

    public OptimizationController(OptimizationService optimizationService, MessageSource messageSource) {
        this.optimizationService = optimizationService;
        this.messageSource = messageSource;
    }

    /**
     * Endpoint to generate an energy optimization recommendation.
     * Uses the 'Accept-Language' header for i18n messaging.
     * * @param request The data required for optimization (historical consumption, forecast, limits).
     * @param locale The locale derived from the 'Accept-Language' header.
     * @return A response containing the recommendation and success message.
     */
    @PostMapping("/recommend")
    // MELHORIA: Usar ResponseEntity<ApiResponse<OptimizationRecommendation>>
    // para tipar a resposta e melhorar a clareza do endpoint.
    public ResponseEntity<ApiResponse<OptimizationRecommendation>> generateRecommendation(
            @Valid @RequestBody OptimizationRequest request,
            Locale locale) {
        
        // 1. Generate the recommendation using the Core Agent Logic (simulated call)
        // No futuro, este método incluirá a chamada HTTP para o serviço Python.
        OptimizationRecommendation recommendation = optimizationService.generateRecommendation(request);

        // 2. Prepare success message using i18n (English, Spanish, Portuguese)
        String successMessage = messageSource.getMessage(
                "recommendation.success", 
                null, 
                locale
        );
        
        // 3. Return the standardized response (HTTP 200 OK by default from ApiResponse constructor)
        return ResponseEntity.ok(
                new ApiResponse<>(successMessage, recommendation)
        );
    }
}