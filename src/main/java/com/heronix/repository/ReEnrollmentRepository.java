package com.heronix.repository;

import com.heronix.model.domain.ReEnrollment;
import com.heronix.model.domain.ReEnrollment.ReEnrollmentStatus;
import com.heronix.model.domain.ReEnrollment.ReEnrollmentReason;
import com.heronix.model.domain.ReEnrollment.ApprovalDecision;
import com.heronix.model.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ReEnrollment Entity
 * Handles database operations for student re-enrollment processing
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@Repository
public interface ReEnrollmentRepository extends JpaRepository<ReEnrollment, Long> {

    // ========================================================================
    // BASIC QUERIES
    // ========================================================================

    /**
     * Find re-enrollment by re-enrollment number
     */
    Optional<ReEnrollment> findByReEnrollmentNumber(String reEnrollmentNumber);

    /**
     * Find all re-enrollments by status
     */
    List<ReEnrollment> findByStatus(ReEnrollmentStatus status);

    /**
     * Find by student
     */
    Optional<ReEnrollment> findByStudent(Student student);

    /**
     * Find all re-enrollments for a student
     */
    List<ReEnrollment> findAllByStudent(Student student);

    /**
     * Find by re-enrollment reason
     */
    List<ReEnrollment> findByReason(ReEnrollmentReason reason);

    // ========================================================================
    // DATE RANGE QUERIES
    // ========================================================================

    /**
     * Find re-enrollments by application date range
     */
    List<ReEnrollment> findByApplicationDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find re-enrollments expected to enroll in date range
     */
    List<ReEnrollment> findByIntendedEnrollmentDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find recent re-enrollments
     */
    @Query("SELECT re FROM ReEnrollment re WHERE re.applicationDate >= :cutoffDate ORDER BY re.applicationDate DESC")
    List<ReEnrollment> findRecentReEnrollments(@Param("cutoffDate") LocalDate cutoffDate);

    // ========================================================================
    // REVIEW & APPROVAL QUERIES
    // ========================================================================

    /**
     * Find re-enrollments pending review
     */
    @Query("SELECT re FROM ReEnrollment re WHERE " +
           "re.status = 'PENDING_REVIEW' AND " +
           "re.previousRecordsReviewed = false")
    List<ReEnrollment> findPendingReview();

    /**
     * Find by counselor
     */
    @Query("SELECT re FROM ReEnrollment re WHERE re.assignedCounselor.id = :counselorId")
    List<ReEnrollment> findByAssignedCounselor(@Param("counselorId") Long counselorId);

    /**
     * Find unassigned re-enrollments
     */
    @Query("SELECT re FROM ReEnrollment re WHERE " +
           "re.assignedCounselor IS NULL AND " +
           "re.status NOT IN ('ENROLLED', 'REJECTED', 'CANCELLED')")
    List<ReEnrollment> findUnassignedReEnrollments();

    /**
     * Find needing counselor review
     */
    @Query("SELECT re FROM ReEnrollment re WHERE " +
           "re.assignedCounselor IS NOT NULL AND " +
           "re.counselorReviewDate IS NULL AND " +
           "re.status = 'PENDING_APPROVAL'")
    List<ReEnrollment> findNeedingCounselorReview();

    /**
     * Find by counselor decision
     */
    List<ReEnrollment> findByCounselorDecision(ApprovalDecision decision);

    /**
     * Find by principal decision
     */
    List<ReEnrollment> findByPrincipalDecision(ApprovalDecision decision);

    // ========================================================================
    // RECORDS & FEES QUERIES
    // ========================================================================

    /**
     * Find with outstanding fees
     */
    List<ReEnrollment> findByHasOutstandingFeesTrueAndFeesPaidFalse();

    /**
     * Find with unpaid fees over amount
     */
    @Query("SELECT re FROM ReEnrollment re WHERE " +
           "re.hasOutstandingFees = true AND " +
           "re.feesPaid = false AND " +
           "re.outstandingFeesAmount > :amount")
    List<ReEnrollment> findWithUnpaidFeesOverAmount(@Param("amount") Double amount);

    /**
     * Find records not current
     */
    @Query("SELECT re FROM ReEnrollment re WHERE " +
           "re.transcriptCurrent = false OR " +
           "re.immunizationsCurrent = false OR " +
           "re.healthRecordsCurrent = false")
    List<ReEnrollment> findWithOutdatedRecords();

    // ========================================================================
    // PREVIOUS WITHDRAWAL QUERIES
    // ========================================================================

    /**
     * Find by previous withdrawal reason
     */
    List<ReEnrollment> findByPreviousWithdrawalReason(ReEnrollment.WithdrawalReason reason);

    /**
     * Find by time away (months)
     */
    @Query("SELECT re FROM ReEnrollment re WHERE re.monthsAway >= :minMonths AND re.monthsAway <= :maxMonths")
    List<ReEnrollment> findByTimeAwayMonths(@Param("minMonths") Integer minMonths, @Param("maxMonths") Integer maxMonths);

    /**
     * Find by years away
     */
    @Query("SELECT re FROM ReEnrollment re WHERE re.yearsAway >= :years")
    List<ReEnrollment> findByYearsAway(@Param("years") Integer years);

    /**
     * Find who were homeschooled
     */
    List<ReEnrollment> findByWasHomeschooledTrue();

    // ========================================================================
    // PLACEMENT QUERIES
    // ========================================================================

    /**
     * Find requiring placement test
     */
    List<ReEnrollment> findByPlacementTestRequiredTrueAndPlacementTestDateIsNull();

    /**
     * Find requiring academic interview
     */
    List<ReEnrollment> findByAcademicInterviewRequiredTrueAndAcademicInterviewDateIsNull();

    /**
     * Find by requested grade level
     */
    List<ReEnrollment> findByRequestedGradeLevel(String gradeLevel);

    /**
     * Find by assigned grade level
     */
    List<ReEnrollment> findByAssignedGradeLevel(String gradeLevel);

    // ========================================================================
    // CONDITIONAL APPROVAL QUERIES
    // ========================================================================

    /**
     * Find conditional approvals
     */
    List<ReEnrollment> findByConditionalApprovalTrue();

    /**
     * Find with behavioral contracts
     */
    List<ReEnrollment> findByBehavioralContractTrue();

    /**
     * Find with academic plans
     */
    List<ReEnrollment> findByAcademicPlanTrue();

    /**
     * Find on probation
     */
    List<ReEnrollment> findByProbationaryPeriodTrue();

    // ========================================================================
    // SEARCH QUERIES
    // ========================================================================

    /**
     * Search by student first or last name
     */
    @Query("SELECT re FROM ReEnrollment re WHERE " +
           "LOWER(re.student.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(re.student.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<ReEnrollment> searchByStudentName(@Param("searchTerm") String searchTerm);

    /**
     * Search by parent name
     */
    @Query("SELECT re FROM ReEnrollment re WHERE " +
           "LOWER(re.parent1FirstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(re.parent1LastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(re.parent2FirstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(re.parent2LastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<ReEnrollment> searchByParentName(@Param("searchTerm") String searchTerm);

    // ========================================================================
    // STATISTICS QUERIES
    // ========================================================================

    /**
     * Count by status
     */
    long countByStatus(ReEnrollmentStatus status);

    /**
     * Count by reason
     */
    long countByReason(ReEnrollmentReason reason);

    /**
     * Count conditional approvals
     */
    long countByConditionalApprovalTrue();

    /**
     * Get re-enrollment counts by reason
     */
    @Query("SELECT re.reason, COUNT(re) FROM ReEnrollment re " +
           "WHERE re.reason IS NOT NULL " +
           "GROUP BY re.reason")
    List<Object[]> getCountByReason();

    /**
     * Get re-enrollment counts by grade level
     */
    @Query("SELECT re.requestedGradeLevel, COUNT(re) FROM ReEnrollment re " +
           "WHERE re.requestedGradeLevel IS NOT NULL " +
           "GROUP BY re.requestedGradeLevel " +
           "ORDER BY re.requestedGradeLevel")
    List<Object[]> getCountByGradeLevel();

    /**
     * Get re-enrollment counts by month
     */
    @Query("SELECT FUNCTION('YEAR', re.applicationDate), FUNCTION('MONTH', re.applicationDate), COUNT(re) " +
           "FROM ReEnrollment re " +
           "WHERE re.applicationDate BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('YEAR', re.applicationDate), FUNCTION('MONTH', re.applicationDate) " +
           "ORDER BY FUNCTION('YEAR', re.applicationDate), FUNCTION('MONTH', re.applicationDate)")
    List<Object[]> getReEnrollmentCountsByMonth(@Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    /**
     * Get average time away (months)
     */
    @Query("SELECT AVG(re.monthsAway) FROM ReEnrollment re WHERE re.monthsAway IS NOT NULL")
    Double getAverageMonthsAway();

    // ========================================================================
    // VALIDATION QUERIES
    // ========================================================================

    /**
     * Check if re-enrollment number exists
     */
    boolean existsByReEnrollmentNumber(String reEnrollmentNumber);

    /**
     * Check if student has pending re-enrollment
     */
    @Query("SELECT COUNT(re) > 0 FROM ReEnrollment re WHERE " +
           "re.student.id = :studentId AND " +
           "re.status NOT IN ('ENROLLED', 'REJECTED', 'CANCELLED')")
    boolean hasPendingReEnrollment(@Param("studentId") Long studentId);

    /**
     * Check if student has been enrolled before
     */
    @Query("SELECT COUNT(re) > 0 FROM ReEnrollment re WHERE re.student.id = :studentId")
    boolean hasReEnrollmentHistory(@Param("studentId") Long studentId);

    // ========================================================================
    // REPORTING QUERIES
    // ========================================================================

    /**
     * Find re-enrollments needing attention
     */
    @Query("SELECT re FROM ReEnrollment re WHERE " +
           "(re.status = 'PENDING_REVIEW' AND re.applicationDate < :cutoffDate) OR " +
           "(re.status = 'PENDING_APPROVAL' AND re.assignedCounselor IS NULL) OR " +
           "(re.hasOutstandingFees = true AND re.feesPaid = false) OR " +
           "(re.placementTestRequired = true AND re.placementTestDate IS NULL)")
    List<ReEnrollment> findNeedingAttention(@Param("cutoffDate") LocalDate cutoffDate);

    /**
     * Find successfully enrolled
     */
    @Query("SELECT re FROM ReEnrollment re WHERE " +
           "re.status = 'ENROLLED' AND " +
           "re.enrollmentDate IS NOT NULL " +
           "ORDER BY re.enrollmentDate DESC")
    List<ReEnrollment> findSuccessfullyEnrolled();

    /**
     * Find rejected applications
     */
    @Query("SELECT re FROM ReEnrollment re WHERE " +
           "re.status = 'REJECTED' " +
           "ORDER BY re.applicationDate DESC")
    List<ReEnrollment> findRejectedApplications();
}
