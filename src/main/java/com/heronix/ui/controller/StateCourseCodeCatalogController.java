package com.heronix.ui.controller;

import com.heronix.model.domain.SchoolConfiguration;
import com.heronix.model.domain.StateCourseCode;
import com.heronix.model.enums.CourseCategory;
import com.heronix.model.enums.CourseType;
import com.heronix.model.enums.EducationLevel;
import com.heronix.model.enums.GradeLevel;
import com.heronix.model.enums.SCEDSubjectArea;
import com.heronix.model.enums.SchoolType;
import com.heronix.model.enums.USState;
import com.heronix.repository.SchoolConfigurationRepository;
import com.heronix.repository.StateCourseCodeRepository;
import com.heronix.service.StateCourseCodeService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for State Course Code Catalog Management
 *
 * Features:
 * - Browse and search state course catalogs
 * - Filter by state, grade level, subject area
 * - Import courses from CSV files
 * - View course details
 * - Export catalog data
 * - Copy courses to local course library
 *
 * This controller allows administrators to:
 * 1. Select a state before setting up the SIS
 * 2. Browse the state's official course catalog
 * 3. Import course codes via CSV upload
 * 4. Filter courses by grade level (PreK-12)
 * 5. View SCED mapping information
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01 - State Course Catalog Feature
 */
@Slf4j
@Component
public class StateCourseCodeCatalogController {

    // ========================================================================
    // FXML COMPONENTS
    // ========================================================================

    @FXML private ComboBox<USState> stateSelector;
    @FXML private ComboBox<String> gradeLevelFilter;
    @FXML private ComboBox<SCEDSubjectArea> subjectAreaFilter;
    @FXML private ComboBox<CourseType> courseTypeFilter;
    @FXML private TextField searchField;
    @FXML private TableView<StateCourseCode> courseTable;
    @FXML private Label recordCountLabel;
    @FXML private Label stateInfoLabel;
    @FXML private ProgressIndicator loadingIndicator;

    // Table columns
    @FXML private TableColumn<StateCourseCode, String> stateCodeColumn;
    @FXML private TableColumn<StateCourseCode, String> courseNameColumn;
    @FXML private TableColumn<StateCourseCode, String> scedCodeColumn;
    @FXML private TableColumn<StateCourseCode, String> subjectAreaColumn;
    @FXML private TableColumn<StateCourseCode, String> gradeLevelColumn;
    @FXML private TableColumn<StateCourseCode, Double> creditsColumn;
    @FXML private TableColumn<StateCourseCode, String> courseTypeColumn;
    @FXML private TableColumn<StateCourseCode, String> categoryColumn;

    // Detail panel components
    @FXML private VBox detailPanel;
    @FXML private Label detailCourseCode;
    @FXML private Label detailCourseName;
    @FXML private TextArea detailDescription;
    @FXML private Label detailSCEDCode;
    @FXML private Label detailSubjectArea;
    @FXML private Label detailGradeLevels;
    @FXML private Label detailCredits;
    @FXML private Label detailCourseType;
    @FXML private Label detailPrerequisites;
    @FXML private Label detailCTE;
    @FXML private Label detailGraduationReq;

    // Statistics panel
    @FXML private Label statTotalCourses;
    @FXML private Label statAPCourses;
    @FXML private Label statCTECourses;
    @FXML private Label statElectives;

    // School offering controls
    @FXML private CheckBox schoolLevelFilterToggle;
    @FXML private Label schoolLevelInfoLabel;
    @FXML private Label addedCountLabel;
    @FXML private TableColumn<StateCourseCode, Boolean> selectColumn;
    @FXML private TableColumn<StateCourseCode, String> addedColumn;

    // ========================================================================
    // SERVICES AND DATA
    // ========================================================================

    @Autowired
    private StateCourseCodeService stateCourseCodeService;

    @Autowired
    private StateCourseCodeRepository stateCourseCodeRepository;

    @Autowired(required = false)
    private SchoolConfigurationRepository schoolConfigurationRepository;

    private Stage stage;
    private ObservableList<StateCourseCode> allCourses = FXCollections.observableArrayList();
    private FilteredList<StateCourseCode> filteredCourses;
    private USState selectedState;
    private SchoolType schoolType;
    private Set<String> localCatalogCodes = new HashSet<>();
    private Set<StateCourseCode> selectedForBatchAdd = new HashSet<>();

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    @FXML
    public void initialize() {
        loadSchoolConfiguration();
        loadLocalCatalogCodes();
        setupStateSelector();
        setupFilters();
        setupTableColumns();
        setupTableSelection();
        setupSearch();
        setupFilteredList();
        setupSchoolLevelFilter();

        // Initially hide loading indicator
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }

        log.info("StateCourseCodeCatalogController initialized");
    }

    private void loadSchoolConfiguration() {
        if (schoolConfigurationRepository != null) {
            try {
                SchoolConfiguration config = schoolConfigurationRepository.findAll().stream().findFirst().orElse(null);
                if (config != null) {
                    schoolType = config.getSchoolType();
                    log.info("School type loaded: {}", schoolType);
                }
            } catch (Exception e) {
                log.warn("Could not load school configuration: {}", e.getMessage());
            }
        }
    }

    private void loadLocalCatalogCodes() {
        try {
            localCatalogCodes = stateCourseCodeService.getLocalCatalogStateCodes();
            log.info("Loaded {} existing local catalog course codes", localCatalogCodes.size());
        } catch (Exception e) {
            log.warn("Could not load local catalog codes: {}", e.getMessage());
        }
    }

    private void setupSchoolLevelFilter() {
        if (schoolLevelFilterToggle != null) {
            if (schoolType != null) {
                schoolLevelFilterToggle.setText("Show only " + schoolType.getDisplayName() + " courses");
                schoolLevelFilterToggle.setOnAction(e -> applyFilters());

                if (schoolLevelInfoLabel != null) {
                    schoolLevelInfoLabel.setText("School: " + schoolType.getDisplayName()
                            + " (" + schoolType.getGradeRangeDisplay() + ")");
                }
            } else {
                schoolLevelFilterToggle.setDisable(true);
                schoolLevelFilterToggle.setText("Configure school type in settings first");
            }
        }
        updateAddedCount();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void setupStateSelector() {
        // Get states that have course data, plus all states for selection
        List<USState> statesWithData = stateCourseCodeService.getStatesWithData();

        // Create list with all US states
        List<USState> allStates = Arrays.stream(USState.values())
                .filter(s -> s.ordinal() < 53) // Only US states and DC, not territories or special
                .sorted((a, b) -> a.getDisplayName().compareTo(b.getDisplayName()))
                .collect(Collectors.toList());

        stateSelector.setItems(FXCollections.observableArrayList(allStates));

        // Custom cell factory to show state name with data indicator
        stateSelector.setCellFactory(lv -> new ListCell<USState>() {
            @Override
            protected void updateItem(USState state, boolean empty) {
                super.updateItem(state, empty);
                if (empty || state == null) {
                    setText(null);
                } else {
                    String text = state.getDisplayName();
                    if (statesWithData.contains(state)) {
                        text += " (Data Loaded)";
                    }
                    setText(text);
                }
            }
        });

        stateSelector.setButtonCell(new ListCell<USState>() {
            @Override
            protected void updateItem(USState state, boolean empty) {
                super.updateItem(state, empty);
                setText(empty || state == null ? "Select a State" : state.getDisplayName());
            }
        });

        // Handle state selection
        stateSelector.setOnAction(e -> {
            selectedState = stateSelector.getValue();
            if (selectedState != null) {
                loadCoursesForState(selectedState);
            }
        });

        // Set default if Texas has data (common state)
        if (statesWithData.contains(USState.TX)) {
            stateSelector.setValue(USState.TX);
        } else if (!statesWithData.isEmpty()) {
            stateSelector.setValue(statesWithData.get(0));
        }
    }

    private void setupFilters() {
        // Grade level filter
        gradeLevelFilter.setItems(FXCollections.observableArrayList(
                "All Grades",
                "Pre-K",
                "Kindergarten",
                "Elementary (K-5)",
                "Middle School (6-8)",
                "High School (9-12)",
                "Grade 1", "Grade 2", "Grade 3", "Grade 4", "Grade 5",
                "Grade 6", "Grade 7", "Grade 8",
                "Grade 9", "Grade 10", "Grade 11", "Grade 12"
        ));
        gradeLevelFilter.setValue("All Grades");
        gradeLevelFilter.setOnAction(e -> applyFilters());

        // Subject area filter
        subjectAreaFilter.setItems(FXCollections.observableArrayList(SCEDSubjectArea.values()));
        subjectAreaFilter.setPromptText("All Subjects");
        subjectAreaFilter.setCellFactory(lv -> new ListCell<SCEDSubjectArea>() {
            @Override
            protected void updateItem(SCEDSubjectArea area, boolean empty) {
                super.updateItem(area, empty);
                setText(empty || area == null ? null : area.getCodeWithName());
            }
        });
        subjectAreaFilter.setButtonCell(new ListCell<SCEDSubjectArea>() {
            @Override
            protected void updateItem(SCEDSubjectArea area, boolean empty) {
                super.updateItem(area, empty);
                setText(empty || area == null ? "All Subjects" : area.getDisplayName());
            }
        });
        subjectAreaFilter.setOnAction(e -> applyFilters());

        // Course type filter
        courseTypeFilter.setItems(FXCollections.observableArrayList(CourseType.values()));
        courseTypeFilter.setPromptText("All Types");
        courseTypeFilter.setCellFactory(lv -> new ListCell<CourseType>() {
            @Override
            protected void updateItem(CourseType type, boolean empty) {
                super.updateItem(type, empty);
                setText(empty || type == null ? null : type.getDisplayName());
            }
        });
        courseTypeFilter.setButtonCell(new ListCell<CourseType>() {
            @Override
            protected void updateItem(CourseType type, boolean empty) {
                super.updateItem(type, empty);
                setText(empty || type == null ? "All Types" : type.getDisplayName());
            }
        });
        courseTypeFilter.setOnAction(e -> applyFilters());
    }

    private void setupTableColumns() {
        stateCodeColumn.setCellValueFactory(new PropertyValueFactory<>("stateCourseCode"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        scedCodeColumn.setCellValueFactory(new PropertyValueFactory<>("scedCode"));
        creditsColumn.setCellValueFactory(new PropertyValueFactory<>("credits"));

        subjectAreaColumn.setCellValueFactory(cellData -> {
            SCEDSubjectArea area = cellData.getValue().getSubjectArea();
            return new SimpleStringProperty(area != null ? area.getDisplayName() : "");
        });

        gradeLevelColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getGradeLevelRange()));

        courseTypeColumn.setCellValueFactory(cellData -> {
            CourseType type = cellData.getValue().getCourseType();
            return new SimpleStringProperty(type != null ? type.getDisplayName() : "Regular");
        });

        categoryColumn.setCellValueFactory(cellData -> {
            CourseCategory cat = cellData.getValue().getCourseCategory();
            return new SimpleStringProperty(cat != null ? cat.name() : "CORE");
        });

        // "Already Added" indicator column
        if (addedColumn != null) {
            addedColumn.setCellValueFactory(cellData -> {
                boolean added = localCatalogCodes.contains(cellData.getValue().getStateCourseCode());
                return new SimpleStringProperty(added ? "In Catalog" : "");
            });
            addedColumn.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.isEmpty()) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
                    }
                }
            });
        }

        // Select checkbox column
        if (selectColumn != null) {
            selectColumn.setCellFactory(col -> new TableCell<>() {
                private final CheckBox checkBox = new CheckBox();
                {
                    checkBox.setOnAction(e -> {
                        StateCourseCode course = getTableView().getItems().get(getIndex());
                        if (checkBox.isSelected()) {
                            selectedForBatchAdd.add(course);
                        } else {
                            selectedForBatchAdd.remove(course);
                        }
                    });
                }
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        StateCourseCode course = getTableView().getItems().get(getIndex());
                        boolean alreadyAdded = localCatalogCodes.contains(course.getStateCourseCode());
                        checkBox.setDisable(alreadyAdded);
                        checkBox.setSelected(selectedForBatchAdd.contains(course));
                        setGraphic(checkBox);
                    }
                }
            });
        }

        // Enable multi-select
        courseTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Make columns sortable
        courseTable.getSortOrder().add(courseNameColumn);
    }

    private void setupTableSelection() {
        courseTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showCourseDetails(newVal);
            } else {
                clearCourseDetails();
            }
        });
    }

    private void setupSearch() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }
    }

    private void setupFilteredList() {
        filteredCourses = new FilteredList<>(allCourses, p -> true);
        SortedList<StateCourseCode> sortedCourses = new SortedList<>(filteredCourses);
        sortedCourses.comparatorProperty().bind(courseTable.comparatorProperty());
        courseTable.setItems(sortedCourses);
    }

    // ========================================================================
    // DATA LOADING
    // ========================================================================

    private void loadCoursesForState(USState state) {
        if (state == null) return;

        showLoading(true);
        updateStateInfo(state);

        Task<List<StateCourseCode>> loadTask = new Task<>() {
            @Override
            protected List<StateCourseCode> call() {
                return stateCourseCodeService.findByState(state);
            }
        };

        loadTask.setOnSucceeded(e -> {
            allCourses.setAll(loadTask.getValue());
            applyFilters();
            updateStatistics(state);
            showLoading(false);
            log.info("Loaded {} courses for state {}", allCourses.size(), state);
        });

        loadTask.setOnFailed(e -> {
            log.error("Failed to load courses for state {}", state, loadTask.getException());
            showLoading(false);
            showError("Failed to Load", "Could not load course catalog for " + state.getDisplayName());
        });

        new Thread(loadTask).start();
    }

    private void updateStateInfo(USState state) {
        if (stateInfoLabel != null && state != null) {
            stateInfoLabel.setText(String.format("%s - %s",
                    state.getDisplayName(), state.getCertifyingAgency()));
        }
    }

    private void updateStatistics(USState state) {
        if (state == null) return;

        Map<String, Object> stats = stateCourseCodeService.getStatistics(state);

        if (statTotalCourses != null) {
            statTotalCourses.setText(String.valueOf(stats.get("totalCourses")));
        }

        // Count AP courses
        long apCount = allCourses.stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsAP()))
                .count();
        if (statAPCourses != null) {
            statAPCourses.setText(String.valueOf(apCount));
        }

        // Count CTE courses
        long cteCount = allCourses.stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsCTE()))
                .count();
        if (statCTECourses != null) {
            statCTECourses.setText(String.valueOf(cteCount));
        }

        // Count electives
        long electiveCount = allCourses.stream()
                .filter(c -> c.getCourseCategory() == CourseCategory.ELECTIVE)
                .count();
        if (statElectives != null) {
            statElectives.setText(String.valueOf(electiveCount));
        }
    }

    // ========================================================================
    // FILTERING
    // ========================================================================

    private void applyFilters() {
        filteredCourses.setPredicate(course -> {
            // School level filter (auto-filter by school type)
            if (schoolLevelFilterToggle != null && schoolLevelFilterToggle.isSelected() && schoolType != null) {
                int minGrade = schoolType.getMinGradeValue();
                int maxGrade = schoolType.getMaxGradeValue();
                Integer courseMin = course.getMinGradeLevel();
                Integer courseMax = course.getMaxGradeLevel();
                if (courseMin != null || courseMax != null) {
                    int cMin = courseMin != null ? courseMin : -1;
                    int cMax = courseMax != null ? courseMax : 12;
                    if (cMin > maxGrade || cMax < minGrade) {
                        return false;
                    }
                }
            }

            // Search filter
            String search = searchField != null ? searchField.getText() : "";
            if (!search.isEmpty()) {
                String lowerSearch = search.toLowerCase();
                boolean matches = course.getCourseName().toLowerCase().contains(lowerSearch) ||
                        course.getStateCourseCode().toLowerCase().contains(lowerSearch) ||
                        (course.getScedCode() != null && course.getScedCode().contains(lowerSearch));
                if (!matches) return false;
            }

            // Grade level filter
            String gradeFilter = gradeLevelFilter != null ? gradeLevelFilter.getValue() : "All Grades";
            if (gradeFilter != null && !gradeFilter.equals("All Grades")) {
                if (!matchesGradeFilter(course, gradeFilter)) {
                    return false;
                }
            }

            // Subject area filter
            SCEDSubjectArea subjectFilter = subjectAreaFilter != null ? subjectAreaFilter.getValue() : null;
            if (subjectFilter != null && course.getSubjectArea() != subjectFilter) {
                return false;
            }

            // Course type filter
            CourseType typeFilter = courseTypeFilter != null ? courseTypeFilter.getValue() : null;
            if (typeFilter != null && course.getCourseType() != typeFilter) {
                return false;
            }

            return true;
        });

        updateRecordCount();
    }

    private boolean matchesGradeFilter(StateCourseCode course, String gradeFilter) {
        Integer min = course.getMinGradeLevel();
        Integer max = course.getMaxGradeLevel();

        // Handle null values - assume course is available for all grades
        if (min == null && max == null) return true;
        if (min == null) min = -1;
        if (max == null) max = 12;

        switch (gradeFilter) {
            case "Pre-K":
                return min <= -1 && max >= -1;
            case "Kindergarten":
                return min <= 0 && max >= 0;
            case "Elementary (K-5)":
                return min <= 5 && max >= 0;
            case "Middle School (6-8)":
                return min <= 8 && max >= 6;
            case "High School (9-12)":
                return min <= 12 && max >= 9;
            default:
                // Handle specific grades (Grade 1, Grade 2, etc.)
                if (gradeFilter.startsWith("Grade ")) {
                    try {
                        int grade = Integer.parseInt(gradeFilter.substring(6).trim());
                        return min <= grade && max >= grade;
                    } catch (NumberFormatException e) {
                        return true;
                    }
                }
                return true;
        }
    }

    private void updateRecordCount() {
        if (recordCountLabel != null) {
            int filtered = filteredCourses.size();
            int total = allCourses.size();
            if (filtered == total) {
                recordCountLabel.setText(String.format("Showing %d courses", total));
            } else {
                recordCountLabel.setText(String.format("Showing %d of %d courses", filtered, total));
            }
        }
    }

    // ========================================================================
    // DETAIL PANEL
    // ========================================================================

    private void showCourseDetails(StateCourseCode course) {
        if (detailPanel != null) {
            detailPanel.setVisible(true);
        }

        if (detailCourseCode != null) detailCourseCode.setText(course.getStateCourseCode());
        if (detailCourseName != null) detailCourseName.setText(course.getCourseName());
        if (detailDescription != null) detailDescription.setText(
                course.getDescription() != null ? course.getDescription() : "No description available");
        if (detailSCEDCode != null) detailSCEDCode.setText(
                course.getScedCode() != null ? course.getScedCode() : "N/A");
        if (detailSubjectArea != null) detailSubjectArea.setText(
                course.getSubjectArea() != null ? course.getSubjectArea().getDisplayName() : "N/A");
        if (detailGradeLevels != null) detailGradeLevels.setText(course.getGradeLevelRange());
        if (detailCredits != null) detailCredits.setText(
                course.getCredits() != null ? String.valueOf(course.getCredits()) : "1.0");
        if (detailCourseType != null) detailCourseType.setText(
                course.getCourseType() != null ? course.getCourseType().getDisplayName() : "Regular");
        if (detailPrerequisites != null) detailPrerequisites.setText(
                course.getPrerequisites() != null ? course.getPrerequisites() : "None");

        // CTE and special designations
        StringBuilder special = new StringBuilder();
        if (Boolean.TRUE.equals(course.getIsCTE())) {
            special.append("CTE");
            if (course.getCteCluster() != null) special.append(" (").append(course.getCteCluster()).append(")");
        }
        if (Boolean.TRUE.equals(course.getIsAP())) {
            if (special.length() > 0) special.append(", ");
            special.append("AP");
        }
        if (Boolean.TRUE.equals(course.getIsIB())) {
            if (special.length() > 0) special.append(", ");
            special.append("IB");
        }
        if (Boolean.TRUE.equals(course.getIsDualCredit())) {
            if (special.length() > 0) special.append(", ");
            special.append("Dual Credit");
        }
        if (Boolean.TRUE.equals(course.getIsSTEM())) {
            if (special.length() > 0) special.append(", ");
            special.append("STEM");
        }
        if (detailCTE != null) {
            detailCTE.setText(special.length() > 0 ? special.toString() : "None");
        }

        // Graduation requirement
        if (detailGraduationReq != null) {
            if (Boolean.TRUE.equals(course.getGraduationRequirement())) {
                String reqType = course.getGraduationRequirementType();
                detailGraduationReq.setText(reqType != null ? "Yes (" + reqType + ")" : "Yes");
            } else {
                detailGraduationReq.setText("No");
            }
        }
    }

    private void clearCourseDetails() {
        if (detailPanel != null) {
            detailPanel.setVisible(false);
        }
    }

    // ========================================================================
    // CSV IMPORT
    // ========================================================================

    @FXML
    private void handleImportCSV() {
        if (selectedState == null) {
            showError("No State Selected", "Please select a state before importing course data.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import State Course Catalog");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = fileChooser.showOpenDialog(stage);
        if (file == null) return;

        // Show import dialog
        Dialog<ButtonType> dialog = createImportDialog(file);
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                performImport(file);
            }
        });
    }

    private Dialog<ButtonType> createImportDialog(File file) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Import Course Catalog");
        dialog.setHeaderText("Import courses from: " + file.getName());

        // Content
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        content.getChildren().addAll(
                new Label("State: " + selectedState.getDisplayName()),
                new Label("File: " + file.getName()),
                new Separator(),
                new Label("The import will:"),
                new Label("• Create new course codes that don't exist"),
                new Label("• Update existing course codes with new data"),
                new Label("• Skip rows with errors (logged for review)"),
                new Separator(),
                new Label("CSV should have headers including:"),
                new Label("  state_course_code (or course_code, service_id)"),
                new Label("  course_name (or course_title, name)"),
                new Label("  Optional: sced_code, subject_area, min_grade, max_grade, credits")
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        return dialog;
    }

    private void performImport(File file) {
        showLoading(true);

        Task<StateCourseCodeService.ImportResult> importTask = new Task<>() {
            @Override
            protected StateCourseCodeService.ImportResult call() throws Exception {
                // Create a MultipartFile wrapper
                MultipartFile multipartFile = new FileMultipartFile(file);
                String schoolYear = "2025-2026"; // Current school year
                return stateCourseCodeService.importFromCSV(selectedState, multipartFile, schoolYear);
            }
        };

        importTask.setOnSucceeded(e -> {
            showLoading(false);
            StateCourseCodeService.ImportResult result = importTask.getValue();
            showImportResult(result);
            loadCoursesForState(selectedState);
        });

        importTask.setOnFailed(e -> {
            showLoading(false);
            log.error("Import failed", importTask.getException());
            showError("Import Failed", "Failed to import course catalog: " + importTask.getException().getMessage());
        });

        new Thread(importTask).start();
    }

    private void showImportResult(StateCourseCodeService.ImportResult result) {
        Alert alert = new Alert(result.hasErrors() ? Alert.AlertType.WARNING : Alert.AlertType.INFORMATION);
        alert.setTitle("Import Complete");
        alert.setHeaderText(result.getSummary());

        StringBuilder content = new StringBuilder();
        content.append("Courses created: ").append(result.getCreatedCount()).append("\n");
        content.append("Courses updated: ").append(result.getUpdatedCount()).append("\n");
        content.append("Failed rows: ").append(result.getFailedCount()).append("\n");

        if (result.hasErrors()) {
            content.append("\nErrors (first 10):\n");
            result.getErrors().stream()
                    .limit(10)
                    .forEach(err -> content.append("• ").append(err).append("\n"));
            if (result.getErrors().size() > 10) {
                content.append("... and ").append(result.getErrors().size() - 10).append(" more errors");
            }
        }

        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    // ========================================================================
    // ACTIONS
    // ========================================================================

    @FXML
    private void handleRefresh() {
        if (selectedState != null) {
            loadCoursesForState(selectedState);
        }
    }

    @FXML
    private void handleClearFilters() {
        if (searchField != null) searchField.clear();
        if (gradeLevelFilter != null) gradeLevelFilter.setValue("All Grades");
        if (subjectAreaFilter != null) subjectAreaFilter.setValue(null);
        if (courseTypeFilter != null) courseTypeFilter.setValue(null);
        applyFilters();
    }

    @FXML
    private void handleExportCatalog() {
        if (selectedState == null || allCourses.isEmpty()) {
            showError("No Data", "No course catalog data to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Course Catalog");
        fileChooser.setInitialFileName(selectedState.name() + "_course_catalog.csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            exportToCSV(file);
        }
    }

    private void exportToCSV(File file) {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
            // Write header
            writer.println("state_course_code,course_name,sced_code,subject_area,min_grade,max_grade,credits,course_type,category,is_cte,is_ap,is_ib");

            // Write data
            for (StateCourseCode course : filteredCourses) {
                writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",%s,%s,%s,\"%s\",\"%s\",%s,%s,%s%n",
                        escapeCSV(course.getStateCourseCode()),
                        escapeCSV(course.getCourseName()),
                        escapeCSV(course.getScedCode()),
                        course.getSubjectArea() != null ? course.getSubjectArea().name() : "",
                        course.getMinGradeLevel() != null ? course.getMinGradeLevel() : "",
                        course.getMaxGradeLevel() != null ? course.getMaxGradeLevel() : "",
                        course.getCredits() != null ? course.getCredits() : "1.0",
                        course.getCourseType() != null ? course.getCourseType().name() : "REGULAR",
                        course.getCourseCategory() != null ? course.getCourseCategory().name() : "CORE",
                        Boolean.TRUE.equals(course.getIsCTE()),
                        Boolean.TRUE.equals(course.getIsAP()),
                        Boolean.TRUE.equals(course.getIsIB())
                );
            }

            showInfo("Export Complete", "Exported " + filteredCourses.size() + " courses to " + file.getName());

        } catch (IOException e) {
            log.error("Export failed", e);
            showError("Export Failed", "Could not export catalog: " + e.getMessage());
        }
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    @FXML
    private void handleCopyToLocalCatalog() {
        StateCourseCode selected = courseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No Selection", "Please select a course to copy to the local catalog.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Copy to Local Catalog");
        confirm.setHeaderText("Copy State Course to Local Catalog");
        confirm.setContentText("Copy \"" + selected.getCourseName() + "\" (" + selected.getStateCourseCode() + ") to the local course catalog?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    stateCourseCodeService.copyToLocalCatalog(selected);
                    showInfo("Success", "Course \"" + selected.getCourseName() + "\" has been copied to the local catalog.");
                } catch (Exception e) {
                    log.error("Failed to copy course to local catalog", e);
                    showError("Copy Failed", "Could not copy course: " + e.getMessage());
                }
            }
        });
    }

    // ========================================================================
    // BATCH ADD TO SCHOOL CATALOG
    // ========================================================================

    @FXML
    private void handleBatchAddToSchool() {
        if (selectedForBatchAdd.isEmpty()) {
            showError("No Selection", "Please select courses to add using the checkboxes.");
            return;
        }

        // Filter out already-added courses
        List<StateCourseCode> toAdd = selectedForBatchAdd.stream()
                .filter(c -> !localCatalogCodes.contains(c.getStateCourseCode()))
                .collect(Collectors.toList());

        if (toAdd.isEmpty()) {
            showInfo("Nothing to Add", "All selected courses are already in the school catalog.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Add Courses to School Catalog");
        confirm.setHeaderText("Add " + toAdd.size() + " courses to school catalog?");
        StringBuilder details = new StringBuilder();
        toAdd.stream().limit(10).forEach(c ->
                details.append("  - ").append(c.getCourseName()).append("\n"));
        if (toAdd.size() > 10) {
            details.append("  ... and ").append(toAdd.size() - 10).append(" more\n");
        }
        confirm.setContentText(details.toString());

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                showLoading(true);

                Task<StateCourseCodeService.BatchCopyResult> batchTask = new Task<>() {
                    @Override
                    protected StateCourseCodeService.BatchCopyResult call() {
                        return stateCourseCodeService.copyMultipleToLocalCatalog(toAdd);
                    }
                };

                batchTask.setOnSucceeded(e -> {
                    showLoading(false);
                    StateCourseCodeService.BatchCopyResult result = batchTask.getValue();
                    showInfo("Batch Add Complete", result.getSummary());
                    selectedForBatchAdd.clear();
                    loadLocalCatalogCodes();
                    updateAddedCount();
                    // Refresh table to update "In Catalog" indicators
                    courseTable.refresh();
                });

                batchTask.setOnFailed(e -> {
                    showLoading(false);
                    log.error("Batch add failed", batchTask.getException());
                    showError("Batch Add Failed", "Error: " + batchTask.getException().getMessage());
                });

                new Thread(batchTask).start();
            }
        });
    }

    @FXML
    private void handleSelectAllVisible() {
        selectedForBatchAdd.addAll(filteredCourses.stream()
                .filter(c -> !localCatalogCodes.contains(c.getStateCourseCode()))
                .collect(Collectors.toSet()));
        courseTable.refresh();
    }

    @FXML
    private void handleDeselectAll() {
        selectedForBatchAdd.clear();
        courseTable.refresh();
    }

    private void updateAddedCount() {
        if (addedCountLabel != null) {
            addedCountLabel.setText(localCatalogCodes.size() + " courses in school catalog");
        }
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    private void showLoading(boolean show) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(show);
        }
        if (courseTable != null) {
            courseTable.setDisable(show);
        }
    }

    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showInfo(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // ========================================================================
    // HELPER CLASS FOR FILE UPLOAD
    // ========================================================================

    /**
     * Simple MultipartFile implementation for local files
     */
    private static class FileMultipartFile implements MultipartFile {
        private final File file;

        public FileMultipartFile(File file) {
            this.file = file;
        }

        @Override
        public String getName() {
            return file.getName();
        }

        @Override
        public String getOriginalFilename() {
            return file.getName();
        }

        @Override
        public String getContentType() {
            return "text/csv";
        }

        @Override
        public boolean isEmpty() {
            return file.length() == 0;
        }

        @Override
        public long getSize() {
            return file.length();
        }

        @Override
        public byte[] getBytes() throws IOException {
            return java.nio.file.Files.readAllBytes(file.toPath());
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new FileInputStream(file);
        }

        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            java.nio.file.Files.copy(file.toPath(), dest.toPath());
        }
    }
}
