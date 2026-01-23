package com.heronix.controller;

import com.heronix.dto.ReportStreaming;
import com.heronix.service.ReportStreamingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Report Streaming API Controller
 *
 * REST API endpoints for real-time report streaming and updates.
 *
 * Endpoints:
 * - POST /api/streaming - Create stream
 * - GET /api/streaming/{id} - Get stream
 * - POST /api/streaming/{id}/start - Start stream
 * - POST /api/streaming/{id}/pause - Pause stream
 * - POST /api/streaming/{id}/resume - Resume stream
 * - POST /api/streaming/{id}/stop - Stop stream
 * - POST /api/streaming/{id}/subscription - Add subscription
 * - DELETE /api/streaming/{id}/subscription/{subscriptionId} - Remove subscription
 * - POST /api/streaming/{id}/connection - Add connection
 * - DELETE /api/streaming/{id}/connection/{connectionId} - Remove connection
 * - PUT /api/streaming/{id}/connection/{connectionId}/status - Update connection status
 * - POST /api/streaming/{id}/update - Publish update
 * - POST /api/streaming/{id}/filter - Add filter
 * - POST /api/streaming/{id}/transform - Add transform
 * - PUT /api/streaming/{id}/metrics - Update metrics
 * - GET /api/streaming/{id}/subscriptions - Get active subscriptions
 * - GET /api/streaming/{id}/connections - Get active connections
 * - DELETE /api/streaming/{id} - Delete stream
 * - GET /api/streaming/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 92 - Report Streaming & Real-time Updates
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/streaming")
@RequiredArgsConstructor
@Slf4j
public class ReportStreamingApiController {

    private final ReportStreamingService streamingService;

    /**
     * Create stream
     */
    @PostMapping
    public ResponseEntity<ReportStreaming> createStream(@RequestBody ReportStreaming stream) {
        log.info("POST /api/streaming - Creating stream for report {}: {}",
                stream.getReportId(), stream.getStreamName());

        try {
            ReportStreaming created = streamingService.createStream(stream);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating stream", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get stream
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportStreaming> getStream(@PathVariable Long id) {
        log.info("GET /api/streaming/{}", id);

        try {
            return streamingService.getStream(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching stream: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Start stream
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<Map<String, Object>> startStream(@PathVariable Long id) {
        log.info("POST /api/streaming/{}/start", id);

        try {
            streamingService.startStream(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Stream started");
            response.put("streamId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Stream not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error starting stream: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Pause stream
     */
    @PostMapping("/{id}/pause")
    public ResponseEntity<Map<String, Object>> pauseStream(@PathVariable Long id) {
        log.info("POST /api/streaming/{}/pause", id);

        try {
            streamingService.pauseStream(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Stream paused");
            response.put("streamId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Stream not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error pausing stream: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Resume stream
     */
    @PostMapping("/{id}/resume")
    public ResponseEntity<Map<String, Object>> resumeStream(@PathVariable Long id) {
        log.info("POST /api/streaming/{}/resume", id);

        try {
            streamingService.resumeStream(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Stream resumed");
            response.put("streamId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Stream not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error resuming stream: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Stop stream
     */
    @PostMapping("/{id}/stop")
    public ResponseEntity<Map<String, Object>> stopStream(@PathVariable Long id) {
        log.info("POST /api/streaming/{}/stop", id);

        try {
            streamingService.stopStream(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Stream stopped");
            response.put("streamId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Stream not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error stopping stream: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Add subscription
     */
    @PostMapping("/{id}/subscription")
    public ResponseEntity<ReportStreaming.Subscription> addSubscription(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/streaming/{}/subscription", id);

        try {
            String subscriberId = request.get("subscriberId");
            String subscriberName = request.get("subscriberName");
            String subscriberEmail = request.get("subscriberEmail");

            ReportStreaming.Subscription subscription = streamingService.addSubscription(
                    id, subscriberId, subscriberName, subscriberEmail
            );

            return ResponseEntity.ok(subscription);

        } catch (IllegalArgumentException e) {
            log.error("Stream not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding subscription to stream: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Remove subscription
     */
    @DeleteMapping("/{id}/subscription/{subscriptionId}")
    public ResponseEntity<Map<String, Object>> removeSubscription(
            @PathVariable Long id,
            @PathVariable String subscriptionId) {
        log.info("DELETE /api/streaming/{}/subscription/{}", id, subscriptionId);

        try {
            streamingService.removeSubscription(id, subscriptionId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Subscription removed");
            response.put("streamId", id);
            response.put("subscriptionId", subscriptionId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Stream not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error removing subscription from stream: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Add connection
     */
    @PostMapping("/{id}/connection")
    public ResponseEntity<ReportStreaming.Connection> addConnection(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/streaming/{}/connection", id);

        try {
            String userId = request.get("userId");
            String protocol = request.get("protocol");
            String ipAddress = request.get("ipAddress");

            ReportStreaming.Connection connection = streamingService.addConnection(
                    id, userId, protocol, ipAddress
            );

            return ResponseEntity.ok(connection);

        } catch (IllegalArgumentException e) {
            log.error("Stream not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            log.error("Cannot accept new connection: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error adding connection to stream: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Remove connection
     */
    @DeleteMapping("/{id}/connection/{connectionId}")
    public ResponseEntity<Map<String, Object>> removeConnection(
            @PathVariable Long id,
            @PathVariable String connectionId) {
        log.info("DELETE /api/streaming/{}/connection/{}", id, connectionId);

        try {
            streamingService.removeConnection(id, connectionId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Connection removed");
            response.put("streamId", id);
            response.put("connectionId", connectionId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Stream or connection not found: {}, {}", id, connectionId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error removing connection from stream: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update connection status
     */
    @PutMapping("/{id}/connection/{connectionId}/status")
    public ResponseEntity<Map<String, Object>> updateConnectionStatus(
            @PathVariable Long id,
            @PathVariable String connectionId,
            @RequestBody Map<String, String> request) {
        log.info("PUT /api/streaming/{}/connection/{}/status", id, connectionId);

        try {
            String statusStr = request.get("status");
            ReportStreaming.ConnectionStatus status =
                    ReportStreaming.ConnectionStatus.valueOf(statusStr);

            streamingService.updateConnectionStatus(id, connectionId, status);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Connection status updated");
            response.put("streamId", id);
            response.put("connectionId", connectionId);
            response.put("status", status);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Stream or connection not found: {}, {}", id, connectionId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating connection status in stream: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Publish update
     */
    @PostMapping("/{id}/update")
    public ResponseEntity<Map<String, Object>> publishUpdate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/streaming/{}/update", id);

        try {
            String updateType = (String) request.get("updateType");
            String changeType = (String) request.get("changeType");

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.get("data");

            streamingService.publishUpdate(id, updateType, data, changeType);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Update published");
            response.put("streamId", id);
            response.put("updateType", updateType);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Stream not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error publishing update to stream: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add filter
     */
    @PostMapping("/{id}/filter")
    public ResponseEntity<Map<String, Object>> addFilter(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/streaming/{}/filter", id);

        try {
            String filterName = request.get("filterName");
            String filterType = request.get("filterType");
            String condition = request.get("condition");

            streamingService.addFilter(id, filterName, filterType, condition);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Filter added");
            response.put("streamId", id);
            response.put("filterName", filterName);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Stream not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding filter to stream: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add transform
     */
    @PostMapping("/{id}/transform")
    public ResponseEntity<Map<String, Object>> addTransform(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/streaming/{}/transform", id);

        try {
            String transformName = request.get("transformName");
            String transformType = request.get("transformType");
            String expression = request.get("expression");

            streamingService.addTransform(id, transformName, transformType, expression);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Transform added");
            response.put("streamId", id);
            response.put("transformName", transformName);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Stream not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding transform to stream: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update metrics
     */
    @PutMapping("/{id}/metrics")
    public ResponseEntity<Map<String, Object>> updateMetrics(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("PUT /api/streaming/{}/metrics", id);

        try {
            Long totalUpdates = ((Number) request.get("totalUpdates")).longValue();
            Long successfulDeliveries = ((Number) request.get("successfulDeliveries")).longValue();
            Long failedDeliveries = ((Number) request.get("failedDeliveries")).longValue();
            Double averageLatencyMs = ((Number) request.get("averageLatencyMs")).doubleValue();
            Double throughputPerSecond = ((Number) request.get("throughputPerSecond")).doubleValue();

            streamingService.updateMetrics(id, totalUpdates, successfulDeliveries,
                    failedDeliveries, averageLatencyMs, throughputPerSecond);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Metrics updated");
            response.put("streamId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Stream not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating metrics for stream: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get active subscriptions
     */
    @GetMapping("/{id}/subscriptions")
    public ResponseEntity<Map<String, Object>> getActiveSubscriptions(@PathVariable Long id) {
        log.info("GET /api/streaming/{}/subscriptions", id);

        try {
            return streamingService.getStream(id)
                    .map(stream -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("streamId", id);
                        response.put("subscriptions", stream.getActiveSubscriptions());
                        response.put("count", stream.getActiveSubscriptions().size());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching active subscriptions for stream: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get active connections
     */
    @GetMapping("/{id}/connections")
    public ResponseEntity<Map<String, Object>> getActiveConnections(@PathVariable Long id) {
        log.info("GET /api/streaming/{}/connections", id);

        try {
            return streamingService.getStream(id)
                    .map(stream -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("streamId", id);
                        response.put("connections", stream.getActiveConnections());
                        response.put("count", stream.getActiveConnections().size());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching active connections for stream: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete stream
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteStream(@PathVariable Long id) {
        log.info("DELETE /api/streaming/{}", id);

        try {
            streamingService.deleteStream(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Stream deleted");
            response.put("streamId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting stream: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/streaming/stats");

        try {
            Map<String, Object> stats = streamingService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching streaming statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
