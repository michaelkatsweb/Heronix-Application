package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * RegistrationDocument Entity
 *
 * Tracks required documents for student registration with their status.
 * Allows registrars to save incomplete registrations and follow up on missing documents.
 *
 * Document Types:
 * - Birth Certificate (Required)
 * - Immunization Records (Required)
 * - Proof of Residence (Required)
 * - Previous School Transcript
 * - IEP/504 Plan
 * - Medical Records
 * - Photo/Media Release Form
 * - Student Photo
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Entity
@Table(name = "registration_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DocumentStatus status = DocumentStatus.NOT_SUBMITTED;

    /**
     * Is this document required for enrollment?
     */
    @Column(name = "is_required")
    private Boolean isRequired = false;

    /**
     * Path to uploaded document file (if submitted)
     */
    @Column(name = "file_path")
    private String filePath;

    /**
     * Original filename
     */
    @Column(name = "original_filename")
    private String originalFilename;

    /**
     * Date document was submitted
     */
    @Column(name = "submitted_date")
    private LocalDateTime submittedDate;

    /**
     * Date document was verified by staff
     */
    @Column(name = "verified_date")
    private LocalDateTime verifiedDate;

    /**
     * Staff member who verified the document
     */
    @Column(name = "verified_by")
    private String verifiedBy;

    /**
     * For REFUSED status: reason for refusal
     * For EXEMPTION: exemption reason/documentation
     */
    @Column(name = "refusal_reason", columnDefinition = "TEXT")
    private String refusalReason;

    /**
     * Expected date for document submission (for follow-up)
     */
    @Column(name = "expected_date")
    private LocalDateTime expectedDate;

    /**
     * Notes about the document
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Record creation timestamp
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Record update timestamp
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ========================================================================
    // ENUMS
    // ========================================================================

    public enum DocumentType {
        BIRTH_CERTIFICATE("Birth Certificate", true,
                "Official birth certificate or certified copy"),
        IMMUNIZATION_RECORDS("Immunization Records", true,
                "Up-to-date immunization records from healthcare provider"),
        PROOF_OF_RESIDENCE("Proof of Residence", true,
                "Utility bill, lease agreement, or mortgage statement"),
        PREVIOUS_TRANSCRIPT("Previous School Transcript", false,
                "Official transcript from previous school"),
        IEP_504_PLAN("IEP/504 Plan", false,
                "Individualized Education Program or 504 Accommodation Plan"),
        MEDICAL_RECORDS("Medical Records", false,
                "Relevant medical records, health conditions, allergies"),
        PHOTO_RELEASE("Photo/Media Release Form", false,
                "Permission for photos/videos in school activities"),
        STUDENT_PHOTO("Student Photo", true,
                "Current photo for student ID and records"),
        CUSTODY_DOCUMENTS("Custody Documents", false,
                "Court orders, custody agreements if applicable"),
        GUARDIANSHIP_PROOF("Guardianship Proof", false,
                "Legal guardianship documentation if not parent"),
        HOME_LANGUAGE_SURVEY("Home Language Survey", true,
                "Required for English learner identification"),
        EMERGENCY_CONTACTS("Emergency Contact Form", true,
                "Emergency contact information form"),
        OTHER("Other Document", false,
                "Other supporting documentation");

        private final String displayName;
        private final boolean requiredByDefault;
        private final String description;

        DocumentType(String displayName, boolean requiredByDefault, String description) {
            this.displayName = displayName;
            this.requiredByDefault = requiredByDefault;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean isRequiredByDefault() {
            return requiredByDefault;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum DocumentStatus {
        NOT_SUBMITTED("Not Submitted", "Document has not been submitted yet"),
        PENDING("Pending", "Document expected - waiting for parent"),
        SUBMITTED("Submitted", "Document submitted, awaiting verification"),
        VERIFIED("Verified", "Document verified and approved"),
        REJECTED("Rejected", "Document rejected - needs resubmission"),
        REFUSED("Refused", "Parent/Guardian refused to provide document"),
        EXEMPTION("Exemption", "Student has exemption from this requirement"),
        WAIVED("Waived", "Requirement waived by administrator"),
        EXPIRED("Expired", "Document has expired - needs renewal");

        private final String displayName;
        private final String description;

        DocumentStatus(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        /**
         * Is this a completed/acceptable status?
         */
        public boolean isComplete() {
            return this == VERIFIED || this == EXEMPTION || this == WAIVED;
        }

        /**
         * Is this status acceptable for enrollment?
         */
        public boolean isAcceptableForEnrollment() {
            return this == VERIFIED || this == EXEMPTION || this == WAIVED || this == REFUSED;
        }
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Mark document as refused with reason
     */
    public void markAsRefused(String reason) {
        this.status = DocumentStatus.REFUSED;
        this.refusalReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Mark document as submitted
     */
    public void markAsSubmitted(String filePath, String filename) {
        this.status = DocumentStatus.SUBMITTED;
        this.filePath = filePath;
        this.originalFilename = filename;
        this.submittedDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Mark document as verified
     */
    public void markAsVerified(String verifiedBy) {
        this.status = DocumentStatus.VERIFIED;
        this.verifiedDate = LocalDateTime.now();
        this.verifiedBy = verifiedBy;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Check if this document is blocking enrollment
     */
    public boolean isBlockingEnrollment() {
        return Boolean.TRUE.equals(isRequired) && !status.isAcceptableForEnrollment();
    }
}
