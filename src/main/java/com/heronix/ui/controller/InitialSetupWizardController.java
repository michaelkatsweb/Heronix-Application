package com.heronix.ui.controller;

import com.heronix.model.DistrictSettings;
import com.heronix.model.domain.StateConfiguration;
import com.heronix.model.enums.USState;
import com.heronix.service.DistrictSettingsService;
import com.heronix.service.StateConfigurationService;
import com.heronix.service.StateCourseCodeService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Initial Setup Wizard Controller
 *
 * Handles the first-time setup of the Heronix SIS when installed.
 * The wizard guides administrators through:
 *
 * 1. State Selection - Choose the state the school/district operates in
 * 2. District Information - Enter district name, address, contact info
 * 3. School Year Setup - Configure academic calendar
 * 4. Import Options - Option to import state course catalog
 *
 * When a state is selected, the entire application is automatically
 * adapted to that state's:
 * - Course coding system
 * - Graduation requirements
 * - Grading scales
 * - Terminology (e.g., "Campus" vs "School")
 * - Reporting formats
 * - Attendance policies
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01 - State Configuration Feature
 */
@Slf4j
@Component
public class InitialSetupWizardController {

    // ========================================================================
    // FXML COMPONENTS
    // ========================================================================

    @FXML private StackPane wizardContainer;
    @FXML private VBox stepIndicator;
    @FXML private Label currentStepLabel;
    @FXML private ProgressBar overallProgress;

    // Step 1: State Selection
    @FXML private VBox stateSelectionPane;
    @FXML private ComboBox<USState> stateSelector;
    @FXML private Label stateInfoLabel;
    @FXML private VBox stateDetailsPanel;
    @FXML private Label stateEducationDept;
    @FXML private Label stateCourseSystem;
    @FXML private Label stateStudentIdSystem;
    @FXML private Label stateGradRequirements;

    // Step 2: District Information
    @FXML private VBox districtInfoPane;
    @FXML private TextField districtNameField;
    @FXML private TextField districtAddressField;
    @FXML private TextField districtCityField;
    @FXML private TextField districtZipField;
    @FXML private TextField districtPhoneField;
    @FXML private TextField districtEmailField;
    @FXML private TextField districtWebsiteField;
    @FXML private TextField stateDistrictIdField;
    @FXML private Label stateDistrictIdLabel;

    // Step 3: School Information
    @FXML private VBox schoolInfoPane;
    @FXML private TextField schoolNameField;
    @FXML private TextField schoolAddressField;
    @FXML private TextField schoolPhoneField;
    @FXML private TextField stateSchoolIdField;
    @FXML private Label stateSchoolIdLabel;

    // Step 4: Configuration Options
    @FXML private VBox configOptionsPane;
    @FXML private CheckBox importCourseCatalogCheck;
    @FXML private CheckBox useStateGradingScaleCheck;
    @FXML private CheckBox useStateGradRequirementsCheck;
    @FXML private CheckBox useStateTerminologyCheck;

    // Navigation
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Button finishButton;
    @FXML private ProgressIndicator loadingIndicator;

    // ========================================================================
    // SERVICES
    // ========================================================================

    @Autowired
    private StateConfigurationService stateConfigService;

    @Autowired
    private DistrictSettingsService districtSettingsService;

    @Autowired
    private StateCourseCodeService courseCatalogService;

    private Stage stage;
    private int currentStep = 1;
    private static final int TOTAL_STEPS = 4;

    private USState selectedState;
    private StateConfiguration stateConfig;

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    @FXML
    public void initialize() {
        setupStateSelector();
        updateStepDisplay();
        updateNavigationButtons();

        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }

        log.info("Initial Setup Wizard initialized");
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Check if setup wizard needs to be shown
     */
    public boolean needsSetup() {
        DistrictSettings settings = districtSettingsService.getOrCreateDistrictSettings();
        return !Boolean.TRUE.equals(settings.getStateSetupCompleted());
    }

    // ========================================================================
    // STATE SELECTOR SETUP
    // ========================================================================

    private void setupStateSelector() {
        // Get all US states (excluding territories and special entries)
        List<USState> states = Arrays.stream(USState.values())
                .filter(s -> s.ordinal() < 51) // Only 50 states + DC
                .sorted((a, b) -> a.getDisplayName().compareTo(b.getDisplayName()))
                .collect(Collectors.toList());

        stateSelector.setItems(FXCollections.observableArrayList(states));

        // Custom cell to show full state name
        stateSelector.setCellFactory(lv -> new ListCell<USState>() {
            @Override
            protected void updateItem(USState state, boolean empty) {
                super.updateItem(state, empty);
                if (empty || state == null) {
                    setText(null);
                } else {
                    setText(state.getDisplayName() + " (" + state.name() + ")");
                }
            }
        });

        stateSelector.setButtonCell(new ListCell<USState>() {
            @Override
            protected void updateItem(USState state, boolean empty) {
                super.updateItem(state, empty);
                setText(empty || state == null ? "Select Your State..." : state.getDisplayName());
            }
        });

        // Handle state selection
        stateSelector.setOnAction(e -> handleStateSelection());
    }

    private void handleStateSelection() {
        selectedState = stateSelector.getValue();
        if (selectedState == null) {
            hideStateDetails();
            return;
        }

        showLoading(true);

        // Load state configuration in background
        Task<StateConfiguration> loadTask = new Task<>() {
            @Override
            protected StateConfiguration call() {
                return stateConfigService.getOrCreateConfiguration(selectedState);
            }
        };

        loadTask.setOnSucceeded(e -> {
            stateConfig = loadTask.getValue();
            showStateDetails(stateConfig);
            updateNavigationButtons();
            showLoading(false);
        });

        loadTask.setOnFailed(e -> {
            log.error("Failed to load state configuration", loadTask.getException());
            showLoading(false);
            showError("Error", "Failed to load state configuration");
        });

        new Thread(loadTask).start();
    }

    private void showStateDetails(StateConfiguration config) {
        if (stateDetailsPanel != null) {
            stateDetailsPanel.setVisible(true);
        }

        if (stateEducationDept != null) {
            stateEducationDept.setText(config.getEducationDepartment() != null ?
                    config.getEducationDepartment() : config.getState().getCertifyingAgency());
        }

        if (stateCourseSystem != null) {
            stateCourseSystem.setText(config.getCourseCodeSystemName() != null ?
                    config.getCourseCodeSystemName() : "State Course Codes");
        }

        if (stateStudentIdSystem != null) {
            stateStudentIdSystem.setText(config.getStudentIdSystemName() != null ?
                    config.getStudentIdSystemName() : "State Student ID");
        }

        if (stateGradRequirements != null) {
            Double credits = config.getGraduationCreditsRequired();
            stateGradRequirements.setText(credits != null ?
                    credits + " credits required" : "See state requirements");
        }

        // Update labels in other steps
        updateStateSpecificLabels(config);
    }

    private void hideStateDetails() {
        if (stateDetailsPanel != null) {
            stateDetailsPanel.setVisible(false);
        }
    }

    private void updateStateSpecificLabels(StateConfiguration config) {
        // Update District ID label based on state
        if (stateDistrictIdLabel != null) {
            stateDistrictIdLabel.setText(config.getDistrictIdLabel() != null ?
                    config.getDistrictIdLabel() + ":" : "State District ID:");
        }

        // Update School ID label based on state
        if (stateSchoolIdLabel != null) {
            stateSchoolIdLabel.setText(config.getSchoolIdLabel() != null ?
                    config.getSchoolIdLabel() + ":" : "State School ID:");
        }
    }

    // ========================================================================
    // NAVIGATION
    // ========================================================================

    @FXML
    private void handlePrevious() {
        if (currentStep > 1) {
            currentStep--;
            showStep(currentStep);
            updateStepDisplay();
            updateNavigationButtons();
        }
    }

    @FXML
    private void handleNext() {
        if (validateCurrentStep()) {
            if (currentStep < TOTAL_STEPS) {
                currentStep++;
                showStep(currentStep);
                updateStepDisplay();
                updateNavigationButtons();
            }
        }
    }

    @FXML
    private void handleFinish() {
        if (validateCurrentStep()) {
            completeSetup();
        }
    }

    @FXML
    private void handleCancel() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Setup");
        confirm.setHeaderText("Are you sure you want to cancel?");
        confirm.setContentText("You can run the setup wizard again later from Settings.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && stage != null) {
                stage.close();
            }
        });
    }

    private void showStep(int step) {
        // Hide all panes
        if (stateSelectionPane != null) stateSelectionPane.setVisible(false);
        if (districtInfoPane != null) districtInfoPane.setVisible(false);
        if (schoolInfoPane != null) schoolInfoPane.setVisible(false);
        if (configOptionsPane != null) configOptionsPane.setVisible(false);

        // Show current step
        switch (step) {
            case 1 -> { if (stateSelectionPane != null) stateSelectionPane.setVisible(true); }
            case 2 -> { if (districtInfoPane != null) districtInfoPane.setVisible(true); }
            case 3 -> { if (schoolInfoPane != null) schoolInfoPane.setVisible(true); }
            case 4 -> { if (configOptionsPane != null) configOptionsPane.setVisible(true); }
        }
    }

    private void updateStepDisplay() {
        if (currentStepLabel != null) {
            String stepName = switch (currentStep) {
                case 1 -> "Select Your State";
                case 2 -> "District Information";
                case 3 -> "School Information";
                case 4 -> "Configuration Options";
                default -> "Setup";
            };
            currentStepLabel.setText("Step " + currentStep + " of " + TOTAL_STEPS + ": " + stepName);
        }

        if (overallProgress != null) {
            overallProgress.setProgress((double) currentStep / TOTAL_STEPS);
        }
    }

    private void updateNavigationButtons() {
        if (prevButton != null) {
            prevButton.setDisable(currentStep == 1);
        }

        if (nextButton != null) {
            nextButton.setVisible(currentStep < TOTAL_STEPS);
            nextButton.setDisable(currentStep == 1 && selectedState == null);
        }

        if (finishButton != null) {
            finishButton.setVisible(currentStep == TOTAL_STEPS);
        }
    }

    // ========================================================================
    // VALIDATION
    // ========================================================================

    private boolean validateCurrentStep() {
        switch (currentStep) {
            case 1 -> {
                if (selectedState == null) {
                    showError("State Required", "Please select your state to continue.");
                    return false;
                }
            }
            case 2 -> {
                if (districtNameField != null && districtNameField.getText().trim().isEmpty()) {
                    showError("District Name Required", "Please enter your district name.");
                    return false;
                }
            }
            case 3 -> {
                // School info is optional for district-level setup
            }
            case 4 -> {
                // Config options are all optional
            }
        }
        return true;
    }

    // ========================================================================
    // COMPLETE SETUP
    // ========================================================================

    private void completeSetup() {
        showLoading(true);

        Task<Void> setupTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // 1. Apply state configuration
                updateMessage("Applying state configuration...");
                stateConfigService.applyStateConfiguration(selectedState);

                // 2. Save district settings
                updateMessage("Saving district settings...");
                saveDistrictSettings();

                // 3. Import course catalog if selected
                if (importCourseCatalogCheck != null && importCourseCatalogCheck.isSelected()) {
                    updateMessage("Loading state course catalog...");
                    // Note: Actual import would require a file, this just prepares the system
                }

                // 4. Mark setup as complete
                updateMessage("Finalizing setup...");
                markSetupComplete();

                return null;
            }
        };

        setupTask.setOnSucceeded(e -> {
            showLoading(false);
            showSuccess();
        });

        setupTask.setOnFailed(e -> {
            showLoading(false);
            log.error("Setup failed", setupTask.getException());
            showError("Setup Failed", "An error occurred during setup: " + setupTask.getException().getMessage());
        });

        new Thread(setupTask).start();
    }

    private void saveDistrictSettings() {
        DistrictSettings settings = districtSettingsService.getOrCreateDistrictSettings();

        // State configuration
        settings.setUsState(selectedState);
        settings.setStateConfiguration(stateConfig);
        settings.setDistrictState(selectedState.getDisplayName());

        // District information
        if (districtNameField != null) {
            settings.setDistrictName(districtNameField.getText().trim());
        }
        if (districtAddressField != null) {
            settings.setDistrictAddress(districtAddressField.getText().trim());
        }
        if (districtCityField != null) {
            settings.setDistrictCity(districtCityField.getText().trim());
        }
        if (districtZipField != null) {
            settings.setDistrictZip(districtZipField.getText().trim());
        }
        if (districtPhoneField != null) {
            settings.setDistrictPhone(districtPhoneField.getText().trim());
        }
        if (districtEmailField != null) {
            settings.setDistrictEmail(districtEmailField.getText().trim());
        }
        if (districtWebsiteField != null) {
            settings.setDistrictWebsite(districtWebsiteField.getText().trim());
        }
        if (stateDistrictIdField != null) {
            settings.setStateDistrictId(stateDistrictIdField.getText().trim());
        }

        districtSettingsService.save(settings);
    }

    private void markSetupComplete() {
        DistrictSettings settings = districtSettingsService.getOrCreateDistrictSettings();
        settings.setStateSetupCompleted(true);
        districtSettingsService.save(settings);
    }

    private void showSuccess() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Setup Complete");
        alert.setHeaderText("Heronix SIS is ready to use!");

        StringBuilder content = new StringBuilder();
        content.append("Your system has been configured for ").append(selectedState.getDisplayName()).append(".\n\n");
        content.append("The following state-specific settings have been applied:\n");
        content.append("• Course coding system: ").append(stateConfig.getCourseCodeSystemName()).append("\n");
        content.append("• Student ID format: ").append(stateConfig.getStudentIdSystemName()).append("\n");
        content.append("• Grading scale configured\n");
        content.append("• State terminology applied\n\n");
        content.append("You can modify these settings at any time in System Settings.");

        alert.setContentText(content.toString());
        alert.showAndWait();

        if (stage != null) {
            stage.close();
        }
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    private void showLoading(boolean show) {
        Platform.runLater(() -> {
            if (loadingIndicator != null) {
                loadingIndicator.setVisible(show);
            }
            if (nextButton != null) nextButton.setDisable(show);
            if (prevButton != null) prevButton.setDisable(show);
            if (finishButton != null) finishButton.setDisable(show);
        });
    }

    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // ========================================================================
    // PUBLIC API FOR PROGRAMMATIC SETUP
    // ========================================================================

    /**
     * Programmatically set up the system for a state
     * Used for testing or automated deployment
     */
    public void setupForState(USState state, String districtName) {
        this.selectedState = state;
        this.stateConfig = stateConfigService.applyStateConfiguration(state);

        DistrictSettings settings = districtSettingsService.getOrCreateDistrictSettings();
        settings.setUsState(state);
        settings.setStateConfiguration(stateConfig);
        settings.setDistrictName(districtName);
        settings.setDistrictState(state.getDisplayName());
        settings.setStateSetupCompleted(true);
        districtSettingsService.save(settings);

        log.info("System configured for {} - {}", state.getDisplayName(), districtName);
    }

    /**
     * Get the current state configuration
     */
    public StateConfiguration getStateConfiguration() {
        return stateConfig;
    }

    /**
     * Get the selected state
     */
    public USState getSelectedState() {
        return selectedState;
    }
}
