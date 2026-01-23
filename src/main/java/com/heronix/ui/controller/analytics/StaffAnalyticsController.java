package com.heronix.ui.controller.analytics;

import com.heronix.dto.analytics.*;
import com.heronix.service.analytics.StaffAnalyticsService;
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
 * Controller for Staff Analytics view
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 59 - Comprehensive Analytics Module
 */
@Slf4j
@Controller
public class StaffAnalyticsController implements Initializable {

    @Autowired
    private StaffAnalyticsService staffService;

    @Autowired
    private ApplicationContext springContext;

    @Autowired(required = false)
    private MainControllerV2 mainController;

    // Tab Pane
    @FXML private TabPane mainTabPane;
    @FXML private Button backButton;

    // Overview Tab - Metric Cards
    @FXML private Label totalStaffValue;
    @FXML private Label certifiedValue;
    @FXML private Label certRateTrend;
    @FXML private Label expiringValue;
    @FXML private Label avgExperienceValue;

    // Charts
    @FXML private BarChart<String, Number> departmentChart;
    @FXML private PieChart certStatusChart;
    @FXML private BarChart<String, Number> experienceChart;
    @FXML private BarChart<String, Number> expByDeptChart;
    @FXML private BarChart<String, Number> workloadChart;

    // Certifications Tab
    @FXML private Label expiredValue;
    @FXML private Label expiring30Value;
    @FXML private Label expiring60Value;
    @FXML private Label expiring90Value;
    @FXML private TableView<Map<String, Object>> expiringCertsTable;
    @FXML private TableColumn<Map<String, Object>, String> certEmpIdCol;
    @FXML private TableColumn<Map<String, Object>, String> certNameCol;
    @FXML private TableColumn<Map<String, Object>, String> certDeptCol;
    @FXML private TableColumn<Map<String, Object>, String> certExpDateCol;
    @FXML private TableColumn<Map<String, Object>, Integer> certDaysCol;

    // Workload Tab
    @FXML private Label avgClassesValue;
    @FXML private Label normalWorkloadValue;
    @FXML private Label overloadedValue;
    @FXML private TableView<Map<String, Object>> workloadTable;
    @FXML private TableColumn<Map<String, Object>, String> wlTeacherCol;
    @FXML private TableColumn<Map<String, Object>, String> wlDeptCol;
    @FXML private TableColumn<Map<String, Object>, Integer> wlCoursesCol;
    @FXML private TableColumn<Map<String, Object>, String> wlStatusCol;

    // Status
    @FXML private Label statusLabel;
    @FXML private Label lastUpdatedLabel;

    private Long currentCampusId = null;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("Initializing Staff Analytics Controller");

        setupCharts();
        setupTables();
        loadAnalyticsData();
    }

    private void setupCharts() {
        if (departmentChart != null) {
            departmentChart.setAnimated(true);
            departmentChart.setLegendVisible(false);
        }
        if (certStatusChart != null) {
            certStatusChart.setLegendSide(javafx.geometry.Side.BOTTOM);
        }
        if (experienceChart != null) {
            experienceChart.setAnimated(true);
        }
        if (workloadChart != null) {
            workloadChart.setAnimated(true);
        }
    }

    private void setupTables() {
        // Expiring Certifications Table
        if (expiringCertsTable != null) {
            certEmpIdCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().get("employeeId") != null ? data.getValue().get("employeeId").toString() : ""));
            certNameCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().get("name") != null ? data.getValue().get("name").toString() : ""));
            certDeptCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().get("department") != null ? data.getValue().get("department").toString() : ""));
            certExpDateCol.setCellValueFactory(data -> {
                Object date = data.getValue().get("expirationDate");
                String dateStr = date != null ? date.toString() : "";
                return new javafx.beans.property.SimpleStringProperty(dateStr);
            });
            certDaysCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleObjectProperty<>(
                            data.getValue().get("daysLeft") != null ? ((Number) data.getValue().get("daysLeft")).intValue() : 0));
        }

        // Workload Table
        if (workloadTable != null) {
            wlTeacherCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().get("teacherName") != null ? data.getValue().get("teacherName").toString() : ""));
            wlDeptCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().get("department") != null ? data.getValue().get("department").toString() : ""));
            wlCoursesCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleObjectProperty<>(
                            data.getValue().get("courseCount") != null ? ((Number) data.getValue().get("courseCount")).intValue() : 0));
            wlStatusCol.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().get("status") != null ? data.getValue().get("status").toString() : ""));
        }
    }

    private void loadAnalyticsData() {
        updateStatus("Loading staff analytics data...");

        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // Get total staff
                    Long totalStaff = staffService.getTotalActiveStaff(currentCampusId);

                    // Get certification summary
                    Map<String, Object> certSummary = staffService.getCertificationSummary(currentCampusId);

                    // Get experience distribution
                    Map<String, Long> expDist = staffService.getExperienceDistribution(currentCampusId);

                    // Get average experience
                    Double avgExp = staffService.getAverageExperience(currentCampusId);

                    // Get department breakdown
                    Map<String, Long> deptBreakdown = staffService.getDepartmentBreakdown(currentCampusId);

                    // Get workload data
                    List<Map<String, Object>> workload = staffService.getWorkloadDistribution(currentCampusId);
                    Map<String, Object> workloadSummary = staffService.getWorkloadSummary(currentCampusId);

                    // Get expiring certifications
                    List<Map<String, Object>> expiringCerts = staffService.getExpiringCertifications(currentCampusId, 90);

                    Platform.runLater(() -> {
                        updateOverviewMetrics(totalStaff, certSummary, avgExp);
                        updateDepartmentChart(deptBreakdown);
                        updateCertificationChart(certSummary);
                        updateExperienceChart(expDist);
                        updateCertificationTiers(certSummary);
                        updateExpiringCertsTable(expiringCerts);
                        updateWorkloadData(workloadSummary, workload);
                        updateStatus("Data loaded successfully");
                        updateLastUpdated();
                    });

                } catch (Exception e) {
                    log.error("Error loading staff analytics data", e);
                    Platform.runLater(() -> updateStatus("Error loading data: " + e.getMessage()));
                }
                return null;
            }
        };

        executorService.submit(loadTask);
    }

    private void updateOverviewMetrics(Long totalStaff, Map<String, Object> certSummary, Double avgExp) {
        if (totalStaffValue != null) {
            totalStaffValue.setText(totalStaff != null ? totalStaff.toString() : "0");
        }

        if (certifiedValue != null && certSummary != null) {
            Object certified = certSummary.get("totalCertified");
            certifiedValue.setText(certified != null ? certified.toString() : "0");
        }

        if (certRateTrend != null && certSummary != null) {
            Object rate = certSummary.get("certificationRate");
            certRateTrend.setText(rate != null ? String.format("%.1f%% compliance", ((Number) rate).doubleValue()) : "0% compliance");
        }

        if (expiringValue != null && certSummary != null) {
            Object expiring = certSummary.get("totalExpiringSoon");
            expiringValue.setText(expiring != null ? expiring.toString() : "0");
        }

        if (avgExperienceValue != null) {
            avgExperienceValue.setText(avgExp != null ? String.format("%.1f", avgExp) : "0");
        }
    }

    private void updateDepartmentChart(Map<String, Long> breakdown) {
        if (departmentChart == null || breakdown == null) return;

        departmentChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Staff");

        for (Map.Entry<String, Long> entry : breakdown.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        departmentChart.getData().add(series);

        // Apply color
        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #0078D4;");
                }
            }
        });
    }

    private void updateCertificationChart(Map<String, Object> certSummary) {
        if (certStatusChart == null || certSummary == null) return;

        certStatusChart.getData().clear();

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        Long certified = certSummary.get("totalCertified") != null ? ((Number) certSummary.get("totalCertified")).longValue() : 0L;
        Long expiring = certSummary.get("totalExpiringSoon") != null ? ((Number) certSummary.get("totalExpiringSoon")).longValue() : 0L;
        Long expired = certSummary.get("expired") != null ? ((Number) certSummary.get("expired")).longValue() : 0L;

        // Calculate valid (certified - expiring)
        Long valid = certified - expiring;

        if (valid > 0) pieData.add(new PieChart.Data("Valid (" + valid + ")", valid));
        if (expiring > 0) pieData.add(new PieChart.Data("Expiring Soon (" + expiring + ")", expiring));
        if (expired > 0) pieData.add(new PieChart.Data("Expired (" + expired + ")", expired));

        certStatusChart.setData(pieData);
    }

    private void updateExperienceChart(Map<String, Long> expDist) {
        if (experienceChart == null || expDist == null) return;

        experienceChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Staff");

        for (Map.Entry<String, Long> entry : expDist.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        experienceChart.getData().add(series);

        // Apply gradient colors
        Platform.runLater(() -> {
            String[] colors = {"#06B6D4", "#0078D4", "#8B5CF6", "#10B981", "#F59E0B"};
            int i = 0;
            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null && i < colors.length) {
                    data.getNode().setStyle("-fx-bar-fill: " + colors[i] + ";");
                }
                i++;
            }
        });
    }

    private void updateCertificationTiers(Map<String, Object> certSummary) {
        if (certSummary == null) return;

        if (expiredValue != null) {
            Object val = certSummary.get("expired");
            expiredValue.setText(val != null ? val.toString() : "0");
        }
        if (expiring30Value != null) {
            Object val = certSummary.get("expiringIn30Days");
            expiring30Value.setText(val != null ? val.toString() : "0");
        }
        if (expiring60Value != null) {
            Object val = certSummary.get("expiringIn60Days");
            expiring60Value.setText(val != null ? val.toString() : "0");
        }
        if (expiring90Value != null) {
            Object val = certSummary.get("expiringIn90Days");
            expiring90Value.setText(val != null ? val.toString() : "0");
        }
    }

    private void updateExpiringCertsTable(List<Map<String, Object>> certs) {
        if (expiringCertsTable == null || certs == null) return;

        ObservableList<Map<String, Object>> data = FXCollections.observableArrayList(certs);
        expiringCertsTable.setItems(data);
    }

    private void updateWorkloadData(Map<String, Object> summary, List<Map<String, Object>> workload) {
        // Update workload metrics
        if (avgClassesValue != null && summary != null) {
            Object avg = summary.get("avgCourses");
            avgClassesValue.setText(avg != null ? String.format("%.1f", ((Number) avg).doubleValue()) : "0");
        }

        if (overloadedValue != null && summary != null) {
            Object overloaded = summary.get("overloaded");
            overloadedValue.setText(overloaded != null ? overloaded.toString() : "0");
        }

        // Update workload chart
        if (workloadChart != null && workload != null) {
            workloadChart.getData().clear();

            // Group by course count
            Map<Integer, Long> courseCounts = new TreeMap<>();
            for (Map<String, Object> w : workload) {
                Integer count = w.get("courseCount") != null ? ((Number) w.get("courseCount")).intValue() : 0;
                courseCounts.merge(count, 1L, Long::sum);
            }

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Teachers");

            for (Map.Entry<Integer, Long> entry : courseCounts.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey() + " classes", entry.getValue()));
            }

            workloadChart.getData().add(series);
        }

        // Update workload table
        if (workloadTable != null && workload != null) {
            // Add status to each workload entry
            List<Map<String, Object>> workloadWithStatus = new ArrayList<>();
            for (Map<String, Object> w : workload) {
                Map<String, Object> copy = new HashMap<>(w);
                int count = w.get("courseCount") != null ? ((Number) w.get("courseCount")).intValue() : 0;
                String status = count > 5 ? "Overloaded" : (count < 2 ? "Underutilized" : "Normal");
                copy.put("status", status);
                workloadWithStatus.add(copy);
            }

            ObservableList<Map<String, Object>> data = FXCollections.observableArrayList(workloadWithStatus);
            workloadTable.setItems(data);
        }
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
            String now = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
            lastUpdatedLabel.setText("Last updated: " + now);
        }
    }

    public void cleanup() {
        executorService.shutdown();
    }
}
