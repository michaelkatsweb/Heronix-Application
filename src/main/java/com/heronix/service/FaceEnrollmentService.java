package com.heronix.service;

import com.heronix.model.domain.Student;
import com.heronix.model.domain.FaceEnrollment;
import com.heronix.repository.StudentRepository;
import com.heronix.repository.FaceEnrollmentRepository;
import com.heronix.service.impl.FacialRecognitionService;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Face Enrollment Service
 *
 * Manages facial recognition enrollment for students with multi-photo capture,
 * quality validation, and liveness detection.
 *
 * Key Responsibilities:
 * - Multi-photo enrollment wizard (3-5 photos from different angles)
 * - Face quality validation (lighting, angle, clarity, size)
 * - Liveness detection to prevent photo spoofing
 * - Face template generation and storage
 * - Re-enrollment for improved recognition
 * - Enrollment status tracking
 * - Batch enrollment for new students
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Attendance Services Enhancement
 */
@Slf4j
@Service
public class FaceEnrollmentService {

    @Autowired
    private FaceEnrollmentRepository faceEnrollmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private FacialRecognitionService facialRecognitionService;

    @Value("${face.enrollment.min.photos:3}")
    private int minPhotosRequired;

    @Value("${face.enrollment.max.photos:5}")
    private int maxPhotosAllowed;

    @Value("${face.quality.min.score:0.70}")
    private double minQualityScore;

    @Value("${face.liveness.enabled:true}")
    private boolean livenessDetectionEnabled;

    @Value("${face.liveness.min.score:0.80}")
    private double minLivenessScore;

    // ========================================================================
    // ENROLLMENT WIZARD
    // ========================================================================

    /**
     * Start a new face enrollment session for a student
     */
    @Transactional
    public FaceEnrollmentSession startEnrollment(Long studentId) {
        log.info("Starting face enrollment for student ID: {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        // Check if student already has enrollment
        FaceEnrollment existing = faceEnrollmentRepository.findActiveByStudent(student)
                .orElse(null);

        if (existing != null) {
            log.info("Student {} already has active enrollment, creating re-enrollment session", studentId);
        }

        FaceEnrollmentSession session = FaceEnrollmentSession.builder()
                .sessionId(generateSessionId())
                .studentId(studentId)
                .studentName(student.getFullName())
                .photosRequired(minPhotosRequired)
                .photosCaptured(0)
                .status("IN_PROGRESS")
                .startedAt(LocalDateTime.now())
                .capturedPhotos(new ArrayList<>())
                .build();

        log.info("Created enrollment session {} for student {}", session.getSessionId(), studentId);

        return session;
    }

    /**
     * Capture and validate a photo during enrollment
     */
    @Transactional
    public PhotoCaptureResult captureEnrollmentPhoto(
            String sessionId,
            byte[] photoData,
            String captureAngle) {

        log.info("Capturing enrollment photo for session {}: angle {}", sessionId, captureAngle);

        // Validate photo quality
        PhotoQualityResult qualityCheck = validatePhotoQuality(photoData);

        if (!qualityCheck.isAcceptable()) {
            log.warn("Photo quality check failed: {}", qualityCheck.getMessage());
            return PhotoCaptureResult.builder()
                    .success(false)
                    .message(qualityCheck.getMessage())
                    .qualityScore(qualityCheck.getQualityScore())
                    .recommendations(qualityCheck.getRecommendations())
                    .build();
        }

        // Perform liveness detection if enabled
        if (livenessDetectionEnabled) {
            LivenessDetectionResult livenessCheck = performLivenessDetection(photoData);

            if (!livenessCheck.isLive()) {
                log.warn("Liveness detection failed: {}", livenessCheck.getMessage());
                return PhotoCaptureResult.builder()
                        .success(false)
                        .message("Liveness detection failed: " + livenessCheck.getMessage())
                        .livenessScore(livenessCheck.getLivenessScore())
                        .build();
            }
        }

        // Photo accepted - would store in session
        log.info("Photo accepted for session {}: quality={}, angle={}",
                sessionId, qualityCheck.getQualityScore(), captureAngle);

        return PhotoCaptureResult.builder()
                .success(true)
                .message("Photo captured successfully")
                .qualityScore(qualityCheck.getQualityScore())
                .captureAngle(captureAngle)
                .photoData(photoData)
                .build();
    }

    /**
     * Complete enrollment and generate face template
     */
    @Transactional
    public FaceEnrollment completeEnrollment(
            String sessionId,
            List<byte[]> capturedPhotos) {

        log.info("Completing enrollment for session {} with {} photos",
                sessionId, capturedPhotos.size());

        if (capturedPhotos.size() < minPhotosRequired) {
            throw new IllegalStateException(
                    String.format("Minimum %d photos required, only %d provided",
                            minPhotosRequired, capturedPhotos.size()));
        }

        // In a real implementation, would:
        // 1. Extract face encoding from each photo
        // 2. Average/combine encodings to create robust template
        // 3. Store template in face enrollment record

        // Mock template generation
        byte[] faceTemplate = generateFaceTemplate(capturedPhotos);

        // Get student from session (would retrieve from actual session storage)
        Long studentId = extractStudentIdFromSession(sessionId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        // Deactivate any existing enrollment
        faceEnrollmentRepository.findActiveByStudent(student)
                .ifPresent(existing -> {
                    existing.setActive(false);
                    existing.setDeactivatedAt(LocalDateTime.now());
                    existing.setDeactivationReason("Replaced with new enrollment");
                    faceEnrollmentRepository.save(existing);
                });

        // Create new enrollment record
        FaceEnrollment enrollment = FaceEnrollment.builder()
                .student(student)
                .faceTemplate(faceTemplate)
                .enrollmentPhotosCount(capturedPhotos.size())
                .enrolledAt(LocalDateTime.now())
                .active(true)
                .qualityScore(calculateAverageQuality(capturedPhotos))
                .provider("FACIAL_RECOGNITION")
                .build();

        enrollment = faceEnrollmentRepository.save(enrollment);

        log.info("Completed face enrollment for student {}: ID {}",
                student.getStudentId(), enrollment.getId());

        return enrollment;
    }

    // ========================================================================
    // PHOTO QUALITY VALIDATION
    // ========================================================================

    /**
     * Validate photo quality for enrollment
     */
    public PhotoQualityResult validatePhotoQuality(byte[] photoData) {
        log.debug("Validating photo quality");

        // Mock implementation - in production would use actual image analysis
        // Checks would include:
        // - Face detection (is there a face?)
        // - Face size (is it large enough?)
        // - Lighting (is it well-lit?)
        // - Blur detection (is it clear?)
        // - Angle detection (is face facing forward?)
        // - Multiple faces (is there only one face?)

        List<String> issues = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        // Mock validation
        double qualityScore = 0.85; // Mock score

        boolean acceptable = qualityScore >= minQualityScore;

        if (qualityScore < 0.70) {
            issues.add("Poor lighting detected");
            recommendations.add("Ensure face is well-lit from front");
        }

        if (!acceptable) {
            issues.add("Quality score below minimum threshold");
        }

        return PhotoQualityResult.builder()
                .acceptable(acceptable)
                .qualityScore(qualityScore)
                .issues(issues)
                .recommendations(recommendations)
                .message(acceptable ? "Photo quality acceptable" : String.join("; ", issues))
                .build();
    }

    // ========================================================================
    // LIVENESS DETECTION
    // ========================================================================

    /**
     * Perform liveness detection to prevent spoofing
     */
    public LivenessDetectionResult performLivenessDetection(byte[] photoData) {
        log.debug("Performing liveness detection");

        // Mock implementation - in production would use:
        // - Texture analysis (detect print patterns)
        // - 3D depth detection
        // - Motion detection (blink, head movement)
        // - Screen detection (moire patterns)

        double livenessScore = 0.90; // Mock score
        boolean isLive = livenessScore >= minLivenessScore;

        return LivenessDetectionResult.builder()
                .live(isLive)
                .livenessScore(livenessScore)
                .method("PASSIVE") // or ACTIVE for blink/movement detection
                .message(isLive ? "Live person detected" : "Possible spoof attempt detected")
                .build();
    }

    // ========================================================================
    // RE-ENROLLMENT
    // ========================================================================

    /**
     * Re-enroll student to improve recognition accuracy
     */
    @Transactional
    public FaceEnrollmentSession startReenrollment(Long studentId, String reason) {
        log.info("Starting re-enrollment for student {}: {}", studentId, reason);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        // Mark existing enrollment for replacement
        FaceEnrollment existing = faceEnrollmentRepository.findActiveByStudent(student)
                .orElseThrow(() -> new IllegalStateException("No active enrollment found for re-enrollment"));

        existing.setReenrollmentRequested(true);
        existing.setReenrollmentReason(reason);
        faceEnrollmentRepository.save(existing);

        return startEnrollment(studentId);
    }

    // ========================================================================
    // BATCH ENROLLMENT
    // ========================================================================

    /**
     * Get students needing enrollment
     */
    public List<Student> getStudentsNeedingEnrollment() {
        return faceEnrollmentRepository.findStudentsWithoutEnrollment();
    }

    /**
     * Get students with low-quality enrollments
     */
    public List<FaceEnrollment> getLowQualityEnrollments(double maxQualityScore) {
        return faceEnrollmentRepository.findByQualityScoreLessThan(maxQualityScore);
    }

    // ========================================================================
    // ENROLLMENT STATUS
    // ========================================================================

    /**
     * Get enrollment status for a student
     */
    public EnrollmentStatus getEnrollmentStatus(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        FaceEnrollment enrollment = faceEnrollmentRepository.findActiveByStudent(student)
                .orElse(null);

        if (enrollment == null) {
            return EnrollmentStatus.builder()
                    .studentId(studentId)
                    .enrolled(false)
                    .message("No active face enrollment found")
                    .build();
        }

        return EnrollmentStatus.builder()
                .studentId(studentId)
                .enrolled(true)
                .enrollmentId(enrollment.getId())
                .enrolledAt(enrollment.getEnrolledAt())
                .photosCount(enrollment.getEnrollmentPhotosCount())
                .qualityScore(enrollment.getQualityScore())
                .provider(enrollment.getProvider())
                .needsReenrollment(enrollment.getReenrollmentRequested() != null &&
                                  enrollment.getReenrollmentRequested())
                .message("Active enrollment found")
                .build();
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    /**
     * Generate unique session ID
     */
    private String generateSessionId() {
        return "FACE-ENROLL-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Extract student ID from session (mock)
     */
    private Long extractStudentIdFromSession(String sessionId) {
        // In production, would retrieve from actual session storage
        return 1L; // Mock
    }

    /**
     * Generate face template from multiple photos
     */
    private byte[] generateFaceTemplate(List<byte[]> photos) {
        log.debug("Generating face template from {} photos", photos.size());

        // Mock implementation - in production would:
        // 1. Extract face encodings from each photo using facial recognition library
        // 2. Average or combine encodings
        // 3. Return template data

        return new byte[128]; // Mock 128-byte template
    }

    /**
     * Calculate average quality score from multiple photos
     */
    private Double calculateAverageQuality(List<byte[]> photos) {
        // Mock implementation
        return 0.85;
    }

    // ========================================================================
    // DTO CLASSES
    // ========================================================================

    @Data
    @Builder
    public static class FaceEnrollmentSession {
        private String sessionId;
        private Long studentId;
        private String studentName;
        private int photosRequired;
        private int photosCaptured;
        private String status; // IN_PROGRESS, COMPLETED, CANCELLED
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private List<PhotoCaptureResult> capturedPhotos;
    }

    @Data
    @Builder
    public static class PhotoCaptureResult {
        private boolean success;
        private String message;
        private Double qualityScore;
        private Double livenessScore;
        private String captureAngle; // FRONT, LEFT, RIGHT, etc.
        private byte[] photoData;
        private List<String> recommendations;
    }

    @Data
    @Builder
    public static class PhotoQualityResult {
        private boolean acceptable;
        private double qualityScore;
        private List<String> issues;
        private List<String> recommendations;
        private String message;
    }

    @Data
    @Builder
    public static class LivenessDetectionResult {
        private boolean live;
        private double livenessScore;
        private String method; // PASSIVE, ACTIVE
        private String message;
    }

    @Data
    @Builder
    public static class EnrollmentStatus {
        private Long studentId;
        private boolean enrolled;
        private Long enrollmentId;
        private LocalDateTime enrolledAt;
        private Integer photosCount;
        private Double qualityScore;
        private String provider;
        private boolean needsReenrollment;
        private String message;
    }
}
