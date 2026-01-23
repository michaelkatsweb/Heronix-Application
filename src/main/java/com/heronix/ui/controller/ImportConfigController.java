package com.heronix.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.springframework.stereotype.Component;

/**
 * Controller for the Import Configuration Dialog
 * Handles column mapping for CSV imports
 */
@Component
public class ImportConfigController {

    @FXML private RadioButton autoDetectRadio;
    @FXML private RadioButton studentRadio;
    @FXML private RadioButton teacherRadio;
    @FXML private RadioButton courseRadio;
    @FXML private RadioButton roomRadio;
    @FXML private ToggleGroup entityTypeGroup;

    @FXML private GridPane mappingGrid;
    @FXML private TableView<?> previewTable;

    @FXML private CheckBox skipHeaderCheckBox;
    @FXML private CheckBox skipDuplicatesCheckBox;
    @FXML private CheckBox validateDataCheckBox;

    @FXML private Label totalRowsLabel;
    @FXML private Label mappedFieldsLabel;
    @FXML private Label unmappedFieldsLabel;

    @FXML
    public void initialize() {
        // Setup entity type change listener
        if (entityTypeGroup != null) {
            entityTypeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    updateMappingsForEntityType();
                }
            });
        }
    }

    /**
     * Update column mappings based on selected entity type
     */
    private void updateMappingsForEntityType() {
        // Clear existing mappings and rebuild based on entity type
        if (mappingGrid != null) {
            // Keep header row (row 0), remove others
            mappingGrid.getChildren().removeIf(node ->
                GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);
        }

        // Update statistics
        updateStatistics();
    }

    /**
     * Update statistics labels
     */
    private void updateStatistics() {
        if (totalRowsLabel != null) {
            totalRowsLabel.setText("Total Rows: 0");
        }
        if (mappedFieldsLabel != null) {
            mappedFieldsLabel.setText("Mapped Fields: 0");
        }
        if (unmappedFieldsLabel != null) {
            unmappedFieldsLabel.setText("Unmapped Fields: 0");
        }
    }

    /**
     * Set the CSV data for preview and mapping
     */
    public void setData(String[][] csvData) {
        if (csvData == null || csvData.length == 0) {
            return;
        }

        // Update preview table with first 5 rows
        // Update total rows count
        if (totalRowsLabel != null) {
            totalRowsLabel.setText("Total Rows: " + csvData.length);
        }
    }

    /**
     * Get the current column mappings
     */
    public java.util.Map<String, String> getColumnMappings() {
        return new java.util.HashMap<>();
    }

    /**
     * Check if header row should be skipped
     */
    public boolean shouldSkipHeader() {
        return skipHeaderCheckBox != null && skipHeaderCheckBox.isSelected();
    }

    /**
     * Check if duplicates should be skipped
     */
    public boolean shouldSkipDuplicates() {
        return skipDuplicatesCheckBox != null && skipDuplicatesCheckBox.isSelected();
    }

    /**
     * Check if data should be validated before import
     */
    public boolean shouldValidateData() {
        return validateDataCheckBox != null && validateDataCheckBox.isSelected();
    }

    /**
     * Get the selected entity type
     */
    public String getSelectedEntityType() {
        if (autoDetectRadio != null && autoDetectRadio.isSelected()) return "auto";
        if (studentRadio != null && studentRadio.isSelected()) return "student";
        if (teacherRadio != null && teacherRadio.isSelected()) return "teacher";
        if (courseRadio != null && courseRadio.isSelected()) return "course";
        if (roomRadio != null && roomRadio.isSelected()) return "room";
        return "auto";
    }
}
