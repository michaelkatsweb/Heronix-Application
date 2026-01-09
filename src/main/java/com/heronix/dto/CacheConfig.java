package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.Map;

/**
 * Cache Configuration DTO
 *
 * Configuration for report caching and performance optimization.
 *
 * Cache Strategies:
 * - Time-based expiration (TTL)
 * - Size-based eviction (LRU)
 * - Manual invalidation
 * - Conditional caching
 *
 * Cache Levels:
 * - L1: In-memory (fast, limited size)
 * - L2: Redis (persistent, distributed)
 * - L3: Database (permanent storage)
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 67 - Report Performance Optimization & Caching
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheConfig {

    /**
     * Cache strategy enumeration
     */
    public enum CacheStrategy {
        NEVER,              // Never cache
        ALWAYS,             // Always cache
        CONDITIONAL,        // Cache based on conditions
        ON_DEMAND          // Cache only when explicitly requested
    }

    /**
     * Cache eviction policy
     */
    public enum EvictionPolicy {
        LRU,                // Least Recently Used
        LFU,                // Least Frequently Used
        FIFO,               // First In First Out
        TTL                 // Time To Live
    }

    /**
     * Cache level
     */
    public enum CacheLevel {
        MEMORY,             // In-memory cache (fastest)
        REDIS,              // Redis cache (distributed)
        DATABASE,           // Database cache (persistent)
        MULTI_LEVEL         // Multiple cache levels
    }

    // ============================================================
    // Basic Configuration
    // ============================================================

    /**
     * Cache name/identifier
     */
    private String cacheName;

    /**
     * Cache strategy
     */
    private CacheStrategy strategy;

    /**
     * Cache level
     */
    private CacheLevel level;

    /**
     * Eviction policy
     */
    private EvictionPolicy evictionPolicy;

    /**
     * Enable caching
     */
    private Boolean enabled;

    // ============================================================
    // Time-Based Configuration
    // ============================================================

    /**
     * Time to live (seconds)
     */
    private Long ttlSeconds;

    /**
     * Time to idle (seconds)
     */
    private Long ttiSeconds;

    /**
     * Refresh interval (seconds)
     */
    private Long refreshIntervalSeconds;

    /**
     * Cache warm-up on startup
     */
    private Boolean warmupOnStartup;

    // ============================================================
    // Size-Based Configuration
    // ============================================================

    /**
     * Maximum cache size (entries)
     */
    private Integer maxSize;

    /**
     * Maximum memory size (bytes)
     */
    private Long maxMemoryBytes;

    /**
     * Size eviction threshold (percentage)
     */
    private Integer evictionThreshold;

    // ============================================================
    // Conditional Caching
    // ============================================================

    /**
     * Minimum data size to cache (bytes)
     */
    private Long minDataSizeBytes;

    /**
     * Maximum data size to cache (bytes)
     */
    private Long maxDataSizeBytes;

    /**
     * Cache only if query time exceeds threshold (milliseconds)
     */
    private Long minQueryTimeMs;

    /**
     * Report types to cache
     */
    private java.util.List<String> cacheableReportTypes;

    /**
     * Report types to never cache
     */
    private java.util.List<String> nonCacheableReportTypes;

    // ============================================================
    // Invalidation Configuration
    // ============================================================

    /**
     * Auto-invalidate on data change
     */
    private Boolean autoInvalidateOnChange;

    /**
     * Invalidation event patterns
     */
    private java.util.List<String> invalidationEvents;

    /**
     * Cascade invalidation to related caches
     */
    private Boolean cascadeInvalidation;

    /**
     * Related cache names for cascade
     */
    private java.util.List<String> relatedCaches;

    // ============================================================
    // Performance Monitoring
    // ============================================================

    /**
     * Enable cache statistics
     */
    private Boolean enableStatistics;

    /**
     * Enable cache hit/miss logging
     */
    private Boolean enableLogging;

    /**
     * Log cache hits
     */
    private Boolean logHits;

    /**
     * Log cache misses
     */
    private Boolean logMisses;

    /**
     * Statistics collection interval (seconds)
     */
    private Long statsIntervalSeconds;

    // ============================================================
    // Advanced Options
    // ============================================================

    /**
     * Use compression for cached data
     */
    private Boolean useCompression;

    /**
     * Serialize cached data
     */
    private Boolean serialize;

    /**
     * Encryption for sensitive data
     */
    private Boolean encrypt;

    /**
     * Pre-compute data on cache
     */
    private Boolean preCompute;

    /**
     * Background refresh
     */
    private Boolean backgroundRefresh;

    /**
     * Custom cache key prefix
     */
    private String keyPrefix;

    /**
     * Custom metadata
     */
    private Map<String, Object> metadata;

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Get TTL as Duration
     */
    public Duration getTtlDuration() {
        return ttlSeconds != null ? Duration.ofSeconds(ttlSeconds) : null;
    }

    /**
     * Get TTI as Duration
     */
    public Duration getTtiDuration() {
        return ttiSeconds != null ? Duration.ofSeconds(ttiSeconds) : null;
    }

    /**
     * Check if report type is cacheable
     */
    public boolean isCacheable(String reportType) {
        if (strategy == CacheStrategy.NEVER) {
            return false;
        }

        if (nonCacheableReportTypes != null && nonCacheableReportTypes.contains(reportType)) {
            return false;
        }

        if (cacheableReportTypes != null && !cacheableReportTypes.isEmpty()) {
            return cacheableReportTypes.contains(reportType);
        }

        return strategy == CacheStrategy.ALWAYS || strategy == CacheStrategy.CONDITIONAL;
    }

    /**
     * Check if data size is within cache limits
     */
    public boolean isWithinSizeLimits(long dataSize) {
        if (minDataSizeBytes != null && dataSize < minDataSizeBytes) {
            return false;
        }

        if (maxDataSizeBytes != null && dataSize > maxDataSizeBytes) {
            return false;
        }

        return true;
    }

    /**
     * Check if query time meets caching threshold
     */
    public boolean meetsQueryTimeThreshold(long queryTimeMs) {
        return minQueryTimeMs == null || queryTimeMs >= minQueryTimeMs;
    }

    /**
     * Generate cache key
     */
    public String generateCacheKey(String... parts) {
        String key = String.join(":", parts);
        if (keyPrefix != null && !keyPrefix.isEmpty()) {
            key = keyPrefix + ":" + key;
        }
        return key;
    }

    /**
     * Validate configuration
     */
    public void validate() {
        if (cacheName == null || cacheName.trim().isEmpty()) {
            throw new IllegalArgumentException("Cache name is required");
        }

        if (strategy == null) {
            throw new IllegalArgumentException("Cache strategy is required");
        }

        if (level == null) {
            throw new IllegalArgumentException("Cache level is required");
        }

        if (ttlSeconds != null && ttlSeconds <= 0) {
            throw new IllegalArgumentException("TTL must be positive");
        }

        if (maxSize != null && maxSize <= 0) {
            throw new IllegalArgumentException("Max size must be positive");
        }
    }
}
