package com.heronix.controller.api;

import com.heronix.model.domain.TeacherCertification;
import com.heronix.repository.TeacherCertificationRepository;
import com.heronix.service.TeacherCertificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Teacher Certification Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/teacher-certifications")
@RequiredArgsConstructor
public class TeacherCertificationApiController {

    private final TeacherCertificationService certificationService;
    private final TeacherCertificationRepository certificationRepository;

    // ==================== Certification CRUD Operations ====================

    @GetMapping("/{id}")
    public ResponseEntity<TeacherCertification> getCertificationById(@PathVariable Long id) {
        TeacherCertification certification = certificationService.getCertificationById(id);
        return ResponseEntity.ok(certification);
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<TeacherCertification>> getCertificationsByTeacher(@PathVariable Long teacherId) {
        List<TeacherCertification> certifications = certificationService.getCertificationsByTeacher(teacherId);
        return ResponseEntity.ok(certifications);
    }

    @GetMapping("/teacher/{teacherId}/active")
    public ResponseEntity<List<TeacherCertification>> getActiveCertificationsByTeacher(@PathVariable Long teacherId) {
        List<TeacherCertification> certifications = certificationService.getActiveCertificationsByTeacher(teacherId);
        return ResponseEntity.ok(certifications);
    }

    @PostMapping
    public ResponseEntity<TeacherCertification> createCertification(@RequestBody TeacherCertification certification) {
        TeacherCertification created = certificationService.createCertification(certification);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeacherCertification> updateCertification(
            @PathVariable Long id,
            @RequestBody TeacherCertification certification) {
        certification.setId(id);
        TeacherCertification updated = certificationService.updateCertification(certification);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCertification(@PathVariable Long id) {
        certificationService.deleteCertification(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Certification Status Queries ====================

    @GetMapping("/expiring-soon")
    public ResponseEntity<List<TeacherCertification>> getExpiringSoonCertifications(
            @RequestParam(defaultValue = "90") int daysAhead) {
        List<TeacherCertification> certifications = certificationService.getExpiringSoonCertifications(daysAhead);
        return ResponseEntity.ok(certifications);
    }

    @GetMapping("/expired")
    public ResponseEntity<List<TeacherCertification>> getExpiredCertifications() {
        List<TeacherCertification> certifications = certificationService.getExpiredCertifications();
        return ResponseEntity.ok(certifications);
    }

    @GetMapping("/needing-renewal-reminder")
    public ResponseEntity<List<TeacherCertification>> getCertificationsNeedingRenewalReminder(
            @RequestParam(defaultValue = "60") int daysBeforeExpiration) {
        List<TeacherCertification> certifications =
                certificationService.getCertificationsNeedingRenewalReminder(daysBeforeExpiration);
        return ResponseEntity.ok(certifications);
    }

    @GetMapping("/highly-qualified-teachers")
    public ResponseEntity<List<TeacherCertification>> getHighlyQualifiedTeachers() {
        List<TeacherCertification> certifications = certificationService.getHighlyQualifiedTeachers();
        return ResponseEntity.ok(certifications);
    }

    @GetMapping("/out-of-field-teaching")
    public ResponseEntity<List<TeacherCertification>> getOutOfFieldTeaching() {
        List<TeacherCertification> certifications = certificationService.getOutOfFieldTeaching();
        return ResponseEntity.ok(certifications);
    }

    // ==================== Renewal Operations ====================

    @PatchMapping("/{id}/send-renewal-reminder")
    public ResponseEntity<TeacherCertification> sendRenewalReminder(@PathVariable Long id) {
        TeacherCertification updated = certificationService.sendRenewalReminder(id);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/start-renewal")
    public ResponseEntity<TeacherCertification> startRenewalProcess(@PathVariable Long id) {
        TeacherCertification updated = certificationService.startRenewalProcess(id);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/approve-renewal")
    public ResponseEntity<TeacherCertification> approveRenewal(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newExpirationDate) {
        TeacherCertification updated = certificationService.approveRenewal(id, newExpirationDate);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/deny-renewal")
    public ResponseEntity<TeacherCertification> denyRenewal(
            @PathVariable Long id,
            @RequestParam String reason) {
        TeacherCertification updated = certificationService.denyRenewal(id, reason);
        return ResponseEntity.ok(updated);
    }

    // ==================== Status Update Operations ====================

    @PostMapping("/update-expired-status")
    public ResponseEntity<Map<String, String>> updateExpiredCertifications() {
        certificationService.updateExpiredCertifications();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Expired certifications updated successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update-expiring-soon-status")
    public ResponseEntity<Map<String, String>> updateExpiringSoonStatus(
            @RequestParam(defaultValue = "90") int daysAhead) {
        certificationService.updateExpiringSoonStatus(daysAhead);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Expiring soon status updated successfully");
        return ResponseEntity.ok(response);
    }

    // ==================== Statistics ====================

    @GetMapping("/statistics/counts")
    public ResponseEntity<Map<String, Long>> getCertificationCounts() {
        Map<String, Long> counts = new HashMap<>();

        long total = certificationRepository.count();
        long expiringSoon = certificationService.getExpiringSoonCertifications(90).size();
        long expired = certificationService.getExpiredCertifications().size();
        long needingReminder = certificationService.getCertificationsNeedingRenewalReminder(60).size();
        long highlyQualified = certificationService.getHighlyQualifiedTeachers().size();
        long outOfField = certificationService.getOutOfFieldTeaching().size();

        counts.put("total", total);
        counts.put("expiringSoon", (long) expiringSoon);
        counts.put("expired", (long) expired);
        counts.put("needingRenewalReminder", (long) needingReminder);
        counts.put("highlyQualified", (long) highlyQualified);
        counts.put("outOfField", (long) outOfField);

        return ResponseEntity.ok(counts);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getDashboardOverview() {
        Map<String, Object> dashboard = new HashMap<>();

        List<TeacherCertification> expiringSoon = certificationService.getExpiringSoonCertifications(90);
        List<TeacherCertification> expired = certificationService.getExpiredCertifications();
        List<TeacherCertification> needingReminder = certificationService.getCertificationsNeedingRenewalReminder(60);
        List<TeacherCertification> highlyQualified = certificationService.getHighlyQualifiedTeachers();
        List<TeacherCertification> outOfField = certificationService.getOutOfFieldTeaching();

        dashboard.put("expiringSoonCertifications", expiringSoon);
        dashboard.put("expiringSoonCount", expiringSoon.size());
        dashboard.put("expiredCertifications", expired);
        dashboard.put("expiredCount", expired.size());
        dashboard.put("needingRenewalReminder", needingReminder);
        dashboard.put("needingReminderCount", needingReminder.size());
        dashboard.put("highlyQualifiedTeachers", highlyQualified);
        dashboard.put("highlyQualifiedCount", highlyQualified.size());
        dashboard.put("outOfFieldTeaching", outOfField);
        dashboard.put("outOfFieldCount", outOfField.size());

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/teacher/{teacherId}")
    public ResponseEntity<Map<String, Object>> getTeacherDashboard(@PathVariable Long teacherId) {
        Map<String, Object> dashboard = new HashMap<>();

        List<TeacherCertification> allCertifications = certificationService.getCertificationsByTeacher(teacherId);
        List<TeacherCertification> activeCertifications = certificationService.getActiveCertificationsByTeacher(teacherId);

        long activeCount = activeCertifications.size();
        long expiredCount = allCertifications.stream()
                .filter(c -> c.getStatus() == TeacherCertification.CertificationStatus.EXPIRED)
                .count();
        long expiringSoonCount = allCertifications.stream()
                .filter(c -> c.getStatus() == TeacherCertification.CertificationStatus.EXPIRING_SOON)
                .count();
        long pendingRenewalCount = allCertifications.stream()
                .filter(c -> c.getStatus() == TeacherCertification.CertificationStatus.PENDING_RENEWAL)
                .count();

        dashboard.put("teacherId", teacherId);
        dashboard.put("allCertifications", allCertifications);
        dashboard.put("activeCertifications", activeCertifications);
        dashboard.put("totalCount", allCertifications.size());
        dashboard.put("activeCount", activeCount);
        dashboard.put("expiredCount", expiredCount);
        dashboard.put("expiringSoonCount", expiringSoonCount);
        dashboard.put("pendingRenewalCount", pendingRenewalCount);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/compliance")
    public ResponseEntity<Map<String, Object>> getComplianceDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        List<TeacherCertification> expired = certificationService.getExpiredCertifications();
        List<TeacherCertification> expiringSoon30 = certificationService.getExpiringSoonCertifications(30);
        List<TeacherCertification> expiringSoon60 = certificationService.getExpiringSoonCertifications(60);
        List<TeacherCertification> expiringSoon90 = certificationService.getExpiringSoonCertifications(90);
        List<TeacherCertification> outOfField = certificationService.getOutOfFieldTeaching();

        dashboard.put("expiredCertifications", expired);
        dashboard.put("expiredCount", expired.size());
        dashboard.put("expiringSoon30Days", expiringSoon30);
        dashboard.put("expiringSoon30Count", expiringSoon30.size());
        dashboard.put("expiringSoon60Days", expiringSoon60);
        dashboard.put("expiringSoon60Count", expiringSoon60.size());
        dashboard.put("expiringSoon90Days", expiringSoon90);
        dashboard.put("expiringSoon90Count", expiringSoon90.size());
        dashboard.put("outOfFieldTeaching", outOfField);
        dashboard.put("outOfFieldCount", outOfField.size());

        return ResponseEntity.ok(dashboard);
    }
}
