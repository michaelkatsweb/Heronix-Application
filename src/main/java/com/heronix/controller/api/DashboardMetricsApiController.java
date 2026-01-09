package com.heronix.controller.api;

import com.heronix.model.domain.Teacher;
import com.heronix.model.dto.DashboardMetrics;
import com.heronix.service.DashboardMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Dashboard Metrics
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/dashboard-metrics")
@RequiredArgsConstructor
public class DashboardMetricsApiController {

    private final DashboardMetricsService metricsService;

    // ==================== Main Metrics ====================

    @GetMapping
    public ResponseEntity<DashboardMetrics> getMetrics() {
        DashboardMetrics metrics = metricsService.calculateMetrics();
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/full")
    public ResponseEntity<DashboardMetrics> getFullMetrics() {
        DashboardMetrics metrics = metricsService.calculateMetrics();
        return ResponseEntity.ok(metrics);
    }

    // ==================== Course Assignment Metrics ====================

    @GetMapping("/courses/fully-assigned/count")
    public ResponseEntity<Map<String, Long>> getFullyAssignedCount() {
        long count = metricsService.countFullyAssignedCourses();
        Map<String, Long> response = new HashMap<>();
        response.put("fullyAssignedCourses", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/courses/partially-assigned/count")
    public ResponseEntity<Map<String, Long>> getPartiallyAssignedCount() {
        long count = metricsService.countPartiallyAssignedCourses();
        Map<String, Long> response = new HashMap<>();
        response.put("partiallyAssignedCourses", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/courses/unassigned/count")
    public ResponseEntity<Map<String, Long>> getUnassignedCount() {
        long count = metricsService.countUnassignedCourses();
        Map<String, Long> response = new HashMap<>();
        response.put("unassignedCourses", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/courses/assignment-summary")
    public ResponseEntity<Map<String, Object>> getCourseAssignmentSummary() {
        DashboardMetrics metrics = metricsService.calculateMetrics();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCourses", metrics.getTotalCourses());
        summary.put("fullyAssignedCourses", metrics.getFullyAssignedCourses());
        summary.put("partiallyAssignedCourses", metrics.getPartiallyAssignedCourses());
        summary.put("unassignedCourses", metrics.getUnassignedCourses());
        summary.put("fullyAssignedPercent", metrics.getFullyAssignedPercent());
        summary.put("partiallyAssignedPercent", metrics.getPartiallyAssignedPercent());
        summary.put("unassignedPercent", metrics.getUnassignedPercent());

        return ResponseEntity.ok(summary);
    }

    // ==================== Teacher Workload Metrics ====================

    @GetMapping("/teachers/overloaded")
    public ResponseEntity<List<Teacher>> getOverloadedTeachers() {
        List<Teacher> teachers = metricsService.getOverloadedTeachers();
        return ResponseEntity.ok(teachers);
    }

    @GetMapping("/teachers/overloaded/count")
    public ResponseEntity<Map<String, Object>> getOverloadedTeachersCount() {
        List<Teacher> teachers = metricsService.getOverloadedTeachers();
        DashboardMetrics metrics = metricsService.calculateMetrics();

        Map<String, Object> response = new HashMap<>();
        response.put("count", teachers.size());
        response.put("message", metrics.getOverloadedTeachersMessage());
        response.put("teachers", teachers);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/teachers/underutilized")
    public ResponseEntity<List<Teacher>> getUnderutilizedTeachers() {
        List<Teacher> teachers = metricsService.getUnderutilizedTeachers();
        return ResponseEntity.ok(teachers);
    }

    @GetMapping("/teachers/underutilized/count")
    public ResponseEntity<Map<String, Object>> getUnderutilizedTeachersCount() {
        List<Teacher> teachers = metricsService.getUnderutilizedTeachers();
        DashboardMetrics metrics = metricsService.calculateMetrics();

        Map<String, Object> response = new HashMap<>();
        response.put("count", teachers.size());
        response.put("message", metrics.getUnderutilizedTeachersMessage());
        response.put("teachers", teachers);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/teachers/workload-summary")
    public ResponseEntity<Map<String, Object>> getTeacherWorkloadSummary() {
        DashboardMetrics metrics = metricsService.calculateMetrics();

        Map<String, Object> summary = new HashMap<>();
        summary.put("overloadedCount", metrics.getOverloadedTeachersCount());
        summary.put("overloadedMessage", metrics.getOverloadedTeachersMessage());
        summary.put("underutilizedCount", metrics.getUnderutilizedTeachersCount());
        summary.put("underutilizedMessage", metrics.getUnderutilizedTeachersMessage());

        return ResponseEntity.ok(summary);
    }

    // ==================== Issues & Warnings ====================

    @GetMapping("/issues/certification-mismatches")
    public ResponseEntity<Map<String, Object>> getCertificationMismatches() {
        DashboardMetrics metrics = metricsService.calculateMetrics();

        Map<String, Object> response = new HashMap<>();
        response.put("count", metrics.getCertificationMismatchCount());
        response.put("message", metrics.getCertificationMismatchMessage());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/issues/lab-rooms")
    public ResponseEntity<Map<String, Object>> getLabRoomIssues() {
        DashboardMetrics metrics = metricsService.calculateMetrics();

        Map<String, Object> response = new HashMap<>();
        response.put("count", metrics.getLabRoomIssuesCount());
        response.put("message", metrics.getLabRoomIssuesMessage());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/issues/capacity")
    public ResponseEntity<Map<String, Object>> getCapacityIssues() {
        DashboardMetrics metrics = metricsService.calculateMetrics();

        Map<String, Object> response = new HashMap<>();
        response.put("count", metrics.getCapacityIssuesCount());
        response.put("message", metrics.getCapacityIssuesMessage());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/issues/summary")
    public ResponseEntity<Map<String, Object>> getIssuesSummary() {
        DashboardMetrics metrics = metricsService.calculateMetrics();

        Map<String, Object> summary = new HashMap<>();
        summary.put("overloadedTeachers", metrics.getOverloadedTeachersCount());
        summary.put("underutilizedTeachers", metrics.getUnderutilizedTeachersCount());
        summary.put("certificationMismatches", metrics.getCertificationMismatchCount());
        summary.put("labRoomIssues", metrics.getLabRoomIssuesCount());
        summary.put("capacityIssues", metrics.getCapacityIssuesCount());
        summary.put("totalIssues",
                metrics.getOverloadedTeachersCount() +
                        metrics.getUnderutilizedTeachersCount() +
                        metrics.getCertificationMismatchCount() +
                        metrics.getLabRoomIssuesCount() +
                        metrics.getCapacityIssuesCount());

        return ResponseEntity.ok(summary);
    }

    // ==================== Cache Management ====================

    @PostMapping("/cache/invalidate")
    public ResponseEntity<Map<String, String>> invalidateCache() {
        metricsService.invalidateMetricsCache();

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Dashboard metrics cache invalidated");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/cache/refresh")
    public ResponseEntity<DashboardMetrics> refreshMetrics() {
        metricsService.invalidateMetricsCache();
        DashboardMetrics metrics = metricsService.calculateMetrics();

        return ResponseEntity.ok(metrics);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getDashboardOverview() {
        DashboardMetrics metrics = metricsService.calculateMetrics();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("courseMetrics", Map.of(
                "total", metrics.getTotalCourses(),
                "fullyAssigned", metrics.getFullyAssignedCourses(),
                "partiallyAssigned", metrics.getPartiallyAssignedCourses(),
                "unassigned", metrics.getUnassignedCourses(),
                "fullyAssignedPercent", metrics.getFullyAssignedPercent(),
                "partiallyAssignedPercent", metrics.getPartiallyAssignedPercent(),
                "unassignedPercent", metrics.getUnassignedPercent()
        ));

        dashboard.put("teacherWorkload", Map.of(
                "overloaded", metrics.getOverloadedTeachersCount(),
                "underutilized", metrics.getUnderutilizedTeachersCount(),
                "overloadedMessage", metrics.getOverloadedTeachersMessage(),
                "underutilizedMessage", metrics.getUnderutilizedTeachersMessage()
        ));

        dashboard.put("issues", Map.of(
                "certificationMismatches", metrics.getCertificationMismatchCount(),
                "labRoomIssues", metrics.getLabRoomIssuesCount(),
                "capacityIssues", metrics.getCapacityIssuesCount()
        ));

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/attention-required")
    public ResponseEntity<Map<String, Object>> getAttentionRequiredDashboard() {
        DashboardMetrics metrics = metricsService.calculateMetrics();

        Map<String, Object> dashboard = new HashMap<>();

        long totalIssues = metrics.getOverloadedTeachersCount() +
                metrics.getUnderutilizedTeachersCount() +
                metrics.getCertificationMismatchCount() +
                metrics.getLabRoomIssuesCount() +
                metrics.getCapacityIssuesCount();

        dashboard.put("totalIssues", totalIssues);
        dashboard.put("hasIssues", totalIssues > 0);

        dashboard.put("overloadedTeachers", Map.of(
                "count", metrics.getOverloadedTeachersCount(),
                "message", metrics.getOverloadedTeachersMessage()
        ));

        dashboard.put("underutilizedTeachers", Map.of(
                "count", metrics.getUnderutilizedTeachersCount(),
                "message", metrics.getUnderutilizedTeachersMessage()
        ));

        dashboard.put("certificationMismatches", Map.of(
                "count", metrics.getCertificationMismatchCount(),
                "message", metrics.getCertificationMismatchMessage()
        ));

        dashboard.put("labRoomIssues", Map.of(
                "count", metrics.getLabRoomIssuesCount(),
                "message", metrics.getLabRoomIssuesMessage()
        ));

        dashboard.put("capacityIssues", Map.of(
                "count", metrics.getCapacityIssuesCount(),
                "message", metrics.getCapacityIssuesMessage()
        ));

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/progress")
    public ResponseEntity<Map<String, Object>> getProgressDashboard() {
        DashboardMetrics metrics = metricsService.calculateMetrics();

        Map<String, Object> dashboard = new HashMap<>();

        dashboard.put("totalCourses", metrics.getTotalCourses());
        dashboard.put("fullyAssigned", metrics.getFullyAssignedCourses());
        dashboard.put("progressPercentage", metrics.getFullyAssignedPercent());
        dashboard.put("remainingCourses", metrics.getPartiallyAssignedCourses() + metrics.getUnassignedCourses());
        dashboard.put("isComplete", metrics.getUnassignedCourses() == 0 && metrics.getPartiallyAssignedCourses() == 0);

        return ResponseEntity.ok(dashboard);
    }
}
