package com.heronix.controller;

import com.heronix.dto.ReportEventSourcing;
import com.heronix.service.ReportEventSourcingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Event Sourcing API Controller
 *
 * REST API endpoints for event sourcing management, CQRS implementation,
 * event store operations, event replay, and projections.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 146 - Event Sourcing & CQRS
 */
@Slf4j
// @RestController  // Disabled for API server mode
@RequestMapping("/api/event-sourcing")
@RequiredArgsConstructor
public class ReportEventSourcingApiController {

    private final ReportEventSourcingService eventSourcingService;

    /**
     * Create new event sourcing configuration
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createEventSourcing(
            @RequestBody ReportEventSourcing eventSourcing) {
        try {
            ReportEventSourcing created = eventSourcingService.createEventSourcing(eventSourcing);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Event sourcing configuration created successfully");
            response.put("eventSourcingId", created.getEventSourcingId());
            response.put("eventSourcingName", created.getEventSourcingName());
            response.put("eventStoreType", created.getEventStoreType());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create event sourcing configuration: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get event sourcing configuration by ID
     */
    @GetMapping("/{eventSourcingId}")
    public ResponseEntity<Map<String, Object>> getEventSourcing(@PathVariable Long eventSourcingId) {
        try {
            ReportEventSourcing eventSourcing = eventSourcingService.getEventSourcing(eventSourcingId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("eventSourcing", eventSourcing);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Activate event sourcing
     */
    @PostMapping("/{eventSourcingId}/activate")
    public ResponseEntity<Map<String, Object>> activate(@PathVariable Long eventSourcingId) {
        try {
            ReportEventSourcing eventSourcing = eventSourcingService.activate(eventSourcingId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Event sourcing activated successfully");
            response.put("eventSourcingStatus", eventSourcing.getEventSourcingStatus());
            response.put("activatedAt", eventSourcing.getActivatedAt());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Publish event
     */
    @PostMapping("/{eventSourcingId}/events")
    public ResponseEntity<Map<String, Object>> publishEvent(
            @PathVariable Long eventSourcingId,
            @RequestBody Map<String, Object> eventData) {
        try {
            Map<String, Object> result = eventSourcingService.publishEvent(eventSourcingId, eventData);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Execute command
     */
    @PostMapping("/{eventSourcingId}/commands")
    public ResponseEntity<Map<String, Object>> executeCommand(
            @PathVariable Long eventSourcingId,
            @RequestBody Map<String, Object> commandData) {
        try {
            Map<String, Object> result = eventSourcingService.executeCommand(eventSourcingId, commandData);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Execute query
     */
    @PostMapping("/{eventSourcingId}/queries")
    public ResponseEntity<Map<String, Object>> executeQuery(
            @PathVariable Long eventSourcingId,
            @RequestBody Map<String, Object> queryData) {
        try {
            Map<String, Object> result = eventSourcingService.executeQuery(eventSourcingId, queryData);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Start event replay
     */
    @PostMapping("/{eventSourcingId}/replay/start")
    public ResponseEntity<Map<String, Object>> startReplay(
            @PathVariable Long eventSourcingId,
            @RequestBody Map<String, Object> replayConfig) {
        try {
            Map<String, Object> result = eventSourcingService.startReplay(eventSourcingId, replayConfig);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Complete event replay
     */
    @PostMapping("/{eventSourcingId}/replay/complete")
    public ResponseEntity<Map<String, Object>> completeReplay(
            @PathVariable Long eventSourcingId,
            @RequestBody Map<String, Boolean> request) {
        try {
            boolean successful = request.getOrDefault("successful", true);
            ReportEventSourcing eventSourcing = eventSourcingService.completeReplay(eventSourcingId, successful);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Event replay completed");
            response.put("replayStatus", eventSourcing.getReplayStatus());
            response.put("successful", successful);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Create snapshot
     */
    @PostMapping("/{eventSourcingId}/snapshots")
    public ResponseEntity<Map<String, Object>> createSnapshot(
            @PathVariable Long eventSourcingId,
            @RequestBody Map<String, String> request) {
        try {
            String aggregateId = request.get("aggregateId");
            if (aggregateId == null || aggregateId.isEmpty()) {
                throw new IllegalArgumentException("Aggregate ID is required");
            }

            Map<String, Object> result = eventSourcingService.createSnapshot(eventSourcingId, aggregateId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Start saga
     */
    @PostMapping("/{eventSourcingId}/sagas/start")
    public ResponseEntity<Map<String, Object>> startSaga(
            @PathVariable Long eventSourcingId,
            @RequestBody Map<String, String> request) {
        try {
            String sagaType = request.getOrDefault("sagaType", "DefaultSaga");
            Map<String, Object> result = eventSourcingService.startSaga(eventSourcingId, sagaType);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Complete saga
     */
    @PostMapping("/{eventSourcingId}/sagas/{sagaId}/complete")
    public ResponseEntity<Map<String, Object>> completeSaga(
            @PathVariable Long eventSourcingId,
            @PathVariable String sagaId,
            @RequestBody Map<String, Boolean> request) {
        try {
            boolean compensated = request.getOrDefault("compensated", false);
            ReportEventSourcing eventSourcing = eventSourcingService.completeSaga(eventSourcingId, sagaId, compensated);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Saga completed");
            response.put("activeSagas", eventSourcing.getActiveSagas());
            response.put("compensated", compensated);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Update projection
     */
    @PutMapping("/{eventSourcingId}/projections/{projectionName}")
    public ResponseEntity<Map<String, Object>> updateProjection(
            @PathVariable Long eventSourcingId,
            @PathVariable String projectionName,
            @RequestBody Map<String, Object> request) {
        try {
            long position = request.containsKey("position")
                    ? ((Number) request.get("position")).longValue()
                    : 0L;

            Map<String, Object> result = eventSourcingService.updateProjection(eventSourcingId, projectionName, position);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Get event store statistics
     */
    @GetMapping("/{eventSourcingId}/statistics/eventstore")
    public ResponseEntity<Map<String, Object>> getEventStoreStatistics(@PathVariable Long eventSourcingId) {
        try {
            Map<String, Object> stats = eventSourcingService.getEventStoreStatistics(eventSourcingId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", stats);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get all event sourcing configurations
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllEventSourcing() {
        List<ReportEventSourcing> configs = eventSourcingService.getAllEventSourcing();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("configurations", configs);
        response.put("count", configs.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Get active configurations
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveConfigs() {
        List<ReportEventSourcing> configs = eventSourcingService.getActiveConfigs();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("configurations", configs);
        response.put("count", configs.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Delete event sourcing configuration
     */
    @DeleteMapping("/{eventSourcingId}")
    public ResponseEntity<Map<String, Object>> deleteEventSourcing(@PathVariable Long eventSourcingId) {
        try {
            eventSourcingService.deleteEventSourcing(eventSourcingId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Event sourcing configuration deleted successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = eventSourcingService.getStatistics();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("statistics", stats);
        return ResponseEntity.ok(response);
    }
}
