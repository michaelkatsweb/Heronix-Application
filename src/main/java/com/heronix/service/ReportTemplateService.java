package com.heronix.service;

import com.heronix.dto.ReportTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Report Template Service
 *
 * Provides template management, customization,
 * and reusable report designs.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 87 - Report Templates
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportTemplateService {

    private final Map<Long, ReportTemplate> templateStore = new ConcurrentHashMap<>();
    private final AtomicLong templateIdGenerator = new AtomicLong(1);

    public ReportTemplate createTemplate(ReportTemplate template) {
        Long templateId = templateIdGenerator.getAndIncrement();
        template.setTemplateId(templateId);
        template.setCreatedAt(LocalDateTime.now());

        templateStore.put(templateId, template);

        log.info("Template created: {} (name: {})", templateId, template.getTemplateName());
        return template;
    }

    public ReportTemplate getTemplate(Long templateId) {
        ReportTemplate template = templateStore.get(templateId);
        if (template == null) {
            throw new IllegalArgumentException("Template not found: " + templateId);
        }
        return template;
    }

    public ReportTemplate cloneTemplate(Long templateId, String newName) {
        ReportTemplate original = getTemplate(templateId);

        ReportTemplate cloned = ReportTemplate.builder()
                .templateName(newName)
                .description("Cloned from: " + original.getTemplateName())
                .templateType(original.getTemplateType())
                .isPublic(false)
                .build();

        return createTemplate(cloned);
    }

    public void deleteTemplate(Long templateId) {
        ReportTemplate template = templateStore.remove(templateId);
        if (template == null) {
            throw new IllegalArgumentException("Template not found: " + templateId);
        }
        log.info("Template deleted: {}", templateId);
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTemplates", templateStore.size());
        stats.put("timestamp", LocalDateTime.now());
        return stats;
    }
}
