package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Edge Computing & CDN DTO
 *
 * Manages edge computing and content delivery for distributed report processing.
 *
 * Features:
 * - Edge node management
 * - Content caching and distribution
 * - Geographic load balancing
 * - CDN integration
 * - Edge computing workloads
 * - Cache invalidation strategies
 * - Origin shield protection
 * - Edge analytics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 106 - Report Edge Computing & CDN
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportEdge {

    // Network Information
    private Long networkId;
    private String networkName;
    private String description;
    private NetworkStatus status;
    private Boolean isActive;

    // CDN Provider
    private CDNProvider provider;
    private String providerVersion;
    private List<String> enabledFeatures;
    private String distributionId;

    // Edge Nodes
    private List<EdgeNode> nodes;
    private Map<String, EdgeNode> nodeRegistry;
    private Integer totalNodes;
    private Integer activeNodes;
    private Integer totalRegions;

    // Cache Configuration
    private CacheConfiguration cacheConfig;
    private Long totalCacheSize;
    private Long usedCacheSize;
    private Double cacheHitRatio;

    // Content Distribution
    private List<CachedContent> cachedContent;
    private Map<String, CachedContent> contentRegistry;
    private Long totalCachedItems;
    private Long totalCacheHits;
    private Long totalCacheMisses;

    // Edge Workloads
    private List<EdgeWorkload> workloads;
    private Map<String, EdgeWorkload> workloadRegistry;
    private Long totalWorkloads;
    private Long completedWorkloads;

    // Invalidation
    private List<InvalidationRequest> invalidations;
    private Map<String, InvalidationRequest> invalidationRegistry;
    private Long totalInvalidations;

    // Origins
    private List<Origin> origins;
    private Map<String, Origin> originRegistry;
    private Boolean originShieldEnabled;

    // Routing
    private List<RoutingRule> routingRules;
    private GeoRoutingPolicy geoRoutingPolicy;
    private LoadBalancingStrategy loadBalancingStrategy;

    // SSL/TLS
    private SSLConfiguration sslConfig;
    private Boolean httpsEnabled;
    private Boolean forceHttps;

    // Metrics
    private EdgeMetrics metrics;
    private LocalDateTime lastMetricsUpdate;

    // Events
    private List<EdgeEvent> events;
    private LocalDateTime lastEventAt;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime deployedAt;
    private LocalDateTime lastSyncAt;

    /**
     * Network Status
     */
    public enum NetworkStatus {
        INITIALIZING,
        DEPLOYING,
        ACTIVE,
        SYNCING,
        DEGRADED,
        OFFLINE
    }

    /**
     * CDN Provider
     */
    public enum CDNProvider {
        CLOUDFLARE,
        AKAMAI,
        FASTLY,
        AWS_CLOUDFRONT,
        AZURE_CDN,
        GOOGLE_CLOUD_CDN,
        CLOUDINARY,
        CUSTOM
    }

    /**
     * Node Status
     */
    public enum NodeStatus {
        PROVISIONING,
        ONLINE,
        BUSY,
        DEGRADED,
        OFFLINE,
        MAINTENANCE
    }

    /**
     * Cache Status
     */
    public enum CacheStatus {
        HIT,
        MISS,
        STALE,
        REVALIDATED,
        BYPASS,
        EXPIRED
    }

    /**
     * Invalidation Status
     */
    public enum InvalidationStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }

    /**
     * Workload Type
     */
    public enum WorkloadType {
        COMPUTE,
        RENDERING,
        DATA_PROCESSING,
        IMAGE_OPTIMIZATION,
        VIDEO_TRANSCODING,
        API_GATEWAY,
        SERVERLESS_FUNCTION,
        EDGE_AI
    }

    /**
     * Cache Strategy
     */
    public enum CacheStrategy {
        TTL_BASED,
        EVENT_BASED,
        LRU,
        LFU,
        ADAPTIVE,
        NO_CACHE
    }

    /**
     * Geo Routing Policy
     */
    public enum GeoRoutingPolicy {
        NEAREST_NODE,
        REGION_BASED,
        LATENCY_BASED,
        GEOLOCATION,
        WEIGHTED,
        FAILOVER
    }

    /**
     * Load Balancing Strategy
     */
    public enum LoadBalancingStrategy {
        ROUND_ROBIN,
        LEAST_CONNECTIONS,
        LEAST_RESPONSE_TIME,
        IP_HASH,
        RANDOM,
        WEIGHTED_ROUND_ROBIN
    }

    /**
     * Edge Node
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EdgeNode {
        private String nodeId;
        private String nodeName;
        private NodeStatus status;
        private String region;
        private String city;
        private String country;
        private String continent;
        private Double latitude;
        private Double longitude;
        private String ipAddress;
        private Integer port;
        private Long totalMemory;
        private Long usedMemory;
        private Long totalStorage;
        private Long usedStorage;
        private Integer cpuCores;
        private Double cpuUsage;
        private Long bandwidthCapacity;
        private Long bandwidthUsed;
        private Integer activeConnections;
        private Long totalRequests;
        private Double averageResponseTimeMs;
        private LocalDateTime lastHealthCheck;
        private LocalDateTime onlineSince;
        private Map<String, String> tags;
        private Map<String, Object> metadata;
    }

    /**
     * Cached Content
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CachedContent {
        private String contentId;
        private String contentKey;
        private String contentType;
        private Long contentSize;
        private String originUrl;
        private CacheStatus cacheStatus;
        private LocalDateTime cachedAt;
        private LocalDateTime expiresAt;
        private Integer ttlSeconds;
        private String eTag;
        private Long hitCount;
        private Long missCount;
        private List<String> nodeIds;
        private String compressionType;
        private Long compressedSize;
        private Map<String, String> headers;
        private Map<String, Object> metadata;
    }

    /**
     * Cache Configuration
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheConfiguration {
        private CacheStrategy strategy;
        private Integer defaultTtlSeconds;
        private Integer maxTtlSeconds;
        private Long maxCacheSize;
        private Boolean enableCompression;
        private List<String> compressibleTypes;
        private Boolean enableQueryStringCaching;
        private List<String> cacheableHeaders;
        private List<String> ignoredHeaders;
        private Boolean respectOriginHeaders;
        private Map<String, Object> customRules;
    }

    /**
     * Edge Workload
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EdgeWorkload {
        private String workloadId;
        private String workloadName;
        private WorkloadType workloadType;
        private String nodeId;
        private String functionCode;
        private Map<String, Object> input;
        private Map<String, Object> output;
        private String status;
        private LocalDateTime submittedAt;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private Long executionTimeMs;
        private Long memoryUsed;
        private String errorMessage;
        private Map<String, Object> metadata;
    }

    /**
     * Invalidation Request
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvalidationRequest {
        private String invalidationId;
        private InvalidationStatus status;
        private List<String> contentKeys;
        private List<String> pathPatterns;
        private String reason;
        private LocalDateTime requestedAt;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private Integer itemsInvalidated;
        private List<String> affectedNodes;
        private Map<String, Object> metadata;
    }

    /**
     * Origin
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Origin {
        private String originId;
        private String originName;
        private String originUrl;
        private String protocol;
        private Integer port;
        private String path;
        private Boolean isPrimary;
        private Integer priority;
        private Integer weight;
        private Integer timeoutSeconds;
        private Integer retryAttempts;
        private Boolean enableOriginShield;
        private String shieldRegion;
        private Long totalRequests;
        private Long successfulRequests;
        private Long failedRequests;
        private Double averageResponseTimeMs;
        private LocalDateTime lastHealthCheck;
        private Boolean isHealthy;
        private Map<String, String> customHeaders;
        private Map<String, Object> metadata;
    }

    /**
     * Routing Rule
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoutingRule {
        private String ruleId;
        private String ruleName;
        private Integer priority;
        private String pathPattern;
        private List<String> methods;
        private Map<String, String> headers;
        private String targetOriginId;
        private String targetNodeId;
        private Boolean enabled;
        private Map<String, Object> conditions;
        private Map<String, Object> actions;
    }

    /**
     * SSL Configuration
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SSLConfiguration {
        private String certificateId;
        private String certificateProvider;
        private String domainName;
        private List<String> subjectAlternativeNames;
        private LocalDateTime issuedAt;
        private LocalDateTime expiresAt;
        private String tlsVersion;
        private List<String> cipherSuites;
        private Boolean enableHSTS;
        private Integer hstsMaxAge;
        private Boolean enableOCSPStapling;
        private Map<String, Object> metadata;
    }

    /**
     * Edge Metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EdgeMetrics {
        private Integer totalNodes;
        private Integer activeNodes;
        private Integer totalRegions;
        private Long totalRequests;
        private Long totalCacheHits;
        private Long totalCacheMisses;
        private Double cacheHitRatio;
        private Long totalBandwidth;
        private Double averageResponseTimeMs;
        private Double p95ResponseTimeMs;
        private Double p99ResponseTimeMs;
        private Long totalCachedItems;
        private Long totalCacheSize;
        private Long usedCacheSize;
        private Double cacheUtilization;
        private Long totalWorkloads;
        private Long completedWorkloads;
        private Double workloadSuccessRate;
        private Long totalInvalidations;
        private LocalDateTime measuredAt;
    }

    /**
     * Edge Event
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EdgeEvent {
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
     * Deploy network
     */
    public void deployNetwork() {
        this.status = NetworkStatus.DEPLOYING;
        this.isActive = true;
        this.deployedAt = LocalDateTime.now();

        recordEvent("NETWORK_DEPLOYED", "Edge network deployed", "NETWORK",
                networkId != null ? networkId.toString() : null);

        this.status = NetworkStatus.ACTIVE;
    }

    /**
     * Add edge node
     */
    public void addNode(EdgeNode node) {
        if (nodes == null) {
            nodes = new java.util.ArrayList<>();
        }
        nodes.add(node);

        if (nodeRegistry == null) {
            nodeRegistry = new java.util.HashMap<>();
        }
        nodeRegistry.put(node.getNodeId(), node);

        totalNodes = (totalNodes != null ? totalNodes : 0) + 1;
        if (node.getStatus() == NodeStatus.ONLINE) {
            activeNodes = (activeNodes != null ? activeNodes : 0) + 1;
        }

        recordEvent("NODE_ADDED", "Edge node added: " + node.getNodeName(),
                "NODE", node.getNodeId());
    }

    /**
     * Cache content
     */
    public void cacheContent(CachedContent content) {
        if (cachedContent == null) {
            cachedContent = new java.util.ArrayList<>();
        }
        cachedContent.add(content);

        if (contentRegistry == null) {
            contentRegistry = new java.util.HashMap<>();
        }
        contentRegistry.put(content.getContentKey(), content);

        totalCachedItems = (totalCachedItems != null ? totalCachedItems : 0L) + 1;
        usedCacheSize = (usedCacheSize != null ? usedCacheSize : 0L) + content.getContentSize();

        if (content.getCacheStatus() == CacheStatus.HIT) {
            totalCacheHits = (totalCacheHits != null ? totalCacheHits : 0L) + 1;
        } else if (content.getCacheStatus() == CacheStatus.MISS) {
            totalCacheMisses = (totalCacheMisses != null ? totalCacheMisses : 0L) + 1;
        }

        updateCacheHitRatio();
    }

    /**
     * Submit workload
     */
    public void submitWorkload(EdgeWorkload workload) {
        if (workloads == null) {
            workloads = new java.util.ArrayList<>();
        }
        workloads.add(workload);

        if (workloadRegistry == null) {
            workloadRegistry = new java.util.HashMap<>();
        }
        workloadRegistry.put(workload.getWorkloadId(), workload);

        totalWorkloads = (totalWorkloads != null ? totalWorkloads : 0L) + 1;

        recordEvent("WORKLOAD_SUBMITTED", "Edge workload submitted: " + workload.getWorkloadName(),
                "WORKLOAD", workload.getWorkloadId());
    }

    /**
     * Complete workload
     */
    public void completeWorkload(String workloadId, boolean success) {
        EdgeWorkload workload = workloadRegistry != null ? workloadRegistry.get(workloadId) : null;
        if (workload != null) {
            workload.setStatus(success ? "COMPLETED" : "FAILED");
            workload.setCompletedAt(LocalDateTime.now());

            if (workload.getStartedAt() != null) {
                workload.setExecutionTimeMs(
                    java.time.Duration.between(workload.getStartedAt(), workload.getCompletedAt()).toMillis()
                );
            }

            if (success) {
                completedWorkloads = (completedWorkloads != null ? completedWorkloads : 0L) + 1;
            }
        }
    }

    /**
     * Request invalidation
     */
    public void requestInvalidation(InvalidationRequest invalidation) {
        if (invalidations == null) {
            invalidations = new java.util.ArrayList<>();
        }
        invalidations.add(invalidation);

        if (invalidationRegistry == null) {
            invalidationRegistry = new java.util.HashMap<>();
        }
        invalidationRegistry.put(invalidation.getInvalidationId(), invalidation);

        totalInvalidations = (totalInvalidations != null ? totalInvalidations : 0L) + 1;

        recordEvent("INVALIDATION_REQUESTED", "Cache invalidation requested",
                "INVALIDATION", invalidation.getInvalidationId());
    }

    /**
     * Add origin
     */
    public void addOrigin(Origin origin) {
        if (origins == null) {
            origins = new java.util.ArrayList<>();
        }
        origins.add(origin);

        if (originRegistry == null) {
            originRegistry = new java.util.HashMap<>();
        }
        originRegistry.put(origin.getOriginId(), origin);

        recordEvent("ORIGIN_ADDED", "Origin added: " + origin.getOriginName(),
                "ORIGIN", origin.getOriginId());
    }

    /**
     * Add routing rule
     */
    public void addRoutingRule(RoutingRule rule) {
        if (routingRules == null) {
            routingRules = new java.util.ArrayList<>();
        }
        routingRules.add(rule);
    }

    /**
     * Update cache hit ratio
     */
    private void updateCacheHitRatio() {
        long hits = totalCacheHits != null ? totalCacheHits : 0L;
        long misses = totalCacheMisses != null ? totalCacheMisses : 0L;
        long total = hits + misses;

        if (total > 0) {
            cacheHitRatio = (double) hits / total;
        } else {
            cacheHitRatio = 0.0;
        }
    }

    /**
     * Get node by ID
     */
    public EdgeNode getNode(String nodeId) {
        return nodeRegistry != null ? nodeRegistry.get(nodeId) : null;
    }

    /**
     * Get content by key
     */
    public CachedContent getContent(String contentKey) {
        return contentRegistry != null ? contentRegistry.get(contentKey) : null;
    }

    /**
     * Get origin by ID
     */
    public Origin getOrigin(String originId) {
        return originRegistry != null ? originRegistry.get(originId) : null;
    }

    /**
     * Record event
     */
    public void recordEvent(String eventType, String description, String resourceType, String resourceId) {
        if (events == null) {
            events = new java.util.ArrayList<>();
        }

        EdgeEvent event = EdgeEvent.builder()
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
     * Check if network is healthy
     */
    public boolean isHealthy() {
        return status == NetworkStatus.ACTIVE || status == NetworkStatus.SYNCING;
    }

    /**
     * Get online nodes
     */
    public List<EdgeNode> getOnlineNodes() {
        if (nodes == null) {
            return new java.util.ArrayList<>();
        }
        return nodes.stream()
                .filter(n -> n.getStatus() == NodeStatus.ONLINE)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get nodes by region
     */
    public List<EdgeNode> getNodesByRegion(String region) {
        if (nodes == null) {
            return new java.util.ArrayList<>();
        }
        return nodes.stream()
                .filter(n -> region.equals(n.getRegion()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get cached content by type
     */
    public List<CachedContent> getCachedContentByType(String contentType) {
        if (cachedContent == null) {
            return new java.util.ArrayList<>();
        }
        return cachedContent.stream()
                .filter(c -> contentType.equals(c.getContentType()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get primary origin
     */
    public Origin getPrimaryOrigin() {
        if (origins == null) {
            return null;
        }
        return origins.stream()
                .filter(o -> Boolean.TRUE.equals(o.getIsPrimary()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get healthy origins
     */
    public List<Origin> getHealthyOrigins() {
        if (origins == null) {
            return new java.util.ArrayList<>();
        }
        return origins.stream()
                .filter(o -> Boolean.TRUE.equals(o.getIsHealthy()))
                .collect(java.util.stream.Collectors.toList());
    }
}
