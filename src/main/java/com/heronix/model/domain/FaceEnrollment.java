package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Face Enrollment Entity
 *
 * Stores facial recognition enrollment data for students.
 * Contains face templates and enrollment metadata for attendance verification.
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Attendance Services Enhancement
 */
@Entity
@Table(name = "face_enrollments", indexes = {
        @Index(name = "idx_face_student_active", columnList = "student_id, active"),
        @Index(name = "idx_face_quality", columnList = "qualityScore"),
        @Index(name = "idx_face_enrolled", columnList = "enrolledAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaceEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========================================================================
    // STUDENT RELATIONSHIP
    // ========================================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @ToString.Exclude
    private Student student;

    // ========================================================================
    // FACE TEMPLATE DATA
    // ========================================================================

    @Lob
    @Column(nullable = false, columnDefinition = "BLOB")
    private byte[] faceTemplate; // Encoded face data from facial recognition system

    @Column
    private Integer enrollmentPhotosCount; // Number of photos used for enrollment

    @Column
    private Double qualityScore; // Overall quality score of enrollment (0.0 - 1.0)

    @Column(length = 50)
    private String provider; // OPENCV, AWS, AZURE, GOOGLE, MOCK

    // ========================================================================
    // ENROLLMENT METADATA
    // ========================================================================

    @Column(nullable = false)
    private LocalDateTime enrolledAt;

    @Column
    @Builder.Default
    private Boolean active = true;

    @Column
    private LocalDateTime deactivatedAt;

    @Column(length = 500)
    private String deactivationReason;

    // ========================================================================
    // RE-ENROLLMENT TRACKING
    // ========================================================================

    @Column
    private Boolean reenrollmentRequested;

    @Column(length = 500)
    private String reenrollmentReason;

    @Column
    private LocalDateTime reenrollmentRequestedAt;

    // ========================================================================
    // USAGE STATISTICS
    // ========================================================================

    @Column
    @Builder.Default
    private Integer successfulMatchCount = 0;

    @Column
    @Builder.Default
    private Integer failedMatchCount = 0;

    @Column
    private LocalDateTime lastMatchedAt;

    @Column
    private Double averageConfidenceScore; // Average match confidence over time

    // ========================================================================
    // AUDIT FIELDS
    // ========================================================================

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ========================================================================
    // BUSINESS METHODS
    // ========================================================================

    /**
     * Check if enrollment quality is acceptable
     */
    public boolean hasAcceptableQuality(double minThreshold) {
        return qualityScore != null && qualityScore >= minThreshold;
    }

    /**
     * Increment successful match counter
     */
    public void recordSuccessfulMatch(double confidenceScore) {
        this.successfulMatchCount++;
        this.lastMatchedAt = LocalDateTime.now();
        updateAverageConfidence(confidenceScore);
    }

    /**
     * Increment failed match counter
     */
    public void recordFailedMatch() {
        this.failedMatchCount++;
    }

    /**
     * Update running average of confidence scores
     */
    private void updateAverageConfidence(double newScore) {
        if (averageConfidenceScore == null) {
            averageConfidenceScore = newScore;
        } else {
            // Running average
            int totalMatches = successfulMatchCount != null ? successfulMatchCount : 0;
            averageConfidenceScore = ((averageConfidenceScore * (totalMatches - 1)) + newScore) / totalMatches;
        }
    }

    /**
     * Get match success rate
     */
    public double getMatchSuccessRate() {
        int total = (successfulMatchCount != null ? successfulMatchCount : 0) +
                   (failedMatchCount != null ? failedMatchCount : 0);
        if (total == 0) return 0.0;
        return (double) (successfulMatchCount != null ? successfulMatchCount : 0) / total;
    }
}
