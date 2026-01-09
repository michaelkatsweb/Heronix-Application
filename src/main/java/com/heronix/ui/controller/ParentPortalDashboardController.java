package com.heronix.ui.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class ParentPortalDashboardController {

    // Header Components
    @FXML private ComboBox<StudentInfo> studentSelectorComboBox;
    @FXML private Label parentNameLabel;
    @FXML private Label currentGPALabel;
    @FXML private Label attendanceRateLabel;
    @FXML private Label missingAssignmentsLabel;
    @FXML private Label unreadMessagesLabel;
    @FXML private Label upcomingEventsLabel;

    // Alerts & Updates
    @FXML private Label alertCountLabel;
    @FXML private ListView<String> alertsListView;
    @FXML private ListView<String> updatesListView;

    // Current Grades
    @FXML private TableView<CourseGradeInfo> gradesTableView;
    @FXML private TableColumn<CourseGradeInfo, String> courseNameColumn;
    @FXML private TableColumn<CourseGradeInfo, String> teacherColumn;
    @FXML private TableColumn<CourseGradeInfo, String> currentGradeColumn;
    @FXML private TableColumn<CourseGradeInfo, String> percentageColumn;
    @FXML private TableColumn<CourseGradeInfo, String> trendColumn;

    // GPA Trend
    @FXML private LineChart<String, Number> gpaTrendChart;

    // Assignments
    @FXML private ComboBox<String> assignmentFilterComboBox;
    @FXML private TableView<AssignmentInfo> assignmentsTableView;
    @FXML private TableColumn<AssignmentInfo, String> assignmentCourseColumn;
    @FXML private TableColumn<AssignmentInfo, String> assignmentNameColumn;
    @FXML private TableColumn<AssignmentInfo, String> dueDateColumn;
    @FXML private TableColumn<AssignmentInfo, String> assignmentStatusColumn;

    // Attendance
    @FXML private Label daysPresentLabel;
    @FXML private Label daysAbsentLabel;
    @FXML private Label tardiesLabel;
    @FXML private Label earlyDismissalsLabel;
    @FXML private PieChart attendancePieChart;

    // Communication & Calendar
    @FXML private ListView<String> messagesListView;
    @FXML private ListView<String> calendarEventsListView;

    // Footer
    @FXML private Label statusLabel;
    @FXML private Label lastUpdatedLabel;

    // Data
    private ObservableList<StudentInfo> students = FXCollections.observableArrayList();
    private StudentInfo currentStudent;
    private ObservableList<CourseGradeInfo> courseGrades = FXCollections.observableArrayList();
    private ObservableList<AssignmentInfo> upcomingAssignments = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupStudentSelector();
        setupGradesTable();
        setupAssignmentsTable();
        setupCharts();
        loadParentData();
        loadSampleData();
    }

    private void setupStudentSelector() {
        studentSelectorComboBox.setConverter(new StringConverter<StudentInfo>() {
            @Override
            public String toString(StudentInfo student) {
                return student == null ? "" : student.getName() + " (Grade " + student.getGradeLevel() + ")";
            }

            @Override
            public StudentInfo fromString(String string) {
                return null;
            }
        });

        studentSelectorComboBox.setOnAction(e -> {
            StudentInfo selected = studentSelectorComboBox.getSelectionModel().getSelectedItem();
            if (selected != null) {
                currentStudent = selected;
                loadStudentData(selected);
            }
        });
    }

    private void setupGradesTable() {
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        teacherColumn.setCellValueFactory(new PropertyValueFactory<>("teacher"));
        currentGradeColumn.setCellValueFactory(new PropertyValueFactory<>("letterGrade"));
        percentageColumn.setCellValueFactory(new PropertyValueFactory<>("percentage"));
        trendColumn.setCellValueFactory(new PropertyValueFactory<>("trend"));

        // Color code grades
        currentGradeColumn.setCellFactory(column -> new TableCell<CourseGradeInfo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String style = "-fx-font-weight: bold; -fx-alignment: center;";
                    switch (item.substring(0, 1)) {
                        case "A": style += " -fx-text-fill: #4caf50;"; break;
                        case "B": style += " -fx-text-fill: #8bc34a;"; break;
                        case "C": style += " -fx-text-fill: #ff9800;"; break;
                        case "D": style += " -fx-text-fill: #ff5722;"; break;
                        case "F": style += " -fx-text-fill: #f44336;"; break;
                    }
                    setStyle(style);
                }
            }
        });

        // Color code trends
        trendColumn.setCellFactory(column -> new TableCell<CourseGradeInfo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String style = "-fx-font-weight: bold; -fx-alignment: center;";
                    if (item.contains("↑")) {
                        style += " -fx-text-fill: #4caf50;";
                    } else if (item.contains("↓")) {
                        style += " -fx-text-fill: #f44336;";
                    } else {
                        style += " -fx-text-fill: #757575;";
                    }
                    setStyle(style);
                }
            }
        });

        gradesTableView.setItems(courseGrades);
    }

    private void setupAssignmentsTable() {
        assignmentCourseColumn.setCellValueFactory(new PropertyValueFactory<>("course"));
        assignmentNameColumn.setCellValueFactory(new PropertyValueFactory<>("assignmentName"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        assignmentStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

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
                    // Check if due soon
                    String style = "";
                    if (item.contains("Today") || item.contains("Tomorrow")) {
                        style = "-fx-text-fill: #f44336; -fx-font-weight: bold;";
                    }
                    setStyle(style);
                }
            }
        });

        // Color code status
        assignmentStatusColumn.setCellFactory(column -> new TableCell<AssignmentInfo, String>() {
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
                            style += " -fx-text-fill: #4caf50; -fx-background-color: #e8f5e9;";
                            break;
                        case "Missing":
                            style += " -fx-text-fill: #f44336; -fx-background-color: #ffebee;";
                            break;
                        case "Pending":
                            style += " -fx-text-fill: #ff9800; -fx-background-color: #fff3e0;";
                            break;
                        case "Graded":
                            style += " -fx-text-fill: #2196f3; -fx-background-color: #e3f2fd;";
                            break;
                    }
                    setStyle(style);
                }
            }
        });

        assignmentsTableView.setItems(upcomingAssignments);
        assignmentFilterComboBox.getSelectionModel().selectFirst();
        assignmentFilterComboBox.setOnAction(e -> filterAssignments());
    }

    private void setupCharts() {
        // GPA Trend Chart
        XYChart.Series<String, Number> gpaSeries = new XYChart.Series<>();
        gpaSeries.setName("GPA");
        gpaSeries.getData().add(new XYChart.Data<>("Q1", 3.65));
        gpaSeries.getData().add(new XYChart.Data<>("Q2", 3.72));
        gpaSeries.getData().add(new XYChart.Data<>("Q3", 3.80));
        gpaSeries.getData().add(new XYChart.Data<>("Q4", 3.85));
        gpaTrendChart.getData().add(gpaSeries);
    }

    private void loadParentData() {
        parentNameLabel.setText("Welcome, Sarah Anderson");

        // Load parent's children
        students.add(new StudentInfo("S001", "Emma Anderson", 10, 3.85));
        students.add(new StudentInfo("S002", "Liam Anderson", 8, 3.62));

        studentSelectorComboBox.setItems(students);
        studentSelectorComboBox.getSelectionModel().selectFirst();
    }

    private void loadSampleData() {
        if (students.isEmpty()) return;

        StudentInfo student = students.get(0);
        loadStudentData(student);
    }

    private void loadStudentData(StudentInfo student) {
        currentStudent = student;

        // Update quick stats
        currentGPALabel.setText(String.format("%.2f", student.getGpa()));
        attendanceRateLabel.setText("96.5%");
        missingAssignmentsLabel.setText("2");
        unreadMessagesLabel.setText("3");
        upcomingEventsLabel.setText("5");

        loadAlerts();
        loadRecentUpdates();
        loadCourseGrades();
        loadUpcomingAssignments();
        loadAttendanceData();
        loadMessages();
        loadCalendarEvents();

        updateLastUpdated();
        statusLabel.setText("Viewing " + student.getName() + "'s dashboard");
    }

    private void loadAlerts() {
        ObservableList<String> alerts = FXCollections.observableArrayList();
        alerts.add("⚠ Missing: Algebra II Homework #12 (Due: Yesterday)");
        alerts.add("⚠ Low Grade Alert: Chemistry Quiz - 62% (D)");

        alertsListView.setItems(alerts);
        alertCountLabel.setText(String.valueOf(alerts.size()));

        alertsListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #d32f2f; -fx-padding: 8; -fx-border-color: #ffcdd2; " +
                            "-fx-border-width: 0 0 1 0; -fx-background-color: #ffebee;");
                }
            }
        });
    }

    private void loadRecentUpdates() {
        ObservableList<String> updates = FXCollections.observableArrayList();
        updates.add("✓ New grade posted: English III - Essay #3: A- (92%)");
        updates.add("📝 New assignment: US History - Chapter 8 Review (Due: Dec 15)");
        updates.add("✉ New message from Ms. Johnson (Chemistry)");
        updates.add("📅 Reminder: Parent-Teacher Conference - Dec 10, 3:00 PM");

        updatesListView.setItems(updates);

        updatesListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-padding: 8; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
                }
            }
        });
    }

    private void loadCourseGrades() {
        courseGrades.clear();
        courseGrades.addAll(
                new CourseGradeInfo("English III", "Ms. Martinez", "A-", "92.3%", "↑ +2%"),
                new CourseGradeInfo("Algebra II", "Mr. Thompson", "B+", "88.5%", "→ Stable"),
                new CourseGradeInfo("US History", "Mr. Davis", "A", "94.7%", "↑ +3%"),
                new CourseGradeInfo("Chemistry", "Ms. Johnson", "B", "82.1%", "↓ -4%"),
                new CourseGradeInfo("Spanish II", "Sra. Garcia", "A", "95.2%", "↑ +1%"),
                new CourseGradeInfo("Physical Education", "Coach Williams", "A", "98.0%", "→ Stable")
        );
    }

    private void loadUpcomingAssignments() {
        upcomingAssignments.clear();

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

        upcomingAssignments.addAll(
                new AssignmentInfo("Algebra II", "Homework #13", today, "Pending"),
                new AssignmentInfo("English III", "Book Report", today.plusDays(2), "Pending"),
                new AssignmentInfo("Chemistry", "Lab Report #5", today.plusDays(3), "Pending"),
                new AssignmentInfo("US History", "Chapter 8 Review", today.plusDays(5), "Pending"),
                new AssignmentInfo("Spanish II", "Oral Presentation", today.plusDays(7), "Pending"),
                new AssignmentInfo("Algebra II", "Homework #12", today.minusDays(1), "Missing"),
                new AssignmentInfo("Chemistry", "Quiz Corrections", today.minusDays(2), "Missing"),
                new AssignmentInfo("English III", "Vocabulary Quiz", today.minusDays(5), "Graded"),
                new AssignmentInfo("US History", "Chapter 7 Test", today.minusDays(7), "Graded")
        );
    }

    private void loadAttendanceData() {
        daysPresentLabel.setText("142");
        daysAbsentLabel.setText("5");
        tardiesLabel.setText("3");
        earlyDismissalsLabel.setText("2");

        // Update pie chart
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Present (142)", 142),
                new PieChart.Data("Absent (5)", 5),
                new PieChart.Data("Tardy (3)", 3),
                new PieChart.Data("Early Dismissal (2)", 2)
        );
        attendancePieChart.setData(pieData);
    }

    private void loadMessages() {
        ObservableList<String> messages = FXCollections.observableArrayList();
        messages.add("📧 Ms. Johnson: Great improvement on the latest lab! (Dec 5)");
        messages.add("📧 Mr. Thompson: Please review homework corrections (Dec 4)");
        messages.add("📧 Ms. Martinez: Excellent essay work! (Dec 3)");

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
                    setStyle("-fx-padding: 10; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0; " +
                            "-fx-background-color: " + (item.contains("(Dec 5)") ? "#e3f2fd" : "white") + ";");
                }
            }
        });
    }

    private void loadCalendarEvents() {
        ObservableList<String> events = FXCollections.observableArrayList();
        events.add("📅 Dec 10 - Parent-Teacher Conference (3:00 PM)");
        events.add("📅 Dec 15 - Last Day Before Winter Break");
        events.add("📅 Dec 18 - Holiday Concert (7:00 PM)");
        events.add("📅 Dec 20 - Winter Break Begins");
        events.add("📅 Jan 6 - School Resumes");

        calendarEventsListView.setItems(events);

        calendarEventsListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-padding: 8; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
                }
            }
        });
    }

    private void filterAssignments() {
        // Filter logic would go here based on assignmentFilterComboBox selection
        statusLabel.setText("Assignment filter applied");
    }

    private void updateLastUpdated() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd 'at' h:mm a"));
        lastUpdatedLabel.setText("Last updated: " + timestamp);
    }

    // Event Handlers

    @FXML
    private void handleViewGradeDetails() {
        statusLabel.setText("Opening detailed grade report...");
        showAlert("Grade Details", "Detailed grade report will open in a new window");
    }

    @FXML
    private void handleComposeMessage() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Compose Message");
        dialog.setHeaderText("Send message to teacher");
        dialog.setContentText("Select teacher and enter your message:");

        dialog.showAndWait().ifPresent(message -> {
            statusLabel.setText("Message sent to teacher");
            showAlert("Message Sent", "Your message has been sent to the teacher");
        });
    }

    @FXML
    private void handleViewAllMessages() {
        statusLabel.setText("Opening message center...");
        showAlert("Messages", "Message center will open in a new window");
    }

    @FXML
    private void handleViewCalendar() {
        statusLabel.setText("Opening full calendar...");
        showAlert("Calendar", "Full school calendar will open in a new window");
    }

    @FXML
    private void handleViewReportCard() {
        if (currentStudent != null) {
            statusLabel.setText("Loading report card for " + currentStudent.getName());
            showAlert("Report Card", "Report card for " + currentStudent.getName() + " will open");
        }
    }

    @FXML
    private void handleMakePayment() {
        statusLabel.setText("Opening payment portal...");
        showAlert("Make Payment", "Payment portal will open for school fees, lunches, etc.");
    }

    @FXML
    private void handleUploadDocuments() {
        statusLabel.setText("Opening document upload...");
        showAlert("Upload Documents", "Document upload interface will open");
    }

    @FXML
    private void handleUpdateInfo() {
        statusLabel.setText("Opening contact information update...");
        showAlert("Update Information", "You can update contact info, emergency contacts, etc.");
    }

    @FXML
    private void handleHealthRecords() {
        if (currentStudent != null) {
            statusLabel.setText("Loading health records for " + currentStudent.getName());
            showAlert("Health Records", "View immunizations, medications, allergies, etc.");
        }
    }

    @FXML
    private void handleTransportation() {
        if (currentStudent != null) {
            statusLabel.setText("Loading transportation info for " + currentStudent.getName());
            showAlert("Transportation", "View bus routes, schedules, and request changes");
        }
    }

    @FXML
    private void handleMealAccount() {
        if (currentStudent != null) {
            statusLabel.setText("Loading meal account for " + currentStudent.getName());
            showAlert("Meal Account", "Current balance: $45.50\nView history and add funds");
        }
    }

    @FXML
    private void handleLibraryAccount() {
        if (currentStudent != null) {
            statusLabel.setText("Loading library account for " + currentStudent.getName());
            showAlert("Library Account", "Checked out books: 2\nNo overdue items");
        }
    }

    @FXML
    private void handleRefreshDashboard() {
        statusLabel.setText("Refreshing dashboard...");
        if (currentStudent != null) {
            loadStudentData(currentStudent);
        }
        statusLabel.setText("Dashboard refreshed successfully");
    }

    @FXML
    private void handleDownloadReports() {
        if (currentStudent != null) {
            statusLabel.setText("Preparing reports for download...");
            showAlert("Download Reports", "Available reports:\n" +
                    "- Progress Report\n" +
                    "- Attendance Summary\n" +
                    "- Grade History\n" +
                    "- Standardized Test Scores");
        }
    }

    @FXML
    private void handleHelp() {
        showAlert("Help & Support",
                "Parent Portal Help:\n\n" +
                        "• Monitor student progress in real-time\n" +
                        "• View grades, attendance, and assignments\n" +
                        "• Communicate with teachers\n" +
                        "• Access school calendar and events\n" +
                        "• Manage payments and accounts\n\n" +
                        "For technical support: support@heronix.edu\n" +
                        "Phone: (555) 123-4567");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Data Classes

    public static class StudentInfo {
        private String studentId;
        private String name;
        private int gradeLevel;
        private double gpa;

        public StudentInfo(String studentId, String name, int gradeLevel, double gpa) {
            this.studentId = studentId;
            this.name = name;
            this.gradeLevel = gradeLevel;
            this.gpa = gpa;
        }

        public String getStudentId() { return studentId; }
        public String getName() { return name; }
        public int getGradeLevel() { return gradeLevel; }
        public double getGpa() { return gpa; }
    }

    public static class CourseGradeInfo {
        private String courseName;
        private String teacher;
        private String letterGrade;
        private String percentage;
        private String trend;

        public CourseGradeInfo(String courseName, String teacher, String letterGrade, String percentage, String trend) {
            this.courseName = courseName;
            this.teacher = teacher;
            this.letterGrade = letterGrade;
            this.percentage = percentage;
            this.trend = trend;
        }

        public String getCourseName() { return courseName; }
        public String getTeacher() { return teacher; }
        public String getLetterGrade() { return letterGrade; }
        public String getPercentage() { return percentage; }
        public String getTrend() { return trend; }
    }

    public static class AssignmentInfo {
        private String course;
        private String assignmentName;
        private String dueDate;
        private String status;

        public AssignmentInfo(String course, String assignmentName, LocalDate dueDate, String status) {
            this.course = course;
            this.assignmentName = assignmentName;
            this.status = status;

            // Format due date
            LocalDate today = LocalDate.now();
            if (dueDate.equals(today)) {
                this.dueDate = "Today";
            } else if (dueDate.equals(today.plusDays(1))) {
                this.dueDate = "Tomorrow";
            } else if (dueDate.equals(today.minusDays(1))) {
                this.dueDate = "Yesterday";
            } else {
                this.dueDate = dueDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
            }
        }

        public String getCourse() { return course; }
        public String getAssignmentName() { return assignmentName; }
        public String getDueDate() { return dueDate; }
        public String getStatus() { return status; }
    }
}
