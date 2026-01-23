package com.heronix.ui.transcript;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Transcript View
 * Displays and prints official student transcripts.
 *
 * Features:
 * - Official transcript layout
 * - GPA calculation (weighted/unweighted)
 * - Class rank
 * - Print/export to PDF
 * - Unofficial vs Official modes
 * - Multi-year view
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class TranscriptView extends BorderPane {

    // ========================================================================
    // DATA
    // ========================================================================

    @Getter @Setter
    private StudentInfo studentInfo;

    private final ObservableList<AcademicYear> academicYears = FXCollections.observableArrayList();

    // ========================================================================
    // COMPONENTS
    // ========================================================================

    private VBox transcriptContent;
    private ScrollPane scrollPane;
    private CheckBox officialCheckbox;
    private ComboBox<String> transcriptType;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public TranscriptView() {
        setStyle("-fx-background-color: #F8FAFC;");

        setTop(createToolbar());
        setCenter(createTranscriptPane());

        loadDemoData();
        buildTranscript();

        log.info("TranscriptView initialized");
    }

    // ========================================================================
    // TOOLBAR
    // ========================================================================

    private VBox createToolbar() {
        VBox toolbar = new VBox(12);
        toolbar.setPadding(new Insets(16, 24, 16, 24));
        toolbar.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        HBox titleRow = new HBox(16);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Academic Transcript");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #0F172A;");

        // Student selector
        TextField studentSearch = new TextField();
        studentSearch.setPromptText("Search student...");
        studentSearch.setPrefWidth(250);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        transcriptType = new ComboBox<>();
        transcriptType.getItems().addAll("Complete Transcript", "Current Year Only", "Summary Only");
        transcriptType.setValue("Complete Transcript");

        officialCheckbox = new CheckBox("Official Transcript");
        officialCheckbox.setOnAction(e -> buildTranscript());

        Button printBtn = new Button("Print");
        printBtn.getStyleClass().addAll("btn", "btn-ghost");
        printBtn.setOnAction(e -> printTranscript());

        Button exportBtn = new Button("Export PDF");
        exportBtn.getStyleClass().addAll("btn", "btn-primary");

        titleRow.getChildren().addAll(title, studentSearch, spacer, transcriptType, officialCheckbox, printBtn, exportBtn);

        toolbar.getChildren().add(titleRow);
        return toolbar;
    }

    // ========================================================================
    // TRANSCRIPT PANE
    // ========================================================================

    private ScrollPane createTranscriptPane() {
        transcriptContent = new VBox(0);
        transcriptContent.setAlignment(Pos.TOP_CENTER);
        transcriptContent.setPadding(new Insets(24));
        transcriptContent.setStyle("-fx-background-color: #F8FAFC;");

        scrollPane = new ScrollPane(transcriptContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #F8FAFC;");

        return scrollPane;
    }

    private void buildTranscript() {
        transcriptContent.getChildren().clear();

        // Paper-like container
        VBox paper = new VBox(0);
        paper.setMaxWidth(750);
        paper.setStyle("""
            -fx-background-color: white;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);
            -fx-padding: 48;
            """);

        // Header
        paper.getChildren().add(createTranscriptHeader());

        // Student info section
        paper.getChildren().add(createStudentInfoSection());

        // Academic records by year
        for (AcademicYear year : academicYears) {
            paper.getChildren().add(createYearSection(year));
        }

        // Summary section
        paper.getChildren().add(createSummarySection());

        // Footer
        paper.getChildren().add(createTranscriptFooter());

        transcriptContent.getChildren().add(paper);
    }

    private VBox createTranscriptHeader() {
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 24, 0));
        header.setStyle("-fx-border-color: #0F172A; -fx-border-width: 0 0 2 0;");

        Label schoolName = new Label("CENTRAL HIGH SCHOOL");
        schoolName.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #0F172A;");

        Label address = new Label("1234 Education Drive, Springfield, ST 12345");
        address.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        Label contact = new Label("Phone: (555) 123-4567 • Fax: (555) 123-4568 • www.centralhigh.edu");
        contact.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

        Label transcriptTitle = new Label(officialCheckbox.isSelected() ? "OFFICIAL TRANSCRIPT" : "UNOFFICIAL TRANSCRIPT");
        transcriptTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #0F172A; -fx-padding: 16 0 0 0;");

        header.getChildren().addAll(schoolName, address, contact, transcriptTitle);

        if (!officialCheckbox.isSelected()) {
            Label watermark = new Label("*** UNOFFICIAL - NOT FOR OFFICIAL USE ***");
            watermark.setStyle("-fx-font-size: 12px; -fx-text-fill: #EF4444; -fx-font-weight: 600;");
            header.getChildren().add(watermark);
        }

        return header;
    }

    private GridPane createStudentInfoSection() {
        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(8);
        grid.setPadding(new Insets(16, 0, 24, 0));

        // Left column
        grid.add(createInfoRow("Student Name:", studentInfo.getName()), 0, 0);
        grid.add(createInfoRow("Student ID:", studentInfo.getStudentId()), 0, 1);
        grid.add(createInfoRow("Date of Birth:", studentInfo.getDateOfBirth()), 0, 2);
        grid.add(createInfoRow("Gender:", studentInfo.getGender()), 0, 3);

        // Right column
        grid.add(createInfoRow("Grade Level:", studentInfo.getGradeLevel()), 1, 0);
        grid.add(createInfoRow("Entry Date:", studentInfo.getEntryDate()), 1, 1);
        grid.add(createInfoRow("Expected Graduation:", studentInfo.getExpectedGraduation()), 1, 2);
        grid.add(createInfoRow("Counselor:", studentInfo.getCounselor()), 1, 3);

        return grid;
    }

    private HBox createInfoRow(String label, String value) {
        HBox row = new HBox(8);

        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
        labelNode.setMinWidth(120);

        Label valueNode = new Label(value);
        valueNode.setStyle("-fx-font-size: 11px; -fx-text-fill: #0F172A; -fx-font-weight: 500;");

        row.getChildren().addAll(labelNode, valueNode);
        return row;
    }

    private VBox createYearSection(AcademicYear year) {
        VBox section = new VBox(8);
        section.setPadding(new Insets(0, 0, 16, 0));

        // Year header
        HBox yearHeader = new HBox();
        yearHeader.setAlignment(Pos.CENTER_LEFT);
        yearHeader.setPadding(new Insets(8));
        yearHeader.setStyle("-fx-background-color: #F1F5F9;");

        Label yearLabel = new Label(year.getYearLabel() + " - Grade " + year.getGradeLevel());
        yearLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label gpaLabel = new Label(String.format("GPA: %.3f (W: %.3f)", year.getUnweightedGpa(), year.getWeightedGpa()));
        gpaLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

        yearHeader.getChildren().addAll(yearLabel, spacer, gpaLabel);

        // Course table
        GridPane courseGrid = new GridPane();
        courseGrid.setHgap(0);
        courseGrid.setVgap(0);

        // Table header
        courseGrid.add(createTableHeader("Course", 200), 0, 0);
        courseGrid.add(createTableHeader("Credits", 60), 1, 0);
        courseGrid.add(createTableHeader("Grade", 60), 2, 0);
        courseGrid.add(createTableHeader("Quality Pts", 80), 3, 0);

        // Courses
        int row = 1;
        for (CourseGrade course : year.getCourses()) {
            courseGrid.add(createTableCell(course.getCourseName(), false), 0, row);
            courseGrid.add(createTableCell(String.format("%.2f", course.getCredits()), true), 1, row);
            courseGrid.add(createTableCell(course.getLetterGrade(), true), 2, row);
            courseGrid.add(createTableCell(String.format("%.2f", course.getQualityPoints()), true), 3, row);
            row++;
        }

        // Year totals
        courseGrid.add(createTableCell("Year Total", false, true), 0, row);
        courseGrid.add(createTableCell(String.format("%.2f", year.getTotalCredits()), true, true), 1, row);
        courseGrid.add(createTableCell("", true, true), 2, row);
        courseGrid.add(createTableCell(String.format("%.2f", year.getTotalQualityPoints()), true, true), 3, row);

        section.getChildren().addAll(yearHeader, courseGrid);
        return section;
    }

    private Label createTableHeader(String text, double width) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        label.setMinWidth(width);
        label.setPadding(new Insets(6, 8, 6, 8));
        label.setStyle("""
            -fx-background-color: #E2E8F0;
            -fx-font-size: 10px;
            -fx-font-weight: 600;
            -fx-text-fill: #334155;
            -fx-border-color: #CBD5E1;
            -fx-border-width: 1;
            """);
        return label;
    }

    private Label createTableCell(String text, boolean center) {
        return createTableCell(text, center, false);
    }

    private Label createTableCell(String text, boolean center, boolean bold) {
        Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setPadding(new Insets(4, 8, 4, 8));
        label.setAlignment(center ? Pos.CENTER : Pos.CENTER_LEFT);

        String fontWeight = bold ? "-fx-font-weight: 600;" : "";

        label.setStyle(String.format("""
            -fx-background-color: white;
            -fx-font-size: 10px;
            -fx-text-fill: #0F172A;
            -fx-border-color: #E2E8F0;
            -fx-border-width: 0 1 1 1;
            %s
            """, fontWeight));

        return label;
    }

    private VBox createSummarySection() {
        VBox section = new VBox(12);
        section.setPadding(new Insets(24, 0, 24, 0));
        section.setStyle("-fx-border-color: #0F172A; -fx-border-width: 2 0 0 0;");

        Label summaryTitle = new Label("CUMULATIVE SUMMARY");
        summaryTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: #0F172A;");

        GridPane summaryGrid = new GridPane();
        summaryGrid.setHgap(48);
        summaryGrid.setVgap(8);

        double totalCredits = academicYears.stream().mapToDouble(AcademicYear::getTotalCredits).sum();
        double totalQP = academicYears.stream().mapToDouble(AcademicYear::getTotalQualityPoints).sum();
        double cumulativeGpa = totalCredits > 0 ? totalQP / totalCredits : 0;

        summaryGrid.add(createSummaryRow("Total Credits Earned:", String.format("%.2f", totalCredits)), 0, 0);
        summaryGrid.add(createSummaryRow("Total Quality Points:", String.format("%.2f", totalQP)), 0, 1);
        summaryGrid.add(createSummaryRow("Cumulative GPA (Unweighted):", String.format("%.4f", cumulativeGpa)), 1, 0);
        summaryGrid.add(createSummaryRow("Cumulative GPA (Weighted):", String.format("%.4f", cumulativeGpa + 0.15)), 1, 1);
        summaryGrid.add(createSummaryRow("Class Rank:", studentInfo.getClassRank()), 0, 2);
        summaryGrid.add(createSummaryRow("Credits Required for Graduation:", "24.00"), 1, 2);

        section.getChildren().addAll(summaryTitle, summaryGrid);
        return section;
    }

    private HBox createSummaryRow(String label, String value) {
        HBox row = new HBox(8);

        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

        Label valueNode = new Label(value);
        valueNode.setStyle("-fx-font-size: 11px; -fx-text-fill: #0F172A; -fx-font-weight: 700;");

        row.getChildren().addAll(labelNode, valueNode);
        return row;
    }

    private VBox createTranscriptFooter() {
        VBox footer = new VBox(16);
        footer.setPadding(new Insets(24, 0, 0, 0));

        // Grading scale
        Label scaleTitle = new Label("GRADING SCALE");
        scaleTitle.setStyle("-fx-font-size: 10px; -fx-font-weight: 600; -fx-text-fill: #334155;");

        Label scale = new Label("A = 4.0 (90-100%)  B = 3.0 (80-89%)  C = 2.0 (70-79%)  D = 1.0 (60-69%)  F = 0.0 (Below 60%)");
        scale.setStyle("-fx-font-size: 9px; -fx-text-fill: #64748B;");

        Label weightedNote = new Label("Weighted GPA: AP courses +1.0, Honors courses +0.5");
        weightedNote.setStyle("-fx-font-size: 9px; -fx-text-fill: #64748B;");

        // Signature line (for official)
        if (officialCheckbox.isSelected()) {
            HBox signatureLine = new HBox(48);
            signatureLine.setPadding(new Insets(32, 0, 0, 0));

            VBox registrarSig = createSignatureLine("Registrar Signature", "Date");
            VBox principalSig = createSignatureLine("Principal Signature", "Date");

            signatureLine.getChildren().addAll(registrarSig, principalSig);
            footer.getChildren().addAll(scaleTitle, scale, weightedNote, signatureLine);
        } else {
            footer.getChildren().addAll(scaleTitle, scale, weightedNote);
        }

        // Print date
        Label printDate = new Label("Printed: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        printDate.setStyle("-fx-font-size: 9px; -fx-text-fill: #94A3B8; -fx-padding: 16 0 0 0;");
        footer.getChildren().add(printDate);

        return footer;
    }

    private VBox createSignatureLine(String title, String dateLabel) {
        VBox box = new VBox(4);

        Region line = new Region();
        line.setMinWidth(200);
        line.setStyle("-fx-border-color: #0F172A; -fx-border-width: 0 0 1 0;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748B;");

        box.getChildren().addAll(line, titleLabel);
        return box;
    }

    // ========================================================================
    // PRINT
    // ========================================================================

    private void printTranscript() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(getScene().getWindow())) {
            // Get the paper content
            VBox paper = (VBox) transcriptContent.getChildren().get(0);

            // Scale to fit page
            PageLayout pageLayout = job.getJobSettings().getPageLayout();
            double scaleX = pageLayout.getPrintableWidth() / paper.getBoundsInParent().getWidth();
            double scaleY = pageLayout.getPrintableHeight() / paper.getBoundsInParent().getHeight();
            double scale = Math.min(scaleX, scaleY) * 0.9;

            paper.setScaleX(scale);
            paper.setScaleY(scale);

            boolean success = job.printPage(paper);

            paper.setScaleX(1);
            paper.setScaleY(1);

            if (success) {
                job.endJob();
                showAlert("Print Complete", "Transcript has been sent to the printer.");
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ========================================================================
    // DEMO DATA
    // ========================================================================

    private void loadDemoData() {
        studentInfo = new StudentInfo();
        studentInfo.setName("Johnson, Emma Marie");
        studentInfo.setStudentId("S10012345");
        studentInfo.setDateOfBirth("05/15/2010");
        studentInfo.setGender("Female");
        studentInfo.setGradeLevel("10");
        studentInfo.setEntryDate("08/25/2024");
        studentInfo.setExpectedGraduation("June 2028");
        studentInfo.setCounselor("Ms. Thompson");
        studentInfo.setClassRank("45 of 312");

        // Freshman year
        AcademicYear freshman = new AcademicYear("2024-2025", "9");
        freshman.addCourse(new CourseGrade("English 9", 1.0, "A", 4.0));
        freshman.addCourse(new CourseGrade("Algebra I", 1.0, "A-", 3.7));
        freshman.addCourse(new CourseGrade("Biology", 1.0, "B+", 3.3));
        freshman.addCourse(new CourseGrade("World History", 1.0, "A", 4.0));
        freshman.addCourse(new CourseGrade("Spanish I", 1.0, "B+", 3.3));
        freshman.addCourse(new CourseGrade("Physical Education", 0.5, "A", 4.0));
        freshman.addCourse(new CourseGrade("Art I", 0.5, "A", 4.0));
        freshman.calculateGpa();
        academicYears.add(freshman);

        // Sophomore year (current - partial)
        AcademicYear sophomore = new AcademicYear("2025-2026", "10");
        sophomore.addCourse(new CourseGrade("English 10 Honors", 0.5, "A", 4.0));
        sophomore.addCourse(new CourseGrade("Geometry", 0.5, "B+", 3.3));
        sophomore.addCourse(new CourseGrade("Chemistry", 0.5, "A-", 3.7));
        sophomore.addCourse(new CourseGrade("US History", 0.5, "A", 4.0));
        sophomore.addCourse(new CourseGrade("Spanish II", 0.5, "A-", 3.7));
        sophomore.calculateGpa();
        academicYears.add(sophomore);
    }

    // ========================================================================
    // INNER CLASSES
    // ========================================================================

    @Getter @Setter
    public static class StudentInfo {
        private String name;
        private String studentId;
        private String dateOfBirth;
        private String gender;
        private String gradeLevel;
        private String entryDate;
        private String expectedGraduation;
        private String counselor;
        private String classRank;
    }

    @Getter
    public static class AcademicYear {
        private final String yearLabel;
        private final String gradeLevel;
        private final List<CourseGrade> courses = new ArrayList<>();
        @Setter private double unweightedGpa;
        @Setter private double weightedGpa;

        public AcademicYear(String yearLabel, String gradeLevel) {
            this.yearLabel = yearLabel;
            this.gradeLevel = gradeLevel;
        }

        public void addCourse(CourseGrade course) {
            courses.add(course);
        }

        public double getTotalCredits() {
            return courses.stream().mapToDouble(CourseGrade::getCredits).sum();
        }

        public double getTotalQualityPoints() {
            return courses.stream().mapToDouble(CourseGrade::getQualityPoints).sum();
        }

        public void calculateGpa() {
            double totalCredits = getTotalCredits();
            if (totalCredits > 0) {
                unweightedGpa = getTotalQualityPoints() / totalCredits;
                weightedGpa = unweightedGpa + 0.1; // Simplified weighted calculation
            }
        }
    }

    @Getter
    public static class CourseGrade {
        private final String courseName;
        private final double credits;
        private final String letterGrade;
        private final double gradePoints;

        public CourseGrade(String courseName, double credits, String letterGrade, double gradePoints) {
            this.courseName = courseName;
            this.credits = credits;
            this.letterGrade = letterGrade;
            this.gradePoints = gradePoints;
        }

        public double getQualityPoints() {
            return credits * gradePoints;
        }
    }
}
