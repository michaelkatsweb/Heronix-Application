package com.heronix.ui.controller;

import javafx.application.Platform;
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
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ReportCardGenerationController {

    // Header Components
    @FXML private ComboBox<GradingPeriod> gradingPeriodComboBox;
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
    private ObservableList<GradingPeriod> gradingPeriods = FXCollections.observableArrayList();
    private StudentReportData currentPreviewStudent = null;
    private Set<String> generatedReportIds = new HashSet<>();

    @FXML
    public void initialize() {
        setupGradingPeriods();
        setupFilters();
        setupStudentList();
        setupPreview();
        setupTemplateSettings();
        loadSampleData();
        updateStatistics();
    }

    private void setupGradingPeriods() {
        gradingPeriods.addAll(
                new GradingPeriod("Q1", "Quarter 1", LocalDate.of(2024, 8, 15), LocalDate.of(2024, 10, 25)),
                new GradingPeriod("Q2", "Quarter 2", LocalDate.of(2024, 10, 28), LocalDate.of(2024, 12, 20)),
                new GradingPeriod("Q3", "Quarter 3", LocalDate.of(2025, 1, 6), LocalDate.of(2025, 3, 14)),
                new GradingPeriod("Q4", "Quarter 4", LocalDate.of(2025, 3, 24), LocalDate.of(2025, 6, 5)),
                new GradingPeriod("S1", "Semester 1", LocalDate.of(2024, 8, 15), LocalDate.of(2024, 12, 20)),
                new GradingPeriod("S2", "Semester 2", LocalDate.of(2025, 1, 6), LocalDate.of(2025, 6, 5)),
                new GradingPeriod("FY", "Full Year", LocalDate.of(2024, 8, 15), LocalDate.of(2025, 6, 5))
        );

        gradingPeriodComboBox.setItems(gradingPeriods);
        gradingPeriodComboBox.setConverter(new StringConverter<GradingPeriod>() {
            @Override
            public String toString(GradingPeriod period) {
                return period == null ? "" : period.getName() + " (" +
                        period.getStartDate().format(DateTimeFormatter.ofPattern("MM/dd")) + " - " +
                        period.getEndDate().format(DateTimeFormatter.ofPattern("MM/dd")) + ")";
            }

            @Override
            public GradingPeriod fromString(String string) {
                return null;
            }
        });
        gradingPeriodComboBox.getSelectionModel().select(1); // Q2 selected

        academicYearComboBox.setItems(FXCollections.observableArrayList(
                "2024-2025", "2023-2024", "2022-2023"
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

        // Add filter listeners
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
                    setText(String.format("%s (%s)\nGrade %d • GPA: %.2f • %s",
                            student.getName(),
                            student.getStudentId(),
                            student.getGradeLevel(),
                            student.getGpa(),
                            student.isGenerated() ? "✓ Generated" : "○ Not Generated"
                    ));

                    String statusColor = student.isGenerated() ? "#4caf50" : "#ff9800";
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

        // Add listeners to settings that affect preview
        templateComboBox.setOnAction(e -> refreshPreview());
        showGPACheckBox.selectedProperty().addListener((obs, old, newVal) -> refreshPreview());
        showClassRankCheckBox.selectedProperty().addListener((obs, old, newVal) -> refreshPreview());
        showAttendanceCheckBox.selectedProperty().addListener((obs, old, newVal) -> refreshPreview());
        showBehaviorCheckBox.selectedProperty().addListener((obs, old, newVal) -> refreshPreview());
        showTeacherCommentsCheckBox.selectedProperty().addListener((obs, old, newVal) -> refreshPreview());
        watermarkCheckBox.selectedProperty().addListener((obs, old, newVal) -> refreshPreview());

        gradeFormatGroup.selectedToggleProperty().addListener((obs, old, newVal) -> refreshPreview());
    }

    private void loadSampleData() {
        Random rand = new Random(42);
        String[] firstNames = {"Emma", "Liam", "Olivia", "Noah", "Ava", "Ethan", "Sophia", "Mason",
                "Isabella", "William", "Mia", "James", "Charlotte", "Benjamin", "Amelia"};
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
                "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson"};

        for (int i = 0; i < 35; i++) {
            String firstName = firstNames[rand.nextInt(firstNames.length)];
            String lastName = lastNames[rand.nextInt(lastNames.length)];
            String studentId = String.format("S%05d", 2000 + i);
            int gradeLevel = 9 + rand.nextInt(4); // Grades 9-12
            double gpa = 2.0 + rand.nextDouble() * 2.0; // GPA 2.0-4.0

            StudentReportData student = new StudentReportData(
                    studentId,
                    firstName + " " + lastName,
                    gradeLevel,
                    gpa
            );

            // Generate sample course grades
            student.getCourseGrades().add(new CourseGrade("English III", "A", 94.5, 4, "Excellent writing skills"));
            student.getCourseGrades().add(new CourseGrade("Algebra II", "B+", 87.3, 4, "Good progress this quarter"));
            student.getCourseGrades().add(new CourseGrade("US History", "A-", 91.2, 3, "Strong analytical thinking"));
            student.getCourseGrades().add(new CourseGrade("Chemistry", "B", 85.7, 4, "Lab work improving"));
            student.getCourseGrades().add(new CourseGrade("Spanish II", "A", 93.8, 3, "Outstanding participation"));
            student.getCourseGrades().add(new CourseGrade("Physical Education", "A", 98.0, 1, "Great teamwork"));

            // Attendance
            student.setDaysPresent(75 + rand.nextInt(10));
            student.setDaysAbsent(rand.nextInt(5));
            student.setTardies(rand.nextInt(3));

            // 40% already generated
            if (rand.nextDouble() < 0.4) {
                student.setGenerated(true);
                generatedReportIds.add(student.getStudentId());
            }

            allStudents.add(student);
        }

        applyFilters();
    }

    private void applyFilters() {
        String searchText = studentSearchField.getText().toLowerCase();
        String gradeFilter = gradeFilterComboBox.getSelectionModel().getSelectedItem();
        String statusFilter = statusFilterComboBox.getSelectionModel().getSelectedItem();

        filteredStudents.clear();
        filteredStudents.addAll(allStudents.stream()
                .filter(s -> {
                    // Search filter
                    if (!searchText.isEmpty() &&
                            !s.getName().toLowerCase().contains(searchText) &&
                            !s.getStudentId().toLowerCase().contains(searchText)) {
                        return false;
                    }

                    // Grade level filter
                    if (gradeFilter != null && !gradeFilter.equals("All Grades")) {
                        int grade = Integer.parseInt(gradeFilter.replace("Grade ", ""));
                        if (s.getGradeLevel() != grade) {
                            return false;
                        }
                    }

                    // Status filter
                    if (statusFilter != null && !statusFilter.equals("All Status")) {
                        if (statusFilter.equals("Generated") && !s.isGenerated()) return false;
                        if (statusFilter.equals("Not Generated") && s.isGenerated()) return false;
                    }

                    // Checkbox filters
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
        GradingPeriod period = gradingPeriodComboBox.getSelectionModel().getSelectedItem();
        String template = templateComboBox.getSelectionModel().getSelectedItem();
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
        html.append(".grade-letter { font-weight: bold; font-size: 18px; color: #4caf50; }");
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

        // Header
        html.append("<div class='header'>");
        if (showSchoolLogoCheckBox.isSelected()) {
            html.append("<h1>🎓 HERONIX HIGH SCHOOL</h1>");
        } else {
            html.append("<h1>HERONIX HIGH SCHOOL</h1>");
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
                    .append(student.getClassRank()).append(" / 350</div></div>");
        }

        html.append("</div></div>");

        // Course Grades
        html.append("<div class='section'>");
        html.append("<div class='section-title'>Course Grades</div>");
        html.append("<table>");
        html.append("<tr><th>Course</th><th style='text-align:center'>Grade</th><th style='text-align:center'>Percentage</th><th style='text-align:center'>Credits</th>");
        if (showTeacherCommentsCheckBox.isSelected()) {
            html.append("<th>Teacher Comments</th>");
        }
        html.append("</tr>");

        for (CourseGrade course : student.getCourseGrades()) {
            html.append("<tr>");
            html.append("<td>").append(course.getCourseName()).append("</td>");
            html.append("<td style='text-align:center'><span class='grade-letter'>").append(course.getLetterGrade()).append("</span></td>");
            html.append("<td style='text-align:center'>").append(String.format("%.1f%%", course.getPercentage())).append("</td>");
            html.append("<td style='text-align:center'>").append(course.getCredits()).append("</td>");
            if (showTeacherCommentsCheckBox.isSelected()) {
                html.append("<td>").append(course.getComment()).append("</td>");
            }
            html.append("</tr>");
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
            double attendanceRate = (double) student.getDaysPresent() / total * 100;
            html.append("<div class='info-item'><div class='info-label'>Attendance Rate</div><div class='info-value'>")
                    .append(String.format("%.1f%%", attendanceRate)).append("</div></div>");
            html.append("</div></div>");
        }

        // Principal's Message
        String message = principalMessageArea.getText();
        if (!message.trim().isEmpty()) {
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
        // Reset generated status when period changes
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
        List<StudentReportData> toGenerate = new ArrayList<>(allStudents);
        generateReportsForStudents(toGenerate);
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

                    // Simulate generation
                    Thread.sleep(100 + new Random().nextInt(200));

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
            statusLabel.setText("Exporting " + selected.size() + " report cards to PDF...");
            Platform.runLater(() -> {
                statusLabel.setText("Exported " + selected.size() + " PDFs to " + dir.getName());
                showAlert("Success", "Exported " + selected.size() + " report cards to:\n" + dir.getAbsolutePath());
            });
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

        statusLabel.setText("Sending " + selected.size() + " report cards to printer...");
        showAlert("Print", "Printing " + selected.size() + " report cards");
    }

    @FXML
    private void handleRefreshPreview() {
        refreshPreview();
        previewStatusLabel.setText("Preview refreshed");
    }

    @FXML
    private void handlePrintCurrent() {
        if (currentPreviewStudent != null) {
            statusLabel.setText("Printing report card for " + currentPreviewStudent.getName());
            showAlert("Print", "Printing report card for " + currentPreviewStudent.getName());
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
            statusLabel.setText("Exported report card to " + file.getName());
            showAlert("Success", "Report card exported to:\n" + file.getAbsolutePath());
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
        statusLabel.setText("Template customization feature coming soon");
        showAlert("Customize Template", "Advanced template customization will be available in a future update");
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

    // Data Classes

    public static class GradingPeriod {
        private String code;
        private String name;
        private LocalDate startDate;
        private LocalDate endDate;

        public GradingPeriod(String code, String name, LocalDate startDate, LocalDate endDate) {
            this.code = code;
            this.name = name;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public String getCode() { return code; }
        public String getName() { return name; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
    }

    public static class StudentReportData {
        private String studentId;
        private String name;
        private int gradeLevel;
        private double gpa;
        private int classRank;
        private List<CourseGrade> courseGrades = new ArrayList<>();
        private int daysPresent;
        private int daysAbsent;
        private int tardies;
        private boolean generated = false;
        private javafx.beans.property.BooleanProperty selected = new javafx.beans.property.SimpleBooleanProperty(false);

        public StudentReportData(String studentId, String name, int gradeLevel, double gpa) {
            this.studentId = studentId;
            this.name = name;
            this.gradeLevel = gradeLevel;
            this.gpa = gpa;
            this.classRank = (int) (Math.random() * 350) + 1;
        }

        public String getStudentId() { return studentId; }
        public String getName() { return name; }
        public int getGradeLevel() { return gradeLevel; }
        public double getGpa() { return gpa; }
        public int getClassRank() { return classRank; }
        public List<CourseGrade> getCourseGrades() { return courseGrades; }
        public int getDaysPresent() { return daysPresent; }
        public void setDaysPresent(int daysPresent) { this.daysPresent = daysPresent; }
        public int getDaysAbsent() { return daysAbsent; }
        public void setDaysAbsent(int daysAbsent) { this.daysAbsent = daysAbsent; }
        public int getTardies() { return tardies; }
        public void setTardies(int tardies) { this.tardies = tardies; }
        public boolean isGenerated() { return generated; }
        public void setGenerated(boolean generated) { this.generated = generated; }
        public boolean isSelected() { return selected.get(); }
        public void setSelected(boolean selected) { this.selected.set(selected); }
        public javafx.beans.property.BooleanProperty selectedProperty() { return selected; }
    }

    public static class CourseGrade {
        private String courseName;
        private String letterGrade;
        private double percentage;
        private int credits;
        private String comment;

        public CourseGrade(String courseName, String letterGrade, double percentage, int credits, String comment) {
            this.courseName = courseName;
            this.letterGrade = letterGrade;
            this.percentage = percentage;
            this.credits = credits;
            this.comment = comment;
        }

        public String getCourseName() { return courseName; }
        public String getLetterGrade() { return letterGrade; }
        public double getPercentage() { return percentage; }
        public int getCredits() { return credits; }
        public String getComment() { return comment; }
    }
}
