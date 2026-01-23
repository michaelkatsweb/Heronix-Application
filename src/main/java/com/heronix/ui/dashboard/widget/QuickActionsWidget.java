package com.heronix.ui.dashboard.widget;

import com.heronix.ui.dashboard.DashboardWidget;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Quick Actions Widget
 * Displays a grid of action buttons for common tasks.
 *
 * Features:
 * - Icon + label buttons
 * - Customizable grid layout
 * - Hover effects
 * - Action callbacks
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class QuickActionsWidget extends DashboardWidget {

    private final FlowPane actionsPane;
    private final List<QuickAction> actions = new ArrayList<>();
    private Consumer<QuickAction> onActionClick;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public QuickActionsWidget() {
        this("Quick Actions");
    }

    public QuickActionsWidget(String title) {
        super(title, "⚡");

        setSize(WidgetSize.MEDIUM);
        setRefreshable(false);
        setCollapsible(true);

        actionsPane = new FlowPane();
        actionsPane.setHgap(12);
        actionsPane.setVgap(12);
        actionsPane.setPadding(new Insets(8));
        actionsPane.setAlignment(Pos.TOP_LEFT);

        setContent(actionsPane);
    }

    // ========================================================================
    // PUBLIC API
    // ========================================================================

    /**
     * Add an action to the widget
     */
    public void addAction(String id, String icon, String label, String description) {
        QuickAction action = new QuickAction(id, icon, label, description);
        actions.add(action);
        actionsPane.getChildren().add(createActionTile(action));
    }

    /**
     * Add an action with a direct callback
     */
    public void addAction(String id, String icon, String label, String description, Runnable callback) {
        QuickAction action = new QuickAction(id, icon, label, description, callback);
        actions.add(action);
        actionsPane.getChildren().add(createActionTile(action));
    }

    /**
     * Set the action click handler
     */
    public void setOnActionClick(Consumer<QuickAction> handler) {
        this.onActionClick = handler;
    }

    /**
     * Clear all actions
     */
    public void clearActions() {
        actions.clear();
        actionsPane.getChildren().clear();
    }

    /**
     * Get all actions
     */
    public List<QuickAction> getActions() {
        return new ArrayList<>(actions);
    }

    // ========================================================================
    // FACTORY METHODS - Admin Actions
    // ========================================================================

    /**
     * Create admin quick actions widget
     */
    public static QuickActionsWidget forAdmin() {
        QuickActionsWidget widget = new QuickActionsWidget("Quick Actions");

        widget.addAction("new-student", "👤+", "Add Student", "Enroll a new student");
        widget.addAction("new-staff", "👔+", "Add Staff", "Add a new staff member");
        widget.addAction("new-course", "📚+", "Add Course", "Create a new course");
        widget.addAction("reports", "📋", "Reports", "View system reports");
        widget.addAction("announcements", "📢", "Announce", "Post announcement");
        widget.addAction("calendar", "📅", "Calendar", "School calendar");

        return widget;
    }

    /**
     * Create teacher quick actions widget
     */
    public static QuickActionsWidget forTeacher() {
        QuickActionsWidget widget = new QuickActionsWidget("Quick Actions");

        widget.addAction("take-attendance", "✓", "Attendance", "Take class attendance");
        widget.addAction("enter-grades", "📝", "Grades", "Enter student grades");
        widget.addAction("assignments", "📄", "Assignments", "Manage assignments");
        widget.addAction("messages", "✉", "Messages", "View messages");
        widget.addAction("schedule", "📅", "Schedule", "View my schedule");
        widget.addAction("resources", "📁", "Resources", "Class resources");

        return widget;
    }

    /**
     * Create student quick actions widget
     */
    public static QuickActionsWidget forStudent() {
        QuickActionsWidget widget = new QuickActionsWidget("Quick Actions");

        widget.addAction("my-grades", "📊", "Grades", "View my grades");
        widget.addAction("my-schedule", "📅", "Schedule", "View my schedule");
        widget.addAction("assignments", "📝", "Assignments", "Due assignments");
        widget.addAction("messages", "✉", "Messages", "View messages");

        return widget;
    }

    /**
     * Create parent quick actions widget
     */
    public static QuickActionsWidget forParent() {
        QuickActionsWidget widget = new QuickActionsWidget("Quick Actions");

        widget.addAction("child-grades", "📊", "Grades", "View child's grades");
        widget.addAction("child-attendance", "✓", "Attendance", "View attendance");
        widget.addAction("messages", "✉", "Messages", "Message teacher");
        widget.addAction("calendar", "📅", "Calendar", "School calendar");

        return widget;
    }

    // ========================================================================
    // INTERNAL
    // ========================================================================

    private VBox createActionTile(QuickAction action) {
        Label iconLabel = new Label(action.icon);
        iconLabel.setStyle("-fx-font-size: 24px;");

        Label textLabel = new Label(action.label);
        textLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 500; -fx-text-fill: #334155;");
        textLabel.setWrapText(true);
        textLabel.setAlignment(Pos.CENTER);

        VBox tile = new VBox(8, iconLabel, textLabel);
        tile.setAlignment(Pos.CENTER);
        tile.setPadding(new Insets(16));
        tile.setPrefSize(90, 90);
        tile.setMaxSize(90, 90);
        tile.setCursor(Cursor.HAND);

        // Default style
        String defaultStyle = """
            -fx-background-color: #F8FAFC;
            -fx-background-radius: 8;
            -fx-border-color: #E2E8F0;
            -fx-border-radius: 8;
            """;
        tile.setStyle(defaultStyle);

        // Hover style
        String hoverStyle = """
            -fx-background-color: #EFF6FF;
            -fx-background-radius: 8;
            -fx-border-color: #2563EB;
            -fx-border-radius: 8;
            """;

        tile.setOnMouseEntered(e -> tile.setStyle(hoverStyle));
        tile.setOnMouseExited(e -> tile.setStyle(defaultStyle));

        // Click handler
        tile.setOnMouseClicked(e -> {
            log.debug("Quick action clicked: {}", action.id);
            if (action.callback != null) {
                action.callback.run();
            }
            if (onActionClick != null) {
                onActionClick.accept(action);
            }
        });

        // Tooltip
        if (action.description != null && !action.description.isEmpty()) {
            javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip(action.description);
            javafx.scene.control.Tooltip.install(tile, tooltip);
        }

        return tile;
    }

    // ========================================================================
    // QUICK ACTION CLASS
    // ========================================================================

    public static class QuickAction {
        public final String id;
        public final String icon;
        public final String label;
        public final String description;
        public final Runnable callback;

        public QuickAction(String id, String icon, String label, String description) {
            this(id, icon, label, description, null);
        }

        public QuickAction(String id, String icon, String label, String description, Runnable callback) {
            this.id = id;
            this.icon = icon;
            this.label = label;
            this.description = description;
            this.callback = callback;
        }
    }
}
