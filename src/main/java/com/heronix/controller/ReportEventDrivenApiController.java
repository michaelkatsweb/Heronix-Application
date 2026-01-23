package com.heronix.controller;

import com.heronix.dto.ReportEventDriven;
import com.heronix.service.ReportEventDrivenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Event-Driven Architecture API Controller
 *
 * REST API endpoints for event-driven architecture management, event sourcing, CQRS,
 * event streams, and asynchronous messaging.
 *
 * Endpoints:
 * - POST /api/event-driven - Create event-driven architecture
 * - GET /api/event-driven/{id} - Get architecture
 * - POST /api/event-driven/{id}/deploy - Deploy architecture
 * - POST /api/event-driven/{id}/stream - Create event stream
 * - POST /api/event-driven/{id}/event - Publish domain event
 * - POST /api/event-driven/{id}/event/{eventId}/process - Process event
 * - POST /api/event-driven/{id}/handler - Register event handler
 * - POST /api/event-driven/{id}/subscription - Create subscription
 * - POST /api/event-driven/{id}/command - Execute command (CQRS)
 * - POST /api/event-driven/{id}/query - Execute query (CQRS)
 * - POST /api/event-driven/{id}/saga - Start saga
 * - POST /api/event-driven/{id}/saga/{sagaId}/complete - Complete saga
 * - POST /api/event-driven/{id}/projection - Create projection
 * - POST /api/event-driven/{id}/dead-letter - Move to dead letter queue
 * - POST /api/event-driven/{id}/replay - Start event replay
 * - POST /api/event-driven/{id}/replay/{replayId}/complete - Complete replay
 * - DELETE /api/event-driven/{id} - Delete architecture
 * - GET /api/event-driven/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 124 - Report Event-Driven Architecture
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/event-driven")
@RequiredArgsConstructor
@Slf4j
public class ReportEventDrivenApiController {

    private final ReportEventDrivenService eventDrivenService;

    /**
     * Create event-driven architecture
     */
    @PostMapping
    public ResponseEntity<ReportEventDriven> createArchitecture(@RequestBody ReportEventDriven architecture) {
        log.info("POST /api/event-driven - Creating event-driven architecture: {}", architecture.getArchitectureName());

        try {
            ReportEventDriven created = eventDrivenService.createArchitecture(architecture);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating event-driven architecture", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get event-driven architecture
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportEventDriven> getArchitecture(@PathVariable Long id) {
        log.info("GET /api/event-driven/{}", id);

        try {
            return eventDrivenService.getArchitecture(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching event-driven architecture: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Deploy architecture
     */
    @PostMapping("/{id}/deploy")
    public ResponseEntity<Map<String, Object>> deployArchitecture(@PathVariable Long id) {
        log.info("POST /api/event-driven/{}/deploy", id);

        try {
            eventDrivenService.deployArchitecture(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Event-driven architecture deployed");
            response.put("architectureId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Architecture not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error deploying architecture: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create event stream
     */
    @PostMapping("/{id}/stream")
    public ResponseEntity<ReportEventDriven.EventStream> createEventStream(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/event-driven/{}/stream", id);

        try {
            String streamName = (String) request.get("streamName");
            String description = (String) request.get("description");
            String topic = (String) request.get("topic");
            Integer partitionCount = request.get("partitionCount") != null ?
                    ((Number) request.get("partitionCount")).intValue() : 3;
            Integer replicationFactor = request.get("replicationFactor") != null ?
                    ((Number) request.get("replicationFactor")).intValue() : 2;
            Long retentionMs = request.get("retentionMs") != null ?
                    ((Number) request.get("retentionMs")).longValue() : 604800000L;

            ReportEventDriven.EventStream stream = eventDrivenService.createEventStream(
                    id, streamName, description, topic, partitionCount, replicationFactor, retentionMs
            );

            return ResponseEntity.ok(stream);

        } catch (IllegalArgumentException e) {
            log.error("Architecture not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating event stream: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Publish domain event
     */
    @PostMapping("/{id}/event")
    public ResponseEntity<ReportEventDriven.DomainEvent> publishEvent(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/event-driven/{}/event", id);

        try {
            String eventType = (String) request.get("eventType");
            String aggregateId = (String) request.get("aggregateId");
            String aggregateType = (String) request.get("aggregateType");
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) request.get("payload");
            String publishedBy = (String) request.get("publishedBy");

            ReportEventDriven.DomainEvent event = eventDrivenService.publishEvent(
                    id, eventType, aggregateId, aggregateType, payload, publishedBy
            );

            return ResponseEntity.ok(event);

        } catch (IllegalArgumentException e) {
            log.error("Architecture not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error publishing event: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Process event
     */
    @PostMapping("/{id}/event/{eventId}/process")
    public ResponseEntity<Map<String, Object>> processEvent(
            @PathVariable Long id,
            @PathVariable String eventId,
            @RequestBody Map<String, Boolean> request) {
        log.info("POST /api/event-driven/{}/event/{}/process", id, eventId);

        try {
            Boolean success = request.get("success");
            eventDrivenService.processEvent(id, eventId, success != null && success);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Event processed");
            response.put("eventId", eventId);
            response.put("success", success);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Architecture not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error processing event: {}", eventId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Register event handler
     */
    @PostMapping("/{id}/handler")
    public ResponseEntity<ReportEventDriven.EventHandler> registerEventHandler(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/event-driven/{}/handler", id);

        try {
            String handlerName = (String) request.get("handlerName");
            String description = (String) request.get("description");
            @SuppressWarnings("unchecked")
            List<String> subscribedEventTypes = (List<String>) request.get("subscribedEventTypes");
            String handlerClass = (String) request.get("handlerClass");

            ReportEventDriven.EventHandler handler = eventDrivenService.registerEventHandler(
                    id, handlerName, description, subscribedEventTypes, handlerClass
            );

            return ResponseEntity.ok(handler);

        } catch (IllegalArgumentException e) {
            log.error("Architecture not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error registering event handler: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create subscription
     */
    @PostMapping("/{id}/subscription")
    public ResponseEntity<ReportEventDriven.EventSubscription> createSubscription(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/event-driven/{}/subscription", id);

        try {
            String subscriptionName = request.get("subscriptionName");
            String streamId = request.get("streamId");
            String handlerId = request.get("handlerId");
            String consumerGroup = request.get("consumerGroup");

            ReportEventDriven.EventSubscription subscription = eventDrivenService.createSubscription(
                    id, subscriptionName, streamId, handlerId, consumerGroup
            );

            return ResponseEntity.ok(subscription);

        } catch (IllegalArgumentException e) {
            log.error("Architecture not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating subscription: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Execute command (CQRS)
     */
    @PostMapping("/{id}/command")
    public ResponseEntity<ReportEventDriven.Command> executeCommand(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/event-driven/{}/command", id);

        try {
            String commandType = (String) request.get("commandType");
            String aggregateId = (String) request.get("aggregateId");
            String aggregateType = (String) request.get("aggregateType");
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) request.get("payload");
            String issuedBy = (String) request.get("issuedBy");

            ReportEventDriven.Command command = eventDrivenService.executeCommand(
                    id, commandType, aggregateId, aggregateType, payload, issuedBy
            );

            return ResponseEntity.ok(command);

        } catch (IllegalArgumentException e) {
            log.error("Architecture not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error executing command: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Execute query (CQRS)
     */
    @PostMapping("/{id}/query")
    public ResponseEntity<ReportEventDriven.Query> executeQuery(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/event-driven/{}/query", id);

        try {
            String queryType = (String) request.get("queryType");
            @SuppressWarnings("unchecked")
            Map<String, Object> parameters = (Map<String, Object>) request.get("parameters");
            String projectionId = (String) request.get("projectionId");

            ReportEventDriven.Query query = eventDrivenService.executeQuery(
                    id, queryType, parameters, projectionId
            );

            return ResponseEntity.ok(query);

        } catch (IllegalArgumentException e) {
            log.error("Architecture not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error executing query: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Start saga
     */
    @PostMapping("/{id}/saga")
    public ResponseEntity<ReportEventDriven.Saga> startSaga(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/event-driven/{}/saga", id);

        try {
            String sagaType = (String) request.get("sagaType");
            String initiatingEventId = (String) request.get("initiatingEventId");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> stepsData = (List<Map<String, Object>>) request.get("steps");

            List<ReportEventDriven.SagaStep> steps = null;
            if (stepsData != null) {
                steps = stepsData.stream()
                        .map(this::mapToSagaStep)
                        .toList();
            }

            ReportEventDriven.Saga saga = eventDrivenService.startSaga(
                    id, sagaType, initiatingEventId, steps
            );

            return ResponseEntity.ok(saga);

        } catch (IllegalArgumentException e) {
            log.error("Architecture not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error starting saga: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Complete saga
     */
    @PostMapping("/{id}/saga/{sagaId}/complete")
    public ResponseEntity<Map<String, Object>> completeSaga(
            @PathVariable Long id,
            @PathVariable String sagaId,
            @RequestBody Map<String, Boolean> request) {
        log.info("POST /api/event-driven/{}/saga/{}/complete", id, sagaId);

        try {
            Boolean success = request.get("success");
            eventDrivenService.completeSaga(id, sagaId, success != null && success);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Saga completed");
            response.put("sagaId", sagaId);
            response.put("success", success);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Architecture not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error completing saga: {}", sagaId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create projection
     */
    @PostMapping("/{id}/projection")
    public ResponseEntity<ReportEventDriven.EventProjection> createProjection(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/event-driven/{}/projection", id);

        try {
            String projectionName = (String) request.get("projectionName");
            String description = (String) request.get("description");
            @SuppressWarnings("unchecked")
            List<String> subscribedEventTypes = (List<String>) request.get("subscribedEventTypes");
            String projectionStore = (String) request.get("projectionStore");
            String tableName = (String) request.get("tableName");

            ReportEventDriven.EventProjection projection = eventDrivenService.createProjection(
                    id, projectionName, description, subscribedEventTypes, projectionStore, tableName
            );

            return ResponseEntity.ok(projection);

        } catch (IllegalArgumentException e) {
            log.error("Architecture not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating projection: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Move to dead letter queue
     */
    @PostMapping("/{id}/dead-letter")
    public ResponseEntity<ReportEventDriven.DeadLetterEvent> moveToDeadLetter(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/event-driven/{}/dead-letter", id);

        try {
            String originalEventId = (String) request.get("originalEventId");
            String eventType = (String) request.get("eventType");
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) request.get("payload");
            String handlerId = (String) request.get("handlerId");
            String failureReason = (String) request.get("failureReason");
            String errorMessage = (String) request.get("errorMessage");
            Integer attemptCount = request.get("attemptCount") != null ?
                    ((Number) request.get("attemptCount")).intValue() : 1;

            ReportEventDriven.DeadLetterEvent deadLetterEvent = eventDrivenService.moveToDeadLetter(
                    id, originalEventId, eventType, payload, handlerId, failureReason, errorMessage, attemptCount
            );

            return ResponseEntity.ok(deadLetterEvent);

        } catch (IllegalArgumentException e) {
            log.error("Architecture not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error moving to dead letter queue: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Start event replay
     */
    @PostMapping("/{id}/replay")
    public ResponseEntity<ReportEventDriven.EventReplay> startEventReplay(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/event-driven/{}/replay", id);

        try {
            String replayName = (String) request.get("replayName");
            String streamId = (String) request.get("streamId");
            Long fromOffset = request.get("fromOffset") != null ?
                    ((Number) request.get("fromOffset")).longValue() : 0L;
            Long toOffset = request.get("toOffset") != null ?
                    ((Number) request.get("toOffset")).longValue() : 1000L;
            @SuppressWarnings("unchecked")
            List<String> eventTypes = (List<String>) request.get("eventTypes");
            @SuppressWarnings("unchecked")
            List<String> targetHandlers = (List<String>) request.get("targetHandlers");

            ReportEventDriven.EventReplay replay = eventDrivenService.startEventReplay(
                    id, replayName, streamId, fromOffset, toOffset, eventTypes, targetHandlers
            );

            return ResponseEntity.ok(replay);

        } catch (IllegalArgumentException e) {
            log.error("Architecture not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error starting event replay: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Complete event replay
     */
    @PostMapping("/{id}/replay/{replayId}/complete")
    public ResponseEntity<Map<String, Object>> completeEventReplay(
            @PathVariable Long id,
            @PathVariable String replayId,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/event-driven/{}/replay/{}/complete", id, replayId);

        try {
            Long eventsReplayed = request.get("eventsReplayed") != null ?
                    ((Number) request.get("eventsReplayed")).longValue() : 0L;
            Long eventsProcessed = request.get("eventsProcessed") != null ?
                    ((Number) request.get("eventsProcessed")).longValue() : 0L;
            Long eventsFailed = request.get("eventsFailed") != null ?
                    ((Number) request.get("eventsFailed")).longValue() : 0L;

            eventDrivenService.completeEventReplay(id, replayId, eventsReplayed, eventsProcessed, eventsFailed);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Event replay completed");
            response.put("replayId", replayId);
            response.put("eventsReplayed", eventsReplayed);
            response.put("eventsProcessed", eventsProcessed);
            response.put("eventsFailed", eventsFailed);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Architecture not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error completing event replay: {}", replayId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete architecture
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteArchitecture(@PathVariable Long id) {
        log.info("DELETE /api/event-driven/{}", id);

        try {
            eventDrivenService.deleteArchitecture(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Event-driven architecture deleted");
            response.put("architectureId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting architecture: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/event-driven/stats");

        try {
            Map<String, Object> stats = eventDrivenService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Helper methods

    private ReportEventDriven.SagaStep mapToSagaStep(Map<String, Object> map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> commandPayload = (Map<String, Object>) map.get("commandPayload");
        @SuppressWarnings("unchecked")
        Map<String, Object> compensatingPayload = (Map<String, Object>) map.get("compensatingPayload");

        return ReportEventDriven.SagaStep.builder()
                .stepId(java.util.UUID.randomUUID().toString())
                .stepName((String) map.get("stepName"))
                .stepOrder(map.get("stepOrder") != null ? ((Number) map.get("stepOrder")).intValue() : 0)
                .commandType((String) map.get("commandType"))
                .commandPayload(commandPayload != null ? commandPayload : new HashMap<>())
                .compensatingCommandType((String) map.get("compensatingCommandType"))
                .compensatingPayload(compensatingPayload != null ? compensatingPayload : new HashMap<>())
                .status("PENDING")
                .build();
    }
}
