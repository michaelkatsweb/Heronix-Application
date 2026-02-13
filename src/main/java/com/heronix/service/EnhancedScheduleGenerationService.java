package com.heronix.service;

import com.heronix.model.domain.*;
import com.heronix.model.dto.ConflictDetail;
import com.heronix.model.dto.ScheduleGenerationRequest;
import com.heronix.model.dto.ScheduleGenerationResult;
import com.heronix.model.enums.*;
import com.heronix.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Enhanced Schedule Generation Service
 *
 * Wraps the existing ScheduleGenerationService to add support for:
 * - Partial schedule generation when hard constraints can't be satisfied
 * - Detailed conflict analysis and reporting
 * - Graceful degradation instead of complete failure
 *
 * This service enables the system to ALWAYS provide a useful result,
 * even when an optimal solution cannot be found.
 *
 * Location: src/main/java/com/heronix/service/EnhancedScheduleGenerationService.java
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since 2025-11-18
 */
@Slf4j
@Service
public class EnhancedScheduleGenerationService {

    @Autowired
    private ScheduleGenerationService scheduleGenerationService;

    @Autowired
    private ConflictAnalysisService conflictAnalysisService;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private ScheduleSlotRepository scheduleSlotRepository;

    /**
     * Generate schedule with fallback to partial scheduling
     *
     * This method attempts full schedule generation, and if hard constraints
     * cannot be satisfied, it gracefully falls back to partial scheduling
     * with detailed conflict reporting.
     *
     * @param request          Schedule generation parameters
     * @param progressCallback Progress update callback (optional)
     * @return Complete result with schedule and conflict information
     */
    public ScheduleGenerationResult generateWithFallback(
            ScheduleGenerationRequest request,
            BiConsumer<Integer, String> progressCallback) {

        log.info("═══════════════════════════════════════════════════════════════");
        log.info("ENHANCED SCHEDULE GENERATION STARTING");
        log.info("═══════════════════════════════════════════════════════════════");

        long startTime = System.currentTimeMillis();
        Schedule schedule = null;
        ScheduleStatus status = ScheduleStatus.DRAFT;
        List<ConflictDetail> conflicts = Collections.emptyList();

        try {
            // Attempt full schedule generation using existing service
            log.info("Attempting full schedule generation...");
            schedule = scheduleGenerationService.generateSchedule(request, progressCallback);

            // If we got here, generation was successful
            status = ScheduleStatus.PUBLISHED;
            conflicts = Collections.emptyList();

            log.info("✅ Full schedule generation successful!");

        } catch (RuntimeException e) {
            // Check if this is a hard constraint violation (could be wrapped)
            Throwable cause = e.getCause();
            boolean isConstraintViolation = e.getMessage().contains("hard constraint violations") ||
                                           (cause != null && cause.getMessage() != null &&
                                            cause.getMessage().contains("hard constraint violations"));

            if (isConstraintViolation) {
                // Hard constraint violations detected
                log.warn("Hard constraint violations detected, attempting partial schedule generation");

                try {
                    // Re-run generation with allowPartial=true
                    log.info("Re-running schedule generation with partial scheduling enabled...");
                    schedule = scheduleGenerationService.generateSchedule(request, progressCallback, true);

                // Schedule was saved with partial results
                status = ScheduleStatus.DRAFT;

                // Analyze conflicts
                log.info("Analyzing constraint violations...");
                conflicts = analyzeScheduleConflicts(schedule);

                log.warn("⚠️  Partial schedule generated with {} conflicts", conflicts.size());

                } catch (Exception partialEx) {
                    log.error("Failed to generate even partial schedule", partialEx);
                    throw new RuntimeException(
                        "Schedule generation completely failed. Original error: " +
                        e.getMessage() + ". Partial generation error: " + partialEx.getMessage(),
                        partialEx
                    );
                }
            } else {
                // Not a constraint violation - re-throw
                log.error("Unexpected error during schedule generation", e);
                throw e;
            }

        } catch (Exception e) {
            // Other errors (database, network, etc.)
            log.error("Unexpected error during schedule generation", e);
            throw new RuntimeException("Schedule generation failed: " + e.getMessage(), e);
        }

        // Calculate statistics
        long generationTime = (System.currentTimeMillis() - startTime) / 1000;

        // Build comprehensive result
        ScheduleGenerationResult result = buildGenerationResult(
            schedule, status, conflicts, generationTime, request);

        // Log summary
        logGenerationSummary(result);

        return result;
    }

    /**
     * Analyze conflicts in a partially generated schedule
     */
    private List<ConflictDetail> analyzeScheduleConflicts(Schedule schedule) {
        log.info("Analyzing conflicts in schedule: {}", schedule.getName());

        // Load schedule slots
        List<ScheduleSlot> slots = scheduleSlotRepository.findByScheduleId(schedule.getId());

        if (slots.isEmpty()) {
            log.warn("No schedule slots found for analysis");
            return Collections.emptyList();
        }

        // Use ConflictAnalysisService to analyze slots directly
        List<ConflictDetail> conflicts = conflictAnalysisService.analyzeSlotConflicts(slots);

        log.info("Identified {} conflicts", conflicts.size());
        return conflicts;
    }

    /**
     * Parse violation count from exception message
     */
    private int parseViolationCount(String errorMessage) {
        try {
            String[] parts = errorMessage.split("\\s+");
            for (int i = 0; i < parts.length - 2; i++) {
                if (parts[i + 1].equals("hard") && parts[i + 2].equals("constraint")) {
                    return Integer.parseInt(parts[i]);
                }
            }
        } catch (Exception e) {
            log.warn("Could not parse violation count from error message", e);
        }
        return 0;
    }

    /**
     * Build comprehensive generation result
     */
    private ScheduleGenerationResult buildGenerationResult(
            Schedule schedule,
            ScheduleStatus status,
            List<ConflictDetail> conflicts,
            long generationTime,
            ScheduleGenerationRequest request) {

        // Count courses
        int totalCourses = schedule.getSlots() != null ? schedule.getSlots().size() : 0;
        int unscheduledCourses = conflicts.size();
        int scheduledCourses = totalCourses - unscheduledCourses;

        // Calculate completion percentage
        double completionPct = totalCourses > 0 ?
            (scheduledCourses * 100.0) / totalCourses : 0.0;

        // Build result
        ScheduleGenerationResult result = ScheduleGenerationResult.builder()
            .schedule(schedule)
            .status(status)
            .conflicts(conflicts)
            .completionPercentage(completionPct)
            .totalCourses(totalCourses)
            .scheduledCourses(scheduledCourses)
            .unscheduledCourses(unscheduledCourses)
            .generationTimeSeconds(generationTime)
            .optimizationScore(schedule.getOptimizationScore() != null ?
                schedule.getOptimizationScore().toString() : "N/A")
            .build();

        // Add recommendations
        result.addRecommendation(generateRecommendations(result));

        // Calculate statistics
        result.calculateStatistics();

        return result;
    }

    /**
     * Generate recommendations based on result
     */
    private String generateRecommendations(ScheduleGenerationResult result) {
        StringBuilder recommendations = new StringBuilder();

        if (result.getCompletionPercentage() >= 100.0) {
            recommendations.append("✅ Perfect! Schedule is complete and ready to publish.");
        } else if (result.getCompletionPercentage() >= 90.0) {
            recommendations.append("⚠️  Schedule is mostly complete (")
                          .append(String.format("%.1f", result.getCompletionPercentage()))
                          .append("%). Review conflicts and consider accepting as-is or fixing critical issues.");
        } else if (result.getCompletionPercentage() >= 70.0) {
            recommendations.append("⚠️  Partial schedule generated (")
                          .append(String.format("%.1f", result.getCompletionPercentage()))
                          .append("%). Fix high-priority conflicts and regenerate for better results.");
        } else {
            recommendations.append("❌ Schedule generation needs attention. Only ")
                          .append(String.format("%.1f", result.getCompletionPercentage()))
                          .append("% complete. Address critical data issues before regenerating.");
        }

        return recommendations.toString();
    }

    /**
     * Log generation summary
     */
    private void logGenerationSummary(ScheduleGenerationResult result) {
        log.info("═══════════════════════════════════════════════════════════════");
        log.info("SCHEDULE GENERATION RESULT SUMMARY");
        log.info("═══════════════════════════════════════════════════════════════");
        log.info("Status: {}", result.getStatus());
        log.info("Completion: {}/{} courses ({}%)",
            result.getScheduledCourses(),
            result.getTotalCourses(),
            String.format("%.1f", result.getCompletionPercentage()));
        log.info("Conflicts: {}", result.getConflicts().size());
        log.info("  - Blocking: {}", result.getBlockingConflictCount());
        log.info("  - Major: {}", result.getMajorConflictCount());
        log.info("Generation Time: {} seconds", result.getGenerationTimeSeconds());
        log.info("Acceptable: {}", result.isSuccess() ? "Yes" : "No");
        log.info("═══════════════════════════════════════════════════════════════");
    }

    /**
     * Analyze existing schedule for conflicts
     *
     * This method can be called on already-generated schedules to
     * produce conflict reports without regenerating.
     *
     * @param scheduleId ID of schedule to analyze
     * @return Conflict analysis result
     */
    public ScheduleGenerationResult analyzeExistingSchedule(Long scheduleId) {
        log.info("Analyzing existing schedule {}", scheduleId);

        Schedule schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Schedule not found: " + scheduleId));

        List<ScheduleSlot> slots = scheduleSlotRepository.findByScheduleId(schedule.getId());

        List<ConflictDetail> conflicts = conflictAnalysisService.analyzeSlotConflicts(slots);

        int totalSlots = slots.size();
        int assignedSlots = (int) slots.stream()
            .filter(s -> s.getTeacher() != null && s.getRoom() != null)
            .count();
        double completionPercentage = totalSlots > 0 ? (assignedSlots * 100.0 / totalSlots) : 100.0;

        List<ConflictDetail> hardConflicts = conflicts.stream()
            .filter(c -> c.getSeverity() != null && c.getSeverity() == ConflictSeverity.CRITICAL)
            .collect(Collectors.toList());

        log.info("Analysis complete for schedule {}: {} total conflicts ({} critical), {}% assigned",
            scheduleId, conflicts.size(), hardConflicts.size(), String.format("%.1f", completionPercentage));

        return ScheduleGenerationResult.builder()
            .schedule(schedule)
            .status(schedule.getStatus())
            .conflicts(conflicts)
            .completionPercentage(completionPercentage)
            .totalCourses(totalSlots)
            .scheduledCourses(assignedSlots)
            .unscheduledCourses(totalSlots - assignedSlots)
            .build();
    }


    /**
     * Update progress callback
     */
    private void updateProgress(BiConsumer<Integer, String> callback, int percent, String message) {
        if (callback != null) {
            callback.accept(percent, message);
        }
        log.info("{}% - {}", percent, message);
    }
}
