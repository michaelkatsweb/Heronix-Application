package com.heronix.controller.api;

import com.heronix.model.dto.CleanupResult;
import com.heronix.model.dto.DatabaseStats;
import com.heronix.service.DataManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API Controller for Data Management
 *
 * IMPORTANT: These endpoints perform destructive operations.
 * Use with extreme caution and ensure proper authorization.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/data-management")
@RequiredArgsConstructor
public class DataManagementApiController {

    private final DataManagementService dataManagementService;

    // ==================== Bulk Delete Operations ====================

    @DeleteMapping("/students/all")
    public ResponseEntity<CleanupResult> deleteAllStudents() {
        CleanupResult result = dataManagementService.deleteAllStudents();
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/teachers/all")
    public ResponseEntity<CleanupResult> deleteAllTeachers() {
        CleanupResult result = dataManagementService.deleteAllTeachers();
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/courses/all")
    public ResponseEntity<CleanupResult> deleteAllCourses() {
        CleanupResult result = dataManagementService.deleteAllCourses();
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/rooms/all")
    public ResponseEntity<CleanupResult> deleteAllRooms() {
        CleanupResult result = dataManagementService.deleteAllRooms();
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/events/all")
    public ResponseEntity<CleanupResult> deleteAllEvents() {
        CleanupResult result = dataManagementService.deleteAllEvents();
        return ResponseEntity.ok(result);
    }

    // ==================== Selective Delete Operations ====================

    @DeleteMapping("/students/grade-level/{gradeLevel}")
    public ResponseEntity<CleanupResult> deleteStudentsByGradeLevel(@PathVariable String gradeLevel) {
        CleanupResult result = dataManagementService.deleteStudentsByGradeLevel(gradeLevel);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/teachers/department/{department}")
    public ResponseEntity<CleanupResult> deleteTeachersByDepartment(@PathVariable String department) {
        CleanupResult result = dataManagementService.deleteTeachersByDepartment(department);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/students/inactive")
    public ResponseEntity<CleanupResult> deleteInactiveStudents() {
        CleanupResult result = dataManagementService.deleteInactiveStudents();
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/teachers/inactive")
    public ResponseEntity<CleanupResult> deleteInactiveTeachers() {
        CleanupResult result = dataManagementService.deleteInactiveTeachers();
        return ResponseEntity.ok(result);
    }

    // ==================== Statistics ====================

    @GetMapping("/stats")
    public ResponseEntity<DatabaseStats> getDatabaseStats() {
        DatabaseStats stats = dataManagementService.getDatabaseStats();
        return ResponseEntity.ok(stats);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getDataManagementDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        DatabaseStats stats = dataManagementService.getDatabaseStats();

        dashboard.put("databaseStats", stats);
        dashboard.put("totalRecords", stats.getTotalRecords());

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/cleanup-candidates")
    public ResponseEntity<Map<String, Object>> getCleanupCandidates() {
        Map<String, Object> dashboard = new HashMap<>();

        // Note: DatabaseStats doesn't have inactive counts
        dashboard.put("message", "Cleanup candidates tracking not yet implemented");

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/stats-summary")
    public ResponseEntity<Map<String, Object>> getStatsSummary() {
        Map<String, Object> dashboard = new HashMap<>();

        DatabaseStats stats = dataManagementService.getDatabaseStats();

        dashboard.put("students", Map.of(
            "total", stats.getStudentCount()
        ));

        dashboard.put("teachers", Map.of(
            "total", stats.getTeacherCount()
        ));

        dashboard.put("courses", Map.of(
            "total", stats.getCourseCount()
        ));

        dashboard.put("rooms", Map.of(
            "total", stats.getRoomCount()
        ));

        dashboard.put("events", Map.of(
            "total", stats.getEventCount()
        ));

        dashboard.put("schedules", Map.of(
            "total", stats.getScheduleCount()
        ));

        dashboard.put("scheduleSlots", Map.of(
            "total", stats.getScheduleSlotCount()
        ));

        dashboard.put("totalRecords", stats.getTotalRecords());
        dashboard.put("lastUpdated", stats.getLastUpdated());

        return ResponseEntity.ok(dashboard);
    }
}
