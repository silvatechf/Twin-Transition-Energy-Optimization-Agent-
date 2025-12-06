package com.twintransition.energy.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração Global de CORS (Cross-Origin Resource Sharing).
 * Permite que o frontend Angular (localhost:4200) se comunique 
 * com o backend Spring Boot (localhost:8080).
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Aplica o CORS a todos os endpoints sob /api/
        registry.addMapping("/api/**") 
                // Permite a origem do servidor de desenvolvimento Angular
                .allowedOrigins("http://localhost:4200") 
                // Permite os métodos essenciais (POST para a API do Agente)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // Permite todos os cabeçalhos
                .allowedHeaders("*")
                // Permite cookies e cabeçalhos de autenticação
                .allowCredentials(true);
    }
}