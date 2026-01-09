package com.heronix.service;

import com.heronix.model.domain.ReEnrollment;
import com.heronix.model.domain.ReEnrollment.ReEnrollmentStatus;
import com.heronix.model.domain.ReEnrollment.ApprovalDecision;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.User;
import com.heronix.repository.ReEnrollmentRepository;
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
 * Integration tests for ReEnrollmentService
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@SpringBootTest(classes = com.heronix.HeronixSchedulerApplication.class)
@ActiveProfiles("test")
@Transactional
class ReEnrollmentServiceTest {

    @Autowired
    private ReEnrollmentService reEnrollmentService;

    @Autowired
    private ReEnrollmentRepository reEnrollmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    private Student testStudent;
    private User testStaff;
    private User testCounselor;
    private User testPrincipal;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        reEnrollmentRepository.deleteAll();

        // Create test student
        testStudent = studentRepository.findAll().stream().findFirst()
                .orElseGet(() -> {
                    Student student = new Student();
                    student.setFirstName("Test");
                    student.setLastName("Student");
                    student.setStudentId("RE001");
                    return studentRepository.save(student);
                });

        // Create test staff users
        List<User> users = userRepository.findAll();
        testStaff = users.stream().findFirst()
                .orElseGet(() -> {
                    User staff = new User();
                    staff.setUsername("teststaff");
                    staff.setEmail("staff@heronix.edu");
                    return userRepository.save(staff);
                });

        testCounselor = users.size() > 1 ? users.get(1) : createUser("counselor", "counselor@heronix.edu");
        testPrincipal = users.size() > 2 ? users.get(2) : createUser("principal", "principal@heronix.edu");
    }

    private User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        return userRepository.save(user);
    }

    @Test
    @DisplayName("Should create re-enrollment application successfully")
    void testCreateReEnrollment() {
        // Given
        String requestedGrade = "10th Grade";
        LocalDate intendedDate = LocalDate.now().plusMonths(2);

        // When
        ReEnrollment reEnrollment = reEnrollmentService.createReEnrollment(
                testStudent.getId(), requestedGrade, intendedDate, testStaff.getId());

        // Then
        assertThat(reEnrollment).isNotNull();
        assertThat(reEnrollment.getId()).isNotNull();
        assertThat(reEnrollment.getReEnrollmentNumber()).isNotNull();
        assertThat(reEnrollment.getReEnrollmentNumber()).startsWith("REE-");
        assertThat(reEnrollment.getStatus()).isEqualTo(ReEnrollmentStatus.DRAFT);
        assertThat(reEnrollment.getStudent()).isNotNull();
        assertThat(reEnrollment.getRequestedGradeLevel()).isEqualTo(requestedGrade);
        assertThat(reEnrollment.getIntendedEnrollmentDate()).isEqualTo(intendedDate);
    }

    @Test
    @DisplayName("Should retrieve re-enrollment by ID")
    void testGetReEnrollmentById() {
        // Given
        ReEnrollment created = reEnrollmentService.createReEnrollment(
                testStudent.getId(), "11th Grade",
                LocalDate.now().plusMonths(1), testStaff.getId());

        // When
        ReEnrollment retrieved = reEnrollmentService.getReEnrollmentById(created.getId());

        // Then
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getId()).isEqualTo(created.getId());
        assertThat(retrieved.getReEnrollmentNumber()).isEqualTo(created.getReEnrollmentNumber());
    }

    @Test
    @DisplayName("Should retrieve re-enrollment by number")
    void testGetByReEnrollmentNumber() {
        // Given
        ReEnrollment created = reEnrollmentService.createReEnrollment(
                testStudent.getId(), "9th Grade",
                LocalDate.now().plusMonths(3), testStaff.getId());

        // When
        ReEnrollment retrieved = reEnrollmentService.getByReEnrollmentNumber(
                created.getReEnrollmentNumber());

        // Then
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getId()).isEqualTo(created.getId());
    }

    @Test
    @DisplayName("Should retrieve re-enrollments by status")
    void testGetByStatus() {
        // Given
        reEnrollmentService.createReEnrollment(
                testStudent.getId(), "10th Grade",
                LocalDate.now().plusMonths(1), testStaff.getId());
        reEnrollmentService.createReEnrollment(
                testStudent.getId(), "11th Grade",
                LocalDate.now().plusMonths(2), testStaff.getId());

        // When
        List<ReEnrollment> drafts = reEnrollmentService.getByStatus(ReEnrollmentStatus.DRAFT);

        // Then
        assertThat(drafts).isNotEmpty();
        assertThat(drafts).hasSizeGreaterThanOrEqualTo(2);
        assertThat(drafts).allMatch(re -> re.getStatus() == ReEnrollmentStatus.DRAFT);
    }

    @Test
    @DisplayName("Should update re-enrollment application")
    void testUpdateReEnrollment() {
        // Given
        ReEnrollment reEnrollment = reEnrollmentService.createReEnrollment(
                testStudent.getId(), "10th Grade",
                LocalDate.now().plusMonths(1), testStaff.getId());

        // When
        reEnrollment.setRequestedGradeLevel("11th Grade");
        ReEnrollment updated = reEnrollmentService.updateReEnrollment(reEnrollment, testStaff.getId());

        // Then
        assertThat(updated.getRequestedGradeLevel()).isEqualTo("11th Grade");
        assertThat(updated.getUpdatedBy()).isNotNull();
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should assign counselor to re-enrollment")
    void testAssignCounselor() {
        // Given
        ReEnrollment reEnrollment = reEnrollmentService.createReEnrollment(
                testStudent.getId(), "12th Grade",
                LocalDate.now().plusMonths(1), testStaff.getId());

        // When
        ReEnrollment updated = reEnrollmentService.assignCounselor(
                reEnrollment.getId(), testCounselor.getId(), testStaff.getId());

        // Then
        assertThat(updated.getAssignedCounselor()).isNotNull();
        assertThat(updated.getAssignedCounselor().getId()).isEqualTo(testCounselor.getId());
    }

    @Test
    @DisplayName("Should record counselor approval decision")
    void testCounselorDecision() {
        // Given
        ReEnrollment reEnrollment = reEnrollmentService.createReEnrollment(
                testStudent.getId(), "10th Grade",
                LocalDate.now().plusMonths(1), testStaff.getId());
        reEnrollmentService.assignCounselor(reEnrollment.getId(), testCounselor.getId(), testStaff.getId());

        // When
        ReEnrollment updated = reEnrollmentService.counselorDecision(
                reEnrollment.getId(), ApprovalDecision.APPROVED,
                "Student shows readiness to return", testCounselor.getId());

        // Then
        assertThat(updated.getCounselorDecision()).isEqualTo(ApprovalDecision.APPROVED);
        assertThat(updated.getCounselorRecommendation()).contains("readiness to return");
        assertThat(updated.getCounselorReviewDate()).isNotNull();
        assertThat(updated.getStatus()).isEqualTo(ReEnrollmentStatus.PENDING_APPROVAL);
    }

    @Test
    @DisplayName("Should record principal approval decision")
    void testPrincipalDecision() {
        // Given
        ReEnrollment reEnrollment = reEnrollmentService.createReEnrollment(
                testStudent.getId(), "11th Grade",
                LocalDate.now().plusMonths(1), testStaff.getId());
        reEnrollmentService.assignCounselor(reEnrollment.getId(), testCounselor.getId(), testStaff.getId());
        reEnrollmentService.counselorDecision(
                reEnrollment.getId(), ApprovalDecision.APPROVED,
                "Counselor approved", testCounselor.getId());

        // When
        ReEnrollment updated = reEnrollmentService.principalDecision(
                reEnrollment.getId(), ApprovalDecision.APPROVED,
                "Principal approved for re-enrollment", testPrincipal.getId());

        // Then
        assertThat(updated.getPrincipalDecision()).isEqualTo(ApprovalDecision.APPROVED);
        assertThat(updated.getPrincipalNotes()).contains("Principal approved");
        assertThat(updated.getStatus()).isEqualTo(ReEnrollmentStatus.APPROVED);
    }

    @Test
    @DisplayName("Should reject re-enrollment at principal level")
    void testPrincipalRejection() {
        // Given
        ReEnrollment reEnrollment = reEnrollmentService.createReEnrollment(
                testStudent.getId(), "9th Grade",
                LocalDate.now().plusMonths(1), testStaff.getId());
        reEnrollmentService.assignCounselor(reEnrollment.getId(), testCounselor.getId(), testStaff.getId());
        reEnrollmentService.counselorDecision(
                reEnrollment.getId(), ApprovalDecision.APPROVED, "Approved", testCounselor.getId());

        // When
        ReEnrollment updated = reEnrollmentService.principalDecision(
                reEnrollment.getId(), ApprovalDecision.DENIED,
                "Academic requirements not met", testPrincipal.getId());

        // Then
        assertThat(updated.getPrincipalDecision()).isEqualTo(ApprovalDecision.DENIED);
        assertThat(updated.getStatus()).isEqualTo(ReEnrollmentStatus.REJECTED);
    }

    @Test
    @DisplayName("Should record fee payment")
    void testRecordFeePayment() {
        // Given
        ReEnrollment reEnrollment = reEnrollmentService.createReEnrollment(
                testStudent.getId(), "10th Grade",
                LocalDate.now().plusMonths(1), testStaff.getId());
        reEnrollment.setHasOutstandingFees(true);
        reEnrollment.setOutstandingFeesAmount(150.00);
        reEnrollmentService.updateReEnrollment(reEnrollment, testStaff.getId());

        // When
        ReEnrollment updated = reEnrollmentService.recordFeePayment(
                reEnrollment.getId(), testStaff.getId());

        // Then
        assertThat(updated.getFeesPaid()).isTrue();
    }

    @Test
    @DisplayName("Should complete re-enrollment")
    void testCompleteReEnrollment() {
        // Given
        ReEnrollment reEnrollment = reEnrollmentService.createReEnrollment(
                testStudent.getId(), "10th Grade",
                LocalDate.now().plusMonths(1), testStaff.getId());

        // Approve the re-enrollment
        reEnrollmentService.assignCounselor(reEnrollment.getId(), testCounselor.getId(), testStaff.getId());
        reEnrollmentService.counselorDecision(
                reEnrollment.getId(), ApprovalDecision.APPROVED, "Approved", testCounselor.getId());
        reEnrollmentService.principalDecision(
                reEnrollment.getId(), ApprovalDecision.APPROVED, "Approved", testPrincipal.getId());

        // When
        ReEnrollment completed = reEnrollmentService.completeReEnrollment(
                reEnrollment.getId(), testStudent.getId(), testStaff.getId());

        // Then
        assertThat(completed.getStatus()).isEqualTo(ReEnrollmentStatus.ENROLLED);
    }

    @Test
    @DisplayName("Should cancel re-enrollment")
    void testCancelReEnrollment() {
        // Given
        ReEnrollment reEnrollment = reEnrollmentService.createReEnrollment(
                testStudent.getId(), "11th Grade",
                LocalDate.now().plusMonths(1), testStaff.getId());

        // When
        String reason = "Student decided to attend different school";
        ReEnrollment cancelled = reEnrollmentService.cancelReEnrollment(
                reEnrollment.getId(), reason, testStaff.getId());

        // Then
        assertThat(cancelled.getStatus()).isEqualTo(ReEnrollmentStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should not allow cancellation of enrolled re-enrollment")
    void testCannotCancelEnrolled() {
        // Given
        ReEnrollment reEnrollment = reEnrollmentService.createReEnrollment(
                testStudent.getId(), "10th Grade",
                LocalDate.now().plusMonths(1), testStaff.getId());

        // Approve and complete
        reEnrollmentService.assignCounselor(reEnrollment.getId(), testCounselor.getId(), testStaff.getId());
        reEnrollmentService.counselorDecision(
                reEnrollment.getId(), ApprovalDecision.APPROVED, "Approved", testCounselor.getId());
        reEnrollmentService.principalDecision(
                reEnrollment.getId(), ApprovalDecision.APPROVED, "Approved", testPrincipal.getId());
        reEnrollmentService.completeReEnrollment(reEnrollment.getId(), testStudent.getId(), testStaff.getId());

        // When/Then
        assertThatThrownBy(() -> reEnrollmentService.cancelReEnrollment(
                reEnrollment.getId(), "Testing", testStaff.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cancel");
    }

    @Test
    @DisplayName("Should delete draft re-enrollment")
    void testDeleteDraftReEnrollment() {
        // Given
        ReEnrollment reEnrollment = reEnrollmentService.createReEnrollment(
                testStudent.getId(), "9th Grade",
                LocalDate.now().plusMonths(1), testStaff.getId());
        Long reEnrollmentId = reEnrollment.getId();

        // When
        reEnrollmentService.deleteReEnrollment(reEnrollmentId);

        // Then
        assertThatThrownBy(() -> reEnrollmentService.getReEnrollmentById(reEnrollmentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Should get re-enrollment statistics")
    void testGetStatistics() {
        // Given
        reEnrollmentService.createReEnrollment(
                testStudent.getId(), "10th Grade",
                LocalDate.now().plusMonths(1), testStaff.getId());
        reEnrollmentService.createReEnrollment(
                testStudent.getId(), "11th Grade",
                LocalDate.now().plusMonths(2), testStaff.getId());

        // When
        ReEnrollmentService.ReEnrollmentStatistics stats = reEnrollmentService.getStatistics();

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.total()).isGreaterThanOrEqualTo(2);
        assertThat(stats.draft()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Should throw exception when principal reviews before counselor")
    void testPrincipalReviewBeforeCounselor() {
        // Given
        ReEnrollment reEnrollment = reEnrollmentService.createReEnrollment(
                testStudent.getId(), "10th Grade",
                LocalDate.now().plusMonths(1), testStaff.getId());

        // When/Then
        assertThatThrownBy(() -> reEnrollmentService.principalDecision(
                reEnrollment.getId(), ApprovalDecision.APPROVED,
                "Trying to approve", testPrincipal.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Counselor decision required");
    }

    @Test
    @DisplayName("Should retrieve pending review re-enrollments")
    void testGetPendingReview() {
        // Given
        ReEnrollment re1 = reEnrollmentService.createReEnrollment(
                testStudent.getId(), "10th Grade",
                LocalDate.now().plusMonths(1), testStaff.getId());
        re1.setStatus(ReEnrollmentStatus.PENDING_REVIEW);
        re1.setPreviousRecordsReviewed(false);
        reEnrollmentService.updateReEnrollment(re1, testStaff.getId());

        // When
        List<ReEnrollment> pending = reEnrollmentService.getPendingReview();

        // Then
        assertThat(pending).isNotEmpty();
        assertThat(pending).anyMatch(re -> re.getId().equals(re1.getId()));
    }

    @Test
    @DisplayName("Should retrieve re-enrollments by counselor")
    void testGetByCounselor() {
        // Given
        ReEnrollment reEnrollment = reEnrollmentService.createReEnrollment(
                testStudent.getId(), "11th Grade",
                LocalDate.now().plusMonths(1), testStaff.getId());
        reEnrollmentService.assignCounselor(reEnrollment.getId(), testCounselor.getId(), testStaff.getId());

        // When
        List<ReEnrollment> counselorCases = reEnrollmentService.getByCounselor(testCounselor.getId());

        // Then
        assertThat(counselorCases).isNotEmpty();
        assertThat(counselorCases).anyMatch(re -> re.getId().equals(reEnrollment.getId()));
    }
}
