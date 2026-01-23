package com.heronix.ui.accessibility;

import javafx.beans.property.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 * User Preferences View
 * Comprehensive settings panel for accessibility and display preferences.
 *
 * Features:
 * - Theme selection (Light/Dark/High Contrast)
 * - Font size scaling
 * - Reduced motion toggle
 * - Focus indicator styles
 * - Keyboard shortcut customization
 * - Screen reader mode
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class UserPreferencesView extends BorderPane {

    // ========================================================================
    // MANAGERS
    // ========================================================================

    private final AccessibilityManager accessibilityManager = AccessibilityManager.getInstance();
    private final ThemeManager themeManager = ThemeManager.getInstance();
    private final ScreenReaderSupport screenReaderSupport = ScreenReaderSupport.getInstance();

    // ========================================================================
    // CALLBACKS
    // ========================================================================

    private Consumer<String> onPreferenceChanged;
    private Runnable onClose;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public UserPreferencesView() {
        getStyleClass().add("user-preferences");
        setStyle("-fx-background-color: #F8FAFC;");

        // Header
        setTop(createHeader());

        // Main content with tabs
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setSide(Side.LEFT);
        tabs.setStyle("-fx-tab-min-width: 150;");

        Tab appearanceTab = new Tab("Appearance", createAppearancePane());
        appearanceTab.setGraphic(createTabIcon("🎨"));

        Tab accessibilityTab = new Tab("Accessibility", createAccessibilityPane());
        accessibilityTab.setGraphic(createTabIcon("♿"));

        Tab keyboardTab = new Tab("Keyboard", createKeyboardPane());
        keyboardTab.setGraphic(createTabIcon("⌨"));

        Tab notificationsTab = new Tab("Notifications", createNotificationsPane());
        notificationsTab.setGraphic(createTabIcon("🔔"));

        Tab languageTab = new Tab("Language", createLanguagePane());
        languageTab.setGraphic(createTabIcon("🌐"));

        tabs.getTabs().addAll(appearanceTab, accessibilityTab, keyboardTab, notificationsTab, languageTab);

        setCenter(tabs);
    }

    // ========================================================================
    // HEADER
    // ========================================================================

    private HBox createHeader() {
        HBox header = new HBox(16);
        header.setPadding(new Insets(20, 24, 16, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        Label title = new Label("Preferences");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #1E293B;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button resetBtn = new Button("Reset to Defaults");
        resetBtn.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #64748B;
            -fx-font-size: 13px;
            -fx-cursor: hand;
            """);
        resetBtn.setOnAction(e -> resetToDefaults());

        Button closeBtn = new Button("✕");
        closeBtn.setStyle("""
            -fx-background-color: transparent;
            -fx-font-size: 18px;
            -fx-text-fill: #64748B;
            -fx-cursor: hand;
            """);
        closeBtn.setOnAction(e -> {
            if (onClose != null) onClose.run();
        });

        header.getChildren().addAll(title, spacer, resetBtn, closeBtn);
        return header;
    }

    private Label createTabIcon(String icon) {
        Label label = new Label(icon);
        label.setStyle("-fx-font-size: 16px;");
        return label;
    }

    // ========================================================================
    // APPEARANCE PANE
    // ========================================================================

    private ScrollPane createAppearancePane() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        VBox content = new VBox(24);
        content.setPadding(new Insets(24));

        // Theme section
        VBox themeSection = createSection("Theme", "Choose your preferred color scheme");

        HBox themeOptions = new HBox(16);
        themeOptions.setAlignment(Pos.CENTER_LEFT);

        for (ThemeManager.Theme theme : ThemeManager.Theme.values()) {
            VBox themeCard = createThemeCard(theme);
            themeOptions.getChildren().add(themeCard);
        }

        themeSection.getChildren().add(themeOptions);

        // Accent color section
        VBox accentSection = createSection("Accent Color", "Customize your interface accent color");

        FlowPane colorOptions = new FlowPane(12, 12);
        for (ThemeManager.AccentColor color : ThemeManager.AccentColor.values()) {
            colorOptions.getChildren().add(createColorOption(color));
        }

        accentSection.getChildren().add(colorOptions);

        // Border radius section
        VBox radiusSection = createSection("Corner Roundness", "Adjust the roundness of interface elements");

        HBox radiusOptions = new HBox(16);
        radiusOptions.setAlignment(Pos.CENTER_LEFT);

        Slider radiusSlider = new Slider(0, 16, themeManager.getBorderRadius().get());
        radiusSlider.setShowTickLabels(true);
        radiusSlider.setShowTickMarks(true);
        radiusSlider.setMajorTickUnit(4);
        radiusSlider.setMinorTickCount(1);
        radiusSlider.setPrefWidth(300);

        Label radiusPreview = new Label();
        radiusPreview.setPrefSize(60, 40);
        radiusPreview.setStyle(String.format("""
            -fx-background-color: #3B82F6;
            -fx-background-radius: %.0f;
            """, radiusSlider.getValue()));

        radiusSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            themeManager.getBorderRadius().set(newVal.doubleValue());
            radiusPreview.setStyle(String.format("""
                -fx-background-color: #3B82F6;
                -fx-background-radius: %.0f;
                """, newVal.doubleValue()));
            notifyPreferenceChanged("borderRadius");
        });

        radiusOptions.getChildren().addAll(radiusSlider, radiusPreview);
        radiusSection.getChildren().add(radiusOptions);

        // Compact mode section
        VBox compactSection = createSection("Display Density", "Adjust spacing and padding");

        HBox compactOptions = new HBox(24);
        compactOptions.setAlignment(Pos.CENTER_LEFT);

        ToggleGroup densityGroup = new ToggleGroup();

        RadioButton comfortableBtn = new RadioButton("Comfortable");
        comfortableBtn.setToggleGroup(densityGroup);
        comfortableBtn.setSelected(!themeManager.getCompactMode().get());
        comfortableBtn.setStyle("-fx-font-size: 13px;");

        RadioButton compactBtn = new RadioButton("Compact");
        compactBtn.setToggleGroup(densityGroup);
        compactBtn.setSelected(themeManager.getCompactMode().get());
        compactBtn.setStyle("-fx-font-size: 13px;");

        densityGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            boolean isCompact = newVal == compactBtn;
            themeManager.getCompactMode().set(isCompact);
            notifyPreferenceChanged("compactMode");
        });

        compactOptions.getChildren().addAll(comfortableBtn, compactBtn);
        compactSection.getChildren().add(compactOptions);

        content.getChildren().addAll(themeSection, accentSection, radiusSection, compactSection);
        scroll.setContent(content);
        return scroll;
    }

    private VBox createThemeCard(ThemeManager.Theme theme) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(12));
        card.setPrefWidth(120);
        card.setCursor(javafx.scene.Cursor.HAND);

        // Theme preview
        VBox preview = new VBox(4);
        preview.setPrefSize(100, 70);
        preview.setPadding(new Insets(8));

        String bgColor, headerColor, textColor;
        if (theme.isDark()) {
            bgColor = "#1E293B";
            headerColor = "#334155";
            textColor = "#F8FAFC";
        } else {
            bgColor = "#FFFFFF";
            headerColor = "#F1F5F9";
            textColor = "#1E293B";
        }

        if (theme == ThemeManager.Theme.HIGH_CONTRAST) {
            bgColor = "#000000";
            headerColor = "#1A1A1A";
            textColor = "#FFFFFF";
        } else if (theme == ThemeManager.Theme.HIGH_CONTRAST_LIGHT) {
            bgColor = "#FFFFFF";
            headerColor = "#F0F0F0";
            textColor = "#000000";
        }

        preview.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 8;
            -fx-border-color: #E2E8F0;
            -fx-border-radius: 8;
            """, bgColor));

        Region header = new Region();
        header.setPrefHeight(16);
        header.setStyle("-fx-background-color: " + headerColor + "; -fx-background-radius: 4;");

        Region content1 = new Region();
        content1.setPrefHeight(8);
        content1.setMaxWidth(60);
        content1.setStyle("-fx-background-color: " + textColor + "40; -fx-background-radius: 2;");

        Region content2 = new Region();
        content2.setPrefHeight(8);
        content2.setMaxWidth(80);
        content2.setStyle("-fx-background-color: " + textColor + "40; -fx-background-radius: 2;");

        preview.getChildren().addAll(header, content1, content2);

        Label name = new Label(theme.getDisplayName());
        name.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");

        // Selection indicator
        boolean isSelected = themeManager.getCurrentTheme().get() == theme;
        updateThemeCardStyle(card, isSelected);

        card.setOnMouseClicked(e -> {
            themeManager.getCurrentTheme().set(theme);
            notifyPreferenceChanged("theme");
        });

        // Listen for theme changes to update selection
        themeManager.getCurrentTheme().addListener((obs, oldVal, newVal) -> {
            updateThemeCardStyle(card, newVal == theme);
        });

        card.getChildren().addAll(preview, name);
        return card;
    }

    private void updateThemeCardStyle(VBox card, boolean selected) {
        card.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 8;
            -fx-border-color: %s;
            -fx-border-radius: 8;
            -fx-border-width: 2;
            """,
            selected ? "#EFF6FF" : "transparent",
            selected ? "#3B82F6" : "transparent"
        ));
    }

    private StackPane createColorOption(ThemeManager.AccentColor color) {
        StackPane option = new StackPane();
        option.setPrefSize(40, 40);
        option.setCursor(javafx.scene.Cursor.HAND);

        Circle circle = new Circle(16);
        circle.setFill(Color.web(color.getHex()));

        Circle checkmark = new Circle(6);
        checkmark.setFill(Color.WHITE);
        checkmark.setVisible(themeManager.getAccentColor().get() == color);

        option.getChildren().addAll(circle, checkmark);

        option.setOnMouseClicked(e -> {
            themeManager.getAccentColor().set(color);
            notifyPreferenceChanged("accentColor");
        });

        themeManager.getAccentColor().addListener((obs, oldVal, newVal) -> {
            checkmark.setVisible(newVal == color);
        });

        Tooltip.install(option, new Tooltip(color.getDisplayName()));

        return option;
    }

    // ========================================================================
    // ACCESSIBILITY PANE
    // ========================================================================

    private ScrollPane createAccessibilityPane() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        VBox content = new VBox(24);
        content.setPadding(new Insets(24));

        // Screen reader section
        VBox screenReaderSection = createSection("Screen Reader", "Optimize for assistive technology");

        CheckBox screenReaderCheck = new CheckBox("Enable screen reader optimizations");
        screenReaderCheck.setSelected(accessibilityManager.getScreenReaderMode().get());
        screenReaderCheck.setStyle("-fx-font-size: 13px;");
        screenReaderCheck.selectedProperty().bindBidirectional(accessibilityManager.getScreenReaderMode());

        Label screenReaderHint = new Label("Enables additional announcements and ARIA-like labels for screen readers");
        screenReaderHint.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B; -fx-wrap-text: true;");
        screenReaderHint.setWrapText(true);

        screenReaderSection.getChildren().addAll(screenReaderCheck, screenReaderHint);

        // Focus indicators section
        VBox focusSection = createSection("Focus Indicators", "Customize keyboard focus visibility");

        CheckBox focusCheck = new CheckBox("Show focus indicators");
        focusCheck.setSelected(accessibilityManager.getFocusIndicatorsEnabled().get());
        focusCheck.setStyle("-fx-font-size: 13px;");
        focusCheck.selectedProperty().bindBidirectional(accessibilityManager.getFocusIndicatorsEnabled());

        VBox focusStyleBox = new VBox(8);
        focusStyleBox.setPadding(new Insets(12, 0, 0, 24));

        Label focusStyleLabel = new Label("Focus style:");
        focusStyleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        HBox focusStyles = new HBox(12);
        ToggleGroup focusGroup = new ToggleGroup();

        for (AccessibilityManager.FocusStyle style : AccessibilityManager.FocusStyle.values()) {
            RadioButton btn = new RadioButton(formatEnumName(style.name()));
            btn.setToggleGroup(focusGroup);
            btn.setSelected(accessibilityManager.getFocusStyle().get() == style);
            btn.setStyle("-fx-font-size: 12px;");
            btn.setUserData(style);
            focusStyles.getChildren().add(btn);
        }

        focusGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                accessibilityManager.getFocusStyle().set((AccessibilityManager.FocusStyle) newVal.getUserData());
                notifyPreferenceChanged("focusStyle");
            }
        });

        focusStyleBox.getChildren().addAll(focusStyleLabel, focusStyles);
        focusSection.getChildren().addAll(focusCheck, focusStyleBox);

        // Motion section
        VBox motionSection = createSection("Motion", "Control animations and transitions");

        CheckBox reducedMotionCheck = new CheckBox("Reduce motion");
        reducedMotionCheck.setSelected(accessibilityManager.getReducedMotion().get());
        reducedMotionCheck.setStyle("-fx-font-size: 13px;");
        reducedMotionCheck.selectedProperty().bindBidirectional(accessibilityManager.getReducedMotion());

        Label motionHint = new Label("Minimizes animations for users sensitive to motion");
        motionHint.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

        motionSection.getChildren().addAll(reducedMotionCheck, motionHint);

        // Font size section
        VBox fontSection = createSection("Text Size", "Adjust text size throughout the application");

        HBox fontSizeRow = new HBox(16);
        fontSizeRow.setAlignment(Pos.CENTER_LEFT);

        Label smallLabel = new Label("A");
        smallLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        Slider fontSlider = new Slider(0.75, 1.5, accessibilityManager.getFontScale().get());
        fontSlider.setShowTickLabels(true);
        fontSlider.setShowTickMarks(true);
        fontSlider.setMajorTickUnit(0.25);
        fontSlider.setPrefWidth(250);

        Label largeLabel = new Label("A");
        largeLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #64748B;");

        Label fontPreview = new Label("Preview Text");
        fontPreview.setStyle(String.format("-fx-font-size: %.0fpx;", 14 * fontSlider.getValue()));

        fontSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            accessibilityManager.getFontScale().set(newVal.doubleValue());
            fontPreview.setStyle(String.format("-fx-font-size: %.0fpx;", 14 * newVal.doubleValue()));
            notifyPreferenceChanged("fontScale");
        });

        fontSizeRow.getChildren().addAll(smallLabel, fontSlider, largeLabel, fontPreview);
        fontSection.getChildren().add(fontSizeRow);

        // High contrast section
        VBox contrastSection = createSection("High Contrast", "Enhanced contrast for better visibility");

        CheckBox highContrastCheck = new CheckBox("Enable high contrast mode");
        highContrastCheck.setSelected(accessibilityManager.getHighContrastMode().get());
        highContrastCheck.setStyle("-fx-font-size: 13px;");
        highContrastCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            accessibilityManager.getHighContrastMode().set(newVal);
            if (newVal) {
                themeManager.getCurrentTheme().set(ThemeManager.Theme.HIGH_CONTRAST);
            } else {
                themeManager.getCurrentTheme().set(ThemeManager.Theme.LIGHT);
            }
            notifyPreferenceChanged("highContrast");
        });

        contrastSection.getChildren().add(highContrastCheck);

        content.getChildren().addAll(screenReaderSection, focusSection, motionSection, fontSection, contrastSection);
        scroll.setContent(content);
        return scroll;
    }

    // ========================================================================
    // KEYBOARD PANE
    // ========================================================================

    private ScrollPane createKeyboardPane() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        VBox content = new VBox(24);
        content.setPadding(new Insets(24));

        // Shortcuts reference
        VBox shortcutsSection = createSection("Keyboard Shortcuts", "Quick reference for keyboard navigation");

        VBox keyboardHelp = KeyboardNavigationHelper.getInstance().createShortcutHelp();
        VBox shortcutsList = keyboardHelp != null ? keyboardHelp : createDefaultShortcutsList();

        shortcutsSection.getChildren().add(shortcutsList);

        content.getChildren().add(shortcutsSection);
        scroll.setContent(content);
        return scroll;
    }

    private VBox createDefaultShortcutsList() {
        VBox list = new VBox(8);
        list.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 16;");

        String[][] shortcuts = {
            {"Alt + 1-5", "Navigate to main sections"},
            {"Alt + S", "Open search"},
            {"Alt + N", "Create new record"},
            {"Ctrl + S", "Save current form"},
            {"Ctrl + P", "Print current view"},
            {"Alt + T", "Toggle dark mode"},
            {"Alt + H", "Show keyboard help"},
            {"Escape", "Close dialog / Cancel"},
            {"Tab / Shift+Tab", "Navigate between fields"},
            {"Arrow Keys", "Navigate in lists and grids"},
            {"Enter / Space", "Activate button / Select item"},
            {"Home / End", "Jump to first / last item"}
        };

        for (String[] shortcut : shortcuts) {
            HBox row = new HBox(16);
            row.setAlignment(Pos.CENTER_LEFT);

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
            list.getChildren().add(row);
        }

        return list;
    }

    // ========================================================================
    // NOTIFICATIONS PANE
    // ========================================================================

    private ScrollPane createNotificationsPane() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        VBox content = new VBox(24);
        content.setPadding(new Insets(24));

        // Notification preferences
        VBox notifySection = createSection("Notifications", "Configure how you receive notifications");

        CheckBox desktopNotify = new CheckBox("Show desktop notifications");
        desktopNotify.setSelected(true);
        desktopNotify.setStyle("-fx-font-size: 13px;");

        CheckBox soundNotify = new CheckBox("Play notification sounds");
        soundNotify.setSelected(true);
        soundNotify.setStyle("-fx-font-size: 13px;");

        CheckBox badgeNotify = new CheckBox("Show badge counts");
        badgeNotify.setSelected(true);
        badgeNotify.setStyle("-fx-font-size: 13px;");

        notifySection.getChildren().addAll(desktopNotify, soundNotify, badgeNotify);

        // Email preferences
        VBox emailSection = createSection("Email Notifications", "Control email alerts");

        CheckBox emailGrades = new CheckBox("Grade updates");
        emailGrades.setSelected(true);
        emailGrades.setStyle("-fx-font-size: 13px;");

        CheckBox emailAttendance = new CheckBox("Attendance alerts");
        emailAttendance.setSelected(true);
        emailAttendance.setStyle("-fx-font-size: 13px;");

        CheckBox emailMessages = new CheckBox("New messages");
        emailMessages.setSelected(true);
        emailMessages.setStyle("-fx-font-size: 13px;");

        CheckBox emailAnnouncements = new CheckBox("School announcements");
        emailAnnouncements.setSelected(false);
        emailAnnouncements.setStyle("-fx-font-size: 13px;");

        emailSection.getChildren().addAll(emailGrades, emailAttendance, emailMessages, emailAnnouncements);

        content.getChildren().addAll(notifySection, emailSection);
        scroll.setContent(content);
        return scroll;
    }

    // ========================================================================
    // LANGUAGE PANE
    // ========================================================================

    private ScrollPane createLanguagePane() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        VBox content = new VBox(24);
        content.setPadding(new Insets(24));

        // Language selection
        VBox languageSection = createSection("Language", "Choose your preferred language");

        ComboBox<String> languageCombo = new ComboBox<>();
        languageCombo.getItems().addAll(
            "English (US)",
            "English (UK)",
            "Español",
            "Français",
            "Deutsch",
            "中文 (简体)",
            "日本語",
            "한국어",
            "Português",
            "العربية"
        );
        languageCombo.setValue("English (US)");
        languageCombo.setPrefWidth(250);
        languageCombo.setStyle("-fx-font-size: 13px;");

        languageSection.getChildren().add(languageCombo);

        // Date/time format
        VBox dateSection = createSection("Date & Time Format", "Configure date and time display");

        HBox dateRow = new HBox(16);
        dateRow.setAlignment(Pos.CENTER_LEFT);

        Label dateLabel = new Label("Date format:");
        dateLabel.setStyle("-fx-font-size: 13px;");
        dateLabel.setMinWidth(100);

        ComboBox<String> dateCombo = new ComboBox<>();
        dateCombo.getItems().addAll("MM/DD/YYYY", "DD/MM/YYYY", "YYYY-MM-DD");
        dateCombo.setValue("MM/DD/YYYY");
        dateCombo.setStyle("-fx-font-size: 13px;");

        dateRow.getChildren().addAll(dateLabel, dateCombo);

        HBox timeRow = new HBox(16);
        timeRow.setAlignment(Pos.CENTER_LEFT);

        Label timeLabel = new Label("Time format:");
        timeLabel.setStyle("-fx-font-size: 13px;");
        timeLabel.setMinWidth(100);

        ComboBox<String> timeCombo = new ComboBox<>();
        timeCombo.getItems().addAll("12-hour (AM/PM)", "24-hour");
        timeCombo.setValue("12-hour (AM/PM)");
        timeCombo.setStyle("-fx-font-size: 13px;");

        timeRow.getChildren().addAll(timeLabel, timeCombo);

        dateSection.getChildren().addAll(dateRow, timeRow);

        content.getChildren().addAll(languageSection, dateSection);
        scroll.setContent(content);
        return scroll;
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    private VBox createSection(String title, String description) {
        VBox section = new VBox(12);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #1E293B;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B;");

        section.getChildren().addAll(titleLabel, descLabel);
        return section;
    }

    private String formatEnumName(String name) {
        return name.substring(0, 1).toUpperCase() +
               name.substring(1).toLowerCase().replace("_", " ");
    }

    private void resetToDefaults() {
        themeManager.getCurrentTheme().set(ThemeManager.Theme.LIGHT);
        themeManager.getAccentColor().set(ThemeManager.AccentColor.BLUE);
        themeManager.getBorderRadius().set(8);
        themeManager.getCompactMode().set(false);

        accessibilityManager.getScreenReaderMode().set(false);
        accessibilityManager.getFocusIndicatorsEnabled().set(true);
        accessibilityManager.getFocusStyle().set(AccessibilityManager.FocusStyle.OUTLINE);
        accessibilityManager.getReducedMotion().set(false);
        accessibilityManager.getFontScale().set(1.0);
        accessibilityManager.getHighContrastMode().set(false);

        notifyPreferenceChanged("reset");
        log.info("Preferences reset to defaults");
    }

    private void notifyPreferenceChanged(String preference) {
        if (onPreferenceChanged != null) {
            onPreferenceChanged.accept(preference);
        }
    }

    // ========================================================================
    // CALLBACKS
    // ========================================================================

    public void setOnPreferenceChanged(Consumer<String> callback) {
        this.onPreferenceChanged = callback;
    }

    public void setOnClose(Runnable callback) {
        this.onClose = callback;
    }
}
