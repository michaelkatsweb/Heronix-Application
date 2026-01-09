package com.heronix.ui.controller;

import com.heronix.model.domain.NurseVisit;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.Teacher;
import com.heronix.service.HealthOfficeService;
import com.heronix.service.StudentService;
import com.heronix.service.TeacherService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Nurse Visit Log Form
 * Handles documentation of student visits to the health office
 */
@Component
public class NurseVisitLogController {

    private static final Logger logger = LoggerFactory.getLogger(NurseVisitLogController.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    private HealthOfficeService healthOfficeService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    // Student & Visit Information
    @FXML private ComboBox<Student> studentComboBox;
    @FXML private TextField gradeField;
    @FXML private DatePicker visitDatePicker;
    @FXML private TextField visitTimeField;
    @FXML private ComboBox<Teacher> attendingNurseComboBox;
    @FXML private TextField checkInTimeField;
    @FXML private TextField checkOutTimeField;
    @FXML private TextField durationField;

    // Visit Reason
    @FXML private ComboBox<String> visitReasonComboBox;
    @FXML private VBox otherReasonBox;
    @FXML private TextField otherReasonField;
    @FXML private TextField chiefComplaintField;

    // Symptoms & Assessment
    @FXML private CheckBox hasFeverCheckBox;
    @FXML private CheckBox hasVomitingCheckBox;
    @FXML private CheckBox hasHeadacheCheckBox;
    @FXML private CheckBox hasStomachAcheCheckBox;
    @FXML private CheckBox hasInjuryCheckBox;
    @FXML private TextArea symptomsTextArea;
    @FXML private VBox injuryDetailsBox;
    @FXML private ComboBox<String> injurySeverityComboBox;
    @FXML private TextArea injuryDescriptionTextArea;

    // Vital Signs
    @FXML private TextField temperatureField;
    @FXML private TextField bloodPressureField;
    @FXML private TextField heartRateField;
    @FXML private TextField respiratoryRateField;
    @FXML private TextField oxygenSaturationField;

    // Treatment
    @FXML private CheckBox medicationAdministeredCheckBox;
    @FXML private CheckBox iceAppliedCheckBox;
    @FXML private CheckBox bandageAppliedCheckBox;
    @FXML private VBox medicationDetailsBox;
    @FXML private TextArea medicationDetailsTextArea;
    @FXML private TextField restPeriodField;
    @FXML private TextArea treatmentProvidedTextArea;

    // Disposition
    @FXML private ComboBox<String> dispositionComboBox;
    @FXML private TextField dispositionTimeField;
    @FXML private VBox sentHomeBox;
    @FXML private TextArea sentHomeReasonTextArea;
    @FXML private CheckBox parentPickedUpCheckBox;
    @FXML private TextField parentPickupTimeField;

    // Parent Communication
    @FXML private CheckBox parentNotifiedCheckBox;
    @FXML private VBox parentNotificationBox;
    @FXML private ComboBox<String> parentNotificationMethodComboBox;
    @FXML private TextField parentNotificationTimeField;
    @FXML private TextArea parentNotificationNotesTextArea;

    // Follow-Up & Referrals
    @FXML private CheckBox requiresFollowUpCheckBox;
    @FXML private CheckBox physicianReferralNeededCheckBox;
    @FXML private CheckBox incidentReportFiledCheckBox;
    @FXML private VBox followUpBox;
    @FXML private TextArea followUpInstructionsTextArea;
    @FXML private VBox physicianReferralBox;
    @FXML private TextArea physicianReferralReasonTextArea;
    @FXML private VBox incidentReportBox;
    @FXML private TextField incidentReportNumberField;

    // Nurse Notes
    @FXML private TextArea nurseNotesTextArea;
    @FXML private TextArea confidentialNotesTextArea;

    // Buttons
    @FXML private Button checkOutButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label statusIndicator;

    private NurseVisit currentVisit;
    private boolean isViewMode = false;

    @FXML
    public void initialize() {
        logger.info("Initializing NurseVisitLogController");

        setupComboBoxes();
        setupConditionalVisibility();
        setupValidation();
        loadInitialData();
    }

    private void setupComboBoxes() {
        // Student ComboBox
        studentComboBox.setConverter(new javafx.util.StringConverter<Student>() {
            @Override
            public String toString(Student student) {
                return student != null ? student.getFirstName() + " " + student.getLastName() +
                       " (" + student.getStudentId() + ")" : "";
            }

            @Override
            public Student fromString(String string) {
                return null;
            }
        });

        // Attending Nurse ComboBox
        attendingNurseComboBox.setConverter(new javafx.util.StringConverter<Teacher>() {
            @Override
            public String toString(Teacher teacher) {
                return teacher != null ? teacher.getFirstName() + " " + teacher.getLastName() : "";
            }

            @Override
            public Teacher fromString(String string) {
                return null;
            }
        });
    }

    private void setupConditionalVisibility() {
        // Auto-populate grade when student selected
        studentComboBox.setOnAction(e -> {
            Student student = studentComboBox.getValue();
            if (student != null) {
                gradeField.setText(String.valueOf(student.getGradeLevel()));
            }
        });

        // Show/hide other reason field
        visitReasonComboBox.setOnAction(e -> {
            boolean isOther = "OTHER".equals(visitReasonComboBox.getValue());
            otherReasonBox.setVisible(isOther);
            otherReasonBox.setManaged(isOther);
        });

        // Show/hide injury details
        hasInjuryCheckBox.setOnAction(e -> {
            boolean hasInjury = hasInjuryCheckBox.isSelected();
            injuryDetailsBox.setVisible(hasInjury);
            injuryDetailsBox.setManaged(hasInjury);
        });

        // Show/hide medication details
        medicationAdministeredCheckBox.setOnAction(e -> {
            boolean medGiven = medicationAdministeredCheckBox.isSelected();
            medicationDetailsBox.setVisible(medGiven);
            medicationDetailsBox.setManaged(medGiven);
        });

        // Show/hide sent home details
        dispositionComboBox.setOnAction(e -> {
            boolean sentHome = "SENT_HOME".equals(dispositionComboBox.getValue());
            sentHomeBox.setVisible(sentHome);
            sentHomeBox.setManaged(sentHome);
        });

        // Show/hide parent notification details
        parentNotifiedCheckBox.setOnAction(e -> {
            boolean notified = parentNotifiedCheckBox.isSelected();
            parentNotificationBox.setVisible(notified);
            parentNotificationBox.setManaged(notified);
        });

        // Show/hide follow-up details
        requiresFollowUpCheckBox.setOnAction(e -> {
            boolean followUp = requiresFollowUpCheckBox.isSelected();
            followUpBox.setVisible(followUp);
            followUpBox.setManaged(followUp);
        });

        // Show/hide physician referral details
        physicianReferralNeededCheckBox.setOnAction(e -> {
            boolean referral = physicianReferralNeededCheckBox.isSelected();
            physicianReferralBox.setVisible(referral);
            physicianReferralBox.setManaged(referral);
        });

        // Show/hide incident report details
        incidentReportFiledCheckBox.setOnAction(e -> {
            boolean incident = incidentReportFiledCheckBox.isSelected();
            incidentReportBox.setVisible(incident);
            incidentReportBox.setManaged(incident);
        });
    }

    private void setupValidation() {
        // Auto-detect fever from temperature
        temperatureField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                try {
                    double temp = Double.parseDouble(newValue);
                    hasFeverCheckBox.setSelected(temp >= 100.4);
                } catch (NumberFormatException e) {
                    // Ignore invalid input
                }
            }
        });
    }

    private void loadInitialData() {
        try {
            // Load all students
            List<Student> students = studentService.findAll();
            studentComboBox.setItems(javafx.collections.FXCollections.observableArrayList(students));

            // Load all teachers (for nurse selection)
            List<Teacher> teachers = teacherService.findAll();
            attendingNurseComboBox.setItems(javafx.collections.FXCollections.observableArrayList(teachers));

            // Set defaults for new visit
            if (currentVisit == null) {
                visitDatePicker.setValue(LocalDate.now());
                visitTimeField.setText(LocalTime.now().format(TIME_FORMATTER));
                checkInTimeField.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")));
            }

        } catch (Exception e) {
            logger.error("Error loading initial data", e);
            showError("Error loading data: " + e.getMessage());
        }
    }

    @FXML
    private void handleCheckOut() {
        if (currentVisit == null) {
            showError("Cannot check out - no active visit");
            return;
        }

        if (currentVisit.getCheckOutTime() != null) {
            showInfo("Student already checked out");
            return;
        }

        if (!validateForm()) {
            return;
        }

        try {
            // Update visit with form data
            NurseVisit visit = buildVisitFromForm();

            // Set checkout time
            visit.setCheckOutTime(LocalDateTime.now());
            checkOutTimeField.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")));

            // Calculate and display duration
            if (visit.getVisitDurationMinutes() != null) {
                durationField.setText(visit.getVisitDurationMinutes() + " minutes");
            }

            // Save
            healthOfficeService.updateNurseVisit(visit);

            showSuccess("Student checked out successfully");
            checkOutButton.setDisable(true);

        } catch (Exception e) {
            logger.error("Error checking out student", e);
            showError("Error checking out: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        try {
            NurseVisit visit = buildVisitFromForm();

            if (currentVisit == null) {
                // New visit - check in student
                NurseVisit savedVisit = healthOfficeService.checkInStudent(
                    visit.getStudent(),
                    visit.getVisitReason(),
                    visit.getChiefComplaint(),
                    visit.getAttendingNurseId()
                );

                // Copy additional details to saved visit
                savedVisit.setVisitDate(visit.getVisitDate());
                savedVisit.setVisitTime(visit.getVisitTime());
                savedVisit.setVisitReasonOther(visit.getVisitReasonOther());
                savedVisit.setSymptoms(visit.getSymptoms());
                savedVisit.setHasFever(visit.getHasFever());
                savedVisit.setTemperature(visit.getTemperature());
                savedVisit.setHasVomiting(visit.getHasVomiting());
                savedVisit.setHasHeadache(visit.getHasHeadache());
                savedVisit.setHasStomachAche(visit.getHasStomachAche());
                savedVisit.setHasInjury(visit.getHasInjury());
                savedVisit.setInjuryDescription(visit.getInjuryDescription());
                savedVisit.setInjurySeverity(visit.getInjurySeverity());
                savedVisit.setBloodPressure(visit.getBloodPressure());
                savedVisit.setHeartRate(visit.getHeartRate());
                savedVisit.setRespiratoryRate(visit.getRespiratoryRate());
                savedVisit.setOxygenSaturation(visit.getOxygenSaturation());
                savedVisit.setTreatmentProvided(visit.getTreatmentProvided());
                savedVisit.setMedicationAdministered(visit.getMedicationAdministered());
                savedVisit.setMedicationDetails(visit.getMedicationDetails());
                savedVisit.setIceApplied(visit.getIceApplied());
                savedVisit.setBandageApplied(visit.getBandageApplied());
                savedVisit.setRestPeriodMinutes(visit.getRestPeriodMinutes());
                savedVisit.setDisposition(visit.getDisposition());
                savedVisit.setDispositionTime(visit.getDispositionTime());
                savedVisit.setSentHome(visit.getSentHome());
                savedVisit.setSentHomeReason(visit.getSentHomeReason());
                savedVisit.setParentPickedUp(visit.getParentPickedUp());
                savedVisit.setParentPickupTime(visit.getParentPickupTime());
                savedVisit.setParentNotified(visit.getParentNotified());
                savedVisit.setParentNotificationTime(visit.getParentNotificationTime());
                savedVisit.setParentNotificationMethod(visit.getParentNotificationMethod());
                savedVisit.setParentNotificationNotes(visit.getParentNotificationNotes());
                savedVisit.setRequiresFollowUp(visit.getRequiresFollowUp());
                savedVisit.setFollowUpInstructions(visit.getFollowUpInstructions());
                savedVisit.setPhysicianReferralNeeded(visit.getPhysicianReferralNeeded());
                savedVisit.setPhysicianReferralReason(visit.getPhysicianReferralReason());
                savedVisit.setIncidentReportFiled(visit.getIncidentReportFiled());
                savedVisit.setIncidentReportNumber(visit.getIncidentReportNumber());
                savedVisit.setNurseNotes(visit.getNurseNotes());
                savedVisit.setConfidentialNotes(visit.getConfidentialNotes());

                currentVisit = healthOfficeService.updateNurseVisit(savedVisit);

                showSuccess("Nurse visit created successfully");
            } else {
                // Update existing visit
                currentVisit = healthOfficeService.updateNurseVisit(visit);
                showSuccess("Nurse visit updated successfully");
            }

            closeWindow();

        } catch (Exception e) {
            logger.error("Error saving nurse visit", e);
            showError("Error saving visit: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateForm() {
        List<String> errors = new ArrayList<>();

        if (studentComboBox.getValue() == null) {
            errors.add("Student is required");
        }

        if (visitDatePicker.getValue() == null) {
            errors.add("Visit date is required");
        }

        if (visitTimeField.getText() == null || visitTimeField.getText().isEmpty()) {
            errors.add("Visit time is required");
        } else {
            try {
                LocalTime.parse(visitTimeField.getText(), TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                errors.add("Visit time must be in HH:MM format (e.g., 09:30)");
            }
        }

        if (attendingNurseComboBox.getValue() == null) {
            errors.add("Attending nurse is required");
        }

        if (visitReasonComboBox.getValue() == null || visitReasonComboBox.getValue().isEmpty()) {
            errors.add("Visit reason is required");
        }

        if ("OTHER".equals(visitReasonComboBox.getValue()) &&
            (otherReasonField.getText() == null || otherReasonField.getText().trim().isEmpty())) {
            errors.add("Other reason details are required when 'Other' is selected");
        }

        if (chiefComplaintField.getText() == null || chiefComplaintField.getText().trim().isEmpty()) {
            errors.add("Chief complaint is required");
        }

        if (hasInjuryCheckBox.isSelected()) {
            if (injurySeverityComboBox.getValue() == null || injurySeverityComboBox.getValue().isEmpty()) {
                errors.add("Injury severity is required when injury is indicated");
            }
        }

        if (medicationAdministeredCheckBox.isSelected()) {
            if (medicationDetailsTextArea.getText() == null || medicationDetailsTextArea.getText().trim().isEmpty()) {
                errors.add("Medication details are required when medication is administered");
            }
        }

        if (dispositionComboBox.getValue() == null || dispositionComboBox.getValue().isEmpty()) {
            errors.add("Disposition is required");
        }

        if ("SENT_HOME".equals(dispositionComboBox.getValue())) {
            if (sentHomeReasonTextArea.getText() == null || sentHomeReasonTextArea.getText().trim().isEmpty()) {
                errors.add("Sent home reason is required");
            }
        }

        if (parentNotifiedCheckBox.isSelected()) {
            if (parentNotificationMethodComboBox.getValue() == null || parentNotificationMethodComboBox.getValue().isEmpty()) {
                errors.add("Parent notification method is required when parent is notified");
            }
        }

        if (requiresFollowUpCheckBox.isSelected()) {
            if (followUpInstructionsTextArea.getText() == null || followUpInstructionsTextArea.getText().trim().isEmpty()) {
                errors.add("Follow-up instructions are required");
            }
        }

        if (physicianReferralNeededCheckBox.isSelected()) {
            if (physicianReferralReasonTextArea.getText() == null || physicianReferralReasonTextArea.getText().trim().isEmpty()) {
                errors.add("Physician referral reason is required");
            }
        }

        if (incidentReportFiledCheckBox.isSelected()) {
            if (incidentReportNumberField.getText() == null || incidentReportNumberField.getText().trim().isEmpty()) {
                errors.add("Incident report number is required");
            }
        }

        if (!errors.isEmpty()) {
            showValidationErrors(errors);
            return false;
        }

        return true;
    }

    private NurseVisit buildVisitFromForm() {
        NurseVisit visit = currentVisit != null ? currentVisit : new NurseVisit();

        // Student & Visit Information
        visit.setStudent(studentComboBox.getValue());
        visit.setVisitDate(visitDatePicker.getValue());
        visit.setVisitTime(LocalTime.parse(visitTimeField.getText(), TIME_FORMATTER));
        visit.setAttendingNurseId(attendingNurseComboBox.getValue().getId());

        // Visit Reason
        visit.setVisitReason(NurseVisit.VisitReason.valueOf(visitReasonComboBox.getValue()));
        visit.setVisitReasonOther(otherReasonField.getText());
        visit.setChiefComplaint(chiefComplaintField.getText());

        // Symptoms
        visit.setHasFever(hasFeverCheckBox.isSelected());
        visit.setHasVomiting(hasVomitingCheckBox.isSelected());
        visit.setHasHeadache(hasHeadacheCheckBox.isSelected());
        visit.setHasStomachAche(hasStomachAcheCheckBox.isSelected());
        visit.setHasInjury(hasInjuryCheckBox.isSelected());
        visit.setSymptoms(symptomsTextArea.getText());

        // Injury details
        if (hasInjuryCheckBox.isSelected()) {
            if (injurySeverityComboBox.getValue() != null && !injurySeverityComboBox.getValue().isEmpty()) {
                visit.setInjurySeverity(NurseVisit.InjurySeverity.valueOf(injurySeverityComboBox.getValue()));
            }
            visit.setInjuryDescription(injuryDescriptionTextArea.getText());
        }

        // Vital Signs
        if (temperatureField.getText() != null && !temperatureField.getText().isEmpty()) {
            try {
                visit.setTemperature(Double.parseDouble(temperatureField.getText()));
            } catch (NumberFormatException e) {
                logger.warn("Invalid temperature format: " + temperatureField.getText());
            }
        }
        visit.setBloodPressure(bloodPressureField.getText());
        if (heartRateField.getText() != null && !heartRateField.getText().isEmpty()) {
            try {
                visit.setHeartRate(Integer.parseInt(heartRateField.getText()));
            } catch (NumberFormatException e) {
                logger.warn("Invalid heart rate format: " + heartRateField.getText());
            }
        }
        if (respiratoryRateField.getText() != null && !respiratoryRateField.getText().isEmpty()) {
            try {
                visit.setRespiratoryRate(Integer.parseInt(respiratoryRateField.getText()));
            } catch (NumberFormatException e) {
                logger.warn("Invalid respiratory rate format: " + respiratoryRateField.getText());
            }
        }
        if (oxygenSaturationField.getText() != null && !oxygenSaturationField.getText().isEmpty()) {
            try {
                visit.setOxygenSaturation(Integer.parseInt(oxygenSaturationField.getText()));
            } catch (NumberFormatException e) {
                logger.warn("Invalid oxygen saturation format: " + oxygenSaturationField.getText());
            }
        }

        // Treatment
        visit.setMedicationAdministered(medicationAdministeredCheckBox.isSelected());
        visit.setMedicationDetails(medicationDetailsTextArea.getText());
        visit.setIceApplied(iceAppliedCheckBox.isSelected());
        visit.setBandageApplied(bandageAppliedCheckBox.isSelected());
        if (restPeriodField.getText() != null && !restPeriodField.getText().isEmpty()) {
            try {
                visit.setRestPeriodMinutes(Integer.parseInt(restPeriodField.getText()));
            } catch (NumberFormatException e) {
                logger.warn("Invalid rest period format: " + restPeriodField.getText());
            }
        }
        visit.setTreatmentProvided(treatmentProvidedTextArea.getText());

        // Disposition
        visit.setDisposition(NurseVisit.Disposition.valueOf(dispositionComboBox.getValue()));
        if (dispositionTimeField.getText() != null && !dispositionTimeField.getText().isEmpty()) {
            try {
                visit.setDispositionTime(LocalTime.parse(dispositionTimeField.getText(), TIME_FORMATTER));
            } catch (DateTimeParseException e) {
                logger.warn("Invalid disposition time format: " + dispositionTimeField.getText());
            }
        }
        visit.setSentHome("SENT_HOME".equals(dispositionComboBox.getValue()));
        visit.setSentHomeReason(sentHomeReasonTextArea.getText());
        visit.setParentPickedUp(parentPickedUpCheckBox.isSelected());
        if (parentPickupTimeField.getText() != null && !parentPickupTimeField.getText().isEmpty()) {
            try {
                visit.setParentPickupTime(LocalTime.parse(parentPickupTimeField.getText(), TIME_FORMATTER));
            } catch (DateTimeParseException e) {
                logger.warn("Invalid parent pickup time format: " + parentPickupTimeField.getText());
            }
        }

        // Parent Communication
        visit.setParentNotified(parentNotifiedCheckBox.isSelected());
        if (parentNotificationMethodComboBox.getValue() != null && !parentNotificationMethodComboBox.getValue().isEmpty()) {
            visit.setParentNotificationMethod(NurseVisit.ContactMethod.valueOf(parentNotificationMethodComboBox.getValue()));
        }
        if (parentNotificationTimeField.getText() != null && !parentNotificationTimeField.getText().isEmpty()) {
            try {
                visit.setParentNotificationTime(LocalTime.parse(parentNotificationTimeField.getText(), TIME_FORMATTER));
            } catch (DateTimeParseException e) {
                logger.warn("Invalid parent notification time format: " + parentNotificationTimeField.getText());
            }
        }
        visit.setParentNotificationNotes(parentNotificationNotesTextArea.getText());

        // Follow-Up & Referrals
        visit.setRequiresFollowUp(requiresFollowUpCheckBox.isSelected());
        visit.setFollowUpInstructions(followUpInstructionsTextArea.getText());
        visit.setPhysicianReferralNeeded(physicianReferralNeededCheckBox.isSelected());
        visit.setPhysicianReferralReason(physicianReferralReasonTextArea.getText());
        visit.setIncidentReportFiled(incidentReportFiledCheckBox.isSelected());
        visit.setIncidentReportNumber(incidentReportNumberField.getText());

        // Nurse Notes
        visit.setNurseNotes(nurseNotesTextArea.getText());
        visit.setConfidentialNotes(confidentialNotesTextArea.getText());

        return visit;
    }

    /**
     * Load existing visit for editing
     */
    public void loadVisit(NurseVisit visit) {
        this.currentVisit = visit;
        populateFormFromVisit(visit);
    }

    /**
     * Set form to view-only mode
     */
    public void setViewMode(boolean viewMode) {
        this.isViewMode = viewMode;
        if (viewMode) {
            // Disable all input controls
            studentComboBox.setDisable(true);
            visitDatePicker.setDisable(true);
            visitTimeField.setEditable(false);
            attendingNurseComboBox.setDisable(true);
            visitReasonComboBox.setDisable(true);
            otherReasonField.setEditable(false);
            chiefComplaintField.setEditable(false);

            // Disable checkboxes and text areas
            hasFeverCheckBox.setDisable(true);
            hasVomitingCheckBox.setDisable(true);
            hasHeadacheCheckBox.setDisable(true);
            hasStomachAcheCheckBox.setDisable(true);
            hasInjuryCheckBox.setDisable(true);
            symptomsTextArea.setEditable(false);
            injurySeverityComboBox.setDisable(true);
            injuryDescriptionTextArea.setEditable(false);

            temperatureField.setEditable(false);
            bloodPressureField.setEditable(false);
            heartRateField.setEditable(false);
            respiratoryRateField.setEditable(false);
            oxygenSaturationField.setEditable(false);

            medicationAdministeredCheckBox.setDisable(true);
            iceAppliedCheckBox.setDisable(true);
            bandageAppliedCheckBox.setDisable(true);
            medicationDetailsTextArea.setEditable(false);
            restPeriodField.setEditable(false);
            treatmentProvidedTextArea.setEditable(false);

            dispositionComboBox.setDisable(true);
            dispositionTimeField.setEditable(false);
            sentHomeReasonTextArea.setEditable(false);
            parentPickedUpCheckBox.setDisable(true);
            parentPickupTimeField.setEditable(false);

            parentNotifiedCheckBox.setDisable(true);
            parentNotificationMethodComboBox.setDisable(true);
            parentNotificationTimeField.setEditable(false);
            parentNotificationNotesTextArea.setEditable(false);

            requiresFollowUpCheckBox.setDisable(true);
            physicianReferralNeededCheckBox.setDisable(true);
            incidentReportFiledCheckBox.setDisable(true);
            followUpInstructionsTextArea.setEditable(false);
            physicianReferralReasonTextArea.setEditable(false);
            incidentReportNumberField.setEditable(false);

            nurseNotesTextArea.setEditable(false);
            confidentialNotesTextArea.setEditable(false);

            // Hide action buttons
            checkOutButton.setVisible(false);
            saveButton.setVisible(false);
            cancelButton.setText("Close");
        }
    }

    private void populateFormFromVisit(NurseVisit visit) {
        // Student & Visit Information
        studentComboBox.setValue(visit.getStudent());
        gradeField.setText(String.valueOf(visit.getStudent().getGradeLevel()));
        visitDatePicker.setValue(visit.getVisitDate());
        visitTimeField.setText(visit.getVisitTime().format(TIME_FORMATTER));
        checkInTimeField.setText(visit.getCheckInTime().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")));
        if (visit.getCheckOutTime() != null) {
            checkOutTimeField.setText(visit.getCheckOutTime().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")));
            checkOutButton.setDisable(true);
        }
        if (visit.getVisitDurationMinutes() != null) {
            durationField.setText(visit.getVisitDurationMinutes() + " minutes");
        }

        // Visit Reason
        visitReasonComboBox.setValue(visit.getVisitReason().toString());
        otherReasonField.setText(visit.getVisitReasonOther());
        chiefComplaintField.setText(visit.getChiefComplaint());

        // Symptoms
        hasFeverCheckBox.setSelected(visit.getHasFever());
        hasVomitingCheckBox.setSelected(visit.getHasVomiting());
        hasHeadacheCheckBox.setSelected(visit.getHasHeadache());
        hasStomachAcheCheckBox.setSelected(visit.getHasStomachAche());
        hasInjuryCheckBox.setSelected(visit.getHasInjury());
        symptomsTextArea.setText(visit.getSymptoms());

        if (visit.getInjurySeverity() != null) {
            injurySeverityComboBox.setValue(visit.getInjurySeverity().toString());
        }
        injuryDescriptionTextArea.setText(visit.getInjuryDescription());

        // Vital Signs
        if (visit.getTemperature() != null) {
            temperatureField.setText(String.format("%.1f", visit.getTemperature()));
        }
        bloodPressureField.setText(visit.getBloodPressure());
        if (visit.getHeartRate() != null) {
            heartRateField.setText(String.valueOf(visit.getHeartRate()));
        }
        if (visit.getRespiratoryRate() != null) {
            respiratoryRateField.setText(String.valueOf(visit.getRespiratoryRate()));
        }
        if (visit.getOxygenSaturation() != null) {
            oxygenSaturationField.setText(String.valueOf(visit.getOxygenSaturation()));
        }

        // Treatment
        medicationAdministeredCheckBox.setSelected(visit.getMedicationAdministered());
        medicationDetailsTextArea.setText(visit.getMedicationDetails());
        iceAppliedCheckBox.setSelected(visit.getIceApplied());
        bandageAppliedCheckBox.setSelected(visit.getBandageApplied());
        if (visit.getRestPeriodMinutes() != null) {
            restPeriodField.setText(String.valueOf(visit.getRestPeriodMinutes()));
        }
        treatmentProvidedTextArea.setText(visit.getTreatmentProvided());

        // Disposition
        dispositionComboBox.setValue(visit.getDisposition().toString());
        if (visit.getDispositionTime() != null) {
            dispositionTimeField.setText(visit.getDispositionTime().format(TIME_FORMATTER));
        }
        sentHomeReasonTextArea.setText(visit.getSentHomeReason());
        parentPickedUpCheckBox.setSelected(visit.getParentPickedUp());
        if (visit.getParentPickupTime() != null) {
            parentPickupTimeField.setText(visit.getParentPickupTime().format(TIME_FORMATTER));
        }

        // Parent Communication
        parentNotifiedCheckBox.setSelected(visit.getParentNotified());
        if (visit.getParentNotificationMethod() != null) {
            parentNotificationMethodComboBox.setValue(visit.getParentNotificationMethod().toString());
        }
        if (visit.getParentNotificationTime() != null) {
            parentNotificationTimeField.setText(visit.getParentNotificationTime().format(TIME_FORMATTER));
        }
        parentNotificationNotesTextArea.setText(visit.getParentNotificationNotes());

        // Follow-Up & Referrals
        requiresFollowUpCheckBox.setSelected(visit.getRequiresFollowUp());
        followUpInstructionsTextArea.setText(visit.getFollowUpInstructions());
        physicianReferralNeededCheckBox.setSelected(visit.getPhysicianReferralNeeded());
        physicianReferralReasonTextArea.setText(visit.getPhysicianReferralReason());
        incidentReportFiledCheckBox.setSelected(visit.getIncidentReportFiled());
        incidentReportNumberField.setText(visit.getIncidentReportNumber());

        // Nurse Notes
        nurseNotesTextArea.setText(visit.getNurseNotes());
        confidentialNotesTextArea.setText(visit.getConfidentialNotes());
    }

    private void showValidationErrors(List<String> errors) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText("Please correct the following errors:");
        alert.setContentText(String.join("\n", errors));
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        statusIndicator.setText(message);
        statusIndicator.setStyle("-fx-text-fill: green;");
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
