package com.heronix.controller;

import com.heronix.dto.ReportCybersecurity;
import com.heronix.service.ReportCybersecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Cybersecurity & Threat Intelligence API Controller
 *
 * REST API endpoints for security operations, threat detection, and incident response.
 *
 * Endpoints:
 * - POST /api/cybersecurity - Create security system
 * - GET /api/cybersecurity/{id} - Get security system
 * - POST /api/cybersecurity/{id}/activate - Activate monitoring
 * - POST /api/cybersecurity/{id}/threat - Detect threat
 * - POST /api/cybersecurity/{id}/threat/mitigate - Mitigate threat
 * - POST /api/cybersecurity/{id}/vulnerability - Add vulnerability
 * - POST /api/cybersecurity/{id}/vulnerability/patch - Patch vulnerability
 * - POST /api/cybersecurity/{id}/incident - Report incident
 * - POST /api/cybersecurity/{id}/incident/resolve - Resolve incident
 * - POST /api/cybersecurity/{id}/scan - Run security scan
 * - POST /api/cybersecurity/{id}/access - Log access
 * - POST /api/cybersecurity/{id}/compliance - Add compliance check
 * - POST /api/cybersecurity/{id}/firewall - Add firewall rule
 * - POST /api/cybersecurity/{id}/intrusion - Record intrusion
 * - POST /api/cybersecurity/{id}/policy - Add security policy
 * - DELETE /api/cybersecurity/{id} - Delete security system
 * - GET /api/cybersecurity/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 116 - Report Cybersecurity & Threat Intelligence
 */
@RestController
@RequestMapping("/api/cybersecurity")
@RequiredArgsConstructor
@Slf4j
public class ReportCybersecurityApiController {

    private final ReportCybersecurityService securityService;

    /**
     * Create security system
     */
    @PostMapping
    public ResponseEntity<ReportCybersecurity> createSecuritySystem(@RequestBody ReportCybersecurity security) {
        log.info("POST /api/cybersecurity - Creating security system: {}", security.getSecurityName());

        try {
            ReportCybersecurity created = securityService.createSecuritySystem(security);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating security system", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get security system
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportCybersecurity> getSecuritySystem(@PathVariable Long id) {
        log.info("GET /api/cybersecurity/{}", id);

        try {
            return securityService.getSecuritySystem(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching security system: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Activate monitoring
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<Map<String, Object>> activateMonitoring(@PathVariable Long id) {
        log.info("POST /api/cybersecurity/{}/activate", id);

        try {
            securityService.activateMonitoring(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Security monitoring activated");
            response.put("securityId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Security system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error activating monitoring: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Detect threat
     */
    @PostMapping("/{id}/threat")
    public ResponseEntity<ReportCybersecurity.ThreatDetection> detectThreat(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/cybersecurity/{}/threat", id);

        try {
            String threatTypeStr = request.get("threatType");
            String severityStr = request.get("severity");
            String threatName = request.get("threatName");
            String sourceIp = request.get("sourceIp");
            String targetIp = request.get("targetIp");

            ReportCybersecurity.ThreatType threatType =
                    ReportCybersecurity.ThreatType.valueOf(threatTypeStr);
            ReportCybersecurity.ThreatSeverity severity =
                    ReportCybersecurity.ThreatSeverity.valueOf(severityStr);

            ReportCybersecurity.ThreatDetection threat = securityService.detectThreat(
                    id, threatType, severity, threatName, sourceIp, targetIp
            );

            return ResponseEntity.ok(threat);

        } catch (IllegalArgumentException e) {
            log.error("Security system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error detecting threat: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Mitigate threat
     */
    @PostMapping("/{id}/threat/mitigate")
    public ResponseEntity<Map<String, Object>> mitigateThreat(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/cybersecurity/{}/threat/mitigate", id);

        try {
            String threatId = request.get("threatId");
            String action = request.get("action");

            securityService.mitigateThreat(id, threatId, action);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Threat mitigated");
            response.put("threatId", threatId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Security system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error mitigating threat: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add vulnerability
     */
    @PostMapping("/{id}/vulnerability")
    public ResponseEntity<ReportCybersecurity.Vulnerability> addVulnerability(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/cybersecurity/{}/vulnerability", id);

        try {
            String cveId = (String) request.get("cveId");
            String vulnerabilityName = (String) request.get("vulnerabilityName");
            String severityStr = (String) request.get("severity");
            String affectedSystem = (String) request.get("affectedSystem");
            Double cvssScore = request.get("cvssScore") != null ?
                    ((Number) request.get("cvssScore")).doubleValue() : 0.0;

            ReportCybersecurity.VulnerabilitySeverity severity =
                    ReportCybersecurity.VulnerabilitySeverity.valueOf(severityStr);

            ReportCybersecurity.Vulnerability vulnerability = securityService.addVulnerability(
                    id, cveId, vulnerabilityName, severity, affectedSystem, cvssScore
            );

            return ResponseEntity.ok(vulnerability);

        } catch (IllegalArgumentException e) {
            log.error("Security system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding vulnerability: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Patch vulnerability
     */
    @PostMapping("/{id}/vulnerability/patch")
    public ResponseEntity<Map<String, Object>> patchVulnerability(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/cybersecurity/{}/vulnerability/patch", id);

        try {
            String vulnerabilityId = request.get("vulnerabilityId");
            String patchVersion = request.get("patchVersion");

            securityService.patchVulnerability(id, vulnerabilityId, patchVersion);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Vulnerability patched");
            response.put("vulnerabilityId", vulnerabilityId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Security system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error patching vulnerability: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Report incident
     */
    @PostMapping("/{id}/incident")
    public ResponseEntity<ReportCybersecurity.SecurityIncident> reportIncident(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/cybersecurity/{}/incident", id);

        try {
            String incidentName = request.get("incidentName");
            String severityStr = request.get("severity");
            String description = request.get("description");
            String reportedBy = request.get("reportedBy");

            ReportCybersecurity.ThreatSeverity severity =
                    ReportCybersecurity.ThreatSeverity.valueOf(severityStr);

            ReportCybersecurity.SecurityIncident incident = securityService.reportIncident(
                    id, incidentName, severity, description, reportedBy
            );

            return ResponseEntity.ok(incident);

        } catch (IllegalArgumentException e) {
            log.error("Security system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error reporting incident: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Resolve incident
     */
    @PostMapping("/{id}/incident/resolve")
    public ResponseEntity<Map<String, Object>> resolveIncident(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/cybersecurity/{}/incident/resolve", id);

        try {
            String incidentId = request.get("incidentId");
            String resolution = request.get("resolution");

            securityService.resolveIncident(id, incidentId, resolution);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Incident resolved");
            response.put("incidentId", incidentId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Security system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error resolving incident: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Run security scan
     */
    @PostMapping("/{id}/scan")
    public ResponseEntity<ReportCybersecurity.SecurityScan> runSecurityScan(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/cybersecurity/{}/scan", id);

        try {
            String scanTypeStr = request.get("scanType");
            String targetSystem = request.get("targetSystem");

            ReportCybersecurity.ScanType scanType =
                    ReportCybersecurity.ScanType.valueOf(scanTypeStr);

            ReportCybersecurity.SecurityScan scan = securityService.runSecurityScan(
                    id, scanType, targetSystem
            );

            return ResponseEntity.ok(scan);

        } catch (IllegalArgumentException e) {
            log.error("Security system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error running security scan: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Log access
     */
    @PostMapping("/{id}/access")
    public ResponseEntity<ReportCybersecurity.AccessLog> logAccess(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/cybersecurity/{}/access", id);

        try {
            String accessTypeStr = (String) request.get("accessType");
            String userId = (String) request.get("userId");
            String userName = (String) request.get("userName");
            String ipAddress = (String) request.get("ipAddress");
            String resource = (String) request.get("resource");
            Boolean successful = (Boolean) request.get("successful");

            ReportCybersecurity.AccessType accessType =
                    ReportCybersecurity.AccessType.valueOf(accessTypeStr);

            ReportCybersecurity.AccessLog log = securityService.logAccess(
                    id, accessType, userId, userName, ipAddress, resource,
                    successful != null && successful
            );

            return ResponseEntity.ok(log);

        } catch (IllegalArgumentException e) {
            log.error("Security system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error logging access: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add compliance check
     */
    @PostMapping("/{id}/compliance")
    public ResponseEntity<ReportCybersecurity.ComplianceCheck> addComplianceCheck(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/cybersecurity/{}/compliance", id);

        try {
            String complianceFramework = (String) request.get("complianceFramework");
            String checkName = (String) request.get("checkName");
            String requirement = (String) request.get("requirement");
            Boolean passed = (Boolean) request.get("passed");

            ReportCybersecurity.ComplianceCheck check = securityService.addComplianceCheck(
                    id, complianceFramework, checkName, requirement, passed != null && passed
            );

            return ResponseEntity.ok(check);

        } catch (IllegalArgumentException e) {
            log.error("Security system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding compliance check: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add firewall rule
     */
    @PostMapping("/{id}/firewall")
    public ResponseEntity<ReportCybersecurity.FirewallRule> addFirewallRule(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/cybersecurity/{}/firewall", id);

        try {
            String ruleName = (String) request.get("ruleName");
            String action = (String) request.get("action");
            String sourceIp = (String) request.get("sourceIp");
            String destinationIp = (String) request.get("destinationIp");
            Integer port = request.get("port") != null ?
                    ((Number) request.get("port")).intValue() : 80;

            ReportCybersecurity.FirewallRule rule = securityService.addFirewallRule(
                    id, ruleName, action, sourceIp, destinationIp, port
            );

            return ResponseEntity.ok(rule);

        } catch (IllegalArgumentException e) {
            log.error("Security system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding firewall rule: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Record intrusion event
     */
    @PostMapping("/{id}/intrusion")
    public ResponseEntity<ReportCybersecurity.IntrusionEvent> recordIntrusionEvent(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/cybersecurity/{}/intrusion", id);

        try {
            String eventType = (String) request.get("eventType");
            String severityStr = (String) request.get("severity");
            String sourceIp = (String) request.get("sourceIp");
            String targetIp = (String) request.get("targetIp");
            Boolean blocked = (Boolean) request.get("blocked");

            ReportCybersecurity.ThreatSeverity severity =
                    ReportCybersecurity.ThreatSeverity.valueOf(severityStr);

            ReportCybersecurity.IntrusionEvent event = securityService.recordIntrusionEvent(
                    id, eventType, severity, sourceIp, targetIp, blocked != null && blocked
            );

            return ResponseEntity.ok(event);

        } catch (IllegalArgumentException e) {
            log.error("Security system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error recording intrusion event: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add security policy
     */
    @PostMapping("/{id}/policy")
    public ResponseEntity<ReportCybersecurity.SecurityPolicy> addSecurityPolicy(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/cybersecurity/{}/policy", id);

        try {
            String policyName = (String) request.get("policyName");
            String policyType = (String) request.get("policyType");
            String description = (String) request.get("description");
            @SuppressWarnings("unchecked")
            List<String> rules = (List<String>) request.get("rules");

            ReportCybersecurity.SecurityPolicy policy = securityService.addSecurityPolicy(
                    id, policyName, policyType, description, rules
            );

            return ResponseEntity.ok(policy);

        } catch (IllegalArgumentException e) {
            log.error("Security system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding security policy: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete security system
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteSecuritySystem(@PathVariable Long id) {
        log.info("DELETE /api/cybersecurity/{}", id);

        try {
            securityService.deleteSecuritySystem(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Security system deleted");
            response.put("securityId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting security system: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/cybersecurity/stats");

        try {
            Map<String, Object> stats = securityService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching security statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
