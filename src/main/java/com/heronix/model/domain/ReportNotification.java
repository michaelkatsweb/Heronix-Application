package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Report Notification Entity
 *
 * Stores notifications related to report events for user alerting.
 *
 * Notifications are created when:
 * - Reports are generated
 * - Scheduled reports execute
 * - Attendance thresholds are exceeded
 * - Report generation fails
 * - Batch exports complete
 *
 * Notification Types:
 * - INFO - Informational notifications
 * - SUCCESS - Successful operations
 * - WARNING - Warning conditions
 * - ERROR - Error conditions
 * - ALERT - Critical alerts
 *
 * Delivery Channels:
 * - In-app (dashboard/UI)
 * - Email
 * - WebSocket (real-time)
 * - SMS (future)
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 62 - Real-time Report Notifications & Alerts
 */
@Entity
@Table(name = "report_notification",
       indexes = {
           @Index(name = "idx_notification_recipient", columnList = "recipient"),
           @Index(name = "idx_notification_created", columnList = "createdAt"),
           @Index(name = "idx_notification_read", columnList = "isRead"),
           @Index(name = "idx_notification_type", columnList = "notificationType")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Notification recipient (username or email)
     */
    @Column(nullable = false, length = 100)
    private String recipient;

    /**
     * Notification type
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationType notificationType;

    /**
     * Notification priority
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private NotificationPriority priority = NotificationPriority.NORMAL;

    /**
     * Notification title
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * Notification message
     */
    @Column(columnDefinition = "TEXT")
    private String message;

    /**
     * Related report type
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ReportHistory.ReportType reportType;

    /**
     * Related report ID
     */
    private Long reportId;

    /**
     * Action URL (optional link for notification)
     */
    @Column(length = 500)
    private String actionUrl;

    /**
     * Action label (text for action button)
     */
    @Column(length = 100)
    private String actionLabel;

    /**
     * Whether notification has been read
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    /**
     * Whether notification has been sent
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isSent = false;

    /**
     * Notification creation timestamp
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * When notification was read
     */
    private LocalDateTime readAt;

    /**
     * When notification was sent
     */
    private LocalDateTime sentAt;

    /**
     * Notification expiry (auto-delete after this time)
     */
    private LocalDateTime expiresAt;

    /**
     * Additional metadata (JSON)
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;

    /**
     * Notification Type Enumeration
     */
    public enum NotificationType {
        INFO,       // General information
        SUCCESS,    // Successful operation
        WARNING,    // Warning condition
        ERROR,      // Error condition
        ALERT       // Critical alert
    }

    /**
     * Notification Priority Enumeration
     */
    public enum NotificationPriority {
        LOW,        // Low priority
        NORMAL,     // Normal priority
        HIGH,       // High priority
        URGENT      // Urgent/critical
    }

    /**
     * Lifecycle callback - set creation timestamp
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();

        // Set default expiry (30 days from creation)
        if (expiresAt == null) {
            expiresAt = createdAt.plusDays(30);
        }
    }

    /**
     * Mark notification as read
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    /**
     * Mark notification as sent
     */
    public void markAsSent() {
        this.isSent = true;
        this.sentAt = LocalDateTime.now();
    }

    /**
     * Check if notification is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if notification requires immediate attention
     */
    public boolean isUrgent() {
        return priority == NotificationPriority.URGENT ||
               notificationType == NotificationType.ALERT ||
               notificationType == NotificationType.ERROR;
    }
}
