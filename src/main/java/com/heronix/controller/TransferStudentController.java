package com.heronix.controller;

import com.heronix.model.domain.TransferStudent;
import com.heronix.model.domain.TransferStudent.TransferStatus;
import com.heronix.service.TransferStudentService;
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
 * REST API Controller for Transfer Student Management
 * Provides endpoints for incoming transfer student processing
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
// @RestController  // Disabled for API server mode
@RequestMapping("/api/transfer-students")
@CrossOrigin(origins = "*")
public class TransferStudentController {

    @Autowired
    private TransferStudentService transferStudentService;

    // ========================================================================
    // CREATE OPERATIONS
    // ========================================================================

    /**
     * Create new transfer student record
     * POST /api/transfer-students
     */
    @PostMapping
    public ResponseEntity<TransferStudent> createTransferStudent(@RequestBody Map<String, Object> request) {
        try {
            log.info("Creating transfer student: {}", request.get("studentFirstName"));

            TransferStudent transfer = transferStudentService.createTransferRecord(
                    (String) request.get("studentFirstName"),
                    (String) request.get("studentLastName"),
                    LocalDate.parse((String) request.get("dateOfBirth")),
                    LocalDate.parse((String) request.get("transferDate")),
                    (String) request.get("previousSchoolName"),
                    Long.parseLong(request.get("createdByStaffId").toString())
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(transfer);
        } catch (Exception e) {
            log.error("Error creating transfer student: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================================================
    // READ OPERATIONS
    // ========================================================================

    /**
     * Get transfer student by ID
     * GET /api/transfer-students/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransferStudent> getTransferStudentById(@PathVariable Long id) {
        try {
            TransferStudent transfer = transferStudentService.getTransferById(id);
            return ResponseEntity.ok(transfer);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get transfer student by transfer number
     * GET /api/transfer-students/number/{transferNumber}
     */
    @GetMapping("/number/{transferNumber}")
    public ResponseEntity<TransferStudent> getByTransferNumber(@PathVariable String transferNumber) {
        try {
            TransferStudent transfer = transferStudentService.getByTransferNumber(transferNumber);
            return ResponseEntity.ok(transfer);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all transfer students by status
     * GET /api/transfer-students/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TransferStudent>> getByStatus(@PathVariable TransferStatus status) {
        List<TransferStudent> transfers = transferStudentService.getByStatus(status);
        return ResponseEntity.ok(transfers);
    }

    /**
     * Get transfers pending records
     * GET /api/transfer-students/pending-records
     */
    @GetMapping("/pending-records")
    public ResponseEntity<List<TransferStudent>> getPendingRecords() {
        List<TransferStudent> transfers = transferStudentService.getPendingRecords();
        return ResponseEntity.ok(transfers);
    }

    /**
     * Get transfers ready for placement
     * GET /api/transfer-students/ready-for-placement
     */
    @GetMapping("/ready-for-placement")
    public ResponseEntity<List<TransferStudent>> getReadyForPlacement() {
        List<TransferStudent> transfers = transferStudentService.getReadyForPlacement();
        return ResponseEntity.ok(transfers);
    }

    /**
     * Get transfers by assigned counselor
     * GET /api/transfer-students/counselor/{counselorId}
     */
    @GetMapping("/counselor/{counselorId}")
    public ResponseEntity<List<TransferStudent>> getByCounselor(@PathVariable Long counselorId) {
        List<TransferStudent> transfers = transferStudentService.getByCounselor(counselorId);
        return ResponseEntity.ok(transfers);
    }

    /**
     * Search transfers by student name
     * GET /api/transfer-students/search?name={name}
     */
    @GetMapping("/search")
    public ResponseEntity<List<TransferStudent>> searchByName(@RequestParam String name) {
        List<TransferStudent> transfers = transferStudentService.searchByStudentName(name);
        return ResponseEntity.ok(transfers);
    }

    // ========================================================================
    // UPDATE OPERATIONS
    // ========================================================================

    /**
     * Update transfer student
     * PUT /api/transfer-students/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransferStudent> updateTransferStudent(
            @PathVariable Long id,
            @RequestBody TransferStudent transfer,
            @RequestParam Long updatedByStaffId) {
        try {
            transfer.setId(id);
            TransferStudent updated = transferStudentService.updateTransfer(transfer, updatedByStaffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating transfer student: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Mark record as received
     * POST /api/transfer-students/{id}/records/{recordType}
     */
    @PostMapping("/{id}/records/{recordType}")
    public ResponseEntity<TransferStudent> markRecordReceived(
            @PathVariable Long id,
            @PathVariable String recordType,
            @RequestParam Long updatedByStaffId) {
        try {
            TransferStudent updated = transferStudentService.markRecordReceived(id, recordType, updatedByStaffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error marking record received: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Assign counselor
     * POST /api/transfer-students/{id}/assign-counselor
     */
    @PostMapping("/{id}/assign-counselor")
    public ResponseEntity<TransferStudent> assignCounselor(
            @PathVariable Long id,
            @RequestParam Long counselorId,
            @RequestParam Long updatedByStaffId) {
        try {
            TransferStudent updated = transferStudentService.assignCounselor(id, counselorId, updatedByStaffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error assigning counselor: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Propose grade level after placement
     * POST /api/transfer-students/{id}/propose-grade
     */
    @PostMapping("/{id}/propose-grade")
    public ResponseEntity<TransferStudent> proposeGradeLevel(
            @PathVariable Long id,
            @RequestParam String proposedGradeLevel,
            @RequestParam Long updatedByStaffId) {
        try {
            TransferStudent transfer = transferStudentService.getTransferById(id);
            transfer.setProposedGradeLevel(proposedGradeLevel);
            TransferStudent updated = transferStudentService.updateTransfer(transfer, updatedByStaffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error proposing grade level: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Complete transfer enrollment
     * POST /api/transfer-students/{id}/complete
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<TransferStudent> completeEnrollment(
            @PathVariable Long id,
            @RequestParam Long enrolledStudentId,
            @RequestParam String assignedGrade,
            @RequestParam String assignedHomeroom,
            @RequestParam Long completedByStaffId) {
        try {
            TransferStudent updated = transferStudentService.completeEnrollment(
                    id, enrolledStudentId, assignedGrade, assignedHomeroom, completedByStaffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error completing enrollment: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Cancel transfer
     * POST /api/transfer-students/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<TransferStudent> cancelTransfer(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestParam Long cancelledByStaffId) {
        try {
            TransferStudent updated = transferStudentService.cancelTransfer(id, reason, cancelledByStaffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error cancelling transfer: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================================================
    // STATISTICS & REPORTING
    // ========================================================================

    /**
     * Get transfer student statistics
     * GET /api/transfer-students/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<TransferStudentService.TransferStatistics> getStatistics() {
        TransferStudentService.TransferStatistics stats = transferStudentService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get count by previous school
     * GET /api/transfer-students/count-by-school
     */
    @GetMapping("/count-by-school")
    public ResponseEntity<List<Object[]>> getCountBySchool() {
        // This would need to be implemented in the service
        // For now, return empty list
        return ResponseEntity.ok(List.of());
    }

    // ========================================================================
    // DELETE OPERATIONS
    // ========================================================================

    /**
     * Delete transfer student (draft only)
     * DELETE /api/transfer-students/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransferStudent(@PathVariable Long id) {
        try {
            transferStudentService.deleteTransfer(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Error deleting transfer student: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
