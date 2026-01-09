package com.heronix.service;

import com.heronix.dto.ReportRecommendation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Report Recommendation Engine Service
 *
 * Manages recommendation engine lifecycle, user profiling, and personalized recommendations.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 107 - Report Recommendation Engine & Personalization
 */
@Service
@Slf4j
public class ReportRecommendationService {

    private final Map<Long, ReportRecommendation> engines = new ConcurrentHashMap<>();
    private Long nextId = 1L;

    /**
     * Create recommendation engine
     */
    public ReportRecommendation createEngine(ReportRecommendation engine) {
        synchronized (this) {
            engine.setEngineId(nextId++);
        }

        engine.setStatus(ReportRecommendation.EngineStatus.INITIALIZING);
        engine.setIsActive(false);
        engine.setCreatedAt(LocalDateTime.now());

        // Initialize collections
        engine.setAlgorithms(new ArrayList<>());
        engine.setAlgorithmRegistry(new HashMap<>());
        engine.setUserProfiles(new ArrayList<>());
        engine.setProfileRegistry(new HashMap<>());
        engine.setRecommendations(new ArrayList<>());
        engine.setRecommendationRegistry(new HashMap<>());
        engine.setInteractions(new ArrayList<>());
        engine.setInteractionRegistry(new HashMap<>());
        engine.setItems(new ArrayList<>());
        engine.setItemRegistry(new HashMap<>());
        engine.setUserSimilarity(new HashMap<>());
        engine.setItemSimilarity(new HashMap<>());
        engine.setAbTests(new ArrayList<>());
        engine.setAbTestRegistry(new HashMap<>());
        engine.setRules(new ArrayList<>());
        engine.setRuleRegistry(new HashMap<>());
        engine.setFeedbacks(new ArrayList<>());
        engine.setFeedbackRegistry(new HashMap<>());
        engine.setEvents(new ArrayList<>());

        // Initialize counts
        engine.setTotalProfiles(0L);
        engine.setTotalRecommendations(0L);
        engine.setAcceptedRecommendations(0L);
        engine.setRejectedRecommendations(0L);
        engine.setTotalInteractions(0L);
        engine.setTotalItems(0L);
        engine.setActiveTests(0L);
        engine.setAverageRating(0.0);

        // Add default algorithms
        addDefaultAlgorithms(engine);

        engines.put(engine.getEngineId(), engine);

        log.info("Created recommendation engine: {} (ID: {})", engine.getEngineName(), engine.getEngineId());
        return engine;
    }

    /**
     * Get recommendation engine
     */
    public Optional<ReportRecommendation> getEngine(Long engineId) {
        return Optional.ofNullable(engines.get(engineId));
    }

    /**
     * Deploy engine
     */
    public void deployEngine(Long engineId) {
        ReportRecommendation engine = engines.get(engineId);
        if (engine == null) {
            throw new IllegalArgumentException("Recommendation engine not found: " + engineId);
        }

        engine.deployEngine();

        log.info("Deployed recommendation engine: {}", engineId);
    }

    /**
     * Create user profile
     */
    public ReportRecommendation.UserProfile createProfile(Long engineId, String userId, String userName,
                                                           String userType) {
        ReportRecommendation engine = engines.get(engineId);
        if (engine == null) {
            throw new IllegalArgumentException("Recommendation engine not found: " + engineId);
        }

        ReportRecommendation.UserProfile profile = ReportRecommendation.UserProfile.builder()
                .profileId(UUID.randomUUID().toString())
                .userId(userId)
                .userName(userName)
                .userType(userType)
                .preferences(new ArrayList<>())
                .categoryPreferences(new HashMap<>())
                .featureWeights(new HashMap<>())
                .viewedItems(new ArrayList<>())
                .favoriteItems(new ArrayList<>())
                .ignoredItems(new ArrayList<>())
                .interactionCounts(new HashMap<>())
                .engagementScore(0.0)
                .personaType("NEW_USER")
                .firstInteractionAt(LocalDateTime.now())
                .totalInteractions(0L)
                .demographics(new HashMap<>())
                .metadata(new HashMap<>())
                .build();

        engine.createProfile(profile);

        log.info("Created user profile: {} for engine: {}", userId, engineId);
        return profile;
    }

    /**
     * Add recommendable item
     */
    public ReportRecommendation.RecommendableItem addItem(Long engineId, String itemType, String title,
                                                           String description, List<String> categories,
                                                           List<String> tags) {
        ReportRecommendation engine = engines.get(engineId);
        if (engine == null) {
            throw new IllegalArgumentException("Recommendation engine not found: " + engineId);
        }

        ReportRecommendation.RecommendableItem item = ReportRecommendation.RecommendableItem.builder()
                .itemId(UUID.randomUUID().toString())
                .itemType(itemType)
                .title(title)
                .description(description)
                .categories(categories)
                .tags(tags)
                .features(new HashMap<>())
                .popularityScore(0.0)
                .qualityScore(0.5)
                .totalViews(0L)
                .totalDownloads(0L)
                .averageRating(0.0)
                .ratingCount(0)
                .createdAt(LocalDateTime.now())
                .metadata(new HashMap<>())
                .build();

        engine.addItem(item);

        log.info("Added item: {} to engine: {}", title, engineId);
        return item;
    }

    /**
     * Record user interaction
     */
    public ReportRecommendation.UserInteraction recordInteraction(Long engineId, String userId, String itemId,
                                                                   ReportRecommendation.InteractionType interactionType,
                                                                   Double rating) {
        ReportRecommendation engine = engines.get(engineId);
        if (engine == null) {
            throw new IllegalArgumentException("Recommendation engine not found: " + engineId);
        }

        ReportRecommendation.UserInteraction interaction = ReportRecommendation.UserInteraction.builder()
                .interactionId(UUID.randomUUID().toString())
                .userId(userId)
                .itemId(itemId)
                .interactionType(interactionType)
                .rating(rating)
                .durationSeconds(0)
                .context("web")
                .deviceType("desktop")
                .timestamp(LocalDateTime.now())
                .metadata(new HashMap<>())
                .build();

        engine.recordInteraction(interaction);

        // Update item statistics
        updateItemStatistics(engine, itemId, interactionType, rating);

        log.info("Recorded {} interaction for user {} on item {} in engine: {}",
                interactionType, userId, itemId, engineId);
        return interaction;
    }

    /**
     * Generate recommendations
     */
    public List<ReportRecommendation.Recommendation> generateRecommendations(Long engineId, String userId,
                                                                              Integer count, String context) {
        ReportRecommendation engine = engines.get(engineId);
        if (engine == null) {
            throw new IllegalArgumentException("Recommendation engine not found: " + engineId);
        }

        ReportRecommendation.UserProfile profile = engine.getProfile(userId);
        if (profile == null) {
            throw new IllegalArgumentException("User profile not found: " + userId);
        }

        List<ReportRecommendation.Recommendation> recommendations = new ArrayList<>();

        // Get algorithm
        ReportRecommendation.RecommendationAlgorithm algorithm = getPrimaryAlgorithm(engine);

        // Generate recommendations based on model type
        if (engine.getModel() == ReportRecommendation.RecommendationModel.COLLABORATIVE_FILTERING) {
            recommendations = generateCollaborativeRecommendations(engine, profile, count, algorithm);
        } else if (engine.getModel() == ReportRecommendation.RecommendationModel.CONTENT_BASED) {
            recommendations = generateContentBasedRecommendations(engine, profile, count, algorithm);
        } else if (engine.getModel() == ReportRecommendation.RecommendationModel.HYBRID) {
            recommendations = generateHybridRecommendations(engine, profile, count, algorithm);
        } else {
            recommendations = generateDefaultRecommendations(engine, profile, count, algorithm);
        }

        // Store recommendations
        for (ReportRecommendation.Recommendation rec : recommendations) {
            rec.setContext(context);
            engine.generateRecommendation(rec);
        }

        log.info("Generated {} recommendations for user {} in engine: {}", count, userId, engineId);
        return recommendations;
    }

    /**
     * Accept recommendation
     */
    public void acceptRecommendation(Long engineId, String recommendationId) {
        ReportRecommendation engine = engines.get(engineId);
        if (engine == null) {
            throw new IllegalArgumentException("Recommendation engine not found: " + engineId);
        }

        engine.acceptRecommendation(recommendationId);

        log.info("Accepted recommendation: {} in engine: {}", recommendationId, engineId);
    }

    /**
     * Reject recommendation
     */
    public void rejectRecommendation(Long engineId, String recommendationId) {
        ReportRecommendation engine = engines.get(engineId);
        if (engine == null) {
            throw new IllegalArgumentException("Recommendation engine not found: " + engineId);
        }

        engine.rejectRecommendation(recommendationId);

        log.info("Rejected recommendation: {} in engine: {}", recommendationId, engineId);
    }

    /**
     * Start A/B test
     */
    public ReportRecommendation.ABTest startABTest(Long engineId, String testName, String variantA,
                                                    String variantB, Double trafficSplit) {
        ReportRecommendation engine = engines.get(engineId);
        if (engine == null) {
            throw new IllegalArgumentException("Recommendation engine not found: " + engineId);
        }

        ReportRecommendation.ABTest test = ReportRecommendation.ABTest.builder()
                .testId(UUID.randomUUID().toString())
                .testName(testName)
                .description("A/B test comparing " + variantA + " vs " + variantB)
                .active(true)
                .variantA(variantA)
                .variantB(variantB)
                .trafficSplit(trafficSplit)
                .variantAViews(0L)
                .variantBViews(0L)
                .variantAConversions(0L)
                .variantBConversions(0L)
                .variantAConversionRate(0.0)
                .variantBConversionRate(0.0)
                .confidenceLevel(0.0)
                .startedAt(LocalDateTime.now())
                .metadata(new HashMap<>())
                .build();

        engine.startABTest(test);

        log.info("Started A/B test: {} in engine: {}", testName, engineId);
        return test;
    }

    /**
     * Submit feedback
     */
    public ReportRecommendation.RecommendationFeedback submitFeedback(Long engineId, String recommendationId,
                                                                       String userId, Double rating,
                                                                       Boolean helpful, String comment) {
        ReportRecommendation engine = engines.get(engineId);
        if (engine == null) {
            throw new IllegalArgumentException("Recommendation engine not found: " + engineId);
        }

        ReportRecommendation.RecommendationFeedback feedback = ReportRecommendation.RecommendationFeedback.builder()
                .feedbackId(UUID.randomUUID().toString())
                .recommendationId(recommendationId)
                .userId(userId)
                .feedbackType(rating != null ? ReportRecommendation.FeedbackType.EXPLICIT_RATING :
                        ReportRecommendation.FeedbackType.IMPLICIT_CLICK)
                .rating(rating)
                .helpful(helpful)
                .comment(comment)
                .reportedIssues(new ArrayList<>())
                .timestamp(LocalDateTime.now())
                .metadata(new HashMap<>())
                .build();

        engine.submitFeedback(feedback);

        log.info("Submitted feedback for recommendation: {} in engine: {}", recommendationId, engineId);
        return feedback;
    }

    /**
     * Train model
     */
    public void trainModel(Long engineId) {
        ReportRecommendation engine = engines.get(engineId);
        if (engine == null) {
            throw new IllegalArgumentException("Recommendation engine not found: " + engineId);
        }

        engine.setStatus(ReportRecommendation.EngineStatus.TRAINING);

        // Simulate training process
        // In production, this would use actual ML algorithms
        computeSimilarityMatrices(engine);

        // Update model metrics
        Random random = new Random();
        engine.setModelAccuracy(0.75 + random.nextDouble() * 0.2); // 75-95%
        engine.setLastTrainingAt(LocalDateTime.now());

        engine.setStatus(ReportRecommendation.EngineStatus.READY);

        log.info("Trained recommendation model for engine: {}", engineId);
    }

    /**
     * Update metrics
     */
    public void updateMetrics(Long engineId) {
        ReportRecommendation engine = engines.get(engineId);
        if (engine == null) {
            throw new IllegalArgumentException("Recommendation engine not found: " + engineId);
        }

        long activeProfiles = engine.getActiveProfiles().size();
        long totalRecs = engine.getTotalRecommendations() != null ? engine.getTotalRecommendations() : 0L;
        long accepted = engine.getAcceptedRecommendations() != null ? engine.getAcceptedRecommendations() : 0L;
        double acceptanceRate = totalRecs > 0 ? (double) accepted / totalRecs : 0.0;

        // Calculate engagement rate
        long totalInteractions = engine.getTotalInteractions() != null ? engine.getTotalInteractions() : 0L;
        long totalProfiles = engine.getTotalProfiles() != null ? engine.getTotalProfiles() : 0L;
        double engagementRate = totalProfiles > 0 ? (double) totalInteractions / totalProfiles : 0.0;

        // Simulate metrics
        Random random = new Random();

        ReportRecommendation.RecommendationMetrics metrics = ReportRecommendation.RecommendationMetrics.builder()
                .totalProfiles(engine.getTotalProfiles())
                .activeProfiles(activeProfiles)
                .totalRecommendations(totalRecs)
                .acceptedRecommendations(accepted)
                .acceptanceRate(acceptanceRate)
                .averageScore(0.7 + random.nextDouble() * 0.25)
                .averageConfidence(0.65 + random.nextDouble() * 0.3)
                .totalInteractions(totalInteractions)
                .engagementRate(engagementRate)
                .clickThroughRate(0.15 + random.nextDouble() * 0.15)
                .conversionRate(0.05 + random.nextDouble() * 0.1)
                .precision(0.6 + random.nextDouble() * 0.3)
                .recall(0.5 + random.nextDouble() * 0.3)
                .f1Score(0.55 + random.nextDouble() * 0.3)
                .ndcg(0.7 + random.nextDouble() * 0.25)
                .diversity(0.6 + random.nextDouble() * 0.3)
                .novelty(0.5 + random.nextDouble() * 0.4)
                .coverage(0.7 + random.nextDouble() * 0.2)
                .activeABTests(engine.getActiveTests())
                .averageRating(engine.getAverageRating())
                .measuredAt(LocalDateTime.now())
                .build();

        engine.setMetrics(metrics);
        engine.setLastMetricsUpdate(LocalDateTime.now());

        log.info("Updated metrics for recommendation engine: {}", engineId);
    }

    /**
     * Delete engine
     */
    public void deleteEngine(Long engineId) {
        engines.remove(engineId);
        log.info("Deleted recommendation engine: {}", engineId);
    }

    /**
     * Get statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        int totalEngines = engines.size();
        int activeEngines = 0;
        long totalProfiles = 0L;
        long totalRecommendations = 0L;
        long totalInteractions = 0L;

        for (ReportRecommendation engine : engines.values()) {
            if (Boolean.TRUE.equals(engine.getIsActive())) {
                activeEngines++;
            }
            totalProfiles += engine.getTotalProfiles() != null ? engine.getTotalProfiles() : 0L;
            totalRecommendations += engine.getTotalRecommendations() != null ? engine.getTotalRecommendations() : 0L;
            totalInteractions += engine.getTotalInteractions() != null ? engine.getTotalInteractions() : 0L;
        }

        stats.put("totalEngines", totalEngines);
        stats.put("activeEngines", activeEngines);
        stats.put("totalProfiles", totalProfiles);
        stats.put("totalRecommendations", totalRecommendations);
        stats.put("totalInteractions", totalInteractions);
        stats.put("timestamp", LocalDateTime.now());

        return stats;
    }

    // Helper Methods

    private void addDefaultAlgorithms(ReportRecommendation engine) {
        // User-based collaborative filtering
        ReportRecommendation.RecommendationAlgorithm userCF = ReportRecommendation.RecommendationAlgorithm.builder()
                .algorithmId(UUID.randomUUID().toString())
                .algorithmName("User-Based Collaborative Filtering")
                .algorithmType(ReportRecommendation.AlgorithmType.USER_BASED_CF)
                .enabled(true)
                .weight(0.4)
                .parameters(new HashMap<>())
                .neighbors(20)
                .totalRecommendations(0L)
                .accuracy(0.0)
                .metadata(new HashMap<>())
                .build();

        // Item-based collaborative filtering
        ReportRecommendation.RecommendationAlgorithm itemCF = ReportRecommendation.RecommendationAlgorithm.builder()
                .algorithmId(UUID.randomUUID().toString())
                .algorithmName("Item-Based Collaborative Filtering")
                .algorithmType(ReportRecommendation.AlgorithmType.ITEM_BASED_CF)
                .enabled(true)
                .weight(0.6)
                .parameters(new HashMap<>())
                .neighbors(20)
                .totalRecommendations(0L)
                .accuracy(0.0)
                .metadata(new HashMap<>())
                .build();

        engine.addAlgorithm(userCF);
        engine.addAlgorithm(itemCF);
        engine.setPrimaryAlgorithm(itemCF.getAlgorithmId());
    }

    private ReportRecommendation.RecommendationAlgorithm getPrimaryAlgorithm(ReportRecommendation engine) {
        if (engine.getPrimaryAlgorithm() != null) {
            return engine.getAlgorithmRegistry().get(engine.getPrimaryAlgorithm());
        }
        return engine.getAlgorithms().isEmpty() ? null : engine.getAlgorithms().get(0);
    }

    private void updateItemStatistics(ReportRecommendation engine, String itemId,
                                       ReportRecommendation.InteractionType interactionType, Double rating) {
        ReportRecommendation.RecommendableItem item = engine.getItem(itemId);
        if (item != null) {
            if (interactionType == ReportRecommendation.InteractionType.VIEW) {
                item.setTotalViews(item.getTotalViews() + 1);
            } else if (interactionType == ReportRecommendation.InteractionType.DOWNLOAD) {
                item.setTotalDownloads(item.getTotalDownloads() + 1);
            }

            if (rating != null) {
                int count = item.getRatingCount();
                double currentAvg = item.getAverageRating();
                double newAvg = (currentAvg * count + rating) / (count + 1);
                item.setAverageRating(newAvg);
                item.setRatingCount(count + 1);
            }

            item.setLastAccessedAt(LocalDateTime.now());

            // Update popularity score
            item.setPopularityScore(calculatePopularityScore(item));
        }
    }

    private double calculatePopularityScore(ReportRecommendation.RecommendableItem item) {
        double viewWeight = 0.3;
        double downloadWeight = 0.4;
        double ratingWeight = 0.3;

        double viewScore = Math.log1p(item.getTotalViews()) / 10.0;
        double downloadScore = Math.log1p(item.getTotalDownloads()) / 10.0;
        double ratingScore = item.getAverageRating() / 5.0;

        return Math.min(1.0, viewWeight * viewScore + downloadWeight * downloadScore + ratingWeight * ratingScore);
    }

    private void computeSimilarityMatrices(ReportRecommendation engine) {
        // Simplified similarity computation
        Random random = new Random();

        // Compute user similarity
        for (ReportRecommendation.UserProfile profile1 : engine.getUserProfiles()) {
            Map<String, Double> similarities = new HashMap<>();
            for (ReportRecommendation.UserProfile profile2 : engine.getUserProfiles()) {
                if (!profile1.getUserId().equals(profile2.getUserId())) {
                    similarities.put(profile2.getUserId(), random.nextDouble());
                }
            }
            engine.getUserSimilarity().put(profile1.getUserId(), similarities);
        }

        // Compute item similarity
        for (ReportRecommendation.RecommendableItem item1 : engine.getItems()) {
            Map<String, Double> similarities = new HashMap<>();
            for (ReportRecommendation.RecommendableItem item2 : engine.getItems()) {
                if (!item1.getItemId().equals(item2.getItemId())) {
                    similarities.put(item2.getItemId(), random.nextDouble());
                }
            }
            engine.getItemSimilarity().put(item1.getItemId(), similarities);
        }
    }

    private List<ReportRecommendation.Recommendation> generateCollaborativeRecommendations(
            ReportRecommendation engine, ReportRecommendation.UserProfile profile, Integer count,
            ReportRecommendation.RecommendationAlgorithm algorithm) {

        List<ReportRecommendation.Recommendation> recommendations = new ArrayList<>();
        Random random = new Random();

        // Find similar users
        Map<String, Double> userSimilarities = engine.getUserSimilarity().get(profile.getUserId());
        if (userSimilarities == null) {
            return generateDefaultRecommendations(engine, profile, count, algorithm);
        }

        // Get items from similar users
        List<String> candidateItems = new ArrayList<>();
        for (ReportRecommendation.UserProfile otherProfile : engine.getUserProfiles()) {
            if (!otherProfile.getUserId().equals(profile.getUserId())) {
                candidateItems.addAll(otherProfile.getViewedItems());
            }
        }

        // Remove already viewed items
        candidateItems.removeAll(profile.getViewedItems());

        // Generate recommendations
        int recCount = Math.min(count, candidateItems.size());
        for (int i = 0; i < recCount; i++) {
            String itemId = candidateItems.get(random.nextInt(candidateItems.size()));
            ReportRecommendation.RecommendableItem item = engine.getItem(itemId);

            if (item != null) {
                recommendations.add(createRecommendation(profile.getUserId(), item, algorithm, i + 1));
            }
        }

        return recommendations;
    }

    private List<ReportRecommendation.Recommendation> generateContentBasedRecommendations(
            ReportRecommendation engine, ReportRecommendation.UserProfile profile, Integer count,
            ReportRecommendation.RecommendationAlgorithm algorithm) {

        List<ReportRecommendation.Recommendation> recommendations = new ArrayList<>();

        // Get user's preferred categories
        List<String> viewedItems = profile.getViewedItems();
        Set<String> preferredCategories = new HashSet<>();

        for (String itemId : viewedItems) {
            ReportRecommendation.RecommendableItem item = engine.getItem(itemId);
            if (item != null && item.getCategories() != null) {
                preferredCategories.addAll(item.getCategories());
            }
        }

        // Find similar items
        List<ReportRecommendation.RecommendableItem> candidates = engine.getItems().stream()
                .filter(item -> !viewedItems.contains(item.getItemId()))
                .filter(item -> item.getCategories() != null &&
                        item.getCategories().stream().anyMatch(preferredCategories::contains))
                .limit(count)
                .collect(Collectors.toList());

        for (int i = 0; i < candidates.size(); i++) {
            recommendations.add(createRecommendation(profile.getUserId(), candidates.get(i), algorithm, i + 1));
        }

        return recommendations;
    }

    private List<ReportRecommendation.Recommendation> generateHybridRecommendations(
            ReportRecommendation engine, ReportRecommendation.UserProfile profile, Integer count,
            ReportRecommendation.RecommendationAlgorithm algorithm) {

        List<ReportRecommendation.Recommendation> cfRecs = generateCollaborativeRecommendations(
                engine, profile, count / 2, algorithm);
        List<ReportRecommendation.Recommendation> cbRecs = generateContentBasedRecommendations(
                engine, profile, count / 2, algorithm);

        List<ReportRecommendation.Recommendation> combined = new ArrayList<>();
        combined.addAll(cfRecs);
        combined.addAll(cbRecs);

        return combined.stream().limit(count).collect(Collectors.toList());
    }

    private List<ReportRecommendation.Recommendation> generateDefaultRecommendations(
            ReportRecommendation engine, ReportRecommendation.UserProfile profile, Integer count,
            ReportRecommendation.RecommendationAlgorithm algorithm) {

        List<ReportRecommendation.Recommendation> recommendations = new ArrayList<>();

        // Recommend popular items
        List<ReportRecommendation.RecommendableItem> popularItems = engine.getItems().stream()
                .filter(item -> !profile.getViewedItems().contains(item.getItemId()))
                .sorted((i1, i2) -> Double.compare(i2.getPopularityScore(), i1.getPopularityScore()))
                .limit(count)
                .collect(Collectors.toList());

        for (int i = 0; i < popularItems.size(); i++) {
            recommendations.add(createRecommendation(profile.getUserId(), popularItems.get(i), algorithm, i + 1));
        }

        return recommendations;
    }

    private ReportRecommendation.Recommendation createRecommendation(String userId,
                                                                      ReportRecommendation.RecommendableItem item,
                                                                      ReportRecommendation.RecommendationAlgorithm algorithm,
                                                                      Integer rank) {
        Random random = new Random();
        double score = 0.5 + random.nextDouble() * 0.5;
        double confidence = 0.6 + random.nextDouble() * 0.35;

        List<String> reasons = new ArrayList<>();
        reasons.add("Based on your viewing history");
        reasons.add("Popular in your category");
        reasons.add("Highly rated by similar users");

        return ReportRecommendation.Recommendation.builder()
                .recommendationId(UUID.randomUUID().toString())
                .userId(userId)
                .itemId(item.getItemId())
                .itemType(item.getItemType())
                .itemTitle(item.getTitle())
                .status(ReportRecommendation.RecommendationStatus.GENERATED)
                .score(score)
                .confidence(confidence)
                .algorithm(algorithm != null ? algorithm.getAlgorithmName() : "Default")
                .reasons(reasons)
                .explanation("This item matches your interests and preferences")
                .rank(rank)
                .itemFeatures(item.getFeatures())
                .generatedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .metadata(new HashMap<>())
                .build();
    }
}
