package com.heronix.controller;

import com.heronix.dto.ReportFederation;
import com.heronix.service.ReportFederationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Federation API Controller
 *
 * REST API endpoints for federation and multi-tenant management.
 *
 * Endpoints:
 * - POST /api/federation - Create federation
 * - GET /api/federation/{id} - Get federation
 * - POST /api/federation/{id}/tenant - Add tenant
 * - DELETE /api/federation/{id}/tenant/{tenantId} - Remove tenant
 * - POST /api/federation/{id}/share - Share report
 * - DELETE /api/federation/{id}/share/{shareId} - Unshare report
 * - GET /api/federation/{id}/access - Check access
 * - POST /api/federation/{id}/query - Execute federated query
 * - POST /api/federation/{id}/quota/check - Check quota
 * - POST /api/federation/{id}/quota/update - Update quota usage
 * - POST /api/federation/{id}/policy - Add policy
 * - DELETE /api/federation/{id}/policy/{policyId} - Remove policy
 * - POST /api/federation/{id}/sync - Synchronize federation
 * - POST /api/federation/{id}/access/track - Track access
 * - PUT /api/federation/{id}/tenant/{tenantId}/status - Update tenant status
 * - GET /api/federation/{id}/tenant/{tenantId}/reports - Get shared reports for tenant
 * - GET /api/federation/{id}/stats - Get federation statistics
 * - DELETE /api/federation/{id} - Delete federation
 * - GET /api/federation/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 87 - Report Federation & Multi-Tenant
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/federation")
@RequiredArgsConstructor
@Slf4j
public class ReportFederationApiController {

    private final ReportFederationService federationService;

    /**
     * Create federation
     */
    @PostMapping
    public ResponseEntity<ReportFederation> createFederation(@RequestBody ReportFederation federation) {
        log.info("POST /api/federation - Creating federation: {}", federation.getFederationName());

        try {
            ReportFederation created = federationService.createFederation(federation);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating federation", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get federation
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportFederation> getFederation(@PathVariable Long id) {
        log.info("GET /api/federation/{}", id);

        try {
            return federationService.getFederation(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching federation: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Add tenant
     */
    @PostMapping("/{id}/tenant")
    public ResponseEntity<Map<String, Object>> addTenant(
            @PathVariable Long id,
            @RequestBody ReportFederation.Tenant tenant) {
        log.info("POST /api/federation/{}/tenant", id);

        try {
            federationService.addTenant(id, tenant);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Tenant added");
            response.put("federationId", id);
            response.put("tenantId", tenant.getTenantId());
            response.put("tenantName", tenant.getTenantName());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Federation not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding tenant to federation: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Remove tenant
     */
    @DeleteMapping("/{id}/tenant/{tenantId}")
    public ResponseEntity<Map<String, Object>> removeTenant(
            @PathVariable Long id,
            @PathVariable String tenantId) {
        log.info("DELETE /api/federation/{}/tenant/{}", id, tenantId);

        try {
            federationService.removeTenant(id, tenantId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Tenant removed");
            response.put("federationId", id);
            response.put("tenantId", tenantId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Federation not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error removing tenant from federation: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Share report
     */
    @PostMapping("/{id}/share")
    public ResponseEntity<ReportFederation.SharedReport> shareReport(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/federation/{}/share", id);

        try {
            String reportId = (String) request.get("reportId");
            String reportName = (String) request.get("reportName");
            String ownerTenantId = (String) request.get("ownerTenantId");
            String scopeStr = (String) request.get("scope");

            @SuppressWarnings("unchecked")
            List<String> sharedWithTenants = (List<String>) request.get("sharedWithTenants");

            ReportFederation.ShareScope scope = ReportFederation.ShareScope.valueOf(scopeStr);

            ReportFederation.SharedReport sharedReport = federationService.shareReport(
                    id, reportId, reportName, ownerTenantId, sharedWithTenants, scope
            );

            return ResponseEntity.ok(sharedReport);

        } catch (IllegalArgumentException e) {
            log.error("Federation not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            log.error("Invalid state for sharing: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error sharing report in federation: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Unshare report
     */
    @DeleteMapping("/{id}/share/{shareId}")
    public ResponseEntity<Map<String, Object>> unshareReport(
            @PathVariable Long id,
            @PathVariable String shareId) {
        log.info("DELETE /api/federation/{}/share/{}", id, shareId);

        try {
            federationService.unshareReport(id, shareId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Report unshared");
            response.put("federationId", id);
            response.put("shareId", shareId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Federation not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error unsharing report in federation: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Check access
     */
    @GetMapping("/{id}/access")
    public ResponseEntity<Map<String, Object>> checkAccess(
            @PathVariable Long id,
            @RequestParam String tenantId,
            @RequestParam String reportId) {
        log.info("GET /api/federation/{}/access?tenantId={}&reportId={}", id, tenantId, reportId);

        try {
            boolean hasAccess = federationService.checkAccess(id, tenantId, reportId);

            Map<String, Object> response = new HashMap<>();
            response.put("federationId", id);
            response.put("tenantId", tenantId);
            response.put("reportId", reportId);
            response.put("hasAccess", hasAccess);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error checking access in federation: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Execute federated query
     */
    @PostMapping("/{id}/query")
    public ResponseEntity<ReportFederation.FederatedQuery> executeFederatedQuery(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/federation/{}/query", id);

        try {
            String queryName = (String) request.get("queryName");
            String query = (String) request.get("query");
            String aggregationType = (String) request.get("aggregationType");

            @SuppressWarnings("unchecked")
            List<String> sourceTenants = (List<String>) request.get("sourceTenants");

            ReportFederation.FederatedQuery result = federationService.executeFederatedQuery(
                    id, queryName, sourceTenants, query, aggregationType
            );

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.error("Federation not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            log.error("Invalid state for federated query: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error executing federated query in federation: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Check quota
     */
    @PostMapping("/{id}/quota/check")
    public ResponseEntity<Map<String, Object>> checkQuota(
            @PathVariable Long id,
            @RequestParam String tenantId) {
        log.info("POST /api/federation/{}/quota/check?tenantId={}", id, tenantId);

        try {
            federationService.checkQuota(id, tenantId);

            ReportFederation federation = federationService.getFederation(id).orElse(null);
            if (federation == null) {
                return ResponseEntity.notFound().build();
            }

            ReportFederation.TenantQuota quota = federation.getTenantQuota(tenantId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Quota checked");
            response.put("federationId", id);
            response.put("tenantId", tenantId);
            response.put("quotaExceeded", quota != null ? quota.getQuotaExceeded() : false);
            response.put("exceededLimits", quota != null ? quota.getExceededLimits() : null);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error checking quota in federation: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update quota usage
     */
    @PostMapping("/{id}/quota/update")
    public ResponseEntity<Map<String, Object>> updateQuotaUsage(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/federation/{}/quota/update", id);

        try {
            String tenantId = (String) request.get("tenantId");
            String metric = (String) request.get("metric");
            Long value = ((Number) request.get("value")).longValue();

            federationService.updateQuotaUsage(id, tenantId, metric, value);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Quota usage updated");
            response.put("federationId", id);
            response.put("tenantId", tenantId);
            response.put("metric", metric);
            response.put("value", value);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error updating quota usage in federation: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add policy
     */
    @PostMapping("/{id}/policy")
    public ResponseEntity<Map<String, Object>> addPolicy(
            @PathVariable Long id,
            @RequestBody ReportFederation.FederationPolicy policy) {
        log.info("POST /api/federation/{}/policy", id);

        try {
            federationService.addPolicy(id, policy);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Policy added");
            response.put("federationId", id);
            response.put("policyId", policy.getPolicyId());
            response.put("policyName", policy.getPolicyName());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Federation not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding policy to federation: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Remove policy
     */
    @DeleteMapping("/{id}/policy/{policyId}")
    public ResponseEntity<Map<String, Object>> removePolicy(
            @PathVariable Long id,
            @PathVariable String policyId) {
        log.info("DELETE /api/federation/{}/policy/{}", id, policyId);

        try {
            federationService.removePolicy(id, policyId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Policy removed");
            response.put("federationId", id);
            response.put("policyId", policyId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Federation not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error removing policy from federation: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Synchronize federation
     */
    @PostMapping("/{id}/sync")
    public ResponseEntity<Map<String, Object>> synchronize(@PathVariable Long id) {
        log.info("POST /api/federation/{}/sync", id);

        try {
            federationService.synchronize(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Synchronization completed");
            response.put("federationId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error synchronizing federation: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Track access
     */
    @PostMapping("/{id}/access/track")
    public ResponseEntity<Map<String, Object>> trackAccess(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/federation/{}/access/track", id);

        try {
            String tenantId = (String) request.get("tenantId");
            String reportId = (String) request.get("reportId");
            String userId = (String) request.get("userId");
            String action = (String) request.get("action");
            Boolean success = (Boolean) request.getOrDefault("success", true);

            federationService.trackAccess(id, tenantId, reportId, userId, action, success);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Access tracked");
            response.put("federationId", id);
            response.put("tenantId", tenantId);
            response.put("reportId", reportId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error tracking access in federation: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update tenant status
     */
    @PutMapping("/{id}/tenant/{tenantId}/status")
    public ResponseEntity<Map<String, Object>> updateTenantStatus(
            @PathVariable Long id,
            @PathVariable String tenantId,
            @RequestBody Map<String, String> request) {
        log.info("PUT /api/federation/{}/tenant/{}/status", id, tenantId);

        try {
            String statusStr = request.get("status");
            ReportFederation.TenantStatus status = ReportFederation.TenantStatus.valueOf(statusStr);

            federationService.updateTenantStatus(id, tenantId, status);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Tenant status updated");
            response.put("federationId", id);
            response.put("tenantId", tenantId);
            response.put("status", status);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Federation or tenant not found: {}, {}", id, tenantId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating tenant status in federation: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get shared reports for tenant
     */
    @GetMapping("/{id}/tenant/{tenantId}/reports")
    public ResponseEntity<List<ReportFederation.SharedReport>> getSharedReportsForTenant(
            @PathVariable Long id,
            @PathVariable String tenantId) {
        log.info("GET /api/federation/{}/tenant/{}/reports", id, tenantId);

        try {
            List<ReportFederation.SharedReport> reports = federationService.getSharedReportsForTenant(id, tenantId);
            return ResponseEntity.ok(reports);

        } catch (Exception e) {
            log.error("Error fetching shared reports for tenant {} in federation: {}", tenantId, id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get federation statistics
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<Map<String, Object>> getFederationStatistics(@PathVariable Long id) {
        log.info("GET /api/federation/{}/stats", id);

        try {
            Map<String, Object> stats = federationService.getFederationStatistics(id);
            return ResponseEntity.ok(stats);

        } catch (IllegalArgumentException e) {
            log.error("Federation not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error fetching federation statistics: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete federation
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteFederation(@PathVariable Long id) {
        log.info("DELETE /api/federation/{}", id);

        try {
            federationService.deleteFederation(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Federation deleted");
            response.put("federationId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting federation: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/federation/stats");

        try {
            Map<String, Object> stats = federationService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching federation statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
