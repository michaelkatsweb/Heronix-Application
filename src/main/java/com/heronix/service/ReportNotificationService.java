package com.heronix.service;

import com.heronix.event.ReportEvent;
import com.heronix.model.domain.ReportHistory;
import com.heronix.model.domain.ReportNotification;
import com.heronix.repository.ReportNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Report Notification Service
 *
 * Manages creation, delivery, and lifecycle of report-related notifications.
 *
 * Features:
 * - Event-driven notification creation
 * - Real-time notification delivery
 * - Notification persistence and retrieval
 * - Automatic cleanup of expired notifications
 * - Priority-based routing
 * - Read/unread tracking
 *
 * Notification Triggers:
 * - Report generation events
 * - Scheduled report execution
 * - Attendance threshold violations
 * - Batch export completion
 * - Error conditions
 *
 * Delivery Methods:
 * - In-app notifications
 * - WebSocket push (real-time)
 * - Email integration
 * - Database storage
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 62 - Real-time Report Notifications & Alerts
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReportNotificationService {

    private final ReportNotificationRepository notificationRepository;

    @Value("${notification.enabled:true}")
    private Boolean notificationsEnabled;

    @Value("${notification.retention-days:30}")
    private Integer retentionDays;

    /**
     * Listen for report events and create notifications
     *
     * @param event Report event
     */
    @EventListener
    @Async("taskExecutor")
    public void handleReportEvent(ReportEvent event) {
        if (!notificationsEnabled) {
            return;
        }

        log.debug("Handling report event: {}", event);

        try {
            ReportNotification notification = createNotificationFromEvent(event);
            notification = notificationRepository.save(notification);

            log.info("Created notification {} for event {}", notification.getId(), event.getEventType());

        } catch (Exception e) {
            log.error("Error creating notification for event: {}", event, e);
        }
    }

    /**
     * Create notification from report event
     */
    private ReportNotification createNotificationFromEvent(ReportEvent event) {
        ReportNotification.NotificationType type;
        ReportNotification.NotificationPriority priority;
        String title;
        String actionUrl = null;
        String actionLabel = null;

        // Determine notification type and priority based on event type
        switch (event.getEventType()) {
            case REPORT_GENERATED -> {
                type = ReportNotification.NotificationType.SUCCESS;
                priority = ReportNotification.NotificationPriority.NORMAL;
                title = "Report Generated Successfully";
                actionUrl = "/reports/history";
                actionLabel = "View Report";
            }
            case REPORT_FAILED -> {
                type = ReportNotification.NotificationType.ERROR;
                priority = ReportNotification.NotificationPriority.HIGH;
                title = "Report Generation Failed";
                actionUrl = "/reports/dashboard";
                actionLabel = "Retry";
            }
            case BATCH_COMPLETED -> {
                type = ReportNotification.NotificationType.SUCCESS;
                priority = ReportNotification.NotificationPriority.NORMAL;
                title = "Batch Export Completed";
                actionUrl = "/reports/history";
                actionLabel = "Download";
            }
            case BATCH_FAILED -> {
                type = ReportNotification.NotificationType.ERROR;
                priority = ReportNotification.NotificationPriority.HIGH;
                title = "Batch Export Failed";
            }
            case THRESHOLD_EXCEEDED -> {
                type = ReportNotification.NotificationType.WARNING;
                priority = ReportNotification.NotificationPriority.HIGH;
                title = "Attendance Threshold Exceeded";
                actionUrl = "/reports/analytics";
                actionLabel = "View Details";
            }
            case CHRONIC_ABSENTEEISM_DETECTED -> {
                type = ReportNotification.NotificationType.ALERT;
                priority = ReportNotification.NotificationPriority.URGENT;
                title = "Chronic Absenteeism Detected";
                actionUrl = "/reports/analytics/top-absentees";
                actionLabel = "View Students";
            }
            case SCHEDULE_EXECUTED -> {
                type = ReportNotification.NotificationType.INFO;
                priority = ReportNotification.NotificationPriority.LOW;
                title = "Scheduled Report Executed";
            }
            case SCHEDULE_FAILED -> {
                type = ReportNotification.NotificationType.ERROR;
                priority = ReportNotification.NotificationPriority.HIGH;
                title = "Scheduled Report Failed";
                actionUrl = "/reports/schedules";
                actionLabel = "View Schedule";
            }
            case REPORT_EMAILED -> {
                type = ReportNotification.NotificationType.INFO;
                priority = ReportNotification.NotificationPriority.LOW;
                title = "Report Emailed Successfully";
            }
            case LARGE_REPORT_GENERATED -> {
                type = ReportNotification.NotificationType.WARNING;
                priority = ReportNotification.NotificationPriority.NORMAL;
                title = "Large Report Generated";
            }
            default -> {
                type = ReportNotification.NotificationType.INFO;
                priority = ReportNotification.NotificationPriority.NORMAL;
                title = "Report Event";
            }
        }

        return ReportNotification.builder()
                .recipient(event.getGeneratedBy())
                .notificationType(type)
                .priority(priority)
                .title(title)
                .message(event.getMessage())
                .reportType(event.getReportType())
                .actionUrl(actionUrl)
                .actionLabel(actionLabel)
                .build();
    }

    /**
     * Get all notifications for a recipient
     *
     * @param recipient Recipient username
     * @return List of notifications
     */
    public List<ReportNotification> getNotificationsForRecipient(String recipient) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient);
    }

    /**
     * Get unread notifications for a recipient
     *
     * @param recipient Recipient username
     * @return List of unread notifications
     */
    public List<ReportNotification> getUnreadNotifications(String recipient) {
        return notificationRepository.findByRecipientAndIsReadFalseOrderByCreatedAtDesc(recipient);
    }

    /**
     * Get recent notifications (last N days)
     *
     * @param recipient Recipient username
     * @param days Number of days to look back
     * @return List of recent notifications
     */
    public List<ReportNotification> getRecentNotifications(String recipient, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return notificationRepository.findRecentNotifications(recipient, since);
    }

    /**
     * Count unread notifications for a recipient
     *
     * @param recipient Recipient username
     * @return Count of unread notifications
     */
    public long countUnreadNotifications(String recipient) {
        return notificationRepository.countByRecipientAndIsReadFalse(recipient);
    }

    /**
     * Mark notification as read
     *
     * @param notificationId Notification ID
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.markAsRead(notificationId, LocalDateTime.now());
        log.debug("Marked notification {} as read", notificationId);
    }

    /**
     * Mark all notifications as read for a recipient
     *
     * @param recipient Recipient username
     */
    @Transactional
    public void markAllAsRead(String recipient) {
        notificationRepository.markAllAsReadForRecipient(recipient, LocalDateTime.now());
        log.info("Marked all notifications as read for {}", recipient);
    }

    /**
     * Delete notification
     *
     * @param notificationId Notification ID
     */
    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
        log.debug("Deleted notification {}", notificationId);
    }

    /**
     * Create custom notification
     *
     * @param recipient Recipient username
     * @param title Notification title
     * @param message Notification message
     * @param type Notification type
     * @param priority Notification priority
     * @return Created notification
     */
    @Transactional
    public ReportNotification createCustomNotification(
            String recipient,
            String title,
            String message,
            ReportNotification.NotificationType type,
            ReportNotification.NotificationPriority priority) {

        ReportNotification notification = ReportNotification.builder()
                .recipient(recipient)
                .title(title)
                .message(message)
                .notificationType(type)
                .priority(priority)
                .build();

        notification = notificationRepository.save(notification);

        log.info("Created custom notification {} for {}", notification.getId(), recipient);

        return notification;
    }

    /**
     * Cleanup expired notifications
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "${notification.cleanup.cron:0 0 2 * * ?}")
    @Transactional
    public void cleanupExpiredNotifications() {
        log.info("Starting cleanup of expired notifications");

        try {
            notificationRepository.deleteExpiredNotifications(LocalDateTime.now());
            log.info("Expired notifications cleanup completed");

        } catch (Exception e) {
            log.error("Error during notification cleanup", e);
        }
    }

    /**
     * Get notification statistics for a recipient
     *
     * @param recipient Recipient username
     * @return Notification statistics
     */
    public NotificationStats getNotificationStats(String recipient) {
        long total = notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient).size();
        long unread = notificationRepository.countByRecipientAndIsReadFalse(recipient);
        long urgent = notificationRepository.countByRecipientAndPriorityAndIsReadFalse(
                recipient, ReportNotification.NotificationPriority.URGENT);

        return new NotificationStats(total, unread, urgent);
    }

    /**
     * Notification Statistics DTO
     */
    public record NotificationStats(long total, long unread, long urgent) {}
}
