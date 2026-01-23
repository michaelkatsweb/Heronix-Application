package com.heronix.ui.schedule;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

/**
 * Schedule Grid View
 * A visual grid for displaying and editing schedules (student, teacher, or room).
 *
 * Features:
 * - Week view with period/time slots
 * - Color-coded course blocks
 * - Drag-and-drop rescheduling
 * - Conflict detection
 * - Print-friendly layout
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class ScheduleGridView extends VBox {

    // ========================================================================
    // CONFIGURATION
    // ========================================================================

    private static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a");

    // ========================================================================
    // DATA
    // ========================================================================

    private final ObservableList<Period> periods = FXCollections.observableArrayList();
    private final ObservableList<ScheduleBlock> blocks = FXCollections.observableArrayList();
    private final Map<String, String> courseColors = new HashMap<>();

    @Getter @Setter
    private String scheduleOwnerId;

    @Getter @Setter
    private String scheduleOwnerName;

    @Getter @Setter
    private ScheduleType scheduleType = ScheduleType.STUDENT;

    // ========================================================================
    // COMPONENTS
    // ========================================================================

    private GridPane scheduleGrid;
    private Label titleLabel;

    // ========================================================================
    // CALLBACKS
    // ========================================================================

    private Consumer<ScheduleBlock> onBlockClick;
    private Consumer<ScheduleBlock> onBlockDoubleClick;
    private Consumer<BlockMove> onBlockMoved;

    // ========================================================================
    // DRAG AND DROP
    // ========================================================================

    private static final DataFormat BLOCK_FORMAT = new DataFormat("application/x-schedule-block");
    private ScheduleBlock draggedBlock;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public ScheduleGridView() {
        setSpacing(16);
        setPadding(new Insets(16));
        setStyle("-fx-background-color: white;");

        // Title
        titleLabel = new Label("Schedule");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");

        // Schedule grid
        scheduleGrid = new GridPane();
        scheduleGrid.setHgap(1);
        scheduleGrid.setVgap(1);
        scheduleGrid.setStyle("-fx-background-color: #E2E8F0;");
        VBox.setVgrow(scheduleGrid, Priority.ALWAYS);

        getChildren().addAll(titleLabel, scheduleGrid);

        // Initialize default periods
        initializeDefaultPeriods();
        initializeCourseColors();
    }

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    private void initializeDefaultPeriods() {
        periods.addAll(
            new Period("P1", "Period 1", LocalTime.of(8, 0), LocalTime.of(8, 50)),
            new Period("P2", "Period 2", LocalTime.of(9, 0), LocalTime.of(9, 50)),
            new Period("P3", "Period 3", LocalTime.of(10, 0), LocalTime.of(10, 50)),
            new Period("P4", "Period 4", LocalTime.of(11, 0), LocalTime.of(11, 50)),
            new Period("LN", "Lunch", LocalTime.of(12, 0), LocalTime.of(12, 45)),
            new Period("P5", "Period 5", LocalTime.of(13, 0), LocalTime.of(13, 50)),
            new Period("P6", "Period 6", LocalTime.of(14, 0), LocalTime.of(14, 50)),
            new Period("P7", "Period 7", LocalTime.of(15, 0), LocalTime.of(15, 50))
        );
    }

    private void initializeCourseColors() {
        // Default color palette for courses
        String[] colors = {"#DBEAFE", "#FCE7F3", "#D1FAE5", "#FEF3C7", "#E0E7FF",
                          "#FECACA", "#CFFAFE", "#DDD6FE", "#FED7AA", "#BBF7D0"};
        int colorIndex = 0;

        for (ScheduleBlock block : blocks) {
            if (!courseColors.containsKey(block.getCourseId())) {
                courseColors.put(block.getCourseId(), colors[colorIndex % colors.length]);
                colorIndex++;
            }
        }
    }

    // ========================================================================
    // GRID BUILDING
    // ========================================================================

    public void buildGrid() {
        scheduleGrid.getChildren().clear();
        scheduleGrid.getColumnConstraints().clear();
        scheduleGrid.getRowConstraints().clear();

        // Column constraints: time column + 5 day columns
        ColumnConstraints timeCol = new ColumnConstraints(80);
        timeCol.setMinWidth(80);
        scheduleGrid.getColumnConstraints().add(timeCol);

        for (int i = 0; i < DAYS.length; i++) {
            ColumnConstraints dayCol = new ColumnConstraints();
            dayCol.setHgrow(Priority.ALWAYS);
            dayCol.setMinWidth(120);
            dayCol.setPrefWidth(150);
            scheduleGrid.getColumnConstraints().add(dayCol);
        }

        // Row constraints: header + periods
        RowConstraints headerRow = new RowConstraints(40);
        scheduleGrid.getRowConstraints().add(headerRow);

        for (Period period : periods) {
            RowConstraints periodRow = new RowConstraints(60);
            periodRow.setMinHeight(50);
            scheduleGrid.getRowConstraints().add(periodRow);
        }

        // Build header row
        buildHeaderRow();

        // Build period rows
        for (int p = 0; p < periods.size(); p++) {
            buildPeriodRow(periods.get(p), p + 1);
        }
    }

    private void buildHeaderRow() {
        // Empty corner cell
        Label cornerCell = createHeaderCell("");
        scheduleGrid.add(cornerCell, 0, 0);

        // Day headers
        for (int d = 0; d < DAYS.length; d++) {
            Label dayCell = createHeaderCell(DAYS[d]);
            scheduleGrid.add(dayCell, d + 1, 0);
        }
    }

    private void buildPeriodRow(Period period, int rowIndex) {
        // Time cell
        VBox timeCell = new VBox(2);
        timeCell.setAlignment(Pos.CENTER);
        timeCell.setPadding(new Insets(4));
        timeCell.setStyle("-fx-background-color: #F8FAFC;");

        Label periodLabel = new Label(period.getName());
        periodLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #334155;");

        Label timeLabel = new Label(period.getStartTime().format(TIME_FORMAT));
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #94A3B8;");

        timeCell.getChildren().addAll(periodLabel, timeLabel);
        scheduleGrid.add(timeCell, 0, rowIndex);

        // Day cells
        for (int d = 0; d < DAYS.length; d++) {
            DayOfWeek day = DayOfWeek.of(d + 1);
            StackPane dayCell = createDayCell(period, day);
            scheduleGrid.add(dayCell, d + 1, rowIndex);
        }
    }

    private Label createHeaderCell(String text) {
        Label cell = new Label(text);
        cell.setAlignment(Pos.CENTER);
        cell.setMaxWidth(Double.MAX_VALUE);
        cell.setMaxHeight(Double.MAX_VALUE);
        cell.setStyle("""
            -fx-background-color: #F1F5F9;
            -fx-font-size: 13px;
            -fx-font-weight: 600;
            -fx-text-fill: #334155;
            """);
        return cell;
    }

    private StackPane createDayCell(Period period, DayOfWeek day) {
        StackPane cell = new StackPane();
        cell.setStyle("-fx-background-color: white;");
        cell.setMinHeight(50);

        // Find block for this cell
        ScheduleBlock block = findBlock(period.getId(), day);

        if (block != null) {
            VBox blockContent = createBlockContent(block);
            cell.getChildren().add(blockContent);
        }

        // Drop target handling
        setupDropTarget(cell, period, day);

        return cell;
    }

    private VBox createBlockContent(ScheduleBlock block) {
        VBox content = new VBox(2);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(6));
        content.setMaxWidth(Double.MAX_VALUE);
        content.setMaxHeight(Double.MAX_VALUE);
        content.setCursor(javafx.scene.Cursor.HAND);

        String bgColor = courseColors.getOrDefault(block.getCourseId(), "#E2E8F0");

        content.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 4;
            -fx-border-radius: 4;
            """, bgColor));

        Label courseLabel = new Label(block.getCourseName());
        courseLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");
        courseLabel.setWrapText(true);
        courseLabel.setAlignment(Pos.CENTER);

        Label teacherLabel = new Label(block.getTeacherName());
        teacherLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748B;");

        Label roomLabel = new Label(block.getRoomNumber());
        roomLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #94A3B8;");

        content.getChildren().addAll(courseLabel, teacherLabel, roomLabel);

        // Click handlers
        content.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1 && onBlockClick != null) {
                onBlockClick.accept(block);
            } else if (e.getClickCount() == 2 && onBlockDoubleClick != null) {
                onBlockDoubleClick.accept(block);
            }
        });

        // Drag handling
        content.setOnDragDetected(e -> {
            draggedBlock = block;
            Dragboard db = content.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent clipContent = new ClipboardContent();
            clipContent.put(BLOCK_FORMAT, block.getId());
            db.setContent(clipContent);
            content.setOpacity(0.5);
            e.consume();
        });

        content.setOnDragDone(e -> {
            content.setOpacity(1.0);
            draggedBlock = null;
            e.consume();
        });

        // Hover effects
        content.setOnMouseEntered(e -> {
            content.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-background-radius: 4;
                -fx-border-color: #2563EB;
                -fx-border-radius: 4;
                -fx-border-width: 2;
                """, bgColor));
        });

        content.setOnMouseExited(e -> {
            content.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-background-radius: 4;
                -fx-border-radius: 4;
                """, bgColor));
        });

        return content;
    }

    private void setupDropTarget(StackPane cell, Period period, DayOfWeek day) {
        cell.setOnDragOver(e -> {
            if (e.getGestureSource() != cell && e.getDragboard().hasContent(BLOCK_FORMAT)) {
                e.acceptTransferModes(TransferMode.MOVE);
            }
            e.consume();
        });

        cell.setOnDragEntered(e -> {
            if (e.getDragboard().hasContent(BLOCK_FORMAT)) {
                cell.setStyle("-fx-background-color: #DBEAFE;");
            }
            e.consume();
        });

        cell.setOnDragExited(e -> {
            cell.setStyle("-fx-background-color: white;");
            e.consume();
        });

        cell.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean success = false;

            if (db.hasContent(BLOCK_FORMAT) && draggedBlock != null) {
                // Check for conflicts
                ScheduleBlock existingBlock = findBlock(period.getId(), day);
                if (existingBlock == null) {
                    // Move the block
                    String oldPeriod = draggedBlock.getPeriodId();
                    DayOfWeek oldDay = draggedBlock.getDay();

                    draggedBlock.setPeriodId(period.getId());
                    draggedBlock.setDay(day);

                    buildGrid();
                    success = true;

                    if (onBlockMoved != null) {
                        onBlockMoved.accept(new BlockMove(draggedBlock, oldPeriod, oldDay, period.getId(), day));
                    }
                } else {
                    // Show conflict warning
                    showConflictWarning(existingBlock);
                }
            }

            e.setDropCompleted(success);
            e.consume();
        });
    }

    private ScheduleBlock findBlock(String periodId, DayOfWeek day) {
        return blocks.stream()
            .filter(b -> b.getPeriodId().equals(periodId) && b.getDay() == day)
            .findFirst()
            .orElse(null);
    }

    private void showConflictWarning(ScheduleBlock conflictingBlock) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Schedule Conflict");
        alert.setHeaderText("This time slot is already occupied");
        alert.setContentText("Conflicting course: " + conflictingBlock.getCourseName());
        alert.showAndWait();
    }

    // ========================================================================
    // PUBLIC API
    // ========================================================================

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setBlocks(List<ScheduleBlock> scheduleBlocks) {
        blocks.setAll(scheduleBlocks);

        // Update course colors
        for (ScheduleBlock block : blocks) {
            if (!courseColors.containsKey(block.getCourseId())) {
                String[] colors = {"#DBEAFE", "#FCE7F3", "#D1FAE5", "#FEF3C7", "#E0E7FF"};
                courseColors.put(block.getCourseId(), colors[courseColors.size() % colors.length]);
            }
        }

        buildGrid();
    }

    public void addBlock(ScheduleBlock block) {
        blocks.add(block);
        buildGrid();
    }

    public void removeBlock(String blockId) {
        blocks.removeIf(b -> b.getId().equals(blockId));
        buildGrid();
    }

    public List<ScheduleBlock> getBlocks() {
        return new ArrayList<>(blocks);
    }

    public void setPeriods(List<Period> periodList) {
        periods.setAll(periodList);
        buildGrid();
    }

    public void setOnBlockClick(Consumer<ScheduleBlock> callback) {
        this.onBlockClick = callback;
    }

    public void setOnBlockDoubleClick(Consumer<ScheduleBlock> callback) {
        this.onBlockDoubleClick = callback;
    }

    public void setOnBlockMoved(Consumer<BlockMove> callback) {
        this.onBlockMoved = callback;
    }

    public void setCourseColor(String courseId, String color) {
        courseColors.put(courseId, color);
        buildGrid();
    }

    // ========================================================================
    // INNER CLASSES
    // ========================================================================

    @Getter @Setter
    public static class Period {
        private String id;
        private String name;
        private LocalTime startTime;
        private LocalTime endTime;

        public Period(String id, String name, LocalTime startTime, LocalTime endTime) {
            this.id = id;
            this.name = name;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    @Getter @Setter
    public static class ScheduleBlock {
        private String id;
        private String courseId;
        private String courseName;
        private String teacherId;
        private String teacherName;
        private String roomNumber;
        private String periodId;
        private DayOfWeek day;
        private String sectionId;

        public ScheduleBlock(String id, String courseId, String courseName, String periodId, DayOfWeek day) {
            this.id = id;
            this.courseId = courseId;
            this.courseName = courseName;
            this.periodId = periodId;
            this.day = day;
        }
    }

    @Getter
    public static class BlockMove {
        private final ScheduleBlock block;
        private final String fromPeriod;
        private final DayOfWeek fromDay;
        private final String toPeriod;
        private final DayOfWeek toDay;

        public BlockMove(ScheduleBlock block, String fromPeriod, DayOfWeek fromDay, String toPeriod, DayOfWeek toDay) {
            this.block = block;
            this.fromPeriod = fromPeriod;
            this.fromDay = fromDay;
            this.toPeriod = toPeriod;
            this.toDay = toDay;
        }
    }

    public enum ScheduleType {
        STUDENT, TEACHER, ROOM, MASTER
    }
}
