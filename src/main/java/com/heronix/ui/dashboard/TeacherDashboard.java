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
 * Teacher Dashboard
 * Dashboard view for teachers with class schedule, attendance, grades, and tasks.
 *
 * Layout:
 * - Top: Welcome message + today's schedule
 * - KPIs: Classes today, pending grades, attendance rate
 * - Quick actions for common tasks
 * - Today's classes timeline
 * - Recent grades + pending assignments
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class TeacherDashboard extends ScrollPane {

    // ========================================================================
    // WIDGETS
    // ========================================================================

    private KpiWidget classesTodayKpi;
    private KpiWidget pendingGradesKpi;
    private KpiWidget attendanceKpi;
    private KpiWidget messagesKpi;

    private QuickActionsWidget quickActions;
    private AlertsWidget alerts;

    private DashboardWidget scheduleWidget;
    private TableWidget<Map<String, String>> recentGrades;
    private TableWidget<Map<String, String>> pendingAssignments;
    private TableWidget<Map<String, String>> classRoster;

    // ========================================================================
    // STATE
    // ========================================================================

    private String teacherName = "Teacher";
    private Consumer<String> onNavigate;
    private Consumer<String> onQuickAction;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public TeacherDashboard() {
        initializeUI();
        loadDemoData();

        log.info("TeacherDashboard initialized");
    }

    private void initializeUI() {
        VBox container = new VBox(24);
        container.setPadding(new Insets(24));
        container.setStyle("-fx-background-color: #F8FAFC;");

        // Header with greeting
        VBox headerBox = createHeader();

        // KPI Row
        HBox kpiRow = createKpiRow();

        // Actions + Alerts Row
        HBox actionsRow = createActionsRow();

        // Schedule + Classes Row
        HBox scheduleRow = createScheduleRow();

        // Tables Row
        HBox tablesRow = createTablesRow();

        container.getChildren().addAll(headerBox, kpiRow, actionsRow, scheduleRow, tablesRow);

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

        Label greetingLabel = new Label(greeting + ", " + teacherName + "!");
        greetingLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: #0F172A;");

        Label dateLabel = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748B;");

        return new VBox(4, greetingLabel, dateLabel);
    }

    private HBox createKpiRow() {
        classesTodayKpi = new KpiWidget("Classes Today", 5);
        classesTodayKpi.setIcon("📚");
        classesTodayKpi.setShowTrend(false);
        classesTodayKpi.setShowComparison(false);

        pendingGradesKpi = new KpiWidget("Pending Grades", 23);
        pendingGradesKpi.setIcon("📝");
        pendingGradesKpi.setShowTrend(false);
        pendingGradesKpi.setComparisonLabel("assignments to grade");
        pendingGradesKpi.setStatus(KpiWidget.StatusType.WARNING);

        attendanceKpi = new KpiWidget("Attendance Today", 94.2);
        attendanceKpi.setIcon("✓");
        attendanceKpi.setUnit("%");
        attendanceKpi.setFormat("%.1f");
        attendanceKpi.setPreviousValue(93.8);
        attendanceKpi.setComparisonLabel("vs. class average");

        messagesKpi = new KpiWidget("Unread Messages", 3);
        messagesKpi.setIcon("✉");
        messagesKpi.setShowTrend(false);
        messagesKpi.setShowComparison(false);

        return new HBox(16, classesTodayKpi, pendingGradesKpi, attendanceKpi, messagesKpi);
    }

    private HBox createActionsRow() {
        quickActions = QuickActionsWidget.forTeacher();
        quickActions.setOnActionClick(action -> {
            if (onQuickAction != null) {
                onQuickAction.accept(action.id);
            }
        });

        alerts = new AlertsWidget();
        alerts.setTitle("Reminders");
        alerts.setIcon("📌");

        HBox.setHgrow(quickActions, Priority.ALWAYS);
        HBox.setHgrow(alerts, Priority.ALWAYS);

        return new HBox(16, quickActions, alerts);
    }

    private HBox createScheduleRow() {
        scheduleWidget = new DashboardWidget("Today's Schedule", "📅");
        scheduleWidget.setSize(DashboardWidget.WidgetSize.LARGE);
        scheduleWidget.setRefreshable(false);
        scheduleWidget.setContent(createScheduleTimeline());

        classRoster = TableWidget.classRoster();
        classRoster.setOnRowClick(cls -> {
            if (onNavigate != null) {
                onNavigate.accept("class:" + cls.get("id"));
            }
        });

        HBox.setHgrow(scheduleWidget, Priority.ALWAYS);
        HBox.setHgrow(classRoster, Priority.ALWAYS);

        return new HBox(16, scheduleWidget, classRoster);
    }

    private HBox createTablesRow() {
        recentGrades = TableWidget.recentGrades();
        recentGrades.setOnViewAll(() -> {
            if (onNavigate != null) onNavigate.accept("gradebook");
        });

        pendingAssignments = new TableWidget<>("Pending Assignments");
        pendingAssignments.setIcon("📄");
        pendingAssignments.setSubtitle("Need grading");
        pendingAssignments.addColumn("Assignment", map -> map.get("title"));
        pendingAssignments.addColumn("Class", map -> map.get("class"));
        pendingAssignments.addColumn("Submissions", map -> map.get("submissions"));
        pendingAssignments.setOnViewAll(() -> {
            if (onNavigate != null) onNavigate.accept("assignments");
        });

        HBox.setHgrow(recentGrades, Priority.ALWAYS);
        HBox.setHgrow(pendingAssignments, Priority.ALWAYS);

        return new HBox(16, recentGrades, pendingAssignments);
    }

    private VBox createScheduleTimeline() {
        VBox timeline = new VBox(0);
        timeline.setPadding(new Insets(8));

        // Demo schedule
        List<ScheduleItem> schedule = Arrays.asList(
            new ScheduleItem("8:00 AM", "9:00 AM", "Algebra II - Period 1", "Room 204", true),
            new ScheduleItem("9:15 AM", "10:15 AM", "Geometry - Period 2", "Room 204", false),
            new ScheduleItem("10:30 AM", "11:30 AM", "Planning Period", "", false),
            new ScheduleItem("11:45 AM", "12:45 PM", "Pre-Calculus - Period 4", "Room 204", false),
            new ScheduleItem("1:00 PM", "2:00 PM", "Lunch", "", false),
            new ScheduleItem("2:15 PM", "3:15 PM", "Algebra I - Period 6", "Room 204", false)
        );

        for (ScheduleItem item : schedule) {
            timeline.getChildren().add(createScheduleCard(item));
        }

        return timeline;
    }

    private HBox createScheduleCard(ScheduleItem item) {
        // Time column
        Label timeLabel = new Label(item.startTime);
        timeLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 500; -fx-text-fill: #64748B;");
        timeLabel.setMinWidth(70);

        // Timeline indicator
        VBox indicator = new VBox();
        indicator.setAlignment(Pos.TOP_CENTER);
        indicator.setMinWidth(20);

        Label dot = new Label("●");
        dot.setStyle(item.isCurrent ?
            "-fx-font-size: 10px; -fx-text-fill: #2563EB;" :
            "-fx-font-size: 10px; -fx-text-fill: #CBD5E1;");

        Region line = new Region();
        line.setStyle("-fx-background-color: #E2E8F0;");
        line.setMinWidth(2);
        line.setMaxWidth(2);
        line.setPrefHeight(40);
        VBox.setVgrow(line, Priority.ALWAYS);

        indicator.getChildren().addAll(dot, line);

        // Content
        Label titleLabel = new Label(item.title);
        titleLabel.setStyle(item.isCurrent ?
            "-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #2563EB;" :
            "-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: #0F172A;");

        Label roomLabel = new Label(item.room);
        roomLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");
        roomLabel.setVisible(!item.room.isEmpty());
        roomLabel.setManaged(!item.room.isEmpty());

        VBox contentBox = new VBox(2, titleLabel, roomLabel);
        contentBox.setPadding(new Insets(0, 0, 16, 8));
        HBox.setHgrow(contentBox, Priority.ALWAYS);

        // Current class badge
        if (item.isCurrent) {
            Label badge = new Label("NOW");
            badge.setStyle("""
                -fx-background-color: #DBEAFE;
                -fx-text-fill: #2563EB;
                -fx-font-size: 10px;
                -fx-font-weight: 600;
                -fx-padding: 2 6;
                -fx-background-radius: 4;
                """);
            contentBox.getChildren().add(badge);
        }

        HBox card = new HBox(8, timeLabel, indicator, contentBox);
        card.setAlignment(Pos.TOP_LEFT);

        return card;
    }

    // ========================================================================
    // DEMO DATA
    // ========================================================================

    private void loadDemoData() {
        loadRecentGrades();
        loadPendingAssignments();
        loadClassRoster();
        loadAlerts();
    }

    private void loadRecentGrades() {
        List<Map<String, String>> grades = new ArrayList<>();
        grades.add(Map.of("student", "Emma J.", "assignment", "Quiz 5", "grade", "92%"));
        grades.add(Map.of("student", "Liam W.", "assignment", "Homework 8", "grade", "85%"));
        grades.add(Map.of("student", "Olivia B.", "assignment", "Quiz 5", "grade", "88%"));
        grades.add(Map.of("student", "Noah D.", "assignment", "Project 2", "grade", "95%"));
        grades.add(Map.of("student", "Ava M.", "assignment", "Quiz 5", "grade", "78%"));

        recentGrades.setData(grades);
    }

    private void loadPendingAssignments() {
        List<Map<String, String>> assignments = new ArrayList<>();
        assignments.add(Map.of("title", "Chapter 5 Test", "class", "Algebra II", "submissions", "28/30"));
        assignments.add(Map.of("title", "Geometry Proof", "class", "Geometry", "submissions", "25/28"));
        assignments.add(Map.of("title", "Problem Set 9", "class", "Pre-Calculus", "submissions", "22/26"));
        assignments.add(Map.of("title", "Weekly Quiz", "class", "Algebra I", "submissions", "30/32"));

        pendingAssignments.setData(assignments);
    }

    private void loadClassRoster() {
        List<Map<String, String>> classes = new ArrayList<>();
        classes.add(Map.of("id", "101", "name", "Algebra II - P1", "count", "30", "nextClass", "8:00 AM"));
        classes.add(Map.of("id", "102", "name", "Geometry - P2", "count", "28", "nextClass", "9:15 AM"));
        classes.add(Map.of("id", "103", "name", "Pre-Calculus - P4", "count", "26", "nextClass", "11:45 AM"));
        classes.add(Map.of("id", "104", "name", "Algebra I - P6", "count", "32", "nextClass", "2:15 PM"));

        classRoster.setData(classes);
    }

    private void loadAlerts() {
        alerts.addAlert(new AlertsWidget.Alert("1", AlertsWidget.AlertType.WARNING,
                "Grades Due", "Chapter 5 Test grades due tomorrow")
                .withAction("gradebook"));

        alerts.addAlert(new AlertsWidget.Alert("2", AlertsWidget.AlertType.INFO,
                "Department Meeting", "Math department meeting at 3:30 PM today"));

        alerts.addAlert(new AlertsWidget.Alert("3", AlertsWidget.AlertType.INFO,
                "Parent Conference", "Conference with Smith family scheduled for tomorrow 4PM"));
    }

    // ========================================================================
    // PUBLIC API
    // ========================================================================

    /**
     * Set teacher name for greeting
     */
    public void setTeacherName(String name) {
        this.teacherName = name;
        // Update greeting would require rebuilding header
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
     * Update pending grades count
     */
    public void updatePendingGrades(int count) {
        pendingGradesKpi.setValue(count);
        pendingGradesKpi.setStatus(count > 20 ? KpiWidget.StatusType.DANGER :
                                   count > 10 ? KpiWidget.StatusType.WARNING :
                                   KpiWidget.StatusType.SUCCESS);
    }

    // ========================================================================
    // INNER CLASS
    // ========================================================================

    private static class ScheduleItem {
        String startTime;
        String endTime;
        String title;
        String room;
        boolean isCurrent;

        ScheduleItem(String start, String end, String title, String room, boolean current) {
            this.startTime = start;
            this.endTime = end;
            this.title = title;
            this.room = room;
            this.isCurrent = current;
        }
    }
}
