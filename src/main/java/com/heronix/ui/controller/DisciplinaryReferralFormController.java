package com.heronix.ui.controller;

import com.heronix.model.domain.Campus;
import com.heronix.model.domain.DisciplinaryReferral;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.Teacher;
import com.heronix.service.CampusService;
import com.heronix.service.DisciplinaryReferralService;
import com.heronix.service.StudentService;
import com.heronix.service.TeacherService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Disciplinary Referral Form
 * Handles teacher-submitted disciplinary referrals to administration
 */
@Component
public class DisciplinaryReferralFormController {

    private static final Logger logger = LoggerFactory.getLogger(DisciplinaryReferralFormController.class);

    @Autowired
    private DisciplinaryReferralService disciplinaryReferralService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private CampusService campusService;

    // Student Information
    @FXML private ComboBox<Student> studentComboBox;
    @FXML private TextField gradeLevelField;
    @FXML private TextField studentIdField;

    // Referral Information
    @FXML private DatePicker referralDatePicker;
    @FXML private TextField referralTimeField;
    @FXML private ComboBox<Teacher> referringTeacherComboBox;
    @FXML private ComboBox<String> classPeriodComboBox;
    @FXML private ComboBox<String> priorityComboBox;
    @FXML private ComboBox<Teacher> assignedAdminComboBox;

    // Incident Details
    @FXML private ComboBox<String> referralReasonComboBox;
    @FXML private ComboBox<Campus> campusComboBox;
    @FXML private TextArea incidentDescriptionTextArea;
    @FXML private TextArea interventionsTextArea;

    // Prior Incidents
    @FXML private CheckBox hasRelatedIncidentsCheckBox;
    @FXML private VBox relatedIncidentsBox;
    @FXML private TextArea relatedIncidentsTextArea;
    @FXML private CheckBox previousSuspensionCheckBox;
    @FXML private CheckBox behaviorContractCheckBox;
    @FXML private CheckBox behaviorPlanCheckBox;

    // Witnesses
    @FXML private ComboBox<Object> witnessComboBox;
    @FXML private Button addWitnessButton;
    @FXML private ListView<String> witnessesListView;
    @FXML private ComboBox<Student> otherStudentsComboBox;
    @FXML private Button addStudentButton;
    @FXML private ListView<Student> otherStudentsListView;

    // Parent Contact
    @FXML private CheckBox parentContactedCheckBox;
    @FXML private GridPane parentContactGrid;
    @FXML private DatePicker parentContactDatePicker;
    @FXML private ComboBox<String> contactMethodComboBox;
    @FXML private TextArea parentResponseTextArea;

    // Attachments
    @FXML private Button attachFileButton;
    @FXML private Label attachmentCountLabel;
    @FXML private ListView<File> attachmentsListView;

    // Recommended Action
    @FXML private TextArea recommendedActionTextArea;

    // Buttons
    @FXML private Label statusLabel;
    @FXML private Button submitButton;
    @FXML private Button saveDraftButton;
    @FXML private Button cancelButton;

    private DisciplinaryReferral currentReferral;
    private List<String> witnesses = new ArrayList<>();
    private List<Student> otherStudents = new ArrayList<>();
    private List<File> attachments = new ArrayList<>();

    @FXML
    public void initialize() {
        logger.info("Initializing Disciplinary Referral Form Controller");

        // Set defaults
        referralDatePicker.setValue(LocalDate.now());

        // Load data
        loadStudents();
        loadTeachers();
        loadCampuses();

        // Configure student combo box
        studentComboBox.setConverter(new javafx.util.StringConverter<Student>() {
            @Override
            public String toString(Student student) {
                return student == null ? "" : student.getFirstName() + " " + student.getLastName() +
                       " (" + student.getStudentId() + ")";
            }

            @Override
            public Student fromString(String string) {
                return null;
            }
        });

        // Auto-populate student info when selected
        studentComboBox.setOnAction(e -> {
            Student student = studentComboBox.getValue();
            if (student != null) {
                gradeLevelField.setText(String.valueOf(student.getGradeLevel()));
                studentIdField.setText(student.getStudentId());
            }
        });

        // Configure teacher combo boxes
        javafx.util.StringConverter<Teacher> teacherConverter = new javafx.util.StringConverter<Teacher>() {
            @Override
            public String toString(Teacher teacher) {
                return teacher == null ? "" : teacher.getFirstName() + " " + teacher.getLastName();
            }

            @Override
            public Teacher fromString(String string) {
                return null;
            }
        };

        referringTeacherComboBox.setConverter(teacherConverter);
        assignedAdminComboBox.setConverter(teacherConverter);

        // Configure campus combo box
        campusComboBox.setConverter(new javafx.util.StringConverter<Campus>() {
            @Override
            public String toString(Campus campus) {
                return campus == null ? "" : campus.getName();
            }

            @Override
            public Campus fromString(String string) {
                return null;
            }
        });

        // Configure other students combo box
        otherStudentsComboBox.setConverter(new javafx.util.StringConverter<Student>() {
            @Override
            public String toString(Student student) {
                return student == null ? "" : student.getFirstName() + " " + student.getLastName();
            }

            @Override
            public Student fromString(String string) {
                return null;
            }
        });

        // Enable/disable related incidents box
        hasRelatedIncidentsCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            relatedIncidentsBox.setDisable(!newVal);
        });

        // Enable/disable parent contact grid
        parentContactedCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            parentContactGrid.setDisable(!newVal);
            if (newVal && parentContactDatePicker.getValue() == null) {
                parentContactDatePicker.setValue(LocalDate.now());
            }
        });

        // Configure other students list view
        otherStudentsListView.setCellFactory(param -> new ListCell<Student>() {
            @Override
            protected void updateItem(Student student, boolean empty) {
                super.updateItem(student, empty);
                if (empty || student == null) {
                    setText(null);
                } else {
                    setText(student.getFirstName() + " " + student.getLastName());
                }
            }
        });

        // Validate time format
        referralTimeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$") && !newVal.isEmpty()) {
                referralTimeField.setStyle("-fx-border-color: red;");
            } else {
                referralTimeField.setStyle("");
            }
        });
    }

    private void loadStudents() {
        try {
            List<Student> students = studentService.getAllStudents();
            studentComboBox.setItems(FXCollections.observableArrayList(students));
            otherStudentsComboBox.setItems(FXCollections.observableArrayList(students));
        } catch (Exception e) {
            logger.error("Error loading students", e);
            showError("Failed to load students: " + e.getMessage());
        }
    }

    private void loadTeachers() {
        try {
            List<Teacher> teachers = teacherService.findAll();
            referringTeacherComboBox.setItems(FXCollections.observableArrayList(teachers));

            // Filter administrators for assignment
            List<Teacher> admins = teachers.stream()
                .filter(t -> t.isAdministrator())
                .toList();
            assignedAdminComboBox.setItems(FXCollections.observableArrayList(admins));
        } catch (Exception e) {
            logger.error("Error loading teachers", e);
            showError("Failed to load teachers: " + e.getMessage());
        }
    }

    private void loadCampuses() {
        try {
            List<Campus> campuses = campusService.getAllCampuses();
            campusComboBox.setItems(FXCollections.observableArrayList(campuses));
        } catch (Exception e) {
            logger.error("Error loading campuses", e);
            showError("Failed to load campuses: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddWitness() {
        Object witness = witnessComboBox.getValue();
        if (witness != null) {
            String witnessName = witness.toString();
            if (!witnesses.contains(witnessName)) {
                witnesses.add(witnessName);
                witnessesListView.setItems(FXCollections.observableArrayList(witnesses));
                witnessComboBox.setValue(null);
            }
        }
    }

    @FXML
    private void handleAddStudent() {
        Student student = otherStudentsComboBox.getValue();
        if (student != null && !otherStudents.contains(student)) {
            otherStudents.add(student);
            otherStudentsListView.setItems(FXCollections.observableArrayList(otherStudents));
            otherStudentsComboBox.setValue(null);
        }
    }

    @FXML
    private void handleAttachFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Attach Supporting Documentation");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("All Files", "*.*"),
            new FileChooser.ExtensionFilter("PDF", "*.pdf"),
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"),
            new FileChooser.ExtensionFilter("Documents", "*.doc", "*.docx")
        );

        Stage stage = (Stage) attachFileButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            attachments.add(file);
            attachmentsListView.setItems(FXCollections.observableArrayList(attachments));
            attachmentCountLabel.setText(attachments.size() + " file(s) attached");
        }
    }

    @FXML
    private void handleSubmit() {
        if (validateForm()) {
            try {
                DisciplinaryReferral referral = buildReferralFromForm();
                referral.setStatus(DisciplinaryReferral.ReferralStatus.PENDING);

                DisciplinaryReferral saved = disciplinaryReferralService.saveReferral(referral);

                logger.info("Submitted disciplinary referral: {}", saved.getId());
                showSuccess("Disciplinary referral submitted successfully. Referral ID: " + saved.getId());
                closeWindow();
            } catch (Exception e) {
                logger.error("Error submitting referral", e);
                showError("Failed to submit referral: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSaveDraft() {
        try {
            DisciplinaryReferral referral = buildReferralFromForm();
            referral.setStatus(DisciplinaryReferral.ReferralStatus.DRAFT);

            DisciplinaryReferral saved = disciplinaryReferralService.saveReferral(referral);

            logger.info("Saved disciplinary referral draft: {}", saved.getId());
            showSuccess("Disciplinary referral draft saved successfully");
            closeWindow();
        } catch (Exception e) {
            logger.error("Error saving draft", e);
            showError("Failed to save draft: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Cancel");
        alert.setHeaderText("Discard Referral?");
        alert.setContentText("Any unsaved changes will be lost. Are you sure?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                closeWindow();
            }
        });
    }

    private boolean validateForm() {
        List<String> errors = new ArrayList<>();

        if (studentComboBox.getValue() == null) {
            errors.add("Student is required");
        }
        if (referralDatePicker.getValue() == null) {
            errors.add("Referral date is required");
        }
        if (referralTimeField.getText() == null || referralTimeField.getText().isEmpty()) {
            errors.add("Referral time is required");
        }
        if (referringTeacherComboBox.getValue() == null) {
            errors.add("Referring teacher is required");
        }
        if (priorityComboBox.getValue() == null) {
            errors.add("Priority is required");
        }
        if (referralReasonComboBox.getValue() == null) {
            errors.add("Referral reason is required");
        }
        if (campusComboBox.getValue() == null) {
            errors.add("Campus is required");
        }
        if (incidentDescriptionTextArea.getText() == null || incidentDescriptionTextArea.getText().trim().isEmpty()) {
            errors.add("Incident description is required");
        }

        if (!errors.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please correct the following errors:");
            alert.setContentText(String.join("\n", errors));
            alert.showAndWait();
            return false;
        }

        return true;
    }

    private DisciplinaryReferral buildReferralFromForm() {
        DisciplinaryReferral referral = currentReferral != null ? currentReferral : new DisciplinaryReferral();

        referral.setStudent(studentComboBox.getValue());
        referral.setReferralDate(referralDatePicker.getValue());

        // Parse time
        try {
            LocalTime time = LocalTime.parse(referralTimeField.getText(), DateTimeFormatter.ofPattern("HH:mm"));
            referral.setReferralTime(time);
        } catch (Exception e) {
            logger.warn("Failed to parse time: {}", referralTimeField.getText());
        }

        referral.setReferringTeacher(referringTeacherComboBox.getValue());
        referral.setClassPeriod(classPeriodComboBox.getValue());
        referral.setPriority(DisciplinaryReferral.Priority.valueOf(priorityComboBox.getValue()));
        referral.setAssignedAdministrator(assignedAdminComboBox.getValue());
        referral.setReferralReason(DisciplinaryReferral.ReferralReason.valueOf(referralReasonComboBox.getValue()));
        referral.setCampus(campusComboBox.getValue());
        referral.setIncidentDescription(incidentDescriptionTextArea.getText());
        referral.setInterventionsAttempted(interventionsTextArea.getText());
        referral.setHasRelatedIncidents(hasRelatedIncidentsCheckBox.isSelected());
        referral.setRelatedIncidents(relatedIncidentsTextArea.getText());
        referral.setPreviousSuspension(previousSuspensionCheckBox.isSelected());
        referral.setOnBehaviorContract(behaviorContractCheckBox.isSelected());
        referral.setHasBehaviorPlan(behaviorPlanCheckBox.isSelected());
        referral.setParentContacted(parentContactedCheckBox.isSelected());
        referral.setParentContactDate(parentContactDatePicker.getValue());
        if (contactMethodComboBox.getValue() != null && !contactMethodComboBox.getValue().isEmpty()) {
            referral.setParentContactMethod(com.heronix.model.domain.BehaviorIncident.ContactMethod.valueOf(contactMethodComboBox.getValue()));
        }
        referral.setParentResponse(parentResponseTextArea.getText());
        referral.setRecommendedAction(recommendedActionTextArea.getText());

        return referral;
    }

    public void setReferral(DisciplinaryReferral referral) {
        this.currentReferral = referral;
        if (referral != null) {
            populateForm(referral);
        }
    }

    private void populateForm(DisciplinaryReferral referral) {
        studentComboBox.setValue(referral.getStudent());
        referralDatePicker.setValue(referral.getReferralDate());
        if (referral.getReferralTime() != null) {
            referralTimeField.setText(referral.getReferralTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        }
        referringTeacherComboBox.setValue(referral.getReferringTeacher());
        classPeriodComboBox.setValue(referral.getClassPeriod());
        if (referral.getPriority() != null) {
            priorityComboBox.setValue(referral.getPriority().name());
        }
        assignedAdminComboBox.setValue(referral.getAssignedAdministrator());
        if (referral.getReferralReason() != null) {
            referralReasonComboBox.setValue(referral.getReferralReason().name());
        }
        campusComboBox.setValue(referral.getCampus());
        incidentDescriptionTextArea.setText(referral.getIncidentDescription());
        interventionsTextArea.setText(referral.getInterventionsAttempted());
        hasRelatedIncidentsCheckBox.setSelected(referral.isHasRelatedIncidents());
        relatedIncidentsTextArea.setText(referral.getRelatedIncidents());
        previousSuspensionCheckBox.setSelected(referral.isPreviousSuspension());
        behaviorContractCheckBox.setSelected(referral.isOnBehaviorContract());
        behaviorPlanCheckBox.setSelected(referral.isHasBehaviorPlan());
        parentContactedCheckBox.setSelected(referral.isParentContacted());
        parentContactDatePicker.setValue(referral.getParentContactDate());
        if (referral.getParentContactMethod() != null) {
            contactMethodComboBox.setValue(referral.getParentContactMethod().name());
        }
        parentResponseTextArea.setText(referral.getParentResponse());
        recommendedActionTextArea.setText(referral.getRecommendedAction());
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
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) submitButton.getScene().getWindow();
        stage.close();
    }
}
