package com.heronix.ui.controller;

import com.heronix.client.TransferAuthorizationApiService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Transfer Authorization Dialog Controller
 *
 * Dialog for creating and viewing HSTP transfer authorizations.
 * Provides student selection, destination form, and signature status panel.
 *
 * @author Heronix Development Team
 * @version 1.0.0
 * @since 2026-02 - HSTP Implementation
 */
@Slf4j
@Component
public class TransferAuthorizationDialogController {

    // ========================================================================
    // FXML FIELDS
    // ========================================================================

    @FXML private Label titleLabel;
    @FXML private Label statusLabel;

    // Authorization Details
    @FXML private Label authNumberLabel;
    @FXML private ComboBox<String> reasonCodeCombo;
    @FXML private TextArea reasonDetailsArea;
    @FXML private ComboBox<String> deliveryMethodCombo;

    // Destination School
    @FXML private TextField destSchoolNameField;
    @FXML private TextField destDistrictField;
    @FXML private TextField destCityField;
    @FXML private TextField destStateField;
    @FXML private TextField destContactNameField;
    @FXML private TextField destContactEmailField;
    @FXML private TextField destContactPhoneField;

    // Student Selection
    @FXML private TableView<Map<String, Object>> studentTable;
    @FXML private TableColumn<Map<String, Object>, String> studentIdColumn;
    @FXML private TableColumn<Map<String, Object>, String> firstNameColumn;
    @FXML private TableColumn<Map<String, Object>, String> lastNameColumn;
    @FXML private TableColumn<Map<String, Object>, String> gradeLevelColumn;
    @FXML private TableColumn<Map<String, Object>, Boolean> selectedColumn;

    // Signature Status
    @FXML private Label registrarSignLabel;
    @FXML private Label principalSignLabel;
    @FXML private Label adminSignLabel;

    // Action Buttons
    @FXML private Button submitButton;
    @FXML private Button signButton;
    @FXML private Button packageButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    @FXML private Label errorLabel;

    // ========================================================================
    // DEPENDENCIES
    // ========================================================================

    @Autowired
    private TransferAuthorizationApiService transferApiService;

    // ========================================================================
    // STATE
    // ========================================================================

    private Long authorizationId;
    private boolean isEditMode = false;
    private final ObservableList<Map<String, Object>> studentData = FXCollections.observableArrayList();

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    @FXML
    public void initialize() {
        // Set up combo boxes
        reasonCodeCombo.setItems(FXCollections.observableArrayList(
                "STUDENT_TRANSFER", "DISTRICT_MIGRATION", "RECORDS_REQUEST",
                "COURT_ORDER", "EMERGENCY_TRANSFER", "INTER_DISTRICT"
        ));
        deliveryMethodCombo.setItems(FXCollections.observableArrayList(
                "USB_ENCRYPTED", "SECURE_FILE_TRANSFER"
        ));

        // Set up student table columns
        studentIdColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        gradeLevelColumn.setCellValueFactory(new PropertyValueFactory<>("gradeLevel"));
        studentTable.setItems(studentData);

        // Default signature status
        updateSignatureStatus(List.of());

        log.info("TransferAuthorizationDialog initialized");
    }

    // ========================================================================
    // PUBLIC API
    // ========================================================================

    /**
     * Set up for creating a new authorization.
     */
    public void setCreateMode() {
        isEditMode = false;
        titleLabel.setText("New Transfer Authorization");
        authNumberLabel.setText("Will be assigned on save");
        statusLabel.setText("DRAFT");
        enableEditing(true);
    }

    /**
     * Set up for viewing an existing authorization.
     */
    public void setViewMode(Long authorizationId) {
        this.authorizationId = authorizationId;
        isEditMode = true;
        titleLabel.setText("Transfer Authorization");
        loadAuthorization(authorizationId);
        enableEditing(false);
    }

    // ========================================================================
    // ACTIONS
    // ========================================================================

    @FXML
    private void handleSave() {
        try {
            clearError();
            if (!validateForm()) return;

            List<Long> studentIds = studentData.stream()
                    .filter(s -> Boolean.TRUE.equals(s.get("selected")))
                    .map(s -> ((Number) s.get("id")).longValue())
                    .toList();

            Map<String, Object> request = new HashMap<>();
            request.put("studentIds", studentIds);
            request.put("reasonCode", reasonCodeCombo.getValue());
            request.put("reasonDetails", reasonDetailsArea.getText());
            request.put("deliveryMethod", deliveryMethodCombo.getValue());
            request.put("destinationSchoolName", destSchoolNameField.getText());
            request.put("destinationSchoolDistrict", destDistrictField.getText());
            request.put("destinationSchoolCity", destCityField.getText());
            request.put("destinationSchoolState", destStateField.getText());
            request.put("destinationContactName", destContactNameField.getText());
            request.put("destinationContactEmail", destContactEmailField.getText());
            request.put("destinationContactPhone", destContactPhoneField.getText());

            Map<String, Object> result = transferApiService.createAuthorization(request);
            authorizationId = ((Number) result.get("id")).longValue();

            showInfo("Authorization created: " + result.get("authorizationNumber"));
            statusLabel.setText("DRAFT");
            authNumberLabel.setText(String.valueOf(result.get("authorizationNumber")));

        } catch (Exception e) {
            showError("Failed to save: " + e.getMessage());
            log.error("Error saving authorization", e);
        }
    }

    @FXML
    private void handleSubmit() {
        if (authorizationId == null) {
            showError("Save the authorization first");
            return;
        }
        try {
            Map<String, Object> result = transferApiService.submitForSignatures(authorizationId);
            statusLabel.setText("PENDING_SIGNATURES");
            showInfo("Submitted for signatures");
        } catch (Exception e) {
            showError("Failed to submit: " + e.getMessage());
        }
    }

    @FXML
    private void handleSign() {
        if (authorizationId == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TransferSigningDialog.fxml"));
            VBox root = loader.load();
            TransferSigningDialogController controller = loader.getController();
            controller.setAuthorization(authorizationId);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Sign Transfer Authorization");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh after signing
            loadAuthorization(authorizationId);

        } catch (Exception e) {
            showError("Failed to open signing dialog: " + e.getMessage());
            log.error("Error opening signing dialog", e);
        }
    }

    @FXML
    private void handlePackage() {
        if (authorizationId == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TransferPackageDialog.fxml"));
            VBox root = loader.load();
            TransferPackageDialogController controller = loader.getController();
            controller.setAuthorization(authorizationId);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Transfer Package");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (Exception e) {
            showError("Failed to open package dialog: " + e.getMessage());
            log.error("Error opening package dialog", e);
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    // ========================================================================
    // HELPERS
    // ========================================================================

    private void loadAuthorization(Long id) {
        try {
            Map<String, Object> auth = transferApiService.getAuthorization(id);
            authNumberLabel.setText(String.valueOf(auth.get("authorizationNumber")));
            statusLabel.setText(String.valueOf(auth.get("status")));
            reasonCodeCombo.setValue(String.valueOf(auth.get("reasonCode")));
            reasonDetailsArea.setText(String.valueOf(auth.getOrDefault("reasonDetails", "")));
            deliveryMethodCombo.setValue(String.valueOf(auth.get("deliveryMethod")));
            destSchoolNameField.setText(String.valueOf(auth.getOrDefault("destinationSchoolName", "")));
            destDistrictField.setText(String.valueOf(auth.getOrDefault("destinationSchoolDistrict", "")));
            destCityField.setText(String.valueOf(auth.getOrDefault("destinationSchoolCity", "")));
            destStateField.setText(String.valueOf(auth.getOrDefault("destinationSchoolState", "")));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> signatures = (List<Map<String, Object>>) auth.getOrDefault("signatures", List.of());
            updateSignatureStatus(signatures);

        } catch (Exception e) {
            showError("Failed to load authorization: " + e.getMessage());
        }
    }

    private void updateSignatureStatus(List<Map<String, Object>> signatures) {
        String pending = "Pending";
        String signed = "SIGNED";

        registrarSignLabel.setText(pending);
        principalSignLabel.setText(pending);
        adminSignLabel.setText(pending);

        for (Map<String, Object> sig : signatures) {
            String role = String.valueOf(sig.get("signerRole"));
            String name = String.valueOf(sig.get("signerFullName"));
            switch (role) {
                case "REGISTRAR" -> registrarSignLabel.setText(signed + " — " + name);
                case "PRINCIPAL" -> principalSignLabel.setText(signed + " — " + name);
                case "ADMIN" -> adminSignLabel.setText(signed + " — " + name);
            }
        }
    }

    private void enableEditing(boolean enabled) {
        reasonCodeCombo.setDisable(!enabled);
        reasonDetailsArea.setDisable(!enabled);
        deliveryMethodCombo.setDisable(!enabled);
        destSchoolNameField.setDisable(!enabled);
        destDistrictField.setDisable(!enabled);
        destCityField.setDisable(!enabled);
        destStateField.setDisable(!enabled);
        destContactNameField.setDisable(!enabled);
        destContactEmailField.setDisable(!enabled);
        destContactPhoneField.setDisable(!enabled);
    }

    private boolean validateForm() {
        if (reasonCodeCombo.getValue() == null) {
            showError("Please select a reason code");
            return false;
        }
        if (deliveryMethodCombo.getValue() == null) {
            showError("Please select a delivery method");
            return false;
        }
        if (destSchoolNameField.getText() == null || destSchoolNameField.getText().isBlank()) {
            showError("Destination school name is required");
            return false;
        }
        return true;
    }

    private void clearError() {
        if (errorLabel != null) errorLabel.setText("");
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            if (errorLabel != null) {
                errorLabel.setText(message);
                errorLabel.setStyle("-fx-text-fill: red;");
            }
        });
    }

    private void showInfo(String message) {
        Platform.runLater(() -> {
            if (errorLabel != null) {
                errorLabel.setText(message);
                errorLabel.setStyle("-fx-text-fill: green;");
            }
        });
    }
}
