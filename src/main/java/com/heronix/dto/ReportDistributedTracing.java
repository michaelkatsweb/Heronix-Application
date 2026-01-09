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
 * Report Distributed Tracing & Observability DTO
 *
 * Manages distributed tracing, service observability, performance monitoring,
 * trace analysis, and system health tracking for microservices architecture.
 *
 * Educational Use Cases:
 * - Student portal microservices request tracing
 * - Learning management system performance monitoring
 * - Course registration workflow tracing
 * - API gateway request flow analysis
 * - Database query performance tracking
 * - Authentication service latency monitoring
 * - Third-party integration tracing
 * - Mobile app API call tracking
 * - Service dependency mapping
 * - Error propagation analysis
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 127 - Report Distributed Tracing & Observability
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDistributedTracing {

    // Basic Information
    private Long tracingSystemId;
    private String systemName;
    private String description;
    private TracingStatus status;
    private String organizationId;
    private String tracingBackend; // JAEGER, ZIPKIN, OPENTELEMETRY, DATADOG, NEW_RELIC

    // Configuration
    private String samplingStrategy;
    private Double samplingRate;
    private String tracePropagation; // W3C, B3, JAEGER
    private Long traceRetentionDays;
    private Boolean spanBatching;
    private Integer batchSize;

    // State
    private Boolean isActive;
    private Boolean isCollecting;
    private LocalDateTime createdAt;
    private LocalDateTime deployedAt;
    private LocalDateTime lastTraceAt;
    private String createdBy;

    // Traces
    private List<Trace> traces;
    private Map<String, Trace> traceRegistry;

    // Spans
    private List<Span> spans;
    private Map<String, Span> spanRegistry;

    // Services
    private List<TracedService> services;
    private Map<String, TracedService> serviceRegistry;

    // Operations
    private List<Operation> operations;
    private Map<String, Operation> operationRegistry;

    // Dependencies
    private List<ServiceDependency> dependencies;
    private Map<String, ServiceDependency> dependencyRegistry;

    // Logs
    private List<SpanLog> spanLogs;

    // Tags
    private List<SpanTag> spanTags;

    // Metrics
    private List<TraceMetric> metrics;
    private Map<String, TraceMetric> metricRegistry;

    // Errors
    private List<TraceError> errors;
    private Map<String, TraceError> errorRegistry;

    // Latency Profiles
    private List<LatencyProfile> latencyProfiles;
    private Map<String, LatencyProfile> profileRegistry;

    // Metrics
    private Long totalTraces;
    private Long totalSpans;
    private Long totalServices;
    private Long totalOperations;
    private Long totalErrors;
    private Double averageLatency;
    private Double p50Latency;
    private Double p95Latency;
    private Double p99Latency;
    private Long tracesPerSecond;

    // Events
    private List<TracingEvent> events;

    /**
     * Tracing status enumeration
     */
    public enum TracingStatus {
        INITIALIZING,
        CONFIGURING,
        DEPLOYING,
        ACTIVE,
        COLLECTING,
        ANALYZING,
        DEGRADED,
        MAINTENANCE,
        OFFLINE
    }

    /**
     * Span status enumeration
     */
    public enum SpanStatus {
        UNSET,
        OK,
        ERROR
    }

    /**
     * Span kind enumeration
     */
    public enum SpanKind {
        INTERNAL,
        SERVER,
        CLIENT,
        PRODUCER,
        CONSUMER
    }

    /**
     * Trace data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Trace {
        private String traceId;
        private String traceName;
        private String rootSpanId;
        private List<String> spanIds;
        private Integer spanCount;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long duration;
        private List<String> services;
        private Integer serviceCount;
        private Boolean hasErrors;
        private Integer errorCount;
        private Map<String, String> tags;
        private Map<String, Object> metadata;
    }

    /**
     * Span data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Span {
        private String spanId;
        private String traceId;
        private String parentSpanId;
        private String operationName;
        private String serviceName;
        private SpanKind spanKind;
        private SpanStatus status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long duration;
        private Map<String, String> tags;
        private Map<String, Object> attributes;
        private List<SpanLog> logs;
        private List<SpanEvent> events;
        private String statusMessage;
        private Map<String, Object> metadata;
    }

    /**
     * Traced service data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TracedService {
        private String serviceId;
        private String serviceName;
        private String version;
        private String environment;
        private String host;
        private Integer port;
        private Long traceCount;
        private Long spanCount;
        private Long errorCount;
        private Double averageLatency;
        private Double errorRate;
        private List<String> operations;
        private LocalDateTime firstSeenAt;
        private LocalDateTime lastSeenAt;
        private Map<String, Object> metadata;
    }

    /**
     * Operation data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Operation {
        private String operationId;
        private String operationName;
        private String serviceName;
        private String httpMethod;
        private String httpRoute;
        private Long callCount;
        private Long errorCount;
        private Double averageLatency;
        private Double minLatency;
        private Double maxLatency;
        private Double p50Latency;
        private Double p95Latency;
        private Double p99Latency;
        private Double throughput;
        private LocalDateTime firstSeenAt;
        private LocalDateTime lastSeenAt;
    }

    /**
     * Service dependency data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceDependency {
        private String dependencyId;
        private String parentService;
        private String childService;
        private Long callCount;
        private Long errorCount;
        private Double averageLatency;
        private Double errorRate;
        private String dependencyType; // HTTP, gRPC, DATABASE, QUEUE, CACHE
        private LocalDateTime firstSeenAt;
        private LocalDateTime lastSeenAt;
    }

    /**
     * Span log data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpanLog {
        private String logId;
        private String spanId;
        private LocalDateTime timestamp;
        private String level; // DEBUG, INFO, WARN, ERROR
        private String message;
        private Map<String, Object> fields;
    }

    /**
     * Span tag data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpanTag {
        private String tagId;
        private String spanId;
        private String key;
        private String value;
        private String type; // STRING, BOOLEAN, LONG, DOUBLE
    }

    /**
     * Span event data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpanEvent {
        private String eventId;
        private String eventName;
        private LocalDateTime timestamp;
        private Map<String, Object> attributes;
    }

    /**
     * Trace metric data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TraceMetric {
        private String metricId;
        private String metricName;
        private String serviceName;
        private String operationName;
        private String metricType; // COUNTER, GAUGE, HISTOGRAM, SUMMARY
        private Double value;
        private String unit;
        private Map<String, String> labels;
        private LocalDateTime timestamp;
    }

    /**
     * Trace error data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TraceError {
        private String errorId;
        private String traceId;
        private String spanId;
        private String serviceName;
        private String operationName;
        private String errorType;
        private String errorMessage;
        private String stackTrace;
        private String severity; // LOW, MEDIUM, HIGH, CRITICAL
        private LocalDateTime timestamp;
        private Map<String, Object> context;
        private Boolean resolved;
        private String resolvedBy;
    }

    /**
     * Latency profile data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LatencyProfile {
        private String profileId;
        private String profileName;
        private String serviceName;
        private String operationName;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long sampleCount;
        private Double minLatency;
        private Double maxLatency;
        private Double avgLatency;
        private Double p50Latency;
        private Double p75Latency;
        private Double p90Latency;
        private Double p95Latency;
        private Double p99Latency;
        private Map<String, Double> percentiles;
        private LocalDateTime createdAt;
    }

    /**
     * Tracing event data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TracingEvent {
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
     * Deploy tracing system
     */
    public void deployTracingSystem() {
        this.status = TracingStatus.ACTIVE;
        this.isActive = true;
        this.isCollecting = true;
        this.deployedAt = LocalDateTime.now();
        recordEvent("TRACING_DEPLOYED", "Distributed tracing system deployed", "SYSTEM",
                tracingSystemId != null ? tracingSystemId.toString() : null);
    }

    /**
     * Start trace
     */
    public void startTrace(Trace trace) {
        if (traces == null) {
            traces = new ArrayList<>();
        }
        traces.add(trace);

        if (traceRegistry == null) {
            traceRegistry = new HashMap<>();
        }
        traceRegistry.put(trace.getTraceId(), trace);

        totalTraces = (totalTraces != null ? totalTraces : 0L) + 1;
        this.lastTraceAt = LocalDateTime.now();

        recordEvent("TRACE_STARTED", "Trace started", "TRACE", trace.getTraceId());
    }

    /**
     * Add span
     */
    public void addSpan(Span span) {
        if (spans == null) {
            spans = new ArrayList<>();
        }
        spans.add(span);

        if (spanRegistry == null) {
            spanRegistry = new HashMap<>();
        }
        spanRegistry.put(span.getSpanId(), span);

        totalSpans = (totalSpans != null ? totalSpans : 0L) + 1;

        // Update trace
        if (traceRegistry != null) {
            Trace trace = traceRegistry.get(span.getTraceId());
            if (trace != null) {
                if (trace.getSpanIds() == null) {
                    trace.setSpanIds(new ArrayList<>());
                }
                trace.getSpanIds().add(span.getSpanId());
                trace.setSpanCount((trace.getSpanCount() != null ? trace.getSpanCount() : 0) + 1);

                if (span.getStatus() == SpanStatus.ERROR) {
                    trace.setHasErrors(true);
                    trace.setErrorCount((trace.getErrorCount() != null ? trace.getErrorCount() : 0) + 1);
                }
            }
        }

        // Update latency metrics
        if (span.getDuration() != null) {
            updateLatencyMetrics(span.getDuration());
        }
    }

    /**
     * Register service
     */
    public void registerService(TracedService service) {
        if (services == null) {
            services = new ArrayList<>();
        }
        services.add(service);

        if (serviceRegistry == null) {
            serviceRegistry = new HashMap<>();
        }
        serviceRegistry.put(service.getServiceId(), service);

        totalServices = (totalServices != null ? totalServices : 0L) + 1;

        recordEvent("SERVICE_REGISTERED", "Service registered", "SERVICE", service.getServiceId());
    }

    /**
     * Register operation
     */
    public void registerOperation(Operation operation) {
        if (operations == null) {
            operations = new ArrayList<>();
        }
        operations.add(operation);

        if (operationRegistry == null) {
            operationRegistry = new HashMap<>();
        }
        operationRegistry.put(operation.getOperationId(), operation);

        totalOperations = (totalOperations != null ? totalOperations : 0L) + 1;

        recordEvent("OPERATION_REGISTERED", "Operation registered", "OPERATION", operation.getOperationId());
    }

    /**
     * Add dependency
     */
    public void addDependency(ServiceDependency dependency) {
        if (dependencies == null) {
            dependencies = new ArrayList<>();
        }
        dependencies.add(dependency);

        if (dependencyRegistry == null) {
            dependencyRegistry = new HashMap<>();
        }
        dependencyRegistry.put(dependency.getDependencyId(), dependency);

        recordEvent("DEPENDENCY_DETECTED", "Service dependency detected", "DEPENDENCY", dependency.getDependencyId());
    }

    /**
     * Record error
     */
    public void recordError(TraceError error) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(error);

        if (errorRegistry == null) {
            errorRegistry = new HashMap<>();
        }
        errorRegistry.put(error.getErrorId(), error);

        totalErrors = (totalErrors != null ? totalErrors : 0L) + 1;

        recordEvent("ERROR_RECORDED", "Trace error recorded", "ERROR", error.getErrorId());
    }

    /**
     * Add latency profile
     */
    public void addLatencyProfile(LatencyProfile profile) {
        if (latencyProfiles == null) {
            latencyProfiles = new ArrayList<>();
        }
        latencyProfiles.add(profile);

        if (profileRegistry == null) {
            profileRegistry = new HashMap<>();
        }
        profileRegistry.put(profile.getProfileId(), profile);

        recordEvent("PROFILE_CREATED", "Latency profile created", "PROFILE", profile.getProfileId());
    }

    /**
     * Calculate traces per second
     */
    public void calculateTracesPerSecond() {
        if (totalTraces != null && totalTraces > 0 && createdAt != null) {
            long secondsSinceCreation = java.time.Duration.between(createdAt, LocalDateTime.now()).toSeconds();
            if (secondsSinceCreation > 0) {
                this.tracesPerSecond = totalTraces / secondsSinceCreation;
            }
        }
    }

    /**
     * Update latency metrics
     */
    private void updateLatencyMetrics(Long duration) {
        Double durationMs = duration.doubleValue();

        if (averageLatency == null) {
            averageLatency = durationMs;
        } else if (totalSpans != null && totalSpans > 0) {
            averageLatency = (averageLatency * (totalSpans - 1) + durationMs) / totalSpans;
        }

        // Simplified percentile calculation (would use proper algorithm in production)
        if (p50Latency == null || durationMs < p50Latency) {
            p50Latency = durationMs;
        }
        if (p95Latency == null || durationMs > p95Latency * 0.95) {
            p95Latency = durationMs;
        }
        if (p99Latency == null || durationMs > p99Latency * 0.99) {
            p99Latency = durationMs;
        }
    }

    /**
     * Record tracing event
     */
    private void recordEvent(String eventType, String description, String targetType, String targetId) {
        if (events == null) {
            events = new ArrayList<>();
        }

        TracingEvent event = TracingEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType(eventType)
                .description(description)
                .targetType(targetType)
                .targetId(targetId)
                .timestamp(LocalDateTime.now())
                .triggeredBy(createdBy)
                .build();

        events.add(event);
    }
}
