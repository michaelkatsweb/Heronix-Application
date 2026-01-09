package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * QR Code Entity
 *
 * Represents a unique QR code assigned to a student for attendance scanning.
 * QR codes can be regenerated, expired, and rotated for security.
 *
 * Lifecycle:
 * - Generated for student
 * - Remains active until expiry/rotation/deactivation
 * - Can be regenerated if lost or compromised
 * - Tracks usage statistics (scan count, last scan)
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Attendance Services Enhancement
 */
@Entity
@Table(name = "qr_codes", indexes = {
        @Index(name = "idx_qr_code_data", columnList = "qrCodeData", unique = true),
        @Index(name = "idx_qr_student_active", columnList = "student_id, active"),
        @Index(name = "idx_qr_expiry", columnList = "expiryDate"),
        @Index(name = "idx_qr_rotation", columnList = "rotationDate")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QrCode {

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
    // QR CODE DATA
    // ========================================================================

    @Column(nullable = false, unique = true, length = 200)
    private String qrCodeData; // Unique QR code string

    @Column(nullable = false, length = 500)
    private String qrCodeUrl; // Full URL for QR code scanning

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column
    private LocalDate rotationDate; // When QR should be rotated for security

    // ========================================================================
    // STATUS
    // ========================================================================

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column
    private LocalDateTime deactivatedAt;

    @Column(length = 500)
    private String deactivationReason;

    // ========================================================================
    // USAGE STATISTICS
    // ========================================================================

    @Column(nullable = false)
    @Builder.Default
    private Integer scanCount = 0;

    @Column
    private LocalDateTime lastScannedAt;

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
     * Check if QR code is expired
     */
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    /**
     * Check if QR code needs rotation
     */
    public boolean needsRotation() {
        return rotationDate != null && rotationDate.isBefore(LocalDate.now());
    }

    /**
     * Check if QR code is valid for use
     */
    public boolean isValid() {
        return Boolean.TRUE.equals(active) && !isExpired();
    }

    /**
     * Get days until expiry
     */
    public long getDaysUntilExpiry() {
        if (expiryDate == null) {
            return Long.MAX_VALUE;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    /**
     * Get days since last scan
     */
    public Long getDaysSinceLastScan() {
        if (lastScannedAt == null) {
            return null;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(
                lastScannedAt.toLocalDate(),
                LocalDate.now()
        );
    }
}
