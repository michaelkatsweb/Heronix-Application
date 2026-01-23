package com.heronix.model.domain;

import com.heronix.model.enums.SchoolType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * SchoolConfiguration Entity
 *
 * Stores the school-wide configuration set during initial installation.
 * This configuration determines:
 * - Grade levels available for registration
 * - Courses displayed and available
 * - Age-appropriate features and workflows
 * - School identification information
 *
 * This is a singleton entity - only one record should exist per installation.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Entity
@Table(name = "school_configuration")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchoolConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========================================================================
    // SCHOOL IDENTIFICATION
    // ========================================================================

    /**
     * Official school name
     */
    @Column(name = "school_name", nullable = false)
    private String schoolName;

    /**
     * School district name
     */
    @Column(name = "district_name")
    private String districtName;

    /**
     * State/District ID number
     */
    @Column(name = "state_school_id")
    private String stateSchoolId;

    /**
     * Federal NCES ID
     */
    @Column(name = "nces_id")
    private String ncesId;

    // ========================================================================
    // SCHOOL TYPE & GRADE LEVELS
    // ========================================================================

    /**
     * Type of school (Elementary, Middle, High, K-8, K-12)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "school_type", nullable = false)
    private SchoolType schoolType;

    /**
     * Minimum grade level offered (numeric: -1=Pre-K, 0=K, 1-12)
     */
    @Column(name = "min_grade_level")
    private Integer minGradeLevel;

    /**
     * Maximum grade level offered (numeric: -1=Pre-K, 0=K, 1-12)
     */
    @Column(name = "max_grade_level")
    private Integer maxGradeLevel;

    // ========================================================================
    // CONTACT INFORMATION
    // ========================================================================

    /**
     * School address - street
     */
    @Column(name = "address_street")
    private String addressStreet;

    /**
     * School address - city
     */
    @Column(name = "address_city")
    private String addressCity;

    /**
     * School address - state
     */
    @Column(name = "address_state")
    private String addressState;

    /**
     * School address - ZIP code
     */
    @Column(name = "address_zip")
    private String addressZip;

    /**
     * Main phone number
     */
    @Column(name = "phone_number")
    private String phoneNumber;

    /**
     * Fax number
     */
    @Column(name = "fax_number")
    private String faxNumber;

    /**
     * School email address
     */
    @Column(name = "email")
    private String email;

    /**
     * School website URL
     */
    @Column(name = "website")
    private String website;

    // ========================================================================
    // LEADERSHIP
    // ========================================================================

    /**
     * Principal's name
     */
    @Column(name = "principal_name")
    private String principalName;

    /**
     * Principal's email
     */
    @Column(name = "principal_email")
    private String principalEmail;

    /**
     * Registrar's name
     */
    @Column(name = "registrar_name")
    private String registrarName;

    /**
     * Registrar's email
     */
    @Column(name = "registrar_email")
    private String registrarEmail;

    // ========================================================================
    // ACADEMIC YEAR SETTINGS
    // ========================================================================

    /**
     * Current academic year (e.g., "2025-2026")
     */
    @Column(name = "academic_year")
    private String academicYear;

    /**
     * First day of school year
     */
    @Column(name = "school_year_start")
    private java.time.LocalDate schoolYearStart;

    /**
     * Last day of school year
     */
    @Column(name = "school_year_end")
    private java.time.LocalDate schoolYearEnd;

    // ========================================================================
    // FEATURE FLAGS
    // ========================================================================

    /**
     * Enable student photo requirement
     */
    @Column(name = "require_student_photo")
    private Boolean requireStudentPhoto = true;

    /**
     * Enable QR code generation for students
     */
    @Column(name = "enable_qr_codes")
    private Boolean enableQrCodes = true;

    /**
     * Enable attendance tracking
     */
    @Column(name = "enable_attendance")
    private Boolean enableAttendance = true;

    /**
     * Enable gradebook
     */
    @Column(name = "enable_gradebook")
    private Boolean enableGradebook = true;

    /**
     * Enable medical records tracking
     */
    @Column(name = "enable_medical_records")
    private Boolean enableMedicalRecords = true;

    /**
     * Enable IEP/504 tracking
     */
    @Column(name = "enable_sped_tracking")
    private Boolean enableSpedTracking = true;

    // ========================================================================
    // TIMESTAMPS
    // ========================================================================

    /**
     * Date of initial setup
     */
    @Column(name = "setup_date")
    private LocalDateTime setupDate;

    /**
     * User who performed initial setup
     */
    @Column(name = "setup_by")
    private String setupBy;

    /**
     * Last modification date
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Has initial setup been completed?
     */
    @Column(name = "setup_complete")
    private Boolean setupComplete = false;

    @PrePersist
    protected void onCreate() {
        setupDate = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Check if a grade level is valid for this school
     */
    public boolean isGradeValid(int gradeLevel) {
        if (minGradeLevel == null || maxGradeLevel == null) {
            return schoolType != null && schoolType.isGradeValid(gradeLevel);
        }
        return gradeLevel >= minGradeLevel && gradeLevel <= maxGradeLevel;
    }

    /**
     * Get display string for grade range
     */
    public String getGradeRangeDisplay() {
        if (schoolType != null) {
            return schoolType.getGradeRangeDisplay();
        }
        return "Grades " + minGradeLevel + " to " + maxGradeLevel;
    }

    /**
     * Complete the initial setup
     */
    public void completeSetup(String setupByUser) {
        this.setupComplete = true;
        this.setupBy = setupByUser;
        this.updatedAt = LocalDateTime.now();
    }
}
