package com.heronix.ui.controller;

import com.heronix.service.SchedulerIntegrationService;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

/**
 * Controller for SchedulerV2 Launch Feature
 *
 * Handles launching Heronix-SchedulerV2 when the feature is available,
 * or displays appropriate messages when it's not.
 *
 * @author Heronix Development Team
 * @since 2025-01-06
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class SchedulerV2LaunchController {

    private final SchedulerIntegrationService schedulerIntegrationService;
    private HostServices hostServices;

    /**
     * Set HostServices for opening URLs in browser
     * This should be called by the main application controller
     */
    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    /**
     * Handle schedule generation request
     * Called when user clicks on "Schedule Generation" menu item
     */
    @FXML
    public void handleScheduleGeneration() {
        log.info("Schedule generation requested");

        // Check if scheduler is enabled and available
        if (!schedulerIntegrationService.isSchedulerEnabled()) {
            showSchedulerNotAvailableDialog();
            return;
        }

        if (!schedulerIntegrationService.isSchedulerAvailable()) {
            showSchedulerNotRunningDialog();
            return;
        }

        // Get current user info
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "unknown";
        String userRole = auth != null && !auth.getAuthorities().isEmpty() ?
                auth.getAuthorities().iterator().next().getAuthority() : "USER";

        // Get launch URL (with SSO token if configured)
        String launchUrl = schedulerIntegrationService.getSchedulerLaunchUrl(username, userRole);

        if (launchUrl == null) {
            showError("Configuration Error", "Failed to generate scheduler launch URL.");
            return;
        }

        // Confirm before launching
        if (confirmLaunch()) {
            launchScheduler(launchUrl);
        }
    }

    /**
     * Show dialog when scheduler add-on is not available
     */
    private void showSchedulerNotAvailableDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Heronix-SchedulerV2 Required");
        alert.setHeaderText("Schedule Generation Add-On Not Available");

        VBox content = new VBox(10);

        Text intro = new Text(
                "The Schedule Generation feature requires the Heronix-SchedulerV2 add-on, " +
                "which is not currently enabled on this system.\n\n"
        );
        intro.setWrappingWidth(450);

        Text features = new Text("Heronix-SchedulerV2 provides:\n");
        features.setStyle("-fx-font-weight: bold;");

        Text featureList = new Text(
                "• Advanced automated schedule generation\n" +
                "• Conflict resolution and optimization\n" +
                "• Multi-constraint scheduling algorithms\n" +
                "• Teacher and room assignment optimization\n" +
                "• Real-time schedule validation\n" +
                "• What-if scenario planning\n\n"
        );
        featureList.setWrappingWidth(450);

        Text contact = new Text("To enable this feature, please contact:\n");
        contact.setStyle("-fx-font-weight: bold;");

        TextFlow contactInfo = new TextFlow();
        Text adminText = new Text("• Your system administrator, or\n• Visit ");
        Hyperlink link = new Hyperlink("https://heronix.com");
        link.setOnAction(e -> {
            if (hostServices != null) {
                hostServices.showDocument("https://heronix.com");
            }
        });
        Text moreInfo = new Text(" for more information");

        contactInfo.getChildren().addAll(adminText, link, moreInfo);

        content.getChildren().addAll(intro, features, featureList, contact, contactInfo);

        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setPrefWidth(500);
        alert.showAndWait();

        log.info("Schedule generation not available - user informed");
    }

    /**
     * Show dialog when scheduler is configured but not currently running
     */
    private void showSchedulerNotRunningDialog() {
        String message = schedulerIntegrationService.getStatusMessage();

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Heronix-SchedulerV2 Not Running");
        alert.setHeaderText("Schedule Generation Service Unavailable");
        alert.setContentText(message);
        alert.getDialogPane().setPrefWidth(500);
        alert.showAndWait();

        log.warn("Schedule generation service not running - user informed");
    }

    /**
     * Confirm before launching scheduler
     */
    private boolean confirmLaunch() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Launch Schedule Generation");
        alert.setHeaderText("Open Heronix-SchedulerV2");

        String authMode = schedulerIntegrationService.getAuthMode().name();
        String message;

        if (authMode.equals("SSO")) {
            message = "This will open the Heronix-SchedulerV2 application in your web browser.\n\n" +
                     "You will be automatically logged in using Single Sign-On (SSO).\n\n" +
                     "Continue?";
        } else {
            message = "This will open the Heronix-SchedulerV2 application in your web browser.\n\n" +
                     "You will need to log in separately to SchedulerV2.\n\n" +
                     "Continue?";
        }

        alert.setContentText(message);

        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    /**
     * Launch scheduler in web browser
     */
    private void launchScheduler(String url) {
        try {
            log.info("Launching Heronix-SchedulerV2 at: {}", url);

            if (hostServices != null) {
                hostServices.showDocument(url);
                showInfo("Launched Successfully",
                        "Heronix-SchedulerV2 has been opened in your default web browser.");
            } else {
                // Fallback - show URL to user
                showUrlDialog(url);
            }

        } catch (Exception e) {
            log.error("Error launching scheduler", e);
            showError("Launch Error",
                    "Failed to launch Heronix-SchedulerV2.\n\n" +
                    "Please try opening this URL manually:\n" + url);
        }
    }

    /**
     * Show URL dialog if hostServices unavailable
     */
    private void showUrlDialog(String url) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Open Schedule Generation");
        alert.setHeaderText("Heronix-SchedulerV2 URL");
        alert.setContentText(
                "Please open the following URL in your web browser:\n\n" + url
        );
        alert.getDialogPane().setPrefWidth(500);
        alert.showAndWait();
    }

    /**
     * Check if scheduler is available (for UI state management)
     */
    public boolean isSchedulerAvailable() {
        return schedulerIntegrationService.isSchedulerAvailable();
    }

    /**
     * Get tooltip text for disabled state
     */
    public String getDisabledTooltipText() {
        return schedulerIntegrationService.getStatusMessage();
    }

    /**
     * Show info dialog
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show error dialog
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
