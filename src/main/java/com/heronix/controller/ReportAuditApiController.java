package com.heronix.controller;

import com.heronix.service.ReportAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Report Audit API Controller
 *
 * REST API endpoints for audit logging and compliance tracking.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 64 - Report Audit Trail & Compliance Logging
 */
@Slf4j
// @RestController  // Disabled for API server mode
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class ReportAuditApiController {

    private final ReportAuditService auditService;

    /**
     * Log report access
     */
    @PostMapping("/access")
    public ResponseEntity<Map<String, Object>> logReportAccess(@RequestBody Map<String, Object> accessData) {
        try {
            String username = (String) accessData.get("username");
            String reportType = (String) accessData.get("reportType");
            Long reportId = Long.parseLong(accessData.get("reportId").toString());
            String ipAddress = (String) accessData.getOrDefault("ipAddress", "unknown");
            Boolean success = (Boolean) accessData.getOrDefault("success", true);

            auditService.logReportAccess(username, reportType, reportId, ipAddress, success);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Report access logged successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to log report access: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Log report download
     */
    @PostMapping("/download")
    public ResponseEntity<Map<String, Object>> logReportDownload(@RequestBody Map<String, Object> downloadData) {
        try {
            String username = (String) downloadData.get("username");
            String reportType = (String) downloadData.get("reportType");
            Long reportId = Long.parseLong(downloadData.get("reportId").toString());
            String format = (String) downloadData.getOrDefault("format", "PDF");
            String ipAddress = (String) downloadData.getOrDefault("ipAddress", "unknown");

            auditService.logReportDownload(username, reportType, reportId, format, ipAddress);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Report download logged successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to log report download: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get audit service health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("service", "ReportAuditService");
        response.put("status", "ACTIVE");
        response.put("message", "Audit logging service is operational");
        return ResponseEntity.ok(response);
    }
}
