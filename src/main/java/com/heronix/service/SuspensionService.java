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
 * Service for managing student suspensions
 * Handles ISS, OSS, emergency removals, and extended suspensions
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Service
@Transactional
public class SuspensionService {

    @Autowired
    private SuspensionRepository suspensionRepository;

    @Autowired
    private DisciplinaryConsequenceRepository consequenceRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    // ========================================================================
    // SUSPENSION CREATION
    // ========================================================================

    /**
     * Create new suspension
     */
    public Suspension createSuspension(
            Long studentId,
            Long issuedById,
            Suspension.SuspensionType type,
            LocalDate startDate,
            LocalDate endDate,
            int daysCount,
            String reason,
            String incidentDescription) {

        log.info("Creating {} suspension for student {}", type, studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        Teacher issuedBy = teacherRepository.findById(issuedById)
                .orElseThrow(() -> new IllegalArgumentException("Administrator not found: " + issuedById));

        // Validate suspension type and duration
        validateSuspensionDuration(type, daysCount);

        Suspension suspension = Suspension.builder()
                .student(student)
                .issuedBy(issuedBy)
                .suspensionType(type)
                .startDate(startDate)
                .endDate(endDate)
                .daysCount(daysCount)
                .status(Suspension.SuspensionStatus.PENDING)
                .suspensionReason(reason)
                .incidentDescription(incidentDescription)
                .parentNotified(false)
                .reentryMeetingRequired(shouldRequireReentryMeeting(type, daysCount))
                .reentryMeetingCompleted(false)
                .homeworkProvided(false)
                .homeworkCompleted(false)
                .appealFiled(false)
                .expulsionRecommended(false)
                .completionVerified(false)
                .build();

        Suspension saved = suspensionRepository.save(suspension);
        log.info("Created suspension {} for student {}", saved.getId(), studentId);

        return saved;
    }

    /**
     * Create suspension from consequence
     */
    public Suspension createSuspensionFromConsequence(
            Long consequenceId,
            Suspension.SuspensionType type,
            LocalDate startDate,
            LocalDate endDate,
            int daysCount) {

        log.info("Creating suspension from consequence {}", consequenceId);

        DisciplinaryConsequence consequence = consequenceRepository.findById(consequenceId)
                .orElseThrow(() -> new IllegalArgumentException("Consequence not found: " + consequenceId));

        // Verify consequence type matches
        if (type == Suspension.SuspensionType.IN_SCHOOL &&
            consequence.getConsequenceType() != DisciplinaryConsequence.ConsequenceType.IN_SCHOOL_SUSPENSION) {
            throw new IllegalArgumentException("Consequence type mismatch for ISS");
        }
        if ((type == Suspension.SuspensionType.OUT_OF_SCHOOL || type == Suspension.SuspensionType.EXTENDED_OSS) &&
            consequence.getConsequenceType() != DisciplinaryConsequence.ConsequenceType.OUT_OF_SCHOOL_SUSPENSION) {
            throw new IllegalArgumentException("Consequence type mismatch for OSS");
        }

        Suspension suspension = Suspension.builder()
                .consequence(consequence)
                .student(consequence.getStudent())
                .issuedBy(consequence.getAssignedBy())
                .suspensionType(type)
                .startDate(startDate)
                .endDate(endDate)
                .daysCount(daysCount)
                .status(Suspension.SuspensionStatus.PENDING)
                .suspensionReason(consequence.getDescription())
                .parentNotified(consequence.getParentNotified())
                .parentNotificationDate(consequence.getParentNotificationDate())
                .reentryMeetingRequired(shouldRequireReentryMeeting(type, daysCount))
                .reentryMeetingCompleted(false)
                .homeworkProvided(false)
                .homeworkCompleted(false)
                .appealFiled(false)
                .expulsionRecommended(false)
                .completionVerified(false)
                .campus(consequence.getCampus())
                .build();

        Suspension saved = suspensionRepository.save(suspension);
        log.info("Created suspension {} from consequence {}", saved.getId(), consequenceId);

        return saved;
    }

    // ========================================================================
    // ISS MANAGEMENT
    // ========================================================================

    /**
     * Assign ISS room and supervisor
     */
    public Suspension assignISSRoomAndSupervisor(
            Long suspensionId,
            String roomAssignment,
            Long supervisorId) {

        log.info("Assigning ISS room {} and supervisor {} to suspension {}", roomAssignment, supervisorId, suspensionId);

        Suspension suspension = getSuspensionById(suspensionId);

        if (!suspension.isInSchool()) {
            throw new IllegalArgumentException("Suspension is not in-school type");
        }

        Teacher supervisor = teacherRepository.findById(supervisorId)
                .orElseThrow(() -> new IllegalArgumentException("Supervisor not found: " + supervisorId));

        suspension.setIssRoomAssignment(roomAssignment);
        suspension.setIssSupervisor(supervisor);

        return suspensionRepository.save(suspension);
    }

    /**
     * Record ISS daily check-in
     */
    public Suspension recordISSCheckIn(Long suspensionId, String dailyNote) {
        log.info("Recording ISS check-in for suspension {}", suspensionId);

        Suspension suspension = getSuspensionById(suspensionId);

        if (!suspension.isInSchool()) {
            throw new IllegalArgumentException("Suspension is not in-school type");
        }

        String existingNotes = suspension.getDailyNotes();
        String newNotes = LocalDate.now() + ": " + dailyNote;

        if (existingNotes != null && !existingNotes.isEmpty()) {
            suspension.setDailyNotes(existingNotes + "\n\n" + newNotes);
        } else {
            suspension.setDailyNotes(newNotes);
        }

        // Increment days attended
        Integer daysAttended = suspension.getDaysAttended() != null ? suspension.getDaysAttended() : 0;
        suspension.setDaysAttended(daysAttended + 1);

        return suspensionRepository.save(suspension);
    }

    // ========================================================================
    // OSS MANAGEMENT
    // ========================================================================

    /**
     * Set alternative placement for extended OSS
     */
    public Suspension setAlternativePlacement(Long suspensionId, String alternativePlacement) {
        log.info("Setting alternative placement for suspension {}", suspensionId);

        Suspension suspension = getSuspensionById(suspensionId);

        if (!suspension.isOutOfSchool()) {
            throw new IllegalArgumentException("Suspension is not out-of-school type");
        }

        suspension.setAlternativePlacement(alternativePlacement);

        return suspensionRepository.save(suspension);
    }

    // ========================================================================
    // SUSPENSION STATUS
    // ========================================================================

    /**
     * Activate suspension (move from PENDING to ACTIVE)
     */
    public Suspension activateSuspension(Long suspensionId) {
        log.info("Activating suspension {}", suspensionId);

        Suspension suspension = getSuspensionById(suspensionId);
        suspension.setStatus(Suspension.SuspensionStatus.ACTIVE);

        return suspensionRepository.save(suspension);
    }

    /**
     * Complete suspension
     */
    public Suspension completeSuspension(Long suspensionId) {
        log.info("Completing suspension {}", suspensionId);

        Suspension suspension = getSuspensionById(suspensionId);
        suspension.setStatus(Suspension.SuspensionStatus.COMPLETED);

        return suspensionRepository.save(suspension);
    }

    /**
     * Verify suspension completion
     */
    public Suspension verifyCompletion(Long suspensionId, Long verifiedById) {
        log.info("Verifying completion of suspension {}", suspensionId);

        Suspension suspension = getSuspensionById(suspensionId);
        Teacher verifiedBy = teacherRepository.findById(verifiedById)
                .orElseThrow(() -> new IllegalArgumentException("Verifier not found: " + verifiedById));

        suspension.setCompletionVerified(true);
        suspension.setCompletionVerificationDate(LocalDate.now());
        suspension.setVerifiedBy(verifiedBy);

        return suspensionRepository.save(suspension);
    }

    // ========================================================================
    // PARENT NOTIFICATION
    // ========================================================================

    /**
     * Record parent notification
     */
    public Suspension recordParentNotification(
            Long suspensionId,
            BehaviorIncident.ContactMethod method,
            LocalDate notificationDate,
            String documentPath) {

        log.info("Recording parent notification for suspension {}", suspensionId);

        Suspension suspension = getSuspensionById(suspensionId);
        suspension.setParentNotified(true);
        suspension.setParentNotificationDate(notificationDate);
        suspension.setParentNotificationMethod(method);
        if (documentPath != null) {
            suspension.setNotificationDocumentPath(documentPath);
        }

        return suspensionRepository.save(suspension);
    }

    // ========================================================================
    // RE-ENTRY PROCESS
    // ========================================================================

    /**
     * Schedule re-entry meeting
     */
    public Suspension scheduleReentryMeeting(Long suspensionId, LocalDate meetingDate) {
        log.info("Scheduling re-entry meeting for suspension {}", suspensionId);

        Suspension suspension = getSuspensionById(suspensionId);
        suspension.setReentryMeetingDate(meetingDate);

        return suspensionRepository.save(suspension);
    }

    /**
     * Complete re-entry meeting
     */
    public Suspension completeReentryMeeting(Long suspensionId, String reentryPlan) {
        log.info("Completing re-entry meeting for suspension {}", suspensionId);

        Suspension suspension = getSuspensionById(suspensionId);
        suspension.setReentryMeetingCompleted(true);
        suspension.setReentryPlan(reentryPlan);

        return suspensionRepository.save(suspension);
    }

    // ========================================================================
    // HOMEWORK TRACKING
    // ========================================================================

    /**
     * Record homework provided
     */
    public Suspension recordHomeworkProvided(Long suspensionId) {
        log.info("Recording homework provided for suspension {}", suspensionId);

        Suspension suspension = getSuspensionById(suspensionId);
        suspension.setHomeworkProvided(true);

        return suspensionRepository.save(suspension);
    }

    /**
     * Record homework completed
     */
    public Suspension recordHomeworkCompleted(Long suspensionId) {
        log.info("Recording homework completed for suspension {}", suspensionId);

        Suspension suspension = getSuspensionById(suspensionId);
        suspension.setHomeworkCompleted(true);

        return suspensionRepository.save(suspension);
    }

    // ========================================================================
    // APPEAL PROCESS
    // ========================================================================

    /**
     * File appeal for suspension
     */
    public Suspension fileAppeal(Long suspensionId, LocalDate hearingDate) {
        log.info("Filing appeal for suspension {}", suspensionId);

        Suspension suspension = getSuspensionById(suspensionId);
        suspension.setAppealFiled(true);
        suspension.setAppealHearingDate(hearingDate);
        suspension.setAppealOutcome(Suspension.AppealOutcome.PENDING);
        suspension.setStatus(Suspension.SuspensionStatus.APPEALED);

        return suspensionRepository.save(suspension);
    }

    /**
     * Resolve appeal
     */
    public Suspension resolveAppeal(
            Long suspensionId,
            Suspension.AppealOutcome outcome,
            String appealDecision) {

        log.info("Resolving appeal for suspension {} with outcome {}", suspensionId, outcome);

        Suspension suspension = getSuspensionById(suspensionId);
        suspension.setAppealOutcome(outcome);
        suspension.setAppealDecision(appealDecision);

        switch (outcome) {
            case UPHELD:
                suspension.setStatus(Suspension.SuspensionStatus.ACTIVE);
                break;
            case OVERTURNED:
                suspension.setStatus(Suspension.SuspensionStatus.OVERTURNED);
                break;
            case REDUCED:
                suspension.setStatus(Suspension.SuspensionStatus.REDUCED);
                break;
            case CONVERTED:
                // Status will be updated by conversion method
                break;
        }

        return suspensionRepository.save(suspension);
    }

    /**
     * Convert ISS to OSS
     */
    public Suspension convertToOSS(Long suspensionId, String reason) {
        log.info("Converting suspension {} from ISS to OSS", suspensionId);

        Suspension suspension = getSuspensionById(suspensionId);

        if (!suspension.isInSchool()) {
            throw new IllegalArgumentException("Suspension is not ISS type");
        }

        suspension.setSuspensionType(Suspension.SuspensionType.OUT_OF_SCHOOL);
        suspension.setStatus(Suspension.SuspensionStatus.CONVERTED_TO_OSS);

        // Clear ISS-specific fields
        suspension.setIssRoomAssignment(null);
        suspension.setIssSupervisor(null);
        suspension.setDailyNotes(suspension.getDailyNotes() + "\n\nCONVERTED TO OSS: " + reason);

        return suspensionRepository.save(suspension);
    }

    /**
     * Convert OSS to ISS
     */
    public Suspension convertToISS(Long suspensionId, String reason) {
        log.info("Converting suspension {} from OSS to ISS", suspensionId);

        Suspension suspension = getSuspensionById(suspensionId);

        if (!suspension.isOutOfSchool()) {
            throw new IllegalArgumentException("Suspension is not OSS type");
        }

        suspension.setSuspensionType(Suspension.SuspensionType.IN_SCHOOL);
        suspension.setStatus(Suspension.SuspensionStatus.CONVERTED_TO_ISS);

        // Clear OSS-specific fields
        suspension.setAlternativePlacement(null);
        String description = suspension.getIncidentDescription();
        suspension.setIncidentDescription(description + "\n\nCONVERTED TO ISS: " + reason);

        return suspensionRepository.save(suspension);
    }

    /**
     * Extend suspension
     */
    public Suspension extendSuspension(Long suspensionId, int additionalDays, String reason) {
        log.info("Extending suspension {} by {} days", suspensionId, additionalDays);

        Suspension suspension = getSuspensionById(suspensionId);
        suspension.setEndDate(suspension.getEndDate().plusDays(additionalDays));
        suspension.setDaysCount(suspension.getDaysCount() + additionalDays);
        suspension.setStatus(Suspension.SuspensionStatus.EXTENDED);

        String description = suspension.getIncidentDescription();
        suspension.setIncidentDescription(description + "\n\nEXTENDED " + additionalDays + " DAYS: " + reason);

        return suspensionRepository.save(suspension);
    }

    // ========================================================================
    // EXPULSION PROCESS
    // ========================================================================

    /**
     * Recommend for expulsion
     */
    public Suspension recommendExpulsion(Long suspensionId, LocalDate hearingDate, String reason) {
        log.info("Recommending expulsion for suspension {}", suspensionId);

        Suspension suspension = getSuspensionById(suspensionId);
        suspension.setExpulsionRecommended(true);
        suspension.setExpulsionHearingDate(hearingDate);

        String description = suspension.getIncidentDescription();
        suspension.setIncidentDescription(description + "\n\nEXPULSION RECOMMENDED: " + reason);

        return suspensionRepository.save(suspension);
    }

    // ========================================================================
    // QUERY METHODS
    // ========================================================================

    /**
     * Get suspension by ID
     */
    public Suspension getSuspensionById(Long suspensionId) {
        return suspensionRepository.findById(suspensionId)
                .orElseThrow(() -> new IllegalArgumentException("Suspension not found: " + suspensionId));
    }

    /**
     * Get all suspensions for student
     */
    public List<Suspension> getSuspensionsByStudent(Long studentId) {
        return suspensionRepository.findByStudentIdOrderByStartDateDesc(studentId);
    }

    /**
     * Get active suspensions
     */
    public List<Suspension> getActiveSuspensions() {
        return suspensionRepository.findActiveSuspensions();
    }

    /**
     * Get active suspensions for student
     */
    public List<Suspension> getActiveSuspensionsByStudent(Long studentId) {
        return suspensionRepository.findActiveSuspensionsByStudent(studentId);
    }

    /**
     * Get active ISS suspensions
     */
    public List<Suspension> getActiveInSchoolSuspensions() {
        return suspensionRepository.findActiveInSchoolSuspensions();
    }

    /**
     * Get active OSS suspensions
     */
    public List<Suspension> getActiveOutOfSchoolSuspensions() {
        return suspensionRepository.findActiveOutOfSchoolSuspensions();
    }

    /**
     * Get suspensions requiring re-entry meetings
     */
    public List<Suspension> getSuspensionsRequiringReentryMeeting() {
        return suspensionRepository.findSuspensionsRequiringReentryMeeting();
    }

    /**
     * Get suspensions without parent notification
     */
    public List<Suspension> getSuspensionsWithoutParentNotification() {
        return suspensionRepository.findWithoutParentNotification();
    }

    /**
     * Get suspensions pending verification
     */
    public List<Suspension> getSuspensionsPendingVerification() {
        return suspensionRepository.findPendingVerification();
    }

    /**
     * Get suspensions with pending appeals
     */
    public List<Suspension> getSuspensionsWithPendingAppeals() {
        return suspensionRepository.findPendingAppeals();
    }

    /**
     * Get suspensions starting today
     */
    public List<Suspension> getSuspensionsStartingToday() {
        return suspensionRepository.findStartingToday(LocalDate.now());
    }

    /**
     * Get emergency removals
     */
    public List<Suspension> getEmergencyRemovals() {
        return suspensionRepository.findEmergencyRemovals();
    }

    /**
     * Get expulsion recommendations
     */
    public List<Suspension> getExpulsionRecommendations() {
        return suspensionRepository.findExpulsionRecommendations();
    }

    /**
     * Get total suspension days for student
     */
    public int getTotalSuspensionDays(Long studentId, LocalDate startDate, LocalDate endDate) {
        Integer days = suspensionRepository.getTotalSuspensionDaysByStudent(studentId, startDate, endDate);
        return days != null ? days : 0;
    }

    /**
     * Get total OSS days for student
     */
    public int getTotalOSSDays(Long studentId, LocalDate startDate, LocalDate endDate) {
        Integer days = suspensionRepository.getTotalOSSDaysByStudent(studentId, startDate, endDate);
        return days != null ? days : 0;
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Validate suspension duration based on type
     */
    private void validateSuspensionDuration(Suspension.SuspensionType type, int daysCount) {
        if (type == Suspension.SuspensionType.EXTENDED_OSS && daysCount < 10) {
            throw new IllegalArgumentException("Extended OSS requires minimum 10 days");
        }
    }

    /**
     * Determine if re-entry meeting should be required
     */
    private boolean shouldRequireReentryMeeting(Suspension.SuspensionType type, int daysCount) {
        // Require re-entry meeting for OSS 3+ days or Extended OSS
        return (type == Suspension.SuspensionType.OUT_OF_SCHOOL && daysCount >= 3) ||
               type == Suspension.SuspensionType.EXTENDED_OSS;
    }
}
