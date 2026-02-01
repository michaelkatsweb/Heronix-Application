package com.heronix.ui.controller;

import com.heronix.model.domain.Teacher;
import com.heronix.repository.TeacherRepository;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class StaffDirectoryManagementController {

    @Autowired
    private TeacherRepository teacherRepository;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> departmentFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TableView<Teacher> staffTableView;
    @FXML private TableColumn<Teacher, String> colEmployeeId;
    @FXML private TableColumn<Teacher, String> colName;
    @FXML private TableColumn<Teacher, String> colRole;
    @FXML private TableColumn<Teacher, String> colDepartment;
    @FXML private TableColumn<Teacher, String> colEmail;
    @FXML private TableColumn<Teacher, String> colPhone;
    @FXML private TableColumn<Teacher, String> colCertification;
    @FXML private TableColumn<Teacher, String> colStatus;
    @FXML private Label totalStaffLabel;
    @FXML private Label statusLabel;

    private ObservableList<Teacher> allStaff = FXCollections.observableArrayList();
    private FilteredList<Teacher> filteredStaff;

    @FXML
    public void initialize() {
        log.info("Initializing StaffDirectoryManagementController");

        // Column cell value factories
        colEmployeeId.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getEmployeeId() != null ? cd.getValue().getEmployeeId() : ""));
        colName.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getName() != null ? cd.getValue().getName() : ""));
        colRole.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getRole() != null ? cd.getValue().getRole().name() : ""));
        colDepartment.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getDepartment() != null ? cd.getValue().getDepartment() : ""));
        colEmail.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getEmail() != null ? cd.getValue().getEmail() : ""));
        colPhone.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getPhoneNumber() != null ? cd.getValue().getPhoneNumber() : ""));
        colCertification.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getCertificationType() != null ? cd.getValue().getCertificationType().name() : ""));
        colStatus.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getActive() ? "Active" : "Inactive"));

        // Filtered list
        filteredStaff = new FilteredList<>(allStaff, p -> true);
        staffTableView.setItems(filteredStaff);

        // Search field listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Status filter
        statusFilter.setItems(FXCollections.observableArrayList("All Statuses", "Active", "Inactive"));
        statusFilter.setValue("All Statuses");
        statusFilter.setOnAction(e -> applyFilters());

        // Load data
        loadStaffData();
    }

    private void applyFilters() {
        String search = searchField.getText() != null ? searchField.getText().toLowerCase().trim() : "";
        String dept = departmentFilter.getValue();
        String status = statusFilter.getValue();

        filteredStaff.setPredicate(t -> {
            // Search filter
            if (!search.isEmpty()) {
                boolean match = (t.getName() != null && t.getName().toLowerCase().contains(search))
                        || (t.getEmployeeId() != null && t.getEmployeeId().toLowerCase().contains(search))
                        || (t.getEmail() != null && t.getEmail().toLowerCase().contains(search))
                        || (t.getDepartment() != null && t.getDepartment().toLowerCase().contains(search));
                if (!match) return false;
            }
            // Department filter
            if (dept != null && !"All Departments".equals(dept)) {
                if (t.getDepartment() == null || !t.getDepartment().equals(dept)) return false;
            }
            // Status filter
            if (status != null && !"All Statuses".equals(status)) {
                if ("Active".equals(status) && !t.getActive()) return false;
                if ("Inactive".equals(status) && t.getActive()) return false;
            }
            return true;
        });

        totalStaffLabel.setText("Total: " + filteredStaff.size());
    }

    private void loadStaffData() {
        statusLabel.setText("Loading staff directory...");
        new Thread(() -> {
            try {
                List<Teacher> teachers = teacherRepository.findAllNonDeleted();
                log.info("Loaded {} staff members", teachers.size());

                // Extract unique departments for filter
                List<String> departments = teachers.stream()
                        .map(Teacher::getDepartment)
                        .filter(d -> d != null && !d.isEmpty())
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList());

                Platform.runLater(() -> {
                    allStaff.setAll(teachers);

                    // Populate department filter
                    ObservableList<String> deptItems = FXCollections.observableArrayList("All Departments");
                    deptItems.addAll(departments);
                    departmentFilter.setItems(deptItems);
                    departmentFilter.setValue("All Departments");
                    departmentFilter.setOnAction(e -> applyFilters());

                    totalStaffLabel.setText("Total: " + teachers.size());
                    statusLabel.setText("Loaded " + teachers.size() + " staff members");
                });
            } catch (Exception e) {
                log.error("Failed to load staff directory", e);
                Platform.runLater(() -> statusLabel.setText("Error loading staff: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleRefresh() {
        loadStaffData();
    }

    @FXML
    private void handleExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Staff Directory");
        fileChooser.setInitialFileName("staff_directory.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        Stage stage = (Stage) staffTableView.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(file), StandardCharsets.UTF_8))) {
                pw.write('\ufeff'); // BOM
                pw.println("Employee ID,Name,Role,Department,Email,Phone,Certification,Status");
                for (Teacher t : filteredStaff) {
                    pw.printf("%s,%s,%s,%s,%s,%s,%s,%s%n",
                            csvEscape(t.getEmployeeId()),
                            csvEscape(t.getName()),
                            t.getRole() != null ? t.getRole().name() : "",
                            csvEscape(t.getDepartment()),
                            csvEscape(t.getEmail()),
                            csvEscape(t.getPhoneNumber()),
                            t.getCertificationType() != null ? t.getCertificationType().name() : "",
                            t.getActive() ? "Active" : "Inactive");
                }
                statusLabel.setText("Exported " + filteredStaff.size() + " records to " + file.getName());
            } catch (Exception e) {
                log.error("Failed to export CSV", e);
                statusLabel.setText("Export failed: " + e.getMessage());
            }
        }
    }

    private String csvEscape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
