package com.heronix.ui.controller;

import com.heronix.model.domain.Student;
import com.heronix.model.domain.Teacher;
import com.heronix.model.domain.CounselingReferral;
import com.heronix.service.CounselingManagementService;
import com.heronix.service.CounselingSessionService;
import com.heronix.service.export.AnalyticsExportService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.heronix.util.ResponsiveDesignHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for the Counseling Services Dashboard.
 * Provides comprehensive case management, referral tracking, and analytics for counselors.
 */
@Component
public class CounselingDashboardController {
    private static final Logger logger = LoggerFactory.getLogger(CounselingDashboardController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    @Autowired
    private CounselingManagementService counselingManagementService;

    @Autowired
    private CounselingSessionService counselingSessionService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private AnalyticsExportService analyticsExportService;

    // Statistics Labels
    @FXML private Label totalReferralsLabel;
    @FXML private Label totalReferralsChangeLabel;
    @FXML private Label pendingReferralsLabel;
    @FXML private Label highRiskLabel;
    @FXML private Label activeCasesLabel;
    @FXML private Label overdueLabel;
    @FXML private Label totalSessionsLabel;
    @FXML private Label sessionsThisMonthLabel;

    // Filters
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> urgencyFilterComboBox;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private ComboBox<Teacher> counselorFilterComboBox;
    @FXML private ComboBox<String> typeFilterComboBox;
    @FXML private TextField searchField;

    // Charts
    @FXML private PieChart urgencyPieChart;
    @FXML private PieChart concernPieChart;
    @FXML private PieChart workloadPieChart;
    @FXML private BarChart<String, Number> trendChart;

    // Table
    @FXML private TableView<CounselingReferral> referralsTable;
    @FXML private TableColumn<CounselingReferral, LocalDate> dateColumn;
    @FXML private TableColumn<CounselingReferral, String> studentColumn;
    @FXML private TableColumn<CounselingReferral, String> gradeColumn;
    @FXML private TableColumn<CounselingReferral, String> urgencyColumn;
    @FXML private TableColumn<CounselingReferral, String> concernColumn;
    @FXML private TableColumn<CounselingReferral, String> typeColumn;
    @FXML private TableColumn<CounselingReferral, String> counselorColumn;
    @FXML private TableColumn<CounselingReferral, String> statusColumn;
    @FXML private TableColumn<CounselingReferral, String> riskColumn;
    @FXML private TableColumn<CounselingReferral, Void> actionsColumn;

    // Pagination
    @FXML private Button previousPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label pageInfoLabel;
    @FXML private ComboBox<String> itemsPerPageComboBox;

    // Sidebar
    @FXML private Label overdueCountLabel;
    @FXML private Label highRiskCountLabel;
    @FXML private Label crisisCountLabel;
    @FXML private ListView<String> overdueListView;
    @FXML private ListView<String> highRiskListView;
    @FXML private ListView<String> crisisListView;
    @FXML private ListView<String> topConcernsListView;
    @FXML private Label needsContactLabel;
    @FXML private Label awaitingAssessmentLabel;
    @FXML private Label needsConsentLabel;
    @FXML private Label safetyPlansDueLabel;

    // Buttons
    @FXML private Button newReferralButton;
    @FXML private Button newSessionButton;
    @FXML private Button refreshButton;

    // Pagination state
    private int currentPage = 0;
    private int itemsPerPage = 25;
    private List<CounselingReferral> allReferrals = new ArrayList<>();
    private List<CounselingReferral> filteredReferrals = new ArrayList<>();

    @FXML
    public void initialize() {
        logger.info("Initializing CounselingDashboardController");

        // Set default date range (last 30 days)
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusDays(30));

        // Set default filter values
        urgencyFilterComboBox.setValue("ALL");
        statusFilterComboBox.setValue("ALL");
        typeFilterComboBox.setValue("ALL");

        setupTableColumns();
        setupPagination();
        setupSearchFilter();
        loadDashboardData();
    }

    private void setupTableColumns() {
        dateColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getReferralDate()));
        dateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? "" : date.format(DATE_FORMATTER));
            }
        });

        studentColumn.setCellValueFactory(cellData -> {
            Student student = cellData.getValue().getStudent();
            return new SimpleStringProperty(student == null ? "" :
                    student.getFirstName() + " " + student.getLastName());
        });

        gradeColumn.setCellValueFactory(cellData -> {
            Student student = cellData.getValue().getStudent();
            return new SimpleStringProperty(student == null ? "" :
                    String.valueOf(student.getGradeLevel()));
        });

        urgencyColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUrgencyLevel() == null ? "" :
                        cellData.getValue().getUrgencyLevel().toString()));
        urgencyColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String urgency, boolean empty) {
                super.updateItem(urgency, empty);
                if (empty || urgency == null) {
                    setText("");
                    setStyle("");
                } else {
                    setText(urgency);
                    switch (urgency) {
                        case "EMERGENCY":
                            setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
                            break;
                        case "URGENT":
                            setStyle("-fx-text-fill: #f57c00; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        concernColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPrimaryConcern() == null ? "" :
                        cellData.getValue().getPrimaryConcern().toString().replace("_", " ")));

        typeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getReferralType() == null ? "" :
                        cellData.getValue().getReferralType().toString().replace("_", " ")));

        counselorColumn.setCellValueFactory(cellData -> {
            Teacher counselor = cellData.getValue().getAssignedCounselor();
            return new SimpleStringProperty(counselor == null ? "Unassigned" :
                    counselor.getFirstName() + " " + counselor.getLastName());
        });

        statusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getReferralStatus() == null ? "" :
                        cellData.getValue().getReferralStatus().toString()));
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText("");
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "PENDING":
                            setStyle("-fx-text-fill: #f57c00;");
                            break;
                        case "IN_PROGRESS":
                            setStyle("-fx-text-fill: #1976d2;");
                            break;
                        case "COMPLETED":
                            setStyle("-fx-text-fill: #388e3c;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        riskColumn.setCellValueFactory(cellData -> {
            CounselingReferral referral = cellData.getValue();
            String risk = "";
            if (referral.getSuicideRiskIndicated() != null && referral.getSuicideRiskIndicated()) {
                risk = "SUICIDE";
            } else if (referral.getHarmToOthersIndicated() != null && referral.getHarmToOthersIndicated()) {
                risk = "HARM";
            } else if (referral.getImmediateSafetyConcerns() != null && referral.getImmediateSafetyConcerns()) {
                risk = "SAFETY";
            } else if ("EMERGENCY".equals(String.valueOf(referral.getUrgencyLevel()))) {
                risk = "HIGH";
            }
            return new SimpleStringProperty(risk);
        });
        riskColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String risk, boolean empty) {
                super.updateItem(risk, empty);
                if (empty || risk == null || risk.isEmpty()) {
                    setText("");
                    setStyle("");
                } else {
                    setText(risk);
                    setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
                }
            }
        });

        // Actions column with View/Edit/Sessions buttons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            private final Button editButton = new Button("Edit");
            private final Button sessionsButton = new Button("Sessions");
            private final HBox buttons = new HBox(5, viewButton, editButton, sessionsButton);

            {
                buttons.setAlignment(Pos.CENTER);
                viewButton.setOnAction(event -> {
                    CounselingReferral referral = getTableView().getItems().get(getIndex());
                    handleViewReferral(referral);
                });
                editButton.setOnAction(event -> {
                    CounselingReferral referral = getTableView().getItems().get(getIndex());
                    handleEditReferral(referral);
                });
                sessionsButton.setOnAction(event -> {
                    CounselingReferral referral = getTableView().getItems().get(getIndex());
                    handleViewSessions(referral);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }

    private void setupPagination() {
        itemsPerPageComboBox.setOnAction(e -> {
            String value = itemsPerPageComboBox.getValue();
            if (value != null) {
                itemsPerPage = Integer.parseInt(value);
                currentPage = 0;
                updateTableView();
            }
        });
    }

    private void setupSearchFilter() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterReferrals();
        });
    }

    private void loadDashboardData() {
        try {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            if (startDate == null || endDate == null) {
                return;
            }

            // Load all referrals
            allReferrals = counselingManagementService.getReferralsByDateRange(startDate, endDate);
            filteredReferrals = new ArrayList<>(allReferrals);

            updateStatistics();
            loadCharts(startDate, endDate);
            updateTableView();
            loadSidebarData();

        } catch (Exception e) {
            logger.error("Error loading dashboard data", e);
            showError("Failed to load dashboard data: " + e.getMessage());
        }
    }

    private void updateStatistics() {
        try {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            // Total referrals
            long totalReferrals = allReferrals.size();
            totalReferralsLabel.setText(String.valueOf(totalReferrals));

            // Calculate change from previous period
            LocalDate previousStart = startDate.minusDays(endDate.toEpochDay() - startDate.toEpochDay());
            List<CounselingReferral> previousPeriod =
                    counselingManagementService.getReferralsByDateRange(previousStart, startDate.minusDays(1));
            long previousCount = previousPeriod.size();
            if (previousCount > 0) {
                double changePercent = ((double) (totalReferrals - previousCount) / previousCount) * 100;
                String changeText = String.format("%+.1f%%", changePercent);
                totalReferralsChangeLabel.setText(changeText);
                totalReferralsChangeLabel.setStyle(changePercent >= 0 ?
                        "-fx-text-fill: #d32f2f;" : "-fx-text-fill: #388e3c;");
            } else {
                totalReferralsChangeLabel.setText("—");
            }

            // Pending referrals
            long pending = allReferrals.stream()
                    .filter(r -> r.getReferralStatus() == CounselingReferral.ReferralStatus.PENDING)
                    .count();
            pendingReferralsLabel.setText(String.valueOf(pending));

            // High risk cases
            long highRisk = allReferrals.stream()
                    .filter(this::isHighRisk)
                    .count();
            highRiskLabel.setText(String.valueOf(highRisk));

            // Active cases
            long active = allReferrals.stream()
                    .filter(r -> r.getReferralStatus() == CounselingReferral.ReferralStatus.IN_PROGRESS)
                    .count();
            activeCasesLabel.setText(String.valueOf(active));

            // Overdue referrals
            List<CounselingReferral> overdue = counselingManagementService.getOverdueReferrals();
            overdueLabel.setText(String.valueOf(overdue.size()));

            // Total sessions
            long totalSessions = counselingSessionService.getAllSessions().size();
            totalSessionsLabel.setText(String.valueOf(totalSessions));

            // Sessions this month
            LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
            long sessionsThisMonth = counselingSessionService.getSessionsByDateRange(monthStart, LocalDate.now()).size();
            sessionsThisMonthLabel.setText(sessionsThisMonth + " this month");

        } catch (Exception e) {
            logger.error("Error updating statistics", e);
        }
    }

    private void loadCharts(LocalDate startDate, LocalDate endDate) {
        loadUrgencyChart();
        loadConcernChart();
        loadWorkloadChart();
        loadTrendChart(startDate, endDate);
    }

    private void loadUrgencyChart() {
        urgencyPieChart.getData().clear();
        Map<String, Long> urgencyCounts = filteredReferrals.stream()
                .filter(r -> r.getUrgencyLevel() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getUrgencyLevel().toString(),
                        Collectors.counting()));

        urgencyCounts.forEach((urgency, count) -> {
            PieChart.Data slice = new PieChart.Data(urgency + " (" + count + ")", count);
            urgencyPieChart.getData().add(slice);
        });
    }

    private void loadConcernChart() {
        concernPieChart.getData().clear();
        Map<String, Long> concernCounts = filteredReferrals.stream()
                .filter(r -> r.getPrimaryConcern() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getPrimaryConcern().toString().replace("_", " "),
                        Collectors.counting()));

        // Get top 5 concerns
        concernCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .forEach(entry -> {
                    PieChart.Data slice = new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")",
                            entry.getValue());
                    concernPieChart.getData().add(slice);
                });
    }

    private void loadWorkloadChart() {
        workloadPieChart.getData().clear();
        Map<String, Long> workloadCounts = filteredReferrals.stream()
                .filter(r -> r.getAssignedCounselor() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getAssignedCounselor().getFirstName() + " " +
                                r.getAssignedCounselor().getLastName(),
                        Collectors.counting()));

        workloadCounts.forEach((counselor, count) -> {
            PieChart.Data slice = new PieChart.Data(counselor + " (" + count + ")", count);
            workloadPieChart.getData().add(slice);
        });
    }

    private void loadTrendChart(LocalDate startDate, LocalDate endDate) {
        trendChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Referrals");

        // Group referrals by date
        Map<LocalDate, Long> dailyCounts = filteredReferrals.stream()
                .collect(Collectors.groupingBy(
                        CounselingReferral::getReferralDate,
                        Collectors.counting()));

        // Fill in missing dates with 0
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            long count = dailyCounts.getOrDefault(current, 0L);
            series.getData().add(new XYChart.Data<>(current.format(DATE_FORMATTER), count));
            current = current.plusDays(1);
        }

        trendChart.getData().add(series);
    }

    private void loadSidebarData() {
        try {
            // Overdue referrals
            List<CounselingReferral> overdueReferrals = counselingManagementService.getOverdueReferrals();
            overdueCountLabel.setText(String.valueOf(overdueReferrals.size()));
            ObservableList<String> overdueList = FXCollections.observableArrayList();
            overdueReferrals.stream()
                    .limit(10)
                    .forEach(r -> {
                        Student student = r.getStudent();
                        String studentName = student != null ?
                                student.getFirstName() + " " + student.getLastName() : "Unknown";
                        overdueList.add(studentName + " - " + r.getReferralDate().format(DATE_FORMATTER));
                    });
            overdueListView.setItems(overdueList);

            // High-risk cases
            List<CounselingReferral> highRiskCases = allReferrals.stream()
                    .filter(this::isHighRisk)
                    .filter(r -> r.getReferralStatus() == CounselingReferral.ReferralStatus.IN_PROGRESS)
                    .collect(Collectors.toList());
            highRiskCountLabel.setText(String.valueOf(highRiskCases.size()));
            ObservableList<String> highRiskList = FXCollections.observableArrayList();
            highRiskCases.stream()
                    .limit(10)
                    .forEach(r -> {
                        Student student = r.getStudent();
                        String studentName = student != null ?
                                student.getFirstName() + " " + student.getLastName() : "Unknown";
                        String riskType = getRiskType(r);
                        highRiskList.add(studentName + " - " + riskType);
                    });
            highRiskListView.setItems(highRiskList);

            // Crisis interventions
            LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
            List<CounselingReferral> crisisReferrals = allReferrals.stream()
                    .filter(r -> r.getReferralType() == CounselingReferral.ReferralType.CRISIS_SERVICES)
                    .filter(r -> r.getReferralDate().isAfter(thirtyDaysAgo))
                    .collect(Collectors.toList());
            crisisCountLabel.setText(String.valueOf(crisisReferrals.size()));
            ObservableList<String> crisisList = FXCollections.observableArrayList();
            crisisReferrals.stream()
                    .limit(10)
                    .forEach(r -> {
                        Student student = r.getStudent();
                        String studentName = student != null ?
                                student.getFirstName() + " " + student.getLastName() : "Unknown";
                        crisisList.add(studentName + " - " + r.getReferralDate().format(DATE_FORMATTER));
                    });
            crisisListView.setItems(crisisList);

            // Top concerns
            Map<String, Long> concernCounts = allReferrals.stream()
                    .filter(r -> r.getPrimaryConcern() != null)
                    .collect(Collectors.groupingBy(
                            r -> r.getPrimaryConcern().toString().replace("_", " "),
                            Collectors.counting()));
            ObservableList<String> topConcernsList = FXCollections.observableArrayList();
            concernCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .forEach(entry -> topConcernsList.add(entry.getKey() + " (" + entry.getValue() + ")"));
            topConcernsListView.setItems(topConcernsList);

            // Pending actions
            long needsContact = allReferrals.stream()
                    .filter(r -> r.getReferralStatus() == CounselingReferral.ReferralStatus.PENDING)
                    .filter(r -> !r.getServicesInitiated())
                    .count();
            needsContactLabel.setText(String.valueOf(needsContact));

            long awaitingAssessment = allReferrals.stream()
                    .filter(r -> r.getReferralStatus() == CounselingReferral.ReferralStatus.IN_PROGRESS)
                    .filter(r -> r.getServicesInitiated())
                    .count();
            awaitingAssessmentLabel.setText(String.valueOf(awaitingAssessment));

            long needsConsent = allReferrals.stream()
                    .filter(r -> r.isParentContacted())
                    .filter(r -> !r.isParentConsentObtained())
                    .count();
            needsConsentLabel.setText(String.valueOf(needsConsent));

            long safetyPlansDue = allReferrals.stream()
                    .filter(r -> r.isSuicideRiskIndicated() || r.isHarmToOthersIndicated() || r.isImmediateSafetyConcerns())
                    .filter(r -> !r.isSafetyPlanCreated())
                    .filter(r -> r.getReferralStatus() != CounselingReferral.ReferralStatus.COMPLETED)
                    .count();
            safetyPlansDueLabel.setText(String.valueOf(safetyPlansDue));

        } catch (Exception e) {
            logger.error("Error loading sidebar data", e);
        }
    }

    private boolean isHighRisk(CounselingReferral referral) {
        return (referral.getSuicideRiskIndicated() != null && referral.getSuicideRiskIndicated()) ||
                (referral.getHarmToOthersIndicated() != null && referral.getHarmToOthersIndicated()) ||
                (referral.getImmediateSafetyConcerns() != null && referral.getImmediateSafetyConcerns()) ||
                referral.getUrgencyLevel() == CounselingReferral.UrgencyLevel.EMERGENCY;
    }

    private String getRiskType(CounselingReferral referral) {
        if (referral.getSuicideRiskIndicated() != null && referral.getSuicideRiskIndicated()) {
            return "SUICIDE RISK";
        } else if (referral.getHarmToOthersIndicated() != null && referral.getHarmToOthersIndicated()) {
            return "HARM TO OTHERS";
        } else if (referral.getImmediateSafetyConcerns() != null && referral.getImmediateSafetyConcerns()) {
            return "SAFETY CONCERN";
        } else {
            return "HIGH RISK";
        }
    }

    private void filterReferrals() {
        String searchText = searchField.getText().toLowerCase();
        String urgencyFilter = urgencyFilterComboBox.getValue();
        String statusFilter = statusFilterComboBox.getValue();
        String typeFilter = typeFilterComboBox.getValue();
        Teacher counselorFilter = counselorFilterComboBox.getValue();

        filteredReferrals = allReferrals.stream()
                .filter(r -> {
                    // Search filter
                    if (!searchText.isEmpty()) {
                        Student student = r.getStudent();
                        String studentName = student != null ?
                                (student.getFirstName() + " " + student.getLastName()).toLowerCase() : "";
                        if (!studentName.contains(searchText)) {
                            return false;
                        }
                    }

                    // Urgency filter
                    if (!"ALL".equals(urgencyFilter)) {
                        if (r.getUrgencyLevel() == null ||
                                !r.getUrgencyLevel().toString().equals(urgencyFilter)) {
                            return false;
                        }
                    }

                    // Status filter
                    if (!"ALL".equals(statusFilter)) {
                        if (r.getReferralStatus() == null ||
                                !r.getReferralStatus().toString().equals(statusFilter)) {
                            return false;
                        }
                    }

                    // Type filter
                    if (!"ALL".equals(typeFilter)) {
                        if (r.getReferralType() == null ||
                                !r.getReferralType().toString().equals(typeFilter)) {
                            return false;
                        }
                    }

                    // Counselor filter
                    if (counselorFilter != null) {
                        if (r.getAssignedCounselor() == null ||
                                !r.getAssignedCounselor().getId().equals(counselorFilter.getId())) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());

        currentPage = 0;
        updateTableView();
        loadCharts(startDatePicker.getValue(), endDatePicker.getValue());
    }

    private void updateTableView() {
        int totalItems = filteredReferrals.size();
        int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
        if (totalPages == 0) totalPages = 1;

        // Update page info
        pageInfoLabel.setText("Page " + (currentPage + 1) + " of " + totalPages);
        previousPageButton.setDisable(currentPage == 0);
        nextPageButton.setDisable(currentPage >= totalPages - 1);

        // Get items for current page
        int fromIndex = currentPage * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, totalItems);

        List<CounselingReferral> pageItems = totalItems > 0 ?
                filteredReferrals.subList(fromIndex, toIndex) : Collections.emptyList();

        referralsTable.setItems(FXCollections.observableArrayList(pageItems));
    }

    @FXML
    private void handleApplyFilters() {
        filterReferrals();
    }

    @FXML
    private void handleClearFilters() {
        urgencyFilterComboBox.setValue("ALL");
        statusFilterComboBox.setValue("ALL");
        typeFilterComboBox.setValue("ALL");
        counselorFilterComboBox.setValue(null);
        searchField.clear();
        filterReferrals();
    }

    @FXML
    private void handlePreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            updateTableView();
        }
    }

    @FXML
    private void handleNextPage() {
        int totalPages = (int) Math.ceil((double) filteredReferrals.size() / itemsPerPage);
        if (currentPage < totalPages - 1) {
            currentPage++;
            updateTableView();
        }
    }

    @FXML
    private void handleNewReferral() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CounselingReferralForm.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("New Counseling Referral");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);
            ResponsiveDesignHelper.makeDialogResponsive(stage);
            stage.showAndWait();

            // Refresh after closing
            handleRefresh();

        } catch (Exception e) {
            logger.error("Error opening new referral form", e);
            showError("Failed to open referral form: " + e.getMessage());
        }
    }

    @FXML
    private void handleNewSession() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CounselingSession.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("New Counseling Session");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);
            ResponsiveDesignHelper.makeDialogResponsive(stage);
            stage.showAndWait();

            // Refresh after closing
            handleRefresh();

        } catch (Exception e) {
            logger.error("Error opening new session form", e);
            showError("Failed to open session form: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadDashboardData();
    }

    @FXML
    private void handleExport() {
        if (filteredReferrals.isEmpty()) {
            showInfo("Export", "No referrals to export. Please adjust filters or date range.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Counseling Referrals");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser.setInitialFileName("counseling-referrals-" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx");

        File file = fileChooser.showSaveDialog(referralsTable.getScene().getWindow());
        if (file != null) {
            logger.info("Exporting counseling referrals to {}", file.getAbsolutePath());

            new Thread(() -> {
                try {
                    LocalDate startDate = startDatePicker.getValue();
                    LocalDate endDate = endDatePicker.getValue();
                    byte[] excelData = analyticsExportService.exportCounselingReferralsExcel(
                            filteredReferrals, startDate, endDate);
                    analyticsExportService.writeToFile(excelData, file);
                    javafx.application.Platform.runLater(() ->
                            showInfo("Export Complete", "Counseling referrals exported successfully to:\n" +
                                    file.getName() + "\n\nTotal referrals: " + filteredReferrals.size()));
                } catch (Exception e) {
                    logger.error("Error exporting counseling referrals", e);
                    javafx.application.Platform.runLater(() ->
                            showError("Failed to export: " + e.getMessage()));
                }
            }).start();
        }
    }

    private void handleViewReferral(CounselingReferral referral) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CounselingReferralForm.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            CounselingReferralFormController controller = loader.getController();
            controller.loadReferral(referral);
            controller.setViewMode(true);

            Stage stage = new Stage();
            stage.setTitle("View Counseling Referral");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);
            ResponsiveDesignHelper.makeDialogResponsive(stage);
            stage.showAndWait();

        } catch (Exception e) {
            logger.error("Error viewing referral", e);
            showError("Failed to view referral: " + e.getMessage());
        }
    }

    private void handleEditReferral(CounselingReferral referral) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CounselingReferralForm.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            CounselingReferralFormController controller = loader.getController();
            controller.loadReferral(referral);

            Stage stage = new Stage();
            stage.setTitle("Edit Counseling Referral");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);
            ResponsiveDesignHelper.makeDialogResponsive(stage);
            stage.showAndWait();

            // Refresh after closing
            handleRefresh();

        } catch (Exception e) {
            logger.error("Error editing referral", e);
            showError("Failed to edit referral: " + e.getMessage());
        }
    }

    private void handleViewSessions(CounselingReferral referral) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CounselingSession.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            CounselingSessionController controller = loader.getController();
            controller.setReferral(referral);

            Stage stage = new Stage();
            stage.setTitle("New Session for " + referral.getStudent().getFirstName() + " " +
                    referral.getStudent().getLastName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);
            ResponsiveDesignHelper.makeDialogResponsive(stage);
            stage.showAndWait();

            // Refresh after closing
            handleRefresh();

        } catch (Exception e) {
            logger.error("Error opening session form", e);
            showError("Failed to open session form: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
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
}
