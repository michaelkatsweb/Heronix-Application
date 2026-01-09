package com.heronix.controller;

import com.heronix.dto.ReportIoTPlatform;
import com.heronix.service.ReportIoTPlatformService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report IoT Platform API Controller
 *
 * REST API endpoints for IoT device management, sensor data collection, and monitoring.
 *
 * Endpoints:
 * - POST /api/iot-platform - Create IoT platform
 * - GET /api/iot-platform/{id} - Get IoT platform
 * - POST /api/iot-platform/{id}/deploy - Deploy IoT platform
 * - POST /api/iot-platform/{id}/device - Register IoT device
 * - PUT /api/iot-platform/{id}/device/{deviceId}/status - Update device status
 * - POST /api/iot-platform/{id}/device-group - Create device group
 * - POST /api/iot-platform/{id}/sensor-data - Record sensor data
 * - POST /api/iot-platform/{id}/command - Send device command
 * - POST /api/iot-platform/{id}/command/{commandId}/complete - Complete command
 * - POST /api/iot-platform/{id}/telemetry - Record device telemetry
 * - POST /api/iot-platform/{id}/twin - Update device twin
 * - POST /api/iot-platform/{id}/alert - Trigger alert
 * - POST /api/iot-platform/{id}/firmware - Schedule firmware update
 * - POST /api/iot-platform/{id}/gateway - Register gateway
 * - POST /api/iot-platform/{id}/rule - Create IoT rule
 * - DELETE /api/iot-platform/{id} - Delete IoT platform
 * - GET /api/iot-platform/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 121 - Report Internet of Things (IoT) Platform
 */
@RestController
@RequestMapping("/api/iot-platform")
@RequiredArgsConstructor
@Slf4j
public class ReportIoTPlatformApiController {

    private final ReportIoTPlatformService iotPlatformService;

    /**
     * Create IoT platform
     */
    @PostMapping
    public ResponseEntity<ReportIoTPlatform> createIoTPlatform(@RequestBody ReportIoTPlatform platform) {
        log.info("POST /api/iot-platform - Creating IoT platform: {}", platform.getPlatformName());

        try {
            ReportIoTPlatform created = iotPlatformService.createIoTPlatform(platform);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating IoT platform", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get IoT platform
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportIoTPlatform> getIoTPlatform(@PathVariable Long id) {
        log.info("GET /api/iot-platform/{}", id);

        try {
            return iotPlatformService.getIoTPlatform(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching IoT platform: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Deploy IoT platform
     */
    @PostMapping("/{id}/deploy")
    public ResponseEntity<Map<String, Object>> deployIoTPlatform(@PathVariable Long id) {
        log.info("POST /api/iot-platform/{}/deploy", id);

        try {
            iotPlatformService.deployIoTPlatform(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "IoT platform deployed");
            response.put("platformId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("IoT platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error deploying IoT platform: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Register IoT device
     */
    @PostMapping("/{id}/device")
    public ResponseEntity<ReportIoTPlatform.IoTDevice> registerDevice(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/iot-platform/{}/device", id);

        try {
            String deviceName = request.get("deviceName");
            String deviceTypeStr = request.get("deviceType");
            String manufacturer = request.get("manufacturer");
            String model = request.get("model");
            String location = request.get("location");

            ReportIoTPlatform.DeviceType deviceType =
                    ReportIoTPlatform.DeviceType.valueOf(deviceTypeStr);

            ReportIoTPlatform.IoTDevice device = iotPlatformService.registerDevice(
                    id, deviceName, deviceType, manufacturer, model, location
            );

            return ResponseEntity.ok(device);

        } catch (IllegalArgumentException e) {
            log.error("IoT platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error registering IoT device: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update device status
     */
    @PutMapping("/{id}/device/{deviceId}/status")
    public ResponseEntity<Map<String, Object>> updateDeviceStatus(
            @PathVariable Long id,
            @PathVariable String deviceId,
            @RequestBody Map<String, String> request) {
        log.info("PUT /api/iot-platform/{}/device/{}/status", id, deviceId);

        try {
            String statusStr = request.get("status");
            ReportIoTPlatform.DeviceStatus status =
                    ReportIoTPlatform.DeviceStatus.valueOf(statusStr);

            iotPlatformService.updateDeviceStatus(id, deviceId, status);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Device status updated");
            response.put("deviceId", deviceId);
            response.put("status", status.toString());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("IoT platform or device not found: {} / {}", id, deviceId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating device status: {}", deviceId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create device group
     */
    @PostMapping("/{id}/device-group")
    public ResponseEntity<ReportIoTPlatform.DeviceGroup> createDeviceGroup(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/iot-platform/{}/device-group", id);

        try {
            String groupName = (String) request.get("groupName");
            String description = (String) request.get("description");
            @SuppressWarnings("unchecked")
            List<String> deviceIds = (List<String>) request.get("deviceIds");
            String location = (String) request.get("location");

            ReportIoTPlatform.DeviceGroup group = iotPlatformService.createDeviceGroup(
                    id, groupName, description, deviceIds, location
            );

            return ResponseEntity.ok(group);

        } catch (IllegalArgumentException e) {
            log.error("IoT platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating device group: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Record sensor data
     */
    @PostMapping("/{id}/sensor-data")
    public ResponseEntity<ReportIoTPlatform.SensorData> recordSensorData(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/iot-platform/{}/sensor-data", id);

        try {
            String deviceId = (String) request.get("deviceId");
            String sensorTypeStr = (String) request.get("sensorType");
            String sensorName = (String) request.get("sensorName");
            Object value = request.get("value");
            String unit = (String) request.get("unit");

            ReportIoTPlatform.SensorType sensorType =
                    ReportIoTPlatform.SensorType.valueOf(sensorTypeStr);

            ReportIoTPlatform.SensorData sensorData = iotPlatformService.recordSensorData(
                    id, deviceId, sensorType, sensorName, value, unit
            );

            return ResponseEntity.ok(sensorData);

        } catch (IllegalArgumentException e) {
            log.error("IoT platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error recording sensor data: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Send device command
     */
    @PostMapping("/{id}/command")
    public ResponseEntity<ReportIoTPlatform.DeviceCommand> sendCommand(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/iot-platform/{}/command", id);

        try {
            String deviceId = (String) request.get("deviceId");
            String commandType = (String) request.get("commandType");
            @SuppressWarnings("unchecked")
            Map<String, Object> parameters = (Map<String, Object>) request.get("parameters");
            String issuedBy = (String) request.get("issuedBy");

            ReportIoTPlatform.DeviceCommand command = iotPlatformService.sendCommand(
                    id, deviceId, commandType, parameters, issuedBy
            );

            return ResponseEntity.ok(command);

        } catch (IllegalArgumentException e) {
            log.error("IoT platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error sending device command: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Complete command
     */
    @PostMapping("/{id}/command/{commandId}/complete")
    public ResponseEntity<Map<String, Object>> completeCommand(
            @PathVariable Long id,
            @PathVariable String commandId,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/iot-platform/{}/command/{}/complete", id, commandId);

        try {
            Boolean success = (Boolean) request.get("success");
            Object response = request.get("response");

            iotPlatformService.completeCommand(id, commandId, success != null && success, response);

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("message", "Device command completed");
            responseMap.put("commandId", commandId);
            responseMap.put("success", success);

            return ResponseEntity.ok(responseMap);

        } catch (IllegalArgumentException e) {
            log.error("IoT platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error completing command: {}", commandId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Record device telemetry
     */
    @PostMapping("/{id}/telemetry")
    public ResponseEntity<ReportIoTPlatform.DeviceTelemetry> recordTelemetry(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/iot-platform/{}/telemetry", id);

        try {
            String deviceId = (String) request.get("deviceId");
            Double cpuUsage = request.get("cpuUsage") != null ?
                    ((Number) request.get("cpuUsage")).doubleValue() : 0.0;
            Double memoryUsage = request.get("memoryUsage") != null ?
                    ((Number) request.get("memoryUsage")).doubleValue() : 0.0;
            Integer batteryLevel = request.get("batteryLevel") != null ?
                    ((Number) request.get("batteryLevel")).intValue() : 100;
            Double signalStrength = request.get("signalStrength") != null ?
                    ((Number) request.get("signalStrength")).doubleValue() : -50.0;

            ReportIoTPlatform.DeviceTelemetry telemetry = iotPlatformService.recordTelemetry(
                    id, deviceId, cpuUsage, memoryUsage, batteryLevel, signalStrength
            );

            return ResponseEntity.ok(telemetry);

        } catch (IllegalArgumentException e) {
            log.error("IoT platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error recording telemetry: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update device twin
     */
    @PostMapping("/{id}/twin")
    public ResponseEntity<ReportIoTPlatform.DeviceTwin> updateDeviceTwin(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/iot-platform/{}/twin", id);

        try {
            String deviceId = (String) request.get("deviceId");
            @SuppressWarnings("unchecked")
            Map<String, Object> reportedState = (Map<String, Object>) request.get("reportedState");
            @SuppressWarnings("unchecked")
            Map<String, Object> desiredState = (Map<String, Object>) request.get("desiredState");

            ReportIoTPlatform.DeviceTwin twin = iotPlatformService.updateDeviceTwin(
                    id, deviceId, reportedState, desiredState
            );

            return ResponseEntity.ok(twin);

        } catch (IllegalArgumentException e) {
            log.error("IoT platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating device twin: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Trigger alert
     */
    @PostMapping("/{id}/alert")
    public ResponseEntity<ReportIoTPlatform.IoTAlert> triggerAlert(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/iot-platform/{}/alert", id);

        try {
            String deviceId = request.get("deviceId");
            String alertType = request.get("alertType");
            String severityStr = request.get("severity");
            String message = request.get("message");
            String description = request.get("description");

            ReportIoTPlatform.AlertSeverity severity =
                    ReportIoTPlatform.AlertSeverity.valueOf(severityStr);

            ReportIoTPlatform.IoTAlert alert = iotPlatformService.triggerAlert(
                    id, deviceId, alertType, severity, message, description
            );

            return ResponseEntity.ok(alert);

        } catch (IllegalArgumentException e) {
            log.error("IoT platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error triggering alert: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Schedule firmware update
     */
    @PostMapping("/{id}/firmware")
    public ResponseEntity<ReportIoTPlatform.FirmwareUpdate> scheduleFirmwareUpdate(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/iot-platform/{}/firmware", id);

        try {
            String deviceId = request.get("deviceId");
            String fromVersion = request.get("fromVersion");
            String toVersion = request.get("toVersion");
            String updatePackageUrl = request.get("updatePackageUrl");

            ReportIoTPlatform.FirmwareUpdate update = iotPlatformService.scheduleFirmwareUpdate(
                    id, deviceId, fromVersion, toVersion, updatePackageUrl
            );

            return ResponseEntity.ok(update);

        } catch (IllegalArgumentException e) {
            log.error("IoT platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error scheduling firmware update: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Register gateway
     */
    @PostMapping("/{id}/gateway")
    public ResponseEntity<ReportIoTPlatform.Gateway> registerGateway(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/iot-platform/{}/gateway", id);

        try {
            String gatewayName = (String) request.get("gatewayName");
            String ipAddress = (String) request.get("ipAddress");
            String protocol = (String) request.get("protocol");
            Integer port = request.get("port") != null ?
                    ((Number) request.get("port")).intValue() : 1883;

            ReportIoTPlatform.Gateway gateway = iotPlatformService.registerGateway(
                    id, gatewayName, ipAddress, protocol, port
            );

            return ResponseEntity.ok(gateway);

        } catch (IllegalArgumentException e) {
            log.error("IoT platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error registering gateway: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create IoT rule
     */
    @PostMapping("/{id}/rule")
    public ResponseEntity<ReportIoTPlatform.IoTRule> createRule(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/iot-platform/{}/rule", id);

        try {
            String ruleName = (String) request.get("ruleName");
            String description = (String) request.get("description");
            String condition = (String) request.get("condition");
            @SuppressWarnings("unchecked")
            List<String> actions = (List<String>) request.get("actions");

            ReportIoTPlatform.IoTRule rule = iotPlatformService.createRule(
                    id, ruleName, description, condition, actions
            );

            return ResponseEntity.ok(rule);

        } catch (IllegalArgumentException e) {
            log.error("IoT platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating IoT rule: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete IoT platform
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteIoTPlatform(@PathVariable Long id) {
        log.info("DELETE /api/iot-platform/{}", id);

        try {
            iotPlatformService.deleteIoTPlatform(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "IoT platform deleted");
            response.put("platformId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting IoT platform: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/iot-platform/stats");

        try {
            Map<String, Object> stats = iotPlatformService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching IoT platform statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
