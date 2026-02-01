package com.heronix.ui.controller;

import com.heronix.model.domain.HallPassSession;
import com.heronix.model.domain.HallPassSession.Destination;
import com.heronix.model.domain.HallPassSession.SessionStatus;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.Teacher;
import com.heronix.repository.StudentRepository;
import com.heronix.repository.TeacherRepository;
import com.heronix.service.impl.HallPassService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Hall Pass Management Controller
 *
 * Digital hall pass system with QR code verification.
 * Provides interfaces for:
 * - Active hall pass monitoring
 * - Hall pass request management
 * - Student history tracking
 * - Overdue pass alerts
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Attendance Enhancement
 */
@Slf4j
@Component
public class HallPassController {

    @Autowired
    private HallPassService hallPassService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    // ========================================================================
    // FXML Components
    // ========================================================================

    @FXML private VBox rootContainer;
    @FXML private TabPane mainTabPane;

    // Active Passes Tab
    @FXML private Tab activePassesTab;
    @FXML private TableView<ActivePassRow> activePassesTable;
    @FXML private TableColumn<ActivePassRow, String> studentNameCol;
    @FXML private TableColumn<ActivePassRow, String> destinationCol;
    @FXML private TableColumn<ActivePassRow, String> departureTimeCol;
    @FXML private TableColumn<ActivePassRow, String> durationCol;
    @FXML private TableColumn<ActivePassRow, String> statusCol;
    @FXML private TableColumn<ActivePassRow, String> roomCol;
    @FXML private Label activeCountLabel;
    @FXML private Label overdueCountLabel;

    // New Pass Tab
    @FXML private Tab newPassTab;
    @FXML private ComboBox<Student> studentComboBox;
    @FXML private ComboBox<Destination> destinationComboBox;
    @FXML private ComboBox<Teacher> teacherComboBox;
    @FXML private TextField departureRoomField;
    @FXML private Spinner<Integer> periodSpinner;
    @FXML private TextArea notesArea;
    @FXML private TextArea resultArea;

    // History Tab
    @FXML private Tab historyTab;
    @FXML private ComboBox<Student> historyStudentComboBox;
    @FXML private TableView<HistoryRow> historyTable;
    @FXML private TableColumn<HistoryRow, String> historyDestinationCol;
    @FXML private TableColumn<HistoryRow, String> historyDepartureCol;
    @FXML private TableColumn<HistoryRow, String> historyReturnCol;
    @FXML private TableColumn<HistoryRow, String> historyDurationCol;
    @FXML private TableColumn<HistoryRow, String> historyStatusCol;

    // Observable Lists
    private final ObservableList<ActivePassRow> activePassesData = FXCollections.observableArrayList();
    private final ObservableList<HistoryRow> historyData = FXCollections.observableArrayList();

    // ========================================================================
    // Initialization
    // ========================================================================

    @FXML
    public void initialize() {
        log.info("Initializing HallPassController");

        setupActivePassesTab();
        setupNewPassTab();
        setupHistoryTab();

        loadStudents();
        loadTeachers();
        loadActivePasses();
    }

    private void setupActivePassesTab() {
        // Setup active passes table
        if (studentNameCol != null) {
            studentNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentName()));
            destinationCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDestination()));
            departureTimeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDepartureTime()));
            durationCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDuration()));
            statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
            roomCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRoom()));

            // Style status column based on status
            statusCol.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        if (item.contains("OVERDUE")) {
                            setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
                        } else if (item.contains("ACTIVE")) {
                            setStyle("-fx-text-fill: #4CAF50;");
                        } else {
                            setStyle("");
                        }
                    }
                }
            });
        }

        if (activePassesTable != null) {
            activePassesTable.setItems(activePassesData);
        }
    }

    private void setupNewPassTab() {
        // Setup destination combo box
        if (destinationComboBox != null) {
            destinationComboBox.setItems(FXCollections.observableArrayList(Destination.values()));
            destinationComboBox.setConverter(new StringConverter<>() {
                @Override
                public String toString(Destination destination) {
                    return destination != null ? formatDestination(destination) : "";
                }

                @Override
                public Destination fromString(String string) {
                    return null;
                }
            });
        }

        // Setup student combo box
        if (studentComboBox != null) {
            studentComboBox.setConverter(new StringConverter<>() {
                @Override
                public String toString(Student student) {
                    if (student == null) return "";
                    return String.format("%s - %s %s",
                        student.getStudentId(),
                        student.getFirstName(),
                        student.getLastName());
                }

                @Override
                public Student fromString(String string) {
                    return null;
                }
            });
        }

        // Setup teacher combo box
        if (teacherComboBox != null) {
            teacherComboBox.setConverter(new StringConverter<>() {
                @Override
                public String toString(Teacher teacher) {
                    if (teacher == null) return "";
                    return String.format("%s %s",
                        teacher.getFirstName(),
                        teacher.getLastName());
                }

                @Override
                public Teacher fromString(String string) {
                    return null;
                }
            });
        }

        // Setup period spinner
        if (periodSpinner != null) {
            SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 8, 1);
            periodSpinner.setValueFactory(valueFactory);
        }
    }

    private void setupHistoryTab() {
        // Setup history student combo box
        if (historyStudentComboBox != null) {
            historyStudentComboBox.setConverter(new StringConverter<>() {
                @Override
                public String toString(Student student) {
                    if (student == null) return "";
                    return String.format("%s - %s %s",
                        student.getStudentId(),
                        student.getFirstName(),
                        student.getLastName());
                }

                @Override
                public Student fromString(String string) {
                    return null;
                }
            });
        }

        // Setup history table
        if (historyDestinationCol != null) {
            historyDestinationCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDestination()));
            historyDepartureCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDepartureTime()));
            historyReturnCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getReturnTime()));
            historyDurationCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDuration()));
            historyStatusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        }

        if (historyTable != null) {
            historyTable.setItems(historyData);
        }
    }

    // ========================================================================
    // Data Loading
    // ========================================================================

    private void loadStudents() {
        try {
            List<Student> students = studentRepository.findAllActive()
                .stream()
                .sorted((s1, s2) -> {
                    String name1 = s1.getLastName() + " " + s1.getFirstName();
                    String name2 = s2.getLastName() + " " + s2.getFirstName();
                    return name1.compareTo(name2);
                })
                .collect(Collectors.toList());

            if (studentComboBox != null) {
                studentComboBox.setItems(FXCollections.observableArrayList(students));
            }
            if (historyStudentComboBox != null) {
                historyStudentComboBox.setItems(FXCollections.observableArrayList(students));
            }
        } catch (Exception e) {
            log.error("Error loading students", e);
            showError("Error loading students: " + e.getMessage());
        }
    }

    private void loadTeachers() {
        try {
            List<Teacher> teachers = teacherRepository.findAll()
                .stream()
                .sorted((t1, t2) -> {
                    String name1 = t1.getLastName() + " " + t1.getFirstName();
                    String name2 = t2.getLastName() + " " + t2.getFirstName();
                    return name1.compareTo(name2);
                })
                .collect(Collectors.toList());

            if (teacherComboBox != null) {
                teacherComboBox.setItems(FXCollections.observableArrayList(teachers));
            }
        } catch (Exception e) {
            log.error("Error loading teachers", e);
            showError("Error loading teachers: " + e.getMessage());
        }
    }

    @FXML
    private void loadActivePasses() {
        try {
            List<HallPassSession> sessions = hallPassService.getActiveSessions();

            activePassesData.clear();
            int overdueCount = 0;

            for (HallPassSession session : sessions) {
                String studentName = session.getStudent() != null ?
                    session.getStudent().getFullName() : "Unknown";

                String destination = formatDestination(session.getDestination());

                String departureTime = session.getDepartureTime() != null ?
                    session.getDepartureTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "N/A";

                String duration = calculateDuration(session.getDepartureTime(), LocalDateTime.now());

                String status = session.getStatus() != null ? session.getStatus().toString() : "UNKNOWN";
                if (session.getStatus() == SessionStatus.OVERDUE) {
                    overdueCount++;
                }

                String room = session.getDepartureRoom() != null ? session.getDepartureRoom() : "N/A";

                activePassesData.add(new ActivePassRow(
                    session.getId(),
                    studentName,
                    destination,
                    departureTime,
                    duration,
                    status,
                    room
                ));
            }

            // Update summary labels
            if (activeCountLabel != null) {
                activeCountLabel.setText(String.valueOf(sessions.size()));
            }
            if (overdueCountLabel != null) {
                overdueCountLabel.setText(String.valueOf(overdueCount));
            }

            log.info("Loaded {} active hall passes ({} overdue)", sessions.size(), overdueCount);

        } catch (Exception e) {
            log.error("Error loading active passes", e);
            showError("Error loading active passes: " + e.getMessage());
        }
    }

    // ========================================================================
    // New Pass Handlers
    // ========================================================================

    @FXML
    private void handleCreatePass() {
        try {
            Student student = studentComboBox.getValue();
            Destination destination = destinationComboBox.getValue();
            Teacher teacher = teacherComboBox.getValue();
            String departureRoom = departureRoomField.getText();
            Integer period = periodSpinner.getValue();

            if (student == null) {
                showWarning("Please select a student");
                return;
            }
            if (destination == null) {
                showWarning("Please select a destination");
                return;
            }
            if (teacher == null) {
                showWarning("Please select approving teacher");
                return;
            }
            if (departureRoom == null || departureRoom.trim().isEmpty()) {
                showWarning("Please enter departure room");
                return;
            }

            // Create hall pass (without QR/facial recognition for manual entry)
            String qrCode = student.getQrCodeId();
            if (qrCode == null || qrCode.isEmpty()) {
                showWarning("Student does not have a QR code assigned");
                return;
            }

            HallPassService.HallPassResult result = hallPassService.startHallPass(
                qrCode,
                null, // No photo for manual entry
                destination,
                teacher.getId(),
                period,
                departureRoom
            );

            // Display result
            StringBuilder output = new StringBuilder();
            output.append("=== HALL PASS CREATED ===\n\n");
            output.append("Student: ").append(student.getFullName()).append("\n");
            output.append("Destination: ").append(formatDestination(destination)).append("\n");
            output.append("Approved By: ").append(teacher.getFirstName()).append(" ").append(teacher.getLastName()).append("\n");
            output.append("Period: ").append(period).append("\n");
            output.append("Departure Room: ").append(departureRoom).append("\n");
            output.append("\nStatus: ").append(result.isSuccess() ? "✅ SUCCESS" : "❌ FAILED").append("\n");
            output.append("Message: ").append(result.getMessage()).append("\n");

            if (result.isSuccess()) {
                output.append("\nPass expires in 15 minutes.\n");
            }

            resultArea.setText(output.toString());

            if (result.isSuccess()) {
                // Clear form
                studentComboBox.setValue(null);
                destinationComboBox.setValue(null);
                departureRoomField.clear();
                notesArea.clear();

                // Refresh active passes
                loadActivePasses();

                showSuccess("Hall pass created successfully");
            } else {
                showError(result.getMessage());
            }

        } catch (Exception e) {
            log.error("Error creating hall pass", e);
            showError("Error creating hall pass: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearForm() {
        studentComboBox.setValue(null);
        destinationComboBox.setValue(null);
        teacherComboBox.setValue(null);
        departureRoomField.clear();
        notesArea.clear();
        resultArea.clear();
    }

    // ========================================================================
    // History Handlers
    // ========================================================================

    @FXML
    private void handleLoadHistory() {
        try {
            Student student = historyStudentComboBox.getValue();
            if (student == null) {
                showWarning("Please select a student");
                return;
            }

            List<HallPassSession> sessions = hallPassService.getStudentHistory(student.getId());

            historyData.clear();
            for (HallPassSession session : sessions) {
                String destination = formatDestination(session.getDestination());

                String departureTime = session.getDepartureTime() != null ?
                    session.getDepartureTime().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")) : "N/A";

                String returnTime = session.getReturnTime() != null ?
                    session.getReturnTime().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")) : "Not returned";

                String duration = session.getReturnTime() != null ?
                    calculateDuration(session.getDepartureTime(), session.getReturnTime()) : "In progress";

                String status = session.getStatus() != null ? session.getStatus().toString() : "UNKNOWN";

                historyData.add(new HistoryRow(
                    destination,
                    departureTime,
                    returnTime,
                    duration,
                    status
                ));
            }

            log.info("Loaded {} hall pass history entries for student {}", sessions.size(), student.getStudentId());

        } catch (Exception e) {
            log.error("Error loading hall pass history", e);
            showError("Error loading history: " + e.getMessage());
        }
    }

    // ========================================================================
    // Action Handlers
    // ========================================================================

    @FXML
    private void handleRefresh() {
        loadActivePasses();
        showSuccess("Active passes refreshed");
    }

    @FXML
    private void handleEndPass() {
        try {
            ActivePassRow selectedRow = activePassesTable.getSelectionModel().getSelectedItem();
            if (selectedRow == null) {
                showWarning("Please select a hall pass to end");
                return;
            }

            // Create dialog for manual end pass
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("End Hall Pass");
            dialog.setHeaderText("Manually end hall pass for " + selectedRow.getStudentName() + "?");

            // Set the button types
            ButtonType endButtonType = new ButtonType("End Pass", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(endButtonType, ButtonType.CANCEL);

            // Create the content
            VBox content = new VBox(10);
            content.setStyle("-fx-padding: 10;");

            Label infoLabel = new Label("Note: For standard returns, use the QR scanner.\nManual end should only be used when QR scanning is not possible.");
            infoLabel.setWrapText(true);
            infoLabel.setStyle("-fx-font-style: italic;");

            Label reasonLabel = new Label("Reason for manual end (required):");
            TextArea reasonField = new TextArea();
            reasonField.setPromptText("e.g., Student ID card lost, QR scanner malfunction, etc.");
            reasonField.setPrefRowCount(2);
            reasonField.setWrapText(true);

            Label roomLabel = new Label("Return room (optional):");
            TextField roomField = new TextField();
            roomField.setPromptText("Room number/name");

            content.getChildren().addAll(infoLabel, reasonLabel, reasonField, roomLabel, roomField);
            dialog.getDialogPane().setContent(content);

            // Disable end button until reason is provided
            dialog.getDialogPane().lookupButton(endButtonType).setDisable(true);
            reasonField.textProperty().addListener((obs, oldVal, newVal) -> {
                dialog.getDialogPane().lookupButton(endButtonType).setDisable(
                    newVal == null || newVal.trim().isEmpty()
                );
            });

            // Convert the result
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == endButtonType) {
                    return reasonField.getText();
                }
                return null;
            });

            // Show dialog and process result
            dialog.showAndWait().ifPresent(reason -> {
                HallPassService.HallPassResult result = hallPassService.manualEndHallPass(
                    selectedRow.getSessionId(),
                    roomField.getText(),
                    reason
                );

                if (result.isSuccess()) {
                    showSuccess("Hall pass ended successfully.\nDuration: " +
                        (result.getSession() != null ? result.getSession().getFormattedDuration() : "N/A"));
                    loadActivePasses(); // Refresh the table
                } else {
                    showError("Failed to end hall pass: " + result.getMessage());
                }
            });

        } catch (Exception e) {
            log.error("Error ending hall pass", e);
            showError("Error ending hall pass: " + e.getMessage());
        }
    }

    // ========================================================================
    // Helper Methods
    // ========================================================================

    private String formatDestination(Destination destination) {
        if (destination == null) return "Unknown";
        return switch (destination) {
            case BATHROOM -> "🚻 Bathroom";
            case CLINIC -> "🏥 Nurse/Clinic";
            case ADMIN_OFFICE -> "🏢 Admin Office";
            case COUNSELOR -> "💬 Counselor";
            case LIBRARY -> "📚 Library";
            case CAFETERIA -> "🍽️ Cafeteria";
            case ANOTHER_CLASSROOM -> "📖 Another Classroom";
            case LOCKER -> "🔐 Locker";
            case WATER_FOUNTAIN -> "💧 Water Fountain";
            case OFFICE -> "🏢 Main Office";
            case OTHER -> "📍 Other";
        };
    }

    private String calculateDuration(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return "N/A";

        Duration duration = Duration.between(start, end);
        long minutes = duration.toMinutes();
        long seconds = duration.getSeconds() % 60;

        return String.format("%d:%02d", minutes, seconds);
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

    // ========================================================================
    // Data Classes
    // ========================================================================

    @Data
    public static class ActivePassRow {
        private final Long sessionId;
        private final String studentName;
        private final String destination;
        private final String departureTime;
        private final String duration;
        private final String status;
        private final String room;
    }

    @Data
    public static class HistoryRow {
        private final String destination;
        private final String departureTime;
        private final String returnTime;
        private final String duration;
        private final String status;
    }
}
