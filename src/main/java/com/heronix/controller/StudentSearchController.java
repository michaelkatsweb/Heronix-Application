package com.heronix.controller;

import com.heronix.model.domain.AuditLog;
import com.heronix.model.domain.Student;
import com.heronix.model.dto.StudentSearchCriteria;
import com.heronix.repository.StudentRepository;
import com.heronix.service.AuditService;
import com.heronix.service.GlobalSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Student Search REST API Controller
 *
 * Provides endpoints for searching students with various criteria:
 * - Global search across all fields
 * - Advanced criteria-based search
 * - Quick search by name or ID
 * - Category-filtered search
 *
 * Security:
 * - All endpoints require authentication
 * - Most endpoints require ADMIN or TEACHER role
 *
 * @author Heronix Development Team
 * @version 1.0.0
 * @since 2025-12-28
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/students/search")
@CrossOrigin(origins = "*")
public class StudentSearchController {

    private final GlobalSearchService globalSearchService;
    private final StudentRepository studentRepository;
    private final AuditService auditService;

    @Autowired
    public StudentSearchController(
            GlobalSearchService globalSearchService,
            StudentRepository studentRepository,
            AuditService auditService) {
        this.globalSearchService = globalSearchService;
        this.studentRepository = studentRepository;
        this.auditService = auditService;
    }

    // ============================================================
    // Global Search Endpoints
    // ============================================================

    /**
     * GET /api/students/search/global
     * Global search across all student fields
     *
     * @param query search query string
     * @param category filter category (ALL, STUDENTS, TEACHERS, COURSES, ROOMS, ACTIONS)
     * @return list of search results
     */
    @GetMapping("/global")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<GlobalSearchService.SearchResult>> globalSearch(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "STUDENTS") String category) {

        auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "Student", null,
                "Global search: " + query + " in " + category, true, AuditLog.AuditSeverity.INFO);

        List<GlobalSearchService.SearchResult> results = globalSearchService.search(query, category);
        return ResponseEntity.ok(results);
    }

    /**
     * GET /api/students/search/quick
     * Quick search by name or student ID
     *
     * @param query search query string
     * @return list of matching students
     */
    @GetMapping("/quick")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<Student>> quickSearch(@RequestParam String query) {

        auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "Student", null,
                "Quick search: " + query, true, AuditLog.AuditSeverity.INFO);

        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String normalizedQuery = query.toLowerCase().trim();

        List<Student> results = studentRepository.findAll().stream()
                .filter(student -> {
                    // Search by student ID
                    if (student.getStudentId() != null &&
                        student.getStudentId().toLowerCase().contains(normalizedQuery)) {
                        return true;
                    }
                    // Search by first name
                    if (student.getFirstName() != null &&
                        student.getFirstName().toLowerCase().contains(normalizedQuery)) {
                        return true;
                    }
                    // Search by last name
                    if (student.getLastName() != null &&
                        student.getLastName().toLowerCase().contains(normalizedQuery)) {
                        return true;
                    }
                    // Search by full name
                    String fullName = (student.getFirstName() + " " + student.getLastName()).toLowerCase();
                    return fullName.contains(normalizedQuery);
                })
                .limit(50) // Limit to top 50 results for performance
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }

    // ============================================================
    // Advanced Search Endpoints
    // ============================================================

    /**
     * POST /api/students/search/advanced
     * Advanced search with multiple criteria
     *
     * @param criteria search criteria object
     * @return list of matching students
     */
    @PostMapping("/advanced")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<Student>> advancedSearch(@RequestBody StudentSearchCriteria criteria) {

        auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "Student", null,
                "Advanced search: " + criteria.toString(), true, AuditLog.AuditSeverity.INFO);

        List<Student> results = studentRepository.findAll().stream()
                .filter(student -> matchesCriteria(student, criteria))
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }

    /**
     * GET /api/students/search/by-grade
     * Search students by grade level
     *
     * @param gradeLevel grade level (9, 10, 11, 12)
     * @return list of students in that grade
     */
    @GetMapping("/by-grade")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<Student>> searchByGrade(@RequestParam String gradeLevel) {

        auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "Student", null,
                "Search by grade: " + gradeLevel, true, AuditLog.AuditSeverity.INFO);

        List<Student> results = studentRepository.findAll().stream()
                .filter(student -> gradeLevel.equals(student.getGradeLevel()))
                .filter(Student::isActive)
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }

    /**
     * GET /api/students/search/by-email
     * Search student by email
     *
     * @param email student email
     * @return student if found
     */
    @GetMapping("/by-email")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Student> searchByEmail(@RequestParam String email) {

        auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "Student", null,
                "Search by email: " + email, true, AuditLog.AuditSeverity.INFO);

        return studentRepository.findAll().stream()
                .filter(student -> email.equalsIgnoreCase(student.getEmail()))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/students/search/by-student-id
     * Search student by student ID
     *
     * @param studentId student ID
     * @return student if found
     */
    @GetMapping("/by-student-id")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Student> searchByStudentId(@RequestParam String studentId) {

        auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "Student", null,
                "Search by student ID: " + studentId, true, AuditLog.AuditSeverity.INFO);

        return studentRepository.findAll().stream()
                .filter(student -> studentId.equalsIgnoreCase(student.getStudentId()))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/students/search/with-iep
     * Get all students with IEP
     *
     * @return list of students with IEP
     */
    @GetMapping("/with-iep")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Student>> searchStudentsWithIEP() {

        auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "Student", null,
                "Search students with IEP", true, AuditLog.AuditSeverity.INFO);

        List<Student> results = studentRepository.findAll().stream()
                .filter(Student::getHasIEP)
                .filter(Student::isActive)
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }

    /**
     * GET /api/students/search/with-504
     * Get all students with 504 plan
     *
     * @return list of students with 504 plan
     */
    @GetMapping("/with-504")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Student>> searchStudentsWith504() {

        auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "Student", null,
                "Search students with 504 plan", true, AuditLog.AuditSeverity.INFO);

        List<Student> results = studentRepository.findAll().stream()
                .filter(Student::getHas504Plan)
                .filter(Student::isActive)
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }

    /**
     * GET /api/students/search/inactive
     * Get all inactive students
     *
     * @return list of inactive students
     */
    @GetMapping("/inactive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Student>> searchInactiveStudents() {

        auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "Student", null,
                "Search inactive students", true, AuditLog.AuditSeverity.INFO);

        List<Student> results = studentRepository.findAll().stream()
                .filter(student -> !student.isActive())
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Check if student matches the search criteria
     */
    private boolean matchesCriteria(Student student, StudentSearchCriteria criteria) {

        // Grade level filter
        if (criteria.getGradeLevel() != null) {
            try {
                int studentGradeLevel = Integer.parseInt(student.getGradeLevel());
                if (studentGradeLevel != criteria.getGradeLevel()) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }

        // IEP filter
        if (criteria.getHasIEP() != null && criteria.getHasIEP() != student.getHasIEP()) {
            return false;
        }

        // 504 filter
        if (criteria.getHas504() != null && criteria.getHas504() != student.getHas504Plan()) {
            return false;
        }

        // Name pattern filter
        if (criteria.getNamePattern() != null && !criteria.getNamePattern().isEmpty()) {
            String fullName = (student.getFirstName() + " " + student.getLastName()).toLowerCase();
            if (!fullName.contains(criteria.getNamePattern().toLowerCase())) {
                return false;
            }
        }

        // Student ID pattern filter
        if (criteria.getStudentIdPattern() != null && !criteria.getStudentIdPattern().isEmpty()) {
            if (student.getStudentId() == null ||
                !student.getStudentId().toLowerCase().contains(criteria.getStudentIdPattern().toLowerCase())) {
                return false;
            }
        }

        // Active filter
        if (criteria.getActive() != null && criteria.getActive() != student.isActive()) {
            return false;
        }

        // Graduated filter
        if (criteria.getGraduated() != null && criteria.getGraduated() != student.getGraduated()) {
            return false;
        }

        return true;
    }
}
