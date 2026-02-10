package com.heronix.controller.api;

import com.heronix.model.domain.WithdrawalRecord;
import com.heronix.model.domain.WithdrawalRecord.WithdrawalStatus;
import com.heronix.model.domain.WithdrawalRecord.FinalStatus;
import com.heronix.service.WithdrawalService;
import com.heronix.service.WithdrawalService.WithdrawalStatistics;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Student Withdrawal Management
 *
 * Provides endpoints for managing student withdrawal process:
 * - Withdrawal record creation and management
 * - Multi-stage clearance workflow (DRAFT -> PENDING -> IN_PROGRESS -> CLEARED -> COMPLETED)
 * - Clearance item tracking (24 items across Academic, Library, Facilities, Financial, Administrative)
 * - Transfer student processing
 * - Search and reporting capabilities
 *
 * Clearance Categories:
 * - Academic: Final grades, transcript, IEP/504, progress reports
 * - Library & Materials: Books, textbooks, devices, athletic equipment, instruments
 * - Facilities: Locker, parking permit, ID card
 * - Financial: Tuition, cafeteria balance, activity fees, damage fees
 * - Administrative: Records release, immunizations, paperwork, notifications
 *
 * Workflow Stages:
 * 1. DRAFT - Initial creation
 * 2. PENDING_CLEARANCE - Ready for clearance process
 * 3. CLEARANCE_IN_PROGRESS - Actively clearing items
 * 4. CLEARED - All items cleared
 * 5. COMPLETED - Final withdrawal processed
 * 6. CANCELLED - Withdrawal cancelled
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 32 - December 29, 2025
 */
@RestController
@RequestMapping("/api/student-withdrawal")
@RequiredArgsConstructor
public class StudentWithdrawalApiController {

    private final WithdrawalService withdrawalService;

    // ==================== Withdrawal CRUD ====================

    @PostMapping("/withdrawals")
    public ResponseEntity<Map<String, Object>> createWithdrawal(
            @RequestBody Map<String, Object> requestBody) {

        try {
            Long studentId = Long.valueOf(requestBody.get("studentId").toString());
            LocalDate withdrawalDate = LocalDate.parse((String) requestBody.get("withdrawalDate"));
            Long createdByStaffId = Long.valueOf(requestBody.get("createdByStaffId").toString());

            WithdrawalRecord created = withdrawalService.createWithdrawal(
                studentId, withdrawalDate, createdByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("withdrawal", created);
            response.put("message", "Withdrawal record created with status DRAFT");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "State error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to create withdrawal: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/withdrawals/{id}")
    public ResponseEntity<Map<String, Object>> getWithdrawalById(@PathVariable Long id) {
        try {
            WithdrawalRecord withdrawal = withdrawalService.getWithdrawalById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("withdrawal", withdrawal);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get withdrawal: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/withdrawals/number/{withdrawalNumber}")
    public ResponseEntity<Map<String, Object>> getByWithdrawalNumber(@PathVariable String withdrawalNumber) {
        try {
            WithdrawalRecord withdrawal = withdrawalService.getByWithdrawalNumber(withdrawalNumber);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("withdrawal", withdrawal);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get withdrawal: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/withdrawals/{id}")
    public ResponseEntity<Map<String, Object>> updateWithdrawal(
            @PathVariable Long id,
            @RequestBody WithdrawalRecord updatedWithdrawal,
            @RequestParam Long updatedByStaffId) {

        try {
            updatedWithdrawal.setId(id);
            WithdrawalRecord updated = withdrawalService.updateWithdrawal(updatedWithdrawal, updatedByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("withdrawal", updated);
            response.put("message", "Withdrawal updated successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found or validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to update withdrawal: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/withdrawals/{id}")
    public ResponseEntity<Map<String, Object>> deleteWithdrawal(@PathVariable Long id) {
        try {
            withdrawalService.deleteWithdrawal(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Withdrawal deleted (only DRAFT status can be deleted)");

            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "State error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to delete withdrawal: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Query Operations ====================

    @GetMapping("/withdrawals/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getByStudent(@PathVariable Long studentId) {
        try {
            List<WithdrawalRecord> withdrawals = withdrawalService.getByStudent(studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("withdrawals", withdrawals);
            response.put("count", withdrawals.size());
            response.put("studentId", studentId);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get withdrawals: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/withdrawals/student/{studentId}/recent")
    public ResponseEntity<Map<String, Object>> getMostRecentWithdrawal(@PathVariable Long studentId) {
        try {
            WithdrawalRecord withdrawal = withdrawalService.getMostRecentWithdrawal(studentId);

            if (withdrawal == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "No withdrawal records found for student: " + studentId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("withdrawal", withdrawal);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get recent withdrawal: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/withdrawals/status/{status}")
    public ResponseEntity<Map<String, Object>> getByStatus(@PathVariable WithdrawalStatus status) {
        try {
            List<WithdrawalRecord> withdrawals = withdrawalService.getByStatus(status);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("withdrawals", withdrawals);
            response.put("count", withdrawals.size());
            response.put("status", status);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get withdrawals: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/withdrawals/pending")
    public ResponseEntity<Map<String, Object>> getPendingWithdrawals() {
        try {
            List<WithdrawalRecord> withdrawals = withdrawalService.getPendingWithdrawals();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("withdrawals", withdrawals);
            response.put("count", withdrawals.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get pending withdrawals: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/withdrawals/in-clearance")
    public ResponseEntity<Map<String, Object>> getInClearanceProcess() {
        try {
            List<WithdrawalRecord> withdrawals = withdrawalService.getInClearanceProcess();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("withdrawals", withdrawals);
            response.put("count", withdrawals.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get clearance withdrawals: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/withdrawals/needing-attention")
    public ResponseEntity<Map<String, Object>> getWithdrawalsNeedingAttention() {
        try {
            List<WithdrawalRecord> withdrawals = withdrawalService.getWithdrawalsNeedingAttention();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("withdrawals", withdrawals);
            response.put("count", withdrawals.size());
            response.put("message", "Withdrawals older than 7 days without progress");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get withdrawals needing attention: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/withdrawals/search")
    public ResponseEntity<Map<String, Object>> searchByStudentName(@RequestParam String name) {
        try {
            List<WithdrawalRecord> results = withdrawalService.searchByStudentName(name);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("results", results);
            response.put("count", results.size());
            response.put("searchTerm", name);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Search failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Workflow Operations ====================

    @PostMapping("/withdrawals/{id}/start-clearance")
    public ResponseEntity<Map<String, Object>> startClearance(
            @PathVariable Long id,
            @RequestParam Long updatedByStaffId) {

        try {
            WithdrawalRecord updated = withdrawalService.startClearance(id, updatedByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("withdrawal", updated);
            response.put("message", "Clearance process started (status: PENDING_CLEARANCE)");

            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "State error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to start clearance: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/withdrawals/{id}/begin-clearance")
    public ResponseEntity<Map<String, Object>> beginClearanceProcess(
            @PathVariable Long id,
            @RequestParam Long updatedByStaffId) {

        try {
            WithdrawalRecord updated = withdrawalService.beginClearanceProcess(id, updatedByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("withdrawal", updated);
            response.put("message", "Clearance process begun (status: CLEARANCE_IN_PROGRESS)");

            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "State error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to begin clearance: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/withdrawals/{id}/mark-cleared")
    public ResponseEntity<Map<String, Object>> markAsCleared(
            @PathVariable Long id,
            @RequestParam Long updatedByStaffId) {

        try {
            WithdrawalRecord updated = withdrawalService.markAsCleared(id, updatedByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("withdrawal", updated);
            response.put("message", "Withdrawal marked as cleared (all items complete)");

            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "State error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to mark as cleared: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/withdrawals/{id}/complete")
    public ResponseEntity<Map<String, Object>> completeWithdrawal(
            @PathVariable Long id,
            @RequestParam FinalStatus finalStatus,
            @RequestParam Long completedByStaffId) {

        try {
            WithdrawalRecord updated = withdrawalService.completeWithdrawal(id, finalStatus, completedByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("withdrawal", updated);
            response.put("message", "Withdrawal completed with final status: " + finalStatus);

            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "State error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to complete withdrawal: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/withdrawals/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelWithdrawal(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestParam Long cancelledByStaffId) {

        try {
            WithdrawalRecord updated = withdrawalService.cancelWithdrawal(id, reason, cancelledByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("withdrawal", updated);
            response.put("message", "Withdrawal cancelled");

            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "State error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to cancel withdrawal: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Clearance Operations ====================

    @PostMapping("/withdrawals/{id}/clearance/check-all")
    public ResponseEntity<Map<String, Object>> checkAllClearanceItems(
            @PathVariable Long id,
            @RequestParam Long updatedByStaffId) {

        try {
            WithdrawalRecord updated = withdrawalService.checkAllClearanceItems(id, updatedByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("withdrawal", updated);
            response.put("message", "All clearance items checked (24/24 complete)");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to check all items: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/withdrawals/{id}/clearance/update-item")
    public ResponseEntity<Map<String, Object>> updateClearanceItem(
            @PathVariable Long id,
            @RequestParam String itemName,
            @RequestParam boolean checked,
            @RequestParam Long updatedByStaffId) {

        try {
            WithdrawalRecord updated = withdrawalService.updateClearanceItem(
                id, itemName, checked, updatedByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("withdrawal", updated);
            response.put("message", "Clearance item '" + itemName + "' updated to: " + checked);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to update clearance item: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Statistics & Reporting ====================

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            WithdrawalStatistics stats = withdrawalService.getStatistics();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", Map.of(
                "total", stats.total(),
                "draft", stats.draft(),
                "pendingClearance", stats.pendingClearance(),
                "clearanceInProgress", stats.clearanceInProgress(),
                "cleared", stats.cleared(),
                "completed", stats.completed(),
                "cancelled", stats.cancelled(),
                "transfers", stats.transfers(),
                "averageClearanceCompletion", String.format("%.1f%%", stats.averageClearanceCompletion())
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/reports/by-type")
    public ResponseEntity<Map<String, Object>> getCountByWithdrawalType() {
        try {
            List<Object[]> counts = withdrawalService.getCountByWithdrawalType();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("counts", counts);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get counts by type: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/reports/by-month")
    public ResponseEntity<Map<String, Object>> getWithdrawalCountsByMonth(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            List<Object[]> counts = withdrawalService.getWithdrawalCountsByMonth(startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("counts", counts);
            response.put("startDate", startDate);
            response.put("endDate", endDate);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get counts by month: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Dashboard ====================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        dashboard.put("operations", List.of(
            Map.of(
                "name", "Create Withdrawal",
                "endpoint", "POST /api/student-withdrawal/withdrawals",
                "description", "Create new withdrawal record (starts as DRAFT)"
            ),
            Map.of(
                "name", "Start Clearance",
                "endpoint", "POST /api/student-withdrawal/withdrawals/{id}/start-clearance",
                "description", "Begin clearance process (DRAFT -> PENDING_CLEARANCE)"
            ),
            Map.of(
                "name", "Track Clearance Items",
                "endpoint", "POST /api/student-withdrawal/withdrawals/{id}/clearance/update-item",
                "description", "Update individual clearance items (24 total)"
            ),
            Map.of(
                "name", "Complete Withdrawal",
                "endpoint", "POST /api/student-withdrawal/withdrawals/{id}/complete",
                "description", "Finalize withdrawal with final status"
            ),
            Map.of(
                "name", "View Statistics",
                "endpoint", "GET /api/student-withdrawal/statistics",
                "description", "Get withdrawal statistics and metrics"
            )
        ));

        dashboard.put("workflowStages", List.of(
            Map.of("stage", "DRAFT", "description", "Initial creation"),
            Map.of("stage", "PENDING_CLEARANCE", "description", "Ready for clearance process"),
            Map.of("stage", "CLEARANCE_IN_PROGRESS", "description", "Actively clearing items"),
            Map.of("stage", "CLEARED", "description", "All items cleared"),
            Map.of("stage", "COMPLETED", "description", "Final withdrawal processed"),
            Map.of("stage", "CANCELLED", "description", "Withdrawal cancelled")
        ));

        dashboard.put("clearanceCategories", Map.of(
            "Academic", "Final grades, transcript, IEP/504, progress reports (4 items)",
            "Library & Materials", "Books, textbooks, devices, athletic equipment, instruments (6 items)",
            "Facilities", "Locker, parking permit, ID card (4 items)",
            "Financial", "Tuition, cafeteria balance, activity fees, damage fees (4 items)",
            "Administrative", "Records release, immunizations, paperwork, notifications (6 items)"
        ));

        dashboard.put("features", List.of(
            "Multi-stage withdrawal workflow (6 stages)",
            "24-item clearance checklist across 5 categories",
            "Transfer student processing",
            "Automatic withdrawal number generation (WD-YYYY-######)",
            "Clearance completion percentage tracking",
            "Needing attention alerts (7+ days without progress)",
            "Search by student name or withdrawal number",
            "Comprehensive statistics and reporting"
        ));

        try {
            WithdrawalStatistics stats = withdrawalService.getStatistics();
            dashboard.put("currentStatistics", Map.of(
                "total", stats.total(),
                "inProgress", stats.clearanceInProgress(),
                "completed", stats.completed(),
                "averageCompletion", String.format("%.1f%%", stats.averageClearanceCompletion())
            ));
        } catch (Exception e) {
            dashboard.put("statisticsError", "Failed to load statistics: " + e.getMessage());
        }

        return ResponseEntity.ok(dashboard);
    }

    // ==================== Reference Data ====================

    @GetMapping("/reference/withdrawal-statuses")
    public ResponseEntity<Map<String, Object>> getWithdrawalStatuses() {
        Map<String, Object> statuses = new HashMap<>();
        statuses.put("withdrawalStatuses", List.of(
            Map.of("value", "DRAFT", "description", "Initial creation"),
            Map.of("value", "PENDING_CLEARANCE", "description", "Ready for clearance process"),
            Map.of("value", "CLEARANCE_IN_PROGRESS", "description", "Actively clearing items"),
            Map.of("value", "CLEARED", "description", "All items cleared"),
            Map.of("value", "COMPLETED", "description", "Final withdrawal processed"),
            Map.of("value", "CANCELLED", "description", "Withdrawal cancelled")
        ));
        return ResponseEntity.ok(statuses);
    }

    @GetMapping("/reference/final-statuses")
    public ResponseEntity<Map<String, Object>> getFinalStatuses() {
        Map<String, Object> statuses = new HashMap<>();
        statuses.put("finalStatuses", List.of(
            Map.of("value", "WITHDRAWN", "description", "Student withdrawn from school"),
            Map.of("value", "TRANSFERRED", "description", "Student transferred to another school"),
            Map.of("value", "GRADUATED", "description", "Student graduated"),
            Map.of("value", "DECEASED", "description", "Student deceased"),
            Map.of("value", "EXPELLED", "description", "Student expelled"),
            Map.of("value", "OTHER", "description", "Other reason")
        ));
        return ResponseEntity.ok(statuses);
    }

    @GetMapping("/reference/clearance-items")
    public ResponseEntity<Map<String, Object>> getClearanceItems() {
        Map<String, Object> items = new HashMap<>();
        items.put("clearanceItems", Map.of(
            "Academic", List.of(
                "finalGradesRecorded", "transcriptPrinted", "iep504Finalized", "progressReportsSent"
            ),
            "Library & Materials", List.of(
                "libraryBooksReturned", "textbooksReturned", "libraryFinesPaid",
                "devicesReturned", "athleticEquipmentReturned", "instrumentsReturned"
            ),
            "Facilities", List.of(
                "lockerCleared", "lockerLockReturned", "parkingPermitReturned", "idCardReturned"
            ),
            "Financial", List.of(
                "tuitionPaid", "cafeteriaBalanceSettled", "activityFeesPaid", "damageFeesPaid"
            ),
            "Administrative", List.of(
                "recordsReleaseSigned", "immunizationsCopied", "paperworkCompleted",
                "parentNotificationSent", "withdrawalFormSigned", "finalTranscriptRequested"
            )
        ));
        items.put("totalItems", 24);
        return ResponseEntity.ok(items);
    }

    // ==================== Metadata ====================

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("apiVersion", "1.0.0");
        metadata.put("phase", "Phase 32");
        metadata.put("category", "Student Lifecycle Management");
        metadata.put("description", "Comprehensive student withdrawal processing with multi-stage clearance workflow");

        metadata.put("capabilities", List.of(
            "Withdrawal record creation and management",
            "Multi-stage workflow (DRAFT -> PENDING -> IN_PROGRESS -> CLEARED -> COMPLETED)",
            "24-item clearance checklist across 5 categories",
            "Transfer student processing",
            "Clearance percentage tracking",
            "Needing attention alerts",
            "Comprehensive statistics and reporting"
        ));

        metadata.put("endpoints", Map.of(
            "crud", List.of("POST /withdrawals", "GET /withdrawals/{id}", "PUT /withdrawals/{id}", "DELETE /withdrawals/{id}"),
            "workflow", List.of("POST /withdrawals/{id}/start-clearance", "POST /withdrawals/{id}/begin-clearance", "POST /withdrawals/{id}/mark-cleared", "POST /withdrawals/{id}/complete", "POST /withdrawals/{id}/cancel"),
            "clearance", List.of("POST /withdrawals/{id}/clearance/check-all", "POST /withdrawals/{id}/clearance/update-item"),
            "queries", List.of("GET /withdrawals/pending", "GET /withdrawals/in-clearance", "GET /withdrawals/needing-attention"),
            "reporting", List.of("GET /statistics", "GET /reports/by-type", "GET /reports/by-month")
        ));

        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = new HashMap<>();
        help.put("description", "Student Withdrawal API - Manage student withdrawal process with comprehensive clearance tracking");

        help.put("commonWorkflows", Map.of(
            "standardWithdrawal", List.of(
                "1. POST /api/student-withdrawal/withdrawals (create DRAFT)",
                "2. POST /api/student-withdrawal/withdrawals/{id}/start-clearance (PENDING_CLEARANCE)",
                "3. POST /api/student-withdrawal/withdrawals/{id}/begin-clearance (CLEARANCE_IN_PROGRESS)",
                "4. POST /api/student-withdrawal/withdrawals/{id}/clearance/update-item (check items individually)",
                "5. POST /api/student-withdrawal/withdrawals/{id}/mark-cleared (CLEARED)",
                "6. POST /api/student-withdrawal/withdrawals/{id}/complete (COMPLETED)"
            ),
            "quickClearance", List.of(
                "1. POST /api/student-withdrawal/withdrawals (create DRAFT)",
                "2. POST /api/student-withdrawal/withdrawals/{id}/clearance/check-all (check all 24 items)",
                "3. POST /api/student-withdrawal/withdrawals/{id}/complete (finalize)"
            ),
            "monitorProgress", List.of(
                "1. GET /api/student-withdrawal/withdrawals/in-clearance",
                "2. GET /api/student-withdrawal/withdrawals/needing-attention",
                "3. GET /api/student-withdrawal/statistics"
            )
        ));

        help.put("examples", Map.of(
            "create", "curl -X POST http://localhost:9590/api/student-withdrawal/withdrawals -H 'Content-Type: application/json' -d '{\"studentId\":123,\"withdrawalDate\":\"2025-12-29\",\"createdByStaffId\":1}'",
            "startClearance", "curl -X POST 'http://localhost:9590/api/student-withdrawal/withdrawals/1/start-clearance?updatedByStaffId=1'",
            "updateItem", "curl -X POST 'http://localhost:9590/api/student-withdrawal/withdrawals/1/clearance/update-item?itemName=finalGradesRecorded&checked=true&updatedByStaffId=1'",
            "complete", "curl -X POST 'http://localhost:9590/api/student-withdrawal/withdrawals/1/complete?finalStatus=TRANSFERRED&completedByStaffId=1'",
            "getStatistics", "curl http://localhost:9590/api/student-withdrawal/statistics"
        ));

        help.put("notes", Map.of(
            "workflow", "Withdrawal must progress through stages in order (DRAFT -> PENDING -> IN_PROGRESS -> CLEARED -> COMPLETED)",
            "clearance", "24 clearance items across 5 categories must be completed before final completion",
            "deletion", "Only DRAFT withdrawals can be deleted",
            "cancellation", "COMPLETED withdrawals cannot be cancelled",
            "numbering", "Withdrawal numbers are auto-generated as WD-YYYY-######"
        ));

        return ResponseEntity.ok(help);
    }
}
