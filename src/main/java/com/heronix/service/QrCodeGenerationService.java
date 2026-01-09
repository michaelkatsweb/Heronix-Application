package com.heronix.service;

import com.heronix.model.domain.Student;
import com.heronix.model.domain.QrCode;
import com.heronix.repository.StudentRepository;
import com.heronix.repository.QrCodeRepository;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * QR Code Generation Service
 *
 * Manages QR code generation, regeneration, and lifecycle for student attendance.
 * Each student receives a unique QR code that can be scanned for attendance verification.
 *
 * Key Responsibilities:
 * - Generate unique QR codes for students
 * - Regenerate codes on demand or when compromised
 * - Manage QR code expiration and rotation
 * - Generate batch QR codes for multiple students
 * - Track QR code usage statistics
 * - Support different QR code formats (URL, encrypted data, etc.)
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Attendance Services Enhancement
 */
@Slf4j
@Service
public class QrCodeGenerationService {

    @Autowired
    private QrCodeRepository qrCodeRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Value("${qr.code.expiry.days:365}")
    private int qrCodeExpiryDays;

    @Value("${qr.code.base.url:https://heronix.edu/attendance/scan}")
    private String qrCodeBaseUrl;

    @Value("${qr.code.auto.rotate.enabled:false}")
    private boolean autoRotateEnabled;

    @Value("${qr.code.rotation.days:90}")
    private int rotationDays;

    // ========================================================================
    // QR CODE GENERATION
    // ========================================================================

    /**
     * Generate a new QR code for a student
     */
    @Transactional
    public QrCode generateQrCode(Long studentId) {
        log.info("Generating QR code for student ID: {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        // Deactivate any existing active QR codes
        List<QrCode> existingCodes = qrCodeRepository.findActiveByStudent(student);
        existingCodes.forEach(code -> {
            code.setActive(false);
            code.setDeactivatedAt(LocalDateTime.now());
            code.setDeactivationReason("Replaced with new QR code");
        });
        qrCodeRepository.saveAll(existingCodes);

        // Generate unique QR code data
        String qrCodeData = generateUniqueQrData(student);
        String qrCodeUrl = String.format("%s?code=%s", qrCodeBaseUrl, qrCodeData);

        LocalDate expiryDate = LocalDate.now().plusDays(qrCodeExpiryDays);
        LocalDate rotationDate = autoRotateEnabled ?
                LocalDate.now().plusDays(rotationDays) : null;

        QrCode qrCode = QrCode.builder()
                .student(student)
                .qrCodeData(qrCodeData)
                .qrCodeUrl(qrCodeUrl)
                .generatedAt(LocalDateTime.now())
                .expiryDate(expiryDate)
                .rotationDate(rotationDate)
                .active(true)
                .scanCount(0)
                .lastScannedAt(null)
                .build();

        qrCode = qrCodeRepository.save(qrCode);
        log.info("Generated QR code {} for student {} (expires: {})",
                qrCode.getQrCodeData(), student.getStudentId(), expiryDate);

        return qrCode;
    }

    /**
     * Regenerate QR code (for security or replacement)
     */
    @Transactional
    public QrCode regenerateQrCode(Long studentId, String reason) {
        log.info("Regenerating QR code for student {}: {}", studentId, reason);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        // Deactivate existing codes with reason
        List<QrCode> existingCodes = qrCodeRepository.findActiveByStudent(student);
        existingCodes.forEach(code -> {
            code.setActive(false);
            code.setDeactivatedAt(LocalDateTime.now());
            code.setDeactivationReason(reason);
        });
        qrCodeRepository.saveAll(existingCodes);

        // Generate new code
        return generateQrCode(studentId);
    }

    /**
     * Generate QR codes for multiple students (batch operation)
     */
    @Transactional
    public List<QrCode> generateBatchQrCodes(List<Long> studentIds) {
        log.info("Generating QR codes for {} students", studentIds.size());

        return studentIds.stream()
                .map(this::generateQrCode)
                .collect(Collectors.toList());
    }

    /**
     * Generate QR codes for all students in a grade level
     */
    @Transactional
    public List<QrCode> generateQrCodesForGradeLevel(String gradeLevel) {
        log.info("Generating QR codes for all students in grade: {}", gradeLevel);

        List<Student> students = studentRepository.findByGradeLevelAndActiveTrue(gradeLevel);
        List<Long> studentIds = students.stream()
                .map(Student::getId)
                .collect(Collectors.toList());

        return generateBatchQrCodes(studentIds);
    }

    /**
     * Generate QR codes for all active students without a valid QR code
     */
    @Transactional
    public List<QrCode> generateMissingQrCodes() {
        log.info("Generating QR codes for students missing valid codes");

        List<Student> studentsWithoutQr = qrCodeRepository.findStudentsWithoutActiveQrCode();
        List<Long> studentIds = studentsWithoutQr.stream()
                .map(Student::getId)
                .collect(Collectors.toList());

        return generateBatchQrCodes(studentIds);
    }

    // ========================================================================
    // QR CODE ROTATION AND EXPIRY
    // ========================================================================

    /**
     * Rotate QR codes that have reached rotation date
     */
    @Transactional
    public List<QrCode> rotateExpiredQrCodes() {
        log.info("Rotating QR codes that have reached rotation date");

        List<QrCode> codesForRotation = qrCodeRepository.findCodesNeedingRotation();

        return codesForRotation.stream()
                .map(code -> regenerateQrCode(code.getStudent().getId(), "Automatic rotation"))
                .collect(Collectors.toList());
    }

    /**
     * Deactivate expired QR codes
     */
    @Transactional
    public int deactivateExpiredQrCodes() {
        log.info("Deactivating expired QR codes");

        List<QrCode> expiredCodes = qrCodeRepository.findExpiredCodes();

        expiredCodes.forEach(code -> {
            code.setActive(false);
            code.setDeactivatedAt(LocalDateTime.now());
            code.setDeactivationReason("QR code expired");
        });

        qrCodeRepository.saveAll(expiredCodes);
        log.info("Deactivated {} expired QR codes", expiredCodes.size());

        return expiredCodes.size();
    }

    /**
     * Extend QR code expiry date
     */
    @Transactional
    public QrCode extendQrCodeExpiry(Long qrCodeId, int additionalDays) {
        log.info("Extending QR code {} expiry by {} days", qrCodeId, additionalDays);

        QrCode qrCode = qrCodeRepository.findById(qrCodeId)
                .orElseThrow(() -> new IllegalArgumentException("QR code not found: " + qrCodeId));

        LocalDate newExpiryDate = qrCode.getExpiryDate().plusDays(additionalDays);
        qrCode.setExpiryDate(newExpiryDate);

        return qrCodeRepository.save(qrCode);
    }

    // ========================================================================
    // QR CODE MANAGEMENT
    // ========================================================================

    /**
     * Get active QR code for a student
     */
    public QrCode getActiveQrCode(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        return qrCodeRepository.findActiveByStudent(student).stream()
                .findFirst()
                .orElse(null);
    }

    /**
     * Get QR code by data
     */
    public QrCode getQrCodeByData(String qrCodeData) {
        return qrCodeRepository.findByQrCodeData(qrCodeData)
                .orElse(null);
    }

    /**
     * Deactivate QR code manually
     */
    @Transactional
    public QrCode deactivateQrCode(Long qrCodeId, String reason) {
        log.info("Manually deactivating QR code {}: {}", qrCodeId, reason);

        QrCode qrCode = qrCodeRepository.findById(qrCodeId)
                .orElseThrow(() -> new IllegalArgumentException("QR code not found: " + qrCodeId));

        qrCode.setActive(false);
        qrCode.setDeactivatedAt(LocalDateTime.now());
        qrCode.setDeactivationReason(reason);

        return qrCodeRepository.save(qrCode);
    }

    /**
     * Increment scan count for QR code
     */
    @Transactional
    public void incrementScanCount(Long qrCodeId) {
        QrCode qrCode = qrCodeRepository.findById(qrCodeId)
                .orElseThrow(() -> new IllegalArgumentException("QR code not found: " + qrCodeId));

        qrCode.setScanCount(qrCode.getScanCount() + 1);
        qrCode.setLastScannedAt(LocalDateTime.now());

        qrCodeRepository.save(qrCode);
    }

    // ========================================================================
    // QR CODE QUERIES
    // ========================================================================

    /**
     * Get QR codes expiring soon (within specified days)
     */
    public List<QrCode> getQrCodesExpiringSoon(int daysAhead) {
        return qrCodeRepository.findCodesExpiringSoon(daysAhead);
    }

    /**
     * Get QR code usage statistics for a student
     */
    public QrCodeStatistics getQrCodeStatistics(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        List<QrCode> allCodes = qrCodeRepository.findByStudent(student);
        QrCode activeCode = qrCodeRepository.findActiveByStudent(student).stream()
                .findFirst()
                .orElse(null);

        int totalScans = allCodes.stream()
                .mapToInt(QrCode::getScanCount)
                .sum();

        int totalGenerated = allCodes.size();
        int activeCount = (int) allCodes.stream()
                .filter(QrCode::getActive)
                .count();

        LocalDateTime lastScan = allCodes.stream()
                .map(QrCode::getLastScannedAt)
                .filter(date -> date != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return QrCodeStatistics.builder()
                .studentId(studentId)
                .studentName(student.getFullName())
                .activeQrCode(activeCode != null ? activeCode.getQrCodeData() : null)
                .expiryDate(activeCode != null ? activeCode.getExpiryDate() : null)
                .totalCodesGenerated(totalGenerated)
                .activeCodesCount(activeCount)
                .totalScans(totalScans)
                .lastScannedAt(lastScan)
                .build();
    }

    /**
     * Get all QR codes for a student (active and inactive)
     */
    public List<QrCode> getAllQrCodesForStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        return qrCodeRepository.findByStudent(student);
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    /**
     * Generate unique QR code data string
     */
    private String generateUniqueQrData(Student student) {
        // Format: STUDENTID-UUID-TIMESTAMP
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        long timestamp = System.currentTimeMillis();
        return String.format("%s-%s-%d", student.getStudentId(), uuid, timestamp);
    }

    /**
     * Validate QR code data format
     */
    public boolean isValidQrCodeFormat(String qrCodeData) {
        if (qrCodeData == null || qrCodeData.isEmpty()) {
            return false;
        }

        // Expected format: STUDENTID-UUID-TIMESTAMP
        String[] parts = qrCodeData.split("-");
        return parts.length == 3;
    }

    // ========================================================================
    // DTO CLASSES
    // ========================================================================

    @Data
    @Builder
    public static class QrCodeStatistics {
        private Long studentId;
        private String studentName;
        private String activeQrCode;
        private LocalDate expiryDate;
        private int totalCodesGenerated;
        private int activeCodesCount;
        private int totalScans;
        private LocalDateTime lastScannedAt;
    }
}
