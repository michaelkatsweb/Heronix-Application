package com.heronix.ui.controller;

import com.heronix.client.TransferAuthorizationApiService;
import com.heronix.security.LocalKeyStore;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Transfer Signing Dialog Controller
 *
 * Dialog for reviewing a transfer authorization and applying an Ed25519
 * digital signature. Supports both local key (client-side signing) and
 * server-side signing modes.
 *
 * @author Heronix Development Team
 * @version 1.0.0
 * @since 2026-02 - HSTP Implementation
 */
@Slf4j
@Component
public class TransferSigningDialogController {

    // ========================================================================
    // FXML FIELDS
    // ========================================================================

    @FXML private Label authNumberLabel;
    @FXML private Label statusLabel;
    @FXML private Label destinationLabel;
    @FXML private Label studentCountLabel;
    @FXML private Label reasonLabel;

    // Signature status
    @FXML private Label registrarSignLabel;
    @FXML private Label principalSignLabel;
    @FXML private Label adminSignLabel;

    // Signing form
    @FXML private ComboBox<String> keyCombo;
    @FXML private PasswordField passwordField;
    @FXML private TextArea remarksArea;

    // Result
    @FXML private Label resultLabel;

    // Buttons
    @FXML private Button signButton;
    @FXML private Button verifyButton;
    @FXML private Button closeButton;

    // ========================================================================
    // DEPENDENCIES
    // ========================================================================

    @Autowired
    private TransferAuthorizationApiService transferApiService;

    private final LocalKeyStore localKeyStore = new LocalKeyStore();

    // ========================================================================
    // STATE
    // ========================================================================

    private Long authorizationId;
    private Map<String, Object> authData;
    private String currentUsername;
    private Long currentUserId;

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    @FXML
    public void initialize() {
        log.info("TransferSigningDialog initialized");
    }

    // ========================================================================
    // PUBLIC API
    // ========================================================================

    /**
     * Set the authorization to sign and load its details.
     */
    public void setAuthorization(Long authorizationId) {
        this.authorizationId = authorizationId;
        loadAuthorizationDetails();
        loadLocalKeys();
    }

    /**
     * Set the current user context (username and ID).
     */
    public void setCurrentUser(String username, Long userId) {
        this.currentUsername = username;
        this.currentUserId = userId;
        loadLocalKeys();
    }

    // ========================================================================
    // ACTIONS
    // ========================================================================

    @FXML
    private void handleSign() {
        try {
            clearResult();

            String selectedKey = keyCombo.getValue();
            if (selectedKey == null || selectedKey.isBlank()) {
                showError("Please select a signing key");
                return;
            }

            String password = passwordField.getText();
            if (password == null || password.isBlank()) {
                showError("Please enter your password to unlock the key");
                return;
            }

            // Retrieve private key from local store
            String privateKeyBase64;
            try {
                privateKeyBase64 = localKeyStore.retrieveKey(currentUsername, selectedKey, password);
            } catch (Exception e) {
                showError("Failed to unlock key — wrong password?");
                return;
            }

            // Sign via server (sends private key over TLS)
            Map<String, Object> result = transferApiService.addSignatureServerSide(
                    authorizationId, currentUserId, privateKeyBase64, selectedKey,
                    remarksArea.getText());

            boolean quorumMet = Boolean.TRUE.equals(result.get("quorumMet"));
            String role = String.valueOf(result.get("signerRole"));

            if (quorumMet) {
                showSuccess("Signature accepted as " + role + ". QUORUM MET — all 3 roles have signed!");
            } else {
                showSuccess("Signature accepted as " + role + ".");
            }

            // Refresh the authorization display
            loadAuthorizationDetails();

            // Clear the password field
            passwordField.clear();

        } catch (Exception e) {
            showError("Signing failed: " + e.getMessage());
            log.error("Error signing authorization", e);
        }
    }

    @FXML
    private void handleVerify() {
        try {
            clearResult();
            Map<String, Object> result = transferApiService.verifySignatures(authorizationId);

            int valid = ((Number) result.getOrDefault("validSignatures", 0)).intValue();
            int total = ((Number) result.getOrDefault("totalSignatures", 0)).intValue();
            boolean quorumMet = Boolean.TRUE.equals(result.get("quorumMet"));

            String message = valid + "/" + total + " signatures verified. " +
                    (quorumMet ? "Quorum MET." : "Quorum NOT met.");
            if (valid == total && total > 0) {
                showSuccess(message);
            } else {
                showError(message);
            }

        } catch (Exception e) {
            showError("Verification failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    // ========================================================================
    // HELPERS
    // ========================================================================

    private void loadAuthorizationDetails() {
        try {
            authData = transferApiService.getAuthorization(authorizationId);

            authNumberLabel.setText(String.valueOf(authData.get("authorizationNumber")));
            statusLabel.setText(String.valueOf(authData.get("status")));
            destinationLabel.setText(String.valueOf(authData.getOrDefault("destinationSchoolName", "-")));
            reasonLabel.setText(String.valueOf(authData.getOrDefault("reasonCode", "-")));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> students = (List<Map<String, Object>>) authData.getOrDefault("students", List.of());
            studentCountLabel.setText(String.valueOf(students.size()));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> signatures = (List<Map<String, Object>>) authData.getOrDefault("signatures", List.of());
            updateSignatureLabels(signatures);

        } catch (Exception e) {
            showError("Failed to load authorization: " + e.getMessage());
        }
    }

    private void loadLocalKeys() {
        if (currentUsername == null) return;
        try {
            List<String> keys = localKeyStore.listKeys(currentUsername);
            keyCombo.getItems().clear();
            keyCombo.getItems().addAll(keys);
            if (!keys.isEmpty()) {
                keyCombo.setValue(keys.get(0));
            }
        } catch (Exception e) {
            log.warn("Could not list local keys: {}", e.getMessage());
        }
    }

    private void updateSignatureLabels(List<Map<String, Object>> signatures) {
        String pending = "Pending";
        registrarSignLabel.setText(pending);
        principalSignLabel.setText(pending);
        adminSignLabel.setText(pending);

        for (Map<String, Object> sig : signatures) {
            String role = String.valueOf(sig.get("signerRole"));
            String name = String.valueOf(sig.get("signerFullName"));
            String text = "SIGNED — " + name;
            switch (role) {
                case "REGISTRAR" -> registrarSignLabel.setText(text);
                case "PRINCIPAL" -> principalSignLabel.setText(text);
                case "ADMIN" -> adminSignLabel.setText(text);
            }
        }
    }

    private void clearResult() {
        if (resultLabel != null) resultLabel.setText("");
    }

    private void showError(String msg) {
        Platform.runLater(() -> {
            if (resultLabel != null) {
                resultLabel.setText(msg);
                resultLabel.setStyle("-fx-text-fill: red;");
            }
        });
    }

    private void showSuccess(String msg) {
        Platform.runLater(() -> {
            if (resultLabel != null) {
                resultLabel.setText(msg);
                resultLabel.setStyle("-fx-text-fill: green;");
            }
        });
    }
}
