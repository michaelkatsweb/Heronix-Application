package com.heronix.ui.controller;

import com.heronix.model.domain.GiftedAssessment;
import com.heronix.model.domain.GiftedStudent;
import com.heronix.model.domain.Teacher;
import com.heronix.service.GiftedManagementService;
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
public class GiftedAssessmentIdentificationController {

    @Autowired
    private GiftedManagementService giftedManagementService;

    @Autowired
    private TeacherService teacherService;

    // Student & Assessment Information
    @FXML private ComboBox<GiftedStudent> giftedStudentComboBox;
    @FXML private TextField gradeField;
    @FXML private ComboBox<GiftedAssessment.AssessmentType> assessmentTypeComboBox;
    @FXML private TextField assessmentNameField;
    @FXML private DatePicker assessmentDatePicker;
    @FXML private ComboBox<GiftedAssessment.AssessmentPurpose> assessmentPurposeComboBox;

    // Overall Scores
    @FXML private TextField overallScoreField;
    @FXML private TextField compositeScoreField;
    @FXML private TextField percentileRankField;
    @FXML private TextField standardScoreField;
    @FXML private TextField scaledScoreField;

    // Cognitive/IQ Assessment
    @FXML private TextField fullScaleIqField;
    @FXML private TextField verbalIqField;
    @FXML private TextField performanceIqField;
    @FXML private TextField workingMemoryIndexField;
    @FXML private TextField processingSpeedIndexField;

    // Achievement Scores
    @FXML private TextField readingScoreField;
    @FXML private TextField readingPercentileField;
    @FXML private TextField mathScoreField;
    @FXML private TextField mathPercentileField;
    @FXML private TextField writingScoreField;
    @FXML private TextField writingPercentileField;
    @FXML private TextField scienceScoreField;
    @FXML private TextField sciencePercentileField;
    @FXML private TextField averageAchievementField;

    // Creativity & Other Scores
    @FXML private TextField creativityScoreField;
    @FXML private TextField creativityPercentileField;
    @FXML private TextField leadershipScoreField;
    @FXML private TextField artsScoreField;

    // Eligibility & Recommendations
    @FXML private CheckBox meetsEligibilityCriteriaCheckBox;
    @FXML private TextField eligibilityThresholdScoreField;
    @FXML private CheckBox recommendedForServicesCheckBox;
    @FXML private ComboBox<GiftedStudent.GiftedArea> recommendedAreaComboBox;
    @FXML private TextArea recommendationsArea;

    // Administration Details
    @FXML private ComboBox<Teacher> administratorComboBox;
    @FXML private TextField administrationLocationField;
    @FXML private TextArea testingConditionsArea;

    // Results & Reporting
    @FXML private DatePicker resultsReceivedDatePicker;
    @FXML private CheckBox validScoreCheckBox;
    @FXML private TextField invalidationReasonField;
    @FXML private CheckBox parentNotificationSentCheckBox;
    @FXML private DatePicker parentNotificationDatePicker;

    // Interpretation & Analysis
    @FXML private TextArea interpretationArea;
    @FXML private TextArea strengthsIdentifiedArea;
    @FXML private TextArea areasForDevelopmentArea;
    @FXML private TextArea notesArea;

    // Action Buttons
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private GiftedAssessment currentAssessment;
    private boolean viewMode = false;

    @FXML
    public void initialize() {
        setupComboBoxes();
        setupListeners();
        loadInitialData();
    }

    private void setupComboBoxes() {
        // Gifted Student ComboBox
        giftedStudentComboBox.setConverter(new StringConverter<GiftedStudent>() {
            @Override
            public String toString(GiftedStudent gs) {
                if (gs == null || gs.getStudent() == null) return "";
                return gs.getStudent().getLastName() + ", " +
                       gs.getStudent().getFirstName() + " (" +
                       gs.getStudent().getStudentId() + ")";
            }

            @Override
            public GiftedStudent fromString(String string) {
                return null;
            }
        });

        // Assessment Type ComboBox
        assessmentTypeComboBox.setItems(FXCollections.observableArrayList(
                GiftedAssessment.AssessmentType.values()));
        assessmentTypeComboBox.setConverter(new StringConverter<GiftedAssessment.AssessmentType>() {
            @Override
            public String toString(GiftedAssessment.AssessmentType type) {
                return type != null ? type.getDisplayName() : "";
            }

            @Override
            public GiftedAssessment.AssessmentType fromString(String string) {
                return null;
            }
        });

        // Assessment Purpose ComboBox
        assessmentPurposeComboBox.setItems(FXCollections.observableArrayList(
                GiftedAssessment.AssessmentPurpose.values()));
        assessmentPurposeComboBox.setConverter(new StringConverter<GiftedAssessment.AssessmentPurpose>() {
            @Override
            public String toString(GiftedAssessment.AssessmentPurpose purpose) {
                return purpose != null ? purpose.getDisplayName() : "";
            }

            @Override
            public GiftedAssessment.AssessmentPurpose fromString(String string) {
                return null;
            }
        });

        // Recommended Area ComboBox
        recommendedAreaComboBox.setItems(FXCollections.observableArrayList(
                GiftedStudent.GiftedArea.values()));
        recommendedAreaComboBox.setConverter(new StringConverter<GiftedStudent.GiftedArea>() {
            @Override
            public String toString(GiftedStudent.GiftedArea area) {
                return area != null ? area.getDisplayName() : "";
            }

            @Override
            public GiftedStudent.GiftedArea fromString(String string) {
                return null;
            }
        });

        // Administrator ComboBox
        administratorComboBox.setConverter(new StringConverter<Teacher>() {
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
        // Auto-populate student grade when student is selected
        giftedStudentComboBox.setOnAction(e -> {
            GiftedStudent selected = giftedStudentComboBox.getValue();
            if (selected != null && selected.getStudent() != null) {
                gradeField.setText(selected.getStudent().getGradeLevel() != null ?
                        selected.getStudent().getGradeLevel().toString() : "");
            }
        });

        // Auto-calculate average achievement percentile
        readingPercentileField.textProperty().addListener((obs, old, newVal) -> calculateAverageAchievement());
        mathPercentileField.textProperty().addListener((obs, old, newVal) -> calculateAverageAchievement());
        writingPercentileField.textProperty().addListener((obs, old, newVal) -> calculateAverageAchievement());
        sciencePercentileField.textProperty().addListener((obs, old, newVal) -> calculateAverageAchievement());

        // Auto-check eligibility based on threshold
        compositeScoreField.textProperty().addListener((obs, old, newVal) -> checkEligibilityThreshold());
        eligibilityThresholdScoreField.textProperty().addListener((obs, old, newVal) -> checkEligibilityThreshold());

        // Disable invalidation reason when score is valid
        validScoreCheckBox.selectedProperty().addListener((obs, old, newVal) -> {
            invalidationReasonField.setDisable(newVal);
            if (newVal) {
                invalidationReasonField.clear();
            }
        });

        // Percentile validation (0-100)
        addPercentileValidation(percentileRankField);
        addPercentileValidation(readingPercentileField);
        addPercentileValidation(mathPercentileField);
        addPercentileValidation(writingPercentileField);
        addPercentileValidation(sciencePercentileField);
        addPercentileValidation(creativityPercentileField);
    }

    private void loadInitialData() {
        // Load all gifted students
        List<GiftedStudent> giftedStudents = giftedManagementService.findAllGiftedStudents().stream()
                .sorted((s1, s2) -> {
                    String name1 = s1.getStudent().getLastName() + s1.getStudent().getFirstName();
                    String name2 = s2.getStudent().getLastName() + s2.getStudent().getFirstName();
                    return name1.compareToIgnoreCase(name2);
                })
                .collect(Collectors.toList());
        giftedStudentComboBox.setItems(FXCollections.observableArrayList(giftedStudents));

        // Load psychologists and assessment specialists
        List<Teacher> administrators = teacherService.findAllTeachers().stream()
                .filter(t -> {
                    List<String> certifications = t.getCertifications();
                    return certifications != null &&
                           (certifications.stream().anyMatch(c -> c.contains("Psychologist")) ||
                            certifications.stream().anyMatch(c -> c.contains("Assessment")) ||
                            certifications.stream().anyMatch(c -> c.contains("Gifted")) ||
                            certifications.stream().anyMatch(c -> c.contains("GT")));
                })
                .sorted((t1, t2) -> {
                    String name1 = t1.getLastName() + t1.getFirstName();
                    String name2 = t2.getLastName() + t2.getFirstName();
                    return name1.compareToIgnoreCase(name2);
                })
                .collect(Collectors.toList());
        administratorComboBox.setItems(FXCollections.observableArrayList(administrators));

        // Set default values
        assessmentDatePicker.setValue(LocalDate.now());
        validScoreCheckBox.setSelected(true);
        invalidationReasonField.setDisable(true);
    }

    private void calculateAverageAchievement() {
        try {
            int count = 0;
            double sum = 0.0;

            if (!readingPercentileField.getText().isEmpty()) {
                sum += Double.parseDouble(readingPercentileField.getText());
                count++;
            }
            if (!mathPercentileField.getText().isEmpty()) {
                sum += Double.parseDouble(mathPercentileField.getText());
                count++;
            }
            if (!writingPercentileField.getText().isEmpty()) {
                sum += Double.parseDouble(writingPercentileField.getText());
                count++;
            }
            if (!sciencePercentileField.getText().isEmpty()) {
                sum += Double.parseDouble(sciencePercentileField.getText());
                count++;
            }

            if (count > 0) {
                double average = sum / count;
                averageAchievementField.setText(String.format("%.1f", average));
            } else {
                averageAchievementField.clear();
            }
        } catch (NumberFormatException e) {
            averageAchievementField.clear();
        }
    }

    private void checkEligibilityThreshold() {
        try {
            if (!compositeScoreField.getText().isEmpty() &&
                !eligibilityThresholdScoreField.getText().isEmpty()) {

                double composite = Double.parseDouble(compositeScoreField.getText());
                double threshold = Double.parseDouble(eligibilityThresholdScoreField.getText());

                meetsEligibilityCriteriaCheckBox.setSelected(composite >= threshold);
            }
        } catch (NumberFormatException e) {
            // Do nothing
        }
    }

    private void addPercentileValidation(TextField field) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                try {
                    int percentile = Integer.parseInt(newVal);
                    if (percentile < 0 || percentile > 100) {
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
            GiftedAssessment assessment = buildAssessmentFromForm();

            if (currentAssessment == null) {
                giftedManagementService.createAssessment(assessment);
                showSuccessAlert("Assessment created successfully!");
            } else {
                assessment.setId(currentAssessment.getId());
                giftedManagementService.updateAssessment(assessment);
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
        if (giftedStudentComboBox.getValue() == null) {
            errors.add("Gifted Student is required");
        }

        if (assessmentTypeComboBox.getValue() == null) {
            errors.add("Assessment Type is required");
        }

        if (assessmentDatePicker.getValue() == null) {
            errors.add("Assessment Date is required");
        }

        // Validate percentiles are in range 0-100
        validatePercentileField(percentileRankField, "Percentile Rank", errors);
        validatePercentileField(readingPercentileField, "Reading Percentile", errors);
        validatePercentileField(mathPercentileField, "Math Percentile", errors);
        validatePercentileField(writingPercentileField, "Writing Percentile", errors);
        validatePercentileField(sciencePercentileField, "Science Percentile", errors);
        validatePercentileField(creativityPercentileField, "Creativity Percentile", errors);

        // Score validity validation
        if (!validScoreCheckBox.isSelected() && invalidationReasonField.getText().trim().isEmpty()) {
            errors.add("Invalidation reason is required when score is marked as invalid");
        }

        // Parent notification validation for eligibility determination
        if (assessmentPurposeComboBox.getValue() == GiftedAssessment.AssessmentPurpose.ELIGIBILITY_DETERMINATION) {
            if (!parentNotificationSentCheckBox.isSelected()) {
                errors.add("WARNING: Parent notification is typically required for eligibility determination");
            }
        }

        return errors;
    }

    private void validatePercentileField(TextField field, String fieldName, List<String> errors) {
        if (!field.getText().isEmpty()) {
            try {
                int percentile = Integer.parseInt(field.getText());
                if (percentile < 0 || percentile > 100) {
                    errors.add(fieldName + " must be between 0 and 100");
                }
            } catch (NumberFormatException e) {
                errors.add(fieldName + " must be a valid number");
            }
        }
    }

    private GiftedAssessment buildAssessmentFromForm() {
        GiftedAssessment assessment = new GiftedAssessment();

        // Student & Assessment Information
        assessment.setGiftedStudent(giftedStudentComboBox.getValue());
        assessment.setAssessmentType(assessmentTypeComboBox.getValue());
        assessment.setAssessmentName(assessmentNameField.getText());
        assessment.setAssessmentDate(assessmentDatePicker.getValue());
        assessment.setAssessmentPurpose(assessmentPurposeComboBox.getValue());

        // Overall Scores
        assessment.setOverallScore(parseDouble(overallScoreField.getText()));
        assessment.setCompositeScore(parseDouble(compositeScoreField.getText()));
        assessment.setPercentileRank(parseInteger(percentileRankField.getText()));
        assessment.setStandardScore(parseInteger(standardScoreField.getText()));
        assessment.setScaledScore(parseInteger(scaledScoreField.getText()));

        // Cognitive/IQ Assessment
        assessment.setFullScaleIq(parseInteger(fullScaleIqField.getText()));
        assessment.setVerbalIq(parseInteger(verbalIqField.getText()));
        assessment.setPerformanceIq(parseInteger(performanceIqField.getText()));
        assessment.setWorkingMemoryIndex(parseInteger(workingMemoryIndexField.getText()));
        assessment.setProcessingSpeedIndex(parseInteger(processingSpeedIndexField.getText()));

        // Achievement Scores
        assessment.setReadingScore(parseDouble(readingScoreField.getText()));
        assessment.setReadingPercentile(parseInteger(readingPercentileField.getText()));
        assessment.setMathScore(parseDouble(mathScoreField.getText()));
        assessment.setMathPercentile(parseInteger(mathPercentileField.getText()));
        assessment.setWritingScore(parseDouble(writingScoreField.getText()));
        assessment.setWritingPercentile(parseInteger(writingPercentileField.getText()));
        assessment.setScienceScore(parseDouble(scienceScoreField.getText()));
        assessment.setSciencePercentile(parseInteger(sciencePercentileField.getText()));

        // Creativity & Other Scores
        assessment.setCreativityScore(parseDouble(creativityScoreField.getText()));
        assessment.setCreativityPercentile(parseInteger(creativityPercentileField.getText()));
        assessment.setLeadershipScore(parseDouble(leadershipScoreField.getText()));
        assessment.setArtsScore(parseDouble(artsScoreField.getText()));

        // Eligibility & Recommendations
        assessment.setMeetsEligibilityCriteria(meetsEligibilityCriteriaCheckBox.isSelected());
        assessment.setEligibilityThresholdScore(parseDouble(eligibilityThresholdScoreField.getText()));
        assessment.setRecommendedForServices(recommendedForServicesCheckBox.isSelected());
        assessment.setRecommendedArea(recommendedAreaComboBox.getValue());
        assessment.setRecommendations(recommendationsArea.getText());

        // Administration Details
        assessment.setAdministrator(administratorComboBox.getValue());
        assessment.setAdministrationLocation(administrationLocationField.getText());
        assessment.setTestingConditions(testingConditionsArea.getText());

        // Results & Reporting
        assessment.setResultsReceivedDate(resultsReceivedDatePicker.getValue());
        assessment.setValidScore(validScoreCheckBox.isSelected());
        assessment.setInvalidationReason(invalidationReasonField.getText());
        assessment.setParentNotificationSent(parentNotificationSentCheckBox.isSelected());
        assessment.setParentNotificationDate(parentNotificationDatePicker.getValue());

        // Interpretation & Analysis
        assessment.setInterpretation(interpretationArea.getText());
        assessment.setStrengthsIdentified(strengthsIdentifiedArea.getText());
        assessment.setAreasForDevelopment(areasForDevelopmentArea.getText());
        assessment.setNotes(notesArea.getText());

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

        giftedStudentComboBox.setValue(null);
        gradeField.clear();
        assessmentTypeComboBox.setValue(null);
        assessmentNameField.clear();
        assessmentDatePicker.setValue(LocalDate.now());
        assessmentPurposeComboBox.setValue(null);

        overallScoreField.clear();
        compositeScoreField.clear();
        percentileRankField.clear();
        standardScoreField.clear();
        scaledScoreField.clear();

        fullScaleIqField.clear();
        verbalIqField.clear();
        performanceIqField.clear();
        workingMemoryIndexField.clear();
        processingSpeedIndexField.clear();

        readingScoreField.clear();
        readingPercentileField.clear();
        mathScoreField.clear();
        mathPercentileField.clear();
        writingScoreField.clear();
        writingPercentileField.clear();
        scienceScoreField.clear();
        sciencePercentileField.clear();
        averageAchievementField.clear();

        creativityScoreField.clear();
        creativityPercentileField.clear();
        leadershipScoreField.clear();
        artsScoreField.clear();

        meetsEligibilityCriteriaCheckBox.setSelected(false);
        eligibilityThresholdScoreField.clear();
        recommendedForServicesCheckBox.setSelected(false);
        recommendedAreaComboBox.setValue(null);
        recommendationsArea.clear();

        administratorComboBox.setValue(null);
        administrationLocationField.clear();
        testingConditionsArea.clear();

        resultsReceivedDatePicker.setValue(null);
        validScoreCheckBox.setSelected(true);
        invalidationReasonField.clear();
        invalidationReasonField.setDisable(true);
        parentNotificationSentCheckBox.setSelected(false);
        parentNotificationDatePicker.setValue(null);

        interpretationArea.clear();
        strengthsIdentifiedArea.clear();
        areasForDevelopmentArea.clear();
        notesArea.clear();

        saveButton.setDisable(false);
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setGiftedStudent(GiftedStudent giftedStudent) {
        giftedStudentComboBox.setValue(giftedStudent);
        if (giftedStudent.getStudent() != null) {
            gradeField.setText(giftedStudent.getStudent().getGradeLevel() != null ?
                    giftedStudent.getStudent().getGradeLevel().toString() : "");
        }
    }

    public void loadAssessment(GiftedAssessment assessment) {
        this.currentAssessment = assessment;

        giftedStudentComboBox.setValue(assessment.getGiftedStudent());
        if (assessment.getGiftedStudent() != null && assessment.getGiftedStudent().getStudent() != null) {
            gradeField.setText(assessment.getGiftedStudent().getStudent().getGradeLevel() != null ?
                    assessment.getGiftedStudent().getStudent().getGradeLevel().toString() : "");
        }

        assessmentTypeComboBox.setValue(assessment.getAssessmentType());
        assessmentNameField.setText(assessment.getAssessmentName());
        assessmentDatePicker.setValue(assessment.getAssessmentDate());
        assessmentPurposeComboBox.setValue(assessment.getAssessmentPurpose());

        overallScoreField.setText(assessment.getOverallScore() != null ?
                assessment.getOverallScore().toString() : "");
        compositeScoreField.setText(assessment.getCompositeScore() != null ?
                assessment.getCompositeScore().toString() : "");
        percentileRankField.setText(assessment.getPercentileRank() != null ?
                assessment.getPercentileRank().toString() : "");
        standardScoreField.setText(assessment.getStandardScore() != null ?
                assessment.getStandardScore().toString() : "");
        scaledScoreField.setText(assessment.getScaledScore() != null ?
                assessment.getScaledScore().toString() : "");

        fullScaleIqField.setText(assessment.getFullScaleIq() != null ?
                assessment.getFullScaleIq().toString() : "");
        verbalIqField.setText(assessment.getVerbalIq() != null ?
                assessment.getVerbalIq().toString() : "");
        performanceIqField.setText(assessment.getPerformanceIq() != null ?
                assessment.getPerformanceIq().toString() : "");
        workingMemoryIndexField.setText(assessment.getWorkingMemoryIndex() != null ?
                assessment.getWorkingMemoryIndex().toString() : "");
        processingSpeedIndexField.setText(assessment.getProcessingSpeedIndex() != null ?
                assessment.getProcessingSpeedIndex().toString() : "");

        readingScoreField.setText(assessment.getReadingScore() != null ?
                assessment.getReadingScore().toString() : "");
        readingPercentileField.setText(assessment.getReadingPercentile() != null ?
                assessment.getReadingPercentile().toString() : "");
        mathScoreField.setText(assessment.getMathScore() != null ?
                assessment.getMathScore().toString() : "");
        mathPercentileField.setText(assessment.getMathPercentile() != null ?
                assessment.getMathPercentile().toString() : "");
        writingScoreField.setText(assessment.getWritingScore() != null ?
                assessment.getWritingScore().toString() : "");
        writingPercentileField.setText(assessment.getWritingPercentile() != null ?
                assessment.getWritingPercentile().toString() : "");
        scienceScoreField.setText(assessment.getScienceScore() != null ?
                assessment.getScienceScore().toString() : "");
        sciencePercentileField.setText(assessment.getSciencePercentile() != null ?
                assessment.getSciencePercentile().toString() : "");

        creativityScoreField.setText(assessment.getCreativityScore() != null ?
                assessment.getCreativityScore().toString() : "");
        creativityPercentileField.setText(assessment.getCreativityPercentile() != null ?
                assessment.getCreativityPercentile().toString() : "");
        leadershipScoreField.setText(assessment.getLeadershipScore() != null ?
                assessment.getLeadershipScore().toString() : "");
        artsScoreField.setText(assessment.getArtsScore() != null ?
                assessment.getArtsScore().toString() : "");

        meetsEligibilityCriteriaCheckBox.setSelected(assessment.getMeetsEligibilityCriteria() != null &&
                assessment.getMeetsEligibilityCriteria());
        eligibilityThresholdScoreField.setText(assessment.getEligibilityThresholdScore() != null ?
                assessment.getEligibilityThresholdScore().toString() : "");
        recommendedForServicesCheckBox.setSelected(assessment.getRecommendedForServices() != null &&
                assessment.getRecommendedForServices());
        recommendedAreaComboBox.setValue(assessment.getRecommendedArea());
        recommendationsArea.setText(assessment.getRecommendations());

        administratorComboBox.setValue(assessment.getAdministrator());
        administrationLocationField.setText(assessment.getAdministrationLocation());
        testingConditionsArea.setText(assessment.getTestingConditions());

        resultsReceivedDatePicker.setValue(assessment.getResultsReceivedDate());
        validScoreCheckBox.setSelected(assessment.getValidScore() != null && assessment.getValidScore());
        invalidationReasonField.setText(assessment.getInvalidationReason());
        invalidationReasonField.setDisable(assessment.getValidScore() != null && assessment.getValidScore());
        parentNotificationSentCheckBox.setSelected(assessment.getParentNotificationSent() != null &&
                assessment.getParentNotificationSent());
        parentNotificationDatePicker.setValue(assessment.getParentNotificationDate());

        interpretationArea.setText(assessment.getInterpretation());
        strengthsIdentifiedArea.setText(assessment.getStrengthsIdentified());
        areasForDevelopmentArea.setText(assessment.getAreasForDevelopment());
        notesArea.setText(assessment.getNotes());
    }

    public void setViewMode(boolean viewMode) {
        this.viewMode = viewMode;
        saveButton.setDisable(viewMode);
    }
}
