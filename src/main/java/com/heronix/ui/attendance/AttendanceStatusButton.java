package com.heronix.ui.attendance;

import javafx.beans.property.*;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 * Attendance Status Button Component
 * A compact, clickable button for quickly marking attendance status.
 *
 * Features:
 * - Single-click status cycling
 * - Color-coded status indicators
 * - Keyboard support
 * - Tooltip with details
 * - Compact and expanded modes
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class AttendanceStatusButton extends StackPane {

    // ========================================================================
    // PROPERTIES
    // ========================================================================

    private final ObjectProperty<AttendanceStatus> status = new SimpleObjectProperty<>(AttendanceStatus.UNMARKED);
    private final BooleanProperty compact = new SimpleBooleanProperty(true);
    private final BooleanProperty editable = new SimpleBooleanProperty(true);

    @Getter
    private String studentId;

    @Getter
    private String periodId;

    // ========================================================================
    // COMPONENTS
    // ========================================================================

    private final Label statusLabel;
    private final Tooltip tooltip;

    // ========================================================================
    // CALLBACKS
    // ========================================================================

    private Consumer<AttendanceStatus> onStatusChanged;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public AttendanceStatusButton() {
        this(AttendanceStatus.UNMARKED);
    }

    public AttendanceStatusButton(AttendanceStatus initialStatus) {
        setPrefSize(36, 36);
        setMinSize(36, 36);
        setAlignment(Pos.CENTER);
        setCursor(javafx.scene.Cursor.HAND);

        statusLabel = new Label();
        statusLabel.setAlignment(Pos.CENTER);

        tooltip = new Tooltip();
        Tooltip.install(this, tooltip);

        getChildren().add(statusLabel);

        // Set initial status
        status.set(initialStatus);
        updateDisplay();

        // Listeners
        status.addListener((obs, oldVal, newVal) -> {
            updateDisplay();
            if (onStatusChanged != null) {
                onStatusChanged.accept(newVal);
            }
        });

        compact.addListener((obs, wasCompact, isCompact) -> updateDisplay());

        // Click handler - cycle through statuses
        setOnMouseClicked(e -> {
            if (editable.get()) {
                cycleStatus();
            }
        });

        // Keyboard support
        setFocusTraversable(true);
        setOnKeyPressed(e -> {
            if (!editable.get()) return;

            switch (e.getCode()) {
                case P -> setStatus(AttendanceStatus.PRESENT);
                case A -> setStatus(AttendanceStatus.ABSENT);
                case T -> setStatus(AttendanceStatus.TARDY);
                case E -> setStatus(AttendanceStatus.EXCUSED);
                case SPACE, ENTER -> cycleStatus();
            }
        });
    }

    // ========================================================================
    // STATUS CYCLING
    // ========================================================================

    public void cycleStatus() {
        AttendanceStatus current = status.get();
        AttendanceStatus next = switch (current) {
            case UNMARKED -> AttendanceStatus.PRESENT;
            case PRESENT -> AttendanceStatus.ABSENT;
            case ABSENT -> AttendanceStatus.TARDY;
            case TARDY -> AttendanceStatus.EXCUSED;
            case EXCUSED -> AttendanceStatus.PRESENT;
        };
        status.set(next);
    }

    // ========================================================================
    // DISPLAY
    // ========================================================================

    private void updateDisplay() {
        AttendanceStatus s = status.get();
        boolean isCompact = compact.get();

        // Text
        statusLabel.setText(isCompact ? s.getShortCode() : s.getDisplayName());

        // Styling
        String bgColor = s.getBackgroundColor();
        String textColor = s.getTextColor();
        String borderColor = s.getBorderColor();

        double size = isCompact ? 36 : 80;
        double fontSize = isCompact ? 12 : 11;
        double radius = isCompact ? 6 : 4;

        setPrefSize(size, 36);
        setMinSize(size, 36);

        setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: %f;
            -fx-border-color: %s;
            -fx-border-radius: %f;
            -fx-border-width: 1;
            """, bgColor, radius, borderColor, radius));

        statusLabel.setStyle(String.format("""
            -fx-text-fill: %s;
            -fx-font-size: %fpx;
            -fx-font-weight: 600;
            """, textColor, fontSize));

        // Tooltip
        tooltip.setText(s.getDisplayName() + "\nClick to change, or press:\nP=Present, A=Absent, T=Tardy, E=Excused");

        // Hover effects
        setOnMouseEntered(e -> {
            if (editable.get()) {
                setStyle(String.format("""
                    -fx-background-color: %s;
                    -fx-background-radius: %f;
                    -fx-border-color: %s;
                    -fx-border-radius: %f;
                    -fx-border-width: 2;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 1);
                    """, bgColor, radius, textColor, radius));
            }
        });

        setOnMouseExited(e -> {
            setStyle(String.format("""
                -fx-background-color: %s;
                -fx-background-radius: %f;
                -fx-border-color: %s;
                -fx-border-radius: %f;
                -fx-border-width: 1;
                """, bgColor, radius, borderColor, radius));
        });
    }

    // ========================================================================
    // PROPERTY ACCESSORS
    // ========================================================================

    public AttendanceStatus getStatus() { return status.get(); }
    public void setStatus(AttendanceStatus value) { status.set(value); }
    public ObjectProperty<AttendanceStatus> statusProperty() { return status; }

    public boolean isCompact() { return compact.get(); }
    public void setCompact(boolean value) { compact.set(value); }
    public BooleanProperty compactProperty() { return compact; }

    public boolean isEditable() { return editable.get(); }
    public void setEditable(boolean value) { editable.set(value); }
    public BooleanProperty editableProperty() { return editable; }

    public void setStudentId(String id) { this.studentId = id; }
    public void setPeriodId(String id) { this.periodId = id; }

    public void setOnStatusChanged(Consumer<AttendanceStatus> callback) {
        this.onStatusChanged = callback;
    }

    // ========================================================================
    // FACTORY METHODS
    // ========================================================================

    public static HBox createStatusLegend() {
        HBox legend = new HBox(12);
        legend.setAlignment(Pos.CENTER_LEFT);

        for (AttendanceStatus status : AttendanceStatus.values()) {
            if (status == AttendanceStatus.UNMARKED) continue;

            HBox item = new HBox(4);
            item.setAlignment(Pos.CENTER_LEFT);

            Label colorBox = new Label(status.getShortCode());
            colorBox.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-font-size: 10px;
                -fx-font-weight: 600;
                -fx-padding: 2 6;
                -fx-background-radius: 4;
                """, status.getBackgroundColor(), status.getTextColor()));

            Label nameLabel = new Label(status.getDisplayName());
            nameLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

            item.getChildren().addAll(colorBox, nameLabel);
            legend.getChildren().add(item);
        }

        return legend;
    }

    // ========================================================================
    // ATTENDANCE STATUS ENUM
    // ========================================================================

    @Getter
    public enum AttendanceStatus {
        UNMARKED("?", "Unmarked", "#F8FAFC", "#94A3B8", "#E2E8F0"),
        PRESENT("P", "Present", "#ECFDF5", "#059669", "#A7F3D0"),
        ABSENT("A", "Absent", "#FEF2F2", "#DC2626", "#FECACA"),
        TARDY("T", "Tardy", "#FFFBEB", "#D97706", "#FDE68A"),
        EXCUSED("E", "Excused", "#EFF6FF", "#2563EB", "#BFDBFE");

        private final String shortCode;
        private final String displayName;
        private final String backgroundColor;
        private final String textColor;
        private final String borderColor;

        AttendanceStatus(String shortCode, String displayName, String bgColor, String textColor, String borderColor) {
            this.shortCode = shortCode;
            this.displayName = displayName;
            this.backgroundColor = bgColor;
            this.textColor = textColor;
            this.borderColor = borderColor;
        }
    }
}
