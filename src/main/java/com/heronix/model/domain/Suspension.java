package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Suspension Entity
 *
 * Detailed tracking of student suspensions (both in-school and out-of-school).
 * Extends DisciplinaryConsequence with suspension-specific fields.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Entity
@Table(name = "suspensions", indexes = {
    @Index(name = "idx_suspension_student", columnList = "student_id"),
    @Index(name = "idx_suspension_type", columnList = "suspension_type"),
    @Index(name = "idx_suspension_dates", columnList = "start_date, end_date"),
    @Index(name = "idx_suspension_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Suspension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Related disciplinary consequence
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consequence_id")
    private DisciplinaryConsequence consequence;

    /**
     * Student being suspended
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * Administrator who issued suspension
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_by_id", nullable = false)
    private Teacher issuedBy;

    /**
     * Type of suspension
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "suspension_type", nullable = false)
    private SuspensionType suspensionType;

    /**
     * Start date of suspension
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * End date of suspension
     */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * Number of school days
     */
    @Column(name = "days_count", nullable = false)
    private Integer daysCount;

    /**
     * Current status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private SuspensionStatus status = SuspensionStatus.PENDING;

    /**
     * Reason for suspension
     */
    @Column(name = "suspension_reason", columnDefinition = "TEXT", nullable = false)
    private String suspensionReason;

    /**
     * Specific incident description
     */
    @Column(name = "incident_description", columnDefinition = "TEXT")
    private String incidentDescription;

    /**
     * ISS room assignment (for in-school suspension)
     */
    @Column(name = "iss_room_assignment")
    private String issRoomAssignment;

    /**
     * ISS supervisor
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iss_supervisor_id")
    private Teacher issSupervisor;

    /**
     * Alternative education placement (for long-term OSS)
     */
    @Column(name = "alternative_placement")
    private String alternativePlacement;

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
     * Parent notification method
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "parent_notification_method")
    private BehaviorIncident.ContactMethod parentNotificationMethod;

    /**
     * Parent notification document path
     */
    @Column(name = "notification_document_path")
    private String notificationDocumentPath;

    /**
     * Re-entry meeting required?
     */
    @Column(name = "reentry_meeting_required")
    @Builder.Default
    private Boolean reentryMeetingRequired = false;

    /**
     * Date of re-entry meeting
     */
    @Column(name = "reentry_meeting_date")
    private LocalDate reentryMeetingDate;

    /**
     * Re-entry meeting completed?
     */
    @Column(name = "reentry_meeting_completed")
    @Builder.Default
    private Boolean reentryMeetingCompleted = false;

    /**
     * Re-entry plan/conditions
     */
    @Column(name = "reentry_plan", columnDefinition = "TEXT")
    private String reentryPlan;

    /**
     * Homework/assignments provided during suspension?
     */
    @Column(name = "homework_provided")
    @Builder.Default
    private Boolean homeworkProvided = false;

    /**
     * Homework completion status
     */
    @Column(name = "homework_completed")
    @Builder.Default
    private Boolean homeworkCompleted = false;

    /**
     * Appeal filed?
     */
    @Column(name = "appeal_filed")
    @Builder.Default
    private Boolean appealFiled = false;

    /**
     * Appeal hearing date
     */
    @Column(name = "appeal_hearing_date")
    private LocalDate appealHearingDate;

    /**
     * Appeal outcome
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "appeal_outcome")
    private AppealOutcome appealOutcome;

    /**
     * Appeal decision notes
     */
    @Column(name = "appeal_decision", columnDefinition = "TEXT")
    private String appealDecision;

    /**
     * Expulsion recommended?
     */
    @Column(name = "expulsion_recommended")
    @Builder.Default
    private Boolean expulsionRecommended = false;

    /**
     * Expulsion hearing date
     */
    @Column(name = "expulsion_hearing_date")
    private LocalDate expulsionHearingDate;

    /**
     * Daily check-in notes (for ISS)
     */
    @Column(name = "daily_notes", columnDefinition = "TEXT")
    private String dailyNotes;

    /**
     * Attendance during suspension (for ISS)
     */
    @Column(name = "days_attended")
    private Integer daysAttended;

    /**
     * Completion verified?
     */
    @Column(name = "completion_verified")
    @Builder.Default
    private Boolean completionVerified = false;

    /**
     * Date verified complete
     */
    @Column(name = "completion_verification_date")
    private LocalDate completionVerificationDate;

    /**
     * Staff member who verified completion
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_id")
    private Teacher verifiedBy;

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

    public enum SuspensionType {
        IN_SCHOOL,          // ISS - student remains on campus
        OUT_OF_SCHOOL,      // OSS - student not allowed on campus
        EMERGENCY_REMOVAL,  // Immediate removal pending investigation
        EXTENDED_OSS        // Long-term OSS (10+ days)
    }

    public enum SuspensionStatus {
        PENDING,           // Suspension issued but not yet started
        ACTIVE,            // Currently serving suspension
        COMPLETED,         // Suspension completed
        APPEALED,          // Under appeal review
        REDUCED,           // Reduced after appeal
        OVERTURNED,        // Overturned on appeal
        CONVERTED_TO_ISS,  // OSS converted to ISS
        CONVERTED_TO_OSS,  // ISS converted to OSS
        EXTENDED,          // Extended due to violations
        EXPIRED            // Expired (student didn't serve)
    }

    public enum AppealOutcome {
        PENDING,           // Appeal under review
        UPHELD,            // Suspension upheld
        OVERTURNED,        // Suspension overturned
        REDUCED,           // Days reduced
        CONVERTED          // Type changed (ISS↔OSS)
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    @Transient
    public boolean isActive() {
        return status == SuspensionStatus.ACTIVE;
    }

    @Transient
    public boolean isCompleted() {
        return status == SuspensionStatus.COMPLETED;
    }

    @Transient
    public boolean isInSchool() {
        return suspensionType == SuspensionType.IN_SCHOOL;
    }

    @Transient
    public boolean isOutOfSchool() {
        return suspensionType == SuspensionType.OUT_OF_SCHOOL ||
               suspensionType == SuspensionType.EXTENDED_OSS;
    }

    @Transient
    public int getDaysRemaining() {
        if (endDate == null) return 0;
        LocalDate today = LocalDate.now();
        if (today.isAfter(endDate)) return 0;
        return (int) (endDate.toEpochDay() - today.toEpochDay()) + 1;
    }

    @Transient
    public int getDaysServed() {
        LocalDate today = LocalDate.now();
        LocalDate servedUntil = today.isBefore(endDate) ? today : endDate;
        if (servedUntil.isBefore(startDate)) return 0;
        return (int) (servedUntil.toEpochDay() - startDate.toEpochDay()) + 1;
    }

    @Transient
    public double getCompletionPercentage() {
        if (daysCount == null || daysCount == 0) return 0.0;
        return (double) getDaysServed() / daysCount * 100.0;
    }

    @Transient
    public boolean requiresReentryMeeting() {
        return reentryMeetingRequired && !reentryMeetingCompleted;
    }

    // ========================================================================
    // JPA LIFECYCLE CALLBACKS
    // ========================================================================

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
