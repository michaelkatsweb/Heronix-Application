package com.heronix.controller;

import com.heronix.model.domain.ReportHistory;
import com.heronix.model.domain.ReportHistory.ReportStatus;
import com.heronix.model.domain.ReportHistory.ReportType;
import com.heronix.service.ReportHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Report Management API Controller
 *
 * Provides REST API endpoints for managing report history and metadata.
 * Allows viewing, tracking, and managing generated reports.
 *
 * Endpoints:
 * - GET /api/reports/management/history - Get all report history
 * - GET /api/reports/management/history/{id} - Get specific report history
 * - GET /api/reports/management/recent - Get recent reports (last 30 days)
 * - GET /api/reports/management/type/{type} - Get reports by type
 * - GET /api/reports/management/status/{status} - Get reports by status
 * - GET /api/reports/management/scheduled - Get scheduled reports
 * - GET /api/reports/management/statistics - Get report statistics
 * - POST /api/reports/management/cleanup - Clean up old reports
 * - POST /api/reports/management/{id}/access - Record report access
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 50 - Report API Endpoints
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/reports/management")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ReportManagementApiController {

    private final ReportHistoryService reportHistoryService;

    /**
     * Get all report history
     *
     * GET /api/reports/management/history
     *
     * @return List of all report history entries
     */
    @GetMapping("/history")
    public ResponseEntity<List<ReportHistory>> getAllReportHistory() {
        log.info("Fetching all report history");
        List<ReportHistory> history = reportHistoryService.getAllReportHistory();
        return ResponseEntity.ok(history);
    }

    /**
     * Get specific report history by ID
     *
     * GET /api/reports/management/history/{id}
     *
     * @param id Report history ID
     * @return Report history entry
     */
    @GetMapping("/history/{id}")
    public ResponseEntity<ReportHistory> getReportHistory(@PathVariable Long id) {
        log.info("Fetching report history for ID: {}", id);
        Optional<ReportHistory> history = reportHistoryService.getReportHistory(id);
        return history.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get recent reports (last 30 days by default)
     *
     * GET /api/reports/management/recent?days=30
     *
     * @param days Number of days to look back (default: 30)
     * @return List of recent report history entries
     */
    @GetMapping("/recent")
    public ResponseEntity<List<ReportHistory>> getRecentReports(
            @RequestParam(required = false, defaultValue = "30") int days) {
        log.info("Fetching reports from last {} days", days);
        List<ReportHistory> history = reportHistoryService.getRecentReports(days);
        return ResponseEntity.ok(history);
    }

    /**
     * Get reports by type
     *
     * GET /api/reports/management/type/{type}
     *
     * @param type Report type (DAILY_ATTENDANCE, STUDENT_SUMMARY, etc.)
     * @return List of report history entries
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<ReportHistory>> getReportsByType(@PathVariable ReportType type) {
        log.info("Fetching reports of type: {}", type);
        List<ReportHistory> history = reportHistoryService.getReportsByType(type);
        return ResponseEntity.ok(history);
    }

    /**
     * Get reports by status
     *
     * GET /api/reports/management/status/{status}
     *
     * @param status Report status (COMPLETED, FAILED, etc.)
     * @return List of report history entries
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReportHistory>> getReportsByStatus(@PathVariable ReportStatus status) {
        log.info("Fetching reports with status: {}", status);
        List<ReportHistory> history = reportHistoryService.getReportsByStatus(status);
        return ResponseEntity.ok(history);
    }

    /**
     * Get scheduled reports
     *
     * GET /api/reports/management/scheduled
     *
     * @return List of scheduled report history entries
     */
    @GetMapping("/scheduled")
    public ResponseEntity<List<ReportHistory>> getScheduledReports() {
        log.info("Fetching scheduled reports");
        List<ReportHistory> history = reportHistoryService.getScheduledReports();
        return ResponseEntity.ok(history);
    }

    /**
     * Get report statistics
     *
     * GET /api/reports/management/statistics
     *
     * @return Report statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ReportHistoryService.ReportStatistics> getReportStatistics() {
        log.info("Fetching report statistics");
        ReportHistoryService.ReportStatistics stats = reportHistoryService.getReportStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Clean up old reports
     *
     * POST /api/reports/management/cleanup?retentionDays=90
     *
     * @param retentionDays Number of days to retain reports (default: 90)
     * @return Number of reports cleaned up
     */
    @PostMapping("/cleanup")
    public ResponseEntity<CleanupResult> cleanupOldReports(
            @RequestParam(required = false, defaultValue = "90") int retentionDays) {
        log.info("Starting cleanup of reports older than {} days", retentionDays);
        int cleanedCount = reportHistoryService.cleanupOldReports(retentionDays);
        return ResponseEntity.ok(new CleanupResult(cleanedCount, retentionDays));
    }

    /**
     * Record report access/download
     *
     * POST /api/reports/management/{id}/access
     *
     * @param id Report history ID
     * @return OK status
     */
    @PostMapping("/{id}/access")
    public ResponseEntity<Void> recordReportAccess(@PathVariable Long id) {
        log.info("Recording access for report ID: {}", id);
        reportHistoryService.recordReportAccess(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Cleanup Result DTO
     */
    public record CleanupResult(int cleanedCount, int retentionDays) {}
}
