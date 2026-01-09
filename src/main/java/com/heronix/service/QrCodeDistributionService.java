package com.heronix.service;

import com.heronix.model.domain.QrCode;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.ParentGuardian;
import com.heronix.repository.QrCodeRepository;
import com.heronix.repository.StudentRepository;
import com.heronix.repository.StudentParentRelationshipRepository;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * QR Code Distribution Service
 *
 * Handles distribution of QR codes to students and parents via email, print, or download.
 * Generates QR code images, creates printable cards, and sends notifications.
 *
 * Key Responsibilities:
 * - Generate QR code images (PNG, SVG)
 * - Create printable QR code cards with student info
 * - Email QR codes to parents
 * - Generate bulk QR code sheets for printing
 * - Track distribution status
 * - Resend lost or damaged QR codes
 * - Generate QR codes for ID card printing
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Attendance Services Enhancement
 */
@Slf4j
@Service
public class QrCodeDistributionService {

    @Autowired
    private QrCodeRepository qrCodeRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentParentRelationshipRepository parentRelationshipRepository;

    @Autowired
    private QrCodeGenerationService qrCodeGenerationService;

    @Value("${qr.code.image.size:300}")
    private int qrCodeImageSize;

    @Value("${qr.code.card.template:default}")
    private String cardTemplate;

    @Value("${notification.email.enabled:true}")
    private boolean emailEnabled;

    // ========================================================================
    // EMAIL DISTRIBUTION
    // ========================================================================

    /**
     * Email QR code to student's parents
     */
    @Transactional
    public QrCodeDistributionResult emailQrCodeToParents(Long studentId) {
        log.info("Emailing QR code for student ID: {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        // Get active QR code
        QrCode qrCode = qrCodeGenerationService.getActiveQrCode(studentId);
        if (qrCode == null) {
            log.info("No active QR code found, generating new one for student {}", studentId);
            qrCode = qrCodeGenerationService.generateQrCode(studentId);
        }

        // Get parent emails
        List<ParentGuardian> parents = parentRelationshipRepository
                .findByStudentAndActiveTrue(student).stream()
                .map(relationship -> relationship.getParent())
                .collect(Collectors.toList());

        List<String> parentEmails = parents.stream()
                .map(ParentGuardian::getEmail)
                .filter(email -> email != null && !email.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        if (parentEmails.isEmpty()) {
            log.warn("No parent emails found for student {}", studentId);
            return QrCodeDistributionResult.builder()
                    .studentId(studentId)
                    .distributionMethod("EMAIL")
                    .success(false)
                    .message("No parent email addresses found")
                    .build();
        }

        // Generate QR code image
        byte[] qrImageData = generateQrCodeImage(qrCode);

        // Send emails (mock implementation - would integrate with email service)
        boolean emailSent = sendQrCodeEmails(student, qrCode, qrImageData, parentEmails);

        if (emailSent) {
            // Update distribution tracking
            updateDistributionStatus(qrCode.getId(), "EMAIL", parentEmails);
        }

        return QrCodeDistributionResult.builder()
                .studentId(studentId)
                .qrCodeId(qrCode.getId())
                .distributionMethod("EMAIL")
                .recipients(parentEmails)
                .success(emailSent)
                .sentAt(LocalDateTime.now())
                .message(String.format("QR code emailed to %d parent(s)", parentEmails.size()))
                .build();
    }

    /**
     * Batch email QR codes to all parents in a grade level
     */
    @Transactional
    public List<QrCodeDistributionResult> emailQrCodesToGradeLevel(String gradeLevel) {
        log.info("Emailing QR codes to all parents in grade: {}", gradeLevel);

        List<Student> students = studentRepository.findByGradeLevelAndActiveTrue(gradeLevel);

        return students.stream()
                .map(student -> emailQrCodeToParents(student.getId()))
                .collect(Collectors.toList());
    }

    // ========================================================================
    // PRINT DISTRIBUTION
    // ========================================================================

    /**
     * Generate printable QR code card for a student
     */
    public QrCodeCard generatePrintableCard(Long studentId) {
        log.info("Generating printable QR code card for student ID: {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        QrCode qrCode = qrCodeGenerationService.getActiveQrCode(studentId);
        if (qrCode == null) {
            qrCode = qrCodeGenerationService.generateQrCode(studentId);
        }

        byte[] qrImageData = generateQrCodeImage(qrCode);

        return QrCodeCard.builder()
                .studentId(studentId)
                .studentName(student.getFullName())
                .studentIdNumber(student.getStudentId())
                .gradeLevel(student.getGradeLevel())
                .qrCodeData(qrCode.getQrCodeData())
                .qrImageData(qrImageData)
                .expiryDate(qrCode.getExpiryDate())
                .generatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Generate batch of printable QR code cards
     */
    public List<QrCodeCard> generateBatchCards(List<Long> studentIds) {
        log.info("Generating {} printable QR code cards", studentIds.size());

        return studentIds.stream()
                .map(this::generatePrintableCard)
                .collect(Collectors.toList());
    }

    /**
     * Generate printable QR code sheet for a grade level
     */
    public QrCodeSheet generateGradeLevelSheet(String gradeLevel) {
        log.info("Generating QR code sheet for grade: {}", gradeLevel);

        List<Student> students = studentRepository.findByGradeLevelAndActiveTrue(gradeLevel);
        List<Long> studentIds = students.stream()
                .map(Student::getId)
                .collect(Collectors.toList());

        List<QrCodeCard> cards = generateBatchCards(studentIds);

        return QrCodeSheet.builder()
                .gradeLevel(gradeLevel)
                .totalCards(cards.size())
                .cards(cards)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    // ========================================================================
    // ID CARD INTEGRATION
    // ========================================================================

    /**
     * Generate QR code data for ID card printer integration
     */
    public QrCodeForIdCard generateQrCodeForIdCard(Long studentId) {
        log.info("Generating QR code data for ID card: student {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        QrCode qrCode = qrCodeGenerationService.getActiveQrCode(studentId);
        if (qrCode == null) {
            qrCode = qrCodeGenerationService.generateQrCode(studentId);
        }

        byte[] qrImageData = generateQrCodeImage(qrCode);

        return QrCodeForIdCard.builder()
                .studentId(studentId)
                .studentName(student.getFullName())
                .studentIdNumber(student.getStudentId())
                .qrCodeData(qrCode.getQrCodeData())
                .qrImageData(qrImageData)
                .imageFormat("PNG")
                .imageSize(qrCodeImageSize)
                .build();
    }

    // ========================================================================
    // DOWNLOAD AND EXPORT
    // ========================================================================

    /**
     * Generate QR code image for download
     */
    public byte[] downloadQrCodeImage(Long studentId) {
        log.info("Generating QR code image download for student {}", studentId);

        QrCode qrCode = qrCodeGenerationService.getActiveQrCode(studentId);
        if (qrCode == null) {
            throw new IllegalStateException("No active QR code found for student: " + studentId);
        }

        return generateQrCodeImage(qrCode);
    }

    /**
     * Export QR codes for multiple students as ZIP archive
     */
    public byte[] exportQrCodesAsZip(List<Long> studentIds) {
        log.info("Exporting QR codes for {} students as ZIP", studentIds.size());

        // Mock implementation - would create actual ZIP file
        // In production, would use ZipOutputStream to create archive

        return new byte[0]; // Placeholder
    }

    // ========================================================================
    // DISTRIBUTION TRACKING
    // ========================================================================

    /**
     * Update distribution status after sending QR code
     */
    @Transactional
    protected void updateDistributionStatus(Long qrCodeId, String method, List<String> recipients) {
        // This would track distribution in a QrCodeDistribution table
        // For now, just log
        log.info("QR code {} distributed via {} to {} recipients",
                qrCodeId, method, recipients.size());
    }

    /**
     * Get distribution history for a student
     */
    public List<QrCodeDistributionRecord> getDistributionHistory(Long studentId) {
        // Mock implementation - would query actual distribution tracking table
        log.info("Retrieving distribution history for student {}", studentId);

        return new ArrayList<>(); // Placeholder
    }

    /**
     * Resend QR code (for lost/damaged codes)
     */
    @Transactional
    public QrCodeDistributionResult resendQrCode(Long studentId, String method) {
        log.info("Resending QR code for student {} via {}", studentId, method);

        if ("EMAIL".equalsIgnoreCase(method)) {
            return emailQrCodeToParents(studentId);
        } else if ("PRINT".equalsIgnoreCase(method)) {
            QrCodeCard card = generatePrintableCard(studentId);
            return QrCodeDistributionResult.builder()
                    .studentId(studentId)
                    .distributionMethod("PRINT")
                    .success(true)
                    .message("Printable card generated")
                    .build();
        }

        throw new IllegalArgumentException("Unsupported distribution method: " + method);
    }

    // ========================================================================
    // QR CODE IMAGE GENERATION
    // ========================================================================

    /**
     * Generate QR code image from QR code data
     */
    private byte[] generateQrCodeImage(QrCode qrCode) {
        log.debug("Generating QR code image for: {}", qrCode.getQrCodeData());

        // Mock implementation - in production would use ZXing or similar library
        // Example using ZXing:
        // BitMatrix bitMatrix = new QRCodeWriter().encode(
        //     qrCode.getQrCodeData(),
        //     BarcodeFormat.QR_CODE,
        //     qrCodeImageSize,
        //     qrCodeImageSize
        // );
        // BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
        // ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // ImageIO.write(image, "PNG", baos);
        // return baos.toByteArray();

        // For now, return empty byte array
        return new byte[0];
    }

    // ========================================================================
    // EMAIL SENDING
    // ========================================================================

    /**
     * Send QR code emails to parent list
     */
    private boolean sendQrCodeEmails(
            Student student,
            QrCode qrCode,
            byte[] qrImageData,
            List<String> parentEmails) {

        if (!emailEnabled) {
            log.info("Email sending disabled in configuration");
            return false;
        }

        log.info("Sending QR code emails to {} parent(s) for student {}",
                parentEmails.size(), student.getStudentId());

        // Mock implementation - would integrate with actual email service
        // Example email content:
        // Subject: Student Attendance QR Code - [Student Name]
        // Body:
        //   Dear Parent/Guardian,
        //
        //   Attached is the QR code for [Student Name]'s attendance check-in.
        //   Please have your student scan this code each morning upon arrival.
        //
        //   QR Code ID: [qrCode.getQrCodeData()]
        //   Expiry Date: [qrCode.getExpiryDate()]
        //
        //   If you have any questions, please contact the school office.
        //
        //   Attachment: [Student Name]_QR_Code.png

        for (String email : parentEmails) {
            log.debug("Sending QR code email to: {}", email);
            // Would send actual email here
        }

        return true;
    }

    // ========================================================================
    // DTO CLASSES
    // ========================================================================

    @Data
    @Builder
    public static class QrCodeDistributionResult {
        private Long studentId;
        private Long qrCodeId;
        private String distributionMethod; // EMAIL, PRINT, DOWNLOAD
        private List<String> recipients;
        private boolean success;
        private LocalDateTime sentAt;
        private String message;
    }

    @Data
    @Builder
    public static class QrCodeCard {
        private Long studentId;
        private String studentName;
        private String studentIdNumber;
        private String gradeLevel;
        private String qrCodeData;
        private byte[] qrImageData;
        private java.time.LocalDate expiryDate;
        private LocalDateTime generatedAt;
    }

    @Data
    @Builder
    public static class QrCodeSheet {
        private String gradeLevel;
        private int totalCards;
        private List<QrCodeCard> cards;
        private LocalDateTime generatedAt;
    }

    @Data
    @Builder
    public static class QrCodeForIdCard {
        private Long studentId;
        private String studentName;
        private String studentIdNumber;
        private String qrCodeData;
        private byte[] qrImageData;
        private String imageFormat;
        private int imageSize;
    }

    @Data
    @Builder
    public static class QrCodeDistributionRecord {
        private Long id;
        private Long studentId;
        private Long qrCodeId;
        private String method;
        private String recipient;
        private LocalDateTime sentAt;
        private boolean delivered;
    }
}
