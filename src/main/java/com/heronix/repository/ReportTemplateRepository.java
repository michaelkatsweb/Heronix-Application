package com.heronix.repository;

import com.heronix.model.domain.ReportTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Report Template Repository
 *
 * Data access layer for report template management.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 61 - Enhanced Report Export Capabilities
 */
@Repository
public interface ReportTemplateRepository extends JpaRepository<ReportTemplate, Long> {

    /**
     * Find all active templates
     */
    List<ReportTemplate> findByActiveTrue();

    /**
     * Find templates by type
     */
    List<ReportTemplate> findByTemplateTypeAndActiveTrue(ReportTemplate.TemplateType templateType);

    /**
     * Find templates by creator
     */
    List<ReportTemplate> findByCreatedByAndActiveTrue(String createdBy);

    /**
     * Find shared templates
     */
    List<ReportTemplate> findBySharedTrueAndActiveTrue();

    /**
     * Find system templates
     */
    List<ReportTemplate> findBySystemTemplateTrueAndActiveTrue();

    /**
     * Find templates by category
     */
    List<ReportTemplate> findByCategoryAndActiveTrue(String category);

    /**
     * Find template by name
     */
    Optional<ReportTemplate> findByNameAndActiveTrue(String name);

    /**
     * Find templates by tag
     */
    @Query("SELECT t FROM ReportTemplate t WHERE t.active = true AND t.tags LIKE %:tag%")
    List<ReportTemplate> findByTag(@Param("tag") String tag);

    /**
     * Find most used templates
     */
    @Query("SELECT t FROM ReportTemplate t WHERE t.active = true ORDER BY t.usageCount DESC")
    List<ReportTemplate> findMostUsed();

    /**
     * Find recently created templates
     */
    @Query("SELECT t FROM ReportTemplate t WHERE t.active = true ORDER BY t.createdAt DESC")
    List<ReportTemplate> findRecentlyCreated();
}
