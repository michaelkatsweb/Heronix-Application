package com.heronix.controller;

import com.heronix.dto.ComplianceReport;
import com.heronix.service.ComplianceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Compliance API Controller
 *
 * REST API endpoints for compliance and audit management.
 *
 * Endpoints:
 * - POST /api/compliance/generate - Generate compliance report
 * - GET /api/compliance/{id} - Get compliance report
 * - GET /api/compliance - Get all compliance reports
 * - GET /api/compliance/status/{status} - Get reports by status
 * - GET /api/compliance/stats - Get compliance statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 75 - Report Compliance & Audit
 */
@RestController
@RequestMapping("/api/compliance")
@RequiredArgsConstructor
@Slf4j
public class ComplianceApiController {

    private final ComplianceService complianceService;

    @PostMapping("/generate")
    public ResponseEntity<ComplianceReport> generateComplianceReport(@RequestBody Map<String, Object> request) {
        log.info("POST /api/compliance/generate");

        try {
            Long reportId = Long.valueOf(request.get("reportId").toString());
            String standardStr = (String) request.get("standard");

            ComplianceReport.ComplianceStandard standard =
                    ComplianceReport.ComplianceStandard.valueOf(standardStr.toUpperCase());

            ComplianceReport report = complianceService.generateComplianceReport(reportId, standard);
            return ResponseEntity.ok(report);

        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error generating compliance report", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComplianceReport> getComplianceReport(@PathVariable Long id) {
        log.info("GET /api/compliance/{}", id);

        try {
            return complianceService.getComplianceReport(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching compliance report: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ComplianceReport>> getAllComplianceReports() {
        log.info("GET /api/compliance");

        try {
            List<ComplianceReport> reports = complianceService.getAllComplianceReports();
            return ResponseEntity.ok(reports);

        } catch (Exception e) {
            log.error("Error fetching compliance reports", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ComplianceReport>> getReportsByStatus(@PathVariable String status) {
        log.info("GET /api/compliance/status/{}", status);

        try {
            ComplianceReport.ComplianceStatus complianceStatus =
                    ComplianceReport.ComplianceStatus.valueOf(status.toUpperCase());

            List<ComplianceReport> reports = complianceService.getReportsByStatus(complianceStatus);
            return ResponseEntity.ok(reports);

        } catch (IllegalArgumentException e) {
            log.error("Invalid status: {}", status);
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error fetching reports by status: {}", status, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/compliance/stats");

        try {
            Map<String, Object> stats = complianceService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching compliance statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
