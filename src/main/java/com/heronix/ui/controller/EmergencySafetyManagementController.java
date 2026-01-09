package com.heronix.ui.controller;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;
@Component
public class EmergencySafetyManagementController {
    @FXML private TableView contactsTableView;
    @FXML private void handleFireDrill() { showAlert("Fire Drill", "Recorded"); }
    @FXML private void handleLockdownDrill() { showAlert("Lockdown Drill", "Recorded"); }
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
