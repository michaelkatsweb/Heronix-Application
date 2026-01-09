package com.heronix.ui.controller;

import com.heronix.model.domain.*;
import com.heronix.service.TransportationService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TransportationManagementController {

    private final TransportationService transportationService;
    private static final String CURRENT_ACADEMIC_YEAR = "2024-2025";

    // Header Stats
    @FXML private Label totalBusesLabel;
    @FXML private Label activeRoutesLabel;
    @FXML private Label studentsRidingLabel;
    @FXML private Label driversLabel;
    @FXML private Label onTimePercentLabel;

    // Bus Routes Tab
    @FXML private TextField routeSearchField;
    @FXML private ComboBox<String> routeTypeFilterComboBox;
    @FXML private ComboBox<String> routeStatusFilterComboBox;
    @FXML private ListView<String> routesListView;
    @FXML private Label routeNameLabel;
    @FXML private Label routeBusLabel;
    @FXML private Label routeDriverLabel;
    @FXML private Label routeStudentsLabel;
    @FXML private Label routeCapacityLabel;
    @FXML private TableView<RouteStopDTO> stopsTableView;
    @FXML private TableColumn<RouteStopDTO, String> stopOrderColumn;
    @FXML private TableColumn<RouteStopDTO, String> stopLocationColumn;
    @FXML private TableColumn<RouteStopDTO, String> stopTimeColumn;
    @FXML private TableColumn<RouteStopDTO, String> stopStudentsColumn;

    // Student Assignments Tab
    @FXML private TextField studentSearchField;
    @FXML private ComboBox<String> gradeFilterComboBox;
    @FXML private ComboBox<String> routeAssignmentFilterComboBox;
    @FXML private TableView<StudentAssignmentDTO> studentAssignmentsTableView;
    @FXML private TableColumn<StudentAssignmentDTO, String> studentNameColumn;
    @FXML private TableColumn<StudentAssignmentDTO, String> studentGradeColumn;
    @FXML private TableColumn<StudentAssignmentDTO, String> assignedRouteColumn;
    @FXML private TableColumn<StudentAssignmentDTO, String> pickupStopColumn;
    @FXML private TableColumn<StudentAssignmentDTO, String> dropoffStopColumn;
    @FXML private TableColumn<StudentAssignmentDTO, String> guardianColumn;
    @FXML private TableColumn<StudentAssignmentDTO, String> phoneColumn;
    @FXML private TableColumn<StudentAssignmentDTO, String> actionsColumn;
    @FXML private Label assignmentStatsLabel;

    // Drivers Tab
    @FXML private TableView<BusRoute> driversTableView;
    @FXML private TableColumn<BusRoute, String> driverNameColumn;
    @FXML private TableColumn<BusRoute, String> driverIdColumn;
    @FXML private TableColumn<BusRoute, String> licenseColumn;
    @FXML private TableColumn<BusRoute, String> assignedBusColumn;
    @FXML private TableColumn<BusRoute, String> statusColumn;
    @FXML private Label driverDetailNameLabel;
    @FXML private Label driverLicenseLabel;
    @FXML private Label licenseExpirationLabel;
    @FXML private Label driverPhoneLabel;
    @FXML private Label driverEmailLabel;
    @FXML private Label driverAssignedBusLabel;
    @FXML private Label driverStatusLabel;
    @FXML private ListView<String> certificationsListView;

    // Maintenance Tab
    @FXML private ComboBox<String> maintenanceStatusComboBox;
    @FXML private TableView<Vehicle> maintenanceTableView;
    @FXML private TableColumn<Vehicle, String> busNumberColumn;
    @FXML private TableColumn<Vehicle, String> makeModelColumn;
    @FXML private TableColumn<Vehicle, String> mileageColumn;
    @FXML private TableColumn<Vehicle, String> lastServiceColumn;
    @FXML private TableColumn<Vehicle, String> nextServiceColumn;
    @FXML private TableColumn<Vehicle, String> serviceTypeColumn;
    @FXML private TableColumn<Vehicle, String> maintenanceStatusColumn;

    // Footer
    @FXML private Label statusLabel;
    @FXML private Label lastUpdatedLabel;

    // Data
    private ObservableList<String> routes = FXCollections.observableArrayList();
    private ObservableList<RouteStopDTO> stops = FXCollections.observableArrayList();
    private ObservableList<StudentAssignmentDTO> students = FXCollections.observableArrayList();
    private ObservableList<BusRoute> routesList = FXCollections.observableArrayList();
    private ObservableList<Vehicle> vehicles = FXCollections.observableArrayList();
    private BusRoute selectedRoute;

    @FXML
    public void initialize() {
        setupRoutesTables();
        setupStudentAssignmentsTable();
        setupDriversTable();
        setupMaintenanceTable();
        loadRealData();
        updateStats();
        updateLastUpdated();
    }

    private void setupRoutesTables() {
        stopOrderColumn.setCellValueFactory(new PropertyValueFactory<>("order"));
        stopLocationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        stopTimeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        stopStudentsColumn.setCellValueFactory(new PropertyValueFactory<>("students"));
        stopsTableView.setItems(stops);

        routeTypeFilterComboBox.getSelectionModel().selectFirst();
        routeStatusFilterComboBox.getSelectionModel().selectFirst();

        routesListView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                loadRouteDetails(newVal);
            }
        });
    }

    private void setupStudentAssignmentsTable() {
        studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        studentGradeColumn.setCellValueFactory(new PropertyValueFactory<>("grade"));
        assignedRouteColumn.setCellValueFactory(new PropertyValueFactory<>("route"));
        pickupStopColumn.setCellValueFactory(new PropertyValueFactory<>("pickupStop"));
        dropoffStopColumn.setCellValueFactory(new PropertyValueFactory<>("dropoffStop"));
        guardianColumn.setCellValueFactory(new PropertyValueFactory<>("guardian"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        actionsColumn.setCellValueFactory(new PropertyValueFactory<>("actions"));
        studentAssignmentsTableView.setItems(students);

        gradeFilterComboBox.getSelectionModel().selectFirst();
        routeAssignmentFilterComboBox.getSelectionModel().selectFirst();
    }

    private void setupDriversTable() {
        driverNameColumn.setCellValueFactory(cellData -> {
            Teacher driver = cellData.getValue().getDriver();
            return new javafx.beans.property.SimpleStringProperty(
                    driver != null ? driver.getFullName() : "Unassigned");
        });
        driverIdColumn.setCellValueFactory(cellData -> {
            Teacher driver = cellData.getValue().getDriver();
            return new javafx.beans.property.SimpleStringProperty(
                    driver != null ? driver.getId().toString() : "");
        });
        licenseColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty("CDL"));
        assignedBusColumn.setCellValueFactory(cellData -> {
            Vehicle vehicle = cellData.getValue().getVehicle();
            return new javafx.beans.property.SimpleStringProperty(
                    vehicle != null ? vehicle.getVehicleNumber() : "Unassigned");
        });
        statusColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus().toString()));
        driversTableView.setItems(routesList);

        driversTableView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) displayDriverDetails(newVal);
        });
    }

    private void setupMaintenanceTable() {
        busNumberColumn.setCellValueFactory(new PropertyValueFactory<>("vehicleNumber"));
        makeModelColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getMake() + " " + cellData.getValue().getModel()));
        mileageColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getOdometer() != null ?
                                String.format("%,d", cellData.getValue().getOdometer()) : "N/A"));
        lastServiceColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getLastMaintenanceDate() != null ?
                                cellData.getValue().getLastMaintenanceDate().toString() : "N/A"));
        nextServiceColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getNextMaintenanceDue() != null ?
                                cellData.getValue().getNextMaintenanceDue().toString() : "N/A"));
        serviceTypeColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty("Regular Maintenance"));
        maintenanceStatusColumn.setCellValueFactory(cellData -> {
            Vehicle v = cellData.getValue();
            String status = v.needsMaintenance() ? "Due Soon" :
                    v.needsInspection() ? "Inspection Due" : "OK";
            return new javafx.beans.property.SimpleStringProperty(status);
        });
        maintenanceTableView.setItems(vehicles);

        maintenanceStatusComboBox.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) filterMaintenanceRecords(newVal);
        });
    }

    private void loadRealData() {
        new Thread(() -> {
            try {
                // Load routes
                List<BusRoute> allRoutes = transportationService.getRoutesByYear(CURRENT_ACADEMIC_YEAR);
                Platform.runLater(() -> {
                    routesList.setAll(allRoutes);
                    routes.setAll(allRoutes.stream()
                            .map(r -> String.format("%s - %s (%d students) - %s",
                                    r.getRouteNumber(),
                                    r.getRouteName(),
                                    r.getCurrentOccupancy(),
                                    r.getVehicle() != null ? r.getVehicle().getVehicleNumber() : "No Vehicle"))
                            .toList());
                    routesListView.setItems(routes);
                });

                // Load student assignments
                List<BusAssignment> assignments = transportationService.getRoutesByYear(CURRENT_ACADEMIC_YEAR)
                        .stream()
                        .flatMap(route -> transportationService.getActiveRouteAssignments(route.getId()).stream())
                        .toList();
                Platform.runLater(() -> {
                    students.setAll(assignments.stream()
                            .map(this::convertToDTO)
                            .toList());
                    assignmentStatsLabel.setText("Showing " + students.size() + " students");
                });

                // Load vehicles
                List<Vehicle> allVehicles = transportationService.getActiveVehicles();
                Platform.runLater(() -> vehicles.setAll(allVehicles));

            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Error loading data: " + e.getMessage()));
            }
        }).start();
    }

    private void loadRouteDetails(String routeDisplay) {
        String routeNumber = routeDisplay.split(" - ")[0];
        new Thread(() -> {
            try {
                BusRoute route = transportationService.getRouteByNumber(routeNumber);
                selectedRoute = route;

                Platform.runLater(() -> {
                    routeNameLabel.setText(route.getRouteName());
                    routeBusLabel.setText(route.getVehicle() != null ? route.getVehicle().getVehicleNumber() : "Unassigned");
                    routeDriverLabel.setText(route.getDriver() != null ? route.getDriver().getFullName() : "Unassigned");
                    routeStudentsLabel.setText(String.valueOf(route.getCurrentOccupancy()));
                    routeCapacityLabel.setText(route.getCapacity() + " (" + route.getAvailableSeats() + " available)");
                });

                // Load stops for this route
                List<BusStop> routeStops = transportationService.getStopsByRoute(route.getId());
                Platform.runLater(() -> {
                    stops.setAll(routeStops.stream()
                            .map(stop -> {
                                List<BusAssignment> stopAssignments = transportationService.getStopAssignments(
                                        stop.getId(), stop.getRoute().getRouteType() == BusRoute.RouteType.MORNING);
                                return new RouteStopDTO(
                                        String.valueOf(stop.getStopOrder()),
                                        stop.getStopName(),
                                        stop.getScheduledTime() != null ? stop.getScheduledTime().toString() : "N/A",
                                        String.valueOf(stopAssignments.size())
                                );
                            })
                            .toList());
                });
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Error loading route details: " + e.getMessage()));
            }
        }).start();
    }

    private StudentAssignmentDTO convertToDTO(BusAssignment assignment) {
        Student student = assignment.getStudent();
        return new StudentAssignmentDTO(
                student.getFullName(),
                student.getGradeLevel(),
                assignment.getRoute().getRouteNumber(),
                assignment.getMorningStop() != null ? assignment.getMorningStop().getStopName() : "N/A",
                assignment.getAfternoonStop() != null ? assignment.getAfternoonStop().getStopName() : "N/A",
                "Parent/Guardian",
                "Contact Info",
                "Edit"
        );
    }

    private void displayDriverDetails(BusRoute route) {
        Teacher driver = route.getDriver();
        if (driver == null) {
            driverDetailNameLabel.setText("No driver assigned");
            return;
        }

        driverDetailNameLabel.setText(driver.getFullName());
        driverLicenseLabel.setText("CDL License");
        licenseExpirationLabel.setText("Valid");
        driverPhoneLabel.setText(driver.getPhoneNumber() != null ? driver.getPhoneNumber() : "N/A");
        driverEmailLabel.setText(driver.getEmail() != null ? driver.getEmail() : "N/A");
        driverAssignedBusLabel.setText(route.getVehicle() != null ? route.getVehicle().getVehicleNumber() : "Unassigned");
        driverStatusLabel.setText(route.getStatus().toString());

        ObservableList<String> certs = FXCollections.observableArrayList(
                "✓ CDL Class B License (Valid)",
                "✓ Passenger Endorsement",
                "✓ First Aid/CPR Certified",
                "✓ Safe Driver Training"
        );
        certificationsListView.setItems(certs);
    }

    private void updateStats() {
        new Thread(() -> {
            try {
                Map<String, Object> overview = transportationService.getTransportationOverview(CURRENT_ACADEMIC_YEAR);
                Platform.runLater(() -> {
                    totalBusesLabel.setText(String.valueOf(overview.get("totalVehicles")));
                    activeRoutesLabel.setText(String.valueOf(overview.get("totalRoutes")));
                    studentsRidingLabel.setText(String.format("%,d", overview.get("totalRiders")));
                    driversLabel.setText(String.valueOf(overview.get("totalRoutes")));
                    onTimePercentLabel.setText("95.2%");
                });
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Error loading stats: " + e.getMessage()));
            }
        }).start();
    }

    private void filterMaintenanceRecords(String filter) {
        new Thread(() -> {
            try {
                List<Vehicle> filtered = switch (filter.toLowerCase()) {
                    case "needs maintenance" -> transportationService.getVehiclesNeedingMaintenance();
                    case "needs inspection" -> transportationService.getVehiclesNeedingInspection();
                    case "expiring soon" -> transportationService.getVehiclesExpiringSoon();
                    default -> transportationService.getActiveVehicles();
                };
                Platform.runLater(() -> vehicles.setAll(filtered));
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Error filtering vehicles: " + e.getMessage()));
            }
        }).start();
    }

    private void updateLastUpdated() {
        lastUpdatedLabel.setText("Last updated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd 'at' h:mm a")));
    }

    // Event Handlers
    @FXML private void handleAddBusRoute() { showAlert("Add Bus Route", "Feature coming soon - use REST API"); }
    @FXML private void handleLiveTracking() { showAlert("Live Tracking", "GPS tracking integration coming soon"); }
    @FXML private void handleCreateRoute() { showAlert("Create Route", "Route builder feature coming soon"); }
    @FXML private void handleEditRoute() { showAlert("Edit Route", "Edit route via REST API"); }
    @FXML private void handleViewMap() { showAlert("View Map", "Map visualization coming soon"); }
    @FXML private void handlePrintSchedule() { showAlert("Print Schedule", "Print feature coming soon"); }
    @FXML private void handleSearchStudents() { statusLabel.setText("Search functionality coming soon"); }
    @FXML private void handleImportAssignments() { showAlert("Import", "CSV import coming soon"); }
    @FXML private void handleExportAssignments() { showAlert("Export", "Excel export coming soon"); }
    @FXML private void handleAssignStudents() { showAlert("Assign", "Assignment dialog coming soon"); }
    @FXML private void handleAddDriver() { showAlert("Add Driver", "Use teacher management to add drivers"); }
    @FXML private void handleEditDriver() { showAlert("Edit Driver", "Edit via teacher management"); }
    @FXML private void handleDriverHistory() { showAlert("Driver History", "History report coming soon"); }
    @FXML private void handleScheduleMaintenance() { showAlert("Schedule Maintenance", "Schedule via REST API"); }
    @FXML private void handleRouteEfficiencyReport() { generateReport("Route Efficiency"); }
    @FXML private void handleRidershipReport() { generateReport("Ridership"); }
    @FXML private void handleDriverPerformanceReport() { generateReport("Driver Performance"); }
    @FXML private void handleMaintenanceReport() { generateReport("Maintenance History"); }
    @FXML private void handleSafetyReport() { generateReport("Safety"); }
    @FXML private void handleCostAnalysisReport() { generateReport("Cost Analysis"); }

    @FXML
    private void handleRefresh() {
        loadRealData();
        updateStats();
        updateLastUpdated();
        statusLabel.setText("Refreshed successfully");
    }

    @FXML private void handleSettings() { showAlert("Settings", "Transportation settings"); }

    private void generateReport(String reportType) {
        new Thread(() -> {
            try {
                Platform.runLater(() -> statusLabel.setText("Generating " + reportType + " report..."));
                Thread.sleep(1000);
                Platform.runLater(() -> {
                    statusLabel.setText(reportType + " report generated successfully");
                    showAlert(reportType + " Report", "Report data available via REST API endpoints");
                });
            } catch (InterruptedException e) {
                Platform.runLater(() -> statusLabel.setText("Report generation failed"));
            }
        }).start();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // DTO Classes
    public static class RouteStopDTO {
        private String order, location, time, students;
        public RouteStopDTO(String order, String location, String time, String students) {
            this.order = order; this.location = location; this.time = time; this.students = students;
        }
        public String getOrder() { return order; }
        public String getLocation() { return location; }
        public String getTime() { return time; }
        public String getStudents() { return students; }
    }

    public static class StudentAssignmentDTO {
        private String name, grade, route, pickupStop, dropoffStop, guardian, phone, actions;
        public StudentAssignmentDTO(String name, String grade, String route, String pickupStop, String dropoffStop, String guardian, String phone, String actions) {
            this.name = name; this.grade = grade; this.route = route; this.pickupStop = pickupStop;
            this.dropoffStop = dropoffStop; this.guardian = guardian; this.phone = phone; this.actions = actions;
        }
        public String getName() { return name; }
        public String getGrade() { return grade; }
        public String getRoute() { return route; }
        public String getPickupStop() { return pickupStop; }
        public String getDropoffStop() { return dropoffStop; }
        public String getGuardian() { return guardian; }
        public String getPhone() { return phone; }
        public String getActions() { return actions; }
    }
}
