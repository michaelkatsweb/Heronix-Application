package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Notification DTO
 *
 * Represents notification and alert configuration for reports.
 *
 * Features:
 * - Multi-channel notifications (Email, SMS, Push, Slack, etc.)
 * - Trigger-based alerts
 * - Scheduled notifications
 * - Template support
 * - Priority levels
 * - Delivery tracking
 *
 * Notification Channels:
 * - Email
 * - SMS
 * - Push notifications
 * - Slack
 * - Microsoft Teams
 * - Webhooks
 * - In-app notifications
 *
 * Trigger Types:
 * - Report generation
 * - Schedule-based
 * - Threshold alerts
 * - Data anomalies
 * - Compliance violations
 * - System events
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 77 - Report Notifications & Alerts
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportNotification {

    /**
     * Notification channel enumeration
     */
    public enum NotificationChannel {
        EMAIL,              // Email notification
        SMS,                // SMS text message
        PUSH,               // Push notification
        SLACK,              // Slack message
        TEAMS,              // Microsoft Teams
        WEBHOOK,            // Webhook callback
        IN_APP,             // In-app notification
        MOBILE_APP,         // Mobile app notification
        DESKTOP,            // Desktop notification
        VOICE_CALL,         // Voice call
        CUSTOM              // Custom channel
    }

    /**
     * Notification type enumeration
     */
    public enum NotificationType {
        REPORT_GENERATED,       // Report generation complete
        REPORT_FAILED,          // Report generation failed
        SCHEDULE_TRIGGERED,     // Scheduled notification
        THRESHOLD_ALERT,        // Threshold exceeded
        ANOMALY_DETECTED,       // Data anomaly detected
        COMPLIANCE_VIOLATION,   // Compliance violation
        SYSTEM_EVENT,           // System event
        USER_ACTION,            // User action required
        REMINDER,               // Reminder notification
        UPDATE,                 // Update notification
        WARNING,                // Warning message
        ERROR,                  // Error message
        INFO,                   // Informational
        CUSTOM                  // Custom notification
    }

    /**
     * Notification priority enumeration
     */
    public enum NotificationPriority {
        CRITICAL,           // Critical - immediate attention
        HIGH,               // High priority
        MEDIUM,             // Medium priority
        LOW,                // Low priority
        INFO                // Informational only
    }

    /**
     * Notification status enumeration
     */
    public enum NotificationStatus {
        PENDING,            // Pending delivery
        QUEUED,             // In delivery queue
        SENDING,            // Currently sending
        SENT,               // Successfully sent
        DELIVERED,          // Delivered to recipient
        READ,               // Read by recipient
        FAILED,             // Delivery failed
        CANCELLED,          // Cancelled
        EXPIRED             // Expired
    }

    /**
     * Delivery status enumeration
     */
    public enum DeliveryStatus {
        SUCCESS,            // Successfully delivered
        FAILED,             // Delivery failed
        PENDING,            // Pending delivery
        RETRYING,           // Retrying delivery
        BOUNCED,            // Email bounced
        BLOCKED,            // Blocked by recipient
        UNSUBSCRIBED        // Recipient unsubscribed
    }

    // ============================================================
    // Basic Information
    // ============================================================

    /**
     * Notification ID
     */
    private Long notificationId;

    /**
     * Notification name
     */
    private String name;

    /**
     * Description
     */
    private String description;

    /**
     * Notification type
     */
    private NotificationType notificationType;

    /**
     * Notification channel
     */
    private NotificationChannel channel;

    /**
     * Priority
     */
    private NotificationPriority priority;

    /**
     * Status
     */
    private NotificationStatus status;

    /**
     * Created at
     */
    private LocalDateTime createdAt;

    /**
     * Created by
     */
    private String createdBy;

    /**
     * Updated at
     */
    private LocalDateTime updatedAt;

    /**
     * Report ID (if specific to a report)
     */
    private Long reportId;

    /**
     * Report name
     */
    private String reportName;

    // ============================================================
    // Recipients
    // ============================================================

    /**
     * Primary recipients
     */
    private List<String> recipients;

    /**
     * CC recipients
     */
    private List<String> ccRecipients;

    /**
     * BCC recipients
     */
    private List<String> bccRecipients;

    /**
     * Recipient groups
     */
    private List<String> recipientGroups;

    /**
     * Recipient roles
     */
    private List<String> recipientRoles;

    /**
     * Total recipients count
     */
    private Integer totalRecipients;

    // ============================================================
    // Content
    // ============================================================

    /**
     * Subject/Title
     */
    private String subject;

    /**
     * Message body
     */
    private String message;

    /**
     * HTML body (for email)
     */
    private String htmlBody;

    /**
     * Template ID
     */
    private String templateId;

    /**
     * Template variables
     */
    private Map<String, Object> templateVariables;

    /**
     * Attachments
     */
    private List<String> attachments;

    /**
     * Action buttons
     */
    private List<NotificationAction> actions;

    /**
     * Deep link URL
     */
    private String deepLinkUrl;

    // ============================================================
    // Trigger Configuration
    // ============================================================

    /**
     * Trigger enabled
     */
    private Boolean triggerEnabled;

    /**
     * Trigger condition
     */
    private String triggerCondition;

    /**
     * Trigger events
     */
    private List<String> triggerEvents;

    /**
     * Threshold value
     */
    private Double thresholdValue;

    /**
     * Threshold operator (>, <, =, >=, <=, !=)
     */
    private String thresholdOperator;

    /**
     * Threshold field
     */
    private String thresholdField;

    /**
     * Cooldown period (minutes)
     */
    private Integer cooldownMinutes;

    /**
     * Last triggered at
     */
    private LocalDateTime lastTriggeredAt;

    /**
     * Trigger count
     */
    private Integer triggerCount;

    // ============================================================
    // Scheduling
    // ============================================================

    /**
     * Schedule enabled
     */
    private Boolean scheduleEnabled;

    /**
     * Cron expression
     */
    private String cronExpression;

    /**
     * Schedule timezone
     */
    private String scheduleTimezone;

    /**
     * Next scheduled time
     */
    private LocalDateTime nextScheduledTime;

    /**
     * Last scheduled time
     */
    private LocalDateTime lastScheduledTime;

    /**
     * Send immediately
     */
    private Boolean sendImmediately;

    /**
     * Scheduled send time
     */
    private LocalDateTime scheduledSendTime;

    // ============================================================
    // Delivery Tracking
    // ============================================================

    /**
     * Sent at
     */
    private LocalDateTime sentAt;

    /**
     * Delivered at
     */
    private LocalDateTime deliveredAt;

    /**
     * Read at
     */
    private LocalDateTime readAt;

    /**
     * Delivery status
     */
    private DeliveryStatus deliveryStatus;

    /**
     * Delivery attempts
     */
    private Integer deliveryAttempts;

    /**
     * Max delivery attempts
     */
    private Integer maxDeliveryAttempts;

    /**
     * Retry delay (minutes)
     */
    private Integer retryDelayMinutes;

    /**
     * Next retry at
     */
    private LocalDateTime nextRetryAt;

    /**
     * Delivery error
     */
    private String deliveryError;

    /**
     * Delivery time (ms)
     */
    private Long deliveryTimeMs;

    // ============================================================
    // Email-Specific Settings
    // ============================================================

    /**
     * From email address
     */
    private String fromEmail;

    /**
     * From name
     */
    private String fromName;

    /**
     * Reply-to email
     */
    private String replyToEmail;

    /**
     * Email headers
     */
    private Map<String, String> emailHeaders;

    /**
     * Track opens
     */
    private Boolean trackOpens;

    /**
     * Track clicks
     */
    private Boolean trackClicks;

    /**
     * Opens count
     */
    private Integer opensCount;

    /**
     * Clicks count
     */
    private Integer clicksCount;

    /**
     * Unique opens
     */
    private Integer uniqueOpens;

    /**
     * Unique clicks
     */
    private Integer uniqueClicks;

    // ============================================================
    // SMS-Specific Settings
    // ============================================================

    /**
     * SMS sender ID
     */
    private String smsSenderId;

    /**
     * SMS provider
     */
    private String smsProvider;

    /**
     * SMS message length
     */
    private Integer smsMessageLength;

    /**
     * SMS segments
     */
    private Integer smsSegments;

    // ============================================================
    // Push Notification Settings
    // ============================================================

    /**
     * Push notification title
     */
    private String pushTitle;

    /**
     * Push notification body
     */
    private String pushBody;

    /**
     * Push notification icon
     */
    private String pushIcon;

    /**
     * Push notification sound
     */
    private String pushSound;

    /**
     * Push notification badge
     */
    private Integer pushBadge;

    /**
     * Push notification data
     */
    private Map<String, Object> pushData;

    /**
     * Device tokens
     */
    private List<String> deviceTokens;

    // ============================================================
    // Slack/Teams Settings
    // ============================================================

    /**
     * Slack channel
     */
    private String slackChannel;

    /**
     * Slack webhook URL
     */
    private String slackWebhookUrl;

    /**
     * Teams channel
     */
    private String teamsChannel;

    /**
     * Teams webhook URL
     */
    private String teamsWebhookUrl;

    /**
     * Message color
     */
    private String messageColor;

    /**
     * Mention users
     */
    private List<String> mentionUsers;

    // ============================================================
    // Preferences
    // ============================================================

    /**
     * Enabled
     */
    private Boolean enabled;

    /**
     * Allow opt-out
     */
    private Boolean allowOptOut;

    /**
     * Quiet hours enabled
     */
    private Boolean quietHoursEnabled;

    /**
     * Quiet hours start (HH:mm)
     */
    private String quietHoursStart;

    /**
     * Quiet hours end (HH:mm)
     */
    private String quietHoursEnd;

    /**
     * Expiry time
     */
    private LocalDateTime expiryTime;

    /**
     * Auto-delete after read
     */
    private Boolean autoDeleteAfterRead;

    /**
     * Delete after days
     */
    private Integer deleteAfterDays;

    // ============================================================
    // Analytics
    // ============================================================

    /**
     * Total sent
     */
    private Long totalSent;

    /**
     * Total delivered
     */
    private Long totalDelivered;

    /**
     * Total read
     */
    private Long totalRead;

    /**
     * Total failed
     */
    private Long totalFailed;

    /**
     * Delivery rate (percentage)
     */
    private Double deliveryRate;

    /**
     * Read rate (percentage)
     */
    private Double readRate;

    /**
     * Click rate (percentage)
     */
    private Double clickRate;

    /**
     * Bounce rate (percentage)
     */
    private Double bounceRate;

    // ============================================================
    // Metadata
    // ============================================================

    /**
     * Tags
     */
    private List<String> tags;

    /**
     * Custom attributes
     */
    private Map<String, Object> customAttributes;

    /**
     * Notes
     */
    private String notes;

    // ============================================================
    // Nested Classes
    // ============================================================

    /**
     * Notification action
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationAction {
        private String actionId;
        private String label;
        private String actionType;
        private String url;
        private Map<String, Object> actionData;
        private String icon;
        private Boolean primary;
    }

    /**
     * Recipient delivery status
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipientStatus {
        private String recipientId;
        private String recipientEmail;
        private String recipientPhone;
        private DeliveryStatus deliveryStatus;
        private LocalDateTime sentAt;
        private LocalDateTime deliveredAt;
        private LocalDateTime readAt;
        private String errorMessage;
        private Integer opensCount;
        private Integer clicksCount;
    }

    /**
     * Notification event
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationEvent {
        private String eventId;
        private String eventType;
        private LocalDateTime eventTime;
        private String recipientId;
        private String description;
        private Map<String, Object> eventData;
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Check if notification is enabled
     */
    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }

    /**
     * Check if notification is sent
     */
    public boolean isSent() {
        return status == NotificationStatus.SENT ||
               status == NotificationStatus.DELIVERED ||
               status == NotificationStatus.READ;
    }

    /**
     * Check if notification is delivered
     */
    public boolean isDelivered() {
        return status == NotificationStatus.DELIVERED ||
               status == NotificationStatus.READ;
    }

    /**
     * Check if notification is read
     */
    public boolean isRead() {
        return status == NotificationStatus.READ;
    }

    /**
     * Check if notification is failed
     */
    public boolean isFailed() {
        return status == NotificationStatus.FAILED;
    }

    /**
     * Check if notification is pending
     */
    public boolean isPending() {
        return status == NotificationStatus.PENDING ||
               status == NotificationStatus.QUEUED;
    }

    /**
     * Check if should retry
     */
    public boolean shouldRetry() {
        if (!isFailed()) {
            return false;
        }

        if (maxDeliveryAttempts != null && deliveryAttempts != null) {
            return deliveryAttempts < maxDeliveryAttempts;
        }

        return true;
    }

    /**
     * Check if expired
     */
    public boolean isExpired() {
        if (expiryTime == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expiryTime);
    }

    /**
     * Check if in quiet hours
     */
    public boolean isInQuietHours() {
        if (!Boolean.TRUE.equals(quietHoursEnabled)) {
            return false;
        }

        if (quietHoursStart == null || quietHoursEnd == null) {
            return false;
        }

        // Simplified check (actual implementation would parse time strings)
        return false;
    }

    /**
     * Check if cooldown active
     */
    public boolean isCooldownActive() {
        if (cooldownMinutes == null || lastTriggeredAt == null) {
            return false;
        }

        LocalDateTime cooldownEnd = lastTriggeredAt.plusMinutes(cooldownMinutes);
        return LocalDateTime.now().isBefore(cooldownEnd);
    }

    /**
     * Calculate delivery rate
     */
    public void calculateDeliveryRate() {
        if (totalSent != null && totalSent > 0 && totalDelivered != null) {
            deliveryRate = (totalDelivered.doubleValue() / totalSent) * 100.0;
        }
    }

    /**
     * Calculate read rate
     */
    public void calculateReadRate() {
        if (totalDelivered != null && totalDelivered > 0 && totalRead != null) {
            readRate = (totalRead.doubleValue() / totalDelivered) * 100.0;
        }
    }

    /**
     * Calculate click rate
     */
    public void calculateClickRate() {
        if (totalDelivered != null && totalDelivered > 0 && clicksCount != null) {
            clickRate = (clicksCount.doubleValue() / totalDelivered) * 100.0;
        }
    }

    /**
     * Increment delivery attempt
     */
    public void incrementDeliveryAttempt() {
        deliveryAttempts = (deliveryAttempts != null ? deliveryAttempts : 0) + 1;

        if (retryDelayMinutes != null) {
            nextRetryAt = LocalDateTime.now().plusMinutes(retryDelayMinutes);
        }
    }

    /**
     * Mark as sent
     */
    public void markAsSent() {
        status = NotificationStatus.SENT;
        sentAt = LocalDateTime.now();
        deliveryStatus = DeliveryStatus.SUCCESS;
    }

    /**
     * Mark as delivered
     */
    public void markAsDelivered() {
        status = NotificationStatus.DELIVERED;
        deliveredAt = LocalDateTime.now();
        deliveryStatus = DeliveryStatus.SUCCESS;
    }

    /**
     * Mark as read
     */
    public void markAsRead() {
        status = NotificationStatus.READ;
        readAt = LocalDateTime.now();
    }

    /**
     * Mark as failed
     */
    public void markAsFailed(String error) {
        status = NotificationStatus.FAILED;
        deliveryStatus = DeliveryStatus.FAILED;
        deliveryError = error;
        incrementDeliveryAttempt();
    }

    /**
     * Get priority level
     */
    public int getPriorityLevel() {
        if (priority == null) {
            return 2; // MEDIUM
        }

        return switch (priority) {
            case CRITICAL -> 0;
            case HIGH -> 1;
            case MEDIUM -> 2;
            case LOW -> 3;
            case INFO -> 4;
        };
    }
}
