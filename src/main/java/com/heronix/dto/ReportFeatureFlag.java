package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Feature Flag DTO
 *
 * Represents feature flag management, A/B testing, gradual rollouts,
 * experimentation, and canary deployments for controlled feature releases.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 145 - Feature Flags & A/B Testing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportFeatureFlag {

    private Long flagId;
    private String flagKey;
    private String flagName;
    private String description;

    // Flag Configuration
    private String flagType; // RELEASE, EXPERIMENT, OPERATIONAL, PERMISSION
    private Boolean enabled;
    private String status; // DRAFT, ACTIVE, ARCHIVED, EXPIRED
    private String environment; // DEVELOPMENT, STAGING, PRODUCTION
    private Map<String, Object> flagConfig;

    // Rollout Strategy
    private String rolloutStrategy; // PERCENTAGE, GRADUAL, TARGETED, CANARY, RING
    private Double rolloutPercentage; // 0.0 to 100.0
    private Integer currentRolloutStage;
    private Integer totalRolloutStages;
    private Boolean gradualRolloutEnabled;
    private Integer rolloutIncrementPercent;
    private Integer rolloutIntervalHours;
    private Map<String, Object> rolloutConfig;

    // Targeting Rules
    private Boolean targetingEnabled;
    private List<Map<String, Object>> targetingRules;
    private List<String> targetedUserIds;
    private List<String> targetedUserGroups;
    private List<String> targetedRegions;
    private Map<String, Object> targetingAttributes;

    // A/B Testing
    private Boolean abTestEnabled;
    private String experimentId;
    private String hypothesis;
    private List<Map<String, Object>> variants;
    private Map<String, Double> variantDistribution;
    private String controlVariant;
    private String winningVariant;
    private Double minimumSampleSize;
    private Double confidenceLevel; // 0.0 to 1.0
    private Map<String, Object> abTestConfig;

    // Metrics & Analytics
    private Long totalEvaluations;
    private Long enabledEvaluations;
    private Long disabledEvaluations;
    private Double conversionRate; // percentage
    private Map<String, Long> variantEvaluations;
    private Map<String, Double> variantConversions;
    private Map<String, Object> metricsTracked;

    // Experimentation
    private Boolean experimentEnabled;
    private LocalDateTime experimentStartDate;
    private LocalDateTime experimentEndDate;
    private Integer experimentDurationDays;
    private String experimentStatus; // DRAFT, RUNNING, COMPLETED, STOPPED
    private List<String> successMetrics;
    private Map<String, Object> experimentResults;

    // Canary Deployment
    private Boolean canaryEnabled;
    private Double canaryPercentage;
    private Integer canaryStage;
    private List<String> canaryMetrics;
    private Boolean canaryHealthy;
    private String canaryStatus; // INITIALIZING, MONITORING, PROMOTING, ROLLING_BACK
    private Map<String, Object> canaryConfig;

    // Ring Deployment
    private Boolean ringDeploymentEnabled;
    private Integer currentRing;
    private Integer totalRings;
    private List<String> ringDefinitions;
    private Map<String, Object> ringConfig;

    // Kill Switch
    private Boolean killSwitchEnabled;
    private String killSwitchReason;
    private LocalDateTime killSwitchActivatedAt;
    private Boolean autoKillSwitchEnabled;
    private List<String> killSwitchConditions;

    // Scheduling
    private Boolean schedulingEnabled;
    private LocalDateTime scheduledStartDate;
    private LocalDateTime scheduledEndDate;
    private Boolean autoEnableOnStart;
    private Boolean autoDisableOnEnd;

    // Dependencies
    private Boolean hasDependencies;
    private List<String> dependsOnFlags;
    private List<String> prerequisiteFlags;
    private Map<String, Object> dependencyConfig;

    // Audit & History
    private List<Map<String, Object>> flagHistory;
    private List<Map<String, Object>> evaluationHistory;
    private LocalDateTime lastEvaluationAt;
    private LocalDateTime lastModifiedAt;
    private String lastModifiedBy;

    // Performance
    private Double averageEvaluationTime; // milliseconds
    private Long cacheHits;
    private Long cacheMisses;
    private Double cacheHitRate; // percentage

    // Monitoring
    private Boolean monitoringEnabled;
    private List<String> monitoredMetrics;
    private Map<String, Object> healthMetrics;
    private Integer errorCount;
    private Double errorRate; // percentage

    // Integration
    private String integrationPlatform; // LAUNCHDARKLY, SPLIT, UNLEASH, FLAGSMITH, CUSTOM
    private String sdkVersion;
    private Map<String, Object> integrationConfig;

    // Compliance
    private Boolean complianceCheckEnabled;
    private List<String> complianceRequirements;
    private Boolean gdprCompliant;
    private Boolean hipaaCompliant;

    // Metadata
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, String> tags;
    private Map<String, Object> metadata;

    // Helper Methods

    /**
     * Evaluate flag for user
     */
    public boolean evaluate(String userId, Map<String, Object> context) {
        if (!Boolean.TRUE.equals(this.enabled)) {
            return false;
        }

        // Check kill switch
        if (Boolean.TRUE.equals(this.killSwitchEnabled)) {
            return false;
        }

        // Check scheduling
        if (Boolean.TRUE.equals(this.schedulingEnabled)) {
            LocalDateTime now = LocalDateTime.now();
            if (this.scheduledStartDate != null && now.isBefore(this.scheduledStartDate)) {
                return false;
            }
            if (this.scheduledEndDate != null && now.isAfter(this.scheduledEndDate)) {
                return false;
            }
        }

        // Check targeting rules
        if (Boolean.TRUE.equals(this.targetingEnabled)) {
            if (this.targetedUserIds != null && this.targetedUserIds.contains(userId)) {
                return true;
            }
        }

        // Check rollout percentage
        if (this.rolloutPercentage != null) {
            double hash = Math.abs(userId.hashCode() % 100);
            return hash < this.rolloutPercentage;
        }

        return true;
    }

    /**
     * Record evaluation
     */
    public void recordEvaluation(boolean enabled) {
        this.totalEvaluations = (this.totalEvaluations != null ? this.totalEvaluations : 0L) + 1L;

        if (enabled) {
            this.enabledEvaluations = (this.enabledEvaluations != null ? this.enabledEvaluations : 0L) + 1L;
        } else {
            this.disabledEvaluations = (this.disabledEvaluations != null ? this.disabledEvaluations : 0L) + 1L;
        }

        this.lastEvaluationAt = LocalDateTime.now();
    }

    /**
     * Record variant evaluation
     */
    public void recordVariantEvaluation(String variant) {
        if (this.variantEvaluations == null) {
            this.variantEvaluations = new java.util.HashMap<>();
        }

        Long count = this.variantEvaluations.getOrDefault(variant, 0L);
        this.variantEvaluations.put(variant, count + 1);
    }

    /**
     * Record conversion
     */
    public void recordConversion(String variant) {
        if (this.variantConversions == null) {
            this.variantConversions = new java.util.HashMap<>();
        }

        Double conversions = this.variantConversions.getOrDefault(variant, 0.0);
        this.variantConversions.put(variant, conversions + 1.0);

        updateConversionRate();
    }

    /**
     * Update conversion rate
     */
    private void updateConversionRate() {
        if (this.totalEvaluations != null && this.totalEvaluations > 0) {
            double totalConversions = 0.0;
            if (this.variantConversions != null) {
                totalConversions = this.variantConversions.values().stream()
                        .mapToDouble(Double::doubleValue)
                        .sum();
            }
            this.conversionRate = (totalConversions / this.totalEvaluations) * 100.0;
        }
    }

    /**
     * Increment rollout percentage
     */
    public void incrementRollout() {
        if (Boolean.TRUE.equals(this.gradualRolloutEnabled)) {
            Integer increment = this.rolloutIncrementPercent != null ? this.rolloutIncrementPercent : 10;
            this.rolloutPercentage = Math.min((this.rolloutPercentage != null ? this.rolloutPercentage : 0.0) + increment, 100.0);

            if (this.currentRolloutStage != null) {
                this.currentRolloutStage++;
            }
        }
    }

    /**
     * Activate kill switch
     */
    public void activateKillSwitch(String reason) {
        this.killSwitchEnabled = true;
        this.killSwitchReason = reason;
        this.killSwitchActivatedAt = LocalDateTime.now();
        this.enabled = false;
    }

    /**
     * Deactivate kill switch
     */
    public void deactivateKillSwitch() {
        this.killSwitchEnabled = false;
        this.killSwitchReason = null;
        this.killSwitchActivatedAt = null;
    }

    /**
     * Start experiment
     */
    public void startExperiment() {
        this.experimentStatus = "RUNNING";
        this.experimentStartDate = LocalDateTime.now();
        if (this.experimentDurationDays != null) {
            this.experimentEndDate = this.experimentStartDate.plusDays(this.experimentDurationDays);
        }
    }

    /**
     * Complete experiment
     */
    public void completeExperiment(String winningVariant) {
        this.experimentStatus = "COMPLETED";
        this.winningVariant = winningVariant;
        this.experimentEndDate = LocalDateTime.now();
    }

    /**
     * Check if experiment is running
     */
    public boolean isExperimentRunning() {
        return "RUNNING".equals(this.experimentStatus) &&
               this.experimentStartDate != null &&
               (this.experimentEndDate == null || LocalDateTime.now().isBefore(this.experimentEndDate));
    }

    /**
     * Check if canary is healthy
     */
    public boolean isCanaryHealthy() {
        return Boolean.TRUE.equals(this.canaryHealthy) &&
               this.errorRate != null && this.errorRate < 1.0;
    }

    /**
     * Get variant for user
     */
    public String getVariantForUser(String userId) {
        if (this.variants == null || this.variants.isEmpty()) {
            return "control";
        }

        // Use consistent hashing to assign variant
        int hash = Math.abs(userId.hashCode() % 100);
        double cumulative = 0.0;

        if (this.variantDistribution != null) {
            for (Map.Entry<String, Double> entry : this.variantDistribution.entrySet()) {
                cumulative += entry.getValue();
                if (hash < cumulative) {
                    return entry.getKey();
                }
            }
        }

        return this.controlVariant != null ? this.controlVariant : "control";
    }

    /**
     * Check if flag is active
     */
    public boolean isActive() {
        return "ACTIVE".equals(this.status) && Boolean.TRUE.equals(this.enabled);
    }

    /**
     * Check if rollout is complete
     */
    public boolean isRolloutComplete() {
        return this.rolloutPercentage != null && this.rolloutPercentage >= 100.0;
    }

    /**
     * Get statistical significance
     */
    public boolean hasStatisticalSignificance() {
        return this.totalEvaluations != null &&
               this.minimumSampleSize != null &&
               this.totalEvaluations >= this.minimumSampleSize;
    }
}
