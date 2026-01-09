package com.heronix.repository;

import com.heronix.model.domain.TransferOutDocumentation;
import com.heronix.model.domain.TransferOutDocumentation.TransferOutStatus;
import com.heronix.model.domain.TransferOutDocumentation.TransferOutType;
import com.heronix.model.domain.TransferOutDocumentation.TransferOutReason;
import com.heronix.model.domain.TransferOutDocumentation.TransmissionMethod;
import com.heronix.model.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for TransferOutDocumentation Entity
 * Handles database operations for outgoing transfer student records
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@Repository
public interface TransferOutDocumentationRepository extends JpaRepository<TransferOutDocumentation, Long> {

    // ========================================================================
    // BASIC QUERIES
    // ========================================================================

    /**
     * Find transfer out record by transfer out number
     */
    Optional<TransferOutDocumentation> findByTransferOutNumber(String transferOutNumber);

    /**
     * Find all transfers by status
     */
    List<TransferOutDocumentation> findByStatus(TransferOutStatus status);

    /**
     * Find transfer by student
     */
    Optional<TransferOutDocumentation> findByStudent(Student student);

    /**
     * Find all transfers for a student
     */
    List<TransferOutDocumentation> findAllByStudent(Student student);

    /**
     * Find by transfer out type
     */
    List<TransferOutDocumentation> findByTransferType(TransferOutType transferType);

    // ========================================================================
    // DATE RANGE QUERIES
    // ========================================================================

    /**
     * Find transfers by request date range
     */
    List<TransferOutDocumentation> findByRequestDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find transfers by expected transfer date range
     */
    List<TransferOutDocumentation> findByExpectedTransferDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find recent transfer out requests
     */
    @Query("SELECT tod FROM TransferOutDocumentation tod WHERE tod.requestDate >= :cutoffDate ORDER BY tod.requestDate DESC")
    List<TransferOutDocumentation> findRecentTransferOuts(@Param("cutoffDate") LocalDate cutoffDate);

    // ========================================================================
    // PROCESSING STATUS QUERIES
    // ========================================================================

    /**
     * Find transfers pending records preparation
     */
    @Query("SELECT tod FROM TransferOutDocumentation tod WHERE " +
           "tod.status = 'RECORDS_PREPARATION' AND " +
           "tod.allDocumentsPackaged = false")
    List<TransferOutDocumentation> findPendingRecordsPreparation();

    /**
     * Find transfers ready to send
     */
    @Query("SELECT tod FROM TransferOutDocumentation tod WHERE " +
           "tod.status = 'READY_TO_SEND' AND " +
           "tod.sentDate IS NULL")
    List<TransferOutDocumentation> findReadyToSend();

    /**
     * Find sent transfers pending acknowledgment
     */
    @Query("SELECT tod FROM TransferOutDocumentation tod WHERE " +
           "tod.status = 'SENT' AND " +
           "tod.destinationAcknowledged = false")
    List<TransferOutDocumentation> findPendingAcknowledgment();

    /**
     * Find transfers with all documents packaged
     */
    List<TransferOutDocumentation> findByAllDocumentsPackagedTrue();

    // ========================================================================
    // DESTINATION SCHOOL QUERIES
    // ========================================================================

    /**
     * Find by destination school name
     */
    List<TransferOutDocumentation> findByDestinationSchoolNameContainingIgnoreCase(String schoolName);

    /**
     * Find by destination school district
     */
    List<TransferOutDocumentation> findByDestinationSchoolDistrictContainingIgnoreCase(String district);

    /**
     * Find by destination school state
     */
    List<TransferOutDocumentation> findByDestinationSchoolState(String state);

    /**
     * Get count by destination school
     */
    @Query("SELECT tod.destinationSchoolName, COUNT(tod) FROM TransferOutDocumentation tod " +
           "WHERE tod.destinationSchoolName IS NOT NULL " +
           "GROUP BY tod.destinationSchoolName " +
           "ORDER BY COUNT(tod) DESC")
    List<Object[]> getCountByDestinationSchool();

    // ========================================================================
    // CONSENT & COMPLIANCE QUERIES
    // ========================================================================

    /**
     * Find transfers pending parent consent
     */
    @Query("SELECT tod FROM TransferOutDocumentation tod WHERE " +
           "tod.requiresParentConsent = true AND " +
           "tod.parentConsentObtained = false")
    List<TransferOutDocumentation> findPendingParentConsent();

    /**
     * Find transfers pending FERPA release
     */
    @Query("SELECT tod FROM TransferOutDocumentation tod WHERE " +
           "tod.ferpaReleaseObtained = false")
    List<TransferOutDocumentation> findPendingFerpaRelease();

    /**
     * Find international transfers
     */
    List<TransferOutDocumentation> findByInternationalTransferTrue();

    /**
     * Find transfers requiring apostille
     */
    List<TransferOutDocumentation> findByApostilleRequiredTrue();

    // ========================================================================
    // FEES QUERIES
    // ========================================================================

    /**
     * Find transfers with outstanding fees
     */
    @Query("SELECT tod FROM TransferOutDocumentation tod WHERE " +
           "tod.hasOutstandingFees = true AND " +
           "tod.feesPaidBeforeRelease = false")
    List<TransferOutDocumentation> findWithOutstandingFees();

    /**
     * Find transfers with records held for non-payment
     */
    List<TransferOutDocumentation> findByRecordsHeldForNonPaymentTrue();

    /**
     * Find transfers with unpaid processing fees
     */
    @Query("SELECT tod FROM TransferOutDocumentation tod WHERE " +
           "tod.totalFees > 0 AND " +
           "tod.feesPaid = false")
    List<TransferOutDocumentation> findWithUnpaidFees();

    // ========================================================================
    // URGENCY QUERIES
    // ========================================================================

    /**
     * Find urgent transfer out requests
     */
    List<TransferOutDocumentation> findByUrgentRequestTrue();

    /**
     * Find expedited processing requests
     */
    List<TransferOutDocumentation> findByExpeditedProcessingTrue();

    /**
     * Find emergency transfers
     */
    List<TransferOutDocumentation> findByIsEmergencyTransferTrue();

    /**
     * Find mid-year transfers
     */
    List<TransferOutDocumentation> findByIsMidYearTransferTrue();

    // ========================================================================
    // TRANSMISSION METHOD QUERIES
    // ========================================================================

    /**
     * Find by transmission method
     */
    List<TransferOutDocumentation> findByTransmissionMethod(TransmissionMethod method);

    /**
     * Find electronic transmissions
     */
    @Query("SELECT tod FROM TransferOutDocumentation tod WHERE " +
           "tod.transmissionMethod IN ('ELECTRONIC_TRANSCRIPT', 'EMAIL', 'PARCHMENT', 'NAVIANCE')")
    List<TransferOutDocumentation> findElectronicTransmissions();

    /**
     * Find physical mail transmissions
     */
    @Query("SELECT tod FROM TransferOutDocumentation tod WHERE " +
           "tod.transmissionMethod IN ('US_MAIL', 'CERTIFIED_MAIL', 'COURIER')")
    List<TransferOutDocumentation> findPhysicalMailTransmissions();

    // ========================================================================
    // STAFF ASSIGNMENT QUERIES
    // ========================================================================

    /**
     * Find by assigned staff
     */
    @Query("SELECT tod FROM TransferOutDocumentation tod WHERE tod.assignedStaff.id = :staffId")
    List<TransferOutDocumentation> findByAssignedStaff(@Param("staffId") Long staffId);

    /**
     * Find unassigned transfer outs
     */
    @Query("SELECT tod FROM TransferOutDocumentation tod WHERE " +
           "tod.assignedStaff IS NULL AND " +
           "tod.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<TransferOutDocumentation> findUnassignedTransferOuts();

    // ========================================================================
    // FOLLOW-UP QUERIES
    // ========================================================================

    /**
     * Find transfers requiring follow-up
     */
    @Query("SELECT tod FROM TransferOutDocumentation tod WHERE " +
           "tod.followUpRequired = true AND " +
           "tod.followUpDate <= :today AND " +
           "tod.status != 'COMPLETED'")
    List<TransferOutDocumentation> findRequiringFollowUp(@Param("today") LocalDate today);

    /**
     * Find sent but unacknowledged after certain days
     */
    @Query("SELECT tod FROM TransferOutDocumentation tod WHERE " +
           "tod.status = 'SENT' AND " +
           "tod.sentDate < :cutoffDate AND " +
           "tod.destinationAcknowledged = false")
    List<TransferOutDocumentation> findUnacknowledgedAfterDate(@Param("cutoffDate") LocalDate cutoffDate);

    // ========================================================================
    // STATISTICS QUERIES
    // ========================================================================

    /**
     * Count by status
     */
    long countByStatus(TransferOutStatus status);

    /**
     * Count by transfer type
     */
    long countByTransferType(TransferOutType transferType);

    /**
     * Count mid-year transfers
     */
    long countByIsMidYearTransferTrue();

    /**
     * Count international transfers
     */
    long countByInternationalTransferTrue();

    /**
     * Get transfer out counts by type
     */
    @Query("SELECT tod.transferType, COUNT(tod) FROM TransferOutDocumentation tod " +
           "GROUP BY tod.transferType")
    List<Object[]> getCountByTransferType();

    /**
     * Get transfer out counts by reason
     */
    @Query("SELECT tod.transferReason, COUNT(tod) FROM TransferOutDocumentation tod " +
           "WHERE tod.transferReason IS NOT NULL " +
           "GROUP BY tod.transferReason " +
           "ORDER BY COUNT(tod) DESC")
    List<Object[]> getCountByTransferReason();

    /**
     * Get transfer out counts by month
     */
    @Query("SELECT FUNCTION('YEAR', tod.requestDate), FUNCTION('MONTH', tod.requestDate), COUNT(tod) " +
           "FROM TransferOutDocumentation tod " +
           "WHERE tod.requestDate BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('YEAR', tod.requestDate), FUNCTION('MONTH', tod.requestDate) " +
           "ORDER BY FUNCTION('YEAR', tod.requestDate), FUNCTION('MONTH', tod.requestDate)")
    List<Object[]> getTransferOutCountsByMonth(@Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    /**
     * Get average documents completion percentage
     */
    @Query("SELECT AVG(tod.documentsPackaged * 1.0 / tod.totalDocumentsIncluded) FROM TransferOutDocumentation tod " +
           "WHERE tod.totalDocumentsIncluded > 0")
    Double getAverageDocumentsCompletion();

    /**
     * Get average processing time (days from request to sent)
     */
    @Query("SELECT AVG(CAST(tod.sentDate AS long) - CAST(tod.requestDate AS long)) FROM TransferOutDocumentation tod " +
           "WHERE tod.sentDate IS NOT NULL")
    Double getAverageProcessingDays();

    // ========================================================================
    // VALIDATION QUERIES
    // ========================================================================

    /**
     * Check if transfer out number exists
     */
    boolean existsByTransferOutNumber(String transferOutNumber);

    /**
     * Check if student has pending transfer out
     */
    @Query("SELECT COUNT(tod) > 0 FROM TransferOutDocumentation tod WHERE " +
           "tod.student.id = :studentId AND " +
           "tod.status NOT IN ('COMPLETED', 'CANCELLED')")
    boolean hasPendingTransferOut(@Param("studentId") Long studentId);

    // ========================================================================
    // REPORTING QUERIES
    // ========================================================================

    /**
     * Find transfers needing attention (delayed processing or follow-up)
     */
    @Query("SELECT tod FROM TransferOutDocumentation tod WHERE " +
           "(tod.status = 'RECORDS_PREPARATION' AND tod.requestDate < :cutoffDate) OR " +
           "(tod.status = 'SENT' AND tod.sentDate < :acknowledgmentCutoff AND tod.destinationAcknowledged = false) OR " +
           "(tod.followUpRequired = true AND tod.followUpDate <= :today)")
    List<TransferOutDocumentation> findNeedingAttention(@Param("cutoffDate") LocalDate cutoffDate,
                                                        @Param("acknowledgmentCutoff") LocalDate acknowledgmentCutoff,
                                                        @Param("today") LocalDate today);

    /**
     * Find completed transfer outs
     */
    @Query("SELECT tod FROM TransferOutDocumentation tod WHERE " +
           "tod.status = 'COMPLETED' AND " +
           "tod.sentDate IS NOT NULL " +
           "ORDER BY tod.sentDate DESC")
    List<TransferOutDocumentation> findCompletedTransferOuts();
}
