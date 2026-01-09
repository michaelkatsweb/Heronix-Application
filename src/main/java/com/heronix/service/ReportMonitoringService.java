package com.heronix.service;

import com.heronix.dto.ReportMonitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Report Monitoring Service
 *
 * Manages system monitoring and health checks for reports.
 *
 * Features:
 * - Health check execution
 * - Performance metric collection
 * - Resource utilization monitoring
 * - Alert management
 * - Uptime tracking
 * - Trend analysis
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 81 - Report Monitoring & Health Checks
 */
@Service
@Slf4j
public class ReportMonitoringService {

    private final Map<Long, ReportMonitor> monitors = new ConcurrentHashMap<>();
    private final Map<Long, List<ReportMonitor.HistoricalData>> historicalDataStore = new ConcurrentHashMap<>();
    private Long nextMonitorId = 1L;

    /**
     * Create monitor
     */
    public ReportMonitor createMonitor(ReportMonitor monitor) {
        synchronized (this) {
            monitor.setMonitorId(nextMonitorId++);
            monitor.setCreatedAt(LocalDateTime.now());
            monitor.setHealthStatus(ReportMonitor.HealthStatus.UNKNOWN);
            monitor.setStartedAt(LocalDateTime.now());
            monitor.setUptimeSeconds(0L);
            monitor.setTotalChecks(0);
            monitor.setPassedChecks(0);
            monitor.setFailedChecks(0);
            monitor.setTotalRequests(0L);
            monitor.setSuccessfulRequests(0L);
            monitor.setFailedRequests(0L);
            monitor.setTotalErrors(0L);
            monitor.setDowntimeEvents(0);
            monitor.setTotalDowntimeMinutes(0L);
            monitor.setAlertCount(0);
            monitor.setCriticalAlerts(0);

            // Set defaults
            if (monitor.getHealthChecksEnabled() == null) {
                monitor.setHealthChecksEnabled(true);
            }

            if (monitor.getCheckIntervalSeconds() == null) {
                monitor.setCheckIntervalSeconds(60);
            }

            if (monitor.getTimeoutSeconds() == null) {
                monitor.setTimeoutSeconds(30);
            }

            // Calculate initial health status
            monitor.calculateHealthStatus();

            monitors.put(monitor.getMonitorId(), monitor);

            log.info("Created monitor {} for component {}",
                    monitor.getMonitorId(), monitor.getComponentName());

            return monitor;
        }
    }

    /**
     * Get monitor
     */
    public Optional<ReportMonitor> getMonitor(Long monitorId) {
        return Optional.ofNullable(monitors.get(monitorId));
    }

    /**
     * Get monitors by component
     */
    public List<ReportMonitor> getMonitorsByComponent(String componentName) {
        return monitors.values().stream()
                .filter(m -> componentName.equals(m.getComponentName()))
                .collect(Collectors.toList());
    }

    /**
     * Get monitors by health status
     */
    public List<ReportMonitor> getMonitorsByHealthStatus(ReportMonitor.HealthStatus status) {
        return monitors.values().stream()
                .filter(m -> m.getHealthStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * Execute health check
     */
    public ReportMonitor.HealthCheck executeHealthCheck(Long monitorId, ReportMonitor.CheckType checkType) {
        ReportMonitor monitor = monitors.get(monitorId);
        if (monitor == null) {
            throw new IllegalArgumentException("Monitor not found: " + monitorId);
        }

        LocalDateTime startTime = LocalDateTime.now();

        ReportMonitor.HealthCheck check = ReportMonitor.HealthCheck.builder()
                .checkId(UUID.randomUUID().toString())
                .checkName(checkType.name())
                .checkType(checkType)
                .checkedAt(startTime)
                .build();

        try {
            // Execute check based on type
            boolean passed = performHealthCheck(monitor, checkType, check);

            check.setPassed(passed);
            check.setStatus(passed ? ReportMonitor.HealthStatus.HEALTHY : ReportMonitor.HealthStatus.UNHEALTHY);
            check.setMessage(passed ? "Check passed" : "Check failed");
            check.setResponseTimeMs(
                    java.time.Duration.between(startTime, LocalDateTime.now()).toMillis());

            log.info("Health check {} for monitor {}: {}",
                    checkType, monitorId, passed ? "PASSED" : "FAILED");

        } catch (Exception e) {
            check.setPassed(false);
            check.setStatus(ReportMonitor.HealthStatus.UNHEALTHY);
            check.setMessage("Check error: " + e.getMessage());
            check.setResponseTimeMs(
                    java.time.Duration.between(startTime, LocalDateTime.now()).toMillis());

            log.error("Health check {} failed for monitor {}", checkType, monitorId, e);
        }

        // Add check result to monitor
        monitor.addHealthCheckResult(check);
        monitor.setLastCheckAt(LocalDateTime.now());

        if (monitor.getCheckIntervalSeconds() != null) {
            monitor.setNextCheckAt(LocalDateTime.now().plusSeconds(monitor.getCheckIntervalSeconds()));
        }

        // Update health status
        monitor.calculateHealthStatus();

        return check;
    }

    /**
     * Perform health check
     */
    private boolean performHealthCheck(ReportMonitor monitor, ReportMonitor.CheckType checkType,
                                       ReportMonitor.HealthCheck check) {
        return switch (checkType) {
            case LIVENESS -> performLivenessCheck(monitor, check);
            case READINESS -> performReadinessCheck(monitor, check);
            case DATABASE -> performDatabaseCheck(monitor, check);
            case CACHE -> performCacheCheck(monitor, check);
            case DISK -> performDiskCheck(monitor, check);
            case MEMORY -> performMemoryCheck(monitor, check);
            case CPU -> performCPUCheck(monitor, check);
            case EXTERNAL_SERVICE -> performExternalServiceCheck(monitor, check);
            case CUSTOM -> performCustomCheck(monitor, check);
        };
    }

    /**
     * Perform liveness check
     */
    private boolean performLivenessCheck(ReportMonitor monitor, ReportMonitor.HealthCheck check) {
        // Check if system is running
        boolean alive = monitor.getStartedAt() != null;

        Map<String, Object> checkData = new HashMap<>();
        checkData.put("startedAt", monitor.getStartedAt());
        checkData.put("uptime", monitor.getUptimeSeconds());
        check.setCheckData(checkData);

        return alive;
    }

    /**
     * Perform readiness check
     */
    private boolean performReadinessCheck(ReportMonitor monitor, ReportMonitor.HealthCheck check) {
        // Check if system is ready to serve requests
        boolean ready = monitor.isHealthy() &&
                       Boolean.TRUE.equals(monitor.getDependenciesHealthy());

        Map<String, Object> checkData = new HashMap<>();
        checkData.put("healthStatus", monitor.getHealthStatus());
        checkData.put("dependenciesHealthy", monitor.getDependenciesHealthy());
        check.setCheckData(checkData);

        return ready;
    }

    /**
     * Perform database check
     */
    private boolean performDatabaseCheck(ReportMonitor monitor, ReportMonitor.HealthCheck check) {
        // Simulate database connectivity check
        String dbStatus = monitor.getDatabaseStatus();
        boolean healthy = "CONNECTED".equals(dbStatus) || "HEALTHY".equals(dbStatus);

        Map<String, Object> checkData = new HashMap<>();
        checkData.put("databaseStatus", dbStatus);
        checkData.put("connectionPoolSize", monitor.getDbConnectionPoolSize());
        check.setCheckData(checkData);

        return healthy;
    }

    /**
     * Perform cache check
     */
    private boolean performCacheCheck(ReportMonitor monitor, ReportMonitor.HealthCheck check) {
        // Simulate cache health check
        String cacheStatus = monitor.getCacheStatus();
        boolean healthy = "CONNECTED".equals(cacheStatus) || "HEALTHY".equals(cacheStatus);

        Map<String, Object> checkData = new HashMap<>();
        checkData.put("cacheStatus", cacheStatus);
        checkData.put("cacheHitRate", monitor.getCacheHitRate());
        check.setCheckData(checkData);

        return healthy;
    }

    /**
     * Perform disk check
     */
    private boolean performDiskCheck(ReportMonitor monitor, ReportMonitor.HealthCheck check) {
        Double diskUsage = monitor.getDiskUsagePercent();
        boolean healthy = diskUsage == null || diskUsage < 90;

        Map<String, Object> checkData = new HashMap<>();
        checkData.put("diskUsagePercent", diskUsage);
        checkData.put("diskUsedGB", monitor.getDiskUsedGB());
        checkData.put("diskTotalGB", monitor.getDiskTotalGB());
        check.setCheckData(checkData);

        return healthy;
    }

    /**
     * Perform memory check
     */
    private boolean performMemoryCheck(ReportMonitor monitor, ReportMonitor.HealthCheck check) {
        Double memoryUsage = monitor.getMemoryUsagePercent();
        boolean healthy = memoryUsage == null || memoryUsage < 90;

        Map<String, Object> checkData = new HashMap<>();
        checkData.put("memoryUsagePercent", memoryUsage);
        checkData.put("memoryUsedMB", monitor.getMemoryUsedMB());
        checkData.put("memoryTotalMB", monitor.getMemoryTotalMB());
        check.setCheckData(checkData);

        return healthy;
    }

    /**
     * Perform CPU check
     */
    private boolean performCPUCheck(ReportMonitor monitor, ReportMonitor.HealthCheck check) {
        Double cpuUsage = monitor.getCpuUsagePercent();
        boolean healthy = cpuUsage == null || cpuUsage < 90;

        Map<String, Object> checkData = new HashMap<>();
        checkData.put("cpuUsagePercent", cpuUsage);
        checkData.put("threadCount", monitor.getThreadCount());
        check.setCheckData(checkData);

        return healthy;
    }

    /**
     * Perform external service check
     */
    private boolean performExternalServiceCheck(ReportMonitor monitor, ReportMonitor.HealthCheck check) {
        Map<String, String> externalServices = monitor.getExternalServicesStatus();
        boolean healthy = externalServices == null || externalServices.values().stream()
                .allMatch(status -> "HEALTHY".equals(status) || "UP".equals(status));

        Map<String, Object> checkData = new HashMap<>();
        checkData.put("externalServicesStatus", externalServices);
        check.setCheckData(checkData);

        return healthy;
    }

    /**
     * Perform custom check
     */
    private boolean performCustomCheck(ReportMonitor monitor, ReportMonitor.HealthCheck check) {
        // Simulate custom check
        Map<String, Object> checkData = new HashMap<>();
        checkData.put("customCheckExecuted", true);
        check.setCheckData(checkData);

        return true;
    }

    /**
     * Update performance metrics
     */
    public void updatePerformanceMetrics(Long monitorId, long responseTimeMs, boolean success) {
        ReportMonitor monitor = monitors.get(monitorId);
        if (monitor == null) {
            return;
        }

        // Update request counts
        monitor.setTotalRequests((monitor.getTotalRequests() != null ? monitor.getTotalRequests() : 0) + 1);

        if (success) {
            monitor.setSuccessfulRequests(
                    (monitor.getSuccessfulRequests() != null ? monitor.getSuccessfulRequests() : 0) + 1);
        } else {
            monitor.setFailedRequests(
                    (monitor.getFailedRequests() != null ? monitor.getFailedRequests() : 0) + 1);
        }

        // Update response time
        monitor.setResponseTimeMs(responseTimeMs);

        if (monitor.getMinResponseTimeMs() == null || responseTimeMs < monitor.getMinResponseTimeMs()) {
            monitor.setMinResponseTimeMs(responseTimeMs);
        }

        if (monitor.getMaxResponseTimeMs() == null || responseTimeMs > monitor.getMaxResponseTimeMs()) {
            monitor.setMaxResponseTimeMs(responseTimeMs);
        }

        // Calculate average response time
        if (monitor.getAverageResponseTimeMs() == null) {
            monitor.setAverageResponseTimeMs((double) responseTimeMs);
        } else {
            double currentAvg = monitor.getAverageResponseTimeMs();
            long totalRequests = monitor.getTotalRequests();
            monitor.setAverageResponseTimeMs(
                    (currentAvg * (totalRequests - 1) + responseTimeMs) / totalRequests);
        }

        // Calculate success rate
        monitor.calculateSuccessRate();

        // Update health status
        monitor.calculateHealthStatus();

        log.debug("Updated performance metrics for monitor {}: {} ms, success={}",
                monitorId, responseTimeMs, success);
    }

    /**
     * Update resource utilization
     */
    public void updateResourceUtilization(Long monitorId, Double cpuUsage, Double memoryUsage,
                                         Double diskUsage) {
        ReportMonitor monitor = monitors.get(monitorId);
        if (monitor == null) {
            return;
        }

        if (cpuUsage != null) {
            monitor.setCpuUsagePercent(cpuUsage);
        }

        if (memoryUsage != null) {
            monitor.setMemoryUsagePercent(memoryUsage);
        }

        if (diskUsage != null) {
            monitor.setDiskUsagePercent(diskUsage);
        }

        monitor.setUpdatedAt(LocalDateTime.now());
        monitor.calculateHealthStatus();

        log.debug("Updated resource utilization for monitor {}: CPU={}%, Memory={}%, Disk={}%",
                monitorId, cpuUsage, memoryUsage, diskUsage);
    }

    /**
     * Update uptime
     */
    public void updateUptime(Long monitorId) {
        ReportMonitor monitor = monitors.get(monitorId);
        if (monitor == null || monitor.getStartedAt() == null) {
            return;
        }

        long uptimeSeconds = java.time.Duration.between(monitor.getStartedAt(), LocalDateTime.now()).toSeconds();
        monitor.setUptimeSeconds(uptimeSeconds);
        monitor.calculateAvailability();
    }

    /**
     * Record error
     */
    public void recordError(Long monitorId, String error) {
        ReportMonitor monitor = monitors.get(monitorId);
        if (monitor == null) {
            return;
        }

        monitor.recordError(error);

        // Create alert if critical
        if (monitor.getCriticalErrors() != null && monitor.getCriticalErrors() > 0) {
            createAlert(monitorId, "Critical error detected",
                    ReportMonitor.AlertSeverity.CRITICAL, error);
        }

        log.warn("Recorded error for monitor {}: {}", monitorId, error);
    }

    /**
     * Create alert
     */
    public void createAlert(Long monitorId, String alertName,
                           ReportMonitor.AlertSeverity severity, String message) {
        ReportMonitor monitor = monitors.get(monitorId);
        if (monitor == null) {
            return;
        }

        ReportMonitor.Alert alert = ReportMonitor.Alert.builder()
                .alertId(UUID.randomUUID().toString())
                .alertName(alertName)
                .severity(severity)
                .message(message)
                .triggeredAt(LocalDateTime.now())
                .acknowledged(false)
                .resolved(false)
                .build();

        monitor.addAlert(alert);

        log.warn("Alert created for monitor {}: {} ({})", monitorId, alertName, severity);
    }

    /**
     * Acknowledge alert
     */
    public void acknowledgeAlert(Long monitorId, String alertId, String acknowledgedBy) {
        ReportMonitor monitor = monitors.get(monitorId);
        if (monitor == null) {
            return;
        }

        if (monitor.getActiveAlerts() == null) {
            return;
        }

        monitor.getActiveAlerts().stream()
                .filter(a -> a.getAlertId().equals(alertId))
                .findFirst()
                .ifPresent(alert -> {
                    alert.setAcknowledged(true);
                    alert.setAcknowledgedAt(LocalDateTime.now());
                    alert.setAcknowledgedBy(acknowledgedBy);

                    log.info("Alert {} acknowledged by {} for monitor {}",
                            alertId, acknowledgedBy, monitorId);
                });
    }

    /**
     * Resolve alert
     */
    public void resolveAlert(Long monitorId, String alertId) {
        ReportMonitor monitor = monitors.get(monitorId);
        if (monitor == null) {
            return;
        }

        if (monitor.getActiveAlerts() == null) {
            return;
        }

        monitor.getActiveAlerts().stream()
                .filter(a -> a.getAlertId().equals(alertId))
                .findFirst()
                .ifPresent(alert -> {
                    alert.setResolved(true);
                    alert.setResolvedAt(LocalDateTime.now());

                    log.info("Alert {} resolved for monitor {}", alertId, monitorId);
                });
    }

    /**
     * Record historical data
     */
    public void recordHistoricalData(Long monitorId) {
        ReportMonitor monitor = monitors.get(monitorId);
        if (monitor == null) {
            return;
        }

        ReportMonitor.HistoricalData data = ReportMonitor.HistoricalData.builder()
                .timestamp(LocalDateTime.now())
                .responseTime(monitor.getAverageResponseTimeMs())
                .cpuUsage(monitor.getCpuUsagePercent())
                .memoryUsage(monitor.getMemoryUsagePercent())
                .errorCount(monitor.getErrorsLastHour())
                .throughput(monitor.getThroughput())
                .build();

        List<ReportMonitor.HistoricalData> history =
                historicalDataStore.computeIfAbsent(monitorId, k -> new ArrayList<>());
        history.add(data);

        // Keep only last 1000 data points
        if (history.size() > 1000) {
            history.remove(0);
        }

        log.debug("Recorded historical data for monitor {}", monitorId);
    }

    /**
     * Get historical data
     */
    public List<ReportMonitor.HistoricalData> getHistoricalData(Long monitorId) {
        return historicalDataStore.getOrDefault(monitorId, new ArrayList<>());
    }

    /**
     * Get active alerts
     */
    public List<ReportMonitor.Alert> getActiveAlerts(Long monitorId) {
        ReportMonitor monitor = monitors.get(monitorId);
        if (monitor == null || monitor.getActiveAlerts() == null) {
            return new ArrayList<>();
        }

        return monitor.getActiveAlerts().stream()
                .filter(a -> !Boolean.TRUE.equals(a.getResolved()))
                .collect(Collectors.toList());
    }

    /**
     * Get monitors needing attention
     */
    public List<ReportMonitor> getMonitorsNeedingAttention() {
        return monitors.values().stream()
                .filter(ReportMonitor::needsAttention)
                .collect(Collectors.toList());
    }

    /**
     * Get degraded monitors
     */
    public List<ReportMonitor> getDegradedMonitors() {
        return monitors.values().stream()
                .filter(ReportMonitor::isDegraded)
                .collect(Collectors.toList());
    }

    /**
     * Get unhealthy monitors
     */
    public List<ReportMonitor> getUnhealthyMonitors() {
        return monitors.values().stream()
                .filter(ReportMonitor::isUnhealthy)
                .collect(Collectors.toList());
    }

    /**
     * Delete monitor
     */
    public void deleteMonitor(Long monitorId) {
        ReportMonitor removed = monitors.remove(monitorId);
        if (removed != null) {
            historicalDataStore.remove(monitorId);
            log.info("Deleted monitor {}", monitorId);
        }
    }

    /**
     * Get statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalMonitors", monitors.size());
        stats.put("healthyMonitors", getMonitorsByHealthStatus(ReportMonitor.HealthStatus.HEALTHY).size());
        stats.put("degradedMonitors", getDegradedMonitors().size());
        stats.put("unhealthyMonitors", getUnhealthyMonitors().size());
        stats.put("monitorsNeedingAttention", getMonitorsNeedingAttention().size());

        long totalAlerts = monitors.values().stream()
                .filter(m -> m.getAlertCount() != null)
                .mapToLong(ReportMonitor::getAlertCount)
                .sum();

        long criticalAlerts = monitors.values().stream()
                .filter(m -> m.getCriticalAlerts() != null)
                .mapToLong(ReportMonitor::getCriticalAlerts)
                .sum();

        stats.put("totalAlerts", totalAlerts);
        stats.put("criticalAlerts", criticalAlerts);

        double avgUptime = monitors.values().stream()
                .filter(m -> m.getUptimeSeconds() != null)
                .mapToLong(ReportMonitor::getUptimeSeconds)
                .average()
                .orElse(0.0);

        stats.put("averageUptimeSeconds", avgUptime);

        double avgAvailability = monitors.values().stream()
                .filter(m -> m.getAvailabilityPercent() != null)
                .mapToDouble(ReportMonitor::getAvailabilityPercent)
                .average()
                .orElse(0.0);

        stats.put("averageAvailability", avgAvailability);

        return stats;
    }
}
