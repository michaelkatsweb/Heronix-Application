package com.heronix.ui.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Report Generation Dialog Controller
 *
 * Handles user interactions for generating attendance reports.
 * Provides UI for selecting report type, format, date range, and parameters.
 *
 * Features:
 * - Report type selection (Daily, Summary, Chronic Absenteeism)
 * - Format selection (Excel, PDF)
 * - Date range picker with presets
 * - Parameter configuration
 * - Real-time validation
 * - Progress feedback
 * - File save dialog
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 51 - Frontend Integration
 */
@Slf4j
@Component
public class ReportGenerationDialogController {

    @Value("${api.base-url:http://localhost:9590}")
    private String apiBaseUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(30))
        .build();

    // FXML Controls
    @FXML private ComboBox<String> reportTypeComboBox;
    @FXML private Text reportTypeDescription;
    @FXML private RadioButton excelRadio;
    @FXML private RadioButton pdfRadio;
    @FXML private ToggleGroup formatToggleGroup;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private VBox parametersSection;
    @FXML private HBox thresholdBox;
    @FXML private Spinner<Double> thresholdSpinner;
    @FXML private VBox progressSection;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label progressLabel;
    @FXML private Text progressDetail;
    @FXML private VBox validationSection;
    @FXML private Label validationMessage;
    @FXML private Button generateButton;

    private Stage dialogStage;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_DATE;

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        log.info("Initializing Report Generation Dialog");

        // Set up report type listener
        reportTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateReportTypeDescription(newVal);
            updateParametersVisibility(newVal);
            validateForm();
        });

        // Set default dates
        setYesterday();

        // Add date validation
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());

        // Initial validation
        validateForm();
    }

    /**
     * Update report type description
     */
    private void updateReportTypeDescription(String reportType) {
        if (reportType == null) {
            reportTypeDescription.setText("");
            return;
        }

        String description = switch (reportType) {
            case "Daily Attendance" ->
                "Generate a detailed report of attendance records for a specific date. " +
                "Includes student names, grades, attendance status, and timestamps.";
            case "Student Summary" ->
                "Generate a summary report showing attendance statistics for each student over a date range. " +
                "Includes present count, absent count, tardy count, and attendance rate.";
            case "Chronic Absenteeism" ->
                "Generate a report of students with chronic absenteeism (absence rate above threshold). " +
                "Helps identify at-risk students who may need intervention.";
            default -> "";
        };

        reportTypeDescription.setText(description);
    }

    /**
     * Update parameters section visibility based on report type
     */
    private void updateParametersVisibility(String reportType) {
        boolean showThreshold = "Chronic Absenteeism".equals(reportType);

        parametersSection.setVisible(showThreshold);
        parametersSection.setManaged(showThreshold);
        thresholdBox.setVisible(showThreshold);
        thresholdBox.setManaged(showThreshold);
    }

    /**
     * Validate form inputs
     */
    private boolean validateForm() {
        validationSection.setVisible(false);
        validationSection.setManaged(false);

        // Check report type
        if (reportTypeComboBox.getValue() == null) {
            showValidationError("Please select a report type");
            return false;
        }

        // Check dates
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null) {
            showValidationError("Please select start and end dates");
            return false;
        }

        if (startDate.isAfter(endDate)) {
            showValidationError("Start date must be before or equal to end date");
            return false;
        }

        if (endDate.isAfter(LocalDate.now())) {
            showValidationError("End date cannot be in the future");
            return false;
        }

        return true;
    }

    /**
     * Show validation error message
     */
    private void showValidationError(String message) {
        validationMessage.setText(message);
        validationSection.setVisible(true);
        validationSection.setManaged(true);
    }

    /**
     * Handle generate button click
     */
    @FXML
    private void handleGenerate() {
        if (!validateForm()) {
            return;
        }

        String reportType = reportTypeComboBox.getValue();
        boolean isExcel = excelRadio.isSelected();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        double threshold = thresholdSpinner.getValue();

        // Show file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report");
        fileChooser.setInitialFileName(generateFileName(reportType, isExcel, startDate, endDate));

        if (isExcel) {
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
            );
        } else {
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
        }

        File selectedFile = fileChooser.showSaveDialog(dialogStage);
        if (selectedFile == null) {
            return; // User cancelled
        }

        // Generate report in background
        generateReportAsync(reportType, isExcel, startDate, endDate, threshold, selectedFile);
    }

    /**
     * Generate report asynchronously
     */
    private void generateReportAsync(String reportType, boolean isExcel, LocalDate startDate,
                                     LocalDate endDate, double threshold, File outputFile) {
        // Show progress
        progressSection.setVisible(true);
        progressSection.setManaged(true);
        generateButton.setDisable(true);
        progressLabel.setText("Generating report...");
        progressDetail.setText("Please wait while the report is being generated.");

        Task<byte[]> task = new Task<>() {
            @Override
            protected byte[] call() throws Exception {
                updateProgress(-1, 100); // Indeterminate progress

                String endpoint = buildEndpoint(reportType, isExcel, startDate, endDate, threshold);
                log.info("Generating report: {}", endpoint);

                // Create HTTP request
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(60))
                    .GET()
                    .build();

                // Send request and get response
                HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

                if (response.statusCode() == 200) {
                    return response.body();
                } else {
                    throw new IOException("Failed to generate report: HTTP " + response.statusCode());
                }
            }

            @Override
            protected void succeeded() {
                try {
                    byte[] reportData = getValue();
                    Files.write(outputFile.toPath(), reportData);

                    Platform.runLater(() -> {
                        progressSection.setVisible(false);
                        progressSection.setManaged(false);
                        generateButton.setDisable(false);

                        showSuccessAlert(outputFile);
                    });

                } catch (IOException e) {
                    log.error("Error saving report file", e);
                    Platform.runLater(() -> showErrorAlert("Error saving file: " + e.getMessage()));
                }
            }

            @Override
            protected void failed() {
                Throwable error = getException();
                log.error("Error generating report", error);

                Platform.runLater(() -> {
                    progressSection.setVisible(false);
                    progressSection.setManaged(false);
                    generateButton.setDisable(false);

                    showErrorAlert("Error generating report: " + error.getMessage());
                });
            }
        };

        progressIndicator.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
    }

    /**
     * Build API endpoint URL
     */
    private String buildEndpoint(String reportType, boolean isExcel, LocalDate startDate,
                                 LocalDate endDate, double threshold) {
        String format = isExcel ? "excel" : "pdf";
        String base = apiBaseUrl + "/api/reports/attendance";

        return switch (reportType) {
            case "Daily Attendance" ->
                String.format("%s/daily/%s?date=%s", base, format, startDate.format(DATE_FORMAT));
            case "Student Summary" ->
                String.format("%s/summary/%s?startDate=%s&endDate=%s",
                    base, format, startDate.format(DATE_FORMAT), endDate.format(DATE_FORMAT));
            case "Chronic Absenteeism" ->
                String.format("%s/chronic-absenteeism?startDate=%s&endDate=%s&threshold=%.1f",
                    base, startDate.format(DATE_FORMAT), endDate.format(DATE_FORMAT), threshold);
            default -> throw new IllegalArgumentException("Unknown report type: " + reportType);
        };
    }

    /**
     * Generate filename for report
     */
    private String generateFileName(String reportType, boolean isExcel, LocalDate startDate, LocalDate endDate) {
        String extension = isExcel ? ".xlsx" : ".pdf";
        String dateStr = startDate.equals(endDate)
            ? startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            : String.format("%s-to-%s",
                startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        String prefix = switch (reportType) {
            case "Daily Attendance" -> "daily-attendance";
            case "Student Summary" -> "student-summary";
            case "Chronic Absenteeism" -> "chronic-absenteeism";
            default -> "report";
        };

        return prefix + "-" + dateStr + extension;
    }

    /**
     * Show success alert
     */
    private void showSuccessAlert(File file) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Report Generated");
        alert.setHeaderText("Report generated successfully!");
        alert.setContentText("Report saved to:\n" + file.getAbsolutePath());

        ButtonType openButton = new ButtonType("Open File");
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(openButton, closeButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == openButton) {
                try {
                    java.awt.Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    log.error("Error opening file", e);
                }
            }
        });
    }

    /**
     * Show error alert
     */
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Failed to generate report");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Handle cancel button
     */
    @FXML
    private void handleCancel() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    // Date Preset Methods

    @FXML
    private void setToday() {
        LocalDate today = LocalDate.now();
        startDatePicker.setValue(today);
        endDatePicker.setValue(today);
    }

    @FXML
    private void setYesterday() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        startDatePicker.setValue(yesterday);
        endDatePicker.setValue(yesterday);
    }

    @FXML
    private void setLast7Days() {
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.minusDays(6);
        startDatePicker.setValue(startDate);
        endDatePicker.setValue(endDate);
    }

    @FXML
    private void setLast30Days() {
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.minusDays(29);
        startDatePicker.setValue(startDate);
        endDatePicker.setValue(endDate);
    }

    @FXML
    private void setThisMonth() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.withDayOfMonth(1);
        LocalDate endDate = today;
        startDatePicker.setValue(startDate);
        endDatePicker.setValue(endDate);
    }

    /**
     * Set the dialog stage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
}
