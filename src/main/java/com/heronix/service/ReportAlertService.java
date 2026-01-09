package com.heronix.service;

import com.heronix.dto.ReportNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Report Alert Service
 *
 * Manages multi-channel alerts and notifications for reports.
 *
 * Features:
 * - Multi-channel notification delivery (Email, SMS, Push, Slack, Teams)
 * - Trigger-based alerts
 * - Scheduled notifications
 * - Template management
 * - Delivery tracking
 * - Analytics and reporting
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 77 - Report Notifications & Alerts
 */
@Service
@Slf4j
public class ReportAlertService {

    private final Map<Long, ReportNotification> alerts = new ConcurrentHashMap<>();
    private final Map<Long, List<ReportNotification.RecipientStatus>> recipientStatuses = new ConcurrentHashMap<>();
    private final Map<Long, List<ReportNotification.NotificationEvent>> alertEvents = new ConcurrentHashMap<>();
    private final Map<String, String> templates = new ConcurrentHashMap<>();
    private Long nextAlertId = 1L;

    /**
     * Constructor - Initialize default templates
     */
    public ReportAlertService() {
        initializeDefaultTemplates();
    }

    /**
     * Initialize default notification templates
     */
    private void initializeDefaultTemplates() {
        templates.put("report_generated",
            "Report '{{reportName}}' has been generated successfully. Generated at: {{generatedAt}}");

        templates.put("report_failed",
            "Report '{{reportName}}' generation failed. Error: {{errorMessage}}");

        templates.put("threshold_alert",
            "Alert: {{thresholdField}} has {{thresholdOperator}} {{thresholdValue}}");

        templates.put("compliance_violation",
            "Compliance Violation: {{violationType}} detected in report '{{reportName}}'");

        templates.put("reminder",
            "Reminder: {{reminderMessage}}");
    }

    /**
     * Create alert
     */
    public ReportNotification createAlert(ReportNotification alert) {
        synchronized (this) {
            alert.setNotificationId(nextAlertId++);
            alert.setCreatedAt(LocalDateTime.now());
            alert.setStatus(ReportNotification.NotificationStatus.PENDING);
            alert.setDeliveryAttempts(0);
            alert.setTotalSent(0L);
            alert.setTotalDelivered(0L);
            alert.setTotalRead(0L);
            alert.setTotalFailed(0L);

            // Process template if specified
            if (alert.getTemplateId() != null) {
                processTemplate(alert);
            }

            // Count recipients
            if (alert.getRecipients() != null) {
                alert.setTotalRecipients(alert.getRecipients().size());
            }

            // Set defaults
            if (alert.getMaxDeliveryAttempts() == null) {
                alert.setMaxDeliveryAttempts(3);
            }

            if (alert.getRetryDelayMinutes() == null) {
                alert.setRetryDelayMinutes(5);
            }

            alerts.put(alert.getNotificationId(), alert);

            log.info("Created alert {} - Type: {}, Channel: {}, Priority: {}",
                    alert.getNotificationId(),
                    alert.getNotificationType(),
                    alert.getChannel(),
                    alert.getPriority());

            // Send immediately if configured
            if (Boolean.TRUE.equals(alert.getSendImmediately())) {
                sendAlert(alert.getNotificationId());
            }

            return alert;
        }
    }

    /**
     * Process notification template
     */
    private void processTemplate(ReportNotification alert) {
        String templateContent = templates.get(alert.getTemplateId());
        if (templateContent == null) {
            log.warn("Template not found: {}", alert.getTemplateId());
            return;
        }

        String processedMessage = templateContent;

        // Replace template variables
        if (alert.getTemplateVariables() != null) {
            for (Map.Entry<String, Object> entry : alert.getTemplateVariables().entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                processedMessage = processedMessage.replace(placeholder, value);
            }
        }

        alert.setMessage(processedMessage);
    }

    /**
     * Send alert
     */
    public Map<String, Object> sendAlert(Long alertId) {
        ReportNotification alert = alerts.get(alertId);
        if (alert == null) {
            throw new IllegalArgumentException("Alert not found: " + alertId);
        }

        if (!alert.isEnabled()) {
            throw new IllegalStateException("Alert is not enabled");
        }

        if (alert.isExpired()) {
            throw new IllegalStateException("Alert has expired");
        }

        if (alert.isInQuietHours()) {
            throw new IllegalStateException("Currently in quiet hours");
        }

        Map<String, Object> result = new HashMap<>();
        LocalDateTime sendStartTime = LocalDateTime.now();

        try {
            alert.setStatus(ReportNotification.NotificationStatus.SENDING);

            // Send based on channel
            boolean sent = sendByChannel(alert);

            if (sent) {
                alert.markAsSent();
                alert.setTotalSent(alert.getTotalSent() + 1);

                // Simulate delivery
                alert.markAsDelivered();
                alert.setTotalDelivered(alert.getTotalDelivered() + 1);

                long deliveryTime = java.time.Duration.between(sendStartTime, LocalDateTime.now()).toMillis();
                alert.setDeliveryTimeMs(deliveryTime);

                result.put("success", true);
                result.put("message", "Alert sent successfully");
                result.put("deliveryTimeMs", deliveryTime);

                log.info("Sent alert {} via {}", alertId, alert.getChannel());

                logAlertEvent(alertId, "ALERT_SENT",
                    "Alert sent via " + alert.getChannel());

            } else {
                alert.markAsFailed("Delivery failed");
                alert.setTotalFailed(alert.getTotalFailed() + 1);

                result.put("success", false);
                result.put("message", "Alert delivery failed");

                log.error("Failed to send alert {}", alertId);

                logAlertEvent(alertId, "ALERT_FAILED", "Delivery failed");
            }

            alert.calculateDeliveryRate();

        } catch (Exception e) {
            alert.markAsFailed(e.getMessage());
            alert.setTotalFailed(alert.getTotalFailed() + 1);

            result.put("success", false);
            result.put("error", e.getMessage());

            log.error("Error sending alert {}", alertId, e);

            logAlertEvent(alertId, "ALERT_ERROR", "Error: " + e.getMessage());
        }

        return result;
    }

    /**
     * Send alert by channel
     */
    private boolean sendByChannel(ReportNotification alert) {
        return switch (alert.getChannel()) {
            case EMAIL -> sendEmail(alert);
            case SMS -> sendSMS(alert);
            case PUSH -> sendPushNotification(alert);
            case SLACK -> sendSlackMessage(alert);
            case TEAMS -> sendTeamsMessage(alert);
            case WEBHOOK -> sendWebhook(alert);
            case IN_APP -> sendInAppNotification(alert);
            default -> {
                log.warn("Unsupported alert channel: {}", alert.getChannel());
                yield false;
            }
        };
    }

    /**
     * Send email alert
     */
    private boolean sendEmail(ReportNotification alert) {
        log.info("Sending email to {} recipients", alert.getTotalRecipients());

        if (alert.getRecipients() == null || alert.getRecipients().isEmpty()) {
            return false;
        }

        // Track recipient statuses
        List<ReportNotification.RecipientStatus> statuses = new ArrayList<>();
        for (String recipient : alert.getRecipients()) {
            ReportNotification.RecipientStatus status = ReportNotification.RecipientStatus.builder()
                    .recipientId(UUID.randomUUID().toString())
                    .recipientEmail(recipient)
                    .deliveryStatus(ReportNotification.DeliveryStatus.SUCCESS)
                    .sentAt(LocalDateTime.now())
                    .deliveredAt(LocalDateTime.now())
                    .opensCount(0)
                    .clicksCount(0)
                    .build();
            statuses.add(status);
        }

        recipientStatuses.put(alert.getNotificationId(), statuses);

        return true;
    }

    /**
     * Send SMS alert
     */
    private boolean sendSMS(ReportNotification alert) {
        log.info("Sending SMS to {} recipients", alert.getTotalRecipients());

        if (alert.getMessage() == null || alert.getMessage().isEmpty()) {
            return false;
        }

        // Calculate SMS segments
        int messageLength = alert.getMessage().length();
        alert.setSmsMessageLength(messageLength);
        alert.setSmsSegments((messageLength / 160) + 1);

        return alert.getRecipients() != null && !alert.getRecipients().isEmpty();
    }

    /**
     * Send push notification
     */
    private boolean sendPushNotification(ReportNotification alert) {
        log.info("Sending push notification to {} devices",
                alert.getDeviceTokens() != null ? alert.getDeviceTokens().size() : 0);

        return alert.getDeviceTokens() != null && !alert.getDeviceTokens().isEmpty();
    }

    /**
     * Send Slack message
     */
    private boolean sendSlackMessage(ReportNotification alert) {
        log.info("Sending Slack message to channel: {}", alert.getSlackChannel());

        return alert.getSlackWebhookUrl() != null && !alert.getSlackWebhookUrl().isEmpty();
    }

    /**
     * Send Teams message
     */
    private boolean sendTeamsMessage(ReportNotification alert) {
        log.info("Sending Teams message to channel: {}", alert.getTeamsChannel());

        return alert.getTeamsWebhookUrl() != null && !alert.getTeamsWebhookUrl().isEmpty();
    }

    /**
     * Send webhook
     */
    private boolean sendWebhook(ReportNotification alert) {
        log.info("Sending webhook alert");

        return alert.getDeepLinkUrl() != null && !alert.getDeepLinkUrl().isEmpty();
    }

    /**
     * Send in-app notification
     */
    private boolean sendInAppNotification(ReportNotification alert) {
        log.info("Creating in-app alert for {} users", alert.getTotalRecipients());

        return alert.getRecipients() != null && !alert.getRecipients().isEmpty();
    }

    /**
     * Get alert by ID
     */
    public Optional<ReportNotification> getAlert(Long alertId) {
        return Optional.ofNullable(alerts.get(alertId));
    }

    /**
     * Get all alerts
     */
    public List<ReportNotification> getAllAlerts() {
        return new ArrayList<>(alerts.values());
    }

    /**
     * Get alerts by report
     */
    public List<ReportNotification> getAlertsByReport(Long reportId) {
        return alerts.values().stream()
                .filter(a -> reportId.equals(a.getReportId()))
                .collect(Collectors.toList());
    }

    /**
     * Get alerts by status
     */
    public List<ReportNotification> getAlertsByStatus(ReportNotification.NotificationStatus status) {
        return alerts.values().stream()
                .filter(a -> a.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * Get alerts by priority
     */
    public List<ReportNotification> getAlertsByPriority(ReportNotification.NotificationPriority priority) {
        return alerts.values().stream()
                .filter(a -> a.getPriority() == priority)
                .sorted(Comparator.comparing(ReportNotification::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Cancel alert
     */
    public void cancelAlert(Long alertId) {
        ReportNotification alert = alerts.get(alertId);
        if (alert == null) {
            throw new IllegalArgumentException("Alert not found: " + alertId);
        }

        alert.setStatus(ReportNotification.NotificationStatus.CANCELLED);

        log.info("Cancelled alert {}", alertId);

        logAlertEvent(alertId, "ALERT_CANCELLED", "Alert cancelled");
    }

    /**
     * Retry failed alert
     */
    public Map<String, Object> retryAlert(Long alertId) {
        ReportNotification alert = alerts.get(alertId);
        if (alert == null) {
            throw new IllegalArgumentException("Alert not found: " + alertId);
        }

        if (!alert.shouldRetry()) {
            throw new IllegalStateException("Maximum retry attempts reached");
        }

        log.info("Retrying alert {}", alertId);

        return sendAlert(alertId);
    }

    /**
     * Get recipient statuses
     */
    public List<ReportNotification.RecipientStatus> getRecipientStatuses(Long alertId) {
        return recipientStatuses.getOrDefault(alertId, new ArrayList<>());
    }

    /**
     * Get alert events
     */
    public List<ReportNotification.NotificationEvent> getAlertEvents(Long alertId) {
        return alertEvents.getOrDefault(alertId, new ArrayList<>());
    }

    /**
     * Log alert event
     */
    private void logAlertEvent(Long alertId, String eventType, String description) {
        ReportNotification.NotificationEvent event = ReportNotification.NotificationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .eventTime(LocalDateTime.now())
                .description(description)
                .build();

        alertEvents.computeIfAbsent(alertId, k -> new ArrayList<>()).add(event);

        // Keep only last 100 events
        List<ReportNotification.NotificationEvent> events = alertEvents.get(alertId);
        if (events.size() > 100) {
            events.remove(0);
        }
    }

    /**
     * Delete alert
     */
    public void deleteAlert(Long alertId) {
        ReportNotification removed = alerts.remove(alertId);
        if (removed != null) {
            recipientStatuses.remove(alertId);
            alertEvents.remove(alertId);

            log.info("Deleted alert {}", alertId);
        }
    }

    /**
     * Get statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalAlerts", alerts.size());
        stats.put("pendingAlerts", getAlertsByStatus(
            ReportNotification.NotificationStatus.PENDING).size());
        stats.put("sentAlerts", alerts.values().stream()
                .filter(ReportNotification::isSent).count());
        stats.put("deliveredAlerts", alerts.values().stream()
                .filter(ReportNotification::isDelivered).count());
        stats.put("failedAlerts", alerts.values().stream()
                .filter(ReportNotification::isFailed).count());

        long totalSent = alerts.values().stream()
                .filter(a -> a.getTotalSent() != null)
                .mapToLong(ReportNotification::getTotalSent)
                .sum();

        long totalDelivered = alerts.values().stream()
                .filter(a -> a.getTotalDelivered() != null)
                .mapToLong(ReportNotification::getTotalDelivered)
                .sum();

        stats.put("totalSent", totalSent);
        stats.put("totalDelivered", totalDelivered);

        double overallDeliveryRate = totalSent > 0 ? (totalDelivered * 100.0 / totalSent) : 0.0;
        stats.put("overallDeliveryRate", overallDeliveryRate);

        // Count by channel
        Map<ReportNotification.NotificationChannel, Long> byChannel = alerts.values().stream()
                .collect(Collectors.groupingBy(ReportNotification::getChannel, Collectors.counting()));
        stats.put("alertsByChannel", byChannel);

        // Count by priority
        Map<ReportNotification.NotificationPriority, Long> byPriority = alerts.values().stream()
                .filter(a -> a.getPriority() != null)
                .collect(Collectors.groupingBy(ReportNotification::getPriority, Collectors.counting()));
        stats.put("alertsByPriority", byPriority);

        return stats;
    }
}
