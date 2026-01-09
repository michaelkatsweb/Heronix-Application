package com.heronix.service;

import com.heronix.dto.ReportFeatureFlag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Report Feature Flag Service
 *
 * Provides feature flag management, A/B testing, gradual rollouts,
 * experimentation, and canary deployment capabilities.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 145 - Feature Flags & A/B Testing
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportFeatureFlagService {

    private final Map<Long, ReportFeatureFlag> flagStore = new ConcurrentHashMap<>();
    private final Map<String, Long> flagKeyIndex = new ConcurrentHashMap<>();
    private final AtomicLong flagIdGenerator = new AtomicLong(1);

    /**
     * Create a new feature flag
     */
    public ReportFeatureFlag createFeatureFlag(ReportFeatureFlag flag) {
        Long flagId = flagIdGenerator.getAndIncrement();
        flag.setFlagId(flagId);
        flag.setStatus("DRAFT");
        flag.setEnabled(false);
        flag.setCreatedAt(LocalDateTime.now());

        // Initialize metrics
        flag.setTotalEvaluations(0L);
        flag.setEnabledEvaluations(0L);
        flag.setDisabledEvaluations(0L);
        flag.setConversionRate(0.0);
        flag.setCurrentRolloutStage(0);
        flag.setRolloutPercentage(0.0);
        flag.setCanaryPercentage(0.0);
        flag.setCanaryStage(0);
        flag.setCurrentRing(0);
        flag.setErrorCount(0);
        flag.setErrorRate(0.0);
        flag.setCacheHits(0L);
        flag.setCacheMisses(0L);
        flag.setCacheHitRate(0.0);

        // Initialize collections
        if (flag.getFlagHistory() == null) {
            flag.setFlagHistory(new ArrayList<>());
        }
        if (flag.getEvaluationHistory() == null) {
            flag.setEvaluationHistory(new ArrayList<>());
        }
        if (flag.getVariants() == null) {
            flag.setVariants(new ArrayList<>());
        }

        flagStore.put(flagId, flag);
        flagKeyIndex.put(flag.getFlagKey(), flagId);

        log.info("Feature flag created: {} (key: {}, type: {})",
                flagId, flag.getFlagKey(), flag.getFlagType());
        return flag;
    }

    /**
     * Get feature flag by ID
     */
    public ReportFeatureFlag getFeatureFlag(Long flagId) {
        ReportFeatureFlag flag = flagStore.get(flagId);
        if (flag == null) {
            throw new IllegalArgumentException("Feature flag not found: " + flagId);
        }
        return flag;
    }

    /**
     * Get feature flag by key
     */
    public ReportFeatureFlag getFeatureFlagByKey(String flagKey) {
        Long flagId = flagKeyIndex.get(flagKey);
        if (flagId == null) {
            throw new IllegalArgumentException("Feature flag not found: " + flagKey);
        }
        return getFeatureFlag(flagId);
    }

    /**
     * Enable feature flag
     */
    public ReportFeatureFlag enableFlag(Long flagId) {
        ReportFeatureFlag flag = getFeatureFlag(flagId);

        flag.setEnabled(true);
        flag.setStatus("ACTIVE");
        flag.setLastModifiedAt(LocalDateTime.now());

        log.info("Feature flag enabled: {} (key: {})", flagId, flag.getFlagKey());
        return flag;
    }

    /**
     * Disable feature flag
     */
    public ReportFeatureFlag disableFlag(Long flagId) {
        ReportFeatureFlag flag = getFeatureFlag(flagId);

        flag.setEnabled(false);
        flag.setLastModifiedAt(LocalDateTime.now());

        log.info("Feature flag disabled: {} (key: {})", flagId, flag.getFlagKey());
        return flag;
    }

    /**
     * Evaluate feature flag for user
     */
    public Map<String, Object> evaluateFlag(Long flagId, String userId, Map<String, Object> context) {
        ReportFeatureFlag flag = getFeatureFlag(flagId);

        boolean result = flag.evaluate(userId, context);
        flag.recordEvaluation(result);

        Map<String, Object> evaluation = new HashMap<>();
        evaluation.put("flagId", flagId);
        evaluation.put("flagKey", flag.getFlagKey());
        evaluation.put("enabled", result);
        evaluation.put("userId", userId);
        evaluation.put("timestamp", LocalDateTime.now());

        // If A/B testing is enabled, get variant
        if (Boolean.TRUE.equals(flag.getAbTestEnabled())) {
            String variant = flag.getVariantForUser(userId);
            evaluation.put("variant", variant);
            flag.recordVariantEvaluation(variant);
        }

        log.debug("Flag evaluated: {} for user {} = {}", flag.getFlagKey(), userId, result);
        return evaluation;
    }

    /**
     * Update rollout percentage
     */
    public ReportFeatureFlag updateRollout(Long flagId, double percentage) {
        ReportFeatureFlag flag = getFeatureFlag(flagId);

        if (percentage < 0.0 || percentage > 100.0) {
            throw new IllegalArgumentException("Rollout percentage must be between 0 and 100");
        }

        flag.setRolloutPercentage(percentage);
        flag.setLastModifiedAt(LocalDateTime.now());

        log.info("Rollout updated: {} to {}%", flag.getFlagKey(), percentage);
        return flag;
    }

    /**
     * Increment gradual rollout
     */
    public ReportFeatureFlag incrementRollout(Long flagId) {
        ReportFeatureFlag flag = getFeatureFlag(flagId);

        if (!Boolean.TRUE.equals(flag.getGradualRolloutEnabled())) {
            throw new IllegalStateException("Gradual rollout is not enabled");
        }

        flag.incrementRollout();
        flag.setLastModifiedAt(LocalDateTime.now());

        log.info("Rollout incremented: {} to {}% (stage {}/{})",
                flag.getFlagKey(), flag.getRolloutPercentage(),
                flag.getCurrentRolloutStage(), flag.getTotalRolloutStages());
        return flag;
    }

    /**
     * Start A/B test
     */
    public ReportFeatureFlag startAbTest(Long flagId) {
        ReportFeatureFlag flag = getFeatureFlag(flagId);

        if (!Boolean.TRUE.equals(flag.getAbTestEnabled())) {
            throw new IllegalStateException("A/B testing is not enabled");
        }

        flag.startExperiment();
        flag.setEnabled(true);
        flag.setStatus("ACTIVE");

        log.info("A/B test started: {} (experiment: {})", flag.getFlagKey(), flag.getExperimentId());
        return flag;
    }

    /**
     * Complete A/B test
     */
    public ReportFeatureFlag completeAbTest(Long flagId, String winningVariant) {
        ReportFeatureFlag flag = getFeatureFlag(flagId);

        flag.completeExperiment(winningVariant);

        log.info("A/B test completed: {} (winner: {})", flag.getFlagKey(), winningVariant);
        return flag;
    }

    /**
     * Record conversion
     */
    public ReportFeatureFlag recordConversion(Long flagId, String userId, String variant) {
        ReportFeatureFlag flag = getFeatureFlag(flagId);

        if (variant == null) {
            variant = flag.getVariantForUser(userId);
        }

        flag.recordConversion(variant);

        log.info("Conversion recorded: {} for variant {}", flag.getFlagKey(), variant);
        return flag;
    }

    /**
     * Start canary deployment
     */
    public ReportFeatureFlag startCanary(Long flagId, double percentage) {
        ReportFeatureFlag flag = getFeatureFlag(flagId);

        if (!Boolean.TRUE.equals(flag.getCanaryEnabled())) {
            throw new IllegalStateException("Canary deployment is not enabled");
        }

        flag.setCanaryPercentage(percentage);
        flag.setCanaryStatus("MONITORING");
        flag.setCanaryHealthy(true);
        flag.setEnabled(true);
        flag.setRolloutPercentage(percentage);

        log.info("Canary deployment started: {} at {}%", flag.getFlagKey(), percentage);
        return flag;
    }

    /**
     * Promote canary
     */
    public ReportFeatureFlag promoteCanary(Long flagId) {
        ReportFeatureFlag flag = getFeatureFlag(flagId);

        if (!"MONITORING".equals(flag.getCanaryStatus())) {
            throw new IllegalStateException("Canary is not in monitoring state");
        }

        if (!flag.isCanaryHealthy()) {
            throw new IllegalStateException("Canary is not healthy");
        }

        flag.setCanaryStatus("PROMOTING");
        flag.setRolloutPercentage(100.0);
        flag.setCanaryPercentage(100.0);

        log.info("Canary promoted: {}", flag.getFlagKey());
        return flag;
    }

    /**
     * Rollback canary
     */
    public ReportFeatureFlag rollbackCanary(Long flagId, String reason) {
        ReportFeatureFlag flag = getFeatureFlag(flagId);

        flag.setCanaryStatus("ROLLING_BACK");
        flag.setCanaryHealthy(false);
        flag.setEnabled(false);
        flag.setRolloutPercentage(0.0);
        flag.setCanaryPercentage(0.0);

        log.warn("Canary rolled back: {} - {}", flag.getFlagKey(), reason);
        return flag;
    }

    /**
     * Activate kill switch
     */
    public ReportFeatureFlag activateKillSwitch(Long flagId, String reason) {
        ReportFeatureFlag flag = getFeatureFlag(flagId);

        flag.activateKillSwitch(reason);

        log.warn("Kill switch activated: {} - {}", flag.getFlagKey(), reason);
        return flag;
    }

    /**
     * Deactivate kill switch
     */
    public ReportFeatureFlag deactivateKillSwitch(Long flagId) {
        ReportFeatureFlag flag = getFeatureFlag(flagId);

        flag.deactivateKillSwitch();
        flag.setEnabled(true);

        log.info("Kill switch deactivated: {}", flag.getFlagKey());
        return flag;
    }

    /**
     * Get experiment results
     */
    public Map<String, Object> getExperimentResults(Long flagId) {
        ReportFeatureFlag flag = getFeatureFlag(flagId);

        if (!Boolean.TRUE.equals(flag.getAbTestEnabled())) {
            throw new IllegalStateException("A/B testing is not enabled");
        }

        Map<String, Object> results = new HashMap<>();
        results.put("flagId", flagId);
        results.put("flagKey", flag.getFlagKey());
        results.put("experimentId", flag.getExperimentId());
        results.put("experimentStatus", flag.getExperimentStatus());
        results.put("totalEvaluations", flag.getTotalEvaluations());
        results.put("conversionRate", flag.getConversionRate());
        results.put("variantEvaluations", flag.getVariantEvaluations());
        results.put("variantConversions", flag.getVariantConversions());
        results.put("winningVariant", flag.getWinningVariant());
        results.put("hasStatisticalSignificance", flag.hasStatisticalSignificance());

        // Calculate variant conversion rates
        if (flag.getVariantEvaluations() != null && flag.getVariantConversions() != null) {
            Map<String, Double> variantRates = new HashMap<>();
            for (String variant : flag.getVariantEvaluations().keySet()) {
                long evals = flag.getVariantEvaluations().get(variant);
                double conversions = flag.getVariantConversions().getOrDefault(variant, 0.0);
                double rate = evals > 0 ? (conversions / evals) * 100.0 : 0.0;
                variantRates.put(variant, rate);
            }
            results.put("variantConversionRates", variantRates);
        }

        return results;
    }

    /**
     * Get flag statistics
     */
    public Map<String, Object> getFlagStatistics(Long flagId) {
        ReportFeatureFlag flag = getFeatureFlag(flagId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("flagId", flagId);
        stats.put("flagKey", flag.getFlagKey());
        stats.put("enabled", flag.getEnabled());
        stats.put("status", flag.getStatus());
        stats.put("totalEvaluations", flag.getTotalEvaluations());
        stats.put("enabledEvaluations", flag.getEnabledEvaluations());
        stats.put("disabledEvaluations", flag.getDisabledEvaluations());
        stats.put("rolloutPercentage", flag.getRolloutPercentage());
        stats.put("isRolloutComplete", flag.isRolloutComplete());
        stats.put("conversionRate", flag.getConversionRate());
        stats.put("errorRate", flag.getErrorRate());
        stats.put("cacheHitRate", flag.getCacheHitRate());
        stats.put("lastEvaluationAt", flag.getLastEvaluationAt());

        return stats;
    }

    /**
     * Delete feature flag
     */
    public void deleteFeatureFlag(Long flagId) {
        ReportFeatureFlag flag = flagStore.remove(flagId);
        if (flag == null) {
            throw new IllegalArgumentException("Feature flag not found: " + flagId);
        }
        flagKeyIndex.remove(flag.getFlagKey());
        log.info("Feature flag deleted: {}", flagId);
    }

    /**
     * Get all feature flags
     */
    public List<ReportFeatureFlag> getAllFeatureFlags() {
        return new ArrayList<>(flagStore.values());
    }

    /**
     * Get active feature flags
     */
    public List<ReportFeatureFlag> getActiveFlags() {
        return flagStore.values().stream()
                .filter(ReportFeatureFlag::isActive)
                .toList();
    }

    /**
     * Get flags by environment
     */
    public List<ReportFeatureFlag> getFlagsByEnvironment(String environment) {
        return flagStore.values().stream()
                .filter(flag -> environment.equals(flag.getEnvironment()))
                .toList();
    }

    /**
     * Get statistics
     */
    public Map<String, Object> getStatistics() {
        long totalFlags = flagStore.size();
        long activeFlags = flagStore.values().stream()
                .filter(ReportFeatureFlag::isActive)
                .count();
        long runningExperiments = flagStore.values().stream()
                .filter(ReportFeatureFlag::isExperimentRunning)
                .count();
        long totalEvaluations = flagStore.values().stream()
                .mapToLong(f -> f.getTotalEvaluations() != null ? f.getTotalEvaluations() : 0L)
                .sum();
        long flagsWithKillSwitch = flagStore.values().stream()
                .filter(f -> Boolean.TRUE.equals(f.getKillSwitchEnabled()))
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFlags", totalFlags);
        stats.put("activeFlags", activeFlags);
        stats.put("runningExperiments", runningExperiments);
        stats.put("totalEvaluations", totalEvaluations);
        stats.put("flagsWithKillSwitch", flagsWithKillSwitch);
        stats.put("timestamp", LocalDateTime.now());

        return stats;
    }
}
