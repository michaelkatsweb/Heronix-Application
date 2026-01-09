package com.heronix.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Email Notification Service
 *
 * Sends email notifications for attendance reports and system alerts.
 * Supports HTML templates, attachments, and bulk sending.
 *
 * Email Types:
 * - Daily Attendance Reports
 * - Weekly Summaries
 * - Chronic Absenteeism Alerts
 * - System Error Notifications
 * - Parent/Guardian Notifications
 *
 * Features:
 * - HTML email templates
 * - File attachments (PDF, Excel)
 * - Bulk sending with rate limiting
 * - Retry logic for failed sends
 * - Delivery tracking
 *
 * Configuration:
 * - SMTP server settings in application.properties
 * - Recipient lists (admins, teachers, parents)
 * - Template customization
 * - Email scheduling
 *
 * Note: This is a stub implementation. For production, integrate with:
 * - Spring Mail (JavaMailSender)
 * - SendGrid API
 * - AWS SES
 * - Other email service providers
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 49 - Report Export & Scheduling
 */
@Service
@Slf4j
public class EmailNotificationService {

    @Value("${email.enabled:false}")
    private boolean emailEnabled;

    @Value("${email.admin.recipients:admin@heronix.com}")
    private String[] adminRecipients;

    @Value("${email.from:noreply@heronix.com}")
    private String fromAddress;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    /**
     * Send daily attendance report email
     *
     * @param date Report date
     * @param excelPath Path to Excel attachment
     * @param pdfPath Path to PDF attachment
     */
    public void sendDailyAttendanceReport(LocalDate date, String excelPath, String pdfPath) {
        if (!emailEnabled) {
            log.debug("Email disabled, skipping daily attendance report email");
            return;
        }

        String subject = String.format("Daily Attendance Report - %s", date.format(DATE_FORMATTER));
        String body = buildDailyReportEmailBody(date, excelPath, pdfPath);

        sendEmail(adminRecipients, subject, body, new String[]{excelPath, pdfPath});
    }

    /**
     * Send weekly attendance summary email
     *
     * @param startDate Start date
     * @param endDate End date
     * @param excelPath Path to Excel attachment
     * @param pdfPath Path to PDF attachment
     */
    public void sendWeeklyAttendanceSummary(LocalDate startDate, LocalDate endDate,
                                           String excelPath, String pdfPath) {
        if (!emailEnabled) {
            log.debug("Email disabled, skipping weekly attendance summary email");
            return;
        }

        String subject = String.format("Weekly Attendance Summary - %s to %s",
            startDate.format(DATE_FORMATTER),
            endDate.format(DATE_FORMATTER));
        String body = buildWeeklySummaryEmailBody(startDate, endDate, excelPath, pdfPath);

        sendEmail(adminRecipients, subject, body, new String[]{excelPath, pdfPath});
    }

    /**
     * Send chronic absenteeism report email
     *
     * @param startDate Start date
     * @param endDate End date
     * @param excelPath Path to Excel attachment
     */
    public void sendChronicAbsenteeismReport(LocalDate startDate, LocalDate endDate, String excelPath) {
        if (!emailEnabled) {
            log.debug("Email disabled, skipping chronic absenteeism report email");
            return;
        }

        String subject = String.format("Chronic Absenteeism Report - %s",
            startDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        String body = buildChronicAbsenteeismEmailBody(startDate, endDate, excelPath);

        sendEmail(adminRecipients, subject, body, new String[]{excelPath});
    }

    /**
     * Send error notification email
     *
     * @param subject Error subject
     * @param message Error message
     */
    public void sendErrorNotification(String subject, String message) {
        if (!emailEnabled) {
            log.debug("Email disabled, skipping error notification email");
            return;
        }

        String body = buildErrorNotificationEmailBody(subject, message);

        sendEmail(adminRecipients, "SYSTEM ALERT: " + subject, body, null);
    }

    /**
     * Send email (stub implementation - logs email details)
     *
     * For production SMTP integration:
     * 1. Add spring-boot-starter-mail dependency to pom.xml
     * 2. Configure SMTP in application.properties:
     *    spring.mail.host=smtp.gmail.com
     *    spring.mail.port=587
     *    spring.mail.username=your-email@gmail.com
     *    spring.mail.password=your-app-password
     *    spring.mail.properties.mail.smtp.auth=true
     *    spring.mail.properties.mail.smtp.starttls.enable=true
     * 3. Inject JavaMailSender and use MimeMessageHelper to send
     *
     * @param recipients Email recipients
     * @param subject Email subject
     * @param body Email body (HTML)
     * @param attachments File paths for attachments
     */
    private void sendEmail(String[] recipients, String subject, String body, String[] attachments) {
        log.info("Email notification (stub mode - not actually sent):");
        log.info("  From: {}", fromAddress);
        log.info("  To: {}", String.join(", ", recipients));
        log.info("  Subject: {}", subject);
        log.info("  Attachments: {}", attachments != null ? String.join(", ", attachments) : "None");
        log.debug("  Body: {}", body);

        // Production SMTP implementation would go here
        // See class documentation for configuration details
    }

    /**
     * Build daily report email body
     */
    private String buildDailyReportEmailBody(LocalDate date, String excelPath, String pdfPath) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2 style="color: #2c3e50;">Daily Attendance Report</h2>
                <p>Good morning,</p>
                <p>Please find attached the daily attendance report for <strong>%s</strong>.</p>

                <h3>Attached Files:</h3>
                <ul>
                    <li>Excel Report: %s</li>
                    <li>PDF Report: %s</li>
                </ul>

                <p>This report was automatically generated by Heronix SIS.</p>

                <hr>
                <p style="color: #7f8c8d; font-size: 12px;">
                    Heronix Student Information System<br>
                    Automated Report Generation<br>
                    © 2025 Heronix Development Team
                </p>
            </body>
            </html>
            """,
            date.format(DATE_FORMATTER),
            excelPath,
            pdfPath
        );
    }

    /**
     * Build weekly summary email body
     */
    private String buildWeeklySummaryEmailBody(LocalDate startDate, LocalDate endDate,
                                               String excelPath, String pdfPath) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2 style="color: #2c3e50;">Weekly Attendance Summary</h2>
                <p>Good morning,</p>
                <p>Please find attached the weekly attendance summary for the period
                   <strong>%s</strong> to <strong>%s</strong>.</p>

                <h3>Report Contents:</h3>
                <ul>
                    <li>Student attendance rates</li>
                    <li>Present/Absent/Tardy/Excused counts</li>
                    <li>Summary statistics</li>
                </ul>

                <h3>Attached Files:</h3>
                <ul>
                    <li>Excel Report: %s</li>
                    <li>PDF Report: %s</li>
                </ul>

                <p>This report was automatically generated by Heronix SIS.</p>

                <hr>
                <p style="color: #7f8c8d; font-size: 12px;">
                    Heronix Student Information System<br>
                    Automated Report Generation<br>
                    © 2025 Heronix Development Team
                </p>
            </body>
            </html>
            """,
            startDate.format(DATE_FORMATTER),
            endDate.format(DATE_FORMATTER),
            excelPath,
            pdfPath
        );
    }

    /**
     * Build chronic absenteeism email body
     */
    private String buildChronicAbsenteeismEmailBody(LocalDate startDate, LocalDate endDate, String excelPath) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2 style="color: #e74c3c;">Chronic Absenteeism Report</h2>
                <p>Good morning,</p>
                <p>Please find attached the chronic absenteeism report for <strong>%s</strong>.</p>

                <p style="background-color: #fadbd8; padding: 15px; border-left: 4px solid #e74c3c;">
                    <strong>Important:</strong> This report identifies students who are at risk due to
                    excessive absences (10%% or more of school days missed).
                </p>

                <h3>Recommended Actions:</h3>
                <ul>
                    <li>Contact parents/guardians of at-risk students</li>
                    <li>Schedule intervention meetings</li>
                    <li>Develop attendance improvement plans</li>
                    <li>Monitor student progress closely</li>
                </ul>

                <h3>Attached Files:</h3>
                <ul>
                    <li>Excel Report: %s</li>
                </ul>

                <p>This report was automatically generated by Heronix SIS.</p>

                <hr>
                <p style="color: #7f8c8d; font-size: 12px;">
                    Heronix Student Information System<br>
                    Automated Report Generation<br>
                    © 2025 Heronix Development Team
                </p>
            </body>
            </html>
            """,
            startDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            excelPath
        );
    }

    /**
     * Build error notification email body
     */
    private String buildErrorNotificationEmailBody(String subject, String message) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2 style="color: #e74c3c;">System Alert</h2>
                <p>An error occurred in the Heronix SIS automated report generation system.</p>

                <div style="background-color: #fadbd8; padding: 15px; border-left: 4px solid #e74c3c; margin: 20px 0;">
                    <h3 style="margin-top: 0;">%s</h3>
                    <pre style="background-color: #fff; padding: 10px; overflow-x: auto;">%s</pre>
                </div>

                <p><strong>Action Required:</strong> Please investigate and resolve this issue.</p>

                <hr>
                <p style="color: #7f8c8d; font-size: 12px;">
                    Heronix Student Information System<br>
                    System Alert Notification<br>
                    © 2025 Heronix Development Team
                </p>
            </body>
            </html>
            """,
            subject,
            message
        );
    }
}
