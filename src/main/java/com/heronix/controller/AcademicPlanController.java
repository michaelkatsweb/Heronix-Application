package com.heronix.controller;

import com.heronix.model.domain.AcademicPlan;
import com.heronix.model.domain.AuditLog;
import com.heronix.model.domain.PlannedCourse;
import com.heronix.service.AcademicPlanningService;
import com.heronix.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Academic Plan REST API Controller
 *
 * Provides endpoints for four-year academic planning:
 * - Create, read, update, delete academic plans
 * - Generate plans from course sequences or AI recommendations
 * - Manage planned courses (add, remove, mark completed)
 * - Approval workflow (counselor, student, parent)
 * - Graduation requirements checking
 * - Planning statistics
 *
 * Security:
 * - All endpoints require authentication
 * - Most endpoints require ADMIN or COUNSELOR role
 * - Students can view their own plans
 *
 * @author Heronix Development Team
 * @version 1.0.0
 * @since 2025-12-28
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/academic-plans")
@CrossOrigin(origins = "*")
public class AcademicPlanController {

    private final AcademicPlanningService academicPlanningService;
    private final AuditService auditService;

    @Autowired
    public AcademicPlanController(
            AcademicPlanningService academicPlanningService,
            AuditService auditService) {
        this.academicPlanningService = academicPlanningService;
        this.auditService = auditService;
    }

    // ============================================================
    // Academic Plan CRUD Endpoints
    // ============================================================

    /**
     * GET /api/academic-plans
     * Get all academic plans
     *
     * @return list of all plans
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<List<AcademicPlan>> getAllPlans() {
        try {
            List<AcademicPlan> plans = academicPlanningService.getAllPlans();
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/academic-plans/{id}
     * Get academic plan by ID
     *
     * @param id plan ID
     * @return academic plan
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT')")
    public ResponseEntity<?> getPlanById(@PathVariable Long id) {
        try {
            return academicPlanningService.getPlanById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve plan: " + e.getMessage()));
        }
    }

    /**
     * GET /api/academic-plans/student/{studentId}
     * Get all plans for a student
     *
     * @param studentId student ID
     * @return list of plans for student
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT')")
    public ResponseEntity<?> getPlansForStudent(@PathVariable Long studentId) {
        try {
            List<AcademicPlan> plans = academicPlanningService.getPlansForStudent(studentId);

            auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "AcademicPlan", studentId,
                    "Retrieved all academic plans", true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(Map.of(
                    "studentId", studentId,
                    "planCount", plans.size(),
                    "plans", plans
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve plans: " + e.getMessage()));
        }
    }

    /**
     * GET /api/academic-plans/student/{studentId}/primary
     * Get primary plan for a student
     *
     * @param studentId student ID
     * @return primary academic plan
     */
    @GetMapping("/student/{studentId}/primary")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT')")
    public ResponseEntity<?> getPrimaryPlanForStudent(@PathVariable Long studentId) {
        try {
            return academicPlanningService.getPrimaryPlanForStudent(studentId)
                    .map(plan -> {
                        auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "AcademicPlan", studentId,
                                "Retrieved primary academic plan", true, AuditLog.AuditSeverity.INFO);
                        return ResponseEntity.ok(plan);
                    })
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * POST /api/academic-plans
     * Create a new academic plan
     *
     * @param plan plan to create
     * @return created plan
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<?> createPlan(@RequestBody AcademicPlan plan) {
        try {
            AcademicPlan created = academicPlanningService.createPlan(plan);

            auditService.log(AuditLog.AuditAction.STUDENT_CREATE, "AcademicPlan", created.getId(),
                    "Created academic plan: " + created.getPlanName(), true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create plan: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/academic-plans/{id}
     * Update an existing academic plan
     *
     * @param id plan ID
     * @param updates plan updates
     * @return updated plan
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<?> updatePlan(
            @PathVariable Long id,
            @RequestBody AcademicPlan updates) {
        try {
            AcademicPlan updated = academicPlanningService.updatePlan(id, updates);

            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "AcademicPlan", id,
                    "Updated academic plan", true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update plan: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/academic-plans/{id}
     * Delete (deactivate) an academic plan
     *
     * @param id plan ID
     * @return success response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePlan(@PathVariable Long id) {
        try {
            academicPlanningService.deletePlan(id);

            auditService.log(AuditLog.AuditAction.STUDENT_DELETE, "AcademicPlan", id,
                    "Deleted (deactivated) academic plan", true, AuditLog.AuditSeverity.WARNING);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Academic plan deleted successfully"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete plan: " + e.getMessage()));
        }
    }

    // ============================================================
    // Plan Generation Endpoints
    // ============================================================

    /**
     * POST /api/academic-plans/generate/from-sequence
     * Generate plan from a course sequence
     *
     * @param request generation request with studentId, sequenceId, startYear, planName
     * @return generated plan
     */
    @PostMapping("/generate/from-sequence")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<?> generatePlanFromSequence(@RequestBody Map<String, Object> request) {
        try {
            Long studentId = Long.valueOf(request.get("studentId").toString());
            Long sequenceId = Long.valueOf(request.get("sequenceId").toString());
            String startYear = request.get("startYear").toString();
            String planName = request.get("planName") != null ? request.get("planName").toString() : null;

            AcademicPlan plan = academicPlanningService.generatePlanFromSequence(
                    studentId, sequenceId, startYear, planName);

            auditService.log(AuditLog.AuditAction.STUDENT_CREATE, "AcademicPlan", plan.getId(),
                    "Generated plan from sequence " + sequenceId, true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.status(HttpStatus.CREATED).body(plan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate plan from sequence: " + e.getMessage()));
        }
    }

    /**
     * POST /api/academic-plans/generate/from-recommendations
     * Generate plan from AI course recommendations
     *
     * @param request generation request with studentId, startYear, planName
     * @return generated plan
     */
    @PostMapping("/generate/from-recommendations")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<?> generatePlanFromRecommendations(@RequestBody Map<String, Object> request) {
        try {
            Long studentId = Long.valueOf(request.get("studentId").toString());
            String startYear = request.get("startYear").toString();
            String planName = request.get("planName") != null ? request.get("planName").toString() : null;

            AcademicPlan plan = academicPlanningService.generatePlanFromRecommendations(
                    studentId, startYear, planName);

            auditService.log(AuditLog.AuditAction.STUDENT_CREATE, "AcademicPlan", plan.getId(),
                    "Generated plan from AI recommendations", true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.status(HttpStatus.CREATED).body(plan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate plan from recommendations: " + e.getMessage()));
        }
    }

    // ============================================================
    // Planned Course Management Endpoints
    // ============================================================

    /**
     * POST /api/academic-plans/{planId}/courses
     * Add a course to an academic plan
     *
     * @param planId plan ID
     * @param plannedCourse planned course to add
     * @return updated plan
     */
    @PostMapping("/{planId}/courses")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<?> addCourseToPlan(
            @PathVariable Long planId,
            @RequestBody PlannedCourse plannedCourse) {
        try {
            AcademicPlan updated = academicPlanningService.addCourseToPlan(planId, plannedCourse);

            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "AcademicPlan", planId,
                    "Added course to plan", true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to add course to plan: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/academic-plans/{planId}/courses/{plannedCourseId}
     * Remove a course from an academic plan
     *
     * @param planId plan ID
     * @param plannedCourseId planned course ID to remove
     * @return updated plan
     */
    @DeleteMapping("/{planId}/courses/{plannedCourseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<?> removeCourseFromPlan(
            @PathVariable Long planId,
            @PathVariable Long plannedCourseId) {
        try {
            AcademicPlan updated = academicPlanningService.removeCourseFromPlan(planId, plannedCourseId);

            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "AcademicPlan", planId,
                    "Removed course from plan", true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to remove course from plan: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/academic-plans/courses/{plannedCourseId}/complete
     * Mark a planned course as completed
     *
     * @param plannedCourseId planned course ID
     * @param request completion data with grade
     * @return updated planned course
     */
    @PutMapping("/courses/{plannedCourseId}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<?> markCourseCompleted(
            @PathVariable Long plannedCourseId,
            @RequestBody Map<String, String> request) {
        try {
            String grade = request.get("grade");
            PlannedCourse updated = academicPlanningService.markCourseCompleted(plannedCourseId, grade);

            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "PlannedCourse", plannedCourseId,
                    "Marked course as completed with grade: " + grade, true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to mark course as completed: " + e.getMessage()));
        }
    }

    // ============================================================
    // Approval Workflow Endpoints
    // ============================================================

    /**
     * POST /api/academic-plans/{planId}/approve/counselor
     * Approve plan by counselor
     *
     * @param planId plan ID
     * @param request approval data with counselorId
     * @return updated plan
     */
    @PostMapping("/{planId}/approve/counselor")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<?> approveByCounselor(
            @PathVariable Long planId,
            @RequestBody Map<String, Long> request) {
        try {
            Long counselorId = request.get("counselorId");
            AcademicPlan approved = academicPlanningService.approveByCounselor(planId, counselorId);

            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "AcademicPlan", planId,
                    "Approved by counselor " + counselorId, true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(approved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to approve plan: " + e.getMessage()));
        }
    }

    /**
     * POST /api/academic-plans/{planId}/accept/student
     * Accept plan by student
     *
     * @param planId plan ID
     * @return updated plan
     */
    @PostMapping("/{planId}/accept/student")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR', 'STUDENT')")
    public ResponseEntity<?> acceptByStudent(@PathVariable Long planId) {
        try {
            AcademicPlan accepted = academicPlanningService.acceptByStudent(planId);

            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "AcademicPlan", planId,
                    "Accepted by student", true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(accepted);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to accept plan: " + e.getMessage()));
        }
    }

    /**
     * POST /api/academic-plans/{planId}/accept/parent
     * Accept plan by parent
     *
     * @param planId plan ID
     * @return updated plan
     */
    @PostMapping("/{planId}/accept/parent")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARENT')")
    public ResponseEntity<?> acceptByParent(@PathVariable Long planId) {
        try {
            AcademicPlan accepted = academicPlanningService.acceptByParent(planId);

            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "AcademicPlan", planId,
                    "Accepted by parent", true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(accepted);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to accept plan: " + e.getMessage()));
        }
    }

    // ============================================================
    // Graduation Requirements Endpoints
    // ============================================================

    /**
     * PUT /api/academic-plans/{planId}/recalculate
     * Recalculate plan totals and graduation requirements
     *
     * @param planId plan ID
     * @return success response
     */
    @PutMapping("/{planId}/recalculate")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<?> recalculatePlanTotals(@PathVariable Long planId) {
        try {
            AcademicPlan plan = academicPlanningService.getPlanById(planId)
                    .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));

            academicPlanningService.recalculatePlanTotals(plan);

            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "AcademicPlan", planId,
                    "Recalculated plan totals", true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Plan totals recalculated",
                    "totalCreditsPlanned", plan.getTotalCreditsPlanned(),
                    "totalCreditsCompleted", plan.getTotalCreditsCompleted(),
                    "meetsGraduationRequirements", plan.getMeetsGraduationRequirements()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to recalculate plan: " + e.getMessage()));
        }
    }

    // ============================================================
    // Statistics Endpoints
    // ============================================================

    /**
     * GET /api/academic-plans/statistics
     * Get planning statistics
     *
     * @return statistics about academic plans
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    public ResponseEntity<?> getStatistics() {
        try {
            AcademicPlanningService.PlanningStatistics stats = academicPlanningService.getStatistics();

            auditService.log(AuditLog.AuditAction.STUDENT_VIEW, "PlanningStatistics", null,
                    "Retrieved planning statistics", true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve statistics: " + e.getMessage()));
        }
    }

    // ============================================================
    // Health Check
    // ============================================================

    /**
     * GET /api/academic-plans/health
     * Health check endpoint
     *
     * @return health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "AcademicPlanService"
        ));
    }
}
