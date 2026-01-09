package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Edge Analytics DTO
 *
 * Represents edge computing analytics, IoT device integration,
 * real-time sensor data processing, and distributed edge intelligence.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 133 - Edge Analytics & IoT Integration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportEdgeAnalytics {

    private Long analyticsId;
    private String analyticsName;
    private String description;

    // Edge Configuration
    private String edgeLocation; // DEVICE, GATEWAY, FOG_NODE, EDGE_CLOUD
    private String edgeNodeId;
    private String edgeNodeType;
    private String deploymentRegion;
    private Map<String, String> edgeMetadata;

    // IoT Device Configuration
    private List<String> deviceIds;
    private String deviceProtocol; // MQTT, COAP, HTTP, AMQP, WEBSOCKET
    private String deviceDataFormat; // JSON, PROTOBUF, AVRO, XML
    private Integer deviceCount;
    private Map<String, Object> deviceConfig;

    // Sensor Data Configuration
    private List<String> sensorTypes; // TEMPERATURE, HUMIDITY, PRESSURE, MOTION, etc.
    private String samplingRate;
    private String dataRetentionPolicy;
    private Boolean compressionEnabled;
    private String compressionAlgorithm;

    // Real-Time Processing
    private Boolean realtimeProcessing;
    private Integer processingLatencyMs;
    private String processingMode; // STREAM, BATCH, MICRO_BATCH
    private Map<String, Object> processingRules;
    private List<String> aggregationFunctions;

    // Analytics Configuration
    private String analyticsType; // DESCRIPTIVE, PREDICTIVE, PRESCRIPTIVE, DIAGNOSTIC
    private List<String> metricsToTrack;
    private Map<String, Object> analyticsModels;
    private Boolean mlInferenceEnabled;
    private String mlModelPath;

    // Data Pipeline
    private String inputSource;
    private String outputDestination;
    private List<Map<String, Object>> transformations;
    private Boolean dataValidationEnabled;
    private Map<String, Object> validationRules;

    // Edge Intelligence
    private Boolean localInferenceEnabled;
    private String inferenceEngine; // TENSORFLOW_LITE, ONNX, PYTORCH_MOBILE
    private Integer inferenceThreads;
    private Map<String, Object> inferenceConfig;
    private List<String> supportedModels;

    // Data Synchronization
    private String syncStrategy; // CONTINUOUS, PERIODIC, ON_DEMAND, EVENT_DRIVEN
    private Integer syncIntervalSeconds;
    private Boolean cloudSyncEnabled;
    private String cloudEndpoint;
    private Map<String, Object> syncFilters;

    // Storage Configuration
    private String localStorageType; // EMBEDDED_DB, TIME_SERIES_DB, FILE_SYSTEM
    private String storagePath;
    private Long storageCapacityMb;
    private Long currentStorageUsageMb;
    private Boolean autoCleanupEnabled;

    // Network Configuration
    private String networkType; // WIFI, CELLULAR_4G, CELLULAR_5G, LORA, ZIGBEE
    private Integer bandwidthKbps;
    private Boolean offlineModeSupported;
    private Map<String, Object> networkQoS;

    // Security Configuration
    private Boolean encryptionEnabled;
    private String encryptionAlgorithm;
    private Boolean deviceAuthentication;
    private String authenticationMethod; // CERTIFICATE, TOKEN, API_KEY
    private Map<String, String> securityPolicies;

    // Alert Configuration
    private Boolean alertingEnabled;
    private List<Map<String, Object>> alertRules;
    private List<String> alertChannels; // EMAIL, SMS, WEBHOOK, MQTT_PUBLISH
    private Integer alertThrottleSeconds;

    // Performance Metrics
    private Long totalEventsProcessed;
    private Long eventsPerSecond;
    private Double averageLatencyMs;
    private Double dataLossPercentage;
    private Map<String, Object> performanceStats;

    // Resource Utilization
    private Double cpuUtilization;
    private Double memoryUtilization;
    private Double storageUtilization;
    private Double networkUtilization;
    private Map<String, Object> resourceMetrics;

    // Execution Status
    private String analyticsStatus; // PENDING, RUNNING, PAUSED, STOPPED, ERROR
    private LocalDateTime startedAt;
    private LocalDateTime lastDataReceivedAt;
    private LocalDateTime lastSyncedAt;
    private String lastError;

    // Metadata
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Object> customMetadata;

    // Helper Methods
    public void addDeviceId(String deviceId) {
        if (this.deviceIds != null) {
            this.deviceIds.add(deviceId);
        }
    }

    public void addSensorType(String sensorType) {
        if (this.sensorTypes != null) {
            this.sensorTypes.add(sensorType);
        }
    }

    public void addMetricToTrack(String metric) {
        if (this.metricsToTrack != null) {
            this.metricsToTrack.add(metric);
        }
    }

    public void addTransformation(Map<String, Object> transformation) {
        if (this.transformations != null) {
            this.transformations.add(transformation);
        }
    }

    public void addAlertRule(Map<String, Object> alertRule) {
        if (this.alertRules != null) {
            this.alertRules.add(alertRule);
        }
    }

    public void incrementEventsProcessed(Long count) {
        if (this.totalEventsProcessed == null) {
            this.totalEventsProcessed = 0L;
        }
        this.totalEventsProcessed += count;
    }

    public boolean isRunning() {
        return "RUNNING".equals(analyticsStatus);
    }

    public boolean isHealthy() {
        return isRunning() && lastError == null;
    }

    public double getStorageUtilizationPercentage() {
        if (storageCapacityMb == null || storageCapacityMb == 0 || currentStorageUsageMb == null) {
            return 0.0;
        }
        return (currentStorageUsageMb * 100.0) / storageCapacityMb;
    }

    public boolean needsCleanup() {
        return Boolean.TRUE.equals(autoCleanupEnabled) &&
               getStorageUtilizationPercentage() > 80.0;
    }

    public boolean isLowBandwidth() {
        return bandwidthKbps != null && bandwidthKbps < 1000;
    }
}
