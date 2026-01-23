package com.heronix.ui.controller;

import com.heronix.client.AuthenticationApiService;
import com.heronix.model.DistrictSettings;
import com.heronix.model.domain.User;
import com.heronix.model.enums.USState;
import com.heronix.security.SecurityContext;
import com.heronix.service.DistrictSettingsService;
import com.heronix.service.UserService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.prefs.Preferences;

/**
 * Login Controller
 * Handles user authentication and login UI
 *
 * Features:
 * - Username/password authentication
 * - Remember me functionality
 * - Error handling and display
 * - Session management
 * - Password validation
 *
 * Location: src/main/java/com/heronix/ui/controller/LoginController.java
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since 2025-11-08
 */
@Slf4j
@Component
public class LoginController {

    // ========================================================================
    // FXML FIELDS
    // ========================================================================

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckBox;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Label errorLabel;
    @FXML private Label infoLabel;
    @FXML private Button loginButton;
    @FXML private HBox loadingBox;
    @FXML private Label sessionInfoLabel;

    // ========================================================================
    // STATE AND DISTRICT DISPLAY FIELDS
    // Phase 57: State Configuration Feature - January 2026
    // ========================================================================

    /**
     * Label to display the configured state name
     * Shown below the app title (e.g., "Florida", "Texas")
     */
    @FXML private Label stateNameLabel;

    /**
     * Label to display the district/county name
     * Shown below the state name (e.g., "Hernando County Schools")
     */
    @FXML private Label districtNameLabel;

    /**
     * Container for state/district info (for visibility control)
     */
    @FXML private VBox stateInfoContainer;

    // ========================================================================
    // DEPENDENCIES
    // ========================================================================

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationApiService authenticationApiService;

    @Autowired
    private DistrictSettingsService districtSettingsService;

    @Value("${api.authentication.enabled:true}")
    private boolean apiAuthenticationEnabled;

    // ========================================================================
    // STATE VARIABLES
    // ========================================================================

    private Stage dialogStage;
    private boolean loginSuccessful = false;
    private Preferences prefs;
    private static final String PREF_USERNAME = "last_username";
    private static final String PREF_REMEMBER_ME = "remember_me";

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    @FXML
    public void initialize() {
        log.info("Initializing Login Controller");

        // Load preferences
        prefs = Preferences.userNodeForPackage(LoginController.class);

        // Load and display state/district information
        loadStateAndDistrictInfo();

        // Check if remember me was enabled
        boolean rememberMe = prefs.getBoolean(PREF_REMEMBER_ME, false);
        if (rememberMe) {
            String lastUsername = prefs.get(PREF_USERNAME, "");
            if (!lastUsername.isEmpty()) {
                usernameField.setText(lastUsername);
                rememberMeCheckBox.setSelected(true);
                // Focus on password field if username is pre-filled
                Platform.runLater(() -> passwordField.requestFocus());
            }
        }

        // Add Enter key handler to fields
        usernameField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> handleLogin());

        // Clear any error messages when user starts typing
        usernameField.textProperty().addListener((obs, old, newVal) -> clearMessages());
        passwordField.textProperty().addListener((obs, old, newVal) -> clearMessages());
    }

    // ========================================================================
    // STATE AND DISTRICT DISPLAY
    // Phase 57: State Configuration Feature - January 2026
    // ========================================================================

    /**
     * Load and display the configured state and district/county name
     * on the login screen. This provides immediate visual confirmation
     * that the user is logging into the correct system.
     */
    private void loadStateAndDistrictInfo() {
        try {
            DistrictSettings settings = districtSettingsService.getOrCreateDistrictSettings();

            // Get state name (use final variables for lambda)
            final String stateName;
            if (settings.getUsState() != null) {
                stateName = settings.getUsState().getDisplayName();
            } else if (settings.getDistrictState() != null && !settings.getDistrictState().isEmpty()) {
                stateName = settings.getDistrictState();
            } else {
                stateName = null;
            }

            // Get district/county name
            final String districtName = settings.getDistrictName();

            // Update UI
            Platform.runLater(() -> {
                // Update state label
                if (stateNameLabel != null) {
                    if (stateName != null && !stateName.isEmpty()) {
                        stateNameLabel.setText(stateName);
                        stateNameLabel.setVisible(true);
                        stateNameLabel.setManaged(true);
                    } else {
                        stateNameLabel.setVisible(false);
                        stateNameLabel.setManaged(false);
                    }
                }

                // Update district label
                if (districtNameLabel != null) {
                    if (districtName != null && !districtName.isEmpty()) {
                        districtNameLabel.setText(districtName);
                        districtNameLabel.setVisible(true);
                        districtNameLabel.setManaged(true);
                    } else {
                        districtNameLabel.setVisible(false);
                        districtNameLabel.setManaged(false);
                    }
                }

                // Show/hide the container based on whether we have info to display
                if (stateInfoContainer != null) {
                    boolean hasInfo = (stateName != null && !stateName.isEmpty()) ||
                                      (districtName != null && !districtName.isEmpty());
                    stateInfoContainer.setVisible(hasInfo);
                    stateInfoContainer.setManaged(hasInfo);
                }

                log.debug("Login screen displaying: State='{}', District='{}'", stateName, districtName);
            });

        } catch (Exception e) {
            log.warn("Could not load district settings for login display", e);
            // Hide the labels if we can't load settings
            Platform.runLater(() -> {
                if (stateNameLabel != null) {
                    stateNameLabel.setVisible(false);
                    stateNameLabel.setManaged(false);
                }
                if (districtNameLabel != null) {
                    districtNameLabel.setVisible(false);
                    districtNameLabel.setManaged(false);
                }
                if (stateInfoContainer != null) {
                    stateInfoContainer.setVisible(false);
                    stateInfoContainer.setManaged(false);
                }
            });
        }
    }

    /**
     * Refresh the state and district display
     * Call this if settings are updated while login dialog is open
     */
    public void refreshStateAndDistrictInfo() {
        loadStateAndDistrictInfo();
    }

    /**
     * Get the currently displayed state name
     */
    public String getDisplayedStateName() {
        return stateNameLabel != null ? stateNameLabel.getText() : null;
    }

    /**
     * Get the currently displayed district name
     */
    public String getDisplayedDistrictName() {
        return districtNameLabel != null ? districtNameLabel.getText() : null;
    }

    // ========================================================================
    // ACTION HANDLERS
    // ========================================================================

    /**
     * Handle login button click
     */
    @FXML
    private void handleLogin() {
        log.info("Login attempt initiated");

        // Clear previous messages
        clearMessages();

        // Validate input
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty()) {
            showError("Please enter your username");
            usernameField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showError("Please enter your password");
            passwordField.requestFocus();
            return;
        }

        // Show loading indicator
        setLoading(true);

        // Perform authentication in background to avoid UI freeze
        new Thread(() -> {
            try {
                // Try API authentication first if enabled
                if (apiAuthenticationEnabled) {
                    // Check API connectivity first
                    if (!authenticationApiService.checkApiConnection()) {
                        log.warn("API server not reachable, falling back to local authentication");
                        performLocalAuthentication(username, password);
                        return;
                    }

                    // Perform API authentication
                    AuthenticationApiService.AuthenticationResult result =
                        authenticationApiService.login(username, password);

                    Platform.runLater(() -> {
                        setLoading(false);

                        if (result.success()) {
                            log.info("API authentication successful for user: {}", username);

                            // Fetch user details for security context
                            Optional<User> userOpt = userService.findByUsername(username);
                            if (userOpt.isPresent()) {
                                User user = userOpt.get();
                                SecurityContext.setCurrentUser(user);

                                // Save preferences if remember me is checked
                                if (rememberMeCheckBox.isSelected()) {
                                    prefs.put(PREF_USERNAME, username);
                                    prefs.putBoolean(PREF_REMEMBER_ME, true);
                                } else {
                                    prefs.remove(PREF_USERNAME);
                                    prefs.putBoolean(PREF_REMEMBER_ME, false);
                                }

                                // Show success and close
                                showInfo("Login successful! Welcome, " + user.getFullName());
                                loginSuccessful = true;

                                // Close dialog after a short delay
                                new Thread(() -> {
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        // Ignore
                                    }
                                    Platform.runLater(() -> {
                                        if (dialogStage != null) {
                                            dialogStage.close();
                                        }
                                    });
                                }).start();
                            } else {
                                showError("User details not found. Please contact administrator.");
                            }

                        } else {
                            log.warn("API authentication failed for user: {}", username);
                            showError(result.errorMessage() != null ?
                                result.errorMessage() :
                                "Invalid username or password.\nPlease check your credentials and try again.");
                            passwordField.clear();
                            passwordField.requestFocus();
                        }
                    });

                } else {
                    // Use local authentication
                    performLocalAuthentication(username, password);
                }

            } catch (Exception e) {
                log.error("Error during authentication", e);
                Platform.runLater(() -> {
                    setLoading(false);
                    showError("An error occurred during login.\nPlease try again.");
                });
            }
        }).start();
    }

    /**
     * Handle forgot password link
     */
    @FXML
    private void handleForgotPassword() {
        log.info("Forgot password clicked");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Password Reset");
        alert.setHeaderText("Password Reset Request");
        alert.setContentText(
                "To reset your password, please contact your system administrator.\n\n" +
                "For security reasons, password resets must be performed by an administrator."
        );
        alert.showAndWait();
    }

    /**
     * Handle help link
     */
    @FXML
    private void handleHelp() {
        log.info("Help clicked");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Login Help");
        alert.setHeaderText("How to Login");
        alert.setContentText(
                "1. Enter your username and password\n" +
                "2. Click the Login button or press Enter\n" +
                "3. Check 'Remember me' to save your username\n\n" +
                "Default Credentials (First Time Setup):\n" +
                "Username: admin\n" +
                "Password: Admin@123\n\n" +
                "IMPORTANT: Change the default password after first login!\n\n" +
                "If you're having trouble logging in:\n" +
                "- Verify your username and password\n" +
                "- Check if Caps Lock is ON\n" +
                "- Contact your administrator if account is locked"
        );
        alert.showAndWait();
    }

    /**
     * Handle about link
     */
    @FXML
    private void handleAbout() {
        log.info("About clicked");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Heronix Scheduling System");
        alert.setHeaderText("Heronix Scheduling System v1.0.0");
        alert.setContentText(
                "Heronix Scheduling System - Professional School Scheduling System\n\n" +
                "Features:\n" +
                "• Advanced scheduling algorithms\n" +
                "• Student and teacher management\n" +
                "• Course and room assignments\n" +
                "• FERPA-compliant security\n" +
                "• PDF export and printing\n" +
                "• Import/export functionality\n\n" +
                "© 2025 Heronix Scheduling System Team\n" +
                "All rights reserved."
        );
        alert.showAndWait();
    }

    // ========================================================================
    // AUTHENTICATION HELPER METHODS
    // ========================================================================

    /**
     * Perform local database authentication (fallback when API is unavailable)
     */
    private void performLocalAuthentication(String username, String password) {
        try {
            // Authenticate user against local database
            Optional<User> userOpt = userService.authenticate(username, password);

            Platform.runLater(() -> {
                setLoading(false);

                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    log.info("Local authentication successful for user: {}", username);

                    // Set security context
                    SecurityContext.setCurrentUser(user);

                    // Save preferences if remember me is checked
                    if (rememberMeCheckBox.isSelected()) {
                        prefs.put(PREF_USERNAME, username);
                        prefs.putBoolean(PREF_REMEMBER_ME, true);
                    } else {
                        prefs.remove(PREF_USERNAME);
                        prefs.putBoolean(PREF_REMEMBER_ME, false);
                    }

                    // Show success and close
                    showInfo("Login successful! Welcome, " + user.getFullName());
                    loginSuccessful = true;

                    // Close dialog after a short delay
                    new Thread(() -> {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            // Ignore
                        }
                        Platform.runLater(() -> {
                            if (dialogStage != null) {
                                dialogStage.close();
                            }
                        });
                    }).start();

                } else {
                    log.warn("Local authentication failed for user: {}", username);
                    showError("Invalid username or password.\nPlease check your credentials and try again.");
                    passwordField.clear();
                    passwordField.requestFocus();
                }
            });

        } catch (Exception e) {
            log.error("Error during local authentication", e);
            Platform.runLater(() -> {
                setLoading(false);
                showError("An error occurred during login.\nPlease try again.");
            });
        }
    }

    // ========================================================================
    // UI HELPER METHODS
    // ========================================================================

    /**
     * Show error message
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        infoLabel.setVisible(false);
        infoLabel.setManaged(false);
    }

    /**
     * Show info message
     */
    private void showInfo(String message) {
        infoLabel.setText(message);
        infoLabel.setVisible(true);
        infoLabel.setManaged(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    /**
     * Clear all messages
     */
    private void clearMessages() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        infoLabel.setVisible(false);
        infoLabel.setManaged(false);
    }

    /**
     * Set loading state
     */
    private void setLoading(boolean loading) {
        loadingBox.setVisible(loading);
        loadingBox.setManaged(loading);
        loginButton.setDisable(loading);
        usernameField.setDisable(loading);
        passwordField.setDisable(loading);
        rememberMeCheckBox.setDisable(loading);
    }

    // ========================================================================
    // GETTERS AND SETTERS
    // ========================================================================

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }

    /**
     * Show session info (for debugging)
     */
    public void showSessionInfo(String info) {
        sessionInfoLabel.setText(info);
        sessionInfoLabel.setVisible(true);
        sessionInfoLabel.setManaged(true);
    }
}
