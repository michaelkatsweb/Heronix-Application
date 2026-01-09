package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Observability DTO
 *
 * Represents observability configuration, distributed tracing, metrics collection,
 * log aggregation, and telemetry data for comprehensive system monitoring.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 144 - Observability & Telemetry
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportObservability {

    private Long observabilityId;
    private String observabilityName;
    private String description;

    // Observability Configuration
    private String observabilityPlatform; // OPENTELEMETRY, PROMETHEUS, DATADOG, NEW_RELIC, DYNATRACE
    private Boolean metricsEnabled;
    private Boolean tracingEnabled;
    private Boolean loggingEnabled;
    private Map<String, Object> platformConfig;

    // Metrics Collection
    private String metricsBackend; // PROMETHEUS, INFLUXDB, GRAPHITE, TIMESCALEDB
    private Integer metricsRetentionDays;
    private Integer scrapingIntervalSeconds;
    private Long totalMetricsCollected;
    private Long activeTimeSeries;
    private List<String> metricTypes; // COUNTER, GAUGE, HISTOGRAM, SUMMARY
    private Map<String, Object> metricsConfig;

    // Distributed Tracing
    private String tracingBackend; // JAEGER, ZIPKIN, TEMPO, X_RAY, LIGHTSTEP
    private Double samplingRate; // 0.0 to 1.0
    private Long totalTraces;
    private Long totalSpans;
    private Integer averageSpansPerTrace;
    private Double averageTraceLatency; // milliseconds
    private Map<String, Object> tracingConfig;

    // Log Aggregation
    private String loggingBackend; // ELASTICSEARCH, LOKI, SPLUNK, CLOUDWATCH
    private String logLevel; // TRACE, DEBUG, INFO, WARN, ERROR, FATAL
    private Long totalLogsCollected;
    private Long errorLogsCount;
    private Long warnLogsCount;
    private Long infoLogsCount;
    private Boolean structuredLoggingEnabled;
    private Map<String, Object> loggingConfig;

    // Service Mesh Observability
    private Boolean serviceMeshEnabled;
    private String serviceMeshType; // ISTIO, LINKERD, CONSUL, ENVOY
    private Integer servicesMonitored;
    private Long totalRequests;
    private Double requestSuccessRate;
    private Double averageLatency; // milliseconds
    private Double p50Latency;
    private Double p95Latency;
    private Double p99Latency;
    private Map<String, Object> serviceMeshConfig;

    // APM (Application Performance Monitoring)
    private Boolean apmEnabled;
    private String apmTool; // NEW_RELIC, DATADOG_APM, DYNATRACE, ELASTIC_APM
    private Double apdexScore; // 0.0 to 1.0
    private Double errorRate; // percentage
    private Double throughput; // requests per second
    private Integer transactionsTracked;
    private Map<String, Object> apmConfig;

    // Real User Monitoring (RUM)
    private Boolean rumEnabled;
    private Long pageViews;
    private Double averagePageLoadTime; // milliseconds
    private Double bounceRate; // percentage
    private Integer userSessions;
    private Map<String, String> geographicDistribution;
    private Map<String, Object> rumConfig;

    // Synthetic Monitoring
    private Boolean syntheticMonitoringEnabled;
    private Integer syntheticChecks;
    private Integer successfulChecks;
    private Integer failedChecks;
    private Double uptimePercentage;
    private List<String> monitoredEndpoints;
    private Map<String, Object> syntheticConfig;

    // Alerting
    private Boolean alertingEnabled;
    private String alertingSystem; // PROMETHEUS_ALERTMANAGER, PAGERDUTY, OPSGENIE, VICTOROPS
    private Integer totalAlerts;
    private Integer criticalAlerts;
    private Integer warningAlerts;
    private Integer resolvedAlerts;
    private Double meanTimeToAlert; // minutes
    private List<Map<String, Object>> activeAlerts;
    private Map<String, Object> alertingConfig;

    // Dashboards
    private Boolean dashboardsEnabled;
    private String dashboardPlatform; // GRAFANA, KIBANA, DATADOG, NEW_RELIC
    private Integer totalDashboards;
    private Integer activeDashboards;
    private List<String> dashboardNames;
    private Map<String, Object> dashboardConfig;

    // Service Level Objectives (SLO)
    private Boolean sloEnabled;
    private List<Map<String, Object>> slos;
    private Double currentSloCompliance; // percentage
    private Double errorBudgetRemaining; // percentage
    private Integer sloBreach;
    private Map<String, Object> sloConfig;

    // Service Level Indicators (SLI)
    private Boolean sliEnabled;
    private Map<String, Double> sliMetrics;
    private Double availabilitySli; // percentage
    private Double latencySli; // milliseconds
    private Double errorRateSli; // percentage
    private Map<String, Object> sliConfig;

    // Infrastructure Monitoring
    private Boolean infraMonitoringEnabled;
    private Integer hostsMonitored;
    private Integer containersMonitored;
    private Double avgCpuUsage; // percentage
    private Double avgMemoryUsage; // percentage
    private Double avgDiskUsage; // percentage
    private Map<String, Object> infraConfig;

    // Network Monitoring
    private Boolean networkMonitoringEnabled;
    private Long networkBytesIn;
    private Long networkBytesOut;
    private Integer activeConnections;
    private Double networkLatency; // milliseconds
    private Double packetLossRate; // percentage
    private Map<String, Object> networkConfig;

    // Database Monitoring
    private Boolean dbMonitoringEnabled;
    private Integer databasesMonitored;
    private Long totalQueries;
    private Double avgQueryTime; // milliseconds
    private Double slowQueryRate; // percentage
    private Integer connectionPoolSize;
    private Map<String, Object> dbConfig;

    // Custom Metrics
    private Boolean customMetricsEnabled;
    private Integer customMetricsCount;
    private Map<String, Object> customMetrics;
    private Map<String, Object> businessMetrics;

    // Correlation & Context
    private Boolean correlationEnabled;
    private String correlationId;
    private Map<String, String> contextTags;
    private Map<String, Object> traceContext;

    // Data Retention
    private Integer metricsRetention; // days
    private Integer tracesRetention; // days
    private Integer logsRetention; // days
    private Long storageUsed; // bytes
    private Map<String, Object> retentionPolicy;

    // Cardinality
    private Long metricCardinality;
    private Long labelCardinality;
    private Boolean highCardinalityDetected;
    private List<String> highCardinalityMetrics;

    // Sampling
    private Boolean adaptiveSamplingEnabled;
    private Double headSamplingRate;
    private Double tailSamplingRate;
    private Map<String, Object> samplingConfig;

    // Exporters
    private Boolean exportersEnabled;
    private List<String> configuredExporters;
    private Long exportedDataPoints;
    private Map<String, Object> exporterConfig;

    // Health Checks
    private Boolean healthChecksEnabled;
    private Integer healthEndpoints;
    private Integer healthyEndpoints;
    private Integer unhealthyEndpoints;
    private Map<String, String> healthStatus;

    // Performance
    private Double observabilityOverhead; // percentage
    private Long telemetryBytesPerSecond;
    private Integer batchSize;
    private Integer flushIntervalSeconds;

    // Status
    private String observabilityStatus; // INITIALIZING, COLLECTING, ACTIVE, DEGRADED, OFFLINE
    private LocalDateTime activatedAt;
    private LocalDateTime lastCollection;
    private LocalDateTime lastExport;

    // Metadata
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Object> metadata;

    // Helper Methods

    /**
     * Record metric collection
     */
    public void recordMetricCollection(long count) {
        this.totalMetricsCollected = (this.totalMetricsCollected != null ? this.totalMetricsCollected : 0L) + count;
        this.lastCollection = LocalDateTime.now();
    }

    /**
     * Record trace
     */
    public void recordTrace(int spanCount, double latency) {
        this.totalTraces = (this.totalTraces != null ? this.totalTraces : 0L) + 1L;
        this.totalSpans = (this.totalSpans != null ? this.totalSpans : 0L) + spanCount;

        // Update average spans per trace
        if (this.totalTraces > 0) {
            this.averageSpansPerTrace = (int) (this.totalSpans / this.totalTraces);
        }

        // Update average trace latency
        if (this.averageTraceLatency == null) {
            this.averageTraceLatency = latency;
        } else {
            this.averageTraceLatency = (this.averageTraceLatency + latency) / 2.0;
        }
    }

    /**
     * Record logs
     */
    public void recordLogs(String level, long count) {
        this.totalLogsCollected = (this.totalLogsCollected != null ? this.totalLogsCollected : 0L) + count;

        switch (level.toUpperCase()) {
            case "ERROR":
                this.errorLogsCount = (this.errorLogsCount != null ? this.errorLogsCount : 0L) + count;
                break;
            case "WARN":
                this.warnLogsCount = (this.warnLogsCount != null ? this.warnLogsCount : 0L) + count;
                break;
            case "INFO":
                this.infoLogsCount = (this.infoLogsCount != null ? this.infoLogsCount : 0L) + count;
                break;
        }
    }

    /**
     * Record alert
     */
    public void recordAlert(String severity) {
        this.totalAlerts = (this.totalAlerts != null ? this.totalAlerts : 0) + 1;

        if ("CRITICAL".equalsIgnoreCase(severity)) {
            this.criticalAlerts = (this.criticalAlerts != null ? this.criticalAlerts : 0) + 1;
        } else if ("WARNING".equalsIgnoreCase(severity)) {
            this.warningAlerts = (this.warningAlerts != null ? this.warningAlerts : 0) + 1;
        }
    }

    /**
     * Resolve alert
     */
    public void resolveAlert() {
        this.resolvedAlerts = (this.resolvedAlerts != null ? this.resolvedAlerts : 0) + 1;
    }

    /**
     * Update SLO compliance
     */
    public void updateSloCompliance() {
        if (this.slos != null && !this.slos.isEmpty()) {
            double totalCompliance = 0.0;
            int count = 0;

            for (Map<String, Object> slo : this.slos) {
                Object compliance = slo.get("compliance");
                if (compliance instanceof Number) {
                    totalCompliance += ((Number) compliance).doubleValue();
                    count++;
                }
            }

            if (count > 0) {
                this.currentSloCompliance = totalCompliance / count;
            }
        }
    }

    /**
     * Calculate error budget
     */
    public Double calculateErrorBudget() {
        if (this.currentSloCompliance != null) {
            this.errorBudgetRemaining = 100.0 - this.currentSloCompliance;
        } else {
            this.errorBudgetRemaining = 100.0;
        }
        return this.errorBudgetRemaining;
    }

    /**
     * Check if SLO is breached
     */
    public boolean isSloBreached() {
        return this.currentSloCompliance != null && this.currentSloCompliance < 99.0;
    }

    /**
     * Check if healthy
     */
    public boolean isHealthy() {
        return "ACTIVE".equals(this.observabilityStatus) &&
               this.errorRate != null && this.errorRate < 1.0 &&
               this.uptimePercentage != null && this.uptimePercentage >= 99.0;
    }

    /**
     * Get observability score
     */
    public Double getObservabilityScore() {
        double score = 0.0;

        if (Boolean.TRUE.equals(this.metricsEnabled)) score += 25.0;
        if (Boolean.TRUE.equals(this.tracingEnabled)) score += 25.0;
        if (Boolean.TRUE.equals(this.loggingEnabled)) score += 25.0;
        if (Boolean.TRUE.equals(this.alertingEnabled)) score += 15.0;
        if (Boolean.TRUE.equals(this.sloEnabled)) score += 10.0;

        return score;
    }

    /**
     * Check high cardinality
     */
    public boolean hasHighCardinality() {
        return Boolean.TRUE.equals(this.highCardinalityDetected) ||
               (this.metricCardinality != null && this.metricCardinality > 100000);
    }
}
