package com.heronix.model.domain;

import com.heronix.model.enums.USState;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * StateConfiguration Entity
 *
 * Stores comprehensive state-specific configuration for the SIS.
 * Each US state has unique requirements for:
 * - Course coding systems
 * - Graduation requirements
 * - Attendance policies
 * - Grading scales
 * - Reporting formats
 * - Terminology preferences
 * - Regulatory compliance
 *
 * When a district selects their state during initial setup,
 * this configuration is loaded to automatically adapt the entire
 * application to that state's specific needs.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01 - State Configuration Feature
 */
@Entity
@Table(name = "state_configurations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StateConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========================================================================
    // STATE IDENTIFICATION
    // ========================================================================

    /**
     * The US state this configuration applies to
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, unique = true, length = 20)
    @NotNull(message = "State is required")
    private USState state;

    /**
     * Full state name for display
     */
    @Column(name = "state_name", nullable = false, length = 100)
    private String stateName;

    /**
     * State education department name
     */
    @Column(name = "education_department", length = 200)
    private String educationDepartment;

    /**
     * State education department abbreviation (e.g., TEA, DOE, OSPI)
     */
    @Column(name = "department_abbreviation", length = 20)
    private String departmentAbbreviation;

    /**
     * State education department website URL
     */
    @Column(name = "department_url", length = 500)
    private String departmentUrl;

    // ========================================================================
    // COURSE CODING SYSTEM
    // ========================================================================

    /**
     * Name of the state's course coding system
     * Examples: "PEIMS Service IDs", "CALPADS Course Codes", "EMIS Course Codes"
     */
    @Column(name = "course_code_system_name", length = 100)
    private String courseCodeSystemName;

    /**
     * Format/pattern for state course codes
     * Examples: "########" (8 digits for TX), "#####" (5 digits for CA)
     */
    @Column(name = "course_code_format", length = 50)
    private String courseCodeFormat;

    /**
     * Length of state course codes
     */
    @Column(name = "course_code_length")
    private Integer courseCodeLength;

    /**
     * Does this state use SCED codes directly?
     */
    @Column(name = "uses_sced_directly")
    private Boolean usesSCEDDirectly = false;

    /**
     * URL to state course catalog/finder
     */
    @Column(name = "course_catalog_url", length = 500)
    private String courseCatalogUrl;

    /**
     * Label for state course code field
     * Examples: "Service ID" (TX), "Course Code" (CA), "Course Number" (NY)
     */
    @Column(name = "course_code_label", length = 50)
    private String courseCodeLabel = "State Course Code";

    // ========================================================================
    // STUDENT IDENTIFICATION
    // ========================================================================

    /**
     * Name of state student ID system
     * Examples: "PEIMS ID", "SSID", "OSIS ID", "SASID"
     */
    @Column(name = "student_id_system_name", length = 100)
    private String studentIdSystemName;

    /**
     * Label for student state ID field
     */
    @Column(name = "student_id_label", length = 50)
    private String studentIdLabel = "State Student ID";

    /**
     * Format for state student IDs
     */
    @Column(name = "student_id_format", length = 50)
    private String studentIdFormat;

    /**
     * Length of state student IDs
     */
    @Column(name = "student_id_length")
    private Integer studentIdLength;

    // ========================================================================
    // SCHOOL/DISTRICT IDENTIFICATION
    // ========================================================================

    /**
     * Name of state school ID system
     * Examples: "Campus ID" (TX), "CDS Code" (CA), "BEDS Code" (NY)
     */
    @Column(name = "school_id_system_name", length = 100)
    private String schoolIdSystemName;

    /**
     * Label for school/campus ID field
     */
    @Column(name = "school_id_label", length = 50)
    private String schoolIdLabel = "School ID";

    /**
     * Label for district ID field
     */
    @Column(name = "district_id_label", length = 50)
    private String districtIdLabel = "District ID";

    // ========================================================================
    // GRADING SYSTEM
    // ========================================================================

    /**
     * Default grading scale type
     * Options: LETTER_STANDARD, LETTER_PLUS_MINUS, NUMERIC_100, NUMERIC_4
     */
    @Column(name = "default_grading_scale", length = 30)
    private String defaultGradingScale = "LETTER_STANDARD";

    /**
     * Minimum passing grade (numeric)
     */
    @Column(name = "minimum_passing_grade")
    private Double minimumPassingGrade = 60.0;

    /**
     * Minimum passing grade (letter)
     */
    @Column(name = "minimum_passing_letter", length = 5)
    private String minimumPassingLetter = "D";

    /**
     * GPA scale (typically 4.0, but some states use 5.0 for weighted)
     */
    @Column(name = "gpa_scale")
    private Double gpaScale = 4.0;

    /**
     * Weighted GPA scale for honors/AP courses
     */
    @Column(name = "weighted_gpa_scale")
    private Double weightedGpaScale = 5.0;

    /**
     * Honor roll minimum GPA
     */
    @Column(name = "honor_roll_gpa")
    private Double honorRollGpa = 3.0;

    /**
     * High honor roll / Principal's list minimum GPA
     */
    @Column(name = "high_honor_roll_gpa")
    private Double highHonorRollGpa = 3.5;

    // ========================================================================
    // ATTENDANCE REQUIREMENTS
    // ========================================================================

    /**
     * Required school days per year
     */
    @Column(name = "required_school_days")
    private Integer requiredSchoolDays = 180;

    /**
     * Required instructional minutes per day (elementary)
     */
    @Column(name = "instructional_minutes_elementary")
    private Integer instructionalMinutesElementary = 360;

    /**
     * Required instructional minutes per day (secondary)
     */
    @Column(name = "instructional_minutes_secondary")
    private Integer instructionalMinutesSecondary = 420;

    /**
     * Maximum absences before truancy intervention (percentage)
     */
    @Column(name = "truancy_threshold_percent")
    private Double truancyThresholdPercent = 10.0;

    /**
     * Chronic absenteeism threshold (percentage)
     */
    @Column(name = "chronic_absenteeism_threshold")
    private Double chronicAbsenteeismThreshold = 10.0;

    /**
     * Minutes late before considered tardy
     */
    @Column(name = "tardy_threshold_minutes")
    private Integer tardyThresholdMinutes = 10;

    /**
     * Attendance tracking method
     * Options: DAILY, PERIOD_BY_PERIOD, BOTH
     */
    @Column(name = "attendance_tracking_method", length = 30)
    private String attendanceTrackingMethod = "PERIOD_BY_PERIOD";

    // ========================================================================
    // GRADUATION REQUIREMENTS (High School)
    // ========================================================================

    /**
     * Total credits required for graduation
     */
    @Column(name = "graduation_credits_required")
    private Double graduationCreditsRequired = 26.0;

    /**
     * English/Language Arts credits required
     */
    @Column(name = "english_credits_required")
    private Double englishCreditsRequired = 4.0;

    /**
     * Mathematics credits required
     */
    @Column(name = "math_credits_required")
    private Double mathCreditsRequired = 4.0;

    /**
     * Science credits required
     */
    @Column(name = "science_credits_required")
    private Double scienceCreditsRequired = 4.0;

    /**
     * Social Studies/History credits required
     */
    @Column(name = "social_studies_credits_required")
    private Double socialStudiesCreditsRequired = 4.0;

    /**
     * Physical Education credits required
     */
    @Column(name = "pe_credits_required")
    private Double peCreditsRequired = 1.0;

    /**
     * Foreign Language credits required
     */
    @Column(name = "foreign_language_credits_required")
    private Double foreignLanguageCreditsRequired = 2.0;

    /**
     * Fine Arts credits required
     */
    @Column(name = "fine_arts_credits_required")
    private Double fineArtsCreditsRequired = 1.0;

    /**
     * Elective credits required
     */
    @Column(name = "elective_credits_required")
    private Double electiveCreditsRequired = 6.0;

    /**
     * Community service hours required (if any)
     */
    @Column(name = "community_service_hours")
    private Integer communityServiceHours = 0;

    /**
     * State exit exam required?
     */
    @Column(name = "exit_exam_required")
    private Boolean exitExamRequired = false;

    /**
     * Name of state exit exam (if required)
     */
    @Column(name = "exit_exam_name", length = 100)
    private String exitExamName;

    // ========================================================================
    // SPECIAL PROGRAMS TERMINOLOGY
    // ========================================================================

    /**
     * Label for Special Education program
     * Examples: "Special Education", "Exceptional Student Education" (FL)
     */
    @Column(name = "special_ed_label", length = 100)
    private String specialEdLabel = "Special Education";

    /**
     * Label for English Language Learners
     * Examples: "ELL", "ESL", "EL", "ESOL", "Emergent Bilingual" (TX)
     */
    @Column(name = "ell_label", length = 100)
    private String ellLabel = "English Language Learner";

    /**
     * Label for Gifted/Talented program
     * Examples: "Gifted and Talented", "GATE", "AIG" (NC)
     */
    @Column(name = "gifted_label", length = 100)
    private String giftedLabel = "Gifted and Talented";

    /**
     * Label for CTE programs
     */
    @Column(name = "cte_label", length = 100)
    private String cteLabel = "Career and Technical Education";

    /**
     * Label for economically disadvantaged
     * Examples: "Economically Disadvantaged", "Free/Reduced Lunch"
     */
    @Column(name = "economically_disadvantaged_label", length = 100)
    private String economicallyDisadvantagedLabel = "Economically Disadvantaged";

    // ========================================================================
    // SCHOOL CALENDAR
    // ========================================================================

    /**
     * Typical school year start month (1-12)
     */
    @Column(name = "school_year_start_month")
    private Integer schoolYearStartMonth = 8;

    /**
     * Typical school year end month (1-12)
     */
    @Column(name = "school_year_end_month")
    private Integer schoolYearEndMonth = 5;

    /**
     * Does this state observe spring break?
     */
    @Column(name = "has_spring_break")
    private Boolean hasSpringBreak = true;

    /**
     * Does this state have a fall break?
     */
    @Column(name = "has_fall_break")
    private Boolean hasFallBreak = false;

    /**
     * Does this state have winter break separate from Christmas?
     */
    @Column(name = "has_winter_break")
    private Boolean hasWinterBreak = true;

    // ========================================================================
    // REPORTING REQUIREMENTS
    // ========================================================================

    /**
     * State reporting system name
     * Examples: "PEIMS" (TX), "CALPADS" (CA), "EMIS" (OH), "STARS" (NM)
     */
    @Column(name = "state_reporting_system", length = 100)
    private String stateReportingSystem;

    /**
     * Number of reporting periods per year
     */
    @Column(name = "reporting_periods_per_year")
    private Integer reportingPeriodsPerYear = 6;

    /**
     * Report card periods per year
     */
    @Column(name = "report_card_periods")
    private Integer reportCardPeriods = 4;

    /**
     * Progress report periods per year
     */
    @Column(name = "progress_report_periods")
    private Integer progressReportPeriods = 4;

    // ========================================================================
    // TEACHER CERTIFICATION
    // ========================================================================

    /**
     * State teacher certification board name
     */
    @Column(name = "certification_board", length = 200)
    private String certificationBoard;

    /**
     * State certification lookup URL
     */
    @Column(name = "certification_lookup_url", length = 500)
    private String certificationLookupUrl;

    // ========================================================================
    // DATA PRIVACY & COMPLIANCE
    // ========================================================================

    /**
     * Additional state privacy laws beyond FERPA
     */
    @Column(name = "state_privacy_laws", length = 500)
    private String statePrivacyLaws;

    /**
     * Parent notification requirements for data sharing
     */
    @Column(name = "parent_notification_required")
    private Boolean parentNotificationRequired = true;

    /**
     * Student data retention years
     */
    @Column(name = "data_retention_years")
    private Integer dataRetentionYears = 7;

    // ========================================================================
    // METADATA
    // ========================================================================

    /**
     * School year this configuration is valid for
     */
    @Column(name = "school_year", length = 20)
    private String schoolYear;

    /**
     * Is this configuration currently active?
     */
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    /**
     * Notes about this configuration
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Last updated timestamp
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Who last updated this record
     */
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        updatedAt = LocalDateTime.now();
        if (stateName == null && state != null) {
            stateName = state.getDisplayName();
        }
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Get display name for the state
     */
    public String getDisplayName() {
        return state != null ? state.getDisplayName() : stateName;
    }

    /**
     * Get the course code label (state-specific terminology)
     */
    public String getCourseCodeLabelOrDefault() {
        return courseCodeLabel != null ? courseCodeLabel : "State Course Code";
    }

    /**
     * Get the student ID label (state-specific terminology)
     */
    public String getStudentIdLabelOrDefault() {
        return studentIdLabel != null ? studentIdLabel : "State Student ID";
    }

    /**
     * Check if exit exam is required for graduation
     */
    public boolean requiresExitExam() {
        return Boolean.TRUE.equals(exitExamRequired);
    }

    /**
     * Get total graduation credits required
     */
    public Double getTotalGraduationCredits() {
        return graduationCreditsRequired != null ? graduationCreditsRequired : 26.0;
    }
}
