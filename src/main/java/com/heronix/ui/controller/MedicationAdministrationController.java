package com.heronix.ui.controller;

import com.heronix.model.domain.Medication;
import com.heronix.model.domain.MedicationAdministration;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.Teacher;
import com.heronix.service.MedicationService;
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
 * Controller for Medication Administration Form
 * Handles legal documentation of medication administration with full compliance tracking
 */
@Component
public class MedicationAdministrationController {

    private static final Logger logger = LoggerFactory.getLogger(MedicationAdministrationController.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    private MedicationService medicationService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    // Student & Medication Selection
    @FXML private ComboBox<Student> studentComboBox;
    @FXML private TextField gradeField;
    @FXML private ComboBox<Medication> medicationComboBox;
    @FXML private TextArea medicationDetailsTextArea;

    // Administration Details
    @FXML private DatePicker administrationDatePicker;
    @FXML private TextField administrationTimeField;
    @FXML private TextField doseGivenField;
    @FXML private ComboBox<String> administrationRouteComboBox;
    @FXML private ComboBox<String> administrationReasonComboBox;
    @FXML private VBox reasonDetailsBox;
    @FXML private TextField reasonDetailsField;

    // Administrator Information
    @FXML private ComboBox<Teacher> administratorComboBox;
    @FXML private TextField administratorTitleField;
    @FXML private ComboBox<Teacher> witnessComboBox;

    // Safety Verification
    @FXML private CheckBox studentIdentifiedCheckBox;
    @FXML private CheckBox medicationVerifiedCheckBox;
    @FXML private CheckBox dosageVerifiedCheckBox;
    @FXML private CheckBox studentConsentVerifiedCheckBox;
    @FXML private CheckBox routeVerifiedCheckBox;
    @FXML private CheckBox refusedByStudentCheckBox;
    @FXML private VBox refusalReasonBox;
    @FXML private TextArea refusalReasonTextArea;

    // Student Response
    @FXML private ComboBox<String> studentResponseComboBox;
    @FXML private TextArea responseNotesTextArea;
    @FXML private CheckBox adverseReactionCheckBox;
    @FXML private VBox adverseReactionBox;
    @FXML private ComboBox<String> adverseReactionSeverityComboBox;
    @FXML private TextArea adverseReactionDescriptionTextArea;
    @FXML private CheckBox parentNotifiedOfReactionCheckBox;
    @FXML private CheckBox physicianNotifiedOfReactionCheckBox;

    // Parent Notification
    @FXML private CheckBox parentNotificationRequiredCheckBox;
    @FXML private CheckBox parentNotifiedCheckBox;

    // Inventory
    @FXML private CheckBox inventoryUpdatedCheckBox;
    @FXML private TextField remainingQuantityField;

    // Notes & Documentation
    @FXML private TextArea notesTextArea;
    @FXML private TextField signatureField;

    // Buttons
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label statusIndicator;

    private MedicationAdministration currentAdministration;

    @FXML
    public void initialize() {
        logger.info("Initializing MedicationAdministrationController");

        setupComboBoxes();
        setupConditionalVisibility();
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

        // Medication ComboBox
        medicationComboBox.setConverter(new javafx.util.StringConverter<Medication>() {
            @Override
            public String toString(Medication med) {
                return med != null ? med.getMedicationName() + " - " + med.getDosage() : "";
            }

            @Override
            public Medication fromString(String string) {
                return null;
            }
        });

        // Administrator ComboBox
        administratorComboBox.setConverter(new javafx.util.StringConverter<Teacher>() {
            @Override
            public String toString(Teacher teacher) {
                return teacher != null ? teacher.getFirstName() + " " + teacher.getLastName() : "";
            }

            @Override
            public Teacher fromString(String string) {
                return null;
            }
        });

        // Witness ComboBox
        witnessComboBox.setConverter(new javafx.util.StringConverter<Teacher>() {
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
                loadMedicationsForStudent(student);
            }
        });

        // Auto-populate medication details
        medicationComboBox.setOnAction(e -> {
            Medication medication = medicationComboBox.getValue();
            if (medication != null) {
                displayMedicationDetails(medication);
                // Pre-fill dose from medication record
                doseGivenField.setText(medication.getDosage());
            }
        });

        // Show/hide PRN reason details
        administrationReasonComboBox.setOnAction(e -> {
            String reason = administrationReasonComboBox.getValue();
            boolean isPRN = reason != null && (reason.startsWith("PRN_") || reason.equals("EMERGENCY"));
            reasonDetailsBox.setVisible(isPRN);
            reasonDetailsBox.setManaged(isPRN);
        });

        // Auto-populate administrator title
        administratorComboBox.setOnAction(e -> {
            Teacher admin = administratorComboBox.getValue();
            if (admin != null && (administratorTitleField.getText() == null ||
                administratorTitleField.getText().isEmpty())) {
                administratorTitleField.setText("School Nurse"); // Default
            }
        });

        // Show/hide refusal reason
        refusedByStudentCheckBox.setOnAction(e -> {
            boolean refused = refusedByStudentCheckBox.isSelected();
            refusalReasonBox.setVisible(refused);
            refusalReasonBox.setManaged(refused);

            // If student refused, uncheck all verification boxes
            if (refused) {
                studentIdentifiedCheckBox.setSelected(false);
                medicationVerifiedCheckBox.setSelected(false);
                dosageVerifiedCheckBox.setSelected(false);
                studentConsentVerifiedCheckBox.setSelected(false);
                routeVerifiedCheckBox.setSelected(false);
            }
        });

        // Show/hide adverse reaction details
        adverseReactionCheckBox.setOnAction(e -> {
            boolean hasReaction = adverseReactionCheckBox.isSelected();
            adverseReactionBox.setVisible(hasReaction);
            adverseReactionBox.setManaged(hasReaction);

            if (hasReaction) {
                studentResponseComboBox.setValue("ADVERSE_REACTION");
                parentNotificationRequiredCheckBox.setSelected(true);
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
            administratorComboBox.setItems(javafx.collections.FXCollections.observableArrayList(teachers));
            witnessComboBox.setItems(javafx.collections.FXCollections.observableArrayList(teachers));

            // Set defaults
            if (currentAdministration == null) {
                administrationDatePicker.setValue(LocalDate.now());
                administrationTimeField.setText(LocalTime.now().format(TIME_FORMATTER));
            }

        } catch (Exception e) {
            logger.error("Error loading initial data", e);
            showError("Error loading data: " + e.getMessage());
        }
    }

    private void loadMedicationsForStudent(Student student) {
        try {
            List<Medication> medications = medicationService.getActiveMedicationsForStudent(student);
            medicationComboBox.setItems(javafx.collections.FXCollections.observableArrayList(medications));

            if (medications.isEmpty()) {
                showWarning("No active medications found for this student.\n\n" +
                    "Please ensure medication orders are on file before administering.");
            }

        } catch (Exception e) {
            logger.error("Error loading medications for student", e);
            showError("Error loading medications: " + e.getMessage());
        }
    }

    private void displayMedicationDetails(Medication medication) {
        StringBuilder details = new StringBuilder();
        details.append("Medication: ").append(medication.getMedicationName()).append("\n");
        details.append("Type: ").append(medication.getMedicationType()).append("\n");
        details.append("Dosage: ").append(medication.getDosage()).append("\n");
        details.append("Form: ").append(medication.getDosageForm()).append("\n");
        details.append("Purpose: ").append(medication.getPurpose()).append("\n");

        if (medication.getInstructions() != null && !medication.getInstructions().isEmpty()) {
            details.append("Instructions: ").append(medication.getInstructions()).append("\n");
        }

        if (medication.getAdministrationTime() != null) {
            details.append("Scheduled Time: ").append(
                medication.getAdministrationTime().format(TIME_FORMATTER)).append("\n");
        }

        if (medication.getAsNeeded()) {
            details.append("⚠ PRN (As Needed) - Requires documented reason\n");
        }

        medicationDetailsTextArea.setText(details.toString());
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        // Critical safety confirmation
        if (!confirmAdministration()) {
            return;
        }

        try {
            MedicationAdministration administration = buildAdministrationFromForm();

            // Save through service
            MedicationAdministration saved = medicationService.recordAdministration(administration);

            showSuccess("Medication administration recorded successfully");

            // Log critical event
            logger.info("Medication administered: {} to student {} by {}",
                administration.getMedication().getMedicationName(),
                administration.getStudent().getStudentId(),
                administration.getAdministratorName());

            closeWindow();

        } catch (Exception e) {
            logger.error("Error saving medication administration", e);
            showError("Error recording administration: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Cancel Medication Administration");
        confirmation.setHeaderText("Discard medication administration record?");
        confirmation.setContentText("All entered information will be lost.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                closeWindow();
            }
        });
    }

    private boolean validateForm() {
        List<String> errors = new ArrayList<>();

        if (studentComboBox.getValue() == null) {
            errors.add("Student is required");
        }

        if (medicationComboBox.getValue() == null) {
            errors.add("Medication is required");
        }

        if (administrationDatePicker.getValue() == null) {
            errors.add("Administration date is required");
        }

        if (administrationTimeField.getText() == null || administrationTimeField.getText().isEmpty()) {
            errors.add("Administration time is required");
        } else {
            try {
                LocalTime.parse(administrationTimeField.getText(), TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                errors.add("Administration time must be in HH:MM format");
            }
        }

        if (doseGivenField.getText() == null || doseGivenField.getText().trim().isEmpty()) {
            errors.add("Dose given is required");
        }

        if (administrationRouteComboBox.getValue() == null || administrationRouteComboBox.getValue().isEmpty()) {
            errors.add("Administration route is required");
        }

        if (administrationReasonComboBox.getValue() == null || administrationReasonComboBox.getValue().isEmpty()) {
            errors.add("Administration reason is required");
        }

        String reason = administrationReasonComboBox.getValue();
        if (reason != null && (reason.startsWith("PRN_") || reason.equals("EMERGENCY"))) {
            if (reasonDetailsField.getText() == null || reasonDetailsField.getText().trim().isEmpty()) {
                errors.add("PRN/Emergency reason details are required");
            }
        }

        if (administratorComboBox.getValue() == null) {
            errors.add("Administrator is required");
        }

        if (administratorTitleField.getText() == null || administratorTitleField.getText().trim().isEmpty()) {
            errors.add("Administrator title is required");
        }

        // Safety verification checks
        if (!refusedByStudentCheckBox.isSelected()) {
            if (!studentIdentifiedCheckBox.isSelected()) {
                errors.add("CRITICAL: Student identity must be verified before medication administration");
            }
            if (!medicationVerifiedCheckBox.isSelected()) {
                errors.add("CRITICAL: Medication must be verified before administration");
            }
            if (!dosageVerifiedCheckBox.isSelected()) {
                errors.add("CRITICAL: Dosage must be verified before administration");
            }
            if (!studentConsentVerifiedCheckBox.isSelected()) {
                errors.add("CRITICAL: Administration time/schedule must be verified");
            }
            if (!routeVerifiedCheckBox.isSelected()) {
                errors.add("CRITICAL: Administration route must be verified");
            }
        } else {
            if (refusalReasonTextArea.getText() == null || refusalReasonTextArea.getText().trim().isEmpty()) {
                errors.add("Refusal reason is required when student refuses medication");
            }
        }

        if (adverseReactionCheckBox.isSelected()) {
            if (adverseReactionSeverityComboBox.getValue() == null ||
                adverseReactionSeverityComboBox.getValue().isEmpty()) {
                errors.add("Adverse reaction severity is required");
            }
            if (adverseReactionDescriptionTextArea.getText() == null ||
                adverseReactionDescriptionTextArea.getText().trim().isEmpty()) {
                errors.add("Adverse reaction description is required");
            }
        }

        if (signatureField.getText() == null || signatureField.getText().trim().isEmpty()) {
            errors.add("Administrator signature/initials required");
        }

        if (!errors.isEmpty()) {
            showValidationErrors(errors);
            return false;
        }

        return true;
    }

    private boolean confirmAdministration() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Medication Administration");
        alert.setHeaderText("LEGAL CONFIRMATION REQUIRED");

        Student student = studentComboBox.getValue();
        Medication medication = medicationComboBox.getValue();

        String content = String.format(
            "You are about to record medication administration:\n\n" +
            "Student: %s %s\n" +
            "Medication: %s\n" +
            "Dose: %s\n" +
            "Route: %s\n" +
            "Date/Time: %s %s\n\n" +
            "Safety Verifications Completed:\n" +
            "✓ Student Identity: %s\n" +
            "✓ Medication Verified: %s\n" +
            "✓ Dosage Verified: %s\n" +
            "✓ Time Verified: %s\n" +
            "✓ Route Verified: %s\n\n" +
            "This is a legal document that becomes part of the student's health record.\n\n" +
            "Confirm that all information is accurate?",
            student.getFirstName(), student.getLastName(),
            medication.getMedicationName(),
            doseGivenField.getText(),
            administrationRouteComboBox.getValue(),
            administrationDatePicker.getValue(),
            administrationTimeField.getText(),
            studentIdentifiedCheckBox.isSelected() ? "YES" : "NO",
            medicationVerifiedCheckBox.isSelected() ? "YES" : "NO",
            dosageVerifiedCheckBox.isSelected() ? "YES" : "NO",
            studentConsentVerifiedCheckBox.isSelected() ? "YES" : "NO",
            routeVerifiedCheckBox.isSelected() ? "YES" : "NO"
        );

        alert.setContentText(content);

        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private MedicationAdministration buildAdministrationFromForm() {
        MedicationAdministration administration = new MedicationAdministration();

        // Student & Medication
        administration.setMedication(medicationComboBox.getValue());
        administration.setStudent(studentComboBox.getValue());

        // Administration Details
        administration.setAdministrationDate(administrationDatePicker.getValue());
        administration.setAdministrationTime(LocalTime.parse(administrationTimeField.getText(), TIME_FORMATTER));
        administration.setAdministrationTimestamp(LocalDateTime.now());
        administration.setDoseGiven(doseGivenField.getText());
        administration.setAdministrationRoute(
            MedicationAdministration.AdministrationRoute.valueOf(administrationRouteComboBox.getValue())
        );
        administration.setAdministrationReason(
            MedicationAdministration.AdministrationReason.valueOf(administrationReasonComboBox.getValue())
        );
        administration.setReasonDetails(reasonDetailsField.getText());

        // Administrator Information
        Teacher administrator = administratorComboBox.getValue();
        administration.setAdministeredByStaffId(administrator.getId());
        administration.setAdministratorName(administrator.getFirstName() + " " + administrator.getLastName());
        administration.setAdministratorTitle(administratorTitleField.getText());

        if (witnessComboBox.getValue() != null) {
            Teacher witness = witnessComboBox.getValue();
            administration.setWitnessStaffId(witness.getId());
            administration.setWitnessName(witness.getFirstName() + " " + witness.getLastName());
        }

        // Safety Verification
        administration.setStudentIdentified(studentIdentifiedCheckBox.isSelected());
        administration.setMedicationVerified(medicationVerifiedCheckBox.isSelected());
        administration.setDosageVerified(dosageVerifiedCheckBox.isSelected());
        administration.setStudentConsentVerified(studentConsentVerifiedCheckBox.isSelected());
        administration.setRefusedByStudent(refusedByStudentCheckBox.isSelected());
        administration.setRefusalReason(refusalReasonTextArea.getText());

        // Student Response
        if (studentResponseComboBox.getValue() != null && !studentResponseComboBox.getValue().isEmpty()) {
            administration.setStudentResponse(
                MedicationAdministration.StudentResponse.valueOf(studentResponseComboBox.getValue())
            );
        }
        administration.setResponseNotes(responseNotesTextArea.getText());

        // Adverse Reaction
        administration.setAdverseReactionObserved(adverseReactionCheckBox.isSelected());
        if (adverseReactionCheckBox.isSelected()) {
            if (adverseReactionSeverityComboBox.getValue() != null &&
                !adverseReactionSeverityComboBox.getValue().isEmpty()) {
                administration.setAdverseReactionSeverity(
                    MedicationAdministration.AdverseReactionSeverity.valueOf(adverseReactionSeverityComboBox.getValue())
                );
            }
            administration.setAdverseReactionDescription(adverseReactionDescriptionTextArea.getText());
            administration.setParentNotifiedOfReaction(parentNotifiedOfReactionCheckBox.isSelected());
            administration.setPhysicianNotifiedOfReaction(physicianNotifiedOfReactionCheckBox.isSelected());
        }

        // Parent Notification
        administration.setParentNotificationRequired(parentNotificationRequiredCheckBox.isSelected());
        administration.setParentNotified(parentNotifiedCheckBox.isSelected());
        if (parentNotifiedCheckBox.isSelected()) {
            administration.setParentNotificationTime(LocalDateTime.now());
        }

        // Inventory
        administration.setInventoryUpdated(inventoryUpdatedCheckBox.isSelected());
        if (remainingQuantityField.getText() != null && !remainingQuantityField.getText().isEmpty()) {
            try {
                administration.setRemainingQuantity(Integer.parseInt(remainingQuantityField.getText()));
            } catch (NumberFormatException e) {
                logger.warn("Invalid remaining quantity format: " + remainingQuantityField.getText());
            }
        }

        // Notes & Documentation
        administration.setNotes(notesTextArea.getText());
        administration.setSignature(signatureField.getText());

        return administration;
    }

    private void showValidationErrors(List<String> errors) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText("Medication Administration Cannot Be Recorded");
        alert.setContentText(String.join("\n\n", errors));
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
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
