package com.heronix.controller;

import com.heronix.dto.ReportIoT;
import com.heronix.service.ReportIoTService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report IoT Integration API Controller
 *
 * REST API endpoints for IoT devices and sensor data.
 *
 * Endpoints:
 * - POST /api/iot - Create IoT platform
 * - GET /api/iot/{id} - Get IoT platform
 * - POST /api/iot/{id}/start - Start platform
 * - POST /api/iot/{id}/stop - Stop platform
 * - POST /api/iot/{id}/device - Provision device
 * - POST /api/iot/{id}/sensor - Register sensor
 * - POST /api/iot/{id}/telemetry - Send telemetry data
 * - POST /api/iot/{id}/twin - Create device twin
 * - POST /api/iot/{id}/command - Send device command
 * - POST /api/iot/{id}/stream - Create data stream
 * - GET /api/iot/{id}/devices/online - Get online devices
 * - GET /api/iot/{id}/alerts/active - Get active alerts
 * - PUT /api/iot/{id}/metrics - Update metrics
 * - DELETE /api/iot/{id} - Delete platform
 * - GET /api/iot/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 103 - Report IoT Integration & Sensor Data
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/iot")
@RequiredArgsConstructor
@Slf4j
public class ReportIoTApiController {

    private final ReportIoTService iotService;

    /**
     * Create IoT platform
     */
    @PostMapping
    public ResponseEntity<ReportIoT> createPlatform(@RequestBody ReportIoT platform) {
        log.info("POST /api/iot - Creating IoT platform: {}", platform.getPlatformName());

        try {
            ReportIoT created = iotService.createPlatform(platform);
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
    public ResponseEntity<ReportIoT> getPlatform(@PathVariable Long id) {
        log.info("GET /api/iot/{}", id);

        try {
            return iotService.getPlatform(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching IoT platform: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Start platform
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<Map<String, Object>> startPlatform(@PathVariable Long id) {
        log.info("POST /api/iot/{}/start", id);

        try {
            iotService.startPlatform(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "IoT platform started");
            response.put("platformId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("IoT platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error starting IoT platform: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Stop platform
     */
    @PostMapping("/{id}/stop")
    public ResponseEntity<Map<String, Object>> stopPlatform(@PathVariable Long id) {
        log.info("POST /api/iot/{}/stop", id);

        try {
            iotService.stopPlatform(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "IoT platform stopped");
            response.put("platformId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("IoT platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error stopping IoT platform: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Provision device
     */
    @PostMapping("/{id}/device")
    public ResponseEntity<ReportIoT.IoTDevice> provisionDevice(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/iot/{}/device", id);

        try {
            String deviceName = request.get("deviceName");
            String deviceType = request.get("deviceType");
            String manufacturer = request.get("manufacturer");
            String model = request.get("model");
            String protocolStr = request.get("protocol");

            ReportIoT.Protocol protocol = ReportIoT.Protocol.valueOf(protocolStr);

            ReportIoT.IoTDevice device = iotService.provisionDevice(
                    id, deviceName, deviceType, manufacturer, model, protocol
            );

            return ResponseEntity.ok(device);

        } catch (IllegalArgumentException e) {
            log.error("IoT platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error provisioning device in IoT platform: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Register sensor
     */
    @PostMapping("/{id}/sensor")
    public ResponseEntity<ReportIoT.Sensor> registerSensor(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/iot/{}/sensor", id);

        try {
            String sensorName = (String) request.get("sensorName");
            String deviceId = (String) request.get("deviceId");
            String sensorTypeStr = (String) request.get("sensorType");
            String unit = (String) request.get("unit");
            Double minValue = request.get("minValue") != null ?
                    ((Number) request.get("minValue")).doubleValue() : null;
            Double maxValue = request.get("maxValue") != null ?
                    ((Number) request.get("maxValue")).doubleValue() : null;

            ReportIoT.SensorType sensorType = ReportIoT.SensorType.valueOf(sensorTypeStr);

            ReportIoT.Sensor sensor = iotService.registerSensor(
                    id, sensorName, deviceId, sensorType, unit, minValue, maxValue
            );

            return ResponseEntity.ok(sensor);

        } catch (IllegalArgumentException e) {
            log.error("IoT platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error registering sensor in IoT platform: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Send telemetry data
     */
    @PostMapping("/{id}/telemetry")
    public ResponseEntity<ReportIoT.TelemetryData> sendTelemetry(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/iot/{}/telemetry", id);

        try {
            String deviceId = (String) request.get("deviceId");
            String sensorId = (String) request.get("sensorId");
            Double value = ((Number) request.get("value")).doubleValue();
            String unit = (String) request.get("unit");

            ReportIoT.TelemetryData telemetry = iotService.sendTelemetry(
                    id, deviceId, sensorId, value, unit
            );

            return ResponseEntity.ok(telemetry);

        } catch (IllegalArgumentException e) {
            log.error("IoT platform or sensor not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error sending telemetry to IoT platform: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create device twin
     */
    @PostMapping("/{id}/twin")
    public ResponseEntity<ReportIoT.DeviceTwin> createDeviceTwin(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/iot/{}/twin", id);

        try {
            String deviceId = (String) request.get("deviceId");
            @SuppressWarnings("unchecked")
            Map<String, Object> reportedProperties = (Map<String, Object>) request.get("reportedProperties");
            @SuppressWarnings("unchecked")
            Map<String, Object> desiredProperties = (Map<String, Object>) request.get("desiredProperties");

            ReportIoT.DeviceTwin twin = iotService.createDeviceTwin(
                    id, deviceId, reportedProperties, desiredProperties
            );

            return ResponseEntity.ok(twin);

        } catch (IllegalArgumentException e) {
            log.error("IoT platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating device twin in IoT platform: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Send device command
     */
    @PostMapping("/{id}/command")
    public ResponseEntity<ReportIoT.DeviceCommand> sendCommand(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/iot/{}/command", id);

        try {
            String deviceId = (String) request.get("deviceId");
            String commandType = (String) request.get("commandType");
            @SuppressWarnings("unchecked")
            Map<String, Object> parameters = (Map<String, Object>) request.get("parameters");

            ReportIoT.DeviceCommand command = iotService.sendCommand(
                    id, deviceId, commandType, parameters
            );

            return ResponseEntity.ok(command);

        } catch (IllegalArgumentException e) {
            log.error("IoT platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error sending command in IoT platform: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create data stream
     */
    @PostMapping("/{id}/stream")
    public ResponseEntity<ReportIoT.DataStream> createDataStream(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/iot/{}/stream", id);

        try {
            String streamName = (String) request.get("streamName");
            String deviceId = (String) request.get("deviceId");
            @SuppressWarnings("unchecked")
            List<String> sensorIds = (List<String>) request.get("sensorIds");
            String protocolStr = (String) request.get("protocol");
            Integer intervalMs = request.get("intervalMs") != null ?
                    ((Number) request.get("intervalMs")).intValue() : 1000;

            ReportIoT.Protocol protocol = ReportIoT.Protocol.valueOf(protocolStr);

            ReportIoT.DataStream stream = iotService.createDataStream(
                    id, streamName, deviceId, sensorIds, protocol, intervalMs
            );

            return ResponseEntity.ok(stream);

        } catch (IllegalArgumentException e) {
            log.error("IoT platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating data stream in IoT platform: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get online devices
     */
    @GetMapping("/{id}/devices/online")
    public ResponseEntity<Map<String, Object>> getOnlineDevices(@PathVariable Long id) {
        log.info("GET /api/iot/{}/devices/online", id);

        try {
            return iotService.getPlatform(id)
                    .map(platform -> {
                        List<ReportIoT.IoTDevice> onlineDevices = platform.getOnlineDevices();
                        Map<String, Object> response = new HashMap<>();
                        response.put("platformId", id);
                        response.put("devices", onlineDevices);
                        response.put("count", onlineDevices.size());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching online devices for IoT platform: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get active alerts
     */
    @GetMapping("/{id}/alerts/active")
    public ResponseEntity<Map<String, Object>> getActiveAlerts(@PathVariable Long id) {
        log.info("GET /api/iot/{}/alerts/active", id);

        try {
            return iotService.getPlatform(id)
                    .map(platform -> {
                        List<ReportIoT.SensorAlert> activeAlerts = platform.getActiveAlerts();
                        Map<String, Object> response = new HashMap<>();
                        response.put("platformId", id);
                        response.put("alerts", activeAlerts);
                        response.put("count", activeAlerts.size());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching active alerts for IoT platform: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update metrics
     */
    @PutMapping("/{id}/metrics")
    public ResponseEntity<Map<String, Object>> updateMetrics(@PathVariable Long id) {
        log.info("PUT /api/iot/{}/metrics", id);

        try {
            iotService.updateMetrics(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Metrics updated");
            response.put("platformId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("IoT platform not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating metrics for IoT platform: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete platform
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePlatform(@PathVariable Long id) {
        log.info("DELETE /api/iot/{}", id);

        try {
            iotService.deletePlatform(id);

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
        log.info("GET /api/iot/stats");

        try {
            Map<String, Object> stats = iotService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching IoT statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
