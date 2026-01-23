package com.heronix.ui.controller.analytics;

import com.heronix.dto.analytics.*;
import com.heronix.service.analytics.BehaviorAnalyticsService;
import com.heronix.ui.controller.MainControllerV2;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller for Behavior Analytics view
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 59 - Comprehensive Analytics Module
 */
@Slf4j
@Controller
public class BehaviorAnalyticsController implements Initializable {

    @Autowired
    private BehaviorAnalyticsService behaviorService;

    @Autowired
    private ApplicationContext springContext;

    @Autowired(required = false)
    private MainControllerV2 mainController;

    // Tab Pane
    @FXML private TabPane mainTabPane;
    @FXML private Button backButton;

    // Date Filters
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    // Overview Tab - Metric Cards
    @FXML private Label totalIncidentsValue;
    @FXML private Label incidentPeriodLabel;
    @FXML private Label positiveValue;
    @FXML private Label negativeValue;
    @FXML private Label todayValue;

    // Charts
    @FXML private LineChart<String, Number> dailyTrendChart;
    @FXML private PieChart typeDistributionChart;
    @FXML private BarChart<String, Number> categoryChart;
    @FXML private BarChart<String, Number> severityChart;
    @FXML private BarChart<String, Number> locationChart;

    // At-Risk Tab
    @FXML private Label repeatOffendersValue;
    @FXML private Label severeIncidentsValue;
    @FXML private Label uniqueStudentsValue;

    // Tables
    @FXML private TableView<Map<String, Object>> locationTable;
    @FXML private TableColumn<Map<String, Object>, String> locNameCol;
    @FXML private TableColumn<Map<String, Object>, Long> locCountCol;
    @FXML private TableColumn<Map<String, Object>, String> locPercentCol;

    @FXML private TableView<Map<String, Object>> repeatOffendersTable;
    @FXML private TableColumn<Map<String, Object>, String> roStudentIdCol;
    @FXML private TableColumn<Map<String, Object>, String> roNameCol;
    @FXML private TableColumn<Map<String, Object>, Long> roIncidentCountCol;
    @FXML private TableColumn<Map<String, Object>, String> roLastIncidentCol;

    @FXML private TableView<Map<String, Object>> positiveStudentsTable;
    @FXML private TableColumn<Map<String, Object>, String> psStudentIdCol;
    @FXML private TableColumn<Map<String, Object>, String> psNameCol;
    @FXML private TableColumn<Map<String, Object>, Long> psPositiveCountCol;

    // Status
    @FXML private Label statusLabel;
    @FXML private Label lastUpdatedLabel;

    private Long currentCampusId = null;
    private LocalDate startDate;
    private LocalDate endDate;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("Initializing Behavior Analytics Controller");

        // Set default date range (last 30 days)
        endDate = LocalDate.now();
        startDate = endDate.minusDays(30);

        if (startDatePicker != null) {
            startDatePicker.setValue(startDate);
        }
        if (endDatePicker != null) {
            endDatePicker.setValue(endDate);
        }

        setupCharts();
        setupTables();
        loadAnalyticsData();
    }

    private void setupCharts() {
        if (dailyTrendChart != null) {
            dailyTrendChart.setAnimated(true);
            dailyTrendChart.setLegendVisible(false);
        }
        if (typeDistributionChart != null) {
            typeDistributionChart.setLegendSide(javafx.geometry.Side.BOTTOM);
        }
        if (categoryChart != null) {
            categoryChart.setAnimated(true);
        }
        if (severityChart != null) {
            severityChart.setAnimated(true);
        }
        if (locationChart != null) {
            locationChart.setAnimated(true);
        }
    }

    private void setupTables() {
        // Location Table
        if (locationTable != null) {
            locNameCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().get("location") != null ? data.getValue().get("location").toString() : ""));
            locCountCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleObjectProperty<>(
                            data.getValue().get("count") != null ? ((Number) data.getValue().get("count")).longValue() : 0L));
            locPercentCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().get("percentage") != null ?
                                    String.format("%.1f%%", ((Number) data.getValue().get("percentage")).doubleValue()) : "0%"));
        }

        // Repeat Offenders Table
        if (repeatOffendersTable != null) {
            roStudentIdCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().get("studentId") != null ? data.getValue().get("studentId").toString() : ""));
            roNameCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().get("studentName") != null ? data.getValue().get("studentName").toString() : ""));
            roIncidentCountCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleObjectProperty<>(
                            data.getValue().get("incidentCount") != null ? ((Number) data.getValue().get("incidentCount")).longValue() : 0L));
            roLastIncidentCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().get("lastIncident") != null ? data.getValue().get("lastIncident").toString() : ""));
        }

        // Positive Students Table
        if (positiveStudentsTable != null) {
            psStudentIdCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().get("studentId") != null ? data.getValue().get("studentId").toString() : ""));
            psNameCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().get("studentName") != null ? data.getValue().get("studentName").toString() : ""));
            psPositiveCountCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleObjectProperty<>(
                            data.getValue().get("positiveCount") != null ? ((Number) data.getValue().get("positiveCount")).longValue() : 0L));
        }
    }

    private void loadAnalyticsData() {
        updateStatus("Loading behavior analytics data...");

        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // Get incident summary
                    Map<String, Object> summary = behaviorService.getIncidentSummary(currentCampusId, startDate, endDate);

                    // Get daily incident counts
                    List<Map<String, Object>> dailyData = behaviorService.getDailyIncidentCounts(currentCampusId, startDate, endDate);

                    // Get category breakdown
                    Map<String, Long> categories = behaviorService.getCategoryBreakdown(currentCampusId, startDate, endDate);

                    // Get type breakdown (positive/negative)
                    Map<String, Long> types = behaviorService.getTypeBreakdown(currentCampusId, startDate, endDate);

                    // Get location breakdown
                    Map<String, Long> locations = behaviorService.getLocationBreakdown(currentCampusId, startDate, endDate);

                    // Get severity breakdown
                    Map<String, Long> severity = behaviorService.getSeverityBreakdown(currentCampusId, startDate, endDate);

                    // Get repeat offenders
                    List<Map<String, Object>> offenders = behaviorService.getRepeatOffenders(currentCampusId, startDate);

                    // Get top positive behavior students
                    List<Map<String, Object>> positiveStudents = behaviorService.getTopPositiveBehaviorStudents(currentCampusId, startDate, endDate);

                    Platform.runLater(() -> {
                        updateOverviewMetrics(summary, types, severity);
                        updateDailyTrendChart(dailyData);
                        updateTypeDistributionChart(types);
                        updateCategoryChart(categories);
                        updateSeverityChart(severity);
                        updateLocationData(locations);
                        updateRepeatOffendersTable(offenders);
                        updatePositiveStudentsTable(positiveStudents);
                        updateStatus("Data loaded successfully");
                        updateLastUpdated();
                    });

                } catch (Exception e) {
                    log.error("Error loading behavior analytics data", e);
                    Platform.runLater(() -> updateStatus("Error loading data: " + e.getMessage()));
                }
                return null;
            }
        };

        executorService.submit(loadTask);
    }

    private void updateOverviewMetrics(Map<String, Object> summary, Map<String, Long> types, Map<String, Long> severity) {
        if (summary == null) return;

        if (totalIncidentsValue != null) {
            Object total = summary.get("totalIncidents");
            totalIncidentsValue.setText(total != null ? total.toString() : "0");
        }

        if (incidentPeriodLabel != null) {
            incidentPeriodLabel.setText(String.format("%s - %s",
                    startDate.format(DateTimeFormatter.ofPattern("MMM d")),
                    endDate.format(DateTimeFormatter.ofPattern("MMM d"))));
        }

        if (positiveValue != null) {
            Object positive = summary.get("positiveIncidents");
            positiveValue.setText(positive != null ? positive.toString() : "0");
        }

        if (negativeValue != null) {
            Object negative = summary.get("negativeIncidents");
            negativeValue.setText(negative != null ? negative.toString() : "0");
        }

        if (todayValue != null) {
            Object today = summary.get("todayIncidents");
            todayValue.setText(today != null ? today.toString() : "0");
        }

        // Update at-risk metrics
        if (uniqueStudentsValue != null) {
            Object unique = summary.get("uniqueStudents");
            uniqueStudentsValue.setText(unique != null ? unique.toString() : "0");
        }

        if (severeIncidentsValue != null && severity != null) {
            Long severe = severity.getOrDefault("Severe", 0L) + severity.getOrDefault("Major", 0L);
            severeIncidentsValue.setText(severe.toString());
        }
    }

    private void updateDailyTrendChart(List<Map<String, Object>> dailyData) {
        if (dailyTrendChart == null || dailyData == null) return;

        dailyTrendChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Incidents");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d");

        for (Map<String, Object> day : dailyData) {
            Object date = day.get("date");
            Object count = day.get("count");

            String dateStr = date != null ? date.toString() : "";
            if (date instanceof LocalDate) {
                dateStr = ((LocalDate) date).format(formatter);
            }

            long countVal = count != null ? ((Number) count).longValue() : 0L;
            series.getData().add(new XYChart.Data<>(dateStr, countVal));
        }

        dailyTrendChart.getData().add(series);
    }

    private void updateTypeDistributionChart(Map<String, Long> types) {
        if (typeDistributionChart == null || types == null) return;

        typeDistributionChart.getData().clear();

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        Long positive = types.getOrDefault("Positive", 0L);
        Long negative = types.getOrDefault("Negative", 0L);

        if (positive > 0) pieData.add(new PieChart.Data("Positive (" + positive + ")", positive));
        if (negative > 0) pieData.add(new PieChart.Data("Negative (" + negative + ")", negative));

        typeDistributionChart.setData(pieData);
    }

    private void updateCategoryChart(Map<String, Long> categories) {
        if (categoryChart == null || categories == null) return;

        categoryChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Incidents");

        for (Map.Entry<String, Long> entry : categories.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        categoryChart.getData().add(series);

        // Apply colors based on category
        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null) {
                    String color = getColorForCategory(data.getXValue());
                    data.getNode().setStyle("-fx-bar-fill: " + color + ";");
                }
            }
        });
    }

    private void updateSeverityChart(Map<String, Long> severity) {
        if (severityChart == null || severity == null) return;

        severityChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Incidents");

        // Order: Minor, Moderate, Major, Severe
        String[] order = {"Minor", "Moderate", "Major", "Severe"};
        for (String sev : order) {
            series.getData().add(new XYChart.Data<>(sev, severity.getOrDefault(sev, 0L)));
        }

        severityChart.getData().add(series);

        // Apply colors
        Platform.runLater(() -> {
            String[] colors = {"#06B6D4", "#F59E0B", "#F97316", "#EF4444"};
            int i = 0;
            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null && i < colors.length) {
                    data.getNode().setStyle("-fx-bar-fill: " + colors[i] + ";");
                }
                i++;
            }
        });
    }

    private void updateLocationData(Map<String, Long> locations) {
        if (locations == null) return;

        // Update chart
        if (locationChart != null) {
            locationChart.getData().clear();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Incidents");

            // Take top 10 locations
            int count = 0;
            for (Map.Entry<String, Long> entry : locations.entrySet()) {
                if (count++ >= 10) break;
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }

            locationChart.getData().add(series);

            // Apply color
            Platform.runLater(() -> {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    if (data.getNode() != null) {
                        data.getNode().setStyle("-fx-bar-fill: #F59E0B;");
                    }
                }
            });
        }

        // Update table
        if (locationTable != null) {
            long total = locations.values().stream().mapToLong(Long::longValue).sum();

            List<Map<String, Object>> tableData = new ArrayList<>();
            for (Map.Entry<String, Long> entry : locations.entrySet()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("location", entry.getKey());
                row.put("count", entry.getValue());
                row.put("percentage", total > 0 ? entry.getValue() * 100.0 / total : 0.0);
                tableData.add(row);
            }

            locationTable.setItems(FXCollections.observableArrayList(tableData));
        }
    }

    private void updateRepeatOffendersTable(List<Map<String, Object>> offenders) {
        if (repeatOffendersTable == null || offenders == null) return;

        repeatOffendersTable.setItems(FXCollections.observableArrayList(offenders));

        if (repeatOffendersValue != null) {
            repeatOffendersValue.setText(String.valueOf(offenders.size()));
        }
    }

    private void updatePositiveStudentsTable(List<Map<String, Object>> students) {
        if (positiveStudentsTable == null || students == null) return;

        positiveStudentsTable.setItems(FXCollections.observableArrayList(students));
    }

    private String getColorForCategory(String category) {
        if (category == null) return "#9CA3AF";

        String upper = category.toUpperCase();
        if (upper.contains("POSITIVE") || upper.contains("RECOGNITION")) {
            return "#10B981"; // Green
        } else if (upper.contains("FIGHT") || upper.contains("BULLY") || upper.contains("HARASS")) {
            return "#EF4444"; // Red
        } else if (upper.contains("TARDY") || upper.contains("DISRUPT")) {
            return "#F59E0B"; // Amber
        } else if (upper.contains("VANDAL") || upper.contains("THEFT")) {
            return "#DC2626"; // Dark Red
        }
        return "#F97316"; // Orange default
    }

    @FXML
    private void handleApplyFilter() {
        if (startDatePicker != null && startDatePicker.getValue() != null) {
            startDate = startDatePicker.getValue();
        }
        if (endDatePicker != null && endDatePicker.getValue() != null) {
            endDate = endDatePicker.getValue();
        }

        loadAnalyticsData();
    }

    @FXML
    private void handleBackToHub() {
        if (mainController != null) {
            mainController.navigateTo("analytics");
        }
    }

    @FXML
    private void handleExportReport() {
        log.info("Export report requested");
        updateStatus("Export functionality coming soon...");
    }

    private void updateStatus(String message) {
        if (statusLabel != null) {
            Platform.runLater(() -> statusLabel.setText(message));
        }
    }

    private void updateLastUpdated() {
        if (lastUpdatedLabel != null) {
            String now = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm"));
            lastUpdatedLabel.setText("Last updated: " + now);
        }
    }

    public void cleanup() {
        executorService.shutdown();
    }
}
