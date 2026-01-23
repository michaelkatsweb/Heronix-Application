package com.heronix.ui.dashboard.widget;

import com.heronix.ui.dashboard.DashboardWidget;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Table Widget
 * Displays tabular data within a dashboard widget.
 *
 * Features:
 * - Compact table styling
 * - Row click handler
 * - Optional "View All" link
 * - Configurable columns
 * - Max rows limit
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class TableWidget<T> extends DashboardWidget {

    private final TableView<T> tableView;
    private final Label emptyLabel;
    private final Hyperlink viewAllLink;
    private final VBox container;

    private int maxRows = 5;
    private Runnable onViewAll;
    private Consumer<T> onRowClick;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public TableWidget(String title) {
        super(title);

        setSize(WidgetSize.LARGE);

        // Table
        tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableView.getStyleClass().add("compact-table");
        tableView.setStyle("""
            -fx-background-color: transparent;
            -fx-border-width: 0;
            """);
        tableView.setFixedCellSize(36);
        tableView.setPlaceholder(new Label(""));
        VBox.setVgrow(tableView, Priority.ALWAYS);

        // Row click
        tableView.setRowFactory(tv -> {
            TableRow<T> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && onRowClick != null) {
                    onRowClick.accept(row.getItem());
                }
            });
            row.setCursor(javafx.scene.Cursor.HAND);
            return row;
        });

        // Empty label
        emptyLabel = new Label("No data available");
        emptyLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13px;");
        emptyLabel.setAlignment(Pos.CENTER);
        emptyLabel.setVisible(false);
        emptyLabel.setManaged(false);

        // View all link
        viewAllLink = new Hyperlink("View All →");
        viewAllLink.setStyle("-fx-text-fill: #2563EB; -fx-font-size: 12px; -fx-border-width: 0;");
        viewAllLink.setOnAction(e -> {
            if (onViewAll != null) onViewAll.run();
        });
        viewAllLink.setVisible(false);
        viewAllLink.setManaged(false);

        HBox footer = new HBox(viewAllLink);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(8, 0, 0, 0));

        container = new VBox(tableView, emptyLabel, footer);
        VBox.setVgrow(container, Priority.ALWAYS);

        setContent(container);
    }

    // ========================================================================
    // PUBLIC API
    // ========================================================================

    /**
     * Add a column to the table
     */
    public TableColumn<T, String> addColumn(String header, String property) {
        TableColumn<T, String> column = new TableColumn<>(header);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setStyle("-fx-font-size: 12px;");
        tableView.getColumns().add(column);
        return column;
    }

    /**
     * Add a column with custom cell factory
     */
    public TableColumn<T, String> addColumn(String header, java.util.function.Function<T, String> valueExtractor) {
        TableColumn<T, String> column = new TableColumn<>(header);
        column.setCellValueFactory(cellData -> new SimpleStringProperty(valueExtractor.apply(cellData.getValue())));
        column.setStyle("-fx-font-size: 12px;");
        tableView.getColumns().add(column);
        return column;
    }

    /**
     * Set table data
     */
    public void setData(List<T> data) {
        if (data == null || data.isEmpty()) {
            tableView.getItems().clear();
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
            tableView.setVisible(false);
            tableView.setManaged(false);
            viewAllLink.setVisible(false);
            viewAllLink.setManaged(false);
        } else {
            emptyLabel.setVisible(false);
            emptyLabel.setManaged(false);
            tableView.setVisible(true);
            tableView.setManaged(true);

            // Limit rows and show "View All" if needed
            List<T> displayData = data.size() > maxRows ? data.subList(0, maxRows) : data;
            tableView.setItems(FXCollections.observableArrayList(displayData));

            // Update table height based on rows
            tableView.setPrefHeight(36 * Math.min(data.size(), maxRows) + 30);

            // Show "View All" if truncated
            if (data.size() > maxRows && onViewAll != null) {
                viewAllLink.setText(String.format("View All %d →", data.size()));
                viewAllLink.setVisible(true);
                viewAllLink.setManaged(true);
            } else {
                viewAllLink.setVisible(false);
                viewAllLink.setManaged(false);
            }
        }
    }

    /**
     * Set observable list directly
     */
    public void setData(ObservableList<T> data) {
        tableView.setItems(data);
    }

    /**
     * Set max visible rows
     */
    public void setMaxRows(int max) {
        this.maxRows = max;
    }

    /**
     * Set view all callback
     */
    public void setOnViewAll(Runnable callback) {
        this.onViewAll = callback;
    }

    /**
     * Set row click handler
     */
    public void setOnRowClick(Consumer<T> handler) {
        this.onRowClick = handler;
    }

    /**
     * Set empty message
     */
    public void setEmptyMessage(String message) {
        emptyLabel.setText(message);
    }

    /**
     * Get the underlying table view
     */
    public TableView<T> getTableView() {
        return tableView;
    }

    // ========================================================================
    // FACTORY METHODS
    // ========================================================================

    /**
     * Create a recent students widget
     */
    public static TableWidget<Map<String, String>> recentStudents() {
        TableWidget<Map<String, String>> widget = new TableWidget<>("Recent Students");
        widget.setIcon("👤");
        widget.setSubtitle("Newly enrolled");

        widget.addColumn("Name", map -> map.get("name"));
        widget.addColumn("Grade", map -> map.get("grade"));
        widget.addColumn("Date", map -> map.get("date"));

        return widget;
    }

    /**
     * Create an upcoming events widget
     */
    public static TableWidget<Map<String, String>> upcomingEvents() {
        TableWidget<Map<String, String>> widget = new TableWidget<>("Upcoming Events");
        widget.setIcon("📅");
        widget.setSubtitle("Next 7 days");

        widget.addColumn("Event", map -> map.get("title"));
        widget.addColumn("Date", map -> map.get("date"));
        widget.addColumn("Type", map -> map.get("type"));

        return widget;
    }

    /**
     * Create a pending tasks widget
     */
    public static TableWidget<Map<String, String>> pendingTasks() {
        TableWidget<Map<String, String>> widget = new TableWidget<>("Pending Tasks");
        widget.setIcon("📋");

        widget.addColumn("Task", map -> map.get("title"));
        widget.addColumn("Due", map -> map.get("due"));
        widget.addColumn("Priority", map -> map.get("priority"));

        return widget;
    }

    /**
     * Create a recent grades widget for teachers
     */
    public static TableWidget<Map<String, String>> recentGrades() {
        TableWidget<Map<String, String>> widget = new TableWidget<>("Recent Grades");
        widget.setIcon("📝");
        widget.setSubtitle("Last entered");

        widget.addColumn("Student", map -> map.get("student"));
        widget.addColumn("Assignment", map -> map.get("assignment"));
        widget.addColumn("Grade", map -> map.get("grade"));

        return widget;
    }

    /**
     * Create a class roster summary widget
     */
    public static TableWidget<Map<String, String>> classRoster() {
        TableWidget<Map<String, String>> widget = new TableWidget<>("My Classes");
        widget.setIcon("📚");

        widget.addColumn("Class", map -> map.get("name"));
        widget.addColumn("Students", map -> map.get("count"));
        widget.addColumn("Next", map -> map.get("nextClass"));

        return widget;
    }
}
