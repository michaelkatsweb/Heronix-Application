package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Attendance Adjustment Entity
 *
 * Tracks all modifications made to attendance records by clerks and administrators.
 * Provides complete audit trail for attendance changes.
 *
 * Use Cases:
 * - Clerk enters excuse note from parent (changes ABSENT to EXCUSED_ABSENT)
 * - Admin corrects teacher's error
 * - Recording pre-approved vacation days
 * - Marking student present who was incorrectly marked absent
 *
 * Features:
 * - Complete audit trail (who, when, why)
 * - Original and new values preserved
 * - Approval workflow support
 * - Documentation attachment tracking
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 58 - Attendance Enhancement - January 2026
 */
@Entity
@Table(name = "attendance_adjustments", indexes = {
    @Index(name = "idx_adj_student", columnList = "student_id"),
    @Index(name = "idx_adj_date", columnList = "adjustment_date"),
    @Index(name = "idx_adj_attendance_date", columnList = "attendance_date"),
    @Index(name = "idx_adj_adjusted_by", columnList = "adjusted_by"),
    @Index(name = "idx_adj_status", columnList = "approval_status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceAdjustment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========================================================================
    // STUDENT AND DATE INFORMATION
    // ========================================================================

    /**
     * Student whose attendance is being adjusted
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * The date of attendance being adjusted
     */
    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    /**
     * Period number (null = all day adjustment)
     */
    @Column(name = "period_number")
    private Integer periodNumber;

    /**
     * Reference to the original attendance record (if exists)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_record_id")
    private AttendanceRecord attendanceRecord;

    // ========================================================================
    // CHANGE INFORMATION
    // ========================================================================

    /**
     * Type of adjustment being made
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_type", nullable = false)
    private AdjustmentType adjustmentType;

    /**
     * Original status before adjustment (null if creating new record)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "original_status")
    private AttendanceRecord.AttendanceStatus originalStatus;

    /**
     * New status after adjustment
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private AttendanceRecord.AttendanceStatus newStatus;

    /**
     * Excuse code applied (if applicable)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "excuse_code_id")
    private ExcuseCode excuseCode;

    /**
     * Reason for the adjustment
     */
    @Column(name = "reason", columnDefinition = "TEXT", nullable = false)
    private String reason;

    /**
     * Additional notes or details
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // ========================================================================
    // SOURCE INFORMATION
    // ========================================================================

    /**
     * Source of the adjustment request
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "source")
    @Builder.Default
    private AdjustmentSource source = AdjustmentSource.OFFICE;

    /**
     * Reference to parent contact (if adjustment from parent note)
     */
    @Column(name = "parent_contact_info")
    private String parentContactInfo;

    /**
     * Date parent note was received (if applicable)
     */
    @Column(name = "parent_note_date")
    private LocalDate parentNoteDate;

    // ========================================================================
    // DOCUMENTATION
    // ========================================================================

    /**
     * Whether documentation was provided
     */
    @Column(name = "documentation_provided")
    @Builder.Default
    private Boolean documentationProvided = false;

    /**
     * Type of documentation (doctor's note, court order, etc.)
     */
    @Column(name = "documentation_type")
    private String documentationType;

    /**
     * Path to uploaded documentation file
     */
    @Column(name = "documentation_path")
    private String documentationPath;

    // ========================================================================
    // APPROVAL WORKFLOW
    // ========================================================================

    /**
     * Current approval status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status")
    @Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.APPROVED;

    /**
     * User who needs to approve (if pending)
     */
    @Column(name = "pending_approver")
    private String pendingApprover;

    /**
     * Date when approved/rejected
     */
    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    /**
     * User who approved/rejected
     */
    @Column(name = "approved_by")
    private String approvedBy;

    /**
     * Approval/rejection notes
     */
    @Column(name = "approval_notes")
    private String approvalNotes;

    // ========================================================================
    // AUDIT INFORMATION
    // ========================================================================

    /**
     * User who made the adjustment
     */
    @Column(name = "adjusted_by", nullable = false)
    private String adjustedBy;

    /**
     * Role of the user who made the adjustment
     */
    @Column(name = "adjusted_by_role")
    private String adjustedByRole;

    /**
     * Date/time when adjustment was made
     */
    @Column(name = "adjustment_date", nullable = false)
    private LocalDateTime adjustmentDate;

    /**
     * Whether this adjustment has been applied to the attendance record
     */
    @Column(name = "applied")
    @Builder.Default
    private Boolean applied = false;

    /**
     * Date/time when adjustment was applied
     */
    @Column(name = "applied_date")
    private LocalDateTime appliedDate;

    /**
     * Created timestamp
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Updated timestamp
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========================================================================
    // ENUMS
    // ========================================================================

    /**
     * Types of attendance adjustments
     */
    public enum AdjustmentType {
        EXCUSE_ABSENCE("Excuse Absence", "Change unexcused absence to excused"),
        REMOVE_EXCUSE("Remove Excuse", "Change excused absence to unexcused"),
        MARK_PRESENT("Mark Present", "Change absence to present"),
        MARK_ABSENT("Mark Absent", "Change present to absent"),
        MARK_TARDY("Mark Tardy", "Change to tardy status"),
        CORRECT_ERROR("Correct Error", "Correct data entry error"),
        PRE_APPROVE_ABSENCE("Pre-Approve Absence", "Pre-approve planned absence"),
        BULK_ADJUSTMENT("Bulk Adjustment", "Part of bulk attendance adjustment"),
        APPEAL_RESOLUTION("Appeal Resolution", "Resolve attendance appeal"),
        SYSTEM_CORRECTION("System Correction", "Automated system correction");

        private final String displayName;
        private final String description;

        AdjustmentType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Source of the adjustment request
     */
    public enum AdjustmentSource {
        OFFICE("Front Office", "Entered by front office staff"),
        PARENT_NOTE("Parent Note", "Based on written parent note"),
        PARENT_CALL("Parent Call", "Based on parent phone call"),
        PARENT_EMAIL("Parent Email", "Based on parent email"),
        PARENT_PORTAL("Parent Portal", "Submitted via parent portal"),
        TEACHER_REQUEST("Teacher Request", "Requested by teacher"),
        ADMIN_OVERRIDE("Admin Override", "Administrator override"),
        SYSTEM_SYNC("System Sync", "From teacher attendance sync"),
        BULK_IMPORT("Bulk Import", "From data import"),
        APPEAL("Student Appeal", "Student/parent appeal");

        private final String displayName;
        private final String description;

        AdjustmentSource(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Approval status for adjustments that require approval
     */
    public enum ApprovalStatus {
        PENDING("Pending", "#FF9800"),
        APPROVED("Approved", "#4CAF50"),
        REJECTED("Rejected", "#F44336"),
        AUTO_APPROVED("Auto-Approved", "#2196F3");

        private final String displayName;
        private final String color;

        ApprovalStatus(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getColor() {
            return color;
        }
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Check if this adjustment needs approval
     */
    @Transient
    public boolean needsApproval() {
        return approvalStatus == ApprovalStatus.PENDING;
    }

    /**
     * Check if this adjustment is approved and ready to apply
     */
    @Transient
    public boolean isReadyToApply() {
        return (approvalStatus == ApprovalStatus.APPROVED ||
                approvalStatus == ApprovalStatus.AUTO_APPROVED)
               && !Boolean.TRUE.equals(applied);
    }

    /**
     * Get a display-friendly description of the change
     */
    @Transient
    public String getChangeDescription() {
        if (originalStatus == null) {
            return "Set to " + newStatus.name().replace("_", " ").toLowerCase();
        }
        return originalStatus.name().replace("_", " ").toLowerCase() +
               " → " +
               newStatus.name().replace("_", " ").toLowerCase();
    }

    /**
     * Get period display (all day or specific period)
     */
    @Transient
    public String getPeriodDisplay() {
        if (periodNumber == null) {
            return "All Day";
        }
        if (periodNumber == 0) {
            return "Homeroom";
        }
        return "Period " + periodNumber;
    }

    // ========================================================================
    // JPA LIFECYCLE CALLBACKS
    // ========================================================================

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (adjustmentDate == null) {
            adjustmentDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
