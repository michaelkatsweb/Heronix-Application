package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Backup DTO
 *
 * Represents a backup of report data and configurations.
 *
 * Features:
 * - Full and incremental backups
 * - Compression support
 * - Encryption support
 * - Automated scheduling
 * - Retention policies
 * - Recovery verification
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 74 - Report Backup & Recovery
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportBackup {

    /**
     * Backup type enumeration
     */
    public enum BackupType {
        FULL,           // Full system backup
        INCREMENTAL,    // Incremental backup (changes only)
        DIFFERENTIAL,   // Differential backup (changes since last full)
        SNAPSHOT,       // Point-in-time snapshot
        REPORT,         // Single report backup
        CONFIGURATION   // Configuration only
    }

    /**
     * Backup status enumeration
     */
    public enum BackupStatus {
        PENDING,        // Scheduled but not started
        IN_PROGRESS,    // Currently running
        COMPLETED,      // Successfully completed
        FAILED,         // Failed
        VERIFYING,      // Verification in progress
        VERIFIED,       // Verified successfully
        CORRUPTED,      // Corruption detected
        ARCHIVED        // Moved to archive
    }

    /**
     * Storage location enumeration
     */
    public enum StorageLocation {
        LOCAL,          // Local file system
        NETWORK,        // Network storage
        CLOUD,          // Cloud storage
        S3,             // Amazon S3
        AZURE,          // Azure Blob Storage
        GCP,            // Google Cloud Storage
        DATABASE        // Database storage
    }

    // ============================================================
    // Basic Backup Information
    // ============================================================

    /**
     * Backup ID
     */
    private Long backupId;

    /**
     * Backup name
     */
    private String backupName;

    /**
     * Description
     */
    private String description;

    /**
     * Backup type
     */
    private BackupType backupType;

    /**
     * Backup status
     */
    private BackupStatus status;

    // ============================================================
    // Timing Information
    // ============================================================

    /**
     * Created at timestamp
     */
    private LocalDateTime createdAt;

    /**
     * Started at timestamp
     */
    private LocalDateTime startedAt;

    /**
     * Completed at timestamp
     */
    private LocalDateTime completedAt;

    /**
     * Duration (milliseconds)
     */
    private Long durationMs;

    /**
     * Expires at timestamp
     */
    private LocalDateTime expiresAt;

    /**
     * Last verified at
     */
    private LocalDateTime lastVerifiedAt;

    // ============================================================
    // Backup Content
    // ============================================================

    /**
     * Report IDs included in backup
     */
    private List<Long> reportIds;

    /**
     * Total reports backed up
     */
    private Integer totalReports;

    /**
     * Total files backed up
     */
    private Integer totalFiles;

    /**
     * Include templates
     */
    private Boolean includeTemplates;

    /**
     * Include configurations
     */
    private Boolean includeConfigurations;

    /**
     * Include schedules
     */
    private Boolean includeSchedules;

    /**
     * Include permissions
     */
    private Boolean includePermissions;

    /**
     * Include versions
     */
    private Boolean includeVersions;

    /**
     * Include analytics
     */
    private Boolean includeAnalytics;

    // ============================================================
    // Storage Information
    // ============================================================

    /**
     * Storage location
     */
    private StorageLocation storageLocation;

    /**
     * Storage path
     */
    private String storagePath;

    /**
     * File name
     */
    private String fileName;

    /**
     * Original size (bytes)
     */
    private Long originalSizeBytes;

    /**
     * Compressed size (bytes)
     */
    private Long compressedSizeBytes;

    /**
     * Compression ratio
     */
    private Double compressionRatio;

    /**
     * Is compressed
     */
    private Boolean isCompressed;

    /**
     * Compression algorithm
     */
    private String compressionAlgorithm;

    // ============================================================
    // Security
    // ============================================================

    /**
     * Is encrypted
     */
    private Boolean isEncrypted;

    /**
     * Encryption algorithm
     */
    private String encryptionAlgorithm;

    /**
     * Encryption key ID
     */
    private String encryptionKeyId;

    /**
     * Password protected
     */
    private Boolean passwordProtected;

    /**
     * Checksum (MD5/SHA256)
     */
    private String checksum;

    /**
     * Checksum algorithm
     */
    private String checksumAlgorithm;

    // ============================================================
    // Verification
    // ============================================================

    /**
     * Is verified
     */
    private Boolean isVerified;

    /**
     * Verification status
     */
    private String verificationStatus;

    /**
     * Verification errors
     */
    private List<String> verificationErrors;

    /**
     * Integrity check passed
     */
    private Boolean integrityCheckPassed;

    // ============================================================
    // Metadata
    // ============================================================

    /**
     * Created by username
     */
    private String createdBy;

    /**
     * Automated backup
     */
    private Boolean isAutomated;

    /**
     * Schedule ID (if automated)
     */
    private Long scheduleId;

    /**
     * Parent backup ID (for incrementals)
     */
    private Long parentBackupId;

    /**
     * Base backup ID (for differentials)
     */
    private Long baseBackupId;

    /**
     * Backup generation
     */
    private Integer generation;

    /**
     * Retention days
     */
    private Integer retentionDays;

    /**
     * Tags
     */
    private List<String> tags;

    /**
     * Custom metadata
     */
    private Map<String, Object> metadata;

    /**
     * Notes
     */
    private String notes;

    // ============================================================
    // Recovery Information
    // ============================================================

    /**
     * Recovery point objective (minutes)
     */
    private Integer recoveryPointObjective;

    /**
     * Recovery time objective (minutes)
     */
    private Integer recoveryTimeObjective;

    /**
     * Can be restored
     */
    private Boolean canBeRestored;

    /**
     * Restore count
     */
    private Integer restoreCount;

    /**
     * Last restored at
     */
    private LocalDateTime lastRestoredAt;

    /**
     * Last restored by
     */
    private String lastRestoredBy;

    // ============================================================
    // Statistics
    // ============================================================

    /**
     * Success rate (for scheduled backups)
     */
    private Double successRate;

    /**
     * Average duration (ms)
     */
    private Long avgDurationMs;

    /**
     * Download count
     */
    private Integer downloadCount;

    // ============================================================
    // Error Handling
    // ============================================================

    /**
     * Error message
     */
    private String errorMessage;

    /**
     * Error details
     */
    private String errorDetails;

    /**
     * Retry count
     */
    private Integer retryCount;

    /**
     * Max retries
     */
    private Integer maxRetries;

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Check if backup is complete
     */
    public boolean isComplete() {
        return status == BackupStatus.COMPLETED || status == BackupStatus.VERIFIED;
    }

    /**
     * Check if backup is in progress
     */
    public boolean isInProgress() {
        return status == BackupStatus.IN_PROGRESS || status == BackupStatus.VERIFYING;
    }

    /**
     * Check if backup failed
     */
    public boolean isFailed() {
        return status == BackupStatus.FAILED || status == BackupStatus.CORRUPTED;
    }

    /**
     * Check if backup is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Calculate compression ratio
     */
    public void calculateCompressionRatio() {
        if (originalSizeBytes != null && compressedSizeBytes != null && originalSizeBytes > 0) {
            compressionRatio = (1.0 - (compressedSizeBytes.doubleValue() / originalSizeBytes)) * 100;
        }
    }

    /**
     * Calculate duration
     */
    public void calculateDuration() {
        if (startedAt != null && completedAt != null) {
            durationMs = java.time.Duration.between(startedAt, completedAt).toMillis();
        }
    }

    /**
     * Get formatted original size
     */
    public String getFormattedOriginalSize() {
        return formatSize(originalSizeBytes);
    }

    /**
     * Get formatted compressed size
     */
    public String getFormattedCompressedSize() {
        return formatSize(compressedSizeBytes);
    }

    /**
     * Format size in bytes
     */
    private String formatSize(Long sizeBytes) {
        if (sizeBytes == null) {
            return "0 B";
        }

        long size = sizeBytes;
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.2f MB", size / (1024.0 * 1024));
        return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
    }

    /**
     * Get formatted duration
     */
    public String getFormattedDuration() {
        if (durationMs == null) {
            return "N/A";
        }

        long seconds = durationMs / 1000;
        if (seconds < 60) {
            return seconds + " seconds";
        } else if (seconds < 3600) {
            return (seconds / 60) + " minutes";
        } else {
            return (seconds / 3600) + " hours";
        }
    }

    /**
     * Check if needs verification
     */
    public boolean needsVerification() {
        if (!isComplete()) {
            return false;
        }

        if (Boolean.FALSE.equals(isVerified)) {
            return true;
        }

        // Re-verify every 30 days
        if (lastVerifiedAt != null) {
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            return lastVerifiedAt.isBefore(thirtyDaysAgo);
        }

        return true;
    }

    /**
     * Increment restore count
     */
    public void incrementRestoreCount() {
        restoreCount = (restoreCount != null ? restoreCount : 0) + 1;
        lastRestoredAt = LocalDateTime.now();
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
     * Add verification error
     */
    public void addVerificationError(String error) {
        if (verificationErrors == null) {
            verificationErrors = new java.util.ArrayList<>();
        }
        verificationErrors.add(error);
    }

    /**
     * Validate backup
     */
    public void validate() {
        if (backupName == null || backupName.trim().isEmpty()) {
            throw new IllegalArgumentException("Backup name is required");
        }

        if (backupType == null) {
            throw new IllegalArgumentException("Backup type is required");
        }

        if (storageLocation == null) {
            throw new IllegalArgumentException("Storage location is required");
        }
    }

    /**
     * Check if can be deleted
     */
    public boolean canBeDeleted() {
        // Don't delete if it's a base backup for others
        if (generation != null && generation == 0) {
            return false;
        }

        // Don't delete if not expired
        if (!isExpired()) {
            return false;
        }

        return true;
    }

    /**
     * Get age in days
     */
    public long getAgeInDays() {
        if (createdAt == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
    }

    /**
     * Get days until expiration
     */
    public Long getDaysUntilExpiration() {
        if (expiresAt == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(expiresAt)) {
            return 0L;
        }

        return java.time.temporal.ChronoUnit.DAYS.between(now, expiresAt);
    }
}
