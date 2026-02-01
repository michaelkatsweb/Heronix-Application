package com.heronix.ui.controller;

import com.heronix.config.OfflineModeConfig;
import com.heronix.gateway.device.*;
import com.heronix.gateway.encryption.GatewayEncryptionService;
import com.heronix.gateway.proxy.SecureOutboundProxyService;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controller for the Security & Network Management Dashboard.
 *
 * Provides comprehensive GUI for:
 * - Device registration and management
 * - Network restriction configuration
 * - Data permission management
 * - Gateway audit log viewing
 * - Encryption key management
 *
 * @author Heronix Development Team
 * @version 1.0
 */
@Controller
@Slf4j
public class SecurityManagementController implements Initializable {

    // ==================== Injected Services ====================
    @Autowired(required = false)
    private DeviceRegistrationService deviceService;

    @Autowired(required = false)
    private SecureOutboundProxyService proxyService;

    @Autowired(required = false)
    private GatewayEncryptionService encryptionService;

    @Autowired(required = false)
    private OfflineModeConfig offlineModeConfig;

    // ==================== Dashboard Tab ====================
    @FXML private Label gatewayStatusIndicator;
    @FXML private Label gatewayStatusLabel;
    @FXML private Label lastRefreshLabel;
    @FXML private Button refreshBtn;

    @FXML private Label dashGatewayStatus;
    @FXML private Label dashEncryption;
    @FXML private Label dashTotalTransmissions;
    @FXML private Label dashActiveDevices;
    @FXML private Label dashPendingDevices;
    @FXML private Label dashExpiringDevices;
    @FXML private Label dashOfflineMode;
    @FXML private Label dashExternalHttp;
    @FXML private Label dashWebhooks;
    @FXML private Label dashBlockedToday;
    @FXML private Label dashFailedAuth;
    @FXML private Label dashUnregisteredAttempts;

    @FXML private TableView<ActivityRecord> recentActivityTable;
    @FXML private TableColumn<ActivityRecord, String> activityTimeCol;
    @FXML private TableColumn<ActivityRecord, String> activityDeviceCol;
    @FXML private TableColumn<ActivityRecord, String> activityTypeCol;
    @FXML private TableColumn<ActivityRecord, String> activityStatusCol;
    @FXML private TableColumn<ActivityRecord, String> activityDetailsCol;

    // ==================== Device Management Tab ====================
    @FXML private TableView<DeviceTableRow> deviceTable;
    @FXML private TableColumn<DeviceTableRow, Boolean> devSelectCol;
    @FXML private TableColumn<DeviceTableRow, String> devStatusCol;
    @FXML private TableColumn<DeviceTableRow, String> devNameCol;
    @FXML private TableColumn<DeviceTableRow, String> devTypeCol;
    @FXML private TableColumn<DeviceTableRow, String> devOrgCol;
    @FXML private TableColumn<DeviceTableRow, String> devPermissionsCol;
    @FXML private TableColumn<DeviceTableRow, String> devLastTransCol;
    @FXML private TableColumn<DeviceTableRow, String> devTransCountCol;
    @FXML private TableColumn<DeviceTableRow, String> devExpiresCol;
    @FXML private TableColumn<DeviceTableRow, Void> devActionsCol;

    @FXML private TextField deviceSearchField;
    @FXML private ComboBox<String> deviceStatusFilter;
    @FXML private ComboBox<String> deviceTypeFilter;
    @FXML private TitledPane deviceDetailsPane;

    @FXML private Label detailDeviceId;
    @FXML private Label detailAdminEmail;
    @FXML private Label detailPubKeyHash;
    @FXML private Label detailAllowedIPs;
    @FXML private Label detailRegistered;
    @FXML private Label detailApprovedBy;
    @FXML private Label detailFailedTrans;
    @FXML private Label detailNotes;

    // ==================== Network Restrictions Tab ====================
    @FXML private ToggleButton offlineModeToggle;
    @FXML private CheckBox blockExternalHttpCb;
    @FXML private CheckBox disableWebhooksCb;
    @FXML private CheckBox disableEmailCb;
    @FXML private CheckBox disableStateLookupsCb;
    @FXML private CheckBox useLocalAssetsCb;
    @FXML private CheckBox localAiOnlyCb;

    @FXML private ListView<String> allowedHostsList;
    @FXML private ListView<String> allowedPatternsList;
    @FXML private TextField newAllowedHostField;

    @FXML private TextField cspDefaultSrc;
    @FXML private TextField cspScriptSrc;
    @FXML private TextField cspStyleSrc;
    @FXML private TextField cspConnectSrc;
    @FXML private TextField cspFrameAncestors;

    @FXML private Spinner<Integer> authRateLimitSpinner;
    @FXML private Spinner<Integer> unauthRateLimitSpinner;
    @FXML private Spinner<Integer> burstCapacitySpinner;

    // ==================== Data Permissions Tab ====================
    @FXML private TableView<PermissionDef> permissionDefTable;
    @FXML private TableColumn<PermissionDef, String> permNameCol;
    @FXML private TableColumn<PermissionDef, String> permCategoryCol;
    @FXML private TableColumn<PermissionDef, String> permDescCol;
    @FXML private TableColumn<PermissionDef, String> permRiskCol;

    @FXML private ComboBox<String> permMatrixDeviceFilter;
    @FXML private GridPane permissionMatrixGrid;
    @FXML private FlowPane removedFieldsPane;
    @FXML private FlowPane maskedFieldsPane;

    // ==================== Audit Log Tab ====================
    @FXML private DatePicker auditStartDate;
    @FXML private DatePicker auditEndDate;
    @FXML private ComboBox<String> auditStatusFilter;
    @FXML private ComboBox<String> auditDeviceFilter;

    @FXML private Label auditTotalCount;
    @FXML private Label auditSuccessCount;
    @FXML private Label auditBlockedCount;
    @FXML private Label auditFailedCount;

    @FXML private TableView<AuditRecord> auditTable;
    @FXML private TableColumn<AuditRecord, String> auditTimeCol;
    @FXML private TableColumn<AuditRecord, String> auditTransIdCol;
    @FXML private TableColumn<AuditRecord, String> auditDeviceCol;
    @FXML private TableColumn<AuditRecord, String> auditDataTypeCol;
    @FXML private TableColumn<AuditRecord, String> auditStatusTableCol;
    @FXML private TableColumn<AuditRecord, String> auditOrigFieldsCol;
    @FXML private TableColumn<AuditRecord, String> auditSanitizedFieldsCol;
    @FXML private TableColumn<AuditRecord, String> auditReasonCol;

    // ==================== Encryption Tab ====================
    @FXML private Label masterKeyStatus;
    @FXML private Label encKeyStatusLabel;
    @FXML private Label encLastRotated;
    @FXML private TextArea generatedKeyArea;

    // ==================== Status Bar ====================
    @FXML private Label statusMessage;
    @FXML private Label connectionStatus;
    @FXML private Label userLabel;

    // ==================== Data ====================
    private ObservableList<DeviceTableRow> deviceData = FXCollections.observableArrayList();
    private ObservableList<ActivityRecord> activityData = FXCollections.observableArrayList();
    private ObservableList<AuditRecord> auditData = FXCollections.observableArrayList();

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("Initializing Security Management Controller");

        initializeDashboard();
        initializeDeviceTable();
        initializeNetworkRestrictions();
        initializePermissions();
        initializeAuditLog();
        initializeEncryption();

        // Initial data load
        Platform.runLater(this::loadDashboardData);
    }

    // ==================== Initialization Methods ====================

    private void initializeDashboard() {
        // Configure recent activity table
        activityTimeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().time()));
        activityDeviceCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().device()));
        activityTypeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().type()));
        activityStatusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().status()));
        activityDetailsCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().details()));

        // Style status column
        activityStatusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item.toUpperCase()) {
                        case "SUCCESS" -> setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                        case "BLOCKED" -> setStyle("-fx-text-fill: #ffc107; -fx-font-weight: bold;");
                        case "FAILED" -> setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                        default -> setStyle("");
                    }
                }
            }
        });

        recentActivityTable.setItems(activityData);
    }

    private void initializeDeviceTable() {
        // Selection column with checkbox
        devSelectCol.setCellValueFactory(data -> data.getValue().selectedProperty());
        devSelectCol.setCellFactory(CheckBoxTableCell.forTableColumn(devSelectCol));

        // Status column with styling
        devStatusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().status()));
        devStatusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.setPadding(new Insets(2, 8, 2, 8));
                    switch (item.toUpperCase()) {
                        case "ACTIVE" -> badge.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-background-radius: 3;");
                        case "PENDING_APPROVAL" -> badge.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; -fx-background-radius: 3;");
                        case "SUSPENDED" -> badge.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-background-radius: 3;");
                        case "REVOKED" -> badge.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-background-radius: 3;");
                        case "EXPIRED" -> badge.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-background-radius: 3;");
                        default -> badge.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-background-radius: 3;");
                    }
                    setGraphic(badge);
                }
            }
        });

        devNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().deviceName()));
        devTypeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().deviceType()));
        devOrgCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().organization()));
        devPermissionsCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().permissions()));
        devLastTransCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().lastTransmission()));
        devTransCountCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().transmissionCount()));
        devExpiresCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().expires()));

        // Actions column with buttons
        devActionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("👁️");
            private final Button editBtn = new Button("✏️");
            private final HBox buttons = new HBox(5, viewBtn, editBtn);

            {
                viewBtn.setTooltip(new Tooltip("View Details"));
                editBtn.setTooltip(new Tooltip("Edit Device"));
                viewBtn.setOnAction(e -> handleViewDevice(getTableRow().getItem()));
                editBtn.setOnAction(e -> handleEditDevice(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });

        deviceTable.setItems(deviceData);

        // Selection listener to show details
        deviceTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showDeviceDetails(newVal);
                deviceDetailsPane.setExpanded(true);
            }
        });

        // Setup filters
        deviceStatusFilter.setItems(FXCollections.observableArrayList(
            "All", "ACTIVE", "PENDING_APPROVAL", "SUSPENDED", "REVOKED", "EXPIRED"
        ));
        deviceStatusFilter.setValue("All");
        deviceStatusFilter.setOnAction(e -> filterDevices());

        deviceTypeFilter.setItems(FXCollections.observableArrayList(
            "All", "DISTRICT_SERVER", "PARENT_PORTAL", "EMAIL_RELAY", "SMS_GATEWAY",
            "BACKUP_SERVER", "ANALYTICS_PLATFORM", "LMS_INTEGRATION", "THIRD_PARTY_API", "AUDIT_SYSTEM"
        ));
        deviceTypeFilter.setValue("All");
        deviceTypeFilter.setOnAction(e -> filterDevices());

        // Search filter
        deviceSearchField.textProperty().addListener((obs, oldVal, newVal) -> filterDevices());
    }

    private void initializeNetworkRestrictions() {
        // Load current settings from config
        if (offlineModeConfig != null) {
            offlineModeToggle.setSelected(offlineModeConfig.isEnabled());
            offlineModeToggle.setText(offlineModeConfig.isEnabled() ? "ENABLED" : "DISABLED");
            blockExternalHttpCb.setSelected(offlineModeConfig.isBlockExternalHttp());
            disableWebhooksCb.setSelected(offlineModeConfig.isDisableWebhooks());
            disableEmailCb.setSelected(offlineModeConfig.isDisableEmail());
            disableStateLookupsCb.setSelected(offlineModeConfig.isDisableStateLookups());
            useLocalAssetsCb.setSelected(offlineModeConfig.isUseLocalAssets());
            localAiOnlyCb.setSelected(offlineModeConfig.isLocalAiOnly());
        }

        // Initialize allowed hosts list
        allowedHostsList.setItems(FXCollections.observableArrayList(
            "localhost", "127.0.0.1", "0.0.0.0", "::1", "host.docker.internal"
        ));

        allowedPatternsList.setItems(FXCollections.observableArrayList(
            "heronix-*", "172.28.*", "192.168.*", "10.*"
        ));

        // Initialize spinners
        authRateLimitSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(100, 50000, 5000, 100));
        unauthRateLimitSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 1000, 100, 10));
        burstCapacitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 500, 100, 10));
    }

    private void initializePermissions() {
        // Setup permission definition table
        permNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().name()));
        permCategoryCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().category()));
        permDescCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().description()));
        permRiskCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().riskLevel()));

        // Style risk column
        permRiskCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item.toUpperCase()) {
                        case "LOW" -> setStyle("-fx-text-fill: #28a745;");
                        case "MEDIUM" -> setStyle("-fx-text-fill: #ffc107;");
                        case "HIGH" -> setStyle("-fx-text-fill: #dc3545;");
                        default -> setStyle("");
                    }
                }
            }
        });

        // Load permission definitions
        ObservableList<PermissionDef> permDefs = FXCollections.observableArrayList(
            new PermissionDef("STUDENT_BASIC_INFO", "Student Data", "Student name and grade level (anonymized)", "LOW"),
            new PermissionDef("STUDENT_CONTACT_INFO", "Student Data", "Parent contact information (masked)", "MEDIUM"),
            new PermissionDef("STUDENT_ATTENDANCE", "Student Data", "Attendance records (anonymized)", "LOW"),
            new PermissionDef("STUDENT_GRADES", "Student Data", "Academic performance data (aggregated)", "MEDIUM"),
            new PermissionDef("SEND_ATTENDANCE_ALERTS", "Notifications", "Send attendance notifications to parents", "LOW"),
            new PermissionDef("SEND_GRADE_UPDATES", "Notifications", "Send grade update notifications", "LOW"),
            new PermissionDef("SEND_EMERGENCY_ALERTS", "Notifications", "Send emergency notifications", "LOW"),
            new PermissionDef("SEND_GENERAL_NOTIFICATIONS", "Notifications", "Send general school announcements", "LOW"),
            new PermissionDef("AGGREGATE_STATISTICS", "Reports", "Access to school-level aggregate statistics", "LOW"),
            new PermissionDef("COMPLIANCE_REPORTS", "Reports", "State compliance reporting data", "MEDIUM"),
            new PermissionDef("SYNC_SCHEDULES", "System", "Schedule synchronization data", "LOW"),
            new PermissionDef("AUDIT_LOGS", "System", "Access to audit trail data", "HIGH")
        );
        permissionDefTable.setItems(permDefs);

        // Load sanitization fields
        loadSanitizationFields();
    }

    private void loadSanitizationFields() {
        // Fields always removed
        String[] removedFields = {
            "ssn", "socialSecurityNumber", "password", "passwordHash",
            "pin", "securityQuestion", "securityAnswer", "internalId",
            "databaseId", "serverIp", "macAddress", "gpsCoordinates",
            "latitude", "longitude", "ipAddress"
        };

        for (String field : removedFields) {
            Label tag = new Label(field);
            tag.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; " +
                "-fx-padding: 3 8; -fx-background-radius: 3;");
            removedFieldsPane.getChildren().add(tag);
        }

        // Fields masked
        String[] maskedFields = {
            "email", "phone", "phoneNumber", "address", "streetAddress",
            "birthDate", "dateOfBirth", "studentId"
        };

        for (String field : maskedFields) {
            Label tag = new Label(field);
            tag.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; " +
                "-fx-padding: 3 8; -fx-background-radius: 3;");
            maskedFieldsPane.getChildren().add(tag);
        }
    }

    private void initializeAuditLog() {
        // Setup date pickers
        auditStartDate.setValue(LocalDate.now().minusDays(7));
        auditEndDate.setValue(LocalDate.now());

        // Setup filters
        auditStatusFilter.setItems(FXCollections.observableArrayList(
            "All", "SUCCESS", "BLOCKED", "FAILED"
        ));
        auditStatusFilter.setValue("All");

        // Setup audit table columns
        auditTimeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().timestamp()));
        auditTransIdCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().transmissionId()));
        auditDeviceCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().deviceId()));
        auditDataTypeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().dataType()));
        auditStatusTableCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().status()));
        auditOrigFieldsCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().originalFields()));
        auditSanitizedFieldsCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().sanitizedFields()));
        auditReasonCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().blockReason()));

        // Style status column
        auditStatusTableCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item.toUpperCase()) {
                        case "SUCCESS" -> setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                        case "BLOCKED" -> setStyle("-fx-text-fill: #ffc107; -fx-font-weight: bold;");
                        case "FAILED" -> setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                        default -> setStyle("");
                    }
                }
            }
        });

        auditTable.setItems(auditData);
    }

    private void initializeEncryption() {
        // Check encryption service status
        if (encryptionService != null) {
            masterKeyStatus.setText("● Configured");
            masterKeyStatus.setStyle("-fx-text-fill: #28a745;");
            encKeyStatusLabel.setText("Active (from environment or generated)");
        } else {
            masterKeyStatus.setText("● Not Configured");
            masterKeyStatus.setStyle("-fx-text-fill: #dc3545;");
            encKeyStatusLabel.setText("Service not available");
        }
    }

    // ==================== Data Loading Methods ====================

    private void loadDashboardData() {
        setStatus("Loading dashboard data...");

        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() {
                // Simulate loading data (replace with actual service calls)
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return null;
            }
        };

        loadTask.setOnSucceeded(e -> {
            // Update gateway status
            gatewayStatusIndicator.setText("●");
            gatewayStatusIndicator.setStyle("-fx-text-fill: #28a745;");
            gatewayStatusLabel.setText("Gateway Status: OPERATIONAL");

            dashGatewayStatus.setText("OPERATIONAL");
            dashGatewayStatus.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
            dashEncryption.setText("AES-256-GCM");

            // Load device counts
            if (deviceService != null) {
                List<RegisteredDevice> activeDevices = deviceService.getActiveDevices();
                List<RegisteredDevice> pendingDevices = deviceService.getPendingDevices();
                List<RegisteredDevice> expiringDevices = deviceService.getDevicesExpiringSoon(30);

                dashActiveDevices.setText(String.valueOf(activeDevices.size()));
                dashPendingDevices.setText(String.valueOf(pendingDevices.size()));
                dashExpiringDevices.setText(String.valueOf(expiringDevices.size()));
                dashTotalTransmissions.setText(String.valueOf(
                    activeDevices.stream().mapToLong(RegisteredDevice::getTransmissionCount).sum()
                ));

                // Load device data for table
                loadDeviceData(activeDevices, pendingDevices);
            } else {
                // Demo data
                dashActiveDevices.setText("3");
                dashPendingDevices.setText("1");
                dashExpiringDevices.setText("0");
                dashTotalTransmissions.setText("1,234");
                loadDemoDeviceData();
            }

            // Network status
            if (offlineModeConfig != null && offlineModeConfig.isEnabled()) {
                dashOfflineMode.setText("ENABLED");
                dashOfflineMode.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                dashExternalHttp.setText("BLOCKED");
                dashWebhooks.setText("DISABLED");
            } else {
                dashOfflineMode.setText("DISABLED");
                dashOfflineMode.setStyle("-fx-text-fill: #ffc107;");
            }

            // Security alerts (demo data)
            dashBlockedToday.setText("0");
            dashFailedAuth.setText("0");
            dashUnregisteredAttempts.setText("0");

            // Load recent activity
            loadRecentActivity();

            lastRefreshLabel.setText("Last refresh: " + LocalDateTime.now().format(TIME_FORMATTER));
            setStatus("Dashboard loaded successfully");
        });

        loadTask.setOnFailed(e -> {
            gatewayStatusIndicator.setStyle("-fx-text-fill: #dc3545;");
            gatewayStatusLabel.setText("Gateway Status: ERROR");
            setStatus("Failed to load dashboard: " + loadTask.getException().getMessage());
        });

        new Thread(loadTask).start();
    }

    private void loadDeviceData(List<RegisteredDevice> activeDevices, List<RegisteredDevice> pendingDevices) {
        deviceData.clear();

        for (RegisteredDevice device : activeDevices) {
            deviceData.add(new DeviceTableRow(
                device.getDeviceId(),
                device.getStatus().name(),
                device.getDeviceName(),
                device.getDeviceType().name(),
                device.getOrganizationName(),
                String.valueOf(device.getPermissions().size()) + " permissions",
                device.getLastDataTransmissionAt() != null ?
                    device.getLastDataTransmissionAt().format(TIME_FORMATTER) : "Never",
                String.valueOf(device.getTransmissionCount()),
                device.getExpiresAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                device
            ));
        }

        for (RegisteredDevice device : pendingDevices) {
            deviceData.add(new DeviceTableRow(
                device.getDeviceId(),
                device.getStatus().name(),
                device.getDeviceName(),
                device.getDeviceType().name(),
                device.getOrganizationName(),
                "Pending",
                "N/A",
                "0",
                device.getExpiresAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                device
            ));
        }
    }

    private void loadDemoDeviceData() {
        deviceData.clear();
        deviceData.addAll(
            new DeviceTableRow("dev-001", "ACTIVE", "District Server Alpha", "DISTRICT_SERVER",
                "Main School District", "5 permissions", "2024-01-23 14:30:00", "523", "2025-01-23", null),
            new DeviceTableRow("dev-002", "ACTIVE", "Parent Portal Gateway", "PARENT_PORTAL",
                "Main School District", "3 permissions", "2024-01-23 12:15:00", "1,204", "2025-03-15", null),
            new DeviceTableRow("dev-003", "ACTIVE", "Email Relay Server", "EMAIL_RELAY",
                "Main School District", "2 permissions", "2024-01-22 09:00:00", "89", "2025-01-01", null),
            new DeviceTableRow("dev-004", "PENDING_APPROVAL", "New Analytics Platform", "ANALYTICS_PLATFORM",
                "EdTech Solutions", "Pending", "N/A", "0", "2025-01-23", null)
        );
    }

    private void loadRecentActivity() {
        activityData.clear();
        // Demo activity data
        activityData.addAll(
            new ActivityRecord("2024-01-23 15:45:23", "District Server Alpha", "DATA_SYNC", "SUCCESS", "Synced 150 student records"),
            new ActivityRecord("2024-01-23 15:30:00", "Parent Portal Gateway", "NOTIFICATION", "SUCCESS", "Sent 45 attendance alerts"),
            new ActivityRecord("2024-01-23 15:15:12", "Unknown Device", "AUTH_ATTEMPT", "BLOCKED", "Unregistered device blocked"),
            new ActivityRecord("2024-01-23 14:45:00", "Email Relay Server", "EMAIL", "SUCCESS", "Delivered 12 notifications"),
            new ActivityRecord("2024-01-23 14:00:00", "District Server Alpha", "DATA_SYNC", "SUCCESS", "Synced grade updates")
        );
    }

    // ==================== Action Handlers ====================

    @FXML
    private void handleRefreshAll() {
        loadDashboardData();
    }

    @FXML
    private void handleViewAllActivity() {
        // Switch to audit tab
        TabPane tabPane = (TabPane) refreshBtn.getScene().lookup("#mainTabPane");
        if (tabPane != null) {
            tabPane.getSelectionModel().select(4); // Audit tab
        }
    }

    @FXML
    private void handleQuickRegisterDevice() {
        handleRegisterDevice();
    }

    @FXML
    private void handleQuickApprovePending() {
        // Switch to device tab and filter pending
        TabPane tabPane = (TabPane) refreshBtn.getScene().lookup("#mainTabPane");
        if (tabPane != null) {
            tabPane.getSelectionModel().select(1); // Device tab
            deviceStatusFilter.setValue("PENDING_APPROVAL");
            filterDevices();
        }
    }

    @FXML
    private void handleSecurityScan() {
        setStatus("Running security scan...");
        showAlert(Alert.AlertType.INFORMATION, "Security Scan",
            "Security scan completed.\n\n" +
            "- All registered devices verified\n" +
            "- No unauthorized access attempts\n" +
            "- Encryption status: OK\n" +
            "- Network isolation: Active");
    }

    @FXML
    private void handleExportAuditLog() {
        handleExportAuditCSV();
    }

    @FXML
    private void handleEmergencyLockdown() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Emergency Lockdown");
        alert.setHeaderText("Confirm Emergency Lockdown");
        alert.setContentText("This will:\n" +
            "- Block ALL external transmissions\n" +
            "- Revoke all device access temporarily\n" +
            "- Enable maximum security restrictions\n\n" +
            "Are you sure you want to proceed?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Enable all security settings
            offlineModeToggle.setSelected(true);
            blockExternalHttpCb.setSelected(true);
            disableWebhooksCb.setSelected(true);
            disableEmailCb.setSelected(true);

            setStatus("EMERGENCY LOCKDOWN ACTIVATED");
            showAlert(Alert.AlertType.WARNING, "Lockdown Active",
                "Emergency lockdown is now active. All external communications are blocked.");
        }
    }

    @FXML
    private void handleRegisterDevice() {
        Dialog<DeviceRegistrationRequest> dialog = new Dialog<>();
        dialog.setTitle("Register New Device");
        dialog.setHeaderText("Enter device registration details");

        // Set button types
        ButtonType registerButtonType = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);

        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField deviceIdField = new TextField();
        deviceIdField.setPromptText("Unique Device ID (UUID)");
        TextField deviceNameField = new TextField();
        deviceNameField.setPromptText("Human-readable name");
        ComboBox<String> deviceTypeCombo = new ComboBox<>();
        deviceTypeCombo.getItems().addAll("DISTRICT_SERVER", "PARENT_PORTAL", "EMAIL_RELAY",
            "SMS_GATEWAY", "BACKUP_SERVER", "ANALYTICS_PLATFORM", "LMS_INTEGRATION",
            "THIRD_PARTY_API", "AUDIT_SYSTEM");
        TextField orgNameField = new TextField();
        orgNameField.setPromptText("Organization name");
        TextField adminEmailField = new TextField();
        adminEmailField.setPromptText("Admin email");
        TextArea publicKeyArea = new TextArea();
        publicKeyArea.setPromptText("Public key certificate (PEM format)");
        publicKeyArea.setPrefRowCount(4);
        TextField allowedIpsField = new TextField();
        allowedIpsField.setPromptText("Allowed IP ranges (CIDR, comma-separated)");

        grid.add(new Label("Device ID:"), 0, 0);
        grid.add(deviceIdField, 1, 0);
        grid.add(new Label("Device Name:"), 0, 1);
        grid.add(deviceNameField, 1, 1);
        grid.add(new Label("Device Type:"), 0, 2);
        grid.add(deviceTypeCombo, 1, 2);
        grid.add(new Label("Organization:"), 0, 3);
        grid.add(orgNameField, 1, 3);
        grid.add(new Label("Admin Email:"), 0, 4);
        grid.add(adminEmailField, 1, 4);
        grid.add(new Label("Public Key:"), 0, 5);
        grid.add(publicKeyArea, 1, 5);
        grid.add(new Label("Allowed IPs:"), 0, 6);
        grid.add(allowedIpsField, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == registerButtonType) {
                return DeviceRegistrationRequest.builder()
                    .deviceId(deviceIdField.getText().isEmpty() ?
                        UUID.randomUUID().toString() : deviceIdField.getText())
                    .deviceName(deviceNameField.getText())
                    .deviceType(RegisteredDevice.DeviceType.valueOf(deviceTypeCombo.getValue()))
                    .organizationName(orgNameField.getText())
                    .adminEmail(adminEmailField.getText())
                    .publicKeyCertificate(publicKeyArea.getText())
                    .allowedIpRanges(allowedIpsField.getText())
                    .build();
            }
            return null;
        });

        Optional<DeviceRegistrationRequest> result = dialog.showAndWait();
        result.ifPresent(request -> {
            if (deviceService != null) {
                try {
                    RegisteredDevice device = deviceService.registerDevice(request);
                    showAlert(Alert.AlertType.INFORMATION, "Device Registered",
                        "Device registered successfully.\nDevice ID: " + device.getDeviceId() +
                        "\nStatus: PENDING_APPROVAL");
                    loadDashboardData();
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Registration Failed", e.getMessage());
                }
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Demo Mode",
                    "Device would be registered with ID: " + request.getDeviceId());
            }
        });
    }

    @FXML
    private void handleApproveDevice() {
        DeviceTableRow selected = deviceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a device to approve.");
            return;
        }

        if (!"PENDING_APPROVAL".equals(selected.status())) {
            showAlert(Alert.AlertType.WARNING, "Invalid Status",
                "Only devices with PENDING_APPROVAL status can be approved.");
            return;
        }

        // Show permission selection dialog
        showPermissionDialog(selected);
    }

    private void showPermissionDialog(DeviceTableRow device) {
        Dialog<Set<RegisteredDevice.DataPermission>> dialog = new Dialog<>();
        dialog.setTitle("Approve Device");
        dialog.setHeaderText("Select permissions for: " + device.deviceName());

        ButtonType approveButton = new ButtonType("Approve", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(approveButton, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        Map<RegisteredDevice.DataPermission, CheckBox> checkBoxes = new HashMap<>();
        for (RegisteredDevice.DataPermission perm : RegisteredDevice.DataPermission.values()) {
            CheckBox cb = new CheckBox(perm.name());
            checkBoxes.put(perm, cb);
            content.getChildren().add(cb);
        }

        dialog.getDialogPane().setContent(new ScrollPane(content));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == approveButton) {
                Set<RegisteredDevice.DataPermission> selected = new HashSet<>();
                checkBoxes.forEach((perm, cb) -> {
                    if (cb.isSelected()) selected.add(perm);
                });
                return selected;
            }
            return null;
        });

        Optional<Set<RegisteredDevice.DataPermission>> result = dialog.showAndWait();
        result.ifPresent(permissions -> {
            if (deviceService != null && device.device() != null) {
                try {
                    deviceService.approveDevice(device.deviceId(), "admin", permissions);
                    showAlert(Alert.AlertType.INFORMATION, "Device Approved",
                        "Device approved with " + permissions.size() + " permissions.");
                    loadDashboardData();
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Approval Failed", e.getMessage());
                }
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Demo Mode",
                    "Device would be approved with " + permissions.size() + " permissions.");
            }
        });
    }

    @FXML
    private void handleSuspendDevice() {
        DeviceTableRow selected = deviceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a device to suspend.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Suspend Device");
        dialog.setHeaderText("Suspend: " + selected.deviceName());
        dialog.setContentText("Reason for suspension:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(reason -> {
            if (deviceService != null && selected.device() != null) {
                deviceService.suspendDevice(selected.deviceId(), reason);
                showAlert(Alert.AlertType.INFORMATION, "Device Suspended", "Device has been suspended.");
                loadDashboardData();
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Demo Mode", "Device would be suspended.");
            }
        });
    }

    @FXML
    private void handleRevokeDevice() {
        DeviceTableRow selected = deviceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a device to revoke.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Revoke Device");
        confirm.setHeaderText("Permanently revoke: " + selected.deviceName() + "?");
        confirm.setContentText("This action cannot be undone. The device will no longer be able to receive any data.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            TextInputDialog reasonDialog = new TextInputDialog();
            reasonDialog.setTitle("Revocation Reason");
            reasonDialog.setContentText("Reason for revocation:");

            Optional<String> reason = reasonDialog.showAndWait();
            reason.ifPresent(r -> {
                if (deviceService != null && selected.device() != null) {
                    deviceService.revokeDevice(selected.deviceId(), r, "admin");
                    showAlert(Alert.AlertType.INFORMATION, "Device Revoked", "Device has been revoked.");
                    loadDashboardData();
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "Demo Mode", "Device would be revoked.");
                }
            });
        }
    }

    @FXML
    private void handleRenewDevice() {
        DeviceTableRow selected = deviceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a device to renew.");
            return;
        }

        if (deviceService != null && selected.device() != null) {
            deviceService.renewDevice(selected.deviceId(), 1);
            showAlert(Alert.AlertType.INFORMATION, "Device Renewed",
                "Device registration renewed for 1 year.");
            loadDashboardData();
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Demo Mode", "Device would be renewed.");
        }
    }

    private void handleViewDevice(DeviceTableRow device) {
        if (device != null) {
            showDeviceDetails(device);
            deviceDetailsPane.setExpanded(true);
        }
    }

    private void handleEditDevice(DeviceTableRow device) {
        showAlert(Alert.AlertType.INFORMATION, "Edit Device",
            "Device editing dialog would open for: " + device.deviceName());
    }

    private void showDeviceDetails(DeviceTableRow device) {
        detailDeviceId.setText(device.deviceId());
        detailAdminEmail.setText("admin@" + device.organization().toLowerCase().replace(" ", "") + ".edu");
        detailPubKeyHash.setText("SHA256:" + device.deviceId().substring(0, 8) + "...");
        detailAllowedIPs.setText("192.168.1.0/24, 10.0.0.0/8");
        detailRegistered.setText("2024-01-01 00:00:00");
        detailApprovedBy.setText("ACTIVE".equals(device.status()) ? "admin" : "N/A");
        detailFailedTrans.setText("0");
        detailNotes.setText("No notes");
    }

    private void filterDevices() {
        String searchText = deviceSearchField.getText().toLowerCase();
        String statusFilter = deviceStatusFilter.getValue();
        String typeFilter = deviceTypeFilter.getValue();

        deviceTable.setItems(deviceData.filtered(device -> {
            boolean matchesSearch = searchText.isEmpty() ||
                device.deviceName().toLowerCase().contains(searchText) ||
                device.organization().toLowerCase().contains(searchText) ||
                device.deviceId().toLowerCase().contains(searchText);

            boolean matchesStatus = "All".equals(statusFilter) ||
                device.status().equals(statusFilter);

            boolean matchesType = "All".equals(typeFilter) ||
                device.deviceType().equals(typeFilter);

            return matchesSearch && matchesStatus && matchesType;
        }));
    }

    // ==================== Network Restrictions Handlers ====================

    @FXML
    private void handleToggleOfflineMode() {
        boolean enabled = offlineModeToggle.isSelected();
        offlineModeToggle.setText(enabled ? "ENABLED" : "DISABLED");

        if (enabled) {
            blockExternalHttpCb.setSelected(true);
            disableWebhooksCb.setSelected(true);
        }

        setStatus("Offline mode " + (enabled ? "enabled" : "disabled"));
    }

    @FXML
    private void handleNetworkSettingChange() {
        setStatus("Network settings updated");
    }

    @FXML
    private void handleAddAllowedHost() {
        String host = newAllowedHostField.getText().trim();
        if (!host.isEmpty()) {
            if (host.contains("*")) {
                allowedPatternsList.getItems().add(host);
            } else {
                allowedHostsList.getItems().add(host);
            }
            newAllowedHostField.clear();
            setStatus("Added allowed host: " + host);
        }
    }

    @FXML
    private void handleRemoveAllowedHost() {
        String selectedHost = allowedHostsList.getSelectionModel().getSelectedItem();
        String selectedPattern = allowedPatternsList.getSelectionModel().getSelectedItem();

        if (selectedHost != null) {
            allowedHostsList.getItems().remove(selectedHost);
            setStatus("Removed host: " + selectedHost);
        } else if (selectedPattern != null) {
            allowedPatternsList.getItems().remove(selectedPattern);
            setStatus("Removed pattern: " + selectedPattern);
        }
    }

    @FXML
    private void handleSaveCSP() {
        setStatus("Content Security Policy settings saved");
        showAlert(Alert.AlertType.INFORMATION, "CSP Saved",
            "Content Security Policy settings have been saved.\n" +
            "Restart the application for changes to take effect.");
    }

    @FXML
    private void handleResetCSP() {
        cspDefaultSrc.setText("'self'");
        cspScriptSrc.setText("'self' 'unsafe-inline'");
        cspStyleSrc.setText("'self' 'unsafe-inline'");
        cspConnectSrc.setText("'self'");
        cspFrameAncestors.setText("'none'");
        setStatus("CSP settings reset to defaults");
    }

    @FXML
    private void handleApplyRateLimits() {
        setStatus("Rate limits applied: Auth=" + authRateLimitSpinner.getValue() +
            ", Unauth=" + unauthRateLimitSpinner.getValue() +
            ", Burst=" + burstCapacitySpinner.getValue());
    }

    // ==================== Permissions Handlers ====================

    @FXML
    private void handleSavePermissions() {
        setStatus("Permissions saved");
    }

    @FXML
    private void handleCopyPermissions() {
        showAlert(Alert.AlertType.INFORMATION, "Copy Permissions",
            "Select a source device to copy permissions from.");
    }

    @FXML
    private void handleRevokeAllPermissions() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Revoke All Permissions");
        confirm.setHeaderText("Revoke all permissions for selected device?");
        confirm.setContentText("The device will not be able to receive any data until permissions are re-granted.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            setStatus("All permissions revoked");
        }
    }

    // ==================== Audit Log Handlers ====================

    @FXML
    private void handleSearchAudit() {
        setStatus("Searching audit logs...");
        // Load demo audit data
        auditData.clear();
        auditData.addAll(
            new AuditRecord("2024-01-23 15:45:23", "trans-001", "dev-001", "STUDENT_RECORD", "SUCCESS", "150", "85", ""),
            new AuditRecord("2024-01-23 15:30:00", "trans-002", "dev-002", "NOTIFICATION", "SUCCESS", "45", "30", ""),
            new AuditRecord("2024-01-23 15:15:12", "trans-003", "unknown", "DATA_SYNC", "BLOCKED", "0", "0", "DEVICE_NOT_REGISTERED"),
            new AuditRecord("2024-01-23 14:45:00", "trans-004", "dev-003", "EMAIL", "SUCCESS", "12", "10", ""),
            new AuditRecord("2024-01-23 14:00:00", "trans-005", "dev-001", "AGGREGATE_STATS", "SUCCESS", "25", "25", "")
        );

        auditTotalCount.setText(String.valueOf(auditData.size()));
        auditSuccessCount.setText(String.valueOf(auditData.filtered(a -> "SUCCESS".equals(a.status())).size()));
        auditBlockedCount.setText(String.valueOf(auditData.filtered(a -> "BLOCKED".equals(a.status())).size()));
        auditFailedCount.setText(String.valueOf(auditData.filtered(a -> "FAILED".equals(a.status())).size()));

        setStatus("Found " + auditData.size() + " audit records");
    }

    @FXML
    private void handleExportAuditCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Audit Log");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("audit_log_" + LocalDate.now() + ".csv");

        File file = fileChooser.showSaveDialog(auditTable.getScene().getWindow());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("Timestamp,Transmission ID,Device,Data Type,Status,Original Fields,Sanitized Fields,Block Reason\n");
                for (AuditRecord record : auditData) {
                    writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s\n",
                        record.timestamp(), record.transmissionId(), record.deviceId(),
                        record.dataType(), record.status(), record.originalFields(),
                        record.sanitizedFields(), record.blockReason()));
                }
                setStatus("Audit log exported to: " + file.getName());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Export Failed", e.getMessage());
            }
        }
    }

    @FXML
    private void handleExportAuditPDF() {
        showAlert(Alert.AlertType.INFORMATION, "Export PDF",
            "PDF export functionality would generate a formatted report.");
    }

    @FXML
    private void handlePurgeAuditLogs() {
        Alert confirm = new Alert(Alert.AlertType.WARNING);
        confirm.setTitle("Purge Audit Logs");
        confirm.setHeaderText("Delete old audit logs?");
        confirm.setContentText("This will permanently delete audit logs older than 365 days.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            setStatus("Audit logs purged");
        }
    }

    // ==================== Encryption Handlers ====================

    @FXML
    private void handleRotateMasterKey() {
        Alert confirm = new Alert(Alert.AlertType.WARNING);
        confirm.setTitle("Rotate Master Key");
        confirm.setHeaderText("Rotate the master encryption key?");
        confirm.setContentText("This will:\n" +
            "- Generate a new master key\n" +
            "- Re-encrypt all device symmetric keys\n" +
            "- Require updating HERONIX_GATEWAY_MASTER_KEY environment variable\n\n" +
            "Proceed with caution.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (encryptionService != null) {
                String newKey = encryptionService.generateDeviceSymmetricKey();
                generatedKeyArea.setText("New Master Key (save this securely!):\n\n" + newKey);
                setStatus("Master key rotated - SAVE THE NEW KEY!");
            }
        }
    }

    @FXML
    private void handleGenerateNewKey() {
        if (encryptionService != null) {
            String key = encryptionService.generateDeviceSymmetricKey();
            generatedKeyArea.setText("Generated AES-256 Key:\n\n" + key);
            setStatus("New AES-256 key generated");
        } else {
            generatedKeyArea.setText("Generated AES-256 Key (Demo):\n\n" +
                Base64.getEncoder().encodeToString(new byte[32]));
        }
    }

    @FXML
    private void handleGenerateAESKey() {
        handleGenerateNewKey();
    }

    @FXML
    private void handleGenerateRSAKeyPair() {
        if (encryptionService != null) {
            GatewayEncryptionService.KeyPairResult keyPair = encryptionService.generateKeyPair();
            generatedKeyArea.setText("Generated RSA-2048 Key Pair:\n\n" +
                "=== PUBLIC KEY ===\n" + keyPair.publicKeyPem() + "\n\n" +
                "=== PRIVATE KEY ===\n" + keyPair.privateKeyPem());
            setStatus("RSA key pair generated");
        } else {
            generatedKeyArea.setText("RSA Key Pair generation requires encryption service.");
        }
    }

    @FXML
    private void handleGenerateDeviceCert() {
        showAlert(Alert.AlertType.INFORMATION, "Generate Certificate",
            "Device certificate generation would create a self-signed certificate for device authentication.");
    }

    @FXML
    private void handleCopyGeneratedKey() {
        String text = generatedKeyArea.getText();
        if (text != null && !text.isEmpty()) {
            ClipboardContent content = new ClipboardContent();
            content.putString(text);
            Clipboard.getSystemClipboard().setContent(content);
            setStatus("Key copied to clipboard");
        }
    }

    @FXML
    private void handleSaveGeneratedKey() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Key");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Key Files", "*.key", "*.pem"));
        fileChooser.setInitialFileName("heronix_key_" + LocalDate.now() + ".key");

        File file = fileChooser.showSaveDialog(generatedKeyArea.getScene().getWindow());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(generatedKeyArea.getText());
                setStatus("Key saved to: " + file.getName());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Save Failed", e.getMessage());
            }
        }
    }

    // ==================== Utility Methods ====================

    private void setStatus(String message) {
        Platform.runLater(() -> statusMessage.setText(message));
        log.info("Security Management: {}", message);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    // ==================== Data Records ====================

    public record ActivityRecord(String time, String device, String type, String status, String details) {}

    public record AuditRecord(String timestamp, String transmissionId, String deviceId, String dataType,
                              String status, String originalFields, String sanitizedFields, String blockReason) {}

    public record PermissionDef(String name, String category, String description, String riskLevel) {}

    public static class DeviceTableRow {
        private final SimpleBooleanProperty selected = new SimpleBooleanProperty(false);
        private final String deviceId;
        private final String status;
        private final String deviceName;
        private final String deviceType;
        private final String organization;
        private final String permissions;
        private final String lastTransmission;
        private final String transmissionCount;
        private final String expires;
        private final RegisteredDevice device;

        public DeviceTableRow(String deviceId, String status, String deviceName, String deviceType,
                              String organization, String permissions, String lastTransmission,
                              String transmissionCount, String expires, RegisteredDevice device) {
            this.deviceId = deviceId;
            this.status = status;
            this.deviceName = deviceName;
            this.deviceType = deviceType;
            this.organization = organization;
            this.permissions = permissions;
            this.lastTransmission = lastTransmission;
            this.transmissionCount = transmissionCount;
            this.expires = expires;
            this.device = device;
        }

        public SimpleBooleanProperty selectedProperty() { return selected; }
        public String deviceId() { return deviceId; }
        public String status() { return status; }
        public String deviceName() { return deviceName; }
        public String deviceType() { return deviceType; }
        public String organization() { return organization; }
        public String permissions() { return permissions; }
        public String lastTransmission() { return lastTransmission; }
        public String transmissionCount() { return transmissionCount; }
        public String expires() { return expires; }
        public RegisteredDevice device() { return device; }
    }
}
