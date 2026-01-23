package com.heronix.controller;

import com.heronix.dto.ReportMultiTenancy;
import com.heronix.service.ReportMultiTenancyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Multi-tenancy & Isolation API Controller
 *
 * REST API endpoints for multi-tenant platform and tenant management.
 *
 * Endpoints:
 * - POST /api/multitenancy - Create multi-tenancy platform
 * - GET /api/multitenancy/{id} - Get multi-tenancy platform
 * - POST /api/multitenancy/{id}/deploy - Deploy platform
 * - POST /api/multitenancy/{id}/tenant - Provision tenant
 * - POST /api/multitenancy/{id}/quota - Add resource quota
 * - POST /api/multitenancy/{id}/config - Set tenant configuration
 * - POST /api/multitenancy/{id}/usage - Record usage
 * - POST /api/multitenancy/{id}/billing - Create billing record
 * - POST /api/multitenancy/{id}/migration - Start migration
 * - POST /api/multitenancy/{id}/migration/complete - Complete migration
 * - POST /api/multitenancy/{id}/group - Create tenant group
 * - POST /api/multitenancy/{id}/suspend - Suspend tenant
 * - POST /api/multitenancy/{id}/activate - Activate tenant
 * - PUT /api/multitenancy/{id}/metrics - Update metrics
 * - DELETE /api/multitenancy/{id} - Delete platform
 * - GET /api/multitenancy/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 109 - Report Multi-tenancy & Isolation
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/multitenancy")
@RequiredArgsConstructor
@Slf4j
public class ReportMultiTenancyApiController {

    private final ReportMultiTenancyService multiTenancyService;

    /**
     * Create multi-tenancy platform
     */
    @PostMapping
    public ResponseEntity<ReportMultiTenancy> createPlatform(@RequestBody ReportMultiTenancy platform) {
        log.info("POST /api/multitenancy - Creating multi-tenancy platform: {}", platform.getPlatformName());

        try {
            ReportMultiTenancy created = multiTenancyService.createPlatform(platform);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating multi-tenancy platform", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get multi-tenancy platform
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportMultiTenancy> getPlatform(@PathVariable Long id) {
        log.info("GET /api/multitenancy/{}", id);

        try {
            return multiTenancyService.getPlatform(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching multi-tenancy platform: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Deploy platform
     */
    @PostMapping("/{id}/deploy")
    public ResponseEntity<Map<String, Object>> deployPlatform(@PathVariable Long id) {
        log.info("POST /api/multitenancy/{}/deploy", id);

        try {
            multiTenancyService.deployPlatform(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Multi-tenancy platform deployed");
            response.put("platformId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Multi-tenancy platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error deploying multi-tenancy platform: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Provision tenant
     */
    @PostMapping("/{id}/tenant")
    public ResponseEntity<ReportMultiTenancy.Tenant> provisionTenant(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/multitenancy/{}/tenant", id);

        try {
            String tenantName = request.get("tenantName");
            String domain = request.get("domain");
            String tierStr = request.get("tier");
            String contactEmail = request.get("contactEmail");

            ReportMultiTenancy.TenantTier tier = ReportMultiTenancy.TenantTier.valueOf(tierStr);

            ReportMultiTenancy.Tenant tenant = multiTenancyService.provisionTenant(
                    id, tenantName, domain, tier, contactEmail
            );

            return ResponseEntity.ok(tenant);

        } catch (IllegalArgumentException e) {
            log.error("Multi-tenancy platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error provisioning tenant in multi-tenancy platform: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add resource quota
     */
    @PostMapping("/{id}/quota")
    public ResponseEntity<ReportMultiTenancy.ResourceQuota> addQuota(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/multitenancy/{}/quota", id);

        try {
            String tenantId = (String) request.get("tenantId");
            String resourceTypeStr = (String) request.get("resourceType");
            Long quota = request.get("quota") != null ?
                    ((Number) request.get("quota")).longValue() : 1000L;
            String unit = (String) request.get("unit");

            ReportMultiTenancy.ResourceType resourceType =
                    ReportMultiTenancy.ResourceType.valueOf(resourceTypeStr);

            ReportMultiTenancy.ResourceQuota resourceQuota = multiTenancyService.addQuota(
                    id, tenantId, resourceType, quota, unit
            );

            return ResponseEntity.ok(resourceQuota);

        } catch (IllegalArgumentException e) {
            log.error("Multi-tenancy platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding quota to multi-tenancy platform: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Set tenant configuration
     */
    @PostMapping("/{id}/config")
    public ResponseEntity<ReportMultiTenancy.TenantConfiguration> setConfiguration(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/multitenancy/{}/config", id);

        try {
            String tenantId = request.get("tenantId");
            String configKey = request.get("configKey");
            String configValue = request.get("configValue");
            String configType = request.get("configType");

            ReportMultiTenancy.TenantConfiguration config = multiTenancyService.setConfiguration(
                    id, tenantId, configKey, configValue, configType
            );

            return ResponseEntity.ok(config);

        } catch (IllegalArgumentException e) {
            log.error("Multi-tenancy platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error setting configuration in multi-tenancy platform: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Record usage
     */
    @PostMapping("/{id}/usage")
    public ResponseEntity<ReportMultiTenancy.UsageRecord> recordUsage(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/multitenancy/{}/usage", id);

        try {
            String tenantId = (String) request.get("tenantId");
            String resourceTypeStr = (String) request.get("resourceType");
            Long quantity = request.get("quantity") != null ?
                    ((Number) request.get("quantity")).longValue() : 0L;
            Double cost = request.get("cost") != null ?
                    ((Number) request.get("cost")).doubleValue() : 0.0;

            ReportMultiTenancy.ResourceType resourceType =
                    ReportMultiTenancy.ResourceType.valueOf(resourceTypeStr);

            ReportMultiTenancy.UsageRecord usage = multiTenancyService.recordUsage(
                    id, tenantId, resourceType, quantity, cost
            );

            return ResponseEntity.ok(usage);

        } catch (IllegalArgumentException e) {
            log.error("Multi-tenancy platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error recording usage in multi-tenancy platform: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create billing record
     */
    @PostMapping("/{id}/billing")
    public ResponseEntity<ReportMultiTenancy.BillingRecord> createBilling(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/multitenancy/{}/billing", id);

        try {
            String tenantId = (String) request.get("tenantId");
            String periodStartStr = (String) request.get("periodStart");
            String periodEndStr = (String) request.get("periodEnd");
            Double amount = request.get("amount") != null ?
                    ((Number) request.get("amount")).doubleValue() : 0.0;

            LocalDateTime periodStart = LocalDateTime.parse(periodStartStr);
            LocalDateTime periodEnd = LocalDateTime.parse(periodEndStr);

            ReportMultiTenancy.BillingRecord billing = multiTenancyService.createBilling(
                    id, tenantId, periodStart, periodEnd, amount
            );

            return ResponseEntity.ok(billing);

        } catch (IllegalArgumentException e) {
            log.error("Multi-tenancy platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating billing in multi-tenancy platform: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Start migration
     */
    @PostMapping("/{id}/migration")
    public ResponseEntity<ReportMultiTenancy.TenantMigration> startMigration(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/multitenancy/{}/migration", id);

        try {
            String tenantId = request.get("tenantId");
            String sourceEnv = request.get("sourceEnvironment");
            String targetEnv = request.get("targetEnvironment");
            String migrationType = request.get("migrationType");

            ReportMultiTenancy.TenantMigration migration = multiTenancyService.startMigration(
                    id, tenantId, sourceEnv, targetEnv, migrationType
            );

            return ResponseEntity.ok(migration);

        } catch (IllegalArgumentException e) {
            log.error("Multi-tenancy platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error starting migration in multi-tenancy platform: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Complete migration
     */
    @PostMapping("/{id}/migration/complete")
    public ResponseEntity<Map<String, Object>> completeMigration(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/multitenancy/{}/migration/complete", id);

        try {
            String migrationId = (String) request.get("migrationId");
            Boolean success = (Boolean) request.getOrDefault("success", true);

            multiTenancyService.completeMigration(id, migrationId, success);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Migration completed");
            response.put("migrationId", migrationId);
            response.put("success", success);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Multi-tenancy platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error completing migration in multi-tenancy platform: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create tenant group
     */
    @PostMapping("/{id}/group")
    public ResponseEntity<ReportMultiTenancy.TenantGroup> createGroup(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/multitenancy/{}/group", id);

        try {
            String groupName = (String) request.get("groupName");
            String description = (String) request.get("description");
            @SuppressWarnings("unchecked")
            List<String> tenantIds = (List<String>) request.get("tenantIds");

            ReportMultiTenancy.TenantGroup group = multiTenancyService.createGroup(
                    id, groupName, description, tenantIds
            );

            return ResponseEntity.ok(group);

        } catch (IllegalArgumentException e) {
            log.error("Multi-tenancy platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating group in multi-tenancy platform: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Suspend tenant
     */
    @PostMapping("/{id}/suspend")
    public ResponseEntity<Map<String, Object>> suspendTenant(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/multitenancy/{}/suspend", id);

        try {
            String tenantId = request.get("tenantId");
            String reason = request.get("reason");

            multiTenancyService.suspendTenant(id, tenantId, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Tenant suspended");
            response.put("tenantId", tenantId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Multi-tenancy platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error suspending tenant in multi-tenancy platform: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Activate tenant
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<Map<String, Object>> activateTenant(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/multitenancy/{}/activate", id);

        try {
            String tenantId = request.get("tenantId");

            multiTenancyService.activateTenant(id, tenantId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Tenant activated");
            response.put("tenantId", tenantId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Multi-tenancy platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error activating tenant in multi-tenancy platform: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update metrics
     */
    @PutMapping("/{id}/metrics")
    public ResponseEntity<Map<String, Object>> updateMetrics(@PathVariable Long id) {
        log.info("PUT /api/multitenancy/{}/metrics", id);

        try {
            multiTenancyService.updateMetrics(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Metrics updated");
            response.put("platformId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Multi-tenancy platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating metrics for multi-tenancy platform: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete platform
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePlatform(@PathVariable Long id) {
        log.info("DELETE /api/multitenancy/{}", id);

        try {
            multiTenancyService.deletePlatform(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Multi-tenancy platform deleted");
            response.put("platformId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting multi-tenancy platform: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/multitenancy/stats");

        try {
            Map<String, Object> stats = multiTenancyService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching multi-tenancy statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
