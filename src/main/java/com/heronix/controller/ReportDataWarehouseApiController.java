package com.heronix.controller;

import com.heronix.dto.ReportDataWarehouse;
import com.heronix.service.ReportDataWarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Report Data Warehouse API Controller
 *
 * REST API endpoints for ETL operations, data integration,
 * and data warehouse management.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 83 - Report Data Warehouse & ETL
 */
@Slf4j
@RestController
@RequestMapping("/api/data-warehouse")
@RequiredArgsConstructor
public class ReportDataWarehouseApiController {

    private final ReportDataWarehouseService warehouseService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createWarehouse(@RequestBody ReportDataWarehouse warehouse) {
        try {
            ReportDataWarehouse created = warehouseService.createWarehouse(warehouse);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Data warehouse created successfully");
            response.put("warehouseId", created.getWarehouseId());
            response.put("warehouseName", "warehouse");
            response.put("createdAt", created.getCreatedAt());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create warehouse: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{warehouseId}")
    public ResponseEntity<Map<String, Object>> getWarehouse(@PathVariable Long warehouseId) {
        try {
            ReportDataWarehouse warehouse = warehouseService.getWarehouse(warehouseId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("warehouse", warehouse);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PostMapping("/{warehouseId}/run-etl")
    public ResponseEntity<Map<String, Object>> runETL(
            @PathVariable Long warehouseId,
            @RequestBody Map<String, String> request) {
        try {
            String jobName = request.get("jobName");
            ReportDataWarehouse warehouse = warehouseService.runETLJob(warehouseId, jobName);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "ETL job completed");
            response.put("status", "COMPLETED");
            response.put("lastRunAt", warehouse.getLastRunAt());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @DeleteMapping("/{warehouseId}")
    public ResponseEntity<Map<String, Object>> deleteWarehouse(@PathVariable Long warehouseId) {
        try {
            warehouseService.deleteWarehouse(warehouseId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Warehouse deleted successfully");
            response.put("deletedAt", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = warehouseService.getStatistics();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("statistics", stats);
        return ResponseEntity.ok(response);
    }
}
