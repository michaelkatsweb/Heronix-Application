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
 * Report Streaming DTO
 *
 * Represents real-time report streaming and incremental updates.
 *
 * Features:
 * - Live data streaming
 * - Incremental updates
 * - Subscription management
 * - Event-driven delivery
 * - Backpressure handling
 * - Stream buffering
 * - Connection management
 * - Data transformation pipelines
 *
 * Stream Status:
 * - INITIALIZING - Stream initializing
 * - CONNECTING - Connecting to source
 * - ACTIVE - Actively streaming
 * - PAUSED - Temporarily paused
 * - BUFFERING - Buffering data
 * - THROTTLED - Rate limited
 * - ERROR - Error state
 * - CLOSED - Stream closed
 *
 * Stream Type:
 * - LIVE - Live real-time stream
 * - INCREMENTAL - Incremental updates
 * - BATCH - Batched updates
 * - SNAPSHOT - Periodic snapshots
 * - EVENT_DRIVEN - Event triggered
 * - PULL - Pull-based updates
 * - PUSH - Push-based updates
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 92 - Report Streaming & Real-time Updates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportStreaming {

    private Long streamId;
    private Long reportId;
    private String streamName;
    private String description;

    // Stream status
    private StreamStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime lastUpdateAt;
    private LocalDateTime stoppedAt;
    private Long uptimeSeconds;
    private Boolean isActive;

    // Stream configuration
    private StreamType streamType;
    private DeliveryMode deliveryMode;
    private String protocol; // WebSocket, SSE, HTTP/2, gRPC
    private Integer updateIntervalMs;
    private Integer batchSize;
    private Integer bufferSize;
    private Integer maxConnections;

    // Subscriptions
    private List<Subscription> subscriptions;
    private Integer activeSubscriptions;
    private Integer totalSubscriptions;
    private Map<String, Subscription> subscriptionRegistry;

    // Data flow
    private List<DataUpdate> updates;
    private Long totalUpdates;
    private Long updatesDelivered;
    private Long updatesFailed;
    private Long updatesBuffered;
    private Double deliveryRate; // Updates per second

    // Connection management
    private List<Connection> connections;
    private Integer activeConnections;
    private Integer maxConcurrentConnections;
    private Map<String, Connection> connectionRegistry;

    // Backpressure
    private BackpressureStrategy backpressureStrategy;
    private Integer currentBufferCount;
    private Integer bufferHighWaterMark;
    private Integer bufferLowWaterMark;
    private Boolean backpressureActive;
    private Integer droppedUpdates;

    // Quality of Service
    private QoSLevel qosLevel;
    private Boolean guaranteedDelivery;
    private Boolean orderPreserving;
    private Integer deliveryTimeout;
    private Integer maxRetries;

    // Filtering
    private List<StreamFilter> filters;
    private Boolean filteringEnabled;
    private Map<String, Object> filterCriteria;

    // Transformation
    private List<StreamTransform> transforms;
    private Boolean transformationEnabled;
    private String outputFormat; // JSON, XML, CSV, Binary

    // Monitoring
    private StreamMetrics metrics;
    private List<StreamEvent> events;
    private List<StreamError> errors;
    private String lastError;
    private LocalDateTime lastErrorAt;

    // Security
    private Boolean authenticationRequired;
    private String authenticationMethod;
    private Boolean encryptionEnabled;
    private String encryptionMethod;
    private List<String> allowedOrigins;

    // Configuration
    private Map<String, Object> configuration;
    private Boolean configurationLocked;

    /**
     * Stream Status
     */
    public enum StreamStatus {
        INITIALIZING,   // Stream initializing
        CONNECTING,     // Connecting to source
        ACTIVE,         // Actively streaming
        PAUSED,         // Temporarily paused
        BUFFERING,      // Buffering data
        THROTTLED,      // Rate limited
        ERROR,          // Error state
        CLOSED          // Stream closed
    }

    /**
     * Stream Type
     */
    public enum StreamType {
        LIVE,           // Live real-time stream
        INCREMENTAL,    // Incremental updates
        BATCH,          // Batched updates
        SNAPSHOT,       // Periodic snapshots
        EVENT_DRIVEN,   // Event triggered
        PULL,           // Pull-based updates
        PUSH            // Push-based updates
    }

    /**
     * Delivery Mode
     */
    public enum DeliveryMode {
        UNICAST,        // Single recipient
        MULTICAST,      // Multiple recipients
        BROADCAST,      // All subscribers
        ANYCAST         // Any available recipient
    }

    /**
     * Backpressure Strategy
     */
    public enum BackpressureStrategy {
        DROP_OLDEST,    // Drop oldest buffered items
        DROP_NEWEST,    // Drop newest incoming items
        BUFFER,         // Buffer until capacity
        BLOCK,          // Block producer
        ERROR           // Error on overflow
    }

    /**
     * QoS Level
     */
    public enum QoSLevel {
        AT_MOST_ONCE,   // Fire and forget
        AT_LEAST_ONCE,  // Guaranteed delivery
        EXACTLY_ONCE,   // Exactly once delivery
        BEST_EFFORT     // Best effort
    }

    /**
     * Connection Status
     */
    public enum ConnectionStatus {
        CONNECTING,
        CONNECTED,
        AUTHENTICATED,
        DISCONNECTING,
        DISCONNECTED,
        ERROR
    }

    /**
     * Subscription
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Subscription {
        private String subscriptionId;
        private String subscriberId;
        private String subscriberName;
        private String subscriberEmail;
        private LocalDateTime subscribedAt;
        private LocalDateTime lastAccessAt;
        private Boolean active;
        private String connectionId;

        // Subscription settings
        private Map<String, Object> filters;
        private List<String> dataFields; // Specific fields to receive
        private Integer updateThrottleMs; // Minimum time between updates
        private Integer maxUpdatesPerSecond;

        // Delivery
        private Long updatesReceived;
        private Long updatesMissed;
        private LocalDateTime lastUpdateAt;
        private Boolean pausedBySubscriber;

        // Metadata
        private Map<String, Object> metadata;
    }

    /**
     * Connection
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Connection {
        private String connectionId;
        private String userId;
        private ConnectionStatus status;
        private String protocol;
        private String ipAddress;
        private LocalDateTime connectedAt;
        private LocalDateTime lastActivityAt;
        private LocalDateTime disconnectedAt;

        // Connection metrics
        private Long bytesSent;
        private Long bytesReceived;
        private Long messagesSent;
        private Long messagesReceived;
        private Integer latencyMs;
        private Integer pingIntervalMs;
        private LocalDateTime lastPingAt;

        // Settings
        private Map<String, Object> connectionSettings;
        private String userAgent;
    }

    /**
     * Data Update
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataUpdate {
        private String updateId;
        private String updateType; // FULL, PARTIAL, DELTA, SNAPSHOT
        private LocalDateTime timestamp;
        private Long sequenceNumber;
        private Map<String, Object> data;
        private Map<String, Object> metadata;
        private String changeType; // INSERT, UPDATE, DELETE
        private List<String> changedFields;
        private Boolean delivered;
        private Integer deliveryAttempts;
    }

    /**
     * Stream Filter
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreamFilter {
        private String filterId;
        private String filterName;
        private String filterType; // INCLUDE, EXCLUDE, TRANSFORM
        private String condition; // Filter expression
        private Boolean enabled;
        private Integer priority;
        private Long itemsFiltered;
        private Long itemsPassed;
    }

    /**
     * Stream Transform
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreamTransform {
        private String transformId;
        private String transformName;
        private String transformType; // MAP, FILTER, REDUCE, AGGREGATE
        private String expression;
        private Boolean enabled;
        private Integer executionOrder;
        private Long itemsTransformed;
        private Long transformErrors;
    }

    /**
     * Stream Metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreamMetrics {
        private Long totalUpdates;
        private Long successfulDeliveries;
        private Long failedDeliveries;
        private Double successRate;
        private Double averageLatencyMs;
        private Double p50LatencyMs;
        private Double p95LatencyMs;
        private Double p99LatencyMs;
        private Double throughputPerSecond;
        private Long totalBytesTransferred;
        private Double bandwidthMbps;
        private LocalDateTime measuredAt;
    }

    /**
     * Stream Event
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreamEvent {
        private String eventId;
        private String eventType;
        private String description;
        private LocalDateTime timestamp;
        private String connectionId;
        private String subscriptionId;
        private Map<String, Object> eventData;
    }

    /**
     * Stream Error
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreamError {
        private String errorId;
        private String errorType;
        private String errorMessage;
        private LocalDateTime occurredAt;
        private String connectionId;
        private String subscriptionId;
        private Boolean recovered;
        private String stackTrace;
    }

    /**
     * Helper Methods
     */

    public void addSubscription(Subscription subscription) {
        if (subscriptions == null) {
            subscriptions = new ArrayList<>();
        }
        subscriptions.add(subscription);

        if (subscriptionRegistry == null) {
            subscriptionRegistry = new HashMap<>();
        }
        subscriptionRegistry.put(subscription.getSubscriptionId(), subscription);

        totalSubscriptions = (totalSubscriptions != null ? totalSubscriptions : 0) + 1;
        if (Boolean.TRUE.equals(subscription.getActive())) {
            activeSubscriptions = (activeSubscriptions != null ? activeSubscriptions : 0) + 1;
        }
    }

    public void removeSubscription(String subscriptionId) {
        if (subscriptionRegistry == null) {
            return;
        }

        Subscription subscription = subscriptionRegistry.remove(subscriptionId);
        if (subscription != null) {
            subscriptions.remove(subscription);
            totalSubscriptions = Math.max(0, (totalSubscriptions != null ? totalSubscriptions : 1) - 1);
            if (Boolean.TRUE.equals(subscription.getActive()) && activeSubscriptions != null && activeSubscriptions > 0) {
                activeSubscriptions--;
            }
        }
    }

    public void addConnection(Connection connection) {
        if (connections == null) {
            connections = new ArrayList<>();
        }
        connections.add(connection);

        if (connectionRegistry == null) {
            connectionRegistry = new HashMap<>();
        }
        connectionRegistry.put(connection.getConnectionId(), connection);

        if (connection.getStatus() == ConnectionStatus.CONNECTED ||
            connection.getStatus() == ConnectionStatus.AUTHENTICATED) {
            activeConnections = (activeConnections != null ? activeConnections : 0) + 1;
        }
    }

    public void removeConnection(String connectionId) {
        if (connectionRegistry == null) {
            return;
        }

        Connection connection = connectionRegistry.remove(connectionId);
        if (connection != null) {
            connections.remove(connection);
            if ((connection.getStatus() == ConnectionStatus.CONNECTED ||
                 connection.getStatus() == ConnectionStatus.AUTHENTICATED) &&
                activeConnections != null && activeConnections > 0) {
                activeConnections--;
            }
        }
    }

    public void publishUpdate(DataUpdate update) {
        if (updates == null) {
            updates = new ArrayList<>();
        }
        updates.add(update);

        totalUpdates = (totalUpdates != null ? totalUpdates : 0L) + 1;
        lastUpdateAt = LocalDateTime.now();

        // Handle buffering
        if (currentBufferCount == null) {
            currentBufferCount = 0;
        }

        if (bufferSize != null && currentBufferCount >= bufferSize) {
            handleBackpressure(update);
        } else {
            currentBufferCount++;
            updatesBuffered = (updatesBuffered != null ? updatesBuffered : 0L) + 1;
        }
    }

    private void handleBackpressure(DataUpdate update) {
        if (backpressureStrategy == null) {
            backpressureStrategy = BackpressureStrategy.DROP_OLDEST;
        }

        switch (backpressureStrategy) {
            case DROP_OLDEST -> {
                if (updates != null && !updates.isEmpty()) {
                    updates.remove(0);
                    currentBufferCount = Math.max(0, currentBufferCount - 1);
                }
                droppedUpdates = (droppedUpdates != null ? droppedUpdates : 0) + 1;
            }
            case DROP_NEWEST -> {
                droppedUpdates = (droppedUpdates != null ? droppedUpdates : 0) + 1;
            }
            case BUFFER -> {
                // Keep buffering (may lead to memory issues)
                currentBufferCount++;
            }
            case BLOCK, ERROR -> {
                status = StreamStatus.THROTTLED;
                backpressureActive = true;
            }
        }

        if (bufferHighWaterMark != null && currentBufferCount >= bufferHighWaterMark) {
            backpressureActive = true;
            status = StreamStatus.BUFFERING;
        }
    }

    public void recordDelivery(boolean success) {
        if (success) {
            updatesDelivered = (updatesDelivered != null ? updatesDelivered : 0L) + 1;
            if (currentBufferCount != null && currentBufferCount > 0) {
                currentBufferCount--;
            }
        } else {
            updatesFailed = (updatesFailed != null ? updatesFailed : 0L) + 1;
        }

        updateDeliveryRate();

        if (bufferLowWaterMark != null && currentBufferCount != null &&
            currentBufferCount <= bufferLowWaterMark) {
            backpressureActive = false;
            if (status == StreamStatus.BUFFERING || status == StreamStatus.THROTTLED) {
                status = StreamStatus.ACTIVE;
            }
        }
    }

    private void updateDeliveryRate() {
        if (startedAt == null) {
            deliveryRate = 0.0;
            return;
        }

        long elapsedSeconds = java.time.Duration.between(startedAt, LocalDateTime.now()).getSeconds();
        if (elapsedSeconds > 0) {
            long delivered = updatesDelivered != null ? updatesDelivered : 0L;
            deliveryRate = (double) delivered / elapsedSeconds;
        }
    }

    public void recordEvent(String eventType, String description, String connectionId) {
        if (events == null) {
            events = new ArrayList<>();
        }

        StreamEvent event = StreamEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType(eventType)
                .description(description)
                .timestamp(LocalDateTime.now())
                .connectionId(connectionId)
                .eventData(new HashMap<>())
                .build();

        events.add(event);
    }

    public void recordError(String errorType, String errorMessage, String connectionId) {
        if (errors == null) {
            errors = new ArrayList<>();
        }

        StreamError error = StreamError.builder()
                .errorId(java.util.UUID.randomUUID().toString())
                .errorType(errorType)
                .errorMessage(errorMessage)
                .occurredAt(LocalDateTime.now())
                .connectionId(connectionId)
                .recovered(false)
                .build();

        errors.add(error);
        lastError = errorMessage;
        lastErrorAt = LocalDateTime.now();
    }

    public void startStream() {
        status = StreamStatus.ACTIVE;
        startedAt = LocalDateTime.now();
        isActive = true;
    }

    public void pauseStream() {
        status = StreamStatus.PAUSED;
        isActive = false;
    }

    public void resumeStream() {
        status = StreamStatus.ACTIVE;
        isActive = true;
    }

    public void stopStream() {
        status = StreamStatus.CLOSED;
        stoppedAt = LocalDateTime.now();
        isActive = false;

        if (startedAt != null && stoppedAt != null) {
            uptimeSeconds = java.time.Duration.between(startedAt, stoppedAt).getSeconds();
        }
    }

    public Subscription getSubscription(String subscriptionId) {
        return subscriptionRegistry != null ? subscriptionRegistry.get(subscriptionId) : null;
    }

    public Connection getConnection(String connectionId) {
        return connectionRegistry != null ? connectionRegistry.get(connectionId) : null;
    }

    public List<Subscription> getActiveSubscriptions() {
        if (subscriptions == null) {
            return new ArrayList<>();
        }
        return subscriptions.stream()
                .filter(s -> Boolean.TRUE.equals(s.getActive()))
                .toList();
    }

    public List<Connection> getActiveConnections() {
        if (connections == null) {
            return new ArrayList<>();
        }
        return connections.stream()
                .filter(c -> c.getStatus() == ConnectionStatus.CONNECTED ||
                           c.getStatus() == ConnectionStatus.AUTHENTICATED)
                .toList();
    }

    public boolean canAcceptNewConnection() {
        if (maxConcurrentConnections == null) {
            return true;
        }

        int active = activeConnections != null ? activeConnections : 0;
        return active < maxConcurrentConnections;
    }

    public boolean isHealthy() {
        return status == StreamStatus.ACTIVE && Boolean.TRUE.equals(isActive);
    }

    public boolean requiresAttention() {
        return status == StreamStatus.ERROR ||
               status == StreamStatus.THROTTLED ||
               Boolean.TRUE.equals(backpressureActive);
    }
}
