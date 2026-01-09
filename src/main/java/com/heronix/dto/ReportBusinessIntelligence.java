package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Business Intelligence DTO
 *
 * Represents advanced analytics and business intelligence for reports.
 *
 * Features:
 * - Data aggregation and grouping
 * - Statistical analysis
 * - Trend detection
 * - Predictive analytics
 * - KPI tracking
 * - Dimensional analysis
 * - Data mining
 *
 * Analytics Types:
 * - Descriptive analytics (what happened)
 * - Diagnostic analytics (why it happened)
 * - Predictive analytics (what will happen)
 * - Prescriptive analytics (what should be done)
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 82 - Report Analytics & Business Intelligence
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportBusinessIntelligence {

    /**
     * Analytics type enumeration
     */
    public enum AnalyticsType {
        DESCRIPTIVE,        // What happened
        DIAGNOSTIC,         // Why it happened
        PREDICTIVE,         // What will happen
        PRESCRIPTIVE,       // What should be done
        COGNITIVE,          // Self-learning analytics
        REAL_TIME,          // Real-time analytics
        BATCH,              // Batch analytics
        STREAMING           // Streaming analytics
    }

    /**
     * Aggregation function enumeration
     */
    public enum AggregationFunction {
        SUM,                // Sum of values
        AVG,                // Average
        MIN,                // Minimum
        MAX,                // Maximum
        COUNT,              // Count
        COUNT_DISTINCT,     // Count distinct
        MEDIAN,             // Median
        MODE,               // Mode
        STDDEV,             // Standard deviation
        VARIANCE,           // Variance
        PERCENTILE,         // Percentile
        FIRST,              // First value
        LAST                // Last value
    }

    /**
     * Trend direction enumeration
     */
    public enum TrendDirection {
        INCREASING,         // Upward trend
        DECREASING,         // Downward trend
        STABLE,             // Stable/flat
        VOLATILE,           // Highly variable
        SEASONAL,           // Seasonal pattern
        CYCLICAL,           // Cyclical pattern
        IRREGULAR,          // Irregular pattern
        UNKNOWN             // Unknown pattern
    }

    /**
     * Dimension type enumeration
     */
    public enum DimensionType {
        TIME,               // Time dimension
        GEOGRAPHY,          // Geographic dimension
        PRODUCT,            // Product dimension
        CUSTOMER,           // Customer dimension
        CATEGORY,           // Category dimension
        DEPARTMENT,         // Department dimension
        CHANNEL,            // Channel dimension
        CUSTOM              // Custom dimension
    }

    /**
     * Chart type enumeration
     */
    public enum ChartType {
        LINE,               // Line chart
        BAR,                // Bar chart
        PIE,                // Pie chart
        SCATTER,            // Scatter plot
        AREA,               // Area chart
        BUBBLE,             // Bubble chart
        HEATMAP,            // Heat map
        TREEMAP,            // Tree map
        FUNNEL,             // Funnel chart
        GAUGE,              // Gauge chart
        RADAR,              // Radar chart
        CANDLESTICK,        // Candlestick chart
        WATERFALL,          // Waterfall chart
        SANKEY              // Sankey diagram
    }

    /**
     * Time granularity enumeration
     */
    public enum TimeGranularity {
        SECOND,             // By second
        MINUTE,             // By minute
        HOUR,               // By hour
        DAY,                // By day
        WEEK,               // By week
        MONTH,              // By month
        QUARTER,            // By quarter
        YEAR,               // By year
        CUSTOM              // Custom period
    }

    // ============================================================
    // Basic Information
    // ============================================================

    /**
     * BI ID
     */
    private Long biId;

    /**
     * Name
     */
    private String name;

    /**
     * Description
     */
    private String description;

    /**
     * Analytics type
     */
    private AnalyticsType analyticsType;

    /**
     * Report ID
     */
    private Long reportId;

    /**
     * Report name
     */
    private String reportName;

    /**
     * Created at
     */
    private LocalDateTime createdAt;

    /**
     * Created by
     */
    private String createdBy;

    /**
     * Updated at
     */
    private LocalDateTime updatedAt;

    /**
     * Last run at
     */
    private LocalDateTime lastRunAt;

    // ============================================================
    // Data Source Configuration
    // ============================================================

    /**
     * Data source
     */
    private String dataSource;

    /**
     * Dataset name
     */
    private String datasetName;

    /**
     * Query
     */
    private String query;

    /**
     * Filters
     */
    private Map<String, Object> filters;

    /**
     * Date range start
     */
    private LocalDateTime dateRangeStart;

    /**
     * Date range end
     */
    private LocalDateTime dateRangeEnd;

    /**
     * Time granularity
     */
    private TimeGranularity timeGranularity;

    /**
     * Sample size
     */
    private Integer sampleSize;

    /**
     * Total records
     */
    private Long totalRecords;

    /**
     * Filtered records
     */
    private Long filteredRecords;

    // ============================================================
    // Aggregation & Grouping
    // ============================================================

    /**
     * Group by fields
     */
    private List<String> groupByFields;

    /**
     * Aggregations
     */
    private List<Aggregation> aggregations;

    /**
     * Having conditions
     */
    private Map<String, Object> havingConditions;

    /**
     * Sort fields
     */
    private List<SortField> sortFields;

    /**
     * Limit
     */
    private Integer limit;

    /**
     * Offset
     */
    private Integer offset;

    // ============================================================
    // Statistical Analysis
    // ============================================================

    /**
     * Statistics
     */
    private Statistics statistics;

    /**
     * Correlation matrix
     */
    private Map<String, Map<String, Double>> correlationMatrix;

    /**
     * Outliers
     */
    private List<Outlier> outliers;

    /**
     * Distribution
     */
    private Distribution distribution;

    /**
     * Confidence level
     */
    private Double confidenceLevel;

    /**
     * P-value
     */
    private Double pValue;

    // ============================================================
    // Trend Analysis
    // ============================================================

    /**
     * Trend direction
     */
    private TrendDirection trendDirection;

    /**
     * Trend strength
     */
    private Double trendStrength;

    /**
     * Trend line
     */
    private TrendLine trendLine;

    /**
     * Seasonality detected
     */
    private Boolean seasonalityDetected;

    /**
     * Seasonal period
     */
    private Integer seasonalPeriod;

    /**
     * Change rate (percentage)
     */
    private Double changeRate;

    /**
     * Year over year growth
     */
    private Double yoyGrowth;

    /**
     * Month over month growth
     */
    private Double momGrowth;

    // ============================================================
    // Predictive Analytics
    // ============================================================

    /**
     * Predictions enabled
     */
    private Boolean predictionsEnabled;

    /**
     * Prediction model
     */
    private String predictionModel;

    /**
     * Predictions
     */
    private List<Prediction> predictions;

    /**
     * Forecast accuracy
     */
    private Double forecastAccuracy;

    /**
     * Forecast horizon (days)
     */
    private Integer forecastHorizonDays;

    /**
     * Confidence interval
     */
    private ConfidenceInterval confidenceInterval;

    // ============================================================
    // KPI Tracking
    // ============================================================

    /**
     * KPIs
     */
    private List<KPI> kpis;

    /**
     * KPI targets met
     */
    private Integer kpiTargetsMet;

    /**
     * Total KPIs
     */
    private Integer totalKpis;

    /**
     * KPI achievement rate (percentage)
     */
    private Double kpiAchievementRate;

    // ============================================================
    // Dimensional Analysis
    // ============================================================

    /**
     * Dimensions
     */
    private List<Dimension> dimensions;

    /**
     * Drill-down levels
     */
    private Integer drillDownLevels;

    /**
     * Current drill level
     */
    private Integer currentDrillLevel;

    /**
     * Drill path
     */
    private List<String> drillPath;

    /**
     * Slice and dice enabled
     */
    private Boolean sliceAndDiceEnabled;

    // ============================================================
    // Visualization
    // ============================================================

    /**
     * Chart type
     */
    private ChartType chartType;

    /**
     * Chart data
     */
    private ChartData chartData;

    /**
     * Dashboard enabled
     */
    private Boolean dashboardEnabled;

    /**
     * Dashboard widgets
     */
    private List<DashboardWidget> dashboardWidgets;

    /**
     * Interactive
     */
    private Boolean interactive;

    /**
     * Export formats
     */
    private List<String> exportFormats;

    // ============================================================
    // Performance Metrics
    // ============================================================

    /**
     * Execution time (ms)
     */
    private Long executionTimeMs;

    /**
     * Data processing time (ms)
     */
    private Long dataProcessingTimeMs;

    /**
     * Rendering time (ms)
     */
    private Long renderingTimeMs;

    /**
     * Memory usage (MB)
     */
    private Long memoryUsageMB;

    /**
     * Cache hit
     */
    private Boolean cacheHit;

    // ============================================================
    // Data Quality
    // ============================================================

    /**
     * Data quality score
     */
    private Double dataQualityScore;

    /**
     * Completeness (percentage)
     */
    private Double completeness;

    /**
     * Accuracy (percentage)
     */
    private Double accuracy;

    /**
     * Missing values count
     */
    private Long missingValuesCount;

    /**
     * Duplicate records count
     */
    private Long duplicateRecordsCount;

    /**
     * Data freshness (minutes)
     */
    private Long dataFreshnessMinutes;

    // ============================================================
    // Alerts & Thresholds
    // ============================================================

    /**
     * Alerts enabled
     */
    private Boolean alertsEnabled;

    /**
     * Threshold rules
     */
    private List<ThresholdRule> thresholdRules;

    /**
     * Active alerts
     */
    private Integer activeAlerts;

    /**
     * Alert history
     */
    private List<BIAlert> alertHistory;

    // ============================================================
    // Metadata
    // ============================================================

    /**
     * Tags
     */
    private List<String> tags;

    /**
     * Category
     */
    private String category;

    /**
     * Owner
     */
    private String owner;

    /**
     * Shared with
     */
    private List<String> sharedWith;

    /**
     * Custom attributes
     */
    private Map<String, Object> customAttributes;

    /**
     * Notes
     */
    private String notes;

    // ============================================================
    // Nested Classes
    // ============================================================

    /**
     * Aggregation
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Aggregation {
        private String fieldName;
        private AggregationFunction function;
        private String alias;
        private Object value;
        private Map<String, Object> parameters;
    }

    /**
     * Sort field
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SortField {
        private String fieldName;
        private String direction; // ASC, DESC
        private Integer priority;
    }

    /**
     * Statistics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Statistics {
        private Double mean;
        private Double median;
        private Double mode;
        private Double standardDeviation;
        private Double variance;
        private Double min;
        private Double max;
        private Double range;
        private Double sum;
        private Long count;
        private Double q1; // First quartile
        private Double q3; // Third quartile
        private Double iqr; // Interquartile range
        private Double skewness;
        private Double kurtosis;
    }

    /**
     * Outlier
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Outlier {
        private String fieldName;
        private Object value;
        private Double zScore;
        private String severity; // MILD, MODERATE, EXTREME
        private LocalDateTime timestamp;
        private Map<String, Object> context;
    }

    /**
     * Distribution
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Distribution {
        private String type; // NORMAL, UNIFORM, EXPONENTIAL, etc.
        private Map<String, Double> parameters;
        private List<Bin> histogram;
        private Double goodnessOfFit;
    }

    /**
     * Histogram bin
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Bin {
        private Double lowerBound;
        private Double upperBound;
        private Long count;
        private Double frequency;
    }

    /**
     * Trend line
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendLine {
        private String equation;
        private Double slope;
        private Double intercept;
        private Double rSquared;
        private List<DataPoint> points;
    }

    /**
     * Data point
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataPoint {
        private LocalDateTime timestamp;
        private Double x;
        private Double y;
        private String label;
        private Map<String, Object> metadata;
    }

    /**
     * Prediction
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Prediction {
        private LocalDateTime timestamp;
        private Double predictedValue;
        private Double lowerBound;
        private Double upperBound;
        private Double confidence;
        private Map<String, Object> features;
    }

    /**
     * Confidence interval
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfidenceInterval {
        private Double level; // e.g., 0.95 for 95%
        private Double lowerBound;
        private Double upperBound;
        private Double marginOfError;
    }

    /**
     * KPI
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KPI {
        private String kpiId;
        private String name;
        private String description;
        private Double currentValue;
        private Double targetValue;
        private String unit;
        private String targetType; // MINIMUM, MAXIMUM, EXACT, RANGE
        private Double achievementRate;
        private String status; // ON_TARGET, AT_RISK, OFF_TARGET
        private TrendDirection trend;
        private LocalDateTime measuredAt;
        private Map<String, Object> metadata;
    }

    /**
     * Dimension
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Dimension {
        private String dimensionId;
        private String name;
        private DimensionType type;
        private List<String> hierarchy;
        private Integer level;
        private List<DimensionMember> members;
        private Map<String, Object> attributes;
    }

    /**
     * Dimension member
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DimensionMember {
        private String memberId;
        private String name;
        private String parentId;
        private Integer level;
        private Object value;
        private Map<String, Object> attributes;
    }

    /**
     * Chart data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartData {
        private List<String> labels;
        private List<Series> series;
        private Map<String, Object> options;
        private String title;
        private String xAxisLabel;
        private String yAxisLabel;
    }

    /**
     * Chart series
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Series {
        private String name;
        private List<Object> data;
        private String color;
        private String type;
        private Map<String, Object> options;
    }

    /**
     * Dashboard widget
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardWidget {
        private String widgetId;
        private String title;
        private String type; // CHART, TABLE, METRIC, GAUGE, etc.
        private Integer row;
        private Integer column;
        private Integer width;
        private Integer height;
        private Map<String, Object> config;
        private Object data;
    }

    /**
     * Threshold rule
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThresholdRule {
        private String ruleId;
        private String name;
        private String metric;
        private String operator; // GT, LT, GTE, LTE, EQ, NEQ, BETWEEN
        private Double threshold;
        private Double upperThreshold; // For BETWEEN
        private String severity; // INFO, WARNING, CRITICAL
        private String action; // ALERT, EMAIL, SMS, WEBHOOK
        private Boolean enabled;
    }

    /**
     * BI alert
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BIAlert {
        private String alertId;
        private String ruleId;
        private String ruleName;
        private String metric;
        private Double value;
        private Double threshold;
        private String severity;
        private LocalDateTime triggeredAt;
        private Boolean acknowledged;
        private LocalDateTime acknowledgedAt;
        private String acknowledgedBy;
        private Map<String, Object> context;
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Calculate KPI achievement rate
     */
    public void calculateKPIAchievementRate() {
        if (totalKpis != null && totalKpis > 0 && kpiTargetsMet != null) {
            kpiAchievementRate = (kpiTargetsMet.doubleValue() / totalKpis) * 100.0;
        }
    }

    /**
     * Calculate data quality score
     */
    public void calculateDataQualityScore() {
        double score = 0.0;
        int factors = 0;

        if (completeness != null) {
            score += completeness;
            factors++;
        }

        if (accuracy != null) {
            score += accuracy;
            factors++;
        }

        // Penalize for missing values
        if (totalRecords != null && totalRecords > 0 && missingValuesCount != null) {
            double missingRate = (missingValuesCount.doubleValue() / totalRecords) * 100.0;
            score += (100.0 - missingRate);
            factors++;
        }

        // Penalize for duplicates
        if (totalRecords != null && totalRecords > 0 && duplicateRecordsCount != null) {
            double duplicateRate = (duplicateRecordsCount.doubleValue() / totalRecords) * 100.0;
            score += (100.0 - duplicateRate);
            factors++;
        }

        if (factors > 0) {
            dataQualityScore = score / factors;
        }
    }

    /**
     * Check if data is fresh
     */
    public boolean isDataFresh(long maxAgeMinutes) {
        if (dataFreshnessMinutes == null) {
            return false;
        }
        return dataFreshnessMinutes <= maxAgeMinutes;
    }

    /**
     * Check if trend is positive
     */
    public boolean hasFavorableTrend() {
        return trendDirection == TrendDirection.INCREASING ||
               trendDirection == TrendDirection.STABLE;
    }

    /**
     * Get total execution time
     */
    public Long getTotalExecutionTime() {
        long total = 0;
        if (dataProcessingTimeMs != null) total += dataProcessingTimeMs;
        if (renderingTimeMs != null) total += renderingTimeMs;
        return total > 0 ? total : executionTimeMs;
    }

    /**
     * Check if predictions are available
     */
    public boolean hasPredictions() {
        return Boolean.TRUE.equals(predictionsEnabled) &&
               predictions != null && !predictions.isEmpty();
    }

    /**
     * Check if outliers detected
     */
    public boolean hasOutliers() {
        return outliers != null && !outliers.isEmpty();
    }

    /**
     * Get severe outliers count
     */
    public long getSevereOutliersCount() {
        if (outliers == null) {
            return 0;
        }
        return outliers.stream()
                .filter(o -> "EXTREME".equals(o.getSeverity()))
                .count();
    }

    /**
     * Check if seasonality exists
     */
    public boolean hasSeasonality() {
        return Boolean.TRUE.equals(seasonalityDetected) &&
               seasonalPeriod != null && seasonalPeriod > 0;
    }

    /**
     * Get filter coverage percentage
     */
    public Double getFilterCoveragePercentage() {
        if (totalRecords == null || totalRecords == 0 || filteredRecords == null) {
            return null;
        }
        return (filteredRecords.doubleValue() / totalRecords) * 100.0;
    }
}
