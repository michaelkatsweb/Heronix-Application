package com.heronix.ui.controller.analytics;

import com.heronix.dto.analytics.*;
import com.heronix.model.domain.Campus;
import com.heronix.repository.CampusRepository;
import com.heronix.service.analytics.AttendanceAnalyticsService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Attendance Analytics Controller
 *
 * Controller for the Attendance Analytics view with 5 tabs:
 * - Overview
 * - Chronic Absenteeism
 * - By Dimension
 * - Tardy Patterns
 * - Equity Analysis
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 59 - Comprehensive Analytics Module
 */
@Slf4j
@Component
public class AttendanceAnalyticsController {

    // ========================================================================
    // FXML COMPONENTS - Filters
    // ========================================================================

    @FXML private ComboBox<Campus> campusFilter;
    @FXML private ComboBox<String> dateRangeFilter;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TabPane analyticsTabs;

    // ========================================================================
    // FXML COMPONENTS - Overview Tab
    // ========================================================================

    @FXML private Label overallRateValue;
    @FXML private Label overallRateTrend;
    @FXML private Label todayRateValue;
    @FXML private Label chronicAbsentValue;
    @FXML private Label chronicAbsentPercent;
    @FXML private Label totalTardiesValue;
    @FXML private Label peakTardyPeriod;
    @FXML private LineChart<String, Number> dailyTrendChart;
    @FXML private PieChart statusBreakdownChart;

    // ========================================================================
    // FXML COMPONENTS - Chronic Absenteeism Tab
    // ========================================================================

    @FXML private Label chronicCountValue;
    @FXML private Label severeCountValue;
    @FXML private Label criticalCountValue;
    @FXML private Label chronicPercentValue;
    @FXML private BarChart<String, Number> chronicThresholdChart;
    @FXML private BarChart<String, Number> chronicByGradeChart;
    @FXML private ComboBox<String> chronicThresholdFilter;
    @FXML private TableView<Map<String, Object>> chronicAbsentTable;
    @FXML private TableColumn<Map<String, Object>, String> caStudentIdCol;
    @FXML private TableColumn<Map<String, Object>, String> caStudentNameCol;
    @FXML private TableColumn<Map<String, Object>, String> caGradeCol;
    @FXML private TableColumn<Map<String, Object>, Double> caAttendanceRateCol;
    @FXML private TableColumn<Map<String, Object>, Integer> caAbsencesCol;
    @FXML private TableColumn<Map<String, Object>, String> caStatusCol;

    // ========================================================================
    // FXML COMPONENTS - By Dimension Tab
    // ========================================================================

    @FXML private ToggleGroup dimensionGroup;
    @FXML private RadioButton byGradeRadio;
    @FXML private RadioButton byCampusRadio;
    @FXML private RadioButton byTeacherRadio;
    @FXML private RadioButton byCourseRadio;
    @FXML private Label dimensionChartTitle;
    @FXML private BarChart<String, Number> dimensionChart;
    @FXML private CategoryAxis dimensionXAxis;
    @FXML private Label dimensionTableTitle;
    @FXML private TableView<?> dimensionTable;

    // ========================================================================
    // FXML COMPONENTS - Tardy Patterns Tab
    // ========================================================================

    @FXML private Label tardyTotalValue;
    @FXML private Label peakPeriodValue;
    @FXML private Label peakPeriodCount;
    @FXML private Label frequentTardyValue;
    @FXML private Label dailyAvgTardyValue;
    @FXML private BarChart<String, Number> tardyByPeriodChart;
    @FXML private BarChart<String, Number> tardyByDayChart;
    @FXML private Spinner<Integer> minTardiesSpinner;
    @FXML private TableView<Map<String, Object>> frequentTardyTable;

    // ========================================================================
    // FXML COMPONENTS - Equity Analysis Tab
    // ========================================================================

    @FXML private HBox disparityAlertBanner;
    @FXML private Label disparityAlertText;
    @FXML private Label equityOverallValue;
    @FXML private Label highestGroupValue;
    @FXML private Label highestGroupName;
    @FXML private Label lowestGroupValue;
    @FXML private Label lowestGroupName;
    @FXML private Label equityGapValue;
    @FXML private BarChart<String, Number> ethnicityAttendanceChart;
    @FXML private BarChart<String, Number> genderAttendanceChart;
    @FXML private TableView<?> equityTable;

    // ========================================================================
    // FXML COMPONENTS - Status Bar
    // ========================================================================

    @FXML private Label statusLabel;
    @FXML private Label lastUpdatedLabel;

    // ========================================================================
    // DEPENDENCIES
    // ========================================================================

    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("attendanceAnalyticsServiceV2")
    private AttendanceAnalyticsService attendanceAnalyticsService;

    @Autowired(required = false)
    private CampusRepository campusRepository;

    @Autowired
    private ApplicationContext springContext;

    // ========================================================================
    // STATE
    // ========================================================================

    private Long selectedCampusId = null;
    private LocalDate startDate;
    private LocalDate endDate;
    private AttendanceAnalyticsDTO currentAnalytics;

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    @FXML
    public void initialize() {
        log.info("Initializing Attendance Analytics Controller...");

        // Set default dates
        endDate = LocalDate.now();
        startDate = endDate.minusDays(30);

        setupFilters();
        setupTableColumns();
        setupDimensionRadioButtons();

        // Load initial data
        Platform.runLater(this::loadAllData);

        log.info("Attendance Analytics Controller initialized");
    }

    private void setupFilters() {
        // Campus filter
        if (campusRepository != null) {
            try {
                List<Campus> campuses = campusRepository.findAll();
                campusFilter.getItems().add(null);
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

        // Date range filter
        dateRangeFilter.getItems().addAll(
                "Last 7 Days", "Last 30 Days", "Last 90 Days",
                "This Month", "Last Month", "This Year", "Custom"
        );
        dateRangeFilter.setValue("Last 30 Days");
        dateRangeFilter.setOnAction(e -> {
            String range = dateRangeFilter.getValue();
            updateDateRange(range);
            loadAllData();
        });

        // Date pickers
        startDatePicker.setValue(startDate);
        endDatePicker.setValue(endDate);
        startDatePicker.setOnAction(e -> {
            startDate = startDatePicker.getValue();
            loadAllData();
        });
        endDatePicker.setOnAction(e -> {
            endDate = endDatePicker.getValue();
            loadAllData();
        });

        // Chronic threshold filter
        chronicThresholdFilter.getItems().addAll("All", "< 90%", "< 85%", "< 80%");
        chronicThresholdFilter.setValue("All");

        // Min tardies spinner
        if (minTardiesSpinner != null) {
            minTardiesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 5));
        }
    }

    private void updateDateRange(String range) {
        endDate = LocalDate.now();
        switch (range) {
            case "Last 7 Days":
                startDate = endDate.minusDays(7);
                break;
            case "Last 30 Days":
                startDate = endDate.minusDays(30);
                break;
            case "Last 90 Days":
                startDate = endDate.minusDays(90);
                break;
            case "This Month":
                startDate = endDate.withDayOfMonth(1);
                break;
            case "Last Month":
                startDate = endDate.minusMonths(1).withDayOfMonth(1);
                endDate = startDate.plusMonths(1).minusDays(1);
                break;
            case "This Year":
                startDate = endDate.withDayOfYear(1);
                break;
            default:
                // Custom - use date pickers
                break;
        }
        startDatePicker.setValue(startDate);
        endDatePicker.setValue(endDate);
    }

    private void setupTableColumns() {
        // Chronic absent table columns
        if (caStudentIdCol != null) {
            caStudentIdCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            String.valueOf(data.getValue().get("studentId"))));
            caStudentNameCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            String.valueOf(data.getValue().get("studentName"))));
            caAttendanceRateCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleObjectProperty<>(
                            (Double) data.getValue().get("attendanceRate")));
            caAbsencesCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleObjectProperty<>(
                            ((Number) data.getValue().get("absenceCount")).intValue()));
        }
    }

    private void setupDimensionRadioButtons() {
        if (dimensionGroup != null) {
            dimensionGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    updateDimensionView();
                }
            });
        }
    }

    // ========================================================================
    // DATA LOADING
    // ========================================================================

    private void loadAllData() {
        setStatus("Loading attendance analytics...");

        CompletableFuture.runAsync(() -> {
            try {
                AnalyticsFilterDTO filter = AnalyticsFilterDTO.builder()
                        .campusId(selectedCampusId)
                        .startDate(startDate)
                        .endDate(endDate)
                        .build();

                currentAnalytics = attendanceAnalyticsService.getAttendanceAnalytics(filter);

                Platform.runLater(() -> {
                    updateOverviewTab();
                    updateChronicAbsenteeismTab();
                    updateDimensionView();
                    updateTardyPatternsTab();
                    updateEquityTab();
                    updateLastUpdated();
                    setStatus("Data loaded successfully");
                });

            } catch (Exception e) {
                log.error("Error loading attendance analytics: {}", e.getMessage(), e);
                Platform.runLater(() -> setStatus("Error loading data: " + e.getMessage()));
            }
        });
    }

    private void updateOverviewTab() {
        if (currentAnalytics == null) return;

        // Overall rate
        overallRateValue.setText(String.format("%.1f%%", currentAnalytics.getOverallRate()));

        // Today's rate
        Double todayRate = attendanceAnalyticsService.getTodayAttendanceRate(selectedCampusId);
        todayRateValue.setText(String.format("%.1f%%", todayRate));

        // Chronic absenteeism
        AttendanceAnalyticsDTO.ChronicAbsenteeismSummary chronic = currentAnalytics.getChronicAbsenteeism();
        if (chronic != null) {
            chronicAbsentValue.setText(String.valueOf(chronic.getChronicCount()));
            chronicAbsentPercent.setText(String.format("%.1f%% of students", chronic.getPercentChronic()));
        }

        // Tardies
        AttendanceAnalyticsDTO.TardyPatterns tardy = currentAnalytics.getTardyPatterns();
        if (tardy != null) {
            totalTardiesValue.setText(String.valueOf(tardy.getTotalTardies()));
            peakTardyPeriod.setText("peak period: " + tardy.getPeakPeriod());
        }

        // Update daily trend chart
        updateDailyTrendChart();

        // Update status breakdown
        updateStatusBreakdownChart();
    }

    private void updateDailyTrendChart() {
        if (dailyTrendChart == null || currentAnalytics.getDailyAttendance() == null) return;

        dailyTrendChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Attendance Rate");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
        for (AttendanceAnalyticsDTO.DailyAttendance day : currentAnalytics.getDailyAttendance()) {
            series.getData().add(new XYChart.Data<>(
                    day.getDate().format(formatter),
                    day.getAttendanceRate()
            ));
        }

        dailyTrendChart.getData().add(series);
    }

    private void updateStatusBreakdownChart() {
        if (statusBreakdownChart == null || currentAnalytics.getDailyAttendance() == null) return;

        statusBreakdownChart.getData().clear();

        // Aggregate totals
        int totalPresent = 0;
        int totalAbsent = 0;
        int totalTardy = 0;

        for (AttendanceAnalyticsDTO.DailyAttendance day : currentAnalytics.getDailyAttendance()) {
            totalPresent += day.getPresentCount();
            totalAbsent += day.getAbsentCount();
            totalTardy += day.getTardyCount();
        }

        statusBreakdownChart.getData().add(new PieChart.Data("Present", totalPresent));
        statusBreakdownChart.getData().add(new PieChart.Data("Absent", totalAbsent));
        statusBreakdownChart.getData().add(new PieChart.Data("Tardy", totalTardy));
    }

    private void updateChronicAbsenteeismTab() {
        if (currentAnalytics == null || currentAnalytics.getChronicAbsenteeism() == null) return;

        AttendanceAnalyticsDTO.ChronicAbsenteeismSummary chronic = currentAnalytics.getChronicAbsenteeism();

        chronicCountValue.setText(String.valueOf(chronic.getChronicCount()));
        severeCountValue.setText(String.valueOf(chronic.getSevereCount()));
        criticalCountValue.setText(String.valueOf(chronic.getCriticalCount()));
        chronicPercentValue.setText(String.format("%.1f%%", chronic.getPercentChronic()));

        // Update threshold chart
        updateChronicThresholdChart(chronic);

        // Load chronic absent students
        loadChronicAbsentStudents();
    }

    private void updateChronicThresholdChart(AttendanceAnalyticsDTO.ChronicAbsenteeismSummary chronic) {
        if (chronicThresholdChart == null) return;

        chronicThresholdChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("< 90%", chronic.getChronicCount()));
        series.getData().add(new XYChart.Data<>("< 85%", chronic.getSevereCount()));
        series.getData().add(new XYChart.Data<>("< 80%", chronic.getCriticalCount()));

        chronicThresholdChart.getData().add(series);
    }

    private void loadChronicAbsentStudents() {
        if (chronicAbsentTable == null) return;

        CompletableFuture.runAsync(() -> {
            try {
                List<Map<String, Object>> students = attendanceAnalyticsService.getChronicallyAbsentStudents(
                        selectedCampusId, startDate, endDate);
                Platform.runLater(() -> {
                    chronicAbsentTable.getItems().clear();
                    chronicAbsentTable.getItems().addAll(students);
                });
            } catch (Exception e) {
                log.error("Error loading chronically absent students: {}", e.getMessage());
            }
        });
    }

    private void updateDimensionView() {
        if (currentAnalytics == null) return;

        String dimension = "Grade Level";
        Map<String, Double> data = currentAnalytics.getAttendanceByGrade();

        if (byGradeRadio != null && byGradeRadio.isSelected()) {
            dimension = "Grade Level";
            data = currentAnalytics.getAttendanceByGrade();
        } else if (byCampusRadio != null && byCampusRadio.isSelected()) {
            dimension = "Campus";
            data = attendanceAnalyticsService.getAttendanceByCampus(startDate, endDate);
        }

        dimensionChartTitle.setText("Attendance Rate by " + dimension);
        dimensionTableTitle.setText(dimension + " Details");
        if (dimensionXAxis != null) {
            dimensionXAxis.setLabel(dimension);
        }

        updateDimensionChart(data);
    }

    private void updateDimensionChart(Map<String, Double> data) {
        if (dimensionChart == null || data == null) return;

        dimensionChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Attendance Rate");

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        dimensionChart.getData().add(series);
    }

    private void updateTardyPatternsTab() {
        if (currentAnalytics == null || currentAnalytics.getTardyPatterns() == null) return;

        AttendanceAnalyticsDTO.TardyPatterns tardy = currentAnalytics.getTardyPatterns();

        tardyTotalValue.setText(String.valueOf(tardy.getTotalTardies()));
        peakPeriodValue.setText("Period " + tardy.getPeakPeriod());

        if (tardy.getTardiesByPeriod() != null && tardy.getPeakPeriod() != null) {
            Long peakCount = tardy.getTardiesByPeriod().get(tardy.getPeakPeriod());
            peakPeriodCount.setText(peakCount + " tardies");
        }

        // Update tardy by period chart
        updateTardyByPeriodChart(tardy.getTardiesByPeriod());

        // Load frequent tardy students
        loadFrequentTardyStudents();
    }

    private void updateTardyByPeriodChart(Map<Integer, Long> tardiesByPeriod) {
        if (tardyByPeriodChart == null || tardiesByPeriod == null) return;

        tardyByPeriodChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Tardies");

        for (Map.Entry<Integer, Long> entry : tardiesByPeriod.entrySet()) {
            series.getData().add(new XYChart.Data<>("Period " + entry.getKey(), entry.getValue()));
        }

        tardyByPeriodChart.getData().add(series);
    }

    private void loadFrequentTardyStudents() {
        if (frequentTardyTable == null) return;

        int minTardies = minTardiesSpinner != null ? minTardiesSpinner.getValue() : 5;

        CompletableFuture.runAsync(() -> {
            try {
                List<Map<String, Object>> students = attendanceAnalyticsService.getFrequentTardyStudents(
                        selectedCampusId, startDate, endDate, minTardies);
                Platform.runLater(() -> {
                    frequentTardyTable.getItems().clear();
                    frequentTardyTable.getItems().addAll(students);
                    frequentTardyValue.setText(String.valueOf(students.size()));
                });
            } catch (Exception e) {
                log.error("Error loading frequent tardy students: {}", e.getMessage());
            }
        });
    }

    private void updateEquityTab() {
        if (currentAnalytics == null || currentAnalytics.getEquityAnalysis() == null) return;

        AttendanceAnalyticsDTO.EquityAnalysis equity = currentAnalytics.getEquityAnalysis();

        equityOverallValue.setText(String.format("%.1f%%", equity.getOverallAverage()));

        // Find highest and lowest groups
        Map<String, Double> byEthnicity = equity.getAttendanceByEthnicity();
        if (byEthnicity != null && !byEthnicity.isEmpty()) {
            String highest = null;
            String lowest = null;
            double highestRate = 0;
            double lowestRate = 100;

            for (Map.Entry<String, Double> entry : byEthnicity.entrySet()) {
                if (entry.getValue() > highestRate) {
                    highestRate = entry.getValue();
                    highest = entry.getKey();
                }
                if (entry.getValue() < lowestRate) {
                    lowestRate = entry.getValue();
                    lowest = entry.getKey();
                }
            }

            highestGroupValue.setText(String.format("%.1f%%", highestRate));
            highestGroupName.setText(highest);
            lowestGroupValue.setText(String.format("%.1f%%", lowestRate));
            lowestGroupName.setText(lowest);
            equityGapValue.setText(String.format("%.1f%%", highestRate - lowestRate));

            // Update ethnicity chart
            updateEthnicityAttendanceChart(byEthnicity);
        }

        // Show disparity alerts
        if (equity.getDisparityAlerts() != null && !equity.getDisparityAlerts().isEmpty()) {
            disparityAlertBanner.setVisible(true);
            disparityAlertBanner.setManaged(true);
            disparityAlertText.setText(String.join("; ", equity.getDisparityAlerts()));
        } else {
            disparityAlertBanner.setVisible(false);
            disparityAlertBanner.setManaged(false);
        }
    }

    private void updateEthnicityAttendanceChart(Map<String, Double> data) {
        if (ethnicityAttendanceChart == null || data == null) return;

        ethnicityAttendanceChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Attendance Rate");

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        ethnicityAttendanceChart.getData().add(series);
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/analytics/AnalyticsHub.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Node hub = loader.load();

            if (analyticsTabs.getScene() != null) {
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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Attendance Analytics Report");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("attendance-analytics-report.pdf");

        File file = fileChooser.showSaveDialog(analyticsTabs.getScene().getWindow());
        if (file != null) {
            exportAttendanceDataToFile(file, "Attendance Analytics Report (PDF format not supported - exported as CSV)");
        }
    }

    @FXML
    private void handleExportExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Attendance Analytics Data");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("attendance-analytics-data.csv");

        File file = fileChooser.showSaveDialog(analyticsTabs.getScene().getWindow());
        if (file != null) {
            exportAttendanceDataToFile(file, "Attendance Analytics Data");
        }
    }

    @FXML
    private void handleExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Attendance Analytics Data");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("attendance-analytics-data.csv");

        File file = fileChooser.showSaveDialog(analyticsTabs.getScene().getWindow());
        if (file != null) {
            exportAttendanceDataToFile(file, "Attendance Analytics Data");
        }
    }

    @FXML
    private void handleExportChronicList() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Chronic Absenteeism List");
        fileChooser.setInitialFileName("chronic_absenteeism_list.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(analyticsTabs.getScene().getWindow());
        if (file != null) {
            exportTableToFile(file, chronicAbsentTable, "Chronic Absenteeism List");
        }
    }

    @FXML
    private void handleExportTardyList() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Frequent Tardy List");
        fileChooser.setInitialFileName("frequent_tardy_list.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(analyticsTabs.getScene().getWindow());
        if (file != null) {
            exportTableToFile(file, frequentTardyTable, "Frequent Tardy List");
        }
    }

    private void exportAttendanceDataToFile(File file, String title) {
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(
                new java.io.FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8))) {
            pw.write('\ufeff');
            pw.println(title);
            pw.println("Generated: " + java.time.LocalDate.now());
            pw.println();
            if (chronicAbsentTable != null && chronicAbsentTable.getItems() != null) {
                pw.println("Chronic Absenteeism");
                for (var row : chronicAbsentTable.getItems()) {
                    pw.println(row.values().stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(",")));
                }
            }
            pw.println();
            if (frequentTardyTable != null && frequentTardyTable.getItems() != null) {
                pw.println("Frequent Tardy");
                for (var row : frequentTardyTable.getItems()) {
                    pw.println(row.values().stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(",")));
                }
            }
            setStatus("Exported to " + file.getName());
        } catch (Exception e) {
            setStatus("Export failed: " + e.getMessage());
        }
    }

    private void exportTableToFile(File file, TableView<Map<String, Object>> table, String title) {
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(
                new java.io.FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8))) {
            pw.write('\ufeff');
            pw.println(title);
            pw.println("Generated: " + java.time.LocalDate.now());
            pw.println();
            if (table != null && table.getItems() != null) {
                for (var row : table.getItems()) {
                    pw.println(row.values().stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(",")));
                }
            }
            setStatus("Exported " + (table != null ? table.getItems().size() : 0) + " records to " + file.getName());
        } catch (Exception e) {
            setStatus("Export failed: " + e.getMessage());
        }
    }
}
