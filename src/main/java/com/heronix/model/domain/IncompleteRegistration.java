package com.heronix.model.domain;

import com.heronix.model.enums.RegistrationStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * IncompleteRegistration Entity
 *
 * Tracks student registrations that are incomplete due to:
 * - Missing required documents
 * - Missing student photo
 * - Pending verification
 * - Other incomplete items
 *
 * This allows administrators to:
 * - View all incomplete registrations in one place
 * - Track follow-up dates and notes
 * - Send reminders to parents
 * - Complete registrations when documents arrive
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Entity
@Table(name = "incomplete_registrations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncompleteRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========================================================================
    // STUDENT INFORMATION
    // ========================================================================

    /**
     * Reference to the student (may be null if student record not yet created)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    /**
     * Student first name (stored separately in case student record not created)
     */
    @Column(name = "student_first_name", nullable = false)
    private String studentFirstName;

    /**
     * Student last name
     */
    @Column(name = "student_last_name", nullable = false)
    private String studentLastName;

    /**
     * Student grade level
     */
    @Column(name = "grade_level")
    private String gradeLevel;

    /**
     * Student date of birth
     */
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    // ========================================================================
    // GUARDIAN/CONTACT INFORMATION
    // ========================================================================

    /**
     * Primary guardian name
     */
    @Column(name = "guardian_name")
    private String guardianName;

    /**
     * Guardian phone number for follow-up
     */
    @Column(name = "guardian_phone")
    private String guardianPhone;

    /**
     * Guardian email for notifications
     */
    @Column(name = "guardian_email")
    private String guardianEmail;

    // ========================================================================
    // REGISTRATION STATUS
    // ========================================================================

    /**
     * Current status of the incomplete registration
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RegistrationStatus status = RegistrationStatus.INCOMPLETE_DOCUMENTS;

    /**
     * Reason for incomplete status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "incomplete_reason")
    private IncompleteReason incompleteReason;

    /**
     * Detailed description of what's missing
     */
    @Column(name = "missing_items", columnDefinition = "TEXT")
    private String missingItems;

    // ========================================================================
    // DOCUMENT TRACKING
    // ========================================================================

    /**
     * Is birth certificate missing?
     */
    @Column(name = "missing_birth_certificate")
    private Boolean missingBirthCertificate = false;

    /**
     * Is immunization record missing?
     */
    @Column(name = "missing_immunization")
    private Boolean missingImmunization = false;

    /**
     * Is proof of residence missing?
     */
    @Column(name = "missing_proof_of_residence")
    private Boolean missingProofOfResidence = false;

    /**
     * Is student photo missing?
     */
    @Column(name = "missing_photo")
    private Boolean missingPhoto = false;

    /**
     * Did parent refuse photo?
     */
    @Column(name = "photo_refused")
    private Boolean photoRefused = false;

    /**
     * Photo refusal reason/documentation
     */
    @Column(name = "photo_refusal_reason", columnDefinition = "TEXT")
    private String photoRefusalReason;

    /**
     * Other missing documents (comma-separated)
     */
    @Column(name = "other_missing_documents")
    private String otherMissingDocuments;

    // ========================================================================
    // FOLLOW-UP TRACKING
    // ========================================================================

    /**
     * Expected date for documents/completion
     */
    @Column(name = "expected_completion_date")
    private LocalDate expectedCompletionDate;

    /**
     * Date of last follow-up contact
     */
    @Column(name = "last_followup_date")
    private LocalDate lastFollowupDate;

    /**
     * Number of follow-up attempts made
     */
    @Column(name = "followup_count")
    private Integer followupCount = 0;

    /**
     * Notes from follow-up contacts
     */
    @Column(name = "followup_notes", columnDefinition = "TEXT")
    private String followupNotes;

    /**
     * Next scheduled follow-up date
     */
    @Column(name = "next_followup_date")
    private LocalDate nextFollowupDate;

    /**
     * Staff member assigned to follow up
     */
    @Column(name = "assigned_to")
    private String assignedTo;

    // ========================================================================
    // PRIORITY & URGENCY
    // ========================================================================

    /**
     * Priority level for this case
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private Priority priority = Priority.NORMAL;

    /**
     * Is this case flagged for urgent attention?
     */
    @Column(name = "is_urgent")
    private Boolean isUrgent = false;

    /**
     * Urgency reason
     */
    @Column(name = "urgency_reason")
    private String urgencyReason;

    // ========================================================================
    // TIMESTAMPS
    // ========================================================================

    /**
     * Date registration was started
     */
    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registrationDate;

    /**
     * Date marked as incomplete
     */
    @Column(name = "incomplete_since")
    private LocalDateTime incompleteSince;

    /**
     * Date registration was completed (if resolved)
     */
    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    /**
     * User who created this record
     */
    @Column(name = "created_by")
    private String createdBy;

    /**
     * Last update timestamp
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (registrationDate == null) {
            registrationDate = LocalDateTime.now();
        }
        if (incompleteSince == null) {
            incompleteSince = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ========================================================================
    // ENUMS
    // ========================================================================

    public enum IncompleteReason {
        MISSING_DOCUMENTS("Missing Required Documents"),
        MISSING_PHOTO("Missing Student Photo"),
        PHOTO_REFUSED("Parent Refused Photo"),
        PENDING_VERIFICATION("Pending Document Verification"),
        MISSING_GUARDIAN_INFO("Missing Guardian Information"),
        MISSING_MEDICAL_INFO("Missing Medical Information"),
        PENDING_TRANSCRIPT("Waiting for Previous School Transcript"),
        OTHER("Other Reason");

        private final String displayName;

        IncompleteReason(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Priority {
        LOW("Low"),
        NORMAL("Normal"),
        HIGH("High"),
        URGENT("Urgent");

        private final String displayName;

        Priority(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Get the student's full name
     */
    public String getStudentFullName() {
        return studentFirstName + " " + studentLastName;
    }

    /**
     * Get count of missing documents
     */
    public int getMissingDocumentCount() {
        int count = 0;
        if (Boolean.TRUE.equals(missingBirthCertificate)) count++;
        if (Boolean.TRUE.equals(missingImmunization)) count++;
        if (Boolean.TRUE.equals(missingProofOfResidence)) count++;
        if (Boolean.TRUE.equals(missingPhoto) && !Boolean.TRUE.equals(photoRefused)) count++;
        return count;
    }

    /**
     * Get list of missing document names
     */
    public List<String> getMissingDocumentList() {
        List<String> missing = new ArrayList<>();
        if (Boolean.TRUE.equals(missingBirthCertificate)) missing.add("Birth Certificate");
        if (Boolean.TRUE.equals(missingImmunization)) missing.add("Immunization Records");
        if (Boolean.TRUE.equals(missingProofOfResidence)) missing.add("Proof of Residence");
        if (Boolean.TRUE.equals(missingPhoto)) {
            if (Boolean.TRUE.equals(photoRefused)) {
                missing.add("Photo (Refused)");
            } else {
                missing.add("Student Photo");
            }
        }
        return missing;
    }

    /**
     * Get days since registration was marked incomplete
     */
    public long getDaysSinceIncomplete() {
        if (incompleteSince == null) return 0;
        return ChronoUnit.DAYS.between(incompleteSince.toLocalDate(), LocalDate.now());
    }

    /**
     * Check if expected completion date has passed
     */
    public boolean isOverdue() {
        if (expectedCompletionDate == null) return false;
        return LocalDate.now().isAfter(expectedCompletionDate);
    }

    /**
     * Check if follow-up is due
     */
    public boolean isFollowupDue() {
        if (nextFollowupDate == null) return false;
        return !LocalDate.now().isBefore(nextFollowupDate);
    }

    /**
     * Record a follow-up contact
     */
    public void recordFollowup(String notes, LocalDate nextDate) {
        this.lastFollowupDate = LocalDate.now();
        this.followupCount = (this.followupCount == null ? 0 : this.followupCount) + 1;
        if (notes != null && !notes.isEmpty()) {
            String timestamp = LocalDate.now().toString();
            String newNote = "[" + timestamp + "] " + notes;
            if (this.followupNotes == null || this.followupNotes.isEmpty()) {
                this.followupNotes = newNote;
            } else {
                this.followupNotes = newNote + "\n---\n" + this.followupNotes;
            }
        }
        this.nextFollowupDate = nextDate;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Mark registration as complete
     */
    public void markComplete() {
        this.status = RegistrationStatus.SUBMITTED;
        this.completedDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Get status display with color hint
     */
    public String getStatusDisplay() {
        if (isOverdue()) {
            return "OVERDUE - " + status.getDisplayName();
        }
        return status.getDisplayName();
    }
}
