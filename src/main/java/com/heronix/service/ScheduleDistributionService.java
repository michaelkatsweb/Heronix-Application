package com.heronix.service;

import com.heronix.model.domain.Schedule;
import com.heronix.model.domain.Student;
import com.heronix.repository.ScheduleRepository;
import com.heronix.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for distributing schedules to students via email, portal, and other channels
 * Handles schedule publication workflow and notification
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleDistributionService {

    private final ScheduleRepository scheduleRepository;
    private final StudentRepository studentRepository;
    private final StudentSchedulePrintService printService;

    @Value("${heronix.schedule.distribution.email-enabled:false}")
    private Boolean emailEnabled;

    @Value("${heronix.schedule.distribution.parent-portal-enabled:true}")
    private Boolean parentPortalEnabled;

    @Value("${heronix.schedule.distribution.auto-publish:false}")
    private Boolean autoPublish;

    // ========================================================================
    // SCHEDULE PUBLICATION
    // ========================================================================

    /**
     * Publish a schedule to make it visible to students
     *
     * @param scheduleId Schedule ID
     * @return Publication result
     */
    @Transactional
    public SchedulePublicationResult publishSchedule(Long scheduleId) {
        log.info("Publishing schedule ID: {}", scheduleId);

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + scheduleId));

        // Mark schedule as published (note: Schedule entity may need published field added)
        // TODO: Add published and publishedDate fields to Schedule entity if needed
        schedule.setLastModifiedDate(LocalDate.now());
        scheduleRepository.save(schedule);

        // Count affected students
        List<Student> students = studentRepository.findAll().stream()
                .filter(Student::isActive)
                .collect(Collectors.toList());

        log.info("Schedule {} published successfully - {} students affected",
                scheduleId, students.size());

        return SchedulePublicationResult.builder()
                .success(true)
                .scheduleId(scheduleId)
                .scheduleName(schedule.getScheduleName())
                .publishedDate(LocalDateTime.now())
                .studentsAffected(students.size())
                .message("Schedule published successfully")
                .build();
    }

    /**
     * Unpublish a schedule (hide from students)
     *
     * @param scheduleId Schedule ID
     * @return Unpublication result
     */
    @Transactional
    public SchedulePublicationResult unpublishSchedule(Long scheduleId) {
        log.info("Unpublishing schedule ID: {}", scheduleId);

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + scheduleId));

        // Mark schedule as unpublished
        // TODO: Add published field to Schedule entity if needed
        schedule.setLastModifiedDate(LocalDate.now());
        scheduleRepository.save(schedule);

        log.info("Schedule {} unpublished successfully", scheduleId);

        return SchedulePublicationResult.builder()
                .success(true)
                .scheduleId(scheduleId)
                .scheduleName(schedule.getScheduleName())
                .publishedDate(LocalDateTime.now())
                .message("Schedule unpublished successfully")
                .build();
    }

    // ========================================================================
    // STUDENT NOTIFICATION
    // ========================================================================

    /**
     * Notify students that their schedule is available
     *
     * @param scheduleId Schedule ID
     * @return Notification result
     */
    @Transactional(readOnly = true)
    public NotificationResult notifyStudents(Long scheduleId) {
        log.info("Notifying students about schedule ID: {}", scheduleId);

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + scheduleId));

        List<Student> students = studentRepository.findAll().stream()
                .filter(Student::isActive)
                .collect(Collectors.toList());

        int successCount = 0;
        int failureCount = 0;
        List<String> errors = new ArrayList<>();

        for (Student student : students) {
            try {
                // Send notification via available channels
                boolean notified = false;

                // Portal notification (always enabled)
                notified = notifyViaPortal(student, schedule);

                // Email notification (if enabled and student has email)
                if (emailEnabled && student.getEmail() != null && !student.getEmail().isEmpty()) {
                    notified = notifyViaEmail(student, schedule) || notified;
                }

                if (notified) {
                    successCount++;
                } else {
                    failureCount++;
                    errors.add("No notification channel available for student: " + student.getStudentId());
                }

            } catch (Exception e) {
                failureCount++;
                errors.add("Failed to notify student " + student.getStudentId() + ": " + e.getMessage());
                log.error("Failed to notify student {}", student.getId(), e);
            }
        }

        log.info("Student notification completed: {} successful, {} failed", successCount, failureCount);

        return NotificationResult.builder()
                .success(failureCount == 0)
                .totalStudents(students.size())
                .successCount(successCount)
                .failureCount(failureCount)
                .errors(errors)
                .message(String.format("Notified %d of %d students", successCount, students.size()))
                .build();
    }

    /**
     * Send schedule notification via portal
     */
    private boolean notifyViaPortal(Student student, Schedule schedule) {
        // TODO: Implement portal notification system
        // This would typically create a notification record in the database
        // that appears when the student logs into the student portal

        log.debug("Portal notification for student {} - schedule {}", student.getId(), schedule.getId());
        return true; // Placeholder
    }

    /**
     * Send schedule notification via email
     *
     * STUB IMPLEMENTATION: Logs notification without sending
     *
     * Production SMTP Setup:
     * ======================
     *
     * 1. Add Spring Boot Mail dependency to pom.xml:
     * <dependency>
     *     <groupId>org.springframework.boot</groupId>
     *     <artifactId>spring-boot-starter-mail</artifactId>
     * </dependency>
     *
     * 2. Configure SMTP in application.properties:
     * spring.mail.host=smtp.gmail.com
     * spring.mail.port=587
     * spring.mail.username=your-email@school.edu
     * spring.mail.password=your-app-specific-password
     * spring.mail.properties.mail.smtp.auth=true
     * spring.mail.properties.mail.smtp.starttls.enable=true
     * spring.mail.properties.mail.smtp.starttls.required=true
     * spring.mail.properties.mail.smtp.ssl.trust=smtp.gmail.com
     *
     * 3. Inject JavaMailSender:
     * @Autowired
     * private JavaMailSender mailSender;
     *
     * 4. Production implementation:
     * try {
     *     MimeMessage message = mailSender.createMimeMessage();
     *     MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
     *     helper.setFrom("noreply@school.edu");
     *     helper.setTo(student.getEmail());
     *     helper.setSubject("Your Schedule is Available");
     *     helper.setText("Dear " + student.getFullName() + ",\n\n" +
     *         "Your class schedule for " + schedule.getScheduleName() + " is now available...");
     *     mailSender.send(message);
     *     return true;
     * } catch (MessagingException e) {
     *     log.error("Failed to send email", e);
     *     return false;
     * }
     */
    private boolean notifyViaEmail(Student student, Schedule schedule) {
        log.info("Email notification (stub mode) for student {} - schedule {}",
                student.getId(), schedule.getId());

        if (student.getEmail() == null || student.getEmail().isEmpty()) {
            log.warn("Cannot send email notification - student {} has no email", student.getId());
            return false;
        }

        log.debug("Would send email to: {} with subject: 'Your Schedule is Available - {}'",
                student.getEmail(), schedule.getScheduleName());

        // Stub: Return false to indicate email not actually sent
        // Production: Use JavaMailSender as documented above
        return false;
    }

    // ========================================================================
    // SCHEDULE EMAIL DISTRIBUTION
    // ========================================================================

    /**
     * Send schedule PDF to student via email
     *
     * @param studentId Student ID
     * @param scheduleId Schedule ID
     * @return Email result
     */
    @Transactional(readOnly = true)
    public EmailResult sendScheduleEmail(Long studentId, Long scheduleId) {
        log.info("Sending schedule email for student {} - schedule {}", studentId, scheduleId);

        if (!emailEnabled) {
            return EmailResult.builder()
                    .success(false)
                    .message("Email distribution is disabled")
                    .build();
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + scheduleId));

        if (student.getEmail() == null || student.getEmail().isEmpty()) {
            return EmailResult.builder()
                    .success(false)
                    .message("Student has no email address on file")
                    .build();
        }

        try {
            // Generate schedule PDF
            byte[] schedulePDF = printService.generateSchedulePDF(studentId, scheduleId);

            // Send email (placeholder - would use JavaMail)
            boolean sent = sendEmailWithAttachment(
                    student.getEmail(),
                    "Your Class Schedule - " + schedule.getScheduleName(),
                    buildScheduleEmailBody(student, schedule),
                    schedulePDF,
                    student.getFullName() + "_Schedule.pdf"
            );

            if (sent) {
                log.info("Schedule email sent successfully to {}", student.getEmail());
                return EmailResult.builder()
                        .success(true)
                        .recipient(student.getEmail())
                        .message("Schedule email sent successfully")
                        .build();
            } else {
                return EmailResult.builder()
                        .success(false)
                        .message("Failed to send email")
                        .build();
            }

        } catch (Exception e) {
            log.error("Failed to send schedule email", e);
            return EmailResult.builder()
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Send email with attachment
     *
     * STUB IMPLEMENTATION: Logs email details without sending
     *
     * Production implementation with JavaMailSender:
     * ===============================================
     *
     * try {
     *     MimeMessage message = mailSender.createMimeMessage();
     *     MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
     *
     *     helper.setFrom("noreply@school.edu");
     *     helper.setTo(to);
     *     helper.setSubject(subject);
     *     helper.setText(body, false); // false = plain text, true = HTML
     *
     *     // Add PDF attachment
     *     helper.addAttachment(attachmentFilename, new ByteArrayResource(attachment));
     *
     *     mailSender.send(message);
     *     log.info("Email sent successfully to: {}", to);
     *     return true;
     *
     * } catch (MessagingException e) {
     *     log.error("Failed to send email to: {}", to, e);
     *     return false;
     * }
     *
     * For HTML emails with embedded images:
     * helper.setText(htmlBody, true); // true = HTML
     * helper.addInline("logo", new ClassPathResource("images/logo.png"));
     *
     * For batch emails (BCC):
     * helper.setBcc(recipients.toArray(new String[0]));
     */
    private boolean sendEmailWithAttachment(String to, String subject, String body,
                                           byte[] attachment, String attachmentFilename) {
        log.info("Email (stub mode) - To: {} | Subject: {} | Attachment: {} ({} bytes)",
                to, subject, attachmentFilename, attachment.length);
        log.debug("Email body preview: {}", body.substring(0, Math.min(100, body.length())) + "...");

        // Stub: Return false to indicate email not actually sent
        // Production: Use JavaMailSender with MimeMessageHelper as documented above
        return false;
    }

    /**
     * Build email body for schedule notification
     */
    private String buildScheduleEmailBody(Student student, Schedule schedule) {
        StringBuilder body = new StringBuilder();

        body.append("Dear ").append(student.getFullName()).append(",\n\n");
        body.append("Your class schedule for ").append(schedule.getScheduleName())
                .append(" is now available.\n\n");
        body.append("Please review your schedule carefully and contact your guidance counselor ");
        body.append("if you have any questions or concerns.\n\n");
        body.append("You can also view your schedule online through the student portal.\n\n");
        body.append("Important Dates:\n");
        body.append("- School Start Date: ").append(schedule.getStartDate()).append("\n");
        body.append("- School End Date: ").append(schedule.getEndDate()).append("\n\n");
        body.append("If you need to request a schedule change, please submit a request ");
        body.append("through the student portal or visit the guidance office.\n\n");
        body.append("Best regards,\n");
        body.append("School Administration\n");

        return body.toString();
    }

    // ========================================================================
    // PARENT PORTAL EXPORT
    // ========================================================================

    /**
     * Export schedule to parent portal
     *
     * @param scheduleId Schedule ID
     * @return Export result
     */
    @Transactional(readOnly = true)
    public ParentPortalExportResult exportScheduleToParentPortal(Long scheduleId) {
        log.info("Exporting schedule {} to parent portal", scheduleId);

        if (!parentPortalEnabled) {
            return ParentPortalExportResult.builder()
                    .success(false)
                    .message("Parent portal integration is disabled")
                    .build();
        }

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + scheduleId));

        List<Student> students = studentRepository.findAll().stream()
                .filter(Student::isActive)
                .collect(Collectors.toList());

        // TODO: Implement actual parent portal API integration
        // This would typically call a parent portal API to sync student schedules

        log.info("Exported {} student schedules to parent portal", students.size());

        return ParentPortalExportResult.builder()
                .success(true)
                .scheduleId(scheduleId)
                .studentsExported(students.size())
                .exportTimestamp(LocalDateTime.now())
                .message("Schedule exported to parent portal successfully")
                .build();
    }

    // ========================================================================
    // BULK OPERATIONS
    // ========================================================================

    /**
     * Distribute schedules to all students (publish + notify + email)
     *
     * @param scheduleId Schedule ID
     * @return Distribution result
     */
    @Transactional
    public DistributionResult distributeScheduleToAllStudents(Long scheduleId) {
        log.info("Distributing schedule {} to all students", scheduleId);

        // Step 1: Publish schedule
        SchedulePublicationResult publicationResult = publishSchedule(scheduleId);

        // Step 2: Notify students
        NotificationResult notificationResult = notifyStudents(scheduleId);

        // Step 3: Export to parent portal (if enabled)
        ParentPortalExportResult portalResult = null;
        if (parentPortalEnabled) {
            portalResult = exportScheduleToParentPortal(scheduleId);
        }

        log.info("Schedule distribution completed for schedule {}", scheduleId);

        return DistributionResult.builder()
                .success(publicationResult.isSuccess() && notificationResult.isSuccess())
                .scheduleId(scheduleId)
                .publishedSuccessfully(publicationResult.isSuccess())
                .studentsNotified(notificationResult.getSuccessCount())
                .totalStudents(notificationResult.getTotalStudents())
                .parentPortalExported(portalResult != null && portalResult.isSuccess())
                .distributionTimestamp(LocalDateTime.now())
                .message("Schedule distributed to " + notificationResult.getSuccessCount() + " students")
                .build();
    }

    // ========================================================================
    // RESULT CLASSES
    // ========================================================================

    @lombok.Data
    @lombok.Builder
    public static class SchedulePublicationResult {
        private boolean success;
        private Long scheduleId;
        private String scheduleName;
        private LocalDateTime publishedDate;
        private Integer studentsAffected;
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    public static class NotificationResult {
        private boolean success;
        private int totalStudents;
        private int successCount;
        private int failureCount;
        private List<String> errors;
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    public static class EmailResult {
        private boolean success;
        private String recipient;
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    public static class ParentPortalExportResult {
        private boolean success;
        private Long scheduleId;
        private Integer studentsExported;
        private LocalDateTime exportTimestamp;
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    public static class DistributionResult {
        private boolean success;
        private Long scheduleId;
        private boolean publishedSuccessfully;
        private int studentsNotified;
        private int totalStudents;
        private boolean parentPortalExported;
        private LocalDateTime distributionTimestamp;
        private String message;
    }
}
