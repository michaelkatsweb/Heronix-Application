package com.heronix.service;

import com.heronix.model.domain.PreRegistration;
import com.heronix.model.domain.PreRegistration.RegistrationStatus;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.User;
import com.heronix.repository.PreRegistrationRepository;
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
 * Service for Pre-Registration Management
 * Handles business logic for pre-registration for upcoming school year
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Service
@Transactional
public class PreRegistrationService {

    @Autowired
    private PreRegistrationRepository preRegistrationRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    // ========================================================================
    // CREATE OPERATIONS
    // ========================================================================

    /**
     * Create new pre-registration for a student
     */
    public PreRegistration createPreRegistration(Long studentId, String targetSchoolYear, Long createdByStaffId) {
        log.info("Creating pre-registration for student ID: {} for school year: {}", studentId, targetSchoolYear);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        User createdBy = userRepository.findById(createdByStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + createdByStaffId));

        // Check if pre-registration already exists
        if (preRegistrationRepository.existsByStudentAndTargetSchoolYear(student, targetSchoolYear)) {
            throw new IllegalStateException("Pre-registration already exists for this student and school year");
        }

        String registrationNumber = generateRegistrationNumber(targetSchoolYear);

        PreRegistration preRegistration = PreRegistration.builder()
                .registrationNumber(registrationNumber)
                .status(RegistrationStatus.DRAFT)
                .targetSchoolYear(targetSchoolYear)
                .registrationDate(LocalDate.now())
                .student(student)
                .currentGrade(student.getGradeLevel())
                .nextGrade(calculateNextGrade(student.getGradeLevel()))
                .isReturningStudent(true)
                .hasCompletedPriorYear(true)
                .isInGoodStanding(true)
                .studentEmail(student.getEmail())
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .build();

        PreRegistration saved = preRegistrationRepository.save(preRegistration);
        log.info("Created pre-registration: {}", saved.getRegistrationNumber());
        return saved;
    }

    /**
     * Generate unique registration number
     */
    private String generateRegistrationNumber(String schoolYear) {
        String year = schoolYear.split("-")[0];
        long count = preRegistrationRepository.countByTargetSchoolYear(schoolYear);
        return String.format("PRE-%s-%06d", year, count + 1);
    }

    /**
     * Calculate next grade level
     */
    private String calculateNextGrade(String currentGrade) {
        return switch (currentGrade) {
            case "Kindergarten" -> "1st Grade";
            case "1st Grade" -> "2nd Grade";
            case "2nd Grade" -> "3rd Grade";
            case "3rd Grade" -> "4th Grade";
            case "4th Grade" -> "5th Grade";
            case "5th Grade" -> "6th Grade";
            case "6th Grade" -> "7th Grade";
            case "7th Grade" -> "8th Grade";
            case "8th Grade" -> "9th Grade";
            case "9th Grade" -> "10th Grade";
            case "10th Grade" -> "11th Grade";
            case "11th Grade" -> "12th Grade";
            case "12th Grade" -> "Graduate";
            default -> currentGrade;
        };
    }

    // ========================================================================
    // READ OPERATIONS
    // ========================================================================

    /**
     * Get pre-registration by ID
     */
    @Transactional(readOnly = true)
    public PreRegistration getPreRegistrationById(Long id) {
        return preRegistrationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pre-registration not found: " + id));
    }

    /**
     * Get pre-registration by registration number
     */
    @Transactional(readOnly = true)
    public PreRegistration getByRegistrationNumber(String registrationNumber) {
        return preRegistrationRepository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new IllegalArgumentException("Pre-registration not found: " + registrationNumber));
    }

    /**
     * Get all pre-registrations for a student
     */
    @Transactional(readOnly = true)
    public List<PreRegistration> getByStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        return preRegistrationRepository.findByStudent(student);
    }

    /**
     * Get all pre-registrations by status
     */
    @Transactional(readOnly = true)
    public List<PreRegistration> getByStatus(RegistrationStatus status) {
        return preRegistrationRepository.findByStatus(status);
    }

    /**
     * Get all pre-registrations for target school year
     */
    @Transactional(readOnly = true)
    public List<PreRegistration> getByTargetSchoolYear(String targetSchoolYear) {
        return preRegistrationRepository.findByTargetSchoolYear(targetSchoolYear);
    }

    /**
     * Get pre-registrations awaiting review
     */
    @Transactional(readOnly = true)
    public List<PreRegistration> getAwaitingReview() {
        return preRegistrationRepository.findAwaitingReview();
    }

    /**
     * Search by student name
     */
    @Transactional(readOnly = true)
    public List<PreRegistration> searchByStudentName(String searchTerm) {
        return preRegistrationRepository.searchByStudentName(searchTerm);
    }

    // ========================================================================
    // UPDATE OPERATIONS
    // ========================================================================

    /**
     * Update pre-registration
     */
    public PreRegistration updatePreRegistration(PreRegistration preRegistration, Long updatedByStaffId) {
        log.info("Updating pre-registration: {}", preRegistration.getRegistrationNumber());

        User updatedBy = userRepository.findById(updatedByStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + updatedByStaffId));

        preRegistration.setUpdatedBy(updatedBy);
        preRegistration.setUpdatedAt(LocalDateTime.now());

        PreRegistration updated = preRegistrationRepository.save(preRegistration);
        log.info("Updated pre-registration: {}", updated.getRegistrationNumber());
        return updated;
    }

    /**
     * Submit pre-registration for review
     */
    public PreRegistration submitForReview(Long preRegistrationId, Long submittedByStaffId) {
        log.info("Submitting pre-registration ID: {} for review", preRegistrationId);

        PreRegistration preRegistration = getPreRegistrationById(preRegistrationId);

        if (!preRegistration.canBeSubmitted()) {
            throw new IllegalStateException("Pre-registration cannot be submitted. Missing required information or acknowledgments.");
        }

        preRegistration.setStatus(RegistrationStatus.SUBMITTED);
        preRegistration.setSubmittedAt(LocalDateTime.now());

        return updatePreRegistration(preRegistration, submittedByStaffId);
    }

    /**
     * Begin review of pre-registration
     */
    public PreRegistration beginReview(Long preRegistrationId, Long reviewedByStaffId) {
        log.info("Beginning review of pre-registration ID: {}", preRegistrationId);

        PreRegistration preRegistration = getPreRegistrationById(preRegistrationId);

        if (preRegistration.getStatus() != RegistrationStatus.SUBMITTED) {
            throw new IllegalStateException("Only submitted pre-registrations can be reviewed");
        }

        User reviewer = userRepository.findById(reviewedByStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + reviewedByStaffId));

        preRegistration.setStatus(RegistrationStatus.UNDER_REVIEW);
        preRegistration.setReviewedBy(reviewer);
        preRegistration.setReviewedAt(LocalDateTime.now());

        return updatePreRegistration(preRegistration, reviewedByStaffId);
    }

    /**
     * Approve pre-registration and reserve seat
     */
    public PreRegistration approve(Long preRegistrationId, Long approvedByStaffId, String approvalNotes) {
        log.info("Approving pre-registration ID: {}", preRegistrationId);

        PreRegistration preRegistration = getPreRegistrationById(preRegistrationId);

        if (!preRegistration.canBeApproved()) {
            throw new IllegalStateException("Pre-registration cannot be approved. Student must be in good standing.");
        }

        User approver = userRepository.findById(approvedByStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + approvedByStaffId));

        preRegistration.setStatus(RegistrationStatus.APPROVED);
        preRegistration.setApprovedBy(approver);
        preRegistration.setApprovedAt(LocalDateTime.now());
        preRegistration.setApprovalNotes(approvalNotes);
        preRegistration.setSeatReserved(true);

        return updatePreRegistration(preRegistration, approvedByStaffId);
    }

    /**
     * Confirm final enrollment
     */
    public PreRegistration confirmEnrollment(Long preRegistrationId, Long confirmedByStaffId) {
        log.info("Confirming enrollment for pre-registration ID: {}", preRegistrationId);

        PreRegistration preRegistration = getPreRegistrationById(preRegistrationId);

        if (preRegistration.getStatus() != RegistrationStatus.APPROVED) {
            throw new IllegalStateException("Only approved pre-registrations can be confirmed");
        }

        preRegistration.setStatus(RegistrationStatus.CONFIRMED);
        preRegistration.setConfirmedAt(LocalDateTime.now());

        return updatePreRegistration(preRegistration, confirmedByStaffId);
    }

    /**
     * Cancel pre-registration
     */
    public PreRegistration cancel(Long preRegistrationId, Long cancelledByStaffId, String reason) {
        log.info("Cancelling pre-registration ID: {}", preRegistrationId);

        PreRegistration preRegistration = getPreRegistrationById(preRegistrationId);

        preRegistration.setStatus(RegistrationStatus.CANCELLED);
        preRegistration.setAdditionalNotes(
                (preRegistration.getAdditionalNotes() != null ? preRegistration.getAdditionalNotes() + "\n\n" : "") +
                "CANCELLED: " + reason
        );

        if (Boolean.TRUE.equals(preRegistration.getSeatReserved())) {
            preRegistration.setSeatReserved(false);
        }

        return updatePreRegistration(preRegistration, cancelledByStaffId);
    }

    /**
     * Move to waitlist (capacity reached)
     */
    public PreRegistration moveToWaitlist(Long preRegistrationId, Long movedByStaffId) {
        log.info("Moving pre-registration ID: {} to waitlist", preRegistrationId);

        PreRegistration preRegistration = getPreRegistrationById(preRegistrationId);

        preRegistration.setStatus(RegistrationStatus.WAITLISTED);
        preRegistration.setSeatReserved(false);

        return updatePreRegistration(preRegistration, movedByStaffId);
    }

    // ========================================================================
    // STATISTICS & REPORTING
    // ========================================================================

    /**
     * Get enrollment statistics for school year
     */
    @Transactional(readOnly = true)
    public EnrollmentStatistics getStatistics(String targetSchoolYear) {
        long total = preRegistrationRepository.countByTargetSchoolYear(targetSchoolYear);
        long draft = preRegistrationRepository.countByStatusAndTargetSchoolYear(RegistrationStatus.DRAFT, targetSchoolYear);
        long submitted = preRegistrationRepository.countByStatusAndTargetSchoolYear(RegistrationStatus.SUBMITTED, targetSchoolYear);
        long underReview = preRegistrationRepository.countByStatusAndTargetSchoolYear(RegistrationStatus.UNDER_REVIEW, targetSchoolYear);
        long approved = preRegistrationRepository.countByStatusAndTargetSchoolYear(RegistrationStatus.APPROVED, targetSchoolYear);
        long confirmed = preRegistrationRepository.countByStatusAndTargetSchoolYear(RegistrationStatus.CONFIRMED, targetSchoolYear);
        long cancelled = preRegistrationRepository.countByStatusAndTargetSchoolYear(RegistrationStatus.CANCELLED, targetSchoolYear);
        long waitlisted = preRegistrationRepository.countByStatusAndTargetSchoolYear(RegistrationStatus.WAITLISTED, targetSchoolYear);

        return new EnrollmentStatistics(total, draft, submitted, underReview, approved, confirmed, cancelled, waitlisted);
    }

    /**
     * Get enrollment counts by grade level
     */
    @Transactional(readOnly = true)
    public List<Object[]> getEnrollmentCountsByGrade(String targetSchoolYear) {
        return preRegistrationRepository.getEnrollmentCountsByGrade(targetSchoolYear);
    }

    /**
     * Statistics record
     */
    public record EnrollmentStatistics(
            long total,
            long draft,
            long submitted,
            long underReview,
            long approved,
            long confirmed,
            long cancelled,
            long waitlisted
    ) {}

    // ========================================================================
    // DELETE OPERATIONS
    // ========================================================================

    /**
     * Delete pre-registration (only if in DRAFT status)
     */
    public void deletePreRegistration(Long preRegistrationId) {
        log.info("Deleting pre-registration ID: {}", preRegistrationId);

        PreRegistration preRegistration = getPreRegistrationById(preRegistrationId);

        if (preRegistration.getStatus() != RegistrationStatus.DRAFT) {
            throw new IllegalStateException("Only draft pre-registrations can be deleted");
        }

        preRegistrationRepository.delete(preRegistration);
        log.info("Deleted pre-registration: {}", preRegistration.getRegistrationNumber());
    }
}
