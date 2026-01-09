package com.heronix.security;

import com.heronix.service.AuditService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Filter for Heronix-SIS
 *
 * Implements rate limiting using the token bucket algorithm (Bucket4j library)
 * to protect against brute force attacks and abuse.
 *
 * RATE LIMITS:
 * - Login attempts: 5 per minute per IP address
 * - Password reset: 3 per hour per IP address
 * - General API calls: 100 per minute per user
 * - Report generation: 10 per hour per user
 *
 * HOW IT WORKS:
 * 1. Each IP address or username gets a token bucket
 * 2. Buckets refill at a configured rate
 * 3. Each request consumes 1 token
 * 4. If bucket is empty, request is rejected with HTTP 429 (Too Many Requests)
 * 5. Failed attempts are logged for security monitoring
 *
 * TOKEN BUCKET ALGORITHM:
 * - Capacity: Maximum number of tokens (burst capacity)
 * - Refill Rate: Tokens added per time period
 * - Example: 5 tokens capacity, refill 5 tokens per 60 seconds
 *   = Maximum 5 requests in quick succession, then 1 request per 12 seconds
 *
 * @author Heronix Development Team
 * @version 1.0.0
 * @since 2025-12-28
 */
@Component
public class RateLimitingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);

    // ============================================================
    // Rate Limit Configuration
    // ============================================================

    // Login attempts: 5 per minute per IP
    private static final int LOGIN_CAPACITY = 5;
    private static final Duration LOGIN_REFILL_PERIOD = Duration.ofMinutes(1);

    // Password reset: 3 per hour per IP
    private static final int PASSWORD_RESET_CAPACITY = 3;
    private static final Duration PASSWORD_RESET_REFILL_PERIOD = Duration.ofHours(1);

    // General API: 100 per minute per user
    private static final int API_CAPACITY = 100;
    private static final Duration API_REFILL_PERIOD = Duration.ofMinutes(1);

    // Report generation: 10 per hour per user
    private static final int REPORT_CAPACITY = 10;
    private static final Duration REPORT_REFILL_PERIOD = Duration.ofHours(1);

    // ============================================================
    // Bucket Storage (In-Memory)
    // ============================================================

    // Map: IP Address -> Bucket
    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> passwordResetBuckets = new ConcurrentHashMap<>();

    // Map: Username -> Bucket
    private final Map<String, Bucket> apiBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> reportBuckets = new ConcurrentHashMap<>();

    // Cleanup tracking
    private long lastCleanupTime = System.currentTimeMillis();
    private static final long CLEANUP_INTERVAL_MS = 3600000; // 1 hour

    @Autowired
    private AuditService auditService;

    // ============================================================
    // Filter Implementation
    // ============================================================

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Rate Limiting Filter initialized");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                        FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String requestURI = request.getRequestURI();
        String ipAddress = getClientIpAddress(request);

        // Periodic cleanup of old buckets
        performPeriodicCleanup();

        try {
            // Apply rate limiting based on request type
            if (requestURI.contains("/login") || requestURI.contains("/perform_login")) {
                if (!checkLoginRateLimit(ipAddress, response)) {
                    return; // Request blocked
                }
            } else if (requestURI.contains("/password-reset") || requestURI.contains("/forgot-password")) {
                if (!checkPasswordResetRateLimit(ipAddress, response)) {
                    return; // Request blocked
                }
            } else if (requestURI.startsWith("/api/")) {
                String username = request.getUserPrincipal() != null
                    ? request.getUserPrincipal().getName()
                    : ipAddress;

                if (requestURI.contains("/report/")) {
                    if (!checkReportRateLimit(username, response)) {
                        return; // Request blocked
                    }
                } else {
                    if (!checkApiRateLimit(username, response)) {
                        return; // Request blocked
                    }
                }
            }

            // Rate limit passed, continue with request
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            logger.error("Error in rate limiting filter", e);
            // On error, allow request through (fail-open for availability)
            filterChain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        logger.info("Rate Limiting Filter destroyed");
        loginBuckets.clear();
        passwordResetBuckets.clear();
        apiBuckets.clear();
        reportBuckets.clear();
    }

    // ============================================================
    // Rate Limit Checking Methods
    // ============================================================

    /**
     * Check login rate limit (5 per minute per IP).
     *
     * @param ipAddress the client IP address
     * @param response the HTTP response
     * @return true if allowed, false if rate limit exceeded
     */
    private boolean checkLoginRateLimit(String ipAddress, HttpServletResponse response)
        throws IOException {

        Bucket bucket = loginBuckets.computeIfAbsent(ipAddress, k -> createLoginBucket());

        if (bucket.tryConsume(1)) {
            return true; // Request allowed
        } else {
            // Rate limit exceeded
            logger.warn("Login rate limit exceeded for IP: {}", ipAddress);
            auditService.logRateLimitExceeded("login:" + ipAddress);

            response.setStatus(429); // HTTP 429 Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\":\"Too many login attempts. Please try again in 1 minute.\"}"
            );
            return false;
        }
    }

    /**
     * Check password reset rate limit (3 per hour per IP).
     */
    private boolean checkPasswordResetRateLimit(String ipAddress, HttpServletResponse response)
        throws IOException {

        Bucket bucket = passwordResetBuckets.computeIfAbsent(ipAddress, k -> createPasswordResetBucket());

        if (bucket.tryConsume(1)) {
            return true;
        } else {
            logger.warn("Password reset rate limit exceeded for IP: {}", ipAddress);
            auditService.logRateLimitExceeded("password_reset:" + ipAddress);

            response.setStatus(429); // HTTP 429 Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\":\"Too many password reset requests. Please try again in 1 hour.\"}"
            );
            return false;
        }
    }

    /**
     * Check general API rate limit (100 per minute per user).
     */
    private boolean checkApiRateLimit(String username, HttpServletResponse response)
        throws IOException {

        Bucket bucket = apiBuckets.computeIfAbsent(username, k -> createApiBucket());

        if (bucket.tryConsume(1)) {
            return true;
        } else {
            logger.warn("API rate limit exceeded for user: {}", username);
            auditService.logRateLimitExceeded("api:" + username);

            response.setStatus(429); // HTTP 429 Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\":\"API rate limit exceeded. Please slow down.\"}"
            );
            return false;
        }
    }

    /**
     * Check report generation rate limit (10 per hour per user).
     */
    private boolean checkReportRateLimit(String username, HttpServletResponse response)
        throws IOException {

        Bucket bucket = reportBuckets.computeIfAbsent(username, k -> createReportBucket());

        if (bucket.tryConsume(1)) {
            return true;
        } else {
            logger.warn("Report generation rate limit exceeded for user: {}", username);
            auditService.logRateLimitExceeded("report:" + username);

            response.setStatus(429); // HTTP 429 Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\":\"Report generation rate limit exceeded. Please try again in 1 hour.\"}"
            );
            return false;
        }
    }

    // ============================================================
    // Bucket Creation Methods
    // ============================================================

    /**
     * Create a bucket for login attempts.
     * Capacity: 5 tokens, Refill: 5 tokens per minute
     */
    private Bucket createLoginBucket() {
        Bandwidth limit = Bandwidth.classic(
            LOGIN_CAPACITY,
            Refill.intervally(LOGIN_CAPACITY, LOGIN_REFILL_PERIOD)
        );
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    /**
     * Create a bucket for password reset requests.
     * Capacity: 3 tokens, Refill: 3 tokens per hour
     */
    private Bucket createPasswordResetBucket() {
        Bandwidth limit = Bandwidth.classic(
            PASSWORD_RESET_CAPACITY,
            Refill.intervally(PASSWORD_RESET_CAPACITY, PASSWORD_RESET_REFILL_PERIOD)
        );
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    /**
     * Create a bucket for general API calls.
     * Capacity: 100 tokens, Refill: 100 tokens per minute
     */
    private Bucket createApiBucket() {
        Bandwidth limit = Bandwidth.classic(
            API_CAPACITY,
            Refill.intervally(API_CAPACITY, API_REFILL_PERIOD)
        );
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    /**
     * Create a bucket for report generation.
     * Capacity: 10 tokens, Refill: 10 tokens per hour
     */
    private Bucket createReportBucket() {
        Bandwidth limit = Bandwidth.classic(
            REPORT_CAPACITY,
            Refill.intervally(REPORT_CAPACITY, REPORT_REFILL_PERIOD)
        );
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Extract client IP address from request.
     * Handles proxy headers (X-Forwarded-For, X-Real-IP).
     */
    private String getClientIpAddress(HttpServletRequest request) {
        // Check X-Forwarded-For header (proxy/load balancer)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs: "client, proxy1, proxy2"
            // The first IP is the original client
            return xForwardedFor.split(",")[0].trim();
        }

        // Check X-Real-IP header
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // Fall back to remote address
        return request.getRemoteAddr();
    }

    /**
     * Periodically clean up old bucket entries to prevent memory leaks.
     * Runs once per hour to remove unused buckets.
     */
    private void performPeriodicCleanup() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCleanupTime > CLEANUP_INTERVAL_MS) {
            synchronized (this) {
                if (currentTime - lastCleanupTime > CLEANUP_INTERVAL_MS) {
                    logger.info("Performing rate limit bucket cleanup");

                    // For simplicity, clear all buckets
                    // In production, you might want to check bucket age and only clear old ones
                    int totalBuckets = loginBuckets.size() + passwordResetBuckets.size()
                        + apiBuckets.size() + reportBuckets.size();

                    loginBuckets.clear();
                    passwordResetBuckets.clear();
                    apiBuckets.clear();
                    reportBuckets.clear();

                    logger.info("Cleared {} rate limit buckets", totalBuckets);
                    lastCleanupTime = currentTime;
                }
            }
        }
    }

    // ============================================================
    // Rate Limit Status Methods (for monitoring)
    // ============================================================

    /**
     * Get the number of available tokens for a login attempt.
     * Used for monitoring/debugging.
     */
    public long getAvailableLoginTokens(String ipAddress) {
        Bucket bucket = loginBuckets.get(ipAddress);
        return bucket != null ? bucket.getAvailableTokens() : LOGIN_CAPACITY;
    }

    /**
     * Get rate limiting statistics.
     */
    public Map<String, Integer> getStatistics() {
        return Map.of(
            "loginBuckets", loginBuckets.size(),
            "passwordResetBuckets", passwordResetBuckets.size(),
            "apiBuckets", apiBuckets.size(),
            "reportBuckets", reportBuckets.size()
        );
    }
}
