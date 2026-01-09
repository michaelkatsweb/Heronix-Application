package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Multi-tenancy & Isolation DTO
 *
 * Manages multi-tenant architecture with data isolation and resource allocation.
 *
 * Features:
 * - Tenant provisioning and management
 * - Data isolation strategies
 * - Resource quota management
 * - Tenant-specific configurations
 * - Cross-tenant analytics
 * - Tenant lifecycle management
 * - Billing and usage tracking
 * - Tenant migration support
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 109 - Report Multi-tenancy & Isolation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportMultiTenancy {

    // Platform Information
    private Long platformId;
    private String platformName;
    private String description;
    private PlatformStatus status;
    private Boolean isActive;

    // Isolation Strategy
    private IsolationStrategy isolationStrategy;
    private String isolationLevel;

    // Tenants
    private List<Tenant> tenants;
    private Map<String, Tenant> tenantRegistry;
    private Long totalTenants;
    private Long activeTenants;
    private Long suspendedTenants;

    // Resource Quotas
    private List<ResourceQuota> quotas;
    private Map<String, ResourceQuota> quotaRegistry;

    // Tenant Configurations
    private List<TenantConfiguration> configurations;
    private Map<String, TenantConfiguration> configRegistry;

    // Usage Tracking
    private List<UsageRecord> usageRecords;
    private Map<String, UsageRecord> usageRegistry;
    private Long totalUsageRecords;

    // Billing
    private List<BillingRecord> billingRecords;
    private Map<String, BillingRecord> billingRegistry;
    private Double totalRevenue;

    // Tenant Migrations
    private List<TenantMigration> migrations;
    private Map<String, TenantMigration> migrationRegistry;
    private Long totalMigrations;
    private Long completedMigrations;

    // Cross-Tenant Analytics
    private List<CrossTenantMetric> crossTenantMetrics;
    private Map<String, CrossTenantMetric> metricRegistry;

    // Tenant Groups
    private List<TenantGroup> tenantGroups;
    private Map<String, TenantGroup> groupRegistry;

    // Isolation Policies
    private List<IsolationPolicy> policies;
    private Map<String, IsolationPolicy> policyRegistry;

    // Metrics
    private MultiTenancyMetrics metrics;
    private LocalDateTime lastMetricsUpdate;

    // Events
    private List<TenancyEvent> events;
    private LocalDateTime lastEventAt;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime deployedAt;
    private LocalDateTime lastMaintenanceAt;

    /**
     * Platform Status
     */
    public enum PlatformStatus {
        INITIALIZING,
        DEPLOYING,
        ACTIVE,
        MAINTENANCE,
        DEGRADED,
        OFFLINE
    }

    /**
     * Isolation Strategy
     */
    public enum IsolationStrategy {
        DATABASE_PER_TENANT,
        SCHEMA_PER_TENANT,
        SHARED_SCHEMA,
        HYBRID,
        SILO,
        POOL,
        BRIDGE
    }

    /**
     * Tenant Status
     */
    public enum TenantStatus {
        PROVISIONING,
        ACTIVE,
        SUSPENDED,
        TRIAL,
        EXPIRED,
        MIGRATING,
        DEACTIVATED
    }

    /**
     * Tenant Tier
     */
    public enum TenantTier {
        FREE,
        BASIC,
        STANDARD,
        PROFESSIONAL,
        ENTERPRISE,
        CUSTOM
    }

    /**
     * Resource Type
     */
    public enum ResourceType {
        STORAGE,
        COMPUTE,
        BANDWIDTH,
        API_CALLS,
        USERS,
        REPORTS,
        CONCURRENT_SESSIONS,
        DATABASE_CONNECTIONS
    }

    /**
     * Migration Status
     */
    public enum MigrationStatus {
        PLANNED,
        IN_PROGRESS,
        VALIDATING,
        COMPLETED,
        FAILED,
        ROLLED_BACK
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
        private String displayName;
        private TenantStatus status;
        private TenantTier tier;
        private String domain;
        private String subdomain;
        private String contactEmail;
        private String contactPhone;
        private String primaryContact;
        private String billingContact;
        private String address;
        private String country;
        private String timezone;
        private String currency;
        private Integer userCount;
        private Integer maxUsers;
        private Long storageUsed;
        private Long storageQuota;
        private LocalDateTime createdAt;
        private LocalDateTime activatedAt;
        private LocalDateTime expiresAt;
        private LocalDateTime lastAccessAt;
        private Boolean isActive;
        private Map<String, String> customSettings;
        private Map<String, Object> metadata;
    }

    /**
     * Resource Quota
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceQuota {
        private String quotaId;
        private String tenantId;
        private ResourceType resourceType;
        private Long quota;
        private Long used;
        private String unit;
        private Boolean hardLimit;
        private Boolean alertEnabled;
        private Double alertThreshold;
        private LocalDateTime lastResetAt;
        private String resetPeriod;
        private Map<String, Object> metadata;
    }

    /**
     * Tenant Configuration
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenantConfiguration {
        private String configId;
        private String tenantId;
        private String configKey;
        private String configValue;
        private String configType;
        private String description;
        private Boolean encrypted;
        private Boolean editable;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String updatedBy;
        private Map<String, Object> metadata;
    }

    /**
     * Usage Record
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsageRecord {
        private String recordId;
        private String tenantId;
        private String tenantName;
        private ResourceType resourceType;
        private Long quantity;
        private String unit;
        private LocalDateTime recordedAt;
        private String period;
        private Double cost;
        private Map<String, Object> metadata;
    }

    /**
     * Billing Record
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillingRecord {
        private String billingId;
        private String tenantId;
        private String tenantName;
        private String invoiceNumber;
        private LocalDateTime billingPeriodStart;
        private LocalDateTime billingPeriodEnd;
        private Double amount;
        private String currency;
        private String status;
        private LocalDateTime dueDate;
        private LocalDateTime paidAt;
        private String paymentMethod;
        private List<String> lineItems;
        private Map<String, Object> metadata;
    }

    /**
     * Tenant Migration
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenantMigration {
        private String migrationId;
        private String tenantId;
        private String tenantName;
        private MigrationStatus status;
        private String sourceEnvironment;
        private String targetEnvironment;
        private String migrationType;
        private Long dataSize;
        private Long recordCount;
        private LocalDateTime scheduledAt;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private Long durationSeconds;
        private Double progressPercentage;
        private String errorMessage;
        private Boolean rollbackAvailable;
        private Map<String, Object> metadata;
    }

    /**
     * Cross-Tenant Metric
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CrossTenantMetric {
        private String metricId;
        private String metricName;
        private String metricType;
        private Double aggregateValue;
        private Double averageValue;
        private Double minValue;
        private Double maxValue;
        private Integer tenantCount;
        private LocalDateTime measuredAt;
        private String period;
        private Map<String, Double> tenantBreakdown;
        private Map<String, Object> metadata;
    }

    /**
     * Tenant Group
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenantGroup {
        private String groupId;
        private String groupName;
        private String description;
        private List<String> tenantIds;
        private Integer memberCount;
        private String groupType;
        private Map<String, String> sharedSettings;
        private LocalDateTime createdAt;
        private Map<String, Object> metadata;
    }

    /**
     * Isolation Policy
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IsolationPolicy {
        private String policyId;
        private String policyName;
        private String description;
        private Boolean enabled;
        private List<String> applicableTenants;
        private Map<String, Object> rules;
        private String enforcementLevel;
        private Integer priority;
        private LocalDateTime createdAt;
        private Map<String, Object> metadata;
    }

    /**
     * Multi-Tenancy Metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MultiTenancyMetrics {
        private Long totalTenants;
        private Long activeTenants;
        private Long suspendedTenants;
        private Long trialTenants;
        private Double tenantGrowthRate;
        private Double averageStoragePerTenant;
        private Double averageUsersPerTenant;
        private Long totalUsers;
        private Long totalStorage;
        private Long totalApiCalls;
        private Double platformUtilization;
        private Double averageResourceUtilization;
        private Long totalMigrations;
        private Long completedMigrations;
        private Double migrationSuccessRate;
        private Double totalRevenue;
        private Double averageRevenuePerTenant;
        private LocalDateTime measuredAt;
    }

    /**
     * Tenancy Event
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenancyEvent {
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
     * Deploy platform
     */
    public void deployPlatform() {
        this.status = PlatformStatus.DEPLOYING;
        this.isActive = true;
        this.deployedAt = LocalDateTime.now();

        recordEvent("PLATFORM_DEPLOYED", "Multi-tenancy platform deployed", "PLATFORM",
                platformId != null ? platformId.toString() : null);

        this.status = PlatformStatus.ACTIVE;
    }

    /**
     * Provision tenant
     */
    public void provisionTenant(Tenant tenant) {
        if (tenants == null) {
            tenants = new java.util.ArrayList<>();
        }
        tenants.add(tenant);

        if (tenantRegistry == null) {
            tenantRegistry = new java.util.HashMap<>();
        }
        tenantRegistry.put(tenant.getTenantId(), tenant);

        totalTenants = (totalTenants != null ? totalTenants : 0L) + 1;
        if (tenant.getStatus() == TenantStatus.ACTIVE) {
            activeTenants = (activeTenants != null ? activeTenants : 0L) + 1;
        }

        recordEvent("TENANT_PROVISIONED", "Tenant provisioned: " + tenant.getTenantName(),
                "TENANT", tenant.getTenantId());
    }

    /**
     * Add resource quota
     */
    public void addQuota(ResourceQuota quota) {
        if (quotas == null) {
            quotas = new java.util.ArrayList<>();
        }
        quotas.add(quota);

        if (quotaRegistry == null) {
            quotaRegistry = new java.util.HashMap<>();
        }
        quotaRegistry.put(quota.getQuotaId(), quota);
    }

    /**
     * Set tenant configuration
     */
    public void setConfiguration(TenantConfiguration config) {
        if (configurations == null) {
            configurations = new java.util.ArrayList<>();
        }
        configurations.add(config);

        if (configRegistry == null) {
            configRegistry = new java.util.HashMap<>();
        }
        configRegistry.put(config.getConfigId(), config);
    }

    /**
     * Record usage
     */
    public void recordUsage(UsageRecord usage) {
        if (usageRecords == null) {
            usageRecords = new java.util.ArrayList<>();
        }
        usageRecords.add(usage);

        if (usageRegistry == null) {
            usageRegistry = new java.util.HashMap<>();
        }
        usageRegistry.put(usage.getRecordId(), usage);

        totalUsageRecords = (totalUsageRecords != null ? totalUsageRecords : 0L) + 1;
    }

    /**
     * Create billing record
     */
    public void createBilling(BillingRecord billing) {
        if (billingRecords == null) {
            billingRecords = new java.util.ArrayList<>();
        }
        billingRecords.add(billing);

        if (billingRegistry == null) {
            billingRegistry = new java.util.HashMap<>();
        }
        billingRegistry.put(billing.getBillingId(), billing);

        if ("PAID".equals(billing.getStatus())) {
            totalRevenue = (totalRevenue != null ? totalRevenue : 0.0) + billing.getAmount();
        }
    }

    /**
     * Start migration
     */
    public void startMigration(TenantMigration migration) {
        if (migrations == null) {
            migrations = new java.util.ArrayList<>();
        }
        migrations.add(migration);

        if (migrationRegistry == null) {
            migrationRegistry = new java.util.HashMap<>();
        }
        migrationRegistry.put(migration.getMigrationId(), migration);

        totalMigrations = (totalMigrations != null ? totalMigrations : 0L) + 1;

        recordEvent("MIGRATION_STARTED", "Migration started for tenant: " + migration.getTenantName(),
                "MIGRATION", migration.getMigrationId());
    }

    /**
     * Complete migration
     */
    public void completeMigration(String migrationId, boolean success) {
        TenantMigration migration = migrationRegistry != null ? migrationRegistry.get(migrationId) : null;
        if (migration != null) {
            migration.setStatus(success ? MigrationStatus.COMPLETED : MigrationStatus.FAILED);
            migration.setCompletedAt(LocalDateTime.now());

            if (migration.getStartedAt() != null) {
                migration.setDurationSeconds(
                    java.time.Duration.between(migration.getStartedAt(), migration.getCompletedAt()).getSeconds()
                );
            }

            if (success) {
                completedMigrations = (completedMigrations != null ? completedMigrations : 0L) + 1;
            }
        }
    }

    /**
     * Add cross-tenant metric
     */
    public void addCrossTenantMetric(CrossTenantMetric metric) {
        if (crossTenantMetrics == null) {
            crossTenantMetrics = new java.util.ArrayList<>();
        }
        crossTenantMetrics.add(metric);

        if (metricRegistry == null) {
            metricRegistry = new java.util.HashMap<>();
        }
        metricRegistry.put(metric.getMetricId(), metric);
    }

    /**
     * Create tenant group
     */
    public void createGroup(TenantGroup group) {
        if (tenantGroups == null) {
            tenantGroups = new java.util.ArrayList<>();
        }
        tenantGroups.add(group);

        if (groupRegistry == null) {
            groupRegistry = new java.util.HashMap<>();
        }
        groupRegistry.put(group.getGroupId(), group);
    }

    /**
     * Add isolation policy
     */
    public void addPolicy(IsolationPolicy policy) {
        if (policies == null) {
            policies = new java.util.ArrayList<>();
        }
        policies.add(policy);

        if (policyRegistry == null) {
            policyRegistry = new java.util.HashMap<>();
        }
        policyRegistry.put(policy.getPolicyId(), policy);
    }

    /**
     * Suspend tenant
     */
    public void suspendTenant(String tenantId, String reason) {
        Tenant tenant = tenantRegistry != null ? tenantRegistry.get(tenantId) : null;
        if (tenant != null) {
            tenant.setStatus(TenantStatus.SUSPENDED);
            tenant.setIsActive(false);

            if (activeTenants != null && activeTenants > 0) {
                activeTenants--;
            }
            suspendedTenants = (suspendedTenants != null ? suspendedTenants : 0L) + 1;

            recordEvent("TENANT_SUSPENDED", "Tenant suspended: " + reason, "TENANT", tenantId);
        }
    }

    /**
     * Activate tenant
     */
    public void activateTenant(String tenantId) {
        Tenant tenant = tenantRegistry != null ? tenantRegistry.get(tenantId) : null;
        if (tenant != null) {
            tenant.setStatus(TenantStatus.ACTIVE);
            tenant.setIsActive(true);
            tenant.setActivatedAt(LocalDateTime.now());

            activeTenants = (activeTenants != null ? activeTenants : 0L) + 1;
            if (suspendedTenants != null && suspendedTenants > 0) {
                suspendedTenants--;
            }

            recordEvent("TENANT_ACTIVATED", "Tenant activated", "TENANT", tenantId);
        }
    }

    /**
     * Get tenant by ID
     */
    public Tenant getTenant(String tenantId) {
        return tenantRegistry != null ? tenantRegistry.get(tenantId) : null;
    }

    /**
     * Record event
     */
    public void recordEvent(String eventType, String description, String resourceType, String resourceId) {
        if (events == null) {
            events = new java.util.ArrayList<>();
        }

        TenancyEvent event = TenancyEvent.builder()
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
     * Check if platform is healthy
     */
    public boolean isHealthy() {
        return status == PlatformStatus.ACTIVE;
    }

    /**
     * Get active tenants list
     */
    public List<Tenant> getActiveTenantsList() {
        if (tenants == null) {
            return new java.util.ArrayList<>();
        }
        return tenants.stream()
                .filter(t -> t.getStatus() == TenantStatus.ACTIVE)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get tenants by tier
     */
    public List<Tenant> getTenantsByTier(TenantTier tier) {
        if (tenants == null) {
            return new java.util.ArrayList<>();
        }
        return tenants.stream()
                .filter(t -> t.getTier() == tier)
                .collect(java.util.stream.Collectors.toList());
    }
}
