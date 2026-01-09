package com.heronix.ui.controller;

import com.heronix.repository.*;
import com.heronix.service.AuthenticationService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Controller for the Module Launcher Dashboard
 * Provides tile-based navigation to major system modules
 */
@Component
public class ModuleLauncherController {

    @FXML private TextField searchField;
    @FXML private Button userButton;
    @FXML private Label studentCountLabel;
    @FXML private Label teacherCountLabel;
    @FXML private Label courseCountLabel;
    @FXML private Label scheduleCountLabel;

    @Autowired
    private ApplicationContext springContext;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    private MainController mainController;

    /**
     * Initialize the module launcher
     */
    @FXML
    public void initialize() {
        setupSearchField();
        setupKeyboardShortcuts();
        loadStatistics();
        updateUserButton();
    }

    /**
     * Set the main controller for navigation
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Setup the search field functionality
     */
    private void setupSearchField() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterModules(newVal);
        });

        // Defer keyboard shortcut setup until scene is available
        Platform.runLater(() -> {
            if (searchField.getScene() != null) {
                searchField.getScene().getAccelerators().put(
                    new KeyCodeCombination(KeyCode.K, KeyCombination.CONTROL_DOWN),
                    () -> searchField.requestFocus()
                );
            }
        });
    }

    /**
     * Setup keyboard shortcuts
     */
    private void setupKeyboardShortcuts() {
        Platform.runLater(() -> {
            if (searchField.getScene() != null) {
                // Ctrl+K to focus search
                searchField.getScene().getAccelerators().put(
                    new KeyCodeCombination(KeyCode.K, KeyCombination.CONTROL_DOWN),
                    () -> searchField.requestFocus()
                );
            }
        });
    }

    /**
     * Load statistics for quick stats display
     */
    private void loadStatistics() {
        Platform.runLater(() -> {
            try {
                long studentCount = studentRepository.count();
                long teacherCount = teacherRepository.count();
                long courseCount = courseRepository.count();
                long scheduleCount = scheduleRepository.count();

                studentCountLabel.setText(String.format("%,d", studentCount));
                teacherCountLabel.setText(String.format("%,d", teacherCount));
                courseCountLabel.setText(String.format("%,d", courseCount));
                scheduleCountLabel.setText(String.format("%,d", scheduleCount));
            } catch (Exception e) {
                System.err.println("Error loading statistics: " + e.getMessage());
            }
        });
    }

    /**
     * Update user button with current user info
     */
    private void updateUserButton() {
        if (authenticationService != null && authenticationService.getCurrentUser() != null) {
            String username = authenticationService.getCurrentUser().getUsername();
            userButton.setText(username.substring(0, Math.min(2, username.length())).toUpperCase());
        } else {
            userButton.setText("?");
        }
    }

    /**
     * Filter modules based on search text
     */
    private void filterModules(String searchText) {
        // TODO: Implement filtering logic to show/hide module tiles
        // For now, this is a placeholder
    }

    // ========== Module Navigation Handlers ==========

    @FXML
    private void handleEnrollment(MouseEvent event) {
        navigateToView("Students.fxml", "Student Enrollment");
    }

    @FXML
    private void handleStudentManagement(MouseEvent event) {
        navigateToView("Students.fxml", "Student Management");
    }

    @FXML
    private void handleAcademics(MouseEvent event) {
        navigateToView("Courses.fxml", "Academic Management");
    }

    @FXML
    private void handleAttendance(MouseEvent event) {
        navigateToView("Attendance.fxml", "Attendance Tracking");
    }

    @FXML
    private void handleDiscipline(MouseEvent event) {
        navigateToView("Discipline.fxml", "Discipline Management");
    }

    @FXML
    private void handleSpecialPrograms(MouseEvent event) {
        navigateToView("SPEDDashboard.fxml", "Special Programs");
    }

    @FXML
    private void handleHealthCounseling(MouseEvent event) {
        navigateToView("HealthOffice.fxml", "Health & Counseling");
    }

    @FXML
    private void handleStaffManagement(MouseEvent event) {
        navigateToView("Teachers.fxml", "Staff Management");
    }

    @FXML
    private void handleOperations(MouseEvent event) {
        navigateToView("StudentFees.fxml", "Operations");
    }

    @FXML
    private void handleActivities(MouseEvent event) {
        navigateToView("Events.fxml", "Activities & Events");
    }

    @FXML
    private void handleCommunication(MouseEvent event) {
        navigateToView("NotificationCenter.fxml", "Communication Center");
    }

    @FXML
    private void handleReportsAnalytics(MouseEvent event) {
        navigateToView("ReportsAnalytics.fxml", "Reports & Analytics");
    }

    @FXML
    private void handleSystemAdmin(MouseEvent event) {
        navigateToView("UserManagement.fxml", "System Administration");
    }

    @FXML
    private void handleDataTools(MouseEvent event) {
        navigateToView("DatabaseManagement.fxml", "Data Tools");
    }

    @FXML
    private void handleAuditSecurity(MouseEvent event) {
        navigateToView("AuditLog.fxml", "Audit & Security");
    }

    @FXML
    private void handleSettings() {
        navigateToView("Settings.fxml", "Settings");
    }

    /**
     * Navigate to a specific view
     */
    private void navigateToView(String fxmlFile, String title) {
        if (mainController != null) {
            try {
                mainController.loadView(fxmlFile);
                System.out.println("Navigating to: " + title);
            } catch (Exception e) {
                System.err.println("Error navigating to " + title + ": " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("MainController not set. Cannot navigate.");
        }
    }

    /**
     * Load a view directly into the launcher area (for testing)
     */
    public void loadViewInPlace(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlFile));
            loader.setControllerFactory(springContext::getBean);
            Parent view = loader.load();

            // Get the root BorderPane and replace center content
            BorderPane root = (BorderPane) searchField.getScene().getRoot();
            root.setCenter(view);
        } catch (IOException e) {
            System.err.println("Error loading view: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
