package com.heronix.controller.api;

import com.heronix.dto.CalendarEventDTO;
import com.heronix.dto.SchoolCalendarDTO;
import com.heronix.service.SchoolCalendarService;
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
 * REST API Controller for School Calendar Management
 *
 * Provides endpoints for managing school calendars and events:
 * - School year calendar setup
 * - Holiday and break period management
 * - Calendar event management
 * - Instructional day calculations
 * - School day validation
 *
 * Supports:
 * - District-wide calendars
 * - Campus-specific calendars
 * - Multiple academic years
 *
 * Event Types: HOLIDAY, BREAK, PROFESSIONAL_DEVELOPMENT, SPECIAL_EVENT
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 31 - December 29, 2025
 */
@RestController
@RequestMapping("/api/school-calendar")
@RequiredArgsConstructor
public class SchoolCalendarApiController {

    private final SchoolCalendarService schoolCalendarService;

    // ==================== Calendar CRUD ====================

    @PostMapping("/calendars")
    public ResponseEntity<Map<String, Object>> createSchoolYear(
            @RequestBody Map<String, Object> requestBody) {

        try {
            String academicYear = (String) requestBody.get("academicYear");
            LocalDate startDate = LocalDate.parse((String) requestBody.get("startDate"));
            LocalDate endDate = LocalDate.parse((String) requestBody.get("endDate"));
            Long campusId = requestBody.get("campusId") != null ?
                Long.valueOf(requestBody.get("campusId").toString()) : null;

            SchoolCalendarDTO created = schoolCalendarService.setSchoolYear(academicYear, startDate, endDate, campusId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("calendar", created);
            response.put("message", "School calendar created successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to create calendar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/calendars/{id}")
    public ResponseEntity<Map<String, Object>> getCalendarById(@PathVariable Long id) {
        try {
            SchoolCalendarDTO calendar = schoolCalendarService.getSchoolCalendarById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("calendar", calendar);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get calendar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/calendars")
    public ResponseEntity<Map<String, Object>> getAllCalendars() {
        try {
            List<SchoolCalendarDTO> calendars = schoolCalendarService.getAllSchoolCalendars();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("calendars", calendars);
            response.put("totalCalendars", calendars.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get calendars: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/calendars/active")
    public ResponseEntity<Map<String, Object>> getActiveCalendars() {
        try {
            List<SchoolCalendarDTO> calendars = schoolCalendarService.getActiveSchoolCalendars();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("activeCalendars", calendars);
            response.put("count", calendars.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get calendars: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/calendars/current")
    public ResponseEntity<Map<String, Object>> getCurrentCalendar() {
        try {
            SchoolCalendarDTO calendar = schoolCalendarService.getCurrentCalendar();

            if (calendar == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "No current calendar found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("calendar", calendar);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get current calendar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/calendars/year/{academicYear}")
    public ResponseEntity<Map<String, Object>> getCalendarByYear(@PathVariable String academicYear) {
        try {
            List<SchoolCalendarDTO> calendars = schoolCalendarService.getCalendarForYear(academicYear);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("academicYear", academicYear);
            response.put("calendars", calendars);
            response.put("count", calendars.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get calendars: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/calendars/{id}")
    public ResponseEntity<Map<String, Object>> updateCalendar(
            @PathVariable Long id,
            @RequestBody SchoolCalendarDTO dto) {

        try {
            SchoolCalendarDTO updated = schoolCalendarService.updateSchoolCalendar(id, dto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("calendar", updated);
            response.put("message", "Calendar updated successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found or validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to update calendar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/calendars/{id}")
    public ResponseEntity<Map<String, Object>> deleteCalendar(@PathVariable Long id) {
        try {
            schoolCalendarService.deleteSchoolCalendar(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Calendar deactivated successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to delete calendar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Event Management ====================

    @PostMapping("/calendars/{calendarId}/holidays")
    public ResponseEntity<Map<String, Object>> addHoliday(
            @PathVariable Long calendarId,
            @RequestBody Map<String, Object> requestBody) {

        try {
            String name = (String) requestBody.get("name");
            LocalDate date = LocalDate.parse((String) requestBody.get("date"));

            SchoolCalendarDTO updated = schoolCalendarService.addHoliday(calendarId, name, date);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("calendar", updated);
            response.put("message", "Holiday added successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found or validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to add holiday: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/calendars/{calendarId}/breaks")
    public ResponseEntity<Map<String, Object>> addBreakPeriod(
            @PathVariable Long calendarId,
            @RequestBody Map<String, Object> requestBody) {

        try {
            String name = (String) requestBody.get("name");
            LocalDate startDate = LocalDate.parse((String) requestBody.get("startDate"));
            LocalDate endDate = LocalDate.parse((String) requestBody.get("endDate"));

            SchoolCalendarDTO updated = schoolCalendarService.addBreakPeriod(calendarId, name, startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("calendar", updated);
            response.put("message", "Break period added successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found or validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to add break: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/calendars/{calendarId}/events")
    public ResponseEntity<Map<String, Object>> addEvent(
            @PathVariable Long calendarId,
            @RequestBody CalendarEventDTO eventDTO) {

        try {
            SchoolCalendarDTO updated = schoolCalendarService.addEvent(calendarId, eventDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("calendar", updated);
            response.put("message", "Event added successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found or validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to add event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/calendars/{calendarId}/events/{eventId}")
    public ResponseEntity<Map<String, Object>> removeEvent(
            @PathVariable Long calendarId,
            @PathVariable Long eventId) {

        try {
            SchoolCalendarDTO updated = schoolCalendarService.removeEvent(calendarId, eventId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("calendar", updated);
            response.put("message", "Event removed successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to remove event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Instructional Days ====================

    @GetMapping("/calendars/{calendarId}/instructional-days")
    public ResponseEntity<Map<String, Object>> getInstructionalDays(
            @PathVariable Long calendarId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            int instructionalDays = schoolCalendarService.getInstructionalDays(calendarId, startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("calendarId", calendarId);
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("instructionalDays", instructionalDays);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to calculate instructional days: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/calendars/{calendarId}/is-school-day")
    public ResponseEntity<Map<String, Object>> isSchoolDay(
            @PathVariable Long calendarId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        try {
            boolean isSchoolDay = schoolCalendarService.isSchoolDay(calendarId, date);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("calendarId", calendarId);
            response.put("date", date);
            response.put("isSchoolDay", isSchoolDay);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to check school day: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Metadata ====================

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("apiVersion", "1.0.0");
        metadata.put("phase", "Phase 31");
        metadata.put("category", "School Calendar Management");
        metadata.put("description", "Comprehensive school calendar and event management");

        metadata.put("capabilities", List.of(
            "School year calendar setup",
            "Holiday and break period management",
            "Calendar event management",
            "Instructional day calculations",
            "School day validation",
            "District-wide and campus-specific calendars"
        ));

        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = new HashMap<>();
        help.put("description", "School Calendar Management API");

        help.put("commonWorkflows", Map.of(
            "setupSchoolYear", List.of(
                "1. POST /api/school-calendar/calendars",
                "2. POST /api/school-calendar/calendars/{id}/holidays",
                "3. POST /api/school-calendar/calendars/{id}/breaks"
            ),
            "checkInstructionalDays", List.of(
                "1. GET /api/school-calendar/calendars/current",
                "2. GET /api/school-calendar/calendars/{id}/instructional-days"
            )
        ));

        help.put("endpoints", Map.of(
            "createCalendar", "POST /api/school-calendar/calendars",
            "getCurrent", "GET /api/school-calendar/calendars/current",
            "addHoliday", "POST /api/school-calendar/calendars/{id}/holidays",
            "addBreak", "POST /api/school-calendar/calendars/{id}/breaks",
            "getInstructionalDays", "GET /api/school-calendar/calendars/{id}/instructional-days"
        ));

        return ResponseEntity.ok(help);
    }
}
