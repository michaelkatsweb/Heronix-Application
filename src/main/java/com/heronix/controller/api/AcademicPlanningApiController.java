package com.heronix.controller.api;

import com.heronix.model.domain.AcademicPlan;
import com.heronix.model.domain.PlannedCourse;
import com.heronix.repository.AcademicPlanRepository;
import com.heronix.repository.PlannedCourseRepository;
import com.heronix.service.AcademicPlanningService;
import com.heronix.service.AcademicPlanningService.PlanningStatistics;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Academic Planning (Four-Year Planning)
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/academic-planning")
@RequiredArgsConstructor
public class AcademicPlanningApiController {

    private final AcademicPlanningService planningService;
    private final AcademicPlanRepository planRepository;
    private final PlannedCourseRepository plannedCourseRepository;

    // ==================== Academic Plan CRUD Operations ====================

    @GetMapping("/plans")
    public ResponseEntity<List<AcademicPlan>> getAllPlans() {
        List<AcademicPlan> plans = planningService.getAllPlans();
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/plans/{id}")
    public ResponseEntity<AcademicPlan> getPlanById(@PathVariable Long id) {
        return planningService.getPlanById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/plans/student/{studentId}")
    public ResponseEntity<List<AcademicPlan>> getPlansForStudent(@PathVariable Long studentId) {
        List<AcademicPlan> plans = planningService.getPlansForStudent(studentId);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/plans/student/{studentId}/primary")
    public ResponseEntity<AcademicPlan> getPrimaryPlanForStudent(@PathVariable Long studentId) {
        return planningService.getPrimaryPlanForStudent(studentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/plans")
    public ResponseEntity<AcademicPlan> createPlan(@RequestBody AcademicPlan plan) {
        AcademicPlan created = planningService.createPlan(plan);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/plans/{id}")
    public ResponseEntity<AcademicPlan> updatePlan(
            @PathVariable Long id,
            @RequestBody AcademicPlan updates) {
        AcademicPlan updated = planningService.updatePlan(id, updates);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/plans/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable Long id) {
        planningService.deletePlan(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Plan Generation Operations ====================

    @PostMapping("/plans/generate/from-sequence")
    public ResponseEntity<AcademicPlan> generatePlanFromSequence(
            @RequestParam Long studentId,
            @RequestParam Long sequenceId,
            @RequestParam String startYear,
            @RequestParam(required = false) String planName) {
        AcademicPlan plan = planningService.generatePlanFromSequence(
                studentId, sequenceId, startYear, planName);
        return ResponseEntity.status(HttpStatus.CREATED).body(plan);
    }

    @PostMapping("/plans/generate/from-recommendations")
    public ResponseEntity<AcademicPlan> generatePlanFromRecommendations(
            @RequestParam Long studentId,
            @RequestParam String startYear,
            @RequestParam(required = false) String planName) {
        AcademicPlan plan = planningService.generatePlanFromRecommendations(
                studentId, startYear, planName);
        return ResponseEntity.status(HttpStatus.CREATED).body(plan);
    }

    // ==================== Planned Course Operations ====================

    @PostMapping("/plans/{planId}/courses")
    public ResponseEntity<AcademicPlan> addCourseToPlan(
            @PathVariable Long planId,
            @RequestBody PlannedCourse plannedCourse) {
        AcademicPlan updated = planningService.addCourseToPlan(planId, plannedCourse);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/plans/{planId}/courses/{plannedCourseId}")
    public ResponseEntity<AcademicPlan> removeCourseFromPlan(
            @PathVariable Long planId,
            @PathVariable Long plannedCourseId) {
        AcademicPlan updated = planningService.removeCourseFromPlan(planId, plannedCourseId);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/courses/{plannedCourseId}/mark-completed")
    public ResponseEntity<PlannedCourse> markCourseCompleted(
            @PathVariable Long plannedCourseId,
            @RequestParam String grade) {
        PlannedCourse updated = planningService.markCourseCompleted(plannedCourseId, grade);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/courses/{id}")
    public ResponseEntity<PlannedCourse> getPlannedCourseById(@PathVariable Long id) {
        return plannedCourseRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/plans/{planId}/courses")
    public ResponseEntity<List<PlannedCourse>> getCoursesForPlan(@PathVariable Long planId) {
        return planRepository.findById(planId)
                .map(plan -> {
                    List<PlannedCourse> courses = plannedCourseRepository
                            .findByAcademicPlanOrderBySchoolYearAscSemesterAsc(plan);
                    return ResponseEntity.ok(courses);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== Approval Operations ====================

    @PatchMapping("/plans/{planId}/approve/counselor")
    public ResponseEntity<AcademicPlan> approveByCounselor(
            @PathVariable Long planId,
            @RequestParam Long counselorId) {
        AcademicPlan approved = planningService.approveByCounselor(planId, counselorId);
        return ResponseEntity.ok(approved);
    }

    @PatchMapping("/plans/{planId}/accept/student")
    public ResponseEntity<AcademicPlan> acceptByStudent(@PathVariable Long planId) {
        AcademicPlan accepted = planningService.acceptByStudent(planId);
        return ResponseEntity.ok(accepted);
    }

    @PatchMapping("/plans/{planId}/accept/parent")
    public ResponseEntity<AcademicPlan> acceptByParent(@PathVariable Long planId) {
        AcademicPlan accepted = planningService.acceptByParent(planId);
        return ResponseEntity.ok(accepted);
    }

    @PostMapping("/plans/{planId}/recalculate")
    public ResponseEntity<Void> recalculatePlanTotals(@PathVariable Long planId) {
        return planRepository.findById(planId)
                .map(plan -> {
                    planningService.recalculatePlanTotals(plan);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== Statistics and Reporting ====================

    @GetMapping("/statistics")
    public ResponseEntity<PlanningStatistics> getStatistics() {
        PlanningStatistics stats = planningService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/plans/status/{status}")
    public ResponseEntity<List<AcademicPlan>> getPlansByStatus(@PathVariable AcademicPlan.PlanStatus status) {
        List<AcademicPlan> plans = planRepository.findByStatusAndActiveTrue(status);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/plans/type/{type}")
    public ResponseEntity<List<AcademicPlan>> getPlansByType(@PathVariable AcademicPlan.PlanType type) {
        List<AcademicPlan> plans = planRepository.findByPlanType(type);
        return ResponseEntity.ok(plans);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getDashboardOverview() {
        Map<String, Object> dashboard = new HashMap<>();

        PlanningStatistics stats = planningService.getStatistics();
        List<AcademicPlan> draftPlans = planRepository.findByStatusAndActiveTrue(AcademicPlan.PlanStatus.DRAFT);
        List<AcademicPlan> approvedPlans = planRepository.findByStatusAndActiveTrue(AcademicPlan.PlanStatus.APPROVED);

        dashboard.put("statistics", stats);
        dashboard.put("draftPlans", draftPlans);
        dashboard.put("draftCount", draftPlans.size());
        dashboard.put("approvedPlans", approvedPlans);
        dashboard.put("approvedCount", approvedPlans.size());

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getStudentDashboard(@PathVariable Long studentId) {
        Map<String, Object> dashboard = new HashMap<>();

        List<AcademicPlan> allPlans = planningService.getPlansForStudent(studentId);
        AcademicPlan primaryPlan = planningService.getPrimaryPlanForStudent(studentId).orElse(null);

        long draftCount = allPlans.stream()
                .filter(p -> p.getStatus() == AcademicPlan.PlanStatus.DRAFT)
                .count();
        long approvedCount = allPlans.stream()
                .filter(p -> p.getStatus() == AcademicPlan.PlanStatus.APPROVED)
                .count();
        long completedCount = allPlans.stream()
                .filter(p -> p.getStatus() == AcademicPlan.PlanStatus.COMPLETED)
                .count();

        dashboard.put("studentId", studentId);
        dashboard.put("allPlans", allPlans);
        dashboard.put("totalPlans", allPlans.size());
        dashboard.put("primaryPlan", primaryPlan);
        dashboard.put("draftCount", draftCount);
        dashboard.put("approvedCount", approvedCount);
        dashboard.put("completedCount", completedCount);

        // If there's a primary plan, include its courses
        if (primaryPlan != null) {
            List<PlannedCourse> courses = plannedCourseRepository
                    .findByAcademicPlanOrderBySchoolYearAscSemesterAsc(primaryPlan);
            dashboard.put("primaryPlanCourses", courses);
            dashboard.put("primaryPlanCoursesCount", courses.size());
            dashboard.put("creditsPlanned", primaryPlan.getTotalCreditsPlanned());
            dashboard.put("creditsCompleted", primaryPlan.getTotalCreditsCompleted());
            dashboard.put("meetsGraduationRequirements", primaryPlan.getMeetsGraduationRequirements());
        }

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/plan/{planId}")
    public ResponseEntity<Map<String, Object>> getPlanDashboard(@PathVariable Long planId) {
        return planRepository.findById(planId)
                .map(plan -> {
                    Map<String, Object> dashboard = new HashMap<>();

                    List<PlannedCourse> courses = plannedCourseRepository
                            .findByAcademicPlanOrderBySchoolYearAscSemesterAsc(plan);

                    long plannedCount = courses.stream()
                            .filter(c -> c.getStatus() == PlannedCourse.CourseStatus.PLANNED)
                            .count();
                    long enrolledCount = courses.stream()
                            .filter(c -> c.getStatus() == PlannedCourse.CourseStatus.ENROLLED)
                            .count();
                    long completedCount = courses.stream()
                            .filter(c -> c.getStatus() == PlannedCourse.CourseStatus.COMPLETED)
                            .count();
                    long droppedCount = courses.stream()
                            .filter(c -> c.getStatus() == PlannedCourse.CourseStatus.DROPPED)
                            .count();

                    dashboard.put("plan", plan);
                    dashboard.put("courses", courses);
                    dashboard.put("totalCourses", courses.size());
                    dashboard.put("plannedCount", plannedCount);
                    dashboard.put("enrolledCount", enrolledCount);
                    dashboard.put("completedCount", completedCount);
                    dashboard.put("droppedCount", droppedCount);
                    dashboard.put("creditsPlanned", plan.getTotalCreditsPlanned());
                    dashboard.put("creditsCompleted", plan.getTotalCreditsCompleted());
                    dashboard.put("meetsGraduationRequirements", plan.getMeetsGraduationRequirements());

                    return ResponseEntity.ok(dashboard);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
