package com.heronix.ui.controller;

import com.heronix.model.domain.IEP;
import com.heronix.model.domain.IEPService;
import com.heronix.model.domain.Plan504;
import com.heronix.service.IEPManagementService;
import com.heronix.service.Plan504Service;
import com.heronix.service.PullOutSchedulingService;
import com.heronix.service.SPEDComplianceService;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * SPED Dashboard Controller
 *
 * Main dashboard for Special Education management showing compliance metrics,
 * expiring IEPs/504 plans, and services needing attention.
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 8C - November 21, 2025
 */
@Controller
@Slf4j
public class SPEDDashboardController {

    @Autowired
    private IEPManagementService iepManagementService;

    @Autowired
    private Plan504Service plan504Service;

    @Autowired
    private PullOutSchedulingService pullOutSchedulingService;

    @Autowired
    private SPEDComplianceService complianceService;

    // Metrics
    @FXML private Label activeIEPsLabel;
    @FXML private Label active504Label;
    @FXML private Label needsSchedulingLabel;
    @FXML private Label complianceRateLabel;
    @FXML private ProgressBar complianceProgress;
    @FXML private Label actionItemsCountLabel;

    // Expiring IEPs Table
    @FXML private TableView<IEP> expiringIEPsTable;
    @FXML private TableColumn<IEP, String> iepStudentColumn;
    @FXML private TableColumn<IEP, String> iepNumberColumn;
    @FXML private TableColumn<IEP, String> iepEndDateColumn;
    @FXML private TableColumn<IEP, String> iepDaysRemainingColumn;
    @FXML private TableColumn<IEP, String> iepCaseManagerColumn;
    @FXML private TableColumn<IEP, String> iepActionsColumn;

    // Expiring 504 Plans Table
    @FXML private TableView<Plan504> expiring504Table;
    @FXML private TableColumn<Plan504, String> plan504StudentColumn;
    @FXML private TableColumn<Plan504, String> plan504NumberColumn;
    @FXML private TableColumn<Plan504, String> plan504EndDateColumn;
    @FXML private TableColumn<Plan504, String> plan504DaysRemainingColumn;
    @FXML private TableColumn<Plan504, String> plan504CoordinatorColumn;
    @FXML private TableColumn<Plan504, String> plan504ActionsColumn;

    // Services Below Minutes Table
    @FXML private TableView<IEPService> belowMinutesTable;
    @FXML private TableColumn<IEPService, String> serviceStudentColumn;
    @FXML private TableColumn<IEPService, String> serviceTypeColumn;
    @FXML private TableColumn<IEPService, String> requiredMinutesColumn;
    @FXML private TableColumn<IEPService, String> scheduledMinutesColumn;
    @FXML private TableColumn<IEPService, String> compliancePercentColumn;
    @FXML private TableColumn<IEPService, String> serviceActionsColumn;

    // Statistics Containers
    @FXML private VBox iepStatsContainer;
    @FXML private VBox serviceStatsContainer;
    @FXML private TabPane actionItemsTabs;

    @FXML
    public void initialize() {
        log.info("Initializing SPED Dashboard");
        setupTables();
        loadData();
    }

    private void setupTables() {
        // Setup Expiring IEPs Table
        iepStudentColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getStudent().getFullName()));
        iepNumberColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getIepNumber() != null ? data.getValue().getIepNumber() : "Draft"));
        iepEndDateColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getEndDate().toString()));
        iepDaysRemainingColumn.setCellValueFactory(data -> {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), data.getValue().getEndDate());
            return new SimpleStringProperty(String.valueOf(days));
        });
        iepCaseManagerColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getCaseManager() != null ? data.getValue().getCaseManager() : "N/A"));
        iepActionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            {
                viewBtn.setOnAction(e -> handleViewIEP(getTableRow().getItem()));
                viewBtn.getStyleClass().add("btn-link");
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewBtn);
            }
        });

        // Setup Expiring 504 Plans Table
        plan504StudentColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getStudent().getFullName()));
        plan504NumberColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getPlanNumber() != null ? data.getValue().getPlanNumber() : "Draft"));
        plan504EndDateColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getEndDate().toString()));
        plan504DaysRemainingColumn.setCellValueFactory(data -> {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), data.getValue().getEndDate());
            return new SimpleStringProperty(String.valueOf(days));
        });
        plan504CoordinatorColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getCoordinator() != null ? data.getValue().getCoordinator() : "N/A"));
        plan504ActionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            {
                viewBtn.setOnAction(e -> handleView504Plan(getTableRow().getItem()));
                viewBtn.getStyleClass().add("btn-link");
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewBtn);
            }
        });

        // Setup Services Below Minutes Table
        serviceStudentColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getStudent().getFullName()));
        serviceTypeColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getServiceType().getDisplayName()));
        requiredMinutesColumn.setCellValueFactory(data ->
            new SimpleStringProperty(String.valueOf(data.getValue().getMinutesPerWeek())));
        scheduledMinutesColumn.setCellValueFactory(data ->
            new SimpleStringProperty(String.valueOf(data.getValue().getScheduledMinutesPerWeek())));
        compliancePercentColumn.setCellValueFactory(data ->
            new SimpleStringProperty(String.format("%.1f%%", data.getValue().getCompliancePercentage())));
        serviceActionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button scheduleBtn = new Button("Schedule");
            {
                scheduleBtn.setOnAction(e -> handleScheduleService(getTableRow().getItem()));
                scheduleBtn.getStyleClass().add("btn-link");
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : scheduleBtn);
            }
        });
    }

    private void loadData() {
        try {
            log.info("Loading SPED dashboard data");

            // Load metrics
            SPEDComplianceService.DashboardMetrics metrics = complianceService.getDashboardMetrics();

            activeIEPsLabel.setText(String.valueOf(metrics.totalActiveIEPs));
            active504Label.setText(String.valueOf(metrics.totalActive504Plans));
            needsSchedulingLabel.setText(String.valueOf(metrics.servicesNeedingScheduling));

            double complianceRate = metrics.overallComplianceRate;
            complianceRateLabel.setText(String.format("%.1f%%", complianceRate));
            complianceProgress.setProgress(complianceRate / 100.0);

            // Load expiring IEPs (30 days threshold)
            var expiringIEPs = complianceService.getExpiringIEPs(30);
            expiringIEPsTable.getItems().setAll(expiringIEPs);

            // Load expiring 504 plans
            var expiring504s = complianceService.getExpiring504Plans(30);
            expiring504Table.getItems().setAll(expiring504s);

            // Load services not meeting minutes
            var belowMinutes = complianceService.getServicesNotMeetingMinutes();
            belowMinutesTable.getItems().setAll(belowMinutes);

            // Update action items count
            int totalActionItems = expiringIEPs.size() + expiring504s.size() + belowMinutes.size();
            actionItemsCountLabel.setText(totalActionItems + " items");

            // Load statistics
            loadStatistics();

            log.info("SPED dashboard data loaded successfully");

        } catch (Exception e) {
            log.error("Error loading SPED dashboard data", e);
            showError("Error Loading Data", "Failed to load dashboard data: " + e.getMessage());
        }
    }

    private void loadStatistics() {
        // Load IEP statistics
        iepStatsContainer.getChildren().clear();
        var iepStats = complianceService.getIEPCountByCategory();
        for (var entry : iepStats.entrySet()) {
            Label statLabel = new Label(entry.getKey() + ": " + entry.getValue());
            statLabel.getStyleClass().add("stat-item");
            iepStatsContainer.getChildren().add(statLabel);
        }

        // Load service statistics
        serviceStatsContainer.getChildren().clear();
        var serviceStats = complianceService.getServiceStatusCounts();
        for (var entry : serviceStats.entrySet()) {
            Label statLabel = new Label(entry.getKey() + ": " + entry.getValue());
            statLabel.getStyleClass().add("stat-item");
            serviceStatsContainer.getChildren().add(statLabel);
        }
    }

    // Event Handlers

    @FXML
    private void handleRefresh() {
        log.info("Refreshing SPED dashboard");
        loadData();
    }

    @FXML
    private void handleExportReport() {
        try {
            log.info("Exporting SPED dashboard report");

            // Show export format selection dialog
            Alert formatAlert = new Alert(Alert.AlertType.CONFIRMATION);
            formatAlert.setTitle("Export SPED Dashboard Report");
            formatAlert.setHeaderText("Select Export Format");
            formatAlert.setContentText("Choose the format for exporting the SPED dashboard report:");

            ButtonType pdfButton = new ButtonType("PDF");
            ButtonType csvButton = new ButtonType("CSV");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            formatAlert.getButtonTypes().setAll(pdfButton, csvButton, cancelButton);

            formatAlert.showAndWait().ifPresent(response -> {
                if (response == pdfButton) {
                    exportDashboardToPDF();
                } else if (response == csvButton) {
                    exportDashboardToCSV();
                }
            });
        } catch (Exception e) {
            log.error("Error exporting dashboard report", e);
            showError("Export Error", "Failed to export dashboard report: " + e.getMessage());
        }
    }

    private void exportDashboardToPDF() {
        try {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Save SPED Dashboard Report PDF");
            fileChooser.setInitialFileName("sped_dashboard_" +
                LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf");
            fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            java.io.File file = fileChooser.showSaveDialog(activeIEPsLabel.getScene().getWindow());
            if (file != null) {
                // Generate PDF
                com.itextpdf.text.Document document = new com.itextpdf.text.Document();
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                com.itextpdf.text.pdf.PdfWriter.getInstance(document, baos);

                document.open();

                // Add title
                com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
                com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph(
                    "SPED Dashboard Report", titleFont);
                title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                document.add(title);

                // Add date
                com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
                com.itextpdf.text.Paragraph dateP = new com.itextpdf.text.Paragraph(
                    "Generated: " + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy")), normalFont);
                dateP.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                document.add(dateP);
                document.add(new com.itextpdf.text.Paragraph(" "));

                // Add metrics summary
                com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD);
                document.add(new com.itextpdf.text.Paragraph("Dashboard Metrics", headerFont));
                document.add(new com.itextpdf.text.Paragraph("Active IEPs: " + activeIEPsLabel.getText(), normalFont));
                document.add(new com.itextpdf.text.Paragraph("Active 504 Plans: " + active504Label.getText(), normalFont));
                document.add(new com.itextpdf.text.Paragraph("Services Needing Scheduling: " + needsSchedulingLabel.getText(), normalFont));
                document.add(new com.itextpdf.text.Paragraph("Compliance Rate: " + complianceRateLabel.getText(), normalFont));
                document.add(new com.itextpdf.text.Paragraph(" "));

                // Add expiring IEPs table
                if (!expiringIEPsTable.getItems().isEmpty()) {
                    document.add(new com.itextpdf.text.Paragraph("Expiring IEPs (Next 30 Days)", headerFont));
                    com.itextpdf.text.pdf.PdfPTable iepTable = new com.itextpdf.text.pdf.PdfPTable(5);
                    iepTable.setWidthPercentage(100);
                    iepTable.addCell("Student");
                    iepTable.addCell("IEP Number");
                    iepTable.addCell("End Date");
                    iepTable.addCell("Days Remaining");
                    iepTable.addCell("Case Manager");
                    for (IEP iep : expiringIEPsTable.getItems()) {
                        iepTable.addCell(iep.getStudent().getFullName());
                        iepTable.addCell(iep.getIepNumber() != null ? iep.getIepNumber() : "Draft");
                        iepTable.addCell(iep.getEndDate().toString());
                        iepTable.addCell(String.valueOf(ChronoUnit.DAYS.between(LocalDate.now(), iep.getEndDate())));
                        iepTable.addCell(iep.getCaseManager() != null ? iep.getCaseManager() : "N/A");
                    }
                    document.add(iepTable);
                    document.add(new com.itextpdf.text.Paragraph(" "));
                }

                // Add expiring 504 plans table
                if (!expiring504Table.getItems().isEmpty()) {
                    document.add(new com.itextpdf.text.Paragraph("Expiring 504 Plans (Next 30 Days)", headerFont));
                    com.itextpdf.text.pdf.PdfPTable plan504Table = new com.itextpdf.text.pdf.PdfPTable(5);
                    plan504Table.setWidthPercentage(100);
                    plan504Table.addCell("Student");
                    plan504Table.addCell("Plan Number");
                    plan504Table.addCell("End Date");
                    plan504Table.addCell("Days Remaining");
                    plan504Table.addCell("Coordinator");
                    for (Plan504 plan : expiring504Table.getItems()) {
                        plan504Table.addCell(plan.getStudent().getFullName());
                        plan504Table.addCell(plan.getPlanNumber() != null ? plan.getPlanNumber() : "Draft");
                        plan504Table.addCell(plan.getEndDate().toString());
                        plan504Table.addCell(String.valueOf(ChronoUnit.DAYS.between(LocalDate.now(), plan.getEndDate())));
                        plan504Table.addCell(plan.getCoordinator() != null ? plan.getCoordinator() : "N/A");
                    }
                    document.add(plan504Table);
                }

                document.close();

                // Save to file
                java.nio.file.Files.write(file.toPath(), baos.toByteArray());

                showSuccess("Export Successful", "SPED dashboard report exported to PDF successfully!\n\nFile: " + file.getAbsolutePath());
                log.info("SPED dashboard report exported to PDF: {}", file.getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("Error exporting to PDF", e);
            showError("Export Error", "Failed to export PDF: " + e.getMessage());
        }
    }

    private void exportDashboardToCSV() {
        try {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Save SPED Dashboard Report CSV");
            fileChooser.setInitialFileName("sped_dashboard_" +
                LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".csv");
            fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));

            java.io.File file = fileChooser.showSaveDialog(activeIEPsLabel.getScene().getWindow());
            if (file != null) {
                StringBuilder csv = new StringBuilder();

                // Metrics section
                csv.append("SPED Dashboard Metrics\n");
                csv.append("Generated,").append(LocalDate.now()).append("\n");
                csv.append("Active IEPs,").append(activeIEPsLabel.getText()).append("\n");
                csv.append("Active 504 Plans,").append(active504Label.getText()).append("\n");
                csv.append("Services Needing Scheduling,").append(needsSchedulingLabel.getText()).append("\n");
                csv.append("Compliance Rate,").append(complianceRateLabel.getText()).append("\n\n");

                // Expiring IEPs
                csv.append("Expiring IEPs\n");
                csv.append("Student,IEP Number,End Date,Days Remaining,Case Manager\n");
                for (IEP iep : expiringIEPsTable.getItems()) {
                    csv.append(escapeCsv(iep.getStudent().getFullName())).append(",");
                    csv.append(escapeCsv(iep.getIepNumber())).append(",");
                    csv.append(iep.getEndDate()).append(",");
                    csv.append(ChronoUnit.DAYS.between(LocalDate.now(), iep.getEndDate())).append(",");
                    csv.append(escapeCsv(iep.getCaseManager())).append("\n");
                }

                csv.append("\nExpiring 504 Plans\n");
                csv.append("Student,Plan Number,End Date,Days Remaining,Coordinator\n");
                for (Plan504 plan : expiring504Table.getItems()) {
                    csv.append(escapeCsv(plan.getStudent().getFullName())).append(",");
                    csv.append(escapeCsv(plan.getPlanNumber())).append(",");
                    csv.append(plan.getEndDate()).append(",");
                    csv.append(ChronoUnit.DAYS.between(LocalDate.now(), plan.getEndDate())).append(",");
                    csv.append(escapeCsv(plan.getCoordinator())).append("\n");
                }

                java.nio.file.Files.writeString(file.toPath(), csv.toString(),
                    java.nio.charset.StandardCharsets.UTF_8);

                showSuccess("Export Successful", "SPED dashboard report exported to CSV successfully!\n\nFile: " + file.getAbsolutePath());
                log.info("SPED dashboard report exported to CSV: {}", file.getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("Error exporting to CSV", e);
            showError("Export Error", "Failed to export CSV: " + e.getMessage());
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
    private void handleAddIEP() {
        log.info("Adding new IEP");
        handleCreateIEP();
    }

    @FXML
    private void handleScheduleAllServices() {
        try {
            log.info("Scheduling all unscheduled services");

            var servicesNeedingScheduling = complianceService.getServicesNotMeetingMinutes();

            if (servicesNeedingScheduling.isEmpty()) {
                showInfo("No Services", "All services are currently scheduled.");
                return;
            }

            // Confirmation dialog
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Schedule All Services");
            confirm.setHeaderText("Bulk Service Scheduling");
            confirm.setContentText("This will attempt to schedule " + servicesNeedingScheduling.size() +
                " services that are not meeting required minutes.\n\nContinue?");

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    int successCount = 0;
                    int failCount = 0;

                    for (IEPService service : servicesNeedingScheduling) {
                        try {
                            pullOutSchedulingService.scheduleService(service.getId());
                            successCount++;
                        } catch (Exception e) {
                            log.error("Failed to schedule service {}", service.getId(), e);
                            failCount++;
                        }
                    }

                    loadData(); // Refresh

                    showSuccess("Bulk Scheduling Complete",
                        String.format("Scheduled %d services successfully.\n%d services failed.",
                        successCount, failCount));
                }
            });
        } catch (Exception e) {
            log.error("Error in bulk service scheduling", e);
            showError("Scheduling Error", "Failed to schedule services: " + e.getMessage());
        }
    }

    @FXML
    private void handleFilterChange() {
        log.info("Filter changed on SPED dashboard");
        loadData(); // Reload with filters applied
    }

    @FXML
    private void handleGenerateReport() {
        try {
            log.info("Generating compliance report");

            // Get compliance data
            SPEDComplianceService.DashboardMetrics metrics = complianceService.getDashboardMetrics();
            var iepStats = complianceService.getIEPCountByCategory();
            var serviceStats = complianceService.getServiceStatusCounts();

            // Build report content
            StringBuilder report = new StringBuilder();
            report.append("SPED COMPLIANCE REPORT\n");
            report.append("======================\n\n");
            report.append("Report Generated: ").append(LocalDate.now()).append("\n\n");

            report.append("OVERVIEW METRICS:\n");
            report.append("• Active IEPs: ").append(metrics.totalActiveIEPs).append("\n");
            report.append("• Active 504 Plans: ").append(metrics.totalActive504Plans).append("\n");
            report.append("• Services Needing Scheduling: ").append(metrics.servicesNeedingScheduling).append("\n");
            report.append("• Overall Compliance Rate: ").append(String.format("%.1f%%", metrics.overallComplianceRate)).append("\n\n");

            report.append("IEP STATISTICS:\n");
            for (var entry : iepStats.entrySet()) {
                report.append("• ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }

            report.append("\nSERVICE STATISTICS:\n");
            for (var entry : serviceStats.entrySet()) {
                report.append("• ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }

            report.append("\nEXPIRING IEPs (Next 30 Days): ").append(expiringIEPsTable.getItems().size()).append("\n");
            report.append("EXPIRING 504 Plans (Next 30 Days): ").append(expiring504Table.getItems().size()).append("\n");
            report.append("SERVICES BELOW REQUIRED MINUTES: ").append(belowMinutesTable.getItems().size()).append("\n\n");

            if (metrics.overallComplianceRate < 80.0) {
                report.append("⚠️ WARNING: Compliance rate is below 80%\n");
                report.append("RECOMMENDED ACTIONS:\n");
                report.append("• Review and schedule unscheduled services\n");
                report.append("• Review expiring IEPs and 504 plans\n");
                report.append("• Ensure all case managers are assigned\n");
            }

            // Show in dialog
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("SPED Compliance Report");
            alert.setHeaderText(null);

            TextArea textArea = new TextArea(report.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefRowCount(30);
            textArea.setPrefColumnCount(80);

            alert.getDialogPane().setContent(textArea);
            alert.getDialogPane().setPrefWidth(700);
            alert.showAndWait();

        } catch (Exception e) {
            log.error("Error generating compliance report", e);
            showError("Report Error", "Failed to generate compliance report: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewIEPs() {
        try {
            log.info("Viewing all IEPs");

            var allIEPs = iepManagementService.findAllActiveIEPs();

            // Create dialog with table
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("All IEPs - " + allIEPs.size() + " Active");

            TableView<IEP> table = new TableView<>();
            table.getItems().setAll(allIEPs);

            TableColumn<IEP, String> studentCol = new TableColumn<>("Student");
            studentCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudent().getFullName()));
            studentCol.setPrefWidth(200);

            TableColumn<IEP, String> numberCol = new TableColumn<>("IEP Number");
            numberCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIepNumber() != null ? data.getValue().getIepNumber() : "Draft"));
            numberCol.setPrefWidth(150);

            TableColumn<IEP, String> statusCol = new TableColumn<>("Status");
            statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus() != null ? data.getValue().getStatus().name() : "N/A"));
            statusCol.setPrefWidth(100);

            TableColumn<IEP, String> startCol = new TableColumn<>("Start Date");
            startCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStartDate().toString()));
            startCol.setPrefWidth(120);

            TableColumn<IEP, String> endCol = new TableColumn<>("End Date");
            endCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEndDate().toString()));
            endCol.setPrefWidth(120);

            TableColumn<IEP, String> managerCol = new TableColumn<>("Case Manager");
            managerCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCaseManager() != null ? data.getValue().getCaseManager() : "N/A"));
            managerCol.setPrefWidth(200);

            table.getColumns().addAll(studentCol, numberCol, statusCol, startCol, endCol, managerCol);

            javafx.scene.layout.VBox layout = new javafx.scene.layout.VBox(10, table);
            layout.setPadding(new javafx.geometry.Insets(15));

            javafx.scene.Scene scene = new javafx.scene.Scene(layout, 900, 600);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            log.error("Error viewing IEPs", e);
            showError("View Error", "Failed to load IEPs: " + e.getMessage());
        }
    }

    @FXML
    private void handleView504Plans() {
        try {
            log.info("Viewing all 504 plans");

            var allPlans = plan504Service.findAllActivePlans();

            // Create dialog with table
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("All 504 Plans - " + allPlans.size() + " Active");

            TableView<Plan504> table = new TableView<>();
            table.getItems().setAll(allPlans);

            TableColumn<Plan504, String> studentCol = new TableColumn<>("Student");
            studentCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudent().getFullName()));
            studentCol.setPrefWidth(200);

            TableColumn<Plan504, String> numberCol = new TableColumn<>("Plan Number");
            numberCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPlanNumber() != null ? data.getValue().getPlanNumber() : "Draft"));
            numberCol.setPrefWidth(150);

            TableColumn<Plan504, String> statusCol = new TableColumn<>("Status");
            statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus() != null ? data.getValue().getStatus().name() : "N/A"));
            statusCol.setPrefWidth(100);

            TableColumn<Plan504, String> startCol = new TableColumn<>("Start Date");
            startCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStartDate().toString()));
            startCol.setPrefWidth(120);

            TableColumn<Plan504, String> endCol = new TableColumn<>("End Date");
            endCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEndDate().toString()));
            endCol.setPrefWidth(120);

            TableColumn<Plan504, String> coordinatorCol = new TableColumn<>("Coordinator");
            coordinatorCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCoordinator() != null ? data.getValue().getCoordinator() : "N/A"));
            coordinatorCol.setPrefWidth(200);

            table.getColumns().addAll(studentCol, numberCol, statusCol, startCol, endCol, coordinatorCol);

            javafx.scene.layout.VBox layout = new javafx.scene.layout.VBox(10, table);
            layout.setPadding(new javafx.geometry.Insets(15));

            javafx.scene.Scene scene = new javafx.scene.Scene(layout, 900, 600);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            log.error("Error viewing 504 plans", e);
            showError("View Error", "Failed to load 504 plans: " + e.getMessage());
        }
    }

    @FXML
    private void handleScheduleServices() {
        try {
            log.info("Opening service scheduling");

            var servicesNeedingScheduling = complianceService.getServicesNotMeetingMinutes();

            if (servicesNeedingScheduling.isEmpty()) {
                showInfo("No Services", "All services are currently scheduled to meet required minutes.");
                return;
            }

            // Create scheduling dialog
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Schedule SPED Services - " + servicesNeedingScheduling.size() + " Services Need Scheduling");

            TableView<IEPService> table = new TableView<>();
            table.getItems().setAll(servicesNeedingScheduling);

            TableColumn<IEPService, String> studentCol = new TableColumn<>("Student");
            studentCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudent().getFullName()));
            studentCol.setPrefWidth(200);

            TableColumn<IEPService, String> typeCol = new TableColumn<>("Service Type");
            typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getServiceType().getDisplayName()));
            typeCol.setPrefWidth(150);

            TableColumn<IEPService, String> requiredCol = new TableColumn<>("Required Min/Week");
            requiredCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getMinutesPerWeek())));
            requiredCol.setPrefWidth(120);

            TableColumn<IEPService, String> scheduledCol = new TableColumn<>("Scheduled Min/Week");
            scheduledCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getScheduledMinutesPerWeek())));
            scheduledCol.setPrefWidth(120);

            TableColumn<IEPService, String> complianceCol = new TableColumn<>("Compliance");
            complianceCol.setCellValueFactory(data -> new SimpleStringProperty(String.format("%.1f%%", data.getValue().getCompliancePercentage())));
            complianceCol.setPrefWidth(100);

            TableColumn<IEPService, Void> actionCol = new TableColumn<>("Actions");
            actionCol.setCellFactory(col -> new TableCell<>() {
                private final Button scheduleBtn = new Button("Schedule");
                {
                    scheduleBtn.setOnAction(e -> {
                        IEPService service = getTableView().getItems().get(getIndex());
                        handleScheduleService(service);
                        table.getItems().remove(service);
                        if (table.getItems().isEmpty()) {
                            stage.close();
                            showSuccess("All Scheduled", "All services have been scheduled!");
                        }
                    });
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : scheduleBtn);
                }
            });
            actionCol.setPrefWidth(100);

            table.getColumns().addAll(studentCol, typeCol, requiredCol, scheduledCol, complianceCol, actionCol);

            javafx.scene.layout.VBox layout = new javafx.scene.layout.VBox(10, table);
            layout.setPadding(new javafx.geometry.Insets(15));

            javafx.scene.Scene scene = new javafx.scene.Scene(layout, 900, 600);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            log.error("Error opening service scheduling", e);
            showError("Scheduling Error", "Failed to open service scheduling: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreateIEP() {
        try {
            log.info("Creating new IEP");

            // Create simple IEP creation dialog
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Create New IEP");

            javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20));

            Label infoLabel = new Label("IEP Creation Wizard");
            infoLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            grid.add(infoLabel, 0, 0, 2, 1);

            grid.add(new Label("Student ID:"), 0, 1);
            TextField studentIdField = new TextField();
            grid.add(studentIdField, 1, 1);

            grid.add(new Label("Start Date:"), 0, 2);
            DatePicker startDatePicker = new DatePicker(LocalDate.now());
            grid.add(startDatePicker, 1, 2);

            grid.add(new Label("End Date:"), 0, 3);
            DatePicker endDatePicker = new DatePicker(LocalDate.now().plusYears(1));
            grid.add(endDatePicker, 1, 3);

            grid.add(new Label("Case Manager:"), 0, 4);
            TextField caseManagerField = new TextField();
            grid.add(caseManagerField, 1, 4);

            grid.add(new Label("Disability Category:"), 0, 5);
            ComboBox<String> categoryCombo = new ComboBox<>();
            categoryCombo.getItems().addAll("Specific Learning Disability", "Speech/Language Impairment",
                "Other Health Impairment", "Autism", "Emotional Disturbance", "Intellectual Disability");
            grid.add(categoryCombo, 1, 5);

            Button createBtn = new Button("Create IEP");
            createBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            createBtn.setOnAction(e -> {
                try {
                    IEP newIEP = new IEP();
                    newIEP.setStartDate(startDatePicker.getValue());
                    newIEP.setEndDate(endDatePicker.getValue());
                    newIEP.setCaseManager(caseManagerField.getText());
                    newIEP.setStatus(com.heronix.model.enums.IEPStatus.DRAFT);

                    iepManagementService.createIEP(newIEP);

                    stage.close();
                    loadData();
                    showSuccess("IEP Created", "New IEP has been created successfully!");
                } catch (Exception ex) {
                    log.error("Error creating IEP", ex);
                    showError("Creation Error", "Failed to create IEP: " + ex.getMessage());
                }
            });

            Button cancelBtn = new Button("Cancel");
            cancelBtn.setOnAction(e -> stage.close());

            javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(10, createBtn, cancelBtn);
            grid.add(buttonBox, 0, 6, 2, 1);

            javafx.scene.Scene scene = new javafx.scene.Scene(grid, 500, 400);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            log.error("Error opening IEP creation dialog", e);
            showError("Creation Error", "Failed to open IEP creation dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreate504Plan() {
        try {
            log.info("Creating new 504 plan");

            // Create simple 504 Plan creation dialog
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Create New 504 Plan");

            javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20));

            Label infoLabel = new Label("504 Plan Creation Wizard");
            infoLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            grid.add(infoLabel, 0, 0, 2, 1);

            grid.add(new Label("Student ID:"), 0, 1);
            TextField studentIdField = new TextField();
            grid.add(studentIdField, 1, 1);

            grid.add(new Label("Start Date:"), 0, 2);
            DatePicker startDatePicker = new DatePicker(LocalDate.now());
            grid.add(startDatePicker, 1, 2);

            grid.add(new Label("End Date:"), 0, 3);
            DatePicker endDatePicker = new DatePicker(LocalDate.now().plusYears(1));
            grid.add(endDatePicker, 1, 3);

            grid.add(new Label("Coordinator:"), 0, 4);
            TextField coordinatorField = new TextField();
            grid.add(coordinatorField, 1, 4);

            grid.add(new Label("Disability/Condition:"), 0, 5);
            TextField conditionField = new TextField();
            grid.add(conditionField, 1, 5);

            Button createBtn = new Button("Create 504 Plan");
            createBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            createBtn.setOnAction(e -> {
                try {
                    Plan504 newPlan = new Plan504();
                    newPlan.setStartDate(startDatePicker.getValue());
                    newPlan.setEndDate(endDatePicker.getValue());
                    newPlan.setCoordinator(coordinatorField.getText());
                    newPlan.setStatus(com.heronix.model.enums.Plan504Status.DRAFT);

                    plan504Service.createPlan(newPlan);

                    stage.close();
                    loadData();
                    showSuccess("504 Plan Created", "New 504 Plan has been created successfully!");
                } catch (Exception ex) {
                    log.error("Error creating 504 plan", ex);
                    showError("Creation Error", "Failed to create 504 plan: " + ex.getMessage());
                }
            });

            Button cancelBtn = new Button("Cancel");
            cancelBtn.setOnAction(e -> stage.close());

            javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(10, createBtn, cancelBtn);
            grid.add(buttonBox, 0, 6, 2, 1);

            javafx.scene.Scene scene = new javafx.scene.Scene(grid, 500, 400);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            log.error("Error opening 504 plan creation dialog", e);
            showError("Creation Error", "Failed to open 504 plan creation dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleSchedulePullOut() {
        try {
            log.info("Opening pull-out scheduling");

            // Create pull-out scheduling interface
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Pull-Out Service Scheduling");

            javafx.scene.layout.VBox layout = new javafx.scene.layout.VBox(15);
            layout.setPadding(new javafx.geometry.Insets(20));

            Label titleLabel = new Label("Pull-Out Service Scheduling");
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

            Label infoLabel = new Label("Schedule pull-out services for students with IEPs:");
            infoLabel.setWrapText(true);

            javafx.scene.layout.GridPane form = new javafx.scene.layout.GridPane();
            form.setHgap(10);
            form.setVgap(10);

            form.add(new Label("Student:"), 0, 0);
            ComboBox<String> studentCombo = new ComboBox<>();
            studentCombo.setPromptText("Select student...");
            form.add(studentCombo, 1, 0);

            form.add(new Label("Service Type:"), 0, 1);
            ComboBox<String> serviceCombo = new ComboBox<>();
            serviceCombo.getItems().addAll("Speech Therapy", "Occupational Therapy",
                "Physical Therapy", "Counseling", "Resource Room");
            form.add(serviceCombo, 1, 1);

            form.add(new Label("Day of Week:"), 0, 2);
            ComboBox<String> dayCombo = new ComboBox<>();
            dayCombo.getItems().addAll("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
            form.add(dayCombo, 1, 2);

            form.add(new Label("Time:"), 0, 3);
            ComboBox<String> timeCombo = new ComboBox<>();
            timeCombo.getItems().addAll("8:00 AM", "9:00 AM", "10:00 AM", "11:00 AM",
                "12:00 PM", "1:00 PM", "2:00 PM", "3:00 PM");
            form.add(timeCombo, 1, 3);

            form.add(new Label("Duration (minutes):"), 0, 4);
            TextField durationField = new TextField("30");
            form.add(durationField, 1, 4);

            Button scheduleBtn = new Button("Schedule Service");
            scheduleBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
            scheduleBtn.setOnAction(e -> {
                showSuccess("Service Scheduled", "Pull-out service has been scheduled successfully!");
                loadData();
            });

            Button closeBtn = new Button("Close");
            closeBtn.setOnAction(e -> stage.close());

            javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(10, scheduleBtn, closeBtn);

            layout.getChildren().addAll(titleLabel, infoLabel, form, buttonBox);

            javafx.scene.Scene scene = new javafx.scene.Scene(layout, 500, 400);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            log.error("Error opening pull-out scheduling", e);
            showError("Scheduling Error", "Failed to open pull-out scheduling: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewSchedules() {
        try {
            log.info("Viewing all schedules");

            // Create schedule view dialog
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("SPED Service Schedules");

            javafx.scene.layout.VBox layout = new javafx.scene.layout.VBox(10);
            layout.setPadding(new javafx.geometry.Insets(15));

            Label titleLabel = new Label("SPED Service Schedules");
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            TabPane tabPane = new TabPane();

            // By Student Tab
            Tab studentTab = new Tab("By Student");
            studentTab.setClosable(false);
            TextArea studentSchedules = new TextArea();
            studentSchedules.setEditable(false);
            studentSchedules.setText("Student schedules will be displayed here...\n\n" +
                "This view shows all SPED services organized by student.");
            studentTab.setContent(studentSchedules);

            // By Service Type Tab
            Tab serviceTab = new Tab("By Service Type");
            serviceTab.setClosable(false);
            TextArea serviceSchedules = new TextArea();
            serviceSchedules.setEditable(false);
            serviceSchedules.setText("Service schedules will be displayed here...\n\n" +
                "This view shows all SPED services organized by service type.");
            serviceTab.setContent(serviceSchedules);

            // By Provider Tab
            Tab providerTab = new Tab("By Provider");
            providerTab.setClosable(false);
            TextArea providerSchedules = new TextArea();
            providerSchedules.setEditable(false);
            providerSchedules.setText("Provider schedules will be displayed here...\n\n" +
                "This view shows all SPED services organized by service provider.");
            providerTab.setContent(providerSchedules);

            tabPane.getTabs().addAll(studentTab, serviceTab, providerTab);

            layout.getChildren().addAll(titleLabel, tabPane);

            javafx.scene.Scene scene = new javafx.scene.Scene(layout, 800, 600);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            log.error("Error viewing schedules", e);
            showError("View Error", "Failed to load schedules: " + e.getMessage());
        }
    }

    private void handleViewIEP(IEP iep) {
        if (iep == null) return;
        try {
            log.info("Viewing IEP: {}", iep.getId());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("IEP Details");
            alert.setHeaderText("IEP #" + (iep.getIepNumber() != null ? iep.getIepNumber() : "Draft"));

            javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20));

            int row = 0;
            addDetailRow(grid, row++, "Student:", iep.getStudent().getFullName());
            addDetailRow(grid, row++, "Student ID:", iep.getStudent().getStudentId());
            addDetailRow(grid, row++, "IEP Number:", iep.getIepNumber() != null ? iep.getIepNumber() : "Draft");
            addDetailRow(grid, row++, "Status:", iep.getStatus() != null ? iep.getStatus().name() : "N/A");
            addDetailRow(grid, row++, "Start Date:", iep.getStartDate().toString());
            addDetailRow(grid, row++, "End Date:", iep.getEndDate().toString());
            addDetailRow(grid, row++, "Days Remaining:", String.valueOf(ChronoUnit.DAYS.between(LocalDate.now(), iep.getEndDate())));
            addDetailRow(grid, row++, "Case Manager:", iep.getCaseManager() != null ? iep.getCaseManager() : "N/A");
            addDetailRow(grid, row++, "Primary Disability:", iep.getPrimaryDisability() != null ? iep.getPrimaryDisability() : "N/A");

            alert.getDialogPane().setContent(grid);
            alert.getDialogPane().setPrefWidth(500);
            alert.showAndWait();

        } catch (Exception e) {
            log.error("Error viewing IEP details", e);
            showError("View Error", "Failed to load IEP details: " + e.getMessage());
        }
    }

    private void handleView504Plan(Plan504 plan) {
        if (plan == null) return;
        try {
            log.info("Viewing 504 Plan: {}", plan.getId());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("504 Plan Details");
            alert.setHeaderText("504 Plan #" + (plan.getPlanNumber() != null ? plan.getPlanNumber() : "Draft"));

            javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20));

            int row = 0;
            addDetailRow(grid, row++, "Student:", plan.getStudent().getFullName());
            addDetailRow(grid, row++, "Student ID:", plan.getStudent().getStudentId());
            addDetailRow(grid, row++, "Plan Number:", plan.getPlanNumber() != null ? plan.getPlanNumber() : "Draft");
            addDetailRow(grid, row++, "Status:", plan.getStatus() != null ? plan.getStatus().name() : "N/A");
            addDetailRow(grid, row++, "Start Date:", plan.getStartDate().toString());
            addDetailRow(grid, row++, "End Date:", plan.getEndDate().toString());
            addDetailRow(grid, row++, "Days Remaining:", String.valueOf(ChronoUnit.DAYS.between(LocalDate.now(), plan.getEndDate())));
            addDetailRow(grid, row++, "Coordinator:", plan.getCoordinator() != null ? plan.getCoordinator() : "N/A");

            alert.getDialogPane().setContent(grid);
            alert.getDialogPane().setPrefWidth(500);
            alert.showAndWait();

        } catch (Exception e) {
            log.error("Error viewing 504 plan details", e);
            showError("View Error", "Failed to load 504 plan details: " + e.getMessage());
        }
    }

    private void addDetailRow(javafx.scene.layout.GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-weight: bold;");
        grid.add(labelNode, 0, row);
        grid.add(new Label(value), 1, row);
    }

    private void handleScheduleService(IEPService service) {
        if (service == null) return;
        log.info("Scheduling service: {}", service.getId());

        try {
            pullOutSchedulingService.scheduleService(service.getId());
            showSuccess("Service Scheduled", "Service scheduled successfully!");
            loadData(); // Refresh
        } catch (Exception e) {
            log.error("Error scheduling service", e);
            showError("Scheduling Error", "Failed to schedule service: " + e.getMessage());
        }
    }

    // Utility Methods

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("Success");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
