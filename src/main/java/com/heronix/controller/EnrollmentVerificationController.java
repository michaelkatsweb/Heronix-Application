package com.heronix.controller;

import com.heronix.model.domain.EnrollmentVerification;
import com.heronix.model.domain.EnrollmentVerification.VerificationStatus;
import com.heronix.model.domain.EnrollmentVerification.VerificationPurpose;
import com.heronix.service.EnrollmentVerificationService;
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
 * REST API Controller for Enrollment Verification Management
 * Provides endpoints for enrollment verification letters and certificates
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
// @RestController  // Disabled for API server mode
@RequestMapping("/api/enrollment-verifications")
@CrossOrigin(origins = "*")
public class EnrollmentVerificationController {

    @Autowired
    private EnrollmentVerificationService verificationService;

    // ========================================================================
    // CREATE OPERATIONS
    // ========================================================================

    /**
     * Create new enrollment verification request
     * POST /api/enrollment-verifications
     */
    @PostMapping
    public ResponseEntity<EnrollmentVerification> createVerification(@RequestBody Map<String, Object> request) {
        try {
            log.info("Creating enrollment verification for student ID: {}", request.get("studentId"));

            EnrollmentVerification verification = verificationService.createVerification(
                    Long.parseLong(request.get("studentId").toString()),
                    VerificationPurpose.valueOf((String) request.get("purpose")),
                    Long.parseLong(request.get("createdByStaffId").toString())
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(verification);
        } catch (Exception e) {
            log.error("Error creating verification: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================================================
    // READ OPERATIONS
    // ========================================================================

    /**
     * Get verification by ID
     * GET /api/enrollment-verifications/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentVerification> getVerificationById(@PathVariable Long id) {
        try {
            EnrollmentVerification verification = verificationService.getVerificationById(id);
            return ResponseEntity.ok(verification);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get verification by number
     * GET /api/enrollment-verifications/number/{verificationNumber}
     */
    @GetMapping("/number/{verificationNumber}")
    public ResponseEntity<EnrollmentVerification> getByVerificationNumber(@PathVariable String verificationNumber) {
        try {
            EnrollmentVerification verification = verificationService.getByVerificationNumber(verificationNumber);
            return ResponseEntity.ok(verification);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all verifications by status
     * GET /api/enrollment-verifications/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<EnrollmentVerification>> getByStatus(@PathVariable VerificationStatus status) {
        List<EnrollmentVerification> verifications = verificationService.getByStatus(status);
        return ResponseEntity.ok(verifications);
    }

    /**
     * Get verifications by purpose
     * GET /api/enrollment-verifications/purpose/{purpose}
     */
    @GetMapping("/purpose/{purpose}")
    public ResponseEntity<List<EnrollmentVerification>> getByPurpose(@PathVariable VerificationPurpose purpose) {
        List<EnrollmentVerification> verifications = verificationService.getByPurpose(purpose);
        return ResponseEntity.ok(verifications);
    }

    /**
     * Get verifications pending verification
     * GET /api/enrollment-verifications/pending-verification
     */
    @GetMapping("/pending-verification")
    public ResponseEntity<List<EnrollmentVerification>> getPendingVerification() {
        List<EnrollmentVerification> verifications = verificationService.getPendingVerification();
        return ResponseEntity.ok(verifications);
    }

    /**
     * Get urgent verifications
     * GET /api/enrollment-verifications/urgent
     */
    @GetMapping("/urgent")
    public ResponseEntity<List<EnrollmentVerification>> getUrgentVerifications() {
        List<EnrollmentVerification> verifications = verificationService.getUrgentVerifications();
        return ResponseEntity.ok(verifications);
    }

    /**
     * Get verifications with unpaid fees
     * GET /api/enrollment-verifications/unpaid-fees
     */
    @GetMapping("/unpaid-fees")
    public ResponseEntity<List<EnrollmentVerification>> getUnpaidFees() {
        List<EnrollmentVerification> verifications = verificationService.getUnpaidFees();
        return ResponseEntity.ok(verifications);
    }

    /**
     * Get verifications by student
     * GET /api/enrollment-verifications/student/{studentId}
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<EnrollmentVerification>> getByStudent(@PathVariable Long studentId) {
        List<EnrollmentVerification> verifications = verificationService.getByStudent(studentId);
        return ResponseEntity.ok(verifications);
    }

    // ========================================================================
    // UPDATE OPERATIONS
    // ========================================================================

    /**
     * Update verification
     * PUT /api/enrollment-verifications/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<EnrollmentVerification> updateVerification(
            @PathVariable Long id,
            @RequestBody EnrollmentVerification verification,
            @RequestParam Long updatedByStaffId) {
        try {
            verification.setId(id);
            EnrollmentVerification updated = verificationService.updateVerification(verification, updatedByStaffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating verification: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Verify enrollment status
     * POST /api/enrollment-verifications/{id}/verify
     */
    @PostMapping("/{id}/verify")
    public ResponseEntity<EnrollmentVerification> verifyEnrollment(
            @PathVariable Long id,
            @RequestParam Boolean fullTimeEnrollment,
            @RequestParam Boolean academicGoodStanding,
            @RequestParam(required = false) Double currentGPA,
            @RequestParam Long verifiedByStaffId) {
        try {
            EnrollmentVerification updated = verificationService.verifyEnrollment(
                    id, fullTimeEnrollment, academicGoodStanding, currentGPA, verifiedByStaffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error verifying enrollment: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Generate verification document
     * POST /api/enrollment-verifications/{id}/generate
     */
    @PostMapping("/{id}/generate")
    public ResponseEntity<EnrollmentVerification> generateDocument(
            @PathVariable Long id,
            @RequestParam String documentPath,
            @RequestParam Long generatedByStaffId) {
        try {
            EnrollmentVerification updated = verificationService.generateDocument(id, documentPath, generatedByStaffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error generating document: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Record fee payment
     * POST /api/enrollment-verifications/{id}/record-payment
     */
    @PostMapping("/{id}/record-payment")
    public ResponseEntity<EnrollmentVerification> recordFeePayment(
            @PathVariable Long id,
            @RequestParam Long updatedByStaffId) {
        try {
            EnrollmentVerification updated = verificationService.recordFeePayment(id, updatedByStaffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error recording payment: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Send verification
     * POST /api/enrollment-verifications/{id}/send
     */
    @PostMapping("/{id}/send")
    public ResponseEntity<EnrollmentVerification> sendVerification(
            @PathVariable Long id,
            @RequestParam String deliveryMethod,
            @RequestParam(required = false) String trackingNumber,
            @RequestParam Long sentByStaffId) {
        try {
            EnrollmentVerification updated = verificationService.sendVerification(
                    id, deliveryMethod, trackingNumber, sentByStaffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error sending verification: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Mark as delivered
     * POST /api/enrollment-verifications/{id}/mark-delivered
     */
    @PostMapping("/{id}/mark-delivered")
    public ResponseEntity<EnrollmentVerification> markAsDelivered(
            @PathVariable Long id,
            @RequestParam(required = false) String trackingNumber,
            @RequestParam Long updatedByStaffId) {
        try {
            EnrollmentVerification updated = verificationService.markAsDelivered(id, trackingNumber, updatedByStaffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error marking as delivered: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Cancel verification
     * POST /api/enrollment-verifications/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<EnrollmentVerification> cancelVerification(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestParam Long cancelledByStaffId) {
        try {
            EnrollmentVerification updated = verificationService.cancelVerification(id, reason, cancelledByStaffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error cancelling verification: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================================================
    // STATISTICS & REPORTING
    // ========================================================================

    /**
     * Get verification statistics
     * GET /api/enrollment-verifications/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<EnrollmentVerificationService.VerificationStatistics> getStatistics() {
        EnrollmentVerificationService.VerificationStatistics stats = verificationService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get count by purpose
     * GET /api/enrollment-verifications/count-by-purpose
     */
    @GetMapping("/count-by-purpose")
    public ResponseEntity<List<Object[]>> getCountByPurpose() {
        List<Object[]> counts = verificationService.getCountByPurpose();
        return ResponseEntity.ok(counts);
    }

    // ========================================================================
    // DELETE OPERATIONS
    // ========================================================================

    /**
     * Delete verification (draft only)
     * DELETE /api/enrollment-verifications/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVerification(@PathVariable Long id) {
        try {
            verificationService.deleteVerification(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Error deleting verification: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
