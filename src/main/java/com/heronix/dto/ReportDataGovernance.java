package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Data Governance & Compliance DTO
 *
 * Manages data governance, regulatory compliance, and audit trails.
 *
 * Features:
 * - Data classification and labeling
 * - Regulatory compliance (GDPR, FERPA, COPPA, HIPAA)
 * - Audit trail and logging
 * - Data retention policies
 * - Access control and permissions
 * - Data lineage tracking
 * - Privacy impact assessments
 * - Consent management
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 108 - Report Data Governance & Compliance
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDataGovernance {

    // Framework Information
    private Long frameworkId;
    private String frameworkName;
    private String description;
    private FrameworkStatus status;
    private Boolean isActive;

    // Compliance Standards
    private List<ComplianceStandard> standards;
    private Map<String, ComplianceStandard> standardRegistry;
    private Integer totalStandards;
    private Integer activeStandards;

    // Data Assets
    private List<DataAsset> dataAssets;
    private Map<String, DataAsset> assetRegistry;
    private Long totalAssets;
    private Long classifiedAssets;

    // Policies
    private List<GovernancePolicy> policies;
    private Map<String, GovernancePolicy> policyRegistry;
    private Integer totalPolicies;
    private Integer activePolicies;

    // Audit Logs
    private List<AuditLog> auditLogs;
    private Map<String, AuditLog> auditRegistry;
    private Long totalAudits;

    // Data Access Requests
    private List<DataAccessRequest> accessRequests;
    private Map<String, DataAccessRequest> requestRegistry;
    private Long totalRequests;
    private Long approvedRequests;
    private Long deniedRequests;

    // Retention Rules
    private List<RetentionRule> retentionRules;
    private Map<String, RetentionRule> retentionRegistry;

    // Privacy Assessments
    private List<PrivacyAssessment> assessments;
    private Map<String, PrivacyAssessment> assessmentRegistry;
    private Long totalAssessments;

    // Consent Records
    private List<ConsentRecord> consents;
    private Map<String, ConsentRecord> consentRegistry;
    private Long totalConsents;
    private Long activeConsents;

    // Data Lineage
    private List<DataLineage> lineageRecords;
    private Map<String, DataLineage> lineageRegistry;

    // Violations
    private List<ComplianceViolation> violations;
    private Map<String, ComplianceViolation> violationRegistry;
    private Long totalViolations;
    private Long resolvedViolations;

    // Metrics
    private GovernanceMetrics metrics;
    private LocalDateTime lastMetricsUpdate;

    // Events
    private List<GovernanceEvent> events;
    private LocalDateTime lastEventAt;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime activatedAt;
    private LocalDateTime lastAuditAt;

    /**
     * Framework Status
     */
    public enum FrameworkStatus {
        INITIALIZING,
        CONFIGURING,
        ACTIVE,
        AUDITING,
        UPDATING,
        SUSPENDED
    }

    /**
     * Compliance Standard Type
     */
    public enum ComplianceStandardType {
        GDPR,
        FERPA,
        COPPA,
        HIPAA,
        CCPA,
        SOC2,
        ISO27001,
        CUSTOM
    }

    /**
     * Data Classification
     */
    public enum DataClassification {
        PUBLIC,
        INTERNAL,
        CONFIDENTIAL,
        RESTRICTED,
        SENSITIVE,
        HIGHLY_SENSITIVE
    }

    /**
     * Policy Type
     */
    public enum PolicyType {
        DATA_RETENTION,
        ACCESS_CONTROL,
        DATA_QUALITY,
        PRIVACY,
        SECURITY,
        ARCHIVAL,
        DELETION,
        ENCRYPTION
    }

    /**
     * Access Request Status
     */
    public enum AccessRequestStatus {
        SUBMITTED,
        UNDER_REVIEW,
        APPROVED,
        DENIED,
        REVOKED,
        EXPIRED
    }

    /**
     * Audit Action Type
     */
    public enum AuditActionType {
        CREATE,
        READ,
        UPDATE,
        DELETE,
        EXPORT,
        SHARE,
        DOWNLOAD,
        PRINT,
        ACCESS_GRANTED,
        ACCESS_DENIED
    }

    /**
     * Violation Severity
     */
    public enum ViolationSeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    /**
     * Compliance Standard
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceStandard {
        private String standardId;
        private String standardName;
        private ComplianceStandardType standardType;
        private String version;
        private Boolean enabled;
        private List<String> requirements;
        private Map<String, String> controls;
        private Double complianceScore;
        private LocalDateTime lastAssessmentAt;
        private LocalDateTime certifiedAt;
        private LocalDateTime expiresAt;
        private Map<String, Object> metadata;
    }

    /**
     * Data Asset
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataAsset {
        private String assetId;
        private String assetName;
        private String assetType;
        private String description;
        private DataClassification classification;
        private String dataOwner;
        private String dataSteward;
        private List<String> tags;
        private List<String> categories;
        private Boolean containsPII;
        private Boolean containsPHI;
        private String storageLocation;
        private String format;
        private Long sizeBytes;
        private Integer recordCount;
        private LocalDateTime createdAt;
        private LocalDateTime lastModifiedAt;
        private LocalDateTime lastAccessedAt;
        private Map<String, String> sensitiveFields;
        private Map<String, Object> metadata;
    }

    /**
     * Governance Policy
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GovernancePolicy {
        private String policyId;
        private String policyName;
        private PolicyType policyType;
        private String description;
        private Boolean enabled;
        private Integer priority;
        private Map<String, Object> rules;
        private List<String> applicableAssets;
        private List<String> exemptions;
        private String enforcementLevel;
        private Long timesEnforced;
        private Long violations;
        private LocalDateTime effectiveDate;
        private LocalDateTime expirationDate;
        private String approvedBy;
        private Map<String, Object> metadata;
    }

    /**
     * Audit Log
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuditLog {
        private String auditId;
        private LocalDateTime timestamp;
        private String userId;
        private String userName;
        private String assetId;
        private String assetName;
        private AuditActionType action;
        private String description;
        private String ipAddress;
        private String userAgent;
        private String location;
        private Boolean successful;
        private String failureReason;
        private Map<String, Object> beforeState;
        private Map<String, Object> afterState;
        private List<String> changedFields;
        private Map<String, Object> metadata;
    }

    /**
     * Data Access Request
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataAccessRequest {
        private String requestId;
        private String requesterId;
        private String requesterName;
        private String assetId;
        private String assetName;
        private AccessRequestStatus status;
        private String purpose;
        private String justification;
        private List<String> requestedPermissions;
        private LocalDateTime requestedAt;
        private LocalDateTime reviewedAt;
        private String reviewedBy;
        private String reviewComments;
        private LocalDateTime grantedAt;
        private LocalDateTime expiresAt;
        private Map<String, Object> metadata;
    }

    /**
     * Retention Rule
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetentionRule {
        private String ruleId;
        private String ruleName;
        private String description;
        private List<String> applicableAssets;
        private Integer retentionDays;
        private String retentionPeriod;
        private Boolean autoDelete;
        private String archiveLocation;
        private Boolean enabled;
        private Integer priority;
        private LocalDateTime createdAt;
        private Map<String, Object> metadata;
    }

    /**
     * Privacy Assessment
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrivacyAssessment {
        private String assessmentId;
        private String assessmentName;
        private String assetId;
        private String assetName;
        private String assessmentType;
        private String status;
        private List<String> identifiedRisks;
        private List<String> mitigations;
        private String riskLevel;
        private Double riskScore;
        private String assessor;
        private LocalDateTime conductedAt;
        private LocalDateTime reviewedAt;
        private String reviewedBy;
        private LocalDateTime nextReviewDate;
        private Map<String, Object> findings;
        private Map<String, Object> metadata;
    }

    /**
     * Consent Record
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsentRecord {
        private String consentId;
        private String userId;
        private String userName;
        private String consentType;
        private String purpose;
        private Boolean consentGiven;
        private LocalDateTime consentedAt;
        private LocalDateTime expiresAt;
        private LocalDateTime revokedAt;
        private String revokeReason;
        private String consentText;
        private String version;
        private Boolean active;
        private Map<String, Object> metadata;
    }

    /**
     * Data Lineage
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataLineage {
        private String lineageId;
        private String assetId;
        private String assetName;
        private List<String> sourceAssets;
        private List<String> derivedAssets;
        private List<String> transformations;
        private String createdBy;
        private LocalDateTime createdAt;
        private Map<String, String> processingSteps;
        private Map<String, Object> metadata;
    }

    /**
     * Compliance Violation
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceViolation {
        private String violationId;
        private String policyId;
        private String policyName;
        private String assetId;
        private String assetName;
        private ViolationSeverity severity;
        private String violationType;
        private String description;
        private String detectedBy;
        private LocalDateTime detectedAt;
        private String assignedTo;
        private String status;
        private String resolution;
        private LocalDateTime resolvedAt;
        private String resolvedBy;
        private Map<String, Object> metadata;
    }

    /**
     * Governance Metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GovernanceMetrics {
        private Integer totalStandards;
        private Integer activeStandards;
        private Double averageComplianceScore;
        private Long totalAssets;
        private Long classifiedAssets;
        private Double classificationRate;
        private Long assetsWithPII;
        private Integer totalPolicies;
        private Integer activePolicies;
        private Long totalAudits;
        private Long totalAccessRequests;
        private Long approvedRequests;
        private Double approvalRate;
        private Long totalConsents;
        private Long activeConsents;
        private Long totalViolations;
        private Long resolvedViolations;
        private Long openViolations;
        private Double violationResolutionRate;
        private Long totalAssessments;
        private LocalDateTime measuredAt;
    }

    /**
     * Governance Event
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GovernanceEvent {
        private String eventId;
        private LocalDateTime timestamp;
        private String eventType;
        private String description;
        private String resourceType;
        private String resourceId;
        private Map<String, Object> details;
    }

    // Helper Methods

    /**
     * Activate framework
     */
    public void activateFramework() {
        this.status = FrameworkStatus.CONFIGURING;
        this.isActive = true;
        this.activatedAt = LocalDateTime.now();

        recordEvent("FRAMEWORK_ACTIVATED", "Governance framework activated", "FRAMEWORK",
                frameworkId != null ? frameworkId.toString() : null);

        this.status = FrameworkStatus.ACTIVE;
    }

    /**
     * Add compliance standard
     */
    public void addStandard(ComplianceStandard standard) {
        if (standards == null) {
            standards = new java.util.ArrayList<>();
        }
        standards.add(standard);

        if (standardRegistry == null) {
            standardRegistry = new java.util.HashMap<>();
        }
        standardRegistry.put(standard.getStandardId(), standard);

        totalStandards = (totalStandards != null ? totalStandards : 0) + 1;
        if (Boolean.TRUE.equals(standard.getEnabled())) {
            activeStandards = (activeStandards != null ? activeStandards : 0) + 1;
        }

        recordEvent("STANDARD_ADDED", "Compliance standard added: " + standard.getStandardName(),
                "STANDARD", standard.getStandardId());
    }

    /**
     * Register data asset
     */
    public void registerAsset(DataAsset asset) {
        if (dataAssets == null) {
            dataAssets = new java.util.ArrayList<>();
        }
        dataAssets.add(asset);

        if (assetRegistry == null) {
            assetRegistry = new java.util.HashMap<>();
        }
        assetRegistry.put(asset.getAssetId(), asset);

        totalAssets = (totalAssets != null ? totalAssets : 0L) + 1;
        if (asset.getClassification() != null) {
            classifiedAssets = (classifiedAssets != null ? classifiedAssets : 0L) + 1;
        }

        recordEvent("ASSET_REGISTERED", "Data asset registered: " + asset.getAssetName(),
                "ASSET", asset.getAssetId());
    }

    /**
     * Add policy
     */
    public void addPolicy(GovernancePolicy policy) {
        if (policies == null) {
            policies = new java.util.ArrayList<>();
        }
        policies.add(policy);

        if (policyRegistry == null) {
            policyRegistry = new java.util.HashMap<>();
        }
        policyRegistry.put(policy.getPolicyId(), policy);

        totalPolicies = (totalPolicies != null ? totalPolicies : 0) + 1;
        if (Boolean.TRUE.equals(policy.getEnabled())) {
            activePolicies = (activePolicies != null ? activePolicies : 0) + 1;
        }

        recordEvent("POLICY_ADDED", "Governance policy added: " + policy.getPolicyName(),
                "POLICY", policy.getPolicyId());
    }

    /**
     * Log audit event
     */
    public void logAudit(AuditLog audit) {
        if (auditLogs == null) {
            auditLogs = new java.util.ArrayList<>();
        }
        auditLogs.add(audit);

        if (auditRegistry == null) {
            auditRegistry = new java.util.HashMap<>();
        }
        auditRegistry.put(audit.getAuditId(), audit);

        totalAudits = (totalAudits != null ? totalAudits : 0L) + 1;
        lastAuditAt = LocalDateTime.now();
    }

    /**
     * Submit access request
     */
    public void submitAccessRequest(DataAccessRequest request) {
        if (accessRequests == null) {
            accessRequests = new java.util.ArrayList<>();
        }
        accessRequests.add(request);

        if (requestRegistry == null) {
            requestRegistry = new java.util.HashMap<>();
        }
        requestRegistry.put(request.getRequestId(), request);

        totalRequests = (totalRequests != null ? totalRequests : 0L) + 1;

        recordEvent("ACCESS_REQUESTED", "Data access requested: " + request.getAssetName(),
                "ACCESS_REQUEST", request.getRequestId());
    }

    /**
     * Approve access request
     */
    public void approveRequest(String requestId, String reviewedBy, String comments) {
        DataAccessRequest request = requestRegistry != null ? requestRegistry.get(requestId) : null;
        if (request != null) {
            request.setStatus(AccessRequestStatus.APPROVED);
            request.setReviewedAt(LocalDateTime.now());
            request.setReviewedBy(reviewedBy);
            request.setReviewComments(comments);
            request.setGrantedAt(LocalDateTime.now());

            approvedRequests = (approvedRequests != null ? approvedRequests : 0L) + 1;
        }
    }

    /**
     * Deny access request
     */
    public void denyRequest(String requestId, String reviewedBy, String comments) {
        DataAccessRequest request = requestRegistry != null ? requestRegistry.get(requestId) : null;
        if (request != null) {
            request.setStatus(AccessRequestStatus.DENIED);
            request.setReviewedAt(LocalDateTime.now());
            request.setReviewedBy(reviewedBy);
            request.setReviewComments(comments);

            deniedRequests = (deniedRequests != null ? deniedRequests : 0L) + 1;
        }
    }

    /**
     * Add retention rule
     */
    public void addRetentionRule(RetentionRule rule) {
        if (retentionRules == null) {
            retentionRules = new java.util.ArrayList<>();
        }
        retentionRules.add(rule);

        if (retentionRegistry == null) {
            retentionRegistry = new java.util.HashMap<>();
        }
        retentionRegistry.put(rule.getRuleId(), rule);
    }

    /**
     * Conduct privacy assessment
     */
    public void conductAssessment(PrivacyAssessment assessment) {
        if (assessments == null) {
            assessments = new java.util.ArrayList<>();
        }
        assessments.add(assessment);

        if (assessmentRegistry == null) {
            assessmentRegistry = new java.util.HashMap<>();
        }
        assessmentRegistry.put(assessment.getAssessmentId(), assessment);

        totalAssessments = (totalAssessments != null ? totalAssessments : 0L) + 1;

        recordEvent("ASSESSMENT_CONDUCTED", "Privacy assessment conducted: " + assessment.getAssessmentName(),
                "ASSESSMENT", assessment.getAssessmentId());
    }

    /**
     * Record consent
     */
    public void recordConsent(ConsentRecord consent) {
        if (consents == null) {
            consents = new java.util.ArrayList<>();
        }
        consents.add(consent);

        if (consentRegistry == null) {
            consentRegistry = new java.util.HashMap<>();
        }
        consentRegistry.put(consent.getConsentId(), consent);

        totalConsents = (totalConsents != null ? totalConsents : 0L) + 1;
        if (Boolean.TRUE.equals(consent.getActive())) {
            activeConsents = (activeConsents != null ? activeConsents : 0L) + 1;
        }
    }

    /**
     * Track data lineage
     */
    public void trackLineage(DataLineage lineage) {
        if (lineageRecords == null) {
            lineageRecords = new java.util.ArrayList<>();
        }
        lineageRecords.add(lineage);

        if (lineageRegistry == null) {
            lineageRegistry = new java.util.HashMap<>();
        }
        lineageRegistry.put(lineage.getLineageId(), lineage);
    }

    /**
     * Report violation
     */
    public void reportViolation(ComplianceViolation violation) {
        if (violations == null) {
            violations = new java.util.ArrayList<>();
        }
        violations.add(violation);

        if (violationRegistry == null) {
            violationRegistry = new java.util.HashMap<>();
        }
        violationRegistry.put(violation.getViolationId(), violation);

        totalViolations = (totalViolations != null ? totalViolations : 0L) + 1;

        recordEvent("VIOLATION_DETECTED", "Compliance violation detected: " + violation.getViolationType(),
                "VIOLATION", violation.getViolationId());
    }

    /**
     * Resolve violation
     */
    public void resolveViolation(String violationId, String resolution, String resolvedBy) {
        ComplianceViolation violation = violationRegistry != null ? violationRegistry.get(violationId) : null;
        if (violation != null) {
            violation.setStatus("RESOLVED");
            violation.setResolution(resolution);
            violation.setResolvedAt(LocalDateTime.now());
            violation.setResolvedBy(resolvedBy);

            resolvedViolations = (resolvedViolations != null ? resolvedViolations : 0L) + 1;
        }
    }

    /**
     * Get asset by ID
     */
    public DataAsset getAsset(String assetId) {
        return assetRegistry != null ? assetRegistry.get(assetId) : null;
    }

    /**
     * Get policy by ID
     */
    public GovernancePolicy getPolicy(String policyId) {
        return policyRegistry != null ? policyRegistry.get(policyId) : null;
    }

    /**
     * Record event
     */
    public void recordEvent(String eventType, String description, String resourceType, String resourceId) {
        if (events == null) {
            events = new java.util.ArrayList<>();
        }

        GovernanceEvent event = GovernanceEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .eventType(eventType)
                .description(description)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .details(new java.util.HashMap<>())
                .build();

        events.add(event);
        lastEventAt = LocalDateTime.now();
    }

    /**
     * Check if framework is healthy
     */
    public boolean isHealthy() {
        return status == FrameworkStatus.ACTIVE || status == FrameworkStatus.AUDITING;
    }

    /**
     * Get active policies
     */
    public List<GovernancePolicy> getActivePolicies() {
        if (policies == null) {
            return new java.util.ArrayList<>();
        }
        return policies.stream()
                .filter(p -> Boolean.TRUE.equals(p.getEnabled()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get open violations
     */
    public List<ComplianceViolation> getOpenViolations() {
        if (violations == null) {
            return new java.util.ArrayList<>();
        }
        return violations.stream()
                .filter(v -> !"RESOLVED".equals(v.getStatus()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get assets with PII
     */
    public List<DataAsset> getAssetsWithPII() {
        if (dataAssets == null) {
            return new java.util.ArrayList<>();
        }
        return dataAssets.stream()
                .filter(a -> Boolean.TRUE.equals(a.getContainsPII()))
                .collect(java.util.stream.Collectors.toList());
    }
}
