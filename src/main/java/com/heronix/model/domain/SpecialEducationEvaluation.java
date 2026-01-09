package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Special Education Evaluation Entity
 * Tracks initial evaluations, re-evaluations, and the 60-day evaluation timeline
 * Supports IDEA compliance and consent tracking
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Entity
@Table(name = "special_education_evaluations", indexes = {
    @Index(name = "idx_eval_student", columnList = "student_id"),
    @Index(name = "idx_eval_type", columnList = "evaluation_type"),
    @Index(name = "idx_eval_status", columnList = "status"),
    @Index(name = "idx_eval_due_date", columnList = "due_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialEducationEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iep_id")
    private IEP iep;

    // Evaluation Type & Timeline
    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation_type", nullable = false, length = 50)
    private EvaluationType evaluationType;

    @Column(name = "referral_date", nullable = false)
    private LocalDate referralDate;

    @Column(name = "referral_source", length = 200)
    private String referralSource;

    @Column(name = "referral_reason", columnDefinition = "TEXT")
    private String referralReason;

    // 60-Day Timeline Tracking
    @Column(name = "consent_sent_date")
    private LocalDate consentSentDate;

    @Column(name = "consent_received_date")
    private LocalDate consentReceivedDate;

    @Column(name = "timeline_start_date")
    private LocalDate timelineStartDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "timeline_days_remaining")
    private Integer timelineDaysRemaining;

    @Column(name = "timeline_paused")
    @Builder.Default
    private Boolean timelinePaused = false;

    @Column(name = "timeline_pause_reason", columnDefinition = "TEXT")
    private String timelinePauseReason;

    // Evaluation Process
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private EvaluationStatus status = EvaluationStatus.REFERRAL_RECEIVED;

    @ElementCollection
    @CollectionTable(name = "evaluation_assessments", joinColumns = @JoinColumn(name = "evaluation_id"))
    @Column(name = "assessment", length = 200)
    @Builder.Default
    private List<String> assessmentsPlanned = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "evaluation_assessments_completed", joinColumns = @JoinColumn(name = "evaluation_id"))
    @Column(name = "assessment", length = 200)
    @Builder.Default
    private List<String> assessmentsCompleted = new ArrayList<>();

    // Team Members
    @ElementCollection
    @CollectionTable(name = "evaluation_team", joinColumns = @JoinColumn(name = "evaluation_id"))
    @Column(name = "team_member", length = 200)
    @Builder.Default
    private List<String> evaluationTeam = new ArrayList<>();

    @Column(name = "case_manager", length = 100)
    private String caseManager;

    // Eligibility Determination
    @Column(name = "evaluation_completed_date")
    private LocalDate evaluationCompletedDate;

    @Column(name = "eligibility_meeting_date")
    private LocalDate eligibilityMeetingDate;

    @Column(name = "eligible_for_services")
    private Boolean eligibleForServices;

    @Enumerated(EnumType.STRING)
    @Column(name = "eligibility_category", length = 100)
    private EligibilityCategory eligibilityCategory;

    @Column(name = "primary_disability", length = 100)
    private String primaryDisability;

    @Column(name = "secondary_disability", length = 100)
    private String secondaryDisability;

    // Results & Recommendations
    @Column(name = "evaluation_summary", columnDefinition = "TEXT")
    private String evaluationSummary;

    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths;

    @Column(name = "areas_of_concern", columnDefinition = "TEXT")
    private String areasOfConcern;

    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    // Parent Involvement
    @Column(name = "parent_consent_for_evaluation")
    private Boolean parentConsentForEvaluation;

    @Column(name = "parent_attended_eligibility_meeting")
    private Boolean parentAttendedEligibilityMeeting;

    @Column(name = "parent_input", columnDefinition = "TEXT")
    private String parentInput;

    @Column(name = "interpreter_required")
    @Builder.Default
    private Boolean interpreterRequired = false;

    @Column(name = "interpreter_language", length = 50)
    private String interpreterLanguage;

    // Documents
    @Column(name = "evaluation_report_path", length = 500)
    private String evaluationReportPath;

    @Column(name = "consent_form_path", length = 500)
    private String consentFormPath;

    @Column(name = "eligibility_determination_path", length = 500)
    private String eligibilityDeterminationPath;

    @Column(name = "pwn_path", length = 500)
    private String pwnPath;

    // Next Steps
    @Column(name = "next_triennial_due_date")
    private LocalDate nextTriennialDueDate;

    @Column(name = "iep_developed")
    @Builder.Default
    private Boolean iepDeveloped = false;

    @Column(name = "services_started_date")
    private LocalDate servicesStartedDate;

    // Compliance
    @Column(name = "completed_within_timeline")
    private Boolean completedWithinTimeline;

    @Column(name = "compliance_notes", columnDefinition = "TEXT")
    private String complianceNotes;

    // Audit Fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public enum EvaluationType {
        INITIAL("Initial Evaluation"),
        TRIENNIAL("Triennial Re-evaluation"),
        RE_EVALUATION("Re-evaluation"),
        INDEPENDENT("Independent Educational Evaluation"),
        MANIFESTATION_DETERMINATION("Manifestation Determination"),
        TRANSITION_ASSESSMENT("Transition Assessment"),
        FUNCTIONAL_BEHAVIOR("Functional Behavior Assessment");

        private final String displayName;

        EvaluationType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum EvaluationStatus {
        REFERRAL_RECEIVED("Referral Received"),
        CONSENT_PENDING("Consent Pending"),
        CONSENT_RECEIVED("Consent Received"),
        ASSESSMENTS_IN_PROGRESS("Assessments In Progress"),
        ASSESSMENTS_COMPLETED("Assessments Completed"),
        REPORT_BEING_WRITTEN("Report Being Written"),
        ELIGIBILITY_MEETING_SCHEDULED("Eligibility Meeting Scheduled"),
        ELIGIBILITY_DETERMINED("Eligibility Determined"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled"),
        OVERDUE("Overdue");

        private final String displayName;

        EvaluationStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum EligibilityCategory {
        SPECIFIC_LEARNING_DISABILITY("Specific Learning Disability"),
        SPEECH_LANGUAGE_IMPAIRMENT("Speech/Language Impairment"),
        OTHER_HEALTH_IMPAIRMENT("Other Health Impairment"),
        AUTISM("Autism Spectrum Disorder"),
        EMOTIONAL_DISTURBANCE("Emotional Disturbance"),
        INTELLECTUAL_DISABILITY("Intellectual Disability"),
        MULTIPLE_DISABILITIES("Multiple Disabilities"),
        ORTHOPEDIC_IMPAIRMENT("Orthopedic Impairment"),
        TRAUMATIC_BRAIN_INJURY("Traumatic Brain Injury"),
        VISUAL_IMPAIRMENT("Visual Impairment"),
        HEARING_IMPAIRMENT("Hearing Impairment"),
        DEAFNESS("Deafness"),
        DEAF_BLINDNESS("Deaf-Blindness"),
        DEVELOPMENTAL_DELAY("Developmental Delay"),
        NOT_ELIGIBLE("Not Eligible");

        private final String displayName;

        EligibilityCategory(String displayName) {
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
        updateTimelineDaysRemaining();
    }

    // Helper Methods

    @Transient
    public boolean isOverdue() {
        return status == EvaluationStatus.OVERDUE ||
               (dueDate != null && LocalDate.now().isAfter(dueDate) &&
                status != EvaluationStatus.COMPLETED && status != EvaluationStatus.CANCELLED);
    }

    @Transient
    public boolean needsConsentFollowUp() {
        return status == EvaluationStatus.CONSENT_PENDING &&
               consentSentDate != null &&
               LocalDate.now().minusDays(7).isAfter(consentSentDate);
    }

    @Transient
    public boolean hasParentConsent() {
        return parentConsentForEvaluation != null && parentConsentForEvaluation;
    }

    @Transient
    public int getDaysInProcess() {
        if (timelineStartDate == null) return 0;
        LocalDate end = evaluationCompletedDate != null ? evaluationCompletedDate : LocalDate.now();
        return (int) (end.toEpochDay() - timelineStartDate.toEpochDay());
    }

    @Transient
    public void updateTimelineDaysRemaining() {
        if (dueDate == null || timelinePaused) {
            timelineDaysRemaining = null;
            return;
        }
        timelineDaysRemaining = (int) (dueDate.toEpochDay() - LocalDate.now().toEpochDay());
    }

    @Transient
    public boolean isWithinTimeline() {
        if (dueDate == null) return true;
        return !LocalDate.now().isAfter(dueDate);
    }

    @Transient
    public Double getAssessmentCompletionPercentage() {
        if (assessmentsPlanned == null || assessmentsPlanned.isEmpty()) return 0.0;
        if (assessmentsCompleted == null) return 0.0;
        return (double) assessmentsCompleted.size() / assessmentsPlanned.size() * 100.0;
    }

    @Transient
    public boolean isReadyForEligibilityMeeting() {
        return status == EvaluationStatus.ASSESSMENTS_COMPLETED ||
               status == EvaluationStatus.REPORT_BEING_WRITTEN;
    }

    @Transient
    public boolean needsIEP() {
        return eligibleForServices != null && eligibleForServices && !iepDeveloped;
    }

    @Transient
    public int getDaysUntilDue() {
        if (dueDate == null) return -1;
        return (int) (dueDate.toEpochDay() - LocalDate.now().toEpochDay());
    }

    @Transient
    public boolean isUrgent() {
        int daysUntilDue = getDaysUntilDue();
        return daysUntilDue >= 0 && daysUntilDue <= 7;
    }
}
