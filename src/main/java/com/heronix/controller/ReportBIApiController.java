package com.heronix.controller;

import com.heronix.dto.ReportBusinessIntelligence;
import com.heronix.service.ReportBIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Business Intelligence API Controller
 *
 * REST API endpoints for business intelligence and advanced analytics.
 *
 * Endpoints:
 * - POST /api/bi - Create BI analytics
 * - GET /api/bi/{id} - Get BI analytics
 * - GET /api/bi/report/{reportId} - Get BI analytics by report
 * - GET /api/bi/type/{type} - Get BI analytics by type
 * - POST /api/bi/{id}/execute - Execute analysis
 * - POST /api/bi/{id}/kpi - Add KPI
 * - POST /api/bi/{id}/threshold - Add threshold rule
 * - POST /api/bi/{id}/alert/{alertId}/acknowledge - Acknowledge alert
 * - DELETE /api/bi/{id} - Delete BI analytics
 * - GET /api/bi/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 82 - Report Analytics & Business Intelligence
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/bi")
@RequiredArgsConstructor
@Slf4j
public class ReportBIApiController {

    private final ReportBIService biService;

    /**
     * Create BI analytics
     */
    @PostMapping
    public ResponseEntity<ReportBusinessIntelligence> createBI(@RequestBody ReportBusinessIntelligence bi) {
        log.info("POST /api/bi - Creating BI analytics for report {}", bi.getReportId());

        try {
            ReportBusinessIntelligence created = biService.createBI(bi);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating BI analytics", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get BI analytics
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportBusinessIntelligence> getBI(@PathVariable Long id) {
        log.info("GET /api/bi/{}", id);

        try {
            return biService.getBI(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching BI analytics: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get BI analytics by report
     */
    @GetMapping("/report/{reportId}")
    public ResponseEntity<List<ReportBusinessIntelligence>> getBIByReport(@PathVariable Long reportId) {
        log.info("GET /api/bi/report/{}", reportId);

        try {
            List<ReportBusinessIntelligence> analytics = biService.getBIByReport(reportId);
            return ResponseEntity.ok(analytics);

        } catch (Exception e) {
            log.error("Error fetching BI analytics for report: {}", reportId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get BI analytics by type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<ReportBusinessIntelligence>> getBIByType(
            @PathVariable ReportBusinessIntelligence.AnalyticsType type) {
        log.info("GET /api/bi/type/{}", type);

        try {
            List<ReportBusinessIntelligence> analytics = biService.getBIByType(type);
            return ResponseEntity.ok(analytics);

        } catch (Exception e) {
            log.error("Error fetching BI analytics by type: {}", type, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Execute analysis
     */
    @PostMapping("/{id}/execute")
    public ResponseEntity<ReportBusinessIntelligence> executeAnalysis(@PathVariable Long id) {
        log.info("POST /api/bi/{}/execute", id);

        try {
            ReportBusinessIntelligence result = biService.executeAnalysis(id);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.error("BI analytics not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error executing analysis for BI: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Add KPI
     */
    @PostMapping("/{id}/kpi")
    public ResponseEntity<Map<String, Object>> addKPI(
            @PathVariable Long id,
            @RequestBody ReportBusinessIntelligence.KPI kpi) {
        log.info("POST /api/bi/{}/kpi", id);

        try {
            biService.addKPI(id, kpi);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "KPI added");
            response.put("biId", id);
            response.put("kpiId", kpi.getKpiId());
            response.put("kpiName", kpi.getName());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("BI analytics not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding KPI to BI: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add threshold rule
     */
    @PostMapping("/{id}/threshold")
    public ResponseEntity<Map<String, Object>> addThresholdRule(
            @PathVariable Long id,
            @RequestBody ReportBusinessIntelligence.ThresholdRule rule) {
        log.info("POST /api/bi/{}/threshold", id);

        try {
            biService.addThresholdRule(id, rule);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Threshold rule added");
            response.put("biId", id);
            response.put("ruleId", rule.getRuleId());
            response.put("ruleName", rule.getName());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("BI analytics not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding threshold rule to BI: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Acknowledge alert
     */
    @PostMapping("/{id}/alert/{alertId}/acknowledge")
    public ResponseEntity<Map<String, Object>> acknowledgeAlert(
            @PathVariable Long id,
            @PathVariable String alertId,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/bi/{}/alert/{}/acknowledge", id, alertId);

        try {
            String acknowledgedBy = request.get("acknowledgedBy");
            biService.acknowledgeAlert(id, alertId, acknowledgedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Alert acknowledged");
            response.put("biId", id);
            response.put("alertId", alertId);
            response.put("acknowledgedBy", acknowledgedBy);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error acknowledging alert {} for BI: {}", alertId, id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete BI analytics
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteBI(@PathVariable Long id) {
        log.info("DELETE /api/bi/{}", id);

        try {
            biService.deleteBI(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "BI analytics deleted");
            response.put("biId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting BI analytics: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/bi/stats");

        try {
            Map<String, Object> stats = biService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching BI statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
