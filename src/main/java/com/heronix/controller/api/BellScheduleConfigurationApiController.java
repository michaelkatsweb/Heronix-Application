package com.heronix.controller.api;

import com.heronix.dto.BellScheduleDTO;
import com.heronix.dto.PeriodTimerDTO;
import com.heronix.service.BellScheduleConfigurationService;
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
 * REST API Controller for Bell Schedule Configuration
 *
 * Manages bell schedule creation, modification, and period configuration.
 * Provides endpoints for:
 * - Creating and updating bell schedules
 * - Managing period timers within schedules
 * - Querying schedules by campus, date, or academic year
 * - Setting default schedules
 * - Managing special day schedules (early dismissal, late start, etc.)
 *
 * Schedule Types:
 * - REGULAR: Standard daily schedule
 * - EARLY_DISMISSAL: Shortened day schedule
 * - LATE_START: Delayed start schedule
 * - BLOCK: Block scheduling (alternating periods)
 * - ASSEMBLY: Special assembly schedule
 * - TESTING: Modified schedule for testing days
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/bell-schedule-configuration")
@RequiredArgsConstructor
public class BellScheduleConfigurationApiController {

    private final BellScheduleConfigurationService bellScheduleConfigurationService;

    // ==================== Schedule Creation & Updates ====================

    @PostMapping("/schedules")
    public ResponseEntity<Map<String, Object>> createBellSchedule(@RequestBody BellScheduleDTO dto) {
        try {
            BellScheduleDTO created = bellScheduleConfigurationService.createBellSchedule(dto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("schedule", created);
            response.put("message", "Bell schedule created successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/schedules/{id}")
    public ResponseEntity<Map<String, Object>> updateBellSchedule(
            @PathVariable Long id,
            @RequestBody BellScheduleDTO dto) {

        try {
            BellScheduleDTO updated = bellScheduleConfigurationService.updateBellSchedule(id, dto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("schedule", updated);
            response.put("message", "Bell schedule updated successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/schedules/{id}")
    public ResponseEntity<Map<String, Object>> deleteBellSchedule(@PathVariable Long id) {
        try {
            bellScheduleConfigurationService.deleteBellSchedule(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Bell schedule deleted successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Period Management ====================

    @PostMapping("/schedules/{scheduleId}/periods")
    public ResponseEntity<Map<String, Object>> addPeriod(
            @PathVariable Long scheduleId,
            @RequestBody PeriodTimerDTO periodDTO) {

        try {
            BellScheduleDTO updated = bellScheduleConfigurationService.addPeriodToBellSchedule(scheduleId, periodDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("schedule", updated);
            response.put("message", "Period added successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/schedules/{scheduleId}/periods/{periodId}")
    public ResponseEntity<Map<String, Object>> removePeriod(
            @PathVariable Long scheduleId,
            @PathVariable Long periodId) {

        try {
            BellScheduleDTO updated = bellScheduleConfigurationService.removePeriodFromBellSchedule(scheduleId, periodId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("schedule", updated);
            response.put("message", "Period removed successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Schedule Queries ====================

    @GetMapping("/schedules/{id}")
    public ResponseEntity<Map<String, Object>> getBellScheduleById(@PathVariable Long id) {
        try {
            BellScheduleDTO schedule = bellScheduleConfigurationService.getBellScheduleById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("schedule", schedule);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/schedules")
    public ResponseEntity<Map<String, Object>> getAllBellSchedules() {
        try {
            List<BellScheduleDTO> schedules = bellScheduleConfigurationService.getAllBellSchedules();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("schedules", schedules);
            response.put("count", schedules.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/schedules/active")
    public ResponseEntity<Map<String, Object>> getActiveBellSchedules() {
        try {
            List<BellScheduleDTO> schedules = bellScheduleConfigurationService.getActiveBellSchedules();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("schedules", schedules);
            response.put("count", schedules.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/schedules/for-date")
    public ResponseEntity<Map<String, Object>> getBellScheduleForDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Long campusId) {

        try {
            BellScheduleDTO schedule = bellScheduleConfigurationService.getBellScheduleForDate(date, campusId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("date", date);
            response.put("campusId", campusId);
            response.put("schedule", schedule);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "No schedule found for the specified date and campus");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @GetMapping("/schedules/by-campus/{campusId}")
    public ResponseEntity<Map<String, Object>> getBellSchedulesByCampus(@PathVariable Long campusId) {
        try {
            List<BellScheduleDTO> schedules = bellScheduleConfigurationService.getBellSchedulesByCampus(campusId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("campusId", campusId);
            response.put("schedules", schedules);
            response.put("count", schedules.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/schedules/by-academic-year/{academicYearId}")
    public ResponseEntity<Map<String, Object>> getBellSchedulesByAcademicYear(@PathVariable Long academicYearId) {
        try {
            List<BellScheduleDTO> schedules = bellScheduleConfigurationService.getBellSchedulesByAcademicYear(academicYearId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("academicYearId", academicYearId);
            response.put("schedules", schedules);
            response.put("count", schedules.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Dashboard ====================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        try {
            List<BellScheduleDTO> allSchedules = bellScheduleConfigurationService.getAllBellSchedules();
            List<BellScheduleDTO> activeSchedules = bellScheduleConfigurationService.getActiveBellSchedules();

            long defaultSchedules = allSchedules.stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsDefault()))
                .count();

            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("totalSchedules", allSchedules.size());
            dashboard.put("activeSchedules", activeSchedules.size());
            dashboard.put("inactiveSchedules", allSchedules.size() - activeSchedules.size());
            dashboard.put("defaultSchedules", defaultSchedules);

            // Count by type
            Map<String, Long> byType = new HashMap<>();
            for (BellScheduleDTO schedule : allSchedules) {
                String type = schedule.getScheduleType() != null ? schedule.getScheduleType().toString() : "REGULAR";
                byType.put(type, byType.getOrDefault(type, 0L) + 1);
            }
            dashboard.put("schedulesByType", byType);

            dashboard.put("quickActions", Map.of(
                "createSchedule", "POST /api/bell-schedule-configuration/schedules",
                "viewActive", "GET /api/bell-schedule-configuration/schedules/active",
                "checkToday", "GET /api/bell-schedule-configuration/schedules/for-date?date=YYYY-MM-DD&campusId=1"
            ));

            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Configuration Reference ====================

    @GetMapping("/reference/schedule-types")
    public ResponseEntity<Map<String, Object>> getScheduleTypes() {
        Map<String, Object> types = new HashMap<>();

        types.put("REGULAR", Map.of(
            "name", "Regular Schedule",
            "description", "Standard daily schedule",
            "usageExample", "Monday-Friday normal school days"
        ));

        types.put("EARLY_DISMISSAL", Map.of(
            "name", "Early Dismissal",
            "description", "Shortened day schedule",
            "usageExample", "Staff development days, before holidays"
        ));

        types.put("LATE_START", Map.of(
            "name", "Late Start",
            "description", "Delayed start schedule",
            "usageExample", "Weekly collaboration time"
        ));

        types.put("BLOCK", Map.of(
            "name", "Block Schedule",
            "description", "Extended periods, alternating days",
            "usageExample", "A/B day rotation"
        ));

        types.put("ASSEMBLY", Map.of(
            "name", "Assembly Schedule",
            "description", "Modified schedule for assemblies",
            "usageExample", "Pep rallies, special events"
        ));

        types.put("TESTING", Map.of(
            "name", "Testing Schedule",
            "description", "Modified schedule for standardized testing",
            "usageExample", "State testing days"
        ));

        return ResponseEntity.ok(types);
    }

    @GetMapping("/reference/best-practices")
    public ResponseEntity<Map<String, Object>> getBestPractices() {
        Map<String, Object> practices = new HashMap<>();

        practices.put("defaultSchedules", Map.of(
            "recommendation", "Set one default schedule per campus",
            "reason", "Ensures system always has a fallback schedule",
            "warning", "Only one schedule can be default per campus"
        ));

        practices.put("periodConfiguration", Map.of(
            "recommendation", "Configure periods in chronological order",
            "reason", "Easier to read and maintain",
            "tip", "Use consistent period numbering across schedules"
        ));

        practices.put("specialDaySchedules", Map.of(
            "recommendation", "Create named schedules for recurring special days",
            "examples", List.of("Early Dismissal - Fridays", "Late Start - Wednesdays", "Testing Schedule"),
            "reason", "Reusable and easier to apply"
        ));

        practices.put("daysOfWeek", Map.of(
            "format", "Comma-separated: MON,TUE,WED,THU,FRI",
            "examples", List.of(
                "MON,TUE,WED,THU,FRI (weekdays)",
                "MON,WED,FRI (A days)",
                "TUE,THU (B days)"
            )
        ));

        return ResponseEntity.ok(practices);
    }

    // ==================== Utility Endpoints ====================

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("version", "1.0.0");
        metadata.put("features", Map.of(
            "multipleSchedules", "Support for multiple bell schedules per campus",
            "defaultSchedule", "Default schedule per campus",
            "specialDaySchedules", "Early dismissal, late start, block, assembly, testing",
            "periodManagement", "Add/remove periods dynamically",
            "dateSpecificSchedules", "Apply specific schedules to specific dates",
            "academicYearSchedules", "Associate schedules with academic years"
        ));

        metadata.put("scheduleTypes", List.of(
            "REGULAR",
            "EARLY_DISMISSAL",
            "LATE_START",
            "BLOCK",
            "ASSEMBLY",
            "TESTING"
        ));

        metadata.put("integrations", Map.of(
            "attendanceTracking", "Bell schedules drive period-based attendance",
            "academicCalendar", "Schedules associated with academic years",
            "campusManagement", "Per-campus schedule configuration"
        ));

        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = new HashMap<>();

        help.put("title", "Bell Schedule Configuration Help");

        help.put("workflows", Map.of(
            "createNewSchedule", List.of(
                "1. Define schedule details (name, type, days of week)",
                "2. POST /api/bell-schedule-configuration/schedules",
                "3. Add periods: POST /api/bell-schedule-configuration/schedules/{id}/periods",
                "4. Set as default if needed (isDefault: true in create/update)"
            ),
            "configureBlockSchedule", List.of(
                "1. Create two schedules: 'A Day' and 'B Day'",
                "2. Set daysOfWeek for each (e.g., MON,WED,FRI vs TUE,THU)",
                "3. Configure different periods for each schedule",
                "4. System will automatically apply correct schedule based on day"
            ),
            "specialDaySetup", List.of(
                "1. Create schedule with appropriate type (EARLY_DISMISSAL, LATE_START, etc.)",
                "2. Add shortened or modified periods",
                "3. Apply to specific dates using specificDates field",
                "4. System will use this schedule on specified dates"
            )
        ));

        help.put("endpoints", Map.of(
            "create", "POST /api/bell-schedule-configuration/schedules",
            "update", "PUT /api/bell-schedule-configuration/schedules/{id}",
            "delete", "DELETE /api/bell-schedule-configuration/schedules/{id}",
            "addPeriod", "POST /api/bell-schedule-configuration/schedules/{id}/periods",
            "viewAll", "GET /api/bell-schedule-configuration/schedules",
            "checkDate", "GET /api/bell-schedule-configuration/schedules/for-date?date=YYYY-MM-DD&campusId=ID"
        ));

        help.put("examples", Map.of(
            "viewAllSchedules", "curl http://localhost:9590/api/bell-schedule-configuration/schedules",
            "checkTodaySchedule", "curl 'http://localhost:9590/api/bell-schedule-configuration/schedules/for-date?date=2025-01-15&campusId=1'",
            "dashboard", "curl http://localhost:9590/api/bell-schedule-configuration/dashboard"
        ));

        return ResponseEntity.ok(help);
    }
}
