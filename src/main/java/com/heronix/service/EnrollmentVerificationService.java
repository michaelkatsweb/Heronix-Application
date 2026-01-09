package com.heronix.service;

import com.heronix.model.domain.EnrollmentVerification;
import com.heronix.model.domain.EnrollmentVerification.VerificationStatus;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.User;
import com.heronix.repository.EnrollmentVerificationRepository;
import com.heronix.repository.StudentRepository;
import com.heronix.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for Enrollment Verification Management
 * Handles business logic for enrollment verification requests
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Service
@Transactional
public class EnrollmentVerificationService {

    @Autowired
    private EnrollmentVerificationRepository verificationRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    public EnrollmentVerification createVerification(Long studentId, EnrollmentVerification.VerificationPurpose purpose,
                                                    Long createdByStaffId) {
        log.info("Creating enrollment verification for student ID: {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        User createdBy = userRepository.findById(createdByStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + createdByStaffId));

        String verificationNumber = generateVerificationNumber();

        EnrollmentVerification verification = EnrollmentVerification.builder()
                .verificationNumber(verificationNumber)
                .status(VerificationStatus.DRAFT)
                .requestDate(LocalDate.now())
                .student(student)
                .purpose(purpose)
                .urgentRequest(false)
                .feePaid(false)
                .studentConsentObtained(false)
                .officialSeal(true)
                .hasExpiration(true)
                .validityDays(90)
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .build();

        EnrollmentVerification saved = verificationRepository.save(verification);
        log.info("Created verification: {}", saved.getVerificationNumber());
        return saved;
    }

    private String generateVerificationNumber() {
        int year = LocalDate.now().getYear();
        long count = verificationRepository.count();
        return String.format("ENV-%d-%06d", year, count + 1);
    }

    @Transactional(readOnly = true)
    public EnrollmentVerification getVerificationById(Long id) {
        return verificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Verification not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<EnrollmentVerification> getByStatus(VerificationStatus status) {
        return verificationRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentVerification> getPendingVerification() {
        return verificationRepository.findPendingVerification();
    }

    public EnrollmentVerification updateVerification(EnrollmentVerification verification, Long updatedByStaffId) {
        User updatedBy = userRepository.findById(updatedByStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + updatedByStaffId));

        verification.setUpdatedBy(updatedBy);
        verification.setUpdatedAt(LocalDateTime.now());
        verification.calculateTotalFee();
        verification.calculateValidityPeriod();

        return verificationRepository.save(verification);
    }

    public EnrollmentVerification verifyEnrollment(Long verificationId, Long verifiedByStaffId) {
        EnrollmentVerification verification = getVerificationById(verificationId);
        User verifiedBy = userRepository.findById(verifiedByStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + verifiedByStaffId));

        verification.setStatus(VerificationStatus.VERIFIED);
        verification.setVerifiedBy(verifiedBy);
        verification.setVerifiedDate(LocalDate.now());

        return updateVerification(verification, verifiedByStaffId);
    }

    public EnrollmentVerification generateDocument(Long verificationId, String documentPath, Long generatedByStaffId) {
        EnrollmentVerification verification = getVerificationById(verificationId);

        if (!verification.isReadyForGeneration()) {
            throw new IllegalStateException("Verification not ready for document generation");
        }

        User generatedBy = userRepository.findById(generatedByStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + generatedByStaffId));

        verification.setStatus(VerificationStatus.GENERATED);
        verification.setGeneratedBy(generatedBy);
        verification.setGeneratedDate(LocalDate.now());
        verification.setDocumentFilePath(documentPath);
        verification.setValidFrom(LocalDate.now());

        return updateVerification(verification, generatedByStaffId);
    }

    public EnrollmentVerification markAsDelivered(Long verificationId, String trackingNumber, Long updatedByStaffId) {
        EnrollmentVerification verification = getVerificationById(verificationId);

        verification.setStatus(VerificationStatus.DELIVERED);
        verification.setDeliveryDate(LocalDate.now());
        verification.setTrackingNumber(trackingNumber);

        return updateVerification(verification, updatedByStaffId);
    }

    public void deleteVerification(Long verificationId) {
        EnrollmentVerification verification = getVerificationById(verificationId);

        if (verification.getStatus() != VerificationStatus.DRAFT) {
            throw new IllegalStateException("Only draft verifications can be deleted");
        }

        verificationRepository.delete(verification);
    }

    @Transactional(readOnly = true)
    public EnrollmentVerification getByVerificationNumber(String verificationNumber) {
        return verificationRepository.findByVerificationNumber(verificationNumber)
                .orElseThrow(() -> new IllegalArgumentException("Verification not found: " + verificationNumber));
    }

    @Transactional(readOnly = true)
    public List<EnrollmentVerification> getByPurpose(EnrollmentVerification.VerificationPurpose purpose) {
        return verificationRepository.findByPurpose(purpose);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentVerification> getUrgentVerifications() {
        return verificationRepository.findByUrgentRequestTrue();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentVerification> getUnpaidFees() {
        return verificationRepository.findByFeePaidFalse();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentVerification> getByStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        return verificationRepository.findAllByStudent(student);
    }

    public EnrollmentVerification verifyEnrollment(Long verificationId, Boolean fullTimeEnrollment,
                                                  Boolean academicGoodStanding, Double currentGPA,
                                                  Long verifiedByStaffId) {
        EnrollmentVerification verification = getVerificationById(verificationId);

        verification.setFullTimeEnrollment(fullTimeEnrollment);
        verification.setAcademicGoodStanding(academicGoodStanding);
        if (currentGPA != null) {
            verification.setIncludeGPA(true);
            verification.setCurrentGPA(currentGPA);
        }

        return verifyEnrollment(verificationId, verifiedByStaffId);
    }

    public EnrollmentVerification recordFeePayment(Long verificationId, Long updatedByStaffId) {
        EnrollmentVerification verification = getVerificationById(verificationId);
        verification.setFeePaid(true);
        return updateVerification(verification, updatedByStaffId);
    }

    public EnrollmentVerification sendVerification(Long verificationId, String deliveryMethod,
                                                  String trackingNumber, Long sentByStaffId) {
        EnrollmentVerification verification = getVerificationById(verificationId);
        verification.setStatus(VerificationStatus.DELIVERED);
        if (trackingNumber != null) {
            verification.setTrackingNumber(trackingNumber);
        }
        return updateVerification(verification, sentByStaffId);
    }

    public EnrollmentVerification cancelVerification(Long verificationId, String reason, Long cancelledByStaffId) {
        EnrollmentVerification verification = getVerificationById(verificationId);
        verification.setStatus(VerificationStatus.CANCELLED);
        return updateVerification(verification, cancelledByStaffId);
    }

    @Transactional(readOnly = true)
    public VerificationStatistics getStatistics() {
        long total = verificationRepository.count();
        long draft = verificationRepository.countByStatus(VerificationStatus.DRAFT);
        long pendingVerification = verificationRepository.countByStatus(VerificationStatus.PENDING_VERIFICATION);
        long verified = verificationRepository.countByStatus(VerificationStatus.VERIFIED);
        long generated = verificationRepository.countByStatus(VerificationStatus.GENERATED);
        long delivered = verificationRepository.countByStatus(VerificationStatus.DELIVERED);
        long cancelled = verificationRepository.countByStatus(VerificationStatus.CANCELLED);
        long urgent = verificationRepository.countByUrgentRequestTrue();
        long unpaidFees = verificationRepository.countByFeePaidFalse();

        return new VerificationStatistics(
                total, draft, pendingVerification, verified, generated, delivered, cancelled,
                urgent, unpaidFees
        );
    }

    @Transactional(readOnly = true)
    public List<Object[]> getCountByPurpose() {
        return verificationRepository.getCountByPurpose();
    }

    public record VerificationStatistics(
            long total,
            long draft,
            long pendingVerification,
            long verified,
            long generated,
            long delivered,
            long cancelled,
            long urgent,
            long unpaidFees
    ) {}
}
