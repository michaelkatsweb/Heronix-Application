package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Professional Development Entity
 *
 * Tracks professional development courses, hours, CEUs, and continuing education
 * for teachers and staff. Manages PD requirements, enrollment, completion, and transcripts.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Entity
@Table(name = "professional_development", indexes = {
    @Index(name = "idx_pd_teacher", columnList = "teacher_id"),
    @Index(name = "idx_pd_status", columnList = "status"),
    @Index(name = "idx_pd_completion", columnList = "completion_date"),
    @Index(name = "idx_pd_type", columnList = "pd_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfessionalDevelopment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Teacher participating in PD
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    /**
     * Course/session title
     */
    @Column(name = "course_title", nullable = false)
    private String courseTitle;

    /**
     * Course code/ID
     */
    @Column(name = "course_code")
    private String courseCode;

    /**
     * Type of PD
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "pd_type", nullable = false)
    private PDType pdType;

    /**
     * Provider/organization offering the PD
     */
    @Column(name = "provider")
    private String provider;

    /**
     * Instructor/facilitator name
     */
    @Column(name = "instructor")
    private String instructor;

    /**
     * Course description
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Start date
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * End date
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Enrollment date
     */
    @Column(name = "enrollment_date")
    private LocalDate enrollmentDate;

    /**
     * Completion date
     */
    @Column(name = "completion_date")
    private LocalDate completionDate;

    /**
     * Current status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PDStatus status = PDStatus.ENROLLED;

    /**
     * PD hours earned
     */
    @Column(name = "hours_earned")
    private Double hoursEarned;

    /**
     * Contact hours (for in-person training)
     */
    @Column(name = "contact_hours")
    private Double contactHours;

    /**
     * Continuing Education Units (CEUs) earned
     */
    @Column(name = "ceus_earned")
    private Double ceusEarned;

    /**
     * Credit hours (for college courses)
     */
    @Column(name = "credit_hours")
    private Double creditHours;

    /**
     * Delivery method
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_method")
    private DeliveryMethod deliveryMethod;

    /**
     * Location (for in-person courses)
     */
    @Column(name = "location")
    private String location;

    /**
     * Online course URL
     */
    @Column(name = "course_url")
    private String courseUrl;

    /**
     * Attendance percentage
     */
    @Column(name = "attendance_percentage")
    private Double attendancePercentage;

    /**
     * Final grade/score
     */
    @Column(name = "final_grade")
    private String finalGrade;

    /**
     * Pass/fail status
     */
    @Column(name = "passed")
    private Boolean passed;

    /**
     * Certificate earned?
     */
    @Column(name = "certificate_earned")
    @Builder.Default
    private Boolean certificateEarned = false;

    /**
     * Certificate issue date
     */
    @Column(name = "certificate_issue_date")
    private LocalDate certificateIssueDate;

    /**
     * Certificate number
     */
    @Column(name = "certificate_number")
    private String certificateNumber;

    /**
     * Certificate document path
     */
    @Column(name = "certificate_document_path")
    private String certificateDocumentPath;

    /**
     * Transcript included?
     */
    @Column(name = "on_transcript")
    @Builder.Default
    private Boolean onTranscript = true;

    /**
     * National Board Certification related?
     */
    @Column(name = "national_board_related")
    @Builder.Default
    private Boolean nationalBoardRelated = false;

    /**
     * Counts toward certification renewal?
     */
    @Column(name = "counts_toward_renewal")
    @Builder.Default
    private Boolean countsTowardRenewal = true;

    /**
     * Required by state/district?
     */
    @Column(name = "required")
    @Builder.Default
    private Boolean required = false;

    /**
     * Deadline for completion (if required)
     */
    @Column(name = "deadline")
    private LocalDate deadline;

    /**
     * Cost of course
     */
    @Column(name = "cost")
    private Double cost;

    /**
     * Reimbursement requested?
     */
    @Column(name = "reimbursement_requested")
    @Builder.Default
    private Boolean reimbursementRequested = false;

    /**
     * Reimbursement amount
     */
    @Column(name = "reimbursement_amount")
    private Double reimbursementAmount;

    /**
     * Reimbursement approved?
     */
    @Column(name = "reimbursement_approved")
    private Boolean reimbursementApproved;

    /**
     * Reimbursement paid?
     */
    @Column(name = "reimbursement_paid")
    @Builder.Default
    private Boolean reimbursementPaid = false;

    /**
     * Administrator who approved enrollment
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private Teacher approvedBy;

    /**
     * Approval date
     */
    @Column(name = "approval_date")
    private LocalDate approvalDate;

    /**
     * Notes
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

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

    public enum PDType {
        WORKSHOP,              // Single-day or multi-day workshop
        CONFERENCE,            // Professional conference
        WEBINAR,               // Online webinar
        ONLINE_COURSE,         // Self-paced online course
        COLLEGE_COURSE,        // University/college course
        SUMMER_INSTITUTE,      // Summer professional development
        COACHING,              // Instructional coaching
        MENTORING,             // Mentoring program
        PLC,                   // Professional Learning Community
        BOOK_STUDY,            // Professional book study
        OBSERVATION,           // Peer observation
        ACTION_RESEARCH,       // Teacher action research
        NATIONAL_BOARD,        // National Board Certification process
        DISTRICT_TRAINING,     // District-mandated training
        OTHER                  // Other PD type
    }

    public enum PDStatus {
        PENDING_APPROVAL,      // Awaiting administrative approval
        APPROVED,              // Approved but not yet enrolled
        ENROLLED,              // Currently enrolled
        IN_PROGRESS,           // Actively participating
        COMPLETED,             // Completed successfully
        INCOMPLETE,            // Did not complete
        WITHDRAWN,             // Withdrawn from course
        CANCELED,              // Course canceled
        FAILED                 // Did not pass
    }

    public enum DeliveryMethod {
        IN_PERSON,             // Face-to-face instruction
        ONLINE,                // Fully online
        HYBRID,                // Mix of in-person and online
        SELF_PACED,            // Self-paced learning
        SYNCHRONOUS,           // Live/real-time online
        ASYNCHRONOUS           // Recorded/time-shifted
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    @Transient
    public boolean isCompleted() {
        return status == PDStatus.COMPLETED;
    }

    @Transient
    public boolean isInProgress() {
        return status == PDStatus.IN_PROGRESS || status == PDStatus.ENROLLED;
    }

    @Transient
    public boolean isPastDeadline() {
        return required && deadline != null && deadline.isBefore(LocalDate.now()) && !isCompleted();
    }

    @Transient
    public int getDaysUntilDeadline() {
        if (deadline == null) return 0;
        LocalDate today = LocalDate.now();
        if (today.isAfter(deadline)) return 0;
        return (int) (deadline.toEpochDay() - today.toEpochDay());
    }

    @Transient
    public double getTotalHoursEarned() {
        double total = 0.0;
        if (hoursEarned != null) total += hoursEarned;
        if (contactHours != null) total += contactHours;
        return total;
    }

    @Transient
    public boolean needsCertificate() {
        return isCompleted() && !certificateEarned;
    }

    @Transient
    public boolean needsReimbursement() {
        return reimbursementRequested &&
               (reimbursementApproved == null || reimbursementApproved) &&
               !reimbursementPaid;
    }

    // ========================================================================
    // JPA LIFECYCLE CALLBACKS
    // ========================================================================

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
