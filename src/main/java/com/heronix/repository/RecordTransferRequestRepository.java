package com.heronix.repository;

import com.heronix.model.domain.EnrollmentApplication;
import com.heronix.model.domain.RecordTransferRequest;
import com.heronix.model.domain.RecordTransferRequest.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Record Transfer Requests
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Inquiry and Registration System
 */
@Repository
public interface RecordTransferRequestRepository extends JpaRepository<RecordTransferRequest, Long> {

    // ========================================================================
    // BASIC QUERIES
    // ========================================================================

    /**
     * Find request by request number
     */
    Optional<RecordTransferRequest> findByRequestNumber(String requestNumber);

    /**
     * Find all requests for an application
     */
    List<RecordTransferRequest> findByApplication(EnrollmentApplication application);

    /**
     * Find requests by status
     */
    List<RecordTransferRequest> findByStatus(RequestStatus status);

    // ========================================================================
    // STATUS TRACKING
    // ========================================================================

    /**
     * Find requests that have been sent (not draft)
     */
    @Query("SELECT r FROM RecordTransferRequest r WHERE r.status != 'DRAFT'")
    List<RecordTransferRequest> findActiveSentRequests();

    /**
     * Find requests waiting for response
     */
    @Query("SELECT r FROM RecordTransferRequest r WHERE " +
           "r.status IN ('SENT', 'ACKNOWLEDGED', 'IN_PROGRESS')")
    List<RecordTransferRequest> findRequestsAwaitingResponse();

    /**
     * Find overdue requests (past due date and not complete)
     */
    @Query("SELECT r FROM RecordTransferRequest r WHERE " +
           "r.dueDate < CURRENT_DATE AND " +
           "r.status NOT IN ('COMPLETE', 'CANCELLED', 'FAILED')")
    List<RecordTransferRequest> findOverdueRequests();

    /**
     * Find requests that need follow-up (due soon or overdue)
     */
    @Query("SELECT r FROM RecordTransferRequest r WHERE " +
           "r.nextFollowUpDate IS NOT NULL AND " +
           "r.nextFollowUpDate <= CURRENT_DATE AND " +
           "r.status NOT IN ('COMPLETE', 'CANCELLED', 'FAILED')")
    List<RecordTransferRequest> findRequestsNeedingFollowUp();

    // ========================================================================
    // RECORD TYPE TRACKING
    // ========================================================================

    /**
     * Find requests where transcript is still pending
     */
    @Query("SELECT r FROM RecordTransferRequest r WHERE " +
           "r.requestTranscript = true AND " +
           "(r.transcriptReceived = false OR r.transcriptReceived IS NULL) AND " +
           "r.status NOT IN ('CANCELLED', 'FAILED')")
    List<RecordTransferRequest> findRequestsAwaitingTranscript();

    /**
     * Find requests where IEP is still pending
     */
    @Query("SELECT r FROM RecordTransferRequest r WHERE " +
           "r.requestIEP = true AND " +
           "(r.iepReceived = false OR r.iepReceived IS NULL) AND " +
           "r.status NOT IN ('CANCELLED', 'FAILED')")
    List<RecordTransferRequest> findRequestsAwaitingIEP();

    /**
     * Find requests where 504 plan is still pending
     */
    @Query("SELECT r FROM RecordTransferRequest r WHERE " +
           "r.request504Plan = true AND " +
           "(r.plan504Received = false OR r.plan504Received IS NULL) AND " +
           "r.status NOT IN ('CANCELLED', 'FAILED')")
    List<RecordTransferRequest> findRequestsAwaiting504Plan();

    // ========================================================================
    // SCHOOL TRACKING
    // ========================================================================

    /**
     * Find requests from a specific previous school
     */
    List<RecordTransferRequest> findBySchoolName(String schoolName);

    /**
     * Find requests from schools in a specific district
     */
    List<RecordTransferRequest> findBySchoolDistrict(String schoolDistrict);

    /**
     * Find requests from schools in a specific state
     */
    List<RecordTransferRequest> findBySchoolState(String schoolState);

    // ========================================================================
    // STAFF TRACKING
    // ========================================================================

    /**
     * Find requests created by specific staff member
     */
    @Query("SELECT r FROM RecordTransferRequest r WHERE r.createdBy.id = :staffId")
    List<RecordTransferRequest> findByCreatedByStaffId(@Param("staffId") Long staffId);

    /**
     * Find requests verified by specific staff member
     */
    @Query("SELECT r FROM RecordTransferRequest r WHERE r.verifiedBy.id = :staffId")
    List<RecordTransferRequest> findByVerifiedByStaffId(@Param("staffId") Long staffId);

    // ========================================================================
    // DATE QUERIES
    // ========================================================================

    /**
     * Find requests sent within a date range
     */
    @Query("SELECT r FROM RecordTransferRequest r WHERE " +
           "r.requestDate >= :startDate AND r.requestDate <= :endDate")
    List<RecordTransferRequest> findByRequestDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find requests completed within a date range
     */
    @Query("SELECT r FROM RecordTransferRequest r WHERE " +
           "r.completedAt >= :startDate AND r.completedAt < :endDate")
    List<RecordTransferRequest> findByCompletedAtBetween(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    // ========================================================================
    // STATISTICS
    // ========================================================================

    /**
     * Count requests by status
     */
    Long countByStatus(RequestStatus status);

    /**
     * Count overdue requests
     */
    @Query("SELECT COUNT(r) FROM RecordTransferRequest r WHERE " +
           "r.dueDate < CURRENT_DATE AND " +
           "r.status NOT IN ('COMPLETE', 'CANCELLED', 'FAILED')")
    Long countOverdueRequests();

    /**
     * Count requests awaiting transcript
     */
    @Query("SELECT COUNT(r) FROM RecordTransferRequest r WHERE " +
           "r.requestTranscript = true AND " +
           "(r.transcriptReceived = false OR r.transcriptReceived IS NULL) AND " +
           "r.status NOT IN ('CANCELLED', 'FAILED')")
    Long countRequestsAwaitingTranscript();
}
