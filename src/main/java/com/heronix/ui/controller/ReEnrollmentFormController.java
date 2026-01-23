package com.heronix.ui.controller;

import com.heronix.model.domain.ReEnrollment;
import com.heronix.model.domain.ReEnrollment.ReEnrollmentStatus;
import com.heronix.model.domain.ReEnrollment.ApprovalDecision;
import com.heronix.model.domain.ReEnrollment.WithdrawalReason;
import com.heronix.model.domain.Student;
import com.heronix.repository.StudentRepository;
import com.heronix.security.SecurityContext;
import com.heronix.service.ReEnrollmentService;
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
 * Controller for Re-Enrollment Form
 * Handles returning students who previously withdrew
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Slf4j
@Component
public class ReEnrollmentFormController {

    @Autowired
    private ReEnrollmentService reEnrollmentService;

    @Autowired
    private StudentRepository studentRepository;

    // Header
    @FXML private TextField reEnrollmentNumberField;
    @FXML private Label statusLabel;
    @FXML private Button submitForReviewButton;
    @FXML private Button approveButton;
    @FXML private Button rejectButton;

    // Section 1: Student Information
    @FXML private TextField studentSearchField;
    @FXML private Label studentNameLabel;
    @FXML private TextField studentIdField;
    @FXML private TextField studentDOBField;

    // Section 2: Re-Enrollment Details
    @FXML private DatePicker applicationDate;
    @FXML private DatePicker intendedEnrollmentDate;
    @FXML private ComboBox<String> requestedGradeLevel;
    @FXML private ComboBox<String> assignedGradeLevel;

    // Section 3: Previous Enrollment History
    @FXML private DatePicker previousEnrollmentDate;
    @FXML private DatePicker previousWithdrawalDate;
    @FXML private ComboBox<String> previousGradeLevel;
    @FXML private ComboBox<String> previousWithdrawalReason;
    @FXML private DatePicker firstEnrollmentDate;
    @FXML private TextField totalPreviousEnrollments;
    @FXML private TextArea previousWithdrawalDetails;

    // Section 4: Returning Student Assessment
    @FXML private CheckBox hasFinancialObligations;
    @FXML private CheckBox hasDisciplinaryIssues;
    @FXML private CheckBox requiresSpecialServices;
    @FXML private CheckBox hasAcademicConcerns;
    @FXML private TextArea financialObligationsNotes;
    @FXML private TextArea disciplinaryHistory;
    @FXML private TextArea specialServicesNotes;

    // Section 5: Counselor Review
    @FXML private TextField assignedCounselorField;
    @FXML private ToggleGroup counselorDecisionGroup;
    @FXML private RadioButton counselorApprove;
    @FXML private RadioButton counselorReject;
    @FXML private RadioButton counselorRecommendWithConditions;
    @FXML private TextArea counselorRecommendation;
    @FXML private TextField counselorReviewDate;

    // Section 6: Principal Approval
    @FXML private TextField approvedByField;
    @FXML private ToggleGroup principalDecisionGroup;
    @FXML private RadioButton principalApprove;
    @FXML private RadioButton principalReject;
    @FXML private TextArea approvalNotes;
    @FXML private TextField approvalDate;
    @FXML private TextArea rejectionReason;

    // Section 7: Administrative Notes
    @FXML private TextArea administrativeNotes;

    // Section 8: Audit Information
    @FXML private Label createdByLabel;
    @FXML private Label createdAtLabel;
    @FXML private Label updatedByLabel;
    @FXML private Label updatedAtLabel;

    private ReEnrollment currentReEnrollment;
    private Student selectedStudent;
    private boolean isDirty = false;

    @FXML
    public void initialize() {
        log.info("Initializing ReEnrollmentFormController");

        populateGradeLevels();
        populateWithdrawalReasons();
        setupListeners();
        createNewReEnrollment();

        log.info("ReEnrollmentFormController initialized successfully");
    }

    private void populateGradeLevels() {
        List<String> grades = List.of(
                "Kindergarten", "1st Grade", "2nd Grade", "3rd Grade", "4th Grade", "5th Grade",
                "6th Grade", "7th Grade", "8th Grade", "9th Grade", "10th Grade", "11th Grade", "12th Grade"
        );
        if (requestedGradeLevel != null) requestedGradeLevel.getItems().addAll(grades);
        if (assignedGradeLevel != null) assignedGradeLevel.getItems().addAll(grades);
        if (previousGradeLevel != null) previousGradeLevel.getItems().addAll(grades);
    }

    private void populateWithdrawalReasons() {
        if (previousWithdrawalReason == null) return;
        previousWithdrawalReason.getItems().addAll(
                "TRANSFERRED", "MOVED", "HOMESCHOOL", "PRIVATE_SCHOOL",
                "EXPELLED", "DROPPED_OUT", "GRADUATED", "OTHER"
        );
    }

    private void setupListeners() {
        // Mark form as dirty when fields change
        if (requestedGradeLevel != null) {
            requestedGradeLevel.setOnAction(e -> isDirty = true);
        }
    }

    private void createNewReEnrollment() {
        // Don't create database record on initialization
        // Wait for user to select student and save
        applicationDate.setValue(LocalDate.now());
        statusLabel.setText("DRAFT");
    }

    @FXML
    private void searchStudent() {
        String searchTerm = studentSearchField.getText();
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            showWarning("Please enter a student name or ID to search");
            return;
        }

        // Search for student
        List<Student> students = studentRepository.searchByName(searchTerm);

        if (students.isEmpty()) {
            showWarning("No students found matching: " + searchTerm);
            return;
        }

        if (students.size() == 1) {
            selectStudent(students.get(0));
        } else {
            // Show selection dialog if multiple matches
            showStudentSelectionDialog(students);
        }
    }

    private void selectStudent(Student student) {
        this.selectedStudent = student;
        studentNameLabel.setText(student.getFirstName() + " " + student.getLastName());
        studentIdField.setText(student.getStudentId());
        studentDOBField.setText(student.getDateOfBirth() != null ?
                student.getDateOfBirth().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : "");

        // Load previous enrollment history if available
        loadPreviousEnrollmentHistory(student);
    }

    private void loadPreviousEnrollmentHistory(Student student) {
        // Get most recent re-enrollment or withdrawal for this student
        List<ReEnrollment> previousReEnrollments = reEnrollmentService.getByStudent(student.getId());

        if (!previousReEnrollments.isEmpty()) {
            ReEnrollment latest = previousReEnrollments.get(0);
            if (previousEnrollmentDate != null) previousEnrollmentDate.setValue(latest.getPreviousEnrollmentDate());
            if (previousWithdrawalDate != null) previousWithdrawalDate.setValue(latest.getPreviousWithdrawalDate());
            if (previousGradeLevel != null && latest.getPreviousGradeLevel() != null) {
                previousGradeLevel.setValue(latest.getPreviousGradeLevel());
            }
            if (totalPreviousEnrollments != null && latest.getTotalPreviousEnrollments() != null) {
                totalPreviousEnrollments.setText(String.valueOf(latest.getTotalPreviousEnrollments() + 1));
            }
        }
    }

    private void showStudentSelectionDialog(List<Student> students) {
        // Create a simple selection dialog
        ChoiceDialog<Student> dialog = new ChoiceDialog<>(students.get(0), students);
        dialog.setTitle("Select Student");
        dialog.setHeaderText("Multiple students found");
        dialog.setContentText("Select a student:");

        dialog.showAndWait().ifPresent(this::selectStudent);
    }

    @FXML
    private void submitForReview() {
        if (!validateForm()) return;

        try {
            if (currentReEnrollment == null) {
                // Create new re-enrollment record
                currentReEnrollment = reEnrollmentService.createReEnrollment(
                        selectedStudent.getId(),
                        requestedGradeLevel.getValue(),
                        intendedEnrollmentDate.getValue(),
                        SecurityContext.getCurrentStaffId()
                );
            }

            // Submit for approval
            currentReEnrollment = reEnrollmentService.submitForApproval(
                    currentReEnrollment.getId(), SecurityContext.getCurrentStaffId());

            statusLabel.setText(currentReEnrollment.getStatus().getDisplayName());
            showSuccess("Re-enrollment submitted for review");
            isDirty = false;
        } catch (Exception e) {
            log.error("Error submitting re-enrollment for review", e);
            showError("Failed to submit for review: " + e.getMessage());
        }
    }

    @FXML
    private void approveReEnrollment() {
        if (currentReEnrollment == null) {
            showWarning("Please save the re-enrollment first");
            return;
        }

        try {
            currentReEnrollment = reEnrollmentService.principalDecision(
                    currentReEnrollment.getId(),
                    ReEnrollment.ApprovalDecision.APPROVED,
                    approvalNotes.getText(),
                    SecurityContext.getCurrentStaffId());

            statusLabel.setText(currentReEnrollment.getStatus().getDisplayName());
            showSuccess("Re-enrollment approved");
            isDirty = false;
        } catch (Exception e) {
            log.error("Error approving re-enrollment", e);
            showError("Failed to approve: " + e.getMessage());
        }
    }

    @FXML
    private void rejectReEnrollment() {
        if (currentReEnrollment == null) {
            showWarning("Please save the re-enrollment first");
            return;
        }

        String reason = rejectionReason.getText();
        if (reason == null || reason.trim().isEmpty()) {
            showWarning("Please provide a rejection reason");
            return;
        }

        try {
            currentReEnrollment = reEnrollmentService.rejectApplication(
                    currentReEnrollment.getId(), reason, SecurityContext.getCurrentStaffId());

            statusLabel.setText(currentReEnrollment.getStatus().getDisplayName());
            showSuccess("Re-enrollment rejected");
            isDirty = false;
        } catch (Exception e) {
            log.error("Error rejecting re-enrollment", e);
            showError("Failed to reject: " + e.getMessage());
        }
    }

    @FXML
    private void saveDraft() {
        if (selectedStudent == null) {
            showWarning("Please select a student first");
            return;
        }

        if (!validateForm()) return;

        try {
            if (currentReEnrollment == null) {
                // Create new
                currentReEnrollment = reEnrollmentService.createReEnrollment(
                        selectedStudent.getId(),
                        requestedGradeLevel.getValue(),
                        intendedEnrollmentDate.getValue(),
                        SecurityContext.getCurrentStaffId()
                );
            } else {
                // Update existing
                saveFormData();
                currentReEnrollment = reEnrollmentService.updateReEnrollment(
                        currentReEnrollment, SecurityContext.getCurrentStaffId());
            }

            reEnrollmentNumberField.setText(currentReEnrollment.getReEnrollmentNumber());
            showSuccess("Re-enrollment saved");
            isDirty = false;
        } catch (Exception e) {
            log.error("Error saving re-enrollment", e);
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
        if (currentReEnrollment == null) return;

        currentReEnrollment.setRequestedGradeLevel(requestedGradeLevel.getValue());
        currentReEnrollment.setAssignedGradeLevel(assignedGradeLevel.getValue());
        currentReEnrollment.setIntendedEnrollmentDate(intendedEnrollmentDate.getValue());

        currentReEnrollment.setPreviousEnrollmentDate(previousEnrollmentDate.getValue());
        currentReEnrollment.setPreviousWithdrawalDate(previousWithdrawalDate.getValue());
        currentReEnrollment.setPreviousGradeLevel(previousGradeLevel.getValue());

        if (previousWithdrawalReason.getValue() != null) {
            currentReEnrollment.setPreviousWithdrawalReason(
                    WithdrawalReason.valueOf(previousWithdrawalReason.getValue()));
        }

        currentReEnrollment.setPreviousWithdrawalDetails(previousWithdrawalDetails.getText());
        currentReEnrollment.setFirstEnrollmentDate(firstEnrollmentDate.getValue());

        if (totalPreviousEnrollments.getText() != null && !totalPreviousEnrollments.getText().isEmpty()) {
            currentReEnrollment.setTotalPreviousEnrollments(
                    Integer.parseInt(totalPreviousEnrollments.getText()));
        }

        // Map UI checkboxes to entity fields
        currentReEnrollment.setHasOutstandingFees(hasFinancialObligations.isSelected());
        currentReEnrollment.setDisciplineRecordsReviewed(hasDisciplinaryIssues.isSelected());
        currentReEnrollment.setSpecialEducationReview(requiresSpecialServices.isSelected());
        currentReEnrollment.setAcademicInterviewRequired(hasAcademicConcerns.isSelected());

        // Combine notes into appropriate fields
        String specialCircumstances = "";
        if (financialObligationsNotes.getText() != null && !financialObligationsNotes.getText().isEmpty()) {
            specialCircumstances += "Financial Obligations: " + financialObligationsNotes.getText() + "\n";
        }
        if (disciplinaryHistory.getText() != null && !disciplinaryHistory.getText().isEmpty()) {
            specialCircumstances += "Disciplinary History: " + disciplinaryHistory.getText() + "\n";
        }
        if (!specialCircumstances.isEmpty()) {
            currentReEnrollment.setSpecialCircumstances(specialCircumstances);
        }

        currentReEnrollment.setAcademicPlanDetails(specialServicesNotes.getText());
        currentReEnrollment.setAdministrativeNotes(administrativeNotes.getText());
    }

    private boolean validateForm() {
        if (selectedStudent == null) {
            showWarning("Please select a student");
            return false;
        }

        if (requestedGradeLevel.getValue() == null) {
            showWarning("Please select a requested grade level");
            return false;
        }

        return true;
    }

    private void closeForm() {
        Stage stage = (Stage) reEnrollmentNumberField.getScene().getWindow();
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

    public void loadReEnrollment(Long reEnrollmentId) {
        currentReEnrollment = reEnrollmentService.getReEnrollmentById(reEnrollmentId);
        loadFormData();
    }

    private void loadFormData() {
        if (currentReEnrollment == null) return;

        reEnrollmentNumberField.setText(currentReEnrollment.getReEnrollmentNumber());
        statusLabel.setText(currentReEnrollment.getStatus().getDisplayName());

        if (currentReEnrollment.getStudent() != null) {
            selectStudent(currentReEnrollment.getStudent());
        }

        applicationDate.setValue(currentReEnrollment.getApplicationDate());
        intendedEnrollmentDate.setValue(currentReEnrollment.getIntendedEnrollmentDate());
        requestedGradeLevel.setValue(currentReEnrollment.getRequestedGradeLevel());
        assignedGradeLevel.setValue(currentReEnrollment.getAssignedGradeLevel());

        previousEnrollmentDate.setValue(currentReEnrollment.getPreviousEnrollmentDate());
        previousWithdrawalDate.setValue(currentReEnrollment.getPreviousWithdrawalDate());
        previousGradeLevel.setValue(currentReEnrollment.getPreviousGradeLevel());

        if (currentReEnrollment.getPreviousWithdrawalReason() != null) {
            previousWithdrawalReason.setValue(currentReEnrollment.getPreviousWithdrawalReason().name());
        }

        previousWithdrawalDetails.setText(currentReEnrollment.getPreviousWithdrawalDetails());
        firstEnrollmentDate.setValue(currentReEnrollment.getFirstEnrollmentDate());

        if (currentReEnrollment.getTotalPreviousEnrollments() != null) {
            totalPreviousEnrollments.setText(String.valueOf(currentReEnrollment.getTotalPreviousEnrollments()));
        }

        // Map entity fields to UI checkboxes
        hasFinancialObligations.setSelected(Boolean.TRUE.equals(currentReEnrollment.getHasOutstandingFees()));
        hasDisciplinaryIssues.setSelected(Boolean.TRUE.equals(currentReEnrollment.getDisciplineRecordsReviewed()));
        requiresSpecialServices.setSelected(Boolean.TRUE.equals(currentReEnrollment.getSpecialEducationReview()));
        hasAcademicConcerns.setSelected(Boolean.TRUE.equals(currentReEnrollment.getAcademicInterviewRequired()));

        // Parse special circumstances back into separate fields
        String specialCircumstancesText = currentReEnrollment.getSpecialCircumstances();
        if (specialCircumstancesText != null) {
            if (specialCircumstancesText.contains("Financial Obligations:")) {
                int start = specialCircumstancesText.indexOf("Financial Obligations:") + "Financial Obligations:".length();
                int end = specialCircumstancesText.indexOf("\n", start);
                if (end == -1) end = specialCircumstancesText.length();
                financialObligationsNotes.setText(specialCircumstancesText.substring(start, end).trim());
            }
            if (specialCircumstancesText.contains("Disciplinary History:")) {
                int start = specialCircumstancesText.indexOf("Disciplinary History:") + "Disciplinary History:".length();
                int end = specialCircumstancesText.indexOf("\n", start);
                if (end == -1) end = specialCircumstancesText.length();
                disciplinaryHistory.setText(specialCircumstancesText.substring(start, end).trim());
            }
        }

        specialServicesNotes.setText(currentReEnrollment.getAcademicPlanDetails());
        administrativeNotes.setText(currentReEnrollment.getAdministrativeNotes());
        counselorRecommendation.setText(currentReEnrollment.getCounselorRecommendation());
        approvalNotes.setText(currentReEnrollment.getPrincipalNotes());
        rejectionReason.setText(currentReEnrollment.getRejectionReason());

        isDirty = false;
    }
}
