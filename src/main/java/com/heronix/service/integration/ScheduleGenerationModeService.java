package com.heronix.service.integration;

import com.heronix.integration.SchedulerApiClient;
import com.heronix.model.domain.Schedule;
import com.heronix.repository.ScheduleRepository;
import com.heronix.service.MasterScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing schedule generation mode selection
 * Allows users to choose between manual scheduling and AI-powered optimization
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleGenerationModeService {

    private final SchedulerApiClient schedulerApiClient;
    private final SchedulerSyncService schedulerSyncService;
    private final ScheduleImportService scheduleImportService;
    private final MasterScheduleService masterScheduleService;
    private final ScheduleRepository scheduleRepository;

    @Value("${heronix.scheduler.enabled:true}")
    private Boolean schedulerEnabled;

    @Value("${heronix.scheduler.default-optimization-time:120}")
    private Integer defaultOptimizationTime;

    @Value("${heronix.scheduler.poll-interval:5}")
    private Integer pollInterval;

    // ========================================================================
    // GENERATION MODE ENUM
    // ========================================================================

    /**
     * Schedule generation modes
     */
    public enum GenerationMode {
        MANUAL("Manual Scheduling", "Build schedule manually with drag-and-drop interface"),
        AI_ASSISTED("AI-Assisted", "AI generates initial schedule, then manual refinement allowed"),
        FULLY_AUTOMATED("Fully Automated (AI)", "AI-powered OptaPlanner optimization (recommended)");

        private final String displayName;
        private final String description;

        GenerationMode(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    // ========================================================================
    // MODE AVAILABILITY CHECK
    // ========================================================================

    /**
     * Check if AI scheduling is available
     *
     * @return Availability status with message
     */
    public ModeAvailability checkAISchedulingAvailability() {
        if (!schedulerEnabled) {
            return ModeAvailability.builder()
                    .isAvailable(false)
                    .reason("AI scheduling is disabled in configuration")
                    .build();
        }

        boolean schedulerOnline = schedulerApiClient.isSchedulerAvailable();

        if (!schedulerOnline) {
            return ModeAvailability.builder()
                    .isAvailable(false)
                    .reason("Heronix-SchedulerV2 is not running or not accessible at the configured URL")
                    .build();
        }

        return ModeAvailability.builder()
                .isAvailable(true)
                .reason("AI scheduling is available")
                .build();
    }

    // ========================================================================
    // SCHEDULE GENERATION METHODS
    // ========================================================================

    /**
     * Generate schedule using specified mode
     *
     * @param request Generation request with mode and parameters
     * @return Generation result
     */
    @Transactional
    public ScheduleGenerationResult generateSchedule(ScheduleGenerationRequest request) {
        log.info("Starting schedule generation with mode: {} for schedule ID: {}",
                request.getMode(), request.getScheduleId());

        Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + request.getScheduleId()));

        switch (request.getMode()) {
            case MANUAL:
                return generateScheduleManual(schedule, request);

            case AI_ASSISTED:
            case FULLY_AUTOMATED:
                return generateScheduleAI(schedule, request);

            default:
                throw new IllegalArgumentException("Unsupported generation mode: " + request.getMode());
        }
    }

    /**
     * Generate schedule manually (no AI)
     */
    private ScheduleGenerationResult generateScheduleManual(Schedule schedule, ScheduleGenerationRequest request) {
        log.info("Generating schedule manually for schedule ID: {}", schedule.getId());

        // Manual generation uses existing MasterScheduleService
        // This is primarily UI-driven, so we just mark it as ready for manual work

        schedule.setLastModifiedDate(LocalDate.now());
        schedule.setLastModifiedBy("Manual");
        scheduleRepository.save(schedule);

        return ScheduleGenerationResult.builder()
                .success(true)
                .scheduleId(schedule.getId())
                .mode(GenerationMode.MANUAL)
                .message("Schedule ready for manual building")
                .manualEditRequired(true)
                .build();
    }

    /**
     * Generate schedule using AI (SchedulerV2)
     */
    private ScheduleGenerationResult generateScheduleAI(Schedule schedule, ScheduleGenerationRequest request) {
        log.info("Generating schedule with AI for schedule ID: {}", schedule.getId());

        try {
            // Step 1: Check availability
            ModeAvailability availability = checkAISchedulingAvailability();
            if (!availability.getIsAvailable()) {
                return ScheduleGenerationResult.builder()
                        .success(false)
                        .scheduleId(schedule.getId())
                        .mode(request.getMode())
                        .message("AI scheduling not available: " + availability.getReason())
                        .build();
            }

            // SchedulerV2 pulls data from SIS via its own SISApiClient — no export step needed.

            // Step 3: Request schedule generation
            log.info("Requesting schedule generation from SchedulerV2...");
            SchedulerApiClient.ScheduleGenerationRequest genRequest =
                    SchedulerApiClient.ScheduleGenerationRequest.builder()
                            .optimizationTimeSeconds(request.getOptimizationTimeSeconds() != null ?
                                    request.getOptimizationTimeSeconds() : defaultOptimizationTime)
                            .enableAdvancedOptimization(true)
                            .optimizationMode(request.getMode() == GenerationMode.FULLY_AUTOMATED ?
                                    "THOROUGH" : "BALANCED")
                            .build();

            String jobId = schedulerApiClient.requestScheduleGeneration(genRequest);
            log.info("Schedule generation job started: {}", jobId);

            // Step 4: Poll for completion
            log.info("Polling for job completion (this may take up to {} seconds)...",
                    request.getOptimizationTimeSeconds() != null ?
                            request.getOptimizationTimeSeconds() : defaultOptimizationTime);

            SchedulerApiClient.ScheduleJobStatus finalStatus =
                    schedulerApiClient.pollUntilComplete(jobId, pollInterval);

            log.info("Job completed with status: {} (Hard: {}, Soft: {})",
                    finalStatus.getStatus(),
                    finalStatus.getHardScore(),
                    finalStatus.getSoftScore());

            // Step 5: Import results
            log.info("Importing optimized schedule from SchedulerV2...");
            ScheduleImportService.ScheduleImportResult importResult =
                    scheduleImportService.importFromScheduler(schedule.getId(), jobId);

            if (!importResult.getSuccess()) {
                return ScheduleGenerationResult.builder()
                        .success(false)
                        .scheduleId(schedule.getId())
                        .mode(request.getMode())
                        .jobId(jobId)
                        .message("Schedule import failed: " + importResult.getMessage())
                        .build();
            }

            // Step 6: Validate imported schedule
            log.info("Validating imported schedule...");
            ScheduleImportService.ValidationResult validationResult =
                    scheduleImportService.validateImportedSchedule(schedule.getId());

            // Build result
            return ScheduleGenerationResult.builder()
                    .success(true)
                    .scheduleId(schedule.getId())
                    .mode(request.getMode())
                    .jobId(jobId)
                    .exportId(null)
                    .importId(null)
                    .sectionsCreated(importResult.getSectionsCreated())
                    .studentsScheduled(importResult.getStudentsScheduled())
                    .hardScore(importResult.getHardScore())
                    .softScore(importResult.getSoftScore())
                    .optimizationTimeSeconds(finalStatus.getElapsedSeconds())
                    .hasConflicts(!validationResult.getIsValid())
                    .conflictCount(validationResult.getConflictCount())
                    .conflicts(validationResult.getConflicts())
                    .manualEditRequired(request.getMode() == GenerationMode.AI_ASSISTED)
                    .message(String.format("Schedule generated successfully with %d sections and %d students scheduled",
                            importResult.getSectionsCreated(), importResult.getStudentsScheduled()))
                    .build();

        } catch (SchedulerApiClient.SchedulerApiException e) {
            log.error("AI schedule generation failed", e);
            return ScheduleGenerationResult.builder()
                    .success(false)
                    .scheduleId(schedule.getId())
                    .mode(request.getMode())
                    .message("AI schedule generation failed: " + e.getMessage())
                    .build();
        }
    }

    // ========================================================================
    // SCHEDULE COMPARISON
    // ========================================================================

    /**
     * Compare two schedules for quality metrics
     *
     * @param scheduleId1 First schedule ID
     * @param scheduleId2 Second schedule ID
     * @return Comparison result
     */
    @Transactional(readOnly = true)
    public ScheduleComparison compareScheduleQuality(Long scheduleId1, Long scheduleId2) {
        log.info("Comparing schedules {} and {}", scheduleId1, scheduleId2);

        Schedule schedule1 = scheduleRepository.findById(scheduleId1)
                .orElseThrow(() -> new IllegalArgumentException("Schedule 1 not found"));
        Schedule schedule2 = scheduleRepository.findById(scheduleId2)
                .orElseThrow(() -> new IllegalArgumentException("Schedule 2 not found"));

        // Validate both schedules
        ScheduleImportService.ValidationResult validation1 =
                scheduleImportService.validateImportedSchedule(scheduleId1);
        ScheduleImportService.ValidationResult validation2 =
                scheduleImportService.validateImportedSchedule(scheduleId2);

        // Build comparison (using qualityScore as proxy for optimization scores)
        return ScheduleComparison.builder()
                .schedule1Id(scheduleId1)
                .schedule1Name(schedule1.getScheduleName())
                .schedule1HardScore(null) // Not stored separately
                .schedule1SoftScore(schedule1.getQualityScore() != null ? schedule1.getQualityScore().intValue() : null)
                .schedule1Conflicts(validation1.getConflictCount())
                .schedule1Method(schedule1.getLastModifiedBy()) // Use lastModifiedBy as proxy for method
                .schedule2Id(scheduleId2)
                .schedule2Name(schedule2.getScheduleName())
                .schedule2HardScore(null) // Not stored separately
                .schedule2SoftScore(schedule2.getQualityScore() != null ? schedule2.getQualityScore().intValue() : null)
                .schedule2Conflicts(validation2.getConflictCount())
                .schedule2Method(schedule2.getLastModifiedBy()) // Use lastModifiedBy as proxy for method
                .recommendation(determineRecommendation(schedule1, schedule2, validation1, validation2))
                .build();
    }

    /**
     * Determine which schedule is better
     */
    private String determineRecommendation(Schedule s1, Schedule s2,
                                          ScheduleImportService.ValidationResult v1,
                                          ScheduleImportService.ValidationResult v2) {

        // Check conflicts first (most important)
        if (v1.getConflictCount() != v2.getConflictCount()) {
            return v1.getConflictCount() < v2.getConflictCount() ?
                    String.format("Schedule 1 (%s) has fewer conflicts", s1.getScheduleName()) :
                    String.format("Schedule 2 (%s) has fewer conflicts", s2.getScheduleName());
        }

        // If no conflicts, compare quality scores
        int qualityComparison = compareScores(
                s1.getQualityScore() != null ? s1.getQualityScore().intValue() : null,
                s2.getQualityScore() != null ? s2.getQualityScore().intValue() : null
        );
        if (qualityComparison != 0) {
            return qualityComparison > 0 ?
                    String.format("Schedule 1 (%s) has better optimization", s1.getScheduleName()) :
                    String.format("Schedule 2 (%s) has better optimization", s2.getScheduleName());
        }

        return "Both schedules have equivalent quality";
    }

    /**
     * Compare scores (higher is better)
     */
    private int compareScores(Integer score1, Integer score2) {
        if (score1 == null && score2 == null) return 0;
        if (score1 == null) return -1;
        if (score2 == null) return 1;
        return score1.compareTo(score2);
    }

    // ========================================================================
    // HELPER CLASSES
    // ========================================================================

    /**
     * Schedule generation request
     */
    @lombok.Data
    @lombok.Builder
    public static class ScheduleGenerationRequest {
        private Long scheduleId;
        private GenerationMode mode;
        private Integer optimizationTimeSeconds;
    }

    /**
     * Schedule generation result
     */
    @lombok.Data
    @lombok.Builder
    public static class ScheduleGenerationResult {
        private Boolean success;
        private Long scheduleId;
        private GenerationMode mode;
        private String jobId;
        private String exportId;
        private String importId;
        private Integer sectionsCreated;
        private Integer studentsScheduled;
        private Integer hardScore;
        private Integer softScore;
        private Integer optimizationTimeSeconds;
        private Boolean hasConflicts;
        private Integer conflictCount;
        private java.util.List<String> conflicts;
        private Boolean manualEditRequired;
        private String message;
    }

    /**
     * Mode availability
     */
    @lombok.Data
    @lombok.Builder
    public static class ModeAvailability {
        private Boolean isAvailable;
        private String reason;
    }

    /**
     * Schedule comparison result
     */
    @lombok.Data
    @lombok.Builder
    public static class ScheduleComparison {
        private Long schedule1Id;
        private String schedule1Name;
        private Integer schedule1HardScore;
        private Integer schedule1SoftScore;
        private Integer schedule1Conflicts;
        private String schedule1Method;

        private Long schedule2Id;
        private String schedule2Name;
        private Integer schedule2HardScore;
        private Integer schedule2SoftScore;
        private Integer schedule2Conflicts;
        private String schedule2Method;

        private String recommendation;
    }
}
