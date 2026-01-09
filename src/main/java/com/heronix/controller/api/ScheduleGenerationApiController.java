package com.heronix.controller.api;

import com.heronix.model.domain.Schedule;
import com.heronix.model.dto.ScheduleGenerationRequest;
import com.heronix.model.dto.ScheduleRequest;
import com.heronix.repository.ScheduleRepository;
import com.heronix.service.ScheduleGenerationService;
import com.heronix.service.ScheduleOptimizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST API Controller for Schedule Generation and Optimization
 *
 * Provides AI-powered schedule generation and optimization endpoints using
 * OptaPlanner constraint solving.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/schedule-generation")
@RequiredArgsConstructor
public class ScheduleGenerationApiController {

    private final ScheduleGenerationService scheduleGenerationService;
    private final ScheduleOptimizationService scheduleOptimizationService;
    private final ScheduleRepository scheduleRepository;

    // ==================== Schedule Generation ====================

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateSchedule(
            @RequestBody ScheduleGenerationRequest request) {

        try {
            // Use null progress callback for synchronous API call
            Schedule schedule = scheduleGenerationService.generateSchedule(request, null);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("schedule", schedule);
            response.put("scheduleId", schedule.getId());
            response.put("status", schedule.getStatus());
            response.put("totalSlots", schedule.getSlots() != null ? schedule.getSlots().size() : 0);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/generate/async")
    public ResponseEntity<Map<String, Object>> generateScheduleAsync(
            @RequestBody ScheduleGenerationRequest request) {

        try {
            // Use null progress callback for async API call
            Schedule schedule = scheduleGenerationService.generateSchedule(request, null, true);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Schedule generation started asynchronously");
            response.put("scheduleId", schedule.getId());
            response.put("status", schedule.getStatus());

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/status/{scheduleId}")
    public ResponseEntity<Map<String, Object>> getGenerationStatus(@PathVariable Long scheduleId) {
        Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);

        if (scheduleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Schedule schedule = scheduleOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("scheduleId", scheduleId);
        response.put("status", schedule.getStatus());
        response.put("totalSlots", schedule.getSlots() != null ? schedule.getSlots().size() : 0);
        response.put("createdDate", schedule.getCreatedDate());
        response.put("createdBy", schedule.getCreatedBy());

        return ResponseEntity.ok(response);
    }

    // ==================== Schedule Optimization ====================

    @PostMapping("/optimize")
    public ResponseEntity<Map<String, Object>> optimizeSchedule(@RequestBody ScheduleRequest request) {
        try {
            Schedule optimized = scheduleOptimizationService.optimizeSchedule(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("schedule", optimized);
            response.put("scheduleId", optimized.getId());
            response.put("optimizationScore", scheduleOptimizationService.calculateOptimizationScore(optimized));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/{scheduleId}/optimize")
    public ResponseEntity<Map<String, Object>> optimizeExistingSchedule(@PathVariable Long scheduleId) {
        Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);

        if (scheduleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            ScheduleRequest request = new ScheduleRequest();

            Schedule optimized = scheduleOptimizationService.optimizeSchedule(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("schedule", optimized);
            response.put("scheduleId", optimized.getId());
            response.put("optimizationScore", scheduleOptimizationService.calculateOptimizationScore(optimized));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{scheduleId}/optimization-score")
    public ResponseEntity<Map<String, Object>> getOptimizationScore(@PathVariable Long scheduleId) {
        Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);

        if (scheduleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Schedule schedule = scheduleOpt.get();
        double score = scheduleOptimizationService.calculateOptimizationScore(schedule);

        Map<String, Object> response = new HashMap<>();
        response.put("scheduleId", scheduleId);
        response.put("optimizationScore", score);

        return ResponseEntity.ok(response);
    }

    // ==================== Optimization Strategies ====================

    @PostMapping("/{scheduleId}/apply-kanban")
    public ResponseEntity<Map<String, Object>> applyKanban(@PathVariable Long scheduleId) {
        Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);

        if (scheduleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Schedule schedule = scheduleOpt.get();
            scheduleOptimizationService.applyKanbanPrinciples(schedule);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Kanban principles applied successfully");
            response.put("scheduleId", scheduleId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/{scheduleId}/apply-eisenhower")
    public ResponseEntity<Map<String, Object>> applyEisenhower(@PathVariable Long scheduleId) {
        Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);

        if (scheduleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Schedule schedule = scheduleOpt.get();
            scheduleOptimizationService.applyEisenhowerMatrix(schedule);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Eisenhower Matrix applied successfully");
            response.put("scheduleId", scheduleId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/{scheduleId}/apply-lean-six-sigma")
    public ResponseEntity<Map<String, Object>> applyLeanSixSigma(@PathVariable Long scheduleId) {
        Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);

        if (scheduleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Schedule schedule = scheduleOpt.get();
            scheduleOptimizationService.applyLeanSixSigma(schedule);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lean Six Sigma principles applied successfully");
            response.put("scheduleId", scheduleId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Machine Learning ====================

    @PostMapping("/ml/train")
    public ResponseEntity<Map<String, Object>> trainMLModel() {
        try {
            List<Schedule> historicalSchedules = scheduleRepository.findAll();
            scheduleOptimizationService.trainMLModel(historicalSchedules);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "ML model training started");
            response.put("trainingDataSize", historicalSchedules.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/ml/train/custom")
    public ResponseEntity<Map<String, Object>> trainMLModelCustom(@RequestBody List<Long> scheduleIds) {
        try {
            List<Schedule> historicalSchedules = scheduleRepository.findAllById(scheduleIds);
            scheduleOptimizationService.trainMLModel(historicalSchedules);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "ML model training started with custom dataset");
            response.put("trainingDataSize", historicalSchedules.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getGenerationDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        List<Schedule> allSchedules = scheduleRepository.findAll();

        long totalSchedules = allSchedules.size();
        long activeSchedules = allSchedules.stream()
            .filter(s -> s.getStatus() != null && s.getStatus().name().equals("ACTIVE"))
            .count();

        dashboard.put("totalSchedules", totalSchedules);
        dashboard.put("activeSchedules", activeSchedules);
        dashboard.put("recentSchedules", allSchedules.stream().limit(5).toList());

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/optimization-stats")
    public ResponseEntity<Map<String, Object>> getOptimizationStats() {
        Map<String, Object> stats = new HashMap<>();

        List<Schedule> allSchedules = scheduleRepository.findAll();

        if (!allSchedules.isEmpty()) {
            double avgScore = allSchedules.stream()
                .mapToDouble(s -> scheduleOptimizationService.calculateOptimizationScore(s))
                .average()
                .orElse(0.0);

            stats.put("totalSchedules", allSchedules.size());
            stats.put("averageOptimizationScore", avgScore);
        } else {
            stats.put("totalSchedules", 0);
            stats.put("averageOptimizationScore", 0.0);
            stats.put("message", "No schedules available for analysis");
        }

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/dashboard/generation-history")
    public ResponseEntity<Map<String, Object>> getGenerationHistory(
            @RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> history = new HashMap<>();

        List<Schedule> recentSchedules = scheduleRepository.findAll().stream()
            .limit(limit)
            .toList();

        history.put("count", recentSchedules.size());
        history.put("schedules", recentSchedules);

        return ResponseEntity.ok(history);
    }
}
