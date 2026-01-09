package com.heronix.controller.api;

import com.heronix.model.domain.ProfessionalDevelopment;
import com.heronix.repository.ProfessionalDevelopmentRepository;
import com.heronix.service.ProfessionalDevelopmentService;
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
 * REST API Controller for Professional Development Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/professional-development")
@RequiredArgsConstructor
public class ProfessionalDevelopmentApiController {

    private final ProfessionalDevelopmentService pdService;
    private final ProfessionalDevelopmentRepository pdRepository;

    // ==================== Professional Development CRUD Operations ====================

    @GetMapping("/{id}")
    public ResponseEntity<ProfessionalDevelopment> getPDById(@PathVariable Long id) {
        ProfessionalDevelopment pd = pdService.getPDById(id);
        return ResponseEntity.ok(pd);
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<ProfessionalDevelopment>> getPDByTeacher(@PathVariable Long teacherId) {
        List<ProfessionalDevelopment> pdList = pdService.getPDByTeacher(teacherId);
        return ResponseEntity.ok(pdList);
    }

    @GetMapping("/teacher/{teacherId}/active")
    public ResponseEntity<List<ProfessionalDevelopment>> getActivePDByTeacher(@PathVariable Long teacherId) {
        List<ProfessionalDevelopment> pdList = pdService.getActivePDByTeacher(teacherId);
        return ResponseEntity.ok(pdList);
    }

    @GetMapping("/teacher/{teacherId}/completed")
    public ResponseEntity<List<ProfessionalDevelopment>> getCompletedPDByTeacher(@PathVariable Long teacherId) {
        List<ProfessionalDevelopment> pdList = pdService.getCompletedPDByTeacher(teacherId);
        return ResponseEntity.ok(pdList);
    }

    @PostMapping
    public ResponseEntity<ProfessionalDevelopment> createPD(@RequestBody ProfessionalDevelopment pd) {
        ProfessionalDevelopment created = pdService.createPD(pd);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfessionalDevelopment> updatePD(
            @PathVariable Long id,
            @RequestBody ProfessionalDevelopment pd) {
        pd.setId(id);
        ProfessionalDevelopment updated = pdService.updatePD(pd);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePD(@PathVariable Long id) {
        pdService.deletePD(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== PD Status Queries ====================

    @GetMapping("/pending-approval")
    public ResponseEntity<List<ProfessionalDevelopment>> getPendingApproval() {
        List<ProfessionalDevelopment> pdList = pdService.getPendingApproval();
        return ResponseEntity.ok(pdList);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<ProfessionalDevelopment>> getOverduePD() {
        List<ProfessionalDevelopment> pdList = pdService.getOverduePD();
        return ResponseEntity.ok(pdList);
    }

    // ==================== Approval and Completion Operations ====================

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ProfessionalDevelopment> approvePD(
            @PathVariable Long id,
            @RequestParam Long approverId) {
        ProfessionalDevelopment approved = pdService.approvePD(id, approverId);
        return ResponseEntity.ok(approved);
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ProfessionalDevelopment> completePD(
            @PathVariable Long id,
            @RequestParam double hoursEarned,
            @RequestParam double ceusEarned,
            @RequestParam(required = false) String finalGrade) {
        ProfessionalDevelopment completed = pdService.completePD(id, hoursEarned, ceusEarned, finalGrade);
        return ResponseEntity.ok(completed);
    }

    @PatchMapping("/{id}/issue-certificate")
    public ResponseEntity<ProfessionalDevelopment> issueCertificate(
            @PathVariable Long id,
            @RequestParam String certificateNumber) {
        ProfessionalDevelopment updated = pdService.issueCertificate(id, certificateNumber);
        return ResponseEntity.ok(updated);
    }

    // ==================== Reimbursement Operations ====================

    @PatchMapping("/{id}/request-reimbursement")
    public ResponseEntity<ProfessionalDevelopment> requestReimbursement(
            @PathVariable Long id,
            @RequestParam double amount) {
        ProfessionalDevelopment updated = pdService.requestReimbursement(id, amount);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/approve-reimbursement")
    public ResponseEntity<ProfessionalDevelopment> approveReimbursement(@PathVariable Long id) {
        ProfessionalDevelopment updated = pdService.approveReimbursement(id);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/mark-reimbursement-paid")
    public ResponseEntity<ProfessionalDevelopment> markReimbursementPaid(@PathVariable Long id) {
        ProfessionalDevelopment updated = pdService.markReimbursementPaid(id);
        return ResponseEntity.ok(updated);
    }

    // ==================== Statistics ====================

    @GetMapping("/teacher/{teacherId}/statistics/hours")
    public ResponseEntity<Double> getTotalPDHours(
            @PathVariable Long teacherId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Double hours = pdService.getTotalPDHours(teacherId, startDate, endDate);
        return ResponseEntity.ok(hours);
    }

    @GetMapping("/teacher/{teacherId}/statistics/ceus")
    public ResponseEntity<Double> getTotalCEUs(
            @PathVariable Long teacherId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Double ceus = pdService.getTotalCEUs(teacherId, startDate, endDate);
        return ResponseEntity.ok(ceus);
    }

    @GetMapping("/statistics/counts")
    public ResponseEntity<Map<String, Long>> getPDCounts() {
        Map<String, Long> counts = new HashMap<>();

        long total = pdRepository.count();
        long pendingApproval = pdService.getPendingApproval().size();
        long overdue = pdService.getOverduePD().size();

        counts.put("total", total);
        counts.put("pendingApproval", (long) pendingApproval);
        counts.put("overdue", (long) overdue);

        return ResponseEntity.ok(counts);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getDashboardOverview() {
        Map<String, Object> dashboard = new HashMap<>();

        List<ProfessionalDevelopment> pendingApproval = pdService.getPendingApproval();
        List<ProfessionalDevelopment> overdue = pdService.getOverduePD();

        long totalCount = pdRepository.count();

        dashboard.put("totalPDCount", totalCount);
        dashboard.put("pendingApproval", pendingApproval);
        dashboard.put("pendingApprovalCount", pendingApproval.size());
        dashboard.put("overduePD", overdue);
        dashboard.put("overdueCount", overdue.size());

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/teacher/{teacherId}")
    public ResponseEntity<Map<String, Object>> getTeacherDashboard(
            @PathVariable Long teacherId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // Default to current school year if dates not provided
        if (startDate == null) {
            LocalDate now = LocalDate.now();
            int year = now.getYear();
            startDate = now.getMonthValue() < 8 ? LocalDate.of(year - 1, 8, 1) : LocalDate.of(year, 8, 1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        Map<String, Object> dashboard = new HashMap<>();

        List<ProfessionalDevelopment> allPD = pdService.getPDByTeacher(teacherId);
        List<ProfessionalDevelopment> activePD = pdService.getActivePDByTeacher(teacherId);
        List<ProfessionalDevelopment> completedPD = pdService.getCompletedPDByTeacher(teacherId);

        Double totalHours = pdService.getTotalPDHours(teacherId, startDate, endDate);
        Double totalCEUs = pdService.getTotalCEUs(teacherId, startDate, endDate);

        long pendingApprovalCount = allPD.stream()
                .filter(pd -> pd.getStatus() == ProfessionalDevelopment.PDStatus.PENDING_APPROVAL)
                .count();

        dashboard.put("teacherId", teacherId);
        dashboard.put("dateRange", Map.of("startDate", startDate, "endDate", endDate));
        dashboard.put("allPD", allPD);
        dashboard.put("activePD", activePD);
        dashboard.put("completedPD", completedPD);
        dashboard.put("totalCount", allPD.size());
        dashboard.put("activeCount", activePD.size());
        dashboard.put("completedCount", completedPD.size());
        dashboard.put("pendingApprovalCount", pendingApprovalCount);
        dashboard.put("totalHours", totalHours);
        dashboard.put("totalCEUs", totalCEUs);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/reimbursements")
    public ResponseEntity<Map<String, Object>> getReimbursementDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        List<ProfessionalDevelopment> allPD = pdRepository.findAll();

        List<ProfessionalDevelopment> requestedReimbursement = allPD.stream()
                .filter(pd -> Boolean.TRUE.equals(pd.getReimbursementRequested()))
                .filter(pd -> !Boolean.TRUE.equals(pd.getReimbursementApproved()))
                .toList();

        List<ProfessionalDevelopment> approvedReimbursement = allPD.stream()
                .filter(pd -> Boolean.TRUE.equals(pd.getReimbursementApproved()))
                .filter(pd -> !Boolean.TRUE.equals(pd.getReimbursementPaid()))
                .toList();

        List<ProfessionalDevelopment> paidReimbursement = allPD.stream()
                .filter(pd -> Boolean.TRUE.equals(pd.getReimbursementPaid()))
                .toList();

        double totalRequested = requestedReimbursement.stream()
                .mapToDouble(ProfessionalDevelopment::getReimbursementAmount)
                .sum();
        double totalApproved = approvedReimbursement.stream()
                .mapToDouble(ProfessionalDevelopment::getReimbursementAmount)
                .sum();
        double totalPaid = paidReimbursement.stream()
                .mapToDouble(ProfessionalDevelopment::getReimbursementAmount)
                .sum();

        dashboard.put("requestedReimbursements", requestedReimbursement);
        dashboard.put("requestedCount", requestedReimbursement.size());
        dashboard.put("totalRequested", totalRequested);
        dashboard.put("approvedReimbursements", approvedReimbursement);
        dashboard.put("approvedCount", approvedReimbursement.size());
        dashboard.put("totalApproved", totalApproved);
        dashboard.put("paidReimbursements", paidReimbursement);
        dashboard.put("paidCount", paidReimbursement.size());
        dashboard.put("totalPaid", totalPaid);

        return ResponseEntity.ok(dashboard);
    }
}
