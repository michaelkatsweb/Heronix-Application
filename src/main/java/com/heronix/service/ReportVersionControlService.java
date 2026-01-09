package com.heronix.service;

import com.heronix.dto.ReportVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Report Version Control Service
 *
 * Manages report versioning and history.
 *
 * Features:
 * - Version creation and tracking
 * - Semantic versioning
 * - Change tracking
 * - Version comparison
 * - Rollback support
 * - Branch management
 * - Version approval workflow
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 72 - Report Version Control & History
 */
@Service
@Slf4j
public class ReportVersionControlService {

    // Version storage (in production, use database)
    private final Map<Long, ReportVersion> versions = new ConcurrentHashMap<>();
    private final Map<Long, List<ReportVersion>> reportVersions = new ConcurrentHashMap<>();
    private Long nextVersionId = 1L;

    // ============================================================
    // Version Management
    // ============================================================

    /**
     * Create new version
     */
    public ReportVersion createVersion(ReportVersion version) {
        synchronized (this) {
            version.validate();

            // Set version ID
            version.setVersionId(nextVersionId++);
            version.setCreatedAt(LocalDateTime.now());

            // Set version number if not provided
            if (version.getVersionNumber() == null) {
                version.setVersionNumber(version.getFullVersionString());
            }

            // Store version
            versions.put(version.getVersionId(), version);

            // Add to report versions list
            reportVersions.computeIfAbsent(version.getReportId(), k -> new ArrayList<>())
                    .add(version);

            // Update current/latest flags
            if (Boolean.TRUE.equals(version.getIsCurrent())) {
                setCurrentVersion(version.getReportId(), version.getVersionId());
            }

            log.info("Created version {} for report {} ({})",
                    version.getFullVersionString(), version.getReportId(), version.getCommitMessage());

            return version;
        }
    }

    /**
     * Create initial version for new report
     */
    public ReportVersion createInitialVersion(Long reportId, String createdBy, String commitMessage) {
        ReportVersion initial = ReportVersion.createInitialVersion(reportId, createdBy);
        initial.setCommitMessage(commitMessage != null ? commitMessage : "Initial version");
        return createVersion(initial);
    }

    /**
     * Create new version from existing
     */
    public ReportVersion createNewVersion(Long reportId, String createdBy, String commitMessage,
                                         ReportVersion.VersionType versionType) {
        // Get current version
        ReportVersion current = getCurrentVersion(reportId)
                .orElse(ReportVersion.createInitialVersion(reportId, createdBy));

        // Create new version
        ReportVersion newVersion = ReportVersion.builder()
                .reportId(reportId)
                .majorVersion(current.getMajorVersion())
                .minorVersion(current.getMinorVersion())
                .patchVersion(current.getPatchVersion())
                .createdBy(createdBy)
                .commitMessage(commitMessage)
                .versionType(versionType)
                .status(ReportVersion.VersionStatus.DRAFT)
                .previousVersionId(current.getVersionId())
                .branchName(current.getBranchName())
                .isMainBranch(current.getIsMainBranch())
                .build();

        // Increment version
        newVersion.incrementVersion(versionType);

        return createVersion(newVersion);
    }

    /**
     * Get version by ID
     */
    public Optional<ReportVersion> getVersion(Long versionId) {
        return Optional.ofNullable(versions.get(versionId));
    }

    /**
     * Get all versions for report
     */
    public List<ReportVersion> getVersionsForReport(Long reportId) {
        return reportVersions.getOrDefault(reportId, new ArrayList<>()).stream()
                .sorted(Comparator.comparing(ReportVersion::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Get current version of report
     */
    public Optional<ReportVersion> getCurrentVersion(Long reportId) {
        return reportVersions.getOrDefault(reportId, new ArrayList<>()).stream()
                .filter(ReportVersion::isCurrentVersion)
                .findFirst();
    }

    /**
     * Get latest version of report
     */
    public Optional<ReportVersion> getLatestVersion(Long reportId) {
        return reportVersions.getOrDefault(reportId, new ArrayList<>()).stream()
                .max(Comparator.comparing(ReportVersion::getCreatedAt));
    }

    /**
     * Set current version
     */
    public void setCurrentVersion(Long reportId, Long versionId) {
        List<ReportVersion> reportVers = reportVersions.get(reportId);
        if (reportVers == null) {
            return;
        }

        // Clear all current flags
        reportVers.forEach(v -> v.setIsCurrent(false));

        // Set new current
        ReportVersion newCurrent = versions.get(versionId);
        if (newCurrent != null) {
            newCurrent.setIsCurrent(true);
            log.info("Set current version of report {} to {}", reportId, newCurrent.getFullVersionString());
        }
    }

    /**
     * Update version
     */
    public ReportVersion updateVersion(ReportVersion version) {
        version.validate();
        versions.put(version.getVersionId(), version);
        log.info("Updated version {}", version.getVersionId());
        return version;
    }

    /**
     * Delete version
     */
    public void deleteVersion(Long versionId) {
        ReportVersion version = versions.remove(versionId);
        if (version != null) {
            List<ReportVersion> reportVers = reportVersions.get(version.getReportId());
            if (reportVers != null) {
                reportVers.remove(version);
            }
            log.info("Deleted version {}", versionId);
        }
    }

    // ============================================================
    // Version Operations
    // ============================================================

    /**
     * Publish version
     */
    public void publishVersion(Long versionId, String publishedBy) {
        ReportVersion version = versions.get(versionId);
        if (version == null) {
            throw new IllegalArgumentException("Version not found: " + versionId);
        }

        version.setStatus(ReportVersion.VersionStatus.PUBLISHED);
        version.setPublishedAt(LocalDateTime.now());
        version.setPublishedBy(publishedBy);
        version.setIsCurrent(true);

        // Set as current version
        setCurrentVersion(version.getReportId(), versionId);

        log.info("Published version {} of report {}", version.getFullVersionString(), version.getReportId());
    }

    /**
     * Approve version
     */
    public void approveVersion(Long versionId, String approvedBy, String comments) {
        ReportVersion version = versions.get(versionId);
        if (version == null) {
            throw new IllegalArgumentException("Version not found: " + versionId);
        }

        version.setStatus(ReportVersion.VersionStatus.APPROVED);
        version.setApprovedBy(approvedBy);
        version.setApprovedAt(LocalDateTime.now());
        version.setApprovalComments(comments);

        log.info("Approved version {} of report {}", version.getFullVersionString(), version.getReportId());
    }

    /**
     * Review version
     */
    public void reviewVersion(Long versionId, String reviewedBy, String comments) {
        ReportVersion version = versions.get(versionId);
        if (version == null) {
            throw new IllegalArgumentException("Version not found: " + versionId);
        }

        version.setStatus(ReportVersion.VersionStatus.REVIEW);
        version.setReviewedBy(reviewedBy);
        version.setReviewedAt(LocalDateTime.now());
        version.setReviewComments(comments);

        log.info("Version {} of report {} is under review", version.getFullVersionString(), version.getReportId());
    }

    /**
     * Deprecate version
     */
    public void deprecateVersion(Long versionId) {
        ReportVersion version = versions.get(versionId);
        if (version == null) {
            throw new IllegalArgumentException("Version not found: " + versionId);
        }

        version.setStatus(ReportVersion.VersionStatus.DEPRECATED);
        log.info("Deprecated version {} of report {}", version.getFullVersionString(), version.getReportId());
    }

    /**
     * Archive version
     */
    public void archiveVersion(Long versionId) {
        ReportVersion version = versions.get(versionId);
        if (version == null) {
            throw new IllegalArgumentException("Version not found: " + versionId);
        }

        version.setStatus(ReportVersion.VersionStatus.ARCHIVED);
        log.info("Archived version {} of report {}", version.getFullVersionString(), version.getReportId());
    }

    /**
     * Rollback to version
     */
    public ReportVersion rollbackToVersion(Long reportId, Long versionId, String username) {
        ReportVersion targetVersion = versions.get(versionId);
        if (targetVersion == null) {
            throw new IllegalArgumentException("Version not found: " + versionId);
        }

        // Create new version from target
        ReportVersion rollbackVersion = ReportVersion.builder()
                .reportId(reportId)
                .majorVersion(targetVersion.getMajorVersion())
                .minorVersion(targetVersion.getMinorVersion())
                .patchVersion(targetVersion.getPatchVersion())
                .createdBy(username)
                .commitMessage("Rollback to version " + targetVersion.getFullVersionString())
                .changeType(ReportVersion.ChangeType.RESTORED)
                .versionType(ReportVersion.VersionType.PATCH)
                .status(ReportVersion.VersionStatus.PUBLISHED)
                .previousVersionId(getCurrentVersion(reportId).map(ReportVersion::getVersionId).orElse(null))
                .contentSnapshot(targetVersion.getContentSnapshot())
                .parametersSnapshot(targetVersion.getParametersSnapshot())
                .configurationSnapshot(targetVersion.getConfigurationSnapshot())
                .dataSnapshot(targetVersion.getDataSnapshot())
                .isCurrent(true)
                .branchName(targetVersion.getBranchName())
                .isMainBranch(targetVersion.getIsMainBranch())
                .build();

        // Increment patch version
        rollbackVersion.incrementVersion(ReportVersion.VersionType.PATCH);

        return createVersion(rollbackVersion);
    }

    // ============================================================
    // Change Tracking
    // ============================================================

    /**
     * Track changes between versions
     */
    public void trackChanges(Long versionId, Map<String, Object> oldData, Map<String, Object> newData) {
        ReportVersion version = versions.get(versionId);
        if (version == null) {
            return;
        }

        List<String> fieldsChanged = new ArrayList<>();
        Map<String, ReportVersion.FieldChange> fieldChanges = new HashMap<>();

        for (String key : newData.keySet()) {
            Object oldValue = oldData.get(key);
            Object newValue = newData.get(key);

            if (!Objects.equals(oldValue, newValue)) {
                fieldsChanged.add(key);

                String changeType = oldValue == null ? "ADDED" :
                                  newValue == null ? "DELETED" : "MODIFIED";

                ReportVersion.FieldChange change = ReportVersion.FieldChange.builder()
                        .fieldName(key)
                        .oldValue(oldValue != null ? oldValue.toString() : null)
                        .newValue(newValue != null ? newValue.toString() : null)
                        .changeType(changeType)
                        .build();

                fieldChanges.put(key, change);
            }
        }

        version.setFieldsChanged(fieldsChanged);
        version.setFieldChanges(fieldChanges);

        log.info("Tracked {} field changes for version {}", fieldsChanged.size(), versionId);
    }

    /**
     * Calculate diff between versions
     */
    public String calculateDiff(Long fromVersionId, Long toVersionId) {
        ReportVersion fromVersion = versions.get(fromVersionId);
        ReportVersion toVersion = versions.get(toVersionId);

        if (fromVersion == null || toVersion == null) {
            return "";
        }

        StringBuilder diff = new StringBuilder();
        diff.append("Diff from ").append(fromVersion.getFullVersionString())
            .append(" to ").append(toVersion.getFullVersionString()).append("\n\n");

        // Compare field changes
        Map<String, ReportVersion.FieldChange> changes = toVersion.getFieldChanges();
        if (changes != null && !changes.isEmpty()) {
            diff.append("Field Changes:\n");
            changes.forEach((field, change) -> {
                diff.append("  - ").append(field).append(": ");
                diff.append(change.getOldValue()).append(" -> ").append(change.getNewValue());
                diff.append(" [").append(change.getChangeType()).append("]\n");
            });
        }

        return diff.toString();
    }

    /**
     * Compare versions
     */
    public Map<String, Object> compareVersions(Long versionId1, Long versionId2) {
        ReportVersion v1 = versions.get(versionId1);
        ReportVersion v2 = versions.get(versionId2);

        Map<String, Object> comparison = new HashMap<>();

        if (v1 == null || v2 == null) {
            comparison.put("error", "One or both versions not found");
            return comparison;
        }

        comparison.put("version1", v1.getFullVersionString());
        comparison.put("version2", v2.getFullVersionString());
        comparison.put("createdBy1", v1.getCreatedBy());
        comparison.put("createdBy2", v2.getCreatedBy());
        comparison.put("createdAt1", v1.getCreatedAt());
        comparison.put("createdAt2", v2.getCreatedAt());
        comparison.put("diff", calculateDiff(versionId1, versionId2));

        return comparison;
    }

    // ============================================================
    // Branch Management
    // ============================================================

    /**
     * Create branch
     */
    public ReportVersion createBranch(Long reportId, String branchName, Long fromVersionId, String createdBy) {
        ReportVersion fromVersion = versions.get(fromVersionId);
        if (fromVersion == null) {
            throw new IllegalArgumentException("Source version not found: " + fromVersionId);
        }

        ReportVersion branch = ReportVersion.builder()
                .reportId(reportId)
                .majorVersion(fromVersion.getMajorVersion())
                .minorVersion(fromVersion.getMinorVersion())
                .patchVersion(fromVersion.getPatchVersion())
                .createdBy(createdBy)
                .commitMessage("Created branch: " + branchName)
                .versionType(ReportVersion.VersionType.SNAPSHOT)
                .status(ReportVersion.VersionStatus.DRAFT)
                .branchName(branchName)
                .isMainBranch(false)
                .parentVersionId(fromVersionId)
                .contentSnapshot(fromVersion.getContentSnapshot())
                .parametersSnapshot(fromVersion.getParametersSnapshot())
                .configurationSnapshot(fromVersion.getConfigurationSnapshot())
                .build();

        return createVersion(branch);
    }

    /**
     * Get versions in branch
     */
    public List<ReportVersion> getVersionsInBranch(Long reportId, String branchName) {
        return reportVersions.getOrDefault(reportId, new ArrayList<>()).stream()
                .filter(v -> branchName.equals(v.getBranchName()))
                .sorted(Comparator.comparing(ReportVersion::getCreatedAt))
                .collect(Collectors.toList());
    }

    /**
     * Get branches for report
     */
    public Set<String> getBranchesForReport(Long reportId) {
        return reportVersions.getOrDefault(reportId, new ArrayList<>()).stream()
                .map(ReportVersion::getBranchName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    // ============================================================
    // Statistics
    // ============================================================

    /**
     * Get version statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalVersions", versions.size());
        stats.put("totalReports", reportVersions.size());

        stats.put("versionsByStatus", versions.values().stream()
                .collect(Collectors.groupingBy(
                        v -> v.getStatus().toString(),
                        Collectors.counting())));

        stats.put("versionsByType", versions.values().stream()
                .collect(Collectors.groupingBy(
                        v -> v.getVersionType().toString(),
                        Collectors.counting())));

        return stats;
    }

    /**
     * Get report version statistics
     */
    public Map<String, Object> getReportStatistics(Long reportId) {
        Map<String, Object> stats = new HashMap<>();

        List<ReportVersion> reportVers = getVersionsForReport(reportId);

        stats.put("versionCount", reportVers.size());
        stats.put("currentVersion", getCurrentVersion(reportId)
                .map(ReportVersion::getFullVersionString).orElse("None"));
        stats.put("latestVersion", getLatestVersion(reportId)
                .map(ReportVersion::getFullVersionString).orElse("None"));

        stats.put("publishedVersions", reportVers.stream()
                .filter(ReportVersion::isPublished).count());
        stats.put("draftVersions", reportVers.stream()
                .filter(ReportVersion::isDraft).count());

        stats.put("branches", getBranchesForReport(reportId).size());

        stats.put("totalDownloads", reportVers.stream()
                .mapToInt(v -> v.getDownloadCount() != null ? v.getDownloadCount() : 0).sum());
        stats.put("totalViews", reportVers.stream()
                .mapToInt(v -> v.getViewCount() != null ? v.getViewCount() : 0).sum());

        return stats;
    }

    /**
     * Get version history
     */
    public List<Map<String, Object>> getVersionHistory(Long reportId) {
        return getVersionsForReport(reportId).stream()
                .map(v -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("versionId", v.getVersionId());
                    entry.put("version", v.getFullVersionString());
                    entry.put("createdBy", v.getCreatedBy());
                    entry.put("createdAt", v.getCreatedAt());
                    entry.put("commitMessage", v.getCommitMessage());
                    entry.put("status", v.getStatus());
                    entry.put("isCurrent", v.isCurrentVersion());
                    entry.put("changeType", v.getChangeType());
                    entry.put("totalChanges", v.getTotalChanges());
                    return entry;
                })
                .collect(Collectors.toList());
    }

    /**
     * Record version access
     */
    public void recordVersionView(Long versionId) {
        ReportVersion version = versions.get(versionId);
        if (version != null) {
            version.setViewCount((version.getViewCount() != null ? version.getViewCount() : 0) + 1);
        }
    }

    /**
     * Record version download
     */
    public void recordVersionDownload(Long versionId) {
        ReportVersion version = versions.get(versionId);
        if (version != null) {
            version.setDownloadCount((version.getDownloadCount() != null ? version.getDownloadCount() : 0) + 1);
        }
    }
}
