package com.heronix.controller;

import com.heronix.model.domain.PreRegistration;
import com.heronix.model.domain.PreRegistration.RegistrationStatus;
import com.heronix.service.PreRegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Pre-Registration Management
 * Provides endpoints for pre-registration for upcoming school year
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Slf4j
@RestController
@RequestMapping("/api/pre-registrations")
@CrossOrigin(origins = "*")
public class PreRegistrationController {

    @Autowired
    private PreRegistrationService preRegistrationService;

    // ========================================================================
    // CREATE OPERATIONS
    // ========================================================================

    /**
     * Create new pre-registration
     * POST /api/pre-registrations
     */
    @PostMapping
    public ResponseEntity<PreRegistration> createPreRegistration(@RequestBody Map<String, Object> request) {
        try {
            log.info("Creating pre-registration for student ID: {} for school year: {}",
                    request.get("studentId"), request.get("targetSchoolYear"));

            Long studentId = Long.parseLong(request.get("studentId").toString());
            String targetSchoolYear = (String) request.get("targetSchoolYear");
            Long createdByStaffId = Long.parseLong(request.get("createdByStaffId").toString());

            PreRegistration preRegistration = preRegistrationService.createPreRegistration(
                    studentId, targetSchoolYear, createdByStaffId);

            return ResponseEntity.status(HttpStatus.CREATED).body(preRegistration);
        } catch (Exception e) {
            log.error("Error creating pre-registration: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================================================
    // READ OPERATIONS
    // ========================================================================

    /**
     * Get pre-registration by ID
     * GET /api/pre-registrations/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PreRegistration> getPreRegistrationById(@PathVariable Long id) {
        try {
            PreRegistration preRegistration = preRegistrationService.getPreRegistrationById(id);
            return ResponseEntity.ok(preRegistration);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get pre-registration by registration number
     * GET /api/pre-registrations/number/{registrationNumber}
     */
    @GetMapping("/number/{registrationNumber}")
    public ResponseEntity<PreRegistration> getByRegistrationNumber(@PathVariable String registrationNumber) {
        try {
            PreRegistration preRegistration = preRegistrationService.getByRegistrationNumber(registrationNumber);
            return ResponseEntity.ok(preRegistration);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all pre-registrations by status
     * GET /api/pre-registrations/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PreRegistration>> getByStatus(@PathVariable RegistrationStatus status) {
        List<PreRegistration> preRegistrations = preRegistrationService.getByStatus(status);
        return ResponseEntity.ok(preRegistrations);
    }

    /**
     * Get all pre-registrations for student
     * GET /api/pre-registrations/student/{studentId}
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<PreRegistration>> getByStudent(@PathVariable Long studentId) {
        try {
            List<PreRegistration> preRegistrations = preRegistrationService.getByStudent(studentId);
            return ResponseEntity.ok(preRegistrations);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all pre-registrations for target school year
     * GET /api/pre-registrations/school-year/{targetSchoolYear}
     */
    @GetMapping("/school-year/{targetSchoolYear}")
    public ResponseEntity<List<PreRegistration>> getByTargetSchoolYear(@PathVariable String targetSchoolYear) {
        List<PreRegistration> preRegistrations = preRegistrationService.getByTargetSchoolYear(targetSchoolYear);
        return ResponseEntity.ok(preRegistrations);
    }

    /**
     * Get pre-registrations awaiting review
     * GET /api/pre-registrations/awaiting-review
     */
    @GetMapping("/awaiting-review")
    public ResponseEntity<List<PreRegistration>> getAwaitingReview() {
        List<PreRegistration> preRegistrations = preRegistrationService.getAwaitingReview();
        return ResponseEntity.ok(preRegistrations);
    }

    /**
     * Search pre-registrations by student name
     * GET /api/pre-registrations/search?name={name}
     */
    @GetMapping("/search")
    public ResponseEntity<List<PreRegistration>> searchByStudentName(@RequestParam String name) {
        List<PreRegistration> preRegistrations = preRegistrationService.searchByStudentName(name);
        return ResponseEntity.ok(preRegistrations);
    }

    /**
     * Get enrollment statistics for school year
     * GET /api/pre-registrations/statistics?schoolYear={schoolYear}
     */
    @GetMapping("/statistics")
    public ResponseEntity<PreRegistrationService.EnrollmentStatistics> getStatistics(
            @RequestParam String schoolYear) {
        PreRegistrationService.EnrollmentStatistics stats = preRegistrationService.getStatistics(schoolYear);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get enrollment counts by grade level
     * GET /api/pre-registrations/counts-by-grade?schoolYear={schoolYear}
     */
    @GetMapping("/counts-by-grade")
    public ResponseEntity<List<Object[]>> getEnrollmentCountsByGrade(@RequestParam String schoolYear) {
        List<Object[]> counts = preRegistrationService.getEnrollmentCountsByGrade(schoolYear);
        return ResponseEntity.ok(counts);
    }

    // ========================================================================
    // UPDATE OPERATIONS
    // ========================================================================

    /**
     * Update pre-registration
     * PUT /api/pre-registrations/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<PreRegistration> updatePreRegistration(
            @PathVariable Long id,
            @RequestBody PreRegistration preRegistration,
            @RequestParam Long staffId) {
        try {
            preRegistration.setId(id); // Ensure ID matches path
            PreRegistration updated = preRegistrationService.updatePreRegistration(preRegistration, staffId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Submit pre-registration for review
     * POST /api/pre-registrations/{id}/submit
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<PreRegistration> submitForReview(
            @PathVariable Long id,
            @RequestParam Long staffId) {
        try {
            PreRegistration updated = preRegistrationService.submitForReview(id, staffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error submitting pre-registration for review: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Begin review of pre-registration
     * POST /api/pre-registrations/{id}/begin-review
     */
    @PostMapping("/{id}/begin-review")
    public ResponseEntity<PreRegistration> beginReview(
            @PathVariable Long id,
            @RequestParam Long staffId) {
        try {
            PreRegistration updated = preRegistrationService.beginReview(id, staffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error beginning review: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Approve pre-registration
     * POST /api/pre-registrations/{id}/approve
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<PreRegistration> approve(
            @PathVariable Long id,
            @RequestParam Long staffId,
            @RequestParam(required = false) String approvalNotes) {
        try {
            PreRegistration updated = preRegistrationService.approve(id, staffId, approvalNotes);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error approving pre-registration: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Confirm enrollment
     * POST /api/pre-registrations/{id}/confirm
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<PreRegistration> confirmEnrollment(
            @PathVariable Long id,
            @RequestParam Long staffId) {
        try {
            PreRegistration updated = preRegistrationService.confirmEnrollment(id, staffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error confirming enrollment: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Cancel pre-registration
     * POST /api/pre-registrations/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<PreRegistration> cancel(
            @PathVariable Long id,
            @RequestParam Long staffId,
            @RequestParam String reason) {
        try {
            PreRegistration updated = preRegistrationService.cancel(id, staffId, reason);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error cancelling pre-registration: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Move to waitlist
     * POST /api/pre-registrations/{id}/waitlist
     */
    @PostMapping("/{id}/waitlist")
    public ResponseEntity<PreRegistration> moveToWaitlist(
            @PathVariable Long id,
            @RequestParam Long staffId) {
        try {
            PreRegistration updated = preRegistrationService.moveToWaitlist(id, staffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error moving to waitlist: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================================================
    // DELETE OPERATIONS
    // ========================================================================

    /**
     * Delete pre-registration (only DRAFT)
     * DELETE /api/pre-registrations/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePreRegistration(@PathVariable Long id) {
        try {
            preRegistrationService.deletePreRegistration(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            log.error("Cannot delete pre-registration: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
