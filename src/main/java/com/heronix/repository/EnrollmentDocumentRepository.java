package com.heronix.repository;

import com.heronix.model.domain.EnrollmentApplication;
import com.heronix.model.domain.EnrollmentDocument;
import com.heronix.model.domain.EnrollmentDocument.DocumentType;
import com.heronix.model.domain.EnrollmentDocument.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Enrollment Documents
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Inquiry and Registration System
 */
@Repository
public interface EnrollmentDocumentRepository extends JpaRepository<EnrollmentDocument, Long> {

    // ========================================================================
    // BASIC QUERIES
    // ========================================================================

    /**
     * Find all documents for an application
     */
    List<EnrollmentDocument> findByApplication(EnrollmentApplication application);

    /**
     * Find all documents for an application, ordered by upload date
     */
    List<EnrollmentDocument> findByApplicationOrderByUploadedAtDesc(EnrollmentApplication application);

    /**
     * Find documents by type
     */
    List<EnrollmentDocument> findByDocumentType(DocumentType documentType);

    /**
     * Find documents by verification status
     */
    List<EnrollmentDocument> findByVerificationStatus(VerificationStatus status);

    // ========================================================================
    // APPLICATION-SPECIFIC QUERIES
    // ========================================================================

    /**
     * Find documents by application and type
     */
    List<EnrollmentDocument> findByApplicationAndDocumentType(
            EnrollmentApplication application,
            DocumentType documentType);

    /**
     * Find documents by application and verification status
     */
    List<EnrollmentDocument> findByApplicationAndVerificationStatus(
            EnrollmentApplication application,
            VerificationStatus status);

    /**
     * Find verified documents for an application
     */
    @Query("SELECT d FROM EnrollmentDocument d WHERE " +
           "d.application = :application AND d.verificationStatus = 'VERIFIED'")
    List<EnrollmentDocument> findVerifiedDocumentsByApplication(
            @Param("application") EnrollmentApplication application);

    /**
     * Find pending documents for an application
     */
    @Query("SELECT d FROM EnrollmentDocument d WHERE " +
           "d.application = :application AND d.verificationStatus = 'PENDING'")
    List<EnrollmentDocument> findPendingDocumentsByApplication(
            @Param("application") EnrollmentApplication application);

    // ========================================================================
    // VERIFICATION TRACKING
    // ========================================================================

    /**
     * Find all documents pending verification
     */
    @Query("SELECT d FROM EnrollmentDocument d WHERE " +
           "d.verificationStatus = 'PENDING' OR d.verificationStatus = 'IN_REVIEW' " +
           "ORDER BY d.uploadedAt ASC")
    List<EnrollmentDocument> findAllPendingVerification();

    /**
     * Find documents needing follow-up
     */
    List<EnrollmentDocument> findByNeedsFollowUpTrue();

    /**
     * Find expired documents
     */
    @Query("SELECT d FROM EnrollmentDocument d WHERE " +
           "d.expirationDate IS NOT NULL AND d.expirationDate < CURRENT_DATE")
    List<EnrollmentDocument> findExpiredDocuments();

    /**
     * Find documents expiring soon (within 30 days)
     */
    @Query("SELECT d FROM EnrollmentDocument d WHERE " +
           "d.expirationDate IS NOT NULL AND " +
           "d.expirationDate >= CURRENT_DATE AND " +
           "d.expirationDate <= FUNCTION('DATEADD', DAY, 30, CURRENT_DATE)")
    List<EnrollmentDocument> findDocumentsExpiringSoon();

    // ========================================================================
    // STAFF TRACKING
    // ========================================================================

    /**
     * Find documents uploaded by specific staff member
     */
    @Query("SELECT d FROM EnrollmentDocument d WHERE d.uploadedBy.id = :staffId")
    List<EnrollmentDocument> findByUploadedByStaffId(@Param("staffId") Long staffId);

    /**
     * Find documents verified by specific staff member
     */
    @Query("SELECT d FROM EnrollmentDocument d WHERE d.verifiedBy.id = :staffId")
    List<EnrollmentDocument> findByVerifiedByStaffId(@Param("staffId") Long staffId);

    // ========================================================================
    // STATISTICS
    // ========================================================================

    /**
     * Count documents by type
     */
    Long countByDocumentType(DocumentType documentType);

    /**
     * Count documents by verification status
     */
    Long countByVerificationStatus(VerificationStatus status);

    /**
     * Count verified documents
     */
    @Query("SELECT COUNT(d) FROM EnrollmentDocument d WHERE d.verificationStatus = 'VERIFIED'")
    Long countVerifiedDocuments();

    /**
     * Count pending documents
     */
    @Query("SELECT COUNT(d) FROM EnrollmentDocument d WHERE " +
           "d.verificationStatus = 'PENDING' OR d.verificationStatus = 'IN_REVIEW'")
    Long countPendingDocuments();
}
