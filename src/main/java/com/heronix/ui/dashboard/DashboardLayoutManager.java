package com.heronix.ui.dashboard;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;

/**
 * Dashboard Layout Manager
 * Manages widget placement, drag-and-drop, and layout persistence.
 *
 * Features:
 * - Drag-and-drop widget repositioning
 * - Responsive grid layout
 * - Widget size customization
 * - Layout persistence
 * - Add/remove widgets
 * - Edit mode toggle
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class DashboardLayoutManager extends VBox {

    // ========================================================================
    // CONFIGURATION
    // ========================================================================

    private static final String LAYOUT_DIR = ".heronix/dashboard";
    private static final DataFormat WIDGET_FORMAT = new DataFormat("application/x-dashboard-widget");

    private final int columns;
    private final double gap;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ========================================================================
    // STATE
    // ========================================================================

    private final Map<String, DashboardWidget> widgets = new LinkedHashMap<>();
    private final Map<String, Supplier<DashboardWidget>> widgetFactories = new HashMap<>();
    private final FlowPane widgetContainer;
    private final HBox toolbar;

    @Getter @Setter
    private String layoutId = "default";

    @Getter
    private boolean editMode = false;

    private DashboardWidget draggedWidget;
    private int dragStartIndex;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public DashboardLayoutManager() {
        this(3, 16);
    }

    public DashboardLayoutManager(int columns, double gap) {
        this.columns = columns;
        this.gap = gap;

        setSpacing(16);
        setPadding(new Insets(0));

        // Toolbar
        toolbar = createToolbar();

        // Widget container using FlowPane for responsive layout
        widgetContainer = new FlowPane();
        widgetContainer.setHgap(gap);
        widgetContainer.setVgap(gap);
        widgetContainer.setPadding(new Insets(0));
        widgetContainer.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(widgetContainer, Priority.ALWAYS);

        getChildren().addAll(toolbar, widgetContainer);

        // Hide toolbar by default
        toolbar.setVisible(false);
        toolbar.setManaged(false);

        log.debug("DashboardLayoutManager initialized with {} columns", columns);
    }

    // ========================================================================
    // TOOLBAR
    // ========================================================================

    private HBox createToolbar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 16, 12, 16));
        bar.setStyle("""
            -fx-background-color: #EFF6FF;
            -fx-background-radius: 8;
            -fx-border-color: #BFDBFE;
            -fx-border-radius: 8;
            """);

        Label editIcon = new Label("✏️");
        editIcon.setStyle("-fx-font-size: 16px;");

        Label editLabel = new Label("Edit Mode");
        editLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #1E40AF;");

        Label instructions = new Label("Drag widgets to reorder • Click × to remove");
        instructions.setStyle("-fx-font-size: 12px; -fx-text-fill: #3B82F6;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addWidgetBtn = new Button("+ Add Widget");
        addWidgetBtn.getStyleClass().addAll("btn", "btn-sm");
        addWidgetBtn.setStyle("""
            -fx-background-color: white;
            -fx-text-fill: #2563EB;
            -fx-border-color: #2563EB;
            -fx-border-radius: 4;
            -fx-background-radius: 4;
            """);
        addWidgetBtn.setOnAction(e -> showAddWidgetDialog());

        Button resetBtn = new Button("Reset Layout");
        resetBtn.getStyleClass().addAll("btn", "btn-sm", "btn-ghost");
        resetBtn.setOnAction(e -> resetLayout());

        Button doneBtn = new Button("Done");
        doneBtn.getStyleClass().addAll("btn", "btn-sm", "btn-primary");
        doneBtn.setStyle("""
            -fx-background-color: #2563EB;
            -fx-text-fill: white;
            -fx-background-radius: 4;
            """);
        doneBtn.setOnAction(e -> setEditMode(false));

        bar.getChildren().addAll(editIcon, editLabel, instructions, spacer, addWidgetBtn, resetBtn, doneBtn);

        return bar;
    }

    // ========================================================================
    // PUBLIC API
    // ========================================================================

    /**
     * Register a widget factory for dynamic widget creation
     */
    public void registerWidgetFactory(String type, Supplier<DashboardWidget> factory) {
        widgetFactories.put(type, factory);
    }

    /**
     * Add a widget to the dashboard
     */
    public void addWidget(String id, DashboardWidget widget) {
        widget.setWidgetId(id);
        widgets.put(id, widget);
        setupWidgetDragDrop(widget);
        widgetContainer.getChildren().add(widget);

        widget.setOnRemove(w -> removeWidget(w.getWidgetId()));

        log.debug("Added widget: {}", id);
    }

    /**
     * Add a widget at a specific position
     */
    public void addWidget(String id, DashboardWidget widget, int index) {
        widget.setWidgetId(id);
        widgets.put(id, widget);
        setupWidgetDragDrop(widget);

        if (index >= 0 && index < widgetContainer.getChildren().size()) {
            widgetContainer.getChildren().add(index, widget);
        } else {
            widgetContainer.getChildren().add(widget);
        }

        widget.setOnRemove(w -> removeWidget(w.getWidgetId()));

        log.debug("Added widget at index {}: {}", index, id);
    }

    /**
     * Remove a widget
     */
    public void removeWidget(String id) {
        DashboardWidget widget = widgets.remove(id);
        if (widget != null) {
            widgetContainer.getChildren().remove(widget);
            log.debug("Removed widget: {}", id);
        }
    }

    /**
     * Get a widget by ID
     */
    public DashboardWidget getWidget(String id) {
        return widgets.get(id);
    }

    /**
     * Get all widgets
     */
    public Collection<DashboardWidget> getWidgets() {
        return widgets.values();
    }

    /**
     * Clear all widgets
     */
    public void clearWidgets() {
        widgets.clear();
        widgetContainer.getChildren().clear();
    }

    /**
     * Toggle edit mode
     */
    public void setEditMode(boolean edit) {
        this.editMode = edit;
        toolbar.setVisible(edit);
        toolbar.setManaged(edit);

        // Update widget appearance for edit mode
        for (DashboardWidget widget : widgets.values()) {
            updateWidgetEditMode(widget, edit);
        }

        if (!edit) {
            saveLayout();
        }

        log.debug("Edit mode: {}", edit);
    }

    /**
     * Toggle edit mode
     */
    public void toggleEditMode() {
        setEditMode(!editMode);
    }

    /**
     * Move a widget to a new position
     */
    public void moveWidget(String id, int newIndex) {
        DashboardWidget widget = widgets.get(id);
        if (widget != null) {
            widgetContainer.getChildren().remove(widget);
            if (newIndex >= 0 && newIndex < widgetContainer.getChildren().size()) {
                widgetContainer.getChildren().add(newIndex, widget);
            } else {
                widgetContainer.getChildren().add(widget);
            }
        }
    }

    /**
     * Get widget order
     */
    public List<String> getWidgetOrder() {
        List<String> order = new ArrayList<>();
        for (Node node : widgetContainer.getChildren()) {
            if (node instanceof DashboardWidget) {
                order.add(((DashboardWidget) node).getWidgetId());
            }
        }
        return order;
    }

    // ========================================================================
    // DRAG AND DROP
    // ========================================================================

    private void setupWidgetDragDrop(DashboardWidget widget) {
        widget.setOnDragDetected(event -> {
            if (!editMode) return;

            draggedWidget = widget;
            dragStartIndex = widgetContainer.getChildren().indexOf(widget);

            Dragboard db = widget.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.put(WIDGET_FORMAT, widget.getWidgetId());
            db.setContent(content);

            // Visual feedback
            widget.setOpacity(0.5);
            widget.setStyle(widget.getStyle() + "-fx-border-color: #2563EB; -fx-border-width: 2;");

            event.consume();
        });

        widget.setOnDragOver(event -> {
            if (!editMode) return;
            if (event.getGestureSource() != widget && event.getDragboard().hasContent(WIDGET_FORMAT)) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        widget.setOnDragEntered(event -> {
            if (!editMode) return;
            if (event.getGestureSource() != widget && event.getDragboard().hasContent(WIDGET_FORMAT)) {
                widget.setStyle(widget.getStyle() + "-fx-background-color: #EFF6FF;");
            }
            event.consume();
        });

        widget.setOnDragExited(event -> {
            if (!editMode) return;
            resetWidgetStyle(widget);
            event.consume();
        });

        widget.setOnDragDropped(event -> {
            if (!editMode) return;

            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasContent(WIDGET_FORMAT)) {
                String draggedId = (String) db.getContent(WIDGET_FORMAT);
                int targetIndex = widgetContainer.getChildren().indexOf(widget);

                if (draggedWidget != null && targetIndex >= 0) {
                    widgetContainer.getChildren().remove(draggedWidget);
                    widgetContainer.getChildren().add(targetIndex, draggedWidget);
                    success = true;
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });

        widget.setOnDragDone(event -> {
            if (draggedWidget != null) {
                draggedWidget.setOpacity(1.0);
                resetWidgetStyle(draggedWidget);
                draggedWidget = null;
            }
            event.consume();
        });
    }

    private void updateWidgetEditMode(DashboardWidget widget, boolean edit) {
        if (edit) {
            widget.setCursor(Cursor.MOVE);
            // Could add visual indicators for edit mode
        } else {
            widget.setCursor(Cursor.DEFAULT);
            resetWidgetStyle(widget);
        }
    }

    private void resetWidgetStyle(DashboardWidget widget) {
        widget.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 12;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);
            -fx-border-color: #E2E8F0;
            -fx-border-radius: 12;
            """);
    }

    // ========================================================================
    // ADD WIDGET DIALOG
    // ========================================================================

    private void showAddWidgetDialog() {
        if (widgetFactories.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Add Widget");
            alert.setHeaderText(null);
            alert.setContentText("No widget types available.");
            alert.showAndWait();
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Add Widget");
        dialog.setHeaderText("Select a widget to add");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ListView<String> listView = new ListView<>();
        listView.getItems().addAll(widgetFactories.keySet());
        listView.setPrefHeight(200);

        dialogPane.setContent(listView);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return listView.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(type -> {
            Supplier<DashboardWidget> factory = widgetFactories.get(type);
            if (factory != null) {
                String id = type + "_" + System.currentTimeMillis();
                DashboardWidget widget = factory.get();
                addWidget(id, widget);
            }
        });
    }

    // ========================================================================
    // PERSISTENCE
    // ========================================================================

    /**
     * Save current layout
     */
    public void saveLayout() {
        try {
            Path layoutDir = Paths.get(System.getProperty("user.home"), LAYOUT_DIR);
            Files.createDirectories(layoutDir);

            Path layoutFile = layoutDir.resolve(layoutId + ".json");

            List<WidgetConfig> configs = new ArrayList<>();
            int index = 0;
            for (Node node : widgetContainer.getChildren()) {
                if (node instanceof DashboardWidget widget) {
                    WidgetConfig config = new WidgetConfig();
                    config.id = widget.getWidgetId();
                    config.type = widget.getClass().getSimpleName();
                    config.order = index++;
                    config.collapsed = widget.isCollapsed();
                    config.visible = widget.isVisible();
                    configs.add(config);
                }
            }

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(layoutFile.toFile(), configs);
            log.debug("Saved layout: {}", layoutId);

        } catch (Exception e) {
            log.warn("Failed to save layout: {}", e.getMessage());
        }
    }

    /**
     * Load layout
     */
    public void loadLayout() {
        try {
            Path layoutFile = Paths.get(System.getProperty("user.home"), LAYOUT_DIR, layoutId + ".json");
            File file = layoutFile.toFile();

            if (file.exists()) {
                List<WidgetConfig> configs = objectMapper.readValue(file,
                        new TypeReference<List<WidgetConfig>>() {});

                // Sort by order
                configs.sort(Comparator.comparingInt(c -> c.order));

                // Apply layout
                List<Node> newOrder = new ArrayList<>();
                for (WidgetConfig config : configs) {
                    DashboardWidget widget = widgets.get(config.id);
                    if (widget != null) {
                        widget.setCollapsed(config.collapsed);
                        widget.setVisible(config.visible);
                        newOrder.add(widget);
                    }
                }

                // Add any widgets not in saved layout
                for (DashboardWidget widget : widgets.values()) {
                    if (!newOrder.contains(widget)) {
                        newOrder.add(widget);
                    }
                }

                widgetContainer.getChildren().setAll(newOrder);
                log.debug("Loaded layout: {}", layoutId);
            }

        } catch (Exception e) {
            log.warn("Failed to load layout: {}", e.getMessage());
        }
    }

    /**
     * Reset to default layout
     */
    public void resetLayout() {
        try {
            Path layoutFile = Paths.get(System.getProperty("user.home"), LAYOUT_DIR, layoutId + ".json");
            Files.deleteIfExists(layoutFile);

            // Reset widget states
            for (DashboardWidget widget : widgets.values()) {
                widget.setCollapsed(false);
                widget.setVisible(true);
            }

            log.debug("Reset layout: {}", layoutId);

        } catch (Exception e) {
            log.warn("Failed to reset layout: {}", e.getMessage());
        }
    }

    // ========================================================================
    // WIDGET CONFIG
    // ========================================================================

    private static class WidgetConfig {
        public String id;
        public String type;
        public int order;
        public boolean collapsed;
        public boolean visible = true;
    }
}
