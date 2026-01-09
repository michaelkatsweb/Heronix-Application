package com.heronix.service;

import com.heronix.model.domain.BehaviorIncident;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.Teacher;
import com.heronix.model.domain.Course;
import com.heronix.model.domain.Campus;
import com.heronix.model.domain.BehaviorIncident.BehaviorType;
import com.heronix.model.domain.BehaviorIncident.BehaviorCategory;
import com.heronix.model.domain.BehaviorIncident.SeverityLevel;
import com.heronix.model.domain.BehaviorIncident.IncidentLocation;
import com.heronix.model.domain.BehaviorIncident.ContactMethod;
import com.heronix.repository.BehaviorIncidentRepository;
import com.heronix.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing behavior incidents (positive and negative student behaviors).
 *
 * Handles CRUD operations, searching, filtering, and tracking of student behavior incidents
 * for monitoring, intervention, parent communication, and CRDC reporting.
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Discipline/Behavior Management System
 */
@Service
public class BehaviorIncidentService {

    private static final Logger log = LoggerFactory.getLogger(BehaviorIncidentService.class);

    @Autowired
    private BehaviorIncidentRepository behaviorIncidentRepository;

    @Autowired
    private StudentRepository studentRepository;

    // ========================================================================
    // CREATE OPERATIONS
    // ========================================================================

    /**
     * Creates a new behavior incident.
     *
     * @param student the student involved
     * @param reportingTeacher the teacher reporting the incident
     * @param incidentDate the date of the incident
     * @param incidentTime the time of the incident
     * @param behaviorType positive or negative
     * @param behaviorCategory specific behavior category
     * @param incidentLocation where it occurred
     * @param description detailed description
     * @param enteredByStaffId ID of staff member entering the record
     * @return created behavior incident
     */
    @Transactional
    public BehaviorIncident createIncident(
            Student student,
            Teacher reportingTeacher,
            LocalDate incidentDate,
            LocalTime incidentTime,
            BehaviorType behaviorType,
            BehaviorCategory behaviorCategory,
            IncidentLocation incidentLocation,
            String description,
            Long enteredByStaffId) {

        log.info("Creating new {} behavior incident for student ID {} by teacher ID {}",
                behaviorType, student.getId(), reportingTeacher.getId());

        BehaviorIncident incident = BehaviorIncident.builder()
                .student(student)
                .reportingTeacher(reportingTeacher)
                .incidentDate(incidentDate)
                .incidentTime(incidentTime)
                .behaviorType(behaviorType)
                .behaviorCategory(behaviorCategory)
                .incidentLocation(incidentLocation)
                .incidentDescription(description)
                .enteredByStaffId(enteredByStaffId)
                .entryTimestamp(LocalDateTime.now())
                .parentContacted(false)
                .adminReferralRequired(false)
                .evidenceAttached(false)
                .build();

        incident = behaviorIncidentRepository.save(incident);
        log.info("Created behavior incident ID {} for student: {} {}",
                incident.getId(), student.getFirstName(), student.getLastName());

        return incident;
    }

    /**
     * Creates a new behavior incident with full details including course and campus.
     *
     * @param student the student involved
     * @param course the course (optional, null for non-classroom incidents)
     * @param reportingTeacher the teacher reporting the incident
     * @param campus the campus
     * @param incidentDate the date of the incident
     * @param incidentTime the time of the incident
     * @param behaviorType positive or negative
     * @param behaviorCategory specific behavior category
     * @param severityLevel severity (for negative behaviors only)
     * @param incidentLocation where it occurred
     * @param description detailed description
     * @param enteredByStaffId ID of staff member entering the record
     * @return created behavior incident
     */
    @Transactional
    public BehaviorIncident createIncidentWithDetails(
            Student student,
            Course course,
            Teacher reportingTeacher,
            Campus campus,
            LocalDate incidentDate,
            LocalTime incidentTime,
            BehaviorType behaviorType,
            BehaviorCategory behaviorCategory,
            SeverityLevel severityLevel,
            IncidentLocation incidentLocation,
            String description,
            Long enteredByStaffId) {

        log.info("Creating detailed {} behavior incident for student ID {}",
                behaviorType, student.getId());

        BehaviorIncident incident = BehaviorIncident.builder()
                .student(student)
                .course(course)
                .reportingTeacher(reportingTeacher)
                .campus(campus)
                .incidentDate(incidentDate)
                .incidentTime(incidentTime)
                .behaviorType(behaviorType)
                .behaviorCategory(behaviorCategory)
                .severityLevel(severityLevel)
                .incidentLocation(incidentLocation)
                .incidentDescription(description)
                .enteredByStaffId(enteredByStaffId)
                .entryTimestamp(LocalDateTime.now())
                .parentContacted(false)
                .adminReferralRequired(false)
                .evidenceAttached(false)
                .build();

        incident = behaviorIncidentRepository.save(incident);
        log.info("Created detailed behavior incident ID {}", incident.getId());

        return incident;
    }

    // ========================================================================
    // READ OPERATIONS
    // ========================================================================

    /**
     * Gets a behavior incident by ID.
     *
     * @param id the incident ID
     * @return the behavior incident
     */
    public BehaviorIncident getIncidentById(Long id) {
        return behaviorIncidentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Behavior incident not found: " + id));
    }

    /**
     * Gets all behavior incidents for a student.
     *
     * @param student the student
     * @return list of all incidents
     */
    public List<BehaviorIncident> getIncidentsByStudent(Student student) {
        log.debug("Fetching all behavior incidents for student ID {}", student.getId());
        return behaviorIncidentRepository.findByStudent(student);
    }

    /**
     * Gets behavior incidents for a student within a date range.
     *
     * @param student the student
     * @param startDate start of date range
     * @param endDate end of date range
     * @return list of incidents
     */
    public List<BehaviorIncident> getIncidentsByStudentAndDateRange(
            Student student,
            LocalDate startDate,
            LocalDate endDate) {

        log.debug("Fetching behavior incidents for student ID {} from {} to {}",
                student.getId(), startDate, endDate);
        return behaviorIncidentRepository.findByStudentAndIncidentDateBetween(student, startDate, endDate);
    }

    /**
     * Gets behavior incidents by type (positive or negative) for a student.
     *
     * @param student the student
     * @param behaviorType the behavior type
     * @return list of incidents
     */
    public List<BehaviorIncident> getIncidentsByStudentAndType(Student student, BehaviorType behaviorType) {
        log.debug("Fetching {} behavior incidents for student ID {}",
                behaviorType, student.getId());
        return behaviorIncidentRepository.findByStudentAndBehaviorType(student, behaviorType);
    }

    /**
     * Gets positive behavior incidents for a student.
     *
     * @param student the student
     * @return list of positive incidents
     */
    public List<BehaviorIncident> getPositiveIncidents(Student student) {
        return getIncidentsByStudentAndType(student, BehaviorType.POSITIVE);
    }

    /**
     * Gets negative behavior incidents for a student.
     *
     * @param student the student
     * @return list of negative incidents
     */
    public List<BehaviorIncident> getNegativeIncidents(Student student) {
        return getIncidentsByStudentAndType(student, BehaviorType.NEGATIVE);
    }

    /**
     * Gets behavior incidents by type within a date range.
     *
     * @param student the student
     * @param behaviorType the behavior type
     * @param startDate start of date range
     * @param endDate end of date range
     * @return list of incidents
     */
    public List<BehaviorIncident> getIncidentsByStudentTypeAndDateRange(
            Student student,
            BehaviorType behaviorType,
            LocalDate startDate,
            LocalDate endDate) {

        log.debug("Fetching {} incidents for student ID {} from {} to {}",
                behaviorType, student.getId(), startDate, endDate);
        return behaviorIncidentRepository.findByStudentAndBehaviorTypeAndIncidentDateBetween(
                student, behaviorType, startDate, endDate);
    }

    /**
     * Gets behavior incidents by category.
     *
     * @param student the student
     * @param category the behavior category
     * @return list of incidents
     */
    public List<BehaviorIncident> getIncidentsByCategory(Student student, BehaviorCategory category) {
        log.debug("Fetching {} incidents for student ID {}", category, student.getId());
        return behaviorIncidentRepository.findByStudentAndCategory(student, category);
    }

    /**
     * Gets all behavior incidents within a date range (for school/district reporting).
     *
     * @param startDate start of date range
     * @param endDate end of date range
     * @return list of all incidents
     */
    public List<BehaviorIncident> getAllIncidentsByDateRange(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching all behavior incidents from {} to {}", startDate, endDate);
        return behaviorIncidentRepository.findByIncidentDateBetween(startDate, endDate);
    }

    /**
     * Gets all behavior incidents.
     *
     * @return list of all incidents
     */
    public List<BehaviorIncident> getAllIncidents() {
        log.debug("Fetching all behavior incidents");
        return behaviorIncidentRepository.findAll();
    }

    /**
     * Gets incidents requiring admin referral for a student.
     *
     * @param student the student
     * @return list of incidents requiring referral
     */
    public List<BehaviorIncident> getIncidentsRequiringReferral(Student student) {
        log.debug("Fetching incidents requiring admin referral for student ID {}", student.getId());
        return behaviorIncidentRepository.findByStudentAndAdminReferralRequired(student, true);
    }

    /**
     * Gets recent critical incidents (major severity or requiring referral).
     *
     * @param student the student
     * @param sinceDate only include incidents on or after this date
     * @return list of critical incidents
     */
    public List<BehaviorIncident> getCriticalIncidentsSince(Student student, LocalDate sinceDate) {
        log.debug("Fetching critical incidents for student ID {} since {}", student.getId(), sinceDate);
        return behaviorIncidentRepository.findCriticalIncidentsSince(student, sinceDate);
    }

    /**
     * Gets incidents where parent has not been contacted yet.
     *
     * @param student the student
     * @return list of uncontacted incidents
     */
    public List<BehaviorIncident> getUncontactedParentIncidents(Student student) {
        log.debug("Fetching uncontacted parent incidents for student ID {}", student.getId());
        return behaviorIncidentRepository.findUncontactedParentIncidents(student);
    }

    // ========================================================================
    // UPDATE OPERATIONS
    // ========================================================================

    /**
     * Updates an existing behavior incident.
     *
     * @param incident the incident to update
     * @return updated incident
     */
    @Transactional
    public BehaviorIncident updateIncident(BehaviorIncident incident) {
        log.info("Updating behavior incident ID {}", incident.getId());
        return behaviorIncidentRepository.save(incident);
    }

    /**
     * Alias for updateIncident - saves an incident
     *
     * @param incident the incident to save
     * @return saved incident
     */
    @Transactional
    public BehaviorIncident saveIncident(BehaviorIncident incident) {
        if (incident.getId() == null) {
            log.info("Creating new behavior incident");
        } else {
            log.info("Updating behavior incident ID {}", incident.getId());
        }
        return behaviorIncidentRepository.save(incident);
    }

    /**
     * Records parent contact for an incident.
     *
     * @param incidentId the incident ID
     * @param contactDate the date parent was contacted
     * @param contactMethod the method of contact
     * @return updated incident
     */
    @Transactional
    public BehaviorIncident recordParentContact(
            Long incidentId,
            LocalDate contactDate,
            ContactMethod contactMethod) {

        log.info("Recording parent contact for incident ID {} on {} via {}",
                incidentId, contactDate, contactMethod);

        BehaviorIncident incident = getIncidentById(incidentId);
        incident.setParentContacted(true);
        incident.setParentContactDate(contactDate);
        incident.setParentContactMethod(contactMethod);

        incident = behaviorIncidentRepository.save(incident);
        log.info("Parent contact recorded for incident ID {}", incidentId);

        return incident;
    }

    /**
     * Marks an incident as requiring admin referral.
     *
     * @param incidentId the incident ID
     * @return updated incident
     */
    @Transactional
    public BehaviorIncident markAsRequiringReferral(Long incidentId) {
        log.info("Marking incident ID {} as requiring admin referral", incidentId);

        BehaviorIncident incident = getIncidentById(incidentId);
        incident.setAdminReferralRequired(true);

        incident = behaviorIncidentRepository.save(incident);
        log.info("Incident ID {} marked as requiring referral", incidentId);

        return incident;
    }

    /**
     * Records the outcome of an admin referral.
     *
     * @param incidentId the incident ID
     * @param outcome the referral outcome description
     * @return updated incident
     */
    @Transactional
    public BehaviorIncident recordReferralOutcome(Long incidentId, String outcome) {
        log.info("Recording referral outcome for incident ID {}", incidentId);

        BehaviorIncident incident = getIncidentById(incidentId);
        incident.setReferralOutcome(outcome);

        incident = behaviorIncidentRepository.save(incident);
        log.info("Referral outcome recorded for incident ID {}", incidentId);

        return incident;
    }

    /**
     * Records intervention applied for an incident.
     *
     * @param incidentId the incident ID
     * @param intervention description of intervention
     * @return updated incident
     */
    @Transactional
    public BehaviorIncident recordIntervention(Long incidentId, String intervention) {
        log.info("Recording intervention for incident ID {}", incidentId);

        BehaviorIncident incident = getIncidentById(incidentId);
        incident.setInterventionApplied(intervention);

        incident = behaviorIncidentRepository.save(incident);
        log.info("Intervention recorded for incident ID {}", incidentId);

        return incident;
    }

    /**
     * Attaches evidence to an incident.
     *
     * @param incidentId the incident ID
     * @param evidenceFilePath file path to evidence
     * @return updated incident
     */
    @Transactional
    public BehaviorIncident attachEvidence(Long incidentId, String evidenceFilePath) {
        log.info("Attaching evidence to incident ID {}: {}", incidentId, evidenceFilePath);

        BehaviorIncident incident = getIncidentById(incidentId);
        incident.setEvidenceAttached(true);
        incident.setEvidenceFilePath(evidenceFilePath);

        incident = behaviorIncidentRepository.save(incident);
        log.info("Evidence attached to incident ID {}", incidentId);

        return incident;
    }

    // ========================================================================
    // DELETE OPERATIONS
    // ========================================================================

    /**
     * Deletes a behavior incident.
     * Should only be used for erroneous entries. Most incidents should remain for historical tracking.
     *
     * @param incidentId the incident ID
     */
    @Transactional
    public void deleteIncident(Long incidentId) {
        log.warn("Deleting behavior incident ID {}", incidentId);

        BehaviorIncident incident = getIncidentById(incidentId);
        behaviorIncidentRepository.delete(incident);

        log.info("Deleted behavior incident ID {}", incidentId);
    }

    // ========================================================================
    // STATISTICS AND COUNTS
    // ========================================================================

    /**
     * Counts behavior incidents by type within a date range.
     *
     * @param student the student
     * @param behaviorType the behavior type
     * @param startDate start of date range
     * @param endDate end of date range
     * @return count of incidents
     */
    public Long countIncidentsByTypeAndDateRange(
            Student student,
            BehaviorType behaviorType,
            LocalDate startDate,
            LocalDate endDate) {

        return behaviorIncidentRepository.countByStudentAndBehaviorTypeAndDateBetween(
                student, behaviorType, startDate, endDate);
    }

    /**
     * Counts positive behavior incidents within a date range.
     *
     * @param student the student
     * @param startDate start of date range
     * @param endDate end of date range
     * @return count of positive incidents
     */
    public Long countPositiveIncidents(Student student, LocalDate startDate, LocalDate endDate) {
        return countIncidentsByTypeAndDateRange(student, BehaviorType.POSITIVE, startDate, endDate);
    }

    /**
     * Counts negative behavior incidents within a date range.
     *
     * @param student the student
     * @param startDate start of date range
     * @param endDate end of date range
     * @return count of negative incidents
     */
    public Long countNegativeIncidents(Student student, LocalDate startDate, LocalDate endDate) {
        return countIncidentsByTypeAndDateRange(student, BehaviorType.NEGATIVE, startDate, endDate);
    }

    /**
     * Calculates behavior ratio (positive to negative) for a student.
     *
     * @param student the student
     * @param startDate start of date range
     * @param endDate end of date range
     * @return ratio (positive/negative), or 0.0 if no negative incidents
     */
    public Double calculateBehaviorRatio(Student student, LocalDate startDate, LocalDate endDate) {
        Long positiveCount = countPositiveIncidents(student, startDate, endDate);
        Long negativeCount = countNegativeIncidents(student, startDate, endDate);

        if (negativeCount == 0) {
            return positiveCount > 0 ? Double.MAX_VALUE : 0.0;
        }

        return (double) positiveCount / negativeCount;
    }

    /**
     * Gets total count of all behavior incidents.
     *
     * @return total count
     */
    public long getTotalIncidentCount() {
        return behaviorIncidentRepository.count();
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Checks if a student has any critical incidents in the past N days.
     *
     * @param student the student
     * @param daysBack number of days to look back
     * @return true if critical incidents found
     */
    public boolean hasRecentCriticalIncidents(Student student, int daysBack) {
        LocalDate sinceDate = LocalDate.now().minusDays(daysBack);
        List<BehaviorIncident> criticalIncidents = getCriticalIncidentsSince(student, sinceDate);
        return !criticalIncidents.isEmpty();
    }

    /**
     * Gets students with uncontacted parent incidents (for notifications/reminders).
     *
     * @return list of student IDs with uncontacted incidents
     */
    public List<Long> getStudentsWithUncontactedIncidents() {
        log.debug("Fetching students with uncontacted parent incidents");

        return studentRepository.findAll().stream()
                .filter(student -> !getUncontactedParentIncidents(student).isEmpty())
                .map(Student::getId)
                .collect(Collectors.toList());
    }
}
