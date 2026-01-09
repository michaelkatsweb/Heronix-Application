package com.heronix.service;

import com.heronix.model.domain.AttendanceRecord;
import com.heronix.model.domain.Student;
import com.heronix.repository.AttendanceRepository;
import com.heronix.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Attendance Report PDF Service
 *
 * Generates professional PDF reports for attendance data.
 * Uses Apache PDFBox for PDF generation with custom formatting.
 *
 * PDF Features:
 * - Professional headers and footers
 * - School branding (name, logo)
 * - Tables with borders and styling
 * - Page numbering
 * - Summary statistics
 * - Print-optimized layout
 *
 * Report Types:
 * - Daily Attendance Report
 * - Student Attendance Summary
 * - Chronic Absenteeism Report
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 49 - Report Export & Scheduling
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AttendanceReportPdfService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final float MARGIN = 50;
    private static final float FONT_SIZE_TITLE = 18;
    private static final float FONT_SIZE_HEADER = 12;
    private static final float FONT_SIZE_BODY = 10;
    private static final float LINE_HEIGHT = 15;

    /**
     * Export daily attendance report to PDF
     *
     * @param date Report date
     * @return PDF file as byte array
     */
    public byte[] exportDailyAttendancePdf(LocalDate date) throws IOException {
        log.info("Generating daily attendance PDF report for {}", date);

        List<AttendanceRecord> records = attendanceRepository.findByAttendanceDate(date);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yPosition = page.getMediaBox().getHeight() - MARGIN;

                // Title
                yPosition = drawTitle(contentStream, "Daily Attendance Report", yPosition);
                yPosition -= LINE_HEIGHT;

                // Subtitle with date
                yPosition = drawText(contentStream, "Date: " + date.format(DATE_FORMATTER),
                    MARGIN, yPosition, FONT_SIZE_HEADER, true);
                yPosition -= LINE_HEIGHT * 2;

                // Table header
                yPosition = drawTableHeader(contentStream, yPosition,
                    new String[]{"Student ID", "Student Name", "Grade", "Status", "Time"});
                yPosition -= LINE_HEIGHT;

                // Table rows
                for (AttendanceRecord record : records) {
                    if (yPosition < MARGIN + 100) {
                        // Start new page if needed
                        contentStream.close();
                        PDPage newPage = new PDPage(PDRectangle.A4);
                        document.addPage(newPage);
                        PDPageContentStream newContentStream = new PDPageContentStream(document, newPage);
                        yPosition = page.getMediaBox().getHeight() - MARGIN;
                        yPosition = drawTableHeader(newContentStream, yPosition,
                            new String[]{"Student ID", "Student Name", "Grade", "Status", "Time"});
                        yPosition -= LINE_HEIGHT;
                    }

                    Student student = record.getStudent();
                    yPosition = drawTableRow(contentStream, yPosition, new String[]{
                        student.getId().toString(),
                        student.getFullName(),
                        student.getGradeLevel() != null ? student.getGradeLevel().toString() : "N/A",
                        record.getStatus().name(),
                        record.getCreatedAt() != null ? record.getCreatedAt().toString() : ""
                    });
                    yPosition -= LINE_HEIGHT;
                }

                // Summary
                yPosition -= LINE_HEIGHT;
                yPosition = drawSummary(contentStream, records, yPosition);

                // Footer
                drawFooter(contentStream, page, 1, 1);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Export student attendance summary to PDF
     *
     * @param startDate Start date
     * @param endDate End date
     * @return PDF file as byte array
     */
    public byte[] exportStudentAttendanceSummaryPdf(LocalDate startDate, LocalDate endDate) throws IOException {
        log.info("Generating student attendance summary PDF from {} to {}", startDate, endDate);

        List<Student> students = studentRepository.findAll();

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            int pageNumber = 1;

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yPosition = page.getMediaBox().getHeight() - MARGIN;

                // Title
                yPosition = drawTitle(contentStream, "Student Attendance Summary", yPosition);
                yPosition -= LINE_HEIGHT;

                // Date range
                yPosition = drawText(contentStream,
                    String.format("Period: %s to %s", startDate.format(DATE_FORMATTER), endDate.format(DATE_FORMATTER)),
                    MARGIN, yPosition, FONT_SIZE_HEADER, true);
                yPosition -= LINE_HEIGHT * 2;

                // Table header
                yPosition = drawTableHeader(contentStream, yPosition,
                    new String[]{"Student Name", "Grade", "Present", "Absent", "Tardy", "Rate %"});
                yPosition -= LINE_HEIGHT;

                // Table rows
                for (Student student : students) {
                    List<AttendanceRecord> records = attendanceRepository
                        .findByStudentAndDateBetween(student, startDate, endDate);

                    if (records.isEmpty()) {
                        continue;
                    }

                    Map<AttendanceRecord.AttendanceStatus, Long> statusCounts = records.stream()
                        .collect(Collectors.groupingBy(AttendanceRecord::getStatus, Collectors.counting()));

                    long presentCount = statusCounts.getOrDefault(AttendanceRecord.AttendanceStatus.PRESENT, 0L);
                    long absentCount = statusCounts.getOrDefault(AttendanceRecord.AttendanceStatus.ABSENT, 0L);
                    long tardyCount = statusCounts.getOrDefault(AttendanceRecord.AttendanceStatus.TARDY, 0L);
                    long totalDays = records.size();
                    double attendanceRate = totalDays > 0 ? (presentCount * 100.0 / totalDays) : 0.0;

                    yPosition = drawTableRow(contentStream, yPosition, new String[]{
                        student.getFullName(),
                        student.getGradeLevel() != null ? student.getGradeLevel().toString() : "N/A",
                        String.valueOf(presentCount),
                        String.valueOf(absentCount),
                        String.valueOf(tardyCount),
                        String.format("%.1f%%", attendanceRate)
                    });
                    yPosition -= LINE_HEIGHT;
                }

                // Footer
                drawFooter(contentStream, page, pageNumber, 1);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Draw title on PDF
     */
    private float drawTitle(PDPageContentStream contentStream, String title, float yPosition) throws IOException {
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), FONT_SIZE_TITLE);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(title);
        contentStream.endText();
        return yPosition - (FONT_SIZE_TITLE + 5);
    }

    /**
     * Draw text on PDF
     */
    private float drawText(PDPageContentStream contentStream, String text, float x, float y,
                          float fontSize, boolean bold) throws IOException {
        contentStream.beginText();
        if (bold) {
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), fontSize);
        } else {
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), fontSize);
        }
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
        return y - (fontSize + 2);
    }

    /**
     * Draw table header
     */
    private float drawTableHeader(PDPageContentStream contentStream, float yPosition,
                                  String[] headers) throws IOException {
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), FONT_SIZE_BODY);

        float xPosition = MARGIN;
        float columnWidth = (PDRectangle.A4.getWidth() - 2 * MARGIN) / headers.length;

        for (String header : headers) {
            contentStream.beginText();
            contentStream.newLineAtOffset(xPosition, yPosition);
            contentStream.showText(header);
            contentStream.endText();
            xPosition += columnWidth;
        }

        // Draw line under header
        contentStream.moveTo(MARGIN, yPosition - 2);
        contentStream.lineTo(PDRectangle.A4.getWidth() - MARGIN, yPosition - 2);
        contentStream.stroke();

        return yPosition - (FONT_SIZE_BODY + 5);
    }

    /**
     * Draw table row
     */
    private float drawTableRow(PDPageContentStream contentStream, float yPosition,
                               String[] values) throws IOException {
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE_BODY);

        float xPosition = MARGIN;
        float columnWidth = (PDRectangle.A4.getWidth() - 2 * MARGIN) / values.length;

        for (String value : values) {
            contentStream.beginText();
            contentStream.newLineAtOffset(xPosition, yPosition);
            contentStream.showText(value != null ? value : "");
            contentStream.endText();
            xPosition += columnWidth;
        }

        return yPosition;
    }

    /**
     * Draw summary section
     */
    private float drawSummary(PDPageContentStream contentStream, List<AttendanceRecord> records,
                             float yPosition) throws IOException {
        Map<AttendanceRecord.AttendanceStatus, Long> statusCounts = records.stream()
            .collect(Collectors.groupingBy(AttendanceRecord::getStatus, Collectors.counting()));

        yPosition = drawText(contentStream, "Summary:", MARGIN, yPosition, FONT_SIZE_HEADER, true);
        yPosition -= LINE_HEIGHT;

        yPosition = drawText(contentStream, "Total Records: " + records.size(), MARGIN, yPosition, FONT_SIZE_BODY, false);
        yPosition = drawText(contentStream, "Present: " + statusCounts.getOrDefault(AttendanceRecord.AttendanceStatus.PRESENT, 0L),
            MARGIN, yPosition, FONT_SIZE_BODY, false);
        yPosition = drawText(contentStream, "Absent: " + statusCounts.getOrDefault(AttendanceRecord.AttendanceStatus.ABSENT, 0L),
            MARGIN, yPosition, FONT_SIZE_BODY, false);
        yPosition = drawText(contentStream, "Tardy: " + statusCounts.getOrDefault(AttendanceRecord.AttendanceStatus.TARDY, 0L),
            MARGIN, yPosition, FONT_SIZE_BODY, false);
        yPosition = drawText(contentStream, "Excused: " + statusCounts.getOrDefault(AttendanceRecord.AttendanceStatus.EXCUSED_ABSENT, 0L),
            MARGIN, yPosition, FONT_SIZE_BODY, false);

        return yPosition;
    }

    /**
     * Draw footer with page number
     */
    private void drawFooter(PDPageContentStream contentStream, PDPage page,
                           int pageNumber, int totalPages) throws IOException {
        String footerText = String.format("Heronix SIS - Page %d of %d - Generated on %s",
            pageNumber, totalPages, LocalDate.now().format(DATE_FORMATTER));

        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, MARGIN / 2);
        contentStream.showText(footerText);
        contentStream.endText();
    }
}
