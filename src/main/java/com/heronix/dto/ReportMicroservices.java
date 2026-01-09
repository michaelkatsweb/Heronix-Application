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
 * Report Microservices DTO
 *
 * Represents microservices architecture and service mesh for report services.
 *
 * Features:
 * - Service discovery and registration
 * - Inter-service communication
 * - Service mesh management
 * - Load balancing
 * - Circuit breaking
 * - Distributed tracing
 * - Service health monitoring
 * - Configuration management
 *
 * Service Status:
 * - REGISTERING - Service being registered
 * - HEALTHY - Service healthy and available
 * - UNHEALTHY - Service unhealthy
 * - DEGRADED - Service degraded performance
 * - MAINTENANCE - Under maintenance
 * - DEREGISTERED - Service removed
 *
 * Communication Protocol:
 * - REST - REST API
 * - GRPC - gRPC
 * - MESSAGE_QUEUE - Message queue
 * - EVENT_STREAM - Event streaming
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 95 - Report Microservices & Service Mesh
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportMicroservices {

    private Long meshId;
    private String meshName;
    private String description;
    private String version;

    // Mesh status
    private MeshStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private Boolean isActive;

    // Services
    private List<MicroService> services;
    private Integer totalServices;
    private Integer healthyServices;
    private Integer unhealthyServices;
    private Map<String, MicroService> serviceRegistry;

    // Service discovery
    private Boolean serviceDiscoveryEnabled;
    private String discoveryProvider; // Consul, Eureka, Etcd
    private Integer discoveryIntervalSeconds;
    private LocalDateTime lastDiscoveryAt;

    // Communication
    private Boolean interServiceCommunicationEnabled;
    private List<ServiceCommunication> communications;
    private Long totalCommunications;
    private Long successfulCommunications;
    private Long failedCommunications;

    // Load balancing
    private Boolean loadBalancingEnabled;
    private LoadBalancingStrategy loadBalancingStrategy;
    private Map<String, LoadBalancerState> loadBalancerStates;

    // Circuit breaking
    private Boolean circuitBreakerEnabled;
    private Integer circuitBreakerThreshold;
    private Integer circuitBreakerTimeoutSeconds;
    private Map<String, CircuitBreakerState> circuitBreakerStates;

    // Distributed tracing
    private Boolean distributedTracingEnabled;
    private String tracingProvider; // Jaeger, Zipkin, OpenTelemetry
    private List<Trace> traces;
    private Long totalTraces;

    // Health monitoring
    private Boolean healthMonitoringEnabled;
    private Integer healthCheckIntervalSeconds;
    private List<HealthCheck> healthChecks;
    private LocalDateTime lastHealthCheckAt;

    // Configuration
    private Boolean centralizedConfigEnabled;
    private String configProvider; // Spring Cloud Config, Consul
    private Map<String, ServiceConfig> serviceConfigs;

    // Service mesh
    private Boolean serviceMeshEnabled;
    private String meshProvider; // Istio, Linkerd, Consul Connect
    private List<MeshPolicy> meshPolicies;

    // Metrics
    private MeshMetrics metrics;
    private List<ServiceMetrics> serviceMetricsList;

    // Events
    private List<MeshEvent> events;
    private LocalDateTime lastEventAt;

    // Configuration
    private Map<String, Object> configuration;
    private Boolean configurationLocked;

    /**
     * Mesh Status
     */
    public enum MeshStatus {
        INITIALIZING,   // Mesh initializing
        ACTIVE,         // Active and operational
        DEGRADED,       // Degraded performance
        MAINTENANCE,    // Under maintenance
        ERROR,          // Error state
        SHUTDOWN        // Shutting down
    }

    /**
     * Service Status
     */
    public enum ServiceStatus {
        REGISTERING,    // Service being registered
        HEALTHY,        // Service healthy and available
        UNHEALTHY,      // Service unhealthy
        DEGRADED,       // Service degraded performance
        MAINTENANCE,    // Under maintenance
        DEREGISTERED    // Service removed
    }

    /**
     * Communication Protocol
     */
    public enum CommunicationProtocol {
        REST,           // REST API
        GRPC,           // gRPC
        MESSAGE_QUEUE,  // Message queue
        EVENT_STREAM    // Event streaming
    }

    /**
     * Load Balancing Strategy
     */
    public enum LoadBalancingStrategy {
        ROUND_ROBIN,
        LEAST_CONNECTIONS,
        WEIGHTED,
        RANDOM,
        CONSISTENT_HASH
    }

    /**
     * Circuit Breaker State
     */
    public enum CircuitBreakerStateEnum {
        CLOSED,
        OPEN,
        HALF_OPEN
    }

    /**
     * Micro Service
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MicroService {
        private String serviceId;
        private String serviceName;
        private String serviceType; // REPORT_GENERATION, ANALYTICS, DISTRIBUTION, etc.
        private String version;
        private ServiceStatus status;

        // Connection details
        private String host;
        private Integer port;
        private String protocol;
        private List<String> endpoints;

        // Registration
        private LocalDateTime registeredAt;
        private LocalDateTime lastHeartbeatAt;
        private Integer heartbeatIntervalSeconds;

        // Health
        private Boolean healthy;
        private String healthStatus;
        private LocalDateTime lastHealthCheckAt;

        // Metrics
        private Long totalRequests;
        private Long successfulRequests;
        private Long failedRequests;
        private Double averageLatencyMs;
        private Integer activeConnections;

        // Dependencies
        private List<String> dependsOn; // Other service IDs
        private List<String> dependents; // Services depending on this

        // Configuration
        private Map<String, Object> configuration;
        private List<String> tags;
        private Map<String, Object> metadata;
    }

    /**
     * Service Communication
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceCommunication {
        private String communicationId;
        private String sourceServiceId;
        private String targetServiceId;
        private CommunicationProtocol protocol;
        private LocalDateTime timestamp;
        private Long requestSizeBytes;
        private Long responseSizeBytes;
        private Long latencyMs;
        private Boolean success;
        private String errorMessage;
        private String traceId;
        private Map<String, Object> metadata;
    }

    /**
     * Load Balancer State
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoadBalancerState {
        private String serviceId;
        private List<String> availableInstances;
        private Integer currentIndex; // For round-robin
        private Map<String, Integer> instanceWeights;
        private Map<String, Integer> instanceConnections;
        private LocalDateTime lastBalancedAt;
    }

    /**
     * Circuit Breaker State
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CircuitBreakerState {
        private String serviceId;
        private CircuitBreakerStateEnum state;
        private Integer failureCount;
        private LocalDateTime stateChangedAt;
        private LocalDateTime nextRetryAt;
        private Long totalFailures;
        private Long totalSuccesses;
        private Integer threshold;
    }

    /**
     * Trace
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Trace {
        private String traceId;
        private String spanId;
        private String parentSpanId;
        private String serviceName;
        private String operation;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long durationMs;
        private Boolean success;
        private Map<String, String> tags;
        private List<TraceLog> logs;
    }

    /**
     * Trace Log
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TraceLog {
        private LocalDateTime timestamp;
        private String level;
        private String message;
        private Map<String, Object> fields;
    }

    /**
     * Health Check
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HealthCheck {
        private String checkId;
        private String serviceId;
        private LocalDateTime checkedAt;
        private Boolean healthy;
        private String status; // UP, DOWN, DEGRADED
        private Long responseTimeMs;
        private String errorMessage;
        private Map<String, Object> details;
    }

    /**
     * Service Config
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceConfig {
        private String serviceId;
        private String configVersion;
        private Map<String, Object> properties;
        private LocalDateTime updatedAt;
        private Boolean encrypted;
        private List<String> profiles; // dev, prod, etc.
    }

    /**
     * Mesh Policy
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeshPolicy {
        private String policyId;
        private String policyName;
        private String policyType; // RETRY, TIMEOUT, CIRCUIT_BREAKER, RATE_LIMIT
        private String targetService; // Service or pattern
        private Map<String, Object> policyConfig;
        private Boolean enabled;
        private Integer priority;
    }

    /**
     * Mesh Metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeshMetrics {
        private Long totalRequests;
        private Long successfulRequests;
        private Long failedRequests;
        private Double successRate;
        private Double averageLatencyMs;
        private Double p50LatencyMs;
        private Double p95LatencyMs;
        private Double p99LatencyMs;
        private Long totalTraces;
        private Integer activeServices;
        private Long totalServiceCommunications;
        private LocalDateTime measuredAt;
    }

    /**
     * Service Metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceMetrics {
        private String serviceId;
        private String serviceName;
        private Long requestCount;
        private Long successCount;
        private Long errorCount;
        private Double successRate;
        private Double averageLatencyMs;
        private Integer activeConnections;
        private Long cpuUsagePercent;
        private Long memoryUsedMb;
        private LocalDateTime measuredAt;
    }

    /**
     * Mesh Event
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeshEvent {
        private String eventId;
        private String eventType;
        private String description;
        private LocalDateTime timestamp;
        private String serviceId;
        private Map<String, Object> eventData;
    }

    /**
     * Helper Methods
     */

    public void registerService(MicroService service) {
        if (services == null) {
            services = new ArrayList<>();
        }
        services.add(service);

        if (serviceRegistry == null) {
            serviceRegistry = new HashMap<>();
        }
        serviceRegistry.put(service.getServiceId(), service);

        totalServices = (totalServices != null ? totalServices : 0) + 1;
        if (service.getStatus() == ServiceStatus.HEALTHY) {
            healthyServices = (healthyServices != null ? healthyServices : 0) + 1;
        } else if (service.getStatus() == ServiceStatus.UNHEALTHY) {
            unhealthyServices = (unhealthyServices != null ? unhealthyServices : 0) + 1;
        }

        recordEvent("SERVICE_REGISTERED", "Service registered: " + service.getServiceName(),
                service.getServiceId());
    }

    public void deregisterService(String serviceId) {
        if (serviceRegistry == null) {
            return;
        }

        MicroService service = serviceRegistry.remove(serviceId);
        if (service != null) {
            services.remove(service);
            totalServices = Math.max(0, (totalServices != null ? totalServices : 1) - 1);

            if (service.getStatus() == ServiceStatus.HEALTHY && healthyServices != null && healthyServices > 0) {
                healthyServices--;
            } else if (service.getStatus() == ServiceStatus.UNHEALTHY && unhealthyServices != null && unhealthyServices > 0) {
                unhealthyServices--;
            }

            recordEvent("SERVICE_DEREGISTERED", "Service deregistered: " + service.getServiceName(),
                    serviceId);
        }
    }

    public void updateServiceStatus(String serviceId, ServiceStatus status) {
        MicroService service = serviceRegistry != null ? serviceRegistry.get(serviceId) : null;
        if (service == null) {
            return;
        }

        ServiceStatus oldStatus = service.getStatus();
        service.setStatus(status);
        service.setLastHeartbeatAt(LocalDateTime.now());

        // Update counts
        if (oldStatus == ServiceStatus.HEALTHY && healthyServices != null && healthyServices > 0) {
            healthyServices--;
        } else if (oldStatus == ServiceStatus.UNHEALTHY && unhealthyServices != null && unhealthyServices > 0) {
            unhealthyServices--;
        }

        if (status == ServiceStatus.HEALTHY) {
            healthyServices = (healthyServices != null ? healthyServices : 0) + 1;
        } else if (status == ServiceStatus.UNHEALTHY) {
            unhealthyServices = (unhealthyServices != null ? unhealthyServices : 0) + 1;
        }
    }

    public void recordCommunication(ServiceCommunication communication) {
        if (communications == null) {
            communications = new ArrayList<>();
        }
        communications.add(communication);

        totalCommunications = (totalCommunications != null ? totalCommunications : 0L) + 1;
        if (Boolean.TRUE.equals(communication.getSuccess())) {
            successfulCommunications = (successfulCommunications != null ? successfulCommunications : 0L) + 1;
        } else {
            failedCommunications = (failedCommunications != null ? failedCommunications : 0L) + 1;
        }
    }

    public void recordTrace(Trace trace) {
        if (traces == null) {
            traces = new ArrayList<>();
        }
        traces.add(trace);

        totalTraces = (totalTraces != null ? totalTraces : 0L) + 1;
    }

    public void recordHealthCheck(HealthCheck healthCheck) {
        if (healthChecks == null) {
            healthChecks = new ArrayList<>();
        }
        healthChecks.add(healthCheck);

        lastHealthCheckAt = LocalDateTime.now();

        // Update service health status
        if (healthCheck.getServiceId() != null) {
            MicroService service = serviceRegistry != null ?
                    serviceRegistry.get(healthCheck.getServiceId()) : null;
            if (service != null) {
                service.setHealthy(healthCheck.getHealthy());
                service.setHealthStatus(healthCheck.getStatus());
                service.setLastHealthCheckAt(healthCheck.getCheckedAt());
            }
        }
    }

    public void recordEvent(String eventType, String description, String serviceId) {
        if (events == null) {
            events = new ArrayList<>();
        }

        MeshEvent event = MeshEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType(eventType)
                .description(description)
                .timestamp(LocalDateTime.now())
                .serviceId(serviceId)
                .eventData(new HashMap<>())
                .build();

        events.add(event);
        lastEventAt = LocalDateTime.now();
    }

    public void startMesh() {
        status = MeshStatus.ACTIVE;
        startedAt = LocalDateTime.now();
        isActive = true;
        recordEvent("MESH_STARTED", "Service mesh started", null);
    }

    public void stopMesh() {
        status = MeshStatus.SHUTDOWN;
        isActive = false;
        recordEvent("MESH_STOPPED", "Service mesh stopped", null);
    }

    public MicroService getService(String serviceId) {
        return serviceRegistry != null ? serviceRegistry.get(serviceId) : null;
    }

    public List<MicroService> getHealthyServices() {
        if (services == null) {
            return new ArrayList<>();
        }
        return services.stream()
                .filter(s -> s.getStatus() == ServiceStatus.HEALTHY)
                .toList();
    }

    public boolean isHealthy() {
        return status == MeshStatus.ACTIVE && Boolean.TRUE.equals(isActive);
    }

    public boolean requiresAttention() {
        return status == MeshStatus.DEGRADED || status == MeshStatus.ERROR ||
               (unhealthyServices != null && unhealthyServices > 0);
    }
}
