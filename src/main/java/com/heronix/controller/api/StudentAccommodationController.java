package com.heronix.controller.api;

import com.heronix.model.domain.StudentAccommodation;
import com.heronix.model.domain.StudentAccommodation.AccommodationType;
import com.heronix.model.domain.StudentAccommodation.AccommodationStatus;
import com.heronix.service.StudentAccommodationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Student Accommodations
 *
 * Provides endpoints for managing:
 * - 504 Plans
 * - IEPs
 * - ELL/ESL Programs
 * - Gifted & Talented
 * - At-Risk Students
 * - Title I
 * - Homeless Status (McKinney-Vento)
 * - Foster Care
 * - Military Families
 * - Lunch Programs
 * - Transportation
 * - Accessibility
 * - Assistive Technology
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@RestController
@RequestMapping("/api/student-accommodations")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class StudentAccommodationController {

    private final StudentAccommodationService accommodationService;

    // ========================================================================
    // CRUD OPERATIONS
    // ========================================================================

    /**
     * Create new accommodation
     * POST /api/student-accommodations
     */
    @PostMapping
    public ResponseEntity<StudentAccommodation> createAccommodation(
            @RequestParam Long studentId,
            @RequestParam AccommodationType type,
            @RequestParam Long createdByStaffId) {
        try {
            log.info("API: Creating {} accommodation for student {}", type, studentId);
            StudentAccommodation accommodation = accommodationService.createAccommodation(
                    studentId, type, createdByStaffId);
            return ResponseEntity.status(HttpStatus.CREATED).body(accommodation);
        } catch (IllegalArgumentException e) {
            log.error("Invalid input: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get accommodation by ID
     * GET /api/student-accommodations/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<StudentAccommodation> getAccommodationById(@PathVariable Long id) {
        return accommodationService.getAccommodationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update accommodation
     * PUT /api/student-accommodations/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<StudentAccommodation> updateAccommodation(
            @PathVariable Long id,
            @RequestBody StudentAccommodation accommodation,
            @RequestParam Long staffId) {
        try {
            accommodation.setId(id);
            StudentAccommodation updated = accommodationService.updateAccommodation(accommodation, staffId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.error("Update failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete accommodation
     * DELETE /api/student-accommodations/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccommodation(@PathVariable Long id) {
        try {
            accommodationService.deleteAccommodation(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all accommodations
     * GET /api/student-accommodations
     */
    @GetMapping
    public ResponseEntity<List<StudentAccommodation>> getAllAccommodations() {
        return ResponseEntity.ok(accommodationService.getAllAccommodations());
    }

    // ========================================================================
    // STUDENT-SPECIFIC QUERIES
    // ========================================================================

    /**
     * Get all accommodations for a student
     * GET /api/student-accommodations/student/{studentId}
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudentAccommodation>> getAccommodationsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(accommodationService.getAccommodationsByStudent(studentId));
    }

    /**
     * Get active accommodations for a student
     * GET /api/student-accommodations/student/{studentId}/active
     */
    @GetMapping("/student/{studentId}/active")
    public ResponseEntity<List<StudentAccommodation>> getActiveAccommodationsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(accommodationService.getActiveAccommodationsByStudent(studentId));
    }

    /**
     * Get accommodations by student and type
     * GET /api/student-accommodations/student/{studentId}/type/{type}
     */
    @GetMapping("/student/{studentId}/type/{type}")
    public ResponseEntity<List<StudentAccommodation>> getAccommodationsByStudentAndType(
            @PathVariable Long studentId,
            @PathVariable AccommodationType type) {
        return ResponseEntity.ok(accommodationService.getAccommodationsByStudentAndType(studentId, type));
    }

    // ========================================================================
    // STATUS MANAGEMENT
    // ========================================================================

    /**
     * Activate accommodation
     * POST /api/student-accommodations/{id}/activate
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<StudentAccommodation> activateAccommodation(
            @PathVariable Long id,
            @RequestParam Long staffId) {
        try {
            StudentAccommodation activated = accommodationService.activateAccommodation(id, staffId);
            return ResponseEntity.ok(activated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deactivate accommodation
     * POST /api/student-accommodations/{id}/deactivate
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<StudentAccommodation> deactivateAccommodation(
            @PathVariable Long id,
            @RequestParam Long staffId) {
        try {
            StudentAccommodation deactivated = accommodationService.deactivateAccommodation(id, staffId);
            return ResponseEntity.ok(deactivated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Expire accommodation
     * POST /api/student-accommodations/{id}/expire
     */
    @PostMapping("/{id}/expire")
    public ResponseEntity<StudentAccommodation> expireAccommodation(
            @PathVariable Long id,
            @RequestParam Long staffId) {
        try {
            StudentAccommodation expired = accommodationService.expireAccommodation(id, staffId);
            return ResponseEntity.ok(expired);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get accommodations by status
     * GET /api/student-accommodations/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<StudentAccommodation>> getAccommodationsByStatus(@PathVariable AccommodationStatus status) {
        return ResponseEntity.ok(accommodationService.getAccommodationsByStatus(status));
    }

    // ========================================================================
    // TYPE-SPECIFIC QUERIES
    // ========================================================================

    /**
     * Get all students with 504 Plans
     * GET /api/student-accommodations/504-plans
     */
    @GetMapping("/504-plans")
    public ResponseEntity<List<StudentAccommodation>> getStudentsWith504Plans() {
        return ResponseEntity.ok(accommodationService.getStudentsWith504Plans());
    }

    /**
     * Get all students with IEPs
     * GET /api/student-accommodations/ieps
     */
    @GetMapping("/ieps")
    public ResponseEntity<List<StudentAccommodation>> getStudentsWithIEPs() {
        return ResponseEntity.ok(accommodationService.getStudentsWithIEPs());
    }

    /**
     * Get all ELL students
     * GET /api/student-accommodations/ell
     */
    @GetMapping("/ell")
    public ResponseEntity<List<StudentAccommodation>> getELLStudents() {
        return ResponseEntity.ok(accommodationService.getELLStudents());
    }

    /**
     * Get all gifted students
     * GET /api/student-accommodations/gifted
     */
    @GetMapping("/gifted")
    public ResponseEntity<List<StudentAccommodation>> getGiftedStudents() {
        return ResponseEntity.ok(accommodationService.getGiftedStudents());
    }

    /**
     * Get all at-risk students
     * GET /api/student-accommodations/at-risk
     */
    @GetMapping("/at-risk")
    public ResponseEntity<List<StudentAccommodation>> getAtRiskStudents() {
        return ResponseEntity.ok(accommodationService.getAtRiskStudents());
    }

    /**
     * Get all Title I students
     * GET /api/student-accommodations/title-i
     */
    @GetMapping("/title-i")
    public ResponseEntity<List<StudentAccommodation>> getTitleIStudents() {
        return ResponseEntity.ok(accommodationService.getTitleIStudents());
    }

    /**
     * Get all homeless students (McKinney-Vento)
     * GET /api/student-accommodations/homeless
     */
    @GetMapping("/homeless")
    public ResponseEntity<List<StudentAccommodation>> getHomelessStudents() {
        return ResponseEntity.ok(accommodationService.getHomelessStudents());
    }

    /**
     * Get all foster care students
     * GET /api/student-accommodations/foster-care
     */
    @GetMapping("/foster-care")
    public ResponseEntity<List<StudentAccommodation>> getFosterCareStudents() {
        return ResponseEntity.ok(accommodationService.getFosterCareStudents());
    }

    /**
     * Get all military family students
     * GET /api/student-accommodations/military-family
     */
    @GetMapping("/military-family")
    public ResponseEntity<List<StudentAccommodation>> getMilitaryFamilyStudents() {
        return ResponseEntity.ok(accommodationService.getMilitaryFamilyStudents());
    }

    // ========================================================================
    // REVIEW & EXPIRATION MANAGEMENT
    // ========================================================================

    /**
     * Get accommodations with overdue reviews
     * GET /api/student-accommodations/reviews/overdue
     */
    @GetMapping("/reviews/overdue")
    public ResponseEntity<List<StudentAccommodation>> getOverdueReviews() {
        return ResponseEntity.ok(accommodationService.getOverdueReviews());
    }

    /**
     * Get accommodations expiring soon
     * GET /api/student-accommodations/expiring-soon
     */
    @GetMapping("/expiring-soon")
    public ResponseEntity<List<StudentAccommodation>> getExpiringSoon(@RequestParam int days) {
        return ResponseEntity.ok(accommodationService.getExpiringSoon(days));
    }

    /**
     * Get expired accommodations
     * GET /api/student-accommodations/expired
     */
    @GetMapping("/expired")
    public ResponseEntity<List<StudentAccommodation>> getExpiredAccommodations() {
        return ResponseEntity.ok(accommodationService.getExpiredAccommodations());
    }

    /**
     * Schedule review
     * POST /api/student-accommodations/{id}/schedule-review
     */
    @PostMapping("/{id}/schedule-review")
    public ResponseEntity<StudentAccommodation> scheduleReview(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reviewDate,
            @RequestParam Long staffId) {
        try {
            StudentAccommodation updated = accommodationService.scheduleReview(id, reviewDate, staffId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Record review completion
     * POST /api/student-accommodations/{id}/complete-review
     */
    @PostMapping("/{id}/complete-review")
    public ResponseEntity<StudentAccommodation> recordReviewCompletion(
            @PathVariable Long id,
            @RequestParam Long staffId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nextReviewDate) {
        try {
            StudentAccommodation updated = accommodationService.recordReviewCompletion(id, staffId, nextReviewDate);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========================================================================
    // COORDINATOR MANAGEMENT
    // ========================================================================

    /**
     * Assign coordinator
     * POST /api/student-accommodations/{id}/assign-coordinator
     */
    @PostMapping("/{id}/assign-coordinator")
    public ResponseEntity<StudentAccommodation> assignCoordinator(
            @PathVariable Long id,
            @RequestParam Long coordinatorId,
            @RequestParam Long staffId) {
        try {
            StudentAccommodation updated = accommodationService.assignCoordinator(id, coordinatorId, staffId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get accommodations by coordinator
     * GET /api/student-accommodations/coordinator/{coordinatorId}
     */
    @GetMapping("/coordinator/{coordinatorId}")
    public ResponseEntity<List<StudentAccommodation>> getAccommodationsByCoordinator(@PathVariable Long coordinatorId) {
        return ResponseEntity.ok(accommodationService.getAccommodationsByCoordinator(coordinatorId));
    }

    /**
     * Get active accommodations by coordinator
     * GET /api/student-accommodations/coordinator/{coordinatorId}/active
     */
    @GetMapping("/coordinator/{coordinatorId}/active")
    public ResponseEntity<List<StudentAccommodation>> getActiveAccommodationsByCoordinator(@PathVariable Long coordinatorId) {
        return ResponseEntity.ok(accommodationService.getActiveAccommodationsByCoordinator(coordinatorId));
    }

    // ========================================================================
    // LUNCH STATUS
    // ========================================================================

    /**
     * Get students by lunch status
     * GET /api/student-accommodations/lunch-status/{status}
     */
    @GetMapping("/lunch-status/{status}")
    public ResponseEntity<List<StudentAccommodation>> getStudentsByLunchStatus(
            @PathVariable StudentAccommodation.LunchStatus status) {
        return ResponseEntity.ok(accommodationService.getStudentsByLunchStatus(status));
    }

    /**
     * Count students by lunch status
     * GET /api/student-accommodations/lunch-status/{status}/count
     */
    @GetMapping("/lunch-status/{status}/count")
    public ResponseEntity<Long> countStudentsByLunchStatus(
            @PathVariable StudentAccommodation.LunchStatus status) {
        return ResponseEntity.ok(accommodationService.countStudentsByLunchStatus(status));
    }

    // ========================================================================
    // TRANSPORTATION
    // ========================================================================

    /**
     * Get students requiring special transportation
     * GET /api/student-accommodations/transportation/special
     */
    @GetMapping("/transportation/special")
    public ResponseEntity<List<StudentAccommodation>> getStudentsRequiringSpecialTransportation() {
        return ResponseEntity.ok(accommodationService.getStudentsRequiringSpecialTransportation());
    }

    /**
     * Get students by bus number
     * GET /api/student-accommodations/transportation/bus/{busNumber}
     */
    @GetMapping("/transportation/bus/{busNumber}")
    public ResponseEntity<List<StudentAccommodation>> getStudentsByBusNumber(@PathVariable String busNumber) {
        return ResponseEntity.ok(accommodationService.getStudentsByBusNumber(busNumber));
    }

    // ========================================================================
    // ASSISTIVE TECHNOLOGY & ACCESSIBILITY
    // ========================================================================

    /**
     * Get students requiring assistive technology
     * GET /api/student-accommodations/assistive-technology
     */
    @GetMapping("/assistive-technology")
    public ResponseEntity<List<StudentAccommodation>> getStudentsRequiringAssistiveTechnology() {
        return ResponseEntity.ok(accommodationService.getStudentsRequiringAssistiveTechnology());
    }

    /**
     * Get students requiring accessibility accommodations
     * GET /api/student-accommodations/accessibility
     */
    @GetMapping("/accessibility")
    public ResponseEntity<List<StudentAccommodation>> getStudentsRequiringAccessibilityAccommodations() {
        return ResponseEntity.ok(accommodationService.getStudentsRequiringAccessibilityAccommodations());
    }

    // ========================================================================
    // STATISTICS
    // ========================================================================

    /**
     * Get accommodation statistics
     * GET /api/student-accommodations/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getAccommodationStatistics() {
        return ResponseEntity.ok(accommodationService.getAccommodationStatistics());
    }

    /**
     * Count accommodations by type
     * GET /api/student-accommodations/count/type/{type}
     */
    @GetMapping("/count/type/{type}")
    public ResponseEntity<Long> countByType(@PathVariable AccommodationType type) {
        return ResponseEntity.ok(accommodationService.countByType(type));
    }

    /**
     * Count active accommodations
     * GET /api/student-accommodations/count/active
     */
    @GetMapping("/count/active")
    public ResponseEntity<Long> countActive() {
        return ResponseEntity.ok(accommodationService.countActive());
    }

    // ========================================================================
    // BULK OPERATIONS
    // ========================================================================

    /**
     * Bulk activate accommodations
     * POST /api/student-accommodations/bulk/activate
     */
    @PostMapping("/bulk/activate")
    public ResponseEntity<List<StudentAccommodation>> bulkActivate(
            @RequestBody List<Long> accommodationIds,
            @RequestParam Long staffId) {
        try {
            List<StudentAccommodation> activated = accommodationService.bulkActivate(accommodationIds, staffId);
            return ResponseEntity.ok(activated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Bulk deactivate accommodations
     * POST /api/student-accommodations/bulk/deactivate
     */
    @PostMapping("/bulk/deactivate")
    public ResponseEntity<List<StudentAccommodation>> bulkDeactivate(
            @RequestBody List<Long> accommodationIds,
            @RequestParam Long staffId) {
        try {
            List<StudentAccommodation> deactivated = accommodationService.bulkDeactivate(accommodationIds, staffId);
            return ResponseEntity.ok(deactivated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Bulk assign coordinator
     * POST /api/student-accommodations/bulk/assign-coordinator
     */
    @PostMapping("/bulk/assign-coordinator")
    public ResponseEntity<List<StudentAccommodation>> bulkAssignCoordinator(
            @RequestBody List<Long> accommodationIds,
            @RequestParam Long coordinatorId,
            @RequestParam Long staffId) {
        try {
            List<StudentAccommodation> updated = accommodationService.bulkAssignCoordinator(
                    accommodationIds, coordinatorId, staffId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
