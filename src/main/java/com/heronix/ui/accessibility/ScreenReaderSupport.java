package com.heronix.ui.accessibility;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;

/**
 * Screen Reader Support
 * Enhanced screen reader integration for assistive technology compatibility.
 *
 * Features:
 * - Live region announcements
 * - ARIA-like role and state management
 * - Table and grid accessibility
 * - Form validation announcements
 * - Progress and loading states
 *
 * WCAG Guidelines Addressed:
 * - 4.1.2 Name, Role, Value
 * - 4.1.3 Status Messages
 * - 1.3.1 Info and Relationships
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class ScreenReaderSupport {

    // ========================================================================
    // SINGLETON
    // ========================================================================

    private static ScreenReaderSupport instance;

    public static ScreenReaderSupport getInstance() {
        if (instance == null) {
            instance = new ScreenReaderSupport();
        }
        return instance;
    }

    // ========================================================================
    // PROPERTIES
    // ========================================================================

    @Getter
    private final BooleanProperty enabled = new SimpleBooleanProperty(true);

    // Live region for announcements
    private Label liveRegion;
    private final Queue<Announcement> announcementQueue = new ConcurrentLinkedQueue<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    private ScreenReaderSupport() {
        // Create live region for announcements
        liveRegion = new Label();
        liveRegion.setAccessibleRole(AccessibleRole.TEXT);
        liveRegion.setVisible(false);
        liveRegion.setManaged(false);

        // Process announcement queue
        scheduler.scheduleAtFixedRate(this::processAnnouncementQueue, 0, 100, TimeUnit.MILLISECONDS);

        log.info("Screen reader support initialized");
    }

    // ========================================================================
    // LIVE REGION ANNOUNCEMENTS
    // ========================================================================

    /**
     * Announce a message to screen readers
     * @param message The message to announce
     * @param priority POLITE waits for pause, ASSERTIVE interrupts
     */
    public void announce(String message, AnnouncementPriority priority) {
        if (!enabled.get() || message == null || message.isEmpty()) return;

        announcementQueue.add(new Announcement(message, priority, System.currentTimeMillis()));
        log.debug("Queued announcement ({}): {}", priority, message);
    }

    /**
     * Announce politely (doesn't interrupt)
     */
    public void announcePolite(String message) {
        announce(message, AnnouncementPriority.POLITE);
    }

    /**
     * Announce assertively (interrupts current speech)
     */
    public void announceAssertive(String message) {
        announce(message, AnnouncementPriority.ASSERTIVE);
    }

    private void processAnnouncementQueue() {
        Announcement announcement = announcementQueue.poll();
        if (announcement != null) {
            Platform.runLater(() -> {
                // Update live region to trigger screen reader
                liveRegion.setText("");
                liveRegion.setText(announcement.message);
                log.debug("Announced: {}", announcement.message);
            });
        }
    }

    /**
     * Get the live region node (must be added to scene)
     */
    public Node getLiveRegion() {
        return liveRegion;
    }

    // ========================================================================
    // ACCESSIBLE ROLES AND STATES
    // ========================================================================

    /**
     * Set up accessible button with proper semantics
     */
    public void configureButton(Button button, String label, String description) {
        button.setAccessibleRole(AccessibleRole.BUTTON);
        button.setAccessibleText(label);
        if (description != null) {
            button.setAccessibleHelp(description);
        }

        // Announce on click for confirmation
        button.setOnAction(e -> {
            if (button.isDisabled()) return;
            announcePolite(label + " activated");
        });
    }

    /**
     * Set up accessible toggle button
     */
    public void configureToggleButton(ToggleButton button, String label, String pressedLabel, String unpressedLabel) {
        button.setAccessibleRole(AccessibleRole.TOGGLE_BUTTON);
        button.setAccessibleText(label);

        button.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            String state = isSelected ? pressedLabel : unpressedLabel;
            announcePolite(label + " " + state);
        });
    }

    /**
     * Set up accessible checkbox
     */
    public void configureCheckBox(CheckBox checkBox, String label) {
        checkBox.setAccessibleRole(AccessibleRole.CHECK_BOX);
        checkBox.setAccessibleText(label);

        checkBox.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            announcePolite(label + (isSelected ? " checked" : " unchecked"));
        });
    }

    /**
     * Set up accessible radio button group
     */
    public void configureRadioGroup(String groupLabel, RadioButton... buttons) {
        for (int i = 0; i < buttons.length; i++) {
            RadioButton button = buttons[i];
            button.setAccessibleRole(AccessibleRole.RADIO_BUTTON);
            button.setAccessibleText(button.getText() + ", " + (i + 1) + " of " + buttons.length);

            button.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    announcePolite(button.getText() + " selected");
                }
            });
        }
    }

    /**
     * Set up accessible combo box
     */
    public void configureComboBox(ComboBox<?> comboBox, String label) {
        comboBox.setAccessibleRole(AccessibleRole.COMBO_BOX);
        comboBox.setAccessibleText(label);

        comboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                announcePolite(label + " changed to " + newVal.toString());
            }
        });
    }

    /**
     * Set up accessible text field with validation
     */
    public void configureTextField(TextField field, String label, String helpText) {
        field.setAccessibleRole(AccessibleRole.TEXT_FIELD);
        field.setAccessibleText(label);
        if (helpText != null) {
            field.setAccessibleHelp(helpText);
        }
    }

    /**
     * Set up accessible slider
     */
    public void configureSlider(Slider slider, String label, String valueFormat) {
        slider.setAccessibleRole(AccessibleRole.SLIDER);
        slider.setAccessibleText(label);

        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            String formatted = String.format(valueFormat, newVal.doubleValue());
            announcePolite(label + " " + formatted);
        });
    }

    // ========================================================================
    // TABLE ACCESSIBILITY
    // ========================================================================

    /**
     * Configure table for screen reader accessibility
     */
    public void configureTable(TableView<?> table, String tableName) {
        table.setAccessibleRole(AccessibleRole.TABLE_VIEW);
        table.setAccessibleText(tableName);

        // Announce row count changes using InvalidationListener (simpler, no type issues)
        table.getItems().addListener((javafx.beans.Observable obs) -> {
            int count = table.getItems().size();
            announcePolite(tableName + " has " + count + (count == 1 ? " row" : " rows"));
        });

        // Announce selection changes
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int index = table.getSelectionModel().getSelectedIndex() + 1;
                int total = table.getItems().size();
                announcePolite("Row " + index + " of " + total + " selected");
            }
        });

        // Configure column headers
        for (TableColumn<?, ?> column : table.getColumns()) {
            configureTableColumn(column);
        }
    }

    private void configureTableColumn(TableColumn<?, ?> column) {
        // Columns don't have direct accessible properties in JavaFX
        // But we can enhance via cell factories if needed
    }

    /**
     * Announce table cell content when focused
     */
    public void announceTableCell(String columnName, String value, int row, int col) {
        announcePolite(columnName + ": " + value + ", row " + row + ", column " + col);
    }

    // ========================================================================
    // FORM VALIDATION
    // ========================================================================

    /**
     * Announce form validation error
     */
    public void announceValidationError(String fieldName, String error) {
        announceAssertive("Error in " + fieldName + ": " + error);
    }

    /**
     * Announce form validation success
     */
    public void announceValidationSuccess(String fieldName) {
        announcePolite(fieldName + " is valid");
    }

    /**
     * Announce form submission result
     */
    public void announceFormResult(boolean success, String message) {
        if (success) {
            announceAssertive("Success: " + message);
        } else {
            announceAssertive("Error: " + message);
        }
    }

    /**
     * Configure required field indicator
     */
    public void markAsRequired(Control field, String fieldName) {
        String current = field.getAccessibleText();
        field.setAccessibleText((current != null ? current : fieldName) + ", required");
    }

    // ========================================================================
    // PROGRESS AND LOADING
    // ========================================================================

    /**
     * Announce loading state
     */
    public void announceLoading(String context) {
        announcePolite(context + " loading");
    }

    /**
     * Announce loading complete
     */
    public void announceLoadingComplete(String context, int itemCount) {
        announcePolite(context + " loaded with " + itemCount + " items");
    }

    /**
     * Configure progress indicator
     */
    public void configureProgressIndicator(ProgressIndicator indicator, String label) {
        indicator.setAccessibleRole(AccessibleRole.PROGRESS_INDICATOR);
        indicator.setAccessibleText(label);

        indicator.progressProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() < 0) {
                // Indeterminate
                return;
            }
            int percent = (int) (newVal.doubleValue() * 100);
            if (percent % 25 == 0) { // Announce at 25% intervals
                announcePolite(label + " " + percent + " percent complete");
            }
            if (percent == 100) {
                announcePolite(label + " complete");
            }
        });
    }

    /**
     * Configure progress bar
     */
    public void configureProgressBar(ProgressBar bar, String label) {
        bar.setAccessibleRole(AccessibleRole.PROGRESS_INDICATOR);
        bar.setAccessibleText(label);

        bar.progressProperty().addListener((obs, oldVal, newVal) -> {
            int percent = (int) (newVal.doubleValue() * 100);
            if (percent == 100) {
                announcePolite(label + " complete");
            }
        });
    }

    // ========================================================================
    // DIALOG AND ALERT ACCESSIBILITY
    // ========================================================================

    /**
     * Announce dialog opened
     */
    public void announceDialogOpened(String dialogTitle) {
        announceAssertive("Dialog opened: " + dialogTitle);
    }

    /**
     * Announce dialog closed
     */
    public void announceDialogClosed(String dialogTitle) {
        announcePolite(dialogTitle + " dialog closed");
    }

    /**
     * Announce alert
     */
    public void announceAlert(AlertType type, String message) {
        String prefix = switch (type) {
            case ERROR -> "Error: ";
            case WARNING -> "Warning: ";
            case SUCCESS -> "Success: ";
            case INFO -> "Information: ";
        };
        announceAssertive(prefix + message);
    }

    // ========================================================================
    // NAVIGATION ANNOUNCEMENTS
    // ========================================================================

    /**
     * Announce page/view change
     */
    public void announcePageChange(String pageName) {
        announceAssertive("Navigated to " + pageName);
    }

    /**
     * Announce section focus
     */
    public void announceSectionFocus(String sectionName) {
        announcePolite("Now in " + sectionName + " section");
    }

    /**
     * Announce breadcrumb navigation
     */
    public void announceBreadcrumb(String... path) {
        String breadcrumb = String.join(" > ", path);
        announcePolite("You are here: " + breadcrumb);
    }

    // ========================================================================
    // LIST AND SELECTION
    // ========================================================================

    /**
     * Configure accessible list view
     */
    public void configureListView(ListView<?> listView, String listName) {
        listView.setAccessibleRole(AccessibleRole.LIST_VIEW);
        listView.setAccessibleText(listName);

        listView.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
            if (newIdx.intValue() >= 0) {
                Object item = listView.getItems().get(newIdx.intValue());
                announcePolite("Selected: " + item.toString() +
                    ", " + (newIdx.intValue() + 1) + " of " + listView.getItems().size());
            }
        });
    }

    /**
     * Announce multi-selection change
     */
    public void announceMultiSelection(int selectedCount, int totalCount) {
        announcePolite(selectedCount + " of " + totalCount + " items selected");
    }

    // ========================================================================
    // TAB NAVIGATION
    // ========================================================================

    /**
     * Configure accessible tab pane
     */
    public void configureTabPane(TabPane tabPane, String tabPaneName) {
        tabPane.setAccessibleRole(AccessibleRole.TAB_PANE);
        tabPane.setAccessibleText(tabPaneName);

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                int index = tabPane.getSelectionModel().getSelectedIndex() + 1;
                int total = tabPane.getTabs().size();
                announcePolite(newTab.getText() + " tab, " + index + " of " + total);
            }
        });
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    /**
     * Get human-readable count string
     */
    public String getCountString(int count, String singular, String plural) {
        return count + " " + (count == 1 ? singular : plural);
    }

    /**
     * Announce sorted column
     */
    public void announceSortChange(String columnName, boolean ascending) {
        String direction = ascending ? "ascending" : "descending";
        announcePolite("Sorted by " + columnName + ", " + direction);
    }

    /**
     * Announce filter applied
     */
    public void announceFilterApplied(String filterDescription, int resultCount) {
        announcePolite("Filter applied: " + filterDescription + ". " +
            getCountString(resultCount, "result", "results"));
    }

    /**
     * Announce filter cleared
     */
    public void announceFilterCleared() {
        announcePolite("Filters cleared");
    }

    // ========================================================================
    // CLEANUP
    // ========================================================================

    /**
     * Shutdown the screen reader support
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }

    // ========================================================================
    // DATA CLASSES
    // ========================================================================

    private record Announcement(String message, AnnouncementPriority priority, long timestamp) {}

    public enum AnnouncementPriority {
        POLITE,     // Waits for pause in speech
        ASSERTIVE   // Interrupts current speech
    }

    public enum AlertType {
        ERROR, WARNING, SUCCESS, INFO
    }
}
