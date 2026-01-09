package com.heronix.service;

import com.heronix.model.domain.Teacher;
import com.heronix.model.domain.TeacherCertification;
import com.heronix.repository.TeacherCertificationRepository;
import com.heronix.repository.TeacherRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing teacher certifications
 * Handles certification tracking, renewals, and compliance
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Service
@Transactional
public class TeacherCertificationService {

    @Autowired
    private TeacherCertificationRepository certificationRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    // ========================================================================
    // CRUD OPERATIONS
    // ========================================================================

    public TeacherCertification createCertification(TeacherCertification certification) {
        log.info("Creating certification for teacher {}", certification.getTeacher().getId());
        return certificationRepository.save(certification);
    }

    public TeacherCertification getCertificationById(Long id) {
        return certificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Certification not found: " + id));
    }

    public TeacherCertification updateCertification(TeacherCertification certification) {
        log.info("Updating certification {}", certification.getId());
        return certificationRepository.save(certification);
    }

    public void deleteCertification(Long id) {
        log.info("Deleting certification {}", id);
        certificationRepository.deleteById(id);
    }

    // ========================================================================
    // QUERY OPERATIONS
    // ========================================================================

    public List<TeacherCertification> getCertificationsByTeacher(Long teacherId) {
        return certificationRepository.findByTeacherIdOrderByExpirationDateDesc(teacherId);
    }

    public List<TeacherCertification> getActiveCertificationsByTeacher(Long teacherId) {
        return certificationRepository.findActiveByTeacher(teacherId);
    }

    public List<TeacherCertification> getExpiringSoonCertifications(int daysAhead) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(daysAhead);
        return certificationRepository.findExpiringSoon(today, futureDate);
    }

    public List<TeacherCertification> getExpiredCertifications() {
        return certificationRepository.findExpired(LocalDate.now());
    }

    public List<TeacherCertification> getCertificationsNeedingRenewalReminder(int daysBeforeExpiration) {
        LocalDate today = LocalDate.now();
        LocalDate reminderDate = today.plusDays(daysBeforeExpiration);
        return certificationRepository.findNeedingRenewalReminder(today, reminderDate);
    }

    public List<TeacherCertification> getHighlyQualifiedTeachers() {
        return certificationRepository.findHighlyQualifiedTeachers();
    }

    public List<TeacherCertification> getOutOfFieldTeaching() {
        return certificationRepository.findOutOfFieldTeaching();
    }

    // ========================================================================
    // RENEWAL OPERATIONS
    // ========================================================================

    public TeacherCertification sendRenewalReminder(Long certificationId) {
        log.info("Sending renewal reminder for certification {}", certificationId);
        TeacherCertification cert = getCertificationById(certificationId);
        cert.setRenewalReminderSent(true);
        cert.setRenewalReminderDate(LocalDate.now());
        return certificationRepository.save(cert);
    }

    public TeacherCertification startRenewalProcess(Long certificationId) {
        log.info("Starting renewal process for certification {}", certificationId);
        TeacherCertification cert = getCertificationById(certificationId);
        cert.setRenewalInProgress(true);
        cert.setRenewalApplicationDate(LocalDate.now());
        cert.setStatus(TeacherCertification.CertificationStatus.PENDING_RENEWAL);
        return certificationRepository.save(cert);
    }

    public TeacherCertification approveRenewal(Long certificationId, LocalDate newExpirationDate) {
        log.info("Approving renewal for certification {}", certificationId);
        TeacherCertification cert = getCertificationById(certificationId);
        cert.setRenewalApprovalDate(LocalDate.now());
        cert.setRenewedExpirationDate(newExpirationDate);
        cert.setExpirationDate(newExpirationDate);
        cert.setStatus(TeacherCertification.CertificationStatus.RENEWED);
        cert.setRenewalInProgress(false);
        return certificationRepository.save(cert);
    }

    public TeacherCertification denyRenewal(Long certificationId, String reason) {
        log.info("Denying renewal for certification {}", certificationId);
        TeacherCertification cert = getCertificationById(certificationId);
        cert.setRenewalDenialDate(LocalDate.now());
        cert.setRenewalDenialReason(reason);
        cert.setStatus(TeacherCertification.CertificationStatus.DENIED);
        cert.setRenewalInProgress(false);
        return certificationRepository.save(cert);
    }

    // ========================================================================
    // STATUS UPDATES
    // ========================================================================

    public void updateExpiredCertifications() {
        log.info("Updating expired certifications");
        List<TeacherCertification> expired = getExpiredCertifications();
        expired.forEach(cert -> {
            cert.setStatus(TeacherCertification.CertificationStatus.EXPIRED);
            certificationRepository.save(cert);
        });
        log.info("Updated {} expired certifications", expired.size());
    }

    public void updateExpiringSoonStatus(int daysAhead) {
        log.info("Updating expiring soon status ({}  days)", daysAhead);
        List<TeacherCertification> expiringSoon = getExpiringSoonCertifications(daysAhead);
        expiringSoon.forEach(cert -> {
            if (cert.getStatus() == TeacherCertification.CertificationStatus.ACTIVE) {
                cert.setStatus(TeacherCertification.CertificationStatus.EXPIRING_SOON);
                certificationRepository.save(cert);
            }
        });
        log.info("Updated {} certifications to expiring soon status", expiringSoon.size());
    }
}
