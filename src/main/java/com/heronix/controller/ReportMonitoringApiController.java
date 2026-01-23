package com.heronix.controller;

import com.heronix.dto.ReportMonitor;
import com.heronix.service.ReportMonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Monitoring API Controller
 *
 * REST API endpoints for monitoring and health check management.
 *
 * Endpoints:
 * - POST /api/monitoring - Create monitor
 * - GET /api/monitoring/{id} - Get monitor
 * - GET /api/monitoring/component/{componentName} - Get monitors by component
 * - GET /api/monitoring/status/{status} - Get monitors by health status
 * - POST /api/monitoring/{id}/check/{checkType} - Execute health check
 * - POST /api/monitoring/{id}/metrics - Update performance metrics
 * - POST /api/monitoring/{id}/resources - Update resource utilization
 * - POST /api/monitoring/{id}/uptime - Update uptime
 * - POST /api/monitoring/{id}/error - Record error
 * - POST /api/monitoring/{id}/alert - Create alert
 * - POST /api/monitoring/{id}/alert/{alertId}/acknowledge - Acknowledge alert
 * - POST /api/monitoring/{id}/alert/{alertId}/resolve - Resolve alert
 * - GET /api/monitoring/{id}/alerts - Get active alerts
 * - GET /api/monitoring/{id}/history - Get historical data
 * - POST /api/monitoring/{id}/record-history - Record historical data
 * - GET /api/monitoring/needs-attention - Get monitors needing attention
 * - GET /api/monitoring/degraded - Get degraded monitors
 * - GET /api/monitoring/unhealthy - Get unhealthy monitors
 * - DELETE /api/monitoring/{id} - Delete monitor
 * - GET /api/monitoring/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 81 - Report Monitoring & Health Checks
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
@Slf4j
public class ReportMonitoringApiController {

    private final ReportMonitoringService monitoringService;

    /**
     * Create monitor
     */
    @PostMapping
    public ResponseEntity<ReportMonitor> createMonitor(@RequestBody ReportMonitor monitor) {
        log.info("POST /api/monitoring - Creating monitor for component {}", monitor.getComponentName());

        try {
            ReportMonitor created = monitoringService.createMonitor(monitor);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating monitor", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get monitor
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportMonitor> getMonitor(@PathVariable Long id) {
        log.info("GET /api/monitoring/{}", id);

        try {
            return monitoringService.getMonitor(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching monitor: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get monitors by component
     */
    @GetMapping("/component/{componentName}")
    public ResponseEntity<List<ReportMonitor>> getMonitorsByComponent(@PathVariable String componentName) {
        log.info("GET /api/monitoring/component/{}", componentName);

        try {
            List<ReportMonitor> monitors = monitoringService.getMonitorsByComponent(componentName);
            return ResponseEntity.ok(monitors);

        } catch (Exception e) {
            log.error("Error fetching monitors for component: {}", componentName, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get monitors by health status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReportMonitor>> getMonitorsByHealthStatus(
            @PathVariable ReportMonitor.HealthStatus status) {
        log.info("GET /api/monitoring/status/{}", status);

        try {
            List<ReportMonitor> monitors = monitoringService.getMonitorsByHealthStatus(status);
            return ResponseEntity.ok(monitors);

        } catch (Exception e) {
            log.error("Error fetching monitors by status: {}", status, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Execute health check
     */
    @PostMapping("/{id}/check/{checkType}")
    public ResponseEntity<ReportMonitor.HealthCheck> executeHealthCheck(
            @PathVariable Long id,
            @PathVariable ReportMonitor.CheckType checkType) {
        log.info("POST /api/monitoring/{}/check/{}", id, checkType);

        try {
            ReportMonitor.HealthCheck check = monitoringService.executeHealthCheck(id, checkType);
            return ResponseEntity.ok(check);

        } catch (IllegalArgumentException e) {
            log.error("Monitor not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error executing health check: {} for monitor {}", checkType, id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update performance metrics
     */
    @PostMapping("/{id}/metrics")
    public ResponseEntity<Map<String, Object>> updatePerformanceMetrics(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/monitoring/{}/metrics", id);

        try {
            Number responseTimeMsObj = (Number) request.get("responseTimeMs");
            long responseTimeMs = responseTimeMsObj != null ? responseTimeMsObj.longValue() : 0;
            boolean success = request.get("success") != null ?
                    (Boolean) request.get("success") : true;

            monitoringService.updatePerformanceMetrics(id, responseTimeMs, success);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Performance metrics updated");
            response.put("monitorId", id);
            response.put("responseTimeMs", responseTimeMs);
            response.put("success", success);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error updating performance metrics for monitor: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update resource utilization
     */
    @PostMapping("/{id}/resources")
    public ResponseEntity<Map<String, Object>> updateResourceUtilization(
            @PathVariable Long id,
            @RequestBody Map<String, Double> request) {
        log.info("POST /api/monitoring/{}/resources", id);

        try {
            Double cpuUsage = request.get("cpuUsage");
            Double memoryUsage = request.get("memoryUsage");
            Double diskUsage = request.get("diskUsage");

            monitoringService.updateResourceUtilization(id, cpuUsage, memoryUsage, diskUsage);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Resource utilization updated");
            response.put("monitorId", id);
            response.put("cpuUsage", cpuUsage);
            response.put("memoryUsage", memoryUsage);
            response.put("diskUsage", diskUsage);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error updating resource utilization for monitor: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update uptime
     */
    @PostMapping("/{id}/uptime")
    public ResponseEntity<Map<String, Object>> updateUptime(@PathVariable Long id) {
        log.info("POST /api/monitoring/{}/uptime", id);

        try {
            monitoringService.updateUptime(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Uptime updated");
            response.put("monitorId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error updating uptime for monitor: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Record error
     */
    @PostMapping("/{id}/error")
    public ResponseEntity<Map<String, Object>> recordError(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/monitoring/{}/error", id);

        try {
            String error = request.get("error");
            monitoringService.recordError(id, error);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error recorded");
            response.put("monitorId", id);
            response.put("error", error);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error recording error for monitor: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create alert
     */
    @PostMapping("/{id}/alert")
    public ResponseEntity<Map<String, Object>> createAlert(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/monitoring/{}/alert", id);

        try {
            String alertName = (String) request.get("alertName");
            String severityStr = (String) request.get("severity");
            ReportMonitor.AlertSeverity severity = severityStr != null ?
                    ReportMonitor.AlertSeverity.valueOf(severityStr) :
                    ReportMonitor.AlertSeverity.MEDIUM;
            String message = (String) request.get("message");

            monitoringService.createAlert(id, alertName, severity, message);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Alert created");
            response.put("monitorId", id);
            response.put("alertName", alertName);
            response.put("severity", severity);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error creating alert for monitor: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Acknowledge alert
     */
    @PostMapping("/{id}/alert/{alertId}/acknowledge")
    public ResponseEntity<Map<String, Object>> acknowledgeAlert(
            @PathVariable Long id,
            @PathVariable String alertId,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/monitoring/{}/alert/{}/acknowledge", id, alertId);

        try {
            String acknowledgedBy = request.get("acknowledgedBy");
            monitoringService.acknowledgeAlert(id, alertId, acknowledgedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Alert acknowledged");
            response.put("monitorId", id);
            response.put("alertId", alertId);
            response.put("acknowledgedBy", acknowledgedBy);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error acknowledging alert {} for monitor: {}", alertId, id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Resolve alert
     */
    @PostMapping("/{id}/alert/{alertId}/resolve")
    public ResponseEntity<Map<String, Object>> resolveAlert(
            @PathVariable Long id,
            @PathVariable String alertId) {
        log.info("POST /api/monitoring/{}/alert/{}/resolve", id, alertId);

        try {
            monitoringService.resolveAlert(id, alertId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Alert resolved");
            response.put("monitorId", id);
            response.put("alertId", alertId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error resolving alert {} for monitor: {}", alertId, id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get active alerts
     */
    @GetMapping("/{id}/alerts")
    public ResponseEntity<List<ReportMonitor.Alert>> getActiveAlerts(@PathVariable Long id) {
        log.info("GET /api/monitoring/{}/alerts", id);

        try {
            List<ReportMonitor.Alert> alerts = monitoringService.getActiveAlerts(id);
            return ResponseEntity.ok(alerts);

        } catch (Exception e) {
            log.error("Error fetching active alerts for monitor: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get historical data
     */
    @GetMapping("/{id}/history")
    public ResponseEntity<List<ReportMonitor.HistoricalData>> getHistoricalData(@PathVariable Long id) {
        log.info("GET /api/monitoring/{}/history", id);

        try {
            List<ReportMonitor.HistoricalData> history = monitoringService.getHistoricalData(id);
            return ResponseEntity.ok(history);

        } catch (Exception e) {
            log.error("Error fetching historical data for monitor: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Record historical data
     */
    @PostMapping("/{id}/record-history")
    public ResponseEntity<Map<String, Object>> recordHistoricalData(@PathVariable Long id) {
        log.info("POST /api/monitoring/{}/record-history", id);

        try {
            monitoringService.recordHistoricalData(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Historical data recorded");
            response.put("monitorId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error recording historical data for monitor: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get monitors needing attention
     */
    @GetMapping("/needs-attention")
    public ResponseEntity<List<ReportMonitor>> getMonitorsNeedingAttention() {
        log.info("GET /api/monitoring/needs-attention");

        try {
            List<ReportMonitor> monitors = monitoringService.getMonitorsNeedingAttention();
            return ResponseEntity.ok(monitors);

        } catch (Exception e) {
            log.error("Error fetching monitors needing attention", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get degraded monitors
     */
    @GetMapping("/degraded")
    public ResponseEntity<List<ReportMonitor>> getDegradedMonitors() {
        log.info("GET /api/monitoring/degraded");

        try {
            List<ReportMonitor> monitors = monitoringService.getDegradedMonitors();
            return ResponseEntity.ok(monitors);

        } catch (Exception e) {
            log.error("Error fetching degraded monitors", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get unhealthy monitors
     */
    @GetMapping("/unhealthy")
    public ResponseEntity<List<ReportMonitor>> getUnhealthyMonitors() {
        log.info("GET /api/monitoring/unhealthy");

        try {
            List<ReportMonitor> monitors = monitoringService.getUnhealthyMonitors();
            return ResponseEntity.ok(monitors);

        } catch (Exception e) {
            log.error("Error fetching unhealthy monitors", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete monitor
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteMonitor(@PathVariable Long id) {
        log.info("DELETE /api/monitoring/{}", id);

        try {
            monitoringService.deleteMonitor(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Monitor deleted");
            response.put("monitorId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting monitor: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/monitoring/stats");

        try {
            Map<String, Object> stats = monitoringService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching monitoring statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
