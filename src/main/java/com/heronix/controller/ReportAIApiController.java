package com.heronix.controller;

import com.heronix.dto.ReportAI;
import com.heronix.service.ReportAIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report AI/ML API Controller
 *
 * REST API endpoints for AI/ML integration and analytics.
 *
 * Endpoints:
 * - POST /api/ai - Create AI system
 * - GET /api/ai/{id} - Get AI system
 * - POST /api/ai/{id}/start - Start AI system
 * - POST /api/ai/{id}/stop - Stop AI system
 * - POST /api/ai/{id}/model/train - Train model
 * - POST /api/ai/{id}/model/{modelId}/deploy - Deploy model
 * - POST /api/ai/{id}/model/{modelId}/predict - Make prediction
 * - POST /api/ai/{id}/insight - Generate insight
 * - POST /api/ai/{id}/anomaly - Detect anomaly
 * - POST /api/ai/{id}/recommendation - Generate recommendation
 * - PUT /api/ai/{id}/metrics - Update metrics
 * - DELETE /api/ai/{id} - Delete AI system
 * - GET /api/ai/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 100 - Report AI/ML Integration & Analytics
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class ReportAIApiController {

    private final ReportAIService aiService;

    /**
     * Create AI system
     */
    @PostMapping
    public ResponseEntity<ReportAI> createAISystem(@RequestBody ReportAI aiSystem) {
        log.info("POST /api/ai - Creating AI system: {}", aiSystem.getSystemName());

        try {
            ReportAI created = aiService.createAISystem(aiSystem);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating AI system", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get AI system
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportAI> getAISystem(@PathVariable Long id) {
        log.info("GET /api/ai/{}", id);

        try {
            return aiService.getAISystem(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching AI system: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Start AI system
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<Map<String, Object>> startAISystem(@PathVariable Long id) {
        log.info("POST /api/ai/{}/start", id);

        try {
            aiService.startAISystem(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "AI system started");
            response.put("aiSystemId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("AI system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error starting AI system: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Stop AI system
     */
    @PostMapping("/{id}/stop")
    public ResponseEntity<Map<String, Object>> stopAISystem(@PathVariable Long id) {
        log.info("POST /api/ai/{}/stop", id);

        try {
            aiService.stopAISystem(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "AI system stopped");
            response.put("aiSystemId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("AI system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error stopping AI system: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Train model
     */
    @PostMapping("/{id}/model/train")
    public ResponseEntity<ReportAI.MLModel> trainModel(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/ai/{}/model/train", id);

        try {
            String modelName = request.get("modelName");
            String modelTypeStr = request.get("modelType");
            String datasetId = request.get("datasetId");

            ReportAI.ModelType modelType = ReportAI.ModelType.valueOf(modelTypeStr);

            ReportAI.MLModel model = aiService.trainModel(id, modelName, modelType, datasetId);

            return ResponseEntity.ok(model);

        } catch (IllegalArgumentException e) {
            log.error("AI system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error training model in AI system: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Deploy model
     */
    @PostMapping("/{id}/model/{modelId}/deploy")
    public ResponseEntity<Map<String, Object>> deployModel(
            @PathVariable Long id,
            @PathVariable String modelId) {
        log.info("POST /api/ai/{}/model/{}/deploy", id, modelId);

        try {
            aiService.deployModel(id, modelId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Model deployed");
            response.put("aiSystemId", id);
            response.put("modelId", modelId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("AI system or model not found: {}, {}", id, modelId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error deploying model in AI system: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Make prediction
     */
    @PostMapping("/{id}/model/{modelId}/predict")
    public ResponseEntity<ReportAI.Prediction> makePrediction(
            @PathVariable Long id,
            @PathVariable String modelId,
            @RequestBody Map<String, Object> inputFeatures) {
        log.info("POST /api/ai/{}/model/{}/predict", id, modelId);

        try {
            ReportAI.Prediction prediction = aiService.makePrediction(id, modelId, inputFeatures);

            return ResponseEntity.ok(prediction);

        } catch (IllegalArgumentException e) {
            log.error("AI system or model not found: {}, {}", id, modelId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error making prediction in AI system: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Generate insight
     */
    @PostMapping("/{id}/insight")
    public ResponseEntity<ReportAI.Insight> generateInsight(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/ai/{}/insight", id);

        try {
            String insightType = request.get("insightType");
            String title = request.get("title");
            String description = request.get("description");

            ReportAI.Insight insight = aiService.generateInsight(id, insightType, title, description);

            return ResponseEntity.ok(insight);

        } catch (IllegalArgumentException e) {
            log.error("AI system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error generating insight in AI system: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Detect anomaly
     */
    @PostMapping("/{id}/anomaly")
    public ResponseEntity<ReportAI.Anomaly> detectAnomaly(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/ai/{}/anomaly", id);

        try {
            String anomalyType = (String) request.get("anomalyType");
            @SuppressWarnings("unchecked")
            Map<String, Object> dataPoint = (Map<String, Object>) request.get("dataPoint");

            ReportAI.Anomaly anomaly = aiService.detectAnomaly(id, anomalyType, dataPoint);

            return ResponseEntity.ok(anomaly);

        } catch (IllegalArgumentException e) {
            log.error("AI system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error detecting anomaly in AI system: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Generate recommendation
     */
    @PostMapping("/{id}/recommendation")
    public ResponseEntity<ReportAI.Recommendation> generateRecommendation(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/ai/{}/recommendation", id);

        try {
            String recommendationType = (String) request.get("recommendationType");
            String title = (String) request.get("title");
            @SuppressWarnings("unchecked")
            List<String> suggestedItems = (List<String>) request.get("suggestedItems");

            ReportAI.Recommendation recommendation = aiService.generateRecommendation(
                    id, recommendationType, title, suggestedItems
            );

            return ResponseEntity.ok(recommendation);

        } catch (IllegalArgumentException e) {
            log.error("AI system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error generating recommendation in AI system: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update metrics
     */
    @PutMapping("/{id}/metrics")
    public ResponseEntity<Map<String, Object>> updateMetrics(@PathVariable Long id) {
        log.info("PUT /api/ai/{}/metrics", id);

        try {
            aiService.updateMetrics(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Metrics updated");
            response.put("aiSystemId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("AI system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating metrics for AI system: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete AI system
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAISystem(@PathVariable Long id) {
        log.info("DELETE /api/ai/{}", id);

        try {
            aiService.deleteAISystem(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "AI system deleted");
            response.put("aiSystemId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting AI system: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/ai/stats");

        try {
            Map<String, Object> stats = aiService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching AI statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
