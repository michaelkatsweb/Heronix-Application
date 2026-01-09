package com.heronix.ui.controller;

import com.heronix.model.domain.EnrollmentVerification;
import com.heronix.model.domain.EnrollmentVerification.*;
import com.heronix.model.domain.Student;
import com.heronix.service.EnrollmentVerificationService;
import com.heronix.repository.StudentRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
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
        showInfo("Student search not yet implemented");
    }

    @FXML
    private void verifyEnrollment() {
        showInfo("Enrollment verification logic not yet implemented");
    }

    @FXML
    private void generateDocument() {
        showInfo("Document generation not yet implemented");
    }

    @FXML
    private void save() {
        showInfo("Save functionality not yet implemented");
    }

    @FXML
    private void cancel() {
        log.info("Cancel clicked");
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
