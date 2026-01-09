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
 * Report AI DTO
 *
 * Represents AI/ML integration for intelligent report generation and analytics.
 *
 * Features:
 * - ML model training and deployment
 * - Predictive analytics
 * - Automated insights generation
 * - Natural language processing
 * - Anomaly detection
 * - Pattern recognition
 * - Recommendation engine
 * - Auto-categorization
 *
 * Model Status:
 * - TRAINING - Model being trained
 * - TRAINED - Model trained and ready
 * - DEPLOYED - Model deployed
 * - FAILED - Training failed
 * - DEPRECATED - Model deprecated
 *
 * Prediction Status:
 * - PENDING - Prediction pending
 * - COMPLETED - Prediction completed
 * - FAILED - Prediction failed
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 100 - Report AI/ML Integration & Analytics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportAI {

    private Long aiSystemId;
    private String systemName;
    private String description;
    private String version;

    // System status
    private SystemStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private Boolean isActive;

    // ML Platform
    private MLPlatform mlPlatform;
    private String platformUrl;
    private String apiKey;
    private Map<String, String> platformConfig;

    // Models
    private List<MLModel> models;
    private Integer totalModels;
    private Integer deployedModels;
    private Map<String, MLModel> modelRegistry;

    // Training
    private Boolean trainingEnabled;
    private List<TrainingJob> trainingJobs;
    private Integer activeTrainingJobs;
    private Map<String, TrainingJob> trainingJobRegistry;

    // Predictions
    private List<Prediction> predictions;
    private Long totalPredictions;
    private Long successfulPredictions;
    private Long failedPredictions;
    private Map<String, Prediction> predictionRegistry;

    // Insights
    private Boolean insightsEnabled;
    private List<Insight> insights;
    private Long totalInsights;
    private Map<String, Insight> insightRegistry;

    // Anomalies
    private Boolean anomalyDetectionEnabled;
    private List<Anomaly> anomalies;
    private Long totalAnomalies;
    private AnomalyThreshold anomalyThreshold;

    // Recommendations
    private Boolean recommendationsEnabled;
    private List<Recommendation> recommendations;
    private Long totalRecommendations;

    // NLP
    private Boolean nlpEnabled;
    private List<NLPTask> nlpTasks;
    private Map<String, String> nlpModels;

    // AutoML
    private Boolean autoMLEnabled;
    private AutoMLConfig autoMLConfig;
    private List<AutoMLExperiment> autoMLExperiments;

    // Feature engineering
    private List<Feature> features;
    private Map<String, Feature> featureRegistry;
    private Boolean autoFeatureEngineering;

    // Model versioning
    private Boolean versioningEnabled;
    private Map<String, List<MLModel>> modelVersions;

    // Metrics
    private AIMetrics metrics;
    private List<ModelMetrics> modelMetricsList;

    // Events
    private List<AIEvent> events;
    private LocalDateTime lastEventAt;

    // Configuration
    private Map<String, Object> configuration;
    private Boolean configurationLocked;

    /**
     * System Status
     */
    public enum SystemStatus {
        INITIALIZING,   // System initializing
        ACTIVE,         // System active
        TRAINING,       // Training in progress
        DEGRADED,       // Degraded performance
        ERROR,          // Error state
        OFFLINE         // System offline
    }

    /**
     * Model Status
     */
    public enum ModelStatus {
        TRAINING,       // Model being trained
        TRAINED,        // Model trained and ready
        DEPLOYED,       // Model deployed
        EVALUATING,     // Model being evaluated
        FAILED,         // Training failed
        DEPRECATED      // Model deprecated
    }

    /**
     * Prediction Status
     */
    public enum PredictionStatus {
        PENDING,        // Prediction pending
        COMPLETED,      // Prediction completed
        FAILED          // Prediction failed
    }

    /**
     * ML Platform
     */
    public enum MLPlatform {
        TENSORFLOW,     // TensorFlow
        PYTORCH,        // PyTorch
        SCIKIT_LEARN,   // Scikit-learn
        XGBOOST,        // XGBoost
        H2O,            // H2O.ai
        AWS_SAGEMAKER,  // AWS SageMaker
        AZURE_ML,       // Azure ML
        GOOGLE_AI       // Google AI Platform
    }

    /**
     * Model Type
     */
    public enum ModelType {
        CLASSIFICATION, // Classification
        REGRESSION,     // Regression
        CLUSTERING,     // Clustering
        TIME_SERIES,    // Time series forecasting
        ANOMALY_DETECTION, // Anomaly detection
        NLP,            // Natural Language Processing
        RECOMMENDATION, // Recommendation
        COMPUTER_VISION // Computer Vision
    }

    /**
     * ML Model
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MLModel {
        private String modelId;
        private String modelName;
        private ModelType modelType;
        private ModelStatus status;

        // Version
        private String version;
        private Integer versionNumber;
        private Boolean isLatest;

        // Training
        private String trainingJobId;
        private LocalDateTime trainedAt;
        private String trainedBy;
        private Long trainingDurationMs;

        // Dataset
        private String datasetId;
        private Long trainingDataSize;
        private Long validationDataSize;
        private Long testDataSize;

        // Performance
        private Double accuracy;
        private Double precision;
        private Double recall;
        private Double f1Score;
        private Double mse; // Mean Squared Error
        private Double mae; // Mean Absolute Error
        private Double r2Score;

        // Hyperparameters
        private Map<String, Object> hyperparameters;

        // Deployment
        private Boolean deployed;
        private LocalDateTime deployedAt;
        private String endpointUrl;

        // Usage
        private Long totalPredictions;
        private Long successfulPredictions;
        private Long failedPredictions;
        private LocalDateTime lastUsedAt;

        // Model artifacts
        private String modelPath;
        private String modelFormat; // H5, ONNX, PMML, PICKLE
        private Long modelSizeMb;

        // Metadata
        private Map<String, String> tags;
        private Map<String, Object> metadata;
    }

    /**
     * Training Job
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrainingJob {
        private String jobId;
        private String jobName;
        private String modelId;
        private ModelType modelType;
        private String status; // PENDING, RUNNING, COMPLETED, FAILED

        // Dataset
        private String datasetId;
        private String datasetPath;
        private Long datasetSize;

        // Training config
        private Map<String, Object> hyperparameters;
        private Integer epochs;
        private Integer batchSize;
        private Double learningRate;

        // Progress
        private Integer currentEpoch;
        private Double trainingLoss;
        private Double validationLoss;
        private Double trainingAccuracy;
        private Double validationAccuracy;

        // Timing
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private Long durationMs;
        private Long estimatedTimeRemainingMs;

        // Resources
        private String instanceType;
        private Integer gpuCount;
        private Double cpuUsagePercent;
        private Double memoryUsageMb;

        // Results
        private String resultModelId;
        private Map<String, Double> finalMetrics;

        // Logs
        private String logPath;
        private List<String> logMessages;

        // Metadata
        private Map<String, Object> metadata;
    }

    /**
     * Prediction
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Prediction {
        private String predictionId;
        private String modelId;
        private String modelName;
        private PredictionStatus status;

        // Input
        private Map<String, Object> inputFeatures;
        private String inputDataId;

        // Output
        private Map<String, Object> output;
        private Object predictedValue;
        private Double confidence;
        private List<String> predictedClasses;
        private Map<String, Double> classProbabilities;

        // Timing
        private LocalDateTime requestedAt;
        private LocalDateTime completedAt;
        private Long latencyMs;

        // Results
        private Boolean success;
        private String errorMessage;

        // Context
        private String reportId;
        private String userId;

        // Metadata
        private Map<String, Object> metadata;
    }

    /**
     * Insight
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Insight {
        private String insightId;
        private String insightType; // TREND, CORRELATION, OUTLIER, PATTERN
        private String title;
        private String description;

        // Confidence
        private Double confidence;
        private String severity; // LOW, MEDIUM, HIGH, CRITICAL

        // Data
        private String dataSourceId;
        private Map<String, Object> dataPoints;
        private String visualizationType;

        // Time range
        private LocalDateTime startDate;
        private LocalDateTime endDate;

        // Impact
        private String impactArea;
        private Double estimatedImpact;

        // Recommendations
        private List<String> suggestedActions;

        // Discovery
        private LocalDateTime discoveredAt;
        private String discoveredBy; // Model or user

        // Metadata
        private Map<String, String> tags;
        private Map<String, Object> metadata;
    }

    /**
     * Anomaly
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Anomaly {
        private String anomalyId;
        private String anomalyType;

        // Detection
        private LocalDateTime detectedAt;
        private String detectedBy; // Model ID
        private Double anomalyScore;
        private Double threshold;

        // Data
        private String dataSourceId;
        private Map<String, Object> dataPoint;
        private Map<String, Object> expectedValues;
        private Map<String, Object> actualValues;

        // Deviation
        private Double deviation;
        private String deviationUnit;

        // Impact
        private String severity; // LOW, MEDIUM, HIGH, CRITICAL
        private String status; // NEW, INVESTIGATING, RESOLVED, FALSE_POSITIVE

        // Investigation
        private String assignedTo;
        private String resolution;
        private LocalDateTime resolvedAt;

        // Metadata
        private Map<String, Object> metadata;
    }

    /**
     * Recommendation
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Recommendation {
        private String recommendationId;
        private String recommendationType; // REPORT_TYPE, VISUALIZATION, METRIC, FILTER

        // Recommendation
        private String title;
        private String description;
        private List<String> suggestedItems;

        // Confidence
        private Double confidence;
        private String reasoning;

        // Context
        private String userId;
        private String reportId;
        private Map<String, Object> userContext;

        // Relevance
        private Double relevanceScore;
        private List<String> basedOn; // Historical patterns, similar users, etc.

        // Timing
        private LocalDateTime generatedAt;
        private LocalDateTime expiresAt;

        // Feedback
        private Boolean accepted;
        private String feedback;

        // Metadata
        private Map<String, Object> metadata;
    }

    /**
     * NLP Task
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NLPTask {
        private String taskId;
        private String taskType; // SENTIMENT, ENTITY_EXTRACTION, SUMMARIZATION, CLASSIFICATION

        // Input
        private String inputText;
        private String language;

        // Output
        private Map<String, Object> result;
        private Double confidence;

        // Sentiment (if applicable)
        private String sentiment; // POSITIVE, NEGATIVE, NEUTRAL
        private Double sentimentScore;

        // Entities (if applicable)
        private List<Entity> entities;

        // Summary (if applicable)
        private String summary;

        // Timing
        private LocalDateTime processedAt;
        private Long processingTimeMs;

        // Metadata
        private Map<String, Object> metadata;
    }

    /**
     * Entity
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Entity {
        private String text;
        private String type; // PERSON, ORGANIZATION, LOCATION, DATE, etc.
        private Double confidence;
        private Integer startPosition;
        private Integer endPosition;
    }

    /**
     * AutoML Config
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AutoMLConfig {
        private Boolean enabled;
        private Integer maxTrials;
        private Long maxRuntimeSeconds;
        private String optimizationMetric;
        private List<String> algorithms;
        private Map<String, Object> searchSpace;
    }

    /**
     * AutoML Experiment
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AutoMLExperiment {
        private String experimentId;
        private String experimentName;
        private String status; // RUNNING, COMPLETED, FAILED

        // Config
        private Integer totalTrials;
        private Integer completedTrials;
        private String bestModelId;
        private Double bestScore;

        // Timing
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;

        // Results
        private List<String> triedModels;
        private Map<String, Double> modelScores;

        // Metadata
        private Map<String, Object> metadata;
    }

    /**
     * Feature
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Feature {
        private String featureId;
        private String featureName;
        private String featureType; // NUMERICAL, CATEGORICAL, TEXT, DATE

        // Importance
        private Double importance;
        private Integer rank;

        // Statistics
        private Double mean;
        private Double median;
        private Double stdDev;
        private Object minValue;
        private Object maxValue;

        // Engineering
        private String transformation; // LOG, NORMALIZE, ONE_HOT, etc.
        private Boolean derived;
        private String derivationFormula;

        // Metadata
        private Map<String, Object> metadata;
    }

    /**
     * Anomaly Threshold
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnomalyThreshold {
        private Double lowThreshold;
        private Double mediumThreshold;
        private Double highThreshold;
        private Double criticalThreshold;
        private String thresholdUnit;
    }

    /**
     * AI Metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIMetrics {
        private Integer totalModels;
        private Integer deployedModels;
        private Long totalPredictions;
        private Long successfulPredictions;
        private Long failedPredictions;
        private Double predictionSuccessRate;
        private Double averagePredictionLatencyMs;
        private Long totalInsights;
        private Long totalAnomalies;
        private Long totalRecommendations;
        private Integer activeTrainingJobs;
        private LocalDateTime measuredAt;
    }

    /**
     * Model Metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelMetrics {
        private String modelId;
        private String modelName;
        private ModelType modelType;
        private Long predictions;
        private Long successCount;
        private Long errorCount;
        private Double successRate;
        private Double averageLatencyMs;
        private Double averageConfidence;
        private LocalDateTime measuredAt;
    }

    /**
     * AI Event
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIEvent {
        private String eventId;
        private String eventType;
        private String description;
        private LocalDateTime timestamp;
        private String resourceType; // MODEL, PREDICTION, INSIGHT, ANOMALY
        private String resourceId;
        private Map<String, Object> eventData;
    }

    /**
     * Helper Methods
     */

    public void registerModel(MLModel model) {
        if (models == null) {
            models = new ArrayList<>();
        }
        models.add(model);

        if (modelRegistry == null) {
            modelRegistry = new HashMap<>();
        }
        modelRegistry.put(model.getModelId(), model);

        totalModels = (totalModels != null ? totalModels : 0) + 1;
        if (Boolean.TRUE.equals(model.getDeployed())) {
            deployedModels = (deployedModels != null ? deployedModels : 0) + 1;
        }

        recordEvent("MODEL_REGISTERED", "ML model registered: " + model.getModelName(),
                "MODEL", model.getModelId());
    }

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
        if (Boolean.TRUE.equals(prediction.getSuccess())) {
            successfulPredictions = (successfulPredictions != null ? successfulPredictions : 0L) + 1;
        } else {
            failedPredictions = (failedPredictions != null ? failedPredictions : 0L) + 1;
        }
    }

    public void addInsight(Insight insight) {
        if (insights == null) {
            insights = new ArrayList<>();
        }
        insights.add(insight);

        if (insightRegistry == null) {
            insightRegistry = new HashMap<>();
        }
        insightRegistry.put(insight.getInsightId(), insight);

        totalInsights = (totalInsights != null ? totalInsights : 0L) + 1;

        recordEvent("INSIGHT_GENERATED", "Insight generated: " + insight.getTitle(),
                "INSIGHT", insight.getInsightId());
    }

    public void recordAnomaly(Anomaly anomaly) {
        if (anomalies == null) {
            anomalies = new ArrayList<>();
        }
        anomalies.add(anomaly);

        totalAnomalies = (totalAnomalies != null ? totalAnomalies : 0L) + 1;

        recordEvent("ANOMALY_DETECTED", "Anomaly detected: " + anomaly.getAnomalyType(),
                "ANOMALY", anomaly.getAnomalyId());
    }

    public void addRecommendation(Recommendation recommendation) {
        if (recommendations == null) {
            recommendations = new ArrayList<>();
        }
        recommendations.add(recommendation);

        totalRecommendations = (totalRecommendations != null ? totalRecommendations : 0L) + 1;

        recordEvent("RECOMMENDATION_GENERATED", "Recommendation generated: " + recommendation.getTitle(),
                "RECOMMENDATION", recommendation.getRecommendationId());
    }

    public void recordEvent(String eventType, String description, String resourceType, String resourceId) {
        if (events == null) {
            events = new ArrayList<>();
        }

        AIEvent event = AIEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType(eventType)
                .description(description)
                .timestamp(LocalDateTime.now())
                .resourceType(resourceType)
                .resourceId(resourceId)
                .eventData(new HashMap<>())
                .build();

        events.add(event);
        lastEventAt = LocalDateTime.now();
    }

    public void startSystem() {
        status = SystemStatus.ACTIVE;
        startedAt = LocalDateTime.now();
        isActive = true;
        recordEvent("SYSTEM_STARTED", "AI system started", "SYSTEM", aiSystemId.toString());
    }

    public void stopSystem() {
        status = SystemStatus.OFFLINE;
        isActive = false;
        recordEvent("SYSTEM_STOPPED", "AI system stopped", "SYSTEM", aiSystemId.toString());
    }

    public MLModel getModel(String modelId) {
        return modelRegistry != null ? modelRegistry.get(modelId) : null;
    }

    public List<MLModel> getDeployedModels() {
        if (models == null) {
            return new ArrayList<>();
        }
        return models.stream()
                .filter(m -> Boolean.TRUE.equals(m.getDeployed()))
                .toList();
    }

    public List<Prediction> getSuccessfulPredictions() {
        if (predictions == null) {
            return new ArrayList<>();
        }
        return predictions.stream()
                .filter(p -> p.getStatus() == PredictionStatus.COMPLETED)
                .toList();
    }

    public boolean isHealthy() {
        return status == SystemStatus.ACTIVE && Boolean.TRUE.equals(isActive);
    }

    public boolean requiresAttention() {
        return status == SystemStatus.DEGRADED || status == SystemStatus.ERROR ||
               (failedPredictions != null && failedPredictions > 0);
    }
}
