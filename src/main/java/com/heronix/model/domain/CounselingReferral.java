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
 * Counseling Referral Entity
 * Tracks referrals for counseling services from various sources
 * Supports internal and external referrals with outcome tracking
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Entity
@Table(name = "counseling_referrals", indexes = {
    @Index(name = "idx_counseling_referral_student", columnList = "student_id"),
    @Index(name = "idx_counseling_referral_status", columnList = "referral_status"),
    @Index(name = "idx_counseling_referral_date", columnList = "referral_date"),
    @Index(name = "idx_counseling_referral_urgency", columnList = "urgency_level"),
    @Index(name = "idx_counseling_referral_type", columnList = "referral_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CounselingReferral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // Referral Details
    @Column(name = "referral_number", length = 50)
    private String referralNumber;

    @Column(name = "referral_date", nullable = false)
    private LocalDate referralDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "referral_source", nullable = false, length = 30)
    private ReferralSource referralSource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referring_teacher_id")
    private Teacher referringTeacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referring_staff_id")
    private Teacher referringStaff;

    @Column(name = "referring_person_name", length = 200)
    private String referringPersonName;

    @Column(name = "referring_person_relationship", length = 100)
    private String referringPersonRelationship;

    @Enumerated(EnumType.STRING)
    @Column(name = "referral_type", nullable = false, length = 50)
    private ReferralType referralType;

    @Enumerated(EnumType.STRING)
    @Column(name = "urgency_level", nullable = false, length = 30)
    @Builder.Default
    private UrgencyLevel urgencyLevel = UrgencyLevel.ROUTINE;

    @Enumerated(EnumType.STRING)
    @Column(name = "referral_status", nullable = false, length = 30)
    @Builder.Default
    private ReferralStatus referralStatus = ReferralStatus.PENDING;

    // Reason for Referral
    @Enumerated(EnumType.STRING)
    @Column(name = "primary_concern", length = 30)
    private ConcernArea primaryConcern;

    @Column(name = "referral_reason", columnDefinition = "TEXT")
    private String referralReason;

    @Column(name = "presenting_concerns", columnDefinition = "TEXT")
    private String presentingConcerns;

    @Column(name = "behavioral_observations", columnDefinition = "TEXT")
    private String behavioralObservations;

    @Column(name = "academic_concerns", columnDefinition = "TEXT")
    private String academicConcerns;

    @Column(name = "teacher_concerns", columnDefinition = "TEXT")
    private String teacherConcerns;

    @Column(name = "parent_concerns", columnDefinition = "TEXT")
    private String parentConcerns;

    @Column(name = "secondary_concerns", columnDefinition = "TEXT")
    private String secondaryConcerns;

    @Column(name = "student_strengths", columnDefinition = "TEXT")
    private String studentStrengths;

    @Column(name = "background_information", columnDefinition = "TEXT")
    private String backgroundInformation;

    // Previous Interventions
    @Column(name = "previous_interventions", columnDefinition = "TEXT")
    private String previousInterventions;

    @Column(name = "interventions_tried", columnDefinition = "TEXT")
    private String interventionsTried;

    @Column(name = "intervention_effectiveness", columnDefinition = "TEXT")
    private String interventionEffectiveness;

    // Assignment and Processing
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_counselor_id")
    private Teacher assignedCounselor;

    @Column(name = "assignment_date")
    private LocalDate assignmentDate;

    @Column(name = "intake_completed")
    @Builder.Default
    private Boolean intakeCompleted = false;

    @Column(name = "intake_date")
    private LocalDate intakeDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intake_counselor_id")
    private Teacher intakeCounselor;

    @Column(name = "intake_notes", columnDefinition = "TEXT")
    private String intakeNotes;

    // External Referral Information
    @Column(name = "external_provider_name", length = 200)
    private String externalProviderName;

    @Column(name = "external_provider_type", length = 100)
    private String externalProviderType;

    @Column(name = "external_provider_phone", length = 20)
    private String externalProviderPhone;

    @Column(name = "referral_sent_date")
    private LocalDate referralSentDate;

    @Column(name = "parent_consent_obtained")
    @Builder.Default
    private Boolean parentConsentObtained = false;

    @Column(name = "parent_consent_date")
    private LocalDate parentConsentDate;

    @Column(name = "release_of_information_signed")
    @Builder.Default
    private Boolean releaseOfInformationSigned = false;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    // Assessment and Screening
    @Column(name = "risk_assessment_completed")
    @Builder.Default
    private Boolean riskAssessmentCompleted = false;

    @Column(name = "risk_assessment_date")
    private LocalDate riskAssessmentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 30)
    private RiskLevel riskLevel;

    @Column(name = "suicide_risk_indicated")
    @Builder.Default
    private Boolean suicideRiskIndicated = false;

    @Column(name = "harm_to_others_indicated")
    @Builder.Default
    private Boolean harmToOthersIndicated = false;

    @Column(name = "immediate_safety_concerns")
    @Builder.Default
    private Boolean immediateSafetyConcerns = false;

    @Column(name = "safety_plan_created")
    @Builder.Default
    private Boolean safetyPlanCreated = false;

    @Column(name = "crisis_intervention_needed")
    @Builder.Default
    private Boolean crisisInterventionNeeded = false;

    @Column(name = "mandated_reporting")
    @Builder.Default
    private Boolean mandatedReporting = false;

    @Column(name = "risk_details", columnDefinition = "TEXT")
    private String riskDetails;

    // Services Provided
    @Column(name = "services_initiated")
    @Builder.Default
    private Boolean servicesInitiated = false;

    @Column(name = "services_start_date")
    private LocalDate servicesStartDate;

    @Column(name = "services_provided", columnDefinition = "TEXT")
    private String servicesProvided;

    @Column(name = "number_of_sessions")
    private Integer numberOfSessions;

    @Column(name = "interventions_implemented", columnDefinition = "TEXT")
    private String interventionsImplemented;

    // Parent Communication
    @Column(name = "parent_contacted")
    @Builder.Default
    private Boolean parentContacted = false;

    @Column(name = "parent_contact_date")
    private LocalDate parentContactDate;

    @Column(name = "parent_meeting_held")
    @Builder.Default
    private Boolean parentMeetingHeld = false;

    @Column(name = "parent_meeting_date")
    private LocalDate parentMeetingDate;

    @Column(name = "parent_response", columnDefinition = "TEXT")
    private String parentResponse;

    @Column(name = "parent_notes", columnDefinition = "TEXT")
    private String parentNotes;

    // Student History
    @Column(name = "previous_counseling")
    @Builder.Default
    private Boolean previousCounseling = false;

    @Column(name = "external_therapy")
    @Builder.Default
    private Boolean externalTherapy = false;

    @Column(name = "current_medication")
    @Builder.Default
    private Boolean currentMedication = false;

    @Column(name = "hospitalization_history")
    @Builder.Default
    private Boolean hospitalizationHistory = false;

    @Column(name = "has_iep_or_504")
    @Builder.Default
    private Boolean hasIepOr504 = false;

    @Column(name = "has_behavior_plan")
    @Builder.Default
    private Boolean hasBehaviorPlan = false;

    @Column(name = "previous_services_details", columnDefinition = "TEXT")
    private String previousServicesDetails;

    // Outcome and Closure
    @Column(name = "outcome", columnDefinition = "TEXT")
    private String outcome;

    @Column(name = "student_progress", columnDefinition = "TEXT")
    private String studentProgress;

    @Column(name = "goals_met")
    @Builder.Default
    private Boolean goalsMet = false;

    @Column(name = "improvement_noted")
    @Builder.Default
    private Boolean improvementNoted = false;

    @Column(name = "follow_up_needed")
    @Builder.Default
    private Boolean followUpNeeded = false;

    @Column(name = "follow_up_plan", columnDefinition = "TEXT")
    private String followUpPlan;

    @Column(name = "closure_date")
    private LocalDate closureDate;

    @Column(name = "closure_reason", length = 200)
    private String closureReason;

    @Column(name = "closure_notes", columnDefinition = "TEXT")
    private String closureNotes;

    // Additional Referrals
    @Column(name = "additional_referrals_made")
    @Builder.Default
    private Boolean additionalReferralsMade = false;

    @Column(name = "additional_referral_types", columnDefinition = "TEXT")
    private String additionalReferralTypes;

    // Documentation
    @Column(name = "documentation_complete")
    @Builder.Default
    private Boolean documentationComplete = false;

    @Column(name = "confidential_notes", columnDefinition = "TEXT")
    private String confidentialNotes;

    // Linked Records
    @OneToOne(mappedBy = "relatedReferral", fetch = FetchType.LAZY)
    private CounselingSession initialSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crisis_intervention_id")
    private CrisisIntervention crisisIntervention;

    // Audit Fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public enum ReferralSource {
        TEACHER("Teacher Referral"),
        PARENT("Parent/Guardian Request"),
        SELF_REFERRAL("Student Self-Referral"),
        ADMINISTRATOR("Administrator Referral"),
        COUNSELOR("Counselor Referral"),
        NURSE("School Nurse Referral"),
        SOCIAL_WORKER("Social Worker Referral"),
        COMMUNITY("Community Agency"),
        COURT("Court Ordered"),
        PHYSICIAN("Physician Referral"),
        OTHER("Other Source");

        private final String displayName;

        ReferralSource(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ReferralType {
        INTERNAL_COUNSELOR("Internal School Counselor"),
        SCHOOL_PSYCHOLOGIST("School Psychologist"),
        SCHOOL_SOCIAL_WORKER("School Social Worker"),
        EXTERNAL_THERAPIST("External Therapist/Counselor"),
        PSYCHIATRIST("Psychiatrist"),
        COMMUNITY_MENTAL_HEALTH("Community Mental Health Center"),
        CRISIS_SERVICES("Crisis Services"),
        HOSPITAL_PSYCHIATRIC("Hospital/Psychiatric Unit"),
        SUBSTANCE_ABUSE("Substance Abuse Treatment"),
        FAMILY_THERAPY("Family Therapy"),
        GROUP_THERAPY("Group Therapy"),
        CRISIS_HOTLINE("Crisis Hotline"),
        SOCIAL_SERVICES("Social Services/CPS"),
        ACADEMIC_SUPPORT("Academic Support Services"),
        SPECIAL_EDUCATION("Special Education Evaluation"),
        BEHAVIORAL_INTERVENTION("Behavioral Intervention Team"),
        THREAT_ASSESSMENT("Threat Assessment Team"),
        PEER_MEDIATION("Peer Mediation"),
        COLLEGE_CAREER("College/Career Counseling"),
        OTHER("Other Service");

        private final String displayName;

        ReferralType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum UrgencyLevel {
        ROUTINE("Routine - Within 2 Weeks"),
        MODERATE("Moderate - Within 1 Week"),
        URGENT("Urgent - Within 48 Hours"),
        EMERGENCY("Emergency - Immediate");

        private final String displayName;

        UrgencyLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ReferralStatus {
        PENDING("Pending Review"),
        ASSIGNED("Assigned to Counselor"),
        INTAKE_SCHEDULED("Intake Scheduled"),
        IN_PROGRESS("Services In Progress"),
        ON_HOLD("On Hold"),
        COMPLETED("Services Completed"),
        DECLINED("Student/Parent Declined"),
        CANCELLED("Referral Cancelled"),
        NO_SHOW("Student No-Show");

        private final String displayName;

        ReferralStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ConcernArea {
        ACADEMIC_PERFORMANCE("Academic Performance"),
        BEHAVIORAL_ISSUES("Behavioral Issues"),
        ATTENDANCE("Attendance Problems"),
        SOCIAL_EMOTIONAL("Social-Emotional Concerns"),
        MENTAL_HEALTH("Mental Health Concerns"),
        CRISIS_SAFETY("Crisis/Safety Concerns"),
        PEER_RELATIONSHIPS("Peer Relationship Issues"),
        FAMILY_ISSUES("Family/Home Issues"),
        GRIEF_LOSS("Grief/Loss"),
        TRAUMA("Trauma"),
        ANXIETY_DEPRESSION("Anxiety/Depression"),
        SELF_HARM("Self-Harm"),
        SUICIDAL_IDEATION("Suicidal Ideation"),
        SUBSTANCE_USE("Substance Use"),
        BULLYING_VICTIM("Bullying (Victim)"),
        BULLYING_PERPETRATOR("Bullying (Perpetrator)"),
        ANGER_MANAGEMENT("Anger Management"),
        CONFLICT("Conflict with Peers/Staff"),
        SELF_ESTEEM("Self-Esteem Issues"),
        ADJUSTMENT("Adjustment Difficulties"),
        COLLEGE_CAREER("College/Career Planning"),
        OTHER("Other Concern");

        private final String displayName;

        ConcernArea(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum RiskLevel {
        NONE("No Risk"),
        LOW("Low Risk"),
        MODERATE("Moderate Risk"),
        HIGH("High Risk"),
        IMMINENT("Imminent Risk");

        private final String displayName;

        RiskLevel(String displayName) {
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
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper Methods

    @Transient
    public boolean isPending() {
        return referralStatus == ReferralStatus.PENDING;
    }

    @Transient
    public boolean isActive() {
        return referralStatus == ReferralStatus.ASSIGNED ||
               referralStatus == ReferralStatus.INTAKE_SCHEDULED ||
               referralStatus == ReferralStatus.IN_PROGRESS;
    }

    @Transient
    public boolean isClosed() {
        return referralStatus == ReferralStatus.COMPLETED ||
               referralStatus == ReferralStatus.DECLINED ||
               referralStatus == ReferralStatus.CANCELLED;
    }

    // Manual getter methods for Boolean fields to provide "is" prefix
    public boolean isSuicideRiskIndicated() {
        return Boolean.TRUE.equals(suicideRiskIndicated);
    }

    public boolean isHarmToOthersIndicated() {
        return Boolean.TRUE.equals(harmToOthersIndicated);
    }

    public boolean isImmediateSafetyConcerns() {
        return Boolean.TRUE.equals(immediateSafetyConcerns);
    }

    public boolean isCrisisInterventionNeeded() {
        return Boolean.TRUE.equals(crisisInterventionNeeded);
    }

    public boolean isSafetyPlanCreated() {
        return Boolean.TRUE.equals(safetyPlanCreated);
    }

    public boolean isMandatedReporting() {
        return Boolean.TRUE.equals(mandatedReporting);
    }

    public boolean isParentContacted() {
        return Boolean.TRUE.equals(parentContacted);
    }

    public boolean isParentConsentObtained() {
        return Boolean.TRUE.equals(parentConsentObtained);
    }

    public boolean isParentMeetingHeld() {
        return Boolean.TRUE.equals(parentMeetingHeld);
    }

    public boolean isPreviousCounseling() {
        return Boolean.TRUE.equals(previousCounseling);
    }

    public boolean isExternalTherapy() {
        return Boolean.TRUE.equals(externalTherapy);
    }

    public boolean isCurrentMedication() {
        return Boolean.TRUE.equals(currentMedication);
    }

    public boolean isHospitalizationHistory() {
        return Boolean.TRUE.equals(hospitalizationHistory);
    }

    public boolean isHasIepOr504() {
        return Boolean.TRUE.equals(hasIepOr504);
    }

    public boolean isHasBehaviorPlan() {
        return Boolean.TRUE.equals(hasBehaviorPlan);
    }

    @Transient
    public boolean isHighPriority() {
        return urgencyLevel == UrgencyLevel.URGENT ||
               urgencyLevel == UrgencyLevel.EMERGENCY ||
               isSuicideRiskIndicated() ||
               isHarmToOthersIndicated() ||
               isImmediateSafetyConcerns();
    }

    @Transient
    public boolean isExternalReferral() {
        return referralType == ReferralType.EXTERNAL_THERAPIST ||
               referralType == ReferralType.PSYCHIATRIST ||
               referralType == ReferralType.COMMUNITY_MENTAL_HEALTH ||
               referralType == ReferralType.HOSPITAL_PSYCHIATRIC ||
               referralType == ReferralType.SUBSTANCE_ABUSE ||
               referralType == ReferralType.FAMILY_THERAPY;
    }

    @Transient
    public boolean needsParentConsent() {
        return isExternalReferral() && !parentConsentObtained;
    }

    @Transient
    public boolean needsRiskAssessment() {
        return !riskAssessmentCompleted && (
            primaryConcern == ConcernArea.SUICIDAL_IDEATION ||
            primaryConcern == ConcernArea.SELF_HARM ||
            primaryConcern == ConcernArea.CRISIS_SAFETY ||
            primaryConcern == ConcernArea.MENTAL_HEALTH
        );
    }

    @Transient
    public boolean needsParentContact() {
        return !isParentContacted() && (
            isHighPriority() ||
            isExternalReferral() ||
            isCrisisInterventionNeeded()
        );
    }

    @Transient
    public int getDaysSinceReferral() {
        if (referralDate == null) return -1;
        return (int) (LocalDate.now().toEpochDay() - referralDate.toEpochDay());
    }

    @Transient
    public boolean isOverdue() {
        if (isClosed() || referralDate == null) return false;
        int days = getDaysSinceReferral();

        return switch (urgencyLevel) {
            case EMERGENCY -> days > 0 && !servicesInitiated;
            case URGENT -> days > 2 && !servicesInitiated;
            case MODERATE -> days > 7 && !servicesInitiated;
            case ROUTINE -> days > 14 && !servicesInitiated;
        };
    }
}
