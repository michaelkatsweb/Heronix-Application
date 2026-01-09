package com.heronix.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * API Version Interceptor
 *
 * Intercepts API requests to:
 * - Extract version information from URI
 * - Add version headers to responses
 * - Log version usage for analytics
 * - Warn clients about deprecated versions
 *
 * Response Headers Added:
 * - X-API-Version: The API version being used (e.g., "v1")
 * - X-API-Deprecation: Warning if version is deprecated
 * - X-API-Sunset: Date when deprecated version will be removed
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 41 - API Documentation & Testing
 */
@Component
public class ApiVersionInterceptor implements HandlerInterceptor {

    private static final Pattern VERSION_PATTERN = Pattern.compile("/api/(v\\d+)/");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestURI = request.getRequestURI();

        // Extract version from URI
        Matcher matcher = VERSION_PATTERN.matcher(requestURI);
        if (matcher.find()) {
            String version = matcher.group(1);

            // Add version to response headers
            response.setHeader("X-API-Version", version);

            // Check if version is deprecated
            for (String deprecatedVersion : ApiVersioningConfig.DEPRECATED_VERSIONS) {
                if (version.equals(deprecatedVersion)) {
                    response.setHeader("X-API-Deprecation",
                        "This API version is deprecated. Please migrate to " +
                        ApiVersioningConfig.CURRENT_VERSION);
                    response.setHeader("X-API-Sunset", "2026-06-30");
                    break;
                }
            }
        }

        return true;
    }
}
