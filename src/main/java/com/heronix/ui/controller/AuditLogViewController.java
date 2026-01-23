package com.heronix.ui.controller;

import com.heronix.model.domain.AuditLog;
import com.heronix.model.domain.AuditLog.AuditAction;
import com.heronix.model.domain.AuditLog.AuditSeverity;
import com.heronix.repository.AuditLogRepository;
import com.heronix.security.SecurityContext;
import com.heronix.service.AuditLogExportService;
import com.heronix.service.AuditLogExportService.ExportFormat;
import com.heronix.service.AuditService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for Audit Log Viewer
 * Provides UI for browsing, filtering, and exporting audit logs.
 *
 * Features:
 * - View all audit logs with pagination
 * - Filter by date range, username, action, severity
 * - Export to CSV, JSON, or PDF
 * - View detailed log information
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since January 20, 2026
 */
@Slf4j
@Component
public class AuditLogViewController {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private AuditLogExportService auditLogExportService;

    @Autowired
    private AuditService auditService;

    // Header
    @FXML private Label totalLogsLabel;
    @FXML private Label filteredCountLabel;
    @FXML private ProgressIndicator loadingIndicator;

    // Filters
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField usernameFilter;
    @FXML private ComboBox<String> actionFilter;
    @FXML private ComboBox<String> severityFilter;
    @FXML private ComboBox<String> entityTypeFilter;
    @FXML private CheckBox showSuccessOnly;
    @FXML private CheckBox showFailuresOnly;

    // Table
    @FXML private TableView<AuditLog> auditLogTable;
    @FXML private TableColumn<AuditLog, Long> idColumn;
    @FXML private TableColumn<AuditLog, String> timestampColumn;
    @FXML private TableColumn<AuditLog, String> usernameColumn;
    @FXML private TableColumn<AuditLog, String> actionColumn;
    @FXML private TableColumn<AuditLog, String> entityColumn;
    @FXML private TableColumn<AuditLog, String> descriptionColumn;
    @FXML private TableColumn<AuditLog, String> ipAddressColumn;
    @FXML private TableColumn<AuditLog, String> severityColumn;
    @FXML private TableColumn<AuditLog, String> statusColumn;

    // Pagination
    @FXML private ComboBox<Integer> pageSizeComboBox;
    @FXML private Label pageInfoLabel;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;

    // Export
    @FXML private ComboBox<String> exportFormatComboBox;
    @FXML private Button exportButton;

    // Detail Panel
    @FXML private TextArea detailsTextArea;

    // Status
    @FXML private Label statusLabel;

    private ObservableList<AuditLog> allLogs = FXCollections.observableArrayList();
    private ObservableList<AuditLog> filteredLogs = FXCollections.observableArrayList();
    private int currentPage = 0;
    private int pageSize = 50;
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        log.info("Initializing AuditLogViewController");

        setupFilters();
        setupTable();
        setupPagination();
        setupExport();
        setupDetailPanel();

        // Set default date range (last 7 days)
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusDays(7));

        // Initial load
        loadAuditLogs();

        log.info("AuditLogViewController initialized");
    }

    private void setupFilters() {
        // Action filter
        actionFilter.getItems().add("All Actions");
        for (AuditAction action : AuditAction.values()) {
            actionFilter.getItems().add(action.name());
        }
        actionFilter.setValue("All Actions");

        // Severity filter
        severityFilter.getItems().add("All Severities");
        for (AuditSeverity severity : AuditSeverity.values()) {
            severityFilter.getItems().add(severity.name());
        }
        severityFilter.setValue("All Severities");

        // Entity type filter
        entityTypeFilter.getItems().addAll(
            "All Entities", "Student", "User", "Course", "Schedule",
            "Enrollment", "Grade", "Attendance", "IEP", "Report"
        );
        entityTypeFilter.setValue("All Entities");

        // Mutually exclusive checkboxes
        showSuccessOnly.selectedProperty().addListener((obs, old, val) -> {
            if (val) showFailuresOnly.setSelected(false);
        });
        showFailuresOnly.selectedProperty().addListener((obs, old, val) -> {
            if (val) showSuccessOnly.setSelected(false);
        });
    }

    private void setupTable() {
        // Column setup
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        timestampColumn.setCellValueFactory(cellData -> {
            LocalDateTime ts = cellData.getValue().getTimestamp();
            return new SimpleStringProperty(ts != null ? ts.format(DISPLAY_FORMATTER) : "");
        });

        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        actionColumn.setCellValueFactory(cellData -> {
            AuditAction action = cellData.getValue().getAction();
            return new SimpleStringProperty(action != null ? formatAction(action.name()) : "");
        });

        entityColumn.setCellValueFactory(cellData -> {
            String type = cellData.getValue().getEntityType();
            Long id = cellData.getValue().getEntityId();
            return new SimpleStringProperty((type != null ? type : "") + (id != null ? ":" + id : ""));
        });

        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        ipAddressColumn.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));

        severityColumn.setCellValueFactory(cellData -> {
            AuditSeverity severity = cellData.getValue().getSeverity();
            return new SimpleStringProperty(severity != null ? severity.name() : "");
        });

        // Severity column with color coding
        severityColumn.setCellFactory(column -> new TableCell<AuditLog, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "CRITICAL":
                            setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
                            break;
                        case "ERROR":
                            setStyle("-fx-background-color: #fd7e14; -fx-text-fill: white;");
                            break;
                        case "WARNING":
                            setStyle("-fx-background-color: #ffc107; -fx-text-fill: black;");
                            break;
                        case "INFO":
                            setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        statusColumn.setCellValueFactory(cellData -> {
            Boolean success = cellData.getValue().getSuccess();
            return new SimpleStringProperty(success != null ? (success ? "Success" : "Failed") : "");
        });

        // Status column with color coding
        statusColumn.setCellFactory(column -> new TableCell<AuditLog, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Success".equals(item)) {
                        setStyle("-fx-text-fill: #28a745;");
                    } else if ("Failed".equals(item)) {
                        setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        // Row selection listener
        auditLogTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    showLogDetails(newSelection);
                }
            });

        auditLogTable.setItems(filteredLogs);
    }

    private void setupPagination() {
        pageSizeComboBox.getItems().addAll(25, 50, 100, 200, 500);
        pageSizeComboBox.setValue(50);
        pageSizeComboBox.setOnAction(e -> {
            pageSize = pageSizeComboBox.getValue();
            currentPage = 0;
            applyFiltersAndPagination();
        });
    }

    private void setupExport() {
        exportFormatComboBox.getItems().addAll("CSV", "JSON", "PDF");
        exportFormatComboBox.setValue("CSV");
    }

    private void setupDetailPanel() {
        if (detailsTextArea != null) {
            detailsTextArea.setEditable(false);
            detailsTextArea.setWrapText(true);
        }
    }

    @FXML
    private void handleApplyFilters() {
        currentPage = 0;
        loadAuditLogs();
    }

    @FXML
    private void handleClearFilters() {
        startDatePicker.setValue(LocalDate.now().minusDays(7));
        endDatePicker.setValue(LocalDate.now());
        usernameFilter.clear();
        actionFilter.setValue("All Actions");
        severityFilter.setValue("All Severities");
        entityTypeFilter.setValue("All Entities");
        showSuccessOnly.setSelected(false);
        showFailuresOnly.setSelected(false);

        currentPage = 0;
        loadAuditLogs();
    }

    @FXML
    private void handleRefresh() {
        loadAuditLogs();
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 0) {
            currentPage--;
            applyFiltersAndPagination();
        }
    }

    @FXML
    private void handleNextPage() {
        int totalPages = (int) Math.ceil((double) allLogs.size() / pageSize);
        if (currentPage < totalPages - 1) {
            currentPage++;
            applyFiltersAndPagination();
        }
    }

    @FXML
    private void handleExport() {
        String formatStr = exportFormatComboBox.getValue();
        ExportFormat format = ExportFormat.valueOf(formatStr);

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Audit Logs");
        fileChooser.setInitialFileName(auditLogExportService.generateFilename(format));

        switch (format) {
            case CSV:
                fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
                break;
            case JSON:
                fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("JSON Files", "*.json"));
                break;
            case PDF:
                fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
                break;
        }

        File file = fileChooser.showSaveDialog(getStage());
        if (file != null) {
            exportToFile(file, format);
        }
    }

    private void exportToFile(File file, ExportFormat format) {
        setLoading(true);
        updateStatus("Exporting audit logs...");

        Task<Void> exportTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                LocalDateTime startDate = startDatePicker.getValue() != null ?
                    startDatePicker.getValue().atStartOfDay() : null;
                LocalDateTime endDate = endDatePicker.getValue() != null ?
                    endDatePicker.getValue().atTime(LocalTime.MAX) : null;

                String username = usernameFilter.getText();
                if (username != null && username.trim().isEmpty()) username = null;

                AuditAction action = null;
                if (!"All Actions".equals(actionFilter.getValue())) {
                    action = AuditAction.valueOf(actionFilter.getValue());
                }

                AuditSeverity severity = null;
                if (!"All Severities".equals(severityFilter.getValue())) {
                    severity = AuditSeverity.valueOf(severityFilter.getValue());
                }

                byte[] data = auditLogExportService.exportWithFilters(
                    startDate, endDate, username, action, severity, format);

                Files.write(file.toPath(), data);
                return null;
            }
        };

        exportTask.setOnSucceeded(e -> {
            setLoading(false);
            updateStatus("Export complete: " + file.getName());
            showSuccess("Audit logs exported successfully!\n\nFile: " + file.getAbsolutePath());
        });

        exportTask.setOnFailed(e -> {
            setLoading(false);
            log.error("Export failed", exportTask.getException());
            updateStatus("Export failed");
            showError("Failed to export: " + exportTask.getException().getMessage());
        });

        new Thread(exportTask).start();
    }

    private void loadAuditLogs() {
        setLoading(true);
        updateStatus("Loading audit logs...");

        Task<List<AuditLog>> loadTask = new Task<>() {
            @Override
            protected List<AuditLog> call() {
                LocalDateTime startDate = startDatePicker.getValue() != null ?
                    startDatePicker.getValue().atStartOfDay() : LocalDateTime.now().minusDays(30);
                LocalDateTime endDate = endDatePicker.getValue() != null ?
                    endDatePicker.getValue().atTime(LocalTime.MAX) : LocalDateTime.now();

                return auditLogRepository.findByTimestampBetween(startDate, endDate);
            }
        };

        loadTask.setOnSucceeded(e -> {
            allLogs.setAll(loadTask.getValue());
            applyFiltersAndPagination();
            setLoading(false);

            if (totalLogsLabel != null) {
                totalLogsLabel.setText(String.valueOf(allLogs.size()));
            }
            updateStatus("Loaded " + allLogs.size() + " audit logs");
        });

        loadTask.setOnFailed(e -> {
            setLoading(false);
            log.error("Failed to load audit logs", loadTask.getException());
            updateStatus("Failed to load logs");
            showError("Failed to load audit logs: " + loadTask.getException().getMessage());
        });

        new Thread(loadTask).start();
    }

    private void applyFiltersAndPagination() {
        // Apply filters
        List<AuditLog> filtered = allLogs.stream()
            .filter(log -> {
                // Username filter
                String userFilter = usernameFilter.getText();
                if (userFilter != null && !userFilter.trim().isEmpty()) {
                    if (log.getUsername() == null ||
                        !log.getUsername().toLowerCase().contains(userFilter.toLowerCase())) {
                        return false;
                    }
                }

                // Action filter
                if (!"All Actions".equals(actionFilter.getValue())) {
                    if (log.getAction() == null ||
                        !log.getAction().name().equals(actionFilter.getValue())) {
                        return false;
                    }
                }

                // Severity filter
                if (!"All Severities".equals(severityFilter.getValue())) {
                    if (log.getSeverity() == null ||
                        !log.getSeverity().name().equals(severityFilter.getValue())) {
                        return false;
                    }
                }

                // Entity type filter
                if (!"All Entities".equals(entityTypeFilter.getValue())) {
                    if (log.getEntityType() == null ||
                        !log.getEntityType().equalsIgnoreCase(entityTypeFilter.getValue())) {
                        return false;
                    }
                }

                // Success/failure filter
                if (showSuccessOnly.isSelected()) {
                    if (log.getSuccess() == null || !log.getSuccess()) {
                        return false;
                    }
                }
                if (showFailuresOnly.isSelected()) {
                    if (log.getSuccess() == null || log.getSuccess()) {
                        return false;
                    }
                }

                return true;
            })
            .toList();

        // Update filtered count
        if (filteredCountLabel != null) {
            filteredCountLabel.setText(String.valueOf(filtered.size()));
        }

        // Apply pagination
        int totalPages = Math.max(1, (int) Math.ceil((double) filtered.size() / pageSize));
        int startIndex = currentPage * pageSize;
        int endIndex = Math.min(startIndex + pageSize, filtered.size());

        if (startIndex < filtered.size()) {
            filteredLogs.setAll(filtered.subList(startIndex, endIndex));
        } else {
            filteredLogs.clear();
        }

        // Update pagination controls
        if (pageInfoLabel != null) {
            pageInfoLabel.setText("Page " + (currentPage + 1) + " of " + totalPages);
        }
        if (prevPageButton != null) {
            prevPageButton.setDisable(currentPage == 0);
        }
        if (nextPageButton != null) {
            nextPageButton.setDisable(currentPage >= totalPages - 1);
        }
    }

    private void showLogDetails(AuditLog log) {
        if (detailsTextArea == null) return;

        StringBuilder details = new StringBuilder();
        details.append("=== Audit Log Details ===\n\n");
        details.append("ID: ").append(log.getId()).append("\n");
        details.append("Timestamp: ").append(log.getTimestamp() != null ?
            log.getTimestamp().format(DISPLAY_FORMATTER) : "N/A").append("\n");
        details.append("Username: ").append(log.getUsername()).append("\n");
        details.append("Action: ").append(log.getAction()).append("\n");
        details.append("Entity Type: ").append(log.getEntityType()).append("\n");
        details.append("Entity ID: ").append(log.getEntityId()).append("\n");
        details.append("Description: ").append(log.getDescription()).append("\n");
        details.append("IP Address: ").append(log.getIpAddress()).append("\n");
        details.append("User Agent: ").append(log.getUserAgent()).append("\n");
        details.append("Severity: ").append(log.getSeverity()).append("\n");
        details.append("Success: ").append(log.getSuccess()).append("\n");
        details.append("Session ID: ").append(log.getSessionId()).append("\n");

        if (log.getDetails() != null && !log.getDetails().isEmpty()) {
            details.append("\n=== Additional Details ===\n");
            details.append(log.getDetails()).append("\n");
        }

        detailsTextArea.setText(details.toString());
    }

    private String formatAction(String action) {
        if (action == null) return "";
        return action.replace("_", " ");
    }

    private void setLoading(boolean loading) {
        Platform.runLater(() -> {
            if (loadingIndicator != null) {
                loadingIndicator.setVisible(loading);
            }
            if (exportButton != null) {
                exportButton.setDisable(loading);
            }
        });
    }

    private void updateStatus(String message) {
        Platform.runLater(() -> {
            if (statusLabel != null) {
                statusLabel.setText(message);
            }
        });
    }

    private Stage getStage() {
        if (auditLogTable != null && auditLogTable.getScene() != null) {
            return (Stage) auditLogTable.getScene().getWindow();
        }
        return null;
    }

    private void showSuccess(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @FXML
    private void handleClose() {
        Stage stage = getStage();
        if (stage != null) {
            stage.close();
        }
    }
}
