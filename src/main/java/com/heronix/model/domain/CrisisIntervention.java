package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Crisis Intervention Entity
 * Tracks crisis situations, suicide risk assessments, and threat assessments
 * Documents safety plans and emergency interventions
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Entity
@Table(name = "crisis_interventions", indexes = {
    @Index(name = "idx_crisis_intervention_student", columnList = "student_id"),
    @Index(name = "idx_crisis_intervention_date", columnList = "crisis_date"),
    @Index(name = "idx_crisis_intervention_type", columnList = "crisis_type"),
    @Index(name = "idx_crisis_intervention_risk", columnList = "risk_level"),
    @Index(name = "idx_crisis_intervention_counselor", columnList = "responding_counselor_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrisisIntervention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responding_counselor_id")
    private Teacher respondingCounselor;

    // Crisis Details
    @Column(name = "crisis_number", length = 50)
    private String crisisNumber;

    @Column(name = "crisis_date", nullable = false)
    private LocalDate crisisDate;

    @Column(name = "crisis_time")
    private LocalTime crisisTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "crisis_type", nullable = false, length = 50)
    private CrisisType crisisType;

    @Enumerated(EnumType.STRING)
    @Column(name = "crisis_severity", nullable = false, length = 30)
    @Builder.Default
    private CrisisSeverity crisisSeverity = CrisisSeverity.MODERATE;

    @Column(name = "crisis_location", length = 200)
    private String crisisLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "location", length = 50)
    private CrisisLocation location;

    @Column(name = "location_details", columnDefinition = "TEXT")
    private String locationDetails;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity_level", length = 30)
    private SeverityLevel severityLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "response_status", length = 30)
    private ResponseStatus responseStatus;

    @Column(name = "presenting_crisis", columnDefinition = "TEXT")
    private String presentingCrisis;

    @Column(name = "presenting_problem", columnDefinition = "TEXT")
    private String presentingProblem;

    @Column(name = "crisis_description", columnDefinition = "TEXT")
    private String crisisDescription;

    @Column(name = "precipitating_events", columnDefinition = "TEXT")
    private String precipitatingEvents;

    // Referral Source
    @Enumerated(EnumType.STRING)
    @Column(name = "referral_source", length = 30)
    private ReferralSource referralSource;

    @Column(name = "referring_person_name", length = 200)
    private String referringPersonName;

    // Suicide Risk Assessment
    @Column(name = "suicide_risk_assessment")
    @Builder.Default
    private Boolean suicideRiskAssessment = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "suicide_risk_level", length = 30)
    private SuicideRiskLevel suicideRiskLevel;

    @Column(name = "suicidal_ideation")
    @Builder.Default
    private Boolean suicidalIdeation = false;

    @Column(name = "suicide_plan")
    @Builder.Default
    private Boolean suicidePlan = false;

    @Column(name = "suicide_plan_details", columnDefinition = "TEXT")
    private String suicidePlanDetails;

    @Column(name = "suicide_means_access")
    @Builder.Default
    private Boolean suicideMeansAccess = false;

    @Column(name = "suicide_means_description", columnDefinition = "TEXT")
    private String suicideMeansDescription;

    @Column(name = "previous_suicide_attempts")
    @Builder.Default
    private Boolean previousSuicideAttempts = false;

    @Column(name = "previous_attempts_description", columnDefinition = "TEXT")
    private String previousAttemptsDescription;

    @Column(name = "current_intent")
    @Builder.Default
    private Boolean currentIntent = false;

    @Column(name = "protective_factors", columnDefinition = "TEXT")
    private String protectiveFactors;

    @Column(name = "risk_factors", columnDefinition = "TEXT")
    private String riskFactors;

    @Column(name = "immediate_danger_to_self")
    @Builder.Default
    private Boolean immediateDangerToSelf = false;

    @Column(name = "immediate_danger_to_others")
    @Builder.Default
    private Boolean immediateDangerToOthers = false;

    @Column(name = "has_suicide_plan")
    @Builder.Default
    private Boolean hasSuicidePlan = false;

    @Column(name = "has_access_to_means")
    @Builder.Default
    private Boolean hasAccessToMeans = false;

    @Column(name = "previous_attempt")
    @Builder.Default
    private Boolean previousAttempt = false;

    @Column(name = "substance_involvement")
    @Builder.Default
    private Boolean substanceInvolvement = false;

    @Column(name = "mental_health_history")
    @Builder.Default
    private Boolean mentalHealthHistory = false;

    @Column(name = "recent_loss_trauma")
    @Builder.Default
    private Boolean recentLossTrauma = false;

    @Column(name = "risk_assessment_details", columnDefinition = "TEXT")
    private String riskAssessmentDetails;

    // Self-Harm
    @Column(name = "self_harm_behavior")
    @Builder.Default
    private Boolean selfHarmBehavior = false;

    @Column(name = "self_harm_description", columnDefinition = "TEXT")
    private String selfHarmDescription;

    @Column(name = "recent_self_harm")
    @Builder.Default
    private Boolean recentSelfHarm = false;

    @Column(name = "self_harm_date")
    private LocalDate selfHarmDate;

    // Threat Assessment
    @Column(name = "threat_assessment_conducted")
    @Builder.Default
    private Boolean threatAssessmentConducted = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "threat_level", length = 30)
    private ThreatLevel threatLevel;

    @Column(name = "threat_to_others")
    @Builder.Default
    private Boolean threatToOthers = false;

    @Column(name = "specific_target_identified")
    @Builder.Default
    private Boolean specificTargetIdentified = false;

    @Column(name = "target_description", columnDefinition = "TEXT")
    private String targetDescription;

    @Column(name = "violent_ideation")
    @Builder.Default
    private Boolean violentIdeation = false;

    @Column(name = "plan_to_harm_others")
    @Builder.Default
    private Boolean planToHarmOthers = false;

    @Column(name = "harm_plan_details", columnDefinition = "TEXT")
    private String harmPlanDetails;

    @Column(name = "weapon_access")
    @Builder.Default
    private Boolean weaponAccess = false;

    @Column(name = "weapon_description", columnDefinition = "TEXT")
    private String weaponDescription;

    @Column(name = "previous_violent_behavior")
    @Builder.Default
    private Boolean previousViolentBehavior = false;

    // Mental Status
    @Column(name = "mental_status_notes", columnDefinition = "TEXT")
    private String mentalStatusNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "affect", length = 30)
    private Affect affect;

    @Enumerated(EnumType.STRING)
    @Column(name = "mood", length = 30)
    private Mood mood;

    @Column(name = "psychotic_symptoms")
    @Builder.Default
    private Boolean psychoticSymptoms = false;

    @Column(name = "disoriented_confused")
    @Builder.Default
    private Boolean disorientedConfused = false;

    @Column(name = "substance_influence")
    @Builder.Default
    private Boolean substanceInfluence = false;

    @Column(name = "substance_details", columnDefinition = "TEXT")
    private String substanceDetails;

    // Risk Level and Safety
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 30)
    @Builder.Default
    private RiskLevel riskLevel = RiskLevel.MODERATE;

    @Column(name = "imminent_danger")
    @Builder.Default
    private Boolean imminentDanger = false;

    @Column(name = "safety_concerns", columnDefinition = "TEXT")
    private String safetyConcerns;

    // Safety Plan
    @Column(name = "safety_plan_created")
    @Builder.Default
    private Boolean safetyPlanCreated = false;

    @Column(name = "safety_plan_date")
    private LocalDate safetyPlanDate;

    @Column(name = "warning_signs", columnDefinition = "TEXT")
    private String warningSigns;

    @Column(name = "coping_strategies", columnDefinition = "TEXT")
    private String copingStrategies;

    @Column(name = "support_contacts", columnDefinition = "TEXT")
    private String supportContacts;

    @Column(name = "professional_resources", columnDefinition = "TEXT")
    private String professionalResources;

    @Column(name = "crisis_hotline_provided")
    @Builder.Default
    private Boolean crisisHotlineProvided = false;

    @Column(name = "environment_made_safe")
    @Builder.Default
    private Boolean environmentMadeSafe = false;

    @Column(name = "means_restriction_discussed")
    @Builder.Default
    private Boolean meansRestrictionDiscussed = false;

    // Immediate Interventions
    @Column(name = "interventions_provided", columnDefinition = "TEXT")
    private String interventionsProvided;

    @Column(name = "de_escalation_techniques_used", columnDefinition = "TEXT")
    private String deEscalationTechniquesUsed;

    @Column(name = "student_response_to_intervention", columnDefinition = "TEXT")
    private String studentResponseToIntervention;

    @Column(name = "student_stabilized")
    @Builder.Default
    private Boolean studentStabilized = false;

    @Column(name = "admin_notified")
    @Builder.Default
    private Boolean adminNotified = false;

    @Column(name = "crisis_team_activated")
    @Builder.Default
    private Boolean crisisTeamActivated = false;

    @Column(name = "weapons_means_removed")
    @Builder.Default
    private Boolean weaponsMeansRemoved = false;

    @Column(name = "one_to_one_supervision")
    @Builder.Default
    private Boolean oneToOneSupervision = false;

    @Column(name = "de_escalation_used")
    @Builder.Default
    private Boolean deEscalationUsed = false;

    @Column(name = "student_isolated")
    @Builder.Default
    private Boolean studentIsolated = false;

    @Column(name = "physical_restraint_used")
    @Builder.Default
    private Boolean physicalRestraintUsed = false;

    @Column(name = "intervention_actions", columnDefinition = "TEXT")
    private String interventionActions;

    // Emergency Response
    @Column(name = "emergency_services_called")
    @Builder.Default
    private Boolean emergencyServicesCalled = false;

    @Column(name = "ambulance_transport")
    @Builder.Default
    private Boolean ambulanceTransport = false;

    @Column(name = "police_involved")
    @Builder.Default
    private Boolean policeInvolved = false;

    @Column(name = "hospital_transport")
    @Builder.Default
    private Boolean hospitalTransport = false;

    @Column(name = "hospital_name", length = 200)
    private String hospitalName;

    @Column(name = "hospitalization_voluntary")
    @Builder.Default
    private Boolean hospitalizationVoluntary = false;

    @Column(name = "psychiatric_hold")
    @Builder.Default
    private Boolean psychiatricHold = false;

    // Parent/Guardian Notification
    @Column(name = "parent_notified")
    @Builder.Default
    private Boolean parentNotified = false;

    @Column(name = "parent_notification_time")
    private LocalDateTime parentNotificationTime;

    @Column(name = "parent_response", columnDefinition = "TEXT")
    private String parentResponse;

    @Column(name = "parent_refused_services")
    @Builder.Default
    private Boolean parentRefusedServices = false;

    @Column(name = "parent_on_site")
    @Builder.Default
    private Boolean parentOnSite = false;

    @Column(name = "parent_picked_up_student")
    @Builder.Default
    private Boolean parentPickedUpStudent = false;

    @Column(name = "parent_contact_time")
    private LocalTime parentContactTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "parent_contact_method", length = 30)
    private ContactMethod parentContactMethod;

    @Column(name = "parent_contacted", length = 200)
    private String parentContacted;

    // Administration Notification
    @Column(name = "administration_notified")
    @Builder.Default
    private Boolean administrationNotified = false;

    @Column(name = "administrator_name", length = 200)
    private String administratorName;

    @Column(name = "administration_notification_time")
    private LocalDateTime administrationNotificationTime;

    // Referrals and Follow-Up
    @Column(name = "referral_made")
    @Builder.Default
    private Boolean referralMade = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "referral_type", length = 50)
    private ExternalReferralType referralType;

    @Column(name = "referral_agency", length = 200)
    private String referralAgency;

    @Column(name = "referral_contact_person", length = 200)
    private String referralContactPerson;

    @Column(name = "referral_phone", length = 20)
    private String referralPhone;

    @Column(name = "referral_accepted")
    @Builder.Default
    private Boolean referralAccepted = false;

    @Column(name = "follow_up_required")
    @Builder.Default
    private Boolean followUpRequired = true;

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    @Column(name = "follow_up_completed")
    @Builder.Default
    private Boolean followUpCompleted = false;

    @Column(name = "follow_up_notes", columnDefinition = "TEXT")
    private String followUpNotes;

    @Column(name = "personnel_involved", columnDefinition = "TEXT")
    private String personnelInvolved;

    @Enumerated(EnumType.STRING)
    @Column(name = "immediate_outcome", length = 50)
    private Outcome immediateOutcome;

    @Column(name = "resolution_time")
    private LocalTime resolutionTime;

    @Column(name = "outcome_details", columnDefinition = "TEXT")
    private String outcomeDetails;

    @Column(name = "follow_up_plan", columnDefinition = "TEXT")
    private String followUpPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_case_manager_id")
    private Teacher assignedCaseManager;

    // Return to School
    @Column(name = "student_returned_to_class")
    @Builder.Default
    private Boolean studentReturnedToClass = false;

    @Column(name = "sent_home")
    @Builder.Default
    private Boolean sentHome = false;

    @Column(name = "clearance_required_to_return")
    @Builder.Default
    private Boolean clearanceRequiredToReturn = false;

    @Column(name = "clearance_received")
    @Builder.Default
    private Boolean clearanceReceived = false;

    @Column(name = "clearance_date")
    private LocalDate clearanceDate;

    @Column(name = "return_to_school_meeting_held")
    @Builder.Default
    private Boolean returnToSchoolMeetingHeld = false;

    @Column(name = "return_to_school_date")
    private LocalDate returnToSchoolDate;

    @Column(name = "re_entry_plan", columnDefinition = "TEXT")
    private String reEntryPlan;

    // Documentation
    @Column(name = "incident_report_completed")
    @Builder.Default
    private Boolean incidentReportCompleted = false;

    @Column(name = "incident_report_filed")
    @Builder.Default
    private Boolean incidentReportFiled = false;

    @Column(name = "mandated_report_filed")
    @Builder.Default
    private Boolean mandatedReportFiled = false;

    @Column(name = "district_notified")
    @Builder.Default
    private Boolean districtNotified = false;

    @Column(name = "safety_plan_documented")
    @Builder.Default
    private Boolean safetyPlanDocumented = false;

    @Column(name = "parent_release_signed")
    @Builder.Default
    private Boolean parentReleaseSigned = false;

    @Column(name = "photographs_taken")
    @Builder.Default
    private Boolean photographsTaken = false;

    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;

    @Column(name = "confidential_notes", columnDefinition = "TEXT")
    private String confidentialNotes;

    @Column(name = "staff_debriefing_conducted")
    @Builder.Default
    private Boolean staffDebriefingConducted = false;

    // Linked Records
    @OneToOne(mappedBy = "crisisIntervention", fetch = FetchType.LAZY)
    private CounselingReferral counselingReferral;

    @OneToOne(mappedBy = "crisisIntervention", fetch = FetchType.LAZY)
    private CounselingSession counselingSession;

    // Audit Fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public enum CrisisType {
        SUICIDAL_IDEATION("Suicidal Ideation"),
        SUICIDE_ATTEMPT("Suicide Attempt"),
        SELF_HARM("Self-Harm"),
        THREAT_TO_OTHERS("Threat to Harm Others"),
        VIOLENT_BEHAVIOR("Violent/Aggressive Behavior"),
        PSYCHOTIC_EPISODE("Psychotic Episode"),
        PANIC_ATTACK("Severe Panic/Anxiety Attack"),
        SUBSTANCE_OVERDOSE("Substance Overdose"),
        DOMESTIC_VIOLENCE("Domestic Violence Disclosure"),
        SEXUAL_ASSAULT("Sexual Assault Disclosure"),
        CHILD_ABUSE("Child Abuse Disclosure"),
        GRIEF_TRAUMA("Acute Grief/Trauma Response"),
        RUNAWAY("Runaway/Missing Student"),
        SCHOOL_THREAT("School Threat Assessment"),
        MEDICAL_EMERGENCY("Medical Emergency"),
        OTHER("Other Crisis");

        private final String displayName;

        CrisisType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum CrisisSeverity {
        MINOR("Minor"),
        MODERATE("Moderate"),
        SERIOUS("Serious"),
        SEVERE("Severe"),
        LIFE_THREATENING("Life-Threatening");

        private final String displayName;

        CrisisSeverity(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ReferralSource {
        TEACHER("Teacher"),
        PARENT("Parent/Guardian"),
        SELF_REFERRAL("Student Self-Referral"),
        PEER("Peer/Friend"),
        ADMINISTRATOR("Administrator"),
        SCHOOL_NURSE("School Nurse"),
        COUNSELOR("Counselor"),
        SOCIAL_WORKER("Social Worker"),
        SCHOOL_RESOURCE_OFFICER("School Resource Officer"),
        OUTSIDE_AGENCY("Outside Agency"),
        OTHER("Other");

        private final String displayName;

        ReferralSource(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum SuicideRiskLevel {
        NO_RISK("No Current Risk"),
        LOW("Low Risk"),
        MODERATE("Moderate Risk"),
        HIGH("High Risk"),
        IMMINENT("Imminent Risk");

        private final String displayName;

        SuicideRiskLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ThreatLevel {
        NO_THREAT("No Threat"),
        LOW("Low Threat"),
        MEDIUM("Medium Threat"),
        HIGH("High Threat"),
        IMMINENT("Imminent Threat");

        private final String displayName;

        ThreatLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Affect {
        FLAT("Flat"),
        BLUNTED("Blunted"),
        CONSTRICTED("Constricted"),
        APPROPRIATE("Appropriate"),
        LABILE("Labile"),
        EXPANSIVE("Expansive"),
        INAPPROPRIATE("Inappropriate");

        private final String displayName;

        Affect(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Mood {
        DEPRESSED("Depressed"),
        ANXIOUS("Anxious"),
        ANGRY("Angry/Hostile"),
        IRRITABLE("Irritable"),
        EUPHORIC("Euphoric"),
        DYSPHORIC("Dysphoric"),
        EUTHYMIC("Euthymic"),
        LABILE("Labile");

        private final String displayName;

        Mood(String displayName) {
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

    public enum ExternalReferralType {
        EMERGENCY_ROOM("Emergency Room"),
        PSYCHIATRIC_HOSPITAL("Psychiatric Hospital"),
        CRISIS_CENTER("Crisis Center"),
        OUTPATIENT_THERAPIST("Outpatient Therapist"),
        PSYCHIATRIST("Psychiatrist"),
        COMMUNITY_MENTAL_HEALTH("Community Mental Health Center"),
        CRISIS_HOTLINE("Crisis Hotline"),
        SUBSTANCE_ABUSE_TREATMENT("Substance Abuse Treatment"),
        DOMESTIC_VIOLENCE_SERVICES("Domestic Violence Services"),
        POLICE_SOCIAL_SERVICES("Police/Social Services"),
        MOBILE_CRISIS_TEAM("Mobile Crisis Team"),
        OTHER("Other");

        private final String displayName;

        ExternalReferralType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum CrisisLocation {
        CLASSROOM("Classroom"),
        HALLWAY("Hallway"),
        CAFETERIA("Cafeteria"),
        RESTROOM("Restroom"),
        GYM("Gymnasium"),
        LIBRARY("Library"),
        COUNSELING_OFFICE("Counseling Office"),
        ADMINISTRATIVE_OFFICE("Administrative Office"),
        PLAYGROUND("Playground/Outside"),
        PARKING_LOT("Parking Lot"),
        BUS("School Bus"),
        SPORTS_FIELD("Sports Field"),
        AUDITORIUM("Auditorium"),
        OTHER("Other Location");

        private final String displayName;

        CrisisLocation(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum SeverityLevel {
        MINOR("Minor"),
        MODERATE("Moderate"),
        SERIOUS("Serious"),
        SEVERE("Severe"),
        CRITICAL("Critical/Life-Threatening");

        private final String displayName;

        SeverityLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ResponseStatus {
        INITIATED("Intervention Initiated"),
        IN_PROGRESS("In Progress"),
        STABILIZED("Student Stabilized"),
        REFERRED("Referred to External Services"),
        HOSPITALIZED("Hospitalized"),
        SENT_HOME("Sent Home with Parent"),
        RETURNED_TO_CLASS("Returned to Class"),
        PENDING_FOLLOW_UP("Pending Follow-Up"),
        RESOLVED("Resolved");

        private final String displayName;

        ResponseStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ContactMethod {
        PHONE("Phone Call"),
        IN_PERSON("In Person"),
        EMAIL("Email"),
        TEXT_MESSAGE("Text Message"),
        LEFT_MESSAGE("Left Message"),
        NO_ANSWER("No Answer"),
        OTHER("Other");

        private final String displayName;

        ContactMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Outcome {
        STABILIZED("Student Stabilized"),
        REFERRED_EXTERNAL("Referred to External Services"),
        HOSPITALIZED("Hospitalized"),
        SENT_HOME("Sent Home with Parent"),
        RETURNED_TO_CLASS("Returned to Class"),
        POLICE_CUSTODY("Police Custody"),
        RUNAWAY("Student Ran Away/Left Campus"),
        ONGOING("Ongoing Intervention"),
        OTHER("Other Outcome");

        private final String displayName;

        Outcome(String displayName) {
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
    public boolean isHighRisk() {
        return riskLevel == RiskLevel.HIGH || riskLevel == RiskLevel.IMMINENT ||
               suicideRiskLevel == SuicideRiskLevel.HIGH ||
               suicideRiskLevel == SuicideRiskLevel.IMMINENT ||
               threatLevel == ThreatLevel.HIGH || threatLevel == ThreatLevel.IMMINENT;
    }

    @Transient
    public boolean isImminentRisk() {
        return riskLevel == RiskLevel.IMMINENT ||
               suicideRiskLevel == SuicideRiskLevel.IMMINENT ||
               threatLevel == ThreatLevel.IMMINENT ||
               imminentDanger;
    }

    @Transient
    public boolean isSuicideRisk() {
        return suicideRiskAssessment &&
               (suicideRiskLevel == SuicideRiskLevel.MODERATE ||
                suicideRiskLevel == SuicideRiskLevel.HIGH ||
                suicideRiskLevel == SuicideRiskLevel.IMMINENT);
    }

    @Transient
    public boolean isViolenceRisk() {
        return threatAssessmentConducted &&
               (threatLevel == ThreatLevel.MEDIUM ||
                threatLevel == ThreatLevel.HIGH ||
                threatLevel == ThreatLevel.IMMINENT);
    }

    @Transient
    public boolean needsEmergencyServices() {
        return isImminentRisk() && !emergencyServicesCalled;
    }

    @Transient
    public boolean needsParentNotification() {
        return !parentNotified && (isHighRisk() || emergencyServicesCalled);
    }

    @Transient
    public boolean needsFollowUp() {
        return followUpRequired && !followUpCompleted &&
               followUpDate != null &&
               (followUpDate.isBefore(LocalDate.now()) ||
                followUpDate.isEqual(LocalDate.now()));
    }

    @Transient
    public boolean needsClearanceToReturn() {
        return clearanceRequiredToReturn && !clearanceReceived;
    }

    @Transient
    public int getDaysSinceCrisis() {
        if (crisisDate == null) return -1;
        return (int) (LocalDate.now().toEpochDay() - crisisDate.toEpochDay());
    }
}
