package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Cache Statistics DTO
 *
 * Tracks cache performance metrics and statistics.
 *
 * Metrics Tracked:
 * - Hit/miss counts and rates
 * - Eviction statistics
 * - Size and memory usage
 * - Average access times
 * - Load times
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 67 - Report Performance Optimization & Caching
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheStatistics {

    /**
     * Cache name
     */
    private String cacheName;

    /**
     * Statistics collection start time
     */
    private LocalDateTime startTime;

    /**
     * Statistics collection end time
     */
    private LocalDateTime endTime;

    // ============================================================
    // Hit/Miss Statistics
    // ============================================================

    /**
     * Total cache hits
     */
    private Long hitCount;

    /**
     * Total cache misses
     */
    private Long missCount;

    /**
     * Hit rate (percentage)
     */
    private Double hitRate;

    /**
     * Miss rate (percentage)
     */
    private Double missRate;

    // ============================================================
    // Load Statistics
    // ============================================================

    /**
     * Total load count (cache misses that triggered loads)
     */
    private Long loadCount;

    /**
     * Successful load count
     */
    private Long loadSuccessCount;

    /**
     * Failed load count
     */
    private Long loadFailureCount;

    /**
     * Total load time (milliseconds)
     */
    private Long totalLoadTimeMs;

    /**
     * Average load time (milliseconds)
     */
    private Double averageLoadTimeMs;

    // ============================================================
    // Eviction Statistics
    // ============================================================

    /**
     * Total eviction count
     */
    private Long evictionCount;

    /**
     * Eviction by size
     */
    private Long evictionBySizeCount;

    /**
     * Eviction by TTL
     */
    private Long evictionByTtlCount;

    /**
     * Eviction by manual invalidation
     */
    private Long evictionByInvalidationCount;

    // ============================================================
    // Size Statistics
    // ============================================================

    /**
     * Current cache size (entries)
     */
    private Long currentSize;

    /**
     * Maximum cache size (entries)
     */
    private Long maxSize;

    /**
     * Current memory usage (bytes)
     */
    private Long currentMemoryBytes;

    /**
     * Maximum memory usage (bytes)
     */
    private Long maxMemoryBytes;

    /**
     * Fill percentage
     */
    private Double fillPercentage;

    // ============================================================
    // Request Statistics
    // ============================================================

    /**
     * Total requests (hits + misses)
     */
    private Long totalRequests;

    /**
     * Requests per second (average)
     */
    private Double requestsPerSecond;

    /**
     * Average access time (milliseconds)
     */
    private Double averageAccessTimeMs;

    // ============================================================
    // Performance Metrics
    // ============================================================

    /**
     * Time saved by caching (milliseconds)
     */
    private Long timeSavedMs;

    /**
     * Database queries avoided
     */
    private Long queriesAvoided;

    /**
     * Network calls avoided
     */
    private Long networkCallsAvoided;

    /**
     * Estimated cost savings
     */
    private Double estimatedCostSavings;

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Calculate hit rate
     */
    public void calculateRates() {
        if (hitCount != null && missCount != null) {
            long total = hitCount + missCount;
            if (total > 0) {
                hitRate = (hitCount * 100.0) / total;
                missRate = (missCount * 100.0) / total;
            } else {
                hitRate = 0.0;
                missRate = 0.0;
            }
        }

        if (currentSize != null && maxSize != null && maxSize > 0) {
            fillPercentage = (currentSize * 100.0) / maxSize;
        }
    }

    /**
     * Calculate average load time
     */
    public void calculateAverages() {
        if (totalLoadTimeMs != null && loadCount != null && loadCount > 0) {
            averageLoadTimeMs = totalLoadTimeMs.doubleValue() / loadCount;
        }

        if (startTime != null && endTime != null) {
            long seconds = java.time.Duration.between(startTime, endTime).getSeconds();
            if (seconds > 0 && totalRequests != null) {
                requestsPerSecond = totalRequests.doubleValue() / seconds;
            }
        }
    }

    /**
     * Get human-readable memory size
     */
    public String getFormattedMemorySize() {
        if (currentMemoryBytes == null) {
            return "N/A";
        }

        if (currentMemoryBytes < 1024) {
            return currentMemoryBytes + " B";
        } else if (currentMemoryBytes < 1024 * 1024) {
            return String.format("%.2f KB", currentMemoryBytes / 1024.0);
        } else if (currentMemoryBytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", currentMemoryBytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", currentMemoryBytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * Get cache efficiency score (0-100)
     */
    public double getEfficiencyScore() {
        double score = 0.0;

        // 40% weight on hit rate
        if (hitRate != null) {
            score += hitRate * 0.4;
        }

        // 30% weight on load success rate
        if (loadCount != null && loadCount > 0 && loadSuccessCount != null) {
            double loadSuccessRate = (loadSuccessCount * 100.0) / loadCount;
            score += loadSuccessRate * 0.3;
        } else {
            score += 30.0; // Assume perfect if no loads
        }

        // 30% weight on memory efficiency
        if (fillPercentage != null) {
            // Optimal fill is 70-80%
            double memoryEfficiency = Math.max(0, 100 - Math.abs(fillPercentage - 75));
            score += memoryEfficiency * 0.3;
        } else {
            score += 30.0;
        }

        return Math.min(100.0, Math.max(0.0, score));
    }
}
