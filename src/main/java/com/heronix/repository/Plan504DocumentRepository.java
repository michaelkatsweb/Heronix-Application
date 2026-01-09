package com.heronix.repository;

import com.heronix.model.domain.Plan504;
import com.heronix.model.domain.Plan504Document;
import com.heronix.model.domain.Plan504Document.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for 504 Plan Documents
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - 504 Management Enhancement
 */
@Repository
public interface Plan504DocumentRepository extends JpaRepository<Plan504Document, Long> {

    // ========================================================================
    // BASIC QUERIES
    // ========================================================================

    /**
     * Find all documents for a 504 plan
     */
    List<Plan504Document> findByPlan504OrderByUploadedAtDesc(Plan504 plan504);

    /**
     * Find documents by plan ID
     */
    @Query("SELECT d FROM Plan504Document d WHERE d.plan504.id = :planId ORDER BY d.uploadedAt DESC")
    List<Plan504Document> findByPlan504Id(@Param("planId") Long planId);

    /**
     * Find current version documents for a plan
     */
    @Query("SELECT d FROM Plan504Document d WHERE d.plan504 = :plan AND d.isCurrentVersion = true ORDER BY d.uploadedAt DESC")
    List<Plan504Document> findCurrentVersionsByPlan(@Param("plan") Plan504 plan);

    /**
     * Find documents by type for a plan
     */
    List<Plan504Document> findByPlan504AndDocumentType(Plan504 plan504, DocumentType documentType);

    // ========================================================================
    // VERSION QUERIES
    // ========================================================================

    /**
     * Find all versions of a document
     */
    @Query("SELECT d FROM Plan504Document d WHERE d.id = :documentId OR d.replacesDocument.id = :documentId ORDER BY d.version DESC")
    List<Plan504Document> findAllVersions(@Param("documentId") Long documentId);

    /**
     * Find latest version of a document
     */
    @Query("SELECT d FROM Plan504Document d WHERE (d.id = :documentId OR d.replacesDocument.id = :documentId) AND d.isCurrentVersion = true")
    Optional<Plan504Document> findLatestVersion(@Param("documentId") Long documentId);

    // ========================================================================
    // TYPE AND STATUS QUERIES
    // ========================================================================

    /**
     * Find confidential documents
     */
    @Query("SELECT d FROM Plan504Document d WHERE d.isConfidential = true")
    List<Plan504Document> findConfidentialDocuments();

    /**
     * Find documents requiring parent consent
     */
    @Query("SELECT d FROM Plan504Document d WHERE d.requiresParentConsent = true")
    List<Plan504Document> findRequiringParentConsent();

    /**
     * Find documents by file type
     */
    List<Plan504Document> findByFileType(String fileType);

    // ========================================================================
    // SEARCH QUERIES
    // ========================================================================

    /**
     * Search documents by filename
     */
    @Query("SELECT d FROM Plan504Document d WHERE LOWER(d.fileName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Plan504Document> searchByFileName(@Param("searchTerm") String searchTerm);

    /**
     * Search documents by description
     */
    @Query("SELECT d FROM Plan504Document d WHERE LOWER(d.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Plan504Document> searchByDescription(@Param("searchTerm") String searchTerm);

    // ========================================================================
    // DATE QUERIES
    // ========================================================================

    /**
     * Find documents uploaded within date range
     */
    @Query("SELECT d FROM Plan504Document d WHERE d.uploadedAt BETWEEN :startDate AND :endDate ORDER BY d.uploadedAt DESC")
    List<Plan504Document> findUploadedBetween(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    /**
     * Find recently uploaded documents
     */
    @Query("SELECT d FROM Plan504Document d WHERE d.uploadedAt >= :sinceDate ORDER BY d.uploadedAt DESC")
    List<Plan504Document> findRecentlyUploaded(@Param("sinceDate") LocalDateTime sinceDate);

    // ========================================================================
    // STATISTICS QUERIES
    // ========================================================================

    /**
     * Count documents for a plan
     */
    long countByPlan504(Plan504 plan504);

    /**
     * Count documents by type
     */
    long countByDocumentType(DocumentType documentType);

    /**
     * Get total storage size for a plan
     */
    @Query("SELECT COALESCE(SUM(d.fileSizeBytes), 0) FROM Plan504Document d WHERE d.plan504 = :plan")
    long getTotalStorageSize(@Param("plan") Plan504 plan);

    // ========================================================================
    // USER QUERIES
    // ========================================================================

    /**
     * Find documents uploaded by a user
     */
    List<Plan504Document> findByUploadedByOrderByUploadedAtDesc(String username);

    /**
     * Find documents updated by a user
     */
    List<Plan504Document> findByUpdatedByOrderByUpdatedAtDesc(String username);
}
