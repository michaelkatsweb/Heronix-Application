package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Cache DTO
 *
 * Represents cached report data and metadata for performance optimization.
 *
 * Features:
 * - Multi-level caching (Memory, Redis, Database)
 * - Cache invalidation strategies
 * - TTL management
 * - Cache warming
 * - Hit/miss tracking
 * - Performance metrics
 *
 * Cache Levels:
 * - L1: In-memory cache (fastest)
 * - L2: Distributed cache (Redis)
 * - L3: Database cache
 *
 * Eviction Policies:
 * - LRU (Least Recently Used)
 * - LFU (Least Frequently Used)
 * - FIFO (First In First Out)
 * - TTL-based
 * - Size-based
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 78 - Report Caching & Performance Optimization
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCache {

    /**
     * Cache level enumeration
     */
    public enum CacheLevel {
        L1_MEMORY,          // In-memory cache
        L2_REDIS,           // Redis distributed cache
        L3_DATABASE,        // Database cache
        ALL                 // All cache levels
    }

    /**
     * Cache status enumeration
     */
    public enum CacheStatus {
        VALID,              // Cache is valid
        STALE,              // Cache is stale but usable
        EXPIRED,            // Cache has expired
        INVALID,            // Cache is invalid
        WARMING,            // Cache is being warmed
        EVICTED             // Cache was evicted
    }

    /**
     * Eviction policy enumeration
     */
    public enum EvictionPolicy {
        LRU,                // Least Recently Used
        LFU,                // Least Frequently Used
        FIFO,               // First In First Out
        LIFO,               // Last In First Out
        TTL,                // Time To Live
        SIZE,               // Size-based
        RANDOM,             // Random eviction
        NONE                // No automatic eviction
    }

    /**
     * Cache type enumeration
     */
    public enum CacheType {
        REPORT_DATA,        // Report data cache
        QUERY_RESULT,       // Query result cache
        METADATA,           // Metadata cache
        AGGREGATION,        // Aggregation cache
        CALCULATION,        // Calculation cache
        TEMPLATE,           // Template cache
        CONFIGURATION,      // Configuration cache
        SESSION,            // Session cache
        CUSTOM              // Custom cache
    }

    // ============================================================
    // Basic Information
    // ============================================================

    /**
     * Cache ID
     */
    private Long cacheId;

    /**
     * Cache key
     */
    private String cacheKey;

    /**
     * Cache name
     */
    private String cacheName;

    /**
     * Cache type
     */
    private CacheType cacheType;

    /**
     * Cache level
     */
    private CacheLevel cacheLevel;

    /**
     * Cache status
     */
    private CacheStatus status;

    /**
     * Created at
     */
    private LocalDateTime createdAt;

    /**
     * Created by
     */
    private String createdBy;

    /**
     * Updated at
     */
    private LocalDateTime updatedAt;

    /**
     * Report ID (if specific to a report)
     */
    private Long reportId;

    /**
     * Report name
     */
    private String reportName;

    // ============================================================
    // Cache Content
    // ============================================================

    /**
     * Cached data (serialized)
     */
    private String cachedData;

    /**
     * Cached data size (bytes)
     */
    private Long dataSizeBytes;

    /**
     * Compressed
     */
    private Boolean compressed;

    /**
     * Compression ratio (percentage)
     */
    private Double compressionRatio;

    /**
     * Original size (bytes)
     */
    private Long originalSizeBytes;

    /**
     * Serialization format
     */
    private String serializationFormat;

    /**
     * Checksum (for integrity)
     */
    private String checksum;

    // ============================================================
    // TTL and Expiry
    // ============================================================

    /**
     * Time To Live (seconds)
     */
    private Integer ttlSeconds;

    /**
     * Expires at
     */
    private LocalDateTime expiresAt;

    /**
     * Is expired
     */
    private Boolean isExpired;

    /**
     * Stale threshold (seconds)
     */
    private Integer staleThresholdSeconds;

    /**
     * Is stale
     */
    private Boolean isStale;

    /**
     * Auto-refresh enabled
     */
    private Boolean autoRefreshEnabled;

    /**
     * Refresh interval (seconds)
     */
    private Integer refreshIntervalSeconds;

    /**
     * Last refreshed at
     */
    private LocalDateTime lastRefreshedAt;

    /**
     * Next refresh at
     */
    private LocalDateTime nextRefreshAt;

    // ============================================================
    // Access Tracking
    // ============================================================

    /**
     * Hit count
     */
    private Long hitCount;

    /**
     * Miss count
     */
    private Long missCount;

    /**
     * Last accessed at
     */
    private LocalDateTime lastAccessedAt;

    /**
     * Access count
     */
    private Long accessCount;

    /**
     * Access frequency (per minute)
     */
    private Double accessFrequency;

    /**
     * Average access time (ms)
     */
    private Double averageAccessTimeMs;

    /**
     * Last access time (ms)
     */
    private Long lastAccessTimeMs;

    // ============================================================
    // Eviction Policy
    // ============================================================

    /**
     * Eviction policy
     */
    private EvictionPolicy evictionPolicy;

    /**
     * Priority (higher = less likely to be evicted)
     */
    private Integer priority;

    /**
     * Eviction score
     */
    private Double evictionScore;

    /**
     * Pin to cache (prevent eviction)
     */
    private Boolean pinned;

    /**
     * Max size (bytes)
     */
    private Long maxSizeBytes;

    /**
     * Can evict
     */
    private Boolean canEvict;

    // ============================================================
    // Warming and Pre-loading
    // ============================================================

    /**
     * Pre-load enabled
     */
    private Boolean preloadEnabled;

    /**
     * Warming status
     */
    private String warmingStatus;

    /**
     * Warming started at
     */
    private LocalDateTime warmingStartedAt;

    /**
     * Warming completed at
     */
    private LocalDateTime warmingCompletedAt;

    /**
     * Warming duration (ms)
     */
    private Long warmingDurationMs;

    /**
     * Warming success
     */
    private Boolean warmingSuccess;

    // ============================================================
    // Dependencies
    // ============================================================

    /**
     * Dependent cache keys
     */
    private List<String> dependentCacheKeys;

    /**
     * Parent cache key
     */
    private String parentCacheKey;

    /**
     * Invalidation tags
     */
    private List<String> invalidationTags;

    /**
     * Version
     */
    private String version;

    /**
     * Hash (for versioning)
     */
    private String hash;

    // ============================================================
    // Performance Metrics
    // ============================================================

    /**
     * Generation time (ms)
     */
    private Long generationTimeMs;

    /**
     * Retrieval time (ms)
     */
    private Long retrievalTimeMs;

    /**
     * Cache efficiency (percentage)
     */
    private Double cacheEfficiency;

    /**
     * Hit rate (percentage)
     */
    private Double hitRate;

    /**
     * Miss rate (percentage)
     */
    private Double missRate;

    /**
     * Space savings (percentage)
     */
    private Double spaceSavings;

    /**
     * Time savings (ms)
     */
    private Long timeSavingsMs;

    // ============================================================
    // Metadata
    // ============================================================

    /**
     * Tags
     */
    private List<String> tags;

    /**
     * Custom attributes
     */
    private Map<String, Object> customAttributes;

    /**
     * Configuration
     */
    private Map<String, Object> configuration;

    /**
     * Notes
     */
    private String notes;

    // ============================================================
    // Nested Classes
    // ============================================================

    /**
     * Cache statistics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheStatistics {
        private Long totalEntries;
        private Long totalSizeBytes;
        private Long totalHits;
        private Long totalMisses;
        private Double hitRate;
        private Double missRate;
        private Long evictionCount;
        private Long expirationCount;
        private Double averageRetrievalTimeMs;
        private Double cacheEfficiency;
        private LocalDateTime lastUpdated;
    }

    /**
     * Cache access log
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheAccessLog {
        private String accessId;
        private LocalDateTime accessTime;
        private String accessedBy;
        private Boolean hit;
        private Long retrievalTimeMs;
        private String operation;
        private Map<String, Object> metadata;
    }

    /**
     * Cache invalidation event
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheInvalidationEvent {
        private String eventId;
        private LocalDateTime eventTime;
        private String invalidationReason;
        private String invalidatedBy;
        private List<String> affectedKeys;
        private String invalidationType;
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Check if cache is expired
     */
    public boolean isExpired() {
        if (expiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if cache is stale
     */
    public boolean isStale() {
        if (staleThresholdSeconds == null || lastRefreshedAt == null) {
            return false;
        }

        LocalDateTime staleTime = lastRefreshedAt.plusSeconds(staleThresholdSeconds);
        return LocalDateTime.now().isAfter(staleTime);
    }

    /**
     * Check if cache is valid
     */
    public boolean isValid() {
        return status == CacheStatus.VALID && !isExpired();
    }

    /**
     * Check if needs refresh
     */
    public boolean needsRefresh() {
        if (!Boolean.TRUE.equals(autoRefreshEnabled)) {
            return false;
        }

        if (isExpired()) {
            return true;
        }

        if (nextRefreshAt != null && LocalDateTime.now().isAfter(nextRefreshAt)) {
            return true;
        }

        return false;
    }

    /**
     * Calculate expiry time
     */
    public void calculateExpiryTime() {
        if (ttlSeconds != null && ttlSeconds > 0) {
            expiresAt = LocalDateTime.now().plusSeconds(ttlSeconds);
        }
    }

    /**
     * Calculate next refresh time
     */
    public void calculateNextRefresh() {
        if (refreshIntervalSeconds != null && refreshIntervalSeconds > 0) {
            nextRefreshAt = LocalDateTime.now().plusSeconds(refreshIntervalSeconds);
        }
    }

    /**
     * Calculate hit rate
     */
    public void calculateHitRate() {
        long total = (hitCount != null ? hitCount : 0) + (missCount != null ? missCount : 0);
        if (total > 0 && hitCount != null) {
            hitRate = (hitCount.doubleValue() / total) * 100.0;
            missRate = 100.0 - hitRate;
        }
    }

    /**
     * Calculate cache efficiency
     */
    public void calculateCacheEfficiency() {
        if (hitRate != null && averageAccessTimeMs != null && generationTimeMs != null) {
            double timeSavingRatio = 1.0 - (averageAccessTimeMs / generationTimeMs);
            cacheEfficiency = (hitRate / 100.0) * timeSavingRatio * 100.0;
        }
    }

    /**
     * Calculate compression ratio
     */
    public void calculateCompressionRatio() {
        if (originalSizeBytes != null && dataSizeBytes != null && originalSizeBytes > 0) {
            compressionRatio = (1.0 - (dataSizeBytes.doubleValue() / originalSizeBytes)) * 100.0;
        }
    }

    /**
     * Calculate space savings
     */
    public void calculateSpaceSavings() {
        if (compressionRatio != null) {
            spaceSavings = compressionRatio;
        }
    }

    /**
     * Calculate eviction score
     */
    public void calculateEvictionScore() {
        if (evictionPolicy == null) {
            evictionScore = 0.0;
            return;
        }

        switch (evictionPolicy) {
            case LRU -> {
                // Lower score = accessed longer ago = more likely to evict
                if (lastAccessedAt != null) {
                    long minutesSinceAccess = java.time.Duration.between(lastAccessedAt, LocalDateTime.now()).toMinutes();
                    evictionScore = 1000.0 / (minutesSinceAccess + 1);
                }
            }
            case LFU -> {
                // Lower score = accessed less frequently = more likely to evict
                evictionScore = accessFrequency != null ? accessFrequency : 0.0;
            }
            case TTL -> {
                // Lower score = closer to expiry = more likely to evict
                if (expiresAt != null) {
                    long minutesToExpiry = java.time.Duration.between(LocalDateTime.now(), expiresAt).toMinutes();
                    evictionScore = minutesToExpiry > 0 ? (double) minutesToExpiry : 0.0;
                }
            }
            case SIZE -> {
                // Higher size = higher score = more likely to evict
                evictionScore = dataSizeBytes != null ? dataSizeBytes.doubleValue() : 0.0;
            }
            default -> evictionScore = 0.0;
        }

        // Apply priority boost
        if (priority != null && priority > 0) {
            evictionScore *= (1.0 + (priority * 0.1));
        }

        // Pinned items have maximum score
        if (Boolean.TRUE.equals(pinned)) {
            evictionScore = Double.MAX_VALUE;
        }
    }

    /**
     * Increment hit count
     */
    public void incrementHit() {
        hitCount = (hitCount != null ? hitCount : 0) + 1;
        accessCount = (accessCount != null ? accessCount : 0) + 1;
        lastAccessedAt = LocalDateTime.now();
        calculateHitRate();
    }

    /**
     * Increment miss count
     */
    public void incrementMiss() {
        missCount = (missCount != null ? missCount : 0) + 1;
        accessCount = (accessCount != null ? accessCount : 0) + 1;
        calculateHitRate();
    }

    /**
     * Mark as expired
     */
    public void markAsExpired() {
        status = CacheStatus.EXPIRED;
        isExpired = true;
    }

    /**
     * Mark as stale
     */
    public void markAsStale() {
        status = CacheStatus.STALE;
        isStale = true;
    }

    /**
     * Mark as invalid
     */
    public void markAsInvalid() {
        status = CacheStatus.INVALID;
    }

    /**
     * Refresh cache
     */
    public void refresh() {
        lastRefreshedAt = LocalDateTime.now();
        status = CacheStatus.VALID;
        isStale = false;
        isExpired = false;
        calculateExpiryTime();
        calculateNextRefresh();
    }

    /**
     * Get size in KB
     */
    public Double getSizeInKB() {
        if (dataSizeBytes == null) {
            return 0.0;
        }
        return dataSizeBytes / 1024.0;
    }

    /**
     * Get size in MB
     */
    public Double getSizeInMB() {
        if (dataSizeBytes == null) {
            return 0.0;
        }
        return dataSizeBytes / (1024.0 * 1024.0);
    }

    /**
     * Get remaining TTL in seconds
     */
    public Long getRemainingTTL() {
        if (expiresAt == null) {
            return null;
        }

        long seconds = java.time.Duration.between(LocalDateTime.now(), expiresAt).getSeconds();
        return seconds > 0 ? seconds : 0;
    }
}
