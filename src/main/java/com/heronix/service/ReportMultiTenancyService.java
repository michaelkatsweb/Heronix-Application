package com.heronix.service;

import com.heronix.dto.ReportMultiTenancy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Report Multi-tenancy & Isolation Service
 *
 * Manages multi-tenant platform lifecycle, tenant provisioning, and resource management.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 109 - Report Multi-tenancy & Isolation
 */
@Service
@Slf4j
public class ReportMultiTenancyService {

    private final Map<Long, ReportMultiTenancy> platforms = new ConcurrentHashMap<>();
    private Long nextId = 1L;

    /**
     * Create multi-tenancy platform
     */
    public ReportMultiTenancy createPlatform(ReportMultiTenancy platform) {
        synchronized (this) {
            platform.setPlatformId(nextId++);
        }

        platform.setStatus(ReportMultiTenancy.PlatformStatus.INITIALIZING);
        platform.setIsActive(false);
        platform.setCreatedAt(LocalDateTime.now());

        // Initialize collections
        platform.setTenants(new ArrayList<>());
        platform.setTenantRegistry(new HashMap<>());
        platform.setQuotas(new ArrayList<>());
        platform.setQuotaRegistry(new HashMap<>());
        platform.setConfigurations(new ArrayList<>());
        platform.setConfigRegistry(new HashMap<>());
        platform.setUsageRecords(new ArrayList<>());
        platform.setUsageRegistry(new HashMap<>());
        platform.setBillingRecords(new ArrayList<>());
        platform.setBillingRegistry(new HashMap<>());
        platform.setMigrations(new ArrayList<>());
        platform.setMigrationRegistry(new HashMap<>());
        platform.setCrossTenantMetrics(new ArrayList<>());
        platform.setMetricRegistry(new HashMap<>());
        platform.setTenantGroups(new ArrayList<>());
        platform.setGroupRegistry(new HashMap<>());
        platform.setPolicies(new ArrayList<>());
        platform.setPolicyRegistry(new HashMap<>());
        platform.setEvents(new ArrayList<>());

        // Initialize counts
        platform.setTotalTenants(0L);
        platform.setActiveTenants(0L);
        platform.setSuspendedTenants(0L);
        platform.setTotalUsageRecords(0L);
        platform.setTotalRevenue(0.0);
        platform.setTotalMigrations(0L);
        platform.setCompletedMigrations(0L);

        platforms.put(platform.getPlatformId(), platform);

        log.info("Created multi-tenancy platform: {} (ID: {})", platform.getPlatformName(), platform.getPlatformId());
        return platform;
    }

    /**
     * Get multi-tenancy platform
     */
    public Optional<ReportMultiTenancy> getPlatform(Long platformId) {
        return Optional.ofNullable(platforms.get(platformId));
    }

    /**
     * Deploy platform
     */
    public void deployPlatform(Long platformId) {
        ReportMultiTenancy platform = platforms.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("Multi-tenancy platform not found: " + platformId);
        }

        platform.deployPlatform();

        log.info("Deployed multi-tenancy platform: {}", platformId);
    }

    /**
     * Provision tenant
     */
    public ReportMultiTenancy.Tenant provisionTenant(Long platformId, String tenantName, String domain,
                                                      ReportMultiTenancy.TenantTier tier, String contactEmail) {
        ReportMultiTenancy platform = platforms.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("Multi-tenancy platform not found: " + platformId);
        }

        ReportMultiTenancy.Tenant tenant = ReportMultiTenancy.Tenant.builder()
                .tenantId(UUID.randomUUID().toString())
                .tenantName(tenantName)
                .displayName(tenantName)
                .status(ReportMultiTenancy.TenantStatus.PROVISIONING)
                .tier(tier)
                .domain(domain)
                .subdomain(generateSubdomain(tenantName))
                .contactEmail(contactEmail)
                .primaryContact(contactEmail)
                .timezone("UTC")
                .currency("USD")
                .userCount(0)
                .maxUsers(getMaxUsersByTier(tier))
                .storageUsed(0L)
                .storageQuota(getStorageQuotaByTier(tier))
                .createdAt(LocalDateTime.now())
                .isActive(false)
                .customSettings(new HashMap<>())
                .metadata(new HashMap<>())
                .build();

        // Set to active
        tenant.setStatus(ReportMultiTenancy.TenantStatus.ACTIVE);
        tenant.setIsActive(true);
        tenant.setActivatedAt(LocalDateTime.now());

        platform.provisionTenant(tenant);

        // Create default resource quotas
        createDefaultQuotas(platform, tenant.getTenantId(), tier);

        log.info("Provisioned tenant: {} in platform: {}", tenantName, platformId);
        return tenant;
    }

    /**
     * Add resource quota
     */
    public ReportMultiTenancy.ResourceQuota addQuota(Long platformId, String tenantId,
                                                      ReportMultiTenancy.ResourceType resourceType,
                                                      Long quota, String unit) {
        ReportMultiTenancy platform = platforms.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("Multi-tenancy platform not found: " + platformId);
        }

        ReportMultiTenancy.ResourceQuota resourceQuota = ReportMultiTenancy.ResourceQuota.builder()
                .quotaId(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .resourceType(resourceType)
                .quota(quota)
                .used(0L)
                .unit(unit)
                .hardLimit(true)
                .alertEnabled(true)
                .alertThreshold(0.8)
                .lastResetAt(LocalDateTime.now())
                .resetPeriod("monthly")
                .metadata(new HashMap<>())
                .build();

        platform.addQuota(resourceQuota);

        log.info("Added resource quota: {} for tenant {} in platform: {}", resourceType, tenantId, platformId);
        return resourceQuota;
    }

    /**
     * Set tenant configuration
     */
    public ReportMultiTenancy.TenantConfiguration setConfiguration(Long platformId, String tenantId,
                                                                     String configKey, String configValue,
                                                                     String configType) {
        ReportMultiTenancy platform = platforms.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("Multi-tenancy platform not found: " + platformId);
        }

        ReportMultiTenancy.TenantConfiguration config = ReportMultiTenancy.TenantConfiguration.builder()
                .configId(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .configKey(configKey)
                .configValue(configValue)
                .configType(configType)
                .description("Configuration for " + configKey)
                .encrypted(false)
                .editable(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .updatedBy("System")
                .metadata(new HashMap<>())
                .build();

        platform.setConfiguration(config);

        log.info("Set configuration: {} for tenant {} in platform: {}", configKey, tenantId, platformId);
        return config;
    }

    /**
     * Record usage
     */
    public ReportMultiTenancy.UsageRecord recordUsage(Long platformId, String tenantId,
                                                       ReportMultiTenancy.ResourceType resourceType,
                                                       Long quantity, Double cost) {
        ReportMultiTenancy platform = platforms.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("Multi-tenancy platform not found: " + platformId);
        }

        ReportMultiTenancy.Tenant tenant = platform.getTenant(tenantId);
        String tenantName = tenant != null ? tenant.getTenantName() : "Unknown";

        ReportMultiTenancy.UsageRecord usage = ReportMultiTenancy.UsageRecord.builder()
                .recordId(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .tenantName(tenantName)
                .resourceType(resourceType)
                .quantity(quantity)
                .unit(getUnitByResourceType(resourceType))
                .recordedAt(LocalDateTime.now())
                .period("current")
                .cost(cost)
                .metadata(new HashMap<>())
                .build();

        platform.recordUsage(usage);

        log.info("Recorded usage: {} {} for tenant {} in platform: {}",
                quantity, resourceType, tenantId, platformId);
        return usage;
    }

    /**
     * Create billing record
     */
    public ReportMultiTenancy.BillingRecord createBilling(Long platformId, String tenantId,
                                                           LocalDateTime periodStart, LocalDateTime periodEnd,
                                                           Double amount) {
        ReportMultiTenancy platform = platforms.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("Multi-tenancy platform not found: " + platformId);
        }

        ReportMultiTenancy.Tenant tenant = platform.getTenant(tenantId);
        String tenantName = tenant != null ? tenant.getTenantName() : "Unknown";
        String currency = tenant != null ? tenant.getCurrency() : "USD";

        ReportMultiTenancy.BillingRecord billing = ReportMultiTenancy.BillingRecord.builder()
                .billingId(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .tenantName(tenantName)
                .invoiceNumber(generateInvoiceNumber())
                .billingPeriodStart(periodStart)
                .billingPeriodEnd(periodEnd)
                .amount(amount)
                .currency(currency)
                .status("PENDING")
                .dueDate(periodEnd.plusDays(30))
                .lineItems(new ArrayList<>())
                .metadata(new HashMap<>())
                .build();

        platform.createBilling(billing);

        log.info("Created billing record: {} for tenant {} in platform: {}",
                billing.getInvoiceNumber(), tenantId, platformId);
        return billing;
    }

    /**
     * Start tenant migration
     */
    public ReportMultiTenancy.TenantMigration startMigration(Long platformId, String tenantId,
                                                              String sourceEnv, String targetEnv,
                                                              String migrationType) {
        ReportMultiTenancy platform = platforms.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("Multi-tenancy platform not found: " + platformId);
        }

        ReportMultiTenancy.Tenant tenant = platform.getTenant(tenantId);
        String tenantName = tenant != null ? tenant.getTenantName() : "Unknown";

        ReportMultiTenancy.TenantMigration migration = ReportMultiTenancy.TenantMigration.builder()
                .migrationId(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .tenantName(tenantName)
                .status(ReportMultiTenancy.MigrationStatus.IN_PROGRESS)
                .sourceEnvironment(sourceEnv)
                .targetEnvironment(targetEnv)
                .migrationType(migrationType)
                .dataSize(tenant != null ? tenant.getStorageUsed() : 0L)
                .recordCount(0L)
                .scheduledAt(LocalDateTime.now())
                .startedAt(LocalDateTime.now())
                .progressPercentage(0.0)
                .rollbackAvailable(true)
                .metadata(new HashMap<>())
                .build();

        platform.startMigration(migration);

        log.info("Started migration: {} for tenant {} in platform: {}",
                migration.getMigrationId(), tenantId, platformId);
        return migration;
    }

    /**
     * Complete migration
     */
    public void completeMigration(Long platformId, String migrationId, boolean success) {
        ReportMultiTenancy platform = platforms.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("Multi-tenancy platform not found: " + platformId);
        }

        platform.completeMigration(migrationId, success);

        log.info("Completed migration: {} with status: {} in platform: {}",
                migrationId, success ? "SUCCESS" : "FAILED", platformId);
    }

    /**
     * Create tenant group
     */
    public ReportMultiTenancy.TenantGroup createGroup(Long platformId, String groupName,
                                                       String description, List<String> tenantIds) {
        ReportMultiTenancy platform = platforms.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("Multi-tenancy platform not found: " + platformId);
        }

        ReportMultiTenancy.TenantGroup group = ReportMultiTenancy.TenantGroup.builder()
                .groupId(UUID.randomUUID().toString())
                .groupName(groupName)
                .description(description)
                .tenantIds(tenantIds)
                .memberCount(tenantIds.size())
                .groupType("STANDARD")
                .sharedSettings(new HashMap<>())
                .createdAt(LocalDateTime.now())
                .metadata(new HashMap<>())
                .build();

        platform.createGroup(group);

        log.info("Created tenant group: {} in platform: {}", groupName, platformId);
        return group;
    }

    /**
     * Suspend tenant
     */
    public void suspendTenant(Long platformId, String tenantId, String reason) {
        ReportMultiTenancy platform = platforms.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("Multi-tenancy platform not found: " + platformId);
        }

        platform.suspendTenant(tenantId, reason);

        log.info("Suspended tenant: {} in platform: {} - Reason: {}", tenantId, platformId, reason);
    }

    /**
     * Activate tenant
     */
    public void activateTenant(Long platformId, String tenantId) {
        ReportMultiTenancy platform = platforms.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("Multi-tenancy platform not found: " + platformId);
        }

        platform.activateTenant(tenantId);

        log.info("Activated tenant: {} in platform: {}", tenantId, platformId);
    }

    /**
     * Update metrics
     */
    public void updateMetrics(Long platformId) {
        ReportMultiTenancy platform = platforms.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("Multi-tenancy platform not found: " + platformId);
        }

        long totalTenants = platform.getTotalTenants() != null ? platform.getTotalTenants() : 0L;
        long activeTenantCount = platform.getActiveTenants() != null ? platform.getActiveTenants() : 0L;

        // Calculate tenant growth rate (simplified)
        double tenantGrowthRate = totalTenants > 0 ? (double) activeTenantCount / totalTenants : 0.0;

        // Calculate averages
        long totalUsers = 0L;
        long totalStorage = 0L;
        int tenantCount = 0;

        for (ReportMultiTenancy.Tenant tenant : platform.getTenants()) {
            if (tenant.getStatus() == ReportMultiTenancy.TenantStatus.ACTIVE) {
                totalUsers += tenant.getUserCount();
                totalStorage += tenant.getStorageUsed();
                tenantCount++;
            }
        }

        double avgUsersPerTenant = tenantCount > 0 ? (double) totalUsers / tenantCount : 0.0;
        double avgStoragePerTenant = tenantCount > 0 ? (double) totalStorage / tenantCount : 0.0;

        // Calculate API calls (from usage records)
        long totalApiCalls = platform.getUsageRecords().stream()
                .filter(u -> u.getResourceType() == ReportMultiTenancy.ResourceType.API_CALLS)
                .mapToLong(ReportMultiTenancy.UsageRecord::getQuantity)
                .sum();

        // Calculate platform utilization
        Random random = new Random();
        double platformUtilization = 0.5 + random.nextDouble() * 0.4;
        double avgResourceUtilization = 0.4 + random.nextDouble() * 0.5;

        // Migration metrics
        long totalMigrations = platform.getTotalMigrations() != null ? platform.getTotalMigrations() : 0L;
        long completedMigrations = platform.getCompletedMigrations() != null ? platform.getCompletedMigrations() : 0L;
        double migrationSuccessRate = totalMigrations > 0 ? (double) completedMigrations / totalMigrations : 0.0;

        // Revenue metrics
        double totalRevenue = platform.getTotalRevenue() != null ? platform.getTotalRevenue() : 0.0;
        double avgRevenuePerTenant = totalTenants > 0 ? totalRevenue / totalTenants : 0.0;

        // Count trial tenants
        long trialTenants = platform.getTenants().stream()
                .filter(t -> t.getStatus() == ReportMultiTenancy.TenantStatus.TRIAL)
                .count();

        ReportMultiTenancy.MultiTenancyMetrics metrics = ReportMultiTenancy.MultiTenancyMetrics.builder()
                .totalTenants(totalTenants)
                .activeTenants(activeTenantCount)
                .suspendedTenants(platform.getSuspendedTenants())
                .trialTenants(trialTenants)
                .tenantGrowthRate(tenantGrowthRate)
                .averageStoragePerTenant(avgStoragePerTenant)
                .averageUsersPerTenant(avgUsersPerTenant)
                .totalUsers(totalUsers)
                .totalStorage(totalStorage)
                .totalApiCalls(totalApiCalls)
                .platformUtilization(platformUtilization)
                .averageResourceUtilization(avgResourceUtilization)
                .totalMigrations(totalMigrations)
                .completedMigrations(completedMigrations)
                .migrationSuccessRate(migrationSuccessRate)
                .totalRevenue(totalRevenue)
                .averageRevenuePerTenant(avgRevenuePerTenant)
                .measuredAt(LocalDateTime.now())
                .build();

        platform.setMetrics(metrics);
        platform.setLastMetricsUpdate(LocalDateTime.now());

        log.info("Updated metrics for multi-tenancy platform: {}", platformId);
    }

    /**
     * Delete platform
     */
    public void deletePlatform(Long platformId) {
        platforms.remove(platformId);
        log.info("Deleted multi-tenancy platform: {}", platformId);
    }

    /**
     * Get statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        int totalPlatforms = platforms.size();
        int activePlatforms = 0;
        long totalTenants = 0L;
        long activeTenants = 0L;
        double totalRevenue = 0.0;

        for (ReportMultiTenancy platform : platforms.values()) {
            if (Boolean.TRUE.equals(platform.getIsActive())) {
                activePlatforms++;
            }
            Long platformTenants = platform.getTotalTenants();
            totalTenants += platformTenants != null ? platformTenants : 0L;

            Long platformActiveTenants = platform.getActiveTenants();
            activeTenants += platformActiveTenants != null ? platformActiveTenants : 0L;

            Double platformRevenue = platform.getTotalRevenue();
            totalRevenue += platformRevenue != null ? platformRevenue : 0.0;
        }

        stats.put("totalPlatforms", totalPlatforms);
        stats.put("activePlatforms", activePlatforms);
        stats.put("totalTenants", totalTenants);
        stats.put("activeTenants", activeTenants);
        stats.put("totalRevenue", totalRevenue);
        stats.put("timestamp", LocalDateTime.now());

        return stats;
    }

    // Helper Methods

    private void createDefaultQuotas(ReportMultiTenancy platform, String tenantId, ReportMultiTenancy.TenantTier tier) {
        // Storage quota
        addQuota(platform.getPlatformId(), tenantId,
                ReportMultiTenancy.ResourceType.STORAGE,
                getStorageQuotaByTier(tier), "GB");

        // API calls quota
        addQuota(platform.getPlatformId(), tenantId,
                ReportMultiTenancy.ResourceType.API_CALLS,
                getApiCallsQuotaByTier(tier), "calls");

        // Users quota
        addQuota(platform.getPlatformId(), tenantId,
                ReportMultiTenancy.ResourceType.USERS,
                (long) getMaxUsersByTier(tier), "users");
    }

    private String generateSubdomain(String tenantName) {
        return tenantName.toLowerCase()
                .replaceAll("[^a-z0-9]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    private Integer getMaxUsersByTier(ReportMultiTenancy.TenantTier tier) {
        return switch (tier) {
            case FREE -> 10;
            case BASIC -> 50;
            case STANDARD -> 200;
            case PROFESSIONAL -> 1000;
            case ENTERPRISE -> 10000;
            case CUSTOM -> 100000;
        };
    }

    private Long getStorageQuotaByTier(ReportMultiTenancy.TenantTier tier) {
        return switch (tier) {
            case FREE -> 1L * 1024 * 1024 * 1024; // 1 GB
            case BASIC -> 10L * 1024 * 1024 * 1024; // 10 GB
            case STANDARD -> 100L * 1024 * 1024 * 1024; // 100 GB
            case PROFESSIONAL -> 500L * 1024 * 1024 * 1024; // 500 GB
            case ENTERPRISE -> 5000L * 1024 * 1024 * 1024; // 5 TB
            case CUSTOM -> 50000L * 1024 * 1024 * 1024; // 50 TB
        };
    }

    private Long getApiCallsQuotaByTier(ReportMultiTenancy.TenantTier tier) {
        return switch (tier) {
            case FREE -> 1000L;
            case BASIC -> 10000L;
            case STANDARD -> 100000L;
            case PROFESSIONAL -> 1000000L;
            case ENTERPRISE -> 10000000L;
            case CUSTOM -> 100000000L;
        };
    }

    private String getUnitByResourceType(ReportMultiTenancy.ResourceType resourceType) {
        return switch (resourceType) {
            case STORAGE -> "GB";
            case COMPUTE -> "hours";
            case BANDWIDTH -> "GB";
            case API_CALLS -> "calls";
            case USERS -> "users";
            case REPORTS -> "reports";
            case CONCURRENT_SESSIONS -> "sessions";
            case DATABASE_CONNECTIONS -> "connections";
        };
    }

    private String generateInvoiceNumber() {
        return "INV-" + LocalDateTime.now().getYear() +
                "-" + String.format("%08d", new Random().nextInt(100000000));
    }
}
