package com.heronix.controller;

import com.heronix.model.domain.ReportHistory;
import com.heronix.model.domain.ReportHistory.ReportFormat;
import com.heronix.model.domain.ReportHistory.ReportStatus;
import com.heronix.model.domain.ReportHistory.ReportType;
import com.heronix.service.ReportHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Report History API Controller
 *
 * REST API endpoints for report generation history and metadata management.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 50 - Report API Endpoints
 */
@Slf4j
// @RestController  // Disabled for API server mode
@RequestMapping("/api/report-history")
@RequiredArgsConstructor
public class ReportHistoryApiController {

    private final ReportHistoryService historyService;

    /**
     * Record a new report generation
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> recordReportGeneration(@RequestBody Map<String, Object> reportData) {
        try {
            ReportType reportType = ReportType.valueOf((String) reportData.get("reportType"));
            ReportFormat reportFormat = ReportFormat.valueOf((String) reportData.get("reportFormat"));
            String reportName = (String) reportData.get("reportName");
            String filePath = (String) reportData.get("filePath");
            Long fileSize = Long.parseLong(reportData.get("fileSize").toString());
            LocalDate startDate = reportData.get("startDate") != null ?
                LocalDate.parse((String) reportData.get("startDate")) : null;
            LocalDate endDate = reportData.get("endDate") != null ?
                LocalDate.parse((String) reportData.get("endDate")) : null;
            String generatedBy = (String) reportData.get("generatedBy");
            Boolean scheduled = (Boolean) reportData.getOrDefault("scheduled", false);

            ReportHistory history = historyService.recordReportGeneration(
                reportType, reportFormat, reportName, filePath, fileSize,
                startDate, endDate, generatedBy, scheduled
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Report generation recorded successfully");
            response.put("reportId", history.getId());
            response.put("reportName", history.getReportName());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to record report generation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Record report generation failure
     */
    @PostMapping("/failure")
    public ResponseEntity<Map<String, Object>> recordReportFailure(@RequestBody Map<String, Object> failureData) {
        try {
            ReportType reportType = ReportType.valueOf((String) failureData.get("reportType"));
            ReportFormat reportFormat = ReportFormat.valueOf((String) failureData.get("reportFormat"));
            String errorMessage = (String) failureData.get("errorMessage");
            String generatedBy = (String) failureData.get("generatedBy");

            ReportHistory history = historyService.recordReportFailure(
                reportType, reportFormat, errorMessage, generatedBy
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Report failure recorded");
            response.put("reportId", history.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to record report failure: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Record report access
     */
    @PostMapping("/{reportId}/access")
    public ResponseEntity<Map<String, Object>> recordReportAccess(@PathVariable Long reportId) {
        try {
            historyService.recordReportAccess(reportId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Report access recorded");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to record report access: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Mark report as emailed
     */
    @PutMapping("/{reportId}/emailed")
    public ResponseEntity<Map<String, Object>> markReportAsEmailed(@PathVariable Long reportId) {
        try {
            historyService.markReportAsEmailed(reportId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Report marked as emailed");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to mark report as emailed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get report history by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getReportHistory(@PathVariable Long id) {
        try {
            Optional<ReportHistory> history = historyService.getReportHistory(id);

            if (history.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("report", history.get());
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Report history not found with ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to get report history: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get all report history
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllReportHistory() {
        List<ReportHistory> reports = historyService.getAllReportHistory();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("reports", reports);
        response.put("count", reports.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Get recent reports
     */
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentReports(
            @RequestParam(defaultValue = "30") int days) {

        List<ReportHistory> reports = historyService.getRecentReports(days);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("reports", reports);
        response.put("count", reports.size());
        response.put("days", days);
        return ResponseEntity.ok(response);
    }

    /**
     * Get reports by type
     */
    @GetMapping("/type/{reportType}")
    public ResponseEntity<Map<String, Object>> getReportsByType(@PathVariable String reportType) {
        try {
            ReportType type = ReportType.valueOf(reportType);
            List<ReportHistory> reports = historyService.getReportsByType(type);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("reports", reports);
            response.put("count", reports.size());
            response.put("reportType", reportType);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Invalid report type: " + reportType);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Get reports by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getReportsByStatus(@PathVariable String status) {
        try {
            ReportStatus reportStatus = ReportStatus.valueOf(status);
            List<ReportHistory> reports = historyService.getReportsByStatus(reportStatus);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("reports", reports);
            response.put("count", reports.size());
            response.put("status", status);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Invalid report status: " + status);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Get reports by user
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<Map<String, Object>> getReportsByUser(@PathVariable String username) {
        List<ReportHistory> reports = historyService.getReportsByUser(username);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("reports", reports);
        response.put("count", reports.size());
        response.put("username", username);
        return ResponseEntity.ok(response);
    }

    /**
     * Get scheduled reports
     */
    @GetMapping("/scheduled")
    public ResponseEntity<Map<String, Object>> getScheduledReports() {
        List<ReportHistory> reports = historyService.getScheduledReports();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("reports", reports);
        response.put("count", reports.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Clean up old reports
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupOldReports(
            @RequestParam(defaultValue = "90") int retentionDays) {

        try {
            int cleanedCount = historyService.cleanupOldReports(retentionDays);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Old reports cleaned up successfully");
            response.put("cleanedCount", cleanedCount);
            response.put("retentionDays", retentionDays);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to cleanup old reports: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get report statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getReportStatistics() {
        ReportHistoryService.ReportStatistics stats = historyService.getReportStatistics();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("statistics", stats);
        return ResponseEntity.ok(response);
    }

    /**
     * Get paginated report history
     */
    @GetMapping("/paginated")
    public ResponseEntity<Map<String, Object>> getReportHistoryPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        ReportHistoryService.PagedReports pagedReports = historyService.getReportHistoryPaginated(page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("reports", pagedReports.content());
        response.put("currentPage", pagedReports.currentPage());
        response.put("pageSize", pagedReports.pageSize());
        response.put("totalElements", pagedReports.totalElements());
        response.put("totalPages", pagedReports.totalPages());
        response.put("hasNext", pagedReports.hasNext());
        response.put("hasPrevious", pagedReports.hasPrevious());
        return ResponseEntity.ok(response);
    }
}
