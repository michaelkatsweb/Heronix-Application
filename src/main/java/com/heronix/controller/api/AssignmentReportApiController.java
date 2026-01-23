package com.heronix.controller.api;

import com.heronix.service.AssignmentReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

/**
 * REST API Controller for Assignment Reporting and Analytics
 *
 * Provides comprehensive assignment and gradebook analytics:
 * - Assignment completion rates
 * - Grade distribution analysis
 * - Student performance trends
 * - Assignment difficulty analysis
 * - Late submission tracking
 * - Missing assignment identification
 * - Comparative performance analytics
 * - Standard alignment reporting
 *
 * Report Types:
 * - Student assignment reports
 * - Course assignment summaries
 * - Teacher gradebook reports
 * - Grade distribution reports
 * - Standards mastery reports
 * - Missing assignment reports
 * - Late work analysis
 *
 * Analytics Features:
 * - Assignment completion rates
 * - Grade averages and distributions
 * - Performance trends over time
 * - Comparative analysis (student, class, grade)
 * - Standards-based grading analytics
 * - At-risk student identification
 * - Assignment effectiveness metrics
 *
 * Integration Points:
 * - Gradebook System
 * - Student Management
 * - Course Management
 * - Standards/Competencies
 * - Parent Portal
 * - Early Warning System
 *
 * @author Heronix Development Team
 * @version 2.0
 * @since December 30, 2025 - Phase 38
 * @updated January 19, 2026 - Full implementation
 */
@RestController
@RequestMapping("/api/assignment-reports")
@RequiredArgsConstructor
public class AssignmentReportApiController {

    private final AssignmentReportService assignmentReportService;

    // ========================================================================
    // STUDENT ASSIGNMENT REPORTS
    // ========================================================================

    /**
     * Get assignment summary for a student
     *
     * GET /api/assignment-reports/students/{studentId}/summary?courseId=123&startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/students/{studentId}/summary")
    public ResponseEntity<Map<String, Object>> getStudentAssignmentSummary(
            @PathVariable Long studentId,
            @RequestParam(required = false) Long courseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            var summary = assignmentReportService.getStudentAssignmentSummary(
                    studentId, courseId, startDate, endDate);

            Map<String, Object> summaryData = new HashMap<>();
            summaryData.put("studentId", summary.getStudentId());
            summaryData.put("studentName", summary.getStudentName());
            summaryData.put("totalAssignments", summary.getTotalAssignments());
            summaryData.put("completedAssignments", summary.getCompletedAssignments());
            summaryData.put("missingAssignments", summary.getMissingAssignments());
            summaryData.put("lateAssignments", summary.getLateAssignments());
            summaryData.put("averageGrade", String.format("%.2f%%", summary.getAverageGrade()));
            summaryData.put("completionRate", String.format("%.2f%%", summary.getCompletionRate()));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("courseId", courseId);
            response.put("summary", summaryData);
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

    /**
     * Get missing assignments for a student
     *
     * GET /api/assignment-reports/students/{studentId}/missing?courseId=123
     */
    @GetMapping("/students/{studentId}/missing")
    public ResponseEntity<Map<String, Object>> getStudentMissingAssignments(
            @PathVariable Long studentId,
            @RequestParam(required = false) Long courseId) {

        try {
            var missingAssignments = assignmentReportService.getStudentMissingAssignments(studentId, courseId);

            List<Map<String, Object>> missingData = missingAssignments.stream()
                    .map(m -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("assignmentId", m.getAssignmentId());
                        item.put("assignmentTitle", m.getAssignmentTitle());
                        item.put("courseId", m.getCourseId());
                        item.put("courseName", m.getCourseName());
                        item.put("dueDate", m.getDueDate());
                        item.put("maxPoints", m.getMaxPoints());
                        item.put("daysOverdue", m.getDaysOverdue());
                        item.put("categoryName", m.getCategoryName());
                        return item;
                    })
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("courseId", courseId);
            response.put("missingAssignments", missingData);
            response.put("count", missingData.size());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve missing assignments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get performance trends for a student
     *
     * GET /api/assignment-reports/students/{studentId}/trends?courseId=123&startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/students/{studentId}/trends")
    public ResponseEntity<Map<String, Object>> getStudentPerformanceTrends(
            @PathVariable Long studentId,
            @RequestParam(required = false) Long courseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            var trendsData = assignmentReportService.getStudentPerformanceTrends(
                    studentId, courseId, startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("trends", trendsData);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to generate trends: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // COURSE/CLASS REPORTS
    // ========================================================================

    /**
     * Get grade distribution for a course
     *
     * GET /api/assignment-reports/courses/{courseId}/grade-distribution?assignmentId=456
     */
    @GetMapping("/courses/{courseId}/grade-distribution")
    public ResponseEntity<Map<String, Object>> getCourseGradeDistribution(
            @PathVariable Long courseId,
            @RequestParam(required = false) Long assignmentId) {

        try {
            var distribution = assignmentReportService.getCourseGradeDistribution(courseId, assignmentId);

            Map<String, Object> distributionData = new HashMap<>();
            distributionData.put("context", distribution.getContext());
            distributionData.put("totalGrades", distribution.getTotalGrades());
            distributionData.put("gradeA", distribution.getGradeA());
            distributionData.put("gradeB", distribution.getGradeB());
            distributionData.put("gradeC", distribution.getGradeC());
            distributionData.put("gradeD", distribution.getGradeD());
            distributionData.put("gradeF", distribution.getGradeF());
            distributionData.put("average", String.format("%.2f%%", distribution.getAverage()));
            distributionData.put("median", String.format("%.2f%%", distribution.getMedian()));
            distributionData.put("highest", String.format("%.2f%%", distribution.getHighest()));
            distributionData.put("lowest", String.format("%.2f%%", distribution.getLowest()));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("courseId", courseId);
            response.put("assignmentId", assignmentId);
            response.put("distribution", distributionData);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to generate distribution: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get assignment completion rates for a course
     *
     * GET /api/assignment-reports/courses/{courseId}/completion-rates?startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/courses/{courseId}/completion-rates")
    public ResponseEntity<Map<String, Object>> getCourseCompletionRates(
            @PathVariable Long courseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            var completionRates = assignmentReportService.getCourseCompletionRates(courseId, startDate, endDate);

            Map<String, Object> ratesData = new HashMap<>();
            ratesData.put("courseName", completionRates.getCourseName());
            ratesData.put("totalAssignments", completionRates.getTotalAssignments());
            ratesData.put("totalStudents", completionRates.getTotalStudents());
            ratesData.put("expectedSubmissions", completionRates.getExpectedSubmissions());
            ratesData.put("actualSubmissions", completionRates.getActualSubmissions());
            ratesData.put("onTimeSubmissions", completionRates.getOnTimeSubmissions());
            ratesData.put("lateSubmissions", completionRates.getLateSubmissions());
            ratesData.put("missingSubmissions", completionRates.getMissingSubmissions());
            ratesData.put("overallCompletionRate", String.format("%.2f%%", completionRates.getOverallCompletionRate()));
            ratesData.put("onTimeCompletionRate", String.format("%.2f%%", completionRates.getOnTimeCompletionRate()));
            ratesData.put("lateSubmissionRate", String.format("%.2f%%", completionRates.getLateSubmissionRate()));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("courseId", courseId);
            response.put("completionRates", ratesData);
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
            errorResponse.put("error", "Failed to calculate completion rates: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Identify struggling students in a course
     *
     * GET /api/assignment-reports/courses/{courseId}/struggling-students?threshold=70
     */
    @GetMapping("/courses/{courseId}/struggling-students")
    public ResponseEntity<Map<String, Object>> getStrugglingStudents(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "70") double threshold) {

        try {
            var strugglingStudents = assignmentReportService.getStrugglingStudents(courseId, threshold);

            List<Map<String, Object>> studentsData = strugglingStudents.stream()
                    .map(s -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("studentId", s.getStudentId());
                        item.put("studentName", s.getStudentName());
                        item.put("studentNumber", s.getStudentNumber());
                        item.put("gradeLevel", s.getGradeLevel());
                        item.put("currentAverage", String.format("%.2f%%", s.getCurrentAverage()));
                        item.put("pointsBelowThreshold", String.format("%.2f", s.getPointsBelowThreshold()));
                        item.put("missingAssignments", s.getMissingAssignments());
                        item.put("lateAssignments", s.getLateAssignments());
                        item.put("totalAssignments", s.getTotalAssignments());
                        item.put("recentTrend", s.getRecentTrend());
                        return item;
                    })
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("courseId", courseId);
            response.put("strugglingStudents", studentsData);
            response.put("count", studentsData.size());
            response.put("threshold", threshold + "%");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to identify struggling students: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // ASSIGNMENT ANALYTICS
    // ========================================================================

    /**
     * Analyze assignment difficulty
     *
     * GET /api/assignment-reports/assignments/{assignmentId}/difficulty-analysis
     */
    @GetMapping("/assignments/{assignmentId}/difficulty-analysis")
    public ResponseEntity<Map<String, Object>> analyzeAssignmentDifficulty(
            @PathVariable Long assignmentId) {

        try {
            var analysis = assignmentReportService.analyzeAssignmentDifficulty(assignmentId);

            Map<String, Object> analysisData = new HashMap<>();
            analysisData.put("assignmentId", analysis.getAssignmentId());
            analysisData.put("assignmentTitle", analysis.getAssignmentTitle());
            analysisData.put("courseName", analysis.getCourseName());
            analysisData.put("maxPoints", analysis.getMaxPoints());
            analysisData.put("dueDate", analysis.getDueDate());
            analysisData.put("totalStudents", analysis.getTotalStudents());
            analysisData.put("submittedCount", analysis.getSubmittedCount());
            analysisData.put("gradedCount", analysis.getGradedCount());
            analysisData.put("completionRate", String.format("%.2f%%", analysis.getCompletionRate()));
            analysisData.put("averageScore", String.format("%.2f%%", analysis.getAverageScore()));
            analysisData.put("standardDeviation", String.format("%.2f", analysis.getStandardDeviation()));
            analysisData.put("studentsAbove70Percent", analysis.getStudentsAbove70Percent());
            analysisData.put("studentsBelow70Percent", analysis.getStudentsBelow70Percent());
            analysisData.put("difficultyLevel", analysis.getDifficultyLevel());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("analysis", analysisData);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to analyze difficulty: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get assignment submission timeline
     *
     * GET /api/assignment-reports/assignments/{assignmentId}/submission-timeline
     */
    @GetMapping("/assignments/{assignmentId}/submission-timeline")
    public ResponseEntity<Map<String, Object>> getSubmissionTimeline(
            @PathVariable Long assignmentId) {

        try {
            var timeline = assignmentReportService.getSubmissionTimeline(assignmentId);

            Map<String, Object> timelineData = new HashMap<>();
            timelineData.put("assignmentId", timeline.getAssignmentId());
            timelineData.put("assignmentTitle", timeline.getAssignmentTitle());
            timelineData.put("dueDate", timeline.getDueDate());
            timelineData.put("assignedDate", timeline.getAssignedDate());
            timelineData.put("totalSubmissions", timeline.getTotalSubmissions());
            timelineData.put("onTimeSubmissions", timeline.getOnTimeSubmissions());
            timelineData.put("lateSubmissions", timeline.getLateSubmissions());
            timelineData.put("notSubmitted", timeline.getNotSubmitted());

            List<Map<String, Object>> points = timeline.getTimelinePoints().stream()
                    .map(p -> {
                        Map<String, Object> point = new HashMap<>();
                        point.put("date", p.getDate());
                        point.put("submissions", p.getSubmissions());
                        point.put("cumulativeSubmissions", p.getCumulativeSubmissions());
                        point.put("isBeforeDue", p.isIsBeforeDue());
                        return point;
                    })
                    .toList();
            timelineData.put("timelinePoints", points);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("timeline", timelineData);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve timeline: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // STANDARDS-BASED GRADING
    // ========================================================================

    /**
     * Get standards mastery report for a student
     *
     * GET /api/assignment-reports/students/{studentId}/standards-mastery?courseId=123
     */
    @GetMapping("/students/{studentId}/standards-mastery")
    public ResponseEntity<Map<String, Object>> getStudentStandardsMastery(
            @PathVariable Long studentId,
            @RequestParam(required = false) Long courseId) {

        try {
            var mastery = assignmentReportService.getStudentStandardsMastery(studentId, courseId);

            Map<String, Object> masteryData = new HashMap<>();
            masteryData.put("studentId", mastery.getStudentId());
            masteryData.put("studentName", mastery.getStudentName());
            masteryData.put("courseId", mastery.getCourseId());
            masteryData.put("overallMasteryPercent", String.format("%.2f%%", mastery.getOverallMasteryPercent()));

            List<Map<String, Object>> standards = mastery.getMasteryItems().stream()
                    .map(item -> {
                        Map<String, Object> s = new HashMap<>();
                        s.put("standardName", item.getStandardName());
                        s.put("averageScore", String.format("%.2f%%", item.getAverageScore()));
                        s.put("masteryLevel", item.getMasteryLevel());
                        s.put("assignmentCount", item.getAssignmentCount());
                        s.put("completedCount", item.getCompletedCount());
                        return s;
                    })
                    .toList();
            masteryData.put("standards", standards);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mastery", masteryData);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve standards mastery: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get class standards mastery report
     *
     * GET /api/assignment-reports/courses/{courseId}/standards-mastery
     */
    @GetMapping("/courses/{courseId}/standards-mastery")
    public ResponseEntity<Map<String, Object>> getCourseStandardsMastery(
            @PathVariable Long courseId) {

        try {
            var mastery = assignmentReportService.getCourseStandardsMastery(courseId);

            Map<String, Object> masteryData = new HashMap<>();
            masteryData.put("courseId", mastery.getCourseId());
            masteryData.put("courseName", mastery.getCourseName());
            masteryData.put("overallMasteryRate", String.format("%.2f%%", mastery.getOverallMasteryRate()));

            List<Map<String, Object>> standards = mastery.getMasteryItems().stream()
                    .map(item -> {
                        Map<String, Object> s = new HashMap<>();
                        s.put("standardName", item.getStandardName());
                        s.put("averageScore", String.format("%.2f%%", item.getAverageScore()));
                        s.put("assignmentCount", item.getAssignmentCount());
                        s.put("totalGrades", item.getTotalGrades());
                        s.put("studentsAtMastery", item.getStudentsAtMastery());
                        s.put("masteryRate", String.format("%.2f%%", item.getMasteryRate()));
                        return s;
                    })
                    .toList();
            masteryData.put("standards", standards);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mastery", masteryData);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve standards mastery: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // COMPARATIVE ANALYTICS
    // ========================================================================

    /**
     * Compare student performance to class average
     *
     * GET /api/assignment-reports/students/{studentId}/compare-to-class?courseId=123
     */
    @GetMapping("/students/{studentId}/compare-to-class")
    public ResponseEntity<Map<String, Object>> compareStudentToClass(
            @PathVariable Long studentId,
            @RequestParam Long courseId) {

        try {
            var comparison = assignmentReportService.compareStudentToClass(studentId, courseId);

            Map<String, Object> comparisonData = new HashMap<>();
            comparisonData.put("studentId", comparison.getStudentId());
            comparisonData.put("studentName", comparison.getStudentName());
            comparisonData.put("courseName", comparison.getCourseName());
            comparisonData.put("studentAverage", String.format("%.2f%%", comparison.getStudentAverage()));
            comparisonData.put("classAverage", String.format("%.2f%%", comparison.getClassAverage()));
            comparisonData.put("differenceFromAverage", String.format("%.2f", comparison.getDifferenceFromAverage()));
            comparisonData.put("percentile", String.format("%.0f", comparison.getPercentile()));
            comparisonData.put("ranking", comparison.getRanking());
            comparisonData.put("classRank", comparison.getClassRank());
            comparisonData.put("totalStudents", comparison.getTotalStudents());
            comparisonData.put("aboveAverage", comparison.isAboveAverage());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("courseId", courseId);
            response.put("comparison", comparisonData);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to compare performance: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // REPORT GENERATION
    // ========================================================================

    /**
     * Generate comprehensive assignment report
     *
     * POST /api/assignment-reports/generate
     *
     * Request Body:
     * {
     *   "reportType": "student|course|teacher|standards",
     *   "entityId": 123,
     *   "startDate": "2025-01-01",
     *   "endDate": "2025-12-31",
     *   "format": "PDF",
     *   "includeCharts": true
     * }
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateReport(
            @RequestBody Map<String, Object> requestBody) {

        try {
            String reportType = (String) requestBody.get("reportType");
            Long entityId = ((Number) requestBody.get("entityId")).longValue();
            LocalDate startDate = LocalDate.parse((String) requestBody.get("startDate"));
            LocalDate endDate = LocalDate.parse((String) requestBody.get("endDate"));
            String format = (String) requestBody.getOrDefault("format", "PDF");

            var generatedReport = assignmentReportService.generateReport(
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
}
