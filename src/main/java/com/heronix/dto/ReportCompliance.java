package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Compliance DTO
 *
 * Represents compliance monitoring, audit trails,
 * regulatory requirements, and governance policies.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 137 - Compliance & Audit Trail
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCompliance {

    private Long complianceId;
    private String complianceName;
    private String description;

    // Compliance Framework
    private String framework; // GDPR, HIPAA, SOC2, PCI_DSS, ISO27001, FERPA
    private String frameworkVersion;
    private List<String> applicableRegulations;
    private Map<String, Object> frameworkConfig;

    // Scope
    private String scopeType; // ORGANIZATION, DEPARTMENT, SYSTEM, DATA
    private List<String> scopeEntities;
    private String dataClassification; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    private List<String> dataTypes;
    private Map<String, Object> scopeConfig;

    // Audit Trail
    private Boolean auditTrailEnabled;
    private String auditLevel; // MINIMAL, STANDARD, COMPREHENSIVE, FORENSIC
    private List<Map<String, Object>> auditEvents;
    private Long totalAuditEvents;
    private Integer retentionDays;
    private Boolean tamperProofEnabled;

    // Event Types
    private List<String> trackedEventTypes; // LOGIN, LOGOUT, CREATE, READ, UPDATE, DELETE, EXPORT, etc.
    private Boolean trackDataAccess;
    private Boolean trackDataModification;
    private Boolean trackSystemChanges;
    private Boolean trackUserActions;

    // Data Privacy
    private Boolean privacyEnabled;
    private List<String> piiFields; // Personally Identifiable Information
    private Boolean dataAnonymizationEnabled;
    private Boolean dataMaskingEnabled;
    private Boolean rightToBeForgotten; // GDPR
    private Map<String, Object> privacyConfig;

    // Access Controls
    private Boolean accessControlEnabled;
    private String accessControlModel; // RBAC, ABAC, MAC, DAC
    private List<Map<String, Object>> accessPolicies;
    private Boolean leastPrivilegeEnforced;
    private Boolean segregationOfDutiesEnabled;

    // Data Retention
    private Boolean retentionPolicyEnabled;
    private Map<String, Integer> retentionPolicies; // dataType -> days
    private Boolean autoDeleteEnabled;
    private LocalDateTime lastRetentionCheck;
    private Long recordsDeleted;

    // Encryption
    private Boolean encryptionAtRest;
    private Boolean encryptionInTransit;
    private String encryptionAlgorithm;
    private Integer keyRotationDays;
    private LocalDateTime lastKeyRotation;
    private Map<String, Object> encryptionConfig;

    // Consent Management
    private Boolean consentManagementEnabled;
    private List<Map<String, Object>> consentRecords;
    private Boolean explicitConsentRequired;
    private Boolean consentWithdrawalEnabled;
    private Map<String, Object> consentConfig;

    // Breach Detection
    private Boolean breachDetectionEnabled;
    private List<Map<String, Object>> breachIndicators;
    private Integer breachNotificationHours; // Time to notify after breach
    private List<String> notificationContacts;
    private Map<String, Object> breachConfig;

    // Compliance Controls
    private List<Map<String, Object>> controls;
    private Integer totalControls;
    private Integer implementedControls;
    private Integer passedControls;
    private Integer failedControls;
    private Double controlCompliance;

    // Assessment
    private String assessmentStatus; // NOT_STARTED, IN_PROGRESS, COMPLETED, FAILED
    private LocalDateTime lastAssessmentDate;
    private LocalDateTime nextAssessmentDate;
    private String assessmentFrequency; // MONTHLY, QUARTERLY, ANNUALLY
    private List<Map<String, Object>> assessmentResults;

    // Findings
    private List<Map<String, Object>> findings;
    private Integer criticalFindings;
    private Integer highFindings;
    private Integer mediumFindings;
    private Integer lowFindings;
    private Integer resolvedFindings;

    // Remediation
    private List<Map<String, Object>> remediationActions;
    private Integer pendingActions;
    private Integer completedActions;
    private Integer overdueActions;
    private Map<String, Object> remediationConfig;

    // Certifications
    private List<String> certifications;
    private Map<String, LocalDateTime> certificationExpiry;
    private Boolean certificationValid;
    private LocalDateTime nextAuditDate;

    // Risk Assessment
    private String overallRiskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    private Map<String, String> riskByCategory;
    private List<Map<String, Object>> riskAssessments;
    private LocalDateTime lastRiskAssessment;

    // Reporting
    private Boolean reportingEnabled;
    private List<String> reportRecipients;
    private String reportFrequency; // DAILY, WEEKLY, MONTHLY, QUARTERLY
    private LocalDateTime lastReportGenerated;
    private Map<String, Object> reportConfig;

    // Third-Party Compliance
    private Boolean vendorComplianceEnabled;
    private List<Map<String, Object>> vendors;
    private Integer compliantVendors;
    private Integer nonCompliantVendors;

    // Incident Management
    private List<Map<String, Object>> incidents;
    private Integer openIncidents;
    private Integer closedIncidents;
    private Double averageResolutionTimeHours;
    private Map<String, Object> incidentConfig;

    // Documentation
    private List<String> policyDocuments;
    private List<String> procedureDocuments;
    private Map<String, LocalDateTime> documentLastUpdated;
    private Boolean documentationComplete;

    // Training & Awareness
    private Boolean trainingEnabled;
    private List<Map<String, Object>> trainingRecords;
    private Integer trainedUsers;
    private Integer totalUsers;
    private Double trainingCompletionRate;

    // Monitoring & Alerting
    private Boolean monitoringEnabled;
    private List<String> monitoredMetrics;
    private List<Map<String, Object>> alerts;
    private Integer activeAlerts;
    private Map<String, Object> monitoringConfig;

    // Integration
    private List<String> integratedSystems;
    private Boolean ssoEnabled;
    private Boolean siemIntegration;
    private Map<String, Object> integrationConfig;

    // Performance Metrics
    private Long totalAuditRecords;
    private Long totalComplianceChecks;
    private Long passedChecks;
    private Long failedChecks;
    private Double complianceScore;

    // Status
    private String complianceStatus; // COMPLIANT, NON_COMPLIANT, PENDING, UNKNOWN
    private LocalDateTime statusLastUpdated;
    private String statusReason;

    // Metadata
    private String createdBy;
    private String assessedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Object> metadata;

    // Helper Methods
    public void addAuditEvent(Map<String, Object> event) {
        if (this.auditEvents != null) {
            this.auditEvents.add(event);
            this.totalAuditEvents = (this.totalAuditEvents != null ? this.totalAuditEvents : 0L) + 1;
        }
    }

    public void addControl(Map<String, Object> control) {
        if (this.controls != null) {
            this.controls.add(control);
            this.totalControls = (this.totalControls != null ? this.totalControls : 0) + 1;
        }
    }

    public void addFinding(Map<String, Object> finding, String severity) {
        if (this.findings != null) {
            this.findings.add(finding);

            switch (severity.toUpperCase()) {
                case "CRITICAL":
                    this.criticalFindings = (this.criticalFindings != null ? this.criticalFindings : 0) + 1;
                    break;
                case "HIGH":
                    this.highFindings = (this.highFindings != null ? this.highFindings : 0) + 1;
                    break;
                case "MEDIUM":
                    this.mediumFindings = (this.mediumFindings != null ? this.mediumFindings : 0) + 1;
                    break;
                case "LOW":
                    this.lowFindings = (this.lowFindings != null ? this.lowFindings : 0) + 1;
                    break;
            }
        }
    }

    public void addRemediationAction(Map<String, Object> action) {
        if (this.remediationActions != null) {
            this.remediationActions.add(action);
            this.pendingActions = (this.pendingActions != null ? this.pendingActions : 0) + 1;
        }
    }

    public void addIncident(Map<String, Object> incident) {
        if (this.incidents != null) {
            this.incidents.add(incident);
            this.openIncidents = (this.openIncidents != null ? this.openIncidents : 0) + 1;
        }
    }

    public void incrementComplianceCheck(boolean passed) {
        this.totalComplianceChecks = (this.totalComplianceChecks != null ? this.totalComplianceChecks : 0L) + 1;

        if (passed) {
            this.passedChecks = (this.passedChecks != null ? this.passedChecks : 0L) + 1;
            this.passedControls = (this.passedControls != null ? this.passedControls : 0) + 1;
        } else {
            this.failedChecks = (this.failedChecks != null ? this.failedChecks : 0L) + 1;
            this.failedControls = (this.failedControls != null ? this.failedControls : 0) + 1;
        }

        updateComplianceScore();
    }

    public void updateComplianceScore() {
        if (this.totalComplianceChecks != null && this.totalComplianceChecks > 0) {
            Long passed = this.passedChecks != null ? this.passedChecks : 0L;
            this.complianceScore = (passed * 100.0) / this.totalComplianceChecks;
        } else {
            this.complianceScore = 0.0;
        }

        if (this.totalControls != null && this.totalControls > 0) {
            Integer passed = this.passedControls != null ? this.passedControls : 0;
            this.controlCompliance = (passed * 100.0) / this.totalControls;
        } else {
            this.controlCompliance = 0.0;
        }
    }

    public boolean isCompliant() {
        return "COMPLIANT".equals(complianceStatus);
    }

    public boolean hasFindings() {
        return criticalFindings != null && criticalFindings > 0 ||
               highFindings != null && highFindings > 0;
    }

    public boolean requiresImmediateAction() {
        return criticalFindings != null && criticalFindings > 0 ||
               overdueActions != null && overdueActions > 0;
    }

    public int getTotalFindings() {
        int total = 0;
        if (criticalFindings != null) total += criticalFindings;
        if (highFindings != null) total += highFindings;
        if (mediumFindings != null) total += mediumFindings;
        if (lowFindings != null) total += lowFindings;
        return total;
    }

    public boolean isCertificationExpiring(int daysThreshold) {
        if (certificationExpiry == null || certificationExpiry.isEmpty()) {
            return false;
        }

        LocalDateTime threshold = LocalDateTime.now().plusDays(daysThreshold);
        return certificationExpiry.values().stream()
                .anyMatch(expiry -> expiry != null && expiry.isBefore(threshold));
    }
}
