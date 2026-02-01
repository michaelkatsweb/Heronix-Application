package com.heronix.ui.controller.analytics;

import com.heronix.dto.analytics.*;
import com.heronix.service.analytics.AcademicPerformanceAnalyticsService;
import com.heronix.ui.controller.MainControllerV2;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
 * Controller for Academic Performance Analytics view
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 59 - Comprehensive Analytics Module
 */
@Slf4j
@Controller
public class AcademicPerformanceAnalyticsController implements Initializable {

    @Autowired
    private AcademicPerformanceAnalyticsService academicService;

    @Autowired
    private ApplicationContext springContext;

    @Autowired(required = false)
    private MainControllerV2 mainController;

    // Tab Pane
    @FXML private TabPane mainTabPane;
    @FXML private Button backButton;

    // Grade Distribution Tab - Metric Cards
    @FXML private Label avgGpaValue;
    @FXML private Label avgGpaTrend;
    @FXML private Label passRateValue;
    @FXML private Label honorRollValue;
    @FXML private Label atRiskValue;

    // Charts
    @FXML private BarChart<String, Number> gpaDistributionChart;
    @FXML private PieChart letterGradeChart;
    @FXML private BarChart<String, Number> gpaByGradeChart;
    @FXML private LineChart<String, Number> gpaTrendChart;
    @FXML private PieChart honorRollChart;
    @FXML private BarChart<String, Number> atRiskChart;

    // Honor Roll Tab
    @FXML private Label highHonorsValue;
    @FXML private Label honorsValue;
    @FXML private Label honorableMentionValue;
    @FXML private TableView<Map<String, Object>> honorRollTable;
    @FXML private TableColumn<Map<String, Object>, String> hrStudentIdCol;
    @FXML private TableColumn<Map<String, Object>, String> hrStudentNameCol;
    @FXML private TableColumn<Map<String, Object>, String> hrGradeLevelCol;
    @FXML private TableColumn<Map<String, Object>, Double> hrGpaCol;
    @FXML private TableColumn<Map<String, Object>, String> hrTierCol;

    // At-Risk Tab
    @FXML private Label criticalRiskValue;
    @FXML private Label highRiskValue;
    @FXML private Label moderateRiskValue;
    @FXML private TableView<Map<String, Object>> atRiskTable;
    @FXML private TableColumn<Map<String, Object>, String> arStudentIdCol;
    @FXML private TableColumn<Map<String, Object>, String> arStudentNameCol;
    @FXML private TableColumn<Map<String, Object>, String> arGradeLevelCol;
    @FXML private TableColumn<Map<String, Object>, Double> arGpaCol;
    @FXML private TableColumn<Map<String, Object>, String> arRiskLevelCol;

    // Status
    @FXML private Label statusLabel;
    @FXML private Label lastUpdatedLabel;

    private Long currentCampusId = null;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("Initializing Academic Performance Analytics Controller");

        setupCharts();
        setupTables();
        loadAnalyticsData();
    }

    private void setupCharts() {
        // Setup chart properties
        if (gpaDistributionChart != null) {
            gpaDistributionChart.setAnimated(true);
            gpaDistributionChart.setLegendVisible(false);
        }
        if (letterGradeChart != null) {
            letterGradeChart.setLegendSide(javafx.geometry.Side.BOTTOM);
        }
        if (gpaByGradeChart != null) {
            gpaByGradeChart.setAnimated(true);
        }
        if (gpaTrendChart != null) {
            gpaTrendChart.setAnimated(true);
        }
    }

    private void setupTables() {
        // Honor Roll Table
        if (honorRollTable != null) {
            hrStudentIdCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().get("studentId") != null ? data.getValue().get("studentId").toString() : ""));
            hrStudentNameCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().get("name") != null ? data.getValue().get("name").toString() : ""));
            hrGradeLevelCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().get("gradeLevel") != null ? data.getValue().get("gradeLevel").toString() : ""));
            hrGpaCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleObjectProperty<>(
                            data.getValue().get("gpa") != null ? (Double) data.getValue().get("gpa") : 0.0));
            hrTierCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().get("tier") != null ? data.getValue().get("tier").toString() : ""));
        }

        // At-Risk Table
        if (atRiskTable != null) {
            arStudentIdCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().get("studentId") != null ? data.getValue().get("studentId").toString() : ""));
            arStudentNameCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().get("name") != null ? data.getValue().get("name").toString() : ""));
            arGradeLevelCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().get("gradeLevel") != null ? data.getValue().get("gradeLevel").toString() : ""));
            arGpaCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleObjectProperty<>(
                            data.getValue().get("gpa") != null ? (Double) data.getValue().get("gpa") : 0.0));
            arRiskLevelCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().get("riskLevel") != null ? data.getValue().get("riskLevel").toString() : ""));
        }
    }

    private void loadAnalyticsData() {
        updateStatus("Loading academic performance data...");

        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // Get overall average GPA
                    Double avgGpa = academicService.getOverallAverageGPA(currentCampusId);

                    // Get GPA distribution
                    Map<String, Long> gpaDistribution = academicService.getGPADistribution(currentCampusId);

                    // Get average GPA by grade
                    Map<String, Double> gpaByGrade = academicService.getAverageGPAByGrade(currentCampusId);

                    // Get honor roll summary
                    Map<String, Object> honorRoll = academicService.getHonorRollSummary(currentCampusId);

                    // Get at-risk summary
                    Map<String, Object> atRisk = academicService.getAtRiskSummary(currentCampusId);

                    Platform.runLater(() -> {
                        updateMetricCards(avgGpa, honorRoll, atRisk);
                        updateGpaDistributionChart(gpaDistribution);
                        updateGpaByGradeChart(gpaByGrade);
                        updateHonorRollData(honorRoll);
                        updateAtRiskData(atRisk);
                        updateStatus("Data loaded successfully");
                        updateLastUpdated();
                    });

                } catch (Exception e) {
                    log.error("Error loading academic performance data", e);
                    Platform.runLater(() -> updateStatus("Error loading data: " + e.getMessage()));
                }
                return null;
            }
        };

        executorService.submit(loadTask);
    }

    private void updateMetricCards(Double avgGpa, Map<String, Object> honorRoll, Map<String, Object> atRisk) {
        if (avgGpaValue != null) {
            avgGpaValue.setText(String.format("%.2f", avgGpa != null ? avgGpa : 0.0));
        }

        if (honorRollValue != null && honorRoll != null) {
            Object total = honorRoll.get("totalHonorRoll");
            honorRollValue.setText(total != null ? total.toString() : "0");
        }

        if (atRiskValue != null && atRisk != null) {
            Object total = atRisk.get("totalAtRisk");
            atRiskValue.setText(total != null ? total.toString() : "0");
        }
    }

    private void updateGpaDistributionChart(Map<String, Long> distribution) {
        if (gpaDistributionChart == null || distribution == null) return;

        gpaDistributionChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Students");

        for (Map.Entry<String, Long> entry : distribution.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        gpaDistributionChart.getData().add(series);

        // Apply colors based on GPA range
        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> data : series.getData()) {
                String color = getColorForGpaRange(data.getXValue());
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: " + color + ";");
                }
            }
        });
    }

    private void updateGpaByGradeChart(Map<String, Double> gpaByGrade) {
        if (gpaByGradeChart == null || gpaByGrade == null) return;

        gpaByGradeChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Average GPA");

        for (Map.Entry<String, Double> entry : gpaByGrade.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        gpaByGradeChart.getData().add(series);

        // Apply info color
        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #06B6D4;");
                }
            }
        });
    }

    private void updateHonorRollData(Map<String, Object> honorRoll) {
        if (honorRoll == null) return;

        // Update tier values
        if (highHonorsValue != null) {
            Object val = honorRoll.get("highHonors");
            highHonorsValue.setText(val != null ? val.toString() : "0");
        }
        if (honorsValue != null) {
            Object val = honorRoll.get("honors");
            honorsValue.setText(val != null ? val.toString() : "0");
        }
        if (honorableMentionValue != null) {
            Object val = honorRoll.get("honorableMention");
            honorableMentionValue.setText(val != null ? val.toString() : "0");
        }

        // Update pie chart
        if (honorRollChart != null) {
            honorRollChart.getData().clear();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

            int highHonors = honorRoll.get("highHonors") != null ? ((Number) honorRoll.get("highHonors")).intValue() : 0;
            int honors = honorRoll.get("honors") != null ? ((Number) honorRoll.get("honors")).intValue() : 0;
            int honorable = honorRoll.get("honorableMention") != null ? ((Number) honorRoll.get("honorableMention")).intValue() : 0;

            if (highHonors > 0) pieData.add(new PieChart.Data("High Honors (" + highHonors + ")", highHonors));
            if (honors > 0) pieData.add(new PieChart.Data("Honors (" + honors + ")", honors));
            if (honorable > 0) pieData.add(new PieChart.Data("Honorable Mention (" + honorable + ")", honorable));

            honorRollChart.setData(pieData);
        }
    }

    private void updateAtRiskData(Map<String, Object> atRisk) {
        if (atRisk == null) return;

        // Update tier values
        if (criticalRiskValue != null) {
            Object val = atRisk.get("criticalRisk");
            criticalRiskValue.setText(val != null ? val.toString() : "0");
        }
        if (highRiskValue != null) {
            Object val = atRisk.get("highRisk");
            highRiskValue.setText(val != null ? val.toString() : "0");
        }
        if (moderateRiskValue != null) {
            Object val = atRisk.get("moderateRisk");
            moderateRiskValue.setText(val != null ? val.toString() : "0");
        }

        // Update bar chart
        if (atRiskChart != null) {
            atRiskChart.getData().clear();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Students");

            int critical = atRisk.get("criticalRisk") != null ? ((Number) atRisk.get("criticalRisk")).intValue() : 0;
            int high = atRisk.get("highRisk") != null ? ((Number) atRisk.get("highRisk")).intValue() : 0;
            int moderate = atRisk.get("moderateRisk") != null ? ((Number) atRisk.get("moderateRisk")).intValue() : 0;

            series.getData().add(new XYChart.Data<>("Critical Risk", critical));
            series.getData().add(new XYChart.Data<>("High Risk", high));
            series.getData().add(new XYChart.Data<>("Moderate Risk", moderate));

            atRiskChart.getData().add(series);

            // Apply colors
            Platform.runLater(() -> {
                String[] colors = {"#EF4444", "#F59E0B", "#06B6D4"};
                int i = 0;
                for (XYChart.Data<String, Number> data : series.getData()) {
                    if (data.getNode() != null && i < colors.length) {
                        data.getNode().setStyle("-fx-bar-fill: " + colors[i] + ";");
                    }
                    i++;
                }
            });
        }
    }

    private String getColorForGpaRange(String range) {
        return switch (range) {
            case "3.5-4.0" -> "#10B981"; // Green
            case "3.0-3.49" -> "#06B6D4"; // Cyan
            case "2.5-2.99" -> "#F59E0B"; // Amber
            case "2.0-2.49" -> "#F97316"; // Orange
            case "1.0-1.99" -> "#EF4444"; // Red
            case "Below 1.0" -> "#DC2626"; // Dark Red
            default -> "#9CA3AF"; // Gray
        };
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
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Export Academic Performance Report");
        fileChooser.setInitialFileName("academic_performance_report.csv");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        java.io.File file = fileChooser.showSaveDialog(statusLabel.getScene().getWindow());
        if (file != null) {
            try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(
                    new java.io.FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8))) {
                pw.write('\ufeff');
                pw.println("Academic Performance Analytics Report");
                pw.println("Generated: " + java.time.LocalDate.now());
                pw.println();
                if (honorRollTable != null && honorRollTable.getItems() != null) {
                    pw.println("Honor Roll Students");
                    for (var row : honorRollTable.getItems()) {
                        pw.println(row.values().stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(",")));
                    }
                }
                pw.println();
                if (atRiskTable != null && atRiskTable.getItems() != null) {
                    pw.println("At-Risk Students");
                    for (var row : atRiskTable.getItems()) {
                        pw.println(row.values().stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(",")));
                    }
                }
                updateStatus("Report exported to " + file.getName());
            } catch (Exception e) {
                log.error("Export failed", e);
                updateStatus("Export failed: " + e.getMessage());
            }
        }
    }

    private void updateStatus(String message) {
        if (statusLabel != null) {
            Platform.runLater(() -> statusLabel.setText(message));
        }
    }

    private void updateLastUpdated() {
        if (lastUpdatedLabel != null) {
            String now = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
            lastUpdatedLabel.setText("Last updated: " + now);
        }
    }

    public void cleanup() {
        executorService.shutdown();
    }
}
