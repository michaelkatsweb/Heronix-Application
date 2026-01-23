package com.heronix.ui.dashboard;

import com.heronix.ui.dashboard.widget.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

/**
 * Student Dashboard
 * Dashboard view for students with schedule, grades, assignments, and announcements.
 *
 * Layout:
 * - Header: Welcome + today's date
 * - KPIs: GPA, attendance, assignments due, unread messages
 * - Today's schedule
 * - Quick actions
 * - Upcoming assignments
 * - Recent grades
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class StudentDashboard extends ScrollPane {

    // ========================================================================
    // WIDGETS
    // ========================================================================

    private KpiWidget gpaKpi;
    private KpiWidget attendanceKpi;
    private KpiWidget assignmentsDueKpi;
    private KpiWidget messagesKpi;

    private QuickActionsWidget quickActions;
    private DashboardWidget scheduleWidget;
    private TableWidget<Map<String, String>> upcomingAssignments;
    private TableWidget<Map<String, String>> recentGrades;
    private AlertsWidget announcements;

    // ========================================================================
    // STATE
    // ========================================================================

    private String studentName = "Student";
    private Consumer<String> onNavigate;
    private Consumer<String> onQuickAction;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public StudentDashboard() {
        initializeUI();
        loadDemoData();

        log.info("StudentDashboard initialized");
    }

    private void initializeUI() {
        VBox container = new VBox(24);
        container.setPadding(new Insets(24));
        container.setStyle("-fx-background-color: #F8FAFC;");

        // Header
        VBox headerBox = createHeader();

        // KPI Row
        HBox kpiRow = createKpiRow();

        // Schedule + Quick Actions Row
        HBox scheduleRow = createScheduleRow();

        // Assignments + Grades Row
        HBox academicsRow = createAcademicsRow();

        container.getChildren().addAll(headerBox, kpiRow, scheduleRow, academicsRow);

        setContent(container);
        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setStyle("-fx-background-color: #F8FAFC; -fx-border-width: 0;");
    }

    // ========================================================================
    // ROW BUILDERS
    // ========================================================================

    private VBox createHeader() {
        int hour = LocalTime.now().getHour();
        String greeting = hour < 12 ? "Good morning" : hour < 17 ? "Good afternoon" : "Good evening";

        Label greetingLabel = new Label(greeting + ", " + studentName + "!");
        greetingLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: #0F172A;");

        Label dateLabel = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748B;");

        // Motivational message
        String[] messages = {
            "Ready to learn something new today?",
            "Make today count!",
            "Keep up the great work!",
            "You're doing amazing!"
        };
        Label motivationLabel = new Label(messages[new Random().nextInt(messages.length)]);
        motivationLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B; -fx-font-style: italic;");

        return new VBox(4, greetingLabel, dateLabel, motivationLabel);
    }

    private HBox createKpiRow() {
        gpaKpi = new KpiWidget("My GPA", 3.45);
        gpaKpi.setIcon("📊");
        gpaKpi.setFormat("%.2f");
        gpaKpi.setShowComparison(false);
        gpaKpi.setShowTrend(true);
        gpaKpi.setPreviousValue(3.38);
        gpaKpi.setStatus(KpiWidget.StatusType.SUCCESS);

        attendanceKpi = new KpiWidget("Attendance", 96.5);
        attendanceKpi.setIcon("✓");
        attendanceKpi.setUnit("%");
        attendanceKpi.setFormat("%.1f");
        attendanceKpi.setShowTrend(false);
        attendanceKpi.setShowComparison(false);
        attendanceKpi.setStatus(KpiWidget.StatusType.SUCCESS);

        assignmentsDueKpi = new KpiWidget("Due This Week", 4);
        assignmentsDueKpi.setIcon("📝");
        assignmentsDueKpi.setShowTrend(false);
        assignmentsDueKpi.setComparisonLabel("assignments");
        assignmentsDueKpi.setStatus(KpiWidget.StatusType.WARNING);

        messagesKpi = new KpiWidget("Unread", 2);
        messagesKpi.setIcon("✉");
        messagesKpi.setShowTrend(false);
        messagesKpi.setComparisonLabel("messages");

        return new HBox(16, gpaKpi, attendanceKpi, assignmentsDueKpi, messagesKpi);
    }

    private HBox createScheduleRow() {
        scheduleWidget = new DashboardWidget("Today's Classes", "📅");
        scheduleWidget.setSize(DashboardWidget.WidgetSize.LARGE);
        scheduleWidget.setRefreshable(false);
        scheduleWidget.setContent(createScheduleList());

        VBox rightColumn = new VBox(16);

        quickActions = QuickActionsWidget.forStudent();
        quickActions.setOnActionClick(action -> {
            if (onQuickAction != null) {
                onQuickAction.accept(action.id);
            }
        });

        announcements = new AlertsWidget();
        announcements.setTitle("Announcements");
        announcements.setIcon("📢");

        rightColumn.getChildren().addAll(quickActions, announcements);

        HBox.setHgrow(scheduleWidget, Priority.ALWAYS);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);

        return new HBox(16, scheduleWidget, rightColumn);
    }

    private HBox createAcademicsRow() {
        upcomingAssignments = new TableWidget<>("Upcoming Assignments");
        upcomingAssignments.setIcon("📄");
        upcomingAssignments.setSubtitle("Due soon");
        upcomingAssignments.addColumn("Assignment", map -> map.get("title"));
        upcomingAssignments.addColumn("Class", map -> map.get("class"));
        upcomingAssignments.addColumn("Due", map -> map.get("due"));
        upcomingAssignments.setOnViewAll(() -> {
            if (onNavigate != null) onNavigate.accept("assignments");
        });
        upcomingAssignments.setOnRowClick(assignment -> {
            if (onNavigate != null) {
                onNavigate.accept("assignment:" + assignment.get("id"));
            }
        });

        recentGrades = new TableWidget<>("Recent Grades");
        recentGrades.setIcon("📊");
        recentGrades.addColumn("Assignment", map -> map.get("assignment"));
        recentGrades.addColumn("Class", map -> map.get("class"));
        recentGrades.addColumn("Grade", map -> map.get("grade"));
        recentGrades.setOnViewAll(() -> {
            if (onNavigate != null) onNavigate.accept("grades");
        });

        HBox.setHgrow(upcomingAssignments, Priority.ALWAYS);
        HBox.setHgrow(recentGrades, Priority.ALWAYS);

        return new HBox(16, upcomingAssignments, recentGrades);
    }

    private VBox createScheduleList() {
        VBox scheduleList = new VBox(8);
        scheduleList.setPadding(new Insets(8));

        // Demo schedule
        List<ClassPeriod> schedule = Arrays.asList(
            new ClassPeriod("Period 1", "8:00 - 9:00 AM", "English 11", "Mrs. Johnson", "Room 105", true, false),
            new ClassPeriod("Period 2", "9:15 - 10:15 AM", "Algebra II", "Mr. Smith", "Room 204", false, false),
            new ClassPeriod("Period 3", "10:30 - 11:30 AM", "Chemistry", "Ms. Davis", "Lab 302", false, false),
            new ClassPeriod("Period 4", "11:45 - 12:45 PM", "US History", "Mr. Brown", "Room 108", false, true),
            new ClassPeriod("Lunch", "1:00 - 1:45 PM", "", "", "Cafeteria", false, false),
            new ClassPeriod("Period 5", "2:00 - 3:00 PM", "PE", "Coach Williams", "Gym", false, false)
        );

        for (ClassPeriod period : schedule) {
            scheduleList.getChildren().add(createClassCard(period));
        }

        return scheduleList;
    }

    private HBox createClassCard(ClassPeriod period) {
        // Period indicator
        Label periodLabel = new Label(period.period);
        periodLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 600; -fx-text-fill: #64748B;");
        periodLabel.setMinWidth(60);

        // Time
        Label timeLabel = new Label(period.time);
        timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");
        timeLabel.setMinWidth(100);

        // Class info
        VBox classInfo = new VBox(2);
        HBox.setHgrow(classInfo, Priority.ALWAYS);

        if (period.className.isEmpty()) {
            // Lunch or break
            Label breakLabel = new Label(period.period);
            breakLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B;");
            classInfo.getChildren().add(breakLabel);
        } else {
            Label classLabel = new Label(period.className);
            classLabel.setStyle(period.isCurrent ?
                "-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #2563EB;" :
                "-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: #0F172A;");

            Label teacherLabel = new Label(period.teacher + " • " + period.room);
            teacherLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");

            classInfo.getChildren().addAll(classLabel, teacherLabel);
        }

        // Badges
        HBox badges = new HBox(4);
        badges.setAlignment(Pos.CENTER_RIGHT);

        if (period.isCurrent) {
            Label nowBadge = new Label("NOW");
            nowBadge.setStyle("""
                -fx-background-color: #DBEAFE;
                -fx-text-fill: #2563EB;
                -fx-font-size: 9px;
                -fx-font-weight: 600;
                -fx-padding: 2 6;
                -fx-background-radius: 4;
                """);
            badges.getChildren().add(nowBadge);
        }

        if (period.hasAssignmentDue) {
            Label dueBadge = new Label("DUE");
            dueBadge.setStyle("""
                -fx-background-color: #FEF3C7;
                -fx-text-fill: #D97706;
                -fx-font-size: 9px;
                -fx-font-weight: 600;
                -fx-padding: 2 6;
                -fx-background-radius: 4;
                """);
            badges.getChildren().add(dueBadge);
        }

        HBox card = new HBox(12, periodLabel, timeLabel, classInfo, badges);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(10, 12, 10, 12));

        String bgColor = period.isCurrent ? "#EFF6FF" : period.className.isEmpty() ? "#F8FAFC" : "white";
        String borderColor = period.isCurrent ? "#BFDBFE" : "#E2E8F0";

        card.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 6;
            -fx-border-color: %s;
            -fx-border-radius: 6;
            """, bgColor, borderColor));

        return card;
    }

    // ========================================================================
    // DEMO DATA
    // ========================================================================

    private void loadDemoData() {
        loadAssignments();
        loadGrades();
        loadAnnouncements();
    }

    private void loadAssignments() {
        List<Map<String, String>> assignments = new ArrayList<>();
        assignments.add(Map.of("id", "1", "title", "Essay: To Kill a Mockingbird", "class", "English 11", "due", "Tomorrow"));
        assignments.add(Map.of("id", "2", "title", "Chapter 8 Problems", "class", "Algebra II", "due", "Wed"));
        assignments.add(Map.of("id", "3", "title", "Lab Report: Titration", "class", "Chemistry", "due", "Thu"));
        assignments.add(Map.of("id", "4", "title", "Civil War Project", "class", "US History", "due", "Fri"));
        assignments.add(Map.of("id", "5", "title", "Vocabulary Quiz Prep", "class", "English 11", "due", "Next Mon"));

        upcomingAssignments.setData(assignments);
    }

    private void loadGrades() {
        List<Map<String, String>> grades = new ArrayList<>();
        grades.add(Map.of("assignment", "Quiz: Chapter 7", "class", "Algebra II", "grade", "92%"));
        grades.add(Map.of("assignment", "Lab Report", "class", "Chemistry", "grade", "88%"));
        grades.add(Map.of("assignment", "Essay Draft", "class", "English 11", "grade", "A-"));
        grades.add(Map.of("assignment", "Test: WWII", "class", "US History", "grade", "85%"));
        grades.add(Map.of("assignment", "Homework 15", "class", "Algebra II", "grade", "100%"));

        recentGrades.setData(grades);
    }

    private void loadAnnouncements() {
        announcements.addAlert(new AlertsWidget.Alert("1", AlertsWidget.AlertType.INFO,
                "Winter Dance", "Winter formal dance tickets on sale now in the cafeteria!"));

        announcements.addAlert(new AlertsWidget.Alert("2", AlertsWidget.AlertType.WARNING,
                "Schedule Change", "Early dismissal this Friday at 1:00 PM"));

        announcements.addAlert(new AlertsWidget.Alert("3", AlertsWidget.AlertType.INFO,
                "Club Fair", "Spring club fair next Wednesday during lunch"));
    }

    // ========================================================================
    // PUBLIC API
    // ========================================================================

    /**
     * Set student name
     */
    public void setStudentName(String name) {
        this.studentName = name;
    }

    /**
     * Refresh dashboard
     */
    public void refresh() {
        loadDemoData();
    }

    /**
     * Set navigation callback
     */
    public void setOnNavigate(Consumer<String> callback) {
        this.onNavigate = callback;
    }

    /**
     * Set quick action callback
     */
    public void setOnQuickAction(Consumer<String> callback) {
        this.onQuickAction = callback;
    }

    /**
     * Update GPA display
     */
    public void updateGpa(double gpa, double previousGpa) {
        gpaKpi.setValue(gpa, previousGpa);
    }

    /**
     * Update attendance display
     */
    public void updateAttendance(double rate) {
        attendanceKpi.setValue(rate);
        attendanceKpi.setStatus(rate >= 95 ? KpiWidget.StatusType.SUCCESS :
                               rate >= 90 ? KpiWidget.StatusType.WARNING :
                               KpiWidget.StatusType.DANGER);
    }

    // ========================================================================
    // INNER CLASS
    // ========================================================================

    private static class ClassPeriod {
        String period;
        String time;
        String className;
        String teacher;
        String room;
        boolean isCurrent;
        boolean hasAssignmentDue;

        ClassPeriod(String period, String time, String className, String teacher, String room,
                   boolean isCurrent, boolean hasAssignmentDue) {
            this.period = period;
            this.time = time;
            this.className = className;
            this.teacher = teacher;
            this.room = room;
            this.isCurrent = isCurrent;
            this.hasAssignmentDue = hasAssignmentDue;
        }
    }
}
