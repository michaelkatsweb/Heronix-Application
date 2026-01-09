package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Schedule Change Request Entity
 * Represents a student's request to change their schedule
 *
 * Features:
 * - Add/drop/swap course requests
 * - Multi-role approval workflow (counselors, administrators, teachers)
 * - Request reason tracking
 * - Parent contact information
 * - Review notes and status tracking
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Entity
@Table(name = "schedule_change_requests", indexes = {
    @Index(name = "idx_schedule_change_student", columnList = "student_id"),
    @Index(name = "idx_schedule_change_status", columnList = "status"),
    @Index(name = "idx_schedule_change_request_date", columnList = "request_date"),
    @Index(name = "idx_schedule_change_reviewer", columnList = "reviewed_by_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Student making the request
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @ToString.Exclude
    private Student student;

    /**
     * Type of change request
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false)
    private RequestType requestType;

    /**
     * Current course (for DROP or SWAP requests, null for ADD)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_course_id")
    @ToString.Exclude
    private Course currentCourse;

    /**
     * Current course section (for DROP or SWAP requests, null for ADD)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_section_id")
    @ToString.Exclude
    private CourseSection currentSection;

    /**
     * Requested course (for ADD or SWAP requests, null for DROP)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_course_id")
    @ToString.Exclude
    private Course requestedCourse;

    /**
     * Requested course section (optional - can be null if student doesn't care which section)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_section_id")
    @ToString.Exclude
    private CourseSection requestedSection;

    /**
     * Reason for the change request
     */
    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    /**
     * Additional details or notes from the student
     */
    @Column(name = "student_notes", columnDefinition = "TEXT")
    private String studentNotes;

    /**
     * Parent contact information (phone or email)
     */
    @Column(name = "parent_contact", length = 200)
    private String parentContact;

    /**
     * Has parent been contacted about this request?
     */
    @Column(name = "parent_contacted")
    @Builder.Default
    private Boolean parentContacted = false;

    /**
     * Request submission date/time
     */
    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;

    /**
     * Request status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    /**
     * Priority level (higher numbers = higher priority)
     * Can be set by counselors for urgent requests
     */
    @Column(name = "priority_level")
    @Builder.Default
    private Integer priorityLevel = 0;

    /**
     * Teacher/counselor/administrator who reviewed the request
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    @ToString.Exclude
    private Teacher reviewedBy;

    /**
     * Review date/time
     */
    @Column(name = "reviewed_date")
    private LocalDateTime reviewedDate;

    /**
     * Review notes from counselor/administrator
     */
    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;

    /**
     * Denial reason (if denied)
     */
    @Column(name = "denial_reason", columnDefinition = "TEXT")
    private String denialReason;

    /**
     * Date/time the request was completed (schedule actually changed)
     */
    @Column(name = "completion_date")
    private LocalDateTime completionDate;

    /**
     * Academic year this request applies to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id")
    @ToString.Exclude
    private AcademicYear academicYear;

    /**
     * Grading period this request applies to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grading_period_id")
    @ToString.Exclude
    private GradingPeriod gradingPeriod;

    /**
     * Record creation timestamp
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Record last update timestamp
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========================================================================
    // JPA LIFECYCLE CALLBACKS
    // ========================================================================

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.requestDate == null) {
            this.requestDate = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = RequestStatus.PENDING;
        }
        if (this.priorityLevel == null) {
            this.priorityLevel = 0;
        }
        if (this.parentContacted == null) {
            this.parentContacted = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ========================================================================
    // BUSINESS LOGIC METHODS
    // ========================================================================

    /**
     * Check if request is pending
     */
    public boolean isPending() {
        return this.status == RequestStatus.PENDING;
    }

    /**
     * Check if request has been approved
     */
    public boolean isApproved() {
        return this.status == RequestStatus.APPROVED;
    }

    /**
     * Check if request has been denied
     */
    public boolean isDenied() {
        return this.status == RequestStatus.DENIED;
    }

    /**
     * Check if request has been completed
     */
    public boolean isCompleted() {
        return this.status == RequestStatus.COMPLETED;
    }

    /**
     * Check if request has been cancelled
     */
    public boolean isCancelled() {
        return this.status == RequestStatus.CANCELLED;
    }

    /**
     * Approve the request
     */
    public void approve(Teacher reviewer, String notes) {
        this.status = RequestStatus.APPROVED;
        this.reviewedBy = reviewer;
        this.reviewedDate = LocalDateTime.now();
        this.reviewNotes = notes;
    }

    /**
     * Deny the request
     */
    public void deny(Teacher reviewer, String reason) {
        this.status = RequestStatus.DENIED;
        this.reviewedBy = reviewer;
        this.reviewedDate = LocalDateTime.now();
        this.denialReason = reason;
    }

    /**
     * Mark request as completed
     */
    public void complete() {
        this.status = RequestStatus.COMPLETED;
        this.completionDate = LocalDateTime.now();
    }

    /**
     * Cancel the request
     */
    public void cancel() {
        this.status = RequestStatus.CANCELLED;
    }

    /**
     * Get display summary of the request
     */
    public String getRequestSummary() {
        StringBuilder summary = new StringBuilder();

        switch (requestType) {
            case ADD:
                summary.append("Add: ").append(requestedCourse != null ? requestedCourse.getCourseCode() : "Unknown");
                break;
            case DROP:
                summary.append("Drop: ").append(currentCourse != null ? currentCourse.getCourseCode() : "Unknown");
                break;
            case SWAP:
                summary.append("Swap: ")
                       .append(currentCourse != null ? currentCourse.getCourseCode() : "Unknown")
                       .append(" → ")
                       .append(requestedCourse != null ? requestedCourse.getCourseCode() : "Unknown");
                break;
        }

        return summary.toString();
    }

    /**
     * Check if request can be auto-approved
     * (No conflicts, course available, prerequisites met)
     */
    public boolean canAutoApprove() {
        // Auto-approval logic would be implemented based on business rules
        // For now, return false - all requests require manual review
        return false;
    }

    /**
     * Get days since request was submitted
     */
    public long getDaysSinceRequest() {
        return java.time.Duration.between(requestDate, LocalDateTime.now()).toDays();
    }

    /**
     * Check if request is overdue (more than X days pending)
     */
    public boolean isOverdue(int maxDays) {
        return isPending() && getDaysSinceRequest() > maxDays;
    }

    // ========================================================================
    // ENUMS
    // ========================================================================

    /**
     * Request type enumeration
     */
    public enum RequestType {
        ADD("Add Course"),
        DROP("Drop Course"),
        SWAP("Swap Course");

        private final String displayName;

        RequestType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Request status enumeration
     */
    public enum RequestStatus {
        PENDING("Pending Review"),
        APPROVED("Approved"),
        DENIED("Denied"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled");

        private final String displayName;

        RequestStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
