package com.heronix.controller;

import com.heronix.dto.ReportRealTimeStreaming;
import com.heronix.service.ReportRealTimeStreamingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Real-Time Streaming API Controller
 *
 * REST API endpoints for real-time streaming management, event processing,
 * stream analytics, and continuous computation.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 130 - Report Real-Time Streaming & Processing
 */
@RestController
@RequestMapping("/api/real-time-streaming")
@RequiredArgsConstructor
@Slf4j
public class ReportRealTimeStreamingApiController {

    private final ReportRealTimeStreamingService streamingService;

    /**
     * Create streaming system
     * POST /api/real-time-streaming
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createStreaming(
            @RequestBody ReportRealTimeStreaming streaming) {
        try {
            ReportRealTimeStreaming created = streamingService.createStreaming(streaming);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Streaming system created successfully");
            response.put("streamId", created.getStreamId());
            response.put("streamName", created.getStreamName());
            response.put("streamingPlatform", created.getStreamingPlatform());
            response.put("status", created.getStatus());
            response.put("createdAt", created.getCreatedAt());

            log.info("Streaming system created via API: {}", created.getStreamId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Error creating streaming system: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create streaming system: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get streaming system
     * GET /api/real-time-streaming/{streamId}
     */
    @GetMapping("/{streamId}")
    public ResponseEntity<Map<String, Object>> getStreaming(@PathVariable Long streamId) {
        try {
            return streamingService.getStreaming(streamId)
                    .map(streaming -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", true);
                        response.put("streaming", streaming);
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("success", false);
                        errorResponse.put("message", "Streaming system not found");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
                    });

        } catch (Exception e) {
            log.error("Error retrieving streaming system {}: {}", streamId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve streaming system: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Start streaming
     * POST /api/real-time-streaming/{streamId}/start
     */
    @PostMapping("/{streamId}/start")
    public ResponseEntity<Map<String, Object>> startStreaming(@PathVariable Long streamId) {
        try {
            streamingService.startStreaming(streamId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Streaming started successfully");
            response.put("streamId", streamId);
            response.put("startedAt", LocalDateTime.now());

            log.info("Streaming started via API: {}", streamId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error starting streaming {}: {}", streamId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to start streaming: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Add data stream
     * POST /api/real-time-streaming/{streamId}/stream
     */
    @PostMapping("/{streamId}/stream")
    public ResponseEntity<Map<String, Object>> addDataStream(
            @PathVariable Long streamId,
            @RequestBody Map<String, Object> streamRequest) {
        try {
            String streamName = (String) streamRequest.get("streamName");
            String topicName = (String) streamRequest.get("topicName");
            String dataFormat = (String) streamRequest.get("dataFormat");
            Integer partitions = (Integer) streamRequest.get("partitions");

            ReportRealTimeStreaming.DataStream dataStream = streamingService.addDataStream(
                    streamId, streamName, topicName, dataFormat, partitions);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Data stream added successfully");
            response.put("dataStreamId", dataStream.getStreamId());
            response.put("streamName", dataStream.getStreamName());
            response.put("topicName", dataStream.getTopicName());
            response.put("partitions", dataStream.getPartitions());
            response.put("createdAt", dataStream.getCreatedAt());

            log.info("Data stream added via API: {} (streaming: {})", dataStream.getStreamId(), streamId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error adding data stream for streaming {}: {}", streamId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to add data stream: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Create topic
     * POST /api/real-time-streaming/{streamId}/topic
     */
    @PostMapping("/{streamId}/topic")
    public ResponseEntity<Map<String, Object>> createTopic(
            @PathVariable Long streamId,
            @RequestBody Map<String, Object> topicRequest) {
        try {
            String topicName = (String) topicRequest.get("topicName");
            String description = (String) topicRequest.get("description");
            Integer partitions = (Integer) topicRequest.get("partitions");
            Long retentionMs = topicRequest.get("retentionMs") != null ?
                    ((Number) topicRequest.get("retentionMs")).longValue() : null;

            ReportRealTimeStreaming.StreamTopic topic = streamingService.createTopic(
                    streamId, topicName, description, partitions, retentionMs);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Topic created successfully");
            response.put("topicId", topic.getTopicId());
            response.put("topicName", topic.getTopicName());
            response.put("partitions", topic.getPartitions());
            response.put("retentionMs", topic.getRetentionMs());
            response.put("createdAt", topic.getCreatedAt());

            log.info("Topic created via API: {} (streaming: {})", topic.getTopicId(), streamId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error creating topic for streaming {}: {}", streamId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create topic: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Add processor
     * POST /api/real-time-streaming/{streamId}/processor
     */
    @PostMapping("/{streamId}/processor")
    public ResponseEntity<Map<String, Object>> addProcessor(
            @PathVariable Long streamId,
            @RequestBody Map<String, Object> processorRequest) {
        try {
            String processorName = (String) processorRequest.get("processorName");
            String processorType = (String) processorRequest.get("processorType");
            String inputStream = (String) processorRequest.get("inputStream");
            String outputStream = (String) processorRequest.get("outputStream");
            String function = (String) processorRequest.get("function");

            ReportRealTimeStreaming.StreamProcessor processor = streamingService.addProcessor(
                    streamId, processorName, processorType, inputStream, outputStream, function);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Processor added successfully");
            response.put("processorId", processor.getProcessorId());
            response.put("processorName", processor.getProcessorName());
            response.put("processorType", processor.getProcessorType());
            response.put("createdAt", processor.getCreatedAt());

            log.info("Processor added via API: {} (streaming: {})", processor.getProcessorId(), streamId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error adding processor for streaming {}: {}", streamId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to add processor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Create window
     * POST /api/real-time-streaming/{streamId}/window
     */
    @PostMapping("/{streamId}/window")
    public ResponseEntity<Map<String, Object>> createWindow(
            @PathVariable Long streamId,
            @RequestBody Map<String, Object> windowRequest) {
        try {
            String windowName = (String) windowRequest.get("windowName");
            String windowTypeStr = (String) windowRequest.get("windowType");
            ReportRealTimeStreaming.WindowType windowType =
                    ReportRealTimeStreaming.WindowType.valueOf(windowTypeStr);
            Long windowSize = ((Number) windowRequest.get("windowSize")).longValue();
            Long windowSlide = windowRequest.get("windowSlide") != null ?
                    ((Number) windowRequest.get("windowSlide")).longValue() : null;
            String targetStreamId = (String) windowRequest.get("targetStreamId");

            ReportRealTimeStreaming.StreamWindow window = streamingService.createWindow(
                    streamId, windowName, windowType, windowSize, windowSlide, targetStreamId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Window created successfully");
            response.put("windowId", window.getWindowId());
            response.put("windowName", window.getWindowName());
            response.put("windowType", window.getWindowType());
            response.put("windowSize", window.getWindowSize());
            response.put("createdAt", window.getCreatedAt());

            log.info("Window created via API: {} (streaming: {})", window.getWindowId(), streamId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error creating window for streaming {}: {}", streamId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create window: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Add event pattern
     * POST /api/real-time-streaming/{streamId}/pattern
     */
    @PostMapping("/{streamId}/pattern")
    public ResponseEntity<Map<String, Object>> addPattern(
            @PathVariable Long streamId,
            @RequestBody Map<String, Object> patternRequest) {
        try {
            String patternName = (String) patternRequest.get("patternName");
            String description = (String) patternRequest.get("description");
            @SuppressWarnings("unchecked")
            List<String> eventSequence = (List<String>) patternRequest.get("eventSequence");
            Long timeConstraint = patternRequest.get("timeConstraint") != null ?
                    ((Number) patternRequest.get("timeConstraint")).longValue() : null;

            ReportRealTimeStreaming.EventPattern pattern = streamingService.addPattern(
                    streamId, patternName, description, eventSequence, timeConstraint);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Event pattern added successfully");
            response.put("patternId", pattern.getPatternId());
            response.put("patternName", pattern.getPatternName());
            response.put("eventSequence", pattern.getEventSequence());
            response.put("createdAt", pattern.getCreatedAt());

            log.info("Event pattern added via API: {} (streaming: {})", pattern.getPatternId(), streamId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error adding event pattern for streaming {}: {}", streamId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to add event pattern: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Create aggregation
     * POST /api/real-time-streaming/{streamId}/aggregation
     */
    @PostMapping("/{streamId}/aggregation")
    public ResponseEntity<Map<String, Object>> createAggregation(
            @PathVariable Long streamId,
            @RequestBody Map<String, Object> aggregationRequest) {
        try {
            String aggregationName = (String) aggregationRequest.get("aggregationName");
            String aggregationType = (String) aggregationRequest.get("aggregationType");
            String targetStreamId = (String) aggregationRequest.get("targetStreamId");
            String groupByField = (String) aggregationRequest.get("groupByField");
            String aggregateField = (String) aggregationRequest.get("aggregateField");
            Long windowSize = aggregationRequest.get("windowSize") != null ?
                    ((Number) aggregationRequest.get("windowSize")).longValue() : null;

            ReportRealTimeStreaming.StreamAggregation aggregation = streamingService.createAggregation(
                    streamId, aggregationName, aggregationType, targetStreamId,
                    groupByField, aggregateField, windowSize);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Aggregation created successfully");
            response.put("aggregationId", aggregation.getAggregationId());
            response.put("aggregationName", aggregation.getAggregationName());
            response.put("aggregationType", aggregation.getAggregationType());
            response.put("createdAt", aggregation.getCreatedAt());

            log.info("Aggregation created via API: {} (streaming: {})",
                     aggregation.getAggregationId(), streamId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error creating aggregation for streaming {}: {}", streamId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create aggregation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Add stream source
     * POST /api/real-time-streaming/{streamId}/source
     */
    @PostMapping("/{streamId}/source")
    public ResponseEntity<Map<String, Object>> addSource(
            @PathVariable Long streamId,
            @RequestBody Map<String, Object> sourceRequest) {
        try {
            String sourceName = (String) sourceRequest.get("sourceName");
            String sourceType = (String) sourceRequest.get("sourceType");
            String connectionString = (String) sourceRequest.get("connectionString");
            @SuppressWarnings("unchecked")
            Map<String, String> properties = (Map<String, String>) sourceRequest.get("properties");

            ReportRealTimeStreaming.StreamSource source = streamingService.addSource(
                    streamId, sourceName, sourceType, connectionString, properties);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Stream source added successfully");
            response.put("sourceId", source.getSourceId());
            response.put("sourceName", source.getSourceName());
            response.put("sourceType", source.getSourceType());
            response.put("createdAt", source.getCreatedAt());

            log.info("Stream source added via API: {} (streaming: {})", source.getSourceId(), streamId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error adding stream source for streaming {}: {}", streamId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to add stream source: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Add stream sink
     * POST /api/real-time-streaming/{streamId}/sink
     */
    @PostMapping("/{streamId}/sink")
    public ResponseEntity<Map<String, Object>> addSink(
            @PathVariable Long streamId,
            @RequestBody Map<String, Object> sinkRequest) {
        try {
            String sinkName = (String) sinkRequest.get("sinkName");
            String sinkType = (String) sinkRequest.get("sinkType");
            String connectionString = (String) sinkRequest.get("connectionString");
            @SuppressWarnings("unchecked")
            Map<String, String> properties = (Map<String, String>) sinkRequest.get("properties");

            ReportRealTimeStreaming.StreamSink sink = streamingService.addSink(
                    streamId, sinkName, sinkType, connectionString, properties);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Stream sink added successfully");
            response.put("sinkId", sink.getSinkId());
            response.put("sinkName", sink.getSinkName());
            response.put("sinkType", sink.getSinkType());
            response.put("createdAt", sink.getCreatedAt());

            log.info("Stream sink added via API: {} (streaming: {})", sink.getSinkId(), streamId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error adding stream sink for streaming {}: {}", streamId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to add stream sink: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Process event
     * POST /api/real-time-streaming/{streamId}/event
     */
    @PostMapping("/{streamId}/event")
    public ResponseEntity<Map<String, Object>> processEvent(
            @PathVariable Long streamId,
            @RequestBody Map<String, Object> eventRequest) {
        try {
            String targetStreamId = (String) eventRequest.get("targetStreamId");
            String eventTypeStr = (String) eventRequest.get("eventType");
            ReportRealTimeStreaming.EventType eventType =
                    ReportRealTimeStreaming.EventType.valueOf(eventTypeStr);
            String eventKey = (String) eventRequest.get("eventKey");
            @SuppressWarnings("unchecked")
            Map<String, Object> eventData = (Map<String, Object>) eventRequest.get("eventData");

            ReportRealTimeStreaming.StreamEvent event = streamingService.processEvent(
                    streamId, targetStreamId, eventType, eventKey, eventData);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Event processed successfully");
            response.put("eventId", event.getEventId());
            response.put("eventType", event.getEventType());
            response.put("timestamp", event.getTimestamp());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error processing event for streaming {}: {}", streamId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to process event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Pause streaming
     * POST /api/real-time-streaming/{streamId}/pause
     */
    @PostMapping("/{streamId}/pause")
    public ResponseEntity<Map<String, Object>> pauseStreaming(@PathVariable Long streamId) {
        try {
            streamingService.pauseStreaming(streamId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Streaming paused successfully");
            response.put("streamId", streamId);
            response.put("timestamp", LocalDateTime.now());

            log.info("Streaming paused via API: {}", streamId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error pausing streaming {}: {}", streamId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to pause streaming: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Resume streaming
     * POST /api/real-time-streaming/{streamId}/resume
     */
    @PostMapping("/{streamId}/resume")
    public ResponseEntity<Map<String, Object>> resumeStreaming(@PathVariable Long streamId) {
        try {
            streamingService.resumeStreaming(streamId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Streaming resumed successfully");
            response.put("streamId", streamId);
            response.put("timestamp", LocalDateTime.now());

            log.info("Streaming resumed via API: {}", streamId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error resuming streaming {}: {}", streamId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to resume streaming: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Stop streaming
     * POST /api/real-time-streaming/{streamId}/stop
     */
    @PostMapping("/{streamId}/stop")
    public ResponseEntity<Map<String, Object>> stopStreaming(@PathVariable Long streamId) {
        try {
            streamingService.stopStreaming(streamId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Streaming stopped successfully");
            response.put("streamId", streamId);
            response.put("timestamp", LocalDateTime.now());

            log.info("Streaming stopped via API: {}", streamId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error stopping streaming {}: {}", streamId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to stop streaming: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Delete streaming system
     * DELETE /api/real-time-streaming/{streamId}
     */
    @DeleteMapping("/{streamId}")
    public ResponseEntity<Map<String, Object>> deleteStreaming(@PathVariable Long streamId) {
        try {
            streamingService.deleteStreaming(streamId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Streaming system deleted successfully");
            response.put("streamId", streamId);

            log.info("Streaming system deleted via API: {}", streamId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting streaming system {}: {}", streamId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to delete streaming system: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get statistics
     * GET /api/real-time-streaming/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> stats = streamingService.getStatistics();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", stats);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving streaming statistics: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
