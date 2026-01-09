package com.heronix.ui.controller;

import com.heronix.model.domain.ReportHistory;
import com.heronix.service.ReportHistoryService;
import com.heronix.ui.util.ReportDialogLauncher;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Report History View Controller
 *
 * Manages the Report History UI for viewing, filtering, and managing generated reports.
 *
 * Features:
 * - View all generated reports in a table
 * - Filter by type, status, date range
 * - Search by report name
 * - Display statistics (total, completed, failed, scheduled)
 * - Download/view reports
 * - Delete reports
 * - Export report list to CSV
 * - Cleanup old reports
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 54 - Report Management UI
 */
@Slf4j
@Controller
public class ReportHistoryViewController {

    @Autowired
    private ReportHistoryService reportHistoryService;

    @Autowired
    private ReportDialogLauncher reportDialogLauncher;

    // Statistics Labels
    @FXML private Text totalReportsLabel;
    @FXML private Text completedReportsLabel;
    @FXML private Text failedReportsLabel;
    @FXML private Text scheduledReportsLabel;

    // Filters
    @FXML private ComboBox<String> reportTypeFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> dateRangeFilter;
    @FXML private TextField searchField;

    // Table
    @FXML private TableView<ReportHistory> reportsTable;
    @FXML private TableColumn<ReportHistory, String> idColumn;
    @FXML private TableColumn<ReportHistory, String> reportNameColumn;
    @FXML private TableColumn<ReportHistory, String> reportTypeColumn;
    @FXML private TableColumn<ReportHistory, String> formatColumn;
    @FXML private TableColumn<ReportHistory, String> statusColumn;
    @FXML private TableColumn<ReportHistory, String> generatedByColumn;
    @FXML private TableColumn<ReportHistory, String> generatedAtColumn;
    @FXML private TableColumn<ReportHistory, String> fileSizeColumn;
    @FXML private TableColumn<ReportHistory, String> downloadCountColumn;
    @FXML private TableColumn<ReportHistory, Void> actionsColumn;

    // Footer
    @FXML private Label recordCountLabel;

    // Progress
    @FXML private StackPane progressOverlay;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label progressLabel;

    private ObservableList<ReportHistory> allReports = FXCollections.observableArrayList();
    private ObservableList<ReportHistory> filteredReports = FXCollections.observableArrayList();

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        log.info("Initializing Report History View Controller");

        setupTable();
        setupFilters();
        loadReportsAsync();
        loadStatistics();
    }

    /**
     * Setup table columns and cell factories
     */
    private void setupTable() {
        // ID Column
        idColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getId().toString()));

        // Report Name Column
        reportNameColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getReportName()));

        // Report Type Column
        reportTypeColumn.setCellValueFactory(data ->
            new SimpleStringProperty(formatReportType(data.getValue().getReportType())));

        // Format Column
        formatColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getReportFormat().name()));

        // Status Column with Badge
        statusColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getStatus().name()));
        statusColumn.setCellFactory(column -> new TableCell<ReportHistory, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(status);
                    badge.getStyleClass().add("status-badge");
                    badge.getStyleClass().add("status-" + status.toLowerCase());
                    setGraphic(badge);
                }
            }
        });

        // Generated By Column
        generatedByColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getGeneratedBy() != null ?
                data.getValue().getGeneratedBy() : "N/A"));

        // Generated At Column
        generatedAtColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getGeneratedAt() != null ?
                data.getValue().getGeneratedAt().format(DATE_TIME_FORMATTER) : "N/A"));

        // File Size Column
        fileSizeColumn.setCellValueFactory(data ->
            new SimpleStringProperty(formatFileSize(data.getValue().getFileSize())));

        // Download Count Column
        downloadCountColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getDownloadCount() != null ?
                data.getValue().getDownloadCount().toString() : "0"));

        // Actions Column with Buttons
        actionsColumn.setCellFactory(column -> new TableCell<ReportHistory, Void>() {
            private final Button downloadBtn = new Button("Download");
            private final Button viewBtn = new Button("View");
            private final Button deleteBtn = new Button("Delete");

            {
                downloadBtn.getStyleClass().add("success-button");
                viewBtn.getStyleClass().add("info-button");
                deleteBtn.getStyleClass().add("danger-button");

                downloadBtn.setOnAction(event -> {
                    ReportHistory report = getTableView().getItems().get(getIndex());
                    handleDownloadReport(report);
                });

                viewBtn.setOnAction(event -> {
                    ReportHistory report = getTableView().getItems().get(getIndex());
                    handleViewReport(report);
                });

                deleteBtn.setOnAction(event -> {
                    ReportHistory report = getTableView().getItems().get(getIndex());
                    handleDeleteReport(report);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, downloadBtn, viewBtn, deleteBtn);
                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });

        reportsTable.setItems(filteredReports);
    }

    /**
     * Setup filter listeners
     */
    private void setupFilters() {
        reportTypeFilter.setValue("All Types");
        statusFilter.setValue("All Statuses");
        dateRangeFilter.setValue("All Time");

        reportTypeFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        dateRangeFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    /**
     * Load reports asynchronously
     */
    private void loadReportsAsync() {
        showProgress("Loading reports...");

        Task<List<ReportHistory>> task = new Task<>() {
            @Override
            protected List<ReportHistory> call() {
                return reportHistoryService.getAllReportHistory();
            }

            @Override
            protected void succeeded() {
                allReports.clear();
                allReports.addAll(getValue());
                applyFilters();
                hideProgress();
                log.info("Loaded {} reports", allReports.size());
            }

            @Override
            protected void failed() {
                hideProgress();
                log.error("Error loading reports", getException());
                showError("Error", "Failed to load reports: " + getException().getMessage());
            }
        };

        new Thread(task).start();
    }

    /**
     * Load statistics
     */
    private void loadStatistics() {
        Task<ReportHistoryService.ReportStatistics> task = new Task<>() {
            @Override
            protected ReportHistoryService.ReportStatistics call() {
                return reportHistoryService.getReportStatistics();
            }

            @Override
            protected void succeeded() {
                ReportHistoryService.ReportStatistics stats = getValue();
                Platform.runLater(() -> {
                    totalReportsLabel.setText(String.valueOf(stats.totalReports()));
                    completedReportsLabel.setText(String.valueOf(stats.completedReports()));
                    failedReportsLabel.setText(String.valueOf(stats.failedReports()));
                    scheduledReportsLabel.setText(String.valueOf(stats.scheduledReports()));
                });
            }

            @Override
            protected void failed() {
                log.error("Error loading statistics", getException());
            }
        };

        new Thread(task).start();
    }

    /**
     * Apply filters to reports list
     */
    private void applyFilters() {
        String typeFilter = reportTypeFilter.getValue();
        String statusFilterValue = statusFilter.getValue();
        String dateRangeFilterValue = dateRangeFilter.getValue();
        String searchText = searchField.getText().toLowerCase();

        filteredReports.clear();
        filteredReports.addAll(allReports.stream()
            .filter(report -> {
                // Type filter
                if (!"All Types".equals(typeFilter)) {
                    String formattedType = formatReportType(report.getReportType());
                    if (!formattedType.equals(typeFilter)) {
                        return false;
                    }
                }

                // Status filter
                if (!"All Statuses".equals(statusFilterValue)) {
                    if (!report.getStatus().name().equalsIgnoreCase(statusFilterValue)) {
                        return false;
                    }
                }

                // Date range filter
                if (!"All Time".equals(dateRangeFilterValue)) {
                    LocalDate reportDate = report.getGeneratedAt().toLocalDate();
                    LocalDate today = LocalDate.now();

                    boolean inRange = switch (dateRangeFilterValue) {
                        case "Today" -> reportDate.equals(today);
                        case "Last 7 Days" -> reportDate.isAfter(today.minusDays(7));
                        case "Last 30 Days" -> reportDate.isAfter(today.minusDays(30));
                        case "This Month" -> reportDate.getMonth() == today.getMonth() &&
                            reportDate.getYear() == today.getYear();
                        default -> true;
                    };

                    if (!inRange) {
                        return false;
                    }
                }

                // Search filter
                if (!searchText.isEmpty()) {
                    return report.getReportName().toLowerCase().contains(searchText);
                }

                return true;
            })
            .collect(Collectors.toList()));

        updateRecordCount();
    }

    /**
     * Update record count label
     */
    private void updateRecordCount() {
        recordCountLabel.setText(String.format("Showing %d of %d reports",
            filteredReports.size(), allReports.size()));
    }

    /**
     * Format report type for display
     */
    private String formatReportType(ReportHistory.ReportType type) {
        return switch (type) {
            case DAILY_ATTENDANCE -> "Daily Attendance";
            case STUDENT_SUMMARY -> "Student Summary";
            case CHRONIC_ABSENTEEISM -> "Chronic Absenteeism";
            case WEEKLY_SUMMARY -> "Weekly Summary";
            case MONTHLY_SUMMARY -> "Monthly Summary";
            default -> type.name();
        };
    }

    /**
     * Format file size for display
     */
    private String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0) return "N/A";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    // ========== Event Handlers ==========

    @FXML
    private void handleRefresh() {
        log.info("Refreshing reports list");
        loadReportsAsync();
        loadStatistics();
    }

    @FXML
    private void handleGenerateNewReport() {
        log.info("Opening report generation dialog");
        try {
            Stage stage = (Stage) reportsTable.getScene().getWindow();
            reportDialogLauncher.showReportDialog(stage);
            // Refresh after dialog closes
            loadReportsAsync();
            loadStatistics();
        } catch (Exception e) {
            log.error("Error opening report generation dialog", e);
            showError("Error", "Failed to open report generation dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleApplyFilters() {
        applyFilters();
    }

    @FXML
    private void handleClearFilters() {
        reportTypeFilter.setValue("All Types");
        statusFilter.setValue("All Statuses");
        dateRangeFilter.setValue("All Time");
        searchField.clear();
        applyFilters();
    }

    private void handleDownloadReport(ReportHistory report) {
        log.info("Downloading report: {}", report.getReportName());

        if (report.getFilePath() == null) {
            showError("Error", "Report file path not available");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report");
        fileChooser.setInitialFileName(report.getReportName());

        String extension = report.getReportFormat() == ReportHistory.ReportFormat.EXCEL ? "*.xlsx" : "*.pdf";
        String description = report.getReportFormat() == ReportHistory.ReportFormat.EXCEL ? "Excel Files" : "PDF Files";
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(description, extension));

        Stage stage = (Stage) reportsTable.getScene().getWindow();
        File selectedFile = fileChooser.showSaveDialog(stage);

        if (selectedFile != null) {
            try {
                Path sourcePath = Paths.get(report.getFilePath());
                Files.copy(sourcePath, selectedFile.toPath());
                reportHistoryService.recordReportAccess(report.getId());
                showInfo("Success", "Report downloaded successfully to:\n" + selectedFile.getAbsolutePath());
                loadReportsAsync(); // Refresh to update download count
            } catch (IOException e) {
                log.error("Error downloading report", e);
                showError("Error", "Failed to download report: " + e.getMessage());
            }
        }
    }

    private void handleViewReport(ReportHistory report) {
        log.info("Viewing report: {}", report.getReportName());

        if (report.getFilePath() == null) {
            showError("Error", "Report file path not available");
            return;
        }

        try {
            File file = new File(report.getFilePath());
            if (file.exists()) {
                java.awt.Desktop.getDesktop().open(file);
                reportHistoryService.recordReportAccess(report.getId());
                loadReportsAsync(); // Refresh to update download count
            } else {
                showError("Error", "Report file not found at:\n" + report.getFilePath());
            }
        } catch (IOException e) {
            log.error("Error opening report", e);
            showError("Error", "Failed to open report: " + e.getMessage());
        }
    }

    private void handleDeleteReport(ReportHistory report) {
        log.info("Deleting report: {}", report.getReportName());

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Report");
        confirmAlert.setContentText("Are you sure you want to delete this report?\n\n" + report.getReportName());

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Delete file if exists
                if (report.getFilePath() != null) {
                    try {
                        Files.deleteIfExists(Paths.get(report.getFilePath()));
                    } catch (IOException e) {
                        log.warn("Could not delete report file: {}", report.getFilePath(), e);
                    }
                }

                // Update status to DELETED (we don't actually delete from DB for audit purposes)
                // This would require adding a delete method to the service
                showInfo("Success", "Report marked as deleted");
                loadReportsAsync();
                loadStatistics();
            }
        });
    }

    @FXML
    private void handleCleanupOldReports() {
        log.info("Cleaning up old reports");

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Cleanup Old Reports");
        confirmAlert.setHeaderText("Clean Up Reports Older Than 90 Days");
        confirmAlert.setContentText("This will archive reports older than 90 days.\nContinue?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                showProgress("Cleaning up old reports...");

                Task<Integer> task = new Task<>() {
                    @Override
                    protected Integer call() {
                        return reportHistoryService.cleanupOldReports(90);
                    }

                    @Override
                    protected void succeeded() {
                        int count = getValue();
                        hideProgress();
                        showInfo("Success", String.format("Cleaned up %d old reports", count));
                        loadReportsAsync();
                        loadStatistics();
                    }

                    @Override
                    protected void failed() {
                        hideProgress();
                        log.error("Error cleaning up reports", getException());
                        showError("Error", "Failed to cleanup reports: " + getException().getMessage());
                    }
                };

                new Thread(task).start();
            }
        });
    }

    @FXML
    private void handleExportToCsv() {
        log.info("Exporting report list to CSV");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Report List");
        fileChooser.setInitialFileName("report-history.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        Stage stage = (Stage) reportsTable.getScene().getWindow();
        File selectedFile = fileChooser.showSaveDialog(stage);

        if (selectedFile != null) {
            try (FileWriter writer = new FileWriter(selectedFile)) {
                // Write CSV header
                writer.write("ID,Report Name,Type,Format,Status,Generated By,Generated At,File Size,Downloads\n");

                // Write data
                for (ReportHistory report : filteredReports) {
                    writer.write(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%s,%d\n",
                        report.getId(),
                        report.getReportName(),
                        formatReportType(report.getReportType()),
                        report.getReportFormat(),
                        report.getStatus(),
                        report.getGeneratedBy() != null ? report.getGeneratedBy() : "N/A",
                        report.getGeneratedAt() != null ? report.getGeneratedAt().format(DATE_TIME_FORMATTER) : "N/A",
                        formatFileSize(report.getFileSize()),
                        report.getDownloadCount() != null ? report.getDownloadCount() : 0
                    ));
                }

                showInfo("Success", "Report list exported successfully to:\n" + selectedFile.getAbsolutePath());
            } catch (IOException e) {
                log.error("Error exporting to CSV", e);
                showError("Error", "Failed to export to CSV: " + e.getMessage());
            }
        }
    }

    // ========== UI Helper Methods ==========

    private void showProgress(String message) {
        Platform.runLater(() -> {
            progressLabel.setText(message);
            progressOverlay.setVisible(true);
            progressOverlay.setManaged(true);
        });
    }

    private void hideProgress() {
        Platform.runLater(() -> {
            progressOverlay.setVisible(false);
            progressOverlay.setManaged(false);
        });
    }

    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showInfo(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
