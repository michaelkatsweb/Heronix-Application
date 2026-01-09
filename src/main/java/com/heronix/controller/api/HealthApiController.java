package com.heronix.controller.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Check API Controller
 *
 * Provides simple health check endpoint for Teacher Portal network monitoring
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@RestController
@RequestMapping("/api")
@Slf4j
@CrossOrigin(origins = "*")
public class HealthApiController {

    /**
     * Simple health check endpoint
     *
     * Used by Teacher Portal NetworkMonitorService to check server connectivity
     *
     * @return Health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Heronix-SIS");
        response.put("version", "1.0.0");
        response.put("timestamp", LocalDateTime.now());

        log.debug("Health check requested");

        return ResponseEntity.ok(response);
    }
}
