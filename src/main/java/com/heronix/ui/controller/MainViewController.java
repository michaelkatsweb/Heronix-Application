package com.heronix.ui.controller;

import com.heronix.ui.util.ReportDialogLauncher;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;

/**
 * Main View Controller - Application Dashboard
 * Location:
 * src/main/java/com/heronix/ui/controller/MainViewController.java
 * 
 * Central hub for navigating to all features:
 * - View Schedule
 * - Generate Schedule
 * - Import Data
 * - Manage Teachers
 * - Manage Courses
 * - Manage Rooms
 * - Reports & Analytics
 * 
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since 2025-10-10
 */
@Slf4j
@Controller
public class MainViewController {

    private ConfigurableApplicationContext springContext;

    @Autowired
    private ReportDialogLauncher reportDialogLauncher;

    @FXML
    private BorderPane mainContainer;
    @FXML
    private StackPane contentArea;
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label subtitleLabel;

    /**
     * Set Spring context for loading other views
     */
    public void setSpringContext(ConfigurableApplicationContext context) {
        this.springContext = context;
    }

    /**
     * Initialize the main view
     */
    @FXML
    public void initialize() {
        log.info("╔════════════════════════════════════════════════════════════════╗");
        log.info("║   EDUSCHEDULER PRO - MAIN DASHBOARD                            ║");
        log.info("╚════════════════════════════════════════════════════════════════╝");

        welcomeLabel.setText("Welcome to Heronix Scheduling System");
        subtitleLabel.setText("Your Intelligent School Scheduling Solution");

        log.info("✓ Main Dashboard initialized");
    }

    /**
     * Navigate to View Schedule
     */
    @FXML
    private void handleViewSchedule() {
        log.info("Navigating to Schedule View...");
        loadView("/fxml/ScheduleView.fxml", "📅 Master Schedule View");
    }

    /**
     * Navigate to Generate Schedule
     */
    @FXML
    private void handleGenerateSchedule() {
        log.info("Navigating to Generate Schedule...");
        loadView("/fxml/ScheduleGenerator.fxml", "🤖 Schedule Generator");
    }

    /**
     * Navigate to Import Wizard
     */
    @FXML
    private void handleImportData() {
        log.info("Navigating to Import Wizard...");
        loadView("/fxml/ImportWizard.fxml", "📥 Import Wizard");
    }

    /**
     * Navigate to Manage Teachers
     */
    @FXML
    private void handleManageTeachers() {
        log.info("Navigating to Manage Teachers...");
        loadView("/fxml/TeacherManagement.fxml", "Teacher Management");
    }

    /**
     * Navigate to Manage Courses
     */
    @FXML
    private void handleManageCourses() {
        log.info("Navigating to Manage Courses...");
        loadView("/fxml/CourseManagement.fxml", "Course Management");
    }

    /**
     * Navigate to Manage Rooms
     */
    @FXML
    private void handleManageRooms() {
        log.info("Navigating to Manage Rooms...");
        loadView("/fxml/RoomManagement.fxml", "Room Management");
    }

    /**
     * Navigate to Reports
     */
    @FXML
    private void handleReports() {
        log.info("Navigating to Reports...");
        loadView("/fxml/ReportsAnalytics.fxml", "Reports & Analytics");
    }

    /**
     * Navigate to At-Risk Students Report
     */
    @FXML
    private void handleAtRiskStudents() {
        log.info("Navigating to At-Risk Students Report...");
        loadView("/fxml/at-risk-students.fxml", "At-Risk Students Report");
    }

    /**
     * Show settings
     */
    @FXML
    private void handleSettings() {
        log.info("Opening settings...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Settings.fxml"));
            loader.setControllerFactory(springContext::getBean);

            Parent root = loader.load();

            // Get controller and set the dialog stage
            SettingsController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Application Settings");
            dialogStage.setScene(new Scene(root));
            dialogStage.setMinWidth(900);
            dialogStage.setMinHeight(700);

            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            log.info("Settings dialog closed");

        } catch (Exception e) {
            log.error("Error loading Settings dialog", e);
            showError("Navigation Error",
                    "Failed to load Settings.\n\n" +
                            "Error: " + e.getMessage());
        }
    }

    /**
     * Show about dialog
     */
    @FXML
    private void handleAbout() {
        showInfo("About Heronix Scheduling System",
                "Heronix Scheduling System v1.0.0\n\n" +
                        "Intelligent School Scheduling System\n" +
                        "Powered by AI (OptaPlanner)\n\n" +
                        "© 2025 Heronix Scheduling System Team\n" +
                        "All rights reserved.\n\n" +
                        "Built with:\n" +
                        "• Java 17+\n" +
                        "• Spring Boot 3\n" +
                        "• JavaFX 21\n" +
                        "• OptaPlanner 9.40");
    }

    /**
     * Exit application
     */
    @FXML
    private void handleExit() {
        log.info("Exit requested");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Application");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("Any unsaved changes will be lost.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                log.info("Exiting application...");
                System.exit(0);
            }
        });
    }

    /**
     * Show Teachers tab - wrapper for menu compatibility
     */
    @FXML
    private void showTeachersTab() {
        handleManageTeachers();
    }

    /**
     * Show Co-Teacher Staff Management
     */
    @FXML
    private void showCoTeacherStaffTab() {
        log.info("Navigating to Co-Teacher Staff Management...");
        loadView("/fxml/CoTeacherStaffManagement.fxml", "Co-Teacher Staff Management");
    }

    /**
     * Show Paraprofessional Staff Management tab
     */
    @FXML
    private void showParaprofessionalStaffTab() {
        log.info("Navigating to Paraprofessional Staff Management...");
        loadView("/fxml/ParaprofessionalStaffManagement.fxml", "Paraprofessional Staff Management");
    }

    /**
     * Show Courses tab - wrapper for menu compatibility
     */
    @FXML
    private void showCoursesTab() {
        handleManageCourses();
    }

    /**
     * Show Rooms tab - wrapper for menu compatibility
     */
    @FXML
    private void showRoomsTab() {
        handleManageRooms();
    }

    /**
     * Show Student Enrollment Management
     */
    @FXML
    private void showStudentEnrollmentTab() {
        log.info("Navigating to Student Enrollment...");
        loadView("/fxml/StudentEnrollment.fxml", "Student Course Enrollment");
    }

    /**
     * Show Lunch Period Management
     */
    @FXML
    private void handleLunchPeriods() {
        log.info("Navigating to Lunch Period Management...");
        loadView("/fxml/LunchPeriodManagement.fxml", "Lunch Period Management");
    }

    /**
     * Show Special Event Blocks Management
     */
    @FXML
    private void handleSpecialEventBlocks() {
        log.info("Navigating to Special Event Blocks...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SpecialEventBlockDialog.fxml"));
            loader.setControllerFactory(springContext::getBean);

            Parent root = loader.load();

            // Get controller and set the dialog stage
            SpecialEventBlockController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Special Event Blocks Management");
            dialogStage.setScene(new Scene(root));
            dialogStage.setMinWidth(800);
            dialogStage.setMinHeight(600);

            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            log.info("Special Event Blocks dialog closed");

        } catch (Exception e) {
            log.error("Error loading Special Event Blocks dialog", e);
            showError("Navigation Error",
                    "Failed to load Special Event Blocks.\n\n" +
                            "Error: " + e.getMessage());
        }
    }

    /**
     * Load a view in a new window
     */
    private void loadView(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(springContext::getBean);

            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();

            log.info("✓ Loaded view: {}", title);

        } catch (Exception e) {
            log.error("Error loading view: {}", fxmlPath, e);
            showError("Navigation Error",
                    "Failed to load view.\n\n" +
                            "Error: " + e.getMessage());
        }
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

    /**
     * Navigate to Notification Center
     */
    @FXML
    private void handleNotificationCenter() {
        log.info("Navigating to Notification Center...");
        loadView("/fxml/NotificationCenter.fxml", "🔔 Notification Center");
    }

    /**
     * Navigate to SIS Export
     */
    @FXML
    private void handleSISExport() {
        log.info("Navigating to SIS Export...");
        loadView("/fxml/SISExport.fxml", "📤 SIS Export");
    }

    /**
     * Navigate to SPED Scheduling Dashboard
     */
    @FXML
    private void handleSPEDScheduling() {
        log.info("Navigating to SPED Scheduling Dashboard...");
        loadView("/fxml/SPEDSchedulingDashboard.fxml", "📋 SPED Scheduling");
    }

    /**
     * Navigate to Enrollment Forecasting Dashboard
     */
    @FXML
    private void handleEnrollmentForecasting() {
        log.info("Navigating to Enrollment Forecasting Dashboard...");
        loadView("/fxml/ForecastingDashboard.fxml", "📊 Enrollment Forecasting");
    }

    /**
     * Navigate to Attendance Management
     */
    @FXML
    private void handleAttendanceManagement() {
        log.info("Navigating to Attendance Management...");
        loadView("/fxml/Attendance.fxml", "📊 Attendance Management");
    }

    /**
     * Navigate to Attendance Reporting
     */
    @FXML
    private void handleAttendanceReporting() {
        log.info("Navigating to Attendance Reporting...");
        loadView("/fxml/AttendanceReporting.fxml", "📈 Attendance Reports & Analytics");
    }

    /**
     * Navigate to Hall Pass Management
     */
    @FXML
    private void handleHallPassManagement() {
        log.info("Navigating to Hall Pass Management...");
        loadView("/fxml/HallPass.fxml", "🎫 Hall Pass Management");
    }

    /**
     * Navigate to Truancy Intervention
     */
    @FXML
    private void handleTruancyIntervention() {
        log.info("Navigating to Truancy Intervention...");
        loadView("/fxml/TruancyIntervention.fxml", "⚠️ Truancy Intervention");
    }

    // ==========================================================================
    // NEW MENU HANDLERS - Service-Based Organization
    // ==========================================================================

    // ========== FILE MENU HANDLERS ==========
    @FXML
    private void handleNewSchoolYear() {
        log.info("Creating new school year...");
        showInfo("New School Year", "New School Year creation feature coming soon!");
    }

    @FXML
    private void handleExportData() {
        log.info("Exporting data...");
        showInfo("Export Data", "Data export feature coming soon!");
    }

    @FXML
    private void handleDatabaseBackup() {
        log.info("Creating database backup...");
        showInfo("Database Backup", "Database backup feature coming soon!");
    }

    @FXML
    private void handleDatabaseRestore() {
        log.info("Restoring database...");
        showInfo("Database Restore", "Database restore feature coming soon!");
    }

    // ========== ENROLLMENT MENU HANDLERS ==========
    @FXML
    private void handleStudentEnrollment() {
        log.info("Navigating to Student Enrollment...");
        loadView("/fxml/StudentEnrollment.fxml", "📝 Student Enrollment");
    }

    @FXML
    private void handleTransferStudents() {
        log.info("Navigating to Transfer Students...");
        showInfo("Transfer Students", "Transfer student processing feature coming soon!");
    }

    @FXML
    private void handleEnrollmentVerification() {
        log.info("Navigating to Enrollment Verification...");
        showInfo("Enrollment Verification", "Enrollment verification feature coming soon!");
    }

    @FXML
    private void handleOnlineEnrollment() {
        log.info("Navigating to Online Enrollment...");
        showInfo("Online Enrollment", "Online enrollment portal feature coming soon!");
    }

    @FXML
    private void handleWithdrawal() {
        log.info("Navigating to Withdrawal Processing...");
        showInfo("Withdrawal Processing", "Student withdrawal feature coming soon!");
    }

    @FXML
    private void handleEnrollmentReports() {
        log.info("Navigating to Enrollment Reports...");
        showInfo("Enrollment Reports", "Enrollment reporting feature coming soon!");
    }

    // ========== STUDENTS MENU HANDLERS ==========
    @FXML
    private void handleStudentDemographics() {
        log.info("Navigating to Student Demographics...");
        showInfo("Student Demographics", "Student demographics management coming soon!");
    }

    @FXML
    private void handleParentGuardian() {
        log.info("Navigating to Parent/Guardian Management...");
        showInfo("Parent/Guardian Management", "Parent/guardian management feature coming soon!");
    }

    @FXML
    private void handleEmergencyContacts() {
        log.info("Navigating to Emergency Contacts...");
        showInfo("Emergency Contacts", "Emergency contact management coming soon!");
    }

    @FXML
    private void handleMedicalInfo() {
        log.info("Navigating to Medical Information...");
        showInfo("Medical Information", "Medical information management coming soon!");
    }

    @FXML
    private void handleAccommodations() {
        log.info("Navigating to Student Accommodations...");
        showInfo("Student Accommodations", "Student accommodations management coming soon!");
    }

    @FXML
    private void handleStudentGroups() {
        log.info("Navigating to Student Groups...");
        showInfo("Student Groups", "Student groups & categories feature coming soon!");
    }

    @FXML
    private void handleStudentSearch() {
        log.info("Navigating to Student Search...");
        showInfo("Student Search", "Student search feature coming soon!");
    }

    // ========== ACADEMICS MENU HANDLERS ==========
    @FXML
    private void handleCourseCatalog() {
        log.info("Navigating to Course Catalog...");
        loadView("/fxml/CourseManagement.fxml", "📚 Course Catalog");
    }

    @FXML
    private void handleCourseSections() {
        log.info("Navigating to Course Sections...");
        loadView("/fxml/SectionManagementDialog.fxml", "📋 Course Sections");
    }

    @FXML
    private void handleSectionEnrollment() {
        log.info("Navigating to Section Enrollment...");
        loadView("/fxml/StudentEnrollment.fxml", "✏️ Section Enrollment");
    }

    @FXML
    private void handleCourseRequests() {
        log.info("Navigating to Course Requests...");
        loadView("/fxml/EnrollmentRequestManagement.fxml", "📝 Course Requests");
    }

    @FXML
    private void handleScheduleGeneration() {
        log.info("Navigating to Schedule Generation...");
        loadView("/fxml/ScheduleGenerator.fxml", "🤖 Schedule Generation");
    }

    @FXML
    private void handleScheduleChanges() {
        log.info("Navigating to Schedule Changes...");
        loadView("/fxml/DragDropScheduleEditor.fxml", "🔄 Schedule Changes");
    }

    @FXML
    private void handleStudentScheduleViewer() {
        log.info("Navigating to Student Schedules...");
        loadView("/fxml/ScheduleView.fxml", "📅 Student Schedules");
    }

    @FXML
    private void handleGradeEntry() {
        log.info("Navigating to Grade Entry...");
        loadView("/fxml/AdminGradebook.fxml", "📝 Grade Entry");
    }

    @FXML
    private void handleGradebook() {
        log.info("Navigating to Gradebook...");
        loadView("/fxml/Gradebook.fxml", "📚 Gradebook");
    }

    @FXML
    private void handleAssignments() {
        log.info("Navigating to Assignment Management...");
        loadView("/fxml/AssignmentReports.fxml", "📋 Assignment Management");
    }

    @FXML
    private void handleRubrics() {
        log.info("Navigating to Rubrics...");
        loadView("/fxml/GradeProgression.fxml", "📊 Rubrics & Grading");
    }

    @FXML
    private void handleReportCards() {
        log.info("Navigating to Report Cards...");
        loadView("/fxml/ReportCardGeneration.fxml", "🎓 Report Cards");
    }

    @FXML
    private void handleProgressReports() {
        log.info("Navigating to Progress Reports...");
        loadView("/fxml/ReportsAnalytics.fxml", "📈 Progress Reports");
    }

    @FXML
    private void handleGPAClassRank() {
        log.info("Navigating to GPA & Class Rank...");
        loadView("/fxml/GradeProgression.fxml", "🎯 GPA & Class Rank");
    }

    @FXML
    private void handleTranscripts() {
        log.info("Navigating to Transcript Management...");
        loadView("/fxml/ReportsAnalytics.fxml", "📄 Transcripts");
    }

    @FXML
    private void handleStandardizedTesting() {
        log.info("Navigating to Standardized Testing...");
        loadView("/fxml/ReportsAnalytics.fxml", "📊 Standardized Testing");
    }

    @FXML
    private void handleGraduationRequirements() {
        log.info("Navigating to Graduation Requirements...");
        loadView("/fxml/AcademicPlanManagement.fxml", "🎓 Graduation Requirements");
    }

    // ========== ATTENDANCE MENU HANDLERS ==========
    @FXML
    private void handleDailyAttendance() {
        log.info("Navigating to Daily Attendance...");
        loadView("/fxml/Attendance.fxml", "✅ Daily Attendance");
    }

    @FXML
    private void handlePeriodAttendance() {
        log.info("Navigating to Period Attendance...");
        loadView("/fxml/Attendance.fxml", "✅ Period Attendance");
    }

    @FXML
    private void handleAttendanceTracking() {
        log.info("Navigating to Attendance Tracking...");
        loadView("/fxml/AttendanceConfiguration.fxml", "🔍 Attendance Tracking");
    }

    @FXML
    private void handleChronicAbsenteeism() {
        log.info("Navigating to Chronic Absenteeism...");
        loadView("/fxml/TruancyIntervention.fxml", "⚠️ Chronic Absenteeism");
    }

    @FXML
    private void handleADAReports() {
        log.info("Navigating to ADA/ADM Reports...");
        loadView("/fxml/AttendanceReports.fxml", "📊 ADA/ADM Reports");
    }

    @FXML
    private void handleAttendanceNotifications() {
        log.info("Navigating to Attendance Notifications...");
        loadView("/fxml/AttendanceReporting.fxml", "🔔 Attendance Notifications");
    }

    // ========== DISCIPLINE MENU HANDLERS ==========
    @FXML
    private void handleIncidentManagement() {
        log.info("Navigating to Incident Management...");
        loadView("/fxml/BehaviorIncidentForm.fxml", "🚨 Incident Management");
    }

    @FXML
    private void handleReferrals() {
        log.info("Navigating to Disciplinary Referrals...");
        loadView("/fxml/BehaviorIncidentForm.fxml", "📋 Disciplinary Referrals");
    }

    @FXML
    private void handleConsequences() {
        log.info("Navigating to Consequence Management...");
        loadView("/fxml/BehaviorDashboard.fxml", "⚖️ Consequence Management");
    }

    @FXML
    private void handleSuspensionExpulsion() {
        log.info("Navigating to Suspension & Expulsion...");
        loadView("/fxml/SuspensionManagement.fxml", "🚫 Suspension & Expulsion");
    }

    @FXML
    private void handleISS() {
        log.info("Navigating to In-School Suspension...");
        loadView("/fxml/SuspensionManagement.fxml", "🏫 In-School Suspension");
    }

    @FXML
    private void handleBehaviorTracking() {
        log.info("Navigating to Behavior Tracking...");
        loadView("/fxml/BehaviorDashboard.fxml", "📊 Behavior Tracking");
    }

    @FXML
    private void handleRestorativePractices() {
        log.info("Navigating to Restorative Practices...");
        loadView("/fxml/BehaviorDashboard.fxml", "🤝 Restorative Practices");
    }

    @FXML
    private void handleDisciplineReports() {
        log.info("Navigating to Discipline Reports...");
        loadView("/fxml/DisciplineReports.fxml", "📈 Discipline Reports");
    }

    // ========== SPECIAL PROGRAMS MENU HANDLERS ==========
    @FXML
    private void handleIEPManagement() {
        log.info("Navigating to IEP Management...");
        loadView("/fxml/IEPManagement.fxml", "📝 IEP Management");
    }

    @FXML
    private void handle504Plans() {
        log.info("Navigating to 504 Plan Management...");
        loadView("/fxml/Plan504Management.fxml", "📋 504 Plan Management");
    }

    @FXML
    private void handleRTI() {
        log.info("Navigating to RTI...");
        loadView("/fxml/SPEDDashboard.fxml", "🎯 Response to Intervention");
    }

    @FXML
    private void handleSpecialEdServices() {
        log.info("Navigating to Special Ed Services...");
        loadView("/fxml/SPEDSchedulingDashboard.fxml", "🧩 Special Ed Services");
    }

    @FXML
    private void handleSPEDCompliance() {
        log.info("Navigating to SPED Compliance...");
        loadView("/fxml/SPEDDashboard.fxml", "✅ SPED Compliance");
    }

    @FXML
    private void handleELLServices() {
        log.info("Navigating to ELL/ESL Services...");
        loadView("/fxml/ELLDashboard.fxml", "🌐 ELL/ESL Services");
    }

    @FXML
    private void handleGiftedTalented() {
        log.info("Navigating to Gifted & Talented...");
        loadView("/fxml/SPEDDashboard.fxml", "⭐ Gifted & Talented");
    }

    @FXML
    private void handleTitleI() {
        log.info("Navigating to Title I Programs...");
        loadView("/fxml/SPEDDashboard.fxml", "📚 Title I Programs");
    }

    @FXML
    private void handleMcKinneyVento() {
        log.info("Navigating to McKinney-Vento...");
        loadView("/fxml/SPEDDashboard.fxml", "🏠 McKinney-Vento");
    }

    // ========== HEALTH & COUNSELING MENU HANDLERS ==========
    @FXML
    private void handleHealthOffice() {
        log.info("Navigating to Health Office Management...");
        loadView("/fxml/HealthOfficeDashboard.fxml", "🏥 Health Office");
    }

    @FXML
    private void handleImmunizations() {
        log.info("Navigating to Immunization Management...");
        loadView("/fxml/HealthScreening.fxml", "💉 Immunizations");
    }

    @FXML
    private void handleMedications() {
        log.info("Navigating to Medication Management...");
        loadView("/fxml/MedicalRecordDialog.fxml", "💊 Medications");
    }

    @FXML
    private void handleHealthScreenings() {
        log.info("Navigating to Health Screenings...");
        loadView("/fxml/HealthScreening.fxml", "🔍 Health Screenings");
    }

    @FXML
    private void handleHealthPlans() {
        log.info("Navigating to Health Plans...");
        loadView("/fxml/MedicalInformationForm.fxml", "📋 Health Plans");
    }

    @FXML
    private void handleHealthReports() {
        log.info("Navigating to Health Reports...");
        loadView("/fxml/HealthOfficeDashboard.fxml", "📊 Health Reports");
    }

    @FXML
    private void handleStudentCounseling() {
        log.info("Navigating to Student Counseling...");
        loadView("/fxml/at-risk-students.fxml", "👥 Student Counseling");
    }

    @FXML
    private void handleAcademicCounseling() {
        log.info("Navigating to Academic Counseling...");
        loadView("/fxml/AcademicPlanManagement.fxml", "🎓 Academic Counseling");
    }

    @FXML
    private void handleCollegePlanning() {
        log.info("Navigating to College & Career Planning...");
        loadView("/fxml/AcademicPlanManagement.fxml", "🎯 College Planning");
    }

    @FXML
    private void handleSocialServices() {
        log.info("Navigating to Social Services...");
        loadView("/fxml/at-risk-students.fxml", "🤝 Social Services");
    }

    @FXML
    private void handleCrisisIntervention() {
        log.info("Navigating to Crisis Intervention...");
        loadView("/fxml/CrisisIntervention.fxml", "🚨 Crisis Intervention");
    }

    // ========== STAFF MENU HANDLERS (Additional) ==========
    @FXML
    private void handleCertifications() {
        log.info("Navigating to Certifications & Licensing...");
        loadView("/fxml/TeacherManagement.fxml", "📜 Certifications");
    }

    @FXML
    private void handleProfessionalDevelopment() {
        log.info("Navigating to Professional Development...");
        loadView("/fxml/TeacherManagement.fxml", "📚 Professional Development");
    }

    @FXML
    private void handleTeacherSchedules() {
        log.info("Navigating to Teacher Schedules...");
        loadView("/fxml/TeacherDashboard.fxml", "📅 Teacher Schedules");
    }

    @FXML
    private void handleTeacherEvaluations() {
        log.info("Navigating to Teacher Evaluations...");
        loadView("/fxml/TeacherManagement.fxml", "⭐ Teacher Evaluations");
    }

    @FXML
    private void handleSubstituteManagement() {
        log.info("Navigating to Substitute Management...");
        loadView("/fxml/SubstituteManagement.fxml", "👥 Substitute Management");
    }

    @FXML
    private void handleStaffDirectory() {
        log.info("Navigating to Staff Directory...");
        loadView("/fxml/Teachers.fxml", "📖 Staff Directory");
    }

    // ========== OPERATIONS MENU HANDLERS ==========
    @FXML
    private void handleFeeCatalog() {
        log.info("Navigating to Fee Catalog...");
        showInfo("Fee Catalog", "Fee catalog management coming soon!");
    }

    @FXML
    private void handleFeeAssignment() {
        log.info("Navigating to Fee Assignment...");
        showInfo("Fee Assignment", "Fee assignment feature coming soon!");
    }

    @FXML
    private void handlePaymentProcessing() {
        log.info("Navigating to Payment Processing...");
        showInfo("Payment Processing", "Payment processing feature coming soon!");
    }

    @FXML
    private void handleFeeReports() {
        log.info("Navigating to Fee Reports...");
        showInfo("Fee Reports", "Fee reporting feature coming soon!");
    }

    @FXML
    private void handleLunchAccounts() {
        log.info("Navigating to Lunch Account Management...");
        loadView("/fxml/LunchPeriodManagement.fxml", "🍽️ Lunch Accounts");
    }

    @FXML
    private void handleMealTracking() {
        log.info("Navigating to Meal Tracking...");
        loadView("/fxml/LunchPeriodManagement.fxml", "📊 Meal Tracking");
    }

    @FXML
    private void handleCafeteriaPOS() {
        log.info("Navigating to Cafeteria POS...");
        loadView("/fxml/LunchPeriodManagement.fxml", "💳 Cafeteria POS");
    }

    @FXML
    private void handleFoodServiceReports() {
        log.info("Navigating to Food Service Reports...");
        loadView("/fxml/ReportsAnalytics.fxml", "📈 Food Service Reports");
    }

    @FXML
    private void handleStudentTransportation() {
        log.info("Navigating to Student Transportation...");
        loadView("/fxml/TransportationManagement.fxml", "🚌 Student Transportation");
    }

    @FXML
    private void handleBusManagement() {
        log.info("Navigating to Bus Management...");
        loadView("/fxml/TransportationManagement.fxml", "🚍 Bus Management");
    }

    @FXML
    private void handleRoutePlanning() {
        log.info("Navigating to Route Planning...");
        loadView("/fxml/TransportationManagement.fxml", "🗺️ Route Planning");
    }

    @FXML
    private void handleTransportationReports() {
        log.info("Navigating to Transportation Reports...");
        loadView("/fxml/ReportsAnalytics.fxml", "📊 Transportation Reports");
    }

    @FXML
    private void handleRoomScheduling() {
        log.info("Navigating to Room Scheduling...");
        loadView("/fxml/RoomManagement.fxml", "🏫 Room Scheduling");
    }

    @FXML
    private void handleRoomUtilization() {
        log.info("Navigating to Room Utilization...");
        loadView("/fxml/RoomUtilizationDashboard.fxml", "📊 Room Utilization");
    }

    @FXML
    private void handleRoomReservations() {
        log.info("Navigating to Room Reservations...");
        loadView("/fxml/Events.fxml", "📅 Room Reservations");
    }

    @FXML
    private void handleLibraryServices() {
        log.info("Navigating to Library Services...");
        loadView("/fxml/RoomManagement.fxml", "📚 Library Services");
    }

    // ========== ACTIVITIES MENU HANDLERS ==========
    @FXML
    private void handleAthleticEligibility() {
        log.info("Navigating to Athletic Eligibility...");
        loadView("/fxml/Events.fxml", "🏆 Athletic Eligibility");
    }

    @FXML
    private void handleSportsManagement() {
        log.info("Navigating to Sports Management...");
        loadView("/fxml/Events.fxml", "⚽ Sports Management");
    }

    @FXML
    private void handleTeamRosters() {
        log.info("Navigating to Team Rosters...");
        loadView("/fxml/Events.fxml", "👥 Team Rosters");
    }

    @FXML
    private void handleGameSchedules() {
        log.info("Navigating to Game Schedules...");
        loadView("/fxml/Events.fxml", "📅 Game Schedules");
    }

    @FXML
    private void handleAthleticEquipment() {
        log.info("Navigating to Athletic Equipment...");
        loadView("/fxml/Events.fxml", "🎽 Athletic Equipment");
    }

    @FXML
    private void handleInjuryTracking() {
        log.info("Navigating to Injury Tracking...");
        loadView("/fxml/HealthOfficeDashboard.fxml", "🩹 Injury Tracking");
    }

    @FXML
    private void handleClubManagement() {
        log.info("Navigating to Club Management...");
        loadView("/fxml/Events.fxml", "🎭 Club Management");
    }

    @FXML
    private void handleClubRosters() {
        log.info("Navigating to Club Rosters...");
        loadView("/fxml/Events.fxml", "📝 Club Rosters");
    }

    @FXML
    private void handleActivityParticipation() {
        log.info("Navigating to Activity Participation...");
        loadView("/fxml/Events.fxml", "📊 Activity Participation");
    }

    @FXML
    private void handleFieldTrips() {
        log.info("Navigating to Field Trip Planning...");
        loadView("/fxml/Events.fxml", "🚌 Field Trips");
    }

    @FXML
    private void handleEventsCalendar() {
        log.info("Navigating to Events & Calendar...");
        loadView("/fxml/Events.fxml", "📅 Events & Calendar");
    }

    @FXML
    private void handleSchoolCalendar() {
        log.info("Navigating to School Calendar...");
        loadView("/fxml/SchoolCalendarManagement.fxml", "📆 School Calendar");
    }

    // ========== COMMUNICATION MENU HANDLERS ==========
    @FXML
    private void handleParentAccounts() {
        log.info("Navigating to Parent Account Management...");
        loadView("/fxml/ParentGuardianManagementForm.fxml", "👨‍👩‍👧‍👦 Parent Accounts");
    }

    @FXML
    private void handleParentPortalSettings() {
        log.info("Navigating to Parent Portal Settings...");
        loadView("/fxml/ParentPortalDashboard.fxml", "⚙️ Parent Portal Settings");
    }

    @FXML
    private void handleStudentAccess() {
        log.info("Navigating to Student Access Management...");
        loadView("/fxml/StudentDashboard.fxml", "🎓 Student Access");
    }

    @FXML
    private void handleStudentPortalSettings() {
        log.info("Navigating to Student Portal Settings...");
        loadView("/fxml/StudentDashboard.fxml", "⚙️ Student Portal Settings");
    }

    @FXML
    private void handleAnnouncements() {
        log.info("Navigating to Announcements...");
        loadView("/fxml/Dashboard.fxml", "📢 Announcements");
    }

    @FXML
    private void handleMassCommunication() {
        log.info("Navigating to Mass Communication...");
        loadView("/fxml/NotificationCenter.fxml", "📨 Mass Communication");
    }

    @FXML
    private void handleMessaging() {
        log.info("Navigating to Messaging Center...");
        loadView("/fxml/NotificationCenter.fxml", "💬 Messaging Center");
    }

    @FXML
    private void handleEmergencyAlerts() {
        log.info("Navigating to Emergency Alerts...");
        loadView("/fxml/NotificationCenter.fxml", "🚨 Emergency Alerts");
    }

    // ========== REPORTS MENU HANDLERS ==========
    @FXML
    private void handleStandardEnrollmentReports() {
        log.info("Navigating to Standard Enrollment Reports...");
        loadView("/fxml/EnrollmentReportsForm.fxml", "📊 Enrollment Reports");
    }

    @FXML
    private void handleStandardAttendanceReports() {
        log.info("Navigating to Standard Attendance Reports...");
        loadView("/fxml/AttendanceReports.fxml", "📈 Attendance Reports");
    }

    @FXML
    private void handleStandardGradeReports() {
        log.info("Navigating to Standard Grade Reports...");
        loadView("/fxml/AssignmentReports.fxml", "📝 Grade Reports");
    }

    @FXML
    private void handleStandardDisciplineReports() {
        log.info("Navigating to Standard Discipline Reports...");
        loadView("/fxml/DisciplineReports.fxml", "⚠️ Discipline Reports");
    }

    @FXML
    private void handleStandardSPEDReports() {
        log.info("Navigating to Standard Special Ed Reports...");
        loadView("/fxml/SPEDDashboard.fxml", "🧩 Special Ed Reports");
    }

    @FXML
    private void handleAnalyticsDashboards() {
        log.info("Navigating to Analytics Dashboards...");
        loadView("/fxml/AdvancedAnalyticsDashboard.fxml", "📊 Analytics Dashboards");
    }

    @FXML
    private void handleCustomReports() {
        log.info("Navigating to Custom Report Builder...");
        loadView("/fxml/ReportGenerationDialog.fxml", "🔧 Custom Reports");
    }

    @FXML
    private void handleStateReporting() {
        log.info("Navigating to State Reporting...");
        loadView("/fxml/ReportsAnalytics.fxml", "🏛️ State Reporting");
    }

    @FXML
    private void handleFederalReporting() {
        log.info("Navigating to Federal Reporting...");
        loadView("/fxml/ReportsAnalytics.fxml", "🦅 Federal Reporting");
    }

    @FXML
    private void handleCRDC() {
        log.info("Navigating to CRDC...");
        loadView("/fxml/ReportsAnalytics.fxml", "⚖️ CRDC");
    }

    @FXML
    private void handleReportScheduling() {
        log.info("Navigating to Report Scheduling...");
        loadView("/fxml/ReportHistoryView.fxml", "📅 Report Scheduling");
    }

    /**
     * Open Report Generation Dialog
     */
    @FXML
    private void handleGenerateAttendanceReport() {
        log.info("Opening Attendance Report Generation Dialog...");
        try {
            reportDialogLauncher.showReportDialog((Stage) mainContainer.getScene().getWindow());
        } catch (Exception e) {
            log.error("Error opening Report Generation Dialog", e);
            showError("Dialog Error", "Failed to open Report Generation Dialog.\n\n" + e.getMessage());
        }
    }

    /**
     * Open Report History View
     */
    @FXML
    private void handleViewReportHistory() {
        log.info("Navigating to Report History...");
        loadView("/fxml/ReportHistoryView.fxml", "📊 Report History");
    }

    // ========== SYSTEM MENU HANDLERS ==========
    @FXML
    private void handleDataImport() {
        log.info("Navigating to Data Import Wizard...");
        loadView("/fxml/ImportWizard.fxml", "📥 Data Import");
    }

    @FXML
    private void handleDataExport() {
        log.info("Navigating to Data Export Wizard...");
        loadView("/fxml/SISExport.fxml", "📤 Data Export");
    }

    @FXML
    private void handleBulkOperations() {
        log.info("Navigating to Bulk Operations...");
        loadView("/fxml/BulkEnrollmentManager.fxml", "⚡ Bulk Operations");
    }

    @FXML
    private void handleDataValidation() {
        log.info("Navigating to Data Validation...");
        loadView("/fxml/StagingReviewDashboard.fxml", "✅ Data Validation");
    }

    @FXML
    private void handleLMSIntegration() {
        log.info("Navigating to LMS Integration...");
        loadView("/fxml/FederationDashboard.fxml", "🔗 LMS Integration");
    }

    @FXML
    private void handleAssessmentIntegration() {
        log.info("Navigating to Assessment Integration...");
        loadView("/fxml/StudentAssessmentDashboard.fxml", "📝 Assessment Integration");
    }

    @FXML
    private void handleCommIntegration() {
        log.info("Navigating to Communication Integration...");
        loadView("/fxml/NotificationCenter.fxml", "💬 Communication Integration");
    }

    @FXML
    private void handlePaymentGateway() {
        log.info("Navigating to Payment Gateway...");
        loadView("/fxml/LunchPeriodManagement.fxml", "💳 Payment Gateway");
    }

    @FXML
    private void handleAPIManagement() {
        log.info("Navigating to API Management...");
        loadView("/fxml/Dashboard.fxml", "🔌 API Management");
    }

    @FXML
    private void handleUserAccounts() {
        log.info("Navigating to User Accounts...");
        loadView("/fxml/UserManagementView.fxml", "👤 User Accounts");
    }

    @FXML
    private void handleRoleManagement() {
        log.info("Navigating to Role Management...");
        loadView("/fxml/UserManagementView.fxml", "🔐 Role Management");
    }

    @FXML
    private void handlePermissions() {
        log.info("Navigating to Permissions...");
        loadView("/fxml/UserManagementView.fxml", "🔒 Permissions");
    }

    @FXML
    private void handleSecuritySettings() {
        log.info("Navigating to Security Settings...");
        loadView("/fxml/SchedulerSettingsView.fxml", "🛡️ Security Settings");
    }

    @FXML
    private void handleSystemConfiguration() {
        log.info("Navigating to System Configuration...");
        loadView("/fxml/SchedulerSettingsView.fxml", "⚙️ System Configuration");
    }

    @FXML
    private void handleAuditLogs() {
        log.info("Navigating to Audit Logs...");
        loadView("/fxml/ReportHistoryView.fxml", "📋 Audit Logs");
    }

    @FXML
    private void handleSystemMonitoring() {
        log.info("Navigating to System Monitoring...");
        loadView("/fxml/Dashboard.fxml", "📊 System Monitoring");
    }

    @FXML
    private void handleDatabaseHealth() {
        log.info("Navigating to Database Health...");
        showInfo("Database Health", "Database health monitoring coming soon!");
    }

    @FXML
    private void handleCheckConflicts() {
        log.info("Checking for schedule conflicts...");
        showInfo("Conflict Detection", "Schedule conflict detection coming soon!");
    }

    // ========== HELP MENU HANDLERS (Additional) ==========
    @FXML
    private void handleUserGuide() {
        log.info("Opening User Guide...");
        showInfo("User Guide", "User guide feature coming soon!");
    }

    @FXML
    private void handleDocumentation() {
        log.info("Opening Documentation...");
        showInfo("Documentation", "Documentation feature coming soon!");
    }

    @FXML
    private void handleVideoTutorials() {
        log.info("Opening Video Tutorials...");
        showInfo("Video Tutorials", "Video tutorials coming soon!");
    }

    @FXML
    private void handleTechnicalSupport() {
        log.info("Opening Technical Support...");
        showInfo("Technical Support", "Technical support feature coming soon!");
    }

    @FXML
    private void handleSubmitFeedback() {
        log.info("Opening Submit Feedback...");
        showInfo("Submit Feedback", "Feedback submission feature coming soon!");
    }

    @FXML
    private void handleSystemInfo() {
        log.info("Opening System Information...");
        showInfo("System Information", "System information feature coming soon!");
    }

    // ========== TOOLBAR HANDLERS ==========
    @FXML
    private void handleDashboard() {
        log.info("Navigating to Dashboard...");
        showInfo("Dashboard", "Dashboard feature coming soon!");
    }
}