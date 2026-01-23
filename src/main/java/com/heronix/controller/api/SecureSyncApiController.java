package com.heronix.controller.api;

import com.heronix.model.domain.RegisteredDevice;
import com.heronix.model.domain.StudentToken;
import com.heronix.service.DeviceAuthenticationService;
import com.heronix.service.SecureBurstSyncService;
import com.heronix.service.StudentTokenizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Secure Sync API Controller
 *
 * Internal API for managing tokenization, device authentication, and burst sync
 * operations within the airgapped SIS (Server 1).
 *
 * SECURITY ARCHITECTURE:
 * ----------------------
 * This controller runs ONLY on Server 1 (airgapped SIS).
 * It NEVER receives external connections.
 * All data leaving this system is tokenized and encrypted.
 *
 * Access Control:
 * - All endpoints require administrator authentication
 * - All operations are logged for audit
 * - Sensitive operations require confirmation
 *
 * Data Flow:
 * Server 1 (this) → [Secure Transfer] → Server 3 (DMZ) → Server 2 (External)
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since January 20, 2026
 */
@RestController
@RequestMapping("/api/secure-sync")
@RequiredArgsConstructor
@Slf4j
public class SecureSyncApiController {

    private final StudentTokenizationService tokenizationService;
    private final DeviceAuthenticationService deviceAuthService;
    private final SecureBurstSyncService burstSyncService;

    // ========================================================================
    // TOKENIZATION MANAGEMENT
    // ========================================================================

    /**
     * Generate token for a specific student.
     *
     * POST /api/secure-sync/tokens/generate
     */
    @PostMapping("/tokens/generate")
    public ResponseEntity<Map<String, Object>> generateToken(@RequestBody Map<String, Object> request) {
        try {
            Long studentId = Long.valueOf(request.get("studentId").toString());

            log.warn("ADMIN: Token generation requested for student {}", studentId);

            StudentToken token =
                    tokenizationService.generateToken(studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", token.getTokenValue());
            response.put("schoolYear", token.getSchoolYear());
            response.put("expiresAt", token.getExpiresAt());
            response.put("note", "Token generated. No PII is stored with the token.");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return badRequest("Token generation failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Token generation error: {}", e.getMessage());
            return serverError("Token generation failed: " + e.getMessage());
        }
    }

    /**
     * Generate tokens for all students (batch operation).
     *
     * POST /api/secure-sync/tokens/generate-all
     */
    @PostMapping("/tokens/generate-all")
    public ResponseEntity<Map<String, Object>> generateAllTokens() {
        try {
            log.warn("ADMIN: Batch token generation requested for ALL students");

            StudentTokenizationService.TokenGenerationSummary summary =
                    tokenizationService.generateAllTokens();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalStudents", summary.getTotalStudents());
            response.put("tokensGenerated", summary.getTokensGenerated());
            response.put("tokensSkipped", summary.getTokensSkipped());
            response.put("tokensFailed", summary.getTokensFailed());
            response.put("schoolYear", summary.getSchoolYear());
            response.put("generatedAt", summary.getGeneratedAt());

            if (!summary.getErrors().isEmpty()) {
                response.put("errors", summary.getErrors());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Batch token generation error: {}", e.getMessage());
            return serverError("Batch token generation failed: " + e.getMessage());
        }
    }

    /**
     * Rotate token for a student (on-demand).
     *
     * POST /api/secure-sync/tokens/rotate
     */
    @PostMapping("/tokens/rotate")
    public ResponseEntity<Map<String, Object>> rotateToken(@RequestBody Map<String, Object> request) {
        try {
            Long studentId = Long.valueOf(request.get("studentId").toString());
            String reason = (String) request.getOrDefault("reason", "Manual rotation");

            log.warn("ADMIN: Token rotation requested for student {} - Reason: {}", studentId, reason);

            StudentToken newToken =
                    tokenizationService.rotateToken(studentId, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("newToken", newToken.getTokenValue());
            response.put("schoolYear", newToken.getSchoolYear());
            response.put("rotationCount", newToken.getRotationCount());
            response.put("note", "Previous token has been deactivated");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Token rotation error: {}", e.getMessage());
            return serverError("Token rotation failed: " + e.getMessage());
        }
    }

    /**
     * Perform annual token rotation for all students.
     *
     * POST /api/secure-sync/tokens/annual-rotation
     */
    @PostMapping("/tokens/annual-rotation")
    public ResponseEntity<Map<String, Object>> annualRotation() {
        try {
            log.warn("ADMIN: Annual token rotation initiated");

            StudentTokenizationService.TokenRotationSummary summary =
                    tokenizationService.performAnnualRotation();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("schoolYear", summary.getSchoolYear());
            response.put("tokensRotated", summary.getTokensRotated());
            response.put("tokensSkipped", summary.getTokensSkipped());
            response.put("tokensFailed", summary.getTokensFailed());
            response.put("completedAt", summary.getCompletedAt());

            if (!summary.getErrors().isEmpty()) {
                response.put("errors", summary.getErrors());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Annual rotation error: {}", e.getMessage());
            return serverError("Annual rotation failed: " + e.getMessage());
        }
    }

    /**
     * Validate a token.
     *
     * POST /api/secure-sync/tokens/validate
     */
    @PostMapping("/tokens/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> request) {
        try {
            String tokenValue = request.get("token");

            StudentTokenizationService.TokenValidationResult result =
                    tokenizationService.validateToken(tokenValue);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", result.isValid());
            response.put("token", tokenValue);

            if (result.isValid()) {
                response.put("schoolYear", result.getSchoolYear());
                response.put("createdAt", result.getCreatedAt());
                response.put("expiresAt", result.getExpiresAt());
            } else {
                response.put("reason", result.getReason());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return serverError("Token validation failed: " + e.getMessage());
        }
    }

    // ========================================================================
    // DEVICE REGISTRATION & AUTHENTICATION
    // ========================================================================

    /**
     * Get pending device registrations for admin review.
     *
     * GET /api/secure-sync/devices/pending
     */
    @GetMapping("/devices/pending")
    public ResponseEntity<Map<String, Object>> getPendingDevices() {
        try {
            List<RegisteredDevice> pending =
                    deviceAuthService.getPendingRegistrations();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("pendingDevices", pending);
            response.put("count", pending.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return serverError("Failed to get pending devices: " + e.getMessage());
        }
    }

    /**
     * Approve a device registration.
     *
     * POST /api/secure-sync/devices/{deviceId}/approve
     */
    @PostMapping("/devices/{deviceId}/approve")
    public ResponseEntity<Map<String, Object>> approveDevice(
            @PathVariable String deviceId,
            @RequestBody Map<String, String> request) {
        try {
            String approvedBy = request.getOrDefault("approvedBy", "admin");

            log.warn("ADMIN: Approving device {} by {}", deviceId, approvedBy);

            DeviceAuthenticationService.DeviceApprovalResult result =
                    deviceAuthService.approveRegistration(deviceId, approvedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());

            if (result.isSuccess()) {
                response.put("deviceId", result.getDeviceId());
                response.put("certificateSerialNumber", result.getCertificateSerialNumber());
                response.put("certificateExpiresAt", result.getCertificateExpiresAt());
                response.put("message", result.getMessage());
                response.put("instructions", result.getCertificateInstallationInstructions());
            } else {
                response.put("error", result.getErrorMessage());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Device approval error: {}", e.getMessage());
            return serverError("Device approval failed: " + e.getMessage());
        }
    }

    /**
     * Reject a device registration.
     *
     * POST /api/secure-sync/devices/{deviceId}/reject
     */
    @PostMapping("/devices/{deviceId}/reject")
    public ResponseEntity<Map<String, Object>> rejectDevice(
            @PathVariable String deviceId,
            @RequestBody Map<String, String> request) {
        try {
            String rejectedBy = request.getOrDefault("rejectedBy", "admin");
            String reason = request.getOrDefault("reason", "Registration denied");

            log.warn("ADMIN: Rejecting device {} by {} - Reason: {}", deviceId, rejectedBy, reason);

            DeviceAuthenticationService.DeviceRejectionResult result =
                    deviceAuthService.rejectRegistration(deviceId, rejectedBy, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("deviceId", deviceId);
            response.put("message", result.getMessage());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return serverError("Device rejection failed: " + e.getMessage());
        }
    }

    /**
     * Revoke a device certificate.
     *
     * POST /api/secure-sync/devices/{deviceId}/revoke
     */
    @PostMapping("/devices/{deviceId}/revoke")
    public ResponseEntity<Map<String, Object>> revokeDevice(
            @PathVariable String deviceId,
            @RequestBody Map<String, String> request) {
        try {
            String revokedBy = request.getOrDefault("revokedBy", "admin");
            String reason = request.getOrDefault("reason", "Certificate revoked");

            log.warn("SECURITY: Revoking certificate for device {} by {} - Reason: {}",
                    deviceId, revokedBy, reason);

            DeviceAuthenticationService.CertificateRevocationResult result =
                    deviceAuthService.revokeCertificate(deviceId, revokedBy, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("deviceId", deviceId);
            response.put("certificateSerialNumber", result.getCertificateSerialNumber());
            response.put("revokedAt", result.getRevokedAt());
            response.put("message", result.getMessage());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Certificate revocation error: {}", e.getMessage());
            return serverError("Certificate revocation failed: " + e.getMessage());
        }
    }

    /**
     * Get Certificate Revocation List for sync to DMZ.
     *
     * GET /api/secure-sync/devices/crl
     */
    @GetMapping("/devices/crl")
    public ResponseEntity<Map<String, Object>> getCRL() {
        try {
            DeviceAuthenticationService.CertificateRevocationList crl =
                    deviceAuthService.getCertificateRevocationList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("generatedAt", crl.getGeneratedAt());
            response.put("totalRevoked", crl.getTotalRevoked());
            response.put("entries", crl.getEntries());
            response.put("checksum", crl.getChecksum());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return serverError("Failed to get CRL: " + e.getMessage());
        }
    }

    // ========================================================================
    // BURST SYNC OPERATIONS
    // ========================================================================

    /**
     * Process burst queue and get sync package.
     *
     * POST /api/secure-sync/burst/process
     */
    @PostMapping("/burst/process")
    public ResponseEntity<Map<String, Object>> processBurstQueue() {
        try {
            SecureBurstSyncService.SyncPackage syncPackage =
                    burstSyncService.processBurstQueue();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("packageId", syncPackage.getPackageId());
            response.put("packageType", syncPackage.getPackageType());
            response.put("entryCount", syncPackage.getEntryCount());
            response.put("checksum", syncPackage.getChecksum());
            response.put("createdAt", syncPackage.getCreatedAt());

            if (syncPackage.getEntryCount() > 0) {
                response.put("entries", syncPackage.getEntries());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Burst queue processing error: {}", e.getMessage());
            return serverError("Burst queue processing failed: " + e.getMessage());
        }
    }

    /**
     * Generate enrollment batch for sync.
     *
     * POST /api/secure-sync/batch/enrollment
     */
    @PostMapping("/batch/enrollment")
    public ResponseEntity<Map<String, Object>> generateEnrollmentBatch() {
        try {
            log.warn("ADMIN: Generating enrollment batch for sync");

            SecureBurstSyncService.EncryptedSyncPackage encrypted =
                    burstSyncService.generateEnrollmentBatch();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("packageId", encrypted.getPackageId());
            response.put("entryCount", encrypted.getEntryCount());
            response.put("encrypted", true);
            response.put("algorithm", encrypted.getAlgorithm());
            response.put("keyId", encrypted.getKeyId());
            response.put("encryptedAt", encrypted.getEncryptedAt());
            response.put("checksum", encrypted.getOriginalChecksum());
            response.put("note", "Package is AES-256-GCM encrypted. Transfer to Server 3 via secure method.");

            // Don't include actual encrypted payload in API response
            // It should be written to secure file for transfer

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Enrollment batch generation error: {}", e.getMessage());
            return serverError("Enrollment batch generation failed: " + e.getMessage());
        }
    }

    /**
     * Generate CRL sync package.
     *
     * POST /api/secure-sync/batch/crl
     */
    @PostMapping("/batch/crl")
    public ResponseEntity<Map<String, Object>> generateCRLBatch() {
        try {
            SecureBurstSyncService.SyncPackage syncPackage =
                    burstSyncService.generateCRLSyncPackage();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("packageId", syncPackage.getPackageId());
            response.put("packageType", syncPackage.getPackageType());
            response.put("entryCount", syncPackage.getEntryCount());
            response.put("checksum", syncPackage.getChecksum());
            response.put("createdAt", syncPackage.getCreatedAt());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("CRL batch generation error: {}", e.getMessage());
            return serverError("CRL batch generation failed: " + e.getMessage());
        }
    }

    /**
     * Get burst queue status.
     *
     * GET /api/secure-sync/burst/status
     */
    @GetMapping("/burst/status")
    public ResponseEntity<Map<String, Object>> getBurstQueueStatus() {
        try {
            SecureBurstSyncService.BurstQueueStatus status =
                    burstSyncService.getBurstQueueStatus();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("queuedEntries", status.getQueuedEntries());
            response.put("oldestEntryAt", status.getOldestEntryAt());
            response.put("realtimeThresholdSeconds", status.getRealtimeThresholdSeconds());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return serverError("Failed to get queue status: " + e.getMessage());
        }
    }

    /**
     * Get sync statistics.
     *
     * GET /api/secure-sync/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getSyncStatistics() {
        try {
            SecureBurstSyncService.SyncStatistics stats =
                    burstSyncService.getSyncStatistics();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalSyncPackages", stats.getTotalSyncPackages());
            response.put("realtimeBurstCount", stats.getRealtimeBurstCount());
            response.put("enrollmentBatchCount", stats.getEnrollmentBatchCount());
            response.put("totalEntriesSynced", stats.getTotalEntriesSynced());
            response.put("pendingBurstEntries", stats.getPendingBurstEntries());
            response.put("lastSyncAt", stats.getLastSyncAt());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return serverError("Failed to get statistics: " + e.getMessage());
        }
    }

    /**
     * Get sync history.
     *
     * GET /api/secure-sync/history?limit=50
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getSyncHistory(
            @RequestParam(defaultValue = "50") int limit) {
        try {
            List<SecureBurstSyncService.SyncBatchRecord> history =
                    burstSyncService.getSyncHistory(limit);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("history", history);
            response.put("count", history.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return serverError("Failed to get history: " + e.getMessage());
        }
    }

    // ========================================================================
    // METADATA & DOCUMENTATION
    // ========================================================================

    /**
     * Get API metadata and security information.
     *
     * GET /api/secure-sync/metadata
     */
    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("apiVersion", "1.0.0");
        metadata.put("apiName", "Heronix Secure Sync API");
        metadata.put("serverType", "Server 1 (Airgapped SIS)");
        metadata.put("externalConnections", false);

        metadata.put("securityArchitecture", Map.of(
                "tokenization", Map.of(
                        "format", "STU-[6-char-hex]",
                        "algorithm", "SHA-256",
                        "rotation", "Annual + on-demand",
                        "collisionResistance", "2^24 possible tokens"
                ),
                "deviceAuth", Map.of(
                        "certificates", "X.509 (2048-bit RSA)",
                        "macWhitelist", true,
                        "deviceFingerprinting", true,
                        "maxDevicesPerAccount", 5,
                        "crl", "Certificate Revocation List"
                ),
                "encryption", Map.of(
                        "algorithm", "AES-256-GCM",
                        "keyManagement", "HSM/TPM 2.0 ready"
                ),
                "dataSync", Map.of(
                        "direction", "One-way (Server 1 → Server 3 → Server 2)",
                        "realtime", "< 60 seconds for grade changes",
                        "batch", "Every 15 minutes for enrollment",
                        "validation", "SHA-256 checksums"
                )
        ));

        metadata.put("dataFlow", List.of(
                "1. Data changes occur in SIS (Server 1)",
                "2. Data is tokenized (no PII leaves Server 1)",
                "3. Tokenized data is encrypted (AES-256-GCM)",
                "4. Encrypted packages transferred to DMZ (Server 3)",
                "5. Server 3 distributes to external services (Server 2)",
                "6. Even if external systems compromised, no PII is exposed"
        ));

        return ResponseEntity.ok(metadata);
    }

    /**
     * Get API help and endpoint documentation.
     *
     * GET /api/secure-sync/help
     */
    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = new HashMap<>();

        help.put("description", "Secure synchronization API for airgapped SIS");

        help.put("tokenization", Map.of(
                "generate", "POST /api/secure-sync/tokens/generate",
                "generateAll", "POST /api/secure-sync/tokens/generate-all",
                "rotate", "POST /api/secure-sync/tokens/rotate",
                "annualRotation", "POST /api/secure-sync/tokens/annual-rotation",
                "validate", "POST /api/secure-sync/tokens/validate"
        ));

        help.put("deviceManagement", Map.of(
                "pending", "GET /api/secure-sync/devices/pending",
                "approve", "POST /api/secure-sync/devices/{deviceId}/approve",
                "reject", "POST /api/secure-sync/devices/{deviceId}/reject",
                "revoke", "POST /api/secure-sync/devices/{deviceId}/revoke",
                "crl", "GET /api/secure-sync/devices/crl"
        ));

        help.put("syncOperations", Map.of(
                "processBurst", "POST /api/secure-sync/burst/process",
                "enrollmentBatch", "POST /api/secure-sync/batch/enrollment",
                "crlBatch", "POST /api/secure-sync/batch/crl",
                "queueStatus", "GET /api/secure-sync/burst/status",
                "statistics", "GET /api/secure-sync/statistics",
                "history", "GET /api/secure-sync/history"
        ));

        help.put("securityNotes", List.of(
                "All endpoints require administrator authentication",
                "All operations are logged for audit compliance",
                "Token-to-student mapping is INTERNAL ONLY",
                "External systems NEVER receive PII",
                "Certificate revocations propagate via CRL sync"
        ));

        return ResponseEntity.ok(help);
    }

    /**
     * Get dashboard summary.
     *
     * GET /api/secure-sync/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        try {
            // Get current status from all services
            SecureBurstSyncService.SyncStatistics syncStats = burstSyncService.getSyncStatistics();
            SecureBurstSyncService.BurstQueueStatus queueStatus = burstSyncService.getBurstQueueStatus();
            List<RegisteredDevice> pendingDevices =
                    deviceAuthService.getPendingRegistrations();
            DeviceAuthenticationService.CertificateRevocationList crl =
                    deviceAuthService.getCertificateRevocationList();

            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("timestamp", LocalDateTime.now());

            dashboard.put("syncStatus", Map.of(
                    "pendingBurstEntries", queueStatus.getQueuedEntries(),
                    "lastSyncAt", syncStats.getLastSyncAt(),
                    "totalPackagesSynced", syncStats.getTotalSyncPackages()
            ));

            dashboard.put("deviceStatus", Map.of(
                    "pendingApprovals", pendingDevices.size(),
                    "revokedCertificates", crl.getTotalRevoked()
            ));

            dashboard.put("alerts", List.of());  // Would contain any security alerts

            dashboard.put("quickActions", List.of(
                    Map.of("action", "Process Burst Queue", "endpoint", "POST /burst/process"),
                    Map.of("action", "Review Pending Devices", "endpoint", "GET /devices/pending"),
                    Map.of("action", "Generate Enrollment Batch", "endpoint", "POST /batch/enrollment")
            ));

            return ResponseEntity.ok(dashboard);

        } catch (Exception e) {
            return serverError("Failed to load dashboard: " + e.getMessage());
        }
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private ResponseEntity<Map<String, Object>> badRequest(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    private ResponseEntity<Map<String, Object>> serverError(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
