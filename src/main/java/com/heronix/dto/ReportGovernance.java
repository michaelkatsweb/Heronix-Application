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
 * Report Governance DTO
 *
 * Represents report governance, lifecycle management, and change control.
 *
 * Features:
 * - Report lifecycle management
 * - Governance policies and rules
 * - Approval workflows
 * - Change management
 * - Version control
 * - Release management
 * - Deprecation handling
 * - Quality assurance
 *
 * Lifecycle Stages:
 * - DRAFT - Report in draft state
 * - REVIEW - Under review
 * - APPROVED - Approved for use
 * - PUBLISHED - Published and active
 * - DEPRECATED - Marked for deprecation
 * - ARCHIVED - Archived
 * - RETIRED - Retired and unavailable
 *
 * Governance Levels:
 * - NONE - No governance
 * - BASIC - Basic governance rules
 * - STANDARD - Standard governance
 * - STRICT - Strict governance
 * - ENTERPRISE - Enterprise-grade governance
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 88 - Report Governance & Lifecycle
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportGovernance {

    private Long governanceId;
    private Long reportId;
    private String reportName;
    private String reportType;

    // Governance configuration
    private GovernanceLevel governanceLevel;
    private Boolean governanceEnabled;
    private Boolean approvalRequired;
    private Boolean versionControlEnabled;
    private Boolean changeTrackingEnabled;
    private Boolean qualityChecksEnabled;

    // Lifecycle management
    private LifecycleStage currentStage;
    private LifecycleStage previousStage;
    private LocalDateTime stageChangedAt;
    private String stageChangedBy;
    private List<LifecycleTransition> lifecycleHistory;
    private Map<LifecycleStage, LocalDateTime> stageDurations;

    // Approval workflow
    private List<ApprovalStep> approvalWorkflow;
    private ApprovalStep currentApprovalStep;
    private Integer totalApprovalSteps;
    private Integer completedApprovalSteps;
    private ApprovalStatus approvalStatus;
    private LocalDateTime approvalRequestedAt;
    private LocalDateTime approvalCompletedAt;

    // Version control
    private String currentVersion;
    private Integer majorVersion;
    private Integer minorVersion;
    private Integer patchVersion;
    private List<Version> versions;
    private Boolean autoVersioning;
    private VersioningStrategy versioningStrategy;

    // Change management
    private List<ChangeRequest> changeRequests;
    private Integer totalChanges;
    private Integer pendingChanges;
    private Integer approvedChanges;
    private Integer rejectedChanges;
    private Boolean changeFreezePeriod;
    private LocalDateTime changeFreezeUntil;

    // Quality assurance
    private List<QualityCheck> qualityChecks;
    private QualityScore overallQualityScore;
    private Boolean passedAllChecks;
    private Integer failedChecks;
    private LocalDateTime lastQualityCheckAt;

    // Governance policies
    private List<GovernancePolicy> policies;
    private Integer totalPolicies;
    private Integer activePolicies;
    private Map<String, Object> policyConfiguration;

    // Release management
    private List<Release> releases;
    private Release currentRelease;
    private ReleaseStatus releaseStatus;
    private LocalDateTime nextReleaseDate;
    private Boolean scheduledRelease;

    // Deprecation
    private Boolean deprecated;
    private LocalDateTime deprecatedAt;
    private String deprecatedBy;
    private String deprecationReason;
    private String replacementReportId;
    private LocalDateTime retirementDate;

    // Metadata
    private String owner;
    private List<String> stakeholders;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime lastModifiedAt;
    private String lastModifiedBy;

    /**
     * Lifecycle Stage
     */
    public enum LifecycleStage {
        DRAFT,       // Report in draft state
        REVIEW,      // Under review
        APPROVED,    // Approved for use
        PUBLISHED,   // Published and active
        DEPRECATED,  // Marked for deprecation
        ARCHIVED,    // Archived
        RETIRED      // Retired and unavailable
    }

    /**
     * Governance Level
     */
    public enum GovernanceLevel {
        NONE,        // No governance
        BASIC,       // Basic governance rules
        STANDARD,    // Standard governance
        STRICT,      // Strict governance
        ENTERPRISE   // Enterprise-grade governance
    }

    /**
     * Approval Status
     */
    public enum ApprovalStatus {
        PENDING,     // Pending approval
        IN_PROGRESS, // Approval in progress
        APPROVED,    // Approved
        REJECTED,    // Rejected
        CANCELLED    // Cancelled
    }

    /**
     * Versioning Strategy
     */
    public enum VersioningStrategy {
        MANUAL,      // Manual versioning
        SEMANTIC,    // Semantic versioning (major.minor.patch)
        TIMESTAMP,   // Timestamp-based versioning
        SEQUENTIAL,  // Sequential versioning
        CUSTOM       // Custom versioning scheme
    }

    /**
     * Release Status
     */
    public enum ReleaseStatus {
        PLANNING,    // Release planning
        SCHEDULED,   // Scheduled for release
        IN_PROGRESS, // Release in progress
        RELEASED,    // Released
        ROLLED_BACK, // Rolled back
        CANCELLED    // Cancelled
    }

    /**
     * Change Type
     */
    public enum ChangeType {
        MAJOR,       // Major change
        MINOR,       // Minor change
        PATCH,       // Patch/bug fix
        HOTFIX,      // Urgent hotfix
        ENHANCEMENT, // Enhancement
        REFACTOR     // Refactoring
    }

    /**
     * Lifecycle Transition
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LifecycleTransition {
        private String transitionId;
        private LifecycleStage fromStage;
        private LifecycleStage toStage;
        private LocalDateTime transitionedAt;
        private String transitionedBy;
        private String reason;
        private Map<String, Object> metadata;
    }

    /**
     * Approval Step
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalStep {
        private String stepId;
        private String stepName;
        private Integer stepOrder;
        private String approverId;
        private String approverName;
        private String approverRole;
        private ApprovalStatus status;
        private LocalDateTime requestedAt;
        private LocalDateTime respondedAt;
        private String decision;
        private String comments;
        private Boolean required;
        private Integer timeoutDays;
    }

    /**
     * Version
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Version {
        private String versionId;
        private String versionNumber;
        private Integer majorVersion;
        private Integer minorVersion;
        private Integer patchVersion;
        private ChangeType changeType;
        private String description;
        private LocalDateTime createdAt;
        private String createdBy;
        private Boolean current;
        private Boolean stable;
        private Long sizeBytes;
        private String checksum;
    }

    /**
     * Change Request
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangeRequest {
        private String changeId;
        private String title;
        private String description;
        private ChangeType changeType;
        private String requestedBy;
        private LocalDateTime requestedAt;
        private ApprovalStatus status;
        private String approvedBy;
        private LocalDateTime approvedAt;
        private String rejectedBy;
        private LocalDateTime rejectedAt;
        private String rejectionReason;
        private LocalDateTime scheduledFor;
        private LocalDateTime implementedAt;
        private Boolean impactsProduction;
        private List<String> affectedReports;
    }

    /**
     * Quality Check
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QualityCheck {
        private String checkId;
        private String checkName;
        private String checkType;
        private Boolean passed;
        private Double score;
        private String severity;
        private String description;
        private LocalDateTime executedAt;
        private String executedBy;
        private List<String> issues;
        private Map<String, Object> results;
    }

    /**
     * Quality Score
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QualityScore {
        private Double overallScore;
        private Double accuracyScore;
        private Double performanceScore;
        private Double reliabilityScore;
        private Double maintainabilityScore;
        private Double securityScore;
        private String grade;
        private Boolean meetsThreshold;
        private Double threshold;
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
        private String description;
        private String policyType;
        private Boolean enabled;
        private Boolean mandatory;
        private Integer priority;
        private String condition;
        private String action;
        private LocalDateTime effectiveFrom;
        private LocalDateTime effectiveUntil;
        private String createdBy;
        private LocalDateTime createdAt;
    }

    /**
     * Release
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Release {
        private String releaseId;
        private String releaseName;
        private String versionNumber;
        private ReleaseStatus status;
        private LocalDateTime scheduledAt;
        private LocalDateTime releasedAt;
        private String releasedBy;
        private List<String> includedChanges;
        private String releaseNotes;
        private Boolean rollbackAvailable;
        private String rollbackVersion;
        private Map<String, Object> releaseMetrics;
    }

    /**
     * Helper Methods
     */

    public void transitionStage(LifecycleStage toStage, String transitionedBy, String reason) {
        LifecycleTransition transition = LifecycleTransition.builder()
                .transitionId(java.util.UUID.randomUUID().toString())
                .fromStage(currentStage)
                .toStage(toStage)
                .transitionedAt(LocalDateTime.now())
                .transitionedBy(transitionedBy)
                .reason(reason)
                .metadata(new HashMap<>())
                .build();

        if (lifecycleHistory == null) {
            lifecycleHistory = new ArrayList<>();
        }
        lifecycleHistory.add(transition);

        previousStage = currentStage;
        currentStage = toStage;
        stageChangedAt = LocalDateTime.now();
        stageChangedBy = transitionedBy;

        // Track stage duration
        if (stageDurations == null) {
            stageDurations = new HashMap<>();
        }
        stageDurations.put(toStage, LocalDateTime.now());
    }

    public void addApprovalStep(ApprovalStep step) {
        if (approvalWorkflow == null) {
            approvalWorkflow = new ArrayList<>();
        }
        approvalWorkflow.add(step);
        totalApprovalSteps = (totalApprovalSteps != null ? totalApprovalSteps : 0) + 1;
    }

    public void approveStep(String stepId, String approvedBy, String comments) {
        if (approvalWorkflow != null) {
            for (ApprovalStep step : approvalWorkflow) {
                if (step.getStepId().equals(stepId)) {
                    step.setStatus(ApprovalStatus.APPROVED);
                    step.setRespondedAt(LocalDateTime.now());
                    step.setDecision("APPROVED");
                    step.setComments(comments);
                    completedApprovalSteps = (completedApprovalSteps != null ? completedApprovalSteps : 0) + 1;
                    break;
                }
            }
        }
        updateApprovalStatus();
    }

    public void rejectStep(String stepId, String rejectedBy, String comments) {
        if (approvalWorkflow != null) {
            for (ApprovalStep step : approvalWorkflow) {
                if (step.getStepId().equals(stepId)) {
                    step.setStatus(ApprovalStatus.REJECTED);
                    step.setRespondedAt(LocalDateTime.now());
                    step.setDecision("REJECTED");
                    step.setComments(comments);
                    approvalStatus = ApprovalStatus.REJECTED;
                    break;
                }
            }
        }
    }

    private void updateApprovalStatus() {
        if (approvalWorkflow == null || approvalWorkflow.isEmpty()) {
            approvalStatus = ApprovalStatus.APPROVED;
            return;
        }

        boolean allApproved = approvalWorkflow.stream()
                .filter(step -> Boolean.TRUE.equals(step.getRequired()))
                .allMatch(step -> step.getStatus() == ApprovalStatus.APPROVED);

        if (allApproved) {
            approvalStatus = ApprovalStatus.APPROVED;
            approvalCompletedAt = LocalDateTime.now();
        } else {
            approvalStatus = ApprovalStatus.IN_PROGRESS;
        }
    }

    public void addVersion(Version version) {
        if (versions == null) {
            versions = new ArrayList<>();
        }

        // Mark current version as not current
        if (versions != null) {
            versions.forEach(v -> v.setCurrent(false));
        }

        version.setCurrent(true);
        versions.add(version);

        currentVersion = version.getVersionNumber();
        majorVersion = version.getMajorVersion();
        minorVersion = version.getMinorVersion();
        patchVersion = version.getPatchVersion();
    }

    public void addChangeRequest(ChangeRequest changeRequest) {
        if (changeRequests == null) {
            changeRequests = new ArrayList<>();
        }
        changeRequests.add(changeRequest);
        totalChanges = (totalChanges != null ? totalChanges : 0) + 1;

        if (changeRequest.getStatus() == ApprovalStatus.PENDING) {
            pendingChanges = (pendingChanges != null ? pendingChanges : 0) + 1;
        }
    }

    public void approveChange(String changeId, String approvedBy) {
        if (changeRequests != null) {
            for (ChangeRequest change : changeRequests) {
                if (change.getChangeId().equals(changeId)) {
                    change.setStatus(ApprovalStatus.APPROVED);
                    change.setApprovedBy(approvedBy);
                    change.setApprovedAt(LocalDateTime.now());
                    approvedChanges = (approvedChanges != null ? approvedChanges : 0) + 1;
                    if (pendingChanges != null && pendingChanges > 0) {
                        pendingChanges--;
                    }
                    break;
                }
            }
        }
    }

    public void rejectChange(String changeId, String rejectedBy, String reason) {
        if (changeRequests != null) {
            for (ChangeRequest change : changeRequests) {
                if (change.getChangeId().equals(changeId)) {
                    change.setStatus(ApprovalStatus.REJECTED);
                    change.setRejectedBy(rejectedBy);
                    change.setRejectedAt(LocalDateTime.now());
                    change.setRejectionReason(reason);
                    rejectedChanges = (rejectedChanges != null ? rejectedChanges : 0) + 1;
                    if (pendingChanges != null && pendingChanges > 0) {
                        pendingChanges--;
                    }
                    break;
                }
            }
        }
    }

    public void addQualityCheck(QualityCheck check) {
        if (qualityChecks == null) {
            qualityChecks = new ArrayList<>();
        }
        qualityChecks.add(check);

        if (!Boolean.TRUE.equals(check.getPassed())) {
            failedChecks = (failedChecks != null ? failedChecks : 0) + 1;
        }

        lastQualityCheckAt = LocalDateTime.now();
        updateQualityStatus();
    }

    private void updateQualityStatus() {
        if (qualityChecks == null || qualityChecks.isEmpty()) {
            passedAllChecks = true;
            return;
        }

        passedAllChecks = qualityChecks.stream()
                .allMatch(check -> Boolean.TRUE.equals(check.getPassed()));
    }

    public void addPolicy(GovernancePolicy policy) {
        if (policies == null) {
            policies = new ArrayList<>();
        }
        policies.add(policy);
        totalPolicies = (totalPolicies != null ? totalPolicies : 0) + 1;

        if (Boolean.TRUE.equals(policy.getEnabled())) {
            activePolicies = (activePolicies != null ? activePolicies : 0) + 1;
        }
    }

    public void deprecate(String deprecatedBy, String reason, String replacementReportId, LocalDateTime retirementDate) {
        this.deprecated = true;
        this.deprecatedAt = LocalDateTime.now();
        this.deprecatedBy = deprecatedBy;
        this.deprecationReason = reason;
        this.replacementReportId = replacementReportId;
        this.retirementDate = retirementDate;

        transitionStage(LifecycleStage.DEPRECATED, deprecatedBy, reason);
    }

    public void addRelease(Release release) {
        if (releases == null) {
            releases = new ArrayList<>();
        }
        releases.add(release);

        if (release.getStatus() == ReleaseStatus.RELEASED) {
            currentRelease = release;
        }
    }

    public boolean isApprovalComplete() {
        return approvalStatus == ApprovalStatus.APPROVED;
    }

    public boolean canTransitionTo(LifecycleStage targetStage) {
        if (currentStage == null) {
            return targetStage == LifecycleStage.DRAFT;
        }

        // Define allowed transitions
        return switch (currentStage) {
            case DRAFT -> targetStage == LifecycleStage.REVIEW;
            case REVIEW -> targetStage == LifecycleStage.APPROVED || targetStage == LifecycleStage.DRAFT;
            case APPROVED -> targetStage == LifecycleStage.PUBLISHED || targetStage == LifecycleStage.REVIEW;
            case PUBLISHED -> targetStage == LifecycleStage.DEPRECATED || targetStage == LifecycleStage.ARCHIVED;
            case DEPRECATED -> targetStage == LifecycleStage.ARCHIVED || targetStage == LifecycleStage.RETIRED;
            case ARCHIVED -> targetStage == LifecycleStage.RETIRED || targetStage == LifecycleStage.PUBLISHED;
            case RETIRED -> false; // No transitions from retired
        };
    }

    public boolean isInChangeFreeze() {
        return Boolean.TRUE.equals(changeFreezePeriod) &&
                changeFreezeUntil != null &&
                LocalDateTime.now().isBefore(changeFreezeUntil);
    }
}
