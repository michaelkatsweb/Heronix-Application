package com.heronix.repository;

import com.heronix.model.domain.WithdrawalRecord;
import com.heronix.model.domain.WithdrawalRecord.WithdrawalStatus;
import com.heronix.model.domain.WithdrawalRecord.WithdrawalType;
import com.heronix.model.domain.WithdrawalRecord.FinalStatus;
import com.heronix.model.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for WithdrawalRecord Entity
 * Handles database operations for student withdrawal processing
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@Repository
public interface WithdrawalRepository extends JpaRepository<WithdrawalRecord, Long> {

    // ========================================================================
    // BASIC QUERIES
    // ========================================================================

    /**
     * Find withdrawal record by withdrawal number
     */
    Optional<WithdrawalRecord> findByWithdrawalNumber(String withdrawalNumber);

    /**
     * Find all withdrawal records by status
     */
    List<WithdrawalRecord> findByStatus(WithdrawalStatus status);

    /**
     * Find all withdrawal records for a specific student
     */
    List<WithdrawalRecord> findByStudent(Student student);

    /**
     * Find withdrawal record for student (most recent)
     */
    Optional<WithdrawalRecord> findFirstByStudentOrderByCreatedAtDesc(Student student);

    /**
     * Find by withdrawal type
     */
    List<WithdrawalRecord> findByWithdrawalType(WithdrawalType withdrawalType);

    /**
     * Find by final status
     */
    List<WithdrawalRecord> findByFinalStatus(FinalStatus finalStatus);

    // ========================================================================
    // CLEARANCE QUERIES
    // ========================================================================

    /**
     * Find withdrawal records pending clearance
     */
    List<WithdrawalRecord> findByStatusAndAllClearedFalse(WithdrawalStatus status);

    /**
     * Find fully cleared withdrawal records
     */
    List<WithdrawalRecord> findByAllClearedTrue();

    /**
     * Find withdrawal records in clearance process
     */
    @Query("SELECT wr FROM WithdrawalRecord wr WHERE " +
           "wr.status IN ('PENDING_CLEARANCE', 'CLEARANCE_IN_PROGRESS') " +
           "ORDER BY wr.withdrawalDate ASC")
    List<WithdrawalRecord> findInClearanceProcess();

    /**
     * Find withdrawal records with incomplete clearance
     */
    @Query("SELECT wr FROM WithdrawalRecord wr WHERE " +
           "wr.allCleared = false AND " +
           "wr.status != 'COMPLETED' AND " +
           "wr.status != 'CANCELLED'")
    List<WithdrawalRecord> findWithIncompleteClearance();

    // ========================================================================
    // TRANSFER QUERIES
    // ========================================================================

    /**
     * Find transfer withdrawals
     */
    List<WithdrawalRecord> findByIsTransferringTrue();

    /**
     * Find by receiving school
     */
    List<WithdrawalRecord> findByReceivingSchoolNameContainingIgnoreCase(String schoolName);

    /**
     * Find transfers pending records request
     */
    @Query("SELECT wr FROM WithdrawalRecord wr WHERE " +
           "wr.isTransferring = true AND " +
           "wr.recordsRequestedBy IS NULL")
    List<WithdrawalRecord> findTransfersPendingRecordsRequest();

    // ========================================================================
    // DATE RANGE QUERIES
    // ========================================================================

    /**
     * Find withdrawals by date range
     */
    List<WithdrawalRecord> findByWithdrawalDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find withdrawals after specific date
     */
    List<WithdrawalRecord> findByWithdrawalDateAfter(LocalDate date);

    /**
     * Find by effective date
     */
    List<WithdrawalRecord> findByEffectiveDate(LocalDate effectiveDate);

    /**
     * Find recent withdrawals
     */
    List<WithdrawalRecord> findByCreatedAtAfter(LocalDateTime dateTime);

    // ========================================================================
    // EXIT INTERVIEW QUERIES
    // ========================================================================

    /**
     * Find withdrawal records without exit interview
     */
    @Query("SELECT wr FROM WithdrawalRecord wr WHERE wr.interviewDate IS NULL")
    List<WithdrawalRecord> findWithoutExitInterview();

    /**
     * Find by satisfaction rating
     */
    List<WithdrawalRecord> findBySatisfactionRating(WithdrawalRecord.SatisfactionRating rating);

    /**
     * Find withdrawal records with negative feedback
     */
    @Query("SELECT wr FROM WithdrawalRecord wr WHERE " +
           "wr.satisfactionRating IN ('DISSATISFIED', 'VERY_DISSATISFIED')")
    List<WithdrawalRecord> findWithNegativeFeedback();

    // ========================================================================
    // SEARCH QUERIES
    // ========================================================================

    /**
     * Search by student name
     */
    @Query("SELECT wr FROM WithdrawalRecord wr WHERE " +
           "LOWER(wr.student.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(wr.student.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<WithdrawalRecord> searchByStudentName(@Param("searchTerm") String searchTerm);

    /**
     * Search by parent email
     */
    List<WithdrawalRecord> findByParentEmailContainingIgnoreCase(String email);

    /**
     * Search by parent phone
     */
    List<WithdrawalRecord> findByParentPhoneContaining(String phone);

    // ========================================================================
    // WORKFLOW QUERIES
    // ========================================================================

    /**
     * Find withdrawal records processed by specific staff
     */
    @Query("SELECT wr FROM WithdrawalRecord wr WHERE wr.processedBy.id = :staffId")
    List<WithdrawalRecord> findProcessedByStaff(@Param("staffId") Long staffId);

    /**
     * Find completed withdrawals
     */
    List<WithdrawalRecord> findByStatusOrderByProcessingDateDesc(WithdrawalStatus status);

    /**
     * Find pending withdrawals (draft or pending clearance)
     */
    @Query("SELECT wr FROM WithdrawalRecord wr WHERE " +
           "wr.status IN ('DRAFT', 'PENDING_CLEARANCE') " +
           "ORDER BY wr.withdrawalDate ASC")
    List<WithdrawalRecord> findPendingWithdrawals();

    // ========================================================================
    // STATISTICS QUERIES
    // ========================================================================

    /**
     * Count withdrawals by status
     */
    long countByStatus(WithdrawalStatus status);

    /**
     * Count withdrawals by type
     */
    long countByWithdrawalType(WithdrawalType withdrawalType);

    /**
     * Count withdrawals by final status
     */
    long countByFinalStatus(FinalStatus finalStatus);

    /**
     * Count transfers
     */
    long countByIsTransferringTrue();

    /**
     * Get withdrawal counts by type
     */
    @Query("SELECT wr.withdrawalType, COUNT(wr) FROM WithdrawalRecord wr " +
           "GROUP BY wr.withdrawalType")
    List<Object[]> getCountByWithdrawalType();

    /**
     * Get withdrawal counts by month
     */
    @Query("SELECT FUNCTION('YEAR', wr.withdrawalDate), FUNCTION('MONTH', wr.withdrawalDate), COUNT(wr) " +
           "FROM WithdrawalRecord wr " +
           "WHERE wr.withdrawalDate BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('YEAR', wr.withdrawalDate), FUNCTION('MONTH', wr.withdrawalDate) " +
           "ORDER BY FUNCTION('YEAR', wr.withdrawalDate), FUNCTION('MONTH', wr.withdrawalDate)")
    List<Object[]> getWithdrawalCountsByMonth(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    /**
     * Get clearance completion statistics
     */
    @Query("SELECT AVG(wr.clearedItems * 1.0 / wr.totalClearanceItems) FROM WithdrawalRecord wr " +
           "WHERE wr.totalClearanceItems > 0")
    Double getAverageClearanceCompletion();

    // ========================================================================
    // VALIDATION QUERIES
    // ========================================================================

    /**
     * Check if withdrawal number exists
     */
    boolean existsByWithdrawalNumber(String withdrawalNumber);

    /**
     * Check if student has pending withdrawal
     */
    @Query("SELECT COUNT(wr) > 0 FROM WithdrawalRecord wr WHERE " +
           "wr.student.id = :studentId AND " +
           "wr.status NOT IN ('COMPLETED', 'CANCELLED')")
    boolean hasPendingWithdrawal(@Param("studentId") Long studentId);

    // ========================================================================
    // REPORT QUERIES
    // ========================================================================

    /**
     * Find withdrawal records for report (with exit interviews)
     */
    @Query("SELECT wr FROM WithdrawalRecord wr WHERE " +
           "wr.status = 'COMPLETED' AND " +
           "wr.interviewDate IS NOT NULL " +
           "ORDER BY wr.withdrawalDate DESC")
    List<WithdrawalRecord> findForExitInterviewReport();

    /**
     * Find withdrawal records needing attention
     */
    @Query("SELECT wr FROM WithdrawalRecord wr WHERE " +
           "(wr.status = 'PENDING_CLEARANCE' AND wr.withdrawalDate < :cutoffDate) OR " +
           "(wr.status = 'CLEARANCE_IN_PROGRESS' AND wr.allCleared = false) OR " +
           "(wr.isTransferring = true AND wr.recordsRequestedBy IS NULL)")
    List<WithdrawalRecord> findNeedingAttention(@Param("cutoffDate") LocalDate cutoffDate);
}
