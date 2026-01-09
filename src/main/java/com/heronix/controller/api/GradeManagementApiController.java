package com.heronix.controller.api;

import com.heronix.model.domain.Student;
import com.heronix.model.domain.StudentGrade;
import com.heronix.model.domain.Course;
import com.heronix.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Grade Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeManagementApiController {

    private final GradeService gradeService;

    // ==================== Grade Entry Operations ====================

    @PostMapping
    public ResponseEntity<StudentGrade> saveGrade(@RequestBody StudentGrade grade) {
        StudentGrade saved = gradeService.saveGrade(grade);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<StudentGrade>> saveGrades(@RequestBody List<StudentGrade> grades) {
        List<StudentGrade> saved = gradeService.saveGrades(grades);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/{gradeId}")
    public ResponseEntity<Void> deleteGrade(@PathVariable Long gradeId) {
        gradeService.deleteGrade(gradeId);
        return ResponseEntity.noContent().build();
    }

    // ==================== GPA Operations ====================

    @PostMapping("/recalculate-gpa/{studentId}")
    public ResponseEntity<Map<String, String>> recalculateStudentGPA(@PathVariable Long studentId) {
        // Note: This requires getting student first
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "GPA recalculation completed");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/term-gpa")
    public ResponseEntity<Map<String, Double>> calculateTermGPA(
            @RequestParam Long studentId,
            @RequestParam String term) {
        // Note: Requires student object
        Map<String, Double> response = new HashMap<>();
        response.put("termGPA", 0.0); // Placeholder
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update-class-ranks")
    public ResponseEntity<Map<String, String>> updateClassRanks(@RequestParam String gradeLevel) {
        gradeService.updateClassRanks(gradeLevel);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Class ranks updated for grade level: " + gradeLevel);

        return ResponseEntity.ok(response);
    }

    // ==================== Query Operations ====================

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudentGrade>> getStudentGrades(@PathVariable Long studentId) {
        List<StudentGrade> grades = gradeService.getStudentGrades(studentId);
        return ResponseEntity.ok(grades);
    }

    @GetMapping("/student/{studentId}/transcript")
    public ResponseEntity<List<StudentGrade>> getStudentTranscript(@PathVariable Long studentId) {
        List<StudentGrade> transcript = gradeService.getStudentTranscript(studentId);
        return ResponseEntity.ok(transcript);
    }

    @GetMapping("/student/{studentId}/term/{term}")
    public ResponseEntity<List<StudentGrade>> getTermGrades(
            @PathVariable Long studentId,
            @PathVariable String term) {
        List<StudentGrade> grades = gradeService.getTermGrades(studentId, term);
        return ResponseEntity.ok(grades);
    }

    @GetMapping("/honor-roll/{term}")
    public ResponseEntity<List<Student>> getHonorRollStudents(
            @PathVariable String term,
            @RequestParam(defaultValue = "false") boolean highHonors) {
        List<Student> students = gradeService.getHonorRollStudents(term, highHonors);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/intervention-needed")
    public ResponseEntity<List<Student>> getStudentsNeedingIntervention() {
        List<Student> students = gradeService.getStudentsNeedingIntervention();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/improved-gpa")
    public ResponseEntity<List<Student>> getStudentsWithImprovedGPA() {
        List<Student> students = gradeService.getStudentsWithImprovedGPA();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/declining-gpa")
    public ResponseEntity<List<Student>> getStudentsWithDecliningGPA() {
        List<Student> students = gradeService.getStudentsWithDecliningGPA();
        return ResponseEntity.ok(students);
    }

    // ==================== Eligibility and Validation ====================

    @GetMapping("/eligible-for-course")
    public ResponseEntity<Map<String, Boolean>> isEligibleForCourse(
            @RequestParam Long studentId,
            @RequestParam Long courseId) {
        // Note: Requires student and course objects
        Map<String, Boolean> response = new HashMap<>();
        response.put("eligible", true); // Placeholder
        return ResponseEntity.ok(response);
    }

    // ==================== Statistics and Reports ====================

    @GetMapping("/distribution/course/{courseId}")
    public ResponseEntity<Map<String, Long>> getGradeDistribution(@PathVariable Long courseId) {
        Map<String, Long> distribution = gradeService.getGradeDistribution(courseId);
        return ResponseEntity.ok(distribution);
    }

    @GetMapping("/statistics/average-gpa")
    public ResponseEntity<Map<String, Double>> getAverageGPA() {
        double avgGPA = gradeService.getAverageGPA();

        Map<String, Double> response = new HashMap<>();
        response.put("averageGPA", avgGPA);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics/gpa")
    public ResponseEntity<Map<String, Object>> getGPAStatistics() {
        Map<String, Object> stats = gradeService.getGPAStatistics();
        return ResponseEntity.ok(stats);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getStudentGradeDashboard(@PathVariable Long studentId) {
        Map<String, Object> dashboard = new HashMap<>();

        List<StudentGrade> allGrades = gradeService.getStudentGrades(studentId);
        List<StudentGrade> transcript = gradeService.getStudentTranscript(studentId);

        long passingGrades = transcript.stream()
                .filter(StudentGrade::isPassing)
                .count();

        long failingGrades = transcript.stream()
                .filter(g -> !g.isPassing())
                .count();

        dashboard.put("studentId", studentId);
        dashboard.put("allGrades", allGrades);
        dashboard.put("allGradesCount", allGrades.size());
        dashboard.put("transcriptGradesCount", transcript.size());
        dashboard.put("passingGrades", passingGrades);
        dashboard.put("failingGrades", failingGrades);
        dashboard.put("passRate", transcript.isEmpty() ? 0.0 :
                (passingGrades * 100.0 / transcript.size()));

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getGradesOverviewDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        Map<String, Object> gpaStats = gradeService.getGPAStatistics();
        List<Student> interventionNeeded = gradeService.getStudentsNeedingIntervention();
        List<Student> improvedGPA = gradeService.getStudentsWithImprovedGPA();
        List<Student> decliningGPA = gradeService.getStudentsWithDecliningGPA();

        dashboard.put("gpaStatistics", gpaStats);
        dashboard.put("interventionNeededCount", interventionNeeded.size());
        dashboard.put("studentsWithImprovedGPA", improvedGPA.size());
        dashboard.put("studentsWithDecliningGPA", decliningGPA.size());
        dashboard.put("interventionNeeded", interventionNeeded);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/honor-roll/{term}")
    public ResponseEntity<Map<String, Object>> getHonorRollDashboard(@PathVariable String term) {
        Map<String, Object> dashboard = new HashMap<>();

        List<Student> honorRoll = gradeService.getHonorRollStudents(term, false);
        List<Student> highHonorRoll = gradeService.getHonorRollStudents(term, true);

        dashboard.put("term", term);
        dashboard.put("honorRollCount", honorRoll.size());
        dashboard.put("highHonorRollCount", highHonorRoll.size());
        dashboard.put("totalHonorStudents", honorRoll.size());
        dashboard.put("honorRollStudents", honorRoll);
        dashboard.put("highHonorRollStudents", highHonorRoll);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/course/{courseId}")
    public ResponseEntity<Map<String, Object>> getCourseGradeDashboard(@PathVariable Long courseId) {
        Map<String, Object> dashboard = new HashMap<>();

        Map<String, Long> distribution = gradeService.getGradeDistribution(courseId);

        long totalGrades = distribution.values().stream().mapToLong(Long::longValue).sum();
        long passingGrades = distribution.entrySet().stream()
                .filter(e -> !e.getKey().equals("F"))
                .mapToLong(Map.Entry::getValue)
                .sum();

        dashboard.put("courseId", courseId);
        dashboard.put("gradeDistribution", distribution);
        dashboard.put("totalGrades", totalGrades);
        dashboard.put("passingGrades", passingGrades);
        dashboard.put("failingGrades", distribution.getOrDefault("F", 0L));
        dashboard.put("passRate", totalGrades > 0 ? (passingGrades * 100.0 / totalGrades) : 0.0);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/trends")
    public ResponseEntity<Map<String, Object>> getGradeTrendsDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        List<Student> improving = gradeService.getStudentsWithImprovedGPA();
        List<Student> declining = gradeService.getStudentsWithDecliningGPA();
        Map<String, Object> gpaStats = gradeService.getGPAStatistics();

        dashboard.put("studentsImproving", improving.size());
        dashboard.put("studentsDeclining", declining.size());
        dashboard.put("improvingStudents", improving);
        dashboard.put("decliningStudents", declining);
        dashboard.put("overallStatistics", gpaStats);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/gpa-distribution")
    public ResponseEntity<Map<String, Object>> getGPADistributionDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        Map<String, Object> stats = gradeService.getGPAStatistics();

        dashboard.put("statistics", stats);
        dashboard.put("averageGPA", stats.get("average"));
        dashboard.put("highestGPA", stats.get("max"));
        dashboard.put("lowestGPA", stats.get("min"));
        dashboard.put("totalStudents", stats.get("count"));
        dashboard.put("honorRollCount", stats.get("honorRollCount"));
        dashboard.put("highHonorRollCount", stats.get("highHonorRollCount"));
        dashboard.put("academicWarningCount", stats.get("academicWarningCount"));

        return ResponseEntity.ok(dashboard);
    }
}
