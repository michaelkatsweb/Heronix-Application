package com.heronix.controller;

import com.heronix.dto.ReportAudit;
import com.heronix.service.ReportComplianceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Compliance API Controller
 *
 * REST API endpoints for audit trail and compliance management.
 *
 * Endpoints:
 * - POST /api/compliance - Create audit
 * - GET /api/compliance/{id} - Get audit
 * - GET /api/compliance/report/{reportId} - Get audit by report
 * - POST /api/compliance/{id}/entry - Log audit entry
 * - POST /api/compliance/{id}/activity - Track user activity
 * - POST /api/compliance/{id}/access - Log data access
 * - POST /api/compliance/{id}/rule - Add compliance rule
 * - POST /api/compliance/{id}/check - Check compliance
 * - POST /api/compliance/{id}/violation/{violationId}/resolve - Resolve violation
 * - POST /api/compliance/{id}/security - Log security event
 * - POST /api/compliance/{id}/consent - Record consent
 * - DELETE /api/compliance/{id}/consent/{consentId} - Withdraw consent
 * - POST /api/compliance/{id}/retention - Apply retention policy
 * - GET /api/compliance/{id}/report - Generate compliance report
 * - GET /api/compliance/{id}/trail - Get audit trail
 * - DELETE /api/compliance/{id} - Delete audit
 * - GET /api/compliance/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 86 - Report Audit & Compliance
 */
@RestController
@RequestMapping("/api/compliance")
@RequiredArgsConstructor
@Slf4j
public class ReportComplianceApiController {

    private final ReportComplianceService complianceService;

    /**
     * Create audit
     */
    @PostMapping
    public ResponseEntity<ReportAudit> createAudit(@RequestBody ReportAudit audit) {
        log.info("POST /api/compliance - Creating audit for report {}", audit.getReportId());

        try {
            ReportAudit created = complianceService.createAudit(audit);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating audit", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get audit
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportAudit> getAudit(@PathVariable Long id) {
        log.info("GET /api/compliance/{}", id);

        try {
            return complianceService.getAudit(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching audit: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get audit by report
     */
    @GetMapping("/report/{reportId}")
    public ResponseEntity<ReportAudit> getAuditByReport(@PathVariable Long reportId) {
        log.info("GET /api/compliance/report/{}", reportId);

        try {
            return complianceService.getAuditByReport(reportId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching audit for report: {}", reportId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Log audit entry
     */
    @PostMapping("/{id}/entry")
    public ResponseEntity<Map<String, Object>> logAuditEntry(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/compliance/{}/entry", id);

        try {
            String auditType = (String) request.get("auditType");
            String action = (String) request.get("action");
            String userId = (String) request.get("userId");
            String description = (String) request.get("description");
            Boolean success = (Boolean) request.getOrDefault("success", true);

            @SuppressWarnings("unchecked")
            Map<String, Object> details = (Map<String, Object>) request.get("details");

            complianceService.logAuditEntry(
                    id,
                    ReportAudit.AuditType.valueOf(auditType),
                    ReportAudit.ActionType.valueOf(action),
                    userId,
                    description,
                    details,
                    success
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Audit entry logged");
            response.put("auditId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error logging audit entry for audit: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Track user activity
     */
    @PostMapping("/{id}/activity")
    public ResponseEntity<Map<String, Object>> trackUserActivity(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/compliance/{}/activity", id);

        try {
            String userId = request.get("userId");
            String action = request.get("action");

            complianceService.trackUserActivity(
                    id,
                    userId,
                    ReportAudit.ActionType.valueOf(action)
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User activity tracked");
            response.put("auditId", id);
            response.put("userId", userId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error tracking user activity for audit: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Log data access
     */
    @PostMapping("/{id}/access")
    public ResponseEntity<Map<String, Object>> logDataAccess(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/compliance/{}/access", id);

        try {
            String userId = (String) request.get("userId");
            String dataField = (String) request.get("dataField");
            Boolean isPII = (Boolean) request.getOrDefault("isPII", false);
            Boolean isSensitive = (Boolean) request.getOrDefault("isSensitive", false);
            String accessReason = (String) request.get("accessReason");

            complianceService.logDataAccess(id, userId, dataField, isPII, isSensitive, accessReason);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Data access logged");
            response.put("auditId", id);
            response.put("dataField", dataField);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error logging data access for audit: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add compliance rule
     */
    @PostMapping("/{id}/rule")
    public ResponseEntity<Map<String, Object>> addComplianceRule(
            @PathVariable Long id,
            @RequestBody ReportAudit.ComplianceRule rule) {
        log.info("POST /api/compliance/{}/rule", id);

        try {
            complianceService.addComplianceRule(id, rule);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Compliance rule added");
            response.put("auditId", id);
            response.put("ruleId", rule.getRuleId());
            response.put("ruleName", rule.getRuleName());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Audit not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding compliance rule to audit: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Check compliance
     */
    @PostMapping("/{id}/check")
    public ResponseEntity<Map<String, Object>> checkCompliance(@PathVariable Long id) {
        log.info("POST /api/compliance/{}/check", id);

        try {
            complianceService.checkCompliance(id);

            ReportAudit audit = complianceService.getAudit(id).orElse(null);
            if (audit == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Compliance check completed");
            response.put("auditId", id);
            response.put("status", audit.getOverallComplianceStatus());
            response.put("totalViolations", audit.getTotalViolations());
            response.put("unresolvedViolations", audit.getUnresolvedViolations().size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error checking compliance for audit: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Resolve violation
     */
    @PostMapping("/{id}/violation/{violationId}/resolve")
    public ResponseEntity<Map<String, Object>> resolveViolation(
            @PathVariable Long id,
            @PathVariable String violationId,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/compliance/{}/violation/{}/resolve", id, violationId);

        try {
            String resolvedBy = request.get("resolvedBy");
            String resolution = request.get("resolution");

            complianceService.resolveViolation(id, violationId, resolvedBy, resolution);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Violation resolved");
            response.put("auditId", id);
            response.put("violationId", violationId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Audit not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error resolving violation: {}", violationId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Log security event
     */
    @PostMapping("/{id}/security")
    public ResponseEntity<Map<String, Object>> logSecurityEvent(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/compliance/{}/security", id);

        try {
            String eventType = (String) request.get("eventType");
            String severity = (String) request.get("severity");
            String userId = (String) request.get("userId");
            String description = (String) request.get("description");
            Boolean blocked = (Boolean) request.getOrDefault("blocked", false);

            complianceService.logSecurityEvent(id, eventType, severity, userId, description, blocked);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Security event logged");
            response.put("auditId", id);
            response.put("eventType", eventType);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error logging security event for audit: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Record consent
     */
    @PostMapping("/{id}/consent")
    public ResponseEntity<Map<String, Object>> recordConsent(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/compliance/{}/consent", id);

        try {
            String userId = (String) request.get("userId");
            String purpose = (String) request.get("purpose");
            Boolean consentGiven = (Boolean) request.getOrDefault("consentGiven", false);
            String expiryDateStr = (String) request.get("expiryDate");

            LocalDateTime expiryDate = expiryDateStr != null ? LocalDateTime.parse(expiryDateStr) : null;

            complianceService.recordConsent(id, userId, purpose, consentGiven, expiryDate);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Consent recorded");
            response.put("auditId", id);
            response.put("userId", userId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Audit not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error recording consent for audit: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Withdraw consent
     */
    @DeleteMapping("/{id}/consent/{consentId}")
    public ResponseEntity<Map<String, Object>> withdrawConsent(
            @PathVariable Long id,
            @PathVariable String consentId,
            @RequestParam String userId) {
        log.info("DELETE /api/compliance/{}/consent/{}", id, consentId);

        try {
            complianceService.withdrawConsent(id, consentId, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Consent withdrawn");
            response.put("auditId", id);
            response.put("consentId", consentId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Audit not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error withdrawing consent: {}", consentId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Apply retention policy
     */
    @PostMapping("/{id}/retention")
    public ResponseEntity<Map<String, Object>> applyRetentionPolicy(@PathVariable Long id) {
        log.info("POST /api/compliance/{}/retention", id);

        try {
            complianceService.applyRetentionPolicy(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Retention policy applied");
            response.put("auditId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error applying retention policy for audit: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generate compliance report
     */
    @GetMapping("/{id}/report")
    public ResponseEntity<Map<String, Object>> generateComplianceReport(@PathVariable Long id) {
        log.info("GET /api/compliance/{}/report", id);

        try {
            Map<String, Object> report = complianceService.generateComplianceReport(id);
            return ResponseEntity.ok(report);

        } catch (IllegalArgumentException e) {
            log.error("Audit not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error generating compliance report for audit: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get audit trail
     */
    @GetMapping("/{id}/trail")
    public ResponseEntity<List<ReportAudit.AuditEntry>> getAuditTrail(
            @PathVariable Long id,
            @RequestParam(defaultValue = "100") int limit) {
        log.info("GET /api/compliance/{}/trail?limit={}", id, limit);

        try {
            List<ReportAudit.AuditEntry> trail = complianceService.getAuditTrail(id, limit);
            return ResponseEntity.ok(trail);

        } catch (Exception e) {
            log.error("Error fetching audit trail for audit: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete audit
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAudit(@PathVariable Long id) {
        log.info("DELETE /api/compliance/{}", id);

        try {
            complianceService.deleteAudit(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Audit deleted");
            response.put("auditId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting audit: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/compliance/stats");

        try {
            Map<String, Object> stats = complianceService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching compliance statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
