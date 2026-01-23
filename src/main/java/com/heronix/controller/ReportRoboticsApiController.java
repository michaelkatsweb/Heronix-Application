package com.heronix.controller;

import com.heronix.dto.ReportRobotics;
import com.heronix.service.ReportRoboticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Robotics & Automation API Controller
 *
 * REST API endpoints for robotics systems, automation workflows, and robot fleet management.
 *
 * Endpoints:
 * - POST /api/robotics - Create robotics system
 * - GET /api/robotics/{id} - Get robotics system
 * - POST /api/robotics/{id}/deploy - Deploy robotics system
 * - POST /api/robotics/{id}/robot - Register robot
 * - POST /api/robotics/{id}/task - Assign task
 * - POST /api/robotics/{id}/task/complete - Complete task
 * - POST /api/robotics/{id}/mission - Start mission
 * - POST /api/robotics/{id}/mission/complete - Complete mission
 * - POST /api/robotics/{id}/path - Create navigation path
 * - POST /api/robotics/{id}/sensor - Record sensor reading
 * - POST /api/robotics/{id}/actuator - Activate actuator
 * - POST /api/robotics/{id}/workflow - Start automation workflow
 * - POST /api/robotics/{id}/safety - Record safety event
 * - POST /api/robotics/{id}/telemetry - Record telemetry
 * - DELETE /api/robotics/{id} - Delete robotics system
 * - GET /api/robotics/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 118 - Report Robotics & Automation
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/robotics")
@RequiredArgsConstructor
@Slf4j
public class ReportRoboticsApiController {

    private final ReportRoboticsService roboticsService;

    /**
     * Create robotics system
     */
    @PostMapping
    public ResponseEntity<ReportRobotics> createRoboticsSystem(@RequestBody ReportRobotics robotics) {
        log.info("POST /api/robotics - Creating robotics system: {}", robotics.getRoboticsName());

        try {
            ReportRobotics created = roboticsService.createRoboticsSystem(robotics);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating robotics system", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get robotics system
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportRobotics> getRoboticsSystem(@PathVariable Long id) {
        log.info("GET /api/robotics/{}", id);

        try {
            return roboticsService.getRoboticsSystem(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching robotics system: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Deploy robotics system
     */
    @PostMapping("/{id}/deploy")
    public ResponseEntity<Map<String, Object>> deploySystem(@PathVariable Long id) {
        log.info("POST /api/robotics/{}/deploy", id);

        try {
            roboticsService.deploySystem(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Robotics system deployed");
            response.put("roboticsId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Robotics system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error deploying robotics system: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Register robot
     */
    @PostMapping("/{id}/robot")
    public ResponseEntity<ReportRobotics.Robot> registerRobot(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/robotics/{}/robot", id);

        try {
            String robotName = request.get("robotName");
            String robotTypeStr = request.get("robotType");
            String manufacturer = request.get("manufacturer");

            ReportRobotics.RobotType robotType =
                    ReportRobotics.RobotType.valueOf(robotTypeStr);

            ReportRobotics.Robot robot = roboticsService.registerRobot(
                    id, robotName, robotType, manufacturer
            );

            return ResponseEntity.ok(robot);

        } catch (IllegalArgumentException e) {
            log.error("Robotics system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error registering robot: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Assign task
     */
    @PostMapping("/{id}/task")
    public ResponseEntity<ReportRobotics.RobotTask> assignTask(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/robotics/{}/task", id);

        try {
            String taskName = (String) request.get("taskName");
            String taskType = (String) request.get("taskType");
            String assignedRobotId = (String) request.get("assignedRobotId");
            @SuppressWarnings("unchecked")
            Map<String, Object> parameters = (Map<String, Object>) request.get("parameters");

            ReportRobotics.RobotTask task = roboticsService.assignTask(
                    id, taskName, taskType, assignedRobotId, parameters
            );

            return ResponseEntity.ok(task);

        } catch (IllegalArgumentException e) {
            log.error("Robotics system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error assigning task: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Complete task
     */
    @PostMapping("/{id}/task/complete")
    public ResponseEntity<Map<String, Object>> completeTask(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/robotics/{}/task/complete", id);

        try {
            String taskId = (String) request.get("taskId");
            Boolean success = (Boolean) request.get("success");

            roboticsService.completeTask(id, taskId, success != null && success);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Task completed");
            response.put("taskId", taskId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Robotics system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error completing task: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Start mission
     */
    @PostMapping("/{id}/mission")
    public ResponseEntity<ReportRobotics.Mission> startMission(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/robotics/{}/mission", id);

        try {
            String missionName = (String) request.get("missionName");
            String description = (String) request.get("description");
            @SuppressWarnings("unchecked")
            List<String> taskIds = (List<String>) request.get("taskIds");
            @SuppressWarnings("unchecked")
            List<String> assignedRobotIds = (List<String>) request.get("assignedRobotIds");

            ReportRobotics.Mission mission = roboticsService.startMission(
                    id, missionName, description, taskIds, assignedRobotIds
            );

            return ResponseEntity.ok(mission);

        } catch (IllegalArgumentException e) {
            log.error("Robotics system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error starting mission: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Complete mission
     */
    @PostMapping("/{id}/mission/complete")
    public ResponseEntity<Map<String, Object>> completeMission(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/robotics/{}/mission/complete", id);

        try {
            String missionId = (String) request.get("missionId");
            Boolean success = (Boolean) request.get("success");

            roboticsService.completeMission(id, missionId, success != null && success);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Mission completed");
            response.put("missionId", missionId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Robotics system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error completing mission: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create navigation path
     */
    @PostMapping("/{id}/path")
    public ResponseEntity<ReportRobotics.NavigationPath> createNavigationPath(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/robotics/{}/path", id);

        try {
            String pathName = (String) request.get("pathName");
            String robotId = (String) request.get("robotId");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> waypointMaps = (List<Map<String, Object>>) request.get("waypoints");

            List<ReportRobotics.Waypoint> waypoints = null;
            if (waypointMaps != null) {
                waypoints = waypointMaps.stream()
                        .map(this::mapToWaypoint)
                        .toList();
            }

            ReportRobotics.NavigationPath path = roboticsService.createNavigationPath(
                    id, pathName, robotId, waypoints
            );

            return ResponseEntity.ok(path);

        } catch (IllegalArgumentException e) {
            log.error("Robotics system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating navigation path: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Record sensor reading
     */
    @PostMapping("/{id}/sensor")
    public ResponseEntity<ReportRobotics.SensorReading> recordSensorReading(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/robotics/{}/sensor", id);

        try {
            String sensorId = (String) request.get("sensorId");
            String robotId = (String) request.get("robotId");
            String sensorType = (String) request.get("sensorType");
            Double value = request.get("value") != null ?
                    ((Number) request.get("value")).doubleValue() : 0.0;
            String unit = (String) request.get("unit");

            ReportRobotics.SensorReading reading = roboticsService.recordSensorReading(
                    id, sensorId, robotId, sensorType, value, unit
            );

            return ResponseEntity.ok(reading);

        } catch (IllegalArgumentException e) {
            log.error("Robotics system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error recording sensor reading: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Activate actuator
     */
    @PostMapping("/{id}/actuator")
    public ResponseEntity<ReportRobotics.Actuator> activateActuator(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/robotics/{}/actuator", id);

        try {
            String actuatorName = (String) request.get("actuatorName");
            String robotId = (String) request.get("robotId");
            String actuatorType = (String) request.get("actuatorType");
            Double targetPosition = request.get("targetPosition") != null ?
                    ((Number) request.get("targetPosition")).doubleValue() : 0.0;

            ReportRobotics.Actuator actuator = roboticsService.activateActuator(
                    id, actuatorName, robotId, actuatorType, targetPosition
            );

            return ResponseEntity.ok(actuator);

        } catch (IllegalArgumentException e) {
            log.error("Robotics system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error activating actuator: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Start automation workflow
     */
    @PostMapping("/{id}/workflow")
    public ResponseEntity<ReportRobotics.AutomationWorkflow> startWorkflow(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/robotics/{}/workflow", id);

        try {
            String workflowName = (String) request.get("workflowName");
            String description = (String) request.get("description");
            String triggerType = (String) request.get("triggerType");
            @SuppressWarnings("unchecked")
            List<String> steps = (List<String>) request.get("steps");

            ReportRobotics.AutomationWorkflow workflow = roboticsService.startWorkflow(
                    id, workflowName, description, triggerType, steps
            );

            return ResponseEntity.ok(workflow);

        } catch (IllegalArgumentException e) {
            log.error("Robotics system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error starting automation workflow: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Record safety event
     */
    @PostMapping("/{id}/safety")
    public ResponseEntity<ReportRobotics.SafetyEvent> recordSafetyEvent(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/robotics/{}/safety", id);

        try {
            String safetyLevelStr = request.get("safetyLevel");
            String robotId = request.get("robotId");
            String eventType = request.get("eventType");
            String description = request.get("description");

            ReportRobotics.SafetyLevel safetyLevel =
                    ReportRobotics.SafetyLevel.valueOf(safetyLevelStr);

            ReportRobotics.SafetyEvent event = roboticsService.recordSafetyEvent(
                    id, safetyLevel, robotId, eventType, description
            );

            return ResponseEntity.ok(event);

        } catch (IllegalArgumentException e) {
            log.error("Robotics system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error recording safety event: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Record telemetry
     */
    @PostMapping("/{id}/telemetry")
    public ResponseEntity<ReportRobotics.TelemetryData> recordTelemetry(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/robotics/{}/telemetry", id);

        try {
            String robotId = (String) request.get("robotId");
            Double batteryLevel = request.get("batteryLevel") != null ?
                    ((Number) request.get("batteryLevel")).doubleValue() : 100.0;
            Double cpuUsage = request.get("cpuUsage") != null ?
                    ((Number) request.get("cpuUsage")).doubleValue() : 0.0;
            Double temperature = request.get("temperature") != null ?
                    ((Number) request.get("temperature")).doubleValue() : 25.0;

            ReportRobotics.TelemetryData telemetry = roboticsService.recordTelemetry(
                    id, robotId, batteryLevel, cpuUsage, temperature
            );

            return ResponseEntity.ok(telemetry);

        } catch (IllegalArgumentException e) {
            log.error("Robotics system not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error recording telemetry: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete robotics system
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteRoboticsSystem(@PathVariable Long id) {
        log.info("DELETE /api/robotics/{}", id);

        try {
            roboticsService.deleteRoboticsSystem(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Robotics system deleted");
            response.put("roboticsId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting robotics system: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/robotics/stats");

        try {
            Map<String, Object> stats = roboticsService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching robotics statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Helper methods

    private ReportRobotics.Waypoint mapToWaypoint(Map<String, Object> map) {
        return ReportRobotics.Waypoint.builder()
                .waypointId(java.util.UUID.randomUUID().toString())
                .x(map.get("x") != null ? ((Number) map.get("x")).doubleValue() : 0.0)
                .y(map.get("y") != null ? ((Number) map.get("y")).doubleValue() : 0.0)
                .z(map.get("z") != null ? ((Number) map.get("z")).doubleValue() : 0.0)
                .orientation(map.get("orientation") != null ? ((Number) map.get("orientation")).doubleValue() : 0.0)
                .waypointType((String) map.get("waypointType"))
                .isReached(false)
                .build();
    }
}
