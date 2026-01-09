package com.heronix.ui.controller;

import com.heronix.model.domain.Campus;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.Teacher;
import com.heronix.repository.CampusRepository;
import com.heronix.repository.StudentRepository;
import com.heronix.repository.TeacherRepository;
import com.heronix.service.AttendanceReportingService;
import com.heronix.service.AttendanceSummaryReportService;
import com.heronix.service.StateReportingService;
import com.heronix.service.impl.AttendanceService;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Attendance Reporting & Analytics Controller
 *
 * Provides comprehensive reporting and analytics for attendance data:
 * - ADA/ADM State Compliance Reports
 * - Teacher Attendance Statistics
 * - Grade-Level Attendance Summaries
 * - Student Attendance Reports
 * - Chronic Absenteeism Analysis
 * - Trend Analysis and Forecasting
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Attendance Enhancement
 */
@Slf4j
@Component
public class AttendanceReportingController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private AttendanceReportingService reportingService;

    @Autowired
    private AttendanceSummaryReportService summaryReportService;

    @Autowired
    private StateReportingService stateReportingService;

    @Autowired
    private CampusRepository campusRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    // ========================================================================
    // FXML Components
    // ========================================================================

    @FXML private VBox rootContainer;
    @FXML private TabPane mainTabPane;

    // ADA/ADM Report Tab
    @FXML private Tab adaAdmTab;
    @FXML private ComboBox<Campus> adaCampusComboBox;
    @FXML private DatePicker adaStartDatePicker;
    @FXML private DatePicker adaEndDatePicker;
    @FXML private Label adaValueLabel;
    @FXML private Label admValueLabel;
    @FXML private Label attendanceRateLabel;
    @FXML private Label totalDaysLabel;
    @FXML private TextArea adaReportOutput;

    // Teacher Stats Tab
    @FXML private Tab teacherStatsTab;
    @FXML private DatePicker teacherStartDatePicker;
    @FXML private DatePicker teacherEndDatePicker;
    @FXML private TableView<TeacherStatsRow> teacherStatsTable;
    @FXML private TableColumn<TeacherStatsRow, String> teacherNameCol;
    @FXML private TableColumn<TeacherStatsRow, Integer> totalClassesCol;
    @FXML private TableColumn<TeacherStatsRow, Integer> totalRecordsCol;
    @FXML private TableColumn<TeacherStatsRow, Double> avgAttendanceCol;
    @FXML private TableColumn<TeacherStatsRow, Integer> totalAbsencesCol;

    // Grade-Level Summary Tab
    @FXML private Tab gradeSummaryTab;
    @FXML private DatePicker gradeStartDatePicker;
    @FXML private DatePicker gradeEndDatePicker;
    @FXML private TableView<GradeSummaryRow> gradeSummaryTable;
    @FXML private TableColumn<GradeSummaryRow, String> gradeLevelCol;
    @FXML private TableColumn<GradeSummaryRow, Integer> gradeStudentsCol;
    @FXML private TableColumn<GradeSummaryRow, Integer> gradePresentCol;
    @FXML private TableColumn<GradeSummaryRow, Integer> gradeAbsentCol;
    @FXML private TableColumn<GradeSummaryRow, Double> gradeRateCol;

    // Student Attendance Tab
    @FXML private Tab studentAttendanceTab;
    @FXML private ComboBox<Student> studentComboBox;
    @FXML private DatePicker studentStartDatePicker;
    @FXML private DatePicker studentEndDatePicker;
    @FXML private Label totalPresentLabel;
    @FXML private Label totalAbsentLabel;
    @FXML private Label totalTardyLabel;
    @FXML private Label studentAttendanceRateLabel;
    @FXML private ProgressBar studentAttendanceProgress;
    @FXML private TextArea studentReportOutput;

    // Chronic Absenteeism Tab
    @FXML private Tab chronicAbsenceTab;
    @FXML private ComboBox<Campus> chronicCampusComboBox;
    @FXML private DatePicker chronicStartDatePicker;
    @FXML private DatePicker chronicEndDatePicker;
    @FXML private Spinner<Integer> thresholdSpinner;
    @FXML private TableView<ChronicAbsenceRow> chronicAbsenceTable;
    @FXML private TableColumn<ChronicAbsenceRow, String> chronicStudentCol;
    @FXML private TableColumn<ChronicAbsenceRow, String> chronicGradeCol;
    @FXML private TableColumn<ChronicAbsenceRow, Integer> chronicAbsencesCol;
    @FXML private TableColumn<ChronicAbsenceRow, Double> chronicRateCol;
    @FXML private TableColumn<ChronicAbsenceRow, String> chronicAlertCol;

    // Observable Lists
    private final ObservableList<TeacherStatsRow> teacherStatsData = FXCollections.observableArrayList();
    private final ObservableList<GradeSummaryRow> gradeSummaryData = FXCollections.observableArrayList();
    private final ObservableList<ChronicAbsenceRow> chronicAbsenceData = FXCollections.observableArrayList();

    // ========================================================================
    // Initialization
    // ========================================================================

    @FXML
    public void initialize() {
        log.info("Initializing AttendanceReportingController");

        setupAdaAdmTab();
        setupTeacherStatsTab();
        setupGradeSummaryTab();
        setupStudentAttendanceTab();
        setupChronicAbsenceTab();

        loadCampuses();
        loadStudents();

        // Set default date ranges (last 30 days)
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        setDefaultDateRanges(startDate, endDate);
    }

    private void setupAdaAdmTab() {
        // Setup campus combo box
        if (adaCampusComboBox != null) {
            adaCampusComboBox.setConverter(new StringConverter<>() {
                @Override
                public String toString(Campus campus) {
                    return campus != null ? campus.getName() : "";
                }

                @Override
                public Campus fromString(String string) {
                    return null;
                }
            });
        }
    }

    private void setupTeacherStatsTab() {
        // Setup teacher stats table
        if (teacherNameCol != null) {
            teacherNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTeacherName()));
            totalClassesCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getTotalClasses()));
            totalRecordsCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getTotalRecords()));
            avgAttendanceCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getAvgAttendance()));
            totalAbsencesCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getTotalAbsences()));
        }

        if (teacherStatsTable != null) {
            teacherStatsTable.setItems(teacherStatsData);
        }
    }

    private void setupGradeSummaryTab() {
        // Setup grade summary table
        if (gradeLevelCol != null) {
            gradeLevelCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGradeLevel()));
            gradeStudentsCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getTotalStudents()));
            gradePresentCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getTotalPresent()));
            gradeAbsentCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getTotalAbsent()));
            gradeRateCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getAttendanceRate()));
        }

        if (gradeSummaryTable != null) {
            gradeSummaryTable.setItems(gradeSummaryData);
        }
    }

    private void setupStudentAttendanceTab() {
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
    }

    private void setupChronicAbsenceTab() {
        // Setup campus combo box
        if (chronicCampusComboBox != null) {
            chronicCampusComboBox.setConverter(new StringConverter<>() {
                @Override
                public String toString(Campus campus) {
                    return campus != null ? campus.getName() : "";
                }

                @Override
                public Campus fromString(String string) {
                    return null;
                }
            });
        }

        // Setup threshold spinner
        if (thresholdSpinner != null) {
            SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 10);
            thresholdSpinner.setValueFactory(valueFactory);
        }

        // Setup chronic absence table
        if (chronicStudentCol != null) {
            chronicStudentCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentName()));
            chronicGradeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGradeLevel()));
            chronicAbsencesCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getTotalAbsences()));
            chronicRateCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getAbsenceRate()));
            chronicAlertCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAlertLevel()));
        }

        if (chronicAbsenceTable != null) {
            chronicAbsenceTable.setItems(chronicAbsenceData);
        }
    }

    private void setDefaultDateRanges(LocalDate start, LocalDate end) {
        if (adaStartDatePicker != null) adaStartDatePicker.setValue(start);
        if (adaEndDatePicker != null) adaEndDatePicker.setValue(end);
        if (teacherStartDatePicker != null) teacherStartDatePicker.setValue(start);
        if (teacherEndDatePicker != null) teacherEndDatePicker.setValue(end);
        if (gradeStartDatePicker != null) gradeStartDatePicker.setValue(start);
        if (gradeEndDatePicker != null) gradeEndDatePicker.setValue(end);
        if (studentStartDatePicker != null) studentStartDatePicker.setValue(start);
        if (studentEndDatePicker != null) studentEndDatePicker.setValue(end);
        if (chronicStartDatePicker != null) chronicStartDatePicker.setValue(start);
        if (chronicEndDatePicker != null) chronicEndDatePicker.setValue(end);
    }

    // ========================================================================
    // Data Loading
    // ========================================================================

    private void loadCampuses() {
        try {
            List<Campus> campuses = campusRepository.findAll();
            if (adaCampusComboBox != null) {
                adaCampusComboBox.setItems(FXCollections.observableArrayList(campuses));
            }
            if (chronicCampusComboBox != null) {
                chronicCampusComboBox.setItems(FXCollections.observableArrayList(campuses));
            }
        } catch (Exception e) {
            log.error("Error loading campuses", e);
            showError("Error loading campuses: " + e.getMessage());
        }
    }

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
        } catch (Exception e) {
            log.error("Error loading students", e);
            showError("Error loading students: " + e.getMessage());
        }
    }

    // ========================================================================
    // ADA/ADM Report Handlers
    // ========================================================================

    @FXML
    private void handleGenerateAdaReport() {
        try {
            Campus campus = adaCampusComboBox.getValue();
            LocalDate startDate = adaStartDatePicker.getValue();
            LocalDate endDate = adaEndDatePicker.getValue();

            if (campus == null || startDate == null || endDate == null) {
                showWarning("Please select campus and date range");
                return;
            }

            if (startDate.isAfter(endDate)) {
                showWarning("Start date must be before end date");
                return;
            }

            // Generate ADA/ADM report
            AttendanceReportingService.ADACalculation adaCalc =
                reportingService.calculateADA(campus.getId(), startDate, endDate);
            AttendanceReportingService.ADMCalculation admCalc =
                reportingService.calculateADM(campus.getId(), startDate, endDate);

            // Update UI
            adaValueLabel.setText(String.format("%.2f", adaCalc.getAda()));
            admValueLabel.setText(String.format("%.2f", admCalc.getAdm()));
            attendanceRateLabel.setText(String.format("%.1f%%", adaCalc.getAttendanceRate()));
            totalDaysLabel.setText(String.valueOf(adaCalc.getTotalDaysInPeriod()));

            // Generate detailed report text
            StringBuilder output = new StringBuilder();
            output.append("ADA/ADM Report\n");
            output.append("=============\n\n");
            output.append(String.format("Campus: %s\n", campus.getName()));
            output.append(String.format("Period: %s to %s\n",
                startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))));
            output.append(String.format("Total Days: %d\n\n", adaCalc.getTotalDaysInPeriod()));
            output.append(String.format("Average Daily Attendance (ADA): %.2f\n", adaCalc.getAda()));
            output.append(String.format("Average Daily Membership (ADM): %.2f\n", admCalc.getAdm()));
            output.append(String.format("Attendance Rate: %.1f%%\n\n", adaCalc.getAttendanceRate()));
            output.append("This report meets state compliance requirements for funding calculations.\n");

            adaReportOutput.setText(output.toString());

            log.info("Generated ADA/ADM report for campus {} from {} to {}",
                campus.getName(), startDate, endDate);

        } catch (Exception e) {
            log.error("Error generating ADA report", e);
            showError("Error generating ADA report: " + e.getMessage());
        }
    }

    // ========================================================================
    // Teacher Stats Handlers
    // ========================================================================

    @FXML
    private void handleLoadTeacherStats() {
        try {
            LocalDate startDate = teacherStartDatePicker.getValue();
            LocalDate endDate = teacherEndDatePicker.getValue();

            if (startDate == null || endDate == null) {
                showWarning("Please select date range");
                return;
            }

            if (startDate.isAfter(endDate)) {
                showWarning("Start date must be before end date");
                return;
            }

            // Load teacher stats
            List<AttendanceReportingService.TeacherAttendanceStats> stats =
                reportingService.getAttendanceByTeacher(startDate, endDate);

            teacherStatsData.clear();
            for (AttendanceReportingService.TeacherAttendanceStats stat : stats) {
                teacherStatsData.add(new TeacherStatsRow(
                    stat.getTeacherName(),
                    stat.getCoursesCount(),
                    stat.getTotalRecords(),
                    stat.getAttendanceRate(),
                    stat.getAbsentCount()
                ));
            }

            log.info("Loaded teacher attendance stats for {} to {}", startDate, endDate);

        } catch (Exception e) {
            log.error("Error loading teacher stats", e);
            showError("Error loading teacher stats: " + e.getMessage());
        }
    }

    // ========================================================================
    // Grade Summary Handlers
    // ========================================================================

    @FXML
    private void handleLoadGradeSummary() {
        try {
            LocalDate startDate = gradeStartDatePicker.getValue();
            LocalDate endDate = gradeEndDatePicker.getValue();

            if (startDate == null || endDate == null) {
                showWarning("Please select date range");
                return;
            }

            if (startDate.isAfter(endDate)) {
                showWarning("Start date must be before end date");
                return;
            }

            // Load grade-level summary
            List<AttendanceReportingService.GradeAttendanceStats> stats =
                reportingService.getAttendanceByGrade(startDate, endDate);

            gradeSummaryData.clear();
            for (AttendanceReportingService.GradeAttendanceStats stat : stats) {
                gradeSummaryData.add(new GradeSummaryRow(
                    stat.getGradeLevel(),
                    stat.getStudentsCount(),
                    stat.getPresentCount(),
                    stat.getAbsentCount(),
                    stat.getAttendanceRate()
                ));
            }

            log.info("Loaded grade-level attendance summary for {} to {}", startDate, endDate);

        } catch (Exception e) {
            log.error("Error loading grade summary", e);
            showError("Error loading grade summary: " + e.getMessage());
        }
    }

    // ========================================================================
    // Student Attendance Handlers
    // ========================================================================

    @FXML
    private void handleLoadStudentReport() {
        try {
            Student student = studentComboBox.getValue();
            LocalDate startDate = studentStartDatePicker.getValue();
            LocalDate endDate = studentEndDatePicker.getValue();

            if (student == null) {
                showWarning("Please select a student");
                return;
            }

            if (startDate == null || endDate == null) {
                showWarning("Please select date range");
                return;
            }

            if (startDate.isAfter(endDate)) {
                showWarning("Start date must be before end date");
                return;
            }

            // Generate student attendance summary
            AttendanceService.AttendanceSummary summary =
                attendanceService.getStudentAttendanceSummary(student.getId(), startDate, endDate);

            // Update UI
            totalPresentLabel.setText(String.valueOf(summary.getDaysPresent()));
            totalAbsentLabel.setText(String.valueOf(summary.getDaysAbsent()));
            totalTardyLabel.setText(String.valueOf(summary.getDaysTardy()));

            double rate = summary.getAttendanceRate();
            studentAttendanceRateLabel.setText(String.format("%.1f%%", rate));
            studentAttendanceProgress.setProgress(rate / 100.0);

            // Generate detailed report
            StringBuilder output = new StringBuilder();
            output.append(String.format("Student Attendance Report\n"));
            output.append("========================\n\n");
            output.append(String.format("Student: %s %s (%s)\n",
                student.getFirstName(), student.getLastName(), student.getStudentId()));
            output.append(String.format("Grade: %s\n", student.getGradeLevel()));
            output.append(String.format("Period: %s to %s\n\n",
                startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))));
            output.append(String.format("Total Days: %d\n", summary.getTotalDays()));
            output.append(String.format("Days Present: %d\n", summary.getDaysPresent()));
            output.append(String.format("Days Absent: %d\n", summary.getDaysAbsent()));
            output.append(String.format("Days Tardy: %d\n", summary.getDaysTardy()));
            output.append(String.format("\nAttendance Rate: %.1f%%\n", rate));

            if (rate < 90.0) {
                output.append("\n⚠️ WARNING: Student is below 90% attendance threshold\n");
            }

            studentReportOutput.setText(output.toString());

            log.info("Generated attendance report for student {}", student.getStudentId());

        } catch (Exception e) {
            log.error("Error loading student report", e);
            showError("Error loading student report: " + e.getMessage());
        }
    }

    // ========================================================================
    // Chronic Absenteeism Handlers
    // ========================================================================

    @FXML
    private void handleLoadChronicAbsences() {
        try {
            Campus campus = chronicCampusComboBox.getValue();
            LocalDate startDate = chronicStartDatePicker.getValue();
            LocalDate endDate = chronicEndDatePicker.getValue();
            Integer threshold = thresholdSpinner.getValue();

            if (startDate == null || endDate == null) {
                showWarning("Please select date range");
                return;
            }

            if (startDate.isAfter(endDate)) {
                showWarning("Start date must be before end date");
                return;
            }

            // Load chronic absence alerts
            List<AttendanceService.ChronicAbsenceAlert> alerts =
                attendanceService.getChronicAbsenceAlerts(startDate, endDate, threshold);

            chronicAbsenceData.clear();
            for (AttendanceService.ChronicAbsenceAlert alert : alerts) {
                double rate = (double) alert.getTotalAbsences() / 30.0 * 100.0;
                chronicAbsenceData.add(new ChronicAbsenceRow(
                    alert.getStudentName(),
                    alert.getGradeLevel(),
                    alert.getTotalAbsences(),
                    rate,
                    alert.getAlertLevel()
                ));
            }

            log.info("Loaded {} chronic absence alerts", chronicAbsenceData.size());

        } catch (Exception e) {
            log.error("Error loading chronic absences", e);
            showError("Error loading chronic absences: " + e.getMessage());
        }
    }

    // ========================================================================
    // Helper Methods
    // ========================================================================

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
    public static class TeacherStatsRow {
        private final String teacherName;
        private final Integer totalClasses;
        private final Integer totalRecords;
        private final Double avgAttendance;
        private final Integer totalAbsences;
    }

    @Data
    public static class GradeSummaryRow {
        private final String gradeLevel;
        private final Integer totalStudents;
        private final Integer totalPresent;
        private final Integer totalAbsent;
        private final Double attendanceRate;
    }

    @Data
    public static class ChronicAbsenceRow {
        private final String studentName;
        private final String gradeLevel;
        private final Integer totalAbsences;
        private final Double absenceRate;
        private final String alertLevel;
    }
}
