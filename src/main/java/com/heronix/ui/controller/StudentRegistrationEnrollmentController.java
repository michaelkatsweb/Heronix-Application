package com.heronix.ui.controller;

import com.heronix.model.domain.Course;
import com.heronix.model.domain.Student;
import com.heronix.service.CourseService;
import com.heronix.service.StudentService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class StudentRegistrationEnrollmentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private CourseService courseService;

    // Progress Indicators
    @FXML private Label step1Indicator;
    @FXML private Label step2Indicator;
    @FXML private Label step3Indicator;
    @FXML private Label step4Indicator;
    @FXML private Label step5Indicator;
    @FXML private ProgressBar registrationProgressBar;

    // Step Panels
    @FXML private VBox step1Panel;
    @FXML private VBox step2Panel;
    @FXML private VBox step3Panel;
    @FXML private VBox step4Panel;
    @FXML private VBox step5Panel;

    // Step 1: Student Information
    @FXML private TextField firstNameField;
    @FXML private TextField middleNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField preferredNameField;
    @FXML private DatePicker birthDatePicker;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private TextField studentIdField;
    @FXML private ComboBox<String> gradeLevelComboBox;
    @FXML private ComboBox<String> ethnicityComboBox;
    @FXML private ComboBox<String> languageComboBox;
    @FXML private TextField addressField;
    @FXML private TextField cityField;
    @FXML private ComboBox<String> stateComboBox;
    @FXML private TextField zipCodeField;
    @FXML private TextField phoneField;

    // Step 2: Guardian Information
    @FXML private TextField guardian1FirstNameField;
    @FXML private TextField guardian1LastNameField;
    @FXML private ComboBox<String> guardian1RelationshipComboBox;
    @FXML private TextField guardian1EmailField;
    @FXML private TextField guardian1CellPhoneField;
    @FXML private TextField guardian1WorkPhoneField;
    @FXML private TextField guardian1EmployerField;
    @FXML private CheckBox guardian1CanPickupCheckBox;
    @FXML private CheckBox guardian1EmergencyContactCheckBox;

    @FXML private CheckBox addGuardian2CheckBox;
    @FXML private GridPane guardian2Grid;
    @FXML private TextField guardian2FirstNameField;
    @FXML private TextField guardian2LastNameField;
    @FXML private ComboBox<String> guardian2RelationshipComboBox;
    @FXML private TextField guardian2EmailField;
    @FXML private TextField guardian2CellPhoneField;

    // Step 3: Course Enrollment
    @FXML private TextField courseSearchField;
    @FXML private TableView<Course> availableCoursesTable;
    @FXML private TableColumn<Course, String> courseCodeColumn;
    @FXML private TableColumn<Course, String> courseNameColumn;
    @FXML private TableColumn<Course, Integer> creditsColumn;
    @FXML private TableColumn<Course, Integer> availableSeatsColumn;
    @FXML private ListView<String> enrolledCoursesList;
    @FXML private Label totalCreditsLabel;

    // Step 4: Documents
    @FXML private CheckBox birthCertificateCheckBox;
    @FXML private CheckBox immunizationCheckBox;
    @FXML private CheckBox proofOfResidenceCheckBox;
    @FXML private CheckBox previousTranscriptCheckBox;
    @FXML private CheckBox iepDocumentCheckBox;
    @FXML private CheckBox medicalRecordsCheckBox;
    @FXML private CheckBox photoReleaseCheckBox;

    // Step 5: Review
    @FXML private TextArea reviewSummaryArea;
    @FXML private CheckBox certifyCheckBox;

    // Navigation
    @FXML private Button previousButton;
    @FXML private Button nextButton;
    @FXML private Button submitButton;

    private int currentStep = 1;
    private ObservableList<Course> availableCourses = FXCollections.observableArrayList();
    private List<Course> enrolledCourses = new ArrayList<>();
    private double totalCredits = 0;

    @FXML
    public void initialize() {
        setupComboBoxes();
        setupTableColumns();
        generateStudentId();
        loadSampleCourses();
        updateStepIndicators();
    }

    private void setupComboBoxes() {
        // Gender
        genderComboBox.setItems(FXCollections.observableArrayList(
                "Male", "Female", "Non-binary", "Prefer not to say"
        ));

        // Grade Level
        gradeLevelComboBox.setItems(FXCollections.observableArrayList(
                "Pre-K", "Kindergarten", "1st Grade", "2nd Grade", "3rd Grade",
                "4th Grade", "5th Grade", "6th Grade", "7th Grade", "8th Grade",
                "9th Grade", "10th Grade", "11th Grade", "12th Grade"
        ));

        // Ethnicity
        ethnicityComboBox.setItems(FXCollections.observableArrayList(
                "American Indian or Alaska Native",
                "Asian",
                "Black or African American",
                "Hispanic or Latino",
                "Native Hawaiian or Other Pacific Islander",
                "White",
                "Two or More Races"
        ));

        // Language
        languageComboBox.setItems(FXCollections.observableArrayList(
                "English", "Spanish", "Chinese", "French", "German", "Arabic", "Other"
        ));

        // State
        stateComboBox.setItems(FXCollections.observableArrayList(
                "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA",
                "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD",
                "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ",
                "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC",
                "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY"
        ));

        // Relationship
        ObservableList<String> relationships = FXCollections.observableArrayList(
                "Mother", "Father", "Stepmother", "Stepfather",
                "Grandmother", "Grandfather", "Aunt", "Uncle",
                "Legal Guardian", "Foster Parent", "Other"
        );
        guardian1RelationshipComboBox.setItems(relationships);
        guardian2RelationshipComboBox.setItems(relationships);
    }

    private void setupTableColumns() {
        courseCodeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCourseCode()));

        courseNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCourseName()));

        creditsColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getCredits() != null ?
                        cellData.getValue().getCredits().intValue() : 1).asObject());

        availableSeatsColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getAvailableSeats()).asObject());

        availableCoursesTable.setItems(availableCourses);

        // Search functionality
        courseSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterCourses(newVal);
        });
    }

    private void generateStudentId() {
        Random random = new Random();
        String id = "S" + (2024000 + random.nextInt(10000));
        studentIdField.setText(id);
    }

    private void loadSampleCourses() {
        String[] courses = {
                "English I", "English II", "English III", "English IV",
                "Algebra I", "Geometry", "Algebra II", "Pre-Calculus",
                "Biology", "Chemistry", "Physics", "Earth Science",
                "World History", "US History", "Government", "Economics",
                "Spanish I", "Spanish II", "French I", "French II",
                "Physical Education", "Health", "Art I", "Music"
        };

        for (int i = 0; i < courses.length; i++) {
            Course course = new Course();
            course.setId((long) (i + 1));
            course.setCourseCode(courses[i].substring(0, 3).toUpperCase() + (i + 1));
            course.setCourseName(courses[i]);
            course.setCredits(1.0);
            course.setMaxStudents(30);
            course.setCurrentEnrollment(15 + new Random().nextInt(10));
            availableCourses.add(course);
        }
    }

    private void filterCourses(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            availableCoursesTable.setItems(availableCourses);
        } else {
            String lowerSearch = searchText.toLowerCase();
            ObservableList<Course> filtered = availableCourses.filtered(course ->
                    course.getCourseCode().toLowerCase().contains(lowerSearch) ||
                            course.getCourseName().toLowerCase().contains(lowerSearch));
            availableCoursesTable.setItems(filtered);
        }
    }

    @FXML
    private void handleAddGuardian2() {
        boolean addGuardian = addGuardian2CheckBox.isSelected();
        guardian2Grid.setManaged(addGuardian);
        guardian2Grid.setVisible(addGuardian);
    }

    @FXML
    private void handleAddCourse() {
        Course selected = availableCoursesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Course Selected",
                    "Please select a course to add.");
            return;
        }

        if (enrolledCourses.contains(selected)) {
            showAlert(Alert.AlertType.WARNING, "Already Enrolled",
                    "Student is already enrolled in this course.");
            return;
        }

        enrolledCourses.add(selected);
        updateEnrolledCoursesList();
        updateTotalCredits();
    }

    @FXML
    private void handleRemoveCourse() {
        String selected = enrolledCoursesList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Course Selected",
                    "Please select a course to remove.");
            return;
        }

        // Find and remove course
        enrolledCourses.removeIf(course ->
                (course.getCourseCode() + " - " + course.getCourseName()).equals(selected));

        updateEnrolledCoursesList();
        updateTotalCredits();
    }

    private void updateEnrolledCoursesList() {
        ObservableList<String> courseStrings = FXCollections.observableArrayList();
        for (Course course : enrolledCourses) {
            courseStrings.add(course.getCourseCode() + " - " + course.getCourseName());
        }
        enrolledCoursesList.setItems(courseStrings);
    }

    private void updateTotalCredits() {
        totalCredits = enrolledCourses.stream()
                .mapToDouble(course -> course.getCredits() != null ? course.getCredits() : 1.0)
                .sum();
        totalCreditsLabel.setText(String.format("Total Credits: %.1f", totalCredits));
    }

    @FXML
    private void handleUploadDocuments() {
        showAlert(Alert.AlertType.INFORMATION, "Upload Documents",
                "Document upload functionality would be implemented here.\n\n" +
                        "Users would be able to upload scanned copies of required documents.");
    }

    @FXML
    private void handlePrevious() {
        if (currentStep > 1) {
            currentStep--;
            updateStepDisplay();
        }
    }

    @FXML
    private void handleNext() {
        // Validate current step
        List<String> errors = validateCurrentStep();
        if (!errors.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error",
                    "Please correct the following errors:\n\n" + String.join("\n", errors));
            return;
        }

        if (currentStep < 5) {
            currentStep++;
            updateStepDisplay();
        }

        // If moving to review step, generate summary
        if (currentStep == 5) {
            generateReviewSummary();
        }
    }

    @FXML
    private void handleSubmit() {
        if (!certifyCheckBox.isSelected()) {
            showAlert(Alert.AlertType.WARNING, "Certification Required",
                    "Please certify that all information is accurate before submitting.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Submit Registration");
        confirmation.setHeaderText("Confirm Registration Submission");
        confirmation.setContentText("Are you sure you want to submit this registration?\n\n" +
                "Student: " + firstNameField.getText() + " " + lastNameField.getText() + "\n" +
                "Grade: " + gradeLevelComboBox.getValue() + "\n" +
                "Courses: " + enrolledCourses.size());

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Submit registration
                submitRegistration();
            }
        });
    }

    private void submitRegistration() {
        // In production, save to database
        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Registration Complete");
        success.setHeaderText("Student Registration Successful");
        success.setContentText("Registration has been submitted successfully!\n\n" +
                "Student ID: " + studentIdField.getText() + "\n" +
                "Student: " + firstNameField.getText() + " " + lastNameField.getText() + "\n\n" +
                "An email confirmation has been sent to " + guardian1EmailField.getText());
        success.showAndWait();

        // Reset form
        resetForm();
    }

    private void resetForm() {
        currentStep = 1;
        updateStepDisplay();

        // Clear all fields
        firstNameField.clear();
        middleNameField.clear();
        lastNameField.clear();
        preferredNameField.clear();
        birthDatePicker.setValue(null);
        genderComboBox.setValue(null);
        gradeLevelComboBox.setValue(null);

        enrolledCourses.clear();
        updateEnrolledCoursesList();
        updateTotalCredits();

        generateStudentId();
    }

    private void updateStepDisplay() {
        // Hide all panels
        step1Panel.setManaged(false);
        step1Panel.setVisible(false);
        step2Panel.setManaged(false);
        step2Panel.setVisible(false);
        step3Panel.setManaged(false);
        step3Panel.setVisible(false);
        step4Panel.setManaged(false);
        step4Panel.setVisible(false);
        step5Panel.setManaged(false);
        step5Panel.setVisible(false);

        // Show current panel
        switch (currentStep) {
            case 1:
                step1Panel.setManaged(true);
                step1Panel.setVisible(true);
                break;
            case 2:
                step2Panel.setManaged(true);
                step2Panel.setVisible(true);
                break;
            case 3:
                step3Panel.setManaged(true);
                step3Panel.setVisible(true);
                break;
            case 4:
                step4Panel.setManaged(true);
                step4Panel.setVisible(true);
                break;
            case 5:
                step5Panel.setManaged(true);
                step5Panel.setVisible(true);
                break;
        }

        updateStepIndicators();
        updateNavigationButtons();
    }

    private void updateStepIndicators() {
        // Reset all indicators
        step1Indicator.setText(currentStep >= 1 ? "●" : "○");
        step1Indicator.setStyle(currentStep >= 1 ?
                "-fx-font-size: 24px; -fx-text-fill: #2196f3;" :
                "-fx-font-size: 24px; -fx-text-fill: #9e9e9e;");

        step2Indicator.setText(currentStep >= 2 ? "●" : "○");
        step2Indicator.setStyle(currentStep >= 2 ?
                "-fx-font-size: 24px; -fx-text-fill: #2196f3;" :
                "-fx-font-size: 24px; -fx-text-fill: #9e9e9e;");

        step3Indicator.setText(currentStep >= 3 ? "●" : "○");
        step3Indicator.setStyle(currentStep >= 3 ?
                "-fx-font-size: 24px; -fx-text-fill: #2196f3;" :
                "-fx-font-size: 24px; -fx-text-fill: #9e9e9e;");

        step4Indicator.setText(currentStep >= 4 ? "●" : "○");
        step4Indicator.setStyle(currentStep >= 4 ?
                "-fx-font-size: 24px; -fx-text-fill: #2196f3;" :
                "-fx-font-size: 24px; -fx-text-fill: #9e9e9e;");

        step5Indicator.setText(currentStep >= 5 ? "●" : "○");
        step5Indicator.setStyle(currentStep >= 5 ?
                "-fx-font-size: 24px; -fx-text-fill: #2196f3;" :
                "-fx-font-size: 24px; -fx-text-fill: #9e9e9e;");

        registrationProgressBar.setProgress(currentStep / 5.0);
    }

    private void updateNavigationButtons() {
        previousButton.setDisable(currentStep == 1);

        if (currentStep == 5) {
            nextButton.setManaged(false);
            nextButton.setVisible(false);
            submitButton.setManaged(true);
            submitButton.setVisible(true);
        } else {
            nextButton.setManaged(true);
            nextButton.setVisible(true);
            submitButton.setManaged(false);
            submitButton.setVisible(false);
        }
    }

    private List<String> validateCurrentStep() {
        List<String> errors = new ArrayList<>();

        switch (currentStep) {
            case 1:
                if (firstNameField.getText().trim().isEmpty()) {
                    errors.add("First name is required");
                }
                if (lastNameField.getText().trim().isEmpty()) {
                    errors.add("Last name is required");
                }
                if (birthDatePicker.getValue() == null) {
                    errors.add("Date of birth is required");
                }
                if (genderComboBox.getValue() == null) {
                    errors.add("Gender is required");
                }
                if (gradeLevelComboBox.getValue() == null) {
                    errors.add("Grade level is required");
                }
                if (addressField.getText().trim().isEmpty()) {
                    errors.add("Address is required");
                }
                if (cityField.getText().trim().isEmpty()) {
                    errors.add("City is required");
                }
                if (stateComboBox.getValue() == null) {
                    errors.add("State is required");
                }
                if (zipCodeField.getText().trim().isEmpty()) {
                    errors.add("ZIP code is required");
                }
                break;

            case 2:
                if (guardian1FirstNameField.getText().trim().isEmpty()) {
                    errors.add("Primary guardian first name is required");
                }
                if (guardian1LastNameField.getText().trim().isEmpty()) {
                    errors.add("Primary guardian last name is required");
                }
                if (guardian1RelationshipComboBox.getValue() == null) {
                    errors.add("Guardian relationship is required");
                }
                if (guardian1EmailField.getText().trim().isEmpty()) {
                    errors.add("Guardian email is required");
                }
                if (guardian1CellPhoneField.getText().trim().isEmpty()) {
                    errors.add("Guardian cell phone is required");
                }
                break;

            case 3:
                if (enrolledCourses.isEmpty()) {
                    errors.add("At least one course must be selected");
                }
                break;

            case 4:
                if (!birthCertificateCheckBox.isSelected() ||
                        !immunizationCheckBox.isSelected() ||
                        !proofOfResidenceCheckBox.isSelected()) {
                    errors.add("All required documents must be acknowledged");
                }
                break;
        }

        return errors;
    }

    private void generateReviewSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("REGISTRATION SUMMARY\n");
        summary.append("=".repeat(60)).append("\n\n");

        summary.append("STUDENT INFORMATION\n");
        summary.append("-".repeat(60)).append("\n");
        summary.append("Name: ").append(firstNameField.getText()).append(" ");
        if (!middleNameField.getText().trim().isEmpty()) {
            summary.append(middleNameField.getText()).append(" ");
        }
        summary.append(lastNameField.getText()).append("\n");

        if (!preferredNameField.getText().trim().isEmpty()) {
            summary.append("Preferred Name: ").append(preferredNameField.getText()).append("\n");
        }

        summary.append("Student ID: ").append(studentIdField.getText()).append("\n");
        summary.append("Date of Birth: ").append(
                birthDatePicker.getValue() != null ?
                        birthDatePicker.getValue().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : "N/A"
        ).append("\n");
        summary.append("Gender: ").append(genderComboBox.getValue()).append("\n");
        summary.append("Grade Level: ").append(gradeLevelComboBox.getValue()).append("\n");

        if (ethnicityComboBox.getValue() != null) {
            summary.append("Ethnicity: ").append(ethnicityComboBox.getValue()).append("\n");
        }
        if (languageComboBox.getValue() != null) {
            summary.append("Primary Language: ").append(languageComboBox.getValue()).append("\n");
        }

        summary.append("\nAddress: ").append(addressField.getText()).append("\n");
        summary.append("         ").append(cityField.getText()).append(", ");
        summary.append(stateComboBox.getValue()).append(" ").append(zipCodeField.getText()).append("\n");

        if (!phoneField.getText().trim().isEmpty()) {
            summary.append("Phone: ").append(phoneField.getText()).append("\n");
        }

        summary.append("\n\nGUARDIAN INFORMATION\n");
        summary.append("-".repeat(60)).append("\n");
        summary.append("Primary Guardian: ").append(guardian1FirstNameField.getText())
                .append(" ").append(guardian1LastNameField.getText()).append("\n");
        summary.append("Relationship: ").append(guardian1RelationshipComboBox.getValue()).append("\n");
        summary.append("Email: ").append(guardian1EmailField.getText()).append("\n");
        summary.append("Cell Phone: ").append(guardian1CellPhoneField.getText()).append("\n");

        if (!guardian1WorkPhoneField.getText().trim().isEmpty()) {
            summary.append("Work Phone: ").append(guardian1WorkPhoneField.getText()).append("\n");
        }
        if (!guardian1EmployerField.getText().trim().isEmpty()) {
            summary.append("Employer: ").append(guardian1EmployerField.getText()).append("\n");
        }

        if (addGuardian2CheckBox.isSelected() && !guardian2FirstNameField.getText().trim().isEmpty()) {
            summary.append("\nSecondary Guardian: ").append(guardian2FirstNameField.getText())
                    .append(" ").append(guardian2LastNameField.getText()).append("\n");
            if (guardian2RelationshipComboBox.getValue() != null) {
                summary.append("Relationship: ").append(guardian2RelationshipComboBox.getValue()).append("\n");
            }
            if (!guardian2EmailField.getText().trim().isEmpty()) {
                summary.append("Email: ").append(guardian2EmailField.getText()).append("\n");
            }
        }

        summary.append("\n\nCOURSE ENROLLMENT\n");
        summary.append("-".repeat(60)).append("\n");
        summary.append("Total Courses: ").append(enrolledCourses.size()).append("\n");
        summary.append("Total Credits: ").append(String.format("%.1f", totalCredits)).append("\n\n");

        for (Course course : enrolledCourses) {
            summary.append("  • ").append(course.getCourseCode())
                    .append(" - ").append(course.getCourseName())
                    .append(" (").append(course.getCredits()).append(" credits)\n");
        }

        summary.append("\n\nDOCUMENT CHECKLIST\n");
        summary.append("-".repeat(60)).append("\n");
        summary.append(birthCertificateCheckBox.isSelected() ? "✓" : "○").append(" Birth Certificate\n");
        summary.append(immunizationCheckBox.isSelected() ? "✓" : "○").append(" Immunization Records\n");
        summary.append(proofOfResidenceCheckBox.isSelected() ? "✓" : "○").append(" Proof of Residence\n");
        summary.append(previousTranscriptCheckBox.isSelected() ? "✓" : "○").append(" Previous Transcript\n");
        summary.append(iepDocumentCheckBox.isSelected() ? "✓" : "○").append(" IEP/504 Plan\n");
        summary.append(medicalRecordsCheckBox.isSelected() ? "✓" : "○").append(" Medical Records\n");
        summary.append(photoReleaseCheckBox.isSelected() ? "✓" : "○").append(" Photo Release Form\n");

        reviewSummaryArea.setText(summary.toString());
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
