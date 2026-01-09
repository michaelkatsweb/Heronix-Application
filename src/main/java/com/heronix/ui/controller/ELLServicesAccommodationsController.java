package com.heronix.ui.controller;

import com.heronix.model.domain.*;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ELLServicesAccommodationsController {

    @Autowired
    private ELLManagementService ellManagementService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    // Tab Pane
    @FXML private TabPane mainTabPane;

    // ==================== SERVICE DELIVERY TAB ====================

    // Student & Service Information
    @FXML private ComboBox<ELLStudent> serviceStudentComboBox;
    @FXML private TextField serviceGradeField;
    @FXML private DatePicker serviceDatePicker;
    @FXML private ComboBox<ELLService.ServiceType> serviceTypeComboBox;
    @FXML private ComboBox<ELLStudent.ServiceDeliveryModel> deliveryModelComboBox;
    @FXML private TextField durationMinutesField;
    @FXML private ComboBox<Teacher> serviceProviderComboBox;
    @FXML private ComboBox<ELLService.FocusArea> focusAreaComboBox;

    // Lesson Details
    @FXML private TextField lessonTopicField;
    @FXML private TextArea objectivesArea;
    @FXML private TextArea activitiesArea;
    @FXML private TextArea materialsUsedArea;

    // Attendance & Participation
    @FXML private ComboBox<ELLService.ServiceStatus> serviceStatusComboBox;
    @FXML private CheckBox studentAttendedCheckBox;
    @FXML private TextField absenceReasonField;
    @FXML private ComboBox<ELLService.ParticipationLevel> participationLevelComboBox;
    @FXML private TextField classSizeField;
    @FXML private TextField serviceLocationField;
    @FXML private TextField groupCompositionField;

    // Progress Notes
    @FXML private TextArea progressNotesArea;
    @FXML private TextArea skillsDemonstratedArea;
    @FXML private TextArea areasForImprovementArea;

    // Assessment & Standards
    @FXML private TextArea standardsAddressedArea;
    @FXML private CheckBox assessmentAdministeredCheckBox;
    @FXML private TextArea assessmentResultsArea;

    // Technology & Resources
    @FXML private CheckBox technologyUsedCheckBox;
    @FXML private TextField technologyDetailsField;

    // Follow-up & Homework
    @FXML private CheckBox homeworkAssignedCheckBox;
    @FXML private TextField homeworkDescriptionField;
    @FXML private CheckBox followUpNeededCheckBox;
    @FXML private TextArea followUpNotesArea;

    // Title III Compliance
    @FXML private CheckBox titleIIIFundedCheckBox;
    @FXML private CheckBox reportedForComplianceCheckBox;

    // ==================== ACCOMMODATIONS TAB ====================

    // Student & Accommodation Information
    @FXML private ComboBox<ELLStudent> accommodationStudentComboBox;
    @FXML private TextField accommodationGradeField;
    @FXML private ComboBox<ELLAccommodation.AccommodationCategory> accommodationCategoryComboBox;
    @FXML private CheckBox accommodationActiveCheckBox;
    @FXML private TextArea accommodationDescriptionArea;
    @FXML private TextArea implementationInstructionsArea;

    // Applicability
    @FXML private CheckBox appliesToAllClassesCheckBox;
    @FXML private TextField applicableSubjectsField;
    @FXML private TextField applicableClassesField;
    @FXML private TextField frequencyField;
    @FXML private TextField responsiblePartyField;

    // Testing Accommodations
    @FXML private CheckBox appliesToClassroomTestsCheckBox;
    @FXML private CheckBox appliesToStateTestsCheckBox;
    @FXML private CheckBox stateTestingApprovedCheckBox;
    @FXML private DatePicker stateTestingApprovalDatePicker;
    @FXML private TextArea testingNotesArea;

    // Implementation Timeline
    @FXML private DatePicker effectiveDatePicker;
    @FXML private DatePicker endDatePicker;

    // Teacher Training & Distribution
    @FXML private CheckBox requiresTeacherTrainingCheckBox;
    @FXML private CheckBox trainingCompletedCheckBox;
    @FXML private DatePicker trainingDatePicker;
    @FXML private CheckBox distributedToTeachersCheckBox;
    @FXML private DatePicker distributionDatePicker;
    @FXML private TextField teachersNotifiedField;

    // Implementation Monitoring
    @FXML private DatePicker lastMonitoredDatePicker;
    @FXML private TextField effectivenessRatingField;
    @FXML private TextField implementationFidelityField;
    @FXML private TextArea monitoringNotesArea;
    @FXML private TextArea concernsArea;

    // Action Buttons
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private ELLService currentService;
    private ELLAccommodation currentAccommodation;
    private boolean viewMode = false;

    @FXML
    public void initialize() {
        setupComboBoxes();
        setupListeners();
        loadInitialData();
    }

    private void setupComboBoxes() {
        // ELL Student ComboBoxes
        StringConverter<ELLStudent> studentConverter = new StringConverter<ELLStudent>() {
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
        };

        serviceStudentComboBox.setConverter(studentConverter);
        accommodationStudentComboBox.setConverter(studentConverter);

        // Service Type ComboBox
        serviceTypeComboBox.setItems(FXCollections.observableArrayList(
                ELLService.ServiceType.values()));
        serviceTypeComboBox.setConverter(new StringConverter<ELLService.ServiceType>() {
            @Override
            public String toString(ELLService.ServiceType type) {
                return type != null ? type.getDisplayName() : "";
            }

            @Override
            public ELLService.ServiceType fromString(String string) {
                return null;
            }
        });

        // Delivery Model ComboBox
        deliveryModelComboBox.setItems(FXCollections.observableArrayList(
                ELLStudent.ServiceDeliveryModel.values()));
        deliveryModelComboBox.setConverter(new StringConverter<ELLStudent.ServiceDeliveryModel>() {
            @Override
            public String toString(ELLStudent.ServiceDeliveryModel model) {
                return model != null ? model.getDisplayName() : "";
            }

            @Override
            public ELLStudent.ServiceDeliveryModel fromString(String string) {
                return null;
            }
        });

        // Focus Area ComboBox
        focusAreaComboBox.setItems(FXCollections.observableArrayList(
                ELLService.FocusArea.values()));
        focusAreaComboBox.setConverter(new StringConverter<ELLService.FocusArea>() {
            @Override
            public String toString(ELLService.FocusArea area) {
                return area != null ? area.getDisplayName() : "";
            }

            @Override
            public ELLService.FocusArea fromString(String string) {
                return null;
            }
        });

        // Service Status ComboBox
        serviceStatusComboBox.setItems(FXCollections.observableArrayList(
                ELLService.ServiceStatus.values()));
        serviceStatusComboBox.setConverter(new StringConverter<ELLService.ServiceStatus>() {
            @Override
            public String toString(ELLService.ServiceStatus status) {
                return status != null ? status.getDisplayName() : "";
            }

            @Override
            public ELLService.ServiceStatus fromString(String string) {
                return null;
            }
        });

        // Participation Level ComboBox
        participationLevelComboBox.setItems(FXCollections.observableArrayList(
                ELLService.ParticipationLevel.values()));
        participationLevelComboBox.setConverter(new StringConverter<ELLService.ParticipationLevel>() {
            @Override
            public String toString(ELLService.ParticipationLevel level) {
                return level != null ? level.getDisplayName() : "";
            }

            @Override
            public ELLService.ParticipationLevel fromString(String string) {
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

        // Accommodation Category ComboBox
        accommodationCategoryComboBox.setItems(FXCollections.observableArrayList(
                ELLAccommodation.AccommodationCategory.values()));
        accommodationCategoryComboBox.setConverter(new StringConverter<ELLAccommodation.AccommodationCategory>() {
            @Override
            public String toString(ELLAccommodation.AccommodationCategory category) {
                return category != null ? category.getDisplayName() : "";
            }

            @Override
            public ELLAccommodation.AccommodationCategory fromString(String string) {
                return null;
            }
        });
    }

    private void setupListeners() {
        // Service student selection
        serviceStudentComboBox.setOnAction(e -> {
            ELLStudent selected = serviceStudentComboBox.getValue();
            if (selected != null) {
                populateServiceStudentInfo(selected);
            }
        });

        // Accommodation student selection
        accommodationStudentComboBox.setOnAction(e -> {
            ELLStudent selected = accommodationStudentComboBox.getValue();
            if (selected != null) {
                populateAccommodationStudentInfo(selected);
            }
        });

        // Disable absence reason when student attended
        studentAttendedCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                absenceReasonField.clear();
                absenceReasonField.setDisable(true);
            } else {
                absenceReasonField.setDisable(false);
            }
        });

        // Disable technology details when technology not used
        technologyUsedCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            technologyDetailsField.setDisable(!newVal);
            if (!newVal) {
                technologyDetailsField.clear();
            }
        });

        // Disable homework description when homework not assigned
        homeworkAssignedCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            homeworkDescriptionField.setDisable(!newVal);
            if (!newVal) {
                homeworkDescriptionField.clear();
            }
        });

        // Disable follow-up notes when follow-up not needed
        followUpNeededCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            followUpNotesArea.setDisable(!newVal);
            if (!newVal) {
                followUpNotesArea.clear();
            }
        });

        // Disable applicable subjects/classes when applies to all classes
        appliesToAllClassesCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            applicableSubjectsField.setDisable(newVal);
            applicableClassesField.setDisable(newVal);
            if (newVal) {
                applicableSubjectsField.clear();
                applicableClassesField.clear();
            }
        });

        // Disable state approval fields when not applicable to state tests
        appliesToStateTestsCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            stateTestingApprovedCheckBox.setDisable(!newVal);
            stateTestingApprovalDatePicker.setDisable(!newVal);
            if (!newVal) {
                stateTestingApprovedCheckBox.setSelected(false);
                stateTestingApprovalDatePicker.setValue(null);
            }
        });

        // Disable training date when training not required
        requiresTeacherTrainingCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            trainingCompletedCheckBox.setDisable(!newVal);
            trainingDatePicker.setDisable(!newVal);
            if (!newVal) {
                trainingCompletedCheckBox.setSelected(false);
                trainingDatePicker.setValue(null);
            }
        });

        // Add numeric validation for ratings (1-5)
        addNumericValidation(effectivenessRatingField, 1, 5);
        addNumericValidation(implementationFidelityField, 1, 5);
    }

    private void loadInitialData() {
        // Load all active ELL students
        List<ELLStudent> ellStudents = ellManagementService.getAllActiveELL().stream()
                .filter(ELLStudent::isActiveELL)
                .sorted((s1, s2) -> {
                    String name1 = s1.getStudent().getLastName() + s1.getStudent().getFirstName();
                    String name2 = s2.getStudent().getLastName() + s2.getStudent().getFirstName();
                    return name1.compareToIgnoreCase(name2);
                })
                .collect(Collectors.toList());

        serviceStudentComboBox.setItems(FXCollections.observableArrayList(ellStudents));
        accommodationStudentComboBox.setItems(FXCollections.observableArrayList(ellStudents));

        // Load ESL/ELL teachers
        List<Teacher> teachers = teacherService.findAll().stream()
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
        serviceProviderComboBox.setItems(FXCollections.observableArrayList(teachers));

        // Set default values
        serviceDatePicker.setValue(LocalDate.now());
        serviceStatusComboBox.setValue(ELLService.ServiceStatus.SCHEDULED);
        accommodationActiveCheckBox.setSelected(true);
        appliesToAllClassesCheckBox.setSelected(true);
        appliesToClassroomTestsCheckBox.setSelected(true);
        effectiveDatePicker.setValue(LocalDate.now());

        // Disable conditional fields initially
        technologyDetailsField.setDisable(true);
        homeworkDescriptionField.setDisable(true);
        followUpNotesArea.setDisable(true);
        applicableSubjectsField.setDisable(true);
        applicableClassesField.setDisable(true);
        stateTestingApprovedCheckBox.setDisable(true);
        stateTestingApprovalDatePicker.setDisable(true);
        trainingCompletedCheckBox.setDisable(true);
        trainingDatePicker.setDisable(true);
    }

    private void populateServiceStudentInfo(ELLStudent ellStudent) {
        if (ellStudent.getStudent() != null) {
            serviceGradeField.setText(ellStudent.getStudent().getGradeLevel() != null ?
                    ellStudent.getStudent().getGradeLevel().toString() : "");
        }
    }

    private void populateAccommodationStudentInfo(ELLStudent ellStudent) {
        if (ellStudent.getStudent() != null) {
            accommodationGradeField.setText(ellStudent.getStudent().getGradeLevel() != null ?
                    ellStudent.getStudent().getGradeLevel().toString() : "");
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

        // Determine which tab is active
        int selectedIndex = mainTabPane.getSelectionModel().getSelectedIndex();

        if (selectedIndex == 0) {
            // Service Delivery tab
            saveService();
        } else {
            // Accommodations tab
            saveAccommodation();
        }
    }

    private void saveService() {
        List<String> errors = validateServiceForm();
        if (!errors.isEmpty()) {
            showValidationErrors(errors);
            return;
        }

        try {
            ELLService service = buildServiceFromForm();

            if (currentService == null) {
                ellManagementService.createService(service);
                showSuccessAlert("Service record created successfully!");
            } else {
                service.setId(currentService.getId());
                ellManagementService.updateService(service);
                showSuccessAlert("Service record updated successfully!");
            }

            clearServiceForm();
        } catch (Exception e) {
            showErrorAlert("Failed to save service record", e.getMessage());
        }
    }

    private void saveAccommodation() {
        List<String> errors = validateAccommodationForm();
        if (!errors.isEmpty()) {
            showValidationErrors(errors);
            return;
        }

        try {
            ELLAccommodation accommodation = buildAccommodationFromForm();

            if (currentAccommodation == null) {
                ellManagementService.createAccommodation(accommodation);
                showSuccessAlert("Accommodation created successfully!");
            } else {
                accommodation.setId(currentAccommodation.getId());
                ellManagementService.updateAccommodation(accommodation);
                showSuccessAlert("Accommodation updated successfully!");
            }

            clearAccommodationForm();
        } catch (Exception e) {
            showErrorAlert("Failed to save accommodation", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        int selectedIndex = mainTabPane.getSelectionModel().getSelectedIndex();
        if (selectedIndex == 0) {
            clearServiceForm();
        } else {
            clearAccommodationForm();
        }
    }

    private List<String> validateServiceForm() {
        List<String> errors = new ArrayList<>();

        if (serviceStudentComboBox.getValue() == null) {
            errors.add("ELL Student is required");
        }

        if (serviceDatePicker.getValue() == null) {
            errors.add("Service Date is required");
        }

        if (serviceTypeComboBox.getValue() == null) {
            errors.add("Service Type is required");
        }

        if (durationMinutesField.getText().trim().isEmpty()) {
            errors.add("Duration in minutes is required");
        } else {
            try {
                int minutes = Integer.parseInt(durationMinutesField.getText().trim());
                if (minutes <= 0) {
                    errors.add("Duration must be greater than 0");
                }
            } catch (NumberFormatException e) {
                errors.add("Duration must be a valid number");
            }
        }

        if (serviceStatusComboBox.getValue() == null) {
            errors.add("Service Status is required");
        }

        // Federal compliance for completed services
        if (serviceStatusComboBox.getValue() == ELLService.ServiceStatus.COMPLETED) {
            if (progressNotesArea.getText().trim().isEmpty()) {
                errors.add("COMPLIANCE: Progress notes are required for completed services");
            }
        }

        return errors;
    }

    private List<String> validateAccommodationForm() {
        List<String> errors = new ArrayList<>();

        if (accommodationStudentComboBox.getValue() == null) {
            errors.add("ELL Student is required");
        }

        if (accommodationCategoryComboBox.getValue() == null) {
            errors.add("Accommodation Category is required");
        }

        if (accommodationDescriptionArea.getText().trim().isEmpty()) {
            errors.add("Accommodation Description is required");
        }

        // Validate ratings if provided
        if (!effectivenessRatingField.getText().isEmpty()) {
            try {
                int rating = Integer.parseInt(effectivenessRatingField.getText());
                if (rating < 1 || rating > 5) {
                    errors.add("Effectiveness rating must be between 1 and 5");
                }
            } catch (NumberFormatException e) {
                errors.add("Effectiveness rating must be a valid number");
            }
        }

        if (!implementationFidelityField.getText().isEmpty()) {
            try {
                int rating = Integer.parseInt(implementationFidelityField.getText());
                if (rating < 1 || rating > 5) {
                    errors.add("Implementation fidelity rating must be between 1 and 5");
                }
            } catch (NumberFormatException e) {
                errors.add("Implementation fidelity rating must be a valid number");
            }
        }

        // State testing approval validation
        if (appliesToStateTestsCheckBox.isSelected() && !stateTestingApprovedCheckBox.isSelected()) {
            errors.add("WARNING: State testing accommodations typically require state approval");
        }

        return errors;
    }

    private ELLService buildServiceFromForm() {
        ELLService service = new ELLService();

        service.setEllStudent(serviceStudentComboBox.getValue());
        service.setServiceDate(serviceDatePicker.getValue());
        service.setServiceType(serviceTypeComboBox.getValue());
        service.setDeliveryModel(deliveryModelComboBox.getValue());
        service.setDurationMinutes(parseInteger(durationMinutesField.getText()));
        service.setServiceProvider(serviceProviderComboBox.getValue());
        service.setFocusArea(focusAreaComboBox.getValue());

        service.setLessonTopic(lessonTopicField.getText());
        service.setObjectives(objectivesArea.getText());
        service.setActivities(activitiesArea.getText());
        service.setMaterialsUsed(materialsUsedArea.getText());

        service.setStatus(serviceStatusComboBox.getValue());
        service.setStudentAttended(studentAttendedCheckBox.isSelected());
        service.setAbsenceReason(absenceReasonField.getText());
        service.setParticipationLevel(participationLevelComboBox.getValue());
        service.setClassSize(parseInteger(classSizeField.getText()));
        service.setLocation(serviceLocationField.getText());
        service.setGroupComposition(groupCompositionField.getText());

        service.setProgressNotes(progressNotesArea.getText());
        service.setSkillsDemonstrated(skillsDemonstratedArea.getText());
        service.setAreasForImprovement(areasForImprovementArea.getText());

        service.setStandardsAddressed(standardsAddressedArea.getText());
        service.setAssessmentAdministered(assessmentAdministeredCheckBox.isSelected());
        service.setAssessmentResults(assessmentResultsArea.getText());

        service.setTechnologyUsed(technologyUsedCheckBox.isSelected());
        service.setTechnologyDetails(technologyDetailsField.getText());

        service.setHomeworkAssigned(homeworkAssignedCheckBox.isSelected());
        service.setHomeworkDescription(homeworkDescriptionField.getText());
        service.setFollowUpNeeded(followUpNeededCheckBox.isSelected());
        service.setFollowUpNotes(followUpNotesArea.getText());

        service.setTitleIIIFunded(titleIIIFundedCheckBox.isSelected());
        service.setReportedForCompliance(reportedForComplianceCheckBox.isSelected());

        return service;
    }

    private ELLAccommodation buildAccommodationFromForm() {
        ELLAccommodation accommodation = new ELLAccommodation();

        accommodation.setEllStudent(accommodationStudentComboBox.getValue());
        accommodation.setCategory(accommodationCategoryComboBox.getValue());
        accommodation.setDescription(accommodationDescriptionArea.getText());
        accommodation.setImplementationInstructions(implementationInstructionsArea.getText());

        accommodation.setAppliesToAllClasses(appliesToAllClassesCheckBox.isSelected());
        accommodation.setApplicableSubjects(parseCommaSeparatedList(applicableSubjectsField.getText()));
        accommodation.setApplicableClasses(parseCommaSeparatedList(applicableClassesField.getText()));
        accommodation.setFrequency(frequencyField.getText());
        accommodation.setResponsibleParty(responsiblePartyField.getText());

        accommodation.setAppliesToClassroomTests(appliesToClassroomTestsCheckBox.isSelected());
        accommodation.setAppliesToStateTests(appliesToStateTestsCheckBox.isSelected());
        accommodation.setStateTestingApproved(stateTestingApprovedCheckBox.isSelected());
        accommodation.setStateTestingApprovalDate(stateTestingApprovalDatePicker.getValue());
        accommodation.setTestingNotes(testingNotesArea.getText());

        accommodation.setEffectiveDate(effectiveDatePicker.getValue());
        accommodation.setEndDate(endDatePicker.getValue());

        accommodation.setRequiresTeacherTraining(requiresTeacherTrainingCheckBox.isSelected());
        accommodation.setTrainingCompleted(trainingCompletedCheckBox.isSelected());
        accommodation.setTrainingDate(trainingDatePicker.getValue());
        accommodation.setDistributedToTeachers(distributedToTeachersCheckBox.isSelected());
        accommodation.setDistributionDate(distributionDatePicker.getValue());
        accommodation.setTeachersNotified(parseCommaSeparatedList(teachersNotifiedField.getText()));

        accommodation.setLastMonitoredDate(lastMonitoredDatePicker.getValue());
        accommodation.setEffectivenessRating(parseInteger(effectivenessRatingField.getText()));
        accommodation.setImplementationFidelityRating(parseInteger(implementationFidelityField.getText()));
        accommodation.setMonitoringNotes(monitoringNotesArea.getText());
        accommodation.setConcerns(concernsArea.getText());

        accommodation.setIsActive(accommodationActiveCheckBox.isSelected());

        return accommodation;
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

    private void clearServiceForm() {
        currentService = null;
        viewMode = false;

        serviceStudentComboBox.setValue(null);
        serviceGradeField.clear();
        serviceDatePicker.setValue(LocalDate.now());
        serviceTypeComboBox.setValue(null);
        deliveryModelComboBox.setValue(null);
        durationMinutesField.clear();
        serviceProviderComboBox.setValue(null);
        focusAreaComboBox.setValue(null);

        lessonTopicField.clear();
        objectivesArea.clear();
        activitiesArea.clear();
        materialsUsedArea.clear();

        serviceStatusComboBox.setValue(ELLService.ServiceStatus.SCHEDULED);
        studentAttendedCheckBox.setSelected(false);
        absenceReasonField.clear();
        participationLevelComboBox.setValue(null);
        classSizeField.clear();
        serviceLocationField.clear();
        groupCompositionField.clear();

        progressNotesArea.clear();
        skillsDemonstratedArea.clear();
        areasForImprovementArea.clear();

        standardsAddressedArea.clear();
        assessmentAdministeredCheckBox.setSelected(false);
        assessmentResultsArea.clear();

        technologyUsedCheckBox.setSelected(false);
        technologyDetailsField.clear();
        technologyDetailsField.setDisable(true);

        homeworkAssignedCheckBox.setSelected(false);
        homeworkDescriptionField.clear();
        homeworkDescriptionField.setDisable(true);
        followUpNeededCheckBox.setSelected(false);
        followUpNotesArea.clear();
        followUpNotesArea.setDisable(true);

        titleIIIFundedCheckBox.setSelected(false);
        reportedForComplianceCheckBox.setSelected(false);

        saveButton.setDisable(false);
    }

    private void clearAccommodationForm() {
        currentAccommodation = null;
        viewMode = false;

        accommodationStudentComboBox.setValue(null);
        accommodationGradeField.clear();
        accommodationCategoryComboBox.setValue(null);
        accommodationActiveCheckBox.setSelected(true);
        accommodationDescriptionArea.clear();
        implementationInstructionsArea.clear();

        appliesToAllClassesCheckBox.setSelected(true);
        applicableSubjectsField.clear();
        applicableSubjectsField.setDisable(true);
        applicableClassesField.clear();
        applicableClassesField.setDisable(true);
        frequencyField.clear();
        responsiblePartyField.clear();

        appliesToClassroomTestsCheckBox.setSelected(true);
        appliesToStateTestsCheckBox.setSelected(false);
        stateTestingApprovedCheckBox.setSelected(false);
        stateTestingApprovedCheckBox.setDisable(true);
        stateTestingApprovalDatePicker.setValue(null);
        stateTestingApprovalDatePicker.setDisable(true);
        testingNotesArea.clear();

        effectiveDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(null);

        requiresTeacherTrainingCheckBox.setSelected(false);
        trainingCompletedCheckBox.setSelected(false);
        trainingCompletedCheckBox.setDisable(true);
        trainingDatePicker.setValue(null);
        trainingDatePicker.setDisable(true);
        distributedToTeachersCheckBox.setSelected(false);
        distributionDatePicker.setValue(null);
        teachersNotifiedField.clear();

        lastMonitoredDatePicker.setValue(null);
        effectivenessRatingField.clear();
        implementationFidelityField.clear();
        monitoringNotesArea.clear();
        concernsArea.clear();

        saveButton.setDisable(false);
    }

    private void showValidationErrors(List<String> errors) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText("Please correct the following errors:");
        alert.setContentText(String.join("\n", errors));
        alert.showAndWait();
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Public methods for integration

    public void setELLStudentForService(ELLStudent ellStudent) {
        mainTabPane.getSelectionModel().select(0); // Select Service tab
        serviceStudentComboBox.setValue(ellStudent);
        populateServiceStudentInfo(ellStudent);
    }

    public void setELLStudentForAccommodation(ELLStudent ellStudent) {
        mainTabPane.getSelectionModel().select(1); // Select Accommodation tab
        accommodationStudentComboBox.setValue(ellStudent);
        populateAccommodationStudentInfo(ellStudent);
    }

    public void loadService(ELLService service) {
        this.currentService = service;
        mainTabPane.getSelectionModel().select(0); // Select Service tab

        serviceStudentComboBox.setValue(service.getEllStudent());
        if (service.getEllStudent() != null) {
            populateServiceStudentInfo(service.getEllStudent());
        }

        serviceDatePicker.setValue(service.getServiceDate());
        serviceTypeComboBox.setValue(service.getServiceType());
        deliveryModelComboBox.setValue(service.getDeliveryModel());
        durationMinutesField.setText(service.getDurationMinutes() != null ?
                service.getDurationMinutes().toString() : "");
        serviceProviderComboBox.setValue(service.getServiceProvider());
        focusAreaComboBox.setValue(service.getFocusArea());

        lessonTopicField.setText(service.getLessonTopic());
        objectivesArea.setText(service.getObjectives());
        activitiesArea.setText(service.getActivities());
        materialsUsedArea.setText(service.getMaterialsUsed());

        serviceStatusComboBox.setValue(service.getStatus());
        studentAttendedCheckBox.setSelected(service.getStudentAttended() != null && service.getStudentAttended());
        absenceReasonField.setText(service.getAbsenceReason());
        participationLevelComboBox.setValue(service.getParticipationLevel());
        classSizeField.setText(service.getClassSize() != null ? service.getClassSize().toString() : "");
        serviceLocationField.setText(service.getLocation());
        groupCompositionField.setText(service.getGroupComposition());

        progressNotesArea.setText(service.getProgressNotes());
        skillsDemonstratedArea.setText(service.getSkillsDemonstrated());
        areasForImprovementArea.setText(service.getAreasForImprovement());

        standardsAddressedArea.setText(service.getStandardsAddressed());
        assessmentAdministeredCheckBox.setSelected(service.getAssessmentAdministered() != null &&
                service.getAssessmentAdministered());
        assessmentResultsArea.setText(service.getAssessmentResults());

        technologyUsedCheckBox.setSelected(service.getTechnologyUsed() != null && service.getTechnologyUsed());
        technologyDetailsField.setText(service.getTechnologyDetails());

        homeworkAssignedCheckBox.setSelected(service.getHomeworkAssigned() != null && service.getHomeworkAssigned());
        homeworkDescriptionField.setText(service.getHomeworkDescription());
        followUpNeededCheckBox.setSelected(service.getFollowUpNeeded() != null && service.getFollowUpNeeded());
        followUpNotesArea.setText(service.getFollowUpNotes());

        titleIIIFundedCheckBox.setSelected(service.getTitleIIIFunded() != null && service.getTitleIIIFunded());
        reportedForComplianceCheckBox.setSelected(service.getReportedForCompliance() != null &&
                service.getReportedForCompliance());
    }

    public void loadAccommodation(ELLAccommodation accommodation) {
        this.currentAccommodation = accommodation;
        mainTabPane.getSelectionModel().select(1); // Select Accommodation tab

        accommodationStudentComboBox.setValue(accommodation.getEllStudent());
        if (accommodation.getEllStudent() != null) {
            populateAccommodationStudentInfo(accommodation.getEllStudent());
        }

        accommodationCategoryComboBox.setValue(accommodation.getCategory());
        accommodationActiveCheckBox.setSelected(accommodation.getIsActive() != null && accommodation.getIsActive());
        accommodationDescriptionArea.setText(accommodation.getDescription());
        implementationInstructionsArea.setText(accommodation.getImplementationInstructions());

        appliesToAllClassesCheckBox.setSelected(accommodation.getAppliesToAllClasses() != null &&
                accommodation.getAppliesToAllClasses());
        applicableSubjectsField.setText(String.join(", ", accommodation.getApplicableSubjects()));
        applicableClassesField.setText(String.join(", ", accommodation.getApplicableClasses()));
        frequencyField.setText(accommodation.getFrequency());
        responsiblePartyField.setText(accommodation.getResponsibleParty());

        appliesToClassroomTestsCheckBox.setSelected(accommodation.getAppliesToClassroomTests() != null &&
                accommodation.getAppliesToClassroomTests());
        appliesToStateTestsCheckBox.setSelected(accommodation.getAppliesToStateTests() != null &&
                accommodation.getAppliesToStateTests());
        stateTestingApprovedCheckBox.setSelected(accommodation.getStateTestingApproved() != null &&
                accommodation.getStateTestingApproved());
        stateTestingApprovalDatePicker.setValue(accommodation.getStateTestingApprovalDate());
        testingNotesArea.setText(accommodation.getTestingNotes());

        effectiveDatePicker.setValue(accommodation.getEffectiveDate());
        endDatePicker.setValue(accommodation.getEndDate());

        requiresTeacherTrainingCheckBox.setSelected(accommodation.getRequiresTeacherTraining() != null &&
                accommodation.getRequiresTeacherTraining());
        trainingCompletedCheckBox.setSelected(accommodation.getTrainingCompleted() != null &&
                accommodation.getTrainingCompleted());
        trainingDatePicker.setValue(accommodation.getTrainingDate());
        distributedToTeachersCheckBox.setSelected(accommodation.getDistributedToTeachers() != null &&
                accommodation.getDistributedToTeachers());
        distributionDatePicker.setValue(accommodation.getDistributionDate());
        teachersNotifiedField.setText(String.join(", ", accommodation.getTeachersNotified()));

        lastMonitoredDatePicker.setValue(accommodation.getLastMonitoredDate());
        effectivenessRatingField.setText(accommodation.getEffectivenessRating() != null ?
                accommodation.getEffectivenessRating().toString() : "");
        implementationFidelityField.setText(accommodation.getImplementationFidelityRating() != null ?
                accommodation.getImplementationFidelityRating().toString() : "");
        monitoringNotesArea.setText(accommodation.getMonitoringNotes());
        concernsArea.setText(accommodation.getConcerns());
    }

    public void setViewMode(boolean viewMode) {
        this.viewMode = viewMode;
        saveButton.setDisable(viewMode);
        // Additional field disabling logic could be added here if needed
    }
}
