package com.heronix.ui.controller;

import com.heronix.client.TransferAuthorizationApiService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Transfer Package Dialog Controller
 *
 * Dialog for generating the encrypted transfer package, downloading it
 * to USB or file, downloading PDF documentation, and confirming delivery.
 *
 * @author Heronix Development Team
 * @version 1.0.0
 * @since 2026-02 - HSTP Implementation
 */
@Slf4j
@Component
public class TransferPackageDialogController {

    // ========================================================================
    // FXML FIELDS
    // ========================================================================

    @FXML private Label authNumberLabel;
    @FXML private Label statusLabel;
    @FXML private Label packageHashLabel;
    @FXML private Label packageSizeLabel;

    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;

    // Buttons
    @FXML private Button generateButton;
    @FXML private Button downloadUsbButton;
    @FXML private Button downloadFileButton;
    @FXML private Button downloadDocsButton;
    @FXML private Button markDeliveredButton;
    @FXML private Button confirmReceiptButton;
    @FXML private Button closeButton;

    @FXML private Label resultLabel;

    // ========================================================================
    // DEPENDENCIES
    // ========================================================================

    @Autowired
    private TransferAuthorizationApiService transferApiService;

    // ========================================================================
    // STATE
    // ========================================================================

    private Long authorizationId;

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    @FXML
    public void initialize() {
        progressBar.setVisible(false);
        progressLabel.setVisible(false);
        log.info("TransferPackageDialog initialized");
    }

    // ========================================================================
    // PUBLIC API
    // ========================================================================

    public void setAuthorization(Long authorizationId) {
        this.authorizationId = authorizationId;
        loadAuthorizationInfo();
    }

    // ========================================================================
    // ACTIONS
    // ========================================================================

    @FXML
    private void handleGenerate() {
        showProgress("Generating encrypted package...");
        new Thread(() -> {
            try {
                Map<String, Object> result = transferApiService.generatePackage(authorizationId);
                Platform.runLater(() -> {
                    packageHashLabel.setText(String.valueOf(result.getOrDefault("sha256Hash", "-")));
                    packageSizeLabel.setText(formatFileSize(
                            ((Number) result.getOrDefault("sizeBytes", 0)).longValue()));
                    statusLabel.setText(String.valueOf(result.getOrDefault("status", "-")));
                    hideProgress();
                    showSuccess("Package generated successfully");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    hideProgress();
                    showError("Generation failed: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleDownloadUsb() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select USB Drive or Destination Folder");
        Stage stage = (Stage) closeButton.getScene().getWindow();
        File selectedDir = chooser.showDialog(stage);

        if (selectedDir == null) return;

        showProgress("Downloading package to USB...");
        new Thread(() -> {
            try {
                byte[] packageBytes = transferApiService.downloadPackage(authorizationId);
                Path destPath = selectedDir.toPath().resolve(
                        authNumberLabel.getText().replace("/", "-") + ".heronix");
                Files.write(destPath, packageBytes);

                Platform.runLater(() -> {
                    hideProgress();
                    showSuccess("Package saved to: " + destPath);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    hideProgress();
                    showError("Download failed: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleDownloadFile() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Destination Folder");
        Stage stage = (Stage) closeButton.getScene().getWindow();
        File selectedDir = chooser.showDialog(stage);

        if (selectedDir == null) return;

        showProgress("Downloading package...");
        new Thread(() -> {
            try {
                byte[] packageBytes = transferApiService.downloadPackage(authorizationId);
                Path destPath = selectedDir.toPath().resolve(
                        authNumberLabel.getText().replace("/", "-") + ".heronix");
                Files.write(destPath, packageBytes);

                Platform.runLater(() -> {
                    hideProgress();
                    showSuccess("Package saved to: " + destPath);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    hideProgress();
                    showError("Download failed: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleDownloadDocs() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Save Documentation");
        Stage stage = (Stage) closeButton.getScene().getWindow();
        File selectedDir = chooser.showDialog(stage);

        if (selectedDir == null) return;

        showProgress("Downloading documentation...");
        new Thread(() -> {
            try {
                byte[] zipBytes = transferApiService.downloadAllDocumentation(authorizationId);
                Path destPath = selectedDir.toPath().resolve("hstp-documentation.zip");
                Files.write(destPath, zipBytes);

                Platform.runLater(() -> {
                    hideProgress();
                    showSuccess("Documentation saved to: " + destPath);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    hideProgress();
                    showError("Download failed: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleMarkDelivered() {
        try {
            Map<String, Object> result = transferApiService.markDelivered(
                    authorizationId, "USB_ENCRYPTED", "Delivered via USB drive");
            statusLabel.setText(String.valueOf(result.getOrDefault("status", "-")));
            showSuccess("Marked as delivered");
        } catch (Exception e) {
            showError("Failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleConfirmReceipt() {
        TextInputDialog dialog = new TextInputDialog("Email confirmation");
        dialog.setTitle("Confirm Receipt");
        dialog.setHeaderText("How was receipt confirmed?");
        dialog.setContentText("Method:");

        dialog.showAndWait().ifPresent(method -> {
            try {
                Map<String, Object> result = transferApiService.confirmReceipt(authorizationId, method);
                statusLabel.setText(String.valueOf(result.getOrDefault("status", "-")));
                showSuccess("Receipt confirmed — transfer COMPLETED");
            } catch (Exception e) {
                showError("Failed: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    // ========================================================================
    // HELPERS
    // ========================================================================

    private void loadAuthorizationInfo() {
        try {
            Map<String, Object> auth = transferApiService.getAuthorization(authorizationId);
            authNumberLabel.setText(String.valueOf(auth.get("authorizationNumber")));
            statusLabel.setText(String.valueOf(auth.get("status")));
            packageHashLabel.setText(String.valueOf(auth.getOrDefault("packageSha256Hash", "Not yet generated")));

            Object sizeObj = auth.get("packageSizeBytes");
            if (sizeObj != null) {
                packageSizeLabel.setText(formatFileSize(((Number) sizeObj).longValue()));
            } else {
                packageSizeLabel.setText("-");
            }
        } catch (Exception e) {
            showError("Failed to load: " + e.getMessage());
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }

    private void showProgress(String message) {
        progressBar.setVisible(true);
        progressBar.setProgress(-1);
        progressLabel.setVisible(true);
        progressLabel.setText(message);
    }

    private void hideProgress() {
        progressBar.setVisible(false);
        progressLabel.setVisible(false);
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
