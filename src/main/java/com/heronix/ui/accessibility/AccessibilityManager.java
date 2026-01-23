package com.heronix.ui.accessibility;

import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.AccessibleRole;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Consumer;

/**
 * Accessibility Manager
 * Centralized management of accessibility features for WCAG 2.1 AA compliance.
 *
 * Features:
 * - Screen reader support (ARIA-like labels)
 * - Keyboard navigation management
 * - Focus indicators and skip links
 * - High contrast mode support
 * - Reduced motion preferences
 * - Font size scaling
 *
 * WCAG 2.1 AA Guidelines Addressed:
 * - 1.1.1 Non-text Content
 * - 1.3.1 Info and Relationships
 * - 1.4.3 Contrast (Minimum)
 * - 1.4.4 Resize Text
 * - 2.1.1 Keyboard
 * - 2.4.1 Bypass Blocks
 * - 2.4.3 Focus Order
 * - 2.4.7 Focus Visible
 * - 4.1.2 Name, Role, Value
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class AccessibilityManager {

    // ========================================================================
    // SINGLETON
    // ========================================================================

    private static AccessibilityManager instance;

    public static AccessibilityManager getInstance() {
        if (instance == null) {
            instance = new AccessibilityManager();
        }
        return instance;
    }

    // ========================================================================
    // PROPERTIES
    // ========================================================================

    @Getter
    private final BooleanProperty screenReaderMode = new SimpleBooleanProperty(false);

    @Getter
    private final BooleanProperty highContrastMode = new SimpleBooleanProperty(false);

    @Getter
    private final BooleanProperty reducedMotion = new SimpleBooleanProperty(false);

    @Getter
    private final DoubleProperty fontScale = new SimpleDoubleProperty(1.0);

    @Getter
    private final BooleanProperty focusIndicatorsEnabled = new SimpleBooleanProperty(true);

    @Getter
    private final ObjectProperty<FocusStyle> focusStyle = new SimpleObjectProperty<>(FocusStyle.OUTLINE);

    // Focus tracking
    private final List<Node> focusHistory = new ArrayList<>();
    private final Map<String, Node> landmarkRegistry = new LinkedHashMap<>();
    private final Map<Node, String> accessibleNames = new HashMap<>();
    private final Map<Node, String> accessibleDescriptions = new HashMap<>();

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    private AccessibilityManager() {
        // Initialize with system preferences detection
        detectSystemPreferences();

        // Listen for property changes
        screenReaderMode.addListener((obs, oldVal, newVal) -> {
            log.info("Screen reader mode: {}", newVal ? "enabled" : "disabled");
            onScreenReaderModeChanged(newVal);
        });

        highContrastMode.addListener((obs, oldVal, newVal) -> {
            log.info("High contrast mode: {}", newVal ? "enabled" : "disabled");
        });

        reducedMotion.addListener((obs, oldVal, newVal) -> {
            log.info("Reduced motion: {}", newVal ? "enabled" : "disabled");
        });

        fontScale.addListener((obs, oldVal, newVal) -> {
            log.info("Font scale changed to: {}%", newVal.doubleValue() * 100);
        });
    }

    private void detectSystemPreferences() {
        // In a real implementation, detect OS accessibility settings
        // For now, use defaults
        log.info("Accessibility manager initialized with default settings");
    }

    // ========================================================================
    // ACCESSIBLE LABELS (WCAG 4.1.2)
    // ========================================================================

    /**
     * Set accessible name for screen readers (WCAG 4.1.2)
     */
    public void setAccessibleName(Node node, String name) {
        if (node != null && name != null) {
            node.setAccessibleText(name);
            accessibleNames.put(node, name);

            // For controls, also set as accessible role description
            if (node instanceof Control control) {
                control.setAccessibleHelp(name);
            }
        }
    }

    /**
     * Set accessible description for additional context
     */
    public void setAccessibleDescription(Node node, String description) {
        if (node != null && description != null) {
            accessibleDescriptions.put(node, description);

            if (node instanceof Control control) {
                control.setAccessibleHelp(description);
            }
        }
    }

    /**
     * Set accessible role for semantic meaning
     */
    public void setAccessibleRole(Node node, AccessibleRole role) {
        if (node != null && role != null) {
            node.setAccessibleRole(role);
        }
    }

    /**
     * Make a decorative image accessible (WCAG 1.1.1)
     */
    public void markAsDecorative(Node node) {
        if (node != null) {
            node.setAccessibleRole(AccessibleRole.IMAGE_VIEW);
            node.setAccessibleText("");
            node.setFocusTraversable(false);
        }
    }

    /**
     * Create accessible label for form field (WCAG 1.3.1)
     */
    public void associateLabelWithControl(Label label, Control control) {
        if (label != null && control != null) {
            label.setLabelFor(control);
            String labelText = label.getText();
            if (labelText != null && !labelText.isEmpty()) {
                setAccessibleName(control, labelText.replace(":", "").trim());
            }
        }
    }

    // ========================================================================
    // LANDMARK REGIONS (WCAG 2.4.1)
    // ========================================================================

    /**
     * Register a landmark region for skip navigation
     */
    public void registerLandmark(String id, String name, Node node) {
        if (id != null && node != null) {
            landmarkRegistry.put(id, node);
            setAccessibleName(node, name);
            node.setAccessibleRole(AccessibleRole.PARENT);
            log.debug("Registered landmark: {} - {}", id, name);
        }
    }

    /**
     * Navigate to a landmark by ID
     */
    public void navigateToLandmark(String landmarkId) {
        Node landmark = landmarkRegistry.get(landmarkId);
        if (landmark != null) {
            landmark.requestFocus();
            announceToScreenReader("Navigated to " + accessibleNames.getOrDefault(landmark, landmarkId));
        }
    }

    /**
     * Get all registered landmarks for skip link menu
     */
    public Map<String, String> getLandmarks() {
        Map<String, String> landmarks = new LinkedHashMap<>();
        for (Map.Entry<String, Node> entry : landmarkRegistry.entrySet()) {
            String name = accessibleNames.getOrDefault(entry.getValue(), entry.getKey());
            landmarks.put(entry.getKey(), name);
        }
        return landmarks;
    }

    /**
     * Create skip link component (WCAG 2.4.1)
     */
    public Region createSkipLinks() {
        VBox skipLinks = new VBox(4);
        skipLinks.setStyle("""
            -fx-background-color: #1E293B;
            -fx-padding: 8 16;
            -fx-position: absolute;
            -fx-translate-y: -100;
            """);
        skipLinks.setFocusTraversable(true);

        // Show on focus
        skipLinks.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            skipLinks.setStyle(String.format("""
                -fx-background-color: #1E293B;
                -fx-padding: 8 16;
                -fx-translate-y: %d;
                """, isFocused ? 0 : -100));
        });

        for (Map.Entry<String, String> landmark : getLandmarks().entrySet()) {
            Hyperlink link = new Hyperlink("Skip to " + landmark.getValue());
            link.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
            link.setOnAction(e -> navigateToLandmark(landmark.getKey()));
            skipLinks.getChildren().add(link);
        }

        return skipLinks;
    }

    // ========================================================================
    // FOCUS MANAGEMENT (WCAG 2.4.3, 2.4.7)
    // ========================================================================

    /**
     * Apply focus indicator styling (WCAG 2.4.7)
     */
    public void applyFocusIndicator(Node node) {
        if (node == null || !focusIndicatorsEnabled.get()) return;

        String focusStyleCss = switch (focusStyle.get()) {
            case OUTLINE -> """
                -fx-effect: dropshadow(gaussian, #3B82F6, 0, 0, 0, 0);
                -fx-border-color: #3B82F6;
                -fx-border-width: 2;
                -fx-border-radius: 4;
                """;
            case GLOW -> """
                -fx-effect: dropshadow(gaussian, #3B82F6, 8, 0.5, 0, 0);
                """;
            case UNDERLINE -> """
                -fx-border-color: transparent transparent #3B82F6 transparent;
                -fx-border-width: 0 0 3 0;
                """;
            case HIGH_VISIBILITY -> """
                -fx-effect: dropshadow(gaussian, #FFFF00, 4, 1, 0, 0);
                -fx-border-color: #000000;
                -fx-border-width: 3;
                """;
        };

        String originalStyle = node.getStyle();

        node.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) {
                node.setStyle(originalStyle + focusStyleCss);
                trackFocus(node);
            } else {
                node.setStyle(originalStyle);
            }
        });
    }

    /**
     * Create focus trap for modal dialogs (WCAG 2.4.3)
     */
    public void createFocusTrap(Parent container) {
        List<Node> focusableNodes = getFocusableChildren(container);
        if (focusableNodes.isEmpty()) return;

        Node firstFocusable = focusableNodes.get(0);
        Node lastFocusable = focusableNodes.get(focusableNodes.size() - 1);

        // Trap focus within container
        container.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.TAB) {
                Node focused = container.getScene().getFocusOwner();

                if (e.isShiftDown() && focused == firstFocusable) {
                    e.consume();
                    lastFocusable.requestFocus();
                } else if (!e.isShiftDown() && focused == lastFocusable) {
                    e.consume();
                    firstFocusable.requestFocus();
                }
            }
        });

        // Focus first element
        firstFocusable.requestFocus();
    }

    /**
     * Restore focus to previous element (for closing dialogs)
     */
    public void restoreFocus() {
        if (!focusHistory.isEmpty()) {
            Node previousFocus = focusHistory.remove(focusHistory.size() - 1);
            if (previousFocus != null && previousFocus.getScene() != null) {
                previousFocus.requestFocus();
            }
        }
    }

    private void trackFocus(Node node) {
        if (focusHistory.size() > 10) {
            focusHistory.remove(0);
        }
        focusHistory.add(node);
    }

    private List<Node> getFocusableChildren(Parent parent) {
        List<Node> focusable = new ArrayList<>();
        collectFocusableNodes(parent, focusable);
        return focusable;
    }

    private void collectFocusableNodes(Parent parent, List<Node> result) {
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (child.isFocusTraversable() && !child.isDisabled() && child.isVisible()) {
                result.add(child);
            }
            if (child instanceof Parent p) {
                collectFocusableNodes(p, result);
            }
        }
    }

    // ========================================================================
    // KEYBOARD SHORTCUTS (WCAG 2.1.1)
    // ========================================================================

    /**
     * Register global keyboard shortcut
     */
    public void registerGlobalShortcut(Scene scene, KeyCombination keys, Runnable action, String description) {
        scene.getAccelerators().put(keys, action);
        log.debug("Registered shortcut: {} - {}", keys.getDisplayText(), description);
    }

    /**
     * Create keyboard shortcut help dialog
     */
    public VBox createShortcutHelp() {
        VBox help = new VBox(16);
        help.setStyle("-fx-padding: 20; -fx-background-color: white;");

        Label title = new Label("Keyboard Shortcuts");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 700;");

        VBox shortcuts = new VBox(8);

        String[][] shortcutData = {
            {"Alt + 1", "Go to Dashboard"},
            {"Alt + 2", "Go to Students"},
            {"Alt + 3", "Go to Grades"},
            {"Alt + S", "Open Search"},
            {"Alt + N", "New Record"},
            {"Ctrl + S", "Save"},
            {"Ctrl + P", "Print"},
            {"Escape", "Close Dialog / Cancel"},
            {"Tab", "Next Field"},
            {"Shift + Tab", "Previous Field"},
            {"Enter", "Activate / Submit"},
            {"Space", "Toggle Checkbox / Select"},
            {"Arrow Keys", "Navigate Lists / Tables"},
            {"F1", "Help"},
            {"Alt + H", "Show Shortcuts Help"}
        };

        for (String[] shortcut : shortcutData) {
            HBox row = new HBox(16);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label key = new Label(shortcut[0]);
            key.setMinWidth(120);
            key.setStyle("""
                -fx-font-family: monospace;
                -fx-font-size: 12px;
                -fx-background-color: #F1F5F9;
                -fx-padding: 4 8;
                -fx-background-radius: 4;
                """);

            Label desc = new Label(shortcut[1]);
            desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

            row.getChildren().addAll(key, desc);
            shortcuts.getChildren().add(row);
        }

        help.getChildren().addAll(title, shortcuts);
        return help;
    }

    // ========================================================================
    // SCREEN READER SUPPORT (WCAG 4.1.2)
    // ========================================================================

    /**
     * Announce message to screen reader (live region)
     */
    public void announceToScreenReader(String message) {
        if (screenReaderMode.get()) {
            log.info("Screen reader announcement: {}", message);
            // In JavaFX, use accessible text updates for live announcements
            // This would integrate with platform screen readers
        }
    }

    /**
     * Announce polite message (doesn't interrupt)
     */
    public void announcePolite(String message) {
        announceToScreenReader(message);
    }

    /**
     * Announce assertive message (interrupts current speech)
     */
    public void announceAssertive(String message) {
        if (screenReaderMode.get()) {
            log.info("Screen reader (assertive): {}", message);
        }
    }

    private void onScreenReaderModeChanged(boolean enabled) {
        if (enabled) {
            announceAssertive("Screen reader mode enabled. Use keyboard shortcuts for navigation.");
        }
    }

    // ========================================================================
    // TEXT AND CONTRAST (WCAG 1.4.3, 1.4.4)
    // ========================================================================

    /**
     * Check if color combination meets contrast requirements
     * WCAG 2.1 AA requires 4.5:1 for normal text, 3:1 for large text
     */
    public boolean meetsContrastRequirement(String foreground, String background, boolean largeText) {
        double contrast = calculateContrastRatio(foreground, background);
        double required = largeText ? 3.0 : 4.5;
        return contrast >= required;
    }

    /**
     * Calculate contrast ratio between two colors
     */
    public double calculateContrastRatio(String color1, String color2) {
        double l1 = getRelativeLuminance(color1);
        double l2 = getRelativeLuminance(color2);

        double lighter = Math.max(l1, l2);
        double darker = Math.min(l1, l2);

        return (lighter + 0.05) / (darker + 0.05);
    }

    private double getRelativeLuminance(String hexColor) {
        // Parse hex color
        String hex = hexColor.replace("#", "");
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);

        // Convert to sRGB
        double rs = r / 255.0;
        double gs = g / 255.0;
        double bs = b / 255.0;

        // Apply gamma correction
        double rg = rs <= 0.03928 ? rs / 12.92 : Math.pow((rs + 0.055) / 1.055, 2.4);
        double gg = gs <= 0.03928 ? gs / 12.92 : Math.pow((gs + 0.055) / 1.055, 2.4);
        double bg = bs <= 0.03928 ? bs / 12.92 : Math.pow((bs + 0.055) / 1.055, 2.4);

        // Calculate luminance
        return 0.2126 * rg + 0.7152 * gg + 0.0722 * bg;
    }

    /**
     * Apply font scaling to a scene
     */
    public void applyFontScale(Scene scene) {
        String scaleStyle = String.format("-fx-font-size: %.0f%%;", fontScale.get() * 100);
        scene.getRoot().setStyle(scene.getRoot().getStyle() + scaleStyle);
    }

    // ========================================================================
    // ANIMATION CONTROL (WCAG 2.3.3)
    // ========================================================================

    /**
     * Check if animations should be played
     */
    public boolean shouldAnimate() {
        return !reducedMotion.get();
    }

    /**
     * Get animation duration based on preferences
     */
    public javafx.util.Duration getAnimationDuration(javafx.util.Duration normal) {
        if (reducedMotion.get()) {
            return javafx.util.Duration.ZERO;
        }
        return normal;
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    /**
     * Make a node accessible with common settings
     */
    public void makeAccessible(Node node, String name, String description, AccessibleRole role) {
        setAccessibleName(node, name);
        if (description != null) {
            setAccessibleDescription(node, description);
        }
        if (role != null) {
            setAccessibleRole(node, role);
        }
        applyFocusIndicator(node);
    }

    /**
     * Configure a button for accessibility
     */
    public void configureAccessibleButton(Button button, String name, String shortcutHint) {
        setAccessibleName(button, name);
        if (shortcutHint != null) {
            setAccessibleDescription(button, "Keyboard shortcut: " + shortcutHint);
            button.setTooltip(new Tooltip(name + " (" + shortcutHint + ")"));
        }
        applyFocusIndicator(button);
    }

    /**
     * Configure a table for accessibility
     */
    public void configureAccessibleTable(TableView<?> table, String name) {
        setAccessibleName(table, name);
        setAccessibleRole(table, AccessibleRole.TABLE_VIEW);

        // Announce row count changes using InvalidationListener (simpler, no type issues)
        table.getItems().addListener((javafx.beans.Observable obs) -> {
            announcePolite(name + " now has " + table.getItems().size() + " items");
        });
    }

    /**
     * Configure form field for accessibility
     */
    public void configureAccessibleFormField(Control field, Label label, String helpText) {
        associateLabelWithControl(label, field);
        if (helpText != null) {
            setAccessibleDescription(field, helpText);
        }
        applyFocusIndicator(field);
    }

    // ========================================================================
    // ENUMS
    // ========================================================================

    public enum FocusStyle {
        OUTLINE,
        GLOW,
        UNDERLINE,
        HIGH_VISIBILITY
    }
}
