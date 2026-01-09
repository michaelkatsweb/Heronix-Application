package com.heronix.service;

import com.heronix.model.domain.AttendanceRecord;
import com.heronix.model.domain.AttendanceRecord.AttendanceStatus;
import com.heronix.model.domain.Student;
import com.heronix.repository.AttendanceRepository;
import com.heronix.repository.StudentRepository;
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
 * Attendance Notification Service
 *
 * Manages automated notifications to parents, guardians, and administrators
 * regarding student absences, tardies, and attendance concerns.
 *
 * Key Responsibilities:
 * - Send real-time absence notifications to parents
 * - Daily attendance digest for chronic absences
 * - Tardy accumulation alerts
 * - Consecutive absence alerts
 * - Weekly attendance summary to parents
 * - Administrator alerts for attendance concerns
 * - Batch notification processing
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Attendance Enhancement
 */
@Slf4j
@Service
public class AttendanceNotificationService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Value("${notification.email.enabled:true}")
    private boolean emailEnabled;

    @Value("${notification.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${attendance.notification.absence.enabled:true}")
    private boolean absenceNotificationEnabled;

    @Value("${attendance.notification.tardy.threshold:3}")
    private int tardyNotificationThreshold;

    @Value("${attendance.notification.consecutive.threshold:3}")
    private int consecutiveAbsenceThreshold;

    private static final String NOTIFICATION_PROVIDER = "MOCK";

    // ========================================================================
    // REAL-TIME ABSENCE NOTIFICATIONS
    // ========================================================================

    /**
     * Send immediate notification to parent when student is marked absent
     */
    public NotificationResult notifyParentOfAbsence(Long attendanceRecordId) {
        log.info("Sending parent notification for absence: {}", attendanceRecordId);

        AttendanceRecord record = attendanceRepository.findById(attendanceRecordId)
                .orElseThrow(() -> new IllegalArgumentException("Attendance record not found: " + attendanceRecordId));

        if (!record.isAbsent()) {
            log.warn("Attendance record {} is not an absence, skipping notification", attendanceRecordId);
            return NotificationResult.builder()
                    .recordId(attendanceRecordId)
                    .notificationType("ABSENCE")
                    .deliveryStatus("SKIPPED")
                    .reason("Not an absence")
                    .build();
        }

        Student student = record.getStudent();
        String parentEmail = getParentEmail(student);
        String parentPhone = getParentPhone(student);

        String subject = String.format("Absence Notification - %s", student.getFullName());
        String message = buildAbsenceMessage(record, student);

        boolean emailSent = false;
        boolean smsSent = false;

        if (emailEnabled && absenceNotificationEnabled) {
            emailSent = sendEmail(parentEmail, subject, message);
        }

        if (smsEnabled && absenceNotificationEnabled) {
            smsSent = sendSMS(parentPhone, buildAbsenceSMSMessage(record, student));
        }

        return NotificationResult.builder()
                .recordId(attendanceRecordId)
                .studentId(student.getId())
                .studentName(student.getFullName())
                .recipientEmail(parentEmail)
                .recipientPhone(parentPhone)
                .notificationType("ABSENCE")
                .subject(subject)
                .message(message)
                .sentAt(LocalDateTime.now())
                .emailSent(emailSent)
                .smsSent(smsSent)
                .deliveryStatus((emailSent || smsSent) ? "SENT" : "FAILED")
                .provider(NOTIFICATION_PROVIDER)
                .build();
    }

    /**
     * Send notification for tardy arrival
     */
    public NotificationResult notifyParentOfTardy(Long attendanceRecordId) {
        log.info("Sending parent notification for tardy: {}", attendanceRecordId);

        AttendanceRecord record = attendanceRepository.findById(attendanceRecordId)
                .orElseThrow(() -> new IllegalArgumentException("Attendance record not found: " + attendanceRecordId));

        if (record.getStatus() != AttendanceStatus.TARDY) {
            return NotificationResult.builder()
                    .recordId(attendanceRecordId)
                    .notificationType("TARDY")
                    .deliveryStatus("SKIPPED")
                    .reason("Not a tardy")
                    .build();
        }

        Student student = record.getStudent();
        String parentEmail = getParentEmail(student);

        String subject = String.format("Tardy Notification - %s", student.getFullName());
        String message = buildTardyMessage(record, student);

        boolean emailSent = emailEnabled ? sendEmail(parentEmail, subject, message) : false;

        return NotificationResult.builder()
                .recordId(attendanceRecordId)
                .studentId(student.getId())
                .studentName(student.getFullName())
                .recipientEmail(parentEmail)
                .notificationType("TARDY")
                .subject(subject)
                .message(message)
                .sentAt(LocalDateTime.now())
                .emailSent(emailSent)
                .deliveryStatus(emailSent ? "SENT" : "FAILED")
                .provider(NOTIFICATION_PROVIDER)
                .build();
    }

    // ========================================================================
    // CONSECUTIVE ABSENCE ALERTS
    // ========================================================================

    /**
     * Check for consecutive absences and notify if threshold reached
     */
    public List<NotificationResult> notifyConsecutiveAbsences(LocalDate asOfDate) {
        log.info("Checking for consecutive absences as of {}", asOfDate);

        List<Student> allStudents = studentRepository.findAllActive();
        List<NotificationResult> results = new ArrayList<>();

        for (Student student : allStudents) {
            int consecutiveAbsences = countConsecutiveAbsences(student.getId(), asOfDate);

            if (consecutiveAbsences >= consecutiveAbsenceThreshold) {
                NotificationResult result = sendConsecutiveAbsenceAlert(student, consecutiveAbsences, asOfDate);
                results.add(result);
            }
        }

        log.info("Sent {} consecutive absence alerts", results.size());
        return results;
    }

    private NotificationResult sendConsecutiveAbsenceAlert(Student student, int consecutiveDays, LocalDate asOfDate) {
        String parentEmail = getParentEmail(student);
        String subject = String.format("ALERT: %d Consecutive Absences - %s", consecutiveDays, student.getFullName());
        String message = String.format(
                "This is an important notification regarding %s's attendance.\n\n" +
                "Your child has been absent for %d consecutive school days as of %s.\n\n" +
                "Consecutive absences can significantly impact academic progress. " +
                "Please contact the school immediately if there are extenuating circumstances.\n\n" +
                "If your child has been ill, please provide documentation from a healthcare provider.\n\n" +
                "School Contact: [School Phone]\n" +
                "Attendance Office: [Attendance Email]",
                student.getFullName(),
                consecutiveDays,
                asOfDate,
                consecutiveDays
        );

        boolean emailSent = emailEnabled ? sendEmail(parentEmail, subject, message) : false;

        return NotificationResult.builder()
                .studentId(student.getId())
                .studentName(student.getFullName())
                .recipientEmail(parentEmail)
                .notificationType("CONSECUTIVE_ABSENCE")
                .subject(subject)
                .message(message)
                .sentAt(LocalDateTime.now())
                .emailSent(emailSent)
                .deliveryStatus(emailSent ? "SENT" : "FAILED")
                .metadata(Map.of("consecutiveDays", consecutiveDays, "asOfDate", asOfDate.toString()))
                .priority("HIGH")
                .provider(NOTIFICATION_PROVIDER)
                .build();
    }

    // ========================================================================
    // TARDY ACCUMULATION ALERTS
    // ========================================================================

    /**
     * Notify when student reaches tardy threshold
     */
    public List<NotificationResult> notifyTardyAccumulation(LocalDate startDate, LocalDate endDate) {
        log.info("Checking tardy accumulation from {} to {}", startDate, endDate);

        List<Student> allStudents = studentRepository.findAllActive();
        List<NotificationResult> results = new ArrayList<>();

        for (Student student : allStudents) {
            long tardyCount = countTardies(student.getId(), startDate, endDate);

            if (tardyCount >= tardyNotificationThreshold) {
                NotificationResult result = sendTardyAccumulationAlert(student, (int) tardyCount, startDate, endDate);
                results.add(result);
            }
        }

        log.info("Sent {} tardy accumulation alerts", results.size());
        return results;
    }

    private NotificationResult sendTardyAccumulationAlert(Student student, int tardyCount, LocalDate startDate, LocalDate endDate) {
        String parentEmail = getParentEmail(student);
        String subject = String.format("Tardy Accumulation Alert - %s", student.getFullName());
        String message = String.format(
                "%s has accumulated %d tardies between %s and %s.\n\n" +
                "Excessive tardies can result in:\n" +
                "- Loss of instructional time\n" +
                "- Academic difficulties\n" +
                "- Disciplinary action\n\n" +
                "Please ensure your child arrives to school on time. " +
                "School starts at [School Start Time].\n\n" +
                "If you need assistance with transportation or other challenges, " +
                "please contact the school counselor.",
                student.getFullName(),
                tardyCount,
                startDate,
                endDate
        );

        boolean emailSent = emailEnabled ? sendEmail(parentEmail, subject, message) : false;

        return NotificationResult.builder()
                .studentId(student.getId())
                .studentName(student.getFullName())
                .recipientEmail(parentEmail)
                .notificationType("TARDY_ACCUMULATION")
                .subject(subject)
                .message(message)
                .sentAt(LocalDateTime.now())
                .emailSent(emailSent)
                .deliveryStatus(emailSent ? "SENT" : "FAILED")
                .metadata(Map.of("tardyCount", tardyCount, "startDate", startDate.toString(), "endDate", endDate.toString()))
                .priority("MEDIUM")
                .provider(NOTIFICATION_PROVIDER)
                .build();
    }

    // ========================================================================
    // WEEKLY ATTENDANCE SUMMARY
    // ========================================================================

    /**
     * Send weekly attendance summary to parents
     */
    public List<NotificationResult> sendWeeklyAttendanceSummary(LocalDate weekStart, LocalDate weekEnd) {
        log.info("Sending weekly attendance summaries for week {} to {}", weekStart, weekEnd);

        List<Student> allStudents = studentRepository.findAllActive();
        List<NotificationResult> results = new ArrayList<>();

        for (Student student : allStudents) {
            List<AttendanceRecord> weekRecords = attendanceRepository
                    .findByStudentIdAndAttendanceDateBetween(student.getId(), weekStart, weekEnd);

            if (!weekRecords.isEmpty()) {
                NotificationResult result = sendWeeklySummary(student, weekRecords, weekStart, weekEnd);
                results.add(result);
            }
        }

        log.info("Sent {} weekly attendance summaries", results.size());
        return results;
    }

    private NotificationResult sendWeeklySummary(Student student, List<AttendanceRecord> records, LocalDate weekStart, LocalDate weekEnd) {
        long presentCount = records.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        long absentCount = records.stream().filter(r -> r.isAbsent()).count();
        long tardyCount = records.stream().filter(r -> r.getStatus() == AttendanceStatus.TARDY).count();

        double attendanceRate = records.isEmpty() ? 0.0 : (double) presentCount / records.size() * 100;

        String parentEmail = getParentEmail(student);
        String subject = String.format("Weekly Attendance Summary - %s", student.getFullName());
        String message = String.format(
                "Weekly Attendance Summary for %s\n" +
                "Week of %s to %s\n\n" +
                "Days Present: %d\n" +
                "Days Absent: %d\n" +
                "Days Tardy: %d\n" +
                "Attendance Rate: %.1f%%\n\n" +
                "%s\n\n" +
                "Thank you for your continued support in ensuring regular school attendance.",
                student.getFullName(),
                weekStart,
                weekEnd,
                presentCount,
                absentCount,
                tardyCount,
                attendanceRate,
                getAttendanceMessage(attendanceRate)
        );

        boolean emailSent = emailEnabled ? sendEmail(parentEmail, subject, message) : false;

        return NotificationResult.builder()
                .studentId(student.getId())
                .studentName(student.getFullName())
                .recipientEmail(parentEmail)
                .notificationType("WEEKLY_SUMMARY")
                .subject(subject)
                .message(message)
                .sentAt(LocalDateTime.now())
                .emailSent(emailSent)
                .deliveryStatus(emailSent ? "SENT" : "FAILED")
                .metadata(Map.of(
                    "weekStart", weekStart.toString(),
                    "weekEnd", weekEnd.toString(),
                    "presentCount", presentCount,
                    "absentCount", absentCount,
                    "tardyCount", tardyCount,
                    "attendanceRate", attendanceRate
                ))
                .provider(NOTIFICATION_PROVIDER)
                .build();
    }

    // ========================================================================
    // DAILY ABSENCE DIGEST (BATCH PROCESSING)
    // ========================================================================

    /**
     * Send daily digest of all absences to administrators
     */
    public NotificationResult sendDailyAbsenceDigest(LocalDate date) {
        log.info("Sending daily absence digest for {}", date);

        List<AttendanceRecord> absences = attendanceRepository.findAll().stream()
                .filter(r -> r.getAttendanceDate().equals(date))
                .filter(AttendanceRecord::isAbsent)
                .collect(Collectors.toList());

        if (absences.isEmpty()) {
            return NotificationResult.builder()
                    .notificationType("DAILY_DIGEST")
                    .deliveryStatus("SKIPPED")
                    .reason("No absences for date")
                    .sentAt(LocalDateTime.now())
                    .build();
        }

        Map<String, Long> absencesByGrade = absences.stream()
                .collect(Collectors.groupingBy(
                    r -> r.getStudent().getGradeLevel(),
                    Collectors.counting()
                ));

        String subject = String.format("Daily Absence Report - %s (%d total absences)", date, absences.size());
        StringBuilder message = new StringBuilder();
        message.append(String.format("Daily Absence Report for %s\n\n", date));
        message.append(String.format("Total Absences: %d\n\n", absences.size()));
        message.append("Absences by Grade Level:\n");

        absencesByGrade.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> message.append(String.format("  Grade %s: %d absences\n", entry.getKey(), entry.getValue())));

        message.append("\nDetailed Absence List:\n");
        for (AttendanceRecord record : absences) {
            message.append(String.format("- %s (Grade %s) - %s - %s\n",
                    record.getStudent().getFullName(),
                    record.getStudent().getGradeLevel(),
                    record.getStatus(),
                    record.getNotes() != null ? record.getNotes() : "No notes"
            ));
        }

        boolean emailSent = emailEnabled ? sendEmail("attendance@school.edu", subject, message.toString()) : false;

        return NotificationResult.builder()
                .notificationType("DAILY_DIGEST")
                .subject(subject)
                .message(message.toString())
                .sentAt(LocalDateTime.now())
                .emailSent(emailSent)
                .deliveryStatus(emailSent ? "SENT" : "FAILED")
                .metadata(Map.of("date", date.toString(), "totalAbsences", absences.size()))
                .provider(NOTIFICATION_PROVIDER)
                .build();
    }

    // ========================================================================
    // BATCH PROCESSING
    // ========================================================================

    /**
     * Process all pending notifications for the day
     */
    public BatchNotificationResult processDailyNotifications(LocalDate date) {
        log.info("Processing daily notifications for {}", date);

        List<NotificationResult> allResults = new ArrayList<>();

        // Send daily absence digest
        allResults.add(sendDailyAbsenceDigest(date));

        // Check consecutive absences
        allResults.addAll(notifyConsecutiveAbsences(date));

        // Check tardy accumulation (last 30 days)
        LocalDate thirtyDaysAgo = date.minusDays(30);
        allResults.addAll(notifyTardyAccumulation(thirtyDaysAgo, date));

        long successCount = allResults.stream().filter(r -> "SENT".equals(r.getDeliveryStatus())).count();
        long failureCount = allResults.stream().filter(r -> "FAILED".equals(r.getDeliveryStatus())).count();
        long skippedCount = allResults.stream().filter(r -> "SKIPPED".equals(r.getDeliveryStatus())).count();

        return BatchNotificationResult.builder()
                .processDate(date)
                .totalNotifications(allResults.size())
                .successCount((int) successCount)
                .failureCount((int) failureCount)
                .skippedCount((int) skippedCount)
                .processedAt(LocalDateTime.now())
                .notifications(allResults)
                .build();
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private String buildAbsenceMessage(AttendanceRecord record, Student student) {
        return String.format(
                "This is to notify you that %s was marked absent on %s.\n\n" +
                "Date: %s\n" +
                "Period: %s\n" +
                "Status: %s\n" +
                "%s\n\n" +
                "If this absence is excused, please provide documentation to the attendance office.\n\n" +
                "Regular school attendance is crucial for academic success. " +
                "If you have questions or concerns, please contact the school.",
                student.getFullName(),
                record.getAttendanceDate(),
                record.getAttendanceDate(),
                record.getPeriodNumber() != null ? "Period " + record.getPeriodNumber() : "Full Day",
                record.getStatus(),
                record.getNotes() != null ? "Notes: " + record.getNotes() : ""
        );
    }

    private String buildAbsenceSMSMessage(AttendanceRecord record, Student student) {
        return String.format(
                "%s was absent on %s. Please contact school if excused.",
                student.getFirstName(),
                record.getAttendanceDate()
        );
    }

    private String buildTardyMessage(AttendanceRecord record, Student student) {
        return String.format(
                "%s arrived late to school on %s.\n\n" +
                "Arrival Time: %s\n" +
                "Period: %s\n\n" +
                "Regular, on-time attendance is important for academic success. " +
                "Please ensure your child arrives before the tardy bell.",
                student.getFullName(),
                record.getAttendanceDate(),
                record.getArrivalTime() != null ? record.getArrivalTime().toString() : "Not recorded",
                record.getPeriodNumber() != null ? "Period " + record.getPeriodNumber() : "School Start"
        );
    }

    private int countConsecutiveAbsences(Long studentId, LocalDate asOfDate) {
        int consecutive = 0;
        LocalDate current = asOfDate;

        for (int i = 0; i < 30; i++) { // Check up to 30 days back
            List<AttendanceRecord> dayRecords = attendanceRepository
                    .findByStudentIdAndAttendanceDateBetween(studentId, current, current);

            boolean wasAbsent = dayRecords.stream().anyMatch(AttendanceRecord::isAbsent);

            if (wasAbsent) {
                consecutive++;
                current = current.minusDays(1);
                // Skip weekends
                while (current.getDayOfWeek().getValue() >= 6) {
                    current = current.minusDays(1);
                }
            } else {
                break;
            }
        }

        return consecutive;
    }

    private long countTardies(Long studentId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByStudentIdAndAttendanceDateBetween(studentId, startDate, endDate)
                .stream()
                .filter(r -> r.getStatus() == AttendanceStatus.TARDY)
                .count();
    }

    private String getParentEmail(Student student) {
        // In production, would fetch from ParentGuardian relationship
        return student.getEmail() != null ? student.getEmail() : "parent@example.com";
    }

    private String getParentPhone(Student student) {
        // In production, would fetch from ParentGuardian relationship
        return student.getEmergencyPhone() != null ? student.getEmergencyPhone() : "555-0100";
    }

    private String getAttendanceMessage(double attendanceRate) {
        if (attendanceRate >= 95.0) {
            return "Excellent attendance! Keep up the great work!";
        } else if (attendanceRate >= 90.0) {
            return "Good attendance. A few more consistent days would be ideal.";
        } else if (attendanceRate >= 85.0) {
            return "Attendance needs improvement. Please work to reduce absences.";
        } else {
            return "CRITICAL: Attendance is below acceptable levels. Please contact the school immediately.";
        }
    }

    private boolean sendEmail(String to, String subject, String message) {
        log.info("MOCK: Sending email to {} - Subject: {}", to, subject);
        return emailEnabled;
    }

    private boolean sendSMS(String phone, String message) {
        log.info("MOCK: Sending SMS to {} - Message: {}", phone, message);
        return smsEnabled;
    }

    // ========================================================================
    // DTO CLASSES
    // ========================================================================

    @Data
    @Builder
    public static class NotificationResult {
        private Long recordId;
        private Long studentId;
        private String studentName;
        private String recipientEmail;
        private String recipientPhone;
        private String notificationType;
        private String subject;
        private String message;
        private LocalDateTime sentAt;
        private Boolean emailSent;
        private Boolean smsSent;
        private String deliveryStatus;
        private String provider;
        private String priority;
        private String reason;
        private Map<String, Object> metadata;
    }

    @Data
    @Builder
    public static class BatchNotificationResult {
        private LocalDate processDate;
        private Integer totalNotifications;
        private Integer successCount;
        private Integer failureCount;
        private Integer skippedCount;
        private LocalDateTime processedAt;
        private List<NotificationResult> notifications;
    }
}
