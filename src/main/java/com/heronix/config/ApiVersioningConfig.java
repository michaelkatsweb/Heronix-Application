package com.heronix.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * API Versioning Configuration
 *
 * Implements URI-based API versioning strategy for the Heronix Scheduling System REST APIs.
 *
 * Versioning Strategy:
 * - URI Path Versioning: /api/v1/, /api/v2/, etc.
 * - Version is explicitly defined in the URL path
 * - Easy to understand and widely adopted
 * - Supports multiple versions simultaneously
 *
 * Version History:
 * - v1: Initial API release (current)
 *   - Attendance Analytics
 *   - Behavior Reporting
 *   - Assignment Reports
 *   - Conflict Analysis
 *   - Webhook Management
 *   - External Integrations
 *   - API Key Management
 *
 * Future Versioning:
 * - When breaking changes are needed, create v2 endpoints
 * - Maintain v1 for backward compatibility
 * - Deprecate old versions with 6-month notice
 * - Document version differences in OpenAPI spec
 *
 * URL Examples:
 * - /api/v1/attendance-analytics/dashboard
 * - /api/v1/webhooks
 * - /api/v2/attendance-analytics/dashboard (future)
 *
 * Benefits:
 * - Clear version identification in URLs
 * - Browser-friendly (can test in address bar)
 * - Cache-friendly (different URLs for different versions)
 * - Easy to route and monitor
 *
 * Migration Strategy:
 * 1. Announce new version 3 months in advance
 * 2. Run both versions in parallel for 6 months
 * 3. Mark old version as deprecated
 * 4. Remove old version after migration period
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 41 - API Documentation & Testing
 */
@Configuration
@RequiredArgsConstructor
public class ApiVersioningConfig implements WebMvcConfigurer {

    private final ApiVersionInterceptor apiVersionInterceptor;

    /**
     * Configure path matching to support trailing slashes
     * and case-insensitive matching for better API usability
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // Allow trailing slashes: /api/v1/students and /api/v1/students/ are equivalent
        configurer.setUseTrailingSlashMatch(true);
    }

    /**
     * Register version interceptor to add version headers to responses
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiVersionInterceptor)
            .addPathPatterns("/api/**");
    }

    /**
     * Current API Version
     * Used for documentation and client libraries
     */
    public static final String CURRENT_VERSION = "v1";

    /**
     * API Version Prefix
     * All versioned endpoints should use this prefix
     */
    public static final String VERSION_PREFIX = "/api/" + CURRENT_VERSION;

    /**
     * Supported API Versions
     * List of all currently supported API versions
     */
    public static final String[] SUPPORTED_VERSIONS = {"v1"};

    /**
     * Deprecated API Versions
     * Versions that are deprecated but still functional
     */
    public static final String[] DEPRECATED_VERSIONS = {};
}
