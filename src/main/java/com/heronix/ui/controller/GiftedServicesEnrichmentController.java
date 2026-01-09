package com.heronix.ui.controller;

import com.heronix.model.domain.GiftedService;
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
public class GiftedServicesEnrichmentController {

    @Autowired
    private GiftedManagementService giftedManagementService;

    @Autowired
    private TeacherService teacherService;

    // Student & Service Information
    @FXML private ComboBox<GiftedStudent> giftedStudentComboBox;
    @FXML private TextField gradeField;
    @FXML private DatePicker serviceDatePicker;
    @FXML private ComboBox<GiftedService.ServiceType> serviceTypeComboBox;
    @FXML private ComboBox<GiftedStudent.ServiceDeliveryModel> deliveryModelComboBox;
    @FXML private TextField durationMinutesField;
    @FXML private ComboBox<Teacher> serviceProviderComboBox;
    @FXML private ComboBox<GiftedService.FocusArea> focusAreaComboBox;
    @FXML private TextField locationField;
    @FXML private ComboBox<GiftedService.ServiceStatus> serviceStatusComboBox;

    // Lesson Details
    @FXML private TextField lessonTopicField;
    @FXML private TextArea objectivesArea;
    @FXML private TextArea activitiesArea;

    // Differentiation & Depth/Complexity
    @FXML private TextArea differentiationUsedArea;
    @FXML private TextField depthComplexityLevelField;
    @FXML private TextField bloomLevelField;

    // Attendance & Engagement
    @FXML private CheckBox studentAttendedCheckBox;
    @FXML private ComboBox<GiftedService.EngagementLevel> engagementLevelComboBox;
    @FXML private TextField absenceReasonField;

    // Progress & Performance
    @FXML private TextArea progressNotesArea;
    @FXML private TextArea skillsDemonstratedArea;
    @FXML private TextField performanceLevelField;
    @FXML private TextField studentWorkSamplePathField;

    // Group Information
    @FXML private TextField groupSizeField;
    @FXML private CheckBox peerCollaborationCheckBox;
    @FXML private CheckBox crossGradeGroupingCheckBox;

    // Materials & Resources
    @FXML private TextArea materialsUsedArea;
    @FXML private TextArea technologyIntegrationArea;
    @FXML private TextArea enrichmentResourcesArea;

    // Standards & Assessment
    @FXML private TextArea standardsAddressedArea;
    @FXML private CheckBox assessmentAdministeredCheckBox;
    @FXML private TextArea assessmentResultsArea;

    // Extension Activities & Follow-up
    @FXML private CheckBox extensionActivityAssignedCheckBox;
    @FXML private TextField extensionActivityDescriptionField;
    @FXML private CheckBox followUpNeededCheckBox;
    @FXML private TextArea followUpNotesArea;

    // Action Buttons
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private GiftedService currentService;
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

        // Service Type ComboBox
        serviceTypeComboBox.setItems(FXCollections.observableArrayList(
                GiftedService.ServiceType.values()));
        serviceTypeComboBox.setConverter(new StringConverter<GiftedService.ServiceType>() {
            @Override
            public String toString(GiftedService.ServiceType type) {
                return type != null ? type.getDisplayName() : "";
            }

            @Override
            public GiftedService.ServiceType fromString(String string) {
                return null;
            }
        });

        // Delivery Model ComboBox
        deliveryModelComboBox.setItems(FXCollections.observableArrayList(
                GiftedStudent.ServiceDeliveryModel.values()));
        deliveryModelComboBox.setConverter(new StringConverter<GiftedStudent.ServiceDeliveryModel>() {
            @Override
            public String toString(GiftedStudent.ServiceDeliveryModel model) {
                return model != null ? model.getDisplayName() : "";
            }

            @Override
            public GiftedStudent.ServiceDeliveryModel fromString(String string) {
                return null;
            }
        });

        // Focus Area ComboBox
        focusAreaComboBox.setItems(FXCollections.observableArrayList(
                GiftedService.FocusArea.values()));
        focusAreaComboBox.setConverter(new StringConverter<GiftedService.FocusArea>() {
            @Override
            public String toString(GiftedService.FocusArea area) {
                return area != null ? area.getDisplayName() : "";
            }

            @Override
            public GiftedService.FocusArea fromString(String string) {
                return null;
            }
        });

        // Service Status ComboBox
        serviceStatusComboBox.setItems(FXCollections.observableArrayList(
                GiftedService.ServiceStatus.values()));
        serviceStatusComboBox.setConverter(new StringConverter<GiftedService.ServiceStatus>() {
            @Override
            public String toString(GiftedService.ServiceStatus status) {
                return status != null ? status.getDisplayName() : "";
            }

            @Override
            public GiftedService.ServiceStatus fromString(String string) {
                return null;
            }
        });

        // Engagement Level ComboBox
        engagementLevelComboBox.setItems(FXCollections.observableArrayList(
                GiftedService.EngagementLevel.values()));
        engagementLevelComboBox.setConverter(new StringConverter<GiftedService.EngagementLevel>() {
            @Override
            public String toString(GiftedService.EngagementLevel level) {
                return level != null ? level.getDisplayName() : "";
            }

            @Override
            public GiftedService.EngagementLevel fromString(String string) {
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
        // Auto-populate student grade when student is selected
        giftedStudentComboBox.setOnAction(e -> {
            GiftedStudent selected = giftedStudentComboBox.getValue();
            if (selected != null && selected.getStudent() != null) {
                gradeField.setText(selected.getStudent().getGradeLevel() != null ?
                        selected.getStudent().getGradeLevel().toString() : "");
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

        // Disable extension description when not assigned
        extensionActivityAssignedCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            extensionActivityDescriptionField.setDisable(!newVal);
            if (!newVal) {
                extensionActivityDescriptionField.clear();
            }
        });

        // Disable follow-up notes when not needed
        followUpNeededCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            followUpNotesArea.setDisable(!newVal);
            if (!newVal) {
                followUpNotesArea.clear();
            }
        });
    }

    private void loadInitialData() {
        // Load active gifted students
        List<GiftedStudent> giftedStudents = giftedManagementService.findAllGiftedStudents().stream()
                .filter(GiftedStudent::isActive)
                .sorted((s1, s2) -> {
                    String name1 = s1.getStudent().getLastName() + s1.getStudent().getFirstName();
                    String name2 = s2.getStudent().getLastName() + s2.getStudent().getFirstName();
                    return name1.compareToIgnoreCase(name2);
                })
                .collect(Collectors.toList());
        giftedStudentComboBox.setItems(FXCollections.observableArrayList(giftedStudents));

        // Load gifted program teachers
        List<Teacher> teachers = teacherService.findAllTeachers().stream()
                .filter(t -> {
                    List<String> certifications = t.getCertifications();
                    return certifications != null &&
                           (certifications.contains("Gifted") ||
                            certifications.contains("GT") ||
                            certifications.contains("Talented"));
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
        serviceStatusComboBox.setValue(GiftedService.ServiceStatus.SCHEDULED);

        // Disable conditional fields initially
        absenceReasonField.setDisable(false);
        extensionActivityDescriptionField.setDisable(true);
        followUpNotesArea.setDisable(true);
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
            GiftedService service = buildServiceFromForm();

            if (currentService == null) {
                giftedManagementService.createService(service);
                showSuccessAlert("Service record created successfully!");
            } else {
                service.setId(currentService.getId());
                giftedManagementService.updateService(service);
                showSuccessAlert("Service record updated successfully!");
            }

            clearForm();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to save service record");
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

        // Documentation requirement for completed services
        if (serviceStatusComboBox.getValue() == GiftedService.ServiceStatus.COMPLETED) {
            if (progressNotesArea.getText().trim().isEmpty()) {
                errors.add("Progress notes are required for completed services");
            }
        }

        return errors;
    }

    private GiftedService buildServiceFromForm() {
        GiftedService service = new GiftedService();

        // Student & Service Information
        service.setGiftedStudent(giftedStudentComboBox.getValue());
        service.setServiceDate(serviceDatePicker.getValue());
        service.setServiceType(serviceTypeComboBox.getValue());
        service.setDeliveryModel(deliveryModelComboBox.getValue());
        service.setDurationMinutes(parseInteger(durationMinutesField.getText()));
        service.setServiceProvider(serviceProviderComboBox.getValue());
        service.setFocusArea(focusAreaComboBox.getValue());
        service.setLocation(locationField.getText());
        service.setStatus(serviceStatusComboBox.getValue());

        // Lesson Details
        service.setLessonTopic(lessonTopicField.getText());
        service.setObjectives(objectivesArea.getText());
        service.setActivities(activitiesArea.getText());

        // Differentiation & Depth/Complexity
        service.setDifferentiationUsed(differentiationUsedArea.getText());
        service.setDepthComplexityLevel(depthComplexityLevelField.getText());
        service.setBloomLevel(bloomLevelField.getText());

        // Attendance & Engagement
        service.setStudentAttended(studentAttendedCheckBox.isSelected());
        service.setEngagementLevel(engagementLevelComboBox.getValue());
        service.setAbsenceReason(absenceReasonField.getText());

        // Progress & Performance
        service.setProgressNotes(progressNotesArea.getText());
        service.setSkillsDemonstrated(skillsDemonstratedArea.getText());
        service.setPerformanceLevel(performanceLevelField.getText());
        service.setStudentWorkSamplePath(studentWorkSamplePathField.getText());

        // Group Information
        service.setGroupSize(parseInteger(groupSizeField.getText()));
        service.setPeerCollaboration(peerCollaborationCheckBox.isSelected());
        service.setCrossGradeGrouping(crossGradeGroupingCheckBox.isSelected());

        // Materials & Resources
        service.setMaterialsUsed(materialsUsedArea.getText());
        service.setTechnologyIntegration(technologyIntegrationArea.getText());
        service.setEnrichmentResources(enrichmentResourcesArea.getText());

        // Standards & Assessment
        service.setStandardsAddressed(standardsAddressedArea.getText());
        service.setAssessmentAdministered(assessmentAdministeredCheckBox.isSelected());
        service.setAssessmentResults(assessmentResultsArea.getText());

        // Extension Activities & Follow-up
        service.setExtensionActivityAssigned(extensionActivityAssignedCheckBox.isSelected());
        service.setExtensionActivityDescription(extensionActivityDescriptionField.getText());
        service.setFollowUpNeeded(followUpNeededCheckBox.isSelected());
        service.setFollowUpNotes(followUpNotesArea.getText());

        return service;
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
        currentService = null;
        viewMode = false;

        giftedStudentComboBox.setValue(null);
        gradeField.clear();
        serviceDatePicker.setValue(LocalDate.now());
        serviceTypeComboBox.setValue(null);
        deliveryModelComboBox.setValue(null);
        durationMinutesField.clear();
        serviceProviderComboBox.setValue(null);
        focusAreaComboBox.setValue(null);
        locationField.clear();
        serviceStatusComboBox.setValue(GiftedService.ServiceStatus.SCHEDULED);

        lessonTopicField.clear();
        objectivesArea.clear();
        activitiesArea.clear();

        differentiationUsedArea.clear();
        depthComplexityLevelField.clear();
        bloomLevelField.clear();

        studentAttendedCheckBox.setSelected(false);
        engagementLevelComboBox.setValue(null);
        absenceReasonField.clear();

        progressNotesArea.clear();
        skillsDemonstratedArea.clear();
        performanceLevelField.clear();
        studentWorkSamplePathField.clear();

        groupSizeField.clear();
        peerCollaborationCheckBox.setSelected(false);
        crossGradeGroupingCheckBox.setSelected(false);

        materialsUsedArea.clear();
        technologyIntegrationArea.clear();
        enrichmentResourcesArea.clear();

        standardsAddressedArea.clear();
        assessmentAdministeredCheckBox.setSelected(false);
        assessmentResultsArea.clear();

        extensionActivityAssignedCheckBox.setSelected(false);
        extensionActivityDescriptionField.clear();
        extensionActivityDescriptionField.setDisable(true);
        followUpNeededCheckBox.setSelected(false);
        followUpNotesArea.clear();
        followUpNotesArea.setDisable(true);

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

    public void loadService(GiftedService service) {
        this.currentService = service;

        giftedStudentComboBox.setValue(service.getGiftedStudent());
        if (service.getGiftedStudent() != null && service.getGiftedStudent().getStudent() != null) {
            gradeField.setText(service.getGiftedStudent().getStudent().getGradeLevel() != null ?
                    service.getGiftedStudent().getStudent().getGradeLevel().toString() : "");
        }

        serviceDatePicker.setValue(service.getServiceDate());
        serviceTypeComboBox.setValue(service.getServiceType());
        deliveryModelComboBox.setValue(service.getDeliveryModel());
        durationMinutesField.setText(service.getDurationMinutes() != null ?
                service.getDurationMinutes().toString() : "");
        serviceProviderComboBox.setValue(service.getServiceProvider());
        focusAreaComboBox.setValue(service.getFocusArea());
        locationField.setText(service.getLocation());
        serviceStatusComboBox.setValue(service.getStatus());

        lessonTopicField.setText(service.getLessonTopic());
        objectivesArea.setText(service.getObjectives());
        activitiesArea.setText(service.getActivities());

        differentiationUsedArea.setText(service.getDifferentiationUsed());
        depthComplexityLevelField.setText(service.getDepthComplexityLevel());
        bloomLevelField.setText(service.getBloomLevel());

        studentAttendedCheckBox.setSelected(service.getStudentAttended() != null &&
                service.getStudentAttended());
        engagementLevelComboBox.setValue(service.getEngagementLevel());
        absenceReasonField.setText(service.getAbsenceReason());

        progressNotesArea.setText(service.getProgressNotes());
        skillsDemonstratedArea.setText(service.getSkillsDemonstrated());
        performanceLevelField.setText(service.getPerformanceLevel());
        studentWorkSamplePathField.setText(service.getStudentWorkSamplePath());

        groupSizeField.setText(service.getGroupSize() != null ?
                service.getGroupSize().toString() : "");
        peerCollaborationCheckBox.setSelected(service.getPeerCollaboration() != null &&
                service.getPeerCollaboration());
        crossGradeGroupingCheckBox.setSelected(service.getCrossGradeGrouping() != null &&
                service.getCrossGradeGrouping());

        materialsUsedArea.setText(service.getMaterialsUsed());
        technologyIntegrationArea.setText(service.getTechnologyIntegration());
        enrichmentResourcesArea.setText(service.getEnrichmentResources());

        standardsAddressedArea.setText(service.getStandardsAddressed());
        assessmentAdministeredCheckBox.setSelected(service.getAssessmentAdministered() != null &&
                service.getAssessmentAdministered());
        assessmentResultsArea.setText(service.getAssessmentResults());

        extensionActivityAssignedCheckBox.setSelected(service.getExtensionActivityAssigned() != null &&
                service.getExtensionActivityAssigned());
        extensionActivityDescriptionField.setText(service.getExtensionActivityDescription());
        followUpNeededCheckBox.setSelected(service.getFollowUpNeeded() != null &&
                service.getFollowUpNeeded());
        followUpNotesArea.setText(service.getFollowUpNotes());
    }

    public void setViewMode(boolean viewMode) {
        this.viewMode = viewMode;
        saveButton.setDisable(viewMode);
    }
}
