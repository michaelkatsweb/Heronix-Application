package com.heronix.ui.gradebook;

import javafx.beans.property.*;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.function.BiConsumer;

/**
 * Grade Cell Component
 * An editable cell for entering and displaying grades in a gradebook.
 *
 * Features:
 * - Click-to-edit functionality
 * - Multiple grade formats (percentage, letter, points)
 * - Visual feedback for grade ranges
 * - Keyboard navigation (Tab, Enter, Arrow keys)
 * - Validation with error indication
 * - Missing/excused/late indicators
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class GradeCell extends StackPane {

    // ========================================================================
    // PROPERTIES
    // ========================================================================

    private final StringProperty gradeValue = new SimpleStringProperty("");
    private final DoubleProperty numericValue = new SimpleDoubleProperty(-1);
    private final DoubleProperty maxPoints = new SimpleDoubleProperty(100);
    private final ObjectProperty<GradeStatus> status = new SimpleObjectProperty<>(GradeStatus.NORMAL);
    private final ObjectProperty<GradeFormat> format = new SimpleObjectProperty<>(GradeFormat.PERCENTAGE);
    private final BooleanProperty editable = new SimpleBooleanProperty(true);
    private final BooleanProperty editing = new SimpleBooleanProperty(false);
    private final BooleanProperty modified = new SimpleBooleanProperty(false);

    @Getter @Setter
    private String studentId;

    @Getter @Setter
    private String assignmentId;

    // ========================================================================
    // COMPONENTS
    // ========================================================================

    private final Label displayLabel;
    private final TextField editField;
    private final Label statusIndicator;

    // ========================================================================
    // CALLBACKS
    // ========================================================================

    private BiConsumer<GradeCell, String> onGradeChanged;
    private Runnable onNavigateNext;
    private Runnable onNavigatePrevious;
    private Runnable onNavigateUp;
    private Runnable onNavigateDown;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public GradeCell() {
        this("");
    }

    public GradeCell(String initialValue) {
        setPrefSize(70, 36);
        setMinSize(70, 36);
        setMaxSize(Double.MAX_VALUE, 36);
        setAlignment(Pos.CENTER);

        // Display label
        displayLabel = new Label();
        displayLabel.setAlignment(Pos.CENTER);
        displayLabel.setMaxWidth(Double.MAX_VALUE);
        displayLabel.setMaxHeight(Double.MAX_VALUE);

        // Edit field
        editField = new TextField();
        editField.setAlignment(Pos.CENTER);
        editField.setVisible(false);
        editField.setManaged(false);
        editField.getStyleClass().add("grade-edit-field");

        // Status indicator (corner badge)
        statusIndicator = new Label();
        statusIndicator.setStyle("-fx-font-size: 8px;");
        statusIndicator.setVisible(false);
        StackPane.setAlignment(statusIndicator, Pos.TOP_RIGHT);

        getChildren().addAll(displayLabel, editField, statusIndicator);

        // Styling
        updateStyle();

        // Event handlers
        setupEventHandlers();

        // Listeners
        gradeValue.addListener((obs, oldVal, newVal) -> updateDisplay());
        status.addListener((obs, oldVal, newVal) -> updateStyle());
        editing.addListener((obs, wasEditing, isEditing) -> {
            if (isEditing) {
                startEditing();
            } else {
                stopEditing();
            }
        });

        // Set initial value
        if (initialValue != null && !initialValue.isEmpty()) {
            setGradeValue(initialValue);
        }
    }

    // ========================================================================
    // EVENT HANDLERS
    // ========================================================================

    private void setupEventHandlers() {
        // Click to edit
        setOnMouseClicked(e -> {
            if (editable.get() && e.getClickCount() == 1) {
                startEditing();
            }
        });

        // Edit field key handling
        editField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                commitEdit();
                if (onNavigateDown != null) onNavigateDown.run();
            } else if (e.getCode() == KeyCode.TAB) {
                commitEdit();
                if (e.isShiftDown()) {
                    if (onNavigatePrevious != null) onNavigatePrevious.run();
                } else {
                    if (onNavigateNext != null) onNavigateNext.run();
                }
                e.consume();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            } else if (e.getCode() == KeyCode.UP) {
                commitEdit();
                if (onNavigateUp != null) onNavigateUp.run();
            } else if (e.getCode() == KeyCode.DOWN) {
                commitEdit();
                if (onNavigateDown != null) onNavigateDown.run();
            }
        });

        // Focus lost
        editField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused && editing.get()) {
                commitEdit();
            }
        });
    }

    // ========================================================================
    // EDITING
    // ========================================================================

    public void startEditing() {
        if (!editable.get() || editing.get()) return;

        editing.set(true);
        editField.setText(gradeValue.get());
        displayLabel.setVisible(false);
        editField.setVisible(true);
        editField.setManaged(true);
        editField.requestFocus();
        editField.selectAll();

        setStyle(getStyle() + "-fx-border-color: #2563EB; -fx-border-width: 2;");
    }

    public void stopEditing() {
        editing.set(false);
        editField.setVisible(false);
        editField.setManaged(false);
        displayLabel.setVisible(true);
        updateStyle();
    }

    public void commitEdit() {
        if (!editing.get()) return;

        String newValue = editField.getText().trim();

        // Handle special inputs
        if (newValue.equalsIgnoreCase("M") || newValue.equalsIgnoreCase("missing")) {
            setStatus(GradeStatus.MISSING);
            newValue = "";
        } else if (newValue.equalsIgnoreCase("E") || newValue.equalsIgnoreCase("excused")) {
            setStatus(GradeStatus.EXCUSED);
            newValue = "E";
        } else if (newValue.equalsIgnoreCase("L") || newValue.equalsIgnoreCase("late")) {
            setStatus(GradeStatus.LATE);
        } else if (newValue.isEmpty()) {
            setStatus(GradeStatus.MISSING);
        } else {
            // Validate numeric input
            if (validateInput(newValue)) {
                setStatus(GradeStatus.NORMAL);
            } else {
                // Invalid input - show error briefly
                setStyle(getStyle() + "-fx-background-color: #FEE2E2;");
                return;
            }
        }

        String oldValue = gradeValue.get();
        gradeValue.set(newValue);

        if (!newValue.equals(oldValue)) {
            modified.set(true);
            if (onGradeChanged != null) {
                onGradeChanged.accept(this, newValue);
            }
        }

        stopEditing();
    }

    public void cancelEdit() {
        stopEditing();
    }

    // ========================================================================
    // VALIDATION
    // ========================================================================

    private boolean validateInput(String input) {
        if (input == null || input.isEmpty()) return true;

        // Try to parse as number
        try {
            // Remove % sign if present
            String cleanInput = input.replace("%", "").trim();

            // Check for letter grades
            if (cleanInput.matches("[A-Fa-f][+-]?")) {
                return true;
            }

            // Parse numeric
            double value = Double.parseDouble(cleanInput);
            numericValue.set(value);

            // Validate range (allow up to 150% for extra credit)
            return value >= 0 && value <= maxPoints.get() * 1.5;

        } catch (NumberFormatException e) {
            return false;
        }
    }

    // ========================================================================
    // DISPLAY
    // ========================================================================

    private void updateDisplay() {
        String value = gradeValue.get();

        if (value == null || value.isEmpty()) {
            if (status.get() == GradeStatus.MISSING) {
                displayLabel.setText("—");
                displayLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13px;");
            } else if (status.get() == GradeStatus.EXCUSED) {
                displayLabel.setText("EX");
                displayLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px; -fx-font-weight: 600;");
            } else {
                displayLabel.setText("");
            }
        } else {
            // Format the display value
            String displayText = formatGrade(value);
            displayLabel.setText(displayText);

            // Color based on grade
            String textColor = getGradeColor(value);
            displayLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 13px; -fx-font-weight: 500;");
        }

        // Update status indicator
        updateStatusIndicator();
    }

    private String formatGrade(String value) {
        try {
            double numeric = Double.parseDouble(value.replace("%", ""));

            return switch (format.get()) {
                case PERCENTAGE -> String.format("%.0f%%", numeric);
                case POINTS -> String.format("%.1f", numeric);
                case LETTER -> convertToLetter(numeric);
            };
        } catch (NumberFormatException e) {
            return value; // Return as-is for letter grades
        }
    }

    private String convertToLetter(double percentage) {
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

    private String getGradeColor(String value) {
        try {
            double numeric = Double.parseDouble(value.replace("%", ""));
            if (numeric >= 90) return "#10B981"; // Green
            if (numeric >= 80) return "#3B82F6"; // Blue
            if (numeric >= 70) return "#F59E0B"; // Yellow
            if (numeric >= 60) return "#F97316"; // Orange
            return "#EF4444"; // Red
        } catch (NumberFormatException e) {
            return "#0F172A"; // Default
        }
    }

    private void updateStatusIndicator() {
        GradeStatus s = status.get();
        switch (s) {
            case LATE:
                statusIndicator.setText("L");
                statusIndicator.setStyle("-fx-font-size: 8px; -fx-text-fill: #F59E0B; -fx-font-weight: bold;");
                statusIndicator.setVisible(true);
                break;
            case EXCUSED:
                statusIndicator.setText("E");
                statusIndicator.setStyle("-fx-font-size: 8px; -fx-text-fill: #64748B; -fx-font-weight: bold;");
                statusIndicator.setVisible(true);
                break;
            case MISSING:
                statusIndicator.setText("!");
                statusIndicator.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444; -fx-font-weight: bold;");
                statusIndicator.setVisible(true);
                break;
            default:
                statusIndicator.setVisible(false);
        }
    }

    private void updateStyle() {
        String bgColor = switch (status.get()) {
            case MISSING -> "#FEF2F2";
            case EXCUSED -> "#F8FAFC";
            case LATE -> "#FFFBEB";
            default -> "white";
        };

        String borderColor = modified.get() ? "#2563EB" : "#E2E8F0";

        setStyle(String.format("""
            -fx-background-color: %s;
            -fx-border-color: %s;
            -fx-border-width: 1;
            -fx-cursor: hand;
            """, bgColor, borderColor));
    }

    // ========================================================================
    // PROPERTY ACCESSORS
    // ========================================================================

    public String getGradeValue() { return gradeValue.get(); }
    public void setGradeValue(String value) { gradeValue.set(value); updateDisplay(); }
    public StringProperty gradeValueProperty() { return gradeValue; }

    public double getNumericValue() { return numericValue.get(); }
    public DoubleProperty numericValueProperty() { return numericValue; }

    public double getMaxPoints() { return maxPoints.get(); }
    public void setMaxPoints(double value) { maxPoints.set(value); }
    public DoubleProperty maxPointsProperty() { return maxPoints; }

    public GradeStatus getStatus() { return status.get(); }
    public void setStatus(GradeStatus value) { status.set(value); updateDisplay(); }
    public ObjectProperty<GradeStatus> statusProperty() { return status; }

    public GradeFormat getFormat() { return format.get(); }
    public void setFormat(GradeFormat value) { format.set(value); updateDisplay(); }
    public ObjectProperty<GradeFormat> formatProperty() { return format; }

    public boolean isEditable() { return editable.get(); }
    public void setEditable(boolean value) { editable.set(value); }
    public BooleanProperty editableProperty() { return editable; }

    public boolean isModified() { return modified.get(); }
    public void setModified(boolean value) { modified.set(value); updateStyle(); }
    public BooleanProperty modifiedProperty() { return modified; }

    public void setOnGradeChanged(BiConsumer<GradeCell, String> callback) { this.onGradeChanged = callback; }
    public void setOnNavigateNext(Runnable callback) { this.onNavigateNext = callback; }
    public void setOnNavigatePrevious(Runnable callback) { this.onNavigatePrevious = callback; }
    public void setOnNavigateUp(Runnable callback) { this.onNavigateUp = callback; }
    public void setOnNavigateDown(Runnable callback) { this.onNavigateDown = callback; }

    // ========================================================================
    // ENUMS
    // ========================================================================

    public enum GradeStatus {
        NORMAL, MISSING, EXCUSED, LATE
    }

    public enum GradeFormat {
        PERCENTAGE, POINTS, LETTER
    }
}
