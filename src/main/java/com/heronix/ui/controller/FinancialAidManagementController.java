package com.heronix.ui.controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class FinancialAidManagementController {

    // Top statistics labels
    @FXML private Label totalApplicationsLabel;
    @FXML private Label approvedLabel;
    @FXML private Label pendingLabel;
    @FXML private Label totalAidLabel;

    // Applications Tab
    @FXML private TextField applicationSearchField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private TableView<FinancialAidApplication> applicationsTableView;
    @FXML private TableColumn<FinancialAidApplication, String> appIdColumn;
    @FXML private TableColumn<FinancialAidApplication, String> studentNameColumn;
    @FXML private TableColumn<FinancialAidApplication, String> studentIdColumn;
    @FXML private TableColumn<FinancialAidApplication, String> appDateColumn;
    @FXML private TableColumn<FinancialAidApplication, String> aidTypeColumn;
    @FXML private TableColumn<FinancialAidApplication, String> amountRequestedColumn;
    @FXML private TableColumn<FinancialAidApplication, String> amountApprovedColumn;
    @FXML private TableColumn<FinancialAidApplication, String> statusColumn;
    @FXML private TableColumn<FinancialAidApplication, Void> actionsColumn;

    // Programs Tab
    @FXML private TableView<AidProgram> programsTableView;
    @FXML private TableColumn<AidProgram, String> programNameColumn;
    @FXML private TableColumn<AidProgram, String> programTypeColumn;
    @FXML private TableColumn<AidProgram, String> maxAwardColumn;
    @FXML private TableColumn<AidProgram, String> budgetColumn;
    @FXML private TableColumn<AidProgram, String> awardedYtdColumn;
    @FXML private TableColumn<AidProgram, String> remainingColumn;
    @FXML private TableColumn<AidProgram, Integer> recipientsColumn;
    @FXML private TableColumn<AidProgram, String> programStatusColumn;

    // Disbursements Tab
    @FXML private ComboBox<String> academicYearCombo;
    @FXML private ComboBox<String> semesterCombo;
    @FXML private TableView<Disbursement> disbursementsTableView;
    @FXML private TableColumn<Disbursement, String> disbStudentColumn;
    @FXML private TableColumn<Disbursement, String> disbStudentIdColumn;
    @FXML private TableColumn<Disbursement, String> disbProgramColumn;
    @FXML private TableColumn<Disbursement, String> disbAmountColumn;
    @FXML private TableColumn<Disbursement, String> disbDateColumn;
    @FXML private TableColumn<Disbursement, String> disbMethodColumn;
    @FXML private TableColumn<Disbursement, String> disbStatusColumn;
    @FXML private TableColumn<Disbursement, String> referenceColumn;

    // Reports Tab
    @FXML private PieChart aidDistributionChart;
    @FXML private Label fafsaRateLabel;
    @FXML private Label avgAwardLabel;
    @FXML private Label renewalRateLabel;
    @FXML private Label needMetLabel;

    private ObservableList<FinancialAidApplication> applications = FXCollections.observableArrayList();
    private ObservableList<AidProgram> programs = FXCollections.observableArrayList();
    private ObservableList<Disbursement> disbursements = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialize status filter
        statusFilterCombo.setItems(FXCollections.observableArrayList(
            "All", "Pending", "Under Review", "Approved", "Denied", "Disbursed"
        ));
        statusFilterCombo.setValue("All");

        // Initialize academic year and semester
        academicYearCombo.setItems(FXCollections.observableArrayList(
            "2024-2025", "2025-2026", "2026-2027"
        ));
        academicYearCombo.setValue("2024-2025");

        semesterCombo.setItems(FXCollections.observableArrayList(
            "Fall", "Spring", "Summer"
        ));
        semesterCombo.setValue("Fall");

        // Set up Applications table
        setupApplicationsTable();

        // Set up Programs table
        setupProgramsTable();

        // Set up Disbursements table
        setupDisbursementsTable();

        // Load sample data
        loadSampleData();

        // Set up pie chart
        setupPieChart();
    }

    private void setupApplicationsTable() {
        appIdColumn.setCellValueFactory(new PropertyValueFactory<>("applicationId"));
        studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        studentIdColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        appDateColumn.setCellValueFactory(new PropertyValueFactory<>("applicationDate"));
        aidTypeColumn.setCellValueFactory(new PropertyValueFactory<>("aidType"));
        amountRequestedColumn.setCellValueFactory(new PropertyValueFactory<>("amountRequested"));
        amountApprovedColumn.setCellValueFactory(new PropertyValueFactory<>("amountApproved"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Color-code status column
        statusColumn.setCellFactory(column -> new TableCell<FinancialAidApplication, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String style = "-fx-padding: 5; -fx-border-radius: 3; -fx-background-radius: 3; ";
                    switch (item) {
                        case "Approved":
                            style += "-fx-background-color: #4caf50; -fx-text-fill: white;";
                            break;
                        case "Disbursed":
                            style += "-fx-background-color: #2196f3; -fx-text-fill: white;";
                            break;
                        case "Under Review":
                            style += "-fx-background-color: #ff9800; -fx-text-fill: white;";
                            break;
                        case "Pending":
                            style += "-fx-background-color: #9e9e9e; -fx-text-fill: white;";
                            break;
                        case "Denied":
                            style += "-fx-background-color: #f44336; -fx-text-fill: white;";
                            break;
                    }
                    setStyle(style);
                }
            }
        });

        // Add action buttons
        actionsColumn.setCellFactory(column -> new TableCell<FinancialAidApplication, Void>() {
            private final Button reviewBtn = new Button("Review");
            private final HBox buttons = new HBox(5, reviewBtn);

            {
                buttons.setAlignment(Pos.CENTER);
                reviewBtn.setStyle("-fx-background-color: #00796b; -fx-text-fill: white; -fx-font-size: 10px;");
                reviewBtn.setOnAction(event -> {
                    FinancialAidApplication app = getTableView().getItems().get(getIndex());
                    handleReviewApplication(app);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });

        applicationsTableView.setItems(applications);
    }

    private void setupProgramsTable() {
        programNameColumn.setCellValueFactory(new PropertyValueFactory<>("programName"));
        programTypeColumn.setCellValueFactory(new PropertyValueFactory<>("programType"));
        maxAwardColumn.setCellValueFactory(new PropertyValueFactory<>("maxAward"));
        budgetColumn.setCellValueFactory(new PropertyValueFactory<>("budget"));
        awardedYtdColumn.setCellValueFactory(new PropertyValueFactory<>("awardedYtd"));
        remainingColumn.setCellValueFactory(new PropertyValueFactory<>("remaining"));
        recipientsColumn.setCellValueFactory(new PropertyValueFactory<>("recipients"));
        programStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Color-code status
        programStatusColumn.setCellFactory(column -> new TableCell<AidProgram, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String style = "-fx-padding: 5; -fx-border-radius: 3; -fx-background-radius: 3; ";
                    if ("Active".equals(item)) {
                        style += "-fx-background-color: #4caf50; -fx-text-fill: white;";
                    } else {
                        style += "-fx-background-color: #9e9e9e; -fx-text-fill: white;";
                    }
                    setStyle(style);
                }
            }
        });

        programsTableView.setItems(programs);
    }

    private void setupDisbursementsTable() {
        disbStudentColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        disbStudentIdColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        disbProgramColumn.setCellValueFactory(new PropertyValueFactory<>("programName"));
        disbAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        disbDateColumn.setCellValueFactory(new PropertyValueFactory<>("disbursementDate"));
        disbMethodColumn.setCellValueFactory(new PropertyValueFactory<>("method"));
        disbStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        referenceColumn.setCellValueFactory(new PropertyValueFactory<>("referenceNumber"));

        // Color-code status
        disbStatusColumn.setCellFactory(column -> new TableCell<Disbursement, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String style = "-fx-padding: 5; -fx-border-radius: 3; -fx-background-radius: 3; ";
                    switch (item) {
                        case "Completed":
                            style += "-fx-background-color: #4caf50; -fx-text-fill: white;";
                            break;
                        case "Processing":
                            style += "-fx-background-color: #ff9800; -fx-text-fill: white;";
                            break;
                        case "Scheduled":
                            style += "-fx-background-color: #2196f3; -fx-text-fill: white;";
                            break;
                    }
                    setStyle(style);
                }
            }
        });

        disbursementsTableView.setItems(disbursements);
    }

    private void loadSampleData() {
        // Sample applications
        applications.addAll(
            new FinancialAidApplication("FA-2025-001", "Emily Rodriguez", "STU-2025-001", "2024-11-15", "Pell Grant", "$6,895", "$5,500", "Approved"),
            new FinancialAidApplication("FA-2025-002", "Michael Chen", "STU-2025-002", "2024-11-18", "State Grant", "$4,200", "$3,800", "Approved"),
            new FinancialAidApplication("FA-2025-003", "Sarah Johnson", "STU-2025-003", "2024-12-01", "Merit Scholarship", "$8,000", "$0", "Under Review"),
            new FinancialAidApplication("FA-2025-004", "David Williams", "STU-2025-004", "2024-12-05", "Work Study", "$3,500", "$3,500", "Disbursed"),
            new FinancialAidApplication("FA-2025-005", "Jessica Brown", "STU-2025-005", "2024-12-10", "Institutional Aid", "$5,000", "$0", "Pending"),
            new FinancialAidApplication("FA-2025-006", "Christopher Lee", "STU-2025-006", "2024-12-12", "Athletic Scholarship", "$12,000", "$12,000", "Approved"),
            new FinancialAidApplication("FA-2025-007", "Amanda Taylor", "STU-2025-007", "2024-12-15", "Pell Grant", "$6,495", "$0", "Denied"),
            new FinancialAidApplication("FA-2025-008", "Ryan Martinez", "STU-2025-008", "2024-12-18", "State Grant", "$3,800", "$3,800", "Disbursed")
        );

        // Sample programs
        programs.addAll(
            new AidProgram("Federal Pell Grant", "Federal", "$7,395", "$500,000", "$342,580", "$157,420", 62, "Active"),
            new AidProgram("State Opportunity Grant", "State", "$5,000", "$250,000", "$198,400", "$51,600", 52, "Active"),
            new AidProgram("Presidential Merit Scholarship", "Institutional", "$15,000", "$300,000", "$195,000", "$105,000", 13, "Active"),
            new AidProgram("Athletic Scholarships", "Institutional", "$20,000", "$400,000", "$384,000", "$16,000", 24, "Active"),
            new AidProgram("Work Study Program", "Federal", "$4,000", "$150,000", "$106,370", "$43,630", 35, "Active"),
            new AidProgram("Emergency Aid Fund", "Institutional", "$2,500", "$50,000", "$18,750", "$31,250", 8, "Active")
        );

        // Sample disbursements
        disbursements.addAll(
            new Disbursement("Emily Rodriguez", "STU-2025-001", "Federal Pell Grant", "$2,750", "2025-01-15", "Direct Deposit", "Completed", "DISB-2025-001"),
            new Disbursement("Michael Chen", "STU-2025-002", "State Opportunity Grant", "$1,900", "2025-01-15", "Direct Deposit", "Completed", "DISB-2025-002"),
            new Disbursement("David Williams", "STU-2025-004", "Work Study Program", "$1,750", "2025-01-10", "Biweekly Payroll", "Processing", "DISB-2025-003"),
            new Disbursement("Christopher Lee", "STU-2025-006", "Athletic Scholarships", "$6,000", "2025-01-20", "Account Credit", "Scheduled", "DISB-2025-004")
        );
    }

    private void setupPieChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Pell Grants", 342580),
            new PieChart.Data("State Grants", 198400),
            new PieChart.Data("Merit Scholarships", 195000),
            new PieChart.Data("Athletic", 384000),
            new PieChart.Data("Work Study", 106370),
            new PieChart.Data("Other", 18750)
        );
        aidDistributionChart.setData(pieChartData);
        aidDistributionChart.setLegendVisible(true);
    }

    @FXML
    private void handleSearchApplications() {
        String searchText = applicationSearchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            applicationsTableView.setItems(applications);
        } else {
            ObservableList<FinancialAidApplication> filtered = applications.filtered(app ->
                app.getStudentName().toLowerCase().contains(searchText) ||
                app.getStudentId().toLowerCase().contains(searchText) ||
                app.getApplicationId().toLowerCase().contains(searchText)
            );
            applicationsTableView.setItems(filtered);
        }
    }

    @FXML
    private void handleNewApplication() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("New Application");
        alert.setHeaderText("Create Financial Aid Application");
        alert.setContentText("Financial aid application wizard will open.");
        alert.showAndWait();
    }

    private void handleReviewApplication(FinancialAidApplication app) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Review Application");
        alert.setHeaderText("Application: " + app.getApplicationId());
        alert.setContentText("Reviewing application for " + app.getStudentName() + "\n" +
                             "Aid Type: " + app.getAidType() + "\n" +
                             "Amount Requested: " + app.getAmountRequested());
        alert.showAndWait();
    }

    @FXML
    private void handleCreateProgram() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Create Program");
        alert.setContentText("Create new financial aid program dialog will open.");
        alert.showAndWait();
    }

    @FXML
    private void handleEditProgram() {
        AidProgram selected = programsTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Edit Program");
            alert.setContentText("Editing program: " + selected.getProgramName());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleDeactivateProgram() {
        AidProgram selected = programsTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Deactivate Program");
            confirm.setContentText("Are you sure you want to deactivate: " + selected.getProgramName() + "?");
            confirm.showAndWait();
        }
    }

    @FXML
    private void handleProcessDisbursements() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Process Disbursements");
        alert.setContentText("Processing disbursements for " + academicYearCombo.getValue() + " - " + semesterCombo.getValue());
        alert.showAndWait();
    }

    @FXML
    private void handleFafsaReport() {
        showReportDialog("FAFSA Completion Report");
    }

    @FXML
    private void handleAidSummaryReport() {
        showReportDialog("Financial Aid Summary Report");
    }

    @FXML
    private void handleComplianceReport() {
        showReportDialog("Compliance Report");
    }

    @FXML
    private void handleExportData() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export Data");
        alert.setContentText("Exporting all financial aid data to CSV...");
        alert.showAndWait();
    }

    private void showReportDialog(String reportName) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Generate Report");
        alert.setHeaderText(reportName);
        alert.setContentText("Report will be generated and opened in your default PDF viewer.");
        alert.showAndWait();
    }

    // Inner classes for data models
    public static class FinancialAidApplication {
        private final SimpleStringProperty applicationId;
        private final SimpleStringProperty studentName;
        private final SimpleStringProperty studentId;
        private final SimpleStringProperty applicationDate;
        private final SimpleStringProperty aidType;
        private final SimpleStringProperty amountRequested;
        private final SimpleStringProperty amountApproved;
        private final SimpleStringProperty status;

        public FinancialAidApplication(String applicationId, String studentName, String studentId,
                                      String applicationDate, String aidType, String amountRequested,
                                      String amountApproved, String status) {
            this.applicationId = new SimpleStringProperty(applicationId);
            this.studentName = new SimpleStringProperty(studentName);
            this.studentId = new SimpleStringProperty(studentId);
            this.applicationDate = new SimpleStringProperty(applicationDate);
            this.aidType = new SimpleStringProperty(aidType);
            this.amountRequested = new SimpleStringProperty(amountRequested);
            this.amountApproved = new SimpleStringProperty(amountApproved);
            this.status = new SimpleStringProperty(status);
        }

        public String getApplicationId() { return applicationId.get(); }
        public String getStudentName() { return studentName.get(); }
        public String getStudentId() { return studentId.get(); }
        public String getApplicationDate() { return applicationDate.get(); }
        public String getAidType() { return aidType.get(); }
        public String getAmountRequested() { return amountRequested.get(); }
        public String getAmountApproved() { return amountApproved.get(); }
        public String getStatus() { return status.get(); }
    }

    public static class AidProgram {
        private final SimpleStringProperty programName;
        private final SimpleStringProperty programType;
        private final SimpleStringProperty maxAward;
        private final SimpleStringProperty budget;
        private final SimpleStringProperty awardedYtd;
        private final SimpleStringProperty remaining;
        private final int recipients;
        private final SimpleStringProperty status;

        public AidProgram(String programName, String programType, String maxAward, String budget,
                         String awardedYtd, String remaining, int recipients, String status) {
            this.programName = new SimpleStringProperty(programName);
            this.programType = new SimpleStringProperty(programType);
            this.maxAward = new SimpleStringProperty(maxAward);
            this.budget = new SimpleStringProperty(budget);
            this.awardedYtd = new SimpleStringProperty(awardedYtd);
            this.remaining = new SimpleStringProperty(remaining);
            this.recipients = recipients;
            this.status = new SimpleStringProperty(status);
        }

        public String getProgramName() { return programName.get(); }
        public String getProgramType() { return programType.get(); }
        public String getMaxAward() { return maxAward.get(); }
        public String getBudget() { return budget.get(); }
        public String getAwardedYtd() { return awardedYtd.get(); }
        public String getRemaining() { return remaining.get(); }
        public int getRecipients() { return recipients; }
        public String getStatus() { return status.get(); }
    }

    public static class Disbursement {
        private final SimpleStringProperty studentName;
        private final SimpleStringProperty studentId;
        private final SimpleStringProperty programName;
        private final SimpleStringProperty amount;
        private final SimpleStringProperty disbursementDate;
        private final SimpleStringProperty method;
        private final SimpleStringProperty status;
        private final SimpleStringProperty referenceNumber;

        public Disbursement(String studentName, String studentId, String programName, String amount,
                          String disbursementDate, String method, String status, String referenceNumber) {
            this.studentName = new SimpleStringProperty(studentName);
            this.studentId = new SimpleStringProperty(studentId);
            this.programName = new SimpleStringProperty(programName);
            this.amount = new SimpleStringProperty(amount);
            this.disbursementDate = new SimpleStringProperty(disbursementDate);
            this.method = new SimpleStringProperty(method);
            this.status = new SimpleStringProperty(status);
            this.referenceNumber = new SimpleStringProperty(referenceNumber);
        }

        public String getStudentName() { return studentName.get(); }
        public String getStudentId() { return studentId.get(); }
        public String getProgramName() { return programName.get(); }
        public String getAmount() { return amount.get(); }
        public String getDisbursementDate() { return disbursementDate.get(); }
        public String getMethod() { return method.get(); }
        public String getStatus() { return status.get(); }
        public String getReferenceNumber() { return referenceNumber.get(); }
    }
}
