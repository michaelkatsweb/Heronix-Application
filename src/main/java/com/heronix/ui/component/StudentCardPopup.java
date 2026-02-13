package com.heronix.ui.component;

import com.heronix.model.domain.*;
import com.heronix.repository.AttendanceRecordRepository;
import com.heronix.repository.BehaviorIncidentRepository;
import com.heronix.repository.DisciplinaryConsequenceRepository;
import com.heronix.repository.DisciplinaryReferralRepository;
import com.heronix.repository.EmergencyContactRepository;
import com.heronix.repository.MedicalRecordRepository;
import com.heronix.repository.ParentGuardianRepository;
import com.heronix.repository.StudentGradeRepository;
import com.heronix.repository.SuspensionRepository;
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
import java.time.LocalDate;
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
            buildContactsTab(ctx, student),
            buildMedicalTab(ctx, student),
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

        VBox content = new VBox(12);
        content.setPadding(new Insets(12));

        try {
            BehaviorIncidentRepository incidentRepo = ctx.getBean(BehaviorIncidentRepository.class);
            SuspensionRepository suspensionRepo = ctx.getBean(SuspensionRepository.class);
            DisciplinaryConsequenceRepository consequenceRepo = ctx.getBean(DisciplinaryConsequenceRepository.class);
            DisciplinaryReferralRepository referralRepo = ctx.getBean(DisciplinaryReferralRepository.class);

            List<BehaviorIncident> incidents = incidentRepo.findByStudent(student);
            List<Suspension> suspensions = suspensionRepo.findByStudentIdOrderByStartDateDesc(student.getId());
            List<DisciplinaryConsequence> consequences = consequenceRepo.findByStudentIdOrderByStartDateDesc(student.getId());
            List<DisciplinaryReferral> referrals = referralRepo.findByStudentIdOrderByReferralDateDesc(student.getId());

            // Count active suspensions and suspension days for this school year
            List<Suspension> activeSuspensions = suspensions.stream()
                .filter(Suspension::isActive).toList();
            LocalDate yearStart = LocalDate.now().getMonthValue() >= 7
                ? LocalDate.of(LocalDate.now().getYear(), 7, 1)
                : LocalDate.of(LocalDate.now().getYear() - 1, 7, 1);
            Integer totalDays = suspensionRepo.getTotalSuspensionDaysByStudent(
                student.getId(), yearStart, LocalDate.now());
            Integer ossDays = suspensionRepo.getTotalOSSDaysByStudent(
                student.getId(), yearStart, LocalDate.now());
            long activeConsequences = consequences.stream()
                .filter(DisciplinaryConsequence::isActive).count();
            long pendingReferrals = referrals.stream()
                .filter(DisciplinaryReferral::isPending).count();

            // ── Summary Cards ──
            HBox summaryCards = new HBox(10);
            summaryCards.getChildren().addAll(
                summaryCard("Incidents", String.valueOf(incidents.size()), "#3B82F6"),
                summaryCard("Suspensions", String.valueOf(suspensions.size()), "#EF4444"),
                summaryCard("ISS/OSS Days",
                    (totalDays != null ? totalDays : 0) + " (" + (ossDays != null ? ossDays : 0) + " OSS)",
                    "#F59E0B"),
                summaryCard("Referrals", String.valueOf(referrals.size()), "#8B5CF6")
            );
            content.getChildren().add(summaryCards);

            // ── Active Alert Banner ──
            if (!activeSuspensions.isEmpty() || activeConsequences > 0 || pendingReferrals > 0) {
                HBox alertBox = new HBox(8);
                alertBox.setPadding(new Insets(8, 14, 8, 14));
                alertBox.setAlignment(Pos.CENTER_LEFT);
                alertBox.setStyle("-fx-background-color: #FEF2F2; -fx-background-radius: 8; -fx-border-color: #FECACA; -fx-border-radius: 8;");
                Label alertIcon = new Label("!");
                alertIcon.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 700; -fx-padding: 0 6; -fx-background-radius: 10;");

                StringBuilder alertMsg = new StringBuilder();
                if (!activeSuspensions.isEmpty()) {
                    Suspension active = activeSuspensions.get(0);
                    alertMsg.append(active.isInSchool() ? "Active ISS" : "Active OSS");
                    alertMsg.append(" — ").append(active.getDaysRemaining()).append(" day(s) remaining");
                }
                if (activeConsequences > 0) {
                    if (!alertMsg.isEmpty()) alertMsg.append("  |  ");
                    alertMsg.append(activeConsequences).append(" active consequence(s)");
                }
                if (pendingReferrals > 0) {
                    if (!alertMsg.isEmpty()) alertMsg.append("  |  ");
                    alertMsg.append(pendingReferrals).append(" pending referral(s)");
                }
                Label alertText = new Label(alertMsg.toString());
                alertText.setStyle("-fx-text-fill: #991B1B; -fx-font-size: 11px; -fx-font-weight: 600;");
                alertText.setWrapText(true);
                alertBox.getChildren().addAll(alertIcon, alertText);
                content.getChildren().add(alertBox);
            }

            // ── IDEA/504 Cumulative Day Warning ──
            int cumDays = totalDays != null ? totalDays : 0;
            if (cumDays >= 7 && (Boolean.TRUE.equals(student.getHasIEP()) || Boolean.TRUE.equals(student.getHas504Plan()))) {
                HBox ideaBox = new HBox(8);
                ideaBox.setPadding(new Insets(8, 14, 8, 14));
                ideaBox.setAlignment(Pos.CENTER_LEFT);
                String bgColor = cumDays >= 10 ? "#FEF2F2" : "#FFFBEB";
                String borderColor = cumDays >= 10 ? "#FECACA" : "#FDE68A";
                String textColor = cumDays >= 10 ? "#991B1B" : "#92400E";
                String iconBg = cumDays >= 10 ? "#EF4444" : "#F59E0B";
                ideaBox.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 8; -fx-border-color: " + borderColor + "; -fx-border-radius: 8;");
                Label icon = new Label(cumDays >= 10 ? "!" : "~");
                icon.setStyle("-fx-background-color: " + iconBg + "; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 700; -fx-padding: 0 6; -fx-background-radius: 10;");
                String programs = Boolean.TRUE.equals(student.getHasIEP()) ? "IEP" : "";
                if (Boolean.TRUE.equals(student.getHas504Plan())) programs += (programs.isEmpty() ? "" : "/") + "504";
                String msg = cumDays >= 10
                    ? "COMPLIANCE: " + cumDays + " cumulative removal days (" + programs + ") — MDR required"
                    : "WARNING: " + cumDays + " cumulative removal days (" + programs + ") — approaching 10-day threshold";
                Label text = new Label(msg);
                text.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 11px; -fx-font-weight: 600;");
                text.setWrapText(true);
                ideaBox.getChildren().addAll(icon, text);
                content.getChildren().add(ideaBox);
            }

            // ── Behavior Incidents Table ──
            Label incidentsTitle = sectionTitle("Behavior Incidents (" + incidents.size() + ")");
            content.getChildren().add(incidentsTitle);

            TableView<BehaviorIncident> incidentTable = new TableView<>(FXCollections.observableArrayList(incidents));
            incidentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
            incidentTable.setPlaceholder(new Label("No behavior incidents"));
            incidentTable.setPrefHeight(180);

            TableColumn<BehaviorIncident, String> dateCol = new TableColumn<>("Date");
            dateCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getIncidentDate() != null
                    ? c.getValue().getIncidentDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : "—"));
            dateCol.setPrefWidth(85);

            TableColumn<BehaviorIncident, String> catCol = new TableColumn<>("Category");
            catCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getBehaviorCategory() != null ? formatEnum(c.getValue().getBehaviorCategory().name()) : "—"));
            catCol.setPrefWidth(110);

            TableColumn<BehaviorIncident, String> sevCol = new TableColumn<>("Severity");
            sevCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getSeverity() != null ? c.getValue().getSeverity().name() : "—"));
            sevCol.setPrefWidth(80);
            sevCol.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        String color = switch (item) {
                            case "SEVERE" -> "-fx-text-fill: #DC2626; -fx-font-weight: 700;";
                            case "MAJOR" -> "-fx-text-fill: #EA580C; -fx-font-weight: 600;";
                            case "MODERATE" -> "-fx-text-fill: #D97706;";
                            default -> "-fx-text-fill: #64748B;";
                        };
                        setStyle(color);
                    }
                }
            });

            TableColumn<BehaviorIncident, String> statusCol = new TableColumn<>("Status");
            statusCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStatus() != null ? formatEnum(c.getValue().getStatus().name()) : "—"));
            statusCol.setPrefWidth(85);

            TableColumn<BehaviorIncident, String> locCol = new TableColumn<>("Location");
            locCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getLocation() != null ? formatEnum(c.getValue().getLocation()) : "—"));
            locCol.setPrefWidth(90);

            incidentTable.getColumns().addAll(dateCol, catCol, sevCol, statusCol, locCol);
            content.getChildren().add(incidentTable);

            // ── Suspensions Table ──
            Label suspTitle = sectionTitle("Suspensions (" + suspensions.size() + ")");
            content.getChildren().add(suspTitle);

            TableView<Suspension> suspTable = new TableView<>(FXCollections.observableArrayList(suspensions));
            suspTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
            suspTable.setPlaceholder(new Label("No suspensions"));
            suspTable.setPrefHeight(150);

            TableColumn<Suspension, String> sTypeCol = new TableColumn<>("Type");
            sTypeCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getSuspensionType() != null ? formatEnum(c.getValue().getSuspensionType().name()) : "—"));
            sTypeCol.setPrefWidth(100);
            sTypeCol.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); setStyle(""); return; }
                    setText(item);
                    if (item.contains("Out") || item.contains("Extended")) {
                        setStyle("-fx-text-fill: #DC2626; -fx-font-weight: 600;");
                    } else if (item.contains("Emergency")) {
                        setStyle("-fx-text-fill: #EA580C; -fx-font-weight: 600;");
                    } else {
                        setStyle("-fx-text-fill: #D97706;");
                    }
                }
            });

            TableColumn<Suspension, String> sStartCol = new TableColumn<>("Start");
            sStartCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStartDate() != null
                    ? c.getValue().getStartDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : "—"));
            sStartCol.setPrefWidth(85);

            TableColumn<Suspension, String> sEndCol = new TableColumn<>("End");
            sEndCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getEndDate() != null
                    ? c.getValue().getEndDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : "—"));
            sEndCol.setPrefWidth(85);

            TableColumn<Suspension, String> sDaysCol = new TableColumn<>("Days");
            sDaysCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDaysCount() != null ? c.getValue().getDaysCount().toString() : "—"));
            sDaysCol.setPrefWidth(45);

            TableColumn<Suspension, String> sReasonCol = new TableColumn<>("Reason");
            sReasonCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getSuspensionReason() != null ? c.getValue().getSuspensionReason() : "—"));
            sReasonCol.setPrefWidth(140);

            TableColumn<Suspension, String> sStatusCol = new TableColumn<>("Status");
            sStatusCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStatus() != null ? formatEnum(c.getValue().getStatus().name()) : "—"));
            sStatusCol.setPrefWidth(80);
            sStatusCol.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); setStyle(""); return; }
                    setText(item);
                    if ("Active".equals(item)) {
                        setStyle("-fx-text-fill: #DC2626; -fx-font-weight: 700;");
                    } else if ("Pending".equals(item)) {
                        setStyle("-fx-text-fill: #D97706; -fx-font-weight: 600;");
                    } else {
                        setStyle("");
                    }
                }
            });

            suspTable.getColumns().addAll(sTypeCol, sStartCol, sEndCol, sDaysCol, sReasonCol, sStatusCol);
            content.getChildren().add(suspTable);

            // ── Consequences Table ──
            Label consTitle = sectionTitle("Consequences (" + consequences.size() + ")");
            content.getChildren().add(consTitle);

            TableView<DisciplinaryConsequence> consTable = new TableView<>(FXCollections.observableArrayList(consequences));
            consTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
            consTable.setPlaceholder(new Label("No consequences assigned"));
            consTable.setPrefHeight(150);

            TableColumn<DisciplinaryConsequence, String> cTypeCol = new TableColumn<>("Type");
            cTypeCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getConsequenceType() != null ? formatEnum(c.getValue().getConsequenceType().name()) : "—"));
            cTypeCol.setPrefWidth(130);

            TableColumn<DisciplinaryConsequence, String> cStartCol = new TableColumn<>("Start");
            cStartCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStartDate() != null
                    ? c.getValue().getStartDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : "—"));
            cStartCol.setPrefWidth(85);

            TableColumn<DisciplinaryConsequence, String> cDurCol = new TableColumn<>("Duration");
            cDurCol.setCellValueFactory(c -> {
                DisciplinaryConsequence dc = c.getValue();
                if (dc.getDuration() != null) return new SimpleStringProperty(dc.getDuration() + " day(s)");
                return new SimpleStringProperty("—");
            });
            cDurCol.setPrefWidth(70);

            TableColumn<DisciplinaryConsequence, String> cDescCol = new TableColumn<>("Description");
            cDescCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDescription() != null ? c.getValue().getDescription() : "—"));
            cDescCol.setPrefWidth(150);

            TableColumn<DisciplinaryConsequence, String> cStatusCol = new TableColumn<>("Status");
            cStatusCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStatus() != null ? formatEnum(c.getValue().getStatus().name()) : "—"));
            cStatusCol.setPrefWidth(90);

            consTable.getColumns().addAll(cTypeCol, cStartCol, cDurCol, cDescCol, cStatusCol);
            content.getChildren().add(consTable);

        } catch (Exception e) {
            log.error("Error loading discipline for student {}", student.getId(), e);
            content.getChildren().add(errorLabel("Could not load discipline records: " + e.getMessage()));
        }

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        tab.setContent(sp);
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
    // TAB 6: CONTACTS (Parents & Emergency Contacts)
    // ====================================================================

    private static Tab buildContactsTab(ApplicationContext ctx, Student student) {
        Tab tab = new Tab("Contacts");

        VBox content = new VBox(16);
        content.setPadding(new Insets(16));

        try {
            ParentGuardianRepository parentRepo = ctx.getBean(ParentGuardianRepository.class);
            EmergencyContactRepository emergencyRepo = ctx.getBean(EmergencyContactRepository.class);

            List<ParentGuardian> parents = parentRepo.findByStudentId(student.getId());
            List<EmergencyContact> emergencyContacts = emergencyRepo.findByStudentId(student.getId());

            // ── Parents/Guardians Section ──
            Label parentsTitle = new Label("Parents / Guardians");
            parentsTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: 700; -fx-text-fill: #0F172A;");
            content.getChildren().add(parentsTitle);

            if (parents.isEmpty()) {
                Label noParents = new Label("No parent/guardian records on file.");
                noParents.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px; -fx-padding: 0 0 0 8;");
                content.getChildren().add(noParents);
            } else {
                for (ParentGuardian pg : parents) {
                    VBox card = new VBox(4);
                    card.setPadding(new Insets(10, 14, 10, 14));
                    card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8; -fx-border-color: #E2E8F0; -fx-border-radius: 8;");

                    HBox headerRow = new HBox(8);
                    headerRow.setAlignment(Pos.CENTER_LEFT);
                    Label nameLabel = new Label(pg.getFullName());
                    nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");
                    Label relLabel = new Label(pg.getRelationship() != null ? pg.getRelationship() : "");
                    relLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
                    headerRow.getChildren().addAll(nameLabel, relLabel);

                    if (Boolean.TRUE.equals(pg.getIsPrimaryCustodian())) {
                        Label badge = new Label("Primary Custodian");
                        badge.setStyle("-fx-background-color: #DBEAFE; -fx-text-fill: #1E40AF; -fx-font-size: 10px; -fx-font-weight: 600; -fx-padding: 2 8; -fx-background-radius: 8;");
                        headerRow.getChildren().add(badge);
                    }
                    card.getChildren().add(headerRow);

                    GridPane grid = new GridPane();
                    grid.setHgap(16);
                    grid.setVgap(3);
                    int row = 0;
                    if (pg.getCellPhone() != null && !pg.getCellPhone().isBlank())
                        addDetailRow(grid, row++, "Cell Phone", pg.getCellPhone());
                    if (pg.getHomePhone() != null && !pg.getHomePhone().isBlank())
                        addDetailRow(grid, row++, "Home Phone", pg.getHomePhone());
                    if (pg.getWorkPhone() != null && !pg.getWorkPhone().isBlank())
                        addDetailRow(grid, row++, "Work Phone", pg.getWorkPhone());
                    if (pg.getEmail() != null && !pg.getEmail().isBlank())
                        addDetailRow(grid, row++, "Email", pg.getEmail());
                    card.getChildren().add(grid);

                    content.getChildren().add(card);
                }
            }

            // ── Emergency Contacts Section ──
            Label ecTitle = new Label("Emergency Contacts");
            ecTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: 700; -fx-text-fill: #0F172A; -fx-padding: 8 0 0 0;");
            content.getChildren().add(ecTitle);

            if (emergencyContacts.isEmpty()) {
                Label noEc = new Label("No emergency contacts on file.");
                noEc.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px; -fx-padding: 0 0 0 8;");
                content.getChildren().add(noEc);
            } else {
                // Sort by priority
                emergencyContacts.sort((a, b) -> {
                    int pa = a.getPriorityOrder() != null ? a.getPriorityOrder() : 99;
                    int pb = b.getPriorityOrder() != null ? b.getPriorityOrder() : 99;
                    return Integer.compare(pa, pb);
                });

                for (EmergencyContact ec : emergencyContacts) {
                    VBox card = new VBox(4);
                    card.setPadding(new Insets(10, 14, 10, 14));
                    card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8; -fx-border-color: #E2E8F0; -fx-border-radius: 8;");

                    HBox headerRow = new HBox(8);
                    headerRow.setAlignment(Pos.CENTER_LEFT);
                    Label priorityLabel = new Label("#" + (ec.getPriorityOrder() != null ? ec.getPriorityOrder() : "?"));
                    priorityLabel.setStyle("-fx-background-color: #1E3A5F; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 700; -fx-padding: 2 8; -fx-background-radius: 8;");
                    Label nameLabel = new Label(ec.getFullName());
                    nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");
                    Label relLabel = new Label(ec.getRelationship() != null ? ec.getRelationship() : "");
                    relLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
                    headerRow.getChildren().addAll(priorityLabel, nameLabel, relLabel);

                    if (Boolean.TRUE.equals(ec.getAuthorizedToPickUp())) {
                        Label badge = new Label("Authorized Pickup");
                        badge.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-font-size: 10px; -fx-font-weight: 600; -fx-padding: 2 8; -fx-background-radius: 8;");
                        headerRow.getChildren().add(badge);
                    }
                    card.getChildren().add(headerRow);

                    GridPane grid = new GridPane();
                    grid.setHgap(16);
                    grid.setVgap(3);
                    int row = 0;
                    if (ec.getPrimaryPhone() != null && !ec.getPrimaryPhone().isBlank())
                        addDetailRow(grid, row++, "Primary Phone", ec.getPrimaryPhone());
                    if (ec.getSecondaryPhone() != null && !ec.getSecondaryPhone().isBlank())
                        addDetailRow(grid, row++, "Secondary Phone", ec.getSecondaryPhone());
                    card.getChildren().add(grid);

                    content.getChildren().add(card);
                }
            }
        } catch (Exception e) {
            log.error("Error loading contacts for student {}", student.getId(), e);
            content.getChildren().add(errorLabel("Could not load contacts: " + e.getMessage()));
        }

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        tab.setContent(sp);
        return tab;
    }

    // ====================================================================
    // TAB 7: MEDICAL
    // ====================================================================

    private static Tab buildMedicalTab(ApplicationContext ctx, Student student) {
        Tab tab = new Tab("Medical");

        VBox content = new VBox(16);
        content.setPadding(new Insets(16));

        try {
            MedicalRecordRepository repo = ctx.getBean(MedicalRecordRepository.class);
            Optional<MedicalRecord> medOpt = repo.findByStudentId(student.getId());

            if (medOpt.isEmpty()) {
                Label noRecord = new Label("No medical information on file.");
                noRecord.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13px; -fx-padding: 24;");
                content.getChildren().add(noRecord);
            } else {
                MedicalRecord med = medOpt.get();

                // ── Medical Alert Banner ──
                if (med.getMedicalAlert() != null && !med.getMedicalAlert().isBlank()) {
                    HBox alertBox = new HBox(8);
                    alertBox.setPadding(new Insets(10, 14, 10, 14));
                    alertBox.setAlignment(Pos.CENTER_LEFT);
                    alertBox.setStyle("-fx-background-color: #FEF2F2; -fx-background-radius: 8; -fx-border-color: #FECACA; -fx-border-radius: 8;");
                    Label alertIcon = new Label("!");
                    alertIcon.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: 700; -fx-padding: 0 6; -fx-background-radius: 10;");
                    Label alertText = new Label(med.getMedicalAlert());
                    alertText.setStyle("-fx-text-fill: #991B1B; -fx-font-size: 12px; -fx-font-weight: 600;");
                    alertText.setWrapText(true);
                    alertBox.getChildren().addAll(alertIcon, alertText);
                    content.getChildren().add(alertBox);
                }

                // ── Allergies Section ──
                Label allergiesTitle = new Label("Allergies");
                allergiesTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: 700; -fx-text-fill: #0F172A;");
                content.getChildren().add(allergiesTitle);

                boolean hasAnyAllergy = false;
                VBox allergyBox = new VBox(4);
                allergyBox.setPadding(new Insets(10, 14, 10, 14));
                allergyBox.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8; -fx-border-color: #E2E8F0; -fx-border-radius: 8;");

                GridPane allergyGrid = new GridPane();
                allergyGrid.setHgap(16);
                allergyGrid.setVgap(4);
                int row = 0;

                if (med.getFoodAllergies() != null && !med.getFoodAllergies().isBlank()) {
                    addDetailRow(allergyGrid, row++, "Food", med.getFoodAllergies());
                    hasAnyAllergy = true;
                }
                if (med.getMedicationAllergies() != null && !med.getMedicationAllergies().isBlank()) {
                    addDetailRow(allergyGrid, row++, "Medication", med.getMedicationAllergies());
                    hasAnyAllergy = true;
                }
                if (med.getEnvironmentalAllergies() != null && !med.getEnvironmentalAllergies().isBlank()) {
                    addDetailRow(allergyGrid, row++, "Environmental", med.getEnvironmentalAllergies());
                    hasAnyAllergy = true;
                }

                if (hasAnyAllergy) {
                    HBox badges = new HBox(6);
                    badges.setPadding(new Insets(4, 0, 0, 0));
                    if (med.getAllergySeverity() != null && med.getAllergySeverity() != MedicalRecord.AllergySeverity.NONE) {
                        String sevColor = switch (med.getAllergySeverity()) {
                            case LIFE_THREATENING -> "#991B1B";
                            case SEVERE -> "#EF4444";
                            case MODERATE -> "#F59E0B";
                            default -> "#64748B";
                        };
                        badges.getChildren().add(flagBadge(med.getAllergySeverity().name(), sevColor));
                    }
                    if (Boolean.TRUE.equals(med.getHasEpiPen())) {
                        badges.getChildren().add(flagBadge("EpiPen", "#DC2626"));
                    }
                    if (!badges.getChildren().isEmpty()) {
                        allergyGrid.add(badges, 0, row, 2, 1);
                    }
                    allergyBox.getChildren().add(allergyGrid);
                    content.getChildren().add(allergyBox);
                } else {
                    Label noAllergies = new Label("No known allergies.");
                    noAllergies.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px; -fx-padding: 0 0 0 8;");
                    content.getChildren().add(noAllergies);
                }

                // ── Conditions Section ──
                Label condTitle = new Label("Conditions");
                condTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: 700; -fx-text-fill: #0F172A;");
                content.getChildren().add(condTitle);

                boolean hasAnyCondition = false;
                VBox condBox = new VBox(4);
                condBox.setPadding(new Insets(10, 14, 10, 14));
                condBox.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8; -fx-border-color: #E2E8F0; -fx-border-radius: 8;");
                VBox condList = new VBox(4);

                if (med.getChronicConditions() != null && !med.getChronicConditions().isBlank()) {
                    condList.getChildren().add(conditionRow("Chronic", med.getChronicConditions()));
                    hasAnyCondition = true;
                }
                if (Boolean.TRUE.equals(med.getHasAsthma())) {
                    String details = "Asthma" + (med.getAsthmaSeverity() != null ? " (" + med.getAsthmaSeverity() + ")" : "");
                    condList.getChildren().add(conditionRow("Asthma", details));
                    hasAnyCondition = true;
                }
                if (Boolean.TRUE.equals(med.getHasDiabetes())) {
                    String details = "Diabetes" + (med.getDiabetesType() != null ? " (" + med.getDiabetesType() + ")" : "");
                    condList.getChildren().add(conditionRow("Diabetes", details));
                    hasAnyCondition = true;
                }
                if (Boolean.TRUE.equals(med.getHasSeizureDisorder())) {
                    condList.getChildren().add(conditionRow("Seizure Disorder", med.getSeizureDetails() != null ? med.getSeizureDetails() : "Yes"));
                    hasAnyCondition = true;
                }

                if (hasAnyCondition) {
                    condBox.getChildren().add(condList);
                    content.getChildren().add(condBox);
                } else {
                    Label noCond = new Label("No chronic conditions recorded.");
                    noCond.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px; -fx-padding: 0 0 0 8;");
                    content.getChildren().add(noCond);
                }

                // ── Medications Section ──
                Label medsTitle = new Label("Medications");
                medsTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: 700; -fx-text-fill: #0F172A;");
                content.getChildren().add(medsTitle);

                if (med.getCurrentMedications() != null && !med.getCurrentMedications().isBlank()) {
                    VBox medsBox = new VBox(4);
                    medsBox.setPadding(new Insets(10, 14, 10, 14));
                    medsBox.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8; -fx-border-color: #E2E8F0; -fx-border-radius: 8;");

                    GridPane medsGrid = new GridPane();
                    medsGrid.setHgap(16);
                    medsGrid.setVgap(4);
                    int mRow = 0;
                    addDetailRow(medsGrid, mRow++, "Medications", med.getCurrentMedications());
                    if (med.getMedicationSchedule() != null && !med.getMedicationSchedule().isBlank())
                        addDetailRow(medsGrid, mRow++, "Schedule", med.getMedicationSchedule());
                    if (Boolean.TRUE.equals(med.getSelfAdministers()))
                        addDetailRow(medsGrid, mRow++, "Self-Administers", "Yes");
                    medsBox.getChildren().add(medsGrid);
                    content.getChildren().add(medsBox);
                } else {
                    Label noMeds = new Label("No current medications.");
                    noMeds.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px; -fx-padding: 0 0 0 8;");
                    content.getChildren().add(noMeds);
                }

                // ── Emergency & Restrictions Section ──
                Label emerTitle = new Label("Emergency Info & Restrictions");
                emerTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: 700; -fx-text-fill: #0F172A;");
                content.getChildren().add(emerTitle);

                VBox emerBox = new VBox(4);
                emerBox.setPadding(new Insets(10, 14, 10, 14));
                emerBox.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8; -fx-border-color: #E2E8F0; -fx-border-radius: 8;");
                GridPane emerGrid = new GridPane();
                emerGrid.setHgap(16);
                emerGrid.setVgap(4);
                int eRow = 0;
                boolean hasEmerInfo = false;

                if (med.getEmergencyActionPlan() != null && !med.getEmergencyActionPlan().isBlank()) {
                    addDetailRow(emerGrid, eRow++, "Action Plan", med.getEmergencyActionPlan());
                    hasEmerInfo = true;
                }
                if (med.getDietaryRestrictions() != null && !med.getDietaryRestrictions().isBlank()) {
                    addDetailRow(emerGrid, eRow++, "Dietary", med.getDietaryRestrictions());
                    hasEmerInfo = true;
                }
                if (med.getPhysicalRestrictions() != null && !med.getPhysicalRestrictions().isBlank()) {
                    addDetailRow(emerGrid, eRow++, "Physical", med.getPhysicalRestrictions());
                    hasEmerInfo = true;
                }
                if (med.getPrimaryPhysicianName() != null && !med.getPrimaryPhysicianName().isBlank()) {
                    String physInfo = med.getPrimaryPhysicianName();
                    if (med.getPrimaryPhysicianPhone() != null && !med.getPrimaryPhysicianPhone().isBlank())
                        physInfo += " \u2014 " + med.getPrimaryPhysicianPhone();
                    addDetailRow(emerGrid, eRow++, "Physician", physInfo);
                    hasEmerInfo = true;
                }

                if (hasEmerInfo) {
                    emerBox.getChildren().add(emerGrid);
                    content.getChildren().add(emerBox);
                } else {
                    Label noEmer = new Label("No emergency info or restrictions on file.");
                    noEmer.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px; -fx-padding: 0 0 0 8;");
                    content.getChildren().add(noEmer);
                }
            }
        } catch (Exception e) {
            log.error("Error loading medical record for student {}", student.getId(), e);
            content.getChildren().add(errorLabel("Could not load medical records: " + e.getMessage()));
        }

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        tab.setContent(sp);
        return tab;
    }

    private static HBox conditionRow(String label, String value) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label + ":");
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B; -fx-min-width: 100;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 12px; -fx-text-fill: #0F172A;");
        val.setWrapText(true);
        row.getChildren().addAll(lbl, val);
        return row;
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

    private static Label sectionTitle(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #0F172A; -fx-padding: 4 0 0 0;");
        return lbl;
    }

    /** Converts ENUM_NAME to Title Case (e.g., OUT_OF_SCHOOL → Out Of School). */
    private static String formatEnum(String raw) {
        if (raw == null || raw.isEmpty()) return raw;
        String[] parts = raw.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }

    private static Label errorLabel(String message) {
        Label lbl = new Label(message);
        lbl.setStyle("-fx-text-fill: #EF4444; -fx-padding: 16;");
        lbl.setWrapText(true);
        return lbl;
    }
}
