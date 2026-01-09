package com.heronix.ui.controller;

import com.heronix.model.domain.AbsenceReasonConfig;
import com.heronix.service.AttendanceConfigurationService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AbsenceReasonsManagementController {

    @Autowired
    private AttendanceConfigurationService attendanceConfigurationService;

    // Left Panel - Reasons List
    @FXML private TextField searchField;
    @FXML private TableView<AbsenceReasonConfig> reasonsTableView;
    @FXML private TableColumn<AbsenceReasonConfig, String> codeColumn;
    @FXML private TableColumn<AbsenceReasonConfig, String> descriptionColumn;
    @FXML private TableColumn<AbsenceReasonConfig, String> categoryColumn;
    @FXML private TableColumn<AbsenceReasonConfig, String> excusedColumn;
    @FXML private TableColumn<AbsenceReasonConfig, Void> actionsColumn;
    @FXML private Label totalReasonsLabel;
    @FXML private Label excusedCountLabel;
    @FXML private Label unexcusedCountLabel;

    // Right Panel - Basic Information
    @FXML private TextField reasonCodeField;
    @FXML private TextField reasonDescriptionField;
    @FXML private TextArea longDescriptionArea;
    @FXML private CheckBox activeCheckBox;

    // Categorization
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private RadioButton excusedRadio;
    @FXML private RadioButton unexcusedRadio;
    @FXML private CheckBox countsTowardTruancyCheckBox;
    @FXML private CheckBox affectsAttendanceRateCheckBox;

    // Documentation Requirements
    @FXML private CheckBox requiresDocumentationCheckBox;
    @FXML private TextField documentTypeField;
    @FXML private TextField maxDaysWithoutDocsField;
    @FXML private CheckBox autoApproveCheckBox;

    // State Reporting
    @FXML private TextField stateCodeField;
    @FXML private TextField federalCodeField;
    @FXML private CheckBox includeInStateReportsCheckBox;
    @FXML private CheckBox chronicAbsenceIndicatorCheckBox;

    // Notifications & Actions
    @FXML private CheckBox notifyParentsCheckBox;
    @FXML private CheckBox notifyAdministrationCheckBox;
    @FXML private ComboBox<String> notificationTemplateComboBox;
    @FXML private CheckBox triggerInterventionCheckBox;

    // Additional Settings
    @FXML private TextField displayOrderField;
    @FXML private ColorPicker colorPicker;
    @FXML private TextField iconField;
    @FXML private TextArea notesArea;

    // Action Buttons
    @FXML private Button saveButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;

    private ObservableList<AbsenceReasonConfig> allReasons = FXCollections.observableArrayList();
    private ObservableList<AbsenceReasonConfig> filteredReasons = FXCollections.observableArrayList();
    private AbsenceReasonConfig selectedReason;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupComboBoxes();
        setupValidation();
        setupListeners();
        loadReasons();
        updateStatistics();
    }

    private void setupTableColumns() {
        codeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCode()));

        descriptionColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDescription()));

        categoryColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCategory()));

        excusedColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isExcused() ? "Excused" : "Unexcused"));

        // Actions column with Edit button
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");

            {
                editButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 10;");
                editButton.setOnAction(event -> {
                    AbsenceReasonConfig reason = getTableView().getItems().get(getIndex());
                    loadReasonToForm(reason);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, editButton);
                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });

        reasonsTableView.setItems(filteredReasons);
    }

    private void setupComboBoxes() {
        // Category options
        categoryComboBox.setItems(FXCollections.observableArrayList(
                "Medical/Health",
                "Personal/Family",
                "School Activity",
                "Legal/Court",
                "Religious Observance",
                "Bereavement",
                "Suspension/Disciplinary",
                "Remote Learning",
                "Weather/Emergency",
                "Other"
        ));

        // Notification templates
        notificationTemplateComboBox.setItems(FXCollections.observableArrayList(
                "Standard Absence Notification",
                "Excused Absence - Documentation Required",
                "Unexcused Absence - Warning",
                "Medical Absence - Extended",
                "Truancy Alert",
                "Early Warning - Attendance Concern",
                "Perfect Attendance Reminder",
                "Custom Template"
        ));

        // Set defaults
        categoryComboBox.setValue("Medical/Health");
        notificationTemplateComboBox.setValue("Standard Absence Notification");
    }

    private void setupValidation() {
        // Add numeric validation for display order and max days
        addNumericValidation(displayOrderField);
        addNumericValidation(maxDaysWithoutDocsField);

        // Validate code format (uppercase letters, numbers, underscores only)
        reasonCodeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !newVal.matches("[A-Z0-9_]+")) {
                reasonCodeField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            } else {
                reasonCodeField.setStyle("");
            }
        });

        // Auto-uppercase code field
        reasonCodeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.equals(newVal.toUpperCase())) {
                reasonCodeField.setText(newVal.toUpperCase());
            }
        });
    }

    private void addNumericValidation(TextField field) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !newVal.matches("\\d+")) {
                field.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            } else {
                field.setStyle("");
            }
        });
    }

    private void setupListeners() {
        // Search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterReasons(newVal);
        });

        // Table selection
        reasonsTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadReasonToForm(newVal);
            }
        });

        // Enable/disable fields based on checkboxes
        requiresDocumentationCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            documentTypeField.setDisable(!newVal);
            maxDaysWithoutDocsField.setDisable(!newVal);
            autoApproveCheckBox.setDisable(!newVal);
        });

        notifyParentsCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                notificationTemplateComboBox.setDisable(true);
            } else {
                notificationTemplateComboBox.setDisable(false);
            }
        });
    }

    private void loadReasons() {
        // Load sample reasons for demonstration
        // In production, this would load from database via service
        allReasons.clear();
        allReasons.addAll(createSampleReasons());
        filteredReasons.setAll(allReasons);
    }

    private List<AbsenceReasonConfig> createSampleReasons() {
        List<AbsenceReasonConfig> reasons = new ArrayList<>();

        reasons.add(AbsenceReasonConfig.builder()
                .code("ILL")
                .description("Illness")
                .longDescription("Student is ill and unable to attend school")
                .category("Medical/Health")
                .excused(true)
                .countsTowardTruancy(false)
                .affectsAttendanceRate(true)
                .requiresDocumentation(true)
                .documentType("Doctor's Note")
                .maxDaysWithoutDocs(3)
                .includeInStateReports(true)
                .chronicAbsenceIndicator(true)
                .notifyParents(true)
                .active(true)
                .displayOrder(1)
                .colorCode("#ffcdd2")
                .build());

        reasons.add(AbsenceReasonConfig.builder()
                .code("DOC")
                .description("Doctor/Medical Appointment")
                .longDescription("Student has a scheduled medical or dental appointment")
                .category("Medical/Health")
                .excused(true)
                .countsTowardTruancy(false)
                .affectsAttendanceRate(true)
                .requiresDocumentation(true)
                .documentType("Doctor's Note or Appointment Card")
                .maxDaysWithoutDocs(0)
                .autoApprove(true)
                .includeInStateReports(true)
                .chronicAbsenceIndicator(true)
                .notifyParents(true)
                .active(true)
                .displayOrder(2)
                .colorCode("#e1bee7")
                .build());

        reasons.add(AbsenceReasonConfig.builder()
                .code("FUN")
                .description("Funeral/Bereavement")
                .longDescription("Student is absent due to death in the family")
                .category("Bereavement")
                .excused(true)
                .countsTowardTruancy(false)
                .affectsAttendanceRate(true)
                .requiresDocumentation(false)
                .includeInStateReports(true)
                .chronicAbsenceIndicator(true)
                .notifyParents(false)
                .notifyAdministration(true)
                .active(true)
                .displayOrder(3)
                .colorCode("#d7ccc8")
                .build());

        reasons.add(AbsenceReasonConfig.builder()
                .code("UNX")
                .description("Unexcused Absence")
                .longDescription("Student absent without valid excuse or documentation")
                .category("Other")
                .excused(false)
                .countsTowardTruancy(true)
                .affectsAttendanceRate(true)
                .requiresDocumentation(false)
                .includeInStateReports(true)
                .chronicAbsenceIndicator(true)
                .notifyParents(true)
                .notifyAdministration(true)
                .triggerIntervention(true)
                .active(true)
                .displayOrder(10)
                .colorCode("#ef9a9a")
                .build());

        reasons.add(AbsenceReasonConfig.builder()
                .code("FLD")
                .description("Field Trip")
                .longDescription("Student on school-sponsored field trip")
                .category("School Activity")
                .excused(true)
                .countsTowardTruancy(false)
                .affectsAttendanceRate(false)
                .requiresDocumentation(false)
                .autoExcuseSchoolActivities(true)
                .includeInStateReports(false)
                .chronicAbsenceIndicator(false)
                .notifyParents(false)
                .active(true)
                .displayOrder(4)
                .colorCode("#c8e6c9")
                .build());

        reasons.add(AbsenceReasonConfig.builder()
                .code("SUS")
                .description("Suspension")
                .longDescription("Student suspended from school")
                .category("Suspension/Disciplinary")
                .excused(false)
                .countsTowardTruancy(true)
                .affectsAttendanceRate(true)
                .requiresDocumentation(false)
                .includeInStateReports(true)
                .chronicAbsenceIndicator(true)
                .notifyParents(true)
                .notifyAdministration(true)
                .active(true)
                .displayOrder(11)
                .colorCode("#ffab91")
                .build());

        reasons.add(AbsenceReasonConfig.builder()
                .code("REL")
                .description("Religious Observance")
                .longDescription("Student absent for religious holiday or observance")
                .category("Religious Observance")
                .excused(true)
                .countsTowardTruancy(false)
                .affectsAttendanceRate(true)
                .requiresDocumentation(false)
                .includeInStateReports(true)
                .chronicAbsenceIndicator(true)
                .notifyParents(false)
                .active(true)
                .displayOrder(5)
                .colorCode("#b3e5fc")
                .build());

        reasons.add(AbsenceReasonConfig.builder()
                .code("CRT")
                .description("Court Appearance")
                .longDescription("Student has mandatory court appearance")
                .category("Legal/Court")
                .excused(true)
                .countsTowardTruancy(false)
                .affectsAttendanceRate(true)
                .requiresDocumentation(true)
                .documentType("Court Summons or Documentation")
                .maxDaysWithoutDocs(0)
                .includeInStateReports(true)
                .chronicAbsenceIndicator(true)
                .notifyParents(false)
                .notifyAdministration(true)
                .active(true)
                .displayOrder(6)
                .colorCode("#ffecb3")
                .build());

        return reasons;
    }

    private void filterReasons(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredReasons.setAll(allReasons);
        } else {
            String lowerSearch = searchText.toLowerCase();
            filteredReasons.setAll(allReasons.stream()
                    .filter(reason ->
                            reason.getCode().toLowerCase().contains(lowerSearch) ||
                            reason.getDescription().toLowerCase().contains(lowerSearch) ||
                            reason.getCategory().toLowerCase().contains(lowerSearch))
                    .collect(Collectors.toList()));
        }
        updateStatistics();
    }

    private void updateStatistics() {
        totalReasonsLabel.setText(String.valueOf(filteredReasons.size()));

        long excusedCount = filteredReasons.stream()
                .filter(AbsenceReasonConfig::isExcused)
                .count();
        excusedCountLabel.setText(String.valueOf(excusedCount));

        long unexcusedCount = filteredReasons.size() - excusedCount;
        unexcusedCountLabel.setText(String.valueOf(unexcusedCount));
    }

    private void loadReasonToForm(AbsenceReasonConfig reason) {
        selectedReason = reason;

        // Basic Information
        reasonCodeField.setText(reason.getCode());
        reasonDescriptionField.setText(reason.getDescription());
        longDescriptionArea.setText(reason.getLongDescription());
        activeCheckBox.setSelected(reason.isActive());

        // Categorization
        categoryComboBox.setValue(reason.getCategory());
        if (reason.isExcused()) {
            excusedRadio.setSelected(true);
        } else {
            unexcusedRadio.setSelected(true);
        }
        countsTowardTruancyCheckBox.setSelected(reason.isCountsTowardTruancy());
        affectsAttendanceRateCheckBox.setSelected(reason.isAffectsAttendanceRate());

        // Documentation Requirements
        requiresDocumentationCheckBox.setSelected(reason.isRequiresDocumentation());
        documentTypeField.setText(reason.getDocumentType());
        maxDaysWithoutDocsField.setText(reason.getMaxDaysWithoutDocs() != null ?
                String.valueOf(reason.getMaxDaysWithoutDocs()) : "");
        autoApproveCheckBox.setSelected(reason.isAutoApprove());

        // State Reporting
        stateCodeField.setText(reason.getStateCode());
        federalCodeField.setText(reason.getFederalCode());
        includeInStateReportsCheckBox.setSelected(reason.isIncludeInStateReports());
        chronicAbsenceIndicatorCheckBox.setSelected(reason.isChronicAbsenceIndicator());

        // Notifications & Actions
        notifyParentsCheckBox.setSelected(reason.isNotifyParents());
        notifyAdministrationCheckBox.setSelected(reason.isNotifyAdministration());
        if (reason.getNotificationTemplate() != null) {
            notificationTemplateComboBox.setValue(reason.getNotificationTemplate());
        }
        triggerInterventionCheckBox.setSelected(reason.isTriggerIntervention());

        // Additional Settings
        displayOrderField.setText(reason.getDisplayOrder() != null ?
                String.valueOf(reason.getDisplayOrder()) : "");

        if (reason.getColorCode() != null && !reason.getColorCode().isEmpty()) {
            try {
                colorPicker.setValue(Color.web(reason.getColorCode()));
            } catch (Exception e) {
                colorPicker.setValue(Color.WHITE);
            }
        }

        iconField.setText(reason.getIcon());
        notesArea.setText(reason.getNotes());

        // Enable delete button
        deleteButton.setDisable(false);
    }

    @FXML
    private void handleAddReason() {
        handleClear();
        reasonCodeField.requestFocus();
    }

    @FXML
    private void handleSave() {
        List<String> errors = validateForm();
        if (!errors.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please correct the following errors:");
            alert.setContentText(String.join("\n", errors));
            alert.showAndWait();
            return;
        }

        try {
            AbsenceReasonConfig reason = buildReasonFromForm();

            // In production, save to database via service
            // attendanceConfigurationService.saveAbsenceReasonConfig(reason);

            // Update local list
            if (selectedReason != null) {
                allReasons.remove(selectedReason);
            }
            allReasons.add(reason);
            filterReasons(searchField.getText());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Absence Reason Saved");
            alert.setContentText("Absence reason '" + reason.getCode() + " - " + reason.getDescription() +
                    "' has been saved successfully.\n\n" +
                    "Type: " + (reason.isExcused() ? "Excused" : "Unexcused") + "\n" +
                    "Category: " + reason.getCategory() + "\n" +
                    "Counts Toward Truancy: " + (reason.isCountsTowardTruancy() ? "Yes" : "No"));
            alert.showAndWait();

            handleClear();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to save absence reason");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedReason == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Reason Selected");
            alert.setContentText("Please select an absence reason to delete.");
            alert.showAndWait();
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete Absence Reason");
        confirmation.setContentText("Are you sure you want to delete the absence reason '" +
                selectedReason.getCode() + " - " + selectedReason.getDescription() + "'?\n\n" +
                "This action cannot be undone and may affect historical attendance records.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // In production, delete from database via service
                    // attendanceConfigurationService.deleteAbsenceReasonConfig(selectedReason.getId());

                    allReasons.remove(selectedReason);
                    filterReasons(searchField.getText());

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Deleted");
                    alert.setHeaderText("Absence Reason Deleted");
                    alert.setContentText("The absence reason has been deleted successfully.");
                    alert.showAndWait();

                    handleClear();
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Failed to delete absence reason");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            }
        });
    }

    @FXML
    private void handleClear() {
        selectedReason = null;

        // Clear Basic Information
        reasonCodeField.clear();
        reasonDescriptionField.clear();
        longDescriptionArea.clear();
        activeCheckBox.setSelected(true);

        // Clear Categorization
        categoryComboBox.setValue("Medical/Health");
        excusedRadio.setSelected(true);
        countsTowardTruancyCheckBox.setSelected(false);
        affectsAttendanceRateCheckBox.setSelected(true);

        // Clear Documentation Requirements
        requiresDocumentationCheckBox.setSelected(false);
        documentTypeField.clear();
        maxDaysWithoutDocsField.clear();
        autoApproveCheckBox.setSelected(false);

        // Clear State Reporting
        stateCodeField.clear();
        federalCodeField.clear();
        includeInStateReportsCheckBox.setSelected(true);
        chronicAbsenceIndicatorCheckBox.setSelected(true);

        // Clear Notifications & Actions
        notifyParentsCheckBox.setSelected(true);
        notifyAdministrationCheckBox.setSelected(false);
        notificationTemplateComboBox.setValue("Standard Absence Notification");
        triggerInterventionCheckBox.setSelected(false);

        // Clear Additional Settings
        displayOrderField.clear();
        colorPicker.setValue(Color.WHITE);
        iconField.clear();
        notesArea.clear();

        // Disable delete button
        deleteButton.setDisable(true);

        // Clear table selection
        reasonsTableView.getSelectionModel().clearSelection();
    }

    private List<String> validateForm() {
        List<String> errors = new ArrayList<>();

        // Validate required fields
        if (reasonCodeField.getText().trim().isEmpty()) {
            errors.add("Reason Code is required");
        } else {
            String code = reasonCodeField.getText().trim();
            if (!code.matches("[A-Z0-9_]+")) {
                errors.add("Reason Code must contain only uppercase letters, numbers, and underscores");
            }
            if (code.length() > 10) {
                errors.add("Reason Code must be 10 characters or less");
            }

            // Check for duplicate code
            boolean isDuplicate = allReasons.stream()
                    .anyMatch(r -> r.getCode().equals(code) && r != selectedReason);
            if (isDuplicate) {
                errors.add("Reason Code '" + code + "' already exists");
            }
        }

        if (reasonDescriptionField.getText().trim().isEmpty()) {
            errors.add("Description is required");
        }

        if (categoryComboBox.getValue() == null) {
            errors.add("Category is required");
        }

        // Validate numeric fields
        if (!displayOrderField.getText().trim().isEmpty()) {
            try {
                int order = Integer.parseInt(displayOrderField.getText().trim());
                if (order < 0) {
                    errors.add("Display Order must be a positive number");
                }
            } catch (NumberFormatException e) {
                errors.add("Display Order must be a valid number");
            }
        }

        if (!maxDaysWithoutDocsField.getText().trim().isEmpty()) {
            try {
                int days = Integer.parseInt(maxDaysWithoutDocsField.getText().trim());
                if (days < 0) {
                    errors.add("Max Days Without Docs must be a positive number");
                }
            } catch (NumberFormatException e) {
                errors.add("Max Days Without Docs must be a valid number");
            }
        }

        // Validate documentation requirements
        if (requiresDocumentationCheckBox.isSelected()) {
            if (documentTypeField.getText().trim().isEmpty()) {
                errors.add("Document Type is required when documentation is required");
            }
        }

        return errors;
    }

    private AbsenceReasonConfig buildReasonFromForm() {
        return AbsenceReasonConfig.builder()
                .code(reasonCodeField.getText().trim())
                .description(reasonDescriptionField.getText().trim())
                .longDescription(longDescriptionArea.getText().trim())
                .active(activeCheckBox.isSelected())
                .category(categoryComboBox.getValue())
                .excused(excusedRadio.isSelected())
                .countsTowardTruancy(countsTowardTruancyCheckBox.isSelected())
                .affectsAttendanceRate(affectsAttendanceRateCheckBox.isSelected())
                .requiresDocumentation(requiresDocumentationCheckBox.isSelected())
                .documentType(documentTypeField.getText().trim())
                .maxDaysWithoutDocs(!maxDaysWithoutDocsField.getText().trim().isEmpty() ?
                        Integer.parseInt(maxDaysWithoutDocsField.getText().trim()) : null)
                .autoApprove(autoApproveCheckBox.isSelected())
                .stateCode(stateCodeField.getText().trim())
                .federalCode(federalCodeField.getText().trim())
                .includeInStateReports(includeInStateReportsCheckBox.isSelected())
                .chronicAbsenceIndicator(chronicAbsenceIndicatorCheckBox.isSelected())
                .notifyParents(notifyParentsCheckBox.isSelected())
                .notifyAdministration(notifyAdministrationCheckBox.isSelected())
                .notificationTemplate(notificationTemplateComboBox.getValue())
                .triggerIntervention(triggerInterventionCheckBox.isSelected())
                .displayOrder(!displayOrderField.getText().trim().isEmpty() ?
                        Integer.parseInt(displayOrderField.getText().trim()) : null)
                .colorCode(colorPicker.getValue() != null ?
                        toHexString(colorPicker.getValue()) : null)
                .icon(iconField.getText().trim())
                .notes(notesArea.getText().trim())
                .build();
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}
