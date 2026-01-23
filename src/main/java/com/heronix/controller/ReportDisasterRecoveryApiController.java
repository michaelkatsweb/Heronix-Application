package com.heronix.controller;

import com.heronix.dto.ReportDisasterRecovery;
import com.heronix.service.ReportDisasterRecoveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Report Disaster Recovery & Business Continuity API Controller
 *
 * REST API endpoints for disaster recovery and business continuity management.
 *
 * Endpoints:
 * - POST /api/disaster-recovery - Create DR plan
 * - GET /api/disaster-recovery/{id} - Get DR plan
 * - POST /api/disaster-recovery/{id}/activate - Activate plan
 * - POST /api/disaster-recovery/{id}/backup - Create backup job
 * - POST /api/disaster-recovery/{id}/backup/start - Start backup
 * - POST /api/disaster-recovery/{id}/backup/complete - Complete backup
 * - POST /api/disaster-recovery/{id}/site - Add recovery site
 * - POST /api/disaster-recovery/{id}/replication - Create replication stream
 * - POST /api/disaster-recovery/{id}/failover - Start failover
 * - POST /api/disaster-recovery/{id}/failover/complete - Complete failover
 * - POST /api/disaster-recovery/{id}/test - Create recovery test
 * - POST /api/disaster-recovery/{id}/test/execute - Execute test
 * - POST /api/disaster-recovery/{id}/test/complete - Complete test
 * - POST /api/disaster-recovery/{id}/incident - Report incident
 * - POST /api/disaster-recovery/{id}/incident/resolve - Resolve incident
 * - PUT /api/disaster-recovery/{id}/metrics - Update metrics
 * - DELETE /api/disaster-recovery/{id} - Delete plan
 * - GET /api/disaster-recovery/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 110 - Report Disaster Recovery & Business Continuity
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/disaster-recovery")
@RequiredArgsConstructor
@Slf4j
public class ReportDisasterRecoveryApiController {

    private final ReportDisasterRecoveryService drService;

    /**
     * Create disaster recovery plan
     */
    @PostMapping
    public ResponseEntity<ReportDisasterRecovery> createPlan(@RequestBody ReportDisasterRecovery plan) {
        log.info("POST /api/disaster-recovery - Creating DR plan: {}", plan.getPlanName());

        try {
            ReportDisasterRecovery created = drService.createPlan(plan);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating DR plan", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get disaster recovery plan
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportDisasterRecovery> getPlan(@PathVariable Long id) {
        log.info("GET /api/disaster-recovery/{}", id);

        try {
            return drService.getPlan(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching DR plan: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Activate disaster recovery plan
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<Map<String, Object>> activatePlan(@PathVariable Long id) {
        log.info("POST /api/disaster-recovery/{}/activate", id);

        try {
            drService.activatePlan(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Disaster recovery plan activated");
            response.put("planId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("DR plan not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error activating DR plan: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create backup job
     */
    @PostMapping("/{id}/backup")
    public ResponseEntity<ReportDisasterRecovery.BackupJob> createBackup(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/disaster-recovery/{}/backup", id);

        try {
            String jobName = request.get("jobName");
            String backupTypeStr = request.get("backupType");
            String sourceSystem = request.get("sourceSystem");
            String targetLocation = request.get("targetLocation");

            ReportDisasterRecovery.BackupType backupType =
                    ReportDisasterRecovery.BackupType.valueOf(backupTypeStr);

            ReportDisasterRecovery.BackupJob backup = drService.createBackup(
                    id, jobName, backupType, sourceSystem, targetLocation
            );

            return ResponseEntity.ok(backup);

        } catch (IllegalArgumentException e) {
            log.error("DR plan not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating backup for DR plan: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Start backup job
     */
    @PostMapping("/{id}/backup/start")
    public ResponseEntity<Map<String, Object>> startBackup(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/disaster-recovery/{}/backup/start", id);

        try {
            String jobId = request.get("jobId");

            drService.startBackup(id, jobId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Backup job started");
            response.put("jobId", jobId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("DR plan or backup job not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error starting backup for DR plan: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Complete backup job
     */
    @PostMapping("/{id}/backup/complete")
    public ResponseEntity<Map<String, Object>> completeBackup(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/disaster-recovery/{}/backup/complete", id);

        try {
            String jobId = (String) request.get("jobId");
            Boolean success = (Boolean) request.getOrDefault("success", true);
            Long backupSize = request.get("backupSize") != null ?
                    ((Number) request.get("backupSize")).longValue() : null;

            drService.completeBackup(id, jobId, success, backupSize);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Backup job completed");
            response.put("jobId", jobId);
            response.put("success", success);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("DR plan or backup job not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error completing backup for DR plan: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add recovery site
     */
    @PostMapping("/{id}/site")
    public ResponseEntity<ReportDisasterRecovery.RecoverySite> addSite(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/disaster-recovery/{}/site", id);

        try {
            String siteName = request.get("siteName");
            String siteTypeStr = request.get("siteType");
            String location = request.get("location");
            String provider = request.get("provider");

            ReportDisasterRecovery.SiteType siteType =
                    ReportDisasterRecovery.SiteType.valueOf(siteTypeStr);

            ReportDisasterRecovery.RecoverySite site = drService.addSite(
                    id, siteName, siteType, location, provider
            );

            return ResponseEntity.ok(site);

        } catch (IllegalArgumentException e) {
            log.error("DR plan not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error adding site to DR plan: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create replication stream
     */
    @PostMapping("/{id}/replication")
    public ResponseEntity<ReportDisasterRecovery.ReplicationStream> createReplication(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/disaster-recovery/{}/replication", id);

        try {
            String sourceSite = request.get("sourceSite");
            String targetSite = request.get("targetSite");
            String modeStr = request.get("replicationMode");
            String dataType = request.get("dataType");

            ReportDisasterRecovery.ReplicationMode mode =
                    ReportDisasterRecovery.ReplicationMode.valueOf(modeStr);

            ReportDisasterRecovery.ReplicationStream replication = drService.createReplication(
                    id, sourceSite, targetSite, mode, dataType
            );

            return ResponseEntity.ok(replication);

        } catch (IllegalArgumentException e) {
            log.error("DR plan not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating replication for DR plan: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Start failover operation
     */
    @PostMapping("/{id}/failover")
    public ResponseEntity<ReportDisasterRecovery.FailoverOperation> startFailover(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/disaster-recovery/{}/failover", id);

        try {
            String sourceSite = request.get("sourceSite");
            String targetSite = request.get("targetSite");
            String failoverType = request.get("failoverType");
            String reason = request.get("reason");

            ReportDisasterRecovery.FailoverOperation failover = drService.startFailover(
                    id, sourceSite, targetSite, failoverType, reason
            );

            return ResponseEntity.ok(failover);

        } catch (IllegalArgumentException e) {
            log.error("DR plan not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error starting failover for DR plan: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Complete failover operation
     */
    @PostMapping("/{id}/failover/complete")
    public ResponseEntity<Map<String, Object>> completeFailover(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/disaster-recovery/{}/failover/complete", id);

        try {
            String failoverId = (String) request.get("failoverId");
            Boolean success = (Boolean) request.getOrDefault("success", true);

            drService.completeFailover(id, failoverId, success);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Failover operation completed");
            response.put("failoverId", failoverId);
            response.put("success", success);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("DR plan or failover not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error completing failover for DR plan: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create recovery test
     */
    @PostMapping("/{id}/test")
    public ResponseEntity<ReportDisasterRecovery.RecoveryTest> createTest(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/disaster-recovery/{}/test", id);

        try {
            String testName = request.get("testName");
            String testType = request.get("testType");
            String targetSite = request.get("targetSite");

            ReportDisasterRecovery.RecoveryTest test = drService.createTest(
                    id, testName, testType, targetSite
            );

            return ResponseEntity.ok(test);

        } catch (IllegalArgumentException e) {
            log.error("DR plan not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating test for DR plan: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Execute recovery test
     */
    @PostMapping("/{id}/test/execute")
    public ResponseEntity<Map<String, Object>> executeTest(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/disaster-recovery/{}/test/execute", id);

        try {
            String testId = request.get("testId");

            drService.executeTest(id, testId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Recovery test started");
            response.put("testId", testId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("DR plan or test not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error executing test for DR plan: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Complete recovery test
     */
    @PostMapping("/{id}/test/complete")
    public ResponseEntity<Map<String, Object>> completeTest(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/disaster-recovery/{}/test/complete", id);

        try {
            String testId = (String) request.get("testId");
            Boolean passed = (Boolean) request.getOrDefault("passed", true);
            Long achievedRto = request.get("achievedRto") != null ?
                    ((Number) request.get("achievedRto")).longValue() : null;
            Long achievedRpo = request.get("achievedRpo") != null ?
                    ((Number) request.get("achievedRpo")).longValue() : null;

            drService.completeTest(id, testId, passed, achievedRto, achievedRpo);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Recovery test completed");
            response.put("testId", testId);
            response.put("passed", passed);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("DR plan or test not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error completing test for DR plan: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Report disaster incident
     */
    @PostMapping("/{id}/incident")
    public ResponseEntity<ReportDisasterRecovery.DisasterIncident> reportIncident(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/disaster-recovery/{}/incident", id);

        try {
            String incidentName = request.get("incidentName");
            String severityStr = request.get("severity");
            String description = request.get("description");
            String affectedSystems = request.get("affectedSystems");

            ReportDisasterRecovery.IncidentSeverity severity =
                    ReportDisasterRecovery.IncidentSeverity.valueOf(severityStr);

            ReportDisasterRecovery.DisasterIncident incident = drService.reportIncident(
                    id, incidentName, severity, description, affectedSystems
            );

            return ResponseEntity.ok(incident);

        } catch (IllegalArgumentException e) {
            log.error("DR plan not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error reporting incident for DR plan: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Resolve disaster incident
     */
    @PostMapping("/{id}/incident/resolve")
    public ResponseEntity<Map<String, Object>> resolveIncident(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/disaster-recovery/{}/incident/resolve", id);

        try {
            String incidentId = request.get("incidentId");
            String resolution = request.get("resolution");

            drService.resolveIncident(id, incidentId, resolution);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Disaster incident resolved");
            response.put("incidentId", incidentId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("DR plan or incident not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error resolving incident for DR plan: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update metrics
     */
    @PutMapping("/{id}/metrics")
    public ResponseEntity<Map<String, Object>> updateMetrics(@PathVariable Long id) {
        log.info("PUT /api/disaster-recovery/{}/metrics", id);

        try {
            drService.updateMetrics(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Metrics updated");
            response.put("planId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("DR plan not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error updating metrics for DR plan: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete disaster recovery plan
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePlan(@PathVariable Long id) {
        log.info("DELETE /api/disaster-recovery/{}", id);

        try {
            drService.deletePlan(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Disaster recovery plan deleted");
            response.put("planId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting DR plan: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/disaster-recovery/stats");

        try {
            Map<String, Object> stats = drService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching DR statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
