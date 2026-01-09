package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Machine Learning DTO
 *
 * Represents machine learning and AI configuration for reports.
 *
 * Features:
 * - Supervised learning (classification, regression)
 * - Unsupervised learning (clustering, anomaly detection)
 * - Time series forecasting
 * - Natural language processing
 * - Recommendation systems
 * - AutoML (automated machine learning)
 * - Model training, evaluation, and deployment
 * - Feature engineering and selection
 *
 * ML Workflow:
 * - Data preparation and feature engineering
 * - Model training and hyperparameter tuning
 * - Model evaluation and validation
 * - Model deployment and serving
 * - Model monitoring and retraining
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 84 - Report Machine Learning & AI
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportML {

    /**
     * ML task type enumeration
     */
    public enum MLTaskType {
        CLASSIFICATION,         // Binary/multi-class classification
        REGRESSION,             // Numerical prediction
        CLUSTERING,             // Grouping similar data
        ANOMALY_DETECTION,      // Outlier detection
        TIME_SERIES_FORECAST,   // Time series prediction
        RECOMMENDATION,         // Recommendation system
        NLP,                    // Natural language processing
        DIMENSIONALITY_REDUCTION, // PCA, t-SNE, etc.
        ASSOCIATION_RULES,      // Market basket analysis
        REINFORCEMENT_LEARNING  // RL-based optimization
    }

    /**
     * Algorithm type enumeration
     */
    public enum AlgorithmType {
        // Classification/Regression
        LINEAR_REGRESSION,
        LOGISTIC_REGRESSION,
        DECISION_TREE,
        RANDOM_FOREST,
        GRADIENT_BOOSTING,
        XGBOOST,
        SVM,
        NAIVE_BAYES,
        KNN,
        NEURAL_NETWORK,

        // Clustering
        KMEANS,
        DBSCAN,
        HIERARCHICAL,
        GAUSSIAN_MIXTURE,

        // Time Series
        ARIMA,
        SARIMA,
        PROPHET,
        LSTM,

        // Dimensionality Reduction
        PCA,
        LDA,
        TSNE,

        // Other
        ISOLATION_FOREST,
        AUTOENCODER,
        CUSTOM
    }

    /**
     * Model status enumeration
     */
    public enum ModelStatus {
        DRAFT,              // Draft/not trained
        TRAINING,           // Currently training
        TRAINED,            // Training completed
        EVALUATING,         // Evaluating performance
        VALIDATED,          // Validation completed
        DEPLOYED,           // Deployed to production
        FAILED,             // Training/deployment failed
        DEPRECATED,         // Deprecated/retired
        ARCHIVED            // Archived
    }

    /**
     * Feature type enumeration
     */
    public enum FeatureType {
        NUMERICAL,          // Numerical feature
        CATEGORICAL,        // Categorical feature
        BINARY,             // Binary feature
        ORDINAL,            // Ordinal feature
        TEXT,               // Text feature
        DATETIME,           // Date/time feature
        DERIVED,            // Derived/engineered feature
        EMBEDDED            // Embedding feature
    }

    /**
     * Training mode enumeration
     */
    public enum TrainingMode {
        BATCH,              // Batch training
        ONLINE,             // Online/incremental training
        MINI_BATCH,         // Mini-batch training
        TRANSFER_LEARNING,  // Transfer learning
        AUTO_ML             // Automated ML
    }

    // ============================================================
    // Basic Information
    // ============================================================

    /**
     * ML model ID
     */
    private Long modelId;

    /**
     * Model name
     */
    private String name;

    /**
     * Description
     */
    private String description;

    /**
     * ML task type
     */
    private MLTaskType taskType;

    /**
     * Algorithm type
     */
    private AlgorithmType algorithmType;

    /**
     * Model status
     */
    private ModelStatus status;

    /**
     * Version
     */
    private String version;

    /**
     * Report ID
     */
    private Long reportId;

    /**
     * Report name
     */
    private String reportName;

    /**
     * Created at
     */
    private LocalDateTime createdAt;

    /**
     * Created by
     */
    private String createdBy;

    /**
     * Updated at
     */
    private LocalDateTime updatedAt;

    // ============================================================
    // Data Configuration
    // ============================================================

    /**
     * Dataset name
     */
    private String datasetName;

    /**
     * Training data size
     */
    private Long trainingDataSize;

    /**
     * Validation data size
     */
    private Long validationDataSize;

    /**
     * Test data size
     */
    private Long testDataSize;

    /**
     * Train/validation/test split ratio (e.g., "70/15/15")
     */
    private String splitRatio;

    /**
     * Target variable
     */
    private String targetVariable;

    /**
     * Features
     */
    private List<Feature> features;

    /**
     * Feature count
     */
    private Integer featureCount;

    /**
     * Class labels (for classification)
     */
    private List<String> classLabels;

    /**
     * Class distribution
     */
    private Map<String, Long> classDistribution;

    // ============================================================
    // Feature Engineering
    // ============================================================

    /**
     * Feature engineering enabled
     */
    private Boolean featureEngineeringEnabled;

    /**
     * Feature transformations
     */
    private List<FeatureTransformation> featureTransformations;

    /**
     * Feature selection enabled
     */
    private Boolean featureSelectionEnabled;

    /**
     * Feature selection method
     */
    private String featureSelectionMethod;

    /**
     * Selected features
     */
    private List<String> selectedFeatures;

    /**
     * Feature importance scores
     */
    private Map<String, Double> featureImportance;

    // ============================================================
    // Training Configuration
    // ============================================================

    /**
     * Training mode
     */
    private TrainingMode trainingMode;

    /**
     * Hyperparameters
     */
    private Map<String, Object> hyperparameters;

    /**
     * Auto-tune hyperparameters
     */
    private Boolean autoTuneHyperparameters;

    /**
     * Tuning method (GRID_SEARCH, RANDOM_SEARCH, BAYESIAN)
     */
    private String tuningMethod;

    /**
     * Max epochs/iterations
     */
    private Integer maxEpochs;

    /**
     * Batch size
     */
    private Integer batchSize;

    /**
     * Learning rate
     */
    private Double learningRate;

    /**
     * Early stopping enabled
     */
    private Boolean earlyStoppingEnabled;

    /**
     * Early stopping patience
     */
    private Integer earlyStoppingPatience;

    /**
     * Random seed
     */
    private Long randomSeed;

    // ============================================================
    // Training Execution
    // ============================================================

    /**
     * Training started at
     */
    private LocalDateTime trainingStartedAt;

    /**
     * Training completed at
     */
    private LocalDateTime trainingCompletedAt;

    /**
     * Training duration (ms)
     */
    private Long trainingDurationMs;

    /**
     * Current epoch
     */
    private Integer currentEpoch;

    /**
     * Training loss
     */
    private Double trainingLoss;

    /**
     * Validation loss
     */
    private Double validationLoss;

    /**
     * Training history
     */
    private List<TrainingHistory> trainingHistory;

    /**
     * Convergence achieved
     */
    private Boolean convergenceAchieved;

    // ============================================================
    // Model Evaluation
    // ============================================================

    /**
     * Evaluation metrics
     */
    private EvaluationMetrics evaluationMetrics;

    /**
     * Confusion matrix (for classification)
     */
    private int[][] confusionMatrix;

    /**
     * ROC AUC score (for classification)
     */
    private Double rocAucScore;

    /**
     * Precision
     */
    private Double precision;

    /**
     * Recall
     */
    private Double recall;

    /**
     * F1 score
     */
    private Double f1Score;

    /**
     * R-squared (for regression)
     */
    private Double rSquared;

    /**
     * Mean absolute error (MAE)
     */
    private Double mae;

    /**
     * Mean squared error (MSE)
     */
    private Double mse;

    /**
     * Root mean squared error (RMSE)
     */
    private Double rmse;

    /**
     * Cross-validation score
     */
    private Double cvScore;

    /**
     * Cross-validation folds
     */
    private Integer cvFolds;

    // ============================================================
    // Predictions
    // ============================================================

    /**
     * Predictions enabled
     */
    private Boolean predictionsEnabled;

    /**
     * Total predictions made
     */
    private Long totalPredictions;

    /**
     * Prediction accuracy (recent)
     */
    private Double predictionAccuracy;

    /**
     * Recent predictions
     */
    private List<Prediction> recentPredictions;

    /**
     * Prediction latency (ms)
     */
    private Long predictionLatencyMs;

    // ============================================================
    // Model Deployment
    // ============================================================

    /**
     * Deployed at
     */
    private LocalDateTime deployedAt;

    /**
     * Deployed by
     */
    private String deployedBy;

    /**
     * Deployment environment
     */
    private String deploymentEnvironment;

    /**
     * Endpoint URL
     */
    private String endpointUrl;

    /**
     * Model serving enabled
     */
    private Boolean modelServingEnabled;

    /**
     * Requests per second
     */
    private Double requestsPerSecond;

    /**
     * Model size (MB)
     */
    private Double modelSizeMB;

    // ============================================================
    // Model Monitoring
    // ============================================================

    /**
     * Model drift detected
     */
    private Boolean modelDriftDetected;

    /**
     * Data drift detected
     */
    private Boolean dataDriftDetected;

    /**
     * Performance degradation
     */
    private Boolean performanceDegradation;

    /**
     * Last monitoring check
     */
    private LocalDateTime lastMonitoringCheck;

    /**
     * Retrain recommended
     */
    private Boolean retrainRecommended;

    /**
     * Last retrained at
     */
    private LocalDateTime lastRetrainedAt;

    /**
     * Retrain frequency (days)
     */
    private Integer retrainFrequencyDays;

    // ============================================================
    // Explainability
    // ============================================================

    /**
     * Explainability enabled
     */
    private Boolean explainabilityEnabled;

    /**
     * Explanation method (SHAP, LIME, etc.)
     */
    private String explanationMethod;

    /**
     * Global explanations
     */
    private Map<String, Object> globalExplanations;

    /**
     * Model interpretability score
     */
    private Double interpretabilityScore;

    // ============================================================
    // Performance Metrics
    // ============================================================

    /**
     * Training time per epoch (ms)
     */
    private Double trainingTimePerEpochMs;

    /**
     * Inference time (ms)
     */
    private Double inferenceTimeMs;

    /**
     * Memory usage (MB)
     */
    private Long memoryUsageMB;

    /**
     * CPU utilization (percentage)
     */
    private Double cpuUtilization;

    /**
     * GPU utilization (percentage)
     */
    private Double gpuUtilization;

    // ============================================================
    // Metadata
    // ============================================================

    /**
     * Tags
     */
    private List<String> tags;

    /**
     * Owner
     */
    private String owner;

    /**
     * Framework (TensorFlow, PyTorch, scikit-learn, etc.)
     */
    private String framework;

    /**
     * Framework version
     */
    private String frameworkVersion;

    /**
     * Custom properties
     */
    private Map<String, Object> customProperties;

    /**
     * Notes
     */
    private String notes;

    // ============================================================
    // Nested Classes
    // ============================================================

    /**
     * Feature
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Feature {
        private String name;
        private FeatureType type;
        private String description;
        private Boolean required;
        private Object defaultValue;
        private Double importanceScore;
        private Map<String, Object> statistics;
        private List<String> transformations;
    }

    /**
     * Feature transformation
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeatureTransformation {
        private String transformId;
        private String featureName;
        private String transformationType; // NORMALIZE, STANDARDIZE, ENCODE, BINNING, etc.
        private Map<String, Object> parameters;
        private Boolean applied;
    }

    /**
     * Training history
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrainingHistory {
        private Integer epoch;
        private Double trainingLoss;
        private Double validationLoss;
        private Double trainingMetric;
        private Double validationMetric;
        private Long durationMs;
        private LocalDateTime timestamp;
    }

    /**
     * Evaluation metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvaluationMetrics {
        private Double accuracy;
        private Double precision;
        private Double recall;
        private Double f1Score;
        private Double rocAuc;
        private Double mae;
        private Double mse;
        private Double rmse;
        private Double rSquared;
        private Map<String, Double> perClassMetrics;
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
        private Map<String, Object> inputFeatures;
        private Object predictedValue;
        private Double confidence;
        private List<PredictionProba> classProbabilities;
        private LocalDateTime timestamp;
        private Long latencyMs;
        private Map<String, Object> explanation;
    }

    /**
     * Prediction probability
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PredictionProba {
        private String className;
        private Double probability;
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Check if model is trained
     */
    public boolean isTrained() {
        return status == ModelStatus.TRAINED ||
               status == ModelStatus.VALIDATED ||
               status == ModelStatus.DEPLOYED;
    }

    /**
     * Check if model is deployed
     */
    public boolean isDeployed() {
        return status == ModelStatus.DEPLOYED;
    }

    /**
     * Check if model is training
     */
    public boolean isTraining() {
        return status == ModelStatus.TRAINING;
    }

    /**
     * Start training
     */
    public void startTraining() {
        status = ModelStatus.TRAINING;
        trainingStartedAt = LocalDateTime.now();
        currentEpoch = 0;
    }

    /**
     * Complete training
     */
    public void completeTraining(boolean success) {
        status = success ? ModelStatus.TRAINED : ModelStatus.FAILED;
        trainingCompletedAt = LocalDateTime.now();
        if (trainingStartedAt != null) {
            trainingDurationMs = java.time.Duration.between(trainingStartedAt, trainingCompletedAt).toMillis();
        }
    }

    /**
     * Deploy model
     */
    public void deploy(String deployedBy, String environment) {
        status = ModelStatus.DEPLOYED;
        deployedAt = LocalDateTime.now();
        this.deployedBy = deployedBy;
        deploymentEnvironment = environment;
    }

    /**
     * Calculate training time per epoch
     */
    public void calculateTrainingTimePerEpoch() {
        if (trainingDurationMs != null && currentEpoch != null && currentEpoch > 0) {
            trainingTimePerEpochMs = trainingDurationMs.doubleValue() / currentEpoch;
        }
    }

    /**
     * Check if performance is good
     */
    public boolean isPerformanceGood() {
        if (taskType == MLTaskType.CLASSIFICATION && f1Score != null) {
            return f1Score >= 0.8;
        } else if (taskType == MLTaskType.REGRESSION && rSquared != null) {
            return rSquared >= 0.7;
        }
        return false;
    }

    /**
     * Check if needs retraining
     */
    public boolean needsRetraining() {
        if (Boolean.TRUE.equals(retrainRecommended)) {
            return true;
        }

        if (retrainFrequencyDays != null && lastRetrainedAt != null) {
            LocalDateTime nextRetrain = lastRetrainedAt.plusDays(retrainFrequencyDays);
            return LocalDateTime.now().isAfter(nextRetrain);
        }

        return Boolean.TRUE.equals(modelDriftDetected) ||
               Boolean.TRUE.equals(dataDriftDetected) ||
               Boolean.TRUE.equals(performanceDegradation);
    }

    /**
     * Get total data size
     */
    public Long getTotalDataSize() {
        long total = 0;
        if (trainingDataSize != null) total += trainingDataSize;
        if (validationDataSize != null) total += validationDataSize;
        if (testDataSize != null) total += testDataSize;
        return total;
    }

    /**
     * Get model quality score (0-100)
     */
    public Double getModelQualityScore() {
        if (evaluationMetrics == null) {
            return null;
        }

        double score = 0.0;
        int factors = 0;

        if (evaluationMetrics.getAccuracy() != null) {
            score += evaluationMetrics.getAccuracy() * 100;
            factors++;
        }

        if (evaluationMetrics.getF1Score() != null) {
            score += evaluationMetrics.getF1Score() * 100;
            factors++;
        }

        if (evaluationMetrics.getRSquared() != null) {
            score += evaluationMetrics.getRSquared() * 100;
            factors++;
        }

        return factors > 0 ? score / factors : null;
    }
}
