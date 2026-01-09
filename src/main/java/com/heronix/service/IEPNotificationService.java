package com.heronix.service;

import com.heronix.model.domain.IEP;
import com.heronix.model.domain.Student;
import com.heronix.repository.IEPRepository;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * IEP Notification Service
 *
 * Manages notifications to parents, guardians, case managers, and service providers
 * regarding IEP meetings, reviews, evaluations, and compliance requirements.
 *
 * Key Responsibilities:
 * - Send parent notifications for IEP meetings and reviews
 * - Notify case managers of upcoming deadlines
 * - Alert service providers of new IEP services
 * - Send compliance notifications for overdue evaluations
 * - Batch notification processing
 * - Track notification delivery status
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - IEP Management Enhancement
 */
@Slf4j
@Service
public class IEPNotificationService {

    @Autowired
    private IEPRepository iepRepository;

    @Value("${notification.email.enabled:true}")
    private boolean emailEnabled;

    @Value("${notification.sms.enabled:false}")
    private boolean smsEnabled;

    private static final String NOTIFICATION_PROVIDER = "MOCK";

    // ========================================================================
    // PARENT NOTIFICATIONS
    // ========================================================================

    /**
     * Notify parents of IEP creation
     */
    public NotificationResult notifyParentsOfIEPCreation(Long iepId) {
        log.info("Sending parent notification for IEP creation: {}", iepId);

        IEP iep = iepRepository.findById(iepId)
                .orElseThrow(() -> new IllegalArgumentException("IEP not found: " + iepId));

        Student student = iep.getStudent();

        String subject = String.format("IEP Created for %s", student.getFullName());
        String message = String.format(
                "An Individualized Education Program (IEP #%s) has been created for your child, %s.\n\n" +
                "IEP Start Date: %s\n" +
                "IEP End Date: %s\n" +
                "Case Manager: %s\n" +
                "Eligibility: %s\n\n" +
                "Your child is entitled to the special education services outlined in this IEP. " +
                "Please contact the case manager if you have any questions or concerns.",
                iep.getIepNumber(),
                student.getFullName(),
                iep.getStartDate(),
                iep.getEndDate(),
                iep.getCaseManager(),
                iep.getEligibilityCategory()
        );

        return sendNotification("PARENT", subject, message, iep);
    }

    /**
     * Notify parents of upcoming IEP review meeting
     */
    public NotificationResult notifyParentsOfUpcomingReview(Long iepId, LocalDate meetingDate, String meetingLocation) {
        log.info("Sending parent notification for upcoming IEP review: {}", iepId);

        IEP iep = iepRepository.findById(iepId)
                .orElseThrow(() -> new IllegalArgumentException("IEP not found: " + iepId));

        Student student = iep.getStudent();

        String subject = String.format("IEP Review Meeting Scheduled - %s", student.getFullName());
        String message = String.format(
                "An IEP review meeting has been scheduled for your child, %s.\n\n" +
                "Meeting Date: %s\n" +
                "Location: %s\n" +
                "Case Manager: %s\n\n" +
                "Your participation is important. During this meeting, we will:\n" +
                "- Review your child's progress toward IEP goals\n" +
                "- Discuss current services and accommodations\n" +
                "- Make any necessary adjustments to the IEP\n" +
                "- Plan for the upcoming year\n\n" +
                "Please confirm your attendance or contact us to reschedule if needed.",
                student.getFullName(),
                meetingDate,
                meetingLocation,
                iep.getCaseManager()
        );

        return sendNotification("PARENT", subject, message, iep);
    }

    /**
     * Notify parents of IEP update
     */
    public NotificationResult notifyParentsOfIEPUpdate(Long iepId, String updateDescription) {
        log.info("Sending parent notification for IEP update: {}", iepId);

        IEP iep = iepRepository.findById(iepId)
                .orElseThrow(() -> new IllegalArgumentException("IEP not found: " + iepId));

        Student student = iep.getStudent();

        String subject = String.format("IEP Updated - %s", student.getFullName());
        String message = String.format(
                "Your child's IEP (#%s) has been updated.\n\n" +
                "Student: %s\n" +
                "Update Description: %s\n" +
                "Case Manager: %s\n\n" +
                "If you have questions about these changes, please contact the case manager.",
                iep.getIepNumber(),
                student.getFullName(),
                updateDescription,
                iep.getCaseManager()
        );

        return sendNotification("PARENT", subject, message, iep);
    }

    /**
     * Notify parents of evaluation results
     */
    public NotificationResult notifyParentsOfEvaluationResults(Long iepId) {
        log.info("Sending parent notification for evaluation results: {}", iepId);

        IEP iep = iepRepository.findById(iepId)
                .orElseThrow(() -> new IllegalArgumentException("IEP not found: " + iepId));

        Student student = iep.getStudent();

        String subject = String.format("IEP Evaluation Results Available - %s", student.getFullName());
        String message = String.format(
                "The evaluation for %s has been completed.\n\n" +
                "IEP Number: %s\n" +
                "Case Manager: %s\n\n" +
                "Please contact the case manager to schedule a meeting to review the evaluation results " +
                "and discuss any recommended changes to your child's IEP.",
                student.getFullName(),
                iep.getIepNumber(),
                iep.getCaseManager()
        );

        return sendNotification("PARENT", subject, message, iep);
    }

    // ========================================================================
    // CASE MANAGER NOTIFICATIONS
    // ========================================================================

    /**
     * Notify case manager of upcoming review deadline
     */
    public List<NotificationResult> notifyCaseManagersOfUpcomingReviews() {
        log.info("Sending case manager notifications for upcoming reviews");

        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysOut = today.plusDays(30);

        List<IEP> iepsNeedingReview = iepRepository.findIEPsNeedingRenewal(today, thirtyDaysOut);

        Map<String, List<IEP>> iepsByCaseManager = iepsNeedingReview.stream()
                .filter(iep -> iep.getCaseManager() != null)
                .collect(Collectors.groupingBy(IEP::getCaseManager));

        List<NotificationResult> results = new ArrayList<>();

        for (Map.Entry<String, List<IEP>> entry : iepsByCaseManager.entrySet()) {
            String caseManager = entry.getKey();
            List<IEP> ieps = entry.getValue();

            String subject = String.format("IEP Review Reminders - %d IEPs Due Soon", ieps.size());
            StringBuilder message = new StringBuilder();
            message.append("The following IEPs under your management have reviews due within 30 days:\n\n");

            for (IEP iep : ieps) {
                long daysUntilReview = java.time.temporal.ChronoUnit.DAYS.between(today, iep.getNextReviewDate());
                message.append(String.format("- %s (IEP #%s) - Review due in %d days (%s)\n",
                        iep.getStudent().getFullName(),
                        iep.getIepNumber(),
                        daysUntilReview,
                        iep.getNextReviewDate()));
            }

            message.append("\nPlease schedule review meetings and notify parents.");

            NotificationResult result = NotificationResult.builder()
                    .recipient(caseManager)
                    .recipientType("CASE_MANAGER")
                    .subject(subject)
                    .message(message.toString())
                    .sentAt(LocalDateTime.now())
                    .deliveryStatus(emailEnabled ? "SENT" : "DISABLED")
                    .provider(NOTIFICATION_PROVIDER)
                    .build();

            results.add(result);
        }

        log.info("Sent review reminders to {} case managers", results.size());
        return results;
    }

    /**
     * Notify case manager of overdue evaluations
     */
    public List<NotificationResult> notifyCaseManagersOfOverdueEvaluations() {
        log.info("Sending case manager notifications for overdue evaluations");

        LocalDate today = LocalDate.now();
        List<IEP> overdueIEPs = iepRepository.findIEPsWithReviewDue(today);

        Map<String, List<IEP>> iepsByCaseManager = overdueIEPs.stream()
                .filter(iep -> iep.getCaseManager() != null)
                .collect(Collectors.groupingBy(IEP::getCaseManager));

        List<NotificationResult> results = new ArrayList<>();

        for (Map.Entry<String, List<IEP>> entry : iepsByCaseManager.entrySet()) {
            String caseManager = entry.getKey();
            List<IEP> ieps = entry.getValue();

            String subject = String.format("URGENT: %d Overdue IEP Evaluations", ieps.size());
            StringBuilder message = new StringBuilder();
            message.append("The following IEPs have OVERDUE evaluations:\n\n");

            for (IEP iep : ieps) {
                long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(iep.getNextReviewDate(), today);
                message.append(String.format("- %s (IEP #%s) - %d days overdue\n",
                        iep.getStudent().getFullName(),
                        iep.getIepNumber(),
                        daysOverdue));
            }

            message.append("\nImmediate action required to ensure IDEA compliance.");

            NotificationResult result = NotificationResult.builder()
                    .recipient(caseManager)
                    .recipientType("CASE_MANAGER")
                    .subject(subject)
                    .message(message.toString())
                    .sentAt(LocalDateTime.now())
                    .deliveryStatus(emailEnabled ? "SENT" : "DISABLED")
                    .provider(NOTIFICATION_PROVIDER)
                    .priority("HIGH")
                    .build();

            results.add(result);
        }

        log.info("Sent overdue evaluation alerts to {} case managers", results.size());
        return results;
    }

    // ========================================================================
    // SERVICE PROVIDER NOTIFICATIONS
    // ========================================================================

    /**
     * Notify service providers of new IEP services
     */
    public List<NotificationResult> notifyServiceProvidersOfNewIEP(Long iepId) {
        log.info("Sending service provider notifications for new IEP: {}", iepId);

        IEP iep = iepRepository.findById(iepId)
                .orElseThrow(() -> new IllegalArgumentException("IEP not found: " + iepId));

        List<NotificationResult> results = new ArrayList<>();

        if (iep.getServices() == null || iep.getServices().isEmpty()) {
            log.info("No services to notify for IEP {}", iepId);
            return results;
        }

        for (com.heronix.model.domain.IEPService service : iep.getServices()) {
            String providerName = service.getAssignedStaff() != null ?
                    service.getAssignedStaff().getFirstName() + " " + service.getAssignedStaff().getLastName() : "TBD";

            String subject = String.format("New IEP Service Assignment - %s", iep.getStudent().getFullName());
            String message = String.format(
                    "You have been assigned to provide IEP services for %s.\n\n" +
                    "Student: %s\n" +
                    "IEP Number: %s\n" +
                    "Service Type: %s\n" +
                    "Minutes Per Week: %d\n" +
                    "Delivery Model: %s\n" +
                    "Service Description: %s\n\n" +
                    "Please coordinate with the case manager (%s) to schedule these services.",
                    iep.getStudent().getFullName(),
                    iep.getStudent().getFullName(),
                    iep.getIepNumber(),
                    service.getServiceType().getDisplayName(),
                    service.getMinutesPerWeek(),
                    service.getDeliveryModel().getDisplayName(),
                    service.getServiceDescription() != null ? service.getServiceDescription() : "N/A",
                    iep.getCaseManager()
            );

            NotificationResult result = NotificationResult.builder()
                    .recipient(providerName)
                    .recipientType("SERVICE_PROVIDER")
                    .subject(subject)
                    .message(message)
                    .sentAt(LocalDateTime.now())
                    .deliveryStatus(emailEnabled ? "SENT" : "DISABLED")
                    .provider(NOTIFICATION_PROVIDER)
                    .build();

            results.add(result);
        }

        log.info("Sent notifications to {} service providers", results.size());
        return results;
    }

    // ========================================================================
    // BATCH PROCESSING
    // ========================================================================

    /**
     * Send all pending notifications (scheduled job)
     */
    public BatchNotificationResult sendPendingNotifications() {
        log.info("Processing batch IEP notifications");

        List<NotificationResult> allResults = new ArrayList<>();

        allResults.addAll(notifyCaseManagersOfUpcomingReviews());
        allResults.addAll(notifyCaseManagersOfOverdueEvaluations());

        long successCount = allResults.stream().filter(r -> "SENT".equals(r.getDeliveryStatus())).count();
        long failureCount = allResults.stream().filter(r -> "FAILED".equals(r.getDeliveryStatus())).count();

        BatchNotificationResult result = BatchNotificationResult.builder()
                .totalNotifications(allResults.size())
                .successCount((int) successCount)
                .failureCount((int) failureCount)
                .processedAt(LocalDateTime.now())
                .build();

        log.info("Batch processing complete: {} sent, {} failed", successCount, failureCount);
        return result;
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private NotificationResult sendNotification(String recipientType, String subject, String message, IEP iep) {
        log.info("Sending {} notification for IEP {}: {}", recipientType, iep.getId(), subject);

        String recipient = getRecipientContact(iep, recipientType);

        boolean sent = false;
        if (emailEnabled) {
            sent = sendEmail(recipient, subject, message);
        }

        return NotificationResult.builder()
                .recipient(recipient)
                .recipientType(recipientType)
                .subject(subject)
                .message(message)
                .sentAt(LocalDateTime.now())
                .deliveryStatus(sent ? "SENT" : "FAILED")
                .provider(NOTIFICATION_PROVIDER)
                .build();
    }

    private String getRecipientContact(IEP iep, String recipientType) {
        if ("PARENT".equals(recipientType)) {
            return iep.getStudent().getEmail() != null ?
                    iep.getStudent().getEmail() : "parent@example.com";
        }
        return "system@example.com";
    }

    private boolean sendEmail(String to, String subject, String message) {
        log.info("MOCK: Sending email to {} - Subject: {}", to, subject);
        return emailEnabled;
    }

    // ========================================================================
    // DTO CLASSES
    // ========================================================================

    @Data
    @Builder
    public static class NotificationResult {
        private String recipient;
        private String recipientType;
        private String subject;
        private String message;
        private LocalDateTime sentAt;
        private String deliveryStatus;
        private String provider;
        private String priority;
    }

    @Data
    @Builder
    public static class BatchNotificationResult {
        private Integer totalNotifications;
        private Integer successCount;
        private Integer failureCount;
        private LocalDateTime processedAt;
    }
}
