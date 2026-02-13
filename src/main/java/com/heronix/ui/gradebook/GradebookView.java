package com.heronix.ui.gradebook;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

/**
 * Modern Gradebook View
 * A spreadsheet-style gradebook with inline editing and real-time statistics.
 *
 * Features:
 * - Spreadsheet-style grid layout
 * - Inline grade editing with keyboard navigation
 * - Assignment columns with category headers
 * - Student rows with names and averages
 * - Real-time grade statistics
 * - Filter by category, date range
 * - Bulk actions (drop lowest, curve, etc.)
 * - Export to CSV/Excel
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class GradebookView extends BorderPane {

    // ========================================================================
    // DATA
    // ========================================================================

    private final ObservableList<StudentRow> students = FXCollections.observableArrayList();
    private final ObservableList<Assignment> assignments = FXCollections.observableArrayList();
    private final Map<String, Map<String, GradeCell>> gradeCells = new HashMap<>();

    @Getter @Setter
    private String classId;

    @Getter @Setter
    private String className;

    // ========================================================================
    // COMPONENTS
    // ========================================================================

    private ComboBox<String> classSelector;
    private ComboBox<String> categoryFilter;
    private ComboBox<String> gradingPeriod;
    private TextField searchField;

    private ScrollPane gridScrollPane;
    private GridPane gradeGrid;
    private VBox statsPanel;

    // Statistics labels
    private Label classAverageLabel;
    private Label highGradeLabel;
    private Label lowGradeLabel;
    private Label missingCountLabel;

    // ========================================================================
    // CALLBACKS
    // ========================================================================

    private Consumer<GradeChange> onGradeChanged;
    private Consumer<Assignment> onAssignmentClick;
    private Consumer<StudentRow> onStudentClick;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public GradebookView() {
        getStyleClass().add("gradebook-view");
        setStyle("-fx-background-color: #F8FAFC;");

        // Build UI
        setTop(createToolbar());
        setCenter(createGradeGrid());
        setRight(createStatsPanel());

        // Load demo data
        loadDemoData();

        log.info("GradebookView initialized");
    }

    // ========================================================================
    // TOOLBAR
    // ========================================================================

    private VBox createToolbar() {
        VBox toolbar = new VBox(12);
        toolbar.setPadding(new Insets(16, 24, 16, 24));
        toolbar.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        // Title row
        HBox titleRow = new HBox(16);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Gradebook");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #0F172A;");

        classSelector = new ComboBox<>();
        classSelector.setPromptText("Select Class");
        classSelector.setPrefWidth(200);
        classSelector.getItems().addAll("Algebra II - Period 1", "Geometry - Period 2", "Pre-Calculus - Period 4");
        classSelector.setValue("Algebra II - Period 1");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addAssignmentBtn = new Button("+ Add Assignment");
        addAssignmentBtn.getStyleClass().addAll("btn", "btn-primary");
        addAssignmentBtn.setOnAction(e -> showAddAssignmentDialog());

        Button exportBtn = new Button("Export");
        exportBtn.getStyleClass().addAll("btn", "btn-ghost");

        Button settingsBtn = new Button("⚙ Settings");
        settingsBtn.getStyleClass().addAll("btn", "btn-ghost");

        titleRow.getChildren().addAll(title, classSelector, spacer, addAssignmentBtn, exportBtn, settingsBtn);

        // Filter row
        HBox filterRow = new HBox(12);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        categoryFilter = new ComboBox<>();
        categoryFilter.setPromptText("All Categories");
        categoryFilter.getItems().addAll("All Categories", "Homework", "Quizzes", "Tests", "Projects");
        categoryFilter.setValue("All Categories");

        gradingPeriod = new ComboBox<>();
        gradingPeriod.setPromptText("Grading Period");
        gradingPeriod.getItems().addAll("Q1", "Q2", "Q3", "Q4", "Semester 1", "Semester 2", "Full Year");
        gradingPeriod.setValue("Q2");

        searchField = new TextField();
        searchField.setPromptText("Search students...");
        searchField.setPrefWidth(200);

        Label lastSaved = new Label("All changes saved");
        lastSaved.setStyle("-fx-font-size: 12px; -fx-text-fill: #10B981;");

        Region filterSpacer = new Region();
        HBox.setHgrow(filterSpacer, Priority.ALWAYS);

        filterRow.getChildren().addAll(
            new Label("Category:"), categoryFilter,
            new Label("Period:"), gradingPeriod,
            searchField,
            filterSpacer,
            lastSaved
        );

        toolbar.getChildren().addAll(titleRow, filterRow);
        return toolbar;
    }

    // ========================================================================
    // GRADE GRID
    // ========================================================================

    private ScrollPane createGradeGrid() {
        gradeGrid = new GridPane();
        gradeGrid.setHgap(0);
        gradeGrid.setVgap(0);
        gradeGrid.setPadding(new Insets(0));
        gradeGrid.setStyle("-fx-background-color: white;");

        gridScrollPane = new ScrollPane(gradeGrid);
        gridScrollPane.setFitToWidth(false);
        gridScrollPane.setFitToHeight(false);
        gridScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        gridScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        gridScrollPane.setStyle("-fx-background-color: white; -fx-border-width: 0;");

        return gridScrollPane;
    }

    private void buildGrid() {
        gradeGrid.getChildren().clear();
        gradeGrid.getColumnConstraints().clear();
        gradeGrid.getRowConstraints().clear();
        gradeCells.clear();

        if (students.isEmpty() || assignments.isEmpty()) {
            gradeGrid.add(new Label("No data to display"), 0, 0);
            return;
        }

        int numCols = assignments.size() + 2; // Student name + assignments + average
        int numRows = students.size() + 2; // Category header + assignment header + students

        // Column constraints
        ColumnConstraints nameCol = new ColumnConstraints(180);
        nameCol.setMinWidth(180);
        gradeGrid.getColumnConstraints().add(nameCol);

        for (int i = 0; i < assignments.size(); i++) {
            ColumnConstraints col = new ColumnConstraints(80);
            col.setMinWidth(70);
            col.setPrefWidth(80);
            gradeGrid.getColumnConstraints().add(col);
        }

        ColumnConstraints avgCol = new ColumnConstraints(80);
        avgCol.setMinWidth(80);
        gradeGrid.getColumnConstraints().add(avgCol);

        // Row constraints
        for (int i = 0; i < numRows; i++) {
            RowConstraints row = new RowConstraints(i < 2 ? 32 : 40);
            row.setMinHeight(i < 2 ? 32 : 40);
            gradeGrid.getRowConstraints().add(row);
        }

        // Build category header row
        buildCategoryHeader();

        // Build assignment header row
        buildAssignmentHeader();

        // Build student rows
        for (int i = 0; i < students.size(); i++) {
            buildStudentRow(students.get(i), i + 2);
        }
    }

    private void buildCategoryHeader() {
        // Empty cell for student column
        Label emptyLabel = createHeaderCell("");
        gradeGrid.add(emptyLabel, 0, 0);

        // Group assignments by category
        Map<String, List<Integer>> categorySpans = new LinkedHashMap<>();
        for (int i = 0; i < assignments.size(); i++) {
            String cat = assignments.get(i).getCategory();
            categorySpans.computeIfAbsent(cat, k -> new ArrayList<>()).add(i);
        }

        int col = 1;
        for (Map.Entry<String, List<Integer>> entry : categorySpans.entrySet()) {
            String category = entry.getKey();
            int span = entry.getValue().size();

            Label catLabel = createHeaderCell(category);
            catLabel.setStyle(catLabel.getStyle() + "-fx-background-color: " + getCategoryColor(category) + ";");

            gradeGrid.add(catLabel, col, 0, span, 1);
            col += span;
        }

        // Average column header
        Label avgHeader = createHeaderCell("Average");
        avgHeader.setStyle(avgHeader.getStyle() + "-fx-background-color: #1E40AF; -fx-text-fill: white;");
        gradeGrid.add(avgHeader, col, 0);
    }

    private void buildAssignmentHeader() {
        // Student column header
        Label studentHeader = createHeaderCell("Student");
        studentHeader.setAlignment(Pos.CENTER_LEFT);
        studentHeader.setPadding(new Insets(0, 0, 0, 12));
        gradeGrid.add(studentHeader, 0, 1);

        // Assignment headers
        for (int i = 0; i < assignments.size(); i++) {
            Assignment a = assignments.get(i);
            VBox headerCell = createAssignmentHeader(a);
            gradeGrid.add(headerCell, i + 1, 1);
        }

        // Average column
        Label avgLabel = createHeaderCell("Avg");
        gradeGrid.add(avgLabel, assignments.size() + 1, 1);
    }

    private VBox createAssignmentHeader(Assignment assignment) {
        VBox cell = new VBox(2);
        cell.setAlignment(Pos.CENTER);
        cell.setPadding(new Insets(4));
        cell.setStyle("-fx-background-color: #F1F5F9; -fx-border-color: #E2E8F0; -fx-border-width: 0 1 1 0;");
        cell.setCursor(javafx.scene.Cursor.HAND);

        Label nameLabel = new Label(assignment.getShortName());
        nameLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 600; -fx-text-fill: #334155;");
        nameLabel.setTooltip(new Tooltip(assignment.getName() + "\nDue: " + assignment.getDueDate() + "\nPoints: " + assignment.getMaxPoints()));

        Label pointsLabel = new Label(String.valueOf((int) assignment.getMaxPoints()) + " pts");
        pointsLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #94A3B8;");

        cell.getChildren().addAll(nameLabel, pointsLabel);

        cell.setOnMouseClicked(e -> {
            if (onAssignmentClick != null) {
                onAssignmentClick.accept(assignment);
            }
        });

        return cell;
    }

    private void buildStudentRow(StudentRow student, int rowIndex) {
        // Student name cell
        HBox nameCell = createStudentNameCell(student);
        gradeGrid.add(nameCell, 0, rowIndex);

        // Grade cells
        gradeCells.put(student.getId(), new HashMap<>());

        for (int i = 0; i < assignments.size(); i++) {
            Assignment assignment = assignments.get(i);
            GradeCell cell = createGradeCell(student, assignment, rowIndex, i + 1);
            gradeGrid.add(cell, i + 1, rowIndex);
            gradeCells.get(student.getId()).put(assignment.getId(), cell);
        }

        // Average cell
        Label avgCell = createAverageCell(student);
        gradeGrid.add(avgCell, assignments.size() + 1, rowIndex);
    }

    private HBox createStudentNameCell(StudentRow student) {
        HBox cell = new HBox(8);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(0, 12, 0, 12));
        cell.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 1 1 0;");
        cell.setCursor(javafx.scene.Cursor.HAND);

        Label nameLabel = new Label(student.getName());
        nameLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #0F172A;");

        cell.getChildren().add(nameLabel);

        cell.setOnMouseClicked(e -> {
            if (onStudentClick != null) {
                onStudentClick.accept(student);
            }
        });

        // Hover effect
        cell.setOnMouseEntered(e -> cell.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-width: 0 1 1 0;"));
        cell.setOnMouseExited(e -> cell.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 1 1 0;"));

        return cell;
    }

    private GradeCell createGradeCell(StudentRow student, Assignment assignment, int row, int col) {
        GradeCell cell = new GradeCell();
        cell.setStudentId(student.getId());
        cell.setAssignmentId(assignment.getId());
        cell.setMaxPoints(assignment.getMaxPoints());

        // Get existing grade if any
        String grade = student.getGrade(assignment.getId());
        if (grade != null) {
            cell.setGradeValue(grade);
        }

        // Setup navigation
        cell.setOnGradeChanged((c, newValue) -> {
            student.setGrade(assignment.getId(), newValue);
            updateStudentAverage(student);
            updateClassStatistics();

            if (onGradeChanged != null) {
                onGradeChanged.accept(new GradeChange(student.getId(), assignment.getId(), newValue));
            }
        });

        // Keyboard navigation
        final int r = row;
        final int c = col;

        cell.setOnNavigateNext(() -> navigateToCell(r, c + 1));
        cell.setOnNavigatePrevious(() -> navigateToCell(r, c - 1));
        cell.setOnNavigateUp(() -> navigateToCell(r - 1, c));
        cell.setOnNavigateDown(() -> navigateToCell(r + 1, c));

        return cell;
    }

    private Label createAverageCell(StudentRow student) {
        double avg = calculateStudentAverage(student);

        Label cell = new Label(String.format("%.1f%%", avg));
        cell.setAlignment(Pos.CENTER);
        cell.setMaxWidth(Double.MAX_VALUE);
        cell.setMaxHeight(Double.MAX_VALUE);
        cell.setPadding(new Insets(0, 8, 0, 8));

        String bgColor = avg >= 90 ? "#ECFDF5" : avg >= 80 ? "#EFF6FF" : avg >= 70 ? "#FFFBEB" : "#FEF2F2";
        String textColor = avg >= 90 ? "#059669" : avg >= 80 ? "#2563EB" : avg >= 70 ? "#D97706" : "#DC2626";

        cell.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: %s;
            -fx-font-size: 13px;
            -fx-font-weight: 600;
            -fx-border-color: #E2E8F0;
            -fx-border-width: 0 0 1 0;
            """, bgColor, textColor));

        cell.setId("avg-" + student.getId());

        return cell;
    }

    private Label createHeaderCell(String text) {
        Label cell = new Label(text);
        cell.setAlignment(Pos.CENTER);
        cell.setMaxWidth(Double.MAX_VALUE);
        cell.setMaxHeight(Double.MAX_VALUE);
        cell.setStyle("""
            -fx-background-color: #F1F5F9;
            -fx-text-fill: #475569;
            -fx-font-size: 11px;
            -fx-font-weight: 600;
            -fx-border-color: #E2E8F0;
            -fx-border-width: 0 1 1 0;
            """);
        return cell;
    }

    private void navigateToCell(int row, int col) {
        // Bounds check
        if (row < 2 || row >= students.size() + 2) return;
        if (col < 1 || col > assignments.size()) return;

        StudentRow student = students.get(row - 2);
        Assignment assignment = assignments.get(col - 1);

        GradeCell cell = gradeCells.get(student.getId()).get(assignment.getId());
        if (cell != null) {
            cell.startEditing();
        }
    }

    // ========================================================================
    // STATISTICS PANEL
    // ========================================================================

    private VBox createStatsPanel() {
        statsPanel = new VBox(16);
        statsPanel.setPrefWidth(220);
        statsPanel.setPadding(new Insets(16));
        statsPanel.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 0 1;");

        Label statsTitle = new Label("Class Statistics");
        statsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");

        // Stats cards
        VBox avgCard = createStatCard("Class Average", "—", "#2563EB");
        classAverageLabel = (Label) avgCard.lookup(".stat-value");

        VBox highCard = createStatCard("Highest Grade", "—", "#10B981");
        highGradeLabel = (Label) highCard.lookup(".stat-value");

        VBox lowCard = createStatCard("Lowest Grade", "—", "#F59E0B");
        lowGradeLabel = (Label) lowCard.lookup(".stat-value");

        VBox missingCard = createStatCard("Missing Grades", "—", "#EF4444");
        missingCountLabel = (Label) missingCard.lookup(".stat-value");

        // Grade distribution
        Label distTitle = new Label("Grade Distribution");
        distTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #334155; -fx-padding: 16 0 8 0;");

        VBox distribution = createGradeDistribution();

        statsPanel.getChildren().addAll(statsTitle, avgCard, highCard, lowCard, missingCard, distTitle, distribution);

        return statsPanel;
    }

    private VBox createStatCard(String label, String value, String color) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8;");

        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("stat-value");
        valueNode.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: " + color + ";");

        card.getChildren().addAll(labelNode, valueNode);
        return card;
    }

    private VBox createGradeDistribution() {
        VBox dist = new VBox(6);

        String[] grades = {"A", "B", "C", "D", "F"};
        String[] colors = {"#10B981", "#3B82F6", "#F59E0B", "#F97316", "#EF4444"};

        for (int i = 0; i < grades.length; i++) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);

            Label gradeLabel = new Label(grades[i]);
            gradeLabel.setMinWidth(20);
            gradeLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600;");

            ProgressBar bar = new ProgressBar(0);
            bar.setPrefWidth(100);
            bar.setPrefHeight(12);
            bar.setStyle("-fx-accent: " + colors[i] + ";");
            HBox.setHgrow(bar, Priority.ALWAYS);

            Label countLabel = new Label("0");
            countLabel.setMinWidth(25);
            countLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

            row.getChildren().addAll(gradeLabel, bar, countLabel);
            dist.getChildren().add(row);
        }

        return dist;
    }

    // ========================================================================
    // CALCULATIONS
    // ========================================================================

    private double calculateStudentAverage(StudentRow student) {
        double totalPoints = 0;
        double earnedPoints = 0;

        for (Assignment a : assignments) {
            String grade = student.getGrade(a.getId());
            if (grade != null && !grade.isEmpty()) {
                try {
                    double value = Double.parseDouble(grade.replace("%", ""));
                    earnedPoints += (value / 100) * a.getMaxPoints();
                    totalPoints += a.getMaxPoints();
                } catch (NumberFormatException e) { log.debug("Invalid numeric input for grade percentage, skipping", e); }
            }
        }

        return totalPoints > 0 ? (earnedPoints / totalPoints) * 100 : 0;
    }

    private void updateStudentAverage(StudentRow student) {
        double avg = calculateStudentAverage(student);
        Label avgLabel = (Label) gradeGrid.lookup("#avg-" + student.getId());
        if (avgLabel != null) {
            avgLabel.setText(String.format("%.1f%%", avg));

            String bgColor = avg >= 90 ? "#ECFDF5" : avg >= 80 ? "#EFF6FF" : avg >= 70 ? "#FFFBEB" : "#FEF2F2";
            String textColor = avg >= 90 ? "#059669" : avg >= 80 ? "#2563EB" : avg >= 70 ? "#D97706" : "#DC2626";

            avgLabel.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-font-size: 13px;
                -fx-font-weight: 600;
                -fx-border-color: #E2E8F0;
                -fx-border-width: 0 0 1 0;
                """, bgColor, textColor));
        }
    }

    private void updateClassStatistics() {
        if (students.isEmpty()) return;

        double total = 0;
        double highest = 0;
        double lowest = 100;
        int count = 0;
        int missing = 0;

        for (StudentRow student : students) {
            double avg = calculateStudentAverage(student);
            if (avg > 0) {
                total += avg;
                highest = Math.max(highest, avg);
                lowest = Math.min(lowest, avg);
                count++;
            }

            // Count missing grades
            for (Assignment a : assignments) {
                String grade = student.getGrade(a.getId());
                if (grade == null || grade.isEmpty()) {
                    missing++;
                }
            }
        }

        double classAvg = count > 0 ? total / count : 0;

        if (classAverageLabel != null) classAverageLabel.setText(String.format("%.1f%%", classAvg));
        if (highGradeLabel != null) highGradeLabel.setText(String.format("%.1f%%", highest));
        if (lowGradeLabel != null) lowGradeLabel.setText(String.format("%.1f%%", lowest));
        if (missingCountLabel != null) missingCountLabel.setText(String.valueOf(missing));
    }

    private String getCategoryColor(String category) {
        return switch (category.toLowerCase()) {
            case "homework" -> "#DBEAFE";
            case "quizzes" -> "#E0E7FF";
            case "tests" -> "#FCE7F3";
            case "projects" -> "#D1FAE5";
            default -> "#F1F5F9";
        };
    }

    // ========================================================================
    // DIALOGS
    // ========================================================================

    private void showAddAssignmentDialog() {
        Dialog<Assignment> dialog = new Dialog<>();
        dialog.setTitle("Add Assignment");
        dialog.setHeaderText("Create a new assignment");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Assignment name");

        ComboBox<String> categoryField = new ComboBox<>();
        categoryField.getItems().addAll("Homework", "Quizzes", "Tests", "Projects");
        categoryField.setValue("Homework");

        TextField pointsField = new TextField("100");
        pointsField.setPromptText("Max points");

        DatePicker dueDateField = new DatePicker(LocalDate.now().plusDays(7));

        form.add(new Label("Name:"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(new Label("Category:"), 0, 1);
        form.add(categoryField, 1, 1);
        form.add(new Label("Points:"), 0, 2);
        form.add(pointsField, 1, 2);
        form.add(new Label("Due Date:"), 0, 3);
        form.add(dueDateField, 1, 3);

        pane.setContent(form);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                Assignment a = new Assignment(
                    UUID.randomUUID().toString(),
                    nameField.getText(),
                    categoryField.getValue(),
                    Double.parseDouble(pointsField.getText()),
                    dueDateField.getValue()
                );
                return a;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(assignment -> {
            assignments.add(assignment);
            buildGrid();
        });
    }

    // ========================================================================
    // DATA LOADING
    // ========================================================================

    private void loadDemoData() {
        // Demo assignments
        assignments.add(new Assignment("a1", "Homework 1", "Homework", 10, LocalDate.now().minusDays(14)));
        assignments.add(new Assignment("a2", "Homework 2", "Homework", 10, LocalDate.now().minusDays(7)));
        assignments.add(new Assignment("a3", "Quiz 1", "Quizzes", 25, LocalDate.now().minusDays(10)));
        assignments.add(new Assignment("a4", "Test 1", "Tests", 100, LocalDate.now().minusDays(5)));
        assignments.add(new Assignment("a5", "Homework 3", "Homework", 10, LocalDate.now()));
        assignments.add(new Assignment("a6", "Project 1", "Projects", 50, LocalDate.now().plusDays(7)));

        // Demo students
        String[] names = {"Anderson, Emma", "Brown, Liam", "Chen, Olivia", "Davis, Noah", "Evans, Ava",
                         "Foster, Ethan", "Garcia, Isabella", "Harris, Mason", "Ibrahim, Sophia", "Johnson, James"};

        Random rand = new Random(42);
        for (int i = 0; i < names.length; i++) {
            StudentRow student = new StudentRow("s" + (i + 1), names[i]);

            // Random grades
            for (Assignment a : assignments) {
                if (rand.nextDouble() > 0.15) { // 85% have grades
                    int grade = 60 + rand.nextInt(41); // 60-100
                    student.setGrade(a.getId(), String.valueOf(grade));
                }
            }

            students.add(student);
        }

        buildGrid();
        updateClassStatistics();
    }

    // ========================================================================
    // PUBLIC API
    // ========================================================================

    public void setOnGradeChanged(Consumer<GradeChange> callback) {
        this.onGradeChanged = callback;
    }

    public void setOnAssignmentClick(Consumer<Assignment> callback) {
        this.onAssignmentClick = callback;
    }

    public void setOnStudentClick(Consumer<StudentRow> callback) {
        this.onStudentClick = callback;
    }

    public void refresh() {
        buildGrid();
        updateClassStatistics();
    }

    // ========================================================================
    // INNER CLASSES
    // ========================================================================

    @Getter
    public static class StudentRow {
        private final String id;
        private final String name;
        private final Map<String, String> grades = new HashMap<>();

        public StudentRow(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getGrade(String assignmentId) {
            return grades.get(assignmentId);
        }

        public void setGrade(String assignmentId, String grade) {
            grades.put(assignmentId, grade);
        }
    }

    @Getter
    public static class Assignment {
        private final String id;
        private final String name;
        private final String category;
        private final double maxPoints;
        private final LocalDate dueDate;

        public Assignment(String id, String name, String category, double maxPoints, LocalDate dueDate) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.maxPoints = maxPoints;
            this.dueDate = dueDate;
        }

        public String getShortName() {
            if (name.length() <= 8) return name;
            return name.substring(0, 6) + "..";
        }

        public String getDueDate() {
            return dueDate.format(DateTimeFormatter.ofPattern("MMM d"));
        }
    }

    @Getter
    public static class GradeChange {
        private final String studentId;
        private final String assignmentId;
        private final String newValue;

        public GradeChange(String studentId, String assignmentId, String newValue) {
            this.studentId = studentId;
            this.assignmentId = assignmentId;
            this.newValue = newValue;
        }
    }
}
