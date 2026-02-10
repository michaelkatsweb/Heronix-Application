package com.heronix.ui.controller;

import com.heronix.client.TimeApiService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class TimeAndHRController {

    @Autowired private TimeApiService timeApiService;

    // Dashboard
    @FXML private Label activeStaffLabel;
    @FXML private Label pendingRequestsLabel;
    @FXML private Label expiringDocsLabel;
    @FXML private Label activeLocationsLabel;

    // Time Entries
    @FXML private TextField timeStaffIdField;
    @FXML private DatePicker timeStartDate;
    @FXML private DatePicker timeEndDate;
    @FXML private TableView<Map<String, Object>> timeEntriesTable;
    @FXML private TableColumn<Map<String, Object>, String> teStaffCol;
    @FXML private TableColumn<Map<String, Object>, String> teClockInCol;
    @FXML private TableColumn<Map<String, Object>, String> teClockOutCol;
    @FXML private TableColumn<Map<String, Object>, String> teDurationCol;
    @FXML private TableColumn<Map<String, Object>, String> teMethodCol;
    @FXML private TableColumn<Map<String, Object>, String> teStatusCol;

    // Leave Requests
    @FXML private TableView<Map<String, Object>> leaveRequestsTable;
    @FXML private TableColumn<Map<String, Object>, String> lrStaffCol;
    @FXML private TableColumn<Map<String, Object>, String> lrTypeCol;
    @FXML private TableColumn<Map<String, Object>, String> lrStartCol;
    @FXML private TableColumn<Map<String, Object>, String> lrEndCol;
    @FXML private TableColumn<Map<String, Object>, String> lrDaysCol;
    @FXML private TableColumn<Map<String, Object>, String> lrStatusCol;
    @FXML private TableColumn<Map<String, Object>, String> lrActionsCol;

    // Documents
    @FXML private TextField docStaffIdField;
    @FXML private TableView<Map<String, Object>> documentsTable;
    @FXML private TableColumn<Map<String, Object>, String> docStaffCol;
    @FXML private TableColumn<Map<String, Object>, String> docNameCol;
    @FXML private TableColumn<Map<String, Object>, String> docTypeCol;
    @FXML private TableColumn<Map<String, Object>, String> docVerifiedCol;
    @FXML private TableColumn<Map<String, Object>, String> docExpirationCol;
    @FXML private TableColumn<Map<String, Object>, String> docActionsCol;

    // Access Log
    @FXML private TextField accessStaffIdField;
    @FXML private DatePicker accessDatePicker;
    @FXML private TableView<Map<String, Object>> accessLogTable;
    @FXML private TableColumn<Map<String, Object>, String> alStaffCol;
    @FXML private TableColumn<Map<String, Object>, String> alTimeCol;
    @FXML private TableColumn<Map<String, Object>, String> alTypeCol;
    @FXML private TableColumn<Map<String, Object>, String> alLocationCol;
    @FXML private TableColumn<Map<String, Object>, String> alMethodCol;
    @FXML private TableColumn<Map<String, Object>, String> alGrantedCol;

    @FXML
    public void initialize() {
        setupTimeEntryCols();
        setupLeaveRequestCols();
        setupDocumentCols();
        setupAccessLogCols();

        timeStartDate.setValue(LocalDate.now());
        timeEndDate.setValue(LocalDate.now());
        accessDatePicker.setValue(LocalDate.now());

        handleRefreshDashboard();
    }

    // ========================================================================
    // COLUMN SETUP
    // ========================================================================

    private void setupTimeEntryCols() {
        teStaffCol.setCellValueFactory(d -> toStr(d.getValue(), "staff"));
        teClockInCol.setCellValueFactory(d -> toStr(d.getValue(), "clockInTime"));
        teClockOutCol.setCellValueFactory(d -> toStr(d.getValue(), "clockOutTime"));
        teDurationCol.setCellValueFactory(d -> toStr(d.getValue(), "durationDisplay"));
        teMethodCol.setCellValueFactory(d -> toStr(d.getValue(), "entryMethod"));
        teStatusCol.setCellValueFactory(d -> toStr(d.getValue(), "status"));
    }

    private void setupLeaveRequestCols() {
        lrStaffCol.setCellValueFactory(d -> toStr(d.getValue(), "staff"));
        lrTypeCol.setCellValueFactory(d -> toStr(d.getValue(), "leaveType"));
        lrStartCol.setCellValueFactory(d -> toStr(d.getValue(), "startDate"));
        lrEndCol.setCellValueFactory(d -> toStr(d.getValue(), "endDate"));
        lrDaysCol.setCellValueFactory(d -> toStr(d.getValue(), "totalDays"));
        lrStatusCol.setCellValueFactory(d -> toStr(d.getValue(), "status"));
        lrActionsCol.setCellValueFactory(d -> toStr(d.getValue(), "status"));
    }

    private void setupDocumentCols() {
        docStaffCol.setCellValueFactory(d -> toStr(d.getValue(), "staff"));
        docNameCol.setCellValueFactory(d -> toStr(d.getValue(), "fileName"));
        docTypeCol.setCellValueFactory(d -> toStr(d.getValue(), "documentType"));
        docVerifiedCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().get("verifiedBy") != null ? "Yes" : "No"));
        docExpirationCol.setCellValueFactory(d -> toStr(d.getValue(), "expirationDate"));
        docActionsCol.setCellValueFactory(d -> new SimpleStringProperty(""));
    }

    private void setupAccessLogCols() {
        alStaffCol.setCellValueFactory(d -> toStr(d.getValue(), "staff"));
        alTimeCol.setCellValueFactory(d -> toStr(d.getValue(), "accessTime"));
        alTypeCol.setCellValueFactory(d -> toStr(d.getValue(), "accessType"));
        alLocationCol.setCellValueFactory(d -> {
            Object loc = d.getValue().get("accessLocation");
            String name = loc instanceof Map ? String.valueOf(((Map<?, ?>) loc).getOrDefault("locationName", "")) : "";
            return new SimpleStringProperty(name);
        });
        alMethodCol.setCellValueFactory(d -> toStr(d.getValue(), "verificationMethod"));
        alGrantedCol.setCellValueFactory(d ->
                new SimpleStringProperty(Boolean.TRUE.equals(d.getValue().get("granted")) ? "Yes" : "No"));
    }

    // ========================================================================
    // EVENT HANDLERS
    // ========================================================================

    @FXML
    private void handleRefreshDashboard() {
        try {
            Map<String, Object> dashboard = timeApiService.getDashboard();
            activeStaffLabel.setText(String.valueOf(dashboard.getOrDefault("activeStaff", 0)));
            pendingRequestsLabel.setText(String.valueOf(dashboard.getOrDefault("pendingLeaveRequests", 0)));
            expiringDocsLabel.setText(String.valueOf(dashboard.getOrDefault("expiringDocuments", 0)));
            activeLocationsLabel.setText(String.valueOf(dashboard.getOrDefault("activeLocations", 0)));
        } catch (Exception e) {
            log.warn("Could not refresh dashboard: {}", e.getMessage());
        }
    }

    @FXML
    private void handleSearchEntries() {
        try {
            Long staffId = Long.parseLong(timeStaffIdField.getText());
            LocalDate start = timeStartDate.getValue();
            LocalDate end = timeEndDate.getValue();
            List<Map<String, Object>> entries = timeApiService.getEntries(staffId, start, end);
            timeEntriesTable.setItems(FXCollections.observableArrayList(entries));
        } catch (Exception e) {
            log.warn("Could not search entries: {}", e.getMessage());
        }
    }

    @FXML
    private void handleShowPending() {
        try {
            List<Map<String, Object>> requests = timeApiService.getPendingLeaveRequests();
            leaveRequestsTable.setItems(FXCollections.observableArrayList(requests));
        } catch (Exception e) {
            log.warn("Could not load pending requests: {}", e.getMessage());
        }
    }

    @FXML
    private void handleSearchDocuments() {
        try {
            Long staffId = Long.parseLong(docStaffIdField.getText());
            List<Map<String, Object>> docs = timeApiService.getDocuments(staffId);
            documentsTable.setItems(FXCollections.observableArrayList(docs));
        } catch (Exception e) {
            log.warn("Could not search documents: {}", e.getMessage());
        }
    }

    @FXML
    private void handleSearchAccessLog() {
        try {
            Long staffId = Long.parseLong(accessStaffIdField.getText());
            LocalDate date = accessDatePicker.getValue();
            List<Map<String, Object>> logs = timeApiService.getAccessLog(staffId, date);
            accessLogTable.setItems(FXCollections.observableArrayList(logs));
        } catch (Exception e) {
            log.warn("Could not search access log: {}", e.getMessage());
        }
    }

    // ========================================================================
    // UTILITY
    // ========================================================================

    private SimpleStringProperty toStr(Map<String, Object> map, String key) {
        Object val = map.getOrDefault(key, "");
        if (val instanceof Map) {
            // For nested objects like staff, try to get a display value
            Map<?, ?> nested = (Map<?, ?>) val;
            if (nested.containsKey("fullName")) return new SimpleStringProperty(String.valueOf(nested.get("fullName")));
            if (nested.containsKey("firstName")) {
                return new SimpleStringProperty(nested.get("firstName") + " " + nested.getOrDefault("lastName", ""));
            }
            if (nested.containsKey("id")) return new SimpleStringProperty("ID: " + nested.get("id"));
        }
        return new SimpleStringProperty(String.valueOf(val));
    }
}
