package com.heronix.ui.controller;

import com.heronix.model.domain.HealthRecord;
import com.heronix.model.domain.NurseVisit;
import com.heronix.model.domain.Student;
import com.heronix.service.HealthOfficeService;
import com.heronix.service.StudentService;
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
import java.util.stream.Collectors;

/**
 * Controller for Health Office Dashboard
 * Manages nurse visits, health screenings, and health office operations
 */
@Component
public class HealthOfficeDashboardController {

    private static final Logger logger = LoggerFactory.getLogger(HealthOfficeDashboardController.class);

    @Autowired
    private HealthOfficeService healthOfficeService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private ApplicationContext applicationContext;

    // Action Buttons
    @FXML private Button newVisitButton;
    @FXML private Button healthScreeningButton;
    @FXML private Button medicationButton;
    @FXML private Button immunizationButton;
    @FXML private Button refreshButton;

    // Statistics Labels
    @FXML private Label activeVisitsLabel;
    @FXML private Label todayVisitsLabel;
    @FXML private Label todayChangeLabel;
    @FXML private Label sentHomeLabel;
    @FXML private Label pendingNotificationsLabel;
    @FXML private Label screeningsDueLabel;
    @FXML private Label highRiskLabel;

    // Filters
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> reasonFilterComboBox;
    @FXML private ComboBox<String> dispositionFilterComboBox;
    @FXML private TextField searchField;

    // Charts
    @FXML private PieChart visitReasonsPieChart;
    @FXML private PieChart dispositionsPieChart;
    @FXML private BarChart<String, Number> trendChart;

    // Visits Table
    @FXML private TableView<NurseVisit> visitsTable;
    @FXML private TableColumn<NurseVisit, LocalDate> visitDateColumn;
    @FXML private TableColumn<NurseVisit, String> visitTimeColumn;
    @FXML private TableColumn<NurseVisit, String> studentNameColumn;
    @FXML private TableColumn<NurseVisit, String> gradeColumn;
    @FXML private TableColumn<NurseVisit, String> reasonColumn;
    @FXML private TableColumn<NurseVisit, String> chiefComplaintColumn;
    @FXML private TableColumn<NurseVisit, String> temperatureColumn;
    @FXML private TableColumn<NurseVisit, String> dispositionColumn;
    @FXML private TableColumn<NurseVisit, String> statusColumn;
    @FXML private TableColumn<NurseVisit, Void> actionsColumn;
    @FXML private Label totalRecordsLabel;

    // Sidebar Lists
    @FXML private ListView<String> activeStudentsList;
    @FXML private ListView<String> pendingNotificationsList;
    @FXML private ListView<String> frequentVisitorsList;
    @FXML private ListView<String> highRiskStudentsList;
    @FXML private Label visionScreeningsDueLabel;
    @FXML private Label hearingScreeningsDueLabel;

    private ObservableList<NurseVisit> allVisits = FXCollections.observableArrayList();
    private ObservableList<NurseVisit> filteredVisits = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        logger.info("Initializing HealthOfficeDashboardController");

        setupTable();
        setupFilters();
        setupSearchFilter();
        loadDashboardData();
    }

    private void setupTable() {
        // Date column
        visitDateColumn.setCellValueFactory(cellData ->
            new SimpleObjectProperty<>(cellData.getValue().getVisitDate()));
        visitDateColumn.setCellFactory(column -> new TableCell<NurseVisit, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                }
            }
        });

        // Time column
        visitTimeColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(
                cellData.getValue().getVisitTime() != null ?
                cellData.getValue().getVisitTime().format(DateTimeFormatter.ofPattern("hh:mm a")) : ""
            ));

        // Student name column
        studentNameColumn.setCellValueFactory(cellData -> {
            Student student = cellData.getValue().getStudent();
            return new SimpleStringProperty(
                student != null ? student.getFirstName() + " " + student.getLastName() : ""
            );
        });

        // Grade column
        gradeColumn.setCellValueFactory(cellData -> {
            Student student = cellData.getValue().getStudent();
            return new SimpleStringProperty(
                student != null ? String.valueOf(student.getGradeLevel()) : ""
            );
        });

        // Reason column
        reasonColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(
                cellData.getValue().getVisitReason() != null ?
                cellData.getValue().getVisitReason().toString() : ""
            ));

        // Chief complaint column
        chiefComplaintColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getChiefComplaint()));

        // Temperature column
        temperatureColumn.setCellValueFactory(cellData -> {
            Double temp = cellData.getValue().getTemperature();
            return new SimpleStringProperty(temp != null ? String.format("%.1f°F", temp) : "--");
        });
        temperatureColumn.setCellFactory(column -> new TableCell<NurseVisit, String>() {
            @Override
            protected void updateItem(String temp, boolean empty) {
                super.updateItem(temp, empty);
                if (empty || temp == null || temp.equals("--")) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(temp);
                    NurseVisit visit = getTableRow().getItem();
                    if (visit != null && visit.getHasFever() != null && visit.getHasFever()) {
                        setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        // Disposition column
        dispositionColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(
                cellData.getValue().getDisposition() != null ?
                cellData.getValue().getDisposition().toString() : "In Progress"
            ));

        // Status column
        statusColumn.setCellValueFactory(cellData -> {
            NurseVisit visit = cellData.getValue();
            String status;
            if (visit.getCheckOutTime() == null) {
                status = "Active";
            } else if (visit.getSentHome() != null && visit.getSentHome()) {
                status = "Sent Home";
            } else {
                status = "Checked Out";
            }
            return new SimpleStringProperty(status);
        });
        statusColumn.setCellFactory(column -> new TableCell<NurseVisit, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    if (status.equals("Active")) {
                        setStyle("-fx-text-fill: #f57c00; -fx-font-weight: bold;");
                    } else if (status.equals("Sent Home")) {
                        setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        // Actions column
        actionsColumn.setCellFactory(param -> new TableCell<NurseVisit, Void>() {
            private final Button viewButton = new Button("View");
            private final Button editButton = new Button("Edit");

            {
                viewButton.setOnAction(event -> {
                    NurseVisit visit = getTableRow().getItem();
                    if (visit != null) {
                        handleViewVisit(visit);
                    }
                });

                editButton.setOnAction(event -> {
                    NurseVisit visit = getTableRow().getItem();
                    if (visit != null) {
                        handleEditVisit(visit);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, viewButton, editButton);
                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });

        visitsTable.setItems(filteredVisits);
    }

    private void setupFilters() {
        // Set default date range - last 7 days
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusDays(7));

        // Populate reason filter
        ObservableList<String> reasons = FXCollections.observableArrayList("All Reasons");
        reasons.addAll(java.util.Arrays.stream(NurseVisit.VisitReason.values())
            .map(Enum::toString)
            .collect(Collectors.toList()));
        reasonFilterComboBox.setItems(reasons);
        reasonFilterComboBox.setValue("All Reasons");

        // Populate disposition filter
        ObservableList<String> dispositions = FXCollections.observableArrayList("All Dispositions");
        dispositions.addAll(java.util.Arrays.stream(NurseVisit.Disposition.values())
            .map(Enum::toString)
            .collect(Collectors.toList()));
        dispositionFilterComboBox.setItems(dispositions);
        dispositionFilterComboBox.setValue("All Dispositions");
    }

    private void setupSearchFilter() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });
    }

    private void loadDashboardData() {
        try {
            logger.info("Loading health office dashboard data");

            // Load all visits for date range
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            allVisits.setAll(healthOfficeService.getNurseVisitsByDateRange(startDate, endDate));

            // Apply filters
            applyFilters();

            // Update statistics
            updateStatistics();

            // Update charts
            updateCharts();

            // Update sidebar
            updateSidebar();

        } catch (Exception e) {
            logger.error("Error loading dashboard data", e);
            showError("Failed to load dashboard data: " + e.getMessage());
        }
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String reasonFilter = reasonFilterComboBox.getValue();
        String dispositionFilter = dispositionFilterComboBox.getValue();

        filteredVisits.setAll(allVisits.stream()
            .filter(visit -> {
                // Search filter
                if (!searchText.isEmpty()) {
                    String studentName = (visit.getStudent().getFirstName() + " " +
                        visit.getStudent().getLastName()).toLowerCase();
                    if (!studentName.contains(searchText)) {
                        return false;
                    }
                }

                // Reason filter
                if (!"All Reasons".equals(reasonFilter)) {
                    if (visit.getVisitReason() == null ||
                        !visit.getVisitReason().toString().equals(reasonFilter)) {
                        return false;
                    }
                }

                // Disposition filter
                if (!"All Dispositions".equals(dispositionFilter)) {
                    if (visit.getDisposition() == null ||
                        !visit.getDisposition().toString().equals(dispositionFilter)) {
                        return false;
                    }
                }

                return true;
            })
            .collect(Collectors.toList()));

        totalRecordsLabel.setText("Total: " + filteredVisits.size() + " visits");
    }

    private void updateStatistics() {
        // Active visits (currently in office)
        List<NurseVisit> activeVisits = healthOfficeService.getActiveVisits();
        activeVisitsLabel.setText(String.valueOf(activeVisits.size()));

        // Today's visits
        LocalDate today = LocalDate.now();
        long todayCount = allVisits.stream()
            .filter(v -> v.getVisitDate().equals(today))
            .count();
        todayVisitsLabel.setText(String.valueOf(todayCount));

        // Calculate change from yesterday
        LocalDate yesterday = today.minusDays(1);
        List<NurseVisit> yesterdayVisits = healthOfficeService.getNurseVisitsByDateRange(yesterday, yesterday);
        long yesterdayCount = yesterdayVisits.size();
        if (yesterdayCount > 0) {
            long change = todayCount - yesterdayCount;
            double changePercent = ((double) change / yesterdayCount) * 100;
            String changeText = String.format("%+d (%+.0f%%)", change, changePercent);
            todayChangeLabel.setText(changeText);
            todayChangeLabel.setStyle(change >= 0 ? "-fx-text-fill: #f57c00;" : "-fx-text-fill: #388e3c;");
        } else {
            todayChangeLabel.setText("--");
        }

        // Sent home today
        List<NurseVisit> sentHome = healthOfficeService.getStudentsSentHomeToday();
        sentHomeLabel.setText(String.valueOf(sentHome.size()));

        // Pending notifications
        List<NurseVisit> pendingNotifications = healthOfficeService.getVisitsRequiringParentNotification();
        pendingNotificationsLabel.setText(String.valueOf(pendingNotifications.size()));

        // Screenings due
        List<HealthRecord> visionDue = healthOfficeService.getStudentsNeedingVisionScreening();
        List<HealthRecord> hearingDue = healthOfficeService.getStudentsNeedingHearingScreening();
        screeningsDueLabel.setText(String.valueOf(visionDue.size() + hearingDue.size()));

        // High-risk students
        List<HealthRecord> highRisk = healthOfficeService.getHighRiskStudents();
        highRiskLabel.setText(String.valueOf(highRisk.size()));
    }

    private void updateCharts() {
        // Visit Reasons Pie Chart
        visitReasonsPieChart.getData().clear();
        Map<String, Long> reasonCounts = filteredVisits.stream()
            .filter(v -> v.getVisitReason() != null)
            .collect(Collectors.groupingBy(
                v -> v.getVisitReason().toString(),
                Collectors.counting()));
        reasonCounts.forEach((reason, count) -> {
            PieChart.Data slice = new PieChart.Data(reason + " (" + count + ")", count);
            visitReasonsPieChart.getData().add(slice);
        });

        // Dispositions Pie Chart
        dispositionsPieChart.getData().clear();
        Map<String, Long> dispositionCounts = filteredVisits.stream()
            .filter(v -> v.getDisposition() != null)
            .collect(Collectors.groupingBy(
                v -> v.getDisposition().toString(),
                Collectors.counting()));
        dispositionCounts.forEach((disposition, count) -> {
            PieChart.Data slice = new PieChart.Data(disposition + " (" + count + ")", count);
            dispositionsPieChart.getData().add(slice);
        });

        // 30-Day Trend Bar Chart
        trendChart.getData().clear();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        List<NurseVisit> trendData = healthOfficeService.getNurseVisitsByDateRange(startDate, endDate);

        Map<LocalDate, Long> dailyCounts = trendData.stream()
            .collect(Collectors.groupingBy(NurseVisit::getVisitDate, Collectors.counting()));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Daily Visits");

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            long count = dailyCounts.getOrDefault(date, 0L);
            series.getData().add(new XYChart.Data<>(
                date.format(DateTimeFormatter.ofPattern("MM/dd")), count));
        }

        trendChart.getData().add(series);
    }

    private void updateSidebar() {
        // Active students in office
        List<NurseVisit> activeVisits = healthOfficeService.getActiveVisits();
        ObservableList<String> activeStudents = FXCollections.observableArrayList();
        for (NurseVisit visit : activeVisits) {
            Student student = visit.getStudent();
            String entry = String.format("%s %s (Gr %d) - %s",
                student.getFirstName(),
                student.getLastName(),
                student.getGradeLevel(),
                visit.getChiefComplaint());
            activeStudents.add(entry);
        }
        activeStudentsList.setItems(activeStudents);

        // Pending notifications
        List<NurseVisit> pendingNotifications = healthOfficeService.getVisitsRequiringParentNotification();
        ObservableList<String> pendingList = FXCollections.observableArrayList();
        for (NurseVisit visit : pendingNotifications) {
            Student student = visit.getStudent();
            String entry = String.format("%s %s - %s",
                student.getFirstName(),
                student.getLastName(),
                visit.getChiefComplaint());
            pendingList.add(entry);
        }
        pendingNotificationsList.setItems(pendingList);

        // Frequent visitors (last 30 days)
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        List<Object[]> frequentVisitors = healthOfficeService.getFrequentVisitors(
            thirtyDaysAgo, LocalDate.now(), 3L);
        ObservableList<String> frequentList = FXCollections.observableArrayList();
        for (Object[] result : frequentVisitors) {
            Student student = (Student) result[0];
            Long visitCount = (Long) result[1];
            String entry = String.format("%s %s - %d visits",
                student.getFirstName(),
                student.getLastName(),
                visitCount);
            frequentList.add(entry);
        }
        frequentVisitorsList.setItems(frequentList);

        // High-risk students
        List<HealthRecord> highRisk = healthOfficeService.getHighRiskStudents();
        ObservableList<String> highRiskList = FXCollections.observableArrayList();
        for (HealthRecord record : highRisk) {
            Student student = record.getStudent();
            String entry = String.format("%s %s (Gr %d)",
                student.getFirstName(),
                student.getLastName(),
                student.getGradeLevel());
            highRiskList.add(entry);
        }
        highRiskStudentsList.setItems(highRiskList);

        // Screenings due
        List<HealthRecord> visionDue = healthOfficeService.getStudentsNeedingVisionScreening();
        List<HealthRecord> hearingDue = healthOfficeService.getStudentsNeedingHearingScreening();
        visionScreeningsDueLabel.setText("Vision: " + visionDue.size() + " students");
        hearingScreeningsDueLabel.setText("Hearing: " + hearingDue.size() + " students");
    }

    @FXML
    private void handleNewVisit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/NurseVisitLog.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("New Nurse Visit");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            handleRefresh();

        } catch (Exception e) {
            logger.error("Error opening new visit form", e);
            showError("Failed to open visit form: " + e.getMessage());
        }
    }

    @FXML
    private void handleHealthScreening() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HealthScreening.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Health Screening");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            handleRefresh();

        } catch (Exception e) {
            logger.error("Error opening health screening form", e);
            showError("Failed to open screening form: " + e.getMessage());
        }
    }

    @FXML
    private void handleMedication() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MedicationAdministration.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Medication Administration");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            handleRefresh();

        } catch (Exception e) {
            logger.error("Error opening medication form", e);
            showError("Failed to open medication form: " + e.getMessage());
        }
    }

    @FXML
    private void handleImmunization() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ImmunizationTracking.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Immunization Tracking");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            handleRefresh();

        } catch (Exception e) {
            logger.error("Error opening immunization form", e);
            showError("Failed to open immunization form: " + e.getMessage());
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
        startDatePicker.setValue(LocalDate.now().minusDays(7));
        endDatePicker.setValue(LocalDate.now());
        reasonFilterComboBox.setValue("All Reasons");
        dispositionFilterComboBox.setValue("All Dispositions");
        searchField.clear();
        loadDashboardData();
    }

    private void handleViewVisit(NurseVisit visit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/NurseVisitLog.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            NurseVisitLogController controller = loader.getController();
            controller.loadVisit(visit);
            controller.setViewMode(true);

            Stage stage = new Stage();
            stage.setTitle("View Nurse Visit - " + visit.getStudent().getFirstName() + " " +
                visit.getStudent().getLastName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            logger.error("Error viewing visit", e);
            showError("Failed to view visit: " + e.getMessage());
        }
    }

    private void handleEditVisit(NurseVisit visit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/NurseVisitLog.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            NurseVisitLogController controller = loader.getController();
            controller.loadVisit(visit);

            Stage stage = new Stage();
            stage.setTitle("Edit Nurse Visit - " + visit.getStudent().getFirstName() + " " +
                visit.getStudent().getLastName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            handleRefresh();

        } catch (Exception e) {
            logger.error("Error editing visit", e);
            showError("Failed to edit visit: " + e.getMessage());
        }
    }

    @FXML
    private void handleExportDaily() {
        showInfo("Export functionality will be implemented in a future update.");
    }

    @FXML
    private void handleCheckOutAll() {
        List<NurseVisit> activeVisits = healthOfficeService.getActiveVisits();
        if (activeVisits.isEmpty()) {
            showInfo("No active visits to check out.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Check Out All Students");
        confirmation.setHeaderText("Check out all students currently in the health office?");
        confirmation.setContentText("This will check out " + activeVisits.size() + " student(s).");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    for (NurseVisit visit : activeVisits) {
                        healthOfficeService.checkOutStudent(visit.getId(),
                            NurseVisit.Disposition.RETURNED_TO_CLASS);
                    }
                    showInfo("Successfully checked out " + activeVisits.size() + " student(s).");
                    handleRefresh();
                } catch (Exception e) {
                    logger.error("Error checking out students", e);
                    showError("Failed to check out students: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleViewHealthRecords() {
        showInfo("Health records view will be implemented in a future update.");
    }

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
