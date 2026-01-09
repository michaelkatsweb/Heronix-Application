package com.heronix.service;

import com.heronix.dto.ReportArchival;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Report Archival Service
 *
 * Manages long-term archival and storage of reports.
 *
 * Features:
 * - Archive lifecycle management
 * - Tiered storage transitions
 * - Retention policy enforcement
 * - Retrieval management
 * - Compliance tracking
 * - Version management
 * - Storage optimization
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 93 - Report Archival & Long-term Storage
 */
@Service
@Slf4j
public class ReportArchivalService {

    private final Map<Long, ReportArchival> archives = new ConcurrentHashMap<>();
    private Long nextArchiveId = 1L;

    /**
     * Create archive
     */
    public ReportArchival createArchive(ReportArchival archive) {
        synchronized (this) {
            archive.setArchiveId(nextArchiveId++);
            archive.setCreatedAt(LocalDateTime.now());
            archive.setStatus(ReportArchival.ArchiveStatus.PENDING);
            archive.setAccessCount(0);
            archive.setTotalRetrievals(0);

            // Set defaults
            if (archive.getStorageTier() == null) {
                archive.setStorageTier(ReportArchival.StorageTier.WARM);
            }

            if (archive.getStorageProvider() == null) {
                archive.setStorageProvider("Local");
            }

            if (archive.getCompressionAlgorithm() == null) {
                archive.setCompressionAlgorithm("GZIP");
            }

            if (archive.getEncrypted() == null) {
                archive.setEncrypted(false);
            }

            if (archive.getRetentionPolicy() == null) {
                archive.setRetentionPolicy(ReportArchival.RetentionPolicy.MEDIUM_TERM);
            }

            if (archive.getRetentionDays() == null) {
                archive.setRetentionDays(1825); // 5 years
            }

            if (archive.getRetentionLocked() == null) {
                archive.setRetentionLocked(false);
            }

            if (archive.getLegalHold() == null) {
                archive.setLegalHold(false);
            }

            if (archive.getArchiveFormat() == null) {
                archive.setArchiveFormat("ZIP");
            }

            if (archive.getChecksumAlgorithm() == null) {
                archive.setChecksumAlgorithm("SHA-256");
            }

            if (archive.getVersion() == null) {
                archive.setVersion(1);
            }

            if (archive.getVersioningEnabled() == null) {
                archive.setVersioningEnabled(true);
            }

            if (archive.getRetrievalTier() == null) {
                archive.setRetrievalTier(ReportArchival.RetrievalTier.STANDARD);
            }

            if (archive.getCurrency() == null) {
                archive.setCurrency("USD");
            }

            if (archive.getBackedUp() == null) {
                archive.setBackedUp(false);
            }

            if (archive.getBackupCopies() == null) {
                archive.setBackupCopies(0);
            }

            if (archive.getConfigurationLocked() == null) {
                archive.setConfigurationLocked(false);
            }

            // Initialize collections
            if (archive.getComplianceStandards() == null) {
                archive.setComplianceStandards(new ArrayList<>());
            }

            if (archive.getComplianceTags() == null) {
                archive.setComplianceTags(new ArrayList<>());
            }

            if (archive.getMetadata() == null) {
                archive.setMetadata(new HashMap<>());
            }

            if (archive.getTags() == null) {
                archive.setTags(new ArrayList<>());
            }

            if (archive.getVersions() == null) {
                archive.setVersions(new ArrayList<>());
            }

            if (archive.getRetrievalRequests() == null) {
                archive.setRetrievalRequests(new ArrayList<>());
            }

            if (archive.getLifecycleTransitions() == null) {
                archive.setLifecycleTransitions(new ArrayList<>());
            }

            if (archive.getAuditTrail() == null) {
                archive.setAuditTrail(new ArrayList<>());
            }

            if (archive.getConfiguration() == null) {
                archive.setConfiguration(new HashMap<>());
            }

            archives.put(archive.getArchiveId(), archive);

            log.info("Created archive {} for report {}: {}",
                    archive.getArchiveId(), archive.getReportId(), archive.getArchiveName());

            return archive;
        }
    }

    /**
     * Get archive
     */
    public Optional<ReportArchival> getArchive(Long archiveId) {
        return Optional.ofNullable(archives.get(archiveId));
    }

    /**
     * Start archival process
     */
    public void startArchival(Long archiveId) {
        ReportArchival archive = archives.get(archiveId);
        if (archive == null) {
            throw new IllegalArgumentException("Archive not found: " + archiveId);
        }

        archive.archive();
        archive.recordEvent("ARCHIVAL_STARTED", "Archival process started", archive.getCreatedBy());

        log.info("Started archival for archive {}", archiveId);
    }

    /**
     * Complete archival
     */
    public void completeArchival(Long archiveId, Long archivedSizeBytes, String checksum,
                                 String storageLocation) {
        ReportArchival archive = archives.get(archiveId);
        if (archive == null) {
            throw new IllegalArgumentException("Archive not found: " + archiveId);
        }

        archive.setArchivedSizeBytes(archivedSizeBytes);
        archive.setChecksum(checksum);
        archive.setStorageLocation(storageLocation);

        archive.completeArchival();
        archive.calculateStorageMetrics();

        // Apply retention policy
        if (archive.getRetentionDays() != null) {
            archive.applyRetentionPolicy(archive.getRetentionPolicy(), archive.getRetentionDays());
        }

        log.info("Completed archival for archive {}: {} bytes compressed to {} bytes (ratio: {:.2f})",
                archiveId, archive.getOriginalSizeBytes(), archivedSizeBytes,
                archive.getCompressionRatio());
    }

    /**
     * Request retrieval
     */
    public void requestRetrieval(Long archiveId, String requestedBy,
                                 ReportArchival.RetrievalTier tier, String reason) {
        ReportArchival archive = archives.get(archiveId);
        if (archive == null) {
            throw new IllegalArgumentException("Archive not found: " + archiveId);
        }

        if (archive.getStatus() != ReportArchival.ArchiveStatus.ARCHIVED) {
            throw new IllegalStateException("Archive must be in ARCHIVED status for retrieval");
        }

        archive.startRetrieval(requestedBy, tier);

        log.info("Retrieval requested for archive {} by {} (tier: {})",
                archiveId, requestedBy, tier);
    }

    /**
     * Complete retrieval
     */
    public void completeRetrieval(Long archiveId, String requestId, boolean success) {
        ReportArchival archive = archives.get(archiveId);
        if (archive == null) {
            throw new IllegalArgumentException("Archive not found: " + archiveId);
        }

        archive.completeRetrieval(requestId, success);

        log.info("Retrieval {} for archive {}: {}",
                success ? "completed" : "failed", archiveId, requestId);
    }

    /**
     * Transition storage tier
     */
    public void transitionStorageTier(Long archiveId, ReportArchival.StorageTier newTier,
                                      String triggeredBy, String reason) {
        ReportArchival archive = archives.get(archiveId);
        if (archive == null) {
            throw new IllegalArgumentException("Archive not found: " + archiveId);
        }

        ReportArchival.StorageTier oldTier = archive.getStorageTier();
        archive.transitionStorageTier(newTier, triggeredBy, reason);

        log.info("Transitioned archive {} from {} to {} (triggered by: {})",
                archiveId, oldTier, newTier, triggeredBy);
    }

    /**
     * Apply retention policy
     */
    public void applyRetentionPolicy(Long archiveId, ReportArchival.RetentionPolicy policy,
                                     Integer days, String reason) {
        ReportArchival archive = archives.get(archiveId);
        if (archive == null) {
            throw new IllegalArgumentException("Archive not found: " + archiveId);
        }

        if (Boolean.TRUE.equals(archive.getRetentionLocked())) {
            throw new IllegalStateException("Retention policy is locked");
        }

        archive.setRetentionReason(reason);
        archive.applyRetentionPolicy(policy, days);

        log.info("Applied retention policy to archive {}: {} ({} days)",
                archiveId, policy, days);
    }

    /**
     * Lock retention
     */
    public void lockRetention(Long archiveId) {
        ReportArchival archive = archives.get(archiveId);
        if (archive == null) {
            throw new IllegalArgumentException("Archive not found: " + archiveId);
        }

        archive.setRetentionLocked(true);
        archive.recordEvent("RETENTION_LOCKED", "Retention policy locked", archive.getCreatedBy());

        log.info("Locked retention for archive {}", archiveId);
    }

    /**
     * Add compliance tag
     */
    public void addComplianceTag(Long archiveId, String tagName, String standard,
                                String category, String value) {
        ReportArchival archive = archives.get(archiveId);
        if (archive == null) {
            throw new IllegalArgumentException("Archive not found: " + archiveId);
        }

        archive.addComplianceTag(tagName, standard, category, value);

        log.info("Added compliance tag to archive {}: {} ({})", archiveId, tagName, standard);
    }

    /**
     * Enable legal hold
     */
    public void enableLegalHold(Long archiveId, String reason) {
        ReportArchival archive = archives.get(archiveId);
        if (archive == null) {
            throw new IllegalArgumentException("Archive not found: " + archiveId);
        }

        archive.enableLegalHold(reason);

        log.info("Enabled legal hold for archive {}: {}", archiveId, reason);
    }

    /**
     * Release legal hold
     */
    public void releaseLegalHold(Long archiveId) {
        ReportArchival archive = archives.get(archiveId);
        if (archive == null) {
            throw new IllegalArgumentException("Archive not found: " + archiveId);
        }

        if (!Boolean.TRUE.equals(archive.getLegalHold())) {
            throw new IllegalStateException("No legal hold is active");
        }

        archive.releaseLegalHold();

        log.info("Released legal hold for archive {}", archiveId);
    }

    /**
     * Add version
     */
    public void addVersion(Long archiveId, Long sizeBytes, String storageLocation, String checksum) {
        ReportArchival archive = archives.get(archiveId);
        if (archive == null) {
            throw new IllegalArgumentException("Archive not found: " + archiveId);
        }

        if (!Boolean.TRUE.equals(archive.getVersioningEnabled())) {
            throw new IllegalStateException("Versioning is not enabled");
        }

        archive.addVersion(sizeBytes, storageLocation, checksum);

        log.info("Added version {} to archive {}", archive.getVersion(), archiveId);
    }

    /**
     * Encrypt archive
     */
    public void encryptArchive(Long archiveId, String algorithm, String keyId) {
        ReportArchival archive = archives.get(archiveId);
        if (archive == null) {
            throw new IllegalArgumentException("Archive not found: " + archiveId);
        }

        archive.setEncrypted(true);
        archive.setEncryptionAlgorithm(algorithm);
        archive.setEncryptionKeyId(keyId);
        archive.setEncryptedAt(LocalDateTime.now());

        archive.recordEvent("ARCHIVE_ENCRYPTED", "Archive encrypted with " + algorithm,
                archive.getCreatedBy());

        log.info("Encrypted archive {} with {} (key: {})", archiveId, algorithm, keyId);
    }

    /**
     * Create backup
     */
    public void createBackup(Long archiveId, String backupLocation) {
        ReportArchival archive = archives.get(archiveId);
        if (archive == null) {
            throw new IllegalArgumentException("Archive not found: " + archiveId);
        }

        archive.setBackedUp(true);
        archive.setBackupLocation(backupLocation);
        archive.setLastBackupAt(LocalDateTime.now());
        archive.setBackupCopies((archive.getBackupCopies() != null ? archive.getBackupCopies() : 0) + 1);

        archive.recordEvent("BACKUP_CREATED", "Backup created at " + backupLocation,
                archive.getCreatedBy());

        log.info("Created backup for archive {} at {}", archiveId, backupLocation);
    }

    /**
     * Check expiration
     */
    public void checkExpiration(Long archiveId) {
        ReportArchival archive = archives.get(archiveId);
        if (archive == null) {
            throw new IllegalArgumentException("Archive not found: " + archiveId);
        }

        if (archive.isExpired() && !archive.isRetentionLocked()) {
            archive.markForExpiration();
            log.info("Archive {} marked for expiration", archiveId);
        }
    }

    /**
     * Expire archive
     */
    public void expireArchive(Long archiveId) {
        ReportArchival archive = archives.get(archiveId);
        if (archive == null) {
            throw new IllegalArgumentException("Archive not found: " + archiveId);
        }

        if (archive.isRetentionLocked()) {
            throw new IllegalStateException("Cannot expire archive with retention lock or legal hold");
        }

        archive.expire();

        log.info("Expired archive {}", archiveId);
    }

    /**
     * Delete archive
     */
    public void deleteArchive(Long archiveId) {
        ReportArchival archive = archives.get(archiveId);
        if (archive == null) {
            throw new IllegalArgumentException("Archive not found: " + archiveId);
        }

        if (!archive.canBeDeleted()) {
            throw new IllegalStateException(
                    "Cannot delete archive: retention locked or not expired");
        }

        archive.delete();
        archives.remove(archiveId);

        log.info("Deleted archive {}", archiveId);
    }

    /**
     * Run expiration check on all archives
     */
    public void runExpirationCheck() {
        int expiredCount = 0;
        int markedCount = 0;

        for (ReportArchival archive : archives.values()) {
            if (archive.isExpired() && !archive.isRetentionLocked()) {
                if (archive.getStatus() == ReportArchival.ArchiveStatus.ARCHIVED) {
                    archive.markForExpiration();
                    markedCount++;
                } else if (archive.getStatus() == ReportArchival.ArchiveStatus.EXPIRING) {
                    archive.expire();
                    expiredCount++;
                }
            }
        }

        log.info("Expiration check completed: {} marked for expiration, {} expired",
                markedCount, expiredCount);
    }

    /**
     * Optimize storage
     */
    public void optimizeStorage() {
        int transitioned = 0;

        for (ReportArchival archive : archives.values()) {
            if (archive.getStatus() != ReportArchival.ArchiveStatus.ARCHIVED) {
                continue;
            }

            LocalDateTime archivedAt = archive.getArchivedAt();
            if (archivedAt == null) {
                continue;
            }

            long daysArchived = java.time.Duration.between(archivedAt, LocalDateTime.now()).toDays();

            // Transition based on age
            if (daysArchived > 180 && archive.getStorageTier() == ReportArchival.StorageTier.WARM) {
                // Move to COLD after 6 months
                archive.transitionStorageTier(ReportArchival.StorageTier.COLD,
                        "AUTOMATIC", "Automatic transition based on age");
                transitioned++;

            } else if (daysArchived > 365 && archive.getStorageTier() == ReportArchival.StorageTier.COLD) {
                // Move to GLACIER after 1 year
                archive.transitionStorageTier(ReportArchival.StorageTier.GLACIER,
                        "AUTOMATIC", "Automatic transition based on age");
                transitioned++;

            } else if (daysArchived > 1825 &&
                       archive.getStorageTier() == ReportArchival.StorageTier.GLACIER &&
                       archive.getRetentionPolicy() == ReportArchival.RetentionPolicy.LONG_TERM) {
                // Move to DEEP_ARCHIVE after 5 years for long-term retention
                archive.transitionStorageTier(ReportArchival.StorageTier.DEEP_ARCHIVE,
                        "AUTOMATIC", "Automatic transition based on age");
                transitioned++;
            }
        }

        log.info("Storage optimization completed: {} archives transitioned", transitioned);
    }

    /**
     * Get statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalArchives", archives.size());

        long archivedCount = archives.values().stream()
                .filter(a -> a.getStatus() == ReportArchival.ArchiveStatus.ARCHIVED)
                .count();

        long expiredCount = archives.values().stream()
                .filter(a -> a.getStatus() == ReportArchival.ArchiveStatus.EXPIRED)
                .count();

        long legalHoldCount = archives.values().stream()
                .filter(a -> Boolean.TRUE.equals(a.getLegalHold()))
                .count();

        long totalSizeBytes = archives.values().stream()
                .mapToLong(a -> a.getOriginalSizeBytes() != null ? a.getOriginalSizeBytes() : 0L)
                .sum();

        long totalArchivedBytes = archives.values().stream()
                .mapToLong(a -> a.getArchivedSizeBytes() != null ? a.getArchivedSizeBytes() : 0L)
                .sum();

        long totalRetrievals = archives.values().stream()
                .mapToLong(a -> a.getTotalRetrievals() != null ? a.getTotalRetrievals() : 0L)
                .sum();

        Map<ReportArchival.StorageTier, Long> tierCounts = new HashMap<>();
        for (ReportArchival.StorageTier tier : ReportArchival.StorageTier.values()) {
            long count = archives.values().stream()
                    .filter(a -> a.getStorageTier() == tier)
                    .count();
            tierCounts.put(tier, count);
        }

        stats.put("archivedCount", archivedCount);
        stats.put("expiredCount", expiredCount);
        stats.put("legalHoldCount", legalHoldCount);
        stats.put("totalSizeBytes", totalSizeBytes);
        stats.put("totalArchivedBytes", totalArchivedBytes);
        stats.put("totalRetrievals", totalRetrievals);
        stats.put("storageTierDistribution", tierCounts);

        if (totalSizeBytes > 0) {
            double avgCompressionRatio = (double) totalSizeBytes / totalArchivedBytes;
            stats.put("averageCompressionRatio", avgCompressionRatio);
        }

        return stats;
    }
}
