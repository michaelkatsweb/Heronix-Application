package com.heronix.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import java.util.function.Supplier;

/**
 * API Retry Handler
 *
 * Provides retry logic for API calls with exponential backoff.
 * Handles transient failures like network timeouts and connection errors.
 *
 * Retry Strategy:
 * - Maximum 3 retry attempts
 * - Exponential backoff: 1s, 2s, 4s
 * - Only retries on network/timeout errors
 * - Does not retry on authentication/authorization errors
 *
 * Usage:
 * ```
 * Map<String, Object> result = retryHandler.executeWithRetry(
 *     () -> restTemplate.getForObject(url, Map.class),
 *     "Get dashboard data"
 * );
 * ```
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 46 - Frontend Dashboard Integration
 */
@Component
@Slf4j
public class ApiRetryHandler {

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_BACKOFF_MS = 1000; // 1 second

    /**
     * Execute operation with retry logic
     *
     * @param operation Operation to execute
     * @param operationName Name for logging
     * @param <T> Return type
     * @return Operation result
     * @throws RestClientException if all retries fail
     */
    public <T> T executeWithRetry(Supplier<T> operation, String operationName) {
        int attempt = 0;
        RestClientException lastException = null;

        while (attempt < MAX_RETRIES) {
            try {
                if (attempt > 0) {
                    log.debug("Retry attempt {} for: {}", attempt, operationName);
                }

                return operation.get();

            } catch (ResourceAccessException e) {
                // Network/timeout error - retry
                lastException = e;
                attempt++;

                if (attempt < MAX_RETRIES) {
                    long backoffMs = INITIAL_BACKOFF_MS * (long) Math.pow(2, attempt - 1);
                    log.warn("API call failed (attempt {}/{}): {} - Retrying in {}ms",
                        attempt, MAX_RETRIES, e.getMessage(), backoffMs);

                    try {
                        Thread.sleep(backoffMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RestClientException("Retry interrupted", ie);
                    }
                } else {
                    log.error("API call failed after {} attempts: {}", MAX_RETRIES, e.getMessage());
                }

            } catch (RestClientException e) {
                // Other REST errors (4xx, 5xx) - don't retry
                log.warn("API call failed with non-retryable error: {}", e.getMessage());
                throw e;
            }
        }

        // All retries exhausted
        throw lastException;
    }

    /**
     * Execute operation with retry, returning null on failure instead of throwing
     *
     * @param operation Operation to execute
     * @param operationName Name for logging
     * @param <T> Return type
     * @return Operation result or null if failed
     */
    public <T> T executeWithRetrySafe(Supplier<T> operation, String operationName) {
        try {
            return executeWithRetry(operation, operationName);
        } catch (Exception e) {
            log.error("API call failed completely for {}: {}", operationName, e.getMessage());
            return null;
        }
    }

    /**
     * Check if exception is retryable
     *
     * @param e Exception to check
     * @return true if should retry
     */
    public boolean isRetryableException(Exception e) {
        // Retry on network/timeout errors
        if (e instanceof ResourceAccessException) {
            return true;
        }

        // Check exception message for common timeout/connection errors
        String message = e.getMessage();
        if (message != null) {
            return message.contains("timeout") ||
                   message.contains("Connection refused") ||
                   message.contains("Connection reset") ||
                   message.contains("No route to host");
        }

        return false;
    }
}
