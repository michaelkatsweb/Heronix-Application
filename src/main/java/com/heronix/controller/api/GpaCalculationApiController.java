package com.heronix.controller.api;

import com.heronix.model.domain.Student;
import com.heronix.repository.StudentRepository;
import com.heronix.service.GpaCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST API Controller for GPA Calculations
 *
 * Provides endpoints for calculating student GPAs in various ways:
 * cumulative, weighted, term-based, and simple average.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/gpa")
@RequiredArgsConstructor
public class GpaCalculationApiController {

    private final GpaCalculationService gpaCalculationService;
    private final StudentRepository studentRepository;

    // ==================== Individual Student GPA ====================

    @GetMapping("/student/{studentId}/cumulative")
    public ResponseEntity<Map<String, Object>> getCumulativeGPA(@PathVariable Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);

        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Student student = studentOpt.get();
        Double cumulativeGPA = gpaCalculationService.calculateCumulativeGPA(student);

        Map<String, Object> response = new HashMap<>();
        response.put("studentId", studentId);
        response.put("studentName", student.getFirstName() + " " + student.getLastName());
        response.put("cumulativeGPA", cumulativeGPA);
        response.put("type", "CUMULATIVE_UNWEIGHTED");
        response.put("scale", "4.0");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{studentId}/weighted")
    public ResponseEntity<Map<String, Object>> getWeightedGPA(@PathVariable Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);

        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Student student = studentOpt.get();
        Double weightedGPA = gpaCalculationService.calculateWeightedGPA(student);

        Map<String, Object> response = new HashMap<>();
        response.put("studentId", studentId);
        response.put("studentName", student.getFirstName() + " " + student.getLastName());
        response.put("weightedGPA", weightedGPA);
        response.put("type", "WEIGHTED");
        response.put("scale", "5.0 (AP/IB +1.0, Honors +0.5)");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{studentId}/term")
    public ResponseEntity<Map<String, Object>> getTermGPA(
            @PathVariable Long studentId,
            @RequestParam String academicYear,
            @RequestParam String term) {

        Optional<Student> studentOpt = studentRepository.findById(studentId);

        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Student student = studentOpt.get();
        Double termGPA = gpaCalculationService.calculateTermGPA(student, academicYear, term);

        Map<String, Object> response = new HashMap<>();
        response.put("studentId", studentId);
        response.put("studentName", student.getFirstName() + " " + student.getLastName());
        response.put("termGPA", termGPA);
        response.put("academicYear", academicYear);
        response.put("term", term);
        response.put("type", "TERM_GPA");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{studentId}/simple")
    public ResponseEntity<Map<String, Object>> getSimpleAverageGPA(@PathVariable Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);

        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Student student = studentOpt.get();
        Double simpleGPA = gpaCalculationService.calculateSimpleAverageGPA(student);

        Map<String, Object> response = new HashMap<>();
        response.put("studentId", studentId);
        response.put("studentName", student.getFirstName() + " " + student.getLastName());
        response.put("simpleAverageGPA", simpleGPA);
        response.put("type", "SIMPLE_AVERAGE");
        response.put("description", "Average GPA without credit weighting");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{studentId}/all")
    public ResponseEntity<Map<String, Object>> getAllGPAs(@PathVariable Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);

        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Student student = studentOpt.get();

        Map<String, Object> gpas = new HashMap<>();
        gpas.put("cumulative", gpaCalculationService.calculateCumulativeGPA(student));
        gpas.put("weighted", gpaCalculationService.calculateWeightedGPA(student));
        gpas.put("simpleAverage", gpaCalculationService.calculateSimpleAverageGPA(student));

        Map<String, Object> response = new HashMap<>();
        response.put("studentId", studentId);
        response.put("studentName", student.getFirstName() + " " + student.getLastName());
        response.put("gpas", gpas);
        response.put("currentGPA", student.getCurrentGPA());
        response.put("unweightedGPA", student.getUnweightedGPA());
        response.put("weightedGPA", student.getWeightedGPA());

        return ResponseEntity.ok(response);
    }

    // ==================== Update Operations ====================

    @PostMapping("/student/{studentId}/update")
    public ResponseEntity<Map<String, Object>> updateStudentGPA(@PathVariable Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);

        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Student student = studentOpt.get();
            gpaCalculationService.updateStudentGPA(student);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("message", "GPA updated successfully");
            response.put("currentGPA", student.getCurrentGPA());
            response.put("unweightedGPA", student.getUnweightedGPA());
            response.put("weightedGPA", student.getWeightedGPA());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/batch-update")
    public ResponseEntity<Map<String, Object>> batchUpdateGPAs(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Long> studentIds = (List<Long>) request.get("studentIds");

        if (studentIds == null || studentIds.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "No student IDs provided");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        int successCount = 0;
        int failureCount = 0;
        List<String> errors = new ArrayList<>();

        for (Long studentId : studentIds) {
            Optional<Student> studentOpt = studentRepository.findById(studentId);
            if (studentOpt.isPresent()) {
                try {
                    gpaCalculationService.updateStudentGPA(studentOpt.get());
                    successCount++;
                } catch (Exception e) {
                    failureCount++;
                    errors.add("Student " + studentId + ": " + e.getMessage());
                }
            } else {
                failureCount++;
                errors.add("Student " + studentId + " not found");
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", failureCount == 0);
        response.put("totalRequested", studentIds.size());
        response.put("successCount", successCount);
        response.put("failureCount", failureCount);
        if (!errors.isEmpty()) {
            response.put("errors", errors);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/update-all")
    public ResponseEntity<Map<String, Object>> updateAllStudentGPAs() {
        List<Student> allStudents = studentRepository.findAll();

        int successCount = 0;
        int failureCount = 0;

        for (Student student : allStudents) {
            try {
                gpaCalculationService.updateStudentGPA(student);
                successCount++;
            } catch (Exception e) {
                failureCount++;
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("totalStudents", allStudents.size());
        response.put("successCount", successCount);
        response.put("failureCount", failureCount);
        response.put("message", "Batch GPA update completed");

        return ResponseEntity.ok(response);
    }

    // ==================== Utility Endpoints ====================

    @GetMapping("/convert")
    public ResponseEntity<Map<String, Object>> convertLetterGrade(@RequestParam String letterGrade) {
        Double gpaPoints = gpaCalculationService.convertLetterGradeToGPA(letterGrade);

        Map<String, Object> response = new HashMap<>();
        response.put("letterGrade", letterGrade);
        response.put("gpaPoints", gpaPoints);
        response.put("scale", "4.0");
        response.put("valid", gpaPoints != null);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/grade-scale")
    public ResponseEntity<Map<String, Object>> getGradeScale() {
        Map<String, Double> gradeScale = new LinkedHashMap<>();
        gradeScale.put("A+", 4.0);
        gradeScale.put("A", 4.0);
        gradeScale.put("A-", 3.7);
        gradeScale.put("B+", 3.3);
        gradeScale.put("B", 3.0);
        gradeScale.put("B-", 2.7);
        gradeScale.put("C+", 2.3);
        gradeScale.put("C", 2.0);
        gradeScale.put("C-", 1.7);
        gradeScale.put("D+", 1.3);
        gradeScale.put("D", 1.0);
        gradeScale.put("D-", 0.7);
        gradeScale.put("F", 0.0);

        Map<String, Object> response = new HashMap<>();
        response.put("gradeScale", gradeScale);
        response.put("scale", "4.0");
        response.put("weightBonus", Map.of(
            "REGULAR", 0.0,
            "HONORS", 0.5,
            "AP", 1.0,
            "IB", 1.0
        ));

        return ResponseEntity.ok(response);
    }

    // ==================== Dashboard & Statistics ====================

    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getGPAStats() {
        List<Student> allStudents = studentRepository.findAll();

        double totalGPA = 0.0;
        int studentCount = 0;
        double highestGPA = 0.0;
        double lowestGPA = 4.0;

        for (Student student : allStudents) {
            Double gpa = gpaCalculationService.calculateCumulativeGPA(student);
            if (gpa != null && gpa > 0) {
                totalGPA += gpa;
                studentCount++;
                highestGPA = Math.max(highestGPA, gpa);
                lowestGPA = Math.min(lowestGPA, gpa);
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudents", allStudents.size());
        stats.put("studentsWithGrades", studentCount);
        stats.put("averageGPA", studentCount > 0 ? totalGPA / studentCount : 0.0);
        stats.put("highestGPA", highestGPA);
        stats.put("lowestGPA", studentCount > 0 ? lowestGPA : 0.0);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/dashboard/distribution")
    public ResponseEntity<Map<String, Object>> getGPADistribution() {
        List<Student> allStudents = studentRepository.findAll();

        Map<String, Integer> distribution = new LinkedHashMap<>();
        distribution.put("4.0 (A)", 0);
        distribution.put("3.0-3.9 (B)", 0);
        distribution.put("2.0-2.9 (C)", 0);
        distribution.put("1.0-1.9 (D)", 0);
        distribution.put("0.0-0.9 (F)", 0);

        for (Student student : allStudents) {
            Double gpa = gpaCalculationService.calculateCumulativeGPA(student);
            if (gpa != null) {
                if (gpa >= 4.0) {
                    distribution.put("4.0 (A)", distribution.get("4.0 (A)") + 1);
                } else if (gpa >= 3.0) {
                    distribution.put("3.0-3.9 (B)", distribution.get("3.0-3.9 (B)") + 1);
                } else if (gpa >= 2.0) {
                    distribution.put("2.0-2.9 (C)", distribution.get("2.0-2.9 (C)") + 1);
                } else if (gpa >= 1.0) {
                    distribution.put("1.0-1.9 (D)", distribution.get("1.0-1.9 (D)") + 1);
                } else {
                    distribution.put("0.0-0.9 (F)", distribution.get("0.0-0.9 (F)") + 1);
                }
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("distribution", distribution);
        response.put("totalStudents", allStudents.size());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard/honors-roll")
    public ResponseEntity<Map<String, Object>> getHonorsRoll(
            @RequestParam(required = false, defaultValue = "3.5") Double threshold) {

        List<Student> allStudents = studentRepository.findAll();
        List<Map<String, Object>> honorsStudents = new ArrayList<>();

        for (Student student : allStudents) {
            Double gpa = gpaCalculationService.calculateCumulativeGPA(student);
            if (gpa != null && gpa >= threshold) {
                Map<String, Object> studentData = new HashMap<>();
                studentData.put("studentId", student.getId());
                studentData.put("studentName", student.getFirstName() + " " + student.getLastName());
                studentData.put("gpa", gpa);
                studentData.put("gradeLevel", student.getGradeLevel());
                honorsStudents.add(studentData);
            }
        }

        // Sort by GPA descending
        honorsStudents.sort((a, b) ->
            Double.compare((Double) b.get("gpa"), (Double) a.get("gpa")));

        Map<String, Object> response = new HashMap<>();
        response.put("threshold", threshold);
        response.put("count", honorsStudents.size());
        response.put("students", honorsStudents);

        return ResponseEntity.ok(response);
    }
}
