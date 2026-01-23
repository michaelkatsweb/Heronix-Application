package com.heronix.controller.api;

import com.heronix.service.GradebookService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

/**
 * REST API Controller for Student Grade Access
 *
 * Provides student-centric endpoints for grade viewing and academic progress tracking:
 * - Student view of their own grades across all courses
 * - Course-specific grade details (assignments, tests, projects)
 * - Overall GPA and academic standing
 * - Grade history and trends
 * - Missing assignments and grade alerts
 * - Progress reports and report cards
 * - Parent/guardian grade access
 *
 * This controller complements the teacher-centric GradebookApiController
 * by providing read-only grade access oriented toward students and parents.
 *
 * Security considerations:
 * - Students should only access their own grades
 * - Parents should only access their child's grades
 * - Teachers can access grades for students in their classes
 * - Administrators have full access
 *
 * Grade Display Features:
 * - Current grade in each course
 * - Assignment-level grades with feedback
 * - Category breakdowns (tests, homework, projects)
 * - What-if grade calculator support
 * - Grade improvement recommendations
 * - Academic alerts and warnings
 *
 * @author Heronix Development Team
 * @version 2.0
 * @since December 30, 2025
 * @updated January 20, 2026 - Full implementation
 */
@RestController
@RequestMapping("/api/student-grades")
@RequiredArgsConstructor
public class StudentGradeApiController {

    private final GradebookService gradebookService;

    // ========================================================================
    // STUDENT GRADE OVERVIEW
    // ========================================================================

    /**
     * Get all grades for a student across all courses
     *
     * GET /api/student-grades/students/{studentId}/all-grades
     *
     * Returns a comprehensive view of the student's grades including:
     * - Current grade in each enrolled course
     * - Overall GPA
     * - Grade distribution
     * - Academic standing
     */
    @GetMapping("/students/{studentId}/all-grades")
    public ResponseEntity<Map<String, Object>> getAllGrades(@PathVariable Long studentId) {
        try {
            var allGrades = gradebookService.getStudentAllGrades(studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("grades", allGrades);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve grades: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get detailed grade for a student in a specific course
     *
     * GET /api/student-grades/students/{studentId}/courses/{courseId}
     *
     * Returns:
     * - Current course grade
     * - Category breakdowns
     * - Individual assignment grades
     * - What-if scenarios
     * - Grade trends
     */
    @GetMapping("/students/{studentId}/courses/{courseId}")
    public ResponseEntity<Map<String, Object>> getCourseGrade(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {

        try {
            var courseGrade = gradebookService.calculateCourseGrade(studentId, courseId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("courseId", courseId);
            response.put("gradeDetails", courseGrade);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve course grade: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // MISSING ASSIGNMENTS AND ALERTS
    // ========================================================================

    /**
     * Get missing assignments for a student
     *
     * GET /api/student-grades/students/{studentId}/missing-assignments
     *
     * Returns all assignments that:
     * - Have no grade recorded
     * - Are past due date
     * - Are impacting the student's grade
     */
    @GetMapping("/students/{studentId}/missing-assignments")
    public ResponseEntity<Map<String, Object>> getMissingAssignments(@PathVariable Long studentId) {
        try {
            var missingAssignments = gradebookService.getStudentMissingAssignments(studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("missingAssignments", missingAssignments);
            response.put("count", missingAssignments.size());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve missing assignments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get grade alerts for a student
     *
     * GET /api/student-grades/students/{studentId}/alerts
     *
     * Returns alerts for:
     * - Failing grades (below 60%)
     * - Grade drops (significant decrease)
     * - Missing assignments
     * - At-risk academic status
     */
    @GetMapping("/students/{studentId}/alerts")
    public ResponseEntity<Map<String, Object>> getGradeAlerts(@PathVariable Long studentId) {
        try {
            var alerts = gradebookService.getStudentGradeAlerts(studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("alerts", alerts);
            response.put("count", alerts.size());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve grade alerts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // GPA AND ACADEMIC STANDING
    // ========================================================================

    /**
     * Calculate GPA for a student
     *
     * GET /api/student-grades/students/{studentId}/gpa
     *
     * Returns:
     * - Current term GPA
     * - Cumulative GPA
     * - Weighted/unweighted GPA
     * - Class rank (if available)
     */
    @GetMapping("/students/{studentId}/gpa")
    public ResponseEntity<Map<String, Object>> getStudentGPA(@PathVariable Long studentId) {
        try {
            var gpaData = gradebookService.calculateStudentGPA(studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("gpa", gpaData);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to calculate GPA: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get academic standing for a student
     *
     * GET /api/student-grades/students/{studentId}/academic-standing
     *
     * Returns academic status:
     * - Honor Roll
     * - Good Standing
     * - Academic Warning
     * - Academic Probation
     */
    @GetMapping("/students/{studentId}/academic-standing")
    public ResponseEntity<Map<String, Object>> getAcademicStanding(@PathVariable Long studentId) {
        try {
            var standing = gradebookService.getStudentAcademicStanding(studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("academicStanding", standing);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve academic standing: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // GRADE HISTORY AND TRENDS
    // ========================================================================

    /**
     * Get grade history for a student in a course
     *
     * GET /api/student-grades/students/{studentId}/courses/{courseId}/history
     *
     * Returns grade progression over time showing:
     * - Assignment grades chronologically
     * - Running course grade
     * - Trend analysis (improving/declining)
     */
    @GetMapping("/students/{studentId}/courses/{courseId}/history")
    public ResponseEntity<Map<String, Object>> getGradeHistory(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {

        try {
            var history = gradebookService.getStudentCourseGradeHistory(studentId, courseId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("courseId", courseId);
            response.put("gradeHistory", history);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve grade history: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get grade trends across all courses
     *
     * GET /api/student-grades/students/{studentId}/trends?startDate=2025-01-01&endDate=2025-12-31
     *
     * Shows grade trends over a date range
     */
    @GetMapping("/students/{studentId}/trends")
    public ResponseEntity<Map<String, Object>> getGradeTrends(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            var trendsData = gradebookService.getStudentGradeTrends(studentId, startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("trends", trendsData);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve grade trends: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // PROGRESS REPORTS
    // ========================================================================

    /**
     * Generate progress report for a student
     *
     * GET /api/student-grades/students/{studentId}/progress-report
     *
     * Comprehensive progress report including:
     * - Current grades in all courses
     * - Attendance summary
     * - Behavior incidents
     * - Teacher comments
     * - Areas of strength and concern
     */
    @GetMapping("/students/{studentId}/progress-report")
    public ResponseEntity<Map<String, Object>> getProgressReport(@PathVariable Long studentId) {
        try {
            var progressReport = gradebookService.generateProgressReport(studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("progressReport", progressReport);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to generate progress report: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Generate report card for a student for a specific term
     *
     * GET /api/student-grades/students/{studentId}/report-card?termId=1
     *
     * Official report card including:
     * - Final grades for each course
     * - Term GPA
     * - Credits earned
     * - Attendance statistics
     * - Academic honors/warnings
     */
    @GetMapping("/students/{studentId}/report-card")
    public ResponseEntity<Map<String, Object>> getReportCard(
            @PathVariable Long studentId,
            @RequestParam(required = false) Long termId) {

        try {
            var reportCard = termId != null
                    ? gradebookService.generateReportCard(studentId, termId)
                    : gradebookService.generateCurrentReportCard(studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("reportCard", reportCard);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to generate report card: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // WHAT-IF CALCULATOR
    // ========================================================================

    /**
     * Calculate what-if grade scenarios
     *
     * POST /api/student-grades/students/{studentId}/courses/{courseId}/what-if
     *
     * Request Body:
     * {
     *   "hypotheticalAssignments": [
     *     {"assignmentId": 123, "hypotheticalScore": 95.0},
     *     {"assignmentId": 124, "hypotheticalScore": 88.5}
     *   ]
     * }
     *
     * Returns what the course grade would be with hypothetical scores
     */
    @PostMapping("/students/{studentId}/courses/{courseId}/what-if")
    public ResponseEntity<Map<String, Object>> calculateWhatIfGrade(
            @PathVariable Long studentId,
            @PathVariable Long courseId,
            @RequestBody Map<String, Object> requestBody) {

        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> hypotheticalAssignmentsRaw =
                (List<Map<String, Object>>) requestBody.get("hypotheticalAssignments");

            // Convert to HypotheticalAssignment objects
            List<GradebookService.HypotheticalAssignment> hypotheticalAssignments = hypotheticalAssignmentsRaw.stream()
                    .map(map -> GradebookService.HypotheticalAssignment.builder()
                            .assignmentId(((Number) map.get("assignmentId")).longValue())
                            .hypotheticalScore(((Number) map.get("hypotheticalScore")).doubleValue())
                            .build())
                    .toList();

            var whatIfResult = gradebookService.calculateWhatIfGrade(studentId, courseId, hypotheticalAssignments);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("courseId", courseId);
            response.put("whatIfGrade", whatIfResult);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to calculate what-if grade: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // ASSIGNMENT DETAILS
    // ========================================================================

    /**
     * Get specific assignment grade with detailed feedback
     *
     * GET /api/student-grades/students/{studentId}/assignments/{assignmentId}/grade
     *
     * Returns:
     * - Score and percentage
     * - Teacher feedback/comments
     * - Rubric scores (if applicable)
     * - Submission date
     * - Late penalties
     */
    @GetMapping("/students/{studentId}/assignments/{assignmentId}/grade")
    public ResponseEntity<Map<String, Object>> getAssignmentGrade(
            @PathVariable Long studentId,
            @PathVariable Long assignmentId) {

        try {
            var assignmentGrade = gradebookService.getStudentAssignmentGrade(studentId, assignmentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("assignmentId", assignmentId);
            response.put("grade", assignmentGrade);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve assignment grade: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
