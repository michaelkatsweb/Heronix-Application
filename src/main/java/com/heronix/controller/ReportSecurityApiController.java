package com.heronix.controller;

import com.heronix.dto.ReportSecurity;
import com.heronix.service.ReportSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Security API Controller
 *
 * REST API endpoints for security and access control management.
 *
 * Endpoints:
 * - POST /api/security - Create security config
 * - GET /api/security/{id} - Get security config
 * - GET /api/security/report/{reportId} - Get security config by report
 * - PUT /api/security/{id} - Update security config
 * - POST /api/security/{id}/check-access - Check access permission
 * - POST /api/security/{id}/mask - Mask sensitive data
 * - POST /api/security/{id}/encrypt - Encrypt data
 * - POST /api/security/{id}/decrypt - Decrypt data
 * - POST /api/security/{id}/acl - Add access control entry
 * - DELETE /api/security/{id}/acl/{aceId} - Remove access control entry
 * - GET /api/security/{id}/acl - Get access control list
 * - GET /api/security/{id}/events - Get security events
 * - POST /api/security/{id}/vulnerability - Add vulnerability
 * - POST /api/security/{id}/vulnerability/{vulnId}/resolve - Resolve vulnerability
 * - GET /api/security/{id}/vulnerabilities - Get vulnerabilities
 * - POST /api/security/{id}/scan - Perform security scan
 * - GET /api/security/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 79 - Report Security & Access Control
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/security")
@RequiredArgsConstructor
@Slf4j
public class ReportSecurityApiController {

    private final ReportSecurityService securityService;

    /**
     * Create security configuration
     */
    @PostMapping
    public ResponseEntity<ReportSecurity> createSecurityConfig(@RequestBody ReportSecurity security) {
        log.info("POST /api/security - Creating security config for report {}", security.getReportId());

        try {
            ReportSecurity created = securityService.createSecurityConfig(security);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating security config", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get security configuration
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportSecurity> getSecurityConfig(@PathVariable Long id) {
        log.info("GET /api/security/{}", id);

        try {
            return securityService.getSecurityConfig(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching security config: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get security configuration by report ID
     */
    @GetMapping("/report/{reportId}")
    public ResponseEntity<ReportSecurity> getSecurityConfigByReportId(@PathVariable Long reportId) {
        log.info("GET /api/security/report/{}", reportId);

        try {
            return securityService.getSecurityConfigByReportId(reportId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching security config for report: {}", reportId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update security configuration
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReportSecurity> updateSecurityConfig(
            @PathVariable Long id,
            @RequestBody ReportSecurity security) {
        log.info("PUT /api/security/{}", id);

        try {
            ReportSecurity updated = securityService.updateSecurityConfig(id, security);
            return ResponseEntity.ok(updated);

        } catch (IllegalArgumentException e) {
            log.error("Security config not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating security config: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Check access permission
     */
    @PostMapping("/{id}/check-access")
    public ResponseEntity<Map<String, Object>> checkAccess(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/security/{}/check-access", id);

        try {
            String username = (String) request.get("username");
            @SuppressWarnings("unchecked")
            List<String> userRoles = (List<String>) request.get("userRoles");
            String ipAddress = (String) request.get("ipAddress");

            boolean hasAccess = securityService.checkAccess(id, username, userRoles, ipAddress);

            Map<String, Object> response = new HashMap<>();
            response.put("hasAccess", hasAccess);
            response.put("username", username);
            response.put("securityId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error checking access: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Mask sensitive data
     */
    @PostMapping("/{id}/mask")
    public ResponseEntity<Map<String, Object>> maskData(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/security/{}/mask", id);

        try {
            String fieldName = request.get("fieldName");
            String value = request.get("value");

            String maskedValue = securityService.maskData(id, fieldName, value);

            Map<String, Object> response = new HashMap<>();
            response.put("fieldName", fieldName);
            response.put("maskedValue", maskedValue);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error masking data: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Encrypt data
     */
    @PostMapping("/{id}/encrypt")
    public ResponseEntity<Map<String, Object>> encryptData(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/security/{}/encrypt", id);

        try {
            String data = request.get("data");
            String encrypted = securityService.encryptData(id, data);

            Map<String, Object> response = new HashMap<>();
            response.put("encrypted", encrypted);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error encrypting data: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Decrypt data
     */
    @PostMapping("/{id}/decrypt")
    public ResponseEntity<Map<String, Object>> decryptData(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/security/{}/decrypt", id);

        try {
            String encryptedData = request.get("encryptedData");
            String decrypted = securityService.decryptData(id, encryptedData);

            Map<String, Object> response = new HashMap<>();
            response.put("decrypted", decrypted);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error decrypting data: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add access control entry
     */
    @PostMapping("/{id}/acl")
    public ResponseEntity<Map<String, Object>> addAccessControlEntry(
            @PathVariable Long id,
            @RequestBody ReportSecurity.AccessControlEntry ace) {
        log.info("POST /api/security/{}/acl", id);

        try {
            securityService.addAccessControlEntry(id, ace);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Access control entry added");
            response.put("aceId", ace.getAceId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error adding ACE: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Remove access control entry
     */
    @DeleteMapping("/{id}/acl/{aceId}")
    public ResponseEntity<Map<String, Object>> removeAccessControlEntry(
            @PathVariable Long id,
            @PathVariable String aceId) {
        log.info("DELETE /api/security/{}/acl/{}", id, aceId);

        try {
            securityService.removeAccessControlEntry(id, aceId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Access control entry removed");
            response.put("aceId", aceId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error removing ACE: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get access control list
     */
    @GetMapping("/{id}/acl")
    public ResponseEntity<List<ReportSecurity.AccessControlEntry>> getAccessControlList(@PathVariable Long id) {
        log.info("GET /api/security/{}/acl", id);

        try {
            List<ReportSecurity.AccessControlEntry> acl = securityService.getAccessControlList(id);
            return ResponseEntity.ok(acl);

        } catch (Exception e) {
            log.error("Error fetching ACL: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get security events
     */
    @GetMapping("/{id}/events")
    public ResponseEntity<List<ReportSecurity.SecurityEvent>> getSecurityEvents(@PathVariable Long id) {
        log.info("GET /api/security/{}/events", id);

        try {
            List<ReportSecurity.SecurityEvent> events = securityService.getSecurityEvents(id);
            return ResponseEntity.ok(events);

        } catch (Exception e) {
            log.error("Error fetching security events: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Add vulnerability
     */
    @PostMapping("/{id}/vulnerability")
    public ResponseEntity<Map<String, Object>> addVulnerability(
            @PathVariable Long id,
            @RequestBody ReportSecurity.SecurityVulnerability vulnerability) {
        log.info("POST /api/security/{}/vulnerability", id);

        try {
            securityService.addVulnerability(id, vulnerability);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Vulnerability added");
            response.put("vulnerabilityId", vulnerability.getVulnerabilityId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error adding vulnerability: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Resolve vulnerability
     */
    @PostMapping("/{id}/vulnerability/{vulnId}/resolve")
    public ResponseEntity<Map<String, Object>> resolveVulnerability(
            @PathVariable Long id,
            @PathVariable String vulnId) {
        log.info("POST /api/security/{}/vulnerability/{}/resolve", id, vulnId);

        try {
            securityService.resolveVulnerability(id, vulnId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Vulnerability resolved");
            response.put("vulnerabilityId", vulnId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error resolving vulnerability: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get vulnerabilities
     */
    @GetMapping("/{id}/vulnerabilities")
    public ResponseEntity<List<ReportSecurity.SecurityVulnerability>> getVulnerabilities(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "false") boolean unresolvedOnly) {
        log.info("GET /api/security/{}/vulnerabilities?unresolvedOnly={}", id, unresolvedOnly);

        try {
            List<ReportSecurity.SecurityVulnerability> vulnerabilities = unresolvedOnly ?
                    securityService.getUnresolvedVulnerabilities(id) :
                    securityService.getVulnerabilities(id);

            return ResponseEntity.ok(vulnerabilities);

        } catch (Exception e) {
            log.error("Error fetching vulnerabilities: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Perform security scan
     */
    @PostMapping("/{id}/scan")
    public ResponseEntity<Map<String, Object>> performSecurityScan(@PathVariable Long id) {
        log.info("POST /api/security/{}/scan", id);

        try {
            Map<String, Object> scanResults = securityService.performSecurityScan(id);
            return ResponseEntity.ok(scanResults);

        } catch (IllegalArgumentException e) {
            log.error("Security config not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error performing security scan: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/security/stats");

        try {
            Map<String, Object> stats = securityService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching security statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
