package com.heronix.ui.accessibility;

import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.prefs.Preferences;

/**
 * Theme Manager
 * Centralized theme and styling management with accessibility support.
 *
 * Features:
 * - Light, Dark, and High Contrast themes
 * - Custom color schemes
 * - WCAG-compliant color combinations
 * - Persistent theme preferences
 * - Dynamic theme switching
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class ThemeManager {

    // ========================================================================
    // SINGLETON
    // ========================================================================

    private static ThemeManager instance;

    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    // ========================================================================
    // PROPERTIES
    // ========================================================================

    @Getter
    private final ObjectProperty<Theme> currentTheme = new SimpleObjectProperty<>(Theme.LIGHT);

    @Getter
    private final ObjectProperty<AccentColor> accentColor = new SimpleObjectProperty<>(AccentColor.BLUE);

    @Getter
    private final DoubleProperty borderRadius = new SimpleDoubleProperty(8);

    @Getter
    private final BooleanProperty compactMode = new SimpleBooleanProperty(false);

    // Registered scenes for theme updates
    private final List<Scene> registeredScenes = new ArrayList<>();

    // Preferences for persistence
    private final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    private ThemeManager() {
        // Load saved preferences
        loadPreferences();

        // Listen for theme changes
        currentTheme.addListener((obs, oldTheme, newTheme) -> {
            applyThemeToAllScenes();
            savePreferences();
            log.info("Theme changed to: {}", newTheme);
        });

        accentColor.addListener((obs, oldColor, newColor) -> {
            applyThemeToAllScenes();
            savePreferences();
            log.info("Accent color changed to: {}", newColor);
        });

        compactMode.addListener((obs, wasCompact, isCompact) -> {
            applyThemeToAllScenes();
            savePreferences();
            log.info("Compact mode: {}", isCompact);
        });
    }

    // ========================================================================
    // SCENE REGISTRATION
    // ========================================================================

    /**
     * Register a scene for automatic theme updates
     */
    public void registerScene(Scene scene) {
        if (!registeredScenes.contains(scene)) {
            registeredScenes.add(scene);
            applyTheme(scene);
        }
    }

    /**
     * Unregister a scene
     */
    public void unregisterScene(Scene scene) {
        registeredScenes.remove(scene);
    }

    /**
     * Apply theme to all registered scenes
     */
    public void applyThemeToAllScenes() {
        for (Scene scene : registeredScenes) {
            applyTheme(scene);
        }
    }

    // ========================================================================
    // THEME APPLICATION
    // ========================================================================

    /**
     * Apply current theme to a scene
     */
    public void applyTheme(Scene scene) {
        if (scene == null) return;

        // Clear existing stylesheets
        scene.getStylesheets().clear();

        // Apply theme CSS
        String themeCss = generateThemeCss();

        // Create and apply stylesheet
        scene.getRoot().setStyle(themeCss);

        log.debug("Applied theme to scene: {}", currentTheme.get());
    }

    /**
     * Generate CSS for current theme
     */
    private String generateThemeCss() {
        ThemeColors colors = getThemeColors();
        String accent = getAccentColorHex();

        double radius = borderRadius.get();
        double spacing = compactMode.get() ? 8 : 12;
        double padding = compactMode.get() ? 8 : 16;
        double fontSize = compactMode.get() ? 12 : 14;

        return String.format("""
            -fx-base: %s;
            -fx-background: %s;
            -fx-control-inner-background: %s;
            -fx-text-fill: %s;
            -fx-accent: %s;
            -fx-focus-color: %s;
            -fx-faint-focus-color: %s22;
            """,
            colors.base,
            colors.background,
            colors.surface,
            colors.textPrimary,
            accent,
            accent,
            accent
        );
    }

    /**
     * Get colors for current theme
     */
    public ThemeColors getThemeColors() {
        return switch (currentTheme.get()) {
            case LIGHT -> new ThemeColors(
                "#FFFFFF",      // base
                "#F8FAFC",      // background
                "#FFFFFF",      // surface
                "#F1F5F9",      // surfaceVariant
                "#E2E8F0",      // border
                "#1E293B",      // textPrimary
                "#475569",      // textSecondary
                "#94A3B8",      // textMuted
                "#DC2626",      // error
                "#16A34A",      // success
                "#CA8A04",      // warning
                "#2563EB"       // info
            );
            case DARK -> new ThemeColors(
                "#0F172A",      // base
                "#1E293B",      // background
                "#334155",      // surface
                "#475569",      // surfaceVariant
                "#64748B",      // border
                "#F8FAFC",      // textPrimary
                "#CBD5E1",      // textSecondary
                "#94A3B8",      // textMuted
                "#F87171",      // error
                "#4ADE80",      // success
                "#FACC15",      // warning
                "#60A5FA"       // info
            );
            case HIGH_CONTRAST -> new ThemeColors(
                "#000000",      // base
                "#000000",      // background
                "#000000",      // surface
                "#1A1A1A",      // surfaceVariant
                "#FFFFFF",      // border
                "#FFFFFF",      // textPrimary
                "#FFFFFF",      // textSecondary
                "#CCCCCC",      // textMuted
                "#FF6B6B",      // error
                "#51CF66",      // success
                "#FFD43B",      // warning
                "#74C0FC"       // info
            );
            case HIGH_CONTRAST_LIGHT -> new ThemeColors(
                "#FFFFFF",      // base
                "#FFFFFF",      // background
                "#FFFFFF",      // surface
                "#F0F0F0",      // surfaceVariant
                "#000000",      // border
                "#000000",      // textPrimary
                "#000000",      // textSecondary
                "#333333",      // textMuted
                "#B91C1C",      // error
                "#166534",      // success
                "#A16207",      // warning
                "#1D4ED8"       // info
            );
        };
    }

    /**
     * Get accent color hex value
     */
    public String getAccentColorHex() {
        return switch (accentColor.get()) {
            case BLUE -> currentTheme.get().isDark() ? "#60A5FA" : "#3B82F6";
            case GREEN -> currentTheme.get().isDark() ? "#4ADE80" : "#22C55E";
            case PURPLE -> currentTheme.get().isDark() ? "#A78BFA" : "#8B5CF6";
            case ORANGE -> currentTheme.get().isDark() ? "#FB923C" : "#F97316";
            case RED -> currentTheme.get().isDark() ? "#F87171" : "#EF4444";
            case TEAL -> currentTheme.get().isDark() ? "#2DD4BF" : "#14B8A6";
            case PINK -> currentTheme.get().isDark() ? "#F472B6" : "#EC4899";
            case YELLOW -> currentTheme.get().isDark() ? "#FACC15" : "#EAB308";
        };
    }

    // ========================================================================
    // CSS GENERATION
    // ========================================================================

    /**
     * Generate component-specific CSS
     */
    public String getButtonCss(ButtonStyle style) {
        ThemeColors colors = getThemeColors();
        String accent = getAccentColorHex();
        double radius = borderRadius.get();

        return switch (style) {
            case PRIMARY -> String.format("""
                -fx-background-color: %s;
                -fx-text-fill: white;
                -fx-font-weight: 600;
                -fx-padding: 10 20;
                -fx-background-radius: %.0f;
                -fx-cursor: hand;
                """, accent, radius);
            case SECONDARY -> String.format("""
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-font-weight: 500;
                -fx-padding: 10 20;
                -fx-background-radius: %.0f;
                -fx-cursor: hand;
                """, colors.surfaceVariant, colors.textPrimary, radius);
            case OUTLINE -> String.format("""
                -fx-background-color: transparent;
                -fx-text-fill: %s;
                -fx-border-color: %s;
                -fx-border-width: 1;
                -fx-border-radius: %.0f;
                -fx-padding: 9 19;
                -fx-cursor: hand;
                """, accent, colors.border, radius);
            case GHOST -> String.format("""
                -fx-background-color: transparent;
                -fx-text-fill: %s;
                -fx-padding: 10 20;
                -fx-cursor: hand;
                """, colors.textSecondary);
            case DANGER -> String.format("""
                -fx-background-color: %s;
                -fx-text-fill: white;
                -fx-font-weight: 600;
                -fx-padding: 10 20;
                -fx-background-radius: %.0f;
                -fx-cursor: hand;
                """, colors.error, radius);
        };
    }

    /**
     * Generate card CSS
     */
    public String getCardCss() {
        ThemeColors colors = getThemeColors();
        double radius = borderRadius.get();

        String shadow = currentTheme.get().isDark()
            ? "dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 2)"
            : "dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2)";

        return String.format("""
            -fx-background-color: %s;
            -fx-background-radius: %.0f;
            -fx-border-color: %s;
            -fx-border-radius: %.0f;
            -fx-effect: %s;
            """, colors.surface, radius, colors.border, radius, shadow);
    }

    /**
     * Generate input field CSS
     */
    public String getInputCss() {
        ThemeColors colors = getThemeColors();
        double radius = borderRadius.get();

        return String.format("""
            -fx-background-color: %s;
            -fx-text-fill: %s;
            -fx-prompt-text-fill: %s;
            -fx-background-radius: %.0f;
            -fx-border-color: %s;
            -fx-border-radius: %.0f;
            -fx-padding: 10 12;
            """, colors.surface, colors.textPrimary, colors.textMuted,
            radius, colors.border, radius);
    }

    /**
     * Generate table CSS
     */
    public String getTableCss() {
        ThemeColors colors = getThemeColors();

        return String.format("""
            -fx-background-color: %s;
            -fx-table-cell-border-color: %s;
            """, colors.surface, colors.border);
    }

    /**
     * Generate sidebar CSS
     */
    public String getSidebarCss() {
        ThemeColors colors = getThemeColors();

        return String.format("""
            -fx-background-color: %s;
            -fx-border-color: %s;
            -fx-border-width: 0 1 0 0;
            """, colors.base, colors.border);
    }

    /**
     * Generate header CSS
     */
    public String getHeaderCss() {
        ThemeColors colors = getThemeColors();

        return String.format("""
            -fx-background-color: %s;
            -fx-border-color: %s;
            -fx-border-width: 0 0 1 0;
            """, colors.base, colors.border);
    }

    // ========================================================================
    // STATUS COLORS
    // ========================================================================

    /**
     * Get status badge CSS
     */
    public String getStatusBadgeCss(StatusType status) {
        ThemeColors colors = getThemeColors();

        String bgColor, textColor;
        switch (status) {
            case SUCCESS -> {
                bgColor = currentTheme.get().isDark() ? "#166534" : "#DCFCE7";
                textColor = currentTheme.get().isDark() ? "#4ADE80" : "#166534";
            }
            case ERROR -> {
                bgColor = currentTheme.get().isDark() ? "#7F1D1D" : "#FEE2E2";
                textColor = currentTheme.get().isDark() ? "#F87171" : "#DC2626";
            }
            case WARNING -> {
                bgColor = currentTheme.get().isDark() ? "#78350F" : "#FEF3C7";
                textColor = currentTheme.get().isDark() ? "#FACC15" : "#CA8A04";
            }
            case INFO -> {
                bgColor = currentTheme.get().isDark() ? "#1E3A8A" : "#DBEAFE";
                textColor = currentTheme.get().isDark() ? "#60A5FA" : "#2563EB";
            }
            default -> {
                bgColor = colors.surfaceVariant;
                textColor = colors.textSecondary;
            }
        }

        return String.format("""
            -fx-background-color: %s;
            -fx-text-fill: %s;
            -fx-font-size: 11px;
            -fx-font-weight: 600;
            -fx-padding: 4 10;
            -fx-background-radius: 12;
            """, bgColor, textColor);
    }

    // ========================================================================
    // PERSISTENCE
    // ========================================================================

    private void loadPreferences() {
        try {
            String themeName = prefs.get("theme", "LIGHT");
            currentTheme.set(Theme.valueOf(themeName));

            String accentName = prefs.get("accentColor", "BLUE");
            accentColor.set(AccentColor.valueOf(accentName));

            borderRadius.set(prefs.getDouble("borderRadius", 8));
            compactMode.set(prefs.getBoolean("compactMode", false));

            log.info("Loaded theme preferences: theme={}, accent={}", themeName, accentName);
        } catch (Exception e) {
            log.warn("Failed to load theme preferences, using defaults", e);
        }
    }

    private void savePreferences() {
        try {
            prefs.put("theme", currentTheme.get().name());
            prefs.put("accentColor", accentColor.get().name());
            prefs.putDouble("borderRadius", borderRadius.get());
            prefs.putBoolean("compactMode", compactMode.get());
            prefs.flush();
        } catch (Exception e) {
            log.warn("Failed to save theme preferences", e);
        }
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    /**
     * Cycle to next theme
     */
    public void cycleTheme() {
        Theme[] themes = Theme.values();
        int nextIndex = (currentTheme.get().ordinal() + 1) % themes.length;
        currentTheme.set(themes[nextIndex]);
    }

    /**
     * Toggle between light and dark
     */
    public void toggleDarkMode() {
        if (currentTheme.get() == Theme.LIGHT) {
            currentTheme.set(Theme.DARK);
        } else if (currentTheme.get() == Theme.DARK) {
            currentTheme.set(Theme.LIGHT);
        }
    }

    /**
     * Check if current theme is dark
     */
    public boolean isDarkMode() {
        return currentTheme.get().isDark();
    }

    // ========================================================================
    // DATA CLASSES
    // ========================================================================

    @Getter
    public static class ThemeColors {
        private final String base;
        private final String background;
        private final String surface;
        private final String surfaceVariant;
        private final String border;
        private final String textPrimary;
        private final String textSecondary;
        private final String textMuted;
        private final String error;
        private final String success;
        private final String warning;
        private final String info;

        public ThemeColors(String base, String background, String surface, String surfaceVariant,
                          String border, String textPrimary, String textSecondary, String textMuted,
                          String error, String success, String warning, String info) {
            this.base = base;
            this.background = background;
            this.surface = surface;
            this.surfaceVariant = surfaceVariant;
            this.border = border;
            this.textPrimary = textPrimary;
            this.textSecondary = textSecondary;
            this.textMuted = textMuted;
            this.error = error;
            this.success = success;
            this.warning = warning;
            this.info = info;
        }
    }

    // ========================================================================
    // ENUMS
    // ========================================================================

    public enum Theme {
        LIGHT(false, "Light"),
        DARK(true, "Dark"),
        HIGH_CONTRAST(true, "High Contrast (Dark)"),
        HIGH_CONTRAST_LIGHT(false, "High Contrast (Light)");

        @Getter
        private final boolean dark;
        @Getter
        private final String displayName;

        Theme(boolean dark, String displayName) {
            this.dark = dark;
            this.displayName = displayName;
        }
    }

    public enum AccentColor {
        BLUE("Blue", "#3B82F6"),
        GREEN("Green", "#22C55E"),
        PURPLE("Purple", "#8B5CF6"),
        ORANGE("Orange", "#F97316"),
        RED("Red", "#EF4444"),
        TEAL("Teal", "#14B8A6"),
        PINK("Pink", "#EC4899"),
        YELLOW("Yellow", "#EAB308");

        @Getter
        private final String displayName;
        @Getter
        private final String hex;

        AccentColor(String displayName, String hex) {
            this.displayName = displayName;
            this.hex = hex;
        }
    }

    public enum ButtonStyle {
        PRIMARY, SECONDARY, OUTLINE, GHOST, DANGER
    }

    public enum StatusType {
        SUCCESS, ERROR, WARNING, INFO, NEUTRAL
    }
}
