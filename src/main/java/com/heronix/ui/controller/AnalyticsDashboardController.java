package com.heronix.ui.controller;

import com.heronix.client.DashboardApiService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Analytics Dashboard Controller
 *
 * Displays school-wide attendance analytics and metrics from REST API.
 * Provides interactive charts and real-time attendance monitoring.
 *
 * Features:
 * - Overall attendance metrics (present, absent, tardy, excused)
 * - ADA (Average Daily Attendance) calculation
 * - Attendance trends over time (line chart)
 * - Chronic absenteeism tracking
 * - Date range filtering
 * - Automatic data refresh
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 47 - Dashboard UI Components
 */
@Slf4j
@Component
public class AnalyticsDashboardController {

    // ========================================================================
    // FXML FIELDS - Metric Cards
    // ========================================================================

    @FXML private Label totalStudentsLabel;
    @FXML private Label presentCountLabel;
    @FXML private Label absentCountLabel;
    @FXML private Label tardyCountLabel;
    @FXML private Label excusedCountLabel;
    @FXML private Label attendanceRateLabel;
    @FXML private Label adaLabel;
    @FXML private Label admLabel;
    @FXML private Label chronicAbsenteeismLabel;

    // ========================================================================
    // FXML FIELDS - Charts
    // ========================================================================

    @FXML private LineChart<String, Number> attendanceTrendChart;
    @FXML private BarChart<String, Number> statusBreakdownChart;

    // ========================================================================
    // FXML FIELDS - Controls
    // ========================================================================

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> groupByComboBox;
    @FXML private Button refreshButton;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label errorLabel;
    @FXML private VBox dashboardContent;

    // ========================================================================
    // DEPENDENCIES
    // ========================================================================

    @Autowired
    private DashboardApiService dashboardApiService;

    @Autowired
    private com.heronix.client.WebSocketClientService webSocketClientService;

    // ========================================================================
    // STATE VARIABLES
    // ========================================================================

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd");
    private boolean webSocketConnected = false;

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    @FXML
    public void initialize() {
        log.info("Initializing Analytics Dashboard Controller");

        // Set default date range (last 30 days)
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusDays(30));

        // Set up groupBy options
        groupByComboBox.getItems().addAll("day", "week", "month");
        groupByComboBox.setValue("day");

        // Initialize WebSocket connection
        initializeWebSocket();

        // Load initial data
        Platform.runLater(this::refreshDashboard);
    }

    /**
     * Initialize WebSocket connection for real-time updates
     */
    private void initializeWebSocket() {
        try {
            // Connect to WebSocket
            webSocketClientService.connect();

            // Register dashboard update callback
            webSocketClientService.onDashboardUpdate(data -> {
                String type = (String) data.get("type");
                if ("DASHBOARD_REFRESH".equals(type)) {
                    log.info("Received dashboard refresh request from WebSocket");
                    Platform.runLater(this::refreshDashboard);
                } else if ("METRICS_UPDATE".equals(type)) {
                    log.info("Received metrics update from WebSocket");
                    @SuppressWarnings("unchecked")
                    Map<String, Object> metrics = (Map<String, Object>) data.get("metrics");
                    if (metrics != null) {
                        Platform.runLater(() -> updateMetricCards(metrics, null, null));
                    }
                }
            });

            // Register attendance update callback
            webSocketClientService.onAttendanceUpdate(data -> {
                String type = (String) data.get("type");
                log.debug("Received attendance update: {}", type);
                // Refresh dashboard on any attendance change
                Platform.runLater(this::refreshDashboard);
            });

            // Register notification callback
            webSocketClientService.onNotification(data -> {
                String title = (String) data.get("title");
                String content = (String) data.get("content");
                String level = (String) data.get("level");

                Platform.runLater(() -> {
                    if ("ERROR".equals(level)) {
                        showError(title + ": " + content);
                    } else {
                        log.info("Notification: {} - {}", title, content);
                    }
                });
            });

            webSocketConnected = true;
            log.info("WebSocket initialized successfully");

        } catch (Exception e) {
            log.error("Failed to initialize WebSocket: {}", e.getMessage());
            webSocketConnected = false;
        }
    }

    // ========================================================================
    // ACTION HANDLERS
    // ========================================================================

    /**
     * Refresh dashboard data
     */
    @FXML
    private void handleRefresh() {
        refreshDashboard();
    }

    /**
     * Handle date range change
     */
    @FXML
    private void handleDateRangeChange() {
        refreshDashboard();
    }

    // ========================================================================
    // DATA LOADING
    // ========================================================================

    /**
     * Refresh all dashboard data
     */
    private void refreshDashboard() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null) {
            showError("Please select both start and end dates");
            return;
        }

        if (startDate.isAfter(endDate)) {
            showError("Start date must be before end date");
            return;
        }

        clearError();
        setLoading(true);

        // Load data in background thread
        new Thread(() -> {
            try {
                // Fetch dashboard data
                Map<String, Object> dashboard = dashboardApiService.getAttendanceDashboard(startDate, endDate);
                Map<String, Object> ada = dashboardApiService.calculateADA(startDate, endDate);
                Map<String, Object> trends = dashboardApiService.getAttendanceTrends(
                    startDate, endDate, groupByComboBox.getValue());
                Map<String, Object> chronicAbsenteeism = dashboardApiService.getChronicAbsenteeism(startDate, endDate);

                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    updateMetricCards(dashboard, ada, chronicAbsenteeism);
                    updateCharts(dashboard, trends);
                    setLoading(false);
                });

            } catch (Exception e) {
                log.error("Error refreshing dashboard: {}", e.getMessage(), e);
                Platform.runLater(() -> {
                    setLoading(false);
                    showError("Failed to load dashboard data. Please check your connection.");
                });
            }
        }).start();
    }

    // ========================================================================
    // UI UPDATE METHODS
    // ========================================================================

    /**
     * Update metric cards with data
     */
    private void updateMetricCards(Map<String, Object> dashboard, Map<String, Object> ada,
                                   Map<String, Object> chronicAbsenteeism) {
        if (dashboard != null && !dashboard.isEmpty()) {
            totalStudentsLabel.setText(getValueAsString(dashboard, "totalStudents", "0"));
            presentCountLabel.setText(getValueAsString(dashboard, "presentCount", "0"));
            absentCountLabel.setText(getValueAsString(dashboard, "absentCount", "0"));
            tardyCountLabel.setText(getValueAsString(dashboard, "tardyCount", "0"));
            excusedCountLabel.setText(getValueAsString(dashboard, "excusedCount", "0"));

            // Calculate and display attendance rate
            double attendanceRate = getValueAsDouble(dashboard, "attendanceRate", 0.0);
            attendanceRateLabel.setText(String.format("%.1f%%", attendanceRate));
        }

        if (ada != null && !ada.isEmpty()) {
            double adaValue = getValueAsDouble(ada, "ada", 0.0);
            double admValue = getValueAsDouble(ada, "adm", 0.0);
            adaLabel.setText(String.format("%.2f", adaValue));
            admLabel.setText(String.format("%.2f", admValue));
        }

        if (chronicAbsenteeism != null && !chronicAbsenteeism.isEmpty()) {
            int chronicCount = getValueAsInt(chronicAbsenteeism, "chronicAbsenteeCount", 0);
            chronicAbsenteeismLabel.setText(String.valueOf(chronicCount));
        }
    }

    /**
     * Update charts with data
     */
    private void updateCharts(Map<String, Object> dashboard, Map<String, Object> trends) {
        // Update status breakdown bar chart
        if (dashboard != null && !dashboard.isEmpty()) {
            statusBreakdownChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Attendance Status");

            series.getData().add(new XYChart.Data<>("Present", getValueAsInt(dashboard, "presentCount", 0)));
            series.getData().add(new XYChart.Data<>("Absent", getValueAsInt(dashboard, "absentCount", 0)));
            series.getData().add(new XYChart.Data<>("Tardy", getValueAsInt(dashboard, "tardyCount", 0)));
            series.getData().add(new XYChart.Data<>("Excused", getValueAsInt(dashboard, "excusedCount", 0)));

            statusBreakdownChart.getData().add(series);
        }

        // Update attendance trend line chart
        if (trends != null && !trends.isEmpty()) {
            attendanceTrendChart.getData().clear();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> trendData = (List<Map<String, Object>>) trends.get("trends");

            if (trendData != null && !trendData.isEmpty()) {
                XYChart.Series<String, Number> attendanceSeries = new XYChart.Series<>();
                attendanceSeries.setName("Attendance Rate");

                for (Map<String, Object> dataPoint : trendData) {
                    String date = (String) dataPoint.get("date");
                    double rate = getValueAsDouble(dataPoint, "attendanceRate", 0.0);

                    // Format date for display
                    String formattedDate = formatDateLabel(date);
                    attendanceSeries.getData().add(new XYChart.Data<>(formattedDate, rate));
                }

                attendanceTrendChart.getData().add(attendanceSeries);
            }
        }
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Get value from map as string
     */
    private String getValueAsString(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * Get value from map as integer
     */
    private int getValueAsInt(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    /**
     * Get value from map as double
     */
    private double getValueAsDouble(Map<String, Object> map, String key, double defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }

    /**
     * Format date string for chart labels
     */
    private String formatDateLabel(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            return date.format(DATE_FORMATTER);
        } catch (Exception e) {
            return dateStr;
        }
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /**
     * Clear error message
     */
    private void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    /**
     * Set loading state
     */
    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        loadingIndicator.setManaged(loading);
        refreshButton.setDisable(loading);
        startDatePicker.setDisable(loading);
        endDatePicker.setDisable(loading);
        groupByComboBox.setDisable(loading);
        dashboardContent.setDisable(loading);
    }
}
