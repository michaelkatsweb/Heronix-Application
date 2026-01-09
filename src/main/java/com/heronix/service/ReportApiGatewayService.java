package com.heronix.service;

import com.heronix.dto.ReportApiGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Report API Gateway Service
 *
 * Manages API gateway operations for report services.
 *
 * Features:
 * - Route management
 * - Rate limiting
 * - Authentication and authorization
 * - Load balancing
 * - Circuit breaking
 * - Caching
 * - Monitoring
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 94 - Report API Gateway & Integration
 */
@Service
@Slf4j
public class ReportApiGatewayService {

    private final Map<Long, ReportApiGateway> gateways = new ConcurrentHashMap<>();
    private Long nextGatewayId = 1L;

    /**
     * Create gateway
     */
    public ReportApiGateway createGateway(ReportApiGateway gateway) {
        synchronized (this) {
            gateway.setGatewayId(nextGatewayId++);
            gateway.setCreatedAt(LocalDateTime.now());
            gateway.setStatus(ReportApiGateway.GatewayStatus.INITIALIZING);
            gateway.setIsActive(false);
            gateway.setTotalRoutes(0);
            gateway.setActiveRoutes(0);
            gateway.setActiveApiKeys(0);
            gateway.setActiveBackends(0);
            gateway.setTotalRequests(0L);
            gateway.setSuccessfulRequests(0L);
            gateway.setFailedRequests(0L);
            gateway.setSuccessRate(0.0);
            gateway.setCacheHits(0L);
            gateway.setCacheMisses(0L);
            gateway.setCacheHitRate(0.0);

            // Set defaults
            if (gateway.getPort() == null) {
                gateway.setPort(8080);
            }

            if (gateway.getProtocol() == null) {
                gateway.setProtocol("HTTPS");
            }

            if (gateway.getSslEnabled() == null) {
                gateway.setSslEnabled(true);
            }

            if (gateway.getRateLimitingEnabled() == null) {
                gateway.setRateLimitingEnabled(true);
            }

            if (gateway.getDefaultRateLimit() == null) {
                gateway.setDefaultRateLimit(1000); // 1000 requests per minute
            }

            if (gateway.getRateLimitStrategy() == null) {
                gateway.setRateLimitStrategy(ReportApiGateway.RateLimitStrategy.SLIDING_WINDOW);
            }

            if (gateway.getAuthenticationEnabled() == null) {
                gateway.setAuthenticationEnabled(true);
            }

            if (gateway.getAuthorizationEnabled() == null) {
                gateway.setAuthorizationEnabled(true);
            }

            if (gateway.getCachingEnabled() == null) {
                gateway.setCachingEnabled(true);
            }

            if (gateway.getCacheProvider() == null) {
                gateway.setCacheProvider("In-Memory");
            }

            if (gateway.getCacheTtlSeconds() == null) {
                gateway.setCacheTtlSeconds(300); // 5 minutes
            }

            if (gateway.getLoadBalancingEnabled() == null) {
                gateway.setLoadBalancingEnabled(true);
            }

            if (gateway.getLoadBalancingStrategy() == null) {
                gateway.setLoadBalancingStrategy(ReportApiGateway.LoadBalancingStrategy.ROUND_ROBIN);
            }

            if (gateway.getCircuitBreakerEnabled() == null) {
                gateway.setCircuitBreakerEnabled(true);
            }

            if (gateway.getCircuitBreakerThreshold() == null) {
                gateway.setCircuitBreakerThreshold(5);
            }

            if (gateway.getCircuitBreakerTimeoutSeconds() == null) {
                gateway.setCircuitBreakerTimeoutSeconds(60);
            }

            if (gateway.getTransformationEnabled() == null) {
                gateway.setTransformationEnabled(false);
            }

            if (gateway.getVersioningEnabled() == null) {
                gateway.setVersioningEnabled(true);
            }

            if (gateway.getCurrentVersion() == null) {
                gateway.setCurrentVersion("v1");
            }

            if (gateway.getVersioningStrategy() == null) {
                gateway.setVersioningStrategy(ReportApiGateway.VersioningStrategy.URL_PATH);
            }

            if (gateway.getCorsEnabled() == null) {
                gateway.setCorsEnabled(true);
            }

            if (gateway.getCsrfProtectionEnabled() == null) {
                gateway.setCsrfProtectionEnabled(true);
            }

            if (gateway.getMaxRequestSizeBytes() == null) {
                gateway.setMaxRequestSizeBytes(10485760); // 10MB
            }

            if (gateway.getConfigurationLocked() == null) {
                gateway.setConfigurationLocked(false);
            }

            // Initialize collections
            if (gateway.getRoutes() == null) {
                gateway.setRoutes(new ArrayList<>());
            }

            if (gateway.getRouteRegistry() == null) {
                gateway.setRouteRegistry(new HashMap<>());
            }

            if (gateway.getRateLimits() == null) {
                gateway.setRateLimits(new ArrayList<>());
            }

            if (gateway.getRateLimitStatuses() == null) {
                gateway.setRateLimitStatuses(new HashMap<>());
            }

            if (gateway.getAuthenticationMethods() == null) {
                gateway.setAuthenticationMethods(Arrays.asList("API_KEY", "JWT"));
            }

            if (gateway.getApiKeys() == null) {
                gateway.setApiKeys(new HashMap<>());
            }

            if (gateway.getAuthorizationPolicies() == null) {
                gateway.setAuthorizationPolicies(new ArrayList<>());
            }

            if (gateway.getRolePermissions() == null) {
                gateway.setRolePermissions(new HashMap<>());
            }

            if (gateway.getBackends() == null) {
                gateway.setBackends(new ArrayList<>());
            }

            if (gateway.getBackendRegistry() == null) {
                gateway.setBackendRegistry(new HashMap<>());
            }

            if (gateway.getCircuitBreakerStates() == null) {
                gateway.setCircuitBreakerStates(new HashMap<>());
            }

            if (gateway.getTransformations() == null) {
                gateway.setTransformations(new ArrayList<>());
            }

            if (gateway.getRequestLogs() == null) {
                gateway.setRequestLogs(new ArrayList<>());
            }

            if (gateway.getSupportedVersions() == null) {
                gateway.setSupportedVersions(Arrays.asList("v1", "v2"));
            }

            if (gateway.getAllowedOrigins() == null) {
                gateway.setAllowedOrigins(new ArrayList<>());
            }

            if (gateway.getAllowedMethods() == null) {
                gateway.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            }

            if (gateway.getAllowedHeaders() == null) {
                gateway.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization"));
            }

            if (gateway.getAuditTrail() == null) {
                gateway.setAuditTrail(new ArrayList<>());
            }

            if (gateway.getConfiguration() == null) {
                gateway.setConfiguration(new HashMap<>());
            }

            gateways.put(gateway.getGatewayId(), gateway);

            log.info("Created gateway {} - {} (version: {})",
                    gateway.getGatewayId(), gateway.getGatewayName(), gateway.getVersion());

            return gateway;
        }
    }

    /**
     * Get gateway
     */
    public Optional<ReportApiGateway> getGateway(Long gatewayId) {
        return Optional.ofNullable(gateways.get(gatewayId));
    }

    /**
     * Start gateway
     */
    public void startGateway(Long gatewayId) {
        ReportApiGateway gateway = gateways.get(gatewayId);
        if (gateway == null) {
            throw new IllegalArgumentException("Gateway not found: " + gatewayId);
        }

        gateway.startGateway();

        log.info("Started gateway {}", gatewayId);
    }

    /**
     * Stop gateway
     */
    public void stopGateway(Long gatewayId) {
        ReportApiGateway gateway = gateways.get(gatewayId);
        if (gateway == null) {
            throw new IllegalArgumentException("Gateway not found: " + gatewayId);
        }

        gateway.stopGateway();

        log.info("Stopped gateway {}", gatewayId);
    }

    /**
     * Add route
     */
    public ReportApiGateway.Route addRoute(Long gatewayId, String routeName, String path,
                                           ReportApiGateway.RouteType routeType, String targetUrl,
                                           List<String> methods) {
        ReportApiGateway gateway = gateways.get(gatewayId);
        if (gateway == null) {
            throw new IllegalArgumentException("Gateway not found: " + gatewayId);
        }

        ReportApiGateway.Route route = ReportApiGateway.Route.builder()
                .routeId(UUID.randomUUID().toString())
                .routeName(routeName)
                .path(path)
                .routeType(routeType)
                .targetUrl(targetUrl)
                .methods(methods != null ? methods : Arrays.asList("GET", "POST"))
                .enabled(true)
                .priority(1)
                .authenticationRequired(true)
                .allowedRoles(new ArrayList<>())
                .cacheable(false)
                .timeoutSeconds(30)
                .retryAttempts(3)
                .requestCount(0L)
                .successCount(0L)
                .errorCount(0L)
                .averageLatencyMs(0.0)
                .metadata(new HashMap<>())
                .build();

        gateway.addRoute(route);

        log.info("Added route {} to gateway {}: {} -> {}",
                route.getRouteId(), gatewayId, path, targetUrl);

        return route;
    }

    /**
     * Add backend
     */
    public ReportApiGateway.Backend addBackend(Long gatewayId, String backendName,
                                               String host, Integer port, String protocol) {
        ReportApiGateway gateway = gateways.get(gatewayId);
        if (gateway == null) {
            throw new IllegalArgumentException("Gateway not found: " + gatewayId);
        }

        ReportApiGateway.Backend backend = ReportApiGateway.Backend.builder()
                .backendId(UUID.randomUUID().toString())
                .backendName(backendName)
                .host(host)
                .port(port)
                .protocol(protocol != null ? protocol : "HTTP")
                .enabled(true)
                .weight(1)
                .status(ReportApiGateway.BackendStatus.HEALTHY)
                .lastHealthCheckAt(LocalDateTime.now())
                .healthCheckIntervalSeconds(30)
                .totalRequests(0L)
                .successfulRequests(0L)
                .failedRequests(0L)
                .averageLatencyMs(0.0)
                .build();

        gateway.addBackend(backend);

        // Initialize circuit breaker state
        ReportApiGateway.CircuitBreakerState cbState = ReportApiGateway.CircuitBreakerState.builder()
                .backendId(backend.getBackendId())
                .state(ReportApiGateway.CircuitBreakerStateEnum.CLOSED)
                .failureCount(0)
                .stateChangedAt(LocalDateTime.now())
                .totalFailures(0L)
                .totalSuccesses(0L)
                .build();

        gateway.getCircuitBreakerStates().put(backend.getBackendId(), cbState);

        log.info("Added backend {} to gateway {}: {}:{} ({})",
                backend.getBackendId(), gatewayId, host, port, protocol);

        return backend;
    }

    /**
     * Create API key
     */
    public ReportApiGateway.ApiKey createApiKey(Long gatewayId, String name, String userId,
                                                List<String> roles) {
        ReportApiGateway gateway = gateways.get(gatewayId);
        if (gateway == null) {
            throw new IllegalArgumentException("Gateway not found: " + gatewayId);
        }

        String key = UUID.randomUUID().toString().replace("-", "");

        ReportApiGateway.ApiKey apiKey = ReportApiGateway.ApiKey.builder()
                .keyId(UUID.randomUUID().toString())
                .key(key)
                .name(name)
                .userId(userId)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusYears(1))
                .totalRequests(0L)
                .allowedRoutes(new ArrayList<>())
                .roles(roles != null ? roles : new ArrayList<>())
                .metadata(new HashMap<>())
                .build();

        gateway.addApiKey(apiKey);

        log.info("Created API key {} for gateway {}: {} (user: {})",
                apiKey.getKeyId(), gatewayId, name, userId);

        return apiKey;
    }

    /**
     * Record request
     */
    public void recordRequest(Long gatewayId, String method, String path, String clientIp,
                              int statusCode, long responseTimeMs, String routeId) {
        ReportApiGateway gateway = gateways.get(gatewayId);
        if (gateway == null) {
            return;
        }

        boolean success = statusCode >= 200 && statusCode < 300;
        gateway.recordRequest(success, responseTimeMs);

        // Update route metrics
        if (routeId != null) {
            ReportApiGateway.Route route = gateway.getRoute(routeId);
            if (route != null) {
                route.setRequestCount((route.getRequestCount() != null ? route.getRequestCount() : 0L) + 1);
                if (success) {
                    route.setSuccessCount((route.getSuccessCount() != null ? route.getSuccessCount() : 0L) + 1);
                } else {
                    route.setErrorCount((route.getErrorCount() != null ? route.getErrorCount() : 0L) + 1);
                }
            }
        }

        log.debug("Recorded request for gateway {}: {} {} - {} ({}ms)",
                gatewayId, method, path, statusCode, responseTimeMs);
    }

    /**
     * Check rate limit
     */
    public boolean checkRateLimit(Long gatewayId, String targetType, String targetValue) {
        ReportApiGateway gateway = gateways.get(gatewayId);
        if (gateway == null || !Boolean.TRUE.equals(gateway.getRateLimitingEnabled())) {
            return true; // Allow if rate limiting disabled
        }

        String targetId = targetType + ":" + targetValue;
        ReportApiGateway.RateLimitStatus status = gateway.getRateLimitStatuses()
                .computeIfAbsent(targetId, k -> ReportApiGateway.RateLimitStatus.builder()
                        .targetId(targetId)
                        .currentMinuteCount(0)
                        .currentHourCount(0)
                        .currentDayCount(0)
                        .windowStartAt(LocalDateTime.now())
                        .limitExceeded(false)
                        .build());

        // Simplified rate limit check (in real implementation, would handle window resets)
        status.setCurrentMinuteCount(status.getCurrentMinuteCount() + 1);

        Integer limit = gateway.getDefaultRateLimit();
        boolean exceeded = status.getCurrentMinuteCount() > limit;
        status.setLimitExceeded(exceeded);
        status.setRemainingRequests(Math.max(0, limit - status.getCurrentMinuteCount()));

        return !exceeded;
    }

    /**
     * Validate API key
     */
    public boolean validateApiKey(Long gatewayId, String key) {
        ReportApiGateway gateway = gateways.get(gatewayId);
        if (gateway == null || gateway.getApiKeys() == null) {
            return false;
        }

        for (ReportApiGateway.ApiKey apiKey : gateway.getApiKeys().values()) {
            if (apiKey.getKey().equals(key) && Boolean.TRUE.equals(apiKey.getEnabled())) {
                // Check expiration
                if (apiKey.getExpiresAt() != null && LocalDateTime.now().isAfter(apiKey.getExpiresAt())) {
                    return false;
                }

                apiKey.setLastUsedAt(LocalDateTime.now());
                apiKey.setTotalRequests((apiKey.getTotalRequests() != null ? apiKey.getTotalRequests() : 0L) + 1);
                return true;
            }
        }

        return false;
    }

    /**
     * Update backend status
     */
    public void updateBackendStatus(Long gatewayId, String backendId,
                                    ReportApiGateway.BackendStatus status) {
        ReportApiGateway gateway = gateways.get(gatewayId);
        if (gateway == null) {
            throw new IllegalArgumentException("Gateway not found: " + gatewayId);
        }

        ReportApiGateway.Backend backend = gateway.getBackend(backendId);
        if (backend == null) {
            throw new IllegalArgumentException("Backend not found: " + backendId);
        }

        backend.setStatus(status);
        backend.setLastHealthCheckAt(LocalDateTime.now());

        log.info("Updated backend {} status to {} in gateway {}", backendId, status, gatewayId);
    }

    /**
     * Update circuit breaker
     */
    public void updateCircuitBreaker(Long gatewayId, String backendId, boolean requestSuccess) {
        ReportApiGateway gateway = gateways.get(gatewayId);
        if (gateway == null || !Boolean.TRUE.equals(gateway.getCircuitBreakerEnabled())) {
            return;
        }

        ReportApiGateway.CircuitBreakerState state = gateway.getCircuitBreakerStates().get(backendId);
        if (state == null) {
            return;
        }

        if (requestSuccess) {
            state.setTotalSuccesses((state.getTotalSuccesses() != null ? state.getTotalSuccesses() : 0L) + 1);
            state.setFailureCount(0);

            // Close circuit if it was half-open
            if (state.getState() == ReportApiGateway.CircuitBreakerStateEnum.HALF_OPEN) {
                state.setState(ReportApiGateway.CircuitBreakerStateEnum.CLOSED);
                state.setStateChangedAt(LocalDateTime.now());
            }
        } else {
            state.setTotalFailures((state.getTotalFailures() != null ? state.getTotalFailures() : 0L) + 1);
            state.setFailureCount(state.getFailureCount() + 1);

            // Open circuit if threshold reached
            if (state.getFailureCount() >= gateway.getCircuitBreakerThreshold()) {
                state.setState(ReportApiGateway.CircuitBreakerStateEnum.OPEN);
                state.setStateChangedAt(LocalDateTime.now());
                state.setNextRetryAt(LocalDateTime.now().plusSeconds(gateway.getCircuitBreakerTimeoutSeconds()));

                log.warn("Circuit breaker opened for backend {} in gateway {}", backendId, gatewayId);
            }
        }
    }

    /**
     * Update metrics
     */
    public void updateMetrics(Long gatewayId, Long totalRequests, Long successfulRequests,
                             Long failedRequests, Double averageLatencyMs, Double throughputPerSecond) {
        ReportApiGateway gateway = gateways.get(gatewayId);
        if (gateway == null) {
            throw new IllegalArgumentException("Gateway not found: " + gatewayId);
        }

        double successRate = totalRequests > 0 ?
                (double) successfulRequests / totalRequests * 100.0 : 0.0;

        ReportApiGateway.GatewayMetrics metrics = ReportApiGateway.GatewayMetrics.builder()
                .totalRequests(totalRequests)
                .successfulRequests(successfulRequests)
                .failedRequests(failedRequests)
                .successRate(successRate)
                .averageLatencyMs(averageLatencyMs)
                .p50LatencyMs(averageLatencyMs * 0.8)
                .p95LatencyMs(averageLatencyMs * 1.5)
                .p99LatencyMs(averageLatencyMs * 2.0)
                .throughputPerSecond(throughputPerSecond)
                .totalBytesReceived(0L)
                .totalBytesSent(0L)
                .activeConnections(0)
                .measuredAt(LocalDateTime.now())
                .build();

        gateway.setMetrics(metrics);

        log.debug("Updated metrics for gateway {}: {:.1f}% success, {:.2f}ms avg",
                gatewayId, successRate, averageLatencyMs);
    }

    /**
     * Delete gateway
     */
    public void deleteGateway(Long gatewayId) {
        ReportApiGateway gateway = gateways.get(gatewayId);
        if (gateway != null && gateway.isHealthy()) {
            stopGateway(gatewayId);
        }

        ReportApiGateway removed = gateways.remove(gatewayId);
        if (removed != null) {
            log.info("Deleted gateway {}", gatewayId);
        }
    }

    /**
     * Get statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalGateways", gateways.size());

        long activeGateways = gateways.values().stream()
                .filter(ReportApiGateway::isHealthy)
                .count();

        long totalRoutes = gateways.values().stream()
                .mapToLong(g -> g.getTotalRoutes() != null ? g.getTotalRoutes() : 0L)
                .sum();

        long totalBackends = gateways.values().stream()
                .mapToLong(g -> g.getBackends() != null ? g.getBackends().size() : 0L)
                .sum();

        long totalApiKeys = gateways.values().stream()
                .mapToLong(g -> g.getApiKeys() != null ? g.getApiKeys().size() : 0L)
                .sum();

        long totalRequests = gateways.values().stream()
                .mapToLong(g -> g.getTotalRequests() != null ? g.getTotalRequests() : 0L)
                .sum();

        long totalSuccessfulRequests = gateways.values().stream()
                .mapToLong(g -> g.getSuccessfulRequests() != null ? g.getSuccessfulRequests() : 0L)
                .sum();

        stats.put("activeGateways", activeGateways);
        stats.put("totalRoutes", totalRoutes);
        stats.put("totalBackends", totalBackends);
        stats.put("totalApiKeys", totalApiKeys);
        stats.put("totalRequests", totalRequests);
        stats.put("totalSuccessfulRequests", totalSuccessfulRequests);

        return stats;
    }
}
