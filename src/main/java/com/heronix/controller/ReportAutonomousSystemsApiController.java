package com.heronix.controller;

import com.heronix.dto.ReportAutonomousSystems;
import com.heronix.service.ReportAutonomousSystemsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Autonomous Systems API Controller
 *
 * REST API endpoints for autonomous systems, self-healing infrastructure,
 * automated remediation, and intelligent failure detection.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 153 - Autonomous Systems & Self-Healing Infrastructure
 */
@Slf4j
// @RestController  // Disabled for API server mode
@RequestMapping("/api/autonomous-systems")
@RequiredArgsConstructor
public class ReportAutonomousSystemsApiController {

    private final ReportAutonomousSystemsService autonomousSystemsService;

    /**
     * Create new autonomous system configuration
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAutonomousSystem(
            @RequestBody ReportAutonomousSystems autonomousSystem) {
        try {
            ReportAutonomousSystems created = autonomousSystemsService.createAutonomousSystem(autonomousSystem);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Autonomous system configuration created successfully");
            response.put("autonomousSystemId", created.getAutonomousSystemId());
            response.put("autonomousSystemName", created.getAutonomousSystemName());
            response.put("platform", created.getPlatform());
            response.put("environment", created.getEnvironment());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create autonomous system configuration: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get autonomous system configuration by ID
     */
    @GetMapping("/{autonomousSystemId}")
    public ResponseEntity<Map<String, Object>> getAutonomousSystem(
            @PathVariable Long autonomousSystemId) {
        try {
            ReportAutonomousSystems autonomousSystem =
                    autonomousSystemsService.getAutonomousSystem(autonomousSystemId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("autonomousSystem", autonomousSystem);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Activate autonomous system
     */
    @PostMapping("/{autonomousSystemId}/activate")
    public ResponseEntity<Map<String, Object>> activateAutonomousSystem(
            @PathVariable Long autonomousSystemId) {
        try {
            Map<String, Object> result =
                    autonomousSystemsService.activateAutonomousSystem(autonomousSystemId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Detect anomaly
     */
    @PostMapping("/{autonomousSystemId}/anomaly/detect")
    public ResponseEntity<Map<String, Object>> detectAnomaly(
            @PathVariable Long autonomousSystemId,
            @RequestBody Map<String, Object> anomalyData) {
        try {
            Map<String, Object> result =
                    autonomousSystemsService.detectAnomaly(autonomousSystemId, anomalyData);
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
     * Execute remediation
     */
    @PostMapping("/{autonomousSystemId}/remediation/execute")
    public ResponseEntity<Map<String, Object>> executeRemediation(
            @PathVariable Long autonomousSystemId,
            @RequestBody Map<String, Object> remediationData) {
        try {
            Map<String, Object> result =
                    autonomousSystemsService.executeRemediation(autonomousSystemId, remediationData);
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
     * Report incident
     */
    @PostMapping("/{autonomousSystemId}/incidents/report")
    public ResponseEntity<Map<String, Object>> reportIncident(
            @PathVariable Long autonomousSystemId,
            @RequestBody Map<String, Object> incidentData) {
        try {
            Map<String, Object> result =
                    autonomousSystemsService.reportIncident(autonomousSystemId, incidentData);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Resolve incident
     */
    @PostMapping("/{autonomousSystemId}/incidents/{incidentId}/resolve")
    public ResponseEntity<Map<String, Object>> resolveIncident(
            @PathVariable Long autonomousSystemId,
            @PathVariable String incidentId) {
        try {
            Map<String, Object> result =
                    autonomousSystemsService.resolveIncident(autonomousSystemId, incidentId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Perform health check
     */
    @PostMapping("/{autonomousSystemId}/health/check")
    public ResponseEntity<Map<String, Object>> performHealthCheck(
            @PathVariable Long autonomousSystemId) {
        try {
            Map<String, Object> result = autonomousSystemsService.performHealthCheck(autonomousSystemId);
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
     * Get remediation metrics
     */
    @GetMapping("/{autonomousSystemId}/metrics/remediation")
    public ResponseEntity<Map<String, Object>> getRemediationMetrics(
            @PathVariable Long autonomousSystemId) {
        try {
            Map<String, Object> metrics = autonomousSystemsService.getRemediationMetrics(autonomousSystemId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("metrics", metrics);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get incident metrics
     */
    @GetMapping("/{autonomousSystemId}/metrics/incidents")
    public ResponseEntity<Map<String, Object>> getIncidentMetrics(
            @PathVariable Long autonomousSystemId) {
        try {
            Map<String, Object> metrics = autonomousSystemsService.getIncidentMetrics(autonomousSystemId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("metrics", metrics);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get all autonomous system configurations
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllAutonomousSystems() {
        List<ReportAutonomousSystems> configs = autonomousSystemsService.getAllAutonomousSystems();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("configurations", configs);
        response.put("count", configs.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Get active configurations
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveConfigs() {
        List<ReportAutonomousSystems> configs = autonomousSystemsService.getActiveConfigs();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("configurations", configs);
        response.put("count", configs.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Delete autonomous system configuration
     */
    @DeleteMapping("/{autonomousSystemId}")
    public ResponseEntity<Map<String, Object>> deleteAutonomousSystem(
            @PathVariable Long autonomousSystemId) {
        try {
            autonomousSystemsService.deleteAutonomousSystem(autonomousSystemId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Autonomous system configuration deleted successfully");
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
        Map<String, Object> stats = autonomousSystemsService.getStatistics();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("statistics", stats);
        return ResponseEntity.ok(response);
    }
}
