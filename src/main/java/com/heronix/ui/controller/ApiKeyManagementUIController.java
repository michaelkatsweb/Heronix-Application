package com.heronix.ui.controller;

import com.heronix.model.domain.ApiKey;
import com.heronix.repository.ApiKeyRepository;
import com.heronix.service.ApiKeyManagementService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyManagementUIController {

    private final ApiKeyManagementService apiKeyManagementService;
    private final ApiKeyRepository apiKeyRepository;

    // Filters
    @FXML private CheckBox activeOnlyToggle;
    @FXML private TextField keySearchField;

    // Summary
    @FXML private Label keyTotalLabel;
    @FXML private Label keyActiveLabel;
    @FXML private Label keyExpiredLabel;
    @FXML private Label keyRequestsLabel;

    // Table
    @FXML private TableView<ApiKey> keyTable;
    @FXML private TableColumn<ApiKey, String> keyNameCol;
    @FXML private TableColumn<ApiKey, String> keyPrefixCol;
    @FXML private TableColumn<ApiKey, String> keyScopesCol;
    @FXML private TableColumn<ApiKey, String> keyActiveCol;
    @FXML private TableColumn<ApiKey, String> keyRateLimitCol;
    @FXML private TableColumn<ApiKey, String> keyCreatedCol;
    @FXML private TableColumn<ApiKey, String> keyExpiresCol;
    @FXML private TableColumn<ApiKey, String> keyLastUsedCol;
    @FXML private TableColumn<ApiKey, String> keyRequestsCol;
    @FXML private TableColumn<ApiKey, Void> keyActionsCol;

    // Analytics
    @FXML private ComboBox<ApiKey> analyticsKeySelector;
    @FXML private Label analyticsTotalRequests;
    @FXML private Label analyticsAvgPerDay;
    @FXML private ProgressBar analyticsRateLimitBar;
    @FXML private Label analyticsRateLimitLabel;

    private ObservableList<ApiKey> allKeys = FXCollections.observableArrayList();
    private ObservableList<ApiKey> filteredKeys = FXCollections.observableArrayList();

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final String ADMIN_USER = "admin";

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        loadData();
    }

    private void setupTable() {
        keyNameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        keyPrefixCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getKeyPrefix()));
        keyPrefixCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle("-fx-font-family: 'Consolas', monospace; -fx-text-fill: #64748b;");
            }
        });
        keyScopesCol.setCellValueFactory(cell -> {
            Set<String> scopes = cell.getValue().getScopes();
            return new SimpleStringProperty(scopes != null ? String.join(", ", scopes) : "");
        });
        keyActiveCol.setCellValueFactory(cell -> new SimpleStringProperty(Boolean.TRUE.equals(cell.getValue().getActive()) ? "Yes" : "No"));
        keyActiveCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle("Yes".equals(item)
                    ? "-fx-text-fill: #10b981; -fx-font-weight: bold;"
                    : "-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            }
        });
        keyRateLimitCol.setCellValueFactory(cell -> {
            Integer rl = cell.getValue().getRateLimit();
            return new SimpleStringProperty(rl != null ? String.valueOf(rl) : "N/A");
        });
        keyCreatedCol.setCellValueFactory(cell -> {
            LocalDateTime dt = cell.getValue().getCreatedAt();
            return new SimpleStringProperty(dt != null ? dt.format(DT_FMT) : "");
        });
        keyExpiresCol.setCellValueFactory(cell -> {
            LocalDateTime dt = cell.getValue().getExpiresAt();
            return new SimpleStringProperty(dt != null ? dt.format(DT_FMT) : "Never");
        });
        keyLastUsedCol.setCellValueFactory(cell -> {
            LocalDateTime dt = cell.getValue().getLastUsedAt();
            return new SimpleStringProperty(dt != null ? dt.format(DT_FMT) : "Never");
        });
        keyRequestsCol.setCellValueFactory(cell -> {
            Long count = cell.getValue().getRequestCount();
            return new SimpleStringProperty(count != null ? String.format("%,d", count) : "0");
        });

        setupActionsColumn();
        keyTable.setItems(filteredKeys);
    }

    private void setupActionsColumn() {
        keyActionsCol.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                ApiKey key = getTableRow().getItem();
                if (key == null) { setGraphic(null); return; }

                HBox buttons = new HBox(4);
                buttons.setAlignment(Pos.CENTER);

                Button editBtn = new Button("Edit");
                editBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-padding: 2 8; -fx-font-size: 11; -fx-background-radius: 4; -fx-cursor: hand;");
                editBtn.setOnAction(e -> handleEditKey(key));
                buttons.getChildren().add(editBtn);

                if (Boolean.TRUE.equals(key.getActive())) {
                    Button rotateBtn = new Button("Rotate");
                    rotateBtn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-padding: 2 8; -fx-font-size: 11; -fx-background-radius: 4; -fx-cursor: hand;");
                    rotateBtn.setOnAction(e -> handleRotateKey(key));

                    Button revokeBtn = new Button("Revoke");
                    revokeBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-padding: 2 8; -fx-font-size: 11; -fx-background-radius: 4; -fx-cursor: hand;");
                    revokeBtn.setOnAction(e -> handleRevokeKey(key));

                    buttons.getChildren().addAll(rotateBtn, revokeBtn);
                }

                Button deleteBtn = new Button("Delete");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 2 8; -fx-font-size: 11; -fx-background-radius: 4; -fx-cursor: hand;");
                deleteBtn.setOnAction(e -> handleDeleteKey(key));
                buttons.getChildren().add(deleteBtn);

                setGraphic(buttons);
            }
        });
    }

    private void setupFilters() {
        activeOnlyToggle.selectedProperty().addListener((obs, o, n) -> applyFilters());
        keySearchField.textProperty().addListener((obs, o, n) -> applyFilters());
    }

    private void loadData() {
        new Thread(() -> {
            try {
                List<ApiKey> keys = apiKeyRepository.findAll();
                Platform.runLater(() -> {
                    allKeys.setAll(keys);
                    applyFilters();
                    updateAnalyticsSelector();
                });
            } catch (Exception e) {
                log.error("Failed to load API keys", e);
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Failed to load API keys: " + e.getMessage()));
            }
        }).start();
    }

    private void applyFilters() {
        boolean activeOnly = activeOnlyToggle.isSelected();
        String search = keySearchField.getText() != null ? keySearchField.getText().toLowerCase() : "";

        filteredKeys.setAll(allKeys.stream().filter(k -> {
            if (activeOnly && !Boolean.TRUE.equals(k.getActive())) return false;
            if (!search.isEmpty()) {
                String name = k.getName() != null ? k.getName().toLowerCase() : "";
                String prefix = k.getKeyPrefix() != null ? k.getKeyPrefix().toLowerCase() : "";
                if (!name.contains(search) && !prefix.contains(search)) return false;
            }
            return true;
        }).collect(Collectors.toList()));

        updateSummary();
    }

    private void updateSummary() {
        keyTotalLabel.setText(String.valueOf(filteredKeys.size()));
        keyActiveLabel.setText(String.valueOf(filteredKeys.stream().filter(k -> Boolean.TRUE.equals(k.getActive())).count()));
        keyExpiredLabel.setText(String.valueOf(filteredKeys.stream().filter(k -> k.isExpired()).count()));
        long totalRequests = filteredKeys.stream().mapToLong(k -> k.getRequestCount() != null ? k.getRequestCount() : 0).sum();
        keyRequestsLabel.setText(String.format("%,d", totalRequests));
    }

    private void updateAnalyticsSelector() {
        analyticsKeySelector.setItems(allKeys);
        analyticsKeySelector.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ApiKey k, boolean empty) {
                super.updateItem(k, empty);
                setText(empty || k == null ? "" : k.getName() + " (" + k.getKeyPrefix() + ")");
            }
        });
        analyticsKeySelector.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ApiKey k, boolean empty) {
                super.updateItem(k, empty);
                setText(empty || k == null ? "" : k.getName() + " (" + k.getKeyPrefix() + ")");
            }
        });
    }

    // ========================================================================
    // KEY GENERATION & ONE-TIME DISPLAY
    // ========================================================================

    @FXML
    private void handleGenerateKey() {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Generate New API Key");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(500);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        TextArea descField = new TextArea();
        descField.setPrefRowCount(2);

        // Scopes checkboxes
        String[] availableScopes = {"read", "write", "admin", "students:read", "students:write",
                "teachers:read", "teachers:write", "courses:read", "reports:read"};
        VBox scopesBox = new VBox(4);
        List<CheckBox> scopeCheckboxes = new ArrayList<>();
        for (String scope : availableScopes) {
            CheckBox cb = new CheckBox(scope);
            scopeCheckboxes.add(cb);
            scopesBox.getChildren().add(cb);
        }

        Spinner<Integer> rateLimitSpinner = new Spinner<>(100, 100000, 1000, 100);
        rateLimitSpinner.setEditable(true);
        Spinner<Integer> expirationSpinner = new Spinner<>(0, 3650, 365, 30);
        expirationSpinner.setEditable(true);
        TextArea ipWhitelistArea = new TextArea();
        ipWhitelistArea.setPromptText("One IP per line (leave empty for no restriction)");
        ipWhitelistArea.setPrefRowCount(3);

        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Description:"), 0, row); grid.add(descField, 1, row++);
        grid.add(new Label("Scopes:"), 0, row); grid.add(scopesBox, 1, row++);
        grid.add(new Label("Rate Limit:"), 0, row); grid.add(rateLimitSpinner, 1, row++);
        grid.add(new Label("Expires In (days):"), 0, row); grid.add(expirationSpinner, 1, row++);
        grid.add(new Label("IP Whitelist:"), 0, row); grid.add(ipWhitelistArea, 1, row++);

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        dialog.getDialogPane().setContent(scrollPane);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                Map<String, Object> params = new HashMap<>();
                params.put("name", nameField.getText());
                params.put("description", descField.getText());
                Set<String> scopes = scopeCheckboxes.stream()
                        .filter(CheckBox::isSelected)
                        .map(CheckBox::getText)
                        .collect(Collectors.toSet());
                params.put("scopes", scopes);
                params.put("rateLimit", rateLimitSpinner.getValue());
                params.put("expiresInDays", expirationSpinner.getValue());
                Set<String> ips = new HashSet<>();
                if (ipWhitelistArea.getText() != null && !ipWhitelistArea.getText().isBlank()) {
                    Arrays.stream(ipWhitelistArea.getText().split("\n"))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .forEach(ips::add);
                }
                params.put("ipWhitelist", ips);
                return params;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(params -> {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> result = apiKeyManagementService.createApiKey(
                        (String) params.get("name"),
                        (String) params.get("description"),
                        ADMIN_USER,
                        (Set<String>) params.get("scopes"),
                        (Integer) params.get("expiresInDays"),
                        (Integer) params.get("rateLimit"),
                        (Set<String>) params.get("ipWhitelist")
                );
                String plainKey = (String) result.get("key");
                showOneTimeKeyDialog(plainKey);
                loadData();
            } catch (Exception e) {
                log.error("Failed to generate API key", e);
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to generate key: " + e.getMessage());
            }
        });
    }

    private void showOneTimeKeyDialog(String plainKey) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("API Key Generated");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().setPrefWidth(550);

        VBox content = new VBox(12);
        content.setPadding(new Insets(20));

        Label warning = new Label("This key will only be shown once. Copy it now!");
        warning.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 14;");

        TextField keyField = new TextField(plainKey);
        keyField.setEditable(false);
        keyField.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 13; -fx-background-color: #f1f5f9; -fx-border-color: #e2e8f0; -fx-padding: 8;");

        Button copyBtn = new Button("Copy to Clipboard");
        copyBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");
        copyBtn.setOnAction(e -> {
            ClipboardContent cc = new ClipboardContent();
            cc.putString(plainKey);
            Clipboard.getSystemClipboard().setContent(cc);
            copyBtn.setText("Copied!");
            copyBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6;");
        });

        content.getChildren().addAll(warning, keyField, copyBtn);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    // ========================================================================
    // KEY ACTIONS
    // ========================================================================

    private void handleEditKey(ApiKey key) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit API Key");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField(key.getName());
        TextArea descField = new TextArea(key.getDescription());
        descField.setPrefRowCount(2);
        Spinner<Integer> rateLimitSpinner = new Spinner<>(100, 100000, key.getRateLimit() != null ? key.getRateLimit() : 1000, 100);
        rateLimitSpinner.setEditable(true);
        TextArea ipWhitelistArea = new TextArea();
        if (key.getIpWhitelist() != null) {
            ipWhitelistArea.setText(String.join("\n", key.getIpWhitelist()));
        }
        ipWhitelistArea.setPrefRowCount(3);

        // Show scopes read-only
        Label scopesLabel = new Label(key.getScopes() != null ? String.join(", ", key.getScopes()) : "None");
        scopesLabel.setStyle("-fx-text-fill: #64748b;");

        int row = 0;
        grid.add(new Label("Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Description:"), 0, row); grid.add(descField, 1, row++);
        grid.add(new Label("Scopes:"), 0, row); grid.add(scopesLabel, 1, row++);
        grid.add(new Label("Rate Limit:"), 0, row); grid.add(rateLimitSpinner, 1, row++);
        grid.add(new Label("IP Whitelist:"), 0, row); grid.add(ipWhitelistArea, 1, row++);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    Set<String> ips = new HashSet<>();
                    if (ipWhitelistArea.getText() != null && !ipWhitelistArea.getText().isBlank()) {
                        Arrays.stream(ipWhitelistArea.getText().split("\n"))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .forEach(ips::add);
                    }
                    apiKeyManagementService.updateApiKey(
                            key.getId(), key.getUserId(),
                            nameField.getText(), descField.getText(),
                            null, // scopes not editable
                            rateLimitSpinner.getValue(), ips
                    );
                    loadData();
                } catch (Exception e) {
                    log.error("Failed to update API key", e);
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Failed to update: " + e.getMessage()));
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void handleRotateKey(ApiKey key) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Rotate key \"" + key.getName() + "\"? The old key will be revoked and a new one generated.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Rotate");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    Map<String, Object> result = apiKeyManagementService.rotateApiKey(key.getId(), key.getUserId());
                    String newPlainKey = (String) result.get("key");
                    showOneTimeKeyDialog(newPlainKey);
                    loadData();
                } catch (Exception e) {
                    log.error("Failed to rotate API key", e);
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to rotate: " + e.getMessage());
                }
            }
        });
    }

    private void handleRevokeKey(ApiKey key) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Revoke key \"" + key.getName() + "\"? This cannot be undone.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Revoke");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    apiKeyManagementService.revokeApiKey(key.getId(), key.getUserId());
                    loadData();
                } catch (Exception e) {
                    log.error("Failed to revoke API key", e);
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to revoke: " + e.getMessage());
                }
            }
        });
    }

    private void handleDeleteKey(ApiKey key) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Permanently delete key \"" + key.getName() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    apiKeyManagementService.deleteApiKey(key.getId(), key.getUserId());
                    loadData();
                } catch (Exception e) {
                    log.error("Failed to delete API key", e);
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete: " + e.getMessage());
                }
            }
        });
    }

    // ========================================================================
    // ANALYTICS
    // ========================================================================

    @FXML
    private void handleLoadAnalytics() {
        loadUsageAnalytics();
    }

    private void loadUsageAnalytics() {
        ApiKey selected = analyticsKeySelector.getValue();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Key Selected", "Please select an API key to view analytics.");
            return;
        }

        new Thread(() -> {
            try {
                Map<String, Object> usage = apiKeyManagementService.getApiKeyUsage(selected.getId(), selected.getUserId());
                Platform.runLater(() -> {
                    Long totalReqs = (Long) usage.get("totalRequests");
                    analyticsTotalRequests.setText(totalReqs != null ? String.format("%,d", totalReqs) : "0");

                    // Calculate avg per day
                    LocalDateTime created = selected.getCreatedAt();
                    if (created != null && totalReqs != null) {
                        long daysSince = ChronoUnit.DAYS.between(created, LocalDateTime.now());
                        if (daysSince > 0) {
                            analyticsAvgPerDay.setText(String.format("%.1f", (double) totalReqs / daysSince));
                        } else {
                            analyticsAvgPerDay.setText(String.valueOf(totalReqs));
                        }
                    }

                    // Rate limit usage (simplified estimate)
                    Integer rateLimit = selected.getRateLimit();
                    if (rateLimit != null && rateLimit > 0 && totalReqs != null) {
                        double pct = Math.min(1.0, (double) totalReqs / rateLimit);
                        analyticsRateLimitBar.setProgress(pct);
                        analyticsRateLimitLabel.setText(String.format("%,d / %,d (%.1f%%)", totalReqs, rateLimit, pct * 100));
                    } else {
                        analyticsRateLimitBar.setProgress(0);
                        analyticsRateLimitLabel.setText("N/A");
                    }
                });
            } catch (Exception e) {
                log.error("Failed to load analytics", e);
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Failed to load analytics: " + e.getMessage()));
            }
        }).start();
    }

    // ========================================================================
    // UTILITIES
    // ========================================================================

    @FXML
    private void handleClearFilters() {
        activeOnlyToggle.setSelected(true);
        keySearchField.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
