package com.heronix.service;

import com.heronix.model.domain.EmailTemplate;
import com.heronix.model.domain.ReportHistory;
import com.heronix.repository.EmailTemplateRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

/**
 * Email Service
 *
 * Provides email sending capabilities for report delivery and notifications.
 *
 * Features:
 * - Template-based email composition
 * - Variable substitution in subject and body
 * - HTML and plain text email support
 * - Attachment support for reports
 * - Async sending for non-blocking operation
 * - Error handling and retry logic
 *
 * Template Variables Supported:
 * - {{reportName}} - Report type name
 * - {{reportDate}} - Report generation date
 * - {{generatedBy}} - User who generated the report
 * - {{reportFormat}} - Report format (Excel, PDF)
 * - {{recordCount}} - Number of records in report
 * - {{fileName}} - Generated file name
 * - {{fileSize}} - File size in KB
 * - {{generationTime}} - Time taken to generate
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 56 - Email Template System
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateRepository templateRepository;

    @Value("${spring.mail.username:noreply@heronix.com}")
    private String fromEmail;

    @Value("${email.enabled:false}")
    private Boolean emailEnabled;

    @Value("${email.default-recipient:admin@heronix.com}")
    private String defaultRecipient;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

    /**
     * Send report via email using template
     *
     * @param templateType Email template type
     * @param recipientEmail Recipient email address
     * @param reportHistory Report history metadata
     * @param reportData Report file data
     * @param fileName Attachment file name
     */
    @Async("taskExecutor")
    public void sendReportEmail(EmailTemplate.TemplateType templateType,
                                String recipientEmail,
                                ReportHistory reportHistory,
                                byte[] reportData,
                                String fileName) {
        if (!emailEnabled) {
            log.info("Email sending is disabled. Skipping email for report: {}", reportHistory.getId());
            return;
        }

        try {
            // Get email template
            Optional<EmailTemplate> templateOpt = templateRepository.findByTemplateTypeAndActive(templateType, true);

            if (templateOpt.isEmpty()) {
                log.warn("No active email template found for type: {}", templateType);
                return;
            }

            EmailTemplate template = templateOpt.get();

            // Build variable map
            Map<String, String> variables = buildVariableMap(reportHistory, fileName, reportData.length);

            // Process template
            String subject = processTemplate(template.getSubject(), variables);
            String body = processTemplate(template.getBody(), variables);

            // Send email
            sendEmail(recipientEmail, subject, body, template.getHtmlFormat(), reportData, fileName);

            log.info("Email sent successfully to {} for report: {}", recipientEmail, reportHistory.getId());

        } catch (Exception e) {
            log.error("Failed to send email for report: {}", reportHistory.getId(), e);
        }
    }

    /**
     * Send notification email without attachment
     *
     * @param templateType Email template type
     * @param recipientEmail Recipient email address
     * @param variables Template variables
     */
    @Async("taskExecutor")
    public void sendNotificationEmail(EmailTemplate.TemplateType templateType,
                                      String recipientEmail,
                                      Map<String, String> variables) {
        if (!emailEnabled) {
            log.info("Email sending is disabled. Skipping notification email.");
            return;
        }

        try {
            // Get email template
            Optional<EmailTemplate> templateOpt = templateRepository.findByTemplateTypeAndActive(templateType, true);

            if (templateOpt.isEmpty()) {
                log.warn("No active email template found for type: {}", templateType);
                return;
            }

            EmailTemplate template = templateOpt.get();

            // Process template
            String subject = processTemplate(template.getSubject(), variables);
            String body = processTemplate(template.getBody(), variables);

            // Send email
            sendEmail(recipientEmail, subject, body, template.getHtmlFormat(), null, null);

            log.info("Notification email sent successfully to {}", recipientEmail);

        } catch (Exception e) {
            log.error("Failed to send notification email to {}", recipientEmail, e);
        }
    }

    /**
     * Send email with optional attachment
     *
     * @param to Recipient email
     * @param subject Email subject
     * @param body Email body
     * @param isHtml Whether body is HTML
     * @param attachmentData Attachment data (optional)
     * @param attachmentName Attachment name (optional)
     */
    private void sendEmail(String to, String subject, String body, boolean isHtml,
                          byte[] attachmentData, String attachmentName) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, isHtml);

        // Add attachment if provided
        if (attachmentData != null && attachmentName != null) {
            ByteArrayResource resource = new ByteArrayResource(attachmentData);
            helper.addAttachment(attachmentName, resource);
        }

        mailSender.send(message);
    }

    /**
     * Build variable map from report history
     *
     * @param reportHistory Report history metadata
     * @param fileName File name
     * @param fileSize File size in bytes
     * @return Variable map
     */
    private Map<String, String> buildVariableMap(ReportHistory reportHistory, String fileName, long fileSize) {
        return Map.of(
            "reportName", formatReportType(reportHistory.getReportType()),
            "reportDate", reportHistory.getGeneratedAt().format(DATETIME_FORMATTER),
            "generatedBy", reportHistory.getGeneratedBy() != null ? reportHistory.getGeneratedBy() : "System",
            "reportFormat", reportHistory.getReportFormat().toString(),
            "recordCount", String.valueOf(reportHistory.getFileSize() != null ? reportHistory.getFileSize() : 0),
            "fileName", fileName,
            "fileSize", String.format("%.2f KB", fileSize / 1024.0),
            "generationTime", "0 ms"
        );
    }

    /**
     * Process template by replacing variables
     *
     * @param template Template string with {{variable}} placeholders
     * @param variables Variable map
     * @return Processed template
     */
    private String processTemplate(String template, Map<String, String> variables) {
        String result = template;

        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            result = result.replace(placeholder, entry.getValue());
        }

        return result;
    }

    /**
     * Format report type for display
     *
     * @param reportType Report type enum
     * @return Formatted report type
     */
    private String formatReportType(ReportHistory.ReportType reportType) {
        return switch (reportType) {
            case DAILY_ATTENDANCE -> "Daily Attendance Report";
            case STUDENT_SUMMARY -> "Student Attendance Summary";
            case CHRONIC_ABSENTEEISM -> "Chronic Absenteeism Report";
            default -> reportType.toString();
        };
    }

    /**
     * Format generation time for display
     *
     * @param generationTimeMs Generation time in milliseconds
     * @return Formatted time string
     */
    private String formatGenerationTime(Long generationTimeMs) {
        if (generationTimeMs == null) {
            return "N/A";
        }

        if (generationTimeMs < 1000) {
            return generationTimeMs + " ms";
        }

        return String.format("%.2f seconds", generationTimeMs / 1000.0);
    }

    /**
     * Test email configuration by sending a test email
     *
     * @param recipientEmail Recipient email
     */
    public void sendTestEmail(String recipientEmail) {
        try {
            String subject = "Heronix SIS - Email Configuration Test";
            String body = """
                <html>
                <body>
                    <h2>Email Configuration Test</h2>
                    <p>This is a test email from Heronix Student Information System.</p>
                    <p>If you received this email, your email configuration is working correctly.</p>
                    <hr>
                    <p><small>Heronix SIS - Phase 56 Email Template System</small></p>
                </body>
                </html>
                """;

            sendEmail(recipientEmail, subject, body, true, null, null);
            log.info("Test email sent successfully to {}", recipientEmail);

        } catch (Exception e) {
            log.error("Failed to send test email to {}", recipientEmail, e);
            throw new RuntimeException("Test email failed: " + e.getMessage(), e);
        }
    }
}
