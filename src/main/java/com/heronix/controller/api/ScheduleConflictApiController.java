package com.heronix.controller.api;

import com.heronix.model.domain.Schedule;
import com.heronix.model.domain.ScheduleSlot;
import com.heronix.model.dto.ConflictDetail;
import com.heronix.repository.ScheduleRepository;
import com.heronix.repository.ScheduleSlotRepository;
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
 * @version 2.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/schedule-conflicts")
@RequiredArgsConstructor
public class ScheduleConflictApiController {

    private final ConflictAnalysisService conflictAnalysisService;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleSlotRepository scheduleSlotRepository;

    // ==================== Conflict Analysis ====================

    @PostMapping("/analyze")
    public ResponseEntity<List<ConflictDetail>> analyzeScheduleConflicts(@RequestBody List<ScheduleSlot> slots) {
        List<ConflictDetail> conflicts = conflictAnalysisService.analyzeSlotConflicts(slots);
        return ResponseEntity.ok(conflicts);
    }

    @GetMapping("/schedule/{scheduleId}/analyze")
    public ResponseEntity<Map<String, Object>> analyzeScheduleById(@PathVariable Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + scheduleId));

        List<ScheduleSlot> slots = scheduleSlotRepository.findByScheduleId(scheduleId);

        List<ConflictDetail> conflicts = conflictAnalysisService.analyzeSlotConflicts(slots);
        double completionPercentage = conflictAnalysisService.calculateCompletionPercentage(slots);

        Map<String, Object> response = new HashMap<>();
        response.put("scheduleId", scheduleId);
        response.put("scheduleName", schedule.getScheduleName());
        response.put("totalConflicts", conflicts.size());
        response.put("completionPercentage", completionPercentage);
        response.put("conflicts", conflicts);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/completion-percentage")
    public ResponseEntity<Map<String, Object>> calculateCompletionPercentage(@RequestBody List<ScheduleSlot> slots) {
        double percentage = conflictAnalysisService.calculateCompletionPercentage(slots);

        Map<String, Object> response = new HashMap<>();
        response.put("completionPercentage", percentage);
        response.put("isComplete", percentage >= 100.0);
        response.put("slotsAssigned", (int) (slots.size() * percentage / 100.0));
        response.put("totalSlots", slots.size());

        return ResponseEntity.ok(response);
    }

    // ==================== Conflict Filtering ====================

    @PostMapping("/analyze/by-severity")
    public ResponseEntity<Map<String, Object>> analyzeConflictsBySeverity(@RequestBody List<ScheduleSlot> slots) {
        List<ConflictDetail> conflicts = conflictAnalysisService.analyzeSlotConflicts(slots);

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
    public ResponseEntity<Map<String, Object>> analyzeConflictsByType(@RequestBody List<ScheduleSlot> slots) {
        List<ConflictDetail> conflicts = conflictAnalysisService.analyzeSlotConflicts(slots);

        Map<String, List<ConflictDetail>> byType = conflicts.stream()
                .collect(Collectors.groupingBy(
                    c -> c.getType() != null ? c.getType().name() : "UNKNOWN"
                ));

        Map<String, Object> response = new HashMap<>();
        response.put("totalConflicts", conflicts.size());
        response.put("byType", byType);

        Map<String, Integer> typeCounts = new HashMap<>();
        byType.forEach((type, list) -> typeCounts.put(type, list.size()));
        response.put("typeCounts", typeCounts);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/analyze/blocking-only")
    public ResponseEntity<List<ConflictDetail>> getBlockingConflicts(@RequestBody List<ScheduleSlot> slots) {
        List<ConflictDetail> conflicts = conflictAnalysisService.analyzeSlotConflicts(slots);

        List<ConflictDetail> blockingConflicts = conflicts.stream()
                .filter(c -> Boolean.TRUE.equals(c.getBlocking()))
                .toList();

        return ResponseEntity.ok(blockingConflicts);
    }

    @PostMapping("/analyze/critical-only")
    public ResponseEntity<List<ConflictDetail>> getCriticalConflicts(@RequestBody List<ScheduleSlot> slots) {
        List<ConflictDetail> conflicts = conflictAnalysisService.analyzeSlotConflicts(slots);

        List<ConflictDetail> criticalConflicts = conflicts.stream()
                .filter(c -> c.getSeverity() != null &&
                            c.getSeverity().name().equals("CRITICAL"))
                .toList();

        return ResponseEntity.ok(criticalConflicts);
    }

    // ==================== Dashboard Endpoints ====================

    @PostMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getConflictDashboard(@RequestBody List<ScheduleSlot> slots) {
        Map<String, Object> dashboard = new HashMap<>();

        List<ConflictDetail> conflicts = conflictAnalysisService.analyzeSlotConflicts(slots);
        double completionPercentage = conflictAnalysisService.calculateCompletionPercentage(slots);

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
        dashboard.put("totalSlots", slots.size());
        dashboard.put("assignedSlots", (int) (slots.size() * completionPercentage / 100.0));

        return ResponseEntity.ok(dashboard);
    }

    @PostMapping("/dashboard/summary")
    public ResponseEntity<Map<String, Object>> getConflictSummary(@RequestBody List<ScheduleSlot> slots) {
        Map<String, Object> dashboard = new HashMap<>();

        List<ConflictDetail> conflicts = conflictAnalysisService.analyzeSlotConflicts(slots);

        Map<String, Long> severityCounts = conflicts.stream()
                .collect(Collectors.groupingBy(
                    c -> c.getSeverity() != null ? c.getSeverity().name() : "UNKNOWN",
                    Collectors.counting()
                ));

        Map<String, Long> typeCounts = conflicts.stream()
                .collect(Collectors.groupingBy(
                    c -> c.getType() != null ? c.getType().name() : "UNKNOWN",
                    Collectors.counting()
                ));

        int totalStudentsAffected = conflicts.stream()
                .mapToInt(ConflictDetail::getStudentsAffected)
                .sum();

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

        List<ScheduleSlot> slots = scheduleSlotRepository.findByScheduleId(scheduleId);
        List<ConflictDetail> conflicts = conflictAnalysisService.analyzeSlotConflicts(slots);
        double completionPercentage = conflictAnalysisService.calculateCompletionPercentage(slots);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("scheduleId", scheduleId);
        dashboard.put("scheduleName", schedule.getScheduleName());
        dashboard.put("totalConflicts", conflicts.size());
        dashboard.put("completionPercentage", completionPercentage);
        dashboard.put("isComplete", completionPercentage >= 100.0);

        Map<String, Long> severityCounts = conflicts.stream()
                .collect(Collectors.groupingBy(
                    c -> c.getSeverity() != null ? c.getSeverity().name() : "UNKNOWN",
                    Collectors.counting()
                ));
        dashboard.put("severityCounts", severityCounts);

        List<ConflictDetail> topConflicts = conflicts.stream()
                .sorted((c1, c2) -> {
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
}
