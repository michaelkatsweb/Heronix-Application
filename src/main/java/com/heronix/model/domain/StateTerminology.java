package com.heronix.model.domain;

import com.heronix.model.enums.USState;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * StateTerminology Entity
 *
 * Maps standard SIS terminology to state-specific terms.
 * Different states use different words for the same concepts:
 *
 * Examples:
 * - "Campus" (TX) vs "School" (most states) vs "Building" (some states)
 * - "Emergent Bilingual" (TX) vs "English Learner" (CA) vs "ELL" (most)
 * - "Service ID" (TX) vs "Course Code" (CA) vs "Course Number" (NY)
 * - "STAAR" (TX) vs "CAASPP" (CA) vs "PARCC" (various) - state assessments
 *
 * This allows the UI and reports to display state-appropriate terminology
 * automatically based on the selected state configuration.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01 - State Configuration Feature
 */
@Entity
@Table(name = "state_terminology",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_state_term_key",
                        columnNames = {"state", "term_key"})
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StateTerminology {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The state this terminology applies to
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    private USState state;

    /**
     * The standard/default term key
     * Examples: "SCHOOL", "STUDENT_ID", "ELL", "COURSE_CODE"
     */
    @Column(name = "term_key", nullable = false, length = 50)
    private String termKey;

    /**
     * The state-specific term to use
     */
    @Column(name = "state_term", nullable = false, length = 200)
    private String stateTerm;

    /**
     * Abbreviated version of the state term
     */
    @Column(name = "state_term_short", length = 50)
    private String stateTermShort;

    /**
     * Description or context for this terminology
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Category of the terminology
     * Examples: GENERAL, STUDENT, STAFF, COURSE, ASSESSMENT, PROGRAM
     */
    @Column(name = "category", length = 30)
    private String category;

    /**
     * Is this term active?
     */
    @Column(name = "active")
    private Boolean active = true;

    // ========================================================================
    // STANDARD TERM KEYS (Constants for reference)
    // ========================================================================

    // General Terms
    public static final String TERM_SCHOOL = "SCHOOL";
    public static final String TERM_CAMPUS = "CAMPUS";
    public static final String TERM_DISTRICT = "DISTRICT";
    public static final String TERM_BUILDING = "BUILDING";

    // Student Terms
    public static final String TERM_STUDENT_ID = "STUDENT_ID";
    public static final String TERM_GRADE_LEVEL = "GRADE_LEVEL";
    public static final String TERM_HOMEROOM = "HOMEROOM";
    public static final String TERM_ADVISORY = "ADVISORY";

    // Program Terms
    public static final String TERM_ELL = "ELL";
    public static final String TERM_SPECIAL_ED = "SPECIAL_ED";
    public static final String TERM_GIFTED = "GIFTED";
    public static final String TERM_CTE = "CTE";
    public static final String TERM_504_PLAN = "504_PLAN";
    public static final String TERM_IEP = "IEP";
    public static final String TERM_AT_RISK = "AT_RISK";
    public static final String TERM_ECONOMICALLY_DISADVANTAGED = "ECONOMICALLY_DISADVANTAGED";

    // Course Terms
    public static final String TERM_COURSE_CODE = "COURSE_CODE";
    public static final String TERM_COURSE_SECTION = "COURSE_SECTION";
    public static final String TERM_PERIOD = "PERIOD";
    public static final String TERM_BLOCK = "BLOCK";

    // Staff Terms
    public static final String TERM_TEACHER = "TEACHER";
    public static final String TERM_PARA = "PARA";
    public static final String TERM_COUNSELOR = "COUNSELOR";
    public static final String TERM_PRINCIPAL = "PRINCIPAL";
    public static final String TERM_AP = "AP"; // Assistant Principal

    // Assessment Terms
    public static final String TERM_STATE_ASSESSMENT = "STATE_ASSESSMENT";
    public static final String TERM_BENCHMARK = "BENCHMARK";
    public static final String TERM_FORMATIVE = "FORMATIVE";
    public static final String TERM_SUMMATIVE = "SUMMATIVE";

    // Attendance Terms
    public static final String TERM_TARDY = "TARDY";
    public static final String TERM_ABSENT = "ABSENT";
    public static final String TERM_EXCUSED = "EXCUSED";
    public static final String TERM_UNEXCUSED = "UNEXCUSED";
    public static final String TERM_TRUANT = "TRUANT";

    // Grading Terms
    public static final String TERM_REPORT_CARD = "REPORT_CARD";
    public static final String TERM_PROGRESS_REPORT = "PROGRESS_REPORT";
    public static final String TERM_TRANSCRIPT = "TRANSCRIPT";
    public static final String TERM_GPA = "GPA";
    public static final String TERM_CLASS_RANK = "CLASS_RANK";

    // Calendar Terms
    public static final String TERM_GRADING_PERIOD = "GRADING_PERIOD";
    public static final String TERM_SEMESTER = "SEMESTER";
    public static final String TERM_QUARTER = "QUARTER";
    public static final String TERM_TRIMESTER = "TRIMESTER";
    public static final String TERM_NINE_WEEKS = "NINE_WEEKS";
    public static final String TERM_SIX_WEEKS = "SIX_WEEKS";

    // ========================================================================
    // CATEGORY CONSTANTS
    // ========================================================================

    public static final String CAT_GENERAL = "GENERAL";
    public static final String CAT_STUDENT = "STUDENT";
    public static final String CAT_STAFF = "STAFF";
    public static final String CAT_COURSE = "COURSE";
    public static final String CAT_ASSESSMENT = "ASSESSMENT";
    public static final String CAT_PROGRAM = "PROGRAM";
    public static final String CAT_ATTENDANCE = "ATTENDANCE";
    public static final String CAT_GRADING = "GRADING";
    public static final String CAT_CALENDAR = "CALENDAR";

    // ========================================================================
    // FACTORY METHODS
    // ========================================================================

    /**
     * Create a terminology entry
     */
    public static StateTerminology create(USState state, String termKey, String stateTerm,
                                           String shortTerm, String category) {
        StateTerminology term = new StateTerminology();
        term.setState(state);
        term.setTermKey(termKey);
        term.setStateTerm(stateTerm);
        term.setStateTermShort(shortTerm);
        term.setCategory(category);
        term.setActive(true);
        return term;
    }
}
