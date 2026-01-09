package com.heronix.controller.api;

import com.heronix.model.domain.*;
import com.heronix.model.enums.ExportFormat;
import com.heronix.repository.*;
import com.heronix.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST API Controller for Export Operations
 *
 * Provides endpoints for exporting schedules, teachers, students, courses, and rooms
 * to various formats (PDF, Excel, CSV, iCal, HTML, JSON).
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/exports")
@RequiredArgsConstructor
public class ExportApiController {

    private final ExportService exportService;
    private final ScheduleRepository scheduleRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final RoomRepository roomRepository;
    private final EventRepository eventRepository;

    // ==================== Schedule Exports ====================

    @GetMapping("/schedule/{scheduleId}")
    public ResponseEntity<byte[]> exportSchedule(
            @PathVariable Long scheduleId,
            @RequestParam(defaultValue = "PDF") ExportFormat format) {

        try {
            byte[] exportData = exportService.exportSchedule(scheduleId, format);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(getContentType(format));
            headers.setContentDispositionFormData("attachment", "schedule_" + scheduleId + getFileExtension(format));

            return ResponseEntity.ok()
                .headers(headers)
                .body(exportData);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/schedule/{scheduleId}/pdf")
    public ResponseEntity<byte[]> exportScheduleToPDF(@PathVariable Long scheduleId) {
        return exportSchedule(scheduleId, ExportFormat.PDF);
    }

    @GetMapping("/schedule/{scheduleId}/excel")
    public ResponseEntity<byte[]> exportScheduleToExcel(@PathVariable Long scheduleId) {
        return exportSchedule(scheduleId, ExportFormat.EXCEL);
    }

    @GetMapping("/schedule/{scheduleId}/csv")
    public ResponseEntity<byte[]> exportScheduleToCSV(@PathVariable Long scheduleId) {
        return exportSchedule(scheduleId, ExportFormat.CSV);
    }

    @GetMapping("/schedule/{scheduleId}/ical")
    public ResponseEntity<byte[]> exportScheduleToICal(@PathVariable Long scheduleId) {
        return exportSchedule(scheduleId, ExportFormat.ICAL);
    }

    @GetMapping("/schedule/{scheduleId}/html")
    public ResponseEntity<byte[]> exportScheduleToHTML(@PathVariable Long scheduleId) {
        return exportSchedule(scheduleId, ExportFormat.HTML);
    }

    @GetMapping("/schedule/{scheduleId}/json")
    public ResponseEntity<byte[]> exportScheduleToJSON(@PathVariable Long scheduleId) {
        return exportSchedule(scheduleId, ExportFormat.JSON);
    }

    // ==================== Teacher Schedule Exports ====================

    @GetMapping("/teacher/{teacherId}/schedule")
    public ResponseEntity<byte[]> exportTeacherSchedule(
            @PathVariable Long teacherId,
            @RequestParam(defaultValue = "PDF") ExportFormat format) {

        try {
            byte[] exportData = exportService.exportTeacherSchedule(teacherId, format);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(getContentType(format));
            headers.setContentDispositionFormData("attachment", "teacher_" + teacherId + "_schedule" + getFileExtension(format));

            return ResponseEntity.ok()
                .headers(headers)
                .body(exportData);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== Room Schedule Exports ====================

    @GetMapping("/room/{roomId}/schedule")
    public ResponseEntity<byte[]> exportRoomSchedule(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "PDF") ExportFormat format) {

        try {
            byte[] exportData = exportService.exportRoomSchedule(roomId, format);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(getContentType(format));
            headers.setContentDispositionFormData("attachment", "room_" + roomId + "_schedule" + getFileExtension(format));

            return ResponseEntity.ok()
                .headers(headers)
                .body(exportData);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== Teachers Export ====================

    @GetMapping("/teachers/excel")
    public ResponseEntity<byte[]> exportTeachersToExcel() {
        try {
            List<Teacher> teachers = teacherRepository.findAll();
            byte[] exportData = exportService.exportTeachersToExcel(teachers);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "teachers.xlsx");

            return ResponseEntity.ok()
                .headers(headers)
                .body(exportData);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/teachers/csv")
    public ResponseEntity<byte[]> exportTeachersToCSV() {
        try {
            List<Teacher> teachers = teacherRepository.findAll();
            byte[] exportData = exportService.exportTeachersToCSV(teachers);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "teachers.csv");

            return ResponseEntity.ok()
                .headers(headers)
                .body(exportData);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== Students Export ====================

    @GetMapping("/students/excel")
    public ResponseEntity<byte[]> exportStudentsToExcel() {
        try {
            List<Student> students = studentRepository.findAll();
            byte[] exportData = exportService.exportStudentsToExcel(students);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "students.xlsx");

            return ResponseEntity.ok()
                .headers(headers)
                .body(exportData);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/students/csv")
    public ResponseEntity<byte[]> exportStudentsToCSV() {
        try {
            List<Student> students = studentRepository.findAll();
            byte[] exportData = exportService.exportStudentsToCSV(students);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "students.csv");

            return ResponseEntity.ok()
                .headers(headers)
                .body(exportData);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== Courses Export ====================

    @GetMapping("/courses/excel")
    public ResponseEntity<byte[]> exportCoursesToExcel() {
        try {
            List<Course> courses = courseRepository.findAll();
            byte[] exportData = exportService.exportCoursesToExcel(courses);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "courses.xlsx");

            return ResponseEntity.ok()
                .headers(headers)
                .body(exportData);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/courses/csv")
    public ResponseEntity<byte[]> exportCoursesToCSV() {
        try {
            List<Course> courses = courseRepository.findAll();
            byte[] exportData = exportService.exportCoursesToCSV(courses);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "courses.csv");

            return ResponseEntity.ok()
                .headers(headers)
                .body(exportData);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== Rooms Export ====================

    @GetMapping("/rooms/excel")
    public ResponseEntity<byte[]> exportRoomsToExcel() {
        try {
            List<Room> rooms = roomRepository.findAll();
            byte[] exportData = exportService.exportRoomsToExcel(rooms);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "rooms.xlsx");

            return ResponseEntity.ok()
                .headers(headers)
                .body(exportData);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/rooms/csv")
    public ResponseEntity<byte[]> exportRoomsToCSV() {
        try {
            List<Room> rooms = roomRepository.findAll();
            byte[] exportData = exportService.exportRoomsToCSV(rooms);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "rooms.csv");

            return ResponseEntity.ok()
                .headers(headers)
                .body(exportData);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== Events Export ====================

    @GetMapping("/events/ical")
    public ResponseEntity<byte[]> exportEventsToICal() {
        try {
            List<Event> events = eventRepository.findAll();
            byte[] exportData = exportService.exportEventsToICal(events);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/calendar"));
            headers.setContentDispositionFormData("attachment", "events.ics");

            return ResponseEntity.ok()
                .headers(headers)
                .body(exportData);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/events/csv")
    public ResponseEntity<byte[]> exportEventsToCSV() {
        try {
            List<Event> events = eventRepository.findAll();
            byte[] exportData = exportService.exportEventsToCSV(events);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "events.csv");

            return ResponseEntity.ok()
                .headers(headers)
                .body(exportData);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== Dashboard & Metadata ====================

    @GetMapping("/formats")
    public ResponseEntity<Map<String, Object>> getSupportedFormats() {
        Map<String, Object> formats = new HashMap<>();
        formats.put("schedules", List.of("PDF", "EXCEL", "CSV", "ICAL", "HTML", "JSON"));
        formats.put("teachers", List.of("EXCEL", "CSV"));
        formats.put("students", List.of("EXCEL", "CSV"));
        formats.put("courses", List.of("EXCEL", "CSV"));
        formats.put("rooms", List.of("EXCEL", "CSV"));
        formats.put("events", List.of("ICAL", "CSV"));

        return ResponseEntity.ok(formats);
    }

    // ==================== Helper Methods ====================

    private MediaType getContentType(ExportFormat format) {
        return switch (format) {
            case PDF -> MediaType.APPLICATION_PDF;
            case EXCEL -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case CSV -> MediaType.parseMediaType("text/csv");
            case ICAL -> MediaType.parseMediaType("text/calendar");
            case HTML -> MediaType.TEXT_HTML;
            case JSON -> MediaType.APPLICATION_JSON;
        };
    }

    private String getFileExtension(ExportFormat format) {
        return switch (format) {
            case PDF -> ".pdf";
            case EXCEL -> ".xlsx";
            case CSV -> ".csv";
            case ICAL -> ".ics";
            case HTML -> ".html";
            case JSON -> ".json";
        };
    }
}
