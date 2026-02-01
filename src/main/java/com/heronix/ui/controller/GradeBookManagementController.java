package com.heronix.ui.controller;

import com.heronix.model.domain.*;
import com.heronix.repository.*;
import com.heronix.service.CourseService;
import com.heronix.service.GradebookService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class GradeBookManagementController {

    @Autowired
    private GradebookService gradebookService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private AssignmentGradeRepository assignmentGradeRepository;

    @Autowired
    private GradingCategoryRepository gradingCategoryRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private com.heronix.service.GradeImportService gradeImportService;

    // Header Components
    @FXML private ComboBox<Course> courseSelectionComboBox;
    @FXML private Label studentCountLabel;
    @FXML private Label assignmentCountLabel;
    @FXML private Label classAverageLabel;
    @FXML private Label ungradedCountLabel;

    // Category Weights
    @FXML private TextField testsWeightField;
    @FXML private TextField quizzesWeightField;
    @FXML private TextField homeworkWeightField;
    @FXML private TextField participationWeightField;
    @FXML private Label totalWeightLabel;

    // Assignment Management
    @FXML private ListView<Assignment> assignmentListView;

    // Grade Sheet
    @FXML private ToggleButton gridViewToggle;
    @FXML private ToggleButton listViewToggle;
    @FXML private ComboBox<String> categoryFilterComboBox;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private TextField studentSearchField;
    @FXML private CheckBox showMissingOnlyCheckBox;
    @FXML private TableView<StudentGradeRow> gradeSheetTableView;
    @FXML private TableColumn<StudentGradeRow, String> studentNameColumn;
    @FXML private TableColumn<StudentGradeRow, String> studentIdColumn;
    @FXML private TableColumn<StudentGradeRow, String> currentGradeColumn;
    @FXML private TableColumn<StudentGradeRow, String> letterGradeColumn;

    // Bottom Status
    @FXML private Label gradeDistributionLabel;
    @FXML private ProgressBar gradingProgressBar;
    @FXML private Label gradingProgressLabel;
    @FXML private Label statusLabel;
    @FXML private Label lastSavedLabel;
    @FXML private Label selectedAssignmentLabel;

    // Data
    private ObservableList<Assignment> assignments = FXCollections.observableArrayList();
    private ObservableList<StudentGradeRow> studentGradeRows = FXCollections.observableArrayList();
    private ObservableList<StudentGradeRow> allStudentGradeRows = FXCollections.observableArrayList();
    private List<GradingCategory> currentCategories = new ArrayList<>();
    private Course selectedCourse = null;
    private boolean hasUnsavedChanges = false;
    private Assignment selectedAssignment = null;

    @FXML
    public void initialize() {
        log.info("Initializing GradeBookManagementController");
        setupCourseSelection();
        setupCategoryWeights();
        setupAssignmentList();
        setupGradeSheet();
        setupFilters();
        setupToggleGroup();
        loadCourses();
        statusLabel.setText("Ready");
    }

    private void setupCourseSelection() {
        courseSelectionComboBox.setConverter(new StringConverter<Course>() {
            @Override
            public String toString(Course course) {
                return course == null ? "" : course.getCourseCode() + " - " + course.getCourseName();
            }

            @Override
            public Course fromString(String string) {
                return null;
            }
        });

        courseSelectionComboBox.setOnAction(e -> {
            Course selected = courseSelectionComboBox.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selectedCourse = selected;
                loadCourseData(selected);
            }
        });
    }

    private void setupCategoryWeights() {
        testsWeightField.textProperty().addListener((obs, old, newVal) -> updateTotalWeight());
        quizzesWeightField.textProperty().addListener((obs, old, newVal) -> updateTotalWeight());
        homeworkWeightField.textProperty().addListener((obs, old, newVal) -> updateTotalWeight());
        participationWeightField.textProperty().addListener((obs, old, newVal) -> updateTotalWeight());
    }

    private void updateTotalWeight() {
        try {
            double tests = Double.parseDouble(testsWeightField.getText());
            double quizzes = Double.parseDouble(quizzesWeightField.getText());
            double homework = Double.parseDouble(homeworkWeightField.getText());
            double participation = Double.parseDouble(participationWeightField.getText());

            double total = tests + quizzes + homework + participation;
            totalWeightLabel.setText(String.format("Total: %.0f%%", total));

            if (Math.abs(total - 100.0) < 0.01) {
                totalWeightLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #4caf50;");
            } else {
                totalWeightLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #f44336;");
            }
        } catch (NumberFormatException e) {
            totalWeightLabel.setText("Total: Invalid");
            totalWeightLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #f44336;");
        }
    }

    private void setupAssignmentList() {
        assignmentListView.setItems(assignments);
        assignmentListView.setCellFactory(lv -> new ListCell<Assignment>() {
            @Override
            protected void updateItem(Assignment assignment, boolean empty) {
                super.updateItem(assignment, empty);
                if (empty || assignment == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    String categoryName = assignment.getCategory() != null ? assignment.getCategory().getName() : "Uncategorized";
                    String dueDate = assignment.getDueDate() != null ? assignment.getDueDate().toString() : "No date";
                    double maxPts = assignment.getMaxPoints() != null ? assignment.getMaxPoints() : 0;
                    setText(String.format("%s\n%s • %.0f pts • Due: %s",
                            assignment.getTitle(),
                            categoryName,
                            maxPts,
                            dueDate));
                    setStyle("-fx-padding: 8; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
                }
            }
        });

        assignmentListView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            selectedAssignment = newVal;
            if (newVal != null) {
                selectedAssignmentLabel.setText("Selected: " + newVal.getTitle());
            } else {
                selectedAssignmentLabel.setText("No assignment selected");
            }
        });
    }

    private void setupGradeSheet() {
        studentNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStudentName()));
        studentIdColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStudentId()));

        currentGradeColumn.setCellValueFactory(cellData -> {
            double grade = cellData.getValue().getCalculatedGrade();
            return new SimpleStringProperty(String.format("%.2f%%", grade));
        });

        letterGradeColumn.setCellValueFactory(cellData -> {
            double grade = cellData.getValue().getCalculatedGrade();
            return new SimpleStringProperty(getLetterGrade(grade));
        });

        letterGradeColumn.setCellFactory(column -> new TableCell<StudentGradeRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String style = "-fx-font-weight: bold; -fx-alignment: center;";
                    if (item.startsWith("A")) style += " -fx-text-fill: #4caf50;";
                    else if (item.startsWith("B")) style += " -fx-text-fill: #8bc34a;";
                    else if (item.startsWith("C")) style += " -fx-text-fill: #ff9800;";
                    else if (item.startsWith("D")) style += " -fx-text-fill: #ff5722;";
                    else if (item.startsWith("F")) style += " -fx-text-fill: #f44336;";
                    setStyle(style);
                }
            }
        });

        gradeSheetTableView.setItems(studentGradeRows);
    }

    private void setupFilters() {
        categoryFilterComboBox.setItems(FXCollections.observableArrayList("All Categories"));
        categoryFilterComboBox.getSelectionModel().selectFirst();

        statusFilterComboBox.setItems(FXCollections.observableArrayList(
                "All Status", "Graded", "Ungraded", "Missing", "Late"
        ));
        statusFilterComboBox.getSelectionModel().selectFirst();

        categoryFilterComboBox.setOnAction(e -> applyFilters());
        statusFilterComboBox.setOnAction(e -> applyFilters());
        studentSearchField.textProperty().addListener((obs, old, newVal) -> applyFilters());
        showMissingOnlyCheckBox.selectedProperty().addListener((obs, old, newVal) -> applyFilters());
    }

    private void setupToggleGroup() {
        ToggleGroup viewToggleGroup = new ToggleGroup();
        gridViewToggle.setToggleGroup(viewToggleGroup);
        listViewToggle.setToggleGroup(viewToggleGroup);
        gridViewToggle.setSelected(true);
    }

    private void loadCourses() {
        statusLabel.setText("Loading courses...");
        new Thread(() -> {
            try {
                List<Course> courseList = courseService.getAllActiveCourses();
                Platform.runLater(() -> {
                    courseSelectionComboBox.setItems(FXCollections.observableArrayList(courseList));
                    if (!courseList.isEmpty()) {
                        courseSelectionComboBox.getSelectionModel().selectFirst();
                    }
                    statusLabel.setText("Loaded " + courseList.size() + " courses");
                });
            } catch (Exception e) {
                log.error("Failed to load courses", e);
                Platform.runLater(() -> statusLabel.setText("Error loading courses: " + e.getMessage()));
            }
        }).start();
    }

    private void loadCourseData(Course course) {
        statusLabel.setText("Loading gradebook for " + course.getCourseName() + "...");

        new Thread(() -> {
            try {
                Long courseId = course.getId();

                // Load categories
                List<GradingCategory> categories = gradebookService.getCategoriesForCourse(courseId);
                if (categories.isEmpty()) {
                    categories = gradebookService.createDefaultCategories(courseId);
                }

                // Load assignments
                List<Assignment> courseAssignments = gradebookService.getAssignmentsForCourse(courseId);

                // Load the class gradebook (includes student grades)
                GradebookService.ClassGradebook gradebook = gradebookService.getClassGradebook(courseId);

                // Get enrolled students
                List<Student> students = course.getStudents() != null
                        ? new ArrayList<>(course.getStudents())
                        : List.of();

                // Build grade rows for each student
                List<StudentGradeRow> rows = new ArrayList<>();
                for (Student student : students) {
                    StudentGradeRow row = new StudentGradeRow();
                    row.setStudentDbId(student.getId());
                    row.setStudentId(student.getStudentId());
                    row.setStudentName(student.getFullName());

                    // Load individual assignment grades
                    List<AssignmentGrade> studentGrades = assignmentGradeRepository
                            .findByStudentIdAndCourseId(student.getId(), courseId);

                    Map<Long, Double> gradeMap = new HashMap<>();
                    for (AssignmentGrade ag : studentGrades) {
                        if (ag.getScore() != null && ag.getAssignment() != null) {
                            gradeMap.put(ag.getAssignment().getId(), ag.getScore());
                        }
                    }
                    row.setGrades(gradeMap);

                    // Get calculated grade from gradebook service
                    GradebookService.StudentCourseGrade scg = gradebook.getStudentGrades().stream()
                            .filter(g -> g.getStudentId().equals(student.getId()))
                            .findFirst().orElse(null);
                    row.setCalculatedGrade(scg != null ? scg.getFinalPercentage() : 0.0);

                    rows.add(row);
                }

                final List<GradingCategory> finalCategories = categories;
                final List<Assignment> finalAssignments = courseAssignments;

                Platform.runLater(() -> {
                    currentCategories = finalCategories;

                    // Update category weight fields
                    updateCategoryWeightFields(finalCategories);

                    // Update category filter
                    List<String> categoryNames = new ArrayList<>();
                    categoryNames.add("All Categories");
                    finalCategories.forEach(c -> categoryNames.add(c.getName()));
                    categoryFilterComboBox.setItems(FXCollections.observableArrayList(categoryNames));
                    categoryFilterComboBox.getSelectionModel().selectFirst();

                    // Clear and rebuild table
                    assignments.clear();
                    assignments.addAll(finalAssignments);

                    // Remove dynamic assignment columns
                    gradeSheetTableView.getColumns().removeIf(col ->
                            col != studentNameColumn &&
                            col != studentIdColumn &&
                            col != currentGradeColumn &&
                            col != letterGradeColumn);

                    // Add dynamic columns for each assignment
                    int columnIndex = 2;
                    for (Assignment assignment : finalAssignments) {
                        TableColumn<StudentGradeRow, String> assignmentCol = new TableColumn<>(
                                assignment.getTitle() + "\n(" + (assignment.getMaxPoints() != null ? assignment.getMaxPoints().intValue() : 0) + " pts)");

                        final Long assignmentId = assignment.getId();
                        final double maxPoints = assignment.getMaxPoints() != null ? assignment.getMaxPoints() : 100.0;

                        assignmentCol.setCellValueFactory(cellData -> {
                            Double grade = cellData.getValue().getGrades().get(assignmentId);
                            if (grade == null) return new SimpleStringProperty("—");
                            return new SimpleStringProperty(String.format("%.1f", grade));
                        });

                        assignmentCol.setCellFactory(TextFieldTableCell.forTableColumn());
                        assignmentCol.setOnEditCommit(event -> {
                            String newValue = event.getNewValue();
                            try {
                                double score = Double.parseDouble(newValue);
                                if (score >= 0 && score <= maxPoints) {
                                    StudentGradeRow row = event.getRowValue();
                                    row.getGrades().put(assignmentId, score);

                                    // Save to database
                                    new Thread(() -> {
                                        try {
                                            gradebookService.enterGrade(
                                                    row.getStudentDbId(), assignmentId,
                                                    score, LocalDate.now(), null);
                                            Platform.runLater(() -> {
                                                markUnsaved();
                                                recalculateStudentGrade(row, course.getId());
                                                updateAllStatistics();
                                                gradeSheetTableView.refresh();
                                            });
                                        } catch (Exception ex) {
                                            log.error("Failed to save grade", ex);
                                            Platform.runLater(() ->
                                                    showAlert("Error", "Failed to save grade: " + ex.getMessage()));
                                        }
                                    }).start();
                                } else {
                                    showAlert("Invalid Grade", "Grade must be between 0 and " + (int) maxPoints);
                                    gradeSheetTableView.refresh();
                                }
                            } catch (NumberFormatException e) {
                                showAlert("Invalid Input", "Please enter a valid number");
                                gradeSheetTableView.refresh();
                            }
                        });

                        assignmentCol.setPrefWidth(80);
                        assignmentCol.setStyle("-fx-alignment: CENTER;");
                        gradeSheetTableView.getColumns().add(columnIndex++, assignmentCol);
                    }

                    // Load student rows
                    allStudentGradeRows.clear();
                    allStudentGradeRows.addAll(rows);
                    studentGradeRows.clear();
                    studentGradeRows.addAll(rows);

                    gradeSheetTableView.setEditable(true);
                    updateAllStatistics();
                    statusLabel.setText("Loaded gradebook for " + course.getCourseName() +
                            " (" + rows.size() + " students, " + finalAssignments.size() + " assignments)");
                });
            } catch (Exception e) {
                log.error("Failed to load course data for {}", course.getCourseCode(), e);
                Platform.runLater(() -> statusLabel.setText("Error: " + e.getMessage()));
            }
        }).start();
    }

    private void updateCategoryWeightFields(List<GradingCategory> categories) {
        testsWeightField.setText("0");
        quizzesWeightField.setText("0");
        homeworkWeightField.setText("0");
        participationWeightField.setText("0");

        for (GradingCategory cat : categories) {
            String name = cat.getName().toLowerCase();
            String weight = String.format("%.0f", cat.getWeight() != null ? cat.getWeight() : 0.0);
            if (name.contains("test") || name.contains("exam")) {
                testsWeightField.setText(weight);
            } else if (name.contains("quiz")) {
                quizzesWeightField.setText(weight);
            } else if (name.contains("homework") || name.contains("project")) {
                homeworkWeightField.setText(weight);
            } else if (name.contains("participation")) {
                participationWeightField.setText(weight);
            }
        }
        updateTotalWeight();
    }

    private void recalculateStudentGrade(StudentGradeRow row, Long courseId) {
        try {
            GradebookService.StudentCourseGrade scg = gradebookService.calculateCourseGrade(row.getStudentDbId(), courseId);
            row.setCalculatedGrade(scg.getFinalPercentage());
        } catch (Exception e) {
            log.warn("Failed to recalculate grade for student {}: {}", row.getStudentId(), e.getMessage());
        }
    }

    private String getLetterGrade(double percentage) {
        if (percentage >= 97) return "A+";
        if (percentage >= 93) return "A";
        if (percentage >= 90) return "A-";
        if (percentage >= 87) return "B+";
        if (percentage >= 83) return "B";
        if (percentage >= 80) return "B-";
        if (percentage >= 77) return "C+";
        if (percentage >= 73) return "C";
        if (percentage >= 70) return "C-";
        if (percentage >= 67) return "D+";
        if (percentage >= 63) return "D";
        if (percentage >= 60) return "D-";
        return "F";
    }

    private void updateAllStatistics() {
        updateQuickStats();
        updateGradeDistribution();
        updateGradingProgress();
    }

    private void updateQuickStats() {
        studentCountLabel.setText(String.valueOf(studentGradeRows.size()));
        assignmentCountLabel.setText(String.valueOf(assignments.size()));

        double classAverage = studentGradeRows.stream()
                .mapToDouble(StudentGradeRow::getCalculatedGrade)
                .average()
                .orElse(0.0);
        classAverageLabel.setText(String.format("%.1f%%", classAverage));

        long ungradedCount = 0;
        for (StudentGradeRow student : studentGradeRows) {
            for (Assignment assignment : assignments) {
                if (student.getGrades().get(assignment.getId()) == null) {
                    ungradedCount++;
                }
            }
        }
        ungradedCountLabel.setText(String.valueOf(ungradedCount));
    }

    private void updateGradeDistribution() {
        Map<String, Long> distribution = studentGradeRows.stream()
                .collect(Collectors.groupingBy(
                        student -> {
                            String letter = getLetterGrade(student.getCalculatedGrade());
                            return letter.substring(0, 1); // Group by first letter
                        },
                        Collectors.counting()
                ));

        gradeDistributionLabel.setText(String.format(
                "A: %d | B: %d | C: %d | D: %d | F: %d",
                distribution.getOrDefault("A", 0L),
                distribution.getOrDefault("B", 0L),
                distribution.getOrDefault("C", 0L),
                distribution.getOrDefault("D", 0L),
                distribution.getOrDefault("F", 0L)
        ));
    }

    private void updateGradingProgress() {
        long totalSlots = (long) studentGradeRows.size() * assignments.size();
        long gradedSlots = 0;

        for (StudentGradeRow student : studentGradeRows) {
            for (Assignment assignment : assignments) {
                if (student.getGrades().get(assignment.getId()) != null) {
                    gradedSlots++;
                }
            }
        }

        double progress = totalSlots > 0 ? (double) gradedSlots / totalSlots : 0.0;
        gradingProgressBar.setProgress(progress);
        gradingProgressLabel.setText(String.format("%.0f%% graded", progress * 100));
    }

    private void applyFilters() {
        String searchText = studentSearchField.getText() != null ? studentSearchField.getText().toLowerCase() : "";
        String categoryFilter = categoryFilterComboBox.getSelectionModel().getSelectedItem();

        studentGradeRows.clear();
        studentGradeRows.addAll(allStudentGradeRows.stream()
                .filter(row -> {
                    if (!searchText.isEmpty()) {
                        return row.getStudentName().toLowerCase().contains(searchText) ||
                                row.getStudentId().toLowerCase().contains(searchText);
                    }
                    return true;
                })
                .filter(row -> {
                    if (showMissingOnlyCheckBox.isSelected()) {
                        return assignments.stream().anyMatch(a -> row.getGrades().get(a.getId()) == null);
                    }
                    return true;
                })
                .collect(Collectors.toList()));

        updateAllStatistics();
        statusLabel.setText("Filters applied - showing " + studentGradeRows.size() + " students");
    }

    private void markUnsaved() {
        hasUnsavedChanges = true;
        lastSavedLabel.setText("Unsaved changes");
        lastSavedLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #f44336;");
    }

    private void markSaved() {
        hasUnsavedChanges = false;
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        lastSavedLabel.setText("Last saved: " + timestamp);
        lastSavedLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #4caf50;");
    }

    // Event Handlers

    @FXML
    private void handleSaveWeights() {
        if (selectedCourse == null) {
            showAlert("No Course", "Please select a course first");
            return;
        }

        try {
            Map<Long, Double> weightUpdates = new HashMap<>();
            double total = 0;

            for (GradingCategory cat : currentCategories) {
                String name = cat.getName().toLowerCase();
                double weight;
                if (name.contains("test") || name.contains("exam")) {
                    weight = Double.parseDouble(testsWeightField.getText());
                } else if (name.contains("quiz")) {
                    weight = Double.parseDouble(quizzesWeightField.getText());
                } else if (name.contains("homework") || name.contains("project")) {
                    weight = Double.parseDouble(homeworkWeightField.getText());
                } else if (name.contains("participation")) {
                    weight = Double.parseDouble(participationWeightField.getText());
                } else {
                    continue;
                }
                weightUpdates.put(cat.getId(), weight);
                total += weight;
            }

            if (Math.abs(total - 100.0) > 0.01) {
                showAlert("Invalid Weights", "Category weights must total 100%. Current total: " + total + "%");
                return;
            }

            gradebookService.updateCategoryWeights(weightUpdates);
            markSaved();
            gradeSheetTableView.refresh();
            updateAllStatistics();
            statusLabel.setText("Category weights saved successfully");
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter valid numbers for all category weights");
        } catch (Exception e) {
            log.error("Failed to save weights", e);
            showAlert("Error", "Failed to save weights: " + e.getMessage());
        }
    }

    @FXML
    private void handleNewAssignment() {
        if (selectedCourse == null) {
            showAlert("No Course", "Please select a course first");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Assignment");
        dialog.setHeaderText("Create New Assignment");
        dialog.setContentText("Assignment name:");

        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                new Thread(() -> {
                    try {
                        // Use first category as default
                        Long categoryId = currentCategories.isEmpty() ? null : currentCategories.get(0).getId();
                        Assignment created = gradebookService.createAssignment(
                                selectedCourse.getId(), categoryId, name, 100.0, LocalDate.now().plusWeeks(1));

                        Platform.runLater(() -> {
                            statusLabel.setText("Assignment '" + name + "' created");
                            loadCourseData(selectedCourse);
                        });
                    } catch (Exception e) {
                        log.error("Failed to create assignment", e);
                        Platform.runLater(() -> showAlert("Error", "Failed to create assignment: " + e.getMessage()));
                    }
                }).start();
            }
        });
    }

    @FXML
    private void handleImportGrades() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Grades from CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showOpenDialog(gradeSheetTableView.getScene().getWindow());
        if (file != null) {
            statusLabel.setText("Importing grades from " + file.getName() + "...");
            new Thread(() -> {
                try (FileInputStream fis = new FileInputStream(file)) {
                    Map<String, String> fieldMapping = new HashMap<>();
                    fieldMapping.put("student_id", "studentId");
                    fieldMapping.put("assignment", "assignmentName");
                    fieldMapping.put("score", "score");
                    fieldMapping.put("max_score", "maxScore");
                    var result = gradeImportService.importFromCSV(fis, fieldMapping);
                    Platform.runLater(() -> {
                        statusLabel.setText("Import complete: " + result.getSuccessCount() + " grades imported, "
                                + result.getErrorCount() + " errors");
                        if (selectedCourse != null) {
                            loadCourseData(selectedCourse);
                        }
                    });
                } catch (Exception e) {
                    log.error("CSV import failed", e);
                    Platform.runLater(() -> statusLabel.setText("Import failed: " + e.getMessage()));
                }
            }).start();
        }
    }

    @FXML
    private void handleExportGradeBook() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Grade Book");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showSaveDialog(gradeSheetTableView.getScene().getWindow());
        if (file != null) {
            statusLabel.setText("Exporting grade book...");
            new Thread(() -> {
                try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                    // BOM for Excel
                    writer.write('\ufeff');

                    // Header row
                    StringBuilder header = new StringBuilder("Student Name,Student ID");
                    for (Assignment a : assignments) {
                        header.append(",").append(a.getTitle()).append(" (").append(a.getMaxPoints() != null ? a.getMaxPoints().intValue() : 0).append(" pts)");
                    }
                    header.append(",Current Grade,Letter Grade");
                    writer.write(header.toString());
                    writer.newLine();

                    // Data rows
                    for (StudentGradeRow row : allStudentGradeRows) {
                        StringBuilder line = new StringBuilder();
                        line.append("\"").append(row.getStudentName()).append("\",");
                        line.append(row.getStudentId());
                        for (Assignment a : assignments) {
                            Double grade = row.getGrades().get(a.getId());
                            line.append(",").append(grade != null ? String.format("%.1f", grade) : "");
                        }
                        line.append(",").append(String.format("%.2f%%", row.getCalculatedGrade()));
                        line.append(",").append(getLetterGrade(row.getCalculatedGrade()));
                        writer.write(line.toString());
                        writer.newLine();
                    }

                    Platform.runLater(() -> statusLabel.setText("Grade book exported to " + file.getName()));
                } catch (IOException e) {
                    log.error("Failed to export gradebook", e);
                    Platform.runLater(() -> showAlert("Error", "Failed to export: " + e.getMessage()));
                }
            }).start();
        }
    }

    @FXML
    private void handleCalculateAll() {
        if (selectedCourse == null) return;

        statusLabel.setText("Recalculating all grades...");
        new Thread(() -> {
            try {
                GradebookService.ClassGradebook gradebook = gradebookService.getClassGradebook(selectedCourse.getId());
                Platform.runLater(() -> {
                    for (StudentGradeRow row : allStudentGradeRows) {
                        gradebook.getStudentGrades().stream()
                                .filter(g -> g.getStudentId().equals(row.getStudentDbId()))
                                .findFirst()
                                .ifPresent(g -> row.setCalculatedGrade(g.getFinalPercentage()));
                    }
                    gradeSheetTableView.refresh();
                    updateAllStatistics();
                    statusLabel.setText("All grades recalculated");
                });
            } catch (Exception e) {
                log.error("Failed to recalculate grades", e);
                Platform.runLater(() -> statusLabel.setText("Error recalculating: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handlePostGrades() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Post Grades");
        alert.setHeaderText("Post grades to student portal?");
        alert.setContentText("This will make all current grades visible to students and parents.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                statusLabel.setText("Posting grades to portal...");
                // Publish all assignments
                new Thread(() -> {
                    try {
                        for (Assignment a : assignments) {
                            if (a.getPublished() == null || !a.getPublished()) {
                                gradebookService.publishAssignment(a.getId());
                            }
                        }
                        Platform.runLater(() -> {
                            statusLabel.setText("Grades posted successfully");
                            showAlert("Success", "Grades have been posted to the student portal");
                        });
                    } catch (Exception e) {
                        log.error("Failed to post grades", e);
                        Platform.runLater(() -> showAlert("Error", "Failed to post grades: " + e.getMessage()));
                    }
                }).start();
            }
        });
    }

    @FXML
    private void handleSaveAll() {
        statusLabel.setText("All changes are auto-saved to database");
        markSaved();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Data Class

    public static class StudentGradeRow {
        private Long studentDbId;
        private String studentId;
        private String studentName;
        private Map<Long, Double> grades = new HashMap<>();
        private double calculatedGrade = 0.0;

        public Long getStudentDbId() { return studentDbId; }
        public void setStudentDbId(Long studentDbId) { this.studentDbId = studentDbId; }

        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }

        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }

        public Map<Long, Double> getGrades() { return grades; }
        public void setGrades(Map<Long, Double> grades) { this.grades = grades; }

        public double getCalculatedGrade() { return calculatedGrade; }
        public void setCalculatedGrade(double calculatedGrade) { this.calculatedGrade = calculatedGrade; }
    }
}
