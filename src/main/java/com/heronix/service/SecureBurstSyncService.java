package com.heronix.service;

import com.heronix.model.domain.StudentToken;
import com.heronix.service.StudentTokenizationService.*;
import com.heronix.service.DeviceAuthenticationService.*;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Secure Burst Sync Service
 *
 * Manages secure, one-way data synchronization from the airgapped SIS (Server 1)
 * to the DMZ server (Server 3) for eventual propagation to external services (Server 2).
 *
 * SECURITY ARCHITECTURE:
 * ----------------------
 * Data Flow: Server 1 (SIS/Airgapped) → Server 3 (DMZ/Sync) → Server 2 (External Portal)
 *
 * This is ONE-WAY only. Server 1 never receives data from external systems.
 * All data is tokenized before leaving Server 1 - no PII ever leaves the airgapped network.
 *
 * SYNC TYPES:
 * - Real-time: Grade changes (< 60 seconds via burst queue)
 * - Batch: Enrollment updates (every 15 minutes)
 * - Scheduled: Daily reconciliation
 *
 * ENCRYPTION:
 * - At Rest: AES-256-GCM
 * - Export Files: AES-256-GCM encrypted
 * - Checksums: SHA-256 for integrity verification
 * - Key Management: Designed for HSM/TPM 2.0 integration
 *
 * IMPORTANT: This service produces EXPORT FILES that must be manually transferred
 * to Server 3 via secure methods (air-gap transfer, dedicated secure link, etc.)
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since January 20, 2026
 */
@Slf4j
@Service
public class SecureBurstSyncService {

    @Autowired
    private StudentTokenizationService tokenizationService;

    @Autowired
    private DeviceAuthenticationService deviceAuthService;

    @Autowired
    private GradebookService gradebookService;

    // Encryption configuration
    private static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final int AES_KEY_SIZE = 256;

    // Sync configuration
    @Value("${heronix.sync.realtime-threshold-seconds:60}")
    private int realtimeThresholdSeconds;

    @Value("${heronix.sync.batch-interval-minutes:15}")
    private int batchIntervalMinutes;

    // Burst queue for real-time changes
    private final Map<String, BurstQueueEntry> burstQueue = new ConcurrentHashMap<>();

    // Sync history
    private final List<SyncBatchRecord> syncHistory = Collections.synchronizedList(new ArrayList<>());

    // Secure random for IV generation
    private final SecureRandom secureRandom = new SecureRandom();

    // ========================================================================
    // REAL-TIME BURST SYNC (Grade Changes < 60 seconds)
    // ========================================================================

    /**
     * Queue a grade change for real-time burst sync.
     * Grade changes should be synced within 60 seconds.
     *
     * @param studentId Internal student ID
     * @param courseId Course ID
     * @param gradeData Grade change data
     */
    public void queueGradeChange(Long studentId, Long courseId, GradeChangeData gradeData) {
        log.info("BURST_SYNC: Queuing grade change for student {} course {}", studentId, courseId);

        // Get student token (tokenize immediately)
        Optional<StudentToken> tokenOpt = tokenizationService.getActiveTokenForStudent(studentId);
        if (tokenOpt.isEmpty()) {
            log.warn("BURST_SYNC: No active token for student {}, generating...", studentId);
            tokenOpt = Optional.of(tokenizationService.generateToken(studentId));
        }

        String studentToken = tokenOpt.get().getTokenValue();

        // Create burst entry (NO PII - only tokenized data)
        BurstQueueEntry entry = BurstQueueEntry.builder()
                .entryId(generateEntryId())
                .studentToken(studentToken)
                .dataType(SyncDataType.GRADE_CHANGE)
                .queuedAt(LocalDateTime.now())
                .priority(SyncPriority.REALTIME)
                .payload(Map.of(
                        "studentToken", studentToken,
                        "courseToken", "CRS-" + courseId.toString().hashCode(), // Tokenize course too
                        "gradePercentage", gradeData.getGradePercentage(),
                        "letterGrade", gradeData.getLetterGrade(),
                        "timestamp", LocalDateTime.now().toString()
                ))
                .checksum(generatePayloadChecksum(gradeData))
                .build();

        burstQueue.put(entry.getEntryId(), entry);

        log.info("BURST_SYNC: Grade change queued. Entry: {}, Token: {}", entry.getEntryId(), studentToken);
    }

    /**
     * Process the burst queue and generate sync package.
     * Should be called frequently (e.g., every 30-60 seconds).
     *
     * @return Sync package ready for secure transfer
     */
    public SyncPackage processBurstQueue() {
        log.info("BURST_SYNC: Processing burst queue. Entries: {}", burstQueue.size());

        if (burstQueue.isEmpty()) {
            return SyncPackage.builder()
                    .packageId(generatePackageId())
                    .packageType(SyncPackageType.REALTIME_BURST)
                    .createdAt(LocalDateTime.now())
                    .entryCount(0)
                    .entries(Collections.emptyList())
                    .build();
        }

        // Extract all entries
        List<BurstQueueEntry> entries = new ArrayList<>(burstQueue.values());
        burstQueue.clear();

        // Create sync package
        String packageId = generatePackageId();
        SyncPackage syncPackage = SyncPackage.builder()
                .packageId(packageId)
                .packageType(SyncPackageType.REALTIME_BURST)
                .createdAt(LocalDateTime.now())
                .entryCount(entries.size())
                .entries(entries)
                .checksum(generatePackageChecksum(entries))
                .build();

        // Record in sync history
        recordSyncBatch(syncPackage);

        log.info("BURST_SYNC: Package {} created with {} entries", packageId, entries.size());

        return syncPackage;
    }

    // ========================================================================
    // BATCH SYNC (Enrollment Updates - Every 15 minutes)
    // ========================================================================

    /**
     * Generate batch enrollment sync package.
     * Contains tokenized enrollment data for all students.
     *
     * @return Encrypted sync package
     */
    public EncryptedSyncPackage generateEnrollmentBatch() {
        log.info("BATCH_SYNC: Generating enrollment batch");

        // Get tokenized student data
        List<TokenizedStudentData> tokenizedData = tokenizationService.generateTokenizedDataForSync();

        // Create batch entries
        List<BurstQueueEntry> entries = tokenizedData.stream()
                .map(data -> BurstQueueEntry.builder()
                        .entryId(generateEntryId())
                        .studentToken(data.getToken())
                        .dataType(SyncDataType.ENROLLMENT)
                        .queuedAt(LocalDateTime.now())
                        .priority(SyncPriority.BATCH)
                        .payload(Map.of(
                                "token", data.getToken(),
                                "gradeLevel", data.getGradeLevel(),
                                "schoolYear", data.getSchoolYear(),
                                "status", data.getEnrollmentStatus(),
                                "checksum", data.getChecksum()
                        ))
                        .checksum(data.getChecksum())
                        .build())
                .toList();

        // Create package
        String packageId = generatePackageId();
        SyncPackage syncPackage = SyncPackage.builder()
                .packageId(packageId)
                .packageType(SyncPackageType.ENROLLMENT_BATCH)
                .createdAt(LocalDateTime.now())
                .entryCount(entries.size())
                .entries(entries)
                .checksum(generatePackageChecksum(entries))
                .build();

        // Record in sync history
        recordSyncBatch(syncPackage);

        // Encrypt the package
        EncryptedSyncPackage encrypted = encryptPackage(syncPackage);

        log.info("BATCH_SYNC: Enrollment batch {} created. {} students, encrypted.",
                packageId, entries.size());

        return encrypted;
    }

    /**
     * Generate CRL sync package for device certificate revocations.
     *
     * @return CRL sync package
     */
    public SyncPackage generateCRLSyncPackage() {
        log.info("SYNC: Generating CRL sync package");

        CertificateRevocationList crl = deviceAuthService.getCertificateRevocationList();

        List<BurstQueueEntry> entries = List.of(BurstQueueEntry.builder()
                .entryId(generateEntryId())
                .studentToken("SYSTEM-CRL")
                .dataType(SyncDataType.CRL_UPDATE)
                .queuedAt(LocalDateTime.now())
                .priority(SyncPriority.HIGH)
                .payload(Map.of(
                        "generatedAt", crl.getGeneratedAt().toString(),
                        "totalRevoked", crl.getTotalRevoked(),
                        "entries", crl.getEntries(),
                        "checksum", crl.getChecksum()
                ))
                .checksum(crl.getChecksum())
                .build());

        String packageId = generatePackageId();
        SyncPackage syncPackage = SyncPackage.builder()
                .packageId(packageId)
                .packageType(SyncPackageType.CRL_UPDATE)
                .createdAt(LocalDateTime.now())
                .entryCount(1)
                .entries(entries)
                .checksum(crl.getChecksum())
                .build();

        recordSyncBatch(syncPackage);

        log.info("SYNC: CRL package {} created. {} revoked certificates.",
                packageId, crl.getTotalRevoked());

        return syncPackage;
    }

    // ========================================================================
    // ENCRYPTION (AES-256-GCM)
    // ========================================================================

    /**
     * Encrypt a sync package using AES-256-GCM.
     *
     * @param syncPackage Package to encrypt
     * @return Encrypted package
     */
    public EncryptedSyncPackage encryptPackage(SyncPackage syncPackage) {
        try {
            // Generate encryption key (in production, get from HSM/TPM)
            SecretKey key = generateEncryptionKey();

            // Generate IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

            // Serialize and encrypt
            String jsonPayload = serializePackage(syncPackage);
            byte[] encryptedData = cipher.doFinal(jsonPayload.getBytes(StandardCharsets.UTF_8));

            // Encode for storage/transfer
            String encryptedBase64 = Base64.getEncoder().encodeToString(encryptedData);
            String ivBase64 = Base64.getEncoder().encodeToString(iv);

            // Key ID for key management (in production, this references HSM key)
            String keyId = "KEY-" + System.currentTimeMillis();

            return EncryptedSyncPackage.builder()
                    .packageId(syncPackage.getPackageId())
                    .encryptedPayload(encryptedBase64)
                    .iv(ivBase64)
                    .keyId(keyId)
                    .algorithm(ENCRYPTION_ALGORITHM)
                    .encryptedAt(LocalDateTime.now())
                    .originalChecksum(syncPackage.getChecksum())
                    .entryCount(syncPackage.getEntryCount())
                    .build();

        } catch (Exception e) {
            log.error("ENCRYPTION: Failed to encrypt package: {}", e.getMessage());
            throw new RuntimeException("Encryption failed", e);
        }
    }

    // ========================================================================
    // SYNC VALIDATION
    // ========================================================================

    /**
     * Validate sync package integrity.
     *
     * @param syncPackage Package to validate
     * @return Validation result
     */
    public SyncValidationResult validatePackage(SyncPackage syncPackage) {
        // Recalculate checksum
        String calculatedChecksum = generatePackageChecksum(syncPackage.getEntries());

        if (!calculatedChecksum.equals(syncPackage.getChecksum())) {
            log.error("SYNC_VALIDATION: Checksum mismatch for package {}. Expected: {}, Got: {}",
                    syncPackage.getPackageId(), syncPackage.getChecksum(), calculatedChecksum);

            return SyncValidationResult.builder()
                    .valid(false)
                    .packageId(syncPackage.getPackageId())
                    .reason("Checksum mismatch - data may be corrupted")
                    .build();
        }

        // Validate each entry
        for (BurstQueueEntry entry : syncPackage.getEntries()) {
            if (entry.getStudentToken() == null || !entry.getStudentToken().startsWith("STU-")) {
                if (!entry.getStudentToken().startsWith("SYSTEM-")) {
                    return SyncValidationResult.builder()
                            .valid(false)
                            .packageId(syncPackage.getPackageId())
                            .reason("Invalid token format in entry: " + entry.getEntryId())
                            .build();
                }
            }
        }

        return SyncValidationResult.builder()
                .valid(true)
                .packageId(syncPackage.getPackageId())
                .entryCount(syncPackage.getEntryCount())
                .checksum(syncPackage.getChecksum())
                .build();
    }

    // ========================================================================
    // SYNC HISTORY & MONITORING
    // ========================================================================

    /**
     * Get sync history.
     *
     * @param limit Maximum records to return
     * @return Recent sync records
     */
    public List<SyncBatchRecord> getSyncHistory(int limit) {
        int start = Math.max(0, syncHistory.size() - limit);
        return new ArrayList<>(syncHistory.subList(start, syncHistory.size()));
    }

    /**
     * Get sync statistics.
     *
     * @return Sync statistics
     */
    public SyncStatistics getSyncStatistics() {
        long realtimeCount = syncHistory.stream()
                .filter(r -> r.getPackageType() == SyncPackageType.REALTIME_BURST)
                .count();

        long batchCount = syncHistory.stream()
                .filter(r -> r.getPackageType() == SyncPackageType.ENROLLMENT_BATCH)
                .count();

        long totalEntries = syncHistory.stream()
                .mapToLong(SyncBatchRecord::getEntryCount)
                .sum();

        return SyncStatistics.builder()
                .totalSyncPackages(syncHistory.size())
                .realtimeBurstCount(realtimeCount)
                .enrollmentBatchCount(batchCount)
                .totalEntriesSynced(totalEntries)
                .pendingBurstEntries(burstQueue.size())
                .lastSyncAt(syncHistory.isEmpty() ? null :
                        syncHistory.get(syncHistory.size() - 1).getCreatedAt())
                .build();
    }

    /**
     * Get current burst queue status.
     *
     * @return Queue status
     */
    public BurstQueueStatus getBurstQueueStatus() {
        return BurstQueueStatus.builder()
                .queuedEntries(burstQueue.size())
                .oldestEntryAt(burstQueue.values().stream()
                        .map(BurstQueueEntry::getQueuedAt)
                        .min(LocalDateTime::compareTo)
                        .orElse(null))
                .realtimeThresholdSeconds(realtimeThresholdSeconds)
                .build();
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private String generateEntryId() {
        return "ENT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generatePackageId() {
        return "PKG-" + System.currentTimeMillis() + "-" +
               UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private String generatePayloadChecksum(GradeChangeData data) {
        String input = data.getGradePercentage() + "|" + data.getLetterGrade() + "|" + System.currentTimeMillis();
        return sha256(input).substring(0, 8).toUpperCase();
    }

    private String generatePackageChecksum(List<BurstQueueEntry> entries) {
        StringBuilder sb = new StringBuilder();
        for (BurstQueueEntry entry : entries) {
            sb.append(entry.getEntryId()).append("|")
              .append(entry.getStudentToken()).append("|")
              .append(entry.getChecksum()).append("|");
        }
        return sha256(sb.toString()).substring(0, 16).toUpperCase();
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 failed", e);
        }
    }

    private SecretKey generateEncryptionKey() throws Exception {
        // In production, retrieve from HSM/TPM
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(AES_KEY_SIZE, secureRandom);
        return keyGen.generateKey();
    }

    private String serializePackage(SyncPackage syncPackage) {
        // Simple JSON-like serialization (in production, use Jackson)
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"packageId\":\"").append(syncPackage.getPackageId()).append("\",");
        sb.append("\"type\":\"").append(syncPackage.getPackageType()).append("\",");
        sb.append("\"createdAt\":\"").append(syncPackage.getCreatedAt()).append("\",");
        sb.append("\"entryCount\":").append(syncPackage.getEntryCount()).append(",");
        sb.append("\"checksum\":\"").append(syncPackage.getChecksum()).append("\",");
        sb.append("\"entries\":[");
        for (int i = 0; i < syncPackage.getEntries().size(); i++) {
            BurstQueueEntry entry = syncPackage.getEntries().get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"id\":\"").append(entry.getEntryId()).append("\",");
            sb.append("\"token\":\"").append(entry.getStudentToken()).append("\",");
            sb.append("\"type\":\"").append(entry.getDataType()).append("\"}");
        }
        sb.append("]}");
        return sb.toString();
    }

    private void recordSyncBatch(SyncPackage syncPackage) {
        syncHistory.add(SyncBatchRecord.builder()
                .packageId(syncPackage.getPackageId())
                .packageType(syncPackage.getPackageType())
                .entryCount(syncPackage.getEntryCount())
                .checksum(syncPackage.getChecksum())
                .createdAt(syncPackage.getCreatedAt())
                .build());

        // Keep history limited
        while (syncHistory.size() > 1000) {
            syncHistory.remove(0);
        }
    }

    // ========================================================================
    // ENUMS AND DTOs
    // ========================================================================

    public enum SyncDataType {
        GRADE_CHANGE,
        ENROLLMENT,
        ATTENDANCE,
        CRL_UPDATE,
        TOKEN_ROTATION
    }

    public enum SyncPriority {
        REALTIME,   // < 60 seconds
        HIGH,       // < 5 minutes
        BATCH,      // 15 minutes
        LOW         // Daily
    }

    public enum SyncPackageType {
        REALTIME_BURST,
        ENROLLMENT_BATCH,
        ATTENDANCE_BATCH,
        CRL_UPDATE,
        FULL_RECONCILIATION
    }

    @Data
    @Builder
    public static class GradeChangeData {
        private Double gradePercentage;
        private String letterGrade;
        private String assignmentType;
        private LocalDateTime changedAt;
    }

    @Data
    @Builder
    public static class BurstQueueEntry {
        private String entryId;
        private String studentToken;
        private SyncDataType dataType;
        private LocalDateTime queuedAt;
        private SyncPriority priority;
        private Map<String, Object> payload;
        private String checksum;
    }

    @Data
    @Builder
    public static class SyncPackage {
        private String packageId;
        private SyncPackageType packageType;
        private LocalDateTime createdAt;
        private int entryCount;
        private List<BurstQueueEntry> entries;
        private String checksum;
    }

    @Data
    @Builder
    public static class EncryptedSyncPackage {
        private String packageId;
        private String encryptedPayload;
        private String iv;
        private String keyId;
        private String algorithm;
        private LocalDateTime encryptedAt;
        private String originalChecksum;
        private int entryCount;
    }

    @Data
    @Builder
    public static class SyncValidationResult {
        private boolean valid;
        private String packageId;
        private int entryCount;
        private String checksum;
        private String reason;
    }

    @Data
    @Builder
    public static class SyncBatchRecord {
        private String packageId;
        private SyncPackageType packageType;
        private int entryCount;
        private String checksum;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    public static class SyncStatistics {
        private int totalSyncPackages;
        private long realtimeBurstCount;
        private long enrollmentBatchCount;
        private long totalEntriesSynced;
        private int pendingBurstEntries;
        private LocalDateTime lastSyncAt;
    }

    @Data
    @Builder
    public static class BurstQueueStatus {
        private int queuedEntries;
        private LocalDateTime oldestEntryAt;
        private int realtimeThresholdSeconds;
    }
}
