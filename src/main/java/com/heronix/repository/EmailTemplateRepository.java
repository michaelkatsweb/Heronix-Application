package com.heronix.repository;

import com.heronix.model.domain.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Email Template Repository
 *
 * Data access layer for email template management.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 56 - Email Template System
 */
@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {

    /**
     * Find email template by type
     *
     * @param templateType Template type
     * @return Optional email template
     */
    Optional<EmailTemplate> findByTemplateType(EmailTemplate.TemplateType templateType);

    /**
     * Find active email template by type
     *
     * @param templateType Template type
     * @param active Active status
     * @return Optional email template
     */
    Optional<EmailTemplate> findByTemplateTypeAndActive(EmailTemplate.TemplateType templateType, Boolean active);

    /**
     * Find all active email templates
     *
     * @param active Active status
     * @return List of active email templates
     */
    List<EmailTemplate> findByActive(Boolean active);

    /**
     * Check if email template exists by type
     *
     * @param templateType Template type
     * @return True if exists
     */
    boolean existsByTemplateType(EmailTemplate.TemplateType templateType);
}
