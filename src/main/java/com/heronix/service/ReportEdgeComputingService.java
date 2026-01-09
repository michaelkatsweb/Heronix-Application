package com.heronix.service;

import com.heronix.dto.ReportEdgeComputing;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Report Edge Computing Service
 *
 * Manages edge computing and CDN for report services.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 98 - Report Edge Computing & CDN
 */
@Service
@Slf4j
public class ReportEdgeComputingService {

    private final Map<Long, ReportEdgeComputing> networks = new ConcurrentHashMap<>();
    private Long nextNetworkId = 1L;

    /**
     * Create network
     */
    public ReportEdgeComputing createNetwork(ReportEdgeComputing network) {
        synchronized (this) {
            network.setNetworkId(nextNetworkId++);
        }

        network.setStatus(ReportEdgeComputing.NetworkStatus.INITIALIZING);
        network.setCreatedAt(LocalDateTime.now());
        network.setIsActive(false);

        // Initialize collections
        if (network.getEdgeNodes() == null) {
            network.setEdgeNodes(new ArrayList<>());
        }
        if (network.getContents() == null) {
            network.setContents(new ArrayList<>());
        }
        if (network.getRequests() == null) {
            network.setRequests(new ArrayList<>());
        }
        if (network.getEvents() == null) {
            network.setEvents(new ArrayList<>());
        }

        // Initialize registries
        network.setNodeRegistry(new ConcurrentHashMap<>());
        network.setContentRegistry(new ConcurrentHashMap<>());
        network.setNodesByRegion(new ConcurrentHashMap<>());
        network.setTrafficByRegion(new ConcurrentHashMap<>());

        // Initialize counters
        network.setTotalNodes(0);
        network.setActiveNodes(0);
        network.setOfflineNodes(0);
        network.setTotalContents(0L);
        network.setCachedContents(0L);

        networks.put(network.getNetworkId(), network);
        log.info("Created edge network: {} (ID: {})", network.getNetworkName(), network.getNetworkId());

        return network;
    }

    /**
     * Get network
     */
    public Optional<ReportEdgeComputing> getNetwork(Long networkId) {
        return Optional.ofNullable(networks.get(networkId));
    }

    /**
     * Start network
     */
    public void startNetwork(Long networkId) {
        ReportEdgeComputing network = networks.get(networkId);
        if (network == null) {
            throw new IllegalArgumentException("Network not found: " + networkId);
        }

        network.startNetwork();
        log.info("Started edge network: {}", networkId);
    }

    /**
     * Stop network
     */
    public void stopNetwork(Long networkId) {
        ReportEdgeComputing network = networks.get(networkId);
        if (network == null) {
            throw new IllegalArgumentException("Network not found: " + networkId);
        }

        network.stopNetwork();
        log.info("Stopped edge network: {}", networkId);
    }

    /**
     * Register edge node
     */
    public ReportEdgeComputing.EdgeNode registerNode(Long networkId, String nodeName,
                                                      String region, String country, String city) {
        ReportEdgeComputing network = networks.get(networkId);
        if (network == null) {
            throw new IllegalArgumentException("Network not found: " + networkId);
        }

        ReportEdgeComputing.EdgeNode node = ReportEdgeComputing.EdgeNode.builder()
                .nodeId(UUID.randomUUID().toString())
                .nodeName(nodeName)
                .status(ReportEdgeComputing.EdgeNodeStatus.PROVISIONING)
                .region(region)
                .country(country)
                .city(city)
                .totalRequests(0L)
                .successfulRequests(0L)
                .failedRequests(0L)
                .cacheHits(0L)
                .cacheMisses(0L)
                .bandwidthUsedGb(0L)
                .currentConnections(0)
                .healthy(false)
                .tags(new HashMap<>())
                .metadata(new HashMap<>())
                .provisionedAt(LocalDateTime.now())
                .lastHeartbeatAt(LocalDateTime.now())
                .build();

        network.registerNode(node);
        log.info("Registered edge node {} in region {} for network {}", nodeName, region, networkId);

        return node;
    }

    /**
     * Update node status
     */
    public void updateNodeStatus(Long networkId, String nodeId, ReportEdgeComputing.EdgeNodeStatus status) {
        ReportEdgeComputing network = networks.get(networkId);
        if (network == null) {
            throw new IllegalArgumentException("Network not found: " + networkId);
        }

        network.updateNodeStatus(nodeId, status);
        log.info("Updated node {} status to {} in network {}", nodeId, status, networkId);
    }

    /**
     * Register content
     */
    public ReportEdgeComputing.Content registerContent(Long networkId, String contentPath,
                                                        String contentType, String originUrl) {
        ReportEdgeComputing network = networks.get(networkId);
        if (network == null) {
            throw new IllegalArgumentException("Network not found: " + networkId);
        }

        ReportEdgeComputing.Content content = ReportEdgeComputing.Content.builder()
                .contentId(UUID.randomUUID().toString())
                .contentPath(contentPath)
                .contentType(contentType)
                .originUrl(originUrl)
                .cached(false)
                .totalRequests(0L)
                .cacheHits(0L)
                .cacheMisses(0L)
                .cachedNodes(new ArrayList<>())
                .distributedRegions(new ArrayList<>())
                .tags(new HashMap<>())
                .metadata(new HashMap<>())
                .uploadedAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .build();

        network.registerContent(content);
        log.info("Registered content {} in network {}", contentPath, networkId);

        return content;
    }

    /**
     * Cache content
     */
    public void cacheContent(Long networkId, String contentId, List<String> nodeIds) {
        ReportEdgeComputing network = networks.get(networkId);
        if (network == null) {
            throw new IllegalArgumentException("Network not found: " + networkId);
        }

        ReportEdgeComputing.Content content = network.getContentRegistry().get(contentId);
        if (content == null) {
            throw new IllegalArgumentException("Content not found: " + contentId);
        }

        content.setCached(true);
        content.setCachedNodes(nodeIds != null ? nodeIds : new ArrayList<>());
        content.setCacheExpiresAt(LocalDateTime.now().plusSeconds(
                network.getDefaultCacheTtlSeconds() != null ? network.getDefaultCacheTtlSeconds() : 3600
        ));

        network.recordEvent("CONTENT_CACHED",
                "Content cached: " + content.getContentPath(),
                "CONTENT", contentId);

        log.info("Cached content {} on {} nodes in network {}", contentId,
                nodeIds != null ? nodeIds.size() : 0, networkId);
    }

    /**
     * Record request
     */
    public void recordRequest(Long networkId, String clientIp, String method, String path,
                               String nodeId, Integer statusCode, Long responseTimeMs,
                               ReportEdgeComputing.CacheStatus cacheStatus) {
        ReportEdgeComputing network = networks.get(networkId);
        if (network == null) {
            throw new IllegalArgumentException("Network not found: " + networkId);
        }

        ReportEdgeComputing.EdgeNode node = network.getNode(nodeId);
        String region = node != null ? node.getRegion() : "UNKNOWN";

        ReportEdgeComputing.Request request = ReportEdgeComputing.Request.builder()
                .requestId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .clientIp(clientIp)
                .method(method)
                .path(path)
                .edgeNodeId(nodeId)
                .edgeNodeRegion(region)
                .statusCode(statusCode)
                .responseTimeMs(responseTimeMs)
                .cacheStatus(cacheStatus)
                .blocked(false)
                .headers(new HashMap<>())
                .metadata(new HashMap<>())
                .build();

        network.recordRequest(request);

        log.debug("Recorded request to {} from {} via node {} in network {}: {} ({} ms, cache: {})",
                path, clientIp, nodeId, networkId, statusCode, responseTimeMs, cacheStatus);
    }

    /**
     * Invalidate cache
     */
    public void invalidateCache(Long networkId, String contentId) {
        ReportEdgeComputing network = networks.get(networkId);
        if (network == null) {
            throw new IllegalArgumentException("Network not found: " + networkId);
        }

        ReportEdgeComputing.Content content = network.getContentRegistry().get(contentId);
        if (content == null) {
            throw new IllegalArgumentException("Content not found: " + contentId);
        }

        content.setCached(false);
        content.setCachedNodes(new ArrayList<>());
        content.setCacheExpiresAt(null);

        network.recordEvent("CACHE_INVALIDATED",
                "Cache invalidated: " + content.getContentPath(),
                "CACHE", contentId);

        log.info("Invalidated cache for content {} in network {}", contentId, networkId);
    }

    /**
     * Update metrics
     */
    public void updateMetrics(Long networkId) {
        ReportEdgeComputing network = networks.get(networkId);
        if (network == null) {
            throw new IllegalArgumentException("Network not found: " + networkId);
        }

        int totalNodes = network.getTotalNodes() != null ? network.getTotalNodes() : 0;
        int activeNodes = network.getActiveNodes() != null ? network.getActiveNodes().size() : 0;

        // Calculate request metrics
        long totalRequests = network.getRequests() != null ? network.getRequests().size() : 0L;
        long successfulRequests = network.getRequests() != null ?
                network.getRequests().stream()
                        .filter(r -> r.getStatusCode() >= 200 && r.getStatusCode() < 400)
                        .count() : 0L;
        long failedRequests = totalRequests - successfulRequests;

        double successRate = totalRequests > 0 ?
                (successfulRequests * 100.0 / totalRequests) : 0.0;

        // Calculate latency
        double avgLatency = network.getRequests() != null ?
                network.getRequests().stream()
                        .filter(r -> r.getResponseTimeMs() != null)
                        .mapToLong(ReportEdgeComputing.Request::getResponseTimeMs)
                        .average()
                        .orElse(0.0) : 0.0;

        // Calculate cache metrics
        long totalCacheHits = network.getRequests() != null ?
                network.getRequests().stream()
                        .filter(r -> r.getCacheStatus() == ReportEdgeComputing.CacheStatus.HIT)
                        .count() : 0L;

        long totalCacheMisses = network.getRequests() != null ?
                network.getRequests().stream()
                        .filter(r -> r.getCacheStatus() == ReportEdgeComputing.CacheStatus.MISS)
                        .count() : 0L;

        double cacheHitRate = (totalCacheHits + totalCacheMisses) > 0 ?
                (totalCacheHits * 100.0 / (totalCacheHits + totalCacheMisses)) : 0.0;

        ReportEdgeComputing.NetworkMetrics metrics = ReportEdgeComputing.NetworkMetrics.builder()
                .totalNodes(totalNodes)
                .activeNodes(activeNodes)
                .totalRequests(totalRequests)
                .successfulRequests(successfulRequests)
                .failedRequests(failedRequests)
                .successRate(successRate)
                .averageLatencyMs(avgLatency)
                .totalCacheHits(totalCacheHits)
                .totalCacheMisses(totalCacheMisses)
                .cacheHitRate(cacheHitRate)
                .measuredAt(LocalDateTime.now())
                .build();

        network.setMetrics(metrics);

        log.debug("Updated metrics for network {}: {} nodes, {} requests, {:.1f}% cache hit rate",
                networkId, totalNodes, totalRequests, cacheHitRate);
    }

    /**
     * Delete network
     */
    public void deleteNetwork(Long networkId) {
        ReportEdgeComputing network = networks.get(networkId);
        if (network != null && network.isHealthy()) {
            stopNetwork(networkId);
        }

        ReportEdgeComputing removed = networks.remove(networkId);
        if (removed != null) {
            log.info("Deleted edge network {}", networkId);
        }
    }

    /**
     * Get statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalNetworks", networks.size());

        long activeNetworks = networks.values().stream()
                .filter(ReportEdgeComputing::isHealthy)
                .count();

        long totalNodes = networks.values().stream()
                .mapToLong(n -> n.getTotalNodes() != null ? n.getTotalNodes() : 0L)
                .sum();

        long activeNodes = networks.values().stream()
                .mapToLong(n -> n.getActiveNodes() != null ? n.getActiveNodes().size() : 0L)
                .sum();

        long totalContents = networks.values().stream()
                .mapToLong(n -> n.getTotalContents() != null ? n.getTotalContents() : 0L)
                .sum();

        long cachedContents = networks.values().stream()
                .mapToLong(n -> n.getCachedContents() != null ? n.getCachedContents() : 0L)
                .sum();

        stats.put("activeNetworks", activeNetworks);
        stats.put("totalNodes", totalNodes);
        stats.put("activeNodes", activeNodes);
        stats.put("totalContents", totalContents);
        stats.put("cachedContents", cachedContents);

        log.debug("Generated edge computing statistics: {} networks, {} nodes, {} contents",
                networks.size(), totalNodes, totalContents);

        return stats;
    }
}
