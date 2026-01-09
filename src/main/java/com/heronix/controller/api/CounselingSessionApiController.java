package com.heronix.controller.api;

import com.heronix.model.domain.CounselingSession;
import com.heronix.model.domain.Student;
import com.heronix.repository.StudentRepository;
import com.heronix.service.CounselingSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Counseling Session Management
 *
 * Provides endpoints for managing student counseling sessions:
 * - Session creation and documentation
 * - Session retrieval and tracking
 * - Student session history
 * - Date range queries
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 31 - December 29, 2025
 */
@RestController
@RequestMapping("/api/counseling-sessions")
@RequiredArgsConstructor
public class CounselingSessionApiController {

    private final CounselingSessionService counselingSessionService;
    private final StudentRepository studentRepository;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createSession(@RequestBody CounselingSession session) {
        try {
            CounselingSession created = counselingSessionService.saveSession(session);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("session", created);
            response.put("message", "Counseling session created successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to create session: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getSessionById(@PathVariable Long id) {
        try {
            CounselingSession session = counselingSessionService.findById(id);

            if (session == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Session not found: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("session", session);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get session: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSessions() {
        try {
            List<CounselingSession> sessions = counselingSessionService.getAllSessions();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
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

    @GetMapping("/students/{studentId}")
    public ResponseEntity<Map<String, Object>> getSessionsByStudent(@PathVariable Long studentId) {
        try {
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            List<CounselingSession> sessions = counselingSessionService.getSessionsByStudent(student);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("sessions", sessions);
            response.put("totalSessions", sessions.size());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get sessions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/date-range")
    public ResponseEntity<Map<String, Object>> getSessionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            List<CounselingSession> sessions = counselingSessionService.getSessionsByDateRange(startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("sessions", sessions);
            response.put("count", sessions.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get sessions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("apiVersion", "1.0.0");
        metadata.put("phase", "Phase 31");
        metadata.put("category", "Student Counseling");
        metadata.put("description", "Counseling session tracking and management");

        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = new HashMap<>();
        help.put("description", "Counseling Session Management API");

        help.put("endpoints", Map.of(
            "create", "POST /api/counseling-sessions",
            "getById", "GET /api/counseling-sessions/{id}",
            "getAllSessions", "GET /api/counseling-sessions",
            "getByStudent", "GET /api/counseling-sessions/students/{id}",
            "getByDateRange", "GET /api/counseling-sessions/date-range"
        ));

        return ResponseEntity.ok(help);
    }
}
