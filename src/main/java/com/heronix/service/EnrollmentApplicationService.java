package com.heronix.service;

import com.heronix.model.domain.*;
import com.heronix.model.domain.EnrollmentApplication.ApplicationStatus;
import com.heronix.model.domain.EnrollmentApplication.EnrollmentType;
import com.heronix.repository.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Enrollment Application Service
 *
 * Manages the complete in-house enrollment workflow for new and transferring students.
 * Handles application creation, document verification tracking, status progression,
 * and student account creation upon approval.
 *
 * Core Workflow:
 * 1. Staff creates draft application during in-person registration
 * 2. Collects required documents (birth cert, residency, immunizations)
 * 3. Verifies documents and updates status
 * 4. Requests records from previous school (if transfer)
 * 5. Submits for admin approval when all docs complete
 * 6. Admin approves/rejects application
 * 7. Creates student account and enrolls upon approval
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Inquiry and Registration System
 */
@Slf4j
@Service
public class EnrollmentApplicationService {

    @Autowired
    private EnrollmentApplicationRepository applicationRepository;

    @Autowired
    private EnrollmentDocumentRepository documentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecordTransferRequestRepository recordTransferRequestRepository;

    // ========================================================================
    // APPLICATION CRUD OPERATIONS
    // ========================================================================

    /**
     * Create a new enrollment application (draft status)
     */
    @Transactional
    public EnrollmentApplication createApplication(
            EnrollmentType enrollmentType,
            String intendedGradeLevel,
            String intendedSchoolYear,
            Long createdByStaffId) {

        log.info("Creating new enrollment application: type={}, grade={}, year={}",
                enrollmentType, intendedGradeLevel, intendedSchoolYear);

        User staff = userRepository.findById(createdByStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + createdByStaffId));

        String appNumber = generateApplicationNumber();

        EnrollmentApplication application = EnrollmentApplication.builder()
                .applicationNumber(appNumber)
                .status(ApplicationStatus.DRAFT)
                .enrollmentType(enrollmentType)
                .applicationDate(LocalDate.now())
                .intendedGradeLevel(intendedGradeLevel)
                .intendedSchoolYear(intendedSchoolYear)
                .createdBy(staff)
                .createdAt(LocalDateTime.now())
                .birthCertificateVerified(false)
                .residencyVerified(false)
                .immunizationsVerified(false)
                .build();

        application = applicationRepository.save(application);
        log.info("Created application: {} (ID: {})", appNumber, application.getId());

        return application;
    }

    /**
     * Update an existing application
     */
    @Transactional
    public EnrollmentApplication updateApplication(Long applicationId, EnrollmentApplication updates, Long staffId) {
        log.info("Updating application ID: {}", applicationId);

        EnrollmentApplication existing = getApplicationById(applicationId);
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + staffId));

        // Update student information
        if (updates.getStudentFirstName() != null) existing.setStudentFirstName(updates.getStudentFirstName());
        if (updates.getStudentMiddleName() != null) existing.setStudentMiddleName(updates.getStudentMiddleName());
        if (updates.getStudentLastName() != null) existing.setStudentLastName(updates.getStudentLastName());
        if (updates.getStudentDateOfBirth() != null) existing.setStudentDateOfBirth(updates.getStudentDateOfBirth());
        if (updates.getStudentGender() != null) existing.setStudentGender(updates.getStudentGender());
        if (updates.getStudentRace() != null) existing.setStudentRace(updates.getStudentRace());
        if (updates.getStudentEthnicity() != null) existing.setStudentEthnicity(updates.getStudentEthnicity());
        if (updates.getPrimaryLanguage() != null) existing.setPrimaryLanguage(updates.getPrimaryLanguage());

        // Update contact information
        if (updates.getResidentialAddress() != null) existing.setResidentialAddress(updates.getResidentialAddress());
        if (updates.getResidentialCity() != null) existing.setResidentialCity(updates.getResidentialCity());
        if (updates.getResidentialState() != null) existing.setResidentialState(updates.getResidentialState());
        if (updates.getResidentialZipCode() != null) existing.setResidentialZipCode(updates.getResidentialZipCode());

        // Update parent 1 information
        if (updates.getParent1FirstName() != null) existing.setParent1FirstName(updates.getParent1FirstName());
        if (updates.getParent1LastName() != null) existing.setParent1LastName(updates.getParent1LastName());
        if (updates.getParent1PhoneNumber() != null) existing.setParent1PhoneNumber(updates.getParent1PhoneNumber());
        if (updates.getParent1Email() != null) existing.setParent1Email(updates.getParent1Email());

        // Update parent 2 information (if provided)
        if (updates.getParent2FirstName() != null) existing.setParent2FirstName(updates.getParent2FirstName());
        if (updates.getParent2LastName() != null) existing.setParent2LastName(updates.getParent2LastName());
        if (updates.getParent2PhoneNumber() != null) existing.setParent2PhoneNumber(updates.getParent2PhoneNumber());
        if (updates.getParent2Email() != null) existing.setParent2Email(updates.getParent2Email());

        // Update previous school information
        if (updates.getPreviousSchoolName() != null) existing.setPreviousSchoolName(updates.getPreviousSchoolName());
        if (updates.getPreviousSchoolDistrict() != null) existing.setPreviousSchoolDistrict(updates.getPreviousSchoolDistrict());
        if (updates.getPreviousSchoolCity() != null) existing.setPreviousSchoolCity(updates.getPreviousSchoolCity());
        if (updates.getPreviousSchoolState() != null) existing.setPreviousSchoolState(updates.getPreviousSchoolState());

        // Update special programs
        if (updates.getHasIEP() != null) existing.setHasIEP(updates.getHasIEP());
        if (updates.getHas504Plan() != null) existing.setHas504Plan(updates.getHas504Plan());
        if (updates.getIsGifted() != null) existing.setIsGifted(updates.getIsGifted());

        existing.setUpdatedBy(staff);
        existing.setUpdatedAt(LocalDateTime.now());

        return applicationRepository.save(existing);
    }

    /**
     * Get application by ID
     */
    public EnrollmentApplication getApplicationById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + id));
    }

    /**
     * Get application by application number
     */
    public EnrollmentApplication getApplicationByNumber(String applicationNumber) {
        return applicationRepository.findByApplicationNumber(applicationNumber)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationNumber));
    }

    // ========================================================================
    // APPLICATION STATUS MANAGEMENT
    // ========================================================================

    /**
     * Submit application for document collection
     */
    @Transactional
    public EnrollmentApplication submitForDocuments(Long applicationId, Long staffId) {
        log.info("Submitting application {} for document collection", applicationId);

        EnrollmentApplication app = getApplicationById(applicationId);
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found"));

        if (app.getStatus() != ApplicationStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT applications can be submitted for documents");
        }

        app.setStatus(ApplicationStatus.DOCUMENTS_PENDING);
        app.setSubmittedAt(LocalDateTime.now());
        app.setUpdatedBy(staff);
        app.setUpdatedAt(LocalDateTime.now());

        return applicationRepository.save(app);
    }

    /**
     * Mark all required documents as received and move to verification
     */
    @Transactional
    public EnrollmentApplication moveToVerification(Long applicationId, Long staffId) {
        log.info("Moving application {} to verification", applicationId);

        EnrollmentApplication app = getApplicationById(applicationId);
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found"));

        app.setStatus(ApplicationStatus.VERIFICATION_IN_PROGRESS);
        app.setUpdatedBy(staff);
        app.setUpdatedAt(LocalDateTime.now());

        return applicationRepository.save(app);
    }

    /**
     * Request records from previous school (for transfers)
     */
    @Transactional
    public EnrollmentApplication requestPreviousSchoolRecords(Long applicationId, Long staffId) {
        log.info("Requesting previous school records for application {}", applicationId);

        EnrollmentApplication app = getApplicationById(applicationId);

        if (!Boolean.TRUE.equals(app.getIsTransferStudent())) {
            throw new IllegalStateException("Cannot request records for non-transfer student");
        }

        app.setStatus(ApplicationStatus.RECORDS_REQUESTED);
        app.setUpdatedAt(LocalDateTime.now());

        return applicationRepository.save(app);
    }

    /**
     * Submit for admin approval (all documents complete)
     */
    @Transactional
    public EnrollmentApplication submitForApproval(Long applicationId, Long staffId) {
        log.info("Submitting application {} for approval", applicationId);

        EnrollmentApplication app = getApplicationById(applicationId);

        if (!app.areRequiredDocumentsComplete()) {
            throw new IllegalStateException("Cannot submit for approval - required documents not complete");
        }

        app.setStatus(ApplicationStatus.PENDING_APPROVAL);
        app.setUpdatedAt(LocalDateTime.now());

        return applicationRepository.save(app);
    }

    /**
     * Approve application
     */
    @Transactional
    public EnrollmentApplication approveApplication(Long applicationId, Long adminStaffId, String approvalNotes) {
        log.info("Approving application {} by admin {}", applicationId, adminStaffId);

        EnrollmentApplication app = getApplicationById(applicationId);
        User admin = userRepository.findById(adminStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found"));

        if (!app.canBeApproved()) {
            throw new IllegalStateException("Application cannot be approved - check status and required documents");
        }

        app.setStatus(ApplicationStatus.APPROVED);
        app.setApprovedBy(admin);
        app.setApprovedAt(LocalDateTime.now());
        app.setApprovalNotes(approvalNotes);
        app.setUpdatedAt(LocalDateTime.now());

        return applicationRepository.save(app);
    }

    /**
     * Reject application
     */
    @Transactional
    public EnrollmentApplication rejectApplication(Long applicationId, Long adminStaffId, String rejectionReason) {
        log.info("Rejecting application {} by admin {}", applicationId, adminStaffId);

        EnrollmentApplication app = getApplicationById(applicationId);
        User admin = userRepository.findById(adminStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found"));

        app.setStatus(ApplicationStatus.REJECTED);
        app.setApprovedBy(admin); // Track who reviewed it
        app.setRejectionReason(rejectionReason);
        app.setUpdatedAt(LocalDateTime.now());

        return applicationRepository.save(app);
    }

    /**
     * Withdraw application
     */
    @Transactional
    public EnrollmentApplication withdrawApplication(Long applicationId, Long staffId, String reason) {
        log.info("Withdrawing application {}", applicationId);

        EnrollmentApplication app = getApplicationById(applicationId);

        app.setStatus(ApplicationStatus.WITHDRAWN);
        app.setRejectionReason(reason); // Reuse field for withdrawal reason
        app.setUpdatedAt(LocalDateTime.now());

        return applicationRepository.save(app);
    }

    // ========================================================================
    // STUDENT ACCOUNT CREATION
    // ========================================================================

    /**
     * Enroll student (create Student account from approved application)
     */
    @Transactional
    public Student enrollStudent(Long applicationId, Long staffId) {
        log.info("Enrolling student from application {}", applicationId);

        EnrollmentApplication app = getApplicationById(applicationId);

        if (app.getStatus() != ApplicationStatus.APPROVED) {
            throw new IllegalStateException("Cannot enroll - application must be APPROVED first");
        }

        if (app.getStudent() != null) {
            throw new IllegalStateException("Student already created for this application");
        }

        // Generate student ID
        String studentId = generateStudentId(app.getIntendedGradeLevel());

        // Create student entity
        Student student = new Student();
        student.setStudentId(studentId);
        student.setFirstName(app.getStudentFirstName());
        student.setLastName(app.getStudentLastName());
        student.setDateOfBirth(app.getStudentDateOfBirth());
        student.setGender(app.getStudentGender());
        student.setGradeLevel(app.getIntendedGradeLevel());
        student.setRace(app.getStudentRace());
        student.setEthnicity(app.getStudentEthnicity());
        student.setHasIEP(app.getHasIEP());
        student.setHas504Plan(app.getHas504Plan());
        student.setIsGifted(app.getIsGifted());
        student.setActive(true);
        // Note: middleName and enrollmentDate may not exist on Student entity

        student = studentRepository.save(student);
        log.info("Created student account: {} (ID: {})", studentId, student.getId());

        // Link student to application
        app.setStudent(student);
        app.setStatus(ApplicationStatus.ENROLLED);
        app.setEnrolledAt(LocalDateTime.now());
        app.setProcessedBy(userRepository.findById(staffId).orElse(null));
        applicationRepository.save(app);

        return student;
    }

    // ========================================================================
    // SEARCH AND QUERY METHODS
    // ========================================================================

    /**
     * Search applications by student or parent name
     */
    public List<EnrollmentApplication> searchByName(String name) {
        List<EnrollmentApplication> results = new ArrayList<>();
        results.addAll(applicationRepository.findByStudentName(name));
        results.addAll(applicationRepository.findByParentName(name));
        return results.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Get applications by status
     */
    public List<EnrollmentApplication> getApplicationsByStatus(ApplicationStatus status) {
        return applicationRepository.findByStatus(status);
    }

    /**
     * Get applications ready for approval
     */
    public List<EnrollmentApplication> getApplicationsReadyForApproval() {
        return applicationRepository.findApplicationsReadyForApproval();
    }

    /**
     * Get applications awaiting documents
     */
    public List<EnrollmentApplication> getApplicationsAwaitingDocuments() {
        return applicationRepository.findApplicationsAwaitingDocuments();
    }

    // ========================================================================
    // STATISTICS
    // ========================================================================

    /**
     * Get enrollment statistics
     */
    public EnrollmentStatistics getStatistics(String schoolYear) {
        return EnrollmentStatistics.builder()
                .totalApplications(applicationRepository.count())
                .draftCount(applicationRepository.countByStatus(ApplicationStatus.DRAFT))
                .awaitingDocsCount(applicationRepository.countByStatus(ApplicationStatus.DOCUMENTS_PENDING))
                .pendingApprovalCount(applicationRepository.countByStatus(ApplicationStatus.PENDING_APPROVAL))
                .approvedCount(applicationRepository.countByStatus(ApplicationStatus.APPROVED))
                .enrolledCount(applicationRepository.countByStatus(ApplicationStatus.ENROLLED))
                .rejectedCount(applicationRepository.countByStatus(ApplicationStatus.REJECTED))
                .build();
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    /**
     * Generate unique application number
     */
    private String generateApplicationNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        long count = applicationRepository.count() + 1;
        return String.format("%s-%06d", year, count);
    }

    /**
     * Generate unique student ID
     */
    private String generateStudentId(String gradeLevel) {
        String year = String.valueOf(LocalDate.now().getYear()).substring(2);
        long count = studentRepository.count() + 1;
        return String.format("%s%s%05d", year, gradeLevel, count);
    }

    // ========================================================================
    // DTO CLASSES
    // ========================================================================

    @Data
    @Builder
    public static class EnrollmentStatistics {
        private long totalApplications;
        private long draftCount;
        private long awaitingDocsCount;
        private long pendingApprovalCount;
        private long approvedCount;
        private long enrolledCount;
        private long rejectedCount;
    }
}
