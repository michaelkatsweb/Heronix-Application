package com.heronix.controller;

import com.heronix.model.domain.TransferOutDocumentation;
import com.heronix.model.domain.TransferOutDocumentation.TransferOutStatus;
import com.heronix.model.domain.TransferOutDocumentation.TransmissionMethod;
import com.heronix.service.TransferOutDocumentationService;
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
 * REST API Controller for Transfer Out Documentation Management
 * Provides endpoints for outgoing transfer student records
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
// @RestController  // Disabled for API server mode
@RequestMapping("/api/transfer-out")
@CrossOrigin(origins = "*")
public class TransferOutDocumentationController {

    @Autowired
    private TransferOutDocumentationService transferOutService;

    // ========================================================================
    // CREATE OPERATIONS
    // ========================================================================

    /**
     * Create new transfer out documentation
     * POST /api/transfer-out
     */
    @PostMapping
    public ResponseEntity<TransferOutDocumentation> createTransferOut(@RequestBody Map<String, Object> request) {
        try {
            log.info("Creating transfer out for student ID: {}", request.get("studentId"));

            TransferOutDocumentation transferOut = transferOutService.createTransferOut(
                    Long.parseLong(request.get("studentId").toString()),
                    (String) request.get("destinationSchoolName"),
                    LocalDate.parse((String) request.get("expectedTransferDate")),
                    Long.parseLong(request.get("createdByStaffId").toString())
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(transferOut);
        } catch (Exception e) {
            log.error("Error creating transfer out: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================================================
    // READ OPERATIONS
    // ========================================================================

    /**
     * Get transfer out by ID
     * GET /api/transfer-out/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransferOutDocumentation> getTransferOutById(@PathVariable Long id) {
        try {
            TransferOutDocumentation transferOut = transferOutService.getTransferOutById(id);
            return ResponseEntity.ok(transferOut);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get transfer out by number
     * GET /api/transfer-out/number/{transferOutNumber}
     */
    @GetMapping("/number/{transferOutNumber}")
    public ResponseEntity<TransferOutDocumentation> getByTransferOutNumber(@PathVariable String transferOutNumber) {
        try {
            TransferOutDocumentation transferOut = transferOutService.getByTransferOutNumber(transferOutNumber);
            return ResponseEntity.ok(transferOut);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all transfer outs by status
     * GET /api/transfer-out/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TransferOutDocumentation>> getByStatus(@PathVariable TransferOutStatus status) {
        List<TransferOutDocumentation> transferOuts = transferOutService.getByStatus(status);
        return ResponseEntity.ok(transferOuts);
    }

    /**
     * Get transfer outs ready to send
     * GET /api/transfer-out/ready-to-send
     */
    @GetMapping("/ready-to-send")
    public ResponseEntity<List<TransferOutDocumentation>> getReadyToSend() {
        List<TransferOutDocumentation> transferOuts = transferOutService.getReadyToSend();
        return ResponseEntity.ok(transferOuts);
    }

    /**
     * Get transfer outs pending acknowledgment
     * GET /api/transfer-out/pending-acknowledgment
     */
    @GetMapping("/pending-acknowledgment")
    public ResponseEntity<List<TransferOutDocumentation>> getPendingAcknowledgment() {
        List<TransferOutDocumentation> transferOuts = transferOutService.getPendingAcknowledgment();
        return ResponseEntity.ok(transferOuts);
    }

    /**
     * Get transfer outs by assigned staff
     * GET /api/transfer-out/staff/{staffId}
     */
    @GetMapping("/staff/{staffId}")
    public ResponseEntity<List<TransferOutDocumentation>> getByAssignedStaff(@PathVariable Long staffId) {
        List<TransferOutDocumentation> transferOuts = transferOutService.getByAssignedStaff(staffId);
        return ResponseEntity.ok(transferOuts);
    }

    /**
     * Get unassigned transfer outs
     * GET /api/transfer-out/unassigned
     */
    @GetMapping("/unassigned")
    public ResponseEntity<List<TransferOutDocumentation>> getUnassignedTransferOuts() {
        List<TransferOutDocumentation> transferOuts = transferOutService.getUnassignedTransferOuts();
        return ResponseEntity.ok(transferOuts);
    }

    /**
     * Search by destination school
     * GET /api/transfer-out/search?school={schoolName}
     */
    @GetMapping("/search")
    public ResponseEntity<List<TransferOutDocumentation>> searchByDestinationSchool(@RequestParam String school) {
        List<TransferOutDocumentation> transferOuts = transferOutService.searchByDestinationSchool(school);
        return ResponseEntity.ok(transferOuts);
    }

    /**
     * Get transfers needing attention
     * GET /api/transfer-out/needs-attention
     */
    @GetMapping("/needs-attention")
    public ResponseEntity<List<TransferOutDocumentation>> getTransfersNeedingAttention() {
        List<TransferOutDocumentation> transferOuts = transferOutService.getTransfersNeedingAttention();
        return ResponseEntity.ok(transferOuts);
    }

    // ========================================================================
    // UPDATE OPERATIONS
    // ========================================================================

    /**
     * Update transfer out
     * PUT /api/transfer-out/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransferOutDocumentation> updateTransferOut(
            @PathVariable Long id,
            @RequestBody TransferOutDocumentation transferOut,
            @RequestParam Long updatedByStaffId) {
        try {
            transferOut.setId(id);
            TransferOutDocumentation updated = transferOutService.updateTransferOut(transferOut, updatedByStaffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating transfer out: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Start records preparation
     * POST /api/transfer-out/{id}/start-records-prep
     */
    @PostMapping("/{id}/start-records-prep")
    public ResponseEntity<TransferOutDocumentation> startRecordsPreparation(
            @PathVariable Long id,
            @RequestParam Long updatedByStaffId) {
        try {
            TransferOutDocumentation updated = transferOutService.startRecordsPreparation(id, updatedByStaffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error starting records preparation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Mark document as included
     * POST /api/transfer-out/{id}/include-document/{documentType}
     */
    @PostMapping("/{id}/include-document/{documentType}")
    public ResponseEntity<TransferOutDocumentation> markDocumentIncluded(
            @PathVariable Long id,
            @PathVariable String documentType,
            @RequestParam Long updatedByStaffId) {
        try {
            TransferOutDocumentation updated = transferOutService.markDocumentIncluded(id, documentType, updatedByStaffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error marking document included: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Include all documents
     * POST /api/transfer-out/{id}/include-all-documents
     */
    @PostMapping("/{id}/include-all-documents")
    public ResponseEntity<TransferOutDocumentation> includeAllDocuments(
            @PathVariable Long id,
            @RequestParam Long updatedByStaffId) {
        try {
            TransferOutDocumentation updated = transferOutService.includeAllDocuments(id, updatedByStaffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error including all documents: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Assign staff member
     * POST /api/transfer-out/{id}/assign-staff
     */
    @PostMapping("/{id}/assign-staff")
    public ResponseEntity<TransferOutDocumentation> assignStaff(
            @PathVariable Long id,
            @RequestParam Long staffId,
            @RequestParam Long updatedByStaffId) {
        try {
            TransferOutDocumentation updated = transferOutService.assignStaff(id, staffId, updatedByStaffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error assigning staff: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Record parent consent
     * POST /api/transfer-out/{id}/record-consent
     */
    @PostMapping("/{id}/record-consent")
    public ResponseEntity<TransferOutDocumentation> recordParentConsent(
            @PathVariable Long id,
            @RequestParam Long updatedByStaffId) {
        try {
            TransferOutDocumentation updated = transferOutService.recordParentConsent(id, updatedByStaffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error recording consent: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Record fee payment
     * POST /api/transfer-out/{id}/record-payment
     */
    @PostMapping("/{id}/record-payment")
    public ResponseEntity<TransferOutDocumentation> recordFeePayment(
            @PathVariable Long id,
            @RequestParam Long updatedByStaffId) {
        try {
            TransferOutDocumentation updated = transferOutService.recordFeePayment(id, updatedByStaffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error recording payment: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Send records to destination school
     * POST /api/transfer-out/{id}/send
     */
    @PostMapping("/{id}/send")
    public ResponseEntity<TransferOutDocumentation> sendRecords(
            @PathVariable Long id,
            @RequestParam TransmissionMethod method,
            @RequestParam(required = false) String trackingNumber,
            @RequestParam Long sentByStaffId) {
        try {
            TransferOutDocumentation updated = transferOutService.sendRecords(id, method, trackingNumber, sentByStaffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error sending records: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Record acknowledgment from destination school
     * POST /api/transfer-out/{id}/acknowledge
     */
    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<TransferOutDocumentation> recordAcknowledgment(
            @PathVariable Long id,
            @RequestParam String acknowledgedBy,
            @RequestParam String acknowledgmentMethod,
            @RequestParam Long updatedByStaffId) {
        try {
            TransferOutDocumentation updated = transferOutService.recordAcknowledgment(
                    id, acknowledgedBy, acknowledgmentMethod, updatedByStaffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error recording acknowledgment: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Complete transfer out
     * POST /api/transfer-out/{id}/complete
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<TransferOutDocumentation> completeTransferOut(
            @PathVariable Long id,
            @RequestParam Long completedByStaffId) {
        try {
            TransferOutDocumentation updated = transferOutService.completeTransferOut(id, completedByStaffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error completing transfer out: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Cancel transfer out
     * POST /api/transfer-out/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<TransferOutDocumentation> cancelTransferOut(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestParam Long cancelledByStaffId) {
        try {
            TransferOutDocumentation updated = transferOutService.cancelTransferOut(id, reason, cancelledByStaffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error cancelling transfer out: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add follow-up note
     * POST /api/transfer-out/{id}/follow-up
     */
    @PostMapping("/{id}/follow-up")
    public ResponseEntity<TransferOutDocumentation> addFollowUpNote(
            @PathVariable Long id,
            @RequestParam String note,
            @RequestParam Long updatedByStaffId) {
        try {
            TransferOutDocumentation updated = transferOutService.addFollowUpNote(id, note, updatedByStaffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error adding follow-up note: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================================================
    // STATISTICS & REPORTING
    // ========================================================================

    /**
     * Get transfer out statistics
     * GET /api/transfer-out/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<TransferOutDocumentationService.TransferOutStatistics> getStatistics() {
        TransferOutDocumentationService.TransferOutStatistics stats = transferOutService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get count by destination school
     * GET /api/transfer-out/count-by-school
     */
    @GetMapping("/count-by-school")
    public ResponseEntity<List<Object[]>> getCountByDestinationSchool() {
        List<Object[]> counts = transferOutService.getCountByDestinationSchool();
        return ResponseEntity.ok(counts);
    }

    // ========================================================================
    // DELETE OPERATIONS
    // ========================================================================

    /**
     * Delete transfer out (draft only)
     * DELETE /api/transfer-out/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransferOut(@PathVariable Long id) {
        try {
            transferOutService.deleteTransferOut(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Error deleting transfer out: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
