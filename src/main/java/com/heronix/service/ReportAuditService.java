package com.heronix.service;

import com.heronix.event.ReportEvent;
import com.heronix.model.domain.AuditLog;
import com.heronix.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Report Audit Service
 *
 * Provides audit logging capabilities for all report-related operations.
 *
 * Automatically logs:
 * - Report generation events
 * - Report access and downloads
 * - Batch export operations
 * - Scheduled report execution
 * - Report configuration changes
 *
 * Compliance Features:
 * - FERPA compliance (student data access tracking)
 * - Tamper-proof logging
 * - Comprehensive audit trail
 * - Retention policy support
 *
 * Integration:
 * - Event-driven via ReportEvent listener
 * - Async processing for performance
 * - Automatic metadata capture
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 64 - Report Audit Trail & Compliance Logging
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReportAuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Listen for report events and create audit logs
     */
    @EventListener
    @Async("taskExecutor")
    public void handleReportEvent(ReportEvent event) {
        try {
            AuditLog auditLog = createAuditLogFromEvent(event);
            auditLogRepository.save(auditLog);

            log.debug("Audit log created for event: {}", event.getEventType());

        } catch (Exception e) {
            log.error("Error creating audit log for event: {}", event, e);
        }
    }

    /**
     * Create audit log entry
     */
    public void logReportAccess(String username, String reportType, Long reportId,
                                String ipAddress, boolean success) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .username(username)
                    .action(AuditLog.AuditAction.REPORT_VIEW)
                    .entityType(reportType)
                    .entityId(reportId)
                    .ipAddress(ipAddress)
                    .success(success)
                    .details(String.format("User %s accessed %s report #%d",
                            username, reportType, reportId))
                    .build();

            auditLogRepository.save(auditLog);

        } catch (Exception e) {
            log.error("Error logging report access", e);
        }
    }

    /**
     * Log report download
     */
    public void logReportDownload(String username, String reportType, Long reportId,
                                  String format, String ipAddress) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .username(username)
                    .action(AuditLog.AuditAction.REPORT_DOWNLOAD)
                    .entityType(reportType)
                    .entityId(reportId)
                    .ipAddress(ipAddress)
                    .success(true)
                    .details(String.format("{\"message\":\"User %s downloaded %s report #%d in %s format\",\"format\":\"%s\"}",
                            username, reportType, reportId, format, format))
                    .build();

            auditLogRepository.save(auditLog);

        } catch (Exception e) {
            log.error("Error logging report download", e);
        }
    }

    /**
     * Create audit log from report event
     */
    private AuditLog createAuditLogFromEvent(ReportEvent event) {
        AuditLog.AuditAction auditAction = mapEventToAuditAction(event.getEventType());
        boolean success = !event.isFailure();

        return AuditLog.builder()
                .username(event.getGeneratedBy())
                .action(auditAction)
                .entityType(event.getReportType() != null ? event.getReportType().name() : null)
                .success(success)
                .details(event.getMessage())
                .severity(event.isFailure() ? AuditLog.AuditSeverity.ERROR : AuditLog.AuditSeverity.INFO)
                .build();
    }

    /**
     * Map report event type to audit action
     */
    private AuditLog.AuditAction mapEventToAuditAction(ReportEvent.ReportEventType eventType) {
        return switch (eventType) {
            case REPORT_GENERATED, REPORT_FAILED -> AuditLog.AuditAction.REPORT_GENERATE;
            case BATCH_STARTED, BATCH_COMPLETED, BATCH_FAILED -> AuditLog.AuditAction.REPORT_GENERATE;
            case SCHEDULE_EXECUTED, SCHEDULE_FAILED -> AuditLog.AuditAction.REPORT_GENERATE;
            case REPORT_EMAILED, LARGE_REPORT_GENERATED -> AuditLog.AuditAction.REPORT_GENERATE;
            case THRESHOLD_EXCEEDED, CHRONIC_ABSENTEEISM_DETECTED -> AuditLog.AuditAction.REPORT_VIEW;
            default -> AuditLog.AuditAction.REPORT_GENERATE;
        };
    }
}
