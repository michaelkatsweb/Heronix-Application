package com.heronix.ui.controller;

import com.heronix.service.AttendanceConfigurationService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AttendanceConfigurationController {

    @Autowired
    private AttendanceConfigurationService attendanceConfigurationService;

    // General Settings
    @FXML private TextField schoolYearField;
    @FXML private TextField totalInstructionalDaysField;
    @FXML private ComboBox<String> trackingMethodComboBox;
    @FXML private CheckBox enableRemoteAttendanceCheckBox;
    @FXML private CheckBox requireParentNotificationCheckBox;

    // Absence Thresholds
    @FXML private TextField earlyWarningThresholdField;
    @FXML private CheckBox earlyWarningAlertCheckBox;
    @FXML private TextField interventionThresholdField;
    @FXML private CheckBox interventionAlertCheckBox;
    @FXML private TextField chronicAbsenteeismThresholdField;
    @FXML private CheckBox chronicAbsenteeismAlertCheckBox;
    @FXML private TextField severeChronicThresholdField;
    @FXML private CheckBox severeChronicAlertCheckBox;
    @FXML private TextField truancyThresholdField;
    @FXML private CheckBox truancyAlertCheckBox;

    // Tardy Settings
    @FXML private TextField tardyGracePeriodField;
    @FXML private TextField tardyBecomesAbsentField;
    @FXML private TextField excessiveTardyThresholdField;
    @FXML private ComboBox<String> tardyConsequenceComboBox;
    @FXML private CheckBox tardiesCountAsAbsenceCheckBox;

    // Notification Settings
    @FXML private CheckBox dailyAbsenceNotificationCheckBox;
    @FXML private TextField notificationTimeField;
    @FXML private CheckBox notifyByEmailCheckBox;
    @FXML private CheckBox notifyBySmsCheckBox;
    @FXML private CheckBox notifyByPhoneCheckBox;
    @FXML private CheckBox notifyByPortalCheckBox;
    @FXML private CheckBox weeklySummaryCheckBox;
    @FXML private ComboBox<String> summaryDayComboBox;

    // Automated Interventions
    @FXML private CheckBox autoCreateCounselorReferralCheckBox;
    @FXML private CheckBox autoCreateAttendancePlanCheckBox;
    @FXML private CheckBox autoAssignCaseManagerCheckBox;
    @FXML private CheckBox autoScheduleParentMeetingCheckBox;
    @FXML private CheckBox autoGenerateReportsCheckBox;

    // Excuse/Documentation Requirements
    @FXML private TextField documentationRequiredField;
    @FXML private CheckBox doctorsNoteRequiredCheckBox;
    @FXML private TextField excuseDeadlineField;
    @FXML private CheckBox allowRetroactiveCheckBox;
    @FXML private CheckBox autoExcuseSchoolActivitiesCheckBox;

    // State Reporting
    @FXML private ComboBox<String> stateReportingFormatComboBox;
    @FXML private CheckBox reportChronicAbsenteeismCheckBox;
    @FXML private TextField truancyReportingThresholdField;
    @FXML private CheckBox autoSubmitStateReportsCheckBox;

    // Additional Settings
    @FXML private TextField perfectAttendanceField;
    @FXML private CheckBox awardPerfectAttendanceCheckBox;
    @FXML private CheckBox allowStudentCheckInCheckBox;
    @FXML private CheckBox attendanceAffectsGradesCheckBox;
    @FXML private TextArea configurationNotesArea;

    // Action Buttons
    @FXML private Button saveButton;
    @FXML private Button resetButton;
    @FXML private Button cancelButton;

    @FXML
    public void initialize() {
        setupComboBoxes();
        loadConfiguration();
        setupValidation();
    }

    private void setupComboBoxes() {
        // Tracking Method
        trackingMethodComboBox.setItems(FXCollections.observableArrayList(
                "Daily - Full Day Only",
                "Period by Period",
                "Hybrid - Daily + Period Tracking",
                "Minutes-Based"
        ));

        // Tardy Consequence
        tardyConsequenceComboBox.setItems(FXCollections.observableArrayList(
                "Warning Only",
                "Detention",
                "Parent Contact",
                "Administrative Referral",
                "Loss of Privileges"
        ));

        // Summary Day
        summaryDayComboBox.setItems(FXCollections.observableArrayList(
                "Monday",
                "Tuesday",
                "Wednesday",
                "Thursday",
                "Friday",
                "Saturday",
                "Sunday"
        ));

        // State Reporting Format
        stateReportingFormatComboBox.setItems(FXCollections.observableArrayList(
                "CSV Export",
                "XML Format",
                "State Portal API",
                "PDF Report",
                "Excel Spreadsheet"
        ));
    }

    private void loadConfiguration() {
        // Load default configuration or from service
        // Set default values
        schoolYearField.setText("2024-2025");
        totalInstructionalDaysField.setText("180");
        trackingMethodComboBox.setValue("Period by Period");

        // Default thresholds
        earlyWarningThresholdField.setText("3");
        interventionThresholdField.setText("5");
        chronicAbsenteeismThresholdField.setText("18");
        severeChronicThresholdField.setText("36");
        truancyThresholdField.setText("10");

        // Tardy defaults
        tardyGracePeriodField.setText("5");
        tardyBecomesAbsentField.setText("30");
        excessiveTardyThresholdField.setText("5");
        tardyConsequenceComboBox.setValue("Parent Contact");

        // Notification defaults
        notificationTimeField.setText("15:00");
        summaryDayComboBox.setValue("Friday");

        // Documentation defaults
        documentationRequiredField.setText("3");
        excuseDeadlineField.setText("3");

        // State reporting
        stateReportingFormatComboBox.setValue("CSV Export");
        truancyReportingThresholdField.setText("10");

        // Perfect attendance
        perfectAttendanceField.setText("0 absences, max 2 tardies");

        // Set default checkboxes
        requireParentNotificationCheckBox.setSelected(true);
        earlyWarningAlertCheckBox.setSelected(true);
        interventionAlertCheckBox.setSelected(true);
        chronicAbsenteeismAlertCheckBox.setSelected(true);
        severeChronicAlertCheckBox.setSelected(true);
        truancyAlertCheckBox.setSelected(true);
        dailyAbsenceNotificationCheckBox.setSelected(true);
        notifyByEmailCheckBox.setSelected(true);
        notifyByPortalCheckBox.setSelected(true);
        reportChronicAbsenteeismCheckBox.setSelected(true);
        allowRetroactiveCheckBox.setSelected(true);
        autoExcuseSchoolActivitiesCheckBox.setSelected(true);
        awardPerfectAttendanceCheckBox.setSelected(true);
    }

    private void setupValidation() {
        // Add numeric validation for threshold fields
        addNumericValidation(totalInstructionalDaysField);
        addNumericValidation(earlyWarningThresholdField);
        addNumericValidation(interventionThresholdField);
        addNumericValidation(chronicAbsenteeismThresholdField);
        addNumericValidation(severeChronicThresholdField);
        addNumericValidation(truancyThresholdField);
        addNumericValidation(tardyGracePeriodField);
        addNumericValidation(tardyBecomesAbsentField);
        addNumericValidation(excessiveTardyThresholdField);
        addNumericValidation(documentationRequiredField);
        addNumericValidation(excuseDeadlineField);
        addNumericValidation(truancyReportingThresholdField);

        // Time validation for notification time
        notificationTimeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !newVal.matches("\\d{1,2}:\\d{2}")) {
                notificationTimeField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            } else {
                notificationTimeField.setStyle("");
            }
        });
    }

    private void addNumericValidation(TextField field) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !newVal.matches("\\d+")) {
                field.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            } else {
                field.setStyle("");
            }
        });
    }

    @FXML
    private void handleSave() {
        List<String> errors = validateConfiguration();
        if (!errors.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please correct the following errors:");
            alert.setContentText(String.join("\n", errors));
            alert.showAndWait();
            return;
        }

        try {
            // Save threshold settings to service
            double chronicThreshold = parseDouble(chronicAbsenteeismThresholdField.getText(), 10.0);
            double earlyWarning = parseDouble(earlyWarningThresholdField.getText(), 3.0);
            AttendanceConfigurationService.ThresholdSettings thresholds =
                    AttendanceConfigurationService.ThresholdSettings.builder()
                            .chronicAbsenceThreshold(chronicThreshold)
                            .atRiskThreshold(earlyWarning)
                            .perfectAttendanceThreshold(100.0)
                            .build();
            attendanceConfigurationService.updateThresholdSettings(thresholds);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Configuration Saved");
            alert.setContentText("Attendance configuration has been saved successfully.\n\n" +
                    "Key Settings:\n" +
                    "• Early Warning: " + earlyWarningThresholdField.getText() + " days\n" +
                    "• Intervention: " + interventionThresholdField.getText() + " days\n" +
                    "• Chronic Absenteeism: " + chronicAbsenteeismThresholdField.getText() + " days\n" +
                    "• Truancy: " + truancyThresholdField.getText() + " unexcused days");
            alert.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to save configuration");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleReset() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Reset Configuration");
        confirmation.setHeaderText("Reset to Default Settings");
        confirmation.setContentText("Are you sure you want to reset all settings to defaults? This cannot be undone.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                loadConfiguration();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Reset Complete");
                alert.setHeaderText("Configuration Reset");
                alert.setContentText("All settings have been reset to default values.");
                alert.showAndWait();
            }
        });
    }

    @FXML
    private void handleCancel() {
        // Reload configuration without saving
        loadConfiguration();
    }

    private List<String> validateConfiguration() {
        List<String> errors = new ArrayList<>();

        // Validate required fields
        if (schoolYearField.getText().trim().isEmpty()) {
            errors.add("School Year is required");
        }

        if (totalInstructionalDaysField.getText().trim().isEmpty()) {
            errors.add("Total Instructional Days is required");
        } else {
            try {
                int days = Integer.parseInt(totalInstructionalDaysField.getText().trim());
                if (days <= 0 || days > 365) {
                    errors.add("Total Instructional Days must be between 1 and 365");
                }
            } catch (NumberFormatException e) {
                errors.add("Total Instructional Days must be a valid number");
            }
        }

        if (trackingMethodComboBox.getValue() == null) {
            errors.add("Attendance Tracking Method is required");
        }

        // Validate thresholds are logical
        try {
            int earlyWarning = Integer.parseInt(earlyWarningThresholdField.getText().trim());
            int intervention = Integer.parseInt(interventionThresholdField.getText().trim());
            int chronic = Integer.parseInt(chronicAbsenteeismThresholdField.getText().trim());
            int severe = Integer.parseInt(severeChronicThresholdField.getText().trim());

            if (earlyWarning >= intervention) {
                errors.add("Early Warning threshold must be less than Intervention threshold");
            }
            if (intervention >= chronic) {
                errors.add("Intervention threshold must be less than Chronic Absenteeism threshold");
            }
            if (chronic >= severe) {
                errors.add("Chronic Absenteeism threshold must be less than Severe Chronic threshold");
            }
        } catch (NumberFormatException e) {
            errors.add("All threshold values must be valid numbers");
        }

        // Validate tardy settings
        if (!tardyGracePeriodField.getText().trim().isEmpty() &&
            !tardyBecomesAbsentField.getText().trim().isEmpty()) {
            try {
                int grace = Integer.parseInt(tardyGracePeriodField.getText().trim());
                int becomesAbsent = Integer.parseInt(tardyBecomesAbsentField.getText().trim());

                if (grace >= becomesAbsent) {
                    errors.add("Grace period must be less than 'tardy becomes absent' time");
                }
            } catch (NumberFormatException e) {
                // Already validated by field validation
            }
        }

        // Validate notification time format
        if (!notificationTimeField.getText().trim().isEmpty()) {
            String time = notificationTimeField.getText().trim();
            if (!time.matches("\\d{1,2}:\\d{2}")) {
                errors.add("Notification time must be in HH:MM format");
            } else {
                String[] parts = time.split(":");
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);
                if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                    errors.add("Notification time must be valid (00:00 - 23:59)");
                }
            }
        }

        // Validate notification methods
        if (dailyAbsenceNotificationCheckBox.isSelected()) {
            if (!notifyByEmailCheckBox.isSelected() &&
                !notifyBySmsCheckBox.isSelected() &&
                !notifyByPhoneCheckBox.isSelected() &&
                !notifyByPortalCheckBox.isSelected()) {
                errors.add("At least one notification method must be selected for daily notifications");
            }
        }

        return errors;
    }

    private double parseDouble(String text, double defaultValue) {
        try {
            return Double.parseDouble(text.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
