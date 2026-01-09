package com.heronix.service;

import com.heronix.model.domain.ELLAssessment;
import com.heronix.model.domain.ELLStudent;
import com.heronix.model.domain.ELLStudent.ELLStatus;
import com.heronix.model.domain.ELLStudent.ProficiencyLevel;
import com.heronix.repository.ELLAssessmentRepository;
import com.heronix.repository.ELLStudentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for ELL/ESL Management
 * Handles ELL identification, program placement, and reclassification
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Service
@Transactional
public class ELLManagementService {

    @Autowired
    private ELLStudentRepository ellStudentRepository;

    @Autowired
    private ELLAssessmentRepository assessmentRepository;

    @Autowired
    private com.heronix.repository.ELLServiceRepository ellServiceRepository;

    @Autowired
    private com.heronix.repository.ELLAccommodationRepository ellAccommodationRepository;

    // CRUD Operations
    public ELLStudent createELLStudent(ELLStudent ellStudent) {
        log.info("Creating ELL record for student {}", ellStudent.getStudent().getId());
        return ellStudentRepository.save(ellStudent);
    }

    public ELLStudent getELLStudentById(Long id) {
        return ellStudentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ELL Student not found: " + id));
    }

    public ELLStudent updateELLStudent(ELLStudent ellStudent) {
        log.info("Updating ELL student {}", ellStudent.getId());
        return ellStudentRepository.save(ellStudent);
    }

    public void deleteELLStudent(Long id) {
        log.info("Deleting ELL student {}", id);
        ellStudentRepository.deleteById(id);
    }

    // Query Operations
    public ELLStudent getByStudentId(Long studentId) {
        return ellStudentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("ELL record not found for student: " + studentId));
    }

    /**
     * Alias for getByStudentId
     */
    public ELLStudent getELLStudentByStudentId(Long studentId) {
        return getByStudentId(studentId);
    }

    public List<ELLStudent> getAllActiveELL() {
        return ellStudentRepository.findAllActive();
    }

    /**
     * Get all ELL students (active and inactive)
     */
    public List<ELLStudent> getAllELLStudents() {
        return ellStudentRepository.findAll();
    }

    public List<ELLStudent> getActiveAndMonitored() {
        return ellStudentRepository.findActiveAndMonitored();
    }

    // Proficiency Level Management
    public ELLStudent updateProficiencyLevel(Long ellStudentId, ProficiencyLevel level,
                                              Integer listening, Integer speaking,
                                              Integer reading, Integer writing) {
        log.info("Updating proficiency level for ELL student {} to {}", ellStudentId, level);
        ELLStudent ellStudent = getELLStudentById(ellStudentId);
        ellStudent.setProficiencyLevel(level);
        ellStudent.setListeningLevel(listening);
        ellStudent.setSpeakingLevel(speaking);
        ellStudent.setReadingLevel(reading);
        ellStudent.setWritingLevel(writing);
        ellStudent.setLastProficiencyAssessmentDate(LocalDate.now());
        return ellStudentRepository.save(ellStudent);
    }

    public List<ELLStudent> getLowProficiencyStudents() {
        return ellStudentRepository.findLowProficiency();
    }

    public List<ELLStudent> getHighProficiencyStudents() {
        return ellStudentRepository.findHighProficiency();
    }

    // Annual Assessment Management
    public List<ELLStudent> getStudentsNeedingAnnualAssessment() {
        return ellStudentRepository.findNeedingAnnualAssessment(LocalDate.now());
    }

    public List<ELLStudent> getUpcomingAnnualAssessments(int daysAhead) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(daysAhead);
        return ellStudentRepository.findUpcomingAnnualAssessments(today, futureDate);
    }

    public ELLStudent scheduleAnnualAssessment(Long ellStudentId, LocalDate assessmentDate) {
        log.info("Scheduling annual assessment for ELL student {}", ellStudentId);
        ELLStudent ellStudent = getELLStudentById(ellStudentId);
        ellStudent.setNextAnnualAssessmentDate(assessmentDate);
        return ellStudentRepository.save(ellStudent);
    }

    // Progress Monitoring
    public List<ELLStudent> getOverdueForProgressMonitoring(int daysWithoutMonitoring) {
        LocalDate cutoffDate = LocalDate.now().minusDays(daysWithoutMonitoring);
        return ellStudentRepository.findOverdueForProgressMonitoring(cutoffDate);
    }

    public ELLStudent recordProgressMonitoring(Long ellStudentId, String notes) {
        log.info("Recording progress monitoring for ELL student {}", ellStudentId);
        ELLStudent ellStudent = getELLStudentById(ellStudentId);
        ellStudent.setLastProgressMonitoringDate(LocalDate.now());
        ellStudent.setNotes(ellStudent.getNotes() != null ?
                ellStudent.getNotes() + "\n\n" + LocalDate.now() + ": " + notes : notes);
        return ellStudentRepository.save(ellStudent);
    }

    // Reclassification
    public ELLStudent markEligibleForReclassification(Long ellStudentId, String reason) {
        log.info("Marking ELL student {} eligible for reclassification", ellStudentId);
        ELLStudent ellStudent = getELLStudentById(ellStudentId);
        ellStudent.setEligibleForReclassification(true);
        ellStudent.setReclassificationEligibilityDate(LocalDate.now());
        ellStudent.setReclassificationReason(reason);
        return ellStudentRepository.save(ellStudent);
    }

    public ELLStudent reclassifyStudent(Long ellStudentId, String reason) {
        log.info("Reclassifying ELL student {}", ellStudentId);
        ELLStudent ellStudent = getELLStudentById(ellStudentId);
        ellStudent.setEllStatus(ELLStatus.RECLASSIFIED);
        ellStudent.setReclassificationDate(LocalDate.now());
        ellStudent.setReclassificationReason(reason);
        ellStudent.setMonitoringPeriodStartDate(LocalDate.now());
        ellStudent.setMonitoringPeriodYears(4);
        ellStudent.setProgramExitDate(LocalDate.now());
        return ellStudentRepository.save(ellStudent);
    }

    public ELLStudent updateMonitoringStatus(Long ellStudentId, ELLStatus monitoredStatus) {
        log.info("Updating monitoring status for ELL student {} to {}", ellStudentId, monitoredStatus);
        ELLStudent ellStudent = getELLStudentById(ellStudentId);
        ellStudent.setEllStatus(monitoredStatus);
        return ellStudentRepository.save(ellStudent);
    }

    public List<ELLStudent> getEligibleForReclassification() {
        return ellStudentRepository.findEligibleForReclassification();
    }

    public List<ELLStudent> getMonitoredStudents() {
        return ellStudentRepository.findMonitored();
    }

    // Parent Communication
    public ELLStudent sendParentNotification(Long ellStudentId, String language) {
        log.info("Recording parent notification for ELL student {}", ellStudentId);
        ELLStudent ellStudent = getELLStudentById(ellStudentId);
        ellStudent.setParentNotificationSent(true);
        ellStudent.setParentNotificationDate(LocalDate.now());
        ellStudent.setParentNotificationLanguage(language);
        return ellStudentRepository.save(ellStudent);
    }

    public List<ELLStudent> getNeedingParentNotification() {
        return ellStudentRepository.findNeedingParentNotification();
    }

    public List<ELLStudent> getNeedingTranslation() {
        return ellStudentRepository.findNeedingTranslation();
    }

    public List<ELLStudent> getNeedingInterpreter() {
        return ellStudentRepository.findNeedingInterpreter();
    }

    // Service Provider Assignment
    public List<ELLStudent> getStudentsByServiceProvider(Long providerId) {
        return ellStudentRepository.findByServiceProvider(providerId);
    }

    // Home Language Survey
    public ELLStudent completeHomeLanguageSurvey(Long ellStudentId, String nativeLanguage,
                                                  String homeLanguage, String parentLanguage) {
        log.info("Completing home language survey for ELL student {}", ellStudentId);
        ELLStudent ellStudent = getELLStudentById(ellStudentId);
        ellStudent.setHomeLanguageSurveyCompleted(true);
        ellStudent.setHomeLanguageSurveyDate(LocalDate.now());
        ellStudent.setNativeLanguage(nativeLanguage);
        ellStudent.setHomeLanguage(homeLanguage);
        ellStudent.setParentLanguage(parentLanguage);
        return ellStudentRepository.save(ellStudent);
    }

    public List<ELLStudent> getNeedingHomeLanguageSurvey() {
        return ellStudentRepository.findNeedingHomeLanguageSurvey();
    }

    // Newcomer Support
    public List<ELLStudent> getNewcomers(int monthsInUS) {
        LocalDate cutoffDate = LocalDate.now().minusMonths(monthsInUS);
        return ellStudentRepository.findNewcomers(cutoffDate);
    }

    // Title III
    public List<ELLStudent> getTitleIIIEligible() {
        return ellStudentRepository.findTitleIIIEligible();
    }

    public List<ELLStudent> getTitleIIIFunded() {
        return ellStudentRepository.findTitleIIIFunded();
    }

    // Statistics and Reporting
    public Long getActiveELLCount() {
        return ellStudentRepository.countActive();
    }

    public Map<String, Long> getCountByStatus() {
        Map<String, Long> counts = new HashMap<>();
        List<Object[]> results = ellStudentRepository.countByStatus();
        for (Object[] result : results) {
            ELLStatus status = (ELLStatus) result[0];
            Long count = (Long) result[1];
            counts.put(status.getDisplayName(), count);
        }
        return counts;
    }

    public Map<String, Long> getCountByProficiencyLevel() {
        Map<String, Long> counts = new HashMap<>();
        List<Object[]> results = ellStudentRepository.countByProficiencyLevel();
        for (Object[] result : results) {
            ProficiencyLevel level = (ProficiencyLevel) result[0];
            Long count = (Long) result[1];
            counts.put(level.getDisplayName(), count);
        }
        return counts;
    }

    public Map<String, Long> getCountByNativeLanguage() {
        Map<String, Long> counts = new HashMap<>();
        List<Object[]> results = ellStudentRepository.countByNativeLanguage();
        for (Object[] result : results) {
            String language = (String) result[0];
            Long count = (Long) result[1];
            counts.put(language, count);
        }
        return counts;
    }

    public Map<String, Object> generateComplianceReport() {
        Map<String, Object> report = new HashMap<>();

        Long activeCount = getActiveELLCount();
        report.put("activeELLCount", activeCount);

        Long monitored = (long) getMonitoredStudents().size();
        report.put("monitoredCount", monitored);

        Long needingAssessment = (long) getStudentsNeedingAnnualAssessment().size();
        report.put("needingAnnualAssessment", needingAssessment);

        Long eligibleReclassification = (long) getEligibleForReclassification().size();
        report.put("eligibleForReclassification", eligibleReclassification);

        Long needingNotification = (long) getNeedingParentNotification().size();
        report.put("needingParentNotification", needingNotification);

        report.put("proficiencyDistribution", getCountByProficiencyLevel());
        report.put("statusDistribution", getCountByStatus());
        report.put("languageDistribution", getCountByNativeLanguage());

        return report;
    }

    // Service and Accommodation management
    public com.heronix.model.domain.ELLService createService(com.heronix.model.domain.ELLService service) {
        log.info("Creating ELL service for student {}", service.getEllStudent().getId());
        return ellServiceRepository.save(service);
    }

    public com.heronix.model.domain.ELLService updateService(com.heronix.model.domain.ELLService service) {
        log.info("Updating ELL service {}", service.getId());
        return ellServiceRepository.save(service);
    }

    public com.heronix.model.domain.ELLAccommodation createAccommodation(com.heronix.model.domain.ELLAccommodation accommodation) {
        log.info("Creating ELL accommodation for student {}", accommodation.getEllStudent().getId());
        return ellAccommodationRepository.save(accommodation);
    }

    public com.heronix.model.domain.ELLAccommodation updateAccommodation(com.heronix.model.domain.ELLAccommodation accommodation) {
        log.info("Updating ELL accommodation {}", accommodation.getId());
        return ellAccommodationRepository.save(accommodation);
    }

    // Assessment management methods
    /**
     * Find all ELL students (alias for getAllELLStudents)
     */
    public List<ELLStudent> findAllELLStudents() {
        return getAllELLStudents();
    }

    /**
     * Find assessments by student
     */
    public List<ELLAssessment> findAssessmentsByStudent(ELLStudent student) {
        if (student == null || student.getId() == null) {
            return List.of();
        }
        return assessmentRepository.findByEllStudent(student);
    }

    /**
     * Create assessment
     */
    public ELLAssessment createAssessment(ELLAssessment assessment) {
        log.info("Creating ELL assessment for student {}", assessment.getEllStudent().getId());
        return assessmentRepository.save(assessment);
    }

    /**
     * Update assessment
     */
    public ELLAssessment updateAssessment(ELLAssessment assessment) {
        log.info("Updating ELL assessment {}", assessment.getId());
        return assessmentRepository.save(assessment);
    }

    /**
     * Delete assessment
     */
    public void deleteAssessment(Long assessmentId) {
        log.info("Deleting ELL assessment {}", assessmentId);
        assessmentRepository.deleteById(assessmentId);
    }

    /**
     * Get all assessments
     */
    public List<ELLAssessment> getAllAssessments() {
        return assessmentRepository.findAll();
    }
}
