package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Recommendation Engine & Personalization DTO
 *
 * Manages intelligent recommendation and personalization for report delivery.
 *
 * Features:
 * - Collaborative filtering
 * - Content-based filtering
 * - Hybrid recommendation models
 * - User behavior tracking
 * - Personalization profiles
 * - A/B testing for recommendations
 * - Real-time recommendation updates
 * - Recommendation explanation
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 107 - Report Recommendation Engine & Personalization
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRecommendation {

    // Engine Information
    private Long engineId;
    private String engineName;
    private String description;
    private EngineStatus status;
    private Boolean isActive;

    // Recommendation Model
    private RecommendationModel model;
    private String modelVersion;
    private Double modelAccuracy;
    private LocalDateTime lastTrainingAt;

    // Algorithms
    private List<RecommendationAlgorithm> algorithms;
    private Map<String, RecommendationAlgorithm> algorithmRegistry;
    private String primaryAlgorithm;

    // User Profiles
    private List<UserProfile> userProfiles;
    private Map<String, UserProfile> profileRegistry;
    private Long totalProfiles;

    // Recommendations
    private List<Recommendation> recommendations;
    private Map<String, Recommendation> recommendationRegistry;
    private Long totalRecommendations;
    private Long acceptedRecommendations;
    private Long rejectedRecommendations;

    // User Interactions
    private List<UserInteraction> interactions;
    private Map<String, UserInteraction> interactionRegistry;
    private Long totalInteractions;

    // Items (Reports/Content)
    private List<RecommendableItem> items;
    private Map<String, RecommendableItem> itemRegistry;
    private Long totalItems;

    // Similarity Matrix
    private Map<String, Map<String, Double>> userSimilarity;
    private Map<String, Map<String, Double>> itemSimilarity;

    // A/B Testing
    private List<ABTest> abTests;
    private Map<String, ABTest> abTestRegistry;
    private Long activeTests;

    // Personalization Rules
    private List<PersonalizationRule> rules;
    private Map<String, PersonalizationRule> ruleRegistry;

    // Feedback
    private List<RecommendationFeedback> feedbacks;
    private Map<String, RecommendationFeedback> feedbackRegistry;
    private Double averageRating;

    // Metrics
    private RecommendationMetrics metrics;
    private LocalDateTime lastMetricsUpdate;

    // Events
    private List<RecommendationEvent> events;
    private LocalDateTime lastEventAt;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime deployedAt;
    private LocalDateTime lastRecommendationAt;

    /**
     * Engine Status
     */
    public enum EngineStatus {
        INITIALIZING,
        TRAINING,
        READY,
        RECOMMENDING,
        UPDATING,
        ERROR
    }

    /**
     * Recommendation Model
     */
    public enum RecommendationModel {
        COLLABORATIVE_FILTERING,
        CONTENT_BASED,
        HYBRID,
        MATRIX_FACTORIZATION,
        DEEP_LEARNING,
        KNOWLEDGE_BASED,
        DEMOGRAPHIC,
        CONTEXT_AWARE
    }

    /**
     * Algorithm Type
     */
    public enum AlgorithmType {
        USER_BASED_CF,
        ITEM_BASED_CF,
        SVD,
        ALS,
        KNN,
        NEURAL_COLLABORATIVE,
        FACTORIZATION_MACHINES,
        WIDE_AND_DEEP
    }

    /**
     * Interaction Type
     */
    public enum InteractionType {
        VIEW,
        CLICK,
        DOWNLOAD,
        SHARE,
        RATE,
        FAVORITE,
        IGNORE,
        SEARCH
    }

    /**
     * Recommendation Status
     */
    public enum RecommendationStatus {
        GENERATED,
        DELIVERED,
        VIEWED,
        ACCEPTED,
        REJECTED,
        EXPIRED
    }

    /**
     * Feedback Type
     */
    public enum FeedbackType {
        EXPLICIT_RATING,
        IMPLICIT_CLICK,
        POSITIVE,
        NEGATIVE,
        NEUTRAL
    }

    /**
     * Recommendation Algorithm
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationAlgorithm {
        private String algorithmId;
        private String algorithmName;
        private AlgorithmType algorithmType;
        private Boolean enabled;
        private Double weight;
        private Map<String, Object> parameters;
        private Integer neighbors;
        private Integer factors;
        private Double learningRate;
        private Integer epochs;
        private Double regularization;
        private Long totalRecommendations;
        private Double accuracy;
        private Double precision;
        private Double recall;
        private Double f1Score;
        private LocalDateTime lastRunAt;
        private Map<String, Object> metadata;
    }

    /**
     * User Profile
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfile {
        private String profileId;
        private String userId;
        private String userName;
        private String userType;
        private List<String> preferences;
        private Map<String, Double> categoryPreferences;
        private Map<String, Double> featureWeights;
        private List<String> viewedItems;
        private List<String> favoriteItems;
        private List<String> ignoredItems;
        private Map<String, Integer> interactionCounts;
        private Double engagementScore;
        private String personaType;
        private LocalDateTime firstInteractionAt;
        private LocalDateTime lastInteractionAt;
        private Long totalInteractions;
        private Map<String, Object> demographics;
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
        private String userId;
        private String itemId;
        private String itemType;
        private String itemTitle;
        private RecommendationStatus status;
        private Double score;
        private Double confidence;
        private String algorithm;
        private List<String> reasons;
        private String explanation;
        private Integer rank;
        private String context;
        private Map<String, Object> itemFeatures;
        private LocalDateTime generatedAt;
        private LocalDateTime deliveredAt;
        private LocalDateTime expiresAt;
        private Boolean accepted;
        private Map<String, Object> metadata;
    }

    /**
     * User Interaction
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInteraction {
        private String interactionId;
        private String userId;
        private String itemId;
        private InteractionType interactionType;
        private Double rating;
        private Integer durationSeconds;
        private String context;
        private String deviceType;
        private String location;
        private LocalDateTime timestamp;
        private Map<String, Object> metadata;
    }

    /**
     * Recommendable Item
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendableItem {
        private String itemId;
        private String itemType;
        private String title;
        private String description;
        private List<String> categories;
        private List<String> tags;
        private Map<String, Object> features;
        private Double popularityScore;
        private Double qualityScore;
        private Long totalViews;
        private Long totalDownloads;
        private Double averageRating;
        private Integer ratingCount;
        private LocalDateTime createdAt;
        private LocalDateTime lastAccessedAt;
        private Map<String, Object> metadata;
    }

    /**
     * A/B Test
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ABTest {
        private String testId;
        private String testName;
        private String description;
        private Boolean active;
        private String variantA;
        private String variantB;
        private Double trafficSplit;
        private Long variantAViews;
        private Long variantBViews;
        private Long variantAConversions;
        private Long variantBConversions;
        private Double variantAConversionRate;
        private Double variantBConversionRate;
        private String winner;
        private Double confidenceLevel;
        private LocalDateTime startedAt;
        private LocalDateTime endedAt;
        private Map<String, Object> metadata;
    }

    /**
     * Personalization Rule
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonalizationRule {
        private String ruleId;
        private String ruleName;
        private Integer priority;
        private Boolean enabled;
        private Map<String, Object> conditions;
        private Map<String, Object> actions;
        private String targetAudience;
        private Long timesApplied;
        private Double effectiveness;
        private LocalDateTime createdAt;
        private Map<String, Object> metadata;
    }

    /**
     * Recommendation Feedback
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationFeedback {
        private String feedbackId;
        private String recommendationId;
        private String userId;
        private FeedbackType feedbackType;
        private Double rating;
        private Boolean helpful;
        private String comment;
        private List<String> reportedIssues;
        private LocalDateTime timestamp;
        private Map<String, Object> metadata;
    }

    /**
     * Recommendation Metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationMetrics {
        private Long totalProfiles;
        private Long activeProfiles;
        private Long totalRecommendations;
        private Long acceptedRecommendations;
        private Double acceptanceRate;
        private Double averageScore;
        private Double averageConfidence;
        private Long totalInteractions;
        private Double engagementRate;
        private Double clickThroughRate;
        private Double conversionRate;
        private Double precision;
        private Double recall;
        private Double f1Score;
        private Double ndcg;
        private Double diversity;
        private Double novelty;
        private Double coverage;
        private Long activeABTests;
        private Double averageRating;
        private LocalDateTime measuredAt;
    }

    /**
     * Recommendation Event
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationEvent {
        private String eventId;
        private LocalDateTime timestamp;
        private String eventType;
        private String description;
        private String resourceType;
        private String resourceId;
        private Map<String, Object> details;
    }

    // Helper Methods

    /**
     * Deploy engine
     */
    public void deployEngine() {
        this.status = EngineStatus.INITIALIZING;
        this.isActive = true;
        this.deployedAt = LocalDateTime.now();

        recordEvent("ENGINE_DEPLOYED", "Recommendation engine deployed", "ENGINE",
                engineId != null ? engineId.toString() : null);

        this.status = EngineStatus.READY;
    }

    /**
     * Add algorithm
     */
    public void addAlgorithm(RecommendationAlgorithm algorithm) {
        if (algorithms == null) {
            algorithms = new java.util.ArrayList<>();
        }
        algorithms.add(algorithm);

        if (algorithmRegistry == null) {
            algorithmRegistry = new java.util.HashMap<>();
        }
        algorithmRegistry.put(algorithm.getAlgorithmId(), algorithm);

        recordEvent("ALGORITHM_ADDED", "Algorithm added: " + algorithm.getAlgorithmName(),
                "ALGORITHM", algorithm.getAlgorithmId());
    }

    /**
     * Create user profile
     */
    public void createProfile(UserProfile profile) {
        if (userProfiles == null) {
            userProfiles = new java.util.ArrayList<>();
        }
        userProfiles.add(profile);

        if (profileRegistry == null) {
            profileRegistry = new java.util.HashMap<>();
        }
        profileRegistry.put(profile.getUserId(), profile);

        totalProfiles = (totalProfiles != null ? totalProfiles : 0L) + 1;

        recordEvent("PROFILE_CREATED", "User profile created: " + profile.getUserName(),
                "PROFILE", profile.getProfileId());
    }

    /**
     * Generate recommendation
     */
    public void generateRecommendation(Recommendation recommendation) {
        if (recommendations == null) {
            recommendations = new java.util.ArrayList<>();
        }
        recommendations.add(recommendation);

        if (recommendationRegistry == null) {
            recommendationRegistry = new java.util.HashMap<>();
        }
        recommendationRegistry.put(recommendation.getRecommendationId(), recommendation);

        totalRecommendations = (totalRecommendations != null ? totalRecommendations : 0L) + 1;
        lastRecommendationAt = LocalDateTime.now();
    }

    /**
     * Record interaction
     */
    public void recordInteraction(UserInteraction interaction) {
        if (interactions == null) {
            interactions = new java.util.ArrayList<>();
        }
        interactions.add(interaction);

        if (interactionRegistry == null) {
            interactionRegistry = new java.util.HashMap<>();
        }
        interactionRegistry.put(interaction.getInteractionId(), interaction);

        totalInteractions = (totalInteractions != null ? totalInteractions : 0L) + 1;

        // Update user profile
        UserProfile profile = profileRegistry != null ? profileRegistry.get(interaction.getUserId()) : null;
        if (profile != null) {
            profile.setLastInteractionAt(interaction.getTimestamp());
            profile.setTotalInteractions((profile.getTotalInteractions() != null ?
                    profile.getTotalInteractions() : 0L) + 1);

            // Track viewed items
            if (interaction.getInteractionType() == InteractionType.VIEW) {
                if (profile.getViewedItems() == null) {
                    profile.setViewedItems(new java.util.ArrayList<>());
                }
                if (!profile.getViewedItems().contains(interaction.getItemId())) {
                    profile.getViewedItems().add(interaction.getItemId());
                }
            }
        }
    }

    /**
     * Add item
     */
    public void addItem(RecommendableItem item) {
        if (items == null) {
            items = new java.util.ArrayList<>();
        }
        items.add(item);

        if (itemRegistry == null) {
            itemRegistry = new java.util.HashMap<>();
        }
        itemRegistry.put(item.getItemId(), item);

        totalItems = (totalItems != null ? totalItems : 0L) + 1;
    }

    /**
     * Accept recommendation
     */
    public void acceptRecommendation(String recommendationId) {
        Recommendation rec = recommendationRegistry != null ? recommendationRegistry.get(recommendationId) : null;
        if (rec != null) {
            rec.setStatus(RecommendationStatus.ACCEPTED);
            rec.setAccepted(true);
            acceptedRecommendations = (acceptedRecommendations != null ? acceptedRecommendations : 0L) + 1;
        }
    }

    /**
     * Reject recommendation
     */
    public void rejectRecommendation(String recommendationId) {
        Recommendation rec = recommendationRegistry != null ? recommendationRegistry.get(recommendationId) : null;
        if (rec != null) {
            rec.setStatus(RecommendationStatus.REJECTED);
            rec.setAccepted(false);
            rejectedRecommendations = (rejectedRecommendations != null ? rejectedRecommendations : 0L) + 1;
        }
    }

    /**
     * Start A/B test
     */
    public void startABTest(ABTest test) {
        if (abTests == null) {
            abTests = new java.util.ArrayList<>();
        }
        abTests.add(test);

        if (abTestRegistry == null) {
            abTestRegistry = new java.util.HashMap<>();
        }
        abTestRegistry.put(test.getTestId(), test);

        if (test.getActive()) {
            activeTests = (activeTests != null ? activeTests : 0L) + 1;
        }

        recordEvent("AB_TEST_STARTED", "A/B test started: " + test.getTestName(),
                "AB_TEST", test.getTestId());
    }

    /**
     * Add personalization rule
     */
    public void addRule(PersonalizationRule rule) {
        if (rules == null) {
            rules = new java.util.ArrayList<>();
        }
        rules.add(rule);

        if (ruleRegistry == null) {
            ruleRegistry = new java.util.HashMap<>();
        }
        ruleRegistry.put(rule.getRuleId(), rule);
    }

    /**
     * Submit feedback
     */
    public void submitFeedback(RecommendationFeedback feedback) {
        if (feedbacks == null) {
            feedbacks = new java.util.ArrayList<>();
        }
        feedbacks.add(feedback);

        if (feedbackRegistry == null) {
            feedbackRegistry = new java.util.HashMap<>();
        }
        feedbackRegistry.put(feedback.getFeedbackId(), feedback);

        // Update average rating
        updateAverageRating();
    }

    /**
     * Update average rating
     */
    private void updateAverageRating() {
        if (feedbacks == null || feedbacks.isEmpty()) {
            averageRating = 0.0;
            return;
        }

        double sum = 0.0;
        int count = 0;

        for (RecommendationFeedback feedback : feedbacks) {
            if (feedback.getRating() != null) {
                sum += feedback.getRating();
                count++;
            }
        }

        averageRating = count > 0 ? sum / count : 0.0;
    }

    /**
     * Get profile by user ID
     */
    public UserProfile getProfile(String userId) {
        return profileRegistry != null ? profileRegistry.get(userId) : null;
    }

    /**
     * Get item by ID
     */
    public RecommendableItem getItem(String itemId) {
        return itemRegistry != null ? itemRegistry.get(itemId) : null;
    }

    /**
     * Get recommendation by ID
     */
    public Recommendation getRecommendation(String recommendationId) {
        return recommendationRegistry != null ? recommendationRegistry.get(recommendationId) : null;
    }

    /**
     * Record event
     */
    public void recordEvent(String eventType, String description, String resourceType, String resourceId) {
        if (events == null) {
            events = new java.util.ArrayList<>();
        }

        RecommendationEvent event = RecommendationEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .eventType(eventType)
                .description(description)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .details(new java.util.HashMap<>())
                .build();

        events.add(event);
        lastEventAt = LocalDateTime.now();
    }

    /**
     * Check if engine is healthy
     */
    public boolean isHealthy() {
        return status == EngineStatus.READY || status == EngineStatus.RECOMMENDING;
    }

    /**
     * Get active profiles
     */
    public List<UserProfile> getActiveProfiles() {
        if (userProfiles == null) {
            return new java.util.ArrayList<>();
        }
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        return userProfiles.stream()
                .filter(p -> p.getLastInteractionAt() != null && p.getLastInteractionAt().isAfter(cutoff))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get recommendations by user
     */
    public List<Recommendation> getRecommendationsByUser(String userId) {
        if (recommendations == null) {
            return new java.util.ArrayList<>();
        }
        return recommendations.stream()
                .filter(r -> userId.equals(r.getUserId()))
                .sorted((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get active A/B tests
     */
    public List<ABTest> getActiveABTests() {
        if (abTests == null) {
            return new java.util.ArrayList<>();
        }
        return abTests.stream()
                .filter(t -> Boolean.TRUE.equals(t.getActive()))
                .collect(java.util.stream.Collectors.toList());
    }
}
