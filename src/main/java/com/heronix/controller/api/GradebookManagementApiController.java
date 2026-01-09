package com.heronix.controller.api;

import com.heronix.model.domain.Assignment;
import com.heronix.model.domain.AssignmentGrade;
import com.heronix.model.domain.GradingCategory;
import com.heronix.service.GradebookService;
import com.heronix.service.GradebookService.StudentCourseGrade;
import com.heronix.service.GradebookService.ClassGradebook;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Gradebook Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/gradebook")
@RequiredArgsConstructor
public class GradebookManagementApiController {

    private final GradebookService gradebookService;

    // ==================== Grading Category Management ====================

    @PostMapping("/courses/{courseId}/categories/default")
    public ResponseEntity<List<GradingCategory>> createDefaultCategories(@PathVariable Long courseId) {
        List<GradingCategory> categories = gradebookService.createDefaultCategories(courseId);
        return ResponseEntity.status(HttpStatus.CREATED).body(categories);
    }

    @GetMapping("/courses/{courseId}/categories")
    public ResponseEntity<List<GradingCategory>> getCategoriesForCourse(@PathVariable Long courseId) {
        List<GradingCategory> categories = gradebookService.getCategoriesForCourse(courseId);
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/categories/weights")
    public ResponseEntity<Void> updateCategoryWeights(@RequestBody Map<Long, Double> categoryWeights) {
        gradebookService.updateCategoryWeights(categoryWeights);
        return ResponseEntity.ok().build();
    }

    // ==================== Assignment Management ====================

    @PostMapping("/courses/{courseId}/assignments")
    public ResponseEntity<Assignment> createAssignment(
            @PathVariable Long courseId,
            @RequestParam Long categoryId,
            @RequestParam String title,
            @RequestParam Double maxPoints,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate) {
        Assignment assignment = gradebookService.createAssignment(
                courseId, categoryId, title, maxPoints, dueDate);
        return ResponseEntity.status(HttpStatus.CREATED).body(assignment);
    }

    @GetMapping("/courses/{courseId}/assignments")
    public ResponseEntity<List<Assignment>> getAssignmentsForCourse(@PathVariable Long courseId) {
        List<Assignment> assignments = gradebookService.getAssignmentsForCourse(courseId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/categories/{categoryId}/assignments")
    public ResponseEntity<List<Assignment>> getAssignmentsByCategory(@PathVariable Long categoryId) {
        List<Assignment> assignments = gradebookService.getAssignmentsByCategory(categoryId);
        return ResponseEntity.ok(assignments);
    }

    @PatchMapping("/assignments/{assignmentId}/publish")
    public ResponseEntity<Void> publishAssignment(@PathVariable Long assignmentId) {
        gradebookService.publishAssignment(assignmentId);
        return ResponseEntity.ok().build();
    }

    // ==================== Grade Entry and Management ====================

    @PostMapping("/grades/enter")
    public ResponseEntity<AssignmentGrade> enterGrade(
            @RequestParam Long studentId,
            @RequestParam Long assignmentId,
            @RequestParam Double score,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate submittedDate,
            @RequestParam(required = false) String comments) {
        AssignmentGrade grade = gradebookService.enterGrade(studentId, assignmentId, score, submittedDate, comments);
        return ResponseEntity.status(HttpStatus.CREATED).body(grade);
    }

    @PostMapping("/assignments/{assignmentId}/grades/bulk")
    public ResponseEntity<Map<String, Integer>> bulkEnterGrades(
            @PathVariable Long assignmentId,
            @RequestBody Map<Long, Double> studentScores) {
        int gradesEntered = gradebookService.bulkEnterGrades(assignmentId, studentScores);
        Map<String, Integer> result = Map.of("gradesEntered", gradesEntered);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/grades/excuse")
    public ResponseEntity<Void> excuseGrade(
            @RequestParam Long studentId,
            @RequestParam Long assignmentId,
            @RequestParam String reason) {
        gradebookService.excuseGrade(studentId, assignmentId, reason);
        return ResponseEntity.ok().build();
    }

    // ==================== Grade Calculation and Reporting ====================

    @GetMapping("/students/{studentId}/courses/{courseId}/grade")
    public ResponseEntity<StudentCourseGrade> calculateCourseGrade(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        StudentCourseGrade grade = gradebookService.calculateCourseGrade(studentId, courseId);
        return ResponseEntity.ok(grade);
    }

    @GetMapping("/courses/{courseId}/gradebook")
    public ResponseEntity<ClassGradebook> getClassGradebook(@PathVariable Long courseId) {
        ClassGradebook gradebook = gradebookService.getClassGradebook(courseId);
        return ResponseEntity.ok(gradebook);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/course/{courseId}")
    public ResponseEntity<Map<String, Object>> getCourseDashboard(@PathVariable Long courseId) {
        ClassGradebook gradebook = gradebookService.getClassGradebook(courseId);
        List<GradingCategory> categories = gradebookService.getCategoriesForCourse(courseId);
        List<Assignment> assignments = gradebookService.getAssignmentsForCourse(courseId);

        Map<String, Object> dashboard = Map.of(
                "gradebook", gradebook,
                "categories", categories,
                "assignments", assignments,
                "totalStudents", gradebook.getStudentGrades().size(),
                "totalAssignments", assignments.size()
        );

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/student/{studentId}/course/{courseId}")
    public ResponseEntity<Map<String, Object>> getStudentDashboard(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        StudentCourseGrade courseGrade = gradebookService.calculateCourseGrade(studentId, courseId);
        List<GradingCategory> categories = gradebookService.getCategoriesForCourse(courseId);

        Map<String, Object> dashboard = Map.of(
                "courseGrade", courseGrade,
                "categories", categories,
                "finalPercentage", courseGrade.getFinalPercentage(),
                "letterGrade", courseGrade.getLetterGrade()
        );

        return ResponseEntity.ok(dashboard);
    }
}
