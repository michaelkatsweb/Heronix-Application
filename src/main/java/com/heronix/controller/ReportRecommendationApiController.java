package com.heronix.controller;

import com.heronix.dto.ReportRecommendation;
import com.heronix.service.ReportRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Recommendation Engine API Controller
 *
 * REST API endpoints for recommendation engine and personalization management.
 *
 * Endpoints:
 * - POST /api/recommendation - Create recommendation engine
 * - GET /api/recommendation/{id} - Get recommendation engine
 * - POST /api/recommendation/{id}/deploy - Deploy engine
 * - POST /api/recommendation/{id}/profile - Create user profile
 * - POST /api/recommendation/{id}/item - Add recommendable item
 * - POST /api/recommendation/{id}/interaction - Record user interaction
 * - POST /api/recommendation/{id}/recommend - Generate recommendations
 * - POST /api/recommendation/{id}/accept - Accept recommendation
 * - POST /api/recommendation/{id}/reject - Reject recommendation
 * - POST /api/recommendation/{id}/abtest - Start A/B test
 * - POST /api/recommendation/{id}/feedback - Submit feedback
 * - POST /api/recommendation/{id}/train - Train model
 * - GET /api/recommendation/{id}/recommendations/{userId} - Get user recommendations
 * - PUT /api/recommendation/{id}/metrics - Update metrics
 * - DELETE /api/recommendation/{id} - Delete engine
 * - GET /api/recommendation/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 107 - Report Recommendation Engine & Personalization
 */
@RestController
@RequestMapping("/api/recommendation")
@RequiredArgsConstructor
@Slf4j
public class ReportRecommendationApiController {

    private final ReportRecommendationService recommendationService;

    /**
     * Create recommendation engine
     */
    @PostMapping
    public ResponseEntity<ReportRecommendation> createEngine(@RequestBody ReportRecommendation engine) {
        log.info("POST /api/recommendation - Creating recommendation engine: {}", engine.getEngineName());

        try {
            ReportRecommendation created = recommendationService.createEngine(engine);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating recommendation engine", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get recommendation engine
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportRecommendation> getEngine(@PathVariable Long id) {
        log.info("GET /api/recommendation/{}", id);

        try {
            return recommendationService.getEngine(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching recommendation engine: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Deploy engine
     */
    @PostMapping("/{id}/deploy")
    public ResponseEntity<Map<String, Object>> deployEngine(@PathVariable Long id) {
        log.info("POST /api/recommendation/{}/deploy", id);

        try {
            recommendationService.deployEngine(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Recommendation engine deployed");
            response.put("engineId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Recommendation engine not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error deploying recommendation engine: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create user profile
     */
    @PostMapping("/{id}/profile")
    public ResponseEntity<ReportRecommendation.UserProfile> createProfile(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/recommendation/{}/profile", id);

        try {
            String userId = request.get("userId");
            String userName = request.get("userName");
            String userType = request.get("userType");

            ReportRecommendation.UserProfile profile = recommendationService.createProfile(
                    id, userId, userName, userType
            );

            return ResponseEntity.ok(profile);

        } catch (IllegalArgumentException e) {
            log.error("Recommendation engine not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating user profile in recommendation engine: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add recommendable item
     */
    @PostMapping("/{id}/item")
    public ResponseEntity<ReportRecommendation.RecommendableItem> addItem(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/recommendation/{}/item", id);

        try {
            String itemType = (String) request.get("itemType");
            String title = (String) request.get("title");
            String description = (String) request.get("description");
            @SuppressWarnings("unchecked")
            List<String> categories = (List<String>) request.get("categories");
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) request.get("tags");

            ReportRecommendation.RecommendableItem item = recommendationService.addItem(
                    id, itemType, title, description, categories, tags
            );

            return ResponseEntity.ok(item);

        } catch (IllegalArgumentException e) {
            log.error("Recommendation engine not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding item to recommendation engine: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Record user interaction
     */
    @PostMapping("/{id}/interaction")
    public ResponseEntity<ReportRecommendation.UserInteraction> recordInteraction(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/recommendation/{}/interaction", id);

        try {
            String userId = (String) request.get("userId");
            String itemId = (String) request.get("itemId");
            String interactionTypeStr = (String) request.get("interactionType");
            Double rating = request.get("rating") != null ?
                    ((Number) request.get("rating")).doubleValue() : null;

            ReportRecommendation.InteractionType interactionType =
                    ReportRecommendation.InteractionType.valueOf(interactionTypeStr);

            ReportRecommendation.UserInteraction interaction = recommendationService.recordInteraction(
                    id, userId, itemId, interactionType, rating
            );

            return ResponseEntity.ok(interaction);

        } catch (IllegalArgumentException e) {
            log.error("Recommendation engine not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error recording interaction in recommendation engine: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Generate recommendations
     */
    @PostMapping("/{id}/recommend")
    public ResponseEntity<Map<String, Object>> generateRecommendations(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/recommendation/{}/recommend", id);

        try {
            String userId = (String) request.get("userId");
            Integer count = request.get("count") != null ?
                    ((Number) request.get("count")).intValue() : 10;
            String context = (String) request.getOrDefault("context", "web");

            List<ReportRecommendation.Recommendation> recommendations =
                    recommendationService.generateRecommendations(id, userId, count, context);

            Map<String, Object> response = new HashMap<>();
            response.put("engineId", id);
            response.put("userId", userId);
            response.put("recommendations", recommendations);
            response.put("count", recommendations.size());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Recommendation engine or user not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error generating recommendations in engine: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Accept recommendation
     */
    @PostMapping("/{id}/accept")
    public ResponseEntity<Map<String, Object>> acceptRecommendation(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/recommendation/{}/accept", id);

        try {
            String recommendationId = request.get("recommendationId");

            recommendationService.acceptRecommendation(id, recommendationId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Recommendation accepted");
            response.put("recommendationId", recommendationId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Recommendation engine not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error accepting recommendation in engine: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Reject recommendation
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectRecommendation(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/recommendation/{}/reject", id);

        try {
            String recommendationId = request.get("recommendationId");

            recommendationService.rejectRecommendation(id, recommendationId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Recommendation rejected");
            response.put("recommendationId", recommendationId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Recommendation engine not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error rejecting recommendation in engine: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Start A/B test
     */
    @PostMapping("/{id}/abtest")
    public ResponseEntity<ReportRecommendation.ABTest> startABTest(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/recommendation/{}/abtest", id);

        try {
            String testName = (String) request.get("testName");
            String variantA = (String) request.get("variantA");
            String variantB = (String) request.get("variantB");
            Double trafficSplit = request.get("trafficSplit") != null ?
                    ((Number) request.get("trafficSplit")).doubleValue() : 0.5;

            ReportRecommendation.ABTest test = recommendationService.startABTest(
                    id, testName, variantA, variantB, trafficSplit
            );

            return ResponseEntity.ok(test);

        } catch (IllegalArgumentException e) {
            log.error("Recommendation engine not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error starting A/B test in recommendation engine: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Submit feedback
     */
    @PostMapping("/{id}/feedback")
    public ResponseEntity<ReportRecommendation.RecommendationFeedback> submitFeedback(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/recommendation/{}/feedback", id);

        try {
            String recommendationId = (String) request.get("recommendationId");
            String userId = (String) request.get("userId");
            Double rating = request.get("rating") != null ?
                    ((Number) request.get("rating")).doubleValue() : null;
            Boolean helpful = (Boolean) request.get("helpful");
            String comment = (String) request.get("comment");

            ReportRecommendation.RecommendationFeedback feedback = recommendationService.submitFeedback(
                    id, recommendationId, userId, rating, helpful, comment
            );

            return ResponseEntity.ok(feedback);

        } catch (IllegalArgumentException e) {
            log.error("Recommendation engine not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error submitting feedback in recommendation engine: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Train model
     */
    @PostMapping("/{id}/train")
    public ResponseEntity<Map<String, Object>> trainModel(@PathVariable Long id) {
        log.info("POST /api/recommendation/{}/train", id);

        try {
            recommendationService.trainModel(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Model training completed");
            response.put("engineId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Recommendation engine not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error training model in recommendation engine: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get user recommendations
     */
    @GetMapping("/{id}/recommendations/{userId}")
    public ResponseEntity<Map<String, Object>> getUserRecommendations(
            @PathVariable Long id,
            @PathVariable String userId) {
        log.info("GET /api/recommendation/{}/recommendations/{}", id, userId);

        try {
            return recommendationService.getEngine(id)
                    .map(engine -> {
                        List<ReportRecommendation.Recommendation> recommendations =
                                engine.getRecommendationsByUser(userId);
                        Map<String, Object> response = new HashMap<>();
                        response.put("engineId", id);
                        response.put("userId", userId);
                        response.put("recommendations", recommendations);
                        response.put("count", recommendations.size());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching recommendations for user {} in engine: {}", userId, id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update metrics
     */
    @PutMapping("/{id}/metrics")
    public ResponseEntity<Map<String, Object>> updateMetrics(@PathVariable Long id) {
        log.info("PUT /api/recommendation/{}/metrics", id);

        try {
            recommendationService.updateMetrics(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Metrics updated");
            response.put("engineId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Recommendation engine not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating metrics for recommendation engine: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete engine
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteEngine(@PathVariable Long id) {
        log.info("DELETE /api/recommendation/{}", id);

        try {
            recommendationService.deleteEngine(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Recommendation engine deleted");
            response.put("engineId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting recommendation engine: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/recommendation/stats");

        try {
            Map<String, Object> stats = recommendationService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching recommendation statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
