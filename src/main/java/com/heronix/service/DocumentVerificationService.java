package com.heronix.service;

import com.heronix.model.domain.EnrollmentApplication;
import com.heronix.model.domain.EnrollmentDocument;
import com.heronix.model.domain.EnrollmentDocument.DocumentType;
import com.heronix.model.domain.EnrollmentDocument.VerificationStatus;
import com.heronix.model.domain.User;
import com.heronix.repository.EnrollmentApplicationRepository;
import com.heronix.repository.EnrollmentDocumentRepository;
import com.heronix.repository.UserRepository;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Document Verification Service
 *
 * Manages document upload, verification, and tracking for enrollment applications.
 * Handles birth certificates, residency proof, immunization records, and other required documents.
 *
 * Key Responsibilities:
 * - Upload/attach documents to applications
 * - Verify document authenticity and validity
 * - Track document expiration (immunizations, etc.)
 * - Update application verification flags
 * - Generate missing document reports
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Inquiry and Registration System
 */
@Slf4j
@Service
public class DocumentVerificationService {

    @Autowired
    private EnrollmentDocumentRepository documentRepository;

    @Autowired
    private EnrollmentApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    // ========================================================================
    // DOCUMENT UPLOAD AND MANAGEMENT
    // ========================================================================

    /**
     * Upload/attach a document to an enrollment application
     */
    @Transactional
    public EnrollmentDocument uploadDocument(
            Long applicationId,
            DocumentType documentType,
            String documentName,
            String filePath,
            String fileType,
            Long fileSizeBytes,
            Boolean isOriginal,
            Long uploadedByStaffId) {

        log.info("Uploading document {} for application {}", documentType, applicationId);

        EnrollmentApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        User staff = userRepository.findById(uploadedByStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + uploadedByStaffId));

        EnrollmentDocument document = EnrollmentDocument.builder()
                .application(application)
                .documentType(documentType)
                .documentName(documentName)
                .filePath(filePath)
                .fileType(fileType)
                .fileSizeBytes(fileSizeBytes)
                .isOriginal(isOriginal)
                .verificationStatus(VerificationStatus.PENDING)
                .uploadedBy(staff)
                .uploadedAt(LocalDateTime.now())
                .build();

        document = documentRepository.save(document);
        application.addDocument(document);
        applicationRepository.save(application);

        log.info("Document uploaded: ID {} for application {}", document.getId(), applicationId);
        return document;
    }

    /**
     * Get all documents for an application
     */
    public List<EnrollmentDocument> getDocumentsForApplication(Long applicationId) {
        EnrollmentApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));
        return documentRepository.findByApplicationOrderByUploadedAtDesc(application);
    }

    /**
     * Get documents by type for an application
     */
    public List<EnrollmentDocument> getDocumentsByType(Long applicationId, DocumentType documentType) {
        EnrollmentApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));
        return documentRepository.findByApplicationAndDocumentType(application, documentType);
    }

    // ========================================================================
    // DOCUMENT VERIFICATION
    // ========================================================================

    /**
     * Verify a document (mark as accepted)
     */
    @Transactional
    public EnrollmentDocument verifyDocument(Long documentId, Long verifierStaffId, String notes) {
        log.info("Verifying document ID {} by staff {}", documentId, verifierStaffId);

        EnrollmentDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));

        User verifier = userRepository.findById(verifierStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Verifier user not found: " + verifierStaffId));

        document.markVerified(verifier, notes);
        document = documentRepository.save(document);

        // Update application verification flags
        updateApplicationVerificationFlags(document.getApplication().getId());

        log.info("Document {} verified by {}", documentId, verifier.getUsername());
        return document;
    }

    /**
     * Reject a document (mark as invalid)
     */
    @Transactional
    public EnrollmentDocument rejectDocument(Long documentId, Long verifierStaffId, String reason) {
        log.info("Rejecting document ID {} by staff {}", documentId, verifierStaffId);

        EnrollmentDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));

        User verifier = userRepository.findById(verifierStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Verifier user not found: " + verifierStaffId));

        document.markRejected(verifier, reason);
        document = documentRepository.save(document);

        log.info("Document {} rejected by {}: {}", documentId, verifier.getUsername(), reason);
        return document;
    }

    /**
     * Mark document as needing follow-up
     */
    @Transactional
    public EnrollmentDocument markNeedsFollowUp(Long documentId, String reason) {
        EnrollmentDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));

        document.setNeedsFollowUp(true);
        document.setFollowUpReason(reason);
        document.setVerificationStatus(VerificationStatus.IN_REVIEW);

        return documentRepository.save(document);
    }

    // ========================================================================
    // APPLICATION VERIFICATION FLAGS UPDATE
    // ========================================================================

    /**
     * Update application verification flags based on verified documents
     */
    @Transactional
    public void updateApplicationVerificationFlags(Long applicationId) {
        log.info("Updating verification flags for application {}", applicationId);

        EnrollmentApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        List<EnrollmentDocument> verifiedDocs = documentRepository
                .findVerifiedDocumentsByApplication(application);

        // Check birth certificate
        boolean hasBirthCert = verifiedDocs.stream()
                .anyMatch(d -> d.getDocumentType() == DocumentType.BIRTH_CERTIFICATE);
        application.setBirthCertificateVerified(hasBirthCert);

        // Check residency
        boolean hasResidency = verifiedDocs.stream()
                .anyMatch(d -> d.getDocumentType() == DocumentType.PROOF_OF_RESIDENCY);
        application.setResidencyVerified(hasResidency);

        // Check immunizations
        boolean hasImmunizations = verifiedDocs.stream()
                .anyMatch(d -> d.getDocumentType() == DocumentType.IMMUNIZATION_RECORDS);
        application.setImmunizationsVerified(hasImmunizations);

        // Check transcript (for transfers)
        if (Boolean.TRUE.equals(application.getIsTransferStudent())) {
            boolean hasTranscript = verifiedDocs.stream()
                    .anyMatch(d -> d.getDocumentType() == DocumentType.TRANSCRIPT);
            application.setTranscriptReceived(hasTranscript);
        }

        // Check IEP (if applicable)
        if (Boolean.TRUE.equals(application.getHasIEP())) {
            boolean hasIEP = verifiedDocs.stream()
                    .anyMatch(d -> d.getDocumentType() == DocumentType.IEP);
            application.setIepReceived(hasIEP);
        }

        // Check 504 (if applicable)
        if (Boolean.TRUE.equals(application.getHas504Plan())) {
            boolean has504 = verifiedDocs.stream()
                    .anyMatch(d -> d.getDocumentType() == DocumentType.PLAN_504);
            application.setPlan504Received(has504);
        }

        applicationRepository.save(application);
        log.info("Verification flags updated for application {}", applicationId);
    }

    // ========================================================================
    // QUERIES AND REPORTS
    // ========================================================================

    /**
     * Get all documents pending verification
     */
    public List<EnrollmentDocument> getPendingDocuments() {
        return documentRepository.findAllPendingVerification();
    }

    /**
     * Get documents needing follow-up
     */
    public List<EnrollmentDocument> getDocumentsNeedingFollowUp() {
        return documentRepository.findByNeedsFollowUpTrue();
    }

    /**
     * Get expired documents
     */
    public List<EnrollmentDocument> getExpiredDocuments() {
        return documentRepository.findExpiredDocuments();
    }

    /**
     * Get documents expiring soon (within 30 days)
     */
    public List<EnrollmentDocument> getDocumentsExpiringSoon() {
        return documentRepository.findDocumentsExpiringSoon();
    }

    /**
     * Get verification summary for an application
     */
    public DocumentVerificationSummary getVerificationSummary(Long applicationId) {
        EnrollmentApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        List<EnrollmentDocument> allDocs = documentRepository.findByApplication(application);
        List<EnrollmentDocument> verifiedDocs = documentRepository.findVerifiedDocumentsByApplication(application);
        List<EnrollmentDocument> pendingDocs = documentRepository.findPendingDocumentsByApplication(application);

        return DocumentVerificationSummary.builder()
                .applicationId(applicationId)
                .totalDocuments(allDocs.size())
                .verifiedDocuments(verifiedDocs.size())
                .pendingDocuments(pendingDocs.size())
                .hasBirthCertificate(application.getBirthCertificateVerified())
                .hasResidencyProof(application.getResidencyVerified())
                .hasImmunizations(application.getImmunizationsVerified())
                .hasTranscript(application.getTranscriptReceived())
                .hasIEP(application.getIepReceived())
                .has504Plan(application.getPlan504Received())
                .allRequiredDocsComplete(application.areRequiredDocumentsComplete())
                .build();
    }

    /**
     * Get missing documents for an application
     */
    public List<DocumentType> getMissingDocuments(Long applicationId) {
        EnrollmentApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        List<EnrollmentDocument> verifiedDocs = documentRepository
                .findVerifiedDocumentsByApplication(application);

        List<DocumentType> missing = new java.util.ArrayList<>();

        // Always required
        if (!Boolean.TRUE.equals(application.getBirthCertificateVerified())) {
            missing.add(DocumentType.BIRTH_CERTIFICATE);
        }
        if (!Boolean.TRUE.equals(application.getResidencyVerified())) {
            missing.add(DocumentType.PROOF_OF_RESIDENCY);
        }
        if (!Boolean.TRUE.equals(application.getImmunizationsVerified())) {
            missing.add(DocumentType.IMMUNIZATION_RECORDS);
        }

        // Conditional requirements
        if (Boolean.TRUE.equals(application.getIsTransferStudent()) &&
            !Boolean.TRUE.equals(application.getTranscriptReceived())) {
            missing.add(DocumentType.TRANSCRIPT);
        }
        if (Boolean.TRUE.equals(application.getHasIEP()) &&
            !Boolean.TRUE.equals(application.getIepReceived())) {
            missing.add(DocumentType.IEP);
        }
        if (Boolean.TRUE.equals(application.getHas504Plan()) &&
            !Boolean.TRUE.equals(application.getPlan504Received())) {
            missing.add(DocumentType.PLAN_504);
        }

        return missing;
    }

    // ========================================================================
    // DTO CLASSES
    // ========================================================================

    @Data
    @Builder
    public static class DocumentVerificationSummary {
        private Long applicationId;
        private int totalDocuments;
        private int verifiedDocuments;
        private int pendingDocuments;
        private Boolean hasBirthCertificate;
        private Boolean hasResidencyProof;
        private Boolean hasImmunizations;
        private Boolean hasTranscript;
        private Boolean hasIEP;
        private Boolean has504Plan;
        private boolean allRequiredDocsComplete;
    }
}
