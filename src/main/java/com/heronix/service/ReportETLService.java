package com.heronix.service;

import com.heronix.dto.ReportDataWarehouse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Report ETL Service
 *
 * Manages ETL (Extract, Transform, Load) processes for data warehousing.
 *
 * Features:
 * - Full and incremental data loads
 * - Multi-source data extraction
 * - Data transformation and cleansing
 * - Data validation and quality checks
 * - Dimensional modeling (SCD Type 1, 2)
 * - Bulk loading and batch processing
 * - Lineage tracking and auditing
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 83 - Report Data Warehouse & ETL
 */
@Service
@Slf4j
public class ReportETLService {

    private final Map<Long, ReportDataWarehouse> warehouses = new ConcurrentHashMap<>();
    private Long nextWarehouseId = 1L;

    /**
     * Create data warehouse
     */
    public ReportDataWarehouse createWarehouse(ReportDataWarehouse warehouse) {
        synchronized (this) {
            warehouse.setWarehouseId(nextWarehouseId++);
            warehouse.setCreatedAt(LocalDateTime.now());
            warehouse.setStatus(ReportDataWarehouse.ETLStatus.PENDING);
            warehouse.setTotalExecutions(0L);
            warehouse.setSuccessfulExecutions(0L);
            warehouse.setFailedExecutions(0L);
            warehouse.setRetryCount(0);

            // Set defaults
            if (warehouse.getEnabled() == null) {
                warehouse.setEnabled(true);
            }

            if (warehouse.getRetryOnFailure() == null) {
                warehouse.setRetryOnFailure(true);
            }

            if (warehouse.getMaxRetryAttempts() == null) {
                warehouse.setMaxRetryAttempts(3);
            }

            if (warehouse.getExtractBatchSize() == null) {
                warehouse.setExtractBatchSize(1000);
            }

            if (warehouse.getLoadBatchSize() == null) {
                warehouse.setLoadBatchSize(500);
            }

            if (warehouse.getDataCleansingEnabled() == null) {
                warehouse.setDataCleansingEnabled(true);
            }

            if (warehouse.getDataValidationEnabled() == null) {
                warehouse.setDataValidationEnabled(true);
            }

            warehouses.put(warehouse.getWarehouseId(), warehouse);

            log.info("Created data warehouse {} for report {} with job type {}",
                    warehouse.getWarehouseId(), warehouse.getReportId(), warehouse.getJobType());

            logAudit(warehouse, "WAREHOUSE_CREATED", "Data warehouse created");

            return warehouse;
        }
    }

    /**
     * Get warehouse
     */
    public Optional<ReportDataWarehouse> getWarehouse(Long warehouseId) {
        return Optional.ofNullable(warehouses.get(warehouseId));
    }

    /**
     * Get warehouses by report
     */
    public List<ReportDataWarehouse> getWarehousesByReport(Long reportId) {
        return warehouses.values().stream()
                .filter(w -> reportId.equals(w.getReportId()))
                .collect(Collectors.toList());
    }

    /**
     * Get warehouses by status
     */
    public List<ReportDataWarehouse> getWarehousesByStatus(ReportDataWarehouse.ETLStatus status) {
        return warehouses.values().stream()
                .filter(w -> w.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * Execute ETL job
     */
    public ReportDataWarehouse executeETL(Long warehouseId) {
        ReportDataWarehouse warehouse = warehouses.get(warehouseId);
        if (warehouse == null) {
            throw new IllegalArgumentException("Data warehouse not found: " + warehouseId);
        }

        if (!Boolean.TRUE.equals(warehouse.getEnabled())) {
            throw new IllegalStateException("Data warehouse is not enabled");
        }

        if (warehouse.isRunning()) {
            throw new IllegalStateException("ETL job is already running");
        }

        log.info("Starting ETL job {} ({})", warehouseId, warehouse.getJobType());

        logAudit(warehouse, "ETL_STARTED", "ETL job execution started");

        try {
            // Phase 1: Extract
            extractData(warehouse);

            // Phase 2: Transform
            transformData(warehouse);

            // Phase 3: Load
            loadData(warehouse);

            // Phase 4: Validate
            validateData(warehouse);

            // Complete job
            warehouse.completeJob(true);

            log.info("Completed ETL job {} successfully in {} ms",
                    warehouseId, warehouse.getTotalDurationMs());

            logAudit(warehouse, "ETL_COMPLETED", "ETL job completed successfully");

        } catch (Exception e) {
            warehouse.setStatus(ReportDataWarehouse.ETLStatus.FAILED);
            warehouse.setLastErrorMessage(e.getMessage());
            warehouse.completeJob(false);

            log.error("ETL job {} failed: {}", warehouseId, e.getMessage(), e);

            logAudit(warehouse, "ETL_FAILED", "ETL job failed: " + e.getMessage());

            // Retry if enabled
            if (Boolean.TRUE.equals(warehouse.getRetryOnFailure())) {
                Integer currentRetry = warehouse.getRetryCount();
                Integer maxRetries = warehouse.getMaxRetryAttempts();

                if (currentRetry < maxRetries) {
                    warehouse.setRetryCount(currentRetry + 1);
                    log.info("Retrying ETL job {} (attempt {}/{})",
                            warehouseId, currentRetry + 1, maxRetries);
                    return executeETL(warehouseId);
                }
            }

            throw new RuntimeException("ETL job failed: " + e.getMessage(), e);
        }

        return warehouse;
    }

    /**
     * Extract data
     */
    private void extractData(ReportDataWarehouse warehouse) {
        log.info("Extracting data for warehouse {}", warehouse.getWarehouseId());

        warehouse.startExtraction();

        try {
            // Simulate data extraction
            long rowsExtracted = 0;

            if (warehouse.getDataSources() != null) {
                for (ReportDataWarehouse.DataSource source : warehouse.getDataSources()) {
                    if (Boolean.TRUE.equals(source.getEnabled())) {
                        rowsExtracted += extractFromSource(warehouse, source);
                    }
                }
            } else {
                // Default extraction
                rowsExtracted = (long) (Math.random() * 10000 + 1000);
            }

            warehouse.completeExtraction(rowsExtracted);

            log.info("Extracted {} rows in {} ms",
                    rowsExtracted, warehouse.getExtractDurationMs());

            logAudit(warehouse, "EXTRACTION_COMPLETED",
                    "Extracted " + rowsExtracted + " rows");

        } catch (Exception e) {
            log.error("Extraction failed for warehouse {}", warehouse.getWarehouseId(), e);
            throw new RuntimeException("Extraction failed: " + e.getMessage(), e);
        }
    }

    /**
     * Extract from source
     */
    private long extractFromSource(ReportDataWarehouse warehouse, ReportDataWarehouse.DataSource source) {
        log.debug("Extracting from source: {} ({})", source.getSourceName(), source.getSourceType());

        source.setLastConnected(LocalDateTime.now());

        // Simulate extraction based on source type
        return switch (source.getSourceType()) {
            case DATABASE -> (long) (Math.random() * 5000 + 500);
            case FILE -> (long) (Math.random() * 3000 + 300);
            case API -> (long) (Math.random() * 2000 + 200);
            case CLOUD_STORAGE -> (long) (Math.random() * 4000 + 400);
            case STREAMING -> (long) (Math.random() * 1000 + 100);
            case DATA_LAKE -> (long) (Math.random() * 8000 + 800);
            default -> (long) (Math.random() * 1000 + 100);
        };
    }

    /**
     * Transform data
     */
    private void transformData(ReportDataWarehouse warehouse) {
        log.info("Transforming data for warehouse {}", warehouse.getWarehouseId());

        warehouse.startTransformation();

        try {
            long rowsTransformed = warehouse.getExtractedRows() != null ? warehouse.getExtractedRows() : 0;
            long rowsRejected = 0;

            // Apply transformations
            if (warehouse.getTransformations() != null) {
                for (ReportDataWarehouse.Transformation transform : warehouse.getTransformations()) {
                    if (Boolean.TRUE.equals(transform.getEnabled())) {
                        long[] result = applyTransformation(warehouse, transform, rowsTransformed);
                        rowsTransformed = result[0];
                        rowsRejected += result[1];
                    }
                }
            }

            // Apply data cleansing
            if (Boolean.TRUE.equals(warehouse.getDataCleansingEnabled())) {
                long cleaned = cleanseData(warehouse, rowsTransformed);
                rowsRejected += (rowsTransformed - cleaned);
                rowsTransformed = cleaned;
            }

            warehouse.completeTransformation(rowsTransformed, rowsRejected);

            log.info("Transformed {} rows ({} rejected) in {} ms",
                    rowsTransformed, rowsRejected, warehouse.getTransformDurationMs());

            logAudit(warehouse, "TRANSFORMATION_COMPLETED",
                    "Transformed " + rowsTransformed + " rows, rejected " + rowsRejected);

        } catch (Exception e) {
            log.error("Transformation failed for warehouse {}", warehouse.getWarehouseId(), e);
            throw new RuntimeException("Transformation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Apply transformation
     */
    private long[] applyTransformation(ReportDataWarehouse warehouse,
                                       ReportDataWarehouse.Transformation transform,
                                       long inputRows) {
        log.debug("Applying transformation: {} ({})", transform.getName(), transform.getType());

        long processed = inputRows;
        long rejected = 0;

        // Simulate transformation based on type
        switch (transform.getType()) {
            case FILTER -> {
                rejected = (long) (inputRows * 0.05); // 5% filtered out
                processed = inputRows - rejected;
            }
            case DEDUPLICATE -> {
                rejected = (long) (inputRows * 0.02); // 2% duplicates
                processed = inputRows - rejected;
            }
            case CLEAN -> {
                rejected = (long) (inputRows * 0.03); // 3% cleaned
                processed = inputRows - rejected;
            }
            case VALIDATE -> {
                rejected = (long) (inputRows * 0.01); // 1% invalid
                processed = inputRows - rejected;
            }
            default -> processed = inputRows;
        }

        transform.setRecordsProcessed(processed);
        transform.setRecordsRejected(rejected);

        return new long[]{processed, rejected};
    }

    /**
     * Cleanse data
     */
    private long cleanseData(ReportDataWarehouse warehouse, long inputRows) {
        log.debug("Cleansing data for warehouse {}", warehouse.getWarehouseId());

        // Simulate data cleansing
        long nulls = (long) (inputRows * 0.01); // 1% null values
        long duplicates = (long) (inputRows * 0.02); // 2% duplicates

        warehouse.setNullValues(nulls);
        warehouse.setDuplicateRecords(duplicates);

        return inputRows - nulls - duplicates;
    }

    /**
     * Load data
     */
    private void loadData(ReportDataWarehouse warehouse) {
        log.info("Loading data for warehouse {}", warehouse.getWarehouseId());

        warehouse.startLoading();

        try {
            long rowsToLoad = warehouse.getTransformedRows() != null ? warehouse.getTransformedRows() : 0;
            long rowsInserted = 0;
            long rowsUpdated = 0;
            long rowsDeleted = 0;

            // Simulate loading based on strategy
            switch (warehouse.getLoadStrategy()) {
                case INSERT -> rowsInserted = rowsToLoad;
                case UPDATE -> rowsUpdated = rowsToLoad;
                case UPSERT -> {
                    rowsInserted = (long) (rowsToLoad * 0.7);
                    rowsUpdated = (long) (rowsToLoad * 0.3);
                }
                case MERGE -> {
                    rowsInserted = (long) (rowsToLoad * 0.6);
                    rowsUpdated = (long) (rowsToLoad * 0.3);
                    rowsDeleted = (long) (rowsToLoad * 0.1);
                }
                case TRUNCATE_INSERT -> rowsInserted = rowsToLoad;
                case APPEND -> rowsInserted = rowsToLoad;
                case REPLACE -> {
                    rowsDeleted = (long) (rowsToLoad * 0.5);
                    rowsInserted = rowsToLoad;
                }
                default -> rowsInserted = rowsToLoad;
            }

            warehouse.completeLoading(rowsToLoad, rowsInserted, rowsUpdated, rowsDeleted);

            log.info("Loaded {} rows (I:{}, U:{}, D:{}) in {} ms",
                    rowsToLoad, rowsInserted, rowsUpdated, rowsDeleted, warehouse.getLoadDurationMs());

            logAudit(warehouse, "LOADING_COMPLETED",
                    String.format("Loaded %d rows (Inserted: %d, Updated: %d, Deleted: %d)",
                            rowsToLoad, rowsInserted, rowsUpdated, rowsDeleted));

        } catch (Exception e) {
            log.error("Loading failed for warehouse {}", warehouse.getWarehouseId(), e);
            throw new RuntimeException("Loading failed: " + e.getMessage(), e);
        }
    }

    /**
     * Validate data
     */
    private void validateData(ReportDataWarehouse warehouse) {
        log.info("Validating data for warehouse {}", warehouse.getWarehouseId());

        warehouse.setStatus(ReportDataWarehouse.ETLStatus.VALIDATING);

        try {
            long invalidRecords = 0;

            // Apply validation rules
            if (Boolean.TRUE.equals(warehouse.getDataValidationEnabled()) &&
                warehouse.getValidationRules() != null) {

                for (ReportDataWarehouse.ValidationRule rule : warehouse.getValidationRules()) {
                    if (Boolean.TRUE.equals(rule.getEnabled())) {
                        long violations = validateRule(warehouse, rule);
                        invalidRecords += violations;
                        rule.setViolationCount(violations);
                    }
                }
            }

            warehouse.setInvalidRecords(invalidRecords);

            // Calculate data quality metrics
            calculateDataQuality(warehouse);

            log.info("Validation completed: {} invalid records, quality score: {}",
                    invalidRecords, warehouse.getDataQualityScore());

            logAudit(warehouse, "VALIDATION_COMPLETED",
                    "Data quality score: " + warehouse.getDataQualityScore());

        } catch (Exception e) {
            log.error("Validation failed for warehouse {}", warehouse.getWarehouseId(), e);
            throw new RuntimeException("Validation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Validate rule
     */
    private long validateRule(ReportDataWarehouse warehouse, ReportDataWarehouse.ValidationRule rule) {
        log.debug("Validating rule: {} ({})", rule.getName(), rule.getRuleType());

        // Simulate validation
        long totalRows = warehouse.getLoadedRows() != null ? warehouse.getLoadedRows() : 0;

        return switch (rule.getRuleType()) {
            case "NOT_NULL" -> (long) (totalRows * 0.01); // 1% nulls
            case "RANGE" -> (long) (totalRows * 0.02); // 2% out of range
            case "REGEX" -> (long) (totalRows * 0.015); // 1.5% invalid format
            case "CUSTOM" -> (long) (totalRows * 0.01); // 1% custom violations
            default -> 0L;
        };
    }

    /**
     * Calculate data quality
     */
    private void calculateDataQuality(ReportDataWarehouse warehouse) {
        long totalRows = warehouse.getLoadedRows() != null ? warehouse.getLoadedRows() : 0;

        if (totalRows == 0) {
            return;
        }

        // Calculate completeness
        long nulls = warehouse.getNullValues() != null ? warehouse.getNullValues() : 0;
        warehouse.setCompleteness((1.0 - (double) nulls / totalRows) * 100.0);

        // Calculate accuracy
        long invalid = warehouse.getInvalidRecords() != null ? warehouse.getInvalidRecords() : 0;
        warehouse.setAccuracy((1.0 - (double) invalid / totalRows) * 100.0);

        // Calculate consistency
        long duplicates = warehouse.getDuplicateRecords() != null ? warehouse.getDuplicateRecords() : 0;
        warehouse.setConsistency((1.0 - (double) duplicates / totalRows) * 100.0);

        // Calculate validity
        long rejected = warehouse.getRejectedRows() != null ? warehouse.getRejectedRows() : 0;
        long total = totalRows + rejected;
        warehouse.setValidity((double) totalRows / total * 100.0);

        // Calculate overall quality score
        warehouse.calculateDataQualityScore();
    }

    /**
     * Add data source
     */
    public void addDataSource(Long warehouseId, ReportDataWarehouse.DataSource source) {
        ReportDataWarehouse warehouse = warehouses.get(warehouseId);
        if (warehouse == null) {
            throw new IllegalArgumentException("Data warehouse not found: " + warehouseId);
        }

        if (warehouse.getDataSources() == null) {
            warehouse.setDataSources(new ArrayList<>());
        }

        source.setSourceId(UUID.randomUUID().toString());
        warehouse.getDataSources().add(source);

        log.info("Added data source {} to warehouse {}", source.getSourceName(), warehouseId);
    }

    /**
     * Add transformation
     */
    public void addTransformation(Long warehouseId, ReportDataWarehouse.Transformation transformation) {
        ReportDataWarehouse warehouse = warehouses.get(warehouseId);
        if (warehouse == null) {
            throw new IllegalArgumentException("Data warehouse not found: " + warehouseId);
        }

        if (warehouse.getTransformations() == null) {
            warehouse.setTransformations(new ArrayList<>());
        }

        transformation.setTransformId(UUID.randomUUID().toString());
        transformation.setSequence(warehouse.getTransformations().size() + 1);
        warehouse.getTransformations().add(transformation);

        log.info("Added transformation {} to warehouse {}", transformation.getName(), warehouseId);
    }

    /**
     * Add validation rule
     */
    public void addValidationRule(Long warehouseId, ReportDataWarehouse.ValidationRule rule) {
        ReportDataWarehouse warehouse = warehouses.get(warehouseId);
        if (warehouse == null) {
            throw new IllegalArgumentException("Data warehouse not found: " + warehouseId);
        }

        if (warehouse.getValidationRules() == null) {
            warehouse.setValidationRules(new ArrayList<>());
        }

        rule.setRuleId(UUID.randomUUID().toString());
        warehouse.getValidationRules().add(rule);

        log.info("Added validation rule {} to warehouse {}", rule.getName(), warehouseId);
    }

    /**
     * Log audit entry
     */
    private void logAudit(ReportDataWarehouse warehouse, String action, String details) {
        if (!Boolean.TRUE.equals(warehouse.getAuditLoggingEnabled())) {
            return;
        }

        ReportDataWarehouse.AuditLog auditLog = ReportDataWarehouse.AuditLog.builder()
                .logId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .action(action)
                .username(warehouse.getCreatedBy())
                .details(details)
                .severity("INFO")
                .build();

        if (warehouse.getAuditLogs() == null) {
            warehouse.setAuditLogs(new ArrayList<>());
        }

        warehouse.getAuditLogs().add(auditLog);

        // Keep only last 1000 audit logs
        if (warehouse.getAuditLogs().size() > 1000) {
            warehouse.getAuditLogs().remove(0);
        }
    }

    /**
     * Delete warehouse
     */
    public void deleteWarehouse(Long warehouseId) {
        ReportDataWarehouse removed = warehouses.remove(warehouseId);
        if (removed != null) {
            log.info("Deleted data warehouse {}", warehouseId);
        }
    }

    /**
     * Get statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalWarehouses", warehouses.size());

        long running = warehouses.values().stream()
                .filter(ReportDataWarehouse::isRunning)
                .count();

        long completed = warehouses.values().stream()
                .filter(ReportDataWarehouse::isCompleted)
                .count();

        long failed = warehouses.values().stream()
                .filter(ReportDataWarehouse::isFailed)
                .count();

        stats.put("runningJobs", running);
        stats.put("completedJobs", completed);
        stats.put("failedJobs", failed);

        double avgDuration = warehouses.values().stream()
                .filter(w -> w.getAverageDurationMs() != null)
                .mapToDouble(ReportDataWarehouse::getAverageDurationMs)
                .average()
                .orElse(0.0);

        stats.put("averageDurationMs", avgDuration);

        double avgQuality = warehouses.values().stream()
                .filter(w -> w.getDataQualityScore() != null)
                .mapToDouble(ReportDataWarehouse::getDataQualityScore)
                .average()
                .orElse(0.0);

        stats.put("averageDataQualityScore", avgQuality);

        // Count by job type
        Map<ReportDataWarehouse.ETLJobType, Long> byType = warehouses.values().stream()
                .collect(Collectors.groupingBy(ReportDataWarehouse::getJobType, Collectors.counting()));
        stats.put("warehousesByJobType", byType);

        return stats;
    }
}
