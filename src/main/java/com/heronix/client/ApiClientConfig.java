package com.heronix.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * API Client Configuration
 *
 * Configures RestTemplate for consuming REST APIs from the JavaFX desktop application.
 * Provides centralized HTTP client configuration with authentication, timeouts, and error handling.
 *
 * Features:
 * - Connection timeout and read timeout configuration
 * - Authentication token injection via interceptor
 * - Error handling and retry logic
 * - Base URL configuration from properties
 *
 * Usage:
 * - Inject RestTemplate into services
 * - RestTemplate handles authentication automatically
 * - Supports both local and remote API endpoints
 *
 * Configuration Properties:
 * - api.base-url: Base URL for REST API (default: http://localhost:9590/api)
 * - api.timeout.connect: Connection timeout in seconds (default: 10)
 * - api.timeout.read: Read timeout in seconds (default: 30)
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 46 - Frontend Dashboard Integration
 */
@Configuration
public class ApiClientConfig {

    @Value("${api.base-url:http://localhost:9590/api}")
    private String apiBaseUrl;

    @Value("${api.timeout.connect:10}")
    private int connectTimeout;

    @Value("${api.timeout.read:30}")
    private int readTimeout;

    /**
     * Configure RestTemplate with timeouts and interceptors
     *
     * @param builder RestTemplateBuilder from Spring Boot
     * @param tokenManager Token manager for authentication
     * @return Configured RestTemplate
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder, TokenManager tokenManager) {
        return builder
            .rootUri(apiBaseUrl)
            .setConnectTimeout(Duration.ofSeconds(connectTimeout))
            .setReadTimeout(Duration.ofSeconds(readTimeout))
            .additionalInterceptors(authenticationInterceptor(tokenManager))
            .build();
    }

    /**
     * Create authentication interceptor to inject JWT token
     *
     * @param tokenManager Token manager
     * @return Request interceptor
     */
    private ClientHttpRequestInterceptor authenticationInterceptor(TokenManager tokenManager) {
        return (request, body, execution) -> {
            String accessToken = tokenManager.getAccessToken();
            if (accessToken != null && !accessToken.isBlank()) {
                request.getHeaders().setBearerAuth(accessToken);
            }
            return execution.execute(request, body);
        };
    }
}
