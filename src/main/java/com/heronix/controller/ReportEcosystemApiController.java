package com.heronix.controller;

import com.heronix.dto.ReportEcosystem;
import com.heronix.service.ReportEcosystemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Report Ecosystem API Controller
 *
 * REST API endpoints for ecosystem and integration hub management.
 *
 * Endpoints:
 * - POST /api/ecosystem - Create ecosystem
 * - GET /api/ecosystem/{id} - Get ecosystem
 * - POST /api/ecosystem/{id}/component - Register component
 * - POST /api/ecosystem/{id}/integration - Register integration
 * - POST /api/ecosystem/{id}/service - Register service
 * - PUT /api/ecosystem/{id}/component/{componentId}/status - Update component status
 * - PUT /api/ecosystem/{id}/integration/{integrationId}/status - Update integration status
 * - POST /api/ecosystem/{id}/event - Publish event
 * - POST /api/ecosystem/{id}/event/{eventId}/process - Process event
 * - POST /api/ecosystem/{id}/integration/{integrationId}/call - Record integration call
 * - POST /api/ecosystem/{id}/metrics/resource - Update resource metrics
 * - POST /api/ecosystem/{id}/metrics/performance - Update performance metrics
 * - POST /api/ecosystem/{id}/health - Run health check
 * - PUT /api/ecosystem/{id}/config - Update configuration
 * - POST /api/ecosystem/{id}/start - Start ecosystem
 * - POST /api/ecosystem/{id}/stop - Stop ecosystem
 * - DELETE /api/ecosystem/{id} - Delete ecosystem
 * - GET /api/ecosystem/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 90 - Report Ecosystem & Integration Hub
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/ecosystem")
@RequiredArgsConstructor
@Slf4j
public class ReportEcosystemApiController {

    private final ReportEcosystemService ecosystemService;

    /**
     * Create ecosystem
     */
    @PostMapping
    public ResponseEntity<ReportEcosystem> createEcosystem(@RequestBody ReportEcosystem ecosystem) {
        log.info("POST /api/ecosystem - Creating ecosystem: {}", ecosystem.getEcosystemName());

        try {
            ReportEcosystem created = ecosystemService.createEcosystem(ecosystem);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating ecosystem", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get ecosystem
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportEcosystem> getEcosystem(@PathVariable Long id) {
        log.info("GET /api/ecosystem/{}", id);

        try {
            return ecosystemService.getEcosystem(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching ecosystem: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Register component
     */
    @PostMapping("/{id}/component")
    public ResponseEntity<Map<String, Object>> registerComponent(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/ecosystem/{}/component", id);

        try {
            String componentName = request.get("componentName");
            String componentType = request.get("componentType");
            String version = request.get("version");

            ecosystemService.registerComponent(id, componentName, componentType, version);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Component registered");
            response.put("ecosystemId", id);
            response.put("componentName", componentName);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Ecosystem not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error registering component in ecosystem: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Register integration
     */
    @PostMapping("/{id}/integration")
    public ResponseEntity<ReportEcosystem.Integration> registerIntegration(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/ecosystem/{}/integration", id);

        try {
            String integrationName = request.get("integrationName");
            String typeStr = request.get("type");
            String endpoint = request.get("endpoint");

            ReportEcosystem.IntegrationType type = ReportEcosystem.IntegrationType.valueOf(typeStr);

            ReportEcosystem.Integration integration = ecosystemService.registerIntegration(
                    id, integrationName, type, endpoint
            );

            return ResponseEntity.ok(integration);

        } catch (IllegalArgumentException e) {
            log.error("Ecosystem not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error registering integration in ecosystem: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Register service
     */
    @PostMapping("/{id}/service")
    public ResponseEntity<Map<String, Object>> registerService(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/ecosystem/{}/service", id);

        try {
            String serviceName = (String) request.get("serviceName");
            String serviceType = (String) request.get("serviceType");
            String host = (String) request.get("host");
            Integer port = ((Number) request.get("port")).intValue();

            ecosystemService.registerService(id, serviceName, serviceType, host, port);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Service registered");
            response.put("ecosystemId", id);
            response.put("serviceName", serviceName);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Ecosystem not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error registering service in ecosystem: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update component status
     */
    @PutMapping("/{id}/component/{componentId}/status")
    public ResponseEntity<Map<String, Object>> updateComponentStatus(
            @PathVariable Long id,
            @PathVariable String componentId,
            @RequestBody Map<String, String> request) {
        log.info("PUT /api/ecosystem/{}/component/{}/status", id, componentId);

        try {
            String statusStr = request.get("status");
            ReportEcosystem.ComponentStatus status = ReportEcosystem.ComponentStatus.valueOf(statusStr);

            ecosystemService.updateComponentStatus(id, componentId, status);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Component status updated");
            response.put("ecosystemId", id);
            response.put("componentId", componentId);
            response.put("status", status);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Ecosystem or component not found: {}, {}", id, componentId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating component status in ecosystem: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update integration status
     */
    @PutMapping("/{id}/integration/{integrationId}/status")
    public ResponseEntity<Map<String, Object>> updateIntegrationStatus(
            @PathVariable Long id,
            @PathVariable String integrationId,
            @RequestBody Map<String, String> request) {
        log.info("PUT /api/ecosystem/{}/integration/{}/status", id, integrationId);

        try {
            String statusStr = request.get("status");
            ReportEcosystem.ComponentStatus status = ReportEcosystem.ComponentStatus.valueOf(statusStr);

            ecosystemService.updateIntegrationStatus(id, integrationId, status);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Integration status updated");
            response.put("ecosystemId", id);
            response.put("integrationId", integrationId);
            response.put("status", status);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Ecosystem or integration not found: {}, {}", id, integrationId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating integration status in ecosystem: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Publish event
     */
    @PostMapping("/{id}/event")
    public ResponseEntity<Map<String, Object>> publishEvent(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/ecosystem/{}/event", id);

        try {
            String eventType = (String) request.get("eventType");
            String source = (String) request.get("source");
            String target = (String) request.get("target");

            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) request.get("payload");

            ecosystemService.publishEvent(id, eventType, source, target, payload);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Event published");
            response.put("ecosystemId", id);
            response.put("eventType", eventType);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Ecosystem not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            log.error("Invalid state for event publishing: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error publishing event in ecosystem: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Process event
     */
    @PostMapping("/{id}/event/{eventId}/process")
    public ResponseEntity<Map<String, Object>> processEvent(
            @PathVariable Long id,
            @PathVariable String eventId,
            @RequestParam boolean success) {
        log.info("POST /api/ecosystem/{}/event/{}/process?success={}", id, eventId, success);

        try {
            ecosystemService.processEvent(id, eventId, success);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Event processed");
            response.put("ecosystemId", id);
            response.put("eventId", eventId);
            response.put("success", success);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Ecosystem not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error processing event in ecosystem: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Record integration call
     */
    @PostMapping("/{id}/integration/{integrationId}/call")
    public ResponseEntity<Map<String, Object>> callIntegration(
            @PathVariable Long id,
            @PathVariable String integrationId,
            @RequestParam boolean success) {
        log.info("POST /api/ecosystem/{}/integration/{}/call?success={}", id, integrationId, success);

        try {
            ecosystemService.callIntegration(id, integrationId, success);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Integration call recorded");
            response.put("ecosystemId", id);
            response.put("integrationId", integrationId);
            response.put("success", success);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Ecosystem not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error recording integration call in ecosystem: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update resource metrics
     */
    @PostMapping("/{id}/metrics/resource")
    public ResponseEntity<Map<String, Object>> updateResourceMetrics(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/ecosystem/{}/metrics/resource", id);

        try {
            Double cpuUsage = ((Number) request.get("cpuUsage")).doubleValue();
            Long memoryUsed = ((Number) request.get("memoryUsed")).longValue();
            Long memoryTotal = ((Number) request.get("memoryTotal")).longValue();
            Long diskUsed = ((Number) request.get("diskUsed")).longValue();
            Long diskTotal = ((Number) request.get("diskTotal")).longValue();
            Integer activeThreads = ((Number) request.get("activeThreads")).intValue();
            Integer maxThreads = ((Number) request.get("maxThreads")).intValue();

            ecosystemService.updateResourceMetrics(id, cpuUsage, memoryUsed, memoryTotal,
                    diskUsed, diskTotal, activeThreads, maxThreads);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Resource metrics updated");
            response.put("ecosystemId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Ecosystem not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating resource metrics for ecosystem: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update performance metrics
     */
    @PostMapping("/{id}/metrics/performance")
    public ResponseEntity<Map<String, Object>> updatePerformanceMetrics(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/ecosystem/{}/metrics/performance", id);

        try {
            Long totalRequests = ((Number) request.get("totalRequests")).longValue();
            Long successfulRequests = ((Number) request.get("successfulRequests")).longValue();
            Long failedRequests = ((Number) request.get("failedRequests")).longValue();
            Double avgResponseTime = ((Number) request.get("avgResponseTime")).doubleValue();
            Double p95ResponseTime = ((Number) request.get("p95ResponseTime")).doubleValue();

            ecosystemService.updatePerformanceMetrics(id, totalRequests, successfulRequests,
                    failedRequests, avgResponseTime, p95ResponseTime);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Performance metrics updated");
            response.put("ecosystemId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Ecosystem not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating performance metrics for ecosystem: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Run health check
     */
    @PostMapping("/{id}/health")
    public ResponseEntity<Map<String, Object>> runHealthCheck(@PathVariable Long id) {
        log.info("POST /api/ecosystem/{}/health", id);

        try {
            ecosystemService.runHealthCheck(id);

            ReportEcosystem ecosystem = ecosystemService.getEcosystem(id).orElse(null);
            if (ecosystem == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Health check completed");
            response.put("ecosystemId", id);
            response.put("status", ecosystem.getStatus());
            response.put("health", ecosystem.getHealth());
            response.put("healthScore", ecosystem.getHealthScore());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Ecosystem not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error running health check for ecosystem: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update configuration
     */
    @PutMapping("/{id}/config")
    public ResponseEntity<Map<String, Object>> updateConfiguration(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("PUT /api/ecosystem/{}/config", id);

        try {
            String key = (String) request.get("key");
            Object value = request.get("value");

            ecosystemService.updateConfiguration(id, key, value);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Configuration updated");
            response.put("ecosystemId", id);
            response.put("key", key);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Ecosystem not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            log.error("Invalid state for configuration update: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error updating configuration for ecosystem: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Start ecosystem
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<Map<String, Object>> startEcosystem(@PathVariable Long id) {
        log.info("POST /api/ecosystem/{}/start", id);

        try {
            ecosystemService.startEcosystem(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Ecosystem started");
            response.put("ecosystemId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Ecosystem not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error starting ecosystem: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Stop ecosystem
     */
    @PostMapping("/{id}/stop")
    public ResponseEntity<Map<String, Object>> stopEcosystem(@PathVariable Long id) {
        log.info("POST /api/ecosystem/{}/stop", id);

        try {
            ecosystemService.stopEcosystem(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Ecosystem stopped");
            response.put("ecosystemId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Ecosystem not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error stopping ecosystem: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete ecosystem
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteEcosystem(@PathVariable Long id) {
        log.info("DELETE /api/ecosystem/{}", id);

        try {
            ecosystemService.deleteEcosystem(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Ecosystem deleted");
            response.put("ecosystemId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting ecosystem: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/ecosystem/stats");

        try {
            Map<String, Object> stats = ecosystemService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching ecosystem statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
