package com.heronix.service;

import com.heronix.model.domain.Plan504;
import com.heronix.model.domain.Plan504Document;
import com.heronix.model.domain.Plan504Document.DocumentType;
import com.heronix.repository.Plan504Repository;
import com.heronix.repository.Plan504DocumentRepository;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 504 Plan Document Service
 *
 * Manages document attachments for 504 plans including upload, versioning,
 * access control, and document lifecycle management.
 *
 * Key Responsibilities:
 * - Upload and store plan documents
 * - Version control for document updates
 * - Document access logging
 * - Document search and retrieval
 * - Storage management
 * - Document type categorization
 * - Confidentiality enforcement
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - 504 Management Enhancement
 */
@Slf4j
@Service
public class Plan504DocumentService {

    @Autowired
    private Plan504DocumentRepository documentRepository;

    @Autowired
    private Plan504Repository plan504Repository;

    @Value("${plan504.documents.storage.path:./data/504-documents}")
    private String storageBasePath;

    @Value("${plan504.documents.max.size.mb:10}")
    private int maxFileSizeMB;

    private static final String STORAGE_PROVIDER = "LOCAL";

    // ========================================================================
    // DOCUMENT UPLOAD
    // ========================================================================

    /**
     * Upload a new document to a 504 plan
     */
    @Transactional
    public Plan504Document uploadDocument(
            Long planId,
            String fileName,
            byte[] fileData,
            DocumentType documentType,
            String description,
            boolean isConfidential,
            String uploadedByUsername) throws IOException {

        log.info("Uploading document {} for 504 plan {}", fileName, planId);

        Plan504 plan = plan504Repository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("504 Plan not found: " + planId));

        // Validate file size
        long fileSizeBytes = fileData.length;
        if (fileSizeBytes > maxFileSizeMB * 1024 * 1024) {
            throw new IllegalArgumentException(String.format(
                    "File size %.2f MB exceeds maximum allowed size of %d MB",
                    fileSizeBytes / (1024.0 * 1024.0), maxFileSizeMB));
        }

        // Determine file type from extension
        String fileType = getFileExtension(fileName).toUpperCase();

        // Generate unique file path
        String uniqueFileName = generateUniqueFileName(fileName);
        String filePath = createFilePath(planId, uniqueFileName);

        // Store file to disk
        saveFileToDisk(filePath, fileData);

        // Create document entity
        Plan504Document document = Plan504Document.builder()
                .plan504(plan)
                .fileName(fileName)
                .fileType(fileType)
                .documentType(documentType)
                .fileSizeBytes(fileSizeBytes)
                .description(description)
                .filePath(filePath)
                .storageProvider(STORAGE_PROVIDER)
                .version(1)
                .isCurrentVersion(true)
                .isConfidential(isConfidential)
                .uploadedBy(uploadedByUsername)
                .uploadedAt(LocalDateTime.now())
                .build();

        document = documentRepository.save(document);

        log.info("Successfully uploaded document ID {} for plan {}", document.getId(), planId);
        return document;
    }

    /**
     * Upload a new version of an existing document
     */
    @Transactional
    public Plan504Document uploadNewVersion(
            Long existingDocumentId,
            byte[] fileData,
            String description,
            String uploadedByUsername) throws IOException {

        log.info("Uploading new version for document ID {}", existingDocumentId);

        Plan504Document existingDoc = documentRepository.findById(existingDocumentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + existingDocumentId));

        // Mark existing document as not current
        existingDoc.setIsCurrentVersion(false);
        existingDoc.setUpdatedBy(uploadedByUsername);
        documentRepository.save(existingDoc);

        // Create new version
        String uniqueFileName = generateUniqueFileName(existingDoc.getFileName());
        String filePath = createFilePath(existingDoc.getPlan504().getId(), uniqueFileName);

        saveFileToDisk(filePath, fileData);

        Plan504Document newVersion = Plan504Document.builder()
                .plan504(existingDoc.getPlan504())
                .fileName(existingDoc.getFileName())
                .fileType(existingDoc.getFileType())
                .documentType(existingDoc.getDocumentType())
                .fileSizeBytes((long) fileData.length)
                .description(description != null ? description : existingDoc.getDescription())
                .filePath(filePath)
                .storageProvider(STORAGE_PROVIDER)
                .version(existingDoc.getVersion() + 1)
                .isCurrentVersion(true)
                .isConfidential(existingDoc.getIsConfidential())
                .replacesDocument(existingDoc)
                .uploadedBy(uploadedByUsername)
                .build();

        newVersion = documentRepository.save(newVersion);

        log.info("Created version {} of document {} (new ID: {})",
                newVersion.getVersion(), existingDocumentId, newVersion.getId());

        return newVersion;
    }

    // ========================================================================
    // DOCUMENT RETRIEVAL
    // ========================================================================

    /**
     * Get all documents for a 504 plan
     */
    public List<Plan504Document> getDocumentsForPlan(Long planId) {
        return documentRepository.findByPlan504Id(planId);
    }

    /**
     * Get current version documents for a plan
     */
    public List<Plan504Document> getCurrentDocuments(Long planId) {
        Plan504 plan = plan504Repository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("504 Plan not found: " + planId));

        return documentRepository.findCurrentVersionsByPlan(plan);
    }

    /**
     * Download document file data
     */
    public byte[] downloadDocument(Long documentId, String accessedByUsername) throws IOException {
        log.info("Downloading document ID {} accessed by {}", documentId, accessedByUsername);

        Plan504Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));

        // Log access
        document.logAccess(accessedByUsername);
        documentRepository.save(document);

        // Read file from disk
        Path path = Paths.get(document.getFilePath());
        return Files.readAllBytes(path);
    }

    /**
     * Get document metadata without downloading file
     */
    public Plan504Document getDocumentMetadata(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));
    }

    // ========================================================================
    // DOCUMENT MANAGEMENT
    // ========================================================================

    /**
     * Update document metadata
     */
    @Transactional
    public Plan504Document updateMetadata(
            Long documentId,
            String description,
            DocumentType documentType,
            Boolean isConfidential,
            String updatedByUsername) {

        log.info("Updating metadata for document ID {}", documentId);

        Plan504Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));

        if (description != null) {
            document.setDescription(description);
        }
        if (documentType != null) {
            document.setDocumentType(documentType);
        }
        if (isConfidential != null) {
            document.setIsConfidential(isConfidential);
        }

        document.setUpdatedBy(updatedByUsername);
        document.setUpdatedAt(LocalDateTime.now());

        return documentRepository.save(document);
    }

    /**
     * Delete a document (and its file from storage)
     */
    @Transactional
    public void deleteDocument(Long documentId) throws IOException {
        log.info("Deleting document ID {}", documentId);

        Plan504Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));

        // Delete file from storage
        Path filePath = Paths.get(document.getFilePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("Deleted file: {}", filePath);
        }

        // Delete from database
        documentRepository.delete(document);
    }

    // ========================================================================
    // DOCUMENT SEARCH
    // ========================================================================

    /**
     * Search documents by filename or description
     */
    public List<Plan504Document> searchDocuments(String searchTerm) {
        List<Plan504Document> byFileName = documentRepository.searchByFileName(searchTerm);
        List<Plan504Document> byDescription = documentRepository.searchByDescription(searchTerm);

        // Merge and deduplicate
        byFileName.addAll(byDescription);
        return byFileName.stream().distinct().toList();
    }

    /**
     * Get documents by type across all plans
     */
    public List<Plan504Document> getDocumentsByType(DocumentType documentType) {
        Plan504 dummyPlan = new Plan504(); // Workaround for query
        return documentRepository.findByPlan504AndDocumentType(dummyPlan, documentType);
    }

    // ========================================================================
    // STATISTICS
    // ========================================================================

    /**
     * Get document statistics for a plan
     */
    public DocumentStatistics getStatistics(Long planId) {
        Plan504 plan = plan504Repository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("504 Plan not found: " + planId));

        List<Plan504Document> allDocs = documentRepository.findByPlan504OrderByUploadedAtDesc(plan);
        List<Plan504Document> currentDocs = documentRepository.findCurrentVersionsByPlan(plan);

        long totalSize = documentRepository.getTotalStorageSize(plan);
        long count = documentRepository.countByPlan504(plan);

        int confidentialCount = (int) allDocs.stream()
                .filter(Plan504Document::getIsConfidential)
                .count();

        return DocumentStatistics.builder()
                .planId(planId)
                .totalDocuments(allDocs.size())
                .currentVersionDocuments(currentDocs.size())
                .totalStorageBytes(totalSize)
                .totalStorageFormatted(formatBytes(totalSize))
                .confidentialDocuments(confidentialCount)
                .build();
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    /**
     * Generate unique filename with UUID
     */
    private String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        String baseName = originalFileName.substring(0,
                originalFileName.lastIndexOf('.'));

        return String.format("%s_%s.%s",
                baseName,
                UUID.randomUUID().toString().substring(0, 8),
                extension);
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }

    /**
     * Create file path for storage
     */
    private String createFilePath(Long planId, String fileName) {
        return String.format("%s/plan_%d/%s", storageBasePath, planId, fileName);
    }

    /**
     * Save file data to disk
     */
    private void saveFileToDisk(String filePath, byte[] fileData) throws IOException {
        Path path = Paths.get(filePath);

        // Create directories if they don't exist
        Files.createDirectories(path.getParent());

        // Write file
        Files.write(path, fileData);

        log.info("Saved file to: {}", filePath);
    }

    /**
     * Format bytes to human-readable string
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    // ========================================================================
    // DTO CLASSES
    // ========================================================================

    @Data
    @Builder
    public static class DocumentStatistics {
        private Long planId;
        private int totalDocuments;
        private int currentVersionDocuments;
        private long totalStorageBytes;
        private String totalStorageFormatted;
        private int confidentialDocuments;
    }
}
