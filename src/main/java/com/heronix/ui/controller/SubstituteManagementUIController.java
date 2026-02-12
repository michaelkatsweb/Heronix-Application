package com.heronix.ui.controller;

import com.heronix.dto.SubstituteAssignmentDTO;
import com.heronix.model.domain.Substitute;
import com.heronix.model.domain.SubstituteAssignment;
import com.heronix.model.enums.AssignmentStatus;
import com.heronix.model.enums.SubstituteType;
import com.heronix.service.SubstituteImportService;
import com.heronix.service.SubstituteManagementService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.heronix.util.ResponsiveDesignHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for Substitute Management main UI
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since 2025-11-05
 */
@Controller
public class SubstituteManagementUIController {

    private static final Logger logger = LoggerFactory.getLogger(SubstituteManagementUIController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");

    @Autowired
    private SubstituteManagementService substituteManagementService;

    @Autowired
    private com.heronix.service.SubstituteReportService substituteReportService;

    @Autowired
    private SubstituteImportService substituteImportService;

    @Autowired
    private ApplicationContext applicationContext;

    // Main Tab Pane
    @FXML private TabPane mainTabPane;

    // ==================== ASSIGNMENTS TAB ====================

    // Filters
    @FXML private DatePicker filterStartDatePicker;
    @FXML private DatePicker filterEndDatePicker;
    @FXML private ComboBox<AssignmentStatus> filterStatusComboBox;
    @FXML private ComboBox<Substitute> filterSubstituteComboBox;

    // Assignments Table
    @FXML private TableView<SubstituteAssignmentDTO> assignmentsTable;
    @FXML private TableColumn<SubstituteAssignmentDTO, String> assignmentDateColumn;
    @FXML private TableColumn<SubstituteAssignmentDTO, String> assignmentSubstituteColumn;
    @FXML private TableColumn<SubstituteAssignmentDTO, String> assignmentTimeColumn;
    @FXML private TableColumn<SubstituteAssignmentDTO, String> assignmentDurationColumn;
    @FXML private TableColumn<SubstituteAssignmentDTO, String> assignmentReplacedStaffColumn;
    @FXML private TableColumn<SubstituteAssignmentDTO, String> assignmentReasonColumn;
    @FXML private TableColumn<SubstituteAssignmentDTO, String> assignmentRoomColumn;
    @FXML private TableColumn<SubstituteAssignmentDTO, String> assignmentStatusColumn;
    @FXML private TableColumn<SubstituteAssignmentDTO, Void> assignmentActionsColumn;
    @FXML private Label assignmentCountLabel;

    // ==================== SUBSTITUTES TAB ====================

    // Substitute Search
    @FXML private TextField substituteSearchField;

    // Substitutes Table
    @FXML private TableView<Substitute> substitutesTable;
    @FXML private TableColumn<Substitute, String> subNameColumn;
    @FXML private TableColumn<Substitute, String> subEmployeeIdColumn;
    @FXML private TableColumn<Substitute, String> subTypeColumn;
    @FXML private TableColumn<Substitute, String> subEmailColumn;
    @FXML private TableColumn<Substitute, String> subPhoneColumn;
    @FXML private TableColumn<Substitute, String> subCertificationsColumn;
    @FXML private TableColumn<Substitute, String> subStatusColumn;
    @FXML private TableColumn<Substitute, Void> subActionsColumn;
    @FXML private Label substituteCountLabel;

    // ==================== REPORTS TAB ====================

    @FXML private ComboBox<String> reportTypeComboBox;
    @FXML private DatePicker reportStartDatePicker;
    @FXML private DatePicker reportEndDatePicker;

    // Statistics
    @FXML private Label statsThisWeekLabel;
    @FXML private Label statsThisMonthLabel;
    @FXML private Label statsActiveSubsLabel;
    @FXML private Label statsTotalHoursLabel;

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        logger.info("Initializing Substitute Management UI Controller");

        setupAssignmentsTab();
        setupSubstitutesTab();
        setupReportsTab();

        loadData();
    }

    /**
     * Setup Assignments Tab
     */
    private void setupAssignmentsTab() {
        // Setup table columns using flat DTO fields (no lazy proxies)
        assignmentDateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getAssignmentDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getAssignmentDate().format(DATE_FORMATTER));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        assignmentSubstituteColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSubstituteName()));

        assignmentTimeColumn.setCellValueFactory(cellData -> {
            String start = cellData.getValue().getStartTime() != null ?
                    cellData.getValue().getStartTime().format(TIME_FORMATTER) : "";
            String end = cellData.getValue().getEndTime() != null ?
                    cellData.getValue().getEndTime().format(TIME_FORMATTER) : "";
            return new javafx.beans.property.SimpleStringProperty(start + " - " + end);
        });

        assignmentDurationColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDurationDisplay()));

        assignmentReplacedStaffColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getReplacedStaffName()));

        assignmentReasonColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getAbsenceReasonDisplay()));

        assignmentRoomColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRoomNumber()));

        assignmentStatusColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatusDisplay()));

        // Add actions column
        addAssignmentActionsColumn();

        // Setup filter combo boxes
        filterStatusComboBox.setItems(FXCollections.observableArrayList(AssignmentStatus.values()));
        filterStatusComboBox.setConverter(createEnumConverter());

        // Set default date range (current month)
        filterStartDatePicker.setValue(YearMonth.now().atDay(1));
        filterEndDatePicker.setValue(YearMonth.now().atEndOfMonth());
    }

    /**
     * Setup Substitutes Tab
     */
    private void setupSubstitutesTab() {
        subNameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFullName()));

        subEmployeeIdColumn.setCellValueFactory(new PropertyValueFactory<>("employeeId"));

        subTypeColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getType() != null ?
                                cellData.getValue().getType().getDisplayName() : ""));

        subEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        subPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        subCertificationsColumn.setCellValueFactory(cellData -> {
            Substitute substitute = cellData.getValue();
            if (substitute != null && substitute.getCertifications() != null && !substitute.getCertifications().isEmpty()) {
                return new javafx.beans.property.SimpleStringProperty(String.join(", ", substitute.getCertifications()));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        subStatusColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getActive() ? "Active" : "Inactive"));

        // Add actions column
        addSubstituteActionsColumn();
    }

    /**
     * Setup Reports Tab
     */
    private void setupReportsTab() {
        // Setup report types
        reportTypeComboBox.setItems(FXCollections.observableArrayList(
                "Substitute Usage Summary",
                "Assignments by Date Range",
                "Substitute Hours Report",
                "Absence Reasons Report",
                "Cost Analysis Report"
        ));

        // Set default dates
        reportStartDatePicker.setValue(YearMonth.now().atDay(1));
        reportEndDatePicker.setValue(YearMonth.now().atEndOfMonth());
    }

    /**
     * Add actions column to assignments table
     */
    private void addAssignmentActionsColumn() {
        assignmentActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");

            {
                editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 4 8;");
                deleteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 4 8;");

                editButton.setOnAction(event -> {
                    SubstituteAssignmentDTO dto = getTableView().getItems().get(getIndex());
                    // Load the full entity for editing
                    substituteManagementService.getAssignmentById(dto.getId()).ifPresent(
                            assignment -> handleEditAssignment(assignment));
                });

                deleteButton.setOnAction(event -> {
                    SubstituteAssignmentDTO dto = getTableView().getItems().get(getIndex());
                    handleDeleteAssignmentById(dto.getId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(5, editButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });
    }

    /**
     * Add actions column to substitutes table
     */
    private void addSubstituteActionsColumn() {
        subActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button viewButton = new Button("View");

            {
                editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 4 8;");
                viewButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 4 8;");

                editButton.setOnAction(event -> {
                    Substitute substitute = getTableView().getItems().get(getIndex());
                    handleEditSubstitute(substitute);
                });

                viewButton.setOnAction(event -> {
                    Substitute substitute = getTableView().getItems().get(getIndex());
                    handleViewSubstitute(substitute);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(5, editButton, viewButton);
                    setGraphic(buttons);
                }
            }
        });
    }

    /**
     * Create enum string converter
     */
    private <T extends Enum<?>> StringConverter<T> createEnumConverter() {
        return new StringConverter<T>() {
            @Override
            public String toString(T value) {
                if (value == null) return "";
                try {
                    return (String) value.getClass().getMethod("getDisplayName").invoke(value);
                } catch (Exception e) {
                    return value.toString();
                }
            }

            @Override
            public T fromString(String string) {
                return null;
            }
        };
    }

    /**
     * Load all data
     */
    private void loadData() {
        loadAssignments();
        loadSubstitutes();
        loadStatistics();
    }

    /**
     * Load assignments
     */
    private void loadAssignments() {
        LocalDate startDate = filterStartDatePicker.getValue();
        LocalDate endDate = filterEndDatePicker.getValue();

        List<SubstituteAssignmentDTO> assignments;

        if (startDate != null && endDate != null) {
            assignments = substituteManagementService.getAssignmentDTOsBetweenDates(startDate, endDate);
        } else {
            assignments = substituteManagementService.getAllAssignmentDTOs();
        }

        // Apply filters
        if (filterStatusComboBox.getValue() != null) {
            assignments = assignments.stream()
                    .filter(a -> a.getStatus() == filterStatusComboBox.getValue())
                    .collect(Collectors.toList());
        }

        if (filterSubstituteComboBox.getValue() != null) {
            Substitute filterSub = filterSubstituteComboBox.getValue();
            assignments = assignments.stream()
                    .filter(a -> a.getSubstituteId() != null && a.getSubstituteId().equals(filterSub.getId()))
                    .collect(Collectors.toList());
        }

        assignmentsTable.setItems(FXCollections.observableArrayList(assignments));
        assignmentCountLabel.setText("Total: " + assignments.size() + " assignments");

        logger.info("Loaded {} assignments", assignments.size());
    }

    /**
     * Load substitutes
     */
    private void loadSubstitutes() {
        List<Substitute> substitutes = substituteManagementService.getAllSubstitutes();
        substitutesTable.setItems(FXCollections.observableArrayList(substitutes));
        substituteCountLabel.setText("Total: " + substitutes.size() + " substitutes");

        // Update filter combo box
        filterSubstituteComboBox.setItems(FXCollections.observableArrayList(substitutes));
        filterSubstituteComboBox.setConverter(new StringConverter<Substitute>() {
            @Override
            public String toString(Substitute sub) {
                return sub != null ? sub.getFullName() : "";
            }

            @Override
            public Substitute fromString(String string) {
                return null;
            }
        });

        logger.info("Loaded {} substitutes", substitutes.size());
    }

    /**
     * Load statistics
     */
    private void loadStatistics() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate weekEnd = today.with(java.time.DayOfWeek.SUNDAY);
        YearMonth currentMonth = YearMonth.now();

        // This week
        long thisWeek = substituteManagementService.getAssignmentsBetweenDates(weekStart, weekEnd).size();
        statsThisWeekLabel.setText(String.valueOf(thisWeek));

        // This month
        long thisMonth = substituteManagementService.getAssignmentsBetweenDates(
                currentMonth.atDay(1), currentMonth.atEndOfMonth()).size();
        statsThisMonthLabel.setText(String.valueOf(thisMonth));

        // Active substitutes
        long activeSubs = substituteManagementService.countActiveSubstitutes();
        statsActiveSubsLabel.setText(String.valueOf(activeSubs));

        // Total hours this month
        List<SubstituteAssignment> monthAssignments = substituteManagementService.getAssignmentsBetweenDates(
                currentMonth.atDay(1), currentMonth.atEndOfMonth());
        double totalHours = monthAssignments.stream()
                .mapToDouble(a -> a.getTotalHours() != null ? a.getTotalHours() : 0.0)
                .sum();
        statsTotalHoursLabel.setText(String.format("%.1f", totalHours));
    }

    // ==================== EVENT HANDLERS ====================

    /**
     * Handle new assignment button
     */
    @FXML
    private void handleNewAssignment() {
        openAssignmentForm(null);
    }

    /**
     * Handle import Frontline button
     */
    @FXML
    private void handleImportFrontline() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FrontlineImport.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Import from Frontline");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);
            ResponsiveDesignHelper.makeDialogResponsive(stage);
            stage.showAndWait();

            // Refresh after import
            loadAssignments();
            loadSubstitutes();
            loadStatistics();

        } catch (Exception e) {
            logger.error("Error opening Frontline import dialog", e);
            showError("Error", "Could not open import dialog: " + e.getMessage());
        }
    }

    /**
     * Handle search assignments
     */
    @FXML
    private void handleSearchAssignments() {
        loadAssignments();
    }

    /**
     * Handle clear filters
     */
    @FXML
    private void handleClearFilters() {
        filterStartDatePicker.setValue(YearMonth.now().atDay(1));
        filterEndDatePicker.setValue(YearMonth.now().atEndOfMonth());
        filterStatusComboBox.setValue(null);
        filterSubstituteComboBox.setValue(null);
        loadAssignments();
    }

    /**
     * Handle refresh assignments
     */
    @FXML
    private void handleRefreshAssignments() {
        loadAssignments();
        loadStatistics();
    }

    /**
     * Handle search substitutes
     */
    @FXML
    private void handleSearchSubstitutes() {
        String query = substituteSearchField.getText();
        if (query == null || query.trim().isEmpty()) {
            loadSubstitutes();
            return;
        }

        List<Substitute> results = substituteManagementService.searchSubstitutesByName(query.trim());
        substitutesTable.setItems(FXCollections.observableArrayList(results));
        substituteCountLabel.setText("Found: " + results.size() + " substitutes");
    }

    /**
     * Handle add substitute
     */
    @FXML
    private void handleAddSubstitute() {
        Dialog<Substitute> dialog = createSubstituteDialog(null);
        Optional<Substitute> result = dialog.showAndWait();

        result.ifPresent(substitute -> {
            try {
                Substitute saved = substituteManagementService.saveSubstitute(substitute);
                showInfo("Success", "Substitute " + saved.getFirstName() + " " + saved.getLastName() + " added successfully!");
                loadSubstitutes();
            } catch (Exception e) {
                logger.error("Error adding substitute", e);
                showError("Error", "Failed to add substitute: " + e.getMessage());
            }
        });
    }

    /**
     * Handle refresh substitutes
     */
    @FXML
    private void handleRefreshSubstitutes() {
        substituteSearchField.clear();
        loadSubstitutes();
    }

    /**
     * Handle import third-party CSV
     * Opens a dialog to select and configure CSV import for third-party substitute services
     */
    @FXML
    private void handleImportThirdPartyCSV() {
        try {
            // Create dialog for import configuration
            Dialog<SubstituteImportService.ImportResult> dialog = new Dialog<>();
            dialog.setTitle("Import Third-Party Substitutes");
            dialog.setHeaderText("Import substitutes from a third-party CSV file");

            // Set button types
            ButtonType importButtonType = new ButtonType("Import", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(importButtonType, ButtonType.CANCEL);

            // Create form layout
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 20, 10, 10));

            // Agency selection
            ComboBox<String> agencyCombo = new ComboBox<>();
            Map<String, SubstituteImportService.ImportConfig> knownConfigs = substituteImportService.getKnownAgencyConfigs();
            agencyCombo.getItems().addAll(knownConfigs.keySet());
            agencyCombo.setPromptText("Select agency or Custom");
            agencyCombo.setPrefWidth(250);

            // Custom agency name field
            TextField customAgencyField = new TextField();
            customAgencyField.setPromptText("Enter custom agency name");
            customAgencyField.setVisible(false);
            customAgencyField.setManaged(false);

            // File selection
            TextField filePathField = new TextField();
            filePathField.setPromptText("Select CSV file...");
            filePathField.setEditable(false);
            filePathField.setPrefWidth(300);

            Button browseButton = new Button("Browse...");
            final File[] selectedFile = {null};

            browseButton.setOnAction(e -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select CSV File");
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                        new FileChooser.ExtensionFilter("All Files", "*.*")
                );

                File file = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
                if (file != null) {
                    selectedFile[0] = file;
                    filePathField.setText(file.getName());
                }
            });

            // Options
            CheckBox updateExistingCheck = new CheckBox("Update existing substitutes if found");
            updateExistingCheck.setSelected(true);

            CheckBox hasHeaderCheck = new CheckBox("CSV has header row");
            hasHeaderCheck.setSelected(true);

            // Preview area
            TextArea previewArea = new TextArea();
            previewArea.setPromptText("CSV preview will appear here after selecting a file");
            previewArea.setEditable(false);
            previewArea.setPrefRowCount(6);
            previewArea.setPrefWidth(500);

            // Show/hide custom agency field based on selection
            agencyCombo.setOnAction(e -> {
                boolean isCustom = "Custom".equals(agencyCombo.getValue());
                customAgencyField.setVisible(isCustom);
                customAgencyField.setManaged(isCustom);
            });

            // Update preview when file is selected
            browseButton.setOnAction(e -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select CSV File");
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                        new FileChooser.ExtensionFilter("All Files", "*.*")
                );

                File file = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
                if (file != null) {
                    selectedFile[0] = file;
                    filePathField.setText(file.getName());

                    // Show preview
                    try {
                        List<String[]> preview = substituteImportService.previewCsv(file, 5);
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < preview.size(); i++) {
                            sb.append("Row ").append(i + 1).append(": ");
                            sb.append(String.join(" | ", preview.get(i)));
                            sb.append("\n");
                        }
                        previewArea.setText(sb.toString());
                    } catch (Exception ex) {
                        previewArea.setText("Error reading file: " + ex.getMessage());
                    }
                }
            });

            // Add to grid
            int row = 0;
            grid.add(new Label("Third-Party Agency:"), 0, row);
            grid.add(agencyCombo, 1, row);
            row++;

            grid.add(new Label("Custom Agency Name:"), 0, row);
            grid.add(customAgencyField, 1, row);
            row++;

            grid.add(new Label("CSV File:"), 0, row);
            javafx.scene.layout.HBox fileBox = new javafx.scene.layout.HBox(10, filePathField, browseButton);
            grid.add(fileBox, 1, row);
            row++;

            grid.add(new Label("Options:"), 0, row);
            VBox optionsBox = new VBox(5, hasHeaderCheck, updateExistingCheck);
            grid.add(optionsBox, 1, row);
            row++;

            grid.add(new Label("Preview:"), 0, row);
            grid.add(previewArea, 1, row);

            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().setPrefWidth(650);

            // Enable/disable import button
            Node importButton = dialog.getDialogPane().lookupButton(importButtonType);
            importButton.setDisable(true);

            agencyCombo.valueProperty().addListener((obs, old, newVal) -> {
                boolean valid = newVal != null && selectedFile[0] != null;
                if ("Custom".equals(newVal)) {
                    valid = valid && !customAgencyField.getText().trim().isEmpty();
                }
                importButton.setDisable(!valid);
            });

            customAgencyField.textProperty().addListener((obs, old, newVal) -> {
                boolean valid = agencyCombo.getValue() != null && selectedFile[0] != null;
                if ("Custom".equals(agencyCombo.getValue())) {
                    valid = valid && !newVal.trim().isEmpty();
                }
                importButton.setDisable(!valid);
            });

            filePathField.textProperty().addListener((obs, old, newVal) -> {
                boolean valid = agencyCombo.getValue() != null && !newVal.isEmpty();
                if ("Custom".equals(agencyCombo.getValue())) {
                    valid = valid && !customAgencyField.getText().trim().isEmpty();
                }
                importButton.setDisable(!valid);
            });

            // Result converter
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == importButtonType && selectedFile[0] != null) {
                    try {
                        String agencyName = "Custom".equals(agencyCombo.getValue())
                                ? customAgencyField.getText().trim()
                                : agencyCombo.getValue();

                        SubstituteImportService.ImportConfig config;

                        // Get config based on agency selection
                        if ("Custom".equals(agencyCombo.getValue())) {
                            // Auto-detect columns from header
                            String[] header = substituteImportService.readHeaderRow(selectedFile[0]);
                            config = substituteImportService.autoDetectColumns(header, agencyName);
                        } else {
                            config = knownConfigs.get(agencyCombo.getValue());
                        }

                        config.hasHeaderRow(hasHeaderCheck.isSelected());
                        config.updateExisting(updateExistingCheck.isSelected());

                        // Perform import
                        return substituteImportService.importFromCsv(selectedFile[0], config);

                    } catch (Exception e) {
                        logger.error("Error during CSV import", e);
                        showError("Import Error", "Failed to import CSV: " + e.getMessage());
                    }
                }
                return null;
            });

            // Show dialog and handle result
            Optional<SubstituteImportService.ImportResult> result = dialog.showAndWait();

            result.ifPresent(importResult -> {
                // Show result summary
                StringBuilder summary = new StringBuilder();
                summary.append("Import Complete!\n\n");
                summary.append("Reference: ").append(importResult.getImportReference()).append("\n\n");
                summary.append("Results:\n");
                summary.append("- Total rows processed: ").append(importResult.getTotalRows()).append("\n");
                summary.append("- New substitutes imported: ").append(importResult.getImported()).append("\n");
                summary.append("- Existing substitutes updated: ").append(importResult.getUpdated()).append("\n");
                summary.append("- Rows skipped: ").append(importResult.getSkipped()).append("\n");
                summary.append("- Errors: ").append(importResult.getErrors()).append("\n");

                if (!importResult.getErrorMessages().isEmpty()) {
                    summary.append("\nError details:\n");
                    int maxErrors = Math.min(10, importResult.getErrorMessages().size());
                    for (int i = 0; i < maxErrors; i++) {
                        summary.append("- ").append(importResult.getErrorMessages().get(i)).append("\n");
                    }
                    if (importResult.getErrorMessages().size() > 10) {
                        summary.append("... and ").append(importResult.getErrorMessages().size() - 10).append(" more errors\n");
                    }
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Import Result");
                alert.setHeaderText("Third-Party Substitute Import");

                TextArea textArea = new TextArea(summary.toString());
                textArea.setEditable(false);
                textArea.setWrapText(true);
                textArea.setPrefRowCount(15);
                alert.getDialogPane().setContent(textArea);
                alert.showAndWait();

                // Refresh the substitutes list
                loadSubstitutes();
            });

        } catch (Exception e) {
            logger.error("Error opening import dialog", e);
            showError("Error", "Could not open import dialog: " + e.getMessage());
        }
    }

    /**
     * Handle generate report
     */
    @FXML
    private void handleGenerateReport() {
        try {
            String reportType = reportTypeComboBox.getValue();
            LocalDate startDate = reportStartDatePicker.getValue();
            LocalDate endDate = reportEndDatePicker.getValue();

            // Validation
            if (reportType == null || reportType.isEmpty()) {
                showError("Validation Error", "Please select a report type");
                return;
            }

            if (startDate == null || endDate == null) {
                showError("Validation Error", "Please select start and end dates");
                return;
            }

            if (startDate.isAfter(endDate)) {
                showError("Validation Error", "Start date must be before end date");
                return;
            }

            logger.info("Generating report: {} from {} to {}", reportType, startDate, endDate);

            // Generate report based on type
            String csvContent = null;
            String reportTitle = reportType;

            switch (reportType) {
                case "Substitute Usage Summary":
                    com.heronix.model.dto.SubstituteUsageReport usageReport =
                            substituteReportService.generateUsageSummary(startDate, endDate);
                    csvContent = exportUsageReportToCSV(usageReport, startDate, endDate);
                    break;

                case "Assignments by Date Range":
                    com.heronix.model.dto.SubstituteReport assignmentsReport =
                            substituteReportService.generateAssignmentsByDateRange(startDate, endDate);
                    csvContent = substituteReportService.exportToCSV(assignmentsReport);
                    break;

                case "Substitute Hours Report":
                    com.heronix.model.dto.SubstituteReport hoursReport =
                            substituteReportService.generateHoursReport(startDate, endDate);
                    csvContent = substituteReportService.exportToCSV(hoursReport);
                    break;

                case "Absence Reasons Report":
                    com.heronix.model.dto.AbsenceReasonsReport reasonsReport =
                            substituteReportService.generateAbsenceReasonsReport(startDate, endDate);
                    csvContent = exportAbsenceReasonsToCSV(reasonsReport, startDate, endDate);
                    break;

                case "Cost Analysis Report":
                    com.heronix.model.dto.SubstituteReport costReport =
                            substituteReportService.generateCostAnalysisReport(startDate, endDate);
                    csvContent = substituteReportService.exportToCSV(costReport);
                    break;

                default:
                    showError("Error", "Unknown report type: " + reportType);
                    return;
            }

            // Save CSV file
            if (csvContent != null) {
                javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
                fileChooser.setTitle("Save Report");
                fileChooser.setInitialFileName(reportType.replace(" ", "_") + "_" +
                        startDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + "_to_" +
                        endDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + ".csv");
                fileChooser.getExtensionFilters().add(
                        new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));

                java.io.File file = fileChooser.showSaveDialog(reportTypeComboBox.getScene().getWindow());

                if (file != null) {
                    java.nio.file.Files.writeString(file.toPath(), csvContent);
                    showInfo("Success", "Report saved successfully to:\n" + file.getAbsolutePath());
                    logger.info("Report saved to: {}", file.getAbsolutePath());
                }
            }

        } catch (Exception e) {
            logger.error("Error generating report", e);
            showError("Error", "Failed to generate report: " + e.getMessage());
        }
    }

    /**
     * Export usage report to CSV
     */
    private String exportUsageReportToCSV(com.heronix.model.dto.SubstituteUsageReport report,
                                          LocalDate startDate, LocalDate endDate) {
        StringBuilder csv = new StringBuilder();

        // Header
        csv.append("# Substitute Usage Summary Report\n");
        csv.append("# Date Range: ").append(startDate).append(" to ").append(endDate).append("\n");
        csv.append("# Generated: ").append(LocalDate.now()).append("\n");
        csv.append("\n");

        // Summary
        com.heronix.model.dto.SubstituteUsageReport.UsageSummary summary = report.getSummary();
        csv.append("Summary\n");
        csv.append("Total Substitutes,").append(summary.getTotalSubstitutes()).append("\n");
        csv.append("Active Substitutes,").append(summary.getActiveSubstitutes()).append("\n");
        csv.append("Total Assignments,").append(summary.getTotalAssignments()).append("\n");
        csv.append("Total Hours,").append(String.format("%.2f", summary.getTotalHours())).append("\n");
        csv.append("Avg Assignments/Sub,").append(String.format("%.2f", summary.getAverageAssignmentsPerSubstitute())).append("\n");
        csv.append("Avg Hours/Sub,").append(String.format("%.2f", summary.getAverageHoursPerSubstitute())).append("\n");
        csv.append("\n");

        // Column headers
        csv.append("Substitute,Type,Email,Phone,Total Assignments,Total Hours,Avg Hours/Assignment,");
        csv.append("Confirmed,Pending,Cancelled,Most Common Reason\n");

        // Data rows
        for (com.heronix.model.dto.SubstituteUsageReport.SubstituteUsageRow row : report.getSubstitutes()) {
            csv.append(escapeCSV(row.getSubstituteName())).append(",");
            csv.append(escapeCSV(row.getSubstituteType())).append(",");
            csv.append(escapeCSV(row.getEmail())).append(",");
            csv.append(escapeCSV(row.getPhone())).append(",");
            csv.append(row.getTotalAssignments()).append(",");
            csv.append(String.format("%.2f", row.getTotalHours())).append(",");
            csv.append(String.format("%.2f", row.getAverageHoursPerAssignment())).append(",");
            csv.append(row.getConfirmedAssignments()).append(",");
            csv.append(row.getPendingAssignments()).append(",");
            csv.append(row.getCancelledAssignments()).append(",");
            csv.append(escapeCSV(row.getMostCommonAbsenceReason())).append("\n");
        }

        return csv.toString();
    }

    /**
     * Export absence reasons report to CSV
     */
    private String exportAbsenceReasonsToCSV(com.heronix.model.dto.AbsenceReasonsReport report,
                                             LocalDate startDate, LocalDate endDate) {
        StringBuilder csv = new StringBuilder();

        // Header
        csv.append("# Absence Reasons Report\n");
        csv.append("# Date Range: ").append(startDate).append(" to ").append(endDate).append("\n");
        csv.append("# Generated: ").append(LocalDate.now()).append("\n");
        csv.append("\n");

        // Summary
        com.heronix.model.dto.AbsenceReasonsReport.AbsenceReasonSummary summary = report.getSummary();
        csv.append("Summary\n");
        csv.append("Total Assignments,").append(summary.getTotalAssignments()).append("\n");
        csv.append("Unique Reasons,").append(summary.getUniqueReasons()).append("\n");
        csv.append("Most Common Reason,").append(summary.getMostCommonReason()).append("\n");
        csv.append("Least Common Reason,").append(summary.getLeastCommonReason()).append("\n");
        csv.append("Total Hours,").append(String.format("%.2f", summary.getTotalHours())).append("\n");
        csv.append("\n");

        // Column headers
        csv.append("Reason,Count,Percentage,Total Hours,Avg Hours/Assignment\n");

        // Data rows
        for (com.heronix.model.dto.AbsenceReasonsReport.AbsenceReasonRow row : report.getReasons()) {
            csv.append(escapeCSV(row.getReasonName())).append(",");
            csv.append(row.getCount()).append(",");
            csv.append(String.format("%.2f%%", row.getPercentage())).append(",");
            csv.append(String.format("%.2f", row.getTotalHours())).append(",");
            csv.append(String.format("%.2f", row.getAverageHoursPerAssignment())).append("\n");
        }

        return csv.toString();
    }

    /**
     * Escape CSV special characters
     */
    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Handle edit assignment
     */
    private void handleEditAssignment(SubstituteAssignment assignment) {
        openAssignmentForm(assignment);
    }

    /**
     * Handle delete assignment by ID (used from DTO table actions)
     */
    private void handleDeleteAssignmentById(Long assignmentId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Assignment");
        alert.setHeaderText("Delete Assignment");
        alert.setContentText("Are you sure you want to delete this assignment?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            substituteManagementService.deleteAssignment(assignmentId);
            loadAssignments();
            loadStatistics();
        }
    }

    /**
     * Handle edit substitute
     */
    private void handleEditSubstitute(Substitute substitute) {
        Dialog<Substitute> dialog = createSubstituteDialog(substitute);
        Optional<Substitute> result = dialog.showAndWait();

        result.ifPresent(updated -> {
            try {
                Substitute saved = substituteManagementService.saveSubstitute(updated);
                showInfo("Success", "Substitute " + saved.getFirstName() + " " + saved.getLastName() + " updated successfully!");
                loadSubstitutes();
            } catch (Exception e) {
                logger.error("Error updating substitute", e);
                showError("Error", "Failed to update substitute: " + e.getMessage());
            }
        });
    }

    /**
     * Handle view substitute
     */
    private void handleViewSubstitute(Substitute substitute) {
        showInfo("View Substitute", "Viewing assignments for: " + substitute.getFullName());
    }

    /**
     * Open assignment form dialog
     */
    private void openAssignmentForm(SubstituteAssignment assignment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SubstituteAssignmentForm.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            SubstituteAssignmentController controller = loader.getController();
            if (assignment != null) {
                controller.setAssignment(assignment);
            }
            controller.setOnSaveCallback(() -> {
                loadAssignments();
                loadStatistics();
            });

            Stage stage = new Stage();
            stage.setTitle(assignment == null ? "New Assignment" : "Edit Assignment");

            // Create scene with reasonable size
            Scene scene = new Scene(root);
            stage.setScene(scene);

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);
            ResponsiveDesignHelper.makeLargeDialogResponsive(stage);
            stage.show();

        } catch (Exception e) {
            logger.error("Error opening assignment form", e);
            showError("Error", "Could not open assignment form: " + e.getMessage());
        }
    }

    /**
     * Show error message
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show info message
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Create substitute dialog for add/edit operations
     */
    private Dialog<Substitute> createSubstituteDialog(Substitute existingSub) {
        Dialog<Substitute> dialog = new Dialog<>();
        dialog.setTitle(existingSub == null ? "Add New Substitute" : "Edit Substitute");
        dialog.setHeaderText(existingSub == null ? "Enter substitute information" : "Modify substitute information");

        // Set button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create form layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Form fields
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First name");

        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last name");

        TextField employeeIdField = new TextField();
        employeeIdField.setPromptText("Employee ID (optional)");

        TextField emailField = new TextField();
        emailField.setPromptText("email@example.com");

        TextField phoneField = new TextField();
        phoneField.setPromptText("(555) 123-4567");

        ComboBox<SubstituteType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(SubstituteType.values());
        typeCombo.setPromptText("Select type");

        TextField availabilityField = new TextField();
        availabilityField.setPromptText("e.g., Full-time, Mornings only");

        TextField hourlyRateField = new TextField();
        hourlyRateField.setPromptText("0.00");

        TextField dailyRateField = new TextField();
        dailyRateField.setPromptText("0.00");

        TextArea certificationsArea = new TextArea();
        certificationsArea.setPromptText("Enter certifications (one per line)");
        certificationsArea.setPrefRowCount(3);

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Notes, preferences, restrictions");
        notesArea.setPrefRowCount(3);

        CheckBox activeCheckBox = new CheckBox("Active");
        activeCheckBox.setSelected(true);

        // Populate if editing
        if (existingSub != null) {
            firstNameField.setText(existingSub.getFirstName());
            lastNameField.setText(existingSub.getLastName());
            employeeIdField.setText(existingSub.getEmployeeId());
            emailField.setText(existingSub.getEmail());
            phoneField.setText(existingSub.getPhoneNumber());
            typeCombo.setValue(existingSub.getType());
            availabilityField.setText(existingSub.getAvailability());
            if (existingSub.getHourlyRate() != null) {
                hourlyRateField.setText(String.valueOf(existingSub.getHourlyRate()));
            }
            if (existingSub.getDailyRate() != null) {
                dailyRateField.setText(String.valueOf(existingSub.getDailyRate()));
            }
            if (existingSub.getCertifications() != null && !existingSub.getCertifications().isEmpty()) {
                certificationsArea.setText(String.join("\n", existingSub.getCertifications()));
            }
            notesArea.setText(existingSub.getNotes());
            activeCheckBox.setSelected(existingSub.getActive());
        }

        // Add fields to grid
        int row = 0;
        grid.add(new Label("First Name: *"), 0, row);
        grid.add(firstNameField, 1, row);
        row++;

        grid.add(new Label("Last Name: *"), 0, row);
        grid.add(lastNameField, 1, row);
        row++;

        grid.add(new Label("Employee ID:"), 0, row);
        grid.add(employeeIdField, 1, row);
        row++;

        grid.add(new Label("Type: *"), 0, row);
        grid.add(typeCombo, 1, row);
        row++;

        grid.add(new Label("Email:"), 0, row);
        grid.add(emailField, 1, row);
        row++;

        grid.add(new Label("Phone:"), 0, row);
        grid.add(phoneField, 1, row);
        row++;

        grid.add(new Label("Availability:"), 0, row);
        grid.add(availabilityField, 1, row);
        row++;

        grid.add(new Label("Hourly Rate:"), 0, row);
        grid.add(hourlyRateField, 1, row);
        row++;

        grid.add(new Label("Daily Rate:"), 0, row);
        grid.add(dailyRateField, 1, row);
        row++;

        grid.add(new Label("Certifications:"), 0, row);
        grid.add(certificationsArea, 1, row);
        row++;

        grid.add(new Label("Notes:"), 0, row);
        grid.add(notesArea, 1, row);
        row++;

        grid.add(activeCheckBox, 1, row);

        // Wrap grid in ScrollPane for better layout with large forms
        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(600, 500);  // Set reasonable size
        scrollPane.setMaxSize(600, 600);   // Prevent excessive growth

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().setPrefSize(650, 550);  // Set dialog size

        // Validation and result converter
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        // Enable save button when required fields are filled
        firstNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty() || lastNameField.getText().trim().isEmpty() || typeCombo.getValue() == null);
        });
        lastNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty() || firstNameField.getText().trim().isEmpty() || typeCombo.getValue() == null);
        });
        typeCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue == null || firstNameField.getText().trim().isEmpty() || lastNameField.getText().trim().isEmpty());
        });

        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    Substitute sub = existingSub != null ? existingSub : new Substitute();

                    sub.setFirstName(firstNameField.getText().trim());
                    sub.setLastName(lastNameField.getText().trim());
                    sub.setEmployeeId(employeeIdField.getText().trim().isEmpty() ? null : employeeIdField.getText().trim());
                    sub.setEmail(emailField.getText().trim().isEmpty() ? null : emailField.getText().trim());
                    sub.setPhoneNumber(phoneField.getText().trim().isEmpty() ? null : phoneField.getText().trim());
                    sub.setType(typeCombo.getValue());
                    sub.setAvailability(availabilityField.getText().trim().isEmpty() ? null : availabilityField.getText().trim());

                    // Parse rates
                    if (!hourlyRateField.getText().trim().isEmpty()) {
                        try {
                            sub.setHourlyRate(Double.parseDouble(hourlyRateField.getText().trim()));
                        } catch (NumberFormatException e) {
                            // Invalid number, skip
                        }
                    }

                    if (!dailyRateField.getText().trim().isEmpty()) {
                        try {
                            sub.setDailyRate(Double.parseDouble(dailyRateField.getText().trim()));
                        } catch (NumberFormatException e) {
                            // Invalid number, skip
                        }
                    }

                    // Parse certifications
                    if (!certificationsArea.getText().trim().isEmpty()) {
                        Set<String> certs = new HashSet<>();
                        for (String line : certificationsArea.getText().split("\n")) {
                            if (!line.trim().isEmpty()) {
                                certs.add(line.trim());
                            }
                        }
                        sub.setCertifications(certs);
                    }

                    sub.setNotes(notesArea.getText().trim().isEmpty() ? null : notesArea.getText().trim());
                    sub.setActive(activeCheckBox.isSelected());

                    return sub;
                } catch (Exception e) {
                    logger.error("Error converting dialog result", e);
                    return null;
                }
            }
            return null;
        });

        // Request focus on first field
        javafx.application.Platform.runLater(() -> firstNameField.requestFocus());

        return dialog;
    }
}
