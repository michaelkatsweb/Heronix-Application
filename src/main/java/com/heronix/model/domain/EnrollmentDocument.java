package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Enrollment Document
 *
 * Represents a document submitted as part of the enrollment application process.
 * Documents are uploaded, scanned, or photocopied by office staff during in-house enrollment.
 *
 * Required documents typically include:
 * - Birth certificate
 * - Proof of residency (utility bill, lease agreement, etc.)
 * - Immunization records
 * - Previous school transcript (if transfer)
 * - IEP (if applicable)
 * - 504 Plan (if applicable)
 * - Custody papers (if applicable)
 * - Photo ID of parent/guardian
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Inquiry and Registration System
 */
@Entity
@Table(name = "enrollment_documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Parent application
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    @ToString.Exclude
    private EnrollmentApplication application;

    // ========================================================================
    // DOCUMENT INFORMATION
    // ========================================================================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DocumentType documentType;

    @Column(nullable = false, length = 200)
    private String documentName;

    @Column(length = 500)
    private String description;

    @Column(length = 500)
    private String filePath; // Local file path or document management system reference

    @Column(length = 100)
    private String fileType; // "PDF", "JPG", "PNG", "DOCX", etc.

    @Column
    private Long fileSizeBytes;

    // ========================================================================
    // VERIFICATION
    // ========================================================================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_staff_id")
    private User verifiedBy;

    @Column
    private LocalDateTime verifiedAt;

    @Column(length = 1000)
    private String verificationNotes;

    @Column
    private Boolean isOriginal; // True if original document sighted (not photocopy)

    @Column
    private LocalDate expirationDate; // For documents that expire (immunizations, etc.)

    @Column
    private Boolean needsFollowUp;

    @Column(length = 500)
    private String followUpReason;

    // ========================================================================
    // AUDIT FIELDS
    // ========================================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_staff_id", nullable = false)
    private User uploadedBy;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @Column(length = 1000)
    private String notes;

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Check if document is verified
     */
    public boolean isVerified() {
        return verificationStatus == VerificationStatus.VERIFIED;
    }

    /**
     * Check if document is rejected
     */
    public boolean isRejected() {
        return verificationStatus == VerificationStatus.REJECTED;
    }

    /**
     * Check if document is expired
     */
    public boolean isExpired() {
        return expirationDate != null && expirationDate.isBefore(LocalDate.now());
    }

    /**
     * Mark as verified
     */
    public void markVerified(User verifier, String notes) {
        this.verificationStatus = VerificationStatus.VERIFIED;
        this.verifiedBy = verifier;
        this.verifiedAt = LocalDateTime.now();
        this.verificationNotes = notes;
    }

    /**
     * Mark as rejected
     */
    public void markRejected(User verifier, String reason) {
        this.verificationStatus = VerificationStatus.REJECTED;
        this.verifiedBy = verifier;
        this.verifiedAt = LocalDateTime.now();
        this.verificationNotes = reason;
    }

    // ========================================================================
    // ENUMS
    // ========================================================================

    public enum DocumentType {
        BIRTH_CERTIFICATE("Birth Certificate"),
        PROOF_OF_RESIDENCY("Proof of Residency"),
        IMMUNIZATION_RECORDS("Immunization Records"),
        TRANSCRIPT("Academic Transcript"),
        IEP("Individualized Education Program (IEP)"),
        PLAN_504("504 Accommodation Plan"),
        CUSTODY_PAPERS("Custody/Guardianship Papers"),
        PARENT_ID("Parent/Guardian Photo ID"),
        MEDICAL_RECORDS("Medical Records"),
        WITHDRAWAL_FORM("Withdrawal from Previous School"),
        HOMELESS_VERIFICATION("Homeless Status Verification"),
        FOSTER_CARE_DOCS("Foster Care Documentation"),
        MILITARY_ORDERS("Military Orders"),
        OTHER("Other Document");

        private final String displayName;

        DocumentType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean isRequired() {
            return this == BIRTH_CERTIFICATE ||
                   this == PROOF_OF_RESIDENCY ||
                   this == IMMUNIZATION_RECORDS;
        }
    }

    public enum VerificationStatus {
        PENDING("Pending Review"),
        IN_REVIEW("Under Review"),
        VERIFIED("Verified - Accepted"),
        REJECTED("Rejected - Invalid"),
        EXPIRED("Expired");

        private final String displayName;

        VerificationStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
