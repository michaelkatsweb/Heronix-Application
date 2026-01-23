package com.heronix.ui.dashboard;

import javafx.animation.*;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 * Base Dashboard Widget Component
 * A reusable, stylized container for dashboard content with common features.
 *
 * Features:
 * - Consistent card styling with shadow
 * - Optional header with title, icon, and actions
 * - Loading state with spinner
 * - Error state with retry
 * - Refresh capability
 * - Collapsible content
 * - Drag handle for repositioning
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class DashboardWidget extends VBox {

    // ========================================================================
    // PROPERTIES
    // ========================================================================

    private final StringProperty title = new SimpleStringProperty("");
    private final StringProperty icon = new SimpleStringProperty("");
    private final StringProperty subtitle = new SimpleStringProperty("");
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final BooleanProperty collapsed = new SimpleBooleanProperty(false);
    private final BooleanProperty refreshable = new SimpleBooleanProperty(true);
    private final BooleanProperty collapsible = new SimpleBooleanProperty(true);
    private final ObjectProperty<WidgetSize> size = new SimpleObjectProperty<>(WidgetSize.MEDIUM);

    @Getter @Setter
    private String widgetId;

    @Getter @Setter
    private int gridColumn = 0;

    @Getter @Setter
    private int gridRow = 0;

    @Getter @Setter
    private int columnSpan = 1;

    @Getter @Setter
    private int rowSpan = 1;

    // ========================================================================
    // COMPONENTS
    // ========================================================================

    private final HBox header;
    private final Label iconLabel;
    private final VBox titleBox;
    private final Label titleLabel;
    private final Label subtitleLabel;
    private final HBox actionBox;
    private final Button refreshBtn;
    private final Button collapseBtn;
    private final Button menuBtn;
    private final StackPane contentPane;
    private final VBox loadingOverlay;
    private final VBox errorOverlay;
    private Node content;

    // ========================================================================
    // CALLBACKS
    // ========================================================================

    private Runnable onRefresh;
    private Consumer<DashboardWidget> onRemove;
    private Consumer<DashboardWidget> onSettings;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public DashboardWidget() {
        this("");
    }

    public DashboardWidget(String title) {
        this(title, "");
    }

    public DashboardWidget(String title, String icon) {
        this.title.set(title);
        this.icon.set(icon);

        // Widget styling
        getStyleClass().add("dashboard-widget");
        setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 12;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);
            -fx-border-color: #E2E8F0;
            -fx-border-radius: 12;
            """);

        // Header
        iconLabel = new Label();
        iconLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #2563EB;");
        iconLabel.managedProperty().bind(iconLabel.textProperty().isNotEmpty());
        iconLabel.visibleProperty().bind(iconLabel.textProperty().isNotEmpty());

        titleLabel = new Label();
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");
        titleLabel.textProperty().bind(this.title);

        subtitleLabel = new Label();
        subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");
        subtitleLabel.textProperty().bind(this.subtitle);
        subtitleLabel.managedProperty().bind(this.subtitle.isNotEmpty());
        subtitleLabel.visibleProperty().bind(this.subtitle.isNotEmpty());

        titleBox = new VBox(2, titleLabel, subtitleLabel);

        // Action buttons
        refreshBtn = createIconButton("↻", "Refresh");
        refreshBtn.setOnAction(e -> refresh());
        refreshBtn.managedProperty().bind(refreshable);
        refreshBtn.visibleProperty().bind(refreshable);

        collapseBtn = createIconButton("▼", "Collapse");
        collapseBtn.setOnAction(e -> toggleCollapse());
        collapseBtn.managedProperty().bind(collapsible);
        collapseBtn.visibleProperty().bind(collapsible);

        menuBtn = createIconButton("⋮", "Options");
        menuBtn.setOnAction(e -> showOptionsMenu());

        actionBox = new HBox(4, refreshBtn, collapseBtn, menuBtn);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header = new HBox(10, iconLabel, titleBox, spacer, actionBox);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 16, 12, 16));
        header.setStyle("-fx-border-color: #F1F5F9; -fx-border-width: 0 0 1 0;");

        // Make header draggable
        header.setCursor(Cursor.MOVE);

        // Content pane with loading/error overlays
        contentPane = new StackPane();
        contentPane.setPadding(new Insets(16));
        VBox.setVgrow(contentPane, Priority.ALWAYS);

        // Loading overlay
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setMaxSize(32, 32);
        Label loadingLabel = new Label("Loading...");
        loadingLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px;");
        loadingOverlay = new VBox(12, spinner, loadingLabel);
        loadingOverlay.setAlignment(Pos.CENTER);
        loadingOverlay.setStyle("-fx-background-color: rgba(255,255,255,0.9);");
        loadingOverlay.visibleProperty().bind(loading);
        loadingOverlay.managedProperty().bind(loading);

        // Error overlay
        Label errorIcon = new Label("⚠");
        errorIcon.setStyle("-fx-font-size: 32px; -fx-text-fill: #EF4444;");
        Label errorLabel = new Label("Failed to load");
        errorLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13px;");
        Button retryBtn = new Button("Retry");
        retryBtn.getStyleClass().addAll("btn", "btn-sm", "btn-ghost");
        errorOverlay = new VBox(8, errorIcon, errorLabel, retryBtn);
        errorOverlay.setAlignment(Pos.CENTER);
        errorOverlay.setStyle("-fx-background-color: rgba(255,255,255,0.95);");
        errorOverlay.setVisible(false);
        errorOverlay.setManaged(false);
        retryBtn.setOnAction(e -> {
            errorOverlay.setVisible(false);
            refresh();
        });

        contentPane.getChildren().addAll(loadingOverlay, errorOverlay);

        getChildren().addAll(header, contentPane);

        // Bind icon
        iconLabel.textProperty().bind(this.icon);

        // Listen for collapse changes
        collapsed.addListener((obs, wasCollapsed, isCollapsed) -> {
            animateCollapse(isCollapsed);
        });

        // Size bindings
        size.addListener((obs, oldSize, newSize) -> applySizeConstraints());
        applySizeConstraints();

        log.debug("DashboardWidget created: {}", title);
    }

    // ========================================================================
    // PUBLIC API
    // ========================================================================

    /**
     * Set the widget content
     */
    public void setContent(Node content) {
        this.content = content;
        contentPane.getChildren().removeIf(n -> n != loadingOverlay && n != errorOverlay);
        contentPane.getChildren().add(0, content);
    }

    /**
     * Get the widget content
     */
    public Node getContent() {
        return content;
    }

    /**
     * Refresh the widget data
     */
    public void refresh() {
        if (onRefresh != null) {
            setLoading(true);
            onRefresh.run();
        }
    }

    /**
     * Show error state
     */
    public void showError(String message) {
        setLoading(false);
        if (message != null && !message.isEmpty()) {
            ((Label) errorOverlay.getChildren().get(1)).setText(message);
        }
        errorOverlay.setVisible(true);
        errorOverlay.setManaged(true);
    }

    /**
     * Hide error state
     */
    public void hideError() {
        errorOverlay.setVisible(false);
        errorOverlay.setManaged(false);
    }

    /**
     * Toggle collapse state
     */
    public void toggleCollapse() {
        collapsed.set(!collapsed.get());
    }

    /**
     * Add a header action button
     */
    public void addAction(String icon, String tooltip, Runnable action) {
        Button btn = createIconButton(icon, tooltip);
        btn.setOnAction(e -> action.run());
        actionBox.getChildren().add(actionBox.getChildren().size() - 1, btn);
    }

    // ========================================================================
    // PROPERTY ACCESSORS
    // ========================================================================

    public String getTitle() { return title.get(); }
    public void setTitle(String value) { title.set(value); }
    public StringProperty titleProperty() { return title; }

    public String getIcon() { return icon.get(); }
    public void setIcon(String value) { icon.set(value); }
    public StringProperty iconProperty() { return icon; }

    public String getSubtitle() { return subtitle.get(); }
    public void setSubtitle(String value) { subtitle.set(value); }
    public StringProperty subtitleProperty() { return subtitle; }

    public boolean isLoading() { return loading.get(); }
    public void setLoading(boolean value) { loading.set(value); hideError(); }
    public BooleanProperty loadingProperty() { return loading; }

    public boolean isCollapsed() { return collapsed.get(); }
    public void setCollapsed(boolean value) { collapsed.set(value); }
    public BooleanProperty collapsedProperty() { return collapsed; }

    public boolean isRefreshable() { return refreshable.get(); }
    public void setRefreshable(boolean value) { refreshable.set(value); }
    public BooleanProperty refreshableProperty() { return refreshable; }

    public boolean isCollapsible() { return collapsible.get(); }
    public void setCollapsible(boolean value) { collapsible.set(value); }
    public BooleanProperty collapsibleProperty() { return collapsible; }

    public WidgetSize getSize() { return size.get(); }
    public void setSize(WidgetSize value) { size.set(value); }
    public ObjectProperty<WidgetSize> sizeProperty() { return size; }

    public void setOnRefresh(Runnable callback) { this.onRefresh = callback; }
    public void setOnRemove(Consumer<DashboardWidget> callback) { this.onRemove = callback; }
    public void setOnSettings(Consumer<DashboardWidget> callback) { this.onSettings = callback; }

    // ========================================================================
    // INTERNAL METHODS
    // ========================================================================

    private Button createIconButton(String icon, String tooltip) {
        Button btn = new Button(icon);
        btn.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #64748B;
            -fx-font-size: 14px;
            -fx-padding: 4 8;
            -fx-cursor: hand;
            """);
        btn.setTooltip(new Tooltip(tooltip));

        btn.setOnMouseEntered(e -> btn.setStyle("""
            -fx-background-color: #F1F5F9;
            -fx-text-fill: #0F172A;
            -fx-font-size: 14px;
            -fx-padding: 4 8;
            -fx-cursor: hand;
            -fx-background-radius: 4;
            """));
        btn.setOnMouseExited(e -> btn.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #64748B;
            -fx-font-size: 14px;
            -fx-padding: 4 8;
            -fx-cursor: hand;
            """));

        return btn;
    }

    private void animateCollapse(boolean collapse) {
        collapseBtn.setText(collapse ? "▶" : "▼");

        if (collapse) {
            // Collapse animation
            Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(contentPane.maxHeightProperty(), contentPane.getHeight())),
                new KeyFrame(Duration.millis(200), new KeyValue(contentPane.maxHeightProperty(), 0, Interpolator.EASE_OUT))
            );
            timeline.setOnFinished(e -> {
                contentPane.setVisible(false);
                contentPane.setManaged(false);
            });
            timeline.play();
        } else {
            // Expand animation
            contentPane.setVisible(true);
            contentPane.setManaged(true);
            contentPane.setMaxHeight(0);

            Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(contentPane.maxHeightProperty(), 0)),
                new KeyFrame(Duration.millis(200), new KeyValue(contentPane.maxHeightProperty(), Double.MAX_VALUE, Interpolator.EASE_OUT))
            );
            timeline.play();
        }
    }

    private void showOptionsMenu() {
        ContextMenu menu = new ContextMenu();

        MenuItem refreshItem = new MenuItem("Refresh");
        refreshItem.setOnAction(e -> refresh());

        MenuItem settingsItem = new MenuItem("Settings");
        settingsItem.setOnAction(e -> {
            if (onSettings != null) onSettings.accept(this);
        });

        MenuItem removeItem = new MenuItem("Remove");
        removeItem.setStyle("-fx-text-fill: #EF4444;");
        removeItem.setOnAction(e -> {
            if (onRemove != null) onRemove.accept(this);
        });

        menu.getItems().addAll(refreshItem, settingsItem, new SeparatorMenuItem(), removeItem);
        menu.show(menuBtn, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private void applySizeConstraints() {
        WidgetSize s = size.get();
        setMinWidth(s.getMinWidth());
        setPrefWidth(s.getPrefWidth());
        setMaxWidth(s.getMaxWidth());
        setMinHeight(s.getMinHeight());
        setPrefHeight(s.getPrefHeight());
    }

    // ========================================================================
    // WIDGET SIZE ENUM
    // ========================================================================

    public enum WidgetSize {
        SMALL(200, 250, 300, 150, 200),
        MEDIUM(280, 350, 450, 200, 280),
        LARGE(400, 500, 700, 280, 400),
        WIDE(500, 700, Double.MAX_VALUE, 200, 280),
        TALL(280, 350, 450, 350, 500),
        FULL(Double.MIN_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, 280, 400);

        @Getter private final double minWidth;
        @Getter private final double prefWidth;
        @Getter private final double maxWidth;
        @Getter private final double minHeight;
        @Getter private final double prefHeight;

        WidgetSize(double minWidth, double prefWidth, double maxWidth, double minHeight, double prefHeight) {
            this.minWidth = minWidth;
            this.prefWidth = prefWidth;
            this.maxWidth = maxWidth;
            this.minHeight = minHeight;
            this.prefHeight = prefHeight;
        }
    }
}
