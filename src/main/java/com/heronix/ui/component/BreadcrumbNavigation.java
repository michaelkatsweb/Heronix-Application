package com.heronix.ui.component;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 * Breadcrumb Navigation Component
 * Displays a clickable breadcrumb trail for navigation.
 *
 * Usage:
 * BreadcrumbNavigation breadcrumb = new BreadcrumbNavigation();
 * breadcrumb.setPath("Home", "Students", "John Smith");
 * breadcrumb.setOnNavigate(path -> System.out.println("Navigate to: " + path));
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class BreadcrumbNavigation extends HBox {

    // ========================================================================
    // PROPERTIES
    // ========================================================================

    private final ObservableList<BreadcrumbItem> items = FXCollections.observableArrayList();
    private Consumer<String> onNavigate;
    private Consumer<Integer> onNavigateByIndex;

    // Styling
    private String separatorText = "›";
    private String itemStyle = "-fx-text-fill: #64748B; -fx-font-size: 13px; -fx-cursor: hand;";
    private String itemHoverStyle = "-fx-text-fill: #2563EB; -fx-underline: true;";
    private String currentItemStyle = "-fx-text-fill: #0F172A; -fx-font-weight: 500; -fx-font-size: 13px;";
    private String separatorStyle = "-fx-text-fill: #94A3B8; -fx-font-size: 12px; -fx-padding: 0 8;";

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public BreadcrumbNavigation() {
        initialize();
    }

    public BreadcrumbNavigation(String... path) {
        initialize();
        setPath(path);
    }

    private void initialize() {
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(0);
        getStyleClass().add("breadcrumb-container");
        setStyle("-fx-padding: 12 24; -fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        // Listen for item changes
        items.addListener((ListChangeListener<BreadcrumbItem>) change -> {
            rebuildBreadcrumbs();
        });
    }

    // ========================================================================
    // PUBLIC API
    // ========================================================================

    /**
     * Set the breadcrumb path
     * @param path Variable number of path segments (e.g., "Home", "Students", "John Smith")
     */
    public void setPath(String... path) {
        items.clear();
        for (int i = 0; i < path.length; i++) {
            items.add(new BreadcrumbItem(path[i], i == path.length - 1));
        }
    }

    /**
     * Add a breadcrumb item to the end
     */
    public void addItem(String text) {
        // Mark previous last item as not current
        if (!items.isEmpty()) {
            items.get(items.size() - 1).setCurrent(false);
        }
        items.add(new BreadcrumbItem(text, true));
    }

    /**
     * Remove the last breadcrumb item
     */
    public void removeLast() {
        if (!items.isEmpty()) {
            items.remove(items.size() - 1);
            if (!items.isEmpty()) {
                items.get(items.size() - 1).setCurrent(true);
            }
        }
    }

    /**
     * Navigate back to a specific index (removes all items after)
     */
    public void navigateToIndex(int index) {
        if (index >= 0 && index < items.size()) {
            // Remove items after index
            while (items.size() > index + 1) {
                items.remove(items.size() - 1);
            }
            // Mark the new last item as current
            if (!items.isEmpty()) {
                items.get(items.size() - 1).setCurrent(true);
            }
        }
    }

    /**
     * Clear all breadcrumb items
     */
    public void clear() {
        items.clear();
    }

    /**
     * Get the current path as a string array
     */
    public String[] getPath() {
        return items.stream()
                .map(BreadcrumbItem::getText)
                .toArray(String[]::new);
    }

    /**
     * Get the current path as a joined string
     */
    public String getPathString(String delimiter) {
        return String.join(delimiter, getPath());
    }

    /**
     * Set navigation callback
     */
    public void setOnNavigate(Consumer<String> callback) {
        this.onNavigate = callback;
    }

    /**
     * Set navigation by index callback
     */
    public void setOnNavigateByIndex(Consumer<Integer> callback) {
        this.onNavigateByIndex = callback;
    }

    // ========================================================================
    // STYLING
    // ========================================================================

    /**
     * Set the separator character
     */
    public void setSeparator(String separator) {
        this.separatorText = separator;
        rebuildBreadcrumbs();
    }

    /**
     * Set custom styles
     */
    public void setItemStyles(String normal, String hover, String current) {
        this.itemStyle = normal;
        this.itemHoverStyle = hover;
        this.currentItemStyle = current;
        rebuildBreadcrumbs();
    }

    // ========================================================================
    // INTERNAL
    // ========================================================================

    private void rebuildBreadcrumbs() {
        getChildren().clear();

        for (int i = 0; i < items.size(); i++) {
            BreadcrumbItem item = items.get(i);
            final int index = i;

            // Create label
            Label label = new Label(item.getText());

            if (item.isCurrent()) {
                // Current item (not clickable)
                label.setStyle(currentItemStyle);
                label.getStyleClass().add("breadcrumb-current");
            } else {
                // Clickable item
                label.setStyle(itemStyle);
                label.getStyleClass().add("breadcrumb-item");
                label.setCursor(Cursor.HAND);

                // Hover effects
                label.setOnMouseEntered(e -> label.setStyle(itemHoverStyle));
                label.setOnMouseExited(e -> label.setStyle(itemStyle));

                // Click handler
                label.setOnMouseClicked(e -> {
                    log.debug("Breadcrumb clicked: {} (index: {})", item.getText(), index);

                    // Navigate to this index
                    navigateToIndex(index);

                    // Fire callbacks
                    if (onNavigate != null) {
                        onNavigate.accept(item.getText());
                    }
                    if (onNavigateByIndex != null) {
                        onNavigateByIndex.accept(index);
                    }
                });
            }

            getChildren().add(label);

            // Add separator (except after last item)
            if (i < items.size() - 1) {
                Label separator = new Label(separatorText);
                separator.setStyle(separatorStyle);
                separator.getStyleClass().add("breadcrumb-separator");
                getChildren().add(separator);
            }
        }
    }

    // ========================================================================
    // BREADCRUMB ITEM CLASS
    // ========================================================================

    @Getter
    public static class BreadcrumbItem {
        private final StringProperty text = new SimpleStringProperty();
        private boolean current;

        public BreadcrumbItem(String text, boolean current) {
            this.text.set(text);
            this.current = current;
        }

        public String getText() {
            return text.get();
        }

        public void setText(String text) {
            this.text.set(text);
        }

        public StringProperty textProperty() {
            return text;
        }

        public void setCurrent(boolean current) {
            this.current = current;
        }
    }
}
