package com.heronix.controller.api;

import com.heronix.model.domain.Schedule;
import com.heronix.model.dto.ScheduleHealthMetrics;
import com.heronix.repository.ScheduleRepository;
import com.heronix.service.ScheduleHealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST API Controller for Schedule Health Monitoring
 *
 * Provides endpoints for calculating and monitoring schedule health metrics,
 * quality scoring, and compliance validation.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/schedule-health")
@RequiredArgsConstructor
public class ScheduleHealthApiController {

    private final ScheduleHealthService scheduleHealthService;
    private final ScheduleRepository scheduleRepository;

    // ==================== Health Metrics ====================

    @GetMapping("/{scheduleId}/metrics")
    public ResponseEntity<ScheduleHealthMetrics> getHealthMetrics(@PathVariable Long scheduleId) {
        Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);

        if (scheduleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ScheduleHealthMetrics metrics = scheduleHealthService.calculateHealthMetrics(scheduleOpt.get());
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/{scheduleId}/score")
    public ResponseEntity<Map<String, Object>> getHealthScore(@PathVariable Long scheduleId) {
        Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);

        if (scheduleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Double score = scheduleHealthService.calculateHealthScore(scheduleOpt.get());
        String summary = scheduleHealthService.getHealthSummary(scheduleOpt.get());

        Map<String, Object> response = new HashMap<>();
        response.put("scheduleId", scheduleId);
        response.put("healthScore", score);
        response.put("summary", summary);
        response.put("grade", getScoreGrade(score));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{scheduleId}/summary")
    public ResponseEntity<Map<String, Object>> getHealthSummary(@PathVariable Long scheduleId) {
        Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);

        if (scheduleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Schedule schedule = scheduleOpt.get();
        String summary = scheduleHealthService.getHealthSummary(schedule);
        boolean acceptable = scheduleHealthService.isScheduleAcceptable(schedule);

        Map<String, Object> response = new HashMap<>();
        response.put("scheduleId", scheduleId);
        response.put("summary", summary);
        response.put("acceptable", acceptable);
        response.put("status", acceptable ? "PRODUCTION_READY" : "NEEDS_IMPROVEMENT");

        return ResponseEntity.ok(response);
    }

    // ==================== Component Scores ====================

    @GetMapping("/{scheduleId}/scores/conflict")
    public ResponseEntity<Map<String, Object>> getConflictScore(@PathVariable Long scheduleId) {
        Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);

        if (scheduleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Double score = scheduleHealthService.calculateConflictScore(scheduleOpt.get());

        Map<String, Object> response = new HashMap<>();
        response.put("scheduleId", scheduleId);
        response.put("conflictScore", score);
        response.put("grade", getScoreGrade(score));
        response.put("description", "100 = no conflicts, 0 = severe conflicts");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{scheduleId}/scores/balance")
    public ResponseEntity<Map<String, Object>> getBalanceScore(@PathVariable Long scheduleId) {
        Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);

        if (scheduleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Double score = scheduleHealthService.calculateBalanceScore(scheduleOpt.get());

        Map<String, Object> response = new HashMap<>();
        response.put("scheduleId", scheduleId);
        response.put("balanceScore", score);
        response.put("grade", getScoreGrade(score));
        response.put("description", "Measures student distribution across sections");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{scheduleId}/scores/utilization")
    public ResponseEntity<Map<String, Object>> getUtilizationScore(@PathVariable Long scheduleId) {
        Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);

        if (scheduleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Double score = scheduleHealthService.calculateUtilizationScore(scheduleOpt.get());

        Map<String, Object> response = new HashMap<>();
        response.put("scheduleId", scheduleId);
        response.put("utilizationScore", score);
        response.put("grade", getScoreGrade(score));
        response.put("description", "Measures teacher and room utilization efficiency");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{scheduleId}/scores/compliance")
    public ResponseEntity<Map<String, Object>> getComplianceScore(@PathVariable Long scheduleId) {
        Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);

        if (scheduleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Double score = scheduleHealthService.calculateComplianceScore(scheduleOpt.get());

        Map<String, Object> response = new HashMap<>();
        response.put("scheduleId", scheduleId);
        response.put("complianceScore", score);
        response.put("grade", getScoreGrade(score));
        response.put("description", "Adherence to prep time, IEP, and other requirements");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{scheduleId}/scores/coverage")
    public ResponseEntity<Map<String, Object>> getCoverageScore(@PathVariable Long scheduleId) {
        Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);

        if (scheduleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Double score = scheduleHealthService.calculateCoverageScore(scheduleOpt.get());

        Map<String, Object> response = new HashMap<>();
        response.put("scheduleId", scheduleId);
        response.put("coverageScore", score);
        response.put("grade", getScoreGrade(score));
        response.put("description", "Percentage of students fully scheduled");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{scheduleId}/scores/all")
    public ResponseEntity<Map<String, Object>> getAllScores(@PathVariable Long scheduleId) {
        Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);

        if (scheduleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Schedule schedule = scheduleOpt.get();

        Map<String, Object> scores = new HashMap<>();
        scores.put("overall", scheduleHealthService.calculateHealthScore(schedule));
        scores.put("conflict", scheduleHealthService.calculateConflictScore(schedule));
        scores.put("balance", scheduleHealthService.calculateBalanceScore(schedule));
        scores.put("utilization", scheduleHealthService.calculateUtilizationScore(schedule));
        scores.put("compliance", scheduleHealthService.calculateComplianceScore(schedule));
        scores.put("coverage", scheduleHealthService.calculateCoverageScore(schedule));

        Map<String, Object> response = new HashMap<>();
        response.put("scheduleId", scheduleId);
        response.put("scores", scores);
        response.put("summary", scheduleHealthService.getHealthSummary(schedule));

        return ResponseEntity.ok(response);
    }

    // ==================== Validation ====================

    @GetMapping("/{scheduleId}/acceptable")
    public ResponseEntity<Map<String, Object>> checkAcceptable(@PathVariable Long scheduleId) {
        Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);

        if (scheduleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Schedule schedule = scheduleOpt.get();
        boolean acceptable = scheduleHealthService.isScheduleAcceptable(schedule);
        Double healthScore = scheduleHealthService.calculateHealthScore(schedule);

        Map<String, Object> response = new HashMap<>();
        response.put("scheduleId", scheduleId);
        response.put("acceptable", acceptable);
        response.put("healthScore", healthScore);
        response.put("status", acceptable ? "PRODUCTION_READY" : "NEEDS_IMPROVEMENT");
        response.put("recommendation", acceptable ?
            "Schedule meets minimum quality thresholds and is ready for production use" :
            "Schedule needs improvement before being deployed to production");

        return ResponseEntity.ok(response);
    }

    // ==================== Batch Operations ====================

    @GetMapping("/compare")
    public ResponseEntity<Map<String, Object>> compareSchedules(
            @RequestParam List<Long> scheduleIds) {

        Map<String, Object> comparison = new HashMap<>();

        for (Long scheduleId : scheduleIds) {
            Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);
            if (scheduleOpt.isPresent()) {
                Schedule schedule = scheduleOpt.get();
                Map<String, Object> scheduleData = new HashMap<>();
                scheduleData.put("healthScore", scheduleHealthService.calculateHealthScore(schedule));
                scheduleData.put("summary", scheduleHealthService.getHealthSummary(schedule));
                scheduleData.put("acceptable", scheduleHealthService.isScheduleAcceptable(schedule));
                comparison.put("schedule_" + scheduleId, scheduleData);
            }
        }

        return ResponseEntity.ok(comparison);
    }

    @GetMapping("/best")
    public ResponseEntity<Map<String, Object>> findBestSchedule() {
        List<Schedule> allSchedules = scheduleRepository.findAll();

        if (allSchedules.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        Schedule bestSchedule = null;
        Double bestScore = 0.0;

        for (Schedule schedule : allSchedules) {
            Double score = scheduleHealthService.calculateHealthScore(schedule);
            if (score != null && score > bestScore) {
                bestScore = score;
                bestSchedule = schedule;
            }
        }

        if (bestSchedule == null) {
            return ResponseEntity.noContent().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("scheduleId", bestSchedule.getId());
        response.put("healthScore", bestScore);
        response.put("summary", scheduleHealthService.getHealthSummary(bestSchedule));
        response.put("acceptable", scheduleHealthService.isScheduleAcceptable(bestSchedule));

        return ResponseEntity.ok(response);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getHealthDashboard() {
        List<Schedule> allSchedules = scheduleRepository.findAll();

        int totalSchedules = allSchedules.size();
        int acceptableCount = 0;
        double averageScore = 0.0;

        for (Schedule schedule : allSchedules) {
            if (scheduleHealthService.isScheduleAcceptable(schedule)) {
                acceptableCount++;
            }
            Double score = scheduleHealthService.calculateHealthScore(schedule);
            if (score != null) {
                averageScore += score;
            }
        }

        if (totalSchedules > 0) {
            averageScore /= totalSchedules;
        }

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalSchedules", totalSchedules);
        dashboard.put("acceptableSchedules", acceptableCount);
        dashboard.put("unacceptableSchedules", totalSchedules - acceptableCount);
        dashboard.put("averageHealthScore", averageScore);
        dashboard.put("acceptanceRate", totalSchedules > 0 ? (acceptableCount * 100.0 / totalSchedules) : 0);

        return ResponseEntity.ok(dashboard);
    }

    // ==================== Helper Methods ====================

    private String getScoreGrade(Double score) {
        if (score == null) {
            return "UNKNOWN";
        }
        if (score >= 90) {
            return "EXCELLENT";
        } else if (score >= 80) {
            return "GOOD";
        } else if (score >= 70) {
            return "FAIR";
        } else if (score >= 60) {
            return "POOR";
        } else {
            return "FAILING";
        }
    }
}
