package com.heronix.controller;

import com.heronix.dto.ReportObservability;
import com.heronix.service.ReportObservabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Observability API Controller
 *
 * REST API endpoints for observability management, distributed tracing,
 * metrics collection, log aggregation, and telemetry.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 144 - Observability & Telemetry
 */
@Slf4j
@RestController
@RequestMapping("/api/observability")
@RequiredArgsConstructor
public class ReportObservabilityApiController {

    private final ReportObservabilityService observabilityService;

    /**
     * Create new observability configuration
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createObservability(
            @RequestBody ReportObservability observability) {
        try {
            ReportObservability created = observabilityService.createObservability(observability);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Observability configuration created successfully");
            response.put("observabilityId", created.getObservabilityId());
            response.put("observabilityName", created.getObservabilityName());
            response.put("platform", created.getObservabilityPlatform());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create observability configuration: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get observability configuration by ID
     */
    @GetMapping("/{observabilityId}")
    public ResponseEntity<Map<String, Object>> getObservability(@PathVariable Long observabilityId) {
        try {
            ReportObservability observability = observabilityService.getObservability(observabilityId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("observability", observability);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Activate observability
     */
    @PostMapping("/{observabilityId}/activate")
    public ResponseEntity<Map<String, Object>> activate(@PathVariable Long observabilityId) {
        try {
            ReportObservability observability = observabilityService.activate(observabilityId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Observability activated successfully");
            response.put("observabilityStatus", observability.getObservabilityStatus());
            response.put("activatedAt", observability.getActivatedAt());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Collect metrics
     */
    @PostMapping("/{observabilityId}/metrics/collect")
    public ResponseEntity<Map<String, Object>> collectMetrics(@PathVariable Long observabilityId) {
        try {
            Map<String, Object> result = observabilityService.collectMetrics(observabilityId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Record trace
     */
    @PostMapping("/{observabilityId}/trace")
    public ResponseEntity<Map<String, Object>> recordTrace(
            @PathVariable Long observabilityId,
            @RequestBody Map<String, Object> traceData) {
        try {
            Map<String, Object> result = observabilityService.recordTrace(observabilityId, traceData);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Collect logs
     */
    @PostMapping("/{observabilityId}/logs/collect")
    public ResponseEntity<Map<String, Object>> collectLogs(
            @PathVariable Long observabilityId,
            @RequestBody Map<String, Object> request) {
        try {
            String level = (String) request.getOrDefault("level", "INFO");
            long count = request.containsKey("count")
                    ? ((Number) request.get("count")).longValue()
                    : 1L;

            Map<String, Object> result = observabilityService.collectLogs(observabilityId, level, count);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Track service performance
     */
    @PostMapping("/{observabilityId}/service/performance")
    public ResponseEntity<Map<String, Object>> trackServicePerformance(
            @PathVariable Long observabilityId,
            @RequestBody Map<String, Object> perfData) {
        try {
            Map<String, Object> result = observabilityService.trackServicePerformance(observabilityId, perfData);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Create alert
     */
    @PostMapping("/{observabilityId}/alert")
    public ResponseEntity<Map<String, Object>> createAlert(
            @PathVariable Long observabilityId,
            @RequestBody Map<String, Object> alertData) {
        try {
            Map<String, Object> result = observabilityService.createAlert(observabilityId, alertData);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Resolve alert
     */
    @PostMapping("/{observabilityId}/alert/{alertId}/resolve")
    public ResponseEntity<Map<String, Object>> resolveAlert(
            @PathVariable Long observabilityId,
            @PathVariable String alertId) {
        try {
            ReportObservability observability = observabilityService.resolveAlert(observabilityId, alertId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Alert resolved successfully");
            response.put("resolvedAlerts", observability.getResolvedAlerts());
            response.put("activeAlerts", observability.getActiveAlerts().size());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Check SLO compliance
     */
    @GetMapping("/{observabilityId}/slo/compliance")
    public ResponseEntity<Map<String, Object>> checkSloCompliance(@PathVariable Long observabilityId) {
        try {
            Map<String, Object> result = observabilityService.checkSloCompliance(observabilityId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Run synthetic check
     */
    @PostMapping("/{observabilityId}/synthetic/check")
    public ResponseEntity<Map<String, Object>> runSyntheticCheck(
            @PathVariable Long observabilityId,
            @RequestBody Map<String, String> request) {
        try {
            String endpoint = request.get("endpoint");
            if (endpoint == null || endpoint.isEmpty()) {
                throw new IllegalArgumentException("Endpoint is required");
            }

            Map<String, Object> result = observabilityService.runSyntheticCheck(observabilityId, endpoint);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Get health status
     */
    @GetMapping("/{observabilityId}/health")
    public ResponseEntity<Map<String, Object>> getHealthStatus(@PathVariable Long observabilityId) {
        try {
            Map<String, Object> health = observabilityService.getHealthStatus(observabilityId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("health", health);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get all observability configurations
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllObservability() {
        List<ReportObservability> configs = observabilityService.getAllObservability();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("configurations", configs);
        response.put("count", configs.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Get healthy configurations
     */
    @GetMapping("/healthy")
    public ResponseEntity<Map<String, Object>> getHealthyConfigs() {
        List<ReportObservability> configs = observabilityService.getHealthyConfigs();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("configurations", configs);
        response.put("count", configs.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Delete observability configuration
     */
    @DeleteMapping("/{observabilityId}")
    public ResponseEntity<Map<String, Object>> deleteObservability(@PathVariable Long observabilityId) {
        try {
            observabilityService.deleteObservability(observabilityId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Observability configuration deleted successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = observabilityService.getStatistics();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("statistics", stats);
        return ResponseEntity.ok(response);
    }
}
