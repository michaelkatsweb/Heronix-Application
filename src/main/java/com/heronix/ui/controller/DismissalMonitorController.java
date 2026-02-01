package com.heronix.ui.controller;

import com.heronix.model.domain.DismissalEvent;
import com.heronix.model.domain.DismissalEvent.DismissalEventStatus;
import com.heronix.model.domain.DismissalEvent.DismissalEventType;
import com.heronix.model.domain.Student;
import com.heronix.repository.StudentRepository;
import com.heronix.service.DismissalEventService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DismissalMonitorController {

    private final DismissalEventService dismissalEventService;
    private final StudentRepository studentRepository;

    // Header
    @FXML private Label dateLabel;
    @FXML private CheckBox autoRefreshToggle;
    @FXML private ProgressIndicator loadingIndicator;

    // Stats
    @FXML private Label statBusArrivals;
    @FXML private Label statCarPickups;
    @FXML private Label statWalkers;
    @FXML private Label statAftercare;
    @FXML private Label statAthletics;
    @FXML private Label statCounselor;
    @FXML private Label statPending;
    @FXML private Label statDeparted;

    // Barcode scan
    @FXML private TextField barcodeScanField;

    // Filters
    @FXML private ComboBox<String> typeFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TextField searchField;
    @FXML private Label recordCountLabel;

    // Table
    @FXML private TableView<DismissalEvent> eventsTable;
    @FXML private TableColumn<DismissalEvent, String> typeColumn;
    @FXML private TableColumn<DismissalEvent, String> busNumberColumn;
    @FXML private TableColumn<DismissalEvent, String> studentNameColumn;
    @FXML private TableColumn<DismissalEvent, String> parentNameColumn;
    @FXML private TableColumn<DismissalEvent, String> vehicleInfoColumn;
    @FXML private TableColumn<DismissalEvent, String> sportMeetingColumn;
    @FXML private TableColumn<DismissalEvent, String> statusColumn;
    @FXML private TableColumn<DismissalEvent, String> arrivalTimeColumn;
    @FXML private TableColumn<DismissalEvent, String> calledTimeColumn;
    @FXML private TableColumn<DismissalEvent, String> laneColumn;
    @FXML private TableColumn<DismissalEvent, String> notesColumn;
    @FXML private TableColumn<DismissalEvent, Void> actionsColumn;

    private final ObservableList<DismissalEvent> allEvents = FXCollections.observableArrayList();
    private final ObservableList<DismissalEvent> filteredEvents = FXCollections.observableArrayList();
    private Timer autoRefreshTimer;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");

    @FXML
    public void initialize() {
        setupDate();
        setupFilters();
        setupTableColumns();
        setupRowFactory();
        setupAutoRefresh();
        loadEvents();
    }

    private void setupDate() {
        dateLabel.setText(LocalDate.now().format(DATE_FORMAT));
    }

    private void setupFilters() {
        typeFilter.setItems(FXCollections.observableArrayList(
                "All Types", "Bus Arrival", "Car Pickup", "Walker", "Aftercare", "Athletics", "Counselor Summon"));
        typeFilter.getSelectionModel().selectFirst();
        typeFilter.setOnAction(e -> applyFilters());

        statusFilter.setItems(FXCollections.observableArrayList(
                "All Statuses", "Pending", "Called", "Arrived", "Departed", "Cancelled"));
        statusFilter.getSelectionModel().selectFirst();
        statusFilter.setOnAction(e -> applyFilters());

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void setupTableColumns() {
        typeColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getEventType() != null ? cd.getValue().getEventType().getDisplayName() : ""));

        busNumberColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getBusNumber() != null ? cd.getValue().getBusNumber() : ""));

        studentNameColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getStudentName() != null ? cd.getValue().getStudentName() : ""));

        parentNameColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getParentName() != null ? cd.getValue().getParentName() : ""));

        vehicleInfoColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getParentVehicleInfo() != null ? cd.getValue().getParentVehicleInfo() : ""));

        sportMeetingColumn.setCellValueFactory(cd -> {
            DismissalEvent ev = cd.getValue();
            if (ev.getSportName() != null && !ev.getSportName().isEmpty()) return new SimpleStringProperty(ev.getSportName());
            if (ev.getMeetingType() != null && !ev.getMeetingType().isEmpty()) return new SimpleStringProperty(ev.getMeetingType());
            return new SimpleStringProperty("");
        });

        statusColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getStatus() != null ? cd.getValue().getStatus().getDisplayName() : ""));

        arrivalTimeColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getArrivalTime() != null ? cd.getValue().getArrivalTime().format(TIME_FORMAT) : ""));

        calledTimeColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getCalledTime() != null ? cd.getValue().getCalledTime().format(TIME_FORMAT) : ""));

        laneColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getLaneNumber() != null ? cd.getValue().getLaneNumber().toString() : ""));

        notesColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getNotes() != null ? cd.getValue().getNotes() : ""));

        setupActionsColumn();
        eventsTable.setItems(filteredEvents);
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button callBtn = new Button("Call");
            private final Button departedBtn = new Button("Departed");
            private final Button cancelBtn = new Button("Cancel");
            private final HBox box = new HBox(5, callBtn, departedBtn, cancelBtn);

            {
                box.setAlignment(Pos.CENTER);
                callBtn.setStyle("-fx-font-size: 11;");
                departedBtn.setStyle("-fx-font-size: 11;");
                cancelBtn.setStyle("-fx-font-size: 11;");

                callBtn.setOnAction(e -> {
                    DismissalEvent event = getTableView().getItems().get(getIndex());
                    handleCallStudent(event);
                });
                departedBtn.setOnAction(e -> {
                    DismissalEvent event = getTableView().getItems().get(getIndex());
                    handleMarkDeparted(event);
                });
                cancelBtn.setOnAction(e -> {
                    DismissalEvent event = getTableView().getItems().get(getIndex());
                    handleCancelEvent(event);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    DismissalEvent event = getTableView().getItems().get(getIndex());
                    boolean isDeparted = event.getStatus() == DismissalEventStatus.DEPARTED;
                    boolean isCancelled = event.getStatus() == DismissalEventStatus.CANCELLED;
                    callBtn.setDisable(isDeparted || isCancelled);
                    departedBtn.setDisable(isDeparted || isCancelled);
                    cancelBtn.setDisable(isDeparted || isCancelled);
                    setGraphic(box);
                }
            }
        });
    }

    private void setupRowFactory() {
        eventsTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(DismissalEvent item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    String bgColor;
                    switch (item.getEventType()) {
                        case BUS_ARRIVAL -> bgColor = "#FFF3E0"; // Orange
                        case CAR_PICKUP -> bgColor = "#E3F2FD";  // Light blue
                        case WALKER -> bgColor = "#E8F5E9";      // Light green
                        case AFTERCARE -> bgColor = "#F3E5F5";   // Light purple
                        case ATHLETICS -> bgColor = "#FFF9C4";   // Light yellow
                        case COUNSELOR_SUMMON -> bgColor = "#FFECB3"; // Amber
                        default -> bgColor = "transparent";
                    }

                    if (item.getStatus() == DismissalEventStatus.DEPARTED) {
                        setStyle("-fx-background-color: " + bgColor + "; -fx-opacity: 0.5; -fx-font-weight: bold;");
                    } else if (item.getStatus() == DismissalEventStatus.CANCELLED) {
                        setStyle("-fx-background-color: #EEEEEE; -fx-opacity: 0.5; -fx-text-fill: gray;");
                    } else {
                        setStyle("-fx-background-color: " + bgColor + "; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void setupAutoRefresh() {
        autoRefreshToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                startAutoRefresh();
            } else {
                stopAutoRefresh();
            }
        });
        startAutoRefresh();
    }

    private void startAutoRefresh() {
        stopAutoRefresh();
        autoRefreshTimer = new Timer(true);
        autoRefreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> loadEvents());
            }
        }, 10000, 10000); // Every 10 seconds
    }

    private void stopAutoRefresh() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.cancel();
            autoRefreshTimer = null;
        }
    }

    private void loadEvents() {
        try {
            loadingIndicator.setVisible(true);
            List<DismissalEvent> events = dismissalEventService.getTodaysEvents();
            allEvents.setAll(events);
            applyFilters();
            updateStats();
        } catch (Exception e) {
            log.error("Failed to load dismissal events", e);
        } finally {
            loadingIndicator.setVisible(false);
        }
    }

    private void applyFilters() {
        String typeVal = typeFilter.getValue();
        String statusVal = statusFilter.getValue();
        String searchText = searchField.getText() != null ? searchField.getText().toLowerCase().trim() : "";

        List<DismissalEvent> filtered = allEvents.stream()
                .filter(ev -> {
                    if (typeVal == null || "All Types".equals(typeVal)) return true;
                    return ev.getEventType() != null && ev.getEventType().getDisplayName().equals(typeVal);
                })
                .filter(ev -> {
                    if (statusVal == null || "All Statuses".equals(statusVal)) return true;
                    return ev.getStatus() != null && ev.getStatus().getDisplayName().equals(statusVal);
                })
                .filter(ev -> {
                    if (searchText.isEmpty()) return true;
                    String bus = ev.getBusNumber() != null ? ev.getBusNumber().toLowerCase() : "";
                    String student = ev.getStudentName() != null ? ev.getStudentName().toLowerCase() : "";
                    String parent = ev.getParentName() != null ? ev.getParentName().toLowerCase() : "";
                    return bus.contains(searchText) || student.contains(searchText) || parent.contains(searchText);
                })
                .collect(Collectors.toList());

        filteredEvents.setAll(filtered);
        recordCountLabel.setText(filtered.size() + " event" + (filtered.size() != 1 ? "s" : ""));
    }

    private void updateStats() {
        try {
            Map<String, Object> stats = dismissalEventService.getTodaysBoardStats();
            statBusArrivals.setText(String.valueOf(stats.getOrDefault("busArrivals", 0L)));
            statCarPickups.setText(String.valueOf(stats.getOrDefault("carPickups", 0L)));
            statWalkers.setText(String.valueOf(stats.getOrDefault("walkers", 0L)));
            statAftercare.setText(String.valueOf(stats.getOrDefault("aftercare", 0L)));
            statAthletics.setText(String.valueOf(stats.getOrDefault("athletics", 0L)));
            statCounselor.setText(String.valueOf(stats.getOrDefault("counselorSummons", 0L)));
            statPending.setText(String.valueOf(stats.getOrDefault("pending", 0L)));
            statDeparted.setText(String.valueOf(stats.getOrDefault("departed", 0L)));
        } catch (Exception e) {
            log.error("Failed to load board stats", e);
        }
    }

    // ========== Event Handlers ==========

    @FXML
    private void handleBarcodeScan() {
        String barcode = barcodeScanField.getText();
        if (barcode == null || barcode.trim().isEmpty()) return;

        try {
            dismissalEventService.registerBusArrival(barcode.trim(), barcode.trim());
            showInfo("Bus #" + barcode.trim() + " registered as arrived.");
            barcodeScanField.clear();
            barcodeScanField.requestFocus();
            loadEvents();
        } catch (IllegalStateException e) {
            showWarning(e.getMessage());
        } catch (Exception e) {
            showError("Failed to register bus arrival: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddCarPickup() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Car Pickup");
        dialog.setHeaderText("Enter Student ID:");
        dialog.setContentText("Student ID:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().trim().isEmpty()) return;

        try {
            Long studentId = Long.parseLong(result.get().trim());

            TextInputDialog parentDialog = new TextInputDialog();
            parentDialog.setTitle("Parent Information");
            parentDialog.setHeaderText("Enter parent name:");
            parentDialog.setContentText("Parent Name:");
            Optional<String> parentResult = parentDialog.showAndWait();

            String parentName = parentResult.map(String::trim).orElse("");

            TextInputDialog vehicleDialog = new TextInputDialog();
            vehicleDialog.setTitle("Vehicle Information");
            vehicleDialog.setHeaderText("Enter vehicle description (optional):");
            vehicleDialog.setContentText("Vehicle Info:");
            Optional<String> vehicleResult = vehicleDialog.showAndWait();

            String vehicleInfo = vehicleResult.map(String::trim).orElse("");

            dismissalEventService.registerCarPickup(studentId, parentName, vehicleInfo);
            showInfo("Car pickup registered successfully.");
            loadEvents();
        } catch (NumberFormatException e) {
            showError("Invalid student ID.");
        } catch (Exception e) {
            showError("Failed to register car pickup: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddWalker() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Walker");
        dialog.setHeaderText("Enter Student ID:");
        dialog.setContentText("Student ID:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().trim().isEmpty()) return;

        try {
            Long studentId = Long.parseLong(result.get().trim());
            dismissalEventService.registerWalker(studentId);
            showInfo("Walker registered successfully.");
            loadEvents();
        } catch (NumberFormatException e) {
            showError("Invalid student ID.");
        } catch (Exception e) {
            showError("Failed to register walker: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddAftercare() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Aftercare");
        dialog.setHeaderText("Enter Student ID:");
        dialog.setContentText("Student ID:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().trim().isEmpty()) return;

        try {
            Long studentId = Long.parseLong(result.get().trim());
            dismissalEventService.registerAftercare(studentId);
            showInfo("Aftercare registered successfully.");
            loadEvents();
        } catch (NumberFormatException e) {
            showError("Invalid student ID.");
        } catch (Exception e) {
            showError("Failed to register aftercare: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddAthletics() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Athletics");
        dialog.setHeaderText("Enter Student ID:");
        dialog.setContentText("Student ID:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().trim().isEmpty()) return;

        try {
            Long studentId = Long.parseLong(result.get().trim());

            TextInputDialog sportDialog = new TextInputDialog();
            sportDialog.setTitle("Sport");
            sportDialog.setHeaderText("Enter sport name (e.g., Football, Tennis, Swimming):");
            sportDialog.setContentText("Sport:");
            Optional<String> sportResult = sportDialog.showAndWait();
            String sportName = sportResult.map(String::trim).orElse("");

            dismissalEventService.registerAthletics(studentId, sportName);
            showInfo("Athletics dismissal registered successfully.");
            loadEvents();
        } catch (NumberFormatException e) {
            showError("Invalid student ID.");
        } catch (Exception e) {
            showError("Failed to register athletics: " + e.getMessage());
        }
    }

    @FXML
    private void handleCounselorSummon() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Counselor Summon");
        dialog.setHeaderText("Enter Student ID:");
        dialog.setContentText("Student ID:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().trim().isEmpty()) return;

        try {
            Long studentId = Long.parseLong(result.get().trim());

            TextInputDialog counselorDialog = new TextInputDialog();
            counselorDialog.setTitle("Counselor Name");
            counselorDialog.setHeaderText("Enter counselor name:");
            counselorDialog.setContentText("Counselor:");
            Optional<String> counselorResult = counselorDialog.showAndWait();
            String counselorName = counselorResult.map(String::trim).orElse("");

            TextInputDialog meetingDialog = new TextInputDialog();
            meetingDialog.setTitle("Meeting Type");
            meetingDialog.setHeaderText("Enter meeting type (e.g., 504, IEP, Guidance, Disciplinary):");
            meetingDialog.setContentText("Meeting Type:");
            Optional<String> meetingResult = meetingDialog.showAndWait();
            String meetingType = meetingResult.map(String::trim).orElse("");

            dismissalEventService.registerCounselorSummon(studentId, counselorName, meetingType);
            showInfo("Counselor summon registered. Student and teacher will be notified.");
            loadEvents();
        } catch (NumberFormatException e) {
            showError("Invalid student ID.");
        } catch (Exception e) {
            showError("Failed to register counselor summon: " + e.getMessage());
        }
    }

    private void handleCallStudent(DismissalEvent event) {
        try {
            dismissalEventService.callStudent(event.getId());
            loadEvents();
        } catch (Exception e) {
            showError("Failed to call student: " + e.getMessage());
        }
    }

    private void handleMarkDeparted(DismissalEvent event) {
        try {
            dismissalEventService.markDeparted(event.getId());
            loadEvents();
        } catch (Exception e) {
            showError("Failed to mark departed: " + e.getMessage());
        }
    }

    private void handleCancelEvent(DismissalEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Cancel this dismissal event?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    dismissalEventService.cancelEvent(event.getId());
                    loadEvents();
                } catch (Exception e) {
                    showError("Failed to cancel event: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleClearFilters() {
        typeFilter.getSelectionModel().selectFirst();
        statusFilter.getSelectionModel().selectFirst();
        searchField.clear();
    }

    @FXML
    private void handleRefresh() {
        loadEvents();
    }

    private void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK).showAndWait();
    }

    private void showWarning(String message) {
        new Alert(Alert.AlertType.WARNING, message, ButtonType.OK).showAndWait();
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }
}
