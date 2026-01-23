package com.heronix.controller;

import com.heronix.dto.ReportGovernance;
import com.heronix.service.ReportGovernanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Report Governance API Controller
 *
 * REST API endpoints for report governance and lifecycle management.
 *
 * Endpoints:
 * - POST /api/governance - Create governance
 * - GET /api/governance/{id} - Get governance
 * - GET /api/governance/report/{reportId} - Get governance by report
 * - POST /api/governance/{id}/transition - Transition lifecycle stage
 * - POST /api/governance/{id}/approval/step - Add approval step
 * - POST /api/governance/{id}/approval/{stepId}/approve - Approve step
 * - POST /api/governance/{id}/approval/{stepId}/reject - Reject step
 * - POST /api/governance/{id}/version - Create version
 * - POST /api/governance/{id}/change - Submit change request
 * - POST /api/governance/{id}/change/{changeId}/approve - Approve change
 * - POST /api/governance/{id}/change/{changeId}/reject - Reject change
 * - POST /api/governance/{id}/quality - Run quality check
 * - POST /api/governance/{id}/quality/calculate - Calculate quality score
 * - POST /api/governance/{id}/policy - Add policy
 * - POST /api/governance/{id}/release - Create release
 * - POST /api/governance/{id}/release/{releaseId}/execute - Release report
 * - POST /api/governance/{id}/deprecate - Deprecate report
 * - DELETE /api/governance/{id} - Delete governance
 * - GET /api/governance/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 88 - Report Governance & Lifecycle
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/governance")
@RequiredArgsConstructor
@Slf4j
public class ReportGovernanceApiController {

    private final ReportGovernanceService governanceService;

    /**
     * Create governance
     */
    @PostMapping
    public ResponseEntity<ReportGovernance> createGovernance(@RequestBody ReportGovernance governance) {
        log.info("POST /api/governance - Creating governance for report {}", governance.getReportId());

        try {
            ReportGovernance created = governanceService.createGovernance(governance);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating governance", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get governance
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportGovernance> getGovernance(@PathVariable Long id) {
        log.info("GET /api/governance/{}", id);

        try {
            return governanceService.getGovernance(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching governance: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get governance by report
     */
    @GetMapping("/report/{reportId}")
    public ResponseEntity<ReportGovernance> getGovernanceByReport(@PathVariable Long reportId) {
        log.info("GET /api/governance/report/{}", reportId);

        try {
            return governanceService.getGovernanceByReport(reportId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching governance for report: {}", reportId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Transition lifecycle stage
     */
    @PostMapping("/{id}/transition")
    public ResponseEntity<Map<String, Object>> transitionStage(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/governance/{}/transition", id);

        try {
            String toStageStr = request.get("toStage");
            String transitionedBy = request.get("transitionedBy");
            String reason = request.get("reason");

            ReportGovernance.LifecycleStage toStage = ReportGovernance.LifecycleStage.valueOf(toStageStr);

            governanceService.transitionStage(id, toStage, transitionedBy, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Stage transition completed");
            response.put("governanceId", id);
            response.put("toStage", toStage);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Governance not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            log.error("Invalid state for transition: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error transitioning stage for governance: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Add approval step
     */
    @PostMapping("/{id}/approval/step")
    public ResponseEntity<Map<String, Object>> addApprovalStep(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/governance/{}/approval/step", id);

        try {
            String stepName = (String) request.get("stepName");
            String approverId = (String) request.get("approverId");
            String approverRole = (String) request.get("approverRole");
            Boolean required = (Boolean) request.getOrDefault("required", true);
            Integer timeoutDays = request.get("timeoutDays") != null ?
                    ((Number) request.get("timeoutDays")).intValue() : null;

            governanceService.addApprovalStep(id, stepName, approverId, approverRole, required, timeoutDays);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Approval step added");
            response.put("governanceId", id);
            response.put("stepName", stepName);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Governance not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding approval step to governance: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Approve step
     */
    @PostMapping("/{id}/approval/{stepId}/approve")
    public ResponseEntity<Map<String, Object>> approveStep(
            @PathVariable Long id,
            @PathVariable String stepId,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/governance/{}/approval/{}/approve", id, stepId);

        try {
            String approvedBy = request.get("approvedBy");
            String comments = request.get("comments");

            governanceService.approveStep(id, stepId, approvedBy, comments);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Approval step approved");
            response.put("governanceId", id);
            response.put("stepId", stepId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Governance not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error approving step in governance: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Reject step
     */
    @PostMapping("/{id}/approval/{stepId}/reject")
    public ResponseEntity<Map<String, Object>> rejectStep(
            @PathVariable Long id,
            @PathVariable String stepId,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/governance/{}/approval/{}/reject", id, stepId);

        try {
            String rejectedBy = request.get("rejectedBy");
            String comments = request.get("comments");

            governanceService.rejectStep(id, stepId, rejectedBy, comments);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Approval step rejected");
            response.put("governanceId", id);
            response.put("stepId", stepId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Governance not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error rejecting step in governance: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create version
     */
    @PostMapping("/{id}/version")
    public ResponseEntity<ReportGovernance.Version> createVersion(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/governance/{}/version", id);

        try {
            String changeTypeStr = request.get("changeType");
            String description = request.get("description");
            String createdBy = request.get("createdBy");

            ReportGovernance.ChangeType changeType = ReportGovernance.ChangeType.valueOf(changeTypeStr);

            ReportGovernance.Version version = governanceService.createVersion(
                    id, changeType, description, createdBy
            );

            return ResponseEntity.ok(version);

        } catch (IllegalArgumentException e) {
            log.error("Governance not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            log.error("Invalid state for version creation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error creating version for governance: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Submit change request
     */
    @PostMapping("/{id}/change")
    public ResponseEntity<ReportGovernance.ChangeRequest> submitChangeRequest(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/governance/{}/change", id);

        try {
            String title = (String) request.get("title");
            String description = (String) request.get("description");
            String changeTypeStr = (String) request.get("changeType");
            String requestedBy = (String) request.get("requestedBy");
            Boolean impactsProduction = (Boolean) request.getOrDefault("impactsProduction", false);

            ReportGovernance.ChangeType changeType = ReportGovernance.ChangeType.valueOf(changeTypeStr);

            ReportGovernance.ChangeRequest changeRequest = governanceService.submitChangeRequest(
                    id, title, description, changeType, requestedBy, impactsProduction
            );

            return ResponseEntity.ok(changeRequest);

        } catch (IllegalArgumentException e) {
            log.error("Governance not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            log.error("Invalid state for change request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error submitting change request for governance: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Approve change request
     */
    @PostMapping("/{id}/change/{changeId}/approve")
    public ResponseEntity<Map<String, Object>> approveChangeRequest(
            @PathVariable Long id,
            @PathVariable String changeId,
            @RequestParam String approvedBy) {
        log.info("POST /api/governance/{}/change/{}/approve", id, changeId);

        try {
            governanceService.approveChangeRequest(id, changeId, approvedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Change request approved");
            response.put("governanceId", id);
            response.put("changeId", changeId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Governance not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error approving change request in governance: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Reject change request
     */
    @PostMapping("/{id}/change/{changeId}/reject")
    public ResponseEntity<Map<String, Object>> rejectChangeRequest(
            @PathVariable Long id,
            @PathVariable String changeId,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/governance/{}/change/{}/reject", id, changeId);

        try {
            String rejectedBy = request.get("rejectedBy");
            String reason = request.get("reason");

            governanceService.rejectChangeRequest(id, changeId, rejectedBy, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Change request rejected");
            response.put("governanceId", id);
            response.put("changeId", changeId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Governance not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error rejecting change request in governance: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Run quality check
     */
    @PostMapping("/{id}/quality")
    public ResponseEntity<Map<String, Object>> runQualityCheck(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/governance/{}/quality", id);

        try {
            String checkName = request.get("checkName");
            String checkType = request.get("checkType");
            String executedBy = request.get("executedBy");

            governanceService.runQualityCheck(id, checkName, checkType, executedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Quality check completed");
            response.put("governanceId", id);
            response.put("checkName", checkName);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Governance not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error running quality check for governance: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Calculate quality score
     */
    @PostMapping("/{id}/quality/calculate")
    public ResponseEntity<ReportGovernance.QualityScore> calculateQualityScore(@PathVariable Long id) {
        log.info("POST /api/governance/{}/quality/calculate", id);

        try {
            governanceService.calculateQualityScore(id);

            ReportGovernance governance = governanceService.getGovernance(id).orElse(null);
            if (governance == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(governance.getOverallQualityScore());

        } catch (IllegalArgumentException e) {
            log.error("Governance not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error calculating quality score for governance: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Add policy
     */
    @PostMapping("/{id}/policy")
    public ResponseEntity<Map<String, Object>> addPolicy(
            @PathVariable Long id,
            @RequestBody ReportGovernance.GovernancePolicy policy) {
        log.info("POST /api/governance/{}/policy", id);

        try {
            governanceService.addPolicy(id, policy);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Policy added");
            response.put("governanceId", id);
            response.put("policyId", policy.getPolicyId());
            response.put("policyName", policy.getPolicyName());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Governance not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding policy to governance: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create release
     */
    @PostMapping("/{id}/release")
    public ResponseEntity<ReportGovernance.Release> createRelease(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/governance/{}/release", id);

        try {
            String releaseName = request.get("releaseName");
            String versionNumber = request.get("versionNumber");
            String scheduledAtStr = request.get("scheduledAt");

            LocalDateTime scheduledAt = scheduledAtStr != null ?
                    LocalDateTime.parse(scheduledAtStr) : LocalDateTime.now();

            ReportGovernance.Release release = governanceService.createRelease(
                    id, releaseName, versionNumber, scheduledAt
            );

            return ResponseEntity.ok(release);

        } catch (IllegalArgumentException e) {
            log.error("Governance not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating release for governance: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Release report
     */
    @PostMapping("/{id}/release/{releaseId}/execute")
    public ResponseEntity<Map<String, Object>> releaseReport(
            @PathVariable Long id,
            @PathVariable String releaseId,
            @RequestParam String releasedBy) {
        log.info("POST /api/governance/{}/release/{}/execute", id, releaseId);

        try {
            governanceService.releaseReport(id, releaseId, releasedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Report released");
            response.put("governanceId", id);
            response.put("releaseId", releaseId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Governance not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error releasing report for governance: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Deprecate report
     */
    @PostMapping("/{id}/deprecate")
    public ResponseEntity<Map<String, Object>> deprecateReport(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/governance/{}/deprecate", id);

        try {
            String deprecatedBy = request.get("deprecatedBy");
            String reason = request.get("reason");
            String replacementReportId = request.get("replacementReportId");
            String retirementDateStr = request.get("retirementDate");

            LocalDateTime retirementDate = retirementDateStr != null ?
                    LocalDateTime.parse(retirementDateStr) : null;

            governanceService.deprecateReport(id, deprecatedBy, reason, replacementReportId, retirementDate);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Report deprecated");
            response.put("governanceId", id);
            response.put("retirementDate", retirementDate);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Governance not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error deprecating report for governance: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete governance
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteGovernance(@PathVariable Long id) {
        log.info("DELETE /api/governance/{}", id);

        try {
            governanceService.deleteGovernance(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Governance deleted");
            response.put("governanceId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting governance: {}", id, e);
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
