package com.heronix.controller;

import com.heronix.dto.ReportNLP;
import com.heronix.service.ReportNLPService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report NLP API Controller
 *
 * REST API endpoints for natural language processing.
 *
 * Endpoints:
 * - POST /api/nlp - Create NLP engine
 * - GET /api/nlp/{id} - Get NLP engine
 * - POST /api/nlp/{id}/start - Start engine
 * - POST /api/nlp/{id}/stop - Stop engine
 * - POST /api/nlp/{id}/model - Load NLP model
 * - POST /api/nlp/{id}/analyze - Analyze text
 * - POST /api/nlp/{id}/query - Process natural language query
 * - POST /api/nlp/{id}/entities - Extract entities
 * - POST /api/nlp/{id}/sentiment - Analyze sentiment
 * - POST /api/nlp/{id}/translate - Translate text
 * - POST /api/nlp/{id}/summarize - Summarize text
 * - POST /api/nlp/{id}/classify - Classify text
 * - PUT /api/nlp/{id}/metrics - Update metrics
 * - DELETE /api/nlp/{id} - Delete engine
 * - GET /api/nlp/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 105 - Report Natural Language Processing & Understanding
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/nlp")
@RequiredArgsConstructor
@Slf4j
public class ReportNLPApiController {

    private final ReportNLPService nlpService;

    /**
     * Create NLP engine
     */
    @PostMapping
    public ResponseEntity<ReportNLP> createEngine(@RequestBody ReportNLP engine) {
        log.info("POST /api/nlp - Creating NLP engine: {}", engine.getEngineName());

        try {
            ReportNLP created = nlpService.createEngine(engine);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating NLP engine", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get NLP engine
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportNLP> getEngine(@PathVariable Long id) {
        log.info("GET /api/nlp/{}", id);

        try {
            return nlpService.getEngine(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching NLP engine: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Start engine
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<Map<String, Object>> startEngine(@PathVariable Long id) {
        log.info("POST /api/nlp/{}/start", id);

        try {
            nlpService.startEngine(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "NLP engine started");
            response.put("engineId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("NLP engine not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error starting NLP engine: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Stop engine
     */
    @PostMapping("/{id}/stop")
    public ResponseEntity<Map<String, Object>> stopEngine(@PathVariable Long id) {
        log.info("POST /api/nlp/{}/stop", id);

        try {
            nlpService.stopEngine(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "NLP engine stopped");
            response.put("engineId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("NLP engine not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error stopping NLP engine: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Load NLP model
     */
    @PostMapping("/{id}/model")
    public ResponseEntity<ReportNLP.NLPModel> loadModel(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/nlp/{}/model", id);

        try {
            String modelName = request.get("modelName");
            String modelTypeStr = request.get("modelType");
            String language = request.get("language");

            ReportNLP.ModelType modelType = ReportNLP.ModelType.valueOf(modelTypeStr);

            ReportNLP.NLPModel model = nlpService.loadModel(id, modelName, modelType, language);

            return ResponseEntity.ok(model);

        } catch (IllegalArgumentException e) {
            log.error("NLP engine not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error loading model in NLP engine: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Analyze text
     */
    @PostMapping("/{id}/analyze")
    public ResponseEntity<ReportNLP.TextAnalysis> analyzeText(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/nlp/{}/analyze", id);

        try {
            String text = request.get("text");
            String language = request.getOrDefault("language", "en");

            ReportNLP.TextAnalysis analysis = nlpService.analyzeText(id, text, language);

            return ResponseEntity.ok(analysis);

        } catch (IllegalArgumentException e) {
            log.error("NLP engine not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error analyzing text in NLP engine: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Process natural language query
     */
    @PostMapping("/{id}/query")
    public ResponseEntity<ReportNLP.NaturalLanguageQuery> processQuery(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/nlp/{}/query", id);

        try {
            String queryText = request.get("queryText");
            String language = request.getOrDefault("language", "en");

            ReportNLP.NaturalLanguageQuery query = nlpService.processQuery(id, queryText, language);

            return ResponseEntity.ok(query);

        } catch (IllegalArgumentException e) {
            log.error("NLP engine not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error processing query in NLP engine: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Extract entities
     */
    @PostMapping("/{id}/entities")
    public ResponseEntity<Map<String, Object>> extractEntities(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/nlp/{}/entities", id);

        try {
            String text = request.get("text");
            String language = request.getOrDefault("language", "en");

            List<ReportNLP.ExtractedEntity> entities = nlpService.extractEntities(id, text, language);

            Map<String, Object> response = new HashMap<>();
            response.put("engineId", id);
            response.put("entities", entities);
            response.put("count", entities.size());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("NLP engine not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error extracting entities in NLP engine: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Analyze sentiment
     */
    @PostMapping("/{id}/sentiment")
    public ResponseEntity<ReportNLP.SentimentAnalysis> analyzeSentiment(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/nlp/{}/sentiment", id);

        try {
            String text = request.get("text");
            String language = request.getOrDefault("language", "en");

            ReportNLP.SentimentAnalysis sentiment = nlpService.analyzeSentiment(id, text, language);

            return ResponseEntity.ok(sentiment);

        } catch (IllegalArgumentException e) {
            log.error("NLP engine not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error analyzing sentiment in NLP engine: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Translate text
     */
    @PostMapping("/{id}/translate")
    public ResponseEntity<ReportNLP.Translation> translateText(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/nlp/{}/translate", id);

        try {
            String text = request.get("text");
            String targetLanguage = request.get("targetLanguage");

            ReportNLP.Translation translation = nlpService.translateText(id, text, targetLanguage);

            return ResponseEntity.ok(translation);

        } catch (IllegalArgumentException e) {
            log.error("NLP engine not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error translating text in NLP engine: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Summarize text
     */
    @PostMapping("/{id}/summarize")
    public ResponseEntity<ReportNLP.TextSummary> summarizeText(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/nlp/{}/summarize", id);

        try {
            String text = (String) request.get("text");
            Integer maxLength = request.get("maxLength") != null ?
                    ((Number) request.get("maxLength")).intValue() : 100;

            ReportNLP.TextSummary summary = nlpService.summarizeText(id, text, maxLength);

            return ResponseEntity.ok(summary);

        } catch (IllegalArgumentException e) {
            log.error("NLP engine not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error summarizing text in NLP engine: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Classify text
     */
    @PostMapping("/{id}/classify")
    public ResponseEntity<ReportNLP.TextClassification> classifyText(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/nlp/{}/classify", id);

        try {
            String text = request.get("text");

            ReportNLP.TextClassification classification = nlpService.classifyText(id, text);

            return ResponseEntity.ok(classification);

        } catch (IllegalArgumentException e) {
            log.error("NLP engine not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error classifying text in NLP engine: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update metrics
     */
    @PutMapping("/{id}/metrics")
    public ResponseEntity<Map<String, Object>> updateMetrics(@PathVariable Long id) {
        log.info("PUT /api/nlp/{}/metrics", id);

        try {
            nlpService.updateMetrics(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Metrics updated");
            response.put("engineId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("NLP engine not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating metrics for NLP engine: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete engine
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteEngine(@PathVariable Long id) {
        log.info("DELETE /api/nlp/{}", id);

        try {
            nlpService.deleteEngine(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "NLP engine deleted");
            response.put("engineId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting NLP engine: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/nlp/stats");

        try {
            Map<String, Object> stats = nlpService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching NLP statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
