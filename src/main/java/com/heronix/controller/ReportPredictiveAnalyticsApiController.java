package com.heronix.controller;

import com.heronix.dto.ReportPredictiveAnalytics;
import com.heronix.service.ReportPredictiveAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Predictive Analytics & Forecasting API Controller
 *
 * REST API endpoints for predictive modeling, forecasting, and analytical insights.
 *
 * Endpoints:
 * - POST /api/predictive-analytics - Create predictive model
 * - GET /api/predictive-analytics/{id} - Get model
 * - POST /api/predictive-analytics/{id}/train - Train model
 * - POST /api/predictive-analytics/{id}/deploy - Deploy model
 * - POST /api/predictive-analytics/{id}/predict - Make prediction
 * - POST /api/predictive-analytics/{id}/forecast - Generate forecast
 * - POST /api/predictive-analytics/{id}/trend - Identify trend
 * - POST /api/predictive-analytics/{id}/anomaly - Detect anomaly
 * - POST /api/predictive-analytics/{id}/risk - Assess risk
 * - POST /api/predictive-analytics/{id}/scenario - Create scenario
 * - POST /api/predictive-analytics/{id}/insight - Generate insight
 * - POST /api/predictive-analytics/{id}/recommendation - Add recommendation
 * - PUT /api/predictive-analytics/{id}/performance - Update performance
 * - DELETE /api/predictive-analytics/{id} - Delete model
 * - GET /api/predictive-analytics/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 111 - Report Predictive Analytics & Forecasting
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/predictive-analytics")
@RequiredArgsConstructor
@Slf4j
public class ReportPredictiveAnalyticsApiController {

    private final ReportPredictiveAnalyticsService analyticsService;

    /**
     * Create predictive model
     */
    @PostMapping
    public ResponseEntity<ReportPredictiveAnalytics> createModel(@RequestBody ReportPredictiveAnalytics model) {
        log.info("POST /api/predictive-analytics - Creating predictive model: {}", model.getModelName());

        try {
            ReportPredictiveAnalytics created = analyticsService.createModel(model);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating predictive model", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get predictive model
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportPredictiveAnalytics> getModel(@PathVariable Long id) {
        log.info("GET /api/predictive-analytics/{}", id);

        try {
            return analyticsService.getModel(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching predictive model: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Train model
     */
    @PostMapping("/{id}/train")
    public ResponseEntity<Map<String, Object>> trainModel(@PathVariable Long id) {
        log.info("POST /api/predictive-analytics/{}/train", id);

        try {
            analyticsService.trainModel(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Model training completed");
            response.put("modelId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Predictive model not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error training model: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Deploy model
     */
    @PostMapping("/{id}/deploy")
    public ResponseEntity<Map<String, Object>> deployModel(@PathVariable Long id) {
        log.info("POST /api/predictive-analytics/{}/deploy", id);

        try {
            analyticsService.deployModel(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Model deployed successfully");
            response.put("modelId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Predictive model not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error deploying model: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Make prediction
     */
    @PostMapping("/{id}/predict")
    public ResponseEntity<ReportPredictiveAnalytics.Prediction> makePrediction(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/predictive-analytics/{}/predict", id);

        try {
            String targetEntity = (String) request.get("targetEntity");
            String targetId = (String) request.get("targetId");
            @SuppressWarnings("unchecked")
            Map<String, Object> features = (Map<String, Object>) request.get("features");

            ReportPredictiveAnalytics.Prediction prediction = analyticsService.makePrediction(
                    id, targetEntity, targetId, features != null ? features : new HashMap<>()
            );

            return ResponseEntity.ok(prediction);

        } catch (IllegalArgumentException e) {
            log.error("Predictive model not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error making prediction: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Generate forecast
     */
    @PostMapping("/{id}/forecast")
    public ResponseEntity<ReportPredictiveAnalytics.Forecast> generateForecast(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/predictive-analytics/{}/forecast", id);

        try {
            String metric = request.get("metric");
            String timeframe = request.get("timeframe");
            String startDateStr = request.get("startDate");
            String endDateStr = request.get("endDate");

            LocalDateTime startDate = LocalDateTime.parse(startDateStr);
            LocalDateTime endDate = LocalDateTime.parse(endDateStr);

            ReportPredictiveAnalytics.Forecast forecast = analyticsService.generateForecast(
                    id, metric, timeframe, startDate, endDate
            );

            return ResponseEntity.ok(forecast);

        } catch (IllegalArgumentException e) {
            log.error("Predictive model not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error generating forecast: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Identify trend
     */
    @PostMapping("/{id}/trend")
    public ResponseEntity<ReportPredictiveAnalytics.Trend> identifyTrend(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/predictive-analytics/{}/trend", id);

        try {
            String metric = request.get("metric");
            String trendType = request.get("trendType");
            String direction = request.get("direction");

            ReportPredictiveAnalytics.Trend trend = analyticsService.identifyTrend(
                    id, metric, trendType, direction
            );

            return ResponseEntity.ok(trend);

        } catch (IllegalArgumentException e) {
            log.error("Predictive model not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error identifying trend: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Detect anomaly
     */
    @PostMapping("/{id}/anomaly")
    public ResponseEntity<ReportPredictiveAnalytics.Anomaly> detectAnomaly(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/predictive-analytics/{}/anomaly", id);

        try {
            String metric = (String) request.get("metric");
            Double actualValue = request.get("actualValue") != null ?
                    ((Number) request.get("actualValue")).doubleValue() : 0.0;
            Double expectedValue = request.get("expectedValue") != null ?
                    ((Number) request.get("expectedValue")).doubleValue() : 0.0;

            ReportPredictiveAnalytics.Anomaly anomaly = analyticsService.detectAnomaly(
                    id, metric, actualValue, expectedValue
            );

            return ResponseEntity.ok(anomaly);

        } catch (IllegalArgumentException e) {
            log.error("Predictive model not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error detecting anomaly: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Assess risk
     */
    @PostMapping("/{id}/risk")
    public ResponseEntity<ReportPredictiveAnalytics.RiskAssessment> assessRisk(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/predictive-analytics/{}/risk", id);

        try {
            String targetEntity = (String) request.get("targetEntity");
            String targetId = (String) request.get("targetId");
            @SuppressWarnings("unchecked")
            List<String> riskFactors = (List<String>) request.get("riskFactors");

            ReportPredictiveAnalytics.RiskAssessment assessment = analyticsService.assessRisk(
                    id, targetEntity, targetId, riskFactors != null ? riskFactors : new java.util.ArrayList<>()
            );

            return ResponseEntity.ok(assessment);

        } catch (IllegalArgumentException e) {
            log.error("Predictive model not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error assessing risk: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create scenario
     */
    @PostMapping("/{id}/scenario")
    public ResponseEntity<ReportPredictiveAnalytics.Scenario> createScenario(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/predictive-analytics/{}/scenario", id);

        try {
            String scenarioName = (String) request.get("scenarioName");
            String description = (String) request.get("description");
            @SuppressWarnings("unchecked")
            Map<String, Object> inputs = (Map<String, Object>) request.get("inputs");

            ReportPredictiveAnalytics.Scenario scenario = analyticsService.createScenario(
                    id, scenarioName, description, inputs != null ? inputs : new HashMap<>()
            );

            return ResponseEntity.ok(scenario);

        } catch (IllegalArgumentException e) {
            log.error("Predictive model not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating scenario: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Generate insight
     */
    @PostMapping("/{id}/insight")
    public ResponseEntity<ReportPredictiveAnalytics.Insight> generateInsight(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/predictive-analytics/{}/insight", id);

        try {
            String insightType = request.get("insightType");
            String title = request.get("title");
            String description = request.get("description");
            String priorityStr = request.get("priority");

            ReportPredictiveAnalytics.InsightPriority priority =
                    ReportPredictiveAnalytics.InsightPriority.valueOf(priorityStr);

            ReportPredictiveAnalytics.Insight insight = analyticsService.generateInsight(
                    id, insightType, title, description, priority
            );

            return ResponseEntity.ok(insight);

        } catch (IllegalArgumentException e) {
            log.error("Predictive model not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error generating insight: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add recommendation
     */
    @PostMapping("/{id}/recommendation")
    public ResponseEntity<ReportPredictiveAnalytics.Recommendation> addRecommendation(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/predictive-analytics/{}/recommendation", id);

        try {
            String recommendationType = request.get("recommendationType");
            String title = request.get("title");
            String description = request.get("description");
            String targetEntity = request.get("targetEntity");
            String targetId = request.get("targetId");

            ReportPredictiveAnalytics.Recommendation recommendation = analyticsService.addRecommendation(
                    id, recommendationType, title, description, targetEntity, targetId
            );

            return ResponseEntity.ok(recommendation);

        } catch (IllegalArgumentException e) {
            log.error("Predictive model not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding recommendation: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update performance
     */
    @PutMapping("/{id}/performance")
    public ResponseEntity<Map<String, Object>> updatePerformance(
            @PathVariable Long id,
            @RequestBody ReportPredictiveAnalytics.ModelPerformance performance) {
        log.info("PUT /api/predictive-analytics/{}/performance", id);

        try {
            analyticsService.updatePerformance(id, performance);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Model performance updated");
            response.put("modelId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Predictive model not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating performance: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete model
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteModel(@PathVariable Long id) {
        log.info("DELETE /api/predictive-analytics/{}", id);

        try {
            analyticsService.deleteModel(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Predictive model deleted");
            response.put("modelId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting predictive model: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/predictive-analytics/stats");

        try {
            Map<String, Object> stats = analyticsService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching predictive analytics statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
