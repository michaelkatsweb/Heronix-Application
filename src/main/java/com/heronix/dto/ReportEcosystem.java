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
 * Report Ecosystem DTO
 *
 * Represents the comprehensive reporting ecosystem and integration hub.
 *
 * Features:
 * - Unified report management
 * - External system integration
 * - Component orchestration
 * - Ecosystem health monitoring
 * - Integration registry
 * - Service mesh management
 * - Event bus coordination
 * - Resource optimization
 *
 * Ecosystem Status:
 * - INITIALIZING - Ecosystem starting up
 * - HEALTHY - All components operational
 * - DEGRADED - Some components failing
 * - CRITICAL - Critical failures
 * - MAINTENANCE - Under maintenance
 * - SHUTDOWN - Shutting down
 *
 * Integration Type:
 * - REST_API - REST API integration
 * - WEBHOOK - Webhook integration
 * - MESSAGE_QUEUE - Message queue integration
 * - DATABASE - Database integration
 * - FILE_SYSTEM - File system integration
 * - CLOUD_SERVICE - Cloud service integration
 * - CUSTOM - Custom integration
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 90 - Report Ecosystem & Integration Hub
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportEcosystem {

    private Long ecosystemId;
    private String ecosystemName;
    private String version;
    private String description;

    // Ecosystem status
    private EcosystemStatus status;
    private EcosystemHealth health;
    private LocalDateTime lastHealthCheckAt;
    private Integer healthScore;

    // Components
    private List<Component> components;
    private Integer totalComponents;
    private Integer activeComponents;
    private Integer failedComponents;
    private Map<String, Component> componentRegistry;

    // Integrations
    private List<Integration> integrations;
    private Integer totalIntegrations;
    private Integer activeIntegrations;
    private Integer failedIntegrations;
    private Map<String, Integration> integrationRegistry;

    // Services
    private List<Service> services;
    private Integer totalServices;
    private Integer runningServices;
    private Map<String, Service> serviceRegistry;

    // Event bus
    private Boolean eventBusEnabled;
    private List<Event> events;
    private Long totalEvents;
    private Long eventsProcessed;
    private Long eventsFailed;
    private Map<String, Long> eventsByType;

    // Resource management
    private ResourceMetrics resourceMetrics;
    private Boolean autoScalingEnabled;
    private Integer maxConcurrentReports;
    private Integer currentConcurrentReports;

    // Performance
    private PerformanceMetrics performanceMetrics;
    private Boolean performanceMonitoringEnabled;
    private Double averageResponseTimeMs;
    private Double p95ResponseTimeMs;

    // Configuration
    private Map<String, Object> configuration;
    private Boolean configurationLocked;
    private LocalDateTime configurationUpdatedAt;

    // Metadata
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime startedAt;
    private Long uptimeSeconds;

    /**
     * Ecosystem Status
     */
    public enum EcosystemStatus {
        INITIALIZING,  // Ecosystem starting up
        HEALTHY,       // All components operational
        DEGRADED,      // Some components failing
        CRITICAL,      // Critical failures
        MAINTENANCE,   // Under maintenance
        SHUTDOWN       // Shutting down
    }

    /**
     * Ecosystem Health
     */
    public enum EcosystemHealth {
        EXCELLENT,  // 90-100%
        GOOD,       // 70-89%
        FAIR,       // 50-69%
        POOR,       // 30-49%
        CRITICAL    // 0-29%
    }

    /**
     * Integration Type
     */
    public enum IntegrationType {
        REST_API,       // REST API integration
        WEBHOOK,        // Webhook integration
        MESSAGE_QUEUE,  // Message queue integration
        DATABASE,       // Database integration
        FILE_SYSTEM,    // File system integration
        CLOUD_SERVICE,  // Cloud service integration
        CUSTOM          // Custom integration
    }

    /**
     * Component Status
     */
    public enum ComponentStatus {
        INITIALIZING,
        RUNNING,
        PAUSED,
        FAILED,
        STOPPED
    }

    /**
     * Service Status
     */
    public enum ServiceStatus {
        STARTING,
        RUNNING,
        STOPPING,
        STOPPED,
        ERROR
    }

    /**
     * Component
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Component {
        private String componentId;
        private String componentName;
        private String componentType;
        private String version;
        private ComponentStatus status;
        private LocalDateTime startedAt;
        private LocalDateTime lastHeartbeatAt;
        private Integer healthScore;
        private Map<String, Object> metrics;
        private List<String> dependencies;
        private Boolean enabled;
        private Map<String, Object> configuration;
    }

    /**
     * Integration
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Integration {
        private String integrationId;
        private String integrationName;
        private IntegrationType type;
        private String endpoint;
        private Boolean enabled;
        private ComponentStatus status;
        private String apiKey;
        private Map<String, String> headers;
        private Integer timeoutSeconds;
        private Integer retryAttempts;
        private LocalDateTime lastCallAt;
        private Long totalCalls;
        private Long successfulCalls;
        private Long failedCalls;
        private Double successRate;
        private Map<String, Object> metadata;
    }

    /**
     * Service
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Service {
        private String serviceId;
        private String serviceName;
        private String serviceType;
        private ServiceStatus status;
        private String host;
        private Integer port;
        private LocalDateTime startedAt;
        private Long requestCount;
        private Long errorCount;
        private Double averageLatencyMs;
        private Map<String, Object> configuration;
    }

    /**
     * Event
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Event {
        private String eventId;
        private String eventType;
        private String source;
        private String target;
        private LocalDateTime timestamp;
        private Map<String, Object> payload;
        private Boolean processed;
        private LocalDateTime processedAt;
        private String processingResult;
        private Integer retryCount;
    }

    /**
     * Resource Metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceMetrics {
        private Double cpuUsagePercent;
        private Long memoryUsedBytes;
        private Long memoryTotalBytes;
        private Double memoryUsagePercent;
        private Long diskUsedBytes;
        private Long diskTotalBytes;
        private Double diskUsagePercent;
        private Integer activeThreads;
        private Integer maxThreads;
        private Integer queuedTasks;
        private LocalDateTime measuredAt;
    }

    /**
     * Performance Metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceMetrics {
        private Long totalRequests;
        private Long successfulRequests;
        private Long failedRequests;
        private Double successRate;
        private Double averageResponseTimeMs;
        private Double p50ResponseTimeMs;
        private Double p95ResponseTimeMs;
        private Double p99ResponseTimeMs;
        private Double maxResponseTimeMs;
        private Double throughputPerSecond;
        private LocalDateTime measuredAt;
    }

    /**
     * Helper Methods
     */

    public void addComponent(Component component) {
        if (components == null) {
            components = new ArrayList<>();
        }
        components.add(component);

        if (componentRegistry == null) {
            componentRegistry = new HashMap<>();
        }
        componentRegistry.put(component.getComponentId(), component);

        totalComponents = (totalComponents != null ? totalComponents : 0) + 1;
        if (component.getStatus() == ComponentStatus.RUNNING) {
            activeComponents = (activeComponents != null ? activeComponents : 0) + 1;
        } else if (component.getStatus() == ComponentStatus.FAILED) {
            failedComponents = (failedComponents != null ? failedComponents : 0) + 1;
        }
    }

    public void addIntegration(Integration integration) {
        if (integrations == null) {
            integrations = new ArrayList<>();
        }
        integrations.add(integration);

        if (integrationRegistry == null) {
            integrationRegistry = new HashMap<>();
        }
        integrationRegistry.put(integration.getIntegrationId(), integration);

        totalIntegrations = (totalIntegrations != null ? totalIntegrations : 0) + 1;
        if (integration.getStatus() == ComponentStatus.RUNNING) {
            activeIntegrations = (activeIntegrations != null ? activeIntegrations : 0) + 1;
        } else if (integration.getStatus() == ComponentStatus.FAILED) {
            failedIntegrations = (failedIntegrations != null ? failedIntegrations : 0) + 1;
        }
    }

    public void addService(Service service) {
        if (services == null) {
            services = new ArrayList<>();
        }
        services.add(service);

        if (serviceRegistry == null) {
            serviceRegistry = new HashMap<>();
        }
        serviceRegistry.put(service.getServiceId(), service);

        totalServices = (totalServices != null ? totalServices : 0) + 1;
        if (service.getStatus() == ServiceStatus.RUNNING) {
            runningServices = (runningServices != null ? runningServices : 0) + 1;
        }
    }

    public void publishEvent(Event event) {
        if (events == null) {
            events = new ArrayList<>();
        }
        events.add(event);

        totalEvents = (totalEvents != null ? totalEvents : 0L) + 1;

        if (eventsByType == null) {
            eventsByType = new HashMap<>();
        }
        eventsByType.put(event.getEventType(),
                eventsByType.getOrDefault(event.getEventType(), 0L) + 1);
    }

    public void processEvent(String eventId, boolean success) {
        if (events != null) {
            for (Event event : events) {
                if (event.getEventId().equals(eventId)) {
                    event.setProcessed(true);
                    event.setProcessedAt(LocalDateTime.now());
                    event.setProcessingResult(success ? "SUCCESS" : "FAILED");

                    if (success) {
                        eventsProcessed = (eventsProcessed != null ? eventsProcessed : 0L) + 1;
                    } else {
                        eventsFailed = (eventsFailed != null ? eventsFailed : 0L) + 1;
                    }
                    break;
                }
            }
        }
    }

    public void updateComponentStatus(String componentId, ComponentStatus status) {
        Component component = componentRegistry != null ? componentRegistry.get(componentId) : null;
        if (component != null) {
            ComponentStatus oldStatus = component.getStatus();
            component.setStatus(status);
            component.setLastHeartbeatAt(LocalDateTime.now());

            // Update counts
            if (oldStatus == ComponentStatus.RUNNING && activeComponents != null && activeComponents > 0) {
                activeComponents--;
            } else if (oldStatus == ComponentStatus.FAILED && failedComponents != null && failedComponents > 0) {
                failedComponents--;
            }

            if (status == ComponentStatus.RUNNING) {
                activeComponents = (activeComponents != null ? activeComponents : 0) + 1;
            } else if (status == ComponentStatus.FAILED) {
                failedComponents = (failedComponents != null ? failedComponents : 0) + 1;
            }

            updateEcosystemStatus();
        }
    }

    public void updateIntegrationStatus(String integrationId, ComponentStatus status) {
        Integration integration = integrationRegistry != null ? integrationRegistry.get(integrationId) : null;
        if (integration != null) {
            ComponentStatus oldStatus = integration.getStatus();
            integration.setStatus(status);

            // Update counts
            if (oldStatus == ComponentStatus.RUNNING && activeIntegrations != null && activeIntegrations > 0) {
                activeIntegrations--;
            } else if (oldStatus == ComponentStatus.FAILED && failedIntegrations != null && failedIntegrations > 0) {
                failedIntegrations--;
            }

            if (status == ComponentStatus.RUNNING) {
                activeIntegrations = (activeIntegrations != null ? activeIntegrations : 0) + 1;
            } else if (status == ComponentStatus.FAILED) {
                failedIntegrations = (failedIntegrations != null ? failedIntegrations : 0) + 1;
            }

            updateEcosystemStatus();
        }
    }

    public void recordIntegrationCall(String integrationId, boolean success) {
        Integration integration = integrationRegistry != null ? integrationRegistry.get(integrationId) : null;
        if (integration != null) {
            integration.setLastCallAt(LocalDateTime.now());
            integration.setTotalCalls((integration.getTotalCalls() != null ? integration.getTotalCalls() : 0L) + 1);

            if (success) {
                integration.setSuccessfulCalls((integration.getSuccessfulCalls() != null ? integration.getSuccessfulCalls() : 0L) + 1);
            } else {
                integration.setFailedCalls((integration.getFailedCalls() != null ? integration.getFailedCalls() : 0L) + 1);
            }

            // Calculate success rate
            if (integration.getTotalCalls() != null && integration.getTotalCalls() > 0) {
                integration.setSuccessRate(
                        (integration.getSuccessfulCalls() != null ? integration.getSuccessfulCalls().doubleValue() : 0.0) /
                                integration.getTotalCalls() * 100.0
                );
            }
        }
    }

    public void updateEcosystemStatus() {
        if (totalComponents == null || totalComponents == 0) {
            status = EcosystemStatus.INITIALIZING;
            health = EcosystemHealth.FAIR;
            healthScore = 50;
            return;
        }

        int activeCount = activeComponents != null ? activeComponents : 0;
        int failedCount = failedComponents != null ? failedComponents : 0;

        double healthPercentage = (double) activeCount / totalComponents * 100.0;
        healthScore = (int) healthPercentage;

        // Determine health
        if (healthPercentage >= 90) {
            health = EcosystemHealth.EXCELLENT;
            status = EcosystemStatus.HEALTHY;
        } else if (healthPercentage >= 70) {
            health = EcosystemHealth.GOOD;
            status = EcosystemStatus.HEALTHY;
        } else if (healthPercentage >= 50) {
            health = EcosystemHealth.FAIR;
            status = EcosystemStatus.DEGRADED;
        } else if (healthPercentage >= 30) {
            health = EcosystemHealth.POOR;
            status = EcosystemStatus.DEGRADED;
        } else {
            health = EcosystemHealth.CRITICAL;
            status = EcosystemStatus.CRITICAL;
        }

        lastHealthCheckAt = LocalDateTime.now();
    }

    public void updateResourceMetrics(ResourceMetrics metrics) {
        this.resourceMetrics = metrics;
        metrics.setMeasuredAt(LocalDateTime.now());
    }

    public void updatePerformanceMetrics(PerformanceMetrics metrics) {
        this.performanceMetrics = metrics;
        metrics.setMeasuredAt(LocalDateTime.now());

        averageResponseTimeMs = metrics.getAverageResponseTimeMs();
        p95ResponseTimeMs = metrics.getP95ResponseTimeMs();
    }

    public void calculateUptime() {
        if (startedAt != null) {
            uptimeSeconds = java.time.Duration.between(startedAt, LocalDateTime.now()).getSeconds();
        }
    }

    public Component getComponent(String componentId) {
        return componentRegistry != null ? componentRegistry.get(componentId) : null;
    }

    public Integration getIntegration(String integrationId) {
        return integrationRegistry != null ? integrationRegistry.get(integrationId) : null;
    }

    public Service getService(String serviceId) {
        return serviceRegistry != null ? serviceRegistry.get(serviceId) : null;
    }

    public List<Component> getFailedComponents() {
        if (components == null) {
            return new ArrayList<>();
        }
        return components.stream()
                .filter(c -> c.getStatus() == ComponentStatus.FAILED)
                .toList();
    }

    public List<Integration> getActiveIntegrationsList() {
        if (integrations == null) {
            return new ArrayList<>();
        }
        return integrations.stream()
                .filter(i -> i.getStatus() == ComponentStatus.RUNNING)
                .toList();
    }

    public boolean isHealthy() {
        return status == EcosystemStatus.HEALTHY;
    }

    public boolean requiresAttention() {
        return status == EcosystemStatus.DEGRADED || status == EcosystemStatus.CRITICAL;
    }
}
