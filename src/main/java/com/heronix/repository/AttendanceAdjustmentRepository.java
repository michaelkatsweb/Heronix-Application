package com.heronix.repository;

import com.heronix.model.domain.AttendanceAdjustment;
import com.heronix.model.domain.AttendanceAdjustment.AdjustmentType;
import com.heronix.model.domain.AttendanceAdjustment.ApprovalStatus;
import com.heronix.model.domain.AttendanceAdjustment.AdjustmentSource;
import com.heronix.model.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for AttendanceAdjustment entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 58 - Attendance Enhancement - January 2026
 */
@Repository
public interface AttendanceAdjustmentRepository extends JpaRepository<AttendanceAdjustment, Long> {

    // ========================================================================
    // QUERIES BY STUDENT
    // ========================================================================

    /**
     * Find all adjustments for a student
     */
    List<AttendanceAdjustment> findByStudentOrderByAdjustmentDateDesc(Student student);

    /**
     * Find adjustments for a student within a date range
     */
    List<AttendanceAdjustment> findByStudentAndAttendanceDateBetweenOrderByAttendanceDateDesc(
            Student student, LocalDate startDate, LocalDate endDate);

    /**
     * Find adjustments for a student on a specific attendance date
     */
    List<AttendanceAdjustment> findByStudentAndAttendanceDate(Student student, LocalDate attendanceDate);

    // ========================================================================
    // QUERIES BY DATE
    // ========================================================================

    /**
     * Find all adjustments made on a specific date
     */
    @Query("SELECT a FROM AttendanceAdjustment a WHERE DATE(a.adjustmentDate) = :date ORDER BY a.adjustmentDate DESC")
    List<AttendanceAdjustment> findByAdjustmentDate(@Param("date") LocalDate date);

    /**
     * Find adjustments for attendance on a specific date
     */
    List<AttendanceAdjustment> findByAttendanceDateOrderByAdjustmentDateDesc(LocalDate attendanceDate);

    /**
     * Find adjustments made within a date range
     */
    List<AttendanceAdjustment> findByAdjustmentDateBetweenOrderByAdjustmentDateDesc(
            LocalDateTime startDate, LocalDateTime endDate);

    // ========================================================================
    // QUERIES BY STATUS
    // ========================================================================

    /**
     * Find pending adjustments requiring approval
     */
    List<AttendanceAdjustment> findByApprovalStatusOrderByAdjustmentDateAsc(ApprovalStatus status);

    /**
     * Find pending adjustments for a specific approver
     */
    List<AttendanceAdjustment> findByApprovalStatusAndPendingApproverOrderByAdjustmentDateAsc(
            ApprovalStatus status, String approver);

    /**
     * Find unapplied approved adjustments
     */
    List<AttendanceAdjustment> findByApprovalStatusInAndAppliedFalseOrderByAttendanceDateAsc(
            List<ApprovalStatus> statuses);

    /**
     * Count pending approvals
     */
    long countByApprovalStatus(ApprovalStatus status);

    // ========================================================================
    // QUERIES BY USER
    // ========================================================================

    /**
     * Find adjustments made by a specific user
     */
    List<AttendanceAdjustment> findByAdjustedByOrderByAdjustmentDateDesc(String username);

    /**
     * Find adjustments made by a user within date range
     */
    List<AttendanceAdjustment> findByAdjustedByAndAdjustmentDateBetweenOrderByAdjustmentDateDesc(
            String username, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find adjustments approved by a specific user
     */
    List<AttendanceAdjustment> findByApprovedByOrderByApprovalDateDesc(String username);

    // ========================================================================
    // QUERIES BY TYPE AND SOURCE
    // ========================================================================

    /**
     * Find adjustments by type
     */
    List<AttendanceAdjustment> findByAdjustmentTypeOrderByAdjustmentDateDesc(AdjustmentType type);

    /**
     * Find adjustments by source
     */
    List<AttendanceAdjustment> findBySourceOrderByAdjustmentDateDesc(AdjustmentSource source);

    /**
     * Find parent-initiated adjustments
     */
    @Query("SELECT a FROM AttendanceAdjustment a WHERE a.source IN " +
           "('PARENT_NOTE', 'PARENT_CALL', 'PARENT_EMAIL', 'PARENT_PORTAL') " +
           "ORDER BY a.adjustmentDate DESC")
    List<AttendanceAdjustment> findParentInitiatedAdjustments();

    // ========================================================================
    // QUERIES FOR EXCUSE CODE
    // ========================================================================

    /**
     * Find adjustments using a specific excuse code
     */
    @Query("SELECT a FROM AttendanceAdjustment a WHERE a.excuseCode.id = :excuseCodeId " +
           "ORDER BY a.adjustmentDate DESC")
    List<AttendanceAdjustment> findByExcuseCodeId(@Param("excuseCodeId") Long excuseCodeId);

    /**
     * Find adjustments with documentation
     */
    List<AttendanceAdjustment> findByDocumentationProvidedTrueOrderByAdjustmentDateDesc();

    // ========================================================================
    // STATISTICS QUERIES
    // ========================================================================

    /**
     * Count adjustments by type within date range
     */
    @Query("SELECT a.adjustmentType, COUNT(a) FROM AttendanceAdjustment a " +
           "WHERE a.adjustmentDate BETWEEN :startDate AND :endDate " +
           "GROUP BY a.adjustmentType")
    List<Object[]> countByAdjustmentTypeInRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count adjustments by source within date range
     */
    @Query("SELECT a.source, COUNT(a) FROM AttendanceAdjustment a " +
           "WHERE a.adjustmentDate BETWEEN :startDate AND :endDate " +
           "GROUP BY a.source")
    List<Object[]> countBySourceInRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count adjustments by user within date range
     */
    @Query("SELECT a.adjustedBy, COUNT(a) FROM AttendanceAdjustment a " +
           "WHERE a.adjustmentDate BETWEEN :startDate AND :endDate " +
           "GROUP BY a.adjustedBy " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> countByUserInRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Get daily adjustment counts for a date range
     */
    @Query("SELECT DATE(a.adjustmentDate), COUNT(a) FROM AttendanceAdjustment a " +
           "WHERE a.adjustmentDate BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(a.adjustmentDate) " +
           "ORDER BY DATE(a.adjustmentDate)")
    List<Object[]> getDailyCountsInRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // ========================================================================
    // AUDIT QUERIES
    // ========================================================================

    /**
     * Find recent adjustments (last N days)
     */
    @Query("SELECT a FROM AttendanceAdjustment a WHERE a.adjustmentDate >= :since " +
           "ORDER BY a.adjustmentDate DESC")
    List<AttendanceAdjustment> findRecentAdjustments(@Param("since") LocalDateTime since);

    /**
     * Find adjustments for audit report
     */
    @Query("SELECT a FROM AttendanceAdjustment a " +
           "WHERE a.adjustmentDate BETWEEN :startDate AND :endDate " +
           "ORDER BY a.student.lastName, a.student.firstName, a.attendanceDate")
    List<AttendanceAdjustment> findForAuditReport(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
