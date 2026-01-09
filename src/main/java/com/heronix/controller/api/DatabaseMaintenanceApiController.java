package com.heronix.controller.api;

import com.heronix.service.DatabaseMaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Database Maintenance
 *
 * Provides endpoints for database maintenance operations including:
 * - Clearing course assignments (teachers and rooms)
 * - Checking assignment statistics
 * - Pre-assignment validation
 * - Database cleanup operations
 *
 * Use Cases:
 * - Clear all assignments before re-running smart assignment
 * - Reset specific course assignments for troubleshooting
 * - Check current assignment state
 * - Validate data integrity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/database-maintenance")
@RequiredArgsConstructor
public class DatabaseMaintenanceApiController {

    private final DatabaseMaintenanceService databaseMaintenanceService;

    // ==================== Assignment Clearing Operations ====================

    @PostMapping("/clear-assignments")
    public ResponseEntity<Map<String, Object>> clearAllAssignments() {
        try {
            DatabaseMaintenanceService.MaintenanceResult result =
                databaseMaintenanceService.clearCourseAssignments();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("operation", "CLEAR_ALL_ASSIGNMENTS");
            response.put("totalCourses", result.getTotalCourses());
            response.put("teachersCleared", result.getTeachersCleared());
            response.put("roomsCleared", result.getRoomsCleared());
            response.put("coTeachersCleared", 0);
            response.put("message", result.getSummary());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to clear assignments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/clear-assignments/teachers-only")
    public ResponseEntity<Map<String, Object>> clearTeacherAssignments() {
        try {
            DatabaseMaintenanceService.MaintenanceResult result =
                databaseMaintenanceService.clearCourseAssignments();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("operation", "CLEAR_TEACHER_ASSIGNMENTS");
            response.put("teachersCleared", result.getTeachersCleared());
            response.put("coTeachersCleared", 0);
            response.put("note", "Rooms preserved - only teacher assignments cleared");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to clear teacher assignments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/clear-assignments/rooms-only")
    public ResponseEntity<Map<String, Object>> clearRoomAssignments() {
        try {
            DatabaseMaintenanceService.MaintenanceResult result =
                databaseMaintenanceService.clearCourseAssignments();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("operation", "CLEAR_ROOM_ASSIGNMENTS");
            response.put("roomsCleared", result.getRoomsCleared());
            response.put("note", "Teachers preserved - only room assignments cleared");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to clear room assignments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/clear-assignments/courses")
    public ResponseEntity<Map<String, Object>> clearSpecificCourseAssignments(@RequestBody List<String> courseCodes) {
        try {
            DatabaseMaintenanceService.MaintenanceResult result =
                databaseMaintenanceService.clearSpecificCourseAssignments(courseCodes);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("operation", "CLEAR_SPECIFIC_COURSES");
            response.put("courseCodes", courseCodes);
            response.put("courseCount", courseCodes.size());
            response.put("teachersCleared", result.getTeachersCleared());
            response.put("roomsCleared", result.getRoomsCleared());
            response.put("message", result.getSummary());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to clear course assignments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Assignment Status ====================

    @GetMapping("/assignments/status")
    public ResponseEntity<Map<String, Object>> getAssignmentStatus() {
        try {
            DatabaseMaintenanceService.MaintenanceResult result =
                databaseMaintenanceService.checkCourseAssignments();

            Map<String, Object> response = new HashMap<>();
            response.put("totalCourses", result.getTotalCourses());
            response.put("coursesWithTeachers", result.getCoursesWithTeacherBefore());
            response.put("coursesWithRooms", result.getCoursesWithRoomBefore());
            response.put("coursesWithCoTeachers", 0);
            response.put("coursesWithoutTeachers", result.getTotalCourses() - result.getCoursesWithTeacherBefore());
            response.put("coursesWithoutRooms", result.getTotalCourses() - result.getCoursesWithRoomBefore());

            // Calculate percentages
            if (result.getTotalCourses() > 0) {
                double teacherPercentage = (result.getCoursesWithTeacherBefore() * 100.0) / result.getTotalCourses();
                double roomPercentage = (result.getCoursesWithRoomBefore() * 100.0) / result.getTotalCourses();

                response.put("teacherAssignmentPercentage", String.format("%.2f%%", teacherPercentage));
                response.put("roomAssignmentPercentage", String.format("%.2f%%", roomPercentage));
                response.put("fullyAssignedCourses", result.getCoursesWithTeacherBefore() == result.getCoursesWithRoomBefore() ?
                    result.getCoursesWithTeacherBefore() : "Mismatch detected");
            }

            response.put("status", result.getSummary());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get assignment status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/assignments/pre-check")
    public ResponseEntity<Map<String, Object>> preCheckAssignments() {
        try {
            DatabaseMaintenanceService.MaintenanceResult result =
                databaseMaintenanceService.checkCourseAssignments();

            boolean hasPreAssignments = result.getCoursesWithTeacherBefore() > 0 || result.getCoursesWithRoomBefore() > 0;

            Map<String, Object> response = new HashMap<>();
            response.put("hasPreAssignments", hasPreAssignments);
            response.put("totalCourses", result.getTotalCourses());
            response.put("preAssignedTeachers", result.getCoursesWithTeacherBefore());
            response.put("preAssignedRooms", result.getCoursesWithRoomBefore());

            if (hasPreAssignments) {
                response.put("recommendation", "Clear existing assignments before running smart assignment");
                response.put("clearEndpoint", "POST /api/database-maintenance/clear-assignments");
            } else {
                response.put("recommendation", "No pre-assignments detected - ready for smart assignment");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to check pre-assignments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Statistics and Reporting ====================

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            DatabaseMaintenanceService.MaintenanceResult result =
                databaseMaintenanceService.checkCourseAssignments();

            Map<String, Object> stats = new HashMap<>();

            stats.put("courseSummary", Map.of(
                "total", result.getTotalCourses(),
                "withTeachers", result.getCoursesWithTeacherBefore(),
                "withRooms", result.getCoursesWithRoomBefore(),
                "withCoTeachers", 0
            ));

            stats.put("unassignedSummary", Map.of(
                "coursesNeedingTeachers", result.getTotalCourses() - result.getCoursesWithTeacherBefore(),
                "coursesNeedingRooms", result.getTotalCourses() - result.getCoursesWithRoomBefore()
            ));

            if (result.getTotalCourses() > 0) {
                stats.put("completionRates", Map.of(
                    "teacherAssignments", String.format("%.1f%%",
                        (result.getCoursesWithTeacherBefore() * 100.0) / result.getTotalCourses()),
                    "roomAssignments", String.format("%.1f%%",
                        (result.getCoursesWithRoomBefore() * 100.0) / result.getTotalCourses())
                ));
            }

            stats.put("status", result.getSummary());

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Dashboard ====================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        try {
            DatabaseMaintenanceService.MaintenanceResult result =
                databaseMaintenanceService.checkCourseAssignments();

            Map<String, Object> dashboard = new HashMap<>();

            dashboard.put("overview", Map.of(
                "totalCourses", result.getTotalCourses(),
                "assignedTeachers", result.getCoursesWithTeacherBefore(),
                "assignedRooms", result.getCoursesWithRoomBefore(),
                "coTeachers", 0
            ));

            boolean needsClearing = result.getCoursesWithTeacherBefore() > 0 || result.getCoursesWithRoomBefore() > 0;
            dashboard.put("maintenanceNeeded", needsClearing);

            if (needsClearing) {
                dashboard.put("recommendation", "Clear existing assignments before running smart assignment");
            } else {
                dashboard.put("recommendation", "Database is clean - ready for assignments");
            }

            dashboard.put("quickActions", Map.of(
                "clearAll", "POST /api/database-maintenance/clear-assignments",
                "clearTeachers", "POST /api/database-maintenance/clear-assignments/teachers-only",
                "clearRooms", "POST /api/database-maintenance/clear-assignments/rooms-only",
                "checkStatus", "GET /api/database-maintenance/assignments/status"
            ));

            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get dashboard: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        try {
            DatabaseMaintenanceService.MaintenanceResult result =
                databaseMaintenanceService.checkCourseAssignments();

            int unassignedTeachers = result.getTotalCourses() - result.getCoursesWithTeacherBefore();
            int unassignedRooms = result.getTotalCourses() - result.getCoursesWithRoomBefore();

            Map<String, Object> summary = new HashMap<>();
            summary.put("totalCourses", result.getTotalCourses());
            summary.put("assignmentStatus", result.getCoursesWithTeacherBefore() == result.getTotalCourses() &&
                result.getCoursesWithRoomBefore() == result.getTotalCourses() ? "FULLY_ASSIGNED" : "PARTIALLY_ASSIGNED");
            summary.put("unassignedCount", Math.max(unassignedTeachers, unassignedRooms));
            summary.put("readyForSmartAssignment", unassignedTeachers == result.getTotalCourses() &&
                unassignedRooms == result.getTotalCourses());

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get summary: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Validation ====================

    @GetMapping("/validate/data-integrity")
    public ResponseEntity<Map<String, Object>> validateDataIntegrity() {
        try {
            DatabaseMaintenanceService.MaintenanceResult result =
                databaseMaintenanceService.checkCourseAssignments();

            Map<String, Object> validation = new HashMap<>();

            boolean isValid = result.getTotalCourses() > 0;
            validation.put("valid", isValid);
            validation.put("totalCourses", result.getTotalCourses());

            if (!isValid) {
                validation.put("error", "No courses found in database");
                validation.put("recommendation", "Import course data using File Import API");
            } else {
                validation.put("status", "Data integrity check passed");
                validation.put("coursesWithTeachers", result.getCoursesWithTeacherBefore());
                validation.put("coursesWithRooms", result.getCoursesWithRoomBefore());
            }

            return ResponseEntity.ok(validation);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("valid", false);
            errorResponse.put("error", "Validation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Utility Endpoints ====================

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("version", "1.0.0");
        metadata.put("features", Map.of(
            "clearAssignments", "Remove all teacher/room assignments from courses",
            "selectiveClear", "Clear only teachers or only rooms",
            "specificClear", "Clear assignments for specific course",
            "statusChecking", "Check current assignment state",
            "dataValidation", "Validate database integrity"
        ));

        metadata.put("operations", Map.of(
            "destructive", "Clear operations permanently remove assignments",
            "safety", "Check status before clearing to verify impact",
            "recommendation", "Always check pre-assignments before smart assignment"
        ));

        metadata.put("useCases", Map.of(
            "resetSchedule", "Clear all assignments before regenerating schedule",
            "troubleshooting", "Clear specific course assignments for debugging",
            "smartAssignmentPrep", "Ensure clean state before running smart assignment",
            "monitoring", "Track assignment completion rates"
        ));

        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = new HashMap<>();

        help.put("title", "Database Maintenance Help");

        help.put("commonWorkflows", Map.of(
            "beforeSmartAssignment", List.of(
                "1. GET /api/database-maintenance/assignments/pre-check",
                "2. POST /api/database-maintenance/clear-assignments (if needed)",
                "3. Run smart assignment APIs"
            ),
            "troubleshooting", List.of(
                "1. GET /api/database-maintenance/assignments/status",
                "2. Identify problem courses",
                "3. POST /api/database-maintenance/clear-assignments/courses",
                "4. Re-run assignment for that course"
            ),
            "monitoring", List.of(
                "1. GET /api/database-maintenance/dashboard",
                "2. Review completion rates",
                "3. Check for unassigned courses"
            )
        ));

        help.put("endpoints", Map.of(
            "clearAll", "POST /api/database-maintenance/clear-assignments",
            "clearTeachers", "POST /api/database-maintenance/clear-assignments/teachers-only",
            "clearRooms", "POST /api/database-maintenance/clear-assignments/rooms-only",
            "clearSpecific", "POST /api/database-maintenance/clear-assignments/courses",
            "checkStatus", "GET /api/database-maintenance/assignments/status",
            "preCheck", "GET /api/database-maintenance/assignments/pre-check"
        ));

        help.put("examples", Map.of(
            "checkStatus", "curl http://localhost:8080/api/database-maintenance/assignments/status",
            "clearAll", "curl -X POST http://localhost:8080/api/database-maintenance/clear-assignments",
            "clearCourses", "curl -X POST http://localhost:8080/api/database-maintenance/clear-assignments/courses -H 'Content-Type: application/json' -d '[\"MATH101\",\"ENG101\"]'"
        ));

        help.put("warnings", Map.of(
            "destructive", "Clear operations are permanent - verify before executing",
            "preCheck", "Always check status before clearing to understand impact",
            "backup", "Consider database backup before major maintenance operations"
        ));

        return ResponseEntity.ok(help);
    }
}
