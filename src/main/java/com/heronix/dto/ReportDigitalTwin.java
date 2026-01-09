package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Digital Twin & Simulation DTO
 *
 * Manages digital twin models, simulations, virtual replicas, and predictive modeling
 * for educational infrastructure, campus facilities, and learning environment optimization.
 *
 * Educational Use Cases:
 * - Virtual campus modeling and space utilization optimization
 * - Classroom environment simulation (temperature, lighting, acoustics)
 * - Student flow and traffic pattern simulation
 * - Energy consumption modeling and optimization
 * - Facility maintenance and predictive analytics
 * - Laboratory equipment digital twins for training
 * - Campus safety and emergency evacuation simulations
 * - Virtual learning environment modeling and optimization
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 122 - Report Digital Twin & Simulation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDigitalTwin {

    // Basic Information
    private Long twinId;
    private String twinName;
    private String description;
    private TwinStatus status;
    private String organizationId;
    private String twinType;

    // Configuration
    private String physicalAssetId;
    private String modelingEngine;
    private String simulationFramework;
    private Integer updateFrequencySeconds;
    private Boolean realTimeSync;
    private Double fidelityLevel;

    // State
    private Boolean isActive;
    private Boolean isSimulating;
    private LocalDateTime createdAt;
    private LocalDateTime lastSyncAt;
    private LocalDateTime lastSimulationAt;
    private String createdBy;

    // Twin Models
    private List<TwinModel> twinModels;
    private Map<String, TwinModel> modelRegistry;

    // Simulations
    private List<Simulation> simulations;
    private Map<String, Simulation> simulationRegistry;

    // Virtual Assets
    private List<VirtualAsset> virtualAssets;
    private Map<String, VirtualAsset> assetRegistry;

    // Twin States
    private List<TwinState> stateHistory;
    private Map<String, TwinState> stateRegistry;

    // Scenarios
    private List<Scenario> scenarios;
    private Map<String, Scenario> scenarioRegistry;

    // Predictions
    private List<Prediction> predictions;
    private Map<String, Prediction> predictionRegistry;

    // Analytics
    private List<TwinAnalytics> analyticsData;
    private Map<String, TwinAnalytics> analyticsRegistry;

    // Events
    private List<SimulationEvent> simulationEvents;
    private Map<String, SimulationEvent> eventRegistry;

    // Synchronizations
    private List<SyncOperation> syncOperations;
    private Map<String, SyncOperation> syncRegistry;

    // Optimizations
    private List<Optimization> optimizations;
    private Map<String, Optimization> optimizationRegistry;

    // Metrics
    private Long totalModels;
    private Long activeModels;
    private Long totalSimulations;
    private Long runningSimulations;
    private Long completedSimulations;
    private Long totalVirtualAssets;
    private Long totalPredictions;
    private Long totalSyncOperations;
    private Double averageAccuracy;
    private Double averageSimulationTime; // milliseconds
    private Long totalOptimizations;

    // Events
    private List<TwinEvent> events;

    /**
     * Twin status enumeration
     */
    public enum TwinStatus {
        INITIALIZING,
        MODELING,
        CALIBRATING,
        ACTIVE,
        SIMULATING,
        SYNCING,
        DEGRADED,
        OFFLINE
    }

    /**
     * Simulation status enumeration
     */
    public enum SimulationStatus {
        PENDING,
        INITIALIZING,
        RUNNING,
        PAUSED,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    /**
     * Asset type enumeration
     */
    public enum AssetType {
        BUILDING,
        CLASSROOM,
        LABORATORY,
        EQUIPMENT,
        HVAC_SYSTEM,
        LIGHTING_SYSTEM,
        NETWORK_INFRASTRUCTURE,
        STUDENT,
        FACULTY,
        CUSTOM
    }

    /**
     * Scenario type enumeration
     */
    public enum ScenarioType {
        NORMAL_OPERATION,
        PEAK_LOAD,
        EMERGENCY,
        MAINTENANCE,
        OPTIMIZATION,
        WHAT_IF,
        STRESS_TEST,
        PREDICTIVE
    }

    /**
     * Twin model data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TwinModel {
        private String modelId;
        private String modelName;
        private String description;
        private AssetType assetType;
        private String physicalAssetId;
        private String geometryData;
        private String behaviorModel;
        private Map<String, Object> properties;
        private Map<String, Object> parameters;
        private Double accuracy;
        private LocalDateTime createdAt;
        private LocalDateTime lastUpdatedAt;
        private Map<String, Object> metadata;
    }

    /**
     * Simulation data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Simulation {
        private String simulationId;
        private String simulationName;
        private SimulationStatus status;
        private String modelId;
        private String scenarioId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long duration; // milliseconds
        private Integer timeStep;
        private Integer iterations;
        private Integer currentIteration;
        private Double progressPercent;
        private Map<String, Object> inputParameters;
        private Map<String, Object> results;
        private LocalDateTime createdAt;
        private String createdBy;
    }

    /**
     * Virtual asset data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VirtualAsset {
        private String assetId;
        private String assetName;
        private AssetType assetType;
        private String modelId;
        private Map<String, Object> currentState;
        private Map<String, Object> properties;
        private String location;
        private Boolean isConnected;
        private LocalDateTime lastUpdateAt;
        private Map<String, Object> sensors;
        private Map<String, Object> actuators;
    }

    /**
     * Twin state data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TwinState {
        private String stateId;
        private String modelId;
        private Map<String, Object> stateData;
        private Map<String, Object> measurements;
        private Double confidence;
        private LocalDateTime timestamp;
        private String source; // PHYSICAL, SIMULATED, PREDICTED
        private Map<String, Object> metadata;
    }

    /**
     * Scenario data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Scenario {
        private String scenarioId;
        private String scenarioName;
        private String description;
        private ScenarioType scenarioType;
        private Map<String, Object> conditions;
        private Map<String, Object> parameters;
        private List<String> affectedAssets;
        private String expectedOutcome;
        private LocalDateTime createdAt;
        private String createdBy;
    }

    /**
     * Prediction data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Prediction {
        private String predictionId;
        private String modelId;
        private String predictionType;
        private Map<String, Object> inputData;
        private Object predictedValue;
        private Double confidence;
        private LocalDateTime predictionTime;
        private LocalDateTime validUntil;
        private Map<String, Object> features;
        private String algorithm;
    }

    /**
     * Twin analytics data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TwinAnalytics {
        private String analyticsId;
        private String modelId;
        private String analysisType;
        private Map<String, Object> metrics;
        private Map<String, Object> insights;
        private List<String> recommendations;
        private Double confidenceScore;
        private LocalDateTime analyzedAt;
        private Map<String, Object> visualizations;
    }

    /**
     * Simulation event data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimulationEvent {
        private String eventId;
        private String simulationId;
        private String eventType;
        private String description;
        private Map<String, Object> eventData;
        private Integer iteration;
        private LocalDateTime timestamp;
        private String severity;
    }

    /**
     * Sync operation data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncOperation {
        private String syncId;
        private String modelId;
        private String physicalAssetId;
        private String syncDirection; // PHYSICAL_TO_TWIN, TWIN_TO_PHYSICAL, BIDIRECTIONAL
        private Long recordsSynced;
        private Boolean successful;
        private Long duration; // milliseconds
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private String errorMessage;
    }

    /**
     * Optimization data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Optimization {
        private String optimizationId;
        private String optimizationName;
        private String targetMetric;
        private List<String> constraints;
        private Map<String, Object> currentState;
        private Map<String, Object> optimizedState;
        private Double improvement;
        private String algorithm;
        private LocalDateTime performedAt;
        private Map<String, Object> results;
    }

    /**
     * Twin event data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TwinEvent {
        private String eventId;
        private String eventType;
        private String description;
        private String targetType;
        private String targetId;
        private LocalDateTime timestamp;
        private String triggeredBy;
        private Map<String, Object> eventData;
    }

    // Helper methods

    /**
     * Activate digital twin
     */
    public void activateTwin() {
        this.status = TwinStatus.ACTIVE;
        this.isActive = true;
        this.lastSyncAt = LocalDateTime.now();
        recordEvent("TWIN_ACTIVATED", "Digital twin activated", "TWIN",
                twinId != null ? twinId.toString() : null);
    }

    /**
     * Add twin model
     */
    public void addTwinModel(TwinModel model) {
        if (twinModels == null) {
            twinModels = new ArrayList<>();
        }
        twinModels.add(model);

        if (modelRegistry == null) {
            modelRegistry = new HashMap<>();
        }
        modelRegistry.put(model.getModelId(), model);

        totalModels = (totalModels != null ? totalModels : 0L) + 1;
        activeModels = (activeModels != null ? activeModels : 0L) + 1;

        recordEvent("MODEL_ADDED", "Twin model added", "MODEL", model.getModelId());
    }

    /**
     * Start simulation
     */
    public void startSimulation(Simulation simulation) {
        if (simulations == null) {
            simulations = new ArrayList<>();
        }
        simulations.add(simulation);

        if (simulationRegistry == null) {
            simulationRegistry = new HashMap<>();
        }
        simulationRegistry.put(simulation.getSimulationId(), simulation);

        totalSimulations = (totalSimulations != null ? totalSimulations : 0L) + 1;
        if (simulation.getStatus() == SimulationStatus.RUNNING) {
            runningSimulations = (runningSimulations != null ? runningSimulations : 0L) + 1;
        }
        this.isSimulating = true;

        recordEvent("SIMULATION_STARTED", "Simulation started", "SIMULATION", simulation.getSimulationId());
    }

    /**
     * Complete simulation
     */
    public void completeSimulation(String simulationId, boolean success) {
        Simulation simulation = simulationRegistry != null ? simulationRegistry.get(simulationId) : null;
        if (simulation != null) {
            simulation.setStatus(success ? SimulationStatus.COMPLETED : SimulationStatus.FAILED);
            simulation.setEndTime(LocalDateTime.now());

            if (simulation.getStartTime() != null) {
                simulation.setDuration(
                    java.time.Duration.between(simulation.getStartTime(), simulation.getEndTime()).toMillis()
                );
            }

            if (success) {
                completedSimulations = (completedSimulations != null ? completedSimulations : 0L) + 1;
            }

            runningSimulations = (runningSimulations != null && runningSimulations > 0) ? runningSimulations - 1 : 0;
            if (runningSimulations == 0) {
                this.isSimulating = false;
            }

            // Update average simulation time
            if (simulation.getDuration() != null && totalSimulations != null && totalSimulations > 0) {
                if (averageSimulationTime == null) {
                    averageSimulationTime = simulation.getDuration().doubleValue();
                } else {
                    averageSimulationTime = (averageSimulationTime * (totalSimulations - 1) + simulation.getDuration()) / totalSimulations;
                }
            }
        }
    }

    /**
     * Add virtual asset
     */
    public void addVirtualAsset(VirtualAsset asset) {
        if (virtualAssets == null) {
            virtualAssets = new ArrayList<>();
        }
        virtualAssets.add(asset);

        if (assetRegistry == null) {
            assetRegistry = new HashMap<>();
        }
        assetRegistry.put(asset.getAssetId(), asset);

        totalVirtualAssets = (totalVirtualAssets != null ? totalVirtualAssets : 0L) + 1;

        recordEvent("ASSET_ADDED", "Virtual asset added", "ASSET", asset.getAssetId());
    }

    /**
     * Record prediction
     */
    public void recordPrediction(Prediction prediction) {
        if (predictions == null) {
            predictions = new ArrayList<>();
        }
        predictions.add(prediction);

        if (predictionRegistry == null) {
            predictionRegistry = new HashMap<>();
        }
        predictionRegistry.put(prediction.getPredictionId(), prediction);

        totalPredictions = (totalPredictions != null ? totalPredictions : 0L) + 1;

        // Update average accuracy
        if (prediction.getConfidence() != null) {
            if (averageAccuracy == null) {
                averageAccuracy = prediction.getConfidence();
            } else {
                averageAccuracy = (averageAccuracy + prediction.getConfidence()) / 2.0;
            }
        }
    }

    /**
     * Perform sync operation
     */
    public void performSync(SyncOperation sync) {
        if (syncOperations == null) {
            syncOperations = new ArrayList<>();
        }
        syncOperations.add(sync);

        if (syncRegistry == null) {
            syncRegistry = new HashMap<>();
        }
        syncRegistry.put(sync.getSyncId(), sync);

        totalSyncOperations = (totalSyncOperations != null ? totalSyncOperations : 0L) + 1;
        this.lastSyncAt = LocalDateTime.now();

        recordEvent("SYNC_COMPLETED", "Sync operation completed", "SYNC", sync.getSyncId());
    }

    /**
     * Add optimization
     */
    public void addOptimization(Optimization optimization) {
        if (optimizations == null) {
            optimizations = new ArrayList<>();
        }
        optimizations.add(optimization);

        if (optimizationRegistry == null) {
            optimizationRegistry = new HashMap<>();
        }
        optimizationRegistry.put(optimization.getOptimizationId(), optimization);

        totalOptimizations = (totalOptimizations != null ? totalOptimizations : 0L) + 1;

        recordEvent("OPTIMIZATION_PERFORMED", "Optimization performed", "OPTIMIZATION", optimization.getOptimizationId());
    }

    /**
     * Record twin event
     */
    private void recordEvent(String eventType, String description, String targetType, String targetId) {
        if (events == null) {
            events = new ArrayList<>();
        }

        TwinEvent event = TwinEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType(eventType)
                .description(description)
                .targetType(targetType)
                .targetId(targetId)
                .timestamp(LocalDateTime.now())
                .triggeredBy(createdBy)
                .build();

        events.add(event);
    }
}
