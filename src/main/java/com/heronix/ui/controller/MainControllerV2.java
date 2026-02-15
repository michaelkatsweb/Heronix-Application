package com.heronix.ui.controller;

import com.heronix.security.SecurityContext;
import com.heronix.service.GlobalSearchService;
import com.heronix.ui.component.BreadcrumbNavigation;
import com.heronix.ui.service.FilterPersistenceService;
import com.heronix.ui.service.KeyboardShortcutManager;
import jakarta.annotation.PreDestroy;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Main Controller V2 - Modern UI with Sidebar Navigation
 * Location: src/main/java/com/heronix/ui/controller/MainControllerV2.java
 *
 * Features:
 * - Collapsible sidebar navigation
 * - Breadcrumb navigation
 * - Enhanced keyboard shortcuts
 * - Filter persistence
 * - Quick actions
 *
 * @author Heronix SIS Team
 * @version 2.0.0
 * @since 2026-01
 */
@Slf4j
@Component
public class MainControllerV2 {

    // ========================================================================
    // FXML INJECTED COMPONENTS
    // ========================================================================

    @FXML private BorderPane mainBorderPane;

    // Sidebar (loaded programmatically with Spring's controller factory)
    @FXML private VBox sidebarPlaceholder;
    private SidebarNavigationController sidebarNavigationController;

    // Top Bar
    @FXML private HBox topBar;
    @FXML private HBox breadcrumbContainer;
    @FXML private MenuButton quickAddBtn;
    @FXML private Button notificationBtn;
    @FXML private MenuButton helpMenuBtn;

    // Page Header
    @FXML private HBox pageHeader;
    @FXML private Label pageTitle;
    @FXML private Label pageSubtitle;
    @FXML private HBox pageActions;

    // Content Area
    @FXML private VBox mainContentArea;
    @FXML private ScrollPane contentScrollPane;
    @FXML private StackPane contentContainer;

    // Status Bar
    @FXML private HBox statusBar;
    @FXML private Circle connectionCircle;
    @FXML private Label connectionIndicator;
    @FXML private Label connectionStatus;
    @FXML private Circle dbTypeCircle;
    @FXML private Label dbTypeLabel;
    @FXML private Label statusLabel;
    @FXML private Label lastSyncTime;
    @FXML private Label currentUserLabel;
    @FXML private Label currentRoleLabel;

    // ========================================================================
    // DEPENDENCIES
    // ========================================================================

    @Autowired
    private ApplicationContext springContext;

    @Autowired
    private KeyboardShortcutManager keyboardShortcutManager;

    @Autowired
    private FilterPersistenceService filterPersistenceService;

    @Autowired(required = false)
    private GlobalSearchService globalSearchService;

    // Server URL for client mode
    @Value("${sis.server.url:http://localhost:9590}")
    private String serverUrl;

    @Value("${sis.client.mode:false}")
    private boolean clientMode;

    // ========================================================================
    // STATE
    // ========================================================================

    private BreadcrumbNavigation breadcrumb;
    private String currentView = "dashboard";
    private Node currentViewNode;

    // API connection status
    private ScheduledExecutorService connectionChecker;
    private Timeline blinkAnimation;
    private Timeline dbBlinkAnimation;
    private boolean isConnected = false;
    private boolean isApiOperationInProgress = false;

    // Connection status colors
    private static final String COLOR_GREEN = "#10B981";  // Connected
    private static final String COLOR_RED = "#EF4444";    // Disconnected
    private static final String COLOR_BLUE = "#3B82F6";   // Communicating
    private static final String COLOR_BLUE_LIGHT = "#93C5FD";  // Blink state

    // Database type indicator colors
    private static final String COLOR_POSTGRESQL = "#8B5CF6";       // Purple for PostgreSQL
    private static final String COLOR_POSTGRESQL_LIGHT = "#C4B5FD"; // Light purple blink
    private static final String COLOR_H2 = "#F59E0B";              // Amber for H2
    private static final String COLOR_H2_LIGHT = "#FCD34D";        // Light amber blink
    private static final String COLOR_GRAY = "#6B7280";            // Unknown/default

    // View cache for faster navigation
    private final Map<String, Node> viewCache = new HashMap<>();

    // View metadata
    private static final Map<String, ViewMetadata> VIEW_METADATA = new HashMap<>();

    static {
        VIEW_METADATA.put("dashboard", new ViewMetadata("Dashboard", "Overview of your school data", "/fxml/dashboard-view.fxml"));
        VIEW_METADATA.put("students", new ViewMetadata("Student Roster", "View and manage existing student records", "/fxml/Students.fxml"));
        VIEW_METADATA.put("teachers", new ViewMetadata("Teachers", "Manage instructional staff (teachers and co-teachers)", "/fxml/TeacherManagement.fxml"));
        VIEW_METADATA.put("staff", new ViewMetadata("Staff", "Manage non-instructional staff (admin, support, facilities)", "/fxml/StaffManagement.fxml"));
        VIEW_METADATA.put("substitutes", new ViewMetadata("Substitutes", "Manage substitute teachers and daily assignments", "/fxml/SubstituteManagement.fxml"));
        VIEW_METADATA.put("courses", new ViewMetadata("Courses", "Manage course catalog", "/fxml/CourseManagement.fxml"));
        VIEW_METADATA.put("schedules", new ViewMetadata("Schedules", "View and manage schedules", "/fxml/schedule-view.fxml"));
        VIEW_METADATA.put("gradebook", new ViewMetadata("Gradebook", "Manage grades and assignments", "/fxml/Gradebook.fxml"));
        VIEW_METADATA.put("attendance", new ViewMetadata("Attendance", "Track attendance", "/fxml/Attendance.fxml"));
        VIEW_METADATA.put("transcripts", new ViewMetadata("Transcripts", "Manage student transcripts and academic records", "/fxml/TranscriptManagement.fxml"));
        VIEW_METADATA.put("discipline", new ViewMetadata("Discipline", "Behavior tracking and incident management", "/fxml/BehaviorDashboard.fxml"));
        VIEW_METADATA.put("suspensions", new ViewMetadata("Suspensions", "Manage student suspensions and disciplinary actions", "/fxml/SuspensionManagement.fxml"));
        VIEW_METADATA.put("hall-pass", new ViewMetadata("Hall Pass", "Digital hall pass management", "/fxml/HallPass.fxml"));
        VIEW_METADATA.put("new-student-registration", new ViewMetadata("New Student Registration", "Register new students to the school", "/fxml/StudentRegistrationEnrollment.fxml"));
        VIEW_METADATA.put("incomplete-registrations", new ViewMetadata("Incomplete Registrations", "Track incomplete student registrations", "/fxml/IncompleteRegistrations.fxml"));
        VIEW_METADATA.put("course-enrollment", new ViewMetadata("Course Enrollment", "Enroll existing students into courses", "/fxml/StudentEnrollment.fxml"));
        VIEW_METADATA.put("secure-transfer", new ViewMetadata("Secure Transfer", "HSTP \u2014 Secure student record transfer with 3-party authorization", "/fxml/SecureTransferDashboard.fxml"));
        VIEW_METADATA.put("rooms", new ViewMetadata("Rooms", "Manage room inventory", "/fxml/RoomManagement.fxml"));
        VIEW_METADATA.put("events", new ViewMetadata("Events", "School events and calendar", "/fxml/Events.fxml"));
        VIEW_METADATA.put("iep", new ViewMetadata("IEP Management", "Manage Individual Education Plans", "/fxml/iep-management.fxml"));
        VIEW_METADATA.put("504", new ViewMetadata("504 Plans", "Manage 504 accommodation plans", "/fxml/plan504-management.fxml"));
        VIEW_METADATA.put("sped", new ViewMetadata("SPED Dashboard", "Special Education metrics", "/fxml/sped-dashboard.fxml"));
        VIEW_METADATA.put("ell", new ViewMetadata("ELL Dashboard", "English Language Learners management", "/fxml/ELLDashboard.fxml"));
        VIEW_METADATA.put("gifted", new ViewMetadata("Gifted & Talented", "Gifted and talented program management", "/fxml/GiftedDashboard.fxml"));
        VIEW_METADATA.put("health-office", new ViewMetadata("Health Office", "Health office and nurse visits", "/fxml/HealthOfficeDashboard.fxml"));
        VIEW_METADATA.put("counseling", new ViewMetadata("Counseling", "Student counseling services", "/fxml/CounselingDashboard.fxml"));
        VIEW_METADATA.put("medications", new ViewMetadata("Medications", "Medication administration tracking", "/fxml/MedicationAdministration.fxml"));
        VIEW_METADATA.put("immunizations", new ViewMetadata("Immunizations", "Immunization tracking and compliance", "/fxml/ImmunizationTracking.fxml"));
        VIEW_METADATA.put("transportation", new ViewMetadata("Transportation", "Bus routes and transportation management", "/fxml/TransportationManagement.fxml"));
        VIEW_METADATA.put("cafeteria", new ViewMetadata("Cafeteria", "Cafeteria and meal management", "/fxml/CafeteriaMealManagement.fxml"));
        VIEW_METADATA.put("library", new ViewMetadata("Library", "Library management and checkout", "/fxml/LibraryManagement.fxml"));
        VIEW_METADATA.put("time-hr", new ViewMetadata("Time & HR", "Staff time tracking and HR management", "/fxml/TimeAndHR.fxml"));
        VIEW_METADATA.put("athletics", new ViewMetadata("Athletics", "Athletics and extracurricular activities", "/fxml/AthleticsExtracurricular.fxml"));
        VIEW_METADATA.put("notifications", new ViewMetadata("Notifications", "Notification center", "/fxml/NotificationCenter.fxml"));
        VIEW_METADATA.put("communication", new ViewMetadata("Communication Center", "Parent and staff communication", "/fxml/CommunicationCenter.fxml"));
        VIEW_METADATA.put("reports", new ViewMetadata("Reports", "Generate and view reports", "/fxml/ReportsAnalytics.fxml"));
        VIEW_METADATA.put("analytics", new ViewMetadata("Analytics Hub", "Comprehensive analytics and reporting", "/fxml/analytics/AnalyticsHub.fxml"));
        VIEW_METADATA.put("student-analytics", new ViewMetadata("Student Analytics", "Enrollment, demographics, and academic analysis", "/fxml/analytics/StudentAnalytics.fxml"));
        VIEW_METADATA.put("attendance-analytics", new ViewMetadata("Attendance Analytics", "Attendance rates, patterns, and chronic absenteeism", "/fxml/analytics/AttendanceAnalytics.fxml"));
        VIEW_METADATA.put("academic-analytics", new ViewMetadata("Academic Performance", "Grades, GPA trends, and pass/fail analysis", "/fxml/analytics/AcademicPerformanceAnalytics.fxml"));
        VIEW_METADATA.put("staff-analytics", new ViewMetadata("Staff Analytics", "Certifications, workload, and professional development", "/fxml/analytics/StaffAnalytics.fxml"));
        VIEW_METADATA.put("behavior-analytics", new ViewMetadata("Behavior Analytics", "Incidents, trends, and intervention tracking", "/fxml/analytics/BehaviorAnalytics.fxml"));
        VIEW_METADATA.put("import", new ViewMetadata("Import Data", "Import data from files", "/fxml/ImportWizard.fxml"));
        VIEW_METADATA.put("data-generator", new ViewMetadata("Generate Sample Data", "Generate test data for the system", "/fxml/DataGenerator.fxml"));
        VIEW_METADATA.put("settings", new ViewMetadata("Settings", "System configuration", "/fxml/Settings.fxml"));
        VIEW_METADATA.put("user-management", new ViewMetadata("User Management", "Manage users, roles, and permissions", "/fxml/UserManagementView.fxml"));
        VIEW_METADATA.put("audit-log", new ViewMetadata("Audit Log", "View system audit logs and activity", "/fxml/AuditLogView.fxml"));
        VIEW_METADATA.put("database", new ViewMetadata("Database", "Database management and maintenance", "/fxml/DatabaseManagement.fxml"));
        VIEW_METADATA.put("network-panel", new ViewMetadata("Network Panel", "Network monitoring and diagnostics", "/fxml/NetworkPanel.fxml"));
        VIEW_METADATA.put("secure-sync", new ViewMetadata("Secure Sync", "Secure synchronization control panel", "/fxml/SecureSyncControlPanel.fxml"));
        VIEW_METADATA.put("secure-audit", new ViewMetadata("Security Audit", "Encrypted Hub activity logs (tamper-proof)", "/fxml/SecureAuditView.fxml"));
        VIEW_METADATA.put("teacher-certifications", new ViewMetadata("Teacher Certifications", "Track teacher certifications, renewals, and compliance", "/fxml/TeacherCertificationManagement.fxml"));
        VIEW_METADATA.put("teacher-evaluations", new ViewMetadata("Teacher Evaluations", "Manage observations, scoring, and performance evaluations", "/fxml/TeacherEvaluationManagement.fxml"));
        VIEW_METADATA.put("vendors", new ViewMetadata("Vendor Management", "Manage vendors, categories, and approval workflows", "/fxml/VendorManagement.fxml"));
        VIEW_METADATA.put("api-keys", new ViewMetadata("API Key Management", "Generate, manage, and monitor API keys", "/fxml/ApiKeyManagement.fxml"));
        VIEW_METADATA.put("master-schedule", new ViewMetadata("Master Schedule Board", "Interactive kanban-style master schedule editor", "/fxml/DragDropScheduleEditor.fxml"));
    }

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    @FXML
    public void initialize() {
        log.info("╔═══════════════════════════════════════════════════════════════╗");
        log.info("║   MAIN CONTROLLER V2 INITIALIZATION                           ║");
        log.info("╚═══════════════════════════════════════════════════════════════╝");

        // Load sidebar navigation with Spring's controller factory
        loadSidebarNavigation();

        // Initialize breadcrumb navigation
        initializeBreadcrumb();

        // Setup sidebar navigation callbacks
        setupSidebarCallbacks();

        // Update user info
        updateUserInfo();

        // Setup keyboard shortcuts
        Platform.runLater(this::setupKeyboardShortcuts);

        // Load default view (dashboard)
        Platform.runLater(() -> navigateTo("dashboard"));

        // Start API connection monitoring (delayed to ensure FXML is fully loaded)
        Platform.runLater(this::startConnectionMonitoring);

        log.info("✓ MainControllerV2 initialized successfully");
    }

    /**
     * Start periodic API connection monitoring
     */
    private void startConnectionMonitoring() {
        // Check if connectionCircle is available
        if (connectionCircle == null) {
            log.warn("Connection circle not available, skipping connection monitoring");
            return;
        }

        try {
            // Create blink animation for "communicating" state using Circle
            blinkAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                    new KeyValue(connectionCircle.fillProperty(), Color.web(COLOR_BLUE))),
                new KeyFrame(Duration.millis(400),
                    new KeyValue(connectionCircle.fillProperty(), Color.web(COLOR_BLUE_LIGHT))),
                new KeyFrame(Duration.millis(800),
                    new KeyValue(connectionCircle.fillProperty(), Color.web(COLOR_BLUE)))
            );
            blinkAnimation.setCycleCount(Timeline.INDEFINITE);

            // Create slower blink animation for database type indicator (1200ms cycle)
            if (dbTypeCircle != null) {
                dbBlinkAnimation = new Timeline(
                    new KeyFrame(Duration.ZERO,
                        new KeyValue(dbTypeCircle.fillProperty(), Color.web(COLOR_GRAY))),
                    new KeyFrame(Duration.millis(600),
                        new KeyValue(dbTypeCircle.fillProperty(), Color.web(COLOR_GRAY).deriveColor(0, 1, 1, 0.3))),
                    new KeyFrame(Duration.millis(1200),
                        new KeyValue(dbTypeCircle.fillProperty(), Color.web(COLOR_GRAY)))
                );
                dbBlinkAnimation.setCycleCount(Timeline.INDEFINITE);
            }

            // Start periodic connection check
            connectionChecker = Executors.newSingleThreadScheduledExecutor();
            connectionChecker.scheduleAtFixedRate(this::checkApiConnection, 0, 30, TimeUnit.SECONDS);

            log.info("API connection monitoring started (checking every 30 seconds)");
        } catch (Exception e) {
            log.error("Failed to start connection monitoring: {}", e.getMessage());
        }
    }

    /**
     * Check API server connection
     */
    private void checkApiConnection() {
        // Show blinking blue while checking
        Platform.runLater(() -> {
            connectionStatus.setText("Checking...");
            blinkAnimation.play();
        });

        try {
            String healthUrl = serverUrl + "/api/health";
            URL url = new URL(healthUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();

            // Read response body to extract databaseType
            String responseBody = "";
            if (responseCode >= 200 && responseCode < 300) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    responseBody = sb.toString();
                }
            }
            connection.disconnect();

            boolean connected = responseCode >= 200 && responseCode < 500;
            String dbType = extractJsonValue(responseBody, "databaseType");

            Platform.runLater(() -> {
                blinkAnimation.stop();
                isConnected = connected;

                if (connected) {
                    connectionCircle.setFill(Color.web(COLOR_GREEN));
                    connectionStatus.setText("Connected");
                    connectionStatus.setStyle("-fx-text-fill: " + COLOR_GREEN + ";");
                    updateDatabaseIndicator(dbType);
                } else {
                    connectionCircle.setFill(Color.web(COLOR_RED));
                    connectionStatus.setText("Offline");
                    connectionStatus.setStyle("-fx-text-fill: " + COLOR_RED + ";");
                    updateDatabaseIndicator(null);
                }
            });

        } catch (Exception e) {
            log.debug("API connection check failed: {}", e.getMessage());
            Platform.runLater(() -> {
                blinkAnimation.stop();
                isConnected = false;
                connectionCircle.setFill(Color.web(COLOR_RED));
                connectionStatus.setText("Offline");
                connectionStatus.setStyle("-fx-text-fill: " + COLOR_RED + ";");
                updateDatabaseIndicator(null);
            });
        }
    }

    /**
     * Call this when starting an API operation to show blinking blue
     */
    public void startApiOperation() {
        isApiOperationInProgress = true;
        Platform.runLater(() -> {
            blinkAnimation.play();
            connectionStatus.setText("Syncing...");
        });
    }

    /**
     * Call this when API operation completes
     */
    public void endApiOperation(boolean success) {
        isApiOperationInProgress = false;
        Platform.runLater(() -> {
            blinkAnimation.stop();
            if (success && isConnected) {
                connectionCircle.setFill(Color.web(COLOR_GREEN));
                connectionStatus.setText("Connected");
                connectionStatus.setStyle("-fx-text-fill: " + COLOR_GREEN + ";");
                lastSyncTime.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            } else {
                connectionCircle.setFill(Color.web(COLOR_RED));
                connectionStatus.setText("Sync Failed");
                connectionStatus.setStyle("-fx-text-fill: " + COLOR_RED + ";");
            }
        });
    }

    /**
     * Stop connection monitoring (call on shutdown)
     */
    @PreDestroy
    public void stopConnectionMonitoring() {
        if (connectionChecker != null && !connectionChecker.isShutdown()) {
            connectionChecker.shutdown();
        }
        if (blinkAnimation != null) {
            blinkAnimation.stop();
        }
        if (dbBlinkAnimation != null) {
            dbBlinkAnimation.stop();
        }
    }

    /**
     * Load sidebar navigation FXML with Spring's controller factory
     * This ensures the SidebarNavigationController is properly managed by Spring
     */
    private void loadSidebarNavigation() {
        try {
            log.info("Loading SidebarNavigation with Spring's controller factory...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/components/SidebarNavigation.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Node sidebar = loader.load();

            // Get the Spring-managed controller
            sidebarNavigationController = loader.getController();

            // Replace placeholder with actual sidebar
            sidebarPlaceholder.getChildren().clear();
            sidebarPlaceholder.getChildren().add(sidebar);
            VBox.setVgrow(sidebar, Priority.ALWAYS);

            log.info("✓ SidebarNavigation loaded successfully with Spring controller");
        } catch (IOException e) {
            log.error("Failed to load SidebarNavigation: {}", e.getMessage(), e);
            // Show error in sidebar placeholder
            Label errorLabel = new Label("Failed to load sidebar");
            errorLabel.setStyle("-fx-text-fill: #EF4444; -fx-padding: 20;");
            sidebarPlaceholder.getChildren().add(errorLabel);
        }
    }

    /**
     * Initialize breadcrumb navigation
     */
    private void initializeBreadcrumb() {
        breadcrumb = new BreadcrumbNavigation("Home");
        breadcrumb.setOnNavigate(path -> {
            log.debug("Breadcrumb navigate to: {}", path);
            if ("Home".equals(path)) {
                navigateTo("dashboard");
            }
        });
        breadcrumbContainer.getChildren().add(breadcrumb);
    }

    /**
     * Setup sidebar navigation callbacks
     */
    private void setupSidebarCallbacks() {
        if (sidebarNavigationController != null) {
            // Navigation callback
            sidebarNavigationController.setOnNavigate(this::navigateTo);

            // Quick search callback
            sidebarNavigationController.setOnQuickSearch(this::handleCommandPalette);

            // Logout callback
            sidebarNavigationController.setOnLogout(this::handleLogout);

            // Help callback
            sidebarNavigationController.setOnHelp(this::handleOpenHelpCenter);

            log.info("✓ Sidebar callbacks configured");
        } else {
            log.warn("SidebarNavigationController not available");
        }
    }

    /**
     * Update user info in status bar
     */
    private void updateUserInfo() {
        try {
            SecurityContext.getCurrentUser().ifPresent(user -> {
                String displayName = user.getFullName() != null && !user.getFullName().isEmpty()
                    ? user.getFullName()
                    : user.getUsername();
                currentUserLabel.setText(displayName);
                currentRoleLabel.setText(user.getPrimaryRole() != null ? user.getPrimaryRole().name() : "User");
            });
        } catch (Exception e) {
            log.debug("Could not update user info: {}", e.getMessage());
        }
    }

    /**
     * Setup keyboard shortcuts
     */
    private void setupKeyboardShortcuts() {
        Scene scene = mainBorderPane.getScene();
        if (scene == null) {
            log.warn("Scene not available, retrying...");
            Platform.runLater(this::setupKeyboardShortcuts);
            return;
        }

        // Initialize keyboard shortcut manager
        keyboardShortcutManager.initialize(scene);

        // Register navigation shortcuts
        registerShortcut("Ctrl+1", () -> navigateTo("dashboard"), "Navigation", "Go to Dashboard");
        registerShortcut("Ctrl+2", () -> navigateTo("students"), "Navigation", "Go to Students");
        registerShortcut("Ctrl+3", () -> navigateTo("teachers"), "Navigation", "Go to Teachers");
        registerShortcut("Ctrl+4", () -> navigateTo("courses"), "Navigation", "Go to Courses");
        registerShortcut("Ctrl+5", () -> navigateTo("schedules"), "Navigation", "Go to Schedules");
        registerShortcut("Ctrl+6", () -> navigateTo("gradebook"), "Navigation", "Go to Gradebook");
        registerShortcut("Ctrl+7", () -> navigateTo("attendance"), "Navigation", "Go to Attendance");
        registerShortcut("Ctrl+8", () -> navigateTo("reports"), "Navigation", "Go to Reports");
        registerShortcut("Ctrl+9", () -> navigateTo("settings"), "Navigation", "Go to Settings");

        // Action shortcuts
        registerShortcut("Ctrl+K", this::handleCommandPalette, "Actions", "Quick Search");
        registerShortcut("Ctrl+B", this::handleToggleSidebar, "Actions", "Toggle Sidebar");
        registerShortcut("Ctrl+N", this::handleQuickAddStudent, "Actions", "Add New Record");
        registerShortcut("Ctrl+I", this::handleImportData, "Actions", "Import Data");
        registerShortcut("Ctrl+R", this::handleRefresh, "Actions", "Refresh View");
        registerShortcut("F5", this::handleRefresh, "Actions", "Refresh View");

        // Help shortcuts
        registerShortcut("F1", this::handleOpenHelpCenter, "Help", "Open Help Center");
        registerShortcut("Shift+?", this::handleShowShortcuts, "Help", "Show Keyboard Shortcuts");

        // Escape to go back
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.ESCAPE),
                this::handleGoBack
        );

        log.info("✓ Keyboard shortcuts configured");
    }

    private void registerShortcut(String combo, Runnable action, String category, String description) {
        keyboardShortcutManager.registerGlobal(combo, action, category, description);
    }

    // ========================================================================
    // NAVIGATION
    // ========================================================================

    /**
     * Navigate to a view
     */
    public void navigateTo(String viewId) {
        log.info("Navigating to: {}", viewId);

        ViewMetadata metadata = VIEW_METADATA.get(viewId.toLowerCase());
        if (metadata == null) {
            log.warn("Unknown view: {}", viewId);
            setStatus("Unknown view: " + viewId, true);
            return;
        }

        try {
            // Load view
            Node viewNode = loadView(viewId, metadata.fxmlPath);
            if (viewNode != null) {
                // Update content
                contentContainer.getChildren().clear();
                contentContainer.getChildren().add(viewNode);
                currentViewNode = viewNode;
                currentView = viewId;

                // Update page header
                updatePageHeader(metadata.title, metadata.subtitle);

                // Update breadcrumb
                updateBreadcrumb(viewId, metadata.title);

                // Update sidebar active state
                if (sidebarNavigationController != null) {
                    sidebarNavigationController.setActiveNav(viewId);
                }

                // Update status
                setStatus("Loaded: " + metadata.title, false);

                // Set keyboard context
                keyboardShortcutManager.setContext(viewId);

                log.debug("Successfully navigated to: {}", viewId);
            }
        } catch (Exception e) {
            log.error("Failed to navigate to {}: {}", viewId, e.getMessage(), e);
            setStatus("Failed to load: " + metadata.title, true);
            showError("Navigation Error", "Could not load " + metadata.title + ": " + e.getMessage());
        }
    }

    /**
     * Load a view from FXML
     */
    private Node loadView(String viewId, String fxmlPath) {
        // Check cache first
        if (viewCache.containsKey(viewId)) {
            log.debug("Loading {} from cache", viewId);
            return viewCache.get(viewId);
        }

        try {
            var resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                log.error("FXML resource not found: {}", fxmlPath);
                return createErrorPlaceholder(viewId, "FXML file not found: " + fxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(resource);
            loader.setControllerFactory(springContext::getBean);
            Node view = loader.load();
            log.info("✓ Loaded FXML for '{}': {} (node: {})", viewId, fxmlPath, view.getClass().getSimpleName());

            // Cache the view (except for dialogs/wizards)
            if (!viewId.equals("import")) {
                viewCache.put(viewId, view);
            }

            return view;
        } catch (Exception e) {
            log.error("Failed to load FXML {}: {}", fxmlPath, e.getMessage(), e);
            return createErrorPlaceholder(viewId, e.getMessage());
        }
    }

    /**
     * Create an error placeholder when view fails to load
     */
    private Node createErrorPlaceholder(String viewId, String error) {
        VBox placeholder = new VBox(16);
        placeholder.setStyle("-fx-alignment: CENTER; -fx-padding: 48;");

        Label iconLabel = new Label("⚠");
        iconLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: #CBD5E1;");

        Label titleLabel = new Label("Failed to load " + viewId);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #475569;");

        Label errorLabel = new Label(error);
        errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8; -fx-wrap-text: true;");
        errorLabel.setMaxWidth(400);

        Button retryBtn = new Button("Retry");
        retryBtn.getStyleClass().addAll("btn", "btn-primary");
        retryBtn.setOnAction(e -> {
            viewCache.remove(viewId);
            navigateTo(viewId);
        });

        placeholder.getChildren().addAll(iconLabel, titleLabel, errorLabel, retryBtn);
        return placeholder;
    }

    /**
     * Update page header
     */
    private void updatePageHeader(String title, String subtitle) {
        pageTitle.setText(title);
        pageSubtitle.setText(subtitle);

        // Show header for non-dashboard views
        boolean showHeader = !"Dashboard".equals(title);
        pageHeader.setVisible(showHeader);
        pageHeader.setManaged(showHeader);
    }

    /**
     * Update breadcrumb navigation
     */
    private void updateBreadcrumb(String viewId, String title) {
        if ("dashboard".equals(viewId)) {
            breadcrumb.setPath("Home");
        } else {
            breadcrumb.setPath("Home", title);
        }
    }

    // ========================================================================
    // QUICK ACTIONS
    // ========================================================================

    @FXML
    private void handleQuickAddStudent() {
        log.info("Quick add student");
        navigateTo("students");
    }

    @FXML
    private void handleQuickAddTeacher() {
        log.info("Quick add teacher");
        navigateTo("teachers");
    }

    @FXML
    private void handleQuickAddCourse() {
        log.info("Quick add course");
        navigateTo("courses");
    }

    @FXML
    private void handleQuickAddEvent() {
        log.info("Quick add event");
        navigateTo("events");
    }

    @FXML
    private void handleQuickAddIEP() {
        log.info("Quick add IEP");
        navigateTo("iep");
    }

    // ========================================================================
    // COMMAND PALETTE
    // ========================================================================

    private void handleCommandPalette() {
        try {
            log.info("Opening Command Palette...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CommandPalette.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            CommandPaletteController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Quick Search");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(mainBorderPane.getScene().getWindow());

            Scene dialogScene = new Scene(root);
            dialogStage.setScene(dialogScene);

            controller.setDialogStage(dialogStage);
            controller.setOnResultSelected(result -> {
                handleSearchResultSelection(result);
            });

            dialogStage.showAndWait();

        } catch (IOException e) {
            log.error("Failed to open command palette: {}", e.getMessage());
        }
    }

    /**
     * Handle search result selection from command palette
     */
    private void handleSearchResultSelection(GlobalSearchService.SearchResult result) {
        log.info("Selected search result: {} ({})", result.getPrimaryText(), result.getType());

        switch (result.getType()) {
            case "STUDENT":
                navigateTo("students");
                break;
            case "TEACHER":
                navigateTo("teachers");
                break;
            case "COURSE":
                navigateTo("courses");
                break;
            case "ROOM":
                navigateTo("rooms");
                break;
            case "ACTION":
                // Handle navigation actions
                String action = result.getPrimaryText().toLowerCase();
                if (action.contains("dashboard")) navigateTo("dashboard");
                else if (action.contains("student")) navigateTo("students");
                else if (action.contains("teacher")) navigateTo("teachers");
                else if (action.contains("course")) navigateTo("courses");
                else if (action.contains("schedule")) navigateTo("schedules");
                break;
            default:
                log.warn("Unknown result type: {}", result.getType());
        }
    }

    // ========================================================================
    // SIDEBAR TOGGLE
    // ========================================================================

    private void handleToggleSidebar() {
        if (sidebarNavigationController != null) {
            if (sidebarNavigationController.isCollapsed()) {
                sidebarNavigationController.expandSidebar();
            } else {
                sidebarNavigationController.collapseSidebar();
            }
        }
    }

    // ========================================================================
    // OTHER HANDLERS
    // ========================================================================

    @FXML
    private void handleNotifications() {
        log.info("Opening notifications");
        navigateTo("notifications");
    }

    @FXML
    private void handleShowShortcuts() {
        log.info("Showing keyboard shortcuts");

        String helpText = keyboardShortcutManager.generateHelpText();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Keyboard Shortcuts");
        alert.setHeaderText("Available Keyboard Shortcuts");

        TextArea textArea = new TextArea(helpText);
        textArea.setEditable(false);
        textArea.setWrapText(false);
        textArea.setStyle("-fx-font-family: 'Cascadia Code', 'Consolas', monospace; -fx-font-size: 12px;");
        textArea.setPrefWidth(500);
        textArea.setPrefHeight(400);

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    @FXML
    private void handleOpenHelpCenter() {
        log.info("Opening Help Center");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HelpCenter.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent helpCenterRoot = loader.load();

            Stage helpStage = new Stage();
            helpStage.setTitle("Heronix SIS - Help Center");
            helpStage.initModality(Modality.NONE);
            helpStage.initOwner(mainBorderPane.getScene().getWindow());

            Scene scene = new Scene(helpCenterRoot, 1100, 750);

            // Apply current theme
            String currentTheme = mainBorderPane.getScene().getStylesheets().stream()
                    .filter(s -> s.contains("theme-"))
                    .findFirst()
                    .orElse(getClass().getResource("/css/theme-dark.css").toExternalForm());
            scene.getStylesheets().add(currentTheme);

            helpStage.setScene(scene);
            helpStage.show();
        } catch (IOException e) {
            log.error("Failed to open Help Center: {}", e.getMessage(), e);
            showError("Help Center Error", "Could not open Help Center: " + e.getMessage());
        }
    }

    @FXML
    private void handleShowAbout() {
        log.info("Showing About dialog");

        Alert about = new Alert(Alert.AlertType.INFORMATION);
        about.setTitle("About Heronix SIS");
        about.setHeaderText("Heronix Student Information System");
        about.setContentText(
                "Version: 2.0.0\n" +
                "Build: 2026.01\n\n" +
                "A comprehensive student information system for\n" +
                "managing students, teachers, courses, grades,\n" +
                "attendance, and more.\n\n" +
                "© 2026 Heronix Education Technology"
        );
        about.showAndWait();
    }

    private void handleImportData() {
        navigateTo("import");
    }

    private void handleRefresh() {
        log.info("Refreshing current view: {}", currentView);
        viewCache.remove(currentView);
        navigateTo(currentView);
        setStatus("Refreshed", false);
    }

    private void handleGoBack() {
        if (!"dashboard".equals(currentView)) {
            navigateTo("dashboard");
        }
    }

    private void handleLogout() {
        log.info("Logout requested");

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Logout");
        confirm.setHeaderText("Are you sure you want to logout?");
        confirm.setContentText("Any unsaved changes will be lost.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                log.info("User confirmed logout");
                SecurityContext.clearCurrentUser();
                Platform.exit();
            }
        });
    }

    // ========================================================================
    // DATABASE INDICATOR
    // ========================================================================

    /**
     * Update the database type indicator in the status bar
     */
    private void updateDatabaseIndicator(String dbType) {
        if (dbTypeCircle == null || dbTypeLabel == null) return;

        if (dbType == null || dbType.isEmpty()) {
            dbTypeCircle.setFill(Color.web(COLOR_GRAY));
            dbTypeLabel.setText("DB: ...");
            dbTypeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + COLOR_GRAY + ";");
            if (dbBlinkAnimation != null) dbBlinkAnimation.stop();
            return;
        }

        boolean isPostgres = dbType.toLowerCase().contains("postgres");
        String color = isPostgres ? COLOR_POSTGRESQL : COLOR_H2;
        String colorLight = isPostgres ? COLOR_POSTGRESQL_LIGHT : COLOR_H2_LIGHT;
        String label = isPostgres ? "PostgreSQL" : dbType;

        dbTypeCircle.setFill(Color.web(color));
        dbTypeLabel.setText(label);
        dbTypeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + color + ";");

        // Update and start the blink animation with the correct color
        if (dbBlinkAnimation != null) {
            dbBlinkAnimation.stop();
            dbBlinkAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                    new KeyValue(dbTypeCircle.fillProperty(), Color.web(color))),
                new KeyFrame(Duration.millis(600),
                    new KeyValue(dbTypeCircle.fillProperty(), Color.web(colorLight))),
                new KeyFrame(Duration.millis(1200),
                    new KeyValue(dbTypeCircle.fillProperty(), Color.web(color)))
            );
            dbBlinkAnimation.setCycleCount(Timeline.INDEFINITE);
            dbBlinkAnimation.play();
        }
    }

    /**
     * Simple JSON value extraction (avoids adding a JSON library dependency to the client)
     */
    private String extractJsonValue(String json, String key) {
        if (json == null || json.isEmpty()) return null;
        String search = "\"" + key + "\"";
        int keyIndex = json.indexOf(search);
        if (keyIndex < 0) return null;
        int colonIndex = json.indexOf(':', keyIndex + search.length());
        if (colonIndex < 0) return null;
        int startQuote = json.indexOf('"', colonIndex + 1);
        if (startQuote < 0) return null;
        int endQuote = json.indexOf('"', startQuote + 1);
        if (endQuote < 0) return null;
        return json.substring(startQuote + 1, endQuote);
    }

    // ========================================================================
    // STATUS BAR
    // ========================================================================

    /**
     * Set status message
     */
    public void setStatus(String message, boolean isError) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (isError ? "#EF4444" : "#64748B") + ";");

            // Update last sync time
            lastSyncTime.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        });
    }

    /**
     * Update connection status
     */
    public void setConnectionStatus(boolean connected) {
        Platform.runLater(() -> {
            connectionCircle.setFill(Color.web(connected ? COLOR_GREEN : COLOR_RED));
            connectionStatus.setText(connected ? "Connected" : "Disconnected");
            connectionStatus.setStyle("-fx-text-fill: " + (connected ? COLOR_GREEN : COLOR_RED) + ";");
        });
    }

    // ========================================================================
    // DIALOGS
    // ========================================================================

    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showInfo(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // ========================================================================
    // VIEW METADATA
    // ========================================================================

    private static class ViewMetadata {
        final String title;
        final String subtitle;
        final String fxmlPath;

        ViewMetadata(String title, String subtitle, String fxmlPath) {
            this.title = title;
            this.subtitle = subtitle;
            this.fxmlPath = fxmlPath;
        }
    }
}
