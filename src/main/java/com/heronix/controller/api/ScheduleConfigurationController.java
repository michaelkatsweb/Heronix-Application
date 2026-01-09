package com.heronix.controller.api;

import com.heronix.dto.*;
import com.heronix.service.BellScheduleConfigurationService;
import com.heronix.service.GradingPeriodManagementService;
import com.heronix.service.SchoolCalendarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Schedule Configuration
 * Provides endpoints for managing grading periods, bell schedules, and school calendars
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@RestController
@RequestMapping("/api/schedule-config")
@RequiredArgsConstructor
public class ScheduleConfigurationController {

    private final GradingPeriodManagementService gradingPeriodService;
    private final BellScheduleConfigurationService bellScheduleService;
    private final SchoolCalendarService schoolCalendarService;

    // ========================================================================
    // GRADING PERIOD ENDPOINTS
    // ========================================================================

    /**
     * Create a new grading period
     */
    @PostMapping("/grading-periods")
    public ResponseEntity<Map<String, Object>> createGradingPeriod(@RequestBody GradingPeriodDTO dto) {
        try {
            log.info("API: Creating grading period: {}", dto.getName());
            GradingPeriodDTO created = gradingPeriodService.createGradingPeriod(dto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Grading period created successfully");
            response.put("data", created);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Validation error creating grading period", e);
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error creating grading period", e);
            return createErrorResponse("Failed to create grading period: " + e.getMessage(),
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update an existing grading period
     */
    @PutMapping("/grading-periods/{id}")
    public ResponseEntity<Map<String, Object>> updateGradingPeriod(
            @PathVariable Long id,
            @RequestBody GradingPeriodDTO dto) {
        try {
            log.info("API: Updating grading period ID: {}", id);
            GradingPeriodDTO updated = gradingPeriodService.updateGradingPeriod(id, dto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Grading period updated successfully");
            response.put("data", updated);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Validation error updating grading period", e);
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error updating grading period", e);
            return createErrorResponse("Failed to update grading period: " + e.getMessage(),
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete a grading period
     */
    @DeleteMapping("/grading-periods/{id}")
    public ResponseEntity<Map<String, Object>> deleteGradingPeriod(@PathVariable Long id) {
        try {
            log.info("API: Deleting grading period ID: {}", id);
            gradingPeriodService.deleteGradingPeriod(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Grading period deleted successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Error deleting grading period", e);
            return createErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error deleting grading period", e);
            return createErrorResponse("Failed to delete grading period: " + e.getMessage(),
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get grading period by ID
     */
    @GetMapping("/grading-periods/{id}")
    public ResponseEntity<Map<String, Object>> getGradingPeriodById(@PathVariable Long id) {
        try {
            GradingPeriodDTO period = gradingPeriodService.getGradingPeriodById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", period);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return createErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error getting grading period", e);
            return createErrorResponse("Failed to get grading period: " + e.getMessage(),
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get grading periods for an academic year
     */
    @GetMapping("/grading-periods/year/{academicYear}")
    public ResponseEntity<Map<String, Object>> getGradingPeriodsForYear(
            @PathVariable String academicYear) {
        try {
            List<GradingPeriodDTO> periods = gradingPeriodService.getGradingPeriodsForYear(academicYear);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", periods);
            response.put("count", periods.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting grading periods for year", e);
            return createErrorResponse("Failed to get grading periods: " + e.getMessage(),
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all grading periods
     */
    @GetMapping("/grading-periods")
    public ResponseEntity<Map<String, Object>> getAllGradingPeriods() {
        try {
            List<GradingPeriodDTO> periods = gradingPeriodService.getAllGradingPeriods();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", periods);
            response.put("count", periods.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting all grading periods", e);
            return createErrorResponse("Failed to get grading periods: " + e.getMessage(),
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get current grading period
     */
    @GetMapping("/grading-periods/current")
    public ResponseEntity<Map<String, Object>> getCurrentGradingPeriod() {
        try {
            GradingPeriodDTO period = gradingPeriodService.getCurrentGradingPeriod();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", period);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting current grading period", e);
            return createErrorResponse("Failed to get current grading period: " + e.getMessage(),
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========================================================================
    // BELL SCHEDULE ENDPOINTS
    // ========================================================================

    /**
     * Create a new bell schedule
     */
    @PostMapping("/bell-schedules")
    public ResponseEntity<Map<String, Object>> createBellSchedule(@RequestBody BellScheduleDTO dto) {
        try {
            log.info("API: Creating bell schedule: {}", dto.getName());
            BellScheduleDTO created = bellScheduleService.createBellSchedule(dto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Bell schedule created successfully");
            response.put("data", created);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Validation error creating bell schedule", e);
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error creating bell schedule", e);
            return createErrorResponse("Failed to create bell schedule: " + e.getMessage(),
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update an existing bell schedule
     */
    @PutMapping("/bell-schedules/{id}")
    public ResponseEntity<Map<String, Object>> updateBellSchedule(
            @PathVariable Long id,
            @RequestBody BellScheduleDTO dto) {
        try {
            log.info("API: Updating bell schedule ID: {}", id);
            BellScheduleDTO updated = bellScheduleService.updateBellSchedule(id, dto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Bell schedule updated successfully");
            response.put("data", updated);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Validation error updating bell schedule", e);
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error updating bell schedule", e);
            return createErrorResponse("Failed to update bell schedule: " + e.getMessage(),
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete a bell schedule
     */
    @DeleteMapping("/bell-schedules/{id}")
    public ResponseEntity<Map<String, Object>> deleteBellSchedule(@PathVariable Long id) {
        try {
            log.info("API: Deleting bell schedule ID: {}", id);
            bellScheduleService.deleteBellSchedule(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Bell schedule deleted successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Error deleting bell schedule", e);
            return createErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error deleting bell schedule", e);
            return createErrorResponse("Failed to delete bell schedule: " + e.getMessage(),
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get bell schedule by ID
     */
    @GetMapping("/bell-schedules/{id}")
    public ResponseEntity<Map<String, Object>> getBellScheduleById(@PathVariable Long id) {
        try {
            BellScheduleDTO schedule = bellScheduleService.getBellScheduleById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", schedule);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return createErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error getting bell schedule", e);
            return createErrorResponse("Failed to get bell schedule: " + e.getMessage(),
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all bell schedules
     */
    @GetMapping("/bell-schedules")
    public ResponseEntity<Map<String, Object>> getAllBellSchedules() {
        try {
            List<BellScheduleDTO> schedules = bellScheduleService.getAllBellSchedules();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", schedules);
            response.put("count", schedules.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting all bell schedules", e);
            return createErrorResponse("Failed to get bell schedules: " + e.getMessage(),
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get bell schedule for a specific date
     */
    @GetMapping("/bell-schedules/for-date")
    public ResponseEntity<Map<String, Object>> getBellScheduleForDate(
            @RequestParam String date,
            @RequestParam(required = false) Long campusId) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            BellScheduleDTO schedule = bellScheduleService.getBellScheduleForDate(localDate, campusId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", schedule);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting bell schedule for date", e);
            return createErrorResponse("Failed to get bell schedule: " + e.getMessage(),
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Add a period to a bell schedule
     */
    @PostMapping("/bell-schedules/{scheduleId}/periods")
    public ResponseEntity<Map<String, Object>> addPeriodToBellSchedule(
            @PathVariable Long scheduleId,
            @RequestBody PeriodTimerDTO periodDTO) {
        try {
            log.info("API: Adding period to bell schedule ID: {}", scheduleId);
            BellScheduleDTO updated = bellScheduleService.addPeriodToBellSchedule(scheduleId, periodDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Period added to bell schedule successfully");
            response.put("data", updated);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Validation error adding period", e);
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error adding period to bell schedule", e);
            return createErrorResponse("Failed to add period: " + e.getMessage(),
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Remove a period from a bell schedule
     */
    @DeleteMapping("/bell-schedules/{scheduleId}/periods/{periodId}")
    public ResponseEntity<Map<String, Object>> removePeriodFromBellSchedule(
            @PathVariable Long scheduleId,
            @PathVariable Long periodId) {
        try {
            log.info("API: Removing period {} from bell schedule ID: {}", periodId, scheduleId);
            BellScheduleDTO updated = bellScheduleService.removePeriodFromBellSchedule(scheduleId, periodId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Period removed from bell schedule successfully");
            response.put("data", updated);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Error removing period", e);
            return createErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error removing period from bell schedule", e);
            return createErrorResponse("Failed to remove period: " + e.getMessage(),
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========================================================================
    // SCHOOL CALENDAR ENDPOINTS
    // ========================================================================

    /**
     * Create a new school calendar (set school year)
     */
    @PostMapping("/calendar")
    public ResponseEntity<Map<String, Object>> createSchoolCalendar(@RequestBody SchoolCalendarDTO dto) {
        try {
            log.info("API: Creating school calendar for year: {}", dto.getAcademicYear());
            SchoolCalendarDTO created = schoolCalendarService.setSchoolYear(
                    dto.getAcademicYear(),
                    dto.getStartDate(),
                    dto.getEndDate(),
                    dto.getCampusId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "School calendar created successfully");
            response.put("data", created);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Validation error creating school calendar", e);
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error creating school calendar", e);
            return createErrorResponse("Failed to create school calendar: " + e.getMessage(),
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update an existing school calendar
     */
    @PutMapping("/calendar/{id}")
    public ResponseEntity<Map<String, Object>> updateSchoolCalendar(
            @PathVariable Long id,
            @RequestBody SchoolCalendarDTO dto) {
        try {
            log.info("API: Updating school calendar ID: {}", id);
            SchoolCalendarDTO updated = schoolCalendarService.updateSchoolCalendar(id, dto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "School calendar updated successfully");
            response.put("data", updated);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Validation error updating school calendar", e);
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error updating school calendar", e);
            return createErrorResponse("Failed to update school calendar: " + e.getMessage(),
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete a school calendar
     */
    @DeleteMapping("/calendar/{id}")
    public ResponseEntity<Map<String, Object>> deleteSchoolCalendar(@PathVariable Long id) {
        try {
            log.info("API: Deleting school calendar ID: {}", id);
            schoolCalendarService.deleteSchoolCalendar(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "School calendar deleted successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Error deleting school calendar", e);
            return createErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error deleting school calendar", e);
            return createErrorResponse("Failed to delete school calendar: " + e.getMessage(),
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get school calendar by ID
     */
    @GetMapping("/calendar/{id}")
    public ResponseEntity<Map<String, Object>> getSchoolCalendarById(@PathVariable Long id) {
        try {
            SchoolCalendarDTO calendar = schoolCalendarService.getSchoolCalendarById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", calendar);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return createErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error getting school calendar", e);
            return createErrorResponse("Failed to get school calendar: " + e.getMessage(),
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get calendar for an academic year
     */
    @GetMapping("/calendar/year/{academicYear}")
    public ResponseEntity<Map<String, Object>> getCalendarForYear(@PathVariable String academicYear) {
        try {
            List<SchoolCalendarDTO> calendars = schoolCalendarService.getCalendarForYear(academicYear);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", calendars);
            response.put("count", calendars.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting calendar for year", e);
            return createErrorResponse("Failed to get calendar: " + e.getMessage(),
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get current school calendar
     */
    @GetMapping("/calendar/current")
    public ResponseEntity<Map<String, Object>> getCurrentCalendar(
            @RequestParam(required = false) Long campusId) {
        try {
            SchoolCalendarDTO calendar = campusId != null
                    ? schoolCalendarService.getCurrentCalendarForCampus(campusId)
                    : schoolCalendarService.getCurrentCalendar();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", calendar);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting current calendar", e);
            return createErrorResponse("Failed to get current calendar: " + e.getMessage(),
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Add a holiday to the calendar
     */
    @PostMapping("/calendar/{calendarId}/holidays")
    public ResponseEntity<Map<String, Object>> addHoliday(
            @PathVariable Long calendarId,
            @RequestBody CalendarEventDTO eventDTO) {
        try {
            log.info("API: Adding holiday to calendar ID: {}", calendarId);
            SchoolCalendarDTO updated = schoolCalendarService.addHoliday(
                    calendarId,
                    eventDTO.getEventName(),
                    eventDTO.getEventDate());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Holiday added successfully");
            response.put("data", updated);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Validation error adding holiday", e);
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error adding holiday", e);
            return createErrorResponse("Failed to add holiday: " + e.getMessage(),
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Add a break period to the calendar
     */
    @PostMapping("/calendar/{calendarId}/breaks")
    public ResponseEntity<Map<String, Object>> addBreakPeriod(
            @PathVariable Long calendarId,
            @RequestBody CalendarEventDTO eventDTO) {
        try {
            log.info("API: Adding break period to calendar ID: {}", calendarId);
            SchoolCalendarDTO updated = schoolCalendarService.addBreakPeriod(
                    calendarId,
                    eventDTO.getEventName(),
                    eventDTO.getStartDate(),
                    eventDTO.getEndDate());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Break period added successfully");
            response.put("data", updated);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Validation error adding break period", e);
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error adding break period", e);
            return createErrorResponse("Failed to add break period: " + e.getMessage(),
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Add a calendar event
     */
    @PostMapping("/calendar/{calendarId}/events")
    public ResponseEntity<Map<String, Object>> addCalendarEvent(
            @PathVariable Long calendarId,
            @RequestBody CalendarEventDTO eventDTO) {
        try {
            log.info("API: Adding event to calendar ID: {}", calendarId);
            SchoolCalendarDTO updated = schoolCalendarService.addEvent(calendarId, eventDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Event added successfully");
            response.put("data", updated);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Validation error adding event", e);
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error adding event", e);
            return createErrorResponse("Failed to add event: " + e.getMessage(),
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Remove an event from the calendar
     */
    @DeleteMapping("/calendar/{calendarId}/events/{eventId}")
    public ResponseEntity<Map<String, Object>> removeCalendarEvent(
            @PathVariable Long calendarId,
            @PathVariable Long eventId) {
        try {
            log.info("API: Removing event {} from calendar ID: {}", eventId, calendarId);
            SchoolCalendarDTO updated = schoolCalendarService.removeEvent(calendarId, eventId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Event removed successfully");
            response.put("data", updated);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Error removing event", e);
            return createErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error removing event", e);
            return createErrorResponse("Failed to remove event: " + e.getMessage(),
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Create error response
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);

        return ResponseEntity.status(status).body(response);
    }
}
