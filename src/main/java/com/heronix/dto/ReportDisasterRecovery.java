package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Disaster Recovery & Business Continuity DTO
 *
 * Manages disaster recovery planning, backup operations, and business continuity.
 *
 * Features:
 * - Disaster recovery planning
 * - Automated backup management
 * - Recovery point objectives (RPO)
 * - Recovery time objectives (RTO)
 * - Failover and failback operations
 * - Data replication
 * - Business continuity testing
 * - Incident response management
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 110 - Report Disaster Recovery & Business Continuity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDisasterRecovery {

    // Plan Information
    private Long planId;
    private String planName;
    private String description;
    private PlanStatus status;
    private Boolean isActive;

    // Recovery Objectives
    private Integer rpoMinutes;
    private Integer rtoMinutes;
    private String criticalityLevel;

    // Backup Configuration
    private List<BackupJob> backupJobs;
    private Map<String, BackupJob> backupRegistry;
    private Long totalBackups;
    private Long successfulBackups;
    private Long failedBackups;

    // Recovery Sites
    private List<RecoverySite> recoverySites;
    private Map<String, RecoverySite> siteRegistry;
    private RecoverySite primarySite;
    private RecoverySite secondarySite;

    // Replication
    private List<ReplicationStream> replications;
    private Map<String, ReplicationStream> replicationRegistry;
    private Long totalReplications;
    private Long activeReplications;

    // Failover Operations
    private List<FailoverOperation> failovers;
    private Map<String, FailoverOperation> failoverRegistry;
    private Long totalFailovers;
    private Long successfulFailovers;

    // Recovery Tests
    private List<RecoveryTest> recoveryTests;
    private Map<String, RecoveryTest> testRegistry;
    private Long totalTests;
    private Long passedTests;
    private LocalDateTime lastTestDate;

    // Incidents
    private List<DisasterIncident> incidents;
    private Map<String, DisasterIncident> incidentRegistry;
    private Long totalIncidents;
    private Long activeIncidents;
    private Long resolvedIncidents;

    // Snapshots
    private List<DataSnapshot> snapshots;
    private Map<String, DataSnapshot> snapshotRegistry;
    private Long totalSnapshots;

    // Recovery Procedures
    private List<RecoveryProcedure> procedures;
    private Map<String, RecoveryProcedure> procedureRegistry;

    // Metrics
    private DRMetrics metrics;
    private LocalDateTime lastMetricsUpdate;

    // Events
    private List<DREvent> events;
    private LocalDateTime lastEventAt;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime activatedAt;
    private LocalDateTime lastBackupAt;
    private LocalDateTime lastTestAt;

    /**
     * Plan Status
     */
    public enum PlanStatus {
        DRAFT,
        ACTIVE,
        TESTING,
        FAILOVER_IN_PROGRESS,
        RECOVERED,
        SUSPENDED
    }

    /**
     * Backup Type
     */
    public enum BackupType {
        FULL,
        INCREMENTAL,
        DIFFERENTIAL,
        SNAPSHOT,
        CONTINUOUS,
        SYNTHETIC
    }

    /**
     * Backup Status
     */
    public enum BackupStatus {
        SCHEDULED,
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED,
        EXPIRED
    }

    /**
     * Site Type
     */
    public enum SiteType {
        PRIMARY,
        SECONDARY,
        TERTIARY,
        HOT_STANDBY,
        WARM_STANDBY,
        COLD_STANDBY,
        CLOUD_BACKUP
    }

    /**
     * Site Status
     */
    public enum SiteStatus {
        ONLINE,
        OFFLINE,
        SYNCING,
        READY,
        DEGRADED,
        FAILOVER_ACTIVE
    }

    /**
     * Replication Mode
     */
    public enum ReplicationMode {
        SYNCHRONOUS,
        ASYNCHRONOUS,
        SEMI_SYNCHRONOUS,
        SNAPSHOT_BASED
    }

    /**
     * Failover Status
     */
    public enum FailoverStatus {
        INITIATED,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        ROLLED_BACK
    }

    /**
     * Test Status
     */
    public enum TestStatus {
        PLANNED,
        IN_PROGRESS,
        PASSED,
        FAILED,
        CANCELLED
    }

    /**
     * Incident Severity
     */
    public enum IncidentSeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL,
        CATASTROPHIC
    }

    /**
     * Backup Job
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BackupJob {
        private String jobId;
        private String jobName;
        private BackupType backupType;
        private BackupStatus status;
        private String dataSource;
        private String destination;
        private String schedule;
        private Integer retentionDays;
        private Long backupSize;
        private Long dataSize;
        private Double compressionRatio;
        private Boolean encrypted;
        private LocalDateTime scheduledAt;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private Long durationSeconds;
        private String errorMessage;
        private Map<String, Object> metadata;
    }

    /**
     * Recovery Site
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecoverySite {
        private String siteId;
        private String siteName;
        private SiteType siteType;
        private SiteStatus status;
        private String location;
        private String region;
        private Boolean isPrimary;
        private Long capacity;
        private Long usedCapacity;
        private Integer latencyMs;
        private LocalDateTime lastHealthCheck;
        private Boolean isHealthy;
        private Map<String, String> endpoints;
        private Map<String, Object> metadata;
    }

    /**
     * Replication Stream
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReplicationStream {
        private String streamId;
        private String streamName;
        private String sourceSite;
        private String targetSite;
        private ReplicationMode mode;
        private String status;
        private Long bytesReplicated;
        private Long pendingBytes;
        private Double lagSeconds;
        private LocalDateTime lastReplicationAt;
        private Boolean healthy;
        private Map<String, Object> metadata;
    }

    /**
     * Failover Operation
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailoverOperation {
        private String failoverId;
        private String failoverName;
        private FailoverStatus status;
        private String sourceSite;
        private String targetSite;
        private String triggerReason;
        private Boolean planned;
        private LocalDateTime initiatedAt;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private Long durationSeconds;
        private Long dataLoss;
        private Boolean successful;
        private String errorMessage;
        private Map<String, Object> metadata;
    }

    /**
     * Recovery Test
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecoveryTest {
        private String testId;
        private String testName;
        private TestStatus status;
        private String testType;
        private String targetSite;
        private LocalDateTime scheduledAt;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private Long durationSeconds;
        private Boolean passed;
        private List<String> testResults;
        private Integer rtoAchieved;
        private Integer rpoAchieved;
        private String notes;
        private Map<String, Object> metadata;
    }

    /**
     * Disaster Incident
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DisasterIncident {
        private String incidentId;
        private String incidentName;
        private IncidentSeverity severity;
        private String status;
        private String incidentType;
        private String affectedSite;
        private String description;
        private LocalDateTime detectedAt;
        private LocalDateTime reportedAt;
        private String reportedBy;
        private LocalDateTime respondedAt;
        private LocalDateTime resolvedAt;
        private String resolution;
        private Long impactDuration;
        private Integer affectedUsers;
        private Map<String, Object> metadata;
    }

    /**
     * Data Snapshot
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataSnapshot {
        private String snapshotId;
        private String snapshotName;
        private String dataSource;
        private Long snapshotSize;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private Boolean isValid;
        private String storageLocation;
        private String status;
        private Map<String, Object> metadata;
    }

    /**
     * Recovery Procedure
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecoveryProcedure {
        private String procedureId;
        private String procedureName;
        private String description;
        private Integer stepCount;
        private List<String> steps;
        private Integer estimatedMinutes;
        private String owner;
        private LocalDateTime lastUpdated;
        private Map<String, Object> metadata;
    }

    /**
     * DR Metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DRMetrics {
        private Long totalBackups;
        private Long successfulBackups;
        private Long failedBackups;
        private Double backupSuccessRate;
        private Long totalBackupSize;
        private Long totalReplications;
        private Long activeReplications;
        private Double averageReplicationLag;
        private Long totalFailovers;
        private Long successfulFailovers;
        private Double failoverSuccessRate;
        private Integer averageFailoverTime;
        private Long totalTests;
        private Long passedTests;
        private Double testSuccessRate;
        private Integer currentRPO;
        private Integer currentRTO;
        private Long totalIncidents;
        private Long activeIncidents;
        private Long resolvedIncidents;
        private Double mttr;
        private LocalDateTime measuredAt;
    }

    /**
     * DR Event
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DREvent {
        private String eventId;
        private LocalDateTime timestamp;
        private String eventType;
        private String description;
        private String resourceType;
        private String resourceId;
        private Map<String, Object> details;
    }

    // Helper Methods

    /**
     * Activate plan
     */
    public void activatePlan() {
        this.status = PlanStatus.ACTIVE;
        this.isActive = true;
        this.activatedAt = LocalDateTime.now();

        recordEvent("PLAN_ACTIVATED", "DR plan activated", "PLAN",
                planId != null ? planId.toString() : null);
    }

    /**
     * Schedule backup
     */
    public void scheduleBackup(BackupJob backup) {
        if (backupJobs == null) {
            backupJobs = new java.util.ArrayList<>();
        }
        backupJobs.add(backup);

        if (backupRegistry == null) {
            backupRegistry = new java.util.HashMap<>();
        }
        backupRegistry.put(backup.getJobId(), backup);

        totalBackups = (totalBackups != null ? totalBackups : 0L) + 1;

        recordEvent("BACKUP_SCHEDULED", "Backup job scheduled: " + backup.getJobName(),
                "BACKUP", backup.getJobId());
    }

    /**
     * Complete backup
     */
    public void completeBackup(String jobId, boolean success) {
        BackupJob job = backupRegistry != null ? backupRegistry.get(jobId) : null;
        if (job != null) {
            job.setStatus(success ? BackupStatus.COMPLETED : BackupStatus.FAILED);
            job.setCompletedAt(LocalDateTime.now());

            if (job.getStartedAt() != null) {
                job.setDurationSeconds(
                    java.time.Duration.between(job.getStartedAt(), job.getCompletedAt()).getSeconds()
                );
            }

            if (success) {
                successfulBackups = (successfulBackups != null ? successfulBackups : 0L) + 1;
                lastBackupAt = LocalDateTime.now();
            } else {
                failedBackups = (failedBackups != null ? failedBackups : 0L) + 1;
            }
        }
    }

    /**
     * Add recovery site
     */
    public void addSite(RecoverySite site) {
        if (recoverySites == null) {
            recoverySites = new java.util.ArrayList<>();
        }
        recoverySites.add(site);

        if (siteRegistry == null) {
            siteRegistry = new java.util.HashMap<>();
        }
        siteRegistry.put(site.getSiteId(), site);

        if (Boolean.TRUE.equals(site.getIsPrimary())) {
            primarySite = site;
        }

        recordEvent("SITE_ADDED", "Recovery site added: " + site.getSiteName(),
                "SITE", site.getSiteId());
    }

    /**
     * Add replication stream
     */
    public void addReplication(ReplicationStream stream) {
        if (replications == null) {
            replications = new java.util.ArrayList<>();
        }
        replications.add(stream);

        if (replicationRegistry == null) {
            replicationRegistry = new java.util.HashMap<>();
        }
        replicationRegistry.put(stream.getStreamId(), stream);

        totalReplications = (totalReplications != null ? totalReplications : 0L) + 1;
        if ("ACTIVE".equals(stream.getStatus())) {
            activeReplications = (activeReplications != null ? activeReplications : 0L) + 1;
        }
    }

    /**
     * Initiate failover
     */
    public void initiateFailover(FailoverOperation failover) {
        if (failovers == null) {
            failovers = new java.util.ArrayList<>();
        }
        failovers.add(failover);

        if (failoverRegistry == null) {
            failoverRegistry = new java.util.HashMap<>();
        }
        failoverRegistry.put(failover.getFailoverId(), failover);

        totalFailovers = (totalFailovers != null ? totalFailovers : 0L) + 1;
        status = PlanStatus.FAILOVER_IN_PROGRESS;

        recordEvent("FAILOVER_INITIATED", "Failover initiated: " + failover.getTriggerReason(),
                "FAILOVER", failover.getFailoverId());
    }

    /**
     * Complete failover
     */
    public void completeFailover(String failoverId, boolean success) {
        FailoverOperation failover = failoverRegistry != null ? failoverRegistry.get(failoverId) : null;
        if (failover != null) {
            failover.setStatus(success ? FailoverStatus.COMPLETED : FailoverStatus.FAILED);
            failover.setCompletedAt(LocalDateTime.now());
            failover.setSuccessful(success);

            if (failover.getStartedAt() != null) {
                failover.setDurationSeconds(
                    java.time.Duration.between(failover.getStartedAt(), failover.getCompletedAt()).getSeconds()
                );
            }

            if (success) {
                successfulFailovers = (successfulFailovers != null ? successfulFailovers : 0L) + 1;
                status = PlanStatus.RECOVERED;
            }
        }
    }

    /**
     * Schedule recovery test
     */
    public void scheduleTest(RecoveryTest test) {
        if (recoveryTests == null) {
            recoveryTests = new java.util.ArrayList<>();
        }
        recoveryTests.add(test);

        if (testRegistry == null) {
            testRegistry = new java.util.HashMap<>();
        }
        testRegistry.put(test.getTestId(), test);

        totalTests = (totalTests != null ? totalTests : 0L) + 1;

        recordEvent("TEST_SCHEDULED", "Recovery test scheduled: " + test.getTestName(),
                "TEST", test.getTestId());
    }

    /**
     * Complete test
     */
    public void completeTest(String testId, boolean passed) {
        RecoveryTest test = testRegistry != null ? testRegistry.get(testId) : null;
        if (test != null) {
            test.setStatus(passed ? TestStatus.PASSED : TestStatus.FAILED);
            test.setCompletedAt(LocalDateTime.now());
            test.setPassed(passed);

            if (test.getStartedAt() != null) {
                test.setDurationSeconds(
                    java.time.Duration.between(test.getStartedAt(), test.getCompletedAt()).getSeconds()
                );
            }

            if (passed) {
                passedTests = (passedTests != null ? passedTests : 0L) + 1;
            }

            lastTestAt = LocalDateTime.now();
            lastTestDate = LocalDateTime.now();
        }
    }

    /**
     * Report incident
     */
    public void reportIncident(DisasterIncident incident) {
        if (incidents == null) {
            incidents = new java.util.ArrayList<>();
        }
        incidents.add(incident);

        if (incidentRegistry == null) {
            incidentRegistry = new java.util.HashMap<>();
        }
        incidentRegistry.put(incident.getIncidentId(), incident);

        totalIncidents = (totalIncidents != null ? totalIncidents : 0L) + 1;
        if ("ACTIVE".equals(incident.getStatus())) {
            activeIncidents = (activeIncidents != null ? activeIncidents : 0L) + 1;
        }

        recordEvent("INCIDENT_REPORTED", "Incident reported: " + incident.getIncidentType(),
                "INCIDENT", incident.getIncidentId());
    }

    /**
     * Resolve incident
     */
    public void resolveIncident(String incidentId, String resolution) {
        DisasterIncident incident = incidentRegistry != null ? incidentRegistry.get(incidentId) : null;
        if (incident != null) {
            incident.setStatus("RESOLVED");
            incident.setResolvedAt(LocalDateTime.now());
            incident.setResolution(resolution);

            if (activeIncidents != null && activeIncidents > 0) {
                activeIncidents--;
            }
            resolvedIncidents = (resolvedIncidents != null ? resolvedIncidents : 0L) + 1;
        }
    }

    /**
     * Create snapshot
     */
    public void createSnapshot(DataSnapshot snapshot) {
        if (snapshots == null) {
            snapshots = new java.util.ArrayList<>();
        }
        snapshots.add(snapshot);

        if (snapshotRegistry == null) {
            snapshotRegistry = new java.util.HashMap<>();
        }
        snapshotRegistry.put(snapshot.getSnapshotId(), snapshot);

        totalSnapshots = (totalSnapshots != null ? totalSnapshots : 0L) + 1;
    }

    /**
     * Add recovery procedure
     */
    public void addProcedure(RecoveryProcedure procedure) {
        if (procedures == null) {
            procedures = new java.util.ArrayList<>();
        }
        procedures.add(procedure);

        if (procedureRegistry == null) {
            procedureRegistry = new java.util.HashMap<>();
        }
        procedureRegistry.put(procedure.getProcedureId(), procedure);
    }

    /**
     * Record event
     */
    public void recordEvent(String eventType, String description, String resourceType, String resourceId) {
        if (events == null) {
            events = new java.util.ArrayList<>();
        }

        DREvent event = DREvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .eventType(eventType)
                .description(description)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .details(new java.util.HashMap<>())
                .build();

        events.add(event);
        lastEventAt = LocalDateTime.now();
    }

    /**
     * Check if plan is healthy
     */
    public boolean isHealthy() {
        return status == PlanStatus.ACTIVE && Boolean.TRUE.equals(isActive);
    }

    /**
     * Get active replications
     */
    public List<ReplicationStream> getActiveReplications() {
        if (replications == null) {
            return new java.util.ArrayList<>();
        }
        return replications.stream()
                .filter(r -> "ACTIVE".equals(r.getStatus()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get active incidents
     */
    public List<DisasterIncident> getActiveIncidentsList() {
        if (incidents == null) {
            return new java.util.ArrayList<>();
        }
        return incidents.stream()
                .filter(i -> "ACTIVE".equals(i.getStatus()))
                .collect(java.util.stream.Collectors.toList());
    }
}
