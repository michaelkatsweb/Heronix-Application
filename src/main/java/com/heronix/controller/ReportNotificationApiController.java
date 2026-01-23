package com.heronix.controller;

import com.heronix.model.domain.ReportNotification;
import com.heronix.service.ReportNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Report Notification API Controller
 *
 * REST API endpoints for report notification management and retrieval.
 *
 * Provides endpoints for:
 * - Fetching user notifications
 * - Marking notifications as read
 * - Getting notification statistics
 * - Managing notification preferences
 *
 * Endpoints:
 * - GET /api/report-notifications - Get all notifications
 * - GET /api/report-notifications/unread - Get unread notifications
 * - GET /api/report-notifications/stats - Get notification statistics
 * - PUT /api/report-notifications/{id}/read - Mark as read
 * - PUT /api/report-notifications/read-all - Mark all as read
 * - DELETE /api/report-notifications/{id} - Delete notification
 *
 * WebSocket Integration:
 * - New notifications are pushed via WebSocket
 * - Real-time updates to notification counts
 * - Instant delivery to connected clients
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 62 - Real-time Report Notifications & Alerts
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/report-notifications")
@RequiredArgsConstructor
@Slf4j
public class ReportNotificationApiController {

    private final ReportNotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Get all notifications for current user
     *
     * @param username Current username (from authentication)
     * @return List of notifications
     */
    @GetMapping
    public ResponseEntity<List<ReportNotification>> getAllNotifications(
            @RequestParam(defaultValue = "System") String username) {

        log.info("GET /api/report-notifications - username: {}", username);

        try {
            List<ReportNotification> notifications =
                    notificationService.getNotificationsForRecipient(username);

            return ResponseEntity.ok(notifications);

        } catch (Exception e) {
            log.error("Error fetching notifications for {}", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get unread notifications for current user
     *
     * @param username Current username
     * @return List of unread notifications
     */
    @GetMapping("/unread")
    public ResponseEntity<List<ReportNotification>> getUnreadNotifications(
            @RequestParam(defaultValue = "System") String username) {

        log.info("GET /api/report-notifications/unread - username: {}", username);

        try {
            List<ReportNotification> notifications =
                    notificationService.getUnreadNotifications(username);

            return ResponseEntity.ok(notifications);

        } catch (Exception e) {
            log.error("Error fetching unread notifications for {}", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get recent notifications (last N days)
     *
     * @param username Current username
     * @param days Number of days to look back (default: 7)
     * @return List of recent notifications
     */
    @GetMapping("/recent")
    public ResponseEntity<List<ReportNotification>> getRecentNotifications(
            @RequestParam(defaultValue = "System") String username,
            @RequestParam(defaultValue = "7") int days) {

        log.info("GET /api/report-notifications/recent - username: {}, days: {}", username, days);

        try {
            List<ReportNotification> notifications =
                    notificationService.getRecentNotifications(username, days);

            return ResponseEntity.ok(notifications);

        } catch (Exception e) {
            log.error("Error fetching recent notifications for {}", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get notification statistics
     *
     * @param username Current username
     * @return Notification statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ReportNotificationService.NotificationStats> getNotificationStats(
            @RequestParam(defaultValue = "System") String username) {

        log.info("GET /api/report-notifications/stats - username: {}", username);

        try {
            ReportNotificationService.NotificationStats stats =
                    notificationService.getNotificationStats(username);

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching notification stats for {}", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Mark notification as read
     *
     * @param id Notification ID
     * @param username Current username
     * @return Success response
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            @RequestParam(defaultValue = "System") String username) {

        log.info("PUT /api/report-notifications/{}/read - username: {}", id, username);

        try {
            notificationService.markAsRead(id);

            // Notify via WebSocket
            messagingTemplate.convertAndSendToUser(
                    username,
                    "/queue/notifications",
                    new NotificationUpdate("read", id)
            );

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Error marking notification {} as read", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Mark all notifications as read
     *
     * @param username Current username
     * @return Success response
     */
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @RequestParam(defaultValue = "System") String username) {

        log.info("PUT /api/report-notifications/read-all - username: {}", username);

        try {
            notificationService.markAllAsRead(username);

            // Notify via WebSocket
            messagingTemplate.convertAndSendToUser(
                    username,
                    "/queue/notifications",
                    new NotificationUpdate("read-all", null)
            );

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Error marking all notifications as read for {}", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete notification
     *
     * @param id Notification ID
     * @param username Current username
     * @return Success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id,
            @RequestParam(defaultValue = "System") String username) {

        log.info("DELETE /api/report-notifications/{} - username: {}", id, username);

        try {
            notificationService.deleteNotification(id);

            // Notify via WebSocket
            messagingTemplate.convertAndSendToUser(
                    username,
                    "/queue/notifications",
                    new NotificationUpdate("deleted", id)
            );

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Error deleting notification {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create test notification (for development/testing)
     *
     * @param username Recipient username
     * @param title Notification title
     * @param message Notification message
     * @return Created notification
     */
    @PostMapping("/test")
    public ResponseEntity<ReportNotification> createTestNotification(
            @RequestParam(defaultValue = "System") String username,
            @RequestParam String title,
            @RequestParam String message) {

        log.info("POST /api/report-notifications/test - username: {}", username);

        try {
            ReportNotification notification = notificationService.createCustomNotification(
                    username,
                    title,
                    message,
                    ReportNotification.NotificationType.INFO,
                    ReportNotification.NotificationPriority.NORMAL
            );

            // Send via WebSocket for real-time delivery
            messagingTemplate.convertAndSendToUser(
                    username,
                    "/queue/notifications",
                    notification
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(notification);

        } catch (Exception e) {
            log.error("Error creating test notification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Notification Update DTO for WebSocket messages
     */
    public record NotificationUpdate(String action, Long notificationId) {}
}
