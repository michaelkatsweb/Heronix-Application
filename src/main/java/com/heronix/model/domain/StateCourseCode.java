package com.heronix.model.domain;

import com.heronix.model.enums.CourseCategory;
import com.heronix.model.enums.CourseType;
import com.heronix.model.enums.EducationLevel;
import com.heronix.model.enums.GradeLevel;
import com.heronix.model.enums.SCEDSubjectArea;
import com.heronix.model.enums.USState;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * StateCourseCode Entity
 *
 * Represents a course code from a state's official course catalog.
 * Each US state maintains their own course coding system, many of which
 * map to the national SCED (School Courses for the Exchange of Data) standard.
 *
 * This entity allows schools to:
 * - Import their state's official course catalog
 * - Map local courses to state-approved codes
 * - Ensure compliance with state reporting requirements
 * - Pre-populate course information when setting up the SIS
 *
 * Structure:
 * - State: Which state this code belongs to
 * - State Course Code: The state's official course identifier
 * - SCED Code: The mapped national SCED code (if applicable)
 * - Course Name: Official state course name
 * - Grade Levels: Which grades can take this course
 * - Credits: Default credit value
 * - Subject Area: SCED subject classification
 *
 * Data Sources:
 * - Each state's Department of Education course catalog
 * - NCES SCED database for cross-state mapping
 * - Updated annually based on state education department releases
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01 - State Course Catalog Feature
 */
@Entity
@Table(name = "state_course_codes",
        indexes = {
                @Index(name = "idx_state_course_state", columnList = "state"),
                @Index(name = "idx_state_course_code", columnList = "state_course_code"),
                @Index(name = "idx_state_course_sced", columnList = "sced_code"),
                @Index(name = "idx_state_course_subject", columnList = "subject_area"),
                @Index(name = "idx_state_course_grade", columnList = "min_grade_level, max_grade_level"),
                @Index(name = "idx_state_course_active", columnList = "active")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_state_course_code",
                        columnNames = {"state", "state_course_code"})
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StateCourseCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========================================================================
    // STATE IDENTIFICATION
    // ========================================================================

    /**
     * The US state this course code belongs to
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    @NotNull(message = "State is required")
    private USState state;

    /**
     * The state's official course code/identifier
     * Format varies by state:
     * - Texas (TEA): 8-digit service ID (e.g., "03220100" for Algebra I)
     * - California: 5-digit code
     * - New York: 4-digit code
     * - Florida: 7-digit code
     */
    @Column(name = "state_course_code", nullable = false, length = 30)
    @NotBlank(message = "State course code is required")
    @Size(max = 30, message = "State course code cannot exceed 30 characters")
    private String stateCourseCode;

    /**
     * National SCED code mapping (5-digit: 2 for subject area + 3 for course)
     * Example: "02101" = Mathematics (02) + Algebra I (101)
     */
    @Column(name = "sced_code", length = 10)
    @Size(max = 10, message = "SCED code cannot exceed 10 characters")
    private String scedCode;

    /**
     * The SCED subject area for this course
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "subject_area", length = 50)
    private SCEDSubjectArea subjectArea;

    // ========================================================================
    // COURSE INFORMATION
    // ========================================================================

    /**
     * Official state course name
     */
    @Column(name = "course_name", nullable = false, length = 200)
    @NotBlank(message = "Course name is required")
    @Size(max = 200, message = "Course name cannot exceed 200 characters")
    private String courseName;

    /**
     * State's abbreviated/short course name
     */
    @Column(name = "course_abbreviation", length = 50)
    @Size(max = 50, message = "Course abbreviation cannot exceed 50 characters")
    private String courseAbbreviation;

    /**
     * Detailed course description from state catalog
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Course category: CORE (required) or ELECTIVE (optional)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "course_category", length = 20)
    private CourseCategory courseCategory = CourseCategory.CORE;

    /**
     * Course type/level: REGULAR, HONORS, AP, IB, DUAL_CREDIT, etc.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "course_type", length = 30)
    private CourseType courseType = CourseType.REGULAR;

    // ========================================================================
    // GRADE LEVEL ELIGIBILITY
    // ========================================================================

    /**
     * Minimum grade level for this course (numeric: -1=Pre-K, 0=K, 1-12)
     * Example: 9 = 9th graders and above
     */
    @Column(name = "min_grade_level")
    @Min(value = -1, message = "Minimum grade level must be at least -1 (Pre-K)")
    @Max(value = 12, message = "Minimum grade level cannot exceed 12")
    private Integer minGradeLevel;

    /**
     * Maximum grade level for this course (numeric: -1=Pre-K, 0=K, 1-12)
     * Example: 12 = up to 12th grade
     */
    @Column(name = "max_grade_level")
    @Min(value = -1, message = "Maximum grade level must be at least -1 (Pre-K)")
    @Max(value = 12, message = "Maximum grade level cannot exceed 12")
    private Integer maxGradeLevel;

    /**
     * Education level this course is designed for
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "education_level", length = 20)
    private EducationLevel educationLevel;

    /**
     * Specific grade levels where this course is typically offered
     * Stored as comma-separated values (e.g., "9,10,11,12")
     */
    @Column(name = "typical_grade_levels", length = 50)
    private String typicalGradeLevels;

    // ========================================================================
    // ACADEMIC REQUIREMENTS
    // ========================================================================

    /**
     * Default credit hours for this course
     * Common values: 0.5, 1.0, 1.5, 2.0
     */
    @Column(name = "credits")
    @DecimalMin(value = "0.0", message = "Credits cannot be negative")
    @DecimalMax(value = "10.0", message = "Credits cannot exceed 10")
    private Double credits = 1.0;

    /**
     * Carnegie units for this course (for graduation requirements)
     */
    @Column(name = "carnegie_units")
    private Double carnegieUnits;

    /**
     * Prerequisite course codes (comma-separated state course codes)
     */
    @Column(name = "prerequisites", length = 500)
    private String prerequisites;

    /**
     * Corequisite course codes (must be taken simultaneously)
     */
    @Column(name = "corequisites", length = 200)
    private String corequisites;

    /**
     * Is this course repeatable for credit?
     */
    @Column(name = "repeatable")
    private Boolean repeatable = false;

    /**
     * Maximum times this course can be taken for credit
     */
    @Column(name = "max_repetitions")
    private Integer maxRepetitions = 1;

    // ========================================================================
    // INSTRUCTIONAL DETAILS
    // ========================================================================

    /**
     * Recommended instructional minutes per session
     */
    @Column(name = "instructional_minutes")
    private Integer instructionalMinutes;

    /**
     * Recommended sessions per week
     */
    @Column(name = "sessions_per_week")
    private Integer sessionsPerWeek;

    /**
     * Course duration type: SEMESTER, YEAR, TRIMESTER, QUARTER
     */
    @Column(name = "duration_type", length = 20)
    private String durationType;

    /**
     * Special equipment or facilities required
     */
    @Column(name = "special_requirements", columnDefinition = "TEXT")
    private String specialRequirements;

    // ========================================================================
    // CAREER AND TECHNICAL EDUCATION (CTE)
    // ========================================================================

    /**
     * Is this a CTE (Career and Technical Education) course?
     */
    @Column(name = "is_cte")
    private Boolean isCTE = false;

    /**
     * CTE career cluster (if CTE course)
     * Examples: Agriculture, Business, Health Sciences, IT, etc.
     */
    @Column(name = "cte_cluster", length = 100)
    private String cteCluster;

    /**
     * CTE program of study
     */
    @Column(name = "cte_program", length = 200)
    private String cteProgram;

    /**
     * Industry certification this course prepares students for
     */
    @Column(name = "industry_certification", length = 200)
    private String industryCertification;

    // ========================================================================
    // SPECIAL DESIGNATIONS
    // ========================================================================

    /**
     * Is this an AP (Advanced Placement) course?
     */
    @Column(name = "is_ap")
    private Boolean isAP = false;

    /**
     * Is this an IB (International Baccalaureate) course?
     */
    @Column(name = "is_ib")
    private Boolean isIB = false;

    /**
     * Is this a dual credit/concurrent enrollment course?
     */
    @Column(name = "is_dual_credit")
    private Boolean isDualCredit = false;

    /**
     * College/university partner for dual credit
     */
    @Column(name = "dual_credit_partner", length = 200)
    private String dualCreditPartner;

    /**
     * Does this course satisfy a graduation requirement?
     */
    @Column(name = "graduation_requirement")
    private Boolean graduationRequirement = false;

    /**
     * Which graduation requirement this satisfies (e.g., "Math", "Science", "English")
     */
    @Column(name = "graduation_requirement_type", length = 100)
    private String graduationRequirementType;

    /**
     * Is this a STEM-designated course?
     */
    @Column(name = "is_stem")
    private Boolean isSTEM = false;

    // ========================================================================
    // STATE-SPECIFIC FIELDS
    // ========================================================================

    /**
     * State's subject area code (state-specific classification)
     */
    @Column(name = "state_subject_code", length = 20)
    private String stateSubjectCode;

    /**
     * State's subject area name
     */
    @Column(name = "state_subject_name", length = 100)
    private String stateSubjectName;

    /**
     * State accountability code
     */
    @Column(name = "accountability_code", length = 30)
    private String accountabilityCode;

    /**
     * State funding code
     */
    @Column(name = "funding_code", length = 30)
    private String fundingCode;

    /**
     * Additional state-specific flags (JSON format)
     */
    @Column(name = "state_flags", columnDefinition = "TEXT")
    private String stateFlags;

    // ========================================================================
    // METADATA
    // ========================================================================

    /**
     * School year this code is valid for (e.g., "2025-2026")
     */
    @Column(name = "school_year", length = 20)
    private String schoolYear;

    /**
     * Effective date of this course code
     */
    @Column(name = "effective_date")
    private java.time.LocalDate effectiveDate;

    /**
     * Expiration date (if course code is retired)
     */
    @Column(name = "expiration_date")
    private java.time.LocalDate expirationDate;

    /**
     * Is this course code currently active?
     */
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    /**
     * Source of this data (e.g., "TX TEA Course Catalog 2026", "CSV Import")
     */
    @Column(name = "data_source", length = 200)
    private String dataSource;

    /**
     * URL to official state documentation
     */
    @Column(name = "documentation_url", length = 500)
    private String documentationUrl;

    /**
     * Notes about this course code
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * When this record was created
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Who created this record
     */
    @Column(name = "created_by", length = 100)
    private String createdBy;

    /**
     * When this record was last updated
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Who last updated this record
     */
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // ========================================================================
    // LIFECYCLE CALLBACKS
    // ========================================================================

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        // Auto-detect subject area from SCED code if not set
        if (subjectArea == null && scedCode != null && scedCode.length() >= 2) {
            subjectArea = SCEDSubjectArea.fromCode(scedCode.substring(0, 2));
        }

        // Auto-detect education level from grade levels
        if (educationLevel == null && minGradeLevel != null) {
            educationLevel = determineEducationLevel(minGradeLevel);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Determine education level from a grade level number
     */
    private EducationLevel determineEducationLevel(Integer gradeLevel) {
        if (gradeLevel == null) return EducationLevel.HIGH_SCHOOL;
        if (gradeLevel < 0) return EducationLevel.PRE_K;
        if (gradeLevel == 0) return EducationLevel.KINDERGARTEN;
        if (gradeLevel <= 5) return EducationLevel.ELEMENTARY;
        if (gradeLevel <= 8) return EducationLevel.MIDDLE_SCHOOL;
        return EducationLevel.HIGH_SCHOOL;
    }

    /**
     * Get formatted display name with state code
     */
    public String getDisplayName() {
        return String.format("%s - %s (%s)",
                stateCourseCode,
                courseName,
                state != null ? state.name() : "");
    }

    /**
     * Get grade level range as string
     */
    public String getGradeLevelRange() {
        if (minGradeLevel == null && maxGradeLevel == null) {
            return "All Grades";
        }

        String min = formatGradeLevel(minGradeLevel);
        String max = formatGradeLevel(maxGradeLevel);

        if (minGradeLevel != null && maxGradeLevel != null) {
            if (minGradeLevel.equals(maxGradeLevel)) {
                return min + " Only";
            }
            return min + " - " + max;
        }

        if (minGradeLevel != null) {
            return min + " and above";
        }

        return "Up to " + max;
    }

    /**
     * Format a numeric grade level to display string
     */
    private String formatGradeLevel(Integer grade) {
        if (grade == null) return "";
        if (grade == -1) return "Pre-K";
        if (grade == 0) return "K";
        return "Grade " + grade;
    }

    /**
     * Check if a student at the given grade level is eligible for this course
     */
    public boolean isEligibleForGrade(Integer studentGrade) {
        if (studentGrade == null) return true;

        if (minGradeLevel != null && studentGrade < minGradeLevel) {
            return false;
        }

        if (maxGradeLevel != null && studentGrade > maxGradeLevel) {
            return false;
        }

        return true;
    }

    /**
     * Check if this is an advanced/honors level course
     */
    public boolean isAdvanced() {
        return Boolean.TRUE.equals(isAP) ||
               Boolean.TRUE.equals(isIB) ||
               Boolean.TRUE.equals(isDualCredit) ||
               courseType == CourseType.HONORS ||
               courseType == CourseType.AP ||
               courseType == CourseType.IB;
    }

    /**
     * Get the full state code with name
     */
    public String getFullStateName() {
        return state != null ? state.getDisplayName() : "";
    }

    /**
     * Get list of grade levels this course is available for
     */
    public List<Integer> getAvailableGradeLevels() {
        List<Integer> grades = new ArrayList<>();
        int min = minGradeLevel != null ? minGradeLevel : -1;
        int max = maxGradeLevel != null ? maxGradeLevel : 12;

        for (int i = min; i <= max; i++) {
            grades.add(i);
        }
        return grades;
    }

    /**
     * Check if this course code is current (not expired)
     */
    public boolean isCurrent() {
        if (!Boolean.TRUE.equals(active)) return false;
        if (expirationDate != null && expirationDate.isBefore(java.time.LocalDate.now())) {
            return false;
        }
        return true;
    }
}
