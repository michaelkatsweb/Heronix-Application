package com.heronix.controller.api;

import com.heronix.service.SmartRoomAssignmentService;
import com.heronix.service.SmartTeacherAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API Controller for Smart Room Assignment
 *
 * Provides endpoints for intelligent automatic room assignment to courses using:
 * - Room type matching (lab for science, gym for PE, etc.)
 * - Capacity matching (room size >= expected enrollment)
 * - Teacher home room preferences
 * - Efficient space usage
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/smart-room-assignment")
@RequiredArgsConstructor
public class SmartRoomAssignmentApiController {

    private final SmartRoomAssignmentService smartRoomAssignmentService;

    // ==================== Assignment Operations ====================

    @PostMapping("/assign-all")
    public ResponseEntity<Map<String, Object>> assignAllRooms() {
        try {
            SmartTeacherAssignmentService.AssignmentResult result =
                smartRoomAssignmentService.smartAssignAllRooms();

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.getCoursesFailed() == 0);
            response.put("message", result.getMessage());
            response.put("totalCourses", result.getTotalCoursesProcessed());
            response.put("coursesAssigned", result.getCoursesAssigned());
            response.put("coursesFailed", result.getCoursesFailed());
            response.put("durationMs", result.getDurationMs());
            response.put("warnings", result.getWarnings());
            response.put("errors", result.getErrors());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/preview")
    public ResponseEntity<Map<String, Object>> previewAssignments() {
        try {
            SmartTeacherAssignmentService.AssignmentResult result =
                smartRoomAssignmentService.previewRoomAssignments();

            Map<String, Object> response = new HashMap<>();
            response.put("preview", true);
            response.put("message", result.getMessage());
            response.put("totalCourses", result.getTotalCoursesProcessed());
            response.put("wouldAssign", result.getCoursesAssigned());
            response.put("wouldFail", result.getCoursesFailed());
            response.put("successRate", result.getTotalCoursesProcessed() > 0 ?
                (result.getCoursesAssigned() * 100.0 / result.getTotalCoursesProcessed()) : 0);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Status & Statistics ====================

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAssignmentStatus() {
        SmartTeacherAssignmentService.AssignmentResult preview =
            smartRoomAssignmentService.previewRoomAssignments();

        Map<String, Object> status = new HashMap<>();
        status.put("totalUnassignedCourses", preview.getTotalCoursesProcessed());
        status.put("canAssign", preview.getCoursesAssigned());
        status.put("cannotAssign", preview.getCoursesFailed());
        status.put("readyForAssignment", preview.getTotalCoursesProcessed() > 0);
        status.put("estimatedSuccessRate", preview.getTotalCoursesProcessed() > 0 ?
            (preview.getCoursesAssigned() * 100.0 / preview.getTotalCoursesProcessed()) : 0);

        return ResponseEntity.ok(status);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        SmartTeacherAssignmentService.AssignmentResult preview =
            smartRoomAssignmentService.previewRoomAssignments();

        Map<String, Object> stats = new HashMap<>();
        stats.put("unassignedCourses", preview.getTotalCoursesProcessed());
        stats.put("potentialAssignments", preview.getCoursesAssigned());
        stats.put("potentialFailures", preview.getCoursesFailed());
        stats.put("completionPercentage", preview.getTotalCoursesProcessed() > 0 ?
            (preview.getCoursesAssigned() * 100.0 / preview.getTotalCoursesProcessed()) : 0);

        return ResponseEntity.ok(stats);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        SmartTeacherAssignmentService.AssignmentResult preview =
            smartRoomAssignmentService.previewRoomAssignments();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("unassignedCourses", preview.getTotalCoursesProcessed());
        dashboard.put("assignableCount", preview.getCoursesAssigned());
        dashboard.put("unassignableCount", preview.getCoursesFailed());
        dashboard.put("successRate", preview.getTotalCoursesProcessed() > 0 ?
            (preview.getCoursesAssigned() * 100.0 / preview.getTotalCoursesProcessed()) : 0);
        dashboard.put("status", preview.getTotalCoursesProcessed() == 0 ? "COMPLETE" :
            preview.getCoursesFailed() == 0 ? "READY" : "NEEDS_ATTENTION");

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/readiness")
    public ResponseEntity<Map<String, Object>> getReadiness() {
        SmartTeacherAssignmentService.AssignmentResult preview =
            smartRoomAssignmentService.previewRoomAssignments();

        boolean ready = preview.getTotalCoursesProcessed() > 0 &&
                       preview.getCoursesFailed() < preview.getTotalCoursesProcessed() * 0.1; // < 10% failure acceptable

        Map<String, Object> readiness = new HashMap<>();
        readiness.put("ready", ready);
        readiness.put("unassignedCourses", preview.getTotalCoursesProcessed());
        readiness.put("assignableCount", preview.getCoursesAssigned());
        readiness.put("unassignableCount", preview.getCoursesFailed());
        readiness.put("recommendation", ready ?
            "System is ready for automatic room assignment" :
            preview.getTotalCoursesProcessed() == 0 ?
                "All courses already have rooms assigned" :
                "Review unassignable courses before running assignment");

        return ResponseEntity.ok(readiness);
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        SmartTeacherAssignmentService.AssignmentResult preview =
            smartRoomAssignmentService.previewRoomAssignments();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalUnassigned", preview.getTotalCoursesProcessed());
        summary.put("canAssign", preview.getCoursesAssigned());
        summary.put("cannotAssign", preview.getCoursesFailed());
        summary.put("message", preview.getMessage());

        return ResponseEntity.ok(summary);
    }

    // ==================== Configuration & Metadata ====================

    @GetMapping("/configuration")
    public ResponseEntity<Map<String, Object>> getConfiguration() {
        Map<String, Object> config = new HashMap<>();

        config.put("features", Map.of(
            "roomTypeMatching", "Matches courses to appropriate room types (lab, gym, etc.)",
            "capacityMatching", "Ensures room size >= expected enrollment",
            "homeRoomPreference", "Prefers teacher's designated home room",
            "efficientSpaceUsage", "Selects smallest suitable room"
        ));

        config.put("roomTypes", Map.of(
            "SCIENCE_LAB", "Science courses requiring lab equipment",
            "COMPUTER_LAB", "Computer and technology courses",
            "GYMNASIUM", "Physical education and athletics",
            "MUSIC_ROOM", "Music, band, orchestra, chorus",
            "ART_STUDIO", "Art courses",
            "CLASSROOM", "Standard academic courses"
        ));

        config.put("scoring", Map.of(
            "typeMatch", "100 points for exact match, 50 for compatible",
            "capacityEfficiency", "0-50 points (prefer smallest suitable room)",
            "homeRoomBonus", "75 points for teacher's home room",
            "minCapacityBonus", "15 points for efficient room usage"
        ));

        return ResponseEntity.ok(config);
    }

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("version", "2.0.0");
        metadata.put("lastUpdated", "December 15, 2025");

        metadata.put("improvements", Map.of(
            "performanceOptimization", "Query-level filtering for schedulable rooms",
            "homeRoomIntegration", "Prefers teacher's designated home room",
            "capacityHandling", "Multi-section course capacity calculation",
            "fireCodeCompliance", "Respects maxCapacity limits"
        ));

        metadata.put("capabilities", Map.of(
            "preview", "Preview assignments before applying",
            "intelligentMatching", "Multi-factor scoring system",
            "errorHandling", "Detailed warnings and error messages",
            "capacityValidation", "Fire code and efficiency compliance"
        ));

        return ResponseEntity.ok(metadata);
    }
}
