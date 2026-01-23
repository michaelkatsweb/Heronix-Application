package com.heronix.controller;

import com.heronix.dto.ReportAnalytics;
import com.heronix.service.ReportUsageAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Report Usage Analytics API Controller
 *
 * REST API endpoints for report usage analytics and insights.
 *
 * Provides Endpoints For:
 * - Usage analytics
 * - Performance metrics
 * - Trend analysis
 * - User engagement
 * - Event tracking
 *
 * Endpoints:
 * - GET /api/usage-analytics/report/{reportId} - Get report analytics
 * - GET /api/usage-analytics/global - Get global analytics
 * - POST /api/usage-analytics/track/view - Track view event
 * - POST /api/usage-analytics/track/download - Track download event
 * - POST /api/usage-analytics/track/export - Track export event
 * - POST /api/usage-analytics/track/share - Track share event
 * - POST /api/usage-analytics/track/generation - Track generation event
 * - GET /api/usage-analytics/stats - Get analytics statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 73 - Report Analytics & Insights
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/usage-analytics")
@RequiredArgsConstructor
@Slf4j
public class ReportUsageAnalyticsApiController {

    private final ReportUsageAnalyticsService analyticsService;

    // ============================================================
    // Analytics Endpoints
    // ============================================================

    /**
     * Get analytics for specific report
     *
     * @param reportId Report ID
     * @param period Time period (optional, defaults to MONTHLY)
     * @return Report analytics
     */
    @GetMapping("/report/{reportId}")
    public ResponseEntity<ReportAnalytics> getReportAnalytics(
            @PathVariable Long reportId,
            @RequestParam(defaultValue = "MONTHLY") String period) {
        log.info("GET /api/usage-analytics/report/{}?period={}", reportId, period);

        try {
            ReportAnalytics.TimePeriod timePeriod = ReportAnalytics.TimePeriod.valueOf(period.toUpperCase());
            ReportAnalytics analytics = analyticsService.generateReportAnalytics(reportId, timePeriod);
            return ResponseEntity.ok(analytics);

        } catch (IllegalArgumentException e) {
            log.error("Invalid time period: {}", period);
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error generating report analytics: {}", reportId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get global analytics across all reports
     *
     * @param period Time period (optional, defaults to MONTHLY)
     * @return Global analytics
     */
    @GetMapping("/global")
    public ResponseEntity<ReportAnalytics> getGlobalAnalytics(
            @RequestParam(defaultValue = "MONTHLY") String period) {
        log.info("GET /api/usage-analytics/global?period={}", period);

        try {
            ReportAnalytics.TimePeriod timePeriod = ReportAnalytics.TimePeriod.valueOf(period.toUpperCase());
            ReportAnalytics analytics = analyticsService.generateGlobalAnalytics(timePeriod);
            return ResponseEntity.ok(analytics);

        } catch (IllegalArgumentException e) {
            log.error("Invalid time period: {}", period);
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error generating global analytics", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ============================================================
    // Event Tracking Endpoints
    // ============================================================

    /**
     * Track report view event
     *
     * @param request View event details
     * @return Success response
     */
    @PostMapping("/track/view")
    public ResponseEntity<Map<String, Object>> trackView(@RequestBody Map<String, Object> request) {
        log.info("POST /api/usage-analytics/track/view");

        try {
            Long reportId = Long.valueOf(request.get("reportId").toString());
            String username = (String) request.get("username");
            String reportType = (String) request.get("reportType");

            analyticsService.trackView(reportId, username, reportType);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "View event tracked");
            response.put("reportId", reportId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error tracking view event", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Track report download event
     *
     * @param request Download event details
     * @return Success response
     */
    @PostMapping("/track/download")
    public ResponseEntity<Map<String, Object>> trackDownload(@RequestBody Map<String, Object> request) {
        log.info("POST /api/usage-analytics/track/download");

        try {
            Long reportId = Long.valueOf(request.get("reportId").toString());
            String username = (String) request.get("username");
            String reportType = (String) request.get("reportType");
            String format = (String) request.get("format");
            Long fileSize = request.get("fileSize") != null ?
                    Long.valueOf(request.get("fileSize").toString()) : null;

            analyticsService.trackDownload(reportId, username, reportType, format, fileSize);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Download event tracked");
            response.put("reportId", reportId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error tracking download event", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Track report export event
     *
     * @param request Export event details
     * @return Success response
     */
    @PostMapping("/track/export")
    public ResponseEntity<Map<String, Object>> trackExport(@RequestBody Map<String, Object> request) {
        log.info("POST /api/usage-analytics/track/export");

        try {
            Long reportId = Long.valueOf(request.get("reportId").toString());
            String username = (String) request.get("username");
            String reportType = (String) request.get("reportType");
            String format = (String) request.get("format");

            analyticsService.trackExport(reportId, username, reportType, format);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Export event tracked");
            response.put("reportId", reportId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error tracking export event", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Track report share event
     *
     * @param request Share event details
     * @return Success response
     */
    @PostMapping("/track/share")
    public ResponseEntity<Map<String, Object>> trackShare(@RequestBody Map<String, Object> request) {
        log.info("POST /api/usage-analytics/track/share");

        try {
            Long reportId = Long.valueOf(request.get("reportId").toString());
            String username = (String) request.get("username");
            String reportType = (String) request.get("reportType");

            analyticsService.trackShare(reportId, username, reportType);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Share event tracked");
            response.put("reportId", reportId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error tracking share event", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Track report generation event
     *
     * @param request Generation event details
     * @return Success response
     */
    @PostMapping("/track/generation")
    public ResponseEntity<Map<String, Object>> trackGeneration(@RequestBody Map<String, Object> request) {
        log.info("POST /api/usage-analytics/track/generation");

        try {
            Long reportId = Long.valueOf(request.get("reportId").toString());
            String username = (String) request.get("username");
            String reportType = (String) request.get("reportType");
            Long generationTime = request.get("generationTime") != null ?
                    Long.valueOf(request.get("generationTime").toString()) : null;
            Boolean success = request.get("success") != null ?
                    Boolean.valueOf(request.get("success").toString()) : true;
            String errorMessage = (String) request.get("errorMessage");

            analyticsService.trackGeneration(reportId, username, reportType, generationTime, success, errorMessage);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Generation event tracked");
            response.put("reportId", reportId);
            response.put("success", success);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error tracking generation event", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // ============================================================
    // Statistics Endpoints
    // ============================================================

    /**
     * Get analytics statistics
     *
     * @return Analytics statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/usage-analytics/stats");

        try {
            Map<String, Object> stats = analyticsService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching analytics statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
