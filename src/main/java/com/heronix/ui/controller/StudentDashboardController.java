package com.heronix.ui.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class StudentDashboardController {

    // Header Components
    @FXML private Label studentNameLabel;
    @FXML private Label studentInfoLabel;
    @FXML private Label currentTimeLabel;
    @FXML private Label currentDateLabel;
    @FXML private Label currentGPALabel;
    @FXML private Label attendanceRateLabel;
    @FXML private Label creditsEarnedLabel;
    @FXML private Label assignmentsDueLabel;
    @FXML private Label missingWorkLabel;

    // Today's Schedule
    @FXML private Label scheduleStatusLabel;
    @FXML private ListView<String> scheduleListView;

    // Next Class Info
    @FXML private Label nextClassNameLabel;
    @FXML private Label nextClassInfoLabel;
    @FXML private Label nextClassTimeLabel;
    @FXML private Label classRankLabel;
    @FXML private Label tardyCountLabel;
    @FXML private Label clubsLabel;

    // Current Grades
    @FXML private TableView<CourseGrade> gradesTableView;
    @FXML private TableColumn<CourseGrade, String> courseNameColumn;
    @FXML private TableColumn<CourseGrade, String> teacherColumn;
    @FXML private TableColumn<CourseGrade, String> periodColumn;
    @FXML private TableColumn<CourseGrade, String> currentGradeColumn;
    @FXML private TableColumn<CourseGrade, String> percentageColumn;
    @FXML private TableColumn<CourseGrade, String> creditsColumn;

    // Progress Charts
    @FXML private LineChart<String, Number> gpaProgressChart;
    @FXML private ProgressBar creditProgressBar;
    @FXML private Label creditProgressLabel;

    // Assignments
    @FXML private ComboBox<String> assignmentFilterComboBox;
    @FXML private TableView<Assignment> assignmentsTableView;
    @FXML private TableColumn<Assignment, String> assignmentCourseColumn;
    @FXML private TableColumn<Assignment, String> assignmentNameColumn;
    @FXML private TableColumn<Assignment, String> dueDateColumn;
    @FXML private TableColumn<Assignment, String> assignmentStatusColumn;
    @FXML private TableColumn<Assignment, String> scoreColumn;

    // Events & Announcements
    @FXML private Label eventsCountLabel;
    @FXML private ListView<String> eventsListView;
    @FXML private ListView<String> announcementsListView;
    @FXML private ListView<String> achievementsListView;

    // Footer
    @FXML private Label statusLabel;
    @FXML private Label lastUpdatedLabel;

    // Data
    private ObservableList<CourseGrade> courseGrades = FXCollections.observableArrayList();
    private ObservableList<Assignment> allAssignments = FXCollections.observableArrayList();
    private ObservableList<Assignment> filteredAssignments = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupGradesTable();
        setupAssignmentsTable();
        setupCharts();
        loadStudentData();
        startClocks();
    }

    private void setupGradesTable() {
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        teacherColumn.setCellValueFactory(new PropertyValueFactory<>("teacher"));
        periodColumn.setCellValueFactory(new PropertyValueFactory<>("period"));
        currentGradeColumn.setCellValueFactory(new PropertyValueFactory<>("letterGrade"));
        percentageColumn.setCellValueFactory(new PropertyValueFactory<>("percentage"));
        creditsColumn.setCellValueFactory(new PropertyValueFactory<>("credits"));

        // Color code grades
        currentGradeColumn.setCellFactory(column -> new TableCell<CourseGrade, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String style = "-fx-font-weight: bold; -fx-alignment: center; -fx-font-size: 14px;";
                    char grade = item.charAt(0);
                    switch (grade) {
                        case 'A': style += " -fx-text-fill: #4caf50;"; break;
                        case 'B': style += " -fx-text-fill: #8bc34a;"; break;
                        case 'C': style += " -fx-text-fill: #ff9800;"; break;
                        case 'D': style += " -fx-text-fill: #ff5722;"; break;
                        case 'F': style += " -fx-text-fill: #f44336;"; break;
                    }
                    setStyle(style);
                }
            }
        });

        gradesTableView.setItems(courseGrades);
    }

    private void setupAssignmentsTable() {
        assignmentCourseColumn.setCellValueFactory(new PropertyValueFactory<>("course"));
        assignmentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        assignmentStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));

        // Color code due dates
        dueDateColumn.setCellFactory(column -> new TableCell<Assignment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String style = "";
                    if (item.equals("Today") || item.equals("Tomorrow")) {
                        style = "-fx-text-fill: #f44336; -fx-font-weight: bold;";
                    }
                    setStyle(style);
                }
            }
        });

        // Color code status
        assignmentStatusColumn.setCellFactory(column -> new TableCell<Assignment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String style = "-fx-font-weight: bold; -fx-alignment: center;";
                    switch (item) {
                        case "Submitted":
                            style += " -fx-text-fill: #2196f3; -fx-background-color: #e3f2fd;";
                            break;
                        case "Graded":
                            style += " -fx-text-fill: #4caf50; -fx-background-color: #e8f5e9;";
                            break;
                        case "Missing":
                            style += " -fx-text-fill: #f44336; -fx-background-color: #ffebee;";
                            break;
                        case "Due Soon":
                            style += " -fx-text-fill: #ff9800; -fx-background-color: #fff3e0;";
                            break;
                        case "Not Started":
                            style += " -fx-text-fill: #757575; -fx-background-color: #f5f5f5;";
                            break;
                    }
                    setStyle(style);
                }
            }
        });

        assignmentsTableView.setItems(filteredAssignments);
        assignmentFilterComboBox.getSelectionModel().selectFirst();
        assignmentFilterComboBox.setOnAction(e -> filterAssignments());
    }

    private void setupCharts() {
        // GPA Progress Chart
        XYChart.Series<String, Number> gpaSeries = new XYChart.Series<>();
        gpaSeries.getData().add(new XYChart.Data<>("Q1", 3.58));
        gpaSeries.getData().add(new XYChart.Data<>("Q2", 3.65));
        gpaSeries.getData().add(new XYChart.Data<>("Q3", 3.72));
        gpaProgressChart.getData().add(gpaSeries);
    }

    private void loadStudentData() {
        // Student Info
        studentNameLabel.setText("Welcome back, Emma Anderson");
        studentInfoLabel.setText("Grade 10 • Student ID: S002045");

        // Quick Stats
        currentGPALabel.setText("3.72");
        attendanceRateLabel.setText("97.2%");
        creditsEarnedLabel.setText("18 / 24");
        assignmentsDueLabel.setText("3");
        missingWorkLabel.setText("0");

        // Additional Stats
        classRankLabel.setText("23 / 350");
        tardyCountLabel.setText("2");
        clubsLabel.setText("3");

        // Credit Progress
        creditProgressBar.setProgress(0.75);
        creditProgressLabel.setText("18 of 24 credits earned (75%)");

        loadSchedule();
        loadCourseGrades();
        loadAssignments();
        loadEvents();
        loadAnnouncements();
        loadAchievements();
        updateLastUpdated();
    }

    private void loadSchedule() {
        ObservableList<String> schedule = FXCollections.observableArrayList();

        String[] periods = {
                "Period 1 • 8:00 - 8:50 AM\nEnglish III - Ms. Martinez\nRoom 201 ✓ Completed",
                "Period 2 • 9:00 - 9:50 AM\nAlgebra II - Mr. Thompson\nRoom 204 ✓ Completed",
                "Period 3 • 10:00 - 10:50 AM\nChemistry - Ms. Johnson\nRoom 108 ⏺ IN PROGRESS",
                "Period 4 • 11:00 - 11:50 AM\nUS History - Mr. Davis\nRoom 305 ○ Next",
                "Lunch • 12:00 - 12:50 PM\nCafeteria",
                "Period 5 • 1:00 - 1:50 PM\nSpanish II - Sra. Garcia\nRoom 112 ○ Upcoming",
                "Period 6 • 2:00 - 2:50 PM\nPhysical Education - Coach Williams\nGym ○ Upcoming"
        };

        schedule.addAll(Arrays.asList(periods));
        scheduleListView.setItems(schedule);

        scheduleListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String style = "-fx-padding: 10; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;";
                    if (item.contains("IN PROGRESS")) {
                        style += " -fx-background-color: #e3f2fd; -fx-border-left-color: #2196f3; -fx-border-left-width: 4;";
                    } else if (item.contains("Next")) {
                        style += " -fx-background-color: #fff3e0; -fx-border-left-color: #ff9800; -fx-border-left-width: 4;";
                    }
                    setStyle(style);
                }
            }
        });

        scheduleStatusLabel.setText("Period 3 in progress");

        // Next Class Info
        nextClassNameLabel.setText("US History");
        nextClassInfoLabel.setText("Room 305 • Mr. Davis");
        nextClassTimeLabel.setText("Starts in 15 minutes (11:00 AM)");
    }

    private void loadCourseGrades() {
        courseGrades.clear();
        courseGrades.addAll(
                new CourseGrade("English III", "Ms. Martinez", "1", "A-", "92.3%", "4"),
                new CourseGrade("Algebra II", "Mr. Thompson", "2", "B+", "88.5%", "4"),
                new CourseGrade("Chemistry", "Ms. Johnson", "3", "B", "85.2%", "4"),
                new CourseGrade("US History", "Mr. Davis", "4", "A", "94.1%", "3"),
                new CourseGrade("Spanish II", "Sra. Garcia", "5", "A", "95.7%", "3"),
                new CourseGrade("Physical Education", "Coach Williams", "6", "A", "98.0%", "1")
        );
    }

    private void loadAssignments() {
        allAssignments.clear();

        LocalDate today = LocalDate.now();

        allAssignments.addAll(
                new Assignment("English III", "Essay #3: Character Analysis", today, "Graded", "92%"),
                new Assignment("Algebra II", "Chapter 5 Homework", today, "Due Soon", "—"),
                new Assignment("Chemistry", "Lab Report #5", today.plusDays(1), "Not Started", "—"),
                new Assignment("US History", "Chapter 8 Review", today.plusDays(2), "Not Started", "—"),
                new Assignment("Spanish II", "Vocabulary Quiz", today.plusDays(5), "Not Started", "—"),
                new Assignment("English III", "Reading Quiz", today.minusDays(2), "Graded", "88%"),
                new Assignment("Algebra II", "Quiz #7", today.minusDays(5), "Graded", "91%"),
                new Assignment("Chemistry", "Quiz #8", today.minusDays(3), "Graded", "85%")
        );

        filterAssignments();
    }

    private void filterAssignments() {
        String filter = assignmentFilterComboBox.getSelectionModel().getSelectedItem();
        if (filter == null) filter = "All Assignments";

        filteredAssignments.clear();

        switch (filter) {
            case "Due This Week":
                filteredAssignments.addAll(allAssignments.stream()
                        .filter(a -> a.status.equals("Due Soon") || a.status.equals("Not Started"))
                        .toList());
                break;
            case "Due Today":
                filteredAssignments.addAll(allAssignments.stream()
                        .filter(a -> a.dueDate.equals("Today"))
                        .toList());
                break;
            case "Missing":
                filteredAssignments.addAll(allAssignments.stream()
                        .filter(a -> a.status.equals("Missing"))
                        .toList());
                break;
            case "Completed":
                filteredAssignments.addAll(allAssignments.stream()
                        .filter(a -> a.status.equals("Graded") || a.status.equals("Submitted"))
                        .toList());
                break;
            default: // All Assignments
                filteredAssignments.addAll(allAssignments);
                break;
        }

        statusLabel.setText("Showing " + filteredAssignments.size() + " assignments");
    }

    private void loadEvents() {
        ObservableList<String> events = FXCollections.observableArrayList();
        events.add("📅 Dec 10 (Tue) - Parent-Teacher Conference\n3:00 PM - 6:00 PM");
        events.add("🎭 Dec 12 (Thu) - Drama Club Rehearsal\n3:30 PM - 5:00 PM • Auditorium");
        events.add("⚽ Dec 13 (Fri) - Soccer Practice\n4:00 PM - 6:00 PM • Field");
        events.add("📚 Dec 15 (Sun) - Library Book Due\nReturn 'The Great Gatsby'");
        events.add("🎄 Dec 18 (Mon) - Winter Concert\n7:00 PM • Main Hall");

        eventsListView.setItems(events);
        eventsCountLabel.setText(events.size() + " events");

        eventsListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-padding: 10; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
                }
            }
        });
    }

    private void loadAnnouncements() {
        ObservableList<String> announcements = FXCollections.observableArrayList();
        announcements.add("🔔 Friday, Dec 15 is the last day before Winter Break!");
        announcements.add("📢 Yearbook photos will be retaken next Monday, Dec 9");
        announcements.add("🎓 National Honor Society applications due Friday");
        announcements.add("🍕 Pizza lunch special tomorrow - $3.50");

        announcementsListView.setItems(announcements);

        announcementsListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-padding: 10; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0; " +
                            "-fx-background-color: #fffde7;");
                }
            }
        });
    }

    private void loadAchievements() {
        ObservableList<String> achievements = FXCollections.observableArrayList();
        achievements.add("🏆 Honor Roll - Quarter 3 (GPA 3.5+)");
        achievements.add("📚 Perfect Attendance - November 2024");
        achievements.add("⭐ Student of the Month - English III (October)");
        achievements.add("🥇 1st Place - Science Fair (Chemistry Project)");

        achievementsListView.setItems(achievements);

        achievementsListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-padding: 10; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0; " +
                            "-fx-background-color: #f3e5f5;");
                }
            }
        });
    }

    private void startClocks() {
        Thread clockThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    Platform.runLater(() -> {
                        LocalDateTime now = LocalDateTime.now();
                        currentTimeLabel.setText(now.format(DateTimeFormatter.ofPattern("h:mm a")));
                        currentDateLabel.setText(now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
                    });
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        clockThread.setDaemon(true);
        clockThread.start();
    }

    private void updateLastUpdated() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd 'at' h:mm a"));
        lastUpdatedLabel.setText("Last updated: " + timestamp);
    }

    // Event Handlers

    @FXML
    private void handleViewReportCard() {
        statusLabel.setText("Opening report card...");
        showAlert("Report Card", "Your current report card will open");
    }

    @FXML
    private void handleLibraryAccount() {
        statusLabel.setText("Opening library account...");
        showAlert("Library Account",
                "Books Checked Out: 2\n" +
                        "• The Great Gatsby (Due: Dec 15)\n" +
                        "• To Kill a Mockingbird (Due: Dec 22)\n\n" +
                        "No overdue items • No fines");
    }

    @FXML
    private void handleMealBalance() {
        statusLabel.setText("Opening meal account...");
        showAlert("Meal Balance",
                "Current Balance: $42.50\n\n" +
                        "Recent Transactions:\n" +
                        "• Dec 6: Lunch - $3.25\n" +
                        "• Dec 5: Lunch - $3.25\n" +
                        "• Dec 4: Lunch - $3.25");
    }

    @FXML
    private void handleBusSchedule() {
        statusLabel.setText("Opening bus schedule...");
        showAlert("Bus Schedule",
                "Your Bus: Route 42\n\n" +
                        "Morning Pickup:\n" +
                        "• Stop: Oak & Main St\n" +
                        "• Time: 7:15 AM\n\n" +
                        "Afternoon Drop-off:\n" +
                        "• Stop: Oak & Main St\n" +
                        "• Time: 3:30 PM");
    }

    @FXML
    private void handleHealthRecords() {
        statusLabel.setText("Opening health records...");
        showAlert("Health Records",
                "Immunizations: Up to date ✓\n" +
                        "Allergies: None on file\n" +
                        "Emergency Contact: Sarah Anderson (555) 123-4567");
    }

    @FXML
    private void handleMessages() {
        statusLabel.setText("Opening messages...");
        showAlert("Messages", "You have 2 new messages from teachers");
    }

    @FXML
    private void handleTranscripts() {
        statusLabel.setText("Opening transcripts...");
        showAlert("Transcripts",
                "Academic Transcript\n\n" +
                        "Grade 9: GPA 3.58 (18 credits)\n" +
                        "Grade 10 (Current): GPA 3.72 (In Progress)\n\n" +
                        "Cumulative GPA: 3.65");
    }

    @FXML
    private void handleClubsActivities() {
        statusLabel.setText("Opening clubs & activities...");
        showAlert("Clubs & Activities",
                "Your Activities:\n\n" +
                        "• Drama Club (Member)\n" +
                        "• Soccer Team (JV)\n" +
                        "• National Honor Society (Candidate)\n\n" +
                        "Total Service Hours: 24");
    }

    @FXML
    private void handleFeesPayments() {
        statusLabel.setText("Opening fees & payments...");
        showAlert("Fees & Payments",
                "Outstanding Fees:\n\n" +
                        "• Yearbook: $45.00\n" +
                        "• Drama Club Dues: $25.00\n\n" +
                        "Total: $70.00");
    }

    @FXML
    private void handleRefresh() {
        loadStudentData();
        statusLabel.setText("Dashboard refreshed");
    }

    @FXML
    private void handleHelp() {
        showAlert("Student Dashboard Help",
                "Student Dashboard Features:\n\n" +
                        "• View your daily schedule and next class\n" +
                        "• Track grades and GPA progress\n" +
                        "• Manage assignments and due dates\n" +
                        "• Access school resources and activities\n" +
                        "• View announcements and events\n\n" +
                        "Need help? Contact: studenthelp@heronix.edu");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Data Classes

    public static class CourseGrade {
        private String courseName;
        private String teacher;
        private String period;
        private String letterGrade;
        private String percentage;
        private String credits;

        public CourseGrade(String courseName, String teacher, String period, String letterGrade,
                           String percentage, String credits) {
            this.courseName = courseName;
            this.teacher = teacher;
            this.period = period;
            this.letterGrade = letterGrade;
            this.percentage = percentage;
            this.credits = credits;
        }

        public String getCourseName() { return courseName; }
        public String getTeacher() { return teacher; }
        public String getPeriod() { return period; }
        public String getLetterGrade() { return letterGrade; }
        public String getPercentage() { return percentage; }
        public String getCredits() { return credits; }
    }

    public static class Assignment {
        private String course;
        private String name;
        private String dueDate;
        private String status;
        private String score;

        public Assignment(String course, String name, LocalDate dueDate, String status, String score) {
            this.course = course;
            this.name = name;
            this.status = status;
            this.score = score;

            LocalDate today = LocalDate.now();
            if (dueDate.equals(today)) {
                this.dueDate = "Today";
            } else if (dueDate.equals(today.plusDays(1))) {
                this.dueDate = "Tomorrow";
            } else if (dueDate.equals(today.minusDays(1))) {
                this.dueDate = "Yesterday";
            } else {
                this.dueDate = dueDate.format(DateTimeFormatter.ofPattern("MMM dd"));
            }
        }

        public String getCourse() { return course; }
        public String getName() { return name; }
        public String getDueDate() { return dueDate; }
        public String getStatus() { return status; }
        public String getScore() { return score; }
    }
}
