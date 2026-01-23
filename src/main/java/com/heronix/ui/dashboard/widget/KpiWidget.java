package com.heronix.ui.dashboard.widget;

import com.heronix.ui.dashboard.DashboardWidget;
import javafx.animation.*;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * KPI (Key Performance Indicator) Widget
 * Displays a single metric with trend indicator and comparison.
 *
 * Features:
 * - Large value display with animated counting
 * - Trend arrow (up/down/neutral)
 * - Comparison to previous period
 * - Compact or detailed mode
 * - Color-coded status
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class KpiWidget extends DashboardWidget {

    // ========================================================================
    // PROPERTIES
    // ========================================================================

    private final DoubleProperty value = new SimpleDoubleProperty(0);
    private final DoubleProperty previousValue = new SimpleDoubleProperty(0);
    private final StringProperty unit = new SimpleStringProperty("");
    private final StringProperty format = new SimpleStringProperty("%.0f");
    private final ObjectProperty<TrendType> trend = new SimpleObjectProperty<>(TrendType.NEUTRAL);
    private final ObjectProperty<StatusType> status = new SimpleObjectProperty<>(StatusType.DEFAULT);
    private final BooleanProperty showTrend = new SimpleBooleanProperty(true);
    private final BooleanProperty showComparison = new SimpleBooleanProperty(true);
    private final BooleanProperty animateValue = new SimpleBooleanProperty(true);

    // ========================================================================
    // COMPONENTS
    // ========================================================================

    private final Label valueLabel;
    private final Label unitLabel;
    private final HBox trendBox;
    private final Label trendIcon;
    private final Label trendLabel;
    private final Label comparisonLabel;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public KpiWidget() {
        this("", 0);
    }

    public KpiWidget(String title, double value) {
        super(title);

        setSize(WidgetSize.SMALL);
        setRefreshable(true);
        setCollapsible(false);

        // Value display
        valueLabel = new Label("0");
        valueLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: 700;");

        unitLabel = new Label();
        unitLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748B; -fx-padding: 0 0 0 4;");
        unitLabel.textProperty().bind(unit);
        unitLabel.managedProperty().bind(unit.isNotEmpty());
        unitLabel.visibleProperty().bind(unit.isNotEmpty());

        HBox valueBox = new HBox(4, valueLabel, unitLabel);
        valueBox.setAlignment(Pos.BASELINE_LEFT);

        // Trend indicator
        trendIcon = new Label();
        trendIcon.setStyle("-fx-font-size: 14px;");

        trendLabel = new Label();
        trendLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 500;");

        trendBox = new HBox(4, trendIcon, trendLabel);
        trendBox.setAlignment(Pos.CENTER_LEFT);
        trendBox.managedProperty().bind(showTrend);
        trendBox.visibleProperty().bind(showTrend);

        // Comparison text
        comparisonLabel = new Label("vs. last period");
        comparisonLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8;");
        comparisonLabel.managedProperty().bind(showComparison);
        comparisonLabel.visibleProperty().bind(showComparison);

        // Layout
        VBox content = new VBox(8, valueBox, trendBox, comparisonLabel);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(8));

        setContent(content);

        // Listeners
        this.value.addListener((obs, oldVal, newVal) -> {
            if (animateValue.get()) {
                animateValueChange(oldVal.doubleValue(), newVal.doubleValue());
            } else {
                updateValueDisplay(newVal.doubleValue());
            }
            calculateTrend();
        });

        previousValue.addListener((obs, oldVal, newVal) -> calculateTrend());
        trend.addListener((obs, oldVal, newVal) -> updateTrendDisplay());
        status.addListener((obs, oldVal, newVal) -> updateStatusColors());

        // Set initial value
        setValue(value);

        log.debug("KpiWidget created: {}", title);
    }

    // ========================================================================
    // PUBLIC API
    // ========================================================================

    /**
     * Set the KPI value with animation
     */
    public void setValue(double newValue) {
        value.set(newValue);
    }

    /**
     * Set value with previous for trend calculation
     */
    public void setValue(double newValue, double previous) {
        previousValue.set(previous);
        value.set(newValue);
    }

    /**
     * Update with percentage change display
     */
    public void setValueWithChange(double newValue, double percentChange) {
        value.set(newValue);

        if (percentChange > 0) {
            trend.set(TrendType.UP);
            trendLabel.setText(String.format("+%.1f%%", percentChange));
        } else if (percentChange < 0) {
            trend.set(TrendType.DOWN);
            trendLabel.setText(String.format("%.1f%%", percentChange));
        } else {
            trend.set(TrendType.NEUTRAL);
            trendLabel.setText("0%");
        }
    }

    /**
     * Factory method for enrollment count
     */
    public static KpiWidget enrollment(int count, int previousCount) {
        KpiWidget widget = new KpiWidget("Total Enrollment", count);
        widget.setIcon("👥");
        widget.setPreviousValue(previousCount);
        widget.setComparisonLabel("vs. last year");
        return widget;
    }

    /**
     * Factory method for attendance rate
     */
    public static KpiWidget attendance(double rate, double previousRate) {
        KpiWidget widget = new KpiWidget("Attendance Rate", rate);
        widget.setIcon("✓");
        widget.setUnit("%");
        widget.setFormat("%.1f");
        widget.setPreviousValue(previousRate);
        widget.setStatus(rate >= 95 ? StatusType.SUCCESS : rate >= 90 ? StatusType.WARNING : StatusType.DANGER);
        return widget;
    }

    /**
     * Factory method for GPA average
     */
    public static KpiWidget gpa(double gpa, double previousGpa) {
        KpiWidget widget = new KpiWidget("Average GPA", gpa);
        widget.setIcon("📊");
        widget.setFormat("%.2f");
        widget.setPreviousValue(previousGpa);
        return widget;
    }

    /**
     * Factory method for graduation rate
     */
    public static KpiWidget graduationRate(double rate) {
        KpiWidget widget = new KpiWidget("Graduation Rate", rate);
        widget.setIcon("🎓");
        widget.setUnit("%");
        widget.setFormat("%.1f");
        widget.setShowTrend(false);
        widget.setStatus(rate >= 90 ? StatusType.SUCCESS : rate >= 80 ? StatusType.WARNING : StatusType.DANGER);
        return widget;
    }

    // ========================================================================
    // PROPERTY ACCESSORS
    // ========================================================================

    public double getValue() { return value.get(); }
    public DoubleProperty valueProperty() { return value; }

    public double getPreviousValue() { return previousValue.get(); }
    public void setPreviousValue(double value) { previousValue.set(value); }
    public DoubleProperty previousValueProperty() { return previousValue; }

    public String getUnit() { return unit.get(); }
    public void setUnit(String value) { unit.set(value); }
    public StringProperty unitProperty() { return unit; }

    public String getFormat() { return format.get(); }
    public void setFormat(String value) { format.set(value); }
    public StringProperty formatProperty() { return format; }

    public TrendType getTrend() { return trend.get(); }
    public void setTrend(TrendType value) { trend.set(value); }
    public ObjectProperty<TrendType> trendProperty() { return trend; }

    public StatusType getStatus() { return status.get(); }
    public void setStatus(StatusType value) { status.set(value); }
    public ObjectProperty<StatusType> statusProperty() { return status; }

    public boolean isShowTrend() { return showTrend.get(); }
    public void setShowTrend(boolean value) { showTrend.set(value); }
    public BooleanProperty showTrendProperty() { return showTrend; }

    public boolean isShowComparison() { return showComparison.get(); }
    public void setShowComparison(boolean value) { showComparison.set(value); }
    public BooleanProperty showComparisonProperty() { return showComparison; }

    public void setComparisonLabel(String text) { comparisonLabel.setText(text); }

    public boolean isAnimateValue() { return animateValue.get(); }
    public void setAnimateValue(boolean value) { animateValue.set(value); }
    public BooleanProperty animateValueProperty() { return animateValue; }

    // ========================================================================
    // INTERNAL METHODS
    // ========================================================================

    private void animateValueChange(double from, double to) {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, e -> updateValueDisplay(from)),
            new KeyFrame(Duration.millis(500), e -> updateValueDisplay(to))
        );

        // Animate through intermediate values
        int steps = 20;
        for (int i = 1; i < steps; i++) {
            final double progress = (double) i / steps;
            final double interpolated = from + (to - from) * progress;
            timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(25 * i), e -> updateValueDisplay(interpolated))
            );
        }

        timeline.play();
    }

    private void updateValueDisplay(double val) {
        valueLabel.setText(String.format(format.get(), val));
    }

    private void calculateTrend() {
        double current = value.get();
        double previous = previousValue.get();

        if (previous == 0) {
            trend.set(TrendType.NEUTRAL);
            return;
        }

        double change = ((current - previous) / previous) * 100;

        if (change > 0.1) {
            trend.set(TrendType.UP);
            trendLabel.setText(String.format("+%.1f%%", change));
        } else if (change < -0.1) {
            trend.set(TrendType.DOWN);
            trendLabel.setText(String.format("%.1f%%", change));
        } else {
            trend.set(TrendType.NEUTRAL);
            trendLabel.setText("0%");
        }
    }

    private void updateTrendDisplay() {
        TrendType t = trend.get();
        switch (t) {
            case UP:
                trendIcon.setText("↑");
                trendIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: #10B981;");
                trendLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: #10B981;");
                break;
            case DOWN:
                trendIcon.setText("↓");
                trendIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: #EF4444;");
                trendLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: #EF4444;");
                break;
            case NEUTRAL:
            default:
                trendIcon.setText("→");
                trendIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748B;");
                trendLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: #64748B;");
                break;
        }
    }

    private void updateStatusColors() {
        StatusType s = status.get();
        String color = switch (s) {
            case SUCCESS -> "#10B981";
            case WARNING -> "#F59E0B";
            case DANGER -> "#EF4444";
            case INFO -> "#3B82F6";
            default -> "#0F172A";
        };
        valueLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: 700; -fx-text-fill: " + color + ";");
    }

    // ========================================================================
    // ENUMS
    // ========================================================================

    public enum TrendType {
        UP, DOWN, NEUTRAL
    }

    public enum StatusType {
        DEFAULT, SUCCESS, WARNING, DANGER, INFO
    }
}
