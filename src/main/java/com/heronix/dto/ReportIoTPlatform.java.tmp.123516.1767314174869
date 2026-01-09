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
 * Report Internet of Things (IoT) Platform DTO
 *
 * Manages IoT device connectivity, sensor data collection, device management, and real-time monitoring
 * for smart campus infrastructure, classroom IoT sensors, and connected learning environments.
 *
 * Educational Use Cases:
 * - Smart classroom environmental monitoring (temperature, humidity, air quality)
 * - Student attendance tracking via RFID/NFC badges
 * - Campus occupancy monitoring and space utilization
 * - Laboratory equipment monitoring and predictive maintenance
 * - Energy consumption tracking and optimization
 * - Smart library access control and book tracking
 * - Cafeteria capacity monitoring and food waste tracking
 * - Sports facility usage and equipment monitoring
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 121 - Report Internet of Things (IoT) Platform
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportIoTPlatform {

    // Basic Information
    private Long platformId;
    private String platformName;
    private String description;
    private PlatformStatus status;
    private String organizationId;
    private String protocol;

    // Configuration
    private String mqttBroker;
    private Integer mqttPort;
    private String httpEndpoint;
    private String webSocketUrl;
    private Boolean tlsEnabled;
    private Integer maxConnections;

    // State
    private Boolean isActive;
    private Boolean isMonitoring;
    private LocalDateTime createdAt;
    private LocalDateTime deployedAt;
    private LocalDateTime lastSyncAt;
    private String createdBy;

    // IoT Devices
    private List<IoTDevice> devices;
    private Map<String, IoTDevice> deviceRegistry;

    // Device Groups
    private List<DeviceGroup> deviceGroups;
    private Map<String, DeviceGroup> groupRegistry;

    // Sensor Data
    private List<SensorData> sensorDataPoints;
    private Map<String, SensorData> dataRegistry;

    // Device Commands
    private List<DeviceCommand> commands;
    private Map<String, DeviceCommand> commandRegistry;

    // Device Telemetry
    private List<DeviceTelemetry> telemetryData;
    private Map<String, DeviceTelemetry> telemetryRegistry;

    // Device Twins
    private List<DeviceTwin> deviceTwins;
    private Map<String, DeviceTwin> twinRegistry;

    // Alerts
    private List<IoTAlert> alerts;
    private Map<String, IoTAlert> alertRegistry;

    // Firmware Updates
    private List<FirmwareUpdate> firmwareUpdates;
    private Map<String, FirmwareUpdate> firmwareRegistry;

    // Gateway Devices
    private List<Gateway> gateways;
    private Map<String, Gateway> gatewayRegistry;

    // Rules
    private List<IoTRule> rules;
    private Map<String, IoTRule> ruleRegistry;

    // Metrics
    private Long totalDevices;
    private Long onlineDevices;
    private Long offlineDevices;
    private Long totalSensorReadings;
    private Long totalCommands;
    private Long successfulCommands;
    private Long failedCommands;
    private Long totalAlerts;
    private Long criticalAlerts;
    private Double averageLatency; // milliseconds
    private Long totalDataVolume; // bytes
    private Long totalGateways;

    // Events
    private List<IoTEvent> events;

    /**
     * Platform status enumeration
     */
    public enum PlatformStatus {
        INITIALIZING,
        PROVISIONING,
        ACTIVE,
        MONITORING,
        DEGRADED,
        MAINTENANCE,
        OFFLINE
    }

    /**
     * Device status enumeration
     */
    public enum DeviceStatus {
        REGISTERED,
        PROVISIONED,
        ONLINE,
        OFFLINE,
        DISCONNECTED,
        DISABLED,
        ERROR,
        MAINTENANCE
    }

    /**
     * Command status enumeration
     */
    public enum CommandStatus {
        PENDING,
        SENT,
        ACKNOWLEDGED,
        EXECUTING,
        COMPLETED,
        FAILED,
        TIMEOUT
    }

    /**
     * Device type enumeration
     */
    public enum DeviceType {
        SENSOR,
        ACTUATOR,
        GATEWAY,
        CAMERA,
        RFID_READER,
        NFC_READER,
        BEACON,
        SMARTBOARD,
        THERMOSTAT,
        ACCESS_CONTROL,
        ENVIRONMENTAL_MONITOR,
        CUSTOM
    }

    /**
     * Sensor type enumeration
     */
    public enum SensorType {
        TEMPERATURE,
        HUMIDITY,
        PRESSURE,
        MOTION,
        PROXIMITY,
        LIGHT,
        SOUND,
        AIR_QUALITY,
        OCCUPANCY,
        ENERGY,
        VIBRATION,
        CUSTOM
    }

    /**
     * Alert severity enumeration
     */
    public enum AlertSeverity {
        INFO,
        WARNING,
        ERROR,
        CRITICAL
    }

    /**
     * IoT device data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IoTDevice {
        private String deviceId;
        private String deviceName;
        private DeviceType deviceType;
        private DeviceStatus status;
        private String manufacturer;
        private String model;
        private String firmwareVersion;
        private String hardwareVersion;
        private String location;
        private String groupId;
        private String ipAddress;
        private String macAddress;
        private Integer batteryLevel;
        private Double signalStrength;
        private LocalDateTime lastSeenAt;
        private LocalDateTime registeredAt;
        private Map<String, Object> capabilities;
        private Map<String, Object> configuration;
        private Map<String, Object> metadata;
    }

    /**
     * Device group data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceGroup {
        private String groupId;
        private String groupName;
        private String description;
        private List<String> deviceIds;
        private Integer deviceCount;
        private String location;
        private String purpose;
        private LocalDateTime createdAt;
        private Map<String, Object> groupSettings;
    }

    /**
     * Sensor data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SensorData {
        private String dataId;
        private String deviceId;
        private SensorType sensorType;
        private String sensorName;
        private Object value;
        private String unit;
        private Double quality;
        private LocalDateTime timestamp;
        private String location;
        private Map<String, Object> additionalData;
    }

    /**
     * Device command data structure
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
        private Object response;
        private Long executionTime; // milliseconds
        private LocalDateTime issuedAt;
        private LocalDateTime acknowledgedAt;
        private LocalDateTime completedAt;
        private String errorMessage;
        private String issuedBy;
    }

    /**
     * Device telemetry data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceTelemetry {
        private String telemetryId;
        private String deviceId;
        private Double cpuUsage;
        private Double memoryUsage;
        private Long diskUsage;
        private Integer temperature;
        private Integer batteryLevel;
        private Double signalStrength;
        private Long uptime; // seconds
        private Long messagesReceived;
        private Long messagesSent;
        private LocalDateTime timestamp;
        private Map<String, Object> customMetrics;
    }

    /**
     * Device twin data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceTwin {
        private String twinId;
        private String deviceId;
        private Map<String, Object> reportedState;
        private Map<String, Object> desiredState;
        private Map<String, Object> tags;
        private String version;
        private LocalDateTime lastUpdated;
        private LocalDateTime lastReported;
    }

    /**
     * IoT alert data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IoTAlert {
        private String alertId;
        private String deviceId;
        private String alertType;
        private AlertSeverity severity;
        private String message;
        private String description;
        private Boolean isAcknowledged;
        private String acknowledgedBy;
        private LocalDateTime triggeredAt;
        private LocalDateTime acknowledgedAt;
        private LocalDateTime resolvedAt;
        private Map<String, Object> alertData;
    }

    /**
     * Firmware update data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FirmwareUpdate {
        private String updateId;
        private String deviceId;
        private String fromVersion;
        private String toVersion;
        private String updatePackageUrl;
        private Long updateSizeBytes;
        private String status; // PENDING, DOWNLOADING, INSTALLING, COMPLETED, FAILED
        private Integer progressPercent;
        private LocalDateTime scheduledAt;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private String errorMessage;
    }

    /**
     * Gateway data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Gateway {
        private String gatewayId;
        private String gatewayName;
        private DeviceStatus status;
        private String ipAddress;
        private List<String> connectedDeviceIds;
        private Integer connectedDeviceCount;
        private String protocol;
        private Integer port;
        private Long messagesRouted;
        private LocalDateTime lastSeenAt;
        private Map<String, Object> configuration;
    }

    /**
     * IoT rule data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IoTRule {
        private String ruleId;
        private String ruleName;
        private String description;
        private String condition;
        private List<String> actions;
        private Boolean isEnabled;
        private Integer triggerCount;
        private LocalDateTime createdAt;
        private LocalDateTime lastTriggeredAt;
        private Map<String, Object> ruleConfig;
    }

    /**
     * IoT event data structure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IoTEvent {
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
     * Deploy IoT platform
     */
    public void deployPlatform() {
        this.status = PlatformStatus.ACTIVE;
        this.isActive = true;
        this.deployedAt = LocalDateTime.now();
        recordEvent("PLATFORM_DEPLOYED", "IoT platform deployed", "PLATFORM",
                platformId != null ? platformId.toString() : null);
    }

    /**
     * Register IoT device
     */
    public void registerDevice(IoTDevice device) {
        if (devices == null) {
            devices = new ArrayList<>();
        }
        devices.add(device);

        if (deviceRegistry == null) {
            deviceRegistry = new HashMap<>();
        }
        deviceRegistry.put(device.getDeviceId(), device);

        totalDevices = (totalDevices != null ? totalDevices : 0L) + 1;
        if (device.getStatus() == DeviceStatus.ONLINE) {
            onlineDevices = (onlineDevices != null ? onlineDevices : 0L) + 1;
        } else {
            offlineDevices = (offlineDevices != null ? offlineDevices : 0L) + 1;
        }

        recordEvent("DEVICE_REGISTERED", "IoT device registered", "DEVICE", device.getDeviceId());
    }

    /**
     * Record sensor data
     */
    public void recordSensorData(SensorData sensorData) {
        if (sensorDataPoints == null) {
            sensorDataPoints = new ArrayList<>();
        }
        sensorDataPoints.add(sensorData);

        if (dataRegistry == null) {
            dataRegistry = new HashMap<>();
        }
        dataRegistry.put(sensorData.getDataId(), sensorData);

        totalSensorReadings = (totalSensorReadings != null ? totalSensorReadings : 0L) + 1;
    }

    /**
     * Send device command
     */
    public void sendCommand(DeviceCommand command) {
        if (commands == null) {
            commands = new ArrayList<>();
        }
        commands.add(command);

        if (commandRegistry == null) {
            commandRegistry = new HashMap<>();
        }
        commandRegistry.put(command.getCommandId(), command);

        totalCommands = (totalCommands != null ? totalCommands : 0L) + 1;

        recordEvent("COMMAND_SENT", "Device command sent", "COMMAND", command.getCommandId());
    }

    /**
     * Complete command
     */
    public void completeCommand(String commandId, boolean success) {
        DeviceCommand command = commandRegistry != null ? commandRegistry.get(commandId) : null;
        if (command != null) {
            command.setStatus(success ? CommandStatus.COMPLETED : CommandStatus.FAILED);
            command.setCompletedAt(LocalDateTime.now());

            if (command.getIssuedAt() != null) {
                command.setExecutionTime(
                    java.time.Duration.between(command.getIssuedAt(), command.getCompletedAt()).toMillis()
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
    public void triggerAlert(IoTAlert alert) {
        if (alerts == null) {
            alerts = new ArrayList<>();
        }
        alerts.add(alert);

        if (alertRegistry == null) {
            alertRegistry = new HashMap<>();
        }
        alertRegistry.put(alert.getAlertId(), alert);

        totalAlerts = (totalAlerts != null ? totalAlerts : 0L) + 1;
        if (alert.getSeverity() == AlertSeverity.CRITICAL) {
            criticalAlerts = (criticalAlerts != null ? criticalAlerts : 0L) + 1;
        }

        recordEvent("ALERT_TRIGGERED", "IoT alert triggered", "ALERT", alert.getAlertId());
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
            if (oldStatus == DeviceStatus.ONLINE && newStatus != DeviceStatus.ONLINE) {
                onlineDevices = (onlineDevices != null && onlineDevices > 0) ? onlineDevices - 1 : 0;
                offlineDevices = (offlineDevices != null ? offlineDevices : 0L) + 1;
            } else if (oldStatus != DeviceStatus.ONLINE && newStatus == DeviceStatus.ONLINE) {
                offlineDevices = (offlineDevices != null && offlineDevices > 0) ? offlineDevices - 1 : 0;
                onlineDevices = (onlineDevices != null ? onlineDevices : 0L) + 1;
            }
        }
    }

    /**
     * Record IoT event
     */
    private void recordEvent(String eventType, String description, String targetType, String targetId) {
        if (events == null) {
            events = new ArrayList<>();
        }

        IoTEvent event = IoTEvent.builder()
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
