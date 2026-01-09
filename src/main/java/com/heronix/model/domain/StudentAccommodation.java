package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Student Accommodation Record
 *
 * Tracks accommodations and support services for students including:
 * - 504 Plans (Section 504 of Rehabilitation Act)
 * - IEP (Individualized Education Program)
 * - ELL/ESL services (English Language Learners)
 * - Gifted and Talented programs
 * - Title I services
 * - At-risk interventions
 * - Special transportation needs
 * - Accessibility accommodations
 * - Assistive technology
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Entity
@Table(name = "student_accommodations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAccommodation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========================================================================
    // STUDENT RELATIONSHIP
    // ========================================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // ========================================================================
    // ACCOMMODATION TYPE & STATUS
    // ========================================================================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AccommodationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccommodationStatus status;

    @Column
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @Column
    private LocalDate nextReviewDate;

    @Column
    private LocalDate lastReviewDate;

    // ========================================================================
    // 504 PLAN DETAILS
    // ========================================================================

    @Column
    private Boolean has504Plan;

    @Column(length = 100)
    private String plan504Coordinator;

    @Column(length = 2000)
    private String plan504Accommodations;

    @Column(length = 2000)
    private String plan504Modifications;

    @Column
    private LocalDate plan504Date;

    @Column
    private LocalDate plan504ReviewDate;

    @Column
    private LocalDate plan504EffectiveDate;

    @Column
    private LocalDate plan504ExpirationDate;

    @Column
    private LocalDate plan504NextMeetingDate;

    @Column(length = 500)
    private String plan504DocumentPath;

    @Column
    private Boolean plan504RequiresParentConsent;

    @Column
    private Boolean plan504ParentConsentObtained;

    // ========================================================================
    // IEP DETAILS
    // ========================================================================

    @Column
    private Boolean hasIEP;

    @Column(length = 100)
    private String iepCaseManager;

    @Column(length = 100)
    private String caseManagerName;

    @Column(length = 100)
    private String specialEducationTeacher;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private IEPPlacement iepPlacement;

    @Column(length = 2000)
    private String iepGoals;

    @Column(length = 2000)
    private String iepAccommodations;

    @Column(length = 2000)
    private String iepModifications;

    @Column
    private LocalDate iepStartDate;

    @Column
    private LocalDate iepReviewDate;

    @Column
    private LocalDate iepEffectiveDate;

    @Column
    private LocalDate iepExpirationDate;

    @Column
    private LocalDate iepNextAnnualReview;

    @Column
    private LocalDate iepNextTriennialEvaluation;

    @Column(length = 500)
    private String iepDocumentPath;

    @Column(length = 200)
    private String primaryDisability;

    @Column(length = 500)
    private String secondaryDisabilities;

    @Column
    private Integer specialEducationMinutesPerWeek;

    @Column
    private Integer relatedServicesMinutesPerWeek;

    // ========================================================================
    // ELL/ESL SERVICES
    // ========================================================================

    @Column
    private Boolean isELL;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ELLProficiencyLevel ellProficiencyLevel;

    @Column(length = 100)
    private String nativeLanguage;

    @Column(length = 100)
    private String homeLanguage;

    @Column
    private LocalDate homeLangagueSurveyDate;

    @Column
    private Boolean ellServicesRequired;

    @Column(length = 2000)
    private String ellServices;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ELLServiceModel ellServiceModel;

    @Column
    private Integer ellMinutesPerWeek;

    @Column
    private LocalDate ellEntryDate;

    @Column
    private LocalDate ellExitDate;

    @Column
    private LocalDate ellNextAssessmentDate;

    @Column(length = 100)
    private String ellTeacher;

    // ========================================================================
    // GIFTED & TALENTED
    // ========================================================================

    @Column
    private Boolean isGifted;

    @Column
    private LocalDate giftedIdentificationDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private GiftedCategory giftedCategory;

    @Column(length = 2000)
    private String giftedServicesProvided;

    @Column(length = 100)
    private String giftedCoordinator;

    @Column
    private Integer giftedMinutesPerWeek;

    // ========================================================================
    // AT-RISK & INTERVENTIONS
    // ========================================================================

    @Column
    private Boolean isAtRisk;

    @Column(length = 500)
    private String atRiskReasons;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private RiskLevel riskLevel;

    @Column(length = 2000)
    private String interventionsProvided;

    @Column
    private Boolean receivesRTI; // Response to Intervention

    @Column(length = 50)
    private String rtiTier; // Tier 1, Tier 2, Tier 3

    @Column
    private LocalDate rtiStartDate;

    @Column(length = 2000)
    private String rtiInterventions;

    @Column(length = 2000)
    private String rtiProgressNotes;

    // ========================================================================
    // TITLE I & FEDERAL PROGRAMS
    // ========================================================================

    @Column
    private Boolean titleIEligible;

    @Column
    private Boolean titleIParticipating;

    @Column(length = 200)
    private String titleIServicesReceived;

    @Column
    private Integer titleIMinutesPerWeek;

    // McKinney-Vento Homeless Assistance
    @Column
    private Boolean homelessStatus;

    @Column(length = 100)
    private String homelessSituationType;

    @Column
    private Boolean mcKinneyVentoEligible;

    // Foster care
    @Column
    private Boolean fosterCareStatus;

    @Column(length = 200)
    private String fosterCareAgency;

    @Column(length = 100)
    private String fosterCareCaseWorker;

    @Column(length = 20)
    private String fosterCaseWorkerPhone;

    // Military family
    @Column
    private Boolean militaryFamily;

    @Column(length = 100)
    private String militaryBranch;

    @Column
    private Boolean militaryFamilyTransitional;

    // ========================================================================
    // FREE/REDUCED LUNCH
    // ========================================================================

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private LunchStatus lunchStatus;

    @Column
    private LocalDate lunchStatusEffectiveDate;

    @Column
    private LocalDate lunchStatusExpirationDate;

    @Column
    private Boolean directCertification; // Automatically qualified

    // ========================================================================
    // TRANSPORTATION
    // ========================================================================

    @Column
    private Boolean requiresSpecialTransportation;

    @Column(length = 500)
    private String transportationNeeds;

    @Column(length = 100)
    private String busNumber;

    @Column(length = 100)
    private String busStopLocation;

    @Column
    private Boolean wheelchairAccessible;

    @Column
    private Boolean requiresBusMonitor;

    @Column(length = 500)
    private String transportationNotes;

    // ========================================================================
    // ACCESSIBILITY & ASSISTIVE TECHNOLOGY
    // ========================================================================

    @Column
    private Boolean requiresAccessibilityAccommodations;

    @Column(length = 500)
    private String accessibilityNeeds;

    @Column(length = 2000)
    private String accessibilityAccommodationsList;

    @Column
    private Boolean wheelchairUser;

    @Column
    private Boolean requiresElevator;

    @Column
    private Boolean requiresGroundFloorClassroom;

    @Column
    private Boolean hearingImpaired;

    @Column
    private Boolean requiresSignLanguageInterpreter;

    @Column
    private Boolean visuallyImpaired;

    @Column
    private Boolean requiresBraille;

    @Column
    private Boolean requiresLargePrint;

    @Column
    private Boolean requiresAssistiveTechnology;

    @Column(length = 2000)
    private String assistiveTechnologyList;

    @Column(length = 2000)
    private String accessibilityNotes;

    // ========================================================================
    // TESTING ACCOMMODATIONS
    // ========================================================================

    @Column
    private Boolean requiresTestingAccommodations;

    @Column(length = 2000)
    private String testingAccommodationsList;

    @Column
    private Boolean extendedTimeOnTests;

    @Column
    private Integer extendedTimePercentage; // e.g., 150% = time and a half

    @Column
    private Boolean separateTestingLocation;

    @Column
    private Boolean testsReadAloud;

    @Column
    private Boolean useOfCalculator;

    @Column
    private Boolean useOfComputer;

    @Column
    private Boolean frequentBreaks;

    // ========================================================================
    // CLASSROOM ACCOMMODATIONS
    // ========================================================================

    @Column
    private Boolean preferentialSeating;

    @Column(length = 200)
    private String seatingPreference; // "Front of room", "Near teacher", etc.

    @Column
    private Boolean reducedDistractionsEnvironment;

    @Column
    private Boolean modifiedAssignments;

    @Column
    private Boolean extendedTimeOnAssignments;

    @Column
    private Boolean checkForUnderstanding;

    @Column
    private Boolean noteTakingAssistance;

    @Column
    private Boolean copyOfTeacherNotes;

    @Column
    private Boolean verbalInstructions;

    @Column
    private Boolean visualAids;

    @Column
    private Boolean frequentReminders;

    @Column(length = 2000)
    private String additionalClassroomAccommodations;

    // ========================================================================
    // ADMINISTRATIVE
    // ========================================================================

    @Column(length = 2000)
    private String administrativeNotes;

    @Column(length = 2000)
    private String coordinatorNotes;

    @Column(length = 2000)
    private String parentConcerns;

    @Column(length = 2000)
    private String teacherConcerns;

    @Column(length = 2000)
    private String successStrategies;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coordinator_staff_id")
    private User coordinator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_staff_id", nullable = false)
    private User createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_staff_id")
    private User updatedBy;

    @Column
    private LocalDateTime updatedAt;

    // ========================================================================
    // ENUMS
    // ========================================================================

    public enum AccommodationType {
        PLAN_504("504 Plan"),
        IEP("Individualized Education Program"),
        ELL_ESL("English Language Learner Services"),
        GIFTED_TALENTED("Gifted and Talented"),
        AT_RISK_INTERVENTION("At-Risk Intervention"),
        TITLE_I("Title I Services"),
        HOMELESS_SERVICES("McKinney-Vento Homeless Services"),
        FOSTER_CARE_SERVICES("Foster Care Services"),
        ACCESSIBILITY("Accessibility Accommodation"),
        ASSISTIVE_TECHNOLOGY("Assistive Technology"),
        TRANSPORTATION("Special Transportation"),
        OTHER("Other Accommodation");

        private final String displayName;

        AccommodationType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum AccommodationStatus {
        DRAFT("Draft"),
        PENDING_EVALUATION("Pending Evaluation"),
        ACTIVE("Active"),
        INACTIVE("Inactive"),
        EXPIRED("Expired"),
        UNDER_REVIEW("Under Review"),
        DISCONTINUED("Discontinued");

        private final String displayName;

        AccommodationStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum IEPPlacement {
        GENERAL_EDUCATION("General Education - Full Time"),
        GENERAL_ED_WITH_SUPPORT("General Education with Support Services"),
        RESOURCE_ROOM("Resource Room - Part Time"),
        SELF_CONTAINED("Self-Contained Classroom"),
        SPECIAL_SCHOOL("Special Education School"),
        HOME_HOSPITAL("Home/Hospital Instruction"),
        RESIDENTIAL("Residential Placement");

        private final String displayName;

        IEPPlacement(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ELLProficiencyLevel {
        LEVEL_1("Level 1 - Entering"),
        LEVEL_2("Level 2 - Emerging"),
        LEVEL_3("Level 3 - Developing"),
        LEVEL_4("Level 4 - Expanding"),
        LEVEL_5("Level 5 - Bridging"),
        LEVEL_6("Level 6 - Reaching/Proficient");

        private final String displayName;

        ELLProficiencyLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ELLServiceModel {
        PULL_OUT("Pull-Out ESL"),
        PUSH_IN("Push-In ESL"),
        CO_TEACHING("Co-Teaching Model"),
        SHELTERED_INSTRUCTION("Sheltered Instruction"),
        DUAL_LANGUAGE("Dual Language Program"),
        NEWCOMER_PROGRAM("Newcomer Program");

        private final String displayName;

        ELLServiceModel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum GiftedCategory {
        GENERAL_INTELLECTUAL("General Intellectual Ability"),
        SPECIFIC_ACADEMIC("Specific Academic Aptitude"),
        CREATIVE_THINKING("Creative Thinking"),
        LEADERSHIP("Leadership Ability"),
        VISUAL_PERFORMING_ARTS("Visual/Performing Arts");

        private final String displayName;

        GiftedCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum RiskLevel {
        LOW("Low Risk"),
        MODERATE("Moderate Risk"),
        HIGH("High Risk"),
        SEVERE("Severe Risk - Immediate Intervention Required");

        private final String displayName;

        RiskLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum LunchStatus {
        FULL_PRICE("Full Price"),
        REDUCED_PRICE("Reduced Price"),
        FREE("Free"),
        NOT_PARTICIPATING("Not Participating");

        private final String displayName;

        LunchStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Check if accommodation is currently active
     */
    public boolean isActive() {
        return status == AccommodationStatus.ACTIVE &&
               (endDate == null || endDate.isAfter(LocalDate.now()));
    }

    /**
     * Check if review is overdue
     */
    public boolean isReviewOverdue() {
        return nextReviewDate != null && nextReviewDate.isBefore(LocalDate.now());
    }

    /**
     * Check if accommodation is expiring soon (within 30 days)
     */
    public boolean isExpiringSoon() {
        if (endDate == null) return false;
        return endDate.isAfter(LocalDate.now()) &&
               endDate.isBefore(LocalDate.now().plusDays(30));
    }

    /**
     * Get days until expiration
     */
    public long getDaysUntilExpiration() {
        if (endDate == null) return -1;
        return LocalDate.now().until(endDate, java.time.temporal.ChronoUnit.DAYS);
    }

    /**
     * Get summary of active services
     */
    public String getActiveServicesSummary() {
        StringBuilder summary = new StringBuilder();

        if (Boolean.TRUE.equals(has504Plan)) {
            summary.append("504 Plan, ");
        }
        if (Boolean.TRUE.equals(hasIEP)) {
            summary.append("IEP, ");
        }
        if (Boolean.TRUE.equals(isELL)) {
            summary.append("ELL/ESL, ");
        }
        if (Boolean.TRUE.equals(isGifted)) {
            summary.append("Gifted, ");
        }
        if (Boolean.TRUE.equals(titleIParticipating)) {
            summary.append("Title I, ");
        }

        if (summary.length() > 0) {
            return summary.substring(0, summary.length() - 2); // Remove trailing comma
        }
        return "No active services";
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
