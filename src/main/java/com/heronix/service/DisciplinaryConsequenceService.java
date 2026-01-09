package com.heronix.service;

import com.heronix.model.domain.*;
import com.heronix.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing disciplinary consequences
 * Handles assignment, tracking, and completion of consequences
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Service
@Transactional
public class DisciplinaryConsequenceService {

    @Autowired
    private DisciplinaryConsequenceRepository consequenceRepository;

    @Autowired
    private DisciplinaryReferralRepository referralRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private BehaviorIncidentRepository incidentRepository;

    // ========================================================================
    // CONSEQUENCE CREATION
    // ========================================================================

    /**
     * Assign consequence to student
     */
    public DisciplinaryConsequence assignConsequence(
            Long studentId,
            Long assignedById,
            DisciplinaryConsequence.ConsequenceType type,
            LocalDate startDate,
            LocalDate endDate,
            Integer duration,
            String description) {

        log.info("Assigning {} consequence to student {}", type, studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        Teacher assignedBy = teacherRepository.findById(assignedById)
                .orElseThrow(() -> new IllegalArgumentException("Administrator not found: " + assignedById));

        DisciplinaryConsequence consequence = DisciplinaryConsequence.builder()
                .student(student)
                .assignedBy(assignedBy)
                .consequenceType(type)
                .startDate(startDate)
                .endDate(endDate)
                .duration(duration)
                .status(DisciplinaryConsequence.ConsequenceStatus.ASSIGNED)
                .description(description)
                .parentNotified(false)
                .appealFiled(false)
                .verifiedCompleted(false)
                .build();

        DisciplinaryConsequence saved = consequenceRepository.save(consequence);
        log.info("Assigned consequence {} to student {}", saved.getId(), studentId);

        return saved;
    }

    /**
     * Assign consequence from referral
     */
    public DisciplinaryConsequence assignConsequenceFromReferral(
            Long referralId,
            DisciplinaryConsequence.ConsequenceType type,
            LocalDate startDate,
            LocalDate endDate,
            Integer duration,
            String description) {

        log.info("Assigning consequence from referral {}", referralId);

        DisciplinaryReferral referral = referralRepository.findById(referralId)
                .orElseThrow(() -> new IllegalArgumentException("Referral not found: " + referralId));

        DisciplinaryConsequence consequence = DisciplinaryConsequence.builder()
                .referral(referral)
                .behaviorIncident(referral.getBehaviorIncident())
                .student(referral.getStudent())
                .assignedBy(referral.getAssignedAdministrator())
                .consequenceType(type)
                .startDate(startDate)
                .endDate(endDate)
                .duration(duration)
                .status(DisciplinaryConsequence.ConsequenceStatus.ASSIGNED)
                .description(description)
                .parentNotified(referral.getParentContacted())
                .parentNotificationDate(referral.getParentContactDate())
                .appealFiled(false)
                .verifiedCompleted(false)
                .campus(referral.getCampus())
                .build();

        DisciplinaryConsequence saved = consequenceRepository.save(consequence);
        log.info("Assigned consequence {} from referral {}", saved.getId(), referralId);

        return saved;
    }

    // ========================================================================
    // CONSEQUENCE TRACKING
    // ========================================================================

    /**
     * Start consequence (move to IN_PROGRESS)
     */
    public DisciplinaryConsequence startConsequence(Long consequenceId) {
        log.info("Starting consequence {}", consequenceId);

        DisciplinaryConsequence consequence = getConsequenceById(consequenceId);
        consequence.setStatus(DisciplinaryConsequence.ConsequenceStatus.IN_PROGRESS);

        return consequenceRepository.save(consequence);
    }

    /**
     * Update community service hours
     */
    public DisciplinaryConsequence updateCommunityServiceHours(
            Long consequenceId,
            int hoursCompleted,
            String serviceLocation) {

        log.info("Updating community service hours for consequence {}", consequenceId);

        DisciplinaryConsequence consequence = getConsequenceById(consequenceId);

        if (consequence.getConsequenceType() != DisciplinaryConsequence.ConsequenceType.COMMUNITY_SERVICE) {
            throw new IllegalArgumentException("Consequence is not community service type");
        }

        consequence.setHoursCompleted(hoursCompleted);
        consequence.setServiceLocation(serviceLocation);

        // Auto-complete if hours requirement met
        if (consequence.getDuration() != null && hoursCompleted >= consequence.getDuration()) {
            consequence.setStatus(DisciplinaryConsequence.ConsequenceStatus.COMPLETED);
            consequence.setCompletionDate(LocalDate.now());
        } else {
            consequence.setStatus(DisciplinaryConsequence.ConsequenceStatus.IN_PROGRESS);
        }

        return consequenceRepository.save(consequence);
    }

    /**
     * Record restitution payment
     */
    public DisciplinaryConsequence recordRestitutionPayment(Long consequenceId, double paymentAmount) {
        log.info("Recording restitution payment of ${} for consequence {}", paymentAmount, consequenceId);

        DisciplinaryConsequence consequence = getConsequenceById(consequenceId);

        if (consequence.getConsequenceType() != DisciplinaryConsequence.ConsequenceType.RESTITUTION) {
            throw new IllegalArgumentException("Consequence is not restitution type");
        }

        double currentPaid = consequence.getRestitutionPaid() != null ? consequence.getRestitutionPaid() : 0.0;
        double newTotal = currentPaid + paymentAmount;
        consequence.setRestitutionPaid(newTotal);

        // Auto-complete if fully paid
        if (consequence.isRestitutionPaid()) {
            consequence.setStatus(DisciplinaryConsequence.ConsequenceStatus.COMPLETED);
            consequence.setCompletionDate(LocalDate.now());
        } else {
            consequence.setStatus(DisciplinaryConsequence.ConsequenceStatus.IN_PROGRESS);
        }

        return consequenceRepository.save(consequence);
    }

    /**
     * Upload behavioral contract document
     */
    public DisciplinaryConsequence uploadBehavioralContract(Long consequenceId, String documentPath) {
        log.info("Uploading behavioral contract for consequence {}", consequenceId);

        DisciplinaryConsequence consequence = getConsequenceById(consequenceId);

        if (consequence.getConsequenceType() != DisciplinaryConsequence.ConsequenceType.BEHAVIOR_CONTRACT) {
            throw new IllegalArgumentException("Consequence is not behavior contract type");
        }

        consequence.setContractDocumentPath(documentPath);
        consequence.setStatus(DisciplinaryConsequence.ConsequenceStatus.IN_PROGRESS);

        return consequenceRepository.save(consequence);
    }

    /**
     * Record parent notification
     */
    public DisciplinaryConsequence recordParentNotification(Long consequenceId, LocalDate notificationDate) {
        log.info("Recording parent notification for consequence {}", consequenceId);

        DisciplinaryConsequence consequence = getConsequenceById(consequenceId);
        consequence.setParentNotified(true);
        consequence.setParentNotificationDate(notificationDate);

        return consequenceRepository.save(consequence);
    }

    // ========================================================================
    // CONSEQUENCE COMPLETION
    // ========================================================================

    /**
     * Mark consequence as completed
     */
    public DisciplinaryConsequence completeConsequence(Long consequenceId, String completionNotes) {
        log.info("Completing consequence {}", consequenceId);

        DisciplinaryConsequence consequence = getConsequenceById(consequenceId);
        consequence.setStatus(DisciplinaryConsequence.ConsequenceStatus.COMPLETED);
        consequence.setCompletionDate(LocalDate.now());
        consequence.setCompletionNotes(completionNotes);

        return consequenceRepository.save(consequence);
    }

    /**
     * Verify consequence completion
     */
    public DisciplinaryConsequence verifyCompletion(
            Long consequenceId,
            Long verifiedById,
            String verificationNotes) {

        log.info("Verifying completion of consequence {}", consequenceId);

        DisciplinaryConsequence consequence = getConsequenceById(consequenceId);
        Teacher verifiedBy = teacherRepository.findById(verifiedById)
                .orElseThrow(() -> new IllegalArgumentException("Verifier not found: " + verifiedById));

        consequence.setVerifiedCompleted(true);
        consequence.setVerifiedBy(verifiedBy);

        if (verificationNotes != null) {
            String notes = consequence.getCompletionNotes();
            consequence.setCompletionNotes(notes != null ? notes + "\n\nVerification: " + verificationNotes : verificationNotes);
        }

        return consequenceRepository.save(consequence);
    }

    // ========================================================================
    // APPEAL PROCESS
    // ========================================================================

    /**
     * File appeal for consequence
     */
    public DisciplinaryConsequence fileAppeal(Long consequenceId, LocalDate appealDate) {
        log.info("Filing appeal for consequence {}", consequenceId);

        DisciplinaryConsequence consequence = getConsequenceById(consequenceId);
        consequence.setAppealFiled(true);
        consequence.setAppealDate(appealDate);
        consequence.setAppealOutcome(DisciplinaryConsequence.AppealOutcome.PENDING);
        consequence.setStatus(DisciplinaryConsequence.ConsequenceStatus.APPEALED);

        return consequenceRepository.save(consequence);
    }

    /**
     * Resolve appeal
     */
    public DisciplinaryConsequence resolveAppeal(
            Long consequenceId,
            DisciplinaryConsequence.AppealOutcome outcome,
            Integer newDuration) {

        log.info("Resolving appeal for consequence {} with outcome {}", consequenceId, outcome);

        DisciplinaryConsequence consequence = getConsequenceById(consequenceId);
        consequence.setAppealOutcome(outcome);

        switch (outcome) {
            case UPHELD:
                consequence.setStatus(DisciplinaryConsequence.ConsequenceStatus.IN_PROGRESS);
                break;
            case OVERTURNED:
                consequence.setStatus(DisciplinaryConsequence.ConsequenceStatus.OVERTURNED);
                consequence.setCompletionDate(LocalDate.now());
                break;
            case REDUCED:
                consequence.setStatus(DisciplinaryConsequence.ConsequenceStatus.MODIFIED);
                if (newDuration != null) {
                    consequence.setDuration(newDuration);
                }
                break;
            case MODIFIED:
                consequence.setStatus(DisciplinaryConsequence.ConsequenceStatus.MODIFIED);
                break;
        }

        return consequenceRepository.save(consequence);
    }

    // ========================================================================
    // QUERY METHODS
    // ========================================================================

    /**
     * Get consequence by ID
     */
    public DisciplinaryConsequence getConsequenceById(Long consequenceId) {
        return consequenceRepository.findById(consequenceId)
                .orElseThrow(() -> new IllegalArgumentException("Consequence not found: " + consequenceId));
    }

    /**
     * Get all consequences for student
     */
    public List<DisciplinaryConsequence> getConsequencesByStudent(Long studentId) {
        return consequenceRepository.findByStudentIdOrderByStartDateDesc(studentId);
    }

    /**
     * Get active consequences for student
     */
    public List<DisciplinaryConsequence> getActiveConsequencesByStudent(Long studentId) {
        return consequenceRepository.findActiveConsequencesByStudent(studentId);
    }

    /**
     * Get consequences requiring tracking
     */
    public List<DisciplinaryConsequence> getConsequencesRequiringTracking() {
        return consequenceRepository.findConsequencesRequiringTracking();
    }

    /**
     * Get overdue consequences
     */
    public List<DisciplinaryConsequence> getOverdueConsequences() {
        return consequenceRepository.findOverdueConsequences(LocalDate.now());
    }

    /**
     * Get consequences pending verification
     */
    public List<DisciplinaryConsequence> getConsequencesPendingVerification() {
        return consequenceRepository.findPendingVerification();
    }

    /**
     * Get consequences with pending appeals
     */
    public List<DisciplinaryConsequence> getConsequencesWithPendingAppeals() {
        return consequenceRepository.findPendingAppeals();
    }

    /**
     * Get consequences without parent notification
     */
    public List<DisciplinaryConsequence> getConsequencesWithoutParentNotification() {
        return consequenceRepository.findWithoutParentNotification();
    }

    /**
     * Get incomplete community service
     */
    public List<DisciplinaryConsequence> getIncompleteCommunityService() {
        return consequenceRepository.findIncompleteCommunityService();
    }

    /**
     * Get outstanding restitution
     */
    public List<DisciplinaryConsequence> getOutstandingRestitution() {
        return consequenceRepository.findOutstandingRestitution();
    }

    /**
     * Count consequences by student in date range
     */
    public long countConsequencesByStudent(Long studentId, LocalDate startDate, LocalDate endDate) {
        return consequenceRepository.countByStudentAndDateRange(studentId, startDate, endDate);
    }
}
