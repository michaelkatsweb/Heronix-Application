package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Social Work Case Entity
 * Tracks social work case management, home visits, and community referrals
 * Supports McKinney-Vento homeless services, foster care, and CPS coordination
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Entity
@Table(name = "social_work_cases", indexes = {
    @Index(name = "idx_social_work_case_student", columnList = "student_id"),
    @Index(name = "idx_social_work_case_status", columnList = "case_status"),
    @Index(name = "idx_social_work_case_type", columnList = "case_type"),
    @Index(name = "idx_social_work_case_social_worker", columnList = "social_worker_id"),
    @Index(name = "idx_social_work_case_priority", columnList = "priority_level")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialWorkCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "social_worker_id")
    private Teacher socialWorker; // Using Teacher entity for staff

    // Case Details
    @Column(name = "case_number", length = 50)
    private String caseNumber;

    @Column(name = "case_opened_date", nullable = false)
    private LocalDate caseOpenedDate;

    @Column(name = "case_closed_date")
    private LocalDate caseClosedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "case_type", nullable = false, length = 50)
    private CaseType caseType;

    @Enumerated(EnumType.STRING)
    @Column(name = "case_status", nullable = false, length = 30)
    @Builder.Default
    private CaseStatus caseStatus = CaseStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority_level", length = 30)
    @Builder.Default
    private PriorityLevel priorityLevel = PriorityLevel.MEDIUM;

    @Column(name = "presenting_issue", columnDefinition = "TEXT")
    private String presentingIssue;

    @Column(name = "case_summary", columnDefinition = "TEXT")
    private String caseSummary;

    // McKinney-Vento Homeless Services
    @Column(name = "mckinney_vento_eligible")
    @Builder.Default
    private Boolean mckinneyVentoEligible = false;

    @Column(name = "mckinney_vento_determination_date")
    private LocalDate mckinneyVentoDeterminationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "housing_situation", length = 50)
    private HousingSituation housingSituation;

    @Column(name = "living_arrangement_details", columnDefinition = "TEXT")
    private String livingArrangementDetails;

    @Column(name = "school_of_origin", length = 200)
    private String schoolOfOrigin;

    @Column(name = "transportation_provided")
    @Builder.Default
    private Boolean transportationProvided = false;

    @Column(name = "transportation_notes", columnDefinition = "TEXT")
    private String transportationNotes;

    @Column(name = "immediate_enrollment_completed")
    @Builder.Default
    private Boolean immediateEnrollmentCompleted = false;

    // Foster Care
    @Column(name = "in_foster_care")
    @Builder.Default
    private Boolean inFosterCare = false;

    @Column(name = "foster_care_placement_date")
    private LocalDate fosterCarePlacementDate;

    @Column(name = "foster_parent_name", length = 200)
    private String fosterParentName;

    @Column(name = "foster_parent_phone", length = 20)
    private String fosterParentPhone;

    @Column(name = "caseworker_name", length = 200)
    private String caseworkerName;

    @Column(name = "caseworker_agency", length = 200)
    private String caseworkerAgency;

    @Column(name = "caseworker_phone", length = 20)
    private String caseworkerPhone;

    @Column(name = "caseworker_email", length = 200)
    private String caseworkerEmail;

    @Column(name = "placement_type", length = 100)
    private String placementType;

    @Column(name = "sibling_placement")
    @Builder.Default
    private Boolean siblingPlacement = false;

    // CPS Involvement
    @Column(name = "cps_involvement")
    @Builder.Default
    private Boolean cpsInvolvement = false;

    @Column(name = "cps_case_open_date")
    private LocalDate cpsCaseOpenDate;

    @Column(name = "cps_case_number", length = 100)
    private String cpsCaseNumber;

    @Column(name = "cps_worker_name", length = 200)
    private String cpsWorkerName;

    @Column(name = "cps_worker_phone", length = 20)
    private String cpsWorkerPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "cps_investigation_status", length = 30)
    private CPSStatus cpsInvestigationStatus;

    @Column(name = "safety_plan_in_place")
    @Builder.Default
    private Boolean safetyPlanInPlace = false;

    @Column(name = "mandated_reporting_made")
    @Builder.Default
    private Boolean mandatedReportingMade = false;

    @Column(name = "mandated_report_date")
    private LocalDate mandatedReportDate;

    @Column(name = "report_made_by", length = 200)
    private String reportMadeBy;

    // Family Situation
    @Enumerated(EnumType.STRING)
    @Column(name = "family_structure", length = 30)
    private FamilyStructure familyStructure;

    @Column(name = "number_of_siblings")
    private Integer numberOfSiblings;

    @Column(name = "primary_caregiver", length = 200)
    private String primaryCaregiver;

    @Column(name = "primary_caregiver_relationship", length = 100)
    private String primaryCaregiverRelationship;

    @Column(name = "family_challenges", columnDefinition = "TEXT")
    private String familyChallenges;

    @Column(name = "financial_hardship")
    @Builder.Default
    private Boolean financialHardship = false;

    @Column(name = "housing_instability")
    @Builder.Default
    private Boolean housingInstability = false;

    @Column(name = "food_insecurity")
    @Builder.Default
    private Boolean foodInsecurity = false;

    @Column(name = "medical_needs_unmet")
    @Builder.Default
    private Boolean medicalNeedsUnmet = false;

    // Home Visits
    @Column(name = "home_visit_conducted")
    @Builder.Default
    private Boolean homeVisitConducted = false;

    @Column(name = "last_home_visit_date")
    private LocalDate lastHomeVisitDate;

    @Column(name = "number_of_home_visits")
    @Builder.Default
    private Integer numberOfHomeVisits = 0;

    @Column(name = "home_visit_findings", columnDefinition = "TEXT")
    private String homeVisitFindings;

    @Column(name = "home_environment_concerns")
    @Builder.Default
    private Boolean homeEnvironmentConcerns = false;

    // Community Resources and Referrals
    @Column(name = "community_referrals_made")
    @Builder.Default
    private Boolean communityReferralsMade = false;

    @Column(name = "referral_agencies", columnDefinition = "TEXT")
    private String referralAgencies;

    @Column(name = "food_bank_referral")
    @Builder.Default
    private Boolean foodBankReferral = false;

    @Column(name = "housing_assistance_referral")
    @Builder.Default
    private Boolean housingAssistanceReferral = false;

    @Column(name = "medical_services_referral")
    @Builder.Default
    private Boolean medicalServicesReferral = false;

    @Column(name = "mental_health_referral")
    @Builder.Default
    private Boolean mentalHealthReferral = false;

    @Column(name = "substance_abuse_services_referral")
    @Builder.Default
    private Boolean substanceAbuseServicesReferral = false;

    @Column(name = "domestic_violence_services_referral")
    @Builder.Default
    private Boolean domesticViolenceServicesReferral = false;

    @Column(name = "legal_aid_referral")
    @Builder.Default
    private Boolean legalAidReferral = false;

    @Column(name = "employment_services_referral")
    @Builder.Default
    private Boolean employmentServicesReferral = false;

    @Column(name = "utility_assistance_referral")
    @Builder.Default
    private Boolean utilityAssistanceReferral = false;

    @Column(name = "clothing_assistance_referral")
    @Builder.Default
    private Boolean clothingAssistanceReferral = false;

    // School Support Services
    @Column(name = "free_lunch_approved")
    @Builder.Default
    private Boolean freeLunchApproved = false;

    @Column(name = "school_supplies_provided")
    @Builder.Default
    private Boolean schoolSuppliesProvided = false;

    @Column(name = "backpack_program_enrolled")
    @Builder.Default
    private Boolean backpackProgramEnrolled = false;

    @Column(name = "after_school_program_enrolled")
    @Builder.Default
    private Boolean afterSchoolProgramEnrolled = false;

    @Column(name = "tutoring_services_provided")
    @Builder.Default
    private Boolean tutoringServicesProvided = false;

    @Column(name = "mentoring_program_enrolled")
    @Builder.Default
    private Boolean mentoringProgramEnrolled = false;

    // Multi-Agency Coordination
    @Column(name = "multi_agency_team_involved")
    @Builder.Default
    private Boolean multiAgencyTeamInvolved = false;

    @Column(name = "team_meeting_scheduled")
    @Builder.Default
    private Boolean teamMeetingScheduled = false;

    @Column(name = "last_team_meeting_date")
    private LocalDate lastTeamMeetingDate;

    @Column(name = "next_team_meeting_date")
    private LocalDate nextTeamMeetingDate;

    @Column(name = "agencies_involved", columnDefinition = "TEXT")
    private String agenciesInvolved;

    // Case Management
    @Column(name = "service_plan_developed")
    @Builder.Default
    private Boolean servicePlanDeveloped = false;

    @Column(name = "service_plan_date")
    private LocalDate servicePlanDate;

    @Column(name = "goals", columnDefinition = "TEXT")
    private String goals;

    @Column(name = "interventions", columnDefinition = "TEXT")
    private String interventions;

    @Column(name = "progress_notes", columnDefinition = "TEXT")
    private String progressNotes;

    @Column(name = "barriers_to_progress", columnDefinition = "TEXT")
    private String barriersToProgress;

    @Column(name = "follow_up_needed")
    @Builder.Default
    private Boolean followUpNeeded = false;

    @Column(name = "next_follow_up_date")
    private LocalDate nextFollowUpDate;

    @Column(name = "last_contact_date")
    private LocalDate lastContactDate;

    @Column(name = "contact_frequency", length = 50)
    private String contactFrequency; // Weekly, Bi-weekly, Monthly

    // Parent/Guardian Communication
    @Column(name = "parent_contact_established")
    @Builder.Default
    private Boolean parentContactEstablished = false;

    @Column(name = "parent_cooperation_level", length = 30)
    @Enumerated(EnumType.STRING)
    private CooperationLevel parentCooperationLevel;

    @Column(name = "language_barrier")
    @Builder.Default
    private Boolean languageBarrier = false;

    @Column(name = "interpreter_needed")
    @Builder.Default
    private Boolean interpreterNeeded = false;

    @Column(name = "preferred_language", length = 50)
    private String preferredLanguage;

    // Documentation and Privacy
    @Column(name = "consent_forms_signed")
    @Builder.Default
    private Boolean consentFormsSigned = false;

    @Column(name = "release_of_information_signed")
    @Builder.Default
    private Boolean releaseOfInformationSigned = false;

    @Column(name = "confidential_notes", columnDefinition = "TEXT")
    private String confidentialNotes;

    // Case Closure
    @Column(name = "closure_reason", columnDefinition = "TEXT")
    private String closureReason;

    @Column(name = "goals_achieved")
    @Builder.Default
    private Boolean goalsAchieved = false;

    @Column(name = "services_completed")
    @Builder.Default
    private Boolean servicesCompleted = false;

    @Column(name = "closure_summary", columnDefinition = "TEXT")
    private String closureSummary;

    // Audit Fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public enum CaseType {
        HOMELESS_SERVICES("McKinney-Vento Homeless Services"),
        FOSTER_CARE("Foster Care Coordination"),
        CPS_INVOLVEMENT("CPS/Child Welfare"),
        FAMILY_SUPPORT("Family Support Services"),
        BASIC_NEEDS("Basic Needs Assistance"),
        HOUSING_INSTABILITY("Housing Instability"),
        FOOD_INSECURITY("Food Insecurity"),
        ATTENDANCE_INTERVENTION("Attendance/Truancy Intervention"),
        BEHAVIORAL_SUPPORT("Behavioral Support"),
        MENTAL_HEALTH("Mental Health Services"),
        SUBSTANCE_ABUSE("Substance Abuse (Family)"),
        DOMESTIC_VIOLENCE("Domestic Violence Support"),
        MEDICAL_NEEDS("Medical/Health Needs"),
        IMMIGRANT_REFUGEE("Immigrant/Refugee Services"),
        TEEN_PARENT("Teen Parent Support"),
        GRIEF_TRAUMA("Grief/Trauma Support"),
        COMMUNITY_REFERRAL("Community Resource Referral"),
        CRISIS_INTERVENTION("Crisis Intervention"),
        OTHER("Other Social Services");

        private final String displayName;

        CaseType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum CaseStatus {
        OPEN("Open/Active"),
        ON_HOLD("On Hold"),
        PENDING_REFERRAL("Pending Referral"),
        SERVICES_IN_PROGRESS("Services In Progress"),
        CLOSED_SUCCESSFUL("Closed - Goals Met"),
        CLOSED_UNSUCCESSFUL("Closed - Goals Not Met"),
        TRANSFERRED("Transferred to Another Provider"),
        FAMILY_DECLINED("Family Declined Services"),
        STUDENT_WITHDREW("Student Withdrew from School");

        private final String displayName;

        CaseStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum PriorityLevel {
        LOW("Low Priority"),
        MEDIUM("Medium Priority"),
        HIGH("High Priority"),
        URGENT("Urgent"),
        CRISIS("Crisis");

        private final String displayName;

        PriorityLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum HousingSituation {
        DOUBLED_UP("Doubled Up with Another Family"),
        SHELTER("Emergency Shelter"),
        HOTEL_MOTEL("Hotel/Motel"),
        TRANSITIONAL_HOUSING("Transitional Housing"),
        UNSHELTERED("Unsheltered (Car, Park, Abandoned Building)"),
        AWAITING_FOSTER_CARE("Awaiting Foster Care Placement"),
        UNACCOMPANIED_YOUTH("Unaccompanied Youth"),
        STABLE_HOUSING("Stable Housing (Case Resolved)");

        private final String displayName;

        HousingSituation(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum CPSStatus {
        INVESTIGATION_OPEN("Investigation Open"),
        SUBSTANTIATED("Substantiated"),
        UNSUBSTANTIATED("Unsubstantiated"),
        SERVICES_OPEN("Services Open"),
        CLOSED("Case Closed"),
        PENDING("Pending Review");

        private final String displayName;

        CPSStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum FamilyStructure {
        TWO_PARENT("Two-Parent Household"),
        SINGLE_MOTHER("Single Mother"),
        SINGLE_FATHER("Single Father"),
        GRANDPARENT("Grandparent(s)"),
        OTHER_RELATIVE("Other Relative"),
        FOSTER_CARE("Foster Care"),
        GROUP_HOME("Group Home"),
        GUARDIAN("Legal Guardian"),
        UNACCOMPANIED("Unaccompanied Minor"),
        OTHER("Other");

        private final String displayName;

        FamilyStructure(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum CooperationLevel {
        HIGHLY_COOPERATIVE("Highly Cooperative"),
        COOPERATIVE("Cooperative"),
        SOMEWHAT_COOPERATIVE("Somewhat Cooperative"),
        MINIMALLY_COOPERATIVE("Minimally Cooperative"),
        UNCOOPERATIVE("Uncooperative"),
        UNABLE_TO_CONTACT("Unable to Contact");

        private final String displayName;

        CooperationLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (caseOpenedDate == null) {
            caseOpenedDate = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper Methods

    @Transient
    public boolean isActive() {
        return caseStatus == CaseStatus.OPEN ||
               caseStatus == CaseStatus.SERVICES_IN_PROGRESS ||
               caseStatus == CaseStatus.PENDING_REFERRAL;
    }

    @Transient
    public boolean isClosed() {
        return caseStatus == CaseStatus.CLOSED_SUCCESSFUL ||
               caseStatus == CaseStatus.CLOSED_UNSUCCESSFUL ||
               caseStatus == CaseStatus.TRANSFERRED ||
               caseStatus == CaseStatus.FAMILY_DECLINED ||
               caseStatus == CaseStatus.STUDENT_WITHDREW;
    }

    @Transient
    public boolean isHighPriority() {
        return priorityLevel == PriorityLevel.HIGH ||
               priorityLevel == PriorityLevel.URGENT ||
               priorityLevel == PriorityLevel.CRISIS;
    }

    @Transient
    public boolean needsFollowUp() {
        return followUpNeeded && nextFollowUpDate != null &&
               (nextFollowUpDate.isBefore(LocalDate.now()) ||
                nextFollowUpDate.isEqual(LocalDate.now()));
    }

    @Transient
    public boolean hasActiveChildWelfareInvolvement() {
        return (cpsInvolvement && cpsInvestigationStatus != CPSStatus.CLOSED) ||
               inFosterCare;
    }

    @Transient
    public boolean needsImmediateAttention() {
        return (priorityLevel == PriorityLevel.CRISIS ||
                priorityLevel == PriorityLevel.URGENT) &&
               isActive();
    }

    @Transient
    public int getDaysSinceCaseOpened() {
        if (caseOpenedDate == null) return -1;
        return (int) (LocalDate.now().toEpochDay() - caseOpenedDate.toEpochDay());
    }

    @Transient
    public int getDaysSinceLastContact() {
        if (lastContactDate == null) return -1;
        return (int) (LocalDate.now().toEpochDay() - lastContactDate.toEpochDay());
    }

    @Transient
    public boolean needsHomeVisit() {
        return !homeVisitConducted ||
               (lastHomeVisitDate != null &&
                lastHomeVisitDate.plusDays(90).isBefore(LocalDate.now()));
    }
}
