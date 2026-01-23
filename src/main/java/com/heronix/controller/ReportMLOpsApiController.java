package com.heronix.controller;

import com.heronix.dto.ReportMLOps;
import com.heronix.service.ReportMLOpsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report MLOps API Controller
 *
 * REST API endpoints for machine learning operations, model lifecycle management, and monitoring.
 *
 * Endpoints:
 * - POST /api/mlops - Create MLOps system
 * - GET /api/mlops/{id} - Get MLOps system
 * - POST /api/mlops/{id}/deploy - Deploy MLOps system
 * - POST /api/mlops/{id}/model - Register ML model
 * - POST /api/mlops/{id}/training - Start training job
 * - POST /api/mlops/{id}/training/{jobId}/complete - Complete training job
 * - POST /api/mlops/{id}/deployment - Deploy model
 * - POST /api/mlops/{id}/experiment - Create experiment
 * - POST /api/mlops/{id}/version - Create model version
 * - POST /api/mlops/{id}/predict - Make prediction
 * - POST /api/mlops/{id}/metrics - Record model metrics
 * - POST /api/mlops/{id}/pipeline - Create ML pipeline
 * - POST /api/mlops/{id}/drift - Detect drift
 * - POST /api/mlops/{id}/abtest - Create A/B test
 * - DELETE /api/mlops/{id} - Delete MLOps system
 * - GET /api/mlops/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 120 - Report Machine Learning Operations (MLOps)
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/mlops")
@RequiredArgsConstructor
@Slf4j
public class ReportMLOpsApiController {

    private final ReportMLOpsService mlopsService;

    /**
     * Create MLOps system
     */
    @PostMapping
    public ResponseEntity<ReportMLOps> createMLOpsSystem(@RequestBody ReportMLOps mlops) {
        log.info("POST /api/mlops - Creating MLOps system: {}", mlops.getMlopsName());

        try {
            ReportMLOps created = mlopsService.createMLOpsSystem(mlops);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating MLOps system", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get MLOps system
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportMLOps> getMLOpsSystem(@PathVariable Long id) {
        log.info("GET /api/mlops/{}", id);

        try {
            return mlopsService.getMLOpsSystem(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching MLOps system: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Deploy MLOps system
     */
    @PostMapping("/{id}/deploy")
    public ResponseEntity<Map<String, Object>> deployMLOpsSystem(@PathVariable Long id) {
        log.info("POST /api/mlops/{}/deploy", id);

        try {
            mlopsService.deployMLOpsSystem(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "MLOps system deployed");
            response.put("mlopsId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("MLOps system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error deploying MLOps system: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Register ML model
     */
    @PostMapping("/{id}/model")
    public ResponseEntity<ReportMLOps.MLModel> registerModel(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/mlops/{}/model", id);

        try {
            String modelName = request.get("modelName");
            String description = request.get("description");
            String modelTypeStr = request.get("modelType");
            String frameworkStr = request.get("framework");
            String algorithm = request.get("algorithm");
            String createdBy = request.get("createdBy");

            ReportMLOps.ModelType modelType = ReportMLOps.ModelType.valueOf(modelTypeStr);
            ReportMLOps.Framework framework = ReportMLOps.Framework.valueOf(frameworkStr);

            ReportMLOps.MLModel model = mlopsService.registerModel(
                    id, modelName, description, modelType, framework, algorithm, createdBy
            );

            return ResponseEntity.ok(model);

        } catch (IllegalArgumentException e) {
            log.error("MLOps system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error registering ML model: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Start training job
     */
    @PostMapping("/{id}/training")
    public ResponseEntity<ReportMLOps.TrainingJob> startTrainingJob(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/mlops/{}/training", id);

        try {
            String jobName = (String) request.get("jobName");
            String modelId = (String) request.get("modelId");
            String datasetId = (String) request.get("datasetId");
            Long trainingRecords = request.get("trainingRecords") != null ?
                    ((Number) request.get("trainingRecords")).longValue() : 10000L;
            Integer epochs = request.get("epochs") != null ?
                    ((Number) request.get("epochs")).intValue() : 10;
            Double learningRate = request.get("learningRate") != null ?
                    ((Number) request.get("learningRate")).doubleValue() : 0.001;
            Integer batchSize = request.get("batchSize") != null ?
                    ((Number) request.get("batchSize")).intValue() : 32;

            ReportMLOps.TrainingJob job = mlopsService.startTrainingJob(
                    id, jobName, modelId, datasetId, trainingRecords, epochs, learningRate, batchSize
            );

            return ResponseEntity.ok(job);

        } catch (IllegalArgumentException e) {
            log.error("MLOps system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error starting training job: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Complete training job
     */
    @PostMapping("/{id}/training/{jobId}/complete")
    public ResponseEntity<Map<String, Object>> completeTrainingJob(
            @PathVariable Long id,
            @PathVariable String jobId,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/mlops/{}/training/{}/complete", id, jobId);

        try {
            Boolean success = (Boolean) request.get("success");
            Double accuracy = request.get("accuracy") != null ?
                    ((Number) request.get("accuracy")).doubleValue() : 0.0;
            Double precision = request.get("precision") != null ?
                    ((Number) request.get("precision")).doubleValue() : 0.0;
            Double recall = request.get("recall") != null ?
                    ((Number) request.get("recall")).doubleValue() : 0.0;

            mlopsService.completeTrainingJob(id, jobId, success != null && success, accuracy, precision, recall);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Training job completed");
            response.put("jobId", jobId);
            response.put("success", success);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("MLOps system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error completing training job: {}", jobId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Deploy model
     */
    @PostMapping("/{id}/deployment")
    public ResponseEntity<ReportMLOps.ModelDeployment> deployModel(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/mlops/{}/deployment", id);

        try {
            String deploymentName = (String) request.get("deploymentName");
            String modelId = (String) request.get("modelId");
            String modelVersion = (String) request.get("modelVersion");
            String endpoint = (String) request.get("endpoint");
            Integer replicas = request.get("replicas") != null ?
                    ((Number) request.get("replicas")).intValue() : 1;
            String computeType = (String) request.get("computeType");

            ReportMLOps.ModelDeployment deployment = mlopsService.deployModel(
                    id, deploymentName, modelId, modelVersion, endpoint, replicas, computeType
            );

            return ResponseEntity.ok(deployment);

        } catch (IllegalArgumentException e) {
            log.error("MLOps system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error deploying model: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create experiment
     */
    @PostMapping("/{id}/experiment")
    public ResponseEntity<ReportMLOps.Experiment> createExperiment(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/mlops/{}/experiment", id);

        try {
            String experimentName = request.get("experimentName");
            String description = request.get("description");
            String modelId = request.get("modelId");
            String metricName = request.get("metricName");
            String createdBy = request.get("createdBy");

            ReportMLOps.Experiment experiment = mlopsService.createExperiment(
                    id, experimentName, description, modelId, metricName, createdBy
            );

            return ResponseEntity.ok(experiment);

        } catch (IllegalArgumentException e) {
            log.error("MLOps system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating experiment: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create model version
     */
    @PostMapping("/{id}/version")
    public ResponseEntity<ReportMLOps.ModelVersion> createModelVersion(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/mlops/{}/version", id);

        try {
            String modelId = (String) request.get("modelId");
            String version = (String) request.get("version");
            String description = (String) request.get("description");
            String artifactPath = (String) request.get("artifactPath");
            Double accuracy = request.get("accuracy") != null ?
                    ((Number) request.get("accuracy")).doubleValue() : 0.0;
            String createdBy = (String) request.get("createdBy");

            ReportMLOps.ModelVersion modelVersion = mlopsService.createModelVersion(
                    id, modelId, version, description, artifactPath, accuracy, createdBy
            );

            return ResponseEntity.ok(modelVersion);

        } catch (IllegalArgumentException e) {
            log.error("MLOps system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating model version: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Make prediction
     */
    @PostMapping("/{id}/predict")
    public ResponseEntity<ReportMLOps.Prediction> makePrediction(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/mlops/{}/predict", id);

        try {
            String modelId = (String) request.get("modelId");
            String modelVersion = (String) request.get("modelVersion");
            @SuppressWarnings("unchecked")
            Map<String, Object> inputData = (Map<String, Object>) request.get("inputData");
            @SuppressWarnings("unchecked")
            List<String> features = (List<String>) request.get("features");

            ReportMLOps.Prediction prediction = mlopsService.makePrediction(
                    id, modelId, modelVersion, inputData, features
            );

            return ResponseEntity.ok(prediction);

        } catch (IllegalArgumentException e) {
            log.error("MLOps system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error making prediction: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Record model metrics
     */
    @PostMapping("/{id}/metrics")
    public ResponseEntity<ReportMLOps.ModelMetrics> recordMetrics(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/mlops/{}/metrics", id);

        try {
            String modelId = (String) request.get("modelId");
            String deploymentId = (String) request.get("deploymentId");
            Double accuracy = request.get("accuracy") != null ?
                    ((Number) request.get("accuracy")).doubleValue() : 0.0;
            Double precision = request.get("precision") != null ?
                    ((Number) request.get("precision")).doubleValue() : 0.0;
            Double recall = request.get("recall") != null ?
                    ((Number) request.get("recall")).doubleValue() : 0.0;
            Long predictionCount = request.get("predictionCount") != null ?
                    ((Number) request.get("predictionCount")).longValue() : 0L;

            ReportMLOps.ModelMetrics metrics = mlopsService.recordMetrics(
                    id, modelId, deploymentId, accuracy, precision, recall, predictionCount
            );

            return ResponseEntity.ok(metrics);

        } catch (IllegalArgumentException e) {
            log.error("MLOps system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error recording metrics: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create ML pipeline
     */
    @PostMapping("/{id}/pipeline")
    public ResponseEntity<ReportMLOps.MLPipeline> createPipeline(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/mlops/{}/pipeline", id);

        try {
            String pipelineName = (String) request.get("pipelineName");
            String description = (String) request.get("description");
            @SuppressWarnings("unchecked")
            List<String> stages = (List<String>) request.get("stages");
            String schedule = (String) request.get("schedule");

            ReportMLOps.MLPipeline pipeline = mlopsService.createPipeline(
                    id, pipelineName, description, stages, schedule
            );

            return ResponseEntity.ok(pipeline);

        } catch (IllegalArgumentException e) {
            log.error("MLOps system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating ML pipeline: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Detect drift
     */
    @PostMapping("/{id}/drift")
    public ResponseEntity<ReportMLOps.DriftDetection> detectDrift(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/mlops/{}/drift", id);

        try {
            String modelId = request.get("modelId");
            String driftType = request.get("driftType");
            String detectionMethod = request.get("detectionMethod");

            ReportMLOps.DriftDetection drift = mlopsService.detectDrift(
                    id, modelId, driftType, detectionMethod
            );

            return ResponseEntity.ok(drift);

        } catch (IllegalArgumentException e) {
            log.error("MLOps system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error detecting drift: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create A/B test
     */
    @PostMapping("/{id}/abtest")
    public ResponseEntity<ReportMLOps.ABTest> createABTest(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/mlops/{}/abtest", id);

        try {
            String testName = (String) request.get("testName");
            String modelAId = (String) request.get("modelAId");
            String modelBId = (String) request.get("modelBId");
            Integer trafficSplitPercent = request.get("trafficSplitPercent") != null ?
                    ((Number) request.get("trafficSplitPercent")).intValue() : 50;

            ReportMLOps.ABTest abTest = mlopsService.createABTest(
                    id, testName, modelAId, modelBId, trafficSplitPercent
            );

            return ResponseEntity.ok(abTest);

        } catch (IllegalArgumentException e) {
            log.error("MLOps system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating A/B test: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete MLOps system
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteMLOpsSystem(@PathVariable Long id) {
        log.info("DELETE /api/mlops/{}", id);

        try {
            mlopsService.deleteMLOpsSystem(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "MLOps system deleted");
            response.put("mlopsId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting MLOps system: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/mlops/stats");

        try {
            Map<String, Object> stats = mlopsService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching MLOps statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
