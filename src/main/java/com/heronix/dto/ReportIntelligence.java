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
 * Report Intelligence DTO
 *
 * Represents intelligent insights, anomaly detection, and predictive analytics for reports.
 *
 * Features:
 * - Intelligent insights generation
 * - Anomaly detection
 * - Pattern recognition
 * - Trend analysis
 * - Predictive analytics
 * - Recommendation engine
 * - Root cause analysis
 * - Impact assessment
 *
 * Intelligence Types:
 * - DESCRIPTIVE - What happened
 * - DIAGNOSTIC - Why it happened
 * - PREDICTIVE - What will happen
 * - PRESCRIPTIVE - What should be done
 * - COGNITIVE - AI-driven insights
 *
 * Insight Confidence:
 * - VERY_LOW - 0-20%
 * - LOW - 20-40%
 * - MEDIUM - 40-60%
 * - HIGH - 60-80%
 * - VERY_HIGH - 80-100%
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 89 - Report Intelligence & Insights
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportIntelligence {

    private Long intelligenceId;
    private Long reportId;
    private String reportName;
    private String reportType;

    // Intelligence configuration
    private Boolean intelligenceEnabled;
    private IntelligenceType intelligenceType;
    private Boolean autoInsightsEnabled;
    private Boolean anomalyDetectionEnabled;
    private Boolean patternRecognitionEnabled;
    private Boolean predictiveAnalyticsEnabled;

    // Insights
    private List<Insight> insights;
    private Integer totalInsights;
    private Integer criticalInsights;
    private Integer actionableInsights;
    private LocalDateTime lastInsightGeneratedAt;

    // Anomalies
    private List<Anomaly> anomalies;
    private Integer totalAnomalies;
    private Integer criticalAnomalies;
    private AnomalySeverity highestSeverity;
    private LocalDateTime lastAnomalyDetectedAt;

    // Patterns
    private List<Pattern> patterns;
    private Integer totalPatterns;
    private Integer significantPatterns;
    private LocalDateTime lastPatternDetectedAt;

    // Trends
    private List<Trend> trends;
    private TrendDirection overallTrend;
    private Double trendStrength;
    private LocalDateTime trendCalculatedAt;

    // Predictions
    private List<Prediction> predictions;
    private Integer totalPredictions;
    private Double averageConfidence;
    private LocalDateTime lastPredictionAt;

    // Recommendations
    private List<Recommendation> recommendations;
    private Integer totalRecommendations;
    private Integer implementedRecommendations;
    private LocalDateTime lastRecommendationAt;

    // Root cause analysis
    private List<RootCause> rootCauses;
    private Integer totalRootCauses;
    private LocalDateTime lastRootCauseAnalysisAt;

    // Impact assessment
    private List<Impact> impacts;
    private ImpactLevel overallImpactLevel;
    private LocalDateTime lastImpactAssessmentAt;

    // Intelligence settings
    private Double anomalyThreshold;
    private Double confidenceThreshold;
    private Integer lookbackDays;
    private Integer forecastDays;
    private Boolean realTimeAnalysis;

    // Metadata
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime lastAnalyzedAt;
    private String lastAnalyzedBy;

    /**
     * Intelligence Type
     */
    public enum IntelligenceType {
        DESCRIPTIVE,   // What happened
        DIAGNOSTIC,    // Why it happened
        PREDICTIVE,    // What will happen
        PRESCRIPTIVE,  // What should be done
        COGNITIVE      // AI-driven insights
    }

    /**
     * Insight Confidence
     */
    public enum InsightConfidence {
        VERY_LOW,   // 0-20%
        LOW,        // 20-40%
        MEDIUM,     // 40-60%
        HIGH,       // 60-80%
        VERY_HIGH   // 80-100%
    }

    /**
     * Anomaly Severity
     */
    public enum AnomalySeverity {
        INFO,
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    /**
     * Trend Direction
     */
    public enum TrendDirection {
        STRONGLY_DECREASING,
        DECREASING,
        STABLE,
        INCREASING,
        STRONGLY_INCREASING,
        VOLATILE,
        UNKNOWN
    }

    /**
     * Impact Level
     */
    public enum ImpactLevel {
        NEGLIGIBLE,
        MINOR,
        MODERATE,
        MAJOR,
        SEVERE
    }

    /**
     * Recommendation Priority
     */
    public enum RecommendationPriority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT,
        CRITICAL
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
        private String title;
        private String description;
        private IntelligenceType type;
        private InsightConfidence confidence;
        private Double confidenceScore;
        private String category;
        private Boolean actionable;
        private Boolean critical;
        private LocalDateTime discoveredAt;
        private String discoveredBy;
        private List<String> relatedMetrics;
        private Map<String, Object> evidence;
        private String recommendation;
        private Boolean acknowledged;
        private LocalDateTime acknowledgedAt;
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
        private String metricName;
        private String description;
        private AnomalySeverity severity;
        private Double expectedValue;
        private Double actualValue;
        private Double deviation;
        private Double deviationPercentage;
        private LocalDateTime detectedAt;
        private LocalDateTime occurredAt;
        private String detectionMethod;
        private Boolean resolved;
        private LocalDateTime resolvedAt;
        private String resolution;
        private Map<String, Object> context;
    }

    /**
     * Pattern
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pattern {
        private String patternId;
        private String patternName;
        private String patternType;
        private String description;
        private Double confidence;
        private Integer occurrences;
        private LocalDateTime firstOccurrence;
        private LocalDateTime lastOccurrence;
        private List<String> affectedMetrics;
        private Boolean recurring;
        private String frequency;
        private Boolean significant;
        private Map<String, Object> characteristics;
    }

    /**
     * Trend
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Trend {
        private String trendId;
        private String metricName;
        private TrendDirection direction;
        private Double strength;
        private Double slope;
        private Double rSquared;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Integer dataPoints;
        private String description;
        private Boolean statisticallySignificantTrend;
        private Double pValue;
        private List<Double> values;
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
        private String metricName;
        private String description;
        private Double predictedValue;
        private Double lowerBound;
        private Double upperBound;
        private Double confidence;
        private LocalDateTime predictionDate;
        private LocalDateTime targetDate;
        private String predictionMethod;
        private Map<String, Object> modelParameters;
        private Boolean validated;
        private Double actualValue;
        private Double accuracy;
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
        private String title;
        private String description;
        private RecommendationPriority priority;
        private String category;
        private String rationale;
        private List<String> actionItems;
        private Double expectedImpact;
        private String impactDescription;
        private LocalDateTime recommendedAt;
        private String recommendedBy;
        private Boolean implemented;
        private LocalDateTime implementedAt;
        private String implementedBy;
        private String outcome;
    }

    /**
     * Root Cause
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RootCause {
        private String rootCauseId;
        private String issueDescription;
        private String rootCause;
        private Double confidence;
        private String category;
        private List<String> contributingFactors;
        private String analysisMethod;
        private LocalDateTime analyzedAt;
        private String analyzedBy;
        private List<String> recommendations;
        private Map<String, Object> supportingData;
    }

    /**
     * Impact
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Impact {
        private String impactId;
        private String description;
        private ImpactLevel level;
        private String affectedArea;
        private Integer affectedUsers;
        private Double estimatedCost;
        private String timeframe;
        private LocalDateTime assessedAt;
        private String assessedBy;
        private List<String> mitigationStrategies;
        private Map<String, Object> impactMetrics;
    }

    /**
     * Helper Methods
     */

    public void addInsight(Insight insight) {
        if (insights == null) {
            insights = new ArrayList<>();
        }
        insights.add(insight);
        totalInsights = (totalInsights != null ? totalInsights : 0) + 1;

        if (Boolean.TRUE.equals(insight.getCritical())) {
            criticalInsights = (criticalInsights != null ? criticalInsights : 0) + 1;
        }

        if (Boolean.TRUE.equals(insight.getActionable())) {
            actionableInsights = (actionableInsights != null ? actionableInsights : 0) + 1;
        }

        lastInsightGeneratedAt = LocalDateTime.now();
    }

    public void addAnomaly(Anomaly anomaly) {
        if (anomalies == null) {
            anomalies = new ArrayList<>();
        }
        anomalies.add(anomaly);
        totalAnomalies = (totalAnomalies != null ? totalAnomalies : 0) + 1;

        if (anomaly.getSeverity() == AnomalySeverity.CRITICAL) {
            criticalAnomalies = (criticalAnomalies != null ? criticalAnomalies : 0) + 1;
        }

        if (highestSeverity == null || anomaly.getSeverity().ordinal() > highestSeverity.ordinal()) {
            highestSeverity = anomaly.getSeverity();
        }

        lastAnomalyDetectedAt = LocalDateTime.now();
    }

    public void addPattern(Pattern pattern) {
        if (patterns == null) {
            patterns = new ArrayList<>();
        }
        patterns.add(pattern);
        totalPatterns = (totalPatterns != null ? totalPatterns : 0) + 1;

        if (Boolean.TRUE.equals(pattern.getSignificant())) {
            significantPatterns = (significantPatterns != null ? significantPatterns : 0) + 1;
        }

        lastPatternDetectedAt = LocalDateTime.now();
    }

    public void addTrend(Trend trend) {
        if (trends == null) {
            trends = new ArrayList<>();
        }
        trends.add(trend);

        // Update overall trend based on weighted average
        updateOverallTrend();

        trendCalculatedAt = LocalDateTime.now();
    }

    private void updateOverallTrend() {
        if (trends == null || trends.isEmpty()) {
            overallTrend = TrendDirection.UNKNOWN;
            trendStrength = 0.0;
            return;
        }

        double avgSlope = trends.stream()
                .mapToDouble(t -> t.getSlope() != null ? t.getSlope() : 0.0)
                .average()
                .orElse(0.0);

        trendStrength = Math.abs(avgSlope);

        if (avgSlope > 0.5) {
            overallTrend = TrendDirection.STRONGLY_INCREASING;
        } else if (avgSlope > 0.1) {
            overallTrend = TrendDirection.INCREASING;
        } else if (avgSlope < -0.5) {
            overallTrend = TrendDirection.STRONGLY_DECREASING;
        } else if (avgSlope < -0.1) {
            overallTrend = TrendDirection.DECREASING;
        } else {
            overallTrend = TrendDirection.STABLE;
        }
    }

    public void addPrediction(Prediction prediction) {
        if (predictions == null) {
            predictions = new ArrayList<>();
        }
        predictions.add(prediction);
        totalPredictions = (totalPredictions != null ? totalPredictions : 0) + 1;

        // Update average confidence
        averageConfidence = predictions.stream()
                .mapToDouble(p -> p.getConfidence() != null ? p.getConfidence() : 0.0)
                .average()
                .orElse(0.0);

        lastPredictionAt = LocalDateTime.now();
    }

    public void addRecommendation(Recommendation recommendation) {
        if (recommendations == null) {
            recommendations = new ArrayList<>();
        }
        recommendations.add(recommendation);
        totalRecommendations = (totalRecommendations != null ? totalRecommendations : 0) + 1;

        if (Boolean.TRUE.equals(recommendation.getImplemented())) {
            implementedRecommendations = (implementedRecommendations != null ? implementedRecommendations : 0) + 1;
        }

        lastRecommendationAt = LocalDateTime.now();
    }

    public void addRootCause(RootCause rootCause) {
        if (rootCauses == null) {
            rootCauses = new ArrayList<>();
        }
        rootCauses.add(rootCause);
        totalRootCauses = (totalRootCauses != null ? totalRootCauses : 0) + 1;

        lastRootCauseAnalysisAt = LocalDateTime.now();
    }

    public void addImpact(Impact impact) {
        if (impacts == null) {
            impacts = new ArrayList<>();
        }
        impacts.add(impact);

        // Update overall impact level
        if (overallImpactLevel == null || impact.getLevel().ordinal() > overallImpactLevel.ordinal()) {
            overallImpactLevel = impact.getLevel();
        }

        lastImpactAssessmentAt = LocalDateTime.now();
    }

    public void acknowledgeInsight(String insightId) {
        if (insights != null) {
            for (Insight insight : insights) {
                if (insight.getInsightId().equals(insightId)) {
                    insight.setAcknowledged(true);
                    insight.setAcknowledgedAt(LocalDateTime.now());
                    break;
                }
            }
        }
    }

    public void resolveAnomaly(String anomalyId, String resolution) {
        if (anomalies != null) {
            for (Anomaly anomaly : anomalies) {
                if (anomaly.getAnomalyId().equals(anomalyId)) {
                    anomaly.setResolved(true);
                    anomaly.setResolvedAt(LocalDateTime.now());
                    anomaly.setResolution(resolution);
                    break;
                }
            }
        }
    }

    public void implementRecommendation(String recommendationId, String implementedBy, String outcome) {
        if (recommendations != null) {
            for (Recommendation rec : recommendations) {
                if (rec.getRecommendationId().equals(recommendationId)) {
                    rec.setImplemented(true);
                    rec.setImplementedAt(LocalDateTime.now());
                    rec.setImplementedBy(implementedBy);
                    rec.setOutcome(outcome);
                    implementedRecommendations = (implementedRecommendations != null ? implementedRecommendations : 0) + 1;
                    break;
                }
            }
        }
    }

    public void validatePrediction(String predictionId, Double actualValue) {
        if (predictions != null) {
            for (Prediction pred : predictions) {
                if (pred.getPredictionId().equals(predictionId)) {
                    pred.setValidated(true);
                    pred.setActualValue(actualValue);

                    // Calculate accuracy
                    if (pred.getPredictedValue() != null && actualValue != null) {
                        double error = Math.abs(pred.getPredictedValue() - actualValue);
                        double accuracy = 100.0 - (error / Math.abs(actualValue) * 100.0);
                        pred.setAccuracy(Math.max(0.0, Math.min(100.0, accuracy)));
                    }
                    break;
                }
            }
        }
    }

    public List<Insight> getCriticalInsights() {
        if (insights == null) {
            return new ArrayList<>();
        }
        return insights.stream()
                .filter(i -> Boolean.TRUE.equals(i.getCritical()))
                .toList();
    }

    public List<Insight> getActionableInsights() {
        if (insights == null) {
            return new ArrayList<>();
        }
        return insights.stream()
                .filter(i -> Boolean.TRUE.equals(i.getActionable()) && !Boolean.TRUE.equals(i.getAcknowledged()))
                .toList();
    }

    public List<Anomaly> getUnresolvedAnomalies() {
        if (anomalies == null) {
            return new ArrayList<>();
        }
        return anomalies.stream()
                .filter(a -> !Boolean.TRUE.equals(a.getResolved()))
                .toList();
    }

    public List<Recommendation> getPendingRecommendations() {
        if (recommendations == null) {
            return new ArrayList<>();
        }
        return recommendations.stream()
                .filter(r -> !Boolean.TRUE.equals(r.getImplemented()))
                .toList();
    }

    public boolean hasHighSeverityAnomalies() {
        return anomalies != null && anomalies.stream()
                .anyMatch(a -> a.getSeverity() == AnomalySeverity.HIGH || a.getSeverity() == AnomalySeverity.CRITICAL);
    }

    public boolean hasCriticalInsights() {
        return criticalInsights != null && criticalInsights > 0;
    }
}
