package com.heronix.ui.controller;

import com.heronix.model.domain.EnrollmentVerification;
import com.heronix.model.domain.EnrollmentVerification.*;
import com.heronix.model.domain.Student;
import com.heronix.service.EnrollmentVerificationService;
import com.heronix.repository.StudentRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class EnrollmentVerificationFormController {

    @Autowired
    private EnrollmentVerificationService verificationService;

    @Autowired
    private StudentRepository studentRepository;

    @FXML private TextField verificationNumberField;
    @FXML private Label statusLabel;
    @FXML private TextField studentSearchField;
    @FXML private Label studentNameLabel;
    @FXML private TextField currentGradeField;
    @FXML private ComboBox<String> requesterType;
    @FXML private TextField requesterName;
    @FXML private TextField requesterOrganization;
    @FXML private TextField requesterEmail;
    @FXML private TextField requesterPhone;
    @FXML private ComboBox<String> purpose;
    @FXML private TextArea purposeDetails;
    @FXML private ComboBox<String> deliveryMethod;
    @FXML private TextField deliveryEmail;

    private Student selectedStudent;
    private EnrollmentVerification currentVerification;

    public void initialize() {
        log.info("Initializing EnrollmentVerificationFormController");
        populateDropdowns();
    }

    private void populateDropdowns() {
        requesterType.getItems().addAll("STUDENT", "PARENT_GUARDIAN", "SCHOOL", "GOVERNMENT_AGENCY",
                "INSURANCE_COMPANY", "EMPLOYER", "OTHER");
        purpose.getItems().addAll("COLLEGE_APPLICATION", "SCHOLARSHIP", "STUDENT_LOAN", "INSURANCE",
                "VISA_IMMIGRATION", "EMPLOYMENT", "OTHER");
        deliveryMethod.getItems().addAll("EMAIL", "POSTAL_MAIL", "PICKUP", "FAX");
    }

    @FXML
    private void searchStudent() {
        String query = studentSearchField.getText();
        if (query == null || query.trim().isEmpty()) {
            showWarning("Please enter a student name or ID to search.");
            return;
        }

        new Thread(() -> {
            try {
                List<Student> results = studentRepository.searchByName(query.trim());
                if (results.isEmpty()) {
                    var byId = studentRepository.findByStudentId(query.trim());
                    if (byId.isPresent()) {
                        results = List.of(byId.get());
                    }
                }

                final List<Student> found = results;
                Platform.runLater(() -> {
                    if (found.isEmpty()) {
                        showWarning("No student found matching: " + query);
                        return;
                    }

                    if (found.size() == 1) {
                        selectStudent(found.get(0));
                    } else {
                        ChoiceDialog<String> dialog = new ChoiceDialog<>();
                        dialog.setTitle("Select Student");
                        dialog.setHeaderText("Multiple students found. Please select:");
                        for (Student s : found) {
                            dialog.getItems().add(s.getStudentId() + " - " + s.getFullName());
                        }
                        dialog.setSelectedItem(dialog.getItems().get(0));
                        dialog.showAndWait().ifPresent(choice -> {
                            String selectedId = choice.split(" - ")[0];
                            found.stream()
                                    .filter(s -> s.getStudentId().equals(selectedId))
                                    .findFirst()
                                    .ifPresent(this::selectStudent);
                        });
                    }
                });
            } catch (Exception e) {
                log.error("Student search failed", e);
                Platform.runLater(() -> showError("Search failed: " + e.getMessage()));
            }
        }).start();
    }

    private void selectStudent(Student student) {
        selectedStudent = student;
        studentNameLabel.setText(student.getFullName());
        currentGradeField.setText(student.getGradeLevel() != null ? student.getGradeLevel() : "");
        log.info("Selected student: {} ({})", student.getFullName(), student.getStudentId());
    }

    @FXML
    private void verifyEnrollment() {
        if (currentVerification == null) {
            showWarning("Please save the verification request first before verifying.");
            return;
        }

        try {
            currentVerification = verificationService.verifyEnrollment(
                    currentVerification.getId(), 1L);
            statusLabel.setText(currentVerification.getStatus().name());
            showInfo("Enrollment verified successfully.");
            log.info("Verified enrollment: {}", currentVerification.getVerificationNumber());
        } catch (Exception e) {
            log.error("Verification failed", e);
            showError("Verification failed: " + e.getMessage());
        }
    }

    @FXML
    private void generateDocument() {
        if (currentVerification == null) {
            showWarning("Please save and verify the request first.");
            return;
        }

        try {
            String docPath = "./exports/verification_" + currentVerification.getVerificationNumber() + ".pdf";
            currentVerification = verificationService.generateDocument(
                    currentVerification.getId(), docPath, 1L);
            statusLabel.setText(currentVerification.getStatus().name());
            showInfo("Document generated: " + docPath);
            log.info("Generated document for: {}", currentVerification.getVerificationNumber());
        } catch (Exception e) {
            log.error("Document generation failed", e);
            showError("Document generation failed: " + e.getMessage());
        }
    }

    @FXML
    private void save() {
        if (selectedStudent == null) {
            showWarning("Please search and select a student first.");
            return;
        }

        String purposeVal = purpose.getValue();
        if (purposeVal == null) {
            showWarning("Please select a verification purpose.");
            return;
        }

        try {
            VerificationPurpose vp = VerificationPurpose.valueOf(purposeVal);

            if (currentVerification == null) {
                currentVerification = verificationService.createVerification(
                        selectedStudent.getId(), vp, 1L);
                verificationNumberField.setText(currentVerification.getVerificationNumber());
                statusLabel.setText(currentVerification.getStatus().name());
                updateRequesterInfo();
                showInfo("Verification saved: " + currentVerification.getVerificationNumber());
                log.info("Created verification: {}", currentVerification.getVerificationNumber());
            } else {
                updateRequesterInfo();
                currentVerification = verificationService.updateVerification(currentVerification, 1L);
                statusLabel.setText(currentVerification.getStatus().name());
                showInfo("Verification updated.");
            }
        } catch (Exception e) {
            log.error("Save failed", e);
            showError("Save failed: " + e.getMessage());
        }
    }

    private void updateRequesterInfo() {
        if (currentVerification != null) {
            if (requesterType.getValue() != null) {
                currentVerification.setRequesterType(
                        RequesterType.valueOf(requesterType.getValue()));
            }
            currentVerification.setRequesterName(requesterName.getText());
            currentVerification.setRequesterOrganization(requesterOrganization.getText());
            currentVerification.setRequesterEmail(requesterEmail.getText());
            currentVerification.setRequesterPhone(requesterPhone.getText());
            if (purposeDetails.getText() != null && !purposeDetails.getText().isEmpty()) {
                currentVerification.setPurposeDetails(purposeDetails.getText());
            }
            if (deliveryMethod.getValue() != null) {
                currentVerification.setDeliveryMethod(
                        DeliveryMethod.valueOf(deliveryMethod.getValue()));
            }
            currentVerification.setDeliveryEmail(deliveryEmail.getText());
        }
    }

    @FXML
    private void cancel() {
        log.info("Cancel clicked");
        selectedStudent = null;
        currentVerification = null;
        studentSearchField.clear();
        studentNameLabel.setText("No student selected");
        currentGradeField.clear();
        verificationNumberField.clear();
        statusLabel.setText("DRAFT");
        requesterType.setValue(null);
        requesterName.clear();
        requesterOrganization.clear();
        requesterEmail.clear();
        requesterPhone.clear();
        purpose.setValue(null);
        purposeDetails.clear();
        deliveryMethod.setValue(null);
        deliveryEmail.clear();
    }

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
