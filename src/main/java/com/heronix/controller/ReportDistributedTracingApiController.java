package com.heronix.controller;

import com.heronix.dto.ReportDistributedTracing;
import com.heronix.service.ReportDistributedTracingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Distributed Tracing API Controller
 *
 * REST API endpoints for distributed tracing, service observability,
 * performance monitoring, and trace analysis.
 *
 * Endpoints:
 * - POST /api/tracing - Create tracing system
 * - GET /api/tracing/{id} - Get tracing system
 * - POST /api/tracing/{id}/deploy - Deploy tracing system
 * - POST /api/tracing/{id}/trace - Start trace
 * - POST /api/tracing/{id}/span - Add span
 * - POST /api/tracing/{id}/span/{spanId}/complete - Complete span
 * - POST /api/tracing/{id}/service - Register service
 * - POST /api/tracing/{id}/operation - Register operation
 * - POST /api/tracing/{id}/dependency - Add service dependency
 * - POST /api/tracing/{id}/span/{spanId}/log - Add span log
 * - POST /api/tracing/{id}/error - Record trace error
 * - POST /api/tracing/{id}/profile - Create latency profile
 * - POST /api/tracing/{id}/metric - Add trace metric
 * - DELETE /api/tracing/{id} - Delete tracing system
 * - GET /api/tracing/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 127 - Report Distributed Tracing & Observability
 */
@RestController
@RequestMapping("/api/tracing")
@RequiredArgsConstructor
@Slf4j
public class ReportDistributedTracingApiController {

    private final ReportDistributedTracingService tracingService;

    /**
     * Create tracing system
     */
    @PostMapping
    public ResponseEntity<ReportDistributedTracing> createTracingSystem(
            @RequestBody ReportDistributedTracing tracingSystem) {
        log.info("POST /api/tracing - Creating tracing system: {}", tracingSystem.getSystemName());

        try {
            ReportDistributedTracing created = tracingService.createTracingSystem(tracingSystem);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating tracing system", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get tracing system
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportDistributedTracing> getTracingSystem(@PathVariable Long id) {
        log.info("GET /api/tracing/{}", id);

        try {
            return tracingService.getTracingSystem(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching tracing system: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Deploy tracing system
     */
    @PostMapping("/{id}/deploy")
    public ResponseEntity<Map<String, Object>> deployTracingSystem(@PathVariable Long id) {
        log.info("POST /api/tracing/{}/deploy", id);

        try {
            tracingService.deployTracingSystem(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Tracing system deployed");
            response.put("tracingSystemId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Tracing system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error deploying tracing system: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Start trace
     */
    @PostMapping("/{id}/trace")
    public ResponseEntity<ReportDistributedTracing.Trace> startTrace(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/tracing/{}/trace", id);

        try {
            String traceName = (String) request.get("traceName");
            @SuppressWarnings("unchecked")
            List<String> services = (List<String>) request.get("services");

            ReportDistributedTracing.Trace trace = tracingService.startTrace(id, traceName, services);

            return ResponseEntity.ok(trace);

        } catch (IllegalArgumentException e) {
            log.error("Tracing system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error starting trace: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add span
     */
    @PostMapping("/{id}/span")
    public ResponseEntity<ReportDistributedTracing.Span> addSpan(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/tracing/{}/span", id);

        try {
            String traceId = (String) request.get("traceId");
            String parentSpanId = (String) request.get("parentSpanId");
            String operationName = (String) request.get("operationName");
            String serviceName = (String) request.get("serviceName");
            String spanKindStr = (String) request.get("spanKind");
            @SuppressWarnings("unchecked")
            Map<String, String> tags = (Map<String, String>) request.get("tags");

            ReportDistributedTracing.SpanKind spanKind = spanKindStr != null ?
                    ReportDistributedTracing.SpanKind.valueOf(spanKindStr) :
                    ReportDistributedTracing.SpanKind.INTERNAL;

            ReportDistributedTracing.Span span = tracingService.addSpan(
                    id, traceId, parentSpanId, operationName, serviceName, spanKind, tags
            );

            return ResponseEntity.ok(span);

        } catch (IllegalArgumentException e) {
            log.error("Tracing system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding span: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Complete span
     */
    @PostMapping("/{id}/span/{spanId}/complete")
    public ResponseEntity<Map<String, Object>> completeSpan(
            @PathVariable Long id,
            @PathVariable String spanId,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/tracing/{}/span/{}/complete", id, spanId);

        try {
            String statusStr = request.get("status");
            String statusMessage = request.get("statusMessage");

            ReportDistributedTracing.SpanStatus status = statusStr != null ?
                    ReportDistributedTracing.SpanStatus.valueOf(statusStr) :
                    ReportDistributedTracing.SpanStatus.OK;

            tracingService.completeSpan(id, spanId, status, statusMessage);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Span completed");
            response.put("spanId", spanId);
            response.put("status", status);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Tracing system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error completing span: {}", spanId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Register service
     */
    @PostMapping("/{id}/service")
    public ResponseEntity<ReportDistributedTracing.TracedService> registerService(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/tracing/{}/service", id);

        try {
            String serviceName = (String) request.get("serviceName");
            String version = (String) request.get("version");
            String environment = (String) request.get("environment");
            String host = (String) request.get("host");
            Integer port = request.get("port") != null ?
                    ((Number) request.get("port")).intValue() : null;

            ReportDistributedTracing.TracedService service = tracingService.registerService(
                    id, serviceName, version, environment, host, port
            );

            return ResponseEntity.ok(service);

        } catch (IllegalArgumentException e) {
            log.error("Tracing system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error registering service: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Register operation
     */
    @PostMapping("/{id}/operation")
    public ResponseEntity<ReportDistributedTracing.Operation> registerOperation(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/tracing/{}/operation", id);

        try {
            String operationName = request.get("operationName");
            String serviceName = request.get("serviceName");
            String httpMethod = request.get("httpMethod");
            String httpRoute = request.get("httpRoute");

            ReportDistributedTracing.Operation operation = tracingService.registerOperation(
                    id, operationName, serviceName, httpMethod, httpRoute
            );

            return ResponseEntity.ok(operation);

        } catch (IllegalArgumentException e) {
            log.error("Tracing system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error registering operation: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add service dependency
     */
    @PostMapping("/{id}/dependency")
    public ResponseEntity<ReportDistributedTracing.ServiceDependency> addServiceDependency(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/tracing/{}/dependency", id);

        try {
            String parentService = request.get("parentService");
            String childService = request.get("childService");
            String dependencyType = request.get("dependencyType");

            ReportDistributedTracing.ServiceDependency dependency = tracingService.addServiceDependency(
                    id, parentService, childService, dependencyType
            );

            return ResponseEntity.ok(dependency);

        } catch (IllegalArgumentException e) {
            log.error("Tracing system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding service dependency: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add span log
     */
    @PostMapping("/{id}/span/{spanId}/log")
    public ResponseEntity<ReportDistributedTracing.SpanLog> addSpanLog(
            @PathVariable Long id,
            @PathVariable String spanId,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/tracing/{}/span/{}/log", id, spanId);

        try {
            String level = (String) request.get("level");
            String message = (String) request.get("message");
            @SuppressWarnings("unchecked")
            Map<String, Object> fields = (Map<String, Object>) request.get("fields");

            ReportDistributedTracing.SpanLog spanLog = tracingService.addSpanLog(
                    id, spanId, level, message, fields
            );

            return ResponseEntity.ok(spanLog);

        } catch (IllegalArgumentException e) {
            log.error("Tracing system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding span log: {}", spanId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Record trace error
     */
    @PostMapping("/{id}/error")
    public ResponseEntity<ReportDistributedTracing.TraceError> recordTraceError(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/tracing/{}/error", id);

        try {
            String traceId = request.get("traceId");
            String spanId = request.get("spanId");
            String serviceName = request.get("serviceName");
            String operationName = request.get("operationName");
            String errorType = request.get("errorType");
            String errorMessage = request.get("errorMessage");
            String stackTrace = request.get("stackTrace");
            String severity = request.get("severity");

            ReportDistributedTracing.TraceError error = tracingService.recordTraceError(
                    id, traceId, spanId, serviceName, operationName, errorType,
                    errorMessage, stackTrace, severity
            );

            return ResponseEntity.ok(error);

        } catch (IllegalArgumentException e) {
            log.error("Tracing system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error recording trace error: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create latency profile
     */
    @PostMapping("/{id}/profile")
    public ResponseEntity<ReportDistributedTracing.LatencyProfile> createLatencyProfile(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/tracing/{}/profile", id);

        try {
            String profileName = (String) request.get("profileName");
            String serviceName = (String) request.get("serviceName");
            String operationName = (String) request.get("operationName");
            String startTimeStr = (String) request.get("startTime");
            String endTimeStr = (String) request.get("endTime");

            LocalDateTime startTime = startTimeStr != null ?
                    LocalDateTime.parse(startTimeStr) : LocalDateTime.now().minusHours(1);
            LocalDateTime endTime = endTimeStr != null ?
                    LocalDateTime.parse(endTimeStr) : LocalDateTime.now();

            ReportDistributedTracing.LatencyProfile profile = tracingService.createLatencyProfile(
                    id, profileName, serviceName, operationName, startTime, endTime
            );

            return ResponseEntity.ok(profile);

        } catch (IllegalArgumentException e) {
            log.error("Tracing system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating latency profile: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add trace metric
     */
    @PostMapping("/{id}/metric")
    public ResponseEntity<ReportDistributedTracing.TraceMetric> addTraceMetric(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/tracing/{}/metric", id);

        try {
            String metricName = (String) request.get("metricName");
            String serviceName = (String) request.get("serviceName");
            String operationName = (String) request.get("operationName");
            String metricType = (String) request.get("metricType");
            Double value = request.get("value") != null ?
                    ((Number) request.get("value")).doubleValue() : 0.0;
            String unit = (String) request.get("unit");
            @SuppressWarnings("unchecked")
            Map<String, String> labels = (Map<String, String>) request.get("labels");

            ReportDistributedTracing.TraceMetric metric = tracingService.addTraceMetric(
                    id, metricName, serviceName, operationName, metricType, value, unit, labels
            );

            return ResponseEntity.ok(metric);

        } catch (IllegalArgumentException e) {
            log.error("Tracing system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding trace metric: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete tracing system
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTracingSystem(@PathVariable Long id) {
        log.info("DELETE /api/tracing/{}", id);

        try {
            tracingService.deleteTracingSystem(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Tracing system deleted");
            response.put("tracingSystemId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting tracing system: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/tracing/stats");

        try {
            Map<String, Object> stats = tracingService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
