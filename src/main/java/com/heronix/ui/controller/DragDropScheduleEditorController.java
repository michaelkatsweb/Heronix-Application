package com.heronix.ui.controller;

import com.heronix.model.domain.CourseSection;
import com.heronix.model.domain.Room;
import com.heronix.model.domain.Teacher;
import com.heronix.repository.CourseSectionRepository;
import com.heronix.repository.RoomRepository;
import com.heronix.repository.TeacherRepository;
import com.heronix.service.ConflictDetectionService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Master Schedule Board Controller (Kanban-Style)
 *
 * Provides an interactive, touch-friendly master schedule board where course sections
 * appear as rich cards on a Teacher x Period grid. Highlights conflicts in real-time
 * using a kanban color scheme (green=OK, amber=warning, red=conflict).
 *
 * Features:
 * - Rich kanban-style cards with subject-colored borders
 * - Unassigned sections pool with search filtering
 * - Real-time conflict detection (teacher double-booking, room double-booking, near-capacity)
 * - Summary statistics bar (assigned, unassigned, conflicts, utilization)
 * - Drag-and-drop with CSS-based visual feedback
 * - Undo/redo support
 * - Department-grouped teacher rows
 */
@Component
@Slf4j
public class DragDropScheduleEditorController {

    @Autowired
    private CourseSectionRepository courseSectionRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ConflictDetectionService conflictDetectionService;

    // Header Controls
    @FXML private ToggleButton editModeToggle;
    @FXML private Button undoButton;
    @FXML private Button redoButton;

    // Status Labels
    @FXML private Label editModeStatusLabel;
    @FXML private Label changesStatusLabel;
    @FXML private Label conflictsStatusLabel;

    // Statistics Bar
    @FXML private Label assignedCountLabel;
    @FXML private Label unassignedCountLabel;
    @FXML private Label conflictCountLabel;
    @FXML private Label teacherUtilLabel;

    // Filters
    @FXML private ComboBox<String> teacherFilterComboBox;
    @FXML private ComboBox<String> roomFilterComboBox;
    @FXML private ComboBox<String> gradeFilterComboBox;
    @FXML private CheckBox showConflictsCheckbox;

    // Schedule Grid
    @FXML private GridPane scheduleGrid;
    @FXML private VBox scheduleGridContainer;

    // Unassigned Pool
    @FXML private VBox unassignedPoolContainer;
    @FXML private TextField poolSearchField;

    // Details Panel
    @FXML private VBox sectionDetailsContainer;
    @FXML private Label noSelectionLabel;
    @FXML private VBox sectionDetailsBox;
    @FXML private Label detailCourseLabel;
    @FXML private Label detailSectionLabel;
    @FXML private Label detailTeacherLabel;
    @FXML private Label detailRoomLabel;
    @FXML private Label detailPeriodLabel;
    @FXML private Label detailEnrollmentLabel;

    // Lists
    @FXML private ListView<String> conflictsList;
    @FXML private ListView<String> recentChangesList;
    @FXML private VBox suggestionsContainer;

    // Constants
    private static final int NUM_PERIODS = 8;
    private static final DataFormat SECTION_DATA_FORMAT = new DataFormat("application/section-id");

    // Subject color map (from ModernScheduleGrid)
    private static final Map<String, String> SUBJECT_COLORS = new HashMap<>();
    static {
        SUBJECT_COLORS.put("Mathematics", "#2563eb");
        SUBJECT_COLORS.put("Algebra", "#2563eb");
        SUBJECT_COLORS.put("Geometry", "#2563eb");
        SUBJECT_COLORS.put("Calculus", "#2563eb");
        SUBJECT_COLORS.put("Pre-Calculus", "#2563eb");
        SUBJECT_COLORS.put("Statistics", "#2563eb");
        SUBJECT_COLORS.put("Science", "#10b981");
        SUBJECT_COLORS.put("Biology", "#10b981");
        SUBJECT_COLORS.put("Chemistry", "#10b981");
        SUBJECT_COLORS.put("Physics", "#10b981");
        SUBJECT_COLORS.put("Environmental Science", "#10b981");
        SUBJECT_COLORS.put("English", "#f59e0b");
        SUBJECT_COLORS.put("Literature", "#f59e0b");
        SUBJECT_COLORS.put("Spanish", "#f59e0b");
        SUBJECT_COLORS.put("French", "#f59e0b");
        SUBJECT_COLORS.put("History", "#ef4444");
        SUBJECT_COLORS.put("World History", "#ef4444");
        SUBJECT_COLORS.put("US History", "#ef4444");
        SUBJECT_COLORS.put("Government", "#ef4444");
        SUBJECT_COLORS.put("Physical Education", "#06b6d4");
        SUBJECT_COLORS.put("Health", "#06b6d4");
        SUBJECT_COLORS.put("Art", "#a855f7");
        SUBJECT_COLORS.put("Music", "#a855f7");
        SUBJECT_COLORS.put("Theater", "#a855f7");
        SUBJECT_COLORS.put("Programming", "#8b5cf6");
        SUBJECT_COLORS.put("Computer Science", "#8b5cf6");
        SUBJECT_COLORS.put("Introduction", "#8b5cf6");
    }

    // Period time ranges for headers
    private static final String[] PERIOD_TIMES = {
        "8:00-8:50", "8:55-9:45", "9:50-10:40", "10:45-11:35",
        "11:40-12:30", "12:35-1:25", "1:30-2:20", "2:25-3:15"
    };

    // Conflict status enum
    private enum ConflictStatus {
        OK, WARNING, CONFLICT
    }

    // State
    private boolean editMode = false;
    private CourseSection selectedSection;
    private Map<Long, CourseSection> modifiedSections = new HashMap<>();
    private Stack<ScheduleChange> undoStack = new Stack<>();
    private Stack<ScheduleChange> redoStack = new Stack<>();
    private Map<String, VBox> cellMap = new HashMap<>();

    // Cached data to avoid repeated DB calls per grid build
    private List<CourseSection> cachedSections = new ArrayList<>();
    private List<Teacher> cachedTeachers = new ArrayList<>();

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    @FXML
    public void initialize() {
        log.info("Initializing Master Schedule Board Controller");

        setupFilters();

        // Wire pool search field listener
        if (poolSearchField != null) {
            poolSearchField.textProperty().addListener((obs, oldVal, newVal) -> buildUnassignedPool());
        }

        loadScheduleData();
    }

    private void setupFilters() {
        if (teacherFilterComboBox != null) {
            List<Teacher> teachers = teacherRepository.findAllActive();
            ObservableList<String> teacherNames = FXCollections.observableArrayList("All Teachers");
            teacherNames.addAll(teachers.stream()
                .map(t -> t.getFirstName() + " " + t.getLastName())
                .collect(Collectors.toList()));
            teacherFilterComboBox.setItems(teacherNames);
            teacherFilterComboBox.setValue("All Teachers");
        }

        if (roomFilterComboBox != null) {
            List<Room> rooms = roomRepository.findAll();
            ObservableList<String> roomNumbers = FXCollections.observableArrayList("All Rooms");
            roomNumbers.addAll(rooms.stream()
                .map(Room::getRoomNumber)
                .collect(Collectors.toList()));
            roomFilterComboBox.setItems(roomNumbers);
            roomFilterComboBox.setValue("All Rooms");
        }

        if (gradeFilterComboBox != null) {
            ObservableList<String> grades = FXCollections.observableArrayList(
                "All Grades", "9", "10", "11", "12"
            );
            gradeFilterComboBox.setItems(grades);
            gradeFilterComboBox.setValue("All Grades");
        }
    }

    // ========================================================================
    // TOOLBAR HANDLERS
    // ========================================================================

    @FXML
    private void handleEditModeToggle() {
        editMode = editModeToggle.isSelected();

        if (editMode) {
            safeSetText(editModeStatusLabel, "Edit Mode ACTIVE");
            editModeStatusLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
            editModeToggle.setText("Lock Mode");
        } else {
            safeSetText(editModeStatusLabel, "View Mode");
            editModeStatusLabel.setStyle("-fx-text-fill: #388e3c; -fx-font-weight: bold;");
            editModeToggle.setText("Edit Mode");
        }

        updateGridInteractivity();
    }

    @FXML
    private void handleUndo() {
        if (undoStack.isEmpty()) return;

        ScheduleChange change = undoStack.pop();
        applyChange(change.reverse());
        redoStack.push(change);

        updateUndoRedoButtons();
        addToRecentChanges("Undo: " + change.getDescription());
        refreshScheduleGrid();
    }

    @FXML
    private void handleRedo() {
        if (redoStack.isEmpty()) return;

        ScheduleChange change = redoStack.pop();
        applyChange(change);
        undoStack.push(change);

        updateUndoRedoButtons();
        addToRecentChanges("Redo: " + change.getDescription());
        refreshScheduleGrid();
    }

    @FXML
    private void handleRefresh() {
        loadScheduleData();
    }

    @FXML
    private void handleSaveChanges() {
        if (modifiedSections.isEmpty()) {
            showInfo("No changes to save");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Save Changes");
        confirm.setHeaderText("Save Schedule Changes");
        confirm.setContentText(String.format("Save %d modified section(s)?", modifiedSections.size()));

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                saveChanges();
            }
        });
    }

    @FXML
    private void handleDiscardChanges() {
        if (modifiedSections.isEmpty()) {
            showInfo("No changes to discard");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Discard Changes");
        confirm.setHeaderText("Discard All Changes");
        confirm.setContentText(String.format("Discard %d unsaved change(s)?", modifiedSections.size()));

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                discardChanges();
            }
        });
    }

    @FXML
    private void handleFilterChange() {
        refreshScheduleGrid();
    }

    @FXML
    private void handleShowConflictsToggle() {
        refreshScheduleGrid();
    }

    // ========================================================================
    // SECTION ACTIONS
    // ========================================================================

    @FXML
    private void handleSwapTeacher() {
        if (selectedSection == null) return;

        List<Teacher> teachers = teacherRepository.findAllActive();
        ChoiceDialog<Teacher> dialog = new ChoiceDialog<>(selectedSection.getAssignedTeacher(), teachers);
        dialog.setTitle("Swap Teacher");
        dialog.setHeaderText("Select New Teacher");
        dialog.setContentText("Teacher:");

        dialog.showAndWait().ifPresent(newTeacher -> {
            ScheduleChange change = new ScheduleChange(
                selectedSection.getId(),
                "teacher",
                selectedSection.getAssignedTeacher(),
                newTeacher,
                String.format("Change teacher for %s to %s",
                    selectedSection.getCourse().getCourseName(),
                    newTeacher.getFirstName() + " " + newTeacher.getLastName())
            );

            applyChange(change);
            undoStack.push(change);
            redoStack.clear();
            updateUndoRedoButtons();
            addToRecentChanges(change.getDescription());
            refreshScheduleGrid();
        });
    }

    @FXML
    private void handleSwapRoom() {
        if (selectedSection == null) return;

        List<Room> rooms = roomRepository.findAll();
        ChoiceDialog<Room> dialog = new ChoiceDialog<>(selectedSection.getAssignedRoom(), rooms);
        dialog.setTitle("Swap Room");
        dialog.setHeaderText("Select New Room");
        dialog.setContentText("Room:");

        dialog.showAndWait().ifPresent(newRoom -> {
            ScheduleChange change = new ScheduleChange(
                selectedSection.getId(),
                "room",
                selectedSection.getAssignedRoom(),
                newRoom,
                String.format("Change room for %s to %s",
                    selectedSection.getCourse().getCourseName(),
                    newRoom.getRoomNumber())
            );

            applyChange(change);
            undoStack.push(change);
            redoStack.clear();
            updateUndoRedoButtons();
            addToRecentChanges(change.getDescription());
            refreshScheduleGrid();
        });
    }

    @FXML
    private void handleChangePeriod() {
        if (selectedSection == null) return;

        List<Integer> periods = new ArrayList<>();
        for (int i = 1; i <= NUM_PERIODS; i++) {
            periods.add(i);
        }

        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(selectedSection.getAssignedPeriod(), periods);
        dialog.setTitle("Change Period");
        dialog.setHeaderText("Select New Period");
        dialog.setContentText("Period:");

        dialog.showAndWait().ifPresent(newPeriod -> {
            ScheduleChange change = new ScheduleChange(
                selectedSection.getId(),
                "period",
                selectedSection.getAssignedPeriod(),
                newPeriod,
                String.format("Change period for %s to P%d",
                    selectedSection.getCourse().getCourseName(),
                    newPeriod)
            );

            applyChange(change);
            undoStack.push(change);
            redoStack.clear();
            updateUndoRedoButtons();
            addToRecentChanges(change.getDescription());
            refreshScheduleGrid();
        });
    }

    @FXML
    private void handleUnassignSection() {
        if (selectedSection == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Unassign Section");
        confirm.setContentText("Remove all assignments (teacher, room, period) from this section?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                unassignSection(selectedSection);
            }
        });
    }

    // ========================================================================
    // DATA LOADING
    // ========================================================================

    private void loadScheduleData() {
        log.info("Loading schedule data");

        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Cache data on background thread reference
                List<CourseSection> sections = courseSectionRepository.findAll();
                List<Teacher> teachers = teacherRepository.findAllActive();

                Platform.runLater(() -> {
                    cachedSections = sections;
                    cachedTeachers = teachers;
                    refreshScheduleGrid();
                    updateSuggestions();
                });
                return null;
            }

            @Override
            protected void failed() {
                log.error("Failed to load schedule data", getException());
                Platform.runLater(() -> showError("Failed to load schedule"));
            }
        };

        new Thread(loadTask).start();
    }

    // ========================================================================
    // SCHEDULE GRID (Kanban-Style)
    // ========================================================================

    private void buildScheduleGrid() {
        if (scheduleGrid == null) return;

        scheduleGrid.getChildren().clear();
        cellMap.clear();

        // Refresh cached data
        cachedSections = courseSectionRepository.findAll();
        cachedTeachers = teacherRepository.findAllActive();

        // Apply teacher filter
        List<Teacher> filteredTeachers = cachedTeachers;
        if (teacherFilterComboBox != null && teacherFilterComboBox.getValue() != null
                && !"All Teachers".equals(teacherFilterComboBox.getValue())) {
            String selected = teacherFilterComboBox.getValue();
            filteredTeachers = cachedTeachers.stream()
                .filter(t -> (t.getFirstName() + " " + t.getLastName()).equals(selected))
                .collect(Collectors.toList());
        }

        // Sort teachers by department then name
        filteredTeachers.sort(Comparator
            .comparing((Teacher t) -> t.getDepartment() != null ? t.getDepartment() : "ZZZ")
            .thenComparing(Teacher::getLastName)
            .thenComparing(Teacher::getFirstName));

        // Build header row
        buildGridHeaders();

        // Build rows for each teacher, inserting department dividers
        int row = 1;
        String currentDept = null;

        for (Teacher teacher : filteredTeachers) {
            String dept = teacher.getDepartment() != null ? teacher.getDepartment() : "Other";

            // Department divider
            if (!dept.equals(currentDept)) {
                currentDept = dept;
                Label deptLabel = new Label(dept.toUpperCase());
                deptLabel.getStyleClass().add("msb-department-divider");
                deptLabel.setMaxWidth(Double.MAX_VALUE);
                scheduleGrid.add(deptLabel, 0, row, NUM_PERIODS + 1, 1);
                row++;
            }

            // Teacher name header
            Label teacherLabel = new Label(teacher.getFirstName() + " " + teacher.getLastName());
            teacherLabel.getStyleClass().add("msb-teacher-header");
            teacherLabel.setMinWidth(180);
            teacherLabel.setMaxWidth(180);
            scheduleGrid.add(teacherLabel, 0, row);

            // Create cells for each period
            for (int period = 1; period <= NUM_PERIODS; period++) {
                VBox cell = createScheduleCell(teacher, period);
                scheduleGrid.add(cell, period, row);

                String cellKey = teacher.getId() + "-" + period;
                cellMap.put(cellKey, cell);
            }

            row++;
        }
    }

    private void buildGridHeaders() {
        // Corner cell
        Label cornerLabel = new Label("Teacher");
        cornerLabel.getStyleClass().add("msb-header-cell");
        cornerLabel.setMinWidth(180);
        cornerLabel.setMaxWidth(180);
        scheduleGrid.add(cornerLabel, 0, 0);

        // Period headers with time ranges
        for (int period = 1; period <= NUM_PERIODS; period++) {
            String timeRange = (period - 1 < PERIOD_TIMES.length) ? PERIOD_TIMES[period - 1] : "";
            Label periodLabel = new Label("Period " + period + "\n" + timeRange);
            periodLabel.getStyleClass().add("msb-header-cell");
            periodLabel.setMinWidth(180);
            periodLabel.setAlignment(Pos.CENTER);
            periodLabel.setWrapText(true);
            scheduleGrid.add(periodLabel, period, 0);
        }
    }

    private VBox createScheduleCell(Teacher teacher, int period) {
        VBox cell = new VBox(4);
        cell.setAlignment(Pos.TOP_LEFT);
        cell.setMinWidth(180);
        cell.setMinHeight(100);
        cell.setPadding(new Insets(6));

        // Find section for this teacher/period
        final int p = period;
        Optional<CourseSection> sectionOpt = cachedSections.stream()
            .filter(s -> s.getAssignedTeacher() != null &&
                       s.getAssignedTeacher().getId().equals(teacher.getId()) &&
                       s.getAssignedPeriod() != null &&
                       s.getAssignedPeriod() == p)
            .findFirst();

        if (sectionOpt.isPresent()) {
            CourseSection section = sectionOpt.get();
            VBox card = buildSectionCard(section, teacher, period);
            cell.getChildren().add(card);
        } else {
            // Empty cell
            cell.getStyleClass().add("msb-cell-empty");
            Label emptyLabel = new Label("(empty)");
            emptyLabel.setStyle("-fx-text-fill: #666; -fx-font-style: italic; -fx-font-size: 11px;");
            cell.getChildren().add(emptyLabel);
        }

        // Setup drag-and-drop
        setupCellDragAndDrop(cell, teacher, period);

        return cell;
    }

    private VBox buildSectionCard(CourseSection section, Teacher teacher, int period) {
        VBox card = new VBox(3);
        card.getStyleClass().add("msb-card");
        card.setPadding(new Insets(8));

        // Determine conflict status
        ConflictStatus status = determineConflictStatus(section);

        // Apply conflict status CSS class
        switch (status) {
            case OK:
                card.getStyleClass().add("msb-card-ok");
                break;
            case WARNING:
                card.getStyleClass().add("msb-card-warning");
                break;
            case CONFLICT:
                card.getStyleClass().add("msb-card-conflict");
                break;
        }

        // Apply subject-colored left border via inline style (overrides conflict border color for left)
        String courseName = section.getCourse() != null ? section.getCourse().getCourseName() : "";
        String subjectColor = getSubjectColor(courseName);
        if (status == ConflictStatus.OK) {
            card.setStyle("-fx-border-width: 0 0 0 4; -fx-border-color: transparent transparent transparent " + subjectColor + ";");
        }

        // Highlight if selected
        if (selectedSection != null && selectedSection.getId().equals(section.getId())) {
            card.getStyleClass().add("msb-card-selected");
        }

        // Teacher name (bold)
        Label teacherNameLabel = new Label(teacher.getFirstName() + " " + teacher.getLastName());
        teacherNameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #E0E0E0;");

        // Course name
        Label courseLabel = new Label(courseName);
        courseLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #CCCCCC;");
        courseLabel.setWrapText(true);

        // Room badge (colored pill)
        String roomText = section.getAssignedRoom() != null ? section.getAssignedRoom().getRoomNumber() : "TBA";
        Label roomBadge = new Label(roomText);
        roomBadge.getStyleClass().add("msb-card-room-badge");
        String roomBadgeColor = section.getAssignedRoom() != null ? "#0078D4" : "#6B7280";
        roomBadge.setStyle("-fx-background-color: " + roomBadgeColor + "; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 2 8;");

        // Enrollment count
        int current = section.getCurrentEnrollment() != null ? section.getCurrentEnrollment() : 0;
        int max = section.getMaxEnrollment() != null ? section.getMaxEnrollment() : 30;
        Label enrollLabel = new Label(current + "/" + max + " students");
        enrollLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #999;");

        // Layout: top row has teacher name, bottom has course + room badge + enrollment
        HBox topRow = new HBox(6);
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.getChildren().addAll(teacherNameLabel);

        HBox bottomRow = new HBox(8);
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        bottomRow.getChildren().addAll(roomBadge, enrollLabel);

        card.getChildren().addAll(courseLabel, bottomRow);

        // Modified indicator
        if (modifiedSections.containsKey(section.getId())) {
            card.setStyle(card.getStyle() + " -fx-effect: dropshadow(gaussian, rgba(245,158,11,0.5), 6, 0, 0, 0);");
        }

        // Click to select
        card.setOnMouseClicked(event -> selectSection(section));

        return card;
    }

    private ConflictStatus determineConflictStatus(CourseSection section) {
        if (section.getAssignedTeacher() == null || section.getAssignedPeriod() == null) {
            return ConflictStatus.OK;
        }

        // Check teacher double-booking
        boolean teacherConflict = cachedSections.stream()
            .anyMatch(s -> !s.getId().equals(section.getId()) &&
                       s.getAssignedTeacher() != null &&
                       s.getAssignedTeacher().getId().equals(section.getAssignedTeacher().getId()) &&
                       s.getAssignedPeriod() != null &&
                       s.getAssignedPeriod().equals(section.getAssignedPeriod()));

        if (teacherConflict) return ConflictStatus.CONFLICT;

        // Check room double-booking
        if (section.getAssignedRoom() != null) {
            boolean roomConflict = cachedSections.stream()
                .anyMatch(s -> !s.getId().equals(section.getId()) &&
                           s.getAssignedRoom() != null &&
                           s.getAssignedRoom().getId().equals(section.getAssignedRoom().getId()) &&
                           s.getAssignedPeriod() != null &&
                           s.getAssignedPeriod().equals(section.getAssignedPeriod()));

            if (roomConflict) return ConflictStatus.CONFLICT;
        }

        // Check near-capacity (>=90%)
        int current = section.getCurrentEnrollment() != null ? section.getCurrentEnrollment() : 0;
        int max = section.getMaxEnrollment() != null ? section.getMaxEnrollment() : 30;
        if (max > 0 && ((double) current / max) >= 0.9) {
            return ConflictStatus.WARNING;
        }

        // Also check room capacity
        if (section.getAssignedRoom() != null && section.getAssignedRoom().getCapacity() != null) {
            int roomCap = section.getAssignedRoom().getCapacity();
            if (roomCap > 0 && ((double) current / roomCap) >= 0.9) {
                return ConflictStatus.WARNING;
            }
        }

        return ConflictStatus.OK;
    }

    // ========================================================================
    // UNASSIGNED SECTIONS POOL
    // ========================================================================

    private void buildUnassignedPool() {
        if (unassignedPoolContainer == null) return;

        unassignedPoolContainer.getChildren().clear();

        // Filter sections with no teacher or no period
        List<CourseSection> unassigned = cachedSections.stream()
            .filter(s -> s.getAssignedTeacher() == null || s.getAssignedPeriod() == null)
            .collect(Collectors.toList());

        // Apply search filter from poolSearchField
        if (poolSearchField != null && poolSearchField.getText() != null && !poolSearchField.getText().isBlank()) {
            String search = poolSearchField.getText().toLowerCase().trim();
            unassigned = unassigned.stream()
                .filter(s -> {
                    String courseName = s.getCourse() != null ? s.getCourse().getCourseName() : "";
                    String courseCode = s.getCourse() != null ? s.getCourse().getCourseCode() : "";
                    String secNum = s.getSectionNumber() != null ? s.getSectionNumber() : "";
                    return courseName.toLowerCase().contains(search) ||
                           courseCode.toLowerCase().contains(search) ||
                           secNum.toLowerCase().contains(search);
                })
                .collect(Collectors.toList());
        }

        // Build pool cards
        for (CourseSection section : unassigned) {
            VBox poolCard = buildPoolCard(section);
            unassignedPoolContainer.getChildren().add(poolCard);
        }

        if (unassigned.isEmpty()) {
            Label emptyLabel = new Label("No unassigned sections");
            emptyLabel.setStyle("-fx-text-fill: #888; -fx-font-style: italic; -fx-padding: 10;");
            unassignedPoolContainer.getChildren().add(emptyLabel);
        }
    }

    private VBox buildPoolCard(CourseSection section) {
        VBox card = new VBox(2);
        card.getStyleClass().add("msb-pool-card");
        card.setPadding(new Insets(6, 8, 6, 8));

        // Course name
        String courseName = section.getCourse() != null ? section.getCourse().getCourseName() : "Unknown";
        Label nameLabel = new Label(courseName);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #E0E0E0;");
        nameLabel.setWrapText(true);

        // Course code + section number
        String courseCode = section.getCourse() != null ? section.getCourse().getCourseCode() : "";
        String secNum = section.getSectionNumber() != null ? section.getSectionNumber() : "";
        Label codeLabel = new Label(courseCode + " - Sec " + secNum);
        codeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #999;");

        // Enrollment
        int current = section.getCurrentEnrollment() != null ? section.getCurrentEnrollment() : 0;
        int max = section.getMaxEnrollment() != null ? section.getMaxEnrollment() : 30;
        Label enrollLabel = new Label(current + "/" + max + " enrolled");
        enrollLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888;");

        // Subject-colored left border
        String subjectColor = getSubjectColor(courseName);
        card.setStyle("-fx-border-width: 0 0 0 3; -fx-border-color: transparent transparent transparent " + subjectColor + ";");

        card.getChildren().addAll(nameLabel, codeLabel, enrollLabel);

        // Setup drag source for pool cards
        card.setOnDragDetected(event -> {
            if (!editMode) return;

            Dragboard db = card.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.put(SECTION_DATA_FORMAT, section.getId());
            db.setContent(content);

            card.setOpacity(0.5);
            event.consume();
        });

        card.setOnDragDone(event -> {
            card.setOpacity(1.0);
            event.consume();
        });

        // Click to select
        card.setOnMouseClicked(event -> selectSection(section));

        return card;
    }

    // ========================================================================
    // STATISTICS BAR
    // ========================================================================

    private void updateStatistics() {
        long assignedCount = cachedSections.stream()
            .filter(s -> s.getAssignedTeacher() != null && s.getAssignedPeriod() != null)
            .count();

        long unassignedCount = cachedSections.size() - assignedCount;

        long conflictCount = cachedSections.stream()
            .filter(s -> determineConflictStatus(s) == ConflictStatus.CONFLICT)
            .count();

        // Teacher utilization: assigned slots / (teachers * periods)
        int totalSlots = cachedTeachers.size() * NUM_PERIODS;
        double utilization = totalSlots > 0 ? ((double) assignedCount / totalSlots) * 100.0 : 0;

        safeSetText(assignedCountLabel, String.valueOf(assignedCount));
        safeSetText(unassignedCountLabel, String.valueOf(unassignedCount));
        safeSetText(conflictCountLabel, String.valueOf(conflictCount));
        safeSetText(teacherUtilLabel, String.format("%.0f%%", utilization));

        // Color-code conflict count
        if (conflictCountLabel != null) {
            if (conflictCount > 0) {
                conflictCountLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            } else {
                conflictCountLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
            }
        }
    }

    // ========================================================================
    // DRAG AND DROP (Enhanced Visual Feedback)
    // ========================================================================

    private void setupCellDragAndDrop(VBox cell, Teacher teacher, int period) {
        // Drag source
        cell.setOnDragDetected(event -> {
            if (!editMode) return;

            Optional<CourseSection> sectionOpt = findSectionInCell(teacher, period);
            if (sectionOpt.isEmpty()) return;

            Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.put(SECTION_DATA_FORMAT, sectionOpt.get().getId());
            db.setContent(content);

            // Visual feedback: reduce opacity of source
            cell.setOpacity(0.5);

            event.consume();
        });

        // Drag over
        cell.setOnDragOver(event -> {
            if (!editMode) return;

            if (event.getGestureSource() != cell && event.getDragboard().hasContent(SECTION_DATA_FORMAT)) {
                event.acceptTransferModes(TransferMode.MOVE);
            }

            event.consume();
        });

        // Drag entered — add CSS class
        cell.setOnDragEntered(event -> {
            if (!editMode) return;

            if (event.getGestureSource() != cell && event.getDragboard().hasContent(SECTION_DATA_FORMAT)) {
                cell.getStyleClass().add("msb-cell-drop-target");
            }

            event.consume();
        });

        // Drag exited — remove CSS class
        cell.setOnDragExited(event -> {
            cell.getStyleClass().remove("msb-cell-drop-target");
            event.consume();
        });

        // Drag dropped
        cell.setOnDragDropped(event -> {
            if (!editMode) return;

            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasContent(SECTION_DATA_FORMAT)) {
                Long sectionId = (Long) db.getContent(SECTION_DATA_FORMAT);
                handleSectionDrop(sectionId, teacher, period);
                success = true;
            }

            event.setDropCompleted(success);
            event.consume();
        });

        // Drag done — restore opacity
        cell.setOnDragDone(event -> {
            cell.setOpacity(1.0);
            event.consume();
        });
    }

    private void handleSectionDrop(Long sectionId, Teacher newTeacher, int newPeriod) {
        CourseSection section = courseSectionRepository.findById(sectionId).orElse(null);
        if (section == null) return;

        Teacher oldTeacher = section.getAssignedTeacher();
        Integer oldPeriod = section.getAssignedPeriod();

        ScheduleChange change = new ScheduleChange(
            sectionId,
            "move",
            new TeacherPeriod(oldTeacher, oldPeriod),
            new TeacherPeriod(newTeacher, newPeriod),
            String.format("Move %s from %s P%d to %s P%d",
                section.getCourse().getCourseName(),
                oldTeacher != null ? oldTeacher.getLastName() : "?",
                oldPeriod != null ? oldPeriod : 0,
                newTeacher.getLastName(),
                newPeriod)
        );

        applyChange(change);
        undoStack.push(change);
        redoStack.clear();
        updateUndoRedoButtons();
        addToRecentChanges(change.getDescription());
        refreshScheduleGrid();
    }

    // ========================================================================
    // CHANGE MANAGEMENT
    // ========================================================================

    private void applyChange(ScheduleChange change) {
        CourseSection section = courseSectionRepository.findById(change.getSectionId()).orElse(null);
        if (section == null) return;

        switch (change.getField()) {
            case "teacher":
                section.setAssignedTeacher((Teacher) change.getNewValue());
                break;
            case "room":
                section.setAssignedRoom((Room) change.getNewValue());
                break;
            case "period":
                section.setAssignedPeriod((Integer) change.getNewValue());
                break;
            case "move":
                TeacherPeriod tp = (TeacherPeriod) change.getNewValue();
                section.setAssignedTeacher(tp.teacher);
                section.setAssignedPeriod(tp.period);
                break;
        }

        modifiedSections.put(section.getId(), section);
        updateChangesStatus();
    }

    private void selectSection(CourseSection section) {
        selectedSection = section;

        if (sectionDetailsBox != null) {
            sectionDetailsBox.setVisible(true);
            sectionDetailsBox.setManaged(true);
        }

        if (noSelectionLabel != null) {
            noSelectionLabel.setVisible(false);
            noSelectionLabel.setManaged(false);
        }

        safeSetText(detailCourseLabel, section.getCourse().getCourseName() + " - " +
            section.getCourse().getCourseCode());
        safeSetText(detailSectionLabel, section.getSectionNumber());
        safeSetText(detailTeacherLabel,
            section.getAssignedTeacher() != null ?
                section.getAssignedTeacher().getFirstName() + " " + section.getAssignedTeacher().getLastName() :
                "Not Assigned");
        safeSetText(detailRoomLabel,
            section.getAssignedRoom() != null ?
                section.getAssignedRoom().getRoomNumber() :
                "Not Assigned");
        safeSetText(detailPeriodLabel,
            section.getAssignedPeriod() != null ?
                "Period " + section.getAssignedPeriod() :
                "Not Assigned");
        safeSetText(detailEnrollmentLabel,
            String.format("%d / %d",
                section.getCurrentEnrollment() != null ? section.getCurrentEnrollment() : 0,
                section.getMaxEnrollment() != null ? section.getMaxEnrollment() : 30));
    }

    private void clearSelection() {
        selectedSection = null;

        if (sectionDetailsBox != null) {
            sectionDetailsBox.setVisible(false);
            sectionDetailsBox.setManaged(false);
        }

        if (noSelectionLabel != null) {
            noSelectionLabel.setVisible(true);
            noSelectionLabel.setManaged(true);
        }
    }

    private void saveChanges() {
        try {
            for (CourseSection section : modifiedSections.values()) {
                courseSectionRepository.save(section);
            }

            modifiedSections.clear();
            undoStack.clear();
            redoStack.clear();

            updateChangesStatus();
            updateUndoRedoButtons();

            showInfo(String.format("Successfully saved all changes"));
            refreshScheduleGrid();

        } catch (Exception e) {
            log.error("Failed to save changes", e);
            showError("Failed to save changes: " + e.getMessage());
        }
    }

    private void discardChanges() {
        modifiedSections.clear();
        undoStack.clear();
        redoStack.clear();

        updateChangesStatus();
        updateUndoRedoButtons();
        refreshScheduleGrid();

        showInfo("All changes discarded");
    }

    private void unassignSection(CourseSection section) {
        section.setAssignedTeacher(null);
        section.setAssignedRoom(null);
        section.setAssignedPeriod(null);

        modifiedSections.put(section.getId(), section);
        updateChangesStatus();
        refreshScheduleGrid();

        addToRecentChanges("Unassigned " + section.getCourse().getCourseName());
    }

    // ========================================================================
    // REFRESH & STATUS
    // ========================================================================

    private void refreshScheduleGrid() {
        buildScheduleGrid();
        buildUnassignedPool();
        updateStatistics();
        updateConflictsList();
    }

    private void updateGridInteractivity() {
        buildScheduleGrid();
    }

    private void updateChangesStatus() {
        int count = modifiedSections.size();
        if (count == 0) {
            safeSetText(changesStatusLabel, "No unsaved changes");
            changesStatusLabel.setStyle("-fx-text-fill: #388e3c;");
        } else {
            safeSetText(changesStatusLabel, count + " unsaved change(s)");
            changesStatusLabel.setStyle("-fx-text-fill: #f57c00; -fx-font-weight: bold;");
        }
    }

    private void updateUndoRedoButtons() {
        if (undoButton != null) {
            undoButton.setDisable(undoStack.isEmpty());
        }
        if (redoButton != null) {
            redoButton.setDisable(redoStack.isEmpty());
        }
    }

    private void updateConflictsList() {
        if (conflictsList == null) return;

        ObservableList<String> conflicts = FXCollections.observableArrayList();

        for (CourseSection section : cachedSections) {
            ConflictStatus status = determineConflictStatus(section);
            if (status == ConflictStatus.CONFLICT) {
                conflicts.add(String.format("CONFLICT: %s (P%d) - Teacher/Room double-booking",
                    section.getCourse().getCourseName(),
                    section.getAssignedPeriod() != null ? section.getAssignedPeriod() : 0));
            } else if (status == ConflictStatus.WARNING) {
                conflicts.add(String.format("WARNING: %s (P%d) - Near capacity (>=90%%)",
                    section.getCourse().getCourseName(),
                    section.getAssignedPeriod() != null ? section.getAssignedPeriod() : 0));
            }
        }

        conflictsList.setItems(conflicts);

        // Update status label
        long conflictOnly = cachedSections.stream()
            .filter(s -> determineConflictStatus(s) == ConflictStatus.CONFLICT)
            .count();

        if (conflictOnly == 0) {
            safeSetText(conflictsStatusLabel, "No conflicts detected");
            conflictsStatusLabel.setStyle("-fx-text-fill: #388e3c;");
        } else {
            safeSetText(conflictsStatusLabel, conflictOnly + " conflict(s) detected");
            conflictsStatusLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
        }
    }

    private void updateSuggestions() {
        if (suggestionsContainer == null) return;

        suggestionsContainer.getChildren().clear();

        List<String> suggestions = new ArrayList<>();
        suggestions.add("Drag sections to rearrange schedule");
        suggestions.add("Use Ctrl+Z to undo changes");
        suggestions.add("Remember to save your changes");

        for (String suggestion : suggestions) {
            Label label = new Label(suggestion);
            label.setWrapText(true);
            label.setStyle("-fx-font-size: 11px; -fx-padding: 3;");
            suggestionsContainer.getChildren().add(label);
        }
    }

    private void addToRecentChanges(String change) {
        if (recentChangesList == null) return;

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String entry = timestamp + " - " + change;

        recentChangesList.getItems().add(0, entry);

        if (recentChangesList.getItems().size() > 20) {
            recentChangesList.getItems().remove(20, recentChangesList.getItems().size());
        }
    }

    // ========================================================================
    // HELPERS
    // ========================================================================

    private Optional<CourseSection> findSectionInCell(Teacher teacher, int period) {
        return cachedSections.stream()
            .filter(s -> s.getAssignedTeacher() != null &&
                       s.getAssignedTeacher().getId().equals(teacher.getId()) &&
                       s.getAssignedPeriod() != null &&
                       s.getAssignedPeriod() == period)
            .findFirst();
    }

    private static String getSubjectColor(String courseName) {
        if (courseName == null) return "#6B7280";

        // Direct match
        String color = SUBJECT_COLORS.get(courseName);
        if (color != null) return color;

        // Partial match: check if course name contains any key
        String lowerName = courseName.toLowerCase();
        for (Map.Entry<String, String> entry : SUBJECT_COLORS.entrySet()) {
            if (lowerName.contains(entry.getKey().toLowerCase())) {
                return entry.getValue();
            }
        }

        // Default gray
        return "#6B7280";
    }

    private void safeSetText(Label label, String text) {
        if (label != null && text != null) {
            label.setText(text);
        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ========================================================================
    // INNER CLASSES
    // ========================================================================

    @Data
    private static class ScheduleChange {
        private final Long sectionId;
        private final String field;
        private final Object oldValue;
        private final Object newValue;
        private final String description;

        public ScheduleChange reverse() {
            return new ScheduleChange(sectionId, field, newValue, oldValue, "Undo: " + description);
        }
    }

    @Data
    private static class TeacherPeriod {
        private final Teacher teacher;
        private final Integer period;
    }
}
