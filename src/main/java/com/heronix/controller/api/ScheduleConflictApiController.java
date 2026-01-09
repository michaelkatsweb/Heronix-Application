package com.heronix.controller.api;

import com.heronix.model.domain.Schedule;
import com.heronix.model.dto.ConflictDetail;
import com.heronix.model.planning.SchedulingSolution;
import com.heronix.repository.ScheduleRepository;
import com.heronix.service.ConflictAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API Controller for Schedule Conflict Analysis
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/schedule-conflicts")
@RequiredArgsConstructor
public class ScheduleConflictApiController {

    private final ConflictAnalysisService conflictAnalysisService;
    private final ScheduleRepository scheduleRepository;

    // ==================== Conflict Analysis ====================

    @PostMapping("/analyze")
    public ResponseEntity<List<ConflictDetail>> analyzeScheduleConflicts(@RequestBody SchedulingSolution solution) {
        List<ConflictDetail> conflicts = conflictAnalysisService.analyzeConstraintViolations(solution);
        return ResponseEntity.ok(conflicts);
    }

    @GetMapping("/schedule/{scheduleId}/analyze")
    public ResponseEntity<Map<String, Object>> analyzeScheduleById(@PathVariable Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + scheduleId));

        // Convert Schedule to SchedulingSolution for analysis
        SchedulingSolution solution = convertToSolution(schedule);

        List<ConflictDetail> conflicts = conflictAnalysisService.analyzeConstraintViolations(solution);
        double completionPercentage = conflictAnalysisService.calculateCompletionPercentage(solution);

        Map<String, Object> response = new HashMap<>();
        response.put("scheduleId", scheduleId);
        response.put("scheduleName", schedule.getScheduleName());
        response.put("totalConflicts", conflicts.size());
        response.put("completionPercentage", completionPercentage);
        response.put("conflicts", conflicts);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/completion-percentage")
    public ResponseEntity<Map<String, Object>> calculateCompletionPercentage(@RequestBody SchedulingSolution solution) {
        double percentage = conflictAnalysisService.calculateCompletionPercentage(solution);

        Map<String, Object> response = new HashMap<>();
        response.put("completionPercentage", percentage);
        response.put("isComplete", percentage >= 100.0);
        response.put("slotsAssigned", (int) (solution.getScheduleSlots().size() * percentage / 100.0));
        response.put("totalSlots", solution.getScheduleSlots().size());

        return ResponseEntity.ok(response);
    }

    // ==================== Conflict Filtering ====================

    @PostMapping("/analyze/by-severity")
    public ResponseEntity<Map<String, Object>> analyzeConflictsBySeverity(@RequestBody SchedulingSolution solution) {
        List<ConflictDetail> conflicts = conflictAnalysisService.analyzeConstraintViolations(solution);

        Map<String, List<ConflictDetail>> bySeverity = conflicts.stream()
                .collect(Collectors.groupingBy(
                    c -> c.getSeverity() != null ? c.getSeverity().name() : "UNKNOWN"
                ));

        Map<String, Object> response = new HashMap<>();
        response.put("totalConflicts", conflicts.size());
        response.put("criticalCount", bySeverity.getOrDefault("CRITICAL", List.of()).size());
        response.put("highCount", bySeverity.getOrDefault("HIGH", List.of()).size());
        response.put("mediumCount", bySeverity.getOrDefault("MEDIUM", List.of()).size());
        response.put("lowCount", bySeverity.getOrDefault("LOW", List.of()).size());
        response.put("bySeverity", bySeverity);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/analyze/by-type")
    public ResponseEntity<Map<String, Object>> analyzeConflictsByType(@RequestBody SchedulingSolution solution) {
        List<ConflictDetail> conflicts = conflictAnalysisService.analyzeConstraintViolations(solution);

        Map<String, List<ConflictDetail>> byType = conflicts.stream()
                .collect(Collectors.groupingBy(
                    c -> c.getType() != null ? c.getType().name() : "UNKNOWN"
                ));

        Map<String, Object> response = new HashMap<>();
        response.put("totalConflicts", conflicts.size());
        response.put("byType", byType);

        // Count by type
        Map<String, Integer> typeCounts = new HashMap<>();
        byType.forEach((type, list) -> typeCounts.put(type, list.size()));
        response.put("typeCounts", typeCounts);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/analyze/blocking-only")
    public ResponseEntity<List<ConflictDetail>> getBlockingConflicts(@RequestBody SchedulingSolution solution) {
        List<ConflictDetail> conflicts = conflictAnalysisService.analyzeConstraintViolations(solution);

        List<ConflictDetail> blockingConflicts = conflicts.stream()
                .filter(c -> Boolean.TRUE.equals(c.getBlocking()))
                .toList();

        return ResponseEntity.ok(blockingConflicts);
    }

    @PostMapping("/analyze/critical-only")
    public ResponseEntity<List<ConflictDetail>> getCriticalConflicts(@RequestBody SchedulingSolution solution) {
        List<ConflictDetail> conflicts = conflictAnalysisService.analyzeConstraintViolations(solution);

        List<ConflictDetail> criticalConflicts = conflicts.stream()
                .filter(c -> c.getSeverity() != null &&
                            c.getSeverity().name().equals("CRITICAL"))
                .toList();

        return ResponseEntity.ok(criticalConflicts);
    }

    // ==================== Dashboard Endpoints ====================

    @PostMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getConflictDashboard(@RequestBody SchedulingSolution solution) {
        Map<String, Object> dashboard = new HashMap<>();

        List<ConflictDetail> conflicts = conflictAnalysisService.analyzeConstraintViolations(solution);
        double completionPercentage = conflictAnalysisService.calculateCompletionPercentage(solution);

        long blockingCount = conflicts.stream().filter(c -> Boolean.TRUE.equals(c.getBlocking())).count();
        long criticalCount = conflicts.stream()
                .filter(c -> c.getSeverity() != null && c.getSeverity().name().equals("CRITICAL"))
                .count();
        long highCount = conflicts.stream()
                .filter(c -> c.getSeverity() != null && c.getSeverity().name().equals("HIGH"))
                .count();

        dashboard.put("totalConflicts", conflicts.size());
        dashboard.put("blockingConflicts", blockingCount);
        dashboard.put("criticalConflicts", criticalCount);
        dashboard.put("highConflicts", highCount);
        dashboard.put("completionPercentage", completionPercentage);
        dashboard.put("isComplete", completionPercentage >= 100.0);
        dashboard.put("totalSlots", solution.getScheduleSlots().size());
        dashboard.put("assignedSlots", (int) (solution.getScheduleSlots().size() * completionPercentage / 100.0));

        return ResponseEntity.ok(dashboard);
    }

    @PostMapping("/dashboard/summary")
    public ResponseEntity<Map<String, Object>> getConflictSummary(@RequestBody SchedulingSolution solution) {
        Map<String, Object> dashboard = new HashMap<>();

        List<ConflictDetail> conflicts = conflictAnalysisService.analyzeConstraintViolations(solution);

        // Group by severity
        Map<String, Long> severityCounts = conflicts.stream()
                .collect(Collectors.groupingBy(
                    c -> c.getSeverity() != null ? c.getSeverity().name() : "UNKNOWN",
                    Collectors.counting()
                ));

        // Group by type
        Map<String, Long> typeCounts = conflicts.stream()
                .collect(Collectors.groupingBy(
                    c -> c.getType() != null ? c.getType().name() : "UNKNOWN",
                    Collectors.counting()
                ));

        // Calculate total students affected
        int totalStudentsAffected = conflicts.stream()
                .mapToInt(ConflictDetail::getStudentsAffected)
                .sum();

        // Calculate estimated fix time
        int totalEstimatedFixTime = conflicts.stream()
                .mapToInt(ConflictDetail::getEstimatedFixTimeMinutes)
                .sum();

        dashboard.put("totalConflicts", conflicts.size());
        dashboard.put("severityCounts", severityCounts);
        dashboard.put("typeCounts", typeCounts);
        dashboard.put("totalStudentsAffected", totalStudentsAffected);
        dashboard.put("estimatedTotalFixTimeMinutes", totalEstimatedFixTime);
        dashboard.put("estimatedFixTimeHours", totalEstimatedFixTime / 60.0);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/schedule/{scheduleId}")
    public ResponseEntity<Map<String, Object>> getScheduleConflictDashboard(@PathVariable Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + scheduleId));

        SchedulingSolution solution = convertToSolution(schedule);
        List<ConflictDetail> conflicts = conflictAnalysisService.analyzeConstraintViolations(solution);
        double completionPercentage = conflictAnalysisService.calculateCompletionPercentage(solution);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("scheduleId", scheduleId);
        dashboard.put("scheduleName", schedule.getScheduleName());
        dashboard.put("totalConflicts", conflicts.size());
        dashboard.put("completionPercentage", completionPercentage);
        dashboard.put("isComplete", completionPercentage >= 100.0);

        // Severity breakdown
        Map<String, Long> severityCounts = conflicts.stream()
                .collect(Collectors.groupingBy(
                    c -> c.getSeverity() != null ? c.getSeverity().name() : "UNKNOWN",
                    Collectors.counting()
                ));
        dashboard.put("severityCounts", severityCounts);

        // Top conflicts (most severe, most students affected)
        List<ConflictDetail> topConflicts = conflicts.stream()
                .sorted((c1, c2) -> {
                    // Sort by severity first, then by students affected
                    int severityCompare = Integer.compare(
                        c2.getSeverity() != null ? c2.getSeverity().getPriorityScore() : 0,
                        c1.getSeverity() != null ? c1.getSeverity().getPriorityScore() : 0
                    );
                    if (severityCompare != 0) return severityCompare;
                    return Integer.compare(c2.getStudentsAffected(), c1.getStudentsAffected());
                })
                .limit(10)
                .toList();
        dashboard.put("topConflicts", topConflicts);

        return ResponseEntity.ok(dashboard);
    }

    // ==================== Helper Methods ====================

    /**
     * Convert Schedule entity to SchedulingSolution for analysis
     */
    private SchedulingSolution convertToSolution(Schedule schedule) {
        SchedulingSolution solution = new SchedulingSolution();
        // Set schedule slots from the schedule
        if (schedule.getSlots() != null) {
            solution.setScheduleSlots(schedule.getSlots());
        }
        return solution;
    }
}
