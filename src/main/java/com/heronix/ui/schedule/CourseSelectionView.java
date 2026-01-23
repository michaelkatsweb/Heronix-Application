package com.heronix.ui.schedule;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Course Selection View
 * Interface for students and counselors to select courses for scheduling.
 *
 * Features:
 * - Course catalog with filtering
 * - Prerequisites validation
 * - Credit requirement tracking
 * - Alternate course selection
 * - Request status tracking
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class CourseSelectionView extends BorderPane {

    // ========================================================================
    // DATA
    // ========================================================================

    private final ObservableList<Course> availableCourses = FXCollections.observableArrayList();
    private final ObservableList<CourseRequest> selectedCourses = FXCollections.observableArrayList();
    private final ObservableList<CourseRequest> alternateCourses = FXCollections.observableArrayList();

    @Getter @Setter
    private String studentId;

    @Getter @Setter
    private String studentName;

    @Getter @Setter
    private int gradeLevel = 10;

    // ========================================================================
    // COMPONENTS
    // ========================================================================

    private TextField searchField;
    private ComboBox<String> departmentFilter;
    private ComboBox<String> creditTypeFilter;
    private ListView<Course> courseListView;
    private VBox selectedCoursesPane;
    private VBox alternateCoursesPane;

    // Credit tracking
    private Label totalCreditsLabel;
    private ProgressBar creditProgress;
    private VBox creditBreakdown;

    // ========================================================================
    // CALLBACKS
    // ========================================================================

    private Consumer<List<CourseRequest>> onSubmit;
    private Consumer<Course> onCourseDetails;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public CourseSelectionView() {
        setStyle("-fx-background-color: #F8FAFC;");

        setTop(createHeader());
        setLeft(createCourseCatalog());
        setCenter(createSelectionPane());
        setRight(createCreditsPanel());

        loadDemoCourses();

        log.info("CourseSelectionView initialized");
    }

    // ========================================================================
    // HEADER
    // ========================================================================

    private VBox createHeader() {
        VBox header = new VBox(8);
        header.setPadding(new Insets(16, 24, 16, 24));
        header.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        HBox titleRow = new HBox(16);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Course Selection");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #0F172A;");

        Label yearLabel = new Label("2026-2027 School Year");
        yearLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748B;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button submitBtn = new Button("Submit Requests");
        submitBtn.getStyleClass().addAll("btn", "btn-primary");
        submitBtn.setOnAction(e -> submitRequests());

        Button printBtn = new Button("Print");
        printBtn.getStyleClass().addAll("btn", "btn-ghost");

        titleRow.getChildren().addAll(title, yearLabel, spacer, printBtn, submitBtn);

        // Info banner
        HBox infoBanner = new HBox(12);
        infoBanner.setPadding(new Insets(12));
        infoBanner.setStyle("-fx-background-color: #EFF6FF; -fx-background-radius: 8;");
        infoBanner.setAlignment(Pos.CENTER_LEFT);

        Label infoIcon = new Label("ℹ");
        infoIcon.setStyle("-fx-font-size: 16px; -fx-text-fill: #2563EB;");

        Label infoText = new Label("Select 6-8 courses for next year. Choose at least 2 alternates in case your first choices aren't available.");
        infoText.setStyle("-fx-font-size: 13px; -fx-text-fill: #1E40AF;");
        infoText.setWrapText(true);

        infoBanner.getChildren().addAll(infoIcon, infoText);

        header.getChildren().addAll(titleRow, infoBanner);
        return header;
    }

    // ========================================================================
    // COURSE CATALOG
    // ========================================================================

    private VBox createCourseCatalog() {
        VBox catalog = new VBox(12);
        catalog.setPrefWidth(350);
        catalog.setPadding(new Insets(16));
        catalog.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 1 0 0;");

        Label catalogTitle = new Label("Course Catalog");
        catalogTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");

        // Search
        searchField = new TextField();
        searchField.setPromptText("Search courses...");
        searchField.getStyleClass().add("search-field");

        // Filters
        HBox filters = new HBox(8);

        departmentFilter = new ComboBox<>();
        departmentFilter.setPromptText("Department");
        departmentFilter.getItems().addAll("All Departments", "English", "Math", "Science", "Social Studies",
                                          "World Languages", "Arts", "Physical Ed", "Electives");
        departmentFilter.setValue("All Departments");
        departmentFilter.setPrefWidth(140);

        creditTypeFilter = new ComboBox<>();
        creditTypeFilter.setPromptText("Credit Type");
        creditTypeFilter.getItems().addAll("All Types", "Required", "Elective", "AP/Honors");
        creditTypeFilter.setValue("All Types");
        creditTypeFilter.setPrefWidth(120);

        filters.getChildren().addAll(departmentFilter, creditTypeFilter);

        // Course list
        courseListView = new ListView<>();
        courseListView.setCellFactory(lv -> new CourseListCell());
        VBox.setVgrow(courseListView, Priority.ALWAYS);

        // Filter logic
        FilteredList<Course> filteredCourses = new FilteredList<>(availableCourses, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredCourses.setPredicate(createFilter());
        });

        departmentFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            filteredCourses.setPredicate(createFilter());
        });

        creditTypeFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            filteredCourses.setPredicate(createFilter());
        });

        courseListView.setItems(filteredCourses);

        catalog.getChildren().addAll(catalogTitle, searchField, filters, courseListView);
        return catalog;
    }

    private Predicate<Course> createFilter() {
        return course -> {
            String searchText = searchField.getText().toLowerCase();
            String dept = departmentFilter.getValue();
            String creditType = creditTypeFilter.getValue();

            boolean matchesSearch = searchText.isEmpty() ||
                course.getName().toLowerCase().contains(searchText) ||
                course.getCode().toLowerCase().contains(searchText);

            boolean matchesDept = dept == null || "All Departments".equals(dept) ||
                course.getDepartment().equals(dept);

            boolean matchesType = creditType == null || "All Types".equals(creditType) ||
                (creditType.equals("Required") && course.isRequired()) ||
                (creditType.equals("Elective") && !course.isRequired()) ||
                (creditType.equals("AP/Honors") && (course.isAp() || course.isHonors()));

            return matchesSearch && matchesDept && matchesType;
        };
    }

    // ========================================================================
    // SELECTION PANE
    // ========================================================================

    private VBox createSelectionPane() {
        VBox pane = new VBox(16);
        pane.setPadding(new Insets(16));

        // Primary selections
        Label primaryTitle = new Label("Primary Course Selections");
        primaryTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");

        Label primarySubtitle = new Label("Select 6-8 courses for your schedule");
        primarySubtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        selectedCoursesPane = new VBox(8);
        selectedCoursesPane.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8; -fx-padding: 12;");
        selectedCoursesPane.setMinHeight(200);

        Label dropHint1 = new Label("Click courses from the catalog to add them here");
        dropHint1.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13px;");
        selectedCoursesPane.getChildren().add(dropHint1);

        // Alternate selections
        Label alternateTitle = new Label("Alternate Courses");
        alternateTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #0F172A; -fx-padding: 16 0 0 0;");

        Label alternateSubtitle = new Label("Select 2-3 alternates in case your first choices aren't available");
        alternateSubtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        alternateCoursesPane = new VBox(8);
        alternateCoursesPane.setStyle("-fx-background-color: #FFFBEB; -fx-background-radius: 8; -fx-padding: 12;");
        alternateCoursesPane.setMinHeight(100);

        Label dropHint2 = new Label("Add alternate courses here");
        dropHint2.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13px;");
        alternateCoursesPane.getChildren().add(dropHint2);

        pane.getChildren().addAll(primaryTitle, primarySubtitle, selectedCoursesPane,
                                 alternateTitle, alternateSubtitle, alternateCoursesPane);

        return pane;
    }

    private HBox createSelectedCourseCard(CourseRequest request, boolean isAlternate) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(10, 12, 10, 12));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 6; -fx-border-color: #E2E8F0; -fx-border-radius: 6;");

        // Priority number
        Label priorityLabel = new Label(String.valueOf(request.getPriority()));
        priorityLabel.setStyle("""
            -fx-background-color: #2563EB;
            -fx-text-fill: white;
            -fx-font-size: 11px;
            -fx-font-weight: 600;
            -fx-padding: 2 8;
            -fx-background-radius: 10;
            """);

        // Course info
        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label nameLabel = new Label(request.getCourse().getName());
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: #0F172A;");

        Label detailsLabel = new Label(String.format("%s • %.1f credits",
            request.getCourse().getCode(), request.getCourse().getCredits()));
        detailsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

        info.getChildren().addAll(nameLabel, detailsLabel);

        // Remove button
        Button removeBtn = new Button("×");
        removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94A3B8; -fx-font-size: 16px; -fx-cursor: hand;");
        removeBtn.setOnAction(e -> removeCourse(request, isAlternate));

        card.getChildren().addAll(priorityLabel, info, removeBtn);

        return card;
    }

    private void refreshSelectedCourses() {
        selectedCoursesPane.getChildren().clear();
        alternateCoursesPane.getChildren().clear();

        if (selectedCourses.isEmpty()) {
            Label hint = new Label("Click courses from the catalog to add them here");
            hint.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13px;");
            selectedCoursesPane.getChildren().add(hint);
        } else {
            for (CourseRequest request : selectedCourses) {
                selectedCoursesPane.getChildren().add(createSelectedCourseCard(request, false));
            }
        }

        if (alternateCourses.isEmpty()) {
            Label hint = new Label("Add alternate courses here");
            hint.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13px;");
            alternateCoursesPane.getChildren().add(hint);
        } else {
            for (CourseRequest request : alternateCourses) {
                alternateCoursesPane.getChildren().add(createSelectedCourseCard(request, true));
            }
        }

        updateCredits();
    }

    // ========================================================================
    // CREDITS PANEL
    // ========================================================================

    private VBox createCreditsPanel() {
        VBox panel = new VBox(16);
        panel.setPrefWidth(220);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 0 1;");

        Label creditsTitle = new Label("Credit Summary");
        creditsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");

        // Total credits
        VBox totalCard = new VBox(4);
        totalCard.setPadding(new Insets(12));
        totalCard.setStyle("-fx-background-color: #EFF6FF; -fx-background-radius: 8;");

        Label totalLabel = new Label("Total Credits");
        totalLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #1E40AF;");

        totalCreditsLabel = new Label("0.0 / 7.0");
        totalCreditsLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #2563EB;");

        creditProgress = new ProgressBar(0);
        creditProgress.setPrefWidth(Double.MAX_VALUE);
        creditProgress.setStyle("-fx-accent: #2563EB;");

        totalCard.getChildren().addAll(totalLabel, totalCreditsLabel, creditProgress);

        // Credit breakdown by type
        Label breakdownTitle = new Label("By Requirement");
        breakdownTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #334155; -fx-padding: 12 0 0 0;");

        creditBreakdown = new VBox(8);

        // Requirements list
        String[][] requirements = {
            {"English", "1.0", "#EF4444"},
            {"Math", "1.0", "#F59E0B"},
            {"Science", "1.0", "#10B981"},
            {"Social Studies", "1.0", "#3B82F6"},
            {"World Language", "1.0", "#8B5CF6"},
            {"Electives", "2.0", "#64748B"}
        };

        for (String[] req : requirements) {
            HBox row = createRequirementRow(req[0], 0, Double.parseDouble(req[1]), req[2]);
            creditBreakdown.getChildren().add(row);
        }

        panel.getChildren().addAll(creditsTitle, totalCard, breakdownTitle, creditBreakdown);

        return panel;
    }

    private HBox createRequirementRow(String name, double current, double required, String color) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Label colorDot = new Label("●");
        colorDot.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 10px;");

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #334155;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        Label valueLabel = new Label(String.format("%.1f/%.1f", current, required));
        valueLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (current >= required ? "#10B981" : "#64748B") + ";");

        row.getChildren().addAll(colorDot, nameLabel, valueLabel);
        row.setId("req-" + name.toLowerCase().replace(" ", "-"));

        return row;
    }

    private void updateCredits() {
        double total = selectedCourses.stream()
            .mapToDouble(r -> r.getCourse().getCredits())
            .sum();

        totalCreditsLabel.setText(String.format("%.1f / 7.0", total));
        creditProgress.setProgress(Math.min(total / 7.0, 1.0));

        // Update breakdown (simplified - would need real category tracking)
    }

    // ========================================================================
    // ACTIONS
    // ========================================================================

    private void addCourse(Course course, boolean isAlternate) {
        // Check if already selected
        boolean alreadySelected = selectedCourses.stream().anyMatch(r -> r.getCourse().getId().equals(course.getId())) ||
                                 alternateCourses.stream().anyMatch(r -> r.getCourse().getId().equals(course.getId()));

        if (alreadySelected) {
            showAlert("Course Already Selected", "This course is already in your selections.");
            return;
        }

        // Check prerequisites
        if (!course.getPrerequisites().isEmpty()) {
            // In real app, would check against completed courses
        }

        CourseRequest request = new CourseRequest(course);

        if (isAlternate) {
            request.setPriority(alternateCourses.size() + 1);
            request.setAlternate(true);
            alternateCourses.add(request);
        } else {
            if (selectedCourses.size() >= 8) {
                showAlert("Maximum Courses", "You can select up to 8 primary courses.");
                return;
            }
            request.setPriority(selectedCourses.size() + 1);
            selectedCourses.add(request);
        }

        refreshSelectedCourses();
    }

    private void removeCourse(CourseRequest request, boolean isAlternate) {
        if (isAlternate) {
            alternateCourses.remove(request);
            // Renumber priorities
            for (int i = 0; i < alternateCourses.size(); i++) {
                alternateCourses.get(i).setPriority(i + 1);
            }
        } else {
            selectedCourses.remove(request);
            for (int i = 0; i < selectedCourses.size(); i++) {
                selectedCourses.get(i).setPriority(i + 1);
            }
        }
        refreshSelectedCourses();
    }

    private void submitRequests() {
        if (selectedCourses.size() < 6) {
            showAlert("Not Enough Courses", "Please select at least 6 primary courses.");
            return;
        }

        if (alternateCourses.size() < 2) {
            showAlert("Need Alternates", "Please select at least 2 alternate courses.");
            return;
        }

        List<CourseRequest> allRequests = new ArrayList<>();
        allRequests.addAll(selectedCourses);
        allRequests.addAll(alternateCourses);

        if (onSubmit != null) {
            onSubmit.accept(allRequests);
        }

        showAlert("Requests Submitted", "Your course requests have been submitted successfully.");
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

    private void loadDemoCourses() {
        availableCourses.addAll(
            new Course("ENG301", "English 11", "English", 1.0, false, false, true),
            new Course("ENG302", "AP English Language", "English", 1.0, true, false, false),
            new Course("MAT301", "Algebra II", "Math", 1.0, false, false, true),
            new Course("MAT302", "Pre-Calculus", "Math", 1.0, false, true, false),
            new Course("MAT303", "AP Calculus AB", "Math", 1.0, true, false, false),
            new Course("SCI301", "Chemistry", "Science", 1.0, false, false, true),
            new Course("SCI302", "AP Chemistry", "Science", 1.0, true, false, false),
            new Course("SCI303", "Physics", "Science", 1.0, false, false, false),
            new Course("SOC301", "US History", "Social Studies", 1.0, false, false, true),
            new Course("SOC302", "AP US History", "Social Studies", 1.0, true, false, false),
            new Course("WL301", "Spanish III", "World Languages", 1.0, false, false, false),
            new Course("WL302", "French III", "World Languages", 1.0, false, false, false),
            new Course("ART301", "Studio Art", "Arts", 0.5, false, false, false),
            new Course("ART302", "Digital Media", "Arts", 0.5, false, false, false),
            new Course("PE301", "Physical Education", "Physical Ed", 0.5, false, false, true),
            new Course("ELE301", "Computer Science", "Electives", 0.5, false, false, false),
            new Course("ELE302", "Psychology", "Electives", 0.5, false, false, false)
        );
    }

    // ========================================================================
    // PUBLIC API
    // ========================================================================

    public void setOnSubmit(Consumer<List<CourseRequest>> callback) {
        this.onSubmit = callback;
    }

    public void setOnCourseDetails(Consumer<Course> callback) {
        this.onCourseDetails = callback;
    }

    // ========================================================================
    // INNER CLASSES
    // ========================================================================

    @Getter @Setter
    public static class Course {
        private String id;
        private String code;
        private String name;
        private String department;
        private double credits;
        private boolean ap;
        private boolean honors;
        private boolean required;
        private String description;
        private List<String> prerequisites = new ArrayList<>();

        public Course(String code, String name, String department, double credits,
                     boolean ap, boolean honors, boolean required) {
            this.id = code;
            this.code = code;
            this.name = name;
            this.department = department;
            this.credits = credits;
            this.ap = ap;
            this.honors = honors;
            this.required = required;
        }
    }

    @Getter @Setter
    public static class CourseRequest {
        private Course course;
        private int priority;
        private boolean alternate;
        private String status = "Pending";
        private String notes;

        public CourseRequest(Course course) {
            this.course = course;
        }
    }

    // ========================================================================
    // COURSE LIST CELL
    // ========================================================================

    private class CourseListCell extends ListCell<Course> {
        @Override
        protected void updateItem(Course course, boolean empty) {
            super.updateItem(course, empty);

            if (empty || course == null) {
                setGraphic(null);
            } else {
                VBox cell = new VBox(4);
                cell.setPadding(new Insets(8, 12, 8, 12));
                cell.setCursor(javafx.scene.Cursor.HAND);

                HBox header = new HBox(8);
                header.setAlignment(Pos.CENTER_LEFT);

                Label nameLabel = new Label(course.getName());
                nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: #0F172A;");

                if (course.isAp()) {
                    Label apBadge = new Label("AP");
                    apBadge.setStyle("-fx-background-color: #7C3AED; -fx-text-fill: white; -fx-font-size: 9px; -fx-padding: 1 4; -fx-background-radius: 3;");
                    header.getChildren().add(apBadge);
                } else if (course.isHonors()) {
                    Label honorsBadge = new Label("H");
                    honorsBadge.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-size: 9px; -fx-padding: 1 4; -fx-background-radius: 3;");
                    header.getChildren().add(honorsBadge);
                }

                header.getChildren().add(0, nameLabel);

                Label detailsLabel = new Label(String.format("%s • %.1f credits • %s",
                    course.getCode(), course.getCredits(), course.getDepartment()));
                detailsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

                HBox actions = new HBox(8);
                actions.setAlignment(Pos.CENTER_LEFT);

                Hyperlink addPrimary = new Hyperlink("+ Add");
                addPrimary.setStyle("-fx-text-fill: #2563EB; -fx-font-size: 11px;");
                addPrimary.setOnAction(e -> addCourse(course, false));

                Hyperlink addAlternate = new Hyperlink("+ Alternate");
                addAlternate.setStyle("-fx-text-fill: #F59E0B; -fx-font-size: 11px;");
                addAlternate.setOnAction(e -> addCourse(course, true));

                actions.getChildren().addAll(addPrimary, addAlternate);

                cell.getChildren().addAll(header, detailsLabel, actions);

                // Hover effect
                cell.setOnMouseEntered(e -> cell.setStyle("-fx-background-color: #F8FAFC;"));
                cell.setOnMouseExited(e -> cell.setStyle("-fx-background-color: transparent;"));

                setGraphic(cell);
            }
        }
    }
}
