package com.heronix.repository;

import com.heronix.model.domain.RegistrationDocument;
import com.heronix.model.domain.RegistrationDocument.DocumentStatus;
import com.heronix.model.domain.RegistrationDocument.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for RegistrationDocument entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Repository
public interface RegistrationDocumentRepository extends JpaRepository<RegistrationDocument, Long> {

    /**
     * Find all documents for a student
     */
    List<RegistrationDocument> findByStudentId(Long studentId);

    /**
     * Find documents by status
     */
    List<RegistrationDocument> findByStatus(DocumentStatus status);

    /**
     * Find documents by type
     */
    List<RegistrationDocument> findByDocumentType(DocumentType documentType);

    /**
     * Find documents for a student by status
     */
    List<RegistrationDocument> findByStudentIdAndStatus(Long studentId, DocumentStatus status);

    /**
     * Find missing required documents for a student
     */
    @Query("SELECT d FROM RegistrationDocument d WHERE d.student.id = :studentId " +
           "AND d.isRequired = true AND d.status NOT IN ('VERIFIED', 'EXEMPTION', 'WAIVED')")
    List<RegistrationDocument> findMissingRequiredDocuments(@Param("studentId") Long studentId);

    /**
     * Find documents pending submission (expected date passed)
     */
    @Query("SELECT d FROM RegistrationDocument d WHERE d.status = 'PENDING' " +
           "AND d.expectedDate < :now")
    List<RegistrationDocument> findOverdueDocuments(@Param("now") LocalDateTime now);

    /**
     * Find all refused documents
     */
    @Query("SELECT d FROM RegistrationDocument d WHERE d.status = 'REFUSED'")
    List<RegistrationDocument> findRefusedDocuments();

    /**
     * Check if student has all required documents
     */
    @Query("SELECT CASE WHEN COUNT(d) = 0 THEN true ELSE false END " +
           "FROM RegistrationDocument d WHERE d.student.id = :studentId " +
           "AND d.isRequired = true AND d.status NOT IN ('VERIFIED', 'EXEMPTION', 'WAIVED', 'REFUSED')")
    boolean hasAllRequiredDocuments(@Param("studentId") Long studentId);

    /**
     * Count missing documents for a student
     */
    @Query("SELECT COUNT(d) FROM RegistrationDocument d WHERE d.student.id = :studentId " +
           "AND d.isRequired = true AND d.status NOT IN ('VERIFIED', 'EXEMPTION', 'WAIVED')")
    long countMissingDocuments(@Param("studentId") Long studentId);

    /**
     * Find photo refusal record for a student
     */
    @Query("SELECT d FROM RegistrationDocument d WHERE d.student.id = :studentId " +
           "AND d.documentType = 'STUDENT_PHOTO' AND d.status = 'REFUSED'")
    java.util.Optional<RegistrationDocument> findPhotoRefusal(@Param("studentId") Long studentId);
}
