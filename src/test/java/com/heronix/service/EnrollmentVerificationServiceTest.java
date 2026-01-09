package com.heronix.service;

import com.heronix.model.domain.EnrollmentVerification;
import com.heronix.model.domain.EnrollmentVerification.VerificationStatus;
import com.heronix.model.domain.EnrollmentVerification.VerificationPurpose;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.User;
import com.heronix.repository.EnrollmentVerificationRepository;
import com.heronix.repository.StudentRepository;
import com.heronix.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for EnrollmentVerificationService
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@SpringBootTest(classes = com.heronix.HeronixSchedulerApplication.class)
@ActiveProfiles("test")
@Transactional
class EnrollmentVerificationServiceTest {

    @Autowired
    private EnrollmentVerificationService verificationService;

    @Autowired
    private EnrollmentVerificationRepository verificationRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    private Student testStudent;
    private User testStaff;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        verificationRepository.deleteAll();

        // Create test student if doesn't exist
        testStudent = studentRepository.findAll().stream().findFirst()
                .orElseGet(() -> {
                    Student student = new Student();
                    student.setFirstName("Test");
                    student.setLastName("Student");
                    student.setStudentId("ENV001");
                    return studentRepository.save(student);
                });

        // Create test staff user if doesn't exist
        testStaff = userRepository.findAll().stream().findFirst()
                .orElseGet(() -> {
                    User staff = new User();
                    staff.setUsername("teststaff");
                    staff.setEmail("teststaff@heronix.edu");
                    return userRepository.save(staff);
                });
    }

    @Test
    @DisplayName("Should create enrollment verification successfully")
    void testCreateVerification() {
        // Given
        VerificationPurpose purpose = VerificationPurpose.SCHOLARSHIP;

        // When
        EnrollmentVerification verification = verificationService.createVerification(
                testStudent.getId(), purpose, testStaff.getId());

        // Then
        assertThat(verification).isNotNull();
        assertThat(verification.getId()).isNotNull();
        assertThat(verification.getVerificationNumber()).isNotNull();
        assertThat(verification.getVerificationNumber()).startsWith("ENV-");
        assertThat(verification.getStatus()).isEqualTo(VerificationStatus.DRAFT);
        assertThat(verification.getPurpose()).isEqualTo(purpose);
        assertThat(verification.getStudent()).isNotNull();
        assertThat(verification.getUrgentRequest()).isFalse();
        assertThat(verification.getFeePaid()).isFalse();
        assertThat(verification.getOfficialSeal()).isTrue();
        assertThat(verification.getValidityDays()).isEqualTo(90);
    }

    @Test
    @DisplayName("Should retrieve verification by ID")
    void testGetVerificationById() {
        // Given
        EnrollmentVerification created = verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.STUDENT_LOAN, testStaff.getId());

        // When
        EnrollmentVerification retrieved = verificationService.getVerificationById(created.getId());

        // Then
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getId()).isEqualTo(created.getId());
        assertThat(retrieved.getVerificationNumber()).isEqualTo(created.getVerificationNumber());
    }

    @Test
    @DisplayName("Should retrieve verification by verification number")
    void testGetByVerificationNumber() {
        // Given
        EnrollmentVerification created = verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.INSURANCE, testStaff.getId());

        // When
        EnrollmentVerification retrieved = verificationService.getByVerificationNumber(
                created.getVerificationNumber());

        // Then
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getId()).isEqualTo(created.getId());
    }

    @Test
    @DisplayName("Should retrieve verifications by status")
    void testGetByStatus() {
        // Given
        verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.SCHOLARSHIP, testStaff.getId());
        verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.STUDENT_LOAN, testStaff.getId());

        // When
        List<EnrollmentVerification> drafts = verificationService.getByStatus(VerificationStatus.DRAFT);

        // Then
        assertThat(drafts).isNotEmpty();
        assertThat(drafts).hasSizeGreaterThanOrEqualTo(2);
        assertThat(drafts).allMatch(v -> v.getStatus() == VerificationStatus.DRAFT);
    }

    @Test
    @DisplayName("Should retrieve verifications by purpose")
    void testGetByPurpose() {
        // Given
        verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.SCHOLARSHIP, testStaff.getId());
        verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.SCHOLARSHIP, testStaff.getId());
        verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.STUDENT_LOAN, testStaff.getId());

        // When
        List<EnrollmentVerification> scholarships = verificationService.getByPurpose(
                VerificationPurpose.SCHOLARSHIP);

        // Then
        assertThat(scholarships).hasSizeGreaterThanOrEqualTo(2);
        assertThat(scholarships).allMatch(v -> v.getPurpose() == VerificationPurpose.SCHOLARSHIP);
    }

    @Test
    @DisplayName("Should retrieve verifications by student")
    void testGetByStudent() {
        // Given
        verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.SCHOLARSHIP, testStaff.getId());
        verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.STUDENT_LOAN, testStaff.getId());

        // When
        List<EnrollmentVerification> studentVerifications = verificationService.getByStudent(
                testStudent.getId());

        // Then
        assertThat(studentVerifications).hasSizeGreaterThanOrEqualTo(2);
        assertThat(studentVerifications).allMatch(v ->
                v.getStudent().getId().equals(testStudent.getId()));
    }

    @Test
    @DisplayName("Should update verification")
    void testUpdateVerification() {
        // Given
        EnrollmentVerification verification = verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.EMPLOYMENT, testStaff.getId());

        // When - Update to urgent request
        verification.setUrgentRequest(true);
        EnrollmentVerification updated = verificationService.updateVerification(
                verification, testStaff.getId());

        // Then
        assertThat(updated.getUrgentRequest()).isTrue();
        assertThat(updated.getUpdatedBy()).isNotNull();
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should verify enrollment with full details")
    void testVerifyEnrollmentWithDetails() {
        // Given
        EnrollmentVerification verification = verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.SCHOLARSHIP, testStaff.getId());

        // When
        EnrollmentVerification verified = verificationService.verifyEnrollment(
                verification.getId(), true, true, 3.75, testStaff.getId());

        // Then
        assertThat(verified.getStatus()).isEqualTo(VerificationStatus.VERIFIED);
        assertThat(verified.getFullTimeEnrollment()).isTrue();
        assertThat(verified.getAcademicGoodStanding()).isTrue();
        assertThat(verified.getIncludeGPA()).isTrue();
        assertThat(verified.getCurrentGPA()).isEqualTo(3.75);
        assertThat(verified.getVerifiedBy()).isNotNull();
        assertThat(verified.getVerifiedDate()).isNotNull();
    }

    @Test
    @DisplayName("Should verify enrollment without GPA")
    void testVerifyEnrollmentWithoutGPA() {
        // Given
        EnrollmentVerification verification = verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.INSURANCE, testStaff.getId());

        // When
        EnrollmentVerification verified = verificationService.verifyEnrollment(
                verification.getId(), true, true, null, testStaff.getId());

        // Then
        assertThat(verified.getStatus()).isEqualTo(VerificationStatus.VERIFIED);
        assertThat(verified.getIncludeGPA()).isNull();
        assertThat(verified.getCurrentGPA()).isNull();
    }

    @Test
    @DisplayName("Should generate verification document")
    void testGenerateDocument() {
        // Given
        EnrollmentVerification verification = verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.STUDENT_LOAN, testStaff.getId());

        // Verify enrollment first
        verificationService.verifyEnrollment(
                verification.getId(), true, true, 3.5, testStaff.getId());

        // When
        String documentPath = "/documents/verifications/ENV-2025-001.pdf";
        EnrollmentVerification generated = verificationService.generateDocument(
                verification.getId(), documentPath, testStaff.getId());

        // Then
        assertThat(generated.getStatus()).isEqualTo(VerificationStatus.GENERATED);
        assertThat(generated.getDocumentFilePath()).isEqualTo(documentPath);
        assertThat(generated.getGeneratedBy()).isNotNull();
        assertThat(generated.getGeneratedDate()).isNotNull();
        assertThat(generated.getValidFrom()).isNotNull();
    }

    @Test
    @DisplayName("Should prevent document generation when not ready")
    void testPreventPrematureGeneration() {
        // Given - Create verification but don't verify it
        EnrollmentVerification verification = verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.SCHOLARSHIP, testStaff.getId());

        // When/Then
        assertThatThrownBy(() -> verificationService.generateDocument(
                verification.getId(), "/path/to/doc.pdf", testStaff.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not ready for document generation");
    }

    @Test
    @DisplayName("Should record fee payment")
    void testRecordFeePayment() {
        // Given
        EnrollmentVerification verification = verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.STUDENT_LOAN, testStaff.getId());

        // When
        EnrollmentVerification updated = verificationService.recordFeePayment(
                verification.getId(), testStaff.getId());

        // Then
        assertThat(updated.getFeePaid()).isTrue();
    }

    @Test
    @DisplayName("Should mark verification as delivered with tracking")
    void testMarkAsDelivered() {
        // Given
        EnrollmentVerification verification = verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.EMPLOYMENT, testStaff.getId());

        verificationService.verifyEnrollment(
                verification.getId(), true, true, null, testStaff.getId());
        verificationService.generateDocument(
                verification.getId(), "/docs/verification.pdf", testStaff.getId());

        // When
        String trackingNumber = "1Z999AA10123456784";
        EnrollmentVerification delivered = verificationService.markAsDelivered(
                verification.getId(), trackingNumber, testStaff.getId());

        // Then
        assertThat(delivered.getStatus()).isEqualTo(VerificationStatus.DELIVERED);
        assertThat(delivered.getTrackingNumber()).isEqualTo(trackingNumber);
        assertThat(delivered.getDeliveryDate()).isNotNull();
    }

    @Test
    @DisplayName("Should send verification")
    void testSendVerification() {
        // Given
        EnrollmentVerification verification = verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.VISA_IMMIGRATION, testStaff.getId());

        verificationService.verifyEnrollment(
                verification.getId(), true, true, null, testStaff.getId());
        verificationService.generateDocument(
                verification.getId(), "/docs/visa-verification.pdf", testStaff.getId());

        // When
        EnrollmentVerification sent = verificationService.sendVerification(
                verification.getId(), "EMAIL", "EMAIL-CONF-12345", testStaff.getId());

        // Then
        assertThat(sent.getStatus()).isEqualTo(VerificationStatus.DELIVERED);
        assertThat(sent.getTrackingNumber()).isEqualTo("EMAIL-CONF-12345");
    }

    @Test
    @DisplayName("Should cancel verification with reason")
    void testCancelVerification() {
        // Given
        EnrollmentVerification verification = verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.SCHOLARSHIP, testStaff.getId());

        // When
        String reason = "Student withdrew application";
        EnrollmentVerification cancelled = verificationService.cancelVerification(
                verification.getId(), reason, testStaff.getId());

        // Then
        assertThat(cancelled.getStatus()).isEqualTo(VerificationStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should delete draft verification")
    void testDeleteDraftVerification() {
        // Given
        EnrollmentVerification verification = verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.OTHER, testStaff.getId());
        Long verificationId = verification.getId();

        // When
        verificationService.deleteVerification(verificationId);

        // Then
        assertThatThrownBy(() -> verificationService.getVerificationById(verificationId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Should prevent deletion of non-draft verification")
    void testPreventDeletionOfNonDraft() {
        // Given
        EnrollmentVerification verification = verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.STUDENT_LOAN, testStaff.getId());

        verificationService.verifyEnrollment(
                verification.getId(), true, true, 3.0, testStaff.getId());

        // When/Then
        assertThatThrownBy(() -> verificationService.deleteVerification(verification.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only draft verifications can be deleted");
    }

    @Test
    @DisplayName("Should retrieve urgent verifications")
    void testGetUrgentVerifications() {
        // Given
        EnrollmentVerification urgent1 = verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.SCHOLARSHIP, testStaff.getId());
        urgent1.setUrgentRequest(true);
        verificationService.updateVerification(urgent1, testStaff.getId());

        EnrollmentVerification urgent2 = verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.STUDENT_LOAN, testStaff.getId());
        urgent2.setUrgentRequest(true);
        verificationService.updateVerification(urgent2, testStaff.getId());

        verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.INSURANCE, testStaff.getId());

        // When
        List<EnrollmentVerification> urgentList = verificationService.getUrgentVerifications();

        // Then
        assertThat(urgentList).hasSizeGreaterThanOrEqualTo(2);
        assertThat(urgentList).allMatch(EnrollmentVerification::getUrgentRequest);
    }

    @Test
    @DisplayName("Should retrieve unpaid fee verifications")
    void testGetUnpaidFees() {
        // Given
        verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.SCHOLARSHIP, testStaff.getId());
        verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.STUDENT_LOAN, testStaff.getId());

        EnrollmentVerification paid = verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.EMPLOYMENT, testStaff.getId());
        verificationService.recordFeePayment(paid.getId(), testStaff.getId());

        // When
        List<EnrollmentVerification> unpaid = verificationService.getUnpaidFees();

        // Then
        assertThat(unpaid).hasSizeGreaterThanOrEqualTo(2);
        assertThat(unpaid).allMatch(v -> !v.getFeePaid());
    }

    @Test
    @DisplayName("Should retrieve pending verifications")
    void testGetPendingVerification() {
        // Given
        EnrollmentVerification pending = verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.VISA_IMMIGRATION, testStaff.getId());
        pending.setStatus(VerificationStatus.PENDING_VERIFICATION);
        verificationService.updateVerification(pending, testStaff.getId());

        // When
        List<EnrollmentVerification> pendingList = verificationService.getPendingVerification();

        // Then
        assertThat(pendingList).isNotEmpty();
        assertThat(pendingList).anyMatch(v -> v.getId().equals(pending.getId()));
    }

    @Test
    @DisplayName("Should get verification statistics")
    void testGetStatistics() {
        // Given - Create verifications with different statuses
        verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.SCHOLARSHIP, testStaff.getId());
        verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.STUDENT_LOAN, testStaff.getId());

        EnrollmentVerification urgent = verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.VISA_IMMIGRATION, testStaff.getId());
        urgent.setUrgentRequest(true);
        verificationService.updateVerification(urgent, testStaff.getId());

        // When
        EnrollmentVerificationService.VerificationStatistics stats =
                verificationService.getStatistics();

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.total()).isGreaterThanOrEqualTo(3);
        assertThat(stats.draft()).isGreaterThanOrEqualTo(3);
        assertThat(stats.urgent()).isGreaterThanOrEqualTo(1);
        assertThat(stats.unpaidFees()).isGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Should get count by purpose")
    void testGetCountByPurpose() {
        // Given
        verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.SCHOLARSHIP, testStaff.getId());
        verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.SCHOLARSHIP, testStaff.getId());
        verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.STUDENT_LOAN, testStaff.getId());

        // When
        List<Object[]> counts = verificationService.getCountByPurpose();

        // Then
        assertThat(counts).isNotEmpty();
    }

    @Test
    @DisplayName("Should complete full verification workflow")
    void testCompleteWorkflow() {
        // Given - Create new verification
        EnrollmentVerification verification = verificationService.createVerification(
                testStudent.getId(), VerificationPurpose.SCHOLARSHIP, testStaff.getId());
        assertThat(verification.getStatus()).isEqualTo(VerificationStatus.DRAFT);

        // When - Go through complete workflow
        // Step 1: Record fee payment
        verification = verificationService.recordFeePayment(verification.getId(), testStaff.getId());
        assertThat(verification.getFeePaid()).isTrue();

        // Step 2: Verify enrollment
        verification = verificationService.verifyEnrollment(
                verification.getId(), true, true, 3.8, testStaff.getId());
        assertThat(verification.getStatus()).isEqualTo(VerificationStatus.VERIFIED);

        // Step 3: Generate document
        verification = verificationService.generateDocument(
                verification.getId(), "/docs/scholarship-verification.pdf", testStaff.getId());
        assertThat(verification.getStatus()).isEqualTo(VerificationStatus.GENERATED);

        // Step 4: Send verification
        verification = verificationService.sendVerification(
                verification.getId(), "MAIL", "USPS-12345", testStaff.getId());
        assertThat(verification.getStatus()).isEqualTo(VerificationStatus.DELIVERED);

        // Then - Verify final state
        assertThat(verification.getFeePaid()).isTrue();
        assertThat(verification.getFullTimeEnrollment()).isTrue();
        assertThat(verification.getAcademicGoodStanding()).isTrue();
        assertThat(verification.getCurrentGPA()).isEqualTo(3.8);
        assertThat(verification.getDocumentFilePath()).isNotNull();
        assertThat(verification.getTrackingNumber()).isEqualTo("USPS-12345");
    }

    @Test
    @DisplayName("Should throw exception when verification not found")
    void testVerificationNotFound() {
        // When/Then
        assertThatThrownBy(() -> verificationService.getVerificationById(99999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }
}
