package com.heronix.ui.dashboard.widget;

import com.heronix.ui.dashboard.DashboardWidget;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Alerts/Notifications Widget
 * Displays important alerts and notifications.
 *
 * Features:
 * - Different alert types (info, warning, error, success)
 * - Dismissible alerts
 * - Click actions
 * - Timestamp display
 * - Badge count
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class AlertsWidget extends DashboardWidget {

    private final VBox alertsContainer;
    private final ScrollPane scrollPane;
    private final Label emptyLabel;
    private final List<Alert> alerts = new ArrayList<>();

    private Consumer<Alert> onAlertClick;
    private Consumer<Alert> onAlertDismiss;
    private int maxAlerts = 10;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public AlertsWidget() {
        this("Alerts");
    }

    public AlertsWidget(String title) {
        super(title, "🔔");

        setSize(WidgetSize.MEDIUM);
        setRefreshable(true);

        alertsContainer = new VBox(8);
        alertsContainer.setPadding(new Insets(4));

        scrollPane = new ScrollPane(alertsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-width: 0;");

        emptyLabel = new Label("No alerts");
        emptyLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13px;");
        emptyLabel.setAlignment(Pos.CENTER);
        emptyLabel.setPadding(new Insets(20));

        StackPane content = new StackPane(scrollPane, emptyLabel);
        emptyLabel.setVisible(true);
        scrollPane.setVisible(false);

        setContent(content);
    }

    // ========================================================================
    // PUBLIC API
    // ========================================================================

    /**
     * Add an alert
     */
    public void addAlert(Alert alert) {
        alerts.add(0, alert); // Add to front
        if (alerts.size() > maxAlerts) {
            alerts.remove(alerts.size() - 1);
        }
        rebuildAlerts();
        updateSubtitle();
    }

    /**
     * Add a simple alert
     */
    public void addAlert(String id, AlertType type, String title, String message) {
        addAlert(new Alert(id, type, title, message));
    }

    /**
     * Remove an alert
     */
    public void removeAlert(String id) {
        alerts.removeIf(a -> a.getId().equals(id));
        rebuildAlerts();
        updateSubtitle();
    }

    /**
     * Clear all alerts
     */
    public void clearAlerts() {
        alerts.clear();
        rebuildAlerts();
        updateSubtitle();
    }

    /**
     * Set alerts
     */
    public void setAlerts(List<Alert> alertList) {
        alerts.clear();
        alerts.addAll(alertList);
        if (alerts.size() > maxAlerts) {
            alerts.subList(maxAlerts, alerts.size()).clear();
        }
        rebuildAlerts();
        updateSubtitle();
    }

    /**
     * Get alert count
     */
    public int getAlertCount() {
        return alerts.size();
    }

    /**
     * Get unread alert count
     */
    public int getUnreadCount() {
        return (int) alerts.stream().filter(a -> !a.isRead()).count();
    }

    /**
     * Mark all as read
     */
    public void markAllRead() {
        alerts.forEach(a -> a.setRead(true));
        rebuildAlerts();
    }

    /**
     * Set max alerts
     */
    public void setMaxAlerts(int max) {
        this.maxAlerts = max;
    }

    /**
     * Set alert click handler
     */
    public void setOnAlertClick(Consumer<Alert> handler) {
        this.onAlertClick = handler;
    }

    /**
     * Set alert dismiss handler
     */
    public void setOnAlertDismiss(Consumer<Alert> handler) {
        this.onAlertDismiss = handler;
    }

    // ========================================================================
    // INTERNAL
    // ========================================================================

    private void rebuildAlerts() {
        alertsContainer.getChildren().clear();

        if (alerts.isEmpty()) {
            emptyLabel.setVisible(true);
            scrollPane.setVisible(false);
        } else {
            emptyLabel.setVisible(false);
            scrollPane.setVisible(true);

            for (Alert alert : alerts) {
                alertsContainer.getChildren().add(createAlertCard(alert));
            }
        }
    }

    private void updateSubtitle() {
        int unread = getUnreadCount();
        if (unread > 0) {
            setSubtitle(unread + " unread");
        } else {
            setSubtitle("");
        }
    }

    private HBox createAlertCard(Alert alert) {
        // Icon
        Label iconLabel = new Label(alert.getType().getIcon());
        iconLabel.setStyle("-fx-font-size: 16px;");
        iconLabel.setMinWidth(24);

        // Title
        Label titleLabel = new Label(alert.getTitle());
        titleLabel.setStyle(alert.isRead() ?
            "-fx-font-size: 13px; -fx-text-fill: #64748B;" :
            "-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");
        titleLabel.setWrapText(true);

        // Message (truncated)
        Label messageLabel = new Label(alert.getMessage());
        messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxHeight(36);

        // Time
        Label timeLabel = new Label(formatTime(alert.getTimestamp()));
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #94A3B8;");

        VBox textBox = new VBox(2, titleLabel, messageLabel, timeLabel);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        // Dismiss button
        Label dismissBtn = new Label("×");
        dismissBtn.setStyle("-fx-font-size: 16px; -fx-text-fill: #94A3B8; -fx-cursor: hand;");
        dismissBtn.setOnMouseEntered(e -> dismissBtn.setStyle("-fx-font-size: 16px; -fx-text-fill: #EF4444; -fx-cursor: hand;"));
        dismissBtn.setOnMouseExited(e -> dismissBtn.setStyle("-fx-font-size: 16px; -fx-text-fill: #94A3B8; -fx-cursor: hand;"));
        dismissBtn.setOnMouseClicked(e -> {
            e.consume();
            alerts.remove(alert);
            rebuildAlerts();
            updateSubtitle();
            if (onAlertDismiss != null) {
                onAlertDismiss.accept(alert);
            }
        });
        dismissBtn.setVisible(alert.isDismissible());
        dismissBtn.setManaged(alert.isDismissible());

        HBox card = new HBox(10, iconLabel, textBox, dismissBtn);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(10, 12, 10, 12));
        card.setCursor(alert.getActionId() != null ? Cursor.HAND : Cursor.DEFAULT);

        // Styling based on type
        String bgColor = switch (alert.getType()) {
            case ERROR -> "#FEF2F2";
            case WARNING -> "#FFFBEB";
            case SUCCESS -> "#F0FDF4";
            case INFO -> "#EFF6FF";
        };
        String borderColor = switch (alert.getType()) {
            case ERROR -> "#FECACA";
            case WARNING -> "#FDE68A";
            case SUCCESS -> "#BBF7D0";
            case INFO -> "#BFDBFE";
        };

        card.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 6;
            -fx-border-color: %s;
            -fx-border-radius: 6;
            """, bgColor, borderColor));

        // Click handler
        if (alert.getActionId() != null) {
            card.setOnMouseClicked(e -> {
                alert.setRead(true);
                rebuildAlerts();
                updateSubtitle();
                if (onAlertClick != null) {
                    onAlertClick.accept(alert);
                }
            });
        }

        return card;
    }

    private String formatTime(LocalDateTime time) {
        if (time == null) return "";

        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(time, now).toMinutes();

        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + "m ago";
        if (minutes < 1440) return (minutes / 60) + "h ago";
        if (minutes < 10080) return (minutes / 1440) + "d ago";

        return time.format(DateTimeFormatter.ofPattern("MMM d"));
    }

    // ========================================================================
    // ALERT CLASS
    // ========================================================================

    @Getter
    public static class Alert {
        private final String id;
        private final AlertType type;
        private final String title;
        private final String message;
        private final LocalDateTime timestamp;
        private String actionId;
        private boolean dismissible = true;
        private boolean read = false;

        public Alert(String id, AlertType type, String title, String message) {
            this.id = id;
            this.type = type;
            this.title = title;
            this.message = message;
            this.timestamp = LocalDateTime.now();
        }

        public Alert withAction(String actionId) {
            this.actionId = actionId;
            return this;
        }

        public Alert nonDismissible() {
            this.dismissible = false;
            return this;
        }

        public void setRead(boolean read) {
            this.read = read;
        }
    }

    // ========================================================================
    // ALERT TYPE ENUM
    // ========================================================================

    @Getter
    public enum AlertType {
        INFO("ℹ"),
        SUCCESS("✓"),
        WARNING("⚠"),
        ERROR("✗");

        private final String icon;

        AlertType(String icon) {
            this.icon = icon;
        }
    }
}
