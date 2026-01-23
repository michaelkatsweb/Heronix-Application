package com.heronix.controller;

import com.heronix.dto.ReportNotification;
import com.heronix.service.ReportAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Report Alert API Controller
 *
 * REST API endpoints for multi-channel alerts and notifications.
 *
 * Endpoints:
 * - POST /api/alerts - Create alert
 * - GET /api/alerts/{id} - Get alert
 * - GET /api/alerts - Get all alerts
 * - GET /api/alerts/report/{reportId} - Get alerts by report
 * - GET /api/alerts/status/{status} - Get alerts by status
 * - GET /api/alerts/priority/{priority} - Get alerts by priority
 * - POST /api/alerts/{id}/send - Send alert
 * - POST /api/alerts/{id}/cancel - Cancel alert
 * - POST /api/alerts/{id}/retry - Retry alert
 * - DELETE /api/alerts/{id} - Delete alert
 * - GET /api/alerts/{id}/recipients - Get recipient statuses
 * - GET /api/alerts/{id}/events - Get alert events
 * - GET /api/alerts/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 77 - Report Notifications & Alerts
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Slf4j
public class ReportAlertApiController {

    private final ReportAlertService alertService;

    /**
     * Create new alert
     *
     * @param alert Alert configuration
     * @return Created alert
     */
    @PostMapping
    public ResponseEntity<ReportNotification> createAlert(@RequestBody ReportNotification alert) {
        log.info("POST /api/alerts - Creating alert: {}", alert.getName());

        try {
            ReportNotification created = alertService.createAlert(alert);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating alert", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get alert by ID
     *
     * @param id Alert ID
     * @return Alert details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportNotification> getAlert(@PathVariable Long id) {
        log.info("GET /api/alerts/{}", id);

        try {
            return alertService.getAlert(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching alert: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all alerts
     *
     * @return List of all alerts
     */
    @GetMapping
    public ResponseEntity<List<ReportNotification>> getAllAlerts() {
        log.info("GET /api/alerts");

        try {
            List<ReportNotification> alerts = alertService.getAllAlerts();
            return ResponseEntity.ok(alerts);

        } catch (Exception e) {
            log.error("Error fetching alerts", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get alerts by report
     *
     * @param reportId Report ID
     * @return List of alerts for the report
     */
    @GetMapping("/report/{reportId}")
    public ResponseEntity<List<ReportNotification>> getAlertsByReport(@PathVariable Long reportId) {
        log.info("GET /api/alerts/report/{}", reportId);

        try {
            List<ReportNotification> alerts = alertService.getAlertsByReport(reportId);
            return ResponseEntity.ok(alerts);

        } catch (Exception e) {
            log.error("Error fetching alerts for report: {}", reportId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get alerts by status
     *
     * @param status Alert status
     * @return List of alerts with specified status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReportNotification>> getAlertsByStatus(@PathVariable String status) {
        log.info("GET /api/alerts/status/{}", status);

        try {
            ReportNotification.NotificationStatus notificationStatus =
                    ReportNotification.NotificationStatus.valueOf(status.toUpperCase());
            List<ReportNotification> alerts = alertService.getAlertsByStatus(notificationStatus);
            return ResponseEntity.ok(alerts);

        } catch (IllegalArgumentException e) {
            log.error("Invalid alert status: {}", status);
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error fetching alerts by status: {}", status, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get alerts by priority
     *
     * @param priority Alert priority
     * @return List of alerts with specified priority
     */
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<ReportNotification>> getAlertsByPriority(@PathVariable String priority) {
        log.info("GET /api/alerts/priority/{}", priority);

        try {
            ReportNotification.NotificationPriority notificationPriority =
                    ReportNotification.NotificationPriority.valueOf(priority.toUpperCase());
            List<ReportNotification> alerts = alertService.getAlertsByPriority(notificationPriority);
            return ResponseEntity.ok(alerts);

        } catch (IllegalArgumentException e) {
            log.error("Invalid alert priority: {}", priority);
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error fetching alerts by priority: {}", priority, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Send alert
     *
     * @param id Alert ID
     * @return Send result
     */
    @PostMapping("/{id}/send")
    public ResponseEntity<Map<String, Object>> sendAlert(@PathVariable Long id) {
        log.info("POST /api/alerts/{}/send", id);

        try {
            Map<String, Object> result = alertService.sendAlert(id);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.error("Alert not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            log.error("Alert not ready: {}", id);
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error sending alert: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Cancel alert
     *
     * @param id Alert ID
     * @return Success response
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelAlert(@PathVariable Long id) {
        log.info("POST /api/alerts/{}/cancel", id);

        try {
            alertService.cancelAlert(id);

            Map<String, Object> response = Map.of(
                "message", "Alert cancelled successfully",
                "alertId", id
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Alert not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error cancelling alert: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Retry failed alert
     *
     * @param id Alert ID
     * @return Retry result
     */
    @PostMapping("/{id}/retry")
    public ResponseEntity<Map<String, Object>> retryAlert(@PathVariable Long id) {
        log.info("POST /api/alerts/{}/retry", id);

        try {
            Map<String, Object> result = alertService.retryAlert(id);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.error("Alert not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            log.error("Cannot retry alert: {}", id);
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error retrying alert: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete alert
     *
     * @param id Alert ID
     * @return Success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long id) {
        log.info("DELETE /api/alerts/{}", id);

        try {
            alertService.deleteAlert(id);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Error deleting alert: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get recipient statuses
     *
     * @param id Alert ID
     * @return List of recipient statuses
     */
    @GetMapping("/{id}/recipients")
    public ResponseEntity<List<ReportNotification.RecipientStatus>> getRecipientStatuses(@PathVariable Long id) {
        log.info("GET /api/alerts/{}/recipients", id);

        try {
            List<ReportNotification.RecipientStatus> statuses = alertService.getRecipientStatuses(id);
            return ResponseEntity.ok(statuses);

        } catch (Exception e) {
            log.error("Error fetching recipient statuses: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get alert events
     *
     * @param id Alert ID
     * @return List of alert events
     */
    @GetMapping("/{id}/events")
    public ResponseEntity<List<ReportNotification.NotificationEvent>> getAlertEvents(@PathVariable Long id) {
        log.info("GET /api/alerts/{}/events", id);

        try {
            List<ReportNotification.NotificationEvent> events = alertService.getAlertEvents(id);
            return ResponseEntity.ok(events);

        } catch (Exception e) {
            log.error("Error fetching alert events: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get alert statistics
     *
     * @return Alert statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/alerts/stats");

        try {
            Map<String, Object> stats = alertService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching alert statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
