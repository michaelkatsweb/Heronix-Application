package com.heronix.controller;

import com.heronix.dto.ReportDataWarehouse;
import com.heronix.service.ReportETLService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report ETL API Controller
 *
 * REST API endpoints for ETL and data warehouse management.
 *
 * Endpoints:
 * - POST /api/etl - Create data warehouse
 * - GET /api/etl/{id} - Get data warehouse
 * - GET /api/etl/report/{reportId} - Get warehouses by report
 * - GET /api/etl/status/{status} - Get warehouses by status
 * - POST /api/etl/{id}/execute - Execute ETL job
 * - POST /api/etl/{id}/source - Add data source
 * - POST /api/etl/{id}/transformation - Add transformation
 * - POST /api/etl/{id}/validation - Add validation rule
 * - DELETE /api/etl/{id} - Delete data warehouse
 * - GET /api/etl/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 83 - Report Data Warehouse & ETL
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/etl")
@RequiredArgsConstructor
@Slf4j
public class ReportETLApiController {

    private final ReportETLService etlService;

    /**
     * Create data warehouse
     */
    @PostMapping
    public ResponseEntity<ReportDataWarehouse> createWarehouse(@RequestBody ReportDataWarehouse warehouse) {
        log.info("POST /api/etl - Creating data warehouse for report {}", warehouse.getReportId());

        try {
            ReportDataWarehouse created = etlService.createWarehouse(warehouse);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating data warehouse", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get data warehouse
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportDataWarehouse> getWarehouse(@PathVariable Long id) {
        log.info("GET /api/etl/{}", id);

        try {
            return etlService.getWarehouse(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching data warehouse: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get warehouses by report
     */
    @GetMapping("/report/{reportId}")
    public ResponseEntity<List<ReportDataWarehouse>> getWarehousesByReport(@PathVariable Long reportId) {
        log.info("GET /api/etl/report/{}", reportId);

        try {
            List<ReportDataWarehouse> warehouses = etlService.getWarehousesByReport(reportId);
            return ResponseEntity.ok(warehouses);

        } catch (Exception e) {
            log.error("Error fetching warehouses for report: {}", reportId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get warehouses by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReportDataWarehouse>> getWarehousesByStatus(
            @PathVariable ReportDataWarehouse.ETLStatus status) {
        log.info("GET /api/etl/status/{}", status);

        try {
            List<ReportDataWarehouse> warehouses = etlService.getWarehousesByStatus(status);
            return ResponseEntity.ok(warehouses);

        } catch (Exception e) {
            log.error("Error fetching warehouses by status: {}", status, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Execute ETL job
     */
    @PostMapping("/{id}/execute")
    public ResponseEntity<ReportDataWarehouse> executeETL(@PathVariable Long id) {
        log.info("POST /api/etl/{}/execute", id);

        try {
            ReportDataWarehouse result = etlService.executeETL(id);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.error("Data warehouse not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (IllegalStateException e) {
            log.error("Invalid state for ETL execution: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error executing ETL job for warehouse: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Add data source
     */
    @PostMapping("/{id}/source")
    public ResponseEntity<Map<String, Object>> addDataSource(
            @PathVariable Long id,
            @RequestBody ReportDataWarehouse.DataSource source) {
        log.info("POST /api/etl/{}/source", id);

        try {
            etlService.addDataSource(id, source);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Data source added");
            response.put("warehouseId", id);
            response.put("sourceId", source.getSourceId());
            response.put("sourceName", source.getSourceName());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Data warehouse not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding data source to warehouse: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add transformation
     */
    @PostMapping("/{id}/transformation")
    public ResponseEntity<Map<String, Object>> addTransformation(
            @PathVariable Long id,
            @RequestBody ReportDataWarehouse.Transformation transformation) {
        log.info("POST /api/etl/{}/transformation", id);

        try {
            etlService.addTransformation(id, transformation);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Transformation added");
            response.put("warehouseId", id);
            response.put("transformId", transformation.getTransformId());
            response.put("transformName", transformation.getName());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Data warehouse not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding transformation to warehouse: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add validation rule
     */
    @PostMapping("/{id}/validation")
    public ResponseEntity<Map<String, Object>> addValidationRule(
            @PathVariable Long id,
            @RequestBody ReportDataWarehouse.ValidationRule rule) {
        log.info("POST /api/etl/{}/validation", id);

        try {
            etlService.addValidationRule(id, rule);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Validation rule added");
            response.put("warehouseId", id);
            response.put("ruleId", rule.getRuleId());
            response.put("ruleName", rule.getName());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Data warehouse not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding validation rule to warehouse: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete data warehouse
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteWarehouse(@PathVariable Long id) {
        log.info("DELETE /api/etl/{}", id);

        try {
            etlService.deleteWarehouse(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Data warehouse deleted");
            response.put("warehouseId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting data warehouse: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/etl/stats");

        try {
            Map<String, Object> stats = etlService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching ETL statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
