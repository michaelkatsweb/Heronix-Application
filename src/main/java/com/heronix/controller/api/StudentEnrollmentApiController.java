package com.heronix.controller.api;

import com.heronix.model.domain.Course;
import com.heronix.model.domain.CourseSection;
import com.heronix.model.domain.Student;
import com.heronix.service.PrerequisiteValidationService;
import com.heronix.service.StudentEnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST API Controller for Student Enrollment Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class StudentEnrollmentApiController {

    private final StudentEnrollmentService enrollmentService;

    // ==================== Basic Enrollment Operations ====================

    @PostMapping("/enroll")
    public ResponseEntity<Map<String, String>> enrollStudent(
            @RequestParam Long studentId,
            @RequestParam Long courseId) {
        enrollmentService.enrollStudent(studentId, courseId);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Student enrolled successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/unenroll")
    public ResponseEntity<Map<String, String>> unenrollStudent(
            @RequestParam Long studentId,
            @RequestParam Long courseId) {
        enrollmentService.unenrollStudent(studentId, courseId);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Student unenrolled successfully");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/bulk-enroll-students")
    public ResponseEntity<Map<String, String>> bulkEnrollStudents(
            @RequestBody List<Long> studentIds,
            @RequestParam Long courseId) {
        enrollmentService.bulkEnrollStudents(studentIds, courseId);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Bulk enrollment completed");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/bulk-enroll-courses")
    public ResponseEntity<Map<String, String>> bulkEnrollCourses(
            @RequestParam Long studentId,
            @RequestBody List<Long> courseIds) {
        enrollmentService.bulkEnrollCourses(studentId, courseIds);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Student enrolled in multiple courses");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================== Query Operations ====================

    @GetMapping("/student/{studentId}/courses")
    public ResponseEntity<List<Course>> getStudentEnrollments(@PathVariable Long studentId) {
        List<Course> courses = enrollmentService.getStudentEnrollments(studentId);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/course/{courseId}/students")
    public ResponseEntity<List<Student>> getCourseEnrollments(@PathVariable Long courseId) {
        List<Student> students = enrollmentService.getCourseEnrollments(courseId);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/student/{studentId}/available-courses")
    public ResponseEntity<List<Course>> getAvailableCoursesForStudent(@PathVariable Long studentId) {
        List<Course> courses = enrollmentService.getAvailableCoursesForStudent(studentId);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/student/{studentId}/available-courses/by-level")
    public ResponseEntity<List<Course>> getAvailableCoursesByLevel(
            @PathVariable Long studentId,
            @RequestParam String level) {
        List<Course> courses = enrollmentService.getAvailableCoursesByLevel(studentId, level);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/student/{studentId}/available-courses/by-subject")
    public ResponseEntity<List<Course>> getAvailableCoursesBySubject(
            @PathVariable Long studentId,
            @RequestParam String subject) {
        List<Course> courses = enrollmentService.getAvailableCoursesBySubject(studentId, subject);
        return ResponseEntity.ok(courses);
    }

    // ==================== Validation ====================

    @GetMapping("/validate/can-enroll")
    public ResponseEntity<Map<String, Boolean>> canEnroll(
            @RequestParam Long studentId,
            @RequestParam Long courseId) {
        boolean canEnroll = enrollmentService.canEnroll(studentId, courseId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("canEnroll", canEnroll);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate/prerequisites")
    public ResponseEntity<PrerequisiteValidationService.ValidationResult> validatePrerequisites(
            @RequestParam Long studentId,
            @RequestParam Long courseId) {
        PrerequisiteValidationService.ValidationResult result =
                enrollmentService.getPrerequisiteValidation(studentId, courseId);
        return ResponseEntity.ok(result);
    }

    // ==================== Statistics ====================

    @GetMapping("/student/{studentId}/stats")
    public ResponseEntity<StudentEnrollmentService.EnrollmentStats> getStudentStats(@PathVariable Long studentId) {
        StudentEnrollmentService.EnrollmentStats stats = enrollmentService.getStudentStats(studentId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/course/{courseId}/stats")
    public ResponseEntity<StudentEnrollmentService.CourseEnrollmentStats> getCourseStats(@PathVariable Long courseId) {
        StudentEnrollmentService.CourseEnrollmentStats stats = enrollmentService.getCourseStats(courseId);
        return ResponseEntity.ok(stats);
    }

    // ==================== Auto-Placement ====================

    @PostMapping("/auto-place")
    public ResponseEntity<StudentEnrollmentService.AutoPlacementResult> autoPlaceStudent(
            @RequestParam Long studentId,
            @RequestBody List<Long> requestedCourseIds) {
        StudentEnrollmentService.AutoPlacementResult result =
                enrollmentService.autoPlaceStudent(studentId, requestedCourseIds);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/student/{studentId}/course/{courseId}/best-section")
    public ResponseEntity<CourseSection> findBestAvailableSection(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        Optional<CourseSection> section = enrollmentService.findBestAvailableSection(
                enrollmentService.getStudentEnrollments(studentId).isEmpty() ?
                        null : enrollmentService.getCourseEnrollments(courseId).get(0),
                courseId);

        return section.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{studentId}/course/{courseId}/alternate-section")
    public ResponseEntity<CourseSection> findAlternateSection(
            @PathVariable Long studentId,
            @PathVariable Long courseId,
            @RequestParam Long excludeSectionId) {
        Optional<CourseSection> section = enrollmentService.findAlternateSection(
                enrollmentService.getCourseEnrollments(courseId).isEmpty() ?
                        null : enrollmentService.getCourseEnrollments(courseId).get(0),
                courseId,
                excludeSectionId);

        return section.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{studentId}/course/{courseId}/section-options")
    public ResponseEntity<List<StudentEnrollmentService.SectionOption>> getAvailableSectionOptions(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        List<StudentEnrollmentService.SectionOption> options =
                enrollmentService.getAvailableSectionOptions(studentId, courseId);
        return ResponseEntity.ok(options);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getStudentEnrollmentDashboard(@PathVariable Long studentId) {
        Map<String, Object> dashboard = new HashMap<>();

        List<Course> enrolledCourses = enrollmentService.getStudentEnrollments(studentId);
        List<Course> availableCourses = enrollmentService.getAvailableCoursesForStudent(studentId);
        StudentEnrollmentService.EnrollmentStats stats = enrollmentService.getStudentStats(studentId);

        dashboard.put("studentId", studentId);
        dashboard.put("enrolledCourses", enrolledCourses);
        dashboard.put("enrolledCoursesCount", enrolledCourses.size());
        dashboard.put("availableCoursesCount", availableCourses.size());
        dashboard.put("totalCredits", stats.getTotalCredits());
        dashboard.put("averageCreditsPerCourse", stats.getAverageCreditsPerCourse());

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/course/{courseId}")
    public ResponseEntity<Map<String, Object>> getCourseEnrollmentDashboard(@PathVariable Long courseId) {
        Map<String, Object> dashboard = new HashMap<>();

        List<Student> enrolledStudents = enrollmentService.getCourseEnrollments(courseId);
        StudentEnrollmentService.CourseEnrollmentStats stats = enrollmentService.getCourseStats(courseId);

        dashboard.put("courseId", courseId);
        dashboard.put("enrolledStudents", enrolledStudents);
        dashboard.put("enrolledCount", stats.getEnrolled());
        dashboard.put("capacity", stats.getCapacity());
        dashboard.put("availableSeats", stats.getAvailableSeats());
        dashboard.put("percentageFull", stats.getPercentageFull());
        dashboard.put("isFull", stats.getPercentageFull() >= 100.0);
        dashboard.put("isNearlyFull", stats.getPercentageFull() >= 90.0);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getEnrollmentOverview() {
        Map<String, Object> dashboard = new HashMap<>();

        // This would need additional service methods to get system-wide statistics
        // For now, return placeholder structure
        dashboard.put("message", "System-wide enrollment overview");

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/auto-placement/{studentId}")
    public ResponseEntity<Map<String, Object>> getAutoPlacementDashboard(
            @PathVariable Long studentId,
            @RequestBody List<Long> requestedCourseIds) {
        Map<String, Object> dashboard = new HashMap<>();

        // Get section options for each requested course
        for (Long courseId : requestedCourseIds) {
            List<StudentEnrollmentService.SectionOption> options =
                    enrollmentService.getAvailableSectionOptions(studentId, courseId);
            dashboard.put("course_" + courseId + "_options", options);
        }

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/validation/{studentId}/{courseId}")
    public ResponseEntity<Map<String, Object>> getEnrollmentValidationDashboard(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        Map<String, Object> dashboard = new HashMap<>();

        boolean canEnroll = enrollmentService.canEnroll(studentId, courseId);
        PrerequisiteValidationService.ValidationResult prereqResult =
                enrollmentService.getPrerequisiteValidation(studentId, courseId);
        StudentEnrollmentService.CourseEnrollmentStats courseStats =
                enrollmentService.getCourseStats(courseId);

        dashboard.put("studentId", studentId);
        dashboard.put("courseId", courseId);
        dashboard.put("canEnroll", canEnroll);
        dashboard.put("prerequisiteValidation", prereqResult);
        dashboard.put("courseAvailability", courseStats);
        dashboard.put("hasCapacity", courseStats.getAvailableSeats() > 0);

        return ResponseEntity.ok(dashboard);
    }
}
