package com.heronix.service;

import com.heronix.model.domain.TransferOutDocumentation;
import com.heronix.model.domain.TransferOutDocumentation.TransferOutStatus;
import com.heronix.model.domain.TransferOutDocumentation.TransmissionMethod;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.User;
import com.heronix.repository.TransferOutDocumentationRepository;
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
 * Integration tests for TransferOutDocumentationService
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@SpringBootTest(classes = com.heronix.config.TestConfiguration.class)
@ActiveProfiles("test")
@Transactional
class TransferOutDocumentationServiceTest {

    @Autowired
    private TransferOutDocumentationService transferOutService;

    @Autowired
    private TransferOutDocumentationRepository transferOutRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    private Student testStudent;
    private User testStaff;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        transferOutRepository.deleteAll();

        // Create test student if doesn't exist
        testStudent = studentRepository.findAll().stream().findFirst()
                .orElseGet(() -> {
                    Student student = new Student();
                    student.setFirstName("Test");
                    student.setLastName("Student");
                    student.setStudentId("TRO001");
                    student.setGradeLevel("10"); // Required field
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
    @DisplayName("Should create transfer out documentation successfully")
    void testCreateTransferOut() {
        // Given
        String destinationSchool = "Mountain View High School";
        LocalDate expectedDate = LocalDate.now().plusMonths(1);

        // When
        TransferOutDocumentation transferOut = transferOutService.createTransferOut(
                testStudent.getId(), destinationSchool, expectedDate, testStaff.getId());

        // Then
        assertThat(transferOut).isNotNull();
        assertThat(transferOut.getId()).isNotNull();
        assertThat(transferOut.getTransferOutNumber()).isNotNull();
        assertThat(transferOut.getTransferOutNumber()).startsWith("TRO-");
        assertThat(transferOut.getStatus()).isEqualTo(TransferOutStatus.DRAFT);
        assertThat(transferOut.getStudent()).isNotNull();
        assertThat(transferOut.getDestinationSchoolName()).isEqualTo(destinationSchool);
        assertThat(transferOut.getExpectedTransferDate()).isEqualTo(expectedDate);
        assertThat(transferOut.getTotalDocumentsIncluded()).isEqualTo(12);
        assertThat(transferOut.getDocumentsPackaged()).isEqualTo(0);
        assertThat(transferOut.getAllDocumentsPackaged()).isFalse();
        assertThat(transferOut.getRequiresParentConsent()).isTrue();
        assertThat(transferOut.getParentConsentObtained()).isFalse();
    }

    @Test
    @DisplayName("Should prevent duplicate pending transfer outs for same student")
    void testPreventDuplicatePendingTransferOuts() {
        // Given - Create first transfer out
        transferOutService.createTransferOut(
                testStudent.getId(), "School A", LocalDate.now().plusDays(30), testStaff.getId());

        // When/Then - Try to create second transfer out for same student
        assertThatThrownBy(() -> transferOutService.createTransferOut(
                testStudent.getId(), "School B", LocalDate.now().plusDays(20), testStaff.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already has a pending transfer out");
    }

    @Test
    @DisplayName("Should retrieve transfer out by ID")
    void testGetTransferOutById() {
        // Given
        TransferOutDocumentation created = transferOutService.createTransferOut(
                testStudent.getId(), "Riverside Academy", LocalDate.now().plusDays(45), testStaff.getId());

        // When
        TransferOutDocumentation retrieved = transferOutService.getTransferOutById(created.getId());

        // Then
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getId()).isEqualTo(created.getId());
        assertThat(retrieved.getTransferOutNumber()).isEqualTo(created.getTransferOutNumber());
    }

    @Test
    @DisplayName("Should retrieve transfer out by transfer out number")
    void testGetByTransferOutNumber() {
        // Given
        TransferOutDocumentation created = transferOutService.createTransferOut(
                testStudent.getId(), "Central High", LocalDate.now().plusDays(60), testStaff.getId());

        // When
        TransferOutDocumentation retrieved = transferOutService.getByTransferOutNumber(
                created.getTransferOutNumber());

        // Then
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getId()).isEqualTo(created.getId());
    }

    @Test
    @DisplayName("Should retrieve transfer outs by status")
    void testGetByStatus() {
        // Given
        transferOutService.createTransferOut(
                testStudent.getId(), "School A", LocalDate.now().plusDays(30), testStaff.getId());

        // Create another student to avoid duplicate error
        Student student2 = new Student();
        student2.setFirstName("Another");
        student2.setLastName("Student");
        student2.setStudentId("TRO002");
        student2 = studentRepository.save(student2);

        transferOutService.createTransferOut(
                student2.getId(), "School B", LocalDate.now().plusDays(40), testStaff.getId());

        // When
        List<TransferOutDocumentation> drafts = transferOutService.getByStatus(TransferOutStatus.DRAFT);

        // Then
        assertThat(drafts).isNotEmpty();
        assertThat(drafts).hasSizeGreaterThanOrEqualTo(2);
        assertThat(drafts).allMatch(t -> t.getStatus() == TransferOutStatus.DRAFT);
    }

    @Test
    @DisplayName("Should search by destination school")
    void testSearchByDestinationSchool() {
        // Given
        transferOutService.createTransferOut(
                testStudent.getId(), "Mountain View High School", LocalDate.now().plusDays(30), testStaff.getId());

        // When
        List<TransferOutDocumentation> results = transferOutService.searchByDestinationSchool("Mountain");

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).anyMatch(t -> t.getDestinationSchoolName().contains("Mountain"));
    }

    @Test
    @DisplayName("Should update transfer out record")
    void testUpdateTransferOut() {
        // Given
        TransferOutDocumentation transferOut = transferOutService.createTransferOut(
                testStudent.getId(), "Original School", LocalDate.now().plusDays(30), testStaff.getId());

        // When
        transferOut.setDestinationSchoolName("Updated School Name");
        TransferOutDocumentation updated = transferOutService.updateTransferOut(transferOut, testStaff.getId());

        // Then
        assertThat(updated.getDestinationSchoolName()).isEqualTo("Updated School Name");
        assertThat(updated.getUpdatedBy()).isNotNull();
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should start records preparation")
    void testStartRecordsPreparation() {
        // Given
        TransferOutDocumentation transferOut = transferOutService.createTransferOut(
                testStudent.getId(), "Destination High", LocalDate.now().plusDays(30), testStaff.getId());

        // When
        TransferOutDocumentation updated = transferOutService.startRecordsPreparation(
                transferOut.getId(), testStaff.getId());

        // Then
        assertThat(updated.getStatus()).isEqualTo(TransferOutStatus.RECORDS_PREPARATION);
        assertThat(updated.getProcessingStartDate()).isNotNull();
    }

    @Test
    @DisplayName("Should mark individual document as included")
    void testMarkDocumentIncluded() {
        // Given
        TransferOutDocumentation transferOut = transferOutService.createTransferOut(
                testStudent.getId(), "Test School", LocalDate.now().plusDays(30), testStaff.getId());
        transferOutService.startRecordsPreparation(transferOut.getId(), testStaff.getId());

        // When
        TransferOutDocumentation updated = transferOutService.markDocumentIncluded(
                transferOut.getId(), "transcript", testStaff.getId());

        // Then
        assertThat(updated.getTranscriptIncluded()).isTrue();
        assertThat(updated.getTranscriptGeneratedDate()).isNotNull();
        assertThat(updated.getDocumentsPackaged()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should mark multiple documents as included")
    void testMarkMultipleDocuments() {
        // Given
        TransferOutDocumentation transferOut = transferOutService.createTransferOut(
                testStudent.getId(), "Test School", LocalDate.now().plusDays(30), testStaff.getId());
        transferOutService.startRecordsPreparation(transferOut.getId(), testStaff.getId());

        // When
        transferOutService.markDocumentIncluded(transferOut.getId(), "transcript", testStaff.getId());
        transferOutService.markDocumentIncluded(transferOut.getId(), "immunization", testStaff.getId());
        transferOutService.markDocumentIncluded(transferOut.getId(), "iep", testStaff.getId());
        TransferOutDocumentation updated = transferOutService.markDocumentIncluded(
                transferOut.getId(), "attendance", testStaff.getId());

        // Then
        assertThat(updated.getDocumentsPackaged()).isEqualTo(4);
        assertThat(updated.getTranscriptIncluded()).isTrue();
        assertThat(updated.getImmunizationRecordsIncluded()).isTrue();
        assertThat(updated.getIepIncluded()).isTrue();
        assertThat(updated.getAttendanceRecordsIncluded()).isTrue();
    }

    @Test
    @DisplayName("Should include all documents at once")
    void testIncludeAllDocuments() {
        // Given
        TransferOutDocumentation transferOut = transferOutService.createTransferOut(
                testStudent.getId(), "Test School", LocalDate.now().plusDays(30), testStaff.getId());
        transferOutService.startRecordsPreparation(transferOut.getId(), testStaff.getId());

        // When
        TransferOutDocumentation updated = transferOutService.includeAllDocuments(
                transferOut.getId(), testStaff.getId());

        // Then
        assertThat(updated.getDocumentsPackaged()).isEqualTo(12);
        assertThat(updated.getAllDocumentsPackaged()).isTrue();
        assertThat(updated.getTranscriptIncluded()).isTrue();
        assertThat(updated.getImmunizationRecordsIncluded()).isTrue();
        assertThat(updated.getIepIncluded()).isTrue();
        assertThat(updated.getCumulativeFolderIncluded()).isTrue();
    }

    @Test
    @DisplayName("Should transition to PENDING_CONSENT when all documents packaged and consent required")
    void testTransitionToPendingConsent() {
        // Given
        TransferOutDocumentation transferOut = transferOutService.createTransferOut(
                testStudent.getId(), "Test School", LocalDate.now().plusDays(30), testStaff.getId());
        transferOutService.startRecordsPreparation(transferOut.getId(), testStaff.getId());

        // When - Include all documents
        TransferOutDocumentation updated = transferOutService.includeAllDocuments(
                transferOut.getId(), testStaff.getId());

        // Then - Should transition to PENDING_CONSENT since consent is required
        assertThat(updated.getAllDocumentsPackaged()).isTrue();
        assertThat(updated.getStatus()).isEqualTo(TransferOutStatus.PENDING_CONSENT);
    }

    @Test
    @DisplayName("Should assign staff to transfer out")
    void testAssignStaff() {
        // Given
        TransferOutDocumentation transferOut = transferOutService.createTransferOut(
                testStudent.getId(), "Test School", LocalDate.now().plusDays(30), testStaff.getId());

        // When
        TransferOutDocumentation updated = transferOutService.assignStaff(
                transferOut.getId(), testStaff.getId(), testStaff.getId());

        // Then
        assertThat(updated.getAssignedStaff()).isNotNull();
        assertThat(updated.getAssignedStaff().getId()).isEqualTo(testStaff.getId());
    }

    @Test
    @DisplayName("Should record parent consent")
    void testRecordParentConsent() {
        // Given
        TransferOutDocumentation transferOut = transferOutService.createTransferOut(
                testStudent.getId(), "Test School", LocalDate.now().plusDays(30), testStaff.getId());
        transferOutService.startRecordsPreparation(transferOut.getId(), testStaff.getId());
        transferOutService.includeAllDocuments(transferOut.getId(), testStaff.getId());

        // When
        TransferOutDocumentation updated = transferOutService.recordParentConsent(
                transferOut.getId(), testStaff.getId());

        // Then
        assertThat(updated.getParentConsentObtained()).isTrue();
        assertThat(updated.getParentConsentDate()).isNotNull();
        assertThat(updated.getFerpaReleaseObtained()).isTrue();
        assertThat(updated.getFerpaReleaseDate()).isNotNull();
        assertThat(updated.getStatus()).isEqualTo(TransferOutStatus.READY_TO_SEND);
    }

    @Test
    @DisplayName("Should record fee payment")
    void testRecordFeePayment() {
        // Given
        TransferOutDocumentation transferOut = transferOutService.createTransferOut(
                testStudent.getId(), "Test School", LocalDate.now().plusDays(30), testStaff.getId());
        transferOut.setHasOutstandingFees(true);
        transferOut.setOutstandingFeesAmount(50.00);
        transferOutService.updateTransferOut(transferOut, testStaff.getId());

        // When
        TransferOutDocumentation updated = transferOutService.recordFeePayment(
                transferOut.getId(), testStaff.getId());

        // Then
        assertThat(updated.getFeesPaid()).isTrue();
        assertThat(updated.getFeesPaidDate()).isNotNull();
        assertThat(updated.getFeesPaidBeforeRelease()).isTrue();
    }

    @Test
    @DisplayName("Should send records via email")
    void testSendRecordsViaEmail() {
        // Given
        TransferOutDocumentation transferOut = transferOutService.createTransferOut(
                testStudent.getId(), "Test School", LocalDate.now().plusDays(30), testStaff.getId());
        transferOut.setRequiresParentConsent(false);
        transferOutService.updateTransferOut(transferOut, testStaff.getId());
        transferOutService.startRecordsPreparation(transferOut.getId(), testStaff.getId());
        transferOutService.includeAllDocuments(transferOut.getId(), testStaff.getId());

        // When
        TransferOutDocumentation updated = transferOutService.sendRecords(
                transferOut.getId(), TransmissionMethod.EMAIL, "EMAIL-CONF-12345", testStaff.getId());

        // Then
        assertThat(updated.getStatus()).isEqualTo(TransferOutStatus.SENT);
        assertThat(updated.getTransmissionMethod()).isEqualTo(TransmissionMethod.EMAIL);
        assertThat(updated.getTrackingNumber()).isEqualTo("EMAIL-CONF-12345");
        assertThat(updated.getSentDate()).isNotNull();
        assertThat(updated.getSentBy()).isNotNull();
        assertThat(updated.getFollowUpRequired()).isTrue();
        assertThat(updated.getFollowUpDate()).isEqualTo(LocalDate.now().plusDays(7));
    }

    @Test
    @DisplayName("Should send records via certified mail")
    void testSendRecordsViaCertifiedMail() {
        // Given
        TransferOutDocumentation transferOut = transferOutService.createTransferOut(
                testStudent.getId(), "Test School", LocalDate.now().plusDays(30), testStaff.getId());
        transferOut.setRequiresParentConsent(false);
        transferOutService.updateTransferOut(transferOut, testStaff.getId());
        transferOutService.startRecordsPreparation(transferOut.getId(), testStaff.getId());
        transferOutService.includeAllDocuments(transferOut.getId(), testStaff.getId());

        // When
        TransferOutDocumentation updated = transferOutService.sendRecords(
                transferOut.getId(), TransmissionMethod.CERTIFIED_MAIL,
                "USPS-1234567890", testStaff.getId());

        // Then
        assertThat(updated.getStatus()).isEqualTo(TransferOutStatus.SENT);
        assertThat(updated.getTransmissionMethod()).isEqualTo(TransmissionMethod.CERTIFIED_MAIL);
        assertThat(updated.getFollowUpDate()).isEqualTo(LocalDate.now().plusDays(14));
    }

    @Test
    @DisplayName("Should prevent sending when not ready")
    void testPreventSendingWhenNotReady() {
        // Given - Transfer out without all documents
        TransferOutDocumentation transferOut = transferOutService.createTransferOut(
                testStudent.getId(), "Test School", LocalDate.now().plusDays(30), testStaff.getId());

        // When/Then
        assertThatThrownBy(() -> transferOutService.sendRecords(
                transferOut.getId(), TransmissionMethod.EMAIL, "TRACKING", testStaff.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not ready to send");
    }

    @Test
    @DisplayName("Should record acknowledgment from destination school")
    void testRecordAcknowledgment() {
        // Given
        TransferOutDocumentation transferOut = transferOutService.createTransferOut(
                testStudent.getId(), "Test School", LocalDate.now().plusDays(30), testStaff.getId());
        transferOut.setRequiresParentConsent(false);
        transferOutService.updateTransferOut(transferOut, testStaff.getId());
        transferOutService.startRecordsPreparation(transferOut.getId(), testStaff.getId());
        transferOutService.includeAllDocuments(transferOut.getId(), testStaff.getId());
        transferOutService.sendRecords(transferOut.getId(), TransmissionMethod.EMAIL, "TRACKING", testStaff.getId());

        // When
        TransferOutDocumentation updated = transferOutService.recordAcknowledgment(
                transferOut.getId(), "Jane Smith, Registrar", "Email", testStaff.getId());

        // Then
        assertThat(updated.getDestinationAcknowledged()).isTrue();
        assertThat(updated.getAcknowledgmentDate()).isNotNull();
        assertThat(updated.getAcknowledgedBy()).isEqualTo("Jane Smith, Registrar");
        assertThat(updated.getAcknowledgmentMethod()).isEqualTo("Email");
        assertThat(updated.getStatus()).isEqualTo(TransferOutStatus.ACKNOWLEDGED);
        assertThat(updated.getFollowUpRequired()).isFalse();
    }

    @Test
    @DisplayName("Should prevent acknowledgment if not sent")
    void testPreventAcknowledgmentIfNotSent() {
        // Given - Transfer out that hasn't been sent
        TransferOutDocumentation transferOut = transferOutService.createTransferOut(
                testStudent.getId(), "Test School", LocalDate.now().plusDays(30), testStaff.getId());

        // When/Then
        assertThatThrownBy(() -> transferOutService.recordAcknowledgment(
                transferOut.getId(), "Someone", "Email", testStaff.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only acknowledge records that have been sent");
    }

    @Test
    @DisplayName("Should complete transfer out process")
    void testCompleteTransferOut() {
        // Given - Complete workflow up to acknowledged
        TransferOutDocumentation transferOut = transferOutService.createTransferOut(
                testStudent.getId(), "Test School", LocalDate.now().plusDays(30), testStaff.getId());
        transferOut.setRequiresParentConsent(false);
        transferOutService.updateTransferOut(transferOut, testStaff.getId());
        transferOutService.startRecordsPreparation(transferOut.getId(), testStaff.getId());
        transferOutService.includeAllDocuments(transferOut.getId(), testStaff.getId());
        transferOutService.sendRecords(transferOut.getId(), TransmissionMethod.EMAIL, "TRACKING", testStaff.getId());
        transferOutService.recordAcknowledgment(transferOut.getId(), "Registrar", "Email", testStaff.getId());

        // When
        TransferOutDocumentation completed = transferOutService.completeTransferOut(
                transferOut.getId(), testStaff.getId());

        // Then
        assertThat(completed.getStatus()).isEqualTo(TransferOutStatus.COMPLETED);
        assertThat(completed.getProcessingCompletedDate()).isNotNull();
    }

    @Test
    @DisplayName("Should prevent completion if not acknowledged")
    void testPreventCompletionIfNotAcknowledged() {
        // Given - Transfer out that hasn't been acknowledged
        TransferOutDocumentation transferOut = transferOutService.createTransferOut(
                testStudent.getId(), "Test School", LocalDate.now().plusDays(30), testStaff.getId());

        // When/Then
        assertThatThrownBy(() -> transferOutService.completeTransferOut(
                transferOut.getId(), testStaff.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be acknowledged before completion");
    }

    @Test
    @DisplayName("Should cancel transfer out with reason")
    void testCancelTransferOut() {
        // Given
        TransferOutDocumentation transferOut = transferOutService.createTransferOut(
                testStudent.getId(), "Test School", LocalDate.now().plusDays(30), testStaff.getId());

        // When
        String reason = "Student decided not to transfer";
        TransferOutDocumentation cancelled = transferOutService.cancelTransferOut(
                transferOut.getId(), reason, testStaff.getId());

        // Then
        assertThat(cancelled.getStatus()).isEqualTo(TransferOutStatus.CANCELLED);
        assertThat(cancelled.getAdministrativeNotes()).contains("CANCELLED");
        assertThat(cancelled.getAdministrativeNotes()).contains(reason);
    }

    @Test
    @DisplayName("Should prevent cancellation of completed transfer out")
    void testPreventCancellationOfCompleted() {
        // Given - Completed transfer out
        TransferOutDocumentation transferOut = transferOutService.createTransferOut(
                testStudent.getId(), "Test School", LocalDate.now().plusDays(30), testStaff.getId());
        transferOut.setRequiresParentConsent(false);
        transferOutService.updateTransferOut(transferOut, testStaff.getId());
        transferOutService.startRecordsPreparation(transferOut.getId(), testStaff.getId());
        transferOutService.includeAllDocuments(transferOut.getId(), testStaff.getId());
        transferOutService.sendRecords(transferOut.getId(), TransmissionMethod.EMAIL, "TRACKING", testStaff.getId());
        transferOutService.recordAcknowledgment(transferOut.getId(), "Registrar", "Email", testStaff.getId());
        transferOutService.completeTransferOut(transferOut.getId(), testStaff.getId());

        // When/Then
        assertThatThrownBy(() -> transferOutService.cancelTransferOut(
                transferOut.getId(), "Testing", testStaff.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cancel completed");
    }

    @Test
    @DisplayName("Should add follow-up note")
    void testAddFollowUpNote() {
        // Given
        TransferOutDocumentation transferOut = transferOutService.createTransferOut(
                testStudent.getId(), "Test School", LocalDate.now().plusDays(30), testStaff.getId());
        transferOut.setRequiresParentConsent(false);
        transferOutService.updateTransferOut(transferOut, testStaff.getId());
        transferOutService.startRecordsPreparation(transferOut.getId(), testStaff.getId());
        transferOutService.includeAllDocuments(transferOut.getId(), testStaff.getId());
        transferOutService.sendRecords(transferOut.getId(), TransmissionMethod.EMAIL, "TRACKING", testStaff.getId());

        // When
        String note = "Called destination school, left message";
        TransferOutDocumentation updated = transferOutService.addFollowUpNote(
                transferOut.getId(), note, testStaff.getId());

        // Then
        assertThat(updated.getFollowUpAttempts()).isEqualTo(1);
        assertThat(updated.getLastFollowUpDate()).isNotNull();
        assertThat(updated.getFollowUpNotes()).contains(note);
    }

    @Test
    @DisplayName("Should delete draft transfer out")
    void testDeleteDraftTransferOut() {
        // Given
        TransferOutDocumentation transferOut = transferOutService.createTransferOut(
                testStudent.getId(), "Test School", LocalDate.now().plusDays(30), testStaff.getId());
        Long transferOutId = transferOut.getId();

        // When
        transferOutService.deleteTransferOut(transferOutId);

        // Then
        assertThatThrownBy(() -> transferOutService.getTransferOutById(transferOutId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Should prevent deletion of non-draft transfer out")
    void testPreventDeletionOfNonDraft() {
        // Given
        TransferOutDocumentation transferOut = transferOutService.createTransferOut(
                testStudent.getId(), "Test School", LocalDate.now().plusDays(30), testStaff.getId());
        transferOutService.startRecordsPreparation(transferOut.getId(), testStaff.getId());

        // When/Then
        assertThatThrownBy(() -> transferOutService.deleteTransferOut(transferOut.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only draft transfer out records can be deleted");
    }

    @Test
    @DisplayName("Should get transfer out statistics")
    void testGetStatistics() {
        // Given - Create transfer outs with different statuses
        transferOutService.createTransferOut(
                testStudent.getId(), "School A", LocalDate.now().plusDays(30), testStaff.getId());

        Student student2 = new Student();
        student2.setFirstName("Another");
        student2.setLastName("Student");
        student2.setStudentId("TRO002");
        student2 = studentRepository.save(student2);

        transferOutService.createTransferOut(
                student2.getId(), "School B", LocalDate.now().plusDays(40), testStaff.getId());

        // When
        TransferOutDocumentationService.TransferOutStatistics stats =
                transferOutService.getStatistics();

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.total()).isGreaterThanOrEqualTo(2);
        assertThat(stats.draft()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Should complete full transfer out workflow")
    void testCompleteWorkflow() {
        // Given - Create new transfer out
        TransferOutDocumentation transferOut = transferOutService.createTransferOut(
                testStudent.getId(), "Destination Academy", LocalDate.now().plusDays(30), testStaff.getId());
        assertThat(transferOut.getStatus()).isEqualTo(TransferOutStatus.DRAFT);

        // When - Go through complete workflow
        // Step 1: Assign staff
        transferOut = transferOutService.assignStaff(
                transferOut.getId(), testStaff.getId(), testStaff.getId());
        assertThat(transferOut.getAssignedStaff()).isNotNull();

        // Step 2: Start records preparation
        transferOut = transferOutService.startRecordsPreparation(
                transferOut.getId(), testStaff.getId());
        assertThat(transferOut.getStatus()).isEqualTo(TransferOutStatus.RECORDS_PREPARATION);

        // Step 3: Include all documents
        transferOut = transferOutService.includeAllDocuments(
                transferOut.getId(), testStaff.getId());
        assertThat(transferOut.getAllDocumentsPackaged()).isTrue();
        assertThat(transferOut.getStatus()).isEqualTo(TransferOutStatus.PENDING_CONSENT);

        // Step 4: Record parent consent
        transferOut = transferOutService.recordParentConsent(
                transferOut.getId(), testStaff.getId());
        assertThat(transferOut.getParentConsentObtained()).isTrue();
        assertThat(transferOut.getStatus()).isEqualTo(TransferOutStatus.READY_TO_SEND);

        // Step 5: Send records
        transferOut = transferOutService.sendRecords(
                transferOut.getId(), TransmissionMethod.CERTIFIED_MAIL,
                "USPS-9876543210", testStaff.getId());
        assertThat(transferOut.getStatus()).isEqualTo(TransferOutStatus.SENT);

        // Step 6: Record acknowledgment
        transferOut = transferOutService.recordAcknowledgment(
                transferOut.getId(), "Sarah Johnson, Registrar", "Email", testStaff.getId());
        assertThat(transferOut.getStatus()).isEqualTo(TransferOutStatus.ACKNOWLEDGED);

        // Step 7: Complete transfer out
        transferOut = transferOutService.completeTransferOut(
                transferOut.getId(), testStaff.getId());
        assertThat(transferOut.getStatus()).isEqualTo(TransferOutStatus.COMPLETED);

        // Then - Verify final state
        assertThat(transferOut.getAllDocumentsPackaged()).isTrue();
        assertThat(transferOut.getParentConsentObtained()).isTrue();
        assertThat(transferOut.getFerpaReleaseObtained()).isTrue();
        assertThat(transferOut.getDestinationAcknowledged()).isTrue();
        assertThat(transferOut.getTrackingNumber()).isEqualTo("USPS-9876543210");
        assertThat(transferOut.getProcessingCompletedDate()).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception when transfer out not found")
    void testTransferOutNotFound() {
        // When/Then
        assertThatThrownBy(() -> transferOutService.getTransferOutById(99999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }
}
