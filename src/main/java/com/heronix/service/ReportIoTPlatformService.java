package com.heronix.service;

import com.heronix.dto.ReportIoTPlatform;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Report IoT Platform Service
 *
 * Manages IoT devices, sensor data collection, device commands, and real-time monitoring
 * for smart campus infrastructure.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 121 - Report Internet of Things (IoT) Platform
 */
@Service
@Slf4j
public class ReportIoTPlatformService {

    private final Map<Long, ReportIoTPlatform> platformStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * Create IoT platform
     */
    public ReportIoTPlatform createIoTPlatform(ReportIoTPlatform platform) {
        Long id;
        synchronized (idGenerator) {
            id = idGenerator.getAndIncrement();
        }

        platform.setPlatformId(id);
        platform.setStatus(ReportIoTPlatform.PlatformStatus.INITIALIZING);
        platform.setIsActive(false);
        platform.setIsMonitoring(false);
        platform.setCreatedAt(LocalDateTime.now());

        // Initialize metrics
        platform.setTotalDevices(0L);
        platform.setOnlineDevices(0L);
        platform.setOfflineDevices(0L);
        platform.setTotalSensorReadings(0L);
        platform.setTotalCommands(0L);
        platform.setSuccessfulCommands(0L);
        platform.setFailedCommands(0L);
        platform.setTotalAlerts(0L);
        platform.setCriticalAlerts(0L);
        platform.setTotalDataVolume(0L);
        platform.setTotalGateways(0L);

        platformStore.put(id, platform);

        log.info("IoT platform created: {}", id);
        return platform;
    }

    /**
     * Get IoT platform
     */
    public Optional<ReportIoTPlatform> getIoTPlatform(Long platformId) {
        return Optional.ofNullable(platformStore.get(platformId));
    }

    /**
     * Deploy IoT platform
     */
    public void deployIoTPlatform(Long platformId) {
        ReportIoTPlatform platform = platformStore.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("IoT platform not found: " + platformId);
        }

        platform.deployPlatform();

        log.info("IoT platform deployed: {}", platformId);
    }

    /**
     * Register IoT device
     */
    public ReportIoTPlatform.IoTDevice registerDevice(
            Long platformId,
            String deviceName,
            ReportIoTPlatform.DeviceType deviceType,
            String manufacturer,
            String model,
            String location) {

        ReportIoTPlatform platform = platformStore.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("IoT platform not found: " + platformId);
        }

        String deviceId = UUID.randomUUID().toString();

        ReportIoTPlatform.IoTDevice device = ReportIoTPlatform.IoTDevice.builder()
                .deviceId(deviceId)
                .deviceName(deviceName)
                .deviceType(deviceType)
                .status(ReportIoTPlatform.DeviceStatus.REGISTERED)
                .manufacturer(manufacturer)
                .model(model)
                .firmwareVersion("1.0.0")
                .hardwareVersion("1.0")
                .location(location)
                .ipAddress(generateIpAddress())
                .macAddress(generateMacAddress())
                .batteryLevel(100)
                .signalStrength(-50.0)
                .lastSeenAt(LocalDateTime.now())
                .registeredAt(LocalDateTime.now())
                .capabilities(new HashMap<>())
                .configuration(new HashMap<>())
                .metadata(new HashMap<>())
                .build();

        platform.registerDevice(device);

        log.info("IoT device registered: {}", deviceId);
        return device;
    }

    /**
     * Update device status
     */
    public void updateDeviceStatus(
            Long platformId,
            String deviceId,
            ReportIoTPlatform.DeviceStatus status) {

        ReportIoTPlatform platform = platformStore.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("IoT platform not found: " + platformId);
        }

        platform.updateDeviceStatus(deviceId, status);

        log.info("Device status updated: {} to {}", deviceId, status);
    }

    /**
     * Create device group
     */
    public ReportIoTPlatform.DeviceGroup createDeviceGroup(
            Long platformId,
            String groupName,
            String description,
            List<String> deviceIds,
            String location) {

        ReportIoTPlatform platform = platformStore.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("IoT platform not found: " + platformId);
        }

        String groupId = UUID.randomUUID().toString();

        ReportIoTPlatform.DeviceGroup group = ReportIoTPlatform.DeviceGroup.builder()
                .groupId(groupId)
                .groupName(groupName)
                .description(description)
                .deviceIds(deviceIds != null ? deviceIds : new ArrayList<>())
                .deviceCount(deviceIds != null ? deviceIds.size() : 0)
                .location(location)
                .purpose("Educational monitoring")
                .createdAt(LocalDateTime.now())
                .groupSettings(new HashMap<>())
                .build();

        if (platform.getDeviceGroups() == null) {
            platform.setDeviceGroups(new ArrayList<>());
        }
        platform.getDeviceGroups().add(group);

        if (platform.getGroupRegistry() == null) {
            platform.setGroupRegistry(new HashMap<>());
        }
        platform.getGroupRegistry().put(groupId, group);

        log.info("Device group created: {}", groupId);
        return group;
    }

    /**
     * Record sensor data
     */
    public ReportIoTPlatform.SensorData recordSensorData(
            Long platformId,
            String deviceId,
            ReportIoTPlatform.SensorType sensorType,
            String sensorName,
            Object value,
            String unit) {

        ReportIoTPlatform platform = platformStore.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("IoT platform not found: " + platformId);
        }

        String dataId = UUID.randomUUID().toString();

        ReportIoTPlatform.SensorData sensorData = ReportIoTPlatform.SensorData.builder()
                .dataId(dataId)
                .deviceId(deviceId)
                .sensorType(sensorType)
                .sensorName(sensorName)
                .value(value)
                .unit(unit)
                .quality(0.95 + Math.random() * 0.05)
                .timestamp(LocalDateTime.now())
                .additionalData(new HashMap<>())
                .build();

        platform.recordSensorData(sensorData);

        log.info("Sensor data recorded: {} from device {}", dataId, deviceId);
        return sensorData;
    }

    /**
     * Send device command
     */
    public ReportIoTPlatform.DeviceCommand sendCommand(
            Long platformId,
            String deviceId,
            String commandType,
            Map<String, Object> parameters,
            String issuedBy) {

        ReportIoTPlatform platform = platformStore.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("IoT platform not found: " + platformId);
        }

        String commandId = UUID.randomUUID().toString();

        ReportIoTPlatform.DeviceCommand command = ReportIoTPlatform.DeviceCommand.builder()
                .commandId(commandId)
                .deviceId(deviceId)
                .commandType(commandType)
                .status(ReportIoTPlatform.CommandStatus.SENT)
                .parameters(parameters != null ? parameters : new HashMap<>())
                .issuedAt(LocalDateTime.now())
                .issuedBy(issuedBy)
                .build();

        platform.sendCommand(command);

        log.info("Device command sent: {} to device {}", commandId, deviceId);
        return command;
    }

    /**
     * Complete command
     */
    public void completeCommand(
            Long platformId,
            String commandId,
            boolean success,
            Object response) {

        ReportIoTPlatform platform = platformStore.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("IoT platform not found: " + platformId);
        }

        platform.completeCommand(commandId, success);

        // Update response
        if (platform.getCommandRegistry() != null) {
            ReportIoTPlatform.DeviceCommand command = platform.getCommandRegistry().get(commandId);
            if (command != null) {
                command.setResponse(response);
            }
        }

        log.info("Device command completed: {} (success: {})", commandId, success);
    }

    /**
     * Record device telemetry
     */
    public ReportIoTPlatform.DeviceTelemetry recordTelemetry(
            Long platformId,
            String deviceId,
            Double cpuUsage,
            Double memoryUsage,
            Integer batteryLevel,
            Double signalStrength) {

        ReportIoTPlatform platform = platformStore.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("IoT platform not found: " + platformId);
        }

        String telemetryId = UUID.randomUUID().toString();

        ReportIoTPlatform.DeviceTelemetry telemetry = ReportIoTPlatform.DeviceTelemetry.builder()
                .telemetryId(telemetryId)
                .deviceId(deviceId)
                .cpuUsage(cpuUsage)
                .memoryUsage(memoryUsage)
                .diskUsage(0L)
                .temperature(25)
                .batteryLevel(batteryLevel)
                .signalStrength(signalStrength)
                .uptime(System.currentTimeMillis() / 1000)
                .messagesReceived(0L)
                .messagesSent(0L)
                .timestamp(LocalDateTime.now())
                .customMetrics(new HashMap<>())
                .build();

        if (platform.getTelemetryData() == null) {
            platform.setTelemetryData(new ArrayList<>());
        }
        platform.getTelemetryData().add(telemetry);

        if (platform.getTelemetryRegistry() == null) {
            platform.setTelemetryRegistry(new HashMap<>());
        }
        platform.getTelemetryRegistry().put(telemetryId, telemetry);

        log.info("Device telemetry recorded: {}", telemetryId);
        return telemetry;
    }

    /**
     * Update device twin
     */
    public ReportIoTPlatform.DeviceTwin updateDeviceTwin(
            Long platformId,
            String deviceId,
            Map<String, Object> reportedState,
            Map<String, Object> desiredState) {

        ReportIoTPlatform platform = platformStore.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("IoT platform not found: " + platformId);
        }

        String twinId = UUID.randomUUID().toString();

        ReportIoTPlatform.DeviceTwin twin = ReportIoTPlatform.DeviceTwin.builder()
                .twinId(twinId)
                .deviceId(deviceId)
                .reportedState(reportedState != null ? reportedState : new HashMap<>())
                .desiredState(desiredState != null ? desiredState : new HashMap<>())
                .tags(new HashMap<>())
                .version("1.0")
                .lastUpdated(LocalDateTime.now())
                .lastReported(LocalDateTime.now())
                .build();

        if (platform.getDeviceTwins() == null) {
            platform.setDeviceTwins(new ArrayList<>());
        }
        platform.getDeviceTwins().add(twin);

        if (platform.getTwinRegistry() == null) {
            platform.setTwinRegistry(new HashMap<>());
        }
        platform.getTwinRegistry().put(twinId, twin);

        log.info("Device twin updated: {}", twinId);
        return twin;
    }

    /**
     * Trigger alert
     */
    public ReportIoTPlatform.IoTAlert triggerAlert(
            Long platformId,
            String deviceId,
            String alertType,
            ReportIoTPlatform.AlertSeverity severity,
            String message,
            String description) {

        ReportIoTPlatform platform = platformStore.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("IoT platform not found: " + platformId);
        }

        String alertId = UUID.randomUUID().toString();

        ReportIoTPlatform.IoTAlert alert = ReportIoTPlatform.IoTAlert.builder()
                .alertId(alertId)
                .deviceId(deviceId)
                .alertType(alertType)
                .severity(severity)
                .message(message)
                .description(description)
                .isAcknowledged(false)
                .triggeredAt(LocalDateTime.now())
                .alertData(new HashMap<>())
                .build();

        platform.triggerAlert(alert);

        log.info("IoT alert triggered: {} (severity: {})", alertId, severity);
        return alert;
    }

    /**
     * Schedule firmware update
     */
    public ReportIoTPlatform.FirmwareUpdate scheduleFirmwareUpdate(
            Long platformId,
            String deviceId,
            String fromVersion,
            String toVersion,
            String updatePackageUrl) {

        ReportIoTPlatform platform = platformStore.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("IoT platform not found: " + platformId);
        }

        String updateId = UUID.randomUUID().toString();

        ReportIoTPlatform.FirmwareUpdate update = ReportIoTPlatform.FirmwareUpdate.builder()
                .updateId(updateId)
                .deviceId(deviceId)
                .fromVersion(fromVersion)
                .toVersion(toVersion)
                .updatePackageUrl(updatePackageUrl)
                .updateSizeBytes(5242880L) // 5MB
                .status("PENDING")
                .progressPercent(0)
                .scheduledAt(LocalDateTime.now())
                .build();

        if (platform.getFirmwareUpdates() == null) {
            platform.setFirmwareUpdates(new ArrayList<>());
        }
        platform.getFirmwareUpdates().add(update);

        if (platform.getFirmwareRegistry() == null) {
            platform.setFirmwareRegistry(new HashMap<>());
        }
        platform.getFirmwareRegistry().put(updateId, update);

        log.info("Firmware update scheduled: {}", updateId);
        return update;
    }

    /**
     * Register gateway
     */
    public ReportIoTPlatform.Gateway registerGateway(
            Long platformId,
            String gatewayName,
            String ipAddress,
            String protocol,
            Integer port) {

        ReportIoTPlatform platform = platformStore.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("IoT platform not found: " + platformId);
        }

        String gatewayId = UUID.randomUUID().toString();

        ReportIoTPlatform.Gateway gateway = ReportIoTPlatform.Gateway.builder()
                .gatewayId(gatewayId)
                .gatewayName(gatewayName)
                .status(ReportIoTPlatform.DeviceStatus.ONLINE)
                .ipAddress(ipAddress)
                .connectedDeviceIds(new ArrayList<>())
                .connectedDeviceCount(0)
                .protocol(protocol)
                .port(port)
                .messagesRouted(0L)
                .lastSeenAt(LocalDateTime.now())
                .configuration(new HashMap<>())
                .build();

        if (platform.getGateways() == null) {
            platform.setGateways(new ArrayList<>());
        }
        platform.getGateways().add(gateway);

        if (platform.getGatewayRegistry() == null) {
            platform.setGatewayRegistry(new HashMap<>());
        }
        platform.getGatewayRegistry().put(gatewayId, gateway);

        platform.setTotalGateways((platform.getTotalGateways() != null ? platform.getTotalGateways() : 0L) + 1);

        log.info("Gateway registered: {}", gatewayId);
        return gateway;
    }

    /**
     * Create IoT rule
     */
    public ReportIoTPlatform.IoTRule createRule(
            Long platformId,
            String ruleName,
            String description,
            String condition,
            List<String> actions) {

        ReportIoTPlatform platform = platformStore.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("IoT platform not found: " + platformId);
        }

        String ruleId = UUID.randomUUID().toString();

        ReportIoTPlatform.IoTRule rule = ReportIoTPlatform.IoTRule.builder()
                .ruleId(ruleId)
                .ruleName(ruleName)
                .description(description)
                .condition(condition)
                .actions(actions != null ? actions : new ArrayList<>())
                .isEnabled(true)
                .triggerCount(0)
                .createdAt(LocalDateTime.now())
                .ruleConfig(new HashMap<>())
                .build();

        if (platform.getRules() == null) {
            platform.setRules(new ArrayList<>());
        }
        platform.getRules().add(rule);

        if (platform.getRuleRegistry() == null) {
            platform.setRuleRegistry(new HashMap<>());
        }
        platform.getRuleRegistry().put(ruleId, rule);

        log.info("IoT rule created: {}", ruleId);
        return rule;
    }

    /**
     * Delete IoT platform
     */
    public void deleteIoTPlatform(Long platformId) {
        platformStore.remove(platformId);
        log.info("IoT platform deleted: {}", platformId);
    }

    /**
     * Get statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalPlatforms = platformStore.size();
        long activePlatforms = platformStore.values().stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                .count();

        long totalDevicesAcrossAll = platformStore.values().stream()
                .mapToLong(p -> p.getTotalDevices() != null ? p.getTotalDevices() : 0L)
                .sum();

        long totalReadingsAcrossAll = platformStore.values().stream()
                .mapToLong(p -> p.getTotalSensorReadings() != null ? p.getTotalSensorReadings() : 0L)
                .sum();

        stats.put("totalIoTPlatforms", totalPlatforms);
        stats.put("activeIoTPlatforms", activePlatforms);
        stats.put("totalDevices", totalDevicesAcrossAll);
        stats.put("totalSensorReadings", totalReadingsAcrossAll);
        stats.put("timestamp", LocalDateTime.now());

        return stats;
    }

    // Helper methods

    private String generateIpAddress() {
        return String.format("192.168.%d.%d",
            (int) (Math.random() * 255),
            (int) (Math.random() * 255));
    }

    private String generateMacAddress() {
        return String.format("%02X:%02X:%02X:%02X:%02X:%02X",
            (int) (Math.random() * 256),
            (int) (Math.random() * 256),
            (int) (Math.random() * 256),
            (int) (Math.random() * 256),
            (int) (Math.random() * 256),
            (int) (Math.random() * 256));
    }
}
