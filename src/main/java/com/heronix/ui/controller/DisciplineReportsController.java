package com.heronix.ui.controller;

import com.heronix.model.domain.*;
import com.heronix.service.BehaviorReportingService;
import com.heronix.service.BehaviorIncidentService;
import com.heronix.service.StudentService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for Discipline Reports UI
 * Provides comprehensive reporting and analytics for behavior incidents including
 * school-wide statistics, student summaries, trend analysis, and intervention planning.
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Behavior & Discipline Management System
 */
@Slf4j
@Component
public class DisciplineReportsController {

    @Autowired
    private BehaviorReportingService behaviorReportingService;

    @Autowired
    private BehaviorIncidentService behaviorIncidentService;

    @Autowired
    private StudentService studentService;

    // ========================================================================
    // FXML FIELDS - Filters
    // ========================================================================

    @FXML private ComboBox<String> reportTypeComboBox;
    @FXML private ComboBox<Student> studentFilterComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> behaviorTypeFilterComboBox;

    // ========================================================================
    // FXML FIELDS - Summary Statistics
    // ========================================================================

    @FXML private Label totalIncidentsLabel;
    @FXML private Label totalIncidentsChangeLabel;
    @FXML private Label positiveIncidentsLabel;
    @FXML private Label positivePercentLabel;
    @FXML private Label negativeIncidentsLabel;
    @FXML private Label negativePercentLabel;
    @FXML private Label adminReferralsLabel;
    @FXML private Label adminReferralsPercentLabel;
    @FXML private Label studentsAffectedLabel;
    @FXML private Label avgIncidentsPerStudentLabel;

    // ========================================================================
    // FXML FIELDS - Charts
    // ========================================================================

    @FXML private PieChart categoryPieChart;
    @FXML private PieChart severityPieChart;
    @FXML private LineChart<String, Number> trendLineChart;
    @FXML private CategoryAxis trendXAxis;
    @FXML private NumberAxis trendYAxis;

    // ========================================================================
    // FXML FIELDS - Top Positive Students
    // ========================================================================

    @FXML private VBox topPositiveStudentsBox;
    @FXML private TableView<StudentIncidentData> topPositiveStudentsTable;
    @FXML private TableColumn<StudentIncidentData, Integer> topPositiveRankColumn;
    @FXML private TableColumn<StudentIncidentData, String> topPositiveStudentColumn;
    @FXML private TableColumn<StudentIncidentData, String> topPositiveGradeColumn;
    @FXML private TableColumn<StudentIncidentData, Long> topPositiveCountColumn;
    @FXML private TableColumn<StudentIncidentData, String> topPositiveRatioColumn;

    // ========================================================================
    // FXML FIELDS - Intervention Students
    // ========================================================================

    @FXML private VBox interventionStudentsBox;
    @FXML private TableView<StudentIncidentData> interventionStudentsTable;
    @FXML private TableColumn<StudentIncidentData, Integer> interventionRankColumn;
    @FXML private TableColumn<StudentIncidentData, String> interventionStudentColumn;
    @FXML private TableColumn<StudentIncidentData, String> interventionGradeColumn;
    @FXML private TableColumn<StudentIncidentData, Long> interventionCountColumn;
    @FXML private TableColumn<StudentIncidentData, String> interventionCategoryColumn;
    @FXML private TableColumn<StudentIncidentData, Long> interventionUncontactedColumn;

    // ========================================================================
    // FXML FIELDS - Incident List
    // ========================================================================

    @FXML private TextField searchField;
    @FXML private TableView<BehaviorIncident> incidentsTable;
    @FXML private TableColumn<BehaviorIncident, String> incidentDateColumn;
    @FXML private TableColumn<BehaviorIncident, String> incidentTimeColumn;
    @FXML private TableColumn<BehaviorIncident, String> incidentStudentColumn;
    @FXML private TableColumn<BehaviorIncident, String> incidentGradeColumn;
    @FXML private TableColumn<BehaviorIncident, String> incidentTypeColumn;
    @FXML private TableColumn<BehaviorIncident, String> incidentCategoryColumn;
    @FXML private TableColumn<BehaviorIncident, String> incidentSeverityColumn;
    @FXML private TableColumn<BehaviorIncident, String> incidentLocationColumn;
    @FXML private TableColumn<BehaviorIncident, String> incidentTeacherColumn;
    @FXML private TableColumn<BehaviorIncident, Void> incidentActionsColumn;

    @FXML private Label totalRecordsLabel;

    // ========================================================================
    // FXML FIELDS - Student Summary
    // ========================================================================

    @FXML private VBox studentSummaryBox;
    @FXML private Label studentNameLabel;
    @FXML private Label studentGradeLabel;
    @FXML private Label studentIdLabel;
    @FXML private Label behaviorRatioLabel;
    @FXML private Label mostCommonCategoryLabel;
    @FXML private Label uncontactedParentLabel;

    // ========================================================================
    // FXML FIELDS - Status
    // ========================================================================

    @FXML private Label dateRangeLabel;
    @FXML private Label statusLabel;

    // ========================================================================
    // STATE
    // ========================================================================

    private ObservableList<BehaviorIncident> allIncidents = FXCollections.observableArrayList();
    private BehaviorReportingService.SchoolBehaviorReport currentReport;

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    @FXML
    public void initialize() {
        log.info("Initializing DisciplineReportsController");
        setupComboBoxes();
        setupTables();
        setupDefaults();
        setupSearch();
    }

    private void setupComboBoxes() {
        // Student Filter ComboBox
        studentFilterComboBox.setConverter(new StringConverter<Student>() {
            @Override
            public String toString(Student student) {
                if (student == null) return "";
                return String.format("%s, %s (%s) - Grade %s",
                        student.getLastName(),
                        student.getFirstName(),
                        student.getStudentId(),
                        student.getGradeLevel() != null ? student.getGradeLevel() : "N/A");
            }

            @Override
            public Student fromString(String string) {
                return null;
            }
        });

        // Load students
        List<Student> students = studentService.getAllStudents();
        studentFilterComboBox.setItems(FXCollections.observableArrayList(students));

        // Report type change handler
        reportTypeComboBox.setOnAction(e -> updateReportTypeVisibility());
    }

    private void setupTables() {
        // Top Positive Students Table
        topPositiveRankColumn.setCellValueFactory(new PropertyValueFactory<>("rank"));
        topPositiveStudentColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        topPositiveGradeColumn.setCellValueFactory(new PropertyValueFactory<>("grade"));
        topPositiveCountColumn.setCellValueFactory(new PropertyValueFactory<>("count"));
        topPositiveRatioColumn.setCellValueFactory(new PropertyValueFactory<>("ratio"));

        // Intervention Students Table
        interventionRankColumn.setCellValueFactory(new PropertyValueFactory<>("rank"));
        interventionStudentColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        interventionGradeColumn.setCellValueFactory(new PropertyValueFactory<>("grade"));
        interventionCountColumn.setCellValueFactory(new PropertyValueFactory<>("count"));
        interventionCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        interventionUncontactedColumn.setCellValueFactory(new PropertyValueFactory<>("uncontacted"));

        // Incidents Table
        incidentDateColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getIncidentDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))));

        incidentTimeColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getIncidentTime() != null ?
                                cellData.getValue().getIncidentTime().format(DateTimeFormatter.ofPattern("HH:mm")) : ""));

        incidentStudentColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getStudent().getLastName() + ", " +
                                cellData.getValue().getStudent().getFirstName()));

        incidentGradeColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getStudent().getGradeLevel() != null ?
                                cellData.getValue().getStudent().getGradeLevel().toString() : ""));

        incidentTypeColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getBehaviorType().name()));

        incidentCategoryColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getBehaviorCategory().getDisplayName()));

        incidentSeverityColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getSeverityLevel() != null ?
                                cellData.getValue().getSeverityLevel().name() : ""));

        incidentLocationColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getIncidentLocation() != null ?
                                cellData.getValue().getIncidentLocation().getDisplayName() : ""));

        incidentTeacherColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getReportingTeacher() != null ?
                                cellData.getValue().getReportingTeacher().getLastName() : ""));

        // Actions column with view button
        incidentActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("View");

            {
                viewButton.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10;");
                viewButton.setOnAction(event -> {
                    BehaviorIncident incident = getTableView().getItems().get(getIndex());
                    handleViewIncident(incident);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewButton);
            }
        });
    }

    private void setupDefaults() {
        // Default date range: last 30 days
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusDays(30));

        // Default report type
        reportTypeComboBox.setValue("SCHOOL_WIDE");

        // Default behavior type filter
        behaviorTypeFilterComboBox.setValue("ALL");

        // Hide conditional sections
        topPositiveStudentsBox.setVisible(false);
        topPositiveStudentsBox.setManaged(false);
        interventionStudentsBox.setVisible(false);
        interventionStudentsBox.setManaged(false);
        studentSummaryBox.setVisible(false);
        studentSummaryBox.setManaged(false);
    }

    private void setupSearch() {
        // Real-time search filtering
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterIncidents(newValue);
        });
    }

    // ========================================================================
    // EVENT HANDLERS
    // ========================================================================

    @FXML
    private void handleGenerateReport() {
        statusLabel.setText("Generating report...");

        try {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            if (startDate == null || endDate == null) {
                showError("Please select start and end dates");
                statusLabel.setText("Error: Date range required");
                return;
            }

            if (endDate.isBefore(startDate)) {
                showError("End date must be after start date");
                statusLabel.setText("Error: Invalid date range");
                return;
            }

            String reportType = reportTypeComboBox.getValue();
            Student selectedStudent = studentFilterComboBox.getValue();

            // Update date range label
            dateRangeLabel.setText(String.format("%s to %s",
                    startDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                    endDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))));

            if ("STUDENT_SUMMARY".equals(reportType) && selectedStudent != null) {
                generateStudentSummaryReport(selectedStudent, startDate, endDate);
            } else {
                generateSchoolWideReport(startDate, endDate);
            }

            if ("POSITIVE_RECOGNITION".equals(reportType)) {
                generatePositiveRecognitionReport(startDate, endDate);
            }

            if ("INTERVENTION_NEEDED".equals(reportType)) {
                generateInterventionReport(startDate, endDate);
            }

            loadIncidentsList(startDate, endDate, selectedStudent);

            statusLabel.setText("Report generated successfully");
            log.info("Report generated: {} from {} to {}", reportType, startDate, endDate);

        } catch (Exception e) {
            log.error("Error generating report", e);
            showError("Failed to generate report: " + e.getMessage());
            statusLabel.setText("Error generating report");
        }
    }

    @FXML
    private void handleExportPDF() {
        statusLabel.setText("Exporting to PDF...");
        showInfo("PDF export functionality will be implemented in a future update.");
        statusLabel.setText("Ready");
    }

    @FXML
    private void handleExportExcel() {
        statusLabel.setText("Exporting to Excel...");
        showInfo("Excel export functionality will be implemented in a future update.");
        statusLabel.setText("Ready");
    }

    // ========================================================================
    // REPORT GENERATION METHODS
    // ========================================================================

    private void generateSchoolWideReport(LocalDate startDate, LocalDate endDate) {
        currentReport = behaviorReportingService.generateSchoolBehaviorReport(startDate, endDate);

        // Update statistics cards
        totalIncidentsLabel.setText(String.valueOf(currentReport.getTotalIncidents()));
        positiveIncidentsLabel.setText(String.valueOf(currentReport.getPositiveIncidents()));
        negativeIncidentsLabel.setText(String.valueOf(currentReport.getNegativeIncidents()));
        adminReferralsLabel.setText(String.valueOf(currentReport.getAdminReferralsCount()));
        studentsAffectedLabel.setText(String.valueOf(currentReport.getStudentsWithIncidents()));

        // Calculate percentages
        if (currentReport.getTotalIncidents() > 0) {
            double positivePercent = (double) currentReport.getPositiveIncidents() / currentReport.getTotalIncidents() * 100;
            double negativePercent = (double) currentReport.getNegativeIncidents() / currentReport.getTotalIncidents() * 100;
            positivePercentLabel.setText(String.format("%.1f%% of total", positivePercent));
            negativePercentLabel.setText(String.format("%.1f%% of total", negativePercent));

            if (currentReport.getNegativeIncidents() > 0) {
                double adminReferralPercent = (double) currentReport.getAdminReferralsCount() /
                        currentReport.getNegativeIncidents() * 100;
                adminReferralsPercentLabel.setText(String.format("%.1f%% of negative", adminReferralPercent));
            }

            double avgPerStudent = (double) currentReport.getTotalIncidents() / currentReport.getStudentsWithIncidents();
            avgIncidentsPerStudentLabel.setText(String.format("%.1f avg per student", avgPerStudent));
        }

        // Update charts
        updateCategoryChart(currentReport.getCategoryBreakdown());
        updateSeverityChart(currentReport.getSeverityBreakdown());
    }

    private void generateStudentSummaryReport(Student student, LocalDate startDate, LocalDate endDate) {
        BehaviorReportingService.StudentBehaviorSummary summary =
                behaviorReportingService.generateStudentBehaviorSummary(student, startDate, endDate);

        // Show student summary box
        studentSummaryBox.setVisible(true);
        studentSummaryBox.setManaged(true);

        // Update student information
        studentNameLabel.setText(student.getFirstName() + " " + student.getLastName());
        studentGradeLabel.setText(student.getGradeLevel() != null ? student.getGradeLevel().toString() : "N/A");
        studentIdLabel.setText(student.getStudentId());

        // Update metrics
        behaviorRatioLabel.setText(String.format("%.2f", summary.getBehaviorRatio()));
        uncontactedParentLabel.setText(String.valueOf(summary.getUncontactedParentIncidents()));

        BehaviorIncident.BehaviorCategory mostCommon =
                behaviorReportingService.getMostCommonCategory(student, startDate, endDate);
        mostCommonCategoryLabel.setText(mostCommon != null ? mostCommon.getDisplayName() : "N/A");

        // Update summary stats for student
        totalIncidentsLabel.setText(String.valueOf(summary.getTotalIncidents()));
        positiveIncidentsLabel.setText(String.valueOf(summary.getPositiveIncidents()));
        negativeIncidentsLabel.setText(String.valueOf(summary.getNegativeIncidents()));

        // Update charts
        updateCategoryChart(summary.getCategoryBreakdown());
        updateSeverityChart(summary.getSeverityBreakdown());

        // Generate trend
        List<BehaviorReportingService.MonthlyBehaviorTrend> trends =
                behaviorReportingService.generateMonthlyTrend(student, startDate, endDate);
        updateTrendChart(trends);
    }

    private void generatePositiveRecognitionReport(LocalDate startDate, LocalDate endDate) {
        List<BehaviorReportingService.StudentIncidentCount> topStudents =
                behaviorReportingService.getTopPositiveBehaviorStudents(startDate, endDate, 20);

        topPositiveStudentsBox.setVisible(true);
        topPositiveStudentsBox.setManaged(true);

        ObservableList<StudentIncidentData> data = FXCollections.observableArrayList();
        int rank = 1;
        for (BehaviorReportingService.StudentIncidentCount sic : topStudents) {
            Student student = sic.getStudent();
            double ratio = behaviorIncidentService.calculateBehaviorRatio(
                    student, startDate, endDate);

            StudentIncidentData sid = new StudentIncidentData();
            sid.setRank(rank++);
            sid.setStudentName(student.getLastName() + ", " + student.getFirstName());
            sid.setGrade(student.getGradeLevel() != null ? student.getGradeLevel().toString() : "");
            sid.setCount(sic.getCount());
            sid.setRatio(String.format("%.2f:1", ratio));

            data.add(sid);
        }

        topPositiveStudentsTable.setItems(data);
    }

    private void generateInterventionReport(LocalDate startDate, LocalDate endDate) {
        List<BehaviorReportingService.StudentIncidentCount> students =
                behaviorReportingService.getStudentsRequiringIntervention(startDate, endDate, 20);

        interventionStudentsBox.setVisible(true);
        interventionStudentsBox.setManaged(true);

        ObservableList<StudentIncidentData> data = FXCollections.observableArrayList();
        int rank = 1;
        for (BehaviorReportingService.StudentIncidentCount sic : students) {
            Student student = sic.getStudent();
            BehaviorIncident.BehaviorCategory mostCommon =
                    behaviorReportingService.getMostCommonCategory(student, startDate, endDate);
            long uncontacted = behaviorIncidentService.getUncontactedParentIncidents(student).size();

            StudentIncidentData sid = new StudentIncidentData();
            sid.setRank(rank++);
            sid.setStudentName(student.getLastName() + ", " + student.getFirstName());
            sid.setGrade(student.getGradeLevel() != null ? student.getGradeLevel().toString() : "");
            sid.setCount(sic.getCount());
            sid.setCategory(mostCommon != null ? mostCommon.getDisplayName() : "");
            sid.setUncontacted(uncontacted);

            data.add(sid);
        }

        interventionStudentsTable.setItems(data);
    }

    private void loadIncidentsList(LocalDate startDate, LocalDate endDate, Student student) {
        List<BehaviorIncident> incidents;

        if (student != null) {
            incidents = behaviorIncidentService.getIncidentsByStudentAndDateRange(student, startDate, endDate);
        } else {
            incidents = behaviorIncidentService.getAllIncidentsByDateRange(startDate, endDate);
        }

        // Apply behavior type filter
        String behaviorTypeFilter = behaviorTypeFilterComboBox.getValue();
        if ("POSITIVE".equals(behaviorTypeFilter)) {
            incidents = incidents.stream()
                    .filter(BehaviorIncident::isPositive)
                    .collect(Collectors.toList());
        } else if ("NEGATIVE".equals(behaviorTypeFilter)) {
            incidents = incidents.stream()
                    .filter(BehaviorIncident::isNegative)
                    .collect(Collectors.toList());
        }

        allIncidents = FXCollections.observableArrayList(incidents);
        incidentsTable.setItems(allIncidents);
        totalRecordsLabel.setText(String.format("Showing %d records", incidents.size()));
    }

    // ========================================================================
    // CHART UPDATE METHODS
    // ========================================================================

    private void updateCategoryChart(Map<BehaviorIncident.BehaviorCategory, Long> categoryBreakdown) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        categoryBreakdown.forEach((category, count) -> {
            pieChartData.add(new PieChart.Data(category.getDisplayName(), count));
        });

        categoryPieChart.setData(pieChartData);
    }

    private void updateSeverityChart(Map<BehaviorIncident.SeverityLevel, Long> severityBreakdown) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        severityBreakdown.forEach((severity, count) -> {
            pieChartData.add(new PieChart.Data(severity.name(), count));
        });

        severityPieChart.setData(pieChartData);
    }

    private void updateTrendChart(List<BehaviorReportingService.MonthlyBehaviorTrend> trends) {
        XYChart.Series<String, Number> positiveSeries = new XYChart.Series<>();
        positiveSeries.setName("Positive");

        XYChart.Series<String, Number> negativeSeries = new XYChart.Series<>();
        negativeSeries.setName("Negative");

        for (BehaviorReportingService.MonthlyBehaviorTrend trend : trends) {
            positiveSeries.getData().add(new XYChart.Data<>(trend.getMonthKey(), trend.getPositiveIncidents()));
            negativeSeries.getData().add(new XYChart.Data<>(trend.getMonthKey(), trend.getNegativeIncidents()));
        }

        trendLineChart.getData().clear();
        trendLineChart.getData().addAll(positiveSeries, negativeSeries);
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private void updateReportTypeVisibility() {
        String reportType = reportTypeComboBox.getValue();

        // Reset visibility
        topPositiveStudentsBox.setVisible(false);
        topPositiveStudentsBox.setManaged(false);
        interventionStudentsBox.setVisible(false);
        interventionStudentsBox.setManaged(false);
        studentSummaryBox.setVisible(false);
        studentSummaryBox.setManaged(false);
    }

    private void filterIncidents(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            incidentsTable.setItems(allIncidents);
            totalRecordsLabel.setText(String.format("Showing %d records", allIncidents.size()));
            return;
        }

        String searchLower = searchText.toLowerCase();
        ObservableList<BehaviorIncident> filtered = allIncidents.stream()
                .filter(incident ->
                        incident.getStudent().getFirstName().toLowerCase().contains(searchLower) ||
                        incident.getStudent().getLastName().toLowerCase().contains(searchLower) ||
                        incident.getBehaviorCategory().getDisplayName().toLowerCase().contains(searchLower) ||
                        (incident.getIncidentDescription() != null &&
                         incident.getIncidentDescription().toLowerCase().contains(searchLower)))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        incidentsTable.setItems(filtered);
        totalRecordsLabel.setText(String.format("Showing %d of %d records", filtered.size(), allIncidents.size()));
    }

    private void handleViewIncident(BehaviorIncident incident) {
        // TODO: Open incident detail dialog
        showInfo("Incident Details: " + incident.getBehaviorCategory().getDisplayName());
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ========================================================================
    // DATA CLASSES
    // ========================================================================

    public static class StudentIncidentData {
        private Integer rank;
        private String studentName;
        private String grade;
        private Long count;
        private String ratio;
        private String category;
        private Long uncontacted;

        public Integer getRank() { return rank; }
        public void setRank(Integer rank) { this.rank = rank; }

        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }

        public String getGrade() { return grade; }
        public void setGrade(String grade) { this.grade = grade; }

        public Long getCount() { return count; }
        public void setCount(Long count) { this.count = count; }

        public String getRatio() { return ratio; }
        public void setRatio(String ratio) { this.ratio = ratio; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public Long getUncontacted() { return uncontacted; }
        public void setUncontacted(Long uncontacted) { this.uncontacted = uncontacted; }
    }
}
