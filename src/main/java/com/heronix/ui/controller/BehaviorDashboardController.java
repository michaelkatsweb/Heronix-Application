package com.heronix.ui.controller;

import com.heronix.model.domain.BehaviorIncident;
import com.heronix.model.domain.Student;
import com.heronix.service.BehaviorDashboardService;
import com.heronix.service.BehaviorIncidentService;
import com.heronix.service.StudentService;
import com.heronix.ui.component.StudentCardPopup;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Controller for Behavior Dashboard
 * Displays behavior incident analytics and management
 */
@Component
public class BehaviorDashboardController {

    private static final Logger logger = LoggerFactory.getLogger(BehaviorDashboardController.class);

    @Autowired
    private BehaviorIncidentService behaviorIncidentService;

    @Autowired
    private BehaviorDashboardService behaviorDashboardService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private ApplicationContext applicationContext;

    // Buttons
    @FXML private Button newIncidentButton;
    @FXML private Button exportButton;
    @FXML private Button refreshButton;

    // Filters
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<Student> studentFilterComboBox;
    @FXML private ComboBox<String> severityFilterComboBox;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private Button applyFiltersButton;
    @FXML private Button clearFiltersButton;

    // Statistics
    @FXML private Label totalIncidentsLabel;
    @FXML private Label totalIncidentsTrendLabel;
    @FXML private Label pendingReviewLabel;
    @FXML private Label majorIncidentsLabel;
    @FXML private Label majorIncidentsTrendLabel;
    @FXML private Label studentsInvolvedLabel;
    @FXML private Label repeatOffendersLabel;

    // Charts
    @FXML private PieChart behaviorTypeChart;
    @FXML private PieChart severityChart;
    @FXML private BarChart<String, Number> trendChart;

    // Table
    @FXML private TextField searchField;
    @FXML private TableView<BehaviorIncident> incidentsTable;
    @FXML private TableColumn<BehaviorIncident, String> dateColumn;
    @FXML private TableColumn<BehaviorIncident, String> studentColumn;
    @FXML private TableColumn<BehaviorIncident, String> behaviorTypeColumn;
    @FXML private TableColumn<BehaviorIncident, String> locationColumn;
    @FXML private TableColumn<BehaviorIncident, String> severityColumn;
    @FXML private TableColumn<BehaviorIncident, String> reportingStaffColumn;
    @FXML private TableColumn<BehaviorIncident, String> statusColumn;
    @FXML private TableColumn<BehaviorIncident, Void> actionsColumn;
    @FXML private Label paginationLabel;
    @FXML private Pagination pagination;

    // Sidebar
    @FXML private ListView<String> topBehaviorsListView;
    @FXML private ListView<String> atRiskStudentsListView;
    @FXML private Label pendingContactsLabel;
    @FXML private Label pendingReferralsLabel;
    @FXML private Label pendingFollowUpsLabel;

    @FXML
    public void initialize() {
        logger.info("Initializing Behavior Dashboard Controller");

        // Set default date range (last 30 days)
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusDays(30));

        // Setup severity filter
        severityFilterComboBox.setItems(FXCollections.observableArrayList(
            "All Severities", "MINOR", "MODERATE", "MAJOR", "SEVERE"
        ));
        severityFilterComboBox.setValue("All Severities");

        // Setup status filter
        statusFilterComboBox.setItems(FXCollections.observableArrayList(
            "All Statuses", "DRAFT", "SUBMITTED", "UNDER_REVIEW", "RESOLVED", "CLOSED"
        ));
        statusFilterComboBox.setValue("All Statuses");

        // Setup table columns
        setupTableColumns();

        // Load students for filter
        loadStudentsFilter();

        // Load initial data
        loadDashboardData();

        // Setup search
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterIncidents());
    }

    private void setupTableColumns() {
        dateColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getIncidentDate() != null ?
                cellData.getValue().getIncidentDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : ""));

        studentColumn.setCellValueFactory(cellData -> {
            Student student = cellData.getValue().getStudent();
            return new SimpleStringProperty(student != null ?
                student.getFirstName() + " " + student.getLastName() : "");
        });

        behaviorTypeColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getBehaviorType() != null ?
                cellData.getValue().getBehaviorType().name() : ""));

        locationColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getLocation()));

        severityColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getSeverity() != null ?
                cellData.getValue().getSeverity().name() : ""));

        reportingStaffColumn.setCellValueFactory(cellData -> {
            var staff = cellData.getValue().getReportingStaff();
            return new SimpleStringProperty(staff != null ?
                staff.getFirstName() + " " + staff.getLastName() : "");
        });

        statusColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getStatus() != null ?
                cellData.getValue().getStatus().name() : ""));

        // Actions column with buttons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            private final Button editButton = new Button("Edit");
            private final HBox buttons = new HBox(5, viewButton, editButton);

            {
                viewButton.setOnAction(event -> {
                    BehaviorIncident incident = getTableView().getItems().get(getIndex());
                    handleViewIncident(incident);
                });

                editButton.setOnAction(event -> {
                    BehaviorIncident incident = getTableView().getItems().get(getIndex());
                    handleEditIncident(incident);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });

        // Double-click row to open Student Card popup
        incidentsTable.setRowFactory(tv -> {
            TableRow<BehaviorIncident> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    BehaviorIncident incident = row.getItem();
                    if (incident != null && incident.getStudent() != null && incident.getStudent().getId() != null) {
                        StudentCardPopup.show(applicationContext, incident.getStudent().getId(),
                            incidentsTable.getScene().getWindow());
                    }
                }
            });
            return row;
        });
    }

    private void loadStudentsFilter() {
        try {
            List<Student> students = studentService.getAllStudents();
            studentFilterComboBox.setItems(FXCollections.observableArrayList(students));

            studentFilterComboBox.setConverter(new javafx.util.StringConverter<Student>() {
                @Override
                public String toString(Student student) {
                    return student == null ? "All Students" :
                        student.getFirstName() + " " + student.getLastName();
                }

                @Override
                public Student fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            logger.error("Error loading students", e);
        }
    }

    private void loadDashboardData() {
        try {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            // Load statistics
            Map<String, Object> stats = behaviorDashboardService.getDashboardStatistics(startDate, endDate);

            totalIncidentsLabel.setText(stats.getOrDefault("totalIncidents", 0).toString());
            pendingReviewLabel.setText(stats.getOrDefault("pendingReview", 0).toString());
            majorIncidentsLabel.setText(stats.getOrDefault("majorIncidents", 0).toString());
            studentsInvolvedLabel.setText(stats.getOrDefault("studentsInvolved", 0).toString());
            repeatOffendersLabel.setText(stats.getOrDefault("repeatOffenders", 0) + " repeat offenders");

            // Load charts
            loadBehaviorTypeChart(startDate, endDate);
            loadSeverityChart(startDate, endDate);
            loadTrendChart(startDate, endDate);

            // Load table data
            loadIncidents();

            // Load sidebar data
            loadTopBehaviors(startDate, endDate);
            loadAtRiskStudents();
            loadPendingActions();

        } catch (Exception e) {
            logger.error("Error loading dashboard data", e);
            showError("Failed to load dashboard data: " + e.getMessage());
        }
    }

    private void loadBehaviorTypeChart(LocalDate startDate, LocalDate endDate) {
        try {
            var behaviorCountsRaw = behaviorDashboardService.getIncidentsByBehaviorType(startDate, endDate);

            behaviorTypeChart.getData().clear();
            behaviorCountsRaw.forEach((type, count) -> {
                String typeName = type != null ? type.name() : "Unknown";
                PieChart.Data slice = new PieChart.Data(typeName + " (" + count + ")", count);
                behaviorTypeChart.getData().add(slice);
            });
        } catch (Exception e) {
            logger.error("Error loading behavior type chart", e);
        }
    }

    private void loadSeverityChart(LocalDate startDate, LocalDate endDate) {
        try {
            var severityCountsRaw = behaviorDashboardService.getIncidentsBySeverity(startDate, endDate);

            severityChart.getData().clear();
            severityCountsRaw.forEach((severity, count) -> {
                String severityName = severity != null ? severity.name() : "Unknown";
                PieChart.Data slice = new PieChart.Data(severityName + " (" + count + ")", count);
                severityChart.getData().add(slice);
            });
        } catch (Exception e) {
            logger.error("Error loading severity chart", e);
        }
    }

    private void loadTrendChart(LocalDate startDate, LocalDate endDate) {
        try {
            Map<LocalDate, Long> trends = behaviorDashboardService.getIncidentTrends(startDate, endDate);

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Incidents");

            trends.forEach((date, count) -> {
                series.getData().add(new XYChart.Data<>(
                    date.format(DateTimeFormatter.ofPattern("MM/dd")), count));
            });

            trendChart.getData().clear();
            trendChart.getData().add(series);
        } catch (Exception e) {
            logger.error("Error loading trend chart", e);
        }
    }

    private void loadIncidents() {
        try {
            List<BehaviorIncident> incidents = behaviorIncidentService.getAllIncidents();
            incidentsTable.setItems(FXCollections.observableArrayList(incidents));
            paginationLabel.setText("Showing " + incidents.size() + " incidents");
        } catch (Exception e) {
            logger.error("Error loading incidents", e);
        }
    }

    private void loadTopBehaviors(LocalDate startDate, LocalDate endDate) {
        try {
            var topBehaviorsRaw = behaviorDashboardService.getTopBehaviorTypes(startDate, endDate, 5);

            List<String> items = topBehaviorsRaw.stream()
                .map(entry -> entry.getKey().name() + " (" + entry.getValue() + ")")
                .toList();

            topBehaviorsListView.setItems(FXCollections.observableArrayList(items));
        } catch (Exception e) {
            logger.error("Error loading top behaviors", e);
        }
    }

    private void loadAtRiskStudents() {
        try {
            var atRiskData = behaviorDashboardService.getAtRiskStudents(5);

            List<String> items = atRiskData.stream()
                .map(data -> {
                    Student student = (Student) data.get("student");
                    Long count = (Long) data.get("incidentCount");
                    return student.getFirstName() + " " + student.getLastName() + " (" + count + " incidents)";
                })
                .toList();

            atRiskStudentsListView.setItems(FXCollections.observableArrayList(items));
        } catch (Exception e) {
            logger.error("Error loading at-risk students", e);
        }
    }

    private void loadPendingActions() {
        try {
            var pendingActionsData = behaviorDashboardService.getPendingActions();

            List<String> items = pendingActionsData.stream()
                .map(action -> action.get("message").toString())
                .toList();

            if (items.size() > 0) pendingContactsLabel.setText(items.get(0));
            if (items.size() > 1) pendingReferralsLabel.setText(items.get(1));
            if (items.size() > 2) pendingFollowUpsLabel.setText(items.get(2));
        } catch (Exception e) {
            logger.error("Error loading pending actions", e);
        }
    }

    @FXML
    private void handleNewIncident() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BehaviorIncidentForm.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("New Behavior Incident");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh data after closing
            loadDashboardData();
        } catch (Exception e) {
            logger.error("Error opening incident form", e);
            showError("Failed to open incident form: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadDashboardData();
    }

    @FXML
    private void handleApplyFilters() {
        loadDashboardData();
    }

    @FXML
    private void handleClearFilters() {
        startDatePicker.setValue(LocalDate.now().minusDays(30));
        endDatePicker.setValue(LocalDate.now());
        studentFilterComboBox.setValue(null);
        severityFilterComboBox.setValue("All Severities");
        statusFilterComboBox.setValue("All Statuses");
        loadDashboardData();
    }

    @FXML
    private void handleExport() {
        // Show export format selection dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Export Behavior Data");
        alert.setHeaderText("Select Export Format");
        alert.setContentText("Choose the format for exporting behavior incident data:");

        ButtonType pdfButton = new ButtonType("PDF");
        ButtonType excelButton = new ButtonType("Excel (CSV)");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(pdfButton, excelButton, cancelButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == pdfButton) {
                exportToPdf();
            } else if (response == excelButton) {
                exportToCsv();
            }
        });
    }

    private void exportToPdf() {
        try {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Save Behavior Report PDF");
            fileChooser.setInitialFileName("behavior_report_" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf");
            fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            java.io.File file = fileChooser.showSaveDialog(exportButton.getScene().getWindow());
            if (file != null) {
                // Get filtered incidents
                List<BehaviorIncident> incidents = incidentsTable.getItems();
                LocalDate startDate = startDatePicker.getValue();
                LocalDate endDate = endDatePicker.getValue();

                // Generate PDF
                com.itextpdf.text.Document document = new com.itextpdf.text.Document();
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                com.itextpdf.text.pdf.PdfWriter.getInstance(document, baos);

                document.open();

                // Add title
                com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
                com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph(
                    "Behavior Incident Report", titleFont);
                title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                document.add(title);

                // Add date range
                com.itextpdf.text.Paragraph dateRange = new com.itextpdf.text.Paragraph(
                    "Report Period: " + startDate + " to " + endDate);
                dateRange.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                document.add(dateRange);
                document.add(new com.itextpdf.text.Paragraph(" "));

                // Add table
                com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(7);
                table.setWidthPercentage(100);

                // Headers
                table.addCell("Date");
                table.addCell("Student");
                table.addCell("Behavior Type");
                table.addCell("Location");
                table.addCell("Severity");
                table.addCell("Status");
                table.addCell("Reporting Staff");

                // Data rows
                for (BehaviorIncident incident : incidents) {
                    table.addCell(incident.getIncidentDate() != null ?
                        incident.getIncidentDate().toString() : "");
                    table.addCell(incident.getStudent() != null ?
                        incident.getStudent().getFirstName() + " " + incident.getStudent().getLastName() : "");
                    table.addCell(incident.getBehaviorType() != null ?
                        incident.getBehaviorType().name() : "");
                    table.addCell(incident.getLocation() != null ? incident.getLocation() : "");
                    table.addCell(incident.getSeverity() != null ?
                        incident.getSeverity().name() : "");
                    table.addCell(incident.getStatus() != null ?
                        incident.getStatus().name() : "");
                    table.addCell(incident.getReportingStaff() != null ?
                        incident.getReportingStaff().getFirstName() + " " +
                        incident.getReportingStaff().getLastName() : "");
                }

                document.add(table);
                document.close();

                // Save to file
                java.nio.file.Files.write(file.toPath(), baos.toByteArray());

                showInfo("Export Successful", "Behavior report exported to PDF successfully!");
                logger.info("Behavior report exported to PDF: {}", file.getAbsolutePath());
            }
        } catch (Exception e) {
            logger.error("Error exporting to PDF", e);
            showError("Failed to export PDF: " + e.getMessage());
        }
    }

    private void exportToCsv() {
        try {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Save Behavior Report CSV");
            fileChooser.setInitialFileName("behavior_report_" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".csv");
            fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));

            java.io.File file = fileChooser.showSaveDialog(exportButton.getScene().getWindow());
            if (file != null) {
                // Get filtered incidents
                List<BehaviorIncident> incidents = incidentsTable.getItems();

                // Generate CSV
                StringBuilder csv = new StringBuilder();
                csv.append("Date,Student ID,Student Name,Behavior Type,Location,Severity,Status,Reporting Staff,Notes\n");

                for (BehaviorIncident incident : incidents) {
                    csv.append(escapeCsv(incident.getIncidentDate() != null ?
                        incident.getIncidentDate().format(DateTimeFormatter.ISO_DATE) : "")).append(",");
                    csv.append(escapeCsv(incident.getStudent() != null ?
                        incident.getStudent().getStudentId() : "")).append(",");
                    csv.append(escapeCsv(incident.getStudent() != null ?
                        incident.getStudent().getFirstName() + " " + incident.getStudent().getLastName() : "")).append(",");
                    csv.append(escapeCsv(incident.getBehaviorType() != null ?
                        incident.getBehaviorType().name() : "")).append(",");
                    csv.append(escapeCsv(incident.getLocation())).append(",");
                    csv.append(escapeCsv(incident.getSeverity() != null ?
                        incident.getSeverity().name() : "")).append(",");
                    csv.append(escapeCsv(incident.getStatus() != null ?
                        incident.getStatus().name() : "")).append(",");
                    csv.append(escapeCsv(incident.getReportingStaff() != null ?
                        incident.getReportingStaff().getFirstName() + " " +
                        incident.getReportingStaff().getLastName() : "")).append(",");
                    csv.append(escapeCsv(incident.getDescription())).append("\n");
                }

                // Save to file
                java.nio.file.Files.writeString(file.toPath(), csv.toString(),
                    java.nio.charset.StandardCharsets.UTF_8);

                showInfo("Export Successful", "Behavior report exported to CSV successfully!");
                logger.info("Behavior report exported to CSV: {}", file.getAbsolutePath());
            }
        } catch (Exception e) {
            logger.error("Error exporting to CSV", e);
            showError("Failed to export CSV: " + e.getMessage());
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    @FXML
    private void handleGenerateReport() {
        try {
            // Show report type selection dialog
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Generate Behavior Report");
            alert.setHeaderText("Select Report Type");
            alert.setContentText("Choose the type of report to generate:");

            ButtonType summaryButton = new ButtonType("Summary Report");
            ButtonType detailedButton = new ButtonType("Detailed Report");
            ButtonType analyticsButton = new ButtonType("Analytics Report");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(summaryButton, detailedButton, analyticsButton, cancelButton);

            alert.showAndWait().ifPresent(response -> {
                if (response == summaryButton) {
                    generateSummaryReport();
                } else if (response == detailedButton) {
                    generateDetailedReport();
                } else if (response == analyticsButton) {
                    generateAnalyticsReport();
                }
            });
        } catch (Exception e) {
            logger.error("Error generating report", e);
            showError("Failed to generate report: " + e.getMessage());
        }
    }

    private void generateSummaryReport() {
        try {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            StringBuilder report = new StringBuilder();
            report.append("BEHAVIOR INCIDENT SUMMARY REPORT\n");
            report.append("=================================\n\n");
            report.append("Report Period: ").append(startDate).append(" to ").append(endDate).append("\n\n");

            report.append("STATISTICS:\n");
            report.append("Total Incidents: ").append(totalIncidentsLabel.getText()).append("\n");
            report.append("Pending Review: ").append(pendingReviewLabel.getText()).append("\n");
            report.append("Major Incidents: ").append(majorIncidentsLabel.getText()).append("\n");
            report.append("Students Involved: ").append(studentsInvolvedLabel.getText()).append("\n");
            report.append(repeatOffendersLabel.getText()).append("\n\n");

            report.append("TOP BEHAVIORS:\n");
            for (String behavior : topBehaviorsListView.getItems()) {
                report.append("- ").append(behavior).append("\n");
            }

            report.append("\nAT-RISK STUDENTS:\n");
            for (String student : atRiskStudentsListView.getItems()) {
                report.append("- ").append(student).append("\n");
            }

            showReportDialog("Summary Report", report.toString());
        } catch (Exception e) {
            logger.error("Error generating summary report", e);
            showError("Failed to generate summary report: " + e.getMessage());
        }
    }

    private void generateDetailedReport() {
        try {
            List<BehaviorIncident> incidents = incidentsTable.getItems();

            StringBuilder report = new StringBuilder();
            report.append("DETAILED BEHAVIOR INCIDENT REPORT\n");
            report.append("===================================\n\n");

            for (BehaviorIncident incident : incidents) {
                report.append("Incident Date: ").append(incident.getIncidentDate()).append("\n");
                report.append("Student: ").append(incident.getStudent() != null ?
                    incident.getStudent().getFirstName() + " " + incident.getStudent().getLastName() : "N/A").append("\n");
                report.append("Behavior Type: ").append(incident.getBehaviorType()).append("\n");
                report.append("Location: ").append(incident.getLocation()).append("\n");
                report.append("Severity: ").append(incident.getSeverity()).append("\n");
                report.append("Status: ").append(incident.getStatus()).append("\n");
                report.append("Description: ").append(incident.getDescription() != null ?
                    incident.getDescription() : "None").append("\n");
                report.append("Reporting Staff: ").append(incident.getReportingStaff() != null ?
                    incident.getReportingStaff().getFirstName() + " " +
                    incident.getReportingStaff().getLastName() : "N/A").append("\n");
                report.append("---\n\n");
            }

            showReportDialog("Detailed Report", report.toString());
        } catch (Exception e) {
            logger.error("Error generating detailed report", e);
            showError("Failed to generate detailed report: " + e.getMessage());
        }
    }

    private void generateAnalyticsReport() {
        try {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            StringBuilder report = new StringBuilder();
            report.append("BEHAVIOR ANALYTICS REPORT\n");
            report.append("==========================\n\n");
            report.append("Report Period: ").append(startDate).append(" to ").append(endDate).append("\n\n");

            report.append("BEHAVIOR TYPE BREAKDOWN:\n");
            var behaviorCounts = behaviorDashboardService.getIncidentsByBehaviorType(startDate, endDate);
            behaviorCounts.forEach((type, count) ->
                report.append("- ").append(type != null ? type.name() : "Unknown")
                    .append(": ").append(count).append("\n"));

            report.append("\nSEVERITY BREAKDOWN:\n");
            var severityCounts = behaviorDashboardService.getIncidentsBySeverity(startDate, endDate);
            severityCounts.forEach((severity, count) ->
                report.append("- ").append(severity != null ? severity.name() : "Unknown")
                    .append(": ").append(count).append("\n"));

            report.append("\nTRENDS ANALYSIS:\n");
            var trends = behaviorDashboardService.getIncidentTrends(startDate, endDate);
            trends.forEach((date, count) ->
                report.append("- ").append(date).append(": ").append(count).append(" incidents\n"));

            showReportDialog("Analytics Report", report.toString());
        } catch (Exception e) {
            logger.error("Error generating analytics report", e);
            showError("Failed to generate analytics report: " + e.getMessage());
        }
    }

    private void showReportDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);

        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(25);
        textArea.setPrefColumnCount(80);

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    @FXML
    private void handleViewTrends() {
        try {
            // Create a new stage for trends view
            Stage trendStage = new Stage();
            trendStage.setTitle("Behavior Trends Analysis");

            // Create tab pane for different trend views
            TabPane tabPane = new TabPane();

            // Tab 1: Trend Chart (already visible in main dashboard)
            Tab chartTab = new Tab("Trend Chart");
            chartTab.setClosable(false);

            LineChart<String, Number> lineChart = new LineChart<>(
                new CategoryAxis(), new NumberAxis());
            lineChart.setTitle("Behavior Incidents Over Time");

            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            var trends = behaviorDashboardService.getIncidentTrends(startDate, endDate);
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Incidents");

            trends.forEach((date, count) -> {
                series.getData().add(new XYChart.Data<>(
                    date.format(DateTimeFormatter.ofPattern("MM/dd")), count));
            });

            lineChart.getData().add(series);
            chartTab.setContent(lineChart);
            tabPane.getTabs().add(chartTab);

            // Tab 2: Weekly Trends
            Tab weeklyTab = new Tab("Weekly Comparison");
            weeklyTab.setClosable(false);

            TextArea weeklyText = new TextArea();
            weeklyText.setEditable(false);
            weeklyText.setText("Weekly Trend Analysis:\n\n" +
                "Week 1: " + (trends.size() > 0 ? trends.values().stream().limit(7).mapToLong(Long::longValue).sum() : 0) + " incidents\n" +
                "Week 2: " + (trends.size() > 7 ? trends.values().stream().skip(7).limit(7).mapToLong(Long::longValue).sum() : 0) + " incidents\n" +
                "Week 3: " + (trends.size() > 14 ? trends.values().stream().skip(14).limit(7).mapToLong(Long::longValue).sum() : 0) + " incidents\n" +
                "Week 4: " + (trends.size() > 21 ? trends.values().stream().skip(21).limit(7).mapToLong(Long::longValue).sum() : 0) + " incidents\n");

            weeklyTab.setContent(weeklyText);
            tabPane.getTabs().add(weeklyTab);

            // Set scene and show
            Scene scene = new Scene(tabPane, 800, 600);
            trendStage.setScene(scene);
            trendStage.show();

        } catch (Exception e) {
            logger.error("Error showing trends view", e);
            showError("Failed to load trends view: " + e.getMessage());
        }
    }

    @FXML
    private void handleInterventions() {
        try {
            // Create interventions dialog
            Stage interventionStage = new Stage();
            interventionStage.setTitle("Behavior Interventions Management");
            interventionStage.initModality(Modality.APPLICATION_MODAL);

            // Create layout
            javafx.scene.layout.VBox layout = new javafx.scene.layout.VBox(10);
            layout.setPadding(new javafx.geometry.Insets(15));

            Label titleLabel = new Label("RECOMMENDED INTERVENTIONS");
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            Label descLabel = new Label("Students requiring behavioral intervention:");
            descLabel.setStyle("-fx-font-size: 12px;");

            // Create table for at-risk students with recommended interventions
            TableView<Map<String, Object>> interventionTable = new TableView<>();

            TableColumn<Map<String, Object>, String> studentCol = new TableColumn<>("Student");
            studentCol.setCellValueFactory(cellData -> {
                Student student = (Student) cellData.getValue().get("student");
                return new SimpleStringProperty(student.getFirstName() + " " + student.getLastName());
            });

            TableColumn<Map<String, Object>, String> incidentCountCol = new TableColumn<>("Incidents");
            incidentCountCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().get("incidentCount").toString()));

            TableColumn<Map<String, Object>, String> interventionCol = new TableColumn<>("Recommended Intervention");
            interventionCol.setCellValueFactory(cellData -> {
                Long count = (Long) cellData.getValue().get("incidentCount");
                String intervention;
                if (count >= 5) {
                    intervention = "Immediate counselor referral + behavior contract";
                } else if (count >= 3) {
                    intervention = "Parent meeting + intervention plan";
                } else {
                    intervention = "Counselor check-in";
                }
                return new SimpleStringProperty(intervention);
            });

            interventionTable.getColumns().addAll(studentCol, incidentCountCol, interventionCol);

            // Load at-risk students data
            var atRiskData = behaviorDashboardService.getAtRiskStudents(10);
            interventionTable.setItems(FXCollections.observableArrayList(atRiskData));

            // Add buttons
            javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(10);
            Button scheduleButton = new Button("Schedule Intervention");
            Button contactButton = new Button("Contact Parent");
            Button closeButton = new Button("Close");

            scheduleButton.setOnAction(e -> {
                if (interventionTable.getSelectionModel().getSelectedItem() != null) {
                    showInfo("Schedule Intervention",
                        "Intervention scheduling will open the counseling calendar.");
                }
            });

            contactButton.setOnAction(e -> {
                if (interventionTable.getSelectionModel().getSelectedItem() != null) {
                    showInfo("Contact Parent",
                        "Parent contact form will be opened.");
                }
            });

            closeButton.setOnAction(e -> interventionStage.close());

            buttonBox.getChildren().addAll(scheduleButton, contactButton, closeButton);

            layout.getChildren().addAll(titleLabel, descLabel, interventionTable, buttonBox);

            Scene scene = new Scene(layout, 700, 500);
            interventionStage.setScene(scene);
            interventionStage.show();

        } catch (Exception e) {
            logger.error("Error showing interventions view", e);
            showError("Failed to load interventions view: " + e.getMessage());
        }
    }

    private void handleViewIncident(BehaviorIncident incident) {
        try {
            // Create incident detail dialog
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Incident Details");
            alert.setHeaderText("Behavior Incident #" + incident.getId());

            // Create detailed view
            javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20));

            int row = 0;

            addDetailRow(grid, row++, "Date:", incident.getIncidentDate() != null ?
                incident.getIncidentDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : "N/A");
            addDetailRow(grid, row++, "Time:", incident.getIncidentTime() != null ?
                incident.getIncidentTime().toString() : "N/A");
            addDetailRow(grid, row++, "Student:", incident.getStudent() != null ?
                incident.getStudent().getFirstName() + " " + incident.getStudent().getLastName() : "N/A");
            addDetailRow(grid, row++, "Student ID:", incident.getStudent() != null ?
                incident.getStudent().getStudentId() : "N/A");
            addDetailRow(grid, row++, "Behavior Type:", incident.getBehaviorType() != null ?
                incident.getBehaviorType().name() : "N/A");
            addDetailRow(grid, row++, "Category:", incident.getBehaviorCategory() != null ?
                incident.getBehaviorCategory().name() : "N/A");
            addDetailRow(grid, row++, "Location:", incident.getLocation() != null ?
                incident.getLocation() : "N/A");
            addDetailRow(grid, row++, "Severity:", incident.getSeverity() != null ?
                incident.getSeverity().name() : "N/A");
            addDetailRow(grid, row++, "Status:", incident.getStatus() != null ?
                incident.getStatus().name() : "N/A");
            addDetailRow(grid, row++, "Reporting Staff:", incident.getReportingStaff() != null ?
                incident.getReportingStaff().getFirstName() + " " +
                incident.getReportingStaff().getLastName() : "N/A");

            grid.add(new Label("Description:"), 0, row);
            TextArea descArea = new TextArea(incident.getDescription() != null ?
                incident.getDescription() : "No description provided");
            descArea.setEditable(false);
            descArea.setPrefRowCount(4);
            descArea.setWrapText(true);
            grid.add(descArea, 1, row++);

            addDetailRow(grid, row++, "Intervention Applied:", incident.getInterventionApplied() != null ?
                incident.getInterventionApplied() : "None");
            addDetailRow(grid, row++, "Parent Contacted:", incident.getParentContacted() != null ?
                (incident.getParentContacted() ? "Yes" : "No") : "N/A");
            if (incident.getParentContactDate() != null) {
                addDetailRow(grid, row++, "Contact Date:", incident.getParentContactDate().toString());
            }
            addDetailRow(grid, row++, "Admin Referral Required:", incident.getAdminReferralRequired() != null ?
                (incident.getAdminReferralRequired() ? "Yes" : "No") : "N/A");
            addDetailRow(grid, row++, "Referral Outcome:", incident.getReferralOutcome() != null ?
                incident.getReferralOutcome() : "N/A");

            alert.getDialogPane().setContent(grid);
            alert.getDialogPane().setPrefWidth(600);
            alert.showAndWait();

        } catch (Exception e) {
            logger.error("Error showing incident details", e);
            showError("Failed to load incident details: " + e.getMessage());
        }
    }

    private void addDetailRow(javafx.scene.layout.GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-weight: bold;");
        grid.add(labelNode, 0, row);
        grid.add(new Label(value), 1, row);
    }

    private void handleEditIncident(BehaviorIncident incident) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BehaviorIncidentForm.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            BehaviorIncidentFormController controller = loader.getController();
            controller.setIncident(incident);

            Stage stage = new Stage();
            stage.setTitle("Edit Behavior Incident");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh data after closing
            loadDashboardData();
        } catch (Exception e) {
            logger.error("Error opening incident form", e);
            showError("Failed to open incident form: " + e.getMessage());
        }
    }

    private void filterIncidents() {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            loadIncidents();
            return;
        }

        List<BehaviorIncident> filtered = incidentsTable.getItems().stream()
            .filter(incident -> {
                String studentName = incident.getStudent().getFirstName() + " " +
                    incident.getStudent().getLastName();
                return studentName.toLowerCase().contains(searchText) ||
                    incident.getBehaviorType().toLowerCase().contains(searchText) ||
                    incident.getLocation().toLowerCase().contains(searchText);
            })
            .toList();

        incidentsTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
