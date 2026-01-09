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
 * Report API Gateway DTO
 *
 * Represents API gateway configuration and management for report services.
 *
 * Features:
 * - Request routing and load balancing
 * - Rate limiting and throttling
 * - Authentication and authorization
 * - API versioning
 * - Request/response transformation
 * - Caching
 * - Monitoring and analytics
 * - Circuit breaking
 *
 * Gateway Status:
 * - INITIALIZING - Gateway starting up
 * - ACTIVE - Actively routing requests
 * - DEGRADED - Performance issues
 * - MAINTENANCE - Under maintenance
 * - ERROR - Error state
 * - SHUTDOWN - Shutting down
 *
 * Route Type:
 * - EXACT - Exact path match
 * - PREFIX - Path prefix match
 * - REGEX - Regular expression match
 * - WILDCARD - Wildcard pattern
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 94 - Report API Gateway & Integration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportApiGateway {

    private Long gatewayId;
    private String gatewayName;
    private String description;
    private String version;

    // Gateway status
    private GatewayStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime lastRequestAt;
    private Long uptimeSeconds;
    private Boolean isActive;

    // Configuration
    private String baseUrl;
    private Integer port;
    private String protocol; // HTTP, HTTPS
    private Boolean sslEnabled;
    private String sslCertificate;

    // Routes
    private List<Route> routes;
    private Integer totalRoutes;
    private Integer activeRoutes;
    private Map<String, Route> routeRegistry;

    // Rate limiting
    private Boolean rateLimitingEnabled;
    private Integer defaultRateLimit; // Requests per minute
    private RateLimitStrategy rateLimitStrategy;
    private List<RateLimit> rateLimits;
    private Map<String, RateLimitStatus> rateLimitStatuses;

    // Authentication
    private Boolean authenticationEnabled;
    private List<String> authenticationMethods; // API_KEY, JWT, OAUTH2, BASIC
    private Map<String, ApiKey> apiKeys;
    private Integer activeApiKeys;

    // Authorization
    private Boolean authorizationEnabled;
    private List<AuthorizationPolicy> authorizationPolicies;
    private Map<String, List<String>> rolePermissions;

    // Caching
    private Boolean cachingEnabled;
    private String cacheProvider; // In-Memory, Redis, Memcached
    private Integer cacheTtlSeconds;
    private Long cacheHits;
    private Long cacheMisses;
    private Double cacheHitRate;

    // Load balancing
    private Boolean loadBalancingEnabled;
    private LoadBalancingStrategy loadBalancingStrategy;
    private List<Backend> backends;
    private Integer activeBackends;
    private Map<String, Backend> backendRegistry;

    // Circuit breaking
    private Boolean circuitBreakerEnabled;
    private Integer circuitBreakerThreshold; // Failed requests before opening
    private Integer circuitBreakerTimeoutSeconds;
    private Map<String, CircuitBreakerState> circuitBreakerStates;

    // Request/Response transformation
    private Boolean transformationEnabled;
    private List<Transformation> transformations;

    // Monitoring
    private GatewayMetrics metrics;
    private List<RequestLog> requestLogs;
    private Long totalRequests;
    private Long successfulRequests;
    private Long failedRequests;
    private Double successRate;

    // API versioning
    private Boolean versioningEnabled;
    private String currentVersion;
    private List<String> supportedVersions;
    private VersioningStrategy versioningStrategy;

    // Security
    private Boolean corsEnabled;
    private List<String> allowedOrigins;
    private List<String> allowedMethods;
    private List<String> allowedHeaders;
    private Boolean csrfProtectionEnabled;
    private Integer maxRequestSizeBytes;

    // Audit
    private List<GatewayEvent> auditTrail;
    private LocalDateTime lastAuditAt;

    // Configuration
    private Map<String, Object> configuration;
    private Boolean configurationLocked;

    /**
     * Gateway Status
     */
    public enum GatewayStatus {
        INITIALIZING,   // Gateway starting up
        ACTIVE,         // Actively routing requests
        DEGRADED,       // Performance issues
        MAINTENANCE,    // Under maintenance
        ERROR,          // Error state
        SHUTDOWN        // Shutting down
    }

    /**
     * Route Type
     */
    public enum RouteType {
        EXACT,      // Exact path match
        PREFIX,     // Path prefix match
        REGEX,      // Regular expression match
        WILDCARD    // Wildcard pattern
    }

    /**
     * Rate Limit Strategy
     */
    public enum RateLimitStrategy {
        FIXED_WINDOW,       // Fixed time window
        SLIDING_WINDOW,     // Sliding time window
        TOKEN_BUCKET,       // Token bucket algorithm
        LEAKY_BUCKET        // Leaky bucket algorithm
    }

    /**
     * Load Balancing Strategy
     */
    public enum LoadBalancingStrategy {
        ROUND_ROBIN,        // Round-robin distribution
        LEAST_CONNECTIONS,  // Least connections
        IP_HASH,            // IP hash based
        WEIGHTED,           // Weighted distribution
        RANDOM              // Random selection
    }

    /**
     * Versioning Strategy
     */
    public enum VersioningStrategy {
        URL_PATH,       // /v1/resource
        QUERY_PARAM,    // /resource?version=1
        HEADER,         // X-API-Version: 1
        CONTENT_TYPE    // application/vnd.api.v1+json
    }

    /**
     * Circuit Breaker State
     */
    public enum CircuitBreakerStateEnum {
        CLOSED,     // Normal operation
        OPEN,       // Blocking requests
        HALF_OPEN   // Testing recovery
    }

    /**
     * Route
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Route {
        private String routeId;
        private String routeName;
        private String path;
        private RouteType routeType;
        private String targetUrl;
        private List<String> methods; // GET, POST, PUT, DELETE
        private Boolean enabled;
        private Integer priority;

        // Rate limiting
        private Integer rateLimit; // Override default
        private String rateLimitKey; // IP, User, API_Key

        // Authentication
        private Boolean authenticationRequired;
        private List<String> allowedRoles;

        // Caching
        private Boolean cacheable;
        private Integer cacheTtlSeconds;

        // Timeouts
        private Integer timeoutSeconds;
        private Integer retryAttempts;

        // Metrics
        private Long requestCount;
        private Long successCount;
        private Long errorCount;
        private Double averageLatencyMs;

        // Metadata
        private Map<String, Object> metadata;
    }

    /**
     * Rate Limit
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RateLimit {
        private String rateLimitId;
        private String name;
        private String targetType; // GLOBAL, IP, USER, API_KEY
        private String targetValue;
        private Integer requestsPerMinute;
        private Integer requestsPerHour;
        private Integer requestsPerDay;
        private Boolean enabled;
        private LocalDateTime createdAt;
    }

    /**
     * Rate Limit Status
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RateLimitStatus {
        private String targetId;
        private Integer currentMinuteCount;
        private Integer currentHourCount;
        private Integer currentDayCount;
        private LocalDateTime windowStartAt;
        private LocalDateTime nextResetAt;
        private Boolean limitExceeded;
        private Integer remainingRequests;
    }

    /**
     * API Key
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiKey {
        private String keyId;
        private String key;
        private String name;
        private String userId;
        private Boolean enabled;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private LocalDateTime lastUsedAt;
        private Long totalRequests;
        private List<String> allowedRoutes;
        private List<String> roles;
        private Map<String, Object> metadata;
    }

    /**
     * Authorization Policy
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorizationPolicy {
        private String policyId;
        private String policyName;
        private String resource; // Route pattern
        private List<String> allowedRoles;
        private List<String> allowedUsers;
        private List<String> actions; // READ, WRITE, DELETE
        private Boolean enabled;
        private Integer priority;
    }

    /**
     * Backend
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Backend {
        private String backendId;
        private String backendName;
        private String host;
        private Integer port;
        private String protocol;
        private Boolean enabled;
        private Integer weight; // For weighted load balancing
        private BackendStatus status;
        private LocalDateTime lastHealthCheckAt;
        private Integer healthCheckIntervalSeconds;
        private Long totalRequests;
        private Long successfulRequests;
        private Long failedRequests;
        private Double averageLatencyMs;
    }

    /**
     * Backend Status
     */
    public enum BackendStatus {
        HEALTHY,
        UNHEALTHY,
        UNKNOWN
    }

    /**
     * Circuit Breaker State
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CircuitBreakerState {
        private String backendId;
        private CircuitBreakerStateEnum state;
        private Integer failureCount;
        private LocalDateTime stateChangedAt;
        private LocalDateTime nextRetryAt;
        private Long totalFailures;
        private Long totalSuccesses;
    }

    /**
     * Transformation
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Transformation {
        private String transformationId;
        private String transformationName;
        private String transformationType; // REQUEST, RESPONSE
        private String targetRoute; // Specific route or pattern
        private String transformScript; // Transformation logic
        private Boolean enabled;
        private Integer executionOrder;
    }

    /**
     * Gateway Metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GatewayMetrics {
        private Long totalRequests;
        private Long successfulRequests;
        private Long failedRequests;
        private Double successRate;
        private Double averageLatencyMs;
        private Double p50LatencyMs;
        private Double p95LatencyMs;
        private Double p99LatencyMs;
        private Double throughputPerSecond;
        private Long totalBytesReceived;
        private Long totalBytesSent;
        private Integer activeConnections;
        private LocalDateTime measuredAt;
    }

    /**
     * Request Log
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestLog {
        private String requestId;
        private String method;
        private String path;
        private String clientIp;
        private String userId;
        private String apiKeyId;
        private LocalDateTime timestamp;
        private Integer statusCode;
        private Long responseTimeMs;
        private Long requestSizeBytes;
        private Long responseSizeBytes;
        private String routeId;
        private String backendId;
        private Boolean cached;
        private String errorMessage;
    }

    /**
     * Gateway Event
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GatewayEvent {
        private String eventId;
        private String eventType;
        private String description;
        private LocalDateTime timestamp;
        private String userId;
        private Map<String, Object> eventData;
    }

    /**
     * Helper Methods
     */

    public void addRoute(Route route) {
        if (routes == null) {
            routes = new ArrayList<>();
        }
        routes.add(route);

        if (routeRegistry == null) {
            routeRegistry = new HashMap<>();
        }
        routeRegistry.put(route.getRouteId(), route);

        totalRoutes = (totalRoutes != null ? totalRoutes : 0) + 1;
        if (Boolean.TRUE.equals(route.getEnabled())) {
            activeRoutes = (activeRoutes != null ? activeRoutes : 0) + 1;
        }
    }

    public void addBackend(Backend backend) {
        if (backends == null) {
            backends = new ArrayList<>();
        }
        backends.add(backend);

        if (backendRegistry == null) {
            backendRegistry = new HashMap<>();
        }
        backendRegistry.put(backend.getBackendId(), backend);

        if (backend.getStatus() == BackendStatus.HEALTHY) {
            activeBackends = (activeBackends != null ? activeBackends : 0) + 1;
        }
    }

    public void addApiKey(ApiKey apiKey) {
        if (apiKeys == null) {
            apiKeys = new HashMap<>();
        }
        apiKeys.put(apiKey.getKeyId(), apiKey);

        if (Boolean.TRUE.equals(apiKey.getEnabled())) {
            activeApiKeys = (activeApiKeys != null ? activeApiKeys : 0) + 1;
        }
    }

    public void recordRequest(boolean success, long latencyMs) {
        totalRequests = (totalRequests != null ? totalRequests : 0L) + 1;
        lastRequestAt = LocalDateTime.now();

        if (success) {
            successfulRequests = (successfulRequests != null ? successfulRequests : 0L) + 1;
        } else {
            failedRequests = (failedRequests != null ? failedRequests : 0L) + 1;
        }

        updateSuccessRate();
    }

    private void updateSuccessRate() {
        if (totalRequests != null && totalRequests > 0) {
            long successful = successfulRequests != null ? successfulRequests : 0L;
            successRate = (double) successful / totalRequests * 100.0;
        } else {
            successRate = 0.0;
        }
    }

    public void recordCacheHit(boolean hit) {
        if (hit) {
            cacheHits = (cacheHits != null ? cacheHits : 0L) + 1;
        } else {
            cacheMisses = (cacheMisses != null ? cacheMisses : 0L) + 1;
        }

        updateCacheHitRate();
    }

    private void updateCacheHitRate() {
        long hits = cacheHits != null ? cacheHits : 0L;
        long misses = cacheMisses != null ? cacheMisses : 0L;
        long total = hits + misses;

        if (total > 0) {
            cacheHitRate = (double) hits / total * 100.0;
        } else {
            cacheHitRate = 0.0;
        }
    }

    public void recordEvent(String eventType, String description, String userId) {
        if (auditTrail == null) {
            auditTrail = new ArrayList<>();
        }

        GatewayEvent event = GatewayEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType(eventType)
                .description(description)
                .timestamp(LocalDateTime.now())
                .userId(userId)
                .eventData(new HashMap<>())
                .build();

        auditTrail.add(event);
        lastAuditAt = LocalDateTime.now();
    }

    public void startGateway() {
        status = GatewayStatus.ACTIVE;
        startedAt = LocalDateTime.now();
        isActive = true;
        recordEvent("GATEWAY_STARTED", "API Gateway started", null);
    }

    public void stopGateway() {
        status = GatewayStatus.SHUTDOWN;
        isActive = false;

        if (startedAt != null) {
            uptimeSeconds = java.time.Duration.between(startedAt, LocalDateTime.now()).getSeconds();
        }

        recordEvent("GATEWAY_STOPPED", "API Gateway stopped", null);
    }

    public Route getRoute(String routeId) {
        return routeRegistry != null ? routeRegistry.get(routeId) : null;
    }

    public Backend getBackend(String backendId) {
        return backendRegistry != null ? backendRegistry.get(backendId) : null;
    }

    public ApiKey getApiKey(String keyId) {
        return apiKeys != null ? apiKeys.get(keyId) : null;
    }

    public List<Route> getActiveRoutes() {
        if (routes == null) {
            return new ArrayList<>();
        }
        return routes.stream()
                .filter(r -> Boolean.TRUE.equals(r.getEnabled()))
                .toList();
    }

    public List<Backend> getHealthyBackends() {
        if (backends == null) {
            return new ArrayList<>();
        }
        return backends.stream()
                .filter(b -> b.getStatus() == BackendStatus.HEALTHY)
                .toList();
    }

    public boolean isHealthy() {
        return status == GatewayStatus.ACTIVE && Boolean.TRUE.equals(isActive);
    }

    public boolean requiresAttention() {
        return status == GatewayStatus.DEGRADED || status == GatewayStatus.ERROR;
    }
}
