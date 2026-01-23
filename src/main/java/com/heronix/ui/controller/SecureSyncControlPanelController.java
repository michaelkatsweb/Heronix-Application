package com.heronix.ui.controller;

import com.heronix.model.domain.CertificateRevocationEntry;
import com.heronix.model.domain.RegisteredDevice;
import com.heronix.model.domain.StudentToken;
import com.heronix.model.enums.Role;
import com.heronix.security.SecurityContext;
import com.heronix.service.DeviceAuthenticationService;
import com.heronix.service.DeviceAuthenticationService.*;
import com.heronix.service.SecureBurstSyncService;
import com.heronix.service.SecureBurstSyncService.*;
import com.heronix.service.StudentTokenizationService;
import com.heronix.service.StudentTokenizationService.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

/**
 * Secure Sync Control Panel Controller
 *
 * Main control panel for managing the airgapped SIS security infrastructure:
 * - Student tokenization management
 * - Device registration and authentication
 * - Burst sync monitoring and operations
 * - Certificate revocation list management
 *
 * SECURITY: This panel is restricted to SUPER_ADMIN and ADMIN roles only.
 * All operations are logged for audit compliance.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since January 20, 2026
 */
@Slf4j
@Component
public class SecureSyncControlPanelController {

    // ========================================================================
    // FXML FIELDS - Header & Navigation
    // ========================================================================

    @FXML private Label panelTitleLabel;
    @FXML private Label securityStatusLabel;
    @FXML private Circle securityStatusIndicator;
    @FXML private Label lastRefreshLabel;
    @FXML private Button refreshAllButton;

    @FXML private TabPane mainTabPane;

    // ========================================================================
    // FXML FIELDS - Dashboard Tab
    // ========================================================================

    @FXML private Label totalTokensLabel;
    @FXML private Label activeTokensLabel;
    @FXML private Label expiredTokensLabel;
    @FXML private Label pendingDevicesLabel;
    @FXML private Label activeDevicesLabel;
    @FXML private Label revokedCertsLabel;
    @FXML private Label burstQueueLabel;
    @FXML private Label lastSyncLabel;
    @FXML private Label totalSyncedLabel;

    @FXML private PieChart tokenStatusChart;
    @FXML private LineChart<String, Number> syncActivityChart;
    @FXML private ListView<String> recentActivityList;
    @FXML private ListView<String> securityAlertsList;

    // ========================================================================
    // FXML FIELDS - Tokenization Tab
    // ========================================================================

    @FXML private TextField tokenSearchField;
    @FXML private ComboBox<String> tokenStatusFilter;
    @FXML private ComboBox<String> schoolYearFilter;

    @FXML private TableView<TokenTableRow> tokenTable;
    @FXML private TableColumn<TokenTableRow, String> tokenValueColumn;
    @FXML private TableColumn<TokenTableRow, String> studentIdColumn;
    @FXML private TableColumn<TokenTableRow, String> tokenSchoolYearColumn;
    @FXML private TableColumn<TokenTableRow, String> tokenCreatedColumn;
    @FXML private TableColumn<TokenTableRow, String> tokenExpiresColumn;
    @FXML private TableColumn<TokenTableRow, String> tokenStatusColumn;
    @FXML private TableColumn<TokenTableRow, Void> tokenActionsColumn;

    @FXML private Button generateAllTokensButton;
    @FXML private Button annualRotationButton;
    @FXML private Button exportTokensButton;
    @FXML private Label tokenCountLabel;

    // ========================================================================
    // FXML FIELDS - Device Management Tab
    // ========================================================================

    @FXML private TableView<RegisteredDevice> pendingDeviceTable;
    @FXML private TableColumn<RegisteredDevice, String> pendingDeviceIdColumn;
    @FXML private TableColumn<RegisteredDevice, String> pendingAccountColumn;
    @FXML private TableColumn<RegisteredDevice, String> pendingMacColumn;
    @FXML private TableColumn<RegisteredDevice, String> pendingDeviceNameColumn;
    @FXML private TableColumn<RegisteredDevice, String> pendingRequestedColumn;
    @FXML private TableColumn<RegisteredDevice, Void> pendingActionsColumn;

    @FXML private TableView<RegisteredDevice> activeDeviceTable;
    @FXML private TableColumn<RegisteredDevice, String> activeDeviceIdColumn;
    @FXML private TableColumn<RegisteredDevice, String> activeAccountColumn;
    @FXML private TableColumn<RegisteredDevice, String> activeMacColumn;
    @FXML private TableColumn<RegisteredDevice, String> activeCertSerialColumn;
    @FXML private TableColumn<RegisteredDevice, String> activeCertExpiresColumn;
    @FXML private TableColumn<RegisteredDevice, String> activeLastSeenColumn;
    @FXML private TableColumn<RegisteredDevice, Void> activeActionsColumn;

    @FXML private Label pendingDeviceCountLabel;
    @FXML private Label activeDeviceCountLabel;
    @FXML private Button approveAllButton;
    @FXML private Button exportCRLButton;

    // ========================================================================
    // FXML FIELDS - Sync Operations Tab
    // ========================================================================

    @FXML private Label burstQueueSizeLabel;
    @FXML private Label oldestEntryLabel;
    @FXML private ProgressBar syncProgressBar;
    @FXML private Label syncStatusLabel;

    @FXML private Button processBurstButton;
    @FXML private Button generateEnrollmentButton;
    @FXML private Button generateCRLSyncButton;

    @FXML private TableView<SyncBatchRecord> syncHistoryTable;
    @FXML private TableColumn<SyncBatchRecord, String> syncPackageIdColumn;
    @FXML private TableColumn<SyncBatchRecord, String> syncTypeColumn;
    @FXML private TableColumn<SyncBatchRecord, Integer> syncEntryCountColumn;
    @FXML private TableColumn<SyncBatchRecord, String> syncChecksumColumn;
    @FXML private TableColumn<SyncBatchRecord, String> syncCreatedColumn;

    @FXML private TextArea syncLogArea;

    // ========================================================================
    // FXML FIELDS - CRL Management Tab
    // ========================================================================

    @FXML private TableView<CRLEntry> crlTable;
    @FXML private TableColumn<CRLEntry, String> crlSerialColumn;
    @FXML private TableColumn<CRLEntry, String> crlRevokedColumn;
    @FXML private TableColumn<CRLEntry, String> crlReasonColumn;

    @FXML private Label crlTotalLabel;
    @FXML private Label crlLastGeneratedLabel;
    @FXML private Label crlChecksumLabel;
    @FXML private Button exportCRLFileButton;
    @FXML private Button regenerateCRLButton;

    // ========================================================================
    // DEPENDENCIES
    // ========================================================================

    @Autowired
    private StudentTokenizationService tokenizationService;

    @Autowired
    private DeviceAuthenticationService deviceAuthService;

    @Autowired
    private SecureBurstSyncService burstSyncService;

    // ========================================================================
    // STATE VARIABLES
    // ========================================================================

    private ObservableList<TokenTableRow> tokenList = FXCollections.observableArrayList();
    private ObservableList<RegisteredDevice> pendingDevices = FXCollections.observableArrayList();
    private ObservableList<RegisteredDevice> activeDevices = FXCollections.observableArrayList();
    private ObservableList<SyncBatchRecord> syncHistory = FXCollections.observableArrayList();
    private ObservableList<CRLEntry> crlEntries = FXCollections.observableArrayList();
    private ObservableList<String> recentActivity = FXCollections.observableArrayList();
    private ObservableList<String> securityAlerts = FXCollections.observableArrayList();

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    private DateTimeFormatter shortDateFormatter = DateTimeFormatter.ofPattern("MM/dd HH:mm");
    private Timer autoRefreshTimer;

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    @FXML
    public void initialize() {
        log.info("Initializing Secure Sync Control Panel");

        // Check permissions - SUPER_ADMIN or ADMIN only
        if (!SecurityContext.hasAnyRole(Role.SUPER_ADMIN, Role.ADMIN)) {
            showError("Access Denied",
                    "You don't have permission to access the Secure Sync Control Panel.\n" +
                    "This panel requires SUPER_ADMIN or ADMIN role.");
            disableAllControls();
            return;
        }

        // Log access for audit
        log.warn("AUDIT: Secure Sync Control Panel accessed by user: {}",
                SecurityContext.getCurrentUsername().orElse("admin"));

        // Initialize UI components
        initializeDashboard();
        initializeTokenizationTab();
        initializeDeviceManagementTab();
        initializeSyncOperationsTab();
        initializeCRLTab();

        // Load initial data
        refreshAllData();

        // Setup auto-refresh (every 30 seconds)
        setupAutoRefresh();

        // Update security status
        updateSecurityStatus();
    }

    /**
     * Initialize dashboard components
     */
    private void initializeDashboard() {
        // Setup pie chart
        if (tokenStatusChart != null) {
            tokenStatusChart.setTitle("Token Status Distribution");
            tokenStatusChart.setLabelsVisible(true);
        }

        // Setup line chart
        if (syncActivityChart != null) {
            syncActivityChart.setTitle("Sync Activity (Last 24 Hours)");
            syncActivityChart.setCreateSymbols(false);
        }

        // Setup activity lists
        if (recentActivityList != null) {
            recentActivityList.setItems(recentActivity);
            recentActivityList.setCellFactory(lv -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        setStyle("-fx-font-size: 11px;");
                    }
                }
            });
        }

        if (securityAlertsList != null) {
            securityAlertsList.setItems(securityAlerts);
            securityAlertsList.setCellFactory(lv -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        if (item.contains("CRITICAL") || item.contains("REVOKED")) {
                            setStyle("-fx-font-size: 11px; -fx-text-fill: #d32f2f; -fx-font-weight: bold;");
                        } else if (item.contains("WARNING")) {
                            setStyle("-fx-font-size: 11px; -fx-text-fill: #f57c00;");
                        } else {
                            setStyle("-fx-font-size: 11px;");
                        }
                    }
                }
            });
        }
    }

    /**
     * Initialize tokenization tab
     */
    private void initializeTokenizationTab() {
        // Setup token table columns
        if (tokenTable != null) {
            tokenValueColumn.setCellValueFactory(new PropertyValueFactory<>("tokenValue"));
            studentIdColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));
            tokenSchoolYearColumn.setCellValueFactory(new PropertyValueFactory<>("schoolYear"));
            tokenCreatedColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
            tokenExpiresColumn.setCellValueFactory(new PropertyValueFactory<>("expiresAt"));
            tokenStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

            // Status column with color coding
            tokenStatusColumn.setCellFactory(column -> new TableCell<TokenTableRow, String>() {
                @Override
                protected void updateItem(String status, boolean empty) {
                    super.updateItem(status, empty);
                    if (empty || status == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(status);
                        switch (status) {
                            case "ACTIVE":
                                setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold;");
                                break;
                            case "EXPIRED":
                                setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
                                break;
                            case "ROTATED":
                                setStyle("-fx-text-fill: #ff9800;");
                                break;
                            default:
                                setStyle("");
                        }
                    }
                }
            });

            // Actions column
            tokenActionsColumn.setCellFactory(column -> new TableCell<TokenTableRow, Void>() {
                private final Button rotateBtn = new Button("Rotate");
                private final Button viewBtn = new Button("View");
                private final HBox buttons = new HBox(5, viewBtn, rotateBtn);

                {
                    buttons.setAlignment(Pos.CENTER);
                    rotateBtn.setStyle("-fx-font-size: 10px;");
                    viewBtn.setStyle("-fx-font-size: 10px;");

                    rotateBtn.setOnAction(e -> {
                        TokenTableRow row = getTableView().getItems().get(getIndex());
                        handleRotateToken(row);
                    });

                    viewBtn.setOnAction(e -> {
                        TokenTableRow row = getTableView().getItems().get(getIndex());
                        handleViewToken(row);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : buttons);
                }
            });

            tokenTable.setItems(tokenList);
        }

        // Setup filters
        if (tokenStatusFilter != null) {
            tokenStatusFilter.setItems(FXCollections.observableArrayList(
                    "All", "Active", "Expired", "Rotated"));
            tokenStatusFilter.setValue("All");
            tokenStatusFilter.setOnAction(e -> filterTokens());
        }

        if (schoolYearFilter != null) {
            schoolYearFilter.setItems(FXCollections.observableArrayList(
                    "All", "2025-2026", "2024-2025"));
            schoolYearFilter.setValue("All");
            schoolYearFilter.setOnAction(e -> filterTokens());
        }

        if (tokenSearchField != null) {
            tokenSearchField.textProperty().addListener((obs, old, newVal) -> filterTokens());
        }
    }

    /**
     * Initialize device management tab
     */
    private void initializeDeviceManagementTab() {
        // Pending devices table
        if (pendingDeviceTable != null) {
            pendingDeviceIdColumn.setCellValueFactory(new PropertyValueFactory<>("deviceId"));
            pendingAccountColumn.setCellValueFactory(new PropertyValueFactory<>("accountToken"));
            pendingMacColumn.setCellValueFactory(new PropertyValueFactory<>("macAddress"));
            pendingDeviceNameColumn.setCellValueFactory(new PropertyValueFactory<>("deviceName"));
            pendingRequestedColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(formatDateTime(cellData.getValue().getRegistrationRequestedAt())));

            // Actions column for pending devices
            pendingActionsColumn.setCellFactory(column -> new TableCell<RegisteredDevice, Void>() {
                private final Button approveBtn = new Button("Approve");
                private final Button rejectBtn = new Button("Reject");
                private final HBox buttons = new HBox(5, approveBtn, rejectBtn);

                {
                    buttons.setAlignment(Pos.CENTER);
                    approveBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #4caf50; -fx-text-fill: white;");
                    rejectBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #f44336; -fx-text-fill: white;");

                    approveBtn.setOnAction(e -> {
                        RegisteredDevice device = getTableView().getItems().get(getIndex());
                        handleApproveDevice(device);
                    });

                    rejectBtn.setOnAction(e -> {
                        RegisteredDevice device = getTableView().getItems().get(getIndex());
                        handleRejectDevice(device);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : buttons);
                }
            });

            pendingDeviceTable.setItems(pendingDevices);
        }

        // Active devices table
        if (activeDeviceTable != null) {
            activeDeviceIdColumn.setCellValueFactory(new PropertyValueFactory<>("deviceId"));
            activeAccountColumn.setCellValueFactory(new PropertyValueFactory<>("accountToken"));
            activeMacColumn.setCellValueFactory(new PropertyValueFactory<>("macAddress"));
            activeCertSerialColumn.setCellValueFactory(new PropertyValueFactory<>("certificateSerialNumber"));
            activeCertExpiresColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(formatDateTime(cellData.getValue().getCertificateExpiresAt())));
            activeLastSeenColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(formatDateTime(cellData.getValue().getLastSeenAt())));

            // Actions column for active devices
            activeActionsColumn.setCellFactory(column -> new TableCell<RegisteredDevice, Void>() {
                private final Button revokeBtn = new Button("Revoke");
                private final Button detailsBtn = new Button("Details");
                private final HBox buttons = new HBox(5, detailsBtn, revokeBtn);

                {
                    buttons.setAlignment(Pos.CENTER);
                    revokeBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #d32f2f; -fx-text-fill: white;");
                    detailsBtn.setStyle("-fx-font-size: 10px;");

                    revokeBtn.setOnAction(e -> {
                        RegisteredDevice device = getTableView().getItems().get(getIndex());
                        handleRevokeDevice(device);
                    });

                    detailsBtn.setOnAction(e -> {
                        RegisteredDevice device = getTableView().getItems().get(getIndex());
                        handleViewDeviceDetails(device);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : buttons);
                }
            });

            activeDeviceTable.setItems(activeDevices);
        }
    }

    /**
     * Initialize sync operations tab
     */
    private void initializeSyncOperationsTab() {
        // Sync history table
        if (syncHistoryTable != null) {
            syncPackageIdColumn.setCellValueFactory(new PropertyValueFactory<>("packageId"));
            syncTypeColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getPackageType().name()));
            syncEntryCountColumn.setCellValueFactory(new PropertyValueFactory<>("entryCount"));
            syncChecksumColumn.setCellValueFactory(new PropertyValueFactory<>("checksum"));
            syncCreatedColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(formatDateTime(cellData.getValue().getCreatedAt())));

            syncHistoryTable.setItems(syncHistory);
        }

        // Setup sync log area
        if (syncLogArea != null) {
            syncLogArea.setEditable(false);
            syncLogArea.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 11px;");
        }
    }

    /**
     * Initialize CRL management tab
     */
    private void initializeCRLTab() {
        if (crlTable != null) {
            crlSerialColumn.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
            crlRevokedColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(formatDateTime(cellData.getValue().getRevokedAt())));
            crlReasonColumn.setCellValueFactory(new PropertyValueFactory<>("reason"));

            crlTable.setItems(crlEntries);
        }
    }

    // ========================================================================
    // DATA LOADING
    // ========================================================================

    /**
     * Refresh all data
     */
    @FXML
    public void refreshAllData() {
        log.info("Refreshing all Secure Sync data");

        CompletableFuture.runAsync(() -> {
            try {
                // Load tokenization stats
                loadTokenizationData();

                // Load device data
                loadDeviceData();

                // Load sync data
                loadSyncData();

                // Load CRL data
                loadCRLData();

                // Update dashboard
                Platform.runLater(() -> {
                    updateDashboard();
                    updateLastRefresh();
                    updateSecurityStatus();
                });

            } catch (Exception e) {
                log.error("Error refreshing data: {}", e.getMessage());
                Platform.runLater(() -> showError("Refresh Error",
                        "Failed to refresh data: " + e.getMessage()));
            }
        });
    }

    private void loadTokenizationData() {
        Platform.runLater(() -> {
            tokenList.clear();

            // Load tokens from service
            List<StudentToken> tokens = tokenizationService.getAllTokens();
            for (StudentToken token : tokens) {
                TokenTableRow row = new TokenTableRow(
                        token.getTokenValue(),
                        String.valueOf(token.getStudentId()),
                        token.getSchoolYear(),
                        formatDateTime(token.getCreatedAt()),
                        formatDateTime(token.getExpiresAt()),
                        token.getDisplayStatus()
                );
                tokenList.add(row);
            }

            // Update stats
            long activeCount = tokenizationService.countActiveTokens();
            long expiredCount = tokenizationService.countExpiredTokens();

            if (totalTokensLabel != null) {
                totalTokensLabel.setText(String.valueOf(tokens.size()));
            }
            if (activeTokensLabel != null) {
                activeTokensLabel.setText(String.valueOf(activeCount));
            }
            if (expiredTokensLabel != null) {
                expiredTokensLabel.setText(String.valueOf(expiredCount));
            }
            if (tokenCountLabel != null) {
                tokenCountLabel.setText(tokens.size() + " tokens");
            }

            addActivity("Tokenization data refreshed - " + tokens.size() + " tokens");
        });
    }

    private void loadDeviceData() {
        Platform.runLater(() -> {
            // Load pending devices
            List<RegisteredDevice> pending = deviceAuthService.getPendingRegistrations();
            pendingDevices.setAll(pending);

            // Load active devices
            List<RegisteredDevice> active = deviceAuthService.getActiveDevices();
            activeDevices.setAll(active);

            // Update counts
            if (pendingDeviceCountLabel != null) {
                pendingDeviceCountLabel.setText(String.valueOf(pending.size()));
            }
            if (activeDeviceCountLabel != null) {
                activeDeviceCountLabel.setText(String.valueOf(active.size()));
            }
            if (pendingDevicesLabel != null) {
                pendingDevicesLabel.setText(String.valueOf(pending.size()));
            }
            if (activeDevicesLabel != null) {
                activeDevicesLabel.setText(String.valueOf(active.size()));
            }

            addActivity("Device data refreshed - " + pending.size() + " pending, " + active.size() + " active");
        });
    }

    private void loadSyncData() {
        Platform.runLater(() -> {
            // Load sync history
            List<SyncBatchRecord> history = burstSyncService.getSyncHistory(50);
            syncHistory.setAll(history);

            // Load queue status
            BurstQueueStatus queueStatus = burstSyncService.getBurstQueueStatus();
            if (burstQueueSizeLabel != null) {
                burstQueueSizeLabel.setText(String.valueOf(queueStatus.getQueuedEntries()));
            }
            if (oldestEntryLabel != null) {
                oldestEntryLabel.setText(queueStatus.getOldestEntryAt() != null ?
                        formatDateTime(queueStatus.getOldestEntryAt()) : "None");
            }

            // Load statistics
            SyncStatistics stats = burstSyncService.getSyncStatistics();
            if (totalSyncedLabel != null) {
                totalSyncedLabel.setText(String.valueOf(stats.getTotalEntriesSynced()));
            }
            if (lastSyncLabel != null) {
                lastSyncLabel.setText(stats.getLastSyncAt() != null ?
                        formatDateTime(stats.getLastSyncAt()) : "Never");
            }

            addActivity("Sync data refreshed");
        });
    }

    private void loadCRLData() {
        Platform.runLater(() -> {
            CertificateRevocationList crl = deviceAuthService.getCertificateRevocationList();
            crlEntries.setAll(crl.getEntries());

            if (crlTotalLabel != null) {
                crlTotalLabel.setText(String.valueOf(crl.getTotalRevoked()));
            }
            if (crlLastGeneratedLabel != null) {
                crlLastGeneratedLabel.setText(formatDateTime(crl.getGeneratedAt()));
            }
            if (crlChecksumLabel != null) {
                crlChecksumLabel.setText(crl.getChecksum());
            }

            if (revokedCertsLabel != null) {
                revokedCertsLabel.setText(String.valueOf(crl.getTotalRevoked()));
            }
        });
    }

    private void updateDashboard() {
        // Update dashboard statistics
        if (burstQueueLabel != null) {
            BurstQueueStatus status = burstSyncService.getBurstQueueStatus();
            burstQueueLabel.setText(String.valueOf(status.getQueuedEntries()));
        }

        // Update charts would go here
    }

    // ========================================================================
    // EVENT HANDLERS - Tokenization
    // ========================================================================

    @FXML
    public void handleGenerateAllTokens() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Generate All Tokens");
        confirm.setHeaderText("Batch Token Generation");
        confirm.setContentText("This will generate tokens for all students who don't have one.\n" +
                "This operation may take some time.\n\nProceed?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                log.warn("AUDIT: Batch token generation initiated by {}",
                        SecurityContext.getCurrentUsername().orElse("admin"));

                generateAllTokensButton.setDisable(true);
                addActivity("Batch token generation started...");
                appendSyncLog("Starting batch token generation...");

                CompletableFuture.runAsync(() -> {
                    try {
                        TokenGenerationSummary summary = tokenizationService.generateAllTokens();

                        Platform.runLater(() -> {
                            generateAllTokensButton.setDisable(false);
                            addActivity("Batch generation complete: " + summary.getTokensGenerated() + " tokens");
                            appendSyncLog("Batch generation complete:\n" +
                                    "  Generated: " + summary.getTokensGenerated() + "\n" +
                                    "  Skipped: " + summary.getTokensSkipped() + "\n" +
                                    "  Failed: " + summary.getTokensFailed());

                            showInfo("Token Generation Complete",
                                    "Generated: " + summary.getTokensGenerated() + "\n" +
                                    "Skipped: " + summary.getTokensSkipped() + "\n" +
                                    "Failed: " + summary.getTokensFailed());

                            refreshAllData();
                        });

                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            generateAllTokensButton.setDisable(false);
                            showError("Generation Failed", e.getMessage());
                        });
                    }
                });
            }
        });
    }

    @FXML
    public void handleAnnualRotation() {
        Alert confirm = new Alert(Alert.AlertType.WARNING);
        confirm.setTitle("Annual Token Rotation");
        confirm.setHeaderText("WARNING: Annual Token Rotation");
        confirm.setContentText("This will rotate ALL student tokens.\n" +
                "Previous tokens will be deactivated.\n" +
                "External systems will need to receive updated tokenized data.\n\n" +
                "Are you sure you want to proceed?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                log.warn("AUDIT: Annual token rotation initiated by {}",
                        SecurityContext.getCurrentUsername().orElse("admin"));

                annualRotationButton.setDisable(true);
                addActivity("Annual token rotation started...");
                addSecurityAlert("WARNING: Annual token rotation in progress");
                appendSyncLog("Starting annual token rotation...");

                CompletableFuture.runAsync(() -> {
                    try {
                        TokenRotationSummary summary = tokenizationService.performAnnualRotation();

                        Platform.runLater(() -> {
                            annualRotationButton.setDisable(false);
                            addActivity("Annual rotation complete: " + summary.getTokensRotated() + " rotated");
                            appendSyncLog("Annual rotation complete:\n" +
                                    "  Rotated: " + summary.getTokensRotated() + "\n" +
                                    "  Skipped: " + summary.getTokensSkipped());

                            showInfo("Annual Rotation Complete",
                                    "Rotated: " + summary.getTokensRotated() + "\n" +
                                    "Skipped: " + summary.getTokensSkipped() + "\n\n" +
                                    "Remember to generate new sync packages for external systems.");

                            refreshAllData();
                        });

                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            annualRotationButton.setDisable(false);
                            showError("Rotation Failed", e.getMessage());
                        });
                    }
                });
            }
        });
    }

    private void handleRotateToken(TokenTableRow row) {
        TextInputDialog dialog = new TextInputDialog("Manual rotation");
        dialog.setTitle("Rotate Token");
        dialog.setHeaderText("Rotate Token: " + row.getTokenValue());
        dialog.setContentText("Enter rotation reason:");

        dialog.showAndWait().ifPresent(reason -> {
            log.warn("AUDIT: Token rotation for student {} by {} - Reason: {}",
                    row.getStudentId(), SecurityContext.getCurrentUsername().orElse("admin"), reason);

            try {
                Long studentId = Long.parseLong(row.getStudentId());
                StudentToken newToken = tokenizationService.rotateToken(studentId, reason);

                addActivity("Token rotated: " + row.getTokenValue() + " -> " + newToken.getTokenValue());
                appendSyncLog("Token rotated:\n" +
                        "  Old Token: " + row.getTokenValue() + "\n" +
                        "  New Token: " + newToken.getTokenValue() + "\n" +
                        "  Reason: " + reason);

                showInfo("Token Rotated", "Token has been rotated successfully.\n\n" +
                        "Old Token: " + row.getTokenValue() + "\n" +
                        "New Token: " + newToken.getTokenValue() + "\n\n" +
                        "Previous token is now inactive.");
                refreshAllData();
            } catch (Exception e) {
                showError("Rotation Failed", e.getMessage());
            }
        });
    }

    @FXML
    public void handleExportTokens() {
        log.warn("AUDIT: Token export requested by {}", SecurityContext.getCurrentUsername().orElse("admin"));

        List<StudentToken> tokens = tokenizationService.getAllTokens();

        if (tokens.isEmpty()) {
            showInfo("No Tokens", "There are no tokens to export.");
            return;
        }

        // Build export content
        StringBuilder export = new StringBuilder();
        export.append("Token Value,Student ID,School Year,Created At,Expires At,Status\n");
        for (StudentToken token : tokens) {
            export.append(token.getTokenValue()).append(",")
                  .append(token.getStudentId()).append(",")
                  .append(token.getSchoolYear()).append(",")
                  .append(formatDateTime(token.getCreatedAt())).append(",")
                  .append(formatDateTime(token.getExpiresAt())).append(",")
                  .append(token.getDisplayStatus()).append("\n");
        }

        addActivity("Token export: " + tokens.size() + " tokens exported");
        appendSyncLog("Token export generated:\n  Total Tokens: " + tokens.size());

        // TODO: Implement FileChooser to save CSV to secure location
        // TODO: Add encryption option for exported token data
        // TODO: Implement audit trail for export operations
        showInfo("Export Tokens",
                "Token export generated.\n\n" +
                "Total Tokens: " + tokens.size() + "\n\n" +
                "In production, this would save to a secure file.\n" +
                "Use secure air-gap transfer method for external systems.");
    }

    private void handleViewToken(TokenTableRow row) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Token Details");
        info.setHeaderText("Token: " + row.getTokenValue());
        info.setContentText(
                "Student ID: " + row.getStudentId() + "\n" +
                "School Year: " + row.getSchoolYear() + "\n" +
                "Created: " + row.getCreatedAt() + "\n" +
                "Expires: " + row.getExpiresAt() + "\n" +
                "Status: " + row.getStatus() + "\n\n" +
                "Note: Student name and PII are not associated with token.");
        info.showAndWait();
    }

    private void filterTokens() {
        String searchText = tokenSearchField != null ? tokenSearchField.getText() : "";
        String statusFilter = tokenStatusFilter != null ? tokenStatusFilter.getValue() : "All";
        String yearFilter = schoolYearFilter != null ? schoolYearFilter.getValue() : "All";

        // Get fresh data from service with filters
        Boolean activeFilter = null;
        if ("Active".equals(statusFilter)) {
            activeFilter = true;
        } else if ("Expired".equals(statusFilter) || "Rotated".equals(statusFilter)) {
            activeFilter = false;
        }

        String schoolYear = "All".equals(yearFilter) ? null : yearFilter;

        List<StudentToken> tokens;
        if (searchText != null && !searchText.trim().isEmpty()) {
            tokens = tokenizationService.searchTokens(searchText.trim());
        } else {
            tokens = tokenizationService.getAllTokens(activeFilter, schoolYear);
        }

        // Apply additional filters
        tokenList.clear();
        for (StudentToken token : tokens) {
            String displayStatus = token.getDisplayStatus();

            // Filter by status if needed
            if ("Rotated".equals(statusFilter) && !"ROTATED".equals(displayStatus)) {
                continue;
            }
            if ("Expired".equals(statusFilter) && !"EXPIRED".equals(displayStatus)) {
                continue;
            }

            TokenTableRow row = new TokenTableRow(
                    token.getTokenValue(),
                    String.valueOf(token.getStudentId()),
                    token.getSchoolYear(),
                    formatDateTime(token.getCreatedAt()),
                    formatDateTime(token.getExpiresAt()),
                    displayStatus
            );
            tokenList.add(row);
        }

        if (tokenCountLabel != null) {
            tokenCountLabel.setText(tokenList.size() + " tokens");
        }
    }

    // ========================================================================
    // EVENT HANDLERS - Device Management
    // ========================================================================

    private void handleApproveDevice(RegisteredDevice device) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Approve Device");
        confirm.setHeaderText("Approve Device Registration");
        confirm.setContentText("Device: " + device.getDeviceName() + "\n" +
                "MAC: " + device.getMacAddress() + "\n" +
                "Account: " + device.getAccountToken() + "\n\n" +
                "This will generate a certificate for this device.\nApprove?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                log.warn("AUDIT: Device {} approved by {}",
                        device.getDeviceId(), SecurityContext.getCurrentUsername().orElse("admin"));

                try {
                    DeviceApprovalResult result = deviceAuthService.approveRegistration(
                            device.getDeviceId(), SecurityContext.getCurrentUsername().orElse("admin"));

                    if (result.isSuccess()) {
                        addActivity("Device approved: " + device.getDeviceId());
                        appendSyncLog("Device approved: " + device.getDeviceId() +
                                "\n  Certificate: " + result.getCertificateSerialNumber());

                        showInfo("Device Approved",
                                "Device has been approved.\n\n" +
                                "Certificate Serial: " + result.getCertificateSerialNumber() + "\n" +
                                "Expires: " + formatDateTime(result.getCertificateExpiresAt()) + "\n\n" +
                                result.getCertificateInstallationInstructions());

                        refreshAllData();
                    } else {
                        showError("Approval Failed", result.getErrorMessage());
                    }

                } catch (Exception e) {
                    showError("Approval Failed", e.getMessage());
                }
            }
        });
    }

    private void handleRejectDevice(RegisteredDevice device) {
        TextInputDialog dialog = new TextInputDialog("Registration denied");
        dialog.setTitle("Reject Device");
        dialog.setHeaderText("Reject Device: " + device.getDeviceName());
        dialog.setContentText("Enter rejection reason:");

        dialog.showAndWait().ifPresent(reason -> {
            log.warn("AUDIT: Device {} rejected by {} - Reason: {}",
                    device.getDeviceId(), SecurityContext.getCurrentUsername().orElse("admin"), reason);

            try {
                DeviceRejectionResult result = deviceAuthService.rejectRegistration(
                        device.getDeviceId(), SecurityContext.getCurrentUsername().orElse("admin"), reason);

                if (result.isSuccess()) {
                    addActivity("Device rejected: " + device.getDeviceId());
                    refreshAllData();
                } else {
                    showError("Rejection Failed", result.getErrorMessage());
                }

            } catch (Exception e) {
                showError("Rejection Failed", e.getMessage());
            }
        });
    }

    private void handleRevokeDevice(RegisteredDevice device) {
        Alert confirm = new Alert(Alert.AlertType.WARNING);
        confirm.setTitle("Revoke Certificate");
        confirm.setHeaderText("WARNING: Revoke Device Certificate");
        confirm.setContentText("Device: " + device.getDeviceName() + "\n" +
                "Certificate: " + device.getCertificateSerialNumber() + "\n\n" +
                "This will immediately revoke the device's certificate.\n" +
                "The device will no longer be able to authenticate.\n\n" +
                "Are you sure?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                TextInputDialog reasonDialog = new TextInputDialog("Security concern");
                reasonDialog.setTitle("Revocation Reason");
                reasonDialog.setContentText("Enter revocation reason:");

                reasonDialog.showAndWait().ifPresent(reason -> {
                    log.warn("SECURITY: Certificate revocation for device {} by {} - Reason: {}",
                            device.getDeviceId(), SecurityContext.getCurrentUsername().orElse("admin"), reason);

                    try {
                        CertificateRevocationResult result = deviceAuthService.revokeCertificate(
                                device.getDeviceId(), SecurityContext.getCurrentUsername().orElse("admin"), reason);

                        if (result.isSuccess()) {
                            addActivity("CERTIFICATE REVOKED: " + device.getCertificateSerialNumber());
                            addSecurityAlert("CRITICAL: Certificate revoked - " +
                                    result.getCertificateSerialNumber());
                            appendSyncLog("CERTIFICATE REVOKED:\n" +
                                    "  Device: " + device.getDeviceId() + "\n" +
                                    "  Serial: " + result.getCertificateSerialNumber() + "\n" +
                                    "  Reason: " + reason + "\n" +
                                    "  CRL sync required!");

                            showWarning("Certificate Revoked",
                                    "Certificate has been revoked and added to CRL.\n\n" +
                                    "IMPORTANT: Generate and distribute new CRL sync package\n" +
                                    "to prevent the device from authenticating.");

                            refreshAllData();
                        } else {
                            showError("Revocation Failed", result.getErrorMessage());
                        }

                    } catch (Exception e) {
                        showError("Revocation Failed", e.getMessage());
                    }
                });
            }
        });
    }

    private void handleViewDeviceDetails(RegisteredDevice device) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Device Details");
        info.setHeaderText("Device: " + device.getDeviceId());
        info.setContentText(
                "Device Name: " + device.getDeviceName() + "\n" +
                "Device Type: " + device.getDeviceType() + "\n" +
                "OS: " + device.getOperatingSystem() + "\n" +
                "MAC Address: " + device.getMacAddress() + "\n" +
                "Account Token: " + device.getAccountToken() + "\n\n" +
                "Certificate Serial: " + device.getCertificateSerialNumber() + "\n" +
                "Certificate Expires: " + formatDateTime(device.getCertificateExpiresAt()) + "\n" +
                "Approved By: " + device.getApprovedBy() + "\n" +
                "Approved At: " + formatDateTime(device.getApprovedAt()) + "\n" +
                "Last Seen: " + formatDateTime(device.getLastSeenAt()));
        info.showAndWait();
    }

    @FXML
    public void handleApproveAllDevices() {
        if (pendingDevices.isEmpty()) {
            showInfo("No Pending Devices", "There are no devices pending approval.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.WARNING);
        confirm.setTitle("Approve All Devices");
        confirm.setHeaderText("Approve All Pending Devices");
        confirm.setContentText("This will approve " + pendingDevices.size() + " devices.\n\n" +
                "Are you sure?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                log.warn("AUDIT: Bulk device approval initiated by {} for {} devices",
                        SecurityContext.getCurrentUsername().orElse("admin"), pendingDevices.size());

                // Would process all pending devices
                addActivity("Bulk approval: " + pendingDevices.size() + " devices");
            }
        });
    }

    // ========================================================================
    // EVENT HANDLERS - Sync Operations
    // ========================================================================

    @FXML
    public void handleProcessBurstQueue() {
        log.info("Processing burst queue");

        processBurstButton.setDisable(true);
        appendSyncLog("Processing burst queue...");

        CompletableFuture.runAsync(() -> {
            try {
                SyncPackage syncPackage = burstSyncService.processBurstQueue();

                Platform.runLater(() -> {
                    processBurstButton.setDisable(false);

                    if (syncPackage.getEntryCount() > 0) {
                        addActivity("Burst processed: " + syncPackage.getEntryCount() + " entries");
                        appendSyncLog("Burst package created:\n" +
                                "  Package ID: " + syncPackage.getPackageId() + "\n" +
                                "  Entries: " + syncPackage.getEntryCount() + "\n" +
                                "  Checksum: " + syncPackage.getChecksum());

                        showInfo("Burst Queue Processed",
                                "Package ID: " + syncPackage.getPackageId() + "\n" +
                                "Entries: " + syncPackage.getEntryCount() + "\n" +
                                "Checksum: " + syncPackage.getChecksum() + "\n\n" +
                                "Package ready for secure transfer to Server 3.");
                    } else {
                        appendSyncLog("Burst queue empty - no package created");
                        showInfo("Queue Empty", "No entries in burst queue to process.");
                    }

                    refreshAllData();
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    processBurstButton.setDisable(false);
                    showError("Processing Failed", e.getMessage());
                });
            }
        });
    }

    @FXML
    public void handleGenerateEnrollmentBatch() {
        log.warn("AUDIT: Enrollment batch generation requested by {}",
                SecurityContext.getCurrentUsername().orElse("admin"));

        generateEnrollmentButton.setDisable(true);
        appendSyncLog("Generating enrollment batch...");

        CompletableFuture.runAsync(() -> {
            try {
                EncryptedSyncPackage encrypted = burstSyncService.generateEnrollmentBatch();

                Platform.runLater(() -> {
                    generateEnrollmentButton.setDisable(false);

                    addActivity("Enrollment batch generated: " + encrypted.getEntryCount() + " students");
                    appendSyncLog("Enrollment batch created:\n" +
                            "  Package ID: " + encrypted.getPackageId() + "\n" +
                            "  Entries: " + encrypted.getEntryCount() + "\n" +
                            "  Algorithm: " + encrypted.getAlgorithm() + "\n" +
                            "  Key ID: " + encrypted.getKeyId() + "\n" +
                            "  Checksum: " + encrypted.getOriginalChecksum());

                    showInfo("Enrollment Batch Generated",
                            "Package ID: " + encrypted.getPackageId() + "\n" +
                            "Students: " + encrypted.getEntryCount() + "\n" +
                            "Encryption: " + encrypted.getAlgorithm() + "\n\n" +
                            "Package is encrypted and ready for secure transfer.\n" +
                            "Use secure air-gap transfer method to Server 3.");

                    refreshAllData();
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    generateEnrollmentButton.setDisable(false);
                    showError("Generation Failed", e.getMessage());
                });
            }
        });
    }

    @FXML
    public void handleGenerateCRLSync() {
        log.info("Generating CRL sync package");

        generateCRLSyncButton.setDisable(true);
        appendSyncLog("Generating CRL sync package...");

        CompletableFuture.runAsync(() -> {
            try {
                SyncPackage syncPackage = burstSyncService.generateCRLSyncPackage();

                Platform.runLater(() -> {
                    generateCRLSyncButton.setDisable(false);

                    addActivity("CRL sync generated: " + syncPackage.getChecksum());
                    appendSyncLog("CRL sync package created:\n" +
                            "  Package ID: " + syncPackage.getPackageId() + "\n" +
                            "  Checksum: " + syncPackage.getChecksum());

                    showInfo("CRL Sync Package Generated",
                            "Package ID: " + syncPackage.getPackageId() + "\n" +
                            "Checksum: " + syncPackage.getChecksum() + "\n\n" +
                            "Transfer to Server 3 immediately to propagate\n" +
                            "certificate revocations.");

                    refreshAllData();
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    generateCRLSyncButton.setDisable(false);
                    showError("Generation Failed", e.getMessage());
                });
            }
        });
    }

    // ========================================================================
    // EVENT HANDLERS - CRL
    // ========================================================================

    @FXML
    public void handleExportCRL() {
        log.warn("AUDIT: CRL export requested by {}", SecurityContext.getCurrentUsername().orElse("admin"));

        // Would open file save dialog and export CRL to file
        CertificateRevocationList crl = deviceAuthService.getCertificateRevocationList();

        showInfo("Export CRL",
                "CRL would be exported to file.\n\n" +
                "Total Revoked: " + crl.getTotalRevoked() + "\n" +
                "Checksum: " + crl.getChecksum() + "\n\n" +
                "In production, this would save to a secure location.");
    }

    @FXML
    public void handleRegenerateCRL() {
        log.info("Regenerating CRL");

        loadCRLData();
        showInfo("CRL Regenerated", "Certificate Revocation List has been regenerated.");
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    private void setupAutoRefresh() {
        autoRefreshTimer = new Timer(true);
        autoRefreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    // Only refresh queue status and stats (lightweight)
                    loadSyncData();
                    updateSecurityStatus();
                });
            }
        }, 30000, 30000); // Every 30 seconds
    }

    private void updateSecurityStatus() {
        if (securityStatusIndicator != null) {
            // Check for any security concerns
            CertificateRevocationList crl = deviceAuthService.getCertificateRevocationList();
            BurstQueueStatus queue = burstSyncService.getBurstQueueStatus();

            boolean hasAlerts = crl.getTotalRevoked() > 0 ||
                    (queue.getOldestEntryAt() != null &&
                     queue.getOldestEntryAt().isBefore(LocalDateTime.now().minusMinutes(5)));

            if (hasAlerts) {
                securityStatusIndicator.setFill(Color.ORANGE);
                securityStatusLabel.setText("Attention Required");
            } else {
                securityStatusIndicator.setFill(Color.GREEN);
                securityStatusLabel.setText("System Secure");
            }
        }
    }

    private void updateLastRefresh() {
        if (lastRefreshLabel != null) {
            lastRefreshLabel.setText("Last refresh: " +
                    LocalDateTime.now().format(shortDateFormatter));
        }
    }

    private void addActivity(String activity) {
        String timestamp = LocalDateTime.now().format(shortDateFormatter);
        recentActivity.add(0, "[" + timestamp + "] " + activity);
        if (recentActivity.size() > 50) {
            recentActivity.remove(recentActivity.size() - 1);
        }
    }

    private void addSecurityAlert(String alert) {
        String timestamp = LocalDateTime.now().format(shortDateFormatter);
        securityAlerts.add(0, "[" + timestamp + "] " + alert);
        if (securityAlerts.size() > 20) {
            securityAlerts.remove(securityAlerts.size() - 1);
        }
    }

    private void appendSyncLog(String message) {
        if (syncLogArea != null) {
            String timestamp = LocalDateTime.now().format(dateFormatter);
            syncLogArea.appendText("[" + timestamp + "]\n" + message + "\n\n");
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(dateFormatter);
    }

    private void disableAllControls() {
        if (mainTabPane != null) mainTabPane.setDisable(true);
        if (refreshAllButton != null) refreshAllButton.setDisable(true);
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
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

    /**
     * Show a toast notification (non-blocking)
     *
     * @param message Message to display
     * @param type Type: "success", "warning", "error", "info"
     */
    private void showToast(String message, String type) {
        String style;
        switch (type) {
            case "success":
                style = "-fx-background-color: #4caf50; -fx-text-fill: white;";
                break;
            case "warning":
                style = "-fx-background-color: #ff9800; -fx-text-fill: white;";
                break;
            case "error":
                style = "-fx-background-color: #f44336; -fx-text-fill: white;";
                break;
            default:
                style = "-fx-background-color: #2196f3; -fx-text-fill: white;";
        }

        // Create toast label
        Label toast = new Label(message);
        toast.setStyle(style + "-fx-padding: 10 20; -fx-background-radius: 4; -fx-font-size: 12px;");

        // Add to activity log as visual feedback
        addActivity("[" + type.toUpperCase() + "] " + message);
    }

    /**
     * Set loading state on a button
     *
     * @param button Button to update
     * @param loading Whether loading
     * @param originalText Original button text to restore
     */
    private void setButtonLoading(Button button, boolean loading, String originalText) {
        if (button == null) return;

        if (loading) {
            button.setText("⏳ Processing...");
            button.setDisable(true);
        } else {
            button.setText(originalText);
            button.setDisable(false);
        }
    }

    /**
     * Update the sync progress bar
     *
     * @param progress Progress value (0.0 to 1.0)
     * @param statusMessage Status message
     */
    private void updateSyncProgress(double progress, String statusMessage) {
        if (syncProgressBar != null) {
            syncProgressBar.setProgress(progress);
        }
        if (syncStatusLabel != null) {
            syncStatusLabel.setText(statusMessage);
        }
    }

    /**
     * Cleanup resources
     */
    public void cleanup() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.cancel();
        }
    }

    // ========================================================================
    // INNER CLASSES
    // ========================================================================

    /**
     * Table row model for token display
     */
    public static class TokenTableRow {
        private String tokenValue;
        private String studentId;
        private String schoolYear;
        private String createdAt;
        private String expiresAt;
        private String status;

        public TokenTableRow(String tokenValue, String studentId, String schoolYear,
                            String createdAt, String expiresAt, String status) {
            this.tokenValue = tokenValue;
            this.studentId = studentId;
            this.schoolYear = schoolYear;
            this.createdAt = createdAt;
            this.expiresAt = expiresAt;
            this.status = status;
        }

        public String getTokenValue() { return tokenValue; }
        public String getStudentId() { return studentId; }
        public String getSchoolYear() { return schoolYear; }
        public String getCreatedAt() { return createdAt; }
        public String getExpiresAt() { return expiresAt; }
        public String getStatus() { return status; }
    }
}
