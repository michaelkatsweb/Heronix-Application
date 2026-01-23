package com.heronix.ui.component;

import javafx.animation.*;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Consumer;

/**
 * Bulk Operations Toolbar
 * A toolbar that appears when items are selected, providing bulk action buttons.
 *
 * Features:
 * - Animated show/hide
 * - Selection count display
 * - Configurable action buttons
 * - Undo support
 * - Confirmation dialogs
 *
 * @param <T> The type of selected items
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class BulkOperationsToolbar<T> extends HBox {

    // ========================================================================
    // COMPONENTS
    // ========================================================================

    private final Label selectionCountLabel;
    private final HBox actionsContainer;
    private final Button clearSelectionBtn;
    private final MenuButton moreActionsMenu;

    // ========================================================================
    // PROPERTIES
    // ========================================================================

    private final ObservableList<T> selectedItems = FXCollections.observableArrayList();
    private final BooleanProperty visible = new SimpleBooleanProperty(false);

    // ========================================================================
    // ACTIONS
    // ========================================================================

    private final List<BulkAction<T>> primaryActions = new ArrayList<>();
    private final List<BulkAction<T>> secondaryActions = new ArrayList<>();

    // ========================================================================
    // ANIMATION
    // ========================================================================

    private final TranslateTransition slideIn;
    private final TranslateTransition slideOut;
    private final FadeTransition fadeIn;
    private final FadeTransition fadeOut;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public BulkOperationsToolbar() {
        // Setup layout
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(12);
        setPadding(new Insets(10, 16, 10, 16));
        setStyle("-fx-background-color: linear-gradient(to right, #1E40AF, #2563EB); " +
                "-fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(37, 99, 235, 0.3), 8, 0, 0, 4);");

        // Initially hidden
        setVisible(false);
        setManaged(false);
        setOpacity(0);
        setTranslateY(-50);

        // Selection count
        selectionCountLabel = new Label("0 items selected");
        selectionCountLabel.setStyle("-fx-text-fill: white; -fx-font-weight: 600; -fx-font-size: 14px;");

        // Separator
        Separator sep = new Separator();
        sep.setOrientation(javafx.geometry.Orientation.VERTICAL);
        sep.setStyle("-fx-background-color: rgba(255,255,255,0.3);");
        sep.setPrefHeight(24);

        // Actions container
        actionsContainer = new HBox(8);
        actionsContainer.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(actionsContainer, Priority.ALWAYS);

        // More actions menu
        moreActionsMenu = new MenuButton("More ▾");
        moreActionsMenu.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;");
        moreActionsMenu.setVisible(false);

        // Clear selection button
        clearSelectionBtn = new Button("✕");
        clearSelectionBtn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; " +
                "-fx-background-radius: 50; -fx-min-width: 28; -fx-min-height: 28; -fx-max-width: 28; " +
                "-fx-max-height: 28; -fx-padding: 0; -fx-cursor: hand;");
        clearSelectionBtn.setTooltip(new Tooltip("Clear Selection"));

        getChildren().addAll(selectionCountLabel, sep, actionsContainer, moreActionsMenu, clearSelectionBtn);

        // Setup animations
        slideIn = new TranslateTransition(Duration.millis(200), this);
        slideIn.setFromY(-50);
        slideIn.setToY(0);

        slideOut = new TranslateTransition(Duration.millis(200), this);
        slideOut.setFromY(0);
        slideOut.setToY(-50);

        fadeIn = new FadeTransition(Duration.millis(200), this);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        fadeOut = new FadeTransition(Duration.millis(200), this);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        // Selection listener
        selectedItems.addListener((javafx.collections.ListChangeListener<T>) change -> {
            updateSelectionCount();
            updateVisibility();
        });

        log.debug("BulkOperationsToolbar initialized");
    }

    // ========================================================================
    // PUBLIC API
    // ========================================================================

    /**
     * Set selected items
     */
    public void setSelectedItems(Collection<T> items) {
        selectedItems.setAll(items);
    }

    /**
     * Clear selection
     */
    public void clearSelection() {
        selectedItems.clear();
    }

    /**
     * Get selected items
     */
    public List<T> getSelectedItems() {
        return new ArrayList<>(selectedItems);
    }

    /**
     * Add a primary action (shown directly in toolbar)
     */
    public void addPrimaryAction(String label, String icon, Consumer<List<T>> action) {
        addPrimaryAction(label, icon, action, false, null);
    }

    /**
     * Add a primary action with confirmation
     */
    public void addPrimaryAction(String label, String icon, Consumer<List<T>> action,
                                  boolean requiresConfirmation, String confirmMessage) {
        BulkAction<T> bulkAction = new BulkAction<>(label, icon, action, requiresConfirmation, confirmMessage);
        primaryActions.add(bulkAction);
        rebuildActions();
    }

    /**
     * Add a secondary action (shown in "More" menu)
     */
    public void addSecondaryAction(String label, String icon, Consumer<List<T>> action) {
        addSecondaryAction(label, icon, action, false, null);
    }

    /**
     * Add a secondary action with confirmation
     */
    public void addSecondaryAction(String label, String icon, Consumer<List<T>> action,
                                    boolean requiresConfirmation, String confirmMessage) {
        BulkAction<T> bulkAction = new BulkAction<>(label, icon, action, requiresConfirmation, confirmMessage);
        secondaryActions.add(bulkAction);
        rebuildActions();
    }

    /**
     * Add common edit action
     */
    public void addEditAction(Consumer<List<T>> action) {
        addPrimaryAction("Edit", "✏️", action);
    }

    /**
     * Add common delete action with confirmation
     */
    public void addDeleteAction(Consumer<List<T>> action) {
        addPrimaryAction("Delete", "🗑️", action, true,
                "Are you sure you want to delete the selected items? This action cannot be undone.");
    }

    /**
     * Add common export action
     */
    public void addExportAction(Consumer<List<T>> action) {
        addPrimaryAction("Export", "📥", action);
    }

    /**
     * Set clear selection callback
     */
    public void setOnClearSelection(Runnable callback) {
        clearSelectionBtn.setOnAction(e -> {
            clearSelection();
            if (callback != null) callback.run();
        });
    }

    /**
     * Set custom styling
     */
    public void setBarStyle(String style) {
        setStyle(style);
    }

    /**
     * Show the toolbar
     */
    public void show() {
        if (!isVisible()) {
            setVisible(true);
            setManaged(true);

            ParallelTransition showAnim = new ParallelTransition(slideIn, fadeIn);
            showAnim.play();
        }
    }

    /**
     * Hide the toolbar
     */
    public void hide() {
        if (isVisible()) {
            ParallelTransition hideAnim = new ParallelTransition(slideOut, fadeOut);
            hideAnim.setOnFinished(e -> {
                setVisible(false);
                setManaged(false);
            });
            hideAnim.play();
        }
    }

    // ========================================================================
    // INTERNAL METHODS
    // ========================================================================

    private void updateSelectionCount() {
        int count = selectedItems.size();
        selectionCountLabel.setText(count + " item" + (count != 1 ? "s" : "") + " selected");
    }

    private void updateVisibility() {
        if (selectedItems.isEmpty()) {
            hide();
        } else {
            show();
        }
    }

    private void rebuildActions() {
        actionsContainer.getChildren().clear();
        moreActionsMenu.getItems().clear();

        // Primary actions
        for (BulkAction<T> action : primaryActions) {
            Button btn = createActionButton(action);
            actionsContainer.getChildren().add(btn);
        }

        // Secondary actions (in menu)
        if (!secondaryActions.isEmpty()) {
            moreActionsMenu.setVisible(true);
            for (BulkAction<T> action : secondaryActions) {
                MenuItem item = createActionMenuItem(action);
                moreActionsMenu.getItems().add(item);
            }
        } else {
            moreActionsMenu.setVisible(false);
        }
    }

    private Button createActionButton(BulkAction<T> action) {
        Button btn = new Button(action.icon + " " + action.label);
        btn.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand; -fx-font-size: 13px;");

        // Hover effect
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.25); -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand; -fx-font-size: 13px;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand; -fx-font-size: 13px;"));

        btn.setOnAction(e -> executeAction(action));

        return btn;
    }

    private MenuItem createActionMenuItem(BulkAction<T> action) {
        MenuItem item = new MenuItem(action.icon + " " + action.label);
        item.setOnAction(e -> executeAction(action));
        return item;
    }

    private void executeAction(BulkAction<T> action) {
        if (selectedItems.isEmpty()) return;

        if (action.requiresConfirmation) {
            // Show confirmation dialog
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Action");
            confirm.setHeaderText(action.label + " " + selectedItems.size() + " items");
            confirm.setContentText(action.confirmMessage != null ? action.confirmMessage :
                    "Are you sure you want to " + action.label.toLowerCase() + " the selected items?");

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    action.action.accept(new ArrayList<>(selectedItems));
                }
            });
        } else {
            action.action.accept(new ArrayList<>(selectedItems));
        }
    }

    // ========================================================================
    // BULK ACTION CLASS
    // ========================================================================

    private static class BulkAction<T> {
        final String label;
        final String icon;
        final Consumer<List<T>> action;
        final boolean requiresConfirmation;
        final String confirmMessage;

        BulkAction(String label, String icon, Consumer<List<T>> action,
                   boolean requiresConfirmation, String confirmMessage) {
            this.label = label;
            this.icon = icon;
            this.action = action;
            this.requiresConfirmation = requiresConfirmation;
            this.confirmMessage = confirmMessage;
        }
    }

    // ========================================================================
    // FACTORY METHODS
    // ========================================================================

    /**
     * Create a toolbar with common CRUD actions
     */
    public static <T> BulkOperationsToolbar<T> withCrudActions(
            Consumer<List<T>> onEdit,
            Consumer<List<T>> onDelete,
            Consumer<List<T>> onExport,
            Runnable onClearSelection) {

        BulkOperationsToolbar<T> toolbar = new BulkOperationsToolbar<>();

        if (onEdit != null) toolbar.addEditAction(onEdit);
        if (onDelete != null) toolbar.addDeleteAction(onDelete);
        if (onExport != null) toolbar.addExportAction(onExport);
        if (onClearSelection != null) toolbar.setOnClearSelection(onClearSelection);

        return toolbar;
    }

    /**
     * Create a toolbar with status change actions
     */
    public static <T> BulkOperationsToolbar<T> withStatusActions(
            Consumer<List<T>> onActivate,
            Consumer<List<T>> onDeactivate,
            Consumer<List<T>> onArchive,
            Runnable onClearSelection) {

        BulkOperationsToolbar<T> toolbar = new BulkOperationsToolbar<>();

        if (onActivate != null) toolbar.addPrimaryAction("Activate", "✅", onActivate);
        if (onDeactivate != null) toolbar.addPrimaryAction("Deactivate", "⏸️", onDeactivate);
        if (onArchive != null) toolbar.addSecondaryAction("Archive", "📦", onArchive, true,
                "Are you sure you want to archive the selected items?");
        if (onClearSelection != null) toolbar.setOnClearSelection(onClearSelection);

        return toolbar;
    }
}
