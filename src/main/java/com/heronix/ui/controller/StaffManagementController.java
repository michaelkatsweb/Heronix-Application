package com.heronix.ui.controller;

import com.heronix.model.domain.Staff;
import com.heronix.model.enums.StaffOccupation;
import com.heronix.service.StaffService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller for Staff Management view
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 59 - Staff/Teacher Separation
 */
@Slf4j
@Controller
public class StaffManagementController implements Initializable {

    @Autowired
    private StaffService staffService;

    @Autowired
    private ApplicationContext springContext;

    // Filter controls
    @FXML private ComboBox<String> occupationFilter;
    @FXML private ComboBox<String> departmentFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TextField searchField;

    // Quick stats
    @FXML private Label totalStaffLabel;
    @FXML private Label paraCountLabel;
    @FXML private Label adminCountLabel;
    @FXML private Label servicesCountLabel;
    @FXML private Label expiringCountLabel;

    // Table
    @FXML private TableView<Staff> staffTable;
    @FXML private TableColumn<Staff, String> employeeIdCol;
    @FXML private TableColumn<Staff, String> nameCol;
    @FXML private TableColumn<Staff, String> occupationCol;
    @FXML private TableColumn<Staff, String> departmentCol;
    @FXML private TableColumn<Staff, String> emailCol;
    @FXML private TableColumn<Staff, String> phoneCol;
    @FXML private TableColumn<Staff, String> hireDateCol;
    @FXML private TableColumn<Staff, String> statusCol;
    @FXML private TableColumn<Staff, String> complianceCol;
    @FXML private TableColumn<Staff, Void> actionsCol;

    // Pagination
    @FXML private Button prevPageBtn;
    @FXML private Button nextPageBtn;
    @FXML private Label pageInfoLabel;
    @FXML private ComboBox<Integer> pageSizeCombo;
    @FXML private Label totalItemsLabel;

    // Status
    @FXML private Label statusLabel;
    @FXML private Label lastUpdatedLabel;

    // Buttons
    @FXML private Button addButton;
    @FXML private Button exportButton;

    private ObservableList<Staff> allStaff = FXCollections.observableArrayList();
    private ObservableList<Staff> filteredStaff = FXCollections.observableArrayList();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private int currentPage = 1;
    private int pageSize = 25;
    private int totalPages = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("Initializing Staff Management Controller");

        setupFilters();
        setupTable();
        setupPagination();
        loadStaffData();
    }

    private void setupFilters() {
        // Occupation filter - grouped by category
        List<String> occupationOptions = new ArrayList<>();
        occupationOptions.add("All Occupations");
        for (StaffOccupation occ : StaffOccupation.values()) {
            occupationOptions.add(occ.getDisplayName());
        }
        occupationFilter.setItems(FXCollections.observableArrayList(occupationOptions));
        occupationFilter.getSelectionModel().selectFirst();
        occupationFilter.setOnAction(e -> applyFilters());

        // Status filter
        statusFilter.setItems(FXCollections.observableArrayList("All", "Active", "Inactive"));
        statusFilter.getSelectionModel().selectFirst();
        statusFilter.setOnAction(e -> applyFilters());

        // Search field
        searchField.setOnAction(e -> applyFilters());
    }

    private void setupTable() {
        // Employee ID
        employeeIdCol.setCellValueFactory(new PropertyValueFactory<>("employeeId"));

        // Name
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFullName()));

        // Occupation
        occupationCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOccupationDisplay()));

        // Department
        departmentCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDepartment() != null ? data.getValue().getDepartment() : "-"));

        // Email
        emailCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getEmail() != null ? data.getValue().getEmail() : "-"));

        // Phone
        phoneCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getPhoneNumber() != null ? data.getValue().getPhoneNumber() : "-"));

        // Hire Date
        hireDateCol.setCellValueFactory(data -> {
            LocalDate hireDate = data.getValue().getHireDate();
            return new SimpleStringProperty(hireDate != null ?
                    hireDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : "-");
        });

        // Status
        statusCol.setCellValueFactory(data ->
                new SimpleStringProperty(Boolean.TRUE.equals(data.getValue().getActive()) ? "Active" : "Inactive"));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Active".equals(item)) {
                        setStyle("-fx-text-fill: #10B981;");
                    } else {
                        setStyle("-fx-text-fill: #EF4444;");
                    }
                }
            }
        });

        // Compliance
        complianceCol.setCellValueFactory(data -> {
            Staff staff = data.getValue();
            if (staff.isBackgroundCheckExpired()) {
                return new SimpleStringProperty("Expired");
            } else if (staff.hasBackgroundCheckExpiring(90)) {
                return new SimpleStringProperty("Expiring");
            }
            return new SimpleStringProperty("Valid");
        });
        complianceCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "Expired" -> setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
                        case "Expiring" -> setStyle("-fx-text-fill: #F59E0B;");
                        default -> setStyle("-fx-text-fill: #10B981;");
                    }
                }
            }
        });

        // Actions column
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox hbox = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("button-link");
                deleteBtn.getStyleClass().add("button-link-danger");

                editBtn.setOnAction(e -> {
                    Staff staff = getTableView().getItems().get(getIndex());
                    handleEditStaff(staff);
                });

                deleteBtn.setOnAction(e -> {
                    Staff staff = getTableView().getItems().get(getIndex());
                    handleDeleteStaff(staff);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });

        staffTable.setItems(filteredStaff);
    }

    private void setupPagination() {
        pageSizeCombo.setItems(FXCollections.observableArrayList(10, 25, 50, 100));
        pageSizeCombo.getSelectionModel().select(Integer.valueOf(25));
        pageSizeCombo.setOnAction(e -> {
            pageSize = pageSizeCombo.getValue();
            currentPage = 1;
            updateTableView();
        });
    }

    private void loadStaffData() {
        updateStatus("Loading staff data...");

        Task<List<Staff>> loadTask = new Task<>() {
            @Override
            protected List<Staff> call() throws Exception {
                return staffService.getAllActiveStaff();
            }
        };

        loadTask.setOnSucceeded(e -> {
            List<Staff> result = loadTask.getValue();
            allStaff.setAll(result);

            // Load departments for filter
            loadDepartmentFilter();

            // Update quick stats
            updateQuickStats();

            // Apply filters and update view
            applyFilters();

            updateStatus("Loaded " + result.size() + " staff members");
            updateLastUpdated();
        });

        loadTask.setOnFailed(e -> {
            log.error("Error loading staff data", loadTask.getException());
            updateStatus("Error loading data: " + loadTask.getException().getMessage());
        });

        executorService.submit(loadTask);
    }

    private void loadDepartmentFilter() {
        Set<String> departments = new TreeSet<>();
        departments.add("All Departments");
        for (Staff staff : allStaff) {
            if (staff.getDepartment() != null && !staff.getDepartment().isEmpty()) {
                departments.add(staff.getDepartment());
            }
        }
        Platform.runLater(() -> {
            departmentFilter.setItems(FXCollections.observableArrayList(departments));
            departmentFilter.getSelectionModel().selectFirst();
            departmentFilter.setOnAction(e -> applyFilters());
        });
    }

    private void updateQuickStats() {
        long total = allStaff.size();
        long paras = allStaff.stream().filter(Staff::isParaprofessional).count();
        long admin = allStaff.stream().filter(Staff::isAdministrative).count();
        long services = allStaff.stream().filter(Staff::isStudentServices).count();
        long expiring = allStaff.stream().filter(s -> s.hasBackgroundCheckExpiring(90)).count();

        Platform.runLater(() -> {
            totalStaffLabel.setText(String.valueOf(total));
            paraCountLabel.setText(String.valueOf(paras));
            adminCountLabel.setText(String.valueOf(admin));
            servicesCountLabel.setText(String.valueOf(services));
            expiringCountLabel.setText(String.valueOf(expiring));
        });
    }

    private void applyFilters() {
        String occupationSelection = occupationFilter.getValue();
        String departmentSelection = departmentFilter.getValue();
        String statusSelection = statusFilter.getValue();
        String searchText = searchField.getText() != null ? searchField.getText().toLowerCase().trim() : "";

        List<Staff> filtered = allStaff.stream()
                .filter(staff -> {
                    // Occupation filter
                    if (occupationSelection != null && !"All Occupations".equals(occupationSelection)) {
                        if (!occupationSelection.equals(staff.getOccupationDisplay())) {
                            return false;
                        }
                    }

                    // Department filter
                    if (departmentSelection != null && !"All Departments".equals(departmentSelection)) {
                        if (staff.getDepartment() == null || !departmentSelection.equals(staff.getDepartment())) {
                            return false;
                        }
                    }

                    // Status filter
                    if (statusSelection != null && !"All".equals(statusSelection)) {
                        boolean isActive = "Active".equals(statusSelection);
                        if (!isActive == Boolean.TRUE.equals(staff.getActive())) {
                            return false;
                        }
                    }

                    // Search filter
                    if (!searchText.isEmpty()) {
                        String fullName = staff.getFullName().toLowerCase();
                        String empId = staff.getEmployeeId() != null ? staff.getEmployeeId().toLowerCase() : "";
                        if (!fullName.contains(searchText) && !empId.contains(searchText)) {
                            return false;
                        }
                    }

                    return true;
                })
                .toList();

        currentPage = 1;
        filteredStaff.setAll(filtered);
        updateTableView();
    }

    private void updateTableView() {
        int totalItems = filteredStaff.size();
        totalPages = (int) Math.ceil((double) totalItems / pageSize);
        if (totalPages == 0) totalPages = 1;

        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);

        List<Staff> pageItems = fromIndex < totalItems ?
                filteredStaff.subList(fromIndex, toIndex) : List.of();

        staffTable.setItems(FXCollections.observableArrayList(pageItems));

        // Update pagination controls
        pageInfoLabel.setText(String.format("Page %d of %d", currentPage, totalPages));
        totalItemsLabel.setText(totalItems + " items");
        prevPageBtn.setDisable(currentPage <= 1);
        nextPageBtn.setDisable(currentPage >= totalPages);
    }

    @FXML
    private void handleSearch() {
        applyFilters();
    }

    @FXML
    private void handleClearFilters() {
        occupationFilter.getSelectionModel().selectFirst();
        departmentFilter.getSelectionModel().selectFirst();
        statusFilter.getSelectionModel().selectFirst();
        searchField.clear();
        applyFilters();
    }

    @FXML
    private void handleAddStaff() {
        showStaffFormDialog(null);
    }

    private void handleEditStaff(Staff staff) {
        showStaffFormDialog(staff);
    }

    private void showStaffFormDialog(Staff staff) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/StaffFormDialog.fxml"));
            loader.setControllerFactory(springContext::getBean);
            DialogPane dialogPane = loader.load();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle(staff == null ? "Add New Staff Member" : "Edit Staff Member");

            // Get controller and set data
            Object controller = loader.getController();
            if (controller instanceof com.heronix.ui.controller.dialogs.StaffFormDialogController formController) {
                if (staff != null) {
                    formController.setStaff(staff);
                }
            }

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Reload data
                loadStaffData();
            }
        } catch (IOException e) {
            log.error("Error showing staff form dialog", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open staff form: " + e.getMessage());
        }
    }

    private void handleDeleteStaff(Staff staff) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Staff Member");
        confirm.setContentText("Are you sure you want to delete " + staff.getFullName() + "?\n" +
                "This will soft-delete the record (it can be restored later).");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                staffService.softDeleteStaff(staff.getId(), "admin"); // TODO: Get current user
                loadStaffData();
                updateStatus("Staff member deleted: " + staff.getFullName());
            } catch (Exception e) {
                log.error("Error deleting staff", e);
                showAlert(Alert.AlertType.ERROR, "Error", "Could not delete staff: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleExport() {
        updateStatus("Export functionality coming soon...");
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            updateTableView();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            updateTableView();
        }
    }

    private void updateStatus(String message) {
        Platform.runLater(() -> {
            if (statusLabel != null) {
                statusLabel.setText(message);
            }
        });
    }

    private void updateLastUpdated() {
        Platform.runLater(() -> {
            if (lastUpdatedLabel != null) {
                String now = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
                lastUpdatedLabel.setText("Last updated: " + now);
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    public void cleanup() {
        executorService.shutdown();
    }
}
