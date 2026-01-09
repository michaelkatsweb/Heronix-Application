package com.heronix.ui.controller;

import com.heronix.model.domain.*;
import com.heronix.service.ELLManagementService;
import com.heronix.service.StudentService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for ELL Dashboard UI
 * Provides comprehensive overview of English Language Learner program including
 * enrollment statistics, proficiency tracking, compliance monitoring, and action items.
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - ELL Management System
 */
@Slf4j
@Component
public class ELLDashboardController {

    @Autowired
    private ELLManagementService ellManagementService;

    @Autowired
    private StudentService studentService;

    // ========================================================================
    // FXML FIELDS - Filters
    // ========================================================================

    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private ComboBox<String> proficiencyFilterComboBox;
    @FXML private ComboBox<String> gradeLevelFilterComboBox;
    @FXML private ComboBox<String> programTypeFilterComboBox;

    // ========================================================================
    // FXML FIELDS - Statistics
    // ========================================================================

    @FXML private Label totalELLLabel;
    @FXML private Label totalELLPercentLabel;
    @FXML private Label activeELLLabel;
    @FXML private Label activeELLPercentLabel;
    @FXML private Label monitoredLabel;
    @FXML private Label monitoredYearsLabel;
    @FXML private Label eligibleReclassLabel;
    @FXML private Label eligibleReclassPercentLabel;
    @FXML private Label assessmentsOverdueLabel;
    @FXML private Label assessmentsDueLabel;
    @FXML private Label titleIIIFundedLabel;
    @FXML private Label titleIIIPercentLabel;

    // ========================================================================
    // FXML FIELDS - Charts
    // ========================================================================

    @FXML private BarChart<String, Number> proficiencyBarChart;
    @FXML private CategoryAxis proficiencyXAxis;
    @FXML private NumberAxis proficiencyYAxis;

    @FXML private PieChart programTypesPieChart;

    @FXML private BarChart<String, Number> languagesBarChart;
    @FXML private CategoryAxis languagesXAxis;
    @FXML private NumberAxis languagesYAxis;

    // ========================================================================
    // FXML FIELDS - Alert Lists
    // ========================================================================

    @FXML private ListView<String> assessmentsDueListView;
    @FXML private ListView<String> parentNotificationsListView;
    @FXML private ListView<String> reclassificationCandidatesListView;
    @FXML private ListView<String> progressMonitoringOverdueListView;

    // ========================================================================
    // FXML FIELDS - Student Table
    // ========================================================================

    @FXML private TextField searchField;
    @FXML private TableView<ELLStudentData> ellStudentsTable;
    @FXML private TableColumn<ELLStudentData, String> studentNameColumn;
    @FXML private TableColumn<ELLStudentData, String> studentIdColumn;
    @FXML private TableColumn<ELLStudentData, String> gradeColumn;
    @FXML private TableColumn<ELLStudentData, String> statusColumn;
    @FXML private TableColumn<ELLStudentData, String> proficiencyColumn;
    @FXML private TableColumn<ELLStudentData, String> programColumn;
    @FXML private TableColumn<ELLStudentData, String> nativeLanguageColumn;
    @FXML private TableColumn<ELLStudentData, Integer> yearsInProgramColumn;
    @FXML private TableColumn<ELLStudentData, String> lastAssessmentColumn;
    @FXML private TableColumn<ELLStudentData, Void> actionsColumn;

    @FXML private Label totalStudentsLabel;

    // ========================================================================
    // FXML FIELDS - Status
    // ========================================================================

    @FXML private Label lastRefreshedLabel;
    @FXML private Label statusLabel;

    // ========================================================================
    // STATE
    // ========================================================================

    private ObservableList<ELLStudentData> allELLStudents = FXCollections.observableArrayList();
    private List<ELLStudent> rawELLStudents = new ArrayList<>();

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    @FXML
    public void initialize() {
        log.info("Initializing ELLDashboardController");
        setupTable();
        setupDefaults();
        setupSearch();
        loadDashboardData();
    }

    private void setupTable() {
        // Configure table columns
        studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        studentIdColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        gradeColumn.setCellValueFactory(new PropertyValueFactory<>("grade"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        proficiencyColumn.setCellValueFactory(new PropertyValueFactory<>("proficiency"));
        programColumn.setCellValueFactory(new PropertyValueFactory<>("program"));
        nativeLanguageColumn.setCellValueFactory(new PropertyValueFactory<>("nativeLanguage"));
        yearsInProgramColumn.setCellValueFactory(new PropertyValueFactory<>("yearsInProgram"));
        lastAssessmentColumn.setCellValueFactory(new PropertyValueFactory<>("lastAssessment"));

        // Actions column with buttons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            private final Button editButton = new Button("Edit");
            private final HBox pane = new HBox(5, viewButton, editButton);

            {
                viewButton.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4 8;");
                editButton.setStyle("-fx-background-color: #388e3c; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4 8;");

                viewButton.setOnAction(event -> {
                    ELLStudentData data = getTableView().getItems().get(getIndex());
                    handleViewStudent(data);
                });

                editButton.setOnAction(event -> {
                    ELLStudentData data = getTableView().getItems().get(getIndex());
                    handleEditStudent(data);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void setupDefaults() {
        // Default filters
        statusFilterComboBox.setValue("ALL");
        proficiencyFilterComboBox.setValue("ALL");
        gradeLevelFilterComboBox.setValue("ALL");
        programTypeFilterComboBox.setValue("ALL");
    }

    private void setupSearch() {
        // Real-time search filtering
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterStudents(newValue);
        });
    }

    // ========================================================================
    // DATA LOADING
    // ========================================================================

    private void loadDashboardData() {
        statusLabel.setText("Loading dashboard data...");

        try {
            // Load all ELL students
            rawELLStudents = ellManagementService.getAllELLStudents();

            // Update statistics
            updateStatistics();

            // Update charts
            updateCharts();

            // Update alert lists
            updateAlertLists();

            // Update student table
            updateStudentTable();

            // Update timestamp
            lastRefreshedLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a")));
            statusLabel.setText("Ready");

            log.info("Dashboard data loaded successfully: {} ELL students", rawELLStudents.size());

        } catch (Exception e) {
            log.error("Error loading dashboard data", e);
            statusLabel.setText("Error loading data");
            showError("Failed to load dashboard data: " + e.getMessage());
        }
    }

    private void updateStatistics() {
        int total = rawELLStudents.size();
        long active = rawELLStudents.stream().filter(ELLStudent::isActiveELL).count();
        long monitored = rawELLStudents.stream().filter(ELLStudent::isMonitored).count();
        long eligibleReclass = rawELLStudents.stream()
                .filter(ELLStudent::getEligibleForReclassification)
                .count();
        long assessmentsOverdue = rawELLStudents.stream()
                .filter(ELLStudent::needsAnnualAssessment)
                .count();
        long titleIIIFunded = rawELLStudents.stream()
                .filter(ELLStudent::getTitleIIIFunded)
                .count();
        long titleIIIEligible = rawELLStudents.stream()
                .filter(ELLStudent::getTitleIIIEligible)
                .count();

        // Total ELL
        totalELLLabel.setText(String.valueOf(total));
        totalELLPercentLabel.setText("100% of ELL enrollment");

        // Active ELL
        activeELLLabel.setText(String.valueOf(active));
        if (total > 0) {
            activeELLPercentLabel.setText(String.format("%.1f%% of total", (double) active / total * 100));
        }

        // Monitored
        monitoredLabel.setText(String.valueOf(monitored));
        monitoredYearsLabel.setText("Across 1-4 years");

        // Eligible for Reclassification
        eligibleReclassLabel.setText(String.valueOf(eligibleReclass));
        if (active > 0) {
            eligibleReclassPercentLabel.setText(String.format("%.1f%% of active", (double) eligibleReclass / active * 100));
        }

        // Assessments Overdue
        assessmentsOverdueLabel.setText(String.valueOf(assessmentsOverdue));
        assessmentsDueLabel.setText(assessmentsOverdue + " due now");

        // Title III Funded
        titleIIIFundedLabel.setText(String.valueOf(titleIIIFunded));
        if (titleIIIEligible > 0) {
            titleIIIPercentLabel.setText(String.format("%.1f%% of eligible", (double) titleIIIFunded / titleIIIEligible * 100));
        }
    }

    private void updateCharts() {
        updateProficiencyChart();
        updateProgramTypesChart();
        updateLanguagesChart();
    }

    private void updateProficiencyChart() {
        Map<ELLStudent.ProficiencyLevel, Long> proficiencyCounts = rawELLStudents.stream()
                .collect(Collectors.groupingBy(ELLStudent::getProficiencyLevel, Collectors.counting()));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Students");

        for (ELLStudent.ProficiencyLevel level : ELLStudent.ProficiencyLevel.values()) {
            long count = proficiencyCounts.getOrDefault(level, 0L);
            series.getData().add(new XYChart.Data<>(level.getDisplayName(), count));
        }

        proficiencyBarChart.getData().clear();
        proficiencyBarChart.getData().add(series);
    }

    private void updateProgramTypesChart() {
        Map<ELLStudent.ProgramType, Long> programCounts = rawELLStudents.stream()
                .filter(s -> s.getProgramType() != null)
                .collect(Collectors.groupingBy(ELLStudent::getProgramType, Collectors.counting()));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        programCounts.forEach((programType, count) -> {
            pieChartData.add(new PieChart.Data(programType.getDisplayName(), count));
        });

        programTypesPieChart.setData(pieChartData);
    }

    private void updateLanguagesChart() {
        Map<String, Long> languageCounts = rawELLStudents.stream()
                .filter(s -> s.getNativeLanguage() != null && !s.getNativeLanguage().isEmpty())
                .collect(Collectors.groupingBy(ELLStudent::getNativeLanguage, Collectors.counting()));

        // Get top 10 languages
        List<Map.Entry<String, Long>> topLanguages = languageCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Students");

        for (Map.Entry<String, Long> entry : topLanguages) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        languagesBarChart.getData().clear();
        languagesBarChart.getData().add(series);
    }

    private void updateAlertLists() {
        // Assessments Due
        List<String> assessmentsDue = rawELLStudents.stream()
                .filter(ELLStudent::needsAnnualAssessment)
                .map(s -> String.format("%s, %s (Grade %s) - Due: %s",
                        s.getStudent().getLastName(),
                        s.getStudent().getFirstName(),
                        s.getStudent().getGradeLevel(),
                        s.getNextAnnualAssessmentDate() != null ?
                                s.getNextAnnualAssessmentDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) :
                                "Overdue"))
                .collect(Collectors.toList());
        assessmentsDueListView.setItems(FXCollections.observableArrayList(assessmentsDue));

        // Parent Notifications Needed
        List<String> parentNotifications = rawELLStudents.stream()
                .filter(ELLStudent::needsParentNotification)
                .map(s -> String.format("%s, %s (Grade %s)",
                        s.getStudent().getLastName(),
                        s.getStudent().getFirstName(),
                        s.getStudent().getGradeLevel()))
                .collect(Collectors.toList());
        parentNotificationsListView.setItems(FXCollections.observableArrayList(parentNotifications));

        // Reclassification Candidates
        List<String> reclassCandidates = rawELLStudents.stream()
                .filter(ELLStudent::getEligibleForReclassification)
                .map(s -> String.format("%s, %s (Grade %s) - %s",
                        s.getStudent().getLastName(),
                        s.getStudent().getFirstName(),
                        s.getStudent().getGradeLevel(),
                        s.getProficiencyLevel().getDisplayName()))
                .collect(Collectors.toList());
        reclassificationCandidatesListView.setItems(FXCollections.observableArrayList(reclassCandidates));

        // Progress Monitoring Overdue
        List<String> progressOverdue = rawELLStudents.stream()
                .filter(ELLStudent::needsProgressMonitoring)
                .map(s -> String.format("%s, %s (Grade %s) - %d days since last check",
                        s.getStudent().getLastName(),
                        s.getStudent().getFirstName(),
                        s.getStudent().getGradeLevel(),
                        s.getDaysSinceProgressMonitoring()))
                .collect(Collectors.toList());
        progressMonitoringOverdueListView.setItems(FXCollections.observableArrayList(progressOverdue));
    }

    private void updateStudentTable() {
        ObservableList<ELLStudentData> data = FXCollections.observableArrayList();

        for (ELLStudent ellStudent : rawELLStudents) {
            ELLStudentData studentData = new ELLStudentData();
            studentData.setEllStudentId(ellStudent.getId());
            studentData.setStudentName(ellStudent.getStudent().getLastName() + ", " +
                    ellStudent.getStudent().getFirstName());
            studentData.setStudentId(ellStudent.getStudent().getStudentId());
            studentData.setGrade(ellStudent.getStudent().getGradeLevel() != null ?
                    ellStudent.getStudent().getGradeLevel().toString() : "");
            studentData.setStatus(ellStudent.getEllStatus().getDisplayName());
            studentData.setProficiency(ellStudent.getProficiencyLevel().getDisplayName());
            studentData.setProgram(ellStudent.getProgramType() != null ?
                    ellStudent.getProgramType().getDisplayName() : "");
            studentData.setNativeLanguage(ellStudent.getNativeLanguage());
            studentData.setYearsInProgram(ellStudent.getYearsInProgram());
            studentData.setLastAssessment(ellStudent.getLastProficiencyAssessmentDate() != null ?
                    ellStudent.getLastProficiencyAssessmentDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : "");

            data.add(studentData);
        }

        allELLStudents = data;
        ellStudentsTable.setItems(allELLStudents);
        totalStudentsLabel.setText(String.format("Showing %d students", data.size()));
    }

    // ========================================================================
    // EVENT HANDLERS
    // ========================================================================

    @FXML
    private void handleApplyFilters() {
        statusLabel.setText("Applying filters...");

        try {
            List<ELLStudent> filtered = new ArrayList<>(rawELLStudents);

            // Apply status filter
            String statusFilter = statusFilterComboBox.getValue();
            if (!"ALL".equals(statusFilter)) {
                if ("MONITORED".equals(statusFilter)) {
                    filtered = filtered.stream().filter(ELLStudent::isMonitored).collect(Collectors.toList());
                } else {
                    ELLStudent.ELLStatus status = ELLStudent.ELLStatus.valueOf(statusFilter);
                    filtered = filtered.stream()
                            .filter(s -> s.getEllStatus() == status)
                            .collect(Collectors.toList());
                }
            }

            // Apply proficiency filter
            String proficiencyFilter = proficiencyFilterComboBox.getValue();
            if (!"ALL".equals(proficiencyFilter)) {
                ELLStudent.ProficiencyLevel level = ELLStudent.ProficiencyLevel.valueOf(proficiencyFilter);
                filtered = filtered.stream()
                        .filter(s -> s.getProficiencyLevel() == level)
                        .collect(Collectors.toList());
            }

            // Apply grade level filter
            String gradeFilter = gradeLevelFilterComboBox.getValue();
            if (!"ALL".equals(gradeFilter)) {
                filtered = filtered.stream()
                        .filter(s -> s.getStudent().getGradeLevel() != null &&
                                s.getStudent().getGradeLevel().toString().equals(gradeFilter))
                        .collect(Collectors.toList());
            }

            // Apply program type filter
            String programFilter = programTypeFilterComboBox.getValue();
            if (!"ALL".equals(programFilter)) {
                ELLStudent.ProgramType programType = ELLStudent.ProgramType.valueOf(programFilter);
                filtered = filtered.stream()
                        .filter(s -> s.getProgramType() == programType)
                        .collect(Collectors.toList());
            }

            // Update display with filtered results
            rawELLStudents = filtered;
            updateStatistics();
            updateCharts();
            updateStudentTable();

            statusLabel.setText("Filters applied");
            log.info("Filters applied: {} students match criteria", filtered.size());

        } catch (Exception e) {
            log.error("Error applying filters", e);
            statusLabel.setText("Error applying filters");
            showError("Failed to apply filters: " + e.getMessage());
        }
    }

    @FXML
    private void handleResetFilters() {
        statusFilterComboBox.setValue("ALL");
        proficiencyFilterComboBox.setValue("ALL");
        gradeLevelFilterComboBox.setValue("ALL");
        programTypeFilterComboBox.setValue("ALL");
        handleRefresh();
    }

    @FXML
    private void handleRefresh() {
        loadDashboardData();
    }

    @FXML
    private void handleAddStudent() {
        // TODO: Open add ELL student dialog
        showInfo("Add New ELL Student functionality will be implemented in the ELL Student Profile component.");
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private void filterStudents(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            ellStudentsTable.setItems(allELLStudents);
            totalStudentsLabel.setText(String.format("Showing %d students", allELLStudents.size()));
            return;
        }

        String searchLower = searchText.toLowerCase();
        ObservableList<ELLStudentData> filtered = allELLStudents.stream()
                .filter(student ->
                        student.getStudentName().toLowerCase().contains(searchLower) ||
                        student.getStudentId().toLowerCase().contains(searchLower) ||
                        (student.getNativeLanguage() != null &&
                         student.getNativeLanguage().toLowerCase().contains(searchLower)))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        ellStudentsTable.setItems(filtered);
        totalStudentsLabel.setText(String.format("Showing %d of %d students", filtered.size(), allELLStudents.size()));
    }

    private void handleViewStudent(ELLStudentData data) {
        // TODO: Open view student dialog
        showInfo("View Student Details: " + data.getStudentName());
    }

    private void handleEditStudent(ELLStudentData data) {
        // TODO: Open edit student dialog
        showInfo("Edit Student: " + data.getStudentName());
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
    // DATA CLASS
    // ========================================================================

    public static class ELLStudentData {
        private Long ellStudentId;
        private String studentName;
        private String studentId;
        private String grade;
        private String status;
        private String proficiency;
        private String program;
        private String nativeLanguage;
        private Integer yearsInProgram;
        private String lastAssessment;

        // Getters and Setters
        public Long getEllStudentId() { return ellStudentId; }
        public void setEllStudentId(Long ellStudentId) { this.ellStudentId = ellStudentId; }

        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }

        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }

        public String getGrade() { return grade; }
        public void setGrade(String grade) { this.grade = grade; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getProficiency() { return proficiency; }
        public void setProficiency(String proficiency) { this.proficiency = proficiency; }

        public String getProgram() { return program; }
        public void setProgram(String program) { this.program = program; }

        public String getNativeLanguage() { return nativeLanguage; }
        public void setNativeLanguage(String nativeLanguage) { this.nativeLanguage = nativeLanguage; }

        public Integer getYearsInProgram() { return yearsInProgram; }
        public void setYearsInProgram(Integer yearsInProgram) { this.yearsInProgram = yearsInProgram; }

        public String getLastAssessment() { return lastAssessment; }
        public void setLastAssessment(String lastAssessment) { this.lastAssessment = lastAssessment; }
    }
}
