package com.heronix.service;

import com.heronix.model.domain.EnrollmentApplication;
import com.heronix.model.domain.RecordTransferRequest;
import com.heronix.model.domain.RecordTransferRequest.RequestMethod;
import com.heronix.model.domain.RecordTransferRequest.RequestStatus;
import com.heronix.model.domain.User;
import com.heronix.repository.EnrollmentApplicationRepository;
import com.heronix.repository.RecordTransferRequestRepository;
import com.heronix.repository.UserRepository;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Record Transfer Service
 *
 * Manages requests for student records from previous schools.
 * Handles the complete lifecycle of requesting transcripts, IEPs, 504 plans,
 * and other educational records from sending schools.
 *
 * Key Responsibilities:
 * - Create and send record requests to previous schools
 * - Track request status and follow-up reminders
 * - Record receipt of requested documents
 * - Link received records to enrollment applications
 * - Generate overdue request reports
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Inquiry and Registration System
 */
@Slf4j
@Service
public class RecordTransferService {

    @Autowired
    private RecordTransferRequestRepository requestRepository;

    @Autowired
    private EnrollmentApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    // ========================================================================
    // REQUEST CREATION AND MANAGEMENT
    // ========================================================================

    /**
     * Create a new record transfer request
     */
    @Transactional
    public RecordTransferRequest createRequest(
            Long applicationId,
            String schoolName,
            String schoolDistrict,
            String schoolCity,
            String schoolState,
            String schoolPhone,
            String schoolEmail,
            Long createdByStaffId) {

        log.info("Creating record transfer request for application {}", applicationId);

        EnrollmentApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        User staff = userRepository.findById(createdByStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + createdByStaffId));

        String requestNumber = generateRequestNumber();
        LocalDate dueDate = LocalDate.now().plusDays(10); // 10 business days typical

        RecordTransferRequest request = RecordTransferRequest.builder()
                .application(application)
                .requestNumber(requestNumber)
                .status(RequestStatus.DRAFT)
                .requestDate(LocalDate.now())
                .dueDate(dueDate)
                .schoolName(schoolName)
                .schoolDistrict(schoolDistrict)
                .schoolCity(schoolCity)
                .schoolState(schoolState)
                .schoolPhone(schoolPhone)
                .schoolEmail(schoolEmail)
                .requestTranscript(true) // Always request transcript for transfers
                .transcriptReceived(false)
                .createdBy(staff)
                .createdAt(LocalDateTime.now())
                .build();

        // Set IEP/504 requests based on application
        if (Boolean.TRUE.equals(application.getHasIEP())) {
            request.setRequestIEP(true);
            request.setIepReceived(false);
        }
        if (Boolean.TRUE.equals(application.getHas504Plan())) {
            request.setRequest504Plan(true);
            request.setPlan504Received(false);
        }

        request = requestRepository.save(request);
        log.info("Created record transfer request: {} (ID: {})", requestNumber, request.getId());

        return request;
    }

    /**
     * Update record types being requested
     */
    @Transactional
    public RecordTransferRequest updateRequestedRecords(
            Long requestId,
            boolean requestTranscript,
            boolean requestIEP,
            boolean request504,
            boolean requestDiscipline,
            boolean requestAttendance,
            boolean requestHealth) {

        RecordTransferRequest request = getRequestById(requestId);

        if (request.getStatus() != RequestStatus.DRAFT) {
            throw new IllegalStateException("Can only update record types for DRAFT requests");
        }

        request.setRequestTranscript(requestTranscript);
        request.setRequestIEP(requestIEP);
        request.setRequest504Plan(request504);
        request.setRequestDisciplineRecords(requestDiscipline);
        request.setRequestAttendanceRecords(requestAttendance);
        request.setRequestHealthRecords(requestHealth);

        return requestRepository.save(request);
    }

    /**
     * Get request by ID
     */
    public RecordTransferRequest getRequestById(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + id));
    }

    /**
     * Get all requests for an application
     */
    public List<RecordTransferRequest> getRequestsForApplication(Long applicationId) {
        EnrollmentApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));
        return requestRepository.findByApplication(application);
    }

    // ========================================================================
    // REQUEST LIFECYCLE MANAGEMENT
    // ========================================================================

    /**
     * Send request to previous school
     */
    @Transactional
    public RecordTransferRequest sendRequest(
            Long requestId,
            RequestMethod method,
            Long staffId) {

        log.info("Sending record transfer request ID {} via {}", requestId, method);

        RecordTransferRequest request = getRequestById(requestId);

        if (request.getStatus() != RequestStatus.DRAFT) {
            throw new IllegalStateException("Can only send DRAFT requests");
        }

        request.setStatus(RequestStatus.SENT);
        request.setRequestMethod(method);
        request.setSentAt(LocalDateTime.now());
        request.setFollowUpCount(0);
        request.setNextFollowUpDate(LocalDate.now().plusDays(5)); // Follow up in 5 days

        String note = String.format("Request sent via %s", method.getDisplayName());
        request.addCommunicationNote(note);

        request = requestRepository.save(request);
        log.info("Request {} sent via {}", request.getRequestNumber(), method);

        return request;
    }

    /**
     * Mark request as acknowledged by previous school
     */
    @Transactional
    public RecordTransferRequest markAcknowledged(Long requestId, Long staffId) {
        log.info("Marking request {} as acknowledged", requestId);

        RecordTransferRequest request = getRequestById(requestId);
        request.setStatus(RequestStatus.ACKNOWLEDGED);
        request.setAcknowledgedAt(LocalDateTime.now());
        request.addCommunicationNote("Previous school acknowledged receipt of request");

        return requestRepository.save(request);
    }

    /**
     * Mark request as in progress (school is preparing records)
     */
    @Transactional
    public RecordTransferRequest markInProgress(Long requestId) {
        RecordTransferRequest request = getRequestById(requestId);
        request.setStatus(RequestStatus.IN_PROGRESS);
        request.addCommunicationNote("Previous school is preparing records");

        return requestRepository.save(request);
    }

    /**
     * Record receipt of specific document type
     */
    @Transactional
    public RecordTransferRequest recordDocumentReceived(
            Long requestId,
            String documentType, // "transcript", "iep", "504", etc.
            String receivedVia,
            Long staffId) {

        log.info("Recording receipt of {} for request {}", documentType, requestId);

        RecordTransferRequest request = getRequestById(requestId);

        switch (documentType.toLowerCase()) {
            case "transcript":
                request.setTranscriptReceived(true);
                request.setTranscriptReceivedDate(LocalDate.now());
                break;
            case "iep":
                request.setIepReceived(true);
                request.setIepReceivedDate(LocalDate.now());
                break;
            case "504":
                request.setPlan504Received(true);
                request.setPlan504ReceivedDate(LocalDate.now());
                break;
            case "discipline":
                request.setDisciplineRecordsReceived(true);
                request.setDisciplineRecordsReceivedDate(LocalDate.now());
                break;
            case "attendance":
                request.setAttendanceRecordsReceived(true);
                request.setAttendanceRecordsReceivedDate(LocalDate.now());
                break;
            case "health":
                request.setHealthRecordsReceived(true);
                request.setHealthRecordsReceivedDate(LocalDate.now());
                break;
        }

        request.setReceivedVia(receivedVia);
        request.setStatus(RequestStatus.RECEIVED);
        request.addCommunicationNote(String.format("Received %s via %s", documentType, receivedVia));

        // Check if all requested records received
        if (request.areAllRecordsReceived()) {
            request.setStatus(RequestStatus.COMPLETE);
            request.setRecordsComplete(true);
            request.setCompletedAt(LocalDateTime.now());
            log.info("All records received - request {} marked COMPLETE", requestId);
        }

        return requestRepository.save(request);
    }

    /**
     * Mark request as complete (all records received and verified)
     */
    @Transactional
    public RecordTransferRequest markComplete(Long requestId, Long verifierStaffId, String notes) {
        log.info("Marking request {} as complete", requestId);

        RecordTransferRequest request = getRequestById(requestId);
        User verifier = userRepository.findById(verifierStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Verifier user not found"));

        if (!request.areAllRecordsReceived()) {
            throw new IllegalStateException("Cannot mark complete - not all requested records received");
        }

        request.setStatus(RequestStatus.COMPLETE);
        request.setRecordsComplete(true);
        request.setVerifiedBy(verifier);
        request.setVerifiedAt(LocalDateTime.now());
        request.setVerificationNotes(notes);
        request.setCompletedAt(LocalDateTime.now());

        return requestRepository.save(request);
    }

    /**
     * Mark request as failed (unable to obtain records)
     */
    @Transactional
    public RecordTransferRequest markFailed(Long requestId, String reason) {
        log.info("Marking request {} as failed: {}", requestId, reason);

        RecordTransferRequest request = getRequestById(requestId);
        request.setStatus(RequestStatus.FAILED);
        request.setIssuesEncountered(reason);
        request.addCommunicationNote("Request failed: " + reason);

        return requestRepository.save(request);
    }

    /**
     * Cancel request
     */
    @Transactional
    public RecordTransferRequest cancelRequest(Long requestId, String reason) {
        RecordTransferRequest request = getRequestById(requestId);
        request.setStatus(RequestStatus.CANCELLED);
        request.addCommunicationNote("Request cancelled: " + reason);

        return requestRepository.save(request);
    }

    // ========================================================================
    // FOLLOW-UP MANAGEMENT
    // ========================================================================

    /**
     * Record a follow-up communication attempt
     */
    @Transactional
    public RecordTransferRequest recordFollowUp(Long requestId, String notes) {
        log.info("Recording follow-up for request {}", requestId);

        RecordTransferRequest request = getRequestById(requestId);

        int currentCount = request.getFollowUpCount() != null ? request.getFollowUpCount() : 0;
        request.setFollowUpCount(currentCount + 1);
        request.setLastFollowUpDate(LocalDate.now());
        request.setNextFollowUpDate(LocalDate.now().plusDays(7)); // Next follow-up in 7 days

        String followUpNote = String.format("Follow-up #%d: %s", currentCount + 1, notes);
        request.addCommunicationNote(followUpNote);

        return requestRepository.save(request);
    }

    /**
     * Get requests needing follow-up
     */
    public List<RecordTransferRequest> getRequestsNeedingFollowUp() {
        return requestRepository.findRequestsNeedingFollowUp();
    }

    /**
     * Get overdue requests
     */
    public List<RecordTransferRequest> getOverdueRequests() {
        return requestRepository.findOverdueRequests();
    }

    // ========================================================================
    // QUERIES AND REPORTS
    // ========================================================================

    /**
     * Get requests by status
     */
    public List<RecordTransferRequest> getRequestsByStatus(RequestStatus status) {
        return requestRepository.findByStatus(status);
    }

    /**
     * Get requests awaiting response
     */
    public List<RecordTransferRequest> getRequestsAwaitingResponse() {
        return requestRepository.findRequestsAwaitingResponse();
    }

    /**
     * Get request summary
     */
    public RequestSummary getRequestSummary(Long requestId) {
        RecordTransferRequest request = getRequestById(requestId);

        return RequestSummary.builder()
                .requestId(requestId)
                .requestNumber(request.getRequestNumber())
                .status(request.getStatus())
                .schoolName(request.getSchoolName())
                .requestDate(request.getRequestDate())
                .dueDate(request.getDueDate())
                .isOverdue(request.isOverdue())
                .totalRecordsRequested(countRequestedRecords(request))
                .recordsReceived(countReceivedRecords(request))
                .pendingRecords(request.getPendingRecordsCount())
                .followUpCount(request.getFollowUpCount() != null ? request.getFollowUpCount() : 0)
                .build();
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    /**
     * Generate unique request number
     */
    private String generateRequestNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        long count = requestRepository.count() + 1;
        return String.format("RTR-%s-%06d", year, count);
    }

    /**
     * Count total records requested
     */
    private int countRequestedRecords(RecordTransferRequest request) {
        int count = 0;
        if (Boolean.TRUE.equals(request.getRequestTranscript())) count++;
        if (Boolean.TRUE.equals(request.getRequestIEP())) count++;
        if (Boolean.TRUE.equals(request.getRequest504Plan())) count++;
        if (Boolean.TRUE.equals(request.getRequestDisciplineRecords())) count++;
        if (Boolean.TRUE.equals(request.getRequestAttendanceRecords())) count++;
        if (Boolean.TRUE.equals(request.getRequestHealthRecords())) count++;
        return count;
    }

    /**
     * Count records received
     */
    private int countReceivedRecords(RecordTransferRequest request) {
        int count = 0;
        if (Boolean.TRUE.equals(request.getTranscriptReceived())) count++;
        if (Boolean.TRUE.equals(request.getIepReceived())) count++;
        if (Boolean.TRUE.equals(request.getPlan504Received())) count++;
        if (Boolean.TRUE.equals(request.getDisciplineRecordsReceived())) count++;
        if (Boolean.TRUE.equals(request.getAttendanceRecordsReceived())) count++;
        if (Boolean.TRUE.equals(request.getHealthRecordsReceived())) count++;
        return count;
    }

    // ========================================================================
    // DTO CLASSES
    // ========================================================================

    @Data
    @Builder
    public static class RequestSummary {
        private Long requestId;
        private String requestNumber;
        private RequestStatus status;
        private String schoolName;
        private LocalDate requestDate;
        private LocalDate dueDate;
        private boolean isOverdue;
        private int totalRecordsRequested;
        private int recordsReceived;
        private int pendingRecords;
        private int followUpCount;
    }
}
