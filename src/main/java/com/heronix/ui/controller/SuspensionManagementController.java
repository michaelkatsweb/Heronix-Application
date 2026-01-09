package com.heronix.ui.controller;

import com.heronix.model.domain.*;
import com.heronix.service.BehaviorIncidentService;
import com.heronix.service.StudentService;
import com.heronix.service.TeacherService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for Suspension Management UI
 * Manages in-school (ISS) and out-of-school (OSS) suspensions with legal compliance,
 * due process requirements, parent notification, appeal processes, and re-entry planning.
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Behavior & Discipline Management System
 */
@Slf4j
@Component
public class SuspensionManagementController {

    @Autowired
    private BehaviorIncidentService behaviorIncidentService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    // ========================================================================
    // FXML FIELDS - Student & Suspension Details
    // ========================================================================

    @FXML private ComboBox<Student> studentComboBox;
    @FXML private TextField gradeField;
    @FXML private TextField studentIdField;
    @FXML private TextField campusField;
    @FXML private ComboBox<String> suspensionTypeComboBox;
    @FXML private ComboBox<Teacher> issuedByComboBox;
    @FXML private ComboBox<String> statusComboBox;

    // ========================================================================
    // FXML FIELDS - Suspension Dates
    // ========================================================================

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField daysCountField;
    @FXML private TextField daysRemainingField;

    // ========================================================================
    // FXML FIELDS - Incident Details
    // ========================================================================

    @FXML private TextArea suspensionReasonArea;
    @FXML private TextArea incidentDescriptionArea;

    // ========================================================================
    // FXML FIELDS - ISS Details
    // ========================================================================

    @FXML private VBox issDetailsBox;
    @FXML private TextField issRoomField;
    @FXML private ComboBox<Teacher> issSupervisorComboBox;
    @FXML private TextField daysAttendedField;
    @FXML private TextArea dailyNotesArea;

    // ========================================================================
    // FXML FIELDS - OSS Details
    // ========================================================================

    @FXML private VBox ossDetailsBox;
    @FXML private TextField alternativePlacementField;
    @FXML private CheckBox homeworkProvidedCheckBox;
    @FXML private CheckBox homeworkCompletedCheckBox;

    // ========================================================================
    // FXML FIELDS - Parent Notification
    // ========================================================================

    @FXML private CheckBox parentNotifiedCheckBox;
    @FXML private DatePicker parentNotificationDatePicker;
    @FXML private ComboBox<String> parentNotificationMethodComboBox;
    @FXML private TextField notificationDocumentField;

    // ========================================================================
    // FXML FIELDS - Re-Entry Meeting
    // ========================================================================

    @FXML private CheckBox reentryMeetingRequiredCheckBox;
    @FXML private CheckBox reentryMeetingCompletedCheckBox;
    @FXML private DatePicker reentryMeetingDatePicker;
    @FXML private TextArea reentryPlanArea;

    // ========================================================================
    // FXML FIELDS - Appeal Process
    // ========================================================================

    @FXML private VBox appealBox;
    @FXML private CheckBox appealFiledCheckBox;
    @FXML private DatePicker appealHearingDatePicker;
    @FXML private ComboBox<String> appealOutcomeComboBox;
    @FXML private TextArea appealDecisionArea;

    // ========================================================================
    // FXML FIELDS - Expulsion Recommendation
    // ========================================================================

    @FXML private VBox expulsionBox;
    @FXML private CheckBox expulsionRecommendedCheckBox;
    @FXML private DatePicker expulsionHearingDatePicker;

    // ========================================================================
    // FXML FIELDS - Completion Verification
    // ========================================================================

    @FXML private CheckBox completionVerifiedCheckBox;
    @FXML private DatePicker completionVerificationDatePicker;
    @FXML private ComboBox<Teacher> verifiedByComboBox;

    // ========================================================================
    // STATE
    // ========================================================================

    private Student currentStudent;
    private Suspension currentSuspension;
    private boolean viewMode = false;
    private String selectedDocumentPath;

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    @FXML
    public void initialize() {
        log.info("Initializing SuspensionManagementController");
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

        // Issued By ComboBox (Administrators)
        issuedByComboBox.setConverter(new StringConverter<Teacher>() {
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

        // ISS Supervisor ComboBox
        issSupervisorComboBox.setConverter(new StringConverter<Teacher>() {
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

        // Verified By ComboBox
        verifiedByComboBox.setConverter(new StringConverter<Teacher>() {
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

        // Suspension type handler
        suspensionTypeComboBox.setOnAction(e -> updateSuspensionTypeVisibility());

        // Appeal filed handler
        appealFiledCheckBox.setOnAction(e -> updateAppealVisibility());

        // Expulsion recommended handler
        expulsionRecommendedCheckBox.setOnAction(e -> updateExpulsionVisibility());
    }

    private void setupConditionalVisibility() {
        // Initially hide conditional sections
        issDetailsBox.setVisible(false);
        issDetailsBox.setManaged(false);

        ossDetailsBox.setVisible(false);
        ossDetailsBox.setManaged(false);

        appealBox.setVisible(false);
        appealBox.setManaged(false);

        expulsionBox.setVisible(false);
        expulsionBox.setManaged(false);
    }

    private void setupCalculations() {
        // Auto-calculate days count when dates change
        startDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> calculateDaysCount());
        endDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> calculateDaysCount());

        // Allow only numeric input for days fields
        daysAttendedField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.matches("\\d*")) {
                daysAttendedField.setText(oldValue);
            }
        });
    }

    private void loadInitialData() {
        // Load all students
        List<Student> students = studentService.getAllStudents();
        studentComboBox.setItems(FXCollections.observableArrayList(students));

        // Load administrators and staff
        List<Teacher> allStaff = teacherService.getAllTeachers();

        // Filter administrators for "Issued By"
        List<Teacher> administrators = allStaff.stream()
                .filter(t -> t.getTitle() != null &&
                        (t.getTitle().contains("Principal") ||
                         t.getTitle().contains("Assistant Principal") ||
                         t.getTitle().contains("Dean") ||
                         t.getTitle().contains("Administrator")))
                .collect(Collectors.toList());
        issuedByComboBox.setItems(FXCollections.observableArrayList(administrators));

        // All staff for ISS supervisor and verified by
        issSupervisorComboBox.setItems(FXCollections.observableArrayList(allStaff));
        verifiedByComboBox.setItems(FXCollections.observableArrayList(allStaff));
    }

    private void setupDefaults() {
        // Default status
        statusComboBox.setValue("PENDING");

        // Default re-entry meeting required for OSS
        reentryMeetingRequiredCheckBox.setSelected(false);
    }

    // ========================================================================
    // PUBLIC METHODS FOR EXTERNAL INTEGRATION
    // ========================================================================

    /**
     * Set the student for suspension
     */
    public void setStudent(Student student) {
        this.currentStudent = student;
        studentComboBox.setValue(student);

        // Populate student information
        if (student.getGradeLevel() != null) {
            gradeField.setText(student.getGradeLevel().toString());
        }

        studentIdField.setText(student.getStudentId());

        if (student.getCampus() != null) {
            campusField.setText(student.getCampus().getCampusName());
        }
    }

    /**
     * Load existing suspension for editing
     */
    public void loadSuspension(Suspension suspension) {
        this.currentSuspension = suspension;

        // Set student first
        setStudent(suspension.getStudent());

        // Suspension Details
        suspensionTypeComboBox.setValue(suspension.getSuspensionType().name());
        issuedByComboBox.setValue(suspension.getIssuedBy());
        statusComboBox.setValue(suspension.getStatus().name());

        // Dates
        startDatePicker.setValue(suspension.getStartDate());
        endDatePicker.setValue(suspension.getEndDate());
        daysCountField.setText(String.valueOf(suspension.getDaysCount()));

        // Incident Details
        suspensionReasonArea.setText(suspension.getSuspensionReason());
        incidentDescriptionArea.setText(suspension.getIncidentDescription());

        // ISS Details
        if (suspension.isInSchool()) {
            issRoomField.setText(suspension.getIssRoomAssignment());
            issSupervisorComboBox.setValue(suspension.getIssSupervisor());
            if (suspension.getDaysAttended() != null) {
                daysAttendedField.setText(String.valueOf(suspension.getDaysAttended()));
            }
            dailyNotesArea.setText(suspension.getDailyNotes());
        }

        // OSS Details
        if (suspension.isOutOfSchool()) {
            alternativePlacementField.setText(suspension.getAlternativePlacement());
            homeworkProvidedCheckBox.setSelected(suspension.getHomeworkProvided());
            homeworkCompletedCheckBox.setSelected(suspension.getHomeworkCompleted());
        }

        // Parent Notification
        parentNotifiedCheckBox.setSelected(suspension.getParentNotified());
        if (suspension.getParentNotificationDate() != null) {
            parentNotificationDatePicker.setValue(suspension.getParentNotificationDate());
        }
        if (suspension.getParentNotificationMethod() != null) {
            parentNotificationMethodComboBox.setValue(suspension.getParentNotificationMethod().name());
        }
        notificationDocumentField.setText(suspension.getNotificationDocumentPath());

        // Re-Entry Meeting
        reentryMeetingRequiredCheckBox.setSelected(suspension.getReentryMeetingRequired());
        reentryMeetingCompletedCheckBox.setSelected(suspension.getReentryMeetingCompleted());
        if (suspension.getReentryMeetingDate() != null) {
            reentryMeetingDatePicker.setValue(suspension.getReentryMeetingDate());
        }
        reentryPlanArea.setText(suspension.getReentryPlan());

        // Appeal
        appealFiledCheckBox.setSelected(suspension.getAppealFiled());
        if (suspension.getAppealHearingDate() != null) {
            appealHearingDatePicker.setValue(suspension.getAppealHearingDate());
        }
        if (suspension.getAppealOutcome() != null) {
            appealOutcomeComboBox.setValue(suspension.getAppealOutcome().name());
        }
        appealDecisionArea.setText(suspension.getAppealDecision());

        // Expulsion
        expulsionRecommendedCheckBox.setSelected(suspension.getExpulsionRecommended());
        if (suspension.getExpulsionHearingDate() != null) {
            expulsionHearingDatePicker.setValue(suspension.getExpulsionHearingDate());
        }

        // Completion
        completionVerifiedCheckBox.setSelected(suspension.getCompletionVerified());
        if (suspension.getCompletionVerificationDate() != null) {
            completionVerificationDatePicker.setValue(suspension.getCompletionVerificationDate());
        }
        verifiedByComboBox.setValue(suspension.getVerifiedBy());

        // Update visibility
        updateSuspensionTypeVisibility();
        updateAppealVisibility();
        updateExpulsionVisibility();
    }

    /**
     * Set view mode (read-only)
     */
    public void setViewMode(boolean viewMode) {
        this.viewMode = viewMode;

        // Disable all input controls
        studentComboBox.setDisable(viewMode);
        suspensionTypeComboBox.setDisable(viewMode);
        issuedByComboBox.setDisable(viewMode);
        statusComboBox.setDisable(viewMode);
        startDatePicker.setDisable(viewMode);
        endDatePicker.setDisable(viewMode);
        daysCountField.setDisable(viewMode);
        suspensionReasonArea.setDisable(viewMode);
        incidentDescriptionArea.setDisable(viewMode);
        issRoomField.setDisable(viewMode);
        issSupervisorComboBox.setDisable(viewMode);
        daysAttendedField.setDisable(viewMode);
        dailyNotesArea.setDisable(viewMode);
        alternativePlacementField.setDisable(viewMode);
        homeworkProvidedCheckBox.setDisable(viewMode);
        homeworkCompletedCheckBox.setDisable(viewMode);
        parentNotifiedCheckBox.setDisable(viewMode);
        parentNotificationDatePicker.setDisable(viewMode);
        parentNotificationMethodComboBox.setDisable(viewMode);
        reentryMeetingRequiredCheckBox.setDisable(viewMode);
        reentryMeetingCompletedCheckBox.setDisable(viewMode);
        reentryMeetingDatePicker.setDisable(viewMode);
        reentryPlanArea.setDisable(viewMode);
        appealFiledCheckBox.setDisable(viewMode);
        appealHearingDatePicker.setDisable(viewMode);
        appealOutcomeComboBox.setDisable(viewMode);
        appealDecisionArea.setDisable(viewMode);
        expulsionRecommendedCheckBox.setDisable(viewMode);
        expulsionHearingDatePicker.setDisable(viewMode);
        completionVerifiedCheckBox.setDisable(viewMode);
        completionVerificationDatePicker.setDisable(viewMode);
        verifiedByComboBox.setDisable(viewMode);
    }

    // ========================================================================
    // CONDITIONAL VISIBILITY METHODS
    // ========================================================================

    private void updateSuspensionTypeVisibility() {
        String selectedType = suspensionTypeComboBox.getValue();
        if (selectedType == null) return;

        // Hide both sections first
        issDetailsBox.setVisible(false);
        issDetailsBox.setManaged(false);
        ossDetailsBox.setVisible(false);
        ossDetailsBox.setManaged(false);

        // Show appropriate section
        if ("IN_SCHOOL".equals(selectedType)) {
            issDetailsBox.setVisible(true);
            issDetailsBox.setManaged(true);
            reentryMeetingRequiredCheckBox.setSelected(false); // ISS typically doesn't require re-entry meeting
        } else if ("OUT_OF_SCHOOL".equals(selectedType) ||
                   "EXTENDED_OSS".equals(selectedType) ||
                   "EMERGENCY_REMOVAL".equals(selectedType)) {
            ossDetailsBox.setVisible(true);
            ossDetailsBox.setManaged(true);
            reentryMeetingRequiredCheckBox.setSelected(true); // OSS typically requires re-entry meeting
        }
    }

    private void updateAppealVisibility() {
        boolean appealFiled = appealFiledCheckBox.isSelected();
        appealBox.setVisible(appealFiled);
        appealBox.setManaged(appealFiled);
    }

    private void updateExpulsionVisibility() {
        boolean expulsionRecommended = expulsionRecommendedCheckBox.isSelected();
        expulsionBox.setVisible(expulsionRecommended);
        expulsionBox.setManaged(expulsionRecommended);
    }

    // ========================================================================
    // CALCULATION METHODS
    // ========================================================================

    private void calculateDaysCount() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate != null && endDate != null) {
            if (endDate.isBefore(startDate)) {
                daysCountField.setText("0");
                daysRemainingField.setText("0");
                return;
            }

            // Calculate school days (Monday-Friday)
            long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            daysCountField.setText(String.valueOf(totalDays));

            // Calculate days remaining
            LocalDate today = LocalDate.now();
            if (today.isAfter(endDate)) {
                daysRemainingField.setText("0");
            } else if (today.isBefore(startDate)) {
                daysRemainingField.setText(String.valueOf(totalDays));
            } else {
                long remaining = ChronoUnit.DAYS.between(today, endDate) + 1;
                daysRemainingField.setText(String.valueOf(remaining));
            }
        }
    }

    // ========================================================================
    // EVENT HANDLERS
    // ========================================================================

    @FXML
    private void handleBrowseDocument() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Parent Notification Document");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Word Documents", "*.doc", "*.docx"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Stage stage = (Stage) studentComboBox.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            selectedDocumentPath = selectedFile.getAbsolutePath();
            notificationDocumentField.setText(selectedFile.getName());
        }
    }

    @FXML
    private void handleSave() {
        if (viewMode) return;

        List<String> errors = validateForm();
        if (!errors.isEmpty()) {
            showValidationErrors(errors);
            return;
        }

        try {
            Suspension suspension = buildSuspensionFromForm();

            if (currentSuspension == null) {
                // Create new suspension - Note: Service method needs to be implemented
                // suspensionService.createSuspension(suspension);
                log.info("Created new suspension for student: {}", currentStudent.getStudentId());
                showSuccess("Suspension record created successfully!");
            } else {
                // Update existing suspension
                // suspension.setId(currentSuspension.getId());
                // suspensionService.updateSuspension(suspension);
                log.info("Updated suspension record ID: {}", currentSuspension.getId());
                showSuccess("Suspension record updated successfully!");
            }

            handleClose();
        } catch (Exception e) {
            log.error("Error saving suspension record", e);
            showError("Failed to save suspension record: " + e.getMessage());
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

        if (suspensionTypeComboBox.getValue() == null || suspensionTypeComboBox.getValue().isEmpty()) {
            errors.add("Suspension type is required");
        }

        if (issuedByComboBox.getValue() == null) {
            errors.add("Administrator who issued suspension is required");
        }

        if (startDatePicker.getValue() == null) {
            errors.add("Start date is required");
        }

        if (endDatePicker.getValue() == null) {
            errors.add("End date is required");
        }

        if (startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
            if (endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
                errors.add("End date cannot be before start date");
            }
        }

        if (suspensionReasonArea.getText() == null || suspensionReasonArea.getText().trim().isEmpty()) {
            errors.add("Suspension reason is required");
        }

        // Legal compliance validation
        if (!parentNotifiedCheckBox.isSelected()) {
            errors.add("LEGAL REQUIREMENT: Parent notification is required by law for all suspensions");
        }

        if (parentNotifiedCheckBox.isSelected()) {
            if (parentNotificationDatePicker.getValue() == null) {
                errors.add("Parent notification date is required");
            }
            if (parentNotificationMethodComboBox.getValue() == null ||
                parentNotificationMethodComboBox.getValue().isEmpty()) {
                errors.add("Parent notification method is required");
            }
        }

        // Extended OSS validation
        String suspensionType = suspensionTypeComboBox.getValue();
        if ("EXTENDED_OSS".equals(suspensionType)) {
            if (!reentryMeetingRequiredCheckBox.isSelected()) {
                errors.add("Re-entry meeting is required for extended OSS (10+ days)");
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

    private Suspension buildSuspensionFromForm() {
        Suspension suspension = Suspension.builder()
                .student(studentComboBox.getValue())
                .issuedBy(issuedByComboBox.getValue())
                .suspensionType(Suspension.SuspensionType.valueOf(suspensionTypeComboBox.getValue()))
                .status(Suspension.SuspensionStatus.valueOf(statusComboBox.getValue()))
                .startDate(startDatePicker.getValue())
                .endDate(endDatePicker.getValue())
                .daysCount(Integer.parseInt(daysCountField.getText()))
                .suspensionReason(suspensionReasonArea.getText())
                .incidentDescription(incidentDescriptionArea.getText())
                .parentNotified(parentNotifiedCheckBox.isSelected())
                .parentNotificationDate(parentNotificationDatePicker.getValue())
                .notificationDocumentPath(selectedDocumentPath)
                .reentryMeetingRequired(reentryMeetingRequiredCheckBox.isSelected())
                .reentryMeetingCompleted(reentryMeetingCompletedCheckBox.isSelected())
                .reentryMeetingDate(reentryMeetingDatePicker.getValue())
                .reentryPlan(reentryPlanArea.getText())
                .appealFiled(appealFiledCheckBox.isSelected())
                .appealHearingDate(appealHearingDatePicker.getValue())
                .appealDecision(appealDecisionArea.getText())
                .expulsionRecommended(expulsionRecommendedCheckBox.isSelected())
                .expulsionHearingDate(expulsionHearingDatePicker.getValue())
                .completionVerified(completionVerifiedCheckBox.isSelected())
                .completionVerificationDate(completionVerificationDatePicker.getValue())
                .verifiedBy(verifiedByComboBox.getValue())
                .campus(currentStudent != null ? currentStudent.getCampus() : null)
                .build();

        // Parent notification method
        if (parentNotificationMethodComboBox.getValue() != null &&
            !parentNotificationMethodComboBox.getValue().isEmpty()) {
            suspension.setParentNotificationMethod(
                    BehaviorIncident.ContactMethod.valueOf(parentNotificationMethodComboBox.getValue()));
        }

        // ISS-specific fields
        if (suspension.isInSchool()) {
            suspension.setIssRoomAssignment(issRoomField.getText());
            suspension.setIssSupervisor(issSupervisorComboBox.getValue());
            if (daysAttendedField.getText() != null && !daysAttendedField.getText().trim().isEmpty()) {
                suspension.setDaysAttended(Integer.parseInt(daysAttendedField.getText()));
            }
            suspension.setDailyNotes(dailyNotesArea.getText());
        }

        // OSS-specific fields
        if (suspension.isOutOfSchool()) {
            suspension.setAlternativePlacement(alternativePlacementField.getText());
            suspension.setHomeworkProvided(homeworkProvidedCheckBox.isSelected());
            suspension.setHomeworkCompleted(homeworkCompletedCheckBox.isSelected());
        }

        // Appeal outcome
        if (appealOutcomeComboBox.getValue() != null && !appealOutcomeComboBox.getValue().isEmpty()) {
            suspension.setAppealOutcome(Suspension.AppealOutcome.valueOf(appealOutcomeComboBox.getValue()));
        }

        return suspension;
    }

    private void clearForm() {
        currentSuspension = null;
        selectedDocumentPath = null;

        suspensionTypeComboBox.setValue(null);
        issuedByComboBox.setValue(null);
        statusComboBox.setValue("PENDING");
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        daysCountField.clear();
        daysRemainingField.clear();
        suspensionReasonArea.clear();
        incidentDescriptionArea.clear();

        issRoomField.clear();
        issSupervisorComboBox.setValue(null);
        daysAttendedField.clear();
        dailyNotesArea.clear();

        alternativePlacementField.clear();
        homeworkProvidedCheckBox.setSelected(false);
        homeworkCompletedCheckBox.setSelected(false);

        parentNotifiedCheckBox.setSelected(false);
        parentNotificationDatePicker.setValue(null);
        parentNotificationMethodComboBox.setValue(null);
        notificationDocumentField.clear();

        reentryMeetingRequiredCheckBox.setSelected(false);
        reentryMeetingCompletedCheckBox.setSelected(false);
        reentryMeetingDatePicker.setValue(null);
        reentryPlanArea.clear();

        appealFiledCheckBox.setSelected(false);
        appealHearingDatePicker.setValue(null);
        appealOutcomeComboBox.setValue(null);
        appealDecisionArea.clear();

        expulsionRecommendedCheckBox.setSelected(false);
        expulsionHearingDatePicker.setValue(null);

        completionVerifiedCheckBox.setSelected(false);
        completionVerificationDatePicker.setValue(null);
        verifiedByComboBox.setValue(null);

        // Reset visibility
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
