package com.heronix.controller;

import com.heronix.dto.ReportIntelligence;
import com.heronix.service.ReportIntelligenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Intelligence API Controller
 *
 * REST API endpoints for intelligent insights and analytics.
 *
 * Endpoints:
 * - POST /api/intelligence - Create intelligence
 * - GET /api/intelligence/{id} - Get intelligence
 * - GET /api/intelligence/report/{reportId} - Get intelligence by report
 * - POST /api/intelligence/{id}/insight - Generate insight
 * - POST /api/intelligence/{id}/anomaly - Detect anomaly
 * - POST /api/intelligence/{id}/pattern - Recognize pattern
 * - POST /api/intelligence/{id}/trend - Analyze trend
 * - POST /api/intelligence/{id}/prediction - Create prediction
 * - POST /api/intelligence/{id}/recommendation - Generate recommendation
 * - POST /api/intelligence/{id}/rootcause - Perform root cause analysis
 * - POST /api/intelligence/{id}/impact - Assess impact
 * - POST /api/intelligence/{id}/insight/{insightId}/acknowledge - Acknowledge insight
 * - POST /api/intelligence/{id}/anomaly/{anomalyId}/resolve - Resolve anomaly
 * - POST /api/intelligence/{id}/recommendation/{recommendationId}/implement - Implement recommendation
 * - POST /api/intelligence/{id}/prediction/{predictionId}/validate - Validate prediction
 * - DELETE /api/intelligence/{id} - Delete intelligence
 * - GET /api/intelligence/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 89 - Report Intelligence & Insights
 */
@RestController
@RequestMapping("/api/intelligence")
@RequiredArgsConstructor
@Slf4j
public class ReportIntelligenceApiController {

    private final ReportIntelligenceService intelligenceService;

    /**
     * Create intelligence
     */
    @PostMapping
    public ResponseEntity<ReportIntelligence> createIntelligence(@RequestBody ReportIntelligence intelligence) {
        log.info("POST /api/intelligence - Creating intelligence for report {}", intelligence.getReportId());

        try {
            ReportIntelligence created = intelligenceService.createIntelligence(intelligence);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating intelligence", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get intelligence
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportIntelligence> getIntelligence(@PathVariable Long id) {
        log.info("GET /api/intelligence/{}", id);

        try {
            return intelligenceService.getIntelligence(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching intelligence: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get intelligence by report
     */
    @GetMapping("/report/{reportId}")
    public ResponseEntity<ReportIntelligence> getIntelligenceByReport(@PathVariable Long reportId) {
        log.info("GET /api/intelligence/report/{}", reportId);

        try {
            return intelligenceService.getIntelligenceByReport(reportId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching intelligence for report: {}", reportId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generate insight
     */
    @PostMapping("/{id}/insight")
    public ResponseEntity<ReportIntelligence.Insight> generateInsight(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/intelligence/{}/insight", id);

        try {
            String title = (String) request.get("title");
            String description = (String) request.get("description");
            String typeStr = (String) request.get("type");
            Double confidenceScore = ((Number) request.get("confidenceScore")).doubleValue();

            ReportIntelligence.IntelligenceType type = ReportIntelligence.IntelligenceType.valueOf(typeStr);

            ReportIntelligence.Insight insight = intelligenceService.generateInsight(
                    id, title, description, type, confidenceScore
            );

            return ResponseEntity.ok(insight);

        } catch (IllegalArgumentException e) {
            log.error("Intelligence not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error generating insight for intelligence: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Detect anomaly
     */
    @PostMapping("/{id}/anomaly")
    public ResponseEntity<ReportIntelligence.Anomaly> detectAnomaly(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/intelligence/{}/anomaly", id);

        try {
            String metricName = (String) request.get("metricName");
            Double expectedValue = ((Number) request.get("expectedValue")).doubleValue();
            Double actualValue = ((Number) request.get("actualValue")).doubleValue();

            ReportIntelligence.Anomaly anomaly = intelligenceService.detectAnomaly(
                    id, metricName, expectedValue, actualValue
            );

            return ResponseEntity.ok(anomaly);

        } catch (IllegalArgumentException e) {
            log.error("Intelligence not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            log.error("Invalid state for anomaly detection: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error detecting anomaly for intelligence: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Recognize pattern
     */
    @PostMapping("/{id}/pattern")
    public ResponseEntity<ReportIntelligence.Pattern> recognizePattern(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/intelligence/{}/pattern", id);

        try {
            String patternName = (String) request.get("patternName");
            String patternType = (String) request.get("patternType");
            Double confidence = ((Number) request.get("confidence")).doubleValue();

            ReportIntelligence.Pattern pattern = intelligenceService.recognizePattern(
                    id, patternName, patternType, confidence
            );

            return ResponseEntity.ok(pattern);

        } catch (IllegalArgumentException e) {
            log.error("Intelligence not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error recognizing pattern for intelligence: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Analyze trend
     */
    @PostMapping("/{id}/trend")
    public ResponseEntity<ReportIntelligence.Trend> analyzeTrend(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/intelligence/{}/trend", id);

        try {
            String metricName = (String) request.get("metricName");

            @SuppressWarnings("unchecked")
            List<Object> valuesObj = (List<Object>) request.get("values");
            List<Double> values = valuesObj.stream()
                    .map(v -> ((Number) v).doubleValue())
                    .toList();

            ReportIntelligence.Trend trend = intelligenceService.analyzeTrend(
                    id, metricName, values
            );

            return ResponseEntity.ok(trend);

        } catch (IllegalArgumentException e) {
            log.error("Intelligence not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error analyzing trend for intelligence: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create prediction
     */
    @PostMapping("/{id}/prediction")
    public ResponseEntity<ReportIntelligence.Prediction> createPrediction(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/intelligence/{}/prediction", id);

        try {
            String metricName = (String) request.get("metricName");
            Double predictedValue = ((Number) request.get("predictedValue")).doubleValue();
            String targetDateStr = (String) request.get("targetDate");

            LocalDateTime targetDate = targetDateStr != null ?
                    LocalDateTime.parse(targetDateStr) : LocalDateTime.now().plusDays(30);

            ReportIntelligence.Prediction prediction = intelligenceService.createPrediction(
                    id, metricName, predictedValue, targetDate
            );

            return ResponseEntity.ok(prediction);

        } catch (IllegalArgumentException e) {
            log.error("Intelligence not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            log.error("Invalid state for prediction: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error creating prediction for intelligence: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Generate recommendation
     */
    @PostMapping("/{id}/recommendation")
    public ResponseEntity<ReportIntelligence.Recommendation> generateRecommendation(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/intelligence/{}/recommendation", id);

        try {
            String title = request.get("title");
            String description = request.get("description");
            String priorityStr = request.get("priority");

            ReportIntelligence.RecommendationPriority priority =
                    ReportIntelligence.RecommendationPriority.valueOf(priorityStr);

            ReportIntelligence.Recommendation recommendation = intelligenceService.generateRecommendation(
                    id, title, description, priority
            );

            return ResponseEntity.ok(recommendation);

        } catch (IllegalArgumentException e) {
            log.error("Intelligence not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error generating recommendation for intelligence: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Perform root cause analysis
     */
    @PostMapping("/{id}/rootcause")
    public ResponseEntity<ReportIntelligence.RootCause> performRootCauseAnalysis(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/intelligence/{}/rootcause", id);

        try {
            String issueDescription = request.get("issueDescription");

            ReportIntelligence.RootCause rootCause = intelligenceService.performRootCauseAnalysis(
                    id, issueDescription
            );

            return ResponseEntity.ok(rootCause);

        } catch (IllegalArgumentException e) {
            log.error("Intelligence not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error performing root cause analysis for intelligence: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Assess impact
     */
    @PostMapping("/{id}/impact")
    public ResponseEntity<ReportIntelligence.Impact> assessImpact(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/intelligence/{}/impact", id);

        try {
            String description = request.get("description");
            String levelStr = request.get("level");

            ReportIntelligence.ImpactLevel level = ReportIntelligence.ImpactLevel.valueOf(levelStr);

            ReportIntelligence.Impact impact = intelligenceService.assessImpact(
                    id, description, level
            );

            return ResponseEntity.ok(impact);

        } catch (IllegalArgumentException e) {
            log.error("Intelligence not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error assessing impact for intelligence: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Acknowledge insight
     */
    @PostMapping("/{id}/insight/{insightId}/acknowledge")
    public ResponseEntity<Map<String, Object>> acknowledgeInsight(
            @PathVariable Long id,
            @PathVariable String insightId) {
        log.info("POST /api/intelligence/{}/insight/{}/acknowledge", id, insightId);

        try {
            intelligenceService.acknowledgeInsight(id, insightId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Insight acknowledged");
            response.put("intelligenceId", id);
            response.put("insightId", insightId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Intelligence not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error acknowledging insight in intelligence: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Resolve anomaly
     */
    @PostMapping("/{id}/anomaly/{anomalyId}/resolve")
    public ResponseEntity<Map<String, Object>> resolveAnomaly(
            @PathVariable Long id,
            @PathVariable String anomalyId,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/intelligence/{}/anomaly/{}/resolve", id, anomalyId);

        try {
            String resolution = request.get("resolution");

            intelligenceService.resolveAnomaly(id, anomalyId, resolution);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Anomaly resolved");
            response.put("intelligenceId", id);
            response.put("anomalyId", anomalyId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Intelligence not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error resolving anomaly in intelligence: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Implement recommendation
     */
    @PostMapping("/{id}/recommendation/{recommendationId}/implement")
    public ResponseEntity<Map<String, Object>> implementRecommendation(
            @PathVariable Long id,
            @PathVariable String recommendationId,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/intelligence/{}/recommendation/{}/implement", id, recommendationId);

        try {
            String implementedBy = request.get("implementedBy");
            String outcome = request.get("outcome");

            intelligenceService.implementRecommendation(id, recommendationId, implementedBy, outcome);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Recommendation implemented");
            response.put("intelligenceId", id);
            response.put("recommendationId", recommendationId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Intelligence not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error implementing recommendation in intelligence: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Validate prediction
     */
    @PostMapping("/{id}/prediction/{predictionId}/validate")
    public ResponseEntity<Map<String, Object>> validatePrediction(
            @PathVariable Long id,
            @PathVariable String predictionId,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/intelligence/{}/prediction/{}/validate", id, predictionId);

        try {
            Double actualValue = ((Number) request.get("actualValue")).doubleValue();

            intelligenceService.validatePrediction(id, predictionId, actualValue);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Prediction validated");
            response.put("intelligenceId", id);
            response.put("predictionId", predictionId);
            response.put("actualValue", actualValue);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Intelligence not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error validating prediction in intelligence: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete intelligence
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteIntelligence(@PathVariable Long id) {
        log.info("DELETE /api/intelligence/{}", id);

        try {
            intelligenceService.deleteIntelligence(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Intelligence deleted");
            response.put("intelligenceId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting intelligence: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/intelligence/stats");

        try {
            Map<String, Object> stats = intelligenceService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching intelligence statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
