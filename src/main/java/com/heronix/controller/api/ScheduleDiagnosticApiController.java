package com.heronix.controller.api;

import com.heronix.model.dto.ScheduleDiagnosticReport;
import com.heronix.service.ScheduleDiagnosticService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API Controller for Schedule Diagnostics
 *
 * Provides endpoints for generating administrator-friendly diagnostic reports
 * about schedule generation readiness, resource availability, and data quality.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/schedule-diagnostics")
@RequiredArgsConstructor
public class ScheduleDiagnosticApiController {

    private final ScheduleDiagnosticService diagnosticService;

    // ==================== Diagnostic Reports ====================

    @GetMapping("/generate")
    public ResponseEntity<ScheduleDiagnosticReport> generateDiagnosticReport() {
        ScheduleDiagnosticReport report = diagnosticService.generateDiagnosticReport();
        return ResponseEntity.ok(report);
    }

    @GetMapping("/full-report")
    public ResponseEntity<ScheduleDiagnosticReport> getFullDiagnosticReport() {
        ScheduleDiagnosticReport report = diagnosticService.generateDiagnosticReport();
        return ResponseEntity.ok(report);
    }

    // ==================== Quick Status Checks ====================

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getQuickStatus() {
        ScheduleDiagnosticReport report = diagnosticService.generateDiagnosticReport();

        Map<String, Object> status = new HashMap<>();
        status.put("overallStatus", report.getOverallStatus());
        status.put("criticalIssuesCount", report.getCriticalIssuesCount());
        status.put("warningsCount", report.getWarningsCount());
        status.put("readyToGenerate", report.getOverallStatus().equals("READY") || report.getOverallStatus().equals("READY_WITH_WARNINGS"));

        return ResponseEntity.ok(status);
    }

    @GetMapping("/ready-check")
    public ResponseEntity<Map<String, Object>> checkReadyToGenerate() {
        ScheduleDiagnosticReport report = diagnosticService.generateDiagnosticReport();

        boolean ready = report.getOverallStatus().equals("READY") ||
                       report.getOverallStatus().equals("READY_WITH_WARNINGS");

        Map<String, Object> response = new HashMap<>();
        response.put("ready", ready);
        response.put("overallStatus", report.getOverallStatus());
        response.put("criticalIssues", report.getCriticalIssuesCount());
        response.put("warnings", report.getWarningsCount());
        response.put("summaryMessage", report.getSummaryMessage());

        return ResponseEntity.ok(response);
    }

    // ==================== Resource Summary ====================

    @GetMapping("/resources")
    public ResponseEntity<Map<String, Object>> getResourceSummary() {
        ScheduleDiagnosticReport report = diagnosticService.generateDiagnosticReport();

        Map<String, Object> resources = new HashMap<>();
        if (report.getResourceSummary() != null) {
            resources.put("resourceSummary", report.getResourceSummary());
        }

        return ResponseEntity.ok(resources);
    }

    // ==================== Issues & Recommendations ====================

    @GetMapping("/issues")
    public ResponseEntity<Map<String, Object>> getIssues() {
        ScheduleDiagnosticReport report = diagnosticService.generateDiagnosticReport();

        Map<String, Object> issues = new HashMap<>();
        issues.put("criticalIssues", report.getIssues().stream()
            .filter(issue -> "CRITICAL".equals(issue.getSeverity()))
            .toList());
        issues.put("warnings", report.getIssues().stream()
            .filter(issue -> "WARNING".equals(issue.getSeverity()))
            .toList());
        issues.put("totalCount", report.getIssues().size());

        return ResponseEntity.ok(issues);
    }

    @GetMapping("/critical-issues")
    public ResponseEntity<Map<String, Object>> getCriticalIssues() {
        ScheduleDiagnosticReport report = diagnosticService.generateDiagnosticReport();

        Map<String, Object> response = new HashMap<>();
        response.put("count", report.getCriticalIssuesCount());
        response.put("issues", report.getIssues().stream()
            .filter(issue -> "CRITICAL".equals(issue.getSeverity()))
            .toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/warnings")
    public ResponseEntity<Map<String, Object>> getWarnings() {
        ScheduleDiagnosticReport report = diagnosticService.generateDiagnosticReport();

        Map<String, Object> response = new HashMap<>();
        response.put("count", report.getWarningsCount());
        response.put("warnings", report.getIssues().stream()
            .filter(issue -> "WARNING".equals(issue.getSeverity()))
            .toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/recommendations")
    public ResponseEntity<Map<String, Object>> getRecommendations() {
        ScheduleDiagnosticReport report = diagnosticService.generateDiagnosticReport();

        Map<String, Object> response = new HashMap<>();
        response.put("count", report.getRecommendedActions().size());
        response.put("actions", report.getRecommendedActions());

        return ResponseEntity.ok(response);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getDiagnosticDashboard() {
        ScheduleDiagnosticReport report = diagnosticService.generateDiagnosticReport();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("overallStatus", report.getOverallStatus());
        dashboard.put("summaryMessage", report.getSummaryMessage());
        dashboard.put("criticalIssuesCount", report.getCriticalIssuesCount());
        dashboard.put("warningsCount", report.getWarningsCount());
        dashboard.put("estimatedFixTimeMinutes", report.getEstimatedFixTimeMinutes());
        dashboard.put("diagnosticTimestamp", report.getDiagnosticTimestamp());

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        ScheduleDiagnosticReport report = diagnosticService.generateDiagnosticReport();

        Map<String, Object> summary = new HashMap<>();
        summary.put("status", report.getOverallStatus());
        summary.put("message", report.getSummaryMessage());
        summary.put("issueCount", report.getIssues().size());
        summary.put("recommendationsCount", report.getRecommendedActions().size());

        // Resource counts
        if (report.getResourceSummary() != null) {
            summary.put("resources", Map.of(
                "teachers", report.getResourceSummary().getActiveTeachers(),
                "courses", report.getResourceSummary().getActiveCourses(),
                "rooms", report.getResourceSummary().getAvailableRooms(),
                "students", report.getResourceSummary().getActiveStudents()
            ));
        }

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/dashboard/action-plan")
    public ResponseEntity<Map<String, Object>> getActionPlan() {
        ScheduleDiagnosticReport report = diagnosticService.generateDiagnosticReport();

        Map<String, Object> actionPlan = new HashMap<>();
        actionPlan.put("criticalIssues", report.getIssues().stream()
            .filter(issue -> "CRITICAL".equals(issue.getSeverity()))
            .toList());
        actionPlan.put("recommendations", report.getRecommendedActions());
        actionPlan.put("estimatedFixTimeMinutes", report.getEstimatedFixTimeMinutes());
        actionPlan.put("priority", report.getCriticalIssuesCount() > 0 ? "HIGH" : "MEDIUM");

        return ResponseEntity.ok(actionPlan);
    }
}
