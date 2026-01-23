package com.heronix.controller;

import com.heronix.dto.ReportBusinessIntelligence;
import com.heronix.service.ReportBusinessIntelligenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Business Intelligence API Controller
 *
 * REST API endpoints for business intelligence operations including
 * analytics, KPIs, aggregations, dimensions, and dashboards.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 82 - Report Analytics & Business Intelligence
 */
@Slf4j
// @RestController  // Disabled for API server mode
@RequestMapping("/api/business-intelligence")
@RequiredArgsConstructor
public class ReportBusinessIntelligenceApiController {

    private final ReportBusinessIntelligenceService biService;

    /**
     * Create new BI report
     * POST /api/business-intelligence
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createBI(
            @RequestBody ReportBusinessIntelligence bi) {
        try {
            ReportBusinessIntelligence created = biService.createBI(bi);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "BI report created successfully");
            response.put("biId", created.getBiId());
            response.put("name", created.getName());
            response.put("analyticsType", created.getAnalyticsType());
            response.put("createdAt", created.getCreatedAt());

            log.info("BI report created via API: {}", created.getBiId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create BI report: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get BI report by ID
     * GET /api/business-intelligence/{biId}
     */
    @GetMapping("/{biId}")
    public ResponseEntity<Map<String, Object>> getBI(@PathVariable Long biId) {
        try {
            ReportBusinessIntelligence bi = biService.getBI(biId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("bi", bi);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Run analytics
     * POST /api/business-intelligence/{biId}/run
     */
    @PostMapping("/{biId}/run")
    public ResponseEntity<Map<String, Object>> runAnalytics(@PathVariable Long biId) {
        try {
            ReportBusinessIntelligence bi = biService.runAnalytics(biId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Analytics executed successfully");
            response.put("biId", bi.getBiId());
            response.put("executionTimeMs", bi.getExecutionTimeMs());
            response.put("lastRunAt", bi.getLastRunAt());
            response.put("statistics", bi.getStatistics());
            response.put("trendDirection", bi.getTrendDirection());

            log.info("Analytics run via API: {} (time: {}ms)", biId, bi.getExecutionTimeMs());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Add aggregation
     * POST /api/business-intelligence/{biId}/aggregation
     */
    @PostMapping("/{biId}/aggregation")
    public ResponseEntity<Map<String, Object>> addAggregation(
            @PathVariable Long biId,
            @RequestBody Map<String, Object> aggregationRequest) {
        try {
            String fieldName = (String) aggregationRequest.get("fieldName");
            String function = (String) aggregationRequest.get("function");
            String alias = (String) aggregationRequest.get("alias");

            ReportBusinessIntelligence.Aggregation aggregation = biService.addAggregation(
                    biId,
                    fieldName,
                    ReportBusinessIntelligence.AggregationFunction.valueOf(function),
                    alias);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Aggregation added successfully");
            response.put("aggregation", aggregation);

            log.info("Aggregation added via API: {} (bi: {})", alias, biId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to add aggregation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Add KPI
     * POST /api/business-intelligence/{biId}/kpi
     */
    @PostMapping("/{biId}/kpi")
    public ResponseEntity<Map<String, Object>> addKPI(
            @PathVariable Long biId,
            @RequestBody Map<String, Object> kpiRequest) {
        try {
            String name = (String) kpiRequest.get("name");
            Double currentValue = ((Number) kpiRequest.get("currentValue")).doubleValue();
            Double targetValue = ((Number) kpiRequest.get("targetValue")).doubleValue();
            String unit = (String) kpiRequest.get("unit");

            ReportBusinessIntelligence.KPI kpi = biService.addKPI(
                    biId,
                    name,
                    currentValue,
                    targetValue,
                    unit);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "KPI added successfully");
            response.put("kpiId", kpi.getKpiId());
            response.put("name", kpi.getName());
            response.put("currentValue", kpi.getCurrentValue());
            response.put("targetValue", kpi.getTargetValue());
            response.put("achievementRate", kpi.getAchievementRate());
            response.put("status", kpi.getStatus());

            log.info("KPI added via API: {} (bi: {})", kpi.getKpiId(), biId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to add KPI: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Add dimension
     * POST /api/business-intelligence/{biId}/dimension
     */
    @PostMapping("/{biId}/dimension")
    public ResponseEntity<Map<String, Object>> addDimension(
            @PathVariable Long biId,
            @RequestBody Map<String, Object> dimensionRequest) {
        try {
            String name = (String) dimensionRequest.get("name");
            String type = (String) dimensionRequest.get("type");
            List<String> hierarchy = (List<String>) dimensionRequest.get("hierarchy");

            ReportBusinessIntelligence.Dimension dimension = biService.addDimension(
                    biId,
                    name,
                    ReportBusinessIntelligence.DimensionType.valueOf(type),
                    hierarchy);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Dimension added successfully");
            response.put("dimensionId", dimension.getDimensionId());
            response.put("name", dimension.getName());
            response.put("type", dimension.getType());

            log.info("Dimension added via API: {} (bi: {})", dimension.getDimensionId(), biId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to add dimension: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Add dashboard widget
     * POST /api/business-intelligence/{biId}/widget
     */
    @PostMapping("/{biId}/widget")
    public ResponseEntity<Map<String, Object>> addDashboardWidget(
            @PathVariable Long biId,
            @RequestBody Map<String, Object> widgetRequest) {
        try {
            String title = (String) widgetRequest.get("title");
            String type = (String) widgetRequest.get("type");
            Integer row = ((Number) widgetRequest.get("row")).intValue();
            Integer column = ((Number) widgetRequest.get("column")).intValue();
            Integer width = ((Number) widgetRequest.get("width")).intValue();
            Integer height = ((Number) widgetRequest.get("height")).intValue();

            ReportBusinessIntelligence.DashboardWidget widget = biService.addDashboardWidget(
                    biId,
                    title,
                    type,
                    row,
                    column,
                    width,
                    height);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Dashboard widget added successfully");
            response.put("widgetId", widget.getWidgetId());
            response.put("title", widget.getTitle());
            response.put("type", widget.getType());

            log.info("Dashboard widget added via API: {} (bi: {})", widget.getWidgetId(), biId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to add widget: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Add threshold rule
     * POST /api/business-intelligence/{biId}/threshold
     */
    @PostMapping("/{biId}/threshold")
    public ResponseEntity<Map<String, Object>> addThresholdRule(
            @PathVariable Long biId,
            @RequestBody Map<String, Object> thresholdRequest) {
        try {
            String name = (String) thresholdRequest.get("name");
            String metric = (String) thresholdRequest.get("metric");
            String operator = (String) thresholdRequest.get("operator");
            Double threshold = ((Number) thresholdRequest.get("threshold")).doubleValue();
            String severity = (String) thresholdRequest.get("severity");

            ReportBusinessIntelligence.ThresholdRule rule = biService.addThresholdRule(
                    biId,
                    name,
                    metric,
                    operator,
                    threshold,
                    severity);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Threshold rule added successfully");
            response.put("ruleId", rule.getRuleId());
            response.put("name", rule.getName());
            response.put("metric", rule.getMetric());
            response.put("threshold", rule.getThreshold());

            log.info("Threshold rule added via API: {} (bi: {})", rule.getRuleId(), biId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to add threshold rule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Delete BI report
     * DELETE /api/business-intelligence/{biId}
     */
    @DeleteMapping("/{biId}")
    public ResponseEntity<Map<String, Object>> deleteBI(@PathVariable Long biId) {
        try {
            biService.deleteBI(biId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "BI report deleted successfully");
            response.put("biId", biId);
            response.put("deletedAt", LocalDateTime.now());

            log.info("BI report deleted via API: {}", biId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get all BI reports statistics
     * GET /api/business-intelligence/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = biService.getStatistics();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("statistics", stats);

        return ResponseEntity.ok(response);
    }
}
