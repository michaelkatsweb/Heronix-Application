package com.heronix.service;

import com.heronix.dto.ReportDataWarehouse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportDataWarehouseService {

    private final Map<Long, ReportDataWarehouse> warehouseStore = new ConcurrentHashMap<>();
    private final AtomicLong warehouseIdGenerator = new AtomicLong(1);

    public ReportDataWarehouse createWarehouse(ReportDataWarehouse warehouse) {
        Long warehouseId = warehouseIdGenerator.getAndIncrement();
        warehouse.setWarehouseId(warehouseId);

        warehouseStore.put(warehouseId, warehouse);

        log.info("Data warehouse created: {} (id: {})", warehouseId, warehouseId);
        return warehouse;
    }

    public ReportDataWarehouse getWarehouse(Long warehouseId) {
        ReportDataWarehouse warehouse = warehouseStore.get(warehouseId);
        if (warehouse == null) {
            throw new IllegalArgumentException("Data warehouse not found: " + warehouseId);
        }
        return warehouse;
    }

    public ReportDataWarehouse runETLJob(Long warehouseId, String jobName) {
        ReportDataWarehouse warehouse = getWarehouse(warehouseId);

        log.info("ETL job completed: {} (warehouse: {})", jobName, warehouseId);
        return warehouse;
    }

    public void deleteWarehouse(Long warehouseId) {
        ReportDataWarehouse warehouse = warehouseStore.remove(warehouseId);
        if (warehouse == null) {
            throw new IllegalArgumentException("Data warehouse not found: " + warehouseId);
        }
        log.info("Data warehouse deleted: {}", warehouseId);
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalWarehouses", warehouseStore.size());
        stats.put("timestamp", LocalDateTime.now());
        return stats;
    }
}
