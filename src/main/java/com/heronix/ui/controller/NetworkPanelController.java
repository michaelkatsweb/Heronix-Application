package com.heronix.ui.controller;

import com.heronix.model.domain.NetworkDevice;
import com.heronix.model.enums.NetworkDeviceStatus;
import com.heronix.model.enums.NetworkDeviceType;
import com.heronix.service.NetworkService;
import com.heronix.service.NetworkService.NetworkHealthSummary;
import com.heronix.service.NetworkService.PingResult;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Controller for the Network Panel.
 *
 * Provides functionality for:
 * - Dashboard overview with statistics
 * - Device management (CRUD)
 * - Network tools (ping, port scan)
 * - Real-time monitoring
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since January 21, 2026
 */
@Component
@Scope("prototype")
@Slf4j
public class NetworkPanelController implements Initializable {

    @Autowired
    private NetworkService networkService;

    // ==================== Header ====================
    @FXML private Label panelTitleLabel;
    @FXML private Circle networkHealthIndicator;
    @FXML private Label networkHealthLabel;
    @FXML private CheckBox autoRefreshCheckbox;
    @FXML private Button refreshAllButton;
    @FXML private Label lastRefreshLabel;

    // ==================== Dashboard Tab ====================
    @FXML private Label totalDevicesLabel;
    @FXML private Label onlineDevicesLabel;
    @FXML private Label offlineDevicesLabel;
    @FXML private Label warningDevicesLabel;
    @FXML private Label maintenanceDevicesLabel;
    @FXML private Label avgLatencyLabel;
    @FXML private PieChart statusPieChart;
    @FXML private PieChart typePieChart;
    @FXML private Label attentionCountLabel;
    @FXML private TableView<NetworkDevice> attentionTable;
    @FXML private TableColumn<NetworkDevice, String> attentionNameColumn;
    @FXML private TableColumn<NetworkDevice, String> attentionIpColumn;
    @FXML private TableColumn<NetworkDevice, String> attentionTypeColumn;
    @FXML private TableColumn<NetworkDevice, String> attentionStatusColumn;
    @FXML private TableColumn<NetworkDevice, String> attentionLocationColumn;
    @FXML private TableColumn<NetworkDevice, String> attentionLastPingColumn;

    // ==================== Device Management Tab ====================
    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeFilterCombo;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private ComboBox<String> locationFilterCombo;
    @FXML private Button addDeviceButton;
    @FXML private Button pingSelectedButton;
    @FXML private Button exportButton;
    @FXML private TableView<NetworkDevice> deviceTable;
    @FXML private TableColumn<NetworkDevice, Boolean> selectColumn;
    @FXML private TableColumn<NetworkDevice, String> statusIndicatorColumn;
    @FXML private TableColumn<NetworkDevice, String> deviceNameColumn;
    @FXML private TableColumn<NetworkDevice, String> ipAddressColumn;
    @FXML private TableColumn<NetworkDevice, String> macAddressColumn;
    @FXML private TableColumn<NetworkDevice, String> deviceTypeColumn;
    @FXML private TableColumn<NetworkDevice, String> statusColumn;
    @FXML private TableColumn<NetworkDevice, String> locationColumn;
    @FXML private TableColumn<NetworkDevice, String> latencyColumn;
    @FXML private TableColumn<NetworkDevice, String> lastPingColumn;
    @FXML private TableColumn<NetworkDevice, Void> actionsColumn;
    @FXML private Label deviceCountLabel;

    // ==================== Network Tools Tab ====================
    @FXML private TextField pingIpField;
    @FXML private Spinner<Integer> pingTimeoutSpinner;
    @FXML private Button singlePingButton;
    @FXML private Button continuousPingButton;
    @FXML private Button stopPingButton;
    @FXML private Label pingResultLabel;
    @FXML private TextField portScanIpField;
    @FXML private Spinner<Integer> startPortSpinner;
    @FXML private Spinner<Integer> endPortSpinner;
    @FXML private TextArea outputArea;

    // ==================== Add/Edit Form ====================
    @FXML private Tab addEditTab;
    @FXML private TabPane mainTabPane;
    @FXML private Label formTitleLabel;
    @FXML private TextField formDeviceName;
    @FXML private TextField formIpAddress;
    @FXML private TextField formMacAddress;
    @FXML private ComboBox<NetworkDeviceType> formDeviceType;
    @FXML private TextField formLocation;
    @FXML private TextArea formDescription;
    @FXML private TextField formManufacturer;
    @FXML private TextField formModel;
    @FXML private TextField formSerialNumber;
    @FXML private TextField formHostname;
    @FXML private CheckBox formMonitoringEnabled;
    @FXML private Spinner<Integer> formPingInterval;
    @FXML private Spinner<Integer> formPingTimeout;
    @FXML private Spinner<Integer> formPort;
    @FXML private CheckBox formAlertOnOffline;
    @FXML private TextField formAlertEmail;
    @FXML private TextArea formNotes;

    // ==================== Footer ====================
    @FXML private Label footerStatusLabel;
    @FXML private Label footerHealthLabel;

    // ==================== Internal State ====================
    private ObservableList<NetworkDevice> allDevices = FXCollections.observableArrayList();
    private FilteredList<NetworkDevice> filteredDevices;
    private Map<Long, SimpleBooleanProperty> deviceSelectionMap = new HashMap<>();
    private NetworkDevice editingDevice = null;
    private Timeline autoRefreshTimeline;
    private Timeline continuousPingTimeline;
    private AtomicBoolean isContinuousPingRunning = new AtomicBoolean(false);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeFilters();
        initializeDeviceTable();
        initializeAttentionTable();
        initializeNetworkTools();
        initializeForm();
        initializeAutoRefresh();
        loadData();
    }

    // ==================== Initialization ====================

    private void initializeFilters() {
        // Type filter
        typeFilterCombo.getItems().add("All Types");
        for (NetworkDeviceType type : NetworkDeviceType.values()) {
            typeFilterCombo.getItems().add(type.getDisplayName());
        }
        typeFilterCombo.setValue("All Types");

        // Status filter
        statusFilterCombo.getItems().add("All Statuses");
        for (NetworkDeviceStatus status : NetworkDeviceStatus.values()) {
            statusFilterCombo.getItems().add(status.getDisplayName());
        }
        statusFilterCombo.setValue("All Statuses");

        // Location filter will be populated dynamically
        locationFilterCombo.getItems().add("All Locations");
        locationFilterCombo.setValue("All Locations");
    }

    private void initializeDeviceTable() {
        // Selection column
        selectColumn.setCellValueFactory(cellData -> {
            NetworkDevice device = cellData.getValue();
            SimpleBooleanProperty prop = deviceSelectionMap.computeIfAbsent(
                    device.getId(), k -> new SimpleBooleanProperty(false));
            return prop;
        });
        selectColumn.setCellFactory(col -> new CheckBoxTableCell<>());

        // Status indicator column
        statusIndicatorColumn.setCellFactory(col -> new TableCell<>() {
            private final Circle circle = new Circle(6);
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    NetworkDevice device = getTableRow().getItem();
                    circle.setFill(Color.web(device.getStatus().getColor()));
                    setGraphic(circle);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Text columns
        deviceNameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDeviceName()));
        ipAddressColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getIpAddress()));
        macAddressColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getMacAddress() != null ? c.getValue().getMacAddress() : ""));
        deviceTypeColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDeviceType().getDisplayName()));
        statusColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStatus().getDisplayName()));
        locationColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getLocation() != null ? c.getValue().getLocation() : ""));
        latencyColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getFormattedLatency()));
        lastPingColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getLastPingTime() != null ?
                        c.getValue().getLastPingTime().format(DATE_TIME_FORMATTER) : "Never"));

        // Actions column
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button pingBtn = new Button("Ping");
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox buttons = new HBox(5, pingBtn, editBtn, deleteBtn);

            {
                pingBtn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-size: 10px;");
                editBtn.setStyle("-fx-font-size: 10px;");
                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 10px;");

                pingBtn.setOnAction(e -> handlePingDevice(getTableRow().getItem()));
                editBtn.setOnAction(e -> handleEditDevice(getTableRow().getItem()));
                deleteBtn.setOnAction(e -> handleDeleteDevice(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });

        // Filtered list
        filteredDevices = new FilteredList<>(allDevices, p -> true);
        deviceTable.setItems(filteredDevices);
    }

    private void initializeAttentionTable() {
        attentionNameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDeviceName()));
        attentionIpColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getIpAddress()));
        attentionTypeColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDeviceType().getDisplayName()));
        attentionStatusColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStatus().getDisplayName()));
        attentionLocationColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getLocation() != null ? c.getValue().getLocation() : ""));
        attentionLastPingColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getLastPingTime() != null ?
                        c.getValue().getLastPingTime().format(DATE_TIME_FORMATTER) : "Never"));
    }

    private void initializeNetworkTools() {
        pingTimeoutSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(100, 30000, 5000, 100));
        startPortSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 65535, 1, 1));
        endPortSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 65535, 1024, 1));
    }

    private void initializeForm() {
        formDeviceType.getItems().addAll(NetworkDeviceType.values());
        formPingInterval.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 3600, 60, 10));
        formPingTimeout.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(100, 30000, 5000, 100));
        formPort.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 65535, 0, 1));

        // Hide the add/edit tab initially
        addEditTab.setDisable(true);
    }

    private void initializeAutoRefresh() {
        autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(60), e -> {
            if (autoRefreshCheckbox.isSelected()) {
                loadData();
            }
        }));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    // ==================== Data Loading ====================

    private void loadData() {
        CompletableFuture.runAsync(() -> {
            try {
                List<NetworkDevice> devices = networkService.getAllDevices();
                NetworkHealthSummary summary = networkService.getNetworkHealthSummary();
                List<NetworkDevice> attentionDevices = networkService.getDevicesRequiringAttention();
                List<String> locations = networkService.getUniqueLocations();

                Platform.runLater(() -> {
                    // Update device list
                    allDevices.setAll(devices);
                    deviceCountLabel.setText(devices.size() + " devices");

                    // Update statistics
                    updateStatistics(summary);

                    // Update attention table
                    attentionTable.getItems().setAll(attentionDevices);
                    attentionCountLabel.setText("(" + attentionDevices.size() + ")");

                    // Update charts
                    updateCharts();

                    // Update location filter
                    String currentLocation = locationFilterCombo.getValue();
                    locationFilterCombo.getItems().clear();
                    locationFilterCombo.getItems().add("All Locations");
                    locationFilterCombo.getItems().addAll(locations);
                    locationFilterCombo.setValue(currentLocation != null ? currentLocation : "All Locations");

                    // Update last refresh time
                    lastRefreshLabel.setText("Last refresh: " + LocalDateTime.now().format(DATE_TIME_FORMATTER));
                    footerStatusLabel.setText("Ready - " + devices.size() + " devices loaded");
                });
            } catch (Exception e) {
                log.error("Error loading network data", e);
                Platform.runLater(() -> {
                    footerStatusLabel.setText("Error loading data: " + e.getMessage());
                });
            }
        });
    }

    private void updateStatistics(NetworkHealthSummary summary) {
        totalDevicesLabel.setText(String.valueOf(summary.getTotalDevices()));
        onlineDevicesLabel.setText(String.valueOf(summary.getOnlineDevices()));
        offlineDevicesLabel.setText(String.valueOf(summary.getOfflineDevices()));
        warningDevicesLabel.setText(String.valueOf(summary.getWarningDevices()));
        maintenanceDevicesLabel.setText(String.valueOf(summary.getMaintenanceDevices()));
        avgLatencyLabel.setText(String.format("%.0f ms", summary.getAverageLatencyMs()));

        // Update health indicator
        double health = summary.getHealthPercentage();
        if (health >= 95) {
            networkHealthIndicator.setFill(Color.web("#4caf50"));
            networkHealthLabel.setText("Network Healthy");
        } else if (health >= 85) {
            networkHealthIndicator.setFill(Color.web("#8bc34a"));
            networkHealthLabel.setText("Network Good");
        } else if (health >= 70) {
            networkHealthIndicator.setFill(Color.web("#ffc107"));
            networkHealthLabel.setText("Network Fair");
        } else {
            networkHealthIndicator.setFill(Color.web("#f44336"));
            networkHealthLabel.setText("Network Issues");
        }

        footerHealthLabel.setText("Network Health: " + String.format("%.1f%%", health) + " (" + summary.getHealthStatus() + ")");
    }

    private void updateCharts() {
        // Status pie chart
        Map<NetworkDeviceStatus, Long> statusCounts = networkService.getStatusCounts();
        ObservableList<PieChart.Data> statusData = FXCollections.observableArrayList();
        for (Map.Entry<NetworkDeviceStatus, Long> entry : statusCounts.entrySet()) {
            if (entry.getValue() > 0) {
                statusData.add(new PieChart.Data(entry.getKey().getDisplayName(), entry.getValue()));
            }
        }
        statusPieChart.setData(statusData);

        // Type pie chart
        Map<NetworkDeviceType, Long> typeCounts = networkService.getTypeCounts();
        ObservableList<PieChart.Data> typeData = FXCollections.observableArrayList();
        for (Map.Entry<NetworkDeviceType, Long> entry : typeCounts.entrySet()) {
            if (entry.getValue() > 0) {
                typeData.add(new PieChart.Data(entry.getKey().getDisplayName(), entry.getValue()));
            }
        }
        typePieChart.setData(typeData);
    }

    // ==================== Event Handlers ====================

    @FXML
    private void handleRefreshAll() {
        loadData();
    }

    @FXML
    private void handleSearch() {
        applyFilters();
    }

    @FXML
    private void handleFilterChange() {
        applyFilters();
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String typeFilter = typeFilterCombo.getValue();
        String statusFilter = statusFilterCombo.getValue();
        String locationFilter = locationFilterCombo.getValue();

        filteredDevices.setPredicate(device -> {
            // Search filter
            if (!searchText.isEmpty()) {
                boolean matches = device.getDeviceName().toLowerCase().contains(searchText) ||
                        device.getIpAddress().toLowerCase().contains(searchText) ||
                        (device.getMacAddress() != null && device.getMacAddress().toLowerCase().contains(searchText)) ||
                        (device.getLocation() != null && device.getLocation().toLowerCase().contains(searchText));
                if (!matches) return false;
            }

            // Type filter
            if (typeFilter != null && !typeFilter.equals("All Types")) {
                if (!device.getDeviceType().getDisplayName().equals(typeFilter)) return false;
            }

            // Status filter
            if (statusFilter != null && !statusFilter.equals("All Statuses")) {
                if (!device.getStatus().getDisplayName().equals(statusFilter)) return false;
            }

            // Location filter
            if (locationFilter != null && !locationFilter.equals("All Locations")) {
                if (device.getLocation() == null || !device.getLocation().equals(locationFilter)) return false;
            }

            return true;
        });

        deviceCountLabel.setText(filteredDevices.size() + " devices" +
                (filteredDevices.size() != allDevices.size() ? " (filtered)" : ""));
    }

    @FXML
    private void handleAddDevice() {
        editingDevice = null;
        clearForm();
        formTitleLabel.setText("Add New Device");
        addEditTab.setDisable(false);
        mainTabPane.getSelectionModel().select(addEditTab);
    }

    private void handleEditDevice(NetworkDevice device) {
        editingDevice = device;
        populateForm(device);
        formTitleLabel.setText("Edit Device: " + device.getDeviceName());
        addEditTab.setDisable(false);
        mainTabPane.getSelectionModel().select(addEditTab);
    }

    private void handleDeleteDevice(NetworkDevice device) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Device");
        confirm.setHeaderText("Delete " + device.getDeviceName() + "?");
        confirm.setContentText("This action cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    networkService.deleteDevice(device.getId());
                    loadData();
                    appendOutput("Deleted device: " + device.getDeviceName());
                } catch (Exception e) {
                    showError("Error deleting device", e.getMessage());
                }
            }
        });
    }

    private void handlePingDevice(NetworkDevice device) {
        footerStatusLabel.setText("Pinging " + device.getDeviceName() + "...");
        CompletableFuture.runAsync(() -> {
            PingResult result = networkService.pingDevice(device);
            networkService.updateDeviceAfterPing(device, result);

            Platform.runLater(() -> {
                if (result.isReachable()) {
                    appendOutput("Ping " + device.getIpAddress() + " (" + device.getDeviceName() + "): " +
                            result.getLatencyMs() + " ms");
                } else {
                    appendOutput("Ping " + device.getIpAddress() + " (" + device.getDeviceName() + "): FAILED" +
                            (result.getErrorMessage() != null ? " - " + result.getErrorMessage() : ""));
                }
                loadData();
            });
        });
    }

    @FXML
    private void handlePingSelected() {
        List<NetworkDevice> selected = getSelectedDevices();
        if (selected.isEmpty()) {
            showInfo("No devices selected", "Please select one or more devices to ping.");
            return;
        }

        footerStatusLabel.setText("Pinging " + selected.size() + " devices...");
        appendOutput("Starting ping for " + selected.size() + " devices...");

        networkService.pingDevices(selected).thenAccept(results -> {
            Platform.runLater(() -> {
                for (PingResult result : results) {
                    if (result.getDevice() != null) {
                        String msg = "Ping " + result.getDevice().getIpAddress() + ": " +
                                (result.isReachable() ? result.getLatencyMs() + " ms" : "FAILED");
                        appendOutput(msg);
                    }
                }
                loadData();
                footerStatusLabel.setText("Ping complete");
            });
        });
    }

    @FXML
    private void handleSelectAll() {
        for (NetworkDevice device : filteredDevices) {
            deviceSelectionMap.computeIfAbsent(device.getId(), k -> new SimpleBooleanProperty(false)).set(true);
        }
    }

    @FXML
    private void handleDeselectAll() {
        deviceSelectionMap.values().forEach(prop -> prop.set(false));
    }

    private List<NetworkDevice> getSelectedDevices() {
        List<NetworkDevice> selected = new ArrayList<>();
        for (NetworkDevice device : allDevices) {
            SimpleBooleanProperty prop = deviceSelectionMap.get(device.getId());
            if (prop != null && prop.get()) {
                selected.add(device);
            }
        }
        return selected;
    }

    @FXML
    private void handleExport() {
        // TODO: Implement CSV export
        showInfo("Export", "Export functionality will be implemented.");
    }

    // ==================== Network Tools ====================

    @FXML
    private void handleSinglePing() {
        String ip = pingIpField.getText().trim();
        if (ip.isEmpty()) {
            pingResultLabel.setText("Please enter an IP address");
            return;
        }

        int timeout = pingTimeoutSpinner.getValue();
        pingResultLabel.setText("Pinging " + ip + "...");

        CompletableFuture.runAsync(() -> {
            PingResult result = networkService.pingIpAddress(ip, timeout);
            Platform.runLater(() -> {
                if (result.isReachable()) {
                    pingResultLabel.setText("Reply from " + ip + ": time=" + result.getLatencyMs() + "ms");
                    appendOutput("Ping " + ip + ": " + result.getLatencyMs() + " ms");
                } else {
                    pingResultLabel.setText("Request timed out for " + ip);
                    appendOutput("Ping " + ip + ": FAILED");
                }
            });
        });
    }

    @FXML
    private void handleContinuousPing() {
        String ip = pingIpField.getText().trim();
        if (ip.isEmpty()) {
            pingResultLabel.setText("Please enter an IP address");
            return;
        }

        isContinuousPingRunning.set(true);
        continuousPingButton.setDisable(true);
        stopPingButton.setDisable(false);
        singlePingButton.setDisable(true);

        int timeout = pingTimeoutSpinner.getValue();
        appendOutput("Starting continuous ping to " + ip + "...");

        continuousPingTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (!isContinuousPingRunning.get()) return;

            CompletableFuture.runAsync(() -> {
                PingResult result = networkService.pingIpAddress(ip, timeout);
                Platform.runLater(() -> {
                    String msg = result.isReachable() ?
                            "Reply from " + ip + ": time=" + result.getLatencyMs() + "ms" :
                            "Request timed out for " + ip;
                    pingResultLabel.setText(msg);
                    appendOutput(msg);
                });
            });
        }));
        continuousPingTimeline.setCycleCount(Timeline.INDEFINITE);
        continuousPingTimeline.play();
    }

    @FXML
    private void handleStopPing() {
        isContinuousPingRunning.set(false);
        if (continuousPingTimeline != null) {
            continuousPingTimeline.stop();
        }
        continuousPingButton.setDisable(false);
        stopPingButton.setDisable(true);
        singlePingButton.setDisable(false);
        pingResultLabel.setText("Continuous ping stopped");
        appendOutput("Continuous ping stopped");
    }

    @FXML
    private void handleScanCommonPorts() {
        String ip = portScanIpField.getText().trim();
        if (ip.isEmpty()) {
            showInfo("Port Scan", "Please enter an IP address");
            return;
        }

        appendOutput("Scanning common ports on " + ip + "...");
        footerStatusLabel.setText("Scanning ports...");

        CompletableFuture.runAsync(() -> {
            Map<Integer, Boolean> results = networkService.scanCommonPorts(ip, 2000);
            Platform.runLater(() -> {
                appendOutput("Port scan results for " + ip + ":");
                for (Map.Entry<Integer, Boolean> entry : results.entrySet()) {
                    appendOutput("  Port " + entry.getKey() + ": " + (entry.getValue() ? "OPEN" : "CLOSED"));
                }
                footerStatusLabel.setText("Port scan complete");
            });
        });
    }

    @FXML
    private void handleScanPortRange() {
        String ip = portScanIpField.getText().trim();
        if (ip.isEmpty()) {
            showInfo("Port Scan", "Please enter an IP address");
            return;
        }

        int start = startPortSpinner.getValue();
        int end = endPortSpinner.getValue();

        if (end < start) {
            showInfo("Port Scan", "End port must be greater than start port");
            return;
        }

        if (end - start > 100) {
            showInfo("Port Scan", "Please limit range to 100 ports for performance");
            return;
        }

        appendOutput("Scanning ports " + start + "-" + end + " on " + ip + "...");
        footerStatusLabel.setText("Scanning ports...");

        CompletableFuture.runAsync(() -> {
            Map<Integer, Boolean> results = networkService.scanPortRange(ip, start, end, 1000);
            Platform.runLater(() -> {
                appendOutput("Port scan results for " + ip + " (" + start + "-" + end + "):");
                for (Map.Entry<Integer, Boolean> entry : results.entrySet()) {
                    if (entry.getValue()) {
                        appendOutput("  Port " + entry.getKey() + ": OPEN");
                    }
                }
                footerStatusLabel.setText("Port scan complete");
            });
        });
    }

    @FXML
    private void handlePingAllDevices() {
        appendOutput("Pinging all monitored devices...");
        footerStatusLabel.setText("Pinging all devices...");

        networkService.pingAllDevices().thenAccept(results -> {
            Platform.runLater(() -> {
                int online = 0, offline = 0;
                for (PingResult result : results) {
                    if (result.isReachable()) online++;
                    else offline++;
                }
                appendOutput("Ping complete: " + online + " online, " + offline + " offline");
                loadData();
                footerStatusLabel.setText("Ping complete");
            });
        });
    }

    @FXML
    private void handleCheckOfflineDevices() {
        List<NetworkDevice> offline = networkService.getOfflineDevices();
        if (offline.isEmpty()) {
            appendOutput("No offline devices found");
            return;
        }

        appendOutput("Checking " + offline.size() + " offline devices...");
        networkService.pingDevices(offline).thenAccept(results -> {
            Platform.runLater(() -> {
                int recovered = 0;
                for (PingResult result : results) {
                    if (result.isReachable()) {
                        recovered++;
                        appendOutput("RECOVERED: " + result.getDevice().getDeviceName() +
                                " (" + result.getDevice().getIpAddress() + ")");
                    }
                }
                appendOutput("Check complete: " + recovered + " devices recovered");
                loadData();
            });
        });
    }

    @FXML
    private void handleGenerateReport() {
        // TODO: Implement report generation
        showInfo("Report", "Report generation will be implemented.");
    }

    @FXML
    private void handleClearOutput() {
        outputArea.clear();
    }

    private void appendOutput(String text) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        outputArea.appendText("[" + timestamp + "] " + text + "\n");
    }

    // ==================== Form Handlers ====================

    @FXML
    private void handleFormCancel() {
        addEditTab.setDisable(true);
        mainTabPane.getSelectionModel().select(1); // Go to Device Management tab
        clearForm();
    }

    @FXML
    private void handleFormTestConnection() {
        String ip = formIpAddress.getText().trim();
        if (ip.isEmpty()) {
            showInfo("Test Connection", "Please enter an IP address");
            return;
        }

        footerStatusLabel.setText("Testing connection to " + ip + "...");
        CompletableFuture.runAsync(() -> {
            PingResult result = networkService.pingIpAddress(ip, formPingTimeout.getValue());
            Platform.runLater(() -> {
                if (result.isReachable()) {
                    showInfo("Test Connection", "Connection successful! Latency: " + result.getLatencyMs() + " ms");
                } else {
                    showInfo("Test Connection", "Connection failed. Device may be offline or unreachable.");
                }
                footerStatusLabel.setText("Ready");
            });
        });
    }

    @FXML
    private void handleFormSave() {
        // Validate required fields
        if (formDeviceName.getText().trim().isEmpty()) {
            showError("Validation Error", "Device name is required");
            return;
        }
        if (formIpAddress.getText().trim().isEmpty()) {
            showError("Validation Error", "IP address is required");
            return;
        }
        if (formDeviceType.getValue() == null) {
            showError("Validation Error", "Device type is required");
            return;
        }

        // Check for duplicate IP
        String ip = formIpAddress.getText().trim();
        if (editingDevice == null && networkService.isIpAddressRegistered(ip)) {
            showError("Validation Error", "A device with this IP address already exists");
            return;
        }

        try {
            NetworkDevice device = editingDevice != null ? editingDevice : new NetworkDevice();
            device.setDeviceName(formDeviceName.getText().trim());
            device.setIpAddress(ip);
            device.setMacAddress(formMacAddress.getText().trim().isEmpty() ? null : formMacAddress.getText().trim());
            device.setDeviceType(formDeviceType.getValue());
            device.setLocation(formLocation.getText().trim().isEmpty() ? null : formLocation.getText().trim());
            device.setDescription(formDescription.getText().trim().isEmpty() ? null : formDescription.getText().trim());
            device.setManufacturer(formManufacturer.getText().trim().isEmpty() ? null : formManufacturer.getText().trim());
            device.setModel(formModel.getText().trim().isEmpty() ? null : formModel.getText().trim());
            device.setSerialNumber(formSerialNumber.getText().trim().isEmpty() ? null : formSerialNumber.getText().trim());
            device.setHostname(formHostname.getText().trim().isEmpty() ? null : formHostname.getText().trim());
            device.setMonitoringEnabled(formMonitoringEnabled.isSelected());
            device.setPingIntervalSeconds(formPingInterval.getValue());
            device.setPingTimeoutMs(formPingTimeout.getValue());
            device.setPort(formPort.getValue());
            device.setAlertOnOffline(formAlertOnOffline.isSelected());
            device.setAlertEmail(formAlertEmail.getText().trim().isEmpty() ? null : formAlertEmail.getText().trim());
            device.setNotes(formNotes.getText().trim().isEmpty() ? null : formNotes.getText().trim());

            networkService.saveDevice(device);

            appendOutput((editingDevice != null ? "Updated" : "Added") + " device: " + device.getDeviceName());
            handleFormCancel();
            loadData();
        } catch (Exception e) {
            showError("Error saving device", e.getMessage());
        }
    }

    private void clearForm() {
        editingDevice = null;
        formDeviceName.clear();
        formIpAddress.clear();
        formMacAddress.clear();
        formDeviceType.setValue(null);
        formLocation.clear();
        formDescription.clear();
        formManufacturer.clear();
        formModel.clear();
        formSerialNumber.clear();
        formHostname.clear();
        formMonitoringEnabled.setSelected(true);
        formPingInterval.getValueFactory().setValue(60);
        formPingTimeout.getValueFactory().setValue(5000);
        formPort.getValueFactory().setValue(0);
        formAlertOnOffline.setSelected(true);
        formAlertEmail.clear();
        formNotes.clear();
    }

    private void populateForm(NetworkDevice device) {
        formDeviceName.setText(device.getDeviceName());
        formIpAddress.setText(device.getIpAddress());
        formMacAddress.setText(device.getMacAddress() != null ? device.getMacAddress() : "");
        formDeviceType.setValue(device.getDeviceType());
        formLocation.setText(device.getLocation() != null ? device.getLocation() : "");
        formDescription.setText(device.getDescription() != null ? device.getDescription() : "");
        formManufacturer.setText(device.getManufacturer() != null ? device.getManufacturer() : "");
        formModel.setText(device.getModel() != null ? device.getModel() : "");
        formSerialNumber.setText(device.getSerialNumber() != null ? device.getSerialNumber() : "");
        formHostname.setText(device.getHostname() != null ? device.getHostname() : "");
        formMonitoringEnabled.setSelected(device.getMonitoringEnabled());
        formPingInterval.getValueFactory().setValue(device.getPingIntervalSeconds());
        formPingTimeout.getValueFactory().setValue(device.getPingTimeoutMs());
        formPort.getValueFactory().setValue(device.getPort());
        formAlertOnOffline.setSelected(device.getAlertOnOffline());
        formAlertEmail.setText(device.getAlertEmail() != null ? device.getAlertEmail() : "");
        formNotes.setText(device.getNotes() != null ? device.getNotes() : "");
    }

    // ==================== Utility Methods ====================

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
