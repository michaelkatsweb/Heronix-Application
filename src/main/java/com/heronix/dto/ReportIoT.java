package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report IoT Integration DTO
 *
 * Manages IoT devices and sensor data for report generation.
 *
 * Features:
 * - IoT device management
 * - Sensor data collection
 * - Real-time data streaming
 * - Device provisioning
 * - MQTT/CoAP/HTTP protocols
 * - Edge analytics
 * - Device twins
 * - Telemetry aggregation
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 103 - Report IoT Integration & Sensor Data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportIoT {

    // Platform Information
    private Long platformId;
    private String platformName;
    private String description;
    private PlatformStatus status;
    private Boolean isActive;

    // IoT Platform
    private IoTPlatform iotPlatform;
    private String platformVersion;
    private List<String> supportedProtocols;
    private String mqttBrokerUrl;
    private String httpEndpoint;
    private Boolean enableEdgeAnalytics;

    // Devices
    private List<IoTDevice> devices;
    private Map<String, IoTDevice> deviceRegistry;
    private Integer totalDevices;
    private Integer onlineDevices;
    private Integer offlineDevices;
    private Integer activeDevices;

    // Sensors
    private List<Sensor> sensors;
    private Map<String, Sensor> sensorRegistry;
    private Integer totalSensors;
    private Integer activeSensors;

    // Telemetry Data
    private List<TelemetryData> telemetryData;
    private Map<String, TelemetryData> telemetryRegistry;
    private Long totalDataPoints;
    private Long dataPointsToday;

    // Device Twins
    private List<DeviceTwin> deviceTwins;
    private Map<String, DeviceTwin> twinRegistry;

    // Commands
    private List<DeviceCommand> commands;
    private Map<String, DeviceCommand> commandRegistry;
    private Long totalCommands;
    private Long successfulCommands;
    private Long failedCommands;

    // Alerts
    private List<SensorAlert> alerts;
    private Map<String, SensorAlert> alertRegistry;
    private Long totalAlerts;
    private Long activeAlerts;

    // Data Streams
    private List<DataStream> dataStreams;
    private Map<String, DataStream> streamRegistry;
    private Integer totalStreams;
    private Integer activeStreams;

    // Metrics
    private IoTMetrics metrics;
    private LocalDateTime lastMetricsUpdate;

    // Events
    private List<IoTEvent> events;
    private LocalDateTime lastEventAt;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime stoppedAt;
    private LocalDateTime lastDataReceivedAt;

    /**
     * Platform Status
     */
    public enum PlatformStatus {
        INITIALIZING,
        READY,
        RUNNING,
        DEGRADED,
        MAINTENANCE,
        ERROR
    }

    /**
     * IoT Platform
     */
    public enum IoTPlatform {
        AWS_IOT_CORE,
        AZURE_IOT_HUB,
        GOOGLE_IOT_CORE,
        THINGSBOARD,
        ECLIPSE_HONO,
        KAA_IOT,
        CUSTOM
    }

    /**
     * Device Status
     */
    public enum DeviceStatus {
        PROVISIONING,
        ONLINE,
        OFFLINE,
        SLEEPING,
        ERROR,
        DECOMMISSIONED
    }

    /**
     * Sensor Type
     */
    public enum SensorType {
        TEMPERATURE,
        HUMIDITY,
        PRESSURE,
        MOTION,
        LIGHT,
        SOUND,
        GPS,
        ACCELEROMETER,
        GYROSCOPE,
        PROXIMITY,
        VOLTAGE,
        CURRENT,
        CUSTOM
    }

    /**
     * Protocol
     */
    public enum Protocol {
        MQTT,
        COAP,
        HTTP,
        HTTPS,
        WEBSOCKET,
        AMQP,
        LORAWAN,
        ZIGBEE,
        BLUETOOTH_LE
    }

    /**
     * Command Status
     */
    public enum CommandStatus {
        PENDING,
        SENT,
        ACKNOWLEDGED,
        COMPLETED,
        FAILED,
        TIMEOUT
    }

    /**
     * Alert Severity
     */
    public enum AlertSeverity {
        INFO,
        WARNING,
        ERROR,
        CRITICAL
    }

    /**
     * IoT Device
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IoTDevice {
        private String deviceId;
        private String deviceName;
        private DeviceStatus status;
        private String deviceType;
        private String manufacturer;
        private String model;
        private String firmwareVersion;
        private Protocol protocol;
        private String ipAddress;
        private String macAddress;
        private List<String> sensorIds;
        private String location;
        private Double latitude;
        private Double longitude;
        private Integer batteryLevel;
        private Integer signalStrength;
        private Long totalDataPoints;
        private Long uptime;
        private LocalDateTime provisionedAt;
        private LocalDateTime lastSeenAt;
        private LocalDateTime lastDataAt;
        private Map<String, String> tags;
        private Map<String, Object> metadata;
    }

    /**
     * Sensor
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Sensor {
        private String sensorId;
        private String sensorName;
        private SensorType sensorType;
        private String deviceId;
        private String unit;
        private Double minValue;
        private Double maxValue;
        private Double currentValue;
        private Double accuracy;
        private Integer sampleRate;
        private Long totalReadings;
        private LocalDateTime lastReadingAt;
        private Boolean calibrated;
        private LocalDateTime calibratedAt;
        private Map<String, String> tags;
        private Map<String, Object> metadata;
    }

    /**
     * Telemetry Data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TelemetryData {
        private String telemetryId;
        private String deviceId;
        private String sensorId;
        private SensorType sensorType;
        private LocalDateTime timestamp;
        private Double value;
        private String unit;
        private Integer quality;
        private Map<String, Object> additionalData;
        private String location;
        private Boolean processed;
        private LocalDateTime processedAt;
        private Map<String, Object> metadata;
    }

    /**
     * Device Twin
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceTwin {
        private String twinId;
        private String deviceId;
        private Map<String, Object> reportedProperties;
        private Map<String, Object> desiredProperties;
        private Map<String, String> tags;
        private LocalDateTime lastUpdatedAt;
        private Integer version;
        private Map<String, Object> metadata;
    }

    /**
     * Device Command
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceCommand {
        private String commandId;
        private String deviceId;
        private String commandType;
        private CommandStatus status;
        private Map<String, Object> parameters;
        private String response;
        private LocalDateTime sentAt;
        private LocalDateTime acknowledgedAt;
        private LocalDateTime completedAt;
        private Long executionTimeMs;
        private String errorMessage;
        private Map<String, Object> metadata;
    }

    /**
     * Sensor Alert
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SensorAlert {
        private String alertId;
        private String deviceId;
        private String sensorId;
        private AlertSeverity severity;
        private String alertType;
        private String message;
        private Double thresholdValue;
        private Double actualValue;
        private LocalDateTime triggeredAt;
        private LocalDateTime acknowledgedAt;
        private LocalDateTime resolvedAt;
        private Boolean active;
        private String acknowledgedBy;
        private Map<String, Object> metadata;
    }

    /**
     * Data Stream
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataStream {
        private String streamId;
        private String streamName;
        private String deviceId;
        private List<String> sensorIds;
        private Protocol protocol;
        private String endpoint;
        private Integer intervalMs;
        private Boolean active;
        private Long totalMessages;
        private Long messagesPerSecond;
        private LocalDateTime startedAt;
        private LocalDateTime lastMessageAt;
        private Map<String, Object> configuration;
        private Map<String, Object> metadata;
    }

    /**
     * IoT Metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IoTMetrics {
        private Integer totalDevices;
        private Integer onlineDevices;
        private Integer offlineDevices;
        private Double deviceAvailability;
        private Long totalDataPoints;
        private Long dataPointsPerMinute;
        private Double averageBatteryLevel;
        private Double averageSignalStrength;
        private Long totalCommands;
        private Long successfulCommands;
        private Double commandSuccessRate;
        private Long activeAlerts;
        private Integer activeStreams;
        private Long totalDataVolumeMb;
        private LocalDateTime measuredAt;
    }

    /**
     * IoT Event
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IoTEvent {
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
     * Start platform
     */
    public void startPlatform() {
        this.status = PlatformStatus.INITIALIZING;
        this.isActive = true;
        this.startedAt = LocalDateTime.now();

        recordEvent("PLATFORM_STARTED", "IoT platform started", "PLATFORM",
                platformId != null ? platformId.toString() : null);

        this.status = PlatformStatus.READY;
    }

    /**
     * Stop platform
     */
    public void stopPlatform() {
        this.status = PlatformStatus.MAINTENANCE;
        this.isActive = false;
        this.stoppedAt = LocalDateTime.now();

        recordEvent("PLATFORM_STOPPED", "IoT platform stopped", "PLATFORM",
                platformId != null ? platformId.toString() : null);
    }

    /**
     * Register device
     */
    public void registerDevice(IoTDevice device) {
        if (devices == null) {
            devices = new java.util.ArrayList<>();
        }
        devices.add(device);

        if (deviceRegistry == null) {
            deviceRegistry = new java.util.HashMap<>();
        }
        deviceRegistry.put(device.getDeviceId(), device);

        totalDevices = (totalDevices != null ? totalDevices : 0) + 1;
        if (device.getStatus() == DeviceStatus.ONLINE) {
            onlineDevices = (onlineDevices != null ? onlineDevices : 0) + 1;
        } else if (device.getStatus() == DeviceStatus.OFFLINE) {
            offlineDevices = (offlineDevices != null ? offlineDevices : 0) + 1;
        }

        recordEvent("DEVICE_REGISTERED", "Device registered: " + device.getDeviceName(),
                "DEVICE", device.getDeviceId());
    }

    /**
     * Update device status
     */
    public void updateDeviceStatus(String deviceId, DeviceStatus newStatus) {
        IoTDevice device = deviceRegistry != null ? deviceRegistry.get(deviceId) : null;
        if (device != null) {
            DeviceStatus oldStatus = device.getStatus();
            device.setStatus(newStatus);
            device.setLastSeenAt(LocalDateTime.now());

            // Update counters
            if (oldStatus == DeviceStatus.ONLINE && onlineDevices != null && onlineDevices > 0) {
                onlineDevices--;
            } else if (oldStatus == DeviceStatus.OFFLINE && offlineDevices != null && offlineDevices > 0) {
                offlineDevices--;
            }

            if (newStatus == DeviceStatus.ONLINE) {
                onlineDevices = (onlineDevices != null ? onlineDevices : 0) + 1;
            } else if (newStatus == DeviceStatus.OFFLINE) {
                offlineDevices = (offlineDevices != null ? offlineDevices : 0) + 1;
            }
        }
    }

    /**
     * Register sensor
     */
    public void registerSensor(Sensor sensor) {
        if (sensors == null) {
            sensors = new java.util.ArrayList<>();
        }
        sensors.add(sensor);

        if (sensorRegistry == null) {
            sensorRegistry = new java.util.HashMap<>();
        }
        sensorRegistry.put(sensor.getSensorId(), sensor);

        totalSensors = (totalSensors != null ? totalSensors : 0) + 1;
        activeSensors = (activeSensors != null ? activeSensors : 0) + 1;

        recordEvent("SENSOR_REGISTERED", "Sensor registered: " + sensor.getSensorName(),
                "SENSOR", sensor.getSensorId());
    }

    /**
     * Record telemetry
     */
    public void recordTelemetry(TelemetryData data) {
        if (telemetryData == null) {
            telemetryData = new java.util.ArrayList<>();
        }
        telemetryData.add(data);

        if (telemetryRegistry == null) {
            telemetryRegistry = new java.util.HashMap<>();
        }
        telemetryRegistry.put(data.getTelemetryId(), data);

        totalDataPoints = (totalDataPoints != null ? totalDataPoints : 0L) + 1;
        lastDataReceivedAt = LocalDateTime.now();

        // Update sensor
        Sensor sensor = sensorRegistry != null ? sensorRegistry.get(data.getSensorId()) : null;
        if (sensor != null) {
            sensor.setCurrentValue(data.getValue());
            sensor.setTotalReadings((sensor.getTotalReadings() != null ? sensor.getTotalReadings() : 0L) + 1);
            sensor.setLastReadingAt(data.getTimestamp());
        }

        // Update device
        IoTDevice device = deviceRegistry != null ? deviceRegistry.get(data.getDeviceId()) : null;
        if (device != null) {
            device.setTotalDataPoints((device.getTotalDataPoints() != null ? device.getTotalDataPoints() : 0L) + 1);
            device.setLastDataAt(data.getTimestamp());
        }
    }

    /**
     * Create device twin
     */
    public void createDeviceTwin(DeviceTwin twin) {
        if (deviceTwins == null) {
            deviceTwins = new java.util.ArrayList<>();
        }
        deviceTwins.add(twin);

        if (twinRegistry == null) {
            twinRegistry = new java.util.HashMap<>();
        }
        twinRegistry.put(twin.getTwinId(), twin);

        recordEvent("TWIN_CREATED", "Device twin created for device: " + twin.getDeviceId(),
                "TWIN", twin.getTwinId());
    }

    /**
     * Send command
     */
    public void sendCommand(DeviceCommand command) {
        if (commands == null) {
            commands = new java.util.ArrayList<>();
        }
        commands.add(command);

        if (commandRegistry == null) {
            commandRegistry = new java.util.HashMap<>();
        }
        commandRegistry.put(command.getCommandId(), command);

        totalCommands = (totalCommands != null ? totalCommands : 0L) + 1;

        recordEvent("COMMAND_SENT", "Command sent: " + command.getCommandType(),
                "COMMAND", command.getCommandId());
    }

    /**
     * Complete command
     */
    public void completeCommand(String commandId, boolean success) {
        DeviceCommand command = commandRegistry != null ? commandRegistry.get(commandId) : null;
        if (command != null) {
            command.setStatus(success ? CommandStatus.COMPLETED : CommandStatus.FAILED);
            command.setCompletedAt(LocalDateTime.now());

            if (command.getSentAt() != null) {
                command.setExecutionTimeMs(
                    java.time.Duration.between(command.getSentAt(), command.getCompletedAt()).toMillis()
                );
            }

            if (success) {
                successfulCommands = (successfulCommands != null ? successfulCommands : 0L) + 1;
            } else {
                failedCommands = (failedCommands != null ? failedCommands : 0L) + 1;
            }
        }
    }

    /**
     * Trigger alert
     */
    public void triggerAlert(SensorAlert alert) {
        if (alerts == null) {
            alerts = new java.util.ArrayList<>();
        }
        alerts.add(alert);

        if (alertRegistry == null) {
            alertRegistry = new java.util.HashMap<>();
        }
        alertRegistry.put(alert.getAlertId(), alert);

        totalAlerts = (totalAlerts != null ? totalAlerts : 0L) + 1;
        if (alert.getActive()) {
            activeAlerts = (activeAlerts != null ? activeAlerts : 0L) + 1;
        }

        recordEvent("ALERT_TRIGGERED", "Alert triggered: " + alert.getMessage(),
                "ALERT", alert.getAlertId());
    }

    /**
     * Resolve alert
     */
    public void resolveAlert(String alertId) {
        SensorAlert alert = alertRegistry != null ? alertRegistry.get(alertId) : null;
        if (alert != null && alert.getActive()) {
            alert.setActive(false);
            alert.setResolvedAt(LocalDateTime.now());

            if (activeAlerts != null && activeAlerts > 0) {
                activeAlerts--;
            }
        }
    }

    /**
     * Create data stream
     */
    public void createDataStream(DataStream stream) {
        if (dataStreams == null) {
            dataStreams = new java.util.ArrayList<>();
        }
        dataStreams.add(stream);

        if (streamRegistry == null) {
            streamRegistry = new java.util.HashMap<>();
        }
        streamRegistry.put(stream.getStreamId(), stream);

        totalStreams = (totalStreams != null ? totalStreams : 0) + 1;
        if (stream.getActive()) {
            activeStreams = (activeStreams != null ? activeStreams : 0) + 1;
        }

        recordEvent("STREAM_CREATED", "Data stream created: " + stream.getStreamName(),
                "STREAM", stream.getStreamId());
    }

    /**
     * Get device by ID
     */
    public IoTDevice getDevice(String deviceId) {
        return deviceRegistry != null ? deviceRegistry.get(deviceId) : null;
    }

    /**
     * Get sensor by ID
     */
    public Sensor getSensor(String sensorId) {
        return sensorRegistry != null ? sensorRegistry.get(sensorId) : null;
    }

    /**
     * Record event
     */
    public void recordEvent(String eventType, String description, String resourceType, String resourceId) {
        if (events == null) {
            events = new java.util.ArrayList<>();
        }

        IoTEvent event = IoTEvent.builder()
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
     * Check if platform is healthy
     */
    public boolean isHealthy() {
        return status == PlatformStatus.READY || status == PlatformStatus.RUNNING;
    }

    /**
     * Get online devices
     */
    public List<IoTDevice> getOnlineDevices() {
        if (devices == null) {
            return new java.util.ArrayList<>();
        }
        return devices.stream()
                .filter(d -> d.getStatus() == DeviceStatus.ONLINE)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get active alerts
     */
    public List<SensorAlert> getActiveAlerts() {
        if (alerts == null) {
            return new java.util.ArrayList<>();
        }
        return alerts.stream()
                .filter(a -> Boolean.TRUE.equals(a.getActive()))
                .collect(java.util.stream.Collectors.toList());
    }
}
