package com.heronix.controller;

import com.heronix.dto.ReportDataGovernance;
import com.heronix.service.ReportDataGovernanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Data Governance & Compliance API Controller
 *
 * REST API endpoints for data governance, compliance, and audit trail management.
 *
 * Endpoints:
 * - POST /api/governance - Create governance framework
 * - GET /api/governance/{id} - Get governance framework
 * - POST /api/governance/{id}/activate - Activate framework
 * - POST /api/governance/{id}/asset - Register data asset
 * - POST /api/governance/{id}/policy - Add governance policy
 * - POST /api/governance/{id}/audit - Log audit event
 * - POST /api/governance/{id}/access-request - Submit access request
 * - POST /api/governance/{id}/approve - Approve access request
 * - POST /api/governance/{id}/deny - Deny access request
 * - POST /api/governance/{id}/retention - Add retention rule
 * - POST /api/governance/{id}/assessment - Conduct privacy assessment
 * - POST /api/governance/{id}/consent - Record consent
 * - POST /api/governance/{id}/lineage - Track data lineage
 * - POST /api/governance/{id}/violation - Report violation
 * - POST /api/governance/{id}/resolve - Resolve violation
 * - PUT /api/governance/{id}/metrics - Update metrics
 * - DELETE /api/governance/{id} - Delete framework
 * - GET /api/governance/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 108 - Report Data Governance & Compliance
 */
@RestController
@RequestMapping("/api/governance")
@RequiredArgsConstructor
@Slf4j
public class ReportDataGovernanceApiController {

    private final ReportDataGovernanceService governanceService;

    /**
     * Create governance framework
     */
    @PostMapping
    public ResponseEntity<ReportDataGovernance> createFramework(@RequestBody ReportDataGovernance framework) {
        log.info("POST /api/governance - Creating governance framework: {}", framework.getFrameworkName());

        try {
            ReportDataGovernance created = governanceService.createFramework(framework);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating governance framework", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get governance framework
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportDataGovernance> getFramework(@PathVariable Long id) {
        log.info("GET /api/governance/{}", id);

        try {
            return governanceService.getFramework(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching governance framework: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Activate framework
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<Map<String, Object>> activateFramework(@PathVariable Long id) {
        log.info("POST /api/governance/{}/activate", id);

        try {
            governanceService.activateFramework(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Governance framework activated");
            response.put("frameworkId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Governance framework not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error activating governance framework: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Register data asset
     */
    @PostMapping("/{id}/asset")
    public ResponseEntity<ReportDataGovernance.DataAsset> registerAsset(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/governance/{}/asset", id);

        try {
            String assetName = (String) request.get("assetName");
            String assetType = (String) request.get("assetType");
            String classificationStr = (String) request.get("classification");
            String dataOwner = (String) request.get("dataOwner");
            Boolean containsPII = (Boolean) request.getOrDefault("containsPII", false);

            ReportDataGovernance.DataClassification classification =
                    ReportDataGovernance.DataClassification.valueOf(classificationStr);

            ReportDataGovernance.DataAsset asset = governanceService.registerAsset(
                    id, assetName, assetType, classification, dataOwner, containsPII
            );

            return ResponseEntity.ok(asset);

        } catch (IllegalArgumentException e) {
            log.error("Governance framework not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error registering asset in governance framework: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add governance policy
     */
    @PostMapping("/{id}/policy")
    public ResponseEntity<ReportDataGovernance.GovernancePolicy> addPolicy(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/governance/{}/policy", id);

        try {
            String policyName = (String) request.get("policyName");
            String policyTypeStr = (String) request.get("policyType");
            String description = (String) request.get("description");
            @SuppressWarnings("unchecked")
            Map<String, Object> rules = (Map<String, Object>) request.get("rules");

            ReportDataGovernance.PolicyType policyType =
                    ReportDataGovernance.PolicyType.valueOf(policyTypeStr);

            ReportDataGovernance.GovernancePolicy policy = governanceService.addPolicy(
                    id, policyName, policyType, description, rules
            );

            return ResponseEntity.ok(policy);

        } catch (IllegalArgumentException e) {
            log.error("Governance framework not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding policy to governance framework: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Log audit event
     */
    @PostMapping("/{id}/audit")
    public ResponseEntity<ReportDataGovernance.AuditLog> logAudit(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/governance/{}/audit", id);

        try {
            String userId = (String) request.get("userId");
            String userName = (String) request.get("userName");
            String assetId = (String) request.get("assetId");
            String assetName = (String) request.get("assetName");
            String actionStr = (String) request.get("action");
            Boolean successful = (Boolean) request.getOrDefault("successful", true);

            ReportDataGovernance.AuditActionType action =
                    ReportDataGovernance.AuditActionType.valueOf(actionStr);

            ReportDataGovernance.AuditLog audit = governanceService.logAudit(
                    id, userId, userName, assetId, assetName, action, successful
            );

            return ResponseEntity.ok(audit);

        } catch (IllegalArgumentException e) {
            log.error("Governance framework not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error logging audit in governance framework: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Submit access request
     */
    @PostMapping("/{id}/access-request")
    public ResponseEntity<ReportDataGovernance.DataAccessRequest> submitAccessRequest(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/governance/{}/access-request", id);

        try {
            String requesterId = (String) request.get("requesterId");
            String requesterName = (String) request.get("requesterName");
            String assetId = (String) request.get("assetId");
            String assetName = (String) request.get("assetName");
            String purpose = (String) request.get("purpose");
            @SuppressWarnings("unchecked")
            List<String> permissions = (List<String>) request.get("permissions");

            ReportDataGovernance.DataAccessRequest accessRequest = governanceService.submitAccessRequest(
                    id, requesterId, requesterName, assetId, assetName, purpose, permissions
            );

            return ResponseEntity.ok(accessRequest);

        } catch (IllegalArgumentException e) {
            log.error("Governance framework not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error submitting access request in governance framework: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Approve access request
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveAccessRequest(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/governance/{}/approve", id);

        try {
            String requestId = request.get("requestId");
            String reviewedBy = request.get("reviewedBy");
            String comments = request.get("comments");

            governanceService.approveAccessRequest(id, requestId, reviewedBy, comments);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Access request approved");
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Governance framework not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error approving access request in governance framework: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Deny access request
     */
    @PostMapping("/{id}/deny")
    public ResponseEntity<Map<String, Object>> denyAccessRequest(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/governance/{}/deny", id);

        try {
            String requestId = request.get("requestId");
            String reviewedBy = request.get("reviewedBy");
            String comments = request.get("comments");

            governanceService.denyAccessRequest(id, requestId, reviewedBy, comments);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Access request denied");
            response.put("requestId", requestId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Governance framework not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error denying access request in governance framework: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add retention rule
     */
    @PostMapping("/{id}/retention")
    public ResponseEntity<ReportDataGovernance.RetentionRule> addRetentionRule(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/governance/{}/retention", id);

        try {
            String ruleName = (String) request.get("ruleName");
            Integer retentionDays = request.get("retentionDays") != null ?
                    ((Number) request.get("retentionDays")).intValue() : 365;
            Boolean autoDelete = (Boolean) request.getOrDefault("autoDelete", false);

            ReportDataGovernance.RetentionRule rule = governanceService.addRetentionRule(
                    id, ruleName, retentionDays, autoDelete
            );

            return ResponseEntity.ok(rule);

        } catch (IllegalArgumentException e) {
            log.error("Governance framework not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding retention rule to governance framework: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Conduct privacy assessment
     */
    @PostMapping("/{id}/assessment")
    public ResponseEntity<ReportDataGovernance.PrivacyAssessment> conductAssessment(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/governance/{}/assessment", id);

        try {
            String assessmentName = request.get("assessmentName");
            String assetId = request.get("assetId");
            String assetName = request.get("assetName");
            String assessor = request.get("assessor");

            ReportDataGovernance.PrivacyAssessment assessment = governanceService.conductAssessment(
                    id, assessmentName, assetId, assetName, assessor
            );

            return ResponseEntity.ok(assessment);

        } catch (IllegalArgumentException e) {
            log.error("Governance framework not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error conducting assessment in governance framework: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Record consent
     */
    @PostMapping("/{id}/consent")
    public ResponseEntity<ReportDataGovernance.ConsentRecord> recordConsent(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/governance/{}/consent", id);

        try {
            String userId = (String) request.get("userId");
            String userName = (String) request.get("userName");
            String consentType = (String) request.get("consentType");
            String purpose = (String) request.get("purpose");
            Boolean consentGiven = (Boolean) request.getOrDefault("consentGiven", true);

            ReportDataGovernance.ConsentRecord consent = governanceService.recordConsent(
                    id, userId, userName, consentType, purpose, consentGiven
            );

            return ResponseEntity.ok(consent);

        } catch (IllegalArgumentException e) {
            log.error("Governance framework not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error recording consent in governance framework: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Track data lineage
     */
    @PostMapping("/{id}/lineage")
    public ResponseEntity<ReportDataGovernance.DataLineage> trackLineage(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/governance/{}/lineage", id);

        try {
            String assetId = (String) request.get("assetId");
            String assetName = (String) request.get("assetName");
            @SuppressWarnings("unchecked")
            List<String> sourceAssets = (List<String>) request.get("sourceAssets");
            String createdBy = (String) request.get("createdBy");

            ReportDataGovernance.DataLineage lineage = governanceService.trackLineage(
                    id, assetId, assetName, sourceAssets, createdBy
            );

            return ResponseEntity.ok(lineage);

        } catch (IllegalArgumentException e) {
            log.error("Governance framework not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error tracking lineage in governance framework: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Report violation
     */
    @PostMapping("/{id}/violation")
    public ResponseEntity<ReportDataGovernance.ComplianceViolation> reportViolation(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/governance/{}/violation", id);

        try {
            String policyId = request.get("policyId");
            String policyName = request.get("policyName");
            String assetId = request.get("assetId");
            String assetName = request.get("assetName");
            String severityStr = request.get("severity");
            String violationType = request.get("violationType");
            String description = request.get("description");

            ReportDataGovernance.ViolationSeverity severity =
                    ReportDataGovernance.ViolationSeverity.valueOf(severityStr);

            ReportDataGovernance.ComplianceViolation violation = governanceService.reportViolation(
                    id, policyId, policyName, assetId, assetName, severity, violationType, description
            );

            return ResponseEntity.ok(violation);

        } catch (IllegalArgumentException e) {
            log.error("Governance framework not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error reporting violation in governance framework: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Resolve violation
     */
    @PostMapping("/{id}/resolve")
    public ResponseEntity<Map<String, Object>> resolveViolation(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/governance/{}/resolve", id);

        try {
            String violationId = request.get("violationId");
            String resolution = request.get("resolution");
            String resolvedBy = request.get("resolvedBy");

            governanceService.resolveViolation(id, violationId, resolution, resolvedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Violation resolved");
            response.put("violationId", violationId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Governance framework not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error resolving violation in governance framework: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update metrics
     */
    @PutMapping("/{id}/metrics")
    public ResponseEntity<Map<String, Object>> updateMetrics(@PathVariable Long id) {
        log.info("PUT /api/governance/{}/metrics", id);

        try {
            governanceService.updateMetrics(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Metrics updated");
            response.put("frameworkId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Governance framework not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating metrics for governance framework: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete framework
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteFramework(@PathVariable Long id) {
        log.info("DELETE /api/governance/{}", id);

        try {
            governanceService.deleteFramework(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Governance framework deleted");
            response.put("frameworkId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting governance framework: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/governance/stats");

        try {
            Map<String, Object> stats = governanceService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching governance statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
