package com.heronix.ui.util;

import com.heronix.ui.controller.ReportGenerationDialogController;
import com.heronix.util.ResponsiveDesignHelper;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Report Dialog Launcher
 *
 * Utility class for launching the Report Generation Dialog from anywhere in the application.
 * Handles FXML loading, Spring context integration, and dialog lifecycle.
 *
 * Usage:
 * <code>
 * reportDialogLauncher.showReportDialog(primaryStage);
 * </code>
 *
 * Features:
 * - Spring-managed controller injection
 * - Modal dialog with proper parent stage
 * - Error handling and logging
 * - Reusable across application
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 51 - Frontend Integration
 */
@Slf4j
@Component
public class ReportDialogLauncher {

    private final ApplicationContext applicationContext;

    public ReportDialogLauncher(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Show the Report Generation Dialog
     *
     * @param owner Parent stage (can be null)
     */
    public void showReportDialog(Stage owner) {
        try {
            log.info("Launching Report Generation Dialog");

            // Load FXML with Spring context
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/ReportGenerationDialog.fxml")
            );
            loader.setControllerFactory(applicationContext::getBean);

            Parent root = loader.load();

            // Get controller and set dialog stage
            ReportGenerationDialogController controller = loader.getController();

            // Create dialog stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Generate Attendance Report");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            if (owner != null) {
                dialogStage.initOwner(owner);
            }

            // Set scene
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.setResizable(true);
            ResponsiveDesignHelper.makeDialogResponsive(dialogStage);

            // Set controller stage reference
            controller.setDialogStage(dialogStage);

            // Show dialog
            dialogStage.showAndWait();

        } catch (Exception e) {
            log.error("Error launching Report Generation Dialog", e);
            throw new RuntimeException("Failed to launch Report Generation Dialog", e);
        }
    }
}
