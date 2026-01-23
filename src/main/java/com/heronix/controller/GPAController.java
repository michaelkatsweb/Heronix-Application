package com.heronix.controller;

import com.heronix.model.domain.AuditLog;
import com.heronix.model.domain.Student;
import com.heronix.repository.StudentRepository;
import com.heronix.service.AuditService;
import com.heronix.service.GpaCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GPA Calculation REST API Controller
 *
 * Provides endpoints for GPA calculations and class rankings:
 * - Get cumulative GPA
 * - Get weighted GPA
 * - Get term/year GPA
 * - Calculate GPA for grade data
 * - Get class rank
 * - Get honor roll students
 *
 * Security:
 * - All endpoints require authentication
 * - Most endpoints allow ADMIN, TEACHER, or STUDENT (own data only)
 *
 * @author Heronix Development Team
 * @version 1.0.0
 * @since 2025-12-28
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/gpa")
@CrossOrigin(origins = "*")
public class GPAController {

    private final GpaCalculationService gpaService;
    private final StudentRepository studentRepository;
    private final AuditService auditService;

    @Autowired
    public GPAController(
            GpaCalculationService gpaService,
            StudentRepository studentRepository,
            AuditService auditService) {
        this.gpaService = gpaService;
        this.studentRepository = studentRepository;
        this.auditService = auditService;
    }

    // ============================================================
    // GPA Calculation Endpoints
    // ============================================================

    /**
     * GET /api/gpa/student/{studentId}/cumulative
     * Get cumulative GPA for a student
     *
     * @param studentId student ID
     * @return cumulative GPA
     */
    @GetMapping("/student/{studentId}/cumulative")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<?> getCumulativeGPA(@PathVariable Long studentId) {
        try {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            Double gpa = gpaService.calculateCumulativeGPA(student);

            auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "GPA", studentId,
                    "Retrieved cumulative GPA", true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(Map.of(
                    "studentId", studentId,
                    "studentName", student.getFullName(),
                    "cumulativeGpa", gpa != null ? gpa : 0.0,
                    "gpaScale", 4.0
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to calculate cumulative GPA: " + e.getMessage()));
        }
    }

    /**
     * GET /api/gpa/student/{studentId}/weighted
     * Get weighted GPA for a student
     *
     * @param studentId student ID
     * @return weighted GPA
     */
    @GetMapping("/student/{studentId}/weighted")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<?> getWeightedGPA(@PathVariable Long studentId) {
        try {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            Double gpa = gpaService.calculateWeightedGPA(student);

            auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "GPA", studentId,
                    "Retrieved weighted GPA", true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(Map.of(
                    "studentId", studentId,
                    "studentName", student.getFullName(),
                    "weightedGpa", gpa != null ? gpa : 0.0,
                    "gpaScale", 5.0,
                    "note", "Weighted GPA includes bonus for AP/Honors courses"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to calculate weighted GPA: " + e.getMessage()));
        }
    }

    /**
     * GET /api/gpa/student/{studentId}/term/{term}
     * Get GPA for a specific term
     *
     * @param studentId student ID
     * @param term term (e.g., "Fall 2024")
     * @return term GPA
     */
    @GetMapping("/student/{studentId}/term/{term}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<?> getTermGPA(
            @PathVariable Long studentId,
            @PathVariable String term) {
        try {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            // Parse term (e.g., "2024-2025:Fall" or "Fall 2024")
            String academicYear;
            String termName;

            if (term.contains(":")) {
                String[] parts = term.split(":");
                academicYear = parts[0];
                termName = parts[1];
            } else {
                // Attempt to parse "Fall 2024" format
                String[] parts = term.split(" ");
                if (parts.length == 2) {
                    termName = parts[0];
                    int year = Integer.parseInt(parts[1]);
                    academicYear = year + "-" + (year + 1);
                } else {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Invalid term format. Use 'YYYY-YYYY:Term' or 'Term YYYY'"));
                }
            }

            Double gpa = gpaService.calculateTermGPA(student, academicYear, termName);

            auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "GPA", studentId,
                    "Retrieved term GPA for " + term, true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(Map.of(
                    "studentId", studentId,
                    "studentName", student.getFullName(),
                    "academicYear", academicYear,
                    "term", termName,
                    "termGpa", gpa != null ? gpa : 0.0
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to calculate term GPA: " + e.getMessage()));
        }
    }

    /**
     * GET /api/gpa/student/{studentId}/year/{year}
     * Get GPA for a specific academic year
     *
     * @param studentId student ID
     * @param year academic year (e.g., "2024-2025")
     * @return year GPA
     */
    @GetMapping("/student/{studentId}/year/{year}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<?> getYearGPA(
            @PathVariable Long studentId,
            @PathVariable String year) {
        try {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            // Calculate GPA for all terms in the year
            Double fallGpa = gpaService.calculateTermGPA(student, year, "Fall");
            Double springGpa = gpaService.calculateTermGPA(student, year, "Spring");

            // Average the terms (simple average for now)
            Double yearGpa = null;
            if (fallGpa != null && springGpa != null) {
                yearGpa = (fallGpa + springGpa) / 2.0;
            } else if (fallGpa != null) {
                yearGpa = fallGpa;
            } else if (springGpa != null) {
                yearGpa = springGpa;
            }

            auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "GPA", studentId,
                    "Retrieved year GPA for " + year, true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(Map.of(
                    "studentId", studentId,
                    "studentName", student.getFullName(),
                    "academicYear", year,
                    "fallGpa", fallGpa != null ? fallGpa : 0.0,
                    "springGpa", springGpa != null ? springGpa : 0.0,
                    "yearGpa", yearGpa != null ? yearGpa : 0.0
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to calculate year GPA: " + e.getMessage()));
        }
    }

    /**
     * GET /api/gpa/student/{studentId}/all
     * Get all GPA metrics for a student
     *
     * @param studentId student ID
     * @return all GPA data
     */
    @GetMapping("/student/{studentId}/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<?> getAllGPAMetrics(@PathVariable Long studentId) {
        try {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            Double cumulativeGpa = gpaService.calculateCumulativeGPA(student);
            Double weightedGpa = gpaService.calculateWeightedGPA(student);

            auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "GPA", studentId,
                    "Retrieved all GPA metrics", true, AuditLog.AuditSeverity.INFO);

            Map<String, Object> response = new HashMap<>();
            response.put("studentId", studentId);
            response.put("studentName", student.getFullName());
            response.put("gradeLevel", student.getGradeLevel());
            response.put("cumulativeGpa", cumulativeGpa != null ? cumulativeGpa : 0.0);
            response.put("weightedGpa", weightedGpa != null ? weightedGpa : 0.0);
            response.put("currentGpa", student.getCurrentGPA() != null ? student.getCurrentGPA() : 0.0);
            response.put("unweightedGpa", student.getUnweightedGPA() != null ? student.getUnweightedGPA() : 0.0);
            response.put("academicStanding", student.getAcademicStanding());
            response.put("honorRollStatus", student.getHonorRollStatus());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve GPA metrics: " + e.getMessage()));
        }
    }

    // ============================================================
    // Class Rank Endpoints
    // ============================================================

    /**
     * GET /api/gpa/class-rank/{studentId}
     * Get class rank for a student
     *
     * @param studentId student ID
     * @return class rank
     */
    @GetMapping("/class-rank/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<?> getClassRank(@PathVariable Long studentId) {
        try {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            // Get all students in same grade level
            List<Student> gradeLevelStudents = studentRepository.findAll().stream()
                    .filter(s -> student.getGradeLevel().equals(s.getGradeLevel()))
                    .filter(Student::isActive)
                    .collect(Collectors.toList());

            // Sort by weighted GPA descending
            gradeLevelStudents.sort((s1, s2) -> {
                Double gpa1 = s1.getWeightedGPA() != null ? s1.getWeightedGPA() : 0.0;
                Double gpa2 = s2.getWeightedGPA() != null ? s2.getWeightedGPA() : 0.0;
                return Double.compare(gpa2, gpa1);
            });

            // Find rank
            int rank = 0;
            for (int i = 0; i < gradeLevelStudents.size(); i++) {
                if (gradeLevelStudents.get(i).getId().equals(studentId)) {
                    rank = i + 1;
                    break;
                }
            }

            int classSize = gradeLevelStudents.size();
            double percentile = classSize > 0 ? ((double) (classSize - rank) / classSize) * 100 : 0.0;

            auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "ClassRank", studentId,
                    "Retrieved class rank", true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(Map.of(
                    "studentId", studentId,
                    "studentName", student.getFullName(),
                    "gradeLevel", student.getGradeLevel(),
                    "rank", rank,
                    "classSize", classSize,
                    "percentile", Math.round(percentile * 100.0) / 100.0,
                    "weightedGpa", student.getWeightedGPA() != null ? student.getWeightedGPA() : 0.0
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to calculate class rank: " + e.getMessage()));
        }
    }

    // ============================================================
    // Honor Roll Endpoints
    // ============================================================

    /**
     * GET /api/gpa/honor-roll
     * Get honor roll students
     *
     * @param minGpa minimum GPA threshold (default: 3.5)
     * @return list of honor roll students
     */
    @GetMapping("/honor-roll")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getHonorRollStudents(
            @RequestParam(defaultValue = "3.5") Double minGpa) {
        try {
            List<Student> honorRollStudents = studentRepository.findAll().stream()
                    .filter(Student::isActive)
                    .filter(s -> s.getWeightedGPA() != null && s.getWeightedGPA() >= minGpa)
                    .sorted((s1, s2) -> Double.compare(
                            s2.getWeightedGPA() != null ? s2.getWeightedGPA() : 0.0,
                            s1.getWeightedGPA() != null ? s1.getWeightedGPA() : 0.0
                    ))
                    .limit(100) // Limit to top 100
                    .collect(Collectors.toList());

            List<Map<String, Object>> results = honorRollStudents.stream()
                    .map(s -> Map.of(
                            "studentId", (Object) s.getId(),
                            "studentName", s.getFullName(),
                            "gradeLevel", s.getGradeLevel(),
                            "weightedGpa", s.getWeightedGPA() != null ? s.getWeightedGPA() : 0.0,
                            "honorRollStatus", s.getHonorRollStatus() != null ? s.getHonorRollStatus() : "Honor Roll"
                    ))
                    .collect(Collectors.toList());

            auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "HonorRoll", null,
                    "Retrieved honor roll students", true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(Map.of(
                    "minGpaThreshold", minGpa,
                    "studentCount", results.size(),
                    "students", results
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve honor roll: " + e.getMessage()));
        }
    }

    // ============================================================
    // Health Check
    // ============================================================

    /**
     * GET /api/gpa/health
     * Health check endpoint
     *
     * @return health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "GPACalculationService"
        ));
    }
}
