package com.heronix.ui.controller;

import com.heronix.model.domain.*;
import com.heronix.service.ImmunizationService;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for Immunization Tracking UI
 * Manages student immunization records, compliance tracking, and state requirement verification.
 * Supports vaccine administration documentation, exemptions, and compliance monitoring.
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Health Office Management System
 */
@Slf4j
@Component
public class ImmunizationTrackingController {

    @Autowired
    private ImmunizationService immunizationService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    // ========================================================================
    // FXML FIELDS - Student Selection
    // ========================================================================

    @FXML private ComboBox<Student> studentComboBox;
    @FXML private TextField gradeField;
    @FXML private TextField dateOfBirthField;
    @FXML private TextField ageField;

    // ========================================================================
    // FXML FIELDS - Vaccine Information
    // ========================================================================

    @FXML private ComboBox<String> vaccineTypeComboBox;
    @FXML private TextField vaccineNameField;
    @FXML private TextField doseNumberField;
    @FXML private TextField totalDosesField;
    @FXML private DatePicker administrationDatePicker;
    @FXML private CheckBox isBoosterCheckBox;

    // ========================================================================
    // FXML FIELDS - Provider Information
    // ========================================================================

    @FXML private ComboBox<Teacher> administeredByComboBox;
    @FXML private TextField administrationLocationField;
    @FXML private TextField lotNumberField;
    @FXML private TextField manufacturerField;
    @FXML private DatePicker expirationDatePicker;

    // ========================================================================
    // FXML FIELDS - Verification
    // ========================================================================

    @FXML private ComboBox<String> verificationMethodComboBox;
    @FXML private TextField verifiedByField;
    @FXML private DatePicker verificationDatePicker;
    @FXML private Button browseDocumentButton;
    @FXML private TextField documentationPathField;
    @FXML private CheckBox meetsStateRequirementCheckBox;

    // ========================================================================
    // FXML FIELDS - Exemptions
    // ========================================================================

    @FXML private CheckBox medicalExemptionCheckBox;
    @FXML private VBox medicalExemptionDetailsBox;
    @FXML private TextField medicalReasonField;
    @FXML private TextField medicalProviderField;
    @FXML private DatePicker medicalExemptionDatePicker;

    @FXML private CheckBox religiousExemptionCheckBox;
    @FXML private VBox religiousExemptionDetailsBox;
    @FXML private TextArea religiousExemptionStatementArea;
    @FXML private DatePicker religiousExemptionDatePicker;

    @FXML private CheckBox philosophicalExemptionCheckBox;
    @FXML private VBox philosophicalExemptionDetailsBox;
    @FXML private TextArea philosophicalExemptionStatementArea;
    @FXML private DatePicker philosophicalExemptionDatePicker;

    // ========================================================================
    // FXML FIELDS - Next Dose & Boosters
    // ========================================================================

    @FXML private DatePicker nextDoseDueDatePicker;
    @FXML private DatePicker boosterDueDatePicker;
    @FXML private TextArea scheduleNotesArea;

    // ========================================================================
    // FXML FIELDS - Adverse Reactions
    // ========================================================================

    @FXML private CheckBox adverseReactionObservedCheckBox;
    @FXML private VBox adverseReactionBox;
    @FXML private ComboBox<String> reactionSeverityComboBox;
    @FXML private TextArea reactionDescriptionArea;
    @FXML private CheckBox parentNotifiedOfReactionCheckBox;
    @FXML private CheckBox physicianNotifiedOfReactionCheckBox;

    // ========================================================================
    // FXML FIELDS - Notes
    // ========================================================================

    @FXML private TextArea notesArea;

    // ========================================================================
    // FXML FIELDS - Right Sidebar
    // ========================================================================

    @FXML private Label complianceStatusLabel;
    @FXML private ListView<String> requiredVaccinesListView;
    @FXML private ListView<String> incompleteSeriesListView;
    @FXML private ListView<String> overdueListView;
    @FXML private Label exemptionsLabel;

    // ========================================================================
    // STATE
    // ========================================================================

    private Student currentStudent;
    private Immunization currentImmunization;
    private boolean viewMode = false;
    private String selectedDocumentPath;

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    @FXML
    public void initialize() {
        log.info("Initializing ImmunizationTrackingController");
        setupComboBoxes();
        setupConditionalVisibility();
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

        // Administered By ComboBox (School nurses and authorized staff)
        administeredByComboBox.setConverter(new StringConverter<Teacher>() {
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

        // Vaccine Type selection handler - auto-populate total doses
        vaccineTypeComboBox.setOnAction(e -> {
            String selectedType = vaccineTypeComboBox.getValue();
            if (selectedType != null) {
                try {
                    Immunization.VaccineType vaccineType = Immunization.VaccineType.valueOf(selectedType);
                    totalDosesField.setText(String.valueOf(vaccineType.getTypicalDosesRequired()));
                    vaccineNameField.setText(vaccineType.getDisplayName());
                } catch (IllegalArgumentException ex) {
                    log.warn("Unknown vaccine type selected: {}", selectedType);
                }
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

    private void setupConditionalVisibility() {
        // Medical Exemption Details
        medicalExemptionCheckBox.setOnAction(e -> {
            boolean hasExemption = medicalExemptionCheckBox.isSelected();
            medicalExemptionDetailsBox.setVisible(hasExemption);
            medicalExemptionDetailsBox.setManaged(hasExemption);
        });

        // Religious Exemption Details
        religiousExemptionCheckBox.setOnAction(e -> {
            boolean hasExemption = religiousExemptionCheckBox.isSelected();
            religiousExemptionDetailsBox.setVisible(hasExemption);
            religiousExemptionDetailsBox.setManaged(hasExemption);
        });

        // Philosophical Exemption Details
        philosophicalExemptionCheckBox.setOnAction(e -> {
            boolean hasExemption = philosophicalExemptionCheckBox.isSelected();
            philosophicalExemptionDetailsBox.setVisible(hasExemption);
            philosophicalExemptionDetailsBox.setManaged(hasExemption);
        });

        // Adverse Reaction Details
        adverseReactionObservedCheckBox.setOnAction(e -> {
            boolean hasReaction = adverseReactionObservedCheckBox.isSelected();
            adverseReactionBox.setVisible(hasReaction);
            adverseReactionBox.setManaged(hasReaction);
        });
    }

    private void setupValidation() {
        // Allow only numeric input for dose numbers
        doseNumberField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.matches("\\d*")) {
                doseNumberField.setText(oldValue);
            }
        });

        totalDosesField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.matches("\\d*")) {
                totalDosesField.setText(oldValue);
            }
        });
    }

    private void loadInitialData() {
        // Load all students
        List<Student> students = studentService.getAllStudents();
        studentComboBox.setItems(FXCollections.observableArrayList(students));

        // Load school nurses and health staff
        List<Teacher> healthStaff = teacherService.findAll().stream()
                .filter(t -> t.getDepartment() != null &&
                        (t.getDepartment().contains("Nurse") ||
                         t.getDepartment().contains("Health") ||
                         t.getDepartment().contains("RN")))
                .collect(Collectors.toList());
        administeredByComboBox.setItems(FXCollections.observableArrayList(healthStaff));
    }

    private void setupDefaults() {
        // Default dates
        administrationDatePicker.setValue(LocalDate.now());
        verificationDatePicker.setValue(LocalDate.now());

        // Default state requirement checkbox
        meetsStateRequirementCheckBox.setSelected(true);
    }

    // ========================================================================
    // PUBLIC METHODS FOR EXTERNAL INTEGRATION
    // ========================================================================

    /**
     * Set the student for immunization tracking
     */
    public void setStudent(Student student) {
        this.currentStudent = student;
        studentComboBox.setValue(student);

        // Populate student information
        if (student.getGradeLevel() != null) {
            gradeField.setText(student.getGradeLevel().toString());
        }

        if (student.getDateOfBirth() != null) {
            dateOfBirthField.setText(student.getDateOfBirth().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            int age = LocalDate.now().getYear() - student.getDateOfBirth().getYear();
            ageField.setText(age + " years");
        }

        // Load student's complete immunization record in sidebar
        loadStudentImmunizationRecord(student);
    }

    /**
     * Load existing immunization for editing
     */
    public void loadImmunization(Immunization immunization) {
        this.currentImmunization = immunization;

        // Set student first
        setStudent(immunization.getStudent());

        // Vaccine Information
        vaccineTypeComboBox.setValue(immunization.getVaccineType().name());
        vaccineNameField.setText(immunization.getVaccineName());
        doseNumberField.setText(String.valueOf(immunization.getDoseNumber()));
        if (immunization.getTotalDosesRequired() != null) {
            totalDosesField.setText(String.valueOf(immunization.getTotalDosesRequired()));
        }
        administrationDatePicker.setValue(immunization.getAdministrationDate());
        isBoosterCheckBox.setSelected(immunization.getIsBooster());

        // Provider Information
        administrationLocationField.setText(immunization.getAdministrationLocation());
        lotNumberField.setText(immunization.getLotNumber());
        manufacturerField.setText(immunization.getManufacturer());
        if (immunization.getExpirationDate() != null) {
            expirationDatePicker.setValue(immunization.getExpirationDate());
        }

        // Verification
        if (immunization.getVerificationMethod() != null) {
            verificationMethodComboBox.setValue(immunization.getVerificationMethod().name());
        }
        verifiedByField.setText(immunization.getVerifiedBy());
        if (immunization.getVerificationDate() != null) {
            verificationDatePicker.setValue(immunization.getVerificationDate());
        }
        documentationPathField.setText(immunization.getDocumentationPath());
        meetsStateRequirementCheckBox.setSelected(immunization.getMeetsStateRequirement());

        // Exemptions
        medicalExemptionCheckBox.setSelected(immunization.getMedicalExemption());
        if (immunization.getMedicalExemption()) {
            medicalReasonField.setText(immunization.getMedicalExemptionReason());
            medicalProviderField.setText(immunization.getMedicalExemptionProvider());
            if (immunization.getMedicalExemptionDate() != null) {
                medicalExemptionDatePicker.setValue(immunization.getMedicalExemptionDate());
            }
        }

        religiousExemptionCheckBox.setSelected(immunization.getReligiousExemption());
        if (immunization.getReligiousExemption()) {
            religiousExemptionStatementArea.setText(immunization.getReligiousExemptionStatement());
            if (immunization.getReligiousExemptionDate() != null) {
                religiousExemptionDatePicker.setValue(immunization.getReligiousExemptionDate());
            }
        }

        philosophicalExemptionCheckBox.setSelected(immunization.getPhilosophicalExemption());
        if (immunization.getPhilosophicalExemption()) {
            philosophicalExemptionStatementArea.setText(immunization.getPhilosophicalExemptionStatement());
            if (immunization.getPhilosophicalExemptionDate() != null) {
                philosophicalExemptionDatePicker.setValue(immunization.getPhilosophicalExemptionDate());
            }
        }

        // Next Dose & Boosters
        if (immunization.getNextDoseDueDate() != null) {
            nextDoseDueDatePicker.setValue(immunization.getNextDoseDueDate());
        }
        if (immunization.getBoosterDueDate() != null) {
            boosterDueDatePicker.setValue(immunization.getBoosterDueDate());
        }
        scheduleNotesArea.setText(immunization.getScheduleNotes());

        // Adverse Reactions
        adverseReactionObservedCheckBox.setSelected(immunization.getAdverseReactionObserved());
        if (immunization.getAdverseReactionObserved()) {
            if (immunization.getReactionSeverity() != null) {
                reactionSeverityComboBox.setValue(immunization.getReactionSeverity().name());
            }
            reactionDescriptionArea.setText(immunization.getReactionDescription());
            parentNotifiedOfReactionCheckBox.setSelected(immunization.getParentNotifiedOfReaction());
            physicianNotifiedOfReactionCheckBox.setSelected(immunization.getPhysicianNotifiedOfReaction());
        }

        // Notes
        notesArea.setText(immunization.getNotes());
    }

    /**
     * Set view mode (read-only)
     */
    public void setViewMode(boolean viewMode) {
        this.viewMode = viewMode;

        // Disable all input controls
        studentComboBox.setDisable(viewMode);
        vaccineTypeComboBox.setDisable(viewMode);
        vaccineNameField.setDisable(viewMode);
        doseNumberField.setDisable(viewMode);
        totalDosesField.setDisable(viewMode);
        administrationDatePicker.setDisable(viewMode);
        isBoosterCheckBox.setDisable(viewMode);
        administeredByComboBox.setDisable(viewMode);
        administrationLocationField.setDisable(viewMode);
        lotNumberField.setDisable(viewMode);
        manufacturerField.setDisable(viewMode);
        expirationDatePicker.setDisable(viewMode);
        verificationMethodComboBox.setDisable(viewMode);
        verifiedByField.setDisable(viewMode);
        verificationDatePicker.setDisable(viewMode);
        browseDocumentButton.setDisable(viewMode);
        meetsStateRequirementCheckBox.setDisable(viewMode);
        medicalExemptionCheckBox.setDisable(viewMode);
        medicalReasonField.setDisable(viewMode);
        medicalProviderField.setDisable(viewMode);
        medicalExemptionDatePicker.setDisable(viewMode);
        religiousExemptionCheckBox.setDisable(viewMode);
        religiousExemptionStatementArea.setDisable(viewMode);
        religiousExemptionDatePicker.setDisable(viewMode);
        philosophicalExemptionCheckBox.setDisable(viewMode);
        philosophicalExemptionStatementArea.setDisable(viewMode);
        philosophicalExemptionDatePicker.setDisable(viewMode);
        nextDoseDueDatePicker.setDisable(viewMode);
        boosterDueDatePicker.setDisable(viewMode);
        scheduleNotesArea.setDisable(viewMode);
        adverseReactionObservedCheckBox.setDisable(viewMode);
        reactionSeverityComboBox.setDisable(viewMode);
        reactionDescriptionArea.setDisable(viewMode);
        parentNotifiedOfReactionCheckBox.setDisable(viewMode);
        physicianNotifiedOfReactionCheckBox.setDisable(viewMode);
        notesArea.setDisable(viewMode);
    }

    // ========================================================================
    // SIDEBAR METHODS
    // ========================================================================

    private void loadStudentImmunizationRecord(Student student) {
        if (student == null) return;

        // Update compliance status
        updateComplianceStatus(student);

        // Load all required vaccines with status
        loadRequiredVaccines(student);

        // Load incomplete series
        loadIncompleteSeries(student);

        // Load overdue doses
        loadOverdueDoses(student);

        // Load exemptions
        loadExemptions(student);
    }

    private void updateComplianceStatus(Student student) {
        boolean isCompliant = immunizationService.isStudentCompliant(student.getId());

        if (isCompliant) {
            complianceStatusLabel.setText("✓ COMPLIANT");
            complianceStatusLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold; -fx-font-size: 14px;");
        } else {
            complianceStatusLabel.setText("⚠ NON-COMPLIANT");
            complianceStatusLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold; -fx-font-size: 14px;");
        }
    }

    private void loadRequiredVaccines(Student student) {
        List<Immunization> immunizations = immunizationService.getImmunizationsByStudent(student.getId());
        List<String> items = new ArrayList<>();

        // Check all required vaccine types
        for (Immunization.VaccineType vaccineType : Immunization.VaccineType.values()) {
            if (vaccineType.isRequiredForSchool()) {
                // Find immunizations for this vaccine type
                List<Immunization> typeImmunizations = immunizations.stream()
                        .filter(i -> i.getVaccineType() == vaccineType)
                        .collect(Collectors.toList());

                String status;
                if (typeImmunizations.isEmpty()) {
                    status = "✗ NOT STARTED";
                } else {
                    boolean seriesComplete = typeImmunizations.stream()
                            .anyMatch(Immunization::isSeriesComplete);
                    if (seriesComplete) {
                        status = "✓ COMPLETE";
                    } else {
                        int maxDose = typeImmunizations.stream()
                                .mapToInt(Immunization::getDoseNumber)
                                .max()
                                .orElse(0);
                        status = String.format("◐ %d/%d doses", maxDose, vaccineType.getTypicalDosesRequired());
                    }
                }

                items.add(String.format("%s - %s", vaccineType.getDisplayName(), status));
            }
        }

        requiredVaccinesListView.setItems(FXCollections.observableArrayList(items));
    }

    private void loadIncompleteSeries(Student student) {
        List<Immunization> immunizations = immunizationService.getImmunizationsByStudent(student.getId());
        List<String> items = new ArrayList<>();

        // Group by vaccine type and find incomplete series
        for (Immunization.VaccineType vaccineType : Immunization.VaccineType.values()) {
            List<Immunization> typeImmunizations = immunizations.stream()
                    .filter(i -> i.getVaccineType() == vaccineType)
                    .collect(Collectors.toList());

            if (!typeImmunizations.isEmpty()) {
                boolean seriesComplete = typeImmunizations.stream()
                        .anyMatch(Immunization::isSeriesComplete);

                if (!seriesComplete) {
                    int maxDose = typeImmunizations.stream()
                            .mapToInt(Immunization::getDoseNumber)
                            .max()
                            .orElse(0);
                    items.add(String.format("%s - Dose %d/%d needed",
                            vaccineType.getDisplayName(),
                            maxDose + 1,
                            vaccineType.getTypicalDosesRequired()));
                }
            }
        }

        incompleteSeriesListView.setItems(FXCollections.observableArrayList(items));
    }

    private void loadOverdueDoses(Student student) {
        List<Immunization> immunizations = immunizationService.getImmunizationsByStudent(student.getId());
        List<String> items = new ArrayList<>();

        for (Immunization immunization : immunizations) {
            if (immunization.isOverdue()) {
                items.add(String.format("%s - Dose %d (Due: %s)",
                        immunization.getVaccineType().getDisplayName(),
                        immunization.getDoseNumber() + 1,
                        immunization.getNextDoseDueDate() != null ?
                                immunization.getNextDoseDueDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) :
                                "N/A"));
            }
        }

        if (items.isEmpty()) {
            items.add("No overdue doses");
        }

        overdueListView.setItems(FXCollections.observableArrayList(items));
    }

    private void loadExemptions(Student student) {
        List<Immunization> immunizations = immunizationService.getImmunizationsByStudent(student.getId());

        long medicalExemptions = immunizations.stream()
                .filter(Immunization::getMedicalExemption)
                .count();
        long religiousExemptions = immunizations.stream()
                .filter(Immunization::getReligiousExemption)
                .count();
        long philosophicalExemptions = immunizations.stream()
                .filter(Immunization::getPhilosophicalExemption)
                .count();

        List<String> exemptionsList = new ArrayList<>();
        if (medicalExemptions > 0) {
            exemptionsList.add(String.format("Medical: %d", medicalExemptions));
        }
        if (religiousExemptions > 0) {
            exemptionsList.add(String.format("Religious: %d", religiousExemptions));
        }
        if (philosophicalExemptions > 0) {
            exemptionsList.add(String.format("Philosophical: %d", philosophicalExemptions));
        }

        if (exemptionsList.isEmpty()) {
            exemptionsLabel.setText("No Active Exemptions");
        } else {
            exemptionsLabel.setText("Active Exemptions: " + String.join(", ", exemptionsList));
        }
    }

    // ========================================================================
    // EVENT HANDLERS
    // ========================================================================

    @FXML
    private void handleBrowseDocument() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Immunization Documentation");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Stage stage = (Stage) browseDocumentButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            selectedDocumentPath = selectedFile.getAbsolutePath();
            documentationPathField.setText(selectedFile.getName());
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
            Immunization immunization = buildImmunizationFromForm();

            if (currentImmunization == null) {
                // Create new immunization
                immunizationService.createImmunization(immunization);
                log.info("Created new immunization record for student: {}", currentStudent.getStudentId());
                showSuccess("Immunization record created successfully!");
            } else {
                // Update existing immunization
                immunization.setId(currentImmunization.getId());
                immunizationService.updateImmunization(immunization);
                log.info("Updated immunization record ID: {}", currentImmunization.getId());
                showSuccess("Immunization record updated successfully!");
            }

            // Refresh sidebar
            loadStudentImmunizationRecord(currentStudent);

            handleClose();
        } catch (Exception e) {
            log.error("Error saving immunization record", e);
            showError("Failed to save immunization record: " + e.getMessage());
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

        if (vaccineTypeComboBox.getValue() == null || vaccineTypeComboBox.getValue().isEmpty()) {
            errors.add("Vaccine type is required");
        }

        if (doseNumberField.getText() == null || doseNumberField.getText().trim().isEmpty()) {
            errors.add("Dose number is required");
        }

        if (administrationDatePicker.getValue() == null) {
            errors.add("Administration date is required");
        }

        if (verificationMethodComboBox.getValue() == null || verificationMethodComboBox.getValue().isEmpty()) {
            errors.add("Verification method is required");
        }

        // Validate adverse reaction documentation
        if (adverseReactionObservedCheckBox.isSelected()) {
            if (reactionSeverityComboBox.getValue() == null || reactionSeverityComboBox.getValue().isEmpty()) {
                errors.add("Adverse reaction severity is required when reaction is observed");
            }
            if (reactionDescriptionArea.getText() == null || reactionDescriptionArea.getText().trim().isEmpty()) {
                errors.add("Adverse reaction description is required when reaction is observed");
            }
        }

        // Validate exemption documentation
        if (medicalExemptionCheckBox.isSelected()) {
            if (medicalReasonField.getText() == null || medicalReasonField.getText().trim().isEmpty()) {
                errors.add("Medical exemption reason is required");
            }
        }

        if (religiousExemptionCheckBox.isSelected()) {
            if (religiousExemptionStatementArea.getText() == null || religiousExemptionStatementArea.getText().trim().isEmpty()) {
                errors.add("Religious exemption statement is required");
            }
        }

        if (philosophicalExemptionCheckBox.isSelected()) {
            if (philosophicalExemptionStatementArea.getText() == null || philosophicalExemptionStatementArea.getText().trim().isEmpty()) {
                errors.add("Philosophical exemption statement is required");
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

    private Immunization buildImmunizationFromForm() {
        Immunization immunization = new Immunization();

        // Student
        immunization.setStudent(studentComboBox.getValue());

        // Vaccine Information
        immunization.setVaccineType(Immunization.VaccineType.valueOf(vaccineTypeComboBox.getValue()));
        immunization.setVaccineName(vaccineNameField.getText());
        immunization.setDoseNumber(Integer.parseInt(doseNumberField.getText()));
        if (!totalDosesField.getText().trim().isEmpty()) {
            immunization.setTotalDosesRequired(Integer.parseInt(totalDosesField.getText()));
        }
        immunization.setAdministrationDate(administrationDatePicker.getValue());
        immunization.setIsBooster(isBoosterCheckBox.isSelected());

        // Provider Information
        if (administeredByComboBox.getValue() != null) {
            immunization.setAdministeredBy(administeredByComboBox.getValue().getFirstName() + " " +
                    administeredByComboBox.getValue().getLastName());
        }
        immunization.setAdministrationLocation(administrationLocationField.getText());
        immunization.setLotNumber(lotNumberField.getText());
        immunization.setManufacturer(manufacturerField.getText());
        immunization.setExpirationDate(expirationDatePicker.getValue());

        // Verification
        immunization.setVerificationMethod(
                Immunization.VerificationMethod.valueOf(verificationMethodComboBox.getValue()));
        immunization.setVerifiedBy(verifiedByField.getText());
        immunization.setVerificationDate(verificationDatePicker.getValue());
        immunization.setDocumentationPath(selectedDocumentPath);
        immunization.setMeetsStateRequirement(meetsStateRequirementCheckBox.isSelected());

        // Exemptions
        immunization.setMedicalExemption(medicalExemptionCheckBox.isSelected());
        if (medicalExemptionCheckBox.isSelected()) {
            immunization.setMedicalExemptionReason(medicalReasonField.getText());
            immunization.setMedicalExemptionProvider(medicalProviderField.getText());
            immunization.setMedicalExemptionDate(medicalExemptionDatePicker.getValue());
        }

        immunization.setReligiousExemption(religiousExemptionCheckBox.isSelected());
        if (religiousExemptionCheckBox.isSelected()) {
            immunization.setReligiousExemptionStatement(religiousExemptionStatementArea.getText());
            immunization.setReligiousExemptionDate(religiousExemptionDatePicker.getValue());
        }

        immunization.setPhilosophicalExemption(philosophicalExemptionCheckBox.isSelected());
        if (philosophicalExemptionCheckBox.isSelected()) {
            immunization.setPhilosophicalExemptionStatement(philosophicalExemptionStatementArea.getText());
            immunization.setPhilosophicalExemptionDate(philosophicalExemptionDatePicker.getValue());
        }

        // Next Dose & Boosters
        immunization.setNextDoseDueDate(nextDoseDueDatePicker.getValue());
        immunization.setBoosterDueDate(boosterDueDatePicker.getValue());
        immunization.setScheduleNotes(scheduleNotesArea.getText());

        // Adverse Reactions
        immunization.setAdverseReactionObserved(adverseReactionObservedCheckBox.isSelected());
        if (adverseReactionObservedCheckBox.isSelected()) {
            if (reactionSeverityComboBox.getValue() != null) {
                immunization.setReactionSeverity(
                        Immunization.ReactionSeverity.valueOf(reactionSeverityComboBox.getValue()));
            }
            immunization.setReactionDescription(reactionDescriptionArea.getText());
            immunization.setParentNotifiedOfReaction(parentNotifiedOfReactionCheckBox.isSelected());
            immunization.setPhysicianNotifiedOfReaction(physicianNotifiedOfReactionCheckBox.isSelected());
        }

        // Notes
        immunization.setNotes(notesArea.getText());

        return immunization;
    }

    private void clearForm() {
        currentImmunization = null;
        selectedDocumentPath = null;

        vaccineTypeComboBox.setValue(null);
        vaccineNameField.clear();
        doseNumberField.clear();
        totalDosesField.clear();
        administrationDatePicker.setValue(LocalDate.now());
        isBoosterCheckBox.setSelected(false);

        administeredByComboBox.setValue(null);
        administrationLocationField.clear();
        lotNumberField.clear();
        manufacturerField.clear();
        expirationDatePicker.setValue(null);

        verificationMethodComboBox.setValue(null);
        verifiedByField.clear();
        verificationDatePicker.setValue(LocalDate.now());
        documentationPathField.clear();
        meetsStateRequirementCheckBox.setSelected(true);

        medicalExemptionCheckBox.setSelected(false);
        medicalReasonField.clear();
        medicalProviderField.clear();
        medicalExemptionDatePicker.setValue(null);

        religiousExemptionCheckBox.setSelected(false);
        religiousExemptionStatementArea.clear();
        religiousExemptionDatePicker.setValue(null);

        philosophicalExemptionCheckBox.setSelected(false);
        philosophicalExemptionStatementArea.clear();
        philosophicalExemptionDatePicker.setValue(null);

        nextDoseDueDatePicker.setValue(null);
        boosterDueDatePicker.setValue(null);
        scheduleNotesArea.clear();

        adverseReactionObservedCheckBox.setSelected(false);
        reactionSeverityComboBox.setValue(null);
        reactionDescriptionArea.clear();
        parentNotifiedOfReactionCheckBox.setSelected(false);
        physicianNotifiedOfReactionCheckBox.setSelected(false);

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
