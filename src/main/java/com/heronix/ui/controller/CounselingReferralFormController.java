package com.heronix.ui.controller;

import com.heronix.model.domain.CounselingReferral;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.Teacher;
import com.heronix.service.CounselingManagementService;
import com.heronix.service.StudentService;
import com.heronix.service.TeacherService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Counseling Referral Form
 * Handles student counseling service referrals with risk assessment
 */
@Component
public class CounselingReferralFormController {

    private static final Logger logger = LoggerFactory.getLogger(CounselingReferralFormController.class);

    @Autowired
    private CounselingManagementService counselingManagementService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    // Student Information
    @FXML private ComboBox<Student> studentComboBox;
    @FXML private TextField gradeField;
    @FXML private TextField ageField;
    @FXML private TextField genderField;

    // Referral Information
    @FXML private DatePicker referralDatePicker;
    @FXML private ComboBox<String> referralSourceComboBox;
    @FXML private ComboBox<Teacher> referringStaffComboBox;
    @FXML private ComboBox<String> urgencyLevelComboBox;
    @FXML private ComboBox<String> referralTypeComboBox;
    @FXML private ComboBox<Teacher> assignedCounselorComboBox;

    // Primary Concern
    @FXML private ComboBox<String> primaryConcernComboBox;
    @FXML private TextField secondaryConcernsField;

    // Detailed Description
    @FXML private TextArea reasonTextArea;
    @FXML private TextArea strengthsTextArea;

    // Risk Assessment
    @FXML private CheckBox suicideRiskCheckBox;
    @FXML private CheckBox harmToOthersCheckBox;
    @FXML private CheckBox immediateSafetyCheckBox;
    @FXML private CheckBox crisisInterventionCheckBox;
    @FXML private CheckBox safetyPlanCheckBox;
    @FXML private CheckBox mandatedReportingCheckBox;
    @FXML private VBox riskDetailsBox;
    @FXML private TextArea riskDetailsTextArea;

    // Parent Information
    @FXML private CheckBox parentAwareCheckBox;
    @FXML private GridPane parentInfoGrid;
    @FXML private DatePicker parentContactDatePicker;
    @FXML private CheckBox parentConsentCheckBox;
    @FXML private CheckBox parentMeetingCheckBox;
    @FXML private TextArea parentNotesTextArea;

    // Previous Services
    @FXML private CheckBox previousCounselingCheckBox;
    @FXML private CheckBox externalTherapyCheckBox;
    @FXML private CheckBox medicationCheckBox;
    @FXML private CheckBox hospitalHistoryCheckBox;
    @FXML private CheckBox iepCheckBox;
    @FXML private CheckBox bipCheckBox;
    @FXML private TextArea previousServicesTextArea;

    // Buttons
    @FXML private Label urgencyIndicator;
    @FXML private Button submitButton;
    @FXML private Button saveDraftButton;
    @FXML private Button cancelButton;

    private CounselingReferral currentReferral;

    @FXML
    public void initialize() {
        logger.info("Initializing Counseling Referral Form Controller");

        // Set defaults
        referralDatePicker.setValue(LocalDate.now());

        // Load data
        loadStudents();
        loadStaff();
        loadCounselors();

        // Configure student combo box
        studentComboBox.setConverter(new javafx.util.StringConverter<Student>() {
            @Override
            public String toString(Student student) {
                return student == null ? "" : student.getFirstName() + " " + student.getLastName() +
                       " (ID: " + student.getStudentId() + ")";
            }

            @Override
            public Student fromString(String string) {
                return null;
            }
        });

        // Auto-populate student info when selected
        studentComboBox.setOnAction(e -> {
            Student student = studentComboBox.getValue();
            if (student != null) {
                gradeField.setText(String.valueOf(student.getGradeLevel()));
                genderField.setText(student.getGender() != null ? student.getGender().toString() : "");

                // Calculate age
                if (student.getDateOfBirth() != null) {
                    int age = Period.between(student.getDateOfBirth(), LocalDate.now()).getYears();
                    ageField.setText(String.valueOf(age));
                }
            }
        });

        // Configure teacher combo boxes
        javafx.util.StringConverter<Teacher> teacherConverter = new javafx.util.StringConverter<Teacher>() {
            @Override
            public String toString(Teacher teacher) {
                return teacher == null ? "" : teacher.getFirstName() + " " + teacher.getLastName();
            }

            @Override
            public Teacher fromString(String string) {
                return null;
            }
        };

        referringStaffComboBox.setConverter(teacherConverter);
        assignedCounselorComboBox.setConverter(teacherConverter);

        // Show/hide risk details box based on risk indicators
        suicideRiskCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> updateRiskDetailsVisibility());
        harmToOthersCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> updateRiskDetailsVisibility());
        immediateSafetyCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> updateRiskDetailsVisibility());

        // Enable/disable parent info grid
        parentAwareCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            parentInfoGrid.setDisable(!newVal);
            if (newVal && parentContactDatePicker.getValue() == null) {
                parentContactDatePicker.setValue(LocalDate.now());
            }
        });

        // Update urgency indicator
        urgencyLevelComboBox.setOnAction(e -> updateUrgencyIndicator());
    }

    private void loadStudents() {
        try {
            List<Student> students = studentService.getAllStudents();
            studentComboBox.setItems(FXCollections.observableArrayList(students));
        } catch (Exception e) {
            logger.error("Error loading students", e);
            showError("Failed to load students: " + e.getMessage());
        }
    }

    private void loadStaff() {
        try {
            List<Teacher> teachers = teacherService.getAllTeachers();
            referringStaffComboBox.setItems(FXCollections.observableArrayList(teachers));
        } catch (Exception e) {
            logger.error("Error loading staff", e);
            showError("Failed to load staff: " + e.getMessage());
        }
    }

    private void loadCounselors() {
        try {
            List<Teacher> teachers = teacherService.getAllTeachers();
            // Filter for counselors
            List<Teacher> counselors = teachers.stream()
                .filter(t -> t.getRole() != null && t.getRole() == com.heronix.model.enums.TeacherRole.COUNSELOR)
                .toList();
            assignedCounselorComboBox.setItems(FXCollections.observableArrayList(counselors));
        } catch (Exception e) {
            logger.error("Error loading counselors", e);
            showError("Failed to load counselors: " + e.getMessage());
        }
    }

    private void updateRiskDetailsVisibility() {
        boolean hasRisk = suicideRiskCheckBox.isSelected() ||
                         harmToOthersCheckBox.isSelected() ||
                         immediateSafetyCheckBox.isSelected();
        riskDetailsBox.setVisible(hasRisk);
        riskDetailsBox.setManaged(hasRisk);
    }

    private void updateUrgencyIndicator() {
        String urgency = urgencyLevelComboBox.getValue();
        if (urgency != null) {
            switch (urgency) {
                case "EMERGENCY":
                    urgencyIndicator.setText("⚠ EMERGENCY - IMMEDIATE RESPONSE REQUIRED");
                    urgencyIndicator.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold; -fx-font-size: 14px;");
                    break;
                case "URGENT":
                    urgencyIndicator.setText("⚠ URGENT - Response within 24 hours");
                    urgencyIndicator.setStyle("-fx-text-fill: #f57c00; -fx-font-weight: bold;");
                    break;
                case "MODERATE":
                    urgencyIndicator.setText("MODERATE - Response within 1 week");
                    urgencyIndicator.setStyle("-fx-text-fill: #fbc02d;");
                    break;
                default:
                    urgencyIndicator.setText("");
            }
        }
    }

    @FXML
    private void handleSubmit() {
        if (validateForm()) {
            // Additional validation for high-risk cases
            if (isHighRisk() && !confirmHighRiskSubmission()) {
                return;
            }

            try {
                CounselingReferral referral = buildReferralFromForm();
                referral.setReferralStatus(CounselingReferral.ReferralStatus.PENDING);

                CounselingReferral saved = counselingManagementService.createReferral(referral);

                logger.info("Submitted counseling referral: {}", saved.getId());

                // Show success with special handling for high-risk cases
                if (isHighRisk()) {
                    showWarning("High-Risk Referral Submitted",
                              "This referral has been flagged for immediate counselor review.\n" +
                              "Referral ID: " + saved.getId() + "\n\n" +
                              "Assigned counselor has been notified.");
                } else {
                    showSuccess("Counseling referral submitted successfully. Referral ID: " + saved.getId());
                }

                closeWindow();
            } catch (Exception e) {
                logger.error("Error submitting referral", e);
                showError("Failed to submit referral: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSaveDraft() {
        try {
            CounselingReferral referral = buildReferralFromForm();
            // Use PENDING status as there's no DRAFT status
            referral.setReferralStatus(CounselingReferral.ReferralStatus.PENDING);

            CounselingReferral saved = counselingManagementService.createReferral(referral);

            logger.info("Saved counseling referral draft: {}", saved.getId());
            showSuccess("Counseling referral draft saved successfully");
            closeWindow();
        } catch (Exception e) {
            logger.error("Error saving draft", e);
            showError("Failed to save draft: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Cancel");
        alert.setHeaderText("Discard Referral?");
        alert.setContentText("Any unsaved changes will be lost. Are you sure?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                closeWindow();
            }
        });
    }

    private boolean validateForm() {
        List<String> errors = new ArrayList<>();

        if (studentComboBox.getValue() == null) {
            errors.add("Student is required");
        }
        if (referralDatePicker.getValue() == null) {
            errors.add("Referral date is required");
        }
        if (referralSourceComboBox.getValue() == null) {
            errors.add("Referral source is required");
        }
        if (urgencyLevelComboBox.getValue() == null) {
            errors.add("Urgency level is required");
        }
        if (referralTypeComboBox.getValue() == null) {
            errors.add("Referral type is required");
        }
        if (primaryConcernComboBox.getValue() == null) {
            errors.add("Primary concern is required");
        }
        if (reasonTextArea.getText() == null || reasonTextArea.getText().trim().isEmpty()) {
            errors.add("Reason for referral is required");
        }

        // Validate risk details if any risk indicators are checked
        if (riskDetailsBox.isVisible() && (riskDetailsTextArea.getText() == null ||
            riskDetailsTextArea.getText().trim().isEmpty())) {
            errors.add("Risk details are required when risk indicators are checked");
        }

        if (!errors.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please correct the following errors:");
            alert.setContentText(String.join("\n", errors));
            alert.showAndWait();
            return false;
        }

        return true;
    }

    private boolean isHighRisk() {
        return suicideRiskCheckBox.isSelected() ||
               harmToOthersCheckBox.isSelected() ||
               immediateSafetyCheckBox.isSelected() ||
               "EMERGENCY".equals(urgencyLevelComboBox.getValue());
    }

    private boolean confirmHighRiskSubmission() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("High-Risk Referral Confirmation");
        alert.setHeaderText("This referral contains HIGH-RISK indicators");
        alert.setContentText("High-risk indicators detected:\n" +
            (suicideRiskCheckBox.isSelected() ? "• Suicide Risk\n" : "") +
            (harmToOthersCheckBox.isSelected() ? "• Harm to Others\n" : "") +
            (immediateSafetyCheckBox.isSelected() ? "• Immediate Safety Concerns\n" : "") +
            "\nAn assigned counselor will be immediately notified.\n\n" +
            "Have you taken immediate safety measures?\n" +
            "Proceed with submission?");

        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    private CounselingReferral buildReferralFromForm() {
        CounselingReferral referral = currentReferral != null ? currentReferral : new CounselingReferral();

        referral.setStudent(studentComboBox.getValue());
        referral.setReferralDate(referralDatePicker.getValue());
        referral.setReferralSource(CounselingReferral.ReferralSource.valueOf(referralSourceComboBox.getValue()));
        referral.setReferringTeacher(referringStaffComboBox.getValue());
        referral.setUrgencyLevel(CounselingReferral.UrgencyLevel.valueOf(urgencyLevelComboBox.getValue()));
        referral.setReferralType(CounselingReferral.ReferralType.valueOf(referralTypeComboBox.getValue()));
        referral.setAssignedCounselor(assignedCounselorComboBox.getValue());
        referral.setPrimaryConcern(CounselingReferral.ConcernArea.valueOf(primaryConcernComboBox.getValue()));
        referral.setPresentingConcerns(secondaryConcernsField.getText());
        referral.setReferralReason(reasonTextArea.getText());
        referral.setBackgroundInformation(strengthsTextArea.getText());

        // Risk assessment
        referral.setSuicideRiskIndicated(suicideRiskCheckBox.isSelected());
        referral.setHarmToOthersIndicated(harmToOthersCheckBox.isSelected());
        referral.setImmediateSafetyConcerns(immediateSafetyCheckBox.isSelected());
        referral.setCrisisInterventionNeeded(crisisInterventionCheckBox.isSelected());
        referral.setSafetyPlanCreated(safetyPlanCheckBox.isSelected());
        referral.setMandatedReporting(mandatedReportingCheckBox.isSelected());
        referral.setRiskDetails(riskDetailsTextArea.getText());

        // Risk level determination
        if (suicideRiskCheckBox.isSelected() || harmToOthersCheckBox.isSelected() || immediateSafetyCheckBox.isSelected()) {
            referral.setRiskLevel(CounselingReferral.RiskLevel.IMMINENT);
        } else if (crisisInterventionCheckBox.isSelected()) {
            referral.setRiskLevel(CounselingReferral.RiskLevel.HIGH);
        } else {
            referral.setRiskLevel(CounselingReferral.RiskLevel.LOW);
        }

        // Parent information
        referral.setParentContacted(parentAwareCheckBox.isSelected());
        referral.setParentContactDate(parentContactDatePicker.getValue());
        referral.setParentConsentObtained(parentConsentCheckBox.isSelected());
        referral.setParentMeetingHeld(parentMeetingCheckBox.isSelected());
        referral.setParentNotes(parentNotesTextArea.getText());

        // Previous services
        referral.setPreviousCounseling(previousCounselingCheckBox.isSelected());
        referral.setExternalTherapy(externalTherapyCheckBox.isSelected());
        referral.setCurrentMedication(medicationCheckBox.isSelected());
        referral.setHospitalizationHistory(hospitalHistoryCheckBox.isSelected());
        referral.setHasIepOr504(iepCheckBox.isSelected());
        referral.setHasBehaviorPlan(bipCheckBox.isSelected());
        referral.setPreviousServicesDetails(previousServicesTextArea.getText());

        return referral;
    }

    public void setReferral(CounselingReferral referral) {
        this.currentReferral = referral;
        if (referral != null) {
            populateForm(referral);
        }
    }

    private void populateForm(CounselingReferral referral) {
        studentComboBox.setValue(referral.getStudent());
        referralDatePicker.setValue(referral.getReferralDate());
        if (referral.getReferralSource() != null) {
            referralSourceComboBox.setValue(referral.getReferralSource().name());
        }
        referringStaffComboBox.setValue(referral.getReferringStaff());
        if (referral.getUrgencyLevel() != null) {
            urgencyLevelComboBox.setValue(referral.getUrgencyLevel().name());
        }
        if (referral.getReferralType() != null) {
            referralTypeComboBox.setValue(referral.getReferralType().name());
        }
        assignedCounselorComboBox.setValue(referral.getAssignedCounselor());
        if (referral.getPrimaryConcern() != null) {
            primaryConcernComboBox.setValue(referral.getPrimaryConcern().name());
        }
        secondaryConcernsField.setText(referral.getSecondaryConcerns());
        reasonTextArea.setText(referral.getReferralReason());
        strengthsTextArea.setText(referral.getStudentStrengths());

        suicideRiskCheckBox.setSelected(referral.isSuicideRiskIndicated());
        harmToOthersCheckBox.setSelected(referral.isHarmToOthersIndicated());
        immediateSafetyCheckBox.setSelected(referral.isImmediateSafetyConcerns());
        crisisInterventionCheckBox.setSelected(referral.isCrisisInterventionNeeded());
        safetyPlanCheckBox.setSelected(referral.isSafetyPlanCreated());
        mandatedReportingCheckBox.setSelected(referral.isMandatedReporting());
        riskDetailsTextArea.setText(referral.getRiskDetails());

        parentAwareCheckBox.setSelected(referral.isParentContacted());
        parentContactDatePicker.setValue(referral.getParentContactDate());
        parentConsentCheckBox.setSelected(referral.isParentConsentObtained());
        parentMeetingCheckBox.setSelected(referral.isParentMeetingHeld());
        parentNotesTextArea.setText(referral.getParentNotes());

        previousCounselingCheckBox.setSelected(referral.isPreviousCounseling());
        externalTherapyCheckBox.setSelected(referral.isExternalTherapy());
        medicationCheckBox.setSelected(referral.isCurrentMedication());
        hospitalHistoryCheckBox.setSelected(referral.isHospitalizationHistory());
        iepCheckBox.setSelected(referral.isHasIepOr504());
        bipCheckBox.setSelected(referral.isHasBehaviorPlan());
        previousServicesTextArea.setText(referral.getPreviousServicesDetails());
    }

    /**
     * Alias for setReferral - used by CounselingDashboardController
     */
    public void loadReferral(CounselingReferral referral) {
        setReferral(referral);
    }

    /**
     * Set the form to view-only mode (disable editing)
     */
    public void setViewMode(boolean viewMode) {
        if (viewMode) {
            // Disable all input fields
            studentComboBox.setDisable(true);
            referralDatePicker.setDisable(true);
            referralSourceComboBox.setDisable(true);
            referringStaffComboBox.setDisable(true);
            urgencyLevelComboBox.setDisable(true);
            referralTypeComboBox.setDisable(true);
            assignedCounselorComboBox.setDisable(true);
            primaryConcernComboBox.setDisable(true);
            secondaryConcernsField.setDisable(true);
            reasonTextArea.setDisable(true);
            strengthsTextArea.setDisable(true);

            suicideRiskCheckBox.setDisable(true);
            harmToOthersCheckBox.setDisable(true);
            immediateSafetyCheckBox.setDisable(true);
            crisisInterventionCheckBox.setDisable(true);
            safetyPlanCheckBox.setDisable(true);
            mandatedReportingCheckBox.setDisable(true);
            riskDetailsTextArea.setDisable(true);

            parentAwareCheckBox.setDisable(true);
            parentContactDatePicker.setDisable(true);
            parentConsentCheckBox.setDisable(true);
            parentMeetingCheckBox.setDisable(true);
            parentNotesTextArea.setDisable(true);

            previousCounselingCheckBox.setDisable(true);
            externalTherapyCheckBox.setDisable(true);
            medicationCheckBox.setDisable(true);
            hospitalHistoryCheckBox.setDisable(true);
            iepCheckBox.setDisable(true);
            bipCheckBox.setDisable(true);
            previousServicesTextArea.setDisable(true);

            // Hide action buttons
            submitButton.setVisible(false);
            saveDraftButton.setVisible(false);
            cancelButton.setText("Close");
        }
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) submitButton.getScene().getWindow();
        stage.close();
    }
}
