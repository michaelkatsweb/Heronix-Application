package com.heronix.controller;

import com.heronix.dto.AttendanceStatistics;
import com.heronix.service.ReportAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Report Analytics API Controller
 *
 * REST API endpoints for attendance analytics and statistical reporting.
 *
 * Endpoints:
 * - GET /api/reports/analytics/statistics - Comprehensive attendance statistics
 * - GET /api/reports/analytics/trends - Trend analysis
 * - GET /api/reports/analytics/grade-breakdown - Grade-level statistics
 * - GET /api/reports/analytics/top-absentees - Students with highest absence rates
 *
 * Query Parameters:
 * - startDate: Period start date (default: 30 days ago)
 * - endDate: Period end date (default: today)
 * - limit: Maximum number of results for top absentees (default: 10)
 *
 * Response Format:
 * - JSON with nested statistics objects
 * - Includes rates as percentages (0-100)
 * - Dates in ISO 8601 format (yyyy-MM-dd)
 *
 * Example Usage:
 * GET /api/reports/analytics/statistics?startDate=2024-01-01&endDate=2024-01-31
 * GET /api/reports/analytics/trends?startDate=2024-01-01
 * GET /api/reports/analytics/top-absentees?limit=20
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 59 - Report Analytics Dashboard
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/reports/analytics")
@RequiredArgsConstructor
@Slf4j
public class ReportAnalyticsApiController {

    private final ReportAnalyticsService analyticsService;

    /**
     * Get comprehensive attendance statistics for a date range
     *
     * Returns overall statistics, daily breakdown, trends, grade-level analysis,
     * and top absentees in a single comprehensive response.
     *
     * @param startDate Period start date (optional, default: 30 days ago)
     * @param endDate Period end date (optional, default: today)
     * @return Comprehensive attendance statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<AttendanceStatistics> getAttendanceStatistics(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        log.info("GET /api/reports/analytics/statistics - startDate: {}, endDate: {}", start, end);

        try {
            AttendanceStatistics statistics = analyticsService.getAttendanceStatistics(start, end);
            return ResponseEntity.ok(statistics);

        } catch (Exception e) {
            log.error("Error generating attendance statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get trend analysis for attendance data
     *
     * Returns trend direction (IMPROVING, DECLINING, STABLE), change percentage,
     * best/worst days, and actionable insights.
     *
     * @param startDate Period start date (optional, default: 30 days ago)
     * @param endDate Period end date (optional, default: today)
     * @return Trend analysis
     */
    @GetMapping("/trends")
    public ResponseEntity<AttendanceStatistics.TrendAnalysis> getTrendAnalysis(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        log.info("GET /api/reports/analytics/trends - startDate: {}, endDate: {}", start, end);

        try {
            AttendanceStatistics statistics = analyticsService.getAttendanceStatistics(start, end);
            return ResponseEntity.ok(statistics.getTrends());

        } catch (Exception e) {
            log.error("Error generating trend analysis", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get grade-level attendance breakdown
     *
     * Returns attendance statistics grouped by grade level, including
     * student counts and attendance/absenteeism rates per grade.
     *
     * @param startDate Period start date (optional, default: 30 days ago)
     * @param endDate Period end date (optional, default: today)
     * @return Grade-level statistics map
     */
    @GetMapping("/grade-breakdown")
    public ResponseEntity<java.util.Map<String, AttendanceStatistics.GradeStats>> getGradeBreakdown(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        log.info("GET /api/reports/analytics/grade-breakdown - startDate: {}, endDate: {}", start, end);

        try {
            AttendanceStatistics statistics = analyticsService.getAttendanceStatistics(start, end);
            return ResponseEntity.ok(statistics.getGradeBreakdown());

        } catch (Exception e) {
            log.error("Error generating grade breakdown", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get top absentees list
     *
     * Returns students with the highest absenteeism rates, sorted by absence rate.
     * Includes risk level classification (CRITICAL, HIGH, MODERATE, LOW).
     *
     * @param startDate Period start date (optional, default: 30 days ago)
     * @param endDate Period end date (optional, default: today)
     * @return List of students with highest absence rates
     */
    @GetMapping("/top-absentees")
    public ResponseEntity<java.util.List<AttendanceStatistics.StudentAbsenceRecord>> getTopAbsentees(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        log.info("GET /api/reports/analytics/top-absentees - startDate: {}, endDate: {}", start, end);

        try {
            AttendanceStatistics statistics = analyticsService.getAttendanceStatistics(start, end);
            return ResponseEntity.ok(statistics.getTopAbsentees());

        } catch (Exception e) {
            log.error("Error generating top absentees list", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get overall statistics summary
     *
     * Returns aggregate statistics including total students, total days,
     * attendance/absenteeism/tardy rates, and record counts.
     *
     * @param startDate Period start date (optional, default: 30 days ago)
     * @param endDate Period end date (optional, default: today)
     * @return Overall statistics summary
     */
    @GetMapping("/overall")
    public ResponseEntity<AttendanceStatistics.OverallStats> getOverallStatistics(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        log.info("GET /api/reports/analytics/overall - startDate: {}, endDate: {}", start, end);

        try {
            AttendanceStatistics statistics = analyticsService.getAttendanceStatistics(start, end);
            return ResponseEntity.ok(statistics.getOverall());

        } catch (Exception e) {
            log.error("Error generating overall statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get daily statistics breakdown
     *
     * Returns day-by-day attendance statistics including date, day of week,
     * attendance counts, and daily attendance rate.
     *
     * @param startDate Period start date (optional, default: 30 days ago)
     * @param endDate Period end date (optional, default: today)
     * @return List of daily statistics
     */
    @GetMapping("/daily")
    public ResponseEntity<java.util.List<AttendanceStatistics.DailyStats>> getDailyStatistics(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        log.info("GET /api/reports/analytics/daily - startDate: {}, endDate: {}", start, end);

        try {
            AttendanceStatistics statistics = analyticsService.getAttendanceStatistics(start, end);
            return ResponseEntity.ok(statistics.getDailyStats());

        } catch (Exception e) {
            log.error("Error generating daily statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
