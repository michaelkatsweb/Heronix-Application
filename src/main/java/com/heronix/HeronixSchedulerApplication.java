package com.heronix;

import com.heronix.ui.controller.MainControllerV2;
import com.heronix.ui.controller.LoginController;
import com.heronix.security.SecurityContext;
import com.heronix.service.UserService;
import com.heronix.service.integration.SchedulerProcessManager;
import com.heronix.ui.accessibility.ScreenReaderSupport;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.shape.Rectangle;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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
    private Label splashStatusLabel;

    // ========================================================================
    // APPLICATION LIFECYCLE
    // ========================================================================

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        log.info("Heronix SIS application init (splash screen will handle Spring Boot startup)");
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Show splash screen immediately
        Stage splashStage = createSplashScreen();
        splashStage.show();

        // Run Spring Boot initialization in a background thread
        Task<ConfigurableApplicationContext> initTask = new Task<>() {
            @Override
            protected ConfigurableApplicationContext call() throws Exception {
                updateMessage("Initializing application...");
                log.info("Initializing Spring Boot context...");

                SpringApplication app = new SpringApplication(HeronixSchedulerApplication.class);
                app.setWebApplicationType(org.springframework.boot.WebApplicationType.SERVLET);

                ConfigurableApplicationContext context = app.run();
                log.info("Spring Boot context initialized successfully!");
                log.info("REST API server is now available on port 9590 for Teacher Portal integration");

                updateMessage("Setting up users...");
                UserService userService = context.getBean(UserService.class);
                userService.initializeDefaultUsers();

                updateMessage("Ready!");
                return context;
            }
        };

        // Bind splash status label to task messages
        splashStatusLabel.textProperty().bind(initTask.messageProperty());

        initTask.setOnSucceeded(event -> {
            springContext = initTask.getValue();
            splashStage.hide();

            // Register JVM shutdown hook for data safety
            registerShutdownHook();

            try {
                // Show login dialog
                boolean loginSuccessful = showLoginDialog();

                if (!loginSuccessful) {
                    log.info("Login cancelled or failed. Exiting application.");
                    Platform.exit();
                    return;
                }

                // Load main window
                loadMainWindow();

            } catch (Exception e) {
                log.error("Failed to start application", e);
                showErrorAndExit(e);
            }
        });

        initTask.setOnFailed(event -> {
            splashStage.hide();
            Throwable ex = initTask.getException();
            log.error("Failed to initialize Spring Boot context", ex);
            showErrorAndExit(ex instanceof Exception ? (Exception) ex : new Exception(ex));
        });

        Thread initThread = new Thread(initTask, "heronix-spring-init");
        initThread.setDaemon(true);
        initThread.start();
    }

    @Override
    public void stop() {
        log.info("Shutting down Heronix Scheduling System...");

        // Stop SchedulerV2 subprocess if we launched it
        try {
            if (springContext != null && springContext.isActive()) {
                SchedulerProcessManager processManager = springContext.getBean(SchedulerProcessManager.class);
                processManager.stopScheduler();
            }
        } catch (Exception e) {
            log.warn("Could not stop SchedulerV2: {}", e.getMessage());
        }

        // Shut down non-Spring-managed singleton
        try {
            ScreenReaderSupport.getInstance().shutdown();
            log.info("ScreenReaderSupport shut down");
        } catch (Exception e) {
            log.warn("Error shutting down ScreenReaderSupport: {}", e.getMessage());
        }

        // Close Spring context (triggers @PreDestroy on all managed beans)
        if (springContext != null) {
            springContext.close();
            log.info("Spring context closed");
        }

        log.info("Application stopped");

        // Safety net: force JVM exit in case any non-daemon threads linger
        System.exit(0);
    }

    // ========================================================================
    // SPLASH SCREEN
    // ========================================================================

    private Stage createSplashScreen() {
        Stage splashStage = new Stage(StageStyle.TRANSPARENT);
        splashStage.setAlwaysOnTop(false);
        splashStage.setTitle("Heronix SIS");

        // Logo
        ImageView logoView = new ImageView();
        try {
            Image logo = new Image(getClass().getResourceAsStream("/images/icons/Text Logo_Vertical.png"));
            logoView.setImage(logo);
            logoView.setFitWidth(360);
            logoView.setPreserveRatio(true);
        } catch (Exception e) {
            log.warn("Splash logo not found, continuing without it");
        }

        // Subtitle
        Label subtitleLabel = new Label("Student Information System");
        subtitleLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 18px; -fx-font-weight: bold;");

        // Animated loading bar — glowing gradient that slides back and forth
        double barWidth = 340;
        double barHeight = 4;

        // Track (dark background)
        Rectangle barTrack = new Rectangle(barWidth, barHeight);
        barTrack.setArcWidth(barHeight);
        barTrack.setArcHeight(barHeight);
        barTrack.setFill(Color.web("#334155"));

        // Glowing indicator that slides across the track
        Rectangle barIndicator = new Rectangle(80, barHeight);
        barIndicator.setArcWidth(barHeight);
        barIndicator.setArcHeight(barHeight);
        barIndicator.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#3B82F6", 0.0)),
            new Stop(0.3, Color.web("#3B82F6", 1.0)),
            new Stop(0.7, Color.web("#60A5FA", 1.0)),
            new Stop(1, Color.web("#3B82F6", 0.0))
        ));
        barIndicator.setEffect(new Glow(0.6));
        barIndicator.setTranslateX(0);

        StackPane loadingBar = new StackPane(barTrack, barIndicator);
        loadingBar.setAlignment(Pos.CENTER_LEFT);
        loadingBar.setMaxWidth(barWidth);
        loadingBar.setPrefWidth(barWidth);

        // Slide animation — indicator sweeps left to right and back
        Timeline loadingAnimation = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(barIndicator.translateXProperty(), 0)),
            new KeyFrame(Duration.seconds(1.2),
                new KeyValue(barIndicator.translateXProperty(), barWidth - 80))
        );
        loadingAnimation.setAutoReverse(true);
        loadingAnimation.setCycleCount(Timeline.INDEFINITE);
        loadingAnimation.play();

        // Status label (bound to task messages)
        splashStatusLabel = new Label("Initializing application...");
        splashStatusLabel.setStyle("-fx-text-fill: #CBD5E1; -fx-font-size: 14px;");

        // Version label
        Label versionLabel = new Label("v6.0.0");
        versionLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;");

        // Layout
        VBox layout = new VBox(24);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(50, 50, 40, 50));
        layout.setStyle(
            "-fx-background-color: #1E293B;" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: #334155;" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 1;"
        );
        layout.setEffect(new DropShadow(30, Color.web("#000000", 0.5)));
        layout.getChildren().addAll(logoView, subtitleLabel, loadingBar, splashStatusLabel, versionLabel);

        Scene splashScene = new Scene(layout, 580, 480);
        splashScene.setFill(Color.TRANSPARENT);
        splashStage.setScene(splashScene);
        splashStage.centerOnScreen();

        // Set icon
        try {
            splashStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app-icon.png")));
        } catch (Exception e) {
            // Ignore — icon is optional
        }

        return splashStage;
    }

    // ========================================================================
    // DATA SAFETY — JVM SHUTDOWN HOOK
    // ========================================================================

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("JVM shutdown hook triggered");
            if (springContext != null && springContext.isActive()) {
                log.info("Closing Spring context from shutdown hook...");
                springContext.close();
                log.info("Spring context closed via shutdown hook");
            } else {
                log.info("Spring context already closed, shutdown hook is a no-op");
            }
        }, "heronix-shutdown-hook"));
        log.info("JVM shutdown hook registered for data safety");
    }

    // ========================================================================
    // MAIN WINDOW LOADING
    // ========================================================================

    private void loadMainWindow() throws Exception {
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
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

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
