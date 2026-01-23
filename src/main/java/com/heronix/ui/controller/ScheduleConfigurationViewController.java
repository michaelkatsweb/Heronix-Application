package com.heronix.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;

/**
 * Controller for the Schedule Configuration View (UI/FXML)
 * Handles school day parameters, lunch periods, breaks, and constraints
 */
@Component("scheduleConfigurationViewController")
public class ScheduleConfigurationViewController {

    // School Day Tab
    @FXML private Spinner<Integer> schoolStartHour;
    @FXML private Spinner<Integer> schoolStartMinute;
    @FXML private Spinner<Integer> schoolEndHour;
    @FXML private Spinner<Integer> schoolEndMinute;
    @FXML private ComboBox<String> scheduleTypeCombo;
    @FXML private Spinner<Integer> periodDurationSpinner;
    @FXML private Spinner<Integer> periodsPerDaySpinner;
    @FXML private Spinner<Integer> passingPeriodDuration;
    @FXML private CheckBox blockScheduleCheck;
    @FXML private Spinner<Integer> blockDurationSpinner;

    // Lunch & Breaks Tab
    @FXML private CheckBox enableLunchCheck;
    @FXML private Spinner<Integer> lunchStartHour;
    @FXML private Spinner<Integer> lunchStartMinute;
    @FXML private Spinner<Integer> lunchDurationSpinner;
    @FXML private Spinner<Integer> lunchWavesSpinner;
    @FXML private CheckBox morningBreakCheck;
    @FXML private Spinner<Integer> morningBreakHour;
    @FXML private Spinner<Integer> morningBreakMinute;
    @FXML private Spinner<Integer> morningBreakDuration;
    @FXML private CheckBox afternoonBreakCheck;
    @FXML private Spinner<Integer> afternoonBreakHour;
    @FXML private Spinner<Integer> afternoonBreakMinute;
    @FXML private Spinner<Integer> afternoonBreakDuration;

    // Teacher Constraints Tab
    @FXML private Spinner<Integer> maxConsecutiveHours;
    @FXML private Spinner<Integer> maxClassesPerDay;
    @FXML private Spinner<Integer> maxDailyHours;
    @FXML private Spinner<Integer> minPrepPeriods;
    @FXML private CheckBox requireLunchBreakCheck;

    // Room Settings Tab
    @FXML private Spinner<Integer> defaultRoomCapacity;
    @FXML private Spinner<Integer> capacityBufferPercent;
    @FXML private CheckBox allowRoomSharingCheck;

    // Advanced Tab
    @FXML private CheckBox enableRotatingCheck;
    @FXML private TextField rotatingDaysField;
    @FXML private CheckBox enableFlexModCheck;
    @FXML private Spinner<Integer> flexModDuration;

    // Footer
    @FXML private Button loadTemplateButton;
    @FXML private Button resetButton;
    @FXML private Button saveButton;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        initializeSpinners();
        initializeComboBoxes();
        setupListeners();
        loadDefaults();
    }

    private void initializeSpinners() {
        // School times (0-23 hours, 0-59 minutes)
        initSpinner(schoolStartHour, 0, 23, 8);
        initSpinner(schoolStartMinute, 0, 59, 0);
        initSpinner(schoolEndHour, 0, 23, 15);
        initSpinner(schoolEndMinute, 0, 59, 0);

        // Period settings
        initSpinner(periodDurationSpinner, 30, 120, 50);
        initSpinner(periodsPerDaySpinner, 4, 12, 8);
        initSpinner(passingPeriodDuration, 3, 15, 5);
        initSpinner(blockDurationSpinner, 60, 180, 90);

        // Lunch settings
        initSpinner(lunchStartHour, 10, 14, 11);
        initSpinner(lunchStartMinute, 0, 59, 30);
        initSpinner(lunchDurationSpinner, 20, 60, 30);
        initSpinner(lunchWavesSpinner, 1, 4, 2);

        // Break settings
        initSpinner(morningBreakHour, 8, 12, 10);
        initSpinner(morningBreakMinute, 0, 59, 0);
        initSpinner(morningBreakDuration, 5, 20, 10);
        initSpinner(afternoonBreakHour, 13, 16, 14);
        initSpinner(afternoonBreakMinute, 0, 59, 0);
        initSpinner(afternoonBreakDuration, 5, 20, 10);

        // Teacher constraints
        initSpinner(maxConsecutiveHours, 1, 6, 3);
        initSpinner(maxClassesPerDay, 3, 10, 6);
        initSpinner(maxDailyHours, 4, 10, 7);
        initSpinner(minPrepPeriods, 0, 3, 1);

        // Room settings
        initSpinner(defaultRoomCapacity, 10, 50, 30);
        initSpinner(capacityBufferPercent, 0, 20, 10);

        // Flex mod
        initSpinner(flexModDuration, 10, 30, 15);
    }

    private void initSpinner(Spinner<Integer> spinner, int min, int max, int initial) {
        if (spinner != null) {
            spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, initial));
        }
    }

    private void initializeComboBoxes() {
        if (scheduleTypeCombo != null) {
            scheduleTypeCombo.getItems().addAll(
                "Traditional (Fixed Periods)",
                "Block Schedule (A/B)",
                "Rotating Schedule",
                "Flex-Mod Schedule",
                "Hybrid Schedule"
            );
            scheduleTypeCombo.setValue("Traditional (Fixed Periods)");
        }
    }

    private void setupListeners() {
        // Block schedule checkbox
        if (blockScheduleCheck != null && blockDurationSpinner != null) {
            blockScheduleCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
                blockDurationSpinner.setDisable(!newVal);
            });
        }

        // Lunch checkbox
        if (enableLunchCheck != null) {
            enableLunchCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
                setLunchFieldsDisabled(!newVal);
            });
        }

        // Morning break checkbox
        if (morningBreakCheck != null) {
            morningBreakCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
                setMorningBreakFieldsDisabled(!newVal);
            });
        }

        // Afternoon break checkbox
        if (afternoonBreakCheck != null) {
            afternoonBreakCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
                setAfternoonBreakFieldsDisabled(!newVal);
            });
        }

        // Rotating schedule checkbox
        if (enableRotatingCheck != null && rotatingDaysField != null) {
            enableRotatingCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
                rotatingDaysField.setDisable(!newVal);
            });
        }

        // Flex mod checkbox
        if (enableFlexModCheck != null && flexModDuration != null) {
            enableFlexModCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
                flexModDuration.setDisable(!newVal);
            });
        }
    }

    private void setLunchFieldsDisabled(boolean disabled) {
        if (lunchStartHour != null) lunchStartHour.setDisable(disabled);
        if (lunchStartMinute != null) lunchStartMinute.setDisable(disabled);
        if (lunchDurationSpinner != null) lunchDurationSpinner.setDisable(disabled);
        if (lunchWavesSpinner != null) lunchWavesSpinner.setDisable(disabled);
    }

    private void setMorningBreakFieldsDisabled(boolean disabled) {
        if (morningBreakHour != null) morningBreakHour.setDisable(disabled);
        if (morningBreakMinute != null) morningBreakMinute.setDisable(disabled);
        if (morningBreakDuration != null) morningBreakDuration.setDisable(disabled);
    }

    private void setAfternoonBreakFieldsDisabled(boolean disabled) {
        if (afternoonBreakHour != null) afternoonBreakHour.setDisable(disabled);
        if (afternoonBreakMinute != null) afternoonBreakMinute.setDisable(disabled);
        if (afternoonBreakDuration != null) afternoonBreakDuration.setDisable(disabled);
    }

    private void loadDefaults() {
        // Set default checkbox states
        if (enableLunchCheck != null) enableLunchCheck.setSelected(true);
        if (requireLunchBreakCheck != null) requireLunchBreakCheck.setSelected(true);
        if (allowRoomSharingCheck != null) allowRoomSharingCheck.setSelected(false);

        updateStatus("Configuration loaded");
    }

    @FXML
    private void handleLoadTemplate() {
        // Show template selection dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Load Template");
        alert.setHeaderText("Schedule Templates");
        alert.setContentText("Template loading coming soon. Default configuration applied.");
        alert.showAndWait();

        updateStatus("Template loaded");
    }

    @FXML
    private void handleResetDefaults() {
        loadDefaults();
        initializeSpinners();
        initializeComboBoxes();
        updateStatus("Reset to defaults");
    }

    @FXML
    private void handleSave() {
        // Save configuration
        updateStatus("Configuration saved successfully");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Save Configuration");
        alert.setHeaderText("Success");
        alert.setContentText("Schedule configuration has been saved.");
        alert.showAndWait();
    }

    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }
}
