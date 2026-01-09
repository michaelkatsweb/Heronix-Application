package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Monitor DTO
 *
 * Represents monitoring and health check configuration for reports.
 *
 * Features:
 * - System health monitoring
 * - Performance metrics
 * - Resource utilization
 * - Error tracking
 * - Alert management
 * - Uptime monitoring
 *
 * Health Check Types:
 * - Liveness check (is system running)
 * - Readiness check (is system ready to serve)
 * - Database connectivity
 * - External service availability
 * - Resource availability
 * - Custom checks
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 81 - Report Monitoring & Health Checks
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportMonitor {

    /**
     * Health status enumeration
     */
    public enum HealthStatus {
        HEALTHY,            // System is healthy
        DEGRADED,           // System is degraded
        UNHEALTHY,          // System is unhealthy
        UNKNOWN             // Health status unknown
    }

    /**
     * Metric type enumeration
     */
    public enum MetricType {
        COUNTER,            // Counter metric
        GAUGE,              // Gauge metric
        HISTOGRAM,          // Histogram metric
        SUMMARY,            // Summary metric
        TIMER               // Timer metric
    }

    /**
     * Alert severity enumeration
     */
    public enum AlertSeverity {
        CRITICAL,           // Critical alert
        HIGH,               // High severity
        MEDIUM,             // Medium severity
        LOW,                // Low severity
        INFO                // Informational
    }

    /**
     * Check type enumeration
     */
    public enum CheckType {
        LIVENESS,           // Liveness check
        READINESS,          // Readiness check
        DATABASE,           // Database check
        CACHE,              // Cache check
        DISK,               // Disk space check
        MEMORY,             // Memory check
        CPU,                // CPU check
        EXTERNAL_SERVICE,   // External service check
        CUSTOM              // Custom check
    }

    // ============================================================
    // Basic Information
    // ============================================================

    /**
     * Monitor ID
     */
    private Long monitorId;

    /**
     * Monitor name
     */
    private String name;

    /**
     * Description
     */
    private String description;

    /**
     * Component name
     */
    private String componentName;

    /**
     * Health status
     */
    private HealthStatus healthStatus;

    /**
     * Created at
     */
    private LocalDateTime createdAt;

    /**
     * Updated at
     */
    private LocalDateTime updatedAt;

    /**
     * Last check at
     */
    private LocalDateTime lastCheckAt;

    /**
     * Next check at
     */
    private LocalDateTime nextCheckAt;

    // ============================================================
    // Health Checks
    // ============================================================

    /**
     * Health checks enabled
     */
    private Boolean healthChecksEnabled;

    /**
     * Check interval (seconds)
     */
    private Integer checkIntervalSeconds;

    /**
     * Timeout (seconds)
     */
    private Integer timeoutSeconds;

    /**
     * Health checks
     */
    private List<HealthCheck> healthChecks;

    /**
     * Total checks
     */
    private Integer totalChecks;

    /**
     * Passed checks
     */
    private Integer passedChecks;

    /**
     * Failed checks
     */
    private Integer failedChecks;

    /**
     * Check success rate (percentage)
     */
    private Double checkSuccessRate;

    // ============================================================
    // Performance Metrics
    // ============================================================

    /**
     * Response time (ms)
     */
    private Long responseTimeMs;

    /**
     * Average response time (ms)
     */
    private Double averageResponseTimeMs;

    /**
     * Min response time (ms)
     */
    private Long minResponseTimeMs;

    /**
     * Max response time (ms)
     */
    private Long maxResponseTimeMs;

    /**
     * Throughput (requests per second)
     */
    private Double throughput;

    /**
     * Error rate (percentage)
     */
    private Double errorRate;

    /**
     * Success rate (percentage)
     */
    private Double successRate;

    /**
     * Total requests
     */
    private Long totalRequests;

    /**
     * Successful requests
     */
    private Long successfulRequests;

    /**
     * Failed requests
     */
    private Long failedRequests;

    // ============================================================
    // Resource Utilization
    // ============================================================

    /**
     * CPU usage (percentage)
     */
    private Double cpuUsagePercent;

    /**
     * Memory usage (percentage)
     */
    private Double memoryUsagePercent;

    /**
     * Memory used (MB)
     */
    private Long memoryUsedMB;

    /**
     * Memory total (MB)
     */
    private Long memoryTotalMB;

    /**
     * Disk usage (percentage)
     */
    private Double diskUsagePercent;

    /**
     * Disk used (GB)
     */
    private Long diskUsedGB;

    /**
     * Disk total (GB)
     */
    private Long diskTotalGB;

    /**
     * Thread count
     */
    private Integer threadCount;

    /**
     * Active connections
     */
    private Integer activeConnections;

    /**
     * Database connection pool size
     */
    private Integer dbConnectionPoolSize;

    /**
     * Cache hit rate (percentage)
     */
    private Double cacheHitRate;

    // ============================================================
    // Uptime & Availability
    // ============================================================

    /**
     * Uptime (seconds)
     */
    private Long uptimeSeconds;

    /**
     * Started at
     */
    private LocalDateTime startedAt;

    /**
     * Availability (percentage)
     */
    private Double availabilityPercent;

    /**
     * Downtime events
     */
    private Integer downtimeEvents;

    /**
     * Last downtime
     */
    private LocalDateTime lastDowntime;

    /**
     * Total downtime (minutes)
     */
    private Long totalDowntimeMinutes;

    /**
     * SLA target (percentage)
     */
    private Double slaTarget;

    /**
     * SLA compliance (percentage)
     */
    private Double slaCompliance;

    // ============================================================
    // Error Tracking
    // ============================================================

    /**
     * Total errors
     */
    private Long totalErrors;

    /**
     * Errors in last hour
     */
    private Integer errorsLastHour;

    /**
     * Errors in last 24h
     */
    private Integer errorsLast24Hours;

    /**
     * Last error
     */
    private String lastError;

    /**
     * Last error at
     */
    private LocalDateTime lastErrorAt;

    /**
     * Error types
     */
    private Map<String, Integer> errorTypes;

    /**
     * Critical errors
     */
    private Integer criticalErrors;

    /**
     * Warning count
     */
    private Integer warningCount;

    // ============================================================
    // Alerts
    // ============================================================

    /**
     * Alerts enabled
     */
    private Boolean alertsEnabled;

    /**
     * Active alerts
     */
    private List<Alert> activeAlerts;

    /**
     * Alert count
     */
    private Integer alertCount;

    /**
     * Critical alerts
     */
    private Integer criticalAlerts;

    /**
     * Last alert
     */
    private LocalDateTime lastAlert;

    /**
     * Alert thresholds
     */
    private Map<String, Double> alertThresholds;

    // ============================================================
    // Dependencies
    // ============================================================

    /**
     * Database status
     */
    private String databaseStatus;

    /**
     * Cache status
     */
    private String cacheStatus;

    /**
     * External services status
     */
    private Map<String, String> externalServicesStatus;

    /**
     * Dependencies healthy
     */
    private Boolean dependenciesHealthy;

    // ============================================================
    // Trends
    // ============================================================

    /**
     * Trend direction
     */
    private String trendDirection;

    /**
     * Performance trend
     */
    private String performanceTrend;

    /**
     * Error trend
     */
    private String errorTrend;

    /**
     * Historical data
     */
    private List<HistoricalData> historicalData;

    // ============================================================
    // Metadata
    // ============================================================

    /**
     * Version
     */
    private String version;

    /**
     * Environment
     */
    private String environment;

    /**
     * Tags
     */
    private List<String> tags;

    /**
     * Custom metrics
     */
    private Map<String, Object> customMetrics;

    /**
     * Notes
     */
    private String notes;

    // ============================================================
    // Nested Classes
    // ============================================================

    /**
     * Health check
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HealthCheck {
        private String checkId;
        private String checkName;
        private CheckType checkType;
        private HealthStatus status;
        private String message;
        private LocalDateTime checkedAt;
        private Long responseTimeMs;
        private Boolean passed;
        private Map<String, Object> checkData;
    }

    /**
     * Alert
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Alert {
        private String alertId;
        private String alertName;
        private AlertSeverity severity;
        private String message;
        private LocalDateTime triggeredAt;
        private Boolean acknowledged;
        private LocalDateTime acknowledgedAt;
        private String acknowledgedBy;
        private Boolean resolved;
        private LocalDateTime resolvedAt;
        private Map<String, Object> alertData;
    }

    /**
     * Metric
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metric {
        private String metricName;
        private MetricType metricType;
        private Double value;
        private String unit;
        private LocalDateTime timestamp;
        private Map<String, String> tags;
    }

    /**
     * Historical data point
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoricalData {
        private LocalDateTime timestamp;
        private Double responseTime;
        private Double cpuUsage;
        private Double memoryUsage;
        private Integer errorCount;
        private Double throughput;
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Check if healthy
     */
    public boolean isHealthy() {
        return healthStatus == HealthStatus.HEALTHY;
    }

    /**
     * Check if degraded
     */
    public boolean isDegraded() {
        return healthStatus == HealthStatus.DEGRADED;
    }

    /**
     * Check if unhealthy
     */
    public boolean isUnhealthy() {
        return healthStatus == HealthStatus.UNHEALTHY;
    }

    /**
     * Calculate health status
     */
    public void calculateHealthStatus() {
        int score = 100;

        // Check CPU usage
        if (cpuUsagePercent != null) {
            if (cpuUsagePercent > 90) score -= 30;
            else if (cpuUsagePercent > 80) score -= 20;
            else if (cpuUsagePercent > 70) score -= 10;
        }

        // Check memory usage
        if (memoryUsagePercent != null) {
            if (memoryUsagePercent > 90) score -= 30;
            else if (memoryUsagePercent > 80) score -= 20;
            else if (memoryUsagePercent > 70) score -= 10;
        }

        // Check error rate
        if (errorRate != null) {
            if (errorRate > 10) score -= 30;
            else if (errorRate > 5) score -= 20;
            else if (errorRate > 2) score -= 10;
        }

        // Check response time
        if (averageResponseTimeMs != null) {
            if (averageResponseTimeMs > 5000) score -= 20;
            else if (averageResponseTimeMs > 2000) score -= 10;
            else if (averageResponseTimeMs > 1000) score -= 5;
        }

        // Check failed health checks
        if (checkSuccessRate != null) {
            if (checkSuccessRate < 70) score -= 30;
            else if (checkSuccessRate < 85) score -= 20;
            else if (checkSuccessRate < 95) score -= 10;
        }

        // Determine health status
        if (score >= 80) {
            healthStatus = HealthStatus.HEALTHY;
        } else if (score >= 60) {
            healthStatus = HealthStatus.DEGRADED;
        } else {
            healthStatus = HealthStatus.UNHEALTHY;
        }
    }

    /**
     * Calculate check success rate
     */
    public void calculateCheckSuccessRate() {
        if (totalChecks != null && totalChecks > 0 && passedChecks != null) {
            checkSuccessRate = (passedChecks.doubleValue() / totalChecks) * 100.0;
        }
    }

    /**
     * Calculate success rate
     */
    public void calculateSuccessRate() {
        if (totalRequests != null && totalRequests > 0 && successfulRequests != null) {
            successRate = (successfulRequests.doubleValue() / totalRequests) * 100.0;
            errorRate = 100.0 - successRate;
        }
    }

    /**
     * Calculate availability
     */
    public void calculateAvailability() {
        if (uptimeSeconds != null && totalDowntimeMinutes != null) {
            long totalSeconds = uptimeSeconds + (totalDowntimeMinutes * 60);
            if (totalSeconds > 0) {
                availabilityPercent = (uptimeSeconds.doubleValue() / totalSeconds) * 100.0;
            }
        }
    }

    /**
     * Calculate SLA compliance
     */
    public void calculateSLACompliance() {
        if (slaTarget != null && availabilityPercent != null) {
            slaCompliance = (availabilityPercent / slaTarget) * 100.0;
        }
    }

    /**
     * Check if needs attention
     */
    public boolean needsAttention() {
        return isUnhealthy() ||
               (errorRate != null && errorRate > 5) ||
               (cpuUsagePercent != null && cpuUsagePercent > 90) ||
               (memoryUsagePercent != null && memoryUsagePercent > 90) ||
               (criticalAlerts != null && criticalAlerts > 0);
    }

    /**
     * Check if performance degraded
     */
    public boolean isPerformanceDegraded() {
        return (averageResponseTimeMs != null && averageResponseTimeMs > 2000) ||
               (errorRate != null && errorRate > 5) ||
               (throughput != null && throughput < 10);
    }

    /**
     * Get uptime in hours
     */
    public Double getUptimeHours() {
        if (uptimeSeconds == null) {
            return null;
        }
        return uptimeSeconds / 3600.0;
    }

    /**
     * Get uptime in days
     */
    public Double getUptimeDays() {
        if (uptimeSeconds == null) {
            return null;
        }
        return uptimeSeconds / 86400.0;
    }

    /**
     * Get memory available (MB)
     */
    public Long getMemoryAvailableMB() {
        if (memoryTotalMB == null || memoryUsedMB == null) {
            return null;
        }
        return memoryTotalMB - memoryUsedMB;
    }

    /**
     * Get disk available (GB)
     */
    public Long getDiskAvailableGB() {
        if (diskTotalGB == null || diskUsedGB == null) {
            return null;
        }
        return diskTotalGB - diskUsedGB;
    }

    /**
     * Add health check result
     */
    public void addHealthCheckResult(HealthCheck check) {
        if (healthChecks == null) {
            healthChecks = new java.util.ArrayList<>();
        }
        healthChecks.add(check);

        totalChecks = (totalChecks != null ? totalChecks : 0) + 1;

        if (Boolean.TRUE.equals(check.getPassed())) {
            passedChecks = (passedChecks != null ? passedChecks : 0) + 1;
        } else {
            failedChecks = (failedChecks != null ? failedChecks : 0) + 1;
        }

        calculateCheckSuccessRate();
    }

    /**
     * Add alert
     */
    public void addAlert(Alert alert) {
        if (activeAlerts == null) {
            activeAlerts = new java.util.ArrayList<>();
        }
        activeAlerts.add(alert);

        alertCount = (alertCount != null ? alertCount : 0) + 1;
        lastAlert = alert.getTriggeredAt();

        if (alert.getSeverity() == AlertSeverity.CRITICAL) {
            criticalAlerts = (criticalAlerts != null ? criticalAlerts : 0) + 1;
        }
    }

    /**
     * Record error
     */
    public void recordError(String error) {
        totalErrors = (totalErrors != null ? totalErrors : 0) + 1;
        lastError = error;
        lastErrorAt = LocalDateTime.now();

        // Count errors in time windows
        if (lastErrorAt.isAfter(LocalDateTime.now().minusHours(1))) {
            errorsLastHour = (errorsLastHour != null ? errorsLastHour : 0) + 1;
        }

        if (lastErrorAt.isAfter(LocalDateTime.now().minusHours(24))) {
            errorsLast24Hours = (errorsLast24Hours != null ? errorsLast24Hours : 0) + 1;
        }
    }

    /**
     * Get health score
     */
    public int getHealthScore() {
        int score = 100;

        if (cpuUsagePercent != null && cpuUsagePercent > 80) score -= 20;
        if (memoryUsagePercent != null && memoryUsagePercent > 80) score -= 20;
        if (errorRate != null && errorRate > 5) score -= 20;
        if (checkSuccessRate != null && checkSuccessRate < 90) score -= 20;
        if (criticalAlerts != null && criticalAlerts > 0) score -= 20;

        return Math.max(0, score);
    }
}
