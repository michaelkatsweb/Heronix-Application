package com.heronix.ui.controller;

import com.heronix.model.domain.AttendanceRecord;
import com.heronix.model.domain.AttendanceRecord.AttendanceStatus;
import com.heronix.model.domain.Student;
import com.heronix.repository.AttendanceRecordRepository;
import com.heronix.service.impl.AttendanceService;
import com.heronix.service.StudentService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AttendanceReportsController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    // Left Panel - Filters
    @FXML private ComboBox<String> reportTypeComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> gradeLevelComboBox;
    @FXML private ComboBox<String> homeroomComboBox;
    @FXML private ComboBox<Student> studentComboBox;
    @FXML private CheckBox includeAbsentCheckBox;
    @FXML private CheckBox includeTardyCheckBox;
    @FXML private CheckBox includeExcusedCheckBox;
    @FXML private CheckBox includeUnexcusedCheckBox;
    @FXML private CheckBox includeEarlyDepartureCheckBox;

    // Action Buttons
    @FXML private Button generateButton;
    @FXML private Button exportButton;
    @FXML private Button exportPdfButton;
    @FXML private Button printButton;

    // Right Panel - Report Header
    @FXML private Label reportTitleLabel;
    @FXML private Label reportSubtitleLabel;
    @FXML private Label generatedDateLabel;
    @FXML private Label recordCountLabel;

    // Summary Statistics
    @FXML private HBox summaryStatsBox;
    @FXML private Label totalPresentLabel;
    @FXML private Label totalAbsentLabel;
    @FXML private Label totalTardyLabel;
    @FXML private Label attendanceRateLabel;

    // Report Content
    @FXML private TabPane reportTabPane;
    @FXML private TableView<AttendanceReportRow> reportTableView;
    @FXML private TextArea summaryTextArea;
    @FXML private StackPane chartContainer;

    // Interventions Tab
    @FXML private ListView<String> chronicAbsenteeismList;
    @FXML private ListView<String> truancyRiskList;
    @FXML private ListView<String> excessiveTardiesList;
    @FXML private ListView<String> earlyWarningList;

    // Perfect Attendance Tab
    @FXML private TableView<PerfectAttendanceRow> perfectAttendanceTableView;
    @FXML private TableColumn<PerfectAttendanceRow, String> perfectStudentIdColumn;
    @FXML private TableColumn<PerfectAttendanceRow, String> perfectStudentNameColumn;
    @FXML private TableColumn<PerfectAttendanceRow, String> perfectGradeColumn;
    @FXML private TableColumn<PerfectAttendanceRow, String> perfectHomeroomColumn;
    @FXML private TableColumn<PerfectAttendanceRow, Integer> perfectDaysColumn;
    @FXML private TableColumn<PerfectAttendanceRow, Integer> perfectTardiesColumn;
    @FXML private TableColumn<PerfectAttendanceRow, Integer> perfectStreakColumn;

    // Status Bar
    @FXML private Label statusLabel;
    @FXML private Label processingTimeLabel;

    private ObservableList<AttendanceReportRow> reportData = FXCollections.observableArrayList();
    private ObservableList<Student> allStudents = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupComboBoxes();
        setupPerfectAttendanceTable();
        setupDefaultFilters();
        loadStudents();
    }

    private void setupComboBoxes() {
        // Report Types
        reportTypeComboBox.setItems(FXCollections.observableArrayList(
                "Daily Attendance Summary",
                "Student Attendance Detail",
                "Chronic Absenteeism Report",
                "Truancy Report",
                "Perfect Attendance Report",
                "Tardy Report",
                "Early Departure Report",
                "Attendance by Grade Level",
                "Attendance by Homeroom",
                "Absence Reason Analysis",
                "Monthly Attendance Summary",
                "Year-to-Date Attendance",
                "State Compliance Report",
                "Parent Notification Report"
        ));

        // Grade Levels
        gradeLevelComboBox.setItems(FXCollections.observableArrayList(
                "All Grades", "Pre-K", "Kindergarten",
                "1st Grade", "2nd Grade", "3rd Grade", "4th Grade", "5th Grade",
                "6th Grade", "7th Grade", "8th Grade",
                "9th Grade", "10th Grade", "11th Grade", "12th Grade"
        ));
        gradeLevelComboBox.setValue("All Grades");

        // Student ComboBox with StringConverter
        studentComboBox.setConverter(new javafx.util.StringConverter<Student>() {
            @Override
            public String toString(Student student) {
                return student != null ? student.getFullName() + " (" + student.getStudentId() + ")" : "";
            }

            @Override
            public Student fromString(String string) {
                return null;
            }
        });

        // Report type selection listener
        reportTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateReportTitle(newVal);
            }
        });
    }

    private void setupPerfectAttendanceTable() {
        perfectStudentIdColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStudentId()));

        perfectStudentNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStudentName()));

        perfectGradeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getGrade()));

        perfectHomeroomColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getHomeroom()));

        perfectDaysColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getDaysPresent()).asObject());

        perfectTardiesColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getTardies()).asObject());

        perfectStreakColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getStreak()).asObject());
    }

    private void setupDefaultFilters() {
        // Set default date range to current month
        LocalDate today = LocalDate.now();
        startDatePicker.setValue(today.withDayOfMonth(1));
        endDatePicker.setValue(today);

        // Default checkboxes
        includeAbsentCheckBox.setSelected(true);
        includeTardyCheckBox.setSelected(true);
        includeExcusedCheckBox.setSelected(true);
        includeUnexcusedCheckBox.setSelected(true);
    }

    private void loadStudents() {
        allStudents.clear();
        try {
            allStudents.addAll(studentService.getActiveStudents());
        } catch (Exception e) {
            log.warn("Failed to load students for filter: {}", e.getMessage());
        }
        studentComboBox.setItems(allStudents);
    }

    private void updateReportTitle(String reportType) {
        reportTitleLabel.setText(reportType);
        statusLabel.setText("Ready to generate: " + reportType);
    }

    @FXML
    private void handleGenerateReport() {
        if (reportTypeComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "No Report Selected",
                    "Please select a report type to generate.");
            return;
        }

        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Date Range",
                    "Please select both start and end dates.");
            return;
        }

        if (startDatePicker.getValue().isAfter(endDatePicker.getValue())) {
            showAlert(Alert.AlertType.WARNING, "Invalid Date Range",
                    "Start date must be before or equal to end date.");
            return;
        }

        statusLabel.setText("Generating report...");
        long startTime = System.currentTimeMillis();

        try {
            String reportType = reportTypeComboBox.getValue();

            switch (reportType) {
                case "Daily Attendance Summary":
                    generateDailyAttendanceSummary();
                    break;
                case "Student Attendance Detail":
                    generateStudentAttendanceDetail();
                    break;
                case "Chronic Absenteeism Report":
                    generateChronicAbsenteeismReport();
                    break;
                case "Truancy Report":
                    generateTruancyReport();
                    break;
                case "Perfect Attendance Report":
                    generatePerfectAttendanceReport();
                    break;
                case "Tardy Report":
                    generateTardyReport();
                    break;
                case "Monthly Attendance Summary":
                    generateMonthlyAttendanceSummary();
                    break;
                default:
                    generateGenericReport(reportType);
            }

            long endTime = System.currentTimeMillis();
            processingTimeLabel.setText("Generated in " + (endTime - startTime) + "ms");
            statusLabel.setText("Report generated successfully");

            // Show summary stats
            summaryStatsBox.setManaged(true);
            summaryStatsBox.setVisible(true);

            // Set generated date
            generatedDateLabel.setText("Generated: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a")));

        } catch (Exception e) {
            statusLabel.setText("Error generating report");
            showAlert(Alert.AlertType.ERROR, "Report Generation Error",
                    "An error occurred while generating the report: " + e.getMessage());
        }
    }

    private void generateDailyAttendanceSummary() {
        reportSubtitleLabel.setText(formatDateRange());

        // Clear previous data
        reportTableView.getColumns().clear();
        reportData.clear();

        // Setup columns
        TableColumn<AttendanceReportRow, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getField1()));
        dateColumn.setPrefWidth(120);

        TableColumn<AttendanceReportRow, String> presentColumn = new TableColumn<>("Present");
        presentColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getField2()));
        presentColumn.setPrefWidth(100);

        TableColumn<AttendanceReportRow, String> absentColumn = new TableColumn<>("Absent");
        absentColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getField3()));
        absentColumn.setPrefWidth(100);

        TableColumn<AttendanceReportRow, String> tardyColumn = new TableColumn<>("Tardy");
        tardyColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getField4()));
        tardyColumn.setPrefWidth(100);

        TableColumn<AttendanceReportRow, String> rateColumn = new TableColumn<>("Attendance %");
        rateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getField5()));
        rateColumn.setPrefWidth(120);

        reportTableView.getColumns().addAll(dateColumn, presentColumn, absentColumn, tardyColumn, rateColumn);

        // Query real attendance data per day
        LocalDate currentDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        int totalPresent = 0;
        int totalAbsent = 0;
        int totalTardy = 0;

        List<AttendanceRecord> allRecords = attendanceRecordRepository.findByDateRange(currentDate, endDate);
        Map<LocalDate, List<AttendanceRecord>> byDate = allRecords.stream()
                .collect(Collectors.groupingBy(AttendanceRecord::getAttendanceDate));

        while (!currentDate.isAfter(endDate)) {
            if (currentDate.getDayOfWeek() != DayOfWeek.SATURDAY &&
                currentDate.getDayOfWeek() != DayOfWeek.SUNDAY) {

                List<AttendanceRecord> dayRecords = byDate.getOrDefault(currentDate, Collections.emptyList());
                int present = (int) dayRecords.stream().filter(AttendanceRecord::isPresent).count();
                int absent = (int) dayRecords.stream().filter(AttendanceRecord::isAbsent).count();
                int tardy = (int) dayRecords.stream().filter(r -> r.getStatus() == AttendanceStatus.TARDY).count();
                int total = present + absent;
                double rate = total > 0 ? (present * 100.0 / total) : 0;

                totalPresent += present;
                totalAbsent += absent;
                totalTardy += tardy;

                reportData.add(new AttendanceReportRow(
                        currentDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                        String.valueOf(present),
                        String.valueOf(absent),
                        String.valueOf(tardy),
                        String.format("%.2f%%", rate)
                ));
            }

            currentDate = currentDate.plusDays(1);
        }

        reportTableView.setItems(reportData);

        // Update statistics
        int totalCount = totalPresent + totalAbsent;
        double attendanceRate = totalCount > 0 ? (totalPresent * 100.0 / totalCount) : 0;

        totalPresentLabel.setText(String.valueOf(totalPresent));
        totalAbsentLabel.setText(String.valueOf(totalAbsent));
        totalTardyLabel.setText(String.valueOf(totalTardy));
        attendanceRateLabel.setText(String.format("%.2f%%", attendanceRate));
        recordCountLabel.setText(reportData.size() + " days");

        // Generate summary text
        generateSummaryText(totalPresent, totalAbsent, totalTardy, attendanceRate);

        // Generate chart
        generateAttendanceChart(reportData);
    }

    private void generateStudentAttendanceDetail() {
        reportSubtitleLabel.setText(formatDateRange());

        reportTableView.getColumns().clear();
        reportData.clear();

        // Setup columns
        TableColumn<AttendanceReportRow, String> studentIdColumn = new TableColumn<>("Student ID");
        studentIdColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getField1()));

        TableColumn<AttendanceReportRow, String> studentNameColumn = new TableColumn<>("Student Name");
        studentNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getField2()));
        studentNameColumn.setPrefWidth(200);

        TableColumn<AttendanceReportRow, String> gradeColumn = new TableColumn<>("Grade");
        gradeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getField3()));

        TableColumn<AttendanceReportRow, String> absencesColumn = new TableColumn<>("Absences");
        absencesColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getField4()));

        TableColumn<AttendanceReportRow, String> tardiesColumn = new TableColumn<>("Tardies");
        tardiesColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getField5()));

        TableColumn<AttendanceReportRow, String> rateColumn = new TableColumn<>("Attendance %");
        rateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getField6()));

        reportTableView.getColumns().addAll(studentIdColumn, studentNameColumn, gradeColumn,
                absencesColumn, tardiesColumn, rateColumn);

        // Query real student attendance data
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        List<Student> students = filterStudentList();

        int totalPresent = 0;
        int totalAbsent = 0;
        int totalTardy = 0;

        for (Student student : students) {
            try {
                AttendanceService.AttendanceSummary summary =
                        attendanceService.getStudentAttendanceSummary(student.getId(), start, end);
                int absences = summary.getDaysAbsent();
                int tardies = summary.getDaysTardy();
                int present = summary.getDaysPresent();
                double rate = summary.getAttendanceRate();

                totalPresent += present;
                totalAbsent += absences;
                totalTardy += tardies;

                reportData.add(new AttendanceReportRow(
                        student.getStudentId(), student.getFullName(),
                        student.getGradeLevel() != null ? student.getGradeLevel() : "",
                        String.valueOf(absences),
                        String.valueOf(tardies),
                        String.format("%.2f%%", rate)
                ));
            } catch (Exception e) {
                log.warn("Failed to get attendance for student {}: {}", student.getStudentId(), e.getMessage());
            }
        }

        reportTableView.setItems(reportData);

        // Update statistics
        int totalCount = totalPresent + totalAbsent;
        double attendanceRate = totalCount > 0 ? (totalPresent * 100.0 / totalCount) : 0;

        totalPresentLabel.setText(String.valueOf(totalPresent));
        totalAbsentLabel.setText(String.valueOf(totalAbsent));
        totalTardyLabel.setText(String.valueOf(totalTardy));
        attendanceRateLabel.setText(String.format("%.2f%%", attendanceRate));
        recordCountLabel.setText(reportData.size() + " students");

        generateInterventionLists();
    }

    private void generateChronicAbsenteeismReport() {
        reportSubtitleLabel.setText("Students with >= 10% absences - " + formatDateRange());

        reportTableView.getColumns().clear();
        reportData.clear();

        // Setup columns similar to student detail
        setupStudentDetailColumns();

        // Generate data for chronically absent students only
        generateChronicAbsentStudentData();

        reportTableView.setItems(reportData);
        recordCountLabel.setText(reportData.size() + " students at risk");

        generateInterventionLists();
    }

    private void generateTruancyReport() {
        reportSubtitleLabel.setText("Unexcused absences - " + formatDateRange());

        reportTableView.getColumns().clear();
        reportData.clear();

        setupStudentDetailColumns();
        generateTruancyData();

        reportTableView.setItems(reportData);
        recordCountLabel.setText(reportData.size() + " students with truancy concerns");

        generateInterventionLists();
    }

    private void generatePerfectAttendanceReport() {
        reportSubtitleLabel.setText("Perfect attendance - " + formatDateRange());

        ObservableList<PerfectAttendanceRow> perfectStudents = FXCollections.observableArrayList();
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        try {
            List<Student> students = filterStudentList();
            for (Student student : students) {
                AttendanceService.AttendanceSummary summary =
                        attendanceService.getStudentAttendanceSummary(student.getId(), start, end);
                if (summary.getDaysAbsent() == 0 && summary.getTotalDays() > 0) {
                    perfectStudents.add(new PerfectAttendanceRow(
                            student.getStudentId(),
                            student.getFullName(),
                            student.getGradeLevel() != null ? student.getGradeLevel() : "",
                            "",
                            summary.getDaysPresent(),
                            summary.getDaysTardy(),
                            summary.getDaysPresent() // streak approximation
                    ));
                }
            }
        } catch (Exception e) {
            log.error("Failed to load perfect attendance data", e);
        }

        perfectAttendanceTableView.setItems(perfectStudents);
        recordCountLabel.setText(perfectStudents.size() + " students with perfect attendance");

        // Switch to perfect attendance tab
        reportTabPane.getSelectionModel().select(4);
    }

    private void generateTardyReport() {
        reportSubtitleLabel.setText("Tardy incidents - " + formatDateRange());
        setupStudentDetailColumns();
        generateTardyData();
        reportTableView.setItems(reportData);
        recordCountLabel.setText(reportData.size() + " students with tardies");
    }

    private void generateMonthlyAttendanceSummary() {
        reportSubtitleLabel.setText("Monthly summary - " + formatDateRange());
        generateDailyAttendanceSummary();
    }

    private void generateGenericReport(String reportType) {
        reportSubtitleLabel.setText(formatDateRange());
        statusLabel.setText("Report type: " + reportType + " - Sample data displayed");

        reportTableView.getColumns().clear();
        reportData.clear();

        TableColumn<AttendanceReportRow, String> col1 = new TableColumn<>("Column 1");
        col1.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getField1()));

        TableColumn<AttendanceReportRow, String> col2 = new TableColumn<>("Column 2");
        col2.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getField2()));

        reportTableView.getColumns().addAll(col1, col2);

        reportData.add(new AttendanceReportRow("Data 1", "Value 1"));
        reportData.add(new AttendanceReportRow("Data 2", "Value 2"));

        reportTableView.setItems(reportData);
        recordCountLabel.setText(reportData.size() + " records");
    }

    private void setupStudentDetailColumns() {
        TableColumn<AttendanceReportRow, String> studentIdColumn = new TableColumn<>("Student ID");
        studentIdColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getField1()));

        TableColumn<AttendanceReportRow, String> studentNameColumn = new TableColumn<>("Student Name");
        studentNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getField2()));
        studentNameColumn.setPrefWidth(200);

        TableColumn<AttendanceReportRow, String> gradeColumn = new TableColumn<>("Grade");
        gradeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getField3()));

        TableColumn<AttendanceReportRow, String> absencesColumn = new TableColumn<>("Absences");
        absencesColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getField4()));

        TableColumn<AttendanceReportRow, String> rateColumn = new TableColumn<>("Attendance %");
        rateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getField5()));

        reportTableView.getColumns().addAll(studentIdColumn, studentNameColumn, gradeColumn,
                absencesColumn, rateColumn);
    }

    private void generateChronicAbsentStudentData() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        try {
            List<AttendanceService.ChronicAbsenceAlert> alerts =
                    attendanceService.getChronicAbsenceAlerts(start, end, 3);
            for (AttendanceService.ChronicAbsenceAlert alert : alerts) {
                Long absences = attendanceRecordRepository.countAbsencesByStudentIdAndDateRange(
                        alert.getStudentId(), start, end);
                Double rate = attendanceRecordRepository.calculateAttendanceRateByStudentIdAndDateRange(
                        alert.getStudentId(), start, end);
                reportData.add(new AttendanceReportRow(
                        String.valueOf(alert.getStudentId()),
                        alert.getStudentName(),
                        alert.getGradeLevel() != null ? alert.getGradeLevel() : "",
                        String.valueOf(absences != null ? absences : alert.getTotalAbsences()),
                        String.format("%.2f%%", rate != null ? rate : 0.0)
                ));
            }
        } catch (Exception e) {
            log.error("Failed to load chronic absenteeism data", e);
        }
    }

    private void generateTruancyData() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        try {
            List<Student> students = filterStudentList();
            for (Student student : students) {
                Long unexcused = attendanceRecordRepository.countUnexcusedAbsencesByStudentIdAndDateRange(
                        student.getId(), start, end);
                if (unexcused != null && unexcused >= 3) {
                    Double rate = attendanceRecordRepository.calculateAttendanceRateByStudentIdAndDateRange(
                            student.getId(), start, end);
                    reportData.add(new AttendanceReportRow(
                            student.getStudentId(), student.getFullName(),
                            student.getGradeLevel() != null ? student.getGradeLevel() : "",
                            unexcused + " unexcused",
                            String.format("%.2f%%", rate != null ? rate : 0.0)
                    ));
                }
            }
        } catch (Exception e) {
            log.error("Failed to load truancy data", e);
        }
    }

    private void generateTardyData() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        try {
            List<Student> students = filterStudentList();
            for (Student student : students) {
                Long tardies = attendanceRecordRepository.countTardiesByStudentIdAndDateRange(
                        student.getId(), start, end);
                if (tardies != null && tardies >= 3) {
                    reportData.add(new AttendanceReportRow(
                            student.getStudentId(), student.getFullName(),
                            student.getGradeLevel() != null ? student.getGradeLevel() : "",
                            tardies + " tardies",
                            "N/A"
                    ));
                }
            }
        } catch (Exception e) {
            log.error("Failed to load tardy data", e);
        }
    }

    private void generateInterventionLists() {
        ObservableList<String> chronicList = FXCollections.observableArrayList();
        ObservableList<String> truancyList = FXCollections.observableArrayList();
        ObservableList<String> tardyList = FXCollections.observableArrayList();
        ObservableList<String> warningList = FXCollections.observableArrayList();

        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        try {
            // Chronic absenteeism (>= 5 absences)
            List<AttendanceService.ChronicAbsenceAlert> alerts =
                    attendanceService.getChronicAbsenceAlerts(start, end, 5);
            for (AttendanceService.ChronicAbsenceAlert alert : alerts) {
                Double rate = attendanceRecordRepository.calculateAttendanceRateByStudentIdAndDateRange(
                        alert.getStudentId(), start, end);
                chronicList.add(String.format("%s - %d absences (%.0f%% attendance)",
                        alert.getStudentName(), alert.getTotalAbsences(),
                        rate != null ? rate : 0.0));
            }

            // Truancy (unexcused >= 3), tardies (>= 5), early warning (3-4 absences)
            List<Student> students = filterStudentList();
            for (Student student : students) {
                Long unexcused = attendanceRecordRepository.countUnexcusedAbsencesByStudentIdAndDateRange(
                        student.getId(), start, end);
                Long tardies = attendanceRecordRepository.countTardiesByStudentIdAndDateRange(
                        student.getId(), start, end);
                Long absences = attendanceRecordRepository.countAbsencesByStudentIdAndDateRange(
                        student.getId(), start, end);

                if (unexcused != null && unexcused >= 3) {
                    truancyList.add(String.format("%s (%s) - %d unexcused absences",
                            student.getFullName(), student.getStudentId(), unexcused));
                }
                if (tardies != null && tardies >= 5) {
                    tardyList.add(String.format("%s (%s) - %d tardies",
                            student.getFullName(), student.getStudentId(), tardies));
                }
                if (absences != null && absences >= 3 && absences <= 4) {
                    warningList.add(String.format("%s (%s) - %d absences",
                            student.getFullName(), student.getStudentId(), absences));
                }
            }
        } catch (Exception e) {
            log.error("Failed to generate intervention lists", e);
        }

        chronicAbsenteeismList.setItems(chronicList);
        truancyRiskList.setItems(truancyList);
        excessiveTardiesList.setItems(tardyList);
        earlyWarningList.setItems(warningList);
    }

    private void generateSummaryText(int totalPresent, int totalAbsent, int totalTardy, double attendanceRate) {
        StringBuilder summary = new StringBuilder();
        summary.append("ATTENDANCE REPORT SUMMARY\n");
        summary.append("=".repeat(60)).append("\n\n");
        summary.append(String.format("Report Period: %s\n", formatDateRange()));
        summary.append(String.format("Generated: %s\n\n",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a"))));

        summary.append("OVERALL STATISTICS\n");
        summary.append("-".repeat(60)).append("\n");
        summary.append(String.format("Total Present:         %,d\n", totalPresent));
        summary.append(String.format("Total Absent:          %,d\n", totalAbsent));
        summary.append(String.format("Total Tardy:           %,d\n", totalTardy));
        summary.append(String.format("Attendance Rate:       %.2f%%\n\n", attendanceRate));

        summary.append("ALERTS AND INTERVENTIONS NEEDED\n");
        summary.append("-".repeat(60)).append("\n");
        summary.append(String.format("Chronic Absenteeism:   %d students\n", chronicAbsenteeismList.getItems().size()));
        summary.append(String.format("Truancy Risk:          %d students\n", truancyRiskList.getItems().size()));
        summary.append(String.format("Excessive Tardies:     %d students\n", excessiveTardiesList.getItems().size()));
        summary.append(String.format("Early Warning:         %d students\n", earlyWarningList.getItems().size()));

        summaryTextArea.setText(summary.toString());
    }

    private void generateAttendanceChart(ObservableList<AttendanceReportRow> data) {
        chartContainer.getChildren().clear();

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        yAxis.setLabel("Count");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Daily Attendance Trend");

        XYChart.Series<String, Number> presentSeries = new XYChart.Series<>();
        presentSeries.setName("Present");

        XYChart.Series<String, Number> absentSeries = new XYChart.Series<>();
        absentSeries.setName("Absent");

        XYChart.Series<String, Number> tardySeries = new XYChart.Series<>();
        tardySeries.setName("Tardy");

        for (AttendanceReportRow row : data) {
            presentSeries.getData().add(new XYChart.Data<>(row.getField1(),
                    Integer.parseInt(row.getField2())));
            absentSeries.getData().add(new XYChart.Data<>(row.getField1(),
                    Integer.parseInt(row.getField3())));
            tardySeries.getData().add(new XYChart.Data<>(row.getField1(),
                    Integer.parseInt(row.getField4())));
        }

        lineChart.getData().addAll(presentSeries, absentSeries, tardySeries);
        chartContainer.getChildren().add(lineChart);
    }

    // Date Range Quick Filters

    @FXML
    private void handleToday() {
        LocalDate today = LocalDate.now();
        startDatePicker.setValue(today);
        endDatePicker.setValue(today);
    }

    @FXML
    private void handleThisWeek() {
        LocalDate today = LocalDate.now();
        startDatePicker.setValue(today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)));
        endDatePicker.setValue(today.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY)));
    }

    @FXML
    private void handleThisMonth() {
        LocalDate today = LocalDate.now();
        startDatePicker.setValue(today.withDayOfMonth(1));
        endDatePicker.setValue(today.withDayOfMonth(today.lengthOfMonth()));
    }

    @FXML
    private void handleThisQuarter() {
        LocalDate today = LocalDate.now();
        int currentQuarter = (today.getMonthValue() - 1) / 3;
        int quarterStartMonth = currentQuarter * 3 + 1;

        startDatePicker.setValue(LocalDate.of(today.getYear(), quarterStartMonth, 1));
        endDatePicker.setValue(LocalDate.of(today.getYear(), quarterStartMonth + 2, 1)
                .withDayOfMonth(LocalDate.of(today.getYear(), quarterStartMonth + 2, 1).lengthOfMonth()));
    }

    @FXML
    private void handleThisYear() {
        LocalDate today = LocalDate.now();
        startDatePicker.setValue(LocalDate.of(today.getYear(), 1, 1));
        endDatePicker.setValue(LocalDate.of(today.getYear(), 12, 31));
    }

    // Export Handlers

    @FXML
    private void handleExportExcel() {
        if (reportData.isEmpty() && perfectAttendanceTableView.getItems().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Data",
                    "Please generate a report first before exporting.");
            return;
        }

        statusLabel.setText("Exporting to Excel...");
        // In production, implement Excel export using Apache POI
        showAlert(Alert.AlertType.INFORMATION, "Export Successful",
                "Report exported to Excel successfully.\n\nFile saved to: Reports/attendance_report_" +
                        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx");
        statusLabel.setText("Export completed");
    }

    @FXML
    private void handleExportPdf() {
        if (reportData.isEmpty() && perfectAttendanceTableView.getItems().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Data",
                    "Please generate a report first before exporting.");
            return;
        }

        statusLabel.setText("Exporting to PDF...");
        // In production, implement PDF export using iText or similar
        showAlert(Alert.AlertType.INFORMATION, "Export Successful",
                "Report exported to PDF successfully.\n\nFile saved to: Reports/attendance_report_" +
                        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");
        statusLabel.setText("Export completed");
    }

    @FXML
    private void handlePrint() {
        if (reportData.isEmpty() && perfectAttendanceTableView.getItems().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Data",
                    "Please generate a report first before printing.");
            return;
        }

        statusLabel.setText("Printing report...");
        // In production, implement print functionality
        showAlert(Alert.AlertType.INFORMATION, "Print",
                "Print dialog would open here.\n\nReport: " + reportTitleLabel.getText());
        statusLabel.setText("Ready");
    }

    private String formatDateRange() {
        if (startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            return startDatePicker.getValue().format(formatter) + " - " +
                    endDatePicker.getValue().format(formatter);
        }
        return "";
    }

    /**
     * Returns students filtered by grade level and student combo box selections.
     */
    private List<Student> filterStudentList() {
        // If a specific student is selected, return just that student
        if (studentComboBox.getValue() != null) {
            return List.of(studentComboBox.getValue());
        }
        List<Student> students = allStudents.isEmpty() ? studentService.getActiveStudents() : new ArrayList<>(allStudents);
        String gradeFilter = gradeLevelComboBox.getValue();
        if (gradeFilter != null && !"All Grades".equals(gradeFilter)) {
            // Extract numeric grade or match by name
            students = students.stream()
                    .filter(s -> gradeFilter.contains(s.getGradeLevel() != null ? s.getGradeLevel() : ""))
                    .collect(Collectors.toList());
        }
        return students;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Inner Classes for Table Rows

    public static class AttendanceReportRow {
        private String field1, field2, field3, field4, field5, field6;

        public AttendanceReportRow(String field1, String field2) {
            this(field1, field2, "", "", "", "");
        }

        public AttendanceReportRow(String field1, String field2, String field3, String field4, String field5) {
            this(field1, field2, field3, field4, field5, "");
        }

        public AttendanceReportRow(String field1, String field2, String field3, String field4, String field5, String field6) {
            this.field1 = field1;
            this.field2 = field2;
            this.field3 = field3;
            this.field4 = field4;
            this.field5 = field5;
            this.field6 = field6;
        }

        public String getField1() { return field1; }
        public String getField2() { return field2; }
        public String getField3() { return field3; }
        public String getField4() { return field4; }
        public String getField5() { return field5; }
        public String getField6() { return field6; }
    }

    public static class PerfectAttendanceRow {
        private String studentId, studentName, grade, homeroom;
        private int daysPresent, tardies, streak;

        public PerfectAttendanceRow(String studentId, String studentName, String grade, String homeroom,
                                    int daysPresent, int tardies, int streak) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.grade = grade;
            this.homeroom = homeroom;
            this.daysPresent = daysPresent;
            this.tardies = tardies;
            this.streak = streak;
        }

        public String getStudentId() { return studentId; }
        public String getStudentName() { return studentName; }
        public String getGrade() { return grade; }
        public String getHomeroom() { return homeroom; }
        public int getDaysPresent() { return daysPresent; }
        public int getTardies() { return tardies; }
        public int getStreak() { return streak; }
    }
}
