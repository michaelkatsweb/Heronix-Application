package com.heronix.controller.api;

import com.heronix.model.domain.Course;
import com.heronix.model.domain.Student;
import com.heronix.repository.StudentRepository;
import com.heronix.service.StudentPlacementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REST API Controller for Student Placement
 *
 * Provides AI-powered course recommendations and student placement operations.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/student-placement")
@RequiredArgsConstructor
public class StudentPlacementApiController {

    private final StudentPlacementService placementService;
    private final StudentRepository studentRepository;

    // ==================== Course Recommendations ====================

    @GetMapping("/student/{studentId}/recommend-electives")
    public ResponseEntity<List<Course>> recommendElectives(@PathVariable Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Course> recommendations = placementService.recommendElectives(studentOpt.get());
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/student/{studentId}/recommend-special-ed")
    public ResponseEntity<List<Course>> recommendSpecialEdCourses(@PathVariable Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Course> recommendations = placementService.recommendSpecialEdCourses(studentOpt.get());
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/student/{studentId}/recommend-gifted")
    public ResponseEntity<List<Course>> recommendGiftedCourses(@PathVariable Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Course> recommendations = placementService.recommendGiftedCourses(studentOpt.get());
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/student/{studentId}/recommend-intervention")
    public ResponseEntity<List<Course>> recommendInterventionCourses(@PathVariable Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Course> recommendations = placementService.recommendInterventionCourses(studentOpt.get());
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/student/{studentId}/ai-recommendations")
    public ResponseEntity<Map<String, Object>> getAIRecommendations(@PathVariable Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // getAIRecommendations returns String, so wrap it
        String aiAnalysis = placementService.getAIRecommendations(studentOpt.get());

        Map<String, Object> response = new HashMap<>();
        response.put("studentId", studentId);
        response.put("analysis", aiAnalysis);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{studentId}/all-recommendations")
    public ResponseEntity<Map<String, Object>> getAllRecommendations(@PathVariable Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Student student = studentOpt.get();
        Map<String, Object> allRecommendations = new HashMap<>();
        allRecommendations.put("electives", placementService.recommendElectives(student));
        allRecommendations.put("specialEd", placementService.recommendSpecialEdCourses(student));
        allRecommendations.put("gifted", placementService.recommendGiftedCourses(student));
        allRecommendations.put("intervention", placementService.recommendInterventionCourses(student));
        allRecommendations.put("aiAnalysis", placementService.getAIRecommendations(student));

        return ResponseEntity.ok(allRecommendations);
    }

    // ==================== Batch Operations ====================

    @PostMapping("/batch/assign-electives")
    public ResponseEntity<Map<String, Object>> batchAssignElectives(
            @RequestBody List<Long> studentIds,
            @RequestParam(defaultValue = "2") int electivesPerStudent) {

        List<Student> students = studentRepository.findAllById(studentIds);

        // batchAssignElectives returns Map<Student, List<Course>>
        Map<Student, List<Course>> serviceResults = placementService.batchAssignElectives(students, electivesPerStudent);

        // Convert to Map<Long, List<Course>> for JSON serialization
        Map<Long, List<Course>> results = serviceResults.entrySet().stream()
            .collect(Collectors.toMap(
                entry -> entry.getKey().getId(),
                Map.Entry::getValue
            ));

        Map<String, Object> response = new HashMap<>();
        response.put("totalStudents", students.size());
        response.put("electivesPerStudent", electivesPerStudent);
        response.put("results", results);
        response.put("successCount", results.size());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/batch/recommend-electives")
    public ResponseEntity<Map<String, Object>> batchRecommendElectives(
            @RequestBody List<Long> studentIds) {

        Map<String, Object> response = new HashMap<>();
        Map<Long, List<Course>> recommendations = new HashMap<>();

        for (Long studentId : studentIds) {
            Optional<Student> studentOpt = studentRepository.findById(studentId);
            if (studentOpt.isPresent()) {
                recommendations.put(studentId, placementService.recommendElectives(studentOpt.get()));
            }
        }

        response.put("totalStudents", studentIds.size());
        response.put("processedStudents", recommendations.size());
        response.put("recommendations", recommendations);

        return ResponseEntity.ok(response);
    }

    // ==================== Student Identification ====================

    @GetMapping("/advanced-placements")
    public ResponseEntity<Map<String, List<Student>>> identifyAdvancedPlacements() {
        // identifyAdvancedPlacements returns Map<String, List<Student>>
        Map<String, List<Student>> students = placementService.identifyAdvancedPlacements();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/advanced-placements/count")
    public ResponseEntity<Map<String, Object>> getAdvancedPlacementsCount() {
        Map<String, List<Student>> students = placementService.identifyAdvancedPlacements();

        long totalCount = students.values().stream()
            .mapToLong(List::size)
            .sum();

        Map<String, Object> response = new HashMap<>();
        response.put("count", totalCount);
        response.put("categories", students);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/students-needing-support")
    public ResponseEntity<List<Student>> identifyStudentsNeedingSupport() {
        List<Student> students = placementService.identifyStudentsNeedingSupport();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/students-needing-support/count")
    public ResponseEntity<Map<String, Object>> getStudentsNeedingSupportCount() {
        List<Student> students = placementService.identifyStudentsNeedingSupport();

        Map<String, Object> response = new HashMap<>();
        response.put("count", students.size());
        response.put("students", students);

        return ResponseEntity.ok(response);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getStudentPlacementDashboard(@PathVariable Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Student student = studentOpt.get();
        Map<String, Object> dashboard = new HashMap<>();

        // Student info
        dashboard.put("studentId", student.getId());
        dashboard.put("studentName", student.getFullName());
        dashboard.put("gradeLevel", student.getGradeLevel());
        dashboard.put("gpa", student.getCurrentGPA());

        // Recommendations
        dashboard.put("electiveRecommendations", placementService.recommendElectives(student));
        dashboard.put("specialEdRecommendations", placementService.recommendSpecialEdCourses(student));
        dashboard.put("giftedRecommendations", placementService.recommendGiftedCourses(student));
        dashboard.put("interventionRecommendations", placementService.recommendInterventionCourses(student));

        // AI analysis
        dashboard.put("aiAnalysis", placementService.getAIRecommendations(student));

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getPlacementOverview() {
        Map<String, Object> dashboard = new HashMap<>();

        Map<String, List<Student>> advancedPlacements = placementService.identifyAdvancedPlacements();
        List<Student> needingSupport = placementService.identifyStudentsNeedingSupport();

        long advancedCount = advancedPlacements.values().stream()
            .mapToLong(List::size)
            .sum();

        dashboard.put("advancedPlacements", Map.of(
            "count", advancedCount,
            "categories", advancedPlacements
        ));

        dashboard.put("studentsNeedingSupport", Map.of(
            "count", needingSupport.size(),
            "students", needingSupport
        ));

        dashboard.put("totalIdentified", advancedCount + needingSupport.size());

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/placement-summary")
    public ResponseEntity<Map<String, Object>> getPlacementSummary() {
        Map<String, Object> summary = new HashMap<>();

        Map<String, List<Student>> advancedPlacements = placementService.identifyAdvancedPlacements();
        List<Student> needingSupport = placementService.identifyStudentsNeedingSupport();

        long advancedCount = advancedPlacements.values().stream()
            .mapToLong(List::size)
            .sum();

        summary.put("advancedPlacementCount", advancedCount);
        summary.put("supportNeededCount", needingSupport.size());
        summary.put("totalStudentsIdentified", advancedCount + needingSupport.size());

        // Categorization breakdown
        Map<String, Long> categoryBreakdown = advancedPlacements.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> (long) entry.getValue().size()
            ));
        categoryBreakdown.put("supportNeeded", (long) needingSupport.size());

        summary.put("categories", categoryBreakdown);

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/dashboard/recommendation-stats")
    public ResponseEntity<Map<String, Object>> getRecommendationStats(
            @RequestParam(required = false) Long studentId) {

        if (studentId != null) {
            Optional<Student> studentOpt = studentRepository.findById(studentId);
            if (studentOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Student student = studentOpt.get();
            Map<String, Object> stats = new HashMap<>();
            stats.put("studentId", studentId);
            stats.put("electiveCount", placementService.recommendElectives(student).size());
            stats.put("specialEdCount", placementService.recommendSpecialEdCourses(student).size());
            stats.put("giftedCount", placementService.recommendGiftedCourses(student).size());
            stats.put("interventionCount", placementService.recommendInterventionCourses(student).size());

            return ResponseEntity.ok(stats);
        }

        // Overall stats
        Map<String, Object> stats = new HashMap<>();
        stats.put("message", "Provide studentId parameter for individual student stats");

        return ResponseEntity.ok(stats);
    }
}
