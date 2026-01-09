package com.heronix.ui.controller;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;
@Component
public class TranscriptManagementController {
    @FXML private TextField studentSearchField;
    @FXML private TableView transcriptTableView;
    @FXML private void handleGenerateTranscript() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Generate Transcript");
        alert.setContentText("Transcript generated successfully");
        alert.showAndWait();
    }
}
