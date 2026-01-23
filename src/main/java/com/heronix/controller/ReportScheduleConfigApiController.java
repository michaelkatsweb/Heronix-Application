package com.heronix.controller;

import com.heronix.dto.ReportScheduleConfig;
import com.heronix.service.ReportScheduleConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
// @RestController  // Disabled for API server mode
@RequestMapping("/api/schedule-config")
@RequiredArgsConstructor
public class ReportScheduleConfigApiController {
    private final ReportScheduleConfigService scheduleService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createSchedule(@RequestBody ReportScheduleConfig schedule) {
        try {
            ReportScheduleConfig created = scheduleService.createSchedule(schedule);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("scheduleId", created.getScheduleId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{scheduleId}")
    public ResponseEntity<Map<String, Object>> getSchedule(@PathVariable Long scheduleId) {
        try {
            ReportScheduleConfig schedule = scheduleService.getSchedule(scheduleId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("schedule", schedule);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PostMapping("/{scheduleId}/enable")
    public ResponseEntity<Map<String, Object>> enableSchedule(@PathVariable Long scheduleId) {
        try {
            scheduleService.enableSchedule(scheduleId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Map<String, Object>> deleteSchedule(@PathVariable Long scheduleId) {
        try {
            scheduleService.deleteSchedule(scheduleId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("statistics", scheduleService.getStatistics());
        return ResponseEntity.ok(response);
    }
}
