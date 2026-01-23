package com.heronix.controller;

import com.heronix.dto.ReportApiGateway;
import com.heronix.service.ReportApiGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report API Gateway API Controller
 *
 * REST API endpoints for API gateway management.
 *
 * Endpoints:
 * - POST /api/api-gateway - Create gateway
 * - GET /api/api-gateway/{id} - Get gateway
 * - POST /api/api-gateway/{id}/start - Start gateway
 * - POST /api/api-gateway/{id}/stop - Stop gateway
 * - POST /api/api-gateway/{id}/route - Add route
 * - POST /api/api-gateway/{id}/backend - Add backend
 * - PUT /api/api-gateway/{id}/backend/{backendId}/status - Update backend status
 * - POST /api/api-gateway/{id}/api-key - Create API key
 * - POST /api/api-gateway/{id}/api-key/validate - Validate API key
 * - POST /api/api-gateway/{id}/request - Record request
 * - POST /api/api-gateway/{id}/rate-limit/check - Check rate limit
 * - POST /api/api-gateway/{id}/circuit-breaker/{backendId} - Update circuit breaker
 * - PUT /api/api-gateway/{id}/metrics - Update metrics
 * - GET /api/api-gateway/{id}/routes - Get active routes
 * - GET /api/api-gateway/{id}/backends - Get healthy backends
 * - DELETE /api/api-gateway/{id} - Delete gateway
 * - GET /api/api-gateway/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 94 - Report API Gateway & Integration
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/api-gateway")
@RequiredArgsConstructor
@Slf4j
public class ReportApiGatewayApiController {

    private final ReportApiGatewayService apiGatewayService;

    /**
     * Create gateway
     */
    @PostMapping
    public ResponseEntity<ReportApiGateway> createGateway(@RequestBody ReportApiGateway gateway) {
        log.info("POST /api/api-gateway - Creating gateway: {}", gateway.getGatewayName());

        try {
            ReportApiGateway created = apiGatewayService.createGateway(gateway);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating gateway", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get gateway
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportApiGateway> getGateway(@PathVariable Long id) {
        log.info("GET /api/api-gateway/{}", id);

        try {
            return apiGatewayService.getGateway(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching gateway: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Start gateway
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<Map<String, Object>> startGateway(@PathVariable Long id) {
        log.info("POST /api/api-gateway/{}/start", id);

        try {
            apiGatewayService.startGateway(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Gateway started");
            response.put("gatewayId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Gateway not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error starting gateway: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Stop gateway
     */
    @PostMapping("/{id}/stop")
    public ResponseEntity<Map<String, Object>> stopGateway(@PathVariable Long id) {
        log.info("POST /api/api-gateway/{}/stop", id);

        try {
            apiGatewayService.stopGateway(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Gateway stopped");
            response.put("gatewayId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Gateway not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error stopping gateway: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Add route
     */
    @PostMapping("/{id}/route")
    public ResponseEntity<ReportApiGateway.Route> addRoute(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/api-gateway/{}/route", id);

        try {
            String routeName = (String) request.get("routeName");
            String path = (String) request.get("path");
            String routeTypeStr = (String) request.get("routeType");
            String targetUrl = (String) request.get("targetUrl");

            @SuppressWarnings("unchecked")
            List<String> methods = (List<String>) request.get("methods");

            ReportApiGateway.RouteType routeType = ReportApiGateway.RouteType.valueOf(routeTypeStr);

            ReportApiGateway.Route route = apiGatewayService.addRoute(
                    id, routeName, path, routeType, targetUrl, methods
            );

            return ResponseEntity.ok(route);

        } catch (IllegalArgumentException e) {
            log.error("Gateway not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding route to gateway: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add backend
     */
    @PostMapping("/{id}/backend")
    public ResponseEntity<ReportApiGateway.Backend> addBackend(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/api-gateway/{}/backend", id);

        try {
            String backendName = (String) request.get("backendName");
            String host = (String) request.get("host");
            Integer port = ((Number) request.get("port")).intValue();
            String protocol = (String) request.get("protocol");

            ReportApiGateway.Backend backend = apiGatewayService.addBackend(
                    id, backendName, host, port, protocol
            );

            return ResponseEntity.ok(backend);

        } catch (IllegalArgumentException e) {
            log.error("Gateway not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding backend to gateway: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update backend status
     */
    @PutMapping("/{id}/backend/{backendId}/status")
    public ResponseEntity<Map<String, Object>> updateBackendStatus(
            @PathVariable Long id,
            @PathVariable String backendId,
            @RequestBody Map<String, String> request) {
        log.info("PUT /api/api-gateway/{}/backend/{}/status", id, backendId);

        try {
            String statusStr = request.get("status");
            ReportApiGateway.BackendStatus status = ReportApiGateway.BackendStatus.valueOf(statusStr);

            apiGatewayService.updateBackendStatus(id, backendId, status);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Backend status updated");
            response.put("gatewayId", id);
            response.put("backendId", backendId);
            response.put("status", status);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Gateway or backend not found: {}, {}", id, backendId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating backend status in gateway: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create API key
     */
    @PostMapping("/{id}/api-key")
    public ResponseEntity<ReportApiGateway.ApiKey> createApiKey(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/api-gateway/{}/api-key", id);

        try {
            String name = (String) request.get("name");
            String userId = (String) request.get("userId");

            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) request.get("roles");

            ReportApiGateway.ApiKey apiKey = apiGatewayService.createApiKey(
                    id, name, userId, roles
            );

            return ResponseEntity.ok(apiKey);

        } catch (IllegalArgumentException e) {
            log.error("Gateway not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating API key for gateway: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Validate API key
     */
    @PostMapping("/{id}/api-key/validate")
    public ResponseEntity<Map<String, Object>> validateApiKey(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/api-gateway/{}/api-key/validate", id);

        try {
            String key = request.get("key");
            boolean valid = apiGatewayService.validateApiKey(id, key);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", valid);
            response.put("gatewayId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error validating API key for gateway: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Record request
     */
    @PostMapping("/{id}/request")
    public ResponseEntity<Map<String, Object>> recordRequest(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/api-gateway/{}/request", id);

        try {
            String method = (String) request.get("method");
            String path = (String) request.get("path");
            String clientIp = (String) request.get("clientIp");
            Integer statusCode = ((Number) request.get("statusCode")).intValue();
            Long responseTimeMs = ((Number) request.get("responseTimeMs")).longValue();
            String routeId = (String) request.get("routeId");

            apiGatewayService.recordRequest(id, method, path, clientIp,
                    statusCode, responseTimeMs, routeId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Request recorded");
            response.put("gatewayId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error recording request for gateway: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Check rate limit
     */
    @PostMapping("/{id}/rate-limit/check")
    public ResponseEntity<Map<String, Object>> checkRateLimit(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/api-gateway/{}/rate-limit/check", id);

        try {
            String targetType = request.get("targetType");
            String targetValue = request.get("targetValue");

            boolean allowed = apiGatewayService.checkRateLimit(id, targetType, targetValue);

            Map<String, Object> response = new HashMap<>();
            response.put("allowed", allowed);
            response.put("gatewayId", id);
            response.put("targetType", targetType);
            response.put("targetValue", targetValue);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error checking rate limit for gateway: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update circuit breaker
     */
    @PostMapping("/{id}/circuit-breaker/{backendId}")
    public ResponseEntity<Map<String, Object>> updateCircuitBreaker(
            @PathVariable Long id,
            @PathVariable String backendId,
            @RequestBody Map<String, Boolean> request) {
        log.info("POST /api/api-gateway/{}/circuit-breaker/{}", id, backendId);

        try {
            Boolean requestSuccess = request.get("requestSuccess");
            if (requestSuccess == null) {
                requestSuccess = true;
            }

            apiGatewayService.updateCircuitBreaker(id, backendId, requestSuccess);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Circuit breaker updated");
            response.put("gatewayId", id);
            response.put("backendId", backendId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error updating circuit breaker for gateway: {}", id, e);
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
        log.info("PUT /api/api-gateway/{}/metrics", id);

        try {
            Long totalRequests = ((Number) request.get("totalRequests")).longValue();
            Long successfulRequests = ((Number) request.get("successfulRequests")).longValue();
            Long failedRequests = ((Number) request.get("failedRequests")).longValue();
            Double averageLatencyMs = ((Number) request.get("averageLatencyMs")).doubleValue();
            Double throughputPerSecond = ((Number) request.get("throughputPerSecond")).doubleValue();

            apiGatewayService.updateMetrics(id, totalRequests, successfulRequests,
                    failedRequests, averageLatencyMs, throughputPerSecond);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Metrics updated");
            response.put("gatewayId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Gateway not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating metrics for gateway: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get active routes
     */
    @GetMapping("/{id}/routes")
    public ResponseEntity<Map<String, Object>> getActiveRoutes(@PathVariable Long id) {
        log.info("GET /api/api-gateway/{}/routes", id);

        try {
            return apiGatewayService.getGateway(id)
                    .map(gateway -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("gatewayId", id);
                        response.put("routes", gateway.getActiveRoutes());
                        response.put("count", gateway.getActiveRoutes().size());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching active routes for gateway: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get healthy backends
     */
    @GetMapping("/{id}/backends")
    public ResponseEntity<Map<String, Object>> getHealthyBackends(@PathVariable Long id) {
        log.info("GET /api/api-gateway/{}/backends", id);

        try {
            return apiGatewayService.getGateway(id)
                    .map(gateway -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("gatewayId", id);
                        response.put("backends", gateway.getHealthyBackends());
                        response.put("count", gateway.getHealthyBackends().size());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching healthy backends for gateway: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete gateway
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteGateway(@PathVariable Long id) {
        log.info("DELETE /api/api-gateway/{}", id);

        try {
            apiGatewayService.deleteGateway(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Gateway deleted");
            response.put("gatewayId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting gateway: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/api-gateway/stats");

        try {
            Map<String, Object> stats = apiGatewayService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching API gateway statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
