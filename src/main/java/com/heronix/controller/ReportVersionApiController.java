package com.heronix.controller;

import com.heronix.dto.ReportVersion;
import com.heronix.service.ReportVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/report-version")
@RequiredArgsConstructor
public class ReportVersionApiController {
    private final ReportVersionService versionService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createVersion(@RequestBody ReportVersion version) {
        try {
            ReportVersion created = versionService.createVersion(version);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("versionId", created.getVersionId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{versionId}")
    public ResponseEntity<Map<String, Object>> getVersion(@PathVariable Long versionId) {
        try {
            ReportVersion version = versionService.getVersion(versionId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("version", version);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/report/{reportId}")
    public ResponseEntity<Map<String, Object>> getVersionsByReport(@PathVariable Long reportId) {
        List<ReportVersion> versions = versionService.getVersionsByReport(reportId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("versions", versions);
        response.put("count", versions.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/report/{reportId}/latest")
    public ResponseEntity<Map<String, Object>> getLatestVersion(@PathVariable Long reportId) {
        ReportVersion version = versionService.getLatestVersion(reportId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("version", version);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{versionId}")
    public ResponseEntity<Map<String, Object>> deleteVersion(@PathVariable Long versionId) {
        try {
            versionService.deleteVersion(versionId);
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
        response.put("statistics", versionService.getStatistics());
        return ResponseEntity.ok(response);
    }
}
