package com.heronix.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration for SchedulerV2 integration
 * Provides RestTemplate bean and other integration components
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Configuration
public class SchedulerIntegrationConfig {

    /**
     * RestTemplate for making HTTP requests to SchedulerV2
     * Configured with appropriate timeouts
     *
     * Note: Bean name is 'schedulerRestTemplate' to avoid conflict with the main restTemplate bean
     */
    @Bean
    public RestTemplate schedulerRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(300))
                .build();
    }
}
