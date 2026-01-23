package com.heronix.ui.service;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;

/**
 * Keyboard Shortcut Manager
 * Centralized management of keyboard shortcuts across the application.
 *
 * Features:
 * - Register/unregister shortcuts dynamically
 * - Context-aware shortcuts (global vs view-specific)
 * - Shortcut help overlay generation
 * - Conflict detection
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
@Service
public class KeyboardShortcutManager {

    // ========================================================================
    // SHORTCUT REGISTRY
    // ========================================================================

    private final Map<KeyCombination, ShortcutEntry> globalShortcuts = new LinkedHashMap<>();
    private final Map<String, Map<KeyCombination, ShortcutEntry>> contextShortcuts = new HashMap<>();

    private Scene currentScene;
    private String currentContext = "global";

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    /**
     * Initialize with a scene
     */
    public void initialize(Scene scene) {
        this.currentScene = scene;
        registerDefaultShortcuts();
        applyShortcuts();
        log.info("✓ KeyboardShortcutManager initialized");
    }

    /**
     * Register default application shortcuts
     */
    private void registerDefaultShortcuts() {
        // Navigation shortcuts are registered by MainController
        // This method registers additional utility shortcuts

        log.info("Default shortcuts registered");
    }

    // ========================================================================
    // REGISTRATION API
    // ========================================================================

    /**
     * Register a global keyboard shortcut
     *
     * @param keyCombo The key combination (e.g., "Ctrl+K")
     * @param action   The action to execute
     * @param category Category for grouping in help
     * @param description Human-readable description
     */
    public void registerGlobal(String keyCombo, Runnable action, String category, String description) {
        KeyCombination combo = parseKeyCombination(keyCombo);
        if (combo != null) {
            globalShortcuts.put(combo, new ShortcutEntry(keyCombo, action, category, description, true));
            applyShortcuts();
            log.debug("Registered global shortcut: {} - {}", keyCombo, description);
        }
    }

    /**
     * Register a global shortcut with KeyCombination
     */
    public void registerGlobal(KeyCombination combo, Runnable action, String category, String description) {
        String keyCombo = formatKeyCombination(combo);
        globalShortcuts.put(combo, new ShortcutEntry(keyCombo, action, category, description, true));
        applyShortcuts();
        log.debug("Registered global shortcut: {} - {}", keyCombo, description);
    }

    /**
     * Register a context-specific shortcut (only active in certain views)
     */
    public void registerContext(String context, String keyCombo, Runnable action, String category, String description) {
        KeyCombination combo = parseKeyCombination(keyCombo);
        if (combo != null) {
            contextShortcuts
                    .computeIfAbsent(context, k -> new LinkedHashMap<>())
                    .put(combo, new ShortcutEntry(keyCombo, action, category, description, false));

            if (context.equals(currentContext)) {
                applyShortcuts();
            }
            log.debug("Registered context shortcut [{}]: {} - {}", context, keyCombo, description);
        }
    }

    /**
     * Unregister a global shortcut
     */
    public void unregisterGlobal(String keyCombo) {
        KeyCombination combo = parseKeyCombination(keyCombo);
        if (combo != null) {
            globalShortcuts.remove(combo);
            applyShortcuts();
            log.debug("Unregistered global shortcut: {}", keyCombo);
        }
    }

    /**
     * Unregister a context shortcut
     */
    public void unregisterContext(String context, String keyCombo) {
        KeyCombination combo = parseKeyCombination(keyCombo);
        if (combo != null && contextShortcuts.containsKey(context)) {
            contextShortcuts.get(context).remove(combo);
            if (context.equals(currentContext)) {
                applyShortcuts();
            }
        }
    }

    /**
     * Clear all shortcuts for a context
     */
    public void clearContext(String context) {
        contextShortcuts.remove(context);
        if (context.equals(currentContext)) {
            applyShortcuts();
        }
    }

    // ========================================================================
    // CONTEXT MANAGEMENT
    // ========================================================================

    /**
     * Set the current context (activates context-specific shortcuts)
     */
    public void setContext(String context) {
        if (!context.equals(currentContext)) {
            currentContext = context;
            applyShortcuts();
            log.debug("Switched to context: {}", context);
        }
    }

    /**
     * Get current context
     */
    public String getContext() {
        return currentContext;
    }

    // ========================================================================
    // SHORTCUT APPLICATION
    // ========================================================================

    /**
     * Apply all registered shortcuts to the current scene
     */
    private void applyShortcuts() {
        if (currentScene == null) {
            log.warn("Cannot apply shortcuts: no scene set");
            return;
        }

        // Clear existing accelerators
        currentScene.getAccelerators().clear();

        // Apply global shortcuts
        for (Map.Entry<KeyCombination, ShortcutEntry> entry : globalShortcuts.entrySet()) {
            currentScene.getAccelerators().put(entry.getKey(), entry.getValue().action);
        }

        // Apply context shortcuts
        Map<KeyCombination, ShortcutEntry> contextMap = contextShortcuts.get(currentContext);
        if (contextMap != null) {
            for (Map.Entry<KeyCombination, ShortcutEntry> entry : contextMap.entrySet()) {
                // Context shortcuts override global if there's a conflict
                currentScene.getAccelerators().put(entry.getKey(), entry.getValue().action);
            }
        }

        log.debug("Applied {} global + {} context shortcuts",
                globalShortcuts.size(),
                contextMap != null ? contextMap.size() : 0);
    }

    // ========================================================================
    // HELP GENERATION
    // ========================================================================

    /**
     * Get all shortcuts grouped by category for help display
     */
    public Map<String, List<ShortcutInfo>> getShortcutsByCategory() {
        Map<String, List<ShortcutInfo>> result = new LinkedHashMap<>();

        // Add global shortcuts
        for (Map.Entry<KeyCombination, ShortcutEntry> entry : globalShortcuts.entrySet()) {
            ShortcutEntry se = entry.getValue();
            result.computeIfAbsent(se.category, k -> new ArrayList<>())
                    .add(new ShortcutInfo(se.keyCombo, se.description, true));
        }

        // Add current context shortcuts
        Map<KeyCombination, ShortcutEntry> contextMap = contextShortcuts.get(currentContext);
        if (contextMap != null) {
            for (Map.Entry<KeyCombination, ShortcutEntry> entry : contextMap.entrySet()) {
                ShortcutEntry se = entry.getValue();
                result.computeIfAbsent(se.category, k -> new ArrayList<>())
                        .add(new ShortcutInfo(se.keyCombo, se.description, false));
            }
        }

        return result;
    }

    /**
     * Get all shortcuts as a flat list
     */
    public List<ShortcutInfo> getAllShortcuts() {
        List<ShortcutInfo> result = new ArrayList<>();

        for (ShortcutEntry se : globalShortcuts.values()) {
            result.add(new ShortcutInfo(se.keyCombo, se.description, true));
        }

        Map<KeyCombination, ShortcutEntry> contextMap = contextShortcuts.get(currentContext);
        if (contextMap != null) {
            for (ShortcutEntry se : contextMap.values()) {
                result.add(new ShortcutInfo(se.keyCombo, se.description, false));
            }
        }

        return result;
    }

    /**
     * Generate help text for display
     */
    public String generateHelpText() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════\n");
        sb.append("           KEYBOARD SHORTCUTS              \n");
        sb.append("═══════════════════════════════════════════\n\n");

        Map<String, List<ShortcutInfo>> byCategory = getShortcutsByCategory();

        for (Map.Entry<String, List<ShortcutInfo>> entry : byCategory.entrySet()) {
            sb.append("▸ ").append(entry.getKey().toUpperCase()).append("\n");
            sb.append("───────────────────────────────────────────\n");

            for (ShortcutInfo info : entry.getValue()) {
                sb.append(String.format("  %-15s  %s%s\n",
                        info.keyCombo,
                        info.description,
                        info.isGlobal ? "" : " (context)"));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    /**
     * Parse a key combination string (e.g., "Ctrl+K", "Ctrl+Shift+S")
     */
    private KeyCombination parseKeyCombination(String keyCombo) {
        try {
            String[] parts = keyCombo.split("\\+");
            List<KeyCombination.Modifier> modifiers = new ArrayList<>();
            KeyCode keyCode = null;

            for (String part : parts) {
                String trimmed = part.trim().toLowerCase();
                switch (trimmed) {
                    case "ctrl":
                    case "control":
                        modifiers.add(KeyCombination.CONTROL_DOWN);
                        break;
                    case "shift":
                        modifiers.add(KeyCombination.SHIFT_DOWN);
                        break;
                    case "alt":
                        modifiers.add(KeyCombination.ALT_DOWN);
                        break;
                    case "meta":
                    case "cmd":
                    case "command":
                        modifiers.add(KeyCombination.META_DOWN);
                        break;
                    default:
                        // Try to parse as KeyCode
                        keyCode = parseKeyCode(trimmed);
                        break;
                }
            }

            if (keyCode != null) {
                return new KeyCodeCombination(keyCode, modifiers.toArray(new KeyCombination.Modifier[0]));
            }
        } catch (Exception e) {
            log.warn("Failed to parse key combination: {}", keyCombo, e);
        }
        return null;
    }

    /**
     * Parse a key code from string
     */
    private KeyCode parseKeyCode(String key) {
        // Handle special cases
        switch (key.toLowerCase()) {
            case "escape":
            case "esc":
                return KeyCode.ESCAPE;
            case "enter":
            case "return":
                return KeyCode.ENTER;
            case "space":
                return KeyCode.SPACE;
            case "tab":
                return KeyCode.TAB;
            case "backspace":
                return KeyCode.BACK_SPACE;
            case "delete":
            case "del":
                return KeyCode.DELETE;
            case "up":
                return KeyCode.UP;
            case "down":
                return KeyCode.DOWN;
            case "left":
                return KeyCode.LEFT;
            case "right":
                return KeyCode.RIGHT;
            case "home":
                return KeyCode.HOME;
            case "end":
                return KeyCode.END;
            case "pageup":
                return KeyCode.PAGE_UP;
            case "pagedown":
                return KeyCode.PAGE_DOWN;
            default:
                // Try to find matching KeyCode
                try {
                    // Handle single letters and digits
                    if (key.length() == 1) {
                        char c = key.toUpperCase().charAt(0);
                        if (Character.isLetter(c)) {
                            return KeyCode.valueOf(String.valueOf(c));
                        } else if (Character.isDigit(c)) {
                            return KeyCode.valueOf("DIGIT" + c);
                        }
                    }
                    // Handle F keys
                    if (key.toLowerCase().startsWith("f") && key.length() <= 3) {
                        return KeyCode.valueOf(key.toUpperCase());
                    }
                    // Try direct match
                    return KeyCode.valueOf(key.toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("Unknown key code: {}", key);
                    return null;
                }
        }
    }

    /**
     * Format a KeyCombination to human-readable string
     */
    private String formatKeyCombination(KeyCombination combo) {
        StringBuilder sb = new StringBuilder();

        if (combo instanceof KeyCodeCombination) {
            KeyCodeCombination kcc = (KeyCodeCombination) combo;

            if (kcc.getControl() == KeyCombination.ModifierValue.DOWN) sb.append("Ctrl+");
            if (kcc.getShift() == KeyCombination.ModifierValue.DOWN) sb.append("Shift+");
            if (kcc.getAlt() == KeyCombination.ModifierValue.DOWN) sb.append("Alt+");
            if (kcc.getMeta() == KeyCombination.ModifierValue.DOWN) sb.append("Meta+");

            sb.append(kcc.getCode().getName());
        } else {
            sb.append(combo.getName());
        }

        return sb.toString();
    }

    /**
     * Check if a shortcut is already registered
     */
    public boolean isRegistered(String keyCombo) {
        KeyCombination combo = parseKeyCombination(keyCombo);
        if (combo == null) return false;

        if (globalShortcuts.containsKey(combo)) return true;

        Map<KeyCombination, ShortcutEntry> contextMap = contextShortcuts.get(currentContext);
        return contextMap != null && contextMap.containsKey(combo);
    }

    // ========================================================================
    // INNER CLASSES
    // ========================================================================

    /**
     * Internal shortcut entry
     */
    private static class ShortcutEntry {
        final String keyCombo;
        final Runnable action;
        final String category;
        final String description;
        final boolean isGlobal;

        ShortcutEntry(String keyCombo, Runnable action, String category, String description, boolean isGlobal) {
            this.keyCombo = keyCombo;
            this.action = action;
            this.category = category;
            this.description = description;
            this.isGlobal = isGlobal;
        }
    }

    /**
     * Public shortcut info for help display
     */
    public static class ShortcutInfo {
        public final String keyCombo;
        public final String description;
        public final boolean isGlobal;

        public ShortcutInfo(String keyCombo, String description, boolean isGlobal) {
            this.keyCombo = keyCombo;
            this.description = description;
            this.isGlobal = isGlobal;
        }
    }
}
