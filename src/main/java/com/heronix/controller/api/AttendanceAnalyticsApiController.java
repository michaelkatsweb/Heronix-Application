package com.heronix.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

/**
 * REST API Controller for Attendance Analytics
 *
 * Provides comprehensive analytics and insights on attendance patterns:
 * - Attendance trends and forecasting
 * - Chronic absenteeism identification
 * - Attendance rate calculations by various dimensions
 * - Comparative analytics (grade, teacher, course)
 * - Early warning system integration
 * - Intervention effectiveness tracking
 * - ADA/ADM calculations for funding
 *
 * Analytics Dimensions:
 * - By student, class, grade level, school
 * - By day of week, time period, season
 * - By demographic groups
 * - By special populations (ELL, SPED, 504)
 *
 * Key Performance Indicators:
 * - Daily attendance rate
 * - Chronic absenteeism rate (missing 10%+ of school days)
 * - Average daily attendance (ADA)
 * - Average daily membership (ADM)
 * - Truancy rates
 * - Perfect attendance tracking
 *
 * Predictive Analytics:
 * - At-risk student identification
 * - Attendance trend forecasting
 * - Intervention impact prediction
 * - Early warning indicators
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since December 30, 2025 - Phase 38
 */
@RestController
@RequestMapping("/api/attendance-analytics")
@RequiredArgsConstructor
@Tag(name = "Attendance Analytics", description = "Attendance analytics, ADA/ADM, and chronic absenteeism tracking")
public class AttendanceAnalyticsApiController {

    private final com.heronix.service.AttendanceAnalyticsService analyticsService;

    // ========================================================================
    // SCHOOL-WIDE ANALYTICS
    // ========================================================================

    /**
     * Get school-wide attendance dashboard
     *
     * GET /api/attendance-analytics/dashboard?startDate=2025-01-01&endDate=2025-12-31
     *
     * Returns comprehensive attendance metrics for the entire school
     */
    @Operation(
        summary = "Get school-wide attendance dashboard",
        description = "Returns comprehensive attendance metrics including total students, attendance rates, chronic absenteeism, and daily trends for the specified period."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getAttendanceDashboard(
            @Parameter(description = "Start date of the reporting period (ISO format: YYYY-MM-DD)", required = true, example = "2025-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date of the reporting period (ISO format: YYYY-MM-DD)", required = true, example = "2025-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            Map<String, Object> dashboard = analyticsService.getSchoolDashboard(startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("dashboard", dashboard);
            response.put("period", Map.of("startDate", startDate, "endDate", endDate));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to generate dashboard: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Calculate Average Daily Attendance (ADA) for funding
     *
     * GET /api/attendance-analytics/ada?startDate=2025-01-01&endDate=2025-12-31
     */
    @Operation(
        summary = "Calculate Average Daily Attendance (ADA)",
        description = "Calculates ADA for state funding purposes. ADA represents the average number of students present per day over the specified period."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "ADA calculated successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/ada")
    public ResponseEntity<Map<String, Object>> calculateADA(
            @Parameter(description = "Start date of calculation period", required = true, example = "2025-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date of calculation period", required = true, example = "2025-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            Map<String, Object> adaData = analyticsService.calculateADA(startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("ada", adaData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to calculate ADA: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Calculate Average Daily Membership (ADM)
     *
     * GET /api/attendance-analytics/adm?startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/adm")
    public ResponseEntity<Map<String, Object>> calculateADM(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            Map<String, Object> admData = analyticsService.calculateADM(startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("adm", admData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to calculate ADM: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // TREND ANALYSIS
    // ========================================================================

    /**
     * Get attendance trends over time
     *
     * GET /api/attendance-analytics/trends?startDate=2025-01-01&endDate=2025-12-31&groupBy=week
     *
     * groupBy options: day, week, month
     */
    @Operation(
        summary = "Get attendance trends over time",
        description = "Returns time-series attendance data grouped by day, week, or month to identify patterns and trends."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trends retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/trends")
    public ResponseEntity<Map<String, Object>> getAttendanceTrends(
            @Parameter(description = "Start date of trend analysis", required = true, example = "2025-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date of trend analysis", required = true, example = "2025-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Grouping interval: day, week, or month", example = "week")
            @RequestParam(defaultValue = "week") String groupBy) {

        try {
            List<Map<String, Object>> trends = analyticsService.getAttendanceTrends(
                startDate, endDate, groupBy);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("trends", trends);
            response.put("groupBy", groupBy);
            response.put("period", Map.of("startDate", startDate, "endDate", endDate));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to generate trends: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Analyze attendance by day of week
     *
     * GET /api/attendance-analytics/by-day-of-week?startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/by-day-of-week")
    public ResponseEntity<Map<String, Object>> analyzeByDayOfWeek(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            Map<String, Object> analysis = analyticsService.analyzeByDayOfWeek(startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("analysis", analysis);
            response.put("period", Map.of("startDate", startDate, "endDate", endDate));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to analyze by day of week: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // CHRONIC ABSENTEEISM
    // ========================================================================

    /**
     * Identify chronically absent students (missing 10%+ of school days)
     *
     * GET /api/attendance-analytics/chronic-absenteeism?startDate=2025-01-01&endDate=2025-12-31&threshold=10
     */
    @GetMapping("/chronic-absenteeism")
    public ResponseEntity<Map<String, Object>> getChronicAbsenteeism(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "10") double threshold) {

        try {
            Map<String, Object> data = analyticsService.analyzeChronicAbsenteeism(
                startDate, endDate, threshold);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("chronicAbsenteeism", data);
            response.put("period", Map.of("startDate", startDate, "endDate", endDate));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to analyze chronic absenteeism: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Track chronic absenteeism rate over time
     *
     * GET /api/attendance-analytics/chronic-absenteeism/trend?startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/chronic-absenteeism/trend")
    public ResponseEntity<Map<String, Object>> getChronicAbsenteeismTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            List<Map<String, Object>> trend = analyticsService.getChronicAbsenteeismTrend(
                startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("trend", trend);
            response.put("period", Map.of("startDate", startDate, "endDate", endDate));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to generate trend: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // COMPARATIVE ANALYTICS
    // ========================================================================

    /**
     * Compare attendance rates by grade level
     *
     * GET /api/attendance-analytics/by-grade?startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/by-grade")
    public ResponseEntity<Map<String, Object>> compareByGrade(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            List<Map<String, Object>> comparison = analyticsService.compareByGradeLevel(
                startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("gradeComparison", comparison);
            response.put("period", Map.of("startDate", startDate, "endDate", endDate));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to compare by grade: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Compare attendance rates by teacher/course
     *
     * GET /api/attendance-analytics/by-teacher?startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/by-teacher")
    public ResponseEntity<Map<String, Object>> compareByTeacher(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            List<Map<String, Object>> comparison = analyticsService.compareByTeacher(
                startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("teacherComparison", comparison);
            response.put("period", Map.of("startDate", startDate, "endDate", endDate));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to compare by teacher: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // EARLY WARNING SYSTEM
    // ========================================================================

    /**
     * Get at-risk students based on attendance patterns
     *
     * GET /api/attendance-analytics/at-risk?days=30&absenceThreshold=5
     */
    @GetMapping("/at-risk")
    public ResponseEntity<Map<String, Object>> getAtRiskStudents(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "5") int absenceThreshold) {

        try {
            List<Map<String, Object>> atRiskStudents = analyticsService.identifyAtRiskStudents(
                days, absenceThreshold);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("atRiskStudents", atRiskStudents);
            response.put("count", atRiskStudents.size());
            response.put("criteria", Map.of(
                "days", days,
                "absenceThreshold", absenceThreshold
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to identify at-risk students: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Forecast attendance for upcoming period
     *
     * POST /api/attendance-analytics/forecast
     *
     * Request Body:
     * {
     *   "startDate": "2025-01-01",
     *   "endDate": "2025-06-30",
     *   "forecastDays": 30
     * }
     */
    @PostMapping("/forecast")
    public ResponseEntity<Map<String, Object>> forecastAttendance(
            @RequestBody Map<String, Object> requestBody) {

        try {
            LocalDate startDate = LocalDate.parse((String) requestBody.get("startDate"));
            LocalDate endDate = LocalDate.parse((String) requestBody.get("endDate"));
            Integer forecastDays = Integer.valueOf(requestBody.get("forecastDays").toString());

            Map<String, Object> forecast = analyticsService.forecastAttendance(
                startDate, endDate, forecastDays);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("forecast", forecast);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to forecast attendance: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // INTERVENTION TRACKING
    // ========================================================================

    /**
     * Measure intervention effectiveness
     *
     * GET /api/attendance-analytics/intervention-impact?studentId=123&interventionDate=2025-03-01&windowDays=30
     */
    @GetMapping("/intervention-impact")
    public ResponseEntity<Map<String, Object>> measureInterventionImpact(
            @RequestParam Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate interventionDate,
            @RequestParam(defaultValue = "30") int windowDays) {

        try {
            Map<String, Object> impact = analyticsService.measureInterventionImpact(
                studentId, interventionDate, windowDays);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("interventionDate", interventionDate);
            response.put("impact", impact);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to measure intervention impact: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // PERFECT ATTENDANCE
    // ========================================================================

    /**
     * Get students with perfect attendance
     *
     * GET /api/attendance-analytics/perfect-attendance?startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/perfect-attendance")
    public ResponseEntity<Map<String, Object>> getPerfectAttendance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            List<Map<String, Object>> students = analyticsService.getPerfectAttendance(
                startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("students", students);
            response.put("count", students.size());
            response.put("period", Map.of("startDate", startDate, "endDate", endDate));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get perfect attendance: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // EXPORT & REPORTING
    // ========================================================================

    /**
     * Generate attendance analytics report
     *
     * POST /api/attendance-analytics/reports/generate
     *
     * Request Body:
     * {
     *   "reportType": "comprehensive",
     *   "startDate": "2025-01-01",
     *   "endDate": "2025-12-31",
     *   "format": "PDF",
     *   "includeCharts": true
     * }
     */
    @PostMapping("/reports/generate")
    public ResponseEntity<Map<String, Object>> generateReport(
            @RequestBody Map<String, Object> requestBody) {

        try {
            String reportType = (String) requestBody.get("reportType");
            LocalDate startDate = LocalDate.parse((String) requestBody.get("startDate"));
            LocalDate endDate = LocalDate.parse((String) requestBody.get("endDate"));
            String format = (String) requestBody.getOrDefault("format", "PDF");

            Map<String, Object> report = analyticsService.generateReport(
                reportType, startDate, endDate, format);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("report", report);
            response.put("message", "Report generated successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to generate report: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
