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
 * Service for managing disciplinary referrals
 * Handles creation, review, and resolution of formal disciplinary referrals
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Service
@Transactional
public class DisciplinaryReferralService {

    @Autowired
    private DisciplinaryReferralRepository referralRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private BehaviorIncidentRepository incidentRepository;

    @Autowired
    private CampusRepository campusRepository;

    // ========================================================================
    // REFERRAL CREATION
    // ========================================================================

    /**
     * Create new disciplinary referral
     */
    public DisciplinaryReferral createReferral(
            Long studentId,
            Long referringTeacherId,
            DisciplinaryReferral.ReferralReason reason,
            LocalDate incidentDate,
            String incidentDescription,
            DisciplinaryReferral.Priority priority) {

        log.info("Creating disciplinary referral for student {} by teacher {}", studentId, referringTeacherId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        Teacher referringTeacher = teacherRepository.findById(referringTeacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + referringTeacherId));

        DisciplinaryReferral referral = DisciplinaryReferral.builder()
                .student(student)
                .referringTeacher(referringTeacher)
                .referralDate(LocalDate.now())
                .incidentDate(incidentDate)
                .referralReason(reason)
                .incidentDescription(incidentDescription)
                .priority(priority != null ? priority : DisciplinaryReferral.Priority.NORMAL)
                .status(DisciplinaryReferral.ReferralStatus.PENDING)
                .parentContacted(false)
                .build();

        DisciplinaryReferral saved = referralRepository.save(referral);
        log.info("Created referral {} for student {}", saved.getId(), studentId);

        return saved;
    }

    /**
     * Create referral from behavior incident
     */
    public DisciplinaryReferral createReferralFromIncident(
            Long incidentId,
            Long referringTeacherId,
            String additionalNotes) {

        log.info("Creating referral from behavior incident {}", incidentId);

        BehaviorIncident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Behavior incident not found: " + incidentId));

        Teacher referringTeacher = teacherRepository.findById(referringTeacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + referringTeacherId));

        // Map behavior category to referral reason
        DisciplinaryReferral.ReferralReason reason = mapBehaviorCategoryToReferralReason(incident.getBehaviorCategory());

        // Set priority based on severity
        DisciplinaryReferral.Priority priority = incident.getSeverityLevel() == BehaviorIncident.SeverityLevel.MAJOR
                ? DisciplinaryReferral.Priority.HIGH
                : DisciplinaryReferral.Priority.NORMAL;

        String description = incident.getIncidentDescription();
        if (additionalNotes != null && !additionalNotes.isEmpty()) {
            description += "\n\nAdditional Notes: " + additionalNotes;
        }

        DisciplinaryReferral referral = DisciplinaryReferral.builder()
                .behaviorIncident(incident)
                .student(incident.getStudent())
                .referringTeacher(referringTeacher)
                .referralDate(LocalDate.now())
                .incidentDate(incident.getIncidentDate())
                .referralReason(reason)
                .incidentDescription(description)
                .priority(priority)
                .status(DisciplinaryReferral.ReferralStatus.PENDING)
                .parentContacted(incident.getParentContacted() != null && incident.getParentContacted())
                .parentContactMethod(incident.getParentContactMethod())
                .parentContactDate(incident.getParentContactDate())
                .campus(incident.getCampus())
                .build();

        DisciplinaryReferral saved = referralRepository.save(referral);
        log.info("Created referral {} from incident {}", saved.getId(), incidentId);

        return saved;
    }

    // ========================================================================
    // REFERRAL ASSIGNMENT
    // ========================================================================

    /**
     * Assign referral to administrator
     */
    public DisciplinaryReferral assignToAdministrator(Long referralId, Long administratorId) {
        log.info("Assigning referral {} to administrator {}", referralId, administratorId);

        DisciplinaryReferral referral = getReferralById(referralId);
        Teacher administrator = teacherRepository.findById(administratorId)
                .orElseThrow(() -> new IllegalArgumentException("Administrator not found: " + administratorId));

        referral.setAssignedAdministrator(administrator);
        referral.setStatus(DisciplinaryReferral.ReferralStatus.UNDER_REVIEW);

        DisciplinaryReferral updated = referralRepository.save(referral);
        log.info("Assigned referral {} to administrator {}", referralId, administratorId);

        return updated;
    }

    // ========================================================================
    // REFERRAL REVIEW
    // ========================================================================

    /**
     * Add administrative notes to referral
     */
    public DisciplinaryReferral addAdminNotes(Long referralId, String notes) {
        log.info("Adding admin notes to referral {}", referralId);

        DisciplinaryReferral referral = getReferralById(referralId);
        String existingNotes = referral.getAdminNotes();

        if (existingNotes != null && !existingNotes.isEmpty()) {
            referral.setAdminNotes(existingNotes + "\n\n" + LocalDate.now() + ": " + notes);
        } else {
            referral.setAdminNotes(LocalDate.now() + ": " + notes);
        }

        return referralRepository.save(referral);
    }

    /**
     * Record parent contact for referral
     */
    public DisciplinaryReferral recordParentContact(
            Long referralId,
            BehaviorIncident.ContactMethod method,
            LocalDate contactDate) {

        log.info("Recording parent contact for referral {}", referralId);

        DisciplinaryReferral referral = getReferralById(referralId);
        referral.setParentContacted(true);
        referral.setParentContactMethod(method);
        referral.setParentContactDate(contactDate);

        return referralRepository.save(referral);
    }

    // ========================================================================
    // REFERRAL RESOLUTION
    // ========================================================================

    /**
     * Resolve referral with disposition
     */
    public DisciplinaryReferral resolveReferral(
            Long referralId,
            String disposition,
            String followUpActions) {

        log.info("Resolving referral {}", referralId);

        DisciplinaryReferral referral = getReferralById(referralId);
        referral.setStatus(DisciplinaryReferral.ReferralStatus.RESOLVED);
        referral.setReviewedDate(LocalDate.now());
        referral.setDisposition(disposition);
        referral.setFollowUpActions(followUpActions);

        DisciplinaryReferral updated = referralRepository.save(referral);
        log.info("Resolved referral {}", referralId);

        return updated;
    }

    /**
     * Close referral without action
     */
    public DisciplinaryReferral closeReferral(Long referralId, String reason) {
        log.info("Closing referral {} without action", referralId);

        DisciplinaryReferral referral = getReferralById(referralId);
        referral.setStatus(DisciplinaryReferral.ReferralStatus.CLOSED);
        referral.setReviewedDate(LocalDate.now());
        referral.setDisposition("Closed: " + reason);

        return referralRepository.save(referral);
    }

    /**
     * Escalate referral to higher authority
     */
    public DisciplinaryReferral escalateReferral(Long referralId, String escalationReason) {
        log.info("Escalating referral {}", referralId);

        DisciplinaryReferral referral = getReferralById(referralId);
        referral.setStatus(DisciplinaryReferral.ReferralStatus.ESCALATED);
        referral.setPriority(DisciplinaryReferral.Priority.URGENT);

        String notes = "ESCALATED: " + escalationReason;
        addAdminNotes(referralId, notes);

        return referralRepository.save(referral);
    }

    /**
     * Schedule formal hearing for referral
     */
    public DisciplinaryReferral scheduleHearing(Long referralId) {
        log.info("Scheduling hearing for referral {}", referralId);

        DisciplinaryReferral referral = getReferralById(referralId);
        referral.setStatus(DisciplinaryReferral.ReferralStatus.PENDING_HEARING);
        referral.setPriority(DisciplinaryReferral.Priority.HIGH);

        return referralRepository.save(referral);
    }

    /**
     * Save or update referral
     */
    public DisciplinaryReferral saveReferral(DisciplinaryReferral referral) {
        log.info("Saving disciplinary referral");
        return referralRepository.save(referral);
    }

    // ========================================================================
    // QUERY METHODS
    // ========================================================================

    /**
     * Get referral by ID
     */
    public DisciplinaryReferral getReferralById(Long referralId) {
        return referralRepository.findById(referralId)
                .orElseThrow(() -> new IllegalArgumentException("Referral not found: " + referralId));
    }

    /**
     * Get all referrals for student
     */
    public List<DisciplinaryReferral> getReferralsByStudent(Long studentId) {
        return referralRepository.findByStudentIdOrderByReferralDateDesc(studentId);
    }

    /**
     * Get pending referrals
     */
    public List<DisciplinaryReferral> getPendingReferrals() {
        return referralRepository.findByStatusOrderByReferralDateDesc(
                DisciplinaryReferral.ReferralStatus.PENDING);
    }

    /**
     * Get urgent referrals
     */
    public List<DisciplinaryReferral> getUrgentReferrals() {
        return referralRepository.findUrgentReferrals();
    }

    /**
     * Get referrals assigned to administrator
     */
    public List<DisciplinaryReferral> getReferralsByAdministrator(Long administratorId) {
        return referralRepository.findByAssignedAdministratorIdAndStatus(
                administratorId, DisciplinaryReferral.ReferralStatus.UNDER_REVIEW);
    }

    /**
     * Get overdue referrals (pending for more than N days)
     */
    public List<DisciplinaryReferral> getOverdueReferrals(int daysOld) {
        LocalDate cutoffDate = LocalDate.now().minusDays(daysOld);
        return referralRepository.findOverdueReferrals(cutoffDate);
    }

    /**
     * Get referrals without parent contact
     */
    public List<DisciplinaryReferral> getReferralsWithoutParentContact() {
        return referralRepository.findReferralsWithoutParentContact();
    }

    /**
     * Get referrals by teacher for date range
     */
    public List<DisciplinaryReferral> getReferralsByTeacherAndDateRange(
            Long teacherId, LocalDate startDate, LocalDate endDate) {
        return referralRepository.findByTeacherAndDateRange(teacherId, startDate, endDate);
    }

    /**
     * Count referrals by student in date range
     */
    public long countReferralsByStudent(Long studentId, LocalDate startDate, LocalDate endDate) {
        return referralRepository.countByStudentAndDateRange(studentId, startDate, endDate);
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Map behavior category to referral reason
     */
    private DisciplinaryReferral.ReferralReason mapBehaviorCategoryToReferralReason(
            BehaviorIncident.BehaviorCategory category) {

        return switch (category) {
            case DISRUPTION -> DisciplinaryReferral.ReferralReason.CHRONIC_DISRUPTION;
            case FIGHTING -> DisciplinaryReferral.ReferralReason.FIGHTING;
            case BULLYING -> DisciplinaryReferral.ReferralReason.BULLYING;
            case HARASSMENT -> DisciplinaryReferral.ReferralReason.HARASSMENT;
            case DEFIANCE -> DisciplinaryReferral.ReferralReason.DEFIANCE;
            case VANDALISM -> DisciplinaryReferral.ReferralReason.VANDALISM;
            case THEFT -> DisciplinaryReferral.ReferralReason.THEFT;
            case TECHNOLOGY_MISUSE -> DisciplinaryReferral.ReferralReason.TECHNOLOGY_MISUSE;
            case TARDINESS -> DisciplinaryReferral.ReferralReason.CHRONIC_TARDINESS;
            case NON_COMPLIANCE -> DisciplinaryReferral.ReferralReason.INSUBORDINATION;
            case INAPPROPRIATE_LANGUAGE -> DisciplinaryReferral.ReferralReason.INAPPROPRIATE_LANGUAGE;
            default -> DisciplinaryReferral.ReferralReason.OTHER;
        };
    }
}
