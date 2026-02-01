package com.heronix.ui.controller;

import com.heronix.model.domain.ScheduleConfiguration;
import com.heronix.repository.ScheduleConfigurationRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

/**
 * Controller for the Schedule Configuration View (UI/FXML)
 * Handles school day parameters, lunch periods, breaks, and constraints
 */
@Slf4j
@Component("scheduleConfigurationViewController")
public class ScheduleConfigurationViewController {

    @Autowired
    private ScheduleConfigurationRepository configRepository;

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

    private ScheduleConfiguration currentConfig;

    @FXML
    public void initialize() {
        initializeSpinners();
        initializeComboBoxes();
        setupListeners();
        loadFromDatabase();
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

        updateStatus("Default configuration loaded");
    }

    private void loadFromDatabase() {
        new Thread(() -> {
            try {
                var configOpt = configRepository.findByActiveTrue();
                Platform.runLater(() -> {
                    if (configOpt.isPresent()) {
                        currentConfig = configOpt.get();
                        applyConfigToUI(currentConfig);
                        updateStatus("Configuration loaded from database");
                        log.info("Loaded schedule configuration: {}", currentConfig.getName());
                    } else {
                        loadDefaults();
                        log.info("No saved configuration found, using defaults");
                    }
                });
            } catch (Exception e) {
                log.error("Failed to load schedule configuration", e);
                Platform.runLater(() -> {
                    loadDefaults();
                    updateStatus("Error loading config, using defaults");
                });
            }
        }).start();
    }

    private void applyConfigToUI(ScheduleConfiguration config) {
        if (config.getSchoolStartTime() != null) {
            setSpinnerValue(schoolStartHour, config.getSchoolStartTime().getHour());
            setSpinnerValue(schoolStartMinute, config.getSchoolStartTime().getMinute());
        }
        if (config.getSchoolEndTime() != null) {
            setSpinnerValue(schoolEndHour, config.getSchoolEndTime().getHour());
            setSpinnerValue(schoolEndMinute, config.getSchoolEndTime().getMinute());
        }
        if (config.getDefaultPeriodDuration() != null) {
            setSpinnerValue(periodDurationSpinner, config.getDefaultPeriodDuration());
        }
        if (config.getPeriodsPerDay() != null) {
            setSpinnerValue(periodsPerDaySpinner, config.getPeriodsPerDay());
        }
        if (config.getBlockPeriodDuration() != null) {
            setSpinnerValue(blockDurationSpinner, config.getBlockPeriodDuration());
        }
        if (config.getLunchDurationMinutes() != null) {
            setSpinnerValue(lunchDurationSpinner, config.getLunchDurationMinutes());
        }
        if (config.getNumberOfLunchPeriods() != null) {
            setSpinnerValue(lunchWavesSpinner, config.getNumberOfLunchPeriods());
        }
        if (config.getDefaultMaxStudents() != null) {
            setSpinnerValue(defaultRoomCapacity, config.getDefaultMaxStudents());
        }
        if (config.getScheduleType() != null && scheduleTypeCombo != null) {
            switch (config.getScheduleType()) {
                case BLOCK -> scheduleTypeCombo.setValue("Block Schedule (A/B)");
                case HYBRID -> scheduleTypeCombo.setValue("Hybrid Schedule");
                default -> scheduleTypeCombo.setValue("Traditional (Fixed Periods)");
            }
        }
        if (Boolean.TRUE.equals(config.getUsesAlternatingDays()) && blockScheduleCheck != null) {
            blockScheduleCheck.setSelected(true);
        }
        if (enableLunchCheck != null) enableLunchCheck.setSelected(true);
        if (requireLunchBreakCheck != null) requireLunchBreakCheck.setSelected(true);
    }

    private void setSpinnerValue(Spinner<Integer> spinner, int value) {
        if (spinner != null && spinner.getValueFactory() != null) {
            spinner.getValueFactory().setValue(value);
        }
    }

    @FXML
    private void handleLoadTemplate() {
        javafx.scene.control.ChoiceDialog<String> dialog = new javafx.scene.control.ChoiceDialog<>(
                "Standard 8-Period Day",
                "Standard 8-Period Day",
                "Block Schedule (A/B Day)",
                "7-Period Traditional",
                "Trimester Schedule",
                "Modified Block"
        );
        dialog.setTitle("Load Template");
        dialog.setHeaderText("Select a Schedule Template");
        dialog.setContentText("Template:");

        dialog.showAndWait().ifPresent(template -> {
            loadDefaults();
            switch (template) {
                case "Block Schedule (A/B Day)" -> {
                    setSpinnerValue(periodsPerDaySpinner, 4);
                    setSpinnerValue(periodDurationSpinner, 90);
                    scheduleTypeCombo.setValue("A/B Block");
                }
                case "7-Period Traditional" -> {
                    setSpinnerValue(periodsPerDaySpinner, 7);
                    setSpinnerValue(periodDurationSpinner, 50);
                }
                case "Trimester Schedule" -> {
                    setSpinnerValue(periodsPerDaySpinner, 5);
                    setSpinnerValue(periodDurationSpinner, 60);
                }
                case "Modified Block" -> {
                    setSpinnerValue(periodsPerDaySpinner, 6);
                    setSpinnerValue(periodDurationSpinner, 55);
                }
                default -> {} // Standard 8-Period keeps defaults
            }
            initializeSpinners();
            initializeComboBoxes();
            updateStatus("Template '" + template + "' loaded");
        });
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
        try {
            if (currentConfig == null) {
                currentConfig = new ScheduleConfiguration();
                currentConfig.setName("Default");
                currentConfig.setActive(true);
            }

            // Read UI values into entity
            currentConfig.setSchoolStartTime(LocalTime.of(
                    schoolStartHour.getValue(), schoolStartMinute.getValue()));
            currentConfig.setSchoolEndTime(LocalTime.of(
                    schoolEndHour.getValue(), schoolEndMinute.getValue()));
            currentConfig.setDefaultPeriodDuration(periodDurationSpinner.getValue());
            currentConfig.setPeriodsPerDay(periodsPerDaySpinner.getValue());
            currentConfig.setBlockPeriodDuration(blockDurationSpinner.getValue());
            currentConfig.setLunchDurationMinutes(lunchDurationSpinner.getValue());
            currentConfig.setNumberOfLunchPeriods(lunchWavesSpinner.getValue());
            currentConfig.setDefaultMaxStudents(defaultRoomCapacity.getValue());

            if (blockScheduleCheck != null) {
                currentConfig.setUsesAlternatingDays(blockScheduleCheck.isSelected());
            }

            // Map schedule type combo to enum
            String typeVal = scheduleTypeCombo != null ? scheduleTypeCombo.getValue() : null;
            if (typeVal != null) {
                if (typeVal.contains("Block")) {
                    currentConfig.setScheduleType(ScheduleConfiguration.ScheduleType.BLOCK);
                } else if (typeVal.contains("Hybrid")) {
                    currentConfig.setScheduleType(ScheduleConfiguration.ScheduleType.HYBRID);
                } else {
                    currentConfig.setScheduleType(ScheduleConfiguration.ScheduleType.STANDARD);
                }
            }

            configRepository.save(currentConfig);
            log.info("Saved schedule configuration: {}", currentConfig.getName());
            updateStatus("Configuration saved successfully");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Save Configuration");
            alert.setHeaderText("Success");
            alert.setContentText("Schedule configuration has been saved to database.");
            alert.showAndWait();
        } catch (Exception e) {
            log.error("Failed to save schedule configuration", e);
            updateStatus("Save failed: " + e.getMessage());

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Save Failed");
            alert.setHeaderText(null);
            alert.setContentText("Failed to save configuration: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }
}
