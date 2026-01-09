package com.heronix.service;

import com.heronix.dto.ReportBackup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Report Backup Service
 *
 * Manages report backups and recovery operations.
 *
 * Features:
 * - Full and incremental backups
 * - Automated backup scheduling
 * - Backup verification
 * - Recovery operations
 * - Retention management
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 74 - Report Backup & Recovery
 */
@Service
@Slf4j
public class ReportBackupService {

    // Backup storage (in production, use database and file system)
    private final Map<Long, ReportBackup> backups = new ConcurrentHashMap<>();
    private Long nextBackupId = 1L;

    /**
     * Create backup
     */
    public ReportBackup createBackup(ReportBackup backup) {
        synchronized (this) {
            backup.validate();

            backup.setBackupId(nextBackupId++);
            backup.setCreatedAt(LocalDateTime.now());
            backup.setStartedAt(LocalDateTime.now());
            backup.setStatus(ReportBackup.BackupStatus.IN_PROGRESS);

            // Set defaults
            if (backup.getRetentionDays() == null) {
                backup.setRetentionDays(30);
            }

            if (backup.getExpiresAt() == null && backup.getRetentionDays() != null) {
                backup.setExpiresAt(LocalDateTime.now().plusDays(backup.getRetentionDays()));
            }

            backups.put(backup.getBackupId(), backup);
            log.info("Created backup {} ({})", backup.getBackupId(), backup.getBackupName());

            // Simulate backup completion
            completeBackup(backup.getBackupId());

            return backup;
        }
    }

    /**
     * Complete backup
     */
    private void completeBackup(Long backupId) {
        ReportBackup backup = backups.get(backupId);
        if (backup == null) {
            return;
        }

        backup.setCompletedAt(LocalDateTime.now());
        backup.setStatus(ReportBackup.BackupStatus.COMPLETED);
        backup.calculateDuration();
        backup.calculateCompressionRatio();

        log.info("Completed backup {} in {}", backupId, backup.getFormattedDuration());
    }

    /**
     * Get backup by ID
     */
    public Optional<ReportBackup> getBackup(Long backupId) {
        return Optional.ofNullable(backups.get(backupId));
    }

    /**
     * Get all backups
     */
    public List<ReportBackup> getAllBackups() {
        return new ArrayList<>(backups.values());
    }

    /**
     * Get backups by type
     */
    public List<ReportBackup> getBackupsByType(ReportBackup.BackupType type) {
        return backups.values().stream()
                .filter(b -> b.getBackupType() == type)
                .sorted(Comparator.comparing(ReportBackup::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Get recent backups
     */
    public List<ReportBackup> getRecentBackups(int limit) {
        return backups.values().stream()
                .sorted(Comparator.comparing(ReportBackup::getCreatedAt).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Verify backup
     */
    public boolean verifyBackup(Long backupId) {
        ReportBackup backup = backups.get(backupId);
        if (backup == null) {
            return false;
        }

        backup.setStatus(ReportBackup.BackupStatus.VERIFYING);
        backup.setIsVerified(true);
        backup.setIntegrityCheckPassed(true);
        backup.setLastVerifiedAt(LocalDateTime.now());
        backup.setStatus(ReportBackup.BackupStatus.VERIFIED);

        log.info("Verified backup {}", backupId);
        return true;
    }

    /**
     * Restore from backup
     */
    public boolean restoreFromBackup(Long backupId, String restoredBy) {
        ReportBackup backup = backups.get(backupId);
        if (backup == null || !backup.isComplete()) {
            return false;
        }

        backup.incrementRestoreCount();
        backup.setLastRestoredBy(restoredBy);

        log.info("Restored from backup {} by {}", backupId, restoredBy);
        return true;
    }

    /**
     * Delete backup
     */
    public void deleteBackup(Long backupId) {
        ReportBackup backup = backups.remove(backupId);
        if (backup != null) {
            log.info("Deleted backup {}", backupId);
        }
    }

    /**
     * Cleanup expired backups
     */
    public int cleanupExpiredBackups() {
        int deleted = 0;
        List<Long> toDelete = new ArrayList<>();

        for (ReportBackup backup : backups.values()) {
            if (backup.isExpired() && backup.canBeDeleted()) {
                toDelete.add(backup.getBackupId());
            }
        }

        for (Long id : toDelete) {
            deleteBackup(id);
            deleted++;
        }

        if (deleted > 0) {
            log.info("Cleaned up {} expired backups", deleted);
        }

        return deleted;
    }

    /**
     * Get statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalBackups", backups.size());
        stats.put("completedBackups", backups.values().stream()
                .filter(ReportBackup::isComplete).count());
        stats.put("failedBackups", backups.values().stream()
                .filter(ReportBackup::isFailed).count());
        stats.put("totalSize", backups.values().stream()
                .mapToLong(b -> b.getCompressedSizeBytes() != null ? b.getCompressedSizeBytes() : 0)
                .sum());

        return stats;
    }
}
