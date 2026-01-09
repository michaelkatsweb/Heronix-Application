package com.heronix.controller.api;

import com.heronix.model.domain.Course;
import com.heronix.model.domain.Room;
import com.heronix.model.domain.Teacher;
import com.heronix.repository.CourseRepository;
import com.heronix.repository.RoomRepository;
import com.heronix.repository.TeacherRepository;
import com.heronix.service.RoomEquipmentService;
import com.heronix.service.RoomService;
import com.heronix.service.RoomZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Room Management
 * Covers room equipment matching and zone management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomManagementApiController {

    private final RoomService roomService;
    private final RoomEquipmentService equipmentService;
    private final RoomZoneService zoneService;
    private final RoomRepository roomRepository;
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;

    // ==================== Room CRUD Operations ====================

    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms() {
        List<Room> rooms = roomService.findAll();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Room>> getAllActiveRooms() {
        List<Room> rooms = roomService.getAllActiveRooms();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        Room room = roomService.getRoomById(id);
        return ResponseEntity.ok(room);
    }

    // ==================== Equipment Compatibility ====================

    @GetMapping("/equipment/compatibility")
    public ResponseEntity<Map<String, Object>> getEquipmentCompatibility(
            @RequestParam Long courseId,
            @RequestParam Long roomId) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

        int score = equipmentService.calculateCompatibilityScore(course, room);
        boolean meetsRequirements = equipmentService.meetsRequirements(course, room);
        List<String> missingEquipment = equipmentService.getMissingEquipment(course, room);
        int penalty = equipmentService.getEquipmentPenalty(course, room);

        Map<String, Object> compatibility = new HashMap<>();
        compatibility.put("courseId", courseId);
        compatibility.put("courseName", course.getCourseName());
        compatibility.put("roomId", roomId);
        compatibility.put("roomNumber", room.getRoomNumber());
        compatibility.put("compatibilityScore", score);
        compatibility.put("meetsRequirements", meetsRequirements);
        compatibility.put("missingEquipment", missingEquipment);
        compatibility.put("equipmentPenalty", penalty);
        compatibility.put("status", meetsRequirements ? "Compatible" : "Incompatible");

        return ResponseEntity.ok(compatibility);
    }

    @GetMapping("/equipment/course/{courseId}/missing")
    public ResponseEntity<List<String>> getMissingEquipmentForCourse(
            @PathVariable Long courseId,
            @RequestParam Long roomId) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

        List<String> missingEquipment = equipmentService.getMissingEquipment(course, room);
        return ResponseEntity.ok(missingEquipment);
    }

    @GetMapping("/equipment/course/{courseId}/summary")
    public ResponseEntity<Map<String, Object>> getEquipmentSummary(@PathVariable Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        String summary = equipmentService.getEquipmentSummary(course);
        boolean hasRequirements = equipmentService.hasEquipmentRequirements(course);

        Map<String, Object> response = new HashMap<>();
        response.put("courseId", courseId);
        response.put("courseName", course.getCourseName());
        response.put("equipmentSummary", summary);
        response.put("hasRequirements", hasRequirements);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/equipment/course/{courseId}/compatible")
    public ResponseEntity<List<Room>> getCompatibleRooms(@PathVariable Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        List<Room> allRooms = roomService.getAllActiveRooms();
        List<Room> compatibleRooms = allRooms.stream()
                .filter(room -> equipmentService.meetsRequirements(course, room))
                .toList();

        return ResponseEntity.ok(compatibleRooms);
    }

    // ==================== Zone Management ====================

    @PostMapping("/zones/auto-assign")
    public ResponseEntity<Map<String, String>> autoAssignZones() {
        zoneService.autoAssignZones();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Zones auto-assigned successfully");
        response.put("status", "completed");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{roomId}/zone/suggested")
    public ResponseEntity<Map<String, String>> getSuggestedZone(@PathVariable Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

        String suggestedZone = zoneService.getSuggestedZone(room);

        Map<String, String> response = new HashMap<>();
        response.put("roomId", roomId.toString());
        response.put("roomNumber", room.getRoomNumber());
        response.put("currentZone", room.getZone());
        response.put("suggestedZone", suggestedZone);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{roomId}/zone/same-zone-rooms")
    public ResponseEntity<List<Room>> getRoomsInSameZone(@PathVariable Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

        List<Room> sameZoneRooms = zoneService.getRoomsInSameZone(room);
        return ResponseEntity.ok(sameZoneRooms);
    }

    @GetMapping("/zones/teacher/{teacherId}/preference")
    public ResponseEntity<Map<String, Object>> getZonePreferenceScore(
            @PathVariable Long teacherId,
            @RequestParam Long roomId) {

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + teacherId));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

        int score = zoneService.getZonePreferenceScore(teacher, room);

        Map<String, Object> response = new HashMap<>();
        response.put("teacherId", teacherId);
        response.put("teacherName", teacher.getName());
        response.put("roomId", roomId);
        response.put("roomNumber", room.getRoomNumber());
        response.put("preferenceScore", score);
        response.put("teacherDepartment", teacher.getDepartment());
        response.put("roomZone", room.getZone());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/zones/department/{department}")
    public ResponseEntity<Map<String, String>> getDepartmentZone(@PathVariable String department) {
        String zone = zoneService.getDepartmentZone(department);

        Map<String, String> response = new HashMap<>();
        response.put("department", department);
        response.put("zone", zone);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/zones/travel-penalty")
    public ResponseEntity<Map<String, Object>> calculateTravelPenalty(
            @RequestParam Long room1Id,
            @RequestParam Long room2Id) {

        Room room1 = roomRepository.findById(room1Id)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + room1Id));
        Room room2 = roomRepository.findById(room2Id)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + room2Id));

        int penalty = zoneService.calculateTravelPenalty(room1, room2);

        Map<String, Object> response = new HashMap<>();
        response.put("room1Id", room1Id);
        response.put("room1Number", room1.getRoomNumber());
        response.put("room2Id", room2Id);
        response.put("room2Number", room2.getRoomNumber());
        response.put("travelPenalty", penalty);
        response.put("sameZone", room1.getZone() != null && room1.getZone().equals(room2.getZone()));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/zones/list")
    public ResponseEntity<List<String>> getAllZones() {
        List<String> zones = zoneService.getAllZones();
        return ResponseEntity.ok(zones);
    }

    @GetMapping("/buildings/list")
    public ResponseEntity<List<String>> getAllBuildings() {
        List<String> buildings = zoneService.getAllBuildings();
        return ResponseEntity.ok(buildings);
    }

    @GetMapping("/zones/{zone}")
    public ResponseEntity<List<Room>> getRoomsByZone(@PathVariable String zone) {
        List<Room> rooms = zoneService.getRoomsByZone(zone);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/building/{building}/floor/{floor}")
    public ResponseEntity<List<Room>> getRoomsByBuildingAndFloor(
            @PathVariable String building,
            @PathVariable Integer floor) {
        List<Room> rooms = zoneService.getRoomsByBuildingAndFloor(building, floor);
        return ResponseEntity.ok(rooms);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getRoomOverview() {
        Map<String, Object> dashboard = new HashMap<>();

        List<Room> allRooms = roomService.findAll();
        List<Room> activeRooms = roomService.getAllActiveRooms();
        List<String> zones = zoneService.getAllZones();
        List<String> buildings = zoneService.getAllBuildings();

        dashboard.put("totalRooms", allRooms.size());
        dashboard.put("activeRooms", activeRooms.size());
        dashboard.put("inactiveRooms", allRooms.size() - activeRooms.size());
        dashboard.put("totalZones", zones.size());
        dashboard.put("totalBuildings", buildings.size());
        dashboard.put("zones", zones);
        dashboard.put("buildings", buildings);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/room/{roomId}")
    public ResponseEntity<Map<String, Object>> getRoomDashboard(@PathVariable Long roomId) {
        Map<String, Object> dashboard = new HashMap<>();

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

        List<Room> sameZoneRooms = zoneService.getRoomsInSameZone(room);
        String suggestedZone = zoneService.getSuggestedZone(room);

        dashboard.put("roomId", roomId);
        dashboard.put("roomNumber", room.getRoomNumber());
        dashboard.put("capacity", room.getCapacity());
        dashboard.put("type", room.getType());
        dashboard.put("zone", room.getZone());
        dashboard.put("building", room.getBuilding());
        dashboard.put("floor", room.getFloor());
        dashboard.put("suggestedZone", suggestedZone);
        dashboard.put("sameZoneRoomsCount", sameZoneRooms.size());

        // Equipment details
        Map<String, Object> equipment = new HashMap<>();
        equipment.put("hasProjector", room.getHasProjector());
        equipment.put("hasSmartboard", room.getHasSmartboard());
        equipment.put("hasComputers", room.getHasComputers());
        equipment.put("equipment", room.getEquipment());
        dashboard.put("equipment", equipment);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/equipment-summary")
    public ResponseEntity<Map<String, Object>> getEquipmentSummaryDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        List<Room> allRooms = roomService.getAllActiveRooms();

        long roomsWithProjector = allRooms.stream()
                .filter(r -> Boolean.TRUE.equals(r.getHasProjector()))
                .count();
        long roomsWithSmartboard = allRooms.stream()
                .filter(r -> Boolean.TRUE.equals(r.getHasSmartboard()))
                .count();
        long roomsWithComputers = allRooms.stream()
                .filter(r -> Boolean.TRUE.equals(r.getHasComputers()))
                .count();

        dashboard.put("totalRooms", allRooms.size());
        dashboard.put("roomsWithProjector", roomsWithProjector);
        dashboard.put("roomsWithSmartboard", roomsWithSmartboard);
        dashboard.put("roomsWithComputers", roomsWithComputers);
        dashboard.put("percentProjector", (roomsWithProjector * 100.0 / allRooms.size()));
        dashboard.put("percentSmartboard", (roomsWithSmartboard * 100.0 / allRooms.size()));
        dashboard.put("percentComputers", (roomsWithComputers * 100.0 / allRooms.size()));

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/zone-distribution")
    public ResponseEntity<Map<String, Object>> getZoneDistributionDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        List<String> zones = zoneService.getAllZones();
        Map<String, Long> zoneDistribution = new HashMap<>();

        for (String zone : zones) {
            long count = zoneService.getRoomsByZone(zone).size();
            zoneDistribution.put(zone, count);
        }

        dashboard.put("totalZones", zones.size());
        dashboard.put("zoneDistribution", zoneDistribution);
        dashboard.put("zones", zones);

        return ResponseEntity.ok(dashboard);
    }
}
