package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Disciplinary Consequence Entity
 *
 * Represents a consequence assigned to a student for a disciplinary infraction.
 * Can be linked to a referral or created independently.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Entity
@Table(name = "disciplinary_consequences", indexes = {
    @Index(name = "idx_consequence_student", columnList = "student_id"),
    @Index(name = "idx_consequence_type", columnList = "consequence_type"),
    @Index(name = "idx_consequence_start_date", columnList = "start_date"),
    @Index(name = "idx_consequence_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisciplinaryConsequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Related disciplinary referral (optional)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referral_id")
    private DisciplinaryReferral referral;

    /**
     * Related behavior incident (optional)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "behavior_incident_id")
    private BehaviorIncident behaviorIncident;

    /**
     * Student receiving consequence
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * Administrator who assigned consequence
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_id", nullable = false)
    private Teacher assignedBy;

    /**
     * Type of consequence
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "consequence_type", nullable = false)
    private ConsequenceType consequenceType;

    /**
     * Start date of consequence
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * End date of consequence
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Number of days/hours (for detention, suspension, community service)
     */
    @Column(name = "duration")
    private Integer duration;

    /**
     * Current status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ConsequenceStatus status = ConsequenceStatus.ASSIGNED;

    /**
     * Description/details of consequence
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Behavioral expectations/conditions
     */
    @Column(name = "conditions", columnDefinition = "TEXT")
    private String conditions;

    /**
     * Parent notification sent?
     */
    @Column(name = "parent_notified")
    @Builder.Default
    private Boolean parentNotified = false;

    /**
     * Date parent was notified
     */
    @Column(name = "parent_notification_date")
    private LocalDate parentNotificationDate;

    /**
     * Appeal filed?
     */
    @Column(name = "appeal_filed")
    @Builder.Default
    private Boolean appealFiled = false;

    /**
     * Date appeal was filed
     */
    @Column(name = "appeal_date")
    private LocalDate appealDate;

    /**
     * Appeal outcome
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "appeal_outcome")
    private AppealOutcome appealOutcome;

    /**
     * Date consequence was completed
     */
    @Column(name = "completion_date")
    private LocalDate completionDate;

    /**
     * Verification that consequence was served
     */
    @Column(name = "verified_completed")
    @Builder.Default
    private Boolean verifiedCompleted = false;

    /**
     * Supervisor who verified completion
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_id")
    private Teacher verifiedBy;

    /**
     * Notes about completion/compliance
     */
    @Column(name = "completion_notes", columnDefinition = "TEXT")
    private String completionNotes;

    /**
     * For community service: hours completed
     */
    @Column(name = "hours_completed")
    private Integer hoursCompleted;

    /**
     * For community service: organization/location
     */
    @Column(name = "service_location")
    private String serviceLocation;

    /**
     * For behavioral contract: contract document path
     */
    @Column(name = "contract_document_path")
    private String contractDocumentPath;

    /**
     * For restitution: amount owed
     */
    @Column(name = "restitution_amount")
    private Double restitutionAmount;

    /**
     * For restitution: amount paid
     */
    @Column(name = "restitution_paid")
    @Builder.Default
    private Double restitutionPaid = 0.0;

    /**
     * Campus
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id")
    private Campus campus;

    /**
     * Created timestamp
     */
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Updated timestamp
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========================================================================
    // ENUMS
    // ========================================================================

    public enum ConsequenceType {
        WARNING,                    // Verbal or written warning
        LUNCH_DETENTION,           // Detention during lunch
        AFTER_SCHOOL_DETENTION,    // After school detention
        SATURDAY_SCHOOL,           // Saturday detention
        IN_SCHOOL_SUSPENSION,      // ISS - remain on campus
        OUT_OF_SCHOOL_SUSPENSION,  // OSS - suspended from campus
        EXPULSION,                 // Permanent removal
        COMMUNITY_SERVICE,         // Required community service hours
        BEHAVIOR_CONTRACT,         // Formal behavior improvement contract
        PARENT_CONFERENCE,         // Required parent meeting
        COUNSELING_REFERRAL,       // Mandatory counseling sessions
        RESTITUTION,               // Financial or property restitution
        LOSS_OF_PRIVILEGES,        // Restrictions (no extracurriculars, etc.)
        ALTERNATIVE_PLACEMENT,     // Alternative education program
        PROBATION,                 // Disciplinary probation
        OTHER                      // Other consequence type
    }

    public enum ConsequenceStatus {
        ASSIGNED,          // Consequence assigned but not yet started
        IN_PROGRESS,       // Currently serving consequence
        COMPLETED,         // Consequence fully served
        PARTIALLY_SERVED,  // Some but not all served
        APPEALED,          // Under appeal review
        OVERTURNED,        // Appeal successful, consequence reversed
        MODIFIED,          // Consequence modified after appeal
        EXPIRED,           // Consequence expired/time limit passed
        WAIVED             // Consequence waived by administration
    }

    public enum AppealOutcome {
        PENDING,           // Appeal under review
        UPHELD,            // Original consequence upheld
        OVERTURNED,        // Consequence overturned
        REDUCED,           // Consequence reduced
        MODIFIED           // Consequence modified
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    @Transient
    public boolean isActive() {
        return status == ConsequenceStatus.ASSIGNED || status == ConsequenceStatus.IN_PROGRESS;
    }

    @Transient
    public boolean isCompleted() {
        return status == ConsequenceStatus.COMPLETED;
    }

    @Transient
    public boolean requiresTracking() {
        return consequenceType == ConsequenceType.COMMUNITY_SERVICE ||
               consequenceType == ConsequenceType.RESTITUTION ||
               consequenceType == ConsequenceType.BEHAVIOR_CONTRACT;
    }

    @Transient
    public int getDaysRemaining() {
        if (endDate == null) return 0;
        LocalDate today = LocalDate.now();
        if (today.isAfter(endDate)) return 0;
        return (int) (endDate.toEpochDay() - today.toEpochDay());
    }

    @Transient
    public double getRestitutionBalance() {
        if (restitutionAmount == null) return 0.0;
        if (restitutionPaid == null) return restitutionAmount;
        return restitutionAmount - restitutionPaid;
    }

    @Transient
    public boolean isRestitutionPaid() {
        return getRestitutionBalance() <= 0.0;
    }

    // ========================================================================
    // JPA LIFECYCLE CALLBACKS
    // ========================================================================

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
