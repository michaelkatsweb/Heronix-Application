package com.heronix.ui.controller;

import com.heronix.model.domain.BehaviorIncident;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.Teacher;
import com.heronix.service.BehaviorIncidentService;
import com.heronix.service.StudentService;
import com.heronix.service.TeacherService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
 * Controller for Behavior Incident Form
 * Handles creation and editing of behavior incident reports
 */
@Component
public class BehaviorIncidentFormController {

    private static final Logger logger = LoggerFactory.getLogger(BehaviorIncidentFormController.class);

    @Autowired
    private BehaviorIncidentService behaviorIncidentService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    // Incident Information
    @FXML private ComboBox<Student> studentComboBox;
    @FXML private DatePicker incidentDatePicker;
    @FXML private TextField incidentTimeField;
    @FXML private ComboBox<Teacher> reportingStaffComboBox;
    @FXML private ComboBox<String> locationComboBox;
    @FXML private ComboBox<String> severityComboBox;
    @FXML private ComboBox<String> behaviorTypeComboBox;

    // Incident Details
    @FXML private TextArea descriptionTextArea;
    @FXML private TextArea actionTakenTextArea;

    // Other Students
    @FXML private ComboBox<Student> otherStudentsComboBox;
    @FXML private Button addStudentButton;
    @FXML private ListView<Student> involvedStudentsListView;

    // Witnesses
    @FXML private ComboBox<Object> witnessComboBox;
    @FXML private Button addWitnessButton;
    @FXML private ListView<String> witnessesListView;

    // Follow-up
    @FXML private CheckBox parentContactedCheckBox;
    @FXML private DatePicker parentContactDatePicker;
    @FXML private ComboBox<String> contactMethodComboBox;
    @FXML private CheckBox administrativeReferralCheckBox;
    @FXML private TextArea notesTextArea;

    // Attachments
    @FXML private Button attachFileButton;
    @FXML private Label attachmentCountLabel;
    @FXML private ListView<File> attachmentsListView;

    // Buttons
    @FXML private Button saveButton;
    @FXML private Button saveDraftButton;
    @FXML private Button cancelButton;

    private BehaviorIncident currentIncident;
    private List<Student> involvedStudents = new ArrayList<>();
    private List<String> witnesses = new ArrayList<>();
    private List<File> attachments = new ArrayList<>();

    @FXML
    public void initialize() {
        logger.info("Initializing Behavior Incident Form Controller");

        // Load students and staff
        loadStudents();
        loadStaff();

        // Set default date to today
        incidentDatePicker.setValue(LocalDate.now());

        // Configure student display
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

        // Configure staff display
        reportingStaffComboBox.setConverter(new javafx.util.StringConverter<Teacher>() {
            @Override
            public String toString(Teacher teacher) {
                return teacher == null ? "" : teacher.getFirstName() + " " + teacher.getLastName();
            }

            @Override
            public Teacher fromString(String string) {
                return null;
            }
        });

        // Configure involved students list
        involvedStudentsListView.setCellFactory(param -> new ListCell<Student>() {
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

        // Enable/disable parent contact fields based on checkbox
        parentContactedCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            parentContactDatePicker.setDisable(!newVal);
            contactMethodComboBox.setDisable(!newVal);
            if (newVal && parentContactDatePicker.getValue() == null) {
                parentContactDatePicker.setValue(LocalDate.now());
            }
        });

        // Validate time format
        incidentTimeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$") && !newVal.isEmpty()) {
                incidentTimeField.setStyle("-fx-border-color: red;");
            } else {
                incidentTimeField.setStyle("");
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

    private void loadStaff() {
        try {
            List<Teacher> teachers = teacherService.getAllTeachers();
            reportingStaffComboBox.setItems(FXCollections.observableArrayList(teachers));
        } catch (Exception e) {
            logger.error("Error loading staff", e);
            showError("Failed to load staff: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddStudent() {
        Student selectedStudent = otherStudentsComboBox.getValue();
        if (selectedStudent != null && !involvedStudents.contains(selectedStudent)) {
            involvedStudents.add(selectedStudent);
            involvedStudentsListView.setItems(FXCollections.observableArrayList(involvedStudents));
            otherStudentsComboBox.setValue(null);
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
    private void handleAttachFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Attach File");
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
    private void handleSave() {
        if (validateForm()) {
            try {
                BehaviorIncident incident = buildIncidentFromForm();
                incident.setStatus(BehaviorIncident.IncidentStatus.SUBMITTED);

                BehaviorIncident saved = behaviorIncidentService.saveIncident(incident);

                logger.info("Saved behavior incident: {}", saved.getId());
                showSuccess("Behavior incident report saved successfully");
                closeWindow();
            } catch (Exception e) {
                logger.error("Error saving incident", e);
                showError("Failed to save incident: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSaveDraft() {
        try {
            BehaviorIncident incident = buildIncidentFromForm();
            incident.setStatus(BehaviorIncident.IncidentStatus.DRAFT);

            BehaviorIncident saved = behaviorIncidentService.saveIncident(incident);

            logger.info("Saved behavior incident draft: {}", saved.getId());
            showSuccess("Behavior incident draft saved successfully");
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
        alert.setHeaderText("Discard Changes?");
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
        if (incidentDatePicker.getValue() == null) {
            errors.add("Incident date is required");
        }
        if (incidentTimeField.getText() == null || incidentTimeField.getText().isEmpty()) {
            errors.add("Incident time is required");
        }
        if (reportingStaffComboBox.getValue() == null) {
            errors.add("Reporting staff is required");
        }
        if (locationComboBox.getValue() == null) {
            errors.add("Location is required");
        }
        if (severityComboBox.getValue() == null) {
            errors.add("Severity is required");
        }
        if (behaviorTypeComboBox.getValue() == null) {
            errors.add("Behavior type is required");
        }
        if (descriptionTextArea.getText() == null || descriptionTextArea.getText().trim().isEmpty()) {
            errors.add("Description is required");
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

    private BehaviorIncident buildIncidentFromForm() {
        BehaviorIncident incident = currentIncident != null ? currentIncident : new BehaviorIncident();

        incident.setStudent(studentComboBox.getValue());
        incident.setIncidentDate(incidentDatePicker.getValue());

        // Parse time
        try {
            LocalTime time = LocalTime.parse(incidentTimeField.getText(), DateTimeFormatter.ofPattern("HH:mm"));
            incident.setIncidentTime(time);
        } catch (Exception e) {
            logger.warn("Failed to parse time: {}", incidentTimeField.getText());
        }

        incident.setReportingStaff(reportingStaffComboBox.getValue());
        incident.setLocation(locationComboBox.getValue());
        if (severityComboBox.getValue() != null && !severityComboBox.getValue().isEmpty()) {
            incident.setSeverity(BehaviorIncident.SeverityLevel.valueOf(severityComboBox.getValue()));
        }
        if (behaviorTypeComboBox.getValue() != null && !behaviorTypeComboBox.getValue().isEmpty()) {
            incident.setBehaviorType(BehaviorIncident.BehaviorType.valueOf(behaviorTypeComboBox.getValue()));
        }
        incident.setDescription(descriptionTextArea.getText());
        incident.setActionTaken(actionTakenTextArea.getText());
        incident.setParentContacted(parentContactedCheckBox.isSelected());
        incident.setParentContactDate(parentContactDatePicker.getValue());
        if (contactMethodComboBox.getValue() != null && !contactMethodComboBox.getValue().isEmpty()) {
            incident.setParentContactMethod(BehaviorIncident.ContactMethod.valueOf(contactMethodComboBox.getValue()));
        }
        incident.setAdministrativeReferral(administrativeReferralCheckBox.isSelected());
        incident.setNotes(notesTextArea.getText());

        return incident;
    }

    public void setIncident(BehaviorIncident incident) {
        this.currentIncident = incident;
        if (incident != null) {
            populateForm(incident);
        }
    }

    private void populateForm(BehaviorIncident incident) {
        studentComboBox.setValue(incident.getStudent());
        incidentDatePicker.setValue(incident.getIncidentDate());
        if (incident.getIncidentTime() != null) {
            incidentTimeField.setText(incident.getIncidentTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        }
        reportingStaffComboBox.setValue(incident.getReportingStaff());
        locationComboBox.setValue(incident.getLocation());
        if (incident.getSeverity() != null) {
            severityComboBox.setValue(incident.getSeverity().name());
        }
        if (incident.getBehaviorType() != null) {
            behaviorTypeComboBox.setValue(incident.getBehaviorType().name());
        }
        descriptionTextArea.setText(incident.getDescription());
        actionTakenTextArea.setText(incident.getActionTaken());
        parentContactedCheckBox.setSelected(incident.isParentContacted());
        parentContactDatePicker.setValue(incident.getParentContactDate());
        if (incident.getParentContactMethod() != null) {
            contactMethodComboBox.setValue(incident.getParentContactMethod().name());
        }
        administrativeReferralCheckBox.setSelected(incident.isAdministrativeReferral());
        notesTextArea.setText(incident.getNotes());
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
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}
