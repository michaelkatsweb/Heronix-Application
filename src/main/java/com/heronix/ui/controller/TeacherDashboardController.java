package com.heronix.ui.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class TeacherDashboardController {

    // Header Components
    @FXML private Label teacherNameLabel;
    @FXML private ComboBox<ClassPeriod> periodSelectorComboBox;
    @FXML private Label todaysClassesLabel;
    @FXML private Label totalStudentsLabel;
    @FXML private Label gradesToPostLabel;
    @FXML private Label unreadMessagesLabel;
    @FXML private Label attendanceRateLabel;

    // Today's Schedule
    @FXML private Label currentTimeLabel;
    @FXML private ListView<String> scheduleListView;

    // Action Items
    @FXML private Label actionItemsCountLabel;
    @FXML private ListView<String> actionItemsListView;

    // Current Class Overview
    @FXML private Label classInfoLabel;
    @FXML private TextField studentSearchField;
    @FXML private ComboBox<String> studentFilterComboBox;
    @FXML private TableView<StudentInfo> studentsTableView;
    @FXML private TableColumn<StudentInfo, String> studentNameColumn;
    @FXML private TableColumn<StudentInfo, String> studentIdColumn;
    @FXML private TableColumn<StudentInfo, String> currentGradeColumn;
    @FXML private TableColumn<StudentInfo, String> attendanceColumn;
    @FXML private TableColumn<StudentInfo, String> lastAssignmentColumn;
    @FXML private TableColumn<StudentInfo, String> participationColumn;

    // Class Statistics
    @FXML private Label classAverageLabel;
    @FXML private Label presentTodayLabel;
    @FXML private Label missingWorkLabel;
    @FXML private Label atRiskLabel;
    @FXML private BarChart<String, Number> gradeDistributionChart;

    // Assignments
    @FXML private TableView<AssignmentInfo> assignmentsTableView;
    @FXML private TableColumn<AssignmentInfo, String> assignmentNameColumn;
    @FXML private TableColumn<AssignmentInfo, String> dueDateColumn;
    @FXML private TableColumn<AssignmentInfo, String> submittedColumn;
    @FXML private TableColumn<AssignmentInfo, String> gradedColumn;
    @FXML private TableColumn<AssignmentInfo, String> averageScoreColumn;

    // Lesson Plans
    @FXML private ListView<String> lessonPlansListView;

    // Messages
    @FXML private ListView<String> messagesListView;

    // Footer
    @FXML private Label statusLabel;
    @FXML private Label lastUpdatedLabel;

    // Data
    private ObservableList<ClassPeriod> periods = FXCollections.observableArrayList();
    private ClassPeriod currentPeriod;
    private ObservableList<StudentInfo> allStudents = FXCollections.observableArrayList();
    private ObservableList<StudentInfo> filteredStudents = FXCollections.observableArrayList();
    private ObservableList<AssignmentInfo> assignments = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupPeriodSelector();
        setupStudentsTable();
        setupAssignmentsTable();
        setupFilters();
        setupCharts();
        loadTeacherData();
        loadSampleData();
        startTimeClock();
    }

    private void setupPeriodSelector() {
        periodSelectorComboBox.setConverter(new StringConverter<ClassPeriod>() {
            @Override
            public String toString(ClassPeriod period) {
                return period == null ? "" :
                    period.getPeriodName() + " - " + period.getCourseName() +
                    " (" + period.getTime() + ") - Room " + period.getRoom();
            }

            @Override
            public ClassPeriod fromString(String string) {
                return null;
            }
        });

        periodSelectorComboBox.setOnAction(e -> {
            ClassPeriod selected = periodSelectorComboBox.getSelectionModel().getSelectedItem();
            if (selected != null) {
                currentPeriod = selected;
                loadPeriodData(selected);
            }
        });
    }

    private void setupStudentsTable() {
        studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        studentIdColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        currentGradeColumn.setCellValueFactory(new PropertyValueFactory<>("currentGrade"));
        attendanceColumn.setCellValueFactory(new PropertyValueFactory<>("attendanceStatus"));
        lastAssignmentColumn.setCellValueFactory(new PropertyValueFactory<>("lastAssignment"));
        participationColumn.setCellValueFactory(new PropertyValueFactory<>("participation"));

        // Color code grades
        currentGradeColumn.setCellFactory(column -> new TableCell<StudentInfo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String style = "-fx-font-weight: bold; -fx-alignment: center;";
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

        // Color code attendance
        attendanceColumn.setCellFactory(column -> new TableCell<StudentInfo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String style = "-fx-alignment: center;";
                    if (item.equals("Present")) {
                        style += " -fx-text-fill: #4caf50; -fx-background-color: #e8f5e9;";
                    } else if (item.equals("Absent")) {
                        style += " -fx-text-fill: #f44336; -fx-background-color: #ffebee;";
                    } else if (item.equals("Tardy")) {
                        style += " -fx-text-fill: #ff9800; -fx-background-color: #fff3e0;";
                    }
                    setStyle(style);
                }
            }
        });

        studentsTableView.setItems(filteredStudents);
    }

    private void setupAssignmentsTable() {
        assignmentNameColumn.setCellValueFactory(new PropertyValueFactory<>("assignmentName"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        submittedColumn.setCellValueFactory(new PropertyValueFactory<>("submittedCount"));
        gradedColumn.setCellValueFactory(new PropertyValueFactory<>("gradedCount"));
        averageScoreColumn.setCellValueFactory(new PropertyValueFactory<>("averageScore"));

        // Color code due dates
        dueDateColumn.setCellFactory(column -> new TableCell<AssignmentInfo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String style = "";
                    if (item.contains("Today") || item.contains("Tomorrow")) {
                        style = "-fx-text-fill: #f44336; -fx-font-weight: bold;";
                    }
                    setStyle(style);
                }
            }
        });

        assignmentsTableView.setItems(assignments);
    }

    private void setupFilters() {
        studentFilterComboBox.getSelectionModel().selectFirst();
        studentFilterComboBox.setOnAction(e -> applyStudentFilters());
        studentSearchField.textProperty().addListener((obs, old, newVal) -> applyStudentFilters());
    }

    private void setupCharts() {
        // Grade distribution chart will be populated when period is selected
    }

    private void loadTeacherData() {
        teacherNameLabel.setText("Welcome, Ms. Rodriguez");

        // Load teacher's periods
        periods.addAll(
                new ClassPeriod("Period 1", "Algebra II", "8:00 AM - 8:50 AM", "Room 204", 24),
                new ClassPeriod("Period 2", "Algebra I", "9:00 AM - 9:50 AM", "Room 204", 26),
                new ClassPeriod("Period 3", "Free Period", "10:00 AM - 10:50 AM", "N/A", 0),
                new ClassPeriod("Period 4", "Geometry", "11:00 AM - 11:50 AM", "Room 204", 22),
                new ClassPeriod("Period 5", "Lunch", "12:00 PM - 12:50 PM", "N/A", 0),
                new ClassPeriod("Period 6", "Pre-Calculus", "1:00 PM - 1:50 PM", "Room 204", 28),
                new ClassPeriod("Period 7", "AP Calculus", "2:00 PM - 2:50 PM", "Room 204", 18)
        );

        periodSelectorComboBox.setItems(periods);
        periodSelectorComboBox.getSelectionModel().select(1); // Period 2 - Algebra I

        loadDashboardStats();
        loadTodaysSchedule();
        loadActionItems();
    }

    private void loadDashboardStats() {
        int totalClasses = (int) periods.stream().filter(p -> p.getStudentCount() > 0).count();
        int totalStudents = periods.stream().mapToInt(ClassPeriod::getStudentCount).sum();

        todaysClassesLabel.setText(String.valueOf(totalClasses));
        totalStudentsLabel.setText(String.valueOf(totalStudents));
        gradesToPostLabel.setText("23");
        unreadMessagesLabel.setText("7");
        attendanceRateLabel.setText("94.8%");
    }

    private void loadTodaysSchedule() {
        ObservableList<String> schedule = FXCollections.observableArrayList();
        LocalDateTime now = LocalDateTime.now();
        int currentHour = now.getHour();

        for (ClassPeriod period : periods) {
            String time = period.getTime();
            String status = "";

            // Determine status based on time
            if (period.getPeriodName().equals("Period 2")) {
                status = " ⏺ IN PROGRESS";
            } else if (period.getPeriodName().equals("Period 1")) {
                status = " ✓ Completed";
            } else {
                status = " ○ Upcoming";
            }

            String entry = String.format("%s - %s\n%s%s\nRoom %s • %d students",
                    period.getPeriodName(),
                    period.getCourseName(),
                    time,
                    status,
                    period.getRoom(),
                    period.getStudentCount());

            schedule.add(entry);
        }

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
                    }
                    setStyle(style);
                }
            }
        });
    }

    private void loadActionItems() {
        ObservableList<String> actions = FXCollections.observableArrayList();
        actions.add("⚠ Grade 23 assignments for Period 2 - Algebra I");
        actions.add("📋 Take attendance for Period 6 - Pre-Calculus (not recorded)");
        actions.add("✉ Respond to 3 parent messages");
        actions.add("📝 Submit lesson plans for next week (Due: Friday)");
        actions.add("🎯 Complete IEP progress notes for 2 students");

        actionItemsListView.setItems(actions);
        actionItemsCountLabel.setText(String.valueOf(actions.size()));

        actionItemsListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-padding: 8; -fx-border-color: #ffe0b2; -fx-border-width: 0 0 1 0; " +
                            "-fx-background-color: #fff3e0;");
                }
            }
        });
    }

    private void loadSampleData() {
        if (periods.isEmpty()) return;
        loadPeriodData(periodSelectorComboBox.getSelectionModel().getSelectedItem());
    }

    private void loadPeriodData(ClassPeriod period) {
        if (period == null || period.getStudentCount() == 0) {
            classInfoLabel.setText("No class data available");
            return;
        }

        currentPeriod = period;
        classInfoLabel.setText(period.getCourseName() + " - " + period.getStudentCount() + " students");

        loadStudents(period);
        loadAssignments(period);
        loadLessonPlans(period);
        loadMessages();
        updateClassStatistics();
        updateLastUpdated();

        statusLabel.setText("Viewing " + period.getCourseName() + " (" + period.getPeriodName() + ")");
    }

    private void loadStudents(ClassPeriod period) {
        allStudents.clear();

        Random rand = new Random(period.getPeriodName().hashCode());
        String[] firstNames = {"Emma", "Liam", "Olivia", "Noah", "Ava", "Ethan", "Sophia", "Mason",
                "Isabella", "William", "Mia", "James", "Charlotte", "Benjamin", "Amelia", "Lucas",
                "Harper", "Henry", "Evelyn", "Alexander", "Abigail", "Michael", "Emily", "Daniel"};
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller",
                "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson"};

        String[] grades = {"A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D", "F"};
        String[] attendance = {"Present", "Present", "Present", "Present", "Absent", "Tardy"};
        String[] participation = {"Excellent", "Good", "Fair", "Needs Improvement"};

        for (int i = 0; i < period.getStudentCount(); i++) {
            String firstName = firstNames[rand.nextInt(firstNames.length)];
            String lastName = lastNames[rand.nextInt(lastNames.length)];
            String studentId = String.format("S%05d", 3000 + i);

            String grade = grades[rand.nextInt(grades.length)];
            String attendanceStatus = attendance[rand.nextInt(attendance.length)];
            String lastAssignment = rand.nextDouble() < 0.8 ?
                    String.format("Quiz #%d: %d%%", rand.nextInt(10) + 1, 60 + rand.nextInt(40)) :
                    "Not submitted";
            String participationLevel = participation[rand.nextInt(participation.length)];

            allStudents.add(new StudentInfo(
                    studentId,
                    firstName + " " + lastName,
                    grade,
                    attendanceStatus,
                    lastAssignment,
                    participationLevel
            ));
        }

        applyStudentFilters();
    }

    private void loadAssignments(ClassPeriod period) {
        assignments.clear();

        LocalDate today = LocalDate.now();
        int totalStudents = period.getStudentCount();

        assignments.addAll(
                new AssignmentInfo("Chapter 5 Homework", today, totalStudents, 22, 18, 85.3),
                new AssignmentInfo("Quiz #8", today.plusDays(1), totalStudents, 0, 0, 0.0),
                new AssignmentInfo("Section 5.2 Practice", today.plusDays(3), totalStudents, 0, 0, 0.0),
                new AssignmentInfo("Chapter 5 Test", today.plusDays(7), totalStudents, 0, 0, 0.0),
                new AssignmentInfo("Quiz #7", today.minusDays(3), totalStudents, 24, 24, 82.7),
                new AssignmentInfo("Chapter 4 Test", today.minusDays(7), totalStudents, 26, 26, 78.9)
        );
    }

    private void loadLessonPlans(ClassPeriod period) {
        ObservableList<String> lessons = FXCollections.observableArrayList();
        lessons.add("📖 Monday: Introduction to Quadratic Equations\nObjective: Understand standard form\nActivities: Notes, Examples 1-5");
        lessons.add("📖 Tuesday: Solving by Factoring\nObjective: Factor quadratics\nActivities: Guided practice, Worksheet");
        lessons.add("📖 Wednesday: Quadratic Formula\nObjective: Apply quadratic formula\nActivities: Formula derivation, Practice problems");
        lessons.add("📖 Thursday: Word Problems\nObjective: Model real-world scenarios\nActivities: Group work, Presentations");
        lessons.add("📖 Friday: Chapter 5 Review\nObjective: Review all concepts\nActivities: Jeopardy game, Practice quiz");

        lessonPlansListView.setItems(lessons);
        lessonPlansListView.setCellFactory(lv -> new ListCell<String>() {
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

    private void loadMessages() {
        ObservableList<String> messages = FXCollections.observableArrayList();
        messages.add("📧 From: Sarah Anderson (Parent)\nRe: Emma's progress - Can we schedule a meeting?\n2 hours ago");
        messages.add("📧 From: Principal Martinez\nRe: Department meeting - Wednesday 3:00 PM\n5 hours ago");
        messages.add("📧 From: Coach Williams\nRe: Student athlete update - James will miss Friday\nYesterday");

        messagesListView.setItems(messages);
        messagesListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String style = "-fx-padding: 10; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;";
                    if (item.contains("2 hours ago")) {
                        style += " -fx-background-color: #e3f2fd;";
                    }
                    setStyle(style);
                }
            }
        });
    }

    private void updateClassStatistics() {
        if (currentPeriod == null) return;

        // Calculate class average
        double average = allStudents.stream()
                .mapToDouble(s -> getGradeValue(s.getCurrentGrade()))
                .average()
                .orElse(0.0);
        classAverageLabel.setText(String.format("%.1f%%", average));

        // Count present/absent
        long present = allStudents.stream().filter(s -> s.getAttendanceStatus().equals("Present")).count();
        presentTodayLabel.setText(present + " / " + allStudents.size());

        // Count missing work
        long missing = allStudents.stream()
                .filter(s -> s.getLastAssignment().contains("Not submitted"))
                .count();
        missingWorkLabel.setText(String.valueOf(missing));

        // Count at-risk students
        long atRisk = allStudents.stream()
                .filter(s -> s.getCurrentGrade().startsWith("D") || s.getCurrentGrade().startsWith("F"))
                .count();
        atRiskLabel.setText(String.valueOf(atRisk));

        // Update grade distribution chart
        updateGradeDistributionChart();
    }

    private void updateGradeDistributionChart() {
        Map<String, Long> distribution = allStudents.stream()
                .collect(Collectors.groupingBy(
                        s -> String.valueOf(s.getCurrentGrade().charAt(0)),
                        Collectors.counting()
                ));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("A", distribution.getOrDefault("A", 0L)));
        series.getData().add(new XYChart.Data<>("B", distribution.getOrDefault("B", 0L)));
        series.getData().add(new XYChart.Data<>("C", distribution.getOrDefault("C", 0L)));
        series.getData().add(new XYChart.Data<>("D", distribution.getOrDefault("D", 0L)));
        series.getData().add(new XYChart.Data<>("F", distribution.getOrDefault("F", 0L)));

        gradeDistributionChart.getData().clear();
        gradeDistributionChart.getData().add(series);
    }

    private double getGradeValue(String grade) {
        char letter = grade.charAt(0);
        double base = switch (letter) {
            case 'A' -> 93.0;
            case 'B' -> 85.0;
            case 'C' -> 75.0;
            case 'D' -> 65.0;
            case 'F' -> 50.0;
            default -> 0.0;
        };

        if (grade.contains("+")) base += 3;
        if (grade.contains("-")) base -= 3;

        return base;
    }

    private void applyStudentFilters() {
        String searchText = studentSearchField.getText().toLowerCase();
        String filter = studentFilterComboBox.getSelectionModel().getSelectedItem();

        filteredStudents.clear();
        filteredStudents.addAll(allStudents.stream()
                .filter(s -> {
                    // Search filter
                    if (!searchText.isEmpty() &&
                            !s.getName().toLowerCase().contains(searchText) &&
                            !s.getStudentId().toLowerCase().contains(searchText)) {
                        return false;
                    }

                    // Status filter
                    if (filter != null) {
                        switch (filter) {
                            case "Present Today":
                                if (!s.getAttendanceStatus().equals("Present")) return false;
                                break;
                            case "Absent Today":
                                if (!s.getAttendanceStatus().equals("Absent")) return false;
                                break;
                            case "Struggling (D/F)":
                                char grade = s.getCurrentGrade().charAt(0);
                                if (grade != 'D' && grade != 'F') return false;
                                break;
                            case "Excelling (A)":
                                if (!s.getCurrentGrade().startsWith("A")) return false;
                                break;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList())
        );
    }

    private void startTimeClock() {
        Thread clockThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    Platform.runLater(() -> {
                        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a"));
                        currentTimeLabel.setText(time);
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
    private void handleTakeAttendance() {
        if (currentPeriod != null) {
            statusLabel.setText("Opening attendance for " + currentPeriod.getCourseName());
            showAlert("Take Attendance", "Attendance interface will open for " + currentPeriod.getCourseName());
        } else {
            showAlert("No Class Selected", "Please select a class period first");
        }
    }

    @FXML
    private void handleEnterGrades() {
        if (currentPeriod != null) {
            statusLabel.setText("Opening grade book for " + currentPeriod.getCourseName());
            showAlert("Enter Grades", "Grade entry interface will open for " + currentPeriod.getCourseName());
        } else {
            showAlert("No Class Selected", "Please select a class period first");
        }
    }

    @FXML
    private void handleViewRoster() {
        if (currentPeriod != null) {
            statusLabel.setText("Opening full roster for " + currentPeriod.getCourseName());
            showAlert("Class Roster", "Full roster with student details will open");
        }
    }

    @FXML
    private void handleCreateAssignment() {
        if (currentPeriod != null) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Create Assignment");
            dialog.setHeaderText("Create new assignment for " + currentPeriod.getCourseName());
            dialog.setContentText("Assignment name:");

            dialog.showAndWait().ifPresent(name -> {
                statusLabel.setText("Created assignment: " + name);
                showAlert("Assignment Created", "Assignment '" + name + "' has been created");
            });
        }
    }

    @FXML
    private void handleNewLesson() {
        if (currentPeriod != null) {
            statusLabel.setText("Opening lesson plan builder for " + currentPeriod.getCourseName());
            showAlert("New Lesson Plan", "Lesson plan builder will open");
        }
    }

    @FXML
    private void handleComposeMessage() {
        statusLabel.setText("Opening message composer");
        showAlert("Compose Message", "Message composer will open");
    }

    @FXML
    private void handleGradeReports() {
        statusLabel.setText("Generating grade reports...");
        showAlert("Grade Reports", "Generate progress reports, deficiency reports, and grade summaries");
    }

    @FXML
    private void handleCalendar() {
        statusLabel.setText("Opening calendar...");
        showAlert("Calendar", "View school calendar, plan lessons, and schedule events");
    }

    @FXML
    private void handleForms() {
        statusLabel.setText("Opening forms library...");
        showAlert("Forms & Templates", "Access permission slips, worksheets, rubrics, and templates");
    }

    @FXML
    private void handleCurriculum() {
        statusLabel.setText("Opening curriculum guides...");
        showAlert("Curriculum", "View curriculum maps, pacing guides, and standards");
    }

    @FXML
    private void handleStandards() {
        statusLabel.setText("Opening standards alignment...");
        showAlert("Standards", "Align lessons with state and national standards");
    }

    @FXML
    private void handleResourcesLibrary() {
        statusLabel.setText("Opening resources library...");
        showAlert("Resources Library", "Access videos, worksheets, presentations, and teaching materials");
    }

    @FXML
    private void handleRefresh() {
        if (currentPeriod != null) {
            loadPeriodData(currentPeriod);
            statusLabel.setText("Dashboard refreshed");
        }
    }

    @FXML
    private void handleSettings() {
        statusLabel.setText("Opening settings...");
        showAlert("Settings", "Configure dashboard preferences, notifications, and integrations");
    }

    @FXML
    private void handleHelp() {
        showAlert("Teacher Dashboard Help",
                "Teacher Dashboard Features:\n\n" +
                        "• View all your classes and schedules\n" +
                        "• Take attendance and enter grades\n" +
                        "• Monitor student progress and performance\n" +
                        "• Create assignments and lesson plans\n" +
                        "• Communicate with students and parents\n" +
                        "• Access curriculum and resources\n\n" +
                        "For support: techsupport@heronix.edu");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Data Classes

    public static class ClassPeriod {
        private String periodName;
        private String courseName;
        private String time;
        private String room;
        private int studentCount;

        public ClassPeriod(String periodName, String courseName, String time, String room, int studentCount) {
            this.periodName = periodName;
            this.courseName = courseName;
            this.time = time;
            this.room = room;
            this.studentCount = studentCount;
        }

        public String getPeriodName() { return periodName; }
        public String getCourseName() { return courseName; }
        public String getTime() { return time; }
        public String getRoom() { return room; }
        public int getStudentCount() { return studentCount; }
    }

    public static class StudentInfo {
        private String studentId;
        private String name;
        private String currentGrade;
        private String attendanceStatus;
        private String lastAssignment;
        private String participation;

        public StudentInfo(String studentId, String name, String currentGrade, String attendanceStatus,
                           String lastAssignment, String participation) {
            this.studentId = studentId;
            this.name = name;
            this.currentGrade = currentGrade;
            this.attendanceStatus = attendanceStatus;
            this.lastAssignment = lastAssignment;
            this.participation = participation;
        }

        public String getStudentId() { return studentId; }
        public String getName() { return name; }
        public String getCurrentGrade() { return currentGrade; }
        public String getAttendanceStatus() { return attendanceStatus; }
        public String getLastAssignment() { return lastAssignment; }
        public String getParticipation() { return participation; }
    }

    public static class AssignmentInfo {
        private String assignmentName;
        private String dueDate;
        private String submittedCount;
        private String gradedCount;
        private String averageScore;

        public AssignmentInfo(String assignmentName, LocalDate dueDate, int totalStudents,
                              int submitted, int graded, double avgScore) {
            this.assignmentName = assignmentName;

            LocalDate today = LocalDate.now();
            if (dueDate.equals(today)) {
                this.dueDate = "Today";
            } else if (dueDate.equals(today.plusDays(1))) {
                this.dueDate = "Tomorrow";
            } else {
                this.dueDate = dueDate.format(DateTimeFormatter.ofPattern("MMM dd"));
            }

            this.submittedCount = submitted + " / " + totalStudents;
            this.gradedCount = graded + " / " + totalStudents;
            this.averageScore = avgScore > 0 ? String.format("%.1f%%", avgScore) : "—";
        }

        public String getAssignmentName() { return assignmentName; }
        public String getDueDate() { return dueDate; }
        public String getSubmittedCount() { return submittedCount; }
        public String getGradedCount() { return gradedCount; }
        public String getAverageScore() { return averageScore; }
    }
}
