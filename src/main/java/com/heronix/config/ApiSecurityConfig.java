package com.heronix.config;

import com.heronix.security.ApiKeyAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * API Security Configuration
 *
 * Configures security for REST API endpoints with API key authentication.
 * This configuration has higher precedence than the default web security configuration.
 *
 * Security Features:
 * - API Key authentication via X-API-Key or Authorization: Bearer headers
 * - Stateless session management (no sessions for API requests)
 * - Rate limiting per API key
 * - Scope-based authorization
 * - IP whitelisting
 * - Security audit logging
 *
 * Authentication Methods:
 * 1. API Key: X-API-Key: hx_live_...
 * 2. Bearer Token: Authorization: Bearer hx_live_...
 * 3. JWT Token: Authorization: Bearer eyJhbGci...
 *
 * Protected Endpoints:
 * - /api/** - All REST API endpoints require authentication
 *
 * Public Endpoints:
 * - /api/auth/** - Authentication endpoints (login, token refresh)
 * - /swagger-ui/** - API documentation
 * - /v3/api-docs/** - OpenAPI specification
 *
 * Session Management:
 * - STATELESS: No sessions created for API requests
 * - Each request must include authentication credentials
 *
 * CSRF Protection:
 * - Disabled for stateless API endpoints
 * - API keys provide request authenticity
 *
 * Integration:
 * - ApiKeyAuthenticationFilter: Validates API keys
 * - RateLimitService: Enforces rate limits
 * - SecurityAuditService: Logs security events
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 42 - API Security & Authentication
 */
@Configuration
@RequiredArgsConstructor
@Order(1) // Process before default security configuration
public class ApiSecurityConfig {

    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
    private final com.heronix.security.JwtAuthenticationFilter jwtAuthenticationFilter;
    private final com.heronix.security.RateLimitFilter rateLimitFilter;

    /**
     * Configure security for API endpoints
     *
     * @param http HttpSecurity configuration
     * @return Configured SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            // Only apply this configuration to API endpoints
            .securityMatcher(new AntPathRequestMatcher("/api/**"))

            // CSRF protection - disabled for stateless API
            .csrf(csrf -> csrf.disable())

            // Session management - stateless for APIs
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - no authentication required
                .requestMatchers("/api/auth/**").permitAll()           // Authentication endpoints
                .requestMatchers("/api/docs/**").permitAll()            // API documentation
                .requestMatchers("/api/health/**").permitAll()          // Health check
                .requestMatchers("/swagger-ui/**").permitAll()          // Swagger UI
                .requestMatchers("/v3/api-docs/**").permitAll()         // OpenAPI spec

                // Inter-service sync endpoints - SECURED with service scope
                // These require API key with 'service:sync' scope for inter-service communication
                .requestMatchers("/api/sync/**").hasAuthority("SCOPE_service:sync")
                .requestMatchers("/api/teacher/all").hasAuthority("SCOPE_service:sync")
                .requestMatchers("/api/teacher/employee/**").hasAuthority("SCOPE_service:sync")
                .requestMatchers("/api/teacher/sync/**").hasAuthority("SCOPE_service:sync")
                .requestMatchers("/api/config/discovery").hasAuthority("SCOPE_service:sync")
                .requestMatchers("/api/config/client").hasAuthority("SCOPE_service:sync")

                // Protected endpoints - require authentication
                .requestMatchers("/api/**").authenticated()

                // Default - deny all other requests
                .anyRequest().authenticated()
            )

            // Add filters in order: Rate Limit → JWT Auth → API Key Auth
            // 1. Rate limit filter checks request limits first
            // 2. JWT filter checks for Bearer tokens starting with "eyJ"
            // 3. API key filter checks for API keys starting with "hx_"
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

            // Security headers for API responses
            .headers(headers -> headers
                // X-Content-Type-Options - Prevent MIME type sniffing
                .contentTypeOptions(contentTypeOptions -> {})

                // X-Frame-Options - Prevent clickjacking
                .frameOptions(frame -> frame.deny())

                // X-XSS-Protection - Enable XSS filter
                .xssProtection(xss -> xss.headerValue(
                    org.springframework.security.web.header.writers.XXssProtectionHeaderWriter
                        .HeaderValue.ENABLED_MODE_BLOCK))

                // Strict-Transport-Security - Enforce HTTPS
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000)) // 1 year

                // Content-Security-Policy - Restrict resource loading
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; frame-ancestors 'none'"))
            );

        return http.build();
    }
}
