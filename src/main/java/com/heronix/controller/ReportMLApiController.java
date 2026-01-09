package com.heronix.controller;

import com.heronix.dto.ReportML;
import com.heronix.service.ReportMLService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Machine Learning API Controller
 *
 * REST API endpoints for machine learning and AI model management.
 *
 * Endpoints:
 * - POST /api/ml - Create ML model
 * - GET /api/ml/{id} - Get ML model
 * - GET /api/ml/report/{reportId} - Get models by report
 * - GET /api/ml/status/{status} - Get models by status
 * - POST /api/ml/{id}/train - Train model
 * - POST /api/ml/{id}/predict - Make prediction
 * - POST /api/ml/{id}/deploy - Deploy model
 * - POST /api/ml/{id}/monitor - Monitor model
 * - DELETE /api/ml/{id} - Delete model
 * - GET /api/ml/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 84 - Report Machine Learning & AI
 */
@RestController
@RequestMapping("/api/ml")
@RequiredArgsConstructor
@Slf4j
public class ReportMLApiController {

    private final ReportMLService mlService;

    /**
     * Create ML model
     */
    @PostMapping
    public ResponseEntity<ReportML> createModel(@RequestBody ReportML model) {
        log.info("POST /api/ml - Creating ML model for {} task", model.getTaskType());

        try {
            ReportML created = mlService.createModel(model);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating ML model", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get ML model
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportML> getModel(@PathVariable Long id) {
        log.info("GET /api/ml/{}", id);

        try {
            return mlService.getModel(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching ML model: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get models by report
     */
    @GetMapping("/report/{reportId}")
    public ResponseEntity<List<ReportML>> getModelsByReport(@PathVariable Long reportId) {
        log.info("GET /api/ml/report/{}", reportId);

        try {
            List<ReportML> models = mlService.getModelsByReport(reportId);
            return ResponseEntity.ok(models);

        } catch (Exception e) {
            log.error("Error fetching models for report: {}", reportId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get models by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReportML>> getModelsByStatus(@PathVariable ReportML.ModelStatus status) {
        log.info("GET /api/ml/status/{}", status);

        try {
            List<ReportML> models = mlService.getModelsByStatus(status);
            return ResponseEntity.ok(models);

        } catch (Exception e) {
            log.error("Error fetching models by status: {}", status, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Train model
     */
    @PostMapping("/{id}/train")
    public ResponseEntity<ReportML> trainModel(@PathVariable Long id) {
        log.info("POST /api/ml/{}/train", id);

        try {
            ReportML result = mlService.trainModel(id);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.error("ML model not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            log.error("Invalid state for training: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error training model: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Make prediction
     */
    @PostMapping("/{id}/predict")
    public ResponseEntity<ReportML.Prediction> predict(
            @PathVariable Long id,
            @RequestBody Map<String, Object> inputFeatures) {
        log.info("POST /api/ml/{}/predict", id);

        try {
            ReportML.Prediction prediction = mlService.predict(id, inputFeatures);
            return ResponseEntity.ok(prediction);

        } catch (IllegalArgumentException e) {
            log.error("ML model not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            log.error("Invalid state for prediction: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error making prediction with model: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Deploy model
     */
    @PostMapping("/{id}/deploy")
    public ResponseEntity<Map<String, Object>> deployModel(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/ml/{}/deploy", id);

        try {
            String environment = request.getOrDefault("environment", "production");
            String deployedBy = request.get("deployedBy");

            mlService.deployModel(id, environment, deployedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Model deployed successfully");
            response.put("modelId", id);
            response.put("environment", environment);
            response.put("deployedBy", deployedBy);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("ML model not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            log.error("Invalid state for deployment: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error deploying model: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Monitor model
     */
    @PostMapping("/{id}/monitor")
    public ResponseEntity<Map<String, Object>> monitorModel(@PathVariable Long id) {
        log.info("POST /api/ml/{}/monitor", id);

        try {
            mlService.monitorModel(id);

            // Get updated model
            ReportML model = mlService.getModel(id).orElseThrow();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Model monitoring completed");
            response.put("modelId", id);
            response.put("modelDriftDetected", model.getModelDriftDetected());
            response.put("dataDriftDetected", model.getDataDriftDetected());
            response.put("performanceDegradation", model.getPerformanceDegradation());
            response.put("retrainRecommended", model.getRetrainRecommended());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("ML model not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error monitoring model: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete model
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteModel(@PathVariable Long id) {
        log.info("DELETE /api/ml/{}", id);

        try {
            mlService.deleteModel(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Model deleted");
            response.put("modelId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting model: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/ml/stats");

        try {
            Map<String, Object> stats = mlService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching ML statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
