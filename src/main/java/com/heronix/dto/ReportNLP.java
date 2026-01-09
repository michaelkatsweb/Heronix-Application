package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Natural Language Processing DTO
 *
 * Manages NLP capabilities for intelligent text analysis and understanding.
 *
 * Features:
 * - Natural language queries
 * - Sentiment analysis
 * - Entity extraction
 * - Text classification
 * - Language translation
 * - Text summarization
 * - Named Entity Recognition (NER)
 * - Question answering
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 105 - Report Natural Language Processing & Understanding
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportNLP {

    // Engine Information
    private Long engineId;
    private String engineName;
    private String description;
    private EngineStatus status;
    private Boolean isActive;

    // NLP Platform
    private NLPPlatform platform;
    private String platformVersion;
    private List<String> supportedLanguages;
    private String defaultLanguage;

    // NLP Models
    private List<NLPModel> models;
    private Map<String, NLPModel> modelRegistry;
    private Integer totalModels;
    private Integer loadedModels;

    // Text Analysis
    private List<TextAnalysis> analyses;
    private Map<String, TextAnalysis> analysisRegistry;
    private Long totalAnalyses;

    // Queries
    private List<NaturalLanguageQuery> queries;
    private Map<String, NaturalLanguageQuery> queryRegistry;
    private Long totalQueries;
    private Long successfulQueries;
    private Long failedQueries;

    // Entities
    private List<ExtractedEntity> entities;
    private Map<String, ExtractedEntity> entityRegistry;
    private Long totalEntities;

    // Sentiments
    private List<SentimentAnalysis> sentiments;
    private Map<String, SentimentAnalysis> sentimentRegistry;
    private Long totalSentiments;

    // Translations
    private List<Translation> translations;
    private Map<String, Translation> translationRegistry;
    private Long totalTranslations;

    // Summaries
    private List<TextSummary> summaries;
    private Map<String, TextSummary> summaryRegistry;
    private Long totalSummaries;

    // Classifications
    private List<TextClassification> classifications;
    private Map<String, TextClassification> classificationRegistry;
    private Long totalClassifications;

    // Metrics
    private NLPMetrics metrics;
    private LocalDateTime lastMetricsUpdate;

    // Events
    private List<NLPEvent> events;
    private LocalDateTime lastEventAt;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime stoppedAt;
    private LocalDateTime lastProcessedAt;

    /**
     * Engine Status
     */
    public enum EngineStatus {
        INITIALIZING,
        LOADING_MODELS,
        READY,
        PROCESSING,
        ERROR,
        MAINTENANCE
    }

    /**
     * NLP Platform
     */
    public enum NLPPlatform {
        OPENAI_GPT,
        GOOGLE_CLOUD_NLP,
        AWS_COMPREHEND,
        AZURE_COGNITIVE,
        SPACY,
        STANFORD_NLP,
        HUGGING_FACE,
        CUSTOM
    }

    /**
     * Model Type
     */
    public enum ModelType {
        SENTIMENT_ANALYSIS,
        ENTITY_EXTRACTION,
        TEXT_CLASSIFICATION,
        QUESTION_ANSWERING,
        SUMMARIZATION,
        TRANSLATION,
        TOKENIZATION,
        POS_TAGGING,
        DEPENDENCY_PARSING
    }

    /**
     * Sentiment
     */
    public enum Sentiment {
        VERY_POSITIVE,
        POSITIVE,
        NEUTRAL,
        NEGATIVE,
        VERY_NEGATIVE,
        MIXED
    }

    /**
     * Entity Type
     */
    public enum EntityType {
        PERSON,
        ORGANIZATION,
        LOCATION,
        DATE,
        TIME,
        MONEY,
        PERCENTAGE,
        EMAIL,
        PHONE,
        URL,
        CUSTOM
    }

    /**
     * Query Status
     */
    public enum QueryStatus {
        PARSING,
        UNDERSTANDING,
        EXECUTING,
        COMPLETED,
        FAILED
    }

    /**
     * NLP Model
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NLPModel {
        private String modelId;
        private String modelName;
        private ModelType modelType;
        private String modelPath;
        private String version;
        private Boolean loaded;
        private Long modelSize;
        private String language;
        private Double accuracy;
        private Long totalInferences;
        private Double averageInferenceTimeMs;
        private LocalDateTime loadedAt;
        private LocalDateTime lastUsedAt;
        private Map<String, String> tags;
        private Map<String, Object> metadata;
    }

    /**
     * Text Analysis
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextAnalysis {
        private String analysisId;
        private String text;
        private String language;
        private Integer wordCount;
        private Integer sentenceCount;
        private Integer paragraphCount;
        private Double readabilityScore;
        private List<String> keywords;
        private List<String> topics;
        private Map<String, Integer> wordFrequency;
        private LocalDateTime analyzedAt;
        private Long processingTimeMs;
        private Map<String, Object> metadata;
    }

    /**
     * Natural Language Query
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NaturalLanguageQuery {
        private String queryId;
        private String queryText;
        private String language;
        private QueryStatus status;
        private String intent;
        private Map<String, Object> entities;
        private String generatedSQL;
        private Map<String, Object> parameters;
        private Map<String, Object> result;
        private Double confidence;
        private LocalDateTime submittedAt;
        private LocalDateTime completedAt;
        private Long executionTimeMs;
        private String errorMessage;
        private Map<String, Object> metadata;
    }

    /**
     * Extracted Entity
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtractedEntity {
        private String entityId;
        private String text;
        private EntityType entityType;
        private String value;
        private Integer startPosition;
        private Integer endPosition;
        private Double confidence;
        private String sourceText;
        private String language;
        private LocalDateTime extractedAt;
        private Map<String, Object> metadata;
    }

    /**
     * Sentiment Analysis
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SentimentAnalysis {
        private String sentimentId;
        private String text;
        private Sentiment sentiment;
        private Double positiveScore;
        private Double neutralScore;
        private Double negativeScore;
        private Double mixedScore;
        private Double overallScore;
        private Double confidence;
        private String language;
        private LocalDateTime analyzedAt;
        private Map<String, Object> metadata;
    }

    /**
     * Translation
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Translation {
        private String translationId;
        private String sourceText;
        private String translatedText;
        private String sourceLanguage;
        private String targetLanguage;
        private String detectedLanguage;
        private Double confidence;
        private String translationModel;
        private LocalDateTime translatedAt;
        private Long processingTimeMs;
        private Map<String, Object> metadata;
    }

    /**
     * Text Summary
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextSummary {
        private String summaryId;
        private String originalText;
        private String summaryText;
        private Integer originalLength;
        private Integer summaryLength;
        private Double compressionRatio;
        private String summaryType;
        private Integer maxLength;
        private String language;
        private LocalDateTime summarizedAt;
        private Long processingTimeMs;
        private Map<String, Object> metadata;
    }

    /**
     * Text Classification
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextClassification {
        private String classificationId;
        private String text;
        private String category;
        private Double confidence;
        private Map<String, Double> allCategories;
        private String language;
        private String classificationModel;
        private LocalDateTime classifiedAt;
        private Map<String, Object> metadata;
    }

    /**
     * NLP Metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NLPMetrics {
        private Integer totalModels;
        private Integer loadedModels;
        private Long totalAnalyses;
        private Long totalQueries;
        private Long successfulQueries;
        private Double querySuccessRate;
        private Double averageQueryTimeMs;
        private Long totalEntities;
        private Long totalSentiments;
        private Double averageSentimentScore;
        private Long totalTranslations;
        private Long totalSummaries;
        private Double averageCompressionRatio;
        private Long totalClassifications;
        private Double averageConfidence;
        private LocalDateTime measuredAt;
    }

    /**
     * NLP Event
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NLPEvent {
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
     * Start engine
     */
    public void startEngine() {
        this.status = EngineStatus.INITIALIZING;
        this.isActive = true;
        this.startedAt = LocalDateTime.now();

        recordEvent("ENGINE_STARTED", "NLP engine started", "ENGINE",
                engineId != null ? engineId.toString() : null);

        this.status = EngineStatus.LOADING_MODELS;
    }

    /**
     * Stop engine
     */
    public void stopEngine() {
        this.status = EngineStatus.MAINTENANCE;
        this.isActive = false;
        this.stoppedAt = LocalDateTime.now();

        recordEvent("ENGINE_STOPPED", "NLP engine stopped", "ENGINE",
                engineId != null ? engineId.toString() : null);
    }

    /**
     * Load model
     */
    public void loadModel(NLPModel model) {
        if (models == null) {
            models = new java.util.ArrayList<>();
        }
        models.add(model);

        if (modelRegistry == null) {
            modelRegistry = new java.util.HashMap<>();
        }
        modelRegistry.put(model.getModelId(), model);

        totalModels = (totalModels != null ? totalModels : 0) + 1;
        if (model.getLoaded()) {
            loadedModels = (loadedModels != null ? loadedModels : 0) + 1;
        }

        recordEvent("MODEL_LOADED", "NLP model loaded: " + model.getModelName(),
                "MODEL", model.getModelId());
    }

    /**
     * Record analysis
     */
    public void recordAnalysis(TextAnalysis analysis) {
        if (analyses == null) {
            analyses = new java.util.ArrayList<>();
        }
        analyses.add(analysis);

        if (analysisRegistry == null) {
            analysisRegistry = new java.util.HashMap<>();
        }
        analysisRegistry.put(analysis.getAnalysisId(), analysis);

        totalAnalyses = (totalAnalyses != null ? totalAnalyses : 0L) + 1;
        lastProcessedAt = LocalDateTime.now();
    }

    /**
     * Submit query
     */
    public void submitQuery(NaturalLanguageQuery query) {
        if (queries == null) {
            queries = new java.util.ArrayList<>();
        }
        queries.add(query);

        if (queryRegistry == null) {
            queryRegistry = new java.util.HashMap<>();
        }
        queryRegistry.put(query.getQueryId(), query);

        totalQueries = (totalQueries != null ? totalQueries : 0L) + 1;

        recordEvent("QUERY_SUBMITTED", "NL query submitted: " + query.getQueryText(),
                "QUERY", query.getQueryId());
    }

    /**
     * Complete query
     */
    public void completeQuery(String queryId, boolean success) {
        NaturalLanguageQuery query = queryRegistry != null ? queryRegistry.get(queryId) : null;
        if (query != null) {
            query.setStatus(success ? QueryStatus.COMPLETED : QueryStatus.FAILED);
            query.setCompletedAt(LocalDateTime.now());

            if (query.getSubmittedAt() != null) {
                query.setExecutionTimeMs(
                    java.time.Duration.between(query.getSubmittedAt(), query.getCompletedAt()).toMillis()
                );
            }

            if (success) {
                successfulQueries = (successfulQueries != null ? successfulQueries : 0L) + 1;
            } else {
                failedQueries = (failedQueries != null ? failedQueries : 0L) + 1;
            }
        }
    }

    /**
     * Add entity
     */
    public void addEntity(ExtractedEntity entity) {
        if (entities == null) {
            entities = new java.util.ArrayList<>();
        }
        entities.add(entity);

        if (entityRegistry == null) {
            entityRegistry = new java.util.HashMap<>();
        }
        entityRegistry.put(entity.getEntityId(), entity);

        totalEntities = (totalEntities != null ? totalEntities : 0L) + 1;
    }

    /**
     * Add sentiment
     */
    public void addSentiment(SentimentAnalysis sentiment) {
        if (sentiments == null) {
            sentiments = new java.util.ArrayList<>();
        }
        sentiments.add(sentiment);

        if (sentimentRegistry == null) {
            sentimentRegistry = new java.util.HashMap<>();
        }
        sentimentRegistry.put(sentiment.getSentimentId(), sentiment);

        totalSentiments = (totalSentiments != null ? totalSentiments : 0L) + 1;
    }

    /**
     * Add translation
     */
    public void addTranslation(Translation translation) {
        if (translations == null) {
            translations = new java.util.ArrayList<>();
        }
        translations.add(translation);

        if (translationRegistry == null) {
            translationRegistry = new java.util.HashMap<>();
        }
        translationRegistry.put(translation.getTranslationId(), translation);

        totalTranslations = (totalTranslations != null ? totalTranslations : 0L) + 1;
    }

    /**
     * Add summary
     */
    public void addSummary(TextSummary summary) {
        if (summaries == null) {
            summaries = new java.util.ArrayList<>();
        }
        summaries.add(summary);

        if (summaryRegistry == null) {
            summaryRegistry = new java.util.HashMap<>();
        }
        summaryRegistry.put(summary.getSummaryId(), summary);

        totalSummaries = (totalSummaries != null ? totalSummaries : 0L) + 1;
    }

    /**
     * Add classification
     */
    public void addClassification(TextClassification classification) {
        if (classifications == null) {
            classifications = new java.util.ArrayList<>();
        }
        classifications.add(classification);

        if (classificationRegistry == null) {
            classificationRegistry = new java.util.HashMap<>();
        }
        classificationRegistry.put(classification.getClassificationId(), classification);

        totalClassifications = (totalClassifications != null ? totalClassifications : 0L) + 1;
    }

    /**
     * Get model by ID
     */
    public NLPModel getModel(String modelId) {
        return modelRegistry != null ? modelRegistry.get(modelId) : null;
    }

    /**
     * Record event
     */
    public void recordEvent(String eventType, String description, String resourceType, String resourceId) {
        if (events == null) {
            events = new java.util.ArrayList<>();
        }

        NLPEvent event = NLPEvent.builder()
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
        return status == EngineStatus.READY || status == EngineStatus.PROCESSING;
    }

    /**
     * Get loaded models
     */
    public List<NLPModel> getLoadedModels() {
        if (models == null) {
            return new java.util.ArrayList<>();
        }
        return models.stream()
                .filter(m -> Boolean.TRUE.equals(m.getLoaded()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get successful queries
     */
    public List<NaturalLanguageQuery> getSuccessfulQueries() {
        if (queries == null) {
            return new java.util.ArrayList<>();
        }
        return queries.stream()
                .filter(q -> q.getStatus() == QueryStatus.COMPLETED)
                .collect(java.util.stream.Collectors.toList());
    }
}
