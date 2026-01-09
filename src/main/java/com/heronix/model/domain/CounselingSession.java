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
 * Counseling Session Entity
 * Tracks individual, group, and crisis counseling sessions
 * Supports academic, personal, social-emotional, and career counseling
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Entity
@Table(name = "counseling_sessions", indexes = {
    @Index(name = "idx_counseling_session_student", columnList = "student_id"),
    @Index(name = "idx_counseling_session_counselor", columnList = "counselor_id"),
    @Index(name = "idx_counseling_session_date", columnList = "session_date"),
    @Index(name = "idx_counseling_session_type", columnList = "session_type"),
    @Index(name = "idx_counseling_session_status", columnList = "session_status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CounselingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id", nullable = false)
    private Teacher counselor;

    // Session Details
    @Column(name = "session_number", length = 50)
    private String sessionNumber;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "session_time")
    private LocalTime sessionTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false, length = 30)
    private SessionType sessionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_format", nullable = false, length = 30)
    @Builder.Default
    private SessionFormat sessionFormat = SessionFormat.INDIVIDUAL;

    @Column(name = "group_name", length = 200)
    private String groupName;

    @Column(name = "group_size")
    private Integer groupSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_status", nullable = false, length = 30)
    @Builder.Default
    private SessionStatus sessionStatus = SessionStatus.SCHEDULED;

    // Topic and Focus
    @Enumerated(EnumType.STRING)
    @Column(name = "primary_focus", length = 30)
    private CounselingFocus primaryFocus;

    @Column(name = "topic", length = 300)
    private String topic;

    @Column(name = "presenting_concern", columnDefinition = "TEXT")
    private String presentingConcern;

    // Academic Counseling Specific
    @Column(name = "courses_discussed", columnDefinition = "TEXT")
    private String coursesDiscussed;

    @Column(name = "schedule_changes_made")
    @Builder.Default
    private Boolean scheduleChangesMade = false;

    @Column(name = "graduation_plan_reviewed")
    @Builder.Default
    private Boolean graduationPlanReviewed = false;

    @Column(name = "credits_discussed")
    @Builder.Default
    private Boolean creditsDiscussed = false;

    @Column(name = "college_planning_discussed")
    @Builder.Default
    private Boolean collegePlanningDiscussed = false;

    // Career Counseling Specific
    @Column(name = "career_assessment_administered")
    @Builder.Default
    private Boolean careerAssessmentAdministered = false;

    @Column(name = "career_interests_explored")
    @Builder.Default
    private Boolean careerInterestsExplored = false;

    @Column(name = "career_pathways_discussed", columnDefinition = "TEXT")
    private String careerPathwaysDiscussed;

    // Session Content
    @Column(name = "session_notes", columnDefinition = "TEXT")
    private String sessionNotes;

    @Column(name = "interventions_used", columnDefinition = "TEXT")
    private String interventionsUsed;

    @Column(name = "student_response", columnDefinition = "TEXT")
    private String studentResponse;

    @Column(name = "student_goals", columnDefinition = "TEXT")
    private String studentGoals;

    @Column(name = "action_steps", columnDefinition = "TEXT")
    private String actionSteps;

    // Crisis Indicators
    @Column(name = "crisis_situation")
    @Builder.Default
    private Boolean crisisSituation = false;

    @Column(name = "safety_concerns")
    @Builder.Default
    private Boolean safetyConcerns = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 30)
    private RiskLevel riskLevel;

    @Column(name = "safety_plan_created")
    @Builder.Default
    private Boolean safetyPlanCreated = false;

    @Column(name = "parent_notified")
    @Builder.Default
    private Boolean parentNotified = false;

    @Column(name = "parent_notification_date")
    private LocalDate parentNotificationDate;

    @Column(name = "administration_notified")
    @Builder.Default
    private Boolean administrationNotified = false;

    // Referrals and Follow-up
    @Column(name = "referral_made")
    @Builder.Default
    private Boolean referralMade = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "referral_type", length = 30)
    private ReferralType referralType;

    @Column(name = "referral_to", length = 200)
    private String referralTo;

    @Column(name = "referral_reason", columnDefinition = "TEXT")
    private String referralReason;

    @Column(name = "follow_up_needed")
    @Builder.Default
    private Boolean followUpNeeded = false;

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    @Column(name = "follow_up_notes", columnDefinition = "TEXT")
    private String followUpNotes;

    // Attendance
    @Column(name = "student_attended")
    @Builder.Default
    private Boolean studentAttended = true;

    @Column(name = "absence_reason", length = 200)
    private String absenceReason;

    @Column(name = "parent_attended")
    @Builder.Default
    private Boolean parentAttended = false;

    @Column(name = "other_attendees", columnDefinition = "TEXT")
    private String otherAttendees;

    // Documentation
    @Column(name = "confidential_notes", columnDefinition = "TEXT")
    private String confidentialNotes;

    @Column(name = "parent_contact_attempted")
    @Builder.Default
    private Boolean parentContactAttempted = false;

    @Column(name = "parent_contact_successful")
    @Builder.Default
    private Boolean parentContactSuccessful = false;

    @Column(name = "consent_obtained")
    @Builder.Default
    private Boolean consentObtained = false;

    // Linked Records
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_referral_id")
    private CounselingReferral relatedReferral;

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

    // Additional Session Details
    @Column(name = "duration")
    private Integer duration;

    @Column(name = "secondary_focus", columnDefinition = "TEXT")
    private String secondaryFocus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referral_id")
    private CounselingReferral referral;

    // Observations and Notes
    @Enumerated(EnumType.STRING)
    @Column(name = "mood_observed", length = 30)
    private Mood moodObserved;

    @Enumerated(EnumType.STRING)
    @Column(name = "engagement_level", length = 30)
    private EngagementLevel engagementLevel;

    @Column(name = "progress_notes", columnDefinition = "TEXT")
    private String progressNotes;

    @Column(name = "behavioral_observations", columnDefinition = "TEXT")
    private String behavioralObservations;

    // Risk Assessment Fields
    @Column(name = "suicidal_ideation")
    @Builder.Default
    private Boolean suicidalIdeation = false;

    @Column(name = "self_harm_risk")
    @Builder.Default
    private Boolean selfHarmRisk = false;

    @Column(name = "harm_to_others_risk")
    @Builder.Default
    private Boolean harmToOthersRisk = false;

    @Column(name = "substance_use_disclosed")
    @Builder.Default
    private Boolean substanceUseDisclosed = false;

    @Column(name = "abuse_disclosed")
    @Builder.Default
    private Boolean abuseDisclosed = false;

    @Column(name = "safety_plan_reviewed")
    @Builder.Default
    private Boolean safetyPlanReviewed = false;

    @Column(name = "risk_details", columnDefinition = "TEXT")
    private String riskDetails;

    // Interventions and Goals
    @Column(name = "goals_addressed", columnDefinition = "TEXT")
    private String goalsAddressed;

    @Column(name = "homework_assigned", columnDefinition = "TEXT")
    private String homeworkAssigned;

    // Collaboration and Communication
    @Column(name = "external_referral_made")
    @Builder.Default
    private Boolean externalReferralMade = false;

    @Column(name = "parent_contacted")
    @Builder.Default
    private Boolean parentContacted = false;

    @Column(name = "teacher_consultation")
    @Builder.Default
    private Boolean teacherConsultation = false;

    @Column(name = "admin_notified")
    @Builder.Default
    private Boolean adminNotified = false;

    @Column(name = "crisis_team_involved")
    @Builder.Default
    private Boolean crisisTeamInvolved = false;

    @Column(name = "emergency_contact_made")
    @Builder.Default
    private Boolean emergencyContactMade = false;

    @Column(name = "collaboration_details", columnDefinition = "TEXT")
    private String collaborationDetails;

    // Clinical Documentation
    @Column(name = "clinical_impressions", columnDefinition = "TEXT")
    private String clinicalImpressions;

    @Column(name = "next_session_plan", columnDefinition = "TEXT")
    private String nextSessionPlan;

    public enum SessionType {
        INITIAL_CONSULTATION("Initial Consultation"),
        FOLLOW_UP("Follow-Up Session"),
        ACADEMIC_ADVISING("Academic Advising"),
        COURSE_SELECTION("Course Selection"),
        SCHEDULE_CHANGE("Schedule Change Consultation"),
        CREDIT_RECOVERY("Credit Recovery Planning"),
        GRADUATION_PLANNING("Graduation Planning"),
        COLLEGE_PLANNING("College Planning"),
        CAREER_COUNSELING("Career Counseling"),
        PERSONAL_COUNSELING("Personal/Social Counseling"),
        CRISIS_INTERVENTION("Crisis Intervention"),
        CONFLICT_RESOLUTION("Conflict Resolution"),
        BEHAVIORAL_SUPPORT("Behavioral Support"),
        SOCIAL_SKILLS("Social Skills Development"),
        GROUP_COUNSELING("Group Counseling Session"),
        PARENT_CONFERENCE("Parent Conference"),
        CHECK_IN("Brief Check-In"),
        OTHER("Other");

        private final String displayName;

        SessionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum SessionFormat {
        INDIVIDUAL("Individual"),
        GROUP("Group"),
        FAMILY("Family"),
        PARENT_ONLY("Parent Only"),
        VIRTUAL("Virtual/Remote"),
        PHONE("Phone Call");

        private final String displayName;

        SessionFormat(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum SessionStatus {
        DRAFT("Draft"),
        SCHEDULED("Scheduled"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled"),
        NO_SHOW("Student No-Show"),
        RESCHEDULED("Rescheduled"),
        IN_PROGRESS("In Progress");

        private final String displayName;

        SessionStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum CounselingFocus {
        ACADEMIC("Academic Performance"),
        BEHAVIORAL("Behavioral Concerns"),
        SOCIAL_EMOTIONAL("Social-Emotional"),
        MENTAL_HEALTH("Mental Health"),
        CRISIS("Crisis/Safety"),
        COLLEGE_CAREER("College/Career Planning"),
        ATTENDANCE("Attendance Issues"),
        PEER_RELATIONSHIPS("Peer Relationships"),
        FAMILY_ISSUES("Family Issues"),
        GRIEF_LOSS("Grief/Loss"),
        ANXIETY_STRESS("Anxiety/Stress Management"),
        SELF_ESTEEM("Self-Esteem"),
        BULLYING("Bullying/Harassment"),
        SUBSTANCE_USE("Substance Use"),
        OTHER("Other");

        private final String displayName;

        CounselingFocus(String displayName) {
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

    public enum ReferralType {
        EXTERNAL_THERAPIST("External Therapist"),
        PSYCHIATRIST("Psychiatrist"),
        COMMUNITY_MENTAL_HEALTH("Community Mental Health Center"),
        HOSPITAL("Hospital/Emergency Services"),
        SOCIAL_SERVICES("Social Services"),
        SUBSTANCE_ABUSE("Substance Abuse Treatment"),
        CRISIS_HOTLINE("Crisis Hotline"),
        ACADEMIC_SUPPORT("Academic Support Services"),
        SPECIAL_EDUCATION("Special Education Evaluation"),
        ADMINISTRATION("School Administration"),
        OTHER("Other Referral");

        private final String displayName;

        ReferralType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum SessionFocus {
        ACADEMIC_PERFORMANCE("Academic Performance"),
        BEHAVIORAL_CONCERNS("Behavioral Concerns"),
        SOCIAL_EMOTIONAL("Social-Emotional Development"),
        MENTAL_HEALTH("Mental Health"),
        CRISIS_SAFETY("Crisis/Safety"),
        COLLEGE_CAREER("College/Career Planning"),
        ATTENDANCE("Attendance Issues"),
        PEER_RELATIONSHIPS("Peer Relationships"),
        FAMILY_ISSUES("Family Issues"),
        GRIEF_LOSS("Grief/Loss"),
        ANXIETY_STRESS("Anxiety/Stress Management"),
        SELF_ESTEEM("Self-Esteem"),
        BULLYING("Bullying/Harassment"),
        SUBSTANCE_USE("Substance Use"),
        OTHER("Other");

        private final String displayName;

        SessionFocus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Mood {
        HAPPY("Happy/Positive"),
        CALM("Calm/Relaxed"),
        NEUTRAL("Neutral"),
        ANXIOUS("Anxious/Worried"),
        SAD("Sad/Depressed"),
        ANGRY("Angry/Frustrated"),
        WITHDRAWN("Withdrawn"),
        AGITATED("Agitated/Irritable"),
        FEARFUL("Fearful"),
        VARIABLE("Variable/Fluctuating");

        private final String displayName;

        Mood(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum EngagementLevel {
        HIGHLY_ENGAGED("Highly Engaged"),
        ENGAGED("Engaged"),
        MODERATELY_ENGAGED("Moderately Engaged"),
        MINIMALLY_ENGAGED("Minimally Engaged"),
        RESISTANT("Resistant"),
        WITHDRAWN("Withdrawn"),
        UNABLE_TO_ASSESS("Unable to Assess");

        private final String displayName;

        EngagementLevel(String displayName) {
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
    public boolean isCompleted() {
        return sessionStatus == SessionStatus.COMPLETED;
    }

    @Transient
    public boolean isCrisis() {
        return crisisSituation || riskLevel == RiskLevel.HIGH ||
               riskLevel == RiskLevel.IMMINENT;
    }

    @Transient
    public boolean needsFollowUp() {
        return followUpNeeded && (followUpDate == null ||
               followUpDate.isAfter(LocalDate.now()));
    }

    @Transient
    public boolean isHighRisk() {
        return riskLevel == RiskLevel.HIGH || riskLevel == RiskLevel.IMMINENT;
    }

    @Transient
    public boolean isGroupSession() {
        return sessionFormat == SessionFormat.GROUP;
    }

    @Transient
    public boolean isAcademicFocus() {
        return sessionType == SessionType.ACADEMIC_ADVISING ||
               sessionType == SessionType.COURSE_SELECTION ||
               sessionType == SessionType.GRADUATION_PLANNING ||
               primaryFocus == CounselingFocus.ACADEMIC;
    }

    @Transient
    public boolean needsParentNotification() {
        return (crisisSituation || isHighRisk()) && !parentNotified;
    }

    @Transient
    public int getDaysSinceSession() {
        if (sessionDate == null) return -1;
        return (int) (LocalDate.now().toEpochDay() - sessionDate.toEpochDay());
    }

    // ========================================================================
    // ALIAS METHODS FOR BOOLEAN FIELD COMPATIBILITY
    // ========================================================================

    public Boolean isSuicidalIdeation() {
        return suicidalIdeation;
    }

    public Boolean isSelfHarmRisk() {
        return selfHarmRisk;
    }

    public Boolean isHarmToOthersRisk() {
        return harmToOthersRisk;
    }

    public Boolean isSubstanceUseDisclosed() {
        return substanceUseDisclosed;
    }

    public Boolean isAbuseDisclosed() {
        return abuseDisclosed;
    }

    public Boolean isSafetyPlanReviewed() {
        return safetyPlanReviewed;
    }

    public Boolean isExternalReferralMade() {
        return externalReferralMade;
    }

    public Boolean isParentContacted() {
        return parentContacted;
    }

    public Boolean isTeacherConsultation() {
        return teacherConsultation;
    }

    public Boolean isAdminNotified() {
        return adminNotified;
    }

    public Boolean isCrisisTeamInvolved() {
        return crisisTeamInvolved;
    }

    public Boolean isEmergencyContactMade() {
        return emergencyContactMade;
    }

    public void setStatus(SessionStatus status) {
        this.sessionStatus = status;
    }
}
