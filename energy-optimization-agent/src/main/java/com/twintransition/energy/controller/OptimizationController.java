
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
    
    public ResponseEntity<ApiResponse<OptimizationRecommendation>> generateRecommendation(
            @Valid @RequestBody OptimizationRequest request,
            Locale locale) {
        
        OptimizationRecommendation recommendation = optimizationService.generateRecommendation(request);

        String successMessage = messageSource.getMessage(
                "recommendation.success", 
                null, 
                locale
        );
        
        return ResponseEntity.ok(
                new ApiResponse<>(successMessage, recommendation)
        );
    }
}
