package com.heronix.controller;

import com.heronix.dto.ReportMonitor;
import com.heronix.service.ReportMonitorService;
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
@RequestMapping("/api/report-monitor")
@RequiredArgsConstructor
public class ReportMonitorApiController {

    private final ReportMonitorService monitorService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createMonitor(@RequestBody ReportMonitor monitor) {
        try {
            ReportMonitor created = monitorService.createMonitor(monitor);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Monitor created successfully");
            response.put("monitorId", created.getMonitorId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{monitorId}")
    public ResponseEntity<Map<String, Object>> getMonitor(@PathVariable Long monitorId) {
        try {
            ReportMonitor monitor = monitorService.getMonitor(monitorId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("monitor", monitor);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PostMapping("/{monitorId}/health-check")
    public ResponseEntity<Map<String, Object>> checkHealth(@PathVariable Long monitorId) {
        try {
            ReportMonitor monitor = monitorService.checkHealth(monitorId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("isHealthy", true);
            response.put("healthScore", monitor.getHealthScore());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @DeleteMapping("/{monitorId}")
    public ResponseEntity<Map<String, Object>> deleteMonitor(@PathVariable Long monitorId) {
        try {
            monitorService.deleteMonitor(monitorId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Monitor deleted successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = monitorService.getStatistics();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("statistics", stats);
        return ResponseEntity.ok(response);
    }
}
