package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report DevSecOps DTO
 *
 * Represents DevSecOps practices, security automation, continuous security testing,
 * vulnerability management, and shift-left security integration.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 143 - DevSecOps & Security Automation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDevSecOps {

    private Long devsecopsId;
    private String devsecopsName;
    private String description;

    // Security Pipeline Configuration
    private String pipelineType; // CI_CD, SAST, DAST, IAST, RASP, SCA
    private String securityFramework; // OWASP, SANS, NIST, CIS, ISO27001
    private Boolean shiftLeftEnabled;
    private Boolean continuousSecurityEnabled;
    private Map<String, Object> pipelineConfig;

    // Static Application Security Testing (SAST)
    private Boolean sastEnabled;
    private String sastTool; // SONARQUBE, CHECKMARX, FORTIFY, VERACODE, SEMGREP
    private List<String> sastRules;
    private Integer sastVulnerabilitiesFound;
    private Integer sastCriticalIssues;
    private Integer sastHighIssues;
    private Integer sastMediumIssues;
    private Integer sastLowIssues;
    private Map<String, Object> sastConfig;

    // Dynamic Application Security Testing (DAST)
    private Boolean dastEnabled;
    private String dastTool; // OWASP_ZAP, BURP_SUITE, ACUNETIX, NETSPARKER
    private String targetUrl;
    private Integer dastVulnerabilitiesFound;
    private Integer dastCriticalIssues;
    private Integer dastHighIssues;
    private Map<String, Object> dastConfig;

    // Software Composition Analysis (SCA)
    private Boolean scaEnabled;
    private String scaTool; // SNYK, WHITESOURCE, BLACK_DUCK, SONATYPE
    private Integer dependenciesScanned;
    private Integer vulnerableDependencies;
    private Integer outdatedDependencies;
    private List<String> knownVulnerabilities;
    private Map<String, Object> scaConfig;

    // Container Security
    private Boolean containerSecurityEnabled;
    private String containerScanTool; // TRIVY, CLAIR, ANCHORE, AQUA
    private Integer imagesScanned;
    private Integer vulnerableImages;
    private List<String> blockedImages;
    private Map<String, Object> containerSecurityConfig;

    // Infrastructure as Code Security
    private Boolean iacSecurityEnabled;
    private String iacScanTool; // CHECKOV, TERRASCAN, TFSEC, KICS
    private Integer iacFilesScanned;
    private Integer iacViolations;
    private List<String> iacBestPractices;
    private Map<String, Object> iacConfig;

    // Secret Management
    private Boolean secretManagementEnabled;
    private String secretsTool; // VAULT, AWS_SECRETS_MANAGER, AZURE_KEY_VAULT, DOPPLER
    private Integer secretsStored;
    private Integer secretsRotated;
    private Integer secretLeaksDetected;
    private Boolean secretRotationEnabled;
    private Integer rotationIntervalDays;
    private Map<String, Object> secretsConfig;

    // Vulnerability Management
    private Boolean vulnerabilityManagementEnabled;
    private Integer totalVulnerabilities;
    private Integer remediatedVulnerabilities;
    private Integer acceptedRisks;
    private Integer falsePositives;
    private Double meanTimeToRemediate; // hours
    private List<Map<String, Object>> vulnerabilities;
    private Map<String, Object> vulnConfig;

    // Security Gates
    private Boolean securityGatesEnabled;
    private List<String> gateRules;
    private Integer gatesExecuted;
    private Integer gatesPassed;
    private Integer gatesFailed;
    private Double gateSuccessRate;
    private Map<String, Object> gateConfig;

    // Compliance Checking
    private Boolean complianceCheckingEnabled;
    private List<String> complianceFrameworks;
    private Integer complianceChecks;
    private Integer compliancePassed;
    private Integer complianceFailed;
    private Double complianceScore;
    private Map<String, Object> complianceConfig;

    // Threat Modeling
    private Boolean threatModelingEnabled;
    private String threatModelingMethod; // STRIDE, PASTA, DREAD, OCTAVE
    private Integer threatsIdentified;
    private Integer threatsMitigated;
    private List<Map<String, Object>> threatModels;
    private Map<String, Object> threatConfig;

    // Penetration Testing
    private Boolean pentestingEnabled;
    private String pentestingType; // WHITE_BOX, BLACK_BOX, GRAY_BOX
    private LocalDateTime lastPentest;
    private Integer pentestFindingsCritical;
    private Integer pentestFindingsHigh;
    private Integer pentestFindingsMedium;
    private List<Map<String, Object>> pentestReports;

    // Security Monitoring
    private Boolean securityMonitoringEnabled;
    private String siemTool; // SPLUNK, ELASTIC_SECURITY, QRADAR, SENTINEL
    private Long securityEventsDetected;
    private Long securityIncidents;
    private Integer securityIncidentsResolved;
    private Double meanTimeToDetect; // minutes
    private Double meanTimeToRespond; // minutes
    private Map<String, Object> monitoringConfig;

    // API Security
    private Boolean apiSecurityEnabled;
    private String apiSecurityTool; // API_GATEWAY, OAUTH, JWT, RATE_LIMITING
    private Integer apisProtected;
    private Long apiCallsBlocked;
    private Integer apiVulnerabilitiesFound;
    private Map<String, Object> apiSecurityConfig;

    // Security Training
    private Boolean securityTrainingEnabled;
    private Integer developersTrained;
    private Integer trainingSessionsCompleted;
    private Double averageTrainingScore;
    private List<String> trainingTopics;
    private Map<String, Object> trainingConfig;

    // Security Automation
    private Boolean automatedRemediationEnabled;
    private Integer automatedFixes;
    private Integer manualReviewsRequired;
    private Double automationRate;
    private List<String> automationRules;
    private Map<String, Object> automationConfig;

    // Security Metrics
    private Double securityPosture; // 0-100 score
    private Double riskScore; // 0-100 score
    private Integer securityDebt; // hours
    private Integer securitySmells;
    private Double codeSecurityRating; // A-F
    private Map<String, Object> securityMetrics;

    // Audit & Reporting
    private Boolean auditLoggingEnabled;
    private Long auditEventsLogged;
    private Integer reportsGenerated;
    private List<String> reportTypes;
    private LocalDateTime lastAudit;
    private Map<String, Object> auditConfig;

    // Policy Enforcement
    private Boolean policyEnforcementEnabled;
    private List<String> securityPolicies;
    private Integer policiesEnforced;
    private Integer policyViolations;
    private Map<String, Object> policyConfig;

    // Status
    private String devsecopsStatus; // INITIALIZING, SCANNING, REMEDIATING, COMPLIANT, NON_COMPLIANT
    private LocalDateTime activatedAt;
    private LocalDateTime lastScan;
    private LocalDateTime lastRemediation;

    // Metadata
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Object> metadata;

    // Helper Methods

    /**
     * Record vulnerability
     */
    public void recordVulnerability(Map<String, Object> vulnerability) {
        if (this.vulnerabilities != null) {
            this.vulnerabilities.add(vulnerability);
            this.totalVulnerabilities = (this.totalVulnerabilities != null ? this.totalVulnerabilities : 0) + 1;

            String severity = (String) vulnerability.get("severity");
            if (severity != null) {
                switch (severity) {
                    case "CRITICAL":
                        this.sastCriticalIssues = (this.sastCriticalIssues != null ? this.sastCriticalIssues : 0) + 1;
                        break;
                    case "HIGH":
                        this.sastHighIssues = (this.sastHighIssues != null ? this.sastHighIssues : 0) + 1;
                        break;
                    case "MEDIUM":
                        this.sastMediumIssues = (this.sastMediumIssues != null ? this.sastMediumIssues : 0) + 1;
                        break;
                    case "LOW":
                        this.sastLowIssues = (this.sastLowIssues != null ? this.sastLowIssues : 0) + 1;
                        break;
                }
            }
        }
    }

    /**
     * Remediate vulnerability
     */
    public void remediateVulnerability() {
        this.remediatedVulnerabilities = (this.remediatedVulnerabilities != null ? this.remediatedVulnerabilities : 0) + 1;
        this.lastRemediation = LocalDateTime.now();
        updateSecurityPosture();
    }

    /**
     * Execute security gate
     */
    public void executeGate(boolean passed) {
        this.gatesExecuted = (this.gatesExecuted != null ? this.gatesExecuted : 0) + 1;

        if (passed) {
            this.gatesPassed = (this.gatesPassed != null ? this.gatesPassed : 0) + 1;
        } else {
            this.gatesFailed = (this.gatesFailed != null ? this.gatesFailed : 0) + 1;
        }

        updateGateSuccessRate();
    }

    /**
     * Record security incident
     */
    public void recordIncident() {
        this.securityIncidents = (this.securityIncidents != null ? this.securityIncidents : 0L) + 1L;
    }

    /**
     * Resolve security incident
     */
    public void resolveIncident() {
        this.securityIncidentsResolved = (this.securityIncidentsResolved != null ? this.securityIncidentsResolved : 0) + 1;
    }

    /**
     * Update gate success rate
     */
    private void updateGateSuccessRate() {
        if (this.gatesExecuted != null && this.gatesExecuted > 0) {
            Integer passed = this.gatesPassed != null ? this.gatesPassed : 0;
            this.gateSuccessRate = (passed * 100.0) / this.gatesExecuted;
        } else {
            this.gateSuccessRate = 0.0;
        }
    }

    /**
     * Update security posture score
     */
    private void updateSecurityPosture() {
        if (this.totalVulnerabilities != null && this.totalVulnerabilities > 0) {
            Integer remediated = this.remediatedVulnerabilities != null ? this.remediatedVulnerabilities : 0;
            double remediationRate = (remediated * 100.0) / this.totalVulnerabilities;

            // Base score on remediation rate, compliance, and gate success
            double complianceScore = this.complianceScore != null ? this.complianceScore : 50.0;
            double gateScore = this.gateSuccessRate != null ? this.gateSuccessRate : 50.0;

            this.securityPosture = (remediationRate * 0.4) + (complianceScore * 0.3) + (gateScore * 0.3);
        } else {
            this.securityPosture = 100.0;
        }
    }

    /**
     * Calculate risk score
     */
    public Double calculateRiskScore() {
        double score = 0.0;

        if (this.sastCriticalIssues != null && this.sastCriticalIssues > 0) {
            score += this.sastCriticalIssues * 10.0;
        }
        if (this.sastHighIssues != null && this.sastHighIssues > 0) {
            score += this.sastHighIssues * 5.0;
        }
        if (this.vulnerableDependencies != null && this.vulnerableDependencies > 0) {
            score += this.vulnerableDependencies * 3.0;
        }
        if (this.secretLeaksDetected != null && this.secretLeaksDetected > 0) {
            score += this.secretLeaksDetected * 20.0;
        }

        this.riskScore = Math.min(score, 100.0);
        return this.riskScore;
    }

    /**
     * Check if compliant
     */
    public boolean isCompliant() {
        return "COMPLIANT".equals(this.devsecopsStatus) &&
               this.complianceScore != null && this.complianceScore >= 80.0;
    }

    /**
     * Check if high risk
     */
    public boolean isHighRisk() {
        calculateRiskScore();
        return this.riskScore != null && this.riskScore >= 70.0;
    }

    /**
     * Check if gates passing
     */
    public boolean areGatesPassing() {
        return this.gateSuccessRate != null && this.gateSuccessRate >= 90.0;
    }

    /**
     * Get vulnerability count by severity
     */
    public Map<String, Integer> getVulnerabilitiesBySeverity() {
        return Map.of(
                "CRITICAL", this.sastCriticalIssues != null ? this.sastCriticalIssues : 0,
                "HIGH", this.sastHighIssues != null ? this.sastHighIssues : 0,
                "MEDIUM", this.sastMediumIssues != null ? this.sastMediumIssues : 0,
                "LOW", this.sastLowIssues != null ? this.sastLowIssues : 0
        );
    }
}
