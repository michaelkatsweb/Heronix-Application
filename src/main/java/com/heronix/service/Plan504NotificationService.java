package com.heronix.service;

import com.heronix.model.domain.Plan504;
import com.heronix.model.domain.Student;
import com.heronix.repository.Plan504Repository;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 504 Plan Notification Service
 *
 * Manages notifications to parents, guardians, and staff regarding 504 plan updates,
 * reviews, and important events.
 *
 * Key Responsibilities:
 * - Send plan update notifications to parents
 * - Alert staff of new/modified plans
 * - Remind coordinators of upcoming reviews
 * - Notify teachers of accommodation changes
 * - Send expiration warnings
 * - Generate compliance notifications
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - 504 Management Enhancement
 */
@Slf4j
@Service
public class Plan504NotificationService {

    @Autowired
    private Plan504Repository plan504Repository;

    @Value("${notification.email.enabled:true}")
    private boolean emailEnabled;

    @Value("${notification.sms.enabled:false}")
    private boolean smsEnabled;

    // ========================================================================
    // PARENT NOTIFICATIONS
    // ========================================================================

    /**
     * Notify parents of plan creation
     */
    public NotificationResult notifyParentsOfPlanCreation(Long planId) {
        log.info("Notifying parents of 504 plan creation: {}", planId);

        Plan504 plan = plan504Repository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("504 Plan not found: " + planId));

        Student student = plan.getStudent();

        String subject = String.format("504 Plan Created for %s", student.getFullName());
        String message = String.format(
                "A 504 plan (#%s) has been created for your child, %s.\n\n" +
                "Plan Start Date: %s\n" +
                "Plan End Date: %s\n" +
                "Coordinator: %s\n\n" +
                "Please contact the school if you have any questions.",
                plan.getPlanNumber(),
                student.getFullName(),
                plan.getStartDate(),
                plan.getEndDate(),
                plan.getCoordinator()
        );

        return sendNotification("PARENT", subject, message, plan);
    }

    /**
     * Notify parents of plan updates
     */
    public NotificationResult notifyParentsOfPlanUpdate(Long planId, String updateDescription) {
        log.info("Notifying parents of 504 plan update: {}", planId);

        Plan504 plan = plan504Repository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("504 Plan not found: " + planId));

        String subject = String.format("504 Plan Updated - %s", plan.getStudent().getFullName());
        String message = String.format(
                "The 504 plan for %s has been updated.\n\n" +
                "Plan Number: %s\n" +
                "Update: %s\n" +
                "Updated: %s\n\n" +
                "Please review the updated plan and contact us with any questions.",
                plan.getStudent().getFullName(),
                plan.getPlanNumber(),
                updateDescription,
                LocalDate.now()
        );

        return sendNotification("PARENT", subject, message, plan);
    }

    /**
     * Notify parents of upcoming review
     */
    public NotificationResult notifyParentsOfUpcomingReview(Long planId) {
        log.info("Notifying parents of upcoming 504 review: {}", planId);

        Plan504 plan = plan504Repository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("504 Plan not found: " + planId));

        String subject = String.format("504 Plan Review Scheduled - %s",
                plan.getStudent().getFullName());

        String message = String.format(
                "The annual review for %s's 504 plan is scheduled.\n\n" +
                "Plan Number: %s\n" +
                "Review Date: %s\n" +
                "Coordinator: %s\n\n" +
                "We will contact you soon to schedule a meeting time.\n" +
                "Please prepare any feedback or concerns you'd like to discuss.",
                plan.getStudent().getFullName(),
                plan.getPlanNumber(),
                plan.getNextReviewDate(),
                plan.getCoordinator()
        );

        return sendNotification("PARENT", subject, message, plan);
    }

    // ========================================================================
    // STAFF NOTIFICATIONS
    // ========================================================================

    /**
     * Notify teachers of new student with 504 plan
     */
    public List<NotificationResult> notifyTeachersOfNewPlan(Long planId, List<String> teacherEmails) {
        log.info("Notifying {} teachers of new 504 plan", teacherEmails.size());

        Plan504 plan = plan504Repository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("504 Plan not found: " + planId));

        String subject = String.format("New 504 Plan - %s", plan.getStudent().getFullName());
        String message = String.format(
                "A student in your class has a 504 plan that requires accommodations.\n\n" +
                "Student: %s\n" +
                "Plan Number: %s\n" +
                "Coordinator: %s\n\n" +
                "ACCOMMODATIONS:\n%s\n\n" +
                "Please review and implement these accommodations immediately.\n" +
                "Contact %s with any questions.",
                plan.getStudent().getFullName(),
                plan.getPlanNumber(),
                plan.getCoordinator(),
                plan.getAccommodations(),
                plan.getCoordinator()
        );

        List<NotificationResult> results = new ArrayList<>();
        for (String email : teacherEmails) {
            results.add(sendNotification("TEACHER", subject, message, plan, email));
        }

        return results;
    }

    /**
     * Notify coordinator of upcoming review deadlines
     */
    public List<NotificationResult> notifyCoordinatorsOfReviewDeadlines() {
        log.info("Sending review deadline notifications to coordinators");

        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysOut = today.plusDays(30);

        List<Plan504> plansNeedingReview = plan504Repository
                .findPlansNeedingRenewal(today, thirtyDaysOut);

        // Group by coordinator
        Map<String, List<Plan504>> plansByCoordinator = new HashMap<>();
        for (Plan504 plan : plansNeedingReview) {
            String coordinator = plan.getCoordinator();
            plansByCoordinator.computeIfAbsent(coordinator, k -> new ArrayList<>()).add(plan);
        }

        List<NotificationResult> results = new ArrayList<>();

        for (Map.Entry<String, List<Plan504>> entry : plansByCoordinator.entrySet()) {
            String coordinator = entry.getKey();
            List<Plan504> plans = entry.getValue();

            StringBuilder planList = new StringBuilder();
            for (Plan504 plan : plans) {
                planList.append(String.format("- %s (%s) - Review due: %s\n",
                        plan.getStudent().getFullName(),
                        plan.getPlanNumber(),
                        plan.getNextReviewDate()));
            }

            String subject = String.format("504 Plan Reviews Due - %d Plans", plans.size());
            String message = String.format(
                    "You have %d 504 plans due for review in the next 30 days:\n\n%s\n" +
                    "Please schedule review meetings for these students.",
                    plans.size(),
                    planList.toString()
            );

            results.add(sendNotification("COORDINATOR", subject, message, null, coordinator));
        }

        return results;
    }

    // ========================================================================
    // COMPLIANCE NOTIFICATIONS
    // ========================================================================

    /**
     * Send notification for expired plans
     */
    public List<NotificationResult> notifyOfExpiredPlans() {
        log.info("Checking for expired 504 plans");

        LocalDate today = LocalDate.now();
        List<Plan504> expiredPlans = plan504Repository.findExpiredPlans(today);

        List<NotificationResult> results = new ArrayList<>();

        for (Plan504 plan : expiredPlans) {
            String subject = String.format("URGENT: 504 Plan Expired - %s",
                    plan.getStudent().getFullName());

            String message = String.format(
                    "The 504 plan for %s has EXPIRED.\n\n" +
                    "Plan Number: %s\n" +
                    "End Date: %s\n" +
                    "Coordinator: %s\n\n" +
                    "IMMEDIATE ACTION REQUIRED:\n" +
                    "1. Contact parents to schedule renewal meeting\n" +
                    "2. Update or renew the plan\n" +
                    "3. Ensure compliance with Section 504 requirements",
                    plan.getStudent().getFullName(),
                    plan.getPlanNumber(),
                    plan.getEndDate(),
                    plan.getCoordinator()
            );

            results.add(sendNotification("COMPLIANCE", subject, message, plan));
        }

        return results;
    }

    // ========================================================================
    // NOTIFICATION SENDING
    // ========================================================================

    /**
     * Send notification (email, SMS, or both)
     */
    private NotificationResult sendNotification(
            String recipientType,
            String subject,
            String message,
            Plan504 plan) {

        return sendNotification(recipientType, subject, message, plan, null);
    }

    /**
     * Send notification to specific recipient
     */
    private NotificationResult sendNotification(
            String recipientType,
            String subject,
            String message,
            Plan504 plan,
            String specificRecipient) {

        log.info("Sending {} notification: {}", recipientType, subject);

        // Mock implementation - in production would integrate with actual email/SMS service
        boolean emailSent = false;
        boolean smsSent = false;

        if (emailEnabled) {
            // Would send actual email here
            emailSent = true;
            log.info("Email sent: {}", subject);
        }

        if (smsEnabled) {
            // Would send actual SMS here
            smsSent = true;
            log.info("SMS sent: {}", subject);
        }

        return NotificationResult.builder()
                .recipientType(recipientType)
                .subject(subject)
                .message(message)
                .planId(plan != null ? plan.getId() : null)
                .planNumber(plan != null ? plan.getPlanNumber() : null)
                .emailSent(emailSent)
                .smsSent(smsSent)
                .sentAt(LocalDateTime.now())
                .success(emailSent || smsSent)
                .build();
    }

    // ========================================================================
    // BATCH NOTIFICATIONS
    // ========================================================================

    /**
     * Send all pending notifications (scheduled job)
     */
    public BatchNotificationResult sendPendingNotifications() {
        log.info("Processing pending notifications");

        List<NotificationResult> results = new ArrayList<>();

        // Review deadlines
        results.addAll(notifyCoordinatorsOfReviewDeadlines());

        // Expired plans
        results.addAll(notifyOfExpiredPlans());

        int successCount = (int) results.stream().filter(NotificationResult::isSuccess).count();
        int failureCount = results.size() - successCount;

        return BatchNotificationResult.builder()
                .totalNotifications(results.size())
                .successCount(successCount)
                .failureCount(failureCount)
                .notifications(results)
                .processedAt(LocalDateTime.now())
                .build();
    }

    // ========================================================================
    // DTO CLASSES
    // ========================================================================

    @Data
    @Builder
    public static class NotificationResult {
        private String recipientType;
        private String subject;
        private String message;
        private Long planId;
        private String planNumber;
        private boolean emailSent;
        private boolean smsSent;
        private LocalDateTime sentAt;
        private boolean success;
    }

    @Data
    @Builder
    public static class BatchNotificationResult {
        private int totalNotifications;
        private int successCount;
        private int failureCount;
        private List<NotificationResult> notifications;
        private LocalDateTime processedAt;
    }
}
