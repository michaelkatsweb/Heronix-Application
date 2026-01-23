package com.heronix.ui.controller;

import com.heronix.model.domain.SchoolConfiguration;
import com.heronix.model.enums.SchoolType;
import com.heronix.repository.SchoolConfigurationRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * School Setup Wizard Controller
 *
 * Handles the first-time installation wizard that configures:
 * - School type (determines grade levels and courses available)
 * - School identification information
 * - Feature enablement
 *
 * This wizard appears on first launch before the main application.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
@Component
public class SchoolSetupWizardController {

    @Autowired(required = false)
    private SchoolConfigurationRepository configRepository;

    // Step indicators
    @FXML private Label step1Circle;
    @FXML private Label step2Circle;
    @FXML private Label step3Circle;
    @FXML private Label step4Circle;

    // Step panels
    @FXML private VBox step1Panel;
    @FXML private VBox step2Panel;
    @FXML private VBox step3Panel;
    @FXML private VBox step4Panel;

    // Step 1: School Type
    @FXML private RadioButton elementaryRadio;
    @FXML private RadioButton middleRadio;
    @FXML private RadioButton highSchoolRadio;
    @FXML private RadioButton k8Radio;
    @FXML private RadioButton k12Radio;
    private ToggleGroup schoolTypeGroup;

    // Step 2: School Info
    @FXML private TextField schoolNameField;
    @FXML private TextField districtField;
    @FXML private TextField addressField;
    @FXML private TextField cityField;
    @FXML private ComboBox<String> stateComboBox;
    @FXML private TextField zipField;
    @FXML private TextField phoneField;
    @FXML private TextField principalField;
    @FXML private TextField principalEmailField;
    @FXML private TextField academicYearField;

    // Step 3: Features
    @FXML private CheckBox attendanceCheckBox;
    @FXML private CheckBox gradebookCheckBox;
    @FXML private CheckBox photoCheckBox;
    @FXML private CheckBox medicalCheckBox;
    @FXML private CheckBox spedCheckBox;
    @FXML private CheckBox schedulingCheckBox;

    // Step 4: Summary
    @FXML private Label summarySchoolType;
    @FXML private Label summarySchoolName;
    @FXML private Label summaryFeatures;

    // Navigation
    @FXML private Button backButton;
    @FXML private Button skipButton;
    @FXML private Button nextButton;
    @FXML private Button finishButton;

    private int currentStep = 1;
    private boolean setupCompleted = false;

    @FXML
    public void initialize() {
        log.info("Initializing School Setup Wizard");

        // Setup toggle group for school type
        schoolTypeGroup = new ToggleGroup();
        elementaryRadio.setToggleGroup(schoolTypeGroup);
        middleRadio.setToggleGroup(schoolTypeGroup);
        highSchoolRadio.setToggleGroup(schoolTypeGroup);
        k8Radio.setToggleGroup(schoolTypeGroup);
        k12Radio.setToggleGroup(schoolTypeGroup);

        // Default to high school
        highSchoolRadio.setSelected(true);

        // Setup state combo box
        setupStateComboBox();

        // Set default academic year
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        if (month >= 7) {
            academicYearField.setText(year + "-" + (year + 1));
        } else {
            academicYearField.setText((year - 1) + "-" + year);
        }

        log.info("School Setup Wizard initialized");
    }

    private void setupStateComboBox() {
        stateComboBox.setItems(FXCollections.observableArrayList(
                "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA",
                "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD",
                "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ",
                "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC",
                "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY", "DC"
        ));
    }

    @FXML
    private void handleNext() {
        if (validateCurrentStep()) {
            if (currentStep < 4) {
                currentStep++;
                updateStepDisplay();

                if (currentStep == 4) {
                    updateSummary();
                }
            }
        }
    }

    @FXML
    private void handleBack() {
        if (currentStep > 1) {
            currentStep--;
            updateStepDisplay();
        }
    }

    @FXML
    private void handleSkip() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Skip Setup?");
        alert.setHeaderText("Skip Initial Setup");
        alert.setContentText("You can configure your school later from Settings.\n\n" +
                "The system will default to a K-12 configuration with all features enabled.\n\n" +
                "Skip setup now?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Create default configuration
                createDefaultConfiguration();
                closeWizard();
            }
        });
    }

    @FXML
    private void handleFinish() {
        log.info("Completing school setup wizard");

        try {
            // Save configuration
            SchoolConfiguration config = createConfiguration();

            if (configRepository != null) {
                config = configRepository.save(config);
                log.info("School configuration saved with ID: {}", config.getId());
            }

            setupCompleted = true;
            closeWizard();

        } catch (Exception e) {
            log.error("Error saving school configuration", e);
            showError("Failed to save configuration: " + e.getMessage());
        }
    }

    private boolean validateCurrentStep() {
        switch (currentStep) {
            case 1:
                if (schoolTypeGroup.getSelectedToggle() == null) {
                    showError("Please select a school type.");
                    return false;
                }
                return true;

            case 2:
                if (schoolNameField.getText().trim().isEmpty()) {
                    showError("Please enter the school name.");
                    return false;
                }
                return true;

            case 3:
                // Features step - no required fields
                return true;

            default:
                return true;
        }
    }

    private void updateStepDisplay() {
        // Hide all panels
        step1Panel.setVisible(false);
        step1Panel.setManaged(false);
        step2Panel.setVisible(false);
        step2Panel.setManaged(false);
        step3Panel.setVisible(false);
        step3Panel.setManaged(false);
        step4Panel.setVisible(false);
        step4Panel.setManaged(false);

        // Show current panel
        VBox currentPanel = switch (currentStep) {
            case 1 -> step1Panel;
            case 2 -> step2Panel;
            case 3 -> step3Panel;
            case 4 -> step4Panel;
            default -> step1Panel;
        };
        currentPanel.setVisible(true);
        currentPanel.setManaged(true);

        // Update step indicators
        updateStepIndicator(step1Circle, currentStep >= 1, currentStep > 1);
        updateStepIndicator(step2Circle, currentStep >= 2, currentStep > 2);
        updateStepIndicator(step3Circle, currentStep >= 3, currentStep > 3);
        updateStepIndicator(step4Circle, currentStep >= 4, false);

        // Update navigation buttons
        backButton.setDisable(currentStep == 1);
        nextButton.setVisible(currentStep < 4);
        nextButton.setManaged(currentStep < 4);
        finishButton.setVisible(currentStep == 4);
        finishButton.setManaged(currentStep == 4);
    }

    private void updateStepIndicator(Label indicator, boolean active, boolean completed) {
        if (completed) {
            indicator.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white; " +
                    "-fx-background-color: #4caf50; -fx-background-radius: 50; " +
                    "-fx-min-width: 40; -fx-min-height: 40; -fx-alignment: center;");
        } else if (active) {
            indicator.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white; " +
                    "-fx-background-color: #1976d2; -fx-background-radius: 50; " +
                    "-fx-min-width: 40; -fx-min-height: 40; -fx-alignment: center;");
        } else {
            indicator.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #9e9e9e; " +
                    "-fx-background-color: #e0e0e0; -fx-background-radius: 50; " +
                    "-fx-min-width: 40; -fx-min-height: 40; -fx-alignment: center;");
        }
    }

    private void updateSummary() {
        SchoolType selectedType = getSelectedSchoolType();

        summarySchoolType.setText("School Type: " + selectedType.getDisplayName() +
                " (" + selectedType.getGradeRangeDisplay() + ")");

        String schoolName = schoolNameField.getText().trim();
        summarySchoolName.setText("School Name: " + (schoolName.isEmpty() ? "Not specified" : schoolName));

        int enabledFeatures = 0;
        if (attendanceCheckBox.isSelected()) enabledFeatures++;
        if (gradebookCheckBox.isSelected()) enabledFeatures++;
        if (photoCheckBox.isSelected()) enabledFeatures++;
        if (medicalCheckBox.isSelected()) enabledFeatures++;
        if (spedCheckBox.isSelected()) enabledFeatures++;
        if (schedulingCheckBox.isSelected()) enabledFeatures++;

        summaryFeatures.setText("Features: " + enabledFeatures + " of 6 enabled");
    }

    private SchoolType getSelectedSchoolType() {
        if (elementaryRadio.isSelected()) return SchoolType.ELEMENTARY;
        if (middleRadio.isSelected()) return SchoolType.MIDDLE_SCHOOL;
        if (highSchoolRadio.isSelected()) return SchoolType.HIGH_SCHOOL;
        if (k8Radio.isSelected()) return SchoolType.K8_SCHOOL;
        if (k12Radio.isSelected()) return SchoolType.K12_SCHOOL;
        return SchoolType.K12_SCHOOL; // Default
    }

    private SchoolConfiguration createConfiguration() {
        SchoolConfiguration config = new SchoolConfiguration();

        // School type
        SchoolType schoolType = getSelectedSchoolType();
        config.setSchoolType(schoolType);
        config.setMinGradeLevel(schoolType.getMinGradeValue());
        config.setMaxGradeLevel(schoolType.getMaxGradeValue());

        // School info
        config.setSchoolName(schoolNameField.getText().trim());
        config.setDistrictName(districtField.getText().trim());
        config.setAddressStreet(addressField.getText().trim());
        config.setAddressCity(cityField.getText().trim());
        config.setAddressState(stateComboBox.getValue());
        config.setAddressZip(zipField.getText().trim());
        config.setPhoneNumber(phoneField.getText().trim());
        config.setPrincipalName(principalField.getText().trim());
        config.setPrincipalEmail(principalEmailField.getText().trim());
        config.setAcademicYear(academicYearField.getText().trim());

        // Features
        config.setEnableAttendance(attendanceCheckBox.isSelected());
        config.setEnableGradebook(gradebookCheckBox.isSelected());
        config.setRequireStudentPhoto(photoCheckBox.isSelected());
        config.setEnableQrCodes(photoCheckBox.isSelected());
        config.setEnableMedicalRecords(medicalCheckBox.isSelected());
        config.setEnableSpedTracking(spedCheckBox.isSelected());

        // Mark setup complete
        config.completeSetup("System Administrator");

        return config;
    }

    private void createDefaultConfiguration() {
        SchoolConfiguration config = new SchoolConfiguration();
        config.setSchoolType(SchoolType.K12_SCHOOL);
        config.setMinGradeLevel(-1);
        config.setMaxGradeLevel(12);
        config.setSchoolName("My School");
        config.setEnableAttendance(true);
        config.setEnableGradebook(true);
        config.setRequireStudentPhoto(true);
        config.setEnableQrCodes(true);
        config.setEnableMedicalRecords(true);
        config.setEnableSpedTracking(true);
        config.completeSetup("Default Setup");

        if (configRepository != null) {
            configRepository.save(config);
            log.info("Default school configuration created");
        }

        setupCompleted = true;
    }

    private void closeWizard() {
        Stage stage = (Stage) nextButton.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Check if setup was completed successfully
     */
    public boolean isSetupCompleted() {
        return setupCompleted;
    }
}
