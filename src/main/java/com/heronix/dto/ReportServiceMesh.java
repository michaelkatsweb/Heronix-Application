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
 * Report Service Mesh DTO
 *
 * Manages service mesh infrastructure, traffic management, service-to-service communication,
 * mTLS encryption, and advanced networking for microservices architecture.
 *
 * Educational Use Cases:
 * - Student portal microservices communication
 * - Learning management service discovery
 * - Course registration traffic routing
 * - Authentication service load balancing
 * - Database connection pooling and routing
 * - Third-party integration circuit breaking
 * - Campus services fault injection testing
 * - API gateway sidecar injection
 * - Cross-service authorization policies
 * - Multi-region service failover
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 128 - Report Service Mesh
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportServiceMesh {

    // Basic Information
    private Long meshId;
    private String meshName;
    private String description;
    private MeshStatus status;
    private String organizationId;
    private String meshPlatform; // ISTIO, LINKERD, CONSUL_CONNECT, AWS_APP_MESH, KUMA

    // Configuration
    private String controlPlaneVersion;
    private String dataPlaneVersion;
    private Boolean mtlsEnabled;
    private String mtlsMode; // STRICT, PERMISSIVE, DISABLED
    private Boolean tracingEnabled;
    private String tracingBackend;
    private Boolean metricsEnabled;

    // State
    private Boolean isActive;
    private Boolean isHealthy;
    private LocalDateTime createdAt;
    private LocalDateTime deployedAt;
    private LocalDateTime lastUpdateAt;
    private String createdBy;

    // Services
    private List<MeshService> services;
    private Map<String, MeshService> serviceRegistry;

    // Sidecars
    private List<SidecarProxy> sidecars;
    private Map<String, SidecarProxy> sidecarRegistry;

    // Virtual Services
    private List<VirtualService> virtualServices;
    private Map<String, VirtualService> virtualServiceRegistry;

    // Destination Rules
    private List<DestinationRule> destinationRules;
    private Map<String, DestinationRule> destinationRuleRegistry;

    // Service Entries
    private List<ServiceEntry> serviceEntries;
    private Map<String, ServiceEntry> serviceEntryRegistry;

    // Gateways
    private List<MeshGateway> gateways;
    private Map<String, MeshGateway> gatewayRegistry;

    // Authorization Policies
    private List<AuthorizationPolicy> authPolicies;
    private Map<String, AuthorizationPolicy> authPolicyRegistry;

    // Peer Authentications
    private List<PeerAuthentication> peerAuths;
    private Map<String, PeerAuthentication> peerAuthRegistry;

    // Traffic Policies
    private List<TrafficPolicy> trafficPolicies;
    private Map<String, TrafficPolicy> trafficPolicyRegistry;

    // Fault Injections
    private List<FaultInjection> faultInjections;
    private Map<String, FaultInjection> faultInjectionRegistry;

    // Metrics
    private Long totalServices;
    private Long totalSidecars;
    private Long healthySidecars;
    private Long totalRequests;
    private Long successfulRequests;
    private Long failedRequests;
    private Double averageLatency;
    private Long mtlsConnections;
    private Long totalPolicies;

    // Events
    private List<MeshEvent> events;

    /**
     * Mesh status enumeration
     */
    public enum MeshStatus {
        INITIALIZING,
        CONFIGURING,
        DEPLOYING,
        ACTIVE,
        UPDATING,
        DEGRADED,
        MAINTENANCE,
        OFFLINE
    }

    /**
     * Load balancing strategy enumeration
     */
    public enum LoadBalancingStrategy {
        ROUND_ROBIN,
        LEAST_CONNECTIONS,
        RANDOM,
        WEIGHTED,
        CONSISTENT_HASH
    }

    /**
     * TLS mode enumeration
     */
    public enum TLSMode {
        DISABLE,
        SIMPLE,
        MUTUAL,
        ISTIO_MUTUAL
    }

    /**
     * Mesh service data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeshService {
        private String serviceId;
        private String serviceName;
        private String namespace;
        private List<String> ports;
        private Map<String, String> labels;
        private List<String> endpoints;
        private Integer endpointCount;
        private Boolean sidecarInjected;
        private String version;
        private LocalDateTime createdAt;
        private LocalDateTime lastSeenAt;
        private Long requestCount;
        private Map<String, Object> metadata;
    }

    /**
     * Sidecar proxy data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SidecarProxy {
        private String sidecarId;
        private String sidecarName;
        private String serviceId;
        private String podName;
        private String proxyVersion;
        private String proxyImage;
        private Boolean isHealthy;
        private String status;
        private Integer cpuUsage;
        private Integer memoryUsage;
        private Long requestsHandled;
        private Long activeConnections;
        private LocalDateTime startedAt;
        private LocalDateTime lastHealthCheckAt;
        private Map<String, Object> configuration;
    }

    /**
     * Virtual service data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VirtualService {
        private String virtualServiceId;
        private String virtualServiceName;
        private String namespace;
        private List<String> hosts;
        private List<String> gateways;
        private List<HTTPRoute> httpRoutes;
        private List<TCPRoute> tcpRoutes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Boolean enabled;
    }

    /**
     * HTTP route data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HTTPRoute {
        private String routeId;
        private String name;
        private HTTPMatchRequest match;
        private List<HTTPRouteDestination> route;
        private HTTPRetry retry;
        private Integer timeout;
        private HTTPFaultInjection fault;
        private HTTPRewrite rewrite;
    }

    /**
     * HTTP match request data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HTTPMatchRequest {
        private String uri;
        private String uriPrefix;
        private String uriRegex;
        private String method;
        private Map<String, String> headers;
        private Map<String, String> queryParams;
    }

    /**
     * HTTP route destination data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HTTPRouteDestination {
        private String destination;
        private String subset;
        private Integer port;
        private Integer weight;
    }

    /**
     * HTTP retry data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HTTPRetry {
        private Integer attempts;
        private Integer perTryTimeout;
        private String retryOn; // 5xx, gateway-error, reset, connect-failure, refused-stream
    }

    /**
     * HTTP fault injection data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HTTPFaultInjection {
        private Integer delayPercent;
        private Integer delayMilliseconds;
        private Integer abortPercent;
        private Integer abortStatusCode;
    }

    /**
     * HTTP rewrite data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HTTPRewrite {
        private String uri;
        private String authority;
    }

    /**
     * TCP route data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TCPRoute {
        private String routeId;
        private List<TCPRouteDestination> route;
    }

    /**
     * TCP route destination data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TCPRouteDestination {
        private String destination;
        private String subset;
        private Integer port;
        private Integer weight;
    }

    /**
     * Destination rule data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DestinationRule {
        private String ruleId;
        private String ruleName;
        private String host;
        private String namespace;
        private TrafficPolicy trafficPolicy;
        private List<Subset> subsets;
        private LocalDateTime createdAt;
        private Boolean enabled;
    }

    /**
     * Subset data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Subset {
        private String name;
        private Map<String, String> labels;
        private TrafficPolicy trafficPolicy;
    }

    /**
     * Service entry data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceEntry {
        private String entryId;
        private String entryName;
        private List<String> hosts;
        private List<String> addresses;
        private List<Port> ports;
        private String location; // MESH_EXTERNAL, MESH_INTERNAL
        private String resolution; // NONE, STATIC, DNS
        private List<String> endpoints;
        private LocalDateTime createdAt;
    }

    /**
     * Port data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Port {
        private Integer number;
        private String protocol;
        private String name;
    }

    /**
     * Mesh gateway data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeshGateway {
        private String gatewayId;
        private String gatewayName;
        private String namespace;
        private Map<String, String> selector;
        private List<Server> servers;
        private LocalDateTime createdAt;
        private Boolean enabled;
    }

    /**
     * Server data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Server {
        private Integer port;
        private String protocol;
        private List<String> hosts;
        private TLSOptions tls;
    }

    /**
     * TLS options data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TLSOptions {
        private TLSMode mode;
        private String credentialName;
        private List<String> subjectAltNames;
        private Integer minProtocolVersion;
    }

    /**
     * Authorization policy data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorizationPolicy {
        private String policyId;
        private String policyName;
        private String namespace;
        private Map<String, String> selector;
        private String action; // ALLOW, DENY, AUDIT, CUSTOM
        private List<Rule> rules;
        private LocalDateTime createdAt;
        private Boolean enabled;
    }

    /**
     * Rule data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Rule {
        private List<Source> from;
        private List<Operation> to;
        private List<Condition> when;
    }

    /**
     * Source data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Source {
        private List<String> principals;
        private List<String> namespaces;
        private List<String> ipBlocks;
    }

    /**
     * Operation data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Operation {
        private List<String> hosts;
        private List<String> ports;
        private List<String> methods;
        private List<String> paths;
    }

    /**
     * Condition data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Condition {
        private String key;
        private List<String> values;
    }

    /**
     * Peer authentication data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeerAuthentication {
        private String peerAuthId;
        private String peerAuthName;
        private String namespace;
        private Map<String, String> selector;
        private String mtlsMode; // STRICT, PERMISSIVE, DISABLE
        private LocalDateTime createdAt;
        private Boolean enabled;
    }

    /**
     * Traffic policy data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrafficPolicy {
        private LoadBalancingStrategy loadBalancer;
        private ConnectionPoolSettings connectionPool;
        private OutlierDetection outlierDetection;
        private TLSSettings tls;
    }

    /**
     * Connection pool settings data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConnectionPoolSettings {
        private Integer maxConnections;
        private Integer maxPendingRequests;
        private Integer maxRequests;
        private Integer maxRetries;
        private Integer connectTimeout;
        private Integer idleTimeout;
    }

    /**
     * Outlier detection data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OutlierDetection {
        private Integer consecutiveErrors;
        private Integer interval;
        private Integer baseEjectionTime;
        private Integer maxEjectionPercent;
        private Integer minHealthPercent;
    }

    /**
     * TLS settings data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TLSSettings {
        private TLSMode mode;
        private String clientCertificate;
        private String privateKey;
        private String caCertificates;
        private List<String> subjectAltNames;
    }

    /**
     * Fault injection data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FaultInjection {
        private String faultId;
        private String faultName;
        private String targetService;
        private String faultType; // DELAY, ABORT
        private Integer percentage;
        private Integer delayMs;
        private Integer abortStatusCode;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private Boolean enabled;
        private Long executionCount;
    }

    /**
     * Mesh event data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeshEvent {
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
     * Deploy service mesh
     */
    public void deployServiceMesh() {
        this.status = MeshStatus.ACTIVE;
        this.isActive = true;
        this.isHealthy = true;
        this.deployedAt = LocalDateTime.now();
        recordEvent("MESH_DEPLOYED", "Service mesh deployed", "MESH",
                meshId != null ? meshId.toString() : null);
    }

    /**
     * Register service
     */
    public void registerService(MeshService service) {
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
     * Inject sidecar
     */
    public void injectSidecar(SidecarProxy sidecar) {
        if (sidecars == null) {
            sidecars = new ArrayList<>();
        }
        sidecars.add(sidecar);

        if (sidecarRegistry == null) {
            sidecarRegistry = new HashMap<>();
        }
        sidecarRegistry.put(sidecar.getSidecarId(), sidecar);

        totalSidecars = (totalSidecars != null ? totalSidecars : 0L) + 1;
        if (Boolean.TRUE.equals(sidecar.getIsHealthy())) {
            healthySidecars = (healthySidecars != null ? healthySidecars : 0L) + 1;
        }

        recordEvent("SIDECAR_INJECTED", "Sidecar proxy injected", "SIDECAR", sidecar.getSidecarId());
    }

    /**
     * Add virtual service
     */
    public void addVirtualService(VirtualService virtualService) {
        if (virtualServices == null) {
            virtualServices = new ArrayList<>();
        }
        virtualServices.add(virtualService);

        if (virtualServiceRegistry == null) {
            virtualServiceRegistry = new HashMap<>();
        }
        virtualServiceRegistry.put(virtualService.getVirtualServiceId(), virtualService);

        recordEvent("VIRTUAL_SERVICE_ADDED", "Virtual service added", "VIRTUAL_SERVICE",
                virtualService.getVirtualServiceId());
    }

    /**
     * Add destination rule
     */
    public void addDestinationRule(DestinationRule rule) {
        if (destinationRules == null) {
            destinationRules = new ArrayList<>();
        }
        destinationRules.add(rule);

        if (destinationRuleRegistry == null) {
            destinationRuleRegistry = new HashMap<>();
        }
        destinationRuleRegistry.put(rule.getRuleId(), rule);

        recordEvent("DESTINATION_RULE_ADDED", "Destination rule added", "DESTINATION_RULE", rule.getRuleId());
    }

    /**
     * Add authorization policy
     */
    public void addAuthorizationPolicy(AuthorizationPolicy policy) {
        if (authPolicies == null) {
            authPolicies = new ArrayList<>();
        }
        authPolicies.add(policy);

        if (authPolicyRegistry == null) {
            authPolicyRegistry = new HashMap<>();
        }
        authPolicyRegistry.put(policy.getPolicyId(), policy);

        totalPolicies = (totalPolicies != null ? totalPolicies : 0L) + 1;

        recordEvent("AUTH_POLICY_ADDED", "Authorization policy added", "AUTH_POLICY", policy.getPolicyId());
    }

    /**
     * Track request
     */
    public void trackRequest(boolean success, Long latency) {
        totalRequests = (totalRequests != null ? totalRequests : 0L) + 1;

        if (success) {
            successfulRequests = (successfulRequests != null ? successfulRequests : 0L) + 1;
        } else {
            failedRequests = (failedRequests != null ? failedRequests : 0L) + 1;
        }

        if (latency != null) {
            updateLatencyMetrics(latency);
        }
    }

    /**
     * Update latency metrics
     */
    private void updateLatencyMetrics(Long latency) {
        Double latencyMs = latency.doubleValue();

        if (averageLatency == null) {
            averageLatency = latencyMs;
        } else if (totalRequests != null && totalRequests > 0) {
            averageLatency = (averageLatency * (totalRequests - 1) + latencyMs) / totalRequests;
        }
    }

    /**
     * Record mesh event
     */
    private void recordEvent(String eventType, String description, String targetType, String targetId) {
        if (events == null) {
            events = new ArrayList<>();
        }

        MeshEvent event = MeshEvent.builder()
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
