package com.heronix.service.export;

import com.heronix.model.domain.HallPassSession;
import com.heronix.model.domain.HallPassSession.SessionStatus;
import com.heronix.model.domain.Student;
import com.heronix.repository.HallPassSessionRepository;
import com.heronix.repository.StudentRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Hall Pass Report Export Service
 *
 * Generates PDF and Excel reports for hall pass data:
 * - Student individual reports
 * - School-wide analytics reports
 * - District/State aggregation reports
 * - Parent summary reports
 *
 * @author Heronix Team
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class HallPassReportService {

    private final HallPassSessionRepository hallPassRepository;
    private final StudentRepository studentRepository;

    // PDF Font definitions
    private static final com.itextpdf.text.Font TITLE_FONT =
            new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
    private static final com.itextpdf.text.Font SUBTITLE_FONT =
            new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD);
    private static final com.itextpdf.text.Font HEADER_FONT =
            new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.BOLD);
    private static final com.itextpdf.text.Font NORMAL_FONT =
            new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.NORMAL);
    private static final com.itextpdf.text.Font SMALL_FONT =
            new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.NORMAL);
    private static final com.itextpdf.text.Font GRADE_FONT =
            new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 36, com.itextpdf.text.Font.BOLD);

    // ==================== Student Report PDF ====================

    /**
     * Generate student hall pass report PDF
     */
    public ByteArrayOutputStream generateStudentReportPdf(Long studentId, LocalDate startDate, LocalDate endDate)
            throws DocumentException {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<HallPassSession> sessions = hallPassRepository.findByStudent_IdOrderByDepartureTimeDesc(studentId)
                .stream()
                .filter(s -> s.getDepartureTime() != null &&
                        !s.getDepartureTime().isBefore(start) &&
                        !s.getDepartureTime().isAfter(end))
                .collect(Collectors.toList());

        log.info("Generating hall pass PDF report for student {} with {} sessions", studentId, sessions.size());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.LETTER, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Title
            Paragraph title = new Paragraph("Hall Pass Activity Report", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Student info
            Paragraph studentInfo = new Paragraph();
            studentInfo.setSpacingBefore(20);
            studentInfo.add(new Chunk("Student: ", HEADER_FONT));
            studentInfo.add(new Chunk(student.getFirstName() + " " + student.getLastName(), NORMAL_FONT));
            studentInfo.add(Chunk.NEWLINE);
            studentInfo.add(new Chunk("Grade: ", HEADER_FONT));
            studentInfo.add(new Chunk(student.getGradeLevel() != null ? student.getGradeLevel().toString() : "N/A", NORMAL_FONT));
            studentInfo.add(Chunk.NEWLINE);
            studentInfo.add(new Chunk("Report Period: ", HEADER_FONT));
            studentInfo.add(new Chunk(formatDateRange(startDate, endDate), NORMAL_FONT));
            document.add(studentInfo);

            // Summary statistics
            addSummarySection(document, sessions);

            // Report card grade
            addReportCardGrade(document, sessions);

            // Destination breakdown
            addDestinationBreakdown(document, sessions);

            // Detailed session list
            addSessionDetails(document, sessions);

            // Footer
            addFooter(document);

            log.info("Student hall pass PDF generated successfully");

        } finally {
            document.close();
        }

        return outputStream;
    }

    /**
     * Generate parent summary PDF
     */
    public ByteArrayOutputStream generateParentSummaryPdf(Long studentId) throws DocumentException {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<HallPassSession> sessions = hallPassRepository.findByStudent_IdOrderByDepartureTimeDesc(studentId)
                .stream()
                .filter(s -> s.getDepartureTime() != null && s.getDepartureTime().isAfter(thirtyDaysAgo))
                .collect(Collectors.toList());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.LETTER, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Header with school branding
            Paragraph header = new Paragraph("HERONIX SCHOOL DISTRICT", SUBTITLE_FONT);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Paragraph title = new Paragraph("Hall Pass Report Card", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(10);
            title.setSpacingAfter(20);
            document.add(title);

            // Student info box
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            addInfoCell(infoTable, "Student Name:", student.getFirstName() + " " + student.getLastName());
            addInfoCell(infoTable, "Grade Level:", student.getGradeLevel() != null ? student.getGradeLevel().toString() : "N/A");
            addInfoCell(infoTable, "Report Period:", "Last 30 Days");
            addInfoCell(infoTable, "Generated:", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
            document.add(infoTable);

            // Large grade display
            addLargeGradeDisplay(document, sessions);

            // Statistics
            addParentStatistics(document, sessions);

            // Recommendations
            addParentRecommendations(document, sessions);

            // Footer
            addFooter(document);

        } finally {
            document.close();
        }

        return outputStream;
    }

    // ==================== School Report PDF ====================

    /**
     * Generate school-wide hall pass report PDF
     */
    public ByteArrayOutputStream generateSchoolReportPdf(LocalDate date) throws DocumentException {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<HallPassSession> sessions = hallPassRepository.findByDateRange(startOfDay, endOfDay);

        log.info("Generating school hall pass PDF for {} with {} sessions", date, sessions.size());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.LETTER.rotate(), 36, 36, 36, 36); // Landscape

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Title
            Paragraph title = new Paragraph("School-Wide Hall Pass Report", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph dateInfo = new Paragraph("Date: " + date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")), SUBTITLE_FONT);
            dateInfo.setAlignment(Element.ALIGN_CENTER);
            dateInfo.setSpacingAfter(20);
            document.add(dateInfo);

            // Summary stats
            addSchoolSummary(document, sessions);

            // By destination chart data
            addDestinationTable(document, sessions);

            // By period breakdown
            addPeriodBreakdown(document, sessions);

            // Top users table
            addTopUsersTable(document, sessions);

            // Footer
            addFooter(document);

        } finally {
            document.close();
        }

        return outputStream;
    }

    // ==================== Excel Export ====================

    /**
     * Generate student hall pass Excel report
     */
    public ByteArrayOutputStream generateStudentReportExcel(Long studentId, LocalDate startDate, LocalDate endDate) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<HallPassSession> sessions = hallPassRepository.findByStudent_IdOrderByDepartureTimeDesc(studentId)
                .stream()
                .filter(s -> s.getDepartureTime() != null &&
                        !s.getDepartureTime().isBefore(start) &&
                        !s.getDepartureTime().isAfter(end))
                .collect(Collectors.toList());

        log.info("Generating hall pass Excel report for student {} with {} sessions", studentId, sessions.size());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (Workbook workbook = new XSSFWorkbook()) {
            // Summary sheet
            Sheet summarySheet = workbook.createSheet("Summary");
            createSummarySheet(workbook, summarySheet, student, sessions, startDate, endDate);

            // Details sheet
            Sheet detailsSheet = workbook.createSheet("Details");
            createDetailsSheet(workbook, detailsSheet, sessions);

            // Analytics sheet
            Sheet analyticsSheet = workbook.createSheet("Analytics");
            createAnalyticsSheet(workbook, analyticsSheet, sessions);

            workbook.write(outputStream);
            log.info("Excel report generated successfully");

        } catch (Exception e) {
            log.error("Failed to generate Excel report", e);
            throw new RuntimeException("Failed to generate Excel report: " + e.getMessage());
        }

        return outputStream;
    }

    /**
     * Generate school-wide Excel report
     */
    public ByteArrayOutputStream generateSchoolReportExcel(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<HallPassSession> sessions = hallPassRepository.findByDateRange(start, end);

        log.info("Generating school Excel report with {} sessions", sessions.size());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (Workbook workbook = new XSSFWorkbook()) {
            // Daily summary sheet
            Sheet dailySheet = workbook.createSheet("Daily Summary");
            createDailySummarySheet(workbook, dailySheet, sessions, startDate, endDate);

            // All sessions sheet
            Sheet allSessionsSheet = workbook.createSheet("All Sessions");
            createAllSessionsSheet(workbook, allSessionsSheet, sessions);

            // By student sheet
            Sheet byStudentSheet = workbook.createSheet("By Student");
            createByStudentSheet(workbook, byStudentSheet, sessions);

            // By destination sheet
            Sheet byDestinationSheet = workbook.createSheet("By Destination");
            createByDestinationSheet(workbook, byDestinationSheet, sessions);

            workbook.write(outputStream);
            log.info("School Excel report generated successfully");

        } catch (Exception e) {
            log.error("Failed to generate school Excel report", e);
            throw new RuntimeException("Failed to generate Excel report: " + e.getMessage());
        }

        return outputStream;
    }

    // ==================== PDF Helper Methods ====================

    private void addSummarySection(Document document, List<HallPassSession> sessions) throws DocumentException {
        Paragraph summaryTitle = new Paragraph("Summary Statistics", SUBTITLE_FONT);
        summaryTitle.setSpacingBefore(20);
        summaryTitle.setSpacingAfter(10);
        document.add(summaryTitle);

        long completed = sessions.stream().filter(s -> s.getStatus() == SessionStatus.COMPLETED).count();
        long overdue = sessions.stream().filter(s -> s.getStatus() == SessionStatus.OVERDUE).count();
        OptionalDouble avgDuration = sessions.stream()
                .filter(s -> s.getDurationMinutes() != null)
                .mapToInt(HallPassSession::getDurationMinutes)
                .average();

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(60);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);

        addStatRow(table, "Total Passes:", String.valueOf(sessions.size()));
        addStatRow(table, "Completed On Time:", String.valueOf(completed));
        addStatRow(table, "Overdue:", String.valueOf(overdue));
        addStatRow(table, "Average Duration:", String.format("%.1f minutes", avgDuration.orElse(0)));

        document.add(table);
    }

    private void addReportCardGrade(Document document, List<HallPassSession> sessions) throws DocumentException {
        int totalPasses = sessions.size();
        long overduePasses = sessions.stream().filter(s -> s.getStatus() == SessionStatus.OVERDUE).count();
        double overdueRate = totalPasses > 0 ? (double) overduePasses / totalPasses : 0;

        String grade;
        BaseColor gradeColor;
        if (totalPasses <= 10 && overdueRate < 0.1) {
            grade = "A";
            gradeColor = new BaseColor(34, 139, 34); // Green
        } else if (totalPasses <= 20 && overdueRate < 0.2) {
            grade = "B";
            gradeColor = new BaseColor(30, 144, 255); // Blue
        } else if (totalPasses <= 30 && overdueRate < 0.3) {
            grade = "C";
            gradeColor = new BaseColor(255, 165, 0); // Orange
        } else {
            grade = "D";
            gradeColor = new BaseColor(220, 20, 60); // Red
        }

        Paragraph gradeSection = new Paragraph();
        gradeSection.setSpacingBefore(20);
        gradeSection.add(new Chunk("Responsibility Grade: ", SUBTITLE_FONT));

        com.itextpdf.text.Font coloredGradeFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 24, com.itextpdf.text.Font.BOLD, gradeColor);
        gradeSection.add(new Chunk(grade, coloredGradeFont));
        document.add(gradeSection);
    }

    private void addDestinationBreakdown(Document document, List<HallPassSession> sessions) throws DocumentException {
        Paragraph destTitle = new Paragraph("Passes by Destination", SUBTITLE_FONT);
        destTitle.setSpacingBefore(20);
        destTitle.setSpacingAfter(10);
        document.add(destTitle);

        Map<String, Long> byDestination = sessions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getDestination().getDisplayName(),
                        Collectors.counting()));

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(50);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);

        addTableHeader(table, "Destination", HEADER_FONT);
        addTableHeader(table, "Count", HEADER_FONT);

        byDestination.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(entry -> {
                    addTableCell(table, entry.getKey());
                    addTableCell(table, String.valueOf(entry.getValue()));
                });

        document.add(table);
    }

    private void addSessionDetails(Document document, List<HallPassSession> sessions) throws DocumentException {
        if (sessions.isEmpty()) return;

        document.newPage();

        Paragraph detailsTitle = new Paragraph("Session Details", SUBTITLE_FONT);
        detailsTitle.setSpacingAfter(10);
        document.add(detailsTitle);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2f, 2f, 2f, 1.5f, 1.5f});

        addTableHeader(table, "Date", HEADER_FONT);
        addTableHeader(table, "Departure", HEADER_FONT);
        addTableHeader(table, "Return", HEADER_FONT);
        addTableHeader(table, "Destination", HEADER_FONT);
        addTableHeader(table, "Duration", HEADER_FONT);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

        for (HallPassSession session : sessions.stream().limit(50).toList()) {
            addTableCell(table, session.getDepartureTime() != null ?
                    session.getDepartureTime().format(dateFormatter) : "");
            addTableCell(table, session.getDepartureTime() != null ?
                    session.getDepartureTime().format(timeFormatter) : "");
            addTableCell(table, session.getReturnTime() != null ?
                    session.getReturnTime().format(timeFormatter) : "Active");
            addTableCell(table, session.getDestination().getDisplayName());
            addTableCell(table, session.getDurationMinutes() != null ?
                    session.getDurationMinutes() + " min" : "-");
        }

        document.add(table);

        if (sessions.size() > 50) {
            Paragraph note = new Paragraph("Showing first 50 of " + sessions.size() + " sessions", SMALL_FONT);
            note.setSpacingBefore(5);
            document.add(note);
        }
    }

    private void addLargeGradeDisplay(Document document, List<HallPassSession> sessions) throws DocumentException {
        int totalPasses = sessions.size();
        long overduePasses = sessions.stream().filter(s -> s.getStatus() == SessionStatus.OVERDUE).count();
        double overdueRate = totalPasses > 0 ? (double) overduePasses / totalPasses : 0;

        String grade;
        String feedback;
        BaseColor gradeColor;

        if (totalPasses <= 10 && overdueRate < 0.1) {
            grade = "A";
            feedback = "Excellent! Minimal hall pass usage with timely returns.";
            gradeColor = new BaseColor(34, 139, 34);
        } else if (totalPasses <= 20 && overdueRate < 0.2) {
            grade = "B";
            feedback = "Good hall pass habits with room for improvement.";
            gradeColor = new BaseColor(30, 144, 255);
        } else if (totalPasses <= 30 && overdueRate < 0.3) {
            grade = "C";
            feedback = "Average usage. Consider reducing unnecessary passes.";
            gradeColor = new BaseColor(255, 165, 0);
        } else {
            grade = "D";
            feedback = "High hall pass usage detected. Please review with teacher.";
            gradeColor = new BaseColor(220, 20, 60);
        }

        // Grade box
        PdfPTable gradeTable = new PdfPTable(1);
        gradeTable.setWidthPercentage(30);
        gradeTable.setSpacingBefore(20);
        gradeTable.setHorizontalAlignment(Element.ALIGN_CENTER);

        com.itextpdf.text.Font largeGradeFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 72, com.itextpdf.text.Font.BOLD, gradeColor);

        PdfPCell gradeCell = new PdfPCell(new Phrase(grade, largeGradeFont));
        gradeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        gradeCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        gradeCell.setPadding(20);
        gradeCell.setBorderColor(gradeColor);
        gradeCell.setBorderWidth(3);
        gradeTable.addCell(gradeCell);

        document.add(gradeTable);

        Paragraph feedbackPara = new Paragraph(feedback, NORMAL_FONT);
        feedbackPara.setAlignment(Element.ALIGN_CENTER);
        feedbackPara.setSpacingBefore(10);
        document.add(feedbackPara);
    }

    private void addParentStatistics(Document document, List<HallPassSession> sessions) throws DocumentException {
        Paragraph statsTitle = new Paragraph("30-Day Statistics", SUBTITLE_FONT);
        statsTitle.setSpacingBefore(30);
        statsTitle.setSpacingAfter(10);
        document.add(statsTitle);

        long completed = sessions.stream().filter(s -> s.getStatus() == SessionStatus.COMPLETED).count();
        long overdue = sessions.stream().filter(s -> s.getStatus() == SessionStatus.OVERDUE).count();

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(70);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);

        addStatRow(table, "Total Hall Passes:", String.valueOf(sessions.size()));
        addStatRow(table, "Returned On Time:", String.valueOf(completed));
        addStatRow(table, "Returned Late:", String.valueOf(overdue));
        addStatRow(table, "On-Time Rate:",
                String.format("%.0f%%", sessions.size() > 0 ? (double) completed / sessions.size() * 100 : 100));

        document.add(table);
    }

    private void addParentRecommendations(Document document, List<HallPassSession> sessions) throws DocumentException {
        int totalPasses = sessions.size();
        long overduePasses = sessions.stream().filter(s -> s.getStatus() == SessionStatus.OVERDUE).count();

        Paragraph recTitle = new Paragraph("Recommendations", SUBTITLE_FONT);
        recTitle.setSpacingBefore(30);
        recTitle.setSpacingAfter(10);
        document.add(recTitle);

        com.itextpdf.text.List recommendations = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);

        if (totalPasses > 20 || overduePasses > 3) {
            recommendations.add(new ListItem("Schedule a meeting with your child's teacher to discuss hall pass usage.", NORMAL_FONT));
            recommendations.add(new ListItem("Set expectations at home about staying focused during class time.", NORMAL_FONT));
        } else if (totalPasses > 10) {
            recommendations.add(new ListItem("Encourage your child to limit non-essential hall passes.", NORMAL_FONT));
        } else {
            recommendations.add(new ListItem("Great job! Continue encouraging responsible behavior.", NORMAL_FONT));
        }

        document.add(recommendations);
    }

    private void addSchoolSummary(Document document, List<HallPassSession> sessions) throws DocumentException {
        Paragraph summaryTitle = new Paragraph("Daily Summary", SUBTITLE_FONT);
        summaryTitle.setSpacingAfter(10);
        document.add(summaryTitle);

        long active = sessions.stream().filter(s -> s.getStatus() == SessionStatus.ACTIVE).count();
        long completed = sessions.stream().filter(s -> s.getStatus() == SessionStatus.COMPLETED).count();
        long overdue = sessions.stream().filter(s -> s.getStatus() == SessionStatus.OVERDUE).count();

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(80);

        addStatCell(table, "Total Passes", String.valueOf(sessions.size()));
        addStatCell(table, "Active", String.valueOf(active));
        addStatCell(table, "Completed", String.valueOf(completed));
        addStatCell(table, "Overdue", String.valueOf(overdue));

        document.add(table);
    }

    private void addDestinationTable(Document document, List<HallPassSession> sessions) throws DocumentException {
        Paragraph title = new Paragraph("By Destination", SUBTITLE_FONT);
        title.setSpacingBefore(20);
        title.setSpacingAfter(10);
        document.add(title);

        Map<String, Long> byDestination = sessions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getDestination().getDisplayName(),
                        Collectors.counting()));

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(50);

        addTableHeader(table, "Destination", HEADER_FONT);
        addTableHeader(table, "Count", HEADER_FONT);

        byDestination.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(entry -> {
                    addTableCell(table, entry.getKey());
                    addTableCell(table, String.valueOf(entry.getValue()));
                });

        document.add(table);
    }

    private void addPeriodBreakdown(Document document, List<HallPassSession> sessions) throws DocumentException {
        Paragraph title = new Paragraph("By Period", SUBTITLE_FONT);
        title.setSpacingBefore(20);
        title.setSpacingAfter(10);
        document.add(title);

        Map<Integer, Long> byPeriod = sessions.stream()
                .filter(s -> s.getPeriod() != null)
                .collect(Collectors.groupingBy(HallPassSession::getPeriod, Collectors.counting()));

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(40);

        addTableHeader(table, "Period", HEADER_FONT);
        addTableHeader(table, "Count", HEADER_FONT);

        byPeriod.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    addTableCell(table, "Period " + entry.getKey());
                    addTableCell(table, String.valueOf(entry.getValue()));
                });

        document.add(table);
    }

    private void addTopUsersTable(Document document, List<HallPassSession> sessions) throws DocumentException {
        Paragraph title = new Paragraph("Top 10 Students by Usage", SUBTITLE_FONT);
        title.setSpacingBefore(20);
        title.setSpacingAfter(10);
        document.add(title);

        Map<String, Long> byStudent = sessions.stream()
                .filter(s -> s.getStudent() != null)
                .collect(Collectors.groupingBy(
                        s -> s.getStudent().getFirstName() + " " + s.getStudent().getLastName(),
                        Collectors.counting()));

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(60);

        addTableHeader(table, "Student", HEADER_FONT);
        addTableHeader(table, "Passes", HEADER_FONT);

        byStudent.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    addTableCell(table, entry.getKey());
                    addTableCell(table, String.valueOf(entry.getValue()));
                });

        document.add(table);
    }

    // ==================== Excel Helper Methods ====================

    private void createSummarySheet(Workbook workbook, Sheet sheet, Student student,
                                     List<HallPassSession> sessions, LocalDate startDate, LocalDate endDate) {
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);

        int rowNum = 0;

        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Hall Pass Report - " + student.getFirstName() + " " + student.getLastName());
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        rowNum++; // Blank row

        // Date range
        Row dateRow = sheet.createRow(rowNum++);
        dateRow.createCell(0).setCellValue("Report Period:");
        dateRow.createCell(1).setCellValue(formatDateRange(startDate, endDate));

        rowNum++; // Blank row

        // Statistics
        long completed = sessions.stream().filter(s -> s.getStatus() == SessionStatus.COMPLETED).count();
        long overdue = sessions.stream().filter(s -> s.getStatus() == SessionStatus.OVERDUE).count();

        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Metric");
        headerRow.createCell(1).setCellValue("Value");
        headerRow.getCell(0).setCellStyle(headerStyle);
        headerRow.getCell(1).setCellStyle(headerStyle);

        createStatRow(sheet, rowNum++, "Total Passes", sessions.size());
        createStatRow(sheet, rowNum++, "Completed On Time", (int) completed);
        createStatRow(sheet, rowNum++, "Overdue", (int) overdue);

        OptionalDouble avgDuration = sessions.stream()
                .filter(s -> s.getDurationMinutes() != null)
                .mapToInt(HallPassSession::getDurationMinutes)
                .average();
        Row avgRow = sheet.createRow(rowNum++);
        avgRow.createCell(0).setCellValue("Average Duration (min)");
        avgRow.createCell(1).setCellValue(String.format("%.1f", avgDuration.orElse(0)));

        // Auto-size columns
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createDetailsSheet(Workbook workbook, Sheet sheet, List<HallPassSession> sessions) {
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);

        // Headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Date", "Departure Time", "Return Time", "Destination", "Duration (min)", "Status", "Period"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        int rowNum = 1;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

        for (HallPassSession session : sessions) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(session.getDepartureTime() != null ?
                    session.getDepartureTime().format(dateFormatter) : "");
            row.createCell(1).setCellValue(session.getDepartureTime() != null ?
                    session.getDepartureTime().format(timeFormatter) : "");
            row.createCell(2).setCellValue(session.getReturnTime() != null ?
                    session.getReturnTime().format(timeFormatter) : "Active");
            row.createCell(3).setCellValue(session.getDestination().getDisplayName());
            row.createCell(4).setCellValue(session.getDurationMinutes() != null ?
                    session.getDurationMinutes() : 0);
            row.createCell(5).setCellValue(session.getStatus().name());
            row.createCell(6).setCellValue(session.getPeriod() != null ? session.getPeriod() : 0);
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createAnalyticsSheet(Workbook workbook, Sheet sheet, List<HallPassSession> sessions) {
        CellStyle headerStyle = createHeaderStyle(workbook);

        int rowNum = 0;

        // By Destination
        Row destTitle = sheet.createRow(rowNum++);
        destTitle.createCell(0).setCellValue("Passes by Destination");

        Row destHeader = sheet.createRow(rowNum++);
        destHeader.createCell(0).setCellValue("Destination");
        destHeader.createCell(1).setCellValue("Count");
        destHeader.getCell(0).setCellStyle(headerStyle);
        destHeader.getCell(1).setCellStyle(headerStyle);

        Map<String, Long> byDestination = sessions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getDestination().getDisplayName(),
                        Collectors.counting()));

        for (Map.Entry<String, Long> entry : byDestination.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createDailySummarySheet(Workbook workbook, Sheet sheet, List<HallPassSession> sessions,
                                          LocalDate startDate, LocalDate endDate) {
        CellStyle headerStyle = createHeaderStyle(workbook);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"Date", "Total", "Completed", "Overdue", "Avg Duration"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            LocalDate date = current;
            List<HallPassSession> daySessions = sessions.stream()
                    .filter(s -> s.getDepartureTime() != null &&
                            s.getDepartureTime().toLocalDate().equals(date))
                    .toList();

            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            row.createCell(1).setCellValue(daySessions.size());
            row.createCell(2).setCellValue(daySessions.stream()
                    .filter(s -> s.getStatus() == SessionStatus.COMPLETED).count());
            row.createCell(3).setCellValue(daySessions.stream()
                    .filter(s -> s.getStatus() == SessionStatus.OVERDUE).count());

            OptionalDouble avg = daySessions.stream()
                    .filter(s -> s.getDurationMinutes() != null)
                    .mapToInt(HallPassSession::getDurationMinutes)
                    .average();
            row.createCell(4).setCellValue(String.format("%.1f", avg.orElse(0)));

            current = current.plusDays(1);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createAllSessionsSheet(Workbook workbook, Sheet sheet, List<HallPassSession> sessions) {
        CellStyle headerStyle = createHeaderStyle(workbook);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"Date", "Student", "Departure", "Return", "Destination", "Duration", "Status"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

        for (HallPassSession session : sessions) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(session.getDepartureTime() != null ?
                    session.getDepartureTime().format(dateFormatter) : "");
            row.createCell(1).setCellValue(session.getStudent() != null ?
                    session.getStudent().getFirstName() + " " + session.getStudent().getLastName() : "");
            row.createCell(2).setCellValue(session.getDepartureTime() != null ?
                    session.getDepartureTime().format(timeFormatter) : "");
            row.createCell(3).setCellValue(session.getReturnTime() != null ?
                    session.getReturnTime().format(timeFormatter) : "Active");
            row.createCell(4).setCellValue(session.getDestination().getDisplayName());
            row.createCell(5).setCellValue(session.getDurationMinutes() != null ?
                    session.getDurationMinutes() : 0);
            row.createCell(6).setCellValue(session.getStatus().name());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createByStudentSheet(Workbook workbook, Sheet sheet, List<HallPassSession> sessions) {
        CellStyle headerStyle = createHeaderStyle(workbook);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"Student", "Total Passes", "Completed", "Overdue", "Avg Duration"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        Map<String, List<HallPassSession>> byStudent = sessions.stream()
                .filter(s -> s.getStudent() != null)
                .collect(Collectors.groupingBy(
                        s -> s.getStudent().getFirstName() + " " + s.getStudent().getLastName()));

        int rowNum = 1;
        for (Map.Entry<String, List<HallPassSession>> entry : byStudent.entrySet()) {
            List<HallPassSession> studentSessions = entry.getValue();
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(studentSessions.size());
            row.createCell(2).setCellValue(studentSessions.stream()
                    .filter(s -> s.getStatus() == SessionStatus.COMPLETED).count());
            row.createCell(3).setCellValue(studentSessions.stream()
                    .filter(s -> s.getStatus() == SessionStatus.OVERDUE).count());

            OptionalDouble avg = studentSessions.stream()
                    .filter(s -> s.getDurationMinutes() != null)
                    .mapToInt(HallPassSession::getDurationMinutes)
                    .average();
            row.createCell(4).setCellValue(String.format("%.1f", avg.orElse(0)));
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createByDestinationSheet(Workbook workbook, Sheet sheet, List<HallPassSession> sessions) {
        CellStyle headerStyle = createHeaderStyle(workbook);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"Destination", "Total", "Avg Duration"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        Map<String, List<HallPassSession>> byDest = sessions.stream()
                .collect(Collectors.groupingBy(s -> s.getDestination().getDisplayName()));

        int rowNum = 1;
        for (Map.Entry<String, List<HallPassSession>> entry : byDest.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue().size());

            OptionalDouble avg = entry.getValue().stream()
                    .filter(s -> s.getDurationMinutes() != null)
                    .mapToInt(HallPassSession::getDurationMinutes)
                    .average();
            row.createCell(2).setCellValue(String.format("%.1f", avg.orElse(0)));
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    // ==================== Style Helpers ====================

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("mm/dd/yyyy"));
        return style;
    }

    private void createStatRow(Sheet sheet, int rowNum, String label, int value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }

    // ==================== PDF Cell Helpers ====================

    private void addTableHeader(PdfPTable table, String text, com.itextpdf.text.Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new BaseColor(230, 230, 230));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, NORMAL_FONT));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addStatRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, HEADER_FONT));
        labelCell.setPadding(5);
        labelCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setPadding(5);
        valueCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(valueCell);
    }

    private void addStatCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(10);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph p = new Paragraph();
        p.add(new Chunk(value + "\n", SUBTITLE_FONT));
        p.add(new Chunk(label, SMALL_FONT));
        cell.addElement(p);

        table.addCell(cell);
    }

    private void addInfoCell(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, HEADER_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(3);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(3);
        table.addCell(valueCell);
    }

    private void addFooter(Document document) throws DocumentException {
        Paragraph footer = new Paragraph(
                "Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a")) +
                        " | Heronix Student Information System",
                SMALL_FONT
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(30);
        document.add(footer);
    }

    private String formatDateRange(LocalDate start, LocalDate end) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        return start.format(formatter) + " - " + end.format(formatter);
    }
}
