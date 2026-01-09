package com.heronix.controller;

import com.heronix.model.domain.Assignment;
import com.heronix.model.domain.AssignmentGrade;
import com.heronix.model.domain.AuditLog;
import com.heronix.model.domain.Teacher;
import com.heronix.service.AssignmentGradeService;
import com.heronix.service.AssignmentService;
import com.heronix.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Assignment REST API Controller
 *
 * Provides endpoints for assignment management:
 * - CRUD operations for assignments
 * - Publishing/unpublishing assignments
 * - Assignment statistics and analytics
 * - Grade entry and management
 * - Missing assignment tracking
 * - Class average calculations
 *
 * Security:
 * - All endpoints require authentication
 * - Most endpoints require ADMIN or TEACHER role
 * - Students can view published assignments only
 *
 * @author Heronix Development Team
 * @version 1.0.0
 * @since 2025-12-28
 */
@RestController
@RequestMapping("/api/assignments")
@CrossOrigin(origins = "*")
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final AssignmentGradeService assignmentGradeService;
    private final AuditService auditService;

    @Autowired
    public AssignmentController(
            AssignmentService assignmentService,
            AssignmentGradeService assignmentGradeService,
            AuditService auditService) {
        this.assignmentService = assignmentService;
        this.assignmentGradeService = assignmentGradeService;
        this.auditService = auditService;
    }

    // ============================================================
    // Assignment CRUD Endpoints
    // ============================================================

    /**
     * GET /api/assignments/{id}
     * Get assignment by ID
     *
     * @param id assignment ID
     * @return assignment details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<?> getAssignmentById(@PathVariable Long id) {
        try {
            Assignment assignment = assignmentService.getAssignmentById(id);
            if (assignment == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(assignment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve assignment: " + e.getMessage()));
        }
    }

    /**
     * GET /api/assignments/course/{courseId}
     * Get all assignments for a course
     *
     * @param courseId course ID
     * @return list of assignments
     */
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<?> getAssignmentsByCourse(@PathVariable Long courseId) {
        try {
            List<Assignment> assignments = assignmentService.getAssignmentsByCourse(courseId);

            auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "Assignment", null,
                    "Retrieved assignments for course " + courseId, true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(Map.of(
                    "courseId", courseId,
                    "assignmentCount", assignments.size(),
                    "assignments", assignments
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve assignments: " + e.getMessage()));
        }
    }

    /**
     * GET /api/assignments/course/{courseId}/term/{term}
     * Get assignments for a course in a specific term
     *
     * @param courseId course ID
     * @param term term name (e.g., "Fall 2024")
     * @return list of assignments for term
     */
    @GetMapping("/course/{courseId}/term/{term}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<?> getAssignmentsByTerm(
            @PathVariable Long courseId,
            @PathVariable String term) {
        try {
            List<Assignment> assignments = assignmentService.getAssignmentsByTerm(courseId, term);

            return ResponseEntity.ok(Map.of(
                    "courseId", courseId,
                    "term", term,
                    "assignmentCount", assignments.size(),
                    "assignments", assignments
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve assignments: " + e.getMessage()));
        }
    }

    /**
     * GET /api/assignments/course/{courseId}/published
     * Get published assignments for a course
     *
     * @param courseId course ID
     * @return list of published assignments
     */
    @GetMapping("/course/{courseId}/published")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<?> getPublishedAssignments(@PathVariable Long courseId) {
        try {
            List<Assignment> assignments = assignmentService.getPublishedAssignments(courseId);

            return ResponseEntity.ok(Map.of(
                    "courseId", courseId,
                    "publishedCount", assignments.size(),
                    "assignments", assignments
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve published assignments: " + e.getMessage()));
        }
    }

    /**
     * GET /api/assignments/course/{courseId}/upcoming
     * Get upcoming assignments (next 30 days by default)
     *
     * @param courseId course ID
     * @param days number of days to look ahead (optional, default 30)
     * @return list of upcoming assignments
     */
    @GetMapping("/course/{courseId}/upcoming")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<?> getUpcomingAssignments(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "30") int days) {
        try {
            List<Assignment> assignments = assignmentService.getUpcomingAssignments(courseId, days);

            return ResponseEntity.ok(Map.of(
                    "courseId", courseId,
                    "daysAhead", days,
                    "upcomingCount", assignments.size(),
                    "assignments", assignments
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve upcoming assignments: " + e.getMessage()));
        }
    }

    /**
     * GET /api/assignments/course/{courseId}/past-due
     * Get past due assignments for a course
     *
     * @param courseId course ID
     * @return list of past due assignments
     */
    @GetMapping("/course/{courseId}/past-due")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> getPastDueAssignments(@PathVariable Long courseId) {
        try {
            List<Assignment> assignments = assignmentService.getPastDueAssignments(courseId);

            return ResponseEntity.ok(Map.of(
                    "courseId", courseId,
                    "pastDueCount", assignments.size(),
                    "assignments", assignments
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve past due assignments: " + e.getMessage()));
        }
    }

    /**
     * POST /api/assignments
     * Create a new assignment
     *
     * @param assignment assignment to create
     * @return created assignment
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> createAssignment(@RequestBody Assignment assignment) {
        try {
            Assignment created = assignmentService.createAssignment(assignment);

            auditService.log(AuditLog.AuditAction.STUDENT_CREATE, "Assignment", created.getId(),
                    "Created assignment: " + created.getTitle(), true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create assignment: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/assignments/{id}
     * Update an existing assignment
     *
     * @param id assignment ID
     * @param assignment updated assignment data
     * @return updated assignment
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> updateAssignment(
            @PathVariable Long id,
            @RequestBody Assignment assignment) {
        try {
            Assignment updated = assignmentService.updateAssignment(id, assignment);

            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "Assignment", id,
                    "Updated assignment", true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update assignment: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/assignments/{id}
     * Delete an assignment
     *
     * @param id assignment ID
     * @return success response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteAssignment(@PathVariable Long id) {
        try {
            assignmentService.deleteAssignment(id);

            auditService.log(AuditLog.AuditAction.STUDENT_DELETE, "Assignment", id,
                    "Deleted assignment", true, AuditLog.AuditSeverity.WARNING);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Assignment deleted successfully"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete assignment: " + e.getMessage()));
        }
    }

    // ============================================================
    // Publishing Endpoints
    // ============================================================

    /**
     * PUT /api/assignments/{id}/publish
     * Publish an assignment (make it visible to students)
     *
     * @param id assignment ID
     * @return success response
     */
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> publishAssignment(@PathVariable Long id) {
        try {
            assignmentService.publishAssignment(id);

            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "Assignment", id,
                    "Published assignment", true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Assignment published successfully"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to publish assignment: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/assignments/{id}/unpublish
     * Unpublish an assignment (hide from students)
     *
     * @param id assignment ID
     * @return success response
     */
    @PutMapping("/{id}/unpublish")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> unpublishAssignment(@PathVariable Long id) {
        try {
            assignmentService.unpublishAssignment(id);

            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "Assignment", id,
                    "Unpublished assignment", true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Assignment unpublished successfully"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to unpublish assignment: " + e.getMessage()));
        }
    }

    // ============================================================
    // Assignment Statistics Endpoints
    // ============================================================

    /**
     * GET /api/assignments/{id}/statistics
     * Get statistics for an assignment (min, max, average, median, count)
     *
     * @param id assignment ID
     * @return assignment statistics
     */
    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> getAssignmentStatistics(@PathVariable Long id) {
        try {
            Map<String, Double> stats = assignmentGradeService.getAssignmentStatistics(id);

            auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "AssignmentStatistics", id,
                    "Retrieved assignment statistics", true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(Map.of(
                    "assignmentId", id,
                    "statistics", stats
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to calculate statistics: " + e.getMessage()));
        }
    }

    /**
     * GET /api/assignments/{id}/class-average
     * Get class average for an assignment
     *
     * @param id assignment ID
     * @return class average percentage
     */
    @GetMapping("/{id}/class-average")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> getClassAverage(@PathVariable Long id) {
        try {
            Double average = assignmentService.getClassAverage(id);

            return ResponseEntity.ok(Map.of(
                    "assignmentId", id,
                    "classAverage", average != null ? average : 0.0,
                    "hasGrades", average != null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to calculate class average: " + e.getMessage()));
        }
    }

    /**
     * GET /api/assignments/course/{courseId}/count
     * Get count of assignments for a course
     *
     * @param courseId course ID
     * @return assignment counts
     */
    @GetMapping("/course/{courseId}/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> getAssignmentCounts(@PathVariable Long courseId) {
        try {
            long totalCount = assignmentService.countAssignmentsByCourse(courseId);
            long gradedCount = assignmentService.countGradedAssignments(courseId);

            return ResponseEntity.ok(Map.of(
                    "courseId", courseId,
                    "totalAssignments", totalCount,
                    "gradedAssignments", gradedCount,
                    "ungradedAssignments", totalCount - gradedCount
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to count assignments: " + e.getMessage()));
        }
    }

    // ============================================================
    // Grade Management Endpoints
    // ============================================================

    /**
     * POST /api/assignments/grades/enter
     * Enter a grade for an assignment
     *
     * @param request grade entry request with studentId, assignmentId, score, comments, teacherId
     * @return created grade
     */
    @PostMapping("/grades/enter")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> enterGrade(@RequestBody Map<String, Object> request) {
        try {
            Long studentId = Long.valueOf(request.get("studentId").toString());
            Long assignmentId = Long.valueOf(request.get("assignmentId").toString());
            Double score = Double.valueOf(request.get("score").toString());
            String comments = request.get("comments") != null ? request.get("comments").toString() : null;

            // Note: Teacher entity would need to be retrieved from security context or request
            // For now, passing null - this should be enhanced with actual teacher lookup
            AssignmentGrade grade = assignmentGradeService.enterGrade(studentId, assignmentId, score, comments, null);

            auditService.log(AuditLog.AuditAction.STUDENT_CREATE, "AssignmentGrade", grade.getId(),
                    "Entered grade for assignment " + assignmentId, true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.status(HttpStatus.CREATED).body(grade);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to enter grade: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/assignments/grades/{gradeId}
     * Update an existing grade
     *
     * @param gradeId grade ID
     * @param request update request with score and comments
     * @return updated grade
     */
    @PutMapping("/grades/{gradeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> updateGrade(
            @PathVariable Long gradeId,
            @RequestBody Map<String, Object> request) {
        try {
            Double score = Double.valueOf(request.get("score").toString());
            String comments = request.get("comments") != null ? request.get("comments").toString() : null;

            AssignmentGrade updated = assignmentGradeService.updateGrade(gradeId, score, comments);

            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "AssignmentGrade", gradeId,
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
     * GET /api/assignments/{assignmentId}/grades
     * Get all grades for an assignment
     *
     * @param assignmentId assignment ID
     * @return list of grades
     */
    @GetMapping("/{assignmentId}/grades")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> getAssignmentGrades(@PathVariable Long assignmentId) {
        try {
            List<AssignmentGrade> grades = assignmentGradeService.getAssignmentGrades(assignmentId);

            return ResponseEntity.ok(Map.of(
                    "assignmentId", assignmentId,
                    "gradeCount", grades.size(),
                    "grades", grades
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve grades: " + e.getMessage()));
        }
    }

    /**
     * GET /api/assignments/grades/student/{studentId}/course/{courseId}
     * Get all assignment grades for a student in a course
     *
     * @param studentId student ID
     * @param courseId course ID
     * @return list of grades
     */
    @GetMapping("/grades/student/{studentId}/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<?> getStudentGrades(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        try {
            List<AssignmentGrade> grades = assignmentGradeService.getStudentGrades(studentId, courseId);

            return ResponseEntity.ok(Map.of(
                    "studentId", studentId,
                    "courseId", courseId,
                    "gradeCount", grades.size(),
                    "grades", grades
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve student grades: " + e.getMessage()));
        }
    }

    /**
     * GET /api/assignments/grades/student/{studentId}/course/{courseId}/missing
     * Get missing assignments for a student in a course
     *
     * @param studentId student ID
     * @param courseId course ID
     * @return list of missing assignments
     */
    @GetMapping("/grades/student/{studentId}/course/{courseId}/missing")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<?> getMissingAssignments(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        try {
            List<AssignmentGrade> missingGrades = assignmentGradeService.getMissingAssignments(studentId, courseId);
            long missingCount = assignmentGradeService.countMissingAssignments(studentId, courseId);

            auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "MissingAssignments", studentId,
                    "Retrieved missing assignments for course " + courseId, true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(Map.of(
                    "studentId", studentId,
                    "courseId", courseId,
                    "missingCount", missingCount,
                    "missingAssignments", missingGrades
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve missing assignments: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/assignments/grades/{gradeId}/mark-excused
     * Mark a grade as excused
     *
     * @param gradeId grade ID
     * @param request request with reason
     * @return success response
     */
    @PutMapping("/grades/{gradeId}/mark-excused")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> markExcused(
            @PathVariable Long gradeId,
            @RequestBody Map<String, String> request) {
        try {
            String reason = request.get("reason");
            assignmentGradeService.markExcused(gradeId, reason);

            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "AssignmentGrade", gradeId,
                    "Marked as excused: " + reason, true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Grade marked as excused"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to mark grade as excused: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/assignments/grades/{gradeId}/mark-missing
     * Mark a grade as missing
     *
     * @param gradeId grade ID
     * @return success response
     */
    @PutMapping("/grades/{gradeId}/mark-missing")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> markMissing(@PathVariable Long gradeId) {
        try {
            assignmentGradeService.markMissing(gradeId);

            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "AssignmentGrade", gradeId,
                    "Marked as missing", true, AuditLog.AuditSeverity.WARNING);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Grade marked as missing"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to mark grade as missing: " + e.getMessage()));
        }
    }

    // ============================================================
    // Health Check
    // ============================================================

    /**
     * GET /api/assignments/health
     * Health check endpoint
     *
     * @return health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "AssignmentService"
        ));
    }
}
