package com.heronix.controller;

import com.heronix.dto.ReportDigitalTwin;
import com.heronix.service.ReportDigitalTwinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Digital Twin API Controller
 *
 * REST API endpoints for digital twin management, simulations, and predictive analytics.
 *
 * Endpoints:
 * - POST /api/digital-twin - Create digital twin
 * - GET /api/digital-twin/{id} - Get digital twin
 * - POST /api/digital-twin/{id}/activate - Activate digital twin
 * - POST /api/digital-twin/{id}/model - Create twin model
 * - POST /api/digital-twin/{id}/simulation - Start simulation
 * - POST /api/digital-twin/{id}/simulation/{simulationId}/complete - Complete simulation
 * - POST /api/digital-twin/{id}/asset - Create virtual asset
 * - POST /api/digital-twin/{id}/state - Update twin state
 * - POST /api/digital-twin/{id}/scenario - Create scenario
 * - POST /api/digital-twin/{id}/prediction - Make prediction
 * - POST /api/digital-twin/{id}/analytics - Perform analytics
 * - POST /api/digital-twin/{id}/sync - Perform sync operation
 * - POST /api/digital-twin/{id}/optimization - Perform optimization
 * - DELETE /api/digital-twin/{id} - Delete digital twin
 * - GET /api/digital-twin/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 122 - Report Digital Twin & Simulation
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/digital-twin")
@RequiredArgsConstructor
@Slf4j
public class ReportDigitalTwinApiController {

    private final ReportDigitalTwinService digitalTwinService;

    /**
     * Create digital twin
     */
    @PostMapping
    public ResponseEntity<ReportDigitalTwin> createDigitalTwin(@RequestBody ReportDigitalTwin twin) {
        log.info("POST /api/digital-twin - Creating digital twin: {}", twin.getTwinName());

        try {
            ReportDigitalTwin created = digitalTwinService.createDigitalTwin(twin);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating digital twin", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get digital twin
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportDigitalTwin> getDigitalTwin(@PathVariable Long id) {
        log.info("GET /api/digital-twin/{}", id);

        try {
            return digitalTwinService.getDigitalTwin(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching digital twin: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Activate digital twin
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<Map<String, Object>> activateDigitalTwin(@PathVariable Long id) {
        log.info("POST /api/digital-twin/{}/activate", id);

        try {
            digitalTwinService.activateDigitalTwin(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Digital twin activated");
            response.put("twinId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Digital twin not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error activating digital twin: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create twin model
     */
    @PostMapping("/{id}/model")
    public ResponseEntity<ReportDigitalTwin.TwinModel> createTwinModel(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/digital-twin/{}/model", id);

        try {
            String modelName = request.get("modelName");
            String description = request.get("description");
            String assetTypeStr = request.get("assetType");
            String physicalAssetId = request.get("physicalAssetId");
            String behaviorModel = request.get("behaviorModel");

            ReportDigitalTwin.AssetType assetType =
                    ReportDigitalTwin.AssetType.valueOf(assetTypeStr);

            ReportDigitalTwin.TwinModel model = digitalTwinService.createTwinModel(
                    id, modelName, description, assetType, physicalAssetId, behaviorModel
            );

            return ResponseEntity.ok(model);

        } catch (IllegalArgumentException e) {
            log.error("Digital twin not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating twin model: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Start simulation
     */
    @PostMapping("/{id}/simulation")
    public ResponseEntity<ReportDigitalTwin.Simulation> startSimulation(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/digital-twin/{}/simulation", id);

        try {
            String simulationName = (String) request.get("simulationName");
            String modelId = (String) request.get("modelId");
            String scenarioId = (String) request.get("scenarioId");
            Integer timeStep = request.get("timeStep") != null ?
                    ((Number) request.get("timeStep")).intValue() : 1;
            Integer iterations = request.get("iterations") != null ?
                    ((Number) request.get("iterations")).intValue() : 100;
            @SuppressWarnings("unchecked")
            Map<String, Object> inputParameters = (Map<String, Object>) request.get("inputParameters");
            String createdBy = (String) request.get("createdBy");

            ReportDigitalTwin.Simulation simulation = digitalTwinService.startSimulation(
                    id, simulationName, modelId, scenarioId, timeStep, iterations, inputParameters, createdBy
            );

            return ResponseEntity.ok(simulation);

        } catch (IllegalArgumentException e) {
            log.error("Digital twin not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error starting simulation: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Complete simulation
     */
    @PostMapping("/{id}/simulation/{simulationId}/complete")
    public ResponseEntity<Map<String, Object>> completeSimulation(
            @PathVariable Long id,
            @PathVariable String simulationId,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/digital-twin/{}/simulation/{}/complete", id, simulationId);

        try {
            Boolean success = (Boolean) request.get("success");
            @SuppressWarnings("unchecked")
            Map<String, Object> results = (Map<String, Object>) request.get("results");

            digitalTwinService.completeSimulation(id, simulationId, success != null && success, results);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Simulation completed");
            response.put("simulationId", simulationId);
            response.put("success", success);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Digital twin not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error completing simulation: {}", simulationId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create virtual asset
     */
    @PostMapping("/{id}/asset")
    public ResponseEntity<ReportDigitalTwin.VirtualAsset> createVirtualAsset(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/digital-twin/{}/asset", id);

        try {
            String assetName = request.get("assetName");
            String assetTypeStr = request.get("assetType");
            String modelId = request.get("modelId");
            String location = request.get("location");

            ReportDigitalTwin.AssetType assetType =
                    ReportDigitalTwin.AssetType.valueOf(assetTypeStr);

            ReportDigitalTwin.VirtualAsset asset = digitalTwinService.createVirtualAsset(
                    id, assetName, assetType, modelId, location
            );

            return ResponseEntity.ok(asset);

        } catch (IllegalArgumentException e) {
            log.error("Digital twin not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating virtual asset: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update twin state
     */
    @PostMapping("/{id}/state")
    public ResponseEntity<ReportDigitalTwin.TwinState> updateTwinState(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/digital-twin/{}/state", id);

        try {
            String modelId = (String) request.get("modelId");
            @SuppressWarnings("unchecked")
            Map<String, Object> stateData = (Map<String, Object>) request.get("stateData");
            @SuppressWarnings("unchecked")
            Map<String, Object> measurements = (Map<String, Object>) request.get("measurements");
            String source = (String) request.get("source");

            ReportDigitalTwin.TwinState state = digitalTwinService.updateTwinState(
                    id, modelId, stateData, measurements, source
            );

            return ResponseEntity.ok(state);

        } catch (IllegalArgumentException e) {
            log.error("Digital twin not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating twin state: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create scenario
     */
    @PostMapping("/{id}/scenario")
    public ResponseEntity<ReportDigitalTwin.Scenario> createScenario(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/digital-twin/{}/scenario", id);

        try {
            String scenarioName = (String) request.get("scenarioName");
            String description = (String) request.get("description");
            String scenarioTypeStr = (String) request.get("scenarioType");
            @SuppressWarnings("unchecked")
            Map<String, Object> conditions = (Map<String, Object>) request.get("conditions");
            @SuppressWarnings("unchecked")
            List<String> affectedAssets = (List<String>) request.get("affectedAssets");
            String createdBy = (String) request.get("createdBy");

            ReportDigitalTwin.ScenarioType scenarioType =
                    ReportDigitalTwin.ScenarioType.valueOf(scenarioTypeStr);

            ReportDigitalTwin.Scenario scenario = digitalTwinService.createScenario(
                    id, scenarioName, description, scenarioType, conditions, affectedAssets, createdBy
            );

            return ResponseEntity.ok(scenario);

        } catch (IllegalArgumentException e) {
            log.error("Digital twin not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating scenario: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Make prediction
     */
    @PostMapping("/{id}/prediction")
    public ResponseEntity<ReportDigitalTwin.Prediction> makePrediction(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/digital-twin/{}/prediction", id);

        try {
            String modelId = (String) request.get("modelId");
            String predictionType = (String) request.get("predictionType");
            @SuppressWarnings("unchecked")
            Map<String, Object> inputData = (Map<String, Object>) request.get("inputData");
            String algorithm = (String) request.get("algorithm");

            ReportDigitalTwin.Prediction prediction = digitalTwinService.makePrediction(
                    id, modelId, predictionType, inputData, algorithm
            );

            return ResponseEntity.ok(prediction);

        } catch (IllegalArgumentException e) {
            log.error("Digital twin not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error making prediction: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Perform analytics
     */
    @PostMapping("/{id}/analytics")
    public ResponseEntity<ReportDigitalTwin.TwinAnalytics> performAnalytics(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/digital-twin/{}/analytics", id);

        try {
            String modelId = request.get("modelId");
            String analysisType = request.get("analysisType");

            ReportDigitalTwin.TwinAnalytics analytics = digitalTwinService.performAnalytics(
                    id, modelId, analysisType
            );

            return ResponseEntity.ok(analytics);

        } catch (IllegalArgumentException e) {
            log.error("Digital twin not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error performing analytics: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Perform sync operation
     */
    @PostMapping("/{id}/sync")
    public ResponseEntity<ReportDigitalTwin.SyncOperation> performSync(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/digital-twin/{}/sync", id);

        try {
            String modelId = request.get("modelId");
            String physicalAssetId = request.get("physicalAssetId");
            String syncDirection = request.get("syncDirection");

            ReportDigitalTwin.SyncOperation sync = digitalTwinService.performSync(
                    id, modelId, physicalAssetId, syncDirection
            );

            return ResponseEntity.ok(sync);

        } catch (IllegalArgumentException e) {
            log.error("Digital twin not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error performing sync: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Perform optimization
     */
    @PostMapping("/{id}/optimization")
    public ResponseEntity<ReportDigitalTwin.Optimization> performOptimization(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/digital-twin/{}/optimization", id);

        try {
            String optimizationName = (String) request.get("optimizationName");
            String targetMetric = (String) request.get("targetMetric");
            @SuppressWarnings("unchecked")
            List<String> constraints = (List<String>) request.get("constraints");
            @SuppressWarnings("unchecked")
            Map<String, Object> currentState = (Map<String, Object>) request.get("currentState");
            String algorithm = (String) request.get("algorithm");

            ReportDigitalTwin.Optimization optimization = digitalTwinService.performOptimization(
                    id, optimizationName, targetMetric, constraints, currentState, algorithm
            );

            return ResponseEntity.ok(optimization);

        } catch (IllegalArgumentException e) {
            log.error("Digital twin not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error performing optimization: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete digital twin
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteDigitalTwin(@PathVariable Long id) {
        log.info("DELETE /api/digital-twin/{}", id);

        try {
            digitalTwinService.deleteDigitalTwin(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Digital twin deleted");
            response.put("twinId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting digital twin: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/digital-twin/stats");

        try {
            Map<String, Object> stats = digitalTwinService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching digital twin statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
