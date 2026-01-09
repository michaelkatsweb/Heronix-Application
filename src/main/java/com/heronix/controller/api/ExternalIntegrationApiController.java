package com.heronix.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * REST API Controller for External System Integration
 *
 * Manages integrations with external educational platforms and services:
 * - Learning Management Systems (Canvas, Moodle, Blackboard)
 * - Google Classroom and Google Workspace
 * - Microsoft Teams for Education
 * - State reporting systems
 * - Assessment platforms (MAP, PARCC, SBAC)
 * - Library management systems
 * - Payment processors
 * - Communication platforms (Remind, ClassDojo)
 *
 * Integration Features:
 * - OAuth 2.0 authentication
 * - API credential management
 * - Data synchronization scheduling
 * - Bidirectional data sync
 * - Field mapping configuration
 * - Sync conflict resolution
 * - Integration health monitoring
 * - Sync audit logs
 *
 * Supported Integration Types:
 * - LMS: Canvas, Moodle, Blackboard, Schoology
 * - Communication: Google Classroom, Microsoft Teams
 * - Assessment: NWEA MAP, Renaissance, iReady
 * - State: State reporting portals
 * - Payment: SchoolPay, MySchoolBucks
 * - Library: Destiny, Follett
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since December 30, 2025 - Phase 37
 */
@RestController
@RequestMapping("/api/integrations")
@RequiredArgsConstructor
public class ExternalIntegrationApiController {

    private final com.heronix.service.IntegrationService integrationService;

    // ========================================================================
    // INTEGRATION CONFIGURATION
    // ========================================================================

    /**
     * Get all configured integrations
     *
     * GET /api/integrations
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllIntegrations() {
        try {
            var integrations = integrationService.getAllIntegrations();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("integrations", integrations);
            response.put("count", integrations.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve integrations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Configure new integration
     *
     * POST /api/integrations
     *
     * Request Body:
     * {
     *   "type": "GOOGLE_CLASSROOM",
     *   "name": "Main Google Classroom",
     *   "credentials": {
     *     "clientId": "xxx",
     *     "clientSecret": "xxx",
     *     "refreshToken": "xxx"
     *   },
     *   "settings": {
     *     "autoSync": true,
     *     "syncInterval": "daily",
     *     "syncDirection": "bidirectional"
     *   }
     * }
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createIntegration(
            @RequestBody Map<String, Object> requestBody) {

        try {
            String type = (String) requestBody.get("type");
            String name = (String) requestBody.get("name");
            @SuppressWarnings("unchecked")
            Map<String, Object> credentials = (Map<String, Object>) requestBody.get("credentials");
            @SuppressWarnings("unchecked")
            Map<String, Object> settings = (Map<String, Object>) requestBody.get("settings");

            var integrationData = integrationService.createIntegration(type, name, credentials, settings);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("integration", integrationData);
            response.put("message", "Integration configured successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to create integration: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get integration by ID
     *
     * GET /api/integrations/{integrationId}
     */
    @GetMapping("/{integrationId}")
    public ResponseEntity<Map<String, Object>> getIntegration(@PathVariable String integrationId) {
        try {
            var integration = integrationService.getIntegration(integrationId);

            if (integration.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Integration not found: " + integrationId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("integration", integration.get());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve integration: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Update integration configuration
     *
     * PUT /api/integrations/{integrationId}
     */
    @PutMapping("/{integrationId}")
    public ResponseEntity<Map<String, Object>> updateIntegration(
            @PathVariable String integrationId,
            @RequestBody Map<String, Object> requestBody) {

        try {
            String type = (String) requestBody.get("type");
            String name = (String) requestBody.get("name");
            @SuppressWarnings("unchecked")
            Map<String, Object> credentials = (Map<String, Object>) requestBody.get("credentials");
            @SuppressWarnings("unchecked")
            Map<String, Object> settings = (Map<String, Object>) requestBody.get("settings");

            var integrationData = integrationService.updateIntegration(integrationId, type, name, credentials, settings);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("integration", integrationData);
            response.put("message", "Integration updated successfully");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to update integration: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Delete integration
     *
     * DELETE /api/integrations/{integrationId}
     */
    @DeleteMapping("/{integrationId}")
    public ResponseEntity<Map<String, Object>> deleteIntegration(@PathVariable String integrationId) {
        try {
            integrationService.deleteIntegration(integrationId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Integration deleted successfully");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to delete integration: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // DATA SYNCHRONIZATION
    // ========================================================================

    /**
     * Trigger manual data sync
     *
     * POST /api/integrations/{integrationId}/sync
     *
     * Request Body:
     * {
     *   "entities": ["students", "courses", "grades"],
     *   "startDate": "2025-01-01",
     *   "endDate": "2025-12-31"
     * }
     */
    @PostMapping("/{integrationId}/sync")
    public ResponseEntity<Map<String, Object>> triggerSync(
            @PathVariable String integrationId,
            @RequestBody Map<String, Object> requestBody) {

        try {
            @SuppressWarnings("unchecked")
            List<String> entities = (List<String>) requestBody.get("entities");
            String startDate = (String) requestBody.get("startDate");
            String endDate = (String) requestBody.get("endDate");

            var syncJob = integrationService.triggerSync(integrationId, entities, startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("syncJob", syncJob);
            response.put("message", "Sync triggered successfully");

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to trigger sync: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get sync job status
     *
     * GET /api/integrations/sync-jobs/{jobId}
     */
    @GetMapping("/sync-jobs/{jobId}")
    public ResponseEntity<Map<String, Object>> getSyncJobStatus(@PathVariable String jobId) {
        try {
            var syncJob = integrationService.getSyncJobStatus(jobId);

            if (syncJob.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Sync job not found: " + jobId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("syncJob", syncJob.get());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve sync status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get sync history for integration
     *
     * GET /api/integrations/{integrationId}/sync-history?limit=20
     */
    @GetMapping("/{integrationId}/sync-history")
    public ResponseEntity<Map<String, Object>> getSyncHistory(
            @PathVariable String integrationId,
            @RequestParam(defaultValue = "20") int limit) {

        try {
            var syncHistory = integrationService.getSyncHistory(integrationId, limit);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("integrationId", integrationId);
            response.put("syncHistory", syncHistory);
            response.put("count", syncHistory.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve sync history: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // FIELD MAPPING
    // ========================================================================

    /**
     * Configure field mapping for integration
     *
     * POST /api/integrations/{integrationId}/field-mapping
     *
     * Request Body:
     * {
     *   "entity": "student",
     *   "mappings": {
     *     "firstName": "first_name",
     *     "lastName": "last_name",
     *     "studentId": "student_number",
     *     "email": "email_address"
     *   }
     * }
     */
    @PostMapping("/{integrationId}/field-mapping")
    public ResponseEntity<Map<String, Object>> configureFieldMapping(
            @PathVariable String integrationId,
            @RequestBody Map<String, Object> requestBody) {

        try {
            String entity = (String) requestBody.get("entity");
            @SuppressWarnings("unchecked")
            Map<String, String> mappings = (Map<String, String>) requestBody.get("mappings");

            var fieldMapping = integrationService.configureFieldMapping(integrationId, entity, mappings);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("fieldMapping", fieldMapping);
            response.put("message", "Field mapping configured successfully");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to configure field mapping: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get field mapping configuration
     *
     * GET /api/integrations/{integrationId}/field-mapping?entity=student
     */
    @GetMapping("/{integrationId}/field-mapping")
    public ResponseEntity<Map<String, Object>> getFieldMapping(
            @PathVariable String integrationId,
            @RequestParam(required = false) String entity) {

        try {
            var fieldMapping = integrationService.getFieldMapping(integrationId, entity);

            if (fieldMapping.isEmpty() && entity != null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Field mapping not found for entity: " + entity);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("fieldMapping", fieldMapping.orElse(null));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve field mapping: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // INTEGRATION HEALTH
    // ========================================================================

    /**
     * Test integration connection
     *
     * POST /api/integrations/{integrationId}/test-connection
     */
    @PostMapping("/{integrationId}/test-connection")
    public ResponseEntity<Map<String, Object>> testConnection(@PathVariable String integrationId) {
        try {
            var testResult = integrationService.testConnection(integrationId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("testResult", testResult);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to test connection: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get integration health status
     *
     * GET /api/integrations/{integrationId}/health
     */
    @GetMapping("/{integrationId}/health")
    public ResponseEntity<Map<String, Object>> getIntegrationHealth(
            @PathVariable String integrationId) {

        try {
            var health = integrationService.getIntegrationHealth(integrationId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("health", health);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve health status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // SUPPORTED INTEGRATIONS CATALOG
    // ========================================================================

    /**
     * Get list of supported integration types
     *
     * GET /api/integrations/supported-types
     */
    @GetMapping("/supported-types")
    public ResponseEntity<Map<String, Object>> getSupportedIntegrationTypes() {
        try {
            List<Map<String, Object>> types = Arrays.asList(
                createIntegrationType("GOOGLE_CLASSROOM", "Google Classroom", "LMS", "Sync courses, assignments, and grades with Google Classroom"),
                createIntegrationType("CANVAS", "Canvas LMS", "LMS", "Full bidirectional sync with Canvas Learning Management System"),
                createIntegrationType("MOODLE", "Moodle", "LMS", "Integration with Moodle LMS platform"),
                createIntegrationType("MICROSOFT_TEAMS", "Microsoft Teams", "Communication", "Sync with Microsoft Teams for Education"),
                createIntegrationType("CLEVER", "Clever", "SSO", "Single Sign-On and rostering via Clever"),
                createIntegrationType("CLASSLINK", "ClassLink", "SSO", "Single Sign-On and rostering via ClassLink"),
                createIntegrationType("POWERSCHOOL", "PowerSchool", "SIS", "Sync with PowerSchool SIS"),
                createIntegrationType("SKYWARD", "Skyward", "SIS", "Integration with Skyward SIS"),
                createIntegrationType("STATE_REPORTING", "State Reporting", "Compliance", "Submit data to state reporting systems")
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("supportedTypes", types);
            response.put("count", types.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve supported types: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private Map<String, Object> createIntegrationType(String code, String name, String category, String description) {
        Map<String, Object> type = new HashMap<>();
        type.put("code", code);
        type.put("name", name);
        type.put("category", category);
        type.put("description", description);
        return type;
    }
}
