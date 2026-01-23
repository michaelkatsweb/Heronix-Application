package com.heronix.service;

import com.heronix.model.domain.AuditLog;
import com.heronix.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

/**
 * Service for exporting audit logs to various formats (CSV, JSON, PDF).
 * Supports filtering by date range, username, action type, and severity.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since January 20, 2026
 */
@Slf4j
@Service
public class AuditLogExportService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private AuditService auditService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * Export audit logs to CSV format
     */
    public byte[] exportToCSV(List<AuditLog> logs) {
        StringBuilder csv = new StringBuilder();

        // CSV Header
        csv.append("ID,Timestamp,Username,Action,Entity Type,Entity ID,Description,IP Address,Severity,Success,Session ID\n");

        // CSV Data
        for (AuditLog log : logs) {
            csv.append(escapeCsvField(String.valueOf(log.getId()))).append(",");
            csv.append(escapeCsvField(log.getTimestamp() != null ? log.getTimestamp().format(DATE_FORMATTER) : "")).append(",");
            csv.append(escapeCsvField(log.getUsername())).append(",");
            csv.append(escapeCsvField(log.getAction() != null ? log.getAction().name() : "")).append(",");
            csv.append(escapeCsvField(log.getEntityType())).append(",");
            csv.append(escapeCsvField(log.getEntityId() != null ? String.valueOf(log.getEntityId()) : "")).append(",");
            csv.append(escapeCsvField(log.getDescription())).append(",");
            csv.append(escapeCsvField(log.getIpAddress())).append(",");
            csv.append(escapeCsvField(log.getSeverity() != null ? log.getSeverity().name() : "")).append(",");
            csv.append(log.getSuccess() != null ? log.getSuccess() : "").append(",");
            csv.append(escapeCsvField(log.getSessionId())).append("\n");
        }

        return csv.toString().getBytes();
    }

    /**
     * Export audit logs to JSON format
     */
    public byte[] exportToJSON(List<AuditLog> logs) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Create export wrapper with metadata
        AuditLogExport export = new AuditLogExport();
        export.setExportDate(LocalDateTime.now().format(DATE_FORMATTER));
        export.setTotalRecords(logs.size());
        export.setLogs(logs);

        return mapper.writeValueAsBytes(export);
    }

    /**
     * Export audit logs to PDF format
     */
    public byte[] exportToPDF(List<AuditLog> logs, String title) throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate()); // Landscape for more columns
        PdfWriter.getInstance(document, baos);

        document.open();

        // Title
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Paragraph titlePara = new Paragraph(title != null ? title : "Audit Log Report", titleFont);
        titlePara.setAlignment(Element.ALIGN_CENTER);
        document.add(titlePara);

        // Metadata
        Font metaFont = new Font(Font.FontFamily.HELVETICA, 10);
        Paragraph metaPara = new Paragraph(
            "Generated: " + LocalDateTime.now().format(DATE_FORMATTER) +
            " | Total Records: " + logs.size(), metaFont);
        metaPara.setAlignment(Element.ALIGN_CENTER);
        metaPara.setSpacingAfter(20);
        document.add(metaPara);

        // Table
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{8, 15, 12, 12, 10, 18, 12, 8});

        // Header
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.WHITE);
        Stream.of("ID", "Timestamp", "Username", "Action", "Entity", "Description", "IP Address", "Severity")
            .forEach(columnTitle -> {
                PdfPCell header = new PdfPCell();
                header.setBackgroundColor(new BaseColor(51, 122, 183)); // Bootstrap primary blue
                header.setBorderWidth(1);
                header.setPhrase(new Phrase(columnTitle, headerFont));
                header.setHorizontalAlignment(Element.ALIGN_CENTER);
                header.setPadding(5);
                table.addCell(header);
            });

        // Data rows
        Font cellFont = new Font(Font.FontFamily.HELVETICA, 8);
        Font errorFont = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.RED);
        Font warningFont = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, new BaseColor(255, 140, 0));

        boolean alternateRow = false;
        for (AuditLog log : logs) {
            BaseColor rowColor = alternateRow ? new BaseColor(245, 245, 245) : BaseColor.WHITE;

            addCell(table, String.valueOf(log.getId()), cellFont, rowColor);
            addCell(table, log.getTimestamp() != null ? log.getTimestamp().format(DATE_FORMATTER) : "", cellFont, rowColor);
            addCell(table, log.getUsername(), cellFont, rowColor);
            addCell(table, log.getAction() != null ? log.getAction().name() : "", cellFont, rowColor);
            addCell(table, truncate(log.getEntityType() + ":" + log.getEntityId(), 15), cellFont, rowColor);
            addCell(table, truncate(log.getDescription(), 30), cellFont, rowColor);
            addCell(table, log.getIpAddress(), cellFont, rowColor);

            // Severity with color coding
            Font severityFont = cellFont;
            if (log.getSeverity() != null) {
                switch (log.getSeverity()) {
                    case ERROR:
                    case CRITICAL:
                        severityFont = errorFont;
                        break;
                    case WARNING:
                        severityFont = warningFont;
                        break;
                    default:
                        severityFont = cellFont;
                }
            }
            addCell(table, log.getSeverity() != null ? log.getSeverity().name() : "", severityFont, rowColor);

            alternateRow = !alternateRow;
        }

        document.add(table);

        // Footer
        Paragraph footer = new Paragraph(
            "\nCONFIDENTIAL - This report contains audit information protected under school data policies.",
            new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
        return baos.toByteArray();
    }

    /**
     * Export logs with filters applied
     */
    public byte[] exportWithFilters(
            LocalDateTime startDate,
            LocalDateTime endDate,
            String username,
            AuditLog.AuditAction action,
            AuditLog.AuditSeverity severity,
            ExportFormat format) throws IOException, DocumentException {

        List<AuditLog> logs = fetchFilteredLogs(startDate, endDate, username, action, severity);

        // Log the export action
        auditService.log(
            AuditLog.AuditAction.REPORT_GENERATED,
            "AuditLog",
            null,
            "Exported " + logs.size() + " audit logs to " + format.name(),
            true,
            AuditLog.AuditSeverity.INFO
        );

        switch (format) {
            case CSV:
                return exportToCSV(logs);
            case JSON:
                return exportToJSON(logs);
            case PDF:
                String title = buildPdfTitle(startDate, endDate, username, action, severity);
                return exportToPDF(logs, title);
            default:
                throw new IllegalArgumentException("Unsupported export format: " + format);
        }
    }

    /**
     * Fetch logs with various filters
     */
    private List<AuditLog> fetchFilteredLogs(
            LocalDateTime startDate,
            LocalDateTime endDate,
            String username,
            AuditLog.AuditAction action,
            AuditLog.AuditSeverity severity) {

        // Apply filters in order of specificity
        if (startDate != null && endDate != null) {
            if (username != null && !username.isEmpty()) {
                return auditLogRepository.findByUsernameAndTimestampBetween(username, startDate, endDate);
            }
            return auditLogRepository.findByTimestampBetween(startDate, endDate);
        }

        if (username != null && !username.isEmpty()) {
            return auditLogRepository.findByUsernameOrderByTimestampDesc(username);
        }

        if (action != null) {
            return auditLogRepository.findByActionOrderByTimestampDesc(action);
        }

        if (severity != null) {
            return auditLogRepository.findBySeverityOrderByTimestampDesc(severity);
        }

        // Default: last 30 days
        return auditLogRepository.findByTimestampAfterOrderByTimestampDesc(
            LocalDateTime.now().minusDays(30));
    }

    /**
     * Generate suggested filename for export
     */
    public String generateFilename(ExportFormat format) {
        String timestamp = LocalDateTime.now().format(FILE_DATE_FORMATTER);
        return "audit_log_export_" + timestamp + "." + format.getExtension();
    }

    /**
     * Get count of logs matching filters (for preview)
     */
    public long getFilteredCount(
            LocalDateTime startDate,
            LocalDateTime endDate,
            String username,
            AuditLog.AuditAction action,
            AuditLog.AuditSeverity severity) {
        return fetchFilteredLogs(startDate, endDate, username, action, severity).size();
    }

    // Helper methods

    private String escapeCsvField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    private void addCell(PdfPTable table, String text, Font font, BaseColor bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(4);
        cell.setBorderWidth(0.5f);
        table.addCell(cell);
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength - 3) + "..." : text;
    }

    private String buildPdfTitle(LocalDateTime startDate, LocalDateTime endDate,
                                  String username, AuditLog.AuditAction action,
                                  AuditLog.AuditSeverity severity) {
        StringBuilder title = new StringBuilder("Audit Log Report");

        if (startDate != null && endDate != null) {
            title.append(" (").append(startDate.toLocalDate()).append(" to ").append(endDate.toLocalDate()).append(")");
        }
        if (username != null && !username.isEmpty()) {
            title.append(" - User: ").append(username);
        }
        if (action != null) {
            title.append(" - Action: ").append(action.name());
        }
        if (severity != null) {
            title.append(" - Severity: ").append(severity.name());
        }

        return title.toString();
    }

    /**
     * Export format enum
     */
    public enum ExportFormat {
        CSV("csv"),
        JSON("json"),
        PDF("pdf");

        private final String extension;

        ExportFormat(String extension) {
            this.extension = extension;
        }

        public String getExtension() {
            return extension;
        }
    }

    /**
     * Export wrapper class for JSON
     */
    public static class AuditLogExport {
        private String exportDate;
        private int totalRecords;
        private List<AuditLog> logs;

        public String getExportDate() { return exportDate; }
        public void setExportDate(String exportDate) { this.exportDate = exportDate; }
        public int getTotalRecords() { return totalRecords; }
        public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
        public List<AuditLog> getLogs() { return logs; }
        public void setLogs(List<AuditLog> logs) { this.logs = logs; }
    }
}
