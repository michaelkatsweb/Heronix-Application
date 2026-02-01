package com.heronix.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Test Configuration for Heronix SIS
 *
 * This configuration class provides a unified Spring Boot configuration for tests,
 * resolving the "Found multiple @SpringBootConfiguration annotated classes" error.
 *
 * Tests should use @SpringBootTest(classes = TestConfiguration.class) or
 * extend a base test class that specifies this configuration.
 *
 * For web/API tests that need MockMvc, use:
 * @SpringBootTest(classes = TestConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since January 2026
 */
@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties.class)
@EnableCaching
@EnableScheduling
@EntityScan(basePackages = "com.heronix")
@EnableJpaRepositories(basePackages = "com.heronix")
@ComponentScan(
    basePackages = "com.heronix",
    excludeFilters = {
        // Exclude the JavaFX Application class (has @SpringBootApplication)
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = com.heronix.HeronixSchedulerApplication.class),
        // Exclude JavaFX UI controllers (require JavaFX runtime)
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.heronix\\.ui\\..*")
    }
)
public class TestConfiguration {
    // This class serves as the single @SpringBootConfiguration for tests
    // All Spring Boot tests should reference this class
}
