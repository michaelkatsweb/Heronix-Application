package com.heronix.ui.controller;

import com.heronix.model.domain.AttendanceRecord;
import com.heronix.model.domain.AttendanceRecord.AttendanceStatus;
import com.heronix.model.domain.Campus;
import com.heronix.model.domain.Course;
import com.heronix.model.domain.Student;
import com.heronix.repository.AttendanceRepository;
import com.heronix.repository.CampusRepository;
import com.heronix.repository.CourseRepository;
import com.heronix.service.AttendanceNotificationService;
import com.heronix.service.AttendanceDocumentService;
import com.heronix.service.TruancyInterventionService;
import com.heronix.service.impl.AttendanceService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Attendance Management Controller
 *
 * Main UI controller for attendance recording, tracking, and management.
 * Provides interfaces for period-based attendance, daily summaries, and quick actions.
 *
 * Key Features:
 * - Period-by-period attendance recording
 * - Daily attendance dashboard
 * - Chronic absence alerts
 * - Quick attendance actions
 * - Notification management
 * - Document generation
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Attendance Enhancement
 */
@Slf4j
@Component
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private AttendanceNotificationService notificationService;

    @Autowired
    private AttendanceDocumentService documentService;

    @Autowired
    private TruancyInterventionService truancyService;

    @Autowired
    private CampusRepository campusRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    // ========================================================================
    // FXML Components - Main Dashboard
    // ========================================================================

    @FXML private VBox rootContainer;
    @FXML private TabPane mainTabPane;

    // Dashboard Tab
    @FXML private Tab dashboardTab;
    @FXML private Label todayDateLabel;
    @FXML private Label totalStudentsLabel;
    @FXML private Label presentCountLabel;
    @FXML private Label absentCountLabel;
    @FXML private Label tardyCountLabel;
    @FXML private Label attendanceRateLabel;
    @FXML private ProgressBar attendanceRateProgress;
    @FXML private TableView<ChronicAbsenceAlertRow> chronicAbsenceTable;
    @FXML private TableColumn<ChronicAbsenceAlertRow, String> studentNameCol;
    @FXML private TableColumn<ChronicAbsenceAlertRow, String> gradeLevelCol;
    @FXML private TableColumn<ChronicAbsenceAlertRow, Integer> absencesCol;
    @FXML private TableColumn<ChronicAbsenceAlertRow, Double> rateCol;
    @FXML private TableColumn<ChronicAbsenceAlertRow, String> actionsCol;

    // Period Attendance Tab
    @FXML private Tab periodAttendanceTab;
    @FXML private DatePicker attendanceDatePicker;
    @FXML private ComboBox<Campus> campusComboBox;
    @FXML private ComboBox<Course> courseComboBox;
    @FXML private ComboBox<Integer> periodComboBox;
    @FXML private Button loadRosterButton;
    @FXML private TableView<StudentAttendanceRow> periodAttendanceTable;
    @FXML private TableColumn<StudentAttendanceRow, String> studentIdCol;
    @FXML private TableColumn<StudentAttendanceRow, String> studentNamePeriodCol;
    @FXML private TableColumn<StudentAttendanceRow, String> gradeCol;
    @FXML private TableColumn<StudentAttendanceRow, AttendanceStatus> statusCol;
    @FXML private TableColumn<StudentAttendanceRow, String> notesCol;
    @FXML private TableColumn<StudentAttendanceRow, String> periodActionsCol;
    @FXML private Button saveAttendanceButton;
    @FXML private Button markAllPresentButton;
    @FXML private Label rosterCountLabel;

    // Quick Actions Tab
    @FXML private Tab quickActionsTab;
    @FXML private DatePicker quickDatePicker;
    @FXML private ComboBox<Campus> quickCampusComboBox;
    @FXML private Button generateDailyReportButton;
    @FXML private Button sendDailyNotificationsButton;
    @FXML private Button viewChronicAbsencesButton;
    @FXML private Button generateTruancyReportButton;
    @FXML private TextArea quickActionsResultArea;

    private ObservableList<ChronicAbsenceAlertRow> chronicAbsenceData = FXCollections.observableArrayList();
    private ObservableList<StudentAttendanceRow> periodAttendanceData = FXCollections.observableArrayList();

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    @FXML
    public void initialize() {
        log.info("Initializing AttendanceController");

        setupDashboardTab();
        setupPeriodAttendanceTab();
        setupQuickActionsTab();

        // Load initial data
        loadDashboardData();
        loadCampuses();

        log.info("AttendanceController initialized successfully");
    }

    private void setupDashboardTab() {
        // Set today's date
        todayDateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));

        // Setup chronic absence table
        studentNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentName()));
        gradeLevelCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGradeLevel()));
        absencesCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getAbsenceCount()));
        rateCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getAbsenceRate()));

        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button actionButton = new Button("View Details");
            {
                actionButton.setOnAction(e -> {
                    ChronicAbsenceAlertRow row = getTableRow().getItem();
                    if (row != null) {
                        handleViewStudentDetails(row.getStudentId());
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(actionButton);
                }
            }
        });

        chronicAbsenceTable.setItems(chronicAbsenceData);
    }

    private void setupPeriodAttendanceTab() {
        // Setup date picker
        attendanceDatePicker.setValue(LocalDate.now());
        attendanceDatePicker.setConverter(new StringConverter<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

            @Override
            public String toString(LocalDate date) {
                return date != null ? formatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return string != null && !string.isEmpty() ? LocalDate.parse(string, formatter) : null;
            }
        });

        // Setup period combo box
        periodComboBox.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8));
        periodComboBox.getSelectionModel().selectFirst();

        // Setup period attendance table
        studentIdCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentId()));
        studentNamePeriodCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentName()));
        gradeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGradeLevel()));

        statusCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getStatus()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<AttendanceStatus> statusComboBox = new ComboBox<>();
            {
                statusComboBox.setItems(FXCollections.observableArrayList(AttendanceStatus.values()));
                statusComboBox.setOnAction(e -> {
                    StudentAttendanceRow row = getTableRow().getItem();
                    if (row != null) {
                        row.setStatus(statusComboBox.getValue());
                    }
                });
            }

            @Override
            protected void updateItem(AttendanceStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    statusComboBox.setValue(item != null ? item : AttendanceStatus.PRESENT);
                    setGraphic(statusComboBox);
                }
            }
        });

        notesCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNotes()));
        notesCol.setCellFactory(col -> new TableCell<>() {
            private final TextField notesField = new TextField();
            {
                notesField.setOnAction(e -> {
                    StudentAttendanceRow row = getTableRow().getItem();
                    if (row != null) {
                        row.setNotes(notesField.getText());
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    notesField.setText(item != null ? item : "");
                    setGraphic(notesField);
                }
            }
        });

        periodAttendanceTable.setItems(periodAttendanceData);

        // Setup buttons
        loadRosterButton.setOnAction(e -> handleLoadRoster());
        saveAttendanceButton.setOnAction(e -> handleSaveAttendance());
        markAllPresentButton.setOnAction(e -> handleMarkAllPresent());

        // Load courses when campus is selected
        campusComboBox.setOnAction(e -> loadCourses());
    }

    private void setupQuickActionsTab() {
        quickDatePicker.setValue(LocalDate.now());

        generateDailyReportButton.setOnAction(e -> handleGenerateDailyReport());
        sendDailyNotificationsButton.setOnAction(e -> handleSendDailyNotifications());
        viewChronicAbsencesButton.setOnAction(e -> handleViewChronicAbsences());
        generateTruancyReportButton.setOnAction(e -> handleGenerateTruancyReport());
    }

    // ========================================================================
    // DATA LOADING
    // ========================================================================

    private void loadDashboardData() {
        try {
            LocalDate today = LocalDate.now();
            Long campusId = null; // All campuses

            // Load daily report
            AttendanceService.DailyAttendanceReport dailyReport =
                attendanceService.getDailyReport(campusId, today);

            if (dailyReport != null) {
                totalStudentsLabel.setText(String.valueOf(dailyReport.getTotalStudents()));
                presentCountLabel.setText(String.valueOf(dailyReport.getStudentsPresent()));
                absentCountLabel.setText(String.valueOf(dailyReport.getStudentsAbsent()));

                // Calculate tardy count from status breakdown
                int tardyCount = dailyReport.getStatusBreakdown() != null ?
                    dailyReport.getStatusBreakdown().getOrDefault(AttendanceStatus.TARDY, 0L).intValue() : 0;
                tardyCountLabel.setText(String.valueOf(tardyCount));

                double rate = dailyReport.getAttendanceRate();
                attendanceRateLabel.setText(String.format("%.1f%%", rate));
                attendanceRateProgress.setProgress(rate / 100.0);
            }

            // Load chronic absence alerts
            loadChronicAbsenceAlerts();

        } catch (Exception e) {
            log.error("Error loading dashboard data", e);
            showError("Error loading dashboard data: " + e.getMessage());
        }
    }

    private void loadChronicAbsenceAlerts() {
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(30);

            List<AttendanceService.ChronicAbsenceAlert> alerts =
                attendanceService.getChronicAbsenceAlerts(startDate, endDate, 10);

            chronicAbsenceData.clear();
            for (AttendanceService.ChronicAbsenceAlert alert : alerts) {
                // Calculate absence rate (assuming 30 days analyzed)
                double absenceRate = (double) alert.getTotalAbsences() / 30.0 * 100.0;

                chronicAbsenceData.add(new ChronicAbsenceAlertRow(
                    alert.getStudentId(),
                    alert.getStudentName(),
                    alert.getGradeLevel(),
                    alert.getTotalAbsences(),
                    absenceRate
                ));
            }

        } catch (Exception e) {
            log.error("Error loading chronic absence alerts", e);
        }
    }

    private void loadCampuses() {
        try {
            List<Campus> campuses = campusRepository.findAll();
            campusComboBox.setItems(FXCollections.observableArrayList(campuses));
            quickCampusComboBox.setItems(FXCollections.observableArrayList(campuses));

            if (!campuses.isEmpty()) {
                campusComboBox.getSelectionModel().selectFirst();
                quickCampusComboBox.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            log.error("Error loading campuses", e);
        }
    }

    private void loadCourses() {
        try {
            Campus selectedCampus = campusComboBox.getValue();
            if (selectedCampus != null) {
                List<Course> courses = courseRepository.findAll();
                courseComboBox.setItems(FXCollections.observableArrayList(courses));

                if (!courses.isEmpty()) {
                    courseComboBox.getSelectionModel().selectFirst();
                }
            }
        } catch (Exception e) {
            log.error("Error loading courses", e);
        }
    }

    // ========================================================================
    // EVENT HANDLERS
    // ========================================================================

    @FXML
    private void handleLoadRoster() {
        try {
            Course selectedCourse = courseComboBox.getValue();
            LocalDate selectedDate = attendanceDatePicker.getValue();
            Integer selectedPeriod = periodComboBox.getValue();

            if (selectedCourse == null || selectedDate == null || selectedPeriod == null) {
                showWarning("Please select course, date, and period");
                return;
            }

            // Get students enrolled in the course
            List<Student> students = selectedCourse.getStudents();

            periodAttendanceData.clear();
            for (Student student : students) {
                // Check if attendance already exists via repository
                List<AttendanceRecord> existingRecords =
                    attendanceRepository.findByStudentIdAndAttendanceDateBetween(student.getId(), selectedDate, selectedDate);

                AttendanceStatus status = AttendanceStatus.PRESENT; // Default
                String notes = "";

                // Find existing record for this period
                Optional<AttendanceRecord> existingRecord = existingRecords.stream()
                    .filter(r -> r.getPeriodNumber() != null && r.getPeriodNumber().equals(selectedPeriod))
                    .findFirst();

                if (existingRecord.isPresent()) {
                    status = existingRecord.get().getStatus();
                    notes = existingRecord.get().getNotes() != null ? existingRecord.get().getNotes() : "";
                }

                periodAttendanceData.add(new StudentAttendanceRow(
                    student.getId(),
                    student.getStudentId(),
                    student.getFullName(),
                    student.getGradeLevel(),
                    status,
                    notes
                ));
            }

            rosterCountLabel.setText(students.size() + " students loaded");

        } catch (Exception e) {
            log.error("Error loading roster", e);
            showError("Error loading roster: " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveAttendance() {
        try {
            Course selectedCourse = courseComboBox.getValue();
            LocalDate selectedDate = attendanceDatePicker.getValue();
            Integer selectedPeriod = periodComboBox.getValue();

            if (selectedCourse == null || selectedDate == null || selectedPeriod == null) {
                showWarning("Please select course, date, and period");
                return;
            }

            // Build attendance map
            Map<Long, AttendanceStatus> attendanceMap = new HashMap<>();
            for (StudentAttendanceRow row : periodAttendanceData) {
                attendanceMap.put(row.getStudentDomainId(), row.getStatus());
            }

            // Save attendance
            List<AttendanceRecord> records = attendanceService.recordClassAttendance(
                selectedCourse.getId(),
                selectedDate,
                selectedPeriod,
                attendanceMap,
                "System" // TODO: Get current user
            );

            showSuccess("Attendance saved successfully for " + records.size() + " students");

            // Refresh dashboard
            loadDashboardData();

        } catch (Exception e) {
            log.error("Error saving attendance", e);
            showError("Error saving attendance: " + e.getMessage());
        }
    }

    @FXML
    private void handleMarkAllPresent() {
        for (StudentAttendanceRow row : periodAttendanceData) {
            row.setStatus(AttendanceStatus.PRESENT);
        }
        periodAttendanceTable.refresh();
    }

    @FXML
    private void handleGenerateDailyReport() {
        try {
            Campus selectedCampus = quickCampusComboBox.getValue();
            LocalDate selectedDate = quickDatePicker.getValue();

            if (selectedCampus == null || selectedDate == null) {
                showWarning("Please select campus and date");
                return;
            }

            AttendanceService.DailyAttendanceReport report =
                attendanceService.getDailyReport(selectedCampus.getId(), selectedDate);

            StringBuilder result = new StringBuilder();
            result.append("=== DAILY ATTENDANCE REPORT ===\n");
            result.append("Campus: ").append(selectedCampus.getName()).append("\n");
            result.append("Date: ").append(selectedDate).append("\n\n");
            result.append("Total Students: ").append(report.getTotalStudents()).append("\n");
            result.append("Present: ").append(report.getStudentsPresent()).append("\n");
            result.append("Absent: ").append(report.getStudentsAbsent()).append("\n");

            int tardyCount = report.getStatusBreakdown() != null ?
                report.getStatusBreakdown().getOrDefault(AttendanceStatus.TARDY, 0L).intValue() : 0;
            result.append("Tardy: ").append(tardyCount).append("\n");
            result.append("Attendance Rate: ").append(String.format("%.1f%%", report.getAttendanceRate())).append("\n");

            quickActionsResultArea.setText(result.toString());

        } catch (Exception e) {
            log.error("Error generating daily report", e);
            showError("Error generating report: " + e.getMessage());
        }
    }

    @FXML
    private void handleSendDailyNotifications() {
        try {
            LocalDate selectedDate = quickDatePicker.getValue();

            if (selectedDate == null) {
                showWarning("Please select date");
                return;
            }

            AttendanceNotificationService.BatchNotificationResult result =
                notificationService.processDailyNotifications(selectedDate);

            StringBuilder output = new StringBuilder();
            output.append("=== DAILY NOTIFICATIONS SENT ===\n");
            output.append("Date: ").append(selectedDate).append("\n\n");
            output.append("Total Notifications: ").append(result.getTotalNotifications()).append("\n");
            output.append("Successful: ").append(result.getSuccessCount()).append("\n");
            output.append("Failed: ").append(result.getFailureCount()).append("\n");
            output.append("Skipped: ").append(result.getSkippedCount()).append("\n");

            quickActionsResultArea.setText(output.toString());
            showSuccess("Notifications processed successfully");

        } catch (Exception e) {
            log.error("Error sending notifications", e);
            showError("Error sending notifications: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewChronicAbsences() {
        mainTabPane.getSelectionModel().select(dashboardTab);
        loadChronicAbsenceAlerts();
    }

    @FXML
    private void handleGenerateTruancyReport() {
        try {
            LocalDate endDate = quickDatePicker.getValue();
            LocalDate startDate = endDate.minusDays(30);

            var truancyReport = truancyService.getActiveCases();

            StringBuilder output = new StringBuilder();
            output.append("=== TRUANCY REPORT ===\n");
            output.append("Active Cases: ").append(truancyReport.size()).append("\n\n");

            for (var truancyCase : truancyReport) {
                output.append("Case ID: ").append(truancyCase.getCaseId()).append("\n");
                output.append("Student: ").append(truancyCase.getStudentName()).append("\n");
                output.append("Absences: ").append(truancyCase.getUnexcusedAbsences()).append("\n");
                output.append("Severity: ").append(truancyCase.getSeverity()).append("\n");
                output.append("Status: ").append(truancyCase.getStatus()).append("\n\n");
            }

            quickActionsResultArea.setText(output.toString());

        } catch (Exception e) {
            log.error("Error generating truancy report", e);
            showError("Error generating truancy report: " + e.getMessage());
        }
    }

    private void handleViewStudentDetails(Long studentId) {
        // TODO: Open student details dialog or navigate to student view
        showInfo("Student details view - Student ID: " + studentId);
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

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

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
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

    // ========================================================================
    // INNER CLASSES
    // ========================================================================

    public static class ChronicAbsenceAlertRow {
        private final Long studentId;
        private final String studentName;
        private final String gradeLevel;
        private final Integer absenceCount;
        private final Double absenceRate;

        public ChronicAbsenceAlertRow(Long studentId, String studentName, String gradeLevel,
                                     Integer absenceCount, Double absenceRate) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.gradeLevel = gradeLevel;
            this.absenceCount = absenceCount;
            this.absenceRate = absenceRate;
        }

        public Long getStudentId() { return studentId; }
        public String getStudentName() { return studentName; }
        public String getGradeLevel() { return gradeLevel; }
        public Integer getAbsenceCount() { return absenceCount; }
        public Double getAbsenceRate() { return absenceRate; }
    }

    public static class StudentAttendanceRow {
        private final Long studentDomainId;
        private final String studentId;
        private final String studentName;
        private final String gradeLevel;
        private AttendanceStatus status;
        private String notes;

        public StudentAttendanceRow(Long studentDomainId, String studentId, String studentName,
                                   String gradeLevel, AttendanceStatus status, String notes) {
            this.studentDomainId = studentDomainId;
            this.studentId = studentId;
            this.studentName = studentName;
            this.gradeLevel = gradeLevel;
            this.status = status;
            this.notes = notes;
        }

        public Long getStudentDomainId() { return studentDomainId; }
        public String getStudentId() { return studentId; }
        public String getStudentName() { return studentName; }
        public String getGradeLevel() { return gradeLevel; }
        public AttendanceStatus getStatus() { return status; }
        public void setStatus(AttendanceStatus status) { this.status = status; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}
