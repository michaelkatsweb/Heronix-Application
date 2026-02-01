package com.heronix.ui.controller;

import com.heronix.model.domain.Student;
import com.heronix.model.domain.TranscriptRecord;
import com.heronix.repository.StudentRepository;
import com.heronix.service.impl.TranscriptService;
import com.heronix.service.impl.TranscriptService.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class TranscriptManagementController {

    @Autowired private TranscriptService transcriptService;
    @Autowired private StudentRepository studentRepository;

    // FXML - Search & buttons
    @FXML private TextField studentSearchField;
    @FXML private Button generateBtn;
    @FXML private Button exportPdfBtn;
    @FXML private Button printBtn;

    // FXML - Student search results table
    @FXML private TableView<StudentRow> studentTableView;
    @FXML private TableColumn<StudentRow, String> colStudentId;
    @FXML private TableColumn<StudentRow, String> colStudentName;
    @FXML private TableColumn<StudentRow, String> colGradeLevel;
    @FXML private TableColumn<StudentRow, String> colStatus;

    // FXML - Student info header
    @FXML private HBox studentInfoPane;
    @FXML private Label lblStudentName;
    @FXML private Label lblStudentNumber;
    @FXML private Label lblGradeLevel;
    @FXML private Label lblCumulativeGpa;
    @FXML private Label lblWeightedGpa;
    @FXML private Label lblCreditsEarned;
    @FXML private Label lblClassRank;
    @FXML private Label lblGradStatus;

    // FXML - Transcript table
    @FXML private TableView<TranscriptRow> transcriptTableView;
    @FXML private TableColumn<TranscriptRow, String> colYear;
    @FXML private TableColumn<TranscriptRow, String> colSemester;
    @FXML private TableColumn<TranscriptRow, String> colCourseCode;
    @FXML private TableColumn<TranscriptRow, String> colCourseName;
    @FXML private TableColumn<TranscriptRow, String> colCourseType;
    @FXML private TableColumn<TranscriptRow, String> colTeacher;
    @FXML private TableColumn<TranscriptRow, String> colLetterGrade;
    @FXML private TableColumn<TranscriptRow, String> colNumericGrade;
    @FXML private TableColumn<TranscriptRow, String> colGradePoints;
    @FXML private TableColumn<TranscriptRow, String> colCreditsAttempted;
    @FXML private TableColumn<TranscriptRow, String> colCreditsEarned;

    // FXML - Status
    @FXML private Label lblStatus;
    @FXML private ProgressIndicator progressIndicator;

    private final ObservableList<StudentRow> studentList = FXCollections.observableArrayList();
    private final ObservableList<TranscriptRow> transcriptList = FXCollections.observableArrayList();

    private Long selectedStudentDbId;
    private StudentTranscript currentTranscript;

    @FXML
    public void initialize() {
        // Student search results table
        colStudentId.setCellValueFactory(cd -> cd.getValue().studentIdProperty());
        colStudentName.setCellValueFactory(cd -> cd.getValue().nameProperty());
        colGradeLevel.setCellValueFactory(cd -> cd.getValue().gradeLevelProperty());
        colStatus.setCellValueFactory(cd -> cd.getValue().statusProperty());
        studentTableView.setItems(studentList);

        studentTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedStudentDbId = newVal.dbId;
                generateBtn.setDisable(false);
                loadTranscript(newVal.dbId);
            }
        });

        // Transcript table columns
        colYear.setCellValueFactory(cd -> cd.getValue().yearProperty());
        colSemester.setCellValueFactory(cd -> cd.getValue().semesterProperty());
        colCourseCode.setCellValueFactory(cd -> cd.getValue().courseCodeProperty());
        colCourseName.setCellValueFactory(cd -> cd.getValue().courseNameProperty());
        colCourseType.setCellValueFactory(cd -> cd.getValue().courseTypeProperty());
        colTeacher.setCellValueFactory(cd -> cd.getValue().teacherProperty());
        colLetterGrade.setCellValueFactory(cd -> cd.getValue().letterGradeProperty());
        colNumericGrade.setCellValueFactory(cd -> cd.getValue().numericGradeProperty());
        colGradePoints.setCellValueFactory(cd -> cd.getValue().gradePointsProperty());
        colCreditsAttempted.setCellValueFactory(cd -> cd.getValue().creditsAttemptedProperty());
        colCreditsEarned.setCellValueFactory(cd -> cd.getValue().creditsEarnedProperty());
        transcriptTableView.setItems(transcriptList);

        // Enter key triggers search
        studentSearchField.setOnAction(e -> handleSearchStudents());
    }

    @FXML
    private void handleSearchStudents() {
        String query = studentSearchField.getText();
        if (query == null || query.trim().isEmpty()) {
            setStatus("Enter a student name or ID to search");
            return;
        }

        setStatus("Searching...");
        showProgress(true);

        new Thread(() -> {
            try {
                List<Student> results = studentRepository.searchByName(query.trim());
                Platform.runLater(() -> {
                    studentList.clear();
                    if (results == null || results.isEmpty()) {
                        setStatus("No students found for: " + query);
                    } else {
                        for (Student s : results) {
                            String name = ((s.getFirstName() != null ? s.getFirstName() : "") + " "
                                    + (s.getLastName() != null ? s.getLastName() : "")).trim();
                            studentList.add(new StudentRow(
                                    s.getId(),
                                    s.getStudentId() != null ? s.getStudentId() : "",
                                    name.isEmpty() ? "Unknown" : name,
                                    s.getGradeLevel() != null ? s.getGradeLevel() : "-",
                                    s.getStatus() != null ? s.getStatus().toString() : "Active"
                            ));
                        }
                        setStatus("Found " + results.size() + " student(s)");
                    }
                    showProgress(false);
                });
            } catch (Exception ex) {
                log.error("Error searching students", ex);
                Platform.runLater(() -> {
                    setStatus("Search error: " + ex.getMessage());
                    showProgress(false);
                });
            }
        }).start();
    }

    private void loadTranscript(Long studentDbId) {
        setStatus("Loading transcript...");
        showProgress(true);

        new Thread(() -> {
            try {
                StudentTranscript transcript = transcriptService.generateTranscript(studentDbId);
                ClassRankInfo rankInfo = null;
                GraduationStatus gradStatus = null;
                try {
                    rankInfo = transcriptService.getClassRank(studentDbId);
                } catch (Exception e) {
                    log.warn("Could not load class rank for student {}", studentDbId, e);
                }
                try {
                    gradStatus = transcriptService.checkGraduationRequirements(studentDbId);
                } catch (Exception e) {
                    log.warn("Could not load graduation status for student {}", studentDbId, e);
                }

                final StudentTranscript t = transcript;
                final ClassRankInfo rank = rankInfo;
                final GraduationStatus grad = gradStatus;

                Platform.runLater(() -> {
                    currentTranscript = t;
                    populateStudentInfo(t, rank, grad);
                    populateTranscriptTable(t);
                    exportPdfBtn.setDisable(false);
                    printBtn.setDisable(false);
                    setStatus("Transcript loaded - " + t.getStudentName());
                    showProgress(false);
                });
            } catch (Exception ex) {
                log.error("Error loading transcript for student {}", studentDbId, ex);
                Platform.runLater(() -> {
                    setStatus("Error loading transcript: " + ex.getMessage());
                    showProgress(false);
                });
            }
        }).start();
    }

    private void populateStudentInfo(StudentTranscript t, ClassRankInfo rank, GraduationStatus grad) {
        studentInfoPane.setVisible(true);
        studentInfoPane.setManaged(true);

        lblStudentName.setText(t.getStudentName());
        lblStudentNumber.setText(t.getStudentNumber() != null ? t.getStudentNumber() : "-");
        lblGradeLevel.setText(t.getCurrentGradeLevel() != null ? "Grade " + t.getCurrentGradeLevel() : "-");
        lblCumulativeGpa.setText(formatGpa(t.getCumulativeGpa()));
        lblWeightedGpa.setText(formatGpa(t.getWeightedGpa()));
        lblCreditsEarned.setText(t.getTotalCreditsEarned() != null ? t.getTotalCreditsEarned().toPlainString() : "0");

        if (rank != null) {
            lblClassRank.setText(rank.getRank() + " / " + rank.getTotalStudents());
        } else {
            lblClassRank.setText("-");
        }

        if (grad != null) {
            if (grad.isMeetsRequirements()) {
                lblGradStatus.setText("On Track");
                lblGradStatus.setStyle("-fx-text-fill: #81c784; -fx-font-size: 14; -fx-font-weight: bold;");
            } else {
                String needed = grad.getCreditsRequired().subtract(grad.getCreditsEarned()).toPlainString();
                lblGradStatus.setText("Needs " + needed + " credits");
                lblGradStatus.setStyle("-fx-text-fill: #ffb74d; -fx-font-size: 14; -fx-font-weight: bold;");
            }
        } else {
            lblGradStatus.setText("-");
            lblGradStatus.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");
        }
    }

    private void populateTranscriptTable(StudentTranscript t) {
        transcriptList.clear();
        if (t.getAcademicYears() == null) return;

        for (AcademicYearSummary year : t.getAcademicYears()) {
            if (year.getCourses() == null) continue;
            for (TranscriptRecord r : year.getCourses()) {
                String courseCode = "";
                String courseName = "";
                if (r.getCourse() != null) {
                    courseCode = r.getCourse().getCourseCode() != null ? r.getCourse().getCourseCode() : "";
                    courseName = r.getCourse().getCourseName() != null ? r.getCourse().getCourseName() : "";
                }

                transcriptList.add(new TranscriptRow(
                        r.getAcademicYear() != null ? r.getAcademicYear() : "",
                        r.getSemester() != null ? r.getSemester().name() : "",
                        courseCode,
                        courseName,
                        r.getCourseType() != null ? r.getCourseType().name() : "REGULAR",
                        r.getTeacherName() != null ? r.getTeacherName() : "",
                        r.getLetterGrade() != null ? r.getLetterGrade() : "",
                        r.getNumericGrade() != null ? r.getNumericGrade().toPlainString() : "",
                        r.getGradePoints() != null ? r.getGradePoints().toPlainString() : "",
                        r.getCreditsAttempted() != null ? r.getCreditsAttempted().toPlainString() : "",
                        r.getCreditsEarned() != null ? r.getCreditsEarned().toPlainString() : ""
                ));
            }
        }
    }

    @FXML
    private void handleGenerateTranscript() {
        if (selectedStudentDbId == null) {
            showAlert("No Student Selected", "Please search and select a student first.");
            return;
        }
        loadTranscript(selectedStudentDbId);
    }

    @FXML
    private void handleExportPdf() {
        if (currentTranscript == null) {
            showAlert("No Transcript", "Please generate a transcript first.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Transcript");
        fileChooser.setInitialFileName("Transcript_" + currentTranscript.getStudentNumber() + ".csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(transcriptTableView.getScene().getWindow());

        if (file != null) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                // BOM for Excel
                pw.print('\ufeff');
                // Header
                pw.println("OFFICIAL TRANSCRIPT");
                pw.println("Student," + currentTranscript.getStudentName());
                pw.println("Student ID," + currentTranscript.getStudentNumber());
                pw.println("Grade Level," + currentTranscript.getCurrentGradeLevel());
                pw.println("Cumulative GPA," + formatGpa(currentTranscript.getCumulativeGpa()));
                pw.println("Weighted GPA," + formatGpa(currentTranscript.getWeightedGpa()));
                pw.println("Credits Earned," + (currentTranscript.getTotalCreditsEarned() != null
                        ? currentTranscript.getTotalCreditsEarned().toPlainString() : "0"));
                pw.println("Generated," + (currentTranscript.getGeneratedAt() != null
                        ? currentTranscript.getGeneratedAt().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")) : ""));
                pw.println();
                pw.println("Academic Year,Semester,Course Code,Course Name,Type,Teacher,Grade,Numeric,GPA Points,Credits Attempted,Credits Earned");

                for (TranscriptRow row : transcriptList) {
                    pw.printf("%s,%s,%s,\"%s\",%s,\"%s\",%s,%s,%s,%s,%s%n",
                            row.year, row.semester, row.courseCode, row.courseName,
                            row.courseType, row.teacher, row.letterGrade, row.numericGrade,
                            row.gradePoints, row.creditsAttempted, row.creditsEarned);
                }

                setStatus("Exported to: " + file.getName());
            } catch (Exception ex) {
                log.error("Error exporting transcript", ex);
                showAlert("Export Error", "Failed to export: " + ex.getMessage());
            }
        }
    }

    @FXML
    private void handlePrint() {
        if (currentTranscript == null) {
            showAlert("No Transcript", "Please generate a transcript first.");
            return;
        }
        // Use JavaFX print dialog
        javafx.print.PrinterJob job = javafx.print.PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(transcriptTableView.getScene().getWindow())) {
            boolean printed = job.printPage(transcriptTableView);
            if (printed) {
                job.endJob();
                setStatus("Transcript sent to printer");
            } else {
                setStatus("Print failed");
            }
        }
    }

    // Helpers

    private String formatGpa(BigDecimal gpa) {
        if (gpa == null) return "0.000";
        return gpa.setScale(3, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private void setStatus(String msg) {
        if (lblStatus != null) lblStatus.setText(msg);
    }

    private void showProgress(boolean show) {
        if (progressIndicator != null) progressIndicator.setVisible(show);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Inner row classes

    public static class StudentRow {
        private final Long dbId;
        private final String studentId;
        private final String name;
        private final String gradeLevel;
        private final String status;

        public StudentRow(Long dbId, String studentId, String name, String gradeLevel, String status) {
            this.dbId = dbId;
            this.studentId = studentId;
            this.name = name;
            this.gradeLevel = gradeLevel;
            this.status = status;
        }

        public SimpleStringProperty studentIdProperty() { return new SimpleStringProperty(studentId); }
        public SimpleStringProperty nameProperty() { return new SimpleStringProperty(name); }
        public SimpleStringProperty gradeLevelProperty() { return new SimpleStringProperty(gradeLevel); }
        public SimpleStringProperty statusProperty() { return new SimpleStringProperty(status); }
    }

    public static class TranscriptRow {
        private final String year, semester, courseCode, courseName, courseType, teacher;
        private final String letterGrade, numericGrade, gradePoints, creditsAttempted, creditsEarned;

        public TranscriptRow(String year, String semester, String courseCode, String courseName,
                             String courseType, String teacher, String letterGrade, String numericGrade,
                             String gradePoints, String creditsAttempted, String creditsEarned) {
            this.year = year;
            this.semester = semester;
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.courseType = courseType;
            this.teacher = teacher;
            this.letterGrade = letterGrade;
            this.numericGrade = numericGrade;
            this.gradePoints = gradePoints;
            this.creditsAttempted = creditsAttempted;
            this.creditsEarned = creditsEarned;
        }

        public SimpleStringProperty yearProperty() { return new SimpleStringProperty(year); }
        public SimpleStringProperty semesterProperty() { return new SimpleStringProperty(semester); }
        public SimpleStringProperty courseCodeProperty() { return new SimpleStringProperty(courseCode); }
        public SimpleStringProperty courseNameProperty() { return new SimpleStringProperty(courseName); }
        public SimpleStringProperty courseTypeProperty() { return new SimpleStringProperty(courseType); }
        public SimpleStringProperty teacherProperty() { return new SimpleStringProperty(teacher); }
        public SimpleStringProperty letterGradeProperty() { return new SimpleStringProperty(letterGrade); }
        public SimpleStringProperty numericGradeProperty() { return new SimpleStringProperty(numericGrade); }
        public SimpleStringProperty gradePointsProperty() { return new SimpleStringProperty(gradePoints); }
        public SimpleStringProperty creditsAttemptedProperty() { return new SimpleStringProperty(creditsAttempted); }
        public SimpleStringProperty creditsEarnedProperty() { return new SimpleStringProperty(creditsEarned); }
    }
}
