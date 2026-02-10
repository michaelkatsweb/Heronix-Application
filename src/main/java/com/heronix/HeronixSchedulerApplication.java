package com.heronix;

import com.heronix.ui.controller.MainControllerV2;
import com.heronix.ui.controller.LoginController;
import com.heronix.security.SecurityContext;
import com.heronix.service.UserService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.heronix.config.ApplicationProperties;

/**
 * Heronix Scheduling System - Desktop Application
 * Location: src/main/java/com/heronix/HeronixSchedulerApplication.java
 *
 * A product of Heronix Educational Systems LLC
 *
 * This is the JavaFX desktop client for Heronix SIS.
 *
 * Modes:
 * - STANDALONE: Runs with embedded server (default, for single-user/development)
 * - CLIENT: Connects to external Heronix-SIS-Server (for enterprise deployment)
 *
 * To run in CLIENT mode:
 *   Set environment variable: SIS_CLIENT_MODE=true
 *   Set server URL: SIS_SERVER_URL=http://server-ip:9590
 *
 * @author Heronix Educational Systems LLC
 * @version 6.0.0 - Client/Server Architecture
 * @since 2026-01
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "com.heronix")
@EnableConfigurationProperties(ApplicationProperties.class)
@EnableCaching
@EnableScheduling
@EntityScan(basePackages = "com.heronix")
@EnableJpaRepositories(basePackages = "com.heronix")
public class HeronixSchedulerApplication extends Application {

    private ConfigurableApplicationContext springContext;
    private Stage primaryStage;

    // ========================================================================
    // APPLICATION LIFECYCLE
    // ========================================================================

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        log.info("Initializing Spring Boot context...");

        // Configure Spring Boot for JavaFX desktop application WITH embedded web server
        // This enables REST API endpoints for Teacher Portal (Heronix-Talk) integration
        SpringApplication app = new SpringApplication(HeronixSchedulerApplication.class);
        app.setWebApplicationType(org.springframework.boot.WebApplicationType.SERVLET);

        springContext = app.run();
        log.info("Spring Boot context initialized successfully!");
        log.info("REST API server is now available on port 9590 for Teacher Portal integration");

        // Initialize default users (super admin)
        UserService userService = springContext.getBean(UserService.class);
        userService.initializeDefaultUsers();
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        try {
            log.info("Showing login dialog...");

            // Show login dialog first
            boolean loginSuccessful = showLoginDialog();

            if (!loginSuccessful) {
                log.info("Login cancelled or failed. Exiting application.");
                Platform.exit();
                return;
            }

            log.info("Login successful. Loading main window V2 (modern UI)...");

            // Load FXML with Spring controller factory
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainWindowV2.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            // Get controller reference
            MainControllerV2 mainController = loader.getController();

            // Create scene with stylesheet
            Scene scene = new Scene(root, 1400, 900);
            loadStylesheet(scene);

            // Configure primary stage
            configurePrimaryStage(primaryStage, scene);

            // Show stage
            primaryStage.show();

            // MainControllerV2 auto-loads dashboard in its initialize() method
            log.info("MainControllerV2 will auto-load dashboard via Platform.runLater");

            log.info("Heronix Scheduling System started successfully!");

        } catch (Exception e) {
            log.error("Failed to start application", e);
            showErrorAndExit(e);
        }
    }

    @Override
    public void stop() {
        log.info("Shutting down Heronix Scheduling System...");

        if (springContext != null) {
            springContext.close();
            log.info("Spring context closed");
        }

        log.info("Application stopped");
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Show login dialog and authenticate user
     * @return true if login successful, false otherwise
     */
    private boolean showLoginDialog() {
        try {
            // Load login dialog FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginDialog.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            // Get controller
            LoginController loginController = loader.getController();

            // Create dialog stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Heronix Scheduling System - Login");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setResizable(false);

            // Set scene with theme stylesheets
            Scene scene = new Scene(root);
            loadStylesheet(scene);  // Apply theme to login dialog
            dialogStage.setScene(scene);

            // Pass stage reference to controller
            loginController.setDialogStage(dialogStage);

            // Show and wait for result
            dialogStage.showAndWait();

            // Check if login was successful
            return loginController.isLoginSuccessful() && SecurityContext.isAuthenticated();

        } catch (Exception e) {
            log.error("Failed to show login dialog", e);
            return false;
        }
    }

    private void configurePrimaryStage(Stage stage, Scene scene) {
        stage.setTitle("Heronix Scheduling System - AI-Powered School Scheduling");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setMinWidth(1200);
        stage.setMinHeight(800);

        // Set application icon (if available)
        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app-icon.png")));
        } catch (Exception e) {
            log.warn("Application icon not found, using default");
        }

        // Handle window close request
        stage.setOnCloseRequest(event -> {
            log.info("Window close requested");
            Platform.exit();
        });
    }

    private void loadStylesheet(Scene scene) {
        try {
            // Load theme preference from settings
            String themeName = loadThemePreference();
            String themeFile;

            switch (themeName) {
                case "Dark":
                    themeFile = "/css/theme-dark.css";
                    log.info("Loading Dark theme (theme-dark.css)");
                    break;
                case "Light":
                    themeFile = "/css/theme-light.css";
                    log.info("Loading Light theme (theme-light.css)");
                    break;
                case "System":
                    // Detect system theme preference
                    themeFile = detectSystemTheme();
                    log.info("Loading System theme ({})", themeFile);
                    break;
                default:
                    themeFile = "/css/theme-dark.css";
                    log.warn("Unknown theme '{}', defaulting to theme-dark.css", themeName);
            }

            // Add theme stylesheet (complete styling - colors and layout)
            // Note: theme-dark.css and theme-light.css now contain all styling
            // base-styles.css is NOT loaded because it uses CSS variables which JavaFX ignores
            String themeStylesheet = getClass().getResource(themeFile).toExternalForm();
            scene.getStylesheets().add(themeStylesheet);
            log.info("Loaded theme: {} ({})", themeName, themeFile);
        } catch (Exception e) {
            log.warn("Failed to load stylesheet, using fallback theme", e);
            try {
                // Fallback to dark theme only
                String fallback = getClass().getResource("/css/theme-dark.css").toExternalForm();
                scene.getStylesheets().add(fallback);
                log.info("Loaded fallback dark theme");
            } catch (Exception ex) {
                log.error("Failed to load fallback stylesheet", ex);
            }
        }
    }

    /**
     * Detect system theme preference (dark/light mode)
     * Returns the appropriate theme CSS file path
     */
    private String detectSystemTheme() {
        try {
            // Try to detect Windows dark mode
            String os = System.getProperty("os.name", "").toLowerCase();
            if (os.contains("windows")) {
                // Check Windows registry for dark mode setting
                ProcessBuilder pb = new ProcessBuilder(
                    "reg", "query",
                    "HKCU\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                    "/v", "AppsUseLightTheme"
                );
                pb.redirectErrorStream(true);
                Process process = pb.start();
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream())
                );
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("AppsUseLightTheme")) {
                        // Value 0x0 means dark mode, 0x1 means light mode
                        if (line.contains("0x0")) {
                            log.info("System theme detected: Dark Mode");
                            return "/css/theme-dark.css";
                        } else {
                            log.info("System theme detected: Light Mode");
                            return "/css/theme-light.css";
                        }
                    }
                }
                process.waitFor();
            }
        } catch (Exception e) {
            log.warn("Could not detect system theme, defaulting to dark", e);
        }
        // Default to dark theme if detection fails
        return "/css/theme-dark.css";
    }

    /**
     * Load theme preference from settings file
     * @return Theme name: "Dark", "Light", or "System"
     */
    private String loadThemePreference() {
        try {
            java.io.File settingsFile = new java.io.File("config/app-settings.properties");
            if (settingsFile.exists()) {
                java.util.Properties settings = new java.util.Properties();
                settings.load(new java.io.FileInputStream(settingsFile));
                String theme = settings.getProperty("theme", "Dark");
                log.info("Loaded theme preference from settings: {}", theme);
                return theme;
            }
        } catch (Exception e) {
            log.warn("Failed to load theme preference, using default", e);
        }
        return "Dark"; // Default to Dark theme
    }

    private void showErrorAndExit(Exception e) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Application Error");
            alert.setHeaderText("Failed to start Heronix Scheduling System");
            alert.setContentText(
                "Error: " + e.getMessage() + "\n\n" +
                "Please check the logs for more details.\n" +
                "Application will now exit."
            );
            alert.showAndWait();
            Platform.exit();
        });
    }
}