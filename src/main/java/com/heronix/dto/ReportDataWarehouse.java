package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Data Warehouse DTO
 *
 * Represents data warehouse and ETL configuration for reports.
 *
 * Features:
 * - Extract, Transform, Load (ETL) processes
 * - Data integration from multiple sources
 * - Data cleansing and validation
 * - Dimensional modeling (star/snowflake schema)
 * - Slowly Changing Dimensions (SCD)
 * - Data quality management
 * - Incremental and full data loads
 *
 * ETL Process:
 * - Extract: Pull data from various sources
 * - Transform: Clean, validate, and transform data
 * - Load: Load data into warehouse
 * - Validate: Ensure data quality
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 83 - Report Data Warehouse & ETL
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDataWarehouse {

    /**
     * ETL job type enumeration
     */
    public enum ETLJobType {
        FULL_LOAD,          // Full data load
        INCREMENTAL,        // Incremental load
        DELTA,              // Delta/change-based load
        REAL_TIME,          // Real-time streaming
        SCHEDULED,          // Scheduled batch
        ON_DEMAND,          // On-demand execution
        MERGE,              // Merge/upsert
        REFRESH             // Refresh materialized views
    }

    /**
     * ETL status enumeration
     */
    public enum ETLStatus {
        PENDING,            // Waiting to start
        EXTRACTING,         // Extracting data
        TRANSFORMING,       // Transforming data
        LOADING,            // Loading data
        VALIDATING,         // Validating data
        COMPLETED,          // Completed successfully
        FAILED,             // Failed
        CANCELLED,          // Cancelled
        PARTIAL             // Partially completed
    }

    /**
     * Data source type enumeration
     */
    public enum DataSourceType {
        DATABASE,           // Relational database
        FILE,               // File (CSV, Excel, etc.)
        API,                // REST/SOAP API
        CLOUD_STORAGE,      // Cloud storage (S3, Azure Blob, etc.)
        STREAMING,          // Streaming (Kafka, etc.)
        DATA_LAKE,          // Data lake
        WAREHOUSE,          // Another data warehouse
        CUSTOM              // Custom source
    }

    /**
     * Transformation type enumeration
     */
    public enum TransformationType {
        FILTER,             // Filter rows
        MAP,                // Map/transform columns
        AGGREGATE,          // Aggregate data
        JOIN,               // Join tables
        UNION,              // Union tables
        SPLIT,              // Split data
        PIVOT,              // Pivot data
        UNPIVOT,            // Unpivot data
        DEDUPLICATE,        // Remove duplicates
        CLEAN,              // Clean data
        VALIDATE,           // Validate data
        ENRICH,             // Enrich with additional data
        CUSTOM              // Custom transformation
    }

    /**
     * SCD type enumeration
     */
    public enum SCDType {
        TYPE_0,             // Retain original (no changes)
        TYPE_1,             // Overwrite
        TYPE_2,             // Add new row with versioning
        TYPE_3,             // Add new column
        TYPE_4,             // Add history table
        TYPE_6              // Hybrid (1+2+3)
    }

    /**
     * Load strategy enumeration
     */
    public enum LoadStrategy {
        INSERT,             // Insert new records
        UPDATE,             // Update existing records
        UPSERT,             // Insert or update
        DELETE,             // Delete records
        TRUNCATE_INSERT,    // Truncate and insert
        MERGE,              // Merge data
        APPEND,             // Append to existing
        REPLACE             // Replace all data
    }

    // ============================================================
    // Basic Information
    // ============================================================

    /**
     * Warehouse ID
     */
    private Long warehouseId;

    /**
     * Warehouse name
     */
    private String name;

    /**
     * Description
     */
    private String description;

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
    // ETL Configuration
    // ============================================================

    /**
     * ETL job type
     */
    private ETLJobType jobType;

    /**
     * ETL status
     */
    private ETLStatus status;

    /**
     * Enabled
     */
    private Boolean enabled;

    /**
     * Schedule expression (cron)
     */
    private String scheduleExpression;

    /**
     * Next run time
     */
    private LocalDateTime nextRunTime;

    /**
     * Timeout (seconds)
     */
    private Integer timeoutSeconds;

    /**
     * Retry on failure
     */
    private Boolean retryOnFailure;

    /**
     * Max retry attempts
     */
    private Integer maxRetryAttempts;

    /**
     * Retry count
     */
    private Integer retryCount;

    // ============================================================
    // Data Sources
    // ============================================================

    /**
     * Data sources
     */
    private List<DataSource> dataSources;

    /**
     * Primary source
     */
    private String primarySource;

    /**
     * Source connection timeout (seconds)
     */
    private Integer sourceConnectionTimeout;

    // ============================================================
    // Extraction
    // ============================================================

    /**
     * Extract query
     */
    private String extractQuery;

    /**
     * Extract filter
     */
    private String extractFilter;

    /**
     * Extract batch size
     */
    private Integer extractBatchSize;

    /**
     * Incremental field
     */
    private String incrementalField;

    /**
     * Last incremental value
     */
    private String lastIncrementalValue;

    /**
     * Extract start time
     */
    private LocalDateTime extractStartTime;

    /**
     * Extract end time
     */
    private LocalDateTime extractEndTime;

    /**
     * Extract duration (ms)
     */
    private Long extractDurationMs;

    /**
     * Extracted rows
     */
    private Long extractedRows;

    // ============================================================
    // Transformation
    // ============================================================

    /**
     * Transformations
     */
    private List<Transformation> transformations;

    /**
     * Data cleansing enabled
     */
    private Boolean dataCleansingEnabled;

    /**
     * Data validation enabled
     */
    private Boolean dataValidationEnabled;

    /**
     * Validation rules
     */
    private List<ValidationRule> validationRules;

    /**
     * Transform start time
     */
    private LocalDateTime transformStartTime;

    /**
     * Transform end time
     */
    private LocalDateTime transformEndTime;

    /**
     * Transform duration (ms)
     */
    private Long transformDurationMs;

    /**
     * Transformed rows
     */
    private Long transformedRows;

    /**
     * Rejected rows
     */
    private Long rejectedRows;

    // ============================================================
    // Loading
    // ============================================================

    /**
     * Target table
     */
    private String targetTable;

    /**
     * Target schema
     */
    private String targetSchema;

    /**
     * Load strategy
     */
    private LoadStrategy loadStrategy;

    /**
     * Bulk load enabled
     */
    private Boolean bulkLoadEnabled;

    /**
     * Load batch size
     */
    private Integer loadBatchSize;

    /**
     * Load start time
     */
    private LocalDateTime loadStartTime;

    /**
     * Load end time
     */
    private LocalDateTime loadEndTime;

    /**
     * Load duration (ms)
     */
    private Long loadDurationMs;

    /**
     * Loaded rows
     */
    private Long loadedRows;

    /**
     * Updated rows
     */
    private Long updatedRows;

    /**
     * Inserted rows
     */
    private Long insertedRows;

    /**
     * Deleted rows
     */
    private Long deletedRows;

    // ============================================================
    // Dimensional Modeling
    // ============================================================

    /**
     * Schema type (STAR, SNOWFLAKE, VAULT)
     */
    private String schemaType;

    /**
     * Fact tables
     */
    private List<String> factTables;

    /**
     * Dimension tables
     */
    private List<DimensionTable> dimensionTables;

    /**
     * SCD type
     */
    private SCDType scdType;

    /**
     * Surrogate key enabled
     */
    private Boolean surrogateKeyEnabled;

    /**
     * Version tracking enabled
     */
    private Boolean versionTrackingEnabled;

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
     * Consistency (percentage)
     */
    private Double consistency;

    /**
     * Validity (percentage)
     */
    private Double validity;

    /**
     * Duplicate records
     */
    private Long duplicateRecords;

    /**
     * Null values
     */
    private Long nullValues;

    /**
     * Invalid records
     */
    private Long invalidRecords;

    /**
     * Data quality issues
     */
    private List<DataQualityIssue> dataQualityIssues;

    // ============================================================
    // Performance Metrics
    // ============================================================

    /**
     * Total duration (ms)
     */
    private Long totalDurationMs;

    /**
     * Records per second
     */
    private Double recordsPerSecond;

    /**
     * Data volume (MB)
     */
    private Double dataVolumeMB;

    /**
     * Peak memory usage (MB)
     */
    private Long peakMemoryUsageMB;

    /**
     * CPU usage (percentage)
     */
    private Double cpuUsagePercent;

    // ============================================================
    // Execution Statistics
    // ============================================================

    /**
     * Total executions
     */
    private Long totalExecutions;

    /**
     * Successful executions
     */
    private Long successfulExecutions;

    /**
     * Failed executions
     */
    private Long failedExecutions;

    /**
     * Success rate (percentage)
     */
    private Double successRate;

    /**
     * Average duration (ms)
     */
    private Double averageDurationMs;

    /**
     * Last success time
     */
    private LocalDateTime lastSuccessTime;

    /**
     * Last failure time
     */
    private LocalDateTime lastFailureTime;

    /**
     * Last error message
     */
    private String lastErrorMessage;

    // ============================================================
    // Lineage & Auditing
    // ============================================================

    /**
     * Data lineage enabled
     */
    private Boolean dataLineageEnabled;

    /**
     * Lineage information
     */
    private List<LineageNode> lineageNodes;

    /**
     * Audit logging enabled
     */
    private Boolean auditLoggingEnabled;

    /**
     * Audit logs
     */
    private List<AuditLog> auditLogs;

    // ============================================================
    // Metadata
    // ============================================================

    /**
     * Tags
     */
    private List<String> tags;

    /**
     * Owner
     */
    private String owner;

    /**
     * Custom properties
     */
    private Map<String, Object> customProperties;

    /**
     * Notes
     */
    private String notes;

    // ============================================================
    // Nested Classes
    // ============================================================

    /**
     * Data source
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataSource {
        private String sourceId;
        private String sourceName;
        private DataSourceType sourceType;
        private String connectionString;
        private String username;
        private String password; // Encrypted
        private Map<String, String> properties;
        private String query;
        private Boolean enabled;
        private LocalDateTime lastConnected;
    }

    /**
     * Transformation
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Transformation {
        private String transformId;
        private String name;
        private TransformationType type;
        private Integer sequence;
        private String expression;
        private Map<String, Object> parameters;
        private Boolean enabled;
        private Long recordsProcessed;
        private Long recordsRejected;
    }

    /**
     * Validation rule
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationRule {
        private String ruleId;
        private String name;
        private String fieldName;
        private String ruleType; // NOT_NULL, RANGE, REGEX, CUSTOM
        private String expression;
        private String errorMessage;
        private String severity; // ERROR, WARNING, INFO
        private Boolean enabled;
        private Long violationCount;
    }

    /**
     * Dimension table
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DimensionTable {
        private String tableName;
        private String description;
        private List<String> attributes;
        private String keyColumn;
        private SCDType scdType;
        private Boolean enabled;
        private Long recordCount;
    }

    /**
     * Data quality issue
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataQualityIssue {
        private String issueId;
        private String issueType;
        private String severity;
        private String description;
        private String fieldName;
        private Long affectedRecords;
        private LocalDateTime detectedAt;
        private Boolean resolved;
        private LocalDateTime resolvedAt;
    }

    /**
     * Lineage node
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineageNode {
        private String nodeId;
        private String nodeName;
        private String nodeType; // SOURCE, TRANSFORMATION, TARGET
        private String parentNodeId;
        private Map<String, Object> metadata;
        private LocalDateTime timestamp;
    }

    /**
     * Audit log
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuditLog {
        private String logId;
        private LocalDateTime timestamp;
        private String action;
        private String username;
        private String details;
        private String severity;
        private Map<String, Object> metadata;
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Calculate total duration
     */
    public void calculateTotalDuration() {
        long total = 0;
        if (extractDurationMs != null) total += extractDurationMs;
        if (transformDurationMs != null) total += transformDurationMs;
        if (loadDurationMs != null) total += loadDurationMs;
        totalDurationMs = total;
    }

    /**
     * Calculate records per second
     */
    public void calculateRecordsPerSecond() {
        if (totalDurationMs != null && totalDurationMs > 0 && loadedRows != null) {
            recordsPerSecond = (loadedRows * 1000.0) / totalDurationMs;
        }
    }

    /**
     * Calculate success rate
     */
    public void calculateSuccessRate() {
        if (totalExecutions != null && totalExecutions > 0 && successfulExecutions != null) {
            successRate = (successfulExecutions.doubleValue() / totalExecutions) * 100.0;
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

        if (consistency != null) {
            score += consistency;
            factors++;
        }

        if (validity != null) {
            score += validity;
            factors++;
        }

        if (factors > 0) {
            dataQualityScore = score / factors;
        }
    }

    /**
     * Check if ETL is running
     */
    public boolean isRunning() {
        return status == ETLStatus.EXTRACTING ||
               status == ETLStatus.TRANSFORMING ||
               status == ETLStatus.LOADING ||
               status == ETLStatus.VALIDATING;
    }

    /**
     * Check if ETL is completed
     */
    public boolean isCompleted() {
        return status == ETLStatus.COMPLETED;
    }

    /**
     * Check if ETL failed
     */
    public boolean isFailed() {
        return status == ETLStatus.FAILED;
    }

    /**
     * Start extraction
     */
    public void startExtraction() {
        status = ETLStatus.EXTRACTING;
        extractStartTime = LocalDateTime.now();
    }

    /**
     * Complete extraction
     */
    public void completeExtraction(long rowsExtracted) {
        extractEndTime = LocalDateTime.now();
        extractedRows = rowsExtracted;
        if (extractStartTime != null) {
            extractDurationMs = java.time.Duration.between(extractStartTime, extractEndTime).toMillis();
        }
    }

    /**
     * Start transformation
     */
    public void startTransformation() {
        status = ETLStatus.TRANSFORMING;
        transformStartTime = LocalDateTime.now();
    }

    /**
     * Complete transformation
     */
    public void completeTransformation(long rowsTransformed, long rowsRejected) {
        transformEndTime = LocalDateTime.now();
        transformedRows = rowsTransformed;
        rejectedRows = rowsRejected;
        if (transformStartTime != null) {
            transformDurationMs = java.time.Duration.between(transformStartTime, transformEndTime).toMillis();
        }
    }

    /**
     * Start loading
     */
    public void startLoading() {
        status = ETLStatus.LOADING;
        loadStartTime = LocalDateTime.now();
    }

    /**
     * Complete loading
     */
    public void completeLoading(long rowsLoaded, long rowsInserted, long rowsUpdated, long rowsDeleted) {
        loadEndTime = LocalDateTime.now();
        loadedRows = rowsLoaded;
        insertedRows = rowsInserted;
        updatedRows = rowsUpdated;
        deletedRows = rowsDeleted;
        if (loadStartTime != null) {
            loadDurationMs = java.time.Duration.between(loadStartTime, loadEndTime).toMillis();
        }
    }

    /**
     * Complete ETL job
     */
    public void completeJob(boolean success) {
        status = success ? ETLStatus.COMPLETED : ETLStatus.FAILED;
        lastRunAt = LocalDateTime.now();

        totalExecutions = (totalExecutions != null ? totalExecutions : 0) + 1;

        if (success) {
            successfulExecutions = (successfulExecutions != null ? successfulExecutions : 0) + 1;
            lastSuccessTime = LocalDateTime.now();
        } else {
            failedExecutions = (failedExecutions != null ? failedExecutions : 0) + 1;
            lastFailureTime = LocalDateTime.now();
        }

        calculateTotalDuration();
        calculateRecordsPerSecond();
        calculateSuccessRate();
    }

    /**
     * Get total data volume in GB
     */
    public Double getDataVolumeGB() {
        if (dataVolumeMB == null) {
            return null;
        }
        return dataVolumeMB / 1024.0;
    }

    /**
     * Check if data quality is good
     */
    public boolean isDataQualityGood() {
        return dataQualityScore != null && dataQualityScore >= 80.0;
    }

    /**
     * Check if has data quality issues
     */
    public boolean hasDataQualityIssues() {
        return dataQualityIssues != null && !dataQualityIssues.isEmpty();
    }
}
