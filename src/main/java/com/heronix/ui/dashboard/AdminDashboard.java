package com.heronix.ui.dashboard;

import com.heronix.ui.dashboard.widget.*;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

/**
 * Admin Dashboard
 * The main dashboard view for administrators with KPIs, charts, and quick actions.
 *
 * Layout:
 * - Top row: KPI cards (4 across)
 * - Second row: Quick actions + Alerts
 * - Third row: Charts (enrollment trend, grade distribution)
 * - Bottom row: Recent students, upcoming events tables
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class AdminDashboard extends ScrollPane {

    // ========================================================================
    // WIDGETS
    // ========================================================================

    private KpiWidget enrollmentKpi;
    private KpiWidget attendanceKpi;
    private KpiWidget gpaKpi;
    private KpiWidget graduationKpi;

    private QuickActionsWidget quickActions;
    private AlertsWidget alerts;

    private ChartWidget enrollmentChart;
    private ChartWidget gradeChart;

    private TableWidget<Map<String, String>> recentStudents;
    private TableWidget<Map<String, String>> upcomingEvents;

    // ========================================================================
    // CALLBACKS
    // ========================================================================

    private Consumer<String> onNavigate;
    private Consumer<String> onQuickAction;
    private Consumer<AlertsWidget.Alert> onAlertClick;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public AdminDashboard() {
        initializeUI();
        loadDemoData();

        log.info("AdminDashboard initialized");
    }

    private void initializeUI() {
        // Main container
        VBox container = new VBox(24);
        container.setPadding(new Insets(24));
        container.setStyle("-fx-background-color: #F8FAFC;");

        // Header
        Label header = new Label("Dashboard");
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: #0F172A;");

        Label dateLabel = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748B;");

        VBox headerBox = new VBox(4, header, dateLabel);

        // KPI Row
        HBox kpiRow = createKpiRow();

        // Actions + Alerts Row
        HBox actionsRow = createActionsRow();

        // Charts Row
        HBox chartsRow = createChartsRow();

        // Tables Row
        HBox tablesRow = createTablesRow();

        container.getChildren().addAll(headerBox, kpiRow, actionsRow, chartsRow, tablesRow);

        // Scroll pane settings
        setContent(container);
        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setStyle("-fx-background-color: #F8FAFC; -fx-border-width: 0;");
    }

    // ========================================================================
    // ROW BUILDERS
    // ========================================================================

    private HBox createKpiRow() {
        enrollmentKpi = KpiWidget.enrollment(2847, 2650);
        attendanceKpi = KpiWidget.attendance(94.7, 93.2);
        gpaKpi = KpiWidget.gpa(3.24, 3.18);
        graduationKpi = KpiWidget.graduationRate(92.5);

        // Set refresh handlers
        enrollmentKpi.setOnRefresh(() -> {
            // Simulate refresh
            enrollmentKpi.setLoading(true);
            new Thread(() -> {
                try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                javafx.application.Platform.runLater(() -> {
                    enrollmentKpi.setValue(2850 + (int)(Math.random() * 50), 2650);
                    enrollmentKpi.setLoading(false);
                });
            }).start();
        });

        HBox row = new HBox(16, enrollmentKpi, attendanceKpi, gpaKpi, graduationKpi);
        row.setFillHeight(false);
        return row;
    }

    private HBox createActionsRow() {
        quickActions = QuickActionsWidget.forAdmin();
        quickActions.setOnActionClick(action -> {
            log.debug("Quick action: {}", action.id);
            if (onQuickAction != null) {
                onQuickAction.accept(action.id);
            }
        });

        alerts = new AlertsWidget();
        alerts.setOnAlertClick(alert -> {
            if (onAlertClick != null) {
                onAlertClick.accept(alert);
            }
        });

        HBox.setHgrow(quickActions, Priority.ALWAYS);
        HBox.setHgrow(alerts, Priority.ALWAYS);

        HBox row = new HBox(16, quickActions, alerts);
        return row;
    }

    private HBox createChartsRow() {
        enrollmentChart = ChartWidget.enrollmentTrend();
        gradeChart = ChartWidget.gradeDistribution();

        HBox.setHgrow(enrollmentChart, Priority.ALWAYS);
        HBox.setHgrow(gradeChart, Priority.ALWAYS);

        HBox row = new HBox(16, enrollmentChart, gradeChart);
        return row;
    }

    private HBox createTablesRow() {
        recentStudents = TableWidget.recentStudents();
        recentStudents.setOnViewAll(() -> {
            if (onNavigate != null) onNavigate.accept("students");
        });
        recentStudents.setOnRowClick(student -> {
            log.debug("Clicked student: {}", student.get("name"));
            if (onNavigate != null) onNavigate.accept("student:" + student.get("id"));
        });

        upcomingEvents = TableWidget.upcomingEvents();
        upcomingEvents.setOnViewAll(() -> {
            if (onNavigate != null) onNavigate.accept("calendar");
        });

        HBox.setHgrow(recentStudents, Priority.ALWAYS);
        HBox.setHgrow(upcomingEvents, Priority.ALWAYS);

        HBox row = new HBox(16, recentStudents, upcomingEvents);
        return row;
    }

    // ========================================================================
    // DEMO DATA
    // ========================================================================

    private void loadDemoData() {
        // Load chart data
        loadEnrollmentChartData();
        loadGradeChartData();

        // Load table data
        loadRecentStudentsData();
        loadUpcomingEventsData();

        // Load alerts
        loadAlerts();
    }

    private void loadEnrollmentChartData() {
        Map<String, Number> data = new LinkedHashMap<>();
        data.put("Feb", 2720);
        data.put("Mar", 2745);
        data.put("Apr", 2760);
        data.put("May", 2780);
        data.put("Jun", 2650);
        data.put("Jul", 2620);
        data.put("Aug", 2800);
        data.put("Sep", 2830);
        data.put("Oct", 2840);
        data.put("Nov", 2845);
        data.put("Dec", 2843);
        data.put("Jan", 2847);

        enrollmentChart.setLineData("Total Enrollment", data);
    }

    private void loadGradeChartData() {
        Map<String, Number> data = new LinkedHashMap<>();
        data.put("A", 425);
        data.put("B", 612);
        data.put("C", 534);
        data.put("D", 189);
        data.put("F", 87);

        gradeChart.setBarData("Grades", data);
    }

    private void loadRecentStudentsData() {
        List<Map<String, String>> students = new ArrayList<>();
        students.add(Map.of("id", "1001", "name", "Emma Johnson", "grade", "10th", "date", "Jan 10"));
        students.add(Map.of("id", "1002", "name", "Liam Williams", "grade", "9th", "date", "Jan 9"));
        students.add(Map.of("id", "1003", "name", "Olivia Brown", "grade", "11th", "date", "Jan 8"));
        students.add(Map.of("id", "1004", "name", "Noah Davis", "grade", "10th", "date", "Jan 7"));
        students.add(Map.of("id", "1005", "name", "Ava Martinez", "grade", "12th", "date", "Jan 6"));
        students.add(Map.of("id", "1006", "name", "Ethan Garcia", "grade", "9th", "date", "Jan 5"));

        recentStudents.setData(students);
    }

    private void loadUpcomingEventsData() {
        List<Map<String, String>> events = new ArrayList<>();
        events.add(Map.of("title", "Parent-Teacher Conf.", "date", "Jan 15", "type", "Meeting"));
        events.add(Map.of("title", "Winter Sports Start", "date", "Jan 17", "type", "Sports"));
        events.add(Map.of("title", "MLK Day (No School)", "date", "Jan 20", "type", "Holiday"));
        events.add(Map.of("title", "Semester 1 Ends", "date", "Jan 24", "type", "Academic"));
        events.add(Map.of("title", "Report Cards Issued", "date", "Jan 27", "type", "Academic"));

        upcomingEvents.setData(events);
    }

    private void loadAlerts() {
        alerts.addAlert(new AlertsWidget.Alert("1", AlertsWidget.AlertType.WARNING,
                "Incomplete Attendance", "5 classes have not submitted attendance for today")
                .withAction("attendance"));

        alerts.addAlert(new AlertsWidget.Alert("2", AlertsWidget.AlertType.INFO,
                "Report Cards Due", "Semester 1 report cards are due in 3 days"));

        alerts.addAlert(new AlertsWidget.Alert("3", AlertsWidget.AlertType.SUCCESS,
                "Enrollment Target Met", "Fall enrollment exceeded target by 2.3%"));

        alerts.addAlert(new AlertsWidget.Alert("4", AlertsWidget.AlertType.ERROR,
                "System Maintenance", "Scheduled maintenance on Jan 18, 10PM-2AM")
                .nonDismissible());
    }

    // ========================================================================
    // PUBLIC API
    // ========================================================================

    /**
     * Refresh all dashboard data
     */
    public void refresh() {
        enrollmentKpi.refresh();
        attendanceKpi.refresh();
        gpaKpi.refresh();
        graduationKpi.refresh();
        loadDemoData();
    }

    /**
     * Update enrollment KPI
     */
    public void updateEnrollment(int current, int previous) {
        enrollmentKpi.setValue(current, previous);
    }

    /**
     * Update attendance KPI
     */
    public void updateAttendance(double rate, double previousRate) {
        attendanceKpi.setValue(rate, previousRate);
    }

    /**
     * Update GPA KPI
     */
    public void updateGpa(double gpa, double previousGpa) {
        gpaKpi.setValue(gpa, previousGpa);
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
     * Set alert click callback
     */
    public void setOnAlertClick(Consumer<AlertsWidget.Alert> callback) {
        this.onAlertClick = callback;
    }

    /**
     * Add a system alert
     */
    public void addAlert(AlertsWidget.AlertType type, String title, String message) {
        alerts.addAlert(UUID.randomUUID().toString(), type, title, message);
    }
}
