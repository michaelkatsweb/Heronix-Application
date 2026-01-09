package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Analytics DTO
 *
 * Contains analytics and insights about report usage and performance.
 *
 * Metrics:
 * - Usage statistics
 * - Performance metrics
 * - User engagement
 * - Trends and patterns
 * - Popular reports
 * - Time-based analysis
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 73 - Report Analytics & Insights
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportAnalytics {

    /**
     * Time period enumeration
     */
    public enum TimePeriod {
        HOURLY,         // Last hour
        DAILY,          // Last 24 hours
        WEEKLY,         // Last 7 days
        MONTHLY,        // Last 30 days
        QUARTERLY,      // Last 90 days
        YEARLY,         // Last 365 days
        ALL_TIME,       // All time
        CUSTOM          // Custom date range
    }

    /**
     * Metric type enumeration
     */
    public enum MetricType {
        VIEWS,          // View count
        DOWNLOADS,      // Download count
        SHARES,         // Share count
        EXPORTS,        // Export count
        SCHEDULES,      // Schedule count
        GENERATION_TIME,    // Average generation time
        FILE_SIZE,      // Average file size
        ERROR_RATE,     // Error rate
        SUCCESS_RATE    // Success rate
    }

    // ============================================================
    // Basic Information
    // ============================================================

    /**
     * Analytics ID
     */
    private Long analyticsId;

    /**
     * Report ID (null for global analytics)
     */
    private Long reportId;

    /**
     * Report name
     */
    private String reportName;

    /**
     * Report type
     */
    private String reportType;

    /**
     * Time period
     */
    private TimePeriod timePeriod;

    /**
     * Start date
     */
    private LocalDateTime startDate;

    /**
     * End date
     */
    private LocalDateTime endDate;

    /**
     * Generated at
     */
    private LocalDateTime generatedAt;

    // ============================================================
    // Usage Statistics
    // ============================================================

    /**
     * Total views
     */
    private Long totalViews;

    /**
     * Unique viewers
     */
    private Long uniqueViewers;

    /**
     * Total downloads
     */
    private Long totalDownloads;

    /**
     * Total exports
     */
    private Long totalExports;

    /**
     * Total shares
     */
    private Long totalShares;

    /**
     * Total generations
     */
    private Long totalGenerations;

    /**
     * Total schedules
     */
    private Long totalSchedules;

    /**
     * Active schedules
     */
    private Long activeSchedules;

    // ============================================================
    // Performance Metrics
    // ============================================================

    /**
     * Average generation time (ms)
     */
    private Double avgGenerationTime;

    /**
     * Min generation time (ms)
     */
    private Long minGenerationTime;

    /**
     * Max generation time (ms)
     */
    private Long maxGenerationTime;

    /**
     * Median generation time (ms)
     */
    private Long medianGenerationTime;

    /**
     * Average file size (bytes)
     */
    private Double avgFileSize;

    /**
     * Min file size (bytes)
     */
    private Long minFileSize;

    /**
     * Max file size (bytes)
     */
    private Long maxFileSize;

    /**
     * Total storage used (bytes)
     */
    private Long totalStorageUsed;

    // ============================================================
    // Quality Metrics
    // ============================================================

    /**
     * Success count
     */
    private Long successCount;

    /**
     * Error count
     */
    private Long errorCount;

    /**
     * Success rate (percentage)
     */
    private Double successRate;

    /**
     * Error rate (percentage)
     */
    private Double errorRate;

    /**
     * Cache hit count
     */
    private Long cacheHitCount;

    /**
     * Cache miss count
     */
    private Long cacheMissCount;

    /**
     * Cache hit rate (percentage)
     */
    private Double cacheHitRate;

    // ============================================================
    // User Engagement
    // ============================================================

    /**
     * Total users
     */
    private Long totalUsers;

    /**
     * Active users
     */
    private Long activeUsers;

    /**
     * New users
     */
    private Long newUsers;

    /**
     * Returning users
     */
    private Long returningUsers;

    /**
     * Average views per user
     */
    private Double avgViewsPerUser;

    /**
     * Average downloads per user
     */
    private Double avgDownloadsPerUser;

    /**
     * Most active users
     */
    private List<UserActivity> mostActiveUsers;

    // ============================================================
    // Format Distribution
    // ============================================================

    /**
     * Export by format (format -> count)
     */
    private Map<String, Long> exportsByFormat;

    /**
     * Most popular format
     */
    private String mostPopularFormat;

    // ============================================================
    // Time-Based Analysis
    // ============================================================

    /**
     * Views by hour (hour -> count)
     */
    private Map<Integer, Long> viewsByHour;

    /**
     * Views by day of week (day -> count)
     */
    private Map<String, Long> viewsByDayOfWeek;

    /**
     * Views over time (date -> count)
     */
    private Map<String, Long> viewsOverTime;

    /**
     * Peak usage hour
     */
    private Integer peakUsageHour;

    /**
     * Peak usage day
     */
    private String peakUsageDay;

    /**
     * Trend direction (INCREASING, DECREASING, STABLE)
     */
    private String trendDirection;

    /**
     * Growth rate (percentage)
     */
    private Double growthRate;

    // ============================================================
    // Top Reports
    // ============================================================

    /**
     * Most viewed reports
     */
    private List<ReportRanking> mostViewedReports;

    /**
     * Most downloaded reports
     */
    private List<ReportRanking> mostDownloadedReports;

    /**
     * Most shared reports
     */
    private List<ReportRanking> mostSharedReports;

    /**
     * Fastest reports
     */
    private List<ReportRanking> fastestReports;

    /**
     * Slowest reports
     */
    private List<ReportRanking> slowestReports;

    // ============================================================
    // Error Analysis
    // ============================================================

    /**
     * Errors by type (type -> count)
     */
    private Map<String, Long> errorsByType;

    /**
     * Most common error
     */
    private String mostCommonError;

    /**
     * Recent errors
     */
    private List<ErrorSummary> recentErrors;

    // ============================================================
    // Comparison Metrics
    // ============================================================

    /**
     * Comparison to previous period
     */
    private ComparisonMetrics comparisonMetrics;

    // ============================================================
    // Insights and Recommendations
    // ============================================================

    /**
     * Key insights
     */
    private List<String> insights;

    /**
     * Recommendations
     */
    private List<String> recommendations;

    /**
     * Anomalies detected
     */
    private List<String> anomalies;

    // ============================================================
    // Custom Metrics
    // ============================================================

    /**
     * Custom metrics
     */
    private Map<String, Object> customMetrics;

    // ============================================================
    // Nested Classes
    // ============================================================

    /**
     * User activity summary
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserActivity {
        private String username;
        private String displayName;
        private Long viewCount;
        private Long downloadCount;
        private Long shareCount;
        private LocalDateTime lastActive;
    }

    /**
     * Report ranking entry
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportRanking {
        private Long reportId;
        private String reportName;
        private String reportType;
        private Long count;
        private Double value;
        private Integer rank;
    }

    /**
     * Error summary
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorSummary {
        private String errorType;
        private String errorMessage;
        private Long count;
        private LocalDateTime lastOccurrence;
    }

    /**
     * Comparison metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonMetrics {
        private Double viewsChange;
        private Double downloadsChange;
        private Double sharesChange;
        private Double generationTimeChange;
        private Double errorRateChange;
        private String viewsTrend;       // UP, DOWN, STABLE
        private String downloadsTrend;
        private String sharesTrend;
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Calculate success rate
     */
    public void calculateSuccessRate() {
        if (successCount != null && errorCount != null) {
            long total = successCount + errorCount;
            if (total > 0) {
                successRate = (successCount * 100.0) / total;
                errorRate = (errorCount * 100.0) / total;
            }
        }
    }

    /**
     * Calculate cache hit rate
     */
    public void calculateCacheHitRate() {
        if (cacheHitCount != null && cacheMissCount != null) {
            long total = cacheHitCount + cacheMissCount;
            if (total > 0) {
                cacheHitRate = (cacheHitCount * 100.0) / total;
            }
        }
    }

    /**
     * Calculate average metrics
     */
    public void calculateAverages() {
        if (totalViews != null && totalUsers != null && totalUsers > 0) {
            avgViewsPerUser = totalViews.doubleValue() / totalUsers;
        }

        if (totalDownloads != null && totalUsers != null && totalUsers > 0) {
            avgDownloadsPerUser = totalDownloads.doubleValue() / totalUsers;
        }
    }

    /**
     * Get formatted generation time
     */
    public String getFormattedAvgGenerationTime() {
        if (avgGenerationTime == null) {
            return "N/A";
        }

        if (avgGenerationTime < 1000) {
            return String.format("%.0f ms", avgGenerationTime);
        } else if (avgGenerationTime < 60000) {
            return String.format("%.2f sec", avgGenerationTime / 1000.0);
        } else {
            return String.format("%.2f min", avgGenerationTime / 60000.0);
        }
    }

    /**
     * Get formatted storage size
     */
    public String getFormattedStorageSize() {
        if (totalStorageUsed == null) {
            return "0 B";
        }

        long size = totalStorageUsed;
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.2f MB", size / (1024.0 * 1024));
        return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
    }

    /**
     * Add insight
     */
    public void addInsight(String insight) {
        if (insights == null) {
            insights = new java.util.ArrayList<>();
        }
        insights.add(insight);
    }

    /**
     * Add recommendation
     */
    public void addRecommendation(String recommendation) {
        if (recommendations == null) {
            recommendations = new java.util.ArrayList<>();
        }
        recommendations.add(recommendation);
    }

    /**
     * Add anomaly
     */
    public void addAnomaly(String anomaly) {
        if (anomalies == null) {
            anomalies = new java.util.ArrayList<>();
        }
        anomalies.add(anomaly);
    }

    /**
     * Check if usage is increasing
     */
    public boolean isUsageIncreasing() {
        return "INCREASING".equals(trendDirection);
    }

    /**
     * Check if usage is decreasing
     */
    public boolean isUsageDecreasing() {
        return "DECREASING".equals(trendDirection);
    }

    /**
     * Get total activity
     */
    public long getTotalActivity() {
        long total = 0;
        if (totalViews != null) total += totalViews;
        if (totalDownloads != null) total += totalDownloads;
        if (totalShares != null) total += totalShares;
        if (totalExports != null) total += totalExports;
        return total;
    }

    /**
     * Check if has errors
     */
    public boolean hasErrors() {
        return errorCount != null && errorCount > 0;
    }

    /**
     * Check if performance is good
     */
    public boolean isPerformanceGood() {
        if (avgGenerationTime == null) return true;
        return avgGenerationTime < 5000; // Less than 5 seconds
    }

    /**
     * Check if highly used
     */
    public boolean isHighlyUsed() {
        return totalViews != null && totalViews > 100;
    }
}
