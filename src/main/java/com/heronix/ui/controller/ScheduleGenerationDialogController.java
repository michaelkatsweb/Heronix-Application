package com.heronix.ui.controller;

import com.heronix.integration.SchedulerApiClient;
import com.heronix.model.domain.Schedule;
import com.heronix.service.ScheduleService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for Schedule Generation Mode Selection Dialog
 * Allows admin to choose between Manual, AI-Assisted, and Fully Automated scheduling
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Component
public class ScheduleGenerationDialogController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private SchedulerApiClient schedulerApiClient;

    // FXML Components
    @FXML private ComboBox<Schedule> scheduleComboBox;
    @FXML private javafx.scene.text.Text scheduleInfoText;

    @FXML private RadioButton manualModeRadio;
    @FXML private RadioButton aiAssistedModeRadio;
    @FXML private RadioButton fullyAutomatedModeRadio;
    @FXML private ToggleGroup modeToggleGroup;

    @FXML private VBox manualModeInfo;
    @FXML private VBox aiAssistedModeInfo;
    @FXML private VBox fullyAutomatedModeInfo;

    @FXML private VBox optimizationSettingsBox;
    @FXML private Slider optimizationTimeSlider;
    @FXML private Label optimizationTimeLabel;
    @FXML private CheckBox waitForCompletionCheckbox;

    @FXML private HBox aiStatusBox;
    @FXML private Label aiStatusIcon;
    @FXML private Label aiStatusLabel;
    @FXML private Label aiStatusMessage;

    private boolean aiAvailable = false;
    private DialogPane dialogPane;

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        log.info("Initializing Schedule Generation Dialog");

        // Set up schedule combo box
        scheduleComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Schedule item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String year = item.getStartDate() != null ?
                        String.valueOf(item.getStartDate().getYear()) : "Unknown";
                    setText(item.getScheduleName() + " (" + year + ")");
                }
            }
        });

        scheduleComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Schedule item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String year = item.getStartDate() != null ?
                        String.valueOf(item.getStartDate().getYear()) : "Unknown";
                    setText(item.getScheduleName() + " (" + year + ")");
                }
            }
        });

        // Load schedules
        loadSchedules();

        // Set up optimization time slider
        optimizationTimeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int seconds = newVal.intValue();
            optimizationTimeLabel.setText(seconds + " seconds");
        });

        // Mode selection listeners
        modeToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            updateUIForSelectedMode();
        });

        // Schedule selection listener
        scheduleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateScheduleInfo(newVal);
            }
        });

        // Check AI availability
        checkAIAvailability();

        // Initial UI update
        updateUIForSelectedMode();
    }

    /**
     * Set the dialog pane reference
     */
    public void setDialogPane(DialogPane pane) {
        this.dialogPane = pane;
    }

    /**
     * Load schedules from database
     */
    private void loadSchedules() {
        try {
            List<Schedule> schedules = scheduleService.getAllSchedules();
            scheduleComboBox.getItems().setAll(schedules);

            if (!schedules.isEmpty()) {
                scheduleComboBox.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            log.error("Error loading schedules", e);
            showError("Failed to load schedules: " + e.getMessage());
        }
    }

    /**
     * Update schedule info text
     */
    private void updateScheduleInfo(Schedule schedule) {
        if (schedule == null) {
            scheduleInfoText.setText("");
            return;
        }

        String info = String.format(
            "Type: %s | Slots: %d | Current Status: %s",
            schedule.getScheduleType() != null ? schedule.getScheduleType().getDisplayName() : "Unknown",
            schedule.getSlots() != null ? schedule.getSlots().size() : 0,
            schedule.getActive() ? "Active" : "Inactive"
        );

        scheduleInfoText.setText(info);
    }

    /**
     * Update UI based on selected mode
     */
    private void updateUIForSelectedMode() {
        Toggle selected = modeToggleGroup.getSelectedToggle();

        if (selected == manualModeRadio) {
            // Manual mode - hide AI settings
            optimizationSettingsBox.setVisible(false);
            optimizationSettingsBox.setManaged(false);
            aiStatusBox.setVisible(false);
            aiStatusBox.setManaged(false);

        } else if (selected == aiAssistedModeRadio || selected == fullyAutomatedModeRadio) {
            // AI modes - show settings
            optimizationSettingsBox.setVisible(true);
            optimizationSettingsBox.setManaged(true);
            aiStatusBox.setVisible(true);
            aiStatusBox.setManaged(true);

            // Disable if AI not available
            if (!aiAvailable) {
                if (dialogPane != null) {
                    Button generateButton = (Button) dialogPane.lookupButton(ButtonType.OK);
                    if (generateButton != null) {
                        generateButton.setDisable(true);
                    }
                }
            }
        }
    }

    /**
     * Check if SchedulerV2 AI service is available
     */
    private void checkAIAvailability() {
        aiStatusLabel.setText("Checking SchedulerV2 availability...");
        aiStatusMessage.setText("Please wait...");
        aiStatusIcon.setStyle("-fx-font-size: 20; -fx-text-fill: #856404;");

        CompletableFuture.runAsync(() -> {
            try {
                // Use SchedulerApiClient to check availability
                boolean available = schedulerApiClient.isSchedulerAvailable();
                String reason = available ?
                    "SchedulerV2 is online and ready for AI scheduling" :
                    "SchedulerV2 service is not accessible (check if running on localhost:8090)";

                Platform.runLater(() -> {
                    aiAvailable = available;

                    if (aiAvailable) {
                        aiStatusLabel.setText("SchedulerV2 Available");
                        aiStatusMessage.setText(reason != null ? reason : "AI scheduling is ready");
                        aiStatusIcon.setStyle("-fx-font-size: 20; -fx-text-fill: #27ae60;");
                        aiStatusBox.setStyle("-fx-background-color: #d4edda; -fx-background-radius: 5; -fx-padding: 10;");

                        // Enable generate button
                        if (dialogPane != null) {
                            Button generateButton = (Button) dialogPane.lookupButton(ButtonType.OK);
                            if (generateButton != null) {
                                generateButton.setDisable(false);
                            }
                        }
                    } else {
                        aiStatusLabel.setText("SchedulerV2 Unavailable");
                        aiStatusMessage.setText(reason != null ? reason : "AI scheduling service is not available");
                        aiStatusIcon.setStyle("-fx-font-size: 20; -fx-text-fill: #dc3545;");
                        aiStatusBox.setStyle("-fx-background-color: #f8d7da; -fx-background-radius: 5; -fx-padding: 10;");

                        // Disable AI modes
                        aiAssistedModeRadio.setDisable(true);
                        fullyAutomatedModeRadio.setDisable(true);

                        // Select manual mode
                        manualModeRadio.setSelected(true);
                    }
                });

            } catch (Exception e) {
                log.error("Error checking AI availability", e);
                Platform.runLater(() -> {
                    aiAvailable = false;
                    aiStatusLabel.setText("SchedulerV2 Unavailable");
                    aiStatusMessage.setText("Could not connect to AI scheduling service");
                    aiStatusIcon.setStyle("-fx-font-size: 20; -fx-text-fill: #dc3545;");
                    aiStatusBox.setStyle("-fx-background-color: #f8d7da; -fx-background-radius: 5; -fx-padding: 10;");

                    aiAssistedModeRadio.setDisable(true);
                    fullyAutomatedModeRadio.setDisable(true);
                    manualModeRadio.setSelected(true);
                });
            }
        });
    }

    /**
     * Get the generation request data
     */
    public Map<String, Object> getGenerationRequest() {
        Map<String, Object> request = new HashMap<>();

        Schedule selectedSchedule = scheduleComboBox.getValue();
        if (selectedSchedule == null) {
            throw new IllegalStateException("No schedule selected");
        }

        request.put("scheduleId", selectedSchedule.getId());

        Toggle selected = modeToggleGroup.getSelectedToggle();
        if (selected == manualModeRadio) {
            request.put("mode", "MANUAL");
        } else if (selected == aiAssistedModeRadio) {
            request.put("mode", "AI_ASSISTED");
        } else if (selected == fullyAutomatedModeRadio) {
            request.put("mode", "FULLY_AUTOMATED");
        }

        request.put("optimizationTimeSeconds", (int) optimizationTimeSlider.getValue());
        request.put("waitForCompletion", waitForCompletionCheckbox.isSelected());

        return request;
    }

    /**
     * Validate the form
     */
    public boolean validate() {
        if (scheduleComboBox.getValue() == null) {
            showError("Please select a schedule");
            return false;
        }

        Toggle selected = modeToggleGroup.getSelectedToggle();
        if (selected == null) {
            showError("Please select a generation mode");
            return false;
        }

        // Check if AI mode selected but AI not available
        if ((selected == aiAssistedModeRadio || selected == fullyAutomatedModeRadio) && !aiAvailable) {
            showError("AI scheduling is not available. Please select Manual mode or ensure SchedulerV2 service is running.");
            return false;
        }

        return true;
    }

    /**
     * Show error alert
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
