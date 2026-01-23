// Location: src/main/java/com/heronix/ui/dialog/LoginDialog.java
package com.heronix.ui.dialog;

import com.heronix.model.DistrictSettings;
import com.heronix.model.domain.User;
import com.heronix.service.AuthenticationService;
import com.heronix.service.DistrictSettingsService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import lombok.extern.slf4j.Slf4j;

/**
 * Login Dialog for JavaFX Application
 * Provides username/password authentication
 *
 * Features:
 * - State and district name display below app title
 * - Username/password authentication
 * - Error handling and display
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since 2025-11-04
 * @updated 2026-01 - Added state/district display (Phase 57)
 */
@Slf4j
public class LoginDialog extends Dialog<User> {

    private final AuthenticationService authenticationService;
    private final DistrictSettingsService districtSettingsService;
    private final TextField usernameField;
    private final PasswordField passwordField;
    private final Label errorLabel;
    private Label stateLabel;
    private Label districtLabel;

    public LoginDialog(AuthenticationService authenticationService) {
        this(authenticationService, null);
    }

    public LoginDialog(AuthenticationService authenticationService, DistrictSettingsService districtSettingsService) {
        this.authenticationService = authenticationService;
        this.districtSettingsService = districtSettingsService;

        setTitle("Heronix Scheduling System - Login");
        setHeaderText("Please log in to continue");

        // Create the dialog content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Title
        Label titleLabel = new Label("Heronix");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        titleLabel.setTextAlignment(TextAlignment.CENTER);

        // State name label (displayed below title)
        stateLabel = new Label();
        stateLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 18));
        stateLabel.setStyle("-fx-text-fill: #1976D2;"); // Blue color
        stateLabel.setTextAlignment(TextAlignment.CENTER);
        stateLabel.setVisible(false);
        stateLabel.setManaged(false);

        // District/County name label (displayed below state)
        districtLabel = new Label();
        districtLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        districtLabel.setStyle("-fx-text-fill: #555555;"); // Gray color
        districtLabel.setTextAlignment(TextAlignment.CENTER);
        districtLabel.setVisible(false);
        districtLabel.setManaged(false);

        // Load state and district info
        loadStateAndDistrictInfo();

        // Username
        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefWidth(250);

        // Password
        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(250);

        // Error label
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.setVisible(false);

        // Info label
        Label infoLabel = new Label("Default credentials:\nUsername: admin | Password: admin123");
        infoLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 10px;");

        // Add components to grid
        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(errorLabel, 1, 2);
        grid.add(infoLabel, 1, 3);

        // Title container with state/district info
        VBox titleContainer = new VBox(5);
        titleContainer.setAlignment(Pos.CENTER);
        titleContainer.getChildren().add(titleLabel);

        // Add state label if we have state info
        if (stateLabel.isVisible()) {
            titleContainer.getChildren().add(stateLabel);
        }

        // Add district label if we have district info
        if (districtLabel.isVisible()) {
            titleContainer.getChildren().add(districtLabel);
        }

        // Wrap in VBox
        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.getChildren().addAll(titleContainer, grid);
        content.setPadding(new Insets(10));

        getDialogPane().setContent(content);

        // Add buttons
        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(loginButtonType, cancelButtonType);

        // Get login button
        Button loginButton = (Button) getDialogPane().lookupButton(loginButtonType);
        loginButton.setDefaultButton(true);

        // Set focus to username field
        Platform.runLater(() -> usernameField.requestFocus());

        // Handle Enter key in password field
        passwordField.setOnAction(e -> {
            if (!usernameField.getText().trim().isEmpty() && !passwordField.getText().isEmpty()) {
                loginButton.fire();
            }
        });

        // Convert result
        setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return performLogin();
            }
            return null;
        });

        // Disable login button if fields are empty
        loginButton.disableProperty().bind(
                usernameField.textProperty().isEmpty()
                        .or(passwordField.textProperty().isEmpty())
        );
    }

    /**
     * Perform login authentication
     */
    private User performLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        try {
            log.info("Attempting login for user: {}", username);
            User user = authenticationService.authenticate(username, password);
            log.info("Login successful for user: {}", username);
            return user;

        } catch (IllegalArgumentException e) {
            log.warn("Login failed for user: {} - {}", username, e.getMessage());

            // Show error message
            errorLabel.setText(e.getMessage());
            errorLabel.setVisible(true);

            // Clear password field
            passwordField.clear();
            passwordField.requestFocus();

            // Return null to keep dialog open
            return null;
        }
    }

    /**
     * Show login dialog and wait for result
     *
     * @return Authenticated user or null if login was cancelled
     */
    public static User showLoginDialog(AuthenticationService authenticationService) {
        LoginDialog dialog = new LoginDialog(authenticationService);

        // Keep showing dialog until successful login or cancel
        while (true) {
            User user = dialog.showAndWait().orElse(null);

            if (user != null) {
                // Successful login
                return user;
            }

            // Check if cancel button was clicked
            if (dialog.getResult() != null) {
                // Login failed but user didn't click cancel - try again
                continue;
            }

            // Cancel was clicked
            return null;
        }
    }

    /**
     * Show login dialog with district settings service for state/district display
     *
     * @return Authenticated user or null if login was cancelled
     */
    public static User showLoginDialog(AuthenticationService authenticationService,
                                        DistrictSettingsService districtSettingsService) {
        LoginDialog dialog = new LoginDialog(authenticationService, districtSettingsService);

        // Keep showing dialog until successful login or cancel
        while (true) {
            User user = dialog.showAndWait().orElse(null);

            if (user != null) {
                // Successful login
                return user;
            }

            // Check if cancel button was clicked
            if (dialog.getResult() != null) {
                // Login failed but user didn't click cancel - try again
                continue;
            }

            // Cancel was clicked
            return null;
        }
    }

    // ========================================================================
    // STATE AND DISTRICT DISPLAY
    // Phase 57: State Configuration Feature - January 2026
    // ========================================================================

    /**
     * Load and display state and district information
     * Shows the configured state name below "Heronix" title
     * and district/county name below the state
     */
    private void loadStateAndDistrictInfo() {
        if (districtSettingsService == null) {
            log.debug("DistrictSettingsService not available, skipping state/district display");
            return;
        }

        try {
            DistrictSettings settings = districtSettingsService.getOrCreateDistrictSettings();

            // Get state name
            String stateName = null;
            if (settings.getUsState() != null) {
                stateName = settings.getUsState().getDisplayName();
            } else if (settings.getDistrictState() != null && !settings.getDistrictState().isEmpty()) {
                stateName = settings.getDistrictState();
            }

            // Get district/county name
            String districtName = settings.getDistrictName();

            // Update state label
            if (stateName != null && !stateName.isEmpty()) {
                stateLabel.setText(stateName);
                stateLabel.setVisible(true);
                stateLabel.setManaged(true);
                log.debug("Login dialog displaying state: {}", stateName);
            }

            // Update district label
            if (districtName != null && !districtName.isEmpty()) {
                districtLabel.setText(districtName);
                districtLabel.setVisible(true);
                districtLabel.setManaged(true);
                log.debug("Login dialog displaying district: {}", districtName);
            }

        } catch (Exception e) {
            log.warn("Could not load state/district info for login dialog", e);
            // Labels remain hidden if we can't load settings
        }
    }

    /**
     * Get the displayed state name
     */
    public String getDisplayedStateName() {
        return stateLabel.getText();
    }

    /**
     * Get the displayed district name
     */
    public String getDisplayedDistrictName() {
        return districtLabel.getText();
    }
}
