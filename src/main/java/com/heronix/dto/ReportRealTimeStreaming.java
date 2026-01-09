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
 * Report Real-Time Streaming & Processing DTO
 *
 * Manages real-time data streaming, event processing, stream analytics,
 * windowing operations, and continuous computation for educational institutions.
 *
 * Educational Use Cases:
 * - Live student enrollment tracking
 * - Real-time attendance monitoring
 * - Online exam activity streaming
 * - Campus security event processing
 * - IoT sensor data from smart classrooms
 * - Live course registration analytics
 * - Real-time grade submissions
 * - Student behavior pattern detection
 * - Library usage stream analysis
 * - Campus network traffic monitoring
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 130 - Report Real-Time Streaming & Processing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRealTimeStreaming {

    // Basic Information
    private Long streamId;
    private String streamName;
    private String description;
    private StreamStatus status;
    private String organizationId;
    private String streamingPlatform; // KAFKA, KINESIS, PULSAR, FLINK, SPARK_STREAMING

    // Configuration
    private String processingMode; // AT_MOST_ONCE, AT_LEAST_ONCE, EXACTLY_ONCE
    private Integer parallelism;
    private Long checkpointInterval; // in milliseconds
    private String stateBackend; // MEMORY, FILESYSTEM, ROCKSDB
    private Long watermarkInterval; // in milliseconds

    // State
    private Boolean isActive;
    private Boolean isProcessing;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime lastEventAt;
    private String createdBy;

    // Streams
    private List<DataStream> streams;
    private Map<String, DataStream> streamRegistry;

    // Topics/Channels
    private List<StreamTopic> topics;
    private Map<String, StreamTopic> topicRegistry;

    // Processors
    private List<StreamProcessor> processors;
    private Map<String, StreamProcessor> processorRegistry;

    // Windows
    private List<StreamWindow> windows;
    private Map<String, StreamWindow> windowRegistry;

    // Patterns
    private List<EventPattern> patterns;
    private Map<String, EventPattern> patternRegistry;

    // Aggregations
    private List<StreamAggregation> aggregations;
    private Map<String, StreamAggregation> aggregationRegistry;

    // Sources
    private List<StreamSource> sources;
    private Map<String, StreamSource> sourceRegistry;

    // Sinks
    private List<StreamSink> sinks;
    private Map<String, StreamSink> sinkRegistry;

    // Events
    private List<StreamEvent> events;

    // Metrics
    private Long totalEvents;
    private Long processedEvents;
    private Long failedEvents;
    private Double eventsPerSecond;
    private Double averageLatency;
    private Long backpressure;
    private Long totalStreams;
    private Long activeStreams;

    // State
    private List<StreamingEvent> streamingEvents;

    /**
     * Stream status enumeration
     */
    public enum StreamStatus {
        INITIALIZING,
        CONFIGURING,
        STARTING,
        RUNNING,
        PROCESSING,
        PAUSED,
        BACKPRESSURE,
        RECOVERING,
        STOPPED,
        FAILED
    }

    /**
     * Event type enumeration
     */
    public enum EventType {
        STUDENT_ENROLLMENT,
        ATTENDANCE_CHECK_IN,
        EXAM_ACTIVITY,
        GRADE_SUBMISSION,
        LIBRARY_ACCESS,
        SECURITY_ALERT,
        IOT_SENSOR_DATA,
        NETWORK_TRAFFIC,
        CUSTOM
    }

    /**
     * Window type enumeration
     */
    public enum WindowType {
        TUMBLING,      // Fixed, non-overlapping windows
        SLIDING,       // Overlapping windows
        SESSION,       // Dynamic windows based on inactivity gap
        GLOBAL,        // Single window for entire stream
        COUNT          // Windows based on event count
    }

    /**
     * Data stream structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataStream {
        private String streamId;
        private String streamName;
        private String topicName;
        private String dataFormat; // JSON, AVRO, PROTOBUF, CSV
        private String schema;
        private Integer partitions;
        private Integer replicationFactor;
        private Long eventCount;
        private Long bytesProcessed;
        private Double throughput; // events per second
        private LocalDateTime createdAt;
        private LocalDateTime lastEventAt;
        private Map<String, Object> metadata;
    }

    /**
     * Stream topic structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreamTopic {
        private String topicId;
        private String topicName;
        private String description;
        private Integer partitions;
        private Integer replicationFactor;
        private Long retentionMs;
        private Long retentionBytes;
        private String compressionType; // NONE, GZIP, SNAPPY, LZ4, ZSTD
        private Long messageCount;
        private Long totalBytes;
        private List<String> consumers;
        private List<String> producers;
        private LocalDateTime createdAt;
        private Map<String, Object> config;
    }

    /**
     * Stream processor structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreamProcessor {
        private String processorId;
        private String processorName;
        private String processorType; // MAP, FILTER, FLATMAP, REDUCE, JOIN, AGGREGATE
        private String inputStream;
        private String outputStream;
        private String function;
        private Map<String, Object> parameters;
        private Long eventsProcessed;
        private Long eventsFailed;
        private Double processingTime; // average in milliseconds
        private LocalDateTime createdAt;
        private LocalDateTime lastProcessedAt;
    }

    /**
     * Stream window structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreamWindow {
        private String windowId;
        private String windowName;
        private WindowType windowType;
        private Long windowSize; // in milliseconds for time windows, count for count windows
        private Long windowSlide; // for sliding windows
        private Long sessionGap; // for session windows
        private String streamId;
        private Long windowsCreated;
        private Long eventsInWindow;
        private LocalDateTime createdAt;
        private Map<String, Object> metadata;
    }

    /**
     * Event pattern structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventPattern {
        private String patternId;
        private String patternName;
        private String description;
        private List<String> eventSequence; // Pattern definition
        private Long timeConstraint; // Maximum time between events
        private String condition;
        private Long matchCount;
        private LocalDateTime createdAt;
        private LocalDateTime lastMatchedAt;
        private Map<String, Object> actions;
    }

    /**
     * Stream aggregation structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreamAggregation {
        private String aggregationId;
        private String aggregationName;
        private String aggregationType; // SUM, COUNT, AVG, MIN, MAX, DISTINCT_COUNT
        private String streamId;
        private String groupByField;
        private String aggregateField;
        private Long windowSize;
        private Object currentValue;
        private Long recordCount;
        private LocalDateTime createdAt;
        private LocalDateTime lastUpdatedAt;
        private Map<String, Object> metadata;
    }

    /**
     * Stream source structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreamSource {
        private String sourceId;
        private String sourceName;
        private String sourceType; // KAFKA, DATABASE, FILE, HTTP, WEBSOCKET, IOT
        private String connectionString;
        private Map<String, String> properties;
        private Long eventsRead;
        private Long bytesRead;
        private Boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime lastReadAt;
    }

    /**
     * Stream sink structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreamSink {
        private String sinkId;
        private String sinkName;
        private String sinkType; // KAFKA, DATABASE, FILE, HTTP, ELASTICSEARCH, S3
        private String connectionString;
        private Map<String, String> properties;
        private Long eventsWritten;
        private Long bytesWritten;
        private Boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime lastWrittenAt;
    }

    /**
     * Stream event structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreamEvent {
        private String eventId;
        private String streamId;
        private EventType eventType;
        private String eventKey;
        private Map<String, Object> eventData;
        private Long timestamp;
        private Long eventTime; // Event time (when event occurred)
        private Long processingTime; // Processing time (when event was processed)
        private Integer partition;
        private Long offset;
        private Map<String, String> headers;
    }

    /**
     * Streaming event structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreamingEvent {
        private String eventId;
        private String eventType;
        private String description;
        private String targetType;
        private String targetId;
        private LocalDateTime timestamp;
        private String triggeredBy;
        private Map<String, Object> eventData;
    }

    // Helper methods

    /**
     * Start streaming
     */
    public void startStreaming() {
        this.status = StreamStatus.RUNNING;
        this.isActive = true;
        this.isProcessing = true;
        this.startedAt = LocalDateTime.now();

        recordEvent("STREAMING_STARTED", "Real-time streaming started", "STREAM",
                streamId != null ? streamId.toString() : null);
    }

    /**
     * Add stream
     */
    public void addStream(DataStream stream) {
        if (streams == null) {
            streams = new ArrayList<>();
        }
        streams.add(stream);

        if (streamRegistry == null) {
            streamRegistry = new HashMap<>();
        }
        streamRegistry.put(stream.getStreamId(), stream);

        totalStreams = (totalStreams != null ? totalStreams : 0L) + 1;
        activeStreams = (activeStreams != null ? activeStreams : 0L) + 1;

        recordEvent("STREAM_ADDED", "Data stream added", "STREAM", stream.getStreamId());
    }

    /**
     * Add topic
     */
    public void addTopic(StreamTopic topic) {
        if (topics == null) {
            topics = new ArrayList<>();
        }
        topics.add(topic);

        if (topicRegistry == null) {
            topicRegistry = new HashMap<>();
        }
        topicRegistry.put(topic.getTopicId(), topic);

        recordEvent("TOPIC_CREATED", "Stream topic created", "TOPIC", topic.getTopicId());
    }

    /**
     * Add processor
     */
    public void addProcessor(StreamProcessor processor) {
        if (processors == null) {
            processors = new ArrayList<>();
        }
        processors.add(processor);

        if (processorRegistry == null) {
            processorRegistry = new HashMap<>();
        }
        processorRegistry.put(processor.getProcessorId(), processor);

        recordEvent("PROCESSOR_ADDED", "Stream processor added", "PROCESSOR", processor.getProcessorId());
    }

    /**
     * Add window
     */
    public void addWindow(StreamWindow window) {
        if (windows == null) {
            windows = new ArrayList<>();
        }
        windows.add(window);

        if (windowRegistry == null) {
            windowRegistry = new HashMap<>();
        }
        windowRegistry.put(window.getWindowId(), window);

        recordEvent("WINDOW_CREATED", "Stream window created", "WINDOW", window.getWindowId());
    }

    /**
     * Add pattern
     */
    public void addPattern(EventPattern pattern) {
        if (patterns == null) {
            patterns = new ArrayList<>();
        }
        patterns.add(pattern);

        if (patternRegistry == null) {
            patternRegistry = new HashMap<>();
        }
        patternRegistry.put(pattern.getPatternId(), pattern);

        recordEvent("PATTERN_CREATED", "Event pattern created", "PATTERN", pattern.getPatternId());
    }

    /**
     * Add aggregation
     */
    public void addAggregation(StreamAggregation aggregation) {
        if (aggregations == null) {
            aggregations = new ArrayList<>();
        }
        aggregations.add(aggregation);

        if (aggregationRegistry == null) {
            aggregationRegistry = new HashMap<>();
        }
        aggregationRegistry.put(aggregation.getAggregationId(), aggregation);

        recordEvent("AGGREGATION_CREATED", "Stream aggregation created", "AGGREGATION",
                aggregation.getAggregationId());
    }

    /**
     * Add source
     */
    public void addSource(StreamSource source) {
        if (sources == null) {
            sources = new ArrayList<>();
        }
        sources.add(source);

        if (sourceRegistry == null) {
            sourceRegistry = new HashMap<>();
        }
        sourceRegistry.put(source.getSourceId(), source);

        recordEvent("SOURCE_ADDED", "Stream source added", "SOURCE", source.getSourceId());
    }

    /**
     * Add sink
     */
    public void addSink(StreamSink sink) {
        if (sinks == null) {
            sinks = new ArrayList<>();
        }
        sinks.add(sink);

        if (sinkRegistry == null) {
            sinkRegistry = new HashMap<>();
        }
        sinkRegistry.put(sink.getSinkId(), sink);

        recordEvent("SINK_ADDED", "Stream sink added", "SINK", sink.getSinkId());
    }

    /**
     * Process event
     */
    public void processEvent(StreamEvent event, boolean success) {
        if (events == null) {
            events = new ArrayList<>();
        }
        events.add(event);

        totalEvents = (totalEvents != null ? totalEvents : 0L) + 1;

        if (success) {
            processedEvents = (processedEvents != null ? processedEvents : 0L) + 1;
        } else {
            failedEvents = (failedEvents != null ? failedEvents : 0L) + 1;
        }

        this.lastEventAt = LocalDateTime.now();

        // Update stream metrics
        if (streamRegistry != null && event.getStreamId() != null) {
            DataStream stream = streamRegistry.get(event.getStreamId());
            if (stream != null) {
                stream.setEventCount((stream.getEventCount() != null ? stream.getEventCount() : 0L) + 1);
                stream.setLastEventAt(LocalDateTime.now());
            }
        }

        // Calculate events per second
        calculateEventsPerSecond();
    }

    /**
     * Calculate events per second
     */
    private void calculateEventsPerSecond() {
        if (totalEvents != null && totalEvents > 0 && startedAt != null) {
            long secondsSinceStart = java.time.Duration.between(startedAt, LocalDateTime.now()).toSeconds();
            if (secondsSinceStart > 0) {
                this.eventsPerSecond = totalEvents.doubleValue() / secondsSinceStart;
            }
        }
    }

    /**
     * Update processor stats
     */
    public void updateProcessorStats(String processorId, boolean success, double processingTime) {
        if (processorRegistry != null) {
            StreamProcessor processor = processorRegistry.get(processorId);
            if (processor != null) {
                if (success) {
                    processor.setEventsProcessed(
                        (processor.getEventsProcessed() != null ? processor.getEventsProcessed() : 0L) + 1
                    );
                } else {
                    processor.setEventsFailed(
                        (processor.getEventsFailed() != null ? processor.getEventsFailed() : 0L) + 1
                    );
                }

                // Update average processing time
                if (processor.getProcessingTime() == null) {
                    processor.setProcessingTime(processingTime);
                } else {
                    long totalProcessed = processor.getEventsProcessed() != null ? processor.getEventsProcessed() : 0L;
                    if (totalProcessed > 0) {
                        processor.setProcessingTime(
                            (processor.getProcessingTime() * (totalProcessed - 1) + processingTime) / totalProcessed
                        );
                    }
                }

                processor.setLastProcessedAt(LocalDateTime.now());
            }
        }
    }

    /**
     * Record streaming event
     */
    private void recordEvent(String eventType, String description, String targetType, String targetId) {
        if (streamingEvents == null) {
            streamingEvents = new ArrayList<>();
        }

        StreamingEvent event = StreamingEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType(eventType)
                .description(description)
                .targetType(targetType)
                .targetId(targetId)
                .timestamp(LocalDateTime.now())
                .triggeredBy(createdBy)
                .build();

        streamingEvents.add(event);
    }
}
