package com.heronix.security;

import com.heronix.model.domain.ApiKey;
import com.heronix.service.ApiKeyService;
import com.heronix.service.RateLimitService;
import com.heronix.service.SecurityAuditService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Rate Limiting Filter
 *
 * HTTP filter that enforces rate limits on API requests.
 * Uses token bucket algorithm to limit requests per hour.
 *
 * Rate Limiting Strategy:
 * - Per API Key: Each API key has its own rate limit
 * - Per User: Authenticated users have default rate limits
 * - Global: Unauthenticated requests have strict global limits
 *
 * HTTP Response Headers:
 * - X-RateLimit-Limit: Maximum requests allowed per hour
 * - X-RateLimit-Remaining: Remaining requests in current window
 * - X-RateLimit-Reset: Unix timestamp when limit resets
 *
 * HTTP Status Codes:
 * - 200 OK: Request allowed, within rate limit
 * - 429 Too Many Requests: Rate limit exceeded
 *
 * Response on Rate Limit Exceeded:
 * {
 *   "success": false,
 *   "error": "Rate limit exceeded",
 *   "limit": 1000,
 *   "remaining": 0,
 *   "resetAt": 1735579200
 * }
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 44 - API Key Management Endpoints & Rate Limiting
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final ApiKeyService apiKeyService;
    private final SecurityAuditService auditService;

    private static final int DEFAULT_RATE_LIMIT = 1000; // Default: 1000 req/hour
    private static final int UNAUTHENTICATED_RATE_LIMIT = 100; // Unauthenticated: 100 req/hour

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Skip rate limiting for public endpoints
        if (isPublicEndpoint(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Determine rate limit identifier and limit
            RateLimitInfo rateLimitInfo = getRateLimitInfo(request);

            // Check rate limit
            RateLimitService.RateLimitResult result = rateLimitService.checkRateLimit(
                rateLimitInfo.identifier(),
                rateLimitInfo.limit()
            );

            // Add rate limit headers to response
            addRateLimitHeaders(response, result);

            if (result.allowed()) {
                // Request allowed - proceed
                filterChain.doFilter(request, response);
            } else {
                // Rate limit exceeded
                handleRateLimitExceeded(request, response, rateLimitInfo, result);
            }

        } catch (Exception e) {
            log.error("Error in rate limit filter: {}", e.getMessage(), e);
            // On error, allow request to proceed (fail open)
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Determine rate limit identifier and limit
     */
    private RateLimitInfo getRateLimitInfo(HttpServletRequest request) {
        // Try to get API key from header
        String apiKeyHeader = request.getHeader("X-API-Key");
        if (apiKeyHeader == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ") &&
                authHeader.substring(7).startsWith("hx_")) {
                apiKeyHeader = authHeader.substring(7);
            }
        }

        if (apiKeyHeader != null) {
            // Rate limit by API key
            Optional<ApiKey> apiKey = apiKeyService.validateApiKey(apiKeyHeader);
            if (apiKey.isPresent()) {
                return new RateLimitInfo(
                    "apikey_" + apiKey.get().getId(),
                    apiKey.get().getRateLimit()
                );
            }
        }

        // Check if user is authenticated (JWT)
        // For now, use IP-based rate limiting for JWT users
        String userId = request.getRemoteUser();
        if (userId != null) {
            return new RateLimitInfo("user_" + userId, DEFAULT_RATE_LIMIT);
        }

        // Unauthenticated requests - use IP address
        String ipAddress = getClientIp(request);
        return new RateLimitInfo("ip_" + ipAddress, UNAUTHENTICATED_RATE_LIMIT);
    }

    /**
     * Add rate limit headers to response
     */
    private void addRateLimitHeaders(HttpServletResponse response,
                                    RateLimitService.RateLimitResult result) {
        response.setHeader("X-RateLimit-Limit", String.valueOf(result.limit()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.remaining()));
        response.setHeader("X-RateLimit-Reset", String.valueOf(result.resetTime()));
    }

    /**
     * Handle rate limit exceeded
     */
    private void handleRateLimitExceeded(
            HttpServletRequest request,
            HttpServletResponse response,
            RateLimitInfo rateLimitInfo,
            RateLimitService.RateLimitResult result) throws IOException {

        String ipAddress = getClientIp(request);
        String endpoint = request.getRequestURI();

        // Audit logging
        auditService.logRateLimitViolation(
            rateLimitInfo.identifier(),
            endpoint,
            0, // Current count not easily available
            result.limit(),
            ipAddress
        );

        // Set response status (429 Too Many Requests)
        response.setStatus(429);
        response.setContentType("application/json");

        // Build JSON response
        String jsonResponse = String.format(
            "{\"success\":false,\"error\":\"Rate limit exceeded\",\"limit\":%d,\"remaining\":%d,\"resetAt\":%d}",
            result.limit(),
            result.remaining(),
            result.resetTime()
        );

        response.getWriter().write(jsonResponse);
    }

    /**
     * Check if endpoint is public (no rate limiting)
     */
    private boolean isPublicEndpoint(String uri) {
        return uri.startsWith("/api/auth/login") ||
               uri.startsWith("/api/auth/refresh") ||
               uri.startsWith("/swagger-ui") ||
               uri.startsWith("/v3/api-docs") ||
               uri.startsWith("/api/health") ||
               uri.startsWith("/actuator");
    }

    /**
     * Extract client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Rate limit information
     */
    private record RateLimitInfo(String identifier, int limit) {}
}
