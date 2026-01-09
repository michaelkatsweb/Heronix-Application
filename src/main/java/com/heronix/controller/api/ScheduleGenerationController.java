package com.heronix.controller.api;

import com.heronix.service.integration.ScheduleGenerationModeService;
import com.heronix.service.integration.ScheduleGenerationModeService.GenerationMode;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API Controller for Schedule Generation Mode Selection
 * Provides endpoints for choosing between manual and AI scheduling
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@RestController
@RequestMapping("/api/schedule-generation")
@RequiredArgsConstructor
public class ScheduleGenerationController {

    private final ScheduleGenerationModeService modeService;

    // ========================================================================
    // MODE SELECTION AND AVAILABILITY
    // ========================================================================

    /**
     * Get available generation modes
     *
     * GET /api/schedule-generation/modes
     *
     * @return List of available generation modes
     */
    @GetMapping("/modes")
    public ResponseEntity<Map<String, Object>> getAvailableModes() {
        try {
            log.info("API: Getting available schedule generation modes");

            // Get AI availability status
            ScheduleGenerationModeService.ModeAvailability aiAvailability =
                    modeService.checkAISchedulingAvailability();

            List<Map<String, Object>> modes = Arrays.stream(GenerationMode.values())
                    .map(mode -> {
                        Map<String, Object> modeInfo = new HashMap<>();
                        modeInfo.put("mode", mode.name());
                        modeInfo.put("displayName", mode.getDisplayName());
                        modeInfo.put("description", mode.getDescription());

                        // Check if this mode is available
                        boolean isAvailable = true;
                        String unavailableReason = null;

                        if (mode == GenerationMode.AI_ASSISTED || mode == GenerationMode.FULLY_AUTOMATED) {
                            isAvailable = aiAvailability.getIsAvailable();
                            unavailableReason = aiAvailability.getReason();
                        }

                        modeInfo.put("available", isAvailable);
                        if (!isAvailable) {
                            modeInfo.put("unavailableReason", unavailableReason);
                        }

                        return modeInfo;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("modes", modes);
            response.put("aiSchedulingAvailable", aiAvailability.getIsAvailable());
            response.put("aiSchedulingReason", aiAvailability.getReason());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting available modes", e);
            return createErrorResponse("Failed to get modes: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Check if AI scheduling is available
     *
     * GET /api/schedule-generation/ai-available
     *
     * @return AI availability status
     */
    @GetMapping("/ai-available")
    public ResponseEntity<Map<String, Object>> checkAIAvailability() {
        try {
            log.info("API: Checking AI scheduling availability");

            ScheduleGenerationModeService.ModeAvailability availability =
                    modeService.checkAISchedulingAvailability();

            Map<String, Object> response = new HashMap<>();
            response.put("available", availability.getIsAvailable());
            response.put("reason", availability.getReason());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error checking AI availability", e);
            return createErrorResponse("Failed to check availability: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========================================================================
    // SCHEDULE GENERATION
    // ========================================================================

    /**
     * Generate schedule using selected mode
     *
     * POST /api/schedule-generation/generate
     *
     * @param request Generation request
     * @return Generation result
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateSchedule(@RequestBody GenerationRequest request) {
        try {
            log.info("API: Generating schedule with mode: {} for schedule ID: {}",
                    request.getMode(), request.getScheduleId());

            // Validate mode
            GenerationMode mode;
            try {
                mode = GenerationMode.valueOf(request.getMode().toUpperCase().replace("-", "_"));
            } catch (IllegalArgumentException e) {
                return createErrorResponse("Invalid generation mode: " + request.getMode(),
                        HttpStatus.BAD_REQUEST);
            }

            // Build service request
            ScheduleGenerationModeService.ScheduleGenerationRequest serviceRequest =
                    ScheduleGenerationModeService.ScheduleGenerationRequest.builder()
                            .scheduleId(request.getScheduleId())
                            .mode(mode)
                            .optimizationTimeSeconds(request.getOptimizationTimeSeconds() != null ?
                                    request.getOptimizationTimeSeconds() : 120)
                            .build();

            // Generate schedule
            ScheduleGenerationModeService.ScheduleGenerationResult result =
                    modeService.generateSchedule(serviceRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.getSuccess());
            response.put("mode", result.getMode().name());
            response.put("scheduleId", result.getScheduleId());
            response.put("message", result.getMessage());

            if (result.getJobId() != null) {
                response.put("jobId", result.getJobId());
            }

            if (result.getExportId() != null) {
                response.put("exportId", result.getExportId());
            }

            if (result.getImportId() != null) {
                response.put("importId", result.getImportId());
            }

            if (result.getHardScore() != null) {
                response.put("hardScore", result.getHardScore());
                response.put("softScore", result.getSoftScore());
            }

            if (result.getSectionsCreated() != null) {
                response.put("sectionsCreated", result.getSectionsCreated());
                response.put("studentsScheduled", result.getStudentsScheduled());
            }

            if (result.getOptimizationTimeSeconds() != null) {
                response.put("optimizationTimeSeconds", result.getOptimizationTimeSeconds());
            }

            if (result.getHasConflicts() != null) {
                response.put("hasConflicts", result.getHasConflicts());
                response.put("conflictCount", result.getConflictCount());
                if (result.getConflicts() != null) {
                    response.put("conflicts", result.getConflicts());
                }
            }

            if (result.getManualEditRequired() != null) {
                response.put("manualEditRequired", result.getManualEditRequired());
            }

            HttpStatus status = result.getSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(response);

        } catch (IllegalArgumentException e) {
            log.error("Validation error generating schedule", e);
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error generating schedule", e);
            return createErrorResponse("Generation failed: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========================================================================
    // SCHEDULE COMPARISON
    // ========================================================================

    /**
     * Compare schedule quality metrics
     *
     * POST /api/schedule-generation/compare
     *
     * @param request Comparison request with two schedule IDs
     * @return Comparison result
     */
    @PostMapping("/compare")
    public ResponseEntity<Map<String, Object>> compareSchedules(@RequestBody ComparisonRequest request) {
        try {
            log.info("API: Comparing schedules {} and {}", request.getScheduleId1(), request.getScheduleId2());

            ScheduleGenerationModeService.ScheduleComparison comparison =
                    modeService.compareScheduleQuality(request.getScheduleId1(), request.getScheduleId2());

            Map<String, Object> response = new HashMap<>();

            // Schedule 1 info
            Map<String, Object> schedule1 = new HashMap<>();
            schedule1.put("scheduleId", comparison.getSchedule1Id());
            schedule1.put("scheduleName", comparison.getSchedule1Name());
            schedule1.put("hardScore", comparison.getSchedule1HardScore());
            schedule1.put("softScore", comparison.getSchedule1SoftScore());
            schedule1.put("conflicts", comparison.getSchedule1Conflicts());
            schedule1.put("generationMethod", comparison.getSchedule1Method());
            response.put("schedule1", schedule1);

            // Schedule 2 info
            Map<String, Object> schedule2 = new HashMap<>();
            schedule2.put("scheduleId", comparison.getSchedule2Id());
            schedule2.put("scheduleName", comparison.getSchedule2Name());
            schedule2.put("hardScore", comparison.getSchedule2HardScore());
            schedule2.put("softScore", comparison.getSchedule2SoftScore());
            schedule2.put("conflicts", comparison.getSchedule2Conflicts());
            schedule2.put("generationMethod", comparison.getSchedule2Method());
            response.put("schedule2", schedule2);

            response.put("recommendation", comparison.getRecommendation());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Validation error comparing schedules", e);
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error comparing schedules", e);
            return createErrorResponse("Comparison failed: " + e.getMessage(),
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
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(status).body(response);
    }

    // ========================================================================
    // REQUEST/RESPONSE DTOS
    // ========================================================================

    /**
     * Generation request DTO
     */
    @Data
    public static class GenerationRequest {
        private Long scheduleId;
        private String mode; // "MANUAL", "AI_ASSISTED", "FULLY_AUTOMATED"
        private Integer optimizationTimeSeconds;
        private String optimizationMode; // "FAST", "BALANCED", "THOROUGH"
        private Boolean enableAdvancedOptimization;
        private Boolean waitForCompletion;
    }

    /**
     * Comparison request DTO
     */
    @Data
    public static class ComparisonRequest {
        private Long scheduleId1;
        private Long scheduleId2;
    }
}
