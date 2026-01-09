package com.heronix.controller.api;

import com.heronix.model.domain.BehaviorIncident;
import com.heronix.model.domain.BehaviorIncident.BehaviorCategory;
import com.heronix.model.domain.BehaviorIncident.BehaviorType;
import com.heronix.model.domain.BehaviorIncident.ContactMethod;
import com.heronix.model.domain.BehaviorIncident.SeverityLevel;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.Teacher;
import com.heronix.repository.StudentRepository;
import com.heronix.repository.TeacherRepository;
import com.heronix.service.BehaviorDashboardService;
import com.heronix.service.BehaviorIncidentService;
import com.heronix.service.BehaviorReportingService;
import com.heronix.service.DisciplineManagementService;
import com.heronix.service.DisciplineManagementService.BehaviorPattern;
import com.heronix.service.DisciplineManagementService.DisciplinaryRecommendation;
import com.heronix.service.DisciplineManagementService.InterventionType;
import com.heronix.service.BehaviorReportingService.StudentBehaviorSummary;
import com.heronix.service.BehaviorReportingService.SchoolBehaviorReport;
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
 * REST API Controller for Discipline and Behavior Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/discipline")
@RequiredArgsConstructor
public class DisciplineManagementApiController {

    private final BehaviorIncidentService behaviorIncidentService;
    private final DisciplineManagementService disciplineService;
    private final BehaviorReportingService behaviorReportingService;
    private final BehaviorDashboardService behaviorDashboardService;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    // ==================== Behavior Incident CRUD Operations ====================

    @GetMapping("/incidents")
    public ResponseEntity<List<BehaviorIncident>> getAllIncidents() {
        List<BehaviorIncident> incidents = behaviorIncidentService.getAllIncidents();
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/incidents/{id}")
    public ResponseEntity<BehaviorIncident> getIncidentById(@PathVariable Long id) {
        BehaviorIncident incident = behaviorIncidentService.getIncidentById(id);
        return ResponseEntity.ok(incident);
    }

    @GetMapping("/incidents/student/{studentId}")
    public ResponseEntity<List<BehaviorIncident>> getIncidentsByStudent(@PathVariable Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));
        List<BehaviorIncident> incidents = behaviorIncidentService.getIncidentsByStudent(student);
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/incidents/student/{studentId}/date-range")
    public ResponseEntity<List<BehaviorIncident>> getIncidentsByStudentAndDateRange(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));
        List<BehaviorIncident> incidents = behaviorIncidentService.getIncidentsByStudentAndDateRange(
                student, startDate, endDate);
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/incidents/student/{studentId}/type/{behaviorType}")
    public ResponseEntity<List<BehaviorIncident>> getIncidentsByStudentAndType(
            @PathVariable Long studentId,
            @PathVariable BehaviorType behaviorType) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));
        List<BehaviorIncident> incidents = behaviorIncidentService.getIncidentsByStudentAndType(student, behaviorType);
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/incidents/student/{studentId}/category/{category}")
    public ResponseEntity<List<BehaviorIncident>> getIncidentsByCategory(
            @PathVariable Long studentId,
            @PathVariable BehaviorCategory category) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));
        List<BehaviorIncident> incidents = behaviorIncidentService.getIncidentsByCategory(student, category);
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/incidents/student/{studentId}/positive")
    public ResponseEntity<List<BehaviorIncident>> getPositiveIncidents(@PathVariable Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));
        List<BehaviorIncident> incidents = behaviorIncidentService.getPositiveIncidents(student);
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/incidents/student/{studentId}/negative")
    public ResponseEntity<List<BehaviorIncident>> getNegativeIncidents(@PathVariable Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));
        List<BehaviorIncident> incidents = behaviorIncidentService.getNegativeIncidents(student);
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/incidents/date-range")
    public ResponseEntity<List<BehaviorIncident>> getAllIncidentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<BehaviorIncident> incidents = behaviorIncidentService.getAllIncidentsByDateRange(startDate, endDate);
        return ResponseEntity.ok(incidents);
    }

    @PostMapping("/incidents")
    public ResponseEntity<BehaviorIncident> createIncident(@RequestBody BehaviorIncident incident) {
        BehaviorIncident created = behaviorIncidentService.saveIncident(incident);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/incidents/{id}")
    public ResponseEntity<BehaviorIncident> updateIncident(
            @PathVariable Long id,
            @RequestBody BehaviorIncident incident) {
        incident.setId(id);
        BehaviorIncident updated = behaviorIncidentService.updateIncident(incident);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/incidents/{id}")
    public ResponseEntity<Void> deleteIncident(@PathVariable Long id) {
        behaviorIncidentService.deleteIncident(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Parent Contact Operations ====================

    @GetMapping("/incidents/student/{studentId}/uncontacted-parents")
    public ResponseEntity<List<BehaviorIncident>> getUncontactedParentIncidents(@PathVariable Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));
        List<BehaviorIncident> incidents = behaviorIncidentService.getUncontactedParentIncidents(student);
        return ResponseEntity.ok(incidents);
    }

    @PostMapping("/incidents/{id}/parent-contact")
    public ResponseEntity<BehaviorIncident> recordParentContact(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate contactDate,
            @RequestParam BehaviorIncident.ContactMethod contactMethod) {
        BehaviorIncident incident = behaviorIncidentService.recordParentContact(id, contactDate, contactMethod);
        return ResponseEntity.ok(incident);
    }

    // ==================== Administrative Referral Operations ====================

    @GetMapping("/referrals/pending")
    public ResponseEntity<List<BehaviorIncident>> getPendingReferrals() {
        List<BehaviorIncident> referrals = disciplineService.getPendingReferrals();
        return ResponseEntity.ok(referrals);
    }

    @GetMapping("/referrals/student/{studentId}/pending")
    public ResponseEntity<List<BehaviorIncident>> getPendingReferralsForStudent(@PathVariable Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));
        List<BehaviorIncident> referrals = disciplineService.getPendingReferralsForStudent(student);
        return ResponseEntity.ok(referrals);
    }

    @GetMapping("/incidents/student/{studentId}/requiring-referral")
    public ResponseEntity<List<BehaviorIncident>> getIncidentsRequiringReferral(@PathVariable Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));
        List<BehaviorIncident> incidents = behaviorIncidentService.getIncidentsRequiringReferral(student);
        return ResponseEntity.ok(incidents);
    }

    @PostMapping("/referrals/create")
    public ResponseEntity<BehaviorIncident> createAdminReferral(
            @RequestParam Long incidentId,
            @RequestParam String referralReason,
            @RequestParam Long teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found with id: " + teacherId));
        BehaviorIncident incident = disciplineService.createAdminReferral(incidentId, referralReason, teacher);
        return ResponseEntity.status(HttpStatus.CREATED).body(incident);
    }

    @PostMapping("/referrals/{incidentId}/process")
    public ResponseEntity<BehaviorIncident> processReferral(
            @PathVariable Long incidentId,
            @RequestParam String outcome,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate actionDate) {
        BehaviorIncident incident = disciplineService.processReferral(incidentId, outcome, actionDate);
        return ResponseEntity.ok(incident);
    }

    @PatchMapping("/incidents/{id}/mark-referral-required")
    public ResponseEntity<BehaviorIncident> markAsRequiringReferral(@PathVariable Long id) {
        BehaviorIncident incident = behaviorIncidentService.markAsRequiringReferral(id);
        return ResponseEntity.ok(incident);
    }

    @PatchMapping("/incidents/{id}/referral-outcome")
    public ResponseEntity<BehaviorIncident> recordReferralOutcome(
            @PathVariable Long id,
            @RequestParam String outcome) {
        BehaviorIncident incident = behaviorIncidentService.recordReferralOutcome(id, outcome);
        return ResponseEntity.ok(incident);
    }

    // ==================== Intervention Operations ====================

    @PostMapping("/incidents/{id}/intervention")
    public ResponseEntity<BehaviorIncident> recordInterventionOnIncident(
            @PathVariable Long id,
            @RequestParam String intervention) {
        BehaviorIncident incident = behaviorIncidentService.recordIntervention(id, intervention);
        return ResponseEntity.ok(incident);
    }

    @PostMapping("/interventions/record")
    public ResponseEntity<BehaviorIncident> recordIntervention(
            @RequestParam Long incidentId,
            @RequestParam InterventionType interventionType,
            @RequestParam String interventionDescription) {
        BehaviorIncident incident = disciplineService.recordIntervention(incidentId, interventionType, interventionDescription);
        return ResponseEntity.ok(incident);
    }

    @GetMapping("/interventions/history/student/{studentId}")
    public ResponseEntity<List<BehaviorIncident>> getInterventionHistory(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));
        List<BehaviorIncident> history = disciplineService.getInterventionHistory(student, startDate, endDate);
        return ResponseEntity.ok(history);
    }

    // ==================== Disciplinary Analysis ====================

    @PostMapping("/analysis/disciplinary-action")
    public ResponseEntity<DisciplinaryRecommendation> analyzeDisciplinaryAction(
            @RequestParam Long studentId,
            @RequestParam Long incidentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));
        BehaviorIncident incident = behaviorIncidentService.getIncidentById(incidentId);
        DisciplinaryRecommendation recommendation = disciplineService.analyzeDisciplinaryAction(student, incident);
        return ResponseEntity.ok(recommendation);
    }

    @GetMapping("/analysis/suspension-risk/student/{studentId}")
    public ResponseEntity<Map<String, Boolean>> isAtRiskForSuspension(@PathVariable Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));
        boolean atRisk = disciplineService.isAtRiskForSuspension(student);
        Map<String, Boolean> response = new HashMap<>();
        response.put("atRiskForSuspension", atRisk);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/analysis/behavior-pattern/student/{studentId}")
    public ResponseEntity<BehaviorPattern> detectBehaviorPattern(@PathVariable Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));
        BehaviorPattern pattern = disciplineService.detectBehaviorPattern(student);
        return ResponseEntity.ok(pattern);
    }

    // ==================== Evidence Management ====================

    @PostMapping("/incidents/{id}/attach-evidence")
    public ResponseEntity<BehaviorIncident> attachEvidence(
            @PathVariable Long id,
            @RequestParam String evidenceFilePath) {
        BehaviorIncident incident = behaviorIncidentService.attachEvidence(id, evidenceFilePath);
        return ResponseEntity.ok(incident);
    }

    // ==================== Reporting ====================

    @GetMapping("/reports/student/{studentId}/summary")
    public ResponseEntity<StudentBehaviorSummary> getStudentBehaviorSummary(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));
        StudentBehaviorSummary summary = behaviorReportingService.generateStudentBehaviorSummary(
                student, startDate, endDate);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/reports/school/summary")
    public ResponseEntity<SchoolBehaviorReport> getSchoolBehaviorReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        SchoolBehaviorReport report = behaviorReportingService.generateSchoolBehaviorReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/reports/students/requiring-intervention")
    public ResponseEntity<List<BehaviorReportingService.StudentIncidentCount>> getStudentsRequiringIntervention(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "3") int threshold) {
        List<BehaviorReportingService.StudentIncidentCount> students =
                behaviorReportingService.getStudentsRequiringIntervention(startDate, endDate, threshold);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/reports/students/top-positive")
    public ResponseEntity<List<BehaviorReportingService.StudentIncidentCount>> getTopPositiveBehaviorStudents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "10") int limit) {
        List<BehaviorReportingService.StudentIncidentCount> students =
                behaviorReportingService.getTopPositiveBehaviorStudents(startDate, endDate, limit);
        return ResponseEntity.ok(students);
    }

    // ==================== Statistics ====================

    @GetMapping("/statistics/total-count")
    public ResponseEntity<Long> getTotalIncidentCount() {
        long count = behaviorIncidentService.getTotalIncidentCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/statistics/student/{studentId}/count")
    public ResponseEntity<Map<String, Long>> getStudentIncidentCounts(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));

        Map<String, Long> counts = new HashMap<>();
        counts.put("positive", behaviorIncidentService.countPositiveIncidents(student, startDate, endDate));
        counts.put("negative", behaviorIncidentService.countNegativeIncidents(student, startDate, endDate));

        return ResponseEntity.ok(counts);
    }

    @GetMapping("/statistics/student/{studentId}/behavior-ratio")
    public ResponseEntity<Double> calculateBehaviorRatio(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));
        Double ratio = behaviorIncidentService.calculateBehaviorRatio(student, startDate, endDate);
        return ResponseEntity.ok(ratio);
    }

    @GetMapping("/statistics/category-breakdown/student/{studentId}")
    public ResponseEntity<Map<BehaviorCategory, Long>> getCategoryBreakdown(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));
        Map<BehaviorCategory, Long> breakdown = behaviorReportingService.getCategoryBreakdown(student, startDate, endDate);
        return ResponseEntity.ok(breakdown);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getDashboardOverview(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> dashboard = new HashMap<>();

        SchoolBehaviorReport schoolReport = behaviorReportingService.generateSchoolBehaviorReport(startDate, endDate);
        List<BehaviorIncident> pendingReferrals = disciplineService.getPendingReferrals();
        long totalIncidents = behaviorIncidentService.getTotalIncidentCount();

        dashboard.put("schoolReport", schoolReport);
        dashboard.put("pendingReferrals", pendingReferrals);
        dashboard.put("pendingReferralCount", pendingReferrals.size());
        dashboard.put("totalIncidents", totalIncidents);
        dashboard.put("startDate", startDate);
        dashboard.put("endDate", endDate);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getStudentDashboard(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));

        Map<String, Object> dashboard = new HashMap<>();

        StudentBehaviorSummary summary = behaviorReportingService.generateStudentBehaviorSummary(
                student, startDate, endDate);
        List<BehaviorIncident> recentIncidents = behaviorIncidentService.getIncidentsByStudentAndDateRange(
                student, startDate, endDate);
        List<BehaviorIncident> pendingReferrals = disciplineService.getPendingReferralsForStudent(student);
        List<BehaviorIncident> uncontactedParents = behaviorIncidentService.getUncontactedParentIncidents(student);
        boolean atRisk = disciplineService.isAtRiskForSuspension(student);
        BehaviorPattern pattern = disciplineService.detectBehaviorPattern(student);

        dashboard.put("studentId", studentId);
        dashboard.put("summary", summary);
        dashboard.put("recentIncidents", recentIncidents);
        dashboard.put("pendingReferrals", pendingReferrals);
        dashboard.put("uncontactedParents", uncontactedParents);
        dashboard.put("atRiskForSuspension", atRisk);
        dashboard.put("behaviorPattern", pattern);
        dashboard.put("startDate", startDate);
        dashboard.put("endDate", endDate);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/referrals")
    public ResponseEntity<Map<String, Object>> getReferralsDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        List<BehaviorIncident> pendingReferrals = disciplineService.getPendingReferrals();

        dashboard.put("pendingReferrals", pendingReferrals);
        dashboard.put("pendingCount", pendingReferrals.size());

        return ResponseEntity.ok(dashboard);
    }
}
