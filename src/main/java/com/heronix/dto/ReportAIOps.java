package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report AIOps DTO
 *
 * Represents AI-powered operations, intelligent automation,
 * predictive analytics, anomaly detection, and self-healing systems.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 141 - Intelligent Automation & AI Operations (AIOps)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportAIOps {

    private Long aiopsId;
    private String aiopsName;
    private String description;

    // AI/ML Configuration
    private String mlFramework; // TENSORFLOW, PYTORCH, SCIKIT_LEARN, KERAS, XGBOOST
    private String modelType; // SUPERVISED, UNSUPERVISED, REINFORCEMENT, DEEP_LEARNING
    private List<String> deployedModels;
    private String modelVersion;
    private Map<String, Object> mlConfig;

    // Anomaly Detection
    private Boolean anomalyDetectionEnabled;
    private String anomalyDetectionAlgorithm; // ISOLATION_FOREST, AUTOENCODER, LSTM, VAE
    private Double anomalyThreshold;
    private Integer detectionWindowMinutes;
    private List<Map<String, Object>> detectedAnomalies;
    private Map<String, Object> anomalyConfig;

    // Predictive Analytics
    private Boolean predictiveAnalyticsEnabled;
    private String predictionType; // FAILURE, CAPACITY, PERFORMANCE, SECURITY
    private Integer predictionHorizonHours;
    private Double predictionConfidence;
    private List<Map<String, Object>> predictions;
    private Map<String, Object> predictionConfig;

    // Root Cause Analysis
    private Boolean rcaEnabled;
    private String rcaMethod; // GRAPH_BASED, CORRELATION, ML_BASED, CAUSAL_INFERENCE
    private List<Map<String, Object>> rootCauses;
    private Double rcaAccuracy;
    private Map<String, Object> rcaConfig;

    // Pattern Recognition
    private Boolean patternRecognitionEnabled;
    private List<String> recognizedPatterns;
    private Map<String, Integer> patternFrequency;
    private LocalDateTime lastPatternUpdate;
    private Map<String, Object> patternConfig;

    // Self-Healing
    private Boolean selfHealingEnabled;
    private List<String> healingActions; // RESTART, SCALE, ROLLBACK, MIGRATE, PATCH
    private Integer totalHealingAttempts;
    private Integer successfulHeals;
    private Integer failedHeals;
    private Map<String, Object> healingConfig;

    // Automated Remediation
    private Boolean autoRemediationEnabled;
    private List<Map<String, Object>> remediationPlaybooks;
    private Integer executedRemediations;
    private Double remediationSuccessRate;
    private Map<String, Object> remediationConfig;

    // Intelligent Alerting
    private Boolean intelligentAlertingEnabled;
    private String alertPrioritization; // ML_BASED, RULE_BASED, HYBRID
    private Boolean alertDeduplication;
    private Boolean alertCorrelation;
    private Integer suppressedAlerts;
    private Map<String, Object> alertingConfig;

    // Capacity Planning
    private Boolean capacityPlanningEnabled;
    private String capacityPredictionMethod;
    private Integer forecastDays;
    private Map<String, Object> capacityForecasts;
    private List<String> capacityRecommendations;
    private Map<String, Object> capacityConfig;

    // Performance Optimization
    private Boolean performanceOptimizationEnabled;
    private List<String> optimizationStrategies;
    private Map<String, Object> performanceBaseline;
    private Map<String, Object> optimizationRecommendations;
    private Integer appliedOptimizations;

    // Incident Management
    private Boolean autoIncidentCreation;
    private Boolean incidentPrioritization;
    private String incidentClassification; // SEVERITY, IMPACT, URGENCY
    private List<Map<String, Object>> incidents;
    private Double meanTimeToDetect;
    private Double meanTimeToResolve;

    // Time Series Analysis
    private Boolean timeSeriesAnalysisEnabled;
    private String timeSeriesAlgorithm; // ARIMA, PROPHET, LSTM, SARIMA
    private Integer historicalDataDays;
    private Map<String, Object> timeSeriesMetrics;
    private Map<String, Object> seasonalityPatterns;

    // Metric Collection
    private List<String> monitoredMetrics;
    private Integer metricsCollectionIntervalSeconds;
    private Long totalMetricsCollected;
    private Map<String, List<Double>> metricHistory;
    private Map<String, Object> metricConfig;

    // Log Analysis
    private Boolean logAnalysisEnabled;
    private String logAnalysisMethod; // NLP, REGEX, ML_CLASSIFICATION
    private Long totalLogsAnalyzed;
    private Integer logAnomaliesDetected;
    private Map<String, Object> logPatterns;
    private Map<String, Object> logConfig;

    // Correlation Engine
    private Boolean correlationEnabled;
    private String correlationMethod; // PEARSON, SPEARMAN, CAUSAL, GRANGER
    private Map<String, Map<String, Double>> correlationMatrix;
    private List<String> correlatedEvents;
    private Map<String, Object> correlationConfig;

    // Topology Awareness
    private Boolean topologyAwarenessEnabled;
    private Map<String, Object> systemTopology;
    private List<String> dependencies;
    private Map<String, Object> impactAnalysis;
    private Map<String, Object> topologyConfig;

    // Business Impact Analysis
    private Boolean businessImpactEnabled;
    private Map<String, String> serviceMapping; // technical service -> business service
    private Map<String, Double> impactScores;
    private List<String> affectedBusinessServices;
    private Map<String, Object> businessConfig;

    // Knowledge Base
    private Boolean knowledgeBaseEnabled;
    private List<Map<String, Object>> knowledgeArticles;
    private Integer totalArticles;
    private Boolean autoKbUpdate;
    private Map<String, Object> kbConfig;

    // Chatbot/Virtual Assistant
    private Boolean virtualAssistantEnabled;
    private String assistantType; // RULE_BASED, ML_BASED, NLP_POWERED
    private Integer totalInteractions;
    private Double assistantAccuracy;
    private Map<String, Object> assistantConfig;

    // Continuous Learning
    private Boolean continuousLearningEnabled;
    private LocalDateTime lastModelTraining;
    private Integer trainingIterations;
    private Double modelAccuracy;
    private Map<String, Object> learningConfig;

    // Data Pipeline
    private String dataIngestionMethod; // STREAMING, BATCH, HYBRID
    private Long dataPointsIngested;
    private String dataStorageType; // TIME_SERIES_DB, DATA_LAKE, WAREHOUSE
    private Map<String, Object> pipelineConfig;

    // Feature Engineering
    private List<String> engineeredFeatures;
    private Boolean autoFeatureEngineering;
    private Map<String, Object> featureImportance;
    private Map<String, Object> featureConfig;

    // Model Performance
    private Double precision;
    private Double recall;
    private Double f1Score;
    private Double auc;
    private Map<String, Double> confusionMatrix;
    private Map<String, Object> performanceMetrics;

    // Automation Statistics
    private Long totalAutomations;
    private Long successfulAutomations;
    private Long failedAutomations;
    private Double automationSuccessRate;
    private Integer averageAutomationTimeSeconds;

    // Cost Optimization
    private Boolean costOptimizationEnabled;
    private Double estimatedMonthlySavings;
    private List<String> costOptimizationActions;
    private Map<String, Object> costAnalysis;

    // Integration
    private List<String> integratedTools; // PROMETHEUS, GRAFANA, SPLUNK, DATADOG, ELK
    private Boolean metricsIntegration;
    private Boolean logsIntegration;
    private Boolean tracesIntegration;
    private Map<String, Object> integrationConfig;

    // Explainability
    private Boolean explainableAiEnabled;
    private List<String> explanationMethods; // LIME, SHAP, ATTENTION, COUNTERFACTUAL
    private Map<String, Object> modelExplanations;
    private Map<String, Object> explainabilityConfig;

    // Status
    private String aiopsStatus; // TRAINING, ACTIVE, LEARNING, DEGRADED, OFFLINE
    private LocalDateTime activatedAt;
    private LocalDateTime lastPrediction;
    private LocalDateTime lastAnomaly;
    private LocalDateTime lastHealing;

    // Metadata
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Object> metadata;

    // Helper Methods
    public void addDetectedAnomaly(Map<String, Object> anomaly) {
        if (this.detectedAnomalies != null) {
            this.detectedAnomalies.add(anomaly);
        }
    }

    public void addPrediction(Map<String, Object> prediction) {
        if (this.predictions != null) {
            this.predictions.add(prediction);
        }
    }

    public void addRootCause(Map<String, Object> rootCause) {
        if (this.rootCauses != null) {
            this.rootCauses.add(rootCause);
        }
    }

    public void addIncident(Map<String, Object> incident) {
        if (this.incidents != null) {
            this.incidents.add(incident);
        }
    }

    public void incrementHealing(boolean successful) {
        this.totalHealingAttempts = (this.totalHealingAttempts != null ? this.totalHealingAttempts : 0) + 1;

        if (successful) {
            this.successfulHeals = (this.successfulHeals != null ? this.successfulHeals : 0) + 1;
        } else {
            this.failedHeals = (this.failedHeals != null ? this.failedHeals : 0) + 1;
        }
    }

    public void incrementAutomation(boolean successful) {
        this.totalAutomations = (this.totalAutomations != null ? this.totalAutomations : 0L) + 1;

        if (successful) {
            this.successfulAutomations = (this.successfulAutomations != null ? this.successfulAutomations : 0L) + 1;
        } else {
            this.failedAutomations = (this.failedAutomations != null ? this.failedAutomations : 0L) + 1;
        }

        updateAutomationSuccessRate();
    }

    public void incrementRemediation() {
        this.executedRemediations = (this.executedRemediations != null ? this.executedRemediations : 0) + 1;
    }

    private void updateAutomationSuccessRate() {
        if (this.totalAutomations != null && this.totalAutomations > 0) {
            Long successful = this.successfulAutomations != null ? this.successfulAutomations : 0L;
            this.automationSuccessRate = (successful * 100.0) / this.totalAutomations;
        } else {
            this.automationSuccessRate = 0.0;
        }
    }

    public boolean isActive() {
        return "ACTIVE".equals(aiopsStatus);
    }

    public boolean isLearning() {
        return "LEARNING".equals(aiopsStatus) || Boolean.TRUE.equals(continuousLearningEnabled);
    }

    public double getHealingSuccessRate() {
        if (totalHealingAttempts == null || totalHealingAttempts == 0) {
            return 0.0;
        }
        Integer successful = successfulHeals != null ? successfulHeals : 0;
        return (successful * 100.0) / totalHealingAttempts;
    }

    public boolean hasHighAccuracy() {
        return modelAccuracy != null && modelAccuracy >= 0.9;
    }

    public boolean needsRetraining() {
        if (lastModelTraining == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(lastModelTraining.plusDays(30));
    }

    public boolean isHighPerforming() {
        return f1Score != null && f1Score >= 0.85;
    }
}
