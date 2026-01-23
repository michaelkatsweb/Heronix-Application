package com.heronix.ui.dashboard;

import com.heronix.ui.dashboard.widget.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

/**
 * Parent Dashboard
 * Dashboard view for parents/guardians to monitor their children's progress.
 *
 * Features:
 * - Child selector for multiple children
 * - Overview of selected child's academics
 * - Attendance summary
 * - Recent grades and assignments
 * - School announcements
 * - Quick contact options
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class ParentDashboard extends ScrollPane {

    // ========================================================================
    // STATE
    // ========================================================================

    private final List<ChildInfo> children = new ArrayList<>();
    private ChildInfo selectedChild;
    private String parentName = "Parent";

    // ========================================================================
    // WIDGETS
    // ========================================================================

    private ComboBox<ChildInfo> childSelector;
    private VBox childOverviewCard;

    private KpiWidget gpaKpi;
    private KpiWidget attendanceKpi;
    private KpiWidget assignmentsKpi;
    private KpiWidget absencesKpi;

    private QuickActionsWidget quickActions;
    private AlertsWidget schoolAlerts;

    private TableWidget<Map<String, String>> recentGrades;
    private TableWidget<Map<String, String>> upcomingAssignments;
    private DashboardWidget attendanceChart;

    // ========================================================================
    // CALLBACKS
    // ========================================================================

    private Consumer<String> onNavigate;
    private Consumer<String> onQuickAction;
    private Consumer<ChildInfo> onChildSelected;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public ParentDashboard() {
        initializeUI();
        loadDemoData();

        log.info("ParentDashboard initialized");
    }

    private void initializeUI() {
        VBox container = new VBox(24);
        container.setPadding(new Insets(24));
        container.setStyle("-fx-background-color: #F8FAFC;");

        // Header with child selector
        HBox headerBox = createHeader();

        // Child overview card
        childOverviewCard = createChildOverview();

        // KPI Row
        HBox kpiRow = createKpiRow();

        // Actions + Alerts Row
        HBox actionsRow = createActionsRow();

        // Academics Row
        HBox academicsRow = createAcademicsRow();

        container.getChildren().addAll(headerBox, childOverviewCard, kpiRow, actionsRow, academicsRow);

        setContent(container);
        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setStyle("-fx-background-color: #F8FAFC; -fx-border-width: 0;");
    }

    // ========================================================================
    // ROW BUILDERS
    // ========================================================================

    private HBox createHeader() {
        VBox titleBox = new VBox(4);

        Label welcomeLabel = new Label("Welcome, " + parentName);
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: #0F172A;");

        Label dateLabel = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748B;");

        titleBox.getChildren().addAll(welcomeLabel, dateLabel);

        // Child selector
        VBox selectorBox = new VBox(4);
        selectorBox.setAlignment(Pos.CENTER_RIGHT);

        Label selectorLabel = new Label("Viewing:");
        selectorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        childSelector = new ComboBox<>();
        childSelector.setPromptText("Select child");
        childSelector.setPrefWidth(200);
        childSelector.setStyle("""
            -fx-font-size: 14px;
            -fx-background-color: white;
            -fx-border-color: #E2E8F0;
            -fx-border-radius: 6;
            -fx-background-radius: 6;
            """);

        childSelector.setCellFactory(lv -> new ListCell<ChildInfo>() {
            @Override
            protected void updateItem(ChildInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.name + " - Grade " + item.grade);
                }
            }
        });

        childSelector.setButtonCell(new ListCell<ChildInfo>() {
            @Override
            protected void updateItem(ChildInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.name + " - Grade " + item.grade);
                }
            }
        });

        childSelector.setOnAction(e -> {
            selectedChild = childSelector.getValue();
            updateChildDisplay();
            if (onChildSelected != null && selectedChild != null) {
                onChildSelected.accept(selectedChild);
            }
        });

        selectorBox.getChildren().addAll(selectorLabel, childSelector);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(16, titleBox, spacer, selectorBox);
        header.setAlignment(Pos.CENTER_LEFT);

        return header;
    }

    private VBox createChildOverview() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle("""
            -fx-background-color: linear-gradient(to right, #2563EB, #3B82F6);
            -fx-background-radius: 12;
            """);

        HBox content = new HBox(20);
        content.setAlignment(Pos.CENTER_LEFT);

        // Avatar
        Label avatar = new Label("👤");
        avatar.setStyle("""
            -fx-font-size: 48px;
            -fx-background-color: white;
            -fx-background-radius: 50;
            -fx-padding: 12;
            """);

        // Info
        VBox info = new VBox(4);

        Label nameLabel = new Label("Select a child to view");
        nameLabel.setId("childNameLabel");
        nameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 600; -fx-text-fill: white;");

        Label detailsLabel = new Label("");
        detailsLabel.setId("childDetailsLabel");
        detailsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.9);");

        info.getChildren().addAll(nameLabel, detailsLabel);
        HBox.setHgrow(info, Priority.ALWAYS);

        // Quick stats
        VBox stats = new VBox(8);
        stats.setAlignment(Pos.CENTER_RIGHT);

        Label gpaLabel = new Label("GPA: --");
        gpaLabel.setId("overviewGpaLabel");
        gpaLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

        Label attendanceLabel = new Label("Attendance: --%");
        attendanceLabel.setId("overviewAttendanceLabel");
        attendanceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

        stats.getChildren().addAll(gpaLabel, attendanceLabel);

        content.getChildren().addAll(avatar, info, stats);
        card.getChildren().add(content);

        return card;
    }

    private HBox createKpiRow() {
        gpaKpi = new KpiWidget("Current GPA", 0);
        gpaKpi.setIcon("📊");
        gpaKpi.setFormat("%.2f");
        gpaKpi.setShowTrend(true);
        gpaKpi.setComparisonLabel("vs. last semester");

        attendanceKpi = new KpiWidget("Attendance", 0);
        attendanceKpi.setIcon("✓");
        attendanceKpi.setUnit("%");
        attendanceKpi.setFormat("%.1f");
        attendanceKpi.setShowTrend(false);
        attendanceKpi.setShowComparison(false);

        assignmentsKpi = new KpiWidget("Pending", 0);
        assignmentsKpi.setIcon("📝");
        assignmentsKpi.setShowTrend(false);
        assignmentsKpi.setComparisonLabel("assignments due");

        absencesKpi = new KpiWidget("Absences", 0);
        absencesKpi.setIcon("📅");
        absencesKpi.setShowTrend(false);
        absencesKpi.setComparisonLabel("this semester");

        return new HBox(16, gpaKpi, attendanceKpi, assignmentsKpi, absencesKpi);
    }

    private HBox createActionsRow() {
        quickActions = QuickActionsWidget.forParent();
        quickActions.setOnActionClick(action -> {
            if (onQuickAction != null) {
                onQuickAction.accept(action.id);
            }
        });

        schoolAlerts = new AlertsWidget();
        schoolAlerts.setTitle("School Updates");
        schoolAlerts.setIcon("🏫");

        HBox.setHgrow(quickActions, Priority.ALWAYS);
        HBox.setHgrow(schoolAlerts, Priority.ALWAYS);

        return new HBox(16, quickActions, schoolAlerts);
    }

    private HBox createAcademicsRow() {
        recentGrades = new TableWidget<>("Recent Grades");
        recentGrades.setIcon("📊");
        recentGrades.addColumn("Assignment", map -> map.get("assignment"));
        recentGrades.addColumn("Class", map -> map.get("class"));
        recentGrades.addColumn("Grade", map -> map.get("grade"));
        recentGrades.setOnViewAll(() -> {
            if (onNavigate != null) onNavigate.accept("grades");
        });

        upcomingAssignments = new TableWidget<>("Upcoming Assignments");
        upcomingAssignments.setIcon("📄");
        upcomingAssignments.setSubtitle("Due soon");
        upcomingAssignments.addColumn("Assignment", map -> map.get("title"));
        upcomingAssignments.addColumn("Class", map -> map.get("class"));
        upcomingAssignments.addColumn("Due", map -> map.get("due"));
        upcomingAssignments.setOnViewAll(() -> {
            if (onNavigate != null) onNavigate.accept("assignments");
        });

        HBox.setHgrow(recentGrades, Priority.ALWAYS);
        HBox.setHgrow(upcomingAssignments, Priority.ALWAYS);

        return new HBox(16, recentGrades, upcomingAssignments);
    }

    // ========================================================================
    // DATA UPDATES
    // ========================================================================

    private void updateChildDisplay() {
        if (selectedChild == null) return;

        // Update overview card
        Label nameLabel = (Label) childOverviewCard.lookup("#childNameLabel");
        Label detailsLabel = (Label) childOverviewCard.lookup("#childDetailsLabel");
        Label gpaLabel = (Label) childOverviewCard.lookup("#overviewGpaLabel");
        Label attendanceLabel = (Label) childOverviewCard.lookup("#overviewAttendanceLabel");

        if (nameLabel != null) {
            nameLabel.setText(selectedChild.name);
        }
        if (detailsLabel != null) {
            detailsLabel.setText(String.format("Grade %s • %s", selectedChild.grade, selectedChild.school));
        }
        if (gpaLabel != null) {
            gpaLabel.setText(String.format("GPA: %.2f", selectedChild.gpa));
        }
        if (attendanceLabel != null) {
            attendanceLabel.setText(String.format("Attendance: %.1f%%", selectedChild.attendance));
        }

        // Update KPIs
        gpaKpi.setValue(selectedChild.gpa, selectedChild.previousGpa);
        gpaKpi.setStatus(selectedChild.gpa >= 3.5 ? KpiWidget.StatusType.SUCCESS :
                        selectedChild.gpa >= 2.5 ? KpiWidget.StatusType.DEFAULT :
                        KpiWidget.StatusType.WARNING);

        attendanceKpi.setValue(selectedChild.attendance);
        attendanceKpi.setStatus(selectedChild.attendance >= 95 ? KpiWidget.StatusType.SUCCESS :
                               selectedChild.attendance >= 90 ? KpiWidget.StatusType.WARNING :
                               KpiWidget.StatusType.DANGER);

        assignmentsKpi.setValue(selectedChild.pendingAssignments);
        assignmentsKpi.setStatus(selectedChild.pendingAssignments > 5 ? KpiWidget.StatusType.WARNING :
                                KpiWidget.StatusType.DEFAULT);

        absencesKpi.setValue(selectedChild.absences);
        absencesKpi.setStatus(selectedChild.absences > 5 ? KpiWidget.StatusType.WARNING :
                             KpiWidget.StatusType.DEFAULT);

        // Update tables
        recentGrades.setData(selectedChild.grades);
        upcomingAssignments.setData(selectedChild.assignments);
    }

    // ========================================================================
    // DEMO DATA
    // ========================================================================

    private void loadDemoData() {
        // Create demo children
        ChildInfo child1 = new ChildInfo("Emma Johnson", "10", "Central High School");
        child1.gpa = 3.72;
        child1.previousGpa = 3.65;
        child1.attendance = 96.5;
        child1.pendingAssignments = 3;
        child1.absences = 2;
        child1.grades = Arrays.asList(
            Map.of("assignment", "Chapter 7 Test", "class", "Chemistry", "grade", "92%"),
            Map.of("assignment", "Essay Draft", "class", "English 11", "grade", "A-"),
            Map.of("assignment", "Quiz 5", "class", "Algebra II", "grade", "88%"),
            Map.of("assignment", "Lab Report", "class", "Chemistry", "grade", "95%")
        );
        child1.assignments = Arrays.asList(
            Map.of("title", "Book Report", "class", "English 11", "due", "Tomorrow"),
            Map.of("title", "Problem Set 8", "class", "Algebra II", "due", "Thu"),
            Map.of("title", "History Project", "class", "US History", "due", "Next Mon")
        );

        ChildInfo child2 = new ChildInfo("Liam Johnson", "7", "Central Middle School");
        child2.gpa = 3.45;
        child2.previousGpa = 3.50;
        child2.attendance = 94.2;
        child2.pendingAssignments = 5;
        child2.absences = 4;
        child2.grades = Arrays.asList(
            Map.of("assignment", "Math Test", "class", "Pre-Algebra", "grade", "85%"),
            Map.of("assignment", "Science Fair", "class", "Life Science", "grade", "A"),
            Map.of("assignment", "Spelling Test", "class", "English 7", "grade", "90%")
        );
        child2.assignments = Arrays.asList(
            Map.of("title", "Reading Log", "class", "English 7", "due", "Fri"),
            Map.of("title", "Math Homework", "class", "Pre-Algebra", "due", "Wed"),
            Map.of("title", "Science Report", "class", "Life Science", "due", "Thu"),
            Map.of("title", "Vocabulary", "class", "English 7", "due", "Fri"),
            Map.of("title", "Social Studies", "class", "World Geography", "due", "Next Mon")
        );

        children.clear();
        children.add(child1);
        children.add(child2);

        // Update selector
        childSelector.getItems().setAll(children);
        if (!children.isEmpty()) {
            childSelector.setValue(children.get(0));
            selectedChild = children.get(0);
            updateChildDisplay();
        }

        // Load alerts
        loadAlerts();
    }

    private void loadAlerts() {
        schoolAlerts.addAlert(new AlertsWidget.Alert("1", AlertsWidget.AlertType.INFO,
                "Parent-Teacher Conference", "Sign up for conferences is now open. Schedule your slot today!")
                .withAction("conferences"));

        schoolAlerts.addAlert(new AlertsWidget.Alert("2", AlertsWidget.AlertType.WARNING,
                "Early Dismissal", "School dismisses at 1:00 PM this Friday"));

        schoolAlerts.addAlert(new AlertsWidget.Alert("3", AlertsWidget.AlertType.INFO,
                "Winter Dance", "Winter formal dance tickets now on sale - $15 each"));
    }

    // ========================================================================
    // PUBLIC API
    // ========================================================================

    /**
     * Set parent name
     */
    public void setParentName(String name) {
        this.parentName = name;
    }

    /**
     * Set children list
     */
    public void setChildren(List<ChildInfo> childList) {
        children.clear();
        children.addAll(childList);
        childSelector.getItems().setAll(children);
        if (!children.isEmpty()) {
            childSelector.setValue(children.get(0));
            selectedChild = children.get(0);
            updateChildDisplay();
        }
    }

    /**
     * Refresh dashboard
     */
    public void refresh() {
        if (selectedChild != null) {
            updateChildDisplay();
        }
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
     * Set child selected callback
     */
    public void setOnChildSelected(Consumer<ChildInfo> callback) {
        this.onChildSelected = callback;
    }

    /**
     * Get currently selected child
     */
    public ChildInfo getSelectedChild() {
        return selectedChild;
    }

    // ========================================================================
    // CHILD INFO CLASS
    // ========================================================================

    @Getter
    public static class ChildInfo {
        private final String name;
        private final String grade;
        private final String school;
        private double gpa;
        private double previousGpa;
        private double attendance;
        private int pendingAssignments;
        private int absences;
        private List<Map<String, String>> grades = new ArrayList<>();
        private List<Map<String, String>> assignments = new ArrayList<>();

        public ChildInfo(String name, String grade, String school) {
            this.name = name;
            this.grade = grade;
            this.school = school;
        }

        @Override
        public String toString() {
            return name + " (Grade " + grade + ")";
        }
    }
}
