package com.heronix.controller.api;

import com.heronix.model.domain.Student;
import com.heronix.repository.StudentRepository;
import com.heronix.service.BehaviorReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

/**
 * REST API Controller for Behavior Reporting and Analytics
 *
 * Provides comprehensive behavior incident reporting and analysis:
 * - Incident reporting and tracking
 * - Behavior trend analysis
 * - Discipline action tracking
 * - Student behavior profiles
 * - Behavior intervention monitoring
 * - Comparative behavior analytics
 * - Early warning indicators
 *
 * Report Types:
 * - Individual student behavior reports
 * - Classroom/teacher behavior reports
 * - School-wide behavior trends
 * - Discipline action summaries
 * - Intervention effectiveness reports
 * - Behavior referral patterns
 *
 * Analytics Features:
 * - Behavior incident frequency analysis
 * - Pattern recognition (time, location, trigger)
 * - Recidivism tracking
 * - Intervention success rates
 * - Comparative analysis by demographics
 * - Predictive risk assessment
 *
 * Integration Points:
 * - Student Management System
 * - Attendance System
 * - Discipline Management
 * - Parent Communication
 * - Early Warning System
 *
 * @author Heronix Development Team
 * @version 2.0
 * @since December 30, 2025 - Phase 38
 * @updated January 19, 2026 - Full implementation
 */
@RestController
@RequestMapping("/api/behavior-reporting")
@RequiredArgsConstructor
public class BehaviorReportingApiController {

    private final BehaviorReportingService reportingService;
    private final StudentRepository studentRepository;

    // ========================================================================
    // INCIDENT REPORTING
    // ========================================================================

    /**
     * Get behavior incidents for a student
     *
     * GET /api/behavior-reporting/students/{studentId}/incidents?startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/students/{studentId}/incidents")
    public ResponseEntity<Map<String, Object>> getStudentIncidents(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            var incidents = reportingService.generateStudentIncidentList(student, startDate, endDate, true, true);

            List<Map<String, Object>> incidentData = incidents.stream()
                .map(incident -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", incident.getId());
                    data.put("date", incident.getIncidentDate());
                    data.put("type", incident.getBehaviorType());
                    data.put("category", incident.getBehaviorCategory());
                    data.put("severity", incident.getSeverityLevel());
                    data.put("description", incident.getDescription());
                    data.put("location", incident.getLocation());
                    data.put("isPositive", incident.isPositive());
                    return data;
                })
                .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("incidents", incidentData);
            response.put("count", incidentData.size());
            response.put("period", Map.of("startDate", startDate, "endDate", endDate));

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve incidents: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get behavior incident summary for a student
     *
     * GET /api/behavior-reporting/students/{studentId}/summary?startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/students/{studentId}/summary")
    public ResponseEntity<Map<String, Object>> getStudentBehaviorSummary(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            var summaryData = reportingService.generateStudentBehaviorSummary(student, startDate, endDate);

            Map<String, Object> summary = new HashMap<>();
            summary.put("totalIncidents", summaryData.getTotalIncidents());
            summary.put("positiveIncidents", summaryData.getPositiveIncidents());
            summary.put("negativeIncidents", summaryData.getNegativeIncidents());
            summary.put("categoryBreakdown", summaryData.getCategoryBreakdown());
            summary.put("severityBreakdown", summaryData.getSeverityBreakdown());
            summary.put("uncontactedParentIncidents", summaryData.getUncontactedParentIncidents());
            summary.put("behaviorRatio", summaryData.getBehaviorRatio());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("summary", summary);
            response.put("period", Map.of("startDate", startDate, "endDate", endDate));

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to generate summary: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // SCHOOL-WIDE REPORTING
    // ========================================================================

    /**
     * Get school-wide behavior dashboard
     *
     * GET /api/behavior-reporting/dashboard?startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getBehaviorDashboard(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            var schoolReport = reportingService.generateSchoolBehaviorReport(startDate, endDate);

            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("totalIncidents", schoolReport.getTotalIncidents());
            dashboard.put("positiveIncidents", schoolReport.getPositiveIncidents());
            dashboard.put("negativeIncidents", schoolReport.getNegativeIncidents());
            dashboard.put("categoryBreakdown", schoolReport.getCategoryBreakdown());
            dashboard.put("severityBreakdown", schoolReport.getSeverityBreakdown());
            dashboard.put("adminReferrals", schoolReport.getAdminReferralsCount());
            dashboard.put("uncontactedParents", schoolReport.getUncontactedParentsCount());
            dashboard.put("studentsWithIncidents", schoolReport.getStudentsWithIncidents());

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
     * Get behavior trends over time
     *
     * GET /api/behavior-reporting/trends?startDate=2025-01-01&endDate=2025-12-31&groupBy=week
     */
    @GetMapping("/trends")
    public ResponseEntity<Map<String, Object>> getBehaviorTrends(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "week") String groupBy) {

        try {
            var trendPoints = reportingService.getSchoolWideTrends(startDate, endDate, groupBy);

            List<Map<String, Object>> trends = trendPoints.stream()
                    .map(point -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("periodKey", point.getPeriodKey());
                        data.put("totalIncidents", point.getTotalIncidents());
                        data.put("positiveIncidents", point.getPositiveIncidents());
                        data.put("negativeIncidents", point.getNegativeIncidents());
                        data.put("uniqueStudents", point.getUniqueStudents());
                        return data;
                    })
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("trends", trends);
            response.put("count", trends.size());
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

    // ========================================================================
    // COMPARATIVE ANALYTICS
    // ========================================================================

    /**
     * Compare behavior incidents by grade level
     *
     * GET /api/behavior-reporting/by-grade?startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/by-grade")
    public ResponseEntity<Map<String, Object>> compareByGrade(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            var comparisons = reportingService.compareByGrade(startDate, endDate);

            List<Map<String, Object>> comparisonData = comparisons.stream()
                    .map(comp -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("gradeLevel", comp.getGradeLevel());
                        data.put("totalIncidents", comp.getTotalIncidents());
                        data.put("positiveIncidents", comp.getPositiveIncidents());
                        data.put("negativeIncidents", comp.getNegativeIncidents());
                        data.put("totalStudents", comp.getTotalStudents());
                        data.put("incidentsPerStudent", String.format("%.2f", comp.getIncidentsPerStudent()));
                        data.put("uniqueStudentsWithIncidents", comp.getUniqueStudentsWithIncidents());
                        return data;
                    })
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("gradeComparison", comparisonData);
            response.put("count", comparisonData.size());
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
     * Compare behavior incidents by location
     *
     * GET /api/behavior-reporting/by-location?startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/by-location")
    public ResponseEntity<Map<String, Object>> compareByLocation(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            var comparisons = reportingService.compareByLocation(startDate, endDate);

            List<Map<String, Object>> comparisonData = comparisons.stream()
                    .map(comp -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("location", comp.getLocation());
                        data.put("totalIncidents", comp.getTotalIncidents());
                        data.put("positiveIncidents", comp.getPositiveIncidents());
                        data.put("negativeIncidents", comp.getNegativeIncidents());
                        data.put("uniqueStudents", comp.getUniqueStudents());
                        data.put("mostCommonIncidentType", comp.getMostCommonIncidentType());
                        data.put("percentageOfTotal", String.format("%.2f%%", comp.getPercentageOfTotal()));
                        return data;
                    })
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("locationComparison", comparisonData);
            response.put("count", comparisonData.size());
            response.put("period", Map.of("startDate", startDate, "endDate", endDate));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to compare by location: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Analyze behavior by incident type
     *
     * GET /api/behavior-reporting/by-type?startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/by-type")
    public ResponseEntity<Map<String, Object>> analyzeByType(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            var analyses = reportingService.analyzeByType(startDate, endDate);

            List<Map<String, Object>> analysisData = analyses.stream()
                    .map(analysis -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("category", analysis.getCategory());
                        data.put("categoryDisplayName", analysis.getCategoryDisplayName());
                        data.put("totalIncidents", analysis.getTotalIncidents());
                        data.put("positiveIncidents", analysis.getPositiveIncidents());
                        data.put("negativeIncidents", analysis.getNegativeIncidents());
                        data.put("severityBreakdown", analysis.getSeverityBreakdown());
                        data.put("adminReferralCount", analysis.getAdminReferralCount());
                        data.put("adminReferralRate", String.format("%.2f%%", analysis.getAdminReferralRate()));
                        data.put("percentageOfTotal", String.format("%.2f%%", analysis.getPercentageOfTotal()));
                        data.put("uniqueStudents", analysis.getUniqueStudents());
                        return data;
                    })
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("typeAnalysis", analysisData);
            response.put("count", analysisData.size());
            response.put("period", Map.of("startDate", startDate, "endDate", endDate));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to analyze by type: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // DISCIPLINE ACTIONS
    // ========================================================================

    /**
     * Get discipline action summary
     *
     * GET /api/behavior-reporting/discipline-actions?startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/discipline-actions")
    public ResponseEntity<Map<String, Object>> getDisciplineActions(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            var summary = reportingService.getDisciplineActions(startDate, endDate);

            Map<String, Object> actionData = new HashMap<>();
            actionData.put("totalNegativeIncidents", summary.getTotalNegativeIncidents());
            actionData.put("actionCounts", summary.getActionCounts());
            actionData.put("severityDistribution", summary.getSeverityDistribution());
            actionData.put("adminReferrals", summary.getAdminReferrals());
            actionData.put("parentContactsMade", summary.getParentContactsMade());
            actionData.put("parentContactRate", String.format("%.2f%%", summary.getParentContactRate()));
            actionData.put("uniqueStudents", summary.getUniqueStudents());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("disciplineActions", actionData);
            response.put("period", Map.of("startDate", startDate, "endDate", endDate));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve discipline actions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // INTERVENTION TRACKING
    // ========================================================================

    /**
     * Measure intervention effectiveness
     *
     * GET /api/behavior-reporting/intervention-effectiveness?interventionType=detention&daysBefore=30&daysAfter=30
     */
    @GetMapping("/intervention-effectiveness")
    public ResponseEntity<Map<String, Object>> measureInterventionEffectiveness(
            @RequestParam String interventionType,
            @RequestParam(defaultValue = "30") int daysBefore,
            @RequestParam(defaultValue = "30") int daysAfter) {

        try {
            var effectiveness = reportingService.measureInterventionEffectiveness(
                    interventionType, daysBefore, daysAfter);

            Map<String, Object> effectivenessData = new HashMap<>();
            effectivenessData.put("interventionType", effectiveness.getInterventionType());
            effectivenessData.put("studentsAnalyzed", effectiveness.getStudentsAnalyzed());
            effectivenessData.put("studentsImproved", effectiveness.getStudentsImproved());
            effectivenessData.put("improvementRate", String.format("%.2f%%", effectiveness.getImprovementRate()));
            effectivenessData.put("averageIncidentsBefore", String.format("%.2f", effectiveness.getAverageIncidentsBefore()));
            effectivenessData.put("averageIncidentsAfter", String.format("%.2f", effectiveness.getAverageIncidentsAfter()));
            effectivenessData.put("overallReduction", String.format("%.2f%%", effectiveness.getOverallReduction()));
            effectivenessData.put("daysBefore", effectiveness.getDaysBefore());
            effectivenessData.put("daysAfter", effectiveness.getDaysAfter());

            if (effectiveness.getMessage() != null) {
                effectivenessData.put("message", effectiveness.getMessage());
            }

            // Include student results if available
            if (effectiveness.getStudentResults() != null && !effectiveness.getStudentResults().isEmpty()) {
                List<Map<String, Object>> studentResults = effectiveness.getStudentResults().stream()
                        .map(result -> {
                            Map<String, Object> data = new HashMap<>();
                            data.put("studentId", result.getStudentId());
                            data.put("studentName", result.getStudentName());
                            data.put("incidentsBefore", result.getIncidentsBefore());
                            data.put("incidentsAfter", result.getIncidentsAfter());
                            data.put("improved", result.isImproved());
                            data.put("changePercent", String.format("%.2f%%", result.getChangePercent()));
                            return data;
                        })
                        .toList();
                effectivenessData.put("studentResults", studentResults);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("effectiveness", effectivenessData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to measure effectiveness: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // EARLY WARNING INDICATORS
    // ========================================================================

    /**
     * Identify at-risk students based on behavior patterns
     *
     * GET /api/behavior-reporting/at-risk?days=30&incidentThreshold=3
     */
    @GetMapping("/at-risk")
    public ResponseEntity<Map<String, Object>> getAtRiskStudents(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "3") int incidentThreshold) {

        try {
            var atRiskStudents = reportingService.getAtRiskStudents(days, incidentThreshold);

            List<Map<String, Object>> studentsData = atRiskStudents.stream()
                    .map(student -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("studentId", student.getStudentId());
                        data.put("studentName", student.getStudentName());
                        data.put("studentNumber", student.getStudentNumber());
                        data.put("gradeLevel", student.getGradeLevel());
                        data.put("totalIncidents", student.getTotalIncidents());
                        data.put("majorIncidents", student.getMajorIncidents());
                        data.put("adminReferrals", student.getAdminReferrals());
                        data.put("mostCommonCategory", student.getMostCommonCategory());
                        data.put("riskScore", student.getRiskScore());
                        data.put("riskLevel", student.getRiskLevel());
                        return data;
                    })
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("atRiskStudents", studentsData);
            response.put("count", studentsData.size());
            response.put("criteria", Map.of(
                "days", days,
                "incidentThreshold", incidentThreshold
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
     * Identify repeat offenders
     *
     * GET /api/behavior-reporting/repeat-offenders?startDate=2025-01-01&endDate=2025-12-31&minIncidents=3
     */
    @GetMapping("/repeat-offenders")
    public ResponseEntity<Map<String, Object>> getRepeatOffenders(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "3") int minIncidents) {

        try {
            var repeatOffenders = reportingService.getRepeatOffenders(startDate, endDate, minIncidents);

            List<Map<String, Object>> offendersData = repeatOffenders.stream()
                    .map(offender -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("studentId", offender.getStudentId());
                        data.put("studentName", offender.getStudentName());
                        data.put("studentNumber", offender.getStudentNumber());
                        data.put("gradeLevel", offender.getGradeLevel());
                        data.put("totalIncidents", offender.getTotalIncidents());
                        data.put("categoryBreakdown", offender.getCategoryBreakdown());
                        data.put("mostCommonCategory", offender.getMostCommonCategory());
                        data.put("adminReferrals", offender.getAdminReferrals());
                        data.put("firstIncidentDate", offender.getFirstIncidentDate());
                        data.put("lastIncidentDate", offender.getLastIncidentDate());
                        data.put("averageDaysBetweenIncidents", String.format("%.1f", offender.getAverageDaysBetweenIncidents()));
                        return data;
                    })
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("repeatOffenders", offendersData);
            response.put("count", offendersData.size());
            response.put("minIncidents", minIncidents);
            response.put("period", Map.of("startDate", startDate, "endDate", endDate));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to identify repeat offenders: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // REPORT GENERATION
    // ========================================================================

    /**
     * Generate comprehensive behavior report
     *
     * POST /api/behavior-reporting/reports/generate
     *
     * Request Body:
     * {
     *   "reportType": "student|school|teacher|grade",
     *   "entityId": 123,
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
            Long entityId = requestBody.get("entityId") != null ?
                    ((Number) requestBody.get("entityId")).longValue() : null;
            LocalDate startDate = LocalDate.parse((String) requestBody.get("startDate"));
            LocalDate endDate = LocalDate.parse((String) requestBody.get("endDate"));
            String format = (String) requestBody.getOrDefault("format", "PDF");

            var generatedReport = reportingService.generateReport(
                    reportType, entityId, startDate, endDate, format);

            Map<String, Object> reportData = new HashMap<>();
            reportData.put("reportType", generatedReport.getReportType());
            reportData.put("title", generatedReport.getTitle());
            reportData.put("format", generatedReport.getFormat());
            reportData.put("status", generatedReport.getStatus());
            reportData.put("generatedAt", generatedReport.getGeneratedAt().toString());
            reportData.put("data", generatedReport.getData());
            reportData.put("period", Map.of("startDate", startDate, "endDate", endDate));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("report", reportData);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to generate report: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // TIME PATTERN ANALYSIS
    // ========================================================================

    /**
     * Analyze behavior incidents by time of day
     *
     * GET /api/behavior-reporting/by-time-of-day?startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/by-time-of-day")
    public ResponseEntity<Map<String, Object>> analyzeByTimeOfDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            var analyses = reportingService.analyzeByTimeOfDay(startDate, endDate);

            List<Map<String, Object>> analysisData = analyses.stream()
                    .map(analysis -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("hour", analysis.getHour());
                        data.put("timeLabel", analysis.getTimeLabel());
                        data.put("totalIncidents", analysis.getTotalIncidents());
                        data.put("positiveIncidents", analysis.getPositiveIncidents());
                        data.put("negativeIncidents", analysis.getNegativeIncidents());
                        data.put("percentageOfTotal", String.format("%.2f%%", analysis.getPercentageOfTotal()));
                        return data;
                    })
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("timeOfDayAnalysis", analysisData);
            response.put("count", analysisData.size());
            response.put("period", Map.of("startDate", startDate, "endDate", endDate));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to analyze by time of day: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Analyze behavior incidents by day of week
     *
     * GET /api/behavior-reporting/by-day-of-week?startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/by-day-of-week")
    public ResponseEntity<Map<String, Object>> analyzeByDayOfWeek(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            var analyses = reportingService.analyzeByDayOfWeek(startDate, endDate);

            List<Map<String, Object>> analysisData = analyses.stream()
                    .map(analysis -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("dayOfWeek", analysis.getDayOfWeek());
                        data.put("dayNumber", analysis.getDayNumber());
                        data.put("totalIncidents", analysis.getTotalIncidents());
                        data.put("positiveIncidents", analysis.getPositiveIncidents());
                        data.put("negativeIncidents", analysis.getNegativeIncidents());
                        data.put("percentageOfTotal", String.format("%.2f%%", analysis.getPercentageOfTotal()));
                        return data;
                    })
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("dayOfWeekAnalysis", analysisData);
            response.put("count", analysisData.size());
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
    // TEACHER/CLASSROOM ANALYTICS
    // ========================================================================

    /**
     * Analyze behavior incidents by reporting teacher
     *
     * GET /api/behavior-reporting/by-teacher?startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/by-teacher")
    public ResponseEntity<Map<String, Object>> analyzeByTeacher(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            var analyses = reportingService.analyzeByTeacher(startDate, endDate);

            List<Map<String, Object>> analysisData = analyses.stream()
                    .map(analysis -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("teacherId", analysis.getTeacherId());
                        data.put("teacherName", analysis.getTeacherName());
                        data.put("totalIncidents", analysis.getTotalIncidents());
                        data.put("positiveIncidents", analysis.getPositiveIncidents());
                        data.put("negativeIncidents", analysis.getNegativeIncidents());
                        data.put("positiveRatio", String.format("%.2f%%", analysis.getPositiveRatio()));
                        data.put("uniqueStudents", analysis.getUniqueStudents());
                        data.put("mostCommonCategory", analysis.getMostCommonCategory());
                        return data;
                    })
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("teacherAnalysis", analysisData);
            response.put("count", analysisData.size());
            response.put("period", Map.of("startDate", startDate, "endDate", endDate));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to analyze by teacher: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Analyze behavior incidents by course
     *
     * GET /api/behavior-reporting/by-course?startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/by-course")
    public ResponseEntity<Map<String, Object>> analyzeByCourse(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            var analyses = reportingService.analyzeByCourse(startDate, endDate);

            List<Map<String, Object>> analysisData = analyses.stream()
                    .map(analysis -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("courseId", analysis.getCourseId());
                        data.put("courseName", analysis.getCourseName());
                        data.put("courseCode", analysis.getCourseCode());
                        data.put("totalIncidents", analysis.getTotalIncidents());
                        data.put("positiveIncidents", analysis.getPositiveIncidents());
                        data.put("negativeIncidents", analysis.getNegativeIncidents());
                        data.put("uniqueStudents", analysis.getUniqueStudents());
                        return data;
                    })
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("courseAnalysis", analysisData);
            response.put("count", analysisData.size());
            response.put("period", Map.of("startDate", startDate, "endDate", endDate));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to analyze by course: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // PREDICTIVE RISK ANALYSIS
    // ========================================================================

    /**
     * Get predictive risk assessment for a student
     *
     * GET /api/behavior-reporting/students/{studentId}/risk-assessment
     */
    @GetMapping("/students/{studentId}/risk-assessment")
    public ResponseEntity<Map<String, Object>> getPredictiveRiskAssessment(
            @PathVariable Long studentId) {

        try {
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            var assessment = reportingService.calculatePredictiveRisk(student);

            Map<String, Object> assessmentData = new HashMap<>();
            assessmentData.put("studentId", assessment.getStudentId());
            assessmentData.put("studentName", assessment.getStudentName());
            assessmentData.put("riskScore", assessment.getRiskScore());
            assessmentData.put("riskLevel", assessment.getRiskLevel());
            assessmentData.put("recentIncidents", assessment.getRecentIncidents());
            assessmentData.put("previousPeriodIncidents", assessment.getPreviousPeriodIncidents());
            assessmentData.put("trend", assessment.getTrend());
            assessmentData.put("trendPercentage", String.format("%.2f%%", assessment.getTrendPercentage()));
            assessmentData.put("severityScore", assessment.getSeverityScore());
            assessmentData.put("adminReferrals", assessment.getAdminReferrals());
            assessmentData.put("hasClusteredIncidents", assessment.isHasClusteredIncidents());
            assessmentData.put("recommendations", assessment.getRecommendations());
            assessmentData.put("assessmentDate", assessment.getAssessmentDate());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("riskAssessment", assessmentData);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to calculate risk assessment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // COHORT ANALYSIS
    // ========================================================================

    /**
     * Analyze behavior by student cohort/demographics
     *
     * GET /api/behavior-reporting/by-cohort?startDate=2025-01-01&endDate=2025-12-31&cohortType=grade
     *
     * cohortType: grade, gender, or program
     */
    @GetMapping("/by-cohort")
    public ResponseEntity<Map<String, Object>> analyzeByCohort(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "grade") String cohortType) {

        try {
            var analyses = reportingService.analyzeByCohort(startDate, endDate, cohortType);

            List<Map<String, Object>> analysisData = analyses.stream()
                    .map(analysis -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("cohortType", analysis.getCohortType());
                        data.put("cohortValue", analysis.getCohortValue());
                        data.put("totalIncidents", analysis.getTotalIncidents());
                        data.put("positiveIncidents", analysis.getPositiveIncidents());
                        data.put("negativeIncidents", analysis.getNegativeIncidents());
                        data.put("totalStudentsInCohort", analysis.getTotalStudentsInCohort());
                        data.put("incidentsPerStudent", String.format("%.2f", analysis.getIncidentsPerStudent()));
                        data.put("uniqueStudentsWithIncidents", analysis.getUniqueStudentsWithIncidents());
                        data.put("studentParticipationRate", String.format("%.2f%%", analysis.getStudentParticipationRate()));
                        return data;
                    })
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("cohortAnalysis", analysisData);
            response.put("cohortType", cohortType);
            response.put("count", analysisData.size());
            response.put("period", Map.of("startDate", startDate, "endDate", endDate));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to analyze by cohort: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // CORRELATION ANALYSIS
    // ========================================================================

    /**
     * Analyze correlation between behavior and academic performance
     *
     * GET /api/behavior-reporting/academic-correlation?startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/academic-correlation")
    public ResponseEntity<Map<String, Object>> analyzeAcademicCorrelation(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            var correlation = reportingService.analyzeAcademicCorrelation(startDate, endDate);

            Map<String, Object> correlationData = new HashMap<>();
            correlationData.put("periodStart", correlation.getPeriodStart());
            correlationData.put("periodEnd", correlation.getPeriodEnd());
            correlationData.put("averageIncidentsByGpaRange", correlation.getAverageIncidentsByGpaRange());
            correlationData.put("studentsAnalyzed", correlation.getStudentsAnalyzed());
            correlationData.put("studentsWithIncidents", correlation.getStudentsWithIncidents());
            correlationData.put("correlationCoefficient", correlation.getCorrelationCoefficient());
            correlationData.put("interpretation", correlation.getInterpretation());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("academicCorrelation", correlationData);
            response.put("period", Map.of("startDate", startDate, "endDate", endDate));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to analyze academic correlation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
