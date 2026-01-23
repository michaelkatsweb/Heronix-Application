package com.heronix.controller;

import com.heronix.dto.ReportNaturalLanguage;
import com.heronix.service.ReportNaturalLanguageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Natural Language Processing & Understanding API Controller
 *
 * REST API endpoints for NLP models, text analysis, sentiment analysis, and language understanding.
 *
 * Endpoints:
 * - POST /api/natural-language - Create NLP model
 * - GET /api/natural-language/{id} - Get NLP model
 * - POST /api/natural-language/{id}/deploy - Deploy model
 * - POST /api/natural-language/{id}/analyze - Analyze text
 * - POST /api/natural-language/{id}/sentiment - Analyze sentiment
 * - POST /api/natural-language/{id}/entities - Recognize entities
 * - POST /api/natural-language/{id}/classify - Classify text
 * - POST /api/natural-language/{id}/question - Answer question
 * - POST /api/natural-language/{id}/summarize - Summarize text
 * - POST /api/natural-language/{id}/translate - Translate text
 * - POST /api/natural-language/{id}/topic - Model topic
 * - POST /api/natural-language/{id}/similarity - Analyze similarity
 * - DELETE /api/natural-language/{id} - Delete model
 * - GET /api/natural-language/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 115 - Report Natural Language Processing & Understanding
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/natural-language")
@RequiredArgsConstructor
@Slf4j
public class ReportNaturalLanguageApiController {

    private final ReportNaturalLanguageService nlpService;

    /**
     * Create NLP model
     */
    @PostMapping
    public ResponseEntity<ReportNaturalLanguage> createNLPModel(@RequestBody ReportNaturalLanguage nlp) {
        log.info("POST /api/natural-language - Creating NLP model: {}", nlp.getNlpName());

        try {
            ReportNaturalLanguage created = nlpService.createNLPModel(nlp);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating NLP model", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get NLP model
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportNaturalLanguage> getNLPModel(@PathVariable Long id) {
        log.info("GET /api/natural-language/{}", id);

        try {
            return nlpService.getNLPModel(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching NLP model: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Deploy NLP model
     */
    @PostMapping("/{id}/deploy")
    public ResponseEntity<Map<String, Object>> deployModel(@PathVariable Long id) {
        log.info("POST /api/natural-language/{}/deploy", id);

        try {
            nlpService.deployModel(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "NLP model deployed successfully");
            response.put("nlpId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("NLP model not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error deploying NLP model: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Analyze text
     */
    @PostMapping("/{id}/analyze")
    public ResponseEntity<ReportNaturalLanguage.TextAnalysis> analyzeText(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/natural-language/{}/analyze", id);

        try {
            String text = request.get("text");
            String analysisTypeStr = request.get("analysisType");

            ReportNaturalLanguage.AnalysisType analysisType =
                    ReportNaturalLanguage.AnalysisType.valueOf(analysisTypeStr);

            ReportNaturalLanguage.TextAnalysis analysis = nlpService.analyzeText(id, text, analysisType);

            return ResponseEntity.ok(analysis);

        } catch (IllegalArgumentException e) {
            log.error("NLP model not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error analyzing text: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Analyze sentiment
     */
    @PostMapping("/{id}/sentiment")
    public ResponseEntity<ReportNaturalLanguage.SentimentAnalysis> analyzeSentiment(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/natural-language/{}/sentiment", id);

        try {
            String text = request.get("text");
            String context = request.get("context");

            ReportNaturalLanguage.SentimentAnalysis sentiment = nlpService.analyzeSentiment(id, text, context);

            return ResponseEntity.ok(sentiment);

        } catch (IllegalArgumentException e) {
            log.error("NLP model not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error analyzing sentiment: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Recognize entities
     */
    @PostMapping("/{id}/entities")
    public ResponseEntity<ReportNaturalLanguage.EntityRecognition> recognizeEntities(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/natural-language/{}/entities", id);

        try {
            String text = request.get("text");

            ReportNaturalLanguage.EntityRecognition recognition = nlpService.recognizeEntities(id, text);

            return ResponseEntity.ok(recognition);

        } catch (IllegalArgumentException e) {
            log.error("NLP model not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error recognizing entities: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Classify text
     */
    @PostMapping("/{id}/classify")
    public ResponseEntity<ReportNaturalLanguage.TextClassification> classifyText(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/natural-language/{}/classify", id);

        try {
            String text = request.get("text");
            String classifier = request.get("classifier");

            ReportNaturalLanguage.TextClassification classification = nlpService.classifyText(id, text, classifier);

            return ResponseEntity.ok(classification);

        } catch (IllegalArgumentException e) {
            log.error("NLP model not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error classifying text: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Answer question
     */
    @PostMapping("/{id}/question")
    public ResponseEntity<ReportNaturalLanguage.QuestionAnswer> answerQuestion(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/natural-language/{}/question", id);

        try {
            String question = request.get("question");
            String context = request.get("context");

            ReportNaturalLanguage.QuestionAnswer qa = nlpService.answerQuestion(id, question, context);

            return ResponseEntity.ok(qa);

        } catch (IllegalArgumentException e) {
            log.error("NLP model not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error answering question: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Summarize text
     */
    @PostMapping("/{id}/summarize")
    public ResponseEntity<ReportNaturalLanguage.TextSummary> summarizeText(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/natural-language/{}/summarize", id);

        try {
            String text = request.get("text");
            String summaryType = request.get("summaryType");

            ReportNaturalLanguage.TextSummary summary = nlpService.summarizeText(id, text, summaryType);

            return ResponseEntity.ok(summary);

        } catch (IllegalArgumentException e) {
            log.error("NLP model not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error summarizing text: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Translate text
     */
    @PostMapping("/{id}/translate")
    public ResponseEntity<ReportNaturalLanguage.Translation> translateText(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/natural-language/{}/translate", id);

        try {
            String sourceText = request.get("sourceText");
            String sourceLanguage = request.get("sourceLanguage");
            String targetLanguage = request.get("targetLanguage");

            ReportNaturalLanguage.Translation translation = nlpService.translateText(
                    id, sourceText, sourceLanguage, targetLanguage
            );

            return ResponseEntity.ok(translation);

        } catch (IllegalArgumentException e) {
            log.error("NLP model not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error translating text: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Model topic
     */
    @PostMapping("/{id}/topic")
    public ResponseEntity<ReportNaturalLanguage.TopicModel> modelTopic(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/natural-language/{}/topic", id);

        try {
            String topicName = (String) request.get("topicName");
            @SuppressWarnings("unchecked")
            List<String> keywords = (List<String>) request.get("keywords");
            @SuppressWarnings("unchecked")
            Map<String, Double> wordDistribution = (Map<String, Double>) request.get("wordDistribution");

            ReportNaturalLanguage.TopicModel topic = nlpService.modelTopic(
                    id, topicName, keywords, wordDistribution != null ? wordDistribution : new HashMap<>()
            );

            return ResponseEntity.ok(topic);

        } catch (IllegalArgumentException e) {
            log.error("NLP model not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error modeling topic: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Analyze similarity
     */
    @PostMapping("/{id}/similarity")
    public ResponseEntity<ReportNaturalLanguage.SimilarityAnalysis> analyzeSimilarity(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/natural-language/{}/similarity", id);

        try {
            String text1 = (String) request.get("text1");
            String text2 = (String) request.get("text2");
            String method = (String) request.get("method");
            Double plagiarismThreshold = request.get("plagiarismThreshold") != null ?
                    ((Number) request.get("plagiarismThreshold")).doubleValue() : 0.7;

            ReportNaturalLanguage.SimilarityAnalysis similarity = nlpService.analyzeSimilarity(
                    id, text1, text2, method, plagiarismThreshold
            );

            return ResponseEntity.ok(similarity);

        } catch (IllegalArgumentException e) {
            log.error("NLP model not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error analyzing similarity: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete NLP model
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteNLPModel(@PathVariable Long id) {
        log.info("DELETE /api/natural-language/{}", id);

        try {
            nlpService.deleteNLPModel(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "NLP model deleted");
            response.put("nlpId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting NLP model: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/natural-language/stats");

        try {
            Map<String, Object> stats = nlpService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching NLP statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
