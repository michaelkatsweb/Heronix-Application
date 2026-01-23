package com.heronix.controller;

import com.heronix.service.EnrollmentReportService;
import com.heronix.service.EnrollmentReportService.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Enrollment Report REST API Controller
 *
 * Provides endpoints for enrollment reporting and analytics:
 * - Course enrollment statistics
 * - Student enrollment summaries
 * - Grade level analytics
 * - School-wide enrollment summary
 * - Enrollment trend analysis
 *
 * Security:
 * - All endpoints require authentication
 * - Most endpoints require ADMIN or TEACHER role
 * - Some endpoints require ADMIN role only
 *
 * @author Heronix Development Team
 * @version 1.0.0
 * @since 2025-12-28
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/reports/enrollment")
@CrossOrigin(origins = "*") // Configure appropriately for production
public class EnrollmentReportController {

    private final EnrollmentReportService enrollmentReportService;

    @Autowired
    public EnrollmentReportController(EnrollmentReportService enrollmentReportService) {
        this.enrollmentReportService = enrollmentReportService;
    }

    // ============================================================
    // Course Enrollment Endpoints
    // ============================================================

    /**
     * GET /api/reports/enrollment/course/{courseId}
     * Get enrollment statistics for a specific course.
     *
     * @param courseId the course ID
     * @return course enrollment statistics
     */
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<CourseEnrollmentStats> getCourseEnrollmentStats(@PathVariable Long courseId) {
        CourseEnrollmentStats stats = enrollmentReportService.getCourseEnrollmentStats(courseId);
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/reports/enrollment/courses/all
     * Get enrollment statistics for all courses.
     *
     * @return list of course enrollment statistics
     */
    @GetMapping("/courses/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<CourseEnrollmentStats>> getAllCourseEnrollmentStats() {
        List<CourseEnrollmentStats> stats = enrollmentReportService.getAllCourseEnrollmentStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/reports/enrollment/courses/over-enrolled
     * Get courses that are over-enrolled (exceeding max capacity).
     *
     * @return list of over-enrolled courses
     */
    @GetMapping("/courses/over-enrolled")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CourseEnrollmentStats>> getOverEnrolledCourses() {
        List<CourseEnrollmentStats> courses = enrollmentReportService.getOverEnrolledCourses();
        return ResponseEntity.ok(courses);
    }

    /**
     * GET /api/reports/enrollment/courses/under-enrolled
     * Get courses that are under-enrolled (below 50% capacity).
     *
     * @return list of under-enrolled courses
     */
    @GetMapping("/courses/under-enrolled")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CourseEnrollmentStats>> getUnderEnrolledCourses() {
        List<CourseEnrollmentStats> courses = enrollmentReportService.getUnderEnrolledCourses();
        return ResponseEntity.ok(courses);
    }

    /**
     * GET /api/reports/enrollment/courses/nearing-capacity
     * Get courses nearing capacity (80-100% full).
     *
     * @return list of courses nearing capacity
     */
    @GetMapping("/courses/nearing-capacity")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CourseEnrollmentStats>> getCoursesNearingCapacity() {
        List<CourseEnrollmentStats> courses = enrollmentReportService.getCoursesNearingCapacity();
        return ResponseEntity.ok(courses);
    }

    // ============================================================
    // Student Enrollment Endpoints
    // ============================================================

    /**
     * GET /api/reports/enrollment/student/{studentId}
     * Get enrollment summary for a specific student.
     *
     * @param studentId the student ID
     * @return student enrollment summary
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<StudentEnrollmentSummary> getStudentEnrollmentSummary(@PathVariable Long studentId) {
        StudentEnrollmentSummary summary = enrollmentReportService.getStudentEnrollmentSummary(studentId);
        return ResponseEntity.ok(summary);
    }

    /**
     * GET /api/reports/enrollment/students/excessive-load
     * Get students with excessive course loads.
     *
     * @param maxCourses maximum recommended courses (default: 8)
     * @return list of students with excessive course load
     */
    @GetMapping("/students/excessive-load")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StudentEnrollmentSummary>> getStudentsWithExcessiveCourseLoad(
            @RequestParam(defaultValue = "8") int maxCourses) {
        List<StudentEnrollmentSummary> students =
            enrollmentReportService.getStudentsWithExcessiveCourseLoad(maxCourses);
        return ResponseEntity.ok(students);
    }

    /**
     * GET /api/reports/enrollment/students/low-load
     * Get students with low course loads.
     *
     * @param minCourses minimum recommended courses (default: 4)
     * @return list of students with low course load
     */
    @GetMapping("/students/low-load")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StudentEnrollmentSummary>> getStudentsWithLowCourseLoad(
            @RequestParam(defaultValue = "4") int minCourses) {
        List<StudentEnrollmentSummary> students =
            enrollmentReportService.getStudentsWithLowCourseLoad(minCourses);
        return ResponseEntity.ok(students);
    }

    // ============================================================
    // Grade Level & School-Wide Analytics Endpoints
    // ============================================================

    /**
     * GET /api/reports/enrollment/grade-levels
     * Get enrollment breakdown by grade level.
     *
     * @return enrollment statistics by grade level
     */
    @GetMapping("/grade-levels")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<Integer, GradeLevelEnrollmentStats>> getEnrollmentByGradeLevel() {
        Map<Integer, GradeLevelEnrollmentStats> stats =
            enrollmentReportService.getEnrollmentByGradeLevel();
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/reports/enrollment/school-summary
     * Get overall enrollment summary for the school.
     *
     * @return school-wide enrollment summary
     */
    @GetMapping("/school-summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SchoolEnrollmentSummary> getSchoolEnrollmentSummary() {
        SchoolEnrollmentSummary summary = enrollmentReportService.getSchoolEnrollmentSummary();
        return ResponseEntity.ok(summary);
    }

    // ============================================================
    // Enrollment Trend Endpoints
    // ============================================================

    /**
     * GET /api/reports/enrollment/trends
     * Get enrollment trends over time.
     *
     * @param startDate start date (format: yyyy-MM-dd)
     * @param endDate end date (format: yyyy-MM-dd)
     * @return enrollment trend report
     */
    @GetMapping("/trends")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnrollmentTrendReport> getEnrollmentTrends(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build();
        }

        EnrollmentTrendReport report = enrollmentReportService.getEnrollmentTrends(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * GET /api/reports/enrollment/trends/current-year
     * Get enrollment trends for the current academic year.
     *
     * @return enrollment trend report for current year
     */
    @GetMapping("/trends/current-year")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnrollmentTrendReport> getCurrentYearEnrollmentTrends() {
        // Assume academic year starts September 1
        LocalDate now = LocalDate.now();
        LocalDate startDate;

        if (now.getMonthValue() >= 9) {
            // Current academic year started this year
            startDate = LocalDate.of(now.getYear(), 9, 1);
        } else {
            // Current academic year started last year
            startDate = LocalDate.of(now.getYear() - 1, 9, 1);
        }

        EnrollmentTrendReport report = enrollmentReportService.getEnrollmentTrends(startDate, now);
        return ResponseEntity.ok(report);
    }

    // ============================================================
    // Health Check Endpoint
    // ============================================================

    /**
     * GET /api/reports/enrollment/health
     * Health check endpoint for enrollment reporting service.
     *
     * @return health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "EnrollmentReportService",
            "timestamp", LocalDate.now().toString()
        ));
    }
}
