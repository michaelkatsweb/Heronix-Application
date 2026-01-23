package com.heronix.ui.reports;

import javafx.animation.*;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

/**
 * Data Visualization Dashboard
 * Interactive charts and analytics for school data.
 *
 * Features:
 * - Multiple chart types (line, bar, pie, area)
 * - Interactive drill-down
 * - Real-time data updates
 * - Export visualizations
 * - Customizable widgets
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class DataVisualizationDashboard extends BorderPane {

    // ========================================================================
    // DATA
    // ========================================================================

    private final ObservableList<DashboardWidget> widgets = FXCollections.observableArrayList();
    private final StringProperty selectedTimeRange = new SimpleStringProperty("This Month");
    private final StringProperty selectedSchoolYear = new SimpleStringProperty("2025-2026");

    // ========================================================================
    // COMPONENTS
    // ========================================================================

    private FlowPane widgetContainer;
    private VBox sidebar;

    // ========================================================================
    // CALLBACKS
    // ========================================================================

    private Consumer<ChartDataPoint> onDataPointClicked;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public DataVisualizationDashboard() {
        getStyleClass().add("data-visualization");
        setStyle("-fx-background-color: #F1F5F9;");

        // Header
        setTop(createHeader());

        // Main content
        HBox content = new HBox(0);

        // Widget area
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        HBox.setHgrow(scroll, Priority.ALWAYS);

        widgetContainer = new FlowPane(20, 20);
        widgetContainer.setPadding(new Insets(20));
        scroll.setContent(widgetContainer);

        // Sidebar
        sidebar = createSidebar();

        content.getChildren().addAll(scroll, sidebar);
        setCenter(content);

        // Load widgets
        loadDefaultWidgets();
    }

    // ========================================================================
    // HEADER
    // ========================================================================

    private HBox createHeader() {
        HBox header = new HBox(16);
        header.setPadding(new Insets(16, 24, 16, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        // Title
        VBox titleBox = new VBox(2);
        Label title = new Label("Analytics Dashboard");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #1E293B;");
        Label subtitle = new Label("Real-time insights and visualizations");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B;");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Filters
        ComboBox<String> yearCombo = new ComboBox<>();
        yearCombo.getItems().addAll("2025-2026", "2024-2025", "2023-2024");
        yearCombo.valueProperty().bindBidirectional(selectedSchoolYear);
        yearCombo.setStyle("-fx-font-size: 13px;");

        ComboBox<String> timeCombo = new ComboBox<>();
        timeCombo.getItems().addAll("Today", "This Week", "This Month", "This Quarter", "This Year");
        timeCombo.valueProperty().bindBidirectional(selectedTimeRange);
        timeCombo.setStyle("-fx-font-size: 13px;");

        Button refreshBtn = new Button("⟳ Refresh");
        refreshBtn.setStyle("""
            -fx-background-color: #F1F5F9;
            -fx-text-fill: #374151;
            -fx-font-size: 13px;
            -fx-padding: 10 16;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            """);
        refreshBtn.setOnAction(e -> refreshAllWidgets());

        Button addWidgetBtn = new Button("+ Add Widget");
        addWidgetBtn.setStyle("""
            -fx-background-color: #3B82F6;
            -fx-text-fill: white;
            -fx-font-size: 13px;
            -fx-font-weight: 600;
            -fx-padding: 10 16;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            """);

        header.getChildren().addAll(titleBox, spacer, yearCombo, timeCombo, refreshBtn, addWidgetBtn);
        return header;
    }

    // ========================================================================
    // SIDEBAR
    // ========================================================================

    private VBox createSidebar() {
        VBox sidebar = new VBox(16);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(280);
        sidebar.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 0 1;");

        // Quick stats
        Label statsLabel = new Label("QUICK STATS");
        statsLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #64748B;");

        VBox statsCards = new VBox(12);
        statsCards.getChildren().addAll(
            createQuickStatCard("Total Students", "1,247", "+23", true),
            createQuickStatCard("Attendance Rate", "94.7%", "+1.2%", true),
            createQuickStatCard("Average GPA", "3.24", "-0.03", false),
            createQuickStatCard("Graduation Rate", "96.8%", "+0.5%", true)
        );

        // Recent activity
        Label activityLabel = new Label("RECENT ACTIVITY");
        activityLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #64748B;");
        VBox.setMargin(activityLabel, new Insets(16, 0, 0, 0));

        VBox activityList = new VBox(8);
        activityList.getChildren().addAll(
            createActivityItem("Report generated", "Student Roster", "2 min ago"),
            createActivityItem("Data exported", "Attendance Report", "15 min ago"),
            createActivityItem("Dashboard shared", "Grade Analytics", "1 hour ago"),
            createActivityItem("Widget added", "GPA Trends", "3 hours ago")
        );

        sidebar.getChildren().addAll(statsLabel, statsCards, activityLabel, activityList);
        return sidebar;
    }

    private VBox createQuickStatCard(String label, String value, String change, boolean positive) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(12));
        card.setStyle("""
            -fx-background-color: #F8FAFC;
            -fx-background-radius: 8;
            """);

        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

        HBox valueRow = new HBox(8);
        valueRow.setAlignment(Pos.CENTER_LEFT);

        Label valueText = new Label(value);
        valueText.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #1E293B;");

        Label changeText = new Label((positive ? "↑ " : "↓ ") + change);
        changeText.setStyle(String.format("""
            -fx-font-size: 11px;
            -fx-text-fill: %s;
            -fx-background-color: %s;
            -fx-padding: 2 6;
            -fx-background-radius: 4;
            """,
            positive ? "#059669" : "#DC2626",
            positive ? "#ECFDF5" : "#FEF2F2"
        ));

        valueRow.getChildren().addAll(valueText, changeText);
        card.getChildren().addAll(labelText, valueRow);
        return card;
    }

    private HBox createActivityItem(String action, String target, String time) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(8));
        item.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 6;");

        VBox textBox = new VBox(2);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Label actionLabel = new Label(action);
        actionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");

        Label targetLabel = new Label(target);
        targetLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

        textBox.getChildren().addAll(actionLabel, targetLabel);

        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #94A3B8;");

        item.getChildren().addAll(textBox, timeLabel);
        return item;
    }

    // ========================================================================
    // WIDGETS
    // ========================================================================

    private void loadDefaultWidgets() {
        // Enrollment trend chart
        widgetContainer.getChildren().add(createEnrollmentTrendWidget());

        // Grade distribution pie chart
        widgetContainer.getChildren().add(createGradeDistributionWidget());

        // Attendance heatmap
        widgetContainer.getChildren().add(createAttendanceHeatmapWidget());

        // GPA comparison bar chart
        widgetContainer.getChildren().add(createGpaComparisonWidget());

        // Course enrollment
        widgetContainer.getChildren().add(createCourseEnrollmentWidget());

        // Demographics donut
        widgetContainer.getChildren().add(createDemographicsWidget());
    }

    private VBox createWidgetContainer(String title, String subtitle, double width, double height) {
        VBox widget = new VBox(0);
        widget.setPrefSize(width, height);
        widget.setMaxSize(width, height);
        widget.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 12;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);
            """);

        // Header
        HBox header = new HBox(8);
        header.setPadding(new Insets(16, 16, 12, 16));
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #1E293B;");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

        titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        MenuButton menuBtn = new MenuButton("⋯");
        menuBtn.setStyle("""
            -fx-background-color: transparent;
            -fx-font-size: 14px;
            -fx-text-fill: #94A3B8;
            """);
        menuBtn.getItems().addAll(
            new MenuItem("📥 Export as PNG"),
            new MenuItem("📊 Export Data"),
            new MenuItem("🔗 Share"),
            new SeparatorMenuItem(),
            new MenuItem("⚙ Configure"),
            new MenuItem("🗑 Remove")
        );

        header.getChildren().addAll(titleBox, menuBtn);
        widget.getChildren().add(header);

        return widget;
    }

    private VBox createEnrollmentTrendWidget() {
        VBox widget = createWidgetContainer("Enrollment Trends", "Student count over time", 580, 350);

        // Line chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(1000, 1300, 50);
        xAxis.setLabel("Month");
        yAxis.setLabel("Students");

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setLegendVisible(true);
        chart.setAnimated(true);
        chart.setCreateSymbols(true);
        chart.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(chart, Priority.ALWAYS);

        XYChart.Series<String, Number> currentYear = new XYChart.Series<>();
        currentYear.setName("2025-26");
        currentYear.getData().addAll(
            new XYChart.Data<>("Aug", 1180),
            new XYChart.Data<>("Sep", 1205),
            new XYChart.Data<>("Oct", 1218),
            new XYChart.Data<>("Nov", 1225),
            new XYChart.Data<>("Dec", 1230),
            new XYChart.Data<>("Jan", 1247)
        );

        XYChart.Series<String, Number> lastYear = new XYChart.Series<>();
        lastYear.setName("2024-25");
        lastYear.getData().addAll(
            new XYChart.Data<>("Aug", 1150),
            new XYChart.Data<>("Sep", 1165),
            new XYChart.Data<>("Oct", 1178),
            new XYChart.Data<>("Nov", 1185),
            new XYChart.Data<>("Dec", 1190),
            new XYChart.Data<>("Jan", 1198)
        );

        chart.getData().addAll(currentYear, lastYear);

        widget.getChildren().add(chart);
        return widget;
    }

    private VBox createGradeDistributionWidget() {
        VBox widget = createWidgetContainer("Grade Distribution", "Current semester grades", 380, 350);

        // Pie chart
        PieChart chart = new PieChart();
        chart.setLegendVisible(true);
        chart.setLabelsVisible(true);
        chart.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(chart, Priority.ALWAYS);

        chart.getData().addAll(
            new PieChart.Data("A (28%)", 28),
            new PieChart.Data("B (35%)", 35),
            new PieChart.Data("C (22%)", 22),
            new PieChart.Data("D (10%)", 10),
            new PieChart.Data("F (5%)", 5)
        );

        // Add colors
        String[] colors = {"#22C55E", "#3B82F6", "#F59E0B", "#F97316", "#EF4444"};
        int i = 0;
        for (PieChart.Data data : chart.getData()) {
            data.getNode().setStyle("-fx-pie-color: " + colors[i++] + ";");
        }

        widget.getChildren().add(chart);
        return widget;
    }

    private VBox createAttendanceHeatmapWidget() {
        VBox widget = createWidgetContainer("Attendance Heatmap", "Weekly attendance patterns", 580, 300);

        // Heatmap grid
        GridPane grid = new GridPane();
        grid.setHgap(4);
        grid.setVgap(4);
        grid.setPadding(new Insets(16));
        grid.setAlignment(Pos.CENTER);
        VBox.setVgrow(grid, Priority.ALWAYS);

        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri"};
        String[] periods = {"P1", "P2", "P3", "P4", "P5", "P6", "P7"};

        // Header row
        for (int j = 0; j < days.length; j++) {
            Label dayLabel = new Label(days[j]);
            dayLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 600; -fx-text-fill: #64748B;");
            dayLabel.setPrefWidth(60);
            dayLabel.setAlignment(Pos.CENTER);
            grid.add(dayLabel, j + 1, 0);
        }

        // Period labels and cells
        Random rand = new Random(42);
        for (int i = 0; i < periods.length; i++) {
            Label periodLabel = new Label(periods[i]);
            periodLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 600; -fx-text-fill: #64748B;");
            periodLabel.setPrefWidth(40);
            grid.add(periodLabel, 0, i + 1);

            for (int j = 0; j < days.length; j++) {
                double rate = 0.85 + rand.nextDouble() * 0.15;
                StackPane cell = createHeatmapCell(rate);
                grid.add(cell, j + 1, i + 1);
            }
        }

        // Legend
        HBox legend = new HBox(16);
        legend.setAlignment(Pos.CENTER);
        legend.setPadding(new Insets(8, 16, 16, 16));

        legend.getChildren().addAll(
            createLegendItem("#FEE2E2", "< 90%"),
            createLegendItem("#FEF3C7", "90-95%"),
            createLegendItem("#D1FAE5", "> 95%")
        );

        widget.getChildren().addAll(grid, legend);
        return widget;
    }

    private StackPane createHeatmapCell(double rate) {
        StackPane cell = new StackPane();
        cell.setPrefSize(60, 36);

        String color;
        if (rate < 0.90) {
            color = "#FEE2E2";
        } else if (rate < 0.95) {
            color = "#FEF3C7";
        } else {
            color = "#D1FAE5";
        }

        cell.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 4;
            """, color));

        Label label = new Label(String.format("%.0f%%", rate * 100));
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: 600; -fx-text-fill: #374151;");

        cell.getChildren().add(label);

        // Hover tooltip
        Tooltip tooltip = new Tooltip(String.format("Attendance Rate: %.1f%%", rate * 100));
        Tooltip.install(cell, tooltip);

        return cell;
    }

    private HBox createLegendItem(String color, String label) {
        HBox item = new HBox(6);
        item.setAlignment(Pos.CENTER_LEFT);

        Region colorBox = new Region();
        colorBox.setPrefSize(16, 16);
        colorBox.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 4;");

        Label text = new Label(label);
        text.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

        item.getChildren().addAll(colorBox, text);
        return item;
    }

    private VBox createGpaComparisonWidget() {
        VBox widget = createWidgetContainer("GPA by Grade Level", "Average GPA comparison", 380, 300);

        // Bar chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, 4, 0.5);
        yAxis.setLabel("GPA");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(true);
        chart.setCategoryGap(20);
        chart.setBarGap(4);
        chart.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(chart, Priority.ALWAYS);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().addAll(
            new XYChart.Data<>("9th", 3.15),
            new XYChart.Data<>("10th", 3.22),
            new XYChart.Data<>("11th", 3.31),
            new XYChart.Data<>("12th", 3.28)
        );

        chart.getData().add(series);

        // Style bars
        for (XYChart.Data<String, Number> data : series.getData()) {
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-bar-fill: #3B82F6;");
                }
            });
        }

        widget.getChildren().add(chart);
        return widget;
    }

    private VBox createCourseEnrollmentWidget() {
        VBox widget = createWidgetContainer("Top Courses by Enrollment", "Most popular courses", 480, 350);

        // Horizontal bar list
        VBox barList = new VBox(12);
        barList.setPadding(new Insets(16));
        VBox.setVgrow(barList, Priority.ALWAYS);

        String[][] courses = {
            {"English 101", "245", "100"},
            {"Algebra II", "198", "81"},
            {"US History", "187", "76"},
            {"Biology", "176", "72"},
            {"Spanish I", "165", "67"},
            {"Chemistry", "142", "58"}
        };

        for (String[] course : courses) {
            barList.getChildren().add(createHorizontalBarItem(course[0],
                Integer.parseInt(course[1]), Integer.parseInt(course[2])));
        }

        widget.getChildren().add(barList);
        return widget;
    }

    private HBox createHorizontalBarItem(String label, int value, int percentage) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");
        nameLabel.setMinWidth(100);

        // Progress bar
        StackPane barContainer = new StackPane();
        barContainer.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(barContainer, Priority.ALWAYS);

        Region bgBar = new Region();
        bgBar.setPrefHeight(24);
        bgBar.setMaxWidth(Double.MAX_VALUE);
        bgBar.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 4;");

        Region valueBar = new Region();
        valueBar.setPrefHeight(24);
        valueBar.setMaxWidth(percentage * 2.5);
        valueBar.setStyle("-fx-background-color: #3B82F6; -fx-background-radius: 4;");

        barContainer.getChildren().addAll(bgBar, valueBar);
        StackPane.setAlignment(valueBar, Pos.CENTER_LEFT);

        Label valueLabel = new Label(String.valueOf(value));
        valueLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: #1E293B;");
        valueLabel.setMinWidth(40);
        valueLabel.setAlignment(Pos.CENTER_RIGHT);

        item.getChildren().addAll(nameLabel, barContainer, valueLabel);
        return item;
    }

    private VBox createDemographicsWidget() {
        VBox widget = createWidgetContainer("Student Demographics", "Enrollment by grade level", 380, 350);

        // Donut chart simulation with stacked panes
        StackPane chartArea = new StackPane();
        chartArea.setPadding(new Insets(16));
        VBox.setVgrow(chartArea, Priority.ALWAYS);

        // Center text
        VBox centerText = new VBox(2);
        centerText.setAlignment(Pos.CENTER);

        Label totalLabel = new Label("1,247");
        totalLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 700; -fx-text-fill: #1E293B;");

        Label totalSubLabel = new Label("Total Students");
        totalSubLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

        centerText.getChildren().addAll(totalLabel, totalSubLabel);

        // Pie chart as donut
        PieChart chart = new PieChart();
        chart.setLegendVisible(false);
        chart.setLabelsVisible(false);
        chart.setStartAngle(90);

        chart.getData().addAll(
            new PieChart.Data("9th Grade", 342),
            new PieChart.Data("10th Grade", 318),
            new PieChart.Data("11th Grade", 298),
            new PieChart.Data("12th Grade", 289)
        );

        String[] colors = {"#3B82F6", "#10B981", "#F59E0B", "#8B5CF6"};
        int i = 0;
        for (PieChart.Data data : chart.getData()) {
            final int idx = i;
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-pie-color: " + colors[idx] + ";");
                }
            });
            i++;
        }

        chartArea.getChildren().addAll(chart, centerText);

        // Legend
        HBox legend = new HBox(16);
        legend.setAlignment(Pos.CENTER);
        legend.setPadding(new Insets(0, 16, 16, 16));

        String[] grades = {"9th", "10th", "11th", "12th"};
        String[] counts = {"342", "318", "298", "289"};
        for (int j = 0; j < grades.length; j++) {
            legend.getChildren().add(createDonutLegendItem(colors[j], grades[j], counts[j]));
        }

        widget.getChildren().addAll(chartArea, legend);
        return widget;
    }

    private VBox createDonutLegendItem(String color, String label, String value) {
        VBox item = new VBox(2);
        item.setAlignment(Pos.CENTER);

        HBox labelRow = new HBox(4);
        labelRow.setAlignment(Pos.CENTER);

        Region dot = new Region();
        dot.setPrefSize(8, 8);
        dot.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 4;");

        Label text = new Label(label);
        text.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

        labelRow.getChildren().addAll(dot, text);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #1E293B;");

        item.getChildren().addAll(labelRow, valueLabel);
        return item;
    }

    // ========================================================================
    // ACTIONS
    // ========================================================================

    private void refreshAllWidgets() {
        // Animate refresh
        for (var node : widgetContainer.getChildren()) {
            FadeTransition fade = new FadeTransition(Duration.millis(200), node);
            fade.setFromValue(1.0);
            fade.setToValue(0.5);
            fade.setAutoReverse(true);
            fade.setCycleCount(2);
            fade.play();
        }
        log.info("Refreshing dashboard widgets");
    }

    // ========================================================================
    // CALLBACKS
    // ========================================================================

    public void setOnDataPointClicked(Consumer<ChartDataPoint> callback) {
        this.onDataPointClicked = callback;
    }

    // ========================================================================
    // DATA CLASSES
    // ========================================================================

    @Getter @Setter
    public static class DashboardWidget {
        private String id;
        private String title;
        private String type;
        private int width;
        private int height;
        private Map<String, Object> config;
    }

    @Getter @Setter
    public static class ChartDataPoint {
        private String category;
        private String series;
        private Number value;
        private Map<String, Object> metadata;
    }
}
