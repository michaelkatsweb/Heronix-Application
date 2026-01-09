package com.heronix.controller.api;

import com.heronix.model.domain.Course;
import com.heronix.model.domain.Student;
import com.heronix.model.dto.CourseSuggestion;
import com.heronix.model.dto.StudentSearchCriteria;
import com.heronix.repository.CourseRepository;
import com.heronix.repository.StudentRepository;
import com.heronix.service.BulkEnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST API Controller for Bulk Student Enrollment Operations
 *
 * Provides endpoints for smart student search, bulk course enrollment/unenrollment,
 * AI-powered course suggestions, and automated enrollment workflows.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/bulk-enrollment")
@RequiredArgsConstructor
public class BulkEnrollmentApiController {

    private final BulkEnrollmentService bulkEnrollmentService;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    // ==================== Student Search ====================

    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> searchStudents(@RequestBody StudentSearchCriteria criteria) {
        try {
            List<Student> students = bulkEnrollmentService.searchStudents(criteria);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", students.size());
            response.put("students", students);
            response.put("criteria", criteria);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/search/count")
    public ResponseEntity<Map<String, Object>> countStudents(@RequestBody StudentSearchCriteria criteria) {
        try {
            List<Student> students = bulkEnrollmentService.searchStudents(criteria);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", students.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Bulk Enrollment ====================

    @PostMapping("/enroll")
    public ResponseEntity<Map<String, Object>> bulkEnroll(
            @RequestBody Map<String, Object> request) {

        try {
            @SuppressWarnings("unchecked")
            List<Long> studentIds = (List<Long>) request.get("studentIds");
            Long courseId = ((Number) request.get("courseId")).longValue();

            List<Student> students = studentRepository.findAllById(studentIds);
            Optional<Course> courseOpt = courseRepository.findById(courseId);

            if (courseOpt.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Course not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            int enrolled = bulkEnrollmentService.bulkEnroll(students, courseOpt.get());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("enrolledCount", enrolled);
            response.put("totalStudents", students.size());
            response.put("courseId", courseId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/unenroll")
    public ResponseEntity<Map<String, Object>> bulkUnenroll(
            @RequestBody Map<String, Object> request) {

        try {
            @SuppressWarnings("unchecked")
            List<Long> studentIds = (List<Long>) request.get("studentIds");
            Long courseId = ((Number) request.get("courseId")).longValue();

            List<Student> students = studentRepository.findAllById(studentIds);
            Optional<Course> courseOpt = courseRepository.findById(courseId);

            if (courseOpt.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Course not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            int unenrolled = bulkEnrollmentService.bulkUnenroll(students, courseOpt.get());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("unenrolledCount", unenrolled);
            response.put("totalStudents", students.size());
            response.put("courseId", courseId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Course Suggestions ====================

    @GetMapping("/suggestions/grade/{gradeLevel}")
    public ResponseEntity<List<CourseSuggestion>> getSuggestionsForGrade(@PathVariable int gradeLevel) {
        List<CourseSuggestion> suggestions = bulkEnrollmentService.getSuggestionsForGrade(gradeLevel);
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/suggestions/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getSuggestionsForStudent(@PathVariable Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);

        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<CourseSuggestion> suggestions = bulkEnrollmentService.getSuggestionsForStudent(studentOpt.get());

        Map<String, Object> response = new HashMap<>();
        response.put("studentId", studentId);
        response.put("studentName", studentOpt.get().getFullName());
        response.put("suggestionCount", suggestions.size());
        response.put("suggestions", suggestions);

        return ResponseEntity.ok(response);
    }

    // ==================== Auto Enrollment ====================

    @PostMapping("/auto-enroll")
    public ResponseEntity<Map<String, Object>> autoEnrollStudents(
            @RequestBody Map<String, Object> request) {

        try {
            @SuppressWarnings("unchecked")
            List<Long> studentIds = (List<Long>) request.get("studentIds");
            Boolean applyRequired = (Boolean) request.getOrDefault("applyRequired", true);
            Boolean applyRecommended = (Boolean) request.getOrDefault("applyRecommended", false);

            List<Student> students = studentRepository.findAllById(studentIds);

            int enrolled = bulkEnrollmentService.autoEnrollStudents(students, applyRequired, applyRecommended);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("enrolledCount", enrolled);
            response.put("totalStudents", students.size());
            response.put("applyRequired", applyRequired);
            response.put("applyRecommended", applyRecommended);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/auto-enroll/grade/{gradeLevel}")
    public ResponseEntity<Map<String, Object>> autoEnrollGrade(
            @PathVariable int gradeLevel,
            @RequestParam(defaultValue = "true") boolean applyRequired,
            @RequestParam(defaultValue = "false") boolean applyRecommended) {

        try {
            List<Student> students = studentRepository.findByGradeLevel(String.valueOf(gradeLevel));

            int enrolled = bulkEnrollmentService.autoEnrollStudents(students, applyRequired, applyRecommended);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("gradeLevel", gradeLevel);
            response.put("enrolledCount", enrolled);
            response.put("totalStudents", students.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Copy Previous Year Enrollments ====================

    @PostMapping("/copy-previous-year/{studentId}")
    public ResponseEntity<Map<String, Object>> copyPreviousYearEnrollments(
            @PathVariable Long studentId,
            @RequestBody Map<Long, Long> courseMapping) {

        Optional<Student> studentOpt = studentRepository.findById(studentId);

        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            // Convert Long->Long mapping to Course->Course mapping
            Map<Course, Course> courseMappingObjects = courseMapping.entrySet().stream()
                .collect(Collectors.toMap(
                    entry -> courseRepository.findById(entry.getKey()).orElse(null),
                    entry -> courseRepository.findById(entry.getValue()).orElse(null)
                ))
                .entrySet().stream()
                .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            int copied = bulkEnrollmentService.copyPreviousYearEnrollments(studentOpt.get(), courseMappingObjects);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("copiedCount", copied);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Missing Required Courses ====================

    @GetMapping("/missing-required/grade/{gradeLevel}")
    public ResponseEntity<Map<String, Object>> findStudentsMissingRequired(@PathVariable int gradeLevel) {
        List<Student> students = bulkEnrollmentService.findStudentsMissingRequiredCourses(gradeLevel);

        Map<String, Object> response = new HashMap<>();
        response.put("gradeLevel", gradeLevel);
        response.put("count", students.size());
        response.put("students", students);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/missing-required/all")
    public ResponseEntity<Map<String, Object>> findAllStudentsMissingRequired() {
        Map<String, Object> response = new HashMap<>();
        Map<Integer, List<Student>> byGrade = new HashMap<>();

        for (int grade = 9; grade <= 12; grade++) {
            List<Student> students = bulkEnrollmentService.findStudentsMissingRequiredCourses(grade);
            if (!students.isEmpty()) {
                byGrade.put(grade, students);
            }
        }

        long totalCount = byGrade.values().stream().mapToLong(List::size).sum();

        response.put("totalCount", totalCount);
        response.put("byGrade", byGrade);

        return ResponseEntity.ok(response);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getBulkEnrollmentDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        // Count students by enrollment status
        List<Student> allStudents = studentRepository.findByActiveTrue();

        long fullyEnrolled = allStudents.stream()
            .filter(s -> s.getEnrolledCourseCount() >= 6)
            .count();

        long underenrolled = allStudents.stream()
            .filter(s -> s.getEnrolledCourseCount() < 6)
            .count();

        dashboard.put("totalActiveStudents", allStudents.size());
        dashboard.put("fullyEnrolled", fullyEnrolled);
        dashboard.put("underenrolled", underenrolled);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/enrollment-stats")
    public ResponseEntity<Map<String, Object>> getEnrollmentStats() {
        Map<String, Object> stats = new HashMap<>();

        List<Student> allStudents = studentRepository.findByActiveTrue();

        Map<String, Long> byGrade = allStudents.stream()
            .collect(Collectors.groupingBy(Student::getGradeLevel, Collectors.counting()));

        stats.put("totalStudents", allStudents.size());
        stats.put("byGrade", byGrade);

        // Average courses per student
        double avgCourses = allStudents.stream()
            .mapToInt(Student::getEnrolledCourseCount)
            .average()
            .orElse(0.0);

        stats.put("averageCoursesPerStudent", avgCourses);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/dashboard/missing-required-summary")
    public ResponseEntity<Map<String, Object>> getMissingRequiredSummary() {
        Map<String, Object> summary = new HashMap<>();

        Map<Integer, Integer> missingByGrade = new HashMap<>();
        int totalMissing = 0;

        for (int grade = 9; grade <= 12; grade++) {
            List<Student> students = bulkEnrollmentService.findStudentsMissingRequiredCourses(grade);
            missingByGrade.put(grade, students.size());
            totalMissing += students.size();
        }

        summary.put("totalMissingRequired", totalMissing);
        summary.put("byGrade", missingByGrade);

        return ResponseEntity.ok(summary);
    }
}
