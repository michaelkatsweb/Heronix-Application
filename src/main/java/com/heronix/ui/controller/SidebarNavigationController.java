package com.heronix.ui.controller;

import com.heronix.repository.StudentRepository;
import com.heronix.repository.TeacherRepository;
import com.heronix.security.SecurityContext;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Sidebar Navigation Controller
 * Handles the collapsible sidebar navigation with grouped menu items.
 *
 * Features:
 * - Collapsible/expandable sidebar
 * - Active state management
 * - Navigation callbacks
 * - User info display
 * - Quick search trigger
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
@Component
public class SidebarNavigationController {

    // ========================================================================
    // FXML COMPONENTS
    // ========================================================================

    @FXML private VBox sidebarContainer;
    @FXML private HBox sidebarHeader;
    @FXML private javafx.scene.image.ImageView logoImageView;
    @FXML private VBox logoTextContainer;
    @FXML private Label logoText;
    @FXML private Button collapseBtn;
    @FXML private Button quickSearchBtn;
    @FXML private ScrollPane navScrollPane;
    @FXML private VBox navContainer;
    @FXML private VBox sidebarFooter;

    // Navigation Items
    @FXML private HBox navDashboard;
    @FXML private HBox navStudents;
    @FXML private HBox navTeachers;
    @FXML private HBox navStaff;
    @FXML private HBox navSubstitutes;
    @FXML private HBox navCourses;
    @FXML private HBox navSchedules;
    @FXML private HBox navGradebook;
    @FXML private HBox navAttendance;
    @FXML private HBox navTranscripts;
    @FXML private HBox navDiscipline;
    @FXML private HBox navSuspensions;
    @FXML private HBox navHallPass;
    @FXML private HBox navNewStudentReg;
    @FXML private HBox navIncompleteRegs;
    @FXML private HBox navCourseEnrollment;
    @FXML private HBox navRooms;
    @FXML private HBox navEvents;
    @FXML private HBox navIEP;
    @FXML private HBox nav504;
    @FXML private HBox navSPED;
    @FXML private HBox navELL;
    @FXML private HBox navGifted;
    @FXML private HBox navHealthOffice;
    @FXML private HBox navCounseling;
    @FXML private HBox navMedications;
    @FXML private HBox navImmunizations;
    @FXML private HBox navTransportation;
    @FXML private HBox navCafeteria;
    @FXML private HBox navLibrary;
    @FXML private HBox navAthletics;
    @FXML private HBox navNotifications;
    @FXML private HBox navCommunication;
    @FXML private HBox navReports;
    @FXML private HBox navAnalytics;
    @FXML private HBox navImport;
    @FXML private HBox navDataGenerator;
    @FXML private HBox navSettings;
    @FXML private HBox navUserManagement;
    @FXML private HBox navAuditLog;
    @FXML private HBox navDatabase;
    @FXML private HBox navNetworkPanel;
    @FXML private HBox navSecureSync;
    @FXML private HBox navSecureAudit;

    // Labels (for collapse mode)
    @FXML private Label navDashboardLabel;
    @FXML private Label navStudentsLabel;
    @FXML private Label navTeachersLabel;
    @FXML private Label navStaffLabel;
    @FXML private Label navSubstitutesLabel;
    @FXML private Label navCoursesLabel;
    @FXML private Label navSchedulesLabel;
    @FXML private Label navGradebookLabel;
    @FXML private Label navAttendanceLabel;
    @FXML private Label navTranscriptsLabel;
    @FXML private Label navDisciplineLabel;
    @FXML private Label navSuspensionsLabel;
    @FXML private Label navHallPassLabel;
    @FXML private Label navNewStudentRegLabel;
    @FXML private Label navIncompleteRegsLabel;
    @FXML private Label navCourseEnrollmentLabel;
    @FXML private Label navRoomsLabel;
    @FXML private Label navEventsLabel;
    @FXML private Label navIEPLabel;
    @FXML private Label nav504Label;
    @FXML private Label navSPEDLabel;
    @FXML private Label navELLLabel;
    @FXML private Label navGiftedLabel;
    @FXML private Label navHealthOfficeLabel;
    @FXML private Label navCounselingLabel;
    @FXML private Label navMedicationsLabel;
    @FXML private Label navImmunizationsLabel;
    @FXML private Label navTransportationLabel;
    @FXML private Label navCafeteriaLabel;
    @FXML private Label navLibraryLabel;
    @FXML private Label navAthleticsLabel;
    @FXML private Label navNotificationsLabel;
    @FXML private Label navCommunicationLabel;
    @FXML private Label navReportsLabel;
    @FXML private Label navAnalyticsLabel;
    @FXML private Label navImportLabel;
    @FXML private Label navDataGeneratorLabel;
    @FXML private Label navSettingsLabel;
    @FXML private Label navUserManagementLabel;
    @FXML private Label navAuditLogLabel;
    @FXML private Label navDatabaseLabel;
    @FXML private Label navNetworkPanelLabel;
    @FXML private Label navSecureSyncLabel;
    @FXML private Label navSecureAuditLabel;

    // Counts
    @FXML private Label studentCount;
    @FXML private Label teacherCount;

    // User Info
    @FXML private VBox avatarContainer;
    @FXML private Label userInitials;
    @FXML private VBox userInfoContainer;
    @FXML private Label userName;
    @FXML private Label userRole;
    @FXML private Button helpBtn;
    @FXML private Button logoutBtn;

    // ========================================================================
    // DEPENDENCIES
    // ========================================================================

    @Autowired(required = false)
    private StudentRepository studentRepository;

    @Autowired(required = false)
    private TeacherRepository teacherRepository;

    // ========================================================================
    // STATE
    // ========================================================================

    private boolean isCollapsed = false;
    private static final double EXPANDED_WIDTH = 260;
    private static final double COLLAPSED_WIDTH = 64;

    private HBox activeNavItem = null;
    private Map<String, HBox> navItemsMap = new HashMap<>();

    // Navigation callback
    private Consumer<String> onNavigate;

    // Quick search callback
    private Runnable onQuickSearch;

    // Logout callback
    private Runnable onLogout;

    // Help callback
    private Runnable onHelp;

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    @FXML
    public void initialize() {
        log.info("Initializing Sidebar Navigation Controller...");

        // Build navigation items map
        buildNavItemsMap();

        // Set default active item
        setActiveNavItem(navDashboard);

        // Load counts
        Platform.runLater(this::loadCounts);

        // Update user info
        updateUserInfo();

        // Add click handler on sidebar header to expand when collapsed
        if (sidebarHeader != null) {
            sidebarHeader.setOnMouseClicked(event -> {
                if (isCollapsed) {
                    expandSidebar();
                }
            });
        }

        // Add click handler on logo icon to expand when collapsed
        if (logoImageView != null) {
            logoImageView.setOnMouseClicked(event -> {
                if (isCollapsed) {
                    expandSidebar();
                }
            });
        }

        log.info("✓ Sidebar Navigation initialized");
    }

    /**
     * Build map of navigation items for easy lookup
     */
    private void buildNavItemsMap() {
        navItemsMap.put("dashboard", navDashboard);
        navItemsMap.put("students", navStudents);
        navItemsMap.put("teachers", navTeachers);
        navItemsMap.put("staff", navStaff);
        navItemsMap.put("substitutes", navSubstitutes);
        navItemsMap.put("courses", navCourses);
        navItemsMap.put("schedules", navSchedules);
        navItemsMap.put("gradebook", navGradebook);
        navItemsMap.put("attendance", navAttendance);
        navItemsMap.put("transcripts", navTranscripts);
        navItemsMap.put("discipline", navDiscipline);
        navItemsMap.put("suspensions", navSuspensions);
        navItemsMap.put("hall-pass", navHallPass);
        navItemsMap.put("new-student-registration", navNewStudentReg);
        navItemsMap.put("incomplete-registrations", navIncompleteRegs);
        navItemsMap.put("course-enrollment", navCourseEnrollment);
        navItemsMap.put("rooms", navRooms);
        navItemsMap.put("events", navEvents);
        navItemsMap.put("iep", navIEP);
        navItemsMap.put("504", nav504);
        navItemsMap.put("sped", navSPED);
        navItemsMap.put("ell", navELL);
        navItemsMap.put("gifted", navGifted);
        navItemsMap.put("health-office", navHealthOffice);
        navItemsMap.put("counseling", navCounseling);
        navItemsMap.put("medications", navMedications);
        navItemsMap.put("immunizations", navImmunizations);
        navItemsMap.put("transportation", navTransportation);
        navItemsMap.put("cafeteria", navCafeteria);
        navItemsMap.put("library", navLibrary);
        navItemsMap.put("athletics", navAthletics);
        navItemsMap.put("notifications", navNotifications);
        navItemsMap.put("communication", navCommunication);
        navItemsMap.put("reports", navReports);
        navItemsMap.put("analytics", navAnalytics);
        navItemsMap.put("import", navImport);
        navItemsMap.put("data-generator", navDataGenerator);
        navItemsMap.put("settings", navSettings);
        navItemsMap.put("user-management", navUserManagement);
        navItemsMap.put("audit-log", navAuditLog);
        navItemsMap.put("database", navDatabase);
        navItemsMap.put("network-panel", navNetworkPanel);
        navItemsMap.put("secure-sync", navSecureSync);
        navItemsMap.put("secure-audit", navSecureAudit);
    }

    /**
     * Load entity counts for badges
     */
    private void loadCounts() {
        try {
            if (studentRepository != null) {
                long count = studentRepository.count();
                studentCount.setText(String.valueOf(count));
            }
            if (teacherRepository != null) {
                long count = teacherRepository.count();
                teacherCount.setText(String.valueOf(count));
            }
        } catch (Exception e) {
            log.warn("Could not load counts: {}", e.getMessage());
        }
    }

    /**
     * Update user info from security context
     */
    private void updateUserInfo() {
        try {
            SecurityContext.getCurrentUser().ifPresent(user -> {
                String displayName = user.getFullName() != null && !user.getFullName().isEmpty()
                    ? user.getFullName()
                    : user.getUsername();
                userName.setText(displayName);
                userRole.setText(user.getPrimaryRole() != null ? user.getPrimaryRole().name() : "User");

                // Set initials from display name
                String initials = "";
                String[] nameParts = displayName.split("\\s+");
                for (String part : nameParts) {
                    if (!part.isEmpty() && initials.length() < 2) {
                        initials += part.charAt(0);
                    }
                }
                userInitials.setText(initials.isEmpty() ? "U" : initials.toUpperCase());
            });
        } catch (Exception e) {
            log.debug("Could not update user info: {}", e.getMessage());
        }
    }

    // ========================================================================
    // SIDEBAR TOGGLE
    // ========================================================================

    @FXML
    private void handleToggleSidebar() {
        if (isCollapsed) {
            expandSidebar();
        } else {
            collapseSidebar();
        }
    }

    /**
     * Collapse sidebar to icon-only mode
     */
    public void collapseSidebar() {
        isCollapsed = true;

        // Update width on sidebar container
        sidebarContainer.setMinWidth(COLLAPSED_WIDTH);
        sidebarContainer.setMaxWidth(COLLAPSED_WIDTH);
        sidebarContainer.setPrefWidth(COLLAPSED_WIDTH);

        // Also update parent container (sidebarPlaceholder in MainWindowV2) so main content expands
        if (sidebarContainer.getParent() instanceof javafx.scene.layout.Region) {
            javafx.scene.layout.Region parent = (javafx.scene.layout.Region) sidebarContainer.getParent();
            parent.setMinWidth(COLLAPSED_WIDTH);
            parent.setMaxWidth(COLLAPSED_WIDTH);
            parent.setPrefWidth(COLLAPSED_WIDTH);
        }

        // Hide text elements, shrink logo for collapsed width
        logoTextContainer.setVisible(false);
        logoTextContainer.setManaged(false);
        if (logoImageView != null) {
            logoImageView.setFitHeight(24);
        }

        quickSearchBtn.setText("\uD83D\uDD0D");
        quickSearchBtn.setStyle("-fx-min-width: 32; -fx-max-width: 32; -fx-padding: 8 0; -fx-alignment: CENTER;");

        // Hide all nav labels
        hideNavLabels();

        // Hide user info text
        userInfoContainer.setVisible(false);
        userInfoContainer.setManaged(false);

        // Hide footer buttons text (collapsed mode uses symbols)
        helpBtn.setText("?");
        logoutBtn.setText("X");

        // Update collapse button
        collapseBtn.setText("▶");
        collapseBtn.setTooltip(new Tooltip("Expand Sidebar (Ctrl+B)"));

        // Add collapsed style
        sidebarContainer.getStyleClass().add("sidebar-collapsed");

        log.debug("Sidebar collapsed");
    }

    /**
     * Expand sidebar to full mode
     */
    public void expandSidebar() {
        isCollapsed = false;

        // Update width on sidebar container
        sidebarContainer.setMinWidth(EXPANDED_WIDTH);
        sidebarContainer.setMaxWidth(EXPANDED_WIDTH);
        sidebarContainer.setPrefWidth(EXPANDED_WIDTH);

        // Also update parent container (sidebarPlaceholder in MainWindowV2) so main content adjusts
        if (sidebarContainer.getParent() instanceof javafx.scene.layout.Region) {
            javafx.scene.layout.Region parent = (javafx.scene.layout.Region) sidebarContainer.getParent();
            parent.setMinWidth(EXPANDED_WIDTH);
            parent.setMaxWidth(EXPANDED_WIDTH);
            parent.setPrefWidth(EXPANDED_WIDTH);
        }

        // Show text elements, restore logo size
        logoTextContainer.setVisible(true);
        logoTextContainer.setManaged(true);
        if (logoImageView != null) {
            logoImageView.setFitHeight(40);
        }

        quickSearchBtn.setText("Quick Search...");
        quickSearchBtn.setStyle("-fx-alignment: CENTER_LEFT; -fx-pref-width: 228;");

        // Show all nav labels
        showNavLabels();

        // Show user info text
        userInfoContainer.setVisible(true);
        userInfoContainer.setManaged(true);

        // Show footer buttons text
        helpBtn.setText("Help");
        logoutBtn.setText("Logout");

        // Update collapse button
        collapseBtn.setText("◀");
        collapseBtn.setTooltip(new Tooltip("Collapse Sidebar (Ctrl+B)"));

        // Remove collapsed style
        sidebarContainer.getStyleClass().remove("sidebar-collapsed");

        log.debug("Sidebar expanded");
    }

    private void hideNavLabels() {
        // Hide all non-icon children in nav items (labels, regions, counts)
        for (HBox navItem : navItemsMap.values()) {
            if (navItem == null) continue;

            for (Node child : navItem.getChildren()) {
                if (child instanceof Label) {
                    Label label = (Label) child;
                    // Only keep the icon visible
                    if (!label.getStyleClass().contains("sidebar-nav-icon")) {
                        label.setVisible(false);
                        label.setManaged(false);
                    }
                } else if (child instanceof Region) {
                    // Hide spacer regions
                    child.setVisible(false);
                    child.setManaged(false);
                }
            }
            // Center the icon when collapsed
            navItem.setStyle("-fx-padding: 12 0; -fx-alignment: CENTER;");
        }

        // Hide section titles
        if (navContainer != null) {
            navContainer.getChildren().forEach(child -> {
                if (child instanceof Label && child.getStyleClass().contains("sidebar-section-title")) {
                    child.setVisible(false);
                    child.setManaged(false);
                }
            });
        }

        // Adjust header for collapsed mode - hide spacer Region
        if (sidebarHeader != null) {
            sidebarHeader.setStyle("-fx-padding: 16 0; -fx-alignment: CENTER;");
            for (Node child : sidebarHeader.getChildren()) {
                if (child instanceof Region && !(child instanceof javafx.scene.layout.Pane)) {
                    child.setVisible(false);
                    child.setManaged(false);
                }
            }
        }

        // Hide collapse button, show expand on click anywhere
        if (collapseBtn != null) {
            collapseBtn.setVisible(false);
            collapseBtn.setManaged(false);
        }

        // Adjust quick search button container
        if (quickSearchBtn != null && quickSearchBtn.getParent() instanceof HBox) {
            ((HBox) quickSearchBtn.getParent()).setStyle("-fx-padding: 12 0; -fx-alignment: CENTER;");
        }

        // Adjust footer for collapsed mode
        if (sidebarFooter != null) {
            sidebarFooter.setStyle("-fx-padding: 16 0; -fx-alignment: CENTER;");
            for (Node child : sidebarFooter.getChildren()) {
                if (child instanceof HBox) {
                    HBox hbox = (HBox) child;
                    // Check if this is the buttons row
                    boolean isButtonRow = hbox.getChildren().stream()
                            .anyMatch(c -> c == helpBtn || c == logoutBtn);
                    if (isButtonRow) {
                        hbox.setVisible(false);
                        hbox.setManaged(false);
                    } else {
                        // Avatar row - center it
                        hbox.setStyle("-fx-alignment: CENTER;");
                    }
                }
            }
        }
    }

    private void showNavLabels() {
        // Show all children in nav items and restore styling
        for (HBox navItem : navItemsMap.values()) {
            if (navItem == null) continue;

            for (Node child : navItem.getChildren()) {
                child.setVisible(true);
                child.setManaged(true);
            }
            // Reset to left alignment with proper padding
            navItem.setStyle("-fx-padding: 10 20; -fx-alignment: CENTER_LEFT;");
        }

        // Show section titles
        if (navContainer != null) {
            navContainer.getChildren().forEach(child -> {
                if (child instanceof Label && child.getStyleClass().contains("sidebar-section-title")) {
                    child.setVisible(true);
                    child.setManaged(true);
                }
            });
        }

        // Reset header styling - restore spacer Region
        if (sidebarHeader != null) {
            sidebarHeader.setStyle("-fx-padding: 16 20;");
            for (Node child : sidebarHeader.getChildren()) {
                if (child instanceof Region && !(child instanceof javafx.scene.layout.Pane)) {
                    child.setVisible(true);
                    child.setManaged(true);
                }
            }
        }

        // Show collapse button
        if (collapseBtn != null) {
            collapseBtn.setVisible(true);
            collapseBtn.setManaged(true);
        }

        // Reset quick search button container
        if (quickSearchBtn != null && quickSearchBtn.getParent() instanceof HBox) {
            ((HBox) quickSearchBtn.getParent()).setStyle("-fx-padding: 12 16;");
        }

        // Reset footer styling
        if (sidebarFooter != null) {
            sidebarFooter.setStyle("-fx-padding: 16 20;");
            for (Node child : sidebarFooter.getChildren()) {
                if (child instanceof HBox) {
                    HBox hbox = (HBox) child;
                    hbox.setVisible(true);
                    hbox.setManaged(true);
                    hbox.setStyle("");
                }
            }
        }
    }

    public boolean isCollapsed() {
        return isCollapsed;
    }

    // ========================================================================
    // ACTIVE STATE MANAGEMENT
    // ========================================================================

    /**
     * Set the active navigation item
     */
    public void setActiveNavItem(HBox navItem) {
        // Remove active class from previous
        if (activeNavItem != null) {
            activeNavItem.getStyleClass().remove("sidebar-nav-item-active");
        }

        // Add active class to new
        if (navItem != null) {
            navItem.getStyleClass().add("sidebar-nav-item-active");
            activeNavItem = navItem;
        }
    }

    /**
     * Set active navigation by key
     */
    public void setActiveNav(String navKey) {
        HBox navItem = navItemsMap.get(navKey.toLowerCase());
        if (navItem != null) {
            setActiveNavItem(navItem);
        }
    }

    // ========================================================================
    // NAVIGATION HANDLERS
    // ========================================================================

    @FXML
    private void handleQuickSearch() {
        if (onQuickSearch != null) {
            onQuickSearch.run();
        }
    }

    @FXML
    private void handleNavDashboard(MouseEvent event) {
        navigateTo("dashboard", navDashboard);
    }

    @FXML
    private void handleNavStudents(MouseEvent event) {
        navigateTo("students", navStudents);
    }

    @FXML
    private void handleNavTeachers(MouseEvent event) {
        navigateTo("teachers", navTeachers);
    }

    @FXML
    private void handleNavStaff(MouseEvent event) {
        navigateTo("staff", navStaff);
    }

    @FXML
    private void handleNavSubstitutes(MouseEvent event) {
        navigateTo("substitutes", navSubstitutes);
    }

    @FXML
    private void handleNavCourses(MouseEvent event) {
        navigateTo("courses", navCourses);
    }

    @FXML
    private void handleNavSchedules(MouseEvent event) {
        navigateTo("schedules", navSchedules);
    }

    @FXML
    private void handleNavGradebook(MouseEvent event) {
        navigateTo("gradebook", navGradebook);
    }

    @FXML
    private void handleNavAttendance(MouseEvent event) {
        navigateTo("attendance", navAttendance);
    }

    @FXML
    private void handleNavTranscripts(MouseEvent event) {
        navigateTo("transcripts", navTranscripts);
    }

    @FXML
    private void handleNavDiscipline(MouseEvent event) {
        navigateTo("discipline", navDiscipline);
    }

    @FXML
    private void handleNavSuspensions(MouseEvent event) {
        navigateTo("suspensions", navSuspensions);
    }

    @FXML
    private void handleNavHallPass(MouseEvent event) {
        navigateTo("hall-pass", navHallPass);
    }

    @FXML
    private void handleNavNewStudentReg(MouseEvent event) {
        navigateTo("new-student-registration", navNewStudentReg);
    }

    @FXML
    private void handleNavIncompleteRegs(MouseEvent event) {
        navigateTo("incomplete-registrations", navIncompleteRegs);
    }

    @FXML
    private void handleNavCourseEnrollment(MouseEvent event) {
        navigateTo("course-enrollment", navCourseEnrollment);
    }

    @FXML
    private void handleNavRooms(MouseEvent event) {
        navigateTo("rooms", navRooms);
    }

    @FXML
    private void handleNavEvents(MouseEvent event) {
        navigateTo("events", navEvents);
    }

    @FXML
    private void handleNavIEP(MouseEvent event) {
        navigateTo("iep", navIEP);
    }

    @FXML
    private void handleNav504(MouseEvent event) {
        navigateTo("504", nav504);
    }

    @FXML
    private void handleNavSPED(MouseEvent event) {
        navigateTo("sped", navSPED);
    }

    @FXML
    private void handleNavELL(MouseEvent event) {
        navigateTo("ell", navELL);
    }

    @FXML
    private void handleNavGifted(MouseEvent event) {
        navigateTo("gifted", navGifted);
    }

    @FXML
    private void handleNavHealthOffice(MouseEvent event) {
        navigateTo("health-office", navHealthOffice);
    }

    @FXML
    private void handleNavCounseling(MouseEvent event) {
        navigateTo("counseling", navCounseling);
    }

    @FXML
    private void handleNavMedications(MouseEvent event) {
        navigateTo("medications", navMedications);
    }

    @FXML
    private void handleNavImmunizations(MouseEvent event) {
        navigateTo("immunizations", navImmunizations);
    }

    @FXML
    private void handleNavTransportation(MouseEvent event) {
        navigateTo("transportation", navTransportation);
    }

    @FXML
    private void handleNavCafeteria(MouseEvent event) {
        navigateTo("cafeteria", navCafeteria);
    }

    @FXML
    private void handleNavLibrary(MouseEvent event) {
        navigateTo("library", navLibrary);
    }

    @FXML
    private void handleNavAthletics(MouseEvent event) {
        navigateTo("athletics", navAthletics);
    }

    @FXML
    private void handleNavNotifications(MouseEvent event) {
        navigateTo("notifications", navNotifications);
    }

    @FXML
    private void handleNavCommunication(MouseEvent event) {
        navigateTo("communication", navCommunication);
    }

    @FXML
    private void handleNavReports(MouseEvent event) {
        navigateTo("reports", navReports);
    }

    @FXML
    private void handleNavAnalytics(MouseEvent event) {
        navigateTo("analytics", navAnalytics);
    }

    @FXML
    private void handleNavImport(MouseEvent event) {
        navigateTo("import", navImport);
    }

    @FXML
    private void handleNavDataGenerator(MouseEvent event) {
        navigateTo("data-generator", navDataGenerator);
    }

    @FXML
    private void handleNavSettings(MouseEvent event) {
        navigateTo("settings", navSettings);
    }

    @FXML
    private void handleNavUserManagement(MouseEvent event) {
        navigateTo("user-management", navUserManagement);
    }

    @FXML
    private void handleNavAuditLog(MouseEvent event) {
        navigateTo("audit-log", navAuditLog);
    }

    @FXML
    private void handleNavDatabase(MouseEvent event) {
        navigateTo("database", navDatabase);
    }

    @FXML
    private void handleNavNetworkPanel(MouseEvent event) {
        navigateTo("network-panel", navNetworkPanel);
    }

    @FXML
    private void handleNavSecureSync(MouseEvent event) {
        navigateTo("secure-sync", navSecureSync);
    }

    @FXML
    private void handleNavSecureAudit(MouseEvent event) {
        navigateTo("secure-audit", navSecureAudit);
    }

    @FXML
    private void handleHelp() {
        if (onHelp != null) {
            onHelp.run();
        }
    }

    @FXML
    private void handleLogout() {
        if (onLogout != null) {
            onLogout.run();
        }
    }

    /**
     * Internal navigation handler
     */
    private void navigateTo(String destination, HBox navItem) {
        log.debug("Navigating to: {}", destination);
        setActiveNavItem(navItem);

        if (onNavigate != null) {
            onNavigate.accept(destination);
        }
    }

    // ========================================================================
    // CALLBACK SETTERS
    // ========================================================================

    /**
     * Set navigation callback
     */
    public void setOnNavigate(Consumer<String> callback) {
        this.onNavigate = callback;
    }

    /**
     * Set quick search callback
     */
    public void setOnQuickSearch(Runnable callback) {
        this.onQuickSearch = callback;
    }

    /**
     * Set logout callback
     */
    public void setOnLogout(Runnable callback) {
        this.onLogout = callback;
    }

    /**
     * Set help callback
     */
    public void setOnHelp(Runnable callback) {
        this.onHelp = callback;
    }

    // ========================================================================
    // PUBLIC METHODS
    // ========================================================================

    /**
     * Refresh counts
     */
    public void refreshCounts() {
        Platform.runLater(this::loadCounts);
    }

    /**
     * Get the sidebar container
     */
    public VBox getSidebarContainer() {
        return sidebarContainer;
    }
}
