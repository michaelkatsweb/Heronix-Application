package com.heronix.controller.api;

import com.heronix.service.EnrollmentForecastingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * REST API Controller for Enrollment Forecasting
 *
 * Provides endpoints for enrollment predictions and capacity planning based on:
 * - Historical enrollment data
 * - Course request trends
 * - Demographic projections
 * - Graduation/promotion rates
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/enrollment-forecasting")
@RequiredArgsConstructor
public class EnrollmentForecastingApiController {

    private final EnrollmentForecastingService forecastingService;

    // ==================== Total Enrollment Forecasting ====================

    @GetMapping("/total/{targetYear}")
    public ResponseEntity<Map<String, Object>> forecastTotalEnrollment(@PathVariable Integer targetYear) {
        int forecastedEnrollment = forecastingService.forecastTotalEnrollment(targetYear);

        Map<String, Object> response = new HashMap<>();
        response.put("targetYear", targetYear);
        response.put("forecastedEnrollment", forecastedEnrollment);
        response.put("forecastDate", LocalDate.now());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-grade/{targetYear}")
    public ResponseEntity<Map<String, Object>> forecastByGrade(@PathVariable Integer targetYear) {
        Map<String, Integer> forecastByGrade = forecastingService.forecastEnrollmentByGrade(targetYear);

        int totalForecast = forecastByGrade.values().stream()
            .mapToInt(Integer::intValue)
            .sum();

        Map<String, Object> response = new HashMap<>();
        response.put("targetYear", targetYear);
        response.put("enrollmentByGrade", forecastByGrade);
        response.put("totalForecast", totalForecast);
        response.put("forecastDate", LocalDate.now());

        return ResponseEntity.ok(response);
    }

    // ==================== Course Demand Forecasting ====================

    @GetMapping("/course-demand/{targetYear}")
    public ResponseEntity<Map<String, Object>> forecastCourseDemand(@PathVariable Integer targetYear) {
        Map<Long, Integer> courseDemand = forecastingService.forecastCourseDemand(targetYear);

        Map<String, Object> response = new HashMap<>();
        response.put("targetYear", targetYear);
        response.put("courseDemand", courseDemand);
        response.put("totalCourses", courseDemand.size());
        response.put("forecastDate", LocalDate.now());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/course/{courseId}/demand/{targetYear}")
    public ResponseEntity<Map<String, Object>> getCourseSpecificDemand(
            @PathVariable Long courseId,
            @PathVariable Integer targetYear) {

        Map<Long, Integer> allDemand = forecastingService.forecastCourseDemand(targetYear);
        Integer demand = allDemand.getOrDefault(courseId, 0);
        int recommendedSections = forecastingService.recommendSectionCount(courseId, targetYear);
        String trend = forecastingService.getCourseTrend(courseId);

        Map<String, Object> response = new HashMap<>();
        response.put("courseId", courseId);
        response.put("targetYear", targetYear);
        response.put("forecastedDemand", demand);
        response.put("recommendedSections", recommendedSections);
        response.put("trend", trend);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/course/{courseId}/sections/{targetYear}")
    public ResponseEntity<Map<String, Object>> recommendSections(
            @PathVariable Long courseId,
            @PathVariable Integer targetYear) {

        int recommendedSections = forecastingService.recommendSectionCount(courseId, targetYear);
        Map<Long, Integer> demand = forecastingService.forecastCourseDemand(targetYear);
        Integer forecastedDemand = demand.getOrDefault(courseId, 0);

        Map<String, Object> response = new HashMap<>();
        response.put("courseId", courseId);
        response.put("targetYear", targetYear);
        response.put("recommendedSections", recommendedSections);
        response.put("forecastedDemand", forecastedDemand);
        response.put("averageStudentsPerSection", recommendedSections > 0 ?
            forecastedDemand / recommendedSections : 0);

        return ResponseEntity.ok(response);
    }

    // ==================== Trend Analysis ====================

    @GetMapping("/course/{courseId}/trend")
    public ResponseEntity<Map<String, Object>> getCourseTrend(@PathVariable Long courseId) {
        String trend = forecastingService.getCourseTrend(courseId);

        Map<String, Object> response = new HashMap<>();
        response.put("courseId", courseId);
        response.put("trend", trend);
        response.put("description", getTrendDescription(trend));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/growth-rate")
    public ResponseEntity<Map<String, Object>> calculateGrowthRate(
            @RequestParam Integer year1,
            @RequestParam Integer year2) {

        double growthRate = forecastingService.calculateGrowthRate(year1, year2);

        Map<String, Object> response = new HashMap<>();
        response.put("year1", year1);
        response.put("year2", year2);
        response.put("growthRate", growthRate);
        response.put("growthRatePercent", String.format("%.2f%%", growthRate));
        response.put("direction", growthRate > 0 ? "GROWING" : growthRate < 0 ? "DECLINING" : "STABLE");

        return ResponseEntity.ok(response);
    }

    // ==================== Comprehensive Reports ====================

    @GetMapping("/report/{targetYear}")
    public ResponseEntity<Map<String, Object>> getForecastingReport(@PathVariable Integer targetYear) {
        Map<String, Object> report = forecastingService.getForecastingReport(targetYear);

        report.put("targetYear", targetYear);
        report.put("generatedDate", LocalDate.now());

        return ResponseEntity.ok(report);
    }

    // ==================== Capacity Analysis ====================

    @GetMapping("/capacity/{targetYear}")
    public ResponseEntity<Map<String, Object>> checkCapacity(@PathVariable Integer targetYear) {
        boolean adequateCapacity = forecastingService.hasAdequateCapacity(targetYear);
        Map<String, String> warnings = forecastingService.getCapacityWarnings(targetYear);

        Map<String, Object> response = new HashMap<>();
        response.put("targetYear", targetYear);
        response.put("hasAdequateCapacity", adequateCapacity);
        response.put("warningCount", warnings.size());
        response.put("warnings", warnings);
        response.put("status", adequateCapacity ? "ADEQUATE" : "INSUFFICIENT");
        response.put("recommendation", adequateCapacity ?
            "Current capacity is sufficient for forecasted enrollment" :
            "Additional sections or rooms may be needed");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/capacity/{targetYear}/warnings")
    public ResponseEntity<Map<String, Object>> getCapacityWarnings(@PathVariable Integer targetYear) {
        Map<String, String> warnings = forecastingService.getCapacityWarnings(targetYear);

        Map<String, Object> response = new HashMap<>();
        response.put("targetYear", targetYear);
        response.put("warningCount", warnings.size());
        response.put("warnings", warnings);
        response.put("hasWarnings", !warnings.isEmpty());

        return ResponseEntity.ok(response);
    }

    // ==================== Forecasting Methods ====================

    @GetMapping("/course/{courseId}/linear-regression/{targetYear}")
    public ResponseEntity<Map<String, Object>> forecastWithLinearRegression(
            @PathVariable Long courseId,
            @PathVariable Integer targetYear) {

        int forecast = forecastingService.forecastWithLinearRegression(courseId, targetYear);

        Map<String, Object> response = new HashMap<>();
        response.put("courseId", courseId);
        response.put("targetYear", targetYear);
        response.put("forecast", forecast);
        response.put("method", "LINEAR_REGRESSION");
        response.put("description", "Forecast using linear regression on historical data");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/course/{courseId}/moving-average/{targetYear}")
    public ResponseEntity<Map<String, Object>> forecastWithMovingAverage(
            @PathVariable Long courseId,
            @PathVariable Integer targetYear,
            @RequestParam(defaultValue = "3") int windowSize) {

        int forecast = forecastingService.forecastWithMovingAverage(courseId, targetYear, windowSize);

        Map<String, Object> response = new HashMap<>();
        response.put("courseId", courseId);
        response.put("targetYear", targetYear);
        response.put("forecast", forecast);
        response.put("windowSize", windowSize);
        response.put("method", "MOVING_AVERAGE");
        response.put("description", String.format("%d-year moving average forecast", windowSize));

        return ResponseEntity.ok(response);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/overview/{targetYear}")
    public ResponseEntity<Map<String, Object>> getDashboard(@PathVariable Integer targetYear) {
        int totalEnrollment = forecastingService.forecastTotalEnrollment(targetYear);
        Map<String, Integer> byGrade = forecastingService.forecastEnrollmentByGrade(targetYear);
        boolean adequateCapacity = forecastingService.hasAdequateCapacity(targetYear);
        Map<String, String> warnings = forecastingService.getCapacityWarnings(targetYear);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("targetYear", targetYear);
        dashboard.put("totalForecast", totalEnrollment);
        dashboard.put("enrollmentByGrade", byGrade);
        dashboard.put("gradeCount", byGrade.size());
        dashboard.put("hasAdequateCapacity", adequateCapacity);
        dashboard.put("warningCount", warnings.size());
        dashboard.put("status", adequateCapacity ? "ON_TRACK" : "CAPACITY_CONCERNS");

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/summary/{targetYear}")
    public ResponseEntity<Map<String, Object>> getSummary(@PathVariable Integer targetYear) {
        int totalEnrollment = forecastingService.forecastTotalEnrollment(targetYear);
        Map<Long, Integer> courseDemand = forecastingService.forecastCourseDemand(targetYear);
        boolean adequateCapacity = forecastingService.hasAdequateCapacity(targetYear);

        int previousYear = targetYear - 1;
        int previousEnrollment = forecastingService.forecastTotalEnrollment(previousYear);
        double growthRate = previousEnrollment > 0 ?
            ((double) (totalEnrollment - previousEnrollment) / previousEnrollment * 100) : 0;

        Map<String, Object> summary = new HashMap<>();
        summary.put("targetYear", targetYear);
        summary.put("totalForecast", totalEnrollment);
        summary.put("previousYearForecast", previousEnrollment);
        summary.put("growthRate", growthRate);
        summary.put("totalCourses", courseDemand.size());
        summary.put("adequateCapacity", adequateCapacity);

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/dashboard/trends")
    public ResponseEntity<Map<String, Object>> getTrendsDashboard() {
        int currentYear = LocalDate.now().getYear();
        int nextYear = currentYear + 1;

        int currentForecast = forecastingService.forecastTotalEnrollment(currentYear);
        int nextYearForecast = forecastingService.forecastTotalEnrollment(nextYear);
        double growthRate = forecastingService.calculateGrowthRate(currentYear, nextYear);

        Map<String, Object> trends = new HashMap<>();
        trends.put("currentYear", currentYear);
        trends.put("currentYearForecast", currentForecast);
        trends.put("nextYear", nextYear);
        trends.put("nextYearForecast", nextYearForecast);
        trends.put("growthRate", growthRate);
        trends.put("trend", growthRate > 0 ? "GROWING" : growthRate < 0 ? "DECLINING" : "STABLE");

        return ResponseEntity.ok(trends);
    }

    // ==================== Utility Endpoints ====================

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("version", "1.0.0");
        metadata.put("features", Map.of(
            "totalEnrollmentForecasting", "Predict total school enrollment",
            "gradeLevel Forecasting", "Enrollment by grade level",
            "courseDemandForecasting", "Predict course demand and sections needed",
            "trendAnalysis", "Identify growing/declining courses",
            "capacityPlanning", "Check if capacity is adequate"
        ));

        metadata.put("methods", Map.of(
            "linearRegression", "Forecast using linear regression on historical data",
            "movingAverage", "Forecast using N-year moving average",
            "trendAnalysis", "Analyze historical enrollment trends"
        ));

        metadata.put("useCases", Map.of(
            "sectionPlanning", "How many sections do we need next year?",
            "courseTrends", "Which courses are growing/declining?",
            "enrollmentTrajectory", "What's our enrollment trajectory?",
            "capacityValidation", "Do we have enough capacity?"
        ));

        return ResponseEntity.ok(metadata);
    }

    // ==================== Helper Methods ====================

    private String getTrendDescription(String trend) {
        switch (trend.toUpperCase()) {
            case "GROWING":
                return "Enrollment is increasing - consider adding sections";
            case "STABLE":
                return "Enrollment is stable - maintain current sections";
            case "DECLINING":
                return "Enrollment is decreasing - consider reducing sections";
            default:
                return "Insufficient data for trend analysis";
        }
    }
}
