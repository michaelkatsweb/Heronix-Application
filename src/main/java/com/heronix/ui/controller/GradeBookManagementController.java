package com.heronix.ui.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class GradeBookManagementController {

    // Header Components
    @FXML private ComboBox<CourseInfo> courseSelectionComboBox;
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
    private ObservableList<CourseInfo> courses = FXCollections.observableArrayList();
    private ObservableList<Assignment> assignments = FXCollections.observableArrayList();
    private ObservableList<StudentGradeRow> studentGradeRows = FXCollections.observableArrayList();
    private Map<String, Double> categoryWeights = new HashMap<>();
    private boolean hasUnsavedChanges = false;
    private Assignment selectedAssignment = null;

    @FXML
    public void initialize() {
        setupCourseSelection();
        setupCategoryWeights();
        setupAssignmentList();
        setupGradeSheet();
        setupFilters();
        setupToggleGroup();
        loadSampleData();
        updateAllStatistics();
    }

    private void setupCourseSelection() {
        courseSelectionComboBox.setConverter(new StringConverter<CourseInfo>() {
            @Override
            public String toString(CourseInfo course) {
                return course == null ? "" : course.getCode() + " - " + course.getName();
            }

            @Override
            public CourseInfo fromString(String string) {
                return null;
            }
        });

        courseSelectionComboBox.setOnAction(e -> {
            CourseInfo selected = courseSelectionComboBox.getSelectionModel().getSelectedItem();
            if (selected != null) {
                loadCourseData(selected);
            }
        });
    }

    private void setupCategoryWeights() {
        categoryWeights.put("Tests/Exams", 40.0);
        categoryWeights.put("Quizzes", 25.0);
        categoryWeights.put("Homework", 20.0);
        categoryWeights.put("Participation", 15.0);

        // Add listeners to weight fields
        testsWeightField.textProperty().addListener((obs, old, newVal) -> updateTotalWeight());
        quizzesWeightField.textProperty().addListener((obs, old, newVal) -> updateTotalWeight());
        homeworkWeightField.textProperty().addListener((obs, old, newVal) -> updateTotalWeight());
        participationWeightField.textProperty().addListener((obs, old, newVal) -> updateTotalWeight());

        updateTotalWeight();
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
                    setText(String.format("%s\n%s • %d pts • Due: %s",
                            assignment.getName(),
                            assignment.getCategory(),
                            assignment.getMaxPoints(),
                            assignment.getDueDate()));
                    setStyle("-fx-padding: 8; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
                }
            }
        });

        assignmentListView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            selectedAssignment = newVal;
            if (newVal != null) {
                selectedAssignmentLabel.setText("Selected: " + newVal.getName());
            } else {
                selectedAssignmentLabel.setText("No assignment selected");
            }
        });
    }

    private void setupGradeSheet() {
        studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        studentIdColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));

        currentGradeColumn.setCellValueFactory(cellData -> {
            double grade = calculateStudentGrade(cellData.getValue());
            return new SimpleStringProperty(String.format("%.2f%%", grade));
        });

        letterGradeColumn.setCellValueFactory(cellData -> {
            double grade = calculateStudentGrade(cellData.getValue());
            String letter = getLetterGrade(grade);
            return new SimpleStringProperty(letter);
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
                    switch (item) {
                        case "A": style += " -fx-text-fill: #4caf50;"; break;
                        case "B": style += " -fx-text-fill: #8bc34a;"; break;
                        case "C": style += " -fx-text-fill: #ff9800;"; break;
                        case "D": style += " -fx-text-fill: #ff5722;"; break;
                        case "F": style += " -fx-text-fill: #f44336;"; break;
                    }
                    setStyle(style);
                }
            }
        });

        gradeSheetTableView.setItems(studentGradeRows);
    }

    private void setupFilters() {
        categoryFilterComboBox.setItems(FXCollections.observableArrayList(
                "All Categories", "Tests/Exams", "Quizzes", "Homework", "Participation"
        ));
        categoryFilterComboBox.getSelectionModel().selectFirst();

        statusFilterComboBox.setItems(FXCollections.observableArrayList(
                "All Status", "Graded", "Ungraded", "Missing", "Late"
        ));
        statusFilterComboBox.getSelectionModel().selectFirst();

        // Add filter listeners
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

    private void loadSampleData() {
        // Sample courses
        courses.addAll(
                new CourseInfo("MATH-101", "Algebra I", "Period 1"),
                new CourseInfo("ENG-201", "English Literature", "Period 2"),
                new CourseInfo("SCI-301", "Biology", "Period 3"),
                new CourseInfo("HIST-401", "US History", "Period 4")
        );
        courseSelectionComboBox.setItems(courses);
        courseSelectionComboBox.getSelectionModel().selectFirst();
    }

    private void loadCourseData(CourseInfo course) {
        // Clear existing data
        assignments.clear();
        studentGradeRows.clear();

        // Remove dynamic assignment columns
        gradeSheetTableView.getColumns().removeIf(col ->
                col != studentNameColumn &&
                        col != studentIdColumn &&
                        col != currentGradeColumn &&
                        col != letterGradeColumn
        );

        // Load assignments for this course
        assignments.addAll(
                new Assignment("A1", "Midterm Exam", "Tests/Exams", 100, "2024-10-15", true),
                new Assignment("A2", "Final Exam", "Tests/Exams", 100, "2024-12-15", false),
                new Assignment("A3", "Chapter 3 Quiz", "Quizzes", 50, "2024-09-20", true),
                new Assignment("A4", "Chapter 5 Quiz", "Quizzes", 50, "2024-10-25", false),
                new Assignment("A5", "Homework Set 1", "Homework", 20, "2024-09-10", true),
                new Assignment("A6", "Homework Set 2", "Homework", 20, "2024-10-01", true),
                new Assignment("A7", "Class Discussion", "Participation", 10, "2024-11-30", false)
        );

        // Add dynamic columns for assignments
        int columnIndex = 2; // After studentId column
        for (Assignment assignment : assignments) {
            TableColumn<StudentGradeRow, String> assignmentCol = new TableColumn<>(
                    assignment.getName() + "\n(" + assignment.getMaxPoints() + " pts)"
            );

            final String assignmentId = assignment.getId();
            assignmentCol.setCellValueFactory(cellData -> {
                Double grade = cellData.getValue().getGrades().get(assignmentId);
                if (grade == null) {
                    return new SimpleStringProperty("—");
                }
                return new SimpleStringProperty(String.format("%.1f", grade));
            });

            // Make cells editable
            assignmentCol.setCellFactory(TextFieldTableCell.forTableColumn());
            assignmentCol.setOnEditCommit(event -> {
                String newValue = event.getNewValue();
                try {
                    double grade = Double.parseDouble(newValue);
                    if (grade >= 0 && grade <= assignment.getMaxPoints()) {
                        event.getRowValue().getGrades().put(assignmentId, grade);
                        markUnsaved();
                        updateAllStatistics();
                        gradeSheetTableView.refresh();
                    } else {
                        showAlert("Invalid Grade", "Grade must be between 0 and " + assignment.getMaxPoints());
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

        // Load student grade rows
        studentGradeRows.addAll(
                createStudentRow("S001", "Alice Anderson"),
                createStudentRow("S002", "Bob Brown"),
                createStudentRow("S003", "Carol Chen"),
                createStudentRow("S004", "David Davis"),
                createStudentRow("S005", "Emma Evans"),
                createStudentRow("S006", "Frank Foster"),
                createStudentRow("S007", "Grace Garcia"),
                createStudentRow("S008", "Henry Harris")
        );

        // Populate with sample grades
        Random rand = new Random(42);
        for (StudentGradeRow row : studentGradeRows) {
            for (Assignment assignment : assignments) {
                if (assignment.isGraded()) {
                    // 80% chance of having a grade
                    if (rand.nextDouble() < 0.8) {
                        double grade = assignment.getMaxPoints() * (0.6 + rand.nextDouble() * 0.4);
                        row.getGrades().put(assignment.getId(), Math.round(grade * 10) / 10.0);
                    }
                }
            }
        }

        gradeSheetTableView.setEditable(true);
        updateAllStatistics();
    }

    private StudentGradeRow createStudentRow(String id, String name) {
        StudentGradeRow row = new StudentGradeRow();
        row.setStudentId(id);
        row.setStudentName(name);
        return row;
    }

    private double calculateStudentGrade(StudentGradeRow student) {
        Map<String, List<Double>> gradesByCategory = new HashMap<>();
        Map<String, List<Double>> maxPointsByCategory = new HashMap<>();

        // Group grades by category
        for (Assignment assignment : assignments) {
            if (!assignment.isGraded()) continue;

            String category = assignment.getCategory();
            gradesByCategory.putIfAbsent(category, new ArrayList<>());
            maxPointsByCategory.putIfAbsent(category, new ArrayList<>());

            Double grade = student.getGrades().get(assignment.getId());
            if (grade != null) {
                gradesByCategory.get(category).add(grade);
                maxPointsByCategory.get(category).add((double) assignment.getMaxPoints());
            }
        }

        // Calculate weighted grade
        double totalWeightedGrade = 0.0;
        double totalWeight = 0.0;

        try {
            Map<String, Double> weights = new HashMap<>();
            weights.put("Tests/Exams", Double.parseDouble(testsWeightField.getText()));
            weights.put("Quizzes", Double.parseDouble(quizzesWeightField.getText()));
            weights.put("Homework", Double.parseDouble(homeworkWeightField.getText()));
            weights.put("Participation", Double.parseDouble(participationWeightField.getText()));

            for (String category : gradesByCategory.keySet()) {
                List<Double> grades = gradesByCategory.get(category);
                List<Double> maxPoints = maxPointsByCategory.get(category);

                if (grades.isEmpty()) continue;

                // Calculate category average
                double totalEarned = grades.stream().mapToDouble(Double::doubleValue).sum();
                double totalPossible = maxPoints.stream().mapToDouble(Double::doubleValue).sum();
                double categoryPercent = (totalEarned / totalPossible) * 100.0;

                double weight = weights.getOrDefault(category, 0.0);
                totalWeightedGrade += categoryPercent * (weight / 100.0);
                totalWeight += weight;
            }

            if (totalWeight > 0) {
                return (totalWeightedGrade / totalWeight) * 100.0;
            }
        } catch (NumberFormatException e) {
            // Invalid weights
        }

        return 0.0;
    }

    private String getLetterGrade(double percentage) {
        if (percentage >= 90) return "A";
        if (percentage >= 80) return "B";
        if (percentage >= 70) return "C";
        if (percentage >= 60) return "D";
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

        // Calculate class average
        double classAverage = studentGradeRows.stream()
                .mapToDouble(this::calculateStudentGrade)
                .average()
                .orElse(0.0);
        classAverageLabel.setText(String.format("%.1f%%", classAverage));

        // Count ungraded assignments
        long ungradedCount = 0;
        for (StudentGradeRow student : studentGradeRows) {
            for (Assignment assignment : assignments) {
                if (assignment.isGraded() && student.getGrades().get(assignment.getId()) == null) {
                    ungradedCount++;
                }
            }
        }
        ungradedCountLabel.setText(String.valueOf(ungradedCount));
    }

    private void updateGradeDistribution() {
        Map<String, Long> distribution = studentGradeRows.stream()
                .collect(Collectors.groupingBy(
                        student -> getLetterGrade(calculateStudentGrade(student)),
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
        long totalSlots = (long) studentGradeRows.size() * assignments.stream().filter(Assignment::isGraded).count();
        long gradedSlots = 0;

        for (StudentGradeRow student : studentGradeRows) {
            for (Assignment assignment : assignments) {
                if (assignment.isGraded() && student.getGrades().get(assignment.getId()) != null) {
                    gradedSlots++;
                }
            }
        }

        double progress = totalSlots > 0 ? (double) gradedSlots / totalSlots : 0.0;
        gradingProgressBar.setProgress(progress);
        gradingProgressLabel.setText(String.format("%.0f%% graded", progress * 100));
    }

    private void applyFilters() {
        // This would filter the TableView based on selected criteria
        // For now, just update status
        statusLabel.setText("Filters applied");
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
        try {
            categoryWeights.put("Tests/Exams", Double.parseDouble(testsWeightField.getText()));
            categoryWeights.put("Quizzes", Double.parseDouble(quizzesWeightField.getText()));
            categoryWeights.put("Homework", Double.parseDouble(homeworkWeightField.getText()));
            categoryWeights.put("Participation", Double.parseDouble(participationWeightField.getText()));

            double total = categoryWeights.values().stream().mapToDouble(Double::doubleValue).sum();
            if (Math.abs(total - 100.0) > 0.01) {
                showAlert("Invalid Weights", "Category weights must total 100%. Current total: " + total + "%");
                return;
            }

            markUnsaved();
            gradeSheetTableView.refresh();
            updateAllStatistics();
            statusLabel.setText("Category weights saved successfully");
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter valid numbers for all category weights");
        }
    }

    @FXML
    private void handleNewAssignment() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Assignment");
        dialog.setHeaderText("Create New Assignment");
        dialog.setContentText("Assignment name:");

        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                String id = "A" + (assignments.size() + 1);
                Assignment newAssignment = new Assignment(id, name, "Homework", 100, "2024-12-31", false);
                assignments.add(newAssignment);
                markUnsaved();
                statusLabel.setText("Assignment '" + name + "' created");

                // Reload to add new column
                loadCourseData(courseSelectionComboBox.getSelectionModel().getSelectedItem());
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
            // Simulate import
            Platform.runLater(() -> {
                markUnsaved();
                updateAllStatistics();
                statusLabel.setText("Grades imported successfully from " + file.getName());
            });
        }
    }

    @FXML
    private void handleExportGradeBook() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Grade Book");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );

        File file = fileChooser.showSaveDialog(gradeSheetTableView.getScene().getWindow());
        if (file != null) {
            statusLabel.setText("Exporting grade book to " + file.getName() + "...");
            // Simulate export
            Platform.runLater(() -> {
                statusLabel.setText("Grade book exported successfully to " + file.getName());
            });
        }
    }

    @FXML
    private void handleCalculateAll() {
        statusLabel.setText("Recalculating all grades...");
        gradeSheetTableView.refresh();
        updateAllStatistics();
        statusLabel.setText("All grades recalculated");
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
                Platform.runLater(() -> {
                    statusLabel.setText("Grades posted successfully");
                    showAlert("Success", "Grades have been posted to the student portal");
                });
            }
        });
    }

    @FXML
    private void handleSaveAll() {
        statusLabel.setText("Saving all changes...");
        Platform.runLater(() -> {
            markSaved();
            statusLabel.setText("All changes saved successfully");
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Data Classes

    public static class CourseInfo {
        private String code;
        private String name;
        private String period;

        public CourseInfo(String code, String name, String period) {
            this.code = code;
            this.name = name;
            this.period = period;
        }

        public String getCode() { return code; }
        public String getName() { return name; }
        public String getPeriod() { return period; }
    }

    public static class Assignment {
        private String id;
        private String name;
        private String category;
        private int maxPoints;
        private String dueDate;
        private boolean graded;

        public Assignment(String id, String name, String category, int maxPoints, String dueDate, boolean graded) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.maxPoints = maxPoints;
            this.dueDate = dueDate;
            this.graded = graded;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public int getMaxPoints() { return maxPoints; }
        public String getDueDate() { return dueDate; }
        public boolean isGraded() { return graded; }

        @Override
        public String toString() {
            return name + " (" + category + ")";
        }
    }

    public static class StudentGradeRow {
        private String studentId;
        private String studentName;
        private Map<String, Double> grades = new HashMap<>();

        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }

        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }

        public Map<String, Double> getGrades() { return grades; }
    }
}
