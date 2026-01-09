package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Disciplinary Referral Entity
 *
 * Represents a formal disciplinary referral from a teacher to an administrator.
 * Created when a behavior incident requires administrative intervention beyond
 * classroom management.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Entity
@Table(name = "disciplinary_referrals", indexes = {
    @Index(name = "idx_referral_student", columnList = "student_id"),
    @Index(name = "idx_referral_date", columnList = "referral_date"),
    @Index(name = "idx_referral_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisciplinaryReferral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Related behavior incident (optional - can create referral without incident record)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "behavior_incident_id")
    private BehaviorIncident behaviorIncident;

    /**
     * Student being referred
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * Teacher making the referral
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referring_teacher_id", nullable = false)
    private Teacher referringTeacher;

    /**
     * Administrator assigned to handle referral
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_administrator_id")
    private Teacher assignedAdministrator;

    /**
     * Course where incident occurred (if applicable)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    /**
     * Date of referral submission
     */
    @Column(name = "referral_date", nullable = false)
    private LocalDate referralDate;

    /**
     * Date of the incident being referred
     */
    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    /**
     * Current status of referral
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ReferralStatus status = ReferralStatus.PENDING;

    /**
     * Priority level
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    @Builder.Default
    private Priority priority = Priority.NORMAL;

    /**
     * Reason for referral
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "referral_reason", nullable = false)
    private ReferralReason referralReason;

    /**
     * Detailed description of incident
     */
    @Column(name = "incident_description", columnDefinition = "TEXT", nullable = false)
    private String incidentDescription;

    /**
     * Actions already taken by teacher
     */
    @Column(name = "prior_interventions", columnDefinition = "TEXT")
    private String priorInterventions;

    /**
     * Witnesses to the incident
     */
    @Column(name = "witnesses", columnDefinition = "TEXT")
    private String witnesses;

    /**
     * Parent already contacted?
     */
    @Column(name = "parent_contacted")
    @Builder.Default
    private Boolean parentContacted = false;

    /**
     * Method of parent contact
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "parent_contact_method")
    private BehaviorIncident.ContactMethod parentContactMethod;

    /**
     * Date parent was contacted
     */
    @Column(name = "parent_contact_date")
    private LocalDate parentContactDate;

    /**
     * Administrative review notes
     */
    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    /**
     * Date reviewed by administrator
     */
    @Column(name = "reviewed_date")
    private LocalDate reviewedDate;

    /**
     * Final disposition/outcome
     */
    @Column(name = "disposition", columnDefinition = "TEXT")
    private String disposition;

    /**
     * Follow-up actions required
     */
    @Column(name = "follow_up_actions", columnDefinition = "TEXT")
    private String followUpActions;

    /**
     * Date follow-up completed
     */
    @Column(name = "follow_up_completed_date")
    private LocalDate followUpCompletedDate;

    /**
     * Campus where incident occurred
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id")
    private Campus campus;

    /**
     * Timestamp when referral was created
     */
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Timestamp when referral was last updated
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Additional Referral Details
    @Column(name = "referral_time")
    private LocalTime referralTime;

    @Column(name = "class_period", length = 50)
    private String classPeriod;

    @Column(name = "interventions_attempted", columnDefinition = "TEXT")
    private String interventionsAttempted;

    @Column(name = "has_related_incidents")
    @Builder.Default
    private Boolean hasRelatedIncidents = false;

    @Column(name = "related_incidents", columnDefinition = "TEXT")
    private String relatedIncidents;

    @Column(name = "previous_suspension")
    @Builder.Default
    private Boolean previousSuspension = false;

    @Column(name = "on_behavior_contract")
    @Builder.Default
    private Boolean onBehaviorContract = false;

    @Column(name = "has_behavior_plan")
    @Builder.Default
    private Boolean hasBehaviorPlan = false;

    @Column(name = "recommended_action", columnDefinition = "TEXT")
    private String recommendedAction;

    @Column(name = "parent_response", columnDefinition = "TEXT")
    private String parentResponse;

    // ========================================================================
    // ENUMS
    // ========================================================================

    public enum ReferralStatus {
        DRAFT,             // Draft referral not yet submitted
        PENDING,           // Awaiting administrator review
        UNDER_REVIEW,      // Administrator actively reviewing
        RESOLVED,          // Action taken, referral complete
        CLOSED,            // Closed without action
        ESCALATED,         // Escalated to higher authority
        PENDING_HEARING    // Formal hearing scheduled
    }

    public enum Priority {
        LOW,               // Can be handled within a few days
        NORMAL,            // Standard priority
        HIGH,              // Requires attention within 24 hours
        URGENT             // Immediate attention required
    }

    public enum ReferralReason {
        CHRONIC_DISRUPTION,
        DEFIANCE,
        FIGHTING,
        BULLYING,
        HARASSMENT,
        THREATENING_BEHAVIOR,
        WEAPON_POSSESSION,
        DRUG_ALCOHOL,
        THEFT,
        VANDALISM,
        TECHNOLOGY_MISUSE,
        ACADEMIC_DISHONESTY,
        CHRONIC_TARDINESS,
        TRUANCY,
        INSUBORDINATION,
        INAPPROPRIATE_LANGUAGE,
        PHYSICAL_AGGRESSION,
        SEXUAL_HARASSMENT,
        OTHER
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    @Transient
    public boolean isPending() {
        return status == ReferralStatus.PENDING;
    }

    @Transient
    public boolean isResolved() {
        return status == ReferralStatus.RESOLVED || status == ReferralStatus.CLOSED;
    }

    @Transient
    public boolean requiresUrgentAttention() {
        return priority == Priority.URGENT || priority == Priority.HIGH;
    }

    @Transient
    public int getDaysSinceReferral() {
        return (int) (LocalDate.now().toEpochDay() - referralDate.toEpochDay());
    }

    // ========================================================================
    // JPA LIFECYCLE CALLBACKS
    // ========================================================================

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ========================================================================
    // ALIAS METHODS FOR BOOLEAN FIELD COMPATIBILITY
    // ========================================================================

    public Boolean isHasRelatedIncidents() {
        return hasRelatedIncidents;
    }

    public Boolean isPreviousSuspension() {
        return previousSuspension;
    }

    public Boolean isOnBehaviorContract() {
        return onBehaviorContract;
    }

    public Boolean isHasBehaviorPlan() {
        return hasBehaviorPlan;
    }

    public Boolean isParentContacted() {
        return parentContacted;
    }
}
