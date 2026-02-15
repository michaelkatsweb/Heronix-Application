package com.heronix.ui.controller;

import com.heronix.client.TransferAuthorizationApiService;
import com.heronix.security.LocalKeyStore;
import com.heronix.security.SecurityContext;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;

/**
 * Secure Transfer Dashboard Controller
 *
 * Full-page dashboard for managing HSTP (Heronix Secure Transfer Protocol)
 * transfer authorizations, signing keys, and FERPA compliance logs.
 *
 * @author Heronix Development Team
 * @version 1.0.0
 * @since 2026-02 - HSTP Implementation
 */
@Slf4j
@Controller
public class SecureTransferDashboardController {

    // ========================================================================
    // DEPENDENCIES
    // ========================================================================

    @Autowired
    private TransferAuthorizationApiService transferApiService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private LocalKeyStore localKeyStore;

    // ========================================================================
    // FXML FIELDS — Statistics
    // ========================================================================

    @FXML private Label statTotal;
    @FXML private Label statPending;
    @FXML private Label statReady;
    @FXML private Label statCompleted;

    // ========================================================================
    // FXML FIELDS — Filters
    // ========================================================================

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;
    @FXML private TabPane mainTabPane;

    // ========================================================================
    // FXML FIELDS — Authorizations Tab
    // ========================================================================

    @FXML private Label authCountLabel;
    @FXML private TableView<Map<String, Object>> authorizationsTable;
    @FXML private TableColumn<Map<String, Object>, String> authNumColumn;
    @FXML private TableColumn<Map<String, Object>, String> authStatusColumn;
    @FXML private TableColumn<Map<String, Object>, String> authStudentsColumn;
    @FXML private TableColumn<Map<String, Object>, String> authDestColumn;
    @FXML private TableColumn<Map<String, Object>, String> authReasonColumn;
    @FXML private TableColumn<Map<String, Object>, String> authCreatedColumn;
    @FXML private TableColumn<Map<String, Object>, String> authDeliveryColumn;
    @FXML private TableColumn<Map<String, Object>, Void> authActionsColumn;

    // ========================================================================
    // FXML FIELDS — Pending Signatures Tab
    // ========================================================================

    @FXML private Label pendingCountLabel;
    @FXML private TableView<Map<String, Object>> pendingTable;
    @FXML private TableColumn<Map<String, Object>, String> pendingAuthNumColumn;
    @FXML private TableColumn<Map<String, Object>, String> pendingDestColumn;
    @FXML private TableColumn<Map<String, Object>, String> pendingStudentsColumn;
    @FXML private TableColumn<Map<String, Object>, String> pendingReasonColumn;
    @FXML private TableColumn<Map<String, Object>, String> pendingCreatedColumn;
    @FXML private TableColumn<Map<String, Object>, Void> pendingActionsColumn;

    // ========================================================================
    // FXML FIELDS — Key Management Tab
    // ========================================================================

    @FXML private TableView<Map<String, Object>> keysTable;
    @FXML private TableColumn<Map<String, Object>, String> keyFingerprintColumn;
    @FXML private TableColumn<Map<String, Object>, String> keyLabelColumn;
    @FXML private TableColumn<Map<String, Object>, String> keyRoleColumn;
    @FXML private TableColumn<Map<String, Object>, String> keyCreatedColumn;
    @FXML private TableColumn<Map<String, Object>, String> keyStatusColumn;
    @FXML private TableColumn<Map<String, Object>, Void> keyActionsColumn;

    @FXML private TableView<Map<String, Object>> authorizersTable;
    @FXML private TableColumn<Map<String, Object>, String> authorizerNameColumn;
    @FXML private TableColumn<Map<String, Object>, String> authorizerRoleColumn;
    @FXML private TableColumn<Map<String, Object>, String> authorizerFingerprintColumn;
    @FXML private TableColumn<Map<String, Object>, String> authorizerKeyDateColumn;
    @FXML private TableColumn<Map<String, Object>, String> authorizerKeyStatusColumn;

    // ========================================================================
    // FXML FIELDS — FERPA Log Tab
    // ========================================================================

    @FXML private Label ferpaCountLabel;
    @FXML private TextField ferpaSearchField;
    @FXML private DatePicker ferpaDateFrom;
    @FXML private DatePicker ferpaDateTo;
    @FXML private TableView<Map<String, Object>> ferpaTable;
    @FXML private TableColumn<Map<String, Object>, String> ferpaDateColumn;
    @FXML private TableColumn<Map<String, Object>, String> ferpaStudentColumn;
    @FXML private TableColumn<Map<String, Object>, String> ferpaRecipientColumn;
    @FXML private TableColumn<Map<String, Object>, String> ferpaRecordsColumn;
    @FXML private TableColumn<Map<String, Object>, String> ferpaLegalBasisColumn;
    @FXML private TableColumn<Map<String, Object>, String> ferpaDisclosedByColumn;

    // ========================================================================
    // STATE
    // ========================================================================

    private final ObservableList<Map<String, Object>> authorizationsList = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> pendingList = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> keysList = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> authorizersList = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> ferpaList = FXCollections.observableArrayList();

    private FilteredList<Map<String, Object>> filteredAuthorizations;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    @FXML
    public void initialize() {
        log.info("SecureTransferDashboardController initializing...");

        setupStatusFilter();
        setupAuthorizationsTable();
        setupPendingTable();
        setupKeysTable();
        setupAuthorizersTable();
        setupFerpaTable();

        // Set up filtered list for main authorizations table
        filteredAuthorizations = new FilteredList<>(authorizationsList, p -> true);
        authorizationsTable.setItems(filteredAuthorizations);
        pendingTable.setItems(pendingList);
        keysTable.setItems(keysList);
        authorizersTable.setItems(authorizersList);
        ferpaTable.setItems(ferpaList);

        // Load data asynchronously
        loadAllData();

        log.info("SecureTransferDashboard initialized");
    }

    private void setupStatusFilter() {
        statusFilter.setItems(FXCollections.observableArrayList(
                "All Statuses",
                "DRAFT",
                "PENDING_SIGNATURES",
                "QUORUM_MET",
                "READY",
                "DELIVERED",
                "COMPLETED",
                "REVOKED"
        ));
        statusFilter.setValue("All Statuses");
    }

    // ========================================================================
    // TABLE SETUP — Authorizations
    // ========================================================================

    private void setupAuthorizationsTable() {
        authNumColumn.setCellValueFactory(data ->
                new SimpleStringProperty(getStr(data.getValue(), "authorizationNumber")));
        authStatusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(formatStatus(getStr(data.getValue(), "status"))));
        authStudentsColumn.setCellValueFactory(data ->
                new SimpleStringProperty(getStr(data.getValue(), "studentCount")));
        authDestColumn.setCellValueFactory(data ->
                new SimpleStringProperty(getStr(data.getValue(), "destinationSchoolName")));
        authReasonColumn.setCellValueFactory(data ->
                new SimpleStringProperty(formatReason(getStr(data.getValue(), "reasonCode"))));
        authCreatedColumn.setCellValueFactory(data ->
                new SimpleStringProperty(formatDateTime(getStr(data.getValue(), "createdAt"))));
        authDeliveryColumn.setCellValueFactory(data ->
                new SimpleStringProperty(formatDelivery(getStr(data.getValue(), "deliveryMethod"))));

        // Status column styling
        authStatusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(getStatusStyle(item));
                }
            }
        });

        // Actions column
        authActionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button signBtn = new Button("Sign");
            private final Button pkgBtn = new Button("Package");
            private final HBox box = new HBox(5, viewBtn, signBtn, pkgBtn);

            {
                viewBtn.getStyleClass().add("button-info");
                viewBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3 8;");
                signBtn.getStyleClass().add("button-primary");
                signBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3 8;");
                pkgBtn.getStyleClass().add("button-success");
                pkgBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3 8;");

                viewBtn.setOnAction(e -> {
                    Map<String, Object> row = getTableView().getItems().get(getIndex());
                    openAuthorizationDialog(getLong(row, "id"), false);
                });
                signBtn.setOnAction(e -> {
                    Map<String, Object> row = getTableView().getItems().get(getIndex());
                    openSigningDialog(getLong(row, "id"));
                });
                pkgBtn.setOnAction(e -> {
                    Map<String, Object> row = getTableView().getItems().get(getIndex());
                    openPackageDialog(getLong(row, "id"));
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    // ========================================================================
    // TABLE SETUP — Pending Signatures
    // ========================================================================

    private void setupPendingTable() {
        pendingAuthNumColumn.setCellValueFactory(data ->
                new SimpleStringProperty(getStr(data.getValue(), "authorizationNumber")));
        pendingDestColumn.setCellValueFactory(data ->
                new SimpleStringProperty(getStr(data.getValue(), "destinationSchoolName")));
        pendingStudentsColumn.setCellValueFactory(data ->
                new SimpleStringProperty(getStr(data.getValue(), "studentCount")));
        pendingReasonColumn.setCellValueFactory(data ->
                new SimpleStringProperty(formatReason(getStr(data.getValue(), "reasonCode"))));
        pendingCreatedColumn.setCellValueFactory(data ->
                new SimpleStringProperty(formatDateTime(getStr(data.getValue(), "createdAt"))));

        // Pending actions — Sign button
        pendingActionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button signBtn = new Button("Sign Now");

            {
                signBtn.getStyleClass().add("button-primary");
                signBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3 12;");
                signBtn.setOnAction(e -> {
                    Map<String, Object> row = getTableView().getItems().get(getIndex());
                    openSigningDialog(getLong(row, "id"));
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : signBtn);
            }
        });
    }

    // ========================================================================
    // TABLE SETUP — Key Management
    // ========================================================================

    private void setupKeysTable() {
        keyFingerprintColumn.setCellValueFactory(data ->
                new SimpleStringProperty(getStr(data.getValue(), "fingerprint")));
        keyLabelColumn.setCellValueFactory(data ->
                new SimpleStringProperty(getStr(data.getValue(), "label")));
        keyRoleColumn.setCellValueFactory(data ->
                new SimpleStringProperty(getStr(data.getValue(), "authorizedRole")));
        keyCreatedColumn.setCellValueFactory(data ->
                new SimpleStringProperty(formatDateTime(getStr(data.getValue(), "registeredAt"))));
        keyStatusColumn.setCellValueFactory(data -> {
            boolean active = Boolean.TRUE.equals(data.getValue().get("active"));
            return new SimpleStringProperty(active ? "Active" : "Revoked");
        });

        // Key status styling
        keyStatusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("Active".equals(item)
                            ? "-fx-text-fill: #00ff88; -fx-font-weight: bold;"
                            : "-fx-text-fill: #ff4444; -fx-font-weight: bold;");
                }
            }
        });

        // Revoke button
        keyActionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button revokeBtn = new Button("Revoke");

            {
                revokeBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3 8; -fx-background-color: #cc3333; -fx-text-fill: white;");
                revokeBtn.setOnAction(e -> {
                    Map<String, Object> row = getTableView().getItems().get(getIndex());
                    handleRevokeKey(row);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Map<String, Object> row = getTableView().getItems().get(getIndex());
                    boolean active = Boolean.TRUE.equals(row.get("active"));
                    setGraphic(active ? revokeBtn : null);
                }
            }
        });
    }

    private void setupAuthorizersTable() {
        authorizerNameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(getStr(data.getValue(), "fullName")));
        authorizerRoleColumn.setCellValueFactory(data ->
                new SimpleStringProperty(getStr(data.getValue(), "authorizedRole")));
        authorizerFingerprintColumn.setCellValueFactory(data ->
                new SimpleStringProperty(getStr(data.getValue(), "fingerprint")));
        authorizerKeyDateColumn.setCellValueFactory(data ->
                new SimpleStringProperty(formatDateTime(getStr(data.getValue(), "registeredAt"))));
        authorizerKeyStatusColumn.setCellValueFactory(data -> {
            boolean active = Boolean.TRUE.equals(data.getValue().get("active"));
            return new SimpleStringProperty(active ? "Active" : "Revoked");
        });

        authorizerKeyStatusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("Active".equals(item)
                            ? "-fx-text-fill: #00ff88; -fx-font-weight: bold;"
                            : "-fx-text-fill: #ff4444; -fx-font-weight: bold;");
                }
            }
        });
    }

    // ========================================================================
    // TABLE SETUP — FERPA Log
    // ========================================================================

    private void setupFerpaTable() {
        ferpaDateColumn.setCellValueFactory(data ->
                new SimpleStringProperty(formatDateTime(getStr(data.getValue(), "disclosedAt"))));
        ferpaStudentColumn.setCellValueFactory(data ->
                new SimpleStringProperty(getStr(data.getValue(), "studentName")));
        ferpaRecipientColumn.setCellValueFactory(data ->
                new SimpleStringProperty(getStr(data.getValue(), "recipientOrganization")));
        ferpaRecordsColumn.setCellValueFactory(data ->
                new SimpleStringProperty(getStr(data.getValue(), "recordsDisclosed")));
        ferpaLegalBasisColumn.setCellValueFactory(data ->
                new SimpleStringProperty(getStr(data.getValue(), "ferpaExceptionCited")));
        ferpaDisclosedByColumn.setCellValueFactory(data ->
                new SimpleStringProperty(getStr(data.getValue(), "disclosedBy")));
    }

    // ========================================================================
    // DATA LOADING
    // ========================================================================

    private void loadAllData() {
        Thread.ofVirtual().name("hstp-dashboard-load").start(() -> {
            try {
                loadStatistics();
                loadAuthorizations();
                loadPendingForMe();
                loadMyKeys();
                loadAuthorizers();
                loadFerpaLog();
            } catch (Exception e) {
                log.error("Error loading dashboard data", e);
                Platform.runLater(() -> showError("Failed to load dashboard data: " + e.getMessage()));
            }
        });
    }

    private void loadStatistics() {
        try {
            Map<String, Object> stats = transferApiService.getStatistics();
            Platform.runLater(() -> {
                statTotal.setText(String.valueOf(getNumeric(stats, "total")));
                statPending.setText(String.valueOf(getNumeric(stats, "pendingSignatures")));
                statReady.setText(String.valueOf(getNumeric(stats, "ready")));
                statCompleted.setText(String.valueOf(getNumeric(stats, "completed")));
            });
        } catch (Exception e) {
            log.warn("Failed to load statistics: {}", e.getMessage());
        }
    }

    private void loadAuthorizations() {
        try {
            String statusValue = statusFilter.getValue();
            String status = (statusValue == null || "All Statuses".equals(statusValue)) ? null : statusValue;
            List<Map<String, Object>> auths = transferApiService.listAuthorizations(status);
            Platform.runLater(() -> {
                authorizationsList.setAll(auths);
                authCountLabel.setText("Total: " + auths.size());
            });
        } catch (Exception e) {
            log.warn("Failed to load authorizations: {}", e.getMessage());
        }
    }

    private void loadPendingForMe() {
        try {
            List<Map<String, Object>> pending = transferApiService.getPendingForMe();
            Platform.runLater(() -> {
                pendingList.setAll(pending);
                pendingCountLabel.setText("Total: " + pending.size());
            });
        } catch (Exception e) {
            log.warn("Failed to load pending authorizations: {}", e.getMessage());
        }
    }

    private void loadMyKeys() {
        try {
            Long userId = SecurityContext.getCurrentUserId();
            List<Map<String, Object>> keys = transferApiService.getKeysForUser(userId);
            Platform.runLater(() -> keysList.setAll(keys));
        } catch (Exception e) {
            log.warn("Failed to load signing keys: {}", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadAuthorizers() {
        try {
            Map<String, Object> authorizersMap = transferApiService.getAuthorizers();
            List<Map<String, Object>> allAuthorizers = new ArrayList<>();

            for (Map.Entry<String, Object> entry : authorizersMap.entrySet()) {
                if (entry.getValue() instanceof List<?> list) {
                    for (Object item : list) {
                        if (item instanceof Map<?, ?> map) {
                            allAuthorizers.add((Map<String, Object>) map);
                        }
                    }
                }
            }
            Platform.runLater(() -> authorizersList.setAll(allAuthorizers));
        } catch (Exception e) {
            log.warn("Failed to load authorizers: {}", e.getMessage());
        }
    }

    private void loadFerpaLog() {
        try {
            // FERPA logs are per-authorization; load from all authorizations
            List<Map<String, Object>> allAuths = transferApiService.listAuthorizations(null);
            List<Map<String, Object>> ferpaEntries = new ArrayList<>();

            for (Map<String, Object> auth : allAuths) {
                Long authId = getLong(auth, "id");
                if (authId != null) {
                    try {
                        Map<String, Object> details = transferApiService.getAuthorization(authId);
                        Object ferpaLogs = details.get("ferpaDisclosures");
                        if (ferpaLogs instanceof List<?> logs) {
                            for (Object logEntry : logs) {
                                if (logEntry instanceof Map<?, ?> map) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> entry = (Map<String, Object>) map;
                                    ferpaEntries.add(entry);
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.trace("Skipping FERPA log for auth {}: {}", authId, e.getMessage());
                    }
                }
            }

            Platform.runLater(() -> {
                ferpaList.setAll(ferpaEntries);
                ferpaCountLabel.setText("Total: " + ferpaEntries.size());
            });
        } catch (Exception e) {
            log.warn("Failed to load FERPA logs: {}", e.getMessage());
        }
    }

    // ========================================================================
    // FILTER & SEARCH HANDLERS
    // ========================================================================

    @FXML
    private void handleSearch() {
        applyAuthorizationsFilter();
    }

    @FXML
    private void handleFilter() {
        // Reload if status filter changed
        Thread.ofVirtual().name("hstp-filter").start(this::loadAuthorizations);
        applyAuthorizationsFilter();
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        statusFilter.setValue("All Statuses");
        dateFrom.setValue(null);
        dateTo.setValue(null);
        applyAuthorizationsFilter();
        Thread.ofVirtual().name("hstp-clear-filter").start(this::loadAuthorizations);
    }

    private void applyAuthorizationsFilter() {
        if (filteredAuthorizations == null) return;
        String searchText = searchField.getText() != null ? searchField.getText().toLowerCase() : "";
        LocalDate from = dateFrom.getValue();
        LocalDate to = dateTo.getValue();

        filteredAuthorizations.setPredicate(auth -> {
            // Text search
            if (!searchText.isEmpty()) {
                String authNum = getStr(auth, "authorizationNumber").toLowerCase();
                String dest = getStr(auth, "destinationSchoolName").toLowerCase();
                String reason = getStr(auth, "reasonCode").toLowerCase();
                String created = getStr(auth, "createdBy").toLowerCase();
                if (!authNum.contains(searchText) && !dest.contains(searchText)
                        && !reason.contains(searchText) && !created.contains(searchText)) {
                    return false;
                }
            }

            // Date range filter
            String createdStr = getStr(auth, "createdAt");
            if ((from != null || to != null) && !createdStr.isEmpty()) {
                try {
                    LocalDate createdDate = LocalDate.parse(createdStr.substring(0, 10));
                    if (from != null && createdDate.isBefore(from)) return false;
                    if (to != null && createdDate.isAfter(to)) return false;
                } catch (Exception e) {
                    // Skip date filtering if parse fails
                }
            }
            return true;
        });

        authCountLabel.setText("Total: " + filteredAuthorizations.size());
    }

    // ========================================================================
    // FERPA FILTER HANDLERS
    // ========================================================================

    @FXML
    private void handleFerpaSearch() {
        applyFerpaFilter();
    }

    @FXML
    private void handleFerpaFilter() {
        applyFerpaFilter();
    }

    @FXML
    private void handleClearFerpaFilters() {
        ferpaSearchField.clear();
        ferpaDateFrom.setValue(null);
        ferpaDateTo.setValue(null);
        applyFerpaFilter();
    }

    private void applyFerpaFilter() {
        String searchText = ferpaSearchField.getText() != null ? ferpaSearchField.getText().toLowerCase() : "";
        LocalDate from = ferpaDateFrom.getValue();
        LocalDate to = ferpaDateTo.getValue();

        List<Map<String, Object>> filtered = ferpaList.stream()
                .filter(entry -> {
                    if (!searchText.isEmpty()) {
                        String student = getStr(entry, "studentName").toLowerCase();
                        String recipient = getStr(entry, "recipientOrganization").toLowerCase();
                        String by = getStr(entry, "disclosedBy").toLowerCase();
                        if (!student.contains(searchText) && !recipient.contains(searchText)
                                && !by.contains(searchText)) {
                            return false;
                        }
                    }
                    String dateStr = getStr(entry, "disclosedAt");
                    if ((from != null || to != null) && !dateStr.isEmpty()) {
                        try {
                            LocalDate date = LocalDate.parse(dateStr.substring(0, 10));
                            if (from != null && date.isBefore(from)) return false;
                            if (to != null && date.isAfter(to)) return false;
                        } catch (Exception e) {
                            // skip
                        }
                    }
                    return true;
                })
                .toList();

        ferpaTable.setItems(FXCollections.observableArrayList(filtered));
        ferpaCountLabel.setText("Total: " + filtered.size());
    }

    // ========================================================================
    // ACTION HANDLERS — Header Buttons
    // ========================================================================

    @FXML
    private void handleNewTransfer() {
        openAuthorizationDialog(null, true);
    }

    @FXML
    private void handleRefresh() {
        log.info("Refreshing transfer dashboard...");
        loadAllData();
    }

    @FXML
    private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Transfer Authorizations");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("transfer-authorizations.csv");

        Stage stage = (Stage) authorizationsTable.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                pw.println("Auth #,Status,Students,Destination,Reason,Delivery,Created");
                for (Map<String, Object> auth : authorizationsList) {
                    pw.printf("%s,%s,%s,\"%s\",%s,%s,%s%n",
                            getStr(auth, "authorizationNumber"),
                            getStr(auth, "status"),
                            getStr(auth, "studentCount"),
                            getStr(auth, "destinationSchoolName").replace("\"", "\"\""),
                            getStr(auth, "reasonCode"),
                            getStr(auth, "deliveryMethod"),
                            getStr(auth, "createdAt"));
                }
                showInfo("Export Successful", "Exported " + authorizationsList.size() + " records to:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                log.error("Export failed", e);
                showError("Export failed: " + e.getMessage());
            }
        }
    }

    // ========================================================================
    // ACTION HANDLERS — Key Management
    // ========================================================================

    @FXML
    private void handleGenerateKey() {
        TextInputDialog labelDialog = new TextInputDialog("Default Key");
        labelDialog.setTitle("Generate Signing Key");
        labelDialog.setHeaderText("Generate a new Ed25519 signing key pair");
        labelDialog.setContentText("Key label:");

        Optional<String> labelResult = labelDialog.showAndWait();
        if (labelResult.isEmpty()) return;

        String label = labelResult.get().trim();
        if (label.isEmpty()) label = "Default Key";

        try {
            Long userId = SecurityContext.getCurrentUserId();
            String role = SecurityContext.getCurrentRole().map(Enum::name).orElse("ADMIN");

            Map<String, Object> result = transferApiService.generateKeyPair(userId, role, label);

            String privateKey = getStr(result, "privateKeyBase64");
            String fingerprint = getStr(result, "fingerprint");

            // Store locally if LocalKeyStore is available
            if (localKeyStore != null && !privateKey.isEmpty()) {
                TextInputDialog pwDialog = new TextInputDialog();
                pwDialog.setTitle("Protect Private Key");
                pwDialog.setHeaderText("Enter a password to encrypt your private key locally");
                pwDialog.setContentText("Password:");

                Optional<String> pwResult = pwDialog.showAndWait();
                if (pwResult.isPresent() && !pwResult.get().isEmpty()) {
                    String username = SecurityContext.getCurrentUsername().orElse("unknown");
                    localKeyStore.storeKey(username, fingerprint, privateKey, pwResult.get());
                }
            }

            // Show private key (one-time display)
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Key Generated Successfully");
            alert.setHeaderText("Save your private key — it will NOT be shown again!");
            TextArea keyArea = new TextArea(privateKey);
            keyArea.setEditable(false);
            keyArea.setWrapText(true);
            keyArea.setPrefRowCount(4);
            VBox content = new VBox(10,
                    new Label("Fingerprint: " + fingerprint),
                    new Label("Private Key (Base64):"),
                    keyArea);
            alert.getDialogPane().setContent(content);
            alert.showAndWait();

            // Refresh keys
            Thread.ofVirtual().name("hstp-reload-keys").start(() -> {
                loadMyKeys();
                loadAuthorizers();
            });

        } catch (Exception e) {
            log.error("Key generation failed", e);
            showError("Key generation failed: " + e.getMessage());
        }
    }

    private void handleRevokeKey(Map<String, Object> keyData) {
        Long keyId = getLong(keyData, "keyId");
        String fingerprint = getStr(keyData, "fingerprint");

        if (keyId == null) {
            showError("Cannot determine key ID");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Revoke Key");
        confirm.setHeaderText("Revoke signing key?");
        confirm.setContentText("Fingerprint: " + fingerprint + "\n\nThis action cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                transferApiService.revokeKey(keyId, "Revoked by user");
                showInfo("Key Revoked", "Signing key " + fingerprint + " has been revoked.");
                Thread.ofVirtual().name("hstp-reload-keys").start(() -> {
                    loadMyKeys();
                    loadAuthorizers();
                });
            } catch (Exception e) {
                log.error("Key revocation failed", e);
                showError("Failed to revoke key: " + e.getMessage());
            }
        }
    }

    // ========================================================================
    // DIALOG OPENERS
    // ========================================================================

    private void openAuthorizationDialog(Long authorizationId, boolean createMode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TransferAuthorizationDialog.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            VBox root = loader.load();

            TransferAuthorizationDialogController controller = loader.getController();
            if (createMode) {
                controller.setCreateMode();
            } else if (authorizationId != null) {
                controller.setViewMode(authorizationId);
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(createMode ? "New Transfer Authorization" : "Transfer Authorization Details");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh after dialog closes
            loadAllData();

        } catch (Exception e) {
            log.error("Failed to open authorization dialog", e);
            showError("Failed to open authorization dialog: " + e.getMessage());
        }
    }

    private void openSigningDialog(Long authorizationId) {
        if (authorizationId == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TransferSigningDialog.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            VBox root = loader.load();

            TransferSigningDialogController controller = loader.getController();
            controller.setAuthorization(authorizationId);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Sign Transfer Authorization");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh after signing
            loadAllData();

        } catch (Exception e) {
            log.error("Failed to open signing dialog", e);
            showError("Failed to open signing dialog: " + e.getMessage());
        }
    }

    private void openPackageDialog(Long authorizationId) {
        if (authorizationId == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TransferPackageDialog.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            VBox root = loader.load();

            TransferPackageDialogController controller = loader.getController();
            controller.setAuthorization(authorizationId);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Transfer Package");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh after packaging
            loadAllData();

        } catch (Exception e) {
            log.error("Failed to open package dialog", e);
            showError("Failed to open package dialog: " + e.getMessage());
        }
    }

    // ========================================================================
    // FORMATTING HELPERS
    // ========================================================================

    private String getStr(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    private Long getLong(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number n) return n.longValue();
        if (val instanceof String s) {
            try { return Long.parseLong(s); } catch (NumberFormatException e) { return null; }
        }
        return null;
    }

    private long getNumeric(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number n) return n.longValue();
        return 0L;
    }

    private String formatStatus(String status) {
        if (status == null || status.isEmpty()) return "";
        return switch (status) {
            case "DRAFT" -> "Draft";
            case "PENDING_SIGNATURES" -> "Pending Signatures";
            case "QUORUM_MET" -> "Quorum Met";
            case "READY" -> "Ready";
            case "DELIVERED" -> "Delivered";
            case "COMPLETED" -> "Completed";
            case "REVOKED" -> "Revoked";
            default -> status;
        };
    }

    private String formatReason(String reason) {
        if (reason == null || reason.isEmpty()) return "";
        return switch (reason) {
            case "STUDENT_TRANSFER" -> "Student Transfer";
            case "DISTRICT_MIGRATION" -> "District Migration";
            case "RECORDS_REQUEST" -> "Records Request";
            case "COURT_ORDER" -> "Court Order";
            case "EMERGENCY_TRANSFER" -> "Emergency Transfer";
            case "INTER_DISTRICT" -> "Inter-District";
            default -> reason;
        };
    }

    private String formatDelivery(String method) {
        if (method == null || method.isEmpty()) return "";
        return switch (method) {
            case "USB_ENCRYPTED" -> "USB (Encrypted)";
            case "SECURE_FILE_TRANSFER" -> "Secure File Transfer";
            default -> method;
        };
    }

    private String formatDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) return "";
        try {
            if (dateTimeStr.length() >= 16) {
                return dateTimeStr.substring(0, 16).replace("T", " ");
            }
            return dateTimeStr;
        } catch (Exception e) {
            return dateTimeStr;
        }
    }

    private String getStatusStyle(String status) {
        if (status == null) return "";
        return switch (status) {
            case "Draft" -> "-fx-text-fill: #999;";
            case "Pending Signatures" -> "-fx-text-fill: #ffaa00; -fx-font-weight: bold;";
            case "Quorum Met" -> "-fx-text-fill: #00d4ff; -fx-font-weight: bold;";
            case "Ready" -> "-fx-text-fill: #00ff88; -fx-font-weight: bold;";
            case "Delivered" -> "-fx-text-fill: #8888ff; -fx-font-weight: bold;";
            case "Completed" -> "-fx-text-fill: #00ff88;";
            case "Revoked" -> "-fx-text-fill: #ff4444; -fx-font-weight: bold;";
            default -> "";
        };
    }

    // ========================================================================
    // ALERT HELPERS
    // ========================================================================

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
