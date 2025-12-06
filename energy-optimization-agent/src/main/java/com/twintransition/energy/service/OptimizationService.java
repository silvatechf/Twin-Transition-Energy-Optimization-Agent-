package com.twintransition.energy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.twintransition.energy.dto.OptimizationRecommendation;
import com.twintransition.energy.dto.OptimizationRequest;

import reactor.core.publisher.Mono;

/**
 * Serviço responsável por orquestrar a lógica de otimização de energia.
 * Atua como um cliente HTTP (WebClient) para se comunicar com o Agente Python (FastAPI).
 */
@Service
public class OptimizationService {

    private final WebClient webClient;
    private final String optimizationEndpoint;

    /**
     * Construtor do Serviço, injetando o WebClient e carregando a configuração 
     * dos endpoints do application.yml.
     */
    public OptimizationService(
        WebClient.Builder webClientBuilder, 
        @Value("${optimization.agent.base-url}") String baseUrl,
        @Value("${optimization.agent.optimize-path}") String optimizePath) {
        
        // Inicializa a instância do WebClient e a URL base
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.optimizationEndpoint = optimizePath;
    }

    /**
     * Chama o Agente de Otimização Python externo via requisição HTTP POST.
     * * @param request Os dados de entrada necessários para o Agente (consumo, clima, limites).
     * @return A OptimizationRecommendation gerada pelo Agente.
     */
    public OptimizationRecommendation generateRecommendation(OptimizationRequest request) {
        
        Mono<OptimizationRecommendation> responseMono = webClient.post()
            .uri(optimizationEndpoint)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            // Configura o tratamento de erro: se receber 4xx ou 5xx, mapeia para uma RuntimeException.
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), 
                      response -> response.bodyToMono(String.class)
                                          // Garante que o corpo do erro (se existir) seja incluído na exceção
                                          .map(errorBody -> new RuntimeException("External Agent Error: " + errorBody)))
            .bodyToMono(OptimizationRecommendation.class);
            
        // Bloqueia a chamada reativa para retornar um resultado síncrono (necessário para o Controller)
        try {
            return responseMono.block();
        } catch (WebClientResponseException e) {
            // Captura exceções específicas do WebClient e relança como RuntimeException
            throw new RuntimeException("External Agent HTTP Error: " + e.getRawStatusCode(), e);
        }
    }
}