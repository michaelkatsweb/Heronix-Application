package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Cybersecurity & Threat Intelligence DTO
 *
 * Manages security operations, threat detection, vulnerability assessment, and incident response
 * for educational institution security and student data protection.
 *
 * Educational Use Cases:
 * - Student data protection and privacy compliance (FERPA, COPPA)
 * - Security monitoring for learning management systems
 * - Threat detection for educational networks
 * - Vulnerability assessment for student portals
 * - Incident response for data breaches
 * - Security awareness training tracking
 * - Access control and authentication monitoring
 * - Compliance reporting and audit trails
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 116 - Report Cybersecurity & Threat Intelligence
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCybersecurity {

    // Basic Information
    private Long securityId;
    private String securityName;
    private String description;
    private SecurityStatus status;
    private String organizationId;
    private String complianceFramework;

    // Configuration
    private SecurityLevel securityLevel;
    private Boolean autoResponse;
    private Boolean realTimeMonitoring;
    private Integer alertThreshold;
    private String alertEmail;

    // State
    private Boolean isActive;
    private Boolean isMonitoring;
    private LocalDateTime createdAt;
    private LocalDateTime activatedAt;
    private LocalDateTime lastScanAt;
    private String createdBy;

    // Threats
    private List<ThreatDetection> threats;
    private Map<String, ThreatDetection> threatRegistry;

    // Vulnerabilities
    private List<Vulnerability> vulnerabilities;
    private Map<String, Vulnerability> vulnerabilityRegistry;

    // Incidents
    private List<SecurityIncident> incidents;
    private Map<String, SecurityIncident> incidentRegistry;

    // Security Scans
    private List<SecurityScan> scans;
    private Map<String, SecurityScan> scanRegistry;

    // Access Logs
    private List<AccessLog> accessLogs;
    private Map<String, AccessLog> accessRegistry;

    // Compliance Checks
    private List<ComplianceCheck> complianceChecks;
    private Map<String, ComplianceCheck> complianceRegistry;

    // Firewall Rules
    private List<FirewallRule> firewallRules;
    private Map<String, FirewallRule> firewallRegistry;

    // Intrusion Detection
    private List<IntrusionEvent> intrusionEvents;
    private Map<String, IntrusionEvent> intrusionRegistry;

    // Security Policies
    private List<SecurityPolicy> policies;
    private Map<String, SecurityPolicy> policyRegistry;

    // Metrics
    private Long totalThreats;
    private Long criticalThreats;
    private Long mitigatedThreats;
    private Long totalVulnerabilities;
    private Long criticalVulnerabilities;
    private Long patchedVulnerabilities;
    private Long totalIncidents;
    private Long resolvedIncidents;
    private Long totalScans;
    private Long totalAccessLogs;
    private Long suspiciousAccess;
    private Long totalComplianceChecks;
    private Long passedComplianceChecks;
    private Double securityScore;
    private Double complianceScore;

    // Events
    private List<SecurityEvent> events;

    /**
     * Security status enumeration
     */
    public enum SecurityStatus {
        INITIALIZING,
        ACTIVE,
        MONITORING,
        ALERT,
        INCIDENT,
        MAINTENANCE,
        COMPROMISED,
        SECURE
    }

    /**
     * Security level enumeration
     */
    public enum SecurityLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL,
        MAXIMUM
    }

    /**
     * Threat severity enumeration
     */
    public enum ThreatSeverity {
        INFO,
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    /**
     * Threat type enumeration
     */
    public enum ThreatType {
        MALWARE,
        PHISHING,
        RANSOMWARE,
        DDoS,
        SQL_INJECTION,
        XSS,
        BRUTE_FORCE,
        ZERO_DAY,
        INSIDER_THREAT,
        DATA_BREACH,
        UNAUTHORIZED_ACCESS,
        SOCIAL_ENGINEERING
    }

    /**
     * Vulnerability severity enumeration
     */
    public enum VulnerabilitySeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    /**
     * Incident status enumeration
     */
    public enum IncidentStatus {
        DETECTED,
        INVESTIGATING,
        CONTAINED,
        MITIGATING,
        RESOLVED,
        CLOSED
    }

    /**
     * Scan type enumeration
     */
    public enum ScanType {
        VULNERABILITY,
        MALWARE,
        NETWORK,
        APPLICATION,
        DATABASE,
        PENETRATION,
        COMPLIANCE
    }

    /**
     * Access type enumeration
     */
    public enum AccessType {
        LOGIN,
        LOGOUT,
        FILE_ACCESS,
        DATABASE_QUERY,
        API_CALL,
        ADMIN_ACTION,
        DATA_EXPORT,
        CONFIGURATION_CHANGE
    }

    /**
     * Threat detection data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThreatDetection {
        private String threatId;
        private ThreatType threatType;
        private ThreatSeverity severity;
        private String threatName;
        private String description;
        private String sourceIp;
        private String targetIp;
        private Integer port;
        private String protocol;
        private Boolean isMitigated;
        private String mitigationAction;
        private LocalDateTime detectedAt;
        private LocalDateTime mitigatedAt;
        private Map<String, Object> threatData;
        private List<String> affectedSystems;
    }

    /**
     * Vulnerability data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Vulnerability {
        private String vulnerabilityId;
        private String cveId;
        private String vulnerabilityName;
        private VulnerabilitySeverity severity;
        private String affectedSystem;
        private String affectedVersion;
        private String description;
        private Double cvssScore;
        private Boolean isPatched;
        private String patchVersion;
        private LocalDateTime discoveredAt;
        private LocalDateTime patchedAt;
        private List<String> remediationSteps;
        private Map<String, Object> metadata;
    }

    /**
     * Security incident data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecurityIncident {
        private String incidentId;
        private String incidentName;
        private IncidentStatus status;
        private ThreatSeverity severity;
        private String description;
        private String reportedBy;
        private String assignedTo;
        private LocalDateTime reportedAt;
        private LocalDateTime detectedAt;
        private LocalDateTime resolvedAt;
        private List<String> affectedSystems;
        private List<String> affectedUsers;
        private String rootCause;
        private String resolution;
        private Map<String, Object> incidentData;
    }

    /**
     * Security scan data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecurityScan {
        private String scanId;
        private ScanType scanType;
        private String targetSystem;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private Long duration; // milliseconds
        private Integer threatsFound;
        private Integer vulnerabilitiesFound;
        private Boolean passed;
        private String scanResult;
        private Map<String, Object> scanData;
        private List<String> findings;
    }

    /**
     * Access log data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccessLog {
        private String logId;
        private AccessType accessType;
        private String userId;
        private String userName;
        private String ipAddress;
        private String resource;
        private String action;
        private Boolean successful;
        private Boolean suspicious;
        private String suspicionReason;
        private LocalDateTime timestamp;
        private String userAgent;
        private String location;
        private Map<String, Object> metadata;
    }

    /**
     * Compliance check data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceCheck {
        private String checkId;
        private String complianceFramework; // FERPA, COPPA, GDPR, etc.
        private String checkName;
        private String requirement;
        private Boolean passed;
        private String result;
        private String evidence;
        private LocalDateTime checkedAt;
        private String checkedBy;
        private List<String> findings;
        private Map<String, Object> checkData;
    }

    /**
     * Firewall rule data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FirewallRule {
        private String ruleId;
        private String ruleName;
        private String action; // ALLOW, DENY, LOG
        private String sourceIp;
        private String destinationIp;
        private Integer port;
        private String protocol;
        private Boolean isActive;
        private Integer priority;
        private LocalDateTime createdAt;
        private LocalDateTime lastModifiedAt;
        private String createdBy;
        private Long hitCount;
    }

    /**
     * Intrusion event data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IntrusionEvent {
        private String eventId;
        private String eventType;
        private ThreatSeverity severity;
        private String sourceIp;
        private String targetIp;
        private Integer port;
        private String protocol;
        private String signature;
        private Boolean blocked;
        private String action;
        private LocalDateTime detectedAt;
        private Map<String, Object> eventData;
    }

    /**
     * Security policy data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecurityPolicy {
        private String policyId;
        private String policyName;
        private String policyType;
        private String description;
        private Boolean isEnforced;
        private Integer violationCount;
        private LocalDateTime createdAt;
        private LocalDateTime lastUpdatedAt;
        private String createdBy;
        private List<String> rules;
        private Map<String, Object> policyData;
    }

    /**
     * Security event data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecurityEvent {
        private String eventId;
        private String eventType;
        private String description;
        private String targetType;
        private String targetId;
        private LocalDateTime timestamp;
        private String triggeredBy;
        private Map<String, Object> eventData;
    }

    // Helper methods

    /**
     * Activate security monitoring
     */
    public void activateMonitoring() {
        this.status = SecurityStatus.MONITORING;
        this.isActive = true;
        this.isMonitoring = true;
        this.activatedAt = LocalDateTime.now();
        recordEvent("MONITORING_ACTIVATED", "Security monitoring activated", "SECURITY",
                securityId != null ? securityId.toString() : null);
    }

    /**
     * Detect threat
     */
    public void detectThreat(ThreatDetection threat) {
        if (threats == null) {
            threats = new ArrayList<>();
        }
        threats.add(threat);

        if (threatRegistry == null) {
            threatRegistry = new HashMap<>();
        }
        threatRegistry.put(threat.getThreatId(), threat);

        totalThreats = (totalThreats != null ? totalThreats : 0L) + 1;
        if (threat.getSeverity() == ThreatSeverity.CRITICAL || threat.getSeverity() == ThreatSeverity.HIGH) {
            criticalThreats = (criticalThreats != null ? criticalThreats : 0L) + 1;
            this.status = SecurityStatus.ALERT;
        }

        recordEvent("THREAT_DETECTED", "Security threat detected", "THREAT", threat.getThreatId());
    }

    /**
     * Mitigate threat
     */
    public void mitigateThreat(String threatId, String action) {
        ThreatDetection threat = threatRegistry != null ? threatRegistry.get(threatId) : null;
        if (threat != null) {
            threat.setIsMitigated(true);
            threat.setMitigationAction(action);
            threat.setMitigatedAt(LocalDateTime.now());

            mitigatedThreats = (mitigatedThreats != null ? mitigatedThreats : 0L) + 1;
        }
    }

    /**
     * Add vulnerability
     */
    public void addVulnerability(Vulnerability vulnerability) {
        if (vulnerabilities == null) {
            vulnerabilities = new ArrayList<>();
        }
        vulnerabilities.add(vulnerability);

        if (vulnerabilityRegistry == null) {
            vulnerabilityRegistry = new HashMap<>();
        }
        vulnerabilityRegistry.put(vulnerability.getVulnerabilityId(), vulnerability);

        totalVulnerabilities = (totalVulnerabilities != null ? totalVulnerabilities : 0L) + 1;
        if (vulnerability.getSeverity() == VulnerabilitySeverity.CRITICAL ||
            vulnerability.getSeverity() == VulnerabilitySeverity.HIGH) {
            criticalVulnerabilities = (criticalVulnerabilities != null ? criticalVulnerabilities : 0L) + 1;
        }

        recordEvent("VULNERABILITY_FOUND", "Vulnerability discovered", "VULNERABILITY", vulnerability.getVulnerabilityId());
    }

    /**
     * Patch vulnerability
     */
    public void patchVulnerability(String vulnerabilityId, String patchVersion) {
        Vulnerability vulnerability = vulnerabilityRegistry != null ? vulnerabilityRegistry.get(vulnerabilityId) : null;
        if (vulnerability != null) {
            vulnerability.setIsPatched(true);
            vulnerability.setPatchVersion(patchVersion);
            vulnerability.setPatchedAt(LocalDateTime.now());

            patchedVulnerabilities = (patchedVulnerabilities != null ? patchedVulnerabilities : 0L) + 1;
        }
    }

    /**
     * Report incident
     */
    public void reportIncident(SecurityIncident incident) {
        if (incidents == null) {
            incidents = new ArrayList<>();
        }
        incidents.add(incident);

        if (incidentRegistry == null) {
            incidentRegistry = new HashMap<>();
        }
        incidentRegistry.put(incident.getIncidentId(), incident);

        totalIncidents = (totalIncidents != null ? totalIncidents : 0L) + 1;
        this.status = SecurityStatus.INCIDENT;

        recordEvent("INCIDENT_REPORTED", "Security incident reported", "INCIDENT", incident.getIncidentId());
    }

    /**
     * Resolve incident
     */
    public void resolveIncident(String incidentId, String resolution) {
        SecurityIncident incident = incidentRegistry != null ? incidentRegistry.get(incidentId) : null;
        if (incident != null) {
            incident.setStatus(IncidentStatus.RESOLVED);
            incident.setResolution(resolution);
            incident.setResolvedAt(LocalDateTime.now());

            resolvedIncidents = (resolvedIncidents != null ? resolvedIncidents : 0L) + 1;
        }
    }

    /**
     * Add security scan
     */
    public void addSecurityScan(SecurityScan scan) {
        if (scans == null) {
            scans = new ArrayList<>();
        }
        scans.add(scan);

        if (scanRegistry == null) {
            scanRegistry = new HashMap<>();
        }
        scanRegistry.put(scan.getScanId(), scan);

        totalScans = (totalScans != null ? totalScans : 0L) + 1;
        lastScanAt = LocalDateTime.now();

        recordEvent("SCAN_COMPLETED", "Security scan completed", "SCAN", scan.getScanId());
    }

    /**
     * Log access
     */
    public void logAccess(AccessLog log) {
        if (accessLogs == null) {
            accessLogs = new ArrayList<>();
        }
        accessLogs.add(log);

        if (accessRegistry == null) {
            accessRegistry = new HashMap<>();
        }
        accessRegistry.put(log.getLogId(), log);

        totalAccessLogs = (totalAccessLogs != null ? totalAccessLogs : 0L) + 1;
        if (Boolean.TRUE.equals(log.getSuspicious())) {
            suspiciousAccess = (suspiciousAccess != null ? suspiciousAccess : 0L) + 1;
        }

        recordEvent("ACCESS_LOGGED", "Access event logged", "ACCESS", log.getLogId());
    }

    /**
     * Add compliance check
     */
    public void addComplianceCheck(ComplianceCheck check) {
        if (complianceChecks == null) {
            complianceChecks = new ArrayList<>();
        }
        complianceChecks.add(check);

        if (complianceRegistry == null) {
            complianceRegistry = new HashMap<>();
        }
        complianceRegistry.put(check.getCheckId(), check);

        totalComplianceChecks = (totalComplianceChecks != null ? totalComplianceChecks : 0L) + 1;
        if (Boolean.TRUE.equals(check.getPassed())) {
            passedComplianceChecks = (passedComplianceChecks != null ? passedComplianceChecks : 0L) + 1;
        }

        // Update compliance score
        if (totalComplianceChecks > 0) {
            complianceScore = (passedComplianceChecks.doubleValue() / totalComplianceChecks.doubleValue()) * 100.0;
        }

        recordEvent("COMPLIANCE_CHECKED", "Compliance check completed", "COMPLIANCE", check.getCheckId());
    }

    /**
     * Record security event
     */
    private void recordEvent(String eventType, String description, String targetType, String targetId) {
        if (events == null) {
            events = new ArrayList<>();
        }

        SecurityEvent event = SecurityEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType(eventType)
                .description(description)
                .targetType(targetType)
                .targetId(targetId)
                .timestamp(LocalDateTime.now())
                .triggeredBy(createdBy)
                .build();

        events.add(event);
    }
}
