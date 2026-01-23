package com.heronix.controller;

import com.heronix.dto.ReportIntegration;
import com.heronix.service.ReportIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Report Integration API Controller
 *
 * REST API endpoints for external system integrations.
 *
 * Endpoints:
 * - POST /api/integrations - Create integration
 * - GET /api/integrations/{id} - Get integration
 * - GET /api/integrations - Get all integrations
 * - PUT /api/integrations/{id} - Update integration
 * - DELETE /api/integrations/{id} - Delete integration
 * - GET /api/integrations/report/{reportId} - Get integrations by report
 * - GET /api/integrations/type/{type} - Get integrations by type
 * - GET /api/integrations/status/{status} - Get integrations by status
 * - POST /api/integrations/{id}/test - Test connection
 * - POST /api/integrations/{id}/execute - Execute request
 * - POST /api/integrations/{id}/sync - Synchronize data
 * - POST /api/integrations/{id}/webhook - Send webhook
 * - GET /api/integrations/{id}/health - Health check
 * - GET /api/integrations/{id}/requests - Get request history
 * - GET /api/integrations/{id}/events - Get event history
 * - GET /api/integrations/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 76 - Report Integration & External APIs
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/integrations")
@RequiredArgsConstructor
@Slf4j
public class ReportIntegrationApiController {

    private final ReportIntegrationService integrationService;

    /**
     * Create new integration
     *
     * @param integration Integration configuration
     * @return Created integration
     */
    @PostMapping
    public ResponseEntity<ReportIntegration> createIntegration(@RequestBody ReportIntegration integration) {
        log.info("POST /api/integrations - Creating integration: {}", integration.getName());

        try {
            ReportIntegration created = integrationService.createIntegration(integration);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating integration", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get integration by ID
     *
     * @param id Integration ID
     * @return Integration details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportIntegration> getIntegration(@PathVariable Long id) {
        log.info("GET /api/integrations/{}", id);

        try {
            return integrationService.getIntegration(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching integration: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all integrations
     *
     * @return List of all integrations
     */
    @GetMapping
    public ResponseEntity<List<ReportIntegration>> getAllIntegrations() {
        log.info("GET /api/integrations");

        try {
            List<ReportIntegration> integrations = integrationService.getAllIntegrations();
            return ResponseEntity.ok(integrations);

        } catch (Exception e) {
            log.error("Error fetching integrations", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update integration
     *
     * @param id Integration ID
     * @param integration Updated integration data
     * @return Updated integration
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReportIntegration> updateIntegration(
            @PathVariable Long id,
            @RequestBody ReportIntegration integration) {
        log.info("PUT /api/integrations/{}", id);

        try {
            ReportIntegration updated = integrationService.updateIntegration(id, integration);
            return ResponseEntity.ok(updated);

        } catch (IllegalArgumentException e) {
            log.error("Integration not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating integration: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete integration
     *
     * @param id Integration ID
     * @return Success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIntegration(@PathVariable Long id) {
        log.info("DELETE /api/integrations/{}", id);

        try {
            integrationService.deleteIntegration(id);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Error deleting integration: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get integrations by report
     *
     * @param reportId Report ID
     * @return List of integrations for the report
     */
    @GetMapping("/report/{reportId}")
    public ResponseEntity<List<ReportIntegration>> getIntegrationsByReport(@PathVariable Long reportId) {
        log.info("GET /api/integrations/report/{}", reportId);

        try {
            List<ReportIntegration> integrations = integrationService.getIntegrationsByReport(reportId);
            return ResponseEntity.ok(integrations);

        } catch (Exception e) {
            log.error("Error fetching integrations for report: {}", reportId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get integrations by type
     *
     * @param type Integration type
     * @return List of integrations of specified type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<ReportIntegration>> getIntegrationsByType(@PathVariable String type) {
        log.info("GET /api/integrations/type/{}", type);

        try {
            ReportIntegration.IntegrationType integrationType =
                    ReportIntegration.IntegrationType.valueOf(type.toUpperCase());
            List<ReportIntegration> integrations = integrationService.getIntegrationsByType(integrationType);
            return ResponseEntity.ok(integrations);

        } catch (IllegalArgumentException e) {
            log.error("Invalid integration type: {}", type);
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error fetching integrations by type: {}", type, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get integrations by status
     *
     * @param status Integration status
     * @return List of integrations with specified status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReportIntegration>> getIntegrationsByStatus(@PathVariable String status) {
        log.info("GET /api/integrations/status/{}", status);

        try {
            ReportIntegration.IntegrationStatus integrationStatus =
                    ReportIntegration.IntegrationStatus.valueOf(status.toUpperCase());
            List<ReportIntegration> integrations = integrationService.getIntegrationsByStatus(integrationStatus);
            return ResponseEntity.ok(integrations);

        } catch (IllegalArgumentException e) {
            log.error("Invalid integration status: {}", status);
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error fetching integrations by status: {}", status, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Test integration connection
     *
     * @param id Integration ID
     * @return Connection test result
     */
    @PostMapping("/{id}/test")
    public ResponseEntity<Map<String, Object>> testConnection(@PathVariable Long id) {
        log.info("POST /api/integrations/{}/test", id);

        try {
            Map<String, Object> result = integrationService.testConnection(id);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.error("Integration not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error testing connection: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Execute integration request
     *
     * @param id Integration ID
     * @param requestData Request data
     * @return Request response
     */
    @PostMapping("/{id}/execute")
    public ResponseEntity<Map<String, Object>> executeRequest(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestData) {
        log.info("POST /api/integrations/{}/execute", id);

        try {
            Map<String, Object> response = integrationService.executeRequest(id, requestData);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Integration not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            log.error("Integration not ready: {}", id);
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error executing request: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Synchronize data
     *
     * @param id Integration ID
     * @return Sync result
     */
    @PostMapping("/{id}/sync")
    public ResponseEntity<Map<String, Object>> synchronizeData(@PathVariable Long id) {
        log.info("POST /api/integrations/{}/sync", id);

        try {
            Map<String, Object> result = integrationService.synchronizeData(id);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.error("Integration not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            log.error("Sync not enabled: {}", id);
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error synchronizing data: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Send webhook
     *
     * @param id Integration ID
     * @param webhookData Webhook data containing eventType and payload
     * @return Webhook result
     */
    @PostMapping("/{id}/webhook")
    public ResponseEntity<Map<String, Object>> sendWebhook(
            @PathVariable Long id,
            @RequestBody Map<String, Object> webhookData) {
        log.info("POST /api/integrations/{}/webhook", id);

        try {
            String eventType = (String) webhookData.get("eventType");
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) webhookData.get("payload");

            if (eventType == null) {
                return ResponseEntity.badRequest().build();
            }

            Map<String, Object> result = integrationService.sendWebhook(id, eventType, payload);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.error("Integration not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            log.error("Webhooks not supported: {}", id);
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error sending webhook: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Perform health check
     *
     * @param id Integration ID
     * @return Health check result
     */
    @GetMapping("/{id}/health")
    public ResponseEntity<Map<String, Object>> performHealthCheck(@PathVariable Long id) {
        log.info("GET /api/integrations/{}/health", id);

        try {
            Map<String, Object> healthReport = integrationService.performHealthCheck(id);
            return ResponseEntity.ok(healthReport);

        } catch (IllegalArgumentException e) {
            log.error("Integration not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error performing health check: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get request history
     *
     * @param id Integration ID
     * @return List of integration requests
     */
    @GetMapping("/{id}/requests")
    public ResponseEntity<List<ReportIntegration.IntegrationRequest>> getRequestHistory(@PathVariable Long id) {
        log.info("GET /api/integrations/{}/requests", id);

        try {
            List<ReportIntegration.IntegrationRequest> requests = integrationService.getRequestHistory(id);
            return ResponseEntity.ok(requests);

        } catch (Exception e) {
            log.error("Error fetching request history: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get event history
     *
     * @param id Integration ID
     * @return List of integration events
     */
    @GetMapping("/{id}/events")
    public ResponseEntity<List<ReportIntegration.IntegrationEvent>> getEventHistory(@PathVariable Long id) {
        log.info("GET /api/integrations/{}/events", id);

        try {
            List<ReportIntegration.IntegrationEvent> events = integrationService.getEventHistory(id);
            return ResponseEntity.ok(events);

        } catch (Exception e) {
            log.error("Error fetching event history: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get integration statistics
     *
     * @return Integration statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/integrations/stats");

        try {
            Map<String, Object> stats = integrationService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching integration statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
