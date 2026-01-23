package com.heronix.ui.controller;

import com.heronix.model.domain.IncompleteRegistration;
import com.heronix.model.domain.IncompleteRegistration.IncompleteReason;
import com.heronix.model.domain.IncompleteRegistration.Priority;
import com.heronix.model.enums.RegistrationStatus;
import com.heronix.repository.IncompleteRegistrationRepository;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Incomplete Registrations Controller
 *
 * Provides comprehensive UI for managing incomplete student registrations:
 * - View all incomplete registrations in table
 * - Filter by status, reason, priority, overdue
 * - Record follow-ups and notes
 * - Mark registrations as complete
 * - View statistics and alerts
 *
 * Used by administrators and registrars to track and resolve
 * incomplete student registrations.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
@Component
public class IncompleteRegistrationsController {

    @Autowired(required = false)
    private IncompleteRegistrationRepository registrationRepository;

    // ========================================================================
    // FXML COMPONENTS - Table and Columns
    // ========================================================================

    @FXML
    private TableView<IncompleteRegistration> registrationsTable;

    @FXML
    private TableColumn<IncompleteRegistration, Boolean> selectColumn;

    @FXML
    private TableColumn<IncompleteRegistration, String> studentNameColumn;

    @FXML
    private TableColumn<IncompleteRegistration, String> gradeLevelColumn;

    @FXML
    private TableColumn<IncompleteRegistration, String> guardianColumn;

    @FXML
    private TableColumn<IncompleteRegistration, String> phoneColumn;

    @FXML
    private TableColumn<IncompleteRegistration, String> statusColumn;

    @FXML
    private TableColumn<IncompleteRegistration, String> reasonColumn;

    @FXML
    private TableColumn<IncompleteRegistration, String> missingDocsColumn;

    @FXML
    private TableColumn<IncompleteRegistration, String> daysIncompleteColumn;

    @FXML
    private TableColumn<IncompleteRegistration, String> nextFollowupColumn;

    @FXML
    private TableColumn<IncompleteRegistration, String> priorityColumn;

    @FXML
    private TableColumn<IncompleteRegistration, String> assignedToColumn;

    // ========================================================================
    // FXML COMPONENTS - Search and Filters
    // ========================================================================

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> statusFilterCombo;

    @FXML
    private ComboBox<String> reasonFilterCombo;

    @FXML
    private ComboBox<String> priorityFilterCombo;

    @FXML
    private CheckBox overdueOnlyCheckbox;

    @FXML
    private CheckBox followupDueCheckbox;

    // ========================================================================
    // FXML COMPONENTS - Statistics
    // ========================================================================

    @FXML
    private Label totalIncompleteLabel;

    @FXML
    private Label overdueCountLabel;

    @FXML
    private Label followupDueLabel;

    @FXML
    private Label urgentCountLabel;

    @FXML
    private Label missingDocsCountLabel;

    @FXML
    private Label missingPhotoCountLabel;

    // ========================================================================
    // FXML COMPONENTS - Details Panel
    // ========================================================================

    @FXML
    private VBox detailsPanel;

    @FXML
    private Label detailStudentName;

    @FXML
    private Label detailGradeLevel;

    @FXML
    private Label detailGuardianInfo;

    @FXML
    private Label detailStatus;

    @FXML
    private Label detailMissingDocs;

    @FXML
    private Label detailDaysIncomplete;

    @FXML
    private TextArea detailFollowupNotes;

    // ========================================================================
    // FXML COMPONENTS - Buttons
    // ========================================================================

    @FXML
    private Button refreshButton;

    @FXML
    private Button recordFollowupButton;

    @FXML
    private Button markCompleteButton;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button exportButton;

    @FXML
    private Button clearFiltersButton;

    // ========================================================================
    // STATE
    // ========================================================================

    private ObservableList<IncompleteRegistration> registrationsList;
    private ObservableList<IncompleteRegistration> filteredList;
    private Set<Long> selectedIds = new HashSet<>();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    @FXML
    public void initialize() {
        log.info("Initializing Incomplete Registrations Controller");

        setupTable();
        setupFilters();
        setupButtons();
        setupSelectionListener();
        loadData();

        log.info("Incomplete Registrations Controller initialized");
    }

    /**
     * Setup table columns
     */
    private void setupTable() {
        // Selection column with checkboxes
        selectColumn.setCellValueFactory(cellData -> {
            IncompleteRegistration reg = cellData.getValue();
            SimpleBooleanProperty property = new SimpleBooleanProperty(
                selectedIds.contains(reg.getId())
            );
            property.addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    selectedIds.add(reg.getId());
                } else {
                    selectedIds.remove(reg.getId());
                }
                updateButtonStates();
            });
            return property;
        });
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
        selectColumn.setEditable(true);

        // Student name column
        studentNameColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getStudentFullName()));

        // Grade level column
        gradeLevelColumn.setCellValueFactory(cellData -> {
            String grade = cellData.getValue().getGradeLevel();
            return new SimpleStringProperty(grade != null ? grade : "N/A");
        });

        // Guardian column
        guardianColumn.setCellValueFactory(cellData -> {
            String guardian = cellData.getValue().getGuardianName();
            return new SimpleStringProperty(guardian != null ? guardian : "N/A");
        });

        // Phone column
        phoneColumn.setCellValueFactory(cellData -> {
            String phone = cellData.getValue().getGuardianPhone();
            return new SimpleStringProperty(phone != null ? phone : "N/A");
        });

        // Status column with color styling
        statusColumn.setCellValueFactory(cellData -> {
            IncompleteRegistration reg = cellData.getValue();
            String status = reg.getStatusDisplay();
            return new SimpleStringProperty(status);
        });
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.startsWith("OVERDUE")) {
                        setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        // Reason column
        reasonColumn.setCellValueFactory(cellData -> {
            IncompleteReason reason = cellData.getValue().getIncompleteReason();
            return new SimpleStringProperty(reason != null ? reason.getDisplayName() : "N/A");
        });

        // Missing docs column
        missingDocsColumn.setCellValueFactory(cellData -> {
            List<String> missing = cellData.getValue().getMissingDocumentList();
            String display = missing.isEmpty() ? "None" : String.join(", ", missing);
            return new SimpleStringProperty(display);
        });

        // Days incomplete column
        daysIncompleteColumn.setCellValueFactory(cellData -> {
            long days = cellData.getValue().getDaysSinceIncomplete();
            return new SimpleStringProperty(days + " days");
        });

        // Next follow-up column
        nextFollowupColumn.setCellValueFactory(cellData -> {
            LocalDate nextDate = cellData.getValue().getNextFollowupDate();
            if (nextDate == null) {
                return new SimpleStringProperty("Not scheduled");
            }
            String display = nextDate.format(DATE_FORMATTER);
            if (cellData.getValue().isFollowupDue()) {
                display = "DUE: " + display;
            }
            return new SimpleStringProperty(display);
        });
        nextFollowupColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.startsWith("DUE:")) {
                        setStyle("-fx-text-fill: #ff9800; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        // Priority column
        priorityColumn.setCellValueFactory(cellData -> {
            Priority priority = cellData.getValue().getPriority();
            Boolean isUrgent = cellData.getValue().getIsUrgent();
            String display = priority != null ? priority.getDisplayName() : "Normal";
            if (Boolean.TRUE.equals(isUrgent)) {
                display = "URGENT";
            }
            return new SimpleStringProperty(display);
        });
        priorityColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "URGENT" -> setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
                        case "High" -> setStyle("-fx-text-fill: #f57c00; -fx-font-weight: bold;");
                        default -> setStyle("");
                    }
                }
            }
        });

        // Assigned to column
        assignedToColumn.setCellValueFactory(cellData -> {
            String assigned = cellData.getValue().getAssignedTo();
            return new SimpleStringProperty(assigned != null ? assigned : "Unassigned");
        });

        // Enable editing for checkboxes
        registrationsTable.setEditable(true);
    }

    /**
     * Setup filter components
     */
    private void setupFilters() {
        // Status filter
        List<String> statusOptions = new ArrayList<>();
        statusOptions.add("All Active");
        statusOptions.add("Incomplete - Documents");
        statusOptions.add("Incomplete - Photo");
        statusOptions.add("Pending Verification");
        statusFilterCombo.setItems(FXCollections.observableArrayList(statusOptions));
        statusFilterCombo.setValue("All Active");

        // Reason filter
        List<String> reasonOptions = new ArrayList<>();
        reasonOptions.add("All Reasons");
        for (IncompleteReason reason : IncompleteReason.values()) {
            reasonOptions.add(reason.getDisplayName());
        }
        reasonFilterCombo.setItems(FXCollections.observableArrayList(reasonOptions));
        reasonFilterCombo.setValue("All Reasons");

        // Priority filter
        List<String> priorityOptions = new ArrayList<>();
        priorityOptions.add("All Priorities");
        priorityOptions.add("Urgent Only");
        for (Priority priority : Priority.values()) {
            priorityOptions.add(priority.getDisplayName());
        }
        priorityFilterCombo.setItems(FXCollections.observableArrayList(priorityOptions));
        priorityFilterCombo.setValue("All Priorities");

        // Add listeners for auto-filter
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        statusFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        reasonFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        priorityFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        overdueOnlyCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        followupDueCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    /**
     * Setup button states
     */
    private void setupButtons() {
        recordFollowupButton.setDisable(true);
        markCompleteButton.setDisable(true);
        editButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    /**
     * Setup selection listener for details panel
     */
    private void setupSelectionListener() {
        registrationsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                updateButtonStates();
                updateDetailsPanel(newSelection);
            }
        );
    }

    /**
     * Update button states based on selection
     */
    private void updateButtonStates() {
        boolean hasSelection = registrationsTable.getSelectionModel().getSelectedItem() != null;
        recordFollowupButton.setDisable(!hasSelection);
        markCompleteButton.setDisable(!hasSelection);
        editButton.setDisable(!hasSelection);

        boolean hasCheckedItems = !selectedIds.isEmpty();
        deleteButton.setDisable(!hasCheckedItems);
    }

    /**
     * Update details panel with selected registration
     */
    private void updateDetailsPanel(IncompleteRegistration reg) {
        if (reg == null) {
            detailStudentName.setText("-");
            detailGradeLevel.setText("-");
            detailGuardianInfo.setText("-");
            detailStatus.setText("-");
            detailMissingDocs.setText("-");
            detailDaysIncomplete.setText("-");
            detailFollowupNotes.setText("");
            return;
        }

        detailStudentName.setText(reg.getStudentFullName());
        detailGradeLevel.setText(reg.getGradeLevel() != null ? reg.getGradeLevel() : "N/A");

        String guardianInfo = reg.getGuardianName() != null ? reg.getGuardianName() : "N/A";
        if (reg.getGuardianPhone() != null) {
            guardianInfo += "\n" + reg.getGuardianPhone();
        }
        if (reg.getGuardianEmail() != null) {
            guardianInfo += "\n" + reg.getGuardianEmail();
        }
        detailGuardianInfo.setText(guardianInfo);

        detailStatus.setText(reg.getStatusDisplay());

        List<String> missing = reg.getMissingDocumentList();
        detailMissingDocs.setText(missing.isEmpty() ? "None" : String.join("\n", missing));

        detailDaysIncomplete.setText(reg.getDaysSinceIncomplete() + " days");

        detailFollowupNotes.setText(reg.getFollowupNotes() != null ? reg.getFollowupNotes() : "");
    }

    // ========================================================================
    // DATA LOADING
    // ========================================================================

    /**
     * Load all data
     */
    private void loadData() {
        loadRegistrations();
        loadStatistics();
    }

    /**
     * Load incomplete registrations
     */
    private void loadRegistrations() {
        log.info("Loading incomplete registrations");

        if (registrationRepository == null) {
            log.warn("IncompleteRegistrationRepository not available, loading sample data");
            loadSampleData();
            return;
        }

        Task<List<IncompleteRegistration>> task = new Task<>() {
            @Override
            protected List<IncompleteRegistration> call() {
                return registrationRepository.findAllActive();
            }

            @Override
            protected void succeeded() {
                List<IncompleteRegistration> registrations = getValue();
                registrationsList = FXCollections.observableArrayList(registrations);
                filteredList = FXCollections.observableArrayList(registrations);
                registrationsTable.setItems(filteredList);
                log.info("Loaded {} incomplete registrations", registrations.size());
            }

            @Override
            protected void failed() {
                log.error("Failed to load incomplete registrations", getException());
                showError("Failed to load data: " + getException().getMessage());
            }
        };

        new Thread(task).start();
    }

    /**
     * Load sample data for testing/demo
     */
    private void loadSampleData() {
        List<IncompleteRegistration> samples = new ArrayList<>();

        // Sample 1: Missing documents
        IncompleteRegistration reg1 = new IncompleteRegistration();
        reg1.setId(1L);
        reg1.setStudentFirstName("Maria");
        reg1.setStudentLastName("Garcia");
        reg1.setGradeLevel("3rd Grade");
        reg1.setGuardianName("Carlos Garcia");
        reg1.setGuardianPhone("(555) 123-4567");
        reg1.setGuardianEmail("cgarcia@email.com");
        reg1.setStatus(RegistrationStatus.INCOMPLETE_DOCUMENTS);
        reg1.setIncompleteReason(IncompleteReason.MISSING_DOCUMENTS);
        reg1.setMissingBirthCertificate(true);
        reg1.setMissingImmunization(true);
        reg1.setMissingPhoto(false);
        reg1.setPriority(Priority.HIGH);
        reg1.setExpectedCompletionDate(LocalDate.now().minusDays(3));
        reg1.setNextFollowupDate(LocalDate.now());
        reg1.setFollowupCount(2);
        reg1.setFollowupNotes("[2026-01-12] Called parent - documents being mailed\n---\n[2026-01-08] Initial contact");
        samples.add(reg1);

        // Sample 2: Photo refused
        IncompleteRegistration reg2 = new IncompleteRegistration();
        reg2.setId(2L);
        reg2.setStudentFirstName("James");
        reg2.setStudentLastName("Wilson");
        reg2.setGradeLevel("7th Grade");
        reg2.setGuardianName("Sarah Wilson");
        reg2.setGuardianPhone("(555) 234-5678");
        reg2.setStatus(RegistrationStatus.INCOMPLETE_PHOTO);
        reg2.setIncompleteReason(IncompleteReason.PHOTO_REFUSED);
        reg2.setMissingPhoto(true);
        reg2.setPhotoRefused(true);
        reg2.setPhotoRefusalReason("Religious reasons - documentation on file");
        reg2.setPriority(Priority.NORMAL);
        reg2.setAssignedTo("Mrs. Johnson");
        samples.add(reg2);

        // Sample 3: Pending verification
        IncompleteRegistration reg3 = new IncompleteRegistration();
        reg3.setId(3L);
        reg3.setStudentFirstName("Emily");
        reg3.setStudentLastName("Chen");
        reg3.setGradeLevel("9th Grade");
        reg3.setGuardianName("Wei Chen");
        reg3.setGuardianPhone("(555) 345-6789");
        reg3.setStatus(RegistrationStatus.PENDING_DOCUMENTS);
        reg3.setIncompleteReason(IncompleteReason.PENDING_TRANSCRIPT);
        reg3.setMissingProofOfResidence(true);
        reg3.setPriority(Priority.NORMAL);
        reg3.setExpectedCompletionDate(LocalDate.now().plusDays(5));
        reg3.setNextFollowupDate(LocalDate.now().plusDays(3));
        samples.add(reg3);

        // Sample 4: Urgent case
        IncompleteRegistration reg4 = new IncompleteRegistration();
        reg4.setId(4L);
        reg4.setStudentFirstName("David");
        reg4.setStudentLastName("Martinez");
        reg4.setGradeLevel("1st Grade");
        reg4.setGuardianName("Rosa Martinez");
        reg4.setGuardianPhone("(555) 456-7890");
        reg4.setStatus(RegistrationStatus.INCOMPLETE_DOCUMENTS);
        reg4.setIncompleteReason(IncompleteReason.MISSING_DOCUMENTS);
        reg4.setMissingBirthCertificate(true);
        reg4.setMissingImmunization(true);
        reg4.setMissingProofOfResidence(true);
        reg4.setMissingPhoto(true);
        reg4.setIsUrgent(true);
        reg4.setUrgencyReason("School starts Monday");
        reg4.setPriority(Priority.URGENT);
        reg4.setExpectedCompletionDate(LocalDate.now().minusDays(7));
        reg4.setAssignedTo("Mr. Smith");
        samples.add(reg4);

        registrationsList = FXCollections.observableArrayList(samples);
        filteredList = FXCollections.observableArrayList(samples);
        registrationsTable.setItems(filteredList);

        log.info("Loaded {} sample registrations", samples.size());
    }

    /**
     * Load statistics
     */
    private void loadStatistics() {
        if (registrationRepository == null) {
            // Sample statistics
            Platform.runLater(() -> {
                totalIncompleteLabel.setText("4");
                overdueCountLabel.setText("2");
                followupDueLabel.setText("1");
                urgentCountLabel.setText("1");
                missingDocsCountLabel.setText("3");
                missingPhotoCountLabel.setText("2");
            });
            return;
        }

        Task<Void> task = new Task<>() {
            private long total, overdue, followupDue, urgent;

            @Override
            protected Void call() {
                LocalDate today = LocalDate.now();
                total = registrationRepository.countActive();
                overdue = registrationRepository.countOverdue(today);
                followupDue = registrationRepository.countNeedingFollowup(today);
                urgent = registrationRepository.countUrgent();
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    totalIncompleteLabel.setText(String.valueOf(total));
                    overdueCountLabel.setText(String.valueOf(overdue));
                    followupDueLabel.setText(String.valueOf(followupDue));
                    urgentCountLabel.setText(String.valueOf(urgent));
                });
            }

            @Override
            protected void failed() {
                log.error("Failed to load statistics", getException());
            }
        };

        new Thread(task).start();
    }

    // ========================================================================
    // FILTERING
    // ========================================================================

    /**
     * Apply current filters
     */
    private void applyFilters() {
        if (registrationsList == null) {
            return;
        }

        List<IncompleteRegistration> filtered = new ArrayList<>(registrationsList);

        // Search filter
        String searchText = searchField.getText();
        if (searchText != null && !searchText.trim().isEmpty()) {
            String search = searchText.toLowerCase().trim();
            filtered = filtered.stream()
                .filter(r ->
                    r.getStudentFullName().toLowerCase().contains(search) ||
                    (r.getGuardianName() != null && r.getGuardianName().toLowerCase().contains(search)))
                .collect(Collectors.toList());
        }

        // Status filter
        String statusValue = statusFilterCombo.getValue();
        if (statusValue != null && !statusValue.equals("All Active")) {
            filtered = filtered.stream()
                .filter(r -> r.getStatus() != null &&
                    r.getStatus().getDisplayName().contains(statusValue.replace("Incomplete - ", "")))
                .collect(Collectors.toList());
        }

        // Reason filter
        String reasonValue = reasonFilterCombo.getValue();
        if (reasonValue != null && !reasonValue.equals("All Reasons")) {
            filtered = filtered.stream()
                .filter(r -> r.getIncompleteReason() != null &&
                    r.getIncompleteReason().getDisplayName().equals(reasonValue))
                .collect(Collectors.toList());
        }

        // Priority filter
        String priorityValue = priorityFilterCombo.getValue();
        if (priorityValue != null && !priorityValue.equals("All Priorities")) {
            if (priorityValue.equals("Urgent Only")) {
                filtered = filtered.stream()
                    .filter(r -> Boolean.TRUE.equals(r.getIsUrgent()))
                    .collect(Collectors.toList());
            } else {
                filtered = filtered.stream()
                    .filter(r -> r.getPriority() != null &&
                        r.getPriority().getDisplayName().equals(priorityValue))
                    .collect(Collectors.toList());
            }
        }

        // Overdue only filter
        if (overdueOnlyCheckbox.isSelected()) {
            filtered = filtered.stream()
                .filter(IncompleteRegistration::isOverdue)
                .collect(Collectors.toList());
        }

        // Follow-up due filter
        if (followupDueCheckbox.isSelected()) {
            filtered = filtered.stream()
                .filter(IncompleteRegistration::isFollowupDue)
                .collect(Collectors.toList());
        }

        filteredList = FXCollections.observableArrayList(filtered);
        registrationsTable.setItems(filteredList);

        log.info("Filters applied: {} results", filtered.size());
    }

    // ========================================================================
    // BUTTON HANDLERS
    // ========================================================================

    /**
     * Handle Refresh button
     */
    @FXML
    private void handleRefresh() {
        log.info("Refreshing incomplete registrations");
        selectedIds.clear();
        loadData();
    }

    /**
     * Handle Clear Filters button
     */
    @FXML
    private void handleClearFilters() {
        searchField.clear();
        statusFilterCombo.setValue("All Active");
        reasonFilterCombo.setValue("All Reasons");
        priorityFilterCombo.setValue("All Priorities");
        overdueOnlyCheckbox.setSelected(false);
        followupDueCheckbox.setSelected(false);
        applyFilters();
    }

    /**
     * Handle Record Follow-up button
     */
    @FXML
    private void handleRecordFollowup() {
        IncompleteRegistration selected = registrationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select a registration to record a follow-up.");
            return;
        }

        showFollowupDialog(selected);
    }

    /**
     * Handle Mark Complete button
     */
    @FXML
    private void handleMarkComplete() {
        IncompleteRegistration selected = registrationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select a registration to mark as complete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Mark Registration Complete");
        confirm.setHeaderText("Complete Registration for " + selected.getStudentFullName());
        confirm.setContentText(
            "This will mark the registration as complete and remove it from the incomplete list.\n\n" +
            "Are you sure all documents have been received?"
        );

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                markRegistrationComplete(selected);
            }
        });
    }

    /**
     * Handle Edit button
     */
    @FXML
    private void handleEdit() {
        IncompleteRegistration selected = registrationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select a registration to edit.");
            return;
        }

        showEditDialog(selected);
    }

    /**
     * Handle Delete button
     */
    @FXML
    private void handleDelete() {
        List<IncompleteRegistration> selectedRegs = getSelectedRegistrations();
        if (selectedRegs.isEmpty()) {
            showWarning("Please check the boxes next to registrations you want to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Registrations");
        confirm.setHeaderText("Delete " + selectedRegs.size() + " Registration(s)");
        confirm.setContentText(
            "Are you sure you want to delete the selected incomplete registration(s)?\n\n" +
            "This action cannot be undone."
        );

        ButtonType deleteBtn = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(deleteBtn, cancelBtn);

        confirm.showAndWait().ifPresent(response -> {
            if (response == deleteBtn) {
                deleteRegistrations(selectedRegs);
            }
        });
    }

    /**
     * Handle Export button
     */
    @FXML
    private void handleExport() {
        showInfo("Export functionality will generate a CSV/PDF report of incomplete registrations.\n\n" +
            "Feature coming soon.");
    }

    // ========================================================================
    // DIALOGS
    // ========================================================================

    /**
     * Show follow-up dialog
     */
    private void showFollowupDialog(IncompleteRegistration reg) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Record Follow-up");
        dialog.setHeaderText("Follow-up for " + reg.getStudentFullName());

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Enter notes from this follow-up contact...");
        notesArea.setPrefRowCount(4);
        notesArea.setPrefWidth(400);

        DatePicker nextFollowupPicker = new DatePicker();
        nextFollowupPicker.setPromptText("Next follow-up date");
        nextFollowupPicker.setValue(LocalDate.now().plusDays(7));

        ComboBox<String> contactMethodCombo = new ComboBox<>();
        contactMethodCombo.setItems(FXCollections.observableArrayList(
            "Phone Call", "Email", "In-Person", "Text Message", "Other"
        ));
        contactMethodCombo.setValue("Phone Call");

        grid.add(new Label("Contact Method:"), 0, 0);
        grid.add(contactMethodCombo, 1, 0);
        grid.add(new Label("Notes:"), 0, 1);
        grid.add(notesArea, 0, 2, 2, 1);
        grid.add(new Label("Next Follow-up:"), 0, 3);
        grid.add(nextFollowupPicker, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                String notes = contactMethodCombo.getValue() + ": " + notesArea.getText();
                reg.recordFollowup(notes, nextFollowupPicker.getValue());

                if (registrationRepository != null) {
                    registrationRepository.save(reg);
                }

                return true;
            }
            return false;
        });

        dialog.showAndWait().ifPresent(saved -> {
            if (saved) {
                showInfo("Follow-up recorded successfully.");
                loadData();
            }
        });
    }

    /**
     * Show edit dialog
     */
    private void showEditDialog(IncompleteRegistration reg) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Edit Registration");
        dialog.setHeaderText("Edit " + reg.getStudentFullName());

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Priority combo
        ComboBox<Priority> priorityCombo = new ComboBox<>();
        priorityCombo.setItems(FXCollections.observableArrayList(Priority.values()));
        priorityCombo.setValue(reg.getPriority());

        // Urgent checkbox
        CheckBox urgentCheckbox = new CheckBox("Mark as Urgent");
        urgentCheckbox.setSelected(Boolean.TRUE.equals(reg.getIsUrgent()));

        // Assigned to field
        TextField assignedToField = new TextField();
        assignedToField.setText(reg.getAssignedTo() != null ? reg.getAssignedTo() : "");
        assignedToField.setPromptText("Staff member name");

        // Expected completion date
        DatePicker expectedDatePicker = new DatePicker();
        expectedDatePicker.setValue(reg.getExpectedCompletionDate());

        // Missing document checkboxes
        CheckBox missingBirthCert = new CheckBox("Birth Certificate");
        missingBirthCert.setSelected(Boolean.TRUE.equals(reg.getMissingBirthCertificate()));

        CheckBox missingImmunization = new CheckBox("Immunization Records");
        missingImmunization.setSelected(Boolean.TRUE.equals(reg.getMissingImmunization()));

        CheckBox missingProofRes = new CheckBox("Proof of Residence");
        missingProofRes.setSelected(Boolean.TRUE.equals(reg.getMissingProofOfResidence()));

        CheckBox missingPhoto = new CheckBox("Student Photo");
        missingPhoto.setSelected(Boolean.TRUE.equals(reg.getMissingPhoto()));

        grid.add(new Label("Priority:"), 0, 0);
        grid.add(priorityCombo, 1, 0);
        grid.add(urgentCheckbox, 2, 0);

        grid.add(new Label("Assigned To:"), 0, 1);
        grid.add(assignedToField, 1, 1, 2, 1);

        grid.add(new Label("Expected Date:"), 0, 2);
        grid.add(expectedDatePicker, 1, 2);

        grid.add(new Label("Missing Documents:"), 0, 3);
        VBox docsBox = new VBox(5, missingBirthCert, missingImmunization, missingProofRes, missingPhoto);
        grid.add(docsBox, 1, 3, 2, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                reg.setPriority(priorityCombo.getValue());
                reg.setIsUrgent(urgentCheckbox.isSelected());
                reg.setAssignedTo(assignedToField.getText().trim().isEmpty() ? null : assignedToField.getText().trim());
                reg.setExpectedCompletionDate(expectedDatePicker.getValue());
                reg.setMissingBirthCertificate(missingBirthCert.isSelected());
                reg.setMissingImmunization(missingImmunization.isSelected());
                reg.setMissingProofOfResidence(missingProofRes.isSelected());
                reg.setMissingPhoto(missingPhoto.isSelected());

                if (registrationRepository != null) {
                    registrationRepository.save(reg);
                }

                return true;
            }
            return false;
        });

        dialog.showAndWait().ifPresent(saved -> {
            if (saved) {
                showInfo("Registration updated successfully.");
                loadData();
            }
        });
    }

    /**
     * Mark registration as complete
     */
    private void markRegistrationComplete(IncompleteRegistration reg) {
        reg.markComplete();

        if (registrationRepository != null) {
            registrationRepository.save(reg);
        }

        showInfo("Registration marked as complete.");
        loadData();
    }

    /**
     * Delete selected registrations
     */
    private void deleteRegistrations(List<IncompleteRegistration> regs) {
        if (registrationRepository != null) {
            registrationRepository.deleteAll(regs);
        }

        selectedIds.clear();
        showInfo("Deleted " + regs.size() + " registration(s).");
        loadData();
    }

    /**
     * Get list of checked registrations
     */
    private List<IncompleteRegistration> getSelectedRegistrations() {
        return registrationsTable.getItems().stream()
            .filter(r -> selectedIds.contains(r.getId()))
            .collect(Collectors.toList());
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
