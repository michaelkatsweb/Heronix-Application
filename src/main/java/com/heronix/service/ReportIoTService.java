package com.heronix.service;

import com.heronix.dto.ReportIoT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Report IoT Integration Service
 *
 * Manages IoT devices and sensor data for report generation.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 103 - Report IoT Integration & Sensor Data
 */
@Service
@Slf4j
public class ReportIoTService {

    private final Map<Long, ReportIoT> platforms = new ConcurrentHashMap<>();
    private Long nextPlatformId = 1L;

    /**
     * Create IoT platform
     */
    public ReportIoT createPlatform(ReportIoT platform) {
        synchronized (this) {
            platform.setPlatformId(nextPlatformId++);
        }

        platform.setStatus(ReportIoT.PlatformStatus.INITIALIZING);
        platform.setCreatedAt(LocalDateTime.now());
        platform.setIsActive(false);

        // Initialize collections
        if (platform.getDevices() == null) {
            platform.setDevices(new ArrayList<>());
        }
        if (platform.getSensors() == null) {
            platform.setSensors(new ArrayList<>());
        }
        if (platform.getTelemetryData() == null) {
            platform.setTelemetryData(new ArrayList<>());
        }
        if (platform.getDeviceTwins() == null) {
            platform.setDeviceTwins(new ArrayList<>());
        }
        if (platform.getCommands() == null) {
            platform.setCommands(new ArrayList<>());
        }
        if (platform.getAlerts() == null) {
            platform.setAlerts(new ArrayList<>());
        }
        if (platform.getDataStreams() == null) {
            platform.setDataStreams(new ArrayList<>());
        }
        if (platform.getEvents() == null) {
            platform.setEvents(new ArrayList<>());
        }

        // Initialize registries
        platform.setDeviceRegistry(new ConcurrentHashMap<>());
        platform.setSensorRegistry(new ConcurrentHashMap<>());
        platform.setTelemetryRegistry(new ConcurrentHashMap<>());
        platform.setTwinRegistry(new ConcurrentHashMap<>());
        platform.setCommandRegistry(new ConcurrentHashMap<>());
        platform.setAlertRegistry(new ConcurrentHashMap<>());
        platform.setStreamRegistry(new ConcurrentHashMap<>());

        // Initialize counters
        platform.setTotalDevices(0);
        platform.setOnlineDevices(0);
        platform.setOfflineDevices(0);
        platform.setActiveDevices(0);
        platform.setTotalSensors(0);
        platform.setActiveSensors(0);
        platform.setTotalDataPoints(0L);
        platform.setDataPointsToday(0L);
        platform.setTotalCommands(0L);
        platform.setSuccessfulCommands(0L);
        platform.setFailedCommands(0L);
        platform.setTotalAlerts(0L);
        platform.setActiveAlerts(0L);
        platform.setTotalStreams(0);
        platform.setActiveStreams(0);

        platforms.put(platform.getPlatformId(), platform);
        log.info("Created IoT platform: {} (ID: {})", platform.getPlatformName(), platform.getPlatformId());

        return platform;
    }

    /**
     * Get IoT platform
     */
    public Optional<ReportIoT> getPlatform(Long platformId) {
        return Optional.ofNullable(platforms.get(platformId));
    }

    /**
     * Start platform
     */
    public void startPlatform(Long platformId) {
        ReportIoT platform = platforms.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("IoT platform not found: " + platformId);
        }

        platform.startPlatform();
        log.info("Started IoT platform: {}", platformId);
    }

    /**
     * Stop platform
     */
    public void stopPlatform(Long platformId) {
        ReportIoT platform = platforms.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("IoT platform not found: " + platformId);
        }

        platform.stopPlatform();
        log.info("Stopped IoT platform: {}", platformId);
    }

    /**
     * Provision device
     */
    public ReportIoT.IoTDevice provisionDevice(Long platformId, String deviceName, String deviceType,
                                                String manufacturer, String model,
                                                ReportIoT.Protocol protocol) {
        ReportIoT platform = platforms.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("IoT platform not found: " + platformId);
        }

        ReportIoT.IoTDevice device = ReportIoT.IoTDevice.builder()
                .deviceId(UUID.randomUUID().toString())
                .deviceName(deviceName)
                .status(ReportIoT.DeviceStatus.PROVISIONING)
                .deviceType(deviceType)
                .manufacturer(manufacturer)
                .model(model)
                .firmwareVersion("1.0.0")
                .protocol(protocol)
                .ipAddress(generateIpAddress())
                .macAddress(generateMacAddress())
                .sensorIds(new ArrayList<>())
                .batteryLevel(100)
                .signalStrength(80)
                .totalDataPoints(0L)
                .uptime(0L)
                .provisionedAt(LocalDateTime.now())
                .tags(new HashMap<>())
                .metadata(new HashMap<>())
                .build();

        // Device goes online after provisioning
        device.setStatus(ReportIoT.DeviceStatus.ONLINE);
        device.setLastSeenAt(LocalDateTime.now());

        platform.registerDevice(device);
        log.info("Provisioned device {} on platform {}", deviceName, platformId);

        return device;
    }

    /**
     * Register sensor
     */
    public ReportIoT.Sensor registerSensor(Long platformId, String sensorName, String deviceId,
                                            ReportIoT.SensorType sensorType, String unit,
                                            Double minValue, Double maxValue) {
        ReportIoT platform = platforms.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("IoT platform not found: " + platformId);
        }

        ReportIoT.Sensor sensor = ReportIoT.Sensor.builder()
                .sensorId(UUID.randomUUID().toString())
                .sensorName(sensorName)
                .sensorType(sensorType)
                .deviceId(deviceId)
                .unit(unit)
                .minValue(minValue)
                .maxValue(maxValue)
                .currentValue(0.0)
                .accuracy(0.95)
                .sampleRate(1000)
                .totalReadings(0L)
                .calibrated(true)
                .calibratedAt(LocalDateTime.now())
                .tags(new HashMap<>())
                .metadata(new HashMap<>())
                .build();

        platform.registerSensor(sensor);

        // Add sensor to device
        ReportIoT.IoTDevice device = platform.getDevice(deviceId);
        if (device != null) {
            device.getSensorIds().add(sensor.getSensorId());
        }

        log.info("Registered sensor {} for device {} on platform {}", sensorName, deviceId, platformId);

        return sensor;
    }

    /**
     * Send telemetry data
     */
    public ReportIoT.TelemetryData sendTelemetry(Long platformId, String deviceId, String sensorId,
                                                  Double value, String unit) {
        ReportIoT platform = platforms.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("IoT platform not found: " + platformId);
        }

        ReportIoT.Sensor sensor = platform.getSensor(sensorId);
        if (sensor == null) {
            throw new IllegalArgumentException("Sensor not found: " + sensorId);
        }

        ReportIoT.TelemetryData telemetry = ReportIoT.TelemetryData.builder()
                .telemetryId(UUID.randomUUID().toString())
                .deviceId(deviceId)
                .sensorId(sensorId)
                .sensorType(sensor.getSensorType())
                .timestamp(LocalDateTime.now())
                .value(value)
                .unit(unit)
                .quality(95)
                .additionalData(new HashMap<>())
                .processed(false)
                .metadata(new HashMap<>())
                .build();

        platform.recordTelemetry(telemetry);

        // Check for alerts
        checkAlertConditions(platform, sensor, value);

        log.debug("Received telemetry from sensor {} on device {} in platform {}: {} {}",
                sensorId, deviceId, platformId, value, unit);

        return telemetry;
    }

    /**
     * Create device twin
     */
    public ReportIoT.DeviceTwin createDeviceTwin(Long platformId, String deviceId,
                                                  Map<String, Object> reportedProperties,
                                                  Map<String, Object> desiredProperties) {
        ReportIoT platform = platforms.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("IoT platform not found: " + platformId);
        }

        ReportIoT.DeviceTwin twin = ReportIoT.DeviceTwin.builder()
                .twinId(UUID.randomUUID().toString())
                .deviceId(deviceId)
                .reportedProperties(reportedProperties != null ? reportedProperties : new HashMap<>())
                .desiredProperties(desiredProperties != null ? desiredProperties : new HashMap<>())
                .tags(new HashMap<>())
                .lastUpdatedAt(LocalDateTime.now())
                .version(1)
                .metadata(new HashMap<>())
                .build();

        platform.createDeviceTwin(twin);
        log.info("Created device twin for device {} on platform {}", deviceId, platformId);

        return twin;
    }

    /**
     * Send device command
     */
    public ReportIoT.DeviceCommand sendCommand(Long platformId, String deviceId, String commandType,
                                                Map<String, Object> parameters) {
        ReportIoT platform = platforms.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("IoT platform not found: " + platformId);
        }

        ReportIoT.DeviceCommand command = ReportIoT.DeviceCommand.builder()
                .commandId(UUID.randomUUID().toString())
                .deviceId(deviceId)
                .commandType(commandType)
                .status(ReportIoT.CommandStatus.PENDING)
                .parameters(parameters != null ? parameters : new HashMap<>())
                .sentAt(LocalDateTime.now())
                .metadata(new HashMap<>())
                .build();

        platform.sendCommand(command);

        // Simulate command execution
        command.setStatus(ReportIoT.CommandStatus.SENT);
        command.setAcknowledgedAt(LocalDateTime.now().plusSeconds(1));
        command.setStatus(ReportIoT.CommandStatus.ACKNOWLEDGED);

        // Simulate completion
        Random random = new Random();
        boolean success = random.nextDouble() > 0.05; // 95% success rate

        platform.completeCommand(command.getCommandId(), success);

        if (success) {
            command.setResponse("Command executed successfully");
        } else {
            command.setErrorMessage("Simulated command failure");
        }

        log.info("Sent command {} to device {} on platform {}: {}",
                commandType, deviceId, platformId, success ? "SUCCESS" : "FAILED");

        return command;
    }

    /**
     * Create data stream
     */
    public ReportIoT.DataStream createDataStream(Long platformId, String streamName, String deviceId,
                                                  List<String> sensorIds, ReportIoT.Protocol protocol,
                                                  Integer intervalMs) {
        ReportIoT platform = platforms.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("IoT platform not found: " + platformId);
        }

        ReportIoT.DataStream stream = ReportIoT.DataStream.builder()
                .streamId(UUID.randomUUID().toString())
                .streamName(streamName)
                .deviceId(deviceId)
                .sensorIds(sensorIds != null ? sensorIds : new ArrayList<>())
                .protocol(protocol)
                .endpoint(generateStreamEndpoint(protocol))
                .intervalMs(intervalMs != null ? intervalMs : 1000)
                .active(true)
                .totalMessages(0L)
                .messagesPerSecond(0L)
                .startedAt(LocalDateTime.now())
                .configuration(new HashMap<>())
                .metadata(new HashMap<>())
                .build();

        platform.createDataStream(stream);
        log.info("Created data stream {} for device {} on platform {}", streamName, deviceId, platformId);

        return stream;
    }

    /**
     * Update metrics
     */
    public void updateMetrics(Long platformId) {
        ReportIoT platform = platforms.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("IoT platform not found: " + platformId);
        }

        int totalDevices = platform.getTotalDevices() != null ? platform.getTotalDevices() : 0;
        int onlineDevices = platform.getOnlineDevices() != null ? platform.getOnlineDevices().size() : 0;
        int offlineDevices = platform.getOfflineDevices() != null ? platform.getOfflineDevices() : 0;

        double deviceAvailability = totalDevices > 0 ?
                (onlineDevices * 100.0 / totalDevices) : 0.0;

        long totalDataPoints = platform.getTotalDataPoints() != null ? platform.getTotalDataPoints() : 0L;

        // Calculate data points per minute (last 60 seconds)
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        long dataPointsPerMinute = platform.getTelemetryData() != null ?
                platform.getTelemetryData().stream()
                        .filter(t -> t.getTimestamp().isAfter(oneMinuteAgo))
                        .count() : 0L;

        // Calculate average battery level
        double avgBattery = platform.getDevices() != null ?
                platform.getDevices().stream()
                        .filter(d -> d.getBatteryLevel() != null)
                        .mapToInt(ReportIoT.IoTDevice::getBatteryLevel)
                        .average()
                        .orElse(0.0) : 0.0;

        // Calculate average signal strength
        double avgSignal = platform.getDevices() != null ?
                platform.getDevices().stream()
                        .filter(d -> d.getSignalStrength() != null)
                        .mapToInt(ReportIoT.IoTDevice::getSignalStrength)
                        .average()
                        .orElse(0.0) : 0.0;

        long totalCommands = platform.getTotalCommands() != null ? platform.getTotalCommands() : 0L;
        long successfulCommands = platform.getSuccessfulCommands() != null ? platform.getSuccessfulCommands() : 0L;

        double commandSuccessRate = totalCommands > 0 ?
                (successfulCommands * 100.0 / totalCommands) : 0.0;

        long activeAlerts = platform.getActiveAlerts() != null ? platform.getActiveAlerts().size() : 0L;
        int activeStreams = platform.getActiveStreams() != null ? platform.getActiveStreams() : 0;

        // Calculate total data volume (estimate)
        long totalDataVolumeMb = totalDataPoints / 1000; // Rough estimate

        ReportIoT.IoTMetrics metrics = ReportIoT.IoTMetrics.builder()
                .totalDevices(totalDevices)
                .onlineDevices(onlineDevices)
                .offlineDevices(offlineDevices)
                .deviceAvailability(deviceAvailability)
                .totalDataPoints(totalDataPoints)
                .dataPointsPerMinute(dataPointsPerMinute)
                .averageBatteryLevel(avgBattery)
                .averageSignalStrength(avgSignal)
                .totalCommands(totalCommands)
                .successfulCommands(successfulCommands)
                .commandSuccessRate(commandSuccessRate)
                .activeAlerts(activeAlerts)
                .activeStreams(activeStreams)
                .totalDataVolumeMb(totalDataVolumeMb)
                .measuredAt(LocalDateTime.now())
                .build();

        platform.setMetrics(metrics);
        platform.setLastMetricsUpdate(LocalDateTime.now());

        log.debug("Updated metrics for IoT platform {}: {} devices, {} data points, {:.1f}% availability",
                platformId, totalDevices, totalDataPoints, deviceAvailability);
    }

    /**
     * Delete platform
     */
    public void deletePlatform(Long platformId) {
        ReportIoT platform = platforms.get(platformId);
        if (platform != null && platform.isHealthy()) {
            stopPlatform(platformId);
        }

        ReportIoT removed = platforms.remove(platformId);
        if (removed != null) {
            log.info("Deleted IoT platform {}", platformId);
        }
    }

    /**
     * Get statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalPlatforms", platforms.size());

        long activePlatforms = platforms.values().stream()
                .filter(ReportIoT::isHealthy)
                .count();

        long totalDevices = platforms.values().stream()
                .mapToLong(p -> p.getTotalDevices() != null ? p.getTotalDevices() : 0L)
                .sum();

        long onlineDevices = platforms.values().stream()
                .mapToLong(p -> p.getOnlineDevices() != null ? p.getOnlineDevices().size() : 0L)
                .sum();

        long totalSensors = platforms.values().stream()
                .mapToLong(p -> p.getTotalSensors() != null ? p.getTotalSensors() : 0L)
                .sum();

        long totalDataPoints = platforms.values().stream()
                .mapToLong(p -> p.getTotalDataPoints() != null ? p.getTotalDataPoints() : 0L)
                .sum();

        long activeAlerts = platforms.values().stream()
                .mapToLong(p -> p.getActiveAlerts() != null ? p.getActiveAlerts().size() : 0L)
                .sum();

        stats.put("activePlatforms", activePlatforms);
        stats.put("totalDevices", totalDevices);
        stats.put("onlineDevices", onlineDevices);
        stats.put("totalSensors", totalSensors);
        stats.put("totalDataPoints", totalDataPoints);
        stats.put("activeAlerts", activeAlerts);

        log.debug("Generated IoT statistics: {} platforms, {} devices, {} data points",
                platforms.size(), totalDevices, totalDataPoints);

        return stats;
    }

    // Helper Methods

    private String generateIpAddress() {
        Random random = new Random();
        return "192.168." + random.nextInt(255) + "." + (random.nextInt(254) + 1);
    }

    private String generateMacAddress() {
        Random random = new Random();
        StringBuilder mac = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            if (i > 0) mac.append(":");
            mac.append(String.format("%02X", random.nextInt(256)));
        }
        return mac.toString();
    }

    private String generateStreamEndpoint(ReportIoT.Protocol protocol) {
        switch (protocol) {
            case MQTT:
                return "mqtt://broker.example.com:1883/topic/" + UUID.randomUUID();
            case HTTP:
            case HTTPS:
                return "https://api.example.com/stream/" + UUID.randomUUID();
            case WEBSOCKET:
                return "wss://stream.example.com/" + UUID.randomUUID();
            case COAP:
                return "coap://server.example.com:5683/stream/" + UUID.randomUUID();
            default:
                return "stream://" + UUID.randomUUID();
        }
    }

    private void checkAlertConditions(ReportIoT platform, ReportIoT.Sensor sensor, Double value) {
        // Check if value exceeds thresholds
        if (sensor.getMaxValue() != null && value > sensor.getMaxValue()) {
            ReportIoT.SensorAlert alert = ReportIoT.SensorAlert.builder()
                    .alertId(UUID.randomUUID().toString())
                    .deviceId(sensor.getDeviceId())
                    .sensorId(sensor.getSensorId())
                    .severity(ReportIoT.AlertSeverity.WARNING)
                    .alertType("THRESHOLD_EXCEEDED")
                    .message("Sensor value exceeds maximum threshold")
                    .thresholdValue(sensor.getMaxValue())
                    .actualValue(value)
                    .triggeredAt(LocalDateTime.now())
                    .active(true)
                    .metadata(new HashMap<>())
                    .build();

            platform.triggerAlert(alert);
        } else if (sensor.getMinValue() != null && value < sensor.getMinValue()) {
            ReportIoT.SensorAlert alert = ReportIoT.SensorAlert.builder()
                    .alertId(UUID.randomUUID().toString())
                    .deviceId(sensor.getDeviceId())
                    .sensorId(sensor.getSensorId())
                    .severity(ReportIoT.AlertSeverity.WARNING)
                    .alertType("THRESHOLD_BELOW")
                    .message("Sensor value below minimum threshold")
                    .thresholdValue(sensor.getMinValue())
                    .actualValue(value)
                    .triggeredAt(LocalDateTime.now())
                    .active(true)
                    .metadata(new HashMap<>())
                    .build();

            platform.triggerAlert(alert);
        }
    }
}
