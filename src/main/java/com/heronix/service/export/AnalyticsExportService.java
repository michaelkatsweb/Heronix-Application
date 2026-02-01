package com.heronix.service.export;

import com.heronix.dto.analytics.AnalyticsSummaryDTO;
import com.heronix.dto.analytics.StudentAnalyticsDTO;
import com.heronix.model.domain.CounselingReferral;
import com.heronix.model.domain.NetworkDevice;
import com.heronix.model.domain.Student;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Analytics Export Service
 *
 * Provides export functionality for analytics data to PDF, Excel, and CSV formats.
 *
 * Features:
 * - Student Analytics export to PDF
 * - Student Analytics export to Excel (.xlsx)
 * - Student Analytics export to CSV
 * - At-Risk student list export
 * - Honor Roll list export
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 60 - Analytics Export Implementation
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyticsExportService {

    private static final String SCHOOL_NAME = "Heronix Student Information System";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // PDF Font definitions
    private static final com.itextpdf.text.Font TITLE_FONT =
            new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
    private static final com.itextpdf.text.Font SECTION_FONT =
            new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD);
    private static final com.itextpdf.text.Font HEADER_FONT =
            new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.BOLD);
    private static final com.itextpdf.text.Font NORMAL_FONT =
            new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.NORMAL);
    private static final com.itextpdf.text.Font SMALL_FONT =
            new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.NORMAL);

    // CSV constants
    private static final String CSV_SEPARATOR = ",";
    private static final String LINE_SEPARATOR = "\r\n";
    private static final byte[] UTF8_BOM = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    // ========================================================================
    // HUB SUMMARY EXPORT
    // ========================================================================

    /**
     * Export analytics hub summary to Excel
     *
     * @param summary The hub summary data
     * @return Excel file as byte array
     */
    public byte[] exportHubSummaryExcel(AnalyticsSummaryDTO summary) throws IOException {
        log.info("Generating Analytics Hub Summary Excel report");

        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = createExcelHeaderStyle(workbook);
            CellStyle titleStyle = createExcelTitleStyle(workbook);

            Sheet sheet = workbook.createSheet("Analytics Summary");
            int rowNum = 0;

            // Title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Analytics Hub Summary Report");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

            // Subtitle
            Row subRow = sheet.createRow(rowNum++);
            subRow.createCell(0).setCellValue("Generated: " + LocalDateTime.now().format(DATETIME_FORMATTER));
            if (summary.getCampusName() != null) {
                subRow.createCell(2).setCellValue("Campus: " + summary.getCampusName());
            }
            if (summary.getAcademicYear() != null) {
                Row yearRow = sheet.createRow(rowNum++);
                yearRow.createCell(0).setCellValue("Academic Year: " + summary.getAcademicYear());
            }
            rowNum++; // Empty row

            // Overall Health Score
            Row healthRow = sheet.createRow(rowNum++);
            createExcelHeaderCell(healthRow, 0, "Overall Health Score", headerStyle);
            healthRow.createCell(1).setCellValue(summary.getOverallHealthScore() + " - " + summary.getHealthStatus());
            rowNum++; // Empty row

            // Student Metrics Section
            Row studentHeader = sheet.createRow(rowNum++);
            createExcelHeaderCell(studentHeader, 0, "STUDENT METRICS", headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1));

            addExcelDataRow(sheet, rowNum++, "Total Students", formatLong(summary.getTotalStudents()), null);
            addExcelDataRow(sheet, rowNum++, "Active Students", formatLong(summary.getActiveStudents()), null);
            addExcelDataRow(sheet, rowNum++, "New Enrollments", formatLong(summary.getNewEnrollments()), null);
            addExcelDataRow(sheet, rowNum++, "Withdrawals", formatLong(summary.getWithdrawals()), null);
            rowNum++; // Empty row

            // Attendance Metrics Section
            Row attendanceHeader = sheet.createRow(rowNum++);
            createExcelHeaderCell(attendanceHeader, 0, "ATTENDANCE METRICS", headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1));

            addExcelDataRow(sheet, rowNum++, "Attendance Rate", formatPercent(summary.getAttendanceRate()), null);
            addExcelDataRow(sheet, rowNum++, "Students Present Today", formatLong(summary.getStudentsPresent()), null);
            addExcelDataRow(sheet, rowNum++, "Students Absent Today", formatLong(summary.getStudentsAbsent()), null);
            addExcelDataRow(sheet, rowNum++, "Chronic Absenteeism Count", formatLong(summary.getChronicAbsenteeismCount()), null);
            rowNum++; // Empty row

            // Academic Metrics Section
            Row academicHeader = sheet.createRow(rowNum++);
            createExcelHeaderCell(academicHeader, 0, "ACADEMIC METRICS", headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1));

            addExcelDataRow(sheet, rowNum++, "Average GPA", formatDouble(summary.getAverageGPA()), null);
            addExcelDataRow(sheet, rowNum++, "Honor Roll Students", formatLong(summary.getHonorRollCount()), null);
            addExcelDataRow(sheet, rowNum++, "Failing Students", formatLong(summary.getFailingStudentsCount()), null);
            addExcelDataRow(sheet, rowNum++, "Pass Rate", formatPercent(summary.getPassRate()), null);
            rowNum++; // Empty row

            // Staff Metrics Section
            Row staffHeader = sheet.createRow(rowNum++);
            createExcelHeaderCell(staffHeader, 0, "STAFF METRICS", headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1));

            addExcelDataRow(sheet, rowNum++, "Total Staff", formatLong(summary.getTotalStaff()), null);
            addExcelDataRow(sheet, rowNum++, "Certification Compliant", formatLong(summary.getCertificationCompliant()), null);
            addExcelDataRow(sheet, rowNum++, "Certifications Expiring Soon", formatLong(summary.getCertificationExpiringSoon()), null);
            addExcelDataRow(sheet, rowNum++, "Certification Compliance Rate", formatPercent(summary.getCertificationComplianceRate()), null);
            rowNum++; // Empty row

            // Behavior Metrics Section
            Row behaviorHeader = sheet.createRow(rowNum++);
            createExcelHeaderCell(behaviorHeader, 0, "BEHAVIOR METRICS", headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1));

            addExcelDataRow(sheet, rowNum++, "Incidents Today", formatLong(summary.getTotalIncidentsToday()), null);
            addExcelDataRow(sheet, rowNum++, "Incidents This Week", formatLong(summary.getTotalIncidentsThisWeek()), null);
            addExcelDataRow(sheet, rowNum++, "Positive Incidents", formatLong(summary.getPositiveIncidents()), null);
            addExcelDataRow(sheet, rowNum++, "Negative Incidents", formatLong(summary.getNegativeIncidents()), null);
            rowNum++; // Empty row

            // At-Risk Summary Section
            Row riskHeader = sheet.createRow(rowNum++);
            createExcelHeaderCell(riskHeader, 0, "AT-RISK SUMMARY", headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1));

            addExcelDataRow(sheet, rowNum++, "Total At-Risk Students", formatLong(summary.getAtRiskStudentsTotal()), null);
            addExcelDataRow(sheet, rowNum++, "Academic Risk", formatLong(summary.getAcademicRisk()), null);
            addExcelDataRow(sheet, rowNum++, "Attendance Risk", formatLong(summary.getAttendanceRisk()), null);
            addExcelDataRow(sheet, rowNum++, "Behavior Risk", formatLong(summary.getBehaviorRisk()), null);

            // Auto-size columns
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);

            log.info("Analytics Hub Summary Excel generated successfully");
            return baos.toByteArray();
        }
    }

    private String formatLong(Long value) {
        return value != null ? String.valueOf(value) : "N/A";
    }

    private String formatDouble(Double value) {
        return value != null ? String.format("%.2f", value) : "N/A";
    }

    private String formatPercent(Double value) {
        return value != null ? String.format("%.1f%%", value) : "N/A";
    }

    // ========================================================================
    // PDF EXPORT
    // ========================================================================

    /**
     * Export student analytics to PDF
     *
     * @param analytics The analytics data
     * @param campusName Optional campus name filter
     * @return PDF file as byte array
     */
    public byte[] exportStudentAnalyticsPdf(StudentAnalyticsDTO analytics, String campusName) throws Exception {
        log.info("Generating Student Analytics PDF report");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.LETTER, 50, 50, 50, 50);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // Title
            Paragraph title = new Paragraph("Student Analytics Report", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            // Subtitle with campus and date
            String subtitle = "Generated: " + LocalDateTime.now().format(DATETIME_FORMATTER);
            if (campusName != null && !campusName.isEmpty()) {
                subtitle = "Campus: " + campusName + " | " + subtitle;
            }
            Paragraph sub = new Paragraph(subtitle, SMALL_FONT);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(20);
            document.add(sub);

            // 1. Enrollment Summary Section
            addPdfSection(document, "Enrollment Summary");
            PdfPTable enrollmentTable = new PdfPTable(2);
            enrollmentTable.setWidthPercentage(60);
            enrollmentTable.setHorizontalAlignment(Element.ALIGN_LEFT);

            addPdfKeyValue(enrollmentTable, "Total Students", String.valueOf(analytics.getTotalStudents()));

            if (analytics.getEnrollmentTrends() != null) {
                var trends = analytics.getEnrollmentTrends();
                addPdfKeyValue(enrollmentTable, "New Students", String.valueOf(trends.getNewEnrollments()));
                addPdfKeyValue(enrollmentTable, "Capacity Utilization",
                        String.format("%.1f%%", trends.getUtilizationRate() != null ? trends.getUtilizationRate() : 0.0));
            }

            document.add(enrollmentTable);
            document.add(new Paragraph(" "));

            // 2. GPA Analytics Section
            addPdfSection(document, "GPA Analytics");
            PdfPTable gpaTable = new PdfPTable(2);
            gpaTable.setWidthPercentage(60);
            gpaTable.setHorizontalAlignment(Element.ALIGN_LEFT);

            if (analytics.getOverallAverageGPA() != null) {
                addPdfKeyValue(gpaTable, "Overall Average GPA",
                        String.format("%.2f", analytics.getOverallAverageGPA()));
            }

            if (analytics.getHonorRollSummary() != null) {
                var honors = analytics.getHonorRollSummary();
                addPdfKeyValue(gpaTable, "High Honors (4.0+)",
                        String.valueOf(honors.getOrDefault("highHonors", 0)));
                addPdfKeyValue(gpaTable, "Honors (3.5-3.99)",
                        String.valueOf(honors.getOrDefault("honors", 0)));
                addPdfKeyValue(gpaTable, "Honorable Mention (3.0-3.49)",
                        String.valueOf(honors.getOrDefault("honorableMention", 0)));
            }

            document.add(gpaTable);
            document.add(new Paragraph(" "));

            // 3. GPA Distribution
            if (analytics.getGpaDistribution() != null && !analytics.getGpaDistribution().isEmpty()) {
                addPdfSection(document, "GPA Distribution");
                PdfPTable distTable = new PdfPTable(2);
                distTable.setWidthPercentage(50);
                distTable.setHorizontalAlignment(Element.ALIGN_LEFT);

                addPdfTableHeader(distTable, "GPA Range");
                addPdfTableHeader(distTable, "Count");

                for (Map.Entry<String, Long> entry : analytics.getGpaDistribution().entrySet()) {
                    addPdfTableCell(distTable, entry.getKey());
                    addPdfTableCell(distTable, String.valueOf(entry.getValue()));
                }

                document.add(distTable);
                document.add(new Paragraph(" "));
            }

            // 4. Special Needs Summary
            if (analytics.getSpecialNeeds() != null) {
                addPdfSection(document, "Special Needs Summary");
                PdfPTable snTable = new PdfPTable(2);
                snTable.setWidthPercentage(60);
                snTable.setHorizontalAlignment(Element.ALIGN_LEFT);

                var sn = analytics.getSpecialNeeds();
                addPdfKeyValue(snTable, "IEP Students", String.valueOf(sn.getIepCount()));
                addPdfKeyValue(snTable, "504 Plan Students", String.valueOf(sn.getPlan504Count()));
                addPdfKeyValue(snTable, "Gifted/Talented", String.valueOf(sn.getGiftedCount()));
                addPdfKeyValue(snTable, "ELL Students", String.valueOf(sn.getEllCount()));

                document.add(snTable);
                document.add(new Paragraph(" "));
            }

            // 5. At-Risk Summary
            if (analytics.getAtRiskSummary() != null) {
                addPdfSection(document, "At-Risk Students Summary");
                PdfPTable riskTable = new PdfPTable(2);
                riskTable.setWidthPercentage(60);
                riskTable.setHorizontalAlignment(Element.ALIGN_LEFT);

                var risk = analytics.getAtRiskSummary();
                addPdfKeyValue(riskTable, "Critical Risk",
                        String.valueOf(risk.getOrDefault("critical", 0)));
                addPdfKeyValue(riskTable, "High Risk",
                        String.valueOf(risk.getOrDefault("high", 0)));
                addPdfKeyValue(riskTable, "Moderate Risk",
                        String.valueOf(risk.getOrDefault("moderate", 0)));
                addPdfKeyValue(riskTable, "Total At-Risk",
                        String.valueOf(risk.getOrDefault("total", 0)));

                document.add(riskTable);
            }

            // Footer
            Paragraph footer = new Paragraph(
                    "\n" + SCHOOL_NAME + " | Confidential Report",
                    SMALL_FONT
            );
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            log.info("Student Analytics PDF generated successfully");

        } finally {
            document.close();
        }

        return baos.toByteArray();
    }

    // ========================================================================
    // EXCEL EXPORT
    // ========================================================================

    /**
     * Export student analytics to Excel
     *
     * @param analytics The analytics data
     * @param campusName Optional campus name filter
     * @return Excel file as byte array
     */
    public byte[] exportStudentAnalyticsExcel(StudentAnalyticsDTO analytics, String campusName) throws IOException {
        log.info("Generating Student Analytics Excel report");

        try (Workbook workbook = new XSSFWorkbook()) {
            // Create styles
            CellStyle headerStyle = createExcelHeaderStyle(workbook);
            CellStyle valueStyle = createExcelValueStyle(workbook);
            CellStyle titleStyle = createExcelTitleStyle(workbook);

            // 1. Summary Sheet
            Sheet summarySheet = workbook.createSheet("Summary");
            int rowNum = 0;

            // Title
            Row titleRow = summarySheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Student Analytics Report");
            titleCell.setCellStyle(titleStyle);
            summarySheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

            // Subtitle
            Row subRow = summarySheet.createRow(rowNum++);
            subRow.createCell(0).setCellValue("Generated: " + LocalDateTime.now().format(DATETIME_FORMATTER));
            if (campusName != null && !campusName.isEmpty()) {
                subRow.createCell(2).setCellValue("Campus: " + campusName);
            }
            rowNum++; // Empty row

            // Enrollment Data
            Row headerRow = summarySheet.createRow(rowNum++);
            createExcelHeaderCell(headerRow, 0, "Metric", headerStyle);
            createExcelHeaderCell(headerRow, 1, "Value", headerStyle);

            addExcelDataRow(summarySheet, rowNum++, "Total Students",
                    String.valueOf(analytics.getTotalStudents()), valueStyle);

            if (analytics.getOverallAverageGPA() != null) {
                addExcelDataRow(summarySheet, rowNum++, "Overall Average GPA",
                        String.format("%.2f", analytics.getOverallAverageGPA()), valueStyle);
            }

            if (analytics.getEnrollmentTrends() != null) {
                addExcelDataRow(summarySheet, rowNum++, "New Students This Year",
                        String.valueOf(analytics.getEnrollmentTrends().getNewEnrollments()), valueStyle);
                addExcelDataRow(summarySheet, rowNum++, "Capacity Utilization",
                        String.format("%.1f%%", analytics.getEnrollmentTrends().getUtilizationRate() != null ?
                                analytics.getEnrollmentTrends().getUtilizationRate() : 0.0), valueStyle);
            }

            // Auto-size columns
            for (int i = 0; i < 4; i++) {
                summarySheet.autoSizeColumn(i);
            }

            // 2. GPA Distribution Sheet
            if (analytics.getGpaDistribution() != null && !analytics.getGpaDistribution().isEmpty()) {
                Sheet gpaSheet = workbook.createSheet("GPA Distribution");
                rowNum = 0;

                Row gpaHeader = gpaSheet.createRow(rowNum++);
                createExcelHeaderCell(gpaHeader, 0, "GPA Range", headerStyle);
                createExcelHeaderCell(gpaHeader, 1, "Student Count", headerStyle);

                for (Map.Entry<String, Long> entry : analytics.getGpaDistribution().entrySet()) {
                    Row dataRow = gpaSheet.createRow(rowNum++);
                    dataRow.createCell(0).setCellValue(entry.getKey());
                    dataRow.createCell(1).setCellValue(entry.getValue());
                }

                gpaSheet.autoSizeColumn(0);
                gpaSheet.autoSizeColumn(1);
            }

            // 3. Special Needs Sheet
            if (analytics.getSpecialNeeds() != null) {
                Sheet snSheet = workbook.createSheet("Special Needs");
                rowNum = 0;

                Row snHeader = snSheet.createRow(rowNum++);
                createExcelHeaderCell(snHeader, 0, "Category", headerStyle);
                createExcelHeaderCell(snHeader, 1, "Count", headerStyle);
                createExcelHeaderCell(snHeader, 2, "Percentage", headerStyle);

                var sn = analytics.getSpecialNeeds();
                long total = analytics.getTotalStudents() != null ? analytics.getTotalStudents() : 1;

                addExcelSpecialNeedsRow(snSheet, rowNum++, "IEP Students", sn.getIepCount(), total, valueStyle);
                addExcelSpecialNeedsRow(snSheet, rowNum++, "504 Plan Students", sn.getPlan504Count(), total, valueStyle);
                addExcelSpecialNeedsRow(snSheet, rowNum++, "Gifted/Talented", sn.getGiftedCount(), total, valueStyle);
                addExcelSpecialNeedsRow(snSheet, rowNum++, "ELL Students", sn.getEllCount(), total, valueStyle);

                for (int i = 0; i < 3; i++) {
                    snSheet.autoSizeColumn(i);
                }
            }

            // Write to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);

            log.info("Student Analytics Excel generated successfully");
            return baos.toByteArray();
        }
    }

    // ========================================================================
    // CSV EXPORT
    // ========================================================================

    /**
     * Export student analytics summary to CSV
     *
     * @param analytics The analytics data
     * @param campusName Optional campus name filter
     * @return CSV file as byte array
     */
    public byte[] exportStudentAnalyticsCsv(StudentAnalyticsDTO analytics, String campusName) throws IOException {
        log.info("Generating Student Analytics CSV report");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(UTF8_BOM);

        try (Writer writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {
            // Header
            writeCsvLine(writer, "Student Analytics Report");
            writeCsvLine(writer, "Generated", LocalDateTime.now().format(DATETIME_FORMATTER));
            if (campusName != null && !campusName.isEmpty()) {
                writeCsvLine(writer, "Campus", campusName);
            }
            writeCsvLine(writer, ""); // Empty line

            // Summary Section
            writeCsvLine(writer, "SUMMARY");
            writeCsvLine(writer, "Metric", "Value");
            writeCsvLine(writer, "Total Students", String.valueOf(analytics.getTotalStudents()));

            if (analytics.getOverallAverageGPA() != null) {
                writeCsvLine(writer, "Overall Average GPA",
                        String.format("%.2f", analytics.getOverallAverageGPA()));
            }

            if (analytics.getEnrollmentTrends() != null) {
                writeCsvLine(writer, "New Students This Year",
                        String.valueOf(analytics.getEnrollmentTrends().getNewEnrollments()));
                writeCsvLine(writer, "Capacity Utilization",
                        String.format("%.1f%%", analytics.getEnrollmentTrends().getUtilizationRate() != null ?
                                analytics.getEnrollmentTrends().getUtilizationRate() : 0.0));
            }

            writeCsvLine(writer, ""); // Empty line

            // GPA Distribution Section
            if (analytics.getGpaDistribution() != null && !analytics.getGpaDistribution().isEmpty()) {
                writeCsvLine(writer, "GPA DISTRIBUTION");
                writeCsvLine(writer, "GPA Range", "Student Count");

                for (Map.Entry<String, Long> entry : analytics.getGpaDistribution().entrySet()) {
                    writeCsvLine(writer, entry.getKey(), String.valueOf(entry.getValue()));
                }

                writeCsvLine(writer, ""); // Empty line
            }

            // Special Needs Section
            if (analytics.getSpecialNeeds() != null) {
                writeCsvLine(writer, "SPECIAL NEEDS");
                writeCsvLine(writer, "Category", "Count", "Percentage");

                var sn = analytics.getSpecialNeeds();
                long total = analytics.getTotalStudents() != null ? analytics.getTotalStudents() : 1;

                writeCsvLine(writer, "IEP Students", String.valueOf(sn.getIepCount()),
                        String.format("%.1f%%", sn.getIepCount() * 100.0 / total));
                writeCsvLine(writer, "504 Plan Students", String.valueOf(sn.getPlan504Count()),
                        String.format("%.1f%%", sn.getPlan504Count() * 100.0 / total));
                writeCsvLine(writer, "Gifted/Talented", String.valueOf(sn.getGiftedCount()),
                        String.format("%.1f%%", sn.getGiftedCount() * 100.0 / total));
                writeCsvLine(writer, "ELL Students", String.valueOf(sn.getEllCount()),
                        String.format("%.1f%%", sn.getEllCount() * 100.0 / total));

                writeCsvLine(writer, ""); // Empty line
            }

            // At-Risk Summary Section
            if (analytics.getAtRiskSummary() != null) {
                writeCsvLine(writer, "AT-RISK STUDENTS");
                writeCsvLine(writer, "Risk Level", "Count");

                var risk = analytics.getAtRiskSummary();
                writeCsvLine(writer, "Critical", String.valueOf(risk.getOrDefault("critical", 0)));
                writeCsvLine(writer, "High", String.valueOf(risk.getOrDefault("high", 0)));
                writeCsvLine(writer, "Moderate", String.valueOf(risk.getOrDefault("moderate", 0)));
                writeCsvLine(writer, "Total At-Risk", String.valueOf(risk.getOrDefault("total", 0)));
            }

            writer.flush();
        }

        log.info("Student Analytics CSV generated successfully");
        return baos.toByteArray();
    }

    // ========================================================================
    // STUDENT LIST EXPORTS
    // ========================================================================

    /**
     * Export at-risk students list to Excel
     *
     * @param students List of at-risk students
     * @return Excel file as byte array
     */
    public byte[] exportAtRiskStudentsExcel(List<Student> students) throws IOException {
        log.info("Generating At-Risk Students Excel report: {} students", students.size());

        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = createExcelHeaderStyle(workbook);
            CellStyle titleStyle = createExcelTitleStyle(workbook);

            Sheet sheet = workbook.createSheet("At-Risk Students");
            int rowNum = 0;

            // Title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("At-Risk Students Report");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

            // Generated date
            Row dateRow = sheet.createRow(rowNum++);
            dateRow.createCell(0).setCellValue("Generated: " + LocalDateTime.now().format(DATETIME_FORMATTER));
            dateRow.createCell(3).setCellValue("Total Students: " + students.size());
            rowNum++; // Empty row

            // Headers
            Row headerRow = sheet.createRow(rowNum++);
            createExcelHeaderCell(headerRow, 0, "Student ID", headerStyle);
            createExcelHeaderCell(headerRow, 1, "Name", headerStyle);
            createExcelHeaderCell(headerRow, 2, "Grade", headerStyle);
            createExcelHeaderCell(headerRow, 3, "GPA", headerStyle);
            createExcelHeaderCell(headerRow, 4, "Risk Level", headerStyle);
            createExcelHeaderCell(headerRow, 5, "Risk Factors", headerStyle);

            // Data rows
            for (Student student : students) {
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(student.getStudentId() != null ? student.getStudentId() : "");
                dataRow.createCell(1).setCellValue(student.getFullName());
                dataRow.createCell(2).setCellValue(student.getGradeLevel() != null ?
                        student.getGradeLevel().toString() : "N/A");
                dataRow.createCell(3).setCellValue(student.getCurrentGPA() != null ?
                        String.format("%.2f", student.getCurrentGPA()) : "N/A");
                dataRow.createCell(4).setCellValue(determineRiskLevel(student));
                dataRow.createCell(5).setCellValue(getRiskFactors(student));
            }

            // Auto-size columns
            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);

            log.info("At-Risk Students Excel generated successfully");
            return baos.toByteArray();
        }
    }

    /**
     * Export honor roll students list to Excel
     *
     * @param students List of honor roll students
     * @return Excel file as byte array
     */
    public byte[] exportHonorRollExcel(List<Student> students) throws IOException {
        log.info("Generating Honor Roll Excel report: {} students", students.size());

        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = createExcelHeaderStyle(workbook);
            CellStyle titleStyle = createExcelTitleStyle(workbook);

            Sheet sheet = workbook.createSheet("Honor Roll");
            int rowNum = 0;

            // Title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Honor Roll Report");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

            // Generated date
            Row dateRow = sheet.createRow(rowNum++);
            dateRow.createCell(0).setCellValue("Generated: " + LocalDateTime.now().format(DATETIME_FORMATTER));
            dateRow.createCell(3).setCellValue("Total Students: " + students.size());
            rowNum++; // Empty row

            // Headers
            Row headerRow = sheet.createRow(rowNum++);
            createExcelHeaderCell(headerRow, 0, "Student ID", headerStyle);
            createExcelHeaderCell(headerRow, 1, "Name", headerStyle);
            createExcelHeaderCell(headerRow, 2, "Grade", headerStyle);
            createExcelHeaderCell(headerRow, 3, "GPA", headerStyle);
            createExcelHeaderCell(headerRow, 4, "Honor Tier", headerStyle);

            // Data rows
            for (Student student : students) {
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(student.getStudentId() != null ? student.getStudentId() : "");
                dataRow.createCell(1).setCellValue(student.getFullName());
                dataRow.createCell(2).setCellValue(student.getGradeLevel() != null ?
                        student.getGradeLevel().toString() : "N/A");
                dataRow.createCell(3).setCellValue(student.getCurrentGPA() != null ?
                        String.format("%.2f", student.getCurrentGPA()) : "N/A");
                dataRow.createCell(4).setCellValue(determineHonorTier(student));
            }

            // Auto-size columns
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);

            log.info("Honor Roll Excel generated successfully");
            return baos.toByteArray();
        }
    }

    // ========================================================================
    // COUNSELING REFERRALS EXPORT
    // ========================================================================

    /**
     * Export counseling referrals to Excel
     *
     * @param referrals List of counseling referrals
     * @param startDate Report period start date
     * @param endDate Report period end date
     * @return Excel file as byte array
     */
    public byte[] exportCounselingReferralsExcel(List<CounselingReferral> referrals,
                                                  LocalDate startDate, LocalDate endDate) throws IOException {
        log.info("Generating Counseling Referrals Excel report: {} referrals", referrals.size());

        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = createExcelHeaderStyle(workbook);
            CellStyle titleStyle = createExcelTitleStyle(workbook);

            Sheet sheet = workbook.createSheet("Counseling Referrals");
            int rowNum = 0;

            // Title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Counseling Referrals Report");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

            // Subtitle with date range
            Row subRow = sheet.createRow(rowNum++);
            subRow.createCell(0).setCellValue("Period: " + startDate.format(DATE_FORMATTER) +
                    " to " + endDate.format(DATE_FORMATTER));
            subRow.createCell(4).setCellValue("Generated: " + LocalDateTime.now().format(DATETIME_FORMATTER));
            subRow.createCell(7).setCellValue("Total Referrals: " + referrals.size());
            rowNum++; // Empty row

            // Headers
            Row headerRow = sheet.createRow(rowNum++);
            createExcelHeaderCell(headerRow, 0, "Referral Date", headerStyle);
            createExcelHeaderCell(headerRow, 1, "Student Name", headerStyle);
            createExcelHeaderCell(headerRow, 2, "Grade", headerStyle);
            createExcelHeaderCell(headerRow, 3, "Urgency", headerStyle);
            createExcelHeaderCell(headerRow, 4, "Primary Concern", headerStyle);
            createExcelHeaderCell(headerRow, 5, "Referral Type", headerStyle);
            createExcelHeaderCell(headerRow, 6, "Assigned Counselor", headerStyle);
            createExcelHeaderCell(headerRow, 7, "Status", headerStyle);
            createExcelHeaderCell(headerRow, 8, "Risk Indicators", headerStyle);
            createExcelHeaderCell(headerRow, 9, "Parent Contacted", headerStyle);

            // Data rows
            for (CounselingReferral referral : referrals) {
                Row dataRow = sheet.createRow(rowNum++);

                // Date
                dataRow.createCell(0).setCellValue(referral.getReferralDate() != null ?
                        referral.getReferralDate().format(DATE_FORMATTER) : "");

                // Student Name
                String studentName = referral.getStudent() != null ?
                        referral.getStudent().getFirstName() + " " + referral.getStudent().getLastName() : "Unknown";
                dataRow.createCell(1).setCellValue(studentName);

                // Grade
                String grade = referral.getStudent() != null && referral.getStudent().getGradeLevel() != null ?
                        referral.getStudent().getGradeLevel() : "N/A";
                dataRow.createCell(2).setCellValue(grade);

                // Urgency
                dataRow.createCell(3).setCellValue(referral.getUrgencyLevel() != null ?
                        referral.getUrgencyLevel().toString() : "");

                // Primary Concern
                dataRow.createCell(4).setCellValue(referral.getPrimaryConcern() != null ?
                        referral.getPrimaryConcern().toString().replace("_", " ") : "");

                // Referral Type
                dataRow.createCell(5).setCellValue(referral.getReferralType() != null ?
                        referral.getReferralType().toString().replace("_", " ") : "");

                // Assigned Counselor
                String counselor = referral.getAssignedCounselor() != null ?
                        referral.getAssignedCounselor().getFirstName() + " " +
                                referral.getAssignedCounselor().getLastName() : "Unassigned";
                dataRow.createCell(6).setCellValue(counselor);

                // Status
                dataRow.createCell(7).setCellValue(referral.getReferralStatus() != null ?
                        referral.getReferralStatus().toString() : "");

                // Risk Indicators
                StringBuilder risks = new StringBuilder();
                if (referral.getSuicideRiskIndicated() != null && referral.getSuicideRiskIndicated()) {
                    risks.append("Suicide Risk; ");
                }
                if (referral.getHarmToOthersIndicated() != null && referral.getHarmToOthersIndicated()) {
                    risks.append("Harm to Others; ");
                }
                if (referral.getImmediateSafetyConcerns() != null && referral.getImmediateSafetyConcerns()) {
                    risks.append("Safety Concern; ");
                }
                dataRow.createCell(8).setCellValue(risks.length() > 0 ? risks.toString().trim() : "None");

                // Parent Contacted
                dataRow.createCell(9).setCellValue(referral.isParentContacted() ? "Yes" : "No");
            }

            // Auto-size columns
            for (int i = 0; i < 10; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);

            log.info("Counseling Referrals Excel generated successfully");
            return baos.toByteArray();
        }
    }

    // ========================================================================
    // NETWORK DEVICE EXPORT
    // ========================================================================

    /**
     * Export network devices to CSV
     *
     * @param devices List of network devices
     * @return CSV file as byte array
     */
    public byte[] exportNetworkDevicesCsv(List<NetworkDevice> devices) throws IOException {
        log.info("Generating Network Devices CSV report: {} devices", devices.size());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(UTF8_BOM);

        try (Writer writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {
            // Header
            writeCsvLine(writer, "Device Name", "IP Address", "MAC Address", "Type", "Status",
                    "Location", "Latency (ms)", "Last Ping", "Monitoring Enabled",
                    "Manufacturer", "Model", "Serial Number");

            // Data rows
            for (NetworkDevice device : devices) {
                writeCsvLine(writer,
                        device.getDeviceName() != null ? device.getDeviceName() : "",
                        device.getIpAddress() != null ? device.getIpAddress() : "",
                        device.getMacAddress() != null ? device.getMacAddress() : "",
                        device.getDeviceType() != null ? device.getDeviceType().getDisplayName() : "",
                        device.getStatus() != null ? device.getStatus().getDisplayName() : "",
                        device.getLocation() != null ? device.getLocation() : "",
                        device.getLastPingLatencyMs() != null ? String.valueOf(device.getLastPingLatencyMs()) : "",
                        device.getLastPingTime() != null ? device.getLastPingTime().format(DATETIME_FORMATTER) : "Never",
                        device.getMonitoringEnabled() != null ? (device.getMonitoringEnabled() ? "Yes" : "No") : "N/A",
                        device.getManufacturer() != null ? device.getManufacturer() : "",
                        device.getModel() != null ? device.getModel() : "",
                        device.getSerialNumber() != null ? device.getSerialNumber() : ""
                );
            }

            writer.flush();
        }

        log.info("Network Devices CSV generated successfully");
        return baos.toByteArray();
    }

    /**
     * Export network devices to Excel with summary report
     *
     * @param devices List of network devices
     * @param onlineCount Number of online devices
     * @param offlineCount Number of offline devices
     * @param avgLatency Average latency
     * @return Excel file as byte array
     */
    public byte[] exportNetworkReportExcel(List<NetworkDevice> devices, int onlineCount,
                                            int offlineCount, double avgLatency) throws IOException {
        log.info("Generating Network Report Excel: {} devices", devices.size());

        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = createExcelHeaderStyle(workbook);
            CellStyle titleStyle = createExcelTitleStyle(workbook);

            // Summary Sheet
            Sheet summarySheet = workbook.createSheet("Summary");
            int rowNum = 0;

            Row titleRow = summarySheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Network Status Report");
            titleCell.setCellStyle(titleStyle);
            summarySheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

            Row dateRow = summarySheet.createRow(rowNum++);
            dateRow.createCell(0).setCellValue("Generated: " + LocalDateTime.now().format(DATETIME_FORMATTER));
            rowNum++; // Empty row

            Row headerRow = summarySheet.createRow(rowNum++);
            createExcelHeaderCell(headerRow, 0, "Metric", headerStyle);
            createExcelHeaderCell(headerRow, 1, "Value", headerStyle);

            addExcelDataRow(summarySheet, rowNum++, "Total Devices", String.valueOf(devices.size()), null);
            addExcelDataRow(summarySheet, rowNum++, "Online", String.valueOf(onlineCount), null);
            addExcelDataRow(summarySheet, rowNum++, "Offline", String.valueOf(offlineCount), null);
            addExcelDataRow(summarySheet, rowNum++, "Health Percentage",
                    String.format("%.1f%%", devices.isEmpty() ? 0 : (onlineCount * 100.0 / devices.size())), null);
            addExcelDataRow(summarySheet, rowNum++, "Average Latency", String.format("%.0f ms", avgLatency), null);

            summarySheet.autoSizeColumn(0);
            summarySheet.autoSizeColumn(1);

            // Device List Sheet
            Sheet deviceSheet = workbook.createSheet("All Devices");
            rowNum = 0;

            Row devHeaderRow = deviceSheet.createRow(rowNum++);
            createExcelHeaderCell(devHeaderRow, 0, "Device Name", headerStyle);
            createExcelHeaderCell(devHeaderRow, 1, "IP Address", headerStyle);
            createExcelHeaderCell(devHeaderRow, 2, "MAC Address", headerStyle);
            createExcelHeaderCell(devHeaderRow, 3, "Type", headerStyle);
            createExcelHeaderCell(devHeaderRow, 4, "Status", headerStyle);
            createExcelHeaderCell(devHeaderRow, 5, "Location", headerStyle);
            createExcelHeaderCell(devHeaderRow, 6, "Latency (ms)", headerStyle);
            createExcelHeaderCell(devHeaderRow, 7, "Last Ping", headerStyle);
            createExcelHeaderCell(devHeaderRow, 8, "Monitoring", headerStyle);

            for (NetworkDevice device : devices) {
                Row dataRow = deviceSheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(device.getDeviceName() != null ? device.getDeviceName() : "");
                dataRow.createCell(1).setCellValue(device.getIpAddress() != null ? device.getIpAddress() : "");
                dataRow.createCell(2).setCellValue(device.getMacAddress() != null ? device.getMacAddress() : "");
                dataRow.createCell(3).setCellValue(device.getDeviceType() != null ? device.getDeviceType().getDisplayName() : "");
                dataRow.createCell(4).setCellValue(device.getStatus() != null ? device.getStatus().getDisplayName() : "");
                dataRow.createCell(5).setCellValue(device.getLocation() != null ? device.getLocation() : "");
                dataRow.createCell(6).setCellValue(device.getLastPingLatencyMs() != null ? device.getLastPingLatencyMs() : 0);
                dataRow.createCell(7).setCellValue(device.getLastPingTime() != null ?
                        device.getLastPingTime().format(DATETIME_FORMATTER) : "Never");
                dataRow.createCell(8).setCellValue(device.getMonitoringEnabled() != null ?
                        (device.getMonitoringEnabled() ? "Yes" : "No") : "N/A");
            }

            for (int i = 0; i < 9; i++) {
                deviceSheet.autoSizeColumn(i);
            }

            // Offline Devices Sheet
            Sheet offlineSheet = workbook.createSheet("Offline Devices");
            rowNum = 0;

            Row offHeaderRow = offlineSheet.createRow(rowNum++);
            createExcelHeaderCell(offHeaderRow, 0, "Device Name", headerStyle);
            createExcelHeaderCell(offHeaderRow, 1, "IP Address", headerStyle);
            createExcelHeaderCell(offHeaderRow, 2, "Type", headerStyle);
            createExcelHeaderCell(offHeaderRow, 3, "Location", headerStyle);
            createExcelHeaderCell(offHeaderRow, 4, "Last Seen", headerStyle);

            for (NetworkDevice device : devices) {
                if (device.getStatus() != null && "OFFLINE".equalsIgnoreCase(device.getStatus().name())) {
                    Row dataRow = offlineSheet.createRow(rowNum++);
                    dataRow.createCell(0).setCellValue(device.getDeviceName() != null ? device.getDeviceName() : "");
                    dataRow.createCell(1).setCellValue(device.getIpAddress() != null ? device.getIpAddress() : "");
                    dataRow.createCell(2).setCellValue(device.getDeviceType() != null ? device.getDeviceType().getDisplayName() : "");
                    dataRow.createCell(3).setCellValue(device.getLocation() != null ? device.getLocation() : "");
                    dataRow.createCell(4).setCellValue(device.getLastPingTime() != null ?
                            device.getLastPingTime().format(DATETIME_FORMATTER) : "Unknown");
                }
            }

            for (int i = 0; i < 5; i++) {
                offlineSheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);

            log.info("Network Report Excel generated successfully");
            return baos.toByteArray();
        }
    }

    // ========================================================================
    // PDF HELPER METHODS
    // ========================================================================

    private void addPdfSection(Document document, String title) throws DocumentException {
        Paragraph section = new Paragraph(title, SECTION_FONT);
        section.setSpacingBefore(15);
        section.setSpacingAfter(10);
        document.add(section);
    }

    private void addPdfKeyValue(PdfPTable table, String key, String value) {
        PdfPCell keyCell = new PdfPCell(new Phrase(key, NORMAL_FONT));
        keyCell.setBorder(Rectangle.NO_BORDER);
        keyCell.setPadding(5);
        table.addCell(keyCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private void addPdfTableHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(new BaseColor(220, 220, 220));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addPdfTableCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, NORMAL_FONT));
        cell.setPadding(5);
        table.addCell(cell);
    }

    // ========================================================================
    // EXCEL HELPER METHODS
    // ========================================================================

    private CellStyle createExcelHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createExcelValueStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createExcelTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        return style;
    }

    private void createExcelHeaderCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void addExcelDataRow(Sheet sheet, int rowNum, String label, String value, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }

    private void addExcelSpecialNeedsRow(Sheet sheet, int rowNum, String category,
                                          long count, long total, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(category);
        row.createCell(1).setCellValue(count);
        row.createCell(2).setCellValue(String.format("%.1f%%", count * 100.0 / total));
    }

    // ========================================================================
    // CSV HELPER METHODS
    // ========================================================================

    private void writeCsvLine(Writer writer, String... fields) throws IOException {
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                writer.write(CSV_SEPARATOR);
            }
            writer.write(escapeCsvField(fields[i]));
        }
        writer.write(LINE_SEPARATOR);
    }

    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }

        boolean needsQuoting = field.contains(CSV_SEPARATOR) ||
                field.contains("\"") ||
                field.contains("\n") ||
                field.contains("\r");

        if (!needsQuoting) {
            return field;
        }

        return "\"" + field.replace("\"", "\"\"") + "\"";
    }

    // ========================================================================
    // BUSINESS LOGIC HELPERS
    // ========================================================================

    private String determineRiskLevel(Student student) {
        if (student.getCurrentGPA() == null) {
            return "Unknown";
        }

        double gpa = student.getCurrentGPA();
        if (gpa < 1.0) {
            return "Critical";
        } else if (gpa < 2.0) {
            return "High";
        } else if (gpa < 2.5) {
            return "Moderate";
        } else {
            return "Low";
        }
    }

    private String getRiskFactors(Student student) {
        StringBuilder factors = new StringBuilder();

        if (student.getCurrentGPA() != null && student.getCurrentGPA() < 2.0) {
            factors.append("Low GPA; ");
        }

        if (student.getHasIEP() != null && student.getHasIEP()) {
            factors.append("IEP; ");
        }

        if (student.getHas504Plan() != null && student.getHas504Plan()) {
            factors.append("504 Plan; ");
        }

        return factors.length() > 0 ? factors.toString().trim() : "None identified";
    }

    private String determineHonorTier(Student student) {
        if (student.getCurrentGPA() == null) {
            return "N/A";
        }

        double gpa = student.getCurrentGPA();
        if (gpa >= 4.0) {
            return "High Honors";
        } else if (gpa >= 3.5) {
            return "Honors";
        } else if (gpa >= 3.0) {
            return "Honorable Mention";
        } else {
            return "N/A";
        }
    }

    // ========================================================================
    // FILE EXPORT HELPER
    // ========================================================================

    /**
     * Write byte array to file
     *
     * @param data The data to write
     * @param file The output file
     */
    public void writeToFile(byte[] data, File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
            log.info("File exported successfully to: {}", file.getAbsolutePath());
        }
    }
}
