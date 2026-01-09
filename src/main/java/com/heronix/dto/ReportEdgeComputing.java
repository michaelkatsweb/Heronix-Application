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
 * Report Edge Computing DTO
 *
 * Represents edge computing and CDN for report services.
 *
 * Features:
 * - Edge node management
 * - Content delivery network
 * - Distributed caching
 * - Geographic distribution
 * - Low-latency access
 * - Traffic routing
 * - Cache invalidation
 * - Edge analytics
 *
 * Edge Node Status:
 * - PROVISIONING - Node being provisioned
 * - ACTIVE - Node active and serving
 * - DEGRADED - Node degraded performance
 * - OFFLINE - Node offline
 * - MAINTENANCE - Under maintenance
 * - FAILED - Node failed
 *
 * Cache Status:
 * - HIT - Cache hit
 * - MISS - Cache miss
 * - STALE - Cache stale
 * - EXPIRED - Cache expired
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 98 - Report Edge Computing & CDN
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportEdgeComputing {

    private Long networkId;
    private String networkName;
    private String description;
    private String version;

    // Network status
    private NetworkStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private Boolean isActive;

    // Provider
    private CdnProvider cdnProvider;
    private String accountId;
    private Map<String, String> providerConfig;

    // Edge nodes
    private List<EdgeNode> edgeNodes;
    private Integer totalNodes;
    private Integer activeNodes;
    private Integer offlineNodes;
    private Map<String, EdgeNode> nodeRegistry;

    // Geographic distribution
    private List<String> regions;
    private Map<String, List<EdgeNode>> nodesByRegion;
    private String primaryRegion;

    // Content
    private List<Content> contents;
    private Long totalContents;
    private Long cachedContents;
    private Map<String, Content> contentRegistry;

    // Cache
    private Boolean cachingEnabled;
    private CacheStrategy cacheStrategy;
    private Integer defaultCacheTtlSeconds;
    private Long totalCacheSize;
    private Long maxCacheSize;
    private List<CacheEntry> cacheEntries;

    // Traffic routing
    private Boolean trafficRoutingEnabled;
    private RoutingStrategy routingStrategy;
    private List<RoutingRule> routingRules;
    private Map<String, Long> trafficByRegion;

    // SSL/TLS
    private Boolean sslEnabled;
    private String certificateArn;
    private LocalDateTime certificateExpiry;

    // Compression
    private Boolean compressionEnabled;
    private List<String> compressionFormats; // GZIP, BROTLI

    // Security
    private Boolean wafEnabled;
    private Boolean ddosProtectionEnabled;
    private List<SecurityRule> securityRules;
    private List<String> allowedOrigins;
    private List<String> blockedIps;

    // Monitoring
    private Boolean monitoringEnabled;
    private List<Request> requests;
    private LocalDateTime lastRequestAt;

    // Performance
    private PerformanceMetrics performanceMetrics;
    private Map<String, NodeMetrics> nodeMetrics;

    // Analytics
    private Boolean analyticsEnabled;
    private AnalyticsData analyticsData;
    private Map<String, Long> requestsByCountry;
    private Map<String, Long> requestsByDevice;

    // Metrics
    private NetworkMetrics metrics;
    private List<EdgeNodeMetrics> edgeNodeMetricsList;

    // Events
    private List<NetworkEvent> events;
    private LocalDateTime lastEventAt;

    // Configuration
    private Map<String, Object> configuration;
    private Boolean configurationLocked;

    /**
     * Network Status
     */
    public enum NetworkStatus {
        INITIALIZING,   // Network initializing
        ACTIVE,         // Active and operational
        DEGRADED,       // Degraded performance
        MAINTENANCE,    // Under maintenance
        ERROR,          // Error state
        SHUTDOWN        // Shutting down
    }

    /**
     * Edge Node Status
     */
    public enum EdgeNodeStatus {
        PROVISIONING,   // Node being provisioned
        ACTIVE,         // Node active and serving
        DEGRADED,       // Node degraded performance
        OFFLINE,        // Node offline
        MAINTENANCE,    // Under maintenance
        FAILED          // Node failed
    }

    /**
     * CDN Provider
     */
    public enum CdnProvider {
        CLOUDFLARE,     // Cloudflare
        AKAMAI,         // Akamai
        FASTLY,         // Fastly
        AWS_CLOUDFRONT, // AWS CloudFront
        AZURE_CDN,      // Azure CDN
        GOOGLE_CDN      // Google Cloud CDN
    }

    /**
     * Cache Strategy
     */
    public enum CacheStrategy {
        LRU,            // Least Recently Used
        LFU,            // Least Frequently Used
        FIFO,           // First In First Out
        TTL,            // Time To Live
        ADAPTIVE        // Adaptive caching
    }

    /**
     * Routing Strategy
     */
    public enum RoutingStrategy {
        GEOGRAPHIC,     // Geographic proximity
        LATENCY,        // Lowest latency
        ROUND_ROBIN,    // Round robin
        WEIGHTED,       // Weighted distribution
        FAILOVER        // Failover routing
    }

    /**
     * Cache Status
     */
    public enum CacheStatus {
        HIT,            // Cache hit
        MISS,           // Cache miss
        STALE,          // Cache stale
        EXPIRED,        // Cache expired
        BYPASS          // Cache bypass
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
        private EdgeNodeStatus status;

        // Location
        private String region;
        private String country;
        private String city;
        private Double latitude;
        private Double longitude;

        // Network
        private String ipAddress;
        private String hostname;
        private Integer port;

        // Capacity
        private Long totalStorageGb;
        private Long usedStorageGb;
        private Long availableStorageGb;
        private Integer maxConnections;
        private Integer currentConnections;

        // Performance
        private Double cpuUsagePercent;
        private Double memoryUsagePercent;
        private Double networkUtilizationPercent;
        private Long averageLatencyMs;

        // Cache
        private Long cacheSize;
        private Long cacheHits;
        private Long cacheMisses;
        private Double cacheHitRate;

        // Traffic
        private Long totalRequests;
        private Long successfulRequests;
        private Long failedRequests;
        private Long bandwidthUsedGb;

        // Health
        private Boolean healthy;
        private String healthStatus;
        private LocalDateTime lastHealthCheckAt;

        // Lifecycle
        private LocalDateTime provisionedAt;
        private LocalDateTime lastHeartbeatAt;

        // Metadata
        private Map<String, String> tags;
        private Map<String, Object> metadata;
    }

    /**
     * Content
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        private String contentId;
        private String contentPath;
        private String contentType; // REPORT, ASSET, IMAGE, VIDEO
        private String mimeType;

        // File info
        private String originUrl;
        private Long sizeBytes;
        private String etag;
        private String checksum;

        // Cache
        private Boolean cached;
        private Integer cacheTtlSeconds;
        private LocalDateTime cacheExpiresAt;
        private List<String> cachedNodes;

        // Distribution
        private List<String> distributedRegions;
        private Integer replicationFactor;

        // Access
        private Long totalRequests;
        private Long cacheHits;
        private Long cacheMisses;
        private LocalDateTime lastAccessedAt;

        // Compression
        private Boolean compressed;
        private String compressionFormat;
        private Long compressedSizeBytes;

        // Security
        private Boolean requiresAuth;
        private List<String> allowedRoles;

        // Lifecycle
        private LocalDateTime uploadedAt;
        private LocalDateTime lastModifiedAt;

        // Metadata
        private Map<String, String> tags;
        private Map<String, Object> metadata;
    }

    /**
     * Cache Entry
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheEntry {
        private String entryId;
        private String key;
        private String contentId;
        private String nodeId;

        // Data
        private Long sizeBytes;
        private String etag;

        // TTL
        private Integer ttlSeconds;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private LocalDateTime lastAccessedAt;

        // Stats
        private Long accessCount;
        private CacheStatus status;

        // Metadata
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

        // Conditions
        private String pathPattern;
        private List<String> sourceRegions;
        private List<String> sourceCountries;

        // Targets
        private List<String> targetNodeIds;
        private List<String> targetRegions;

        // Configuration
        private RoutingStrategy strategy;
        private Map<String, Integer> weights;

        // Status
        private Boolean enabled;
        private LocalDateTime createdAt;
    }

    /**
     * Security Rule
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecurityRule {
        private String ruleId;
        private String ruleName;
        private String ruleType; // IP_WHITELIST, IP_BLACKLIST, GEO_BLOCK, RATE_LIMIT

        // Configuration
        private List<String> ipAddresses;
        private List<String> countries;
        private Integer rateLimit;
        private String rateLimitWindow; // PER_SECOND, PER_MINUTE, PER_HOUR

        // Action
        private String action; // ALLOW, BLOCK, CHALLENGE

        // Status
        private Boolean enabled;
        private LocalDateTime createdAt;
    }

    /**
     * Request
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String requestId;
        private LocalDateTime timestamp;

        // Client
        private String clientIp;
        private String country;
        private String region;
        private String city;
        private String userAgent;
        private String deviceType; // DESKTOP, MOBILE, TABLET

        // Request
        private String method;
        private String path;
        private String protocol;
        private Map<String, String> headers;

        // Routing
        private String edgeNodeId;
        private String edgeNodeRegion;

        // Response
        private Integer statusCode;
        private Long responseSizeBytes;
        private Long responseTimeMs;

        // Cache
        private CacheStatus cacheStatus;
        private String cacheKey;

        // Security
        private Boolean blocked;
        private String blockReason;

        // Metadata
        private Map<String, Object> metadata;
    }

    /**
     * Performance Metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceMetrics {
        private Double averageLatencyMs;
        private Double p50LatencyMs;
        private Double p95LatencyMs;
        private Double p99LatencyMs;
        private Long totalRequests;
        private Double requestsPerSecond;
        private Long bandwidthUsedGb;
        private Double cacheHitRate;
        private LocalDateTime measuredAt;
    }

    /**
     * Node Metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NodeMetrics {
        private String nodeId;
        private String region;
        private Long requests;
        private Double averageLatencyMs;
        private Long cacheHits;
        private Long cacheMisses;
        private Double cacheHitRate;
        private Long bandwidthUsedGb;
        private LocalDateTime measuredAt;
    }

    /**
     * Analytics Data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalyticsData {
        private Long totalRequests;
        private Long uniqueVisitors;
        private Map<String, Long> topPaths;
        private Map<String, Long> topCountries;
        private Map<String, Long> topDevices;
        private Map<String, Long> statusCodes;
        private Double averageResponseTimeMs;
        private LocalDateTime periodStart;
        private LocalDateTime periodEnd;
    }

    /**
     * Network Metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NetworkMetrics {
        private Integer totalNodes;
        private Integer activeNodes;
        private Long totalRequests;
        private Long successfulRequests;
        private Long failedRequests;
        private Double successRate;
        private Double averageLatencyMs;
        private Long totalBandwidthGb;
        private Long totalCacheHits;
        private Long totalCacheMisses;
        private Double cacheHitRate;
        private LocalDateTime measuredAt;
    }

    /**
     * Edge Node Metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EdgeNodeMetrics {
        private String nodeId;
        private String nodeName;
        private String region;
        private Long requests;
        private Long successCount;
        private Long errorCount;
        private Double successRate;
        private Double averageLatencyMs;
        private Long cacheHits;
        private Long cacheMisses;
        private Double cacheHitRate;
        private Long bandwidthUsedGb;
        private LocalDateTime measuredAt;
    }

    /**
     * Network Event
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NetworkEvent {
        private String eventId;
        private String eventType;
        private String description;
        private LocalDateTime timestamp;
        private String resourceType; // NODE, CONTENT, CACHE
        private String resourceId;
        private Map<String, Object> eventData;
    }

    /**
     * Helper Methods
     */

    public void registerNode(EdgeNode node) {
        if (edgeNodes == null) {
            edgeNodes = new ArrayList<>();
        }
        edgeNodes.add(node);

        if (nodeRegistry == null) {
            nodeRegistry = new HashMap<>();
        }
        nodeRegistry.put(node.getNodeId(), node);

        totalNodes = (totalNodes != null ? totalNodes : 0) + 1;
        if (node.getStatus() == EdgeNodeStatus.ACTIVE) {
            activeNodes = (activeNodes != null ? activeNodes : 0) + 1;
        } else if (node.getStatus() == EdgeNodeStatus.OFFLINE) {
            offlineNodes = (offlineNodes != null ? offlineNodes : 0) + 1;
        }

        // Add to region mapping
        if (nodesByRegion == null) {
            nodesByRegion = new HashMap<>();
        }
        nodesByRegion.computeIfAbsent(node.getRegion(), k -> new ArrayList<>()).add(node);

        recordEvent("NODE_REGISTERED", "Edge node registered: " + node.getNodeName(),
                "NODE", node.getNodeId());
    }

    public void updateNodeStatus(String nodeId, EdgeNodeStatus newStatus) {
        EdgeNode node = nodeRegistry != null ? nodeRegistry.get(nodeId) : null;
        if (node == null) {
            return;
        }

        EdgeNodeStatus oldStatus = node.getStatus();
        node.setStatus(newStatus);
        node.setLastHeartbeatAt(LocalDateTime.now());

        // Update counts
        if (oldStatus == EdgeNodeStatus.ACTIVE && activeNodes != null && activeNodes > 0) {
            activeNodes--;
        } else if (oldStatus == EdgeNodeStatus.OFFLINE && offlineNodes != null && offlineNodes > 0) {
            offlineNodes--;
        }

        if (newStatus == EdgeNodeStatus.ACTIVE) {
            activeNodes = (activeNodes != null ? activeNodes : 0) + 1;
        } else if (newStatus == EdgeNodeStatus.OFFLINE) {
            offlineNodes = (offlineNodes != null ? offlineNodes : 0) + 1;
        }

        recordEvent("NODE_STATUS_UPDATED",
                "Node status updated: " + node.getNodeName() + " -> " + newStatus,
                "NODE", nodeId);
    }

    public void registerContent(Content content) {
        if (contents == null) {
            contents = new ArrayList<>();
        }
        contents.add(content);

        if (contentRegistry == null) {
            contentRegistry = new HashMap<>();
        }
        contentRegistry.put(content.getContentId(), content);

        totalContents = (totalContents != null ? totalContents : 0L) + 1;
        if (Boolean.TRUE.equals(content.getCached())) {
            cachedContents = (cachedContents != null ? cachedContents : 0L) + 1;
        }

        recordEvent("CONTENT_REGISTERED", "Content registered: " + content.getContentPath(),
                "CONTENT", content.getContentId());
    }

    public void recordRequest(Request request) {
        if (requests == null) {
            requests = new ArrayList<>();
        }
        requests.add(request);

        lastRequestAt = LocalDateTime.now();

        // Update node stats
        if (request.getEdgeNodeId() != null) {
            EdgeNode node = nodeRegistry != null ? nodeRegistry.get(request.getEdgeNodeId()) : null;
            if (node != null) {
                node.setTotalRequests((node.getTotalRequests() != null ? node.getTotalRequests() : 0L) + 1);
                if (request.getStatusCode() >= 200 && request.getStatusCode() < 400) {
                    node.setSuccessfulRequests((node.getSuccessfulRequests() != null ?
                            node.getSuccessfulRequests() : 0L) + 1);
                } else {
                    node.setFailedRequests((node.getFailedRequests() != null ?
                            node.getFailedRequests() : 0L) + 1);
                }

                // Update cache stats
                if (request.getCacheStatus() == CacheStatus.HIT) {
                    node.setCacheHits((node.getCacheHits() != null ? node.getCacheHits() : 0L) + 1);
                } else if (request.getCacheStatus() == CacheStatus.MISS) {
                    node.setCacheMisses((node.getCacheMisses() != null ? node.getCacheMisses() : 0L) + 1);
                }
            }
        }

        // Update traffic by region
        if (trafficByRegion == null) {
            trafficByRegion = new HashMap<>();
        }
        String region = request.getEdgeNodeRegion() != null ? request.getEdgeNodeRegion() : "UNKNOWN";
        trafficByRegion.put(region, trafficByRegion.getOrDefault(region, 0L) + 1);
    }

    public void recordEvent(String eventType, String description, String resourceType, String resourceId) {
        if (events == null) {
            events = new ArrayList<>();
        }

        NetworkEvent event = NetworkEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType(eventType)
                .description(description)
                .timestamp(LocalDateTime.now())
                .resourceType(resourceType)
                .resourceId(resourceId)
                .eventData(new HashMap<>())
                .build();

        events.add(event);
        lastEventAt = LocalDateTime.now();
    }

    public void startNetwork() {
        status = NetworkStatus.ACTIVE;
        startedAt = LocalDateTime.now();
        isActive = true;
        recordEvent("NETWORK_STARTED", "Edge network started", "NETWORK", networkId.toString());
    }

    public void stopNetwork() {
        status = NetworkStatus.SHUTDOWN;
        isActive = false;
        recordEvent("NETWORK_STOPPED", "Edge network stopped", "NETWORK", networkId.toString());
    }

    public EdgeNode getNode(String nodeId) {
        return nodeRegistry != null ? nodeRegistry.get(nodeId) : null;
    }

    public List<EdgeNode> getActiveNodes() {
        if (edgeNodes == null) {
            return new ArrayList<>();
        }
        return edgeNodes.stream()
                .filter(n -> n.getStatus() == EdgeNodeStatus.ACTIVE)
                .toList();
    }

    public List<EdgeNode> getNodesByRegion(String region) {
        if (nodesByRegion == null) {
            return new ArrayList<>();
        }
        return nodesByRegion.getOrDefault(region, new ArrayList<>());
    }

    public boolean isHealthy() {
        return status == NetworkStatus.ACTIVE && Boolean.TRUE.equals(isActive);
    }

    public boolean requiresAttention() {
        return status == NetworkStatus.DEGRADED || status == NetworkStatus.ERROR ||
               (offlineNodes != null && offlineNodes > 0);
    }
}
