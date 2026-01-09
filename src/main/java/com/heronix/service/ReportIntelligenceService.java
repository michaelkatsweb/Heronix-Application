package com.heronix.service;

import com.heronix.dto.ReportIntelligence;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Report Intelligence Service
 *
 * Manages intelligent insights, anomaly detection, and predictive analytics.
 *
 * Features:
 * - Insight generation
 * - Anomaly detection
 * - Pattern recognition
 * - Trend analysis
 * - Predictive analytics
 * - Recommendation engine
 * - Root cause analysis
 * - Impact assessment
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 89 - Report Intelligence & Insights
 */
@Service
@Slf4j
public class ReportIntelligenceService {

    private final Map<Long, ReportIntelligence> intelligences = new ConcurrentHashMap<>();
    private Long nextIntelligenceId = 1L;

    /**
     * Create intelligence
     */
    public ReportIntelligence createIntelligence(ReportIntelligence intelligence) {
        synchronized (this) {
            intelligence.setIntelligenceId(nextIntelligenceId++);
            intelligence.setCreatedAt(LocalDateTime.now());
            intelligence.setTotalInsights(0);
            intelligence.setCriticalInsights(0);
            intelligence.setActionableInsights(0);
            intelligence.setTotalAnomalies(0);
            intelligence.setCriticalAnomalies(0);
            intelligence.setTotalPatterns(0);
            intelligence.setSignificantPatterns(0);
            intelligence.setTotalPredictions(0);
            intelligence.setAverageConfidence(0.0);
            intelligence.setTotalRecommendations(0);
            intelligence.setImplementedRecommendations(0);
            intelligence.setTotalRootCauses(0);

            // Set defaults
            if (intelligence.getIntelligenceEnabled() == null) {
                intelligence.setIntelligenceEnabled(true);
            }

            if (intelligence.getIntelligenceType() == null) {
                intelligence.setIntelligenceType(ReportIntelligence.IntelligenceType.DESCRIPTIVE);
            }

            if (intelligence.getAutoInsightsEnabled() == null) {
                intelligence.setAutoInsightsEnabled(true);
            }

            if (intelligence.getAnomalyDetectionEnabled() == null) {
                intelligence.setAnomalyDetectionEnabled(true);
            }

            if (intelligence.getPatternRecognitionEnabled() == null) {
                intelligence.setPatternRecognitionEnabled(true);
            }

            if (intelligence.getPredictiveAnalyticsEnabled() == null) {
                intelligence.setPredictiveAnalyticsEnabled(true);
            }

            if (intelligence.getAnomalyThreshold() == null) {
                intelligence.setAnomalyThreshold(2.0); // 2 standard deviations
            }

            if (intelligence.getConfidenceThreshold() == null) {
                intelligence.setConfidenceThreshold(0.7); // 70%
            }

            if (intelligence.getLookbackDays() == null) {
                intelligence.setLookbackDays(90);
            }

            if (intelligence.getForecastDays() == null) {
                intelligence.setForecastDays(30);
            }

            if (intelligence.getRealTimeAnalysis() == null) {
                intelligence.setRealTimeAnalysis(false);
            }

            // Initialize collections
            if (intelligence.getInsights() == null) {
                intelligence.setInsights(new ArrayList<>());
            }

            if (intelligence.getAnomalies() == null) {
                intelligence.setAnomalies(new ArrayList<>());
            }

            if (intelligence.getPatterns() == null) {
                intelligence.setPatterns(new ArrayList<>());
            }

            if (intelligence.getTrends() == null) {
                intelligence.setTrends(new ArrayList<>());
            }

            if (intelligence.getPredictions() == null) {
                intelligence.setPredictions(new ArrayList<>());
            }

            if (intelligence.getRecommendations() == null) {
                intelligence.setRecommendations(new ArrayList<>());
            }

            if (intelligence.getRootCauses() == null) {
                intelligence.setRootCauses(new ArrayList<>());
            }

            if (intelligence.getImpacts() == null) {
                intelligence.setImpacts(new ArrayList<>());
            }

            intelligences.put(intelligence.getIntelligenceId(), intelligence);

            log.info("Created intelligence {} for report {} with type {}",
                    intelligence.getIntelligenceId(), intelligence.getReportId(),
                    intelligence.getIntelligenceType());

            return intelligence;
        }
    }

    /**
     * Get intelligence
     */
    public Optional<ReportIntelligence> getIntelligence(Long intelligenceId) {
        return Optional.ofNullable(intelligences.get(intelligenceId));
    }

    /**
     * Get intelligence by report
     */
    public Optional<ReportIntelligence> getIntelligenceByReport(Long reportId) {
        return intelligences.values().stream()
                .filter(i -> reportId.equals(i.getReportId()))
                .findFirst();
    }

    /**
     * Generate insight
     */
    public ReportIntelligence.Insight generateInsight(Long intelligenceId, String title,
                                                       String description,
                                                       ReportIntelligence.IntelligenceType type,
                                                       Double confidenceScore) {
        ReportIntelligence intelligence = intelligences.get(intelligenceId);
        if (intelligence == null) {
            throw new IllegalArgumentException("Intelligence not found: " + intelligenceId);
        }

        ReportIntelligence.InsightConfidence confidence = getConfidenceLevel(confidenceScore);

        boolean actionable = confidenceScore >= intelligence.getConfidenceThreshold();
        boolean critical = confidenceScore >= 0.9 || description.toLowerCase().contains("critical");

        ReportIntelligence.Insight insight = ReportIntelligence.Insight.builder()
                .insightId(UUID.randomUUID().toString())
                .title(title)
                .description(description)
                .type(type)
                .confidence(confidence)
                .confidenceScore(confidenceScore)
                .category("General")
                .actionable(actionable)
                .critical(critical)
                .discoveredAt(LocalDateTime.now())
                .discoveredBy("SYSTEM")
                .relatedMetrics(new ArrayList<>())
                .evidence(new HashMap<>())
                .acknowledged(false)
                .build();

        intelligence.addInsight(insight);

        log.info("Generated insight {} for intelligence {}: {} (confidence: {:.2f})",
                insight.getInsightId(), intelligenceId, title, confidenceScore);

        return insight;
    }

    /**
     * Detect anomaly
     */
    public ReportIntelligence.Anomaly detectAnomaly(Long intelligenceId, String metricName,
                                                     Double expectedValue, Double actualValue) {
        ReportIntelligence intelligence = intelligences.get(intelligenceId);
        if (intelligence == null) {
            throw new IllegalArgumentException("Intelligence not found: " + intelligenceId);
        }

        if (!Boolean.TRUE.equals(intelligence.getAnomalyDetectionEnabled())) {
            throw new IllegalStateException("Anomaly detection is not enabled");
        }

        double deviation = Math.abs(actualValue - expectedValue);
        double deviationPercentage = (deviation / Math.abs(expectedValue)) * 100.0;

        ReportIntelligence.AnomalySeverity severity = calculateAnomalySeverity(deviationPercentage);

        ReportIntelligence.Anomaly anomaly = ReportIntelligence.Anomaly.builder()
                .anomalyId(UUID.randomUUID().toString())
                .metricName(metricName)
                .description(String.format("Anomaly detected in %s: expected %.2f, actual %.2f",
                        metricName, expectedValue, actualValue))
                .severity(severity)
                .expectedValue(expectedValue)
                .actualValue(actualValue)
                .deviation(deviation)
                .deviationPercentage(deviationPercentage)
                .detectedAt(LocalDateTime.now())
                .occurredAt(LocalDateTime.now())
                .detectionMethod("Statistical")
                .resolved(false)
                .context(new HashMap<>())
                .build();

        intelligence.addAnomaly(anomaly);

        log.warn("Detected anomaly {} in intelligence {}: {} (severity: {}, deviation: {:.2f}%)",
                anomaly.getAnomalyId(), intelligenceId, metricName, severity, deviationPercentage);

        return anomaly;
    }

    /**
     * Recognize pattern
     */
    public ReportIntelligence.Pattern recognizePattern(Long intelligenceId, String patternName,
                                                        String patternType, Double confidence) {
        ReportIntelligence intelligence = intelligences.get(intelligenceId);
        if (intelligence == null) {
            throw new IllegalArgumentException("Intelligence not found: " + intelligenceId);
        }

        boolean significant = confidence >= intelligence.getConfidenceThreshold();

        ReportIntelligence.Pattern pattern = ReportIntelligence.Pattern.builder()
                .patternId(UUID.randomUUID().toString())
                .patternName(patternName)
                .patternType(patternType)
                .description("Pattern: " + patternName)
                .confidence(confidence)
                .occurrences(1)
                .firstOccurrence(LocalDateTime.now())
                .lastOccurrence(LocalDateTime.now())
                .affectedMetrics(new ArrayList<>())
                .recurring(false)
                .significant(significant)
                .characteristics(new HashMap<>())
                .build();

        intelligence.addPattern(pattern);

        log.info("Recognized pattern {} in intelligence {}: {} (confidence: {:.2f})",
                pattern.getPatternId(), intelligenceId, patternName, confidence);

        return pattern;
    }

    /**
     * Analyze trend
     */
    public ReportIntelligence.Trend analyzeTrend(Long intelligenceId, String metricName,
                                                  List<Double> values) {
        ReportIntelligence intelligence = intelligences.get(intelligenceId);
        if (intelligence == null) {
            throw new IllegalArgumentException("Intelligence not found: " + intelligenceId);
        }

        // Simple linear regression
        int n = values.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += values.get(i);
            sumXY += i * values.get(i);
            sumX2 += i * i;
        }

        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;

        // Calculate R-squared
        double meanY = sumY / n;
        double ssTotal = 0, ssResidual = 0;
        for (int i = 0; i < n; i++) {
            double predicted = slope * i + intercept;
            ssTotal += Math.pow(values.get(i) - meanY, 2);
            ssResidual += Math.pow(values.get(i) - predicted, 2);
        }
        double rSquared = 1 - (ssResidual / ssTotal);

        ReportIntelligence.TrendDirection direction = determineTrendDirection(slope);
        double strength = Math.abs(slope);

        ReportIntelligence.Trend trend = ReportIntelligence.Trend.builder()
                .trendId(UUID.randomUUID().toString())
                .metricName(metricName)
                .direction(direction)
                .strength(strength)
                .slope(slope)
                .rSquared(rSquared)
                .startDate(LocalDateTime.now().minusDays(values.size()))
                .endDate(LocalDateTime.now())
                .dataPoints(values.size())
                .description("Trend analysis for " + metricName)
                .statisticallySignificantTrend(rSquared >= 0.5)
                .pValue(0.05) // Simplified
                .values(values)
                .build();

        intelligence.addTrend(trend);

        log.info("Analyzed trend {} in intelligence {}: {} (direction: {}, R²: {:.2f})",
                trend.getTrendId(), intelligenceId, metricName, direction, rSquared);

        return trend;
    }

    /**
     * Create prediction
     */
    public ReportIntelligence.Prediction createPrediction(Long intelligenceId, String metricName,
                                                           Double predictedValue,
                                                           LocalDateTime targetDate) {
        ReportIntelligence intelligence = intelligences.get(intelligenceId);
        if (intelligence == null) {
            throw new IllegalArgumentException("Intelligence not found: " + intelligenceId);
        }

        if (!Boolean.TRUE.equals(intelligence.getPredictiveAnalyticsEnabled())) {
            throw new IllegalStateException("Predictive analytics is not enabled");
        }

        // Calculate confidence interval (simplified)
        double margin = predictedValue * 0.1; // 10% margin
        double lowerBound = predictedValue - margin;
        double upperBound = predictedValue + margin;
        double confidence = 0.8; // 80% confidence

        ReportIntelligence.Prediction prediction = ReportIntelligence.Prediction.builder()
                .predictionId(UUID.randomUUID().toString())
                .metricName(metricName)
                .description("Prediction for " + metricName)
                .predictedValue(predictedValue)
                .lowerBound(lowerBound)
                .upperBound(upperBound)
                .confidence(confidence)
                .predictionDate(LocalDateTime.now())
                .targetDate(targetDate)
                .predictionMethod("Time Series Analysis")
                .modelParameters(new HashMap<>())
                .validated(false)
                .build();

        intelligence.addPrediction(prediction);

        log.info("Created prediction {} in intelligence {}: {} = {:.2f} (target: {})",
                prediction.getPredictionId(), intelligenceId, metricName, predictedValue, targetDate);

        return prediction;
    }

    /**
     * Generate recommendation
     */
    public ReportIntelligence.Recommendation generateRecommendation(Long intelligenceId, String title,
                                                                     String description,
                                                                     ReportIntelligence.RecommendationPriority priority) {
        ReportIntelligence intelligence = intelligences.get(intelligenceId);
        if (intelligence == null) {
            throw new IllegalArgumentException("Intelligence not found: " + intelligenceId);
        }

        ReportIntelligence.Recommendation recommendation = ReportIntelligence.Recommendation.builder()
                .recommendationId(UUID.randomUUID().toString())
                .title(title)
                .description(description)
                .priority(priority)
                .category("General")
                .rationale("Based on intelligent analysis")
                .actionItems(new ArrayList<>())
                .expectedImpact(0.0)
                .recommendedAt(LocalDateTime.now())
                .recommendedBy("SYSTEM")
                .implemented(false)
                .build();

        intelligence.addRecommendation(recommendation);

        log.info("Generated recommendation {} in intelligence {}: {} (priority: {})",
                recommendation.getRecommendationId(), intelligenceId, title, priority);

        return recommendation;
    }

    /**
     * Perform root cause analysis
     */
    public ReportIntelligence.RootCause performRootCauseAnalysis(Long intelligenceId,
                                                                  String issueDescription) {
        ReportIntelligence intelligence = intelligences.get(intelligenceId);
        if (intelligence == null) {
            throw new IllegalArgumentException("Intelligence not found: " + intelligenceId);
        }

        // Simplified root cause analysis
        ReportIntelligence.RootCause rootCause = ReportIntelligence.RootCause.builder()
                .rootCauseId(UUID.randomUUID().toString())
                .issueDescription(issueDescription)
                .rootCause("Identified through automated analysis")
                .confidence(0.75)
                .category("System Analysis")
                .contributingFactors(new ArrayList<>())
                .analysisMethod("Automated Root Cause Analysis")
                .analyzedAt(LocalDateTime.now())
                .analyzedBy("SYSTEM")
                .recommendations(new ArrayList<>())
                .supportingData(new HashMap<>())
                .build();

        intelligence.addRootCause(rootCause);

        log.info("Performed root cause analysis {} in intelligence {}: {}",
                rootCause.getRootCauseId(), intelligenceId, issueDescription);

        return rootCause;
    }

    /**
     * Assess impact
     */
    public ReportIntelligence.Impact assessImpact(Long intelligenceId, String description,
                                                   ReportIntelligence.ImpactLevel level) {
        ReportIntelligence intelligence = intelligences.get(intelligenceId);
        if (intelligence == null) {
            throw new IllegalArgumentException("Intelligence not found: " + intelligenceId);
        }

        ReportIntelligence.Impact impact = ReportIntelligence.Impact.builder()
                .impactId(UUID.randomUUID().toString())
                .description(description)
                .level(level)
                .affectedArea("Report Analytics")
                .affectedUsers(0)
                .estimatedCost(0.0)
                .timeframe("Immediate")
                .assessedAt(LocalDateTime.now())
                .assessedBy("SYSTEM")
                .mitigationStrategies(new ArrayList<>())
                .impactMetrics(new HashMap<>())
                .build();

        intelligence.addImpact(impact);

        log.info("Assessed impact {} in intelligence {}: {} (level: {})",
                impact.getImpactId(), intelligenceId, description, level);

        return impact;
    }

    /**
     * Acknowledge insight
     */
    public void acknowledgeInsight(Long intelligenceId, String insightId) {
        ReportIntelligence intelligence = intelligences.get(intelligenceId);
        if (intelligence == null) {
            throw new IllegalArgumentException("Intelligence not found: " + intelligenceId);
        }

        intelligence.acknowledgeInsight(insightId);

        log.info("Acknowledged insight {} in intelligence {}", insightId, intelligenceId);
    }

    /**
     * Resolve anomaly
     */
    public void resolveAnomaly(Long intelligenceId, String anomalyId, String resolution) {
        ReportIntelligence intelligence = intelligences.get(intelligenceId);
        if (intelligence == null) {
            throw new IllegalArgumentException("Intelligence not found: " + intelligenceId);
        }

        intelligence.resolveAnomaly(anomalyId, resolution);

        log.info("Resolved anomaly {} in intelligence {}: {}", anomalyId, intelligenceId, resolution);
    }

    /**
     * Implement recommendation
     */
    public void implementRecommendation(Long intelligenceId, String recommendationId,
                                       String implementedBy, String outcome) {
        ReportIntelligence intelligence = intelligences.get(intelligenceId);
        if (intelligence == null) {
            throw new IllegalArgumentException("Intelligence not found: " + intelligenceId);
        }

        intelligence.implementRecommendation(recommendationId, implementedBy, outcome);

        log.info("Implemented recommendation {} in intelligence {} by {}: {}",
                recommendationId, intelligenceId, implementedBy, outcome);
    }

    /**
     * Validate prediction
     */
    public void validatePrediction(Long intelligenceId, String predictionId, Double actualValue) {
        ReportIntelligence intelligence = intelligences.get(intelligenceId);
        if (intelligence == null) {
            throw new IllegalArgumentException("Intelligence not found: " + intelligenceId);
        }

        intelligence.validatePrediction(predictionId, actualValue);

        log.info("Validated prediction {} in intelligence {} with actual value: {}",
                predictionId, intelligenceId, actualValue);
    }

    /**
     * Helper methods
     */

    private ReportIntelligence.InsightConfidence getConfidenceLevel(Double score) {
        if (score >= 0.8) return ReportIntelligence.InsightConfidence.VERY_HIGH;
        if (score >= 0.6) return ReportIntelligence.InsightConfidence.HIGH;
        if (score >= 0.4) return ReportIntelligence.InsightConfidence.MEDIUM;
        if (score >= 0.2) return ReportIntelligence.InsightConfidence.LOW;
        return ReportIntelligence.InsightConfidence.VERY_LOW;
    }

    private ReportIntelligence.AnomalySeverity calculateAnomalySeverity(Double deviationPercentage) {
        if (deviationPercentage >= 50) return ReportIntelligence.AnomalySeverity.CRITICAL;
        if (deviationPercentage >= 30) return ReportIntelligence.AnomalySeverity.HIGH;
        if (deviationPercentage >= 15) return ReportIntelligence.AnomalySeverity.MEDIUM;
        if (deviationPercentage >= 5) return ReportIntelligence.AnomalySeverity.LOW;
        return ReportIntelligence.AnomalySeverity.INFO;
    }

    private ReportIntelligence.TrendDirection determineTrendDirection(Double slope) {
        if (slope > 0.5) return ReportIntelligence.TrendDirection.STRONGLY_INCREASING;
        if (slope > 0.1) return ReportIntelligence.TrendDirection.INCREASING;
        if (slope < -0.5) return ReportIntelligence.TrendDirection.STRONGLY_DECREASING;
        if (slope < -0.1) return ReportIntelligence.TrendDirection.DECREASING;
        return ReportIntelligence.TrendDirection.STABLE;
    }

    /**
     * Delete intelligence
     */
    public void deleteIntelligence(Long intelligenceId) {
        ReportIntelligence removed = intelligences.remove(intelligenceId);
        if (removed != null) {
            log.info("Deleted intelligence {}", intelligenceId);
        }
    }

    /**
     * Get statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalIntelligences", intelligences.size());

        long totalInsights = intelligences.values().stream()
                .mapToLong(i -> i.getTotalInsights() != null ? i.getTotalInsights() : 0)
                .sum();

        long totalAnomalies = intelligences.values().stream()
                .mapToLong(i -> i.getTotalAnomalies() != null ? i.getTotalAnomalies() : 0)
                .sum();

        long totalPredictions = intelligences.values().stream()
                .mapToLong(i -> i.getTotalPredictions() != null ? i.getTotalPredictions() : 0)
                .sum();

        long totalRecommendations = intelligences.values().stream()
                .mapToLong(i -> i.getTotalRecommendations() != null ? i.getTotalRecommendations() : 0)
                .sum();

        stats.put("totalInsights", totalInsights);
        stats.put("totalAnomalies", totalAnomalies);
        stats.put("totalPredictions", totalPredictions);
        stats.put("totalRecommendations", totalRecommendations);

        return stats;
    }
}
