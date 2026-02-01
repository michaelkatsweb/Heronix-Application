package com.heronix.ui.controller;

import com.heronix.model.domain.Course;
import com.heronix.model.domain.IncompleteRegistration;
import com.heronix.model.domain.Student;
import com.heronix.model.enums.GradeLevel;
import com.heronix.model.enums.RegistrationStatus;
import com.heronix.repository.IncompleteRegistrationRepository;
import com.heronix.security.SecurityContext;
import com.heronix.service.CourseService;
import com.heronix.service.GradeAppropriateCourseService;
import com.heronix.service.StudentEnrollmentService;
import com.heronix.service.StudentService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.extern.slf4j.Slf4j;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
public class StudentRegistrationEnrollmentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private GradeAppropriateCourseService gradeAppropriateCourseService;

    @Autowired
    private IncompleteRegistrationRepository incompleteRegistrationRepository;

    @Autowired
    private StudentEnrollmentService studentEnrollmentService;

    // Currently selected grade level for course filtering
    private GradeLevel selectedGradeLevel;

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

    // Step 4: Documents - Required
    @FXML private CheckBox birthCertificateCheckBox;
    @FXML private ComboBox<String> birthCertificateStatus;
    @FXML private CheckBox immunizationCheckBox;
    @FXML private ComboBox<String> immunizationStatus;
    @FXML private CheckBox proofOfResidenceCheckBox;
    @FXML private ComboBox<String> proofOfResidenceStatus;

    // Step 4: Photo options
    @FXML private RadioButton photoTakenRadio;
    @FXML private RadioButton photoNotAvailableRadio;
    @FXML private RadioButton photoRefusedRadio;
    @FXML private VBox photoRefusalReasonBox;
    @FXML private TextArea photoRefusalReasonArea;
    private ToggleGroup photoToggleGroup;

    // Step 4: Optional documents
    @FXML private CheckBox previousTranscriptCheckBox;
    @FXML private CheckBox iepDocumentCheckBox;
    @FXML private CheckBox medicalRecordsCheckBox;
    @FXML private CheckBox photoReleaseCheckBox;

    // Step 4: Incomplete registration fields
    @FXML private VBox missingDocumentsBox;
    @FXML private DatePicker expectedDocumentsDate;
    @FXML private TextField documentNotesField;
    @FXML private Button saveIncompleteBtn;

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
        setupDocumentControls();
        generateStudentId();
        loadSampleCourses();
        updateStepIndicators();
    }

    /**
     * Setup document status combo boxes and photo radio buttons
     */
    private void setupDocumentControls() {
        // Document status options
        ObservableList<String> documentStatuses = FXCollections.observableArrayList(
                "Submitted", "Pending", "Not Available", "Waived"
        );

        if (birthCertificateStatus != null) {
            birthCertificateStatus.setItems(documentStatuses);
        }
        if (immunizationStatus != null) {
            immunizationStatus.setItems(documentStatuses);
        }
        if (proofOfResidenceStatus != null) {
            proofOfResidenceStatus.setItems(documentStatuses);
        }

        // Setup photo toggle group
        photoToggleGroup = new ToggleGroup();
        if (photoTakenRadio != null) {
            photoTakenRadio.setToggleGroup(photoToggleGroup);
            photoTakenRadio.setSelected(true); // Default to photo taken
        }
        if (photoNotAvailableRadio != null) {
            photoNotAvailableRadio.setToggleGroup(photoToggleGroup);
        }
        if (photoRefusedRadio != null) {
            photoRefusedRadio.setToggleGroup(photoToggleGroup);

            // Show/hide refusal reason box based on selection
            photoRefusedRadio.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (photoRefusalReasonBox != null) {
                    photoRefusalReasonBox.setVisible(isSelected);
                    photoRefusalReasonBox.setManaged(isSelected);
                }
            });
        }
    }

    private void setupComboBoxes() {
        // Gender
        genderComboBox.setItems(FXCollections.observableArrayList(
                "Male", "Female"
        ));

        // Grade Level - Use GradeLevel enum display names
        gradeLevelComboBox.setItems(FXCollections.observableArrayList(
                GradeLevel.getAllDisplayNames()
        ));

        // Listen for grade level changes to update available courses
        gradeLevelComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            onGradeLevelChanged(newVal);
        });

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

    /**
     * Handle grade level selection change - reload appropriate courses
     */
    private void onGradeLevelChanged(String gradeLevelStr) {
        if (gradeLevelStr == null || gradeLevelStr.isEmpty()) {
            selectedGradeLevel = null;
            availableCourses.clear();
            log.info("Grade level cleared, no courses available");
            return;
        }

        // Parse the grade level string to enum
        selectedGradeLevel = GradeLevel.fromString(gradeLevelStr);

        if (selectedGradeLevel == null) {
            log.warn("Could not parse grade level: {}", gradeLevelStr);
            availableCourses.clear();
            return;
        }

        log.info("Grade level changed to: {} ({})", selectedGradeLevel.getDisplayName(),
                selectedGradeLevel.getEducationLevel().getDisplayName());

        // Clear enrolled courses when grade changes (they may no longer be appropriate)
        if (!enrolledCourses.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Grade Level Changed");
            alert.setHeaderText("Clear Enrolled Courses?");
            alert.setContentText("Changing the grade level will clear the currently enrolled courses " +
                    "as they may not be appropriate for the new grade level.\n\n" +
                    "Do you want to continue?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    enrolledCourses.clear();
                    updateEnrolledCoursesList();
                    updateTotalCredits();
                }
            });
        }

        // Load grade-appropriate courses
        loadCoursesForGradeLevel();
    }

    /**
     * Load courses appropriate for the selected grade level
     */
    private void loadCoursesForGradeLevel() {
        availableCourses.clear();

        if (selectedGradeLevel == null) {
            log.info("No grade level selected, courses not loaded");
            return;
        }

        // Get grade-appropriate courses from the service
        List<Course> appropriateCourses = gradeAppropriateCourseService.getSampleCoursesForGrade(selectedGradeLevel);

        availableCourses.addAll(appropriateCourses);

        log.info("Loaded {} courses for {} ({})",
                appropriateCourses.size(),
                selectedGradeLevel.getDisplayName(),
                selectedGradeLevel.getEducationLevel().getDisplayName());

        // Update the table
        availableCoursesTable.setItems(availableCourses);
    }

    /**
     * Legacy method - now redirects to loadCoursesForGradeLevel
     * Called during initialization before grade is selected
     */
    private void loadSampleCourses() {
        // Don't load courses until grade level is selected
        // This prevents Pre-K students from seeing high school courses
        log.info("Courses will be loaded after grade level is selected");
        availableCourses.clear();
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
        // Verify grade level is selected first
        if (selectedGradeLevel == null) {
            showAlert(Alert.AlertType.WARNING, "Grade Level Required",
                    "Please select a grade level in Step 1 before enrolling in courses.");
            return;
        }

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

        // Double-check grade eligibility (defensive programming)
        String ineligibilityReason = gradeAppropriateCourseService.getIneligibilityReason(selected, selectedGradeLevel);
        if (ineligibilityReason != null) {
            showAlert(Alert.AlertType.ERROR, "Course Not Available",
                    ineligibilityReason + "\n\nThis course is not appropriate for " +
                    selectedGradeLevel.getDisplayName() + " students.");
            log.warn("Attempted to enroll {} student in ineligible course: {} - {}",
                    selectedGradeLevel.getDisplayName(), selected.getCourseCode(), ineligibilityReason);
            return;
        }

        enrolledCourses.add(selected);
        updateEnrolledCoursesList();
        updateTotalCredits();

        log.info("Enrolled {} student in course: {} - {}",
                selectedGradeLevel.getDisplayName(), selected.getCourseCode(), selected.getCourseName());
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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload Student Documents");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Document Files", "*.pdf", "*.jpg", "*.jpeg", "*.png", "*.gif"),
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Stage stage = (Stage) firstNameField.getScene().getWindow();
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);

        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            StringBuilder uploadedInfo = new StringBuilder();
            uploadedInfo.append("Selected Documents:\n\n");

            for (File file : selectedFiles) {
                uploadedInfo.append("• ").append(file.getName());
                uploadedInfo.append(" (").append(String.format("%.2f KB", file.length() / 1024.0)).append(")\n");
                log.info("Document selected for upload: {} ({} bytes)", file.getName(), file.length());
            }

            uploadedInfo.append("\nNote: Documents will be attached to the student record ");
            uploadedInfo.append("when registration is submitted.\n\n");
            uploadedInfo.append("For full document management features, use the ");
            uploadedInfo.append("Document Management module after registration.");

            showAlert(Alert.AlertType.INFORMATION, "Documents Selected",
                    uploadedInfo.toString());

            // Mark the appropriate document checkboxes based on file names (heuristic)
            for (File file : selectedFiles) {
                String fileName = file.getName().toLowerCase();
                if (fileName.contains("birth") || fileName.contains("certificate")) {
                    birthCertificateCheckBox.setSelected(true);
                } else if (fileName.contains("immun") || fileName.contains("vaccine") || fileName.contains("shot")) {
                    immunizationCheckBox.setSelected(true);
                } else if (fileName.contains("residence") || fileName.contains("address") || fileName.contains("utility") || fileName.contains("bill")) {
                    proofOfResidenceCheckBox.setSelected(true);
                } else if (fileName.contains("transcript")) {
                    previousTranscriptCheckBox.setSelected(true);
                } else if (fileName.contains("iep") || fileName.contains("504")) {
                    iepDocumentCheckBox.setSelected(true);
                } else if (fileName.contains("medical") || fileName.contains("health")) {
                    medicalRecordsCheckBox.setSelected(true);
                } else if (fileName.contains("photo") || fileName.contains("release")) {
                    photoReleaseCheckBox.setSelected(true);
                }
            }
        }
    }

    @FXML
    private void handleSaveIncomplete() {
        // Validate minimum required fields (student name at least)
        if (firstNameField.getText().trim().isEmpty() || lastNameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Incomplete Information",
                    "Please enter at least the student's first and last name before saving as incomplete.");
            return;
        }

        // Build summary of what's missing
        StringBuilder missingSummary = new StringBuilder();
        missingSummary.append("The following items are incomplete:\n\n");

        // Check required documents
        boolean hasMissingDocs = false;
        if (!birthCertificateCheckBox.isSelected()) {
            missingSummary.append("• Birth Certificate\n");
            hasMissingDocs = true;
        }
        if (!immunizationCheckBox.isSelected()) {
            missingSummary.append("• Immunization Records\n");
            hasMissingDocs = true;
        }
        if (!proofOfResidenceCheckBox.isSelected()) {
            missingSummary.append("• Proof of Residence\n");
            hasMissingDocs = true;
        }

        // Check photo status
        boolean photoMissing = false;
        boolean photoRefused = false;
        if (photoNotAvailableRadio != null && photoNotAvailableRadio.isSelected()) {
            missingSummary.append("• Student Photo (not available)\n");
            photoMissing = true;
        } else if (photoRefusedRadio != null && photoRefusedRadio.isSelected()) {
            missingSummary.append("• Student Photo (parent refused)\n");
            photoRefused = true;
        }

        if (!hasMissingDocs && !photoMissing && !photoRefused) {
            showAlert(Alert.AlertType.INFORMATION, "No Missing Items",
                    "All required documents appear to be checked.\n\n" +
                    "If you still need to save as incomplete, please uncheck the missing document items.");
            return;
        }

        // Add expected date and notes if provided
        String notes = documentNotesField != null ? documentNotesField.getText().trim() : "";
        LocalDate expectedDate = expectedDocumentsDate != null ? expectedDocumentsDate.getValue() : null;

        if (expectedDate != null) {
            missingSummary.append("\nExpected completion: ").append(expectedDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
        }
        if (!notes.isEmpty()) {
            missingSummary.append("\nNotes: ").append(notes);
        }

        // Confirm save
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Save Incomplete Registration");
        confirm.setHeaderText("Save registration as incomplete?");
        confirm.setContentText(missingSummary.toString() +
                "\n\nThis registration will be saved and can be completed later " +
                "when all documents are received.");

        // Create final copies for lambda
        final boolean finalHasMissingDocs = hasMissingDocs;
        final boolean finalPhotoMissing = photoMissing;
        final boolean finalPhotoRefused = photoRefused;

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                saveIncompleteRegistration(finalHasMissingDocs, finalPhotoMissing, finalPhotoRefused, notes, expectedDate);
            }
        });
    }

    /**
     * Save the registration as incomplete to the database
     */
    private void saveIncompleteRegistration(boolean missingDocs, boolean missingPhoto,
                                            boolean photoRefused, String notes, LocalDate expectedDate) {
        try {
            // Create the incomplete registration entity
            IncompleteRegistration incomplete = new IncompleteRegistration();

            // Student Information
            incomplete.setStudentFirstName(firstNameField.getText().trim());
            incomplete.setStudentLastName(lastNameField.getText().trim());

            // Convert display name (e.g., "9th Grade") to short code (e.g., "9")
            String gradeLevelDisplayInc = gradeLevelComboBox.getValue();
            GradeLevel gradeEnumInc = GradeLevel.fromString(gradeLevelDisplayInc);
            if (gradeEnumInc != null) {
                String shortCodeInc = switch (gradeEnumInc) {
                    case PRE_K -> "PK";
                    case KINDERGARTEN -> "K";
                    default -> String.valueOf(gradeEnumInc.getNumericValue());
                };
                incomplete.setGradeLevel(shortCodeInc);
            } else {
                incomplete.setGradeLevel(gradeLevelDisplayInc);
            }

            if (birthDatePicker.getValue() != null) {
                incomplete.setDateOfBirth(birthDatePicker.getValue());
            }

            // Guardian Information
            if (guardian1FirstNameField != null && !guardian1FirstNameField.getText().trim().isEmpty()) {
                String guardianName = guardian1FirstNameField.getText().trim();
                if (guardian1LastNameField != null && !guardian1LastNameField.getText().trim().isEmpty()) {
                    guardianName += " " + guardian1LastNameField.getText().trim();
                }
                incomplete.setGuardianName(guardianName);
            }
            if (guardian1CellPhoneField != null) {
                incomplete.setGuardianPhone(guardian1CellPhoneField.getText().trim());
            }
            if (guardian1EmailField != null) {
                incomplete.setGuardianEmail(guardian1EmailField.getText().trim());
            }

            // Determine status and reason
            if (missingDocs) {
                incomplete.setStatus(RegistrationStatus.INCOMPLETE_DOCUMENTS);
                incomplete.setIncompleteReason(IncompleteRegistration.IncompleteReason.MISSING_DOCUMENTS);
            } else if (photoRefused) {
                incomplete.setStatus(RegistrationStatus.INCOMPLETE_DOCUMENTS);
                incomplete.setIncompleteReason(IncompleteRegistration.IncompleteReason.PHOTO_REFUSED);
                incomplete.setPhotoRefused(true);
                if (photoRefusalReasonArea != null && !photoRefusalReasonArea.getText().trim().isEmpty()) {
                    incomplete.setPhotoRefusalReason(photoRefusalReasonArea.getText().trim());
                }
            } else if (missingPhoto) {
                incomplete.setStatus(RegistrationStatus.INCOMPLETE_DOCUMENTS);
                incomplete.setIncompleteReason(IncompleteRegistration.IncompleteReason.MISSING_PHOTO);
                incomplete.setMissingPhoto(true);
            }

            // Track specific missing documents
            incomplete.setMissingBirthCertificate(!birthCertificateCheckBox.isSelected());
            incomplete.setMissingImmunization(!immunizationCheckBox.isSelected());
            incomplete.setMissingProofOfResidence(!proofOfResidenceCheckBox.isSelected());

            // Build missing items summary
            StringBuilder missingItems = new StringBuilder();
            if (!birthCertificateCheckBox.isSelected()) missingItems.append("Birth Certificate, ");
            if (!immunizationCheckBox.isSelected()) missingItems.append("Immunization Records, ");
            if (!proofOfResidenceCheckBox.isSelected()) missingItems.append("Proof of Residence, ");
            if (missingPhoto) missingItems.append("Student Photo, ");
            if (photoRefused) missingItems.append("Photo (Parent Refused), ");
            if (missingItems.length() > 2) {
                incomplete.setMissingItems(missingItems.substring(0, missingItems.length() - 2));
            }

            // Follow-up information
            if (expectedDate != null) {
                incomplete.setExpectedCompletionDate(expectedDate);
                // Set next follow-up to 3 days before expected completion
                incomplete.setNextFollowupDate(expectedDate.minusDays(3));
            } else {
                // Default: follow up in 1 week
                incomplete.setNextFollowupDate(LocalDate.now().plusDays(7));
            }

            if (notes != null && !notes.isEmpty()) {
                incomplete.setFollowupNotes("[" + LocalDate.now() + "] Initial save: " + notes);
            }

            // Set created by from security context
            incomplete.setCreatedBy(SecurityContext.getCurrentUsername().orElse("System"));

            // Save to database
            IncompleteRegistration saved = incompleteRegistrationRepository.save(incomplete);

            log.info("Saved incomplete registration ID {} for student: {} {}",
                    saved.getId(), saved.getStudentFirstName(), saved.getStudentLastName());

            // Show success message
            StringBuilder savedInfo = new StringBuilder();
            savedInfo.append("Incomplete Registration Saved Successfully!\n\n");
            savedInfo.append("Registration ID: ").append(saved.getId()).append("\n");
            savedInfo.append("Student: ").append(saved.getStudentFullName()).append("\n");
            savedInfo.append("Grade: ").append(saved.getGradeLevel() != null ? saved.getGradeLevel() : "Not selected").append("\n\n");

            if (saved.getMissingItems() != null) {
                savedInfo.append("Missing Items: ").append(saved.getMissingItems()).append("\n");
            }

            if (expectedDate != null) {
                savedInfo.append("Expected Completion: ").append(expectedDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))).append("\n");
            }

            savedInfo.append("\nThis registration can be viewed and completed from the\n'Incomplete Registrations' section in the sidebar.");

            showAlert(Alert.AlertType.INFORMATION, "Registration Saved", savedInfo.toString());

            // Reset form after successful save
            resetForm();

        } catch (Exception e) {
            log.error("Failed to save incomplete registration", e);
            showAlert(Alert.AlertType.ERROR, "Save Failed",
                    "Failed to save incomplete registration:\n\n" + e.getMessage() +
                    "\n\nPlease try again or contact support.");
        }
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
        try {
            // Create new student entity
            Student student = new Student();

            // Basic Information (Step 1)
            student.setStudentId(studentIdField.getText().trim());
            student.setFirstName(firstNameField.getText().trim());
            student.setLastName(lastNameField.getText().trim());
            student.setMiddleName(middleNameField.getText() != null ? middleNameField.getText().trim() : null);
            student.setPreferredFirstName(preferredNameField.getText() != null ? preferredNameField.getText().trim() : null);
            student.setDateOfBirth(birthDatePicker.getValue());
            student.setGender(genderComboBox.getValue());

            // Convert display name (e.g., "9th Grade") to short code (e.g., "9")
            String gradeLevelDisplay = gradeLevelComboBox.getValue();
            GradeLevel gradeEnum = GradeLevel.fromString(gradeLevelDisplay);
            if (gradeEnum != null) {
                // Use short code: "PK", "K", "1", "2", ... "12"
                String shortCode = switch (gradeEnum) {
                    case PRE_K -> "PK";
                    case KINDERGARTEN -> "K";
                    default -> String.valueOf(gradeEnum.getNumericValue());
                };
                student.setGradeLevel(shortCode);
            } else {
                student.setGradeLevel(gradeLevelDisplay); // Fallback
            }

            // Set student as active
            student.setStudentStatus(Student.StudentStatus.ACTIVE);
            student.setActive(true);

            // Guardian/Contact Information (Step 2)
            if (guardian1EmailField != null && !guardian1EmailField.getText().trim().isEmpty()) {
                student.setEmail(guardian1EmailField.getText().trim());
            }
            if (guardian1CellPhoneField != null && !guardian1CellPhoneField.getText().trim().isEmpty()) {
                student.setCellPhone(guardian1CellPhoneField.getText().trim());
            }

            // Address Information
            if (addressField != null && !addressField.getText().trim().isEmpty()) {
                student.setHomeStreetAddress(addressField.getText().trim());
                student.setStreetAddress(addressField.getText().trim());
            }
            if (cityField != null && !cityField.getText().trim().isEmpty()) {
                student.setCity(cityField.getText().trim());
            }
            if (stateComboBox != null && stateComboBox.getValue() != null) {
                student.setState(stateComboBox.getValue());
            }
            if (zipCodeField != null && !zipCodeField.getText().trim().isEmpty()) {
                student.setZipCode(zipCodeField.getText().trim());
            }

            // Save the student to database
            Student savedStudent = studentService.createStudent(student);
            log.info("Student created successfully: {} {} (ID: {})",
                    savedStudent.getFirstName(), savedStudent.getLastName(), savedStudent.getStudentId());

            // Enroll student in selected courses
            int successfulEnrollments = 0;
            int failedEnrollments = 0;
            StringBuilder enrollmentErrors = new StringBuilder();

            for (Course course : enrolledCourses) {
                try {
                    studentEnrollmentService.enrollStudent(savedStudent.getId(), course.getId());
                    successfulEnrollments++;
                    log.info("Enrolled student {} in course {}", savedStudent.getStudentId(), course.getCourseCode());
                } catch (Exception e) {
                    failedEnrollments++;
                    enrollmentErrors.append("• ").append(course.getCourseCode()).append(": ").append(e.getMessage()).append("\n");
                    log.warn("Failed to enroll student {} in course {}: {}",
                            savedStudent.getStudentId(), course.getCourseCode(), e.getMessage());
                }
            }

            // Build success message
            StringBuilder successMessage = new StringBuilder();
            successMessage.append("Registration has been saved successfully!\n\n");
            successMessage.append("Student ID: ").append(savedStudent.getStudentId()).append("\n");
            successMessage.append("Student: ").append(savedStudent.getFirstName()).append(" ").append(savedStudent.getLastName()).append("\n");
            successMessage.append("Grade: ").append(savedStudent.getGradeLevel()).append("\n\n");
            successMessage.append("Course Enrollments:\n");
            successMessage.append("  Successful: ").append(successfulEnrollments).append("\n");

            if (failedEnrollments > 0) {
                successMessage.append("  Failed: ").append(failedEnrollments).append("\n\n");
                successMessage.append("Some course enrollments failed:\n").append(enrollmentErrors);
                successMessage.append("\nYou can enroll in these courses later through the Student Enrollment module.");
            }

            successMessage.append("\n\nThe student has been added to the system.");

            // Show success message
            Alert success = new Alert(failedEnrollments > 0 ? Alert.AlertType.WARNING : Alert.AlertType.INFORMATION);
            success.setTitle("Registration Complete");
            success.setHeaderText(failedEnrollments > 0 ? "Student Registered with Warnings" : "Student Registration Successful");
            success.setContentText(successMessage.toString());
            success.showAndWait();

            // Reset form
            resetForm();

        } catch (Exception e) {
            log.error("Failed to save student registration", e);
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Registration Failed");
            error.setHeaderText("Failed to Save Student Registration");
            error.setContentText("An error occurred while saving the registration:\n\n" + e.getMessage() +
                    "\n\nPlease check the information and try again.");
            error.showAndWait();
        }
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
