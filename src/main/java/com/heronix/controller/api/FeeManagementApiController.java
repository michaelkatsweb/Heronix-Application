package com.heronix.controller.api;

import com.heronix.model.domain.AuditLog;
import com.heronix.model.domain.Fee;
import com.heronix.model.domain.FeePayment;
import com.heronix.model.domain.StudentFee;
import com.heronix.service.AuditService;
import com.heronix.service.FeeManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Fee Management
 * Location: src/main/java/com/heronix/controller/api/FeeManagementApiController.java
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Slf4j
@RestController
@RequestMapping("/api/fees")
@RequiredArgsConstructor
@Tag(name = "Fee Management", description = "APIs for managing student fees and payments")
public class FeeManagementApiController {

    private final FeeManagementService feeManagementService;
    private final AuditService auditService;

    // ========================================================================
    // FEE CRUD
    // ========================================================================

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
    @Operation(summary = "Create a new fee")
    public ResponseEntity<Fee> createFee(@RequestBody Fee fee) {
        try {
            Fee created = feeManagementService.createFee(fee);
            auditService.log(AuditLog.AuditAction.SYSTEM_STARTUP, "Fee", created.getId(),
                    "Created fee: " + created.getFeeName(), true, AuditLog.AuditSeverity.INFO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("Error creating fee", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{feeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
    @Operation(summary = "Update an existing fee")
    public ResponseEntity<Fee> updateFee(@PathVariable Long feeId, @RequestBody Fee fee) {
        try {
            Fee updated = feeManagementService.updateFee(feeId, fee);
            auditService.log(AuditLog.AuditAction.CONFIG_CHANGE, "Fee", feeId,
                    "Updated fee: " + updated.getFeeName(), true, AuditLog.AuditSeverity.INFO);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error updating fee", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{feeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
    @Operation(summary = "Delete (deactivate) a fee")
    public ResponseEntity<Void> deleteFee(@PathVariable Long feeId) {
        try {
            feeManagementService.deleteFee(feeId);
            auditService.log(AuditLog.AuditAction.CONFIG_CHANGE, "Fee", feeId,
                    "Deleted fee", true, AuditLog.AuditSeverity.INFO);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{feeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER', 'TEACHER')")
    @Operation(summary = "Get fee by ID")
    public ResponseEntity<Fee> getFeeById(@PathVariable Long feeId) {
        Fee fee = feeManagementService.getFeeById(feeId);
        return fee != null ? ResponseEntity.ok(fee) : ResponseEntity.notFound().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER', 'TEACHER')")
    @Operation(summary = "Get all active fees")
    public ResponseEntity<List<Fee>> getAllActiveFees() {
        List<Fee> fees = feeManagementService.getAllActiveFees();
        return ResponseEntity.ok(fees);
    }

    @GetMapping("/type/{feeType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER', 'TEACHER')")
    @Operation(summary = "Get fees by type")
    public ResponseEntity<List<Fee>> getFeesByType(@PathVariable Fee.FeeType feeType) {
        List<Fee> fees = feeManagementService.getFeesByType(feeType);
        return ResponseEntity.ok(fees);
    }

    @GetMapping("/year/{academicYear}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
    @Operation(summary = "Get fees by academic year")
    public ResponseEntity<List<Fee>> getFeesByAcademicYear(@PathVariable String academicYear) {
        List<Fee> fees = feeManagementService.getFeesByAcademicYear(academicYear);
        return ResponseEntity.ok(fees);
    }

    // ========================================================================
    // STUDENT FEE ASSIGNMENT
    // ========================================================================

    @PostMapping("/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
    @Operation(summary = "Assign a fee to a student")
    public ResponseEntity<StudentFee> assignFeeToStudent(
            @RequestParam Long studentId,
            @RequestParam Long feeId,
            @RequestParam(required = false) BigDecimal customAmount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate customDueDate) {
        try {
            StudentFee studentFee;
            if (customAmount != null || customDueDate != null) {
                studentFee = feeManagementService.assignFeeToStudent(studentId, feeId, customAmount, customDueDate);
            } else {
                studentFee = feeManagementService.assignFeeToStudent(studentId, feeId);
            }
            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "StudentFee", studentFee.getId(),
                    "Assigned fee to student " + studentId, true, AuditLog.AuditSeverity.INFO);
            return ResponseEntity.status(HttpStatus.CREATED).body(studentFee);
        } catch (Exception e) {
            log.error("Error assigning fee to student", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/assign/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
    @Operation(summary = "Assign a fee to multiple students")
    public ResponseEntity<Void> assignFeeToMultipleStudents(
            @RequestParam Long feeId,
            @RequestBody List<Long> studentIds) {
        try {
            feeManagementService.assignFeeToMultipleStudents(feeId, studentIds);
            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "StudentFee", feeId,
                    "Bulk assigned fee to " + studentIds.size() + " students", true, AuditLog.AuditSeverity.INFO);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            log.error("Error bulk assigning fees", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/assign/grade-level")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
    @Operation(summary = "Assign fees to all students in a grade level")
    public ResponseEntity<Void> assignFeesToGradeLevel(
            @RequestParam String gradeLevel,
            @RequestParam String academicYear) {
        try {
            feeManagementService.assignFeesToGradeLevel(gradeLevel, academicYear);
            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "StudentFee", null,
                    "Assigned fees to grade level " + gradeLevel, true, AuditLog.AuditSeverity.INFO);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            log.error("Error assigning fees to grade level", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/student-fees/{studentFeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
    @Operation(summary = "Remove a student fee")
    public ResponseEntity<Void> removeStudentFee(@PathVariable Long studentFeeId) {
        try {
            feeManagementService.removeStudentFee(studentFeeId);
            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "StudentFee", studentFeeId,
                    "Removed student fee", true, AuditLog.AuditSeverity.INFO);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error removing student fee", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========================================================================
    // PAYMENT PROCESSING
    // ========================================================================

    @PostMapping("/payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER', 'CASHIER')")
    @Operation(summary = "Record a payment")
    public ResponseEntity<FeePayment> recordPayment(
            @RequestParam Long studentFeeId,
            @RequestParam BigDecimal amount,
            @RequestParam FeePayment.PaymentMethod paymentMethod,
            @RequestParam(required = false) String transactionId,
            @RequestParam String receivedBy) {
        try {
            FeePayment payment = feeManagementService.recordPayment(
                    studentFeeId, amount, paymentMethod, transactionId, receivedBy);
            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "FeePayment", payment.getId(),
                    "Recorded payment of " + amount, true, AuditLog.AuditSeverity.INFO);
            return ResponseEntity.status(HttpStatus.CREATED).body(payment);
        } catch (Exception e) {
            log.error("Error recording payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/payments/{paymentId}/refund")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
    @Operation(summary = "Refund a payment")
    public ResponseEntity<Void> refundPayment(
            @PathVariable Long paymentId,
            @RequestParam String reason,
            @RequestParam String refundedBy) {
        try {
            feeManagementService.refundPayment(paymentId, reason, refundedBy);
            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "FeePayment", paymentId,
                    "Refunded payment: " + reason, true, AuditLog.AuditSeverity.WARNING);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error refunding payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========================================================================
    // FEE WAIVERS
    // ========================================================================

    @PostMapping("/student-fees/{studentFeeId}/waive")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
    @Operation(summary = "Waive a student fee completely")
    public ResponseEntity<Void> waiveFee(
            @PathVariable Long studentFeeId,
            @RequestParam String reason,
            @RequestParam String waivedBy) {
        try {
            feeManagementService.waiveFee(studentFeeId, reason, waivedBy);
            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "StudentFee", studentFeeId,
                    "Waived fee: " + reason, true, AuditLog.AuditSeverity.INFO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error waiving fee", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/student-fees/{studentFeeId}/partial-waive")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
    @Operation(summary = "Partially waive a student fee")
    public ResponseEntity<Void> partialWaiveFee(
            @PathVariable Long studentFeeId,
            @RequestParam BigDecimal amount,
            @RequestParam String reason,
            @RequestParam String waivedBy) {
        try {
            feeManagementService.partialWaiveFee(studentFeeId, amount, reason, waivedBy);
            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "StudentFee", studentFeeId,
                    "Partially waived fee: " + amount, true, AuditLog.AuditSeverity.INFO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error partially waiving fee", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/student-fees/{studentFeeId}/waive")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
    @Operation(summary = "Remove fee waiver")
    public ResponseEntity<Void> removeWaiver(@PathVariable Long studentFeeId) {
        try {
            feeManagementService.removeWaiver(studentFeeId);
            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "StudentFee", studentFeeId,
                    "Removed fee waiver", true, AuditLog.AuditSeverity.INFO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error removing waiver", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========================================================================
    // STUDENT FEE QUERIES
    // ========================================================================

    @GetMapping("/students/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER', 'TEACHER', 'PARENT', 'STUDENT')")
    @Operation(summary = "Get all fees for a student")
    public ResponseEntity<List<StudentFee>> getStudentFees(@PathVariable Long studentId) {
        List<StudentFee> fees = feeManagementService.getStudentFees(studentId);
        return ResponseEntity.ok(fees);
    }

    @GetMapping("/students/{studentId}/outstanding")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER', 'TEACHER', 'PARENT', 'STUDENT')")
    @Operation(summary = "Get outstanding fees for a student")
    public ResponseEntity<List<StudentFee>> getOutstandingFees(@PathVariable Long studentId) {
        List<StudentFee> fees = feeManagementService.getOutstandingFees(studentId);
        return ResponseEntity.ok(fees);
    }

    @GetMapping("/students/{studentId}/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER', 'TEACHER', 'PARENT')")
    @Operation(summary = "Get overdue fees for a student")
    public ResponseEntity<List<StudentFee>> getOverdueFees(@PathVariable Long studentId) {
        List<StudentFee> fees = feeManagementService.getOverdueFees(studentId);
        return ResponseEntity.ok(fees);
    }

    @GetMapping("/students/{studentId}/balance")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER', 'TEACHER', 'PARENT', 'STUDENT')")
    @Operation(summary = "Get student's outstanding balance")
    public ResponseEntity<Map<String, Object>> getStudentBalance(@PathVariable Long studentId) {
        BigDecimal outstanding = feeManagementService.getStudentOutstandingBalance(studentId);
        BigDecimal totalPaid = feeManagementService.getStudentTotalPaid(studentId);

        return ResponseEntity.ok(Map.of(
                "studentId", studentId,
                "outstandingBalance", outstanding,
                "totalPaid", totalPaid
        ));
    }

    // ========================================================================
    // PAYMENT HISTORY
    // ========================================================================

    @GetMapping("/students/{studentId}/payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER', 'PARENT', 'STUDENT')")
    @Operation(summary = "Get payment history for a student")
    public ResponseEntity<List<FeePayment>> getStudentPaymentHistory(@PathVariable Long studentId) {
        List<FeePayment> payments = feeManagementService.getStudentPaymentHistory(studentId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/student-fees/{studentFeeId}/payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER', 'TEACHER')")
    @Operation(summary = "Get payment history for a specific student fee")
    public ResponseEntity<List<FeePayment>> getPaymentHistory(@PathVariable Long studentFeeId) {
        List<FeePayment> payments = feeManagementService.getPaymentHistory(studentFeeId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/payments/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
    @Operation(summary = "Get payments within a date range")
    public ResponseEntity<List<FeePayment>> getPaymentsInDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<FeePayment> payments = feeManagementService.getPaymentsInDateRange(startDate, endDate);
        return ResponseEntity.ok(payments);
    }

    // ========================================================================
    // REPORTING
    // ========================================================================

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
    @Operation(summary = "Get all overdue fees")
    public ResponseEntity<List<StudentFee>> getAllOverdueFees() {
        List<StudentFee> overdueFees = feeManagementService.getAllOverdueFees();
        return ResponseEntity.ok(overdueFees);
    }

    @GetMapping("/reports/total-outstanding")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
    @Operation(summary = "Get total outstanding balance across all students")
    public ResponseEntity<Map<String, Object>> getTotalOutstanding() {
        BigDecimal total = feeManagementService.calculateTotalOutstanding();
        return ResponseEntity.ok(Map.of("totalOutstanding", total));
    }

    @GetMapping("/reports/collection")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
    @Operation(summary = "Get fee collection report for a date range")
    public ResponseEntity<Map<String, Object>> getFeeCollectionReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> report = feeManagementService.getFeeCollectionReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/reports/collection-by-type")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
    @Operation(summary = "Get fee collection grouped by type")
    public ResponseEntity<List<Map<String, Object>>> getFeeCollectionByType(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Map<String, Object>> report = feeManagementService.getFeeCollectionByType(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/students/{studentId}/statement")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER', 'PARENT', 'STUDENT')")
    @Operation(summary = "Get complete fee statement for a student")
    public ResponseEntity<Map<String, Object>> getStudentFeeStatement(@PathVariable Long studentId) {
        Map<String, Object> statement = feeManagementService.getStudentFeeStatement(studentId);
        return ResponseEntity.ok(statement);
    }

    // ========================================================================
    // AUTOMATED FEE ASSIGNMENT
    // ========================================================================

    @PostMapping("/students/{studentId}/assign-mandatory")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
    @Operation(summary = "Assign all mandatory fees to a new student")
    public ResponseEntity<Void> assignMandatoryFeesToNewStudent(@PathVariable Long studentId) {
        try {
            feeManagementService.assignMandatoryFeesToNewStudent(studentId);
            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "StudentFee", studentId,
                    "Assigned mandatory fees to new student", true, AuditLog.AuditSeverity.INFO);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            log.error("Error assigning mandatory fees", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/assign-annual")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign annual fees to all students for an academic year")
    public ResponseEntity<Void> assignAnnualFeesToAllStudents(@RequestParam String academicYear) {
        try {
            feeManagementService.assignAnnualFeesToAllStudents(academicYear);
            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "StudentFee", null,
                    "Assigned annual fees for year " + academicYear, true, AuditLog.AuditSeverity.INFO);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            log.error("Error assigning annual fees", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "FeeManagementService"
        ));
    }
}
