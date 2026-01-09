package com.heronix.ui.controller;

import com.heronix.model.domain.CrisisIntervention;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.Teacher;
import com.heronix.service.CrisisInterventionService;
import com.heronix.service.StudentService;
import com.heronix.service.TeacherService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Crisis Intervention Report Form
 * Handles documentation of immediate crisis response, safety protocols, and emergency interventions
 * CRITICAL: This form documents life-threatening situations and requires complete, accurate documentation
 */
@Component
public class CrisisInterventionController {

    private static final Logger logger = LoggerFactory.getLogger(CrisisInterventionController.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    private CrisisInterventionService crisisInterventionService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    // Crisis Information
    @FXML private ComboBox<Student> studentComboBox;
    @FXML private TextField gradeField;
    @FXML private DatePicker crisisDatePicker;
    @FXML private TextField crisisTimeField;
    @FXML private ComboBox<String> locationComboBox;
    @FXML private TextField locationDetailsField;
    @FXML private ComboBox<Teacher> counselorComboBox;

    // Crisis Type & Severity
    @FXML private ComboBox<String> crisisTypeComboBox;
    @FXML private ComboBox<String> severityComboBox;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TextArea presentingCrisisTextArea;

    // Risk Assessment
    @FXML private CheckBox immediateDangerCheckBox;
    @FXML private CheckBox dangerToOthersCheckBox;
    @FXML private CheckBox suicidePlanCheckBox;
    @FXML private CheckBox suicideMeansCheckBox;
    @FXML private CheckBox previousAttemptCheckBox;
    @FXML private CheckBox substanceInvolvementCheckBox;
    @FXML private CheckBox mentalHealthHistoryCheckBox;
    @FXML private CheckBox recentLossCheckBox;
    @FXML private TextArea riskAssessmentTextArea;

    // Crisis Intervention Actions
    @FXML private CheckBox emergencyServicesCheckBox;
    @FXML private CheckBox hospitalTransportCheckBox;
    @FXML private CheckBox parentNotifiedCheckBox;
    @FXML private CheckBox adminNotifiedCheckBox;
    @FXML private CheckBox policeInvolvedCheckBox;
    @FXML private CheckBox crisisTeamActivatedCheckBox;
    @FXML private CheckBox safetyPlanCreatedCheckBox;
    @FXML private CheckBox weaponsRemovedCheckBox;
    @FXML private CheckBox oneToOneSupervisionCheckBox;
    @FXML private CheckBox deEscalationCheckBox;
    @FXML private CheckBox isolationCheckBox;
    @FXML private CheckBox restraintCheckBox;
    @FXML private TextArea interventionActionsTextArea;

    // Notifications & Communication
    @FXML private TextField parentContactTimeField;
    @FXML private ComboBox<String> parentContactMethodComboBox;
    @FXML private TextField parentContactedField;
    @FXML private TextArea parentResponseTextArea;
    @FXML private TextArea personnelInvolvedTextArea;

    // Outcome & Follow-Up
    @FXML private ComboBox<String> outcomeComboBox;
    @FXML private TextField resolutionTimeField;
    @FXML private TextArea outcomeDetailsTextArea;
    @FXML private TextArea followUpPlanTextArea;
    @FXML private DatePicker followUpDatePicker;
    @FXML private ComboBox<Teacher> caseManagerComboBox;

    // Documentation & Reports
    @FXML private CheckBox incidentReportCheckBox;
    @FXML private CheckBox mandatedReportCheckBox;
    @FXML private CheckBox districtNotifiedCheckBox;
    @FXML private CheckBox safetyPlanDocumentedCheckBox;
    @FXML private CheckBox parentReleaseCheckBox;
    @FXML private CheckBox photographsCheckBox;
    @FXML private TextArea additionalNotesTextArea;

    // Buttons
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label crisisIndicator;

    private CrisisIntervention currentCrisis;
    private Student preSelectedStudent;

    @FXML
    public void initialize() {
        logger.info("Initializing CrisisInterventionController");

        setupComboBoxConverters();
        loadFormData();
        setDefaultValues();
    }

    private void setupComboBoxConverters() {
        // Student ComboBox converter
        studentComboBox.setConverter(new javafx.util.StringConverter<Student>() {
            @Override
            public String toString(Student student) {
                return student == null ? "" :
                        student.getFirstName() + " " + student.getLastName() +
                                " (Grade " + student.getGradeLevel() + ")";
            }

            @Override
            public Student fromString(String string) {
                return null;
            }
        });

        // Auto-populate grade when student selected
        studentComboBox.setOnAction(e -> {
            Student student = studentComboBox.getValue();
            if (student != null) {
                gradeField.setText(String.valueOf(student.getGradeLevel()));
            }
        });

        // Counselor ComboBox converter
        counselorComboBox.setConverter(new javafx.util.StringConverter<Teacher>() {
            @Override
            public String toString(Teacher teacher) {
                return teacher == null ? "" :
                        teacher.getFirstName() + " " + teacher.getLastName();
            }

            @Override
            public Teacher fromString(String string) {
                return null;
            }
        });

        // Case Manager ComboBox converter
        caseManagerComboBox.setConverter(new javafx.util.StringConverter<Teacher>() {
            @Override
            public String toString(Teacher teacher) {
                return teacher == null ? "" :
                        teacher.getFirstName() + " " + teacher.getLastName();
            }

            @Override
            public Teacher fromString(String string) {
                return null;
            }
        });
    }

    private void loadFormData() {
        try {
            // Load students
            List<Student> students = studentService.getAllStudents();
            studentComboBox.setItems(FXCollections.observableArrayList(students));

            // Load counselors and case managers
            List<Teacher> staff = teacherService.findAll().stream()
                    .filter(t -> t.getRole() != null &&
                            (t.getRole().toString().contains("COUNSELOR") ||
                                    t.getRole().toString().contains("SOCIAL_WORKER") ||
                                    t.getRole().toString().contains("PSYCHOLOGIST") ||
                                    t.getRole().toString().contains("ADMIN")))
                    .toList();
            counselorComboBox.setItems(FXCollections.observableArrayList(staff));
            caseManagerComboBox.setItems(FXCollections.observableArrayList(staff));

        } catch (Exception e) {
            logger.error("Error loading form data", e);
            showError("Failed to load form data: " + e.getMessage());
        }
    }

    private void setDefaultValues() {
        crisisDatePicker.setValue(LocalDate.now());
        crisisTimeField.setText(LocalTime.now().format(TIME_FORMATTER));
        statusComboBox.setValue("IN_PROGRESS");

        // If pre-selected student, set them
        if (preSelectedStudent != null) {
            studentComboBox.setValue(preSelectedStudent);
        }
    }

    @FXML
    private void handleSave() {
        if (!confirmCriticalSave()) {
            return;
        }

        if (validateForm()) {
            try {
                CrisisIntervention crisis = buildCrisisFromForm();

                CrisisIntervention saved = crisisInterventionService.saveCrisisIntervention(crisis);

                // Show critical success message
                showCriticalSuccess();
                closeWindow();

            } catch (Exception e) {
                logger.error("Error saving crisis intervention", e);
                showError("Failed to save crisis intervention: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancel() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cancel Crisis Report");
        alert.setHeaderText("WARNING: Crisis documentation incomplete");
        alert.setContentText("Are you sure you want to cancel without saving?\n\n" +
                "Crisis interventions MUST be documented for legal and safety reasons.");
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            closeWindow();
        }
    }

    private boolean confirmCriticalSave() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Save Crisis Intervention Report");
        alert.setHeaderText("CRITICAL DOCUMENTATION CONFIRMATION");
        alert.setContentText(
                "This crisis intervention report documents a serious safety situation.\n\n" +
                        "Have you:\n" +
                        "• Documented all safety interventions taken?\n" +
                        "• Notified all required personnel and parents?\n" +
                        "• Ensured student safety is addressed?\n" +
                        "• Completed all required follow-up actions?\n" +
                        "• Verified accuracy of all information?\n\n" +
                        "This report may be used for legal purposes.\n\n" +
                        "Proceed with saving?");

        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    private boolean validateForm() {
        List<String> errors = new ArrayList<>();

        if (studentComboBox.getValue() == null) {
            errors.add("Student is required");
        }
        if (crisisDatePicker.getValue() == null) {
            errors.add("Crisis date is required");
        }
        if (crisisTimeField.getText() == null || crisisTimeField.getText().trim().isEmpty()) {
            errors.add("Crisis time is required");
        } else {
            try {
                LocalTime.parse(crisisTimeField.getText().trim(), TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                errors.add("Crisis time must be in HH:MM format");
            }
        }
        if (locationComboBox.getValue() == null || locationComboBox.getValue().isEmpty()) {
            errors.add("Location is required");
        }
        if (counselorComboBox.getValue() == null) {
            errors.add("Responding counselor is required");
        }
        if (crisisTypeComboBox.getValue() == null || crisisTypeComboBox.getValue().isEmpty()) {
            errors.add("Crisis type is required");
        }
        if (severityComboBox.getValue() == null || severityComboBox.getValue().isEmpty()) {
            errors.add("Severity level is required");
        }
        if (statusComboBox.getValue() == null || statusComboBox.getValue().isEmpty()) {
            errors.add("Response status is required");
        }
        if (presentingCrisisTextArea.getText() == null || presentingCrisisTextArea.getText().trim().isEmpty()) {
            errors.add("Presenting crisis description is required");
        }
        if (riskAssessmentTextArea.getText() == null || riskAssessmentTextArea.getText().trim().isEmpty()) {
            errors.add("Risk assessment details are required");
        }
        if (interventionActionsTextArea.getText() == null || interventionActionsTextArea.getText().trim().isEmpty()) {
            errors.add("Intervention actions documentation is required");
        }
        if (outcomeComboBox.getValue() == null || outcomeComboBox.getValue().isEmpty()) {
            errors.add("Immediate outcome is required");
        }
        if (followUpPlanTextArea.getText() == null || followUpPlanTextArea.getText().trim().isEmpty()) {
            errors.add("Follow-up plan is required");
        }

        // Critical safety checks
        if (immediateDangerCheckBox.isSelected() && !emergencyServicesCheckBox.isSelected() &&
                !hospitalTransportCheckBox.isSelected() && !oneToOneSupervisionCheckBox.isSelected()) {
            errors.add("CRITICAL: Imminent danger reported but no emergency response documented");
        }

        if (suicidePlanCheckBox.isSelected() && !safetyPlanCreatedCheckBox.isSelected()) {
            errors.add("CRITICAL: Suicide plan reported but no safety plan created");
        }

        if (weaponsRemovedCheckBox.isSelected() && !policeInvolvedCheckBox.isSelected()) {
            errors.add("WARNING: Weapons removed should typically involve law enforcement");
        }

        if (!errors.isEmpty()) {
            showValidationErrors(errors);
            return false;
        }

        return true;
    }

    private CrisisIntervention buildCrisisFromForm() {
        CrisisIntervention crisis = currentCrisis != null ? currentCrisis : new CrisisIntervention();

        // Crisis information
        crisis.setStudent(studentComboBox.getValue());
        crisis.setCrisisDate(crisisDatePicker.getValue());
        if (crisisTimeField.getText() != null && !crisisTimeField.getText().trim().isEmpty()) {
            try {
                crisis.setCrisisTime(LocalTime.parse(crisisTimeField.getText().trim(), TIME_FORMATTER));
            } catch (DateTimeParseException e) {
                logger.warn("Could not parse crisis time", e);
            }
        }
        if (locationComboBox.getValue() != null) {
            crisis.setLocation(CrisisIntervention.CrisisLocation.valueOf(locationComboBox.getValue()));
        }
        crisis.setLocationDetails(locationDetailsField.getText());
        crisis.setRespondingCounselor(counselorComboBox.getValue());

        // Crisis type & severity
        if (crisisTypeComboBox.getValue() != null) {
            crisis.setCrisisType(CrisisIntervention.CrisisType.valueOf(crisisTypeComboBox.getValue()));
        }
        if (severityComboBox.getValue() != null) {
            crisis.setSeverityLevel(CrisisIntervention.SeverityLevel.valueOf(severityComboBox.getValue()));
        }
        if (statusComboBox.getValue() != null) {
            crisis.setResponseStatus(CrisisIntervention.ResponseStatus.valueOf(statusComboBox.getValue()));
        }
        crisis.setPresentingCrisis(presentingCrisisTextArea.getText());

        // Risk assessment
        crisis.setImmediateDangerToSelf(immediateDangerCheckBox.isSelected());
        crisis.setImmediateDangerToOthers(dangerToOthersCheckBox.isSelected());
        crisis.setHasSuicidePlan(suicidePlanCheckBox.isSelected());
        crisis.setHasAccessToMeans(suicideMeansCheckBox.isSelected());
        crisis.setPreviousAttempt(previousAttemptCheckBox.isSelected());
        crisis.setSubstanceInvolvement(substanceInvolvementCheckBox.isSelected());
        crisis.setMentalHealthHistory(mentalHealthHistoryCheckBox.isSelected());
        crisis.setRecentLossTrauma(recentLossCheckBox.isSelected());
        crisis.setRiskAssessmentDetails(riskAssessmentTextArea.getText());

        // Crisis intervention actions
        crisis.setEmergencyServicesCalled(emergencyServicesCheckBox.isSelected());
        crisis.setHospitalTransport(hospitalTransportCheckBox.isSelected());
        crisis.setParentNotified(parentNotifiedCheckBox.isSelected());
        crisis.setAdminNotified(adminNotifiedCheckBox.isSelected());
        crisis.setPoliceInvolved(policeInvolvedCheckBox.isSelected());
        crisis.setCrisisTeamActivated(crisisTeamActivatedCheckBox.isSelected());
        crisis.setSafetyPlanCreated(safetyPlanCreatedCheckBox.isSelected());
        crisis.setWeaponsMeansRemoved(weaponsRemovedCheckBox.isSelected());
        crisis.setOneToOneSupervision(oneToOneSupervisionCheckBox.isSelected());
        crisis.setDeEscalationUsed(deEscalationCheckBox.isSelected());
        crisis.setStudentIsolated(isolationCheckBox.isSelected());
        crisis.setPhysicalRestraintUsed(restraintCheckBox.isSelected());
        crisis.setInterventionActions(interventionActionsTextArea.getText());

        // Notifications & communication
        if (parentContactTimeField.getText() != null && !parentContactTimeField.getText().trim().isEmpty()) {
            try {
                crisis.setParentContactTime(LocalTime.parse(parentContactTimeField.getText().trim(), TIME_FORMATTER));
            } catch (DateTimeParseException e) {
                logger.warn("Could not parse parent contact time", e);
            }
        }
        if (parentContactMethodComboBox.getValue() != null) {
            crisis.setParentContactMethod(CrisisIntervention.ContactMethod.valueOf(parentContactMethodComboBox.getValue()));
        }
        crisis.setParentContacted(parentContactedField.getText());
        crisis.setParentResponse(parentResponseTextArea.getText());
        crisis.setPersonnelInvolved(personnelInvolvedTextArea.getText());

        // Outcome & follow-up
        if (outcomeComboBox.getValue() != null) {
            crisis.setImmediateOutcome(CrisisIntervention.Outcome.valueOf(outcomeComboBox.getValue()));
        }
        if (resolutionTimeField.getText() != null && !resolutionTimeField.getText().trim().isEmpty()) {
            try {
                crisis.setResolutionTime(LocalTime.parse(resolutionTimeField.getText().trim(), TIME_FORMATTER));
            } catch (DateTimeParseException e) {
                logger.warn("Could not parse resolution time", e);
            }
        }
        crisis.setOutcomeDetails(outcomeDetailsTextArea.getText());
        crisis.setFollowUpPlan(followUpPlanTextArea.getText());
        crisis.setFollowUpDate(followUpDatePicker.getValue());
        crisis.setAssignedCaseManager(caseManagerComboBox.getValue());

        // Documentation & reports
        crisis.setIncidentReportFiled(incidentReportCheckBox.isSelected());
        crisis.setMandatedReportFiled(mandatedReportCheckBox.isSelected());
        crisis.setDistrictNotified(districtNotifiedCheckBox.isSelected());
        crisis.setSafetyPlanDocumented(safetyPlanDocumentedCheckBox.isSelected());
        crisis.setParentReleaseSigned(parentReleaseCheckBox.isSelected());
        crisis.setPhotographsTaken(photographsCheckBox.isSelected());
        crisis.setAdditionalNotes(additionalNotesTextArea.getText());

        return crisis;
    }

    /**
     * Pre-select a student for the crisis intervention
     */
    public void setStudent(Student student) {
        this.preSelectedStudent = student;
        if (studentComboBox != null) {
            studentComboBox.setValue(student);
        }
    }

    /**
     * Load existing crisis intervention for editing/viewing
     */
    public void loadCrisisIntervention(CrisisIntervention crisis) {
        this.currentCrisis = crisis;
        if (crisis != null) {
            populateForm(crisis);
        }
    }

    private void populateForm(CrisisIntervention crisis) {
        studentComboBox.setValue(crisis.getStudent());
        crisisDatePicker.setValue(crisis.getCrisisDate());
        if (crisis.getCrisisTime() != null) {
            crisisTimeField.setText(crisis.getCrisisTime().format(TIME_FORMATTER));
        }
        if (crisis.getLocation() != null) {
            locationComboBox.setValue(crisis.getLocation().name());
        }
        locationDetailsField.setText(crisis.getLocationDetails());
        counselorComboBox.setValue(crisis.getRespondingCounselor());

        if (crisis.getCrisisType() != null) {
            crisisTypeComboBox.setValue(crisis.getCrisisType().name());
        }
        if (crisis.getSeverityLevel() != null) {
            severityComboBox.setValue(crisis.getSeverityLevel().name());
        }
        if (crisis.getResponseStatus() != null) {
            statusComboBox.setValue(crisis.getResponseStatus().name());
        }
        presentingCrisisTextArea.setText(crisis.getPresentingCrisis());

        immediateDangerCheckBox.setSelected(crisis.getImmediateDangerToSelf() != null && crisis.getImmediateDangerToSelf());
        dangerToOthersCheckBox.setSelected(crisis.getImmediateDangerToOthers() != null && crisis.getImmediateDangerToOthers());
        suicidePlanCheckBox.setSelected(crisis.getHasSuicidePlan() != null && crisis.getHasSuicidePlan());
        suicideMeansCheckBox.setSelected(crisis.getHasAccessToMeans() != null && crisis.getHasAccessToMeans());
        previousAttemptCheckBox.setSelected(crisis.getPreviousAttempt() != null && crisis.getPreviousAttempt());
        substanceInvolvementCheckBox.setSelected(crisis.getSubstanceInvolvement() != null && crisis.getSubstanceInvolvement());
        mentalHealthHistoryCheckBox.setSelected(crisis.getMentalHealthHistory() != null && crisis.getMentalHealthHistory());
        recentLossCheckBox.setSelected(crisis.getRecentLossTrauma() != null && crisis.getRecentLossTrauma());
        riskAssessmentTextArea.setText(crisis.getRiskAssessmentDetails());

        emergencyServicesCheckBox.setSelected(crisis.getEmergencyServicesCalled() != null && crisis.getEmergencyServicesCalled());
        hospitalTransportCheckBox.setSelected(crisis.getHospitalTransport() != null && crisis.getHospitalTransport());
        parentNotifiedCheckBox.setSelected(crisis.getParentNotified() != null && crisis.getParentNotified());
        adminNotifiedCheckBox.setSelected(crisis.getAdminNotified() != null && crisis.getAdminNotified());
        policeInvolvedCheckBox.setSelected(crisis.getPoliceInvolved() != null && crisis.getPoliceInvolved());
        crisisTeamActivatedCheckBox.setSelected(crisis.getCrisisTeamActivated() != null && crisis.getCrisisTeamActivated());
        safetyPlanCreatedCheckBox.setSelected(crisis.getSafetyPlanCreated() != null && crisis.getSafetyPlanCreated());
        weaponsRemovedCheckBox.setSelected(crisis.getWeaponsMeansRemoved() != null && crisis.getWeaponsMeansRemoved());
        oneToOneSupervisionCheckBox.setSelected(crisis.getOneToOneSupervision() != null && crisis.getOneToOneSupervision());
        deEscalationCheckBox.setSelected(crisis.getDeEscalationUsed() != null && crisis.getDeEscalationUsed());
        isolationCheckBox.setSelected(crisis.getStudentIsolated() != null && crisis.getStudentIsolated());
        restraintCheckBox.setSelected(crisis.getPhysicalRestraintUsed() != null && crisis.getPhysicalRestraintUsed());
        interventionActionsTextArea.setText(crisis.getInterventionActions());

        if (crisis.getParentContactTime() != null) {
            parentContactTimeField.setText(crisis.getParentContactTime().format(TIME_FORMATTER));
        }
        if (crisis.getParentContactMethod() != null) {
            parentContactMethodComboBox.setValue(crisis.getParentContactMethod().name());
        }
        parentContactedField.setText(crisis.getParentContacted());
        parentResponseTextArea.setText(crisis.getParentResponse());
        personnelInvolvedTextArea.setText(crisis.getPersonnelInvolved());

        if (crisis.getImmediateOutcome() != null) {
            outcomeComboBox.setValue(crisis.getImmediateOutcome().name());
        }
        if (crisis.getResolutionTime() != null) {
            resolutionTimeField.setText(crisis.getResolutionTime().format(TIME_FORMATTER));
        }
        outcomeDetailsTextArea.setText(crisis.getOutcomeDetails());
        followUpPlanTextArea.setText(crisis.getFollowUpPlan());
        followUpDatePicker.setValue(crisis.getFollowUpDate());
        caseManagerComboBox.setValue(crisis.getAssignedCaseManager());

        incidentReportCheckBox.setSelected(crisis.getIncidentReportFiled() != null && crisis.getIncidentReportFiled());
        mandatedReportCheckBox.setSelected(crisis.getMandatedReportFiled() != null && crisis.getMandatedReportFiled());
        districtNotifiedCheckBox.setSelected(crisis.getDistrictNotified() != null && crisis.getDistrictNotified());
        safetyPlanDocumentedCheckBox.setSelected(crisis.getSafetyPlanDocumented() != null && crisis.getSafetyPlanDocumented());
        parentReleaseCheckBox.setSelected(crisis.getParentReleaseSigned() != null && crisis.getParentReleaseSigned());
        photographsCheckBox.setSelected(crisis.getPhotographsTaken() != null && crisis.getPhotographsTaken());
        additionalNotesTextArea.setText(crisis.getAdditionalNotes());
    }

    /**
     * Set the form to view-only mode (disable editing)
     */
    public void setViewMode(boolean viewMode) {
        if (viewMode) {
            // Disable all form controls
            studentComboBox.setDisable(true);
            crisisDatePicker.setDisable(true);
            crisisTimeField.setDisable(true);
            locationComboBox.setDisable(true);
            locationDetailsField.setDisable(true);
            counselorComboBox.setDisable(true);
            crisisTypeComboBox.setDisable(true);
            severityComboBox.setDisable(true);
            statusComboBox.setDisable(true);
            presentingCrisisTextArea.setDisable(true);

            immediateDangerCheckBox.setDisable(true);
            dangerToOthersCheckBox.setDisable(true);
            suicidePlanCheckBox.setDisable(true);
            suicideMeansCheckBox.setDisable(true);
            previousAttemptCheckBox.setDisable(true);
            substanceInvolvementCheckBox.setDisable(true);
            mentalHealthHistoryCheckBox.setDisable(true);
            recentLossCheckBox.setDisable(true);
            riskAssessmentTextArea.setDisable(true);

            emergencyServicesCheckBox.setDisable(true);
            hospitalTransportCheckBox.setDisable(true);
            parentNotifiedCheckBox.setDisable(true);
            adminNotifiedCheckBox.setDisable(true);
            policeInvolvedCheckBox.setDisable(true);
            crisisTeamActivatedCheckBox.setDisable(true);
            safetyPlanCreatedCheckBox.setDisable(true);
            weaponsRemovedCheckBox.setDisable(true);
            oneToOneSupervisionCheckBox.setDisable(true);
            deEscalationCheckBox.setDisable(true);
            isolationCheckBox.setDisable(true);
            restraintCheckBox.setDisable(true);
            interventionActionsTextArea.setDisable(true);

            parentContactTimeField.setDisable(true);
            parentContactMethodComboBox.setDisable(true);
            parentContactedField.setDisable(true);
            parentResponseTextArea.setDisable(true);
            personnelInvolvedTextArea.setDisable(true);

            outcomeComboBox.setDisable(true);
            resolutionTimeField.setDisable(true);
            outcomeDetailsTextArea.setDisable(true);
            followUpPlanTextArea.setDisable(true);
            followUpDatePicker.setDisable(true);
            caseManagerComboBox.setDisable(true);

            incidentReportCheckBox.setDisable(true);
            mandatedReportCheckBox.setDisable(true);
            districtNotifiedCheckBox.setDisable(true);
            safetyPlanDocumentedCheckBox.setDisable(true);
            parentReleaseCheckBox.setDisable(true);
            photographsCheckBox.setDisable(true);
            additionalNotesTextArea.setDisable(true);

            saveButton.setVisible(false);
            cancelButton.setText("Close");
        }
    }

    private void showValidationErrors(List<String> errors) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Crisis Report Validation Errors");
        alert.setHeaderText("The following errors must be corrected:");
        alert.setContentText(String.join("\n\n", errors));
        alert.showAndWait();
    }

    private void showCriticalSuccess() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Crisis Intervention Saved");
        alert.setHeaderText("Crisis intervention report saved successfully");
        alert.setContentText(
                "IMPORTANT NEXT STEPS:\n\n" +
                        "1. Ensure all required follow-up actions are completed\n" +
                        "2. File any mandated reports if applicable\n" +
                        "3. Notify district office if required\n" +
                        "4. Schedule follow-up with student and family\n" +
                        "5. Brief crisis team on outcome\n\n" +
                        "Crisis report has been securely documented.");
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
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}
