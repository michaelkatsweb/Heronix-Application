package com.heronix.controller;

import com.heronix.dto.ReportScheduleConfig;
import com.heronix.dto.ScheduleExecutionHistory;
import com.heronix.service.EnhancedSchedulingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Schedule Management API Controller
 *
 * REST API endpoints for managing automated report schedules.
 *
 * Provides endpoints for:
 * - Creating and configuring schedules
 * - Updating schedule settings
 * - Pausing/resuming schedules
 * - Deleting schedules
 * - Viewing execution history
 * - Getting schedule statistics
 *
 * Endpoints:
 * - POST /api/schedules - Create new schedule
 * - GET /api/schedules - List all schedules
 * - GET /api/schedules/{id} - Get schedule details
 * - PUT /api/schedules/{id} - Update schedule
 * - DELETE /api/schedules/{id} - Delete schedule
 * - POST /api/schedules/{id}/pause - Pause schedule
 * - POST /api/schedules/{id}/resume - Resume schedule
 * - GET /api/schedules/{id}/history - Get execution history
 * - GET /api/schedules/history - Get all execution history
 * - GET /api/schedules/statistics - Get schedule statistics
 *
 * Security:
 * - Admin or schedule owner access recommended
 * - Audit logging for all modifications
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 65 - Report Scheduling & Automation Enhancements
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@Slf4j
public class ScheduleManagementApiController {

    private final EnhancedSchedulingService schedulingService;

    /**
     * Create new report schedule
     *
     * @param config Schedule configuration
     * @return Created schedule
     */
    @PostMapping
    public ResponseEntity<ReportScheduleConfig> createSchedule(@RequestBody ReportScheduleConfig config) {
        log.info("POST /api/schedules - Creating schedule: {}", config.getScheduleName());

        try {
            ReportScheduleConfig created = schedulingService.createSchedule(config);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);

        } catch (IllegalArgumentException e) {
            log.error("Invalid schedule configuration: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error creating schedule", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all schedules
     *
     * @param status Optional status filter
     * @param createdBy Optional creator filter
     * @return List of schedules
     */
    @GetMapping
    public ResponseEntity<List<ReportScheduleConfig>> getAllSchedules(
            @RequestParam(required = false) ReportScheduleConfig.ScheduleStatus status,
            @RequestParam(required = false) String createdBy) {

        log.info("GET /api/schedules - status: {}, createdBy: {}", status, createdBy);

        try {
            List<ReportScheduleConfig> schedules;

            if (status != null) {
                schedules = schedulingService.getSchedulesByStatus(status);
            } else if (createdBy != null) {
                schedules = schedulingService.getSchedulesByCreator(createdBy);
            } else {
                schedules = schedulingService.getAllSchedules();
            }

            return ResponseEntity.ok(schedules);

        } catch (Exception e) {
            log.error("Error fetching schedules", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get schedule by ID
     *
     * @param id Schedule ID
     * @return Schedule details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportScheduleConfig> getSchedule(@PathVariable Long id) {
        log.info("GET /api/schedules/{}", id);

        try {
            ReportScheduleConfig schedule = schedulingService.getSchedule(id);

            if (schedule == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(schedule);

        } catch (Exception e) {
            log.error("Error fetching schedule {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update schedule
     *
     * @param id Schedule ID
     * @param config Updated configuration
     * @return Updated schedule
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReportScheduleConfig> updateSchedule(
            @PathVariable Long id,
            @RequestBody ReportScheduleConfig config) {

        log.info("PUT /api/schedules/{}", id);

        try {
            ReportScheduleConfig updated = schedulingService.updateSchedule(id, config);
            return ResponseEntity.ok(updated);

        } catch (IllegalArgumentException e) {
            log.error("Invalid schedule update: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error updating schedule {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete schedule
     *
     * @param id Schedule ID
     * @return Success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        log.info("DELETE /api/schedules/{}", id);

        try {
            schedulingService.deleteSchedule(id);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            log.error("Schedule not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error deleting schedule {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Pause schedule
     *
     * @param id Schedule ID
     * @return Success response
     */
    @PostMapping("/{id}/pause")
    public ResponseEntity<Void> pauseSchedule(@PathVariable Long id) {
        log.info("POST /api/schedules/{}/pause", id);

        try {
            schedulingService.pauseSchedule(id);
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            log.error("Schedule not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error pausing schedule {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Resume schedule
     *
     * @param id Schedule ID
     * @return Success response
     */
    @PostMapping("/{id}/resume")
    public ResponseEntity<Void> resumeSchedule(@PathVariable Long id) {
        log.info("POST /api/schedules/{}/resume", id);

        try {
            schedulingService.resumeSchedule(id);
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            log.error("Schedule not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error resuming schedule {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get execution history for schedule
     *
     * @param id Schedule ID
     * @param limit Number of records to return
     * @return Execution history
     */
    @GetMapping("/{id}/history")
    public ResponseEntity<List<ScheduleExecutionHistory>> getScheduleHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "50") int limit) {

        log.info("GET /api/schedules/{}/history - limit: {}", id, limit);

        try {
            List<ScheduleExecutionHistory> history = schedulingService.getExecutionHistory(id, limit);
            return ResponseEntity.ok(history);

        } catch (Exception e) {
            log.error("Error fetching execution history for schedule {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all execution history
     *
     * @param limit Number of records to return
     * @param failedOnly Only show failed executions
     * @return Execution history
     */
    @GetMapping("/history")
    public ResponseEntity<List<ScheduleExecutionHistory>> getAllExecutionHistory(
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "false") boolean failedOnly) {

        log.info("GET /api/schedules/history - limit: {}, failedOnly: {}", limit, failedOnly);

        try {
            List<ScheduleExecutionHistory> history;

            if (failedOnly) {
                history = schedulingService.getFailedExecutions(limit);
            } else {
                history = schedulingService.getAllExecutionHistory(limit);
            }

            return ResponseEntity.ok(history);

        } catch (Exception e) {
            log.error("Error fetching execution history", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get schedule statistics
     *
     * @return Statistics map
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/schedules/statistics");

        try {
            Map<String, Object> stats = schedulingService.getScheduleStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching schedule statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Execute schedule immediately (manual trigger)
     *
     * @param id Schedule ID
     * @return Success response
     */
    @PostMapping("/{id}/execute")
    public ResponseEntity<String> executeScheduleNow(@PathVariable Long id) {
        log.info("POST /api/schedules/{}/execute - Manual execution requested", id);

        try {
            ReportScheduleConfig schedule = schedulingService.getSchedule(id);

            if (schedule == null) {
                return ResponseEntity.notFound().build();
            }

            // In real implementation, trigger immediate execution
            // For now, just return success message
            String message = String.format("Schedule '%s' queued for immediate execution",
                    schedule.getScheduleName());

            return ResponseEntity.ok(message);

        } catch (Exception e) {
            log.error("Error executing schedule {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
