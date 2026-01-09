package com.heronix.controller.api;

import com.heronix.model.domain.ELLAssessment;
import com.heronix.model.domain.ELLStudent;
import com.heronix.model.domain.ELLStudent.ELLStatus;
import com.heronix.model.domain.ELLStudent.ProficiencyLevel;
import com.heronix.repository.ELLAssessmentRepository;
import com.heronix.repository.ELLStudentRepository;
import com.heronix.service.ELLManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for ELL (English Language Learner) Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/ell")
@RequiredArgsConstructor
public class ELLManagementApiController {

    private final ELLManagementService ellService;
    private final ELLStudentRepository ellStudentRepository;
    private final ELLAssessmentRepository assessmentRepository;

    // ==================== ELL Student CRUD Operations ====================

    @GetMapping("/students")
    public ResponseEntity<List<ELLStudent>> getAllELLStudents() {
        List<ELLStudent> students = ellService.getAllELLStudents();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<ELLStudent> getELLStudentById(@PathVariable Long id) {
        ELLStudent student = ellService.getELLStudentById(id);
        return ResponseEntity.ok(student);
    }

    @GetMapping("/students/by-student-id/{studentId}")
    public ResponseEntity<ELLStudent> getByStudentId(@PathVariable Long studentId) {
        ELLStudent student = ellService.getByStudentId(studentId);
        return ResponseEntity.ok(student);
    }

    @PostMapping("/students")
    public ResponseEntity<ELLStudent> createELLStudent(@RequestBody ELLStudent ellStudent) {
        ELLStudent created = ellService.createELLStudent(ellStudent);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/students/{id}")
    public ResponseEntity<ELLStudent> updateELLStudent(
            @PathVariable Long id,
            @RequestBody ELLStudent ellStudent) {
        ellStudent.setId(id);
        ELLStudent updated = ellService.updateELLStudent(ellStudent);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<Void> deleteELLStudent(@PathVariable Long id) {
        ellService.deleteELLStudent(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Student Queries ====================

    @GetMapping("/students/active")
    public ResponseEntity<List<ELLStudent>> getAllActiveELL() {
        List<ELLStudent> students = ellService.getAllActiveELL();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/students/active-and-monitored")
    public ResponseEntity<List<ELLStudent>> getActiveAndMonitored() {
        List<ELLStudent> students = ellService.getActiveAndMonitored();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/students/monitored")
    public ResponseEntity<List<ELLStudent>> getMonitoredStudents() {
        List<ELLStudent> students = ellService.getMonitoredStudents();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/students/service-provider/{providerId}")
    public ResponseEntity<List<ELLStudent>> getStudentsByServiceProvider(@PathVariable Long providerId) {
        List<ELLStudent> students = ellService.getStudentsByServiceProvider(providerId);
        return ResponseEntity.ok(students);
    }

    // ==================== Proficiency Level Management ====================

    @PatchMapping("/students/{id}/proficiency-level")
    public ResponseEntity<ELLStudent> updateProficiencyLevel(
            @PathVariable Long id,
            @RequestParam ProficiencyLevel level,
            @RequestParam Integer listening,
            @RequestParam Integer speaking,
            @RequestParam Integer reading,
            @RequestParam Integer writing) {
        ELLStudent updated = ellService.updateProficiencyLevel(id, level, listening, speaking, reading, writing);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/students/proficiency/low")
    public ResponseEntity<List<ELLStudent>> getLowProficiencyStudents() {
        List<ELLStudent> students = ellService.getLowProficiencyStudents();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/students/proficiency/high")
    public ResponseEntity<List<ELLStudent>> getHighProficiencyStudents() {
        List<ELLStudent> students = ellService.getHighProficiencyStudents();
        return ResponseEntity.ok(students);
    }

    // ==================== Annual Assessment Management ====================

    @GetMapping("/students/needing-annual-assessment")
    public ResponseEntity<List<ELLStudent>> getStudentsNeedingAnnualAssessment() {
        List<ELLStudent> students = ellService.getStudentsNeedingAnnualAssessment();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/students/upcoming-annual-assessments")
    public ResponseEntity<List<ELLStudent>> getUpcomingAnnualAssessments(
            @RequestParam(defaultValue = "30") int daysAhead) {
        List<ELLStudent> students = ellService.getUpcomingAnnualAssessments(daysAhead);
        return ResponseEntity.ok(students);
    }

    @PatchMapping("/students/{id}/schedule-annual-assessment")
    public ResponseEntity<ELLStudent> scheduleAnnualAssessment(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate assessmentDate) {
        ELLStudent updated = ellService.scheduleAnnualAssessment(id, assessmentDate);
        return ResponseEntity.ok(updated);
    }

    // ==================== Progress Monitoring ====================

    @GetMapping("/students/overdue-progress-monitoring")
    public ResponseEntity<List<ELLStudent>> getOverdueForProgressMonitoring(
            @RequestParam(defaultValue = "30") int daysWithoutMonitoring) {
        List<ELLStudent> students = ellService.getOverdueForProgressMonitoring(daysWithoutMonitoring);
        return ResponseEntity.ok(students);
    }

    @PostMapping("/students/{id}/progress-monitoring")
    public ResponseEntity<ELLStudent> recordProgressMonitoring(
            @PathVariable Long id,
            @RequestParam String notes) {
        ELLStudent updated = ellService.recordProgressMonitoring(id, notes);
        return ResponseEntity.ok(updated);
    }

    // ==================== Reclassification ====================

    @GetMapping("/students/eligible-for-reclassification")
    public ResponseEntity<List<ELLStudent>> getEligibleForReclassification() {
        List<ELLStudent> students = ellService.getEligibleForReclassification();
        return ResponseEntity.ok(students);
    }

    @PatchMapping("/students/{id}/mark-eligible-reclassification")
    public ResponseEntity<ELLStudent> markEligibleForReclassification(
            @PathVariable Long id,
            @RequestParam String reason) {
        ELLStudent updated = ellService.markEligibleForReclassification(id, reason);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/students/{id}/reclassify")
    public ResponseEntity<ELLStudent> reclassifyStudent(
            @PathVariable Long id,
            @RequestParam String reason) {
        ELLStudent updated = ellService.reclassifyStudent(id, reason);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/students/{id}/monitoring-status")
    public ResponseEntity<ELLStudent> updateMonitoringStatus(
            @PathVariable Long id,
            @RequestParam ELLStatus monitoredStatus) {
        ELLStudent updated = ellService.updateMonitoringStatus(id, monitoredStatus);
        return ResponseEntity.ok(updated);
    }

    // ==================== Parent Communication ====================

    @GetMapping("/students/needing-parent-notification")
    public ResponseEntity<List<ELLStudent>> getNeedingParentNotification() {
        List<ELLStudent> students = ellService.getNeedingParentNotification();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/students/needing-translation")
    public ResponseEntity<List<ELLStudent>> getNeedingTranslation() {
        List<ELLStudent> students = ellService.getNeedingTranslation();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/students/needing-interpreter")
    public ResponseEntity<List<ELLStudent>> getNeedingInterpreter() {
        List<ELLStudent> students = ellService.getNeedingInterpreter();
        return ResponseEntity.ok(students);
    }

    @PostMapping("/students/{id}/parent-notification")
    public ResponseEntity<ELLStudent> sendParentNotification(
            @PathVariable Long id,
            @RequestParam String language) {
        ELLStudent updated = ellService.sendParentNotification(id, language);
        return ResponseEntity.ok(updated);
    }

    // ==================== Home Language Survey ====================

    @GetMapping("/students/needing-home-language-survey")
    public ResponseEntity<List<ELLStudent>> getNeedingHomeLanguageSurvey() {
        List<ELLStudent> students = ellService.getNeedingHomeLanguageSurvey();
        return ResponseEntity.ok(students);
    }

    @PostMapping("/students/{id}/complete-home-language-survey")
    public ResponseEntity<ELLStudent> completeHomeLanguageSurvey(
            @PathVariable Long id,
            @RequestParam String nativeLanguage,
            @RequestParam String homeLanguage,
            @RequestParam String parentLanguage) {
        ELLStudent updated = ellService.completeHomeLanguageSurvey(
                id, nativeLanguage, homeLanguage, parentLanguage);
        return ResponseEntity.ok(updated);
    }

    // ==================== Newcomer Support ====================

    @GetMapping("/students/newcomers")
    public ResponseEntity<List<ELLStudent>> getNewcomers(
            @RequestParam(defaultValue = "12") int monthsInUS) {
        List<ELLStudent> students = ellService.getNewcomers(monthsInUS);
        return ResponseEntity.ok(students);
    }

    // ==================== Title III ====================

    @GetMapping("/students/title-iii-eligible")
    public ResponseEntity<List<ELLStudent>> getTitleIIIEligible() {
        List<ELLStudent> students = ellService.getTitleIIIEligible();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/students/title-iii-funded")
    public ResponseEntity<List<ELLStudent>> getTitleIIIFunded() {
        List<ELLStudent> students = ellService.getTitleIIIFunded();
        return ResponseEntity.ok(students);
    }

    // ==================== Assessment Management ====================

    @GetMapping("/assessments")
    public ResponseEntity<List<ELLAssessment>> getAllAssessments() {
        List<ELLAssessment> assessments = ellService.getAllAssessments();
        return ResponseEntity.ok(assessments);
    }

    @GetMapping("/assessments/{id}")
    public ResponseEntity<ELLAssessment> getAssessmentById(@PathVariable Long id) {
        return assessmentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/students/{ellStudentId}/assessments")
    public ResponseEntity<List<ELLAssessment>> getAssessmentsForStudent(@PathVariable Long ellStudentId) {
        ELLStudent student = ellService.getELLStudentById(ellStudentId);
        List<ELLAssessment> assessments = ellService.findAssessmentsByStudent(student);
        return ResponseEntity.ok(assessments);
    }

    @PostMapping("/assessments")
    public ResponseEntity<ELLAssessment> createAssessment(@RequestBody ELLAssessment assessment) {
        ELLAssessment created = ellService.createAssessment(assessment);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/assessments/{id}")
    public ResponseEntity<ELLAssessment> updateAssessment(
            @PathVariable Long id,
            @RequestBody ELLAssessment assessment) {
        assessment.setId(id);
        ELLAssessment updated = ellService.updateAssessment(assessment);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/assessments/{id}")
    public ResponseEntity<Void> deleteAssessment(@PathVariable Long id) {
        ellService.deleteAssessment(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Statistics and Reporting ====================

    @GetMapping("/statistics/active-count")
    public ResponseEntity<Long> getActiveELLCount() {
        Long count = ellService.getActiveELLCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/statistics/count-by-status")
    public ResponseEntity<Map<String, Long>> getCountByStatus() {
        Map<String, Long> counts = ellService.getCountByStatus();
        return ResponseEntity.ok(counts);
    }

    @GetMapping("/statistics/count-by-proficiency-level")
    public ResponseEntity<Map<String, Long>> getCountByProficiencyLevel() {
        Map<String, Long> counts = ellService.getCountByProficiencyLevel();
        return ResponseEntity.ok(counts);
    }

    @GetMapping("/statistics/count-by-native-language")
    public ResponseEntity<Map<String, Long>> getCountByNativeLanguage() {
        Map<String, Long> counts = ellService.getCountByNativeLanguage();
        return ResponseEntity.ok(counts);
    }

    @GetMapping("/reports/compliance")
    public ResponseEntity<Map<String, Object>> generateComplianceReport() {
        Map<String, Object> report = ellService.generateComplianceReport();
        return ResponseEntity.ok(report);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getDashboardOverview() {
        Map<String, Object> dashboard = new HashMap<>();

        Long activeCount = ellService.getActiveELLCount();
        List<ELLStudent> needingAssessment = ellService.getStudentsNeedingAnnualAssessment();
        List<ELLStudent> eligibleReclassification = ellService.getEligibleForReclassification();
        List<ELLStudent> monitored = ellService.getMonitoredStudents();
        List<ELLStudent> needingNotification = ellService.getNeedingParentNotification();
        List<ELLStudent> newcomers = ellService.getNewcomers(12);

        Map<String, Long> statusCounts = ellService.getCountByStatus();
        Map<String, Long> proficiencyCounts = ellService.getCountByProficiencyLevel();
        Map<String, Long> languageCounts = ellService.getCountByNativeLanguage();

        dashboard.put("activeELLCount", activeCount);
        dashboard.put("needingAnnualAssessment", needingAssessment);
        dashboard.put("needingAssessmentCount", needingAssessment.size());
        dashboard.put("eligibleForReclassification", eligibleReclassification);
        dashboard.put("eligibleReclassificationCount", eligibleReclassification.size());
        dashboard.put("monitoredStudents", monitored);
        dashboard.put("monitoredCount", monitored.size());
        dashboard.put("needingParentNotification", needingNotification);
        dashboard.put("needingNotificationCount", needingNotification.size());
        dashboard.put("newcomers", newcomers);
        dashboard.put("newcomersCount", newcomers.size());
        dashboard.put("statusDistribution", statusCounts);
        dashboard.put("proficiencyDistribution", proficiencyCounts);
        dashboard.put("languageDistribution", languageCounts);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/student/{ellStudentId}")
    public ResponseEntity<Map<String, Object>> getStudentDashboard(@PathVariable Long ellStudentId) {
        ELLStudent student = ellService.getELLStudentById(ellStudentId);
        List<ELLAssessment> assessments = ellService.findAssessmentsByStudent(student);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("ellStudent", student);
        dashboard.put("studentId", student.getStudent() != null ? student.getStudent().getId() : null);
        dashboard.put("studentName", student.getStudent() != null ?
                student.getStudent().getFirstName() + " " + student.getStudent().getLastName() : "Unknown");
        dashboard.put("ellStatus", student.getEllStatus());
        dashboard.put("proficiencyLevel", student.getProficiencyLevel());
        dashboard.put("nativeLanguage", student.getNativeLanguage());
        dashboard.put("homeLanguage", student.getHomeLanguage());
        dashboard.put("assessments", assessments);
        dashboard.put("assessmentCount", assessments.size());
        dashboard.put("eligibleForReclassification", student.getEligibleForReclassification());
        dashboard.put("titleIIIEligible", student.getTitleIIIEligible());
        dashboard.put("titleIIIFunded", student.getTitleIIIFunded());
        dashboard.put("lastProgressMonitoringDate", student.getLastProgressMonitoringDate());
        dashboard.put("nextAnnualAssessmentDate", student.getNextAnnualAssessmentDate());

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/compliance")
    public ResponseEntity<Map<String, Object>> getComplianceDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        List<ELLStudent> needingAssessment = ellService.getStudentsNeedingAnnualAssessment();
        List<ELLStudent> upcomingAssessments = ellService.getUpcomingAnnualAssessments(30);
        List<ELLStudent> overdueMonitoring = ellService.getOverdueForProgressMonitoring(30);
        List<ELLStudent> needingNotification = ellService.getNeedingParentNotification();
        List<ELLStudent> needingSurvey = ellService.getNeedingHomeLanguageSurvey();
        List<ELLStudent> titleIIIEligible = ellService.getTitleIIIEligible();
        List<ELLStudent> titleIIIFunded = ellService.getTitleIIIFunded();

        dashboard.put("needingAnnualAssessment", needingAssessment);
        dashboard.put("needingAssessmentCount", needingAssessment.size());
        dashboard.put("upcomingAssessments", upcomingAssessments);
        dashboard.put("upcomingAssessmentsCount", upcomingAssessments.size());
        dashboard.put("overdueProgressMonitoring", overdueMonitoring);
        dashboard.put("overdueMonitoringCount", overdueMonitoring.size());
        dashboard.put("needingParentNotification", needingNotification);
        dashboard.put("needingNotificationCount", needingNotification.size());
        dashboard.put("needingHomeLanguageSurvey", needingSurvey);
        dashboard.put("needingSurveyCount", needingSurvey.size());
        dashboard.put("titleIIIEligible", titleIIIEligible);
        dashboard.put("titleIIIEligibleCount", titleIIIEligible.size());
        dashboard.put("titleIIIFunded", titleIIIFunded);
        dashboard.put("titleIIIFundedCount", titleIIIFunded.size());

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/reclassification")
    public ResponseEntity<Map<String, Object>> getReclassificationDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        List<ELLStudent> eligible = ellService.getEligibleForReclassification();
        List<ELLStudent> monitored = ellService.getMonitoredStudents();
        List<ELLStudent> highProficiency = ellService.getHighProficiencyStudents();

        dashboard.put("eligibleForReclassification", eligible);
        dashboard.put("eligibleCount", eligible.size());
        dashboard.put("monitoredStudents", monitored);
        dashboard.put("monitoredCount", monitored.size());
        dashboard.put("highProficiencyStudents", highProficiency);
        dashboard.put("highProficiencyCount", highProficiency.size());

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/language-support")
    public ResponseEntity<Map<String, Object>> getLanguageSupportDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        List<ELLStudent> needingTranslation = ellService.getNeedingTranslation();
        List<ELLStudent> needingInterpreter = ellService.getNeedingInterpreter();
        List<ELLStudent> newcomers = ellService.getNewcomers(12);
        Map<String, Long> languageCounts = ellService.getCountByNativeLanguage();

        dashboard.put("needingTranslation", needingTranslation);
        dashboard.put("needingTranslationCount", needingTranslation.size());
        dashboard.put("needingInterpreter", needingInterpreter);
        dashboard.put("needingInterpreterCount", needingInterpreter.size());
        dashboard.put("newcomers", newcomers);
        dashboard.put("newcomersCount", newcomers.size());
        dashboard.put("languageDistribution", languageCounts);

        return ResponseEntity.ok(dashboard);
    }
}
