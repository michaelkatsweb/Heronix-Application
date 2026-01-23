package com.heronix.controller;

import com.heronix.dto.ReportMicroservices;
import com.heronix.service.ReportMicroservicesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Report Microservices API Controller
 *
 * REST API endpoints for microservices and service mesh management.
 *
 * Endpoints:
 * - POST /api/microservices - Create mesh
 * - GET /api/microservices/{id} - Get mesh
 * - POST /api/microservices/{id}/start - Start mesh
 * - POST /api/microservices/{id}/stop - Stop mesh
 * - POST /api/microservices/{id}/service - Register service
 * - DELETE /api/microservices/{id}/service/{serviceId} - Deregister service
 * - PUT /api/microservices/{id}/service/{serviceId}/status - Update service status
 * - POST /api/microservices/{id}/communication - Record communication
 * - POST /api/microservices/{id}/trace - Record trace
 * - POST /api/microservices/{id}/health-check/{serviceId} - Perform health check
 * - PUT /api/microservices/{id}/service/{serviceId}/config - Update service config
 * - POST /api/microservices/{id}/policy - Add mesh policy
 * - POST /api/microservices/{id}/metrics/update - Update metrics
 * - GET /api/microservices/{id}/services - Get healthy services
 * - DELETE /api/microservices/{id} - Delete mesh
 * - GET /api/microservices/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 95 - Report Microservices & Service Mesh
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/microservices")
@RequiredArgsConstructor
@Slf4j
public class ReportMicroservicesApiController {

    private final ReportMicroservicesService microservicesService;

    /**
     * Create mesh
     */
    @PostMapping
    public ResponseEntity<ReportMicroservices> createMesh(@RequestBody ReportMicroservices mesh) {
        log.info("POST /api/microservices - Creating mesh: {}", mesh.getMeshName());

        try {
            ReportMicroservices created = microservicesService.createMesh(mesh);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating mesh", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get mesh
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportMicroservices> getMesh(@PathVariable Long id) {
        log.info("GET /api/microservices/{}", id);

        try {
            return microservicesService.getMesh(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching mesh: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Start mesh
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<Map<String, Object>> startMesh(@PathVariable Long id) {
        log.info("POST /api/microservices/{}/start", id);

        try {
            microservicesService.startMesh(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Mesh started");
            response.put("meshId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Mesh not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error starting mesh: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Stop mesh
     */
    @PostMapping("/{id}/stop")
    public ResponseEntity<Map<String, Object>> stopMesh(@PathVariable Long id) {
        log.info("POST /api/microservices/{}/stop", id);

        try {
            microservicesService.stopMesh(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Mesh stopped");
            response.put("meshId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Mesh not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error stopping mesh: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Register service
     */
    @PostMapping("/{id}/service")
    public ResponseEntity<ReportMicroservices.MicroService> registerService(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/microservices/{}/service", id);

        try {
            String serviceName = (String) request.get("serviceName");
            String serviceType = (String) request.get("serviceType");
            String host = (String) request.get("host");
            Integer port = ((Number) request.get("port")).intValue();
            String protocol = (String) request.get("protocol");

            ReportMicroservices.MicroService service = microservicesService.registerService(
                    id, serviceName, serviceType, host, port, protocol
            );

            return ResponseEntity.ok(service);

        } catch (IllegalArgumentException e) {
            log.error("Mesh not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error registering service in mesh: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Deregister service
     */
    @DeleteMapping("/{id}/service/{serviceId}")
    public ResponseEntity<Map<String, Object>> deregisterService(
            @PathVariable Long id,
            @PathVariable String serviceId) {
        log.info("DELETE /api/microservices/{}/service/{}", id, serviceId);

        try {
            microservicesService.deregisterService(id, serviceId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Service deregistered");
            response.put("meshId", id);
            response.put("serviceId", serviceId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Mesh not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error deregistering service from mesh: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update service status
     */
    @PutMapping("/{id}/service/{serviceId}/status")
    public ResponseEntity<Map<String, Object>> updateServiceStatus(
            @PathVariable Long id,
            @PathVariable String serviceId,
            @RequestBody Map<String, String> request) {
        log.info("PUT /api/microservices/{}/service/{}/status", id, serviceId);

        try {
            String statusStr = request.get("status");
            ReportMicroservices.ServiceStatus status = ReportMicroservices.ServiceStatus.valueOf(statusStr);

            microservicesService.updateServiceStatus(id, serviceId, status);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Service status updated");
            response.put("meshId", id);
            response.put("serviceId", serviceId);
            response.put("status", status);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Mesh not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating service status in mesh: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Record communication
     */
    @PostMapping("/{id}/communication")
    public ResponseEntity<Map<String, Object>> recordCommunication(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/microservices/{}/communication", id);

        try {
            String sourceServiceId = (String) request.get("sourceServiceId");
            String targetServiceId = (String) request.get("targetServiceId");
            String protocolStr = (String) request.get("protocol");
            Long requestSize = ((Number) request.get("requestSize")).longValue();
            Long responseSize = ((Number) request.get("responseSize")).longValue();
            Long latencyMs = ((Number) request.get("latencyMs")).longValue();
            Boolean success = (Boolean) request.get("success");
            String traceId = (String) request.get("traceId");

            ReportMicroservices.CommunicationProtocol protocol =
                    ReportMicroservices.CommunicationProtocol.valueOf(protocolStr);

            microservicesService.recordCommunication(id, sourceServiceId, targetServiceId,
                    protocol, requestSize, responseSize, latencyMs,
                    success != null ? success : true, traceId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Communication recorded");
            response.put("meshId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Mesh not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error recording communication in mesh: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Record trace
     */
    @PostMapping("/{id}/trace")
    public ResponseEntity<Map<String, Object>> recordTrace(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/microservices/{}/trace", id);

        try {
            String serviceName = (String) request.get("serviceName");
            String operation = (String) request.get("operation");
            Long durationMs = ((Number) request.get("durationMs")).longValue();
            Boolean success = (Boolean) request.get("success");
            String traceId = (String) request.get("traceId");
            String parentSpanId = (String) request.get("parentSpanId");

            microservicesService.recordTrace(id, serviceName, operation, durationMs,
                    success != null ? success : true, traceId, parentSpanId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Trace recorded");
            response.put("meshId", id);
            response.put("traceId", traceId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Mesh not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error recording trace in mesh: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Perform health check
     */
    @PostMapping("/{id}/health-check/{serviceId}")
    public ResponseEntity<Map<String, Object>> performHealthCheck(
            @PathVariable Long id,
            @PathVariable String serviceId) {
        log.info("POST /api/microservices/{}/health-check/{}", id, serviceId);

        try {
            microservicesService.performHealthCheck(id, serviceId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Health check performed");
            response.put("meshId", id);
            response.put("serviceId", serviceId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Mesh or service not found: {}, {}", id, serviceId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error performing health check in mesh: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update service config
     */
    @PutMapping("/{id}/service/{serviceId}/config")
    public ResponseEntity<Map<String, Object>> updateServiceConfig(
            @PathVariable Long id,
            @PathVariable String serviceId,
            @RequestBody Map<String, Object> properties) {
        log.info("PUT /api/microservices/{}/service/{}/config", id, serviceId);

        try {
            microservicesService.updateServiceConfig(id, serviceId, properties);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Service configuration updated");
            response.put("meshId", id);
            response.put("serviceId", serviceId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Mesh not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating service config in mesh: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add mesh policy
     */
    @PostMapping("/{id}/policy")
    public ResponseEntity<Map<String, Object>> addMeshPolicy(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/microservices/{}/policy", id);

        try {
            String policyName = (String) request.get("policyName");
            String policyType = (String) request.get("policyType");
            String targetService = (String) request.get("targetService");

            @SuppressWarnings("unchecked")
            Map<String, Object> policyConfig = (Map<String, Object>) request.get("policyConfig");

            microservicesService.addMeshPolicy(id, policyName, policyType,
                    targetService, policyConfig);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Mesh policy added");
            response.put("meshId", id);
            response.put("policyName", policyName);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Mesh not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding mesh policy: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update metrics
     */
    @PostMapping("/{id}/metrics/update")
    public ResponseEntity<Map<String, Object>> updateMetrics(@PathVariable Long id) {
        log.info("POST /api/microservices/{}/metrics/update", id);

        try {
            microservicesService.updateMetrics(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Metrics updated");
            response.put("meshId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Mesh not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating metrics for mesh: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get healthy services
     */
    @GetMapping("/{id}/services")
    public ResponseEntity<Map<String, Object>> getHealthyServices(@PathVariable Long id) {
        log.info("GET /api/microservices/{}/services", id);

        try {
            return microservicesService.getMesh(id)
                    .map(mesh -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("meshId", id);
                        response.put("services", mesh.getHealthyServices());
                        response.put("count", mesh.getHealthyServices().size());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching healthy services for mesh: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete mesh
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteMesh(@PathVariable Long id) {
        log.info("DELETE /api/microservices/{}", id);

        try {
            microservicesService.deleteMesh(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Mesh deleted");
            response.put("meshId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting mesh: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/microservices/stats");

        try {
            Map<String, Object> stats = microservicesService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching microservices statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
