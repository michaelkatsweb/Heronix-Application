package com.heronix.controller;

import com.heronix.model.domain.ReEnrollment;
import com.heronix.model.domain.ReEnrollment.ReEnrollmentStatus;
import com.heronix.model.domain.ReEnrollment.ApprovalDecision;
import com.heronix.service.ReEnrollmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Re-Enrollment Management
 * Provides endpoints for student re-enrollment processing
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
// @RestController  // Disabled for API server mode
@RequestMapping("/api/re-enrollments")
@CrossOrigin(origins = "*")
public class ReEnrollmentController {

    @Autowired
    private ReEnrollmentService reEnrollmentService;

    // ========================================================================
    // CREATE OPERATIONS
    // ========================================================================

    /**
     * Create new re-enrollment application
     * POST /api/re-enrollments
     */
    @PostMapping
    public ResponseEntity<ReEnrollment> createReEnrollment(@RequestBody Map<String, Object> request) {
        try {
            log.info("Creating re-enrollment for student ID: {}", request.get("studentId"));

            ReEnrollment reEnrollment = reEnrollmentService.createReEnrollment(
                    Long.parseLong(request.get("studentId").toString()),
                    (String) request.get("requestedGradeLevel"),
                    LocalDate.parse((String) request.get("intendedEnrollmentDate")),
                    Long.parseLong(request.get("createdByStaffId").toString())
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(reEnrollment);
        } catch (Exception e) {
            log.error("Error creating re-enrollment: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================================================
    // READ OPERATIONS
    // ========================================================================

    /**
     * Get re-enrollment by ID
     * GET /api/re-enrollments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReEnrollment> getReEnrollmentById(@PathVariable Long id) {
        try {
            ReEnrollment reEnrollment = reEnrollmentService.getReEnrollmentById(id);
            return ResponseEntity.ok(reEnrollment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get re-enrollment by number
     * GET /api/re-enrollments/number/{reEnrollmentNumber}
     */
    @GetMapping("/number/{reEnrollmentNumber}")
    public ResponseEntity<ReEnrollment> getByReEnrollmentNumber(@PathVariable String reEnrollmentNumber) {
        try {
            ReEnrollment reEnrollment = reEnrollmentService.getByReEnrollmentNumber(reEnrollmentNumber);
            return ResponseEntity.ok(reEnrollment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all re-enrollments by status
     * GET /api/re-enrollments/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReEnrollment>> getByStatus(@PathVariable ReEnrollmentStatus status) {
        List<ReEnrollment> reEnrollments = reEnrollmentService.getByStatus(status);
        return ResponseEntity.ok(reEnrollments);
    }

    /**
     * Get re-enrollments pending review
     * GET /api/re-enrollments/pending-review
     */
    @GetMapping("/pending-review")
    public ResponseEntity<List<ReEnrollment>> getPendingReview() {
        List<ReEnrollment> reEnrollments = reEnrollmentService.getPendingReview();
        return ResponseEntity.ok(reEnrollments);
    }


    /**
     * Get re-enrollments by assigned counselor
     * GET /api/re-enrollments/counselor/{counselorId}
     */
    @GetMapping("/counselor/{counselorId}")
    public ResponseEntity<List<ReEnrollment>> getByCounselor(@PathVariable Long counselorId) {
        List<ReEnrollment> reEnrollments = reEnrollmentService.getByCounselor(counselorId);
        return ResponseEntity.ok(reEnrollments);
    }

    // ========================================================================
    // UPDATE OPERATIONS
    // ========================================================================

    /**
     * Update re-enrollment
     * PUT /api/re-enrollments/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReEnrollment> updateReEnrollment(
            @PathVariable Long id,
            @RequestBody ReEnrollment reEnrollment,
            @RequestParam Long updatedByStaffId) {
        try {
            reEnrollment.setId(id);
            ReEnrollment updated = reEnrollmentService.updateReEnrollment(reEnrollment, updatedByStaffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating re-enrollment: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }


    /**
     * Assign counselor
     * POST /api/re-enrollments/{id}/assign-counselor
     */
    @PostMapping("/{id}/assign-counselor")
    public ResponseEntity<ReEnrollment> assignCounselor(
            @PathVariable Long id,
            @RequestParam Long counselorId,
            @RequestParam Long updatedByStaffId) {
        try {
            ReEnrollment updated = reEnrollmentService.assignCounselor(id, counselorId, updatedByStaffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error assigning counselor: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Counselor decision
     * POST /api/re-enrollments/{id}/counselor-decision
     */
    @PostMapping("/{id}/counselor-decision")
    public ResponseEntity<ReEnrollment> counselorDecision(
            @PathVariable Long id,
            @RequestParam ApprovalDecision decision,
            @RequestParam(required = false) String notes,
            @RequestParam Long counselorId) {
        try {
            ReEnrollment updated = reEnrollmentService.counselorDecision(id, decision, notes, counselorId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error recording counselor decision: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Principal decision
     * POST /api/re-enrollments/{id}/principal-decision
     */
    @PostMapping("/{id}/principal-decision")
    public ResponseEntity<ReEnrollment> principalDecision(
            @PathVariable Long id,
            @RequestParam ApprovalDecision decision,
            @RequestParam(required = false) String notes,
            @RequestParam Long principalId) {
        try {
            ReEnrollment updated = reEnrollmentService.principalDecision(id, decision, notes, principalId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error recording principal decision: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }


    /**
     * Record fee payment
     * POST /api/re-enrollments/{id}/record-fee-payment
     */
    @PostMapping("/{id}/record-fee-payment")
    public ResponseEntity<ReEnrollment> recordFeePayment(
            @PathVariable Long id,
            @RequestParam Long updatedByStaffId) {
        try {
            ReEnrollment updated = reEnrollmentService.recordFeePayment(id, updatedByStaffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error recording fee payment: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Complete re-enrollment
     * POST /api/re-enrollments/{id}/complete
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<ReEnrollment> completeReEnrollment(
            @PathVariable Long id,
            @RequestParam Long enrolledStudentId,
            @RequestParam Long completedByStaffId) {
        try {
            ReEnrollment updated = reEnrollmentService.completeReEnrollment(id, enrolledStudentId, completedByStaffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error completing re-enrollment: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Cancel re-enrollment
     * POST /api/re-enrollments/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ReEnrollment> cancelReEnrollment(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestParam Long cancelledByStaffId) {
        try {
            ReEnrollment updated = reEnrollmentService.cancelReEnrollment(id, reason, cancelledByStaffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error cancelling re-enrollment: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================================================
    // STATISTICS & REPORTING
    // ========================================================================

    /**
     * Get re-enrollment statistics
     * GET /api/re-enrollments/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ReEnrollmentService.ReEnrollmentStatistics> getStatistics() {
        ReEnrollmentService.ReEnrollmentStatistics stats = reEnrollmentService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get count by reason
     * GET /api/re-enrollments/count-by-reason
     */
    @GetMapping("/count-by-reason")
    public ResponseEntity<List<Object[]>> getCountByReason() {
        List<Object[]> counts = reEnrollmentService.getCountByReason();
        return ResponseEntity.ok(counts);
    }

    // ========================================================================
    // DELETE OPERATIONS
    // ========================================================================

    /**
     * Delete re-enrollment (draft only)
     * DELETE /api/re-enrollments/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReEnrollment(@PathVariable Long id) {
        try {
            reEnrollmentService.deleteReEnrollment(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Error deleting re-enrollment: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
