package com.heronix.controller.api;

import com.heronix.model.domain.Student;
import com.heronix.repository.StudentRepository;
import com.heronix.service.AlertGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * REST API Controller for Alert Generation
 *
 * Provides endpoints for generating and routing intervention alerts to school personnel
 * based on student risk assessments. Integrates with student progress monitoring.
 *
 * Alert Types:
 * - ATTENDANCE: Tardiness and absence patterns
 * - ACADEMIC: Failing grades and missing work
 * - BEHAVIOR: Negative incidents
 * - OBSERVATION: Teacher concerns
 * - COMBINED: Multiple risk areas
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertGenerationApiController {

    private final AlertGenerationService alertGenerationService;
    private final StudentRepository studentRepository;

    // ==================== Alert Generation ====================

    @PostMapping("/student/{studentId}/generate")
    public ResponseEntity<Map<String, Object>> generateAlertForStudent(@PathVariable Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);

        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            alertGenerationService.generateAlert(studentOpt.get());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("message", "Alert generated and routed successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/student/{studentId}/immediate")
    public ResponseEntity<Map<String, Object>> generateImmediateAlert(
            @PathVariable Long studentId,
            @RequestBody Map<String, String> request) {

        Optional<Student> studentOpt = studentRepository.findById(studentId);

        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String reason = request.get("reason");
        String details = request.get("details");

        if (reason == null || reason.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Reason is required");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            alertGenerationService.generateImmediateAlert(
                studentOpt.get(), reason, details != null ? details : "");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("alertType", "IMMEDIATE");
            response.put("reason", reason);
            response.put("message", "Immediate alert generated and routed to all relevant personnel");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/generate-all")
    public ResponseEntity<Map<String, Object>> generateAlertsForAllAtRiskStudents() {
        try {
            alertGenerationService.generateAlertsForAllAtRiskStudents();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Alerts generated for all at-risk students");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Alert Configuration ====================

    @GetMapping("/configuration/types")
    public ResponseEntity<Map<String, Object>> getAlertTypes() {
        Map<String, Object> types = new HashMap<>();

        types.put("ATTENDANCE", Map.of(
            "name", "Attendance Alert",
            "description", "Excessive tardies or absences detected",
            "severity", "Varies based on pattern"
        ));

        types.put("ACADEMIC", Map.of(
            "name", "Academic Alert",
            "description", "Failing grades or significant missing work",
            "severity", "Varies based on grades"
        ));

        types.put("BEHAVIOR", Map.of(
            "name", "Behavior Alert",
            "description", "Multiple negative incidents or major incident",
            "severity", "Varies based on incidents"
        ));

        types.put("OBSERVATION", Map.of(
            "name", "Teacher Observation Alert",
            "description", "Multiple concern-level observations from teachers",
            "severity", "Varies based on observations"
        ));

        types.put("COMBINED", Map.of(
            "name", "Combined Risk Alert",
            "description", "Multiple risk areas detected",
            "severity", "Usually HIGH or URGENT"
        ));

        types.put("CRITICAL", Map.of(
            "name", "Critical Incident Alert",
            "description", "Immediate attention required",
            "severity", "URGENT"
        ));

        Map<String, Object> response = new HashMap<>();
        response.put("alertTypes", types);
        response.put("totalTypes", types.size());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/configuration/priorities")
    public ResponseEntity<Map<String, Object>> getAlertPriorities() {
        Map<String, Object> priorities = new HashMap<>();

        priorities.put("LOW", Map.of(
            "level", "LOW",
            "action", "Monitor",
            "recipients", "Teacher and counselor",
            "responseTime", "Monitor ongoing"
        ));

        priorities.put("NORMAL", Map.of(
            "level", "NORMAL",
            "action", "Review within 3 days",
            "recipients", "Teacher and counselor",
            "responseTime", "3 days"
        ));

        priorities.put("HIGH", Map.of(
            "level", "HIGH",
            "action", "Review within 1 day",
            "recipients", "Teacher, counselor, and grade-level administrator",
            "responseTime", "1 day"
        ));

        priorities.put("URGENT", Map.of(
            "level", "URGENT",
            "action", "Immediate attention",
            "recipients", "Teacher, counselor, grade-level admin, and principal",
            "responseTime", "Immediate"
        ));

        Map<String, Object> response = new HashMap<>();
        response.put("priorities", priorities);
        response.put("totalPriorities", priorities.size());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/configuration/routing")
    public ResponseEntity<Map<String, Object>> getRoutingRules() {
        Map<String, Object> routing = new HashMap<>();

        routing.put("LOW_RISK", Map.of(
            "recipients", "Teacher and counselor",
            "notification", "In-app notification"
        ));

        routing.put("MEDIUM_RISK", Map.of(
            "recipients", "Teacher, counselor, and grade-level administrator",
            "notification", "In-app notification and email"
        ));

        routing.put("HIGH_RISK", Map.of(
            "recipients", "Teacher, counselor, grade-level admin, and principal",
            "notification", "In-app notification, email, and dashboard alert"
        ));

        routing.put("CRITICAL", Map.of(
            "recipients", "All relevant personnel (immediate)",
            "notification", "Immediate notification to all parties"
        ));

        Map<String, Object> response = new HashMap<>();
        response.put("routingRules", routing);
        response.put("deduplication", "Alerts are deduplicated within 24-hour window");
        response.put("cooldownHours", 24);

        return ResponseEntity.ok(response);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        dashboard.put("features", Map.of(
            "automaticMonitoring", "Nightly job monitors all at-risk students",
            "smartRouting", "Routes alerts based on risk level and type",
            "deduplication", "Prevents duplicate alerts within 24 hours",
            "immediateAlerts", "Support for critical incidents"
        ));

        dashboard.put("alertTypes", 6);
        dashboard.put("priorityLevels", 4);
        dashboard.put("status", "ACTIVE");

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        // Note: Alert statistics would require database tracking
        // This is a placeholder implementation

        Map<String, Object> stats = new HashMap<>();
        stats.put("note", "Alert statistics require database persistence");
        stats.put("currentImplementation", "In-memory tracking with 24-hour deduplication");
        stats.put("futureEnhancement", "Database-backed alert history and reporting");

        return ResponseEntity.ok(stats);
    }

    // ==================== Utility Endpoints ====================

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("version", "1.0.0");
        metadata.put("features", Map.of(
            "alertGeneration", "Generate alerts for individual students",
            "bulkGeneration", "Generate alerts for all at-risk students",
            "immediateAlerts", "Critical incident alerts bypass normal routing",
            "smartRouting", "Route to appropriate personnel based on severity",
            "deduplication", "Prevent duplicate alerts within 24 hours"
        ));

        metadata.put("integrations", Map.of(
            "progressMonitoring", "StudentProgressMonitoringService",
            "notifications", "NotificationService",
            "userManagement", "UserRepository for personnel routing"
        ));

        metadata.put("riskLevels", Map.of(
            "NONE", "No intervention needed",
            "LOW", "Monitor - notify teacher and counselor",
            "MEDIUM", "Review within 3 days - notify teacher, counselor, and grade admin",
            "HIGH", "Review within 1 day - notify all relevant personnel including principal"
        ));

        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = new HashMap<>();

        help.put("endpoints", Map.of(
            "generateAlert", "POST /api/alerts/student/{studentId}/generate - Generate alert for specific student",
            "generateImmediate", "POST /api/alerts/student/{studentId}/immediate - Generate critical incident alert",
            "generateAll", "POST /api/alerts/generate-all - Generate alerts for all at-risk students",
            "configuration", "GET /api/alerts/configuration/* - View alert configuration"
        ));

        help.put("examples", Map.of(
            "generateAlert", "curl -X POST /api/alerts/student/123/generate",
            "immediateAlert", "curl -X POST /api/alerts/student/123/immediate -d '{\"reason\":\"Safety concern\", \"details\":\"...\"}'",
            "generateAll", "curl -X POST /api/alerts/generate-all"
        ));

        return ResponseEntity.ok(help);
    }
}
