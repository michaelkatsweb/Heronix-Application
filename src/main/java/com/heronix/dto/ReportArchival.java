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
 * Report Archival DTO
 *
 * Represents long-term archival and storage management for reports.
 *
 * Features:
 * - Tiered storage management
 * - Compression and encryption
 * - Retention policy enforcement
 * - Archive lifecycle management
 * - Retrieval and restoration
 * - Compliance archiving
 * - Storage optimization
 * - Data integrity verification
 *
 * Archive Status:
 * - PENDING - Queued for archival
 * - ARCHIVING - Currently archiving
 * - ARCHIVED - Successfully archived
 * - RETRIEVING - Being retrieved
 * - RESTORED - Restored from archive
 * - EXPIRING - Pending expiration
 * - EXPIRED - Retention period expired
 * - DELETED - Permanently deleted
 *
 * Storage Tier:
 * - HOT - Immediate access (active)
 * - WARM - Quick access (recent)
 * - COLD - Standard retrieval (archived)
 * - GLACIER - Long-term deep archive
 * - DEEP_ARCHIVE - Ultra-long-term storage
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 93 - Report Archival & Long-term Storage
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportArchival {

    private Long archiveId;
    private Long reportId;
    private String archiveName;
    private String description;

    // Archive status
    private ArchiveStatus status;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime archivedAt;
    private LocalDateTime lastAccessedAt;
    private LocalDateTime expiresAt;
    private Integer accessCount;

    // Storage configuration
    private StorageTier storageTier;
    private String storageLocation;
    private String storageProvider; // S3, Azure Blob, Google Cloud Storage, Local
    private Long originalSizeBytes;
    private Long archivedSizeBytes;
    private Double compressionRatio;
    private String compressionAlgorithm; // GZIP, BZIP2, LZMA, ZSTD

    // Encryption
    private Boolean encrypted;
    private String encryptionAlgorithm; // AES-256, RSA
    private String encryptionKeyId;
    private LocalDateTime encryptedAt;

    // Retention policy
    private RetentionPolicy retentionPolicy;
    private Integer retentionDays;
    private LocalDateTime retentionStartDate;
    private LocalDateTime retentionEndDate;
    private Boolean retentionLocked; // Compliance lock
    private String retentionReason;

    // Compliance
    private List<String> complianceStandards; // GDPR, HIPAA, SOX, etc.
    private Boolean legalHold;
    private LocalDateTime legalHoldStartDate;
    private String legalHoldReason;
    private List<ComplianceTag> complianceTags;

    // Archive metadata
    private Map<String, Object> metadata;
    private List<String> tags;
    private String archiveFormat; // ZIP, TAR, 7Z, Custom
    private String checksum; // For integrity verification
    private String checksumAlgorithm; // MD5, SHA-256, SHA-512

    // Versioning
    private Integer version;
    private String previousVersionId;
    private List<ArchiveVersion> versions;
    private Boolean versioningEnabled;

    // Retrieval
    private RetrievalTier retrievalTier;
    private Integer retrievalTimeMinutes;
    private Double retrievalCostEstimate;
    private List<RetrievalRequest> retrievalRequests;
    private Integer totalRetrievals;

    // Storage metrics
    private StorageMetrics storageMetrics;
    private Double storageCostPerMonth;
    private String currency;

    // Lifecycle
    private List<LifecycleTransition> lifecycleTransitions;
    private LocalDateTime lastTransitionAt;
    private String currentLifecycleStage;

    // Audit trail
    private List<ArchiveEvent> auditTrail;
    private LocalDateTime lastAuditAt;

    // Backup
    private Boolean backedUp;
    private String backupLocation;
    private LocalDateTime lastBackupAt;
    private Integer backupCopies;

    // Configuration
    private Map<String, Object> configuration;
    private Boolean configurationLocked;

    /**
     * Archive Status
     */
    public enum ArchiveStatus {
        PENDING,        // Queued for archival
        ARCHIVING,      // Currently archiving
        ARCHIVED,       // Successfully archived
        RETRIEVING,     // Being retrieved
        RESTORED,       // Restored from archive
        EXPIRING,       // Pending expiration
        EXPIRED,        // Retention period expired
        DELETED         // Permanently deleted
    }

    /**
     * Storage Tier
     */
    public enum StorageTier {
        HOT,            // Immediate access (active)
        WARM,           // Quick access (recent)
        COLD,           // Standard retrieval (archived)
        GLACIER,        // Long-term deep archive
        DEEP_ARCHIVE    // Ultra-long-term storage
    }

    /**
     * Retention Policy
     */
    public enum RetentionPolicy {
        SHORT_TERM,     // < 1 year
        MEDIUM_TERM,    // 1-5 years
        LONG_TERM,      // 5-10 years
        PERMANENT,      // Indefinite retention
        COMPLIANCE,     // Compliance-driven
        CUSTOM          // Custom retention period
    }

    /**
     * Retrieval Tier
     */
    public enum RetrievalTier {
        EXPEDITED,      // 1-5 minutes
        STANDARD,       // 3-5 hours
        BULK            // 5-12 hours
    }

    /**
     * Lifecycle Stage
     */
    public enum LifecycleStage {
        ACTIVE,
        TRANSITIONING,
        ARCHIVED,
        DEEP_ARCHIVED,
        EXPIRING,
        EXPIRED
    }

    /**
     * Compliance Tag
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceTag {
        private String tagId;
        private String tagName;
        private String standard; // GDPR, HIPAA, SOX, etc.
        private String category;
        private String value;
        private LocalDateTime appliedAt;
        private String appliedBy;
        private Boolean mandatory;
    }

    /**
     * Archive Version
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArchiveVersion {
        private String versionId;
        private Integer versionNumber;
        private LocalDateTime createdAt;
        private String createdBy;
        private Long sizeBytes;
        private String storageLocation;
        private String checksum;
        private Boolean current;
        private Map<String, Object> versionMetadata;
    }

    /**
     * Retrieval Request
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetrievalRequest {
        private String requestId;
        private String requestedBy;
        private LocalDateTime requestedAt;
        private RetrievalTier retrievalTier;
        private String status; // PENDING, IN_PROGRESS, COMPLETED, FAILED
        private LocalDateTime completedAt;
        private Integer retrievalTimeMinutes;
        private Double cost;
        private String reason;
        private String destinationPath;
    }

    /**
     * Storage Metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StorageMetrics {
        private Long totalSizeBytes;
        private Long compressedSizeBytes;
        private Double compressionRatio;
        private Long storageUsedBytes;
        private Long storageAvailableBytes;
        private Integer totalFiles;
        private Integer totalVersions;
        private Double averageCompressionRatio;
        private LocalDateTime measuredAt;
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
        private StorageTier fromTier;
        private StorageTier toTier;
        private LocalDateTime transitionedAt;
        private String triggeredBy; // POLICY, MANUAL, AUTOMATIC
        private String reason;
        private Long durationMs;
        private Boolean successful;
    }

    /**
     * Archive Event
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArchiveEvent {
        private String eventId;
        private String eventType;
        private String description;
        private LocalDateTime timestamp;
        private String userId;
        private Map<String, Object> eventData;
    }

    /**
     * Helper Methods
     */

    public void archive() {
        status = ArchiveStatus.ARCHIVING;
    }

    public void completeArchival() {
        status = ArchiveStatus.ARCHIVED;
        archivedAt = LocalDateTime.now();

        if (originalSizeBytes != null && archivedSizeBytes != null && archivedSizeBytes > 0) {
            compressionRatio = (double) originalSizeBytes / archivedSizeBytes;
        }

        recordEvent("ARCHIVE_COMPLETED", "Archive completed successfully", createdBy);
    }

    public void startRetrieval(String requestedBy, RetrievalTier tier) {
        status = ArchiveStatus.RETRIEVING;

        RetrievalRequest request = RetrievalRequest.builder()
                .requestId(java.util.UUID.randomUUID().toString())
                .requestedBy(requestedBy)
                .requestedAt(LocalDateTime.now())
                .retrievalTier(tier)
                .status("PENDING")
                .build();

        if (retrievalRequests == null) {
            retrievalRequests = new ArrayList<>();
        }
        retrievalRequests.add(request);

        totalRetrievals = (totalRetrievals != null ? totalRetrievals : 0) + 1;

        recordEvent("RETRIEVAL_STARTED", "Retrieval started", requestedBy);
    }

    public void completeRetrieval(String requestId, boolean success) {
        if (retrievalRequests == null) {
            return;
        }

        for (RetrievalRequest request : retrievalRequests) {
            if (request.getRequestId().equals(requestId)) {
                request.setStatus(success ? "COMPLETED" : "FAILED");
                request.setCompletedAt(LocalDateTime.now());

                if (request.getRequestedAt() != null && request.getCompletedAt() != null) {
                    long minutes = java.time.Duration.between(
                            request.getRequestedAt(), request.getCompletedAt()
                    ).toMinutes();
                    request.setRetrievalTimeMinutes((int) minutes);
                }
                break;
            }
        }

        if (success) {
            status = ArchiveStatus.RESTORED;
            lastAccessedAt = LocalDateTime.now();
            accessCount = (accessCount != null ? accessCount : 0) + 1;

            recordEvent("RETRIEVAL_COMPLETED", "Retrieval completed successfully", null);
        } else {
            status = ArchiveStatus.ARCHIVED;
            recordEvent("RETRIEVAL_FAILED", "Retrieval failed", null);
        }
    }

    public void transitionStorageTier(StorageTier newTier, String triggeredBy, String reason) {
        if (storageTier == newTier) {
            return;
        }

        LifecycleTransition transition = LifecycleTransition.builder()
                .transitionId(java.util.UUID.randomUUID().toString())
                .fromTier(storageTier)
                .toTier(newTier)
                .transitionedAt(LocalDateTime.now())
                .triggeredBy(triggeredBy)
                .reason(reason)
                .successful(false)
                .build();

        if (lifecycleTransitions == null) {
            lifecycleTransitions = new ArrayList<>();
        }

        LocalDateTime startTime = LocalDateTime.now();

        storageTier = newTier;
        lastTransitionAt = LocalDateTime.now();

        transition.setSuccessful(true);
        transition.setDurationMs(
                java.time.Duration.between(startTime, LocalDateTime.now()).toMillis()
        );

        lifecycleTransitions.add(transition);

        recordEvent("TIER_TRANSITION", "Storage tier transitioned from " +
                transition.getFromTier() + " to " + transition.getToTier(), triggeredBy);
    }

    public void applyRetentionPolicy(RetentionPolicy policy, Integer days) {
        this.retentionPolicy = policy;
        this.retentionDays = days;
        this.retentionStartDate = LocalDateTime.now();

        if (days != null) {
            this.retentionEndDate = LocalDateTime.now().plusDays(days);
            this.expiresAt = this.retentionEndDate;
        }

        recordEvent("RETENTION_POLICY_APPLIED", "Retention policy applied: " + policy, createdBy);
    }

    public void addComplianceTag(String tagName, String standard, String category, String value) {
        if (complianceTags == null) {
            complianceTags = new ArrayList<>();
        }

        ComplianceTag tag = ComplianceTag.builder()
                .tagId(java.util.UUID.randomUUID().toString())
                .tagName(tagName)
                .standard(standard)
                .category(category)
                .value(value)
                .appliedAt(LocalDateTime.now())
                .appliedBy(createdBy)
                .mandatory(false)
                .build();

        complianceTags.add(tag);

        recordEvent("COMPLIANCE_TAG_ADDED", "Compliance tag added: " + tagName, createdBy);
    }

    public void enableLegalHold(String reason) {
        legalHold = true;
        legalHoldStartDate = LocalDateTime.now();
        legalHoldReason = reason;
        retentionLocked = true;

        recordEvent("LEGAL_HOLD_ENABLED", "Legal hold enabled: " + reason, createdBy);
    }

    public void releaseLegalHold() {
        legalHold = false;
        legalHoldReason = null;

        recordEvent("LEGAL_HOLD_RELEASED", "Legal hold released", createdBy);
    }

    public void addVersion(Long sizeBytes, String storageLocation, String checksum) {
        if (versions == null) {
            versions = new ArrayList<>();
        }

        // Mark previous versions as not current
        for (ArchiveVersion v : versions) {
            v.setCurrent(false);
        }

        int versionNumber = versions.size() + 1;

        ArchiveVersion version = ArchiveVersion.builder()
                .versionId(java.util.UUID.randomUUID().toString())
                .versionNumber(versionNumber)
                .createdAt(LocalDateTime.now())
                .createdBy(createdBy)
                .sizeBytes(sizeBytes)
                .storageLocation(storageLocation)
                .checksum(checksum)
                .current(true)
                .versionMetadata(new HashMap<>())
                .build();

        versions.add(version);
        this.version = versionNumber;

        recordEvent("VERSION_CREATED", "New version created: v" + versionNumber, createdBy);
    }

    public void recordEvent(String eventType, String description, String userId) {
        if (auditTrail == null) {
            auditTrail = new ArrayList<>();
        }

        ArchiveEvent event = ArchiveEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType(eventType)
                .description(description)
                .timestamp(LocalDateTime.now())
                .userId(userId)
                .eventData(new HashMap<>())
                .build();

        auditTrail.add(event);
        lastAuditAt = LocalDateTime.now();
    }

    public void calculateStorageMetrics() {
        if (storageMetrics == null) {
            storageMetrics = StorageMetrics.builder().build();
        }

        storageMetrics.setTotalSizeBytes(originalSizeBytes);
        storageMetrics.setCompressedSizeBytes(archivedSizeBytes);
        storageMetrics.setCompressionRatio(compressionRatio);
        storageMetrics.setTotalFiles(1);
        storageMetrics.setTotalVersions(versions != null ? versions.size() : 1);
        storageMetrics.setMeasuredAt(LocalDateTime.now());
    }

    public boolean isExpired() {
        if (expiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isRetentionLocked() {
        return Boolean.TRUE.equals(retentionLocked) || Boolean.TRUE.equals(legalHold);
    }

    public boolean canBeDeleted() {
        return !isRetentionLocked() &&
               (isExpired() || status == ArchiveStatus.EXPIRED);
    }

    public void markForExpiration() {
        if (isExpired() && !isRetentionLocked()) {
            status = ArchiveStatus.EXPIRING;
            recordEvent("MARKED_FOR_EXPIRATION", "Archive marked for expiration", null);
        }
    }

    public void expire() {
        if (!isRetentionLocked()) {
            status = ArchiveStatus.EXPIRED;
            recordEvent("ARCHIVE_EXPIRED", "Archive expired", null);
        }
    }

    public void delete() {
        if (canBeDeleted()) {
            status = ArchiveStatus.DELETED;
            recordEvent("ARCHIVE_DELETED", "Archive permanently deleted", createdBy);
        }
    }

    public Long getRetentionDaysRemaining() {
        if (retentionEndDate == null) {
            return null;
        }

        long daysRemaining = java.time.Duration.between(
                LocalDateTime.now(), retentionEndDate
        ).toDays();

        return Math.max(0, daysRemaining);
    }

    public boolean requiresCompliance() {
        return complianceStandards != null && !complianceStandards.isEmpty();
    }

    public ArchiveVersion getCurrentVersion() {
        if (versions == null || versions.isEmpty()) {
            return null;
        }

        return versions.stream()
                .filter(v -> Boolean.TRUE.equals(v.getCurrent()))
                .findFirst()
                .orElse(versions.get(versions.size() - 1));
    }
}
