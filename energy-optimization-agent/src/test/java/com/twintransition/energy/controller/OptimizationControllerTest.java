package com.twintransition.energy.controller;

import java.util.Arrays;
import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twintransition.energy.dto.OptimizationLimitsDto;
import com.twintransition.energy.dto.OptimizationRecommendation;
import com.twintransition.energy.dto.OptimizationRequest;
import com.twintransition.energy.service.OptimizationService;

/**
 * Unit tests for the OptimizationController layer, focusing on validation, 
 * routing, i18n, and status codes.
 * Assumes a RestExceptionHandler is present to handle validation errors (@Valid).
 */
@WebMvcTest(OptimizationController.class)
@DisplayName("Optimization Controller Unit Tests")
public class OptimizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Mock the external service dependency
    @MockBean
    private OptimizationService optimizationService;
    
    // Mock the i18n component
    @MockBean
    private MessageSource messageSource;

    private OptimizationRequest createValidRequest() {
        OptimizationLimitsDto limits = new OptimizationLimitsDto(24.0, 20.0);
        // CORRIGIDO: Adicionado o quarto argumento: "en"
        return new OptimizationRequest(Arrays.asList(100.0, 110.0), Arrays.asList(22.5, 23.0), limits, "en"); 
    }
    
    private OptimizationRecommendation createMockRecommendation() {
        return new OptimizationRecommendation(
                "HVAC: Reduce temp by 2C",
                "Justification from Gemini API: Saves 50 EUR.",
                50.0, 
                15.0, 
                "REC-123"
        );
    }

    @Test
    @DisplayName("Should return 200 OK and recommendation for valid request (English)")
    void shouldReturnOkAndRecommendationForValidRequest() throws Exception {
        // ARRANGE
        OptimizationRequest request = createValidRequest();
        OptimizationRecommendation mockRecommendation = createMockRecommendation();
        
        when(optimizationService.generateRecommendation(any(OptimizationRequest.class))).thenReturn(mockRecommendation);
        when(messageSource.getMessage("recommendation.success", null, Locale.ENGLISH)).thenReturn("Optimization recommendation generated successfully.");

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/optimization/recommend")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "en")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Optimization recommendation generated successfully."))
                .andExpect(jsonPath("$.data.recommendationId").value("REC-123"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request for invalid request (Missing data)")
    void shouldReturnBadRequestForInvalidRequest() throws Exception {
        // ARRANGE: Create an invalid request (missing historical consumption)
        OptimizationLimitsDto limits = new OptimizationLimitsDto(24.0, 20.0);
        // CORRIGIDO: Adicionado o idioma, mas o campo de consumo é NULL
        OptimizationRequest invalidRequest = new OptimizationRequest(null, Arrays.asList(22.5, 23.0), limits, "en"); 

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/optimization/recommend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()) 
                // Verifica o JSON retornado pelo RestExceptionHandler (agora que o campo está presente)
                .andExpect(jsonPath("$.errors[0]").value("Historical data cannot be null.")); 
    }

    @Test
    @DisplayName("Should use Portuguese i18n message")
    void shouldUsePortugueseI18nMessage() throws Exception {
        // ARRANGE
        OptimizationRequest request = createValidRequest();
        OptimizationRecommendation mockRecommendation = createMockRecommendation();
        
        when(optimizationService.generateRecommendation(any(OptimizationRequest.class))).thenReturn(mockRecommendation);
        when(messageSource.getMessage("recommendation.success", null, new Locale("pt"))).thenReturn("Recomendação de otimização de energia gerada com sucesso.");

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/optimization/recommend")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "pt")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Recomendação de otimização de energia gerada com sucesso."));
    }
}