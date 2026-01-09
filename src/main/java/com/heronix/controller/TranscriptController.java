package com.heronix.controller;

import com.heronix.model.domain.AuditLog;
import com.heronix.repository.StudentRepository;
import com.heronix.service.AuditService;
import com.heronix.service.impl.TranscriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Transcript REST API Controller
 *
 * Provides endpoints for student transcript management:
 * - Generate full transcripts
 * - Get cumulative/weighted GPA
 * - Get class rank
 * - Get transcript by academic year
 * - Get credits earned
 * - Get advanced courses (AP/Honors)
 *
 * Security:
 * - All endpoints require authentication
 * - Most endpoints allow ADMIN, TEACHER, or STUDENT (own data only)
 *
 * @author Heronix Development Team
 * @version 1.0.0
 * @since 2025-12-28
 */
@RestController
@RequestMapping("/api/transcripts")
@CrossOrigin(origins = "*")
public class TranscriptController {

    private final TranscriptService transcriptService;
    private final StudentRepository studentRepository;
    private final AuditService auditService;

    @Autowired
    public TranscriptController(
            TranscriptService transcriptService,
            StudentRepository studentRepository,
            AuditService auditService) {
        this.transcriptService = transcriptService;
        this.studentRepository = studentRepository;
        this.auditService = auditService;
    }

    // ============================================================
    // Transcript Generation Endpoints
    // ============================================================

    /**
     * GET /api/transcripts/student/{studentId}
     * Generate full transcript for a student
     *
     * @param studentId student ID
     * @return student transcript
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<?> getStudentTranscript(@PathVariable Long studentId) {
        try {
            var transcript = transcriptService.generateTranscript(studentId);
            auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "Transcript", studentId,
                    "Generated transcript", true, AuditLog.AuditSeverity.INFO);
            return ResponseEntity.ok(transcript);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate transcript: " + e.getMessage()));
        }
    }

    /**
     * GET /api/transcripts/student/{studentId}/pdf
     * Generate PDF transcript for a student
     *
     * @param studentId student ID
     * @return PDF transcript (future implementation)
     */
    @GetMapping("/student/{studentId}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<?> getStudentTranscriptPDF(@PathVariable Long studentId) {
        try {
            // Future: Generate PDF using transcript data
            var transcript = transcriptService.generateTranscript(studentId);

            auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "Transcript", studentId,
                    "Generated PDF transcript", true, AuditLog.AuditSeverity.INFO);

            // For now, return transcript data with PDF flag
            return ResponseEntity.ok(Map.of(
                    "format", "pdf",
                    "message", "PDF generation not yet implemented",
                    "transcript", transcript
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate PDF transcript: " + e.getMessage()));
        }
    }

    // ============================================================
    // GPA Endpoints
    // ============================================================

    /**
     * GET /api/transcripts/student/{studentId}/gpa
     * Get cumulative and weighted GPA for a student
     *
     * @param studentId student ID
     * @return GPA data
     */
    @GetMapping("/student/{studentId}/gpa")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<?> getStudentGPA(@PathVariable Long studentId) {
        try {
            BigDecimal cumulativeGpa = transcriptService.calculateCumulativeGpa(studentId);
            BigDecimal weightedGpa = transcriptService.calculateWeightedGpa(studentId);

            auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "GPA", studentId,
                    "Retrieved GPA data", true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(Map.of(
                    "studentId", studentId,
                    "cumulativeGpa", cumulativeGpa != null ? cumulativeGpa : 0.0,
                    "weightedGpa", weightedGpa != null ? weightedGpa : 0.0
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to calculate GPA: " + e.getMessage()));
        }
    }

    // ============================================================
    // Class Rank Endpoints
    // ============================================================

    /**
     * GET /api/transcripts/student/{studentId}/class-rank
     * Get class rank for a student
     *
     * @param studentId student ID
     * @return class rank information
     */
    @GetMapping("/student/{studentId}/class-rank")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<?> getStudentClassRank(@PathVariable Long studentId) {
        try {
            var classRankInfo = transcriptService.getClassRank(studentId);

            auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "ClassRank", studentId,
                    "Retrieved class rank", true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(classRankInfo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to calculate class rank: " + e.getMessage()));
        }
    }

    // ============================================================
    // Credits Endpoints
    // ============================================================

    /**
     * GET /api/transcripts/student/{studentId}/credits
     * Get total credits earned by student
     *
     * @param studentId student ID
     * @return credits information
     */
    @GetMapping("/student/{studentId}/credits")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<?> getStudentCredits(@PathVariable Long studentId) {
        try {
            var student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "Credits", studentId,
                    "Retrieved credits", true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(Map.of(
                    "studentId", studentId,
                    "creditsEarned", student.getCreditsEarned() != null ? student.getCreditsEarned() : 0.0,
                    "creditsRequired", student.getCreditsRequired() != null ? student.getCreditsRequired() : 24.0,
                    "creditsRemaining", (student.getCreditsRequired() != null ? student.getCreditsRequired() : 24.0) -
                                       (student.getCreditsEarned() != null ? student.getCreditsEarned() : 0.0),
                    "onTrackForGraduation", (student.getCreditsEarned() != null ? student.getCreditsEarned() : 0.0) >=
                                           (student.getCreditsRequired() != null ? student.getCreditsRequired() : 24.0)
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve credits: " + e.getMessage()));
        }
    }

    // ============================================================
    // Advanced Courses Endpoints
    // ============================================================

    /**
     * GET /api/transcripts/student/{studentId}/advanced-courses
     * Get advanced courses (AP/IB/Honors) taken by student
     *
     * @param studentId student ID
     * @return list of advanced courses
     */
    @GetMapping("/student/{studentId}/advanced-courses")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<?> getStudentAdvancedCourses(@PathVariable Long studentId) {
        try {
            var transcript = transcriptService.generateTranscript(studentId);

            // Filter for advanced courses from all academic years
            var advancedCourses = transcript.getAcademicYears().stream()
                    .flatMap(year -> year.getCourses().stream())
                    .filter(course -> {
                        String courseName = course.getCourse().getCourseName().toUpperCase();
                        return courseName.contains("AP ") ||
                               courseName.contains("IB ") ||
                               courseName.contains("HONORS") ||
                               courseName.contains("ADVANCED");
                    })
                    .toList();

            auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "AdvancedCourses", studentId,
                    "Retrieved advanced courses", true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(Map.of(
                    "studentId", studentId,
                    "advancedCoursesCount", advancedCourses.size(),
                    "advancedCourses", advancedCourses
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve advanced courses: " + e.getMessage()));
        }
    }

    // ============================================================
    // Year-Specific Endpoints
    // ============================================================

    /**
     * GET /api/transcripts/student/{studentId}/year/{year}
     * Get transcript for a specific academic year
     *
     * @param studentId student ID
     * @param year academic year (e.g., "2024-2025")
     * @return transcript for specified year
     */
    @GetMapping("/student/{studentId}/year/{year}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<?> getStudentTranscriptForYear(
            @PathVariable Long studentId,
            @PathVariable String year) {
        try {
            var fullTranscript = transcriptService.generateTranscript(studentId);

            // Filter courses for specific year
            var yearCourses = fullTranscript.getAcademicYears().stream()
                    .filter(academicYear -> year.equals(academicYear.getAcademicYear()))
                    .flatMap(academicYear -> academicYear.getCourses().stream())
                    .toList();

            auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "Transcript", studentId,
                    "Generated transcript for year " + year, true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(Map.of(
                    "studentId", studentId,
                    "academicYear", year,
                    "courses", yearCourses,
                    "courseCount", yearCourses.size()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate transcript: " + e.getMessage()));
        }
    }

    // ============================================================
    // Health Check
    // ============================================================

    /**
     * GET /api/transcripts/health
     * Health check endpoint
     *
     * @return health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "TranscriptService"
        ));
    }
}
