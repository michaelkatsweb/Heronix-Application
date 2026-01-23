package com.heronix.controller;

import com.heronix.dto.ReportArchival;
import com.heronix.service.ReportArchivalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Report Archival API Controller
 *
 * REST API endpoints for report archival and long-term storage.
 *
 * Endpoints:
 * - POST /api/archival - Create archive
 * - GET /api/archival/{id} - Get archive
 * - POST /api/archival/{id}/start - Start archival
 * - POST /api/archival/{id}/complete - Complete archival
 * - POST /api/archival/{id}/retrieval - Request retrieval
 * - POST /api/archival/{id}/retrieval/{requestId}/complete - Complete retrieval
 * - POST /api/archival/{id}/tier - Transition storage tier
 * - POST /api/archival/{id}/retention - Apply retention policy
 * - POST /api/archival/{id}/retention/lock - Lock retention
 * - POST /api/archival/{id}/compliance-tag - Add compliance tag
 * - POST /api/archival/{id}/legal-hold/enable - Enable legal hold
 * - POST /api/archival/{id}/legal-hold/release - Release legal hold
 * - POST /api/archival/{id}/version - Add version
 * - POST /api/archival/{id}/encrypt - Encrypt archive
 * - POST /api/archival/{id}/backup - Create backup
 * - POST /api/archival/{id}/expiration/check - Check expiration
 * - POST /api/archival/{id}/expire - Expire archive
 * - DELETE /api/archival/{id} - Delete archive
 * - POST /api/archival/expiration/check - Run expiration check
 * - POST /api/archival/storage/optimize - Optimize storage
 * - GET /api/archival/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 93 - Report Archival & Long-term Storage
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/archival")
@RequiredArgsConstructor
@Slf4j
public class ReportArchivalApiController {

    private final ReportArchivalService archivalService;

    /**
     * Create archive
     */
    @PostMapping
    public ResponseEntity<ReportArchival> createArchive(@RequestBody ReportArchival archive) {
        log.info("POST /api/archival - Creating archive for report {}: {}",
                archive.getReportId(), archive.getArchiveName());

        try {
            ReportArchival created = archivalService.createArchive(archive);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating archive", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get archive
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportArchival> getArchive(@PathVariable Long id) {
        log.info("GET /api/archival/{}", id);

        try {
            return archivalService.getArchive(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching archive: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Start archival
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<Map<String, Object>> startArchival(@PathVariable Long id) {
        log.info("POST /api/archival/{}/start", id);

        try {
            archivalService.startArchival(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Archival started");
            response.put("archiveId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Archive not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error starting archival: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Complete archival
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<Map<String, Object>> completeArchival(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/archival/{}/complete", id);

        try {
            Long archivedSizeBytes = ((Number) request.get("archivedSizeBytes")).longValue();
            String checksum = (String) request.get("checksum");
            String storageLocation = (String) request.get("storageLocation");

            archivalService.completeArchival(id, archivedSizeBytes, checksum, storageLocation);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Archival completed");
            response.put("archiveId", id);
            response.put("archivedSizeBytes", archivedSizeBytes);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Archive not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error completing archival: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Request retrieval
     */
    @PostMapping("/{id}/retrieval")
    public ResponseEntity<Map<String, Object>> requestRetrieval(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/archival/{}/retrieval", id);

        try {
            String requestedBy = request.get("requestedBy");
            String tierStr = request.get("tier");
            String reason = request.get("reason");

            ReportArchival.RetrievalTier tier = ReportArchival.RetrievalTier.valueOf(tierStr);

            archivalService.requestRetrieval(id, requestedBy, tier, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Retrieval requested");
            response.put("archiveId", id);
            response.put("tier", tier);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Archive not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            log.error("Invalid archive state for retrieval: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error requesting retrieval: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Complete retrieval
     */
    @PostMapping("/{id}/retrieval/{requestId}/complete")
    public ResponseEntity<Map<String, Object>> completeRetrieval(
            @PathVariable Long id,
            @PathVariable String requestId,
            @RequestBody Map<String, Boolean> request) {
        log.info("POST /api/archival/{}/retrieval/{}/complete", id, requestId);

        try {
            Boolean success = request.get("success");
            if (success == null) {
                success = true;
            }

            archivalService.completeRetrieval(id, requestId, success);

            Map<String, Object> response = new HashMap<>();
            response.put("message", success ? "Retrieval completed" : "Retrieval failed");
            response.put("archiveId", id);
            response.put("requestId", requestId);
            response.put("success", success);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Archive not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error completing retrieval: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Transition storage tier
     */
    @PostMapping("/{id}/tier")
    public ResponseEntity<Map<String, Object>> transitionStorageTier(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/archival/{}/tier", id);

        try {
            String tierStr = request.get("tier");
            String triggeredBy = request.get("triggeredBy");
            String reason = request.get("reason");

            ReportArchival.StorageTier tier = ReportArchival.StorageTier.valueOf(tierStr);

            archivalService.transitionStorageTier(id, tier, triggeredBy, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Storage tier transitioned");
            response.put("archiveId", id);
            response.put("tier", tier);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Archive not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error transitioning storage tier: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Apply retention policy
     */
    @PostMapping("/{id}/retention")
    public ResponseEntity<Map<String, Object>> applyRetentionPolicy(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/archival/{}/retention", id);

        try {
            String policyStr = (String) request.get("policy");
            Integer days = ((Number) request.get("days")).intValue();
            String reason = (String) request.get("reason");

            ReportArchival.RetentionPolicy policy = ReportArchival.RetentionPolicy.valueOf(policyStr);

            archivalService.applyRetentionPolicy(id, policy, days, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Retention policy applied");
            response.put("archiveId", id);
            response.put("policy", policy);
            response.put("days", days);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Archive not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            log.error("Retention policy is locked: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error applying retention policy: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Lock retention
     */
    @PostMapping("/{id}/retention/lock")
    public ResponseEntity<Map<String, Object>> lockRetention(@PathVariable Long id) {
        log.info("POST /api/archival/{}/retention/lock", id);

        try {
            archivalService.lockRetention(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Retention locked");
            response.put("archiveId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Archive not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error locking retention: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Add compliance tag
     */
    @PostMapping("/{id}/compliance-tag")
    public ResponseEntity<Map<String, Object>> addComplianceTag(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/archival/{}/compliance-tag", id);

        try {
            String tagName = request.get("tagName");
            String standard = request.get("standard");
            String category = request.get("category");
            String value = request.get("value");

            archivalService.addComplianceTag(id, tagName, standard, category, value);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Compliance tag added");
            response.put("archiveId", id);
            response.put("tagName", tagName);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Archive not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding compliance tag: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Enable legal hold
     */
    @PostMapping("/{id}/legal-hold/enable")
    public ResponseEntity<Map<String, Object>> enableLegalHold(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/archival/{}/legal-hold/enable", id);

        try {
            String reason = request.get("reason");

            archivalService.enableLegalHold(id, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Legal hold enabled");
            response.put("archiveId", id);
            response.put("reason", reason);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Archive not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error enabling legal hold: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Release legal hold
     */
    @PostMapping("/{id}/legal-hold/release")
    public ResponseEntity<Map<String, Object>> releaseLegalHold(@PathVariable Long id) {
        log.info("POST /api/archival/{}/legal-hold/release", id);

        try {
            archivalService.releaseLegalHold(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Legal hold released");
            response.put("archiveId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Archive not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            log.error("No legal hold active: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error releasing legal hold: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Add version
     */
    @PostMapping("/{id}/version")
    public ResponseEntity<Map<String, Object>> addVersion(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/archival/{}/version", id);

        try {
            Long sizeBytes = ((Number) request.get("sizeBytes")).longValue();
            String storageLocation = (String) request.get("storageLocation");
            String checksum = (String) request.get("checksum");

            archivalService.addVersion(id, sizeBytes, storageLocation, checksum);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Version added");
            response.put("archiveId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Archive not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            log.error("Versioning not enabled: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error adding version: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Encrypt archive
     */
    @PostMapping("/{id}/encrypt")
    public ResponseEntity<Map<String, Object>> encryptArchive(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/archival/{}/encrypt", id);

        try {
            String algorithm = request.get("algorithm");
            String keyId = request.get("keyId");

            archivalService.encryptArchive(id, algorithm, keyId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Archive encrypted");
            response.put("archiveId", id);
            response.put("algorithm", algorithm);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Archive not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error encrypting archive: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create backup
     */
    @PostMapping("/{id}/backup")
    public ResponseEntity<Map<String, Object>> createBackup(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/archival/{}/backup", id);

        try {
            String backupLocation = request.get("backupLocation");

            archivalService.createBackup(id, backupLocation);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Backup created");
            response.put("archiveId", id);
            response.put("backupLocation", backupLocation);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Archive not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating backup: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Check expiration
     */
    @PostMapping("/{id}/expiration/check")
    public ResponseEntity<Map<String, Object>> checkExpiration(@PathVariable Long id) {
        log.info("POST /api/archival/{}/expiration/check", id);

        try {
            archivalService.checkExpiration(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Expiration checked");
            response.put("archiveId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Archive not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error checking expiration: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Expire archive
     */
    @PostMapping("/{id}/expire")
    public ResponseEntity<Map<String, Object>> expireArchive(@PathVariable Long id) {
        log.info("POST /api/archival/{}/expire", id);

        try {
            archivalService.expireArchive(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Archive expired");
            response.put("archiveId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Archive not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            log.error("Cannot expire archive: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error expiring archive: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete archive
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteArchive(@PathVariable Long id) {
        log.info("DELETE /api/archival/{}", id);

        try {
            archivalService.deleteArchive(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Archive deleted");
            response.put("archiveId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Archive not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            log.error("Cannot delete archive: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error deleting archive: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Run expiration check
     */
    @PostMapping("/expiration/check")
    public ResponseEntity<Map<String, Object>> runExpirationCheck() {
        log.info("POST /api/archival/expiration/check");

        try {
            archivalService.runExpirationCheck();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Expiration check completed");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error running expiration check", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Optimize storage
     */
    @PostMapping("/storage/optimize")
    public ResponseEntity<Map<String, Object>> optimizeStorage() {
        log.info("POST /api/archival/storage/optimize");

        try {
            archivalService.optimizeStorage();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Storage optimization completed");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error optimizing storage", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/archival/stats");

        try {
            Map<String, Object> stats = archivalService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching archival statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
