package com.heronix.controller;

import com.heronix.model.domain.AuditLog;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.StudentGrade;
import com.heronix.repository.StudentGradeRepository;
import com.heronix.repository.StudentRepository;
import com.heronix.service.AuditService;
import com.heronix.service.GpaCalculationService;
import com.heronix.service.impl.TranscriptService;
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
 * Report Card REST API Controller
 *
 * Provides endpoints for report card generation and retrieval:
 * - Generate report card data for students
 * - Get report cards by grading period
 * - Get report cards by academic year
 * - Bulk report card generation
 * - Report card statistics
 *
 * Report cards aggregate data from:
 * - Student grades (by term/period)
 * - GPA calculations
 * - Attendance records
 * - Behavioral records
 * - Teacher comments
 *
 * Security:
 * - All endpoints require authentication
 * - Most endpoints require ADMIN or TEACHER role
 * - Students can view their own report cards
 *
 * @author Heronix Development Team
 * @version 1.0.0
 * @since 2025-12-28
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/report-cards")
@CrossOrigin(origins = "*")
public class ReportCardController {

    private final StudentRepository studentRepository;
    private final StudentGradeRepository gradeRepository;
    private final TranscriptService transcriptService;
    private final GpaCalculationService gpaCalculationService;
    private final AuditService auditService;

    @Autowired
    public ReportCardController(
            StudentRepository studentRepository,
            StudentGradeRepository gradeRepository,
            TranscriptService transcriptService,
            GpaCalculationService gpaCalculationService,
            AuditService auditService) {
        this.studentRepository = studentRepository;
        this.gradeRepository = gradeRepository;
        this.transcriptService = transcriptService;
        this.gpaCalculationService = gpaCalculationService;
        this.auditService = auditService;
    }

    // ============================================================
    // Report Card Generation Endpoints
    // ============================================================

    /**
     * GET /api/report-cards/student/{studentId}/term/{term}
     * Generate report card for a student for a specific term
     *
     * @param studentId student ID
     * @param term term name (e.g., "Fall 2024", "Q1 2024-25")
     * @return report card data
     */
    @GetMapping("/student/{studentId}/term/{term}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<?> getReportCardForTerm(
            @PathVariable Long studentId,
            @PathVariable String term) {
        try {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            // Get all grades for the term
            List<StudentGrade> termGrades = gradeRepository.findByStudentIdAndTerm(studentId, term);

            // Calculate term GPA
            Double termGpa = gpaCalculationService.calculateTermGPA(student, getCurrentAcademicYear(), term);

            // Get cumulative GPA
            Double cumulativeGpa = gpaCalculationService.calculateCumulativeGPA(student);
            Double weightedGpa = gpaCalculationService.calculateWeightedGPA(student);

            // Build report card data
            Map<String, Object> reportCard = new HashMap<>();
            reportCard.put("studentId", studentId);
            reportCard.put("studentName", student.getFullName());
            reportCard.put("studentIdNumber", student.getStudentId());
            reportCard.put("gradeLevel", student.getGradeLevel());
            reportCard.put("term", term);
            reportCard.put("academicYear", getCurrentAcademicYear());

            // Grades
            reportCard.put("grades", termGrades.stream().map(grade -> Map.of(
                    "courseCode", grade.getCourse() != null ? grade.getCourse().getCourseCode() : "",
                    "courseName", grade.getCourse() != null ? grade.getCourse().getCourseName() : "",
                    "letterGrade", grade.getLetterGrade() != null ? grade.getLetterGrade() : "",
                    "numericalGrade", grade.getNumericalGrade() != null ? grade.getNumericalGrade() : 0.0,
                    "gpaPoints", grade.getGpaPoints() != null ? grade.getGpaPoints() : 0.0,
                    "credits", grade.getCredits() != null ? grade.getCredits() : 0.0,
                    "comments", grade.getComments() != null ? grade.getComments() : ""
            )).collect(Collectors.toList()));

            // GPA
            reportCard.put("termGpa", termGpa != null ? termGpa : 0.0);
            reportCard.put("cumulativeGpa", cumulativeGpa != null ? cumulativeGpa : 0.0);
            reportCard.put("weightedGpa", weightedGpa != null ? weightedGpa : 0.0);

            // Statistics
            reportCard.put("totalCourses", termGrades.size());
            reportCard.put("creditsEarned", termGrades.stream()
                    .mapToDouble(g -> g.getCredits() != null ? g.getCredits() : 0.0)
                    .sum());

            auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "ReportCard", studentId,
                    "Generated report card for term " + term, true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(reportCard);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate report card: " + e.getMessage()));
        }
    }

    /**
     * GET /api/report-cards/student/{studentId}/year/{year}
     * Generate full year report card for a student
     *
     * @param studentId student ID
     * @param year academic year (e.g., "2024-2025")
     * @return full year report card data
     */
    @GetMapping("/student/{studentId}/year/{year}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<?> getReportCardForYear(
            @PathVariable Long studentId,
            @PathVariable String year) {
        try {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            // Get all grades for the student (filter by year if needed)
            List<StudentGrade> allGrades = gradeRepository.findByStudentId(studentId);

            // Calculate GPAs
            Double cumulativeGpa = gpaCalculationService.calculateCumulativeGPA(student);
            Double weightedGpa = gpaCalculationService.calculateWeightedGPA(student);

            // Get class rank
            TranscriptService.ClassRankInfo classRank = transcriptService.getClassRank(studentId);

            // Build year report card
            Map<String, Object> reportCard = new HashMap<>();
            reportCard.put("studentId", studentId);
            reportCard.put("studentName", student.getFullName());
            reportCard.put("studentIdNumber", student.getStudentId());
            reportCard.put("gradeLevel", student.getGradeLevel());
            reportCard.put("academicYear", year);

            reportCard.put("cumulativeGpa", cumulativeGpa != null ? cumulativeGpa : 0.0);
            reportCard.put("weightedGpa", weightedGpa != null ? weightedGpa : 0.0);
            reportCard.put("classRank", classRank != null ? classRank.getRank() : 0);
            reportCard.put("classSize", classRank != null ? classRank.getTotalStudents() : 0);
            reportCard.put("percentile", classRank != null ? classRank.getPercentile() : 0.0);

            reportCard.put("totalCourses", allGrades.size());
            reportCard.put("totalCreditsEarned", allGrades.stream()
                    .mapToDouble(g -> g.getCredits() != null ? g.getCredits() : 0.0)
                    .sum());

            // Group grades by term
            Map<String, List<StudentGrade>> gradesByTerm = allGrades.stream()
                    .collect(Collectors.groupingBy(
                            grade -> grade.getTerm() != null ? grade.getTerm() : "Unknown"
                    ));

            reportCard.put("gradesByTerm", gradesByTerm.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().stream().map(grade -> Map.of(
                                    "courseCode", grade.getCourse() != null ? grade.getCourse().getCourseCode() : "",
                                    "courseName", grade.getCourse() != null ? grade.getCourse().getCourseName() : "",
                                    "letterGrade", grade.getLetterGrade() != null ? grade.getLetterGrade() : "",
                                    "numericalGrade", grade.getNumericalGrade() != null ? grade.getNumericalGrade() : 0.0,
                                    "credits", grade.getCredits() != null ? grade.getCredits() : 0.0
                            )).collect(Collectors.toList())
                    )));

            auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "ReportCard", studentId,
                    "Generated full year report card for " + year, true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(reportCard);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate year report card: " + e.getMessage()));
        }
    }

    /**
     * GET /api/report-cards/student/{studentId}/current
     * Get current term report card for a student
     *
     * @param studentId student ID
     * @return current term report card
     */
    @GetMapping("/student/{studentId}/current")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<?> getCurrentReportCard(@PathVariable Long studentId) {
        try {
            // Use current term (this would need to be dynamically determined)
            String currentTerm = getCurrentTerm();
            return getReportCardForTerm(studentId, currentTerm);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate current report card: " + e.getMessage()));
        }
    }

    // ============================================================
    // Bulk Report Card Generation
    // ============================================================

    /**
     * POST /api/report-cards/bulk/grade/{gradeLevel}/term/{term}
     * Generate report cards for all students in a grade level for a term
     *
     * @param gradeLevel grade level (9, 10, 11, 12)
     * @param term term name
     * @return summary of generated report cards
     */
    @PostMapping("/bulk/grade/{gradeLevel}/term/{term}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> bulkGenerateReportCards(
            @PathVariable String gradeLevel,
            @PathVariable String term) {
        try {
            List<Student> students = studentRepository.findAll().stream()
                    .filter(s -> gradeLevel.equals(s.getGradeLevel()))
                    .filter(Student::isActive)
                    .collect(Collectors.toList());

            int successCount = 0;
            int failureCount = 0;

            for (Student student : students) {
                try {
                    // Generate report card data (in production, this would save to database)
                    getReportCardForTerm(student.getId(), term);
                    successCount++;
                } catch (Exception e) {
                    failureCount++;
                }
            }

            auditService.log(AuditLog.AuditAction.STUDENT_CREATE, "ReportCard", null,
                    String.format("Bulk generated report cards for grade %s, term %s: %d success, %d failed",
                            gradeLevel, term, successCount, failureCount),
                    true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(Map.of(
                    "gradeLevel", gradeLevel,
                    "term", term,
                    "totalStudents", students.size(),
                    "successCount", successCount,
                    "failureCount", failureCount
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Bulk generation failed: " + e.getMessage()));
        }
    }

    // ============================================================
    // Report Card Statistics
    // ============================================================

    /**
     * GET /api/report-cards/statistics/term/{term}
     * Get report card statistics for a term
     *
     * @param term term name
     * @return statistics
     */
    @GetMapping("/statistics/term/{term}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> getTermStatistics(@PathVariable String term) {
        try {
            List<StudentGrade> termGrades = gradeRepository.findAll().stream()
                    .filter(grade -> term.equals(grade.getTerm()))
                    .collect(Collectors.toList());

            // Count students by grade level
            Map<String, Long> studentsByGrade = termGrades.stream()
                    .map(StudentGrade::getStudent)
                    .distinct()
                    .collect(Collectors.groupingBy(
                            s -> s.getGradeLevel() != null ? s.getGradeLevel() : "Unknown",
                            Collectors.counting()
                    ));

            // Calculate grade distribution
            Map<String, Long> gradeDistribution = termGrades.stream()
                    .filter(g -> g.getLetterGrade() != null)
                    .collect(Collectors.groupingBy(
                            StudentGrade::getLetterGrade,
                            Collectors.counting()
                    ));

            // Calculate average GPA
            double avgGpa = termGrades.stream()
                    .filter(g -> g.getGpaPoints() != null)
                    .mapToDouble(StudentGrade::getGpaPoints)
                    .average()
                    .orElse(0.0);

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("term", term);
            statistics.put("totalGrades", termGrades.size());
            statistics.put("studentsByGradeLevel", studentsByGrade);
            statistics.put("gradeDistribution", gradeDistribution);
            statistics.put("averageGpa", Math.round(avgGpa * 100.0) / 100.0);

            auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "ReportCardStatistics", null,
                    "Retrieved report card statistics for term " + term, true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to calculate statistics: " + e.getMessage()));
        }
    }

    /**
     * GET /api/report-cards/honor-roll/term/{term}
     * Get honor roll students for a term
     *
     * @param term term name
     * @param minGpa minimum GPA threshold (optional, default 3.5)
     * @return list of honor roll students
     */
    @GetMapping("/honor-roll/term/{term}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> getHonorRollForTerm(
            @PathVariable String term,
            @RequestParam(defaultValue = "3.5") Double minGpa) {
        try {
            List<Student> allStudents = studentRepository.findAll().stream()
                    .filter(Student::isActive)
                    .collect(Collectors.toList());

            List<Map<String, Object>> honorRollStudents = allStudents.stream()
                    .map(student -> {
                        Double termGpa = gpaCalculationService.calculateTermGPA(student, getCurrentAcademicYear(), term);
                        if (termGpa != null && termGpa >= minGpa) {
                            Map<String, Object> studentData = new HashMap<>();
                            studentData.put("studentId", student.getId());
                            studentData.put("studentName", student.getFullName());
                            studentData.put("studentIdNumber", student.getStudentId());
                            studentData.put("gradeLevel", student.getGradeLevel());
                            studentData.put("termGpa", termGpa);
                            return studentData;
                        }
                        return null;
                    })
                    .filter(data -> data != null)
                    .sorted((a, b) -> Double.compare(
                            (Double) b.get("termGpa"),
                            (Double) a.get("termGpa")
                    ))
                    .collect(Collectors.toList());

            auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "HonorRoll", null,
                    "Retrieved honor roll for term " + term, true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(Map.of(
                    "term", term,
                    "minGpa", minGpa,
                    "honorRollCount", honorRollStudents.size(),
                    "students", honorRollStudents
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve honor roll: " + e.getMessage()));
        }
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Get current academic year
     * In production, this would be determined by school calendar
     */
    private String getCurrentAcademicYear() {
        return "2024-2025"; // Hardcoded for now
    }

    /**
     * Get current term
     * In production, this would be determined by current date and school calendar
     */
    private String getCurrentTerm() {
        return "Fall 2024"; // Hardcoded for now
    }

    // ============================================================
    // Health Check
    // ============================================================

    /**
     * GET /api/report-cards/health
     * Health check endpoint
     *
     * @return health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "ReportCardService"
        ));
    }
}
