package com.heronix.controller.api;

import com.heronix.dto.ScheduleChangeRequestDTO;
import com.heronix.service.ScheduleChangeRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Schedule Change Requests
 *
 * Provides endpoints for managing schedule change request workflow:
 * - Submission and cancellation by students
 * - Approval and denial by counselors/administrators
 * - Request tracking and status updates
 * - Query by student, counselor, status
 *
 * Request Types:
 * - ADD: Add a new course to schedule
 * - DROP: Remove a course from schedule
 * - SWAP: Replace one course with another
 *
 * Workflow:
 * PENDING → APPROVED → COMPLETED
 *         ↓
 *       DENIED
 *         ↓
 *    CANCELLED
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 30 - December 29, 2025
 */
@RestController
@RequestMapping("/api/schedule-change-requests")
@RequiredArgsConstructor
public class ScheduleChangeRequestApiController {

    private final ScheduleChangeRequestService scheduleChangeRequestService;

    // ==================== Request Submission & Management ====================

    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitChangeRequest(
            @RequestParam Long studentId,
            @RequestBody ScheduleChangeRequestDTO dto) {

        try {
            ScheduleChangeRequestDTO created = scheduleChangeRequestService.submitChangeRequest(studentId, dto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("request", created);
            response.put("message", "Schedule change request submitted successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to submit request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/{requestId}/approve")
    public ResponseEntity<Map<String, Object>> approveRequest(
            @PathVariable Long requestId,
            @RequestParam Long reviewerId,
            @RequestBody(required = false) Map<String, String> requestBody) {

        try {
            String notes = requestBody != null ? requestBody.get("notes") : null;
            ScheduleChangeRequestDTO updated = scheduleChangeRequestService.approveRequest(requestId, reviewerId, notes);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("request", updated);
            response.put("message", "Request approved successfully");
            response.put("nextStep", "Execute the schedule change and mark as COMPLETED");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid state: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to approve request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/{requestId}/deny")
    public ResponseEntity<Map<String, Object>> denyRequest(
            @PathVariable Long requestId,
            @RequestParam Long reviewerId,
            @RequestBody Map<String, String> requestBody) {

        try {
            String reason = requestBody.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Denial reason is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            ScheduleChangeRequestDTO updated = scheduleChangeRequestService.denyRequest(requestId, reviewerId, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("request", updated);
            response.put("message", "Request denied");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid state: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to deny request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/{requestId}/complete")
    public ResponseEntity<Map<String, Object>> completeRequest(@PathVariable Long requestId) {
        try {
            ScheduleChangeRequestDTO updated = scheduleChangeRequestService.completeRequest(requestId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("request", updated);
            response.put("message", "Request marked as completed");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid state: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to complete request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/{requestId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelRequest(
            @PathVariable Long requestId,
            @RequestParam Long studentId) {

        try {
            ScheduleChangeRequestDTO updated = scheduleChangeRequestService.cancelRequest(requestId, studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("request", updated);
            response.put("message", "Request cancelled successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Authorization or validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid state: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to cancel request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Query Operations ====================

    @GetMapping("/{requestId}")
    public ResponseEntity<Map<String, Object>> getRequestById(@PathVariable Long requestId) {
        try {
            ScheduleChangeRequestDTO request = scheduleChangeRequestService.getRequestById(requestId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("request", request);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getRequestsForStudent(@PathVariable Long studentId) {
        try {
            List<ScheduleChangeRequestDTO> requests = scheduleChangeRequestService.getRequestsForStudent(studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("requests", requests);
            response.put("totalRequests", requests.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get student requests: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/student/{studentId}/pending")
    public ResponseEntity<Map<String, Object>> getPendingRequestsForStudent(@PathVariable Long studentId) {
        try {
            List<ScheduleChangeRequestDTO> requests = scheduleChangeRequestService.getPendingRequestsForStudent(studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("pendingRequests", requests);
            response.put("count", requests.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get pending requests: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getAllPendingRequests() {
        try {
            List<ScheduleChangeRequestDTO> requests = scheduleChangeRequestService.getAllPendingRequests();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("pendingRequests", requests);
            response.put("totalPending", requests.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get pending requests: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/counselor/{counselorId}/pending")
    public ResponseEntity<Map<String, Object>> getPendingRequestsForCounselor(@PathVariable Long counselorId) {
        try {
            List<ScheduleChangeRequestDTO> requests =
                scheduleChangeRequestService.getPendingRequestsForCounselor(counselorId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("counselorId", counselorId);
            response.put("pendingRequests", requests);
            response.put("count", requests.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get counselor requests: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/overdue")
    public ResponseEntity<Map<String, Object>> getOverdueRequests() {
        try {
            List<ScheduleChangeRequestDTO> requests = scheduleChangeRequestService.getOverdueRequests();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("overdueRequests", requests);
            response.put("count", requests.size());
            response.put("note", "Requests older than 5 days without review");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get overdue requests: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Dashboard & Analytics ====================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        try {
            List<ScheduleChangeRequestDTO> allPending = scheduleChangeRequestService.getAllPendingRequests();
            List<ScheduleChangeRequestDTO> overdue = scheduleChangeRequestService.getOverdueRequests();

            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("totalPending", allPending.size());
            dashboard.put("totalOverdue", overdue.size());
            dashboard.put("pendingRequests", allPending);
            dashboard.put("overdueRequests", overdue);

            // Count by request type
            long addRequests = allPending.stream().filter(r -> r.getRequestType().name().equals("ADD")).count();
            long dropRequests = allPending.stream().filter(r -> r.getRequestType().name().equals("DROP")).count();
            long swapRequests = allPending.stream().filter(r -> r.getRequestType().name().equals("SWAP")).count();

            dashboard.put("byType", Map.of(
                "ADD", addRequests,
                "DROP", dropRequests,
                "SWAP", swapRequests
            ));

            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get dashboard: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        try {
            List<ScheduleChangeRequestDTO> allPending = scheduleChangeRequestService.getAllPendingRequests();
            List<ScheduleChangeRequestDTO> overdue = scheduleChangeRequestService.getOverdueRequests();

            Map<String, Object> summary = new HashMap<>();
            summary.put("totalPending", allPending.size());
            summary.put("totalOverdue", overdue.size());
            summary.put("requiresAttention", overdue.size() > 0);
            summary.put("status", overdue.isEmpty() ? "All requests current" : overdue.size() + " overdue requests need attention");

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get summary: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Reference Data ====================

    @GetMapping("/reference/request-types")
    public ResponseEntity<Map<String, Object>> getRequestTypes() {
        Map<String, Object> types = new HashMap<>();
        types.put("requestTypes", List.of(
            Map.of(
                "type", "ADD",
                "description", "Add a new course to student's schedule",
                "requiredFields", List.of("requestedCourseId", "reason")
            ),
            Map.of(
                "type", "DROP",
                "description", "Remove a course from student's schedule",
                "requiredFields", List.of("currentCourseId", "reason")
            ),
            Map.of(
                "type", "SWAP",
                "description", "Replace one course with another",
                "requiredFields", List.of("currentCourseId", "requestedCourseId", "reason")
            )
        ));

        return ResponseEntity.ok(types);
    }

    @GetMapping("/reference/workflow")
    public ResponseEntity<Map<String, Object>> getWorkflow() {
        Map<String, Object> workflow = new HashMap<>();
        workflow.put("states", List.of("PENDING", "APPROVED", "DENIED", "COMPLETED", "CANCELLED"));
        workflow.put("transitions", Map.of(
            "PENDING", List.of("APPROVED", "DENIED", "CANCELLED"),
            "APPROVED", List.of("COMPLETED"),
            "DENIED", List.of(),
            "COMPLETED", List.of(),
            "CANCELLED", List.of()
        ));

        workflow.put("workflow", List.of(
            "1. Student submits request (PENDING)",
            "2. Counselor/Admin reviews request",
            "3a. Approved → Execute schedule change → COMPLETED",
            "3b. Denied → Notify student",
            "4. Student can cancel PENDING requests"
        ));

        return ResponseEntity.ok(workflow);
    }

    // ==================== Metadata ====================

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("apiVersion", "1.0.0");
        metadata.put("phase", "Phase 30");
        metadata.put("category", "Schedule Management");
        metadata.put("description", "Manage schedule change request workflow");

        metadata.put("capabilities", List.of(
            "Submit schedule change requests (ADD, DROP, SWAP)",
            "Approve or deny requests with notes",
            "Track request status and history",
            "Query by student, counselor, or status",
            "Identify overdue requests needing attention",
            "Dashboard analytics for counselors"
        ));

        metadata.put("endpoints", Map.of(
            "submission", List.of("POST /submit", "POST /{id}/cancel"),
            "review", List.of("POST /{id}/approve", "POST /{id}/deny", "POST /{id}/complete"),
            "queries", List.of("GET /{id}", "GET /student/{id}", "GET /pending", "GET /overdue"),
            "dashboard", List.of("GET /dashboard", "GET /dashboard/summary")
        ));

        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = new HashMap<>();
        help.put("description", "Schedule Change Request Management API");

        help.put("commonWorkflows", Map.of(
            "studentSubmission", List.of(
                "1. POST /api/schedule-change-requests/submit?studentId=123",
                "2. Track with GET /api/schedule-change-requests/student/123",
                "3. Cancel if needed: POST /api/schedule-change-requests/{id}/cancel?studentId=123"
            ),
            "counselorReview", List.of(
                "1. GET /api/schedule-change-requests/counselor/{counselorId}/pending",
                "2. Review request: GET /api/schedule-change-requests/{id}",
                "3. Approve: POST /api/schedule-change-requests/{id}/approve?reviewerId={counselorId}",
                "4. OR Deny: POST /api/schedule-change-requests/{id}/deny?reviewerId={counselorId}"
            ),
            "executeChange", List.of(
                "1. After approval, execute the schedule change in the system",
                "2. Mark as completed: POST /api/schedule-change-requests/{id}/complete"
            ),
            "monitoring", List.of(
                "1. GET /api/schedule-change-requests/dashboard",
                "2. GET /api/schedule-change-requests/overdue",
                "3. Address overdue requests first"
            )
        ));

        help.put("endpoints", Map.of(
            "submit", "POST /api/schedule-change-requests/submit?studentId={id}",
            "approve", "POST /api/schedule-change-requests/{id}/approve?reviewerId={id}",
            "deny", "POST /api/schedule-change-requests/{id}/deny?reviewerId={id}",
            "complete", "POST /api/schedule-change-requests/{id}/complete",
            "cancel", "POST /api/schedule-change-requests/{id}/cancel?studentId={id}",
            "getById", "GET /api/schedule-change-requests/{id}",
            "getByStudent", "GET /api/schedule-change-requests/student/{id}",
            "getPending", "GET /api/schedule-change-requests/pending",
            "getOverdue", "GET /api/schedule-change-requests/overdue",
            "dashboard", "GET /api/schedule-change-requests/dashboard"
        ));

        help.put("examples", Map.of(
            "submitRequest", "curl -X POST 'http://localhost:8080/api/schedule-change-requests/submit?studentId=1' -H 'Content-Type: application/json' -d '{\"requestType\":\"ADD\",\"requestedCourseId\":101,\"reason\":\"Need AP Calculus for college prep\"}'",
            "approveRequest", "curl -X POST 'http://localhost:8080/api/schedule-change-requests/5/approve?reviewerId=10' -H 'Content-Type: application/json' -d '{\"notes\":\"Approved - meets prerequisites\"}'",
            "denyRequest", "curl -X POST 'http://localhost:8080/api/schedule-change-requests/5/deny?reviewerId=10' -H 'Content-Type: application/json' -d '{\"reason\":\"Schedule conflict with required course\"}'",
            "viewDashboard", "curl http://localhost:8080/api/schedule-change-requests/dashboard"
        ));

        help.put("requestTypes", Map.of(
            "ADD", "Add new course (requires: requestedCourseId, reason)",
            "DROP", "Drop existing course (requires: currentCourseId, reason)",
            "SWAP", "Swap courses (requires: currentCourseId, requestedCourseId, reason)"
        ));

        help.put("notes", Map.of(
            "overdueDays", "Requests are considered overdue after 5 days without review",
            "duplicates", "System prevents duplicate pending requests for the same course change",
            "authorization", "Students can only cancel their own requests",
            "workflow", "Only PENDING requests can be approved/denied/cancelled"
        ));

        return ResponseEntity.ok(help);
    }
}
