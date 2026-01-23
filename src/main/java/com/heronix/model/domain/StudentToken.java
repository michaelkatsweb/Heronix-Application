package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Student Token Entity
 *
 * Represents an anonymized token for a student that can be safely shared
 * with external systems without exposing PII.
 *
 * TOKEN FORMAT: STU-[6-char-hex] (e.g., STU-7A3F2E)
 * ALGORITHM: SHA-256(student_id + salt + timestamp + school_year)
 * ROTATION: Annual automatic rotation + on-demand capability
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since January 20, 2026
 */
@Entity
@Table(name = "student_tokens", indexes = {
    @Index(name = "idx_token_value", columnList = "token_value", unique = true),
    @Index(name = "idx_token_student", columnList = "student_id"),
    @Index(name = "idx_token_school_year", columnList = "school_year"),
    @Index(name = "idx_token_active", columnList = "active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_value", nullable = false, unique = true, length = 10)
    private String tokenValue;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "school_year", nullable = false, length = 9)
    private String schoolYear;

    @Column(name = "salt", nullable = false, length = 64)
    private String salt;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "deactivated_at")
    private LocalDateTime deactivatedAt;

    @Column(name = "rotation_count")
    @Builder.Default
    private Integer rotationCount = 0;

    @Column(name = "rotation_reason", length = 255)
    private String rotationReason;

    @Column(name = "last_rotated_at")
    private LocalDateTime lastRotatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "deactivated_by", length = 100)
    private String deactivatedBy;

    /**
     * Check if the token is currently valid
     */
    @Transient
    public boolean isValid() {
        return active != null && active &&
               (expiresAt == null || LocalDateTime.now().isBefore(expiresAt));
    }

    /**
     * Check if the token is expired
     */
    @Transient
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Get display status
     */
    @Transient
    public String getDisplayStatus() {
        if (!active) {
            return "DEACTIVATED";
        }
        if (isExpired()) {
            return "EXPIRED";
        }
        if (rotationCount > 0) {
            return "ROTATED";
        }
        return "ACTIVE";
    }
}
