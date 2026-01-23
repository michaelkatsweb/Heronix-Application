package com.heronix.controller;

import com.heronix.dto.ReportBackup;
import com.heronix.service.ReportBackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Backup API Controller
 *
 * REST API endpoints for report backup and recovery.
 *
 * Endpoints:
 * - POST /api/backups - Create backup
 * - GET /api/backups/{id} - Get backup
 * - GET /api/backups - Get all backups
 * - GET /api/backups/type/{type} - Get backups by type
 * - GET /api/backups/recent - Get recent backups
 * - POST /api/backups/{id}/verify - Verify backup
 * - POST /api/backups/{id}/restore - Restore from backup
 * - DELETE /api/backups/{id} - Delete backup
 * - POST /api/backups/cleanup - Cleanup expired backups
 * - GET /api/backups/stats - Get backup statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 74 - Report Backup & Recovery
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/backups")
@RequiredArgsConstructor
@Slf4j
public class ReportBackupApiController {

    private final ReportBackupService backupService;

    @PostMapping
    public ResponseEntity<ReportBackup> createBackup(@RequestBody ReportBackup backup) {
        log.info("POST /api/backups - Creating backup: {}", backup.getBackupName());

        try {
            ReportBackup created = backupService.createBackup(backup);
            return ResponseEntity.ok(created);

        } catch (IllegalArgumentException e) {
            log.error("Invalid backup: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error creating backup", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportBackup> getBackup(@PathVariable Long id) {
        log.info("GET /api/backups/{}", id);

        try {
            return backupService.getBackup(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching backup: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ReportBackup>> getAllBackups() {
        log.info("GET /api/backups");

        try {
            List<ReportBackup> backups = backupService.getAllBackups();
            return ResponseEntity.ok(backups);

        } catch (Exception e) {
            log.error("Error fetching backups", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<ReportBackup>> getBackupsByType(@PathVariable String type) {
        log.info("GET /api/backups/type/{}", type);

        try {
            ReportBackup.BackupType backupType = ReportBackup.BackupType.valueOf(type.toUpperCase());
            List<ReportBackup> backups = backupService.getBackupsByType(backupType);
            return ResponseEntity.ok(backups);

        } catch (IllegalArgumentException e) {
            log.error("Invalid backup type: {}", type);
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error fetching backups by type: {}", type, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ReportBackup>> getRecentBackups(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/backups/recent?limit={}", limit);

        try {
            List<ReportBackup> backups = backupService.getRecentBackups(limit);
            return ResponseEntity.ok(backups);

        } catch (Exception e) {
            log.error("Error fetching recent backups", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<Map<String, Object>> verifyBackup(@PathVariable Long id) {
        log.info("POST /api/backups/{}/verify", id);

        try {
            boolean verified = backupService.verifyBackup(id);

            Map<String, Object> response = new HashMap<>();
            response.put("backupId", id);
            response.put("verified", verified);
            response.put("message", verified ? "Backup verified successfully" : "Backup verification failed");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error verifying backup: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<Map<String, Object>> restoreFromBackup(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/backups/{}/restore", id);

        try {
            String restoredBy = request.get("restoredBy");
            boolean restored = backupService.restoreFromBackup(id, restoredBy);

            Map<String, Object> response = new HashMap<>();
            response.put("backupId", id);
            response.put("restored", restored);
            response.put("message", restored ? "Backup restored successfully" : "Backup restore failed");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error restoring backup: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteBackup(@PathVariable Long id) {
        log.info("DELETE /api/backups/{}", id);

        try {
            backupService.deleteBackup(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Backup deleted successfully");
            response.put("backupId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting backup: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupExpiredBackups() {
        log.info("POST /api/backups/cleanup");

        try {
            int deleted = backupService.cleanupExpiredBackups();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cleanup completed");
            response.put("deletedCount", deleted);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error cleaning up backups", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/backups/stats");

        try {
            Map<String, Object> stats = backupService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching backup statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
