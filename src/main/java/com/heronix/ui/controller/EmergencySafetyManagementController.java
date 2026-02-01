package com.heronix.ui.controller;

import com.heronix.model.domain.EmergencyContact;
import com.heronix.model.domain.Student;
import com.heronix.repository.StudentRepository;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class EmergencySafetyManagementController {

    @Autowired
    private StudentRepository studentRepository;

    // Emergency Contacts tab
    @FXML private TextField searchField;
    @FXML private Label totalLabel;
    @FXML private TableView<ContactRow> contactsTableView;
    @FXML private TableColumn<ContactRow, String> colStudent;
    @FXML private TableColumn<ContactRow, String> colGrade;
    @FXML private TableColumn<ContactRow, String> colContactName;
    @FXML private TableColumn<ContactRow, String> colRelationship;
    @FXML private TableColumn<ContactRow, String> colPhone;
    @FXML private TableColumn<ContactRow, String> colAltPhone;
    @FXML private TableColumn<ContactRow, String> colEmail;
    @FXML private Label contactsStatusLabel;

    // Drills tab
    @FXML private TableView<DrillEntry> drillLogTable;
    @FXML private TableColumn<DrillEntry, String> colDrillType;
    @FXML private TableColumn<DrillEntry, String> colDrillDate;
    @FXML private TableColumn<DrillEntry, String> colDrillDuration;
    @FXML private TableColumn<DrillEntry, String> colDrillNotes;

    private ObservableList<ContactRow> allContacts = FXCollections.observableArrayList();
    private FilteredList<ContactRow> filteredContacts;
    private ObservableList<DrillEntry> drillLog = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        log.info("Initializing EmergencySafetyManagementController");

        // Contact columns
        colStudent.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().studentName));
        colGrade.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().gradeLevel));
        colContactName.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().contactName));
        colRelationship.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().relationship));
        colPhone.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().phone));
        colAltPhone.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().altPhone));
        colEmail.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().email));

        filteredContacts = new FilteredList<>(allContacts, p -> true);
        contactsTableView.setItems(filteredContacts);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String search = newVal != null ? newVal.toLowerCase().trim() : "";
            filteredContacts.setPredicate(r -> {
                if (search.isEmpty()) return true;
                return r.studentName.toLowerCase().contains(search)
                        || r.contactName.toLowerCase().contains(search);
            });
            totalLabel.setText("Total: " + filteredContacts.size());
        });

        // Drill columns
        colDrillType.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().type));
        colDrillDate.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().dateTime));
        colDrillDuration.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().duration));
        colDrillNotes.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().notes));
        drillLogTable.setItems(drillLog);

        loadContacts();
    }

    private void loadContacts() {
        contactsStatusLabel.setText("Loading emergency contacts...");
        new Thread(() -> {
            try {
                List<Student> students = studentRepository.findAllActive();
                List<ContactRow> rows = new ArrayList<>();

                for (Student s : students) {
                    String studentName = s.getFullName();
                    String grade = s.getGradeLevel() != null ? s.getGradeLevel() : "";

                    // Use EmergencyContact list if available
                    if (s.getEmergencyContacts() != null && !s.getEmergencyContacts().isEmpty()) {
                        for (EmergencyContact ec : s.getEmergencyContacts()) {
                            rows.add(new ContactRow(
                                    studentName, grade,
                                    (ec.getFirstName() != null ? ec.getFirstName() : "") + " " +
                                            (ec.getLastName() != null ? ec.getLastName() : ""),
                                    ec.getRelationship() != null ? ec.getRelationship() : "",
                                    ec.getPrimaryPhone() != null ? ec.getPrimaryPhone() : "",
                                    ec.getSecondaryPhone() != null ? ec.getSecondaryPhone() : "",
                                    ec.getEmail() != null ? ec.getEmail() : ""
                            ));
                        }
                    } else if (s.getEmergencyContact() != null) {
                        // Fallback to inline fields
                        rows.add(new ContactRow(
                                studentName, grade,
                                s.getEmergencyContact(),
                                "",
                                s.getEmergencyPhone() != null ? s.getEmergencyPhone() : "",
                                "", ""
                        ));
                    }
                }

                log.info("Loaded {} emergency contact records for {} students", rows.size(), students.size());

                Platform.runLater(() -> {
                    allContacts.setAll(rows);
                    totalLabel.setText("Total: " + rows.size());
                    contactsStatusLabel.setText("Loaded " + rows.size() + " contacts for " + students.size() + " students");
                });
            } catch (Exception e) {
                log.error("Failed to load emergency contacts", e);
                Platform.runLater(() -> contactsStatusLabel.setText("Error: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleRefresh() {
        loadContacts();
    }

    @FXML
    private void handleFireDrill() {
        recordDrill("Fire Drill");
    }

    @FXML
    private void handleLockdownDrill() {
        recordDrill("Lockdown Drill");
    }

    @FXML
    private void handleTornadoDrill() {
        recordDrill("Tornado Drill");
    }

    @FXML
    private void handleEvacuationDrill() {
        recordDrill("Evacuation Drill");
    }

    private void recordDrill(String type) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Record " + type);
        dialog.setHeaderText("Enter drill duration (e.g., 3 min 45 sec):");
        dialog.setContentText("Duration:");

        Optional<String> duration = dialog.showAndWait();
        if (duration.isPresent()) {
            TextInputDialog notesDialog = new TextInputDialog();
            notesDialog.setTitle("Record " + type);
            notesDialog.setHeaderText("Enter any notes or observations:");
            notesDialog.setContentText("Notes:");

            String notes = notesDialog.showAndWait().orElse("");

            DrillEntry entry = new DrillEntry(
                    type,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    duration.get(),
                    notes
            );
            drillLog.add(0, entry);
            log.info("Recorded {}: duration={}, notes={}", type, duration.get(), notes);

            Alert confirm = new Alert(Alert.AlertType.INFORMATION);
            confirm.setTitle(type + " Recorded");
            confirm.setHeaderText(null);
            confirm.setContentText(type + " recorded at " + entry.dateTime + "\nDuration: " + entry.duration);
            confirm.showAndWait();
        }
    }

    // Inner row classes
    public static class ContactRow {
        final String studentName, gradeLevel, contactName, relationship, phone, altPhone, email;

        ContactRow(String studentName, String gradeLevel, String contactName, String relationship,
                   String phone, String altPhone, String email) {
            this.studentName = studentName;
            this.gradeLevel = gradeLevel;
            this.contactName = contactName;
            this.relationship = relationship;
            this.phone = phone;
            this.altPhone = altPhone;
            this.email = email;
        }
    }

    public static class DrillEntry {
        final String type, dateTime, duration, notes;

        DrillEntry(String type, String dateTime, String duration, String notes) {
            this.type = type;
            this.dateTime = dateTime;
            this.duration = duration;
            this.notes = notes;
        }
    }
}
