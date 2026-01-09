package com.heronix.controller;

import com.heronix.model.domain.ReportSchedule;
import com.heronix.service.DynamicScheduledReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Report Schedule API Controller
 *
 * Provides REST API endpoints for managing automated report schedules.
 *
 * Endpoints:
 * - GET /api/reports/schedules - List all schedules
 * - GET /api/reports/schedules/{id} - Get schedule by ID
 * - POST /api/reports/schedules - Create new schedule
 * - PUT /api/reports/schedules/{id} - Update schedule
 * - DELETE /api/reports/schedules/{id} - Delete schedule
 * - POST /api/reports/schedules/{id}/execute - Manually execute schedule
 * - GET /api/reports/schedules/active - Get active schedules
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 57 - Dynamic Scheduled Report Generation
 */
@RestController
@RequestMapping("/api/reports/schedules")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ReportScheduleApiController {

    private final DynamicScheduledReportService scheduleService;

    /**
     * Get all report schedules
     *
     * GET /api/reports/schedules
     *
     * @return List of all schedules
     */
    @GetMapping
    public ResponseEntity<List<ReportSchedule>> getAllSchedules() {
        try {
            List<ReportSchedule> schedules = scheduleService.getAllSchedules();
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            log.error("Error retrieving all schedules", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get active report schedules
     *
     * GET /api/reports/schedules/active
     *
     * @return List of active schedules
     */
    @GetMapping("/active")
    public ResponseEntity<List<ReportSchedule>> getActiveSchedules() {
        try {
            List<ReportSchedule> schedules = scheduleService.getActiveSchedules();
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            log.error("Error retrieving active schedules", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get schedule by ID
     *
     * GET /api/reports/schedules/{id}
     *
     * @param id Schedule ID
     * @return Schedule
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportSchedule> getScheduleById(@PathVariable Long id) {
        try {
            ReportSchedule schedule = scheduleService.getScheduleById(id);
            return ResponseEntity.ok(schedule);
        } catch (IllegalArgumentException e) {
            log.warn("Schedule not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving schedule: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create new report schedule
     *
     * POST /api/reports/schedules
     *
     * @param schedule Schedule data
     * @return Created schedule
     */
    @PostMapping
    public ResponseEntity<ReportSchedule> createSchedule(@RequestBody ReportSchedule schedule) {
        try {
            log.info("Creating new report schedule: {}", schedule.getName());
            ReportSchedule created = scheduleService.createSchedule(schedule);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("Error creating schedule", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update existing report schedule
     *
     * PUT /api/reports/schedules/{id}
     *
     * @param id Schedule ID
     * @param schedule Updated schedule data
     * @return Updated schedule
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReportSchedule> updateSchedule(
            @PathVariable Long id,
            @RequestBody ReportSchedule schedule) {
        try {
            log.info("Updating report schedule: {}", id);
            ReportSchedule updated = scheduleService.updateSchedule(id, schedule);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.warn("Schedule not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating schedule: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete report schedule
     *
     * DELETE /api/reports/schedules/{id}
     *
     * @param id Schedule ID
     * @return No content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        try {
            log.info("Deleting report schedule: {}", id);
            scheduleService.deleteSchedule(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting schedule: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Manually execute a schedule (trigger immediately)
     *
     * POST /api/reports/schedules/{id}/execute
     *
     * @param id Schedule ID
     * @return Success message
     */
    @PostMapping("/{id}/execute")
    public ResponseEntity<String> executeSchedule(@PathVariable Long id) {
        try {
            log.info("Manually executing schedule: {}", id);
            scheduleService.executeScheduleManually(id);
            return ResponseEntity.ok("Schedule executed successfully");
        } catch (IllegalArgumentException e) {
            log.warn("Schedule not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error executing schedule: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error executing schedule: " + e.getMessage());
        }
    }

    /**
     * Health check endpoint
     *
     * GET /api/reports/schedules/health
     *
     * @return OK status
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Report Schedule API is running");
    }
}
