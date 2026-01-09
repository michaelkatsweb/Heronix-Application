package com.heronix.ui.controller;

import com.heronix.model.domain.*;
import com.heronix.service.HealthOfficeService;
import com.heronix.service.StudentService;
import com.heronix.service.TeacherService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for Health Screening UI
 * Manages state-mandated health screenings including vision, hearing, scoliosis, BMI, and dental.
 * Supports documentation, parent notification, and professional referrals.
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Health Office Management System
 */
@Slf4j
@Component
public class HealthScreeningController {

    @Autowired
    private HealthOfficeService healthOfficeService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    // ========================================================================
    // FXML FIELDS - Student Information
    // ========================================================================

    @FXML private ComboBox<Student> studentComboBox;
    @FXML private TextField gradeField;
    @FXML private TextField dateOfBirthField;
    @FXML private TextField ageField;

    // ========================================================================
    // FXML FIELDS - Screening Details
    // ========================================================================

    @FXML private ComboBox<String> screeningTypeComboBox;
    @FXML private DatePicker screeningDatePicker;
    @FXML private ComboBox<Teacher> screenedByComboBox;
    @FXML private TextField locationField;
    @FXML private TextField gradeLevelField;

    // ========================================================================
    // FXML FIELDS - Vision Screening
    // ========================================================================

    @FXML private VBox visionScreeningBox;
    @FXML private TextField rightEyeVisionField;
    @FXML private TextField leftEyeVisionField;
    @FXML private TextField bothEyesVisionField;
    @FXML private CheckBox wearsGlassesCheckBox;
    @FXML private CheckBox visionConcernsCheckBox;
    @FXML private TextArea visionNotesArea;

    // ========================================================================
    // FXML FIELDS - Hearing Screening
    // ========================================================================

    @FXML private VBox hearingScreeningBox;
    @FXML private ComboBox<String> rightEarResultComboBox;
    @FXML private ComboBox<String> leftEarResultComboBox;
    @FXML private TextField rightEarFrequenciesField;
    @FXML private TextField leftEarFrequenciesField;
    @FXML private CheckBox usesHearingAidCheckBox;
    @FXML private CheckBox hearingConcernsCheckBox;
    @FXML private TextArea hearingNotesArea;

    // ========================================================================
    // FXML FIELDS - Scoliosis Screening
    // ========================================================================

    @FXML private VBox scoliosisScreeningBox;
    @FXML private ComboBox<String> adamsBendTestComboBox;
    @FXML private ComboBox<String> ribHumpComboBox;
    @FXML private CheckBox shoulderAsymmetryCheckBox;
    @FXML private CheckBox hipAsymmetryCheckBox;
    @FXML private CheckBox spinalCurvatureCheckBox;
    @FXML private TextArea scoliosisFindingsArea;

    // ========================================================================
    // FXML FIELDS - BMI Screening
    // ========================================================================

    @FXML private VBox bmiScreeningBox;
    @FXML private TextField heightField;
    @FXML private TextField weightField;
    @FXML private TextField bmiField;
    @FXML private TextField bmiPercentileField;
    @FXML private ComboBox<String> bmiCategoryComboBox;
    @FXML private TextArea bmiNotesArea;

    // ========================================================================
    // FXML FIELDS - Dental Screening
    // ========================================================================

    @FXML private VBox dentalScreeningBox;
    @FXML private CheckBox obviousCavitiesCheckBox;
    @FXML private CheckBox gumDiseaseCheckBox;
    @FXML private CheckBox oralPainCheckBox;
    @FXML private CheckBox needsDentalCareCheckBox;
    @FXML private TextArea dentalFindingsArea;

    // ========================================================================
    // FXML FIELDS - Overall Results
    // ========================================================================

    @FXML private ComboBox<String> overallResultComboBox;
    @FXML private CheckBox meetsStateRequirementsCheckBox;

    // ========================================================================
    // FXML FIELDS - Parent Notification
    // ========================================================================

    @FXML private CheckBox parentNotifiedCheckBox;
    @FXML private CheckBox referralProvidedCheckBox;
    @FXML private DatePicker notificationDatePicker;
    @FXML private ComboBox<String> notificationMethodComboBox;
    @FXML private TextArea parentNotificationNotesArea;

    // ========================================================================
    // FXML FIELDS - Follow-Up & Referrals
    // ========================================================================

    @FXML private VBox followUpBox;
    @FXML private ComboBox<String> referralTypeComboBox;
    @FXML private DatePicker followUpDatePicker;
    @FXML private TextArea referralReasonArea;
    @FXML private TextArea followUpInstructionsArea;

    // ========================================================================
    // FXML FIELDS - Notes
    // ========================================================================

    @FXML private TextArea notesArea;

    // ========================================================================
    // STATE
    // ========================================================================

    private Student currentStudent;
    private HealthScreening currentScreening;
    private boolean viewMode = false;

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    @FXML
    public void initialize() {
        log.info("Initializing HealthScreeningController");
        setupComboBoxes();
        setupConditionalVisibility();
        setupCalculations();
        loadInitialData();
        setupDefaults();
    }

    private void setupComboBoxes() {
        // Student ComboBox
        studentComboBox.setConverter(new StringConverter<Student>() {
            @Override
            public String toString(Student student) {
                if (student == null) return "";
                return String.format("%s, %s (%s) - Grade %s",
                        student.getLastName(),
                        student.getFirstName(),
                        student.getStudentId(),
                        student.getGradeLevel() != null ? student.getGradeLevel() : "N/A");
            }

            @Override
            public Student fromString(String string) {
                return null;
            }
        });

        // Screened By ComboBox (School nurses and health staff)
        screenedByComboBox.setConverter(new StringConverter<Teacher>() {
            @Override
            public String toString(Teacher teacher) {
                if (teacher == null) return "";
                return teacher.getFirstName() + " " + teacher.getLastName();
            }

            @Override
            public Teacher fromString(String string) {
                return null;
            }
        });

        // Student selection handler
        studentComboBox.setOnAction(e -> {
            Student selected = studentComboBox.getValue();
            if (selected != null) {
                setStudent(selected);
            }
        });

        // Screening type selection handler
        screeningTypeComboBox.setOnAction(e -> updateScreeningTypeVisibility());

        // Overall result handler for follow-up visibility
        overallResultComboBox.setOnAction(e -> updateFollowUpVisibility());
    }

    private void setupConditionalVisibility() {
        // Initially hide all screening-specific sections
        visionScreeningBox.setVisible(false);
        visionScreeningBox.setManaged(false);

        hearingScreeningBox.setVisible(false);
        hearingScreeningBox.setManaged(false);

        scoliosisScreeningBox.setVisible(false);
        scoliosisScreeningBox.setManaged(false);

        bmiScreeningBox.setVisible(false);
        bmiScreeningBox.setManaged(false);

        dentalScreeningBox.setVisible(false);
        dentalScreeningBox.setManaged(false);

        followUpBox.setVisible(false);
        followUpBox.setManaged(false);
    }

    private void setupCalculations() {
        // Auto-calculate BMI when height or weight changes
        heightField.textProperty().addListener((observable, oldValue, newValue) -> calculateBMI());
        weightField.textProperty().addListener((observable, oldValue, newValue) -> calculateBMI());
    }

    private void loadInitialData() {
        // Load all students
        List<Student> students = studentService.getAllStudents();
        studentComboBox.setItems(FXCollections.observableArrayList(students));

        // Load school nurses and health staff
        List<Teacher> healthStaff = teacherService.findAll().stream()
                .filter(t -> t.getDepartment() != null &&
                        (t.getDepartment().contains("Nurse") ||
                         t.getDepartment().contains("Health") ||
                         t.getDepartment().contains("RN")))
                .collect(Collectors.toList());
        screenedByComboBox.setItems(FXCollections.observableArrayList(healthStaff));
    }

    private void setupDefaults() {
        // Default dates
        screeningDatePicker.setValue(LocalDate.now());
        meetsStateRequirementsCheckBox.setSelected(true);
    }

    // ========================================================================
    // PUBLIC METHODS FOR EXTERNAL INTEGRATION
    // ========================================================================

    /**
     * Set the student for health screening
     */
    public void setStudent(Student student) {
        this.currentStudent = student;
        studentComboBox.setValue(student);

        // Populate student information
        if (student.getGradeLevel() != null) {
            gradeField.setText(student.getGradeLevel().toString());
            gradeLevelField.setText(student.getGradeLevel().toString());
        }

        if (student.getDateOfBirth() != null) {
            dateOfBirthField.setText(student.getDateOfBirth().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            int age = LocalDate.now().getYear() - student.getDateOfBirth().getYear();
            ageField.setText(age + " years");
        }
    }

    /**
     * Load existing screening for editing
     */
    public void loadScreening(HealthScreening screening) {
        this.currentScreening = screening;

        // Set student first
        setStudent(screening.getStudent());

        // Screening Details
        screeningTypeComboBox.setValue(screening.getScreeningType().name());
        screeningDatePicker.setValue(screening.getScreeningDate());
        locationField.setText(screening.getLocation());
        if (screening.getGradeLevelAtScreening() != null) {
            gradeLevelField.setText(screening.getGradeLevelAtScreening().toString());
        }

        // Vision Screening
        if (screening.getScreeningType() == HealthScreening.ScreeningType.VISION) {
            rightEyeVisionField.setText(screening.getRightEyeVision());
            leftEyeVisionField.setText(screening.getLeftEyeVision());
            bothEyesVisionField.setText(screening.getBothEyesVision());
            wearsGlassesCheckBox.setSelected(screening.getWearsGlasses());
            visionConcernsCheckBox.setSelected(screening.getVisionConcerns());
            visionNotesArea.setText(screening.getVisionNotes());
        }

        // Hearing Screening
        if (screening.getScreeningType() == HealthScreening.ScreeningType.HEARING) {
            if (screening.getRightEarResult() != null) {
                rightEarResultComboBox.setValue(screening.getRightEarResult());
            }
            if (screening.getLeftEarResult() != null) {
                leftEarResultComboBox.setValue(screening.getLeftEarResult());
            }
            rightEarFrequenciesField.setText(screening.getRightEarFrequencies());
            leftEarFrequenciesField.setText(screening.getLeftEarFrequencies());
            usesHearingAidCheckBox.setSelected(screening.getUsesHearingAid());
            hearingConcernsCheckBox.setSelected(screening.getHearingConcerns());
            hearingNotesArea.setText(screening.getHearingNotes());
        }

        // Scoliosis Screening
        if (screening.getScreeningType() == HealthScreening.ScreeningType.SCOLIOSIS) {
            if (screening.getAdamsBendTest() != null) {
                adamsBendTestComboBox.setValue(screening.getAdamsBendTest().name());
            }
            if (screening.getRibHump() != null) {
                ribHumpComboBox.setValue(screening.getRibHump().name());
            }
            shoulderAsymmetryCheckBox.setSelected(screening.getShoulderAsymmetry());
            hipAsymmetryCheckBox.setSelected(screening.getHipAsymmetry());
            spinalCurvatureCheckBox.setSelected(screening.getSpinalCurvature());
            scoliosisFindingsArea.setText(screening.getScoliosisFindings());
        }

        // BMI Screening
        if (screening.getScreeningType() == HealthScreening.ScreeningType.BMI) {
            if (screening.getHeight() != null) {
                heightField.setText(String.valueOf(screening.getHeight()));
            }
            if (screening.getWeight() != null) {
                weightField.setText(String.valueOf(screening.getWeight()));
            }
            if (screening.getBmi() != null) {
                bmiField.setText(String.format("%.1f", screening.getBmi()));
            }
            bmiPercentileField.setText(screening.getBmiPercentile() != null ? screening.getBmiPercentile().toString() : "");
            if (screening.getBmiCategory() != null) {
                bmiCategoryComboBox.setValue(screening.getBmiCategory().name());
            }
            bmiNotesArea.setText(screening.getBmiNotes());
        }

        // Dental Screening
        if (screening.getScreeningType() == HealthScreening.ScreeningType.DENTAL) {
            obviousCavitiesCheckBox.setSelected(screening.getObviousCavities());
            gumDiseaseCheckBox.setSelected(screening.getGumDisease());
            oralPainCheckBox.setSelected(screening.getOralPain());
            needsDentalCareCheckBox.setSelected(screening.getNeedsDentalCare());
            dentalFindingsArea.setText(screening.getDentalFindings());
        }

        // Overall Results
        if (screening.getOverallResult() != null) {
            overallResultComboBox.setValue(screening.getOverallResult().name());
        }
        meetsStateRequirementsCheckBox.setSelected(screening.getMeetsStateRequirements());

        // Parent Notification
        parentNotifiedCheckBox.setSelected(screening.getParentNotified());
        referralProvidedCheckBox.setSelected(screening.getReferralProvided());
        if (screening.getNotificationDate() != null) {
            notificationDatePicker.setValue(screening.getNotificationDate());
        }
        if (screening.getNotificationMethod() != null) {
            notificationMethodComboBox.setValue(screening.getNotificationMethod().name());
        }
        parentNotificationNotesArea.setText(screening.getParentNotificationNotes());

        // Follow-Up & Referrals
        if (screening.getReferralType() != null) {
            referralTypeComboBox.setValue(screening.getReferralType().name());
        }
        if (screening.getFollowUpDate() != null) {
            followUpDatePicker.setValue(screening.getFollowUpDate());
        }
        referralReasonArea.setText(screening.getReferralReason());
        followUpInstructionsArea.setText(screening.getFollowUpInstructions());

        // Notes
        notesArea.setText(screening.getNotes());

        // Update visibility
        updateScreeningTypeVisibility();
        updateFollowUpVisibility();
    }

    /**
     * Set view mode (read-only)
     */
    public void setViewMode(boolean viewMode) {
        this.viewMode = viewMode;

        // Disable all input controls
        studentComboBox.setDisable(viewMode);
        screeningTypeComboBox.setDisable(viewMode);
        screeningDatePicker.setDisable(viewMode);
        screenedByComboBox.setDisable(viewMode);
        locationField.setDisable(viewMode);

        // Vision
        rightEyeVisionField.setDisable(viewMode);
        leftEyeVisionField.setDisable(viewMode);
        bothEyesVisionField.setDisable(viewMode);
        wearsGlassesCheckBox.setDisable(viewMode);
        visionConcernsCheckBox.setDisable(viewMode);
        visionNotesArea.setDisable(viewMode);

        // Hearing
        rightEarResultComboBox.setDisable(viewMode);
        leftEarResultComboBox.setDisable(viewMode);
        rightEarFrequenciesField.setDisable(viewMode);
        leftEarFrequenciesField.setDisable(viewMode);
        usesHearingAidCheckBox.setDisable(viewMode);
        hearingConcernsCheckBox.setDisable(viewMode);
        hearingNotesArea.setDisable(viewMode);

        // Scoliosis
        adamsBendTestComboBox.setDisable(viewMode);
        ribHumpComboBox.setDisable(viewMode);
        shoulderAsymmetryCheckBox.setDisable(viewMode);
        hipAsymmetryCheckBox.setDisable(viewMode);
        spinalCurvatureCheckBox.setDisable(viewMode);
        scoliosisFindingsArea.setDisable(viewMode);

        // BMI
        heightField.setDisable(viewMode);
        weightField.setDisable(viewMode);
        bmiPercentileField.setDisable(viewMode);
        bmiCategoryComboBox.setDisable(viewMode);
        bmiNotesArea.setDisable(viewMode);

        // Dental
        obviousCavitiesCheckBox.setDisable(viewMode);
        gumDiseaseCheckBox.setDisable(viewMode);
        oralPainCheckBox.setDisable(viewMode);
        needsDentalCareCheckBox.setDisable(viewMode);
        dentalFindingsArea.setDisable(viewMode);

        // Overall
        overallResultComboBox.setDisable(viewMode);
        meetsStateRequirementsCheckBox.setDisable(viewMode);

        // Parent Notification
        parentNotifiedCheckBox.setDisable(viewMode);
        referralProvidedCheckBox.setDisable(viewMode);
        notificationDatePicker.setDisable(viewMode);
        notificationMethodComboBox.setDisable(viewMode);
        parentNotificationNotesArea.setDisable(viewMode);

        // Follow-Up
        referralTypeComboBox.setDisable(viewMode);
        followUpDatePicker.setDisable(viewMode);
        referralReasonArea.setDisable(viewMode);
        followUpInstructionsArea.setDisable(viewMode);

        // Notes
        notesArea.setDisable(viewMode);
    }

    // ========================================================================
    // CONDITIONAL VISIBILITY METHODS
    // ========================================================================

    private void updateScreeningTypeVisibility() {
        String selectedType = screeningTypeComboBox.getValue();
        if (selectedType == null) return;

        // Hide all sections first
        visionScreeningBox.setVisible(false);
        visionScreeningBox.setManaged(false);
        hearingScreeningBox.setVisible(false);
        hearingScreeningBox.setManaged(false);
        scoliosisScreeningBox.setVisible(false);
        scoliosisScreeningBox.setManaged(false);
        bmiScreeningBox.setVisible(false);
        bmiScreeningBox.setManaged(false);
        dentalScreeningBox.setVisible(false);
        dentalScreeningBox.setManaged(false);

        // Show appropriate section
        switch (selectedType) {
            case "VISION":
                visionScreeningBox.setVisible(true);
                visionScreeningBox.setManaged(true);
                break;
            case "HEARING":
                hearingScreeningBox.setVisible(true);
                hearingScreeningBox.setManaged(true);
                break;
            case "SCOLIOSIS":
                scoliosisScreeningBox.setVisible(true);
                scoliosisScreeningBox.setManaged(true);
                break;
            case "BMI":
            case "HEIGHT_WEIGHT":
                bmiScreeningBox.setVisible(true);
                bmiScreeningBox.setManaged(true);
                break;
            case "DENTAL":
                dentalScreeningBox.setVisible(true);
                dentalScreeningBox.setManaged(true);
                break;
        }
    }

    private void updateFollowUpVisibility() {
        String overallResult = overallResultComboBox.getValue();
        boolean needsFollowUp = "REFER".equals(overallResult) ||
                                "FOLLOW_UP_NEEDED".equals(overallResult) ||
                                "RESCREEN".equals(overallResult);

        followUpBox.setVisible(needsFollowUp);
        followUpBox.setManaged(needsFollowUp);
    }

    // ========================================================================
    // CALCULATION METHODS
    // ========================================================================

    private void calculateBMI() {
        try {
            String heightStr = heightField.getText();
            String weightStr = weightField.getText();

            if (heightStr != null && !heightStr.trim().isEmpty() &&
                weightStr != null && !weightStr.trim().isEmpty()) {

                double height = Double.parseDouble(heightStr);
                double weight = Double.parseDouble(weightStr);

                // BMI = (weight in pounds / (height in inches)^2) * 703
                double bmi = (weight / (height * height)) * 703;

                bmiField.setText(String.format("%.1f", bmi));

                // Auto-suggest BMI category based on common ranges
                // Note: These are approximate adult ranges; pediatric BMI uses percentiles
                if (bmi < 18.5) {
                    bmiCategoryComboBox.setValue("UNDERWEIGHT");
                } else if (bmi < 25) {
                    bmiCategoryComboBox.setValue("HEALTHY_WEIGHT");
                } else if (bmi < 30) {
                    bmiCategoryComboBox.setValue("OVERWEIGHT");
                } else {
                    bmiCategoryComboBox.setValue("OBESE");
                }
            }
        } catch (NumberFormatException e) {
            // Invalid input, clear BMI field
            bmiField.clear();
        }
    }

    // ========================================================================
    // EVENT HANDLERS
    // ========================================================================

    @FXML
    private void handleSave() {
        if (viewMode) return;

        List<String> errors = validateForm();
        if (!errors.isEmpty()) {
            showValidationErrors(errors);
            return;
        }

        try {
            HealthScreening screening = buildScreeningFromForm();

            if (currentScreening == null) {
                // Create new screening
                healthOfficeService.createHealthScreening(screening);
                log.info("Created new health screening for student: {}", currentStudent.getStudentId());
                showSuccess("Health screening record created successfully!");
            } else {
                // Update existing screening
                screening.setId(currentScreening.getId());
                healthOfficeService.updateHealthScreening(screening);
                log.info("Updated health screening record ID: {}", currentScreening.getId());
                showSuccess("Health screening record updated successfully!");
            }

            handleClose();
        } catch (Exception e) {
            log.error("Error saving health screening", e);
            showError("Failed to save health screening: " + e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        if (viewMode) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Form");
        alert.setHeaderText("Clear all fields?");
        alert.setContentText("This will reset the form. Any unsaved changes will be lost.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                clearForm();
            }
        });
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) studentComboBox.getScene().getWindow();
        stage.close();
    }

    // ========================================================================
    // VALIDATION
    // ========================================================================

    private List<String> validateForm() {
        List<String> errors = new ArrayList<>();

        // Required fields
        if (studentComboBox.getValue() == null) {
            errors.add("Student is required");
        }

        if (screeningTypeComboBox.getValue() == null || screeningTypeComboBox.getValue().isEmpty()) {
            errors.add("Screening type is required");
        }

        if (screeningDatePicker.getValue() == null) {
            errors.add("Screening date is required");
        }

        if (screenedByComboBox.getValue() == null) {
            errors.add("Screened by staff member is required");
        }

        if (overallResultComboBox.getValue() == null || overallResultComboBox.getValue().isEmpty()) {
            errors.add("Overall result is required");
        }

        // Validate screening-specific fields
        String screeningType = screeningTypeComboBox.getValue();
        if (screeningType != null) {
            switch (screeningType) {
                case "VISION":
                    if ((rightEyeVisionField.getText() == null || rightEyeVisionField.getText().trim().isEmpty()) &&
                        (leftEyeVisionField.getText() == null || leftEyeVisionField.getText().trim().isEmpty()) &&
                        (bothEyesVisionField.getText() == null || bothEyesVisionField.getText().trim().isEmpty())) {
                        errors.add("At least one vision measurement is required for vision screening");
                    }
                    break;

                case "HEARING":
                    if ((rightEarResultComboBox.getValue() == null || rightEarResultComboBox.getValue().isEmpty()) &&
                        (leftEarResultComboBox.getValue() == null || leftEarResultComboBox.getValue().isEmpty())) {
                        errors.add("At least one ear result is required for hearing screening");
                    }
                    break;

                case "SCOLIOSIS":
                    if (adamsBendTestComboBox.getValue() == null || adamsBendTestComboBox.getValue().isEmpty()) {
                        errors.add("Adam's Forward Bend Test result is required for scoliosis screening");
                    }
                    break;

                case "BMI":
                case "HEIGHT_WEIGHT":
                    if (heightField.getText() == null || heightField.getText().trim().isEmpty()) {
                        errors.add("Height is required for BMI/Height-Weight screening");
                    }
                    if (weightField.getText() == null || weightField.getText().trim().isEmpty()) {
                        errors.add("Weight is required for BMI/Height-Weight screening");
                    }
                    break;
            }
        }

        // Validate follow-up fields if required
        String overallResult = overallResultComboBox.getValue();
        if (overallResult != null && ("REFER".equals(overallResult) || "FOLLOW_UP_NEEDED".equals(overallResult))) {
            if (referralTypeComboBox.getValue() == null || referralTypeComboBox.getValue().isEmpty()) {
                errors.add("Referral type is required when student needs follow-up");
            }
        }

        return errors;
    }

    private void showValidationErrors(List<String> errors) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation Error");
        alert.setHeaderText("Please correct the following errors:");
        alert.setContentText(String.join("\n", errors));
        alert.showAndWait();
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private HealthScreening buildScreeningFromForm() {
        HealthScreening screening = new HealthScreening();

        // Student
        screening.setStudent(studentComboBox.getValue());

        // Screening Details
        screening.setScreeningType(HealthScreening.ScreeningType.valueOf(screeningTypeComboBox.getValue()));
        screening.setScreeningDate(screeningDatePicker.getValue());
        if (screenedByComboBox.getValue() != null) {
            screening.setScreenedBy(screenedByComboBox.getValue().getFirstName() + " " +
                    screenedByComboBox.getValue().getLastName());
            screening.setScreenedByStaffId(screenedByComboBox.getValue().getId());
        }
        screening.setLocation(locationField.getText());
        if (!gradeLevelField.getText().trim().isEmpty()) {
            try {
                screening.setGradeLevelAtScreening(Integer.parseInt(gradeLevelField.getText()));
            } catch (NumberFormatException e) {
                log.warn("Invalid grade level: {}", gradeLevelField.getText());
            }
        }

        // Vision Screening
        screening.setRightEyeVision(rightEyeVisionField.getText());
        screening.setLeftEyeVision(leftEyeVisionField.getText());
        screening.setBothEyesVision(bothEyesVisionField.getText());
        screening.setWearsGlasses(wearsGlassesCheckBox.isSelected());
        screening.setVisionConcerns(visionConcernsCheckBox.isSelected());
        screening.setVisionNotes(visionNotesArea.getText());

        // Hearing Screening
        if (rightEarResultComboBox.getValue() != null && !rightEarResultComboBox.getValue().isEmpty()) {
            screening.setRightEarResult(rightEarResultComboBox.getValue());
        }
        if (leftEarResultComboBox.getValue() != null && !leftEarResultComboBox.getValue().isEmpty()) {
            screening.setLeftEarResult(leftEarResultComboBox.getValue());
        }
        screening.setRightEarFrequencies(rightEarFrequenciesField.getText());
        screening.setLeftEarFrequencies(leftEarFrequenciesField.getText());
        screening.setUsesHearingAid(usesHearingAidCheckBox.isSelected());
        screening.setHearingConcerns(hearingConcernsCheckBox.isSelected());
        screening.setHearingNotes(hearingNotesArea.getText());

        // Scoliosis Screening
        if (adamsBendTestComboBox.getValue() != null && !adamsBendTestComboBox.getValue().isEmpty()) {
            screening.setAdamsBendTest(HealthScreening.ScoliosisResult.valueOf(adamsBendTestComboBox.getValue()));
        }
        if (ribHumpComboBox.getValue() != null && !ribHumpComboBox.getValue().isEmpty()) {
            screening.setRibHump(HealthScreening.RibHumpSeverity.valueOf(ribHumpComboBox.getValue()));
        }
        screening.setShoulderAsymmetry(shoulderAsymmetryCheckBox.isSelected());
        screening.setHipAsymmetry(hipAsymmetryCheckBox.isSelected());
        screening.setSpinalCurvature(spinalCurvatureCheckBox.isSelected());
        screening.setScoliosisFindings(scoliosisFindingsArea.getText());

        // BMI Screening
        if (heightField.getText() != null && !heightField.getText().trim().isEmpty()) {
            screening.setHeight(Double.parseDouble(heightField.getText()));
        }
        if (weightField.getText() != null && !weightField.getText().trim().isEmpty()) {
            screening.setWeight(Double.parseDouble(weightField.getText()));
        }
        if (bmiField.getText() != null && !bmiField.getText().trim().isEmpty()) {
            screening.setBmi(Double.parseDouble(bmiField.getText()));
        }
        if (bmiPercentileField.getText() != null && !bmiPercentileField.getText().trim().isEmpty()) {
            screening.setBmiPercentile(Integer.parseInt(bmiPercentileField.getText()));
        }
        if (bmiCategoryComboBox.getValue() != null && !bmiCategoryComboBox.getValue().isEmpty()) {
            screening.setBmiCategory(HealthScreening.BMICategory.valueOf(bmiCategoryComboBox.getValue()));
        }
        screening.setBmiNotes(bmiNotesArea.getText());

        // Dental Screening
        screening.setObviousCavities(obviousCavitiesCheckBox.isSelected());
        screening.setGumDisease(gumDiseaseCheckBox.isSelected());
        screening.setOralPain(oralPainCheckBox.isSelected());
        screening.setNeedsDentalCare(needsDentalCareCheckBox.isSelected());
        screening.setDentalFindings(dentalFindingsArea.getText());

        // Overall Results
        screening.setOverallResult(HealthScreening.OverallResult.valueOf(overallResultComboBox.getValue()));
        screening.setMeetsStateRequirements(meetsStateRequirementsCheckBox.isSelected());

        // Parent Notification
        screening.setParentNotified(parentNotifiedCheckBox.isSelected());
        screening.setReferralProvided(referralProvidedCheckBox.isSelected());
        screening.setNotificationDate(notificationDatePicker.getValue());
        if (notificationMethodComboBox.getValue() != null && !notificationMethodComboBox.getValue().isEmpty()) {
            screening.setNotificationMethod(
                    HealthScreening.NotificationMethod.valueOf(notificationMethodComboBox.getValue()));
        }
        screening.setParentNotificationNotes(parentNotificationNotesArea.getText());

        // Follow-Up & Referrals
        if (referralTypeComboBox.getValue() != null && !referralTypeComboBox.getValue().isEmpty()) {
            screening.setReferralType(HealthScreening.ReferralType.valueOf(referralTypeComboBox.getValue()));
        }
        screening.setFollowUpDate(followUpDatePicker.getValue());
        screening.setReferralReason(referralReasonArea.getText());
        screening.setFollowUpInstructions(followUpInstructionsArea.getText());

        // Notes
        screening.setNotes(notesArea.getText());

        return screening;
    }

    private void clearForm() {
        currentScreening = null;

        screeningTypeComboBox.setValue(null);
        screeningDatePicker.setValue(LocalDate.now());
        screenedByComboBox.setValue(null);
        locationField.clear();
        gradeLevelField.clear();

        // Vision
        rightEyeVisionField.clear();
        leftEyeVisionField.clear();
        bothEyesVisionField.clear();
        wearsGlassesCheckBox.setSelected(false);
        visionConcernsCheckBox.setSelected(false);
        visionNotesArea.clear();

        // Hearing
        rightEarResultComboBox.setValue(null);
        leftEarResultComboBox.setValue(null);
        rightEarFrequenciesField.clear();
        leftEarFrequenciesField.clear();
        usesHearingAidCheckBox.setSelected(false);
        hearingConcernsCheckBox.setSelected(false);
        hearingNotesArea.clear();

        // Scoliosis
        adamsBendTestComboBox.setValue(null);
        ribHumpComboBox.setValue(null);
        shoulderAsymmetryCheckBox.setSelected(false);
        hipAsymmetryCheckBox.setSelected(false);
        spinalCurvatureCheckBox.setSelected(false);
        scoliosisFindingsArea.clear();

        // BMI
        heightField.clear();
        weightField.clear();
        bmiField.clear();
        bmiPercentileField.clear();
        bmiCategoryComboBox.setValue(null);
        bmiNotesArea.clear();

        // Dental
        obviousCavitiesCheckBox.setSelected(false);
        gumDiseaseCheckBox.setSelected(false);
        oralPainCheckBox.setSelected(false);
        needsDentalCareCheckBox.setSelected(false);
        dentalFindingsArea.clear();

        // Overall
        overallResultComboBox.setValue(null);
        meetsStateRequirementsCheckBox.setSelected(true);

        // Parent Notification
        parentNotifiedCheckBox.setSelected(false);
        referralProvidedCheckBox.setSelected(false);
        notificationDatePicker.setValue(null);
        notificationMethodComboBox.setValue(null);
        parentNotificationNotesArea.clear();

        // Follow-Up
        referralTypeComboBox.setValue(null);
        followUpDatePicker.setValue(null);
        referralReasonArea.clear();
        followUpInstructionsArea.clear();

        // Notes
        notesArea.clear();

        // Hide conditional sections
        setupConditionalVisibility();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
