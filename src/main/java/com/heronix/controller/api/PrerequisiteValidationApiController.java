package com.heronix.controller.api;

import com.heronix.model.domain.Course;
import com.heronix.model.domain.CoursePrerequisite;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.StudentCourseHistory;
import com.heronix.repository.CourseRepository;
import com.heronix.repository.StudentRepository;
import com.heronix.service.PrerequisiteValidationService;
import com.heronix.service.PrerequisiteValidationService.PrerequisiteCheckResult;
import com.heronix.service.PrerequisiteValidationService.ValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REST API Controller for Prerequisite Validation
 *
 * Provides endpoints for validating course prerequisites, checking student eligibility,
 * and managing course history.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/prerequisites")
@RequiredArgsConstructor
public class PrerequisiteValidationApiController {

    private final PrerequisiteValidationService prerequisiteValidationService;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    // ==================== Validation Operations ====================

    @GetMapping("/validate/{studentId}/{courseId}")
    public ResponseEntity<Map<String, Object>> validatePrerequisites(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {

        Optional<Student> studentOpt = studentRepository.findById(studentId);
        Optional<Course> courseOpt = courseRepository.findById(courseId);

        if (studentOpt.isEmpty() || courseOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ValidationResult result = prerequisiteValidationService.validatePrerequisites(
            studentOpt.get(), courseOpt.get());

        Map<String, Object> response = new HashMap<>();
        response.put("studentId", studentId);
        response.put("courseId", courseId);
        response.put("canEnroll", result.isCanEnroll());
        response.put("message", result.getMessage());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validatePrerequisitesPost(
            @RequestBody Map<String, Long> request) {

        Long studentId = request.get("studentId");
        Long courseId = request.get("courseId");

        if (studentId == null || courseId == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "studentId and courseId are required");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Optional<Student> studentOpt = studentRepository.findById(studentId);
        Optional<Course> courseOpt = courseRepository.findById(courseId);

        if (studentOpt.isEmpty() || courseOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ValidationResult result = prerequisiteValidationService.validatePrerequisites(
            studentOpt.get(), courseOpt.get());

        Map<String, Object> response = new HashMap<>();
        response.put("studentId", studentId);
        response.put("courseId", courseId);
        response.put("canEnroll", result.isCanEnroll());
        response.put("message", result.getMessage());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-detailed/{studentId}/{courseId}")
    public ResponseEntity<Map<String, Object>> checkPrerequisitesDetailed(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {

        Optional<Student> studentOpt = studentRepository.findById(studentId);
        Optional<Course> courseOpt = courseRepository.findById(courseId);

        if (studentOpt.isEmpty() || courseOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PrerequisiteCheckResult result = prerequisiteValidationService.checkPrerequisites(
            studentOpt.get(), courseOpt.get());

        Map<String, Object> response = new HashMap<>();
        response.put("studentId", studentId);
        response.put("courseId", courseId);
        response.put("studentName", result.getStudent().getFirstName() + " " + result.getStudent().getLastName());
        response.put("courseName", result.getCourse().getCourseName());
        response.put("meetsPrerequisites", result.isMeetsPrerequisites());
        response.put("missingPrerequisites", result.getMissingPrerequisites());
        response.put("satisfiedPrerequisites", result.getSatisfiedPrerequisites());
        response.put("allowOverride", result.isAllowOverride());
        response.put("failureReason", result.getFailureReason());

        return ResponseEntity.ok(response);
    }

    // ==================== Course Prerequisites ====================

    @GetMapping("/course/{courseId}")
    public ResponseEntity<Map<String, Object>> getCoursePrerequisites(@PathVariable Long courseId) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);

        if (courseOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Course course = courseOpt.get();
        List<CoursePrerequisite> prerequisites = prerequisiteValidationService.getPrerequisites(course);
        String description = prerequisiteValidationService.getPrerequisiteDescription(course);

        Map<String, Object> response = new HashMap<>();
        response.put("courseId", courseId);
        response.put("courseName", course.getCourseName());
        response.put("courseCode", course.getCourseCode());
        response.put("hasPrerequisites", !prerequisites.isEmpty());
        response.put("prerequisiteCount", prerequisites.size());
        response.put("prerequisites", prerequisites);
        response.put("description", description);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/course/{courseId}/chain")
    public ResponseEntity<Map<String, Object>> getPrerequisiteChain(@PathVariable Long courseId) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);

        if (courseOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Course course = courseOpt.get();
        List<Course> chain = prerequisiteValidationService.getPrerequisiteChain(course);

        List<Map<String, Object>> chainData = chain.stream()
            .map(c -> {
                Map<String, Object> courseData = new HashMap<>();
                courseData.put("id", c.getId());
                courseData.put("code", c.getCourseCode());
                courseData.put("name", c.getCourseName());
                return courseData;
            })
            .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("courseId", courseId);
        response.put("courseName", course.getCourseName());
        response.put("chainLength", chain.size());
        response.put("chain", chainData);
        response.put("message", chain.size() > 1 ?
            "This course requires completion of " + (chain.size() - 1) + " prerequisite course(s)" :
            "This course has no prerequisites");

        return ResponseEntity.ok(response);
    }

    // ==================== Student History ====================

    @GetMapping("/student/{studentId}/history")
    public ResponseEntity<Map<String, Object>> getStudentHistory(@PathVariable Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);

        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Student student = studentOpt.get();
        List<StudentCourseHistory> history = prerequisiteValidationService.getStudentHistory(student);

        Map<String, Object> response = new HashMap<>();
        response.put("studentId", studentId);
        response.put("studentName", student.getFirstName() + " " + student.getLastName());
        response.put("totalCourses", history.size());
        response.put("history", history);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{studentId}/completed")
    public ResponseEntity<Map<String, Object>> getCompletedCourses(@PathVariable Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);

        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Student student = studentOpt.get();
        List<StudentCourseHistory> completed = prerequisiteValidationService.getCompletedCourses(student);

        Map<String, Object> response = new HashMap<>();
        response.put("studentId", studentId);
        response.put("studentName", student.getFirstName() + " " + student.getLastName());
        response.put("completedCount", completed.size());
        response.put("completedCourses", completed);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{studentId}/credits")
    public ResponseEntity<Map<String, Object>> getTotalCredits(@PathVariable Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);

        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Student student = studentOpt.get();
        Double totalCredits = prerequisiteValidationService.calculateTotalCredits(student);

        Map<String, Object> response = new HashMap<>();
        response.put("studentId", studentId);
        response.put("studentName", student.getFirstName() + " " + student.getLastName());
        response.put("totalCredits", totalCredits);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{studentId}/gpa")
    public ResponseEntity<Map<String, Object>> getGPA(@PathVariable Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);

        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Student student = studentOpt.get();
        Double gpa = prerequisiteValidationService.calculateGPA(student);

        Map<String, Object> response = new HashMap<>();
        response.put("studentId", studentId);
        response.put("studentName", student.getFirstName() + " " + student.getLastName());
        response.put("gpa", gpa);
        response.put("scale", "4.0");

        return ResponseEntity.ok(response);
    }

    // ==================== Course Qualification ====================

    @GetMapping("/student/{studentId}/qualified-courses")
    public ResponseEntity<Map<String, Object>> getQualifiedCourses(@PathVariable Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);

        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Student student = studentOpt.get();
        List<Course> allCourses = courseRepository.findAll();
        List<Course> qualifiedCourses = prerequisiteValidationService.getQualifiedCourses(student, allCourses);

        Map<String, Object> response = new HashMap<>();
        response.put("studentId", studentId);
        response.put("studentName", student.getFirstName() + " " + student.getLastName());
        response.put("totalAvailableCourses", allCourses.size());
        response.put("qualifiedCoursesCount", qualifiedCourses.size());
        response.put("qualifiedCourses", qualifiedCourses);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{studentId}/unqualified-courses")
    public ResponseEntity<Map<String, Object>> getUnqualifiedCourses(@PathVariable Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);

        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Student student = studentOpt.get();
        List<Course> allCourses = courseRepository.findAll();
        Map<Course, String> unqualifiedCourses = prerequisiteValidationService.getUnqualifiedCourses(student, allCourses);

        List<Map<String, Object>> unqualifiedList = new ArrayList<>();
        for (Map.Entry<Course, String> entry : unqualifiedCourses.entrySet()) {
            Map<String, Object> courseData = new HashMap<>();
            courseData.put("courseId", entry.getKey().getId());
            courseData.put("courseCode", entry.getKey().getCourseCode());
            courseData.put("courseName", entry.getKey().getCourseName());
            courseData.put("reason", entry.getValue());
            unqualifiedList.add(courseData);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("studentId", studentId);
        response.put("studentName", student.getFirstName() + " " + student.getLastName());
        response.put("unqualifiedCoursesCount", unqualifiedCourses.size());
        response.put("unqualifiedCourses", unqualifiedList);

        return ResponseEntity.ok(response);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/overview/{studentId}")
    public ResponseEntity<Map<String, Object>> getStudentPrerequisiteDashboard(@PathVariable Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);

        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Student student = studentOpt.get();
        List<Course> allCourses = courseRepository.findAll();
        List<Course> qualifiedCourses = prerequisiteValidationService.getQualifiedCourses(student, allCourses);
        List<StudentCourseHistory> completedCourses = prerequisiteValidationService.getCompletedCourses(student);
        Double totalCredits = prerequisiteValidationService.calculateTotalCredits(student);
        Double gpa = prerequisiteValidationService.calculateGPA(student);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("studentId", studentId);
        dashboard.put("studentName", student.getFirstName() + " " + student.getLastName());
        dashboard.put("completedCourses", completedCourses.size());
        dashboard.put("totalCredits", totalCredits);
        dashboard.put("gpa", gpa);
        dashboard.put("qualifiedForCourses", qualifiedCourses.size());
        dashboard.put("totalAvailableCourses", allCourses.size());
        dashboard.put("eligibilityRate", allCourses.size() > 0 ?
            (qualifiedCourses.size() * 100.0 / allCourses.size()) : 0);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/statistics")
    public ResponseEntity<Map<String, Object>> getPrerequisiteStatistics() {
        List<Course> allCourses = courseRepository.findAll();

        long coursesWithPrereqs = allCourses.stream()
            .filter(c -> !prerequisiteValidationService.getPrerequisites(c).isEmpty())
            .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCourses", allCourses.size());
        stats.put("coursesWithPrerequisites", coursesWithPrereqs);
        stats.put("coursesWithoutPrerequisites", allCourses.size() - coursesWithPrereqs);
        stats.put("prerequisitePercentage", allCourses.size() > 0 ?
            (coursesWithPrereqs * 100.0 / allCourses.size()) : 0);

        return ResponseEntity.ok(stats);
    }

    // ==================== Utility Endpoints ====================

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("version", "1.0.0");
        metadata.put("features", Map.of(
            "prerequisiteValidation", "Validate student eligibility for courses",
            "courseHistory", "Track completed courses and grades",
            "prerequisiteChains", "Show course sequences and dependencies",
            "qualificationChecking", "Find courses student can/cannot take"
        ));

        metadata.put("validationRules", Map.of(
            "groupLogic", "AND logic between groups, OR logic within groups",
            "gradeRequirements", "Minimum grade thresholds supported",
            "allowOverride", "Recommended prerequisites allow override"
        ));

        return ResponseEntity.ok(metadata);
    }
}
