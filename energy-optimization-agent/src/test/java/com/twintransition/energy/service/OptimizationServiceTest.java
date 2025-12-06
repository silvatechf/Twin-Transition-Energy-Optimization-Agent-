package com.twintransition.energy.service;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import com.twintransition.energy.dto.OptimizationLimitsDto;
import com.twintransition.energy.dto.OptimizationRecommendation;
import com.twintransition.energy.dto.OptimizationRequest;

import reactor.core.publisher.Mono;


@ExtendWith(MockitoExtension.class)
@DisplayName("Optimization Service Unit Tests (WebClient Mocked)")
public class OptimizationServiceTest {

   
    @Mock
    private WebClient.Builder webClientBuilder;
    @Mock
    private WebClient webClient;
    
    // Mocks para os passos da cadeia de chamadas HTTP
    @Mock private WebClient.RequestBodyUriSpec requestBodyUriSpec; 
    @Mock private WebClient.RequestBodySpec requestBodySpec; 
    @Mock private WebClient.RequestHeadersSpec<?> requestHeadersSpec; 
    @Mock private WebClient.ResponseSpec responseSpec;

    // A classe sob teste (sem @InjectMocks, inicializada manualmente)
    private OptimizationService optimizationService; 

    private OptimizationRequest validRequest;
    
    private final String MOCK_BASE_URL = "http://mock-base";
    private final String MOCK_OPTIMIZE_PATH = "/mock-optimize";

    @BeforeEach
    void setupMocks() {
        
       
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        // Inicialização manual do serviço (CORREÇÃO DE CONSTRUTOR)
        this.optimizationService = new OptimizationService(
            webClientBuilder, 
            MOCK_BASE_URL, 
            MOCK_OPTIMIZE_PATH
        );

        
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(MOCK_OPTIMIZE_PATH);
        doReturn(requestBodySpec).when(requestBodySpec).contentType(MediaType.APPLICATION_JSON);
        doReturn(requestHeadersSpec).when(requestBodySpec).bodyValue(any(OptimizationRequest.class)); 
        
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec); 

        
        OptimizationRecommendation mockRecommendation = new OptimizationRecommendation(
            "HVAC: Reduce temp", "Justified by mock savings.", 50.0, 15.0, "REC-MOCK-456"
        );
        
        when(responseSpec.bodyToMono(OptimizationRecommendation.class)).thenReturn(Mono.just(mockRecommendation));

        // Dados de requisição (CORREÇÃO DE CONSTRUTOR)
        OptimizationLimitsDto limits = new OptimizationLimitsDto(24.0, 20.0);
        validRequest = new OptimizationRequest(
            Arrays.asList(100.0, 110.0, 120.0), 
            Arrays.asList(22.5, 23.0, 24.0), 
            limits, 
            "en" 
        );
    }

    @Test
    @DisplayName("Should generate a valid recommendation using mocked web client")
    void shouldGenerateValidRecommendation() {
        
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec); 
        
        OptimizationRecommendation result = optimizationService.generateRecommendation(validRequest);
        assertNotNull(result);
        assertEquals(50.0, result.estimatedCostSavingsEur(), "Cost savings should match the mocked value.");
        assertEquals("REC-MOCK-456", result.recommendationId());
        verify(webClient, times(1)).post();
        verify(requestBodySpec, times(1)).bodyValue(validRequest); 
    }
    
    @Test
    @DisplayName("Should throw RuntimeException when WebClient returns 5xx error")
    void shouldThrowExceptionOnExternalServerError() {
        
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec); 

       
        when(responseSpec.bodyToMono(OptimizationRecommendation.class)).thenThrow(new RuntimeException("Simulated External Agent Error"));

        assertThrows(RuntimeException.class, () -> {
            optimizationService.generateRecommendation(validRequest);
        });
    }
}
