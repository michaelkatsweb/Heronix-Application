package com.heronix.ui.dashboard.widget;

import com.heronix.ui.dashboard.DashboardWidget;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.chart.*;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * Chart Widget
 * Displays various chart types within a dashboard widget.
 *
 * Supported chart types:
 * - Line chart (trends over time)
 * - Bar chart (comparisons)
 * - Pie chart (distributions)
 * - Area chart (cumulative trends)
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class ChartWidget extends DashboardWidget {

    // ========================================================================
    // CHART TYPES
    // ========================================================================

    public enum ChartType {
        LINE, BAR, PIE, AREA, STACKED_BAR
    }

    private Chart chart;
    private final ChartType chartType;

    // ========================================================================
    // CONSTRUCTORS
    // ========================================================================

    public ChartWidget(String title, ChartType type) {
        super(title);
        this.chartType = type;
        setSize(WidgetSize.LARGE);
        initializeChart();
    }

    private void initializeChart() {
        switch (chartType) {
            case LINE -> createLineChart();
            case BAR -> createBarChart();
            case PIE -> createPieChart();
            case AREA -> createAreaChart();
            case STACKED_BAR -> createStackedBarChart();
        }
    }

    // ========================================================================
    // LINE CHART
    // ========================================================================

    private void createLineChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("");
        yAxis.setLabel("");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setLegendSide(Side.BOTTOM);
        lineChart.setAnimated(true);
        lineChart.setCreateSymbols(true);

        applyChartStyle(lineChart);
        this.chart = lineChart;
        setContent(lineChart);
    }

    /**
     * Set line chart data with single series
     */
    public void setLineData(String seriesName, Map<String, Number> data) {
        if (!(chart instanceof LineChart)) return;

        @SuppressWarnings("unchecked")
        LineChart<String, Number> lineChart = (LineChart<String, Number>) chart;
        lineChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(seriesName);
        data.forEach((key, value) -> series.getData().add(new XYChart.Data<>(key, value)));

        lineChart.getData().add(series);
    }

    /**
     * Set line chart data with multiple series
     */
    public void setLineData(Map<String, Map<String, Number>> seriesData) {
        if (!(chart instanceof LineChart)) return;

        @SuppressWarnings("unchecked")
        LineChart<String, Number> lineChart = (LineChart<String, Number>) chart;
        lineChart.getData().clear();

        seriesData.forEach((seriesName, data) -> {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(seriesName);
            data.forEach((key, value) -> series.getData().add(new XYChart.Data<>(key, value)));
            lineChart.getData().add(series);
        });
    }

    // ========================================================================
    // BAR CHART
    // ========================================================================

    private void createBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setLegendSide(Side.BOTTOM);
        barChart.setAnimated(true);
        barChart.setCategoryGap(20);
        barChart.setBarGap(3);

        applyChartStyle(barChart);
        this.chart = barChart;
        setContent(barChart);
    }

    /**
     * Set bar chart data
     */
    public void setBarData(String seriesName, Map<String, Number> data) {
        if (!(chart instanceof BarChart)) return;

        @SuppressWarnings("unchecked")
        BarChart<String, Number> barChart = (BarChart<String, Number>) chart;
        barChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(seriesName);
        data.forEach((key, value) -> series.getData().add(new XYChart.Data<>(key, value)));

        barChart.getData().add(series);
    }

    /**
     * Set bar chart with multiple series (grouped bars)
     */
    public void setBarData(Map<String, Map<String, Number>> seriesData) {
        if (!(chart instanceof BarChart)) return;

        @SuppressWarnings("unchecked")
        BarChart<String, Number> barChart = (BarChart<String, Number>) chart;
        barChart.getData().clear();

        seriesData.forEach((seriesName, data) -> {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(seriesName);
            data.forEach((key, value) -> series.getData().add(new XYChart.Data<>(key, value)));
            barChart.getData().add(series);
        });
    }

    // ========================================================================
    // STACKED BAR CHART
    // ========================================================================

    private void createStackedBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        StackedBarChart<String, Number> stackedBarChart = new StackedBarChart<>(xAxis, yAxis);
        stackedBarChart.setLegendSide(Side.BOTTOM);
        stackedBarChart.setAnimated(true);

        applyChartStyle(stackedBarChart);
        this.chart = stackedBarChart;
        setContent(stackedBarChart);
    }

    /**
     * Set stacked bar chart data
     */
    public void setStackedBarData(Map<String, Map<String, Number>> seriesData) {
        if (!(chart instanceof StackedBarChart)) return;

        @SuppressWarnings("unchecked")
        StackedBarChart<String, Number> stackedBarChart = (StackedBarChart<String, Number>) chart;
        stackedBarChart.getData().clear();

        seriesData.forEach((seriesName, data) -> {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(seriesName);
            data.forEach((key, value) -> series.getData().add(new XYChart.Data<>(key, value)));
            stackedBarChart.getData().add(series);
        });
    }

    // ========================================================================
    // PIE CHART
    // ========================================================================

    private void createPieChart() {
        PieChart pieChart = new PieChart();
        pieChart.setLegendSide(Side.RIGHT);
        pieChart.setAnimated(true);
        pieChart.setLabelsVisible(true);
        pieChart.setLabelLineLength(10);

        applyChartStyle(pieChart);
        this.chart = pieChart;
        setContent(pieChart);
    }

    /**
     * Set pie chart data
     */
    public void setPieData(Map<String, Number> data) {
        if (!(chart instanceof PieChart)) return;

        PieChart pieChart = (PieChart) chart;
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        data.forEach((key, value) -> pieData.add(new PieChart.Data(key, value.doubleValue())));

        pieChart.setData(pieData);
    }

    // ========================================================================
    // AREA CHART
    // ========================================================================

    private void createAreaChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        AreaChart<String, Number> areaChart = new AreaChart<>(xAxis, yAxis);
        areaChart.setLegendSide(Side.BOTTOM);
        areaChart.setAnimated(true);
        areaChart.setCreateSymbols(false);

        applyChartStyle(areaChart);
        this.chart = areaChart;
        setContent(areaChart);
    }

    /**
     * Set area chart data
     */
    public void setAreaData(String seriesName, Map<String, Number> data) {
        if (!(chart instanceof AreaChart)) return;

        @SuppressWarnings("unchecked")
        AreaChart<String, Number> areaChart = (AreaChart<String, Number>) chart;
        areaChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(seriesName);
        data.forEach((key, value) -> series.getData().add(new XYChart.Data<>(key, value)));

        areaChart.getData().add(series);
    }

    // ========================================================================
    // STYLING
    // ========================================================================

    private void applyChartStyle(Chart chart) {
        chart.setStyle("""
            -fx-background-color: transparent;
            -fx-padding: 10;
            """);

        if (chart instanceof XYChart) {
            ((XYChart<?, ?>) chart).getXAxis().setStyle("-fx-tick-label-fill: #64748B; -fx-font-size: 11px;");
            ((XYChart<?, ?>) chart).getYAxis().setStyle("-fx-tick-label-fill: #64748B; -fx-font-size: 11px;");
        }

        VBox.setMargin(chart, new Insets(0));
    }

    // ========================================================================
    // FACTORY METHODS
    // ========================================================================

    /**
     * Create an enrollment trend chart
     */
    public static ChartWidget enrollmentTrend() {
        ChartWidget widget = new ChartWidget("Enrollment Trend", ChartType.LINE);
        widget.setIcon("📈");
        widget.setSubtitle("Last 12 months");
        return widget;
    }

    /**
     * Create a grade distribution chart
     */
    public static ChartWidget gradeDistribution() {
        ChartWidget widget = new ChartWidget("Grade Distribution", ChartType.BAR);
        widget.setIcon("📊");
        widget.setSubtitle("Current semester");
        return widget;
    }

    /**
     * Create a student demographics chart
     */
    public static ChartWidget demographics() {
        ChartWidget widget = new ChartWidget("Student Demographics", ChartType.PIE);
        widget.setIcon("🥧");
        return widget;
    }

    /**
     * Create an attendance trend chart
     */
    public static ChartWidget attendanceTrend() {
        ChartWidget widget = new ChartWidget("Attendance Trend", ChartType.AREA);
        widget.setIcon("✓");
        widget.setSubtitle("Weekly average");
        return widget;
    }

    /**
     * Create a course enrollment chart
     */
    public static ChartWidget courseEnrollment() {
        ChartWidget widget = new ChartWidget("Course Enrollment", ChartType.STACKED_BAR);
        widget.setIcon("📚");
        widget.setSubtitle("By department");
        return widget;
    }

    // ========================================================================
    // ACCESSORS
    // ========================================================================

    public Chart getChart() {
        return chart;
    }

    public ChartType getChartType() {
        return chartType;
    }
}
