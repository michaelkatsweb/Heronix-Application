package com.heronix.controller.api;

import com.heronix.integration.SchedulerApiClient;
import com.heronix.service.integration.ScheduleImportService;
import com.heronix.service.integration.SchedulerSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API Controller for SchedulerV2 Integration
 * Provides endpoints for syncing data with the AI scheduling engine
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
public class SchedulerIntegrationController {

    private final SchedulerApiClient schedulerApiClient;
    private final SchedulerSyncService schedulerSyncService;
    private final ScheduleImportService scheduleImportService;

    // ========================================================================
    // HEALTH CHECK
    // ========================================================================

    /**
     * Check if SchedulerV2 is available
     *
     * GET /api/scheduler/health
     *
     * @return Health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkSchedulerHealth() {
        try {
            boolean available = schedulerApiClient.isSchedulerAvailable();

            Map<String, Object> response = new HashMap<>();
            response.put("available", available);
            response.put("status", available ? "UP" : "DOWN");
            response.put("message", available ?
                    "SchedulerV2 is online and ready" :
                    "SchedulerV2 is not accessible");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error checking SchedulerV2 health", e);
            return createErrorResponse("Health check failed: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========================================================================
    // DATA EXPORT
    // ========================================================================

    /**
     * Export schedule data to SchedulerV2 for AI optimization
     *
     * POST /api/scheduler/export/{scheduleId}
     *
     * @param scheduleId SIS schedule ID
     * @return Export result
     */
    @PostMapping("/export/{scheduleId}")
    public ResponseEntity<Map<String, Object>> exportToScheduler(@PathVariable Long scheduleId) {
        try {
            log.info("API: Exporting schedule ID {} to SchedulerV2", scheduleId);

            SchedulerSyncService.SchedulerExportResult result =
                    schedulerSyncService.exportToScheduler(scheduleId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.getSuccess());
            response.put("message", result.getMessage());
            response.put("scheduleId", result.getScheduleId());
            response.put("exportId", result.getExportId());
            response.put("importId", result.getImportId());
            response.put("studentsExported", result.getStudentsExported());
            response.put("coursesExported", result.getCoursesExported());
            response.put("teachersExported", result.getTeachersExported());

            if (Boolean.TRUE.equals(result.getSuccess())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (IllegalArgumentException e) {
            log.error("Validation error exporting to SchedulerV2", e);
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error exporting to SchedulerV2", e);
            return createErrorResponse("Export failed: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========================================================================
    // SCHEDULE GENERATION
    // ========================================================================

    /**
     * Request AI schedule generation from SchedulerV2
     *
     * POST /api/scheduler/generate
     *
     * @param request Generation parameters
     * @return Job ID for tracking
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> requestScheduleGeneration(
            @RequestBody SchedulerApiClient.ScheduleGenerationRequest request) {

        try {
            log.info("API: Requesting schedule generation from SchedulerV2");

            String jobId = schedulerApiClient.requestScheduleGeneration(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("jobId", jobId);
            response.put("message", "Schedule generation started");
            response.put("status", "PROCESSING");

            return ResponseEntity.accepted().body(response);

        } catch (SchedulerApiClient.SchedulerApiException e) {
            log.error("Error requesting schedule generation", e);
            return createErrorResponse("Generation request failed: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error requesting schedule generation", e);
            return createErrorResponse("Failed to start generation: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Check status of a schedule generation job
     *
     * GET /api/scheduler/status/{jobId}
     *
     * @param jobId SchedulerV2 job ID
     * @return Job status
     */
    @GetMapping("/status/{jobId}")
    public ResponseEntity<Map<String, Object>> getJobStatus(@PathVariable String jobId) {
        try {
            log.info("API: Checking job status for: {}", jobId);

            SchedulerApiClient.ScheduleJobStatus status = schedulerApiClient.getJobStatus(jobId);

            Map<String, Object> response = new HashMap<>();
            response.put("jobId", status.getJobId());
            response.put("status", status.getStatus());
            response.put("progress", status.getProgress());
            response.put("message", status.getMessage());
            response.put("elapsedSeconds", status.getElapsedSeconds());
            response.put("hardScore", status.getHardScore());
            response.put("softScore", status.getSoftScore());

            return ResponseEntity.ok(response);

        } catch (SchedulerApiClient.SchedulerApiException e) {
            log.error("Error checking job status", e);
            return createErrorResponse("Status check failed: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error checking job status", e);
            return createErrorResponse("Failed to get status: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========================================================================
    // SCHEDULE IMPORT
    // ========================================================================

    /**
     * Import optimized schedule from SchedulerV2
     *
     * POST /api/scheduler/import/{scheduleId}
     *
     * @param scheduleId SIS schedule ID
     * @param jobId SchedulerV2 job ID
     * @return Import result
     */
    @PostMapping("/import/{scheduleId}")
    public ResponseEntity<Map<String, Object>> importFromScheduler(
            @PathVariable Long scheduleId,
            @RequestParam String jobId) {

        try {
            log.info("API: Importing schedule from SchedulerV2 job: {}", jobId);

            ScheduleImportService.ScheduleImportResult result =
                    scheduleImportService.importFromScheduler(scheduleId, jobId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.getSuccess());
            response.put("message", result.getMessage());
            response.put("scheduleId", result.getScheduleId());
            response.put("jobId", result.getJobId());
            response.put("sectionsCreated", result.getSectionsCreated());
            response.put("slotsAssigned", result.getSlotsAssigned());
            response.put("studentsScheduled", result.getStudentsScheduled());
            response.put("hardScore", result.getHardScore());
            response.put("softScore", result.getSoftScore());
            response.put("importTimestamp", result.getImportTimestamp());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Validation error importing from SchedulerV2", e);
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (SchedulerApiClient.SchedulerApiException e) {
            log.error("SchedulerV2 API error during import", e);
            return createErrorResponse("Import failed: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error importing from SchedulerV2", e);
            return createErrorResponse("Import failed: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========================================================================
    // FULL WORKFLOW
    // ========================================================================

    /**
     * Complete workflow: Export, Generate, and Import
     *
     * POST /api/scheduler/optimize/{scheduleId}
     *
     * @param scheduleId SIS schedule ID
     * @param waitForCompletion Whether to wait for generation to complete
     * @return Workflow result
     */
    @PostMapping("/optimize/{scheduleId}")
    public ResponseEntity<Map<String, Object>> optimizeSchedule(
            @PathVariable Long scheduleId,
            @RequestParam(defaultValue = "false") boolean waitForCompletion) {

        try {
            log.info("API: Starting full optimization workflow for schedule ID: {}", scheduleId);

            Map<String, Object> response = new HashMap<>();
            response.put("scheduleId", scheduleId);

            // Step 1: Export data to SchedulerV2
            log.info("Step 1: Exporting data to SchedulerV2");
            SchedulerSyncService.SchedulerExportResult exportResult =
                    schedulerSyncService.exportToScheduler(scheduleId);

            if (!Boolean.TRUE.equals(exportResult.getSuccess())) {
                response.put("success", false);
                response.put("message", "Export failed: " + exportResult.getMessage());
                response.put("stage", "EXPORT");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            response.put("exportId", exportResult.getExportId());
            response.put("studentsExported", exportResult.getStudentsExported());
            response.put("coursesExported", exportResult.getCoursesExported());

            // Step 2: Request schedule generation
            log.info("Step 2: Requesting schedule generation");
            SchedulerApiClient.ScheduleGenerationRequest genRequest =
                    SchedulerApiClient.ScheduleGenerationRequest.builder()
                            .optimizationTimeSeconds(120)
                            .optimizationMode("BALANCED")
                            .build();

            String jobId = schedulerApiClient.requestScheduleGeneration(genRequest);
            response.put("jobId", jobId);

            if (waitForCompletion) {
                // Step 3: Wait for completion (poll status)
                log.info("Step 3: Waiting for schedule generation to complete");
                SchedulerApiClient.ScheduleJobStatus status = waitForJobCompletion(jobId);

                if (!"COMPLETED".equals(status.getStatus())) {
                    response.put("success", false);
                    response.put("message", "Generation failed or timed out");
                    response.put("stage", "GENERATION");
                    response.put("status", status.getStatus());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }

                response.put("hardScore", status.getHardScore());
                response.put("softScore", status.getSoftScore());

                // Step 4: Import optimized schedule
                log.info("Step 4: Importing optimized schedule");
                ScheduleImportService.ScheduleImportResult importResult =
                        scheduleImportService.importFromScheduler(scheduleId, jobId);

                response.put("success", importResult.getSuccess());
                response.put("message", "Schedule optimization completed successfully");
                response.put("stage", "COMPLETED");
                response.put("sectionsCreated", importResult.getSectionsCreated());
                response.put("slotsAssigned", importResult.getSlotsAssigned());
                response.put("studentsScheduled", importResult.getStudentsScheduled());

                return ResponseEntity.ok(response);

            } else {
                // Return job ID for manual polling
                response.put("success", true);
                response.put("message", "Optimization started. Use /api/scheduler/status/{jobId} to check progress.");
                response.put("stage", "PROCESSING");
                response.put("status", "ACCEPTED");

                return ResponseEntity.accepted().body(response);
            }

        } catch (Exception e) {
            log.error("Error during schedule optimization workflow", e);
            return createErrorResponse("Optimization workflow failed: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Wait for job completion with polling
     */
    private SchedulerApiClient.ScheduleJobStatus waitForJobCompletion(String jobId)
            throws SchedulerApiClient.SchedulerApiException, InterruptedException {

        int maxAttempts = 60; // 60 attempts = 5 minutes (with 5s intervals)
        int attemptCount = 0;

        while (attemptCount < maxAttempts) {
            SchedulerApiClient.ScheduleJobStatus status = schedulerApiClient.getJobStatus(jobId);

            if ("COMPLETED".equals(status.getStatus())) {
                return status;
            }

            if ("FAILED".equals(status.getStatus()) || "ERROR".equals(status.getStatus())) {
                return status;
            }

            // Wait 5 seconds before next poll
            Thread.sleep(5000);
            attemptCount++;

            if (attemptCount % 6 == 0) { // Log every 30 seconds
                log.info("Still waiting for job completion... ({}s elapsed)", attemptCount * 5);
            }
        }

        // Timeout
        log.warn("Job completion polling timed out after {} seconds", maxAttempts * 5);
        return SchedulerApiClient.ScheduleJobStatus.builder()
                .jobId(jobId)
                .status("TIMEOUT")
                .message("Polling timed out after " + (maxAttempts * 5) + " seconds")
                .build();
    }

    /**
     * Create error response
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(status).body(response);
    }
}
