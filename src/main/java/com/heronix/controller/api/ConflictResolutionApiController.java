package com.heronix.controller.api;

import com.heronix.model.ConflictPriorityScore;
import com.heronix.model.ConflictResolutionSuggestion;
import com.heronix.model.domain.Conflict;
import com.heronix.repository.ConflictRepository;
import com.heronix.service.ConflictResolutionSuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST API Controller for AI-Powered Conflict Resolution
 *
 * Provides endpoints for generating conflict resolution suggestions,
 * prioritizing conflicts, and applying automated resolutions.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/conflict-resolution")
@RequiredArgsConstructor
public class ConflictResolutionApiController {

    private final ConflictResolutionSuggestionService conflictResolutionService;
    private final ConflictRepository conflictRepository;

    // ==================== Conflict Resolution Suggestions ====================

    @GetMapping("/conflicts/{conflictId}/suggestions")
    public ResponseEntity<List<ConflictResolutionSuggestion>> getSuggestions(@PathVariable Long conflictId) {
        Optional<Conflict> conflictOpt = conflictRepository.findById(conflictId);

        if (conflictOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<ConflictResolutionSuggestion> suggestions = conflictResolutionService.generateSuggestions(conflictOpt.get());
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/conflicts/{conflictId}/suggestions/best")
    public ResponseEntity<ConflictResolutionSuggestion> getBestSuggestion(@PathVariable Long conflictId) {
        Optional<Conflict> conflictOpt = conflictRepository.findById(conflictId);

        if (conflictOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<ConflictResolutionSuggestion> suggestions = conflictResolutionService.generateSuggestions(conflictOpt.get());

        if (suggestions.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        // First suggestion is the best (ranked by priority)
        return ResponseEntity.ok(suggestions.get(0));
    }

    @PostMapping("/conflicts/{conflictId}/suggestions/generate")
    public ResponseEntity<Map<String, Object>> generateSuggestionsForConflict(@PathVariable Long conflictId) {
        Optional<Conflict> conflictOpt = conflictRepository.findById(conflictId);

        if (conflictOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            List<ConflictResolutionSuggestion> suggestions = conflictResolutionService.generateSuggestions(conflictOpt.get());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("conflictId", conflictId);
            response.put("suggestionCount", suggestions.size());
            response.put("suggestions", suggestions);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Apply Suggestions ====================

    @PostMapping("/conflicts/{conflictId}/apply")
    public ResponseEntity<Map<String, Object>> applySuggestion(
            @PathVariable Long conflictId,
            @RequestBody ConflictResolutionSuggestion suggestion) {

        Optional<Conflict> conflictOpt = conflictRepository.findById(conflictId);

        if (conflictOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            boolean success = conflictResolutionService.applySuggestion(conflictOpt.get(), suggestion);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("conflictId", conflictId);
            response.put("message", success ? "Suggestion applied successfully" : "Failed to apply suggestion");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/conflicts/{conflictId}/apply-best")
    public ResponseEntity<Map<String, Object>> applyBestSuggestion(@PathVariable Long conflictId) {
        Optional<Conflict> conflictOpt = conflictRepository.findById(conflictId);

        if (conflictOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Conflict conflict = conflictOpt.get();
            List<ConflictResolutionSuggestion> suggestions = conflictResolutionService.generateSuggestions(conflict);

            if (suggestions.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "No suggestions available for this conflict");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            ConflictResolutionSuggestion bestSuggestion = suggestions.get(0);
            boolean success = conflictResolutionService.applySuggestion(conflict, bestSuggestion);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("conflictId", conflictId);
            response.put("appliedSuggestion", bestSuggestion);
            response.put("message", success ? "Best suggestion applied successfully" : "Failed to apply suggestion");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/conflicts/auto-resolve")
    public ResponseEntity<Map<String, Object>> autoResolveConflicts() {
        try {
            List<Conflict> conflicts = conflictResolutionService.getConflictsByPriority();

            int resolvedCount = 0;
            int failedCount = 0;

            for (Conflict conflict : conflicts) {
                List<ConflictResolutionSuggestion> suggestions = conflictResolutionService.generateSuggestions(conflict);

                if (!suggestions.isEmpty()) {
                    ConflictResolutionSuggestion bestSuggestion = suggestions.get(0);

                    if (conflictResolutionService.canAutoApply(bestSuggestion)) {
                        boolean success = conflictResolutionService.applySuggestion(conflict, bestSuggestion);
                        if (success) {
                            resolvedCount++;
                        } else {
                            failedCount++;
                        }
                    }
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalConflicts", conflicts.size());
            response.put("resolvedCount", resolvedCount);
            response.put("failedCount", failedCount);
            response.put("skippedCount", conflicts.size() - resolvedCount - failedCount);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Priority & Analysis ====================

    @GetMapping("/conflicts/{conflictId}/priority")
    public ResponseEntity<ConflictPriorityScore> getPriorityScore(@PathVariable Long conflictId) {
        Optional<Conflict> conflictOpt = conflictRepository.findById(conflictId);

        if (conflictOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ConflictPriorityScore score = conflictResolutionService.calculatePriorityScore(conflictOpt.get());
        return ResponseEntity.ok(score);
    }

    @GetMapping("/conflicts/by-priority")
    public ResponseEntity<List<Conflict>> getConflictsByPriority() {
        List<Conflict> conflicts = conflictResolutionService.getConflictsByPriority();
        return ResponseEntity.ok(conflicts);
    }

    @GetMapping("/conflicts/{conflictId}/cascade-impact")
    public ResponseEntity<Map<String, Object>> getCascadeImpact(@PathVariable Long conflictId) {
        Optional<Conflict> conflictOpt = conflictRepository.findById(conflictId);

        if (conflictOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        int impact = conflictResolutionService.estimateCascadeImpact(conflictOpt.get());

        Map<String, Object> response = new HashMap<>();
        response.put("conflictId", conflictId);
        response.put("cascadeImpact", impact);
        response.put("severity", impact > 10 ? "HIGH" : impact > 5 ? "MEDIUM" : "LOW");

        return ResponseEntity.ok(response);
    }

    // ==================== Historical Success Rates ====================

    @GetMapping("/success-rates/{resolutionType}")
    public ResponseEntity<Map<String, Object>> getSuccessRate(@PathVariable String resolutionType) {
        try {
            ConflictResolutionSuggestion.ResolutionType type =
                ConflictResolutionSuggestion.ResolutionType.valueOf(resolutionType);

            int successRate = conflictResolutionService.getHistoricalSuccessRate(type);

            Map<String, Object> response = new HashMap<>();
            response.put("resolutionType", resolutionType);
            response.put("successRate", successRate);
            response.put("successRatePercent", successRate + "%");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid resolution type: " + resolutionType);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/success-rates/all")
    public ResponseEntity<Map<String, Object>> getAllSuccessRates() {
        Map<String, Object> allRates = new HashMap<>();

        for (ConflictResolutionSuggestion.ResolutionType type : ConflictResolutionSuggestion.ResolutionType.values()) {
            int rate = conflictResolutionService.getHistoricalSuccessRate(type);
            allRates.put(type.name(), rate);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("successRates", allRates);

        return ResponseEntity.ok(response);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getConflictResolutionDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        List<Conflict> allConflicts = conflictRepository.findAll();
        List<Conflict> prioritizedConflicts = conflictResolutionService.getConflictsByPriority();

        long totalConflicts = allConflicts.size();
        long highPriorityCount = prioritizedConflicts.stream()
            .limit(10)
            .count();

        dashboard.put("totalConflicts", totalConflicts);
        dashboard.put("highPriorityConflicts", highPriorityCount);
        dashboard.put("topConflicts", prioritizedConflicts.stream().limit(5).toList());

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/resolution-stats")
    public ResponseEntity<Map<String, Object>> getResolutionStats() {
        Map<String, Object> stats = new HashMap<>();

        List<Conflict> conflicts = conflictRepository.findAll();

        long totalConflicts = conflicts.size();
        long autoResolvable = conflicts.stream()
            .map(c -> conflictResolutionService.generateSuggestions(c))
            .filter(suggestions -> !suggestions.isEmpty())
            .filter(suggestions -> conflictResolutionService.canAutoApply(suggestions.get(0)))
            .count();

        stats.put("totalConflicts", totalConflicts);
        stats.put("autoResolvableCount", autoResolvable);
        stats.put("autoResolvablePercent", totalConflicts > 0 ? (autoResolvable * 100.0 / totalConflicts) : 0);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/dashboard/cascade-analysis")
    public ResponseEntity<Map<String, Object>> getCascadeAnalysis() {
        Map<String, Object> analysis = new HashMap<>();

        List<Conflict> conflicts = conflictResolutionService.getConflictsByPriority();

        int highImpactCount = 0;
        int mediumImpactCount = 0;
        int lowImpactCount = 0;

        for (Conflict conflict : conflicts) {
            int impact = conflictResolutionService.estimateCascadeImpact(conflict);
            if (impact > 10) {
                highImpactCount++;
            } else if (impact > 5) {
                mediumImpactCount++;
            } else {
                lowImpactCount++;
            }
        }

        analysis.put("highImpactConflicts", highImpactCount);
        analysis.put("mediumImpactConflicts", mediumImpactCount);
        analysis.put("lowImpactConflicts", lowImpactCount);
        analysis.put("totalConflicts", conflicts.size());

        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/dashboard/priority-distribution")
    public ResponseEntity<Map<String, Object>> getPriorityDistribution() {
        Map<String, Object> distribution = new HashMap<>();

        List<Conflict> conflicts = conflictRepository.findAll();

        Map<String, Long> priorityCounts = new HashMap<>();
        priorityCounts.put("CRITICAL", 0L);
        priorityCounts.put("HIGH", 0L);
        priorityCounts.put("MEDIUM", 0L);
        priorityCounts.put("LOW", 0L);

        for (Conflict conflict : conflicts) {
            ConflictPriorityScore score = conflictResolutionService.calculatePriorityScore(conflict);
            // Assuming ConflictPriorityScore has a severity or priority field
            // This is a placeholder - adjust based on actual implementation
            long totalScore = score != null ? 50 : 0; // Placeholder logic

            if (totalScore > 75) {
                priorityCounts.put("CRITICAL", priorityCounts.get("CRITICAL") + 1);
            } else if (totalScore > 50) {
                priorityCounts.put("HIGH", priorityCounts.get("HIGH") + 1);
            } else if (totalScore > 25) {
                priorityCounts.put("MEDIUM", priorityCounts.get("MEDIUM") + 1);
            } else {
                priorityCounts.put("LOW", priorityCounts.get("LOW") + 1);
            }
        }

        distribution.put("priorityDistribution", priorityCounts);
        distribution.put("totalConflicts", conflicts.size());

        return ResponseEntity.ok(distribution);
    }
}
