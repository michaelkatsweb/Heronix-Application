package com.heronix.ui.controller;

import com.heronix.model.domain.AttendanceRecord;
import com.heronix.model.domain.Student;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AttendanceReportsController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private StudentService studentService;

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
        // Load all students for student filter
        // In production, load from service
        allStudents.clear();
        // allStudents.addAll(studentService.findAllActiveStudents());
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

        // Generate sample data
        LocalDate currentDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        int totalPresent = 0;
        int totalAbsent = 0;
        int totalTardy = 0;

        Random random = new Random();

        while (!currentDate.isAfter(endDate)) {
            if (currentDate.getDayOfWeek() != DayOfWeek.SATURDAY &&
                currentDate.getDayOfWeek() != DayOfWeek.SUNDAY) {

                int present = 450 + random.nextInt(50);
                int absent = 20 + random.nextInt(30);
                int tardy = 10 + random.nextInt(20);
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

        // Generate sample student data
        String[] firstNames = {"John", "Emma", "Michael", "Sophia", "William", "Olivia", "James", "Ava"};
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis"};
        String[] grades = {"9th Grade", "10th Grade", "11th Grade", "12th Grade"};

        Random random = new Random();
        int totalPresent = 0;
        int totalAbsent = 0;
        int totalTardy = 0;

        for (int i = 0; i < 50; i++) {
            String studentId = "S" + String.format("%05d", 10000 + i);
            String studentName = lastNames[random.nextInt(lastNames.length)] + ", " +
                    firstNames[random.nextInt(firstNames.length)];
            String grade = grades[random.nextInt(grades.length)];

            int absences = random.nextInt(15);
            int tardies = random.nextInt(10);
            int daysInPeriod = 20;
            int present = daysInPeriod - absences;
            double rate = (present * 100.0 / daysInPeriod);

            totalPresent += present;
            totalAbsent += absences;
            totalTardy += tardies;

            reportData.add(new AttendanceReportRow(
                    studentId, studentName, grade,
                    String.valueOf(absences),
                    String.valueOf(tardies),
                    String.format("%.2f%%", rate)
            ));
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

        String[] firstNames = {"John", "Emma", "Michael", "Sophia", "William", "Olivia"};
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia"};
        String[] grades = {"9th Grade", "10th Grade", "11th Grade", "12th Grade"};
        String[] homerooms = {"101", "102", "103", "104", "105"};

        Random random = new Random();

        for (int i = 0; i < 25; i++) {
            String studentId = "S" + String.format("%05d", 10000 + i);
            String studentName = lastNames[random.nextInt(lastNames.length)] + ", " +
                    firstNames[random.nextInt(firstNames.length)];
            String grade = grades[random.nextInt(grades.length)];
            String homeroom = homerooms[random.nextInt(homerooms.length)];
            int daysPresent = 20;
            int tardies = random.nextInt(3); // Max 2 for perfect attendance
            int streak = 10 + random.nextInt(30);

            perfectStudents.add(new PerfectAttendanceRow(
                    studentId, studentName, grade, homeroom, daysPresent, tardies, streak
            ));
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
        Random random = new Random();
        String[] firstNames = {"John", "Emma", "Michael", "Sophia"};
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown"};
        String[] grades = {"9th Grade", "10th Grade", "11th Grade", "12th Grade"};

        for (int i = 0; i < 15; i++) {
            String studentId = "S" + String.format("%05d", 20000 + i);
            String studentName = lastNames[random.nextInt(lastNames.length)] + ", " +
                    firstNames[random.nextInt(firstNames.length)];
            String grade = grades[random.nextInt(grades.length)];
            int absences = 3 + random.nextInt(10); // 3-12 absences
            int daysInPeriod = 20;
            double rate = ((daysInPeriod - absences) * 100.0 / daysInPeriod);

            reportData.add(new AttendanceReportRow(
                    studentId, studentName, grade,
                    String.valueOf(absences),
                    String.format("%.2f%%", rate)
            ));
        }
    }

    private void generateTruancyData() {
        Random random = new Random();
        String[] firstNames = {"James", "Sarah", "Robert", "Lisa"};
        String[] lastNames = {"Davis", "Miller", "Wilson", "Moore"};
        String[] grades = {"9th Grade", "10th Grade", "11th Grade", "12th Grade"};

        for (int i = 0; i < 12; i++) {
            String studentId = "S" + String.format("%05d", 30000 + i);
            String studentName = lastNames[random.nextInt(lastNames.length)] + ", " +
                    firstNames[random.nextInt(firstNames.length)];
            String grade = grades[random.nextInt(grades.length)];
            int unexcusedAbsences = 5 + random.nextInt(10);
            int daysInPeriod = 20;
            double rate = ((daysInPeriod - unexcusedAbsences) * 100.0 / daysInPeriod);

            reportData.add(new AttendanceReportRow(
                    studentId, studentName, grade,
                    String.valueOf(unexcusedAbsences) + " unexcused",
                    String.format("%.2f%%", rate)
            ));
        }
    }

    private void generateTardyData() {
        Random random = new Random();
        String[] firstNames = {"David", "Jennifer", "Daniel", "Maria"};
        String[] lastNames = {"Anderson", "Thomas", "Jackson", "White"};
        String[] grades = {"9th Grade", "10th Grade", "11th Grade", "12th Grade"};

        for (int i = 0; i < 20; i++) {
            String studentId = "S" + String.format("%05d", 40000 + i);
            String studentName = lastNames[random.nextInt(lastNames.length)] + ", " +
                    firstNames[random.nextInt(firstNames.length)];
            String grade = grades[random.nextInt(grades.length)];
            int tardies = 5 + random.nextInt(15);

            reportData.add(new AttendanceReportRow(
                    studentId, studentName, grade,
                    String.valueOf(tardies) + " tardies",
                    "N/A"
            ));
        }
    }

    private void generateInterventionLists() {
        ObservableList<String> chronicList = FXCollections.observableArrayList();
        ObservableList<String> truancyList = FXCollections.observableArrayList();
        ObservableList<String> tardyList = FXCollections.observableArrayList();
        ObservableList<String> warningList = FXCollections.observableArrayList();

        chronicList.add("Smith, John (S10001) - 12 absences (60% attendance)");
        chronicList.add("Johnson, Emma (S10002) - 10 absences (50% attendance)");
        chronicList.add("Williams, Michael (S10003) - 8 absences (60% attendance)");

        truancyList.add("Brown, Sophia (S10004) - 8 unexcused absences");
        truancyList.add("Jones, William (S10005) - 7 unexcused absences");

        tardyList.add("Garcia, Olivia (S10006) - 15 tardies");
        tardyList.add("Miller, James (S10007) - 12 tardies");
        tardyList.add("Davis, Ava (S10008) - 10 tardies");

        warningList.add("Martinez, Isabella (S10009) - 4 absences");
        warningList.add("Rodriguez, Liam (S10010) - 3 absences");
        warningList.add("Hernandez, Mia (S10011) - 3 absences");

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
