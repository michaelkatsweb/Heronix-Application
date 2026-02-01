package com.heronix.ui.controller;

import com.heronix.client.SecureAuditApiService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller for the Security Audit Trail view.
 * Displays decrypted audit logs from Hub instances that were encrypted and sent to the SIS Server.
 * This is the ONLY place these tamper-proof logs can be read.
 */
@Slf4j
@Component
public class SecureAuditViewController {

    @FXML private TableView<Map<String, Object>> auditTable;
    @FXML private TableColumn<Map<String, Object>, String> timestampColumn;
    @FXML private TableColumn<Map<String, Object>, String> hubDeviceColumn;
    @FXML private TableColumn<Map<String, Object>, String> usernameColumn;
    @FXML private TableColumn<Map<String, Object>, String> roleColumn;
    @FXML private TableColumn<Map<String, Object>, String> actionColumn;
    @FXML private TableColumn<Map<String, Object>, String> detailsColumn;
    @FXML private TableColumn<Map<String, Object>, String> severityColumn;

    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private TextField deviceFilter;
    @FXML private Label totalEntriesLabel;
    @FXML private Label pageInfoLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Button prevPageBtn;
    @FXML private Button nextPageBtn;

    @Autowired
    private SecureAuditApiService secureAuditApiService;

    private final ObservableList<Map<String, Object>> entries = FXCollections.observableArrayList();
    private int currentPage = 0;
    private int totalPages = 0;
    private static final int PAGE_SIZE = 50;

    @FXML
    public void initialize() {
        setupTableColumns();
        auditTable.setItems(entries);
        Platform.runLater(this::loadLogs);
    }

    private void setupTableColumns() {
        timestampColumn.setCellValueFactory(cd -> new SimpleStringProperty(str(cd.getValue(), "timestamp")));
        hubDeviceColumn.setCellValueFactory(cd -> new SimpleStringProperty(str(cd.getValue(), "hubDeviceId")));
        usernameColumn.setCellValueFactory(cd -> new SimpleStringProperty(str(cd.getValue(), "username")));
        roleColumn.setCellValueFactory(cd -> new SimpleStringProperty(str(cd.getValue(), "role")));
        actionColumn.setCellValueFactory(cd -> new SimpleStringProperty(str(cd.getValue(), "action")));
        detailsColumn.setCellValueFactory(cd -> new SimpleStringProperty(str(cd.getValue(), "details")));
        severityColumn.setCellValueFactory(cd -> new SimpleStringProperty(str(cd.getValue(), "severity")));

        severityColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item.toUpperCase()) {
                        case "ERROR" -> setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold;");
                        case "WARNING" -> setStyle("-fx-text-fill: #F57C00; -fx-font-weight: bold;");
                        default -> setStyle("");
                    }
                }
            }
        });
    }

    private void loadLogs() {
        setLoading(true);
        new Thread(() -> {
            try {
                Map<String, Object> result = secureAuditApiService.getLogs(currentPage, PAGE_SIZE);
                Platform.runLater(() -> updateTable(result));
            } catch (Exception e) {
                log.error("Failed to load secure audit logs", e);
                Platform.runLater(() -> setLoading(false));
            }
        }).start();
    }

    @SuppressWarnings("unchecked")
    private void updateTable(Map<String, Object> result) {
        if (result == null) {
            setLoading(false);
            return;
        }
        List<Map<String, Object>> content = (List<Map<String, Object>>) result.get("content");
        entries.setAll(content != null ? content : List.of());

        Object totalEl = result.get("totalElements");
        long total = totalEl instanceof Number n ? n.longValue() : 0;
        totalEntriesLabel.setText(String.valueOf(total));

        Object totalPg = result.get("totalPages");
        totalPages = totalPg instanceof Number n ? n.intValue() : 1;

        pageInfoLabel.setText("Page " + (currentPage + 1) + " of " + Math.max(totalPages, 1));
        prevPageBtn.setDisable(currentPage <= 0);
        nextPageBtn.setDisable(currentPage >= totalPages - 1);
        setLoading(false);
    }

    @FXML
    private void handleSearch() {
        currentPage = 0;
        setLoading(true);

        String device = deviceFilter.getText() != null ? deviceFilter.getText().trim() : "";
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();

        new Thread(() -> {
            try {
                Map<String, Object> result;
                if (!device.isEmpty()) {
                    result = secureAuditApiService.searchByDevice(device, currentPage, PAGE_SIZE);
                } else if (from != null && to != null) {
                    result = secureAuditApiService.searchByDateRange(from.toString(), to.toString(), currentPage, PAGE_SIZE);
                } else {
                    result = secureAuditApiService.getLogs(currentPage, PAGE_SIZE);
                }
                Platform.runLater(() -> updateTable(result));
            } catch (Exception e) {
                log.error("Failed to search secure audit logs", e);
                Platform.runLater(() -> setLoading(false));
            }
        }).start();
    }

    @FXML
    private void handleClearFilters() {
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        deviceFilter.clear();
        currentPage = 0;
        loadLogs();
    }

    @FXML
    private void handleRefresh() {
        loadLogs();
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 0) {
            currentPage--;
            handleSearch();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            handleSearch();
        }
    }

    @FXML
    private void handleExportCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Security Audit Logs");
        fileChooser.setInitialFileName("secure-audit-logs.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(auditTable.getScene().getWindow());

        if (file != null) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                pw.println("Timestamp,Hub Device,Username,Role,Action,Details,Severity");
                for (Map<String, Object> entry : entries) {
                    pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                            str(entry, "timestamp"),
                            str(entry, "hubDeviceId"),
                            str(entry, "username"),
                            str(entry, "role"),
                            str(entry, "action"),
                            str(entry, "details").replace("\"", "\"\""),
                            str(entry, "severity"));
                }
                log.info("Exported {} secure audit entries to {}", entries.size(), file.getAbsolutePath());
            } catch (Exception e) {
                log.error("Failed to export secure audit logs", e);
            }
        }
    }

    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
    }

    private String str(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }
}
