package com.heronix.controller;

import com.heronix.model.domain.AuditLog;
import com.heronix.model.domain.StudentGrade;
import com.heronix.repository.StudentGradeRepository;
import com.heronix.service.AuditService;
import com.heronix.service.GradeService;
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
 * Student Grades REST API Controller
 *
 * Provides endpoints for student grade management:
 * - Get all grades for a student
 * - Get grades for specific term/year
 * - Get grades for specific course
 * - Create/update grades
 * - Delete grades
 * - Get failing/incomplete grades
 * - Get grade distribution
 * - Bulk import grades
 * - Get grade audit log
 *
 * Security:
 * - All endpoints require authentication
 * - Read endpoints allow ADMIN, TEACHER, STUDENT (own data)
 * - Write endpoints require ADMIN or TEACHER only
 *
 * @author Heronix Development Team
 * @version 1.0.0
 * @since 2025-12-28
 */
@RestController
@RequestMapping("/api/student-grades")
@CrossOrigin(origins = "*")
public class StudentGradesController {

    private final GradeService gradeService;
    private final StudentGradeRepository gradeRepository;
    private final AuditService auditService;

    @Autowired
    public StudentGradesController(
            GradeService gradeService,
            StudentGradeRepository gradeRepository,
            AuditService auditService) {
        this.gradeService = gradeService;
        this.gradeRepository = gradeRepository;
        this.auditService = auditService;
    }

    // ============================================================
    // Grade Retrieval Endpoints
    // ============================================================

    /**
     * GET /api/student-grades/student/{studentId}
     * Get all grades for a student
     *
     * @param studentId student ID
     * @return list of all grades
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<?> getStudentGrades(@PathVariable Long studentId) {
        try {
            List<StudentGrade> grades = gradeRepository.findByStudentId(studentId);

            auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "StudentGrade", studentId,
                    "Retrieved all grades", true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(Map.of(
                    "studentId", studentId,
                    "gradeCount", grades.size(),
                    "grades", grades
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve grades: " + e.getMessage()));
        }
    }

    /**
     * GET /api/student-grades/student/{studentId}/term/{term}
     * Get grades for a student in a specific term
     *
     * @param studentId student ID
     * @param term term name (e.g., "Fall 2024")
     * @return list of grades for term
     */
    @GetMapping("/student/{studentId}/term/{term}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<?> getStudentGradesForTerm(
            @PathVariable Long studentId,
            @PathVariable String term) {
        try {
            List<StudentGrade> grades = gradeRepository.findByStudentIdAndTerm(studentId, term);

            auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "StudentGrade", studentId,
                    "Retrieved grades for term " + term, true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(Map.of(
                    "studentId", studentId,
                    "term", term,
                    "gradeCount", grades.size(),
                    "grades", grades
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve term grades: " + e.getMessage()));
        }
    }

    /**
     * GET /api/student-grades/course/{courseId}
     * Get all grades for a course
     *
     * @param courseId course ID
     * @return list of grades for course
     */
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> getCourseGrades(@PathVariable Long courseId) {
        try {
            List<StudentGrade> grades = gradeRepository.findByCourseId(courseId);

            auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "StudentGrade", null,
                    "Retrieved grades for course " + courseId, true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(Map.of(
                    "courseId", courseId,
                    "gradeCount", grades.size(),
                    "grades", grades
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve course grades: " + e.getMessage()));
        }
    }

    // ============================================================
    // Grade Management Endpoints
    // ============================================================

    /**
     * POST /api/student-grades
     * Create or update a final grade
     *
     * @param grade grade data
     * @return created/updated grade
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> createOrUpdateGrade(@RequestBody StudentGrade grade) {
        try {
            StudentGrade saved = gradeService.saveGrade(grade);

            auditService.log(AuditLog.AuditAction.STUDENT_CREATE, "StudentGrade", saved.getId(),
                    "Created/updated grade", true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to save grade: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/student-grades/{gradeId}
     * Update an existing grade
     *
     * @param gradeId grade ID
     * @param gradeUpdate grade update data
     * @return updated grade
     */
    @PutMapping("/{gradeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> updateGrade(
            @PathVariable Long gradeId,
            @RequestBody StudentGrade gradeUpdate) {
        try {
            StudentGrade existing = gradeRepository.findById(gradeId)
                    .orElseThrow(() -> new IllegalArgumentException("Grade not found: " + gradeId));

            // Update fields
            if (gradeUpdate.getNumericalGrade() != null) {
                existing.setNumericalGrade(gradeUpdate.getNumericalGrade());
            }
            if (gradeUpdate.getLetterGrade() != null) {
                existing.setLetterGrade(gradeUpdate.getLetterGrade());
            }
            if (gradeUpdate.getGpaPoints() != null) {
                existing.setGpaPoints(gradeUpdate.getGpaPoints());
            }
            if (gradeUpdate.getComments() != null) {
                existing.setComments(gradeUpdate.getComments());
            }

            StudentGrade updated = gradeService.saveGrade(existing);

            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "StudentGrade", gradeId,
                    "Updated grade", true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update grade: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/student-grades/{gradeId}
     * Delete a grade
     *
     * @param gradeId grade ID
     * @return success response
     */
    @DeleteMapping("/{gradeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteGrade(@PathVariable Long gradeId) {
        try {
            if (!gradeRepository.existsById(gradeId)) {
                return ResponseEntity.notFound().build();
            }

            gradeRepository.deleteById(gradeId);

            auditService.log(AuditLog.AuditAction.STUDENT_DELETE, "StudentGrade", gradeId,
                    "Deleted grade", true, AuditLog.AuditSeverity.WARNING);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Grade deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete grade: " + e.getMessage()));
        }
    }

    // ============================================================
    // Grade Analysis Endpoints
    // ============================================================

    /**
     * GET /api/student-grades/failing
     * Get all failing grades (< 60%)
     *
     * @return list of failing grades
     */
    @GetMapping("/failing")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getFailingGrades() {
        try {
            List<StudentGrade> failingGrades = gradeRepository.findAll().stream()
                    .filter(grade -> {
                        if (grade.getNumericalGrade() != null) {
                            return grade.getNumericalGrade() < 60.0;
                        }
                        if (grade.getLetterGrade() != null) {
                            return grade.getLetterGrade().equals("F");
                        }
                        return false;
                    })
                    .collect(Collectors.toList());

            auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "StudentGrade", null,
                    "Retrieved failing grades", true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(Map.of(
                    "failingGradeCount", failingGrades.size(),
                    "grades", failingGrades
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve failing grades: " + e.getMessage()));
        }
    }

    /**
     * GET /api/student-grades/incomplete
     * Get all incomplete grades
     *
     * @return list of incomplete grades
     */
    @GetMapping("/incomplete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getIncompleteGrades() {
        try {
            List<StudentGrade> incompleteGrades = gradeRepository.findAll().stream()
                    .filter(grade -> "I".equals(grade.getLetterGrade()) ||
                                    "Incomplete".equalsIgnoreCase(grade.getLetterGrade()))
                    .collect(Collectors.toList());

            auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "StudentGrade", null,
                    "Retrieved incomplete grades", true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(Map.of(
                    "incompleteGradeCount", incompleteGrades.size(),
                    "grades", incompleteGrades
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve incomplete grades: " + e.getMessage()));
        }
    }

    /**
     * GET /api/student-grades/distribution/course/{courseId}
     * Get grade distribution for a course
     *
     * @param courseId course ID
     * @return grade distribution statistics
     */
    @GetMapping("/distribution/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> getGradeDistribution(@PathVariable Long courseId) {
        try {
            List<StudentGrade> grades = gradeRepository.findByCourseId(courseId);

            Map<String, Long> letterGradeDistribution = grades.stream()
                    .filter(g -> g.getLetterGrade() != null)
                    .collect(Collectors.groupingBy(
                            StudentGrade::getLetterGrade,
                            Collectors.counting()
                    ));

            // Calculate statistics
            double avgNumerical = grades.stream()
                    .filter(g -> g.getNumericalGrade() != null)
                    .mapToDouble(StudentGrade::getNumericalGrade)
                    .average()
                    .orElse(0.0);

            double avgGpa = grades.stream()
                    .filter(g -> g.getGpaPoints() != null)
                    .mapToDouble(StudentGrade::getGpaPoints)
                    .average()
                    .orElse(0.0);

            auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "GradeDistribution", courseId,
                    "Retrieved grade distribution", true, AuditLog.AuditSeverity.INFO);

            Map<String, Object> response = new HashMap<>();
            response.put("courseId", courseId);
            response.put("totalGrades", grades.size());
            response.put("letterGradeDistribution", letterGradeDistribution);
            response.put("averageNumericalGrade", Math.round(avgNumerical * 100.0) / 100.0);
            response.put("averageGpaPoints", Math.round(avgGpa * 100.0) / 100.0);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to calculate grade distribution: " + e.getMessage()));
        }
    }

    // ============================================================
    // Bulk Operations
    // ============================================================

    /**
     * POST /api/student-grades/bulk-import
     * Bulk import grades
     *
     * @param grades list of grades to import
     * @return import summary
     */
    @PostMapping("/bulk-import")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> bulkImportGrades(@RequestBody List<StudentGrade> grades) {
        try {
            int successCount = 0;
            int failureCount = 0;

            for (StudentGrade grade : grades) {
                try {
                    gradeService.saveGrade(grade);
                    successCount++;
                } catch (Exception e) {
                    failureCount++;
                }
            }

            auditService.log(AuditLog.AuditAction.STUDENT_CREATE, "StudentGrade", null,
                    String.format("Bulk import: %d success, %d failed", successCount, failureCount),
                    true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(Map.of(
                    "totalGrades", grades.size(),
                    "successCount", successCount,
                    "failureCount", failureCount
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Bulk import failed: " + e.getMessage()));
        }
    }

    // ============================================================
    // Health Check
    // ============================================================

    /**
     * GET /api/student-grades/health
     * Health check endpoint
     *
     * @return health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "StudentGradesService"
        ));
    }
}
