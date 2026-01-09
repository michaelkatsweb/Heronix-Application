package com.heronix.controller;

import com.heronix.dto.ReportShare;
import com.heronix.service.ReportShareService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/report-share")
@RequiredArgsConstructor
public class ReportShareApiController {
    private final ReportShareService shareService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createShare(@RequestBody ReportShare share) {
        try {
            ReportShare created = shareService.createShare(share);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("shareId", created.getShareId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{shareId}")
    public ResponseEntity<Map<String, Object>> getShare(@PathVariable Long shareId) {
        try {
            ReportShare share = shareService.getShare(shareId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("share", share);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PostMapping("/{shareId}/revoke")
    public ResponseEntity<Map<String, Object>> revokeShare(@PathVariable Long shareId) {
        try {
            shareService.revokeShare(shareId);
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

    @DeleteMapping("/{shareId}")
    public ResponseEntity<Map<String, Object>> deleteShare(@PathVariable Long shareId) {
        try {
            shareService.deleteShare(shareId);
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
        response.put("statistics", shareService.getStatistics());
        return ResponseEntity.ok(response);
    }
}
