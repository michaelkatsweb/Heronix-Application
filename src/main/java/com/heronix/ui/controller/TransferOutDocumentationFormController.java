package com.heronix.ui.controller;

import com.heronix.model.domain.TransferOutDocumentation;
import com.heronix.model.domain.TransferOutDocumentation.TransferOutStatus;
import com.heronix.model.domain.TransferOutDocumentation.TransmissionMethod;
import com.heronix.model.domain.TransferOutDocumentation.TransferOutReason;
import com.heronix.model.domain.Student;
import com.heronix.repository.StudentRepository;
import com.heronix.security.SecurityContext;
import com.heronix.service.TransferOutDocumentationService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for Transfer Out Documentation Form
 * Handles students transferring to other schools with document tracking
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Slf4j
@Component
public class TransferOutDocumentationFormController {

    @Autowired
    private TransferOutDocumentationService transferOutService;

    @Autowired
    private StudentRepository studentRepository;

    // Header
    @FXML private TextField transferOutNumberField;
    @FXML private Label statusLabel;

    // Section 1: Student Information
    @FXML private TextField studentSearchField;
    @FXML private Label studentNameLabel;
    @FXML private TextField studentIdField;
    @FXML private TextField currentGradeField;
    @FXML private TextField currentHomeroomField;
    @FXML private DatePicker lastAttendanceDate;

    // Section 2: Transfer Information
    @FXML private DatePicker requestDate;
    @FXML private DatePicker expectedTransferDate;
    @FXML private DatePicker actualTransferDate;
    @FXML private ComboBox<String> transferReason;

    // Section 3: Destination School
    @FXML private TextField destinationSchoolName;
    @FXML private TextField destinationSchoolDistrict;
    @FXML private TextField destinationCity;
    @FXML private ComboBox<String> destinationState;
    @FXML private TextField destinationZip;
    @FXML private TextField destinationPhone;
    @FXML private TextField destinationEmail;
    @FXML private TextField destinationFax;
    @FXML private TextField destinationContactPerson;
    @FXML private TextField destinationContactEmail;
    @FXML private TextField destinationContactPhone;

    // Section 4: Documents Checklist (12 items matching entity)
    @FXML private CheckBox transcriptIncluded;
    @FXML private CheckBox attendanceRecordIncluded;  // Maps to attendanceRecordsIncluded
    @FXML private CheckBox disciplineRecordIncluded;  // Maps to disciplineRecordsIncluded
    @FXML private CheckBox immunizationRecordIncluded;  // Maps to immunizationRecordsIncluded
    @FXML private CheckBox healthRecordIncluded;  // Maps to healthRecordsIncluded
    @FXML private CheckBox iepIncluded;
    @FXML private CheckBox plan504Included;
    @FXML private CheckBox testScoresIncluded;
    @FXML private CheckBox cumulativeFileIncluded;  // Maps to cumulativeFolderIncluded
    @FXML private CheckBox birthCertificateIncluded;  // Maps to specialEducationRecordsIncluded (repurposed)
    @FXML private CheckBox withdrawalFormIncluded;  // Maps to counselingRecordsIncluded (repurposed)
    @FXML private CheckBox parentRequestIncluded;  // Maps to athleticEligibilityIncluded (repurposed)
    @FXML private Label documentsIncludedCount;

    // Section 5: Transmission Details
    @FXML private ComboBox<String> transmissionMethod;
    @FXML private DatePicker sentDate;
    @FXML private TextField trackingNumber;
    @FXML private TextField sentByField;

    // Section 6: Acknowledgment
    @FXML private CheckBox parentConsentObtained;
    @FXML private TextField acknowledgedBy;
    @FXML private ComboBox<String> acknowledgmentMethod;
    @FXML private DatePicker acknowledgmentDate;
    @FXML private TextArea acknowledgmentNotes;

    // Section 7: Administrative Notes
    @FXML private TextArea administrativeNotes;

    // Section 8: Audit Information
    @FXML private Label createdByLabel;
    @FXML private Label createdAtLabel;
    @FXML private Label assignedStaffLabel;
    @FXML private Label processingDaysLabel;

    private TransferOutDocumentation currentTransferOut;
    private Student selectedStudent;
    private boolean isDirty = false;

    @FXML
    public void initialize() {
        log.info("Initializing TransferOutDocumentationFormController");

        populateStates();
        populateTransferReasons();
        populateTransmissionMethods();
        populateAcknowledgmentMethods();
        setupListeners();
        createNewTransferOut();

        log.info("TransferOutDocumentationFormController initialized successfully");
    }

    private void populateStates() {
        if (destinationState == null) return;
        destinationState.getItems().addAll(
                "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA",
                "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD",
                "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ",
                "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC",
                "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY"
        );
    }

    private void populateTransferReasons() {
        if (transferReason == null) return;
        transferReason.getItems().addAll(
                "FAMILY_RELOCATION", "PARENT_CHOICE", "ACADEMIC_REASONS",
                "DISCIPLINARY_REASONS", "SPECIAL_SERVICES_NEEDED", "OTHER"
        );
    }

    private void populateTransmissionMethods() {
        if (transmissionMethod == null) return;
        transmissionMethod.getItems().addAll(
                "EMAIL", "FAX", "CERTIFIED_MAIL", "REGULAR_MAIL",
                "HAND_DELIVERY", "ELECTRONIC_TRANSCRIPT_SYSTEM"
        );
    }

    private void populateAcknowledgmentMethods() {
        if (acknowledgmentMethod == null) return;
        acknowledgmentMethod.getItems().addAll(
                "EMAIL", "PHONE", "FAX", "MAIL", "IN_PERSON"
        );
    }

    private void setupListeners() {
        // Update documents count when checkboxes change
        List.of(transcriptIncluded, attendanceRecordIncluded, disciplineRecordIncluded,
                immunizationRecordIncluded, healthRecordIncluded, iepIncluded,
                plan504Included, testScoresIncluded, cumulativeFileIncluded,
                birthCertificateIncluded, withdrawalFormIncluded, parentRequestIncluded)
                .forEach(cb -> {
                    if (cb != null) {
                        cb.setOnAction(e -> {
                            updateDocumentsCount();
                            isDirty = true;
                        });
                    }
                });
    }

    private void createNewTransferOut() {
        // Don't create database record on initialization
        requestDate.setValue(LocalDate.now());
        statusLabel.setText("DRAFT");
        updateDocumentsCount();
    }

    @FXML
    private void searchStudent() {
        String searchTerm = studentSearchField.getText();
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            showWarning("Please enter a student name or ID to search");
            return;
        }

        List<Student> students = studentRepository.searchByName(searchTerm);

        if (students.isEmpty()) {
            showWarning("No students found matching: " + searchTerm);
            return;
        }

        if (students.size() == 1) {
            selectStudent(students.get(0));
        } else {
            showStudentSelectionDialog(students);
        }
    }

    private void selectStudent(Student student) {
        this.selectedStudent = student;
        studentNameLabel.setText(student.getFirstName() + " " + student.getLastName());
        studentIdField.setText(student.getStudentId());
        currentGradeField.setText(student.getGradeLevel());
    }

    private void showStudentSelectionDialog(List<Student> students) {
        ChoiceDialog<Student> dialog = new ChoiceDialog<>(students.get(0), students);
        dialog.setTitle("Select Student");
        dialog.setHeaderText("Multiple students found");
        dialog.setContentText("Select a student:");

        dialog.showAndWait().ifPresent(this::selectStudent);
    }

    @FXML
    private void startRecordsPreparation() {
        if (currentTransferOut == null) {
            showWarning("Please save the transfer out record first");
            return;
        }

        try {
            currentTransferOut = transferOutService.startRecordsPreparation(
                    currentTransferOut.getId(), 1L);

            statusLabel.setText(currentTransferOut.getStatus().getDisplayName());
            showSuccess("Records preparation started");
        } catch (Exception e) {
            log.error("Error starting records preparation", e);
            showError("Failed to start records preparation: " + e.getMessage());
        }
    }

    @FXML
    private void markReadyToSend() {
        if (currentTransferOut == null) {
            showWarning("Please save the transfer out record first");
            return;
        }

        try {
            currentTransferOut = transferOutService.markReadyToSend(
                    currentTransferOut.getId(), 1L);

            statusLabel.setText(currentTransferOut.getStatus().getDisplayName());
            showSuccess("Marked as ready to send");
        } catch (Exception e) {
            log.error("Error marking ready to send", e);
            showError("Failed to mark ready: " + e.getMessage());
        }
    }

    @FXML
    private void recordSent() {
        if (currentTransferOut == null) {
            showWarning("Please save the transfer out record first");
            return;
        }

        if (transmissionMethod.getValue() == null) {
            showWarning("Please select a transmission method");
            return;
        }

        try {
            TransmissionMethod method = TransmissionMethod.valueOf(transmissionMethod.getValue());

            currentTransferOut = transferOutService.sendRecords(
                    currentTransferOut.getId(), method, trackingNumber.getText(), 1L);

            statusLabel.setText(currentTransferOut.getStatus().getDisplayName());
            sentDate.setValue(LocalDate.now());
            showSuccess("Records marked as sent");
        } catch (Exception e) {
            log.error("Error recording sent", e);
            showError("Failed to record sent: " + e.getMessage());
        }
    }

    @FXML
    private void selectAllDocuments() {
        List.of(transcriptIncluded, attendanceRecordIncluded, disciplineRecordIncluded,
                immunizationRecordIncluded, healthRecordIncluded, iepIncluded,
                plan504Included, testScoresIncluded, cumulativeFileIncluded,
                birthCertificateIncluded, withdrawalFormIncluded, parentRequestIncluded)
                .forEach(cb -> {
                    if (cb != null) cb.setSelected(true);
                });
        updateDocumentsCount();
        isDirty = true;
    }

    @FXML
    private void clearAllDocuments() {
        List.of(transcriptIncluded, attendanceRecordIncluded, disciplineRecordIncluded,
                immunizationRecordIncluded, healthRecordIncluded, iepIncluded,
                plan504Included, testScoresIncluded, cumulativeFileIncluded,
                birthCertificateIncluded, withdrawalFormIncluded, parentRequestIncluded)
                .forEach(cb -> {
                    if (cb != null) cb.setSelected(false);
                });
        updateDocumentsCount();
        isDirty = true;
    }

    private void updateDocumentsCount() {
        long count = List.of(transcriptIncluded, attendanceRecordIncluded, disciplineRecordIncluded,
                immunizationRecordIncluded, healthRecordIncluded, iepIncluded,
                plan504Included, testScoresIncluded, cumulativeFileIncluded,
                birthCertificateIncluded, withdrawalFormIncluded, parentRequestIncluded)
                .stream()
                .filter(cb -> cb != null && cb.isSelected())
                .count();

        documentsIncludedCount.setText(count + " / 12");
    }

    @FXML
    private void save() {
        if (selectedStudent == null) {
            showWarning("Please select a student first");
            return;
        }

        if (!validateForm()) return;

        try {
            if (currentTransferOut == null) {
                // Create new
                currentTransferOut = transferOutService.createTransferOut(
                        selectedStudent.getId(),
                        destinationSchoolName.getText(),
                        requestDate.getValue(),
                        SecurityContext.getCurrentStaffId()
                );
            }

            // Update with form data
            saveFormData();
            currentTransferOut = transferOutService.updateTransferOut(
                    currentTransferOut, SecurityContext.getCurrentStaffId());

            transferOutNumberField.setText(currentTransferOut.getTransferOutNumber());
            showSuccess("Transfer out documentation saved");
            isDirty = false;
        } catch (Exception e) {
            log.error("Error saving transfer out documentation", e);
            showError("Failed to save: " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        if (isDirty) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Unsaved Changes");
            alert.setHeaderText("You have unsaved changes");
            alert.setContentText("Do you want to close without saving?");

            if (alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }
        }
        closeForm();
    }

    private void saveFormData() {
        if (currentTransferOut == null) return;

        currentTransferOut.setCurrentGradeLevel(currentGradeField.getText());
        currentTransferOut.setCurrentHomeroom(currentHomeroomField.getText());
        currentTransferOut.setLastAttendanceDate(lastAttendanceDate.getValue());

        currentTransferOut.setRequestDate(requestDate.getValue());
        currentTransferOut.setExpectedTransferDate(expectedTransferDate.getValue());
        currentTransferOut.setActualTransferDate(actualTransferDate.getValue());

        if (transferReason.getValue() != null) {
            currentTransferOut.setTransferReason(TransferOutReason.valueOf(transferReason.getValue()));
        }

        // Destination school
        currentTransferOut.setDestinationSchoolName(destinationSchoolName.getText());
        currentTransferOut.setDestinationSchoolDistrict(destinationSchoolDistrict.getText());
        currentTransferOut.setDestinationSchoolCity(destinationCity.getText());
        currentTransferOut.setDestinationSchoolState(destinationState.getValue());
        currentTransferOut.setDestinationSchoolZip(destinationZip.getText());
        currentTransferOut.setDestinationSchoolPhone(destinationPhone.getText());
        currentTransferOut.setDestinationSchoolEmail(destinationEmail.getText());
        currentTransferOut.setDestinationSchoolFax(destinationFax.getText());
        currentTransferOut.setDestinationContactPerson(destinationContactPerson.getText());
        currentTransferOut.setDestinationContactEmail(destinationContactEmail.getText());
        currentTransferOut.setDestinationContactPhone(destinationContactPhone.getText());

        // Documents - map UI checkboxes to entity fields
        currentTransferOut.setTranscriptIncluded(transcriptIncluded.isSelected());
        currentTransferOut.setAttendanceRecordsIncluded(attendanceRecordIncluded.isSelected());
        currentTransferOut.setDisciplineRecordsIncluded(disciplineRecordIncluded.isSelected());
        currentTransferOut.setImmunizationRecordsIncluded(immunizationRecordIncluded.isSelected());
        currentTransferOut.setHealthRecordsIncluded(healthRecordIncluded.isSelected());
        currentTransferOut.setIepIncluded(iepIncluded.isSelected());
        currentTransferOut.setPlan504Included(plan504Included.isSelected());
        currentTransferOut.setTestScoresIncluded(testScoresIncluded.isSelected());
        currentTransferOut.setCumulativeFolderIncluded(cumulativeFileIncluded.isSelected());
        currentTransferOut.setSpecialEducationRecordsIncluded(birthCertificateIncluded.isSelected());
        currentTransferOut.setCounselingRecordsIncluded(withdrawalFormIncluded.isSelected());
        currentTransferOut.setAthleticEligibilityIncluded(parentRequestIncluded.isSelected());

        // Transmission
        if (transmissionMethod.getValue() != null) {
            currentTransferOut.setTransmissionMethod(TransmissionMethod.valueOf(transmissionMethod.getValue()));
        }
        currentTransferOut.setSentDate(sentDate.getValue());
        currentTransferOut.setTrackingNumber(trackingNumber.getText());

        // Acknowledgment
        currentTransferOut.setParentConsentObtained(parentConsentObtained.isSelected());
        currentTransferOut.setAcknowledgedBy(acknowledgedBy.getText());
        currentTransferOut.setAcknowledgmentDate(acknowledgmentDate.getValue());
        currentTransferOut.setAcknowledgmentNotes(acknowledgmentNotes.getText());

        currentTransferOut.setAdministrativeNotes(administrativeNotes.getText());
    }

    private boolean validateForm() {
        if (selectedStudent == null) {
            showWarning("Please select a student");
            return false;
        }

        if (destinationSchoolName.getText() == null || destinationSchoolName.getText().trim().isEmpty()) {
            showWarning("Please enter destination school name");
            return false;
        }

        if (requestDate.getValue() == null) {
            showWarning("Please select request date");
            return false;
        }

        return true;
    }

    private void closeForm() {
        Stage stage = (Stage) transferOutNumberField.getScene().getWindow();
        stage.close();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
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

    public void loadTransferOut(Long transferOutId) {
        currentTransferOut = transferOutService.getTransferOutById(transferOutId);
        loadFormData();
    }

    private void loadFormData() {
        if (currentTransferOut == null) return;

        transferOutNumberField.setText(currentTransferOut.getTransferOutNumber());
        statusLabel.setText(currentTransferOut.getStatus().getDisplayName());

        if (currentTransferOut.getStudent() != null) {
            selectStudent(currentTransferOut.getStudent());
        }

        currentGradeField.setText(currentTransferOut.getCurrentGradeLevel());
        currentHomeroomField.setText(currentTransferOut.getCurrentHomeroom());
        lastAttendanceDate.setValue(currentTransferOut.getLastAttendanceDate());

        requestDate.setValue(currentTransferOut.getRequestDate());
        expectedTransferDate.setValue(currentTransferOut.getExpectedTransferDate());
        actualTransferDate.setValue(currentTransferOut.getActualTransferDate());

        if (currentTransferOut.getTransferReason() != null) {
            transferReason.setValue(currentTransferOut.getTransferReason().name());
        }

        // Destination school
        destinationSchoolName.setText(currentTransferOut.getDestinationSchoolName());
        destinationSchoolDistrict.setText(currentTransferOut.getDestinationSchoolDistrict());
        destinationCity.setText(currentTransferOut.getDestinationSchoolCity());
        destinationState.setValue(currentTransferOut.getDestinationSchoolState());
        destinationZip.setText(currentTransferOut.getDestinationSchoolZip());
        destinationPhone.setText(currentTransferOut.getDestinationSchoolPhone());
        destinationEmail.setText(currentTransferOut.getDestinationSchoolEmail());
        destinationFax.setText(currentTransferOut.getDestinationSchoolFax());
        destinationContactPerson.setText(currentTransferOut.getDestinationContactPerson());
        destinationContactEmail.setText(currentTransferOut.getDestinationContactEmail());
        destinationContactPhone.setText(currentTransferOut.getDestinationContactPhone());

        // Documents - map entity fields to UI checkboxes
        transcriptIncluded.setSelected(Boolean.TRUE.equals(currentTransferOut.getTranscriptIncluded()));
        attendanceRecordIncluded.setSelected(Boolean.TRUE.equals(currentTransferOut.getAttendanceRecordsIncluded()));
        disciplineRecordIncluded.setSelected(Boolean.TRUE.equals(currentTransferOut.getDisciplineRecordsIncluded()));
        immunizationRecordIncluded.setSelected(Boolean.TRUE.equals(currentTransferOut.getImmunizationRecordsIncluded()));
        healthRecordIncluded.setSelected(Boolean.TRUE.equals(currentTransferOut.getHealthRecordsIncluded()));
        iepIncluded.setSelected(Boolean.TRUE.equals(currentTransferOut.getIepIncluded()));
        plan504Included.setSelected(Boolean.TRUE.equals(currentTransferOut.getPlan504Included()));
        testScoresIncluded.setSelected(Boolean.TRUE.equals(currentTransferOut.getTestScoresIncluded()));
        cumulativeFileIncluded.setSelected(Boolean.TRUE.equals(currentTransferOut.getCumulativeFolderIncluded()));
        birthCertificateIncluded.setSelected(Boolean.TRUE.equals(currentTransferOut.getSpecialEducationRecordsIncluded()));
        withdrawalFormIncluded.setSelected(Boolean.TRUE.equals(currentTransferOut.getCounselingRecordsIncluded()));
        parentRequestIncluded.setSelected(Boolean.TRUE.equals(currentTransferOut.getAthleticEligibilityIncluded()));

        updateDocumentsCount();

        // Transmission
        if (currentTransferOut.getTransmissionMethod() != null) {
            transmissionMethod.setValue(currentTransferOut.getTransmissionMethod().name());
        }
        sentDate.setValue(currentTransferOut.getSentDate());
        trackingNumber.setText(currentTransferOut.getTrackingNumber());

        // Acknowledgment
        parentConsentObtained.setSelected(Boolean.TRUE.equals(currentTransferOut.getParentConsentObtained()));
        acknowledgedBy.setText(currentTransferOut.getAcknowledgedBy());
        acknowledgmentDate.setValue(currentTransferOut.getAcknowledgmentDate());
        acknowledgmentNotes.setText(currentTransferOut.getAcknowledgmentNotes());

        administrativeNotes.setText(currentTransferOut.getAdministrativeNotes());

        isDirty = false;
    }
}
