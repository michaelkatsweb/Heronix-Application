package com.heronix.controller;

import com.heronix.model.domain.MedicalRecord;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.User;
import com.heronix.service.MedicalRecordService;
import com.heronix.service.StudentService;
import com.heronix.service.UserService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for Medical Information Form
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class MedicalInformationFormController {

    private final MedicalRecordService medicalRecordService;
    private final StudentService studentService;
    private final UserService userService;

    // Table and columns
    @FXML private TableView<MedicalRecord> tblMedicalRecords;
    @FXML private TableColumn<MedicalRecord, String> colStudent;
    @FXML private TableColumn<MedicalRecord, String> colAllergies;
    @FXML private TableColumn<MedicalRecord, String> colConditions;
    @FXML private TableColumn<MedicalRecord, String> colMedications;
    @FXML private TableColumn<MedicalRecord, String> colAlerts;
    @FXML private TableColumn<MedicalRecord, String> colLastReview;

    // Filter controls
    @FXML private ComboBox<Student> cmbStudentFilter;
    @FXML private ComboBox<String> cmbStatusFilter;
    @FXML private ToggleButton toggleHasAllergies;
    @FXML private ToggleButton toggleHasConditions;
    @FXML private ToggleButton toggleHasMedications;
    @FXML private ToggleButton toggleHasAlerts;
    @FXML private ToggleButton toggleOverdueReview;
    @FXML private ToggleButton toggleIncomplete;

    // Action buttons
    @FXML private Button btnRefresh;
    @FXML private Button btnSearch;
    @FXML private Button btnClearFilters;
    @FXML private Button btnView;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    @FXML private Button btnScheduleReview;
    @FXML private Button btnPrint;

    // Basic Information
    @FXML private ComboBox<Student> cmbStudent;
    @FXML private ComboBox<String> cmbBloodType;
    @FXML private ComboBox<String> cmbImmunizationStatus;

    // Allergies
    @FXML private CheckBox chkHasAllergies;
    @FXML private TextArea txtFoodAllergies;
    @FXML private TextArea txtMedicationAllergies;
    @FXML private TextArea txtEnvironmentalAllergies;
    @FXML private ComboBox<String> cmbAllergySeverity;

    // Chronic Conditions
    @FXML private CheckBox chkHasChronicConditions;
    @FXML private TextArea txtChronicConditions;
    @FXML private ComboBox<String> cmbConditionSeverity;

    // Medications
    @FXML private CheckBox chkTakingMedications;
    @FXML private TextArea txtCurrentMedications;
    @FXML private CheckBox chkSelfAdministered;
    @FXML private CheckBox chkRequiresNurse;

    // Medical Alerts
    @FXML private CheckBox chkHasMedicalAlerts;
    @FXML private TextArea txtMedicalAlerts;
    @FXML private CheckBox chkHasEpiPen;
    @FXML private CheckBox chkHasInhaler;

    // Physician Information
    @FXML private TextField txtPhysicianName;
    @FXML private TextField txtPhysicianPhone;
    @FXML private TextArea txtPhysicianAddress;

    // Insurance Information
    @FXML private TextField txtInsuranceProvider;
    @FXML private TextField txtInsurancePolicyNumber;
    @FXML private TextField txtInsuranceGroupNumber;

    // Health Screenings
    @FXML private DatePicker dpLastPhysicalDate;
    @FXML private DatePicker dpPhysicalRequiredDate;
    @FXML private CheckBox chkImmunizationsComplete;
    @FXML private CheckBox chkAthleticClearance;
    @FXML private DatePicker dpAthleticClearanceDate;
    @FXML private CheckBox chkConcussionProtocol;

    // Emergency Authorization
    @FXML private CheckBox chkEmergencyTreatmentAuthorized;
    @FXML private DatePicker dpEmergencyAuthorizationDate;
    @FXML private TextField txtPreferredHospital;

    // Review and Notes
    @FXML private DatePicker dpLastReviewDate;
    @FXML private DatePicker dpNextReviewDate;
    @FXML private CheckBox chkVerified;
    @FXML private DatePicker dpVerificationDate;
    @FXML private TextArea txtNotes;

    // Status bar
    @FXML private Label lblCurrentUser;
    @FXML private Label lblTotalRecords;
    @FXML private Label lblIncompleteCount;

    private ObservableList<MedicalRecord> medicalRecordsList = FXCollections.observableArrayList();
    private ObservableList<MedicalRecord> filteredList = FXCollections.observableArrayList();
    private MedicalRecord selectedRecord;
    private boolean editMode = false;

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        setupTableColumns();
        setupComboBoxes();
        setupListeners();
        loadStudents();
        loadMedicalRecords();
        updateStatusBar();
        setFormEditable(false);

        // Set current user
        try {
            List<User> users = userService.findAll();
            if (!users.isEmpty()) {
                lblCurrentUser.setText(users.get(0).getUsername());
            } else {
                lblCurrentUser.setText("System User");
            }
        } catch (Exception e) {
            lblCurrentUser.setText("System User");
        }
    }

    /**
     * Setup table columns
     */
    private void setupTableColumns() {
        colStudent.setCellValueFactory(cellData -> {
            Student student = cellData.getValue().getStudent();
            return new SimpleStringProperty(student != null ?
                student.getLastName() + ", " + student.getFirstName() : "");
        });

        colAllergies.setCellValueFactory(cellData -> {
            MedicalRecord record = cellData.getValue();
            String text = Boolean.TRUE.equals(record.getHasAllergies()) ? "Yes" : "No";
            return new SimpleStringProperty(text);
        });

        colConditions.setCellValueFactory(cellData -> {
            MedicalRecord record = cellData.getValue();
            String text = Boolean.TRUE.equals(record.getHasChronicConditions()) ? "Yes" : "No";
            return new SimpleStringProperty(text);
        });

        colMedications.setCellValueFactory(cellData -> {
            MedicalRecord record = cellData.getValue();
            String text = Boolean.TRUE.equals(record.getTakingMedications()) ? "Yes" : "No";
            return new SimpleStringProperty(text);
        });

        colAlerts.setCellValueFactory(cellData -> {
            MedicalRecord record = cellData.getValue();
            String text = Boolean.TRUE.equals(record.getHasMedicalAlerts()) ? "Yes" : "No";
            return new SimpleStringProperty(text);
        });

        colLastReview.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getLastReviewDate();
            String dateStr = date != null ? date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : "";
            return new SimpleStringProperty(dateStr);
        });
    }

    /**
     * Setup combo boxes
     */
    private void setupComboBoxes() {
        // Blood types
        cmbBloodType.setItems(FXCollections.observableArrayList(
            "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Unknown"
        ));

        // Immunization status
        cmbImmunizationStatus.setItems(FXCollections.observableArrayList(
            "Complete", "Incomplete", "Pending", "Exempt - Medical", "Exempt - Religious", "Unknown"
        ));

        // Severity levels
        ObservableList<String> severityLevels = FXCollections.observableArrayList(
            "Mild", "Moderate", "Severe", "Life-Threatening"
        );
        cmbAllergySeverity.setItems(severityLevels);
        cmbConditionSeverity.setItems(severityLevels);

        // Status filter
        cmbStatusFilter.setItems(FXCollections.observableArrayList(
            "All Statuses", "Complete", "Incomplete", "Overdue Review", "Verified", "Unverified"
        ));

        // Student combo box converter
        StringConverter<Student> studentConverter = new StringConverter<>() {
            @Override
            public String toString(Student student) {
                return student != null ?
                    student.getLastName() + ", " + student.getFirstName() + " (" + student.getStudentId() + ")" : "";
            }

            @Override
            public Student fromString(String string) {
                return null;
            }
        };

        cmbStudent.setConverter(studentConverter);
        cmbStudentFilter.setConverter(studentConverter);
    }

    /**
     * Setup listeners
     */
    private void setupListeners() {
        // Table selection listener
        tblMedicalRecords.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                selectedRecord = newValue;
                if (newValue != null) {
                    populateForm(newValue);
                    btnView.setDisable(false);
                    btnEdit.setDisable(false);
                    btnDelete.setDisable(false);
                } else {
                    btnView.setDisable(true);
                    btnEdit.setDisable(true);
                    btnDelete.setDisable(true);
                }
            }
        );
    }

    /**
     * Load students into combo boxes
     */
    private void loadStudents() {
        List<Student> students = studentService.getActiveStudents();
        cmbStudent.setItems(FXCollections.observableArrayList(students));
        cmbStudentFilter.setItems(FXCollections.observableArrayList(students));
    }

    /**
     * Load medical records
     */
    private void loadMedicalRecords() {
        try {
            List<MedicalRecord> records = medicalRecordService.getAllMedicalRecords();
            medicalRecordsList.setAll(records);
            filteredList.setAll(records);
            tblMedicalRecords.setItems(filteredList);
            updateStatusBar();
        } catch (Exception e) {
            log.error("Error loading medical records", e);
            showError("Failed to load medical records: " + e.getMessage());
        }
    }

    /**
     * Populate form with medical record data
     */
    private void populateForm(MedicalRecord record) {
        if (record == null) {
            clearForm();
            return;
        }

        // Basic Information
        cmbStudent.setValue(record.getStudent());
        cmbBloodType.setValue(record.getBloodType());
        cmbImmunizationStatus.setValue(record.getImmunizationStatus() != null ?
            record.getImmunizationStatus().toString() : null);

        // Allergies
        chkHasAllergies.setSelected(Boolean.TRUE.equals(record.getHasAllergies()));
        txtFoodAllergies.setText(record.getFoodAllergies());
        txtMedicationAllergies.setText(record.getMedicationAllergies());
        txtEnvironmentalAllergies.setText(record.getEnvironmentalAllergies());
        cmbAllergySeverity.setValue(record.getAllergySeverity() != null ?
            record.getAllergySeverity().toString() : null);

        // Chronic Conditions
        chkHasChronicConditions.setSelected(Boolean.TRUE.equals(record.getHasChronicConditions()));
        txtChronicConditions.setText(record.getChronicConditions());
        cmbConditionSeverity.setValue(record.getConditionSeverity() != null ?
            record.getConditionSeverity().toString() : null);

        // Medications
        chkTakingMedications.setSelected(Boolean.TRUE.equals(record.getTakingMedications()));
        txtCurrentMedications.setText(record.getCurrentMedications());
        chkSelfAdministered.setSelected(Boolean.TRUE.equals(record.getMedicationSelfAdministered()));
        chkRequiresNurse.setSelected(Boolean.TRUE.equals(record.getMedicationRequiresNurse()));

        // Medical Alerts
        chkHasMedicalAlerts.setSelected(Boolean.TRUE.equals(record.getHasMedicalAlerts()));
        txtMedicalAlerts.setText(record.getMedicalAlerts());
        chkHasEpiPen.setSelected(Boolean.TRUE.equals(record.getHasEpiPen()));
        chkHasInhaler.setSelected(Boolean.TRUE.equals(record.getHasInhaler()));

        // Physician Information
        txtPhysicianName.setText(record.getPhysicianName());
        txtPhysicianPhone.setText(record.getPhysicianPhone());
        txtPhysicianAddress.setText(record.getPhysicianAddress());

        // Insurance Information
        txtInsuranceProvider.setText(record.getInsuranceProvider());
        txtInsurancePolicyNumber.setText(record.getInsurancePolicyNumber());
        txtInsuranceGroupNumber.setText(record.getInsuranceGroupNumber());

        // Health Screenings
        dpLastPhysicalDate.setValue(record.getLastPhysicalDate());
        dpPhysicalRequiredDate.setValue(record.getPhysicalRequiredDate());
        chkImmunizationsComplete.setSelected(Boolean.TRUE.equals(record.getImmunizationsComplete()));
        chkAthleticClearance.setSelected(Boolean.TRUE.equals(record.getAthleticClearance()));
        dpAthleticClearanceDate.setValue(record.getAthleticClearanceDate());
        chkConcussionProtocol.setSelected(Boolean.TRUE.equals(record.getConcussionProtocol()));

        // Emergency Authorization
        chkEmergencyTreatmentAuthorized.setSelected(Boolean.TRUE.equals(record.getEmergencyTreatmentAuthorized()));
        dpEmergencyAuthorizationDate.setValue(record.getEmergencyAuthorizationDate());
        txtPreferredHospital.setText(record.getPreferredHospital());

        // Review and Notes
        dpLastReviewDate.setValue(record.getLastReviewDate());
        dpNextReviewDate.setValue(record.getNextReviewDate());
        chkVerified.setSelected(Boolean.TRUE.equals(record.getVerified()));
        dpVerificationDate.setValue(record.getVerificationDate());
        txtNotes.setText(record.getNotes());
    }

    /**
     * Clear the form
     */
    private void clearForm() {
        cmbStudent.setValue(null);
        cmbBloodType.setValue(null);
        cmbImmunizationStatus.setValue(null);

        chkHasAllergies.setSelected(false);
        txtFoodAllergies.clear();
        txtMedicationAllergies.clear();
        txtEnvironmentalAllergies.clear();
        cmbAllergySeverity.setValue(null);

        chkHasChronicConditions.setSelected(false);
        txtChronicConditions.clear();
        cmbConditionSeverity.setValue(null);

        chkTakingMedications.setSelected(false);
        txtCurrentMedications.clear();
        chkSelfAdministered.setSelected(false);
        chkRequiresNurse.setSelected(false);

        chkHasMedicalAlerts.setSelected(false);
        txtMedicalAlerts.clear();
        chkHasEpiPen.setSelected(false);
        chkHasInhaler.setSelected(false);

        txtPhysicianName.clear();
        txtPhysicianPhone.clear();
        txtPhysicianAddress.clear();

        txtInsuranceProvider.clear();
        txtInsurancePolicyNumber.clear();
        txtInsuranceGroupNumber.clear();

        dpLastPhysicalDate.setValue(null);
        dpPhysicalRequiredDate.setValue(null);
        chkImmunizationsComplete.setSelected(false);
        chkAthleticClearance.setSelected(false);
        dpAthleticClearanceDate.setValue(null);
        chkConcussionProtocol.setSelected(false);

        chkEmergencyTreatmentAuthorized.setSelected(false);
        dpEmergencyAuthorizationDate.setValue(null);
        txtPreferredHospital.clear();

        dpLastReviewDate.setValue(null);
        dpNextReviewDate.setValue(null);
        chkVerified.setSelected(false);
        dpVerificationDate.setValue(null);
        txtNotes.clear();
    }

    /**
     * Set form editable state
     */
    private void setFormEditable(boolean editable) {
        cmbStudent.setDisable(!editable);
        cmbBloodType.setDisable(!editable);
        cmbImmunizationStatus.setDisable(!editable);

        chkHasAllergies.setDisable(!editable);
        txtFoodAllergies.setEditable(editable);
        txtMedicationAllergies.setEditable(editable);
        txtEnvironmentalAllergies.setEditable(editable);
        cmbAllergySeverity.setDisable(!editable);

        chkHasChronicConditions.setDisable(!editable);
        txtChronicConditions.setEditable(editable);
        cmbConditionSeverity.setDisable(!editable);

        chkTakingMedications.setDisable(!editable);
        txtCurrentMedications.setEditable(editable);
        chkSelfAdministered.setDisable(!editable);
        chkRequiresNurse.setDisable(!editable);

        chkHasMedicalAlerts.setDisable(!editable);
        txtMedicalAlerts.setEditable(editable);
        chkHasEpiPen.setDisable(!editable);
        chkHasInhaler.setDisable(!editable);

        txtPhysicianName.setEditable(editable);
        txtPhysicianPhone.setEditable(editable);
        txtPhysicianAddress.setEditable(editable);

        txtInsuranceProvider.setEditable(editable);
        txtInsurancePolicyNumber.setEditable(editable);
        txtInsuranceGroupNumber.setEditable(editable);

        dpLastPhysicalDate.setDisable(!editable);
        dpPhysicalRequiredDate.setDisable(!editable);
        chkImmunizationsComplete.setDisable(!editable);
        chkAthleticClearance.setDisable(!editable);
        dpAthleticClearanceDate.setDisable(!editable);
        chkConcussionProtocol.setDisable(!editable);

        chkEmergencyTreatmentAuthorized.setDisable(!editable);
        dpEmergencyAuthorizationDate.setDisable(!editable);
        txtPreferredHospital.setEditable(editable);

        dpLastReviewDate.setDisable(!editable);
        dpNextReviewDate.setDisable(!editable);
        chkVerified.setDisable(!editable);
        dpVerificationDate.setDisable(!editable);
        txtNotes.setEditable(editable);

        btnSave.setDisable(!editable);
        btnCancel.setDisable(!editable);
    }

    /**
     * Update status bar
     */
    private void updateStatusBar() {
        lblTotalRecords.setText(String.valueOf(medicalRecordsList.size()));

        long incompleteCount = medicalRecordService.getIncompleteMedicalRecords().size();
        lblIncompleteCount.setText(String.valueOf(incompleteCount));
    }

    // Event Handlers

    @FXML
    private void handleRefresh() {
        loadMedicalRecords();
        showInfo("Medical records refreshed");
    }

    @FXML
    private void handleSearch() {
        applyFilters();
    }

    @FXML
    private void handleClearFilters() {
        cmbStudentFilter.setValue(null);
        cmbStatusFilter.setValue(null);
        toggleHasAllergies.setSelected(false);
        toggleHasConditions.setSelected(false);
        toggleHasMedications.setSelected(false);
        toggleHasAlerts.setSelected(false);
        toggleOverdueReview.setSelected(false);
        toggleIncomplete.setSelected(false);
        filteredList.setAll(medicalRecordsList);
    }

    @FXML
    private void handleQuickFilter() {
        applyFilters();
    }

    /**
     * Apply filters to medical records list
     */
    private void applyFilters() {
        List<MedicalRecord> filtered = medicalRecordsList.stream()
            .filter(record -> {
                // Student filter
                if (cmbStudentFilter.getValue() != null) {
                    if (!record.getStudent().equals(cmbStudentFilter.getValue())) {
                        return false;
                    }
                }

                // Quick filters
                if (toggleHasAllergies.isSelected() && !Boolean.TRUE.equals(record.getHasAllergies())) {
                    return false;
                }
                if (toggleHasConditions.isSelected() && !Boolean.TRUE.equals(record.getHasChronicConditions())) {
                    return false;
                }
                if (toggleHasMedications.isSelected() && !Boolean.TRUE.equals(record.getTakingMedications())) {
                    return false;
                }
                if (toggleHasAlerts.isSelected() && !Boolean.TRUE.equals(record.getHasMedicalAlerts())) {
                    return false;
                }
                if (toggleOverdueReview.isSelected()) {
                    if (record.getNextReviewDate() == null || record.getNextReviewDate().isAfter(LocalDate.now())) {
                        return false;
                    }
                }

                return true;
            })
            .collect(Collectors.toList());

        filteredList.setAll(filtered);
    }

    @FXML
    private void handleView() {
        if (selectedRecord != null) {
            populateForm(selectedRecord);
            setFormEditable(false);
            editMode = false;
        }
    }

    @FXML
    private void handleEdit() {
        if (selectedRecord != null) {
            populateForm(selectedRecord);
            setFormEditable(true);
            editMode = true;
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedRecord == null) {
            showWarning("Please select a medical record to delete");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Medical Record");
        alert.setContentText("Are you sure you want to delete this medical record?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    medicalRecordService.deleteMedicalRecord(selectedRecord.getId());
                    showInfo("Medical record deleted successfully");
                    loadMedicalRecords();
                    clearForm();
                } catch (Exception e) {
                    log.error("Error deleting medical record", e);
                    showError("Failed to delete medical record: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        try {
            Student student = cmbStudent.getValue();

            MedicalRecord record;
            if (editMode && selectedRecord != null) {
                record = selectedRecord;
            } else {
                record = medicalRecordService.getOrCreateMedicalRecord(student.getId());
            }

            // Update fields
            record.setBloodType(cmbBloodType.getValue());

            // Allergies
            record.setHasAllergies(chkHasAllergies.isSelected());
            record.setFoodAllergies(txtFoodAllergies.getText());
            record.setMedicationAllergies(txtMedicationAllergies.getText());
            record.setEnvironmentalAllergies(txtEnvironmentalAllergies.getText());

            // Chronic Conditions
            record.setHasChronicConditions(chkHasChronicConditions.isSelected());
            record.setChronicConditions(txtChronicConditions.getText());

            // Medications
            record.setTakingMedications(chkTakingMedications.isSelected());
            record.setCurrentMedications(txtCurrentMedications.getText());
            record.setMedicationSelfAdministered(chkSelfAdministered.isSelected());
            record.setMedicationRequiresNurse(chkRequiresNurse.isSelected());

            // Medical Alerts
            record.setHasMedicalAlerts(chkHasMedicalAlerts.isSelected());
            record.setMedicalAlerts(txtMedicalAlerts.getText());
            record.setHasEpiPen(chkHasEpiPen.isSelected());
            record.setHasInhaler(chkHasInhaler.isSelected());

            // Physician Information
            record.setPhysicianName(txtPhysicianName.getText());
            record.setPhysicianPhone(txtPhysicianPhone.getText());
            record.setPhysicianAddress(txtPhysicianAddress.getText());

            // Insurance Information
            record.setInsuranceProvider(txtInsuranceProvider.getText());
            record.setInsurancePolicyNumber(txtInsurancePolicyNumber.getText());
            record.setInsuranceGroupNumber(txtInsuranceGroupNumber.getText());

            // Health Screenings
            record.setLastPhysicalDate(dpLastPhysicalDate.getValue());
            record.setPhysicalRequiredDate(dpPhysicalRequiredDate.getValue());
            record.setImmunizationsComplete(chkImmunizationsComplete.isSelected());
            record.setAthleticClearance(chkAthleticClearance.isSelected());
            record.setAthleticClearanceDate(dpAthleticClearanceDate.getValue());
            record.setConcussionProtocol(chkConcussionProtocol.isSelected());

            // Emergency Authorization
            record.setEmergencyTreatmentAuthorized(chkEmergencyTreatmentAuthorized.isSelected());
            record.setEmergencyAuthorizationDate(dpEmergencyAuthorizationDate.getValue());
            record.setPreferredHospital(txtPreferredHospital.getText());

            // Review and Notes
            record.setLastReviewDate(dpLastReviewDate.getValue());
            record.setNextReviewDate(dpNextReviewDate.getValue());
            record.setVerified(chkVerified.isSelected());
            record.setVerificationDate(dpVerificationDate.getValue());
            record.setNotes(txtNotes.getText());

            MedicalRecord saved = medicalRecordService.updateMedicalRecord(record);

            showInfo("Medical record saved successfully");
            loadMedicalRecords();
            setFormEditable(false);
            editMode = false;

            tblMedicalRecords.getSelectionModel().select(saved);
        } catch (Exception e) {
            log.error("Error saving medical record", e);
            showError("Failed to save medical record: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        if (selectedRecord != null) {
            populateForm(selectedRecord);
        } else {
            clearForm();
        }
        setFormEditable(false);
        editMode = false;
    }

    @FXML
    private void handleScheduleReview() {
        if (selectedRecord == null) {
            showWarning("Please select a medical record");
            return;
        }

        LocalDate nextReview = LocalDate.now().plusMonths(6);
        dpNextReviewDate.setValue(nextReview);
        showInfo("Review scheduled for " + nextReview.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
    }

    @FXML
    private void handlePrint() {
        if (selectedRecord == null) {
            showWarning("Please select a medical record to print");
            return;
        }
        showInfo("Print functionality will be implemented in a future update");
    }

    /**
     * Validate form inputs
     */
    private boolean validateForm() {
        if (cmbStudent.getValue() == null) {
            showWarning("Please select a student");
            return false;
        }

        return true;
    }

    // Helper methods for alerts

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
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

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
