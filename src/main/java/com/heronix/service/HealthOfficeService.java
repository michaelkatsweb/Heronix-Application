package com.heronix.service;

import com.heronix.model.domain.HealthRecord;
import com.heronix.model.domain.HealthScreening;
import com.heronix.model.domain.NurseVisit;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.Teacher;
import com.heronix.model.domain.NurseVisit.VisitReason;
import com.heronix.model.domain.NurseVisit.Disposition;
import com.heronix.model.domain.NurseVisit.ContactMethod;
import com.heronix.model.domain.HealthRecord.ScreeningResult;
import com.heronix.repository.HealthRecordRepository;
import com.heronix.repository.NurseVisitRepository;
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

/**
 * Service for managing health office operations including health records and nurse visits.
 *
 * Handles:
 * - Health record CRUD operations
 * - Nurse visit tracking and management
 * - Health screenings coordination
 * - Emergency contact management
 * - Health alerts and notifications
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Health Office Management System
 */
@Service
public class HealthOfficeService {

    private static final Logger log = LoggerFactory.getLogger(HealthOfficeService.class);

    @Autowired
    private HealthRecordRepository healthRecordRepository;

    @Autowired
    private NurseVisitRepository nurseVisitRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private com.heronix.repository.HealthScreeningRepository healthScreeningRepository;

    // ========================================================================
    // HEALTH RECORD OPERATIONS
    // ========================================================================

    /**
     * Creates a new health record for a student.
     *
     * @param student the student
     * @param emergencyContactName emergency contact name
     * @param emergencyContactRelationship emergency contact relationship
     * @param emergencyContactPhone emergency contact phone
     * @param createdByStaffId staff member creating the record
     * @return created health record
     */
    @Transactional
    public HealthRecord createHealthRecord(
            Student student,
            String emergencyContactName,
            String emergencyContactRelationship,
            String emergencyContactPhone,
            Long createdByStaffId) {

        log.info("Creating health record for student ID {}", student.getId());

        // Check if health record already exists
        Optional<HealthRecord> existing = healthRecordRepository.findByStudent(student);
        if (existing.isPresent()) {
            throw new IllegalStateException("Health record already exists for student ID " + student.getId());
        }

        HealthRecord record = HealthRecord.builder()
                .student(student)
                .emergencyContactName(emergencyContactName)
                .emergencyContactRelationship(emergencyContactRelationship)
                .emergencyContactPhone(emergencyContactPhone)
                .updatedByStaffId(createdByStaffId)
                .lastUpdated(LocalDateTime.now())
                .recordComplete(false)
                .build();

        record = healthRecordRepository.save(record);
        log.info("Created health record ID {} for student: {} {}",
                record.getId(), student.getFirstName(), student.getLastName());

        return record;
    }

    /**
     * Gets health record for a student.
     *
     * @param student the student
     * @return health record
     */
    public HealthRecord getHealthRecord(Student student) {
        return healthRecordRepository.findByStudent(student)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No health record found for student ID " + student.getId()));
    }

    /**
     * Gets health record by ID.
     *
     * @param id the health record ID
     * @return health record
     */
    public HealthRecord getHealthRecordById(Long id) {
        return healthRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Health record not found: " + id));
    }

    /**
     * Updates a health record.
     *
     * @param healthRecord the health record to update
     * @param updatedByStaffId staff member updating the record
     * @return updated health record
     */
    @Transactional
    public HealthRecord updateHealthRecord(HealthRecord healthRecord, Long updatedByStaffId) {
        log.info("Updating health record ID {}", healthRecord.getId());

        healthRecord.setLastUpdated(LocalDateTime.now());
        healthRecord.setUpdatedByStaffId(updatedByStaffId);

        healthRecord = healthRecordRepository.save(healthRecord);
        log.info("Updated health record ID {}", healthRecord.getId());

        return healthRecord;
    }

    /**
     * Marks a health record as complete.
     *
     * @param healthRecordId the health record ID
     * @param staffId staff member marking as complete
     * @return updated health record
     */
    @Transactional
    public HealthRecord markHealthRecordComplete(Long healthRecordId, Long staffId) {
        log.info("Marking health record ID {} as complete", healthRecordId);

        HealthRecord record = getHealthRecordById(healthRecordId);
        record.setRecordComplete(true);
        record.setLastUpdated(LocalDateTime.now());
        record.setUpdatedByStaffId(staffId);

        record = healthRecordRepository.save(record);
        log.info("Health record ID {} marked as complete", healthRecordId);

        return record;
    }

    /**
     * Records vision screening results.
     *
     * @param healthRecordId the health record ID
     * @param screeningDate date of screening
     * @param result screening result
     * @param staffId staff member recording results
     * @return updated health record
     */
    @Transactional
    public HealthRecord recordVisionScreening(
            Long healthRecordId,
            LocalDate screeningDate,
            ScreeningResult result,
            Long staffId) {

        log.info("Recording vision screening for health record ID {}", healthRecordId);

        HealthRecord record = getHealthRecordById(healthRecordId);
        record.setLastVisionScreeningDate(screeningDate);
        record.setLastVisionScreeningResult(result);
        record.setLastUpdated(LocalDateTime.now());
        record.setUpdatedByStaffId(staffId);

        record = healthRecordRepository.save(record);
        log.info("Vision screening recorded: {} - {}", screeningDate, result);

        return record;
    }

    /**
     * Records hearing screening results.
     *
     * @param healthRecordId the health record ID
     * @param screeningDate date of screening
     * @param result screening result
     * @param staffId staff member recording results
     * @return updated health record
     */
    @Transactional
    public HealthRecord recordHearingScreening(
            Long healthRecordId,
            LocalDate screeningDate,
            ScreeningResult result,
            Long staffId) {

        log.info("Recording hearing screening for health record ID {}", healthRecordId);

        HealthRecord record = getHealthRecordById(healthRecordId);
        record.setLastHearingScreeningDate(screeningDate);
        record.setLastHearingScreeningResult(result);
        record.setLastUpdated(LocalDateTime.now());
        record.setUpdatedByStaffId(staffId);

        record = healthRecordRepository.save(record);
        log.info("Hearing screening recorded: {} - {}", screeningDate, result);

        return record;
    }

    /**
     * Gets students with high-risk health conditions.
     *
     * @return list of health records
     */
    public List<HealthRecord> getHighRiskStudents() {
        log.debug("Fetching high-risk students");

        return healthRecordRepository.findAll().stream()
                .filter(HealthRecord::hasHighRiskCondition)
                .toList();
    }

    /**
     * Gets students needing vision screenings.
     *
     * @return list of health records
     */
    public List<HealthRecord> getStudentsNeedingVisionScreening() {
        log.debug("Fetching students needing vision screening");
        return healthRecordRepository.findStudentsNeedingVisionScreening();
    }

    /**
     * Gets students needing hearing screenings.
     *
     * @return list of health records
     */
    public List<HealthRecord> getStudentsNeedingHearingScreening() {
        log.debug("Fetching students needing hearing screening");
        return healthRecordRepository.findStudentsNeedingHearingScreening();
    }

    /**
     * Gets incomplete health records.
     *
     * @return list of incomplete health records
     */
    public List<HealthRecord> getIncompleteHealthRecords() {
        log.debug("Fetching incomplete health records");
        return healthRecordRepository.findByRecordComplete(false);
    }

    // ========================================================================
    // NURSE VISIT OPERATIONS
    // ========================================================================

    /**
     * Checks in a student for a nurse visit.
     *
     * @param student the student
     * @param visitReason reason for visit
     * @param chiefComplaint primary symptom
     * @param attendingNurseId nurse staff ID
     * @return created nurse visit
     */
    @Transactional
    public NurseVisit checkInStudent(
            Student student,
            VisitReason visitReason,
            String chiefComplaint,
            Long attendingNurseId) {

        log.info("Checking in student ID {} to nurse office", student.getId());

        NurseVisit visit = NurseVisit.builder()
                .student(student)
                .visitDate(LocalDate.now())
                .visitTime(LocalTime.now())
                .checkInTime(LocalDateTime.now())
                .visitReason(visitReason)
                .chiefComplaint(chiefComplaint)
                .attendingNurseId(attendingNurseId)
                .build();

        visit = nurseVisitRepository.save(visit);
        log.info("Created nurse visit ID {} for student: {} {}",
                visit.getId(), student.getFirstName(), student.getLastName());

        return visit;
    }

    /**
     * Checks out a student from nurse office.
     *
     * @param visitId the visit ID
     * @param disposition how the visit ended
     * @return updated nurse visit
     */
    @Transactional
    public NurseVisit checkOutStudent(Long visitId, Disposition disposition) {
        log.info("Checking out student from nurse visit ID {}", visitId);

        NurseVisit visit = getNurseVisitById(visitId);
        visit.setCheckOutTime(LocalDateTime.now());
        visit.setDisposition(disposition);
        visit.setDispositionTime(LocalTime.now());

        visit = nurseVisitRepository.save(visit);
        log.info("Student checked out from visit ID {}: {}", visitId, disposition);

        return visit;
    }

    /**
     * Records temperature for a nurse visit.
     *
     * @param visitId the visit ID
     * @param temperature temperature in Fahrenheit
     * @return updated nurse visit
     */
    @Transactional
    public NurseVisit recordTemperature(Long visitId, Double temperature) {
        log.info("Recording temperature {} for visit ID {}", temperature, visitId);

        NurseVisit visit = getNurseVisitById(visitId);
        visit.setTemperature(temperature);
        visit.setHasFever(temperature >= 100.4);

        visit = nurseVisitRepository.save(visit);
        return visit;
    }

    /**
     * Records parent notification for a visit.
     *
     * @param visitId the visit ID
     * @param contactMethod how parent was contacted
     * @param notes notification notes
     * @return updated nurse visit
     */
    @Transactional
    public NurseVisit recordParentNotification(
            Long visitId,
            ContactMethod contactMethod,
            String notes) {

        log.info("Recording parent notification for visit ID {}", visitId);

        NurseVisit visit = getNurseVisitById(visitId);
        visit.setParentNotified(true);
        visit.setParentNotificationTime(LocalTime.now());
        visit.setParentNotificationMethod(contactMethod);
        visit.setParentNotificationNotes(notes);

        visit = nurseVisitRepository.save(visit);
        log.info("Parent notification recorded for visit ID {}", visitId);

        return visit;
    }

    /**
     * Marks a student as sent home.
     *
     * @param visitId the visit ID
     * @param reason reason for sending home
     * @return updated nurse visit
     */
    @Transactional
    public NurseVisit sendStudentHome(Long visitId, String reason) {
        log.info("Sending student home from visit ID {}", visitId);

        NurseVisit visit = getNurseVisitById(visitId);
        visit.setSentHome(true);
        visit.setSentHomeReason(reason);
        visit.setDisposition(Disposition.SENT_HOME);

        visit = nurseVisitRepository.save(visit);
        log.info("Student sent home from visit ID {}", visitId);

        return visit;
    }

    /**
     * Gets a nurse visit by ID.
     *
     * @param id the visit ID
     * @return nurse visit
     */
    public NurseVisit getNurseVisitById(Long id) {
        return nurseVisitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nurse visit not found: " + id));
    }

    /**
     * Gets all nurse visits for a student.
     *
     * @param student the student
     * @return list of nurse visits
     */
    public List<NurseVisit> getNurseVisitsForStudent(Student student) {
        log.debug("Fetching nurse visits for student ID {}", student.getId());
        return nurseVisitRepository.findByStudent(student);
    }

    /**
     * Gets nurse visits within a date range.
     *
     * @param startDate start of date range
     * @param endDate end of date range
     * @return list of nurse visits
     */
    public List<NurseVisit> getNurseVisitsByDateRange(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching nurse visits from {} to {}", startDate, endDate);
        return nurseVisitRepository.findByVisitDateBetween(startDate, endDate);
    }

    /**
     * Gets active nurse visits (students currently in nurse office).
     *
     * @return list of active visits
     */
    public List<NurseVisit> getActiveVisits() {
        log.debug("Fetching active nurse visits");
        return nurseVisitRepository.findActiveVisits();
    }

    /**
     * Gets visits requiring parent notification.
     *
     * @return list of visits needing parent contact
     */
    public List<NurseVisit> getVisitsRequiringParentNotification() {
        log.debug("Fetching visits requiring parent notification");
        return nurseVisitRepository.findVisitsRequiringParentNotification();
    }

    /**
     * Gets students sent home today.
     *
     * @return list of visits where students were sent home
     */
    public List<NurseVisit> getStudentsSentHomeToday() {
        log.debug("Fetching students sent home today");
        return nurseVisitRepository.findBySentHome(true).stream()
                .filter(v -> v.getVisitDate().equals(LocalDate.now()))
                .toList();
    }

    /**
     * Gets frequent nurse office visitors.
     *
     * @param startDate start of date range
     * @param endDate end of date range
     * @param minimumVisits minimum number of visits to be considered frequent
     * @return list of student and visit count pairs
     */
    public List<Object[]> getFrequentVisitors(
            LocalDate startDate,
            LocalDate endDate,
            Long minimumVisits) {

        log.debug("Fetching frequent visitors from {} to {} (min {} visits)",
                startDate, endDate, minimumVisits);
        return nurseVisitRepository.findFrequentVisitors(startDate, endDate, minimumVisits);
    }

    /**
     * Updates a nurse visit.
     *
     * @param nurseVisit the visit to update
     * @return updated nurse visit
     */
    @Transactional
    public NurseVisit updateNurseVisit(NurseVisit nurseVisit) {
        log.info("Updating nurse visit ID {}", nurseVisit.getId());
        return nurseVisitRepository.save(nurseVisit);
    }

    /**
     * Gets total count of health records.
     *
     * @return count
     */
    public long getHealthRecordCount() {
        return healthRecordRepository.count();
    }

    /**
     * Gets total count of nurse visits.
     *
     * @return count
     */
    public long getNurseVisitCount() {
        return nurseVisitRepository.count();
    }

    // ========================================================================
    // HEALTH SCREENING OPERATIONS
    // ========================================================================

    /**
     * Creates a new health screening record.
     *
     * @param screening the health screening to create
     * @return created health screening
     */
    @Transactional
    public HealthScreening createHealthScreening(HealthScreening screening) {
        log.info("Creating health screening for student ID {}: {}",
                screening.getStudent().getId(), screening.getScreeningType());

        HealthScreening saved = healthScreeningRepository.save(screening);
        log.info("Created health screening ID {} for student: {} {}",
                saved.getId(), screening.getStudent().getFirstName(), screening.getStudent().getLastName());

        return saved;
    }

    /**
     * Updates a health screening record.
     *
     * @param screening the health screening to update
     * @return updated health screening
     */
    @Transactional
    public HealthScreening updateHealthScreening(HealthScreening screening) {
        log.info("Updating health screening ID {}", screening.getId());

        HealthScreening updated = healthScreeningRepository.save(screening);
        log.info("Updated health screening ID {}", screening.getId());

        return updated;
    }
}
