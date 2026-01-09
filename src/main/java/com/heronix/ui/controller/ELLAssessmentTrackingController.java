package com.heronix.ui.controller;

import com.heronix.model.domain.ELLAssessment;
import com.heronix.model.domain.ELLStudent;
import com.heronix.model.domain.Teacher;
import com.heronix.service.ELLManagementService;
import com.heronix.service.StudentService;
import com.heronix.service.TeacherService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ELLAssessmentTrackingController {

    @Autowired
    private ELLManagementService ellManagementService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    // Student & Assessment Information
    @FXML private ComboBox<ELLStudent> ellStudentComboBox;
    @FXML private TextField studentGradeField;
    @FXML private TextField currentProficiencyField;
    @FXML private TextField nativeLanguageField;

    @FXML private ComboBox<ELLAssessment.AssessmentType> assessmentTypeComboBox;
    @FXML private DatePicker assessmentDatePicker;
    @FXML private ComboBox<ELLAssessment.AssessmentPurpose> assessmentPurposeComboBox;
    @FXML private TextField testFormField;
    @FXML private TextField testVersionField;

    // Overall Results
    @FXML private TextField performanceLevelField;
    @FXML private TextField compositeScoreField;
    @FXML private TextField scaleScoreField;
    @FXML private TextField percentileRankField;

    // Domain Scores
    @FXML private TextField listeningScoreField;
    @FXML private TextField listeningLevelField;
    @FXML private TextField speakingScoreField;
    @FXML private TextField speakingLevelField;
    @FXML private TextField readingScoreField;
    @FXML private TextField readingLevelField;
    @FXML private TextField writingScoreField;
    @FXML private TextField writingLevelField;
    @FXML private TextField averageDomainScoreField;

    // Composite Scores
    @FXML private TextField literacyCompositeScoreField;
    @FXML private TextField oralLanguageCompositeScoreField;

    // Progress & Growth
    @FXML private TextField previousAssessmentDateField;
    @FXML private TextField previousCompositeScoreField;
    @FXML private TextField growthScoreField;
    @FXML private CheckBox metGrowthTargetCheckBox;

    // Reclassification Criteria
    @FXML private TextField reclassificationThresholdField;
    @FXML private CheckBox meetsReclassificationCriteriaCheckBox;
    @FXML private TextArea reclassificationNotesArea;

    // Administration Details
    @FXML private ComboBox<Teacher> testAdministratorComboBox;
    @FXML private TextField testLocationField;
    @FXML private TextArea accommodationsProvidedArea;
    @FXML private TextField testDurationField;

    // Reporting & Compliance
    @FXML private DatePicker resultsReceivedDatePicker;
    @FXML private CheckBox parentNotificationSentCheckBox;
    @FXML private DatePicker parentNotificationDatePicker;
    @FXML private CheckBox reportedToStateCheckBox;
    @FXML private DatePicker stateReportingDatePicker;

    // Score Validity
    @FXML private CheckBox scoreValidCheckBox;
    @FXML private TextArea invalidationReasonArea;

    // Additional Notes
    @FXML private TextArea additionalNotesArea;

    // Action Buttons
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private ELLAssessment currentAssessment;
    private boolean viewMode = false;

    @FXML
    public void initialize() {
        setupComboBoxes();
        setupListeners();
        loadInitialData();
    }

    private void setupComboBoxes() {
        // ELL Student ComboBox
        ellStudentComboBox.setConverter(new StringConverter<ELLStudent>() {
            @Override
            public String toString(ELLStudent student) {
                if (student == null) return "";
                return student.getStudent().getLastName() + ", " +
                       student.getStudent().getFirstName() + " (" +
                       student.getStudent().getStudentId() + ")";
            }

            @Override
            public ELLStudent fromString(String string) {
                return null;
            }
        });

        // Assessment Type ComboBox
        assessmentTypeComboBox.setItems(FXCollections.observableArrayList(
                ELLAssessment.AssessmentType.values()));
        assessmentTypeComboBox.setConverter(new StringConverter<ELLAssessment.AssessmentType>() {
            @Override
            public String toString(ELLAssessment.AssessmentType type) {
                return type != null ? type.getDisplayName() : "";
            }

            @Override
            public ELLAssessment.AssessmentType fromString(String string) {
                return null;
            }
        });

        // Assessment Purpose ComboBox
        assessmentPurposeComboBox.setItems(FXCollections.observableArrayList(
                ELLAssessment.AssessmentPurpose.values()));
        assessmentPurposeComboBox.setConverter(new StringConverter<ELLAssessment.AssessmentPurpose>() {
            @Override
            public String toString(ELLAssessment.AssessmentPurpose purpose) {
                return purpose != null ? purpose.getDisplayName() : "";
            }

            @Override
            public ELLAssessment.AssessmentPurpose fromString(String string) {
                return null;
            }
        });

        // Test Administrator ComboBox
        testAdministratorComboBox.setConverter(new StringConverter<Teacher>() {
            @Override
            public String toString(Teacher teacher) {
                if (teacher == null) return "";
                return teacher.getLastName() + ", " + teacher.getFirstName();
            }

            @Override
            public Teacher fromString(String string) {
                return null;
            }
        });
    }

    private void setupListeners() {
        // Auto-populate student information when ELL student is selected
        ellStudentComboBox.setOnAction(e -> {
            ELLStudent selected = ellStudentComboBox.getValue();
            if (selected != null) {
                populateStudentInformation(selected);
            }
        });

        // Auto-calculate average domain score when any domain score changes
        listeningScoreField.textProperty().addListener((obs, old, newVal) -> calculateAverageDomainScore());
        speakingScoreField.textProperty().addListener((obs, old, newVal) -> calculateAverageDomainScore());
        readingScoreField.textProperty().addListener((obs, old, newVal) -> calculateAverageDomainScore());
        writingScoreField.textProperty().addListener((obs, old, newVal) -> calculateAverageDomainScore());

        // Auto-calculate growth score when previous composite score changes
        compositeScoreField.textProperty().addListener((obs, old, newVal) -> calculateGrowthScore());
        previousCompositeScoreField.textProperty().addListener((obs, old, newVal) -> calculateGrowthScore());

        // Auto-check reclassification criteria when composite score changes
        compositeScoreField.textProperty().addListener((obs, old, newVal) -> checkReclassificationCriteria());
        reclassificationThresholdField.textProperty().addListener((obs, old, newVal) -> checkReclassificationCriteria());

        // Validate proficiency levels (1-6 range)
        addNumericValidation(listeningLevelField, 1, 6);
        addNumericValidation(speakingLevelField, 1, 6);
        addNumericValidation(readingLevelField, 1, 6);
        addNumericValidation(writingLevelField, 1, 6);

        // Enable/disable invalidation reason based on score validity
        scoreValidCheckBox.selectedProperty().addListener((obs, old, newVal) -> {
            invalidationReasonArea.setDisable(newVal);
            if (newVal) {
                invalidationReasonArea.clear();
            }
        });
    }

    private void loadInitialData() {
        // Load all active ELL students
        List<ELLStudent> ellStudents = ellManagementService.findAllELLStudents().stream()
                .filter(ELLStudent::isActiveELL)
                .sorted((s1, s2) -> {
                    String name1 = s1.getStudent().getLastName() + s1.getStudent().getFirstName();
                    String name2 = s2.getStudent().getLastName() + s2.getStudent().getFirstName();
                    return name1.compareToIgnoreCase(name2);
                })
                .collect(Collectors.toList());
        ellStudentComboBox.setItems(FXCollections.observableArrayList(ellStudents));

        // Load ESL/ELL teachers
        List<Teacher> teachers = teacherService.findAllTeachers().stream()
                .filter(t -> {
                    List<String> certifications = t.getCertifications();
                    return certifications != null &&
                           (certifications.contains("ESL") ||
                            certifications.contains("ELL") ||
                            certifications.contains("ESOL"));
                })
                .sorted((t1, t2) -> {
                    String name1 = t1.getLastName() + t1.getFirstName();
                    String name2 = t2.getLastName() + t2.getFirstName();
                    return name1.compareToIgnoreCase(name2);
                })
                .collect(Collectors.toList());
        testAdministratorComboBox.setItems(FXCollections.observableArrayList(teachers));

        // Set default values
        assessmentDatePicker.setValue(LocalDate.now());
        scoreValidCheckBox.setSelected(true);
        invalidationReasonArea.setDisable(true);
    }

    private void populateStudentInformation(ELLStudent ellStudent) {
        if (ellStudent.getStudent() != null) {
            studentGradeField.setText(ellStudent.getStudent().getGradeLevel() != null ?
                    ellStudent.getStudent().getGradeLevel().toString() : "");
        }

        currentProficiencyField.setText(ellStudent.getProficiencyLevel() != null ?
                ellStudent.getProficiencyLevel().getDisplayName() : "");

        nativeLanguageField.setText(ellStudent.getNativeLanguage() != null ?
                ellStudent.getNativeLanguage() : "");

        // Auto-populate previous assessment data if available
        loadPreviousAssessmentData(ellStudent);
    }

    private void loadPreviousAssessmentData(ELLStudent ellStudent) {
        List<ELLAssessment> assessments = ellManagementService.findAssessmentsByStudent(ellStudent);
        if (!assessments.isEmpty()) {
            // Get the most recent assessment
            ELLAssessment previous = assessments.stream()
                    .filter(a -> a.getAssessmentDate() != null)
                    .max((a1, a2) -> a1.getAssessmentDate().compareTo(a2.getAssessmentDate()))
                    .orElse(null);

            if (previous != null) {
                previousAssessmentDateField.setText(previous.getAssessmentDate().toString());
                if (previous.getCompositeScore() != null) {
                    previousCompositeScoreField.setText(previous.getCompositeScore().toString());
                }
            }
        }
    }

    private void calculateAverageDomainScore() {
        try {
            int count = 0;
            double sum = 0.0;

            if (!listeningScoreField.getText().isEmpty()) {
                sum += Double.parseDouble(listeningScoreField.getText());
                count++;
            }
            if (!speakingScoreField.getText().isEmpty()) {
                sum += Double.parseDouble(speakingScoreField.getText());
                count++;
            }
            if (!readingScoreField.getText().isEmpty()) {
                sum += Double.parseDouble(readingScoreField.getText());
                count++;
            }
            if (!writingScoreField.getText().isEmpty()) {
                sum += Double.parseDouble(writingScoreField.getText());
                count++;
            }

            if (count > 0) {
                double average = sum / count;
                averageDomainScoreField.setText(String.format("%.2f", average));
            } else {
                averageDomainScoreField.clear();
            }
        } catch (NumberFormatException e) {
            averageDomainScoreField.clear();
        }
    }

    private void calculateGrowthScore() {
        try {
            if (!compositeScoreField.getText().isEmpty() &&
                !previousCompositeScoreField.getText().isEmpty()) {

                double current = Double.parseDouble(compositeScoreField.getText());
                double previous = Double.parseDouble(previousCompositeScoreField.getText());
                double growth = current - previous;

                growthScoreField.setText(String.format("%+.2f", growth));
            } else {
                growthScoreField.clear();
            }
        } catch (NumberFormatException e) {
            growthScoreField.clear();
        }
    }

    private void checkReclassificationCriteria() {
        try {
            if (!compositeScoreField.getText().isEmpty() &&
                !reclassificationThresholdField.getText().isEmpty()) {

                double current = Double.parseDouble(compositeScoreField.getText());
                double threshold = Double.parseDouble(reclassificationThresholdField.getText());

                meetsReclassificationCriteriaCheckBox.setSelected(current >= threshold);
            }
        } catch (NumberFormatException e) {
            meetsReclassificationCriteriaCheckBox.setSelected(false);
        }
    }

    private void addNumericValidation(TextField field, int min, int max) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                try {
                    int value = Integer.parseInt(newVal);
                    if (value < min || value > max) {
                        field.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                    } else {
                        field.setStyle("");
                    }
                } catch (NumberFormatException e) {
                    field.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                }
            } else {
                field.setStyle("");
            }
        });
    }

    @FXML
    private void handleSave() {
        if (viewMode) {
            return;
        }

        List<String> errors = validateForm();
        if (!errors.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please correct the following errors:");
            alert.setContentText(String.join("\n", errors));
            alert.showAndWait();
            return;
        }

        try {
            ELLAssessment assessment = buildAssessmentFromForm();

            if (currentAssessment == null) {
                ellManagementService.createAssessment(assessment);
                showSuccessAlert("Assessment created successfully!");
            } else {
                assessment.setId(currentAssessment.getId());
                ellManagementService.updateAssessment(assessment);
                showSuccessAlert("Assessment updated successfully!");
            }

            clearForm();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to save assessment");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleCancel() {
        clearForm();
    }

    private List<String> validateForm() {
        List<String> errors = new ArrayList<>();

        // Required fields
        if (ellStudentComboBox.getValue() == null) {
            errors.add("ELL Student is required");
        }

        if (assessmentTypeComboBox.getValue() == null) {
            errors.add("Assessment Type is required");
        }

        if (assessmentDatePicker.getValue() == null) {
            errors.add("Assessment Date is required");
        }

        if (assessmentPurposeComboBox.getValue() == null) {
            errors.add("Assessment Purpose is required");
        }

        // Federal compliance validation
        ELLAssessment.AssessmentPurpose purpose = assessmentPurposeComboBox.getValue();
        if (purpose == ELLAssessment.AssessmentPurpose.ANNUAL_PROFICIENCY) {
            if (!parentNotificationSentCheckBox.isSelected()) {
                errors.add("FEDERAL REQUIREMENT: Parent notification is required for annual proficiency assessments");
            }
            if (!reportedToStateCheckBox.isSelected()) {
                errors.add("FEDERAL REQUIREMENT: State reporting is required for annual proficiency assessments");
            }
        }

        // Validate proficiency levels are in range 1-6
        if (!listeningLevelField.getText().isEmpty()) {
            try {
                int level = Integer.parseInt(listeningLevelField.getText());
                if (level < 1 || level > 6) {
                    errors.add("Listening Level must be between 1 and 6");
                }
            } catch (NumberFormatException e) {
                errors.add("Listening Level must be a valid number");
            }
        }

        if (!speakingLevelField.getText().isEmpty()) {
            try {
                int level = Integer.parseInt(speakingLevelField.getText());
                if (level < 1 || level > 6) {
                    errors.add("Speaking Level must be between 1 and 6");
                }
            } catch (NumberFormatException e) {
                errors.add("Speaking Level must be a valid number");
            }
        }

        if (!readingLevelField.getText().isEmpty()) {
            try {
                int level = Integer.parseInt(readingLevelField.getText());
                if (level < 1 || level > 6) {
                    errors.add("Reading Level must be between 1 and 6");
                }
            } catch (NumberFormatException e) {
                errors.add("Reading Level must be a valid number");
            }
        }

        if (!writingLevelField.getText().isEmpty()) {
            try {
                int level = Integer.parseInt(writingLevelField.getText());
                if (level < 1 || level > 6) {
                    errors.add("Writing Level must be between 1 and 6");
                }
            } catch (NumberFormatException e) {
                errors.add("Writing Level must be a valid number");
            }
        }

        // Score validity validation
        if (!scoreValidCheckBox.isSelected() && invalidationReasonArea.getText().trim().isEmpty()) {
            errors.add("Invalidation reason is required when score is marked as invalid");
        }

        return errors;
    }

    private ELLAssessment buildAssessmentFromForm() {
        ELLAssessment assessment = new ELLAssessment();

        // Student & Assessment Information
        assessment.setEllStudent(ellStudentComboBox.getValue());
        assessment.setAssessmentType(assessmentTypeComboBox.getValue());
        assessment.setAssessmentDate(assessmentDatePicker.getValue());
        assessment.setAssessmentPurpose(assessmentPurposeComboBox.getValue());
        assessment.setTestForm(testFormField.getText());
        assessment.setTestVersion(testVersionField.getText());

        // Overall Results
        assessment.setPerformanceLevel(performanceLevelField.getText());
        assessment.setCompositeScore(parseDouble(compositeScoreField.getText()));
        assessment.setScaleScore(parseInteger(scaleScoreField.getText()));
        assessment.setPercentileRank(parseInteger(percentileRankField.getText()));

        // Domain Scores
        assessment.setListeningScore(parseDouble(listeningScoreField.getText()));
        assessment.setListeningLevel(parseInteger(listeningLevelField.getText()));
        assessment.setSpeakingScore(parseDouble(speakingScoreField.getText()));
        assessment.setSpeakingLevel(parseInteger(speakingLevelField.getText()));
        assessment.setReadingScore(parseDouble(readingScoreField.getText()));
        assessment.setReadingLevel(parseInteger(readingLevelField.getText()));
        assessment.setWritingScore(parseDouble(writingScoreField.getText()));
        assessment.setWritingLevel(parseInteger(writingLevelField.getText()));

        // Composite Scores
        assessment.setLiteracyCompositeScore(parseDouble(literacyCompositeScoreField.getText()));
        assessment.setOralLanguageCompositeScore(parseDouble(oralLanguageCompositeScoreField.getText()));

        // Progress & Growth
        assessment.setGrowthScore(parseDouble(growthScoreField.getText()));
        assessment.setMetGrowthTarget(metGrowthTargetCheckBox.isSelected());

        // Reclassification Criteria
        assessment.setReclassificationThreshold(parseDouble(reclassificationThresholdField.getText()));
        assessment.setMeetsReclassificationCriteria(meetsReclassificationCriteriaCheckBox.isSelected());
        assessment.setReclassificationNotes(reclassificationNotesArea.getText());

        // Administration Details
        assessment.setTestAdministrator(testAdministratorComboBox.getValue());
        assessment.setTestLocation(testLocationField.getText());
        assessment.setAccommodationsProvided(accommodationsProvidedArea.getText());
        assessment.setTestDuration(parseInteger(testDurationField.getText()));

        // Reporting & Compliance
        assessment.setResultsReceivedDate(resultsReceivedDatePicker.getValue());
        assessment.setParentNotificationSent(parentNotificationSentCheckBox.isSelected());
        assessment.setParentNotificationDate(parentNotificationDatePicker.getValue());
        assessment.setReportedToState(reportedToStateCheckBox.isSelected());
        assessment.setStateReportingDate(stateReportingDatePicker.getValue());

        // Score Validity
        assessment.setScoreValid(scoreValidCheckBox.isSelected());
        assessment.setInvalidationReason(invalidationReasonArea.getText());

        // Additional Notes
        assessment.setAdditionalNotes(additionalNotesArea.getText());

        return assessment;
    }

    private Double parseDouble(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void clearForm() {
        currentAssessment = null;
        viewMode = false;

        ellStudentComboBox.setValue(null);
        studentGradeField.clear();
        currentProficiencyField.clear();
        nativeLanguageField.clear();

        assessmentTypeComboBox.setValue(null);
        assessmentDatePicker.setValue(LocalDate.now());
        assessmentPurposeComboBox.setValue(null);
        testFormField.clear();
        testVersionField.clear();

        performanceLevelField.clear();
        compositeScoreField.clear();
        scaleScoreField.clear();
        percentileRankField.clear();

        listeningScoreField.clear();
        listeningLevelField.clear();
        speakingScoreField.clear();
        speakingLevelField.clear();
        readingScoreField.clear();
        readingLevelField.clear();
        writingScoreField.clear();
        writingLevelField.clear();
        averageDomainScoreField.clear();

        literacyCompositeScoreField.clear();
        oralLanguageCompositeScoreField.clear();

        previousAssessmentDateField.clear();
        previousCompositeScoreField.clear();
        growthScoreField.clear();
        metGrowthTargetCheckBox.setSelected(false);

        reclassificationThresholdField.clear();
        meetsReclassificationCriteriaCheckBox.setSelected(false);
        reclassificationNotesArea.clear();

        testAdministratorComboBox.setValue(null);
        testLocationField.clear();
        accommodationsProvidedArea.clear();
        testDurationField.clear();

        resultsReceivedDatePicker.setValue(null);
        parentNotificationSentCheckBox.setSelected(false);
        parentNotificationDatePicker.setValue(null);
        reportedToStateCheckBox.setSelected(false);
        stateReportingDatePicker.setValue(null);

        scoreValidCheckBox.setSelected(true);
        invalidationReasonArea.clear();
        invalidationReasonArea.setDisable(true);

        additionalNotesArea.clear();

        saveButton.setDisable(false);
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setELLStudent(ELLStudent ellStudent) {
        ellStudentComboBox.setValue(ellStudent);
        populateStudentInformation(ellStudent);
    }

    public void loadAssessment(ELLAssessment assessment) {
        this.currentAssessment = assessment;

        ellStudentComboBox.setValue(assessment.getEllStudent());
        if (assessment.getEllStudent() != null) {
            populateStudentInformation(assessment.getEllStudent());
        }

        assessmentTypeComboBox.setValue(assessment.getAssessmentType());
        assessmentDatePicker.setValue(assessment.getAssessmentDate());
        assessmentPurposeComboBox.setValue(assessment.getAssessmentPurpose());
        testFormField.setText(assessment.getTestForm());
        testVersionField.setText(assessment.getTestVersion());

        performanceLevelField.setText(assessment.getPerformanceLevel());
        compositeScoreField.setText(assessment.getCompositeScore() != null ?
                assessment.getCompositeScore().toString() : "");
        scaleScoreField.setText(assessment.getScaleScore() != null ?
                assessment.getScaleScore().toString() : "");
        percentileRankField.setText(assessment.getPercentileRank() != null ?
                assessment.getPercentileRank().toString() : "");

        listeningScoreField.setText(assessment.getListeningScore() != null ?
                assessment.getListeningScore().toString() : "");
        listeningLevelField.setText(assessment.getListeningLevel() != null ?
                assessment.getListeningLevel().toString() : "");
        speakingScoreField.setText(assessment.getSpeakingScore() != null ?
                assessment.getSpeakingScore().toString() : "");
        speakingLevelField.setText(assessment.getSpeakingLevel() != null ?
                assessment.getSpeakingLevel().toString() : "");
        readingScoreField.setText(assessment.getReadingScore() != null ?
                assessment.getReadingScore().toString() : "");
        readingLevelField.setText(assessment.getReadingLevel() != null ?
                assessment.getReadingLevel().toString() : "");
        writingScoreField.setText(assessment.getWritingScore() != null ?
                assessment.getWritingScore().toString() : "");
        writingLevelField.setText(assessment.getWritingLevel() != null ?
                assessment.getWritingLevel().toString() : "");

        literacyCompositeScoreField.setText(assessment.getLiteracyCompositeScore() != null ?
                assessment.getLiteracyCompositeScore().toString() : "");
        oralLanguageCompositeScoreField.setText(assessment.getOralLanguageCompositeScore() != null ?
                assessment.getOralLanguageCompositeScore().toString() : "");

        growthScoreField.setText(assessment.getGrowthScore() != null ?
                assessment.getGrowthScore().toString() : "");
        metGrowthTargetCheckBox.setSelected(assessment.isMetGrowthTarget());

        reclassificationThresholdField.setText(assessment.getReclassificationThreshold() != null ?
                assessment.getReclassificationThreshold().toString() : "");
        meetsReclassificationCriteriaCheckBox.setSelected(assessment.isMeetsReclassificationCriteria());
        reclassificationNotesArea.setText(assessment.getReclassificationNotes());

        testAdministratorComboBox.setValue(assessment.getTestAdministrator());
        testLocationField.setText(assessment.getTestLocation());
        accommodationsProvidedArea.setText(assessment.getAccommodationsProvided());
        testDurationField.setText(assessment.getTestDuration() != null ?
                assessment.getTestDuration().toString() : "");

        resultsReceivedDatePicker.setValue(assessment.getResultsReceivedDate());
        parentNotificationSentCheckBox.setSelected(assessment.isParentNotificationSent());
        parentNotificationDatePicker.setValue(assessment.getParentNotificationDate());
        reportedToStateCheckBox.setSelected(assessment.isReportedToState());
        stateReportingDatePicker.setValue(assessment.getStateReportingDate());

        scoreValidCheckBox.setSelected(assessment.isScoreValid());
        invalidationReasonArea.setText(assessment.getInvalidationReason());
        invalidationReasonArea.setDisable(assessment.isScoreValid());

        additionalNotesArea.setText(assessment.getAdditionalNotes());
    }

    public void setViewMode(boolean viewMode) {
        this.viewMode = viewMode;

        ellStudentComboBox.setDisable(viewMode);
        assessmentTypeComboBox.setDisable(viewMode);
        assessmentDatePicker.setDisable(viewMode);
        assessmentPurposeComboBox.setDisable(viewMode);
        testFormField.setEditable(!viewMode);
        testVersionField.setEditable(!viewMode);

        performanceLevelField.setEditable(!viewMode);
        compositeScoreField.setEditable(!viewMode);
        scaleScoreField.setEditable(!viewMode);
        percentileRankField.setEditable(!viewMode);

        listeningScoreField.setEditable(!viewMode);
        listeningLevelField.setEditable(!viewMode);
        speakingScoreField.setEditable(!viewMode);
        speakingLevelField.setEditable(!viewMode);
        readingScoreField.setEditable(!viewMode);
        readingLevelField.setEditable(!viewMode);
        writingScoreField.setEditable(!viewMode);
        writingLevelField.setEditable(!viewMode);

        literacyCompositeScoreField.setEditable(!viewMode);
        oralLanguageCompositeScoreField.setEditable(!viewMode);

        metGrowthTargetCheckBox.setDisable(viewMode);

        reclassificationThresholdField.setEditable(!viewMode);
        meetsReclassificationCriteriaCheckBox.setDisable(viewMode);
        reclassificationNotesArea.setEditable(!viewMode);

        testAdministratorComboBox.setDisable(viewMode);
        testLocationField.setEditable(!viewMode);
        accommodationsProvidedArea.setEditable(!viewMode);
        testDurationField.setEditable(!viewMode);

        resultsReceivedDatePicker.setDisable(viewMode);
        parentNotificationSentCheckBox.setDisable(viewMode);
        parentNotificationDatePicker.setDisable(viewMode);
        reportedToStateCheckBox.setDisable(viewMode);
        stateReportingDatePicker.setDisable(viewMode);

        scoreValidCheckBox.setDisable(viewMode);
        invalidationReasonArea.setEditable(!viewMode);

        additionalNotesArea.setEditable(!viewMode);

        saveButton.setDisable(viewMode);
    }
}
