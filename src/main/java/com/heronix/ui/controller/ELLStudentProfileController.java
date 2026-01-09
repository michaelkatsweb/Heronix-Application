package com.heronix.ui.controller;

import com.heronix.model.domain.*;
import com.heronix.service.ELLManagementService;
import com.heronix.service.StudentService;
import com.heronix.service.TeacherService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for ELL Student Profile UI
 * Manages comprehensive English Language Learner student profiles including
 * identification, language information, proficiency levels, program placement,
 * service delivery, progress monitoring, reclassification, and parent communication.
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - ELL Management System
 */
@Slf4j
@Component
public class ELLStudentProfileController {

    @Autowired
    private ELLManagementService ellManagementService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    // ========================================================================
    // FXML FIELDS - Student Information
    // ========================================================================

    @FXML private ComboBox<Student> studentComboBox;
    @FXML private TextField gradeField;
    @FXML private TextField studentIdField;
    @FXML private TextField dateOfBirthField;
    @FXML private DatePicker identificationDatePicker;
    @FXML private ComboBox<String> ellStatusComboBox;

    // ========================================================================
    // FXML FIELDS - Home Language Survey
    // ========================================================================

    @FXML private CheckBox homeLanguageSurveyCompletedCheckBox;
    @FXML private DatePicker homeLanguageSurveyDatePicker;

    // ========================================================================
    // FXML FIELDS - Language Information
    // ========================================================================

    @FXML private TextField nativeLanguageField;
    @FXML private TextField homeLanguageField;
    @FXML private TextField parentLanguageField;
    @FXML private TextField countryOfOriginField;
    @FXML private TextField otherLanguagesField;
    @FXML private DatePicker enrollmentInUSSchoolsDatePicker;
    @FXML private TextField yearsInUSSchoolsField;
    @FXML private CheckBox immigrantStatusCheckBox;

    // ========================================================================
    // FXML FIELDS - Proficiency Level
    // ========================================================================

    @FXML private ComboBox<String> proficiencyLevelComboBox;
    @FXML private DatePicker lastProficiencyAssessmentDatePicker;
    @FXML private TextField listeningLevelField;
    @FXML private TextField speakingLevelField;
    @FXML private TextField readingLevelField;
    @FXML private TextField writingLevelField;
    @FXML private TextField comprehensionLevelField;
    @FXML private TextField averageScoreField;

    // ========================================================================
    // FXML FIELDS - Program Placement
    // ========================================================================

    @FXML private ComboBox<String> programTypeComboBox;
    @FXML private DatePicker programEntryDatePicker;
    @FXML private DatePicker programExitDatePicker;
    @FXML private TextField yearsInProgramField;

    // ========================================================================
    // FXML FIELDS - Service Delivery
    // ========================================================================

    @FXML private ComboBox<String> serviceDeliveryModelComboBox;
    @FXML private ComboBox<Teacher> serviceProviderComboBox;
    @FXML private TextField serviceMinutesField;
    @FXML private TextField serviceFrequencyField;

    // ========================================================================
    // FXML FIELDS - Progress Monitoring
    // ========================================================================

    @FXML private CheckBox annualAssessmentRequiredCheckBox;
    @FXML private DatePicker nextAnnualAssessmentDatePicker;
    @FXML private DatePicker lastProgressMonitoringDatePicker;
    @FXML private TextField progressMonitoringFrequencyField;

    // ========================================================================
    // FXML FIELDS - Reclassification
    // ========================================================================

    @FXML private CheckBox eligibleForReclassificationCheckBox;
    @FXML private DatePicker reclassificationEligibilityDatePicker;
    @FXML private DatePicker reclassificationDatePicker;
    @FXML private TextArea reclassificationReasonArea;
    @FXML private DatePicker monitoringPeriodStartDatePicker;
    @FXML private TextField monitoringPeriodYearsField;

    // ========================================================================
    // FXML FIELDS - Parent Communication
    // ========================================================================

    @FXML private CheckBox parentNotificationSentCheckBox;
    @FXML private DatePicker parentNotificationDatePicker;
    @FXML private TextField parentNotificationLanguageField;
    @FXML private CheckBox translationServicesRequiredCheckBox;
    @FXML private CheckBox interpreterRequiredCheckBox;
    @FXML private TextField interpreterLanguageField;

    // ========================================================================
    // FXML FIELDS - Title III
    // ========================================================================

    @FXML private CheckBox titleIIIEligibleCheckBox;
    @FXML private CheckBox titleIIIFundedCheckBox;

    // ========================================================================
    // FXML FIELDS - Notes
    // ========================================================================

    @FXML private TextArea notesArea;

    // ========================================================================
    // STATE
    // ========================================================================

    private Student currentStudent;
    private ELLStudent currentELLStudent;
    private boolean viewMode = false;

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    @FXML
    public void initialize() {
        log.info("Initializing ELLStudentProfileController");
        setupComboBoxes();
        setupCalculations();
        setupValidation();
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

        // Service Provider ComboBox (ESL Teachers)
        serviceProviderComboBox.setConverter(new StringConverter<Teacher>() {
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
    }

    private void setupCalculations() {
        // Auto-calculate years in US schools
        enrollmentInUSSchoolsDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            calculateYearsInUSSchools();
        });

        // Auto-calculate years in program
        programEntryDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            calculateYearsInProgram();
        });
        programExitDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            calculateYearsInProgram();
        });

        // Auto-calculate average proficiency score
        listeningLevelField.textProperty().addListener((observable, oldValue, newValue) -> calculateAverageScore());
        speakingLevelField.textProperty().addListener((observable, oldValue, newValue) -> calculateAverageScore());
        readingLevelField.textProperty().addListener((observable, oldValue, newValue) -> calculateAverageScore());
        writingLevelField.textProperty().addListener((observable, oldValue, newValue) -> calculateAverageScore());
        comprehensionLevelField.textProperty().addListener((observable, oldValue, newValue) -> calculateAverageScore());
    }

    private void setupValidation() {
        // Numeric-only fields (1-6 range)
        setupNumericField(listeningLevelField, 1, 6);
        setupNumericField(speakingLevelField, 1, 6);
        setupNumericField(readingLevelField, 1, 6);
        setupNumericField(writingLevelField, 1, 6);
        setupNumericField(comprehensionLevelField, 1, 6);

        // Numeric fields (general)
        serviceMinutesField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.matches("\\d*")) {
                serviceMinutesField.setText(oldValue);
            }
        });

        monitoringPeriodYearsField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.matches("\\d*")) {
                monitoringPeriodYearsField.setText(oldValue);
            }
        });
    }

    private void setupNumericField(TextField field, int min, int max) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                if (!newValue.matches("\\d*")) {
                    field.setText(oldValue);
                } else if (!newValue.isEmpty()) {
                    try {
                        int value = Integer.parseInt(newValue);
                        if (value < min || value > max) {
                            field.setText(oldValue);
                        }
                    } catch (NumberFormatException e) {
                        field.setText(oldValue);
                    }
                }
            }
        });
    }

    private void loadInitialData() {
        // Load all students
        List<Student> students = studentService.getAllStudents();
        studentComboBox.setItems(FXCollections.observableArrayList(students));

        // Load ESL teachers
        List<Teacher> eslTeachers = teacherService.getAllTeachers().stream()
                .filter(t -> t.getDepartment() != null &&
                        (t.getDepartment().contains("ESL") ||
                         t.getDepartment().contains("ELL") ||
                         t.getDepartment().contains("ESOL") ||
                         t.getDepartment().contains("English Language")))
                .collect(Collectors.toList());
        serviceProviderComboBox.setItems(FXCollections.observableArrayList(eslTeachers));
    }

    private void setupDefaults() {
        // Default status
        ellStatusComboBox.setValue("ACTIVE");

        // Default dates
        identificationDatePicker.setValue(LocalDate.now());

        // Default checkboxes
        annualAssessmentRequiredCheckBox.setSelected(true);
        titleIIIEligibleCheckBox.setSelected(true);
    }

    // ========================================================================
    // PUBLIC METHODS FOR EXTERNAL INTEGRATION
    // ========================================================================

    /**
     * Set the student for ELL profile
     */
    public void setStudent(Student student) {
        this.currentStudent = student;
        studentComboBox.setValue(student);

        // Populate student information
        if (student.getGradeLevel() != null) {
            gradeField.setText(student.getGradeLevel().toString());
        }

        studentIdField.setText(student.getStudentId());

        if (student.getDateOfBirth() != null) {
            dateOfBirthField.setText(student.getDateOfBirth().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
        }

        // Try to load existing ELL profile
        try {
            ELLStudent existingProfile = ellManagementService.getELLStudentByStudentId(student.getId());
            if (existingProfile != null) {
                loadELLProfile(existingProfile);
            }
        } catch (Exception e) {
            log.debug("No existing ELL profile found for student: {}", student.getStudentId());
        }
    }

    /**
     * Load existing ELL profile for editing
     */
    public void loadELLProfile(ELLStudent ellStudent) {
        this.currentELLStudent = ellStudent;

        // Set student first
        setStudent(ellStudent.getStudent());

        // Identification
        identificationDatePicker.setValue(ellStudent.getIdentificationDate());
        ellStatusComboBox.setValue(ellStudent.getEllStatus().name());

        // Home Language Survey
        homeLanguageSurveyCompletedCheckBox.setSelected(ellStudent.getHomeLanguageSurveyCompleted());
        if (ellStudent.getHomeLanguageSurveyDate() != null) {
            homeLanguageSurveyDatePicker.setValue(ellStudent.getHomeLanguageSurveyDate());
        }

        // Language Information
        nativeLanguageField.setText(ellStudent.getNativeLanguage());
        homeLanguageField.setText(ellStudent.getHomeLanguage());
        parentLanguageField.setText(ellStudent.getParentLanguage());
        countryOfOriginField.setText(ellStudent.getCountryOfOrigin());

        if (!ellStudent.getOtherLanguagesSpoken().isEmpty()) {
            otherLanguagesField.setText(String.join(", ", ellStudent.getOtherLanguagesSpoken()));
        }

        if (ellStudent.getEnrollmentInUSSchoolsDate() != null) {
            enrollmentInUSSchoolsDatePicker.setValue(ellStudent.getEnrollmentInUSSchoolsDate());
        }

        if (ellStudent.getImmigrantStatus() != null) {
            immigrantStatusCheckBox.setSelected(ellStudent.getImmigrantStatus());
        }

        // Proficiency Level
        proficiencyLevelComboBox.setValue(ellStudent.getProficiencyLevel().name());
        if (ellStudent.getLastProficiencyAssessmentDate() != null) {
            lastProficiencyAssessmentDatePicker.setValue(ellStudent.getLastProficiencyAssessmentDate());
        }

        if (ellStudent.getListeningLevel() != null) listeningLevelField.setText(String.valueOf(ellStudent.getListeningLevel()));
        if (ellStudent.getSpeakingLevel() != null) speakingLevelField.setText(String.valueOf(ellStudent.getSpeakingLevel()));
        if (ellStudent.getReadingLevel() != null) readingLevelField.setText(String.valueOf(ellStudent.getReadingLevel()));
        if (ellStudent.getWritingLevel() != null) writingLevelField.setText(String.valueOf(ellStudent.getWritingLevel()));
        if (ellStudent.getComprehensionLevel() != null) comprehensionLevelField.setText(String.valueOf(ellStudent.getComprehensionLevel()));

        // Program Placement
        if (ellStudent.getProgramType() != null) {
            programTypeComboBox.setValue(ellStudent.getProgramType().name());
        }
        if (ellStudent.getProgramEntryDate() != null) {
            programEntryDatePicker.setValue(ellStudent.getProgramEntryDate());
        }
        if (ellStudent.getProgramExitDate() != null) {
            programExitDatePicker.setValue(ellStudent.getProgramExitDate());
        }

        // Service Delivery
        if (ellStudent.getServiceDeliveryModel() != null) {
            serviceDeliveryModelComboBox.setValue(ellStudent.getServiceDeliveryModel().name());
        }
        serviceProviderComboBox.setValue(ellStudent.getServiceProvider());
        if (ellStudent.getServiceMinutesPerWeek() != null) {
            serviceMinutesField.setText(String.valueOf(ellStudent.getServiceMinutesPerWeek()));
        }
        serviceFrequencyField.setText(ellStudent.getServiceFrequency());

        // Progress Monitoring
        annualAssessmentRequiredCheckBox.setSelected(ellStudent.getAnnualAssessmentRequired());
        if (ellStudent.getNextAnnualAssessmentDate() != null) {
            nextAnnualAssessmentDatePicker.setValue(ellStudent.getNextAnnualAssessmentDate());
        }
        if (ellStudent.getLastProgressMonitoringDate() != null) {
            lastProgressMonitoringDatePicker.setValue(ellStudent.getLastProgressMonitoringDate());
        }
        progressMonitoringFrequencyField.setText(ellStudent.getProgressMonitoringFrequency());

        // Reclassification
        eligibleForReclassificationCheckBox.setSelected(ellStudent.getEligibleForReclassification());
        if (ellStudent.getReclassificationEligibilityDate() != null) {
            reclassificationEligibilityDatePicker.setValue(ellStudent.getReclassificationEligibilityDate());
        }
        if (ellStudent.getReclassificationDate() != null) {
            reclassificationDatePicker.setValue(ellStudent.getReclassificationDate());
        }
        reclassificationReasonArea.setText(ellStudent.getReclassificationReason());
        if (ellStudent.getMonitoringPeriodStartDate() != null) {
            monitoringPeriodStartDatePicker.setValue(ellStudent.getMonitoringPeriodStartDate());
        }
        if (ellStudent.getMonitoringPeriodYears() != null) {
            monitoringPeriodYearsField.setText(String.valueOf(ellStudent.getMonitoringPeriodYears()));
        }

        // Parent Communication
        parentNotificationSentCheckBox.setSelected(ellStudent.getParentNotificationSent());
        if (ellStudent.getParentNotificationDate() != null) {
            parentNotificationDatePicker.setValue(ellStudent.getParentNotificationDate());
        }
        parentNotificationLanguageField.setText(ellStudent.getParentNotificationLanguage());
        translationServicesRequiredCheckBox.setSelected(ellStudent.getTranslationServicesRequired());
        interpreterRequiredCheckBox.setSelected(ellStudent.getInterpreterRequired());
        interpreterLanguageField.setText(ellStudent.getInterpreterLanguage());

        // Title III
        titleIIIEligibleCheckBox.setSelected(ellStudent.getTitleIIIEligible());
        titleIIIFundedCheckBox.setSelected(ellStudent.getTitleIIIFunded());

        // Notes
        notesArea.setText(ellStudent.getNotes());
    }

    /**
     * Set view mode (read-only)
     */
    public void setViewMode(boolean viewMode) {
        this.viewMode = viewMode;

        // Disable all input controls
        studentComboBox.setDisable(viewMode);
        identificationDatePicker.setDisable(viewMode);
        ellStatusComboBox.setDisable(viewMode);
        homeLanguageSurveyCompletedCheckBox.setDisable(viewMode);
        homeLanguageSurveyDatePicker.setDisable(viewMode);
        nativeLanguageField.setDisable(viewMode);
        homeLanguageField.setDisable(viewMode);
        parentLanguageField.setDisable(viewMode);
        countryOfOriginField.setDisable(viewMode);
        otherLanguagesField.setDisable(viewMode);
        enrollmentInUSSchoolsDatePicker.setDisable(viewMode);
        immigrantStatusCheckBox.setDisable(viewMode);
        proficiencyLevelComboBox.setDisable(viewMode);
        lastProficiencyAssessmentDatePicker.setDisable(viewMode);
        listeningLevelField.setDisable(viewMode);
        speakingLevelField.setDisable(viewMode);
        readingLevelField.setDisable(viewMode);
        writingLevelField.setDisable(viewMode);
        comprehensionLevelField.setDisable(viewMode);
        programTypeComboBox.setDisable(viewMode);
        programEntryDatePicker.setDisable(viewMode);
        programExitDatePicker.setDisable(viewMode);
        serviceDeliveryModelComboBox.setDisable(viewMode);
        serviceProviderComboBox.setDisable(viewMode);
        serviceMinutesField.setDisable(viewMode);
        serviceFrequencyField.setDisable(viewMode);
        annualAssessmentRequiredCheckBox.setDisable(viewMode);
        nextAnnualAssessmentDatePicker.setDisable(viewMode);
        lastProgressMonitoringDatePicker.setDisable(viewMode);
        progressMonitoringFrequencyField.setDisable(viewMode);
        eligibleForReclassificationCheckBox.setDisable(viewMode);
        reclassificationEligibilityDatePicker.setDisable(viewMode);
        reclassificationDatePicker.setDisable(viewMode);
        reclassificationReasonArea.setDisable(viewMode);
        monitoringPeriodStartDatePicker.setDisable(viewMode);
        monitoringPeriodYearsField.setDisable(viewMode);
        parentNotificationSentCheckBox.setDisable(viewMode);
        parentNotificationDatePicker.setDisable(viewMode);
        parentNotificationLanguageField.setDisable(viewMode);
        translationServicesRequiredCheckBox.setDisable(viewMode);
        interpreterRequiredCheckBox.setDisable(viewMode);
        interpreterLanguageField.setDisable(viewMode);
        titleIIIEligibleCheckBox.setDisable(viewMode);
        titleIIIFundedCheckBox.setDisable(viewMode);
        notesArea.setDisable(viewMode);
    }

    // ========================================================================
    // CALCULATION METHODS
    // ========================================================================

    private void calculateYearsInUSSchools() {
        LocalDate enrollmentDate = enrollmentInUSSchoolsDatePicker.getValue();
        if (enrollmentDate != null) {
            long years = ChronoUnit.YEARS.between(enrollmentDate, LocalDate.now());
            yearsInUSSchoolsField.setText(String.valueOf(years));
        }
    }

    private void calculateYearsInProgram() {
        LocalDate entryDate = programEntryDatePicker.getValue();
        if (entryDate != null) {
            LocalDate exitDate = programExitDatePicker.getValue() != null ?
                    programExitDatePicker.getValue() : LocalDate.now();
            long years = ChronoUnit.YEARS.between(entryDate, exitDate);
            yearsInProgramField.setText(String.valueOf(years));
        }
    }

    private void calculateAverageScore() {
        try {
            int count = 0;
            int sum = 0;

            if (!listeningLevelField.getText().isEmpty()) {
                sum += Integer.parseInt(listeningLevelField.getText());
                count++;
            }
            if (!speakingLevelField.getText().isEmpty()) {
                sum += Integer.parseInt(speakingLevelField.getText());
                count++;
            }
            if (!readingLevelField.getText().isEmpty()) {
                sum += Integer.parseInt(readingLevelField.getText());
                count++;
            }
            if (!writingLevelField.getText().isEmpty()) {
                sum += Integer.parseInt(writingLevelField.getText());
                count++;
            }
            if (!comprehensionLevelField.getText().isEmpty()) {
                sum += Integer.parseInt(comprehensionLevelField.getText());
                count++;
            }

            if (count > 0) {
                double average = (double) sum / count;
                averageScoreField.setText(String.format("%.2f", average));
            } else {
                averageScoreField.clear();
            }
        } catch (NumberFormatException e) {
            averageScoreField.clear();
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
            ELLStudent ellStudent = buildELLStudentFromForm();

            if (currentELLStudent == null) {
                // Create new ELL student profile
                ellManagementService.createELLStudent(ellStudent);
                log.info("Created new ELL student profile for: {}", currentStudent.getStudentId());
                showSuccess("ELL student profile created successfully!");
            } else {
                // Update existing profile
                ellStudent.setId(currentELLStudent.getId());
                ellManagementService.updateELLStudent(ellStudent);
                log.info("Updated ELL student profile ID: {}", currentELLStudent.getId());
                showSuccess("ELL student profile updated successfully!");
            }

            handleClose();
        } catch (Exception e) {
            log.error("Error saving ELL student profile", e);
            showError("Failed to save ELL student profile: " + e.getMessage());
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

        if (identificationDatePicker.getValue() == null) {
            errors.add("Identification date is required");
        }

        if (ellStatusComboBox.getValue() == null || ellStatusComboBox.getValue().isEmpty()) {
            errors.add("ELL status is required");
        }

        if (nativeLanguageField.getText() == null || nativeLanguageField.getText().trim().isEmpty()) {
            errors.add("Native language is required");
        }

        if (proficiencyLevelComboBox.getValue() == null || proficiencyLevelComboBox.getValue().isEmpty()) {
            errors.add("Proficiency level is required");
        }

        if (programTypeComboBox.getValue() == null || programTypeComboBox.getValue().isEmpty()) {
            errors.add("Program type is required");
        }

        // Federal compliance validation
        if (!homeLanguageSurveyCompletedCheckBox.isSelected()) {
            errors.add("FEDERAL REQUIREMENT: Home Language Survey must be completed");
        }

        if (!parentNotificationSentCheckBox.isSelected()) {
            errors.add("FEDERAL REQUIREMENT: Parent notification is required by law");
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

    private ELLStudent buildELLStudentFromForm() {
        ELLStudent ellStudent = ELLStudent.builder()
                .student(studentComboBox.getValue())
                .identificationDate(identificationDatePicker.getValue())
                .ellStatus(ELLStudent.ELLStatus.valueOf(ellStatusComboBox.getValue()))
                .homeLanguageSurveyCompleted(homeLanguageSurveyCompletedCheckBox.isSelected())
                .homeLanguageSurveyDate(homeLanguageSurveyDatePicker.getValue())
                .nativeLanguage(nativeLanguageField.getText())
                .homeLanguage(homeLanguageField.getText())
                .parentLanguage(parentLanguageField.getText())
                .countryOfOrigin(countryOfOriginField.getText())
                .enrollmentInUSSchoolsDate(enrollmentInUSSchoolsDatePicker.getValue())
                .immigrantStatus(immigrantStatusCheckBox.isSelected())
                .proficiencyLevel(ELLStudent.ProficiencyLevel.valueOf(proficiencyLevelComboBox.getValue()))
                .lastProficiencyAssessmentDate(lastProficiencyAssessmentDatePicker.getValue())
                .programEntryDate(programEntryDatePicker.getValue())
                .programExitDate(programExitDatePicker.getValue())
                .serviceProvider(serviceProviderComboBox.getValue())
                .serviceFrequency(serviceFrequencyField.getText())
                .annualAssessmentRequired(annualAssessmentRequiredCheckBox.isSelected())
                .nextAnnualAssessmentDate(nextAnnualAssessmentDatePicker.getValue())
                .lastProgressMonitoringDate(lastProgressMonitoringDatePicker.getValue())
                .progressMonitoringFrequency(progressMonitoringFrequencyField.getText())
                .eligibleForReclassification(eligibleForReclassificationCheckBox.isSelected())
                .reclassificationEligibilityDate(reclassificationEligibilityDatePicker.getValue())
                .reclassificationDate(reclassificationDatePicker.getValue())
                .reclassificationReason(reclassificationReasonArea.getText())
                .monitoringPeriodStartDate(monitoringPeriodStartDatePicker.getValue())
                .parentNotificationSent(parentNotificationSentCheckBox.isSelected())
                .parentNotificationDate(parentNotificationDatePicker.getValue())
                .parentNotificationLanguage(parentNotificationLanguageField.getText())
                .translationServicesRequired(translationServicesRequiredCheckBox.isSelected())
                .interpreterRequired(interpreterRequiredCheckBox.isSelected())
                .interpreterLanguage(interpreterLanguageField.getText())
                .titleIIIEligible(titleIIIEligibleCheckBox.isSelected())
                .titleIIIFunded(titleIIIFundedCheckBox.isSelected())
                .notes(notesArea.getText())
                .build();

        // Parse other languages
        if (otherLanguagesField.getText() != null && !otherLanguagesField.getText().trim().isEmpty()) {
            List<String> otherLanguages = Arrays.stream(otherLanguagesField.getText().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
            ellStudent.setOtherLanguagesSpoken(otherLanguages);
        }

        // Parse numeric fields
        if (!listeningLevelField.getText().isEmpty()) {
            ellStudent.setListeningLevel(Integer.parseInt(listeningLevelField.getText()));
        }
        if (!speakingLevelField.getText().isEmpty()) {
            ellStudent.setSpeakingLevel(Integer.parseInt(speakingLevelField.getText()));
        }
        if (!readingLevelField.getText().isEmpty()) {
            ellStudent.setReadingLevel(Integer.parseInt(readingLevelField.getText()));
        }
        if (!writingLevelField.getText().isEmpty()) {
            ellStudent.setWritingLevel(Integer.parseInt(writingLevelField.getText()));
        }
        if (!comprehensionLevelField.getText().isEmpty()) {
            ellStudent.setComprehensionLevel(Integer.parseInt(comprehensionLevelField.getText()));
        }

        if (programTypeComboBox.getValue() != null && !programTypeComboBox.getValue().isEmpty()) {
            ellStudent.setProgramType(ELLStudent.ProgramType.valueOf(programTypeComboBox.getValue()));
        }

        if (serviceDeliveryModelComboBox.getValue() != null && !serviceDeliveryModelComboBox.getValue().isEmpty()) {
            ellStudent.setServiceDeliveryModel(ELLStudent.ServiceDeliveryModel.valueOf(serviceDeliveryModelComboBox.getValue()));
        }

        if (!serviceMinutesField.getText().isEmpty()) {
            ellStudent.setServiceMinutesPerWeek(Integer.parseInt(serviceMinutesField.getText()));
        }

        if (!monitoringPeriodYearsField.getText().isEmpty()) {
            ellStudent.setMonitoringPeriodYears(Integer.parseInt(monitoringPeriodYearsField.getText()));
        }

        return ellStudent;
    }

    private void clearForm() {
        currentELLStudent = null;

        identificationDatePicker.setValue(LocalDate.now());
        ellStatusComboBox.setValue("ACTIVE");
        homeLanguageSurveyCompletedCheckBox.setSelected(false);
        homeLanguageSurveyDatePicker.setValue(null);
        nativeLanguageField.clear();
        homeLanguageField.clear();
        parentLanguageField.clear();
        countryOfOriginField.clear();
        otherLanguagesField.clear();
        enrollmentInUSSchoolsDatePicker.setValue(null);
        yearsInUSSchoolsField.clear();
        immigrantStatusCheckBox.setSelected(false);
        proficiencyLevelComboBox.setValue(null);
        lastProficiencyAssessmentDatePicker.setValue(null);
        listeningLevelField.clear();
        speakingLevelField.clear();
        readingLevelField.clear();
        writingLevelField.clear();
        comprehensionLevelField.clear();
        averageScoreField.clear();
        programTypeComboBox.setValue(null);
        programEntryDatePicker.setValue(null);
        programExitDatePicker.setValue(null);
        yearsInProgramField.clear();
        serviceDeliveryModelComboBox.setValue(null);
        serviceProviderComboBox.setValue(null);
        serviceMinutesField.clear();
        serviceFrequencyField.clear();
        annualAssessmentRequiredCheckBox.setSelected(true);
        nextAnnualAssessmentDatePicker.setValue(null);
        lastProgressMonitoringDatePicker.setValue(null);
        progressMonitoringFrequencyField.clear();
        eligibleForReclassificationCheckBox.setSelected(false);
        reclassificationEligibilityDatePicker.setValue(null);
        reclassificationDatePicker.setValue(null);
        reclassificationReasonArea.clear();
        monitoringPeriodStartDatePicker.setValue(null);
        monitoringPeriodYearsField.clear();
        parentNotificationSentCheckBox.setSelected(false);
        parentNotificationDatePicker.setValue(null);
        parentNotificationLanguageField.clear();
        translationServicesRequiredCheckBox.setSelected(false);
        interpreterRequiredCheckBox.setSelected(false);
        interpreterLanguageField.clear();
        titleIIIEligibleCheckBox.setSelected(true);
        titleIIIFundedCheckBox.setSelected(false);
        notesArea.clear();
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
