package com.heronix.service;

import com.heronix.model.domain.ReEnrollment;
import com.heronix.model.domain.ReEnrollment.ReEnrollmentStatus;
import com.heronix.model.domain.ReEnrollment.ApprovalDecision;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.User;
import com.heronix.repository.ReEnrollmentRepository;
import com.heronix.repository.StudentRepository;
import com.heronix.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for Re-Enrollment Management
 * Handles business logic for student re-enrollment processing
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Service
@Transactional
public class ReEnrollmentService {

    @Autowired
    private ReEnrollmentRepository reEnrollmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    // ========================================================================
    // CREATE OPERATIONS
    // ========================================================================

    /**
     * Create new re-enrollment application
     */
    public ReEnrollment createReEnrollment(Long studentId, String requestedGradeLevel,
                                          LocalDate intendedEnrollmentDate, Long createdByStaffId) {
        log.info("Creating re-enrollment for student ID: {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        User createdBy = userRepository.findById(createdByStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + createdByStaffId));

        // Check if student has pending re-enrollment
        if (reEnrollmentRepository.hasPendingReEnrollment(studentId)) {
            throw new IllegalStateException("Student already has a pending re-enrollment");
        }

        String reEnrollmentNumber = generateReEnrollmentNumber();

        ReEnrollment reEnrollment = ReEnrollment.builder()
                .reEnrollmentNumber(reEnrollmentNumber)
                .status(ReEnrollmentStatus.DRAFT)
                .applicationDate(LocalDate.now())
                .intendedEnrollmentDate(intendedEnrollmentDate)
                .student(student)
                .requestedGradeLevel(requestedGradeLevel)
                .previousRecordsReviewed(false)
                .transcriptCurrent(false)
                .immunizationsCurrent(false)
                .healthRecordsCurrent(false)
                .hasOutstandingFees(false)
                .feesPaid(false)
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .build();

        ReEnrollment saved = reEnrollmentRepository.save(reEnrollment);
        log.info("Created re-enrollment: {}", saved.getReEnrollmentNumber());
        return saved;
    }

    /**
     * Generate unique re-enrollment number
     */
    private String generateReEnrollmentNumber() {
        int year = LocalDate.now().getYear();
        long count = reEnrollmentRepository.count();
        return String.format("RNE-%d-%06d", year, count + 1);
    }

    // ========================================================================
    // READ OPERATIONS
    // ========================================================================

    /**
     * Get re-enrollment by ID
     */
    @Transactional(readOnly = true)
    public ReEnrollment getReEnrollmentById(Long id) {
        return reEnrollmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Re-enrollment not found: " + id));
    }

    /**
     * Get by re-enrollment number
     */
    @Transactional(readOnly = true)
    public ReEnrollment getByReEnrollmentNumber(String reEnrollmentNumber) {
        return reEnrollmentRepository.findByReEnrollmentNumber(reEnrollmentNumber)
                .orElseThrow(() -> new IllegalArgumentException("Re-enrollment not found: " + reEnrollmentNumber));
    }

    /**
     * Get all re-enrollments by status
     */
    @Transactional(readOnly = true)
    public List<ReEnrollment> getByStatus(ReEnrollmentStatus status) {
        return reEnrollmentRepository.findByStatus(status);
    }

    /**
     * Get pending review
     */
    @Transactional(readOnly = true)
    public List<ReEnrollment> getPendingReview() {
        return reEnrollmentRepository.findPendingReview();
    }

    /**
     * Get by counselor
     */
    @Transactional(readOnly = true)
    public List<ReEnrollment> getByCounselor(Long counselorId) {
        return reEnrollmentRepository.findByAssignedCounselor(counselorId);
    }

    /**
     * Get unassigned re-enrollments
     */
    @Transactional(readOnly = true)
    public List<ReEnrollment> getUnassignedReEnrollments() {
        return reEnrollmentRepository.findUnassignedReEnrollments();
    }

    /**
     * Search by student name
     */
    @Transactional(readOnly = true)
    public List<ReEnrollment> searchByStudentName(String searchTerm) {
        return reEnrollmentRepository.searchByStudentName(searchTerm);
    }

    /**
     * Get all re-enrollments for a student
     */
    @Transactional(readOnly = true)
    public List<ReEnrollment> getByStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        return reEnrollmentRepository.findAllByStudent(student);
    }

    // ========================================================================
    // UPDATE OPERATIONS
    // ========================================================================

    /**
     * Update re-enrollment
     */
    public ReEnrollment updateReEnrollment(ReEnrollment reEnrollment, Long updatedByStaffId) {
        log.info("Updating re-enrollment: {}", reEnrollment.getReEnrollmentNumber());

        User updatedBy = userRepository.findById(updatedByStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + updatedByStaffId));

        reEnrollment.setUpdatedBy(updatedBy);
        reEnrollment.setUpdatedAt(LocalDateTime.now());

        // Calculate time away
        reEnrollment.calculateTimeAway();

        ReEnrollment updated = reEnrollmentRepository.save(reEnrollment);
        log.info("Updated re-enrollment: {}", updated.getReEnrollmentNumber());
        return updated;
    }

    /**
     * Assign counselor
     */
    public ReEnrollment assignCounselor(Long reEnrollmentId, Long counselorId, Long updatedByStaffId) {
        log.info("Assigning counselor ID: {} to re-enrollment ID: {}", counselorId, reEnrollmentId);

        ReEnrollment reEnrollment = getReEnrollmentById(reEnrollmentId);
        User counselor = userRepository.findById(counselorId)
                .orElseThrow(() -> new IllegalArgumentException("Counselor not found: " + counselorId));

        reEnrollment.setAssignedCounselor(counselor);
        return updateReEnrollment(reEnrollment, updatedByStaffId);
    }

    /**
     * Review previous records
     */
    public ReEnrollment reviewRecords(Long reEnrollmentId, boolean transcriptCurrent,
                                     boolean immunizationsCurrent, boolean healthRecordsCurrent,
                                     Long reviewedByStaffId) {
        log.info("Reviewing records for re-enrollment ID: {}", reEnrollmentId);

        ReEnrollment reEnrollment = getReEnrollmentById(reEnrollmentId);
        User reviewer = userRepository.findById(reviewedByStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Reviewer not found: " + reviewedByStaffId));

        reEnrollment.setPreviousRecordsReviewed(true);
        reEnrollment.setRecordsReviewDate(LocalDate.now());
        reEnrollment.setReviewedBy(reviewer);
        reEnrollment.setTranscriptCurrent(transcriptCurrent);
        reEnrollment.setImmunizationsCurrent(immunizationsCurrent);
        reEnrollment.setHealthRecordsCurrent(healthRecordsCurrent);

        // Auto-update status
        if (reEnrollment.getStatus() == ReEnrollmentStatus.DRAFT) {
            reEnrollment.setStatus(ReEnrollmentStatus.PENDING_REVIEW);
        }

        return updateReEnrollment(reEnrollment, reviewedByStaffId);
    }

    /**
     * Set outstanding fees
     */
    public ReEnrollment setOutstandingFees(Long reEnrollmentId, Double amount, Long updatedByStaffId) {
        log.info("Setting outstanding fees for re-enrollment ID: {}", reEnrollmentId);

        ReEnrollment reEnrollment = getReEnrollmentById(reEnrollmentId);
        reEnrollment.setHasOutstandingFees(amount > 0);
        reEnrollment.setOutstandingFeesAmount(amount);
        reEnrollment.setFeesPaid(false);

        return updateReEnrollment(reEnrollment, updatedByStaffId);
    }

    /**
     * Mark fees as paid
     */
    public ReEnrollment markFeesPaid(Long reEnrollmentId, Long updatedByStaffId) {
        log.info("Marking fees paid for re-enrollment ID: {}", reEnrollmentId);

        ReEnrollment reEnrollment = getReEnrollmentById(reEnrollmentId);
        reEnrollment.setFeesPaid(true);

        return updateReEnrollment(reEnrollment, updatedByStaffId);
    }

    /**
     * Submit for approval
     */
    public ReEnrollment submitForApproval(Long reEnrollmentId, Long updatedByStaffId) {
        log.info("Submitting re-enrollment ID: {} for approval", reEnrollmentId);

        ReEnrollment reEnrollment = getReEnrollmentById(reEnrollmentId);

        if (!Boolean.TRUE.equals(reEnrollment.getPreviousRecordsReviewed())) {
            throw new IllegalStateException("Records must be reviewed before submitting for approval");
        }

        if (Boolean.TRUE.equals(reEnrollment.getHasOutstandingFees()) &&
            !Boolean.TRUE.equals(reEnrollment.getFeesPaid())) {
            throw new IllegalStateException("Outstanding fees must be paid before submitting for approval");
        }

        reEnrollment.setStatus(ReEnrollmentStatus.PENDING_APPROVAL);
        return updateReEnrollment(reEnrollment, updatedByStaffId);
    }

    /**
     * Counselor decision
     */
    public ReEnrollment counselorDecision(Long reEnrollmentId, ApprovalDecision decision,
                                         String recommendation, Long counselorId) {
        log.info("Counselor decision for re-enrollment ID: {} - {}", reEnrollmentId, decision);

        ReEnrollment reEnrollment = getReEnrollmentById(reEnrollmentId);

        if (reEnrollment.getAssignedCounselor() == null ||
            !reEnrollment.getAssignedCounselor().getId().equals(counselorId)) {
            throw new IllegalStateException("Only assigned counselor can make this decision");
        }

        reEnrollment.setCounselorDecision(decision);
        reEnrollment.setCounselorRecommendation(recommendation);
        reEnrollment.setCounselorReviewDate(LocalDate.now());

        return updateReEnrollment(reEnrollment, counselorId);
    }

    /**
     * Principal decision
     */
    public ReEnrollment principalDecision(Long reEnrollmentId, ApprovalDecision decision,
                                         String notes, Long principalId) {
        log.info("Principal decision for re-enrollment ID: {} - {}", reEnrollmentId, decision);

        ReEnrollment reEnrollment = getReEnrollmentById(reEnrollmentId);

        if (reEnrollment.getCounselorDecision() == null) {
            throw new IllegalStateException("Counselor decision required before principal review");
        }

        User principal = userRepository.findById(principalId)
                .orElseThrow(() -> new IllegalArgumentException("Principal not found: " + principalId));

        reEnrollment.setPrincipalReviewer(principal);
        reEnrollment.setPrincipalDecision(decision);
        reEnrollment.setPrincipalNotes(notes);
        reEnrollment.setPrincipalReviewDate(LocalDate.now());

        // Update status based on decision
        if (decision == ApprovalDecision.APPROVED || decision == ApprovalDecision.CONDITIONAL) {
            reEnrollment.setStatus(ReEnrollmentStatus.APPROVED);
            reEnrollment.setApprovalDate(LocalDate.now());
        } else if (decision == ApprovalDecision.DENIED) {
            reEnrollment.setStatus(ReEnrollmentStatus.REJECTED);
        }

        return updateReEnrollment(reEnrollment, principalId);
    }

    /**
     * Apply conditional approval
     */
    public ReEnrollment applyConditionalApproval(Long reEnrollmentId, String conditions,
                                                boolean behavioralContract, boolean academicPlan,
                                                boolean probation, Integer probationDays,
                                                Long updatedByStaffId) {
        log.info("Applying conditional approval to re-enrollment ID: {}", reEnrollmentId);

        ReEnrollment reEnrollment = getReEnrollmentById(reEnrollmentId);
        reEnrollment.setConditionalApproval(true);
        reEnrollment.setApprovalConditions(conditions);
        reEnrollment.setBehavioralContract(behavioralContract);
        reEnrollment.setAcademicPlan(academicPlan);
        reEnrollment.setProbationaryPeriod(probation);
        reEnrollment.setProbationaryPeriodDays(probationDays);

        return updateReEnrollment(reEnrollment, updatedByStaffId);
    }

    /**
     * Complete enrollment
     */
    public ReEnrollment completeEnrollment(Long reEnrollmentId, String assignedGrade,
                                          String homeroom, Long completedByStaffId) {
        log.info("Completing enrollment for re-enrollment ID: {}", reEnrollmentId);

        ReEnrollment reEnrollment = getReEnrollmentById(reEnrollmentId);

        if (!reEnrollment.isApprovedForEnrollment()) {
            throw new IllegalStateException("Re-enrollment must be approved before completion");
        }

        User processedBy = userRepository.findById(completedByStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + completedByStaffId));

        reEnrollment.setStatus(ReEnrollmentStatus.ENROLLED);
        reEnrollment.setEnrollmentDate(LocalDate.now());
        reEnrollment.setAssignedGradeLevel(assignedGrade);
        reEnrollment.setAssignedHomeroom(homeroom);
        reEnrollment.setProcessedBy(processedBy);
        reEnrollment.setProcessingDate(LocalDate.now());

        return updateReEnrollment(reEnrollment, completedByStaffId);
    }

    /**
     * Reject application
     */
    public ReEnrollment rejectApplication(Long reEnrollmentId, String reason, Long rejectedByStaffId) {
        log.info("Rejecting re-enrollment ID: {}", reEnrollmentId);

        ReEnrollment reEnrollment = getReEnrollmentById(reEnrollmentId);
        reEnrollment.setStatus(ReEnrollmentStatus.REJECTED);
        reEnrollment.setRejectionReason(reason);

        return updateReEnrollment(reEnrollment, rejectedByStaffId);
    }

    /**
     * Cancel application
     */
    public ReEnrollment cancelApplication(Long reEnrollmentId, String reason, Long cancelledByStaffId) {
        log.info("Cancelling re-enrollment ID: {}", reEnrollmentId);

        ReEnrollment reEnrollment = getReEnrollmentById(reEnrollmentId);
        reEnrollment.setStatus(ReEnrollmentStatus.CANCELLED);
        reEnrollment.setAdministrativeNotes(
                (reEnrollment.getAdministrativeNotes() != null ? reEnrollment.getAdministrativeNotes() + "\n\n" : "") +
                "CANCELLED: " + reason
        );

        return updateReEnrollment(reEnrollment, cancelledByStaffId);
    }

    // ========================================================================
    // STATISTICS & REPORTING
    // ========================================================================

    /**
     * Get re-enrollment statistics
     */
    @Transactional(readOnly = true)
    public ReEnrollmentStatistics getStatistics() {
        long total = reEnrollmentRepository.count();
        long draft = reEnrollmentRepository.countByStatus(ReEnrollmentStatus.DRAFT);
        long pendingReview = reEnrollmentRepository.countByStatus(ReEnrollmentStatus.PENDING_REVIEW);
        long pendingApproval = reEnrollmentRepository.countByStatus(ReEnrollmentStatus.PENDING_APPROVAL);
        long approved = reEnrollmentRepository.countByStatus(ReEnrollmentStatus.APPROVED);
        long enrolled = reEnrollmentRepository.countByStatus(ReEnrollmentStatus.ENROLLED);
        long rejected = reEnrollmentRepository.countByStatus(ReEnrollmentStatus.REJECTED);
        long cancelled = reEnrollmentRepository.countByStatus(ReEnrollmentStatus.CANCELLED);
        long conditional = reEnrollmentRepository.countByConditionalApprovalTrue();
        Double avgMonthsAway = reEnrollmentRepository.getAverageMonthsAway();

        return new ReEnrollmentStatistics(
                total, draft, pendingReview, pendingApproval, approved,
                enrolled, rejected, cancelled, conditional,
                avgMonthsAway != null ? avgMonthsAway : 0.0
        );
    }

    /**
     * Get re-enrollment counts by reason
     */
    @Transactional(readOnly = true)
    public List<Object[]> getCountByReason() {
        return reEnrollmentRepository.getCountByReason();
    }

    /**
     * Get re-enrollments needing attention
     */
    @Transactional(readOnly = true)
    public List<ReEnrollment> getReEnrollmentsNeedingAttention() {
        LocalDate cutoffDate = LocalDate.now().minusDays(7); // 1 week old
        return reEnrollmentRepository.findNeedingAttention(cutoffDate);
    }

    /**
     * Record fee payment
     */
    public ReEnrollment recordFeePayment(Long reEnrollmentId, Long updatedByStaffId) {
        log.info("Recording fee payment for re-enrollment ID: {}", reEnrollmentId);

        ReEnrollment reEnrollment = getReEnrollmentById(reEnrollmentId);
        reEnrollment.setFeesPaid(true);

        return updateReEnrollment(reEnrollment, updatedByStaffId);
    }

    /**
     * Complete re-enrollment
     */
    public ReEnrollment completeReEnrollment(Long reEnrollmentId, Long enrolledStudentId, Long completedByStaffId) {
        log.info("Completing re-enrollment ID: {}", reEnrollmentId);

        ReEnrollment reEnrollment = getReEnrollmentById(reEnrollmentId);

        Student student = studentRepository.findById(enrolledStudentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + enrolledStudentId));

        reEnrollment.setStatus(ReEnrollmentStatus.ENROLLED);

        return updateReEnrollment(reEnrollment, completedByStaffId);
    }

    /**
     * Cancel re-enrollment
     */
    public ReEnrollment cancelReEnrollment(Long reEnrollmentId, String reason, Long cancelledByStaffId) {
        log.info("Cancelling re-enrollment ID: {}", reEnrollmentId);

        ReEnrollment reEnrollment = getReEnrollmentById(reEnrollmentId);

        if (reEnrollment.getStatus() == ReEnrollmentStatus.ENROLLED) {
            throw new IllegalStateException("Cannot cancel re-enrollment that has been enrolled");
        }

        reEnrollment.setStatus(ReEnrollmentStatus.CANCELLED);

        return updateReEnrollment(reEnrollment, cancelledByStaffId);
    }

    /**
     * Statistics record
     */
    public record ReEnrollmentStatistics(
            long total,
            long draft,
            long pendingReview,
            long pendingApproval,
            long approved,
            long enrolled,
            long rejected,
            long cancelled,
            long conditionalApprovals,
            double averageMonthsAway
    ) {}

    // ========================================================================
    // DELETE OPERATIONS
    // ========================================================================

    /**
     * Delete re-enrollment (only if in DRAFT status)
     */
    public void deleteReEnrollment(Long reEnrollmentId) {
        log.info("Deleting re-enrollment ID: {}", reEnrollmentId);

        ReEnrollment reEnrollment = getReEnrollmentById(reEnrollmentId);

        if (reEnrollment.getStatus() != ReEnrollmentStatus.DRAFT) {
            throw new IllegalStateException("Only draft re-enrollment applications can be deleted");
        }

        reEnrollmentRepository.delete(reEnrollment);
        log.info("Deleted re-enrollment: {}", reEnrollment.getReEnrollmentNumber());
    }
}
