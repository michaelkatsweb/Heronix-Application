package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 504 Plan Document Entity
 *
 * Represents documents and attachments associated with a 504 plan.
 * Supports medical documentation, parent consent forms, meeting notes, etc.
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - 504 Management Enhancement
 */
@Entity
@Table(name = "plan504_documents", indexes = {
        @Index(name = "idx_504doc_plan", columnList = "plan504_id"),
        @Index(name = "idx_504doc_type", columnList = "documentType"),
        @Index(name = "idx_504doc_uploaded", columnList = "uploadedAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Plan504Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========================================================================
    // 504 PLAN RELATIONSHIP
    // ========================================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan504_id", nullable = false)
    @ToString.Exclude
    private Plan504 plan504;

    // ========================================================================
    // DOCUMENT METADATA
    // ========================================================================

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false, length = 50)
    private String fileType; // PDF, DOC, DOCX, JPG, PNG, etc.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DocumentType documentType;

    @Column(nullable = false)
    private Long fileSizeBytes;

    @Column(length = 500)
    private String description;

    // ========================================================================
    // STORAGE
    // ========================================================================

    @Column(nullable = false, length = 500)
    private String filePath; // Path to stored file

    @Column(length = 100)
    private String storageProvider; // LOCAL, S3, AZURE, GOOGLE_CLOUD

    // ========================================================================
    // VERSION CONTROL
    // ========================================================================

    @Column
    @Builder.Default
    private Integer version = 1;

    @Column
    @Builder.Default
    private Boolean isCurrentVersion = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaces_document_id")
    @ToString.Exclude
    private Plan504Document replacesDocument; // Previous version

    // ========================================================================
    // SECURITY & PRIVACY
    // ========================================================================

    @Column
    @Builder.Default
    private Boolean isConfidential = true;

    @Column
    @Builder.Default
    private Boolean requiresParentConsent = false;

    @Column(columnDefinition = "TEXT")
    private String accessLog; // Track who accessed this document

    // ========================================================================
    // AUDIT FIELDS
    // ========================================================================

    @Column(nullable = false)
    private String uploadedBy;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @Column
    private String updatedBy;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ========================================================================
    // BUSINESS METHODS
    // ========================================================================

    /**
     * Get human-readable file size
     */
    public String getFileSizeFormatted() {
        if (fileSizeBytes == null) return "Unknown";

        long bytes = fileSizeBytes;
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    /**
     * Log document access
     */
    public void logAccess(String username) {
        String entry = String.format("[%s] Accessed by %s\n",
                LocalDateTime.now(), username);

        if (accessLog == null || accessLog.isEmpty()) {
            accessLog = entry;
        } else {
            accessLog = entry + accessLog; // Newest first
        }
    }

    // ========================================================================
    // ENUMS
    // ========================================================================

    public enum DocumentType {
        MEDICAL_DOCUMENTATION("Medical Documentation"),
        PARENT_CONSENT("Parent Consent Form"),
        MEETING_NOTES("504 Meeting Notes"),
        EVALUATION_REPORT("Evaluation Report"),
        PHYSICIAN_LETTER("Physician Letter"),
        ACCOMMODATION_REQUEST("Accommodation Request"),
        PROGRESS_REPORT("Progress Report"),
        COMPLIANCE_DOCUMENTATION("Compliance Documentation"),
        PARENT_COMMUNICATION("Parent Communication"),
        TEACHER_FEEDBACK("Teacher Feedback"),
        OTHER("Other Document");

        private final String displayName;

        DocumentType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
