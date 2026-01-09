package com.heronix.service;

import com.heronix.dto.ReportGovernance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Report Governance Service
 *
 * Manages report governance, lifecycle, and change control.
 *
 * Features:
 * - Lifecycle stage transitions
 * - Approval workflow management
 * - Version control
 * - Change request handling
 * - Quality assurance
 * - Policy enforcement
 * - Release management
 * - Deprecation handling
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 88 - Report Governance & Lifecycle
 */
@Service
@Slf4j
public class ReportGovernanceService {

    private final Map<Long, ReportGovernance> governances = new ConcurrentHashMap<>();
    private Long nextGovernanceId = 1L;

    /**
     * Create governance
     */
    public ReportGovernance createGovernance(ReportGovernance governance) {
        synchronized (this) {
            governance.setGovernanceId(nextGovernanceId++);
            governance.setCreatedAt(LocalDateTime.now());
            governance.setCurrentStage(ReportGovernance.LifecycleStage.DRAFT);
            governance.setStageChangedAt(LocalDateTime.now());
            governance.setTotalApprovalSteps(0);
            governance.setCompletedApprovalSteps(0);
            governance.setTotalChanges(0);
            governance.setPendingChanges(0);
            governance.setApprovedChanges(0);
            governance.setRejectedChanges(0);
            governance.setFailedChecks(0);
            governance.setTotalPolicies(0);
            governance.setActivePolicies(0);

            // Set defaults
            if (governance.getGovernanceEnabled() == null) {
                governance.setGovernanceEnabled(true);
            }

            if (governance.getGovernanceLevel() == null) {
                governance.setGovernanceLevel(ReportGovernance.GovernanceLevel.STANDARD);
            }

            if (governance.getApprovalRequired() == null) {
                governance.setApprovalRequired(true);
            }

            if (governance.getVersionControlEnabled() == null) {
                governance.setVersionControlEnabled(true);
            }

            if (governance.getChangeTrackingEnabled() == null) {
                governance.setChangeTrackingEnabled(true);
            }

            if (governance.getQualityChecksEnabled() == null) {
                governance.setQualityChecksEnabled(true);
            }

            if (governance.getApprovalStatus() == null) {
                governance.setApprovalStatus(ReportGovernance.ApprovalStatus.PENDING);
            }

            if (governance.getAutoVersioning() == null) {
                governance.setAutoVersioning(true);
            }

            if (governance.getVersioningStrategy() == null) {
                governance.setVersioningStrategy(ReportGovernance.VersioningStrategy.SEMANTIC);
            }

            if (governance.getChangeFreezePeriod() == null) {
                governance.setChangeFreezePeriod(false);
            }

            if (governance.getPassedAllChecks() == null) {
                governance.setPassedAllChecks(true);
            }

            if (governance.getDeprecated() == null) {
                governance.setDeprecated(false);
            }

            if (governance.getScheduledRelease() == null) {
                governance.setScheduledRelease(false);
            }

            // Initialize collections
            if (governance.getLifecycleHistory() == null) {
                governance.setLifecycleHistory(new ArrayList<>());
            }

            if (governance.getApprovalWorkflow() == null) {
                governance.setApprovalWorkflow(new ArrayList<>());
            }

            if (governance.getVersions() == null) {
                governance.setVersions(new ArrayList<>());
            }

            if (governance.getChangeRequests() == null) {
                governance.setChangeRequests(new ArrayList<>());
            }

            if (governance.getQualityChecks() == null) {
                governance.setQualityChecks(new ArrayList<>());
            }

            if (governance.getPolicies() == null) {
                governance.setPolicies(new ArrayList<>());
            }

            if (governance.getReleases() == null) {
                governance.setReleases(new ArrayList<>());
            }

            if (governance.getStakeholders() == null) {
                governance.setStakeholders(new ArrayList<>());
            }

            if (governance.getStageDurations() == null) {
                governance.setStageDurations(new HashMap<>());
            }

            if (governance.getPolicyConfiguration() == null) {
                governance.setPolicyConfiguration(new HashMap<>());
            }

            // Create initial version
            if (Boolean.TRUE.equals(governance.getVersionControlEnabled())) {
                createInitialVersion(governance);
            }

            governances.put(governance.getGovernanceId(), governance);

            log.info("Created governance {} for report {} with level {}",
                    governance.getGovernanceId(), governance.getReportId(),
                    governance.getGovernanceLevel());

            return governance;
        }
    }

    /**
     * Create initial version
     */
    private void createInitialVersion(ReportGovernance governance) {
        ReportGovernance.Version version = ReportGovernance.Version.builder()
                .versionId(UUID.randomUUID().toString())
                .versionNumber("1.0.0")
                .majorVersion(1)
                .minorVersion(0)
                .patchVersion(0)
                .changeType(ReportGovernance.ChangeType.MAJOR)
                .description("Initial version")
                .createdAt(LocalDateTime.now())
                .createdBy(governance.getCreatedBy())
                .current(true)
                .stable(true)
                .build();

        governance.addVersion(version);
    }

    /**
     * Get governance
     */
    public Optional<ReportGovernance> getGovernance(Long governanceId) {
        return Optional.ofNullable(governances.get(governanceId));
    }

    /**
     * Get governance by report
     */
    public Optional<ReportGovernance> getGovernanceByReport(Long reportId) {
        return governances.values().stream()
                .filter(g -> reportId.equals(g.getReportId()))
                .findFirst();
    }

    /**
     * Transition lifecycle stage
     */
    public void transitionStage(Long governanceId, ReportGovernance.LifecycleStage toStage,
                                String transitionedBy, String reason) {
        ReportGovernance governance = governances.get(governanceId);
        if (governance == null) {
            throw new IllegalArgumentException("Governance not found: " + governanceId);
        }

        if (!governance.canTransitionTo(toStage)) {
            throw new IllegalStateException("Cannot transition from " + governance.getCurrentStage() +
                    " to " + toStage);
        }

        // Check approval requirements for certain transitions
        if (toStage == ReportGovernance.LifecycleStage.PUBLISHED &&
                Boolean.TRUE.equals(governance.getApprovalRequired()) &&
                !governance.isApprovalComplete()) {
            throw new IllegalStateException("Approval required before publishing");
        }

        // Check quality checks for publishing
        if (toStage == ReportGovernance.LifecycleStage.PUBLISHED &&
                Boolean.TRUE.equals(governance.getQualityChecksEnabled()) &&
                !Boolean.TRUE.equals(governance.getPassedAllChecks())) {
            throw new IllegalStateException("All quality checks must pass before publishing");
        }

        governance.transitionStage(toStage, transitionedBy, reason);

        log.info("Transitioned governance {} from {} to {} by {}",
                governanceId, governance.getPreviousStage(), toStage, transitionedBy);
    }

    /**
     * Add approval step
     */
    public void addApprovalStep(Long governanceId, String stepName, String approverId,
                                String approverRole, boolean required, Integer timeoutDays) {
        ReportGovernance governance = governances.get(governanceId);
        if (governance == null) {
            throw new IllegalArgumentException("Governance not found: " + governanceId);
        }

        Integer nextOrder = governance.getTotalApprovalSteps() != null ?
                governance.getTotalApprovalSteps() + 1 : 1;

        ReportGovernance.ApprovalStep step = ReportGovernance.ApprovalStep.builder()
                .stepId(UUID.randomUUID().toString())
                .stepName(stepName)
                .stepOrder(nextOrder)
                .approverId(approverId)
                .approverRole(approverRole)
                .status(ReportGovernance.ApprovalStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .required(required)
                .timeoutDays(timeoutDays)
                .build();

        governance.addApprovalStep(step);

        log.info("Added approval step {} to governance {}: {} (approver: {})",
                step.getStepId(), governanceId, stepName, approverId);
    }

    /**
     * Approve step
     */
    public void approveStep(Long governanceId, String stepId, String approvedBy, String comments) {
        ReportGovernance governance = governances.get(governanceId);
        if (governance == null) {
            throw new IllegalArgumentException("Governance not found: " + governanceId);
        }

        governance.approveStep(stepId, approvedBy, comments);

        log.info("Approved step {} in governance {} by {}", stepId, governanceId, approvedBy);

        // Check if all approvals are complete
        if (governance.isApprovalComplete()) {
            log.info("All approvals complete for governance {}", governanceId);
        }
    }

    /**
     * Reject step
     */
    public void rejectStep(Long governanceId, String stepId, String rejectedBy, String comments) {
        ReportGovernance governance = governances.get(governanceId);
        if (governance == null) {
            throw new IllegalArgumentException("Governance not found: " + governanceId);
        }

        governance.rejectStep(stepId, rejectedBy, comments);

        log.warn("Rejected step {} in governance {} by {}: {}",
                stepId, governanceId, rejectedBy, comments);
    }

    /**
     * Create version
     */
    public ReportGovernance.Version createVersion(Long governanceId, ReportGovernance.ChangeType changeType,
                                                  String description, String createdBy) {
        ReportGovernance governance = governances.get(governanceId);
        if (governance == null) {
            throw new IllegalArgumentException("Governance not found: " + governanceId);
        }

        if (!Boolean.TRUE.equals(governance.getVersionControlEnabled())) {
            throw new IllegalStateException("Version control is not enabled");
        }

        // Calculate new version number
        int newMajor = governance.getMajorVersion() != null ? governance.getMajorVersion() : 1;
        int newMinor = governance.getMinorVersion() != null ? governance.getMinorVersion() : 0;
        int newPatch = governance.getPatchVersion() != null ? governance.getPatchVersion() : 0;

        switch (changeType) {
            case MAJOR -> {
                newMajor++;
                newMinor = 0;
                newPatch = 0;
            }
            case MINOR, ENHANCEMENT -> {
                newMinor++;
                newPatch = 0;
            }
            case PATCH, HOTFIX -> newPatch++;
        }

        String versionNumber = String.format("%d.%d.%d", newMajor, newMinor, newPatch);

        ReportGovernance.Version version = ReportGovernance.Version.builder()
                .versionId(UUID.randomUUID().toString())
                .versionNumber(versionNumber)
                .majorVersion(newMajor)
                .minorVersion(newMinor)
                .patchVersion(newPatch)
                .changeType(changeType)
                .description(description)
                .createdAt(LocalDateTime.now())
                .createdBy(createdBy)
                .current(true)
                .stable(true)
                .build();

        governance.addVersion(version);

        log.info("Created version {} for governance {}: {} ({})",
                versionNumber, governanceId, changeType, description);

        return version;
    }

    /**
     * Submit change request
     */
    public ReportGovernance.ChangeRequest submitChangeRequest(Long governanceId, String title,
                                                               String description,
                                                               ReportGovernance.ChangeType changeType,
                                                               String requestedBy,
                                                               boolean impactsProduction) {
        ReportGovernance governance = governances.get(governanceId);
        if (governance == null) {
            throw new IllegalArgumentException("Governance not found: " + governanceId);
        }

        if (governance.isInChangeFreeze()) {
            throw new IllegalStateException("Changes are frozen until " + governance.getChangeFreezeUntil());
        }

        ReportGovernance.ChangeRequest changeRequest = ReportGovernance.ChangeRequest.builder()
                .changeId(UUID.randomUUID().toString())
                .title(title)
                .description(description)
                .changeType(changeType)
                .requestedBy(requestedBy)
                .requestedAt(LocalDateTime.now())
                .status(ReportGovernance.ApprovalStatus.PENDING)
                .impactsProduction(impactsProduction)
                .affectedReports(new ArrayList<>())
                .build();

        governance.addChangeRequest(changeRequest);

        log.info("Submitted change request {} for governance {}: {} ({})",
                changeRequest.getChangeId(), governanceId, title, changeType);

        return changeRequest;
    }

    /**
     * Approve change request
     */
    public void approveChangeRequest(Long governanceId, String changeId, String approvedBy) {
        ReportGovernance governance = governances.get(governanceId);
        if (governance == null) {
            throw new IllegalArgumentException("Governance not found: " + governanceId);
        }

        governance.approveChange(changeId, approvedBy);

        log.info("Approved change request {} in governance {} by {}",
                changeId, governanceId, approvedBy);
    }

    /**
     * Reject change request
     */
    public void rejectChangeRequest(Long governanceId, String changeId, String rejectedBy, String reason) {
        ReportGovernance governance = governances.get(governanceId);
        if (governance == null) {
            throw new IllegalArgumentException("Governance not found: " + governanceId);
        }

        governance.rejectChange(changeId, rejectedBy, reason);

        log.warn("Rejected change request {} in governance {} by {}: {}",
                changeId, governanceId, rejectedBy, reason);
    }

    /**
     * Run quality check
     */
    public void runQualityCheck(Long governanceId, String checkName, String checkType,
                               String executedBy) {
        ReportGovernance governance = governances.get(governanceId);
        if (governance == null) {
            throw new IllegalArgumentException("Governance not found: " + governanceId);
        }

        // Simulate quality check
        boolean passed = Math.random() > 0.1; // 90% pass rate
        double score = 0.7 + (Math.random() * 0.3); // 0.7-1.0

        ReportGovernance.QualityCheck check = ReportGovernance.QualityCheck.builder()
                .checkId(UUID.randomUUID().toString())
                .checkName(checkName)
                .checkType(checkType)
                .passed(passed)
                .score(score)
                .severity(passed ? "INFO" : "WARNING")
                .description("Quality check: " + checkName)
                .executedAt(LocalDateTime.now())
                .executedBy(executedBy)
                .issues(passed ? new ArrayList<>() : List.of("Minor issues detected"))
                .results(new HashMap<>())
                .build();

        governance.addQualityCheck(check);

        log.info("Ran quality check {} for governance {}: {} (score: {:.2f})",
                checkName, governanceId, passed ? "PASSED" : "FAILED", score);
    }

    /**
     * Calculate quality score
     */
    public void calculateQualityScore(Long governanceId) {
        ReportGovernance governance = governances.get(governanceId);
        if (governance == null) {
            throw new IllegalArgumentException("Governance not found: " + governanceId);
        }

        // Simplified quality score calculation
        double overallScore = governance.getQualityChecks() != null && !governance.getQualityChecks().isEmpty() ?
                governance.getQualityChecks().stream()
                        .mapToDouble(c -> c.getScore() != null ? c.getScore() : 0.0)
                        .average()
                        .orElse(0.0) : 0.0;

        String grade = overallScore >= 0.9 ? "A" :
                overallScore >= 0.8 ? "B" :
                        overallScore >= 0.7 ? "C" :
                                overallScore >= 0.6 ? "D" : "F";

        ReportGovernance.QualityScore qualityScore = ReportGovernance.QualityScore.builder()
                .overallScore(overallScore)
                .accuracyScore(overallScore * 0.95)
                .performanceScore(overallScore * 1.05)
                .reliabilityScore(overallScore)
                .maintainabilityScore(overallScore * 0.9)
                .securityScore(overallScore * 1.1)
                .grade(grade)
                .meetsThreshold(overallScore >= 0.7)
                .threshold(0.7)
                .build();

        governance.setOverallQualityScore(qualityScore);

        log.info("Calculated quality score for governance {}: {} (grade: {})",
                governanceId, overallScore, grade);
    }

    /**
     * Add policy
     */
    public void addPolicy(Long governanceId, ReportGovernance.GovernancePolicy policy) {
        ReportGovernance governance = governances.get(governanceId);
        if (governance == null) {
            throw new IllegalArgumentException("Governance not found: " + governanceId);
        }

        policy.setPolicyId(UUID.randomUUID().toString());
        policy.setCreatedAt(LocalDateTime.now());

        if (policy.getPriority() == null) {
            policy.setPriority(100);
        }

        governance.addPolicy(policy);

        log.info("Added policy {} to governance {}: {}",
                policy.getPolicyId(), governanceId, policy.getPolicyName());
    }

    /**
     * Create release
     */
    public ReportGovernance.Release createRelease(Long governanceId, String releaseName,
                                                  String versionNumber, LocalDateTime scheduledAt) {
        ReportGovernance governance = governances.get(governanceId);
        if (governance == null) {
            throw new IllegalArgumentException("Governance not found: " + governanceId);
        }

        ReportGovernance.Release release = ReportGovernance.Release.builder()
                .releaseId(UUID.randomUUID().toString())
                .releaseName(releaseName)
                .versionNumber(versionNumber)
                .status(ReportGovernance.ReleaseStatus.PLANNING)
                .scheduledAt(scheduledAt)
                .includedChanges(new ArrayList<>())
                .rollbackAvailable(true)
                .releaseMetrics(new HashMap<>())
                .build();

        governance.addRelease(release);
        governance.setNextReleaseDate(scheduledAt);

        log.info("Created release {} for governance {}: {} (version: {})",
                release.getReleaseId(), governanceId, releaseName, versionNumber);

        return release;
    }

    /**
     * Release report
     */
    public void releaseReport(Long governanceId, String releaseId, String releasedBy) {
        ReportGovernance governance = governances.get(governanceId);
        if (governance == null) {
            throw new IllegalArgumentException("Governance not found: " + governanceId);
        }

        if (governance.getReleases() != null) {
            for (ReportGovernance.Release release : governance.getReleases()) {
                if (release.getReleaseId().equals(releaseId)) {
                    release.setStatus(ReportGovernance.ReleaseStatus.RELEASED);
                    release.setReleasedAt(LocalDateTime.now());
                    release.setReleasedBy(releasedBy);
                    governance.setCurrentRelease(release);
                    governance.setReleaseStatus(ReportGovernance.ReleaseStatus.RELEASED);

                    log.info("Released report {} in governance {} by {}",
                            releaseId, governanceId, releasedBy);
                    break;
                }
            }
        }
    }

    /**
     * Deprecate report
     */
    public void deprecateReport(Long governanceId, String deprecatedBy, String reason,
                                String replacementReportId, LocalDateTime retirementDate) {
        ReportGovernance governance = governances.get(governanceId);
        if (governance == null) {
            throw new IllegalArgumentException("Governance not found: " + governanceId);
        }

        governance.deprecate(deprecatedBy, reason, replacementReportId, retirementDate);

        log.warn("Deprecated report in governance {} by {}: {} (retirement: {})",
                governanceId, deprecatedBy, reason, retirementDate);
    }

    /**
     * Delete governance
     */
    public void deleteGovernance(Long governanceId) {
        ReportGovernance removed = governances.remove(governanceId);
        if (removed != null) {
            log.info("Deleted governance {}", governanceId);
        }
    }

    /**
     * Get statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalGovernances", governances.size());

        long totalApprovals = governances.values().stream()
                .mapToLong(g -> g.getTotalApprovalSteps() != null ? g.getTotalApprovalSteps() : 0)
                .sum();

        long totalChanges = governances.values().stream()
                .mapToLong(g -> g.getTotalChanges() != null ? g.getTotalChanges() : 0)
                .sum();

        long deprecatedReports = governances.values().stream()
                .filter(g -> Boolean.TRUE.equals(g.getDeprecated()))
                .count();

        stats.put("totalApprovals", totalApprovals);
        stats.put("totalChanges", totalChanges);
        stats.put("deprecatedReports", deprecatedReports);

        // Count by lifecycle stage
        Map<ReportGovernance.LifecycleStage, Long> byStage = governances.values().stream()
                .collect(Collectors.groupingBy(ReportGovernance::getCurrentStage, Collectors.counting()));
        stats.put("reportsByStage", byStage);

        // Count by governance level
        Map<ReportGovernance.GovernanceLevel, Long> byLevel = governances.values().stream()
                .collect(Collectors.groupingBy(ReportGovernance::getGovernanceLevel, Collectors.counting()));
        stats.put("governancesByLevel", byLevel);

        return stats;
    }
}
