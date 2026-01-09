package com.heronix.repository;

import com.heronix.model.domain.ScheduleChangeRequest;
import com.heronix.model.domain.ScheduleChangeRequest.RequestStatus;
import com.heronix.model.domain.ScheduleChangeRequest.RequestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Schedule Change Request entities
 * Provides data access for schedule change request management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface ScheduleChangeRequestRepository extends JpaRepository<ScheduleChangeRequest, Long> {

    // ========================================================================
    // BASIC QUERIES
    // ========================================================================

    /**
     * Find all requests for a student
     */
    List<ScheduleChangeRequest> findByStudentIdOrderByRequestDateDesc(Long studentId);

    /**
     * Find requests by status
     */
    List<ScheduleChangeRequest> findByStatusOrderByRequestDateAsc(RequestStatus status);

    /**
     * Find requests by request type
     */
    List<ScheduleChangeRequest> findByRequestTypeOrderByRequestDateDesc(RequestType requestType);

    /**
     * Find requests reviewed by a specific teacher/counselor
     */
    List<ScheduleChangeRequest> findByReviewedByIdOrderByReviewedDateDesc(Long reviewerId);

    // ========================================================================
    // STATUS QUERIES
    // ========================================================================

    /**
     * Find all pending requests
     */
    @Query("SELECT scr FROM ScheduleChangeRequest scr WHERE scr.status = 'PENDING' " +
           "ORDER BY scr.priorityLevel DESC, scr.requestDate ASC")
    List<ScheduleChangeRequest> findPendingRequests();

    /**
     * Find pending requests for a student
     */
    @Query("SELECT scr FROM ScheduleChangeRequest scr " +
           "WHERE scr.student.id = :studentId " +
           "AND scr.status = 'PENDING' " +
           "ORDER BY scr.requestDate DESC")
    List<ScheduleChangeRequest> findPendingRequestsByStudent(@Param("studentId") Long studentId);

    /**
     * Find approved but not yet completed requests
     */
    @Query("SELECT scr FROM ScheduleChangeRequest scr " +
           "WHERE scr.status = 'APPROVED' " +
           "ORDER BY scr.reviewedDate ASC")
    List<ScheduleChangeRequest> findApprovedNotCompleted();

    /**
     * Find denied requests
     */
    @Query("SELECT scr FROM ScheduleChangeRequest scr " +
           "WHERE scr.status = 'DENIED' " +
           "ORDER BY scr.reviewedDate DESC")
    List<ScheduleChangeRequest> findDeniedRequests();

    // ========================================================================
    // DATE RANGE QUERIES
    // ========================================================================

    /**
     * Find requests submitted within a date range
     */
    @Query("SELECT scr FROM ScheduleChangeRequest scr " +
           "WHERE scr.requestDate BETWEEN :startDate AND :endDate " +
           "ORDER BY scr.requestDate DESC")
    List<ScheduleChangeRequest> findByRequestDateBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find requests reviewed within a date range
     */
    @Query("SELECT scr FROM ScheduleChangeRequest scr " +
           "WHERE scr.reviewedDate BETWEEN :startDate AND :endDate " +
           "ORDER BY scr.reviewedDate DESC")
    List<ScheduleChangeRequest> findByReviewedDateBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find overdue pending requests (older than X days)
     */
    @Query("SELECT scr FROM ScheduleChangeRequest scr " +
           "WHERE scr.status = 'PENDING' " +
           "AND scr.requestDate < :cutoffDate " +
           "ORDER BY scr.requestDate ASC")
    List<ScheduleChangeRequest> findOverdueRequests(@Param("cutoffDate") LocalDateTime cutoffDate);

    // ========================================================================
    // COUNSELOR/ADMINISTRATOR QUERIES
    // ========================================================================

    /**
     * Find pending requests for a counselor's assigned students
     * TODO: Implement this query when counselor assignment is added to Student entity
     * For now, returns all pending requests
     */
    @Query("SELECT scr FROM ScheduleChangeRequest scr " +
           "WHERE scr.status = 'PENDING' " +
           "ORDER BY scr.priorityLevel DESC, scr.requestDate ASC")
    List<ScheduleChangeRequest> findPendingRequestsForCounselor(@Param("counselorId") Long counselorId);

    /**
     * Find high priority pending requests
     */
    @Query("SELECT scr FROM ScheduleChangeRequest scr " +
           "WHERE scr.status = 'PENDING' " +
           "AND scr.priorityLevel >= :minPriority " +
           "ORDER BY scr.priorityLevel DESC, scr.requestDate ASC")
    List<ScheduleChangeRequest> findHighPriorityRequests(@Param("minPriority") Integer minPriority);

    // ========================================================================
    // COURSE-SPECIFIC QUERIES
    // ========================================================================

    /**
     * Find requests involving a specific course (current or requested)
     */
    @Query("SELECT scr FROM ScheduleChangeRequest scr " +
           "WHERE scr.currentCourse.id = :courseId " +
           "OR scr.requestedCourse.id = :courseId " +
           "ORDER BY scr.requestDate DESC")
    List<ScheduleChangeRequest> findRequestsForCourse(@Param("courseId") Long courseId);

    /**
     * Find pending ADD requests for a specific course
     */
    @Query("SELECT scr FROM ScheduleChangeRequest scr " +
           "WHERE scr.status = 'PENDING' " +
           "AND scr.requestType = 'ADD' " +
           "AND scr.requestedCourse.id = :courseId " +
           "ORDER BY scr.priorityLevel DESC, scr.requestDate ASC")
    List<ScheduleChangeRequest> findPendingAddRequestsForCourse(@Param("courseId") Long courseId);

    /**
     * Find pending DROP requests for a specific course
     */
    @Query("SELECT scr FROM ScheduleChangeRequest scr " +
           "WHERE scr.status = 'PENDING' " +
           "AND scr.requestType = 'DROP' " +
           "AND scr.currentCourse.id = :courseId " +
           "ORDER BY scr.priorityLevel DESC, scr.requestDate ASC")
    List<ScheduleChangeRequest> findPendingDropRequestsForCourse(@Param("courseId") Long courseId);

    // ========================================================================
    // ACADEMIC YEAR/GRADING PERIOD QUERIES
    // ========================================================================

    /**
     * Find requests for a specific academic year
     */
    @Query("SELECT scr FROM ScheduleChangeRequest scr " +
           "WHERE scr.academicYear.id = :academicYearId " +
           "ORDER BY scr.requestDate DESC")
    List<ScheduleChangeRequest> findByAcademicYear(@Param("academicYearId") Long academicYearId);

    /**
     * Find requests for a specific grading period
     */
    @Query("SELECT scr FROM ScheduleChangeRequest scr " +
           "WHERE scr.gradingPeriod.id = :gradingPeriodId " +
           "ORDER BY scr.requestDate DESC")
    List<ScheduleChangeRequest> findByGradingPeriod(@Param("gradingPeriodId") Long gradingPeriodId);

    // ========================================================================
    // STATISTICS QUERIES
    // ========================================================================

    /**
     * Count pending requests
     */
    @Query("SELECT COUNT(scr) FROM ScheduleChangeRequest scr " +
           "WHERE scr.status = 'PENDING'")
    Long countPendingRequests();

    /**
     * Count pending requests for a student
     */
    @Query("SELECT COUNT(scr) FROM ScheduleChangeRequest scr " +
           "WHERE scr.student.id = :studentId " +
           "AND scr.status = 'PENDING'")
    Long countPendingRequestsByStudent(@Param("studentId") Long studentId);

    /**
     * Count requests by status
     */
    @Query("SELECT COUNT(scr) FROM ScheduleChangeRequest scr " +
           "WHERE scr.status = :status")
    Long countByStatus(@Param("status") RequestStatus status);

    /**
     * Count requests by request type
     */
    @Query("SELECT COUNT(scr) FROM ScheduleChangeRequest scr " +
           "WHERE scr.requestType = :requestType")
    Long countByRequestType(@Param("requestType") RequestType requestType);

    /**
     * Count overdue pending requests
     */
    @Query("SELECT COUNT(scr) FROM ScheduleChangeRequest scr " +
           "WHERE scr.status = 'PENDING' " +
           "AND scr.requestDate < :cutoffDate")
    Long countOverdueRequests(@Param("cutoffDate") LocalDateTime cutoffDate);

    // ========================================================================
    // VALIDATION QUERIES
    // ========================================================================

    /**
     * Check if student has pending request for the same course
     */
    @Query("SELECT COUNT(scr) > 0 FROM ScheduleChangeRequest scr " +
           "WHERE scr.student.id = :studentId " +
           "AND scr.status = 'PENDING' " +
           "AND (scr.currentCourse.id = :courseId OR scr.requestedCourse.id = :courseId)")
    boolean hasPendingRequestForCourse(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId);

    /**
     * Find duplicate pending requests
     */
    @Query("SELECT scr FROM ScheduleChangeRequest scr " +
           "WHERE scr.student.id = :studentId " +
           "AND scr.status = 'PENDING' " +
           "AND scr.requestType = :requestType " +
           "AND ((:currentCourseId IS NULL AND scr.currentCourse IS NULL) OR scr.currentCourse.id = :currentCourseId) " +
           "AND ((:requestedCourseId IS NULL AND scr.requestedCourse IS NULL) OR scr.requestedCourse.id = :requestedCourseId)")
    List<ScheduleChangeRequest> findDuplicateRequests(
            @Param("studentId") Long studentId,
            @Param("requestType") RequestType requestType,
            @Param("currentCourseId") Long currentCourseId,
            @Param("requestedCourseId") Long requestedCourseId);

    // ========================================================================
    // REPORTING QUERIES
    // ========================================================================

    /**
     * Get request summary by status for a date range
     */
    @Query("SELECT scr.status, COUNT(scr) FROM ScheduleChangeRequest scr " +
           "WHERE scr.requestDate BETWEEN :startDate AND :endDate " +
           "GROUP BY scr.status")
    List<Object[]> getRequestSummaryByStatus(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Get request summary by type for a date range
     */
    @Query("SELECT scr.requestType, COUNT(scr) FROM ScheduleChangeRequest scr " +
           "WHERE scr.requestDate BETWEEN :startDate AND :endDate " +
           "GROUP BY scr.requestType")
    List<Object[]> getRequestSummaryByType(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
