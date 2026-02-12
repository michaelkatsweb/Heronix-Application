package com.heronix.ui.component;

import com.heronix.model.domain.*;
import com.heronix.repository.AttendanceRecordRepository;
import com.heronix.repository.BehaviorIncidentRepository;
import com.heronix.repository.StudentGradeRepository;
import com.heronix.service.StudentService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Reusable Student Card popup — ID-card style dialog with tabbed detail views.
 * Not a Spring bean; call via {@code StudentCardPopup.show(ctx, studentId, owner)}.
 */
@Slf4j
public final class StudentCardPopup {

    private StudentCardPopup() { }

    public static void show(ApplicationContext ctx, Long studentId, Window owner) {
        StudentService studentService = ctx.getBean(StudentService.class);

        Optional<Student> opt = studentService.findByIdWithEnrolledCourses(studentId);
        if (opt.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Student not found (ID: " + studentId + ")");
            alert.initOwner(owner);
            alert.showAndWait();
            return;
        }
        Student student = opt.get();

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Student Record \u2014 " + student.getFullName());
        dialog.setResizable(true);
        dialog.initOwner(owner);

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().add(ButtonType.CLOSE);
        pane.setPrefSize(720, 680);

        VBox root = new VBox(0);

        // ── Header (ID-card style) ──────────────────────────────────────
        HBox header = buildHeader(student);

        // ── Tabs ────────────────────────────────────────────────────────
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        tabPane.getTabs().addAll(
            buildOverviewTab(student),
            buildGradesTab(ctx, student),
            buildDisciplineTab(ctx, student),
            buildAttendanceTab(ctx, student),
            buildCoursesTab(student)
        );

        root.getChildren().addAll(header, tabPane);
        pane.setContent(root);

        dialog.showAndWait();
    }

    // ====================================================================
    // HEADER
    // ====================================================================

    private static HBox buildHeader(Student student) {
        HBox header = new HBox(16);
        header.setPadding(new Insets(20, 24, 20, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: linear-gradient(to right, #1E3A5F, #2563EB);");

        // Photo
        ImageView photo = new ImageView();
        photo.setFitWidth(80);
        photo.setFitHeight(100);
        photo.setPreserveRatio(false);
        photo.setSmooth(true);
        Rectangle clip = new Rectangle(80, 100);
        clip.setArcWidth(8);
        clip.setArcHeight(8);
        photo.setClip(clip);

        boolean photoLoaded = false;
        if (student.getPhotoData() != null && student.getPhotoData().length > 0) {
            try {
                photo.setImage(new Image(new ByteArrayInputStream(student.getPhotoData())));
                photoLoaded = true;
            } catch (Exception e) {
                log.debug("Could not load photo from photoData", e);
            }
        }
        if (!photoLoaded && student.getPhotoPath() != null) {
            try {
                File f = new File(student.getPhotoPath());
                if (f.exists()) {
                    photo.setImage(new Image(new FileInputStream(f)));
                    photoLoaded = true;
                }
            } catch (Exception e) {
                log.debug("Could not load photo from photoPath", e);
            }
        }
        if (!photoLoaded) {
            // Placeholder – gray box with initials
            StackPane placeholder = new StackPane();
            placeholder.setPrefSize(80, 100);
            placeholder.setMinSize(80, 100);
            placeholder.setMaxSize(80, 100);
            placeholder.setStyle("-fx-background-color: #94A3B8; -fx-background-radius: 4;");
            String initials = "";
            if (student.getFirstName() != null && !student.getFirstName().isEmpty())
                initials += student.getFirstName().charAt(0);
            if (student.getLastName() != null && !student.getLastName().isEmpty())
                initials += student.getLastName().charAt(0);
            Label initialsLabel = new Label(initials.toUpperCase());
            initialsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
            placeholder.getChildren().add(initialsLabel);
            header.getChildren().add(placeholder);
        } else {
            header.getChildren().add(photo);
        }

        // Right side – name & info
        VBox info = new VBox(4);
        Label nameLabel = new Label(student.getFullName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        nameLabel.setTextFill(Color.WHITE);

        Label idLabel = new Label("ID: " + (student.getStudentId() != null ? student.getStudentId() : "N/A"));
        idLabel.setStyle("-fx-text-fill: #CBD5E1; -fx-font-size: 13px;");

        Label gradeLabel = new Label("Grade " + (student.getGradeLevel() != null ? student.getGradeLevel() : "N/A"));
        gradeLabel.setStyle("-fx-text-fill: #CBD5E1; -fx-font-size: 13px;");

        HBox badgeRow = new HBox(8);
        badgeRow.setAlignment(Pos.CENTER_LEFT);
        String standing = student.getAcademicStanding() != null ? student.getAcademicStanding() : "Good Standing";
        Label standingBadge = new Label(standing);
        String badgeColor = switch (standing) {
            case "Academic Probation" -> "#EF4444";
            case "Academic Warning" -> "#F59E0B";
            default -> "#10B981";
        };
        standingBadge.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 600; -fx-padding: 2 10; -fx-background-radius: 10;",
            badgeColor));
        badgeRow.getChildren().add(standingBadge);

        info.getChildren().addAll(nameLabel, idLabel, gradeLabel, badgeRow);
        HBox.setHgrow(info, Priority.ALWAYS);
        header.getChildren().add(info);

        return header;
    }

    // ====================================================================
    // TAB 1: OVERVIEW
    // ====================================================================

    private static Tab buildOverviewTab(Student student) {
        Tab tab = new Tab("Overview");

        VBox content = new VBox(16);
        content.setPadding(new Insets(16));

        // Academic summary cards row
        HBox cards = new HBox(12);
        cards.getChildren().addAll(
            summaryCard("GPA", student.getCurrentGPA() != null ? String.format("%.2f", student.getCurrentGPA()) : "N/A", "#3B82F6"),
            summaryCard("Weighted GPA", student.getWeightedGPA() != null ? String.format("%.2f", student.getWeightedGPA()) : "N/A", "#8B5CF6"),
            summaryCard("Credits", student.getCreditsEarned() != null ? String.format("%.1f", student.getCreditsEarned()) : "N/A", "#10B981")
        );

        // Contact info
        VBox contactSection = new VBox(8);
        contactSection.setPadding(new Insets(12));
        contactSection.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8; -fx-border-color: #E2E8F0; -fx-border-radius: 8;");
        Label contactTitle = new Label("Contact Information");
        contactTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");

        GridPane contactGrid = new GridPane();
        contactGrid.setHgap(16);
        contactGrid.setVgap(6);
        addDetailRow(contactGrid, 0, "Email", student.getEmail() != null ? student.getEmail() : "N/A");
        addDetailRow(contactGrid, 1, "Phone", student.getCellPhone() != null ? student.getCellPhone() : "N/A");
        addDetailRow(contactGrid, 2, "Class Rank", student.getClassRank() != null ? "#" + student.getClassRank() : "N/A");
        addDetailRow(contactGrid, 3, "Graduation Year", student.getGraduationYear() != null ? student.getGraduationYear().toString() : "N/A");

        contactSection.getChildren().addAll(contactTitle, contactGrid);

        // Special program flags
        HBox flags = new HBox(8);
        flags.setPadding(new Insets(4, 0, 0, 0));
        if (Boolean.TRUE.equals(student.getHasIEP()))
            flags.getChildren().add(flagBadge("IEP", "#8B5CF6"));
        if (Boolean.TRUE.equals(student.getHas504Plan()))
            flags.getChildren().add(flagBadge("504", "#F59E0B"));
        if (Boolean.TRUE.equals(student.getIsEnglishLearner()))
            flags.getChildren().add(flagBadge("ELL", "#3B82F6"));
        if (Boolean.TRUE.equals(student.getIsGifted()))
            flags.getChildren().add(flagBadge("Gifted", "#10B981"));

        if (flags.getChildren().isEmpty()) {
            Label noFlags = new Label("No special program flags");
            noFlags.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px;");
            flags.getChildren().add(noFlags);
        }

        content.getChildren().addAll(cards, contactSection, flags);
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        tab.setContent(sp);
        return tab;
    }

    // ====================================================================
    // TAB 2: GRADES
    // ====================================================================

    private static Tab buildGradesTab(ApplicationContext ctx, Student student) {
        Tab tab = new Tab("Grades");

        try {
            StudentGradeRepository repo = ctx.getBean(StudentGradeRepository.class);
            List<StudentGrade> grades = repo.findByStudentId(student.getId());

            TableView<StudentGrade> table = new TableView<>(FXCollections.observableArrayList(grades));
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
            table.setPlaceholder(new Label("No grade records found"));

            TableColumn<StudentGrade, String> courseCol = new TableColumn<>("Course");
            courseCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCourseName()));
            courseCol.setPrefWidth(180);

            TableColumn<StudentGrade, String> letterCol = new TableColumn<>("Grade");
            letterCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getLetterGrade() != null ? c.getValue().getLetterGrade() : "—"));
            letterCol.setPrefWidth(60);

            TableColumn<StudentGrade, String> numCol = new TableColumn<>("Numerical");
            numCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNumericalGrade() != null ? String.format("%.1f%%", c.getValue().getNumericalGrade()) : "—"));
            numCol.setPrefWidth(80);

            TableColumn<StudentGrade, String> gpaCol = new TableColumn<>("GPA Pts");
            gpaCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getGpaPoints() != null ? String.format("%.2f", c.getValue().getGpaPoints()) : "—"));
            gpaCol.setPrefWidth(70);

            TableColumn<StudentGrade, String> termCol = new TableColumn<>("Term");
            termCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTerm() != null ? c.getValue().getTerm() : "—"));
            termCol.setPrefWidth(100);

            table.getColumns().addAll(courseCol, letterCol, numCol, gpaCol, termCol);
            tab.setContent(table);
        } catch (Exception e) {
            log.error("Error loading grades for student {}", student.getId(), e);
            tab.setContent(errorLabel("Could not load grades: " + e.getMessage()));
        }

        return tab;
    }

    // ====================================================================
    // TAB 3: DISCIPLINE
    // ====================================================================

    private static Tab buildDisciplineTab(ApplicationContext ctx, Student student) {
        Tab tab = new Tab("Discipline");

        try {
            BehaviorIncidentRepository repo = ctx.getBean(BehaviorIncidentRepository.class);
            List<BehaviorIncident> incidents = repo.findByStudent(student);

            TableView<BehaviorIncident> table = new TableView<>(FXCollections.observableArrayList(incidents));
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
            table.setPlaceholder(new Label("No discipline records found"));

            TableColumn<BehaviorIncident, String> dateCol = new TableColumn<>("Date");
            dateCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getIncidentDate() != null
                    ? c.getValue().getIncidentDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : "—"));
            dateCol.setPrefWidth(90);

            TableColumn<BehaviorIncident, String> catCol = new TableColumn<>("Category");
            catCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getBehaviorCategory() != null ? c.getValue().getBehaviorCategory().name() : "—"));
            catCol.setPrefWidth(120);

            TableColumn<BehaviorIncident, String> sevCol = new TableColumn<>("Severity");
            sevCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getSeverity() != null ? c.getValue().getSeverity().name() : "—"));
            sevCol.setPrefWidth(80);

            TableColumn<BehaviorIncident, String> statusCol = new TableColumn<>("Status");
            statusCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStatus() != null ? c.getValue().getStatus().name() : "—"));
            statusCol.setPrefWidth(90);

            TableColumn<BehaviorIncident, String> locCol = new TableColumn<>("Location");
            locCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getLocation() != null ? c.getValue().getLocation() : "—"));
            locCol.setPrefWidth(100);

            table.getColumns().addAll(dateCol, catCol, sevCol, statusCol, locCol);
            tab.setContent(table);
        } catch (Exception e) {
            log.error("Error loading discipline for student {}", student.getId(), e);
            tab.setContent(errorLabel("Could not load discipline records: " + e.getMessage()));
        }

        return tab;
    }

    // ====================================================================
    // TAB 4: ATTENDANCE
    // ====================================================================

    private static Tab buildAttendanceTab(ApplicationContext ctx, Student student) {
        Tab tab = new Tab("Attendance");

        try {
            AttendanceRecordRepository repo = ctx.getBean(AttendanceRecordRepository.class);

            Long absences = repo.countAbsencesByStudentId(student.getId());
            Long tardies = repo.countTardiesByStudentId(student.getId());
            Double rate = repo.calculateAttendanceRateByStudentId(student.getId());

            List<AttendanceRecord> records = repo.findByStudentId(student.getId());
            long presentCount = records.stream().filter(AttendanceRecord::isPresent).count();

            VBox content = new VBox(12);
            content.setPadding(new Insets(12));

            // Summary cards
            HBox summaryCards = new HBox(12);
            summaryCards.getChildren().addAll(
                summaryCard("Present", String.valueOf(presentCount), "#10B981"),
                summaryCard("Absent", absences != null ? absences.toString() : "0", "#EF4444"),
                summaryCard("Tardy", tardies != null ? tardies.toString() : "0", "#F59E0B"),
                summaryCard("Rate", rate != null ? String.format("%.1f%%", rate) : "N/A", "#3B82F6")
            );

            // Recent records table
            int limit = Math.min(records.size(), 50);
            List<AttendanceRecord> recent = records.subList(0, limit);

            TableView<AttendanceRecord> table = new TableView<>(FXCollections.observableArrayList(recent));
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
            table.setPlaceholder(new Label("No attendance records found"));
            VBox.setVgrow(table, Priority.ALWAYS);

            TableColumn<AttendanceRecord, String> dateCol = new TableColumn<>("Date");
            dateCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getAttendanceDate() != null
                    ? c.getValue().getAttendanceDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : "—"));
            dateCol.setPrefWidth(100);

            TableColumn<AttendanceRecord, String> statusCol = new TableColumn<>("Status");
            statusCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStatus() != null ? c.getValue().getStatus().name() : "—"));
            statusCol.setPrefWidth(120);

            TableColumn<AttendanceRecord, String> periodCol = new TableColumn<>("Period");
            periodCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getPeriodNumber() != null ? c.getValue().getPeriodNumber().toString() : "—"));
            periodCol.setPrefWidth(60);

            TableColumn<AttendanceRecord, String> notesCol = new TableColumn<>("Notes");
            notesCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNotes() != null ? c.getValue().getNotes() : ""));
            notesCol.setPrefWidth(200);

            table.getColumns().addAll(dateCol, statusCol, periodCol, notesCol);

            content.getChildren().addAll(summaryCards, table);
            tab.setContent(content);
        } catch (Exception e) {
            log.error("Error loading attendance for student {}", student.getId(), e);
            tab.setContent(errorLabel("Could not load attendance records: " + e.getMessage()));
        }

        return tab;
    }

    // ====================================================================
    // TAB 5: COURSES
    // ====================================================================

    private static Tab buildCoursesTab(Student student) {
        Tab tab = new Tab("Courses");

        List<Course> courses = student.getEnrolledCourses();
        if (courses == null || courses.isEmpty()) {
            tab.setContent(new Label("  No enrolled courses found."));
            return tab;
        }

        TableView<Course> table = new TableView<>(FXCollections.observableArrayList(courses));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Course, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getCourseCode() != null ? c.getValue().getCourseCode() : "—"));
        codeCol.setPrefWidth(90);

        TableColumn<Course, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getCourseName() != null ? c.getValue().getCourseName() : "—"));
        nameCol.setPrefWidth(200);

        @SuppressWarnings("deprecation")
        TableColumn<Course, String> teacherCol = new TableColumn<>("Teacher");
        teacherCol.setCellValueFactory(c -> {
            Teacher t = c.getValue().getTeacher();
            return new SimpleStringProperty(t != null ? t.getFullName() : "—");
        });
        teacherCol.setPrefWidth(150);

        @SuppressWarnings("deprecation")
        TableColumn<Course, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(c -> {
            Room r = c.getValue().getRoom();
            return new SimpleStringProperty(r != null ? r.getRoomNumber() : "—");
        });
        roomCol.setPrefWidth(70);

        table.getColumns().addAll(codeCol, nameCol, teacherCol, roomCol);
        tab.setContent(table);
        return tab;
    }

    // ====================================================================
    // HELPERS
    // ====================================================================

    private static VBox summaryCard(String title, String value, String color) {
        VBox card = new VBox(2);
        card.setPadding(new Insets(12));
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #E2E8F0; -fx-border-radius: 8;");
        card.setPrefWidth(140);

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

        Label valueLbl = new Label(value);
        valueLbl.setStyle("-fx-font-size: 22px; -fx-font-weight: 700; -fx-text-fill: " + color + ";");

        card.getChildren().addAll(titleLbl, valueLbl);
        return card;
    }

    private static Label flagBadge(String text, String color) {
        Label badge = new Label(text);
        badge.setStyle(String.format(
            "-fx-background-color: %s20; -fx-text-fill: %s; -fx-font-size: 11px; -fx-font-weight: 600; -fx-padding: 3 10; -fx-background-radius: 10;",
            color, color));
        return badge;
    }

    private static void addDetailRow(GridPane grid, int row, String label, String value) {
        Label lbl = new Label(label + ":");
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");
        lbl.setMinWidth(100);
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 12px; -fx-text-fill: #0F172A;");
        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    private static Label errorLabel(String message) {
        Label lbl = new Label(message);
        lbl.setStyle("-fx-text-fill: #EF4444; -fx-padding: 16;");
        lbl.setWrapText(true);
        return lbl;
    }
}
