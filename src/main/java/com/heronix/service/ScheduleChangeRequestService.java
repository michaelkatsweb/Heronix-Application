package com.heronix.service;

import com.heronix.dto.ScheduleChangeRequestDTO;
import com.heronix.model.domain.*;
import com.heronix.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing schedule change requests
 * Handles CRUD operations and approval workflow for schedule change requests
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleChangeRequestService {

    private final ScheduleChangeRequestRepository scheduleChangeRequestRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final TeacherRepository teacherRepository;
    private final AcademicYearRepository academicYearRepository;
    private final GradingPeriodRepository gradingPeriodRepository;

    // Default number of days before a request is considered overdue
    private static final int DEFAULT_OVERDUE_DAYS = 5;

    // ========================================================================
    // CREATE & UPDATE OPERATIONS
    // ========================================================================

    /**
     * Submit a schedule change request
     *
     * @param studentId Student ID
     * @param dto Request data
     * @return Created request DTO
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public ScheduleChangeRequestDTO submitChangeRequest(Long studentId, ScheduleChangeRequestDTO dto) {
        log.info("Student {} submitting schedule change request: {}", studentId, dto.getRequestType());

        // Find student
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        // Validate request
        validateChangeRequest(dto);

        // Check for duplicate pending requests
        checkForDuplicateRequests(studentId, dto);

        // Create request entity
        ScheduleChangeRequest request = ScheduleChangeRequest.builder()
                .student(student)
                .requestType(dto.getRequestType())
                .reason(dto.getReason())
                .studentNotes(dto.getStudentNotes())
                .parentContact(dto.getParentContact())
                .status(ScheduleChangeRequest.RequestStatus.PENDING)
                .priorityLevel(dto.getPriorityLevel() != null ? dto.getPriorityLevel() : 0)
                .build();

        // Set current course/section if provided
        if (dto.getCurrentCourseId() != null) {
            Course currentCourse = courseRepository.findById(dto.getCurrentCourseId())
                    .orElseThrow(() -> new IllegalArgumentException("Current course not found: " + dto.getCurrentCourseId()));
            request.setCurrentCourse(currentCourse);
        }

        if (dto.getCurrentSectionId() != null) {
            CourseSection currentSection = courseSectionRepository.findById(dto.getCurrentSectionId())
                    .orElseThrow(() -> new IllegalArgumentException("Current section not found: " + dto.getCurrentSectionId()));
            request.setCurrentSection(currentSection);
        }

        // Set requested course/section if provided
        if (dto.getRequestedCourseId() != null) {
            Course requestedCourse = courseRepository.findById(dto.getRequestedCourseId())
                    .orElseThrow(() -> new IllegalArgumentException("Requested course not found: " + dto.getRequestedCourseId()));
            request.setRequestedCourse(requestedCourse);
        }

        if (dto.getRequestedSectionId() != null) {
            CourseSection requestedSection = courseSectionRepository.findById(dto.getRequestedSectionId())
                    .orElseThrow(() -> new IllegalArgumentException("Requested section not found: " + dto.getRequestedSectionId()));
            request.setRequestedSection(requestedSection);
        }

        // Set academic year and grading period if provided
        if (dto.getAcademicYearId() != null) {
            AcademicYear academicYear = academicYearRepository.findById(dto.getAcademicYearId())
                    .orElseThrow(() -> new IllegalArgumentException("Academic year not found: " + dto.getAcademicYearId()));
            request.setAcademicYear(academicYear);
        }

        if (dto.getGradingPeriodId() != null) {
            GradingPeriod gradingPeriod = gradingPeriodRepository.findById(dto.getGradingPeriodId())
                    .orElseThrow(() -> new IllegalArgumentException("Grading period not found: " + dto.getGradingPeriodId()));
            request.setGradingPeriod(gradingPeriod);
        }

        // Save
        ScheduleChangeRequest saved = scheduleChangeRequestRepository.save(request);
        log.info("Created schedule change request with ID: {}", saved.getId());

        return toDTO(saved);
    }

    /**
     * Approve a schedule change request
     *
     * @param requestId Request ID
     * @param reviewerId Reviewer (teacher/counselor/admin) ID
     * @param notes Review notes
     * @return Updated request DTO
     */
    @Transactional
    public ScheduleChangeRequestDTO approveRequest(Long requestId, Long reviewerId, String notes) {
        log.info("Approving schedule change request ID: {} by reviewer: {}", requestId, reviewerId);

        ScheduleChangeRequest request = scheduleChangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule change request not found: " + requestId));

        if (!request.isPending()) {
            throw new IllegalStateException("Request is not pending: " + request.getStatus());
        }

        Teacher reviewer = teacherRepository.findById(reviewerId)
                .orElseThrow(() -> new IllegalArgumentException("Reviewer not found: " + reviewerId));

        request.approve(reviewer, notes);

        ScheduleChangeRequest updated = scheduleChangeRequestRepository.save(request);
        log.info("Approved schedule change request ID: {}", updated.getId());

        return toDTO(updated);
    }

    /**
     * Deny a schedule change request
     *
     * @param requestId Request ID
     * @param reviewerId Reviewer ID
     * @param reason Denial reason
     * @return Updated request DTO
     */
    @Transactional
    public ScheduleChangeRequestDTO denyRequest(Long requestId, Long reviewerId, String reason) {
        log.info("Denying schedule change request ID: {} by reviewer: {}", requestId, reviewerId);

        ScheduleChangeRequest request = scheduleChangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule change request not found: " + requestId));

        if (!request.isPending()) {
            throw new IllegalStateException("Request is not pending: " + request.getStatus());
        }

        Teacher reviewer = teacherRepository.findById(reviewerId)
                .orElseThrow(() -> new IllegalArgumentException("Reviewer not found: " + reviewerId));

        request.deny(reviewer, reason);

        ScheduleChangeRequest updated = scheduleChangeRequestRepository.save(request);
        log.info("Denied schedule change request ID: {}", updated.getId());

        return toDTO(updated);
    }

    /**
     * Mark a request as completed (schedule has been changed)
     *
     * @param requestId Request ID
     * @return Updated request DTO
     */
    @Transactional
    public ScheduleChangeRequestDTO completeRequest(Long requestId) {
        log.info("Completing schedule change request ID: {}", requestId);

        ScheduleChangeRequest request = scheduleChangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule change request not found: " + requestId));

        if (!request.isApproved()) {
            throw new IllegalStateException("Request is not approved: " + request.getStatus());
        }

        request.complete();

        ScheduleChangeRequest updated = scheduleChangeRequestRepository.save(request);
        log.info("Completed schedule change request ID: {}", updated.getId());

        return toDTO(updated);
    }

    /**
     * Cancel a schedule change request
     *
     * @param requestId Request ID
     * @param studentId Student ID (for authorization check)
     * @return Updated request DTO
     */
    @Transactional
    public ScheduleChangeRequestDTO cancelRequest(Long requestId, Long studentId) {
        log.info("Cancelling schedule change request ID: {} by student: {}", requestId, studentId);

        ScheduleChangeRequest request = scheduleChangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule change request not found: " + requestId));

        // Verify the student owns this request
        if (!request.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("Student does not own this request");
        }

        if (!request.isPending()) {
            throw new IllegalStateException("Only pending requests can be cancelled: " + request.getStatus());
        }

        request.cancel();

        ScheduleChangeRequest updated = scheduleChangeRequestRepository.save(request);
        log.info("Cancelled schedule change request ID: {}", updated.getId());

        return toDTO(updated);
    }

    // ========================================================================
    // QUERY OPERATIONS
    // ========================================================================

    /**
     * Get schedule change request by ID
     *
     * @param requestId Request ID
     * @return Request DTO
     */
    @Transactional(readOnly = true)
    public ScheduleChangeRequestDTO getRequestById(Long requestId) {
        ScheduleChangeRequest request = scheduleChangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule change request not found: " + requestId));

        return toDTO(request);
    }

    /**
     * Get all schedule change requests for a student
     *
     * @param studentId Student ID
     * @return List of request DTOs
     */
    @Transactional(readOnly = true)
    public List<ScheduleChangeRequestDTO> getRequestsForStudent(Long studentId) {
        return scheduleChangeRequestRepository.findByStudentIdOrderByRequestDateDesc(studentId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get pending requests for a student
     *
     * @param studentId Student ID
     * @return List of pending request DTOs
     */
    @Transactional(readOnly = true)
    public List<ScheduleChangeRequestDTO> getPendingRequestsForStudent(Long studentId) {
        return scheduleChangeRequestRepository.findPendingRequestsByStudent(studentId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all pending requests
     *
     * @return List of pending request DTOs
     */
    @Transactional(readOnly = true)
    public List<ScheduleChangeRequestDTO> getAllPendingRequests() {
        return scheduleChangeRequestRepository.findPendingRequests()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get pending requests for a counselor's assigned students
     *
     * @param counselorId Counselor ID
     * @return List of pending request DTOs
     */
    @Transactional(readOnly = true)
    public List<ScheduleChangeRequestDTO> getPendingRequestsForCounselor(Long counselorId) {
        return scheduleChangeRequestRepository.findPendingRequestsForCounselor(counselorId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get overdue pending requests
     *
     * @return List of overdue request DTOs
     */
    @Transactional(readOnly = true)
    public List<ScheduleChangeRequestDTO> getOverdueRequests() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(DEFAULT_OVERDUE_DAYS);
        return scheduleChangeRequestRepository.findOverdueRequests(cutoffDate)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ========================================================================
    // VALIDATION METHODS
    // ========================================================================

    /**
     * Validate schedule change request data
     *
     * @param dto Request data
     * @throws IllegalArgumentException if validation fails
     */
    private void validateChangeRequest(ScheduleChangeRequestDTO dto) {
        if (dto.getRequestType() == null) {
            throw new IllegalArgumentException("Request type is required");
        }

        if (dto.getReason() == null || dto.getReason().trim().isEmpty()) {
            throw new IllegalArgumentException("Reason is required");
        }

        // Validate based on request type
        switch (dto.getRequestType()) {
            case ADD:
                if (dto.getRequestedCourseId() == null) {
                    throw new IllegalArgumentException("Requested course is required for ADD request");
                }
                break;

            case DROP:
                if (dto.getCurrentCourseId() == null) {
                    throw new IllegalArgumentException("Current course is required for DROP request");
                }
                break;

            case SWAP:
                if (dto.getCurrentCourseId() == null) {
                    throw new IllegalArgumentException("Current course is required for SWAP request");
                }
                if (dto.getRequestedCourseId() == null) {
                    throw new IllegalArgumentException("Requested course is required for SWAP request");
                }
                break;
        }
    }

    /**
     * Check for duplicate pending requests
     *
     * @param studentId Student ID
     * @param dto Request data
     * @throws IllegalArgumentException if duplicate exists
     */
    private void checkForDuplicateRequests(Long studentId, ScheduleChangeRequestDTO dto) {
        List<ScheduleChangeRequest> duplicates = scheduleChangeRequestRepository.findDuplicateRequests(
                studentId,
                dto.getRequestType(),
                dto.getCurrentCourseId(),
                dto.getRequestedCourseId());

        if (!duplicates.isEmpty()) {
            throw new IllegalArgumentException("A similar pending request already exists for this student");
        }
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Convert entity to DTO
     */
    private ScheduleChangeRequestDTO toDTO(ScheduleChangeRequest request) {
        ScheduleChangeRequestDTO dto = ScheduleChangeRequestDTO.builder()
                .id(request.getId())
                .studentId(request.getStudent().getId())
                .studentName(request.getStudent().getFullName())
                .studentNumber(request.getStudent().getStudentId())
                .requestType(request.getRequestType())
                .reason(request.getReason())
                .studentNotes(request.getStudentNotes())
                .parentContact(request.getParentContact())
                .parentContacted(request.getParentContacted())
                .requestDate(request.getRequestDate())
                .status(request.getStatus())
                .priorityLevel(request.getPriorityLevel())
                .reviewedDate(request.getReviewedDate())
                .reviewNotes(request.getReviewNotes())
                .denialReason(request.getDenialReason())
                .completionDate(request.getCompletionDate())
                .requestSummary(request.getRequestSummary())
                .daysSinceRequest(request.getDaysSinceRequest())
                .isOverdue(request.isOverdue(DEFAULT_OVERDUE_DAYS))
                .canAutoApprove(request.canAutoApprove())
                .build();

        if (request.getCurrentCourse() != null) {
            dto.setCurrentCourseId(request.getCurrentCourse().getId());
            dto.setCurrentCourseName(request.getCurrentCourse().getCourseName());
            dto.setCurrentCourseCode(request.getCurrentCourse().getCourseCode());
        }

        if (request.getCurrentSection() != null) {
            dto.setCurrentSectionId(request.getCurrentSection().getId());
            dto.setCurrentSectionName(request.getCurrentSection().getSectionNumber());
        }

        if (request.getRequestedCourse() != null) {
            dto.setRequestedCourseId(request.getRequestedCourse().getId());
            dto.setRequestedCourseName(request.getRequestedCourse().getCourseName());
            dto.setRequestedCourseCode(request.getRequestedCourse().getCourseCode());
        }

        if (request.getRequestedSection() != null) {
            dto.setRequestedSectionId(request.getRequestedSection().getId());
            dto.setRequestedSectionName(request.getRequestedSection().getSectionNumber());
        }

        if (request.getReviewedBy() != null) {
            dto.setReviewedById(request.getReviewedBy().getId());
            dto.setReviewedByName(request.getReviewedBy().getName());
        }

        if (request.getAcademicYear() != null) {
            dto.setAcademicYearId(request.getAcademicYear().getId());
            dto.setAcademicYearName(request.getAcademicYear().getYearName());
        }

        if (request.getGradingPeriod() != null) {
            dto.setGradingPeriodId(request.getGradingPeriod().getId());
            dto.setGradingPeriodName(request.getGradingPeriod().getName());
        }

        return dto;
    }
}
