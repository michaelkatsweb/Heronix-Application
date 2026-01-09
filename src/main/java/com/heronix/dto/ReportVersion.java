package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Version DTO
 *
 * Represents a version of a report with full history tracking.
 *
 * Features:
 * - Version numbering (major.minor.patch)
 * - Change tracking
 * - Snapshot storage
 * - Comparison support
 * - Rollback capability
 * - Branch/merge support
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 72 - Report Version Control & History
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportVersion {

    /**
     * Version type enumeration
     */
    public enum VersionType {
        MAJOR,          // Major version change (1.0.0 -> 2.0.0)
        MINOR,          // Minor version change (1.0.0 -> 1.1.0)
        PATCH,          // Patch version change (1.0.0 -> 1.0.1)
        SNAPSHOT,       // Development snapshot
        DRAFT,          // Draft version
        RELEASE         // Release version
    }

    /**
     * Version status enumeration
     */
    public enum VersionStatus {
        DRAFT,          // Work in progress
        REVIEW,         // Under review
        APPROVED,       // Approved
        PUBLISHED,      // Published/released
        DEPRECATED,     // Deprecated
        ARCHIVED        // Archived
    }

    /**
     * Change type enumeration
     */
    public enum ChangeType {
        CREATED,        // Report created
        MODIFIED,       // Report modified
        PARAMETERS_CHANGED,  // Parameters changed
        TEMPLATE_CHANGED,    // Template changed
        DATA_UPDATED,        // Data updated
        STRUCTURE_CHANGED,   // Structure changed
        METADATA_CHANGED,    // Metadata changed
        RESTORED,       // Restored from previous version
        MERGED          // Merged from branch
    }

    // ============================================================
    // Basic Version Information
    // ============================================================

    /**
     * Version ID
     */
    private Long versionId;

    /**
     * Report ID
     */
    private Long reportId;

    /**
     * Report name
     */
    private String reportName;

    /**
     * Version number (semantic versioning)
     */
    private String versionNumber;

    /**
     * Major version
     */
    private Integer majorVersion;

    /**
     * Minor version
     */
    private Integer minorVersion;

    /**
     * Patch version
     */
    private Integer patchVersion;

    /**
     * Version type
     */
    private VersionType versionType;

    /**
     * Version status
     */
    private VersionStatus status;

    // ============================================================
    // Version Metadata
    // ============================================================

    /**
     * Created by username
     */
    private String createdBy;

    /**
     * Created by display name
     */
    private String createdByDisplayName;

    /**
     * Created at timestamp
     */
    private LocalDateTime createdAt;

    /**
     * Commit message/description
     */
    private String commitMessage;

    /**
     * Change type
     */
    private ChangeType changeType;

    /**
     * Change summary
     */
    private String changeSummary;

    /**
     * Detailed change log
     */
    private List<String> changeLog;

    // ============================================================
    // Version Content
    // ============================================================

    /**
     * Report content snapshot
     */
    private String contentSnapshot;

    /**
     * Report parameters snapshot
     */
    private Map<String, Object> parametersSnapshot;

    /**
     * Report configuration snapshot
     */
    private Map<String, Object> configurationSnapshot;

    /**
     * Report data snapshot (serialized)
     */
    private String dataSnapshot;

    /**
     * Template ID at this version
     */
    private Long templateId;

    /**
     * Template snapshot
     */
    private String templateSnapshot;

    // ============================================================
    // Version Relationships
    // ============================================================

    /**
     * Parent version ID
     */
    private Long parentVersionId;

    /**
     * Previous version ID
     */
    private Long previousVersionId;

    /**
     * Branch name
     */
    private String branchName;

    /**
     * Is main/master branch
     */
    private Boolean isMainBranch;

    /**
     * Merged from version ID
     */
    private Long mergedFromVersionId;

    /**
     * Merge commit message
     */
    private String mergeMessage;

    // ============================================================
    // Changes Tracking
    // ============================================================

    /**
     * Fields changed
     */
    private List<String> fieldsChanged;

    /**
     * Field changes (field -> before/after)
     */
    private Map<String, FieldChange> fieldChanges;

    /**
     * Lines added
     */
    private Integer linesAdded;

    /**
     * Lines removed
     */
    private Integer linesRemoved;

    /**
     * Lines modified
     */
    private Integer linesModified;

    /**
     * Diff from previous version
     */
    private String diff;

    // ============================================================
    // Review and Approval
    // ============================================================

    /**
     * Reviewed by
     */
    private String reviewedBy;

    /**
     * Reviewed at
     */
    private LocalDateTime reviewedAt;

    /**
     * Review comments
     */
    private String reviewComments;

    /**
     * Approved by
     */
    private String approvedBy;

    /**
     * Approved at
     */
    private LocalDateTime approvedAt;

    /**
     * Approval comments
     */
    private String approvalComments;

    // ============================================================
    // Publication
    // ============================================================

    /**
     * Published at
     */
    private LocalDateTime publishedAt;

    /**
     * Published by
     */
    private String publishedBy;

    /**
     * Is current version
     */
    private Boolean isCurrent;

    /**
     * Is latest version
     */
    private Boolean isLatest;

    /**
     * Download count
     */
    private Integer downloadCount;

    /**
     * View count
     */
    private Integer viewCount;

    // ============================================================
    // File Information
    // ============================================================

    /**
     * File size (bytes)
     */
    private Long fileSizeBytes;

    /**
     * File checksum (MD5/SHA)
     */
    private String fileChecksum;

    /**
     * Storage path
     */
    private String storagePath;

    /**
     * Compressed
     */
    private Boolean isCompressed;

    // ============================================================
    // Tags and Labels
    // ============================================================

    /**
     * Version tags
     */
    private List<String> tags;

    /**
     * Version labels
     */
    private Map<String, String> labels;

    /**
     * Release notes
     */
    private String releaseNotes;

    // ============================================================
    // Metadata
    // ============================================================

    /**
     * Custom metadata
     */
    private Map<String, Object> metadata;

    /**
     * Notes
     */
    private String notes;

    // ============================================================
    // Nested Classes
    // ============================================================

    /**
     * Field change details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldChange {
        private String fieldName;
        private String oldValue;
        private String newValue;
        private String changeType;  // ADDED, MODIFIED, DELETED
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Get full version string
     */
    public String getFullVersionString() {
        if (versionNumber != null) {
            return versionNumber;
        }

        if (majorVersion != null && minorVersion != null && patchVersion != null) {
            return String.format("%d.%d.%d", majorVersion, minorVersion, patchVersion);
        }

        return "0.0.1";
    }

    /**
     * Check if this is a draft version
     */
    public boolean isDraft() {
        return status == VersionStatus.DRAFT || versionType == VersionType.DRAFT;
    }

    /**
     * Check if this is a published version
     */
    public boolean isPublished() {
        return status == VersionStatus.PUBLISHED;
    }

    /**
     * Check if this is the current version
     */
    public boolean isCurrentVersion() {
        return Boolean.TRUE.equals(isCurrent);
    }

    /**
     * Check if this is the latest version
     */
    public boolean isLatestVersion() {
        return Boolean.TRUE.equals(isLatest);
    }

    /**
     * Increment version number
     */
    public void incrementVersion(VersionType type) {
        if (majorVersion == null) majorVersion = 0;
        if (minorVersion == null) minorVersion = 0;
        if (patchVersion == null) patchVersion = 1;

        switch (type) {
            case MAJOR:
                majorVersion++;
                minorVersion = 0;
                patchVersion = 0;
                break;
            case MINOR:
                minorVersion++;
                patchVersion = 0;
                break;
            case PATCH:
                patchVersion++;
                break;
        }

        versionNumber = getFullVersionString();
        this.versionType = type;
    }

    /**
     * Compare version numbers
     */
    public int compareVersion(ReportVersion other) {
        if (other == null) return 1;

        int majorCompare = Integer.compare(
                this.majorVersion != null ? this.majorVersion : 0,
                other.majorVersion != null ? other.majorVersion : 0
        );
        if (majorCompare != 0) return majorCompare;

        int minorCompare = Integer.compare(
                this.minorVersion != null ? this.minorVersion : 0,
                other.minorVersion != null ? other.minorVersion : 0
        );
        if (minorCompare != 0) return minorCompare;

        return Integer.compare(
                this.patchVersion != null ? this.patchVersion : 0,
                other.patchVersion != null ? other.patchVersion : 0
        );
    }

    /**
     * Check if this version is newer than another
     */
    public boolean isNewerThan(ReportVersion other) {
        return compareVersion(other) > 0;
    }

    /**
     * Get total changes
     */
    public int getTotalChanges() {
        int total = 0;
        if (linesAdded != null) total += linesAdded;
        if (linesRemoved != null) total += linesRemoved;
        if (linesModified != null) total += linesModified;
        return total;
    }

    /**
     * Add field change
     */
    public void addFieldChange(String fieldName, String oldValue, String newValue, String changeType) {
        if (fieldChanges == null) {
            fieldChanges = new java.util.HashMap<>();
        }

        FieldChange change = FieldChange.builder()
                .fieldName(fieldName)
                .oldValue(oldValue)
                .newValue(newValue)
                .changeType(changeType)
                .build();

        fieldChanges.put(fieldName, change);

        if (fieldsChanged == null) {
            fieldsChanged = new java.util.ArrayList<>();
        }
        if (!fieldsChanged.contains(fieldName)) {
            fieldsChanged.add(fieldName);
        }
    }

    /**
     * Check if field was changed
     */
    public boolean wasFieldChanged(String fieldName) {
        return fieldsChanged != null && fieldsChanged.contains(fieldName);
    }

    /**
     * Get formatted file size
     */
    public String getFormattedFileSize() {
        if (fileSizeBytes == null) {
            return "0 B";
        }

        long size = fileSizeBytes;
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.2f MB", size / (1024.0 * 1024));
        return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
    }

    /**
     * Add tag
     */
    public void addTag(String tag) {
        if (tags == null) {
            tags = new java.util.ArrayList<>();
        }
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    /**
     * Add label
     */
    public void addLabel(String key, String value) {
        if (labels == null) {
            labels = new java.util.HashMap<>();
        }
        labels.put(key, value);
    }

    /**
     * Validate version
     */
    public void validate() {
        if (reportId == null) {
            throw new IllegalArgumentException("Report ID is required");
        }

        if (createdBy == null || createdBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Created by username is required");
        }

        if (commitMessage == null || commitMessage.trim().isEmpty()) {
            throw new IllegalArgumentException("Commit message is required");
        }

        if (majorVersion == null || minorVersion == null || patchVersion == null) {
            throw new IllegalArgumentException("Version numbers (major.minor.patch) are required");
        }
    }

    /**
     * Create initial version
     */
    public static ReportVersion createInitialVersion(Long reportId, String createdBy) {
        return ReportVersion.builder()
                .reportId(reportId)
                .majorVersion(1)
                .minorVersion(0)
                .patchVersion(0)
                .versionNumber("1.0.0")
                .versionType(VersionType.MAJOR)
                .status(VersionStatus.PUBLISHED)
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .commitMessage("Initial version")
                .changeType(ChangeType.CREATED)
                .isCurrent(true)
                .isLatest(true)
                .isMainBranch(true)
                .branchName("main")
                .downloadCount(0)
                .viewCount(0)
                .build();
    }
}
