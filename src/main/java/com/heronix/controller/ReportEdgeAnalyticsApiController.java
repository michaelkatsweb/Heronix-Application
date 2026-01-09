package com.heronix.controller;

import com.heronix.dto.ReportEdgeAnalytics;
import com.heronix.service.ReportEdgeAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Edge Analytics API Controller
 *
 * REST API endpoints for edge computing analytics, IoT device management,
 * real-time sensor data processing, and distributed edge intelligence.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 133 - Edge Analytics & IoT Integration
 */
@Slf4j
@RestController
@RequestMapping("/api/edge-analytics")
@RequiredArgsConstructor
public class ReportEdgeAnalyticsApiController {

    private final ReportEdgeAnalyticsService edgeAnalyticsService;

    /**
     * Create new edge analytics configuration
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createEdgeAnalytics(
            @RequestBody ReportEdgeAnalytics analytics) {
        try {
            ReportEdgeAnalytics created = edgeAnalyticsService.createEdgeAnalytics(analytics);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Edge analytics created successfully");
            response.put("analyticsId", created.getAnalyticsId());
            response.put("analyticsName", created.getAnalyticsName());
            response.put("edgeLocation", created.getEdgeLocation());
            response.put("status", created.getAnalyticsStatus());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create edge analytics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get edge analytics by ID
     */
    @GetMapping("/{analyticsId}")
    public ResponseEntity<Map<String, Object>> getEdgeAnalytics(@PathVariable Long analyticsId) {
        try {
            ReportEdgeAnalytics analytics = edgeAnalyticsService.getEdgeAnalytics(analyticsId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("analytics", analytics);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Start edge analytics processing
     */
    @PostMapping("/{analyticsId}/start")
    public ResponseEntity<Map<String, Object>> startAnalytics(@PathVariable Long analyticsId) {
        try {
            ReportEdgeAnalytics analytics = edgeAnalyticsService.startAnalytics(analyticsId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Edge analytics started successfully");
            response.put("status", analytics.getAnalyticsStatus());
            response.put("startedAt", analytics.getStartedAt());
            response.put("deviceCount", analytics.getDeviceCount());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Stop edge analytics processing
     */
    @PostMapping("/{analyticsId}/stop")
    public ResponseEntity<Map<String, Object>> stopAnalytics(@PathVariable Long analyticsId) {
        try {
            ReportEdgeAnalytics analytics = edgeAnalyticsService.stopAnalytics(analyticsId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Edge analytics stopped successfully");
            response.put("status", analytics.getAnalyticsStatus());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Pause edge analytics processing
     */
    @PostMapping("/{analyticsId}/pause")
    public ResponseEntity<Map<String, Object>> pauseAnalytics(@PathVariable Long analyticsId) {
        try {
            ReportEdgeAnalytics analytics = edgeAnalyticsService.pauseAnalytics(analyticsId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Edge analytics paused successfully");
            response.put("status", analytics.getAnalyticsStatus());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Process sensor data
     */
    @PostMapping("/{analyticsId}/process")
    public ResponseEntity<Map<String, Object>> processSensorData(
            @PathVariable Long analyticsId,
            @RequestBody Map<String, Object> sensorData) {
        try {
            ReportEdgeAnalytics analytics = edgeAnalyticsService.processSensorData(analyticsId, sensorData);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Sensor data processed successfully");
            response.put("totalEventsProcessed", analytics.getTotalEventsProcessed());
            response.put("lastDataReceivedAt", analytics.getLastDataReceivedAt());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Add IoT device
     */
    @PostMapping("/{analyticsId}/device")
    public ResponseEntity<Map<String, Object>> addDevice(
            @PathVariable Long analyticsId,
            @RequestBody Map<String, String> request) {
        try {
            String deviceId = request.get("deviceId");
            if (deviceId == null || deviceId.isEmpty()) {
                throw new IllegalArgumentException("Device ID is required");
            }

            ReportEdgeAnalytics analytics = edgeAnalyticsService.addDevice(analyticsId, deviceId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Device added successfully");
            response.put("deviceId", deviceId);
            response.put("deviceCount", analytics.getDeviceCount());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Remove IoT device
     */
    @DeleteMapping("/{analyticsId}/device/{deviceId}")
    public ResponseEntity<Map<String, Object>> removeDevice(
            @PathVariable Long analyticsId,
            @PathVariable String deviceId) {
        try {
            ReportEdgeAnalytics analytics = edgeAnalyticsService.removeDevice(analyticsId, deviceId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Device removed successfully");
            response.put("deviceId", deviceId);
            response.put("deviceCount", analytics.getDeviceCount());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Add sensor type
     */
    @PostMapping("/{analyticsId}/sensor-type")
    public ResponseEntity<Map<String, Object>> addSensorType(
            @PathVariable Long analyticsId,
            @RequestBody Map<String, String> request) {
        try {
            String sensorType = request.get("sensorType");
            if (sensorType == null || sensorType.isEmpty()) {
                throw new IllegalArgumentException("Sensor type is required");
            }

            edgeAnalyticsService.addSensorType(analyticsId, sensorType);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Sensor type added successfully");
            response.put("sensorType", sensorType);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Add transformation rule
     */
    @PostMapping("/{analyticsId}/transformation")
    public ResponseEntity<Map<String, Object>> addTransformation(
            @PathVariable Long analyticsId,
            @RequestBody Map<String, Object> transformation) {
        try {
            edgeAnalyticsService.addTransformation(analyticsId, transformation);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Transformation rule added successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Add alert rule
     */
    @PostMapping("/{analyticsId}/alert-rule")
    public ResponseEntity<Map<String, Object>> addAlertRule(
            @PathVariable Long analyticsId,
            @RequestBody Map<String, Object> alertRule) {
        try {
            edgeAnalyticsService.addAlertRule(analyticsId, alertRule);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Alert rule added successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Sync data to cloud
     */
    @PostMapping("/{analyticsId}/sync")
    public ResponseEntity<Map<String, Object>> syncToCloud(@PathVariable Long analyticsId) {
        try {
            ReportEdgeAnalytics analytics = edgeAnalyticsService.syncToCloud(analyticsId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Data synced to cloud successfully");
            response.put("lastSyncedAt", analytics.getLastSyncedAt());
            response.put("cloudEndpoint", analytics.getCloudEndpoint());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Run ML inference
     */
    @PostMapping("/{analyticsId}/inference")
    public ResponseEntity<Map<String, Object>> runInference(
            @PathVariable Long analyticsId,
            @RequestBody Map<String, Object> inputData) {
        try {
            Map<String, Object> inferenceResult = edgeAnalyticsService.runInference(analyticsId, inputData);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "ML inference completed successfully");
            response.put("result", inferenceResult);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Cleanup old data
     */
    @PostMapping("/{analyticsId}/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupData(@PathVariable Long analyticsId) {
        try {
            ReportEdgeAnalytics analytics = edgeAnalyticsService.cleanupData(analyticsId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Data cleanup completed successfully");
            response.put("currentStorageUsageMb", analytics.getCurrentStorageUsageMb());
            response.put("storageUtilization", analytics.getStorageUtilizationPercentage());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Update resource metrics
     */
    @PutMapping("/{analyticsId}/metrics")
    public ResponseEntity<Map<String, Object>> updateResourceMetrics(
            @PathVariable Long analyticsId,
            @RequestBody Map<String, Object> metrics) {
        try {
            edgeAnalyticsService.updateResourceMetrics(analyticsId, metrics);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Resource metrics updated successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get performance statistics
     */
    @GetMapping("/{analyticsId}/performance")
    public ResponseEntity<Map<String, Object>> getPerformanceStats(@PathVariable Long analyticsId) {
        try {
            Map<String, Object> stats = edgeAnalyticsService.getPerformanceStats(analyticsId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("performance", stats);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get all edge analytics configurations
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllEdgeAnalytics() {
        List<ReportEdgeAnalytics> analyticsList = edgeAnalyticsService.getAllEdgeAnalytics();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("analytics", analyticsList);
        response.put("count", analyticsList.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Get analytics by location
     */
    @GetMapping("/location/{edgeLocation}")
    public ResponseEntity<Map<String, Object>> getAnalyticsByLocation(@PathVariable String edgeLocation) {
        List<ReportEdgeAnalytics> analyticsList = edgeAnalyticsService.getAnalyticsByLocation(edgeLocation);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("analytics", analyticsList);
        response.put("count", analyticsList.size());
        response.put("edgeLocation", edgeLocation);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete edge analytics
     */
    @DeleteMapping("/{analyticsId}")
    public ResponseEntity<Map<String, Object>> deleteEdgeAnalytics(@PathVariable Long analyticsId) {
        try {
            edgeAnalyticsService.deleteEdgeAnalytics(analyticsId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Edge analytics deleted successfully");
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
        Map<String, Object> stats = edgeAnalyticsService.getStatistics();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("statistics", stats);
        return ResponseEntity.ok(response);
    }
}
