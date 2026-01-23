package com.heronix.service;

import com.heronix.model.DistrictSettings;
import com.heronix.model.domain.*;
import com.heronix.repository.*;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service for printing and generating student schedules in various formats
 * Supports PDF, HTML, and plain text schedule generation
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentSchedulePrintService {

    private final StudentRepository studentRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleSlotRepository scheduleSlotRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final PeriodTimerRepository periodTimerRepository;
    private final DistrictSettingsService districtSettingsService;

    // ========================================================================
    // SCHEDULE GENERATION METHODS
    // ========================================================================

    /**
     * Generate schedule PDF for a single student
     *
     * @param studentId Student ID
     * @param scheduleId Schedule ID
     * @return PDF as byte array
     */
    @Transactional(readOnly = true)
    public byte[] generateSchedulePDF(Long studentId, Long scheduleId) {
        log.info("Generating PDF schedule for student {} - schedule {}", studentId, scheduleId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + scheduleId));

        // Get student's schedule slots
        List<ScheduleSlot> slots = getStudentScheduleSlots(student, schedule);

        // Generate PDF (placeholder - would use iText or similar library)
        return generatePDFFromSlots(student, schedule, slots);
    }

    /**
     * Generate HTML schedule for a single student
     *
     * @param studentId Student ID
     * @param scheduleId Schedule ID
     * @return HTML string
     */
    @Transactional(readOnly = true)
    public String generateScheduleHTML(Long studentId, Long scheduleId) {
        log.info("Generating HTML schedule for student {} - schedule {}", studentId, scheduleId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + scheduleId));

        List<ScheduleSlot> slots = getStudentScheduleSlots(student, schedule);

        return generateHTMLFromSlots(student, schedule, slots);
    }

    /**
     * Generate plain text schedule for a single student
     *
     * @param studentId Student ID
     * @param scheduleId Schedule ID
     * @return Plain text schedule
     */
    @Transactional(readOnly = true)
    public String generateScheduleText(Long studentId, Long scheduleId) {
        log.info("Generating text schedule for student {} - schedule {}", studentId, scheduleId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + scheduleId));

        List<ScheduleSlot> slots = getStudentScheduleSlots(student, schedule);

        return generateTextFromSlots(student, schedule, slots);
    }

    // ========================================================================
    // BULK GENERATION METHODS
    // ========================================================================

    /**
     * Generate schedules for all students in a specific grade level
     *
     * @param gradeLevel Grade level (e.g., "9", "10", "11", "12")
     * @param scheduleId Schedule ID
     * @return Map of student ID to PDF bytes
     */
    @Transactional(readOnly = true)
    public Map<Long, byte[]> generateSchedulesForGrade(String gradeLevel, Long scheduleId) {
        log.info("Generating schedules for grade level {} - schedule {}", gradeLevel, scheduleId);

        List<Student> students = studentRepository.findByGradeLevel(gradeLevel);
        Map<Long, byte[]> schedules = new HashMap<>();

        for (Student student : students) {
            try {
                byte[] pdf = generateSchedulePDF(student.getId(), scheduleId);
                schedules.put(student.getId(), pdf);
            } catch (Exception e) {
                log.error("Failed to generate schedule for student {}", student.getId(), e);
            }
        }

        log.info("Generated {} schedules for grade level {}", schedules.size(), gradeLevel);
        return schedules;
    }

    /**
     * Generate schedules for all students
     *
     * @param scheduleId Schedule ID
     * @return Map of student ID to PDF bytes
     */
    @Transactional(readOnly = true)
    public Map<Long, byte[]> generateSchedulesForAllStudents(Long scheduleId) {
        log.info("Generating schedules for all students - schedule {}", scheduleId);

        List<Student> students = studentRepository.findAll().stream()
                .filter(Student::isActive)
                .collect(Collectors.toList());

        Map<Long, byte[]> schedules = new HashMap<>();

        for (Student student : students) {
            try {
                byte[] pdf = generateSchedulePDF(student.getId(), scheduleId);
                schedules.put(student.getId(), pdf);
            } catch (Exception e) {
                log.error("Failed to generate schedule for student {}", student.getId(), e);
            }
        }

        log.info("Generated {} schedules for all students", schedules.size());
        return schedules;
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Get schedule slots for a student
     */
    private List<ScheduleSlot> getStudentScheduleSlots(Student student, Schedule schedule) {
        // Get all slots for this schedule
        return scheduleSlotRepository.findByScheduleId(schedule.getId()).stream()
                .filter(slot -> slot.getStudents() != null &&
                        slot.getStudents().contains(student))
                .sorted(Comparator.comparing(slot -> {
                    if (slot.getPeriodNumber() != null) {
                        return slot.getPeriodNumber();
                    }
                    return 999;
                }))
                .collect(Collectors.toList());
    }

    /**
     * Generate PDF from schedule slots using iTextPDF
     */
    private byte[] generatePDFFromSlots(Student student, Schedule schedule, List<ScheduleSlot> slots) {
        log.info("Generating PDF schedule for student: {}", student.getId());

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.LETTER);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Get district settings for header
            DistrictSettings settings = districtSettingsService.getOrCreateDistrictSettings();

            // Title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Student Schedule", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // School/District info
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12);
            Paragraph schoolInfo = new Paragraph(settings.getDistrictNameOrDefault(), headerFont);
            schoolInfo.setAlignment(Element.ALIGN_CENTER);
            document.add(schoolInfo);

            document.add(new Paragraph(" ")); // Spacer

            // Student info section
            Font labelFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
            Font valueFont = new Font(Font.FontFamily.HELVETICA, 10);

            PdfPTable infoTable = new PdfPTable(4);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{15, 35, 15, 35});

            addInfoCell(infoTable, "Student:", labelFont);
            addInfoCell(infoTable, student.getFullName(), valueFont);
            addInfoCell(infoTable, "Student ID:", labelFont);
            addInfoCell(infoTable, student.getStudentId(), valueFont);

            addInfoCell(infoTable, "Grade:", labelFont);
            addInfoCell(infoTable, student.getGradeLevel(), valueFont);
            addInfoCell(infoTable, "School Year:", labelFont);
            addInfoCell(infoTable, schedule.getScheduleName(), valueFont);

            document.add(infoTable);
            document.add(new Paragraph(" ")); // Spacer

            // Schedule table
            PdfPTable scheduleTable = new PdfPTable(5);
            scheduleTable.setWidthPercentage(100);
            scheduleTable.setWidths(new float[]{12, 18, 30, 22, 12});

            // Table header
            Font tableHeaderFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
            BaseColor headerColor = new BaseColor(60, 100, 150); // School blue

            addScheduleHeaderCell(scheduleTable, "Period", tableHeaderFont, headerColor);
            addScheduleHeaderCell(scheduleTable, "Time", tableHeaderFont, headerColor);
            addScheduleHeaderCell(scheduleTable, "Course", tableHeaderFont, headerColor);
            addScheduleHeaderCell(scheduleTable, "Teacher", tableHeaderFont, headerColor);
            addScheduleHeaderCell(scheduleTable, "Room", tableHeaderFont, headerColor);

            // Table data
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 9);
            boolean alternateRow = false;

            for (ScheduleSlot slot : slots) {
                BaseColor rowColor = alternateRow ? new BaseColor(240, 240, 245) : BaseColor.WHITE;

                // Period
                String period = slot.getPeriodNumber() != null ? String.valueOf(slot.getPeriodNumber()) : "-";
                addScheduleDataCell(scheduleTable, period, cellFont, rowColor);

                // Time
                String time = (slot.getStartTime() != null && slot.getEndTime() != null) ?
                        slot.getStartTime() + " - " + slot.getEndTime() : "-";
                addScheduleDataCell(scheduleTable, time, cellFont, rowColor);

                // Course
                String course = "";
                if (slot.getCourse() != null) {
                    Course c = slot.getCourse();
                    course = c.getCourseCode() + "\n" + c.getCourseName();
                }
                addScheduleDataCell(scheduleTable, course, cellFont, rowColor);

                // Teacher
                String teacher = slot.getTeacher() != null ? slot.getTeacher().getName() : "TBD";
                addScheduleDataCell(scheduleTable, teacher, cellFont, rowColor);

                // Room
                String room = slot.getRoom() != null ? slot.getRoom().getRoomNumber() : "TBD";
                addScheduleDataCell(scheduleTable, room, cellFont, rowColor);

                alternateRow = !alternateRow;
            }

            document.add(scheduleTable);

            // Footer
            document.add(new Paragraph(" "));
            Font footerFont = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY);
            Paragraph footer = new Paragraph(
                    "Generated on " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")) +
                    " | Heronix Student Information System",
                    footerFont
            );
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return baos.toByteArray();

        } catch (DocumentException e) {
            log.error("Error generating PDF for student {}: {}", student.getId(), e.getMessage(), e);
            // Fall back to HTML as bytes
            return generateHTMLFromSlots(student, schedule, slots).getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    /**
     * Add a cell to the info table (no borders)
     */
    private void addInfoCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3);
        table.addCell(cell);
    }

    /**
     * Add a header cell to the schedule table
     */
    private void addScheduleHeaderCell(PdfPTable table, String text, Font font, BaseColor bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        cell.setBorderWidth(0.5f);
        table.addCell(cell);
    }

    /**
     * Add a data cell to the schedule table
     */
    private void addScheduleDataCell(PdfPTable table, String text, Font font, BaseColor bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(5);
        cell.setBorderWidth(0.5f);
        table.addCell(cell);
    }

    /**
     * Generate HTML from schedule slots
     */
    private String generateHTMLFromSlots(Student student, Schedule schedule, List<ScheduleSlot> slots) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n<head>\n");
        html.append("<meta charset='UTF-8'>\n");
        html.append("<title>Student Schedule - ").append(student.getFullName()).append("</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }\n");
        html.append("h1 { color: #333; }\n");
        html.append("table { border-collapse: collapse; width: 100%; margin-top: 20px; }\n");
        html.append("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }\n");
        html.append("th { background-color: #4CAF50; color: white; }\n");
        html.append("tr:nth-child(even) { background-color: #f2f2f2; }\n");
        html.append(".header { margin-bottom: 20px; }\n");
        html.append(".info { color: #666; margin: 5px 0; }\n");
        html.append("</style>\n");
        html.append("</head>\n<body>\n");

        // Header
        html.append("<div class='header'>\n");
        html.append("<h1>Student Schedule</h1>\n");
        html.append("<p class='info'><strong>Student:</strong> ").append(student.getFullName()).append("</p>\n");
        html.append("<p class='info'><strong>Student ID:</strong> ").append(student.getStudentId()).append("</p>\n");
        html.append("<p class='info'><strong>Grade:</strong> ").append(student.getGradeLevel()).append("</p>\n");
        html.append("<p class='info'><strong>Academic Year:</strong> ").append(schedule.getScheduleName()).append("</p>\n");
        html.append("<p class='info'><strong>Generated:</strong> ")
                .append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))).append("</p>\n");
        html.append("</div>\n");

        // Schedule table
        html.append("<table>\n");
        html.append("<thead>\n<tr>\n");
        html.append("<th>Period</th>\n");
        html.append("<th>Time</th>\n");
        html.append("<th>Course</th>\n");
        html.append("<th>Teacher</th>\n");
        html.append("<th>Room</th>\n");
        html.append("</tr>\n</thead>\n<tbody>\n");

        for (ScheduleSlot slot : slots) {
            html.append("<tr>\n");

            // Period
            html.append("<td>");
            if (slot.getPeriodNumber() != null) {
                html.append("Period ").append(slot.getPeriodNumber());
            } else {
                html.append("N/A");
            }
            html.append("</td>\n");

            // Time
            html.append("<td>");
            if (slot.getStartTime() != null && slot.getEndTime() != null) {
                html.append(slot.getStartTime())
                        .append(" - ")
                        .append(slot.getEndTime());
            } else {
                html.append("N/A");
            }
            html.append("</td>\n");

            // Course
            html.append("<td>");
            if (slot.getCourse() != null) {
                Course course = slot.getCourse();
                html.append("<strong>").append(course.getCourseCode()).append("</strong><br>");
                html.append(course.getCourseName());
            } else {
                html.append("N/A");
            }
            html.append("</td>\n");

            // Teacher
            html.append("<td>");
            if (slot.getTeacher() != null) {
                html.append(slot.getTeacher().getName());
            } else {
                html.append("TBD");
            }
            html.append("</td>\n");

            // Room
            html.append("<td>");
            if (slot.getRoom() != null) {
                html.append(slot.getRoom().getRoomNumber());
            } else {
                html.append("TBD");
            }
            html.append("</td>\n");

            html.append("</tr>\n");
        }

        html.append("</tbody>\n</table>\n");

        // Footer
        html.append("<p style='margin-top: 30px; color: #666; font-size: 12px;'>");
        html.append("Generated by Heronix SIS on ")
                .append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        html.append("</p>\n");

        html.append("</body>\n</html>");

        return html.toString();
    }

    /**
     * Generate plain text from schedule slots
     */
    private String generateTextFromSlots(Student student, Schedule schedule, List<ScheduleSlot> slots) {
        StringBuilder text = new StringBuilder();

        text.append("=" .repeat(80)).append("\n");
        text.append("STUDENT SCHEDULE\n");
        text.append("=".repeat(80)).append("\n\n");

        text.append("Student: ").append(student.getFullName()).append("\n");
        text.append("Student ID: ").append(student.getStudentId()).append("\n");
        text.append("Grade: ").append(student.getGradeLevel()).append("\n");
        text.append("Academic Year: ").append(schedule.getScheduleName()).append("\n");
        text.append("Generated: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))).append("\n\n");

        text.append("-".repeat(80)).append("\n");
        text.append(String.format("%-10s %-15s %-30s %-20s %-10s\n", "PERIOD", "TIME", "COURSE", "TEACHER", "ROOM"));
        text.append("-".repeat(80)).append("\n");

        for (ScheduleSlot slot : slots) {
            String period = slot.getPeriodNumber() != null ? "Period " + slot.getPeriodNumber() : "N/A";
            String time = (slot.getStartTime() != null && slot.getEndTime() != null) ?
                    slot.getStartTime() + "-" + slot.getEndTime() : "N/A";
            String course = "";
            if (slot.getCourse() != null) {
                Course c = slot.getCourse();
                course = c.getCourseCode() + " - " + c.getCourseName();
            }
            String teacher = slot.getTeacher() != null ? slot.getTeacher().getName() : "TBD";
            String room = slot.getRoom() != null ? slot.getRoom().getRoomNumber() : "TBD";

            text.append(String.format("%-10s %-15s %-30s %-20s %-10s\n",
                    truncate(period, 10),
                    truncate(time, 15),
                    truncate(course, 30),
                    truncate(teacher, 20),
                    truncate(room, 10)));
        }

        text.append("-".repeat(80)).append("\n\n");
        text.append("Generated by Heronix SIS on ")
                .append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))).append("\n");
        text.append("=".repeat(80)).append("\n");

        return text.toString();
    }

    /**
     * Truncate string to max length
     */
    private String truncate(String str, int maxLength) {
        if (str == null) {
            return "";
        }
        return str.length() <= maxLength ? str : str.substring(0, maxLength - 3) + "...";
    }
}
