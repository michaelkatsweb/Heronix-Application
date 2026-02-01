package com.heronix.ui.controller.analytics;

import com.heronix.dto.analytics.AnalyticsFilterDTO;
import com.heronix.dto.analytics.AnalyticsSummaryDTO;
import com.heronix.model.domain.Campus;
import com.heronix.service.analytics.AnalyticsHubService;
import com.heronix.service.export.AnalyticsExportService;
import com.heronix.ui.controller.MainControllerV2;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the Analytics Hub dashboard
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 59 - Analytics Module
 */
@Slf4j
@Component
public class AnalyticsHubController {

    @Autowired
    private AnalyticsHubService analyticsHubService;

    @Autowired
    private ApplicationContext springContext;

    @Autowired(required = false)
    private MainControllerV2 mainController;

    @Autowired
    private AnalyticsExportService analyticsExportService;

    // Filters
    @FXML private ComboBox<Campus> campusFilter;
    @FXML private ComboBox<String> yearFilter;
    @FXML private Button refreshBtn;
    @FXML private Button exportSummaryBtn;

    // Quick Stats Labels
    @FXML private Label totalStudentsLabel;
    @FXML private Label studentsTrendLabel;
    @FXML private Label attendanceRateLabel;
    @FXML private Label attendanceTrendLabel;
    @FXML private Label averageGPALabel;
    @FXML private Label gpaTrendLabel;
    @FXML private Label todayIncidentsLabel;
    @FXML private Label incidentsTrendLabel;
    @FXML private Label totalStaffLabel;
    @FXML private Label certComplianceLabel;
    @FXML private Label healthScoreLabel;
    @FXML private Label healthStatusLabel;

    // Tile Metrics
    @FXML private Label tileStudentCount;
    @FXML private Label tileAttendanceRate;
    @FXML private Label tileAverageGPA;
    @FXML private Label tileCertCompliance;
    @FXML private Label tileTodayIncidents;
    @FXML private Label tileCampusCount;

    // Alert Counts
    @FXML private Label chronicAbsentCount;
    @FXML private Label certExpiringCount;
    @FXML private Label atRiskCount;

    // Status Bar
    @FXML private Label lastUpdatedLabel;
    @FXML private Label dataSourceLabel;

    // Navigation Tiles
    @FXML private VBox studentAnalyticsTile;
    @FXML private VBox attendanceAnalyticsTile;
    @FXML private VBox academicAnalyticsTile;
    @FXML private VBox staffAnalyticsTile;
    @FXML private VBox behaviorAnalyticsTile;
    @FXML private VBox districtOverviewTile;

    @FXML private GridPane quickStatsGrid;

    // Current data for export
    private AnalyticsSummaryDTO currentSummary;

    @FXML
    public void initialize() {
        log.info("Initializing AnalyticsHubController");

        setupFilters();
        setupTileHoverEffects();
        loadData();

        log.info("AnalyticsHubController initialized successfully");
    }

    private void setupFilters() {
        // Load campuses
        List<Campus> campuses = analyticsHubService.getAllCampuses();

        // Add "All Campuses" option
        Campus allCampuses = new Campus();
        allCampuses.setName("All Campuses");
        allCampuses.setId(null);

        campusFilter.setItems(FXCollections.observableArrayList());
        campusFilter.getItems().add(allCampuses);
        campusFilter.getItems().addAll(campuses);
        campusFilter.getSelectionModel().selectFirst();

        // Custom cell factory for campus display
        campusFilter.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Campus item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        campusFilter.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Campus item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        // Load academic years
        int currentYear = LocalDate.now().getYear();
        yearFilter.setItems(FXCollections.observableArrayList(
                (currentYear) + "-" + (currentYear + 1),
                (currentYear - 1) + "-" + currentYear,
                (currentYear - 2) + "-" + (currentYear - 1)
        ));
        yearFilter.getSelectionModel().selectFirst();

        // Add change listeners
        campusFilter.setOnAction(e -> loadData());
        yearFilter.setOnAction(e -> loadData());
    }

    private void setupTileHoverEffects() {
        // Hover effects are handled via CSS
        // Additional click feedback can be added here if needed
    }

    private void loadData() {
        log.info("Loading analytics hub data");

        // Build filter
        Campus selectedCampus = campusFilter.getValue();
        Long campusId = selectedCampus != null ? selectedCampus.getId() : null;

        AnalyticsFilterDTO filter = AnalyticsFilterDTO.builder()
                .campusId(campusId)
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now())
                .build();

        // Load data asynchronously
        new Thread(() -> {
            try {
                AnalyticsSummaryDTO summary = analyticsHubService.getHubSummary(filter);
                List<Campus> allCampuses = analyticsHubService.getAllCampuses();

                Platform.runLater(() -> {
                    currentSummary = summary;
                    updateUI(summary, allCampuses.size());
                });
            } catch (Exception e) {
                log.error("Error loading analytics data", e);
                Platform.runLater(() -> showError("Failed to load analytics data: " + e.getMessage()));
            }
        }).start();
    }

    private void updateUI(AnalyticsSummaryDTO summary, int campusCount) {
        // Quick Stats
        totalStudentsLabel.setText(formatNumber(summary.getTotalStudents()));
        attendanceRateLabel.setText(String.format("%.1f%%", summary.getAttendanceRate()));
        averageGPALabel.setText(String.format("%.2f", summary.getAverageGPA()));
        todayIncidentsLabel.setText(formatNumber(summary.getTotalIncidentsToday()));
        totalStaffLabel.setText(formatNumber(summary.getTotalStaff()));

        // Health Score
        Integer healthScore = summary.getOverallHealthScore();
        healthScoreLabel.setText(healthScore.toString());
        healthStatusLabel.setText(summary.getHealthStatus());
        updateHealthScoreStyle(healthScore);

        // Certification compliance
        certComplianceLabel.setText(String.format("%.0f%% Certified", summary.getCertificationComplianceRate()));

        // Tile Metrics
        tileStudentCount.setText(formatNumber(summary.getTotalStudents()));
        tileAttendanceRate.setText(String.format("%.1f%%", summary.getAttendanceRate()));
        tileAverageGPA.setText(String.format("%.2f", summary.getAverageGPA()));
        tileCertCompliance.setText(String.format("%.0f%%", summary.getCertificationComplianceRate()));
        tileTodayIncidents.setText(formatNumber(summary.getTotalIncidentsToday()));
        tileCampusCount.setText(String.valueOf(campusCount));

        // Alert Counts
        chronicAbsentCount.setText(summary.getChronicAbsenteeismCount() + " students");
        certExpiringCount.setText(summary.getCertificationExpiringSoon() + " staff");
        atRiskCount.setText(summary.getAtRiskStudentsTotal() + " students");

        // Status Bar
        lastUpdatedLabel.setText("Last updated: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")));
        dataSourceLabel.setText("Data as of: " +
                summary.getDataAsOfDate().format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
    }

    private void updateHealthScoreStyle(Integer score) {
        healthScoreLabel.getStyleClass().removeAll("health-excellent", "health-good",
                "health-fair", "health-attention", "health-critical");

        if (score >= 90) {
            healthScoreLabel.getStyleClass().add("health-excellent");
        } else if (score >= 80) {
            healthScoreLabel.getStyleClass().add("health-good");
        } else if (score >= 70) {
            healthScoreLabel.getStyleClass().add("health-fair");
        } else if (score >= 60) {
            healthScoreLabel.getStyleClass().add("health-attention");
        } else {
            healthScoreLabel.getStyleClass().add("health-critical");
        }
    }

    private String formatNumber(Long number) {
        if (number == null) return "0";
        if (number >= 1000) {
            return String.format("%.1fK", number / 1000.0);
        }
        return number.toString();
    }

    // ========================================================================
    // EVENT HANDLERS
    // ========================================================================

    @FXML
    private void handleRefresh() {
        log.info("Refreshing analytics data");
        loadData();
    }

    @FXML
    private void handleExportSummary() {
        if (currentSummary == null) {
            showInfo("No data to export. Please wait for data to load.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Analytics Summary");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser.setInitialFileName("analytics-summary-" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx");

        File file = fileChooser.showSaveDialog(campusFilter.getScene().getWindow());
        if (file != null) {
            log.info("Exporting analytics summary to {}", file.getAbsolutePath());

            new Thread(() -> {
                try {
                    byte[] excelData = analyticsExportService.exportHubSummaryExcel(currentSummary);
                    analyticsExportService.writeToFile(excelData, file);
                    Platform.runLater(() -> showInfo("Analytics summary exported successfully to:\n" + file.getName()));
                } catch (Exception e) {
                    log.error("Error exporting analytics summary", e);
                    Platform.runLater(() -> showError("Failed to export: " + e.getMessage()));
                }
            }).start();
        }
    }

    @FXML
    private void navigateToStudentAnalytics() {
        log.info("Navigating to Student Analytics");
        navigateTo("student-analytics");
    }

    @FXML
    private void navigateToAttendanceAnalytics() {
        log.info("Navigating to Attendance Analytics");
        navigateTo("attendance-analytics");
    }

    @FXML
    private void navigateToAcademicAnalytics() {
        log.info("Navigating to Academic Analytics");
        navigateTo("academic-analytics");
    }

    @FXML
    private void navigateToStaffAnalytics() {
        log.info("Navigating to Staff Analytics");
        navigateTo("staff-analytics");
    }

    @FXML
    private void navigateToBehaviorAnalytics() {
        log.info("Navigating to Behavior Analytics");
        navigateTo("behavior-analytics");
    }

    @FXML
    private void navigateToDistrictOverview() {
        log.info("Navigating to District Overview");
        navigateTo("district-overview");
    }

    private void navigateTo(String viewId) {
        if (mainController != null) {
            mainController.navigateTo(viewId);
        } else {
            showInfo("Navigation to " + viewId + " - Main controller not set");
        }
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
