package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Federation DTO
 *
 * Represents federated reporting and multi-tenant capabilities.
 *
 * Features:
 * - Multi-tenant isolation
 * - Cross-tenant report sharing
 * - Federated queries
 * - Tenant-specific customization
 * - Data aggregation across tenants
 * - Access control and permissions
 * - Tenant quotas and limits
 * - Federation synchronization
 *
 * Federation Types:
 * - ISOLATED - Complete tenant isolation
 * - SHARED - Shared reports with access control
 * - FEDERATED - Cross-tenant federated queries
 * - AGGREGATED - Aggregated data from multiple tenants
 * - HYBRID - Mixed isolation and sharing
 *
 * Isolation Levels:
 * - STRICT - Complete data isolation
 * - MODERATE - Controlled sharing
 * - RELAXED - Open sharing with audit
 * - NONE - No isolation (single tenant)
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 87 - Report Federation & Multi-Tenant
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportFederation {

    private Long federationId;
    private String federationName;
    private String description;
    private FederationType federationType;
    private IsolationLevel isolationLevel;

    // Federation configuration
    private Boolean federationEnabled;
    private Boolean crossTenantSharingEnabled;
    private Boolean federatedQueriesEnabled;
    private Boolean dataAggregationEnabled;
    private Boolean tenantCustomizationEnabled;

    // Tenant management
    private List<Tenant> tenants;
    private Integer totalTenants;
    private Integer activeTenants;
    private Integer suspendedTenants;
    private Map<String, Tenant> tenantMap;

    // Report sharing
    private List<SharedReport> sharedReports;
    private Integer totalSharedReports;
    private Map<String, List<String>> reportAccessMap; // reportId -> List<tenantId>

    // Federation sync
    private Boolean syncEnabled;
    private SyncStrategy syncStrategy;
    private Integer syncIntervalMinutes;
    private LocalDateTime lastSyncAt;
    private LocalDateTime nextSyncAt;
    private SyncStatus syncStatus;

    // Access control
    private List<FederationPolicy> policies;
    private Boolean roleBasedAccessEnabled;
    private Boolean attributeBasedAccessEnabled;
    private Map<String, List<String>> tenantRoles; // tenantId -> List<role>

    // Quotas and limits
    private Boolean quotasEnabled;
    private Map<String, TenantQuota> tenantQuotas;
    private Boolean enforceLimits;
    private Boolean alertOnLimitExceeded;

    // Performance
    private Boolean cachingEnabled;
    private Integer cacheExpiryMinutes;
    private Boolean queryOptimizationEnabled;
    private Boolean parallelExecutionEnabled;
    private Integer maxConcurrentQueries;

    // Metadata
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime lastModifiedAt;
    private String lastModifiedBy;

    /**
     * Federation Type
     */
    public enum FederationType {
        ISOLATED,    // Complete tenant isolation
        SHARED,      // Shared reports with access control
        FEDERATED,   // Cross-tenant federated queries
        AGGREGATED,  // Aggregated data from multiple tenants
        HYBRID       // Mixed isolation and sharing
    }

    /**
     * Isolation Level
     */
    public enum IsolationLevel {
        STRICT,      // Complete data isolation
        MODERATE,    // Controlled sharing
        RELAXED,     // Open sharing with audit
        NONE         // No isolation (single tenant)
    }

    /**
     * Sync Strategy
     */
    public enum SyncStrategy {
        REALTIME,    // Real-time synchronization
        SCHEDULED,   // Scheduled synchronization
        ON_DEMAND,   // Manual synchronization
        BATCH,       // Batch synchronization
        INCREMENTAL  // Incremental synchronization
    }

    /**
     * Sync Status
     */
    public enum SyncStatus {
        IDLE,
        SYNCING,
        COMPLETED,
        FAILED,
        PARTIAL
    }

    /**
     * Tenant Status
     */
    public enum TenantStatus {
        ACTIVE,
        SUSPENDED,
        INACTIVE,
        PENDING,
        DELETED
    }

    /**
     * Share Scope
     */
    public enum ShareScope {
        PRIVATE,       // Private to tenant
        ORGANIZATION,  // Shared within organization
        FEDERATION,    // Shared across federation
        PUBLIC         // Public access
    }

    /**
     * Tenant
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Tenant {
        private String tenantId;
        private String tenantName;
        private String organizationName;
        private TenantStatus status;
        private IsolationLevel isolationLevel;
        private LocalDateTime joinedAt;
        private LocalDateTime lastActiveAt;
        private Long reportCount;
        private Long userCount;
        private Long dataVolumeBytes;
        private Boolean quotaEnabled;
        private TenantQuota quota;
        private Map<String, Object> customSettings;
        private List<String> allowedTenants; // For cross-tenant sharing
        private Boolean canShareReports;
        private Boolean canAccessFederatedData;
    }

    /**
     * Shared Report
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SharedReport {
        private String shareId;
        private String reportId;
        private String reportName;
        private String ownerTenantId;
        private String ownerTenantName;
        private ShareScope shareScope;
        private List<String> sharedWithTenants;
        private List<String> allowedRoles;
        private LocalDateTime sharedAt;
        private String sharedBy;
        private LocalDateTime expiresAt;
        private Boolean expired;
        private Integer accessCount;
        private LocalDateTime lastAccessedAt;
        private Boolean allowModification;
        private Boolean allowExport;
        private Boolean trackAccess;
        private Map<String, Object> sharingRules;
    }

    /**
     * Federation Policy
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FederationPolicy {
        private String policyId;
        private String policyName;
        private String description;
        private String policyType;
        private Boolean enabled;
        private Integer priority;
        private String condition;
        private String action;
        private List<String> applicableTenants;
        private Map<String, Object> parameters;
        private LocalDateTime createdAt;
        private String createdBy;
    }

    /**
     * Tenant Quota
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenantQuota {
        private String tenantId;
        private Long maxReports;
        private Long currentReports;
        private Long maxUsers;
        private Long currentUsers;
        private Long maxDataVolumeBytes;
        private Long currentDataVolumeBytes;
        private Integer maxQueriesPerHour;
        private Integer currentQueriesThisHour;
        private Integer maxConcurrentQueries;
        private Integer currentConcurrentQueries;
        private Long maxStorageBytes;
        private Long currentStorageBytes;
        private Boolean quotaExceeded;
        private List<String> exceededLimits;
        private LocalDateTime quotaResetAt;
    }

    /**
     * Federated Query
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FederatedQuery {
        private String queryId;
        private String queryName;
        private List<String> sourceTenants;
        private String query;
        private String aggregationType;
        private Map<String, Object> filters;
        private LocalDateTime executedAt;
        private String executedBy;
        private Long executionTimeMs;
        private Integer resultCount;
        private Boolean cached;
        private Map<String, Object> results;
    }

    /**
     * Tenant Access Log
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenantAccessLog {
        private String logId;
        private String tenantId;
        private String reportId;
        private String userId;
        private String action;
        private LocalDateTime timestamp;
        private Boolean success;
        private String errorMessage;
        private Map<String, Object> metadata;
    }

    /**
     * Sync Event
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncEvent {
        private String eventId;
        private String sourceTenantId;
        private String targetTenantId;
        private String entityType;
        private String entityId;
        private String syncAction;
        private LocalDateTime timestamp;
        private Boolean success;
        private String errorMessage;
        private Long durationMs;
    }

    /**
     * Helper Methods
     */

    public void addTenant(Tenant tenant) {
        if (tenants == null) {
            tenants = new ArrayList<>();
        }
        tenants.add(tenant);

        if (tenantMap == null) {
            tenantMap = new HashMap<>();
        }
        tenantMap.put(tenant.getTenantId(), tenant);

        totalTenants = (totalTenants != null ? totalTenants : 0) + 1;
        if (tenant.getStatus() == TenantStatus.ACTIVE) {
            activeTenants = (activeTenants != null ? activeTenants : 0) + 1;
        } else if (tenant.getStatus() == TenantStatus.SUSPENDED) {
            suspendedTenants = (suspendedTenants != null ? suspendedTenants : 0) + 1;
        }
    }

    public void removeTenant(String tenantId) {
        if (tenants != null) {
            Tenant tenant = getTenant(tenantId);
            if (tenant != null) {
                tenants.remove(tenant);
                totalTenants = (totalTenants != null ? totalTenants : 0) - 1;

                if (tenant.getStatus() == TenantStatus.ACTIVE) {
                    activeTenants = (activeTenants != null ? activeTenants : 0) - 1;
                } else if (tenant.getStatus() == TenantStatus.SUSPENDED) {
                    suspendedTenants = (suspendedTenants != null ? suspendedTenants : 0) - 1;
                }
            }
        }

        if (tenantMap != null) {
            tenantMap.remove(tenantId);
        }
    }

    public Tenant getTenant(String tenantId) {
        if (tenantMap != null) {
            return tenantMap.get(tenantId);
        }
        if (tenants != null) {
            return tenants.stream()
                    .filter(t -> t.getTenantId().equals(tenantId))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public void shareReport(SharedReport sharedReport) {
        if (sharedReports == null) {
            sharedReports = new ArrayList<>();
        }
        sharedReports.add(sharedReport);
        totalSharedReports = (totalSharedReports != null ? totalSharedReports : 0) + 1;

        if (reportAccessMap == null) {
            reportAccessMap = new HashMap<>();
        }
        reportAccessMap.put(sharedReport.getReportId(),
                sharedReport.getSharedWithTenants() != null ?
                new ArrayList<>(sharedReport.getSharedWithTenants()) : new ArrayList<>());
    }

    public void unshareReport(String shareId) {
        if (sharedReports != null) {
            SharedReport report = getSharedReport(shareId);
            if (report != null) {
                sharedReports.remove(report);
                totalSharedReports = (totalSharedReports != null ? totalSharedReports : 0) - 1;

                if (reportAccessMap != null) {
                    reportAccessMap.remove(report.getReportId());
                }
            }
        }
    }

    public SharedReport getSharedReport(String shareId) {
        if (sharedReports != null) {
            return sharedReports.stream()
                    .filter(r -> r.getShareId().equals(shareId))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public boolean canAccessReport(String tenantId, String reportId) {
        if (reportAccessMap != null && reportAccessMap.containsKey(reportId)) {
            return reportAccessMap.get(reportId).contains(tenantId);
        }
        return false;
    }

    public void addPolicy(FederationPolicy policy) {
        if (policies == null) {
            policies = new ArrayList<>();
        }
        policies.add(policy);
    }

    public void removePolicy(String policyId) {
        if (policies != null) {
            policies.removeIf(p -> p.getPolicyId().equals(policyId));
        }
    }

    public FederationPolicy getPolicy(String policyId) {
        if (policies != null) {
            return policies.stream()
                    .filter(p -> p.getPolicyId().equals(policyId))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public void setTenantQuota(String tenantId, TenantQuota quota) {
        if (tenantQuotas == null) {
            tenantQuotas = new HashMap<>();
        }
        tenantQuotas.put(tenantId, quota);
    }

    public TenantQuota getTenantQuota(String tenantId) {
        if (tenantQuotas != null) {
            return tenantQuotas.get(tenantId);
        }
        return null;
    }

    public boolean isQuotaExceeded(String tenantId) {
        TenantQuota quota = getTenantQuota(tenantId);
        return quota != null && Boolean.TRUE.equals(quota.getQuotaExceeded());
    }

    public void checkQuota(String tenantId) {
        TenantQuota quota = getTenantQuota(tenantId);
        if (quota == null) {
            return;
        }

        List<String> exceeded = new ArrayList<>();

        if (quota.getMaxReports() != null && quota.getCurrentReports() != null &&
                quota.getCurrentReports() >= quota.getMaxReports()) {
            exceeded.add("reports");
        }

        if (quota.getMaxUsers() != null && quota.getCurrentUsers() != null &&
                quota.getCurrentUsers() >= quota.getMaxUsers()) {
            exceeded.add("users");
        }

        if (quota.getMaxDataVolumeBytes() != null && quota.getCurrentDataVolumeBytes() != null &&
                quota.getCurrentDataVolumeBytes() >= quota.getMaxDataVolumeBytes()) {
            exceeded.add("dataVolume");
        }

        if (quota.getMaxQueriesPerHour() != null && quota.getCurrentQueriesThisHour() != null &&
                quota.getCurrentQueriesThisHour() >= quota.getMaxQueriesPerHour()) {
            exceeded.add("queriesPerHour");
        }

        if (quota.getMaxStorageBytes() != null && quota.getCurrentStorageBytes() != null &&
                quota.getCurrentStorageBytes() >= quota.getMaxStorageBytes()) {
            exceeded.add("storage");
        }

        quota.setExceededLimits(exceeded);
        quota.setQuotaExceeded(!exceeded.isEmpty());
    }

    public void updateSyncStatus(SyncStatus status) {
        this.syncStatus = status;
        if (status == SyncStatus.COMPLETED || status == SyncStatus.FAILED) {
            this.lastSyncAt = LocalDateTime.now();
            scheduleNextSync();
        }
    }

    public void scheduleNextSync() {
        if (syncIntervalMinutes != null && Boolean.TRUE.equals(syncEnabled)) {
            nextSyncAt = LocalDateTime.now().plusMinutes(syncIntervalMinutes);
        }
    }

    public boolean needsSync() {
        return Boolean.TRUE.equals(syncEnabled) &&
                nextSyncAt != null &&
                LocalDateTime.now().isAfter(nextSyncAt);
    }

    public List<SharedReport> getSharedReportsForTenant(String tenantId) {
        if (sharedReports == null) {
            return new ArrayList<>();
        }
        return sharedReports.stream()
                .filter(r -> r.getOwnerTenantId().equals(tenantId) ||
                        (r.getSharedWithTenants() != null && r.getSharedWithTenants().contains(tenantId)))
                .toList();
    }

    public List<Tenant> getActiveTenants() {
        if (tenants == null) {
            return new ArrayList<>();
        }
        return tenants.stream()
                .filter(t -> t.getStatus() == TenantStatus.ACTIVE)
                .toList();
    }

    public void updateTenantStatus(String tenantId, TenantStatus status) {
        Tenant tenant = getTenant(tenantId);
        if (tenant != null) {
            TenantStatus oldStatus = tenant.getStatus();
            tenant.setStatus(status);

            // Update counts
            if (oldStatus == TenantStatus.ACTIVE) {
                activeTenants = (activeTenants != null ? activeTenants : 0) - 1;
            } else if (oldStatus == TenantStatus.SUSPENDED) {
                suspendedTenants = (suspendedTenants != null ? suspendedTenants : 0) - 1;
            }

            if (status == TenantStatus.ACTIVE) {
                activeTenants = (activeTenants != null ? activeTenants : 0) + 1;
            } else if (status == TenantStatus.SUSPENDED) {
                suspendedTenants = (suspendedTenants != null ? suspendedTenants : 0) + 1;
            }
        }
    }

    public boolean isFederationActive() {
        return Boolean.TRUE.equals(federationEnabled) && activeTenants != null && activeTenants > 0;
    }
}
