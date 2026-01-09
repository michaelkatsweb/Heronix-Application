package com.heronix.repository;

import com.heronix.model.domain.TransferStudent;
import com.heronix.model.domain.TransferStudent.TransferStatus;
import com.heronix.model.domain.TransferStudent.TransferType;
import com.heronix.model.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for TransferStudent Entity
 * Handles database operations for incoming transfer student processing
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@Repository
public interface TransferStudentRepository extends JpaRepository<TransferStudent, Long> {

    // ========================================================================
    // BASIC QUERIES
    // ========================================================================

    /**
     * Find transfer record by transfer number
     */
    Optional<TransferStudent> findByTransferNumber(String transferNumber);

    /**
     * Find all transfers by status
     */
    List<TransferStudent> findByStatus(TransferStatus status);

    /**
     * Find transfer by student
     */
    Optional<TransferStudent> findByStudent(Student student);

    /**
     * Find all transfers for a student
     */
    List<TransferStudent> findAllByStudent(Student student);

    /**
     * Find by transfer type
     */
    List<TransferStudent> findByTransferType(TransferType transferType);

    // ========================================================================
    // DATE RANGE QUERIES
    // ========================================================================

    /**
     * Find transfers by date range
     */
    List<TransferStudent> findByTransferDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find transfers expected to enroll in date range
     */
    List<TransferStudent> findByExpectedEnrollmentDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find recent transfers
     */
    @Query("SELECT ts FROM TransferStudent ts WHERE ts.transferDate >= :cutoffDate ORDER BY ts.transferDate DESC")
    List<TransferStudent> findRecentTransfers(@Param("cutoffDate") LocalDate cutoffDate);

    // ========================================================================
    // RECORDS STATUS QUERIES
    // ========================================================================

    /**
     * Find transfers pending records
     */
    @Query("SELECT ts FROM TransferStudent ts WHERE " +
           "ts.status IN ('RECORDS_REQUESTED', 'PARTIAL_RECORDS') AND " +
           "ts.allRecordsReceived = false")
    List<TransferStudent> findPendingRecords();

    /**
     * Find transfers with complete records
     */
    List<TransferStudent> findByAllRecordsReceivedTrue();

    /**
     * Find transfers missing specific records
     */
    @Query("SELECT ts FROM TransferStudent ts WHERE " +
           "ts.transcriptReceived = false OR " +
           "ts.immunizationRecordsReceived = false")
    List<TransferStudent> findMissingCriticalRecords();

    /**
     * Find transfers ready for placement
     */
    @Query("SELECT ts FROM TransferStudent ts WHERE " +
           "ts.status = 'READY_FOR_PLACEMENT' AND " +
           "ts.allRecordsReceived = true AND " +
           "ts.proposedGradeLevel IS NOT NULL")
    List<TransferStudent> findReadyForPlacement();

    // ========================================================================
    // PREVIOUS SCHOOL QUERIES
    // ========================================================================

    /**
     * Find by previous school name
     */
    List<TransferStudent> findByPreviousSchoolNameContainingIgnoreCase(String schoolName);

    /**
     * Find by previous school district
     */
    List<TransferStudent> findByPreviousSchoolDistrictContainingIgnoreCase(String district);

    /**
     * Find by previous school state
     */
    List<TransferStudent> findByPreviousSchoolState(String state);

    /**
     * Get count by previous school
     */
    @Query("SELECT ts.previousSchoolName, COUNT(ts) FROM TransferStudent ts " +
           "WHERE ts.previousSchoolName IS NOT NULL " +
           "GROUP BY ts.previousSchoolName " +
           "ORDER BY COUNT(ts) DESC")
    List<Object[]> getCountByPreviousSchool();

    // ========================================================================
    // SEARCH QUERIES
    // ========================================================================

    /**
     * Search by student name
     */
    @Query("SELECT ts FROM TransferStudent ts WHERE " +
           "LOWER(ts.studentFirstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ts.studentLastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<TransferStudent> searchByStudentName(@Param("searchTerm") String searchTerm);

    /**
     * Search by parent name
     */
    @Query("SELECT ts FROM TransferStudent ts WHERE " +
           "LOWER(ts.parentFirstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ts.parentLastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<TransferStudent> searchByParentName(@Param("searchTerm") String searchTerm);

    /**
     * Search by parent email
     */
    List<TransferStudent> findByParentEmailContainingIgnoreCase(String email);

    /**
     * Search by parent phone
     */
    List<TransferStudent> findByParentPhoneContaining(String phone);

    // ========================================================================
    // COUNSELOR QUERIES
    // ========================================================================

    /**
     * Find by assigned counselor
     */
    @Query("SELECT ts FROM TransferStudent ts WHERE ts.assignedCounselor.id = :counselorId")
    List<TransferStudent> findByAssignedCounselor(@Param("counselorId") Long counselorId);

    /**
     * Find unassigned transfers
     */
    @Query("SELECT ts FROM TransferStudent ts WHERE " +
           "ts.assignedCounselor IS NULL AND " +
           "ts.status NOT IN ('ENROLLED', 'CANCELLED')")
    List<TransferStudent> findUnassignedTransfers();

    /**
     * Find transfers needing counselor review
     */
    @Query("SELECT ts FROM TransferStudent ts WHERE " +
           "ts.assignedCounselor IS NOT NULL AND " +
           "ts.counselorReviewDate IS NULL AND " +
           "ts.status = 'READY_FOR_PLACEMENT'")
    List<TransferStudent> findNeedingCounselorReview();

    // ========================================================================
    // PLACEMENT QUERIES
    // ========================================================================

    /**
     * Find by proposed grade level
     */
    List<TransferStudent> findByProposedGradeLevel(String gradeLevel);

    /**
     * Find requiring placement test
     */
    List<TransferStudent> findByPlacementTestRequiredTrueAndPlacementTestDateIsNull();

    /**
     * Find requiring English proficiency assessment
     */
    List<TransferStudent> findByEnglishProficiencyRequiredTrueAndEnglishProficiencyLevelIsNull();

    // ========================================================================
    // MID-YEAR TRANSFER QUERIES
    // ========================================================================

    /**
     * Find mid-year transfers
     */
    List<TransferStudent> findByIsMidYearTransferTrue();

    /**
     * Find emergency transfers
     */
    List<TransferStudent> findByIsEmergencyTransferTrue();

    /**
     * Find transfers requiring interpreter
     */
    List<TransferStudent> findByRequiresInterpreterTrue();

    // ========================================================================
    // STATISTICS QUERIES
    // ========================================================================

    /**
     * Count by status
     */
    long countByStatus(TransferStatus status);

    /**
     * Count by transfer type
     */
    long countByTransferType(TransferType transferType);

    /**
     * Count mid-year transfers
     */
    long countByIsMidYearTransferTrue();

    /**
     * Get transfer counts by type
     */
    @Query("SELECT ts.transferType, COUNT(ts) FROM TransferStudent ts " +
           "GROUP BY ts.transferType")
    List<Object[]> getCountByTransferType();

    /**
     * Get transfer counts by grade level
     */
    @Query("SELECT ts.proposedGradeLevel, COUNT(ts) FROM TransferStudent ts " +
           "WHERE ts.proposedGradeLevel IS NOT NULL " +
           "GROUP BY ts.proposedGradeLevel " +
           "ORDER BY ts.proposedGradeLevel")
    List<Object[]> getCountByGradeLevel();

    /**
     * Get transfer counts by month
     */
    @Query("SELECT FUNCTION('YEAR', ts.transferDate), FUNCTION('MONTH', ts.transferDate), COUNT(ts) " +
           "FROM TransferStudent ts " +
           "WHERE ts.transferDate BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('YEAR', ts.transferDate), FUNCTION('MONTH', ts.transferDate) " +
           "ORDER BY FUNCTION('YEAR', ts.transferDate), FUNCTION('MONTH', ts.transferDate)")
    List<Object[]> getTransferCountsByMonth(@Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    /**
     * Get average records completion percentage
     */
    @Query("SELECT AVG(ts.recordsReceived * 1.0 / ts.totalRecordsExpected) FROM TransferStudent ts " +
           "WHERE ts.totalRecordsExpected > 0")
    Double getAverageRecordsCompletion();

    // ========================================================================
    // VALIDATION QUERIES
    // ========================================================================

    /**
     * Check if transfer number exists
     */
    boolean existsByTransferNumber(String transferNumber);

    /**
     * Check if student has pending transfer
     */
    @Query("SELECT COUNT(ts) > 0 FROM TransferStudent ts WHERE " +
           "ts.student.id = :studentId AND " +
           "ts.status NOT IN ('ENROLLED', 'CANCELLED')")
    boolean hasPendingTransfer(@Param("studentId") Long studentId);

    // ========================================================================
    // REPORTING QUERIES
    // ========================================================================

    /**
     * Find transfers needing attention (delayed processing)
     */
    @Query("SELECT ts FROM TransferStudent ts WHERE " +
           "(ts.status = 'RECORDS_REQUESTED' AND ts.transferDate < :cutoffDate) OR " +
           "(ts.status = 'READY_FOR_PLACEMENT' AND ts.assignedCounselor IS NULL) OR " +
           "(ts.placementTestRequired = true AND ts.placementTestDate IS NULL)")
    List<TransferStudent> findNeedingAttention(@Param("cutoffDate") LocalDate cutoffDate);

    /**
     * Find completed enrollments from transfers
     */
    @Query("SELECT ts FROM TransferStudent ts WHERE " +
           "ts.status = 'ENROLLED' AND " +
           "ts.enrollmentDate IS NOT NULL " +
           "ORDER BY ts.enrollmentDate DESC")
    List<TransferStudent> findCompletedEnrollments();
}
