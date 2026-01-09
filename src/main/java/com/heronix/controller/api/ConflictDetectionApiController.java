package com.heronix.controller.api;

import com.heronix.model.domain.Room;
import com.heronix.model.domain.Schedule;
import com.heronix.model.domain.Teacher;
import com.heronix.model.dto.Conflict;
import com.heronix.repository.RoomRepository;
import com.heronix.repository.ScheduleRepository;
import com.heronix.repository.TeacherRepository;
import com.heronix.service.ConflictDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * REST API Controller for Conflict Detection
 *
 * Provides endpoints for detecting and resolving scheduling conflicts:
 * - Teacher double-booking
 * - Room conflicts
 * - Capacity violations
 * - Time slot conflicts
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/conflict-detection")
@RequiredArgsConstructor
public class ConflictDetectionApiController {

    private final ConflictDetectionService conflictDetectionService;
    private final ScheduleRepository scheduleRepository;
    private final TeacherRepository teacherRepository;
    private final RoomRepository roomRepository;

    // ==================== Schedule Conflict Detection ====================

    @GetMapping("/schedule/{scheduleId}")
    public ResponseEntity<Map<String, Object>> detectScheduleConflicts(@PathVariable Long scheduleId) {
        Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);

        if (scheduleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Conflict> conflicts = conflictDetectionService.detectConflicts(scheduleId);

        Map<String, Object> response = new HashMap<>();
        response.put("scheduleId", scheduleId);
        response.put("conflictCount", conflicts.size());
        response.put("hasConflicts", !conflicts.isEmpty());
        response.put("conflicts", conflicts);
        response.put("status", conflicts.isEmpty() ? "CLEAN" : "CONFLICTS_FOUND");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/schedule/{scheduleId}/descriptions")
    public ResponseEntity<Map<String, Object>> getConflictDescriptions(@PathVariable Long scheduleId) {
        Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);

        if (scheduleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<String> descriptions = conflictDetectionService.detectAllConflicts(scheduleOpt.get());

        Map<String, Object> response = new HashMap<>();
        response.put("scheduleId", scheduleId);
        response.put("conflictCount", descriptions.size());
        response.put("hasConflicts", !descriptions.isEmpty());
        response.put("descriptions", descriptions);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/schedule/{scheduleId}/auto-resolve")
    public ResponseEntity<Map<String, Object>> autoResolveConflicts(@PathVariable Long scheduleId) {
        Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);

        if (scheduleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            boolean resolved = conflictDetectionService.autoResolveConflicts(scheduleId);

            Map<String, Object> response = new HashMap<>();
            response.put("scheduleId", scheduleId);
            response.put("success", resolved);
            response.put("message", resolved ?
                "All conflicts resolved successfully" :
                "Some conflicts could not be automatically resolved");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Slot-Specific Conflicts ====================

    @GetMapping("/slot/{slotId}")
    public ResponseEntity<Map<String, Object>> checkSlotConflicts(@PathVariable Long slotId) {
        List<Conflict> conflicts = conflictDetectionService.checkSlotConflicts(slotId);

        Map<String, Object> response = new HashMap<>();
        response.put("slotId", slotId);
        response.put("conflictCount", conflicts.size());
        response.put("hasConflicts", !conflicts.isEmpty());
        response.put("conflicts", conflicts);

        return ResponseEntity.ok(response);
    }

    // ==================== Teacher Conflicts ====================

    @GetMapping("/teacher/{teacherId}/check")
    public ResponseEntity<Map<String, Object>> checkTeacherConflict(
            @PathVariable Long teacherId,
            @RequestParam String day,
            @RequestParam String startTime,
            @RequestParam String endTime) {

        Optional<Teacher> teacherOpt = teacherRepository.findById(teacherId);

        if (teacherOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            DayOfWeek dayOfWeek = DayOfWeek.valueOf(day.toUpperCase());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime start = LocalTime.parse(startTime, formatter);
            LocalTime end = LocalTime.parse(endTime, formatter);

            boolean hasConflict = conflictDetectionService.hasTeacherConflict(
                teacherId, dayOfWeek, start, end);

            Map<String, Object> response = new HashMap<>();
            response.put("teacherId", teacherId);
            response.put("day", day);
            response.put("startTime", startTime);
            response.put("endTime", endTime);
            response.put("hasConflict", hasConflict);
            response.put("available", !hasConflict);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid date/time format: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/teacher/{teacherId}/period/{periodNumber}")
    public ResponseEntity<Map<String, Object>> checkTeacherPeriodConflict(
            @PathVariable Long teacherId,
            @PathVariable Integer periodNumber,
            @RequestParam(required = false, defaultValue = "DAILY") String dayType,
            @RequestParam Long scheduleId) {

        Optional<Teacher> teacherOpt = teacherRepository.findById(teacherId);

        if (teacherOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        boolean hasConflict = conflictDetectionService.hasTeacherConflict(
            teacherOpt.get(), periodNumber, dayType, scheduleId);

        Map<String, Object> response = new HashMap<>();
        response.put("teacherId", teacherId);
        response.put("periodNumber", periodNumber);
        response.put("dayType", dayType);
        response.put("scheduleId", scheduleId);
        response.put("hasConflict", hasConflict);
        response.put("available", !hasConflict);

        return ResponseEntity.ok(response);
    }

    // ==================== Room Conflicts ====================

    @GetMapping("/room/{roomId}/check")
    public ResponseEntity<Map<String, Object>> checkRoomConflict(
            @PathVariable Long roomId,
            @RequestParam String day,
            @RequestParam String startTime,
            @RequestParam String endTime) {

        Optional<Room> roomOpt = roomRepository.findById(roomId);

        if (roomOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            DayOfWeek dayOfWeek = DayOfWeek.valueOf(day.toUpperCase());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime start = LocalTime.parse(startTime, formatter);
            LocalTime end = LocalTime.parse(endTime, formatter);

            boolean hasConflict = conflictDetectionService.hasRoomConflict(
                roomId, dayOfWeek, start, end);

            Map<String, Object> response = new HashMap<>();
            response.put("roomId", roomId);
            response.put("day", day);
            response.put("startTime", startTime);
            response.put("endTime", endTime);
            response.put("hasConflict", hasConflict);
            response.put("available", !hasConflict);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid date/time format: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/room/{roomId}/period/{periodNumber}")
    public ResponseEntity<Map<String, Object>> checkRoomPeriodConflict(
            @PathVariable Long roomId,
            @PathVariable Integer periodNumber,
            @RequestParam(required = false, defaultValue = "DAILY") String dayType,
            @RequestParam Long scheduleId) {

        Optional<Room> roomOpt = roomRepository.findById(roomId);

        if (roomOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        boolean hasConflict = conflictDetectionService.hasRoomConflict(
            roomOpt.get(), periodNumber, dayType, scheduleId);

        Map<String, Object> response = new HashMap<>();
        response.put("roomId", roomId);
        response.put("periodNumber", periodNumber);
        response.put("dayType", dayType);
        response.put("scheduleId", scheduleId);
        response.put("hasConflict", hasConflict);
        response.put("available", !hasConflict);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/room/{roomId}/capacity")
    public ResponseEntity<Map<String, Object>> checkCapacityConflict(
            @PathVariable Long roomId,
            @RequestParam Integer enrollment) {

        Optional<Room> roomOpt = roomRepository.findById(roomId);

        if (roomOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Room room = roomOpt.get();
        boolean hasConflict = conflictDetectionService.hasCapacityConflict(room, enrollment);

        Map<String, Object> response = new HashMap<>();
        response.put("roomId", roomId);
        response.put("roomNumber", room.getRoomNumber());
        response.put("roomCapacity", room.getCapacity());
        response.put("requestedEnrollment", enrollment);
        response.put("hasConflict", hasConflict);
        response.put("exceedsCapacity", hasConflict);
        response.put("availableSeats", hasConflict ? 0 : room.getCapacity() - enrollment);

        return ResponseEntity.ok(response);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/summary/{scheduleId}")
    public ResponseEntity<Map<String, Object>> getConflictSummary(@PathVariable Long scheduleId) {
        Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);

        if (scheduleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Conflict> conflicts = conflictDetectionService.detectConflicts(scheduleId);

        // Categorize conflicts
        long teacherConflicts = conflicts.stream()
            .filter(c -> c.getConflictType() != null && c.getConflictType().contains("TEACHER"))
            .count();

        long roomConflicts = conflicts.stream()
            .filter(c -> c.getConflictType() != null && c.getConflictType().contains("ROOM"))
            .count();

        long capacityConflicts = conflicts.stream()
            .filter(c -> c.getConflictType() != null && c.getConflictType().contains("CAPACITY"))
            .count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("scheduleId", scheduleId);
        summary.put("totalConflicts", conflicts.size());
        summary.put("teacherConflicts", teacherConflicts);
        summary.put("roomConflicts", roomConflicts);
        summary.put("capacityConflicts", capacityConflicts);
        summary.put("hasConflicts", !conflicts.isEmpty());
        summary.put("status", conflicts.isEmpty() ? "CLEAN" : "NEEDS_RESOLUTION");

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/dashboard/overview/{scheduleId}")
    public ResponseEntity<Map<String, Object>> getDashboard(@PathVariable Long scheduleId) {
        Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);

        if (scheduleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Conflict> conflicts = conflictDetectionService.detectConflicts(scheduleId);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("scheduleId", scheduleId);
        dashboard.put("conflictCount", conflicts.size());
        dashboard.put("hasConflicts", !conflicts.isEmpty());
        dashboard.put("conflicts", conflicts);
        dashboard.put("status", conflicts.isEmpty() ? "CLEAN" : "CONFLICTS_DETECTED");
        dashboard.put("severity", conflicts.isEmpty() ? "NONE" :
            conflicts.size() > 10 ? "HIGH" :
            conflicts.size() > 5 ? "MEDIUM" : "LOW");

        return ResponseEntity.ok(dashboard);
    }

    // ==================== Utility Endpoints ====================

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("version", "2.0.0");
        metadata.put("features", Map.of(
            "scheduleConflictDetection", "Detect all conflicts in a schedule",
            "slotConflictDetection", "Check specific slot for conflicts",
            "teacherAvailability", "Check teacher availability at specific times",
            "roomAvailability", "Check room availability at specific times",
            "capacityValidation", "Validate room capacity vs enrollment",
            "autoResolution", "Attempt to automatically resolve conflicts"
        ));

        metadata.put("conflictTypes", Map.of(
            "TEACHER_CONFLICT", "Teacher double-booked at same time",
            "ROOM_CONFLICT", "Room double-booked at same time",
            "CAPACITY_CONFLICT", "Enrollment exceeds room capacity",
            "TIME_CONFLICT", "Time slot overlap"
        ));

        return ResponseEntity.ok(metadata);
    }
}
