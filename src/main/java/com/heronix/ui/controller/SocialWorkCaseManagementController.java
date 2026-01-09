package com.heronix.ui.controller;

import com.heronix.model.domain.SocialWorkCase;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.Teacher;
import com.heronix.service.CounselingManagementService;
import com.heronix.service.StudentService;
import com.heronix.service.TeacherService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Social Work Case Management Form
 * Handles comprehensive case tracking for social work interventions and family support
 */
@Component
public class SocialWorkCaseManagementController {

    private static final Logger logger = LoggerFactory.getLogger(SocialWorkCaseManagementController.class);

    @Autowired
    private CounselingManagementService counselingManagementService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private ApplicationContext applicationContext;

    // Case Information
    @FXML private ComboBox<Student> studentComboBox;
    @FXML private TextField gradeField;
    @FXML private DatePicker caseOpenedDatePicker;
    @FXML private ComboBox<Teacher> socialWorkerComboBox;
    @FXML private ComboBox<String> caseTypeComboBox;
    @FXML private ComboBox<String> priorityComboBox;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private ComboBox<String> referralSourceComboBox;

    // Case Description
    @FXML private TextArea presentingIssuesTextArea;
    @FXML private TextArea familyBackgroundTextArea;
    @FXML private TextArea strengthsTextArea;

    // Family & Household Information
    @FXML private TextField primaryGuardianField;
    @FXML private TextField guardianPhoneField;
    @FXML private TextField householdSizeField;
    @FXML private ComboBox<String> livingSituationComboBox;
    @FXML private TextField addressField;
    @FXML private TextArea householdNotesTextArea;

    // Needs Assessment
    @FXML private CheckBox foodAssistanceCheckBox;
    @FXML private CheckBox housingAssistanceCheckBox;
    @FXML private CheckBox financialAssistanceCheckBox;
    @FXML private CheckBox medicalServicesCheckBox;
    @FXML private CheckBox mentalHealthServicesCheckBox;
    @FXML private CheckBox substanceAbuseServicesCheckBox;
    @FXML private CheckBox clothingAssistanceCheckBox;
    @FXML private CheckBox transportationAssistanceCheckBox;
    @FXML private CheckBox childCareCheckBox;
    @FXML private CheckBox legalServicesCheckBox;
    @FXML private CheckBox parentingSupportCheckBox;
    @FXML private CheckBox immigrationServicesCheckBox;
    @FXML private TextArea needsAssessmentTextArea;

    // Services & Interventions
    @FXML private TextArea servicesProvidedTextArea;
    @FXML private TextArea externalAgenciesTextArea;
    @FXML private TextArea schoolPersonnelTextArea;

    // Goals & Action Plan
    @FXML private TextArea caseGoalsTextArea;
    @FXML private TextArea actionPlanTextArea;
    @FXML private DatePicker nextContactDatePicker;
    @FXML private DatePicker reviewDatePicker;

    // Progress & Outcomes
    @FXML private TextArea progressNotesTextArea;
    @FXML private ComboBox<String> overallProgressComboBox;
    @FXML private ComboBox<String> engagementComboBox;

    // Case Closure
    @FXML private VBox closureSection;
    @FXML private DatePicker caseClosedDatePicker;
    @FXML private ComboBox<String> closureReasonComboBox;
    @FXML private CheckBox followUpRecommendedCheckBox;
    @FXML private TextArea closureSummaryTextArea;

    // Contact Log
    @FXML private TableView<ContactLogEntry> contactLogTable;
    @FXML private TableColumn<ContactLogEntry, LocalDate> contactDateColumn;
    @FXML private TableColumn<ContactLogEntry, String> contactTypeColumn;
    @FXML private TableColumn<ContactLogEntry, String> contactWithColumn;
    @FXML private TableColumn<ContactLogEntry, String> contactPurposeColumn;
    @FXML private TableColumn<ContactLogEntry, String> contactNotesColumn;
    @FXML private Button addContactButton;

    // Buttons and Status
    @FXML private Button saveButton;
    @FXML private Button saveDraftButton;
    @FXML private Button cancelButton;
    @FXML private Label statusIndicator;

    private SocialWorkCase currentCase;
    private Student preselectedStudent;
    private boolean isViewMode = false;

    @FXML
    public void initialize() {
        logger.info("Initializing SocialWorkCaseManagementController");

        setupComboBoxes();
        setupContactLogTable();
        setupEventListeners();
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

        // Social Worker ComboBox
        socialWorkerComboBox.setConverter(new javafx.util.StringConverter<Teacher>() {
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

    private void setupContactLogTable() {
        contactDateColumn.setCellValueFactory(cellData ->
            new SimpleObjectProperty<>(cellData.getValue().getDate()));
        contactDateColumn.setCellFactory(column -> new TableCell<ContactLogEntry, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                }
            }
        });

        contactTypeColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getType()));
        contactWithColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getContactWith()));
        contactPurposeColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getPurpose()));
        contactNotesColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getNotes()));
    }

    private void setupEventListeners() {
        // Auto-populate grade when student is selected
        studentComboBox.setOnAction(e -> {
            Student student = studentComboBox.getValue();
            if (student != null) {
                gradeField.setText(String.valueOf(student.getGradeLevel()));
            }
        });

        // Show/hide closure section based on status
        statusComboBox.setOnAction(e -> updateClosureSectionVisibility());
    }

    private void loadInitialData() {
        try {
            // Load all students
            List<Student> students = studentService.getAllStudents();
            studentComboBox.setItems(FXCollections.observableArrayList(students));

            // Load all teachers (for social worker selection)
            List<Teacher> teachers = teacherService.findAll();
            socialWorkerComboBox.setItems(FXCollections.observableArrayList(teachers));

            // Set default dates
            caseOpenedDatePicker.setValue(LocalDate.now());

            // Pre-select student if provided
            if (preselectedStudent != null) {
                studentComboBox.setValue(preselectedStudent);
                gradeField.setText(String.valueOf(preselectedStudent.getGradeLevel()));
            }

        } catch (Exception e) {
            logger.error("Error loading initial data", e);
            showError("Error loading data: " + e.getMessage());
        }
    }

    private void updateClosureSectionVisibility() {
        String status = statusComboBox.getValue();
        boolean showClosure = status != null &&
            (status.equals("CLOSED_SUCCESSFUL") || status.equals("CLOSED_UNSUCCESSFUL") ||
             status.equals("REFERRED_OUT"));
        closureSection.setVisible(showClosure);
        closureSection.setManaged(showClosure);
    }

    @FXML
    private void handleAddContact() {
        Dialog<ContactLogEntry> dialog = new Dialog<>();
        dialog.setTitle("Add Contact Log Entry");
        dialog.setHeaderText("Record contact with student/family/agency");

        ButtonType saveButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        DatePicker contactDate = new DatePicker(LocalDate.now());
        ComboBox<String> contactType = new ComboBox<>(FXCollections.observableArrayList(
            "PHONE_CALL", "HOME_VISIT", "SCHOOL_MEETING", "EMAIL", "TEXT",
            "AGENCY_MEETING", "PARENT_CONFERENCE", "STUDENT_CHECK_IN"
        ));
        TextField contactWith = new TextField();
        contactWith.setPromptText("Who was contacted");
        TextField purpose = new TextField();
        purpose.setPromptText("Purpose of contact");
        TextArea notes = new TextArea();
        notes.setPromptText("Contact notes");
        notes.setPrefRowCount(3);

        grid.add(new Label("Date:"), 0, 0);
        grid.add(contactDate, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(contactType, 1, 1);
        grid.add(new Label("Contact With:"), 0, 2);
        grid.add(contactWith, 1, 2);
        grid.add(new Label("Purpose:"), 0, 3);
        grid.add(purpose, 1, 3);
        grid.add(new Label("Notes:"), 0, 4);
        grid.add(notes, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new ContactLogEntry(
                    contactDate.getValue(),
                    contactType.getValue(),
                    contactWith.getText(),
                    purpose.getText(),
                    notes.getText()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(entry -> {
            contactLogTable.getItems().add(entry);
        });
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        try {
            SocialWorkCase socialCase = buildCaseFromForm();

            // Save through service
            // Note: The service would need a save method, but for now we'll use the repository pattern
            counselingManagementService.saveSocialWorkCase(socialCase);

            showSuccess("Social work case saved successfully");
            closeWindow();

        } catch (Exception e) {
            logger.error("Error saving social work case", e);
            showError("Error saving case: " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveDraft() {
        try {
            SocialWorkCase socialCase = buildCaseFromForm();
            socialCase.setCaseStatus(SocialWorkCase.CaseStatus.PENDING_REFERRAL);

            counselingManagementService.saveSocialWorkCase(socialCase);

            showSuccess("Draft saved successfully");
            closeWindow();

        } catch (Exception e) {
            logger.error("Error saving draft", e);
            showError("Error saving draft: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateForm() {
        List<String> errors = new ArrayList<>();

        if (studentComboBox.getValue() == null) {
            errors.add("Student is required");
        }

        if (caseOpenedDatePicker.getValue() == null) {
            errors.add("Case opened date is required");
        }

        if (socialWorkerComboBox.getValue() == null) {
            errors.add("Assigned social worker is required");
        }

        if (caseTypeComboBox.getValue() == null || caseTypeComboBox.getValue().isEmpty()) {
            errors.add("Case type is required");
        }

        if (priorityComboBox.getValue() == null || priorityComboBox.getValue().isEmpty()) {
            errors.add("Priority level is required");
        }

        if (statusComboBox.getValue() == null || statusComboBox.getValue().isEmpty()) {
            errors.add("Case status is required");
        }

        if (presentingIssuesTextArea.getText() == null || presentingIssuesTextArea.getText().trim().isEmpty()) {
            errors.add("Presenting issues description is required");
        }

        if (caseGoalsTextArea.getText() == null || caseGoalsTextArea.getText().trim().isEmpty()) {
            errors.add("Case goals are required");
        }

        // Closure validation
        String status = statusComboBox.getValue();
        if (status != null && (status.equals("CLOSED_SUCCESSFUL") || status.equals("CLOSED_UNSUCCESSFUL"))) {
            if (caseClosedDatePicker.getValue() == null) {
                errors.add("Case closed date is required for closed cases");
            }
            if (closureReasonComboBox.getValue() == null || closureReasonComboBox.getValue().isEmpty()) {
                errors.add("Closure reason is required for closed cases");
            }
        }

        if (!errors.isEmpty()) {
            showValidationErrors(errors);
            return false;
        }

        return true;
    }

    private SocialWorkCase buildCaseFromForm() {
        SocialWorkCase socialCase = currentCase != null ? currentCase : new SocialWorkCase();

        // Case Information
        socialCase.setStudent(studentComboBox.getValue());
        socialCase.setCaseOpenedDate(caseOpenedDatePicker.getValue());
        socialCase.setSocialWorker(socialWorkerComboBox.getValue());
        socialCase.setCaseType(SocialWorkCase.CaseType.valueOf(caseTypeComboBox.getValue()));
        socialCase.setPriorityLevel(SocialWorkCase.PriorityLevel.valueOf(priorityComboBox.getValue()));
        socialCase.setCaseStatus(SocialWorkCase.CaseStatus.valueOf(statusComboBox.getValue()));

        // Case Description
        socialCase.setPresentingIssue(presentingIssuesTextArea.getText());

        // Store family background and strengths in appropriate fields
        String familyBackground = familyBackgroundTextArea.getText();
        String strengths = strengthsTextArea.getText();

        // Combine into case summary or use existing fields
        String caseSummary = "FAMILY BACKGROUND:\n" +
            (familyBackground != null ? familyBackground : "") + "\n\n" +
            "STRENGTHS & PROTECTIVE FACTORS:\n" +
            (strengths != null ? strengths : "");
        socialCase.setCaseSummary(caseSummary);

        // Family & Household Information
        socialCase.setPrimaryCaregiver(primaryGuardianField.getText());
        // Note: The entity uses different field names, map accordingly
        if (guardianPhoneField.getText() != null && !guardianPhoneField.getText().isEmpty()) {
            // Store in appropriate field or handle in entity
        }
        if (householdSizeField.getText() != null && !householdSizeField.getText().isEmpty()) {
            try {
                // Could map to numberOfSiblings or similar field
            } catch (NumberFormatException e) {
                logger.warn("Invalid household size format");
            }
        }
        if (livingSituationComboBox.getValue() != null) {
            socialCase.setHousingSituation(
                SocialWorkCase.HousingSituation.valueOf(livingSituationComboBox.getValue())
            );
        }
        socialCase.setLivingArrangementDetails(addressField.getText() + "\n" +
            householdNotesTextArea.getText());

        // Needs Assessment - Map to boolean fields
        socialCase.setFoodInsecurity(foodAssistanceCheckBox.isSelected());
        socialCase.setHousingInstability(housingAssistanceCheckBox.isSelected());
        socialCase.setFinancialHardship(financialAssistanceCheckBox.isSelected());
        socialCase.setMedicalNeedsUnmet(medicalServicesCheckBox.isSelected());
        socialCase.setMentalHealthReferral(mentalHealthServicesCheckBox.isSelected());
        socialCase.setSubstanceAbuseServicesReferral(substanceAbuseServicesCheckBox.isSelected());
        socialCase.setClothingAssistanceReferral(clothingAssistanceCheckBox.isSelected());
        socialCase.setLegalAidReferral(legalServicesCheckBox.isSelected());

        // Store detailed needs assessment
        String familyChallenges = "NEEDS ASSESSMENT:\n" + needsAssessmentTextArea.getText();
        socialCase.setFamilyChallenges(familyChallenges);

        // Services & Interventions
        socialCase.setInterventions(servicesProvidedTextArea.getText());
        socialCase.setAgenciesInvolved(externalAgenciesTextArea.getText());

        // Goals & Action Plan
        socialCase.setGoals(caseGoalsTextArea.getText());
        socialCase.setNextFollowUpDate(nextContactDatePicker.getValue());

        // Store action plan in appropriate field
        String actionPlan = actionPlanTextArea.getText();
        if (actionPlan != null && !actionPlan.isEmpty()) {
            String currentInterventions = socialCase.getInterventions() != null ?
                socialCase.getInterventions() : "";
            socialCase.setInterventions(currentInterventions + "\n\nACTION PLAN:\n" + actionPlan);
        }

        // Progress & Outcomes
        socialCase.setProgressNotes(progressNotesTextArea.getText());

        // Map progress and engagement to appropriate fields
        if (engagementComboBox.getValue() != null) {
            try {
                SocialWorkCase.CooperationLevel cooperation =
                    SocialWorkCase.CooperationLevel.valueOf(
                        engagementComboBox.getValue().replace("ENGAGED", "COOPERATIVE")
                    );
                socialCase.setParentCooperationLevel(cooperation);
            } catch (IllegalArgumentException e) {
                logger.warn("Could not map engagement level: " + engagementComboBox.getValue());
            }
        }

        // Case Closure
        if (closureSection.isVisible()) {
            socialCase.setCaseClosedDate(caseClosedDatePicker.getValue());
            socialCase.setClosureReason(closureReasonComboBox.getValue());
            socialCase.setClosureSummary(closureSummaryTextArea.getText());
            socialCase.setFollowUpNeeded(followUpRecommendedCheckBox.isSelected());
        }

        // Contact log would need to be stored separately or in a notes field
        if (!contactLogTable.getItems().isEmpty()) {
            StringBuilder contactLog = new StringBuilder("CONTACT LOG:\n");
            for (ContactLogEntry entry : contactLogTable.getItems()) {
                contactLog.append(String.format("%s - %s with %s: %s - %s\n",
                    entry.getDate(),
                    entry.getType(),
                    entry.getContactWith(),
                    entry.getPurpose(),
                    entry.getNotes()
                ));
            }
            String currentNotes = socialCase.getProgressNotes() != null ?
                socialCase.getProgressNotes() : "";
            socialCase.setProgressNotes(currentNotes + "\n\n" + contactLog.toString());
        }

        return socialCase;
    }

    /**
     * Set student for the case (used when launching from dashboard)
     */
    public void setStudent(Student student) {
        this.preselectedStudent = student;
        if (studentComboBox != null && studentComboBox.getItems().contains(student)) {
            studentComboBox.setValue(student);
            gradeField.setText(String.valueOf(student.getGradeLevel()));
        }
    }

    /**
     * Load existing case for editing
     */
    public void loadCase(SocialWorkCase socialCase) {
        this.currentCase = socialCase;
        populateFormFromCase(socialCase);
    }

    /**
     * Set form to view-only mode
     */
    public void setViewMode(boolean viewMode) {
        this.isViewMode = viewMode;
        if (viewMode) {
            // Disable all input controls
            studentComboBox.setDisable(true);
            caseOpenedDatePicker.setDisable(true);
            socialWorkerComboBox.setDisable(true);
            caseTypeComboBox.setDisable(true);
            priorityComboBox.setDisable(true);
            statusComboBox.setDisable(true);
            referralSourceComboBox.setDisable(true);

            presentingIssuesTextArea.setEditable(false);
            familyBackgroundTextArea.setEditable(false);
            strengthsTextArea.setEditable(false);

            primaryGuardianField.setEditable(false);
            guardianPhoneField.setEditable(false);
            householdSizeField.setEditable(false);
            livingSituationComboBox.setDisable(true);
            addressField.setEditable(false);
            householdNotesTextArea.setEditable(false);

            // Disable all checkboxes
            foodAssistanceCheckBox.setDisable(true);
            housingAssistanceCheckBox.setDisable(true);
            financialAssistanceCheckBox.setDisable(true);
            medicalServicesCheckBox.setDisable(true);
            mentalHealthServicesCheckBox.setDisable(true);
            substanceAbuseServicesCheckBox.setDisable(true);
            clothingAssistanceCheckBox.setDisable(true);
            transportationAssistanceCheckBox.setDisable(true);
            childCareCheckBox.setDisable(true);
            legalServicesCheckBox.setDisable(true);
            parentingSupportCheckBox.setDisable(true);
            immigrationServicesCheckBox.setDisable(true);

            needsAssessmentTextArea.setEditable(false);
            servicesProvidedTextArea.setEditable(false);
            externalAgenciesTextArea.setEditable(false);
            schoolPersonnelTextArea.setEditable(false);

            caseGoalsTextArea.setEditable(false);
            actionPlanTextArea.setEditable(false);
            nextContactDatePicker.setDisable(true);
            reviewDatePicker.setDisable(true);

            progressNotesTextArea.setEditable(false);
            overallProgressComboBox.setDisable(true);
            engagementComboBox.setDisable(true);

            caseClosedDatePicker.setDisable(true);
            closureReasonComboBox.setDisable(true);
            followUpRecommendedCheckBox.setDisable(true);
            closureSummaryTextArea.setEditable(false);

            addContactButton.setDisable(true);

            // Hide save buttons
            saveButton.setVisible(false);
            saveDraftButton.setVisible(false);
            cancelButton.setText("Close");
        }
    }

    private void populateFormFromCase(SocialWorkCase socialCase) {
        // Case Information
        studentComboBox.setValue(socialCase.getStudent());
        gradeField.setText(String.valueOf(socialCase.getStudent().getGradeLevel()));
        caseOpenedDatePicker.setValue(socialCase.getCaseOpenedDate());
        socialWorkerComboBox.setValue(socialCase.getSocialWorker());
        caseTypeComboBox.setValue(socialCase.getCaseType().name());
        priorityComboBox.setValue(socialCase.getPriorityLevel().name());
        statusComboBox.setValue(socialCase.getCaseStatus().name());

        // Case Description
        presentingIssuesTextArea.setText(socialCase.getPresentingIssue());

        // Parse case summary for family background and strengths
        if (socialCase.getCaseSummary() != null) {
            String[] parts = socialCase.getCaseSummary().split("STRENGTHS & PROTECTIVE FACTORS:");
            if (parts.length > 0) {
                familyBackgroundTextArea.setText(
                    parts[0].replace("FAMILY BACKGROUND:", "").trim()
                );
            }
            if (parts.length > 1) {
                strengthsTextArea.setText(parts[1].trim());
            }
        }

        // Family & Household Information
        primaryGuardianField.setText(socialCase.getPrimaryCaregiver());
        if (socialCase.getHousingSituation() != null) {
            livingSituationComboBox.setValue(socialCase.getHousingSituation().name());
        }
        if (socialCase.getLivingArrangementDetails() != null) {
            String[] livingDetails = socialCase.getLivingArrangementDetails().split("\n", 2);
            if (livingDetails.length > 0) {
                addressField.setText(livingDetails[0]);
            }
            if (livingDetails.length > 1) {
                householdNotesTextArea.setText(livingDetails[1]);
            }
        }

        // Needs Assessment
        foodAssistanceCheckBox.setSelected(socialCase.getFoodInsecurity());
        housingAssistanceCheckBox.setSelected(socialCase.getHousingInstability());
        financialAssistanceCheckBox.setSelected(socialCase.getFinancialHardship());
        medicalServicesCheckBox.setSelected(socialCase.getMedicalNeedsUnmet());
        mentalHealthServicesCheckBox.setSelected(socialCase.getMentalHealthReferral());
        substanceAbuseServicesCheckBox.setSelected(socialCase.getSubstanceAbuseServicesReferral());
        clothingAssistanceCheckBox.setSelected(socialCase.getClothingAssistanceReferral());
        legalServicesCheckBox.setSelected(socialCase.getLegalAidReferral());

        if (socialCase.getFamilyChallenges() != null) {
            needsAssessmentTextArea.setText(
                socialCase.getFamilyChallenges().replace("NEEDS ASSESSMENT:", "").trim()
            );
        }

        // Services & Interventions
        servicesProvidedTextArea.setText(socialCase.getInterventions());
        externalAgenciesTextArea.setText(socialCase.getAgenciesInvolved());

        // Goals & Action Plan
        caseGoalsTextArea.setText(socialCase.getGoals());
        nextContactDatePicker.setValue(socialCase.getNextFollowUpDate());

        // Progress & Outcomes
        progressNotesTextArea.setText(socialCase.getProgressNotes());
        if (socialCase.getParentCooperationLevel() != null) {
            String engagement = socialCase.getParentCooperationLevel().name()
                .replace("COOPERATIVE", "ENGAGED");
            engagementComboBox.setValue(engagement);
        }

        // Case Closure
        if (socialCase.getCaseClosedDate() != null) {
            caseClosedDatePicker.setValue(socialCase.getCaseClosedDate());
            closureReasonComboBox.setValue(socialCase.getClosureReason());
            closureSummaryTextArea.setText(socialCase.getClosureSummary());
            followUpRecommendedCheckBox.setSelected(socialCase.getFollowUpNeeded());
            updateClosureSectionVisibility();
        }
    }

    private void showValidationErrors(List<String> errors) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText("Please correct the following errors:");
        alert.setContentText(String.join("\n", errors));
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
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

    /**
     * Inner class for Contact Log entries
     */
    public static class ContactLogEntry {
        private LocalDate date;
        private String type;
        private String contactWith;
        private String purpose;
        private String notes;

        public ContactLogEntry(LocalDate date, String type, String contactWith,
                              String purpose, String notes) {
            this.date = date;
            this.type = type;
            this.contactWith = contactWith;
            this.purpose = purpose;
            this.notes = notes;
        }

        public LocalDate getDate() { return date; }
        public String getType() { return type; }
        public String getContactWith() { return contactWith; }
        public String getPurpose() { return purpose; }
        public String getNotes() { return notes; }
    }
}
