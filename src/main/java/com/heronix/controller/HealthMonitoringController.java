package com.heronix.controller;

import com.heronix.service.SystemMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Health Monitoring Controller
 *
 * REST API endpoints for system health and performance monitoring.
 *
 * Endpoints:
 * - GET /api/health - Basic health check
 * - GET /api/health/metrics - Current system metrics
 * - GET /api/health/metrics/history - Historical metrics
 * - GET /api/health/status - Detailed health status
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 63 - System Health Monitoring & Performance Dashboard
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Slf4j
public class HealthMonitoringController {

    private final SystemMetricsService metricsService;

    /**
     * Basic health check endpoint
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("service", "Heronix SIS");
        health.put("version", "1.0.0");

        return ResponseEntity.ok(health);
    }

    /**
     * Get current system metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<SystemMetricsService.SystemSnapshot> getCurrentMetrics() {
        log.info("GET /api/health/metrics");

        try {
            SystemMetricsService.SystemSnapshot metrics = metricsService.getCurrentMetrics();
            return ResponseEntity.ok(metrics);

        } catch (Exception e) {
            log.error("Error retrieving current metrics", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get metrics history
     */
    @GetMapping("/metrics/history")
    public ResponseEntity<List<SystemMetricsService.SystemSnapshot>> getMetricsHistory(
            @RequestParam(defaultValue = "24") int hours) {

        log.info("GET /api/health/metrics/history - hours: {}", hours);

        try {
            List<SystemMetricsService.SystemSnapshot> history =
                    metricsService.getMetricsHistory(hours);

            return ResponseEntity.ok(history);

        } catch (Exception e) {
            log.error("Error retrieving metrics history", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get system health status
     */
    @GetMapping("/status")
    public ResponseEntity<SystemMetricsService.HealthStatus> getHealthStatus() {
        log.info("GET /api/health/status");

        try {
            SystemMetricsService.HealthStatus status = metricsService.getHealthStatus();
            return ResponseEntity.ok(status);

        } catch (Exception e) {
            log.error("Error retrieving health status", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
