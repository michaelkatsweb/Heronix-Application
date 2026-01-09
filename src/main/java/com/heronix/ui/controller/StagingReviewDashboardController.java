package com.heronix.ui.controller;

import com.heronix.service.StagingDataImportService;
import com.heronix.service.StagingDataImportService.ImportResult;
import com.heronix.service.StagingDataImportService.StagedSubmission;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Staging Review Dashboard Controller
 *
 * JavaFX controller for admin interface to review and import data from staging server
 *
 * Features:
 * - View pending submissions
 * - Import student registrations
 * - Import parent updates
 * - Import teacher submissions
 * - View import statistics
 *
 * @author Heronix Development Team
 * @version 1.0.0
 * @since December 27, 2025
 */
@Slf4j
@Component
public class StagingReviewDashboardController {

    @Autowired
    private StagingDataImportService stagingImportService;

    // Header
    @FXML private Label lblServerStatus;
    @FXML private Button btnRefreshHealth;

    // Tab 1: Student Registrations
    @FXML private Button btnRefreshStudents;
    @FXML private Button btnImportStudents;
    @FXML private TableView<StagedSubmission> tblStudentRegistrations;
    @FXML private TableColumn<StagedSubmission, Long> colStudentStagingId;
    @FXML private TableColumn<StagedSubmission, String> colStudentName;
    @FXML private TableColumn<StagedSubmission, String> colStudentGrade;
    @FXML private TableColumn<StagedSubmission, String> colStudentSubmittedBy;
    @FXML private TableColumn<StagedSubmission, LocalDateTime> colStudentSubmittedAt;
    @FXML private TableColumn<StagedSubmission, String> colStudentStatus;
    @FXML private TableColumn<StagedSubmission, String> colStudentReviewedBy;
    @FXML private TableColumn<StagedSubmission, Void> colStudentActions;
    @FXML private Label lblStudentCount;
    @FXML private Label lblLastStudentImport;

    // Tab 2: Parent Updates
    @FXML private Button btnRefreshParents;
    @FXML private Button btnImportParents;
    @FXML private TableView<StagedSubmission> tblParentUpdates;
    @FXML private TableColumn<StagedSubmission, Long> colParentStagingId;
    @FXML private TableColumn<StagedSubmission, String> colParentStudent;
    @FXML private TableColumn<StagedSubmission, String> colParentUpdateType;
    @FXML private TableColumn<StagedSubmission, String> colParentSubmittedBy;
    @FXML private TableColumn<StagedSubmission, LocalDateTime> colParentSubmittedAt;
    @FXML private TableColumn<StagedSubmission, String> colParentStatus;
    @FXML private TableColumn<StagedSubmission, Void> colParentActions;
    @FXML private Label lblParentCount;
    @FXML private Label lblLastParentImport;

    // Tab 3: Teacher Submissions
    @FXML private Button btnRefreshTeachers;
    @FXML private Button btnImportTeachers;
    @FXML private TableView<StagedSubmission> tblTeacherSubmissions;
    @FXML private TableColumn<StagedSubmission, Long> colTeacherStagingId;
    @FXML private TableColumn<StagedSubmission, String> colTeacherName;
    @FXML private TableColumn<StagedSubmission, String> colTeacherSubmissionType;
    @FXML private TableColumn<StagedSubmission, String> colTeacherCourse;
    @FXML private TableColumn<StagedSubmission, LocalDateTime> colTeacherSubmittedAt;
    @FXML private TableColumn<StagedSubmission, String> colTeacherStatus;
    @FXML private TableColumn<StagedSubmission, Void> colTeacherActions;
    @FXML private Label lblTeacherCount;
    @FXML private Label lblLastTeacherImport;

    // Tab 4: Statistics
    @FXML private Label lblTodayImports;
    @FXML private Label lblWeekImports;
    @FXML private Label lblMonthImports;
    @FXML private Label lblSuccessRate;
    @FXML private Label lblPendingCount;
    @FXML private Label lblFailedCount;
    @FXML private TableView<?> tblImportHistory;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        log.info("Initializing StagingReviewDashboardController");

        setupStudentTable();
        setupParentTable();
        setupTeacherTable();

        // Auto-refresh on load
        Platform.runLater(this::refreshAll);
    }

    /**
     * Setup student registrations table
     */
    private void setupStudentTable() {
        colStudentStagingId.setCellValueFactory(new PropertyValueFactory<>("stagingId"));
        colStudentSubmittedBy.setCellValueFactory(new PropertyValueFactory<>("submittedBy"));
        colStudentSubmittedAt.setCellValueFactory(new PropertyValueFactory<>("submittedAt"));
        colStudentStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStudentReviewedBy.setCellValueFactory(new PropertyValueFactory<>("reviewedBy"));

        // Format date column
        colStudentSubmittedAt.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(dateFormatter));
            }
        });
    }

    /**
     * Setup parent updates table
     */
    private void setupParentTable() {
        colParentStagingId.setCellValueFactory(new PropertyValueFactory<>("stagingId"));
        colParentUpdateType.setCellValueFactory(new PropertyValueFactory<>("submissionType"));
        colParentSubmittedBy.setCellValueFactory(new PropertyValueFactory<>("submittedBy"));
        colParentSubmittedAt.setCellValueFactory(new PropertyValueFactory<>("submittedAt"));
        colParentStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Format date column
        colParentSubmittedAt.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(dateFormatter));
            }
        });
    }

    /**
     * Setup teacher submissions table
     */
    private void setupTeacherTable() {
        colTeacherStagingId.setCellValueFactory(new PropertyValueFactory<>("stagingId"));
        colTeacherSubmissionType.setCellValueFactory(new PropertyValueFactory<>("submissionType"));
        colTeacherSubmittedAt.setCellValueFactory(new PropertyValueFactory<>("submittedAt"));
        colTeacherStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Format date column
        colTeacherSubmittedAt.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(dateFormatter));
            }
        });
    }

    /**
     * Refresh all data
     */
    private void refreshAll() {
        refreshStudentRegistrations();
        refreshParentUpdates();
        refreshTeacherSubmissions();
        checkStagingServerHealth();
    }

    /**
     * Check staging server health
     */
    @FXML
    public void checkStagingServerHealth() {
        log.info("Checking staging server health");

        // TODO: Implement actual health check
        lblServerStatus.setText("Not Connected");
        lblServerStatus.getStyleClass().clear();
        lblServerStatus.getStyleClass().add("status-disconnected");

        showInfo("Staging Server", "Staging server integration not yet configured.\n" +
            "This will check connectivity when staging server is deployed.");
    }

    /**
     * Refresh student registrations
     */
    @FXML
    public void refreshStudentRegistrations() {
        log.info("Refreshing student registrations");

        List<StagedSubmission> submissions = stagingImportService.fetchPendingSubmissions("STUDENT_REGISTRATION");

        ObservableList<StagedSubmission> data = FXCollections.observableArrayList(submissions);
        tblStudentRegistrations.setItems(data);

        lblStudentCount.setText(submissions.size() + " pending registrations");
        lblPendingCount.setText(String.valueOf(submissions.size()));
    }

    /**
     * Import student registrations
     */
    @FXML
    public void importStudentRegistrations() {
        log.info("Importing student registrations");

        // Confirm action
        Optional<ButtonType> confirm = showConfirmation(
            "Import Student Registrations",
            "Import all approved student registrations from staging server?",
            "This will create new student records in the SIS database.");

        if (confirm.isEmpty() || confirm.get() != ButtonType.OK) {
            return;
        }

        // Get current user
        String currentUser = getCurrentUsername();

        // Perform import
        ImportResult result = stagingImportService.importNewStudentRegistrations(currentUser);

        // Show result
        if (result.getFailureCount() == 0) {
            showSuccess("Import Complete",
                String.format("Successfully imported %d student registrations", result.getSuccessCount()));
        } else {
            showError("Import Completed with Errors",
                result.toString() + "\n\nErrors:\n" + String.join("\n", result.getErrors()));
        }

        // Refresh table
        refreshStudentRegistrations();
        lblLastStudentImport.setText("Last import: Just now");
    }

    /**
     * Refresh parent updates
     */
    @FXML
    public void refreshParentUpdates() {
        log.info("Refreshing parent updates");

        List<StagedSubmission> submissions = stagingImportService.fetchPendingSubmissions("PARENT_UPDATE");

        ObservableList<StagedSubmission> data = FXCollections.observableArrayList(submissions);
        tblParentUpdates.setItems(data);

        lblParentCount.setText(submissions.size() + " pending updates");
    }

    /**
     * Import parent updates
     */
    @FXML
    public void importParentUpdates() {
        log.info("Importing parent updates");

        Optional<ButtonType> confirm = showConfirmation(
            "Import Parent Updates",
            "Import all approved parent/guardian updates from staging server?",
            "This will update existing student records with new parent information.");

        if (confirm.isEmpty() || confirm.get() != ButtonType.OK) {
            return;
        }

        String currentUser = getCurrentUsername();
        ImportResult result = stagingImportService.importParentUpdates(currentUser);

        if (result.getFailureCount() == 0) {
            showSuccess("Import Complete",
                String.format("Successfully imported %d parent updates", result.getSuccessCount()));
        } else {
            showError("Import Completed with Errors", result.toString());
        }

        refreshParentUpdates();
        lblLastParentImport.setText("Last import: Just now");
    }

    /**
     * Refresh teacher submissions
     */
    @FXML
    public void refreshTeacherSubmissions() {
        log.info("Refreshing teacher submissions");

        List<StagedSubmission> submissions = stagingImportService.fetchPendingSubmissions("TEACHER_SUBMISSION");

        ObservableList<StagedSubmission> data = FXCollections.observableArrayList(submissions);
        tblTeacherSubmissions.setItems(data);

        lblTeacherCount.setText(submissions.size() + " pending submissions");
    }

    /**
     * Import teacher submissions
     */
    @FXML
    public void importTeacherSubmissions() {
        log.info("Importing teacher submissions");

        Optional<ButtonType> confirm = showConfirmation(
            "Import Teacher Submissions",
            "Import all approved teacher submissions from staging server?",
            "This will import grades, attendance, and other teacher data.");

        if (confirm.isEmpty() || confirm.get() != ButtonType.OK) {
            return;
        }

        String currentUser = getCurrentUsername();
        ImportResult result = stagingImportService.importTeacherSubmissions(currentUser);

        if (result.getFailureCount() == 0) {
            showSuccess("Import Complete",
                String.format("Successfully imported %d teacher submissions", result.getSuccessCount()));
        } else {
            showError("Import Completed with Errors", result.toString());
        }

        refreshTeacherSubmissions();
        lblLastTeacherImport.setText("Last import: Just now");
    }

    /**
     * Get current username from security context
     */
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SYSTEM";
    }

    /**
     * Show success alert
     */
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show error alert
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show info alert
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show confirmation dialog
     */
    private Optional<ButtonType> showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert.showAndWait();
    }
}
