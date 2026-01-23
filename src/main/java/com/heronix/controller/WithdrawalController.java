package com.heronix.controller;

import com.heronix.model.domain.WithdrawalRecord;
import com.heronix.model.domain.WithdrawalRecord.WithdrawalStatus;
import com.heronix.model.domain.WithdrawalRecord.FinalStatus;
import com.heronix.service.WithdrawalService;
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
 * REST API Controller for Student Withdrawal Management
 * Provides endpoints for student withdrawal processing and clearance
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Slf4j
// @RestController  // Disabled for API server mode
@RequestMapping("/api/withdrawals")
@CrossOrigin(origins = "*")
public class WithdrawalController {

    @Autowired
    private WithdrawalService withdrawalService;

    // ========================================================================
    // CREATE OPERATIONS
    // ========================================================================

    /**
     * Create new withdrawal record
     * POST /api/withdrawals
     */
    @PostMapping
    public ResponseEntity<WithdrawalRecord> createWithdrawal(@RequestBody Map<String, Object> request) {
        try {
            log.info("Creating withdrawal record for student ID: {}", request.get("studentId"));

            Long studentId = Long.parseLong(request.get("studentId").toString());
            LocalDate withdrawalDate = LocalDate.parse((String) request.get("withdrawalDate"));
            Long createdByStaffId = Long.parseLong(request.get("createdByStaffId").toString());

            WithdrawalRecord withdrawal = withdrawalService.createWithdrawal(
                    studentId, withdrawalDate, createdByStaffId);

            return ResponseEntity.status(HttpStatus.CREATED).body(withdrawal);
        } catch (Exception e) {
            log.error("Error creating withdrawal: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================================================
    // READ OPERATIONS
    // ========================================================================

    /**
     * Get withdrawal by ID
     * GET /api/withdrawals/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<WithdrawalRecord> getWithdrawalById(@PathVariable Long id) {
        try {
            WithdrawalRecord withdrawal = withdrawalService.getWithdrawalById(id);
            return ResponseEntity.ok(withdrawal);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get withdrawal by withdrawal number
     * GET /api/withdrawals/number/{withdrawalNumber}
     */
    @GetMapping("/number/{withdrawalNumber}")
    public ResponseEntity<WithdrawalRecord> getByWithdrawalNumber(@PathVariable String withdrawalNumber) {
        try {
            WithdrawalRecord withdrawal = withdrawalService.getByWithdrawalNumber(withdrawalNumber);
            return ResponseEntity.ok(withdrawal);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all withdrawals by status
     * GET /api/withdrawals/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<WithdrawalRecord>> getByStatus(@PathVariable WithdrawalStatus status) {
        List<WithdrawalRecord> withdrawals = withdrawalService.getByStatus(status);
        return ResponseEntity.ok(withdrawals);
    }

    /**
     * Get all withdrawals for student
     * GET /api/withdrawals/student/{studentId}
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<WithdrawalRecord>> getByStudent(@PathVariable Long studentId) {
        try {
            List<WithdrawalRecord> withdrawals = withdrawalService.getByStudent(studentId);
            return ResponseEntity.ok(withdrawals);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get most recent withdrawal for student
     * GET /api/withdrawals/student/{studentId}/recent
     */
    @GetMapping("/student/{studentId}/recent")
    public ResponseEntity<WithdrawalRecord> getMostRecentWithdrawal(@PathVariable Long studentId) {
        try {
            WithdrawalRecord withdrawal = withdrawalService.getMostRecentWithdrawal(studentId);
            if (withdrawal == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(withdrawal);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get pending withdrawals
     * GET /api/withdrawals/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<List<WithdrawalRecord>> getPendingWithdrawals() {
        List<WithdrawalRecord> withdrawals = withdrawalService.getPendingWithdrawals();
        return ResponseEntity.ok(withdrawals);
    }

    /**
     * Get withdrawals in clearance process
     * GET /api/withdrawals/in-clearance
     */
    @GetMapping("/in-clearance")
    public ResponseEntity<List<WithdrawalRecord>> getInClearanceProcess() {
        List<WithdrawalRecord> withdrawals = withdrawalService.getInClearanceProcess();
        return ResponseEntity.ok(withdrawals);
    }

    /**
     * Get withdrawals needing attention
     * GET /api/withdrawals/needing-attention
     */
    @GetMapping("/needing-attention")
    public ResponseEntity<List<WithdrawalRecord>> getWithdrawalsNeedingAttention() {
        List<WithdrawalRecord> withdrawals = withdrawalService.getWithdrawalsNeedingAttention();
        return ResponseEntity.ok(withdrawals);
    }

    /**
     * Search withdrawals by student name
     * GET /api/withdrawals/search?name={name}
     */
    @GetMapping("/search")
    public ResponseEntity<List<WithdrawalRecord>> searchByStudentName(@RequestParam String name) {
        List<WithdrawalRecord> withdrawals = withdrawalService.searchByStudentName(name);
        return ResponseEntity.ok(withdrawals);
    }

    /**
     * Get withdrawal statistics
     * GET /api/withdrawals/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<WithdrawalService.WithdrawalStatistics> getStatistics() {
        WithdrawalService.WithdrawalStatistics stats = withdrawalService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get withdrawal counts by type
     * GET /api/withdrawals/counts-by-type
     */
    @GetMapping("/counts-by-type")
    public ResponseEntity<List<Object[]>> getCountByWithdrawalType() {
        List<Object[]> counts = withdrawalService.getCountByWithdrawalType();
        return ResponseEntity.ok(counts);
    }

    /**
     * Get withdrawal counts by month
     * GET /api/withdrawals/counts-by-month?startDate={startDate}&endDate={endDate}
     */
    @GetMapping("/counts-by-month")
    public ResponseEntity<List<Object[]>> getWithdrawalCountsByMonth(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Object[]> counts = withdrawalService.getWithdrawalCountsByMonth(startDate, endDate);
        return ResponseEntity.ok(counts);
    }

    // ========================================================================
    // UPDATE OPERATIONS
    // ========================================================================

    /**
     * Update withdrawal record
     * PUT /api/withdrawals/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<WithdrawalRecord> updateWithdrawal(
            @PathVariable Long id,
            @RequestBody WithdrawalRecord withdrawal,
            @RequestParam Long staffId) {
        try {
            withdrawal.setId(id); // Ensure ID matches path
            WithdrawalRecord updated = withdrawalService.updateWithdrawal(withdrawal, staffId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Start clearance process
     * POST /api/withdrawals/{id}/start-clearance
     */
    @PostMapping("/{id}/start-clearance")
    public ResponseEntity<WithdrawalRecord> startClearance(
            @PathVariable Long id,
            @RequestParam Long staffId) {
        try {
            WithdrawalRecord updated = withdrawalService.startClearance(id, staffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error starting clearance: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Begin clearance process
     * POST /api/withdrawals/{id}/begin-clearance
     */
    @PostMapping("/{id}/begin-clearance")
    public ResponseEntity<WithdrawalRecord> beginClearanceProcess(
            @PathVariable Long id,
            @RequestParam Long staffId) {
        try {
            WithdrawalRecord updated = withdrawalService.beginClearanceProcess(id, staffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error beginning clearance process: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Mark as cleared
     * POST /api/withdrawals/{id}/mark-cleared
     */
    @PostMapping("/{id}/mark-cleared")
    public ResponseEntity<WithdrawalRecord> markAsCleared(
            @PathVariable Long id,
            @RequestParam Long staffId) {
        try {
            WithdrawalRecord updated = withdrawalService.markAsCleared(id, staffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error marking as cleared: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Complete withdrawal
     * POST /api/withdrawals/{id}/complete
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<WithdrawalRecord> completeWithdrawal(
            @PathVariable Long id,
            @RequestParam FinalStatus finalStatus,
            @RequestParam Long staffId) {
        try {
            WithdrawalRecord updated = withdrawalService.completeWithdrawal(id, finalStatus, staffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error completing withdrawal: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Cancel withdrawal
     * POST /api/withdrawals/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<WithdrawalRecord> cancelWithdrawal(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestParam Long staffId) {
        try {
            WithdrawalRecord updated = withdrawalService.cancelWithdrawal(id, reason, staffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error cancelling withdrawal: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Check all clearance items (bulk clearance)
     * POST /api/withdrawals/{id}/check-all-clearance
     */
    @PostMapping("/{id}/check-all-clearance")
    public ResponseEntity<WithdrawalRecord> checkAllClearanceItems(
            @PathVariable Long id,
            @RequestParam Long staffId) {
        try {
            WithdrawalRecord updated = withdrawalService.checkAllClearanceItems(id, staffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error checking all clearance items: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update specific clearance item
     * POST /api/withdrawals/{id}/clearance-item
     */
    @PostMapping("/{id}/clearance-item")
    public ResponseEntity<WithdrawalRecord> updateClearanceItem(
            @PathVariable Long id,
            @RequestParam String itemName,
            @RequestParam boolean checked,
            @RequestParam Long staffId) {
        try {
            WithdrawalRecord updated = withdrawalService.updateClearanceItem(id, itemName, checked, staffId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating clearance item: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================================================
    // DELETE OPERATIONS
    // ========================================================================

    /**
     * Delete withdrawal record (only DRAFT)
     * DELETE /api/withdrawals/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWithdrawal(@PathVariable Long id) {
        try {
            withdrawalService.deleteWithdrawal(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            log.error("Cannot delete withdrawal: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
