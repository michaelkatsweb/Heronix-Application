package com.heronix.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Service
 *
 * Implements token bucket algorithm for API rate limiting.
 * Prevents API abuse by limiting the number of requests per time window.
 *
 * Token Bucket Algorithm:
 * - Each client has a bucket with a maximum capacity (rate limit)
 * - Bucket starts full and refills at a constant rate
 * - Each request consumes one token
 * - If bucket is empty, request is rejected (HTTP 429 Too Many Requests)
 * - Tokens refill over time up to maximum capacity
 *
 * Rate Limiting Strategies:
 * - Per API Key: Each API key has its own rate limit
 * - Per Endpoint: Different endpoints can have different limits
 * - Per User: User-based rate limiting for authenticated requests
 * - Global: Overall system rate limit
 *
 * Features:
 * - Thread-safe concurrent implementation
 * - Configurable bucket capacity and refill rate
 * - Automatic cleanup of old buckets
 * - Rate limit headers in responses (X-RateLimit-Remaining, X-RateLimit-Reset)
 *
 * Configuration:
 * - Rate limits are stored per API key in database
 * - Default rate limit: 1000 requests per hour
 * - Burst capacity: Same as rate limit
 *
 * HTTP Headers:
 * - X-RateLimit-Limit: Maximum requests per hour
 * - X-RateLimit-Remaining: Remaining requests in current window
 * - X-RateLimit-Reset: Unix timestamp when limit resets
 *
 * Example Usage:
 * if (!rateLimitService.allowRequest(apiKeyId, rateLimit)) {
 *     return ResponseEntity.status(429).body("Too many requests");
 * }
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 42 - API Security & Authentication
 */
@Service
@Slf4j
public class RateLimitService {

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    /**
     * Check if request is allowed under rate limit
     *
     * @param identifier Unique identifier (API key ID, user ID, etc.)
     * @param maxRequestsPerHour Maximum requests allowed per hour
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean allowRequest(String identifier, int maxRequestsPerHour) {
        TokenBucket bucket = buckets.computeIfAbsent(identifier,
            k -> new TokenBucket(maxRequestsPerHour));

        boolean allowed = bucket.tryConsume();

        if (!allowed) {
            log.warn("Rate limit exceeded for identifier: {} (limit: {} req/hour)",
                identifier, maxRequestsPerHour);
        }

        return allowed;
    }

    /**
     * Get remaining requests for identifier
     *
     * @param identifier Unique identifier
     * @param maxRequestsPerHour Maximum requests per hour
     * @return Number of remaining requests
     */
    public long getRemainingRequests(String identifier, int maxRequestsPerHour) {
        TokenBucket bucket = buckets.computeIfAbsent(identifier,
            k -> new TokenBucket(maxRequestsPerHour));
        return bucket.getAvailableTokens();
    }

    /**
     * Get reset time for rate limit window
     *
     * @param identifier Unique identifier
     * @return Unix timestamp when rate limit resets
     */
    public long getResetTime(String identifier) {
        TokenBucket bucket = buckets.get(identifier);
        if (bucket == null) {
            return Instant.now().plusSeconds(3600).getEpochSecond();
        }
        return bucket.getResetTime();
    }

    /**
     * Clear rate limit for identifier (admin function)
     *
     * @param identifier Unique identifier
     */
    public void clearRateLimit(String identifier) {
        buckets.remove(identifier);
        log.info("Cleared rate limit for identifier: {}", identifier);
    }

    /**
     * Clean up old buckets that haven't been used recently
     * Should be called periodically (e.g., via scheduled task)
     */
    public void cleanupOldBuckets() {
        long now = Instant.now().getEpochSecond();
        long oneHourAgo = now - 3600;

        buckets.entrySet().removeIf(entry -> {
            TokenBucket bucket = entry.getValue();
            return bucket.getLastRefillTime() < oneHourAgo;
        });

        log.debug("Cleaned up old rate limit buckets. Current buckets: {}", buckets.size());
    }

    /**
     * Token Bucket Implementation
     * Thread-safe token bucket for rate limiting
     */
    private static class TokenBucket {
        private final long capacity;
        private final double refillRate; // tokens per second
        private volatile long availableTokens;
        private volatile long lastRefillTimestamp;

        /**
         * Create token bucket with capacity in requests per hour
         *
         * @param requestsPerHour Maximum requests per hour
         */
        public TokenBucket(int requestsPerHour) {
            this.capacity = requestsPerHour;
            this.refillRate = requestsPerHour / 3600.0; // Convert to tokens per second
            this.availableTokens = requestsPerHour; // Start with full bucket
            this.lastRefillTimestamp = Instant.now().getEpochSecond();
        }

        /**
         * Try to consume one token from bucket
         *
         * @return true if token was consumed, false if bucket is empty
         */
        public synchronized boolean tryConsume() {
            refill();

            if (availableTokens > 0) {
                availableTokens--;
                return true;
            }

            return false;
        }

        /**
         * Get number of available tokens
         */
        public synchronized long getAvailableTokens() {
            refill();
            return availableTokens;
        }

        /**
         * Get reset time (when bucket will be full again)
         */
        public synchronized long getResetTime() {
            refill();
            if (availableTokens >= capacity) {
                return Instant.now().getEpochSecond();
            }

            long tokensNeeded = capacity - availableTokens;
            long secondsToRefill = (long) (tokensNeeded / refillRate);
            return Instant.now().getEpochSecond() + secondsToRefill;
        }

        /**
         * Get last refill timestamp
         */
        public long getLastRefillTime() {
            return lastRefillTimestamp;
        }

        /**
         * Refill tokens based on elapsed time
         */
        private void refill() {
            long now = Instant.now().getEpochSecond();
            long elapsedSeconds = now - lastRefillTimestamp;

            if (elapsedSeconds > 0) {
                long tokensToAdd = (long) (elapsedSeconds * refillRate);
                availableTokens = Math.min(capacity, availableTokens + tokensToAdd);
                lastRefillTimestamp = now;
            }
        }
    }

    /**
     * Rate limit result with details for HTTP headers
     */
    public record RateLimitResult(
        boolean allowed,
        long limit,
        long remaining,
        long resetTime
    ) {}

    /**
     * Check rate limit and return detailed result
     *
     * @param identifier Unique identifier
     * @param maxRequestsPerHour Maximum requests per hour
     * @return Rate limit result with details
     */
    public RateLimitResult checkRateLimit(String identifier, int maxRequestsPerHour) {
        boolean allowed = allowRequest(identifier, maxRequestsPerHour);
        long remaining = getRemainingRequests(identifier, maxRequestsPerHour);
        long resetTime = getResetTime(identifier);

        return new RateLimitResult(allowed, maxRequestsPerHour, remaining, resetTime);
    }
}
