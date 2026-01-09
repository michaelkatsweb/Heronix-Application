package com.heronix.service;

import com.heronix.model.domain.*;
import com.heronix.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
     * Generate PDF from schedule slots
     *
     * STUB IMPLEMENTATION: Returns HTML content as bytes
     *
     * Production PDF Generation Setup:
     * ================================
     *
     * Option 1: Flying Saucer (Recommended for HTML-to-PDF)
     * ------------------------------------------------------
     * Add to pom.xml:
     * <dependency>
     *     <groupId>org.xhtmlrenderer</groupId>
     *     <artifactId>flying-saucer-pdf</artifactId>
     *     <version>9.1.22</version>
     * </dependency>
     *
     * Implementation:
     * import org.xhtmlrenderer.pdf.ITextRenderer;
     *
     * ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
     * String html = generateHTMLFromSlots(student, schedule, slots);
     * ITextRenderer renderer = new ITextRenderer();
     * renderer.setDocumentFromString(html);
     * renderer.layout();
     * renderer.createPDF(outputStream);
     * return outputStream.toByteArray();
     *
     *
     * Option 2: Apache PDFBox (Low-level PDF creation)
     * -------------------------------------------------
     * Add to pom.xml:
     * <dependency>
     *     <groupId>org.apache.pdfbox</groupId>
     *     <artifactId>pdfbox</artifactId>
     *     <version>2.0.29</version>
     * </dependency>
     *
     * Implementation:
     * PDDocument document = new PDDocument();
     * PDPage page = new PDPage();
     * document.addPage(page);
     * PDPageContentStream contentStream = new PDPageContentStream(document, page);
     * // Draw text, tables, etc.
     * contentStream.close();
     * ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
     * document.save(outputStream);
     * document.close();
     * return outputStream.toByteArray();
     *
     *
     * Option 3: iText 7 (Feature-rich PDF library)
     * ---------------------------------------------
     * Add to pom.xml:
     * <dependency>
     *     <groupId>com.itextpdf</groupId>
     *     <artifactId>itext7-core</artifactId>
     *     <version>7.2.5</version>
     *     <type>pom</type>
     * </dependency>
     *
     * Implementation:
     * ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
     * PdfWriter writer = new PdfWriter(outputStream);
     * PdfDocument pdf = new PdfDocument(writer);
     * Document document = new Document(pdf);
     * // Add paragraphs, tables, etc.
     * document.close();
     * return outputStream.toByteArray();
     *
     *
     * Option 4: OpenPDF (iText fork, LGPL/MPL)
     * -----------------------------------------
     * Add to pom.xml:
     * <dependency>
     *     <groupId>com.github.librepdf</groupId>
     *     <artifactId>openpdf</artifactId>
     *     <version>1.3.30</version>
     * </dependency>
     */
    private byte[] generatePDFFromSlots(Student student, Schedule schedule, List<ScheduleSlot> slots) {
        log.info("Generating PDF schedule (stub mode - returning HTML as bytes)");

        String html = generateHTMLFromSlots(student, schedule, slots);

        // Stub implementation: Return HTML content as bytes
        // In production, implement one of the PDF generation options documented above
        log.debug("PDF generation using HTML stub for student: {}", student.getId());

        return html.getBytes(java.nio.charset.StandardCharsets.UTF_8);
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
