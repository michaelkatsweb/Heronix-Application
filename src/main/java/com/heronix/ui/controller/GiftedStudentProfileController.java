package com.heronix.ui.controller;

import com.heronix.model.domain.GiftedStudent;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.Teacher;
import com.heronix.service.GiftedManagementService;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GiftedStudentProfileController {

    @Autowired
    private GiftedManagementService giftedManagementService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    // Student Information
    @FXML private ComboBox<Student> studentComboBox;
    @FXML private TextField gradeField;
    @FXML private TextField studentIdField;
    @FXML private TextField currentGpaField;

    // Referral & Identification
    @FXML private DatePicker referralDatePicker;
    @FXML private ComboBox<GiftedStudent.ReferralType> referralTypeComboBox;
    @FXML private TextField referralSourceField;
    @FXML private DatePicker screeningDatePicker;
    @FXML private CheckBox screeningCompletedCheckBox;
    @FXML private DatePicker identificationDatePicker;

    // Gifted Status & Areas
    @FXML private ComboBox<GiftedStudent.GiftedStatus> giftedStatusComboBox;
    @FXML private ComboBox<GiftedStudent.GiftedArea> primaryGiftedAreaComboBox;
    @FXML private TextField additionalGiftedAreasField;

    // Eligibility & Assessment Scores
    @FXML private DatePicker eligibilityDeterminationDatePicker;
    @FXML private CheckBox eligibleForServicesCheckBox;
    @FXML private TextField iqScoreField;
    @FXML private TextField achievementPercentileField;
    @FXML private TextField compositeScoreField;
    @FXML private TextArea eligibilityCriteriaMetArea;

    // Program Placement
    @FXML private ComboBox<GiftedStudent.ProgramType> programTypeComboBox;
    @FXML private DatePicker programEntryDatePicker;
    @FXML private DatePicker programExitDatePicker;
    @FXML private TextField programPlacementsField;

    // Service Delivery
    @FXML private ComboBox<GiftedStudent.ServiceDeliveryModel> serviceDeliveryModelComboBox;
    @FXML private TextField serviceMinutesPerWeekField;
    @FXML private TextField serviceFrequencyField;
    @FXML private ComboBox<Teacher> serviceProviderComboBox;

    // Advanced Coursework & Acceleration
    @FXML private TextField apCoursesEnrolledField;
    @FXML private TextField honorsCoursesEnrolledField;
    @FXML private TextField advancedCoursesField;
    @FXML private CheckBox dualEnrollmentCheckBox;
    @FXML private CheckBox gradeAccelerationCheckBox;
    @FXML private TextField subjectAccelerationField;

    // Enrichment & Cluster Grouping
    @FXML private TextField enrichmentProgramsField;
    @FXML private TextArea competitionParticipationArea;
    @FXML private TextArea specialProjectsArea;
    @FXML private CheckBox clusterGroupedCheckBox;
    @FXML private TextField clusterGroupNameField;
    @FXML private TextField clusterTeacherField;

    // Talent Development & Mentorship
    @FXML private TextField talentAreaField;
    @FXML private CheckBox talentDevelopmentPlanActiveCheckBox;
    @FXML private TextArea talentDevelopmentGoalsArea;
    @FXML private CheckBox mentorshipProgramCheckBox;
    @FXML private TextField mentorNameField;

    // Progress Monitoring & Review
    @FXML private DatePicker lastProgressReviewDatePicker;
    @FXML private DatePicker nextProgressReviewDatePicker;
    @FXML private CheckBox annualReviewRequiredCheckBox;
    @FXML private DatePicker nextAnnualReviewDatePicker;
    @FXML private TextField performanceLevelField;
    @FXML private CheckBox meetingExpectationsCheckBox;
    @FXML private TextArea concernsArea;

    // Parent Communication
    @FXML private CheckBox parentNotificationSentCheckBox;
    @FXML private DatePicker parentNotificationDatePicker;
    @FXML private CheckBox parentConsentReceivedCheckBox;
    @FXML private DatePicker parentConsentDatePicker;

    // Student Characteristics
    @FXML private TextArea learningCharacteristicsArea;
    @FXML private TextArea socialEmotionalNeedsArea;
    @FXML private TextArea strengthsArea;
    @FXML private TextArea interestsArea;
    @FXML private TextArea notesArea;

    // Action Buttons
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private GiftedStudent currentGiftedStudent;
    private boolean viewMode = false;

    @FXML
    public void initialize() {
        setupComboBoxes();
        setupListeners();
        loadInitialData();
    }

    private void setupComboBoxes() {
        // Student ComboBox
        studentComboBox.setConverter(new StringConverter<Student>() {
            @Override
            public String toString(Student student) {
                if (student == null) return "";
                return student.getLastName() + ", " + student.getFirstName() +
                       " (" + student.getStudentId() + ")";
            }

            @Override
            public Student fromString(String string) {
                return null;
            }
        });

        // Referral Type ComboBox
        referralTypeComboBox.setItems(FXCollections.observableArrayList(
                GiftedStudent.ReferralType.values()));
        referralTypeComboBox.setConverter(new StringConverter<GiftedStudent.ReferralType>() {
            @Override
            public String toString(GiftedStudent.ReferralType type) {
                return type != null ? type.getDisplayName() : "";
            }

            @Override
            public GiftedStudent.ReferralType fromString(String string) {
                return null;
            }
        });

        // Gifted Status ComboBox
        giftedStatusComboBox.setItems(FXCollections.observableArrayList(
                GiftedStudent.GiftedStatus.values()));
        giftedStatusComboBox.setConverter(new StringConverter<GiftedStudent.GiftedStatus>() {
            @Override
            public String toString(GiftedStudent.GiftedStatus status) {
                return status != null ? status.getDisplayName() : "";
            }

            @Override
            public GiftedStudent.GiftedStatus fromString(String string) {
                return null;
            }
        });

        // Primary Gifted Area ComboBox
        primaryGiftedAreaComboBox.setItems(FXCollections.observableArrayList(
                GiftedStudent.GiftedArea.values()));
        primaryGiftedAreaComboBox.setConverter(new StringConverter<GiftedStudent.GiftedArea>() {
            @Override
            public String toString(GiftedStudent.GiftedArea area) {
                return area != null ? area.getDisplayName() : "";
            }

            @Override
            public GiftedStudent.GiftedArea fromString(String string) {
                return null;
            }
        });

        // Program Type ComboBox
        programTypeComboBox.setItems(FXCollections.observableArrayList(
                GiftedStudent.ProgramType.values()));
        programTypeComboBox.setConverter(new StringConverter<GiftedStudent.ProgramType>() {
            @Override
            public String toString(GiftedStudent.ProgramType type) {
                return type != null ? type.getDisplayName() : "";
            }

            @Override
            public GiftedStudent.ProgramType fromString(String string) {
                return null;
            }
        });

        // Service Delivery Model ComboBox
        serviceDeliveryModelComboBox.setItems(FXCollections.observableArrayList(
                GiftedStudent.ServiceDeliveryModel.values()));
        serviceDeliveryModelComboBox.setConverter(new StringConverter<GiftedStudent.ServiceDeliveryModel>() {
            @Override
            public String toString(GiftedStudent.ServiceDeliveryModel model) {
                return model != null ? model.getDisplayName() : "";
            }

            @Override
            public GiftedStudent.ServiceDeliveryModel fromString(String string) {
                return null;
            }
        });

        // Service Provider ComboBox
        serviceProviderComboBox.setConverter(new StringConverter<Teacher>() {
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
        // Auto-populate student information when student is selected
        studentComboBox.setOnAction(e -> {
            Student selected = studentComboBox.getValue();
            if (selected != null) {
                populateStudentInformation(selected);
            }
        });

        // Disable cluster group fields when not cluster grouped
        clusterGroupedCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            clusterGroupNameField.setDisable(!newVal);
            clusterTeacherField.setDisable(!newVal);
            if (!newVal) {
                clusterGroupNameField.clear();
                clusterTeacherField.clear();
            }
        });

        // Disable mentorship fields when not in mentorship program
        mentorshipProgramCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            mentorNameField.setDisable(!newVal);
            if (!newVal) {
                mentorNameField.clear();
            }
        });

        // Disable talent development goals when plan not active
        talentDevelopmentPlanActiveCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            talentDevelopmentGoalsArea.setDisable(!newVal);
            if (!newVal) {
                talentDevelopmentGoalsArea.clear();
            }
        });

        // GPA validation (0.00 - 4.00)
        currentGpaField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                try {
                    double gpa = Double.parseDouble(newVal);
                    if (gpa < 0.0 || gpa > 4.0) {
                        currentGpaField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                    } else {
                        currentGpaField.setStyle("");
                    }
                } catch (NumberFormatException e) {
                    currentGpaField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                }
            } else {
                currentGpaField.setStyle("");
            }
        });

        // Achievement percentile validation (0-100)
        achievementPercentileField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                try {
                    int percentile = Integer.parseInt(newVal);
                    if (percentile < 0 || percentile > 100) {
                        achievementPercentileField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                    } else {
                        achievementPercentileField.setStyle("");
                    }
                } catch (NumberFormatException e) {
                    achievementPercentileField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                }
            } else {
                achievementPercentileField.setStyle("");
            }
        });
    }

    private void loadInitialData() {
        // Load all students
        List<Student> students = studentService.findAllStudents().stream()
                .sorted((s1, s2) -> {
                    String name1 = s1.getLastName() + s1.getFirstName();
                    String name2 = s2.getLastName() + s2.getFirstName();
                    return name1.compareToIgnoreCase(name2);
                })
                .collect(Collectors.toList());
        studentComboBox.setItems(FXCollections.observableArrayList(students));

        // Load gifted program teachers
        List<Teacher> teachers = teacherService.findAllTeachers().stream()
                .filter(t -> {
                    List<String> certifications = t.getCertifications();
                    return certifications != null &&
                           (certifications.stream().anyMatch(c -> c.contains("Gifted")) ||
                            certifications.stream().anyMatch(c -> c.contains("GT")) ||
                            certifications.stream().anyMatch(c -> c.contains("Talented")));
                })
                .sorted((t1, t2) -> {
                    String name1 = t1.getLastName() + t1.getFirstName();
                    String name2 = t2.getLastName() + t2.getFirstName();
                    return name1.compareToIgnoreCase(name2);
                })
                .collect(Collectors.toList());
        serviceProviderComboBox.setItems(FXCollections.observableArrayList(teachers));

        // Set default values
        giftedStatusComboBox.setValue(GiftedStudent.GiftedStatus.REFERRED);
        annualReviewRequiredCheckBox.setSelected(true);
        meetingExpectationsCheckBox.setSelected(true);

        // Disable conditional fields initially
        clusterGroupNameField.setDisable(true);
        clusterTeacherField.setDisable(true);
        mentorNameField.setDisable(true);
        talentDevelopmentGoalsArea.setDisable(true);
    }

    private void populateStudentInformation(Student student) {
        studentIdField.setText(student.getStudentId());
        gradeField.setText(student.getGradeLevel() != null ?
                student.getGradeLevel().toString() : "");

        // Check if student already has a gifted profile
        giftedManagementService.findByStudent(student).ifPresent(this::loadGiftedStudent);
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
            GiftedStudent giftedStudent = buildGiftedStudentFromForm();

            if (currentGiftedStudent == null) {
                giftedManagementService.createGiftedStudent(giftedStudent);
                showSuccessAlert("Gifted student profile created successfully!");
            } else {
                giftedStudent.setId(currentGiftedStudent.getId());
                giftedManagementService.updateGiftedStudent(giftedStudent);
                showSuccessAlert("Gifted student profile updated successfully!");
            }

            clearForm();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to save gifted student profile");
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
        if (studentComboBox.getValue() == null) {
            errors.add("Student is required");
        }

        if (giftedStatusComboBox.getValue() == null) {
            errors.add("Gifted Status is required");
        }

        // GPA validation
        if (!currentGpaField.getText().isEmpty()) {
            try {
                double gpa = Double.parseDouble(currentGpaField.getText());
                if (gpa < 0.0 || gpa > 4.0) {
                    errors.add("GPA must be between 0.00 and 4.00");
                }
            } catch (NumberFormatException e) {
                errors.add("GPA must be a valid number");
            }
        }

        // Achievement percentile validation
        if (!achievementPercentileField.getText().isEmpty()) {
            try {
                int percentile = Integer.parseInt(achievementPercentileField.getText());
                if (percentile < 0 || percentile > 100) {
                    errors.add("Achievement percentile must be between 0 and 100");
                }
            } catch (NumberFormatException e) {
                errors.add("Achievement percentile must be a valid number");
            }
        }

        // Parent consent validation for active students
        GiftedStudent.GiftedStatus status = giftedStatusComboBox.getValue();
        if (status == GiftedStudent.GiftedStatus.ACTIVE || status == GiftedStudent.GiftedStatus.ELIGIBLE) {
            if (!parentConsentReceivedCheckBox.isSelected()) {
                errors.add("WARNING: Parent consent is typically required for active gifted services");
            }
        }

        return errors;
    }

    private GiftedStudent buildGiftedStudentFromForm() {
        GiftedStudent giftedStudent = new GiftedStudent();

        // Student Information
        giftedStudent.setStudent(studentComboBox.getValue());
        giftedStudent.setCurrentGpa(parseDouble(currentGpaField.getText()));

        // Referral & Identification
        giftedStudent.setReferralDate(referralDatePicker.getValue());
        giftedStudent.setReferralType(referralTypeComboBox.getValue());
        giftedStudent.setReferralSource(referralSourceField.getText());
        giftedStudent.setScreeningDate(screeningDatePicker.getValue());
        giftedStudent.setScreeningCompleted(screeningCompletedCheckBox.isSelected());
        giftedStudent.setIdentificationDate(identificationDatePicker.getValue());

        // Gifted Status & Areas
        giftedStudent.setGiftedStatus(giftedStatusComboBox.getValue());
        giftedStudent.setPrimaryGiftedArea(primaryGiftedAreaComboBox.getValue());
        giftedStudent.setGiftedAreas(parseGiftedAreasList(additionalGiftedAreasField.getText()));

        // Eligibility & Assessment Scores
        giftedStudent.setEligibilityDeterminationDate(eligibilityDeterminationDatePicker.getValue());
        giftedStudent.setEligibleForServices(eligibleForServicesCheckBox.isSelected());
        giftedStudent.setIqScore(parseInteger(iqScoreField.getText()));
        giftedStudent.setAchievementPercentile(parseInteger(achievementPercentileField.getText()));
        giftedStudent.setCompositeScore(parseDouble(compositeScoreField.getText()));
        giftedStudent.setEligibilityCriteriaMet(eligibilityCriteriaMetArea.getText());

        // Program Placement
        giftedStudent.setProgramType(programTypeComboBox.getValue());
        giftedStudent.setProgramEntryDate(programEntryDatePicker.getValue());
        giftedStudent.setProgramExitDate(programExitDatePicker.getValue());
        giftedStudent.setProgramPlacements(parseCommaSeparatedList(programPlacementsField.getText()));

        // Service Delivery
        giftedStudent.setServiceDeliveryModel(serviceDeliveryModelComboBox.getValue());
        giftedStudent.setServiceMinutesPerWeek(parseInteger(serviceMinutesPerWeekField.getText()));
        giftedStudent.setServiceFrequency(serviceFrequencyField.getText());
        giftedStudent.setServiceProvider(serviceProviderComboBox.getValue());

        // Advanced Coursework & Acceleration
        giftedStudent.setApCoursesEnrolled(parseInteger(apCoursesEnrolledField.getText()));
        giftedStudent.setHonorsCoursesEnrolled(parseInteger(honorsCoursesEnrolledField.getText()));
        giftedStudent.setAdvancedCourses(parseCommaSeparatedList(advancedCoursesField.getText()));
        giftedStudent.setDualEnrollment(dualEnrollmentCheckBox.isSelected());
        giftedStudent.setGradeAcceleration(gradeAccelerationCheckBox.isSelected());
        giftedStudent.setSubjectAcceleration(subjectAccelerationField.getText());

        // Enrichment & Cluster Grouping
        giftedStudent.setEnrichmentPrograms(parseCommaSeparatedList(enrichmentProgramsField.getText()));
        giftedStudent.setCompetitionParticipation(competitionParticipationArea.getText());
        giftedStudent.setSpecialProjects(specialProjectsArea.getText());
        giftedStudent.setClusterGrouped(clusterGroupedCheckBox.isSelected());
        giftedStudent.setClusterGroupName(clusterGroupNameField.getText());
        giftedStudent.setClusterTeacher(clusterTeacherField.getText());

        // Talent Development & Mentorship
        giftedStudent.setTalentArea(talentAreaField.getText());
        giftedStudent.setTalentDevelopmentPlanActive(talentDevelopmentPlanActiveCheckBox.isSelected());
        giftedStudent.setTalentDevelopmentGoals(talentDevelopmentGoalsArea.getText());
        giftedStudent.setMentorshipProgram(mentorshipProgramCheckBox.isSelected());
        giftedStudent.setMentorName(mentorNameField.getText());

        // Progress Monitoring & Review
        giftedStudent.setLastProgressReviewDate(lastProgressReviewDatePicker.getValue());
        giftedStudent.setNextProgressReviewDate(nextProgressReviewDatePicker.getValue());
        giftedStudent.setAnnualReviewRequired(annualReviewRequiredCheckBox.isSelected());
        giftedStudent.setNextAnnualReviewDate(nextAnnualReviewDatePicker.getValue());
        giftedStudent.setPerformanceLevel(performanceLevelField.getText());
        giftedStudent.setMeetingExpectations(meetingExpectationsCheckBox.isSelected());
        giftedStudent.setConcerns(concernsArea.getText());

        // Parent Communication
        giftedStudent.setParentNotificationSent(parentNotificationSentCheckBox.isSelected());
        giftedStudent.setParentNotificationDate(parentNotificationDatePicker.getValue());
        giftedStudent.setParentConsentReceived(parentConsentReceivedCheckBox.isSelected());
        giftedStudent.setParentConsentDate(parentConsentDatePicker.getValue());

        // Student Characteristics
        giftedStudent.setLearningCharacteristics(learningCharacteristicsArea.getText());
        giftedStudent.setSocialEmotionalNeeds(socialEmotionalNeedsArea.getText());
        giftedStudent.setStrengths(strengthsArea.getText());
        giftedStudent.setInterests(interestsArea.getText());
        giftedStudent.setNotes(notesArea.getText());

        return giftedStudent;
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

    private List<String> parseCommaSeparatedList(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private List<GiftedStudent.GiftedArea> parseGiftedAreasList(String text) {
        List<String> areaNames = parseCommaSeparatedList(text);
        List<GiftedStudent.GiftedArea> areas = new ArrayList<>();

        for (String areaName : areaNames) {
            for (GiftedStudent.GiftedArea area : GiftedStudent.GiftedArea.values()) {
                if (area.getDisplayName().equalsIgnoreCase(areaName.trim())) {
                    areas.add(area);
                    break;
                }
            }
        }

        return areas;
    }

    private void clearForm() {
        currentGiftedStudent = null;
        viewMode = false;

        studentComboBox.setValue(null);
        gradeField.clear();
        studentIdField.clear();
        currentGpaField.clear();

        referralDatePicker.setValue(null);
        referralTypeComboBox.setValue(null);
        referralSourceField.clear();
        screeningDatePicker.setValue(null);
        screeningCompletedCheckBox.setSelected(false);
        identificationDatePicker.setValue(null);

        giftedStatusComboBox.setValue(GiftedStudent.GiftedStatus.REFERRED);
        primaryGiftedAreaComboBox.setValue(null);
        additionalGiftedAreasField.clear();

        eligibilityDeterminationDatePicker.setValue(null);
        eligibleForServicesCheckBox.setSelected(false);
        iqScoreField.clear();
        achievementPercentileField.clear();
        compositeScoreField.clear();
        eligibilityCriteriaMetArea.clear();

        programTypeComboBox.setValue(null);
        programEntryDatePicker.setValue(null);
        programExitDatePicker.setValue(null);
        programPlacementsField.clear();

        serviceDeliveryModelComboBox.setValue(null);
        serviceMinutesPerWeekField.clear();
        serviceFrequencyField.clear();
        serviceProviderComboBox.setValue(null);

        apCoursesEnrolledField.clear();
        honorsCoursesEnrolledField.clear();
        advancedCoursesField.clear();
        dualEnrollmentCheckBox.setSelected(false);
        gradeAccelerationCheckBox.setSelected(false);
        subjectAccelerationField.clear();

        enrichmentProgramsField.clear();
        competitionParticipationArea.clear();
        specialProjectsArea.clear();
        clusterGroupedCheckBox.setSelected(false);
        clusterGroupNameField.clear();
        clusterTeacherField.clear();

        talentAreaField.clear();
        talentDevelopmentPlanActiveCheckBox.setSelected(false);
        talentDevelopmentGoalsArea.clear();
        mentorshipProgramCheckBox.setSelected(false);
        mentorNameField.clear();

        lastProgressReviewDatePicker.setValue(null);
        nextProgressReviewDatePicker.setValue(null);
        annualReviewRequiredCheckBox.setSelected(true);
        nextAnnualReviewDatePicker.setValue(null);
        performanceLevelField.clear();
        meetingExpectationsCheckBox.setSelected(true);
        concernsArea.clear();

        parentNotificationSentCheckBox.setSelected(false);
        parentNotificationDatePicker.setValue(null);
        parentConsentReceivedCheckBox.setSelected(false);
        parentConsentDatePicker.setValue(null);

        learningCharacteristicsArea.clear();
        socialEmotionalNeedsArea.clear();
        strengthsArea.clear();
        interestsArea.clear();
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

    public void setStudent(Student student) {
        studentComboBox.setValue(student);
        populateStudentInformation(student);
    }

    public void loadGiftedStudent(GiftedStudent giftedStudent) {
        this.currentGiftedStudent = giftedStudent;

        studentComboBox.setValue(giftedStudent.getStudent());
        if (giftedStudent.getStudent() != null) {
            studentIdField.setText(giftedStudent.getStudent().getStudentId());
            gradeField.setText(giftedStudent.getStudent().getGradeLevel() != null ?
                    giftedStudent.getStudent().getGradeLevel().toString() : "");
        }
        currentGpaField.setText(giftedStudent.getCurrentGpa() != null ?
                giftedStudent.getCurrentGpa().toString() : "");

        referralDatePicker.setValue(giftedStudent.getReferralDate());
        referralTypeComboBox.setValue(giftedStudent.getReferralType());
        referralSourceField.setText(giftedStudent.getReferralSource());
        screeningDatePicker.setValue(giftedStudent.getScreeningDate());
        screeningCompletedCheckBox.setSelected(giftedStudent.getScreeningCompleted() != null &&
                giftedStudent.getScreeningCompleted());
        identificationDatePicker.setValue(giftedStudent.getIdentificationDate());

        giftedStatusComboBox.setValue(giftedStudent.getGiftedStatus());
        primaryGiftedAreaComboBox.setValue(giftedStudent.getPrimaryGiftedArea());
        if (giftedStudent.getGiftedAreas() != null) {
            additionalGiftedAreasField.setText(giftedStudent.getGiftedAreas().stream()
                    .map(GiftedStudent.GiftedArea::getDisplayName)
                    .collect(Collectors.joining(", ")));
        }

        eligibilityDeterminationDatePicker.setValue(giftedStudent.getEligibilityDeterminationDate());
        eligibleForServicesCheckBox.setSelected(giftedStudent.getEligibleForServices() != null &&
                giftedStudent.getEligibleForServices());
        iqScoreField.setText(giftedStudent.getIqScore() != null ? giftedStudent.getIqScore().toString() : "");
        achievementPercentileField.setText(giftedStudent.getAchievementPercentile() != null ?
                giftedStudent.getAchievementPercentile().toString() : "");
        compositeScoreField.setText(giftedStudent.getCompositeScore() != null ?
                giftedStudent.getCompositeScore().toString() : "");
        eligibilityCriteriaMetArea.setText(giftedStudent.getEligibilityCriteriaMet());

        programTypeComboBox.setValue(giftedStudent.getProgramType());
        programEntryDatePicker.setValue(giftedStudent.getProgramEntryDate());
        programExitDatePicker.setValue(giftedStudent.getProgramExitDate());
        programPlacementsField.setText(String.join(", ", giftedStudent.getProgramPlacements()));

        serviceDeliveryModelComboBox.setValue(giftedStudent.getServiceDeliveryModel());
        serviceMinutesPerWeekField.setText(giftedStudent.getServiceMinutesPerWeek() != null ?
                giftedStudent.getServiceMinutesPerWeek().toString() : "");
        serviceFrequencyField.setText(giftedStudent.getServiceFrequency());
        serviceProviderComboBox.setValue(giftedStudent.getServiceProvider());

        apCoursesEnrolledField.setText(giftedStudent.getApCoursesEnrolled() != null ?
                giftedStudent.getApCoursesEnrolled().toString() : "");
        honorsCoursesEnrolledField.setText(giftedStudent.getHonorsCoursesEnrolled() != null ?
                giftedStudent.getHonorsCoursesEnrolled().toString() : "");
        advancedCoursesField.setText(String.join(", ", giftedStudent.getAdvancedCourses()));
        dualEnrollmentCheckBox.setSelected(giftedStudent.getDualEnrollment() != null &&
                giftedStudent.getDualEnrollment());
        gradeAccelerationCheckBox.setSelected(giftedStudent.getGradeAcceleration() != null &&
                giftedStudent.getGradeAcceleration());
        subjectAccelerationField.setText(giftedStudent.getSubjectAcceleration());

        enrichmentProgramsField.setText(String.join(", ", giftedStudent.getEnrichmentPrograms()));
        competitionParticipationArea.setText(giftedStudent.getCompetitionParticipation());
        specialProjectsArea.setText(giftedStudent.getSpecialProjects());
        clusterGroupedCheckBox.setSelected(giftedStudent.getClusterGrouped() != null &&
                giftedStudent.getClusterGrouped());
        clusterGroupNameField.setText(giftedStudent.getClusterGroupName());
        clusterTeacherField.setText(giftedStudent.getClusterTeacher());

        talentAreaField.setText(giftedStudent.getTalentArea());
        talentDevelopmentPlanActiveCheckBox.setSelected(giftedStudent.getTalentDevelopmentPlanActive() != null &&
                giftedStudent.getTalentDevelopmentPlanActive());
        talentDevelopmentGoalsArea.setText(giftedStudent.getTalentDevelopmentGoals());
        mentorshipProgramCheckBox.setSelected(giftedStudent.getMentorshipProgram() != null &&
                giftedStudent.getMentorshipProgram());
        mentorNameField.setText(giftedStudent.getMentorName());

        lastProgressReviewDatePicker.setValue(giftedStudent.getLastProgressReviewDate());
        nextProgressReviewDatePicker.setValue(giftedStudent.getNextProgressReviewDate());
        annualReviewRequiredCheckBox.setSelected(giftedStudent.getAnnualReviewRequired() != null &&
                giftedStudent.getAnnualReviewRequired());
        nextAnnualReviewDatePicker.setValue(giftedStudent.getNextAnnualReviewDate());
        performanceLevelField.setText(giftedStudent.getPerformanceLevel());
        meetingExpectationsCheckBox.setSelected(giftedStudent.getMeetingExpectations() != null &&
                giftedStudent.getMeetingExpectations());
        concernsArea.setText(giftedStudent.getConcerns());

        parentNotificationSentCheckBox.setSelected(giftedStudent.getParentNotificationSent() != null &&
                giftedStudent.getParentNotificationSent());
        parentNotificationDatePicker.setValue(giftedStudent.getParentNotificationDate());
        parentConsentReceivedCheckBox.setSelected(giftedStudent.getParentConsentReceived() != null &&
                giftedStudent.getParentConsentReceived());
        parentConsentDatePicker.setValue(giftedStudent.getParentConsentDate());

        learningCharacteristicsArea.setText(giftedStudent.getLearningCharacteristics());
        socialEmotionalNeedsArea.setText(giftedStudent.getSocialEmotionalNeeds());
        strengthsArea.setText(giftedStudent.getStrengths());
        interestsArea.setText(giftedStudent.getInterests());
        notesArea.setText(giftedStudent.getNotes());
    }

    public void setViewMode(boolean viewMode) {
        this.viewMode = viewMode;
        saveButton.setDisable(viewMode);
        // Additional field disabling logic could be added here if needed
    }
}
