package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Chart Configuration DTO
 *
 * Comprehensive configuration for generating charts and visualizations.
 *
 * Supports Multiple Chart Types:
 * - Line charts (trends over time)
 * - Bar charts (categorical comparisons)
 * - Pie charts (proportional data)
 * - Area charts (cumulative trends)
 * - Scatter plots (correlation analysis)
 * - Mixed charts (combination of types)
 *
 * Features:
 * - Customizable colors and themes
 * - Multiple data series
 * - Legends and labels
 * - Interactive tooltips
 * - Export to PNG/SVG
 * - Responsive sizing
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 66 - Report Data Visualization & Charts
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartConfig {

    /**
     * Chart type enumeration
     */
    public enum ChartType {
        LINE,           // Line chart for trends
        BAR,            // Vertical bar chart
        HORIZONTAL_BAR, // Horizontal bar chart
        PIE,            // Pie chart
        DOUGHNUT,       // Doughnut chart
        AREA,           // Area chart (filled line)
        SCATTER,        // Scatter plot
        RADAR,          // Radar/spider chart
        POLAR,          // Polar area chart
        MIXED           // Mixed chart (multiple types)
    }

    /**
     * Chart theme/color scheme
     */
    public enum ChartTheme {
        DEFAULT,        // Standard colors
        PASTEL,         // Soft pastel colors
        VIBRANT,        // Bright vibrant colors
        MONOCHROME,     // Shades of gray
        EDUCATIONAL,    // School-friendly colors
        ACCESSIBLE,     // High contrast for accessibility
        CUSTOM          // User-defined colors
    }

    /**
     * Export format
     */
    public enum ExportFormat {
        PNG,            // Raster image
        SVG,            // Vector image
        PDF,            // PDF document
        JSON            // Raw data
    }

    // ============================================================
    // Basic Chart Information
    // ============================================================

    /**
     * Chart ID (for caching/reference)
     */
    private String chartId;

    /**
     * Chart type
     */
    private ChartType chartType;

    /**
     * Chart title
     */
    private String title;

    /**
     * Chart subtitle
     */
    private String subtitle;

    /**
     * Chart theme
     */
    private ChartTheme theme;

    /**
     * Custom color palette (hex colors)
     */
    private List<String> customColors;

    // ============================================================
    // Data Configuration
    // ============================================================

    /**
     * X-axis labels
     */
    private List<String> labels;

    /**
     * Data series
     */
    private List<ChartDataSeries> dataSeries;

    /**
     * X-axis title
     */
    private String xAxisLabel;

    /**
     * Y-axis title
     */
    private String yAxisLabel;

    // ============================================================
    // Display Options
    // ============================================================

    /**
     * Chart width in pixels
     */
    private Integer width;

    /**
     * Chart height in pixels
     */
    private Integer height;

    /**
     * Show legend
     */
    private Boolean showLegend;

    /**
     * Legend position (top, bottom, left, right)
     */
    private String legendPosition;

    /**
     * Show grid lines
     */
    private Boolean showGrid;

    /**
     * Show data labels on points/bars
     */
    private Boolean showDataLabels;

    /**
     * Enable animations
     */
    private Boolean enableAnimations;

    /**
     * Enable interactive tooltips
     */
    private Boolean enableTooltips;

    /**
     * Enable zoom/pan controls
     */
    private Boolean enableZoom;

    /**
     * Responsive sizing
     */
    private Boolean responsive;

    // ============================================================
    // Chart-Specific Options
    // ============================================================

    /**
     * For line/area charts: smooth curves vs straight lines
     */
    private Boolean smoothCurves;

    /**
     * For line charts: fill area under line
     */
    private Boolean fillArea;

    /**
     * For bar charts: stacked bars
     */
    private Boolean stacked;

    /**
     * For pie/doughnut: start angle in degrees
     */
    private Integer startAngle;

    /**
     * For pie/doughnut: show percentages
     */
    private Boolean showPercentages;

    /**
     * For scatter: point size
     */
    private Integer pointSize;

    /**
     * For all charts: minimum Y value
     */
    private Double minYValue;

    /**
     * For all charts: maximum Y value
     */
    private Double maxYValue;

    // ============================================================
    // Export Options
    // ============================================================

    /**
     * Export format
     */
    private ExportFormat exportFormat;

    /**
     * Background color for export (transparent if null)
     */
    private String backgroundColor;

    /**
     * Font family for export
     */
    private String fontFamily;

    /**
     * Font size for export
     */
    private Integer fontSize;

    // ============================================================
    // Metadata
    // ============================================================

    /**
     * Chart generation timestamp
     */
    private java.time.LocalDateTime generatedAt;

    /**
     * Generated by username
     */
    private String generatedBy;

    /**
     * Source report type
     */
    private String sourceReport;

    /**
     * Additional metadata
     */
    private Map<String, Object> metadata;

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Get color palette for theme
     */
    public List<String> getColorPalette() {
        if (theme == ChartTheme.CUSTOM && customColors != null && !customColors.isEmpty()) {
            return customColors;
        }

        return switch (theme != null ? theme : ChartTheme.DEFAULT) {
            case PASTEL -> List.of(
                    "#FFB3BA", "#FFDFBA", "#FFFFBA", "#BAFFC9", "#BAE1FF",
                    "#E0BBE4", "#FFDFD3", "#D4F1F4", "#FFE5B4", "#C7CEEA"
            );
            case VIBRANT -> List.of(
                    "#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A", "#98D8C8",
                    "#F7DC6F", "#BB8FCE", "#85C1E2", "#F8B739", "#52B788"
            );
            case MONOCHROME -> List.of(
                    "#212121", "#424242", "#616161", "#757575", "#9E9E9E",
                    "#BDBDBD", "#E0E0E0", "#EEEEEE", "#F5F5F5", "#FAFAFA"
            );
            case EDUCATIONAL -> List.of(
                    "#5470C6", "#91CC75", "#FAC858", "#EE6666", "#73C0DE",
                    "#3BA272", "#FC8452", "#9A60B4", "#EA7CCC", "#8BC34A"
            );
            case ACCESSIBLE -> List.of(
                    "#0077BB", "#33BBEE", "#009988", "#EE7733", "#CC3311",
                    "#EE3377", "#BBBBBB", "#000000", "#FFAA00", "#AA3377"
            );
            default -> List.of(
                    "#36A2EB", "#FF6384", "#FFCE56", "#4BC0C0", "#9966FF",
                    "#FF9F40", "#FF6384", "#C9CBCF", "#4BC0C0", "#FF9F40"
            );
        };
    }

    /**
     * Validate configuration
     */
    public void validate() {
        if (chartType == null) {
            throw new IllegalArgumentException("Chart type is required");
        }
        if (dataSeries == null || dataSeries.isEmpty()) {
            throw new IllegalArgumentException("At least one data series is required");
        }
        if (labels == null || labels.isEmpty()) {
            throw new IllegalArgumentException("Labels are required");
        }
    }

    /**
     * Chart Data Series
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartDataSeries {
        /**
         * Series name/label
         */
        private String label;

        /**
         * Data points
         */
        private List<Double> data;

        /**
         * Series color (hex)
         */
        private String color;

        /**
         * Chart type for this series (for mixed charts)
         */
        private ChartType type;

        /**
         * Fill area under line (for line/area charts)
         */
        private Boolean fill;

        /**
         * Line tension/smoothness (0-1, for line charts)
         */
        private Double tension;

        /**
         * Border width in pixels
         */
        private Integer borderWidth;

        /**
         * Point radius in pixels
         */
        private Integer pointRadius;

        /**
         * Show this series in legend
         */
        private Boolean showInLegend;

        /**
         * Y-axis ID (for charts with multiple Y axes)
         */
        private String yAxisId;
    }
}
