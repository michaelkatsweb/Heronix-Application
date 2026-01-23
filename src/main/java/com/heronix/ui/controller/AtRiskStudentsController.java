package com.heronix.ui.controller;

import com.heronix.model.domain.Student;
import com.heronix.service.StudentService;
import com.heronix.service.GraduationRequirementsService;
import com.heronix.ui.util.CopyableErrorDialog;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * At-Risk Students Controller
 *
 * Provides a dedicated view for identifying and managing students who are:
 * - At Risk (1-2 credits behind OR GPA 1.5-1.99)
 * - Retention Risk (3+ credits behind OR GPA < 1.5)
 * - Seniors not meeting graduation requirements
 *
 * Features:
 * - Color-coded table with visual indicators
 * - Filtering by risk level and grade
 * - Summary statistics
 * - Quick access to student details
 * - Export functionality
 *
 * Location: src/main/java/com/heronix/ui/controller/AtRiskStudentsController.java
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since November 2025
 */
@Slf4j
@Controller
public class AtRiskStudentsController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private GraduationRequirementsService graduationRequirementsService;

    @FXML private ComboBox<String> riskLevelFilter;
    @FXML private ComboBox<String> gradeFilter;
    @FXML private TableView<Student> atRiskTable;
    @FXML private TableColumn<Student, String> statusColumn;
    @FXML private TableColumn<Student, String> studentIdColumn;
    @FXML private TableColumn<Student, String> nameColumn;
    @FXML private TableColumn<Student, String> gradeColumn;
    @FXML private TableColumn<Student, Double> gpaColumn;
    @FXML private TableColumn<Student, Double> creditsEarnedColumn;
    @FXML private TableColumn<Student, Double> creditsRequiredColumn;
    @FXML private TableColumn<Student, Double> creditsBehindColumn;
    @FXML private TableColumn<Student, Void> actionsColumn;

    @FXML private Label totalStudentsLabel;
    @FXML private Label atRiskCountLabel;
    @FXML private Label retentionRiskCountLabel;
    @FXML private Label seniorsAtRiskLabel;

    private ObservableList<Student> atRiskStudentsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        log.info("Initializing AtRiskStudentsController");

        try {
            // Setup table columns
            setupTableColumns();

            // Setup filters
            setupFilters();

            // Setup action buttons
            setupActionButtons();

            // Setup row factory for color coding
            setupRowFactory();

            // Load initial data
            loadAtRiskStudents();

            // Update summary statistics
            updateSummaryStats();

            log.info("AtRiskStudentsController initialized successfully");

        } catch (Exception e) {
            log.error("Error initializing AtRiskStudentsController", e);
            showError("Initialization Error", "Failed to initialize At-Risk Students view: " + e.getMessage());
        }
    }

    /**
     * Setup table columns with appropriate cell factories
     */
    private void setupTableColumns() {
        // Status column with icon and color
        statusColumn.setCellValueFactory(data -> {
            Student student = data.getValue();
            if (graduationRequirementsService != null) {
                String status = graduationRequirementsService.getAcademicStandingStatus(student);
                String icon = graduationRequirementsService.getStandingIcon(student);
                return new SimpleStringProperty(icon + " " + status);
            }
            return new SimpleStringProperty("");
        });
        statusColumn.setCellFactory(col -> new TableCell<Student, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                Student student = getTableRow().getItem();
                if (empty || status == null || student == null) {
                    setText("");
                    setStyle("");
                    setTooltip(null);
                } else {
                    setText(status);
                    if (graduationRequirementsService != null) {
                        String color = graduationRequirementsService.getStandingColorCode(student);
                        setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");

                        // Add tooltip
                        String tooltipText = graduationRequirementsService.getStandingTooltip(student);
                        Tooltip tooltip = new Tooltip(tooltipText);
                        tooltip.setWrapText(true);
                        tooltip.setMaxWidth(400);
                        setTooltip(tooltip);
                    }
                }
            }
        });

        // Student ID column
        studentIdColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getStudentId()));

        // Name column
        nameColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getFullName()));

        // Grade column
        gradeColumn.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getGradeLevel()));

        // GPA column with color coding
        gpaColumn.setCellValueFactory(new PropertyValueFactory<>("currentGPA"));
        gpaColumn.setCellFactory(col -> new TableCell<Student, Double>() {
            @Override
            protected void updateItem(Double gpa, boolean empty) {
                super.updateItem(gpa, empty);
                if (empty || gpa == null) {
                    setText("");
                    setStyle("");
                } else {
                    setText(String.format("%.2f", gpa));
                    // Color code based on GPA
                    if (gpa < 1.5) {
                        setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;"); // Red - critical
                    } else if (gpa < 2.0) {
                        setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;"); // Orange - at risk
                    } else {
                        setStyle("-fx-text-fill: #4CAF50;"); // Green - meets requirement
                    }
                }
            }
        });

        // Credits Earned column
        creditsEarnedColumn.setCellValueFactory(new PropertyValueFactory<>("creditsEarned"));
        creditsEarnedColumn.setCellFactory(col -> new TableCell<Student, Double>() {
            @Override
            protected void updateItem(Double credits, boolean empty) {
                super.updateItem(credits, empty);
                if (empty || credits == null) {
                    setText("");
                } else {
                    setText(String.format("%.1f", credits));
                }
            }
        });

        // Credits Required column
        creditsRequiredColumn.setCellValueFactory(data -> {
            Student student = data.getValue();
            if (graduationRequirementsService != null) {
                double required = graduationRequirementsService.getRequiredCreditsByGrade(student.getGradeLevel());
                return new SimpleDoubleProperty(required).asObject();
            }
            return new SimpleDoubleProperty(0.0).asObject();
        });
        creditsRequiredColumn.setCellFactory(col -> new TableCell<Student, Double>() {
            @Override
            protected void updateItem(Double credits, boolean empty) {
                super.updateItem(credits, empty);
                if (empty || credits == null) {
                    setText("");
                } else {
                    setText(String.format("%.1f", credits));
                }
            }
        });

        // Credits Behind column with color coding
        creditsBehindColumn.setCellValueFactory(data -> {
            Student student = data.getValue();
            if (graduationRequirementsService != null) {
                double behind = graduationRequirementsService.getCreditsBehind(student);
                return new SimpleDoubleProperty(behind).asObject();
            }
            return new SimpleDoubleProperty(0.0).asObject();
        });
        creditsBehindColumn.setCellFactory(col -> new TableCell<Student, Double>() {
            @Override
            protected void updateItem(Double behind, boolean empty) {
                super.updateItem(behind, empty);
                if (empty || behind == null || behind == 0.0) {
                    setText("");
                    setStyle("");
                } else {
                    setText(String.format("%.1f", behind));
                    // Color code based on severity
                    if (behind >= 3.0) {
                        setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;"); // Red - critical
                    } else if (behind >= 1.0) {
                        setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;"); // Orange - at risk
                    }
                }
            }
        });
    }

    /**
     * Setup filter dropdowns
     */
    private void setupFilters() {
        // Risk Level Filter
        if (riskLevelFilter != null) {
            riskLevelFilter.getItems().addAll(
                "All At-Risk Students",
                "At Risk Only",
                "Retention Risk Only",
                "Seniors At Risk"
            );
            riskLevelFilter.setValue("All At-Risk Students");
            riskLevelFilter.setOnAction(e -> applyFilters());
        }

        // Grade Filter
        if (gradeFilter != null) {
            gradeFilter.getItems().addAll("All Grades", "9", "10", "11", "12");
            gradeFilter.setValue("All Grades");
            gradeFilter.setOnAction(e -> applyFilters());
        }
    }

    /**
     * Setup action buttons column
     */
    private void setupActionButtons() {
        actionsColumn.setCellFactory(col -> new TableCell<Student, Void>() {
            private final Button viewBtn = new Button("View");
            private final Button notesBtn = new Button("Add Note");
            private final HBox pane = new HBox(5, viewBtn, notesBtn);

            {
                pane.setAlignment(Pos.CENTER);
                viewBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
                notesBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-cursor: hand;");

                viewBtn.setTooltip(new Tooltip("View Student Details"));
                notesBtn.setTooltip(new Tooltip("Add Counselor Note"));

                viewBtn.setOnAction(e -> {
                    Student student = getTableView().getItems().get(getIndex());
                    handleViewStudent(student);
                });

                notesBtn.setOnAction(e -> {
                    Student student = getTableView().getItems().get(getIndex());
                    handleAddNote(student);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    /**
     * Setup row factory for background colors
     */
    private void setupRowFactory() {
        atRiskTable.setRowFactory(tv -> new TableRow<Student>() {
            @Override
            protected void updateItem(Student student, boolean empty) {
                super.updateItem(student, empty);

                if (empty || student == null || graduationRequirementsService == null) {
                    setStyle("");
                    setTooltip(null);
                } else {
                    // Get background color from service
                    String bgColor = graduationRequirementsService.getStandingBackgroundColor(student);
                    setStyle("-fx-background-color: " + bgColor + ";");

                    // Add tooltip
                    String status = graduationRequirementsService.getAcademicStandingStatus(student);
                    double creditsBehind = graduationRequirementsService.getCreditsBehind(student);
                    Double gpa = student.getCurrentGPA();
                    String gpaStr = gpa != null ? String.format("%.2f", gpa) : "N/A";

                    StringBuilder tooltipText = new StringBuilder();
                    tooltipText.append(String.format("%s %s\n",
                        graduationRequirementsService.getStandingIcon(student), status));
                    tooltipText.append(String.format("GPA: %s\n", gpaStr));

                    if (creditsBehind > 0) {
                        tooltipText.append(String.format("Credits Behind: %.1f\n", creditsBehind));
                    }

                    if ("Retention Risk".equals(status)) {
                        tooltipText.append("\n⚠️ URGENT: Immediate intervention required!");
                    } else if ("At Risk".equals(status)) {
                        tooltipText.append("\n⚠️ Monitor progress closely");
                    }

                    Tooltip tooltip = new Tooltip(tooltipText.toString());
                    tooltip.setWrapText(true);
                    tooltip.setMaxWidth(300);
                    setTooltip(tooltip);
                }
            }
        });
    }

    /**
     * Load at-risk students from database
     */
    private void loadAtRiskStudents() {
        try {
            log.info("Loading at-risk students...");

            if (graduationRequirementsService == null) {
                log.error("GraduationRequirementsService is null!");
                showError("Service Error", "Graduation requirements service is not available");
                return;
            }

            // Get all at-risk students (both At Risk and Retention Risk)
            List<Student> atRiskStudents = graduationRequirementsService.getAtRiskStudents();
            List<Student> retentionRiskStudents = graduationRequirementsService.getRetentionRiskStudents();

            // Combine lists (retention risk students are more critical)
            atRiskStudentsList.clear();
            atRiskStudentsList.addAll(retentionRiskStudents); // Add critical students first
            atRiskStudentsList.addAll(atRiskStudents); // Then add at-risk students

            atRiskTable.setItems(atRiskStudentsList);

            log.info("Loaded {} at-risk students ({} retention risk, {} at risk)",
                atRiskStudentsList.size(), retentionRiskStudents.size(), atRiskStudents.size());

        } catch (Exception e) {
            log.error("Error loading at-risk students", e);
            showError("Load Error", "Failed to load at-risk students: " + e.getMessage());
        }
    }

    /**
     * Apply filters to the table
     */
    private void applyFilters() {
        try {
            String riskLevel = riskLevelFilter.getValue();
            String grade = gradeFilter.getValue();

            List<Student> filteredList = atRiskStudentsList.stream()
                .filter(student -> {
                    // Risk level filter
                    if (!"All At-Risk Students".equals(riskLevel)) {
                        String status = graduationRequirementsService.getAcademicStandingStatus(student);

                        if ("At Risk Only".equals(riskLevel) && !"At Risk".equals(status)) {
                            return false;
                        }
                        if ("Retention Risk Only".equals(riskLevel) && !"Retention Risk".equals(status)) {
                            return false;
                        }
                        if ("Seniors At Risk".equals(riskLevel)) {
                            if (!"12".equals(student.getGradeLevel())) {
                                return false;
                            }
                        }
                    }

                    // Grade filter
                    if (!"All Grades".equals(grade)) {
                        if (!grade.equals(student.getGradeLevel())) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());

            atRiskTable.setItems(FXCollections.observableArrayList(filteredList));

            log.info("Applied filters: {} students shown", filteredList.size());

        } catch (Exception e) {
            log.error("Error applying filters", e);
            showError("Filter Error", "Failed to apply filters: " + e.getMessage());
        }
    }

    /**
     * Update summary statistics labels
     */
    private void updateSummaryStats() {
        try {
            if (graduationRequirementsService == null) return;

            Map<String, Integer> summary = graduationRequirementsService.getAcademicStandingSummary();

            int total = summary.getOrDefault("total", 0);
            int atRisk = summary.getOrDefault("atRisk", 0);
            int retentionRisk = summary.getOrDefault("retentionRisk", 0);

            List<Student> seniorsAtRisk = graduationRequirementsService.getSeniorsNotMeetingRequirements();

            if (totalStudentsLabel != null) {
                totalStudentsLabel.setText(String.valueOf(total));
            }
            if (atRiskCountLabel != null) {
                atRiskCountLabel.setText(String.valueOf(atRisk));
                atRiskCountLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold; -fx-font-size: 24px;");
            }
            if (retentionRiskCountLabel != null) {
                retentionRiskCountLabel.setText(String.valueOf(retentionRisk));
                retentionRiskCountLabel.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold; -fx-font-size: 24px;");
            }
            if (seniorsAtRiskLabel != null) {
                seniorsAtRiskLabel.setText(String.valueOf(seniorsAtRisk.size()));
                seniorsAtRiskLabel.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold; -fx-font-size: 24px;");
            }

            log.info("Summary stats - Total: {}, At Risk: {}, Retention Risk: {}, Seniors At Risk: {}",
                total, atRisk, retentionRisk, seniorsAtRisk.size());

        } catch (Exception e) {
            log.error("Error updating summary stats", e);
        }
    }

    // ========================================================================
    // EVENT HANDLERS
    // ========================================================================

    @FXML
    private void handleRefresh() {
        log.info("Refreshing at-risk students list...");
        loadAtRiskStudents();
        updateSummaryStats();
        applyFilters();
    }

    @FXML
    private void handleExportPDF() {
        try {
            log.info("Export to PDF requested");

            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Save At-Risk Students Report PDF");
            fileChooser.setInitialFileName("at_risk_students_" +
                java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf");
            fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            java.io.File file = fileChooser.showSaveDialog(atRiskTable.getScene().getWindow());
            if (file != null) {
                // Get filtered students
                List<Student> students = atRiskTable.getItems();

                // Generate PDF
                com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4.rotate());
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                com.itextpdf.text.pdf.PdfWriter.getInstance(document, baos);

                document.open();

                // Add title
                com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
                com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph(
                    "At-Risk Students Report", titleFont);
                title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                document.add(title);

                // Add summary statistics
                com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
                com.itextpdf.text.Paragraph summary = new com.itextpdf.text.Paragraph();
                summary.add(new com.itextpdf.text.Chunk("Report Generated: " +
                    java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a")) + "\n", normalFont));
                summary.add(new com.itextpdf.text.Chunk("Total At-Risk Students: " + students.size() + "\n", normalFont));
                summary.add(new com.itextpdf.text.Chunk("At-Risk: " + atRiskCountLabel.getText() + " | ", normalFont));
                summary.add(new com.itextpdf.text.Chunk("Retention Risk: " + retentionRiskCountLabel.getText() + " | ", normalFont));
                summary.add(new com.itextpdf.text.Chunk("Seniors At Risk: " + seniorsAtRiskLabel.getText() + "\n\n", normalFont));
                document.add(summary);

                // Add table
                com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(8);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{10, 10, 15, 8, 8, 10, 10, 10});

                // Headers
                com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.BOLD);
                addTableHeader(table, "Status", headerFont);
                addTableHeader(table, "Student ID", headerFont);
                addTableHeader(table, "Name", headerFont);
                addTableHeader(table, "Grade", headerFont);
                addTableHeader(table, "GPA", headerFont);
                addTableHeader(table, "Credits Earned", headerFont);
                addTableHeader(table, "Credits Required", headerFont);
                addTableHeader(table, "Credits Behind", headerFont);

                // Data rows
                com.itextpdf.text.Font dataFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 8);
                for (Student student : students) {
                    String status = graduationRequirementsService != null ?
                        graduationRequirementsService.getAcademicStandingStatus(student) : "N/A";
                    double creditsRequired = graduationRequirementsService != null ?
                        graduationRequirementsService.getRequiredCreditsByGrade(student.getGradeLevel()) : 0.0;
                    double creditsBehind = graduationRequirementsService != null ?
                        graduationRequirementsService.getCreditsBehind(student) : 0.0;

                    addTableCell(table, status, dataFont);
                    addTableCell(table, student.getStudentId() != null ? student.getStudentId() : "", dataFont);
                    addTableCell(table, student.getFullName(), dataFont);
                    addTableCell(table, student.getGradeLevel() != null ? student.getGradeLevel() : "", dataFont);
                    addTableCell(table, student.getCurrentGPA() != null ?
                        String.format("%.2f", student.getCurrentGPA()) : "N/A", dataFont);
                    addTableCell(table, student.getCreditsEarned() != null ?
                        String.format("%.1f", student.getCreditsEarned()) : "0.0", dataFont);
                    addTableCell(table, String.format("%.1f", creditsRequired), dataFont);
                    addTableCell(table, creditsBehind > 0 ? String.format("%.1f", creditsBehind) : "", dataFont);
                }

                document.add(table);

                // Add footer
                document.add(new com.itextpdf.text.Paragraph("\n"));
                com.itextpdf.text.Paragraph footer = new com.itextpdf.text.Paragraph(
                    "This report identifies students who are at risk academically and may need additional support.", normalFont);
                footer.setAlignment(com.itextpdf.text.Element.ALIGN_LEFT);
                document.add(footer);

                document.close();

                // Save to file
                java.nio.file.Files.write(file.toPath(), baos.toByteArray());

                showInfo("Export Successful", "At-risk students report exported to PDF successfully!\n\nFile: " + file.getAbsolutePath());
                log.info("At-risk students report exported to PDF: {}", file.getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("Error exporting to PDF", e);
            showError("Export Error", "Failed to export PDF: " + e.getMessage());
        }
    }

    @FXML
    private void handleExportExcel() {
        try {
            log.info("Export to Excel (CSV) requested");

            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Save At-Risk Students Report CSV");
            fileChooser.setInitialFileName("at_risk_students_" +
                java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".csv");
            fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));

            java.io.File file = fileChooser.showSaveDialog(atRiskTable.getScene().getWindow());
            if (file != null) {
                // Get filtered students
                List<Student> students = atRiskTable.getItems();

                // Generate CSV
                StringBuilder csv = new StringBuilder();
                csv.append("Status,Student ID,Name,Grade,GPA,Credits Earned,Credits Required,Credits Behind,Email,Advisor Notes\n");

                for (Student student : students) {
                    String status = graduationRequirementsService != null ?
                        graduationRequirementsService.getAcademicStandingStatus(student) : "N/A";
                    double creditsRequired = graduationRequirementsService != null ?
                        graduationRequirementsService.getRequiredCreditsByGrade(student.getGradeLevel()) : 0.0;
                    double creditsBehind = graduationRequirementsService != null ?
                        graduationRequirementsService.getCreditsBehind(student) : 0.0;

                    csv.append(escapeCsv(status)).append(",");
                    csv.append(escapeCsv(student.getStudentId())).append(",");
                    csv.append(escapeCsv(student.getFullName())).append(",");
                    csv.append(escapeCsv(student.getGradeLevel())).append(",");
                    csv.append(student.getCurrentGPA() != null ?
                        String.format("%.2f", student.getCurrentGPA()) : "").append(",");
                    csv.append(student.getCreditsEarned() != null ?
                        String.format("%.1f", student.getCreditsEarned()) : "0.0").append(",");
                    csv.append(String.format("%.1f", creditsRequired)).append(",");
                    csv.append(creditsBehind > 0 ? String.format("%.1f", creditsBehind) : "").append(",");
                    csv.append(escapeCsv(student.getEmail())).append(",");
                    csv.append(escapeCsv(student.getAdvisorNotes())).append("\n");
                }

                // Save to file
                java.nio.file.Files.writeString(file.toPath(), csv.toString(),
                    java.nio.charset.StandardCharsets.UTF_8);

                showInfo("Export Successful", "At-risk students report exported to CSV successfully!\n\nFile: " + file.getAbsolutePath());
                log.info("At-risk students report exported to CSV: {}", file.getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("Error exporting to CSV", e);
            showError("Export Error", "Failed to export CSV: " + e.getMessage());
        }
    }

    private void addTableHeader(com.itextpdf.text.pdf.PdfPTable table, String text, com.itextpdf.text.Font font) {
        com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(
            new com.itextpdf.text.Phrase(text, font));
        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addTableCell(com.itextpdf.text.pdf.PdfPTable table, String text, com.itextpdf.text.Font font) {
        com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(
            new com.itextpdf.text.Phrase(text, font));
        cell.setPadding(4);
        table.addCell(cell);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void handleViewStudent(Student student) {
        log.info("View student details: {}", student.getStudentId());

        // Show detailed dialog with graduation requirements
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Student Details - " + student.getFullName());
        alert.setHeaderText("Academic Standing Report");

        StringBuilder content = new StringBuilder();
        content.append(String.format("Student ID: %s\n", student.getStudentId()));
        content.append(String.format("Grade: %s\n", student.getGradeLevel()));
        content.append(String.format("Email: %s\n\n", student.getEmail() != null ? student.getEmail() : "N/A"));

        // Add graduation status details
        if (graduationRequirementsService != null) {
            String status = graduationRequirementsService.getAcademicStandingStatus(student);
            content.append(String.format("Status: %s %s\n\n",
                graduationRequirementsService.getStandingIcon(student), status));

            Double gpa = student.getCurrentGPA();
            if (gpa != null) {
                content.append(String.format("Current GPA: %.2f\n", gpa));
            }

            Double creditsEarned = student.getCreditsEarned();
            if (creditsEarned != null) {
                content.append(String.format("Credits Earned: %.1f\n", creditsEarned));
            }

            double creditsRequired = graduationRequirementsService.getRequiredCreditsByGrade(student.getGradeLevel());
            content.append(String.format("Credits Required: %.1f\n", creditsRequired));

            double creditsBehind = graduationRequirementsService.getCreditsBehind(student);
            if (creditsBehind > 0) {
                content.append(String.format("Credits Behind: %.1f\n", creditsBehind));
            }

            content.append("\n");
            content.append(graduationRequirementsService.getStandingTooltip(student));
        }

        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    private void handleAddNote(Student student) {
        log.info("Add note for student: {}", student.getStudentId());

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Counselor Note");
        dialog.setHeaderText("Add note for " + student.getFullName());
        dialog.setContentText("Note:");

        dialog.showAndWait().ifPresent(note -> {
            if (!note.trim().isEmpty()) {
                // Append to advisor notes
                String currentNotes = student.getAdvisorNotes() != null ? student.getAdvisorNotes() : "";
                String timestamp = java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                String newNote = String.format("[%s] %s\n%s", timestamp, note, currentNotes);
                student.setAdvisorNotes(newNote);

                studentService.save(student);

                log.info("Added note for student {}", student.getStudentId());
                showInfo("Note Added", "Counselor note has been added successfully.");
            }
        });
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    private void showError(String title, String message) {
        CopyableErrorDialog.showError(title, message);
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
