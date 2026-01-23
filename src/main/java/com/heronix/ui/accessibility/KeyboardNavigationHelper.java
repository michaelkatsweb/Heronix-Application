package com.heronix.ui.accessibility;

import javafx.collections.ObservableList;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Consumer;

/**
 * Keyboard Navigation Helper
 * Enhanced keyboard navigation support for WCAG 2.1.1 compliance.
 *
 * Features:
 * - Arrow key navigation in lists/grids
 * - Tab order management
 * - Focus trap for modals
 * - Keyboard shortcuts registration
 * - Custom key handlers
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class KeyboardNavigationHelper {

    // ========================================================================
    // SINGLETON
    // ========================================================================

    private static KeyboardNavigationHelper instance;

    public static KeyboardNavigationHelper getInstance() {
        if (instance == null) {
            instance = new KeyboardNavigationHelper();
        }
        return instance;
    }

    // ========================================================================
    // DATA
    // ========================================================================

    private final Map<Scene, List<KeyBinding>> sceneBindings = new HashMap<>();
    private final Map<Node, NavigationGroup> navigationGroups = new HashMap<>();

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    private KeyboardNavigationHelper() {
        log.info("Keyboard navigation helper initialized");
    }

    // ========================================================================
    // GLOBAL SHORTCUTS
    // ========================================================================

    /**
     * Register global keyboard shortcuts for a scene
     */
    public void registerGlobalShortcuts(Scene scene) {
        // Navigation shortcuts
        registerShortcut(scene, new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.ALT_DOWN),
            "Navigate to Dashboard", () -> navigateToSection("dashboard"));

        registerShortcut(scene, new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.ALT_DOWN),
            "Navigate to Students", () -> navigateToSection("students"));

        registerShortcut(scene, new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.ALT_DOWN),
            "Navigate to Grades", () -> navigateToSection("grades"));

        registerShortcut(scene, new KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.ALT_DOWN),
            "Navigate to Attendance", () -> navigateToSection("attendance"));

        registerShortcut(scene, new KeyCodeCombination(KeyCode.DIGIT5, KeyCombination.ALT_DOWN),
            "Navigate to Reports", () -> navigateToSection("reports"));

        // Action shortcuts
        registerShortcut(scene, new KeyCodeCombination(KeyCode.S, KeyCombination.ALT_DOWN),
            "Open Search", () -> triggerAction("search"));

        registerShortcut(scene, new KeyCodeCombination(KeyCode.N, KeyCombination.ALT_DOWN),
            "New Record", () -> triggerAction("new"));

        registerShortcut(scene, new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
            "Save", () -> triggerAction("save"));

        registerShortcut(scene, new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN),
            "Print", () -> triggerAction("print"));

        registerShortcut(scene, new KeyCodeCombination(KeyCode.H, KeyCombination.ALT_DOWN),
            "Show Help", () -> triggerAction("help"));

        registerShortcut(scene, new KeyCodeCombination(KeyCode.F1),
            "Show Help", () -> triggerAction("help"));

        // Theme toggle
        registerShortcut(scene, new KeyCodeCombination(KeyCode.T, KeyCombination.ALT_DOWN),
            "Toggle Theme", () -> ThemeManager.getInstance().toggleDarkMode());

        log.info("Registered global shortcuts for scene");
    }

    /**
     * Register a keyboard shortcut
     */
    public void registerShortcut(Scene scene, KeyCombination keys, String description, Runnable action) {
        scene.getAccelerators().put(keys, action);

        // Track for help display
        sceneBindings.computeIfAbsent(scene, k -> new ArrayList<>())
            .add(new KeyBinding(keys, description, action));
    }

    /**
     * Get all registered shortcuts for a scene
     */
    public List<KeyBinding> getShortcuts(Scene scene) {
        return sceneBindings.getOrDefault(scene, Collections.emptyList());
    }

    // ========================================================================
    // NAVIGATION GROUPS
    // ========================================================================

    /**
     * Create a navigation group for arrow key navigation
     */
    public void createNavigationGroup(String id, Parent container, NavigationType type) {
        NavigationGroup group = new NavigationGroup(id, container, type);
        navigationGroups.put(container, group);

        setupGroupNavigation(group);
        log.debug("Created navigation group: {} ({})", id, type);
    }

    private void setupGroupNavigation(NavigationGroup group) {
        Parent container = group.container;

        container.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (!isNavigationKey(e.getCode())) return;

            Node focused = container.getScene().getFocusOwner();
            if (focused == null || !isChildOf(focused, container)) return;

            List<Node> focusable = getFocusableChildren(container);
            int currentIndex = focusable.indexOf(focused);
            if (currentIndex < 0) return;

            int nextIndex = calculateNextIndex(currentIndex, focusable.size(), e.getCode(), group.type);

            if (nextIndex != currentIndex && nextIndex >= 0 && nextIndex < focusable.size()) {
                focusable.get(nextIndex).requestFocus();
                e.consume();
            }
        });
    }

    private boolean isNavigationKey(KeyCode code) {
        return code == KeyCode.UP || code == KeyCode.DOWN ||
               code == KeyCode.LEFT || code == KeyCode.RIGHT ||
               code == KeyCode.HOME || code == KeyCode.END;
    }

    private int calculateNextIndex(int current, int total, KeyCode key, NavigationType type) {
        return switch (type) {
            case VERTICAL_LIST -> switch (key) {
                case UP -> Math.max(0, current - 1);
                case DOWN -> Math.min(total - 1, current + 1);
                case HOME -> 0;
                case END -> total - 1;
                default -> current;
            };
            case HORIZONTAL_LIST -> switch (key) {
                case LEFT -> Math.max(0, current - 1);
                case RIGHT -> Math.min(total - 1, current + 1);
                case HOME -> 0;
                case END -> total - 1;
                default -> current;
            };
            case GRID -> current; // Grid navigation requires column count
            case ROVING_TABINDEX -> switch (key) {
                case UP, LEFT -> Math.max(0, current - 1);
                case DOWN, RIGHT -> Math.min(total - 1, current + 1);
                case HOME -> 0;
                case END -> total - 1;
                default -> current;
            };
        };
    }

    // ========================================================================
    // GRID NAVIGATION
    // ========================================================================

    /**
     * Setup grid navigation (for tables, calendars, etc.)
     */
    public void setupGridNavigation(Parent grid, int columns) {
        grid.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (!isNavigationKey(e.getCode())) return;

            Node focused = grid.getScene().getFocusOwner();
            if (focused == null || !isChildOf(focused, grid)) return;

            List<Node> focusable = getFocusableChildren(grid);
            int currentIndex = focusable.indexOf(focused);
            if (currentIndex < 0) return;

            int rows = (int) Math.ceil(focusable.size() / (double) columns);
            int row = currentIndex / columns;
            int col = currentIndex % columns;

            int nextIndex = switch (e.getCode()) {
                case UP -> currentIndex - columns;
                case DOWN -> currentIndex + columns;
                case LEFT -> currentIndex - 1;
                case RIGHT -> currentIndex + 1;
                case HOME -> e.isControlDown() ? 0 : row * columns;
                case END -> e.isControlDown() ? focusable.size() - 1 : Math.min(row * columns + columns - 1, focusable.size() - 1);
                default -> currentIndex;
            };

            if (nextIndex != currentIndex && nextIndex >= 0 && nextIndex < focusable.size()) {
                focusable.get(nextIndex).requestFocus();
                e.consume();
            }
        });
    }

    // ========================================================================
    // FOCUS TRAP
    // ========================================================================

    /**
     * Create focus trap for modal dialogs
     */
    public void createFocusTrap(Parent container, Node initialFocus) {
        List<Node> focusable = getFocusableChildren(container);
        if (focusable.isEmpty()) return;

        Node first = focusable.get(0);
        Node last = focusable.get(focusable.size() - 1);

        container.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() != KeyCode.TAB) return;

            Node focused = container.getScene().getFocusOwner();

            if (e.isShiftDown() && focused == first) {
                e.consume();
                last.requestFocus();
            } else if (!e.isShiftDown() && focused == last) {
                e.consume();
                first.requestFocus();
            }
        });

        // Escape to close (common pattern)
        container.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                triggerAction("close-modal");
                e.consume();
            }
        });

        // Set initial focus
        if (initialFocus != null) {
            initialFocus.requestFocus();
        } else {
            first.requestFocus();
        }
    }

    // ========================================================================
    // TAB ORDER MANAGEMENT
    // ========================================================================

    /**
     * Set explicit tab order for a container
     */
    public void setTabOrder(Parent container, Node... orderedNodes) {
        // Make all children non-focusable
        for (Node child : container.getChildrenUnmodifiable()) {
            if (child.isFocusTraversable()) {
                child.setFocusTraversable(false);
            }
        }

        // Enable focus only for specified nodes in order
        for (int i = 0; i < orderedNodes.length; i++) {
            Node node = orderedNodes[i];
            node.setFocusTraversable(true);

            // Store tab index as user data
            node.setUserData(i);
        }

        // Custom tab handling
        container.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() != KeyCode.TAB) return;

            Node focused = container.getScene().getFocusOwner();
            if (focused == null) return;

            int currentIndex = -1;
            for (int i = 0; i < orderedNodes.length; i++) {
                if (orderedNodes[i] == focused) {
                    currentIndex = i;
                    break;
                }
            }

            if (currentIndex >= 0) {
                int nextIndex;
                if (e.isShiftDown()) {
                    nextIndex = currentIndex > 0 ? currentIndex - 1 : orderedNodes.length - 1;
                } else {
                    nextIndex = currentIndex < orderedNodes.length - 1 ? currentIndex + 1 : 0;
                }

                orderedNodes[nextIndex].requestFocus();
                e.consume();
            }
        });
    }

    // ========================================================================
    // MENU NAVIGATION
    // ========================================================================

    /**
     * Setup menu-style keyboard navigation
     */
    public void setupMenuNavigation(Parent menu, Consumer<Node> onSelect) {
        List<Node> items = getFocusableChildren(menu);
        if (items.isEmpty()) return;

        menu.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            Node focused = menu.getScene().getFocusOwner();
            int currentIndex = items.indexOf(focused);
            if (currentIndex < 0) return;

            switch (e.getCode()) {
                case UP -> {
                    int prev = currentIndex > 0 ? currentIndex - 1 : items.size() - 1;
                    items.get(prev).requestFocus();
                    e.consume();
                }
                case DOWN -> {
                    int next = currentIndex < items.size() - 1 ? currentIndex + 1 : 0;
                    items.get(next).requestFocus();
                    e.consume();
                }
                case ENTER, SPACE -> {
                    if (onSelect != null) {
                        onSelect.accept(focused);
                    }
                    e.consume();
                }
                case HOME -> {
                    items.get(0).requestFocus();
                    e.consume();
                }
                case END -> {
                    items.get(items.size() - 1).requestFocus();
                    e.consume();
                }
            }
        });

        // Type-ahead search
        StringBuilder typeAhead = new StringBuilder();
        long[] lastKeyTime = {0};

        menu.addEventFilter(KeyEvent.KEY_TYPED, e -> {
            String ch = e.getCharacter();
            if (ch.length() != 1 || !Character.isLetterOrDigit(ch.charAt(0))) return;

            long now = System.currentTimeMillis();
            if (now - lastKeyTime[0] > 500) {
                typeAhead.setLength(0);
            }
            lastKeyTime[0] = now;
            typeAhead.append(ch.toLowerCase());

            String search = typeAhead.toString();
            for (Node item : items) {
                String text = getNodeText(item).toLowerCase();
                if (text.startsWith(search)) {
                    item.requestFocus();
                    break;
                }
            }

            e.consume();
        });
    }

    // ========================================================================
    // TABLE NAVIGATION
    // ========================================================================

    /**
     * Setup enhanced table navigation
     */
    public void setupTableNavigation(TableView<?> table) {
        table.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case HOME -> {
                    if (e.isControlDown()) {
                        table.getSelectionModel().selectFirst();
                        table.scrollTo(0);
                    }
                    e.consume();
                }
                case END -> {
                    if (e.isControlDown()) {
                        table.getSelectionModel().selectLast();
                        table.scrollTo(table.getItems().size() - 1);
                    }
                    e.consume();
                }
                case PAGE_UP -> {
                    int visibleRows = 10; // Approximate
                    int newIndex = Math.max(0, table.getSelectionModel().getSelectedIndex() - visibleRows);
                    table.getSelectionModel().select(newIndex);
                    table.scrollTo(newIndex);
                    e.consume();
                }
                case PAGE_DOWN -> {
                    int visibleRows = 10;
                    int newIndex = Math.min(table.getItems().size() - 1,
                        table.getSelectionModel().getSelectedIndex() + visibleRows);
                    table.getSelectionModel().select(newIndex);
                    table.scrollTo(newIndex);
                    e.consume();
                }
                case A -> {
                    if (e.isControlDown()) {
                        table.getSelectionModel().selectAll();
                        e.consume();
                    }
                }
            }
        });
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

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

    private boolean isChildOf(Node node, Parent parent) {
        Node current = node;
        while (current != null) {
            if (current == parent) return true;
            current = current.getParent();
        }
        return false;
    }

    private String getNodeText(Node node) {
        if (node instanceof Labeled labeled) {
            return labeled.getText() != null ? labeled.getText() : "";
        }
        if (node instanceof TextInputControl textInput) {
            return textInput.getText() != null ? textInput.getText() : "";
        }
        return "";
    }

    // Action callbacks - these would be wired to actual navigation/action handlers
    private Consumer<String> navigationHandler;
    private Consumer<String> actionHandler;

    public void setNavigationHandler(Consumer<String> handler) {
        this.navigationHandler = handler;
    }

    public void setActionHandler(Consumer<String> handler) {
        this.actionHandler = handler;
    }

    private void navigateToSection(String section) {
        if (navigationHandler != null) {
            navigationHandler.accept(section);
        }
        log.debug("Navigate to: {}", section);
    }

    private void triggerAction(String action) {
        if (actionHandler != null) {
            actionHandler.accept(action);
        }
        log.debug("Trigger action: {}", action);
    }

    // ========================================================================
    // HELP DISPLAY
    // ========================================================================

    /**
     * Create a help display for keyboard shortcuts
     * @return VBox containing shortcut help or null if no shortcuts registered
     */
    public javafx.scene.layout.VBox createShortcutHelp() {
        if (sceneBindings.isEmpty()) {
            return null;
        }

        javafx.scene.layout.VBox container = new javafx.scene.layout.VBox(8);
        container.setStyle("-fx-padding: 16;");

        for (var entry : sceneBindings.entrySet()) {
            for (KeyBinding binding : entry.getValue()) {
                javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(16);
                row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                javafx.scene.control.Label keyLabel = new javafx.scene.control.Label(binding.keys().getDisplayText());
                keyLabel.setStyle("-fx-font-weight: bold; -fx-background-color: #e0e0e0; -fx-padding: 4 8; -fx-background-radius: 4;");
                keyLabel.setMinWidth(120);

                javafx.scene.control.Label descLabel = new javafx.scene.control.Label(binding.description());
                descLabel.setStyle("-fx-text-fill: #666;");

                row.getChildren().addAll(keyLabel, descLabel);
                container.getChildren().add(row);
            }
        }

        return container;
    }

    // ========================================================================
    // DATA CLASSES
    // ========================================================================

    public record KeyBinding(KeyCombination keys, String description, Runnable action) {}

    private record NavigationGroup(String id, Parent container, NavigationType type) {}

    public enum NavigationType {
        VERTICAL_LIST,
        HORIZONTAL_LIST,
        GRID,
        ROVING_TABINDEX
    }
}
