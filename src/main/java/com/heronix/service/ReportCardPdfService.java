package com.heronix.service;

import com.heronix.model.DistrictSettings;
import com.heronix.model.domain.Student;
import com.heronix.repository.StudentRepository;
import com.heronix.service.impl.AttendanceService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.Data;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for generating student report card PDFs using iText.
 * Follows the same patterns as TranscriptPdfService.
 *
 * @author Heronix Development Team
 * @version 1.0.0
 * @since Phase 17 - Report Card PDF Generation
 */
@Slf4j
@Service
public class ReportCardPdfService {

    @Autowired
    private GradebookService gradebookService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private DistrictSettingsService districtSettingsService;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private GradeService gradeService;

    // Fonts
    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font SUBTITLE_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, new BaseColor(103, 58, 183));
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 11);
    private static final Font LABEL_FONT = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);
    private static final Font VALUE_FONT = new Font(Font.FontFamily.HELVETICA, 9);
    private static final Font TABLE_HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, BaseColor.WHITE);
    private static final Font CELL_FONT = new Font(Font.FontFamily.HELVETICA, 8);
    private static final Font CELL_BOLD_FONT = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD);
    private static final Font SUMMARY_LABEL_FONT = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);
    private static final Font SUMMARY_VALUE_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, new BaseColor(103, 58, 183));
    private static final Font FOOTER_FONT = new Font(Font.FontFamily.HELVETICA, 7, Font.ITALIC, BaseColor.GRAY);
    private static final Font SECTION_TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, new BaseColor(74, 20, 140));
    private static final Font MESSAGE_FONT = new Font(Font.FontFamily.HELVETICA, 9);
    private static final Font SIGNATURE_FONT = new Font(Font.FontFamily.HELVETICA, 8);
    private static final Font WATERMARK_FONT = new Font(Font.FontFamily.HELVETICA, 52, Font.BOLD, new BaseColor(244, 67, 54, 30));

    // Colors
    private static final BaseColor PRIMARY_COLOR = new BaseColor(103, 58, 183);
    private static final BaseColor DARK_PRIMARY = new BaseColor(74, 20, 140);
    private static final BaseColor ALT_ROW_COLOR = new BaseColor(237, 231, 246);
    private static final BaseColor GRADE_A_COLOR = new BaseColor(76, 175, 80);
    private static final BaseColor GRADE_B_COLOR = new BaseColor(139, 195, 74);
    private static final BaseColor GRADE_C_COLOR = new BaseColor(255, 152, 0);
    private static final BaseColor GRADE_D_COLOR = new BaseColor(255, 87, 34);
    private static final BaseColor GRADE_F_COLOR = new BaseColor(244, 67, 54);

    // ========================================================================
    // PUBLIC API
    // ========================================================================

    /**
     * Generate a report card PDF from a pre-built ReportCard object.
     */
    @Transactional(readOnly = true)
    public byte[] generateReportCardPdf(GradebookService.ReportCard reportCard, ReportCardOptions options) {
        log.info("Generating report card PDF for student {}", reportCard.getStudentId());

        Student student = studentRepository.findById(reportCard.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + reportCard.getStudentId()));

        DistrictSettings settings = districtSettingsService.getOrCreateDistrictSettings();

        // Attendance data
        AttendanceService.AttendanceSummary attendance = null;
        if (options.isShowAttendance()) {
            try {
                attendance = attendanceService.getStudentAttendanceSummary(
                        reportCard.getStudentId(),
                        LocalDate.now().withDayOfYear(1),
                        LocalDate.now());
            } catch (Exception e) {
                log.warn("Could not load attendance for student {}: {}", reportCard.getStudentId(), e.getMessage());
            }
        }

        // Class rank
        int classRank = 0;
        if (options.isShowClassRank()) {
            try {
                classRank = gradeService.calculateClassRank(student);
            } catch (Exception e) {
                log.warn("Could not calculate class rank for student {}: {}", reportCard.getStudentId(), e.getMessage());
            }
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.LETTER, 36, 36, 36, 36);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            if (options.isDraft()) {
                addWatermark(writer, document);
            }

            addSchoolHeader(document, settings, options);
            addReportTitle(document, reportCard);
            addStudentInfoSection(document, reportCard, student, classRank, options);
            addCourseGradesTable(document, reportCard.getCourseGrades(), options);
            addGpaSummary(document, reportCard, classRank, options);

            if (options.isShowAttendance() && attendance != null) {
                addAttendanceSummary(document, attendance);
            }

            if (options.getPrincipalMessage() != null && !options.getPrincipalMessage().trim().isEmpty()) {
                addPrincipalMessage(document, options.getPrincipalMessage());
            }

            if (options.isShowSignatures()) {
                addSignatures(document);
            }

            addFooter(document, options.isDraft());

            document.close();
            log.info("Successfully generated report card PDF for student {}", reportCard.getStudentId());
            return baos.toByteArray();

        } catch (DocumentException e) {
            log.error("Error generating report card PDF for student {}: {}", reportCard.getStudentId(), e.getMessage(), e);
            throw new RuntimeException("Failed to generate report card PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Generate a report card PDF by student ID and term ID.
     */
    @Transactional(readOnly = true)
    public byte[] generateReportCardPdf(Long studentId, Long termId, ReportCardOptions options) {
        GradebookService.ReportCard reportCard = gradebookService.generateReportCard(studentId, termId);
        return generateReportCardPdf(reportCard, options);
    }

    // ========================================================================
    // PDF LAYOUT SECTIONS
    // ========================================================================

    private void addSchoolHeader(Document document, DistrictSettings settings, ReportCardOptions options) throws DocumentException {
        // Logo
        if (options.isShowSchoolLogo() && settings.getLogoPath() != null && !settings.getLogoPath().trim().isEmpty()) {
            try {
                Path logoPath = Paths.get(settings.getLogoPath());
                if (Files.exists(logoPath)) {
                    Image logo = Image.getInstance(logoPath.toAbsolutePath().toString());
                    logo.scaleToFit(80, 80);
                    logo.setAlignment(Element.ALIGN_CENTER);
                    document.add(logo);
                    document.add(new Paragraph(" ", new Font(Font.FontFamily.HELVETICA, 4)));
                }
            } catch (IOException | BadElementException e) {
                log.warn("Could not load school logo from {}: {}", settings.getLogoPath(), e.getMessage());
            }
        }

        // District/School name
        Paragraph schoolName = new Paragraph(settings.getDistrictNameOrDefault(), TITLE_FONT);
        schoolName.setAlignment(Element.ALIGN_CENTER);
        document.add(schoolName);

        // Campus name if different
        String campus = settings.getCampusNameOrDefault();
        if (campus != null && !campus.equals("Main Campus")) {
            Paragraph campusP = new Paragraph(campus, HEADER_FONT);
            campusP.setAlignment(Element.ALIGN_CENTER);
            document.add(campusP);
        }

        // Address
        if (settings.getDistrictAddress() != null && !settings.getDistrictAddress().trim().isEmpty()) {
            Paragraph address = new Paragraph(settings.getDistrictAddress(), new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.GRAY));
            address.setAlignment(Element.ALIGN_CENTER);
            document.add(address);
        }

        document.add(new Paragraph(" ", new Font(Font.FontFamily.HELVETICA, 4)));
    }

    private void addReportTitle(Document document, GradebookService.ReportCard reportCard) throws DocumentException {
        Paragraph title = new Paragraph("OFFICIAL REPORT CARD", SUBTITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        String periodInfo = reportCard.getTermName() != null ? reportCard.getTermName() : "Current Term";
        Paragraph periodP = new Paragraph(periodInfo, HEADER_FONT);
        periodP.setAlignment(Element.ALIGN_CENTER);
        document.add(periodP);

        // Divider
        addDivider(document);
    }

    private void addStudentInfoSection(Document document, GradebookService.ReportCard reportCard,
                                        Student student, int classRank, ReportCardOptions options) throws DocumentException {
        PdfPTable infoTable = new PdfPTable(4);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{15, 35, 15, 35});
        infoTable.setSpacingAfter(10);

        addInfoCell(infoTable, "Name:", LABEL_FONT);
        addInfoCell(infoTable, reportCard.getStudentName(), VALUE_FONT);
        addInfoCell(infoTable, "Student ID:", LABEL_FONT);
        addInfoCell(infoTable, reportCard.getStudentNumber() != null ? reportCard.getStudentNumber() : "-", VALUE_FONT);

        addInfoCell(infoTable, "Grade Level:", LABEL_FONT);
        addInfoCell(infoTable, reportCard.getGradeLevel() != null ? "Grade " + reportCard.getGradeLevel() : "-", VALUE_FONT);
        addInfoCell(infoTable, "Date of Birth:", LABEL_FONT);
        String dob = student.getDateOfBirth() != null
                ? student.getDateOfBirth().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
                : "-";
        addInfoCell(infoTable, dob, VALUE_FONT);

        if (options.isShowGPA()) {
            addInfoCell(infoTable, "Term GPA:", LABEL_FONT);
            addInfoCell(infoTable, reportCard.getTermGPA() != null ? String.format("%.3f", reportCard.getTermGPA()) : "-", VALUE_FONT);
            addInfoCell(infoTable, "Cumulative GPA:", LABEL_FONT);
            addInfoCell(infoTable, reportCard.getCumulativeGPA() != null ? String.format("%.3f", reportCard.getCumulativeGPA()) : "-", VALUE_FONT);
        }

        if (options.isShowClassRank() && classRank > 0) {
            addInfoCell(infoTable, "Class Rank:", LABEL_FONT);
            addInfoCell(infoTable, String.valueOf(classRank), VALUE_FONT);
            addInfoCell(infoTable, "Academic Standing:", LABEL_FONT);
            addInfoCell(infoTable, reportCard.getAcademicStanding() != null ? reportCard.getAcademicStanding() : "-", VALUE_FONT);
        }

        if (reportCard.getHonorRoll() != null) {
            addInfoCell(infoTable, "Honor Roll:", LABEL_FONT);
            addInfoCell(infoTable, formatHonorRoll(reportCard.getHonorRoll()), VALUE_FONT);
            addInfoCell(infoTable, "", LABEL_FONT);
            addInfoCell(infoTable, "", VALUE_FONT);
        }

        document.add(infoTable);
    }

    private void addCourseGradesTable(Document document, List<GradebookService.ReportCardEntry> courseGrades,
                                       ReportCardOptions options) throws DocumentException {
        // Section title
        addSectionTitle(document, "Course Grades");

        // Determine columns based on options
        boolean showComments = options.isShowTeacherComments();
        int numCols = showComments ? 8 : 7;
        PdfPTable table;
        if (showComments) {
            table = new PdfPTable(8);
            table.setWidths(new float[]{10, 22, 8, 10, 8, 8, 12, 22});
        } else {
            table = new PdfPTable(7);
            table.setWidths(new float[]{12, 28, 10, 12, 10, 12, 16});
        }
        table.setWidthPercentage(100);

        // Headers
        addTableHeaderCell(table, "Code");
        addTableHeaderCell(table, "Course Name");
        addTableHeaderCell(table, "Grade");
        addTableHeaderCell(table, "Percentage");
        addTableHeaderCell(table, "GPA Pts");
        addTableHeaderCell(table, "Credits");
        addTableHeaderCell(table, "Teacher");
        if (showComments) {
            addTableHeaderCell(table, "Comments");
        }

        if (courseGrades == null || courseGrades.isEmpty()) {
            PdfPCell emptyCell = new PdfPCell(new Phrase("No course grades available for this period", CELL_FONT));
            emptyCell.setColspan(numCols);
            emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            emptyCell.setPadding(15);
            table.addCell(emptyCell);
        } else {
            boolean alternate = false;
            for (GradebookService.ReportCardEntry entry : courseGrades) {
                BaseColor rowColor = alternate ? ALT_ROW_COLOR : BaseColor.WHITE;

                // Course code
                addDataCell(table, entry.getCourseCode() != null ? entry.getCourseCode() : "-", CELL_FONT, rowColor);

                // Course name
                addDataCell(table, entry.getCourseName() != null ? entry.getCourseName() : "-", CELL_FONT, rowColor);

                // Grade with color coding
                addGradeCell(table, entry.getLetterGrade(), options.getGradeFormat(), entry.getFinalGrade(), rowColor);

                // Percentage
                String pct = entry.getFinalGrade() != null ? String.format("%.1f%%", entry.getFinalGrade()) : "-";
                addDataCell(table, pct, CELL_FONT, rowColor);

                // GPA points
                String gpa = entry.getGpaPoints() != null ? String.format("%.2f", entry.getGpaPoints()) : "-";
                addDataCell(table, gpa, CELL_FONT, rowColor);

                // Credits
                String credits = entry.getCredits() != null ? String.format("%.1f", entry.getCredits()) : "-";
                addDataCell(table, credits, CELL_FONT, rowColor);

                // Teacher
                addDataCell(table, entry.getTeacher() != null ? entry.getTeacher() : "-", CELL_FONT, rowColor);

                // Comments
                if (showComments) {
                    addDataCell(table, entry.getComments() != null ? entry.getComments() : "", CELL_FONT, rowColor);
                }

                alternate = !alternate;
            }
        }

        document.add(table);
    }

    private void addGpaSummary(Document document, GradebookService.ReportCard reportCard,
                                int classRank, ReportCardOptions options) throws DocumentException {
        addDivider(document);

        PdfPTable summaryTable = new PdfPTable(4);
        summaryTable.setWidthPercentage(100);
        summaryTable.setWidths(new float[]{25, 25, 25, 25});

        if (options.isShowGPA()) {
            addSummaryBox(summaryTable, "Term GPA",
                    reportCard.getTermGPA() != null ? String.format("%.3f", reportCard.getTermGPA()) : "N/A");
            addSummaryBox(summaryTable, "Cumulative GPA",
                    reportCard.getCumulativeGPA() != null ? String.format("%.3f", reportCard.getCumulativeGPA()) : "N/A");
        } else {
            addSummaryBox(summaryTable, "", "");
            addSummaryBox(summaryTable, "", "");
        }

        addSummaryBox(summaryTable, "Credits Earned",
                reportCard.getCreditsEarned() != null ? String.format("%.1f", reportCard.getCreditsEarned()) : "0");

        if (options.isShowClassRank() && classRank > 0) {
            addSummaryBox(summaryTable, "Class Rank", String.valueOf(classRank));
        } else {
            addSummaryBox(summaryTable, "", "");
        }

        document.add(summaryTable);
    }

    private void addAttendanceSummary(Document document, AttendanceService.AttendanceSummary attendance) throws DocumentException {
        addSectionTitle(document, "Attendance Summary");

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{25, 25, 25, 25});

        addSummaryBox(table, "Days Present", String.valueOf(attendance.getDaysPresent()));
        addSummaryBox(table, "Days Absent", String.valueOf(attendance.getDaysAbsent()));
        addSummaryBox(table, "Tardies", String.valueOf(attendance.getDaysTardy()));
        double rate = attendance.getTotalDays() > 0
                ? (double) attendance.getDaysPresent() / attendance.getTotalDays() * 100
                : 0;
        addSummaryBox(table, "Attendance Rate", String.format("%.1f%%", rate));

        document.add(table);
    }

    private void addPrincipalMessage(Document document, String message) throws DocumentException {
        addSectionTitle(document, "Principal's Message");

        PdfPTable msgTable = new PdfPTable(1);
        msgTable.setWidthPercentage(100);
        PdfPCell msgCell = new PdfPCell(new Phrase(message, MESSAGE_FONT));
        msgCell.setPadding(10);
        msgCell.setBackgroundColor(new BaseColor(249, 249, 249));
        msgCell.setBorderWidth(0.5f);
        msgCell.setBorderColor(new BaseColor(224, 224, 224));
        msgTable.addCell(msgCell);
        document.add(msgTable);
    }

    private void addSignatures(Document document) throws DocumentException {
        document.add(new Paragraph(" ", new Font(Font.FontFamily.HELVETICA, 15)));

        PdfPTable sigTable = new PdfPTable(4);
        sigTable.setWidthPercentage(90);
        sigTable.setWidths(new float[]{35, 15, 35, 15});

        // Date issued
        PdfPTable dateTable = new PdfPTable(1);
        dateTable.setWidthPercentage(100);
        PdfPCell dateLabel = new PdfPCell(new Phrase("Date Issued: " +
                LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")), LABEL_FONT));
        dateLabel.setBorder(Rectangle.NO_BORDER);
        dateLabel.setPaddingBottom(20);
        dateLabel.setColspan(4);
        dateTable.addCell(dateLabel);

        // Principal signature line
        PdfPCell sigLine1 = new PdfPCell(new Phrase("", SIGNATURE_FONT));
        sigLine1.setBorder(Rectangle.NO_BORDER);
        sigLine1.setBorderWidthBottom(1);
        sigLine1.setPaddingBottom(15);
        sigTable.addCell(sigLine1);

        PdfPCell spacer1 = new PdfPCell(new Phrase(""));
        spacer1.setBorder(Rectangle.NO_BORDER);
        sigTable.addCell(spacer1);

        // Counselor signature line
        PdfPCell sigLine2 = new PdfPCell(new Phrase("", SIGNATURE_FONT));
        sigLine2.setBorder(Rectangle.NO_BORDER);
        sigLine2.setBorderWidthBottom(1);
        sigLine2.setPaddingBottom(15);
        sigTable.addCell(sigLine2);

        PdfPCell spacer2 = new PdfPCell(new Phrase(""));
        spacer2.setBorder(Rectangle.NO_BORDER);
        sigTable.addCell(spacer2);

        // Labels under lines
        PdfPCell label1 = new PdfPCell(new Phrase("Principal Signature", SIGNATURE_FONT));
        label1.setBorder(Rectangle.NO_BORDER);
        label1.setPaddingTop(3);
        sigTable.addCell(label1);

        PdfPCell spacer3 = new PdfPCell(new Phrase(""));
        spacer3.setBorder(Rectangle.NO_BORDER);
        sigTable.addCell(spacer3);

        PdfPCell label2 = new PdfPCell(new Phrase("Counselor Signature", SIGNATURE_FONT));
        label2.setBorder(Rectangle.NO_BORDER);
        label2.setPaddingTop(3);
        sigTable.addCell(label2);

        PdfPCell spacer4 = new PdfPCell(new Phrase(""));
        spacer4.setBorder(Rectangle.NO_BORDER);
        sigTable.addCell(spacer4);

        document.add(dateTable);
        document.add(sigTable);
    }

    private void addFooter(Document document, boolean isDraft) throws DocumentException {
        document.add(new Paragraph(" ", new Font(Font.FontFamily.HELVETICA, 6)));

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a"));
        String disclaimerText = isDraft
                ? "DRAFT — Generated on " + timestamp + " | This is a draft document and is not official."
                : "Generated on " + timestamp + " | This is an unofficial document. Official report cards must be requested through the registrar's office.";

        Paragraph footer = new Paragraph(disclaimerText, FOOTER_FONT);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    private void addWatermark(PdfWriter writer, Document document) {
        PdfContentByte canvas = writer.getDirectContentUnder();
        canvas.saveState();
        PdfGState gs = new PdfGState();
        gs.setFillOpacity(0.1f);
        canvas.setGState(gs);

        try {
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, false);
            canvas.beginText();
            canvas.setFontAndSize(bf, 80);
            canvas.setColorFill(new BaseColor(244, 67, 54));
            canvas.showTextAligned(Element.ALIGN_CENTER, "DRAFT",
                    document.getPageSize().getWidth() / 2,
                    document.getPageSize().getHeight() / 2,
                    45);
            canvas.endText();
        } catch (DocumentException | IOException e) {
            log.warn("Could not add watermark: {}", e.getMessage());
        }

        canvas.restoreState();
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private void addDivider(Document document) throws DocumentException {
        PdfPTable divider = new PdfPTable(1);
        divider.setWidthPercentage(100);
        divider.setSpacingBefore(8);
        divider.setSpacingAfter(8);
        PdfPCell dividerCell = new PdfPCell();
        dividerCell.setBorderWidthTop(0);
        dividerCell.setBorderWidthLeft(0);
        dividerCell.setBorderWidthRight(0);
        dividerCell.setBorderWidthBottom(1.5f);
        dividerCell.setBorderColorBottom(PRIMARY_COLOR);
        dividerCell.setFixedHeight(1);
        divider.addCell(dividerCell);
        document.add(divider);
    }

    private void addSectionTitle(Document document, String title) throws DocumentException {
        PdfPTable sectionHeader = new PdfPTable(1);
        sectionHeader.setWidthPercentage(100);
        sectionHeader.setSpacingBefore(10);
        sectionHeader.setSpacingAfter(4);

        PdfPCell cell = new PdfPCell(new Phrase(title, SECTION_TITLE_FONT));
        cell.setBackgroundColor(new BaseColor(237, 231, 246));
        cell.setPadding(6);
        cell.setBorderWidthLeft(4);
        cell.setBorderColorLeft(PRIMARY_COLOR);
        cell.setBorderWidthTop(0);
        cell.setBorderWidthRight(0);
        cell.setBorderWidthBottom(0);
        sectionHeader.addCell(cell);
        document.add(sectionHeader);
    }

    private void addInfoCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3);
        table.addCell(cell);
    }

    private void addTableHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, TABLE_HEADER_FONT));
        cell.setBackgroundColor(PRIMARY_COLOR);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        cell.setBorderWidth(0.5f);
        table.addCell(cell);
    }

    private void addDataCell(PdfPTable table, String text, Font font, BaseColor bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(4);
        cell.setBorderWidth(0.5f);
        table.addCell(cell);
    }

    private void addGradeCell(PdfPTable table, String letterGrade, GradeFormat format,
                               Double percentage, BaseColor rowColor) {
        String displayText;
        if (format == GradeFormat.PERCENTAGE && percentage != null) {
            displayText = String.format("%.1f%%", percentage);
        } else if (format == GradeFormat.BOTH && letterGrade != null && percentage != null) {
            displayText = letterGrade + " (" + String.format("%.0f%%", percentage) + ")";
        } else {
            displayText = letterGrade != null ? letterGrade : "-";
        }

        BaseColor gradeColor = getGradeColor(letterGrade);
        Font gradeFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, gradeColor);

        PdfPCell cell = new PdfPCell(new Phrase(displayText, gradeFont));
        cell.setBackgroundColor(rowColor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(4);
        cell.setBorderWidth(0.5f);
        table.addCell(cell);
    }

    private BaseColor getGradeColor(String letterGrade) {
        if (letterGrade == null || letterGrade.isEmpty()) return BaseColor.BLACK;
        char first = letterGrade.charAt(0);
        return switch (first) {
            case 'A' -> GRADE_A_COLOR;
            case 'B' -> GRADE_B_COLOR;
            case 'C' -> GRADE_C_COLOR;
            case 'D' -> GRADE_D_COLOR;
            case 'F' -> GRADE_F_COLOR;
            default -> BaseColor.BLACK;
        };
    }

    private void addSummaryBox(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorderWidth(0.5f);
        cell.setBorderColor(new BaseColor(200, 200, 200));
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        if (label != null && !label.isEmpty()) {
            Paragraph labelP = new Paragraph(label, SUMMARY_LABEL_FONT);
            labelP.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(labelP);

            Paragraph valueP = new Paragraph(value != null ? value : "", SUMMARY_VALUE_FONT);
            valueP.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(valueP);
        } else {
            cell.setPhrase(new Phrase(""));
            cell.setBorderWidth(0);
        }

        table.addCell(cell);
    }

    private String formatHonorRoll(String honorRoll) {
        if (honorRoll == null) return "";
        return switch (honorRoll) {
            case "HIGH_HONORS" -> "High Honors";
            case "HONORS" -> "Honors";
            case "MERIT" -> "Merit";
            default -> honorRoll;
        };
    }

    // ========================================================================
    // OPTIONS CLASS
    // ========================================================================

    public enum GradeFormat {
        LETTER, PERCENTAGE, BOTH
    }

    @Data
    @Builder
    public static class ReportCardOptions {
        @Builder.Default private boolean showGPA = true;
        @Builder.Default private boolean showClassRank = false;
        @Builder.Default private boolean showAttendance = true;
        @Builder.Default private boolean showBehavior = true;
        @Builder.Default private boolean showTeacherComments = true;
        @Builder.Default private boolean showSchoolLogo = true;
        @Builder.Default private boolean showSignatures = true;
        @Builder.Default private GradeFormat gradeFormat = GradeFormat.LETTER;
        private String principalMessage;
        @Builder.Default private boolean draft = false;
    }
}
