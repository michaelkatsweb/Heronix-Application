package com.heronix.controller.api;

import com.heronix.model.domain.*;
import com.heronix.repository.*;
import com.heronix.service.GiftedManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST API Controller for Gifted and Talented Program Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/gifted")
@RequiredArgsConstructor
public class GiftedManagementApiController {

    private final GiftedManagementService giftedService;
    private final GiftedStudentRepository giftedStudentRepository;
    private final GiftedAssessmentRepository assessmentRepository;
    private final GiftedEducationPlanRepository planRepository;
    private final GiftedServiceRepository serviceRepository;

    // ==================== Gifted Student Management ====================

    @GetMapping("/students")
    public ResponseEntity<List<GiftedStudent>> getActiveGiftedStudents() {
        List<GiftedStudent> students = giftedService.getActiveGiftedStudents();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<GiftedStudent> getGiftedStudentById(@PathVariable Long id) {
        return giftedStudentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/students/by-student-id/{studentId}")
    public ResponseEntity<GiftedStudent> getGiftedStudentByStudentId(@PathVariable Long studentId) {
        return giftedService.getGiftedStudentByStudentId(studentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/students/status/{status}")
    public ResponseEntity<List<GiftedStudent>> getGiftedStudentsByStatus(
            @PathVariable GiftedStudent.GiftedStatus status) {
        List<GiftedStudent> students = giftedService.getGiftedStudentsByStatus(status);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/students/area/{area}")
    public ResponseEntity<List<GiftedStudent>> getGiftedStudentsByArea(
            @PathVariable GiftedStudent.GiftedArea area) {
        List<GiftedStudent> students = giftedService.getGiftedStudentsByArea(area);
        return ResponseEntity.ok(students);
    }

    @PostMapping("/students")
    public ResponseEntity<GiftedStudent> createGiftedStudent(@RequestBody GiftedStudent giftedStudent) {
        GiftedStudent created = giftedService.createGiftedStudent(giftedStudent);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/students/{id}")
    public ResponseEntity<GiftedStudent> updateGiftedStudent(
            @PathVariable Long id,
            @RequestBody GiftedStudent giftedStudent) {
        giftedStudent.setId(id);
        GiftedStudent updated = giftedService.updateGiftedStudent(giftedStudent);
        return ResponseEntity.ok(updated);
    }

    // ==================== Screening and Identification ====================

    @GetMapping("/students/awaiting-screening")
    public ResponseEntity<List<GiftedStudent>> getStudentsAwaitingScreening() {
        List<GiftedStudent> students = giftedService.getStudentsAwaitingScreening();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/students/in-assessment")
    public ResponseEntity<List<GiftedStudent>> getStudentsInAssessment() {
        List<GiftedStudent> students = giftedService.getStudentsInAssessment();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/students/eligible")
    public ResponseEntity<List<GiftedStudent>> getEligibleStudents() {
        List<GiftedStudent> students = giftedService.getEligibleStudents();
        return ResponseEntity.ok(students);
    }

    // ==================== Assessment Management ====================

    @GetMapping("/assessments")
    public ResponseEntity<List<GiftedAssessment>> getAllAssessments() {
        List<GiftedAssessment> assessments = assessmentRepository.findAll();
        return ResponseEntity.ok(assessments);
    }

    @GetMapping("/assessments/{id}")
    public ResponseEntity<GiftedAssessment> getAssessmentById(@PathVariable Long id) {
        return assessmentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/assessments/student/{giftedStudentId}")
    public ResponseEntity<List<GiftedAssessment>> getAssessmentsByStudent(@PathVariable Long giftedStudentId) {
        List<GiftedAssessment> assessments = giftedService.getAssessmentsByStudent(giftedStudentId);
        return ResponseEntity.ok(assessments);
    }

    @GetMapping("/assessments/highly-gifted")
    public ResponseEntity<List<GiftedAssessment>> getHighlyGiftedAssessments() {
        List<GiftedAssessment> assessments = giftedService.getHighlyGiftedAssessments();
        return ResponseEntity.ok(assessments);
    }

    @GetMapping("/assessments/exceptionally-gifted")
    public ResponseEntity<List<GiftedAssessment>> getExceptionallyGiftedAssessments() {
        List<GiftedAssessment> assessments = giftedService.getExceptionallyGiftedAssessments();
        return ResponseEntity.ok(assessments);
    }

    @GetMapping("/assessments/pending-results")
    public ResponseEntity<List<GiftedAssessment>> getAssessmentsPendingResults() {
        List<GiftedAssessment> assessments = giftedService.getAssessmentsPendingResults();
        return ResponseEntity.ok(assessments);
    }

    @GetMapping("/assessments/needing-parent-notification")
    public ResponseEntity<List<GiftedAssessment>> getAssessmentsNeedingParentNotification() {
        List<GiftedAssessment> assessments = giftedService.getAssessmentsNeedingParentNotification();
        return ResponseEntity.ok(assessments);
    }

    @PostMapping("/assessments")
    public ResponseEntity<GiftedAssessment> createAssessment(@RequestBody GiftedAssessment assessment) {
        GiftedAssessment created = giftedService.createAssessment(assessment);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/assessments/{id}")
    public ResponseEntity<GiftedAssessment> updateAssessment(
            @PathVariable Long id,
            @RequestBody GiftedAssessment assessment) {
        assessment.setId(id);
        GiftedAssessment updated = giftedService.updateAssessment(assessment);
        return ResponseEntity.ok(updated);
    }

    // ==================== GEP (Gifted Education Plan) Management ====================

    @GetMapping("/plans")
    public ResponseEntity<List<GiftedEducationPlan>> getActivePlans() {
        List<GiftedEducationPlan> plans = giftedService.getActivePlans();
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/plans/{id}")
    public ResponseEntity<GiftedEducationPlan> getPlanById(@PathVariable Long id) {
        return planRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/plans/student/{giftedStudentId}")
    public ResponseEntity<List<GiftedEducationPlan>> getPlansByStudent(@PathVariable Long giftedStudentId) {
        List<GiftedEducationPlan> plans = giftedService.getPlansByStudent(giftedStudentId);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/plans/number/{planNumber}")
    public ResponseEntity<GiftedEducationPlan> getPlanByNumber(@PathVariable String planNumber) {
        return giftedService.getPlanByNumber(planNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/plans/due-for-review")
    public ResponseEntity<List<GiftedEducationPlan>> getPlansDueForReview() {
        List<GiftedEducationPlan> plans = giftedService.getPlansDueForReview();
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/plans/expiring-soon")
    public ResponseEntity<List<GiftedEducationPlan>> getPlansExpiringSoon(
            @RequestParam(defaultValue = "30") int daysAhead) {
        List<GiftedEducationPlan> plans = giftedService.getPlansExpiringSoon(daysAhead);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/plans/needing-parent-consent")
    public ResponseEntity<List<GiftedEducationPlan>> getPlansNeedingParentConsent() {
        List<GiftedEducationPlan> plans = giftedService.getPlansNeedingParentConsent();
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/plans/case-manager/{caseManagerId}")
    public ResponseEntity<List<GiftedEducationPlan>> getPlansByCaseManager(@PathVariable Long caseManagerId) {
        List<GiftedEducationPlan> plans = giftedService.getPlansByCaseManager(caseManagerId);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/plans/overdue-progress-review")
    public ResponseEntity<List<GiftedEducationPlan>> getPlansOverdueForProgressReview(
            @RequestParam(defaultValue = "90") int daysOverdue) {
        List<GiftedEducationPlan> plans = giftedService.getPlansOverdueForProgressReview(daysOverdue);
        return ResponseEntity.ok(plans);
    }

    // ==================== Service Delivery ====================

    @GetMapping("/services")
    public ResponseEntity<List<GiftedService>> getAllServices() {
        List<GiftedService> services = serviceRepository.findAll();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/services/{id}")
    public ResponseEntity<GiftedService> getServiceById(@PathVariable Long id) {
        return serviceRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/services/student/{giftedStudentId}")
    public ResponseEntity<List<GiftedService>> getServicesByStudent(@PathVariable Long giftedStudentId) {
        List<GiftedService> services = giftedService.getServicesByStudent(giftedStudentId);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/services/upcoming")
    public ResponseEntity<List<GiftedService>> getUpcomingServices() {
        List<GiftedService> services = giftedService.getUpcomingServices();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/services/needing-documentation")
    public ResponseEntity<List<GiftedService>> getServicesNeedingDocumentation() {
        List<GiftedService> services = giftedService.getServicesNeedingDocumentation();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/services/provider/{providerId}")
    public ResponseEntity<List<GiftedService>> getServicesByProvider(@PathVariable Long providerId) {
        List<GiftedService> services = giftedService.getServicesByProvider(providerId);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/services/needing-followup")
    public ResponseEntity<List<GiftedService>> getServicesNeedingFollowUp() {
        List<GiftedService> services = giftedService.getServicesNeedingFollowUp();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/services/high-engagement")
    public ResponseEntity<List<GiftedService>> getHighEngagementServices() {
        List<GiftedService> services = giftedService.getHighEngagementServices();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/services/low-engagement")
    public ResponseEntity<List<GiftedService>> getLowEngagementServices() {
        List<GiftedService> services = giftedService.getLowEngagementServices();
        return ResponseEntity.ok(services);
    }

    @PostMapping("/services")
    public ResponseEntity<GiftedService> createService(@RequestBody GiftedService service) {
        GiftedService created = giftedService.createService(service);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/services/{id}")
    public ResponseEntity<GiftedService> updateService(
            @PathVariable Long id,
            @RequestBody GiftedService service) {
        service.setId(id);
        GiftedService updated = giftedService.updateService(service);
        return ResponseEntity.ok(updated);
    }

    // ==================== Service Minutes Tracking ====================

    @GetMapping("/services/minutes/student/{giftedStudentId}")
    public ResponseEntity<Long> calculateServiceMinutes(
            @PathVariable Long giftedStudentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long minutes = giftedService.calculateServiceMinutes(giftedStudentId, startDate, endDate);
        return ResponseEntity.ok(minutes);
    }

    @GetMapping("/services/minutes/all-students")
    public ResponseEntity<Map<Long, Long>> calculateServiceMinutesByStudent(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<Long, Long> minutesByStudent = giftedService.calculateServiceMinutesByStudent(startDate, endDate);
        return ResponseEntity.ok(minutesByStudent);
    }

    // ==================== Progress Monitoring and Reviews ====================

    @GetMapping("/students/needing-progress-review")
    public ResponseEntity<List<GiftedStudent>> getStudentsNeedingProgressReview(
            @RequestParam(defaultValue = "90") int daysOverdue) {
        List<GiftedStudent> students = giftedService.getStudentsNeedingProgressReview(daysOverdue);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/students/needing-annual-review")
    public ResponseEntity<List<GiftedStudent>> getStudentsNeedingAnnualReview() {
        List<GiftedStudent> students = giftedService.getStudentsNeedingAnnualReview();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/students/upcoming-annual-review")
    public ResponseEntity<List<GiftedStudent>> getStudentsWithUpcomingAnnualReview(
            @RequestParam(defaultValue = "30") int daysAhead) {
        List<GiftedStudent> students = giftedService.getStudentsWithUpcomingAnnualReview(daysAhead);
        return ResponseEntity.ok(students);
    }

    // ==================== Parent Communication ====================

    @GetMapping("/students/needing-parent-notification")
    public ResponseEntity<List<GiftedStudent>> getStudentsNeedingParentNotification() {
        List<GiftedStudent> students = giftedService.getStudentsNeedingParentNotification();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/students/needing-parent-consent")
    public ResponseEntity<List<GiftedStudent>> getStudentsNeedingParentConsent() {
        List<GiftedStudent> students = giftedService.getStudentsNeedingParentConsent();
        return ResponseEntity.ok(students);
    }

    // ==================== Performance Tracking ====================

    @GetMapping("/students/underperforming")
    public ResponseEntity<List<GiftedStudent>> getUnderperformingStudents() {
        List<GiftedStudent> students = giftedService.getUnderperformingStudents();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/students/below-gpa")
    public ResponseEntity<List<GiftedStudent>> getStudentsBelowGPA(@RequestParam Double minGpa) {
        List<GiftedStudent> students = giftedService.getStudentsBelowGPA(minGpa);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/students/with-concerns")
    public ResponseEntity<List<GiftedStudent>> getStudentsWithConcerns() {
        List<GiftedStudent> students = giftedService.getStudentsWithConcerns();
        return ResponseEntity.ok(students);
    }

    // ==================== Advanced Coursework ====================

    @GetMapping("/students/ap-courses")
    public ResponseEntity<List<GiftedStudent>> getStudentsInAPCourses() {
        List<GiftedStudent> students = giftedService.getStudentsInAPCourses();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/students/honors-courses")
    public ResponseEntity<List<GiftedStudent>> getStudentsInHonorsCourses() {
        List<GiftedStudent> students = giftedService.getStudentsInHonorsCourses();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/students/dual-enrollment")
    public ResponseEntity<List<GiftedStudent>> getDualEnrollmentStudents() {
        List<GiftedStudent> students = giftedService.getDualEnrollmentStudents();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/students/grade-accelerated")
    public ResponseEntity<List<GiftedStudent>> getGradeAcceleratedStudents() {
        List<GiftedStudent> students = giftedService.getGradeAcceleratedStudents();
        return ResponseEntity.ok(students);
    }

    // ==================== Talent Development ====================

    @GetMapping("/students/active-talent-plan")
    public ResponseEntity<List<GiftedStudent>> getStudentsWithActiveTalentPlan() {
        List<GiftedStudent> students = giftedService.getStudentsWithActiveTalentPlan();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/students/mentorship-program")
    public ResponseEntity<List<GiftedStudent>> getStudentsInMentorshipProgram() {
        List<GiftedStudent> students = giftedService.getStudentsInMentorshipProgram();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/students/multi-talented")
    public ResponseEntity<List<GiftedStudent>> getMultiTalentedStudents() {
        List<GiftedStudent> students = giftedService.getMultiTalentedStudents();
        return ResponseEntity.ok(students);
    }

    // ==================== Cluster Grouping ====================

    @GetMapping("/students/cluster-grouped")
    public ResponseEntity<List<GiftedStudent>> getClusterGroupedStudents() {
        List<GiftedStudent> students = giftedService.getClusterGroupedStudents();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/students/cluster-group/{groupName}")
    public ResponseEntity<List<GiftedStudent>> getStudentsByClusterGroup(@PathVariable String groupName) {
        List<GiftedStudent> students = giftedService.getStudentsByClusterGroup(groupName);
        return ResponseEntity.ok(students);
    }

    // ==================== Statistics and Reporting ====================

    @GetMapping("/statistics/program")
    public ResponseEntity<Map<String, Object>> getGiftedProgramStatistics() {
        Map<String, Object> stats = giftedService.getGiftedProgramStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/statistics/assessments")
    public ResponseEntity<Map<String, Object>> getAssessmentStatistics() {
        Map<String, Object> stats = giftedService.getAssessmentStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/statistics/services")
    public ResponseEntity<Map<String, Object>> getServiceStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> stats = giftedService.getServiceStatistics(startDate, endDate);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/compliance/alerts")
    public ResponseEntity<Map<String, List<?>>> getComplianceAlerts() {
        Map<String, List<?>> alerts = giftedService.getComplianceAlerts();
        return ResponseEntity.ok(alerts);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getDashboardOverview() {
        Map<String, Object> dashboard = new HashMap<>();

        Map<String, Object> programStats = giftedService.getGiftedProgramStatistics();
        List<GiftedStudent> awaitingScreening = giftedService.getStudentsAwaitingScreening();
        List<GiftedStudent> needingAnnualReview = giftedService.getStudentsNeedingAnnualReview();
        List<GiftedEducationPlan> plansDueReview = giftedService.getPlansDueForReview();
        List<GiftedService> servicesNeedingDoc = giftedService.getServicesNeedingDocumentation();

        dashboard.put("programStatistics", programStats);
        dashboard.put("awaitingScreening", awaitingScreening);
        dashboard.put("awaitingScreeningCount", awaitingScreening.size());
        dashboard.put("needingAnnualReview", needingAnnualReview);
        dashboard.put("needingAnnualReviewCount", needingAnnualReview.size());
        dashboard.put("plansDueForReview", plansDueReview);
        dashboard.put("plansDueReviewCount", plansDueReview.size());
        dashboard.put("servicesNeedingDocumentation", servicesNeedingDoc);
        dashboard.put("servicesNeedingDocCount", servicesNeedingDoc.size());

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/student/{giftedStudentId}")
    public ResponseEntity<Map<String, Object>> getStudentDashboard(@PathVariable Long giftedStudentId) {
        Optional<GiftedStudent> giftedStudentOpt = giftedStudentRepository.findById(giftedStudentId);
        if (giftedStudentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        GiftedStudent giftedStudent = giftedStudentOpt.get();
        Map<String, Object> dashboard = new HashMap<>();

        List<GiftedAssessment> assessments = giftedService.getAssessmentsByStudent(giftedStudentId);
        List<GiftedEducationPlan> plans = giftedService.getPlansByStudent(giftedStudentId);
        List<GiftedService> services = giftedService.getServicesByStudent(giftedStudentId);

        dashboard.put("giftedStudent", giftedStudent);
        dashboard.put("studentId", giftedStudent.getStudent() != null ? giftedStudent.getStudent().getId() : null);
        dashboard.put("giftedStatus", giftedStudent.getGiftedStatus());
        dashboard.put("primaryArea", giftedStudent.getPrimaryGiftedArea());
        dashboard.put("assessments", assessments);
        dashboard.put("assessmentCount", assessments.size());
        dashboard.put("plans", plans);
        dashboard.put("planCount", plans.size());
        dashboard.put("services", services);
        dashboard.put("serviceCount", services.size());

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/compliance")
    public ResponseEntity<Map<String, Object>> getComplianceDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        Map<String, List<?>> alerts = giftedService.getComplianceAlerts();
        List<GiftedStudent> awaitingScreening = giftedService.getStudentsAwaitingScreening();
        List<GiftedStudent> needingAnnualReview = giftedService.getStudentsNeedingAnnualReview();
        List<GiftedStudent> needingParentConsent = giftedService.getStudentsNeedingParentConsent();
        List<GiftedEducationPlan> plansDueReview = giftedService.getPlansDueForReview();
        List<GiftedEducationPlan> plansExpiring = giftedService.getPlansExpiringSoon(30);
        List<GiftedService> servicesNeedingDoc = giftedService.getServicesNeedingDocumentation();
        List<GiftedAssessment> assessmentsPending = giftedService.getAssessmentsPendingResults();

        dashboard.put("complianceAlerts", alerts);
        dashboard.put("awaitingScreening", awaitingScreening);
        dashboard.put("awaitingScreeningCount", awaitingScreening.size());
        dashboard.put("needingAnnualReview", needingAnnualReview);
        dashboard.put("needingAnnualReviewCount", needingAnnualReview.size());
        dashboard.put("needingParentConsent", needingParentConsent);
        dashboard.put("needingParentConsentCount", needingParentConsent.size());
        dashboard.put("plansDueForReview", plansDueReview);
        dashboard.put("plansDueReviewCount", plansDueReview.size());
        dashboard.put("plansExpiringSoon", plansExpiring);
        dashboard.put("plansExpiringCount", plansExpiring.size());
        dashboard.put("servicesNeedingDocumentation", servicesNeedingDoc);
        dashboard.put("servicesNeedingDocCount", servicesNeedingDoc.size());
        dashboard.put("assessmentsPendingResults", assessmentsPending);
        dashboard.put("assessmentsPendingCount", assessmentsPending.size());

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/performance")
    public ResponseEntity<Map<String, Object>> getPerformanceDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        List<GiftedStudent> underperforming = giftedService.getUnderperformingStudents();
        List<GiftedStudent> withConcerns = giftedService.getStudentsWithConcerns();
        List<GiftedStudent> apStudents = giftedService.getStudentsInAPCourses();
        List<GiftedStudent> honorsStudents = giftedService.getStudentsInHonorsCourses();
        List<GiftedStudent> dualEnrollment = giftedService.getDualEnrollmentStudents();
        List<GiftedStudent> gradeAccelerated = giftedService.getGradeAcceleratedStudents();

        dashboard.put("underperformingStudents", underperforming);
        dashboard.put("underperformingCount", underperforming.size());
        dashboard.put("studentsWithConcerns", withConcerns);
        dashboard.put("concernsCount", withConcerns.size());
        dashboard.put("apStudents", apStudents);
        dashboard.put("apCount", apStudents.size());
        dashboard.put("honorsStudents", honorsStudents);
        dashboard.put("honorsCount", honorsStudents.size());
        dashboard.put("dualEnrollmentStudents", dualEnrollment);
        dashboard.put("dualEnrollmentCount", dualEnrollment.size());
        dashboard.put("gradeAcceleratedStudents", gradeAccelerated);
        dashboard.put("gradeAcceleratedCount", gradeAccelerated.size());

        return ResponseEntity.ok(dashboard);
    }
}
