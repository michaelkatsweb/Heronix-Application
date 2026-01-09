package com.heronix.controller.api;

import com.heronix.model.domain.HallPassSession;
import com.heronix.model.domain.HallPassSession.Destination;
import com.heronix.repository.HallPassSessionRepository;
import com.heronix.service.impl.HallPassService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Hall Pass Management
 *
 * Provides endpoints for digital hall pass system:
 * - Hall pass session start/end
 * - Active session tracking
 * - Overdue detection
 * - Session history
 *
 * Features:
 * - QR code and facial recognition verification
 * - Automatic duration tracking
 * - Multiple destination support
 * - Parent notifications
 * - Complete audit trail
 *
 * Destinations: RESTROOM, NURSE, OFFICE, LIBRARY, LOCKER, COUNSELOR, OTHER
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 32 - December 29, 2025
 */
@RestController
@RequestMapping("/api/hall-pass")
@RequiredArgsConstructor
public class HallPassApiController {

    private final HallPassService hallPassService;
    private final HallPassSessionRepository hallPassRepository;

    // ==================== Hall Pass Operations ====================

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startHallPass(@RequestBody Map<String, Object> requestBody) {
        try {
            String qrCodeData = (String) requestBody.get("qrCodeData");
            String destinationStr = (String) requestBody.get("destination");
            Long teacherId = requestBody.get("teacherId") != null ?
                Long.valueOf(requestBody.get("teacherId").toString()) : null;
            Integer period = requestBody.get("period") != null ?
                Integer.valueOf(requestBody.get("period").toString()) : null;
            String departureRoom = (String) requestBody.get("departureRoom");

            Destination destination = Destination.valueOf(destinationStr);

            // For API, we don't handle photo capture - that's done by mobile app
            HallPassService.HallPassResult result = hallPassService.startHallPass(
                qrCodeData, null, destination, teacherId, period, departureRoom);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());

            if (result.isSuccess()) {
                response.put("session", result.getSession());
                response.put("student", result.getStudent());
            }

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to start hall pass: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/end")
    public ResponseEntity<Map<String, Object>> endHallPass(@RequestBody Map<String, Object> requestBody) {
        try {
            String qrCodeData = (String) requestBody.get("qrCodeData");
            String returnRoom = (String) requestBody.get("returnRoom");

            HallPassService.HallPassResult result = hallPassService.endHallPass(qrCodeData, null, returnRoom);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());

            if (result.isSuccess()) {
                response.put("session", result.getSession());
                response.put("student", result.getStudent());
                response.put("duration", result.getSession().getDurationMinutes());
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to end hall pass: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Session Queries ====================

    @GetMapping("/sessions/active")
    public ResponseEntity<Map<String, Object>> getActiveSessions() {
        try {
            List<HallPassSession> sessions = hallPassRepository.findAllActive();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("activeSessions", sessions);
            response.put("count", sessions.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get active sessions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/sessions/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getStudentSessions(@PathVariable Long studentId) {
        try {
            List<HallPassSession> sessions = hallPassRepository.findByStudent_IdOrderByDepartureTimeDesc(studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("sessions", sessions);
            response.put("totalSessions", sessions.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get sessions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/sessions/student/{studentId}/active")
    public ResponseEntity<Map<String, Object>> getActiveSessionByStudent(@PathVariable Long studentId) {
        try {
            HallPassSession session = hallPassRepository.findActiveByStudentId(studentId).orElse(null);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("hasActiveSession", session != null);
            response.put("activeSession", session);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get active session: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/sessions/overdue")
    public ResponseEntity<Map<String, Object>> getOverdueSessions() {
        try {
            // Calculate overdue threshold (15 minutes ago by default)
            LocalDateTime overdueThreshold = LocalDateTime.now().minusMinutes(15);
            List<HallPassSession> sessions = hallPassRepository.findOverdueSessions(overdueThreshold);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("overdueSessions", sessions);
            response.put("count", sessions.size());
            response.put("note", "Sessions exceeding max duration threshold");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get overdue sessions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Dashboard ====================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        try {
            List<HallPassSession> active = hallPassRepository.findAllActive();
            LocalDateTime overdueThreshold = LocalDateTime.now().minusMinutes(15);
            List<HallPassSession> overdue = hallPassRepository.findOverdueSessions(overdueThreshold);

            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("activeSessions", active.size());
            dashboard.put("overdueSessions", overdue.size());
            dashboard.put("activeDetails", active);
            dashboard.put("overdueDetails", overdue);

            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get dashboard: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Reference Data ====================

    @GetMapping("/reference/destinations")
    public ResponseEntity<Map<String, Object>> getDestinations() {
        Map<String, Object> destinations = new HashMap<>();
        destinations.put("destinations", List.of(
            Map.of("value", "RESTROOM", "displayName", "Restroom"),
            Map.of("value", "NURSE", "displayName", "Nurse's Office"),
            Map.of("value", "OFFICE", "displayName", "Main Office"),
            Map.of("value", "LIBRARY", "displayName", "Library"),
            Map.of("value", "LOCKER", "displayName", "Locker"),
            Map.of("value", "COUNSELOR", "displayName", "Counselor"),
            Map.of("value", "OTHER", "displayName", "Other")
        ));
        return ResponseEntity.ok(destinations);
    }

    // ==================== Metadata ====================

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("apiVersion", "1.0.0");
        metadata.put("phase", "Phase 32");
        metadata.put("category", "School Operations");
        metadata.put("description", "Digital hall pass system with QR code and facial recognition");

        metadata.put("capabilities", List.of(
            "Start hall pass sessions",
            "End hall pass sessions",
            "Track active sessions",
            "Detect overdue sessions",
            "Student session history",
            "QR code verification",
            "Facial recognition support",
            "Parent notifications"
        ));

        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = new HashMap<>();
        help.put("description", "Hall Pass Management API");

        help.put("commonWorkflows", Map.of(
            "issueHallPass", List.of(
                "1. POST /api/hall-pass/start with student QR code",
                "2. System verifies student and checks for active passes",
                "3. Hall pass session created and tracked"
            ),
            "returnFromPass", List.of(
                "1. POST /api/hall-pass/end with student QR code",
                "2. Session closed with duration calculated",
                "3. Parent notification sent if enabled"
            ),
            "monitorActive", List.of(
                "1. GET /api/hall-pass/sessions/active",
                "2. GET /api/hall-pass/sessions/overdue",
                "3. Take action on overdue passes"
            )
        ));

        help.put("endpoints", Map.of(
            "start", "POST /api/hall-pass/start",
            "end", "POST /api/hall-pass/end",
            "getActive", "GET /api/hall-pass/sessions/active",
            "getOverdue", "GET /api/hall-pass/sessions/overdue",
            "dashboard", "GET /api/hall-pass/dashboard"
        ));

        help.put("notes", Map.of(
            "qrCode", "QR code is scanned from student ID card",
            "faceRecognition", "Optional facial recognition for additional verification",
            "maxDuration", "Default max duration is 15 minutes (configurable)",
            "parentNotification", "Parents notified on departure and return if enabled"
        ));

        return ResponseEntity.ok(help);
    }
}
