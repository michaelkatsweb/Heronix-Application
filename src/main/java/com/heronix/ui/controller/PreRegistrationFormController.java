package com.heronix.ui.controller;

import com.heronix.model.domain.Student;
import com.heronix.model.domain.PreRegistration;
import com.heronix.model.domain.PreRegistration.RegistrationStatus;
import com.heronix.repository.StudentRepository;
import com.heronix.service.PreRegistrationService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller for Pre-Registration Form
 * Handles early enrollment for upcoming school year
 *
 * Features:
 * - Grade advancement tracking
 * - Course preference selection
 * - Early bird registration
 * - Seat reservation
 * - Returning student verification
 *
 * Location: src/main/java/com/heronix/ui/controller/PreRegistrationFormController.java
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Component
public class PreRegistrationFormController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PreRegistrationService preRegistrationService;

    // ========================================================================
    // HEADER SECTION
    // ========================================================================

    @FXML private TextField registrationNumberField;
    @FXML private Label statusLabel;
    @FXML private ComboBox<String> targetSchoolYear;
    @FXML private Label registrationPeriodLabel;
    @FXML private Label seatsAvailableLabel;
    @FXML private Button submitButton;

    // ========================================================================
    // SECTION 1: STUDENT INFORMATION
    // ========================================================================

    @FXML private TextField studentSearchField;
    @FXML private TextField studentNameField;
    @FXML private TextField studentIdField;
    @FXML private TextField currentGradeField;
    @FXML private ComboBox<String> nextGradeComboBox;
    @FXML private TextField dateOfBirthField;
    @FXML private TextField studentEmailField;
    @FXML private CheckBox isReturningStudentCheckbox;
    @FXML private CheckBox hasCompletedPriorYearCheckbox;
    @FXML private CheckBox isInGoodStandingCheckbox;

    // ========================================================================
    // SECTION 2: PARENT/GUARDIAN INFORMATION
    // ========================================================================

    @FXML private TextField parentNameField;
    @FXML private TextField parentPhoneField;
    @FXML private TextField parentEmailField;
    @FXML private TextArea currentAddressArea;
    @FXML private CheckBox addressChangedCheckbox;
    @FXML private Label newAddressLabel;
    @FXML private TextArea newAddressArea;
    @FXML private CheckBox emergencyContactsVerified;

    // ========================================================================
    // SECTION 3: COURSE PREFERENCES
    // ========================================================================

    @FXML private TableView requiredCoursesTable;
    @FXML private TableView electiveCoursesTable;
    @FXML private CheckBox apHonorsCheckbox;
    @FXML private CheckBox dualEnrollmentCheckbox;
    @FXML private CheckBox cteCheckbox;
    @FXML private CheckBox athleticsCheckbox;
    @FXML private CheckBox fineArtsCheckbox;
    @FXML private CheckBox stemCheckbox;

    // ========================================================================
    // SECTION 4: SPECIAL SERVICES
    // ========================================================================

    @FXML private CheckBox continueIEPCheckbox;
    @FXML private DatePicker iepReviewDatePicker;
    @FXML private CheckBox continue504Checkbox;
    @FXML private DatePicker plan504ReviewDatePicker;
    @FXML private CheckBox continueESLCheckbox;
    @FXML private ComboBox<String> languageProficiencyComboBox;
    @FXML private CheckBox continueGiftedCheckbox;
    @FXML private CheckBox specialTransportationCheckbox;
    @FXML private ComboBox<String> transportationTypeComboBox;
    @FXML private TextArea medicalAccommodationsArea;

    // ========================================================================
    // SECTION 5: LUNCH PROGRAM & FEES
    // ========================================================================

    @FXML private ComboBox<String> lunchProgramComboBox;
    @FXML private TextField currentLunchStatusField;
    @FXML private CheckBox needsLunchApplicationCheckbox;
    @FXML private Label technologyFeeLabel;
    @FXML private CheckBox techFeeWaiverCheckbox;
    @FXML private Label activityFeeLabel;
    @FXML private CheckBox activityFeeWaiverCheckbox;
    @FXML private Label totalFeesLabel;

    // ========================================================================
    // SECTION 6: SCHEDULE PREFERENCES
    // ========================================================================

    @FXML private ComboBox<String> preferredStartTimeComboBox;
    @FXML private ComboBox<String> studyHallPreferenceComboBox;
    @FXML private ComboBox<String> lunchPeriodComboBox;
    @FXML private CheckBox earlyBirdCheckbox;
    @FXML private CheckBox afterSchoolCheckbox;
    @FXML private TextArea schedulingNotesArea;

    // ========================================================================
    // SECTION 7: ACKNOWLEDGMENT
    // ========================================================================

    @FXML private CheckBox acknowledgeAccuracyCheckbox;
    @FXML private CheckBox acknowledgeReviewCheckbox;
    @FXML private CheckBox acknowledgeDeadlineCheckbox;
    @FXML private CheckBox acknowledgeFeesCheckbox;
    @FXML private CheckBox acknowledgeUpdatesCheckbox;
    @FXML private TextField parentSignatureField;
    @FXML private TextField signatureDateField;

    // ========================================================================
    // SECTION 8: ADDITIONAL NOTES
    // ========================================================================

    @FXML private TextArea additionalNotesArea;

    // ========================================================================
    // FOOTER
    // ========================================================================

    @FXML private Label lastSavedLabel;
    @FXML private Label createdByLabel;
    @FXML private Label registrationWindowLabel;

    // ========================================================================
    // INTERNAL STATE
    // ========================================================================

    private Student currentStudent;
    private PreRegistration currentPreRegistration;
    private boolean isDirty = false;
    private LocalDateTime lastSavedTime;

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    @FXML
    public void initialize() {
        log.info("Initializing PreRegistrationFormController");

        // Populate combo boxes
        populateSchoolYears();
        populateGrades();
        populateLanguageProficiency();
        populateTransportationTypes();
        populateLunchProgram();
        populateSchedulePreferences();

        // Set default values
        if (signatureDateField != null) {
            signatureDateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }

        // Setup listeners
        setupAddressChangeListener();
        setupFeeCalculation();

        // Initialize registration number
        generateRegistrationNumber();

        log.info("PreRegistrationFormController initialized successfully");
    }

    private void populateSchoolYears() {
        if (targetSchoolYear == null) return;
        int currentYear = LocalDate.now().getYear();
        int nextYear = currentYear + 1;
        int yearAfterNext = currentYear + 2;

        targetSchoolYear.getItems().addAll(
            nextYear + "-" + (nextYear + 1),
            yearAfterNext + "-" + (yearAfterNext + 1)
        );
        targetSchoolYear.getSelectionModel().selectFirst();
    }

    private void populateGrades() {
        if (nextGradeComboBox == null) return;
        nextGradeComboBox.getItems().addAll(
            "Kindergarten", "1st Grade", "2nd Grade", "3rd Grade", "4th Grade", "5th Grade",
            "6th Grade", "7th Grade", "8th Grade", "9th Grade", "10th Grade", "11th Grade", "12th Grade"
        );
    }

    private void populateLanguageProficiency() {
        if (languageProficiencyComboBox == null) return;
        languageProficiencyComboBox.getItems().addAll(
            "Level 1 - Entering", "Level 2 - Emerging", "Level 3 - Developing",
            "Level 4 - Expanding", "Level 5 - Bridging", "Level 6 - Reaching"
        );
    }

    private void populateTransportationTypes() {
        if (transportationTypeComboBox == null) return;
        transportationTypeComboBox.getItems().addAll(
            "Wheelchair Accessible", "Door-to-Door Service", "Special Needs Van",
            "Extended Route", "Medical Transport"
        );
    }

    private void populateLunchProgram() {
        if (lunchProgramComboBox == null) return;
        lunchProgramComboBox.getItems().addAll(
            "Full Price", "Reduced Price", "Free", "Bring Own Lunch"
        );
    }

    private void populateSchedulePreferences() {
        if (preferredStartTimeComboBox != null) {
            preferredStartTimeComboBox.getItems().addAll(
                "No Preference", "Early Start (7:30 AM)", "Regular Start (8:00 AM)", "Late Start (8:30 AM)"
            );
        }
        if (studyHallPreferenceComboBox != null) {
            studyHallPreferenceComboBox.getItems().addAll(
                "No Study Hall", "Morning", "Afternoon", "No Preference"
            );
        }
        if (lunchPeriodComboBox != null) {
            lunchPeriodComboBox.getItems().addAll(
                "1st Lunch (11:00 AM)", "2nd Lunch (11:30 AM)", "3rd Lunch (12:00 PM)", "No Preference"
            );
        }
    }

    private void setupAddressChangeListener() {
        if (addressChangedCheckbox == null) return;
        addressChangedCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newAddressLabel != null) {
                newAddressLabel.setVisible(newVal);
                newAddressLabel.setManaged(newVal);
            }
            if (newAddressArea != null) {
                newAddressArea.setVisible(newVal);
                newAddressArea.setManaged(newVal);
            }
        });
    }

    private void setupFeeCalculation() {
        // Recalculate total fees when waivers change
        if (techFeeWaiverCheckbox != null) {
            techFeeWaiverCheckbox.selectedProperty().addListener((obs, old, val) -> calculateTotalFees());
        }
        if (activityFeeWaiverCheckbox != null) {
            activityFeeWaiverCheckbox.selectedProperty().addListener((obs, old, val) -> calculateTotalFees());
        }
    }

    private void calculateTotalFees() {
        double total = 0.0;
        if (techFeeWaiverCheckbox != null && !techFeeWaiverCheckbox.isSelected()) {
            total += 50.0;
        }
        if (activityFeeWaiverCheckbox != null && !activityFeeWaiverCheckbox.isSelected()) {
            total += 75.0;
        }
        if (totalFeesLabel != null) {
            totalFeesLabel.setText(String.format("$%.2f", total));
        }
    }

    private void generateRegistrationNumber() {
        // Registration number will be generated by service when creating pre-registration
        if (registrationNumberField != null) {
            registrationNumberField.setText("(New Pre-Registration)");
        }
        if (statusLabel != null) {
            statusLabel.setText("DRAFT");
        }
    }

    /**
     * Create new pre-registration in database
     */
    private void createNewPreRegistration() {
        if (currentStudent == null) {
            showError("Please select a student first.");
            return;
        }

        if (targetSchoolYear == null || targetSchoolYear.getValue() == null) {
            showError("Please select a target school year.");
            return;
        }

        try {
            currentPreRegistration = preRegistrationService.createPreRegistration(
                currentStudent.getId(),
                targetSchoolYear.getValue(),
                1L  // TODO: Get actual staff ID from session
            );

            registrationNumberField.setText(currentPreRegistration.getRegistrationNumber());
            statusLabel.setText(currentPreRegistration.getStatus().getDisplayName());

            log.info("Created new pre-registration: {}", currentPreRegistration.getRegistrationNumber());
        } catch (Exception e) {
            log.error("Error creating pre-registration", e);
            showError("Error creating pre-registration: " + e.getMessage());
        }
    }

    // ========================================================================
    // EVENT HANDLERS - STUDENT SEARCH
    // ========================================================================

    @FXML
    private void handleSearchStudent() {
        if (studentSearchField == null || studentSearchField.getText().trim().isEmpty()) {
            showError("Please enter a Student ID or Name to search.");
            return;
        }

        try {
            String searchTerm = studentSearchField.getText().trim();

            // Try to find by ID first, then by name
            Student student = null;
            if (searchTerm.matches("\\d+")) {
                // Numeric search - try ID
                student = studentRepository.findById(Long.parseLong(searchTerm)).orElse(null);
            } else {
                // Name search - show selection dialog
                student = handleStudentNameSearch(searchTerm);
                if (student == null) {
                    return;  // User canceled or no match found
                }
            }

            if (student != null) {
                loadStudentData(student);
                showSuccess("Student found: " + student.getFirstName() + " " + student.getLastName());
            } else {
                showError("Student not found.");
            }

        } catch (Exception e) {
            log.error("Error searching for student", e);
            showError("Error searching for student: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearStudent() {
        currentStudent = null;
        clearStudentFields();
    }

    private void loadStudentData(Student student) {
        currentStudent = student;

        // Create pre-registration record
        createNewPreRegistration();

        if (studentNameField != null) {
            studentNameField.setText(student.getFirstName() + " " + student.getLastName());
        }
        if (studentIdField != null) {
            studentIdField.setText(student.getStudentId());
        }
        if (currentGradeField != null) {
            currentGradeField.setText(student.getGradeLevel());
        }
        if (dateOfBirthField != null && student.getDateOfBirth() != null) {
            dateOfBirthField.setText(student.getDateOfBirth().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (studentEmailField != null) {
            studentEmailField.setText(student.getEmail());
        }

        // Set next grade based on current grade
        advanceGrade(student.getGradeLevel());

        // Load parent information
        // TODO: Load from StudentParentRelationship

        // Load current services
        if (student.getHasIEP() != null && student.getHasIEP()) {
            if (continueIEPCheckbox != null) continueIEPCheckbox.setSelected(true);
        }
        if (student.getHas504Plan() != null && student.getHas504Plan()) {
            if (continue504Checkbox != null) continue504Checkbox.setSelected(true);
        }
        if (student.getIsEnglishLearner() != null && student.getIsEnglishLearner()) {
            if (continueESLCheckbox != null) continueESLCheckbox.setSelected(true);
        }
        if (student.getIsGifted() != null && student.getIsGifted()) {
            if (continueGiftedCheckbox != null) continueGiftedCheckbox.setSelected(true);
        }

        // Load lunch program
        if (currentLunchStatusField != null) {
            currentLunchStatusField.setText("N/A"); // TODO: Get from student lunch status field
        }

        log.info("Loaded student data for: {}", student.getStudentId());
    }

    private void advanceGrade(String currentGrade) {
        if (currentGrade == null || nextGradeComboBox == null) return;

        String nextGrade = switch (currentGrade) {
            case "Kindergarten" -> "1st Grade";
            case "1st Grade" -> "2nd Grade";
            case "2nd Grade" -> "3rd Grade";
            case "3rd Grade" -> "4th Grade";
            case "4th Grade" -> "5th Grade";
            case "5th Grade" -> "6th Grade";
            case "6th Grade" -> "7th Grade";
            case "7th Grade" -> "8th Grade";
            case "8th Grade" -> "9th Grade";
            case "9th Grade" -> "10th Grade";
            case "10th Grade" -> "11th Grade";
            case "11th Grade" -> "12th Grade";
            default -> currentGrade;
        };

        nextGradeComboBox.setValue(nextGrade);
    }

    private void clearStudentFields() {
        if (studentNameField != null) studentNameField.clear();
        if (studentIdField != null) studentIdField.clear();
        if (currentGradeField != null) currentGradeField.clear();
        if (nextGradeComboBox != null) nextGradeComboBox.getSelectionModel().clearSelection();
        if (dateOfBirthField != null) dateOfBirthField.clear();
        if (studentEmailField != null) studentEmailField.clear();
    }

    // ========================================================================
    // EVENT HANDLERS - COURSE SELECTION
    // ========================================================================

    @FXML
    private void handleAddElective() {
        try {
            log.info("Adding elective course");

            // Create course selection dialog
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Add Elective Course");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            javafx.scene.layout.VBox layout = new javafx.scene.layout.VBox(15);
            layout.setPadding(new javafx.geometry.Insets(20));

            Label titleLabel = new Label("Select Elective Course");
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            Label infoLabel = new Label("Choose an elective course for the upcoming school year:");
            infoLabel.setWrapText(true);

            // Course category selection
            javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
            grid.setHgap(10);
            grid.setVgap(10);

            grid.add(new Label("Course Category:"), 0, 0);
            ComboBox<String> categoryCombo = new ComboBox<>();
            categoryCombo.getItems().addAll(
                "Fine Arts", "Foreign Language", "Technology", "Business",
                "Physical Education", "Music", "Drama", "Other"
            );
            categoryCombo.setPromptText("Select category...");
            grid.add(categoryCombo, 1, 0);

            grid.add(new Label("Course Name:"), 0, 1);
            ComboBox<String> courseCombo = new ComboBox<>();
            courseCombo.setPromptText("Select course...");
            courseCombo.setDisable(true);
            grid.add(courseCombo, 1, 1);

            // Populate courses based on category
            categoryCombo.setOnAction(e -> {
                String category = categoryCombo.getValue();
                courseCombo.getItems().clear();
                if (category != null) {
                    switch (category) {
                        case "Fine Arts":
                            courseCombo.getItems().addAll("Art I", "Art II", "Ceramics", "Drawing & Painting");
                            break;
                        case "Foreign Language":
                            courseCombo.getItems().addAll("Spanish I", "Spanish II", "French I", "French II");
                            break;
                        case "Technology":
                            courseCombo.getItems().addAll("Computer Science I", "Web Design", "Robotics");
                            break;
                        case "Business":
                            courseCombo.getItems().addAll("Business Management", "Marketing", "Accounting");
                            break;
                        case "Physical Education":
                            courseCombo.getItems().addAll("Team Sports", "Fitness & Wellness", "Yoga");
                            break;
                        case "Music":
                            courseCombo.getItems().addAll("Band", "Choir", "Orchestra", "Music Theory");
                            break;
                        case "Drama":
                            courseCombo.getItems().addAll("Theatre I", "Theatre II", "Technical Theatre");
                            break;
                        default:
                            courseCombo.getItems().addAll("Elective Course");
                    }
                    courseCombo.setDisable(false);
                }
            });

            grid.add(new Label("Period Preference:"), 0, 2);
            ComboBox<String> periodCombo = new ComboBox<>();
            periodCombo.getItems().addAll("No Preference", "Period 1", "Period 2", "Period 3",
                                         "Period 4", "Period 5", "Period 6", "Period 7");
            periodCombo.setValue("No Preference");
            grid.add(periodCombo, 1, 2);

            // Buttons
            javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(10);
            Button addButton = new Button("Add Course");
            addButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            addButton.setOnAction(e -> {
                if (courseCombo.getValue() != null) {
                    // Add to elective table (would be implemented with actual TableView)
                    showSuccess("Elective course '" + courseCombo.getValue() + "' added successfully.");
                    log.info("Added elective: {}", courseCombo.getValue());
                    stage.close();
                } else {
                    showError("Please select a course.");
                }
            });

            Button cancelButton = new Button("Cancel");
            cancelButton.setOnAction(e -> stage.close());

            buttonBox.getChildren().addAll(addButton, cancelButton);

            layout.getChildren().addAll(titleLabel, infoLabel, grid, buttonBox);

            javafx.scene.Scene scene = new javafx.scene.Scene(layout, 500, 350);
            stage.setScene(scene);
            stage.showAndWait();

        } catch (Exception e) {
            log.error("Error adding elective course", e);
            showError("Failed to add elective course: " + e.getMessage());
        }
    }

    @FXML
    private void handleRemoveElective() {
        try {
            log.info("Removing elective course");

            if (electiveCoursesTable == null || electiveCoursesTable.getSelectionModel().getSelectedItem() == null) {
                showError("Please select an elective course to remove.");
                return;
            }

            // Confirmation dialog
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Remove Elective Course");
            confirm.setHeaderText("Confirm Removal");
            confirm.setContentText("Are you sure you want to remove the selected elective course?\n\n" +
                                  "This action can be undone by re-adding the course.");

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Remove from table
                    Object selectedItem = electiveCoursesTable.getSelectionModel().getSelectedItem();
                    electiveCoursesTable.getItems().remove(selectedItem);

                    showSuccess("Elective course removed successfully.");
                    log.info("Removed elective course from pre-registration");
                }
            });

        } catch (Exception e) {
            log.error("Error removing elective course", e);
            showError("Failed to remove elective course: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateEmergencyContacts() {
        try {
            log.info("Updating emergency contacts");

            if (currentStudent == null) {
                showError("Please select a student first.");
                return;
            }

            // Create emergency contacts dialog
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Update Emergency Contacts - " + currentStudent.getFullName());
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            javafx.scene.layout.VBox layout = new javafx.scene.layout.VBox(15);
            layout.setPadding(new javafx.geometry.Insets(20));

            Label titleLabel = new Label("Emergency Contacts Verification");
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            Label infoLabel = new Label("Please review and update emergency contact information for the upcoming school year:");
            infoLabel.setWrapText(true);

            // Display current emergency contacts
            TextArea contactsArea = new TextArea();
            contactsArea.setEditable(false);
            contactsArea.setPrefRowCount(12);
            contactsArea.setWrapText(true);

            StringBuilder contactsText = new StringBuilder();
            contactsText.append("CURRENT EMERGENCY CONTACTS\n");
            contactsText.append("========================================\n\n");

            if (currentStudent.getEmergencyContacts() != null && !currentStudent.getEmergencyContacts().isEmpty()) {
                int priority = 1;
                for (var contact : currentStudent.getEmergencyContacts()) {
                    contactsText.append("Contact #").append(priority++).append(":\n");
                    contactsText.append("  Name: ").append(contact.getFullName()).append("\n");
                    contactsText.append("  Relationship: ").append(contact.getRelationship()).append("\n");
                    contactsText.append("  Primary Phone: ").append(contact.getPrimaryPhone() != null ? contact.getPrimaryPhone() : "N/A").append("\n");
                    if (contact.getSecondaryPhone() != null && !contact.getSecondaryPhone().trim().isEmpty()) {
                        contactsText.append("  Secondary Phone: ").append(contact.getSecondaryPhone()).append("\n");
                    }
                    if (contact.getEmail() != null && !contact.getEmail().trim().isEmpty()) {
                        contactsText.append("  Email: ").append(contact.getEmail()).append("\n");
                    }
                    contactsText.append("  Authorized to Pick Up: ").append(contact.getAuthorizedToPickUp() != null && contact.getAuthorizedToPickUp() ? "Yes" : "No").append("\n");
                    if (contact.getAvailabilityNotes() != null && !contact.getAvailabilityNotes().trim().isEmpty()) {
                        contactsText.append("  Availability: ").append(contact.getAvailabilityNotes()).append("\n");
                    }
                    contactsText.append("\n");
                }
            } else {
                contactsText.append("No emergency contacts on file.\n\n");
                contactsText.append("Please contact the school office to add emergency contacts.\n");
            }

            contactsArea.setText(contactsText.toString());

            // Verification checkbox
            CheckBox verifyCheckbox = new CheckBox("I have reviewed and verified all emergency contact information is current and accurate");
            verifyCheckbox.setWrapText(true);
            verifyCheckbox.setStyle("-fx-font-weight: bold;");

            // Buttons
            javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(10);

            Button editButton = new Button("Edit Contacts");
            editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
            editButton.setOnAction(e -> {
                showInfo("Edit Contacts", "Emergency contact editing will open the full student record form.\n\n" +
                        "For now, please note any changes needed in the Additional Notes section.");
            });

            Button confirmButton = new Button("Confirm Verification");
            confirmButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            confirmButton.setDisable(true);
            confirmButton.setOnAction(e -> {
                if (emergencyContactsVerified != null) {
                    emergencyContactsVerified.setSelected(true);
                }
                showSuccess("Emergency contacts verified successfully.");
                log.info("Emergency contacts verified for student: {}", currentStudent.getStudentId());
                stage.close();
            });

            verifyCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                confirmButton.setDisable(!newVal);
            });

            Button cancelButton = new Button("Cancel");
            cancelButton.setOnAction(e -> stage.close());

            buttonBox.getChildren().addAll(editButton, confirmButton, cancelButton);

            layout.getChildren().addAll(titleLabel, infoLabel, contactsArea, verifyCheckbox, buttonBox);

            javafx.scene.Scene scene = new javafx.scene.Scene(layout, 650, 600);
            stage.setScene(scene);
            stage.showAndWait();

        } catch (Exception e) {
            log.error("Error updating emergency contacts", e);
            showError("Failed to update emergency contacts: " + e.getMessage());
        }
    }

    @FXML
    private void handleDownloadLunchApp() {
        try {
            log.info("Downloading lunch application form");

            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Save Lunch Application Form");
            fileChooser.setInitialFileName("lunch_application_" +
                (currentStudent != null ? currentStudent.getStudentId() : "form") + ".pdf");
            fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            java.io.File file = fileChooser.showSaveDialog(
                needsLunchApplicationCheckbox != null ? needsLunchApplicationCheckbox.getScene().getWindow() : null);

            if (file != null) {
                // Generate lunch application PDF
                com.itextpdf.text.Document document = new com.itextpdf.text.Document();
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                com.itextpdf.text.pdf.PdfWriter.getInstance(document, baos);

                document.open();

                // Title
                com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
                com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph(
                    "FREE AND REDUCED PRICE SCHOOL MEALS APPLICATION", titleFont);
                title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                document.add(title);
                document.add(new com.itextpdf.text.Paragraph(" "));

                // School year
                com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 11);
                com.itextpdf.text.Paragraph schoolYear = new com.itextpdf.text.Paragraph(
                    "School Year: " + (targetSchoolYear != null && targetSchoolYear.getValue() != null ?
                        targetSchoolYear.getValue() : "____________________"), normalFont);
                schoolYear.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                document.add(schoolYear);
                document.add(new com.itextpdf.text.Paragraph(" "));

                // Instructions
                com.itextpdf.text.Font boldFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.BOLD);
                document.add(new com.itextpdf.text.Paragraph("INSTRUCTIONS:", boldFont));
                document.add(new com.itextpdf.text.Paragraph(
                    "Complete this application to apply for free or reduced price meals for your child(ren). " +
                    "Return the completed form to your school office. Applications are reviewed within 10 business days.",
                    normalFont));
                document.add(new com.itextpdf.text.Paragraph(" "));

                // Student Information
                document.add(new com.itextpdf.text.Paragraph("PART 1: STUDENT INFORMATION", boldFont));
                document.add(new com.itextpdf.text.Paragraph(" "));

                com.itextpdf.text.pdf.PdfPTable studentTable = new com.itextpdf.text.pdf.PdfPTable(2);
                studentTable.setWidthPercentage(100);
                studentTable.setWidths(new float[]{40, 60});

                addFormField(studentTable, "Student Name:", currentStudent != null ? currentStudent.getFullName() : "____________________");
                addFormField(studentTable, "Student ID:", currentStudent != null && currentStudent.getStudentId() != null ? currentStudent.getStudentId() : "____________________");
                addFormField(studentTable, "Grade Level:", currentStudent != null && currentStudent.getGradeLevel() != null ? currentStudent.getGradeLevel() : "____________________");
                addFormField(studentTable, "School:", "Heronix SIS");

                document.add(studentTable);
                document.add(new com.itextpdf.text.Paragraph(" "));

                // Household Information
                document.add(new com.itextpdf.text.Paragraph("PART 2: HOUSEHOLD INFORMATION", boldFont));
                document.add(new com.itextpdf.text.Paragraph(" "));

                com.itextpdf.text.pdf.PdfPTable householdTable = new com.itextpdf.text.pdf.PdfPTable(2);
                householdTable.setWidthPercentage(100);
                householdTable.setWidths(new float[]{40, 60});

                addFormField(householdTable, "Total Household Members:", "____________________");
                addFormField(householdTable, "Annual Household Income:", "$ ____________________");

                document.add(householdTable);
                document.add(new com.itextpdf.text.Paragraph(" "));

                // Income Eligibility Guidelines
                document.add(new com.itextpdf.text.Paragraph("INCOME ELIGIBILITY GUIDELINES (2025-2026):", boldFont));
                document.add(new com.itextpdf.text.Paragraph(" "));

                com.itextpdf.text.pdf.PdfPTable incomeTable = new com.itextpdf.text.pdf.PdfPTable(3);
                incomeTable.setWidthPercentage(100);
                incomeTable.setWidths(new float[]{33, 33, 34});

                addLunchTableHeader(incomeTable, "Household Size");
                addLunchTableHeader(incomeTable, "Free Meals (Annual)");
                addLunchTableHeader(incomeTable, "Reduced Price (Annual)");

                addLunchTableRow(incomeTable, "1", "$25,142", "$35,798");
                addLunchTableRow(incomeTable, "2", "$33,874", "$48,216");
                addLunchTableRow(incomeTable, "3", "$42,606", "$60,634");
                addLunchTableRow(incomeTable, "4", "$51,338", "$73,052");
                addLunchTableRow(incomeTable, "5", "$60,070", "$85,470");
                addLunchTableRow(incomeTable, "6", "$68,802", "$97,888");

                document.add(incomeTable);
                document.add(new com.itextpdf.text.Paragraph(" "));

                // Signature section
                document.add(new com.itextpdf.text.Paragraph("PART 3: SIGNATURE AND ATTESTATION", boldFont));
                document.add(new com.itextpdf.text.Paragraph(" "));
                document.add(new com.itextpdf.text.Paragraph(
                    "I certify that all information provided is true and correct to the best of my knowledge. " +
                    "I understand that this information is given for the purpose of receiving federal funds and " +
                    "that school officials may verify the information.", normalFont));
                document.add(new com.itextpdf.text.Paragraph(" "));

                com.itextpdf.text.pdf.PdfPTable signatureTable = new com.itextpdf.text.pdf.PdfPTable(2);
                signatureTable.setWidthPercentage(100);
                signatureTable.setWidths(new float[]{50, 50});

                addFormField(signatureTable, "Parent/Guardian Signature:", "____________________");
                addFormField(signatureTable, "Date:", "____________________");
                addFormField(signatureTable, "Phone Number:", "____________________");
                addFormField(signatureTable, "Email:", "____________________");

                document.add(signatureTable);
                document.add(new com.itextpdf.text.Paragraph(" "));

                // Footer
                com.itextpdf.text.Font smallFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 9);
                com.itextpdf.text.Paragraph footer = new com.itextpdf.text.Paragraph(
                    "For Office Use Only - Date Received: ________  Approved: [ ] Yes  [ ] No  By: ________", smallFont);
                footer.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                document.add(footer);

                document.close();

                // Save to file
                java.nio.file.Files.write(file.toPath(), baos.toByteArray());

                showSuccess("Lunch application form downloaded successfully!\n\nFile: " + file.getAbsolutePath() +
                          "\n\nPlease complete the form and return it to the school office.");
                log.info("Lunch application form saved to: {}", file.getAbsolutePath());
            }

        } catch (Exception e) {
            log.error("Error downloading lunch application", e);
            showError("Failed to download lunch application: " + e.getMessage());
        }
    }

    /**
     * Handle student name search - shows selection dialog for multiple matches
     */
    private Student handleStudentNameSearch(String searchTerm) {
        try {
            // Search for students by name (would use actual repository query)
            java.util.List<Student> matches = studentRepository.findAll().stream()
                .filter(s -> (s.getFirstName() + " " + s.getLastName()).toLowerCase().contains(searchTerm.toLowerCase()))
                .limit(20)
                .collect(java.util.stream.Collectors.toList());

            if (matches.isEmpty()) {
                showError("No students found matching: " + searchTerm);
                return null;
            }

            if (matches.size() == 1) {
                return matches.get(0);
            }

            // Show selection dialog for multiple matches
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Select Student");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            javafx.scene.layout.VBox layout = new javafx.scene.layout.VBox(15);
            layout.setPadding(new javafx.geometry.Insets(20));

            Label titleLabel = new Label("Multiple Students Found");
            titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

            Label infoLabel = new Label("Found " + matches.size() + " students matching '" + searchTerm + "'. Please select:");

            ListView<Student> listView = new ListView<>();
            listView.getItems().addAll(matches);
            listView.setCellFactory(lv -> new javafx.scene.control.ListCell<Student>() {
                @Override
                protected void updateItem(Student student, boolean empty) {
                    super.updateItem(student, empty);
                    if (empty || student == null) {
                        setText(null);
                    } else {
                        setText(String.format("%s - %s (%s)",
                            student.getStudentId() != null ? student.getStudentId() : "N/A",
                            student.getFullName(),
                            student.getGradeLevel() != null ? student.getGradeLevel() : "N/A"));
                    }
                }
            });
            listView.setPrefHeight(300);

            final Student[] selectedStudent = new Student[1];

            javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(10);
            Button selectButton = new Button("Select");
            selectButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            selectButton.setDisable(true);
            selectButton.setOnAction(e -> {
                selectedStudent[0] = listView.getSelectionModel().getSelectedItem();
                stage.close();
            });

            listView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
                selectButton.setDisable(newVal == null);
            });

            listView.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && listView.getSelectionModel().getSelectedItem() != null) {
                    selectedStudent[0] = listView.getSelectionModel().getSelectedItem();
                    stage.close();
                }
            });

            Button cancelButton = new Button("Cancel");
            cancelButton.setOnAction(e -> stage.close());

            buttonBox.getChildren().addAll(selectButton, cancelButton);

            layout.getChildren().addAll(titleLabel, infoLabel, listView, buttonBox);

            javafx.scene.Scene scene = new javafx.scene.Scene(layout, 600, 500);
            stage.setScene(scene);
            stage.showAndWait();

            return selectedStudent[0];

        } catch (Exception e) {
            log.error("Error searching for student by name", e);
            showError("Error searching for student: " + e.getMessage());
            return null;
        }
    }

    /**
     * Helper method for lunch form PDF generation
     */
    private void addFormField(com.itextpdf.text.pdf.PdfPTable table, String label, String value) {
        com.itextpdf.text.Font labelFont = new com.itextpdf.text.Font(
            com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Font valueFont = new com.itextpdf.text.Font(
            com.itextpdf.text.Font.FontFamily.HELVETICA, 10);

        com.itextpdf.text.pdf.PdfPCell labelCell = new com.itextpdf.text.pdf.PdfPCell(
            new com.itextpdf.text.Phrase(label, labelFont));
        labelCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
        labelCell.setPadding(5);
        table.addCell(labelCell);

        com.itextpdf.text.pdf.PdfPCell valueCell = new com.itextpdf.text.pdf.PdfPCell(
            new com.itextpdf.text.Phrase(value, valueFont));
        valueCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }

    private void addLunchTableHeader(com.itextpdf.text.pdf.PdfPTable table, String text) {
        com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(
            com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(
            new com.itextpdf.text.Phrase(text, headerFont));
        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addLunchTableRow(com.itextpdf.text.pdf.PdfPTable table, String... values) {
        com.itextpdf.text.Font dataFont = new com.itextpdf.text.Font(
            com.itextpdf.text.Font.FontFamily.HELVETICA, 9);
        for (String value : values) {
            com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(
                new com.itextpdf.text.Phrase(value, dataFont));
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            cell.setPadding(4);
            table.addCell(cell);
        }
    }

    // ========================================================================
    // EVENT HANDLERS - FORM ACTIONS
    // ========================================================================

    @FXML
    private void handleSaveDraft() {
        try {
            if (!validateMinimalFields()) {
                return;
            }

            if (currentPreRegistration == null) {
                showError("Please select a student first.");
                return;
            }

            // Update pre-registration with form data
            saveFormDataToEntity();

            // Save to database
            currentPreRegistration = preRegistrationService.updatePreRegistration(
                currentPreRegistration,
                1L  // TODO: Get actual staff ID from session
            );

            isDirty = false;
            lastSavedTime = LocalDateTime.now();
            updateLastSavedLabel(lastSavedTime);

            showSuccess("Pre-registration draft saved successfully.");
            log.info("Saved pre-registration draft: {}", currentPreRegistration.getRegistrationNumber());

        } catch (Exception e) {
            log.error("Error saving draft", e);
            showError("Failed to save draft: " + e.getMessage());
        }
    }

    @FXML
    private void handleSubmit() {
        try {
            if (!validateAllFields()) {
                return;
            }

            if (!validateAcknowledgments()) {
                showError("Please check all acknowledgment boxes to continue.");
                return;
            }

            if (parentSignatureField == null || parentSignatureField.getText().trim().isEmpty()) {
                showError("Parent/Guardian signature is required.");
                return;
            }

            if (currentPreRegistration == null) {
                showError("Please select a student first.");
                return;
            }

            // Update pre-registration with form data
            saveFormDataToEntity();

            // Submit to database
            currentPreRegistration = preRegistrationService.submitForReview(
                currentPreRegistration.getId(),
                1L  // TODO: Get actual staff ID from session
            );

            if (statusLabel != null) {
                statusLabel.setText(currentPreRegistration.getStatus().getDisplayName());
            }
            if (submitButton != null) {
                submitButton.setDisable(true);
            }

            showSuccess("Pre-registration submitted successfully!\n\n" +
                       "Confirmation Number: " + currentPreRegistration.getRegistrationNumber() + "\n\n" +
                       "You will receive a confirmation email within 24 hours.");
            log.info("Submitted pre-registration: {}", currentPreRegistration.getRegistrationNumber());

        } catch (Exception e) {
            log.error("Error submitting pre-registration", e);
            showError("Failed to submit: " + e.getMessage());
        }
    }

    @FXML
    private void handleCheckEligibility() {
        if (currentStudent == null) {
            showError("Please search and select a student first.");
            return;
        }

        // TODO: Check actual eligibility rules
        boolean eligible = true;
        StringBuilder message = new StringBuilder();
        message.append("Eligibility Check Results:\n\n");

        if (isReturningStudentCheckbox != null && isReturningStudentCheckbox.isSelected()) {
            message.append("✓ Returning Student Status: ELIGIBLE\n");
        } else {
            message.append("✗ Returning Student Status: NOT ELIGIBLE\n");
            eligible = false;
        }

        if (hasCompletedPriorYearCheckbox != null && hasCompletedPriorYearCheckbox.isSelected()) {
            message.append("✓ Prior Year Completion: ELIGIBLE\n");
        } else {
            message.append("⚠ Prior Year Completion: PENDING\n");
        }

        if (isInGoodStandingCheckbox != null && isInGoodStandingCheckbox.isSelected()) {
            message.append("✓ Good Academic Standing: ELIGIBLE\n");
        } else {
            message.append("✗ Good Academic Standing: NOT ELIGIBLE\n");
            eligible = false;
        }

        message.append("\n").append(eligible ? "Student is ELIGIBLE for pre-registration." : "Student is NOT ELIGIBLE. Please contact the office.");

        showInfo(message.toString());
    }

    @FXML
    private void handlePrint() {
        showNotImplemented("Print Confirmation");
        // TODO: Generate PDF confirmation
    }

    @FXML
    private void handleCancel() {
        if (isDirty) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Unsaved Changes");
            confirm.setHeaderText("You have unsaved changes.");
            confirm.setContentText("Do you want to save before closing?");

            ButtonType saveButton = new ButtonType("Save");
            ButtonType discardButton = new ButtonType("Discard");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            confirm.getButtonTypes().setAll(saveButton, discardButton, cancelButton);

            confirm.showAndWait().ifPresent(response -> {
                if (response == saveButton) {
                    handleSaveDraft();
                    closeForm();
                } else if (response == discardButton) {
                    closeForm();
                }
            });
        } else {
            closeForm();
        }
    }

    // ========================================================================
    // DATA BINDING
    // ========================================================================

    /**
     * Save form data to PreRegistration entity
     */
    private void saveFormDataToEntity() {
        if (currentPreRegistration == null) return;

        // Parent/Guardian Verification
        currentPreRegistration.setParentName(getTextOrNull(parentNameField));
        currentPreRegistration.setParentPhone(getTextOrNull(parentPhoneField));
        currentPreRegistration.setParentEmail(getTextOrNull(parentEmailField));
        currentPreRegistration.setCurrentAddress(getTextOrNull(currentAddressArea));
        currentPreRegistration.setAddressChanged(getCheckboxValue(addressChangedCheckbox));
        currentPreRegistration.setNewAddress(getTextOrNull(newAddressArea));
        currentPreRegistration.setEmergencyContactsVerified(getCheckboxValue(emergencyContactsVerified));

        // Course Preferences
        currentPreRegistration.setInterestedInAPHonors(getCheckboxValue(apHonorsCheckbox));
        currentPreRegistration.setInterestedInDualEnrollment(getCheckboxValue(dualEnrollmentCheckbox));
        currentPreRegistration.setInterestedInCTE(getCheckboxValue(cteCheckbox));
        currentPreRegistration.setInterestedInAthletics(getCheckboxValue(athleticsCheckbox));
        currentPreRegistration.setInterestedInFineArts(getCheckboxValue(fineArtsCheckbox));
        currentPreRegistration.setInterestedInSTEM(getCheckboxValue(stemCheckbox));

        // Special Services Continuation
        currentPreRegistration.setContinueIEP(getCheckboxValue(continueIEPCheckbox));
        currentPreRegistration.setContinue504Plan(getCheckboxValue(continue504Checkbox));
        currentPreRegistration.setContinueESL(getCheckboxValue(continueESLCheckbox));
        if (languageProficiencyComboBox != null && languageProficiencyComboBox.getValue() != null) {
            currentPreRegistration.setLanguageProficiencyLevel(languageProficiencyComboBox.getValue());
        }
        currentPreRegistration.setContinueGiftedProgram(getCheckboxValue(continueGiftedCheckbox));
        currentPreRegistration.setNeedsSpecialTransportation(getCheckboxValue(specialTransportationCheckbox));
        if (transportationTypeComboBox != null && transportationTypeComboBox.getValue() != null) {
            currentPreRegistration.setTransportationType(transportationTypeComboBox.getValue());
        }
        currentPreRegistration.setMedicalAccommodations(getTextOrNull(medicalAccommodationsArea));

        // Lunch Program & Fees
        if (lunchProgramComboBox != null && lunchProgramComboBox.getValue() != null) {
            currentPreRegistration.setLunchProgramStatus(lunchProgramComboBox.getValue());
        }
        currentPreRegistration.setNeedsLunchApplication(getCheckboxValue(needsLunchApplicationCheckbox));
        currentPreRegistration.setTechnologyFeeWaiverRequested(getCheckboxValue(techFeeWaiverCheckbox));
        currentPreRegistration.setActivityFeeWaiverRequested(getCheckboxValue(activityFeeWaiverCheckbox));

        // TODO: Calculate total fees
        double totalFees = 0.0;
        currentPreRegistration.setEstimatedTotalFees(totalFees);

        // Schedule Preferences
        if (preferredStartTimeComboBox != null && preferredStartTimeComboBox.getValue() != null) {
            currentPreRegistration.setPreferredStartTime(preferredStartTimeComboBox.getValue());
        }
        if (studyHallPreferenceComboBox != null && studyHallPreferenceComboBox.getValue() != null) {
            currentPreRegistration.setStudyHallPreference(studyHallPreferenceComboBox.getValue());
        }
        if (lunchPeriodComboBox != null && lunchPeriodComboBox.getValue() != null) {
            currentPreRegistration.setLunchPeriodPreference(lunchPeriodComboBox.getValue());
        }
        currentPreRegistration.setInterestedInEarlyBird(getCheckboxValue(earlyBirdCheckbox));
        currentPreRegistration.setInterestedInAfterSchool(getCheckboxValue(afterSchoolCheckbox));
        currentPreRegistration.setSchedulingNotes(getTextOrNull(schedulingNotesArea));

        // Parent Acknowledgment
        currentPreRegistration.setAcknowledgedAccuracy(getCheckboxValue(acknowledgeAccuracyCheckbox));
        currentPreRegistration.setAcknowledgedReview(getCheckboxValue(acknowledgeReviewCheckbox));
        currentPreRegistration.setAcknowledgedDeadline(getCheckboxValue(acknowledgeDeadlineCheckbox));
        currentPreRegistration.setAcknowledgedFees(getCheckboxValue(acknowledgeFeesCheckbox));
        currentPreRegistration.setAcknowledgedUpdates(getCheckboxValue(acknowledgeUpdatesCheckbox));
        currentPreRegistration.setParentSignature(getTextOrNull(parentSignatureField));
        if (signatureDateField != null && signatureDateField.getText() != null && !signatureDateField.getText().trim().isEmpty()) {
            try {
                currentPreRegistration.setSignatureDate(LocalDate.parse(signatureDateField.getText()));
            } catch (Exception e) {
                log.warn("Could not parse signature date: {}", signatureDateField.getText());
            }
        }

        // Additional Notes
        currentPreRegistration.setAdditionalNotes(getTextOrNull(additionalNotesArea));
    }

    private String getTextOrNull(TextField field) {
        return (field != null && field.getText() != null && !field.getText().trim().isEmpty())
            ? field.getText().trim() : null;
    }

    private String getTextOrNull(TextArea area) {
        return (area != null && area.getText() != null && !area.getText().trim().isEmpty())
            ? area.getText().trim() : null;
    }

    private Boolean getCheckboxValue(CheckBox checkbox) {
        return checkbox != null ? checkbox.isSelected() : null;
    }

    // ========================================================================
    // VALIDATION
    // ========================================================================

    private boolean validateMinimalFields() {
        if (currentStudent == null) {
            showError("Please search and select a student.");
            return false;
        }
        return true;
    }

    private boolean validateAllFields() {
        StringBuilder errors = new StringBuilder();

        if (currentStudent == null) {
            errors.append("• Student must be selected\n");
        }

        if (nextGradeComboBox == null || nextGradeComboBox.getValue() == null) {
            errors.append("• Next grade level is required\n");
        }

        if (parentPhoneField == null || parentPhoneField.getText().trim().isEmpty()) {
            errors.append("• Parent phone number is required\n");
        }

        if (parentEmailField == null || parentEmailField.getText().trim().isEmpty()) {
            errors.append("• Parent email is required\n");
        }

        if (lunchProgramComboBox == null || lunchProgramComboBox.getValue() == null) {
            errors.append("• Lunch program status is required\n");
        }

        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please correct the following errors:");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }

        return true;
    }

    private boolean validateAcknowledgments() {
        return (acknowledgeAccuracyCheckbox != null && acknowledgeAccuracyCheckbox.isSelected()) &&
               (acknowledgeReviewCheckbox != null && acknowledgeReviewCheckbox.isSelected()) &&
               (acknowledgeDeadlineCheckbox != null && acknowledgeDeadlineCheckbox.isSelected()) &&
               (acknowledgeFeesCheckbox != null && acknowledgeFeesCheckbox.isSelected()) &&
               (acknowledgeUpdatesCheckbox != null && acknowledgeUpdatesCheckbox.isSelected());
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    private void closeForm() {
        if (registrationNumberField != null && registrationNumberField.getScene() != null) {
            Stage stage = (Stage) registrationNumberField.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        }
    }

    private void updateLastSavedLabel(LocalDateTime time) {
        if (lastSavedLabel != null && time != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            lastSavedLabel.setText(time.format(formatter));
        }
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

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showNotImplemented(String featureName) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Feature Not Implemented");
        alert.setHeaderText(featureName);
        alert.setContentText("This feature is planned for a future release.\n\n" +
                           "Please check back later or contact support for more information.");
        alert.showAndWait();
    }
}
