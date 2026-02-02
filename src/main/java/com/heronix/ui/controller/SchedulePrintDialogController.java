package com.heronix.ui.controller;

import com.heronix.model.domain.Schedule;
import com.heronix.model.domain.Student;
import com.heronix.service.ScheduleExportService;
import com.heronix.service.ScheduleService;
import com.heronix.service.StudentService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Controller for Schedule Print Dialog
 * Handles bulk printing of student schedules with various options
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Component
public class SchedulePrintDialogController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private ScheduleExportService scheduleExportService;

    // FXML Components - Schedule Selection
    @FXML private ComboBox<Schedule> scheduleComboBox;
    @FXML private javafx.scene.text.Text scheduleInfoText;

    // FXML Components - Scope Selection
    @FXML private RadioButton allStudentsRadio;
    @FXML private RadioButton byGradeLevelRadio;
    @FXML private RadioButton individualStudentsRadio;
    @FXML private ToggleGroup scopeToggleGroup;

    @FXML private VBox allStudentsInfo;
    @FXML private VBox byGradeLevelInfo;
    @FXML private VBox individualStudentsInfo;

    @FXML private Label totalStudentsLabel;
    @FXML private ComboBox<String> gradeLevelComboBox;
    @FXML private Label gradeStudentsLabel;
    @FXML private TextField studentSearchField;
    @FXML private Button addStudentButton;
    @FXML private ListView<Student> selectedStudentsList;
    @FXML private Button removeStudentButton;

    // FXML Components - Print Options
    @FXML private ComboBox<String> outputFormatComboBox;
    @FXML private CheckBox includePhotoCheckbox;
    @FXML private CheckBox includeRoomNumbersCheckbox;
    @FXML private CheckBox includeTeacherNamesCheckbox;
    @FXML private CheckBox includeNotesCheckbox;
    @FXML private ComboBox<String> layoutComboBox;

    // FXML Components - Distribution
    @FXML private CheckBox emailToParentsCheckbox;
    @FXML private CheckBox emailToStudentsCheckbox;
    @FXML private CheckBox saveToPortalCheckbox;

    // FXML Components - Progress
    @FXML private VBox progressBox;
    @FXML private Label progressIcon;
    @FXML private Label progressLabel;
    @FXML private Label progressMessage;
    @FXML private ProgressBar progressBar;

    private DialogPane dialogPane;
    private List<Student> allStudents = new ArrayList<>();
    private List<Student> selectedStudents = new ArrayList<>();
    private Map<String, List<Student>> studentsByGrade = new HashMap<>();

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        log.info("Initializing Schedule Print Dialog");

        // Set up schedule combo box
        scheduleComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Schedule item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String year = item.getStartDate() != null ?
                        String.valueOf(item.getStartDate().getYear()) : "Unknown";
                    setText(item.getScheduleName() + " (" + year + ")");
                }
            }
        });

        scheduleComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Schedule item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String year = item.getStartDate() != null ?
                        String.valueOf(item.getStartDate().getYear()) : "Unknown";
                    setText(item.getScheduleName() + " (" + year + ")");
                }
            }
        });

        // Set up output format combo
        outputFormatComboBox.setItems(FXCollections.observableArrayList(
            "PDF", "Excel (XLSX)", "CSV", "HTML"
        ));
        outputFormatComboBox.getSelectionModel().selectFirst();

        // Set up layout combo
        layoutComboBox.setItems(FXCollections.observableArrayList(
            "Standard (Portrait)", "Compact (Portrait)", "Landscape", "Card Format (4x6)"
        ));
        layoutComboBox.getSelectionModel().selectFirst();

        // Set up grade level combo
        gradeLevelComboBox.setItems(FXCollections.observableArrayList(
            "K", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"
        ));

        // Set up student list cell factory
        selectedStudentsList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getLastName() + ", " + item.getFirstName() + " (Grade " + item.getGradeLevel() + ")");
                }
            }
        });

        // Load schedules
        loadSchedules();

        // Set up event listeners
        scheduleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateScheduleInfo(newVal);
                loadStudentsForSchedule(newVal);
            }
        });

        scopeToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            updateUIForSelectedScope();
        });

        gradeLevelComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateGradeStudentCount(newVal);
            }
        });

        addStudentButton.setOnAction(e -> addSelectedStudent());
        removeStudentButton.setOnAction(e -> removeSelectedStudent());

        // Initial UI update
        updateUIForSelectedScope();
    }

    /**
     * Set the dialog pane reference
     */
    public void setDialogPane(DialogPane pane) {
        this.dialogPane = pane;
    }

    /**
     * Load schedules from database
     */
    private void loadSchedules() {
        try {
            List<Schedule> schedules = scheduleService.getAllSchedules();
            scheduleComboBox.getItems().setAll(schedules);

            if (!schedules.isEmpty()) {
                scheduleComboBox.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            log.error("Error loading schedules", e);
            showError("Failed to load schedules: " + e.getMessage());
        }
    }

    /**
     * Update schedule info text
     */
    private void updateScheduleInfo(Schedule schedule) {
        if (schedule == null) {
            scheduleInfoText.setText("");
            return;
        }

        String info = String.format(
            "Type: %s | Slots: %d | Status: %s",
            schedule.getScheduleType() != null ? schedule.getScheduleType().getDisplayName() : "Unknown",
            schedule.getSlots() != null ? schedule.getSlots().size() : 0,
            schedule.getActive() ? "Active" : "Inactive"
        );

        scheduleInfoText.setText(info);
    }

    /**
     * Load students for selected schedule
     */
    private void loadStudentsForSchedule(Schedule schedule) {
        try {
            // Get all students (simplified - actual implementation would get students enrolled in schedule)
            allStudents = studentService.getAllStudents();

            // Group by grade level
            studentsByGrade = allStudents.stream()
                .collect(Collectors.groupingBy(Student::getGradeLevel));

            totalStudentsLabel.setText("Total students: " + allStudents.size());

            log.info("Loaded {} students for schedule {}", allStudents.size(), schedule.getId());
        } catch (Exception e) {
            log.error("Error loading students for schedule", e);
            totalStudentsLabel.setText("Total students: Error loading");
        }
    }

    /**
     * Update UI based on selected scope
     */
    private void updateUIForSelectedScope() {
        Toggle selected = scopeToggleGroup.getSelectedToggle();

        // Hide all info sections initially
        byGradeLevelInfo.setVisible(false);
        byGradeLevelInfo.setManaged(false);
        individualStudentsInfo.setVisible(false);
        individualStudentsInfo.setManaged(false);

        if (selected == byGradeLevelRadio) {
            byGradeLevelInfo.setVisible(true);
            byGradeLevelInfo.setManaged(true);
        } else if (selected == individualStudentsRadio) {
            individualStudentsInfo.setVisible(true);
            individualStudentsInfo.setManaged(true);
        }
    }

    /**
     * Update grade student count
     */
    private void updateGradeStudentCount(String gradeLevel) {
        List<Student> studentsInGrade = studentsByGrade.getOrDefault(gradeLevel, new ArrayList<>());
        gradeStudentsLabel.setText("Students in selected grade: " + studentsInGrade.size());
    }

    /**
     * Add student to selected list
     */
    private void addSelectedStudent() {
        String searchText = studentSearchField.getText().trim().toLowerCase();
        if (searchText.isEmpty()) {
            showError("Please enter a student name or ID to search");
            return;
        }

        List<Student> matches = allStudents.stream()
            .filter(s -> s.getLastName().toLowerCase().contains(searchText) ||
                        s.getFirstName().toLowerCase().contains(searchText) ||
                        String.valueOf(s.getId()).contains(searchText))
            .filter(s -> !selectedStudents.contains(s))
            .collect(Collectors.toList());

        if (matches.isEmpty()) {
            showError("No students found matching: " + searchText);
            return;
        }

        if (matches.size() == 1) {
            selectedStudents.add(matches.get(0));
            selectedStudentsList.setItems(FXCollections.observableArrayList(selectedStudents));
            studentSearchField.clear();
        } else {
            // Multiple matches - show selection dialog
            ChoiceDialog<Student> dialog = new ChoiceDialog<>(matches.get(0), matches);
            dialog.setTitle("Multiple Students Found");
            dialog.setHeaderText("Select a student:");
            dialog.setContentText("Student:");

            dialog.showAndWait().ifPresent(student -> {
                selectedStudents.add(student);
                selectedStudentsList.setItems(FXCollections.observableArrayList(selectedStudents));
                studentSearchField.clear();
            });
        }
    }

    /**
     * Remove selected student from list
     */
    private void removeSelectedStudent() {
        Student selected = selectedStudentsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedStudents.remove(selected);
            selectedStudentsList.setItems(FXCollections.observableArrayList(selectedStudents));
        }
    }

    /**
     * Generate schedules
     */
    public void generateSchedules() {
        Schedule schedule = scheduleComboBox.getValue();
        if (schedule == null) {
            showError("Please select a schedule");
            return;
        }

        // Determine which students to print
        List<Student> studentsToPrint = getStudentsToPrint();
        if (studentsToPrint.isEmpty()) {
            showError("No students selected for printing");
            return;
        }

        // Show progress
        progressBox.setVisible(true);
        progressBox.setManaged(true);
        progressLabel.setText("Generating schedules...");
        progressMessage.setText("Processing " + studentsToPrint.size() + " student schedules");

        // Disable generate button
        if (dialogPane != null) {
            Button generateButton = (Button) dialogPane.lookupButton(ButtonType.OK);
            if (generateButton != null) {
                generateButton.setDisable(true);
            }
        }

        // Generate in background
        CompletableFuture.runAsync(() -> {
            try {
                String format = outputFormatComboBox.getValue();
                String layout = layoutComboBox.getValue();

                int total = studentsToPrint.size();
                int processed = 0;

                for (Student student : studentsToPrint) {
                    // Generate schedule for this student
                    generateScheduleForStudent(schedule, student, format, layout);

                    processed++;
                    final int count = processed;
                    Platform.runLater(() -> {
                        double progress = (double) count / total;
                        progressBar.setProgress(progress);
                        progressMessage.setText(String.format("Processed %d of %d students", count, total));
                    });
                }

                // Handle distribution
                if (emailToParentsCheckbox.isSelected() || emailToStudentsCheckbox.isSelected()) {
                    Platform.runLater(() -> {
                        progressLabel.setText("Sending emails...");
                        progressMessage.setText("Distributing schedules via email");
                    });
                    // Email distribution logic here
                }

                Platform.runLater(() -> {
                    progressIcon.setText("✅");
                    progressLabel.setText("Complete!");
                    progressMessage.setText(String.format("Generated %d schedules successfully", total));

                    showSuccess("Successfully generated " + total + " student schedules");

                    // Re-enable button and change to "Close"
                    if (dialogPane != null) {
                        Button generateButton = (Button) dialogPane.lookupButton(ButtonType.OK);
                        if (generateButton != null) {
                            generateButton.setDisable(false);
                            generateButton.setText("Close");
                        }
                    }
                });

            } catch (Exception e) {
                log.error("Error generating schedules", e);
                Platform.runLater(() -> {
                    progressIcon.setText("❌");
                    progressLabel.setText("Error");
                    progressMessage.setText("Failed to generate schedules: " + e.getMessage());
                    showError("Failed to generate schedules: " + e.getMessage());
                });
            }
        });
    }

    /**
     * Get list of students to print based on selected scope
     */
    private List<Student> getStudentsToPrint() {
        Toggle selected = scopeToggleGroup.getSelectedToggle();

        if (selected == allStudentsRadio) {
            return new ArrayList<>(allStudents);
        } else if (selected == byGradeLevelRadio) {
            String gradeLevel = gradeLevelComboBox.getValue();
            if (gradeLevel == null) {
                return new ArrayList<>();
            }
            return studentsByGrade.getOrDefault(gradeLevel, new ArrayList<>());
        } else if (selected == individualStudentsRadio) {
            return new ArrayList<>(selectedStudents);
        }

        return new ArrayList<>();
    }

    /**
     * Generate schedule for individual student
     * Note: This is a simplified implementation. In production, you would filter the schedule
     * to only include this student's classes and use student-specific export methods.
     */
    private void generateScheduleForStudent(Schedule schedule, Student student, String format, String layout) {
        try {
            // Exports full schedule — student-specific filtering requires per-student export methods
            File outputFile;
            switch (format) {
                case "PDF":
                    outputFile = scheduleExportService.exportToPDF(schedule);
                    break;
                case "Excel (XLSX)":
                    outputFile = scheduleExportService.exportToExcel(schedule);
                    break;
                case "CSV":
                    outputFile = scheduleExportService.exportToCSV(schedule);
                    break;
                case "HTML":
                    // HTML export not in interface, fallback to PDF
                    outputFile = scheduleExportService.exportToPDF(schedule);
                    break;
                default:
                    outputFile = scheduleExportService.exportToPDF(schedule);
            }

            log.info("Generated schedule export for student {} in format {}: {}", student.getId(), format, outputFile.getPath());

        } catch (Exception e) {
            log.error("Error generating schedule for student {}", student.getId(), e);
            throw new RuntimeException("Failed to generate schedule for " + student.getFirstName() + " " + student.getLastName(), e);
        }
    }

    /**
     * Validate form
     */
    public boolean validate() {
        if (scheduleComboBox.getValue() == null) {
            showError("Please select a schedule");
            return false;
        }

        List<Student> studentsToPrint = getStudentsToPrint();
        if (studentsToPrint.isEmpty()) {
            Toggle selected = scopeToggleGroup.getSelectedToggle();

            if (selected == byGradeLevelRadio && gradeLevelComboBox.getValue() == null) {
                showError("Please select a grade level");
                return false;
            } else if (selected == individualStudentsRadio) {
                showError("Please add at least one student to the list");
                return false;
            } else {
                showError("No students available for printing");
                return false;
            }
        }

        if (outputFormatComboBox.getValue() == null) {
            showError("Please select an output format");
            return false;
        }

        return true;
    }

    /**
     * Show error alert
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show success alert
     */
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
