package com.heronix.controller;

import com.heronix.model.domain.EnrollmentApplication;
import com.heronix.model.domain.EnrollmentApplication.ApplicationStatus;
import com.heronix.model.domain.EnrollmentApplication.EnrollmentType;
import com.heronix.model.domain.Student;
import com.heronix.service.EnrollmentApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Enrollment Application Management
 * Provides endpoints for in-house enrollment application processing
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Slf4j
// @RestController  // Disabled for API server mode
@RequestMapping("/api/enrollment-applications")
@CrossOrigin(origins = "*")
public class EnrollmentApplicationController {

    @Autowired
    private EnrollmentApplicationService enrollmentApplicationService;

    // ========================================================================
    // CREATE OPERATIONS
    // ========================================================================

    /**
     * Create new enrollment application
     * POST /api/enrollment-applications
     */
    @PostMapping
    public ResponseEntity<EnrollmentApplication> createApplication(@RequestBody Map<String, Object> request) {
        try {
            log.info("Creating enrollment application: type={}, grade={}",
                    request.get("enrollmentType"), request.get("intendedGradeLevel"));

            EnrollmentType enrollmentType = EnrollmentType.valueOf((String) request.get("enrollmentType"));
            String intendedGradeLevel = (String) request.get("intendedGradeLevel");
            String intendedSchoolYear = (String) request.get("intendedSchoolYear");
            Long createdByStaffId = Long.parseLong(request.get("createdByStaffId").toString());

            EnrollmentApplication application = enrollmentApplicationService.createApplication(
                    enrollmentType, intendedGradeLevel, intendedSchoolYear, createdByStaffId);

            return ResponseEntity.status(HttpStatus.CREATED).body(application);
        } catch (Exception e) {
            log.error("Error creating enrollment application: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================================================
    // READ OPERATIONS
    // ========================================================================

    /**
     * Get application by ID
     * GET /api/enrollment-applications/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentApplication> getApplicationById(@PathVariable Long id) {
        try {
            EnrollmentApplication application = enrollmentApplicationService.getApplicationById(id);
            return ResponseEntity.ok(application);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get application by application number
     * GET /api/enrollment-applications/number/{applicationNumber}
     */
    @GetMapping("/number/{applicationNumber}")
    public ResponseEntity<EnrollmentApplication> getByApplicationNumber(@PathVariable String applicationNumber) {
        try {
            EnrollmentApplication application = enrollmentApplicationService.getApplicationByNumber(applicationNumber);
            return ResponseEntity.ok(application);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all applications by status
     * GET /api/enrollment-applications/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<EnrollmentApplication>> getByStatus(@PathVariable ApplicationStatus status) {
        List<EnrollmentApplication> applications = enrollmentApplicationService.getApplicationsByStatus(status);
        return ResponseEntity.ok(applications);
    }

    /**
     * Get applications ready for approval
     * GET /api/enrollment-applications/ready-for-approval
     */
    @GetMapping("/ready-for-approval")
    public ResponseEntity<List<EnrollmentApplication>> getApplicationsReadyForApproval() {
        List<EnrollmentApplication> applications = enrollmentApplicationService.getApplicationsReadyForApproval();
        return ResponseEntity.ok(applications);
    }

    /**
     * Get applications awaiting documents
     * GET /api/enrollment-applications/awaiting-documents
     */
    @GetMapping("/awaiting-documents")
    public ResponseEntity<List<EnrollmentApplication>> getApplicationsAwaitingDocuments() {
        List<EnrollmentApplication> applications = enrollmentApplicationService.getApplicationsAwaitingDocuments();
        return ResponseEntity.ok(applications);
    }

    /**
     * Search applications by name
     * GET /api/enrollment-applications/search?name={name}
     */
    @GetMapping("/search")
    public ResponseEntity<List<EnrollmentApplication>> searchByName(@RequestParam String name) {
        List<EnrollmentApplication> applications = enrollmentApplicationService.searchByName(name);
        return ResponseEntity.ok(applications);
    }

    /**
     * Get enrollment statistics
     * GET /api/enrollment-applications/statistics?schoolYear={schoolYear}
     */
    @GetMapping("/statistics")
    public ResponseEntity<EnrollmentApplicationService.EnrollmentStatistics> getStatistics(
            @RequestParam(required = false) String schoolYear) {
        EnrollmentApplicationService.EnrollmentStatistics stats = enrollmentApplicationService.getStatistics(schoolYear);
        return ResponseEntity.ok(stats);
    }

    // ========================================================================
    // UPDATE OPERATIONS
    // ========================================================================

    /**
     * Update application
     * PUT /api/enrollment-applications/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<EnrollmentApplication> updateApplication(
            @PathVariable Long id,
            @RequestBody EnrollmentApplication updates,
            @RequestParam Long staffId) {
        try {
            EnrollmentApplication updated = enrollmentApplicationService.updateApplication(id, updates, staffId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Submit application for documents
     * POST /api/enrollment-applications/{id}/submit-for-documents
     */
    @PostMapping("/{id}/submit-for-documents")
    public ResponseEntity<EnrollmentApplication> submitForDocuments(
            @PathVariable Long id,
            @RequestParam Long staffId) {
        try {
            EnrollmentApplication updated = enrollmentApplicationService.submitForDocuments(id, staffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error submitting application for documents: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Move application to verification
     * POST /api/enrollment-applications/{id}/move-to-verification
     */
    @PostMapping("/{id}/move-to-verification")
    public ResponseEntity<EnrollmentApplication> moveToVerification(
            @PathVariable Long id,
            @RequestParam Long staffId) {
        try {
            EnrollmentApplication updated = enrollmentApplicationService.moveToVerification(id, staffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error moving application to verification: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Request previous school records
     * POST /api/enrollment-applications/{id}/request-records
     */
    @PostMapping("/{id}/request-records")
    public ResponseEntity<EnrollmentApplication> requestPreviousSchoolRecords(
            @PathVariable Long id,
            @RequestParam Long staffId) {
        try {
            EnrollmentApplication updated = enrollmentApplicationService.requestPreviousSchoolRecords(id, staffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error requesting previous school records: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Submit application for approval
     * POST /api/enrollment-applications/{id}/submit-for-approval
     */
    @PostMapping("/{id}/submit-for-approval")
    public ResponseEntity<EnrollmentApplication> submitForApproval(
            @PathVariable Long id,
            @RequestParam Long staffId) {
        try {
            EnrollmentApplication updated = enrollmentApplicationService.submitForApproval(id, staffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error submitting application for approval: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Approve application
     * POST /api/enrollment-applications/{id}/approve
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<EnrollmentApplication> approveApplication(
            @PathVariable Long id,
            @RequestParam Long adminStaffId,
            @RequestParam(required = false) String approvalNotes) {
        try {
            EnrollmentApplication updated = enrollmentApplicationService.approveApplication(
                    id, adminStaffId, approvalNotes);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error approving application: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Reject application
     * POST /api/enrollment-applications/{id}/reject
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<EnrollmentApplication> rejectApplication(
            @PathVariable Long id,
            @RequestParam Long adminStaffId,
            @RequestParam String rejectionReason) {
        try {
            EnrollmentApplication updated = enrollmentApplicationService.rejectApplication(
                    id, adminStaffId, rejectionReason);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error rejecting application: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Withdraw application
     * POST /api/enrollment-applications/{id}/withdraw
     */
    @PostMapping("/{id}/withdraw")
    public ResponseEntity<EnrollmentApplication> withdrawApplication(
            @PathVariable Long id,
            @RequestParam Long staffId,
            @RequestParam String reason) {
        try {
            EnrollmentApplication updated = enrollmentApplicationService.withdrawApplication(id, staffId, reason);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error withdrawing application: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Enroll student from approved application
     * POST /api/enrollment-applications/{id}/enroll
     */
    @PostMapping("/{id}/enroll")
    public ResponseEntity<Student> enrollStudent(
            @PathVariable Long id,
            @RequestParam Long staffId) {
        try {
            Student student = enrollmentApplicationService.enrollStudent(id, staffId);
            return ResponseEntity.status(HttpStatus.CREATED).body(student);
        } catch (Exception e) {
            log.error("Error enrolling student: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
