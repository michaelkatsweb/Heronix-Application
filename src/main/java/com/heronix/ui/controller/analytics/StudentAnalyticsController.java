package com.heronix.ui.controller.analytics;

import com.heronix.dto.analytics.*;
import com.heronix.model.domain.Campus;
import com.heronix.model.domain.Student;
import com.heronix.repository.CampusRepository;
import com.heronix.service.analytics.StudentAnalyticsService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.heronix.service.export.AnalyticsExportService;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Student Analytics Controller
 *
 * Controller for the Student Analytics view with 5 tabs:
 * - Enrollment Trends
 * - Demographics
 * - Special Needs
 * - At-Risk Students
 * - GPA Analytics
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 59 - Comprehensive Analytics Module
 */
@Slf4j
@Component
public class StudentAnalyticsController {

    // ========================================================================
    // FXML COMPONENTS
    // ========================================================================

    // Filters
    @FXML private ComboBox<Campus> campusFilter;
    @FXML private ComboBox<String> yearFilter;
    @FXML private TabPane analyticsTabs;

    // Enrollment Tab
    @FXML private Label totalEnrollmentValue;
    @FXML private Label enrollmentChangeLabel;
    @FXML private Label newStudentsValue;
    @FXML private Label projectedValue;
    @FXML private Label capacityValue;
    @FXML private BarChart<String, Number> enrollmentByGradeChart;
    @FXML private LineChart<String, Number> enrollmentTrendChart;

    // Demographics Tab
    @FXML private PieChart genderChart;
    @FXML private PieChart ethnicityChart;
    @FXML private PieChart raceChart;
    @FXML private PieChart languageChart;
    @FXML private TableView<Map.Entry<String, Long>> demographicsTable;
    @FXML private TableColumn<Map.Entry<String, Long>, String> categoryCol;
    @FXML private TableColumn<Map.Entry<String, Long>, Long> countCol;
    @FXML private TableColumn<Map.Entry<String, Long>, String> percentCol;

    // Special Needs Tab
    @FXML private Label iepCountValue;
    @FXML private Label iepPercentLabel;
    @FXML private Label plan504CountValue;
    @FXML private Label plan504PercentLabel;
    @FXML private Label giftedCountValue;
    @FXML private Label giftedPercentLabel;
    @FXML private Label ellCountValue;
    @FXML private Label ellPercentLabel;
    @FXML private PieChart specialNeedsChart;
    @FXML private StackedBarChart<String, Number> specialNeedsByGradeChart;
    @FXML private ComboBox<String> specialNeedsTypeFilter;
    @FXML private TableView<?> specialNeedsTable;

    // At-Risk Tab
    @FXML private Label criticalRiskValue;
    @FXML private Label highRiskValue;
    @FXML private Label moderateRiskValue;
    @FXML private Label totalAtRiskValue;
    @FXML private Label atRiskPercentLabel;
    @FXML private BarChart<String, Number> atRiskByGradeChart;
    @FXML private PieChart riskLevelChart;
    @FXML private ComboBox<String> riskLevelFilter;
    @FXML private TableView<Student> atRiskTable;
    @FXML private TableColumn<Student, String> arStudentIdCol;
    @FXML private TableColumn<Student, String> arStudentNameCol;
    @FXML private TableColumn<Student, String> arGradeCol;
    @FXML private TableColumn<Student, Double> arGpaCol;
    @FXML private TableColumn<Student, String> arRiskLevelCol;

    // GPA Tab
    @FXML private Label avgGpaValue;
    @FXML private Label highHonorsValue;
    @FXML private Label honorsValue;
    @FXML private Label honorableMentionValue;
    @FXML private BarChart<String, Number> gpaDistributionChart;
    @FXML private LineChart<String, Number> avgGpaByGradeChart;
    @FXML private ComboBox<String> honorRollTierFilter;
    @FXML private TableView<Student> honorRollTable;
    @FXML private TableColumn<Student, String> hrStudentIdCol;
    @FXML private TableColumn<Student, String> hrStudentNameCol;
    @FXML private TableColumn<Student, String> hrGradeCol;
    @FXML private TableColumn<Student, Double> hrGpaCol;
    @FXML private TableColumn<Student, String> hrTierCol;

    // Status Bar
    @FXML private Label statusLabel;
    @FXML private Label lastUpdatedLabel;

    // ========================================================================
    // DEPENDENCIES
    // ========================================================================

    @Autowired
    private StudentAnalyticsService studentAnalyticsService;

    @Autowired(required = false)
    private CampusRepository campusRepository;

    @Autowired
    private ApplicationContext springContext;

    @Autowired
    private AnalyticsExportService analyticsExportService;

    // ========================================================================
    // STATE
    // ========================================================================

    private Long selectedCampusId = null;
    private StudentAnalyticsDTO currentAnalytics;

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    @FXML
    public void initialize() {
        log.info("Initializing Student Analytics Controller...");

        // Setup filters
        setupFilters();

        // Setup table columns
        setupTableColumns();

        // Load initial data
        Platform.runLater(this::loadAllData);

        log.info("Student Analytics Controller initialized");
    }

    private void setupFilters() {
        // Campus filter
        if (campusRepository != null) {
            try {
                List<Campus> campuses = campusRepository.findAll();
                campusFilter.getItems().add(null); // "All Campuses" option
                campusFilter.getItems().addAll(campuses);
                campusFilter.setConverter(new javafx.util.StringConverter<>() {
                    @Override
                    public String toString(Campus campus) {
                        return campus == null ? "All Campuses" : campus.getName();
                    }

                    @Override
                    public Campus fromString(String s) {
                        return null;
                    }
                });
            } catch (Exception e) {
                log.warn("Could not load campuses: {}", e.getMessage());
            }
        }

        campusFilter.setOnAction(e -> {
            Campus selected = campusFilter.getValue();
            selectedCampusId = selected != null ? selected.getId() : null;
            loadAllData();
        });

        // Year filter
        yearFilter.getItems().addAll("2025-26", "2024-25", "2023-24");
        yearFilter.setValue("2025-26");
        yearFilter.setOnAction(e -> loadAllData());

        // Special needs type filter
        specialNeedsTypeFilter.getItems().addAll("All Types", "IEP", "504 Plan", "Gifted", "ELL");
        specialNeedsTypeFilter.setValue("All Types");

        // Risk level filter
        riskLevelFilter.getItems().addAll("All Risk Levels", "Critical", "High", "Moderate");
        riskLevelFilter.setValue("All Risk Levels");

        // Honor roll tier filter
        honorRollTierFilter.getItems().addAll("All Tiers", "High Honors", "Honors", "Honorable Mention");
        honorRollTierFilter.setValue("All Tiers");
    }

    private void setupTableColumns() {
        // At-Risk table columns
        if (arStudentIdCol != null) {
            arStudentIdCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(data.getValue().getStudentId()));
            arStudentNameCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().getFirstName() + " " + data.getValue().getLastName()));
            arGradeCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(data.getValue().getGradeLevel()));
            arGpaCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getCurrentGPA()));
            arRiskLevelCol.setCellValueFactory(data -> {
                Double gpa = data.getValue().getCurrentGPA();
                String level = gpa == null ? "Unknown" :
                        gpa < 1.5 ? "Critical" :
                        gpa < 2.0 ? "High" : "Moderate";
                return new javafx.beans.property.SimpleStringProperty(level);
            });
        }

        // Honor Roll table columns
        if (hrStudentIdCol != null) {
            hrStudentIdCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(data.getValue().getStudentId()));
            hrStudentNameCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().getFirstName() + " " + data.getValue().getLastName()));
            hrGradeCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(data.getValue().getGradeLevel()));
            hrGpaCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getCurrentGPA()));
            hrTierCol.setCellValueFactory(data -> {
                Double gpa = data.getValue().getCurrentGPA();
                String tier = gpa == null ? "Unknown" :
                        gpa >= 3.75 ? "High Honors" :
                        gpa >= 3.5 ? "Honors" : "Honorable Mention";
                return new javafx.beans.property.SimpleStringProperty(tier);
            });
        }
    }

    // ========================================================================
    // DATA LOADING
    // ========================================================================

    private void loadAllData() {
        setStatus("Loading student analytics...");

        CompletableFuture.runAsync(() -> {
            try {
                AnalyticsFilterDTO filter = AnalyticsFilterDTO.builder()
                        .campusId(selectedCampusId)
                        .build();

                currentAnalytics = studentAnalyticsService.getStudentAnalytics(filter);

                Platform.runLater(() -> {
                    updateEnrollmentTab();
                    updateDemographicsTab();
                    updateSpecialNeedsTab();
                    updateAtRiskTab();
                    updateGPATab();
                    updateLastUpdated();
                    setStatus("Data loaded successfully");
                });

            } catch (Exception e) {
                log.error("Error loading student analytics: {}", e.getMessage(), e);
                Platform.runLater(() -> setStatus("Error loading data: " + e.getMessage()));
            }
        });
    }

    private void updateEnrollmentTab() {
        if (currentAnalytics == null) return;

        EnrollmentTrendDTO enrollment = currentAnalytics.getEnrollmentTrends();
        if (enrollment != null) {
            totalEnrollmentValue.setText(String.valueOf(enrollment.getTotalEnrollment()));
            projectedValue.setText(String.valueOf(enrollment.getProjectedEnrollment()));

            double changePercent = enrollment.getYearOverYearChange();
            String changeText = String.format("%+.1f%% vs. last year", changePercent);
            enrollmentChangeLabel.setText(changeText);

            // Update enrollment by grade chart
            updateEnrollmentByGradeChart(enrollment.getGradeBreakdown());
        }

        // New students - would need actual tracking
        newStudentsValue.setText("--");

        // Capacity - would need building capacity data
        capacityValue.setText("--");
    }

    private void updateEnrollmentByGradeChart(List<EnrollmentTrendDTO.GradeBreakdown> gradeData) {
        if (enrollmentByGradeChart == null || gradeData == null) return;

        enrollmentByGradeChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Students");

        for (EnrollmentTrendDTO.GradeBreakdown grade : gradeData) {
            series.getData().add(new XYChart.Data<>(grade.getGradeLevel(), grade.getCount()));
        }

        enrollmentByGradeChart.getData().add(series);
    }

    private void updateDemographicsTab() {
        if (currentAnalytics == null || currentAnalytics.getDemographics() == null) return;

        DemographicsBreakdownDTO demographics = currentAnalytics.getDemographics();

        // Update pie charts
        updatePieChart(genderChart, demographics.getGenderDistribution());
        updatePieChart(ethnicityChart, demographics.getEthnicityDistribution());
        updatePieChart(raceChart, demographics.getRaceDistribution());
        updatePieChart(languageChart, demographics.getLanguageDistribution());
    }

    private void updatePieChart(PieChart chart, Map<String, Long> data) {
        if (chart == null || data == null) return;

        chart.getData().clear();

        long total = data.values().stream().mapToLong(Long::longValue).sum();

        for (Map.Entry<String, Long> entry : data.entrySet()) {
            double percentage = total > 0 ? entry.getValue() * 100.0 / total : 0;
            String label = String.format("%s (%.1f%%)", entry.getKey(), percentage);
            chart.getData().add(new PieChart.Data(label, entry.getValue()));
        }
    }

    private void updateSpecialNeedsTab() {
        if (currentAnalytics == null || currentAnalytics.getSpecialNeeds() == null) return;

        DemographicsBreakdownDTO.SpecialNeedsBreakdown specialNeeds = currentAnalytics.getSpecialNeeds();
        long total = currentAnalytics.getTotalStudents();

        // Update metric cards
        iepCountValue.setText(String.valueOf(specialNeeds.getIepCount()));
        iepPercentLabel.setText(formatPercent(specialNeeds.getIepCount(), total) + " of total");

        plan504CountValue.setText(String.valueOf(specialNeeds.getPlan504Count()));
        plan504PercentLabel.setText(formatPercent(specialNeeds.getPlan504Count(), total) + " of total");

        giftedCountValue.setText(String.valueOf(specialNeeds.getGiftedCount()));
        giftedPercentLabel.setText(formatPercent(specialNeeds.getGiftedCount(), total) + " of total");

        ellCountValue.setText(String.valueOf(specialNeeds.getEllCount()));
        ellPercentLabel.setText(formatPercent(specialNeeds.getEllCount(), total) + " of total");

        // Update pie chart
        if (specialNeedsChart != null) {
            specialNeedsChart.getData().clear();
            specialNeedsChart.getData().add(new PieChart.Data("IEP", specialNeeds.getIepCount()));
            specialNeedsChart.getData().add(new PieChart.Data("504 Plan", specialNeeds.getPlan504Count()));
            specialNeedsChart.getData().add(new PieChart.Data("Gifted", specialNeeds.getGiftedCount()));
            specialNeedsChart.getData().add(new PieChart.Data("ELL", specialNeeds.getEllCount()));
        }
    }

    private void updateAtRiskTab() {
        if (currentAnalytics == null || currentAnalytics.getAtRiskSummary() == null) return;

        Map<String, Object> atRisk = currentAnalytics.getAtRiskSummary();
        long total = currentAnalytics.getTotalStudents();

        // Update metric cards
        criticalRiskValue.setText(String.valueOf(atRisk.get("criticalRisk")));
        highRiskValue.setText(String.valueOf(atRisk.get("highRisk")));
        moderateRiskValue.setText(String.valueOf(atRisk.get("moderateRisk")));
        totalAtRiskValue.setText(String.valueOf(atRisk.get("totalAtRisk")));

        Object percentAtRisk = atRisk.get("percentAtRisk");
        if (percentAtRisk instanceof Number) {
            atRiskPercentLabel.setText(String.format("%.1f%% of total", ((Number) percentAtRisk).doubleValue()));
        }

        // Update risk level pie chart
        if (riskLevelChart != null) {
            riskLevelChart.getData().clear();
            riskLevelChart.getData().add(new PieChart.Data("Critical",
                    ((Number) atRisk.get("criticalRisk")).intValue()));
            riskLevelChart.getData().add(new PieChart.Data("High",
                    ((Number) atRisk.get("highRisk")).intValue()));
            riskLevelChart.getData().add(new PieChart.Data("Moderate",
                    ((Number) atRisk.get("moderateRisk")).intValue()));
        }

        // Load at-risk students table
        loadAtRiskStudentsTable();
    }

    private void loadAtRiskStudentsTable() {
        if (atRiskTable == null) return;

        CompletableFuture.runAsync(() -> {
            try {
                List<Student> atRiskStudents = studentAnalyticsService.getAtRiskStudentsByGPA(selectedCampusId, 2.5);
                Platform.runLater(() -> {
                    atRiskTable.getItems().clear();
                    atRiskTable.getItems().addAll(atRiskStudents);
                });
            } catch (Exception e) {
                log.error("Error loading at-risk students: {}", e.getMessage());
            }
        });
    }

    private void updateGPATab() {
        if (currentAnalytics == null) return;

        // Update average GPA
        avgGpaValue.setText(String.format("%.2f", currentAnalytics.getOverallAverageGPA()));

        // Update honor roll stats
        Map<String, Object> honorRoll = currentAnalytics.getHonorRollSummary();
        if (honorRoll != null) {
            highHonorsValue.setText(String.valueOf(honorRoll.get("highHonors")));
            honorsValue.setText(String.valueOf(honorRoll.get("honors")));
            honorableMentionValue.setText(String.valueOf(honorRoll.get("honorableMention")));
        }

        // Update GPA distribution chart
        updateGPADistributionChart();

        // Update average GPA by grade chart
        updateAvgGPAByGradeChart();

        // Load honor roll table
        loadHonorRollTable();
    }

    private void updateGPADistributionChart() {
        if (gpaDistributionChart == null || currentAnalytics.getGpaDistribution() == null) return;

        gpaDistributionChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Students");

        Map<String, Long> distribution = currentAnalytics.getGpaDistribution();
        for (Map.Entry<String, Long> entry : distribution.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        gpaDistributionChart.getData().add(series);
    }

    private void updateAvgGPAByGradeChart() {
        if (avgGpaByGradeChart == null || currentAnalytics.getAvgGpaByGrade() == null) return;

        avgGpaByGradeChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Average GPA");

        Map<String, Double> avgByGrade = currentAnalytics.getAvgGpaByGrade();
        for (Map.Entry<String, Double> entry : avgByGrade.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        avgGpaByGradeChart.getData().add(series);
    }

    private void loadHonorRollTable() {
        if (honorRollTable == null) return;

        CompletableFuture.runAsync(() -> {
            try {
                List<Student> honorRollStudents = studentAnalyticsService.getHonorRollStudents(selectedCampusId, 3.25);
                Platform.runLater(() -> {
                    honorRollTable.getItems().clear();
                    honorRollTable.getItems().addAll(honorRollStudents);
                });
            } catch (Exception e) {
                log.error("Error loading honor roll students: {}", e.getMessage());
            }
        });
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    private String formatPercent(long count, long total) {
        if (total == 0) return "0%";
        return String.format("%.1f%%", count * 100.0 / total);
    }

    private void setStatus(String message) {
        Platform.runLater(() -> {
            if (statusLabel != null) {
                statusLabel.setText(message);
            }
        });
    }

    private void updateLastUpdated() {
        if (lastUpdatedLabel != null) {
            lastUpdatedLabel.setText("Last updated: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")));
        }
    }

    // ========================================================================
    // EVENT HANDLERS
    // ========================================================================

    @FXML
    private void handleBackToHub() {
        try {
            // Navigate back to analytics hub
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/analytics/AnalyticsHub.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Node hub = loader.load();

            // Replace current view with hub
            if (analyticsTabs.getScene() != null && analyticsTabs.getScene().getRoot() != null) {
                StackPane parent = (StackPane) analyticsTabs.getParent().getParent();
                if (parent != null) {
                    parent.getChildren().clear();
                    parent.getChildren().add(hub);
                }
            }
        } catch (Exception e) {
            log.error("Error navigating back to hub: {}", e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadAllData();
    }

    @FXML
    private void handleExportPDF() {
        if (currentAnalytics == null) {
            setStatus("No data to export");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Student Analytics Report");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("student-analytics-report.pdf");

        File file = fileChooser.showSaveDialog(analyticsTabs.getScene().getWindow());
        if (file != null) {
            setStatus("Exporting PDF...");
            CompletableFuture.runAsync(() -> {
                try {
                    String campusName = campusFilter.getValue() != null ?
                            campusFilter.getValue().getName() : null;
                    byte[] pdfData = analyticsExportService.exportStudentAnalyticsPdf(
                            currentAnalytics, campusName);
                    analyticsExportService.writeToFile(pdfData, file);
                    Platform.runLater(() -> setStatus("PDF exported successfully: " + file.getName()));
                } catch (Exception e) {
                    log.error("Error exporting PDF: {}", e.getMessage(), e);
                    Platform.runLater(() -> setStatus("Error exporting PDF: " + e.getMessage()));
                }
            });
        }
    }

    @FXML
    private void handleExportExcel() {
        if (currentAnalytics == null) {
            setStatus("No data to export");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Student Analytics Data");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser.setInitialFileName("student-analytics-data.xlsx");

        File file = fileChooser.showSaveDialog(analyticsTabs.getScene().getWindow());
        if (file != null) {
            setStatus("Exporting Excel...");
            CompletableFuture.runAsync(() -> {
                try {
                    String campusName = campusFilter.getValue() != null ?
                            campusFilter.getValue().getName() : null;
                    byte[] excelData = analyticsExportService.exportStudentAnalyticsExcel(
                            currentAnalytics, campusName);
                    analyticsExportService.writeToFile(excelData, file);
                    Platform.runLater(() -> setStatus("Excel exported successfully: " + file.getName()));
                } catch (Exception e) {
                    log.error("Error exporting Excel: {}", e.getMessage(), e);
                    Platform.runLater(() -> setStatus("Error exporting Excel: " + e.getMessage()));
                }
            });
        }
    }

    @FXML
    private void handleExportCSV() {
        if (currentAnalytics == null) {
            setStatus("No data to export");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Student Analytics Data");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("student-analytics-data.csv");

        File file = fileChooser.showSaveDialog(analyticsTabs.getScene().getWindow());
        if (file != null) {
            setStatus("Exporting CSV...");
            CompletableFuture.runAsync(() -> {
                try {
                    String campusName = campusFilter.getValue() != null ?
                            campusFilter.getValue().getName() : null;
                    byte[] csvData = analyticsExportService.exportStudentAnalyticsCsv(
                            currentAnalytics, campusName);
                    analyticsExportService.writeToFile(csvData, file);
                    Platform.runLater(() -> setStatus("CSV exported successfully: " + file.getName()));
                } catch (Exception e) {
                    log.error("Error exporting CSV: {}", e.getMessage(), e);
                    Platform.runLater(() -> setStatus("Error exporting CSV: " + e.getMessage()));
                }
            });
        }
    }

    @FXML
    private void handleViewAllSpecialNeeds() {
        // Navigate to students view filtered by special needs
        setStatus("Viewing all special needs students...");
    }

    @FXML
    private void handleExportAtRiskList() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export At-Risk Students List");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser.setInitialFileName("at-risk-students.xlsx");

        File file = fileChooser.showSaveDialog(analyticsTabs.getScene().getWindow());
        if (file != null) {
            setStatus("Exporting at-risk students list...");
            CompletableFuture.runAsync(() -> {
                try {
                    List<Student> atRiskStudents = studentAnalyticsService.getAtRiskStudentsByGPA(
                            selectedCampusId, 2.5);
                    byte[] excelData = analyticsExportService.exportAtRiskStudentsExcel(atRiskStudents);
                    analyticsExportService.writeToFile(excelData, file);
                    Platform.runLater(() -> setStatus("At-risk list exported: " +
                            atRiskStudents.size() + " students to " + file.getName()));
                } catch (Exception e) {
                    log.error("Error exporting at-risk list: {}", e.getMessage(), e);
                    Platform.runLater(() -> setStatus("Error exporting at-risk list: " + e.getMessage()));
                }
            });
        }
    }

    @FXML
    private void handleExportHonorRoll() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Honor Roll List");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser.setInitialFileName("honor-roll-students.xlsx");

        File file = fileChooser.showSaveDialog(analyticsTabs.getScene().getWindow());
        if (file != null) {
            setStatus("Exporting honor roll list...");
            CompletableFuture.runAsync(() -> {
                try {
                    List<Student> honorRollStudents = studentAnalyticsService.getHonorRollStudents(
                            selectedCampusId, 3.25);
                    byte[] excelData = analyticsExportService.exportHonorRollExcel(honorRollStudents);
                    analyticsExportService.writeToFile(excelData, file);
                    Platform.runLater(() -> setStatus("Honor roll exported: " +
                            honorRollStudents.size() + " students to " + file.getName()));
                } catch (Exception e) {
                    log.error("Error exporting honor roll: {}", e.getMessage(), e);
                    Platform.runLater(() -> setStatus("Error exporting honor roll: " + e.getMessage()));
                }
            });
        }
    }
}
