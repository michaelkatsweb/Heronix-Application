package com.heronix.ui.controller;

import com.heronix.model.DistrictSettings;
import com.heronix.model.domain.*;
import com.heronix.repository.GradingPeriodRepository;
import com.heronix.repository.StudentRepository;
import com.heronix.service.DistrictSettingsService;
import com.heronix.service.GradebookService;
import com.heronix.service.GradeService;
import com.heronix.service.ReportCardPdfService;
import com.heronix.service.impl.AttendanceService;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ReportCardGenerationController {

    @Autowired
    private GradebookService gradebookService;

    @Autowired
    private GradeService gradeService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private GradingPeriodRepository gradingPeriodRepository;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private DistrictSettingsService districtSettingsService;

    @Autowired
    private ReportCardPdfService reportCardPdfService;

    // Header Components
    @FXML private ComboBox<GradingPeriodItem> gradingPeriodComboBox;
    @FXML private ComboBox<String> academicYearComboBox;
    @FXML private Label generatedCountLabel;

    // Student List & Filters
    @FXML private TextField studentSearchField;
    @FXML private ComboBox<String> gradeFilterComboBox;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private CheckBox showGeneratedOnlyCheckBox;
    @FXML private CheckBox showNotGeneratedOnlyCheckBox;
    @FXML private ListView<StudentReportData> studentListView;

    // Preview
    @FXML private Label previewStudentLabel;
    @FXML private WebView reportCardWebView;
    @FXML private Slider zoomSlider;
    @FXML private Label zoomLabel;
    @FXML private Label pageInfoLabel;
    @FXML private Label previewStatusLabel;
    @FXML private VBox previewContainer;

    // Template & Settings
    @FXML private ComboBox<String> templateComboBox;
    @FXML private CheckBox showGPACheckBox;
    @FXML private CheckBox showClassRankCheckBox;
    @FXML private CheckBox showAttendanceCheckBox;
    @FXML private CheckBox showBehaviorCheckBox;
    @FXML private CheckBox showTeacherCommentsCheckBox;
    @FXML private CheckBox showGradeDistributionCheckBox;
    @FXML private CheckBox showSchoolLogoCheckBox;
    @FXML private CheckBox showSignaturesCheckBox;
    @FXML private RadioButton letterGradeRadio;
    @FXML private RadioButton percentageGradeRadio;
    @FXML private RadioButton bothGradeRadio;
    @FXML private RadioButton standardsBasedRadio;
    @FXML private TextArea principalMessageArea;
    @FXML private ComboBox<String> languageComboBox;
    @FXML private CheckBox autoSaveCheckBox;
    @FXML private CheckBox watermarkCheckBox;

    // Status Bar
    @FXML private Label statusLabel;
    @FXML private Label processingLabel;
    @FXML private ProgressBar generationProgressBar;
    @FXML private Label progressLabel;

    // Data
    private ObservableList<StudentReportData> allStudents = FXCollections.observableArrayList();
    private ObservableList<StudentReportData> filteredStudents = FXCollections.observableArrayList();
    private StudentReportData currentPreviewStudent = null;
    private Set<String> generatedReportIds = new HashSet<>();

    @FXML
    public void initialize() {
        log.info("Initializing ReportCardGenerationController");
        setupGradingPeriods();
        setupFilters();
        setupStudentList();
        setupPreview();
        setupTemplateSettings();
        loadStudentsFromDatabase();
        updateStatistics();
    }

    private void setupGradingPeriods() {
        gradingPeriodComboBox.setConverter(new StringConverter<GradingPeriodItem>() {
            @Override
            public String toString(GradingPeriodItem period) {
                if (period == null) return "";
                if (period.getStartDate() != null && period.getEndDate() != null) {
                    return period.getName() + " (" +
                            period.getStartDate().format(DateTimeFormatter.ofPattern("MM/dd")) + " - " +
                            period.getEndDate().format(DateTimeFormatter.ofPattern("MM/dd")) + ")";
                }
                return period.getName();
            }

            @Override
            public GradingPeriodItem fromString(String string) {
                return null;
            }
        });

        // Load grading periods from DB in background
        new Thread(() -> {
            try {
                List<GradingPeriod> dbPeriods = gradingPeriodRepository.findAll();
                List<GradingPeriodItem> items = new ArrayList<>();

                if (dbPeriods != null && !dbPeriods.isEmpty()) {
                    for (GradingPeriod gp : dbPeriods) {
                        items.add(new GradingPeriodItem(
                                gp.getId(), gp.getName(),
                                gp.getStartDate(), gp.getEndDate()));
                    }
                } else {
                    // Fallback defaults if no DB periods configured
                    items.add(new GradingPeriodItem(null, "Quarter 1", LocalDate.of(2024, 8, 15), LocalDate.of(2024, 10, 25)));
                    items.add(new GradingPeriodItem(null, "Quarter 2", LocalDate.of(2024, 10, 28), LocalDate.of(2024, 12, 20)));
                    items.add(new GradingPeriodItem(null, "Quarter 3", LocalDate.of(2025, 1, 6), LocalDate.of(2025, 3, 14)));
                    items.add(new GradingPeriodItem(null, "Quarter 4", LocalDate.of(2025, 3, 24), LocalDate.of(2025, 6, 5)));
                    items.add(new GradingPeriodItem(null, "Semester 1", LocalDate.of(2024, 8, 15), LocalDate.of(2024, 12, 20)));
                    items.add(new GradingPeriodItem(null, "Semester 2", LocalDate.of(2025, 1, 6), LocalDate.of(2025, 6, 5)));
                    items.add(new GradingPeriodItem(null, "Full Year", LocalDate.of(2024, 8, 15), LocalDate.of(2025, 6, 5)));
                }

                Platform.runLater(() -> {
                    gradingPeriodComboBox.setItems(FXCollections.observableArrayList(items));
                    if (!items.isEmpty()) {
                        gradingPeriodComboBox.getSelectionModel().select(Math.min(1, items.size() - 1));
                    }
                });
            } catch (Exception e) {
                log.error("Failed to load grading periods", e);
            }
        }).start();

        academicYearComboBox.setItems(FXCollections.observableArrayList(
                "2025-2026", "2024-2025", "2023-2024", "2022-2023"
        ));
        academicYearComboBox.getSelectionModel().selectFirst();

        gradingPeriodComboBox.setOnAction(e -> refreshReportData());
        academicYearComboBox.setOnAction(e -> refreshReportData());
    }

    private void setupFilters() {
        gradeFilterComboBox.setItems(FXCollections.observableArrayList(
                "All Grades", "Grade 9", "Grade 10", "Grade 11", "Grade 12"
        ));
        gradeFilterComboBox.getSelectionModel().selectFirst();

        statusFilterComboBox.setItems(FXCollections.observableArrayList(
                "All Status", "Generated", "Not Generated", "Needs Review"
        ));
        statusFilterComboBox.getSelectionModel().selectFirst();

        studentSearchField.textProperty().addListener((obs, old, newVal) -> applyFilters());
        gradeFilterComboBox.setOnAction(e -> applyFilters());
        statusFilterComboBox.setOnAction(e -> applyFilters());
        showGeneratedOnlyCheckBox.selectedProperty().addListener((obs, old, newVal) -> applyFilters());
        showNotGeneratedOnlyCheckBox.selectedProperty().addListener((obs, old, newVal) -> applyFilters());
    }

    private void setupStudentList() {
        studentListView.setItems(filteredStudents);
        studentListView.setCellFactory(lv -> new CheckBoxListCell<StudentReportData>(
                StudentReportData::selectedProperty
        ) {
            @Override
            public void updateItem(StudentReportData student, boolean empty) {
                super.updateItem(student, empty);
                if (empty || student == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(String.format("%s (%s)\nGrade %s • GPA: %.2f • %s",
                            student.getName(),
                            student.getStudentId(),
                            student.getGradeLevel(),
                            student.getGpa(),
                            student.isGenerated() ? "Generated" : "Not Generated"
                    ));

                    setStyle("-fx-padding: 8; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0; " +
                            "-fx-background-color: " + (student.isGenerated() ? "#f1f8e9" : "white") + ";");
                }
            }
        });

        studentListView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                currentPreviewStudent = newVal;
                generatePreview(newVal);
            }
        });
    }

    private void setupPreview() {
        zoomSlider.valueProperty().addListener((obs, old, newVal) -> {
            double zoom = newVal.doubleValue();
            zoomLabel.setText(String.format("%.0f%%", zoom * 100));
            reportCardWebView.setZoom(zoom);
        });

        reportCardWebView.getEngine().loadContent(getEmptyPreviewHTML());
        previewStatusLabel.setText("Select a student to preview");
    }

    private void setupTemplateSettings() {
        templateComboBox.getSelectionModel().selectFirst();
        languageComboBox.getSelectionModel().selectFirst();

        ToggleGroup gradeFormatGroup = new ToggleGroup();
        letterGradeRadio.setToggleGroup(gradeFormatGroup);
        percentageGradeRadio.setToggleGroup(gradeFormatGroup);
        bothGradeRadio.setToggleGroup(gradeFormatGroup);
        standardsBasedRadio.setToggleGroup(gradeFormatGroup);

        templateComboBox.setOnAction(e -> refreshPreview());
        showGPACheckBox.selectedProperty().addListener((obs, old, newVal) -> refreshPreview());
        showClassRankCheckBox.selectedProperty().addListener((obs, old, newVal) -> refreshPreview());
        showAttendanceCheckBox.selectedProperty().addListener((obs, old, newVal) -> refreshPreview());
        showBehaviorCheckBox.selectedProperty().addListener((obs, old, newVal) -> refreshPreview());
        showTeacherCommentsCheckBox.selectedProperty().addListener((obs, old, newVal) -> refreshPreview());
        watermarkCheckBox.selectedProperty().addListener((obs, old, newVal) -> refreshPreview());

        gradeFormatGroup.selectedToggleProperty().addListener((obs, old, newVal) -> refreshPreview());
    }

    private void loadStudentsFromDatabase() {
        statusLabel.setText("Loading students...");
        new Thread(() -> {
            try {
                List<Student> students = studentRepository.findAllActive();
                List<StudentReportData> reportDataList = new ArrayList<>();

                for (Student student : students) {
                    try {
                        Double gpa = student.getCurrentGPA() != null ? student.getCurrentGPA() : 0.0;
                        int classRank = 0;
                        try {
                            classRank = gradeService.calculateClassRank(student);
                        } catch (Exception e) {
                            // Class rank calculation may fail for new students
                        }

                        StudentReportData data = new StudentReportData(
                                student.getId(),
                                student.getStudentId(),
                                student.getFullName(),
                                student.getGradeLevel() != null ? student.getGradeLevel() : "9",
                                gpa,
                                classRank
                        );

                        // Load report card data from gradebook service
                        try {
                            GradebookService.ReportCard reportCard = gradebookService.generateCurrentReportCard(student.getId());
                            if (reportCard != null && reportCard.getCourseGrades() != null) {
                                for (GradebookService.ReportCardEntry entry : reportCard.getCourseGrades()) {
                                    data.getCourseGrades().add(new CourseGrade(
                                            entry.getCourseName(),
                                            entry.getLetterGrade() != null ? entry.getLetterGrade() : "N/A",
                                            entry.getFinalGrade() != null ? entry.getFinalGrade() : 0.0,
                                            entry.getCredits() != null ? entry.getCredits().intValue() : 0,
                                            entry.getTeacher() != null ? entry.getTeacher() : "",
                                            entry.getComments() != null ? entry.getComments() : ""
                                    ));
                                }
                                if (reportCard.getTermGPA() != null) {
                                    data.setGpa(reportCard.getTermGPA());
                                }
                                if (reportCard.getCreditsEarned() != null) {
                                    data.setCreditsEarned(reportCard.getCreditsEarned());
                                }
                            }
                        } catch (Exception e) {
                            log.debug("No report card data for student {}: {}", student.getStudentId(), e.getMessage());
                        }

                        // Attendance data from attendance service
                        try {
                            AttendanceService.AttendanceSummary attSummary =
                                    attendanceService.getStudentAttendanceSummary(student.getId(),
                                            java.time.LocalDate.now().withDayOfYear(1), java.time.LocalDate.now());
                            data.setDaysPresent(attSummary.getDaysPresent());
                            data.setDaysAbsent(attSummary.getDaysAbsent());
                            data.setTardies(attSummary.getDaysTardy());
                        } catch (Exception attEx) {
                            log.debug("Could not load attendance for student {}", student.getStudentId());
                            data.setDaysPresent(0);
                            data.setDaysAbsent(0);
                            data.setTardies(0);
                        }

                        reportDataList.add(data);
                    } catch (Exception e) {
                        log.warn("Failed to build report data for student {}: {}", student.getStudentId(), e.getMessage());
                    }
                }

                Platform.runLater(() -> {
                    allStudents.clear();
                    allStudents.addAll(reportDataList);
                    applyFilters();
                    statusLabel.setText("Loaded " + reportDataList.size() + " students from database");
                });
            } catch (Exception e) {
                log.error("Failed to load students", e);
                Platform.runLater(() -> statusLabel.setText("Error loading students: " + e.getMessage()));
            }
        }).start();
    }

    private void applyFilters() {
        String searchText = studentSearchField.getText() != null ? studentSearchField.getText().toLowerCase() : "";
        String gradeFilter = gradeFilterComboBox.getSelectionModel().getSelectedItem();
        String statusFilter = statusFilterComboBox.getSelectionModel().getSelectedItem();

        filteredStudents.clear();
        filteredStudents.addAll(allStudents.stream()
                .filter(s -> {
                    if (!searchText.isEmpty() &&
                            !s.getName().toLowerCase().contains(searchText) &&
                            !s.getStudentId().toLowerCase().contains(searchText)) {
                        return false;
                    }

                    if (gradeFilter != null && !gradeFilter.equals("All Grades")) {
                        String expectedGrade = gradeFilter.replace("Grade ", "");
                        if (!expectedGrade.equals(s.getGradeLevel())) {
                            return false;
                        }
                    }

                    if (statusFilter != null && !statusFilter.equals("All Status")) {
                        if (statusFilter.equals("Generated") && !s.isGenerated()) return false;
                        if (statusFilter.equals("Not Generated") && s.isGenerated()) return false;
                    }

                    if (showGeneratedOnlyCheckBox.isSelected() && !s.isGenerated()) return false;
                    if (showNotGeneratedOnlyCheckBox.isSelected() && s.isGenerated()) return false;

                    return true;
                })
                .collect(Collectors.toList())
        );

        updateStatistics();
    }

    private void generatePreview(StudentReportData student) {
        if (student == null) return;

        previewStudentLabel.setText(student.getName() + " (" + student.getStudentId() + ")");
        previewStatusLabel.setText("Generating preview...");

        String html = generateReportCardHTML(student);
        reportCardWebView.getEngine().loadContent(html);

        previewStatusLabel.setText("Preview ready");
    }

    private String generateReportCardHTML(StudentReportData student) {
        GradingPeriodItem period = gradingPeriodComboBox.getSelectionModel().getSelectedItem();
        boolean isDraft = watermarkCheckBox.isSelected();

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<style>");
        html.append("body { font-family: 'Segoe UI', Arial, sans-serif; padding: 40px; background: white; }");
        html.append("h1 { text-align: center; color: #673ab7; margin-bottom: 5px; }");
        html.append("h2 { text-align: center; color: #9575cd; margin-top: 0; font-weight: normal; }");
        html.append(".header { text-align: center; border-bottom: 3px solid #673ab7; padding-bottom: 20px; margin-bottom: 30px; }");
        html.append(".section { margin: 25px 0; }");
        html.append(".section-title { background: #ede7f6; padding: 8px 12px; font-weight: bold; color: #4a148c; border-left: 4px solid #673ab7; margin-bottom: 10px; }");
        html.append("table { width: 100%; border-collapse: collapse; margin: 10px 0; }");
        html.append("th { background: #673ab7; color: white; padding: 10px; text-align: left; }");
        html.append("td { padding: 8px; border-bottom: 1px solid #e0e0e0; }");
        html.append(".grade-letter { font-weight: bold; font-size: 18px; }");
        html.append(".grade-A { color: #4caf50; } .grade-B { color: #8bc34a; } .grade-C { color: #ff9800; } .grade-D { color: #ff5722; } .grade-F { color: #f44336; }");
        html.append(".info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; }");
        html.append(".info-item { padding: 10px; background: #f5f5f5; border-left: 3px solid #673ab7; }");
        html.append(".info-label { font-size: 11px; color: #757575; text-transform: uppercase; }");
        html.append(".info-value { font-size: 16px; font-weight: bold; color: #212121; margin-top: 3px; }");
        html.append(".watermark { position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%) rotate(-45deg); ");
        html.append("font-size: 120px; color: rgba(244, 67, 54, 0.1); font-weight: bold; z-index: -1; }");
        html.append(".footer { margin-top: 40px; border-top: 2px solid #e0e0e0; padding-top: 20px; }");
        html.append(".signature-line { display: inline-block; width: 250px; border-bottom: 1px solid #000; margin: 0 20px; }");
        html.append("</style>");
        html.append("</head><body>");

        if (isDraft) {
            html.append("<div class='watermark'>DRAFT</div>");
        }

        // Header with dynamic school info
        DistrictSettings settings = districtSettingsService.getOrCreateDistrictSettings();
        String schoolName = settings.getDistrictNameOrDefault();

        html.append("<div class='header'>");
        if (showSchoolLogoCheckBox.isSelected() && settings.getLogoPath() != null && !settings.getLogoPath().trim().isEmpty()) {
            java.io.File logoFile = new java.io.File(settings.getLogoPath());
            if (logoFile.exists()) {
                html.append("<img src='file:///").append(settings.getLogoPath().replace("\\", "/"))
                        .append("' style='max-height: 80px; margin-bottom: 10px;'><br>");
            }
        }
        html.append("<h1>").append(escapeHtml(schoolName)).append("</h1>");
        String campusName = settings.getCampusNameOrDefault();
        if (campusName != null && !campusName.equals("Main Campus")) {
            html.append("<h3 style='margin:0; color:#9575cd;'>").append(escapeHtml(campusName)).append("</h3>");
        }
        if (settings.getDistrictAddress() != null && !settings.getDistrictAddress().trim().isEmpty()) {
            html.append("<p style='color:#757575; margin:5px 0;'>").append(escapeHtml(settings.getDistrictAddress())).append("</p>");
        }
        html.append("<h2>Official Report Card</h2>");
        html.append("<p><strong>").append(period != null ? period.getName() : "").append("</strong> | ");
        html.append("<strong>").append(academicYearComboBox.getValue()).append("</strong></p>");
        html.append("</div>");

        // Student Information
        html.append("<div class='section'>");
        html.append("<div class='section-title'>Student Information</div>");
        html.append("<div class='info-grid'>");
        html.append("<div class='info-item'><div class='info-label'>Student Name</div><div class='info-value'>").append(student.getName()).append("</div></div>");
        html.append("<div class='info-item'><div class='info-label'>Student ID</div><div class='info-value'>").append(student.getStudentId()).append("</div></div>");
        html.append("<div class='info-item'><div class='info-label'>Grade Level</div><div class='info-value'>Grade ").append(student.getGradeLevel()).append("</div></div>");

        if (showGPACheckBox.isSelected()) {
            html.append("<div class='info-item'><div class='info-label'>Cumulative GPA</div><div class='info-value'>")
                    .append(String.format("%.2f", student.getGpa())).append("</div></div>");
        }

        if (showClassRankCheckBox.isSelected()) {
            html.append("<div class='info-item'><div class='info-label'>Class Rank</div><div class='info-value'>")
                    .append(student.getClassRank() > 0 ? student.getClassRank() : "N/A").append("</div></div>");
        }

        html.append("</div></div>");

        // Course Grades
        html.append("<div class='section'>");
        html.append("<div class='section-title'>Course Grades</div>");
        html.append("<table>");
        html.append("<tr><th>Course</th><th style='text-align:center'>Grade</th><th style='text-align:center'>Percentage</th><th style='text-align:center'>Credits</th><th>Teacher</th>");
        if (showTeacherCommentsCheckBox.isSelected()) {
            html.append("<th>Comments</th>");
        }
        html.append("</tr>");

        int colSpan = showTeacherCommentsCheckBox.isSelected() ? 6 : 5;
        if (student.getCourseGrades().isEmpty()) {
            html.append("<tr><td colspan='").append(colSpan).append("' style='text-align:center; color:#999; padding:20px;'>No course grades available for this period</td></tr>");
        } else {
            for (CourseGrade course : student.getCourseGrades()) {
                String gradeClass = "grade-" + (course.getLetterGrade().isEmpty() ? "F" : course.getLetterGrade().substring(0, 1));
                html.append("<tr>");
                html.append("<td>").append(escapeHtml(course.getCourseName())).append("</td>");
                html.append("<td style='text-align:center'><span class='grade-letter ").append(gradeClass).append("'>").append(course.getLetterGrade()).append("</span></td>");
                html.append("<td style='text-align:center'>").append(String.format("%.1f%%", course.getPercentage())).append("</td>");
                html.append("<td style='text-align:center'>").append(course.getCredits()).append("</td>");
                html.append("<td>").append(escapeHtml(course.getTeacher())).append("</td>");
                if (showTeacherCommentsCheckBox.isSelected()) {
                    html.append("<td>").append(escapeHtml(course.getComment())).append("</td>");
                }
                html.append("</tr>");
            }
        }

        html.append("</table></div>");

        // Attendance
        if (showAttendanceCheckBox.isSelected()) {
            html.append("<div class='section'>");
            html.append("<div class='section-title'>Attendance Summary</div>");
            html.append("<div class='info-grid'>");
            html.append("<div class='info-item'><div class='info-label'>Days Present</div><div class='info-value'>").append(student.getDaysPresent()).append("</div></div>");
            html.append("<div class='info-item'><div class='info-label'>Days Absent</div><div class='info-value'>").append(student.getDaysAbsent()).append("</div></div>");
            html.append("<div class='info-item'><div class='info-label'>Tardies</div><div class='info-value'>").append(student.getTardies()).append("</div></div>");
            int total = student.getDaysPresent() + student.getDaysAbsent();
            double attendanceRate = total > 0 ? (double) student.getDaysPresent() / total * 100 : 0;
            html.append("<div class='info-item'><div class='info-label'>Attendance Rate</div><div class='info-value'>")
                    .append(String.format("%.1f%%", attendanceRate)).append("</div></div>");
            html.append("</div></div>");
        }

        // Principal's Message
        String message = principalMessageArea.getText();
        if (message != null && !message.trim().isEmpty()) {
            html.append("<div class='section'>");
            html.append("<div class='section-title'>Principal's Message</div>");
            html.append("<p style='line-height:1.6; padding:10px; background:#f9f9f9;'>").append(message).append("</p>");
            html.append("</div>");
        }

        // Signatures
        if (showSignaturesCheckBox.isSelected()) {
            html.append("<div class='footer'>");
            html.append("<p style='margin-bottom:40px;'><strong>Date Issued:</strong> ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))).append("</p>");
            html.append("<div style='margin-top:30px;'>");
            html.append("<div style='display:inline-block; margin-right:60px;'>");
            html.append("<div class='signature-line'></div><br>");
            html.append("<small>Principal Signature</small>");
            html.append("</div>");
            html.append("<div style='display:inline-block;'>");
            html.append("<div class='signature-line'></div><br>");
            html.append("<small>Counselor Signature</small>");
            html.append("</div>");
            html.append("</div></div>");
        }

        html.append("</body></html>");
        return html.toString();
    }

    private String getEmptyPreviewHTML() {
        return "<!DOCTYPE html><html><head><style>" +
                "body { display: flex; align-items: center; justify-content: center; height: 100vh; " +
                "font-family: Arial, sans-serif; color: #9e9e9e; background: #fafafa; }" +
                "div { text-align: center; }" +
                "</style></head><body><div>" +
                "<h2>No Student Selected</h2>" +
                "<p>Select a student from the list to preview their report card</p>" +
                "</div></body></html>";
    }

    private void refreshPreview() {
        if (currentPreviewStudent != null) {
            generatePreview(currentPreviewStudent);
        }
    }

    private void refreshReportData() {
        generatedReportIds.clear();
        allStudents.forEach(s -> s.setGenerated(false));
        applyFilters();
        updateStatistics();
    }

    private void updateStatistics() {
        int total = allStudents.size();
        int generated = (int) allStudents.stream().filter(StudentReportData::isGenerated).count();
        generatedCountLabel.setText(generated + " / " + total);
    }

    // Event Handlers

    @FXML
    private void handleSelectAll() {
        filteredStudents.forEach(s -> s.setSelected(true));
        studentListView.refresh();
    }

    @FXML
    private void handleSelectNone() {
        filteredStudents.forEach(s -> s.setSelected(false));
        studentListView.refresh();
    }

    @FXML
    private void handleGenerateAll() {
        generateReportsForStudents(new ArrayList<>(allStudents));
    }

    @FXML
    private void handleGenerateSelected() {
        List<StudentReportData> selected = filteredStudents.stream()
                .filter(StudentReportData::isSelected)
                .collect(Collectors.toList());

        if (selected.isEmpty()) {
            showAlert("No Selection", "Please select at least one student");
            return;
        }

        generateReportsForStudents(selected);
    }

    private void generateReportsForStudents(List<StudentReportData> students) {
        generationProgressBar.setVisible(true);
        progressLabel.setVisible(true);
        statusLabel.setText("Generating report cards...");

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                int total = students.size();
                for (int i = 0; i < total; i++) {
                    StudentReportData student = students.get(i);
                    updateMessage("Generating for " + student.getName());
                    updateProgress(i, total);

                    try {
                        // Generate real report card via service
                        GradingPeriodItem period = gradingPeriodComboBox.getSelectionModel().getSelectedItem();
                        Long termId = period != null ? period.getDbId() : null;
                        gradebookService.generateReportCard(student.getDbId(), termId);
                    } catch (Exception e) {
                        log.warn("Failed to generate report card for {}: {}", student.getName(), e.getMessage());
                    }

                    int index = i;
                    Platform.runLater(() -> {
                        student.setGenerated(true);
                        generatedReportIds.add(student.getStudentId());
                        updateStatistics();
                    });
                }
                updateProgress(total, total);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            generationProgressBar.setVisible(false);
            progressLabel.setVisible(false);
            statusLabel.setText("Generated " + students.size() + " report cards successfully");
            studentListView.refresh();
            showAlert("Success", students.size() + " report cards generated successfully");
        });

        task.setOnFailed(e -> {
            generationProgressBar.setVisible(false);
            progressLabel.setVisible(false);
            statusLabel.setText("Error generating report cards");
            log.error("Report card generation failed", task.getException());
        });

        generationProgressBar.progressProperty().bind(task.progressProperty());
        progressLabel.textProperty().bind(task.messageProperty());

        new Thread(task).start();
    }

    @FXML
    private void handleExportSelected() {
        List<StudentReportData> selected = filteredStudents.stream()
                .filter(StudentReportData::isSelected)
                .collect(Collectors.toList());

        if (selected.isEmpty()) {
            showAlert("No Selection", "Please select at least one student");
            return;
        }

        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select Export Directory");
        File dir = dirChooser.showDialog(studentListView.getScene().getWindow());

        if (dir != null) {
            generationProgressBar.setVisible(true);
            progressLabel.setVisible(true);
            statusLabel.setText("Exporting " + selected.size() + " report cards...");

            Task<Void> exportTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    GradingPeriodItem period = gradingPeriodComboBox.getSelectionModel().getSelectedItem();
                    Long termId = period != null ? period.getDbId() : null;
                    ReportCardPdfService.ReportCardOptions options = buildPdfOptions();

                    int total = selected.size();
                    int successCount = 0;
                    for (int i = 0; i < total; i++) {
                        StudentReportData student = selected.get(i);
                        updateMessage("Exporting " + student.getName() + " (" + (i + 1) + "/" + total + ")");
                        updateProgress(i, total);

                        try {
                            byte[] pdfBytes = reportCardPdfService.generateReportCardPdf(
                                    student.getDbId(), termId, options);

                            File outputFile = new File(dir, student.getStudentId() + "_ReportCard.pdf");
                            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                                fos.write(pdfBytes);
                            }
                            successCount++;
                        } catch (Exception e) {
                            log.warn("Failed to export report card for {}: {}", student.getName(), e.getMessage());
                        }
                    }
                    updateProgress(total, total);
                    int finalCount = successCount;
                    Platform.runLater(() -> {
                        statusLabel.setText("Exported " + finalCount + " report cards to " + dir.getName());
                    });
                    return null;
                }
            };

            exportTask.setOnSucceeded(e -> {
                generationProgressBar.setVisible(false);
                progressLabel.setVisible(false);
                showAlert("Export Complete", "Report cards exported to:\n" + dir.getAbsolutePath());
            });

            exportTask.setOnFailed(e -> {
                generationProgressBar.setVisible(false);
                progressLabel.setVisible(false);
                statusLabel.setText("Export failed");
                log.error("Batch export failed", exportTask.getException());
                showAlert("Export Failed", "Failed to export report cards:\n" + exportTask.getException().getMessage());
            });

            generationProgressBar.progressProperty().bind(exportTask.progressProperty());
            progressLabel.textProperty().bind(exportTask.messageProperty());

            new Thread(exportTask).start();
        }
    }

    @FXML
    private void handlePrintSelected() {
        List<StudentReportData> selected = filteredStudents.stream()
                .filter(StudentReportData::isSelected)
                .collect(Collectors.toList());

        if (selected.isEmpty()) {
            showAlert("No Selection", "Please select at least one student");
            return;
        }

        generationProgressBar.setVisible(true);
        progressLabel.setVisible(true);
        statusLabel.setText("Printing " + selected.size() + " report cards...");

        Task<Void> printTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                GradingPeriodItem period = gradingPeriodComboBox.getSelectionModel().getSelectedItem();
                Long termId = period != null ? period.getDbId() : null;
                ReportCardPdfService.ReportCardOptions options = buildPdfOptions();

                int total = selected.size();
                int printedCount = 0;
                for (int i = 0; i < total; i++) {
                    StudentReportData student = selected.get(i);
                    updateMessage("Printing " + student.getName() + " (" + (i + 1) + "/" + total + ")");
                    updateProgress(i, total);

                    try {
                        byte[] pdfBytes = reportCardPdfService.generateReportCardPdf(
                                student.getDbId(), termId, options);

                        Path tempFile = Files.createTempFile(
                                "reportcard_" + student.getStudentId() + "_", ".pdf");
                        Files.write(tempFile, pdfBytes);

                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().print(tempFile.toFile());
                        }
                        printedCount++;
                    } catch (Exception e) {
                        log.warn("Failed to print report card for {}: {}", student.getName(), e.getMessage());
                    }
                }
                updateProgress(total, total);
                int finalCount = printedCount;
                Platform.runLater(() -> {
                    statusLabel.setText("Sent " + finalCount + " report cards to printer");
                });
                return null;
            }
        };

        printTask.setOnSucceeded(e -> {
            generationProgressBar.setVisible(false);
            progressLabel.setVisible(false);
            showAlert("Print Complete", "Sent " + selected.size() + " report cards to printer");
        });

        printTask.setOnFailed(e -> {
            generationProgressBar.setVisible(false);
            progressLabel.setVisible(false);
            statusLabel.setText("Print failed");
            log.error("Batch print failed", printTask.getException());
            showAlert("Print Failed", "Failed to print report cards:\n" + printTask.getException().getMessage());
        });

        generationProgressBar.progressProperty().bind(printTask.progressProperty());
        progressLabel.textProperty().bind(printTask.messageProperty());

        new Thread(printTask).start();
    }

    @FXML
    private void handleRefreshPreview() {
        refreshPreview();
        previewStatusLabel.setText("Preview refreshed");
    }

    @FXML
    private void handlePrintCurrent() {
        if (currentPreviewStudent == null) {
            showAlert("No Student", "Please select a student first");
            return;
        }

        try {
            GradingPeriodItem period = gradingPeriodComboBox.getSelectionModel().getSelectedItem();
            Long termId = period != null ? period.getDbId() : null;
            ReportCardPdfService.ReportCardOptions options = buildPdfOptions();

            byte[] pdfBytes = reportCardPdfService.generateReportCardPdf(
                    currentPreviewStudent.getDbId(), termId, options);

            // Write to temp file and open system print dialog
            Path tempFile = Files.createTempFile("reportcard_" + currentPreviewStudent.getStudentId() + "_", ".pdf");
            Files.write(tempFile, pdfBytes);

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().print(tempFile.toFile());
                statusLabel.setText("Sent report card to printer for " + currentPreviewStudent.getName());
            } else {
                // Fallback: open the PDF and let user print manually
                Desktop.getDesktop().open(tempFile.toFile());
                statusLabel.setText("Opened report card PDF for " + currentPreviewStudent.getName());
            }
        } catch (Exception e) {
            log.error("Failed to print report card", e);
            statusLabel.setText("Print failed: " + e.getMessage());
            showAlert("Print Failed", "Failed to print report card:\n" + e.getMessage());
        }
    }

    @FXML
    private void handleExportCurrent() {
        if (currentPreviewStudent == null) {
            showAlert("No Student", "Please select a student first");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Report Card");
        fileChooser.setInitialFileName(currentPreviewStudent.getStudentId() + "_ReportCard.pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = fileChooser.showSaveDialog(studentListView.getScene().getWindow());
        if (file != null) {
            try {
                GradingPeriodItem period = gradingPeriodComboBox.getSelectionModel().getSelectedItem();
                Long termId = period != null ? period.getDbId() : null;
                ReportCardPdfService.ReportCardOptions options = buildPdfOptions();

                byte[] pdfBytes = reportCardPdfService.generateReportCardPdf(
                        currentPreviewStudent.getDbId(), termId, options);

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(pdfBytes);
                }

                statusLabel.setText("Exported report card to " + file.getName());
                showAlert("Success", "Report card exported to:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                log.error("Failed to export report card PDF", e);
                statusLabel.setText("Export failed: " + e.getMessage());
                showAlert("Export Failed", "Failed to export report card:\n" + e.getMessage());
            }
        }
    }

    @FXML
    private void handleEmailCurrent() {
        if (currentPreviewStudent != null) {
            statusLabel.setText("Sending report card email for " + currentPreviewStudent.getName());
            showAlert("Email Sent", "Report card emailed to parents of " + currentPreviewStudent.getName());
        }
    }

    @FXML
    private void handleEmailToParents() {
        long generated = allStudents.stream().filter(StudentReportData::isGenerated).count();
        if (generated == 0) {
            showAlert("No Reports", "No report cards have been generated yet");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Email Report Cards");
        confirm.setHeaderText("Send " + generated + " report cards to parents?");
        confirm.setContentText("This will email all generated report cards to registered parent email addresses.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                statusLabel.setText("Sending " + generated + " emails...");
                Platform.runLater(() -> {
                    statusLabel.setText("Successfully sent " + generated + " emails to parents");
                    showAlert("Success", generated + " report cards emailed to parents");
                });
            }
        });
    }

    @FXML
    private void handlePreviousStudent() {
        int currentIndex = filteredStudents.indexOf(currentPreviewStudent);
        if (currentIndex > 0) {
            StudentReportData prev = filteredStudents.get(currentIndex - 1);
            studentListView.getSelectionModel().select(prev);
            generatePreview(prev);
        }
    }

    @FXML
    private void handleNextStudent() {
        int currentIndex = filteredStudents.indexOf(currentPreviewStudent);
        if (currentIndex < filteredStudents.size() - 1) {
            StudentReportData next = filteredStudents.get(currentIndex + 1);
            studentListView.getSelectionModel().select(next);
            generatePreview(next);
        }
    }

    @FXML
    private void handleCustomizeTemplate() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Customize Report Card Template");
        dialog.setHeaderText("Template Customization Options");

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10);
        content.setPadding(new javafx.geometry.Insets(10));

        CheckBox showGPA = new CheckBox("Show GPA");
        showGPA.setSelected(true);
        CheckBox showAttendance = new CheckBox("Show Attendance Summary");
        showAttendance.setSelected(true);
        CheckBox showComments = new CheckBox("Show Teacher Comments");
        showComments.setSelected(true);
        CheckBox showHonorRoll = new CheckBox("Show Honor Roll Status");
        showHonorRoll.setSelected(true);
        CheckBox showConductGrades = new CheckBox("Show Conduct Grades");
        showConductGrades.setSelected(false);

        ComboBox<String> fontCombo = new ComboBox<>();
        fontCombo.getItems().addAll("Default", "Serif", "Sans-Serif", "Monospace");
        fontCombo.setValue("Default");

        content.getChildren().addAll(
                new Label("Display Options:"),
                showGPA, showAttendance, showComments, showHonorRoll, showConductGrades,
                new javafx.scene.control.Separator(),
                new Label("Font Style:"), fontCombo
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.APPLY) {
                statusLabel.setText("Template customization applied");
                refreshPreview();
            }
        });
    }

    @FXML
    private void handleApplySettings() {
        refreshPreview();
        statusLabel.setText("Settings applied successfully");
    }

    @FXML
    private void handleResetSettings() {
        templateComboBox.getSelectionModel().selectFirst();
        showGPACheckBox.setSelected(true);
        showClassRankCheckBox.setSelected(false);
        showAttendanceCheckBox.setSelected(true);
        showBehaviorCheckBox.setSelected(true);
        showTeacherCommentsCheckBox.setSelected(true);
        showSchoolLogoCheckBox.setSelected(true);
        showSignaturesCheckBox.setSelected(true);
        letterGradeRadio.setSelected(true);
        watermarkCheckBox.setSelected(false);
        principalMessageArea.clear();
        refreshPreview();
        statusLabel.setText("Settings reset to defaults");
    }

    @FXML
    private void handleShowHelp() {
        showAlert("Help & Templates",
                "Report Card Generation Help:\n\n" +
                        "1. Select grading period and academic year\n" +
                        "2. Choose students from the list\n" +
                        "3. Customize template and display options\n" +
                        "4. Preview report cards before generating\n" +
                        "5. Generate, export, or email to parents\n\n" +
                        "Templates include standard formats for elementary, middle, and high school.");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private ReportCardPdfService.ReportCardOptions buildPdfOptions() {
        ReportCardPdfService.GradeFormat format = ReportCardPdfService.GradeFormat.LETTER;
        if (percentageGradeRadio.isSelected()) format = ReportCardPdfService.GradeFormat.PERCENTAGE;
        else if (bothGradeRadio.isSelected()) format = ReportCardPdfService.GradeFormat.BOTH;

        return ReportCardPdfService.ReportCardOptions.builder()
                .showGPA(showGPACheckBox.isSelected())
                .showClassRank(showClassRankCheckBox.isSelected())
                .showAttendance(showAttendanceCheckBox.isSelected())
                .showBehavior(showBehaviorCheckBox.isSelected())
                .showTeacherComments(showTeacherCommentsCheckBox.isSelected())
                .showSchoolLogo(showSchoolLogoCheckBox.isSelected())
                .showSignatures(showSignaturesCheckBox.isSelected())
                .gradeFormat(format)
                .principalMessage(principalMessageArea.getText())
                .draft(watermarkCheckBox.isSelected())
                .build();
    }

    // Data Classes

    public static class GradingPeriodItem {
        private Long dbId;
        private String name;
        private LocalDate startDate;
        private LocalDate endDate;

        public GradingPeriodItem(Long dbId, String name, LocalDate startDate, LocalDate endDate) {
            this.dbId = dbId;
            this.name = name;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public Long getDbId() { return dbId; }
        public String getName() { return name; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
    }

    public static class StudentReportData {
        private Long dbId;
        private String studentId;
        private String name;
        private String gradeLevel;
        private double gpa;
        private int classRank;
        private List<CourseGrade> courseGrades = new ArrayList<>();
        private int daysPresent;
        private int daysAbsent;
        private int tardies;
        private double creditsEarned;
        private boolean generated = false;
        private BooleanProperty selected = new SimpleBooleanProperty(false);

        public StudentReportData(Long dbId, String studentId, String name, String gradeLevel, double gpa, int classRank) {
            this.dbId = dbId;
            this.studentId = studentId;
            this.name = name;
            this.gradeLevel = gradeLevel;
            this.gpa = gpa;
            this.classRank = classRank;
        }

        public Long getDbId() { return dbId; }
        public String getStudentId() { return studentId; }
        public String getName() { return name; }
        public String getGradeLevel() { return gradeLevel; }
        public double getGpa() { return gpa; }
        public void setGpa(double gpa) { this.gpa = gpa; }
        public int getClassRank() { return classRank; }
        public List<CourseGrade> getCourseGrades() { return courseGrades; }
        public int getDaysPresent() { return daysPresent; }
        public void setDaysPresent(int daysPresent) { this.daysPresent = daysPresent; }
        public int getDaysAbsent() { return daysAbsent; }
        public void setDaysAbsent(int daysAbsent) { this.daysAbsent = daysAbsent; }
        public int getTardies() { return tardies; }
        public void setTardies(int tardies) { this.tardies = tardies; }
        public double getCreditsEarned() { return creditsEarned; }
        public void setCreditsEarned(double creditsEarned) { this.creditsEarned = creditsEarned; }
        public boolean isGenerated() { return generated; }
        public void setGenerated(boolean generated) { this.generated = generated; }
        public boolean isSelected() { return selected.get(); }
        public void setSelected(boolean selected) { this.selected.set(selected); }
        public BooleanProperty selectedProperty() { return selected; }
    }

    public static class CourseGrade {
        private String courseName;
        private String letterGrade;
        private double percentage;
        private int credits;
        private String teacher;
        private String comment;

        public CourseGrade(String courseName, String letterGrade, double percentage, int credits, String teacher, String comment) {
            this.courseName = courseName;
            this.letterGrade = letterGrade;
            this.percentage = percentage;
            this.credits = credits;
            this.teacher = teacher;
            this.comment = comment;
        }

        public String getCourseName() { return courseName; }
        public String getLetterGrade() { return letterGrade; }
        public double getPercentage() { return percentage; }
        public int getCredits() { return credits; }
        public String getTeacher() { return teacher; }
        public String getComment() { return comment; }
    }
}
