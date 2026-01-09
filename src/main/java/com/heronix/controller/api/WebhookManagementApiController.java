package com.heronix.controller.api;

import com.heronix.api.dto.WebhookRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * REST API Controller for Webhook Management
 *
 * Provides comprehensive webhook management for real-time event notifications:
 * - Webhook registration and configuration
 * - Event subscription management
 * - Webhook delivery tracking and retries
 * - Webhook security (signatures, authentication)
 * - Webhook testing and validation
 * - Event type catalog
 *
 * Supported Event Types:
 * - student.enrolled, student.withdrawn, student.updated
 * - grade.entered, grade.updated, assignment.published
 * - attendance.recorded, attendance.alert
 * - behavior.incident.created, discipline.action.taken
 * - schedule.changed, course.enrolled, course.dropped
 * - parent.notification.sent, emergency.alert
 *
 * Webhook Features:
 * - HMAC signature verification
 * - Automatic retry with exponential backoff
 * - Event filtering and transformation
 * - Batch event delivery
 * - Webhook health monitoring
 * - Rate limiting and throttling
 *
 * Security:
 * - API key authentication
 * - IP whitelisting
 * - HTTPS enforcement
 * - Payload encryption options
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since December 30, 2025 - Phase 37
 */
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Validated
@Tag(name = "Webhooks", description = "Webhook management and event notifications")
public class WebhookManagementApiController {

    private final com.heronix.service.WebhookService webhookService;

    // ========================================================================
    // WEBHOOK REGISTRATION
    // ========================================================================

    /**
     * Register a new webhook endpoint
     *
     * POST /api/webhooks
     *
     * Request Body:
     * {
     *   "name": "Student Enrollment Webhook",
     *   "url": "https://external-system.com/api/webhooks/student-enrollment",
     *   "events": ["student.enrolled", "student.withdrawn"],
     *   "secret": "webhook_secret_key",
     *   "active": true,
     *   "headers": {
     *     "Authorization": "Bearer token123"
     *   }
     * }
     */
    @Operation(
        summary = "Register a new webhook endpoint",
        description = "Creates a new webhook subscription for receiving real-time event notifications. Supports event filtering, custom headers, and HMAC signature verification."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Webhook registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data - validation failed"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<Map<String, Object>> registerWebhook(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Webhook configuration including URL, event types, and authentication details"
            )
            @Valid @RequestBody WebhookRequestDTO request) {

        try {
            var webhookData = webhookService.registerWebhook(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("webhook", webhookData);
            response.put("message", "Webhook registered successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to register webhook: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get all registered webhooks
     *
     * GET /api/webhooks
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllWebhooks() {
        try {
            var webhooks = webhookService.getAllWebhooks();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("webhooks", webhooks);
            response.put("count", webhooks.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve webhooks: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get webhook by ID
     *
     * GET /api/webhooks/{webhookId}
     */
    @GetMapping("/{webhookId}")
    public ResponseEntity<Map<String, Object>> getWebhook(@PathVariable String webhookId) {
        try {
            var webhook = webhookService.getWebhook(webhookId);

            if (webhook.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Webhook not found: " + webhookId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("webhook", webhook.get());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve webhook: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Update webhook configuration
     *
     * PUT /api/webhooks/{webhookId}
     */
    @PutMapping("/{webhookId}")
    public ResponseEntity<Map<String, Object>> updateWebhook(
            @PathVariable String webhookId,
            @Valid @RequestBody WebhookRequestDTO request) {

        try {
            var webhookData = webhookService.updateWebhook(webhookId, request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("webhook", webhookData);
            response.put("message", "Webhook updated successfully");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to update webhook: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Delete webhook
     *
     * DELETE /api/webhooks/{webhookId}
     */
    @DeleteMapping("/{webhookId}")
    public ResponseEntity<Map<String, Object>> deleteWebhook(@PathVariable String webhookId) {
        try {
            webhookService.deleteWebhook(webhookId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Webhook deleted successfully");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to delete webhook: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // WEBHOOK TESTING
    // ========================================================================

    /**
     * Test webhook endpoint
     *
     * POST /api/webhooks/{webhookId}/test
     *
     * Sends a test event to verify webhook configuration
     */
    @PostMapping("/{webhookId}/test")
    public ResponseEntity<Map<String, Object>> testWebhook(@PathVariable String webhookId) {
        try {
            var testResult = webhookService.testWebhook(webhookId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("testResult", testResult);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to test webhook: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // WEBHOOK DELIVERY HISTORY
    // ========================================================================

    /**
     * Get webhook delivery history
     *
     * GET /api/webhooks/{webhookId}/deliveries?limit=50
     */
    @GetMapping("/{webhookId}/deliveries")
    public ResponseEntity<Map<String, Object>> getDeliveryHistory(
            @PathVariable String webhookId,
            @RequestParam(defaultValue = "50") int limit) {

        try {
            var deliveries = webhookService.getDeliveryHistory(webhookId, limit);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("webhookId", webhookId);
            response.put("deliveries", deliveries);
            response.put("count", deliveries.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve delivery history: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Retry failed webhook delivery
     *
     * POST /api/webhooks/deliveries/{deliveryId}/retry
     */
    @PostMapping("/deliveries/{deliveryId}/retry")
    public ResponseEntity<Map<String, Object>> retryDelivery(@PathVariable String deliveryId) {
        try {
            webhookService.retryDelivery(deliveryId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("deliveryId", deliveryId);
            response.put("message", "Delivery retry initiated");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retry delivery: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // EVENT TYPES CATALOG
    // ========================================================================

    /**
     * Get available event types for webhook subscriptions
     *
     * GET /api/webhooks/event-types
     */
    @GetMapping("/event-types")
    public ResponseEntity<Map<String, Object>> getEventTypes() {
        try {
            List<Map<String, Object>> eventTypes = Arrays.asList(
                createEventType("student.enrolled", "Student Enrollment", "Fired when a student is enrolled in a course"),
                createEventType("student.withdrawn", "Student Withdrawal", "Fired when a student withdraws from school"),
                createEventType("student.updated", "Student Updated", "Fired when student information is updated"),
                createEventType("grade.entered", "Grade Entered", "Fired when a grade is entered for an assignment"),
                createEventType("grade.updated", "Grade Updated", "Fired when a grade is modified"),
                createEventType("attendance.recorded", "Attendance Recorded", "Fired when attendance is taken"),
                createEventType("attendance.alert", "Attendance Alert", "Fired for attendance concerns (truancy, excessive absences)"),
                createEventType("behavior.incident.created", "Behavior Incident", "Fired when a behavior incident is reported"),
                createEventType("schedule.changed", "Schedule Changed", "Fired when a student's schedule changes"),
                createEventType("assignment.published", "Assignment Published", "Fired when an assignment is published")
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("eventTypes", eventTypes);
            response.put("count", eventTypes.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve event types: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // WEBHOOK ACTIVATION/DEACTIVATION
    // ========================================================================

    /**
     * Activate webhook
     *
     * POST /api/webhooks/{webhookId}/activate
     */
    @PostMapping("/{webhookId}/activate")
    public ResponseEntity<Map<String, Object>> activateWebhook(@PathVariable String webhookId) {
        try {
            webhookService.activateWebhook(webhookId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("webhookId", webhookId);
            response.put("active", true);
            response.put("message", "Webhook activated successfully");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to activate webhook: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Deactivate webhook
     *
     * POST /api/webhooks/{webhookId}/deactivate
     */
    @PostMapping("/{webhookId}/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateWebhook(@PathVariable String webhookId) {
        try {
            webhookService.deactivateWebhook(webhookId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("webhookId", webhookId);
            response.put("active", false);
            response.put("message", "Webhook deactivated successfully");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to deactivate webhook: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private Map<String, Object> createEventType(String type, String name, String description) {
        Map<String, Object> eventType = new HashMap<>();
        eventType.put("type", type);
        eventType.put("name", name);
        eventType.put("description", description);
        return eventType;
    }
}
