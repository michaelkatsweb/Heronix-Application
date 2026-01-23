// Location: src/main/java/com/heronix/ui/controller/SettingsController.java
package com.heronix.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Settings Controller
 * Location:
 * src/main/java/com/heronix/ui/controller/SettingsController.java
 * 
 * Manages application settings and preferences
 */
@Slf4j
@Component
public class SettingsController {

    // ========== General Settings ==========
    @FXML
    private TextField schoolNameField;

    @FXML
    private TextField schoolYearField;

    @FXML
    private ComboBox<String> themeCombo;

    @FXML
    private CheckBox autoSaveCheck;

    @FXML
    private Spinner<Integer> autoSaveIntervalSpinner;

    // ========== Schedule Settings ==========
    @FXML
    private ComboBox<String> scheduleTypeCombo;

    @FXML
    private Spinner<Integer> periodsPerDaySpinner;

    @FXML
    private Spinner<Integer> periodDurationSpinner;

    @FXML
    private TextField startTimeField;

    @FXML
    private TextField endTimeField;

    @FXML
    private Spinner<Integer> breakDurationSpinner;

    // ========== Optimization Settings ==========
    @FXML
    private Slider optimizationTimeSlider;

    @FXML
    private Label optimizationTimeLabel;

    @FXML
    private CheckBox applyLeanCheck;

    @FXML
    private CheckBox applyKanbanCheck;

    @FXML
    private CheckBox applyEisenhowerCheck;

    @FXML
    private Spinner<Integer> maxConsecutiveHoursSpinner;

    @FXML
    private Spinner<Integer> minBreakMinutesSpinner;

    // ========== Database Settings ==========
    @FXML
    private TextField databasePathField;

    @FXML
    private Button browseDatabaseButton;

    @FXML
    private CheckBox autoBackupCheck;

    @FXML
    private TextField backupPathField;

    @FXML
    private Button browseBackupButton;

    @FXML
    private ComboBox<String> backupFrequencyCombo;

    // ========== Import/Export Settings ==========
    @FXML
    private TextField importPathField;

    @FXML
    private TextField exportPathField;

    @FXML
    private ComboBox<String> defaultExportFormatCombo;

    @FXML
    private CheckBox autoImportCheck;

    @Value("${heronix.environment:dev}")
    private String environment;

    @Autowired(required = false)
    private DataSource dataSource;

    private Stage dialogStage;
    private Properties settings;

    /**
     * Initialize controller
     */
    @FXML
    public void initialize() {
        log.info("Initializing SettingsController...");

        settings = new Properties();
        loadSettings();
        setupControls();
        populateSettings();

        log.info("SettingsController initialized");
    }

    /**
     * Set dialog stage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Setup control bindings and listeners
     */
    private void setupControls() {
        // Theme combo with live preview
        themeCombo.getItems().addAll("Dark", "Light", "System");
        themeCombo.setValue("Dark");

        // Add listener for immediate theme preview
        themeCombo.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                log.info("Theme changed from '{}' to '{}'", oldValue, newValue);
                applyTheme(newValue);

                // Auto-save theme preference immediately
                settings.setProperty("theme", newValue);
                try {
                    File configDir = new File("config");
                    if (!configDir.exists()) {
                        configDir.mkdirs();
                    }
                    try (FileOutputStream fos = new FileOutputStream("config/app-settings.properties")) {
                        settings.store(fos, "Heronix Scheduler Settings - Theme updated");
                    }
                    log.info("Theme preference saved: {}", newValue);
                } catch (Exception e) {
                    log.error("Failed to save theme preference", e);
                }
            }
        });

        // Schedule type combo
        scheduleTypeCombo.getItems().addAll(
                "Traditional", "Block", "Rotating", "Modular", "Flex-Mod");
        scheduleTypeCombo.setValue("Traditional");

        // Spinners
        periodsPerDaySpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(4, 12, 7));
        periodDurationSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(30, 120, 50, 5));
        breakDurationSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 30, 10));
        autoSaveIntervalSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60, 5));
        maxConsecutiveHoursSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 8, 3));
        minBreakMinutesSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 60, 15));

        // Optimization time slider
        optimizationTimeSlider.setMin(1);
        optimizationTimeSlider.setMax(30);
        optimizationTimeSlider.setValue(5);
        optimizationTimeSlider.setMajorTickUnit(5);
        optimizationTimeSlider.setMinorTickCount(4);
        optimizationTimeSlider.setShowTickLabels(true);
        optimizationTimeSlider.setShowTickMarks(true);
        optimizationTimeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            optimizationTimeLabel.setText(newVal.intValue() + " minutes");
        });
        optimizationTimeLabel.setText("5 minutes");

        // Backup frequency
        backupFrequencyCombo.getItems().addAll(
                "Every Hour", "Daily", "Weekly", "Monthly");
        backupFrequencyCombo.setValue("Daily");

        // Export format
        defaultExportFormatCombo.getItems().addAll(
                "Excel (.xlsx)", "PDF", "CSV", "iCalendar (.ics)", "HTML", "JSON");
        defaultExportFormatCombo.setValue("Excel (.xlsx)");

        // Auto-save interval enable/disable
        autoSaveCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            autoSaveIntervalSpinner.setDisable(!newVal);
        });
    }

    /**
     * Load settings from properties file
     */
    private void loadSettings() {
        try {
            File settingsFile = new File("config/app-settings.properties");
            if (settingsFile.exists()) {
                settings.load(new java.io.FileInputStream(settingsFile));
                log.info("Settings loaded from file");
            } else {
                // Load defaults
                setDefaultSettings();
                log.info("Using default settings");
            }
        } catch (IOException e) {
            log.error("Failed to load settings", e);
            setDefaultSettings();
        }
    }

    /**
     * Set default settings
     */
    private void setDefaultSettings() {
        settings.setProperty("school.name", "Sample School");
        settings.setProperty("school.year", "2025-2026");
        settings.setProperty("theme", "Dark");
        settings.setProperty("auto.save", "true");
        settings.setProperty("auto.save.interval", "5");
        settings.setProperty("schedule.type", "Traditional");
        settings.setProperty("periods.per.day", "7");
        settings.setProperty("period.duration", "50");
        settings.setProperty("start.time", "08:00");
        settings.setProperty("end.time", "15:00");
        settings.setProperty("break.duration", "10");
        settings.setProperty("optimization.time", "5");
        settings.setProperty("apply.lean", "true");
        settings.setProperty("apply.kanban", "true");
        settings.setProperty("apply.eisenhower", "true");
        settings.setProperty("max.consecutive.hours", "3");
        settings.setProperty("min.break.minutes", "15");
        settings.setProperty("database.path", "./data/heronix");
        settings.setProperty("auto.backup", "true");
        settings.setProperty("backup.path", "./backups");
        settings.setProperty("backup.frequency", "Daily");
        settings.setProperty("default.export.format", "Excel (.xlsx)");
    }

    /**
     * Populate UI with settings values
     */
    private void populateSettings() {
        schoolNameField.setText(settings.getProperty("school.name", "Sample School"));
        schoolYearField.setText(settings.getProperty("school.year", "2025-2026"));
        themeCombo.setValue(settings.getProperty("theme", "Light"));
        autoSaveCheck.setSelected(Boolean.parseBoolean(settings.getProperty("auto.save", "true")));
        autoSaveIntervalSpinner.getValueFactory().setValue(
                Integer.parseInt(settings.getProperty("auto.save.interval", "5")));

        scheduleTypeCombo.setValue(settings.getProperty("schedule.type", "Traditional"));
        periodsPerDaySpinner.getValueFactory().setValue(
                Integer.parseInt(settings.getProperty("periods.per.day", "7")));
        periodDurationSpinner.getValueFactory().setValue(
                Integer.parseInt(settings.getProperty("period.duration", "50")));
        startTimeField.setText(settings.getProperty("start.time", "08:00"));
        endTimeField.setText(settings.getProperty("end.time", "15:00"));
        breakDurationSpinner.getValueFactory().setValue(
                Integer.parseInt(settings.getProperty("break.duration", "10")));

        optimizationTimeSlider.setValue(
                Double.parseDouble(settings.getProperty("optimization.time", "5")));
        applyLeanCheck.setSelected(Boolean.parseBoolean(settings.getProperty("apply.lean", "true")));
        applyKanbanCheck.setSelected(Boolean.parseBoolean(settings.getProperty("apply.kanban", "true")));
        applyEisenhowerCheck.setSelected(Boolean.parseBoolean(settings.getProperty("apply.eisenhower", "true")));
        maxConsecutiveHoursSpinner.getValueFactory().setValue(
                Integer.parseInt(settings.getProperty("max.consecutive.hours", "3")));
        minBreakMinutesSpinner.getValueFactory().setValue(
                Integer.parseInt(settings.getProperty("min.break.minutes", "15")));

        databasePathField.setText(settings.getProperty("database.path", "./data/heronix"));
        autoBackupCheck.setSelected(Boolean.parseBoolean(settings.getProperty("auto.backup", "true")));
        backupPathField.setText(settings.getProperty("backup.path", "./backups"));
        backupFrequencyCombo.setValue(settings.getProperty("backup.frequency", "Daily"));

        defaultExportFormatCombo.setValue(settings.getProperty("default.export.format", "Excel (.xlsx)"));
    }

    /**
     * Save settings
     */
    @FXML
    private void handleSave() {
        // Collect settings from UI
        settings.setProperty("school.name", schoolNameField.getText());
        settings.setProperty("school.year", schoolYearField.getText());
        settings.setProperty("theme", themeCombo.getValue());
        settings.setProperty("auto.save", String.valueOf(autoSaveCheck.isSelected()));
        settings.setProperty("auto.save.interval",
                String.valueOf(autoSaveIntervalSpinner.getValue()));

        settings.setProperty("schedule.type", scheduleTypeCombo.getValue());
        settings.setProperty("periods.per.day",
                String.valueOf(periodsPerDaySpinner.getValue()));
        settings.setProperty("period.duration",
                String.valueOf(periodDurationSpinner.getValue()));
        settings.setProperty("start.time", startTimeField.getText());
        settings.setProperty("end.time", endTimeField.getText());
        settings.setProperty("break.duration",
                String.valueOf(breakDurationSpinner.getValue()));

        settings.setProperty("optimization.time",
                String.valueOf((int) optimizationTimeSlider.getValue()));
        settings.setProperty("apply.lean", String.valueOf(applyLeanCheck.isSelected()));
        settings.setProperty("apply.kanban", String.valueOf(applyKanbanCheck.isSelected()));
        settings.setProperty("apply.eisenhower", String.valueOf(applyEisenhowerCheck.isSelected()));
        settings.setProperty("max.consecutive.hours",
                String.valueOf(maxConsecutiveHoursSpinner.getValue()));
        settings.setProperty("min.break.minutes",
                String.valueOf(minBreakMinutesSpinner.getValue()));

        settings.setProperty("database.path", databasePathField.getText());
        settings.setProperty("auto.backup", String.valueOf(autoBackupCheck.isSelected()));
        settings.setProperty("backup.path", backupPathField.getText());
        settings.setProperty("backup.frequency", backupFrequencyCombo.getValue());

        settings.setProperty("default.export.format", defaultExportFormatCombo.getValue());

        // Save to file
        try {
            File configDir = new File("config");
            if (!configDir.exists()) {
                configDir.mkdirs();
            }

            File settingsFile = new File("config/app-settings.properties");
            settings.store(new FileOutputStream(settingsFile), "Heronix Scheduling System Settings");

            showSuccess("Settings saved successfully!");
            log.info("Settings saved to file");

        } catch (IOException e) {
            log.error("Failed to save settings", e);
            showError("Failed to save settings: " + e.getMessage());
        }
    }

    /**
     * Reset to defaults
     */
    @FXML
    private void handleReset() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset Settings");
        alert.setHeaderText("Reset to Default Settings?");
        alert.setContentText("This will restore all settings to their default values.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                setDefaultSettings();
                populateSettings();
                log.info("Settings reset to defaults");
                showSuccess("Settings reset to defaults");
            }
        });
    }

    /**
     * Browse for database location
     */
    @FXML
    private void handleBrowseDatabase() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Database Location");
        File dir = chooser.showDialog(dialogStage);
        if (dir != null) {
            databasePathField.setText(dir.getAbsolutePath());
        }
    }

    /**
     * Browse for backup location
     */
    @FXML
    private void handleBrowseBackup() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Backup Location");
        File dir = chooser.showDialog(dialogStage);
        if (dir != null) {
            backupPathField.setText(dir.getAbsolutePath());
        }
    }

    /**
     * Test database connection
     */
    @FXML
    private void handleTestConnection() {
        if (dataSource == null) {
            showError("DataSource not available. Database may not be configured.");
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                String dbProduct = conn.getMetaData().getDatabaseProductName();
                String dbVersion = conn.getMetaData().getDatabaseProductVersion();

                log.info("Database connection successful: {} {}", dbProduct, dbVersion);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Connection Test");
                alert.setHeaderText("Connection Successful");
                alert.setContentText(String.format(
                    "Successfully connected to database!\n\n" +
                    "Database: %s\n" +
                    "Version: %s\n" +
                    "Connection valid: Yes",
                    dbProduct, dbVersion
                ));
                alert.showAndWait();
            } else {
                showError("Connection is closed or invalid.");
            }
        } catch (SQLException e) {
            log.error("Database connection test failed", e);
            showError("Failed to connect to database:\n" + e.getMessage());
        }
    }

    /**
     * Create backup now
     * Creates a backup of the H2 database files
     */
    @FXML
    private void handleBackupNow() {
        String backupPath = backupPathField.getText();
        if (backupPath == null || backupPath.trim().isEmpty()) {
            showError("Please specify a backup directory first.");
            return;
        }

        try {
            // Get backup directory
            Path backupDir = Paths.get(backupPath);
            if (!Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
            }

            // Create timestamped backup filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFileName = "heronix_backup_" + timestamp;

            // H2 database files to backup (data/heronix.mv.db)
            Path dbSourcePath = Paths.get("data/heronix.mv.db");
            Path dbBackupPath = backupDir.resolve(backupFileName + ".mv.db");

            if (Files.exists(dbSourcePath)) {
                Files.copy(dbSourcePath, dbBackupPath, StandardCopyOption.REPLACE_EXISTING);
                log.info("Database backup created: {}", dbBackupPath);

                // Also backup trace file if it exists
                Path traceSourcePath = Paths.get("data/heronix.trace.db");
                if (Files.exists(traceSourcePath)) {
                    Path traceBackupPath = backupDir.resolve(backupFileName + ".trace.db");
                    Files.copy(traceSourcePath, traceBackupPath, StandardCopyOption.REPLACE_EXISTING);
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Backup Complete");
                alert.setHeaderText("Backup Created Successfully");
                alert.setContentText(String.format(
                    "Database backup created:\n\n" +
                    "Location: %s\n" +
                    "File: %s\n" +
                    "Time: %s",
                    backupDir.toAbsolutePath(),
                    backupFileName + ".mv.db",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                ));
                alert.showAndWait();
            } else {
                showError("Database file not found at: " + dbSourcePath.toAbsolutePath());
            }

        } catch (IOException e) {
            log.error("Backup failed", e);
            showError("Failed to create backup:\n" + e.getMessage());
        }
    }

    /**
     * Cancel and close
     */
    @FXML
    private void handleCancel() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    /**
     * Show success message
     */
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Apply theme to the application
     *
     * @param themeName Theme name: "Light", "Dark", or "System"
     */
    private void applyTheme(String themeName) {
        try {
            log.info("========== APPLYING THEME: {} ==========", themeName);

            // Determine the CSS file path based on theme
            String themeFile;
            switch (themeName) {
                case "Dark":
                    themeFile = "/css/theme-dark.css";
                    log.info("Applying Dark theme (theme-dark.css)");
                    break;
                case "Light":
                    themeFile = "/css/theme-light.css";
                    log.info("Applying Light theme (theme-light.css)");
                    break;
                case "System":
                    // Detect system theme preference
                    themeFile = detectSystemTheme();
                    log.info("System theme applied ({})", themeFile);
                    break;
                default:
                    log.warn("Unknown theme: {}, defaulting to dark", themeName);
                    themeFile = "/css/theme-dark.css";
            }

            log.info("Theme CSS file: {}", themeFile);

            // Apply theme to all application windows
            applyThemeToAllWindows(themeFile);

            log.info("========== THEME APPLICATION COMPLETE ==========");

        } catch (Exception e) {
            log.error("Failed to apply theme", e);
            e.printStackTrace();
        }
    }

    /**
     * Detect system theme preference (dark/light mode)
     * Returns the appropriate theme CSS file path
     */
    private String detectSystemTheme() {
        try {
            // Try to detect Windows dark mode
            String os = System.getProperty("os.name", "").toLowerCase();
            if (os.contains("windows")) {
                // Check Windows registry for dark mode setting
                ProcessBuilder pb = new ProcessBuilder(
                    "reg", "query",
                    "HKCU\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                    "/v", "AppsUseLightTheme"
                );
                pb.redirectErrorStream(true);
                Process process = pb.start();
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream())
                );
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("AppsUseLightTheme")) {
                        // Value 0x0 means dark mode, 0x1 means light mode
                        if (line.contains("0x0")) {
                            log.info("System theme detected: Dark Mode");
                            return "/css/theme-dark.css";
                        } else {
                            log.info("System theme detected: Light Mode");
                            return "/css/theme-light.css";
                        }
                    }
                }
                process.waitFor();
            }
        } catch (Exception e) {
            log.warn("Could not detect system theme, defaulting to dark", e);
        }
        // Default to dark theme if detection fails
        return "/css/theme-dark.css";
    }

    /**
     * Apply theme to all application windows
     * This method finds all JavaFX windows and updates their stylesheets
     *
     * Note: We only load the theme CSS file (theme-dark.css or theme-light.css)
     * because these files contain complete styling. base-styles.css is NOT loaded
     * because it uses CSS variables (var()) which JavaFX does not support.
     */
    private void applyThemeToAllWindows(String themeFilePath) {
        try {
            // Check if theme resource exists
            java.net.URL themeUrl = getClass().getResource(themeFilePath);
            if (themeUrl == null) {
                log.error("Theme stylesheet not found: {}", themeFilePath);
                // Try fallback to dark theme
                themeUrl = getClass().getResource("/css/theme-dark.css");
                if (themeUrl == null) {
                    log.error("Fallback theme also not found!");
                    return;
                }
                log.info("Using fallback dark theme instead");
            }

            String themeCssUrl = themeUrl.toExternalForm();
            log.info("Loading theme stylesheet: {}", themeCssUrl);

            // Get all open windows and apply theme to each
            javafx.application.Platform.runLater(() -> {
                try {
                    javafx.stage.Window.getWindows().forEach(window -> {
                        if (window instanceof javafx.stage.Stage) {
                            javafx.stage.Stage stage = (javafx.stage.Stage) window;
                            if (stage.getScene() != null) {
                                javafx.scene.Scene scene = stage.getScene();

                                // Clear all existing stylesheets
                                scene.getStylesheets().clear();

                                // Add theme stylesheet (complete styling)
                                scene.getStylesheets().add(themeCssUrl);

                                log.info("Applied theme to window: {}", stage.getTitle());
                            }
                        }
                    });
                    log.info("Theme applied to all windows successfully");
                } catch (Exception e) {
                    log.error("Error applying theme to windows", e);
                }
            });
        } catch (Exception e) {
            log.error("Failed to apply theme stylesheet to windows", e);
            e.printStackTrace();
        }
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}